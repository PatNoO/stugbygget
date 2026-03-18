const { onSchedule } = require("firebase-functions/v2/scheduler");
const { onCall, HttpsError } = require("firebase-functions/v2/https");
const logger = require("firebase-functions/logger");
const admin = require("firebase-admin");
const { adapters } = require("./storeAdapters");
const { withRetry } = require("./retry");

admin.initializeApp();
const firestore = admin.firestore();

// ─────────────────────────────────────────────────────────────
// generateAssistantReply — callable
// Proxies messages to the Vercel AI Gateway on behalf of the Android app.
// Model is read at runtime from Firestore config/ai (field: model).
// Schema change: requires Firestore document config/ai { model: string }.
// ─────────────────────────────────────────────────────────────

const FALLBACK_MODEL = "anthropic/claude-sonnet-4-6";

/**
 * Reads the active model from Firestore config/ai.
 * Falls back to FALLBACK_MODEL if the document or field is missing.
 */
async function getActiveModel() {
  try {
    const configDoc = await firestore.collection("config").doc("ai").get();
    const model = configDoc.exists ? configDoc.get("model") : null;
    return typeof model === "string" && model.length > 0 ? model : FALLBACK_MODEL;
  } catch (err) {
    logger.warn("Could not read config/ai model — using fallback", { error: err?.message });
    return FALLBACK_MODEL;
  }
}

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

  const gatewayApiKey = process.env.VERCEL_AI_GATEWAY_API_KEY;
  if (!gatewayApiKey) {
    throw new HttpsError("failed-precondition", "VERCEL_AI_GATEWAY_API_KEY is not configured on backend.");
  }

  const gatewayUrl = process.env.VERCEL_AI_GATEWAY_URL;
  if (!gatewayUrl) {
    throw new HttpsError("failed-precondition", "VERCEL_AI_GATEWAY_URL is not configured on backend.");
  }

  const model = await getActiveModel();
  logger.info("generateAssistantReply using model", { model, projectId });

  // Build OpenAI-compatible messages array; prepend system prompt as system role message
  const openAiMessages = [];
  if (typeof system === "string" && system.length > 0) {
    openAiMessages.push({ role: "system", content: system });
  }
  openAiMessages.push(...messages);

  try {
    const response = await fetch(`${gatewayUrl}/v1/chat/completions`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${gatewayApiKey}`
      },
      body: JSON.stringify({
        model,
        messages: openAiMessages,
        max_tokens: 1024
      })
    });

    if (!response.ok) {
      const errorBody = await response.text();
      logger.error("Vercel AI Gateway request failed", { status: response.status, projectId, errorBody });
      throw new HttpsError("internal", "AI Gateway request failed.");
    }

    const payload = await response.json();
    const text = (payload.choices?.[0]?.message?.content ?? "").trim();

    return { text: text || "Jag kunde inte generera ett svar just nu." };
  } catch (error) {
    if (error instanceof HttpsError) throw error;
    logger.error("AI Gateway proxy failed", { projectId, message: error?.message });
    throw new HttpsError("internal", "Assistant proxy failed.");
  }
});

// ─────────────────────────────────────────────────────────────
// ingestPrices — scheduled daily
// Fetches prices from all store adapters, calculates day-over-day
// delta, and triggers FCM notifications for drops > 5%.
// ─────────────────────────────────────────────────────────────

const PRICE_DROP_THRESHOLD_PCT = 5;

/**
 * Scheduled daily price scraper.
 * Runs at 06:00 Stockholm time, fetches prices for every material in every project,
 * calculates a day-over-day delta, and triggers FCM notifications for drops > 5%.
 */
exports.ingestPrices = onSchedule("0 6 * * *", async () => {
  const projectsSnapshot = await firestore.collection("projects").get();

  logger.info("Starting daily price ingestion", { projectCount: projectsSnapshot.size });

  for (const projectDoc of projectsSnapshot.docs) {
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

// ─────────────────────────────────────────────────────────────
// priceCompare — callable
// Input:  { projectId, shoppingListId }
// Output: { storeTotals, bestSplit, itemsWithNoPrice }
// ─────────────────────────────────────────────────────────────

/**
 * For each item in a shopping list, finds the cheapest price per store and
 * returns both a per-store total and an optimal "best split" recommendation
 * (buy each item from whichever store is cheapest).
 */
exports.priceCompare = onCall(async (request) => {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "Authentication required.");
  }

  const { projectId, shoppingListId } = request.data ?? {};
  if (!projectId || !shoppingListId) {
    throw new HttpsError("invalid-argument", "projectId and shoppingListId are required.");
  }

  const projectRef = firestore.collection("projects").doc(projectId);

  const listDoc = await projectRef.collection("shopping_lists").doc(shoppingListId).get();
  if (!listDoc.exists) {
    throw new HttpsError("not-found", `Shopping list ${shoppingListId} not found.`);
  }
  const items = listDoc.get("items") ?? [];
  if (items.length === 0) {
    return { storeTotals: [], bestSplit: { items: [], total: 0 }, itemsWithNoPrice: [] };
  }

  // ── Load latest price per materialId per store ──
  const pricesSnapshot = await projectRef.collection("prices").get();
  // Map: materialId -> store -> { price, fetchedAt }
  const priceMap = {};
  for (const doc of pricesSnapshot.docs) {
    const materialId = doc.get("materialId");
    const store = doc.get("store");
    const price = doc.get("price");
    if (!materialId || !store || price == null) continue;
    if (!priceMap[materialId]) priceMap[materialId] = {};
    const existing = priceMap[materialId][store];
    const fetchedAt = doc.get("fetchedAt")?.toMillis() ?? 0;
    if (!existing || fetchedAt > existing.fetchedAt || price < existing.price) {
      priceMap[materialId][store] = { price, store, fetchedAt };
    }
  }

  // ── Per-item analysis ──
  const itemsWithNoPrice = [];
  const storeTotalsMap = {};
  const bestSplitItems = [];
  let bestSplitTotal = 0;

  for (const item of items) {
    const materialId = item.materialId ?? item.id;
    const itemName = item.name ?? materialId;
    const quantity = item.quantity ?? 1;
    const storeEntries = priceMap[materialId];

    if (!storeEntries || Object.keys(storeEntries).length === 0) {
      itemsWithNoPrice.push(itemName);
      continue;
    }

    for (const [store, entry] of Object.entries(storeEntries)) {
      if (!storeTotalsMap[store]) storeTotalsMap[store] = { store, total: 0, itemCount: 0, missingItems: 0 };
      storeTotalsMap[store].total += entry.price * quantity;
      storeTotalsMap[store].itemCount += 1;
    }

    const cheapest = Object.values(storeEntries).reduce((a, b) => (a.price <= b.price ? a : b));
    bestSplitItems.push({
      itemName,
      store: cheapest.store,
      pricePerUnit: cheapest.price,
      quantity,
      lineTotal: Math.round(cheapest.price * quantity * 100) / 100
    });
    bestSplitTotal += cheapest.price * quantity;
  }

  // Mark missing items on per-store totals
  const itemsWithPrice = items.length - itemsWithNoPrice.length;
  for (const entry of Object.values(storeTotalsMap)) {
    entry.missingItems = itemsWithPrice - entry.itemCount;
    entry.total = Math.round(entry.total * 100) / 100;
  }

  const storeTotals = Object.values(storeTotalsMap).sort((a, b) => a.total - b.total);
  const allEqual = storeTotals.length > 1 && storeTotals.every((s) => s.total === storeTotals[0].total);

  return {
    storeTotals,
    bestSplit: {
      items: bestSplitItems,
      total: Math.round(bestSplitTotal * 100) / 100,
      note: allEqual ? "All stores have equal prices for this list." : null
    },
    itemsWithNoPrice
  };
});

