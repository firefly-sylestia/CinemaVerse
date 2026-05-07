package chromahub.rhythm.app.features.streaming.presentation.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import chromahub.rhythm.app.core.domain.model.SourceType
import chromahub.rhythm.app.core.domain.model.StreamingConfig
import chromahub.rhythm.app.core.domain.model.StreamingQuality
import chromahub.rhythm.app.core.utils.NetworkUtils
import chromahub.rhythm.app.features.streaming.data.repository.StreamingMusicRepositoryImpl
import chromahub.rhythm.app.features.streaming.data.repository.StreamingServiceSession
import chromahub.rhythm.app.features.streaming.data.repository.StreamingServiceSessionRepository
import chromahub.rhythm.app.features.streaming.di.StreamingMusicModule
import chromahub.rhythm.app.features.streaming.domain.model.BrowseCategory
import chromahub.rhythm.app.features.streaming.domain.model.StreamingAlbum
import chromahub.rhythm.app.features.streaming.domain.model.StreamingArtist
import chromahub.rhythm.app.features.streaming.domain.model.StreamingPlaylist
import chromahub.rhythm.app.features.streaming.domain.model.StreamingServiceId
import chromahub.rhythm.app.features.streaming.domain.model.StreamingServiceRules
import chromahub.rhythm.app.features.streaming.domain.model.StreamingSong
import chromahub.rhythm.app.shared.data.model.AppSettings
import chromahub.rhythm.app.util.ArtistSeparator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

/**
 * ViewModel for managing streaming music playback and library.
 * Handles authentication, browsing, and playback for streaming services.
 */
class StreamingMusicViewModel(application: Application) : AndroidViewModel(application) {
    private val appSettings = AppSettings.getInstance(application)
    private val context: Context = application
    private val serviceSessionRepository = StreamingServiceSessionRepository(application)
    private val repository = StreamingMusicModule.provideStreamingMusicRepository(application)
    private val providerRepository = repository as? StreamingMusicRepositoryImpl
    private var playbackHandler: ((List<StreamingSong>, Int) -> Unit)? = null
    private var seekProgressHandler: ((Float) -> Unit)? = null
    private var seekPositionHandler: ((Long) -> Unit)? = null

    
    // Authentication state
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    val serviceSessions: StateFlow<Map<String, StreamingServiceSession>> = serviceSessionRepository.sessions
    
    private val _currentService = MutableStateFlow(SourceType.SUBSONIC)
    val currentService: StateFlow<SourceType> = _currentService.asStateFlow()
    
    private val _streamingConfig = MutableStateFlow(StreamingConfig())
    val streamingConfig: StateFlow<StreamingConfig> = _streamingConfig.asStateFlow()
    
    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // Home content
    private val _recommendations = MutableStateFlow<List<StreamingSong>>(emptyList())
    val recommendations: StateFlow<List<StreamingSong>> = _recommendations.asStateFlow()
    
    private val _newReleases = MutableStateFlow<List<StreamingAlbum>>(emptyList())
    val newReleases: StateFlow<List<StreamingAlbum>> = _newReleases.asStateFlow()
    
    private val _featuredPlaylists = MutableStateFlow<List<StreamingPlaylist>>(emptyList())
    val featuredPlaylists: StateFlow<List<StreamingPlaylist>> = _featuredPlaylists.asStateFlow()
    
    // Browse content
    private val _browseCategories = MutableStateFlow<List<BrowseCategory>>(emptyList())
    val browseCategories: StateFlow<List<BrowseCategory>> = _browseCategories.asStateFlow()
    
    private val _topCharts = MutableStateFlow<List<StreamingSong>>(emptyList())
    val topCharts: StateFlow<List<StreamingSong>> = _topCharts.asStateFlow()
    
    // Library content
    private val _likedSongs = MutableStateFlow<List<StreamingSong>>(emptyList())
    val likedSongs: StateFlow<List<StreamingSong>> = _likedSongs.asStateFlow()
    
    private val _savedAlbums = MutableStateFlow<List<StreamingAlbum>>(emptyList())
    val savedAlbums: StateFlow<List<StreamingAlbum>> = _savedAlbums.asStateFlow()
    
    private val _followedArtists = MutableStateFlow<List<StreamingArtist>>(emptyList())
    val followedArtists: StateFlow<List<StreamingArtist>> = _followedArtists.asStateFlow()
    
    private val _savedPlaylists = MutableStateFlow<List<StreamingPlaylist>>(emptyList())
    val savedPlaylists: StateFlow<List<StreamingPlaylist>> = _savedPlaylists.asStateFlow()
    
    private val _downloadedSongs = MutableStateFlow<List<StreamingSong>>(emptyList())
    val downloadedSongs: StateFlow<List<StreamingSong>> = _downloadedSongs.asStateFlow()
    
    // Current playback state
    private val _currentSong = MutableStateFlow<StreamingSong?>(null)
    val currentSong: StateFlow<StreamingSong?> = _currentSong.asStateFlow()
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()
    
    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()
    
    // Queue
    private val _queue = MutableStateFlow<List<StreamingSong>>(emptyList())
    val queue: StateFlow<List<StreamingSong>> = _queue.asStateFlow()
    
    // Search state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _searchResults = MutableStateFlow<StreamingSearchResults>(StreamingSearchResults())
    val searchResults: StateFlow<StreamingSearchResults> = _searchResults.asStateFlow()
    
    init {
        observeSelectedService()
    }

    private fun observeSelectedService() {
        viewModelScope.launch {
            appSettings.streamingService.collect { serviceId ->
                val normalizedServiceId = normalizeServiceId(serviceId)
                if (normalizedServiceId != serviceId) {
                    appSettings.setStreamingService(normalizedServiceId)
                    return@collect
                }

                _currentService.value = sourceTypeFromServiceId(normalizedServiceId)

                val connected = checkAndSyncAuthentication(normalizedServiceId)
                if (connected) {
                    loadHomeContent()
                } else {
                    val fallbackConnectedServiceId = serviceSessionRepository.sessions.value.values
                        .firstOrNull {
                            it.isConnected && normalizeServiceId(it.serviceId) != normalizedServiceId
                        }
                        ?.serviceId
                        ?.let(::normalizeServiceId)

                    if (!fallbackConnectedServiceId.isNullOrBlank()) {
                        appSettings.setStreamingService(fallbackConnectedServiceId)
                        return@collect
                    }

                    clearContent()
                }
            }
        }
    }
    
    /**
     * Check if user is authenticated with the current service.
     */
    private fun checkAuthenticationStatus() {
        viewModelScope.launch {
            checkAndSyncAuthentication()
        }
    }
    
    /**
     * Select a streaming service.
     */
    fun selectService(service: SourceType) {
        viewModelScope.launch {
            val serviceId = serviceIdFromSourceType(service)
            _currentService.value = service
            if (appSettings.streamingService.value != serviceId) {
                appSettings.setStreamingService(serviceId)
            }
            checkAndSyncAuthentication(serviceId)
        }
    }
    
    /**
     * Authenticate with the current streaming service.
     */
    fun authenticate() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                checkAndSyncAuthentication()
                if (!_isAuthenticated.value) {
                    _error.value = "Open service setup and connect an account first"
                }
            } catch (e: Exception) {
                _error.value = "Authentication failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Log out from the current streaming service.
     */
    fun logout() {
        val selectedService = appSettings.streamingService.value
        disconnectService(selectedService)
    }

    fun connectService(serviceId: String, serverUrl: String, username: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val normalizedServiceId = normalizeServiceId(serviceId)
                validateCredentials(normalizedServiceId, serverUrl, username, password)

                val connection = providerRepository?.connect(
                    serviceId = normalizedServiceId,
                    serverUrl = serverUrl,
                    username = username,
                    password = password
                ) ?: throw IllegalStateException("Streaming repository is not initialized")

                serviceSessionRepository.connect(
                    serviceId = normalizedServiceId,
                    serverUrl = connection.serverUrl.trim(),
                    username = connection.displayName.trim()
                )
                if (appSettings.streamingService.value != normalizedServiceId) {
                    appSettings.setStreamingService(normalizedServiceId)
                }
                checkAndSyncAuthentication(normalizedServiceId)
                loadHomeContent()
            } catch (e: Exception) {
                _error.value = "Connection failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun disconnectService(serviceId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val normalizedServiceId = normalizeServiceId(serviceId)
                providerRepository?.disconnect(normalizedServiceId)

                serviceSessionRepository.disconnect(normalizedServiceId)
                if (appSettings.streamingService.value == normalizedServiceId) {
                    checkAndSyncAuthentication(normalizedServiceId)
                    clearContent()
                }
            } catch (e: Exception) {
                _error.value = "Disconnect failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getServiceSession(serviceId: String): StreamingServiceSession {
        return serviceSessionRepository.getSession(serviceId)
    }

    fun setPlaybackHandler(handler: (List<StreamingSong>, Int) -> Unit) {
        playbackHandler = handler
    }

    fun setSeekHandlers(
        progressHandler: (Float) -> Unit,
        positionHandler: (Long) -> Unit
    ) {
        seekProgressHandler = progressHandler
        seekPositionHandler = positionHandler
    }
    
    /**
     * Report an error to the user.
     */
    fun reportError(message: String) {
        _error.value = message
    }
    
    /**
     * Report a warning (stored in error state for display).
     */
    fun reportWarning(message: String) {
        _error.value = message
    }
    
    /**
     * Refresh the current service session connection status.
     */
    fun refreshCurrentSession() {
        viewModelScope.launch {
            val currentServiceId = appSettings.streamingService.value
            checkAndSyncAuthentication(currentServiceId)
        }
    }
    
    /**
     * Load home screen content.
     */
    fun loadHomeContent() {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                if (!checkAndSyncAuthentication()) {
                    clearContent()
                    return@launch
                }
                
                // Check network constraints
                if (!NetworkUtils.canStream(context, appSettings.allowCellularStreaming.value)) {
                    clearContent()
                    _error.value = "Streaming not allowed on current network"
                    return@launch
                }
                
                if (appSettings.offlineMode.value) {
                    clearContent()
                    _error.value = "Content loading unavailable in offline mode"
                    return@launch
                }

                val catalogSongs = providerRepository?.syncCatalog(limit = 5_000)
                    ?.filter { it.isPlayable }
                    .orEmpty()
                val seedSongs = if (catalogSongs.isNotEmpty()) {
                    catalogSongs
                } else {
                    seedSongsFromService(limit = 240)
                }

                var recommendations = repository.getRecommendations(limit = 24)
                if (recommendations.isEmpty()) {
                    recommendations = seedSongs.take(24)
                }

                var newReleases = repository.getNewReleases(limit = 24)
                if (newReleases.isEmpty()) {
                    newReleases = deriveAlbumsFromSongs(seedSongs, limit = 24)
                }

                // Don't derive playlists for streaming - only use actual provider playlists
                val featuredPlaylists = repository.getFeaturedPlaylists(limit = 24)

                _recommendations.value = recommendations
                _newReleases.value = newReleases
                _featuredPlaylists.value = featuredPlaylists
            } catch (e: Exception) {
                // Check if error is due to stale connection (timeout, connection error)
                val isConnectionError = e.message?.contains("timeout", ignoreCase = true) == true ||
                    e.message?.contains("connection", ignoreCase = true) == true ||
                    e.message?.contains("socket", ignoreCase = true) == true ||
                    e.cause is java.net.SocketException ||
                    e.cause is java.net.ConnectException ||
                    e is java.io.IOException
                
                if (isConnectionError) {
                    // Attempt automatic reconnection by refreshing authentication
                    _error.value = "Connection lost. Attempting to reconnect..."
                    delay(1000) // Wait before attempting reconnection
                    val serviceId = appSettings.streamingService.value
                    if (checkAndSyncAuthentication(serviceId)) {
                        // Retry loading content after reconnection
                        _error.value = null
                        loadHomeContent()
                    } else {
                        _error.value = "Failed to reconnect to streaming service. Please try reconnecting manually."
                        clearContent()
                    }
                } else {
                    _error.value = "Failed to load content: ${e.message}"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Refresh home screen content.
     */
    fun refreshHome() {
        loadHomeContent()
    }
    
    /**
     * Load browse categories.
     */
    fun loadBrowseCategories() {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                if (!checkAndSyncAuthentication()) {
                    _browseCategories.value = emptyList()
                    return@launch
                }

                _browseCategories.value = repository.getBrowseCategories()
            } catch (e: Exception) {
                _error.value = "Failed to load categories: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Load top charts.
     */
    fun loadTopCharts() {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                if (!checkAndSyncAuthentication()) {
                    _topCharts.value = emptyList()
                    return@launch
                }

                _topCharts.value = repository.getTopCharts()
            } catch (e: Exception) {
                _error.value = "Failed to load charts: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Load user's library content.
     */
    fun loadLibrary() {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                if (!checkAndSyncAuthentication()) {
                    _likedSongs.value = emptyList()
                    _savedAlbums.value = emptyList()
                    _followedArtists.value = emptyList()
                    _savedPlaylists.value = emptyList()
                    _downloadedSongs.value = emptyList()
                    return@launch
                }

                val likedSongs = repository.getLikedSongs().first()
                val savedAlbums = repository.getSavedAlbums().first()
                val followedArtists = repository.getFollowedArtists().first()
                val downloadedSongs = repository.getDownloadedSongs().first()
                val savedPlaylists = repository.getPlaylists().first()
                    .filterIsInstance<StreamingPlaylist>()

                val hasExplicitLibraryData = likedSongs.isNotEmpty() ||
                    savedAlbums.isNotEmpty() ||
                    followedArtists.isNotEmpty() ||
                    downloadedSongs.isNotEmpty() ||
                    savedPlaylists.isNotEmpty()

                val seedSongs = if (hasExplicitLibraryData) {
                    emptyList()
                } else {
                    providerRepository?.syncCatalog(limit = 5_000)
                        ?.filter { it.isPlayable }
                        ?.takeIf { it.isNotEmpty() }
                        ?: seedSongsFromService(limit = 240)
                }

                val mergedSongs = (likedSongs + downloadedSongs + seedSongs)
                    .distinctBy { it.id }

                val resolvedAlbums = if (savedAlbums.isNotEmpty()) {
                    savedAlbums
                } else {
                    deriveAlbumsFromSongs(mergedSongs, limit = 48)
                }

                val resolvedArtists = if (followedArtists.isNotEmpty()) {
                    followedArtists
                } else {
                    deriveArtistsFromSongs(mergedSongs, limit = 48)
                }

                val resolvedPlaylists = when {
                    savedPlaylists.isNotEmpty() -> savedPlaylists
                    else -> {
                        // Try featured playlists from the provider
                        val featuredPlaylists = repository.getFeaturedPlaylists(limit = 24)
                        if (featuredPlaylists.isNotEmpty()) {
                            featuredPlaylists
                        } else {
                            // Don't derive playlists in streaming context - show empty list instead
                            // Only show actual streaming provider playlists
                            emptyList()
                        }
                    }
                }

                _likedSongs.value = if (likedSongs.isNotEmpty()) likedSongs else mergedSongs
                _savedAlbums.value = resolvedAlbums
                
                // Enrich artists with Deezer images if artwork is missing
                val enrichedArtists = providerRepository?.enrichArtistsWithDeezerImages(resolvedArtists)
                    ?: resolvedArtists
                _followedArtists.value = enrichedArtists
                
                _savedPlaylists.value = resolvedPlaylists
                _downloadedSongs.value = downloadedSongs

                if (_featuredPlaylists.value.isEmpty()) {
                    _featuredPlaylists.value = resolvedPlaylists
                }
            } catch (e: Exception) {
                _error.value = "Failed to load library: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Search across the streaming service.
     */
    fun search(query: String) {
        _searchQuery.value = query

        if (query.isBlank()) {
            _searchResults.value = StreamingSearchResults()
            return
        }

        viewModelScope.launch {
            _isLoading.value = true

            try {
                if (!checkAndSyncAuthentication()) {
                    _searchResults.value = StreamingSearchResults()
                    _error.value = "Connect to a streaming service first"
                    return@launch
                }
                
                // Check network and offline constraints
                if (!NetworkUtils.canStream(context, appSettings.allowCellularStreaming.value)) {
                    _searchResults.value = StreamingSearchResults()
                    _error.value = "Streaming not allowed on current network"
                    return@launch
                }
                
                if (appSettings.offlineMode.value) {
                    _searchResults.value = StreamingSearchResults()
                    _error.value = "Search not available in offline mode"
                    return@launch
                }

                val songs = repository.searchSongs(query).filterIsInstance<StreamingSong>()
                val artistsFromRepository = repository.searchArtists(query).filterIsInstance<StreamingArtist>()
                val albumsFromRepository = repository.searchAlbums(query).filterIsInstance<StreamingAlbum>()
                val playlists = repository.searchPlaylists(query).filterIsInstance<StreamingPlaylist>()

                val derivedArtists = songs
                    .mapNotNull { searchSong ->
                        if (searchSong.artist.isBlank()) {
                            null
                        } else {
                            StreamingArtist(
                                id = "${searchSong.sourceType.name}:artist:${searchSong.artist.lowercase()}",
                                name = searchSong.artist,
                                artworkUri = searchSong.artworkUri,
                                songCount = 0,
                                albumCount = 0,
                                sourceType = searchSong.sourceType
                            )
                        }
                    }
                    .distinctBy { it.id }

                val derivedAlbums = songs
                    .mapNotNull { searchSong ->
                        if (searchSong.album.isBlank()) {
                            null
                        } else {
                            StreamingAlbum(
                                id = "${searchSong.sourceType.name}:album:${searchSong.album.lowercase()}",
                                title = searchSong.album,
                                artist = searchSong.artist,
                                artworkUri = searchSong.artworkUri,
                                songCount = 0,
                                year = null,
                                sourceType = searchSong.sourceType
                            )
                        }
                    }
                    .distinctBy { it.id }

                _searchResults.value = StreamingSearchResults(
                    songs = songs,
                    albums = if (albumsFromRepository.isNotEmpty()) albumsFromRepository else derivedAlbums,
                    artists = if (artistsFromRepository.isNotEmpty()) artistsFromRepository else derivedArtists,
                    playlists = playlists
                )
            } catch (e: Exception) {
                _error.value = "Search failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Play a streaming song.
     */
    fun playSong(song: StreamingSong) {
        val queueSource = when {
            _searchResults.value.songs.any { it.id == song.id } -> _searchResults.value.songs
            _recommendations.value.any { it.id == song.id } -> _recommendations.value
            _queue.value.any { it.id == song.id } -> _queue.value
            else -> listOf(song)
        }

        val selectedIndex = queueSource.indexOfFirst { it.id == song.id }
            .takeIf { it >= 0 }
            ?: 0

        playQueue(queueSource, startIndex = selectedIndex, shuffle = false)
    }

    /**
     * Play the current recommendation list.
     */
    fun playRecommendations(shuffle: Boolean = false) {
        playQueue(_recommendations.value, startIndex = 0, shuffle = shuffle)
    }

    /**
     * Play a specific queue and start index.
     */
    fun playQueue(queue: List<StreamingSong>, startIndex: Int = 0, shuffle: Boolean = false) {
        val playableQueue = queue.filter { it.isPlayable }
        if (playableQueue.isEmpty()) {
            _error.value = "No playable tracks available"
            return
        }

        viewModelScope.launch {
                if (!checkAndSyncAuthentication()) {
                    _error.value = "Connect to a streaming service first"
                    return@launch
                }

            val safeStartIndex = startIndex.coerceIn(0, playableQueue.lastIndex)
            val queueToPlay = if (shuffle && playableQueue.size > 1) {
                val startSong = playableQueue[safeStartIndex]
                val tail = playableQueue.toMutableList().apply {
                    removeAt(safeStartIndex)
                    shuffle()
                }
                listOf(startSong) + tail
            } else {
                playableQueue
            }

            val selectedIndex = if (shuffle && queueToPlay.size > 1) {
                0
            } else {
                safeStartIndex
            }

            val selectedSong = queueToPlay[selectedIndex]
            val resolvedUrl = if (selectedSong.streamingUrl.isNullOrBlank()) {
                repository.getStreamingUrl(selectedSong.id)
            } else {
                selectedSong.streamingUrl
            }

            if (resolvedUrl.isNullOrBlank()) {
                _error.value = when {
                    appSettings.offlineMode.value -> "Offline mode: Song not in cache"
                    !NetworkUtils.canStream(context, appSettings.allowCellularStreaming.value) -> "Streaming not allowed on current network"
                    else -> "Unable to resolve stream URL for this song"
                }
                return@launch
            }

            val resolvedSong = selectedSong.copy(streamingUrl = resolvedUrl)
            val queueWithResolvedSong = queueToPlay.map {
                if (it.id == resolvedSong.id) resolvedSong else it
            }

            _queue.value = queueWithResolvedSong
            _currentSong.value = queueWithResolvedSong[selectedIndex]
            _isPlaying.value = true

            playbackHandler?.invoke(queueWithResolvedSong, selectedIndex)
            }
    }

    /**
     * Play an album.
     */
    fun playAlbum(album: StreamingAlbum) {
        viewModelScope.launch {
            val tracks = album.getTracks()
            if (tracks.isNotEmpty()) {
                playQueue(tracks, startIndex = 0, shuffle = false)
            }
        }
    }

    /**
     * Play a playlist.
     */
    fun playPlaylist(playlist: StreamingPlaylist) {
        viewModelScope.launch {
            val tracks = playlist.getTracks()
            if (tracks.isNotEmpty()) {
                playQueue(tracks, startIndex = 0, shuffle = false)
            }
        }
    }

    /**
     * Resolve songs for an album with repository-first lookup and local fallback.
     */
    suspend fun getAlbumSongs(album: StreamingAlbum): List<StreamingSong> {
        val embeddedTracks = album.getTracks()
            .filter { it.isPlayable }
            .distinctBy { it.id }
        if (embeddedTracks.isNotEmpty()) {
            return embeddedTracks
        }

        val repositoryTracks = repository.getSongsForAlbum(album.id)
            .filterIsInstance<StreamingSong>()
            .filter { it.isPlayable }
            .distinctBy { it.id }
        if (repositoryTracks.isNotEmpty()) {
            return repositoryTracks
        }

        return (
            _likedSongs.value +
                _downloadedSongs.value +
                _recommendations.value +
                _searchResults.value.songs +
                _queue.value
            )
            .asSequence()
            .filter {
                it.album.equals(album.title, ignoreCase = true) &&
                    it.artist.equals(album.artist, ignoreCase = true)
            }
            .filter { it.isPlayable }
            .distinctBy { it.id }
            .toList()
    }

    /**
     * Resolve top songs for an artist with repository-first lookup and local fallback.
     */
    suspend fun getArtistTopSongs(
        artistId: String,
        artistNameHint: String? = null,
        limit: Int = 40
    ): List<StreamingSong> {
        val safeLimit = limit.coerceAtLeast(1)
        val cachedArtist = (_followedArtists.value + _searchResults.value.artists)
            .distinctBy { it.id }
            .firstOrNull { it.id == artistId }

        val embeddedTracks = cachedArtist
            ?.getTopTracks()
            .orEmpty()
            .filter { it.isPlayable }
            .distinctBy { it.id }
        if (embeddedTracks.isNotEmpty()) {
            return embeddedTracks.take(safeLimit)
        }

        val repositoryTracks = repository.getArtistTopTracks(artistId, safeLimit)
            .filter { it.isPlayable }
            .distinctBy { it.id }
        if (repositoryTracks.isNotEmpty()) {
            return repositoryTracks
        }

        val normalizedHint = artistNameHint?.trim().orEmpty()
        return (
            _likedSongs.value +
                _downloadedSongs.value +
                _recommendations.value +
                _searchResults.value.songs +
                _queue.value
            )
            .asSequence()
            .filter {
                if (normalizedHint.isNotBlank()) {
                    it.artist.equals(normalizedHint, ignoreCase = true)
                } else {
                    artistIdMatchesSongArtist(artistId = artistId, songArtist = it.artist)
                }
            }
            .filter { it.isPlayable }
            .distinctBy { it.id }
            .take(safeLimit)
            .toList()
    }

    /**
     * Resolve albums for an artist with repository-first lookup and local fallback.
     */
    suspend fun getArtistAlbums(
        artistId: String,
        artistNameHint: String? = null
    ): List<StreamingAlbum> {
        val cachedArtist = (_followedArtists.value + _searchResults.value.artists)
            .distinctBy { it.id }
            .firstOrNull { it.id == artistId }

        val embeddedAlbums = cachedArtist
            ?.getAlbumsList()
            .orEmpty()
            .distinctBy { it.id }
        if (embeddedAlbums.isNotEmpty()) {
            return embeddedAlbums
        }

        val repositoryAlbums = repository.getArtistAlbums(artistId)
            .distinctBy { it.id }
        if (repositoryAlbums.isNotEmpty()) {
            return repositoryAlbums
        }

        val fallbackName = artistNameHint?.trim().orEmpty()

        val derivedFromSongs = getArtistTopSongs(
            artistId = artistId,
            artistNameHint = fallbackName,
            limit = 120
        )
        if (derivedFromSongs.isNotEmpty()) {
            return deriveAlbumsFromSongs(derivedFromSongs, limit = 24)
        }

        return (_savedAlbums.value + _newReleases.value)
            .asSequence()
            .filter {
                if (fallbackName.isNotBlank()) {
                    it.artist.equals(fallbackName, ignoreCase = true)
                } else {
                    artistIdMatchesSongArtist(artistId = artistId, songArtist = it.artist)
                }
            }
            .distinctBy { it.id }
            .toList()
    }

    /**
     * Toggle play/pause.
     */
    fun togglePlayPause() {
        _isPlaying.value = !_isPlaying.value
        // TODO: Connect to MediaPlaybackService
    }

    /**
     * Skip to next song.
     */
    private var lastSkipTime = 0L
    private val SKIP_DEBOUNCE_MS = 400L

    fun skipToNext() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastSkipTime < SKIP_DEBOUNCE_MS) return
        lastSkipTime = currentTime

        val currentIndex = _queue.value.indexOf(_currentSong.value)
        if (currentIndex >= 0 && currentIndex < _queue.value.size - 1) {
            playSong(_queue.value[currentIndex + 1])
        }
    }

    /**
     * Skip to previous song.
     */
    fun skipToPrevious() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastSkipTime < SKIP_DEBOUNCE_MS) return
        lastSkipTime = currentTime

        val currentIndex = _queue.value.indexOf(_currentSong.value)
        if (currentIndex > 0) {
            playSong(_queue.value[currentIndex - 1])
        }
    }

    /**
     * Seek to a position.
     */
    fun seekTo(progress: Float) {
        _progress.value = progress
        seekProgressHandler?.invoke(progress)
    }

    fun seekTo(positionMs: Long) {
        seekPositionHandler?.invoke(positionMs)
    }

    /**
     * Like/save a song.
     */
    fun likeSong(song: StreamingSong) {
        viewModelScope.launch {
            try {
                repository.likeSong(song.id)
                _likedSongs.value = repository.getLikedSongs().first()
            } catch (e: Exception) {
                _error.value = "Failed to save song: ${e.message}"
            }
        }
    }

    /**
     * Unlike/unsave a song.
     */
    fun unlikeSong(song: StreamingSong) {
        viewModelScope.launch {
            try {
                repository.unlikeSong(song.id)
                _likedSongs.value = repository.getLikedSongs().first()
            } catch (e: Exception) {
                _error.value = "Failed to remove song: ${e.message}"
            }
        }
    }

    /**
     * Follow an artist.
     */
    fun followArtist(artist: StreamingArtist) {
        viewModelScope.launch {
            try {
                repository.followArtist(artist.id)
                _followedArtists.value = repository.getFollowedArtists().first()
            } catch (e: Exception) {
                _error.value = "Failed to follow artist: ${e.message}"
            }
        }
    }

    /**
     * Save an album.
     */
    fun saveAlbum(album: StreamingAlbum) {
        viewModelScope.launch {
            try {
                repository.saveAlbum(album.id)
                _savedAlbums.value = repository.getSavedAlbums().first()
            } catch (e: Exception) {
                _error.value = "Failed to save album: ${e.message}"
            }
        }
    }

    /**
     * Download a song for offline playback.
     */
    fun downloadSong(song: StreamingSong) {
        viewModelScope.launch {
            try {
                repository.downloadSong(song.id)
                _downloadedSongs.value = repository.getDownloadedSongs().first()
            } catch (e: Exception) {
                _error.value = "Download failed: ${e.message}"
            }
        }
    }
    
    /**
     * Set streaming quality.
     */
    fun setStreamingQuality(quality: StreamingQuality) {
        viewModelScope.launch {
            _streamingConfig.value = _streamingConfig.value.copy(streamingQuality = quality)
            appSettings.setStreamingQuality(quality.name)
        }
    }
    
    /**
     * Clear all loaded content.
     */
    private fun clearContent() {
        _recommendations.value = emptyList()
        _newReleases.value = emptyList()
        _featuredPlaylists.value = emptyList()
        _browseCategories.value = emptyList()
        _topCharts.value = emptyList()
        _likedSongs.value = emptyList()
        _savedAlbums.value = emptyList()
        _followedArtists.value = emptyList()
        _savedPlaylists.value = emptyList()
        _downloadedSongs.value = emptyList()
        _queue.value = emptyList()
        _currentSong.value = null
    }
    
    /**
     * Clear error state.
     */
    fun clearError() {
        _error.value = null
    }

    private suspend fun seedSongsFromService(limit: Int): List<StreamingSong> {
        val seedQueries = listOf(
            "",
            "a",
            "e",
            "i",
            "o",
            "u",
            "the",
            "love",
            "mix",
            "live",
            "remix"
        )
        val uniqueSongs = LinkedHashMap<String, StreamingSong>()
        val safeLimit = limit.coerceAtLeast(1)

        for (query in seedQueries) {
            val songs = repository.searchSongs(query)
                .filterIsInstance<StreamingSong>()
                .distinctBy { it.id }
            for (song in songs) {
                uniqueSongs.putIfAbsent(song.id, song)
                if (uniqueSongs.size >= safeLimit) {
                    return uniqueSongs.values.take(safeLimit).toList()
                }
            }
        }

        return uniqueSongs.values.take(safeLimit).toList()
    }

    private fun deriveAlbumsFromSongs(
        songs: List<StreamingSong>,
        limit: Int
    ): List<StreamingAlbum> {
        if (songs.isEmpty()) {
            return emptyList()
        }

        return songs
            .filter { it.album.isNotBlank() }
            .groupBy { song -> "${song.sourceType.name}:${song.artist.lowercase()}:${song.album.lowercase()}" }
            .values
            .sortedByDescending { albumSongs -> albumSongs.size }
            .take(limit.coerceAtLeast(1))
            .map { albumSongs ->
                val firstSong = albumSongs.first()
                StreamingAlbum(
                    id = "derived:${firstSong.sourceType.name}:album:${firstSong.artist.lowercase()}:${firstSong.album.lowercase()}",
                    title = firstSong.album,
                    artist = firstSong.artist,
                    artworkUri = albumSongs.firstNotNullOfOrNull { it.artworkUri },
                    songCount = albumSongs.size,
                    year = firstSong.releaseDate?.take(4)?.toIntOrNull(),
                    sourceType = firstSong.sourceType,
                    tracks = albumSongs
                )
            }
    }

    private fun deriveArtistsFromSongs(
        songs: List<StreamingSong>,
        limit: Int
    ): List<StreamingArtist> {
        if (songs.isEmpty()) {
            return emptyList()
        }

        val separatorEnabled = appSettings.artistSeparatorEnabled.value
        val separatorDelimiters = appSettings.artistSeparatorDelimiters.value.ifBlank { "/;,+&" }

        return songs
            .filter { it.artist.isNotBlank() }
            .flatMap { song ->
                val artistNames = ArtistSeparator.splitArtists(
                    artistString = song.artist,
                    delimiters = separatorDelimiters,
                    enabled = separatorEnabled
                )

                if (artistNames.isEmpty()) {
                    listOf(song to song.artist.trim())
                } else {
                    artistNames.mapNotNull { artistName ->
                        artistName.trim().takeIf { it.isNotBlank() }?.let { trimmedName ->
                            song to trimmedName
                        }
                    }
                }
            }
            .groupBy { (song, artistName) -> "${song.sourceType.name}:${artistName.lowercase()}" }
            .values
            .sortedByDescending { artistSongs -> artistSongs.size }
            .take(limit.coerceAtLeast(1))
            .map { artistSongs ->
                val firstSong = artistSongs.first().first
                val artistName = artistSongs.first().second
                val artistTracks = artistSongs.map { it.first }
                val artistAlbums = deriveAlbumsFromSongs(artistTracks, limit = 8)
                StreamingArtist(
                    id = "derived:${firstSong.sourceType.name}:artist:${artistName.lowercase()}",
                    name = artistName,
                    artworkUri = null,
                    songCount = artistTracks.size,
                    albumCount = artistAlbums.size,
                    sourceType = firstSong.sourceType,
                    topTracks = artistTracks.take(20),
                    albums = artistAlbums
                )
            }
    }

    private fun derivePlaylistsFromSongs(
        songs: List<StreamingSong>,
        limit: Int
    ): List<StreamingPlaylist> {
        if (songs.isEmpty()) {
            return emptyList()
        }

        val sourceType = songs.first().sourceType
        val maxCount = limit.coerceAtLeast(1)
        val results = mutableListOf<StreamingPlaylist>()

        results += StreamingPlaylist(
            id = "derived:${sourceType.name}:playlist:library-mix",
            name = "Library Mix",
            description = "Built from your streaming catalog",
            artworkUri = songs.firstNotNullOfOrNull { it.artworkUri },
            songCount = songs.size,
            isEditable = false,
            sourceType = sourceType,
            tracks = songs.take(120)
        )

        val artistMixes = songs
            .filter { it.artist.isNotBlank() }
            .groupBy { it.artist }
            .values
            .filter { it.size >= 2 }
            .sortedByDescending { it.size }
            .take((maxCount - 1).coerceAtLeast(0))
            .map { artistSongs ->
                val firstSong = artistSongs.first()
                StreamingPlaylist(
                    id = "derived:${sourceType.name}:playlist:artist:${firstSong.artist.lowercase()}",
                    name = "${firstSong.artist} Mix",
                    description = "Auto playlist from your streaming service",
                    artworkUri = artistSongs.firstNotNullOfOrNull { it.artworkUri },
                    songCount = artistSongs.size,
                    isEditable = false,
                    sourceType = sourceType,
                    tracks = artistSongs.take(60)
                )
            }

        results += artistMixes
        return results.take(maxCount)
    }

    private suspend fun checkAndSyncAuthentication(
        serviceId: String = appSettings.streamingService.value
    ): Boolean {
        val normalizedServiceId = normalizeServiceId(serviceId)
        val credentialsExist = providerRepository?.isServiceConnected(normalizedServiceId)
            ?: serviceSessionRepository.isConnected(normalizedServiceId)

        if (!credentialsExist) {
            _isAuthenticated.value = false
            _streamingConfig.value = _streamingConfig.value.copy(
                activeService = sourceTypeFromServiceId(normalizedServiceId),
                isAuthenticated = false
            )
            return false
        }

        // Actually test the connection with a ping to detect stale connections
        val connected = try {
            when (normalizedServiceId) {
                "SUBSONIC" -> providerRepository?.authenticate() == true
                "JELLYFIN" -> providerRepository?.authenticate() == true
                else -> false
            }
        } catch (e: Exception) {
            // Network error - connection is stale or network unavailable
            false
        }

        _isAuthenticated.value = connected
        _streamingConfig.value = _streamingConfig.value.copy(
            activeService = sourceTypeFromServiceId(normalizedServiceId),
            isAuthenticated = connected
        )
        return connected
    }

    private fun validateCredentials(
        serviceId: String,
        serverUrl: String,
        username: String,
        password: String
    ) {
        if (StreamingServiceRules.requiresServerUrl(serviceId) && serverUrl.isBlank()) {
            throw IllegalArgumentException("Server URL is required")
        }
        val requiresUsername = true
        if (requiresUsername && username.isBlank()) {
            throw IllegalArgumentException("Username is required")
        }
        if (password.isBlank()) {
            throw IllegalArgumentException("Password is required")
        }
    }

    private fun sourceTypeFromServiceId(serviceId: String): SourceType {
        return when (serviceId.uppercase()) {
            StreamingServiceId.SUBSONIC -> SourceType.SUBSONIC
            StreamingServiceId.JELLYFIN -> SourceType.JELLYFIN
            else -> SourceType.UNKNOWN
        }
    }

    private fun serviceIdFromSourceType(sourceType: SourceType): String {
        return when (sourceType) {
            SourceType.SUBSONIC -> StreamingServiceId.SUBSONIC
            SourceType.JELLYFIN -> StreamingServiceId.JELLYFIN
            SourceType.SPOTIFY,
            SourceType.APPLE_MUSIC,
            SourceType.YOUTUBE_MUSIC,
            SourceType.DEEZER,
            SourceType.LOCAL,
            SourceType.UNKNOWN -> StreamingServiceId.SUBSONIC
        }
    }

    private fun normalizeServiceId(serviceId: String): String {
        val normalized = serviceId.uppercase()
        return if (StreamingServiceId.all.contains(normalized)) {
            normalized
        } else {
            StreamingServiceId.SUBSONIC
        }
    }

    private fun artistIdMatchesSongArtist(artistId: String, songArtist: String): Boolean {
        val normalizedArtist = songArtist.trim().lowercase()
        if (normalizedArtist.isBlank()) {
            return false
        }

        val normalizedId = artistId.lowercase()
        return normalizedId.contains(normalizedArtist) ||
            normalizedId.contains(normalizedArtist.replace(" ", "_")) ||
            normalizedId.contains(normalizedArtist.replace(" ", "-"))
    }
}

/**
 * Container for streaming search results.
 */
data class StreamingSearchResults(
    val songs: List<StreamingSong> = emptyList(),
    val albums: List<StreamingAlbum> = emptyList(),
    val artists: List<StreamingArtist> = emptyList(),
    val playlists: List<StreamingPlaylist> = emptyList()
) {
    val isEmpty: Boolean
        get() = songs.isEmpty() && albums.isEmpty() && artists.isEmpty() && playlists.isEmpty()
    
    val totalCount: Int
        get() = songs.size + albums.size + artists.size + playlists.size
}
