# CinemaVerse MCU - Quick Start for Developers

## What Changed

CinemaVerse now features MCU viewing order with bundled local data, Rhythm UI polish, and viewing-first mode.

### Key Components Added
- **McuAssetDataSource** → Loads JSON from assets, resolves poster paths
- **McuViewingRepository** → Merges JSON data with curated metadata
- **MarvelSpectrumHeader** → Polished Material 3 header component
- **Performance fixes** → Remember blocks, stable lazy list keys, image caching

### Changes Required to Compile
1. ✓ Imports already added (Icon, IconButton, ImageRequest)
2. ✓ Default mode changed to VIEWING
3. ✓ Music flags disabled in build.gradle
4. ✓ All asset files copied to app/src/main/assets/mcu_posters/

## Building and Running

```bash
# Build without API keys (offline mode)
./gradlew :app:assembleGithubDebug

# Build with API enrichment (optional)
export OMDB_API_KEY="your-key"
export TMDB_API_KEY="your-key"
export TMDB_READ_ACCESS_TOKEN="your-token"
./gradlew :app:assembleGithubDebug

# Install
adb install app/build/outputs/apk/github/debug/app-github-debug.apk
```

## Offline vs. With APIs

### Offline (No API keys)
- ✓ Full MCU viewing experience
- ✓ All 50+ posters load from assets
- ✓ Title, saga, phase, viewing order
- ✓ Director, runtime, genres (from curated data)
- ✗ No ratings, full cast, plot from external APIs

### With OMDB_API_KEY + TMDB_API_KEY
- ✓ Everything above, PLUS
- ✓ Ratings from OMDB/TMDB
- ✓ Full cast lists
- ✓ Extended plot summaries
- ✓ Complete runtime and language info

## File Structure for Development

```
app/src/main/java/com/cinemaverse/mcu/
└── shared/data/viewing/
    ├── McuAssetDataSource.kt       ← NEW: Asset loader
    ├── McuViewingRepository.kt     ← NEW: Data merge logic
    ├── ViewingLists.kt             ← EXISTING: Curated data (not replaced)
    ├── ViewingModels.kt            ← EXISTING: Data classes
    └── ViewingArtworkUtils.kt      ← EXISTING: Poster resolution

app/src/main/assets/
├── mcu_data/
│   ├── mcu_titles.json             ← MCU metadata (34+ entries)
│   └── posters.json                ← Poster filename mapping
└── mcu_posters/
    ├── 001-captain-america-the-first-avenger.jpg
    └── ... (52 total)
```

## Adding a New MCU Title

1. **Edit** `app/src/main/assets/mcu_data/mcu_titles.json`
   ```json
   {
     "id": "50",
     "title": "New MCU Title",
     "type": "movie",
     "series": "Series Name",
     "saga": "Multiverse Saga",
     "viewingOrder": 50,
     "releaseDate": 1719792000000,
     "posterPath": "050-new-mcu-title.jpg"
   }
   ```

2. **Add poster image** `app/src/main/assets/mcu_posters/050-new-mcu-title.jpg`

3. **Update ViewingLists.kt** (optional)
   - Add to existing collections if desired
   - Append to `releaseItems` with curated metadata

4. **Rebuild** `./gradlew :app:assembleGithubDebug`

## Re-enabling Music Mode

In `app/build.gradle.kts`, change:
```gradle
buildConfigField("boolean", "ENABLE_YOUTUBE_MUSIC", "false")   // → "true"
buildConfigField("boolean", "ENABLE_APPLE_MUSIC", "false")     // → "true"
// ... etc
```

Then rebuild. Users can toggle between modes in Settings > Experience Mode.

## Performance Notes

### Why Scroll is Fast
- Images cached in memory + disk (Coil)
- List items have stable keys (no unnecessary recomposition)
- Expensive list filters memoized with `remember`
- Content type hints for efficient diffing

### Monitoring Performance
```kotlin
// Debug: Enable recomposition logging
// In any composable:
println("[v0] ViewingHomeScreen recomposed")

// Check cache hits:
// Logcat: "coil/ImageRequest: Cache hit for posterPath"
```

## Testing Checklist

- [ ] App launches in viewing mode (not music mode)
- [ ] Marvel Spectrum header visible on home screen
- [ ] Posters load without network
- [ ] Search works for titles, phases, sagas
- [ ] Library shows all 5 phases + 3 saga collections
- [ ] Scroll is smooth (no jank)
- [ ] Settings option to switch to music mode (if re-enabled)

## Troubleshooting

**Q: Posters not loading**
- Check: `app/src/main/assets/mcu_posters/` directory exists with 52 files
- Check: Poster filenames match JSON posterPath values
- Check: Coil library version in `libs.versions.toml`

**Q: JSON not loading**
- Check: `app/src/main/assets/mcu_data/mcu_titles.json` is valid JSON
- Check: `McuAssetDataSource.loadJsonString()` logs show asset opened
- Try: `adb logcat | grep McuAssetDataSource`

**Q: App crashes on startup**
- Check: `CinemaVerseApplication.kt` still exists and is referenced in AndroidManifest
- Check: All imports in ViewingExperienceScreens.kt are correct
- Try: Clean build `./gradlew clean :app:assembleGithubDebug`

## Related Documentation

- See `README.md` for MCU metadata setup details
- See `CINEMAVERSE_MCU_IMPLEMENTATION_SUMMARY.md` for complete implementation details
- See `MCU_PLAYER_INTEGRATION_GUIDE.md` for trailer player adaptation (future)

---

**Last Updated:** June 3, 2026
**Status:** Ready for compilation and testing
