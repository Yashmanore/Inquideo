/**
 * Formats seconds to M:SS string — exact port of formatTime() from query.js.
 *
 * Examples:
 *   275.28 → "4:35"
 *   305.28 → "5:05"
 *   0      → "0:00"
 *
 * @param {number} seconds
 * @returns {string}
 */
export function formatTime(seconds) {
  const totalSeconds = Math.floor(Number(seconds) || 0)
  const m = Math.floor(totalSeconds / 60)
  const s = (totalSeconds % 60).toString().padStart(2, '0')
  return `${m}:${s}`
}
