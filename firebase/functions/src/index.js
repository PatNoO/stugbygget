const { onSchedule } = require("firebase-functions/v2/scheduler");
const { onCall, HttpsError } = require("firebase-functions/v2/https");
const logger = require("firebase-functions/logger");
const admin = require("firebase-admin");
const { adapters } = require("./storeAdapters");
const { withRetry } = require("./retry");

admin.initializeApp();
const firestore = admin.firestore();

exports.generateAssistantReply = onCall(async (request) => {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "Authentication required.");
  }

  const projectId = request.data?.projectId;
  const system = request.data?.system;
  const messages = request.data?.messages;
  if (!projectId || !Array.isArray(messages) || messages.length === 0) {
    throw new HttpsError("invalid-argument", "projectId and messages are required.");
  }

  const claudeApiKey = process.env.CLAUDE_API_KEY;
  if (!claudeApiKey) {
    throw new HttpsError("failed-precondition", "CLAUDE_API_KEY is not configured on backend.");
  }

  try {
    const response = await fetch("https://api.anthropic.com/v1/messages", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "anthropic-version": "2023-06-01",
        "x-api-key": claudeApiKey
      },
      body: JSON.stringify({
        model: "claude-3-5-sonnet-latest",
        max_tokens: 1024,
        system: typeof system === "string" ? system : "",
        messages
      })
    });

    if (!response.ok) {
      const errorBody = await response.text();
      logger.error("Claude proxy request failed", {
        status: response.status,
        projectId,
        errorBody
      });
      throw new HttpsError("internal", "Claude proxy request failed.");
    }

    const payload = await response.json();
    const text = (payload.content || [])
      .filter((item) => item.type === "text")
      .map((item) => item.text || "")
      .join("")
      .trim();

    return { text: text || "Jag kunde inte generera ett svar just nu." };
  } catch (error) {
    logger.error("Claude proxy failed", {
      projectId,
      message: error?.message
    });
    throw new HttpsError("internal", "Assistant proxy failed.");
  }
});

const PRICE_DROP_THRESHOLD_PCT = 5;

/**
 * Scheduled daily price scraper.
 * Runs at 06:00 Stockholm time, fetches prices for every material in every project,
 * calculates a day-over-day delta, and triggers FCM notifications for drops > 5%.
 */
exports.ingestPrices = onSchedule("0 6 * * *", async () => {
  const projectsSnapshot = await firestore.collection("projects").get();
  const projectDocs = projectsSnapshot.docs;

  logger.info("Starting daily price ingestion", { projectCount: projectDocs.length });

  for (const projectDoc of projectDocs) {
    await ingestProjectPrices(projectDoc.id);
  }
});

/**
 * Fetches the most recent price document for a given material + store combination.
 * Returns null if no previous price exists (first run).
 */
async function getPreviousPrice(pricesRef, materialId, store) {
  const snapshot = await pricesRef
    .where("materialId", "==", materialId)
    .where("store", "==", store)
    .orderBy("fetchedAt", "desc")
    .limit(1)
    .get();
  if (snapshot.empty) return null;
  return snapshot.docs[0].get("price") ?? null;
}

/**
 * Sends an FCM push notification to all users with a registered token.
 * Failures per-token are logged but do not throw.
 */
async function notifyPriceDrop(materialName, store, oldPrice, newPrice) {
  const usersSnapshot = await firestore.collection("users").get();
  const tokens = usersSnapshot.docs
    .map((doc) => doc.get("fcmToken"))
    .filter(Boolean);

  if (tokens.length === 0) {
    logger.info("No FCM tokens found — skipping price drop notification", { materialName, store });
    return;
  }

  const changePct = Math.abs(((newPrice - oldPrice) / oldPrice) * 100).toFixed(1);
  const message = {
    notification: {
      title: "Prissänkning — " + materialName,
      body: `${store}: ${newPrice} SEK (−${changePct}% från igår)`
    },
    data: {
      screen: "materials",
      title: "Prissänkning — " + materialName,
      body: `${store}: ${newPrice} SEK (−${changePct}% från igår)`
    },
    tokens
  };

  try {
    const response = await admin.messaging().sendEachForMulticast(message);
    logger.info("FCM price-drop notification sent", {
      materialName,
      store,
      successCount: response.successCount,
      failureCount: response.failureCount
    });
  } catch (error) {
    logger.error("FCM notification failed", { materialName, store, error: error?.message });
  }
}

async function ingestProjectPrices(projectId) {
  const projectRef = firestore.collection("projects").doc(projectId);
  const materialsSnapshot = await projectRef.collection("materials").get();
  const materials = materialsSnapshot.docs.map((doc) => ({
    id: doc.id,
    name: doc.get("name") || "Unknown material"
  }));

  const pricesRef = projectRef.collection("prices");

  for (const material of materials) {
    for (const adapter of adapters) {
      try {
        const startedAt = Date.now();
        const { result, attemptCount } = await withRetry(
          () => adapter.fetchPrices({ materialName: material.name }),
          { maxAttempts: 3, baseDelayMs: 500, maxDelayMs: 5000 }
        );
        const latencyMs = Date.now() - startedAt;

        const batch = firestore.batch();
        for (const entry of result) {
          // ── Delta calculation ──
          const previousPrice = await getPreviousPrice(pricesRef, material.id, entry.store);
          let priceChangePct = null;
          let priceDrop = false;

          if (previousPrice !== null) {
            priceChangePct = ((entry.amount - previousPrice) / previousPrice) * 100;
            priceDrop = priceChangePct < -PRICE_DROP_THRESHOLD_PCT;
          }

          const docRef = pricesRef.doc();
          batch.set(docRef, {
            materialId: material.id,
            materialName: material.name,
            store: entry.store,
            price: entry.amount,
            currency: "SEK",
            inStock: entry.inStock,
            productUrl: entry.productUrl,
            fetchedAt: admin.firestore.FieldValue.serverTimestamp(),
            sourceType: adapter.sourceType,
            sourceName: adapter.sourceName,
            fetchStatus: "SUCCESS",
            fetchLatencyMs: latencyMs,
            attemptCount,
            priceChangePct: priceChangePct !== null ? Math.round(priceChangePct * 10) / 10 : null,
            priceDrop
          });

          // ── FCM notification on significant price drop ──
          if (priceDrop) {
            await notifyPriceDrop(material.name, entry.store, previousPrice, entry.amount);
          }
        }
        await batch.commit();
      } catch (error) {
        logger.error("Price fetch failed", {
          projectId,
          materialId: material.id,
          sourceName: adapter.sourceName,
          error: error?.message
        });

        await projectRef.collection("price_ingestion_failures").add({
          materialId: material.id,
          materialName: material.name,
          sourceName: adapter.sourceName,
          fetchStatus: "FAILED",
          attemptCount: 3,
          lastError: error?.message ?? "Unknown failure",
          failedAt: admin.firestore.FieldValue.serverTimestamp()
        });
      }
    }
  }
}
