<div align="center">

# Marvel Spectrum
**MCU viewing order, poster-first.**

[![Platform](https://img.shields.io/badge/Android-34A853?style=flat-square&logo=android&logoColor=white)](https://android.com)
[![API Level](https://img.shields.io/badge/API-26%2B-4285f4?style=flat-square&logo=android&logoColor=white)](https://android-arsenal.com/api?level=26)
[![Kotlin](https://img.shields.io/badge/Kotlin-100%25-7c4dff?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/License-GPL_v3-4285f4?style=flat-square)](docs/LICENSE)

</div>

---

## ✨ What is Marvel Spectrum?

Marvel Spectrum is an Android/Kotlin Jetpack Compose app focused on MCU viewing orders. It keeps Rhythm's polished Material 3 design language—rounded cards, expressive headers, horizontal shelves, and smooth interaction feedback—while defaulting to a poster-driven viewing experience instead of a music player.

The active Android namespace and package are:

```text
com.cinemaverse.mcu
```

Music and streaming integrations are disabled for viewing-first builds. Bundled MCU JSON and local poster assets are the primary offline data source, while TMDB and OMDb keys are optional enrichment only.

## 🛠 Build

```bash
./gradlew :app:assembleGithubDebug
```

Do not commit API keys or generated secrets.

---

## 🏆 Credits and attribution

Marvel Spectrum is adapted from the original Rhythm Android project by Team ChromaHub. Rhythm attribution, credits, licensing, and upstream source references are intentionally preserved in About/credits documentation.

---

## Marvel Spectrum viewing metadata setup

Marvel Spectrum is viewing-first: MCU titles, curated viewing orders, and bundled poster artwork work offline without asking users to scan a music library or configure streaming services.

### Bundled JSON metadata

Local MCU metadata lives in:

```text
app/src/main/assets/mcu_data/mcu_titles.json
app/src/main/assets/mcu_data/posters.json
```

`mcu_titles.json` provides viewing-order records such as title, type, series, saga, `viewingOrder`, release date, and `posterPath`. `posters.json` maps known ids/titles to poster filenames. The Android loader reads both files with `context.assets.open(...)`, merges them with the curated Kotlin data in `ViewingLists.kt`, and keeps richer curated fields such as phase, runtime, plot, genres, IMDb/TMDB IDs, trailer URLs, saga, and order whenever they are already available.

### Bundled local posters

Poster images are exposed to the app as assets at:

```text
app/src/main/assets/mcu_posters/
```

Poster files are stored directly under the Android assets tree so filenames can stay exactly as referenced by JSON, including names that are not Android drawable-resource safe. A JSON value such as:

```json
{ "posterPath": "001-captain-america-the-first-avenger.jpg" }
```

maps to the Coil-loadable URL:

```text
file:///android_asset/mcu_posters/001-captain-america-the-first-avenger.jpg
```

Missing poster files are handled safely during data loading: the loader skips the missing local asset URL, logs a warning, and lets the UI fall back to other artwork sources.

### Artwork priority

Marvel Spectrum resolves viewing artwork in this order:

1. Bundled local asset poster/backdrop: `file:///android_asset/mcu_posters/<posterPath>`
2. Valid local curated poster/backdrop override
3. TMDB poster/backdrop
4. OMDb poster
5. Built-in Marvel-style fallback artwork

### Optional OMDb and TMDB enrichment

API keys are optional and are only used to enrich missing metadata or remote artwork. Missing keys do not break the app because local JSON metadata and poster assets are bundled.

```bash
export OMDB_API_KEY="YOUR_OMDB_API_KEY_HERE"
export TMDB_API_KEY="YOUR_TMDB_API_KEY_HERE"
export TMDB_READ_ACCESS_TOKEN="YOUR_TMDB_READ_ACCESS_TOKEN_HERE"
./gradlew :app:assembleGithubDebug
```

Do not commit real API secrets. `.env.example` contains placeholder names only.

### Editing viewing lists

Add or edit curated viewing orders in `app/src/main/java/com/cinemaverse/mcu/shared/data/viewing/ViewingLists.kt`. Keep MCU/movie metadata there when it is richer than JSON, and add/refresh poster filenames in the JSON/assets layer so artwork remains local, stable, and offline-friendly.
