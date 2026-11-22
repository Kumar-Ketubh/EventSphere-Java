/**
 * Theme colours for JavaScript-only consumers (canvas confetti, component props).
 * Components that accept CSS strings take `var(--token)`; canvas code needs the resolved value.
 */
export const cssVar = (name) => `var(--${name})`;

export function token(name) {
  return getComputedStyle(document.documentElement).getPropertyValue(`--${name}`).trim();
}
