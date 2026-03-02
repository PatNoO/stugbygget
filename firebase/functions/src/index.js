const { onSchedule } = require("firebase-functions/v2/scheduler");
const logger = require("firebase-functions/logger");
const admin = require("firebase-admin");
const { adapters } = require("./storeAdapters");
const { withRetry } = require("./retry");

admin.initializeApp();
const firestore = admin.firestore();

exports.ingestPrices = onSchedule("every 2 hours", async () => {
  const projectsSnapshot = await firestore.collection("projects").get();
  const projectDocs = projectsSnapshot.docs;

  logger.info("Starting price ingestion", { projectCount: projectDocs.length });

  for (const projectDoc of projectDocs) {
    const projectId = projectDoc.id;
    await ingestProjectPrices(projectId);
  }
});

async function ingestProjectPrices(projectId) {
  const projectRef = firestore.collection("projects").doc(projectId);
  const materialsSnapshot = await projectRef.collection("materials").get();
  const materials = materialsSnapshot.docs.map((doc) => ({
    id: doc.id,
    name: doc.get("name") || "Unknown material"
  }));

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
        result.forEach((entry) => {
          const docRef = projectRef.collection("prices").doc();
          batch.set(docRef, {
            materialId: material.id,
            materialName: material.name,
            store: entry.store,
            amount: entry.amount,
            price: entry.amount,
            currency: "SEK",
            inStock: entry.inStock,
            productUrl: entry.productUrl,
            fetchedAt: admin.firestore.FieldValue.serverTimestamp(),
            sourceType: adapter.sourceType,
            sourceName: adapter.sourceName,
            fetchStatus: "SUCCESS",
            fetchLatencyMs: latencyMs,
            attemptCount
          });
        });
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
