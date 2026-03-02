function sleep(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

async function withRetry(fn, options = {}) {
  const maxAttempts = options.maxAttempts ?? 3;
  const baseDelayMs = options.baseDelayMs ?? 400;
  const maxDelayMs = options.maxDelayMs ?? 5000;

  let attempt = 0;
  let lastError;
  while (attempt < maxAttempts) {
    attempt += 1;
    try {
      const result = await fn(attempt);
      return { result, attemptCount: attempt };
    } catch (error) {
      lastError = error;
      if (attempt >= maxAttempts) break;
      const exponential = Math.min(maxDelayMs, baseDelayMs * 2 ** (attempt - 1));
      const jitter = Math.floor(Math.random() * 150);
      await sleep(exponential + jitter);
    }
  }

  throw Object.assign(new Error(lastError?.message ?? "Retry attempts exhausted"), {
    cause: lastError
  });
}

module.exports = {
  withRetry
};
