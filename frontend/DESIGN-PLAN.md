# EventSphere design plan

Follows the repo's CLAUDE.md. Stack: Vite + React, Tailwind 4, shadcn/ui, Magic UI (installed into `src/components/ui`), Motion. All colours are CSS variables in `src/index.css` (this is a Vite app, so there is no `app/globals.css`).

## Direction: calm and premium
A dark, quiet pine theme with one soft sand-gold accent. Nothing bright: the live status uses a muted clay instead of red. The site runs in dark mode (`<html class="dark">`), so Magic UI components use their dark styles.

## Colour
| Token | Hex | Use |
|---|---|---|
| pine | #0F1A17 | Dominant colour: page background |
| surface / raised | #16231F / #1E2E29 | Cards, panels, date blocks |
| line | #2A3B35 | Borders |
| mist | #E6EBE7 | Main text |
| sage | #9AABA4 | Secondary text, pattern dots |
| sand | #D6B47A | The one accent: main buttons, current nav tab, focus ring, highlights. Pine text on it |
| clay | #D98B78 | "On now" and errors (muted, not alarm red) |

`bg-hero` (in index.css) layers two soft glows over the pine ground for the hero's depth.

## Type
- Display: Fraunces, weight extremes: 200 for page headlines, 800 for titles and section headings.
- Body: Bricolage Grotesque. Times and seat counts use tabular figures.

## Magic UI components and where they are used
| Component | Where |
|---|---|
| Dot Pattern | Landing hero and auth panel background |
| Text Animate | Landing headline, auth tagline, Dashboard and My bookings headings |
| Word Rotate | Landing headline: "talk. / workshop. / fest." (edited to render a span, not a nested h1) |
| Animated Shiny Text | "N events on campus this week" line; "On now" badge on tickets |
| Shimmer Button | Landing "Log in to book" and "Create account" (sand) |
| Marquee | Strip of real upcoming events under the hero |
| Bento Grid | "This week" on the landing page; spans adapt to 1-5 events; a large faint date fills each card (recoloured to the theme) |
| Number Ticker | "seats still open" on the landing page; seats left on tickets |
| Border Beam | Closing card on the landing page (sand to sage) |
| Scroll Progress | Thin sand bar at the top of the landing page (recoloured from its purple default) |
| Blur Fade | Entrances on every page; the Dashboard reveals once on load, staggered |
| Magic Card | Every event ticket on the Dashboard (spotlight in sand and sage) |
| Confetti (canvas-confetti) | After a seat is booked, in sand, sage and mist; skipped for reduced motion |
| Animated List | Booked events on My bookings |

Left out on purpose: Aurora Text, Rainbow Button, Animated Gradient Text, Neon Gradient Card (purple-blue gradients, forbidden by CLAUDE.md), and Globe, Meteors, Particles (generic).

## Routes
`/` landing (public), `/login`, `/register`, `/dashboard` and `/profile` (need a login). A 401 sends you to `/login`.
