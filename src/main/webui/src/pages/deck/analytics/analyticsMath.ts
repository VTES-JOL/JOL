/** Binomial coefficient C(n, k). */
export function comb(n: number, k: number): number {
  if (k < 0 || k > n) return 0;
  if (k === 0 || k === n) return 1;
  const r = Math.min(k, n - k);
  let result = 1;
  for (let i = 0; i < r; i++) result = (result * (n - i)) / (i + 1);
  return Math.round(result);
}

/**
 * P(drawing ≥1 copy of a card that has K copies in a crypt of N), when the
 * opening draw is `n` cards.
 */
export function openingHandProb(N: number, K: number, n = 4): number {
  if (K <= 0) return 0;
  if (N <= n) return 1;
  return 1 - comb(N - K, n) / comb(N, n);
}
