# Rhythm UI Components - Reusable for MCU Integration

## 1. HorizontalUncontainedCarousel (BEST FOR: Featured Movies/Upcoming Releases)

### What it is:
- Material 3 Carousel with auto-scroll capability (4.5s per item)
- Smooth animations between items
- Full-width, immersive hero sections
- Built-in item snapping and state management

### How Rhythm uses it:
- Discovery Carousel in HomeScreen (albums)
- Background image with gradient overlay
- Title, artist, and play button overlay
- Auto-scrolls through albums with 900ms animation

### MCU Use Cases:
1. **Featured Movie Carousel** - Showcase upcoming MCU releases or recently watched movies
2. **"Next to Watch" Section** - Featured viewing picks that auto-rotate
3. **Saga Hero Section** - Each saga gets a featured carousel on its library page
4. **Trending/Popular Titles** - Current trending MCU movies in a rotating hero

---

## 2. ModernSectionTitle (BEST FOR: Section Headers with Actions)

### What it is:
- Reusable header component with title, subtitle, and action buttons
- Supports "Play All", "Shuffle Play", and "View All" actions
- Clean typography with proper hierarchy

### MCU Use Cases:
1. **"Recently Watched"** - With "Resume" and "Continue Watching" buttons
2. **"Your Watchlist"** - With "Sort" and "Filter" buttons
3. **"By Saga"** - Shows saga sections (Infinity Saga, Multiverse Saga, etc.)
4. **"By Phase"** - Groups titles by MCU phase with action buttons

---

## 3. LazyRow + Grid Layouts (BEST FOR: Title Lists/Collections)

### What it is:
- Horizontal scrolling rows for phones, grid layouts for tablets
- Adaptive layouts based on screen size (compact vs tablet)
- Efficient lazy loading

### MCU Use Cases:
1. **Viewing Order Row** - Horizontal scroll of upcoming movies to watch
2. **Watched Movies Grid** - Grid showing all watched titles (compact as row, tablet as grid)
3. **Series Collections** - Show all series horizontally (Avengers, Guardians, Doctor Strange, etc.)
4. **Phase Breakdown** - Each phase shows movies in responsive layout

---

## 4. Card Components for Items (BEST FOR: Thumbnail Display)

### MCU Use Cases:
1. **Movie Poster Card** - Shows poster image with title, year, runtime overlay
2. **Series Card** - Series artwork with series name and "# movies" count
3. **Watchlist Item** - Movie with watched checkbox and watched date
4. **Saga Card** - Saga artwork with phase info overlay

---

## 5. Column Sections with Spacers (BEST FOR: Structured Layouts)

### MCU Use Cases:
1. **Movie Detail Page** - Header, poster, title, metadata, actions, synopsis, cast
2. **Saga Overview** - Hero, saga info, stats, movie list, recommended next
3. **Watchlist** - Header, sort/filter controls, list of movies, watched progress

---

## 6. State Management with StateFlow (BEST FOR: Dynamic UI Updates)

### MCU Use Cases:
1. Track watched status across screens
2. Auto-refresh viewed list when movie is marked watched
3. Update carousel when new movies added to watchlist
4. Responsive grid columns based on device rotation

---

## Suggested MCU Feature Implementation:

**1. MCU Featured Carousel** (Use HorizontalUncontainedCarousel)
- Auto-rotating featured movie with background poster
- Title, year, "Watch" and "Details" buttons
- 4.5s auto-scroll interval

**2. Recently Watched Section** (Use ModernSectionTitle + LazyRow)
- Header with "Continue Watching" button
- Horizontal scroll of recent MCU titles
- Each card shows poster + title + date watched

**3. Your Watchlist** (Use LazyVerticalGrid/LazyRow)
- Grid/row of watchlist items
- Toggle watched status
- Reorder capability

**4. By Saga Collections** (Use Column + LazyRow sections)
- Infinity Saga section with featured carousel
- Multiverse Saga section with carousel
- Each saga has its own hero + horizontal movies

**5. Movie Detail Page** (Use Column sections)
- Hero carousel or large poster
- Title, metadata, watched status
- Synopsis, cast, recommendations
- Watch trailer, mark watched buttons

**6. Phase-Based Browser** (Use ModernSectionTitle + Grid)
- Phase One, Phase Two, etc. sections
- Each phase shows movie grid
- Filter and sort by release date/viewing order
