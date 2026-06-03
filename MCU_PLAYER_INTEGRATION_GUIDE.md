# MCU Viewing Integration with Rhythm Player Infrastructure

This guide shows how to repurpose Rhythm's music player components for MCU movie/show viewing.

---

## 1. PLAYER CONTROLS → TRAILER PLAYER

### Current Rhythm Pattern:
- Play/Pause button controls music playback
- Skip forward/backward skip through songs
- Progress slider shows playback position
- MediaStore handles the actual playback

### MCU Adaptation:

**Reuse for:** Trailer playback controls in MCU detail pages

```kotlin
// Replace Song with MCUTitle for the data model
// The Player UI logic stays identical:
// - Play/Pause → Play/Stop trailer
// - Progress bar → Shows trailer progress (0-100%)
// - Time display → Shows "1m 45s of 2m 30s trailer"
// - Next/Previous → Could cycle through multiple trailers for same movie

// Key reusable components:
- PlayPauseButton → Controls trailer video playback
- ProgressSlider → Scrub through trailer timeline
- DurationText → Show "0:45 / 2:30" for trailer length
- VolumeButton → Control trailer audio
```

**Benefits:**
- Reuses all Rhythm's existing gesture handling (swipe to seek, tap for details)
- Animation system already built (play → pause icon transition)
- No new code needed for playback state management

---

## 2. QUEUE SYSTEM → CONTINUE WATCHING LIST

### Current Rhythm Pattern:
- QueueStateHolder manages song playback order
- Shuffle/unshuffle preserves original queue
- Queue source tracking ("Playlist: Favorites", "Album: Greatest Hits")
- Drag-to-reorder in queue bottom sheet
- Current index tracking for position

### MCU Adaptation:

**Reuse for:** "Continue Watching" viewing queue

```kotlin
// Map concepts:
// Song → MCUTitle
// Queue → Viewing Queue / Watchlist
// Queue Source → "Saga: Infinity Saga", "Phase: Phase Three", "Custom List"
// Current Song → Currently Watching
// Shuffle → Shuffle viewing order (watch by phase vs release date)

// QueueStateHolder becomes "ViewingQueueHolder":
class ViewingQueueHolder {
    // Current titles to watch (ordered)
    private val _currentViewingQueue = MutableStateFlow<List<MCUTitle>>(emptyList())
    
    // Titles already watched (for resume)
    private val _watchedTitles = MutableStateFlow<List<MCUTitle>>(emptyList())
    
    // Current viewing position
    private val _currentTitleIndex = MutableStateFlow(0)
    
    // Source (e.g., "Infinity Saga Release Order", "Phase Three Chronological")
    private val _queueSourceName = MutableStateFlow<String?>(null)
}

// UI stays the same:
// - Open queue sheet → Shows titles to watch next
// - Drag to reorder → Rearrange viewing order
// - Mark as watched → Removes from queue (like marking song as played)
// - Shuffle toggle → Randomize or restore original viewing order
```

**Benefits:**
- Entire drag-to-reorder UI already built
- Queue persistence logic can be adapted (save watching progress)
- Resume functionality mirrors "resume playback"
- Watched status syncs across all screens automatically

---

## 3. LYRICS VIEW → TRAILER/TEASER GALLERY

### Current Rhythm Pattern:
- FullScreenLyricsView displays synced song lyrics
- WordByWordLyricsView shows individual word timing
- SyncedLyricsView animates as song plays
- Tap to toggle UI elements
- Swipe gestures for navigation

### MCU Adaptation:

**Reuse for:** Movie trailer gallery, behind-the-scenes clips, or teaser carousel

```kotlin
// Option A: Replace lyrics with trailer clips
fun TrailerGalleryView(
    trailers: List<Trailer>,  // Instead of LyricsData
    currentTrailerIndex: Int,
    onTrailerSelected: (Int) -> Unit
) {
    // Use same scrolling + animation system
    // Each "lyric line" becomes a trailer thumbnail
    // Syncing ties to video timestamps
}

// Option B: Keep lyrics concept but show movie metadata synced with trailer
// "0:00 - Character introduction"
// "0:15 - Action sequence starts"
// "1:30 - Plot twist teaser"
// This creates a "guided viewing" experience

// Option C: Display cast/crew bios that sync with trailer scenes
// As trailer plays, show corresponding actor/director info
```

**Benefits:**
- Smooth scrolling animations already implemented
- Tap-to-hide UI logic works for video players
- Swipe gestures can skip to different trailer segments
- Syncing system can track video timeline instead of music timeline
- Word-by-word view becomes scene-by-scene breakdown

---

## 4. MINI PLAYER → MOVIE CARD PLAYER

### Current Rhythm Pattern:
- Mini player shows thumbnail, title, play/pause on home screen
- Floating action button for quick control
- Swipe up to expand to full player
- Shows current song + next in queue

### MCU Adaptation:

**Reuse for:** Movie cards with inline trailer preview

```kotlin
// Transform mini player into movie card:
// [Movie Poster] [Title] [Play Trailer] [+Queue]
//                                    ↓ (tap play)
// [Full Trailer Player with controls]

// Benefits:
// - Smooth animation from card → full screen (already implemented)
// - Compact display of current viewing item
// - Quick access to trailer without leaving list
// - "Next up" shows next movie in viewing queue
```

---

## 5. PLAYBACK SETTINGS → VIEWING PREFERENCES

### Current Rhythm Reuse:

**Playback Speed Dialog** → Playback Speed for trailers/clips
- 0.75x, 1.0x, 1.25x, 1.5x, 2.0x speeds
- Perfect for rewatching trailers or catching details

**Sleep Timer** → Auto-stop watching after duration
- "Stop after 1 episode"
- "Stop after 2 hours" (useful for binge sessions)

**Pitch Control** → Could become language selection
- Multiple audio tracks (same concept)

---

## 6. BOTTOM SHEETS → MCU VIEWING CONTROLS

### Current Rhythm Pattern:
- QueueBottomSheet shows upcoming songs
- PlaybackBottomSheet shows speed/pitch options
- SongInfoBottomSheet shows metadata
- AddToPlaylistBottomSheet for organizing

### MCU Adaptation:

**Queue Bottom Sheet → "Continue Watching" sheet**
- Shows next titles in viewing order
- Drag to reorder
- Add/remove from queue
- Check watched status

**Playback Bottom Sheet → Viewing Options**
- Video quality
- Playback speed
- Audio language
- Subtitles

**Info Bottom Sheet → Movie Details**
- Full plot summary
- Cast & crew
- Runtime & rating
- Links to related movies/shows

---

## 7. PLAYER THEMES → VIEWING THEMES

### Current Rhythm Pattern:
- ExpressivePlayerScreen (rounded, minimal)
- MaterialPlayerScreen (clean, material design)
- Customizable colors and layouts

### MCU Adaptation:

**Expressive Viewing Screen**
- Large poster artwork
- Minimal controls overlay
- Focus on beautiful imagery

**Material Viewing Screen**
- Clean, structured layout
- Organized sections
- Easy access to metadata

---

## 8. HAPTIC FEEDBACK → VIEWING INTERACTIONS

### Current Rhythm Reuse:
- Tap play/pause → Haptic pulse
- Skip to next → Double tap haptic
- Shuffle toggle → Haptic feedback
- Mark as watched → Haptic confirmation

**Benefits:** Same tactile feedback enhances viewing experience

---

## Implementation Priority

### Phase 1 (Quick Wins - Reuse immediately):
1. **Trailer Player Controls** - Use player UI for trailer playback
2. **Continue Watching Queue** - Adapt QueueStateHolder
3. **Mini Player Cards** - Movie poster + quick play

### Phase 2 (Enhanced Experience):
4. **Trailer Gallery View** - Adapt FullScreenLyricsView
5. **Viewing Preferences** - Reuse settings dialogs
6. **Player Themes** - Adapt screen layouts

### Phase 3 (Polish):
7. **Haptic Feedback** - Add tactile confirmation
8. **Advanced Features** - Watched status sync, resume functionality

---

## Code Example: Minimal Adaptation

```kotlin
// Instead of creating new player code, adapt existing:

// Was: PlaySong(song: Song) → Plays music
// Now: PlayTrailer(trailer: Trailer) → Plays trailer (same controls!)

// Was: QueueStateHolder<Song>
// Now: QueueStateHolder<MCUTitle> (generic already, just change type!)

// Was: PlayerScreen(song: Song, onSkipNext: () -> Unit)
// Now: ViewerScreen(title: MCUTitle, onMarkWatched: () -> Unit)
//      (UI logic stays 95% the same)
```

---

## Summary

| Rhythm Component | MCU Use Case | Effort | Reuse % |
|---|---|---|---|
| Player Controls (Play/Pause/Skip) | Trailer player buttons | Minimal | 95% |
| QueueStateHolder | Viewing queue management | Low | 90% |
| QueueBottomSheet | Continue Watching list | Low | 85% |
| FullScreenLyricsView | Trailer gallery/scenes | Medium | 75% |
| Mini Player | Movie card inline player | Low | 80% |
| Playback Settings | Viewing preferences | Low | 70% |
| PlayerThemes | Viewing screen layouts | Low | 60% |
| Haptic Feedback | Interaction confirmation | Minimal | 100% |

**Total Estimated Reuse: 80-90% of player infrastructure**

This approach means you build MCU viewing with minimal new code while keeping Rhythm's proven player architecture!
