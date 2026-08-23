# v1.2.0

**New**
- **Ptyxis themes** — 44 terminal-inspired palettes (Nord, Dracula-adjacent Synthwave, Kanagawa, Monokai Pro, Solarized, GitHub, and more) alongside a new palette picker.
- **Catppuccin, all 4 flavors** — Latte, Frappé, Macchiato, and Mocha are now all selectable (previously Mocha-only), each with all 14 accent colors.
- **13 new fonts** — Nunito, Cormorant Garamond, IBM Plex Mono/Serif, Instrument Serif, Playfair Display, Quicksand, Space Grotesk, Turret Road, Viaoda Libre, DotGothic16, Press Start 2P, and Egyptian Hieroglyphs, added to the Font Family picker. Picking a hard-to-read display font (Press Start 2P, DotGothic16) now shows a confirmation first.
- **Per-feed accent color** — each feed's list row is now subtly tinted with a color extracted from its favicon.
- **Saved articles** — bookmark any article (from the article list, the saved list, or the reader) to read later; new "Saved" tab from the home screen.
- Android 13 (API 33) is now supported, down from Android 15 (API 35).

**Fixed**
- Closed a DNS-rebinding gap in feed discovery that could let a malicious feed's DNS answers point the app at private/internal network addresses.
- Fixed a bug where a biometric-enrollment change (e.g. adding/removing a fingerprint) would silently disable biometric app-lock and grant access with no authentication prompt.

# v1.1.0

**New**
- **Browser Privacy settings** — control the in-app reader: cookie policy (block all / first-party only / allow all), tracker & ad blocking, JavaScript, site data, clear-on-close, Do Not Track / GPC, Safe Browsing, and Force-HTTPS. Privacy-preserving defaults.
- **Atom feed support** — Reddit (`/r/<sub>/.rss`), YouTube, GitHub and other Atom feeds now load articles.

**Fixed**
- Cookie-consent banners (e.g. The Guardian) now work instead of leaving the page stuck.
- Article list keeps its scroll position when you come back from an article.
- HTTP feeds and links now load over HTTPS in the reader instead of failing with a "cleartext not permitted" error.
- Fixed a crash when a page opened certain inline content.
