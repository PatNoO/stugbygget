/**
 * Adapter contract:
 * adapter.fetchPrices({ materialName }) => [{ store, amount, inStock, productUrl }]
 *
 * This baseline uses deterministic sample payloads for MVP stability.
 * Replace adapter internals per store with licensed API calls in production.
 */
const adapters = [
  {
    sourceName: "bauhaus",
    sourceType: "ADAPTER",
    async fetchPrices({ materialName }) {
      return [
        {
          store: "Bauhaus Kungens Kurva",
          amount: 1599.0,
          inStock: true,
          productUrl: "https://www.bauhaus.se/"
        }
      ].map((item) => ({ ...item, materialName }));
    }
  },
  {
    sourceName: "beijer",
    sourceType: "ADAPTER",
    async fetchPrices({ materialName }) {
      return [
        {
          store: "Beijer Södertälje",
          amount: 24.0,
          inStock: true,
          productUrl: "https://www.beijerbygg.se/"
        }
      ].map((item) => ({ ...item, materialName }));
    }
  },
  {
    sourceName: "byggmax",
    sourceType: "ADAPTER",
    async fetchPrices({ materialName }) {
      return [
        {
          store: "Byggmax Botkyrka",
          amount: 389.0,
          inStock: true,
          productUrl: "https://www.byggmax.se/"
        }
      ].map((item) => ({ ...item, materialName }));
    }
  }
];

module.exports = {
  adapters
};
