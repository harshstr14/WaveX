package com.example.wavex.service

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.text.Html
import android.util.Log
import android.util.LruCache
import androidx.annotation.OptIn
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleService
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Player.Listener
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaStyleNotificationHelper
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.wavex.R
import com.example.wavex.homeScreen.PlayerManager
import com.example.wavex.homeScreen.SongItem
import com.example.wavex.homeScreen.WaveXApplication.Companion.MUSIC_CHANNEL_ID
import com.example.wavex.homeScreen.htmlToText
import com.example.wavex.homeScreen.localDB.entity.DownloadedSongEntity
import com.example.wavex.homeScreen.repository.RecentlyPlayedRepository
import com.example.wavex.homeScreen.toRecentlyPlayedEntity
import com.example.wavex.playerScreen.PlayerActivityScreen
import com.example.wavex.profileScreen.downloadedSongScreen.DownloadRepository
import com.example.wavex.profileScreen.settingScreen.AudioStreamQualityPreference
import com.example.wavex.profileScreen.settingScreen.StreamQualitySelector
import com.example.wavex.profileScreen.settingScreen.repository.SettingsRepository
import com.example.wavex.recommendation.MusicHistoryRepository
import com.example.wavex.recommendation.dataClass.PlayedSong
import com.example.wavex.searchScreen.SearchSource
import com.example.wavex.searchScreen.repository.SearchSongsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class  MusicPlayerService : LifecycleService() {
    companion object {
        const val NOTIFICATION_ID = 1

        // Action strings for notification intents
        const val ACTION_PLAY_NEW = "com.example.app.action.PLAY_NEW"
        const val ACTION_PLAY_SONG = "com.example.app.action.PLAY_SONG"
        const val ACTION_PLAY = "com.example.app.ACTION_PLAY"
        const val ACTION_PAUSE = "com.example.app.ACTION_PAUSE"
        const val ACTION_NEXT = "com.example.app.ACTION_NEXT"
        const val ACTION_PREV = "com.example.app.ACTION_PREV"
        const val ACTION_REPEAT = "com.example.app.ACTION_REPEAT"
        const val ACTION_SHUFFLE = "com.example.app.ACTION_SHUFFLE"
        const val ACTION_STOP = "com.example.app.ACTION_STOP"

        const val ACTION_DOWNLOAD_START = "ACTION_DOWNLOAD_START"
        const val ACTION_DOWNLOAD_RESUME = "ACTION_DOWNLOAD_RESUME"
        const val ACTION_DOWNLOAD_PAUSE = "ACTION_DOWNLOAD_PAUSE"
    }

    inner class LocalBinder : Binder() {
        fun getService(): MusicPlayerService = this@MusicPlayerService
    }

    private val binder = LocalBinder()
    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaSession
    private val handler = Handler(Looper.getMainLooper())
    private val progressRefreshMs = 100L
    private var currentAlbumArt: Bitmap? = null
    private var currentArtSongId: String? = null
    private var streamQualityPreference = AudioStreamQualityPreference.HIGH
    var downloadQualityPreference = AudioStreamQualityPreference.HIGH
        private set

    private val ytMusicRegex = Regex("w\\d+-h\\d+")
    private val normalRegex = Regex("\\d+x\\d+")

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val imageLoader by lazy {
        ImageLoader.Builder(this)
            .crossfade(true)
            .respectCacheHeaders(false)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("album_art_cache"))
                    .maxSizeBytes(100L * 1024 * 1024)
                    .build()
            }
            .build()
    }

    private val progressRunnable = object : Runnable {
        override fun run() {
            if (::player.isInitialized) {
                updatePlaybackInfo()
                handler.postDelayed(this, progressRefreshMs)
            }
        }
    }

    private val history: MutableList<SongItem> = mutableListOf()
    private val queue: MutableList<SongItem> = mutableListOf()
    private var playlist: MutableList<SongItem> = mutableListOf()
    private var currentQueuedSong: SongItem? = null
    private var currentIndex = -1

    private var playJob: Job? = null
    private var artLoadJob: Job? = null
    private var qualityJob: Job? = null
    private var bufferingJob: Job? = null
    private var downloadQualityJob: Job? = null

    private val artCache = LruCache<String, Bitmap>(30)
    private val historyRepository = MusicHistoryRepository()
    private var isHistorySaved = false

    @Inject
    lateinit var settingsRepository: SettingsRepository
    @Inject
    lateinit var recentlyPlayedRepository: RecentlyPlayedRepository
    @Inject
    lateinit var downloadedSongsRepository: DownloadRepository

    private val _currentSong = MutableStateFlow<SongItem?>(null)
    val currentSong: StateFlow<SongItem?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _progress = MutableStateFlow(0)
    val progress: StateFlow<Int> = _progress.asStateFlow()

    private val _duration = MutableStateFlow(0)
    val duration: StateFlow<Int> = _duration.asStateFlow()

    private val _buffer = MutableStateFlow(0)
    val buffer: StateFlow<Int> = _buffer.asStateFlow()

    private val _isBuffering = MutableStateFlow(false)
    val isBuffering: StateFlow<Boolean> = _isBuffering.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    private val _playlistFlow = MutableStateFlow<List<SongItem>>(emptyList())
    val playlistFlow: StateFlow<List<SongItem>> = _playlistFlow.asStateFlow()

    private val _upNextFlow = MutableStateFlow<List<SongItem>>(emptyList())
    val upNextFlow: StateFlow<List<SongItem>> = _upNextFlow.asStateFlow()

    private val _currentIndexFlow = MutableStateFlow(-1)
    val currentIndexFlow: StateFlow<Int> = _currentIndexFlow.asStateFlow()

    private val _queue = MutableStateFlow<List<SongItem>>(emptyList())
    val queueFlow: StateFlow<List<SongItem>> = _queue.asStateFlow()
    private var isQueueOnlyMode = false
    private var queuePointer = 0
    private val repository = SearchSongsRepository()

    val isOnlineFlow by lazy {
        NetworkMonitor(applicationContext).isOnline
    }
    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline

    private var shouldRetry = false
    private var isRetrying = false
    private var shouldFetchSuggestions = false

    override fun onCreate() {
        super.onCreate()

        ServiceLocator.musicService = this

        val placeholderNotification = NotificationCompat.Builder(this, MUSIC_CHANNEL_ID)
            .setContentTitle("WaveX is starting…")
            .setContentText("Preparing your music")
            .setSmallIcon(R.drawable.headset_icon)
            .build()
        startForeground(NOTIFICATION_ID, placeholderNotification)

        initPlayer()
        initMediaSession()
        observeStreamQuality()
        observeDownloadQuality()

        serviceScope.launch {
            isOnlineFlow
                .distinctUntilChanged()
                .collectLatest { isOnline ->
                    handleNetworkChange(isOnline)
                }
        }

        serviceScope.launch {
            _currentSong
                .filterNotNull()
                .collect { song ->
                    handleSongChange(song)
                }
        }
    }

    private fun observeStreamQuality() {
        qualityJob?.cancel()
        qualityJob = serviceScope.launch {
            settingsRepository
                .streamQualityFlow
                .distinctUntilChanged()
                .collectLatest { preference ->
                    if (streamQualityPreference != preference) {
                        setQuality(preference)
                    }
                }
        }
    }

    private fun observeDownloadQuality() {
        downloadQualityJob?.cancel()

        downloadQualityJob = serviceScope.launch {
            settingsRepository
                .downloadQualityFlow
                .distinctUntilChanged()
                .collectLatest { preference ->

                    if (downloadQualityPreference != preference) {
                        setDownloadQuality(preference)
                    }
                }
        }
    }

    private fun handleSongChange(song: SongItem) {
        serviceScope.launch {
            recentlyPlayedRepository.addSong(
                song.toRecentlyPlayedEntity()
            )
        }

        if (!shouldFetchSuggestions) return
        shouldFetchSuggestions = false

        serviceScope.launch(Dispatchers.IO) {
            try {
                val suggestions = repository.fetchSuggestionSongs(song.id)

                val updated = (playlist + suggestions)
                    .distinctBy { it.id }

                withContext(Dispatchers.Main) {
                    playlist.apply {
                        clear()
                        addAll(updated)
                    }

                    _playlistFlow.value = playlist.toList()
                    updateUpNext()
                }
            } catch (e: Exception) {
                Log.e("Suggestion", "Failed: ${e.message}")
            }
        }
    }

    private fun handleNetworkChange(isOnline: Boolean) {
        val currentSong = _currentSong.value ?: return
        val localFile = currentSong.localPath?.let { File(it) }

        val isLocalPlaying =
            player.currentMediaItem?.localConfiguration?.uri?.scheme == "file"

        if (!isOnline) {
            if (localFile != null && localFile.exists() && !isLocalPlaying) {
                val position = player.currentPosition

                player.setMediaItem(
                    MediaItem.fromUri(localFile.toUri()),
                    position
                )
                player.prepare()
                player.play()
                player.playWhenReady = true
            } else {
                player.pause()
            }
            return
        }

        handleOnlineRecovery()
    }

    private fun handleOnlineRecovery() {
        if (!shouldRetry || isRetrying) return

        serviceScope.launch {
            isRetrying = true
            shouldRetry = false

            val song = _currentSong.value ?: return@launch
            val position = player.currentPosition

            val refreshedSong = recentlyPlayedRepository.getPlayableSong(song)

            val uri = refreshedSong.getBestUri(true)

            if (uri != Uri.EMPTY) {
                player.setMediaItem(MediaItem.fromUri(uri))
                player.prepare()
                player.seekTo(position)
                player.playWhenReady = true
            }

            isRetrying = false
        }
    }

    @OptIn(UnstableApi::class)
    private fun initPlayer() {
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                30000,
                120000,
                5000,
                10000
            )
            .build()

        val httpDataSourceFactory =
            DefaultHttpDataSource.Factory()
                .setUserAgent(
                    "com.google.android.youtube/20.12.46 (Linux; U; Android 14)"
                )
                .setAllowCrossProtocolRedirects(true)
                .setConnectTimeoutMs(30000)
                .setReadTimeoutMs(30000)
                .setKeepPostFor302Redirects(true)
                .setDefaultRequestProperties(
                    mapOf(
                        "Referer" to "https://www.youtube.com/",
                        "Origin" to "https://www.youtube.com",
                        "Accept" to "*/*"
                    )
                )

        val dataSourceFactory =
            DefaultDataSource.Factory(
                this,
                httpDataSourceFactory
            )

        player = ExoPlayer.Builder(this)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .setLoadControl(loadControl)
            .setMediaSourceFactory(
                DefaultMediaSourceFactory(dataSourceFactory)
            )
            .build()

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()
        player.setAudioAttributes(audioAttributes, true)
        player.setHandleAudioBecomingNoisy(true)

        player.addListener(object : Listener {
            override fun onPlaybackStateChanged(state: Int) {
                Log.d(
                    "PLAYER_STATE",
                    when (state) {
                        Player.STATE_IDLE -> "IDLE"
                        Player.STATE_BUFFERING -> "BUFFERING"
                        Player.STATE_READY -> "READY"
                        Player.STATE_ENDED -> "ENDED"
                        else -> "UNKNOWN"
                    }
                )

                when(state) {
                    Player.STATE_BUFFERING -> {
                        bufferingJob?.cancel()

                        bufferingJob = serviceScope.launch {
                            delay(500)

                            if (player.playbackState == Player.STATE_BUFFERING) {
                                _isBuffering.value = true
                            }
                        }
                    }

                    Player.STATE_READY -> {
                        bufferingJob?.cancel()
                        _isBuffering.value = false

                        if (currentIndex in playlist.indices) {
                            val song = playlist[currentIndex]
                            val duration = player.duration.coerceAtLeast(song.duration.toLong())

                            _duration.value = duration.toInt()
                            _buffer.value = player.bufferedPosition.toInt()

                            val bitmap = currentAlbumArt ?: placeholderBitmap
                            updateMetadata(song, bitmap)

                            serviceScope.launch {
                                updateNotification()
                            }
                        }
                    }

                    Player.STATE_ENDED -> {
                        _isBuffering.value = false
                        next()
                    }

                    else -> {
                        _isBuffering.value = false
                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
                if (isPlaying) handler.post(progressRunnable) else handler.removeCallbacks(progressRunnable)
                serviceScope.launch {
                    updateNotification()
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.e(
                    "PLAYER_ERROR",
                    """
                Message : ${error.message}
                Code    : ${error.errorCode}
                Name    : ${error.errorCodeName}
                Cause   : ${error.cause}
                """.trimIndent(),
                    error
                )

                val currentSong = _currentSong.value ?: return

                val localFile = currentSong.localPath?.let(::File)

                if (localFile?.exists() == true) {
                    val position = player.currentPosition

                    player.setMediaItem(
                        MediaItem.fromUri(localFile.toUri()),
                        position
                    )

                    player.prepare()
                    player.play()

                    return
                }

                shouldRetry = true
            }
        })
    }

    private fun initMediaSession() {
        mediaSession = MediaSession.Builder(this, player)
            .setCallback(object : MediaSession.Callback {})
            .build()
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        intent?.action?.let { action ->
            when (action) {
                ACTION_PLAY_NEW -> {
                    val index = intent.getIntExtra("index", 0)
                    val fromSearch = intent.getBooleanExtra("from_search", false)
                    val playlist = PlayerManager.currentPlaylist

                    if (playlist.isNotEmpty() && index in playlist.indices) {
                        setPlaylist(playlist, index)
                        shouldFetchSuggestions = fromSearch
                        // Only start foreground if a song is available
                        _currentSong.value?.let { song ->
                            startForegroundWithNotification(song)
                        }
                    }
                }
                ACTION_PLAY_SONG -> {
                    val index = intent.getIntExtra("index", 0)
                    val playlist = PlayerManager.currentPlaylist

                    if (playlist.isNotEmpty() && index in playlist.indices) {
                        play(playlist[index])
                        _currentSong.value?.let { song ->
                            startForegroundWithNotification(song)
                        }
                    }
                }
                ACTION_PLAY -> resume()
                ACTION_PAUSE -> togglePlayPause()
                ACTION_NEXT -> next()
                ACTION_PREV -> previous()
                ACTION_SHUFFLE -> shuffleToggle()
                ACTION_REPEAT -> repeatToggle()
                ACTION_STOP -> stopServiceAndNotification()

                ACTION_DOWNLOAD_START -> {
                    val url = intent.getStringExtra("url") ?: return START_NOT_STICKY
                    val fileName = intent.getStringExtra("fileName") ?: return START_NOT_STICKY
                    val songId = intent.getStringExtra("songId") ?: return START_NOT_STICKY

                    serviceScope.launch {
                        Log.d("DOWNLOAD_DEBUG", "Service download start: $songId")

                        ParallelDownloader.start(
                            scope = serviceScope,
                            songId = songId,
                            url = url,
                            fileName = fileName,
                            context = this@MusicPlayerService
                        ) { path ->
                            if (path != null) {
                                val song = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    intent.getParcelableExtra("song", SongItem::class.java)
                                } else {
                                    @Suppress("DEPRECATION")
                                    intent.getParcelableExtra("song")
                                }

                                song?.let {
                                    serviceScope.launch {
                                        downloadedSongsRepository.insert(
                                            DownloadedSongEntity(
                                                id = it.id,
                                                name = it.name,
                                                artist = it.artist,
                                                album = it.album,
                                                image = it.image,
                                                duration = it.duration,
                                                playCount = it.playCount,
                                                downloadUrl = it.downloadUrl,
                                                localPath = path,
                                                songSource = song.songSource,
                                                playedAt = song.playedAt
                                            )
                                        )
                                    }
                                }

                                ParallelDownloader.downloadStates[songId] =
                                    ParallelDownloader.DownloadState.COMPLETED
                            } else {
                                ParallelDownloader.downloadStates[songId] =
                                    ParallelDownloader.DownloadState.FAILED
                            }
                        }
                    }
                }

                ACTION_DOWNLOAD_RESUME -> {
                    val url = intent.getStringExtra("url") ?: return START_NOT_STICKY
                    val fileName = intent.getStringExtra("fileName") ?: return START_NOT_STICKY
                    val songId = intent.getStringExtra("songId") ?: return START_NOT_STICKY

                    serviceScope.launch {
                        Log.d("DOWNLOAD_DEBUG", "Service download start: $songId")

                        ParallelDownloader.resume(
                            scope = serviceScope,
                            songId = songId,
                            url = url,
                            fileName = fileName,
                            context = this@MusicPlayerService
                        ) { path ->
                            if (path != null) {
                                val song = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    intent.getParcelableExtra("song", SongItem::class.java)
                                } else {
                                    @Suppress("DEPRECATION")
                                    intent.getParcelableExtra("song")
                                }

                                song?.let {
                                    serviceScope.launch {
                                        downloadedSongsRepository.insert(
                                            DownloadedSongEntity(
                                                id = it.id,
                                                name = it.name,
                                                artist = it.artist,
                                                album = it.album,
                                                image = it.image,
                                                duration = it.duration,
                                                playCount = it.playCount,
                                                downloadUrl = it.downloadUrl,
                                                localPath = path,
                                                songSource = song.songSource,
                                                playedAt = song.playedAt
                                            )
                                        )
                                    }
                                }

                                ParallelDownloader.downloadStates[songId] =
                                    ParallelDownloader.DownloadState.COMPLETED
                            } else {
                                ParallelDownloader.downloadStates[songId] =
                                    ParallelDownloader.DownloadState.FAILED
                            }
                        }
                    }
                }

                ACTION_DOWNLOAD_PAUSE -> {
                    val songId = intent.getStringExtra("songId") ?: return START_NOT_STICKY
                    ParallelDownloader.pause(songId)
                }
            }
        }

        if (!::player.isInitialized) {
            val notification = NotificationCompat.Builder(this, MUSIC_CHANNEL_ID)
                .setContentTitle("WaveX is running…")
                .setContentText("Preparing your music")
                .setSmallIcon(R.drawable.headset_icon)
                .build()
            startForeground(NOTIFICATION_ID, notification)
        }

        return START_STICKY
    }

    fun SongItem.getBestUri(isOnline: Boolean): Uri {
        Log.d(
            "PLAY_FLOW",
            """
        ========= GET BEST URI =========
        Online     : $isOnline
        Streams    : ${downloadUrl.size}
        LocalPath  : $localPath
        =================================
        """.trimIndent()
        )

        downloadUrl.forEachIndexed { index, stream ->
            Log.d(
                "PLAY_FLOW",
                """
            Stream[$index]
            Quality : ${stream.quality}
            Expire  : ${stream.expiresAt}
            Url     : ${stream.url.take(100)}
            """.trimIndent()
            )
        }

        val localFile = localPath?.let { File(it) }

        return when {
            localFile != null && localFile.exists() -> {
                Log.d(
                    "PLAY_FLOW",
                    "Using local file"
                )

                localFile.toUri()
            }

            isOnline -> {
                if (downloadUrl.isEmpty()) {
                    Log.d("URL", "Empty url")
                    return Uri.EMPTY
                }

                val selectedStream =
                    StreamQualitySelector.selectPlaybackStream(
                        streams = downloadUrl,
                        preference = streamQualityPreference
                    )

                Log.d(
                    "PLAY_FLOW",
                    """
                Selected Stream
                Quality : ${selectedStream?.quality}
                Url     : ${selectedStream?.url?.take(150)}
                """.trimIndent()
                )

                selectedStream?.url?.toUri() ?: Uri.EMPTY
            }
            else -> {
                Log.d(
                    "PLAY_FLOW",
                    "Offline and no local file"
                )

                Uri.EMPTY
            }
        }
    }

    fun setPlaylist(songs: List<SongItem>?, startAtIndex: Int = 0) {
        _isShuffle.value = false
        _repeatMode.value = Player.REPEAT_MODE_OFF

        playlist.clear()
        history.clear()
        clearQueue()

        val safeSongs = songs?.distinctBy { it.id } ?: emptyList()
        playlist.addAll(safeSongs)
        _playlistFlow.value = playlist.toList()

        if (startAtIndex in playlist.indices) {
            playIndex(startAtIndex)
        }

        updateUpNext()
        updateNotification()
    }

    fun play(song: SongItem) {
        val idx = playlist.indexOfFirst { it.id == song.id }
        if (idx >= 0) {
            playIndex(idx)
        } else {
            playlist.add(song)
            _playlistFlow.value = playlist.toList()
            playIndex(playlist.lastIndex)
        }
    }

    fun play() {
        if (::player.isInitialized) {
            player.play()
        }
    }

    fun pause() {
        if (::player.isInitialized) {
            player.pause()
        }
    }

    fun isPlaying(): Boolean {
        return ::player.isInitialized && player.isPlaying
    }

    private fun playIndex(index: Int) {
        if (index !in playlist.indices) return

        isHistorySaved = false
        currentIndex = index
        _currentIndexFlow.value = index

        val song = playlist[index]

        currentAlbumArt = null
        currentArtSongId = null

        _currentSong.value = song

        updateUpNext()
        updateNotification()
        prepareAndPlay(song)
    }

    private fun prepareAndPlay(song: SongItem) {
        _isBuffering.value = true
        playJob?.cancel()

        Log.d(
            "PLAY_FLOW",
            """
        ========= PLAY REQUEST =========
        Song Id   : ${song.id}
        Title     : ${song.name}
        Source    : ${song.songSource}
        Online    : ${_isOnline.value}
        =================================
        """.trimIndent()
        )

        playJob = serviceScope.launch {
            val playableSong =
                if (song.songSource == SearchSource.YTMUSIC.name) {
                    recentlyPlayedRepository.getPlayableSong(song)
                } else {
                    song
                }

            Log.d(
                "PLAY_FLOW",
                """
            ========= PLAYABLE SONG =========
            Song Id   : ${playableSong.id}
            Duration  : ${playableSong.duration}
            Streams   : ${playableSong.downloadUrl.size}
            =================================
            """.trimIndent()
            )

            val downloadedSong = downloadedSongsRepository.getSongById(playableSong.id)

            val uri =
                if (
                    downloadedSong != null &&
                    File(downloadedSong.localPath).exists()
                ) {
                    Log.d(
                        "PLAY_FLOW",
                        "Using downloaded file: ${downloadedSong.localPath}"
                    )

                    Uri.fromFile(File(downloadedSong.localPath))
                } else {
                    playableSong.getBestUri(_isOnline.value)
                }

//            Log.d(
//                "STREAM_URI",
//                """
//                    Scheme : ${uri.scheme}
//                    Host   : ${uri.host}
//                    Path   : ${uri.path}
//                    Itag   : ${uri.getQueryParameter("itag")}
//                    Mime   : ${uri.getQueryParameter("mime")}
//                    Dur    : ${uri.getQueryParameter("dur")}
//                    Source : ${uri.getQueryParameter("source")}
//                    Expire : ${uri.getQueryParameter("expire")}
//                    Ip     : ${uri.getQueryParameter("ip")}
//                    """.trimIndent()
//            )

            Log.d(
                "PLAY_FLOW",
                """
            ========= FINAL URI =========
            Uri     : $uri
            Scheme  : ${uri.scheme}
            Host    : ${uri.host}
            Path    : ${uri.path}
            Itag    : ${uri.getQueryParameter("itag")}
            Expire  : ${uri.getQueryParameter("expire")}
            =================================
            """.trimIndent()
            )

            if (uri == Uri.EMPTY) {
                Log.e("PLAY_FLOW", "URI EMPTY - Playback aborted")
                _isBuffering.value = false
                return@launch
            }

            Log.d(
                "PLAY_FLOW",
                """
                Saving song
                Id    : ${playableSong.id}
                Title : ${playableSong.name}
                Duration : ${playableSong.duration}
                """.trimIndent()
            )

            recentlyPlayedRepository.addSong(
                playableSong.toRecentlyPlayedEntity()
            )

            recentlyPlayedRepository.addSong(
                playableSong.toRecentlyPlayedEntity()
            )

            player.setMediaItem(
                MediaItem.Builder()
                    .setUri(uri)
                    .setMediaId(playableSong.id)
                    .build()
            )

            Log.d("PLAY_FLOW", "Calling player.prepare()")

            player.prepare()

            Log.d("PLAY_FLOW", "Setting playWhenReady=true")
            player.playWhenReady = true
        }
    }

    fun getCurrentPosition(): Int {
        return player.currentPosition.toInt()
    }

//    fun getDuration(): Int {
//        return player.duration.toInt()
//    }

    fun togglePlayPause() {
        if (::player.isInitialized) {
            if (player.isPlaying) {
                player.pause()
            } else {
                player.play()
            }
        }
    }

    fun resume() {
        if (::player.isInitialized) player.play()
    }

    fun seekTo(positionMs: Long) {
        if (::player.isInitialized) {
            player.seekTo(positionMs)

            _progress.value = positionMs.toInt()
        }
    }

    fun next() {
        val current = _currentSong.value

        if (_repeatMode.value == Player.REPEAT_MODE_ONE) {
            playIndex(currentIndex)
            return
        }

        if (!isQueueOnlyMode) {
            currentQueuedSong?.let { queuedSong ->
                queue.removeAll { it.id == queuedSong.id }
                _queue.value = queue.toList()
                currentQueuedSong = null

                updateUpNext()
            }
        }

        addToHistory(current)

        if (queue.isNotEmpty()) {
            if (isQueueOnlyMode) {
                val nextSong = if (isShuffle.value) {
                    if (queue.size == 1) {
                        queue[0]
                    } else {
                        val available = queue.filter { it.id != _currentSong.value?.id }
                        if (available.isNotEmpty()) {
                            available.random()
                        } else {
                            queue.random()
                        }
                    }
                } else {
                    queuePointer++

                    if (queuePointer >= queue.size) {
                        if (_repeatMode.value == Player.REPEAT_MODE_ALL) {
                            queuePointer = 0
                        } else {
                            return
                        }
                    }

                    queue[queuePointer]
                }

                val idx = playlist.indexOfFirst { it.id == nextSong.id }
                if (idx >= 0) {
                    currentIndex = idx
                    _currentIndexFlow.value = idx
                }

                _currentSong.value = nextSong
                prepareAndPlay(nextSong)
                updateUpNext()

                return
            } else {
                val nextSong = queue.first()
                currentQueuedSong = nextSong

                val idx = playlist.indexOfFirst { it.id == nextSong.id }
                if (idx >= 0) {
                    currentIndex = idx
                    _currentIndexFlow.value = idx
                }

                _currentSong.value = nextSong
                prepareAndPlay(nextSong)
                updateUpNext()
            }

            return
        }

        if (playlist.isEmpty()) return

        if (isShuffle.value) {
            val randomIndex = getRandomIndex()
            if (randomIndex != -1) {
                playIndex(randomIndex)
            }
            return
        } else {
            val nextIndex = currentIndex + 1

            if (nextIndex in playlist.indices) {
                playIndex(nextIndex)
            } else {
                if (_repeatMode.value == Player.REPEAT_MODE_ALL) {
                    playIndex(0)
                } else {
                    updateUpNext()
                    return
                }
            }
        }
    }

    fun previous() {
        if (::player.isInitialized && player.currentPosition > 5000) {
            player.seekTo(0)
            return
        }

        if (_repeatMode.value == Player.REPEAT_MODE_ONE) {
            player.seekTo(0)
            player.play()
            return
        }

        if (isQueueOnlyMode && queue.isNotEmpty()) {
            if (isShuffle.value) {
                val available = queue.filter { it.id != _currentSong.value?.id }
                val prevSong = if (available.isNotEmpty()) {
                    available.random()
                } else {
                    queue.random()
                }

                val idx = playlist.indexOfFirst { it.id == prevSong.id }
                if (idx >= 0) {
                    currentIndex = idx
                    _currentIndexFlow.value = idx
                }

                _currentSong.value = prevSong
                prepareAndPlay(prevSong)
                updateUpNext()
                return
            }

            queuePointer--

            if (queuePointer < 0) {
                if (_repeatMode.value == Player.REPEAT_MODE_ALL) {
                    queuePointer = queue.lastIndex
                } else {
                    player.seekTo(0)
                    return
                }
            }

            val prevSong = queue.getOrNull(queuePointer) ?: return

            val idx = playlist.indexOfFirst { it.id == prevSong.id }
            if (idx >= 0) {
                currentIndex = idx
                _currentIndexFlow.value = idx
            }

            _currentSong.value = prevSong
            prepareAndPlay(prevSong)
            updateUpNext()
            return
        }

        if (playlist.isEmpty()) return

        if (history.isNotEmpty()) {
            val previousSong = history.removeAt(history.lastIndex)

            val index = playlist.indexOfFirst { it.id == previousSong.id }

            if (index in playlist.indices) {
                playIndex(index)
            }

            return
        }

        if (isShuffle.value) {
            val randomIndex = getRandomIndex()
            if (randomIndex != -1) {
                playIndex(randomIndex)
            }
            return
        }

        val prevIndex = currentIndex - 1

        if (prevIndex >= 0) {
            playIndex(prevIndex)
        } else {
            if (_repeatMode.value == Player.REPEAT_MODE_ALL) {
                playIndex(playlist.lastIndex)
            } else {
                player.seekTo(0)
            }
        }
    }

    fun shuffleToggle() {
        _isShuffle.value = !_isShuffle.value

        if (!_isShuffle.value) {
            val current = _currentSong.value

            if (isQueueOnlyMode) {
                val index = queue.indexOfFirst { it.id == current?.id }
                if (index != -1) {
                    queuePointer = index
                }
            } else {
                val index = playlist.indexOfFirst { it.id == current?.id }
                if (index != -1) {
                    currentIndex = index
                }
            }
        }

        updateUpNext()
        updateNotification()
    }

    fun repeatToggle() {
        val newMode = when (_repeatMode.value) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_OFF
            else -> Player.REPEAT_MODE_OFF
        }

        _repeatMode.value = newMode

        updateNotification()
    }

    private fun updateUpNext() {
        val seen = mutableSetOf<String>()

        val combined = buildList {
            if (currentIndex in playlist.indices) {
                playlist.take(currentIndex + 1).forEach { if (seen.add(it.id)) add(it) }
                queue.forEach { if (seen.add(it.id)) add(it) }
                playlist.drop(currentIndex + 1).forEach { if (seen.add(it.id)) add(it) }
            } else {
                playlist.forEach { if (seen.add(it.id)) add(it) }
                queue.forEach { if (seen.add(it.id)) add(it) }
            }
        }

        _upNextFlow.value = combined
    }

    fun setQuality(preference: AudioStreamQualityPreference) {
        streamQualityPreference = preference

        val currentSong = _currentSong.value ?: return

        val currentPosition = player.currentPosition

        val wasPlaying = player.isPlaying

        val selectedStream =
            StreamQualitySelector.selectPlaybackStream(
                streams = currentSong.downloadUrl,
                preference = preference
            ) ?: return

        val uri =
            if (!currentSong.localPath.isNullOrEmpty()) {
                currentSong.localPath!!.toUri()
            } else {
                selectedStream.url.toUri()
            }

        val currentUri = player.currentMediaItem
                            ?.localConfiguration
                            ?.uri

        if (currentUri == uri) { return }

        player.setMediaItem(
            MediaItem.fromUri(uri),
            currentPosition
        )

        player.prepare()

        if (wasPlaying) { player.play() }
    }

    fun setDownloadQuality(
        preference: AudioStreamQualityPreference
    ) {
        downloadQualityPreference = preference
    }

    fun addToQueue(song: SongItem) {
        if (queue.any { it.id == song.id }) return

        queue.add(song)
        _queue.value = queue.toList()

        if (_currentSong.value == null) {
            queuePointer = 0
            isQueueOnlyMode = true

            val firstSong = queue[0]
            _currentSong.value = firstSong
            prepareAndPlay(firstSong)

            updateUpNext()
            return
        }

        updateUpNext()
    }

//    fun isInQueue(songId: String): Boolean {
//        return queue.any { it.id == songId }
//    }

//    fun playNext(song: SongItem) {
//        if (!queue.any { it.id == song.id }) {
//            queue.add(0, song)
//            _queue.value = queue.toList()
//        }
//
//        updateUpNext()
//    }

    fun removeFromQueue(songId: String) {
        queue.removeAll { it.id == songId }
        _queue.value = queue.toList()

        updateUpNext()
    }

    fun clearQueue() {
        queue.clear()
        _queue.value = emptyList()

        updateUpNext()
    }

    private fun addToHistory(song: SongItem?) {
        song?.let { it ->
            history.removeAll { it.id == song.id }
            history.add(it)

            if (history.size > 10) {
                history.removeAt(0)
            }
        }
    }

    private fun getRandomIndex(excludeCurrent: Boolean = true): Int {
        val size = playlist.size
        if (size == 0) return -1
        if (!excludeCurrent || size == 1) return (0 until size).random()

        val rand = (0 until size - 1).random()
        return if (rand >= currentIndex) rand + 1 else rand
    }

    private fun stopServiceAndNotification() {
        try {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        } catch (_: Throwable) { /* ignore */ }
    }

    override fun onDestroy() {
        Log.w("MusicPlayerService", "Service destroyed by system")
        handler.removeCallbacks(progressRunnable)
        if (::player.isInitialized) player.release()
        if (::mediaSession.isInitialized) mediaSession.release()
        try { stopForeground(STOP_FOREGROUND_REMOVE) } catch (_: Exception) {}
        serviceScope.cancel()
        ServiceLocator.musicService = null
        super.onDestroy()
    }

    private val placeholderBitmap by lazy {
        BitmapFactory.decodeResource(resources, R.drawable.default_image)
    }

    private fun startForegroundWithNotification(song: SongItem) {
        updateNotification()
    }

    fun updateNotification() {
        val song = _currentSong.value ?: return

        val cachedBitmap = artCache.get(song.id)

        val bitmap = when {
            currentArtSongId == song.id && currentAlbumArt != null -> {
                currentAlbumArt!!
            }

            cachedBitmap != null -> {
                currentAlbumArt = cachedBitmap
                currentArtSongId = song.id
                cachedBitmap
            }

            else -> {
                placeholderBitmap
            }
        }

        if (
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val notification = buildNotification(song, bitmap)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        if (cachedBitmap == null) {
            artLoadJob?.cancel()

            artLoadJob = serviceScope.launch {

                val loadedBitmap = withContext(Dispatchers.IO) {
                    loadAlbumArt(song)
                }

                if (_currentSong.value?.id != song.id) {
                    return@launch
                }

                currentAlbumArt = loadedBitmap
                currentArtSongId = song.id

                updateMetadata(song, loadedBitmap)

                val updatedNotification =
                    buildNotification(song, loadedBitmap)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(
                        NOTIFICATION_ID,
                        updatedNotification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                    )
                } else {
                    startForeground(
                        NOTIFICATION_ID,
                        updatedNotification
                    )
                }
            }
        }
    }

    @OptIn(UnstableApi::class)
    private fun buildNotification(song: SongItem, bitmap: Bitmap): Notification {
        val playPending    = servicePendingIntent(ACTION_PLAY)
        val pausePending   = servicePendingIntent(ACTION_PAUSE)
        val nextPending    = servicePendingIntent(ACTION_NEXT)
        val prevPending    = servicePendingIntent(ACTION_PREV)
        val shufflePending = servicePendingIntent(ACTION_SHUFFLE)
        val repeatPending  = servicePendingIntent(ACTION_REPEAT)

        val openIntent = Intent(this, PlayerActivityScreen::class.java)
        val openPending = PendingIntent.getActivity(this, 200, openIntent, PendingIntent.FLAG_IMMUTABLE)

        val isPlaying = _isPlaying.value
        val playPauseAction = if (isPlaying) {
            NotificationCompat.Action(R.drawable.notificationpausebutton, "Pause", pausePending)
        } else {
            NotificationCompat.Action(R.drawable.notificationplaybutton, "Play", playPending)
        }

        val isShuffle = _isShuffle.value
        val shuffleAction = if (isShuffle) {
            NotificationCompat.Action(R.drawable.notificationshufflebutton, "shuffle", shufflePending)
        } else {
            NotificationCompat.Action(R.drawable.disableshuffle, "disableShuffle", shufflePending)
        }

        val repeatAction = when (_repeatMode.value) {
            Player.REPEAT_MODE_OFF ->
                NotificationCompat.Action(
                    R.drawable.disablerepeat,
                    "Repeat Off",
                    repeatPending
                )

            Player.REPEAT_MODE_ALL ->
                NotificationCompat.Action(
                    R.drawable.notificationrepeatbutton,
                    "Repeat All",
                    repeatPending
                )

            Player.REPEAT_MODE_ONE ->
                NotificationCompat.Action(
                    R.drawable.notificationrepeatonebutton,
                    "Repeat One",
                    repeatPending
                )

            else -> NotificationCompat.Action(
                R.drawable.disablerepeat,
                "Repeat Off",
                repeatPending
            )
        }

        val songName = Html.fromHtml(song.name,Html.FROM_HTML_MODE_LEGACY)
        val artistsName = song.artist
            .takeIf { it.isNotEmpty() }     // only proceed if list not empty
            ?.joinToString(", ") { it.name } // join all artist names
            ?: "Unknown Artist"
        val artistsNameList = Html.fromHtml(artistsName.ifEmpty { "Unknown Artist" },Html.FROM_HTML_MODE_LEGACY)

        val notifBuilder = NotificationCompat.Builder(this, MUSIC_CHANNEL_ID)
            .setContentTitle(songName)
            .setContentText(artistsNameList)
            .setSmallIcon(R.drawable.headset_icon)
            .setLargeIcon(bitmap)
            .setContentIntent(openPending)
            .setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(shuffleAction)
            .addAction(NotificationCompat.Action(R.drawable.notificationprevbutton, "Prev", prevPending))
            .addAction(playPauseAction)
            .addAction(NotificationCompat.Action(R.drawable.notificationnextbutton, "Next", nextPending))
            .addAction(repeatAction)
            .setStyle(MediaStyleNotificationHelper.MediaStyle(mediaSession)
                .setShowActionsInCompactView(0, 1, 2, 3, 4))

        return notifBuilder.build()
    }


    private fun updateMetadata(song: SongItem, bitmap: Bitmap) {

        val songName = Html.fromHtml(
            song.name,
            Html.FROM_HTML_MODE_LEGACY
        ).toString()

        val artistsName = song.artist
            .takeIf { it.isNotEmpty() }
            ?.joinToString(", ") { it.name }
            ?: "Unknown Artist"

        val metadata = MediaMetadata.Builder()
            .setTitle(songName)
            .setArtist(artistsName)
            .setArtworkData(
                bitmapToByteArray(bitmap),
                MediaMetadata.PICTURE_TYPE_FRONT_COVER
            )
            .build()

        val currentItem = player.currentMediaItem ?: return

        val updatedItem = currentItem.buildUpon()
            .setMediaMetadata(metadata)
            .build()

        player.replaceMediaItem(
            player.currentMediaItemIndex,
            updatedItem
        )
    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()

        bitmap.compress(
            Bitmap.CompressFormat.JPEG,
            90,
            stream
        )

        return stream.toByteArray()
    }

    private fun updatePlaybackInfo() {
        val song = _currentSong.value ?: return
        if (!::player.isInitialized) return

        val duration = player.duration.coerceAtLeast(song.duration.toLong())
        val position = player.currentPosition

        if (!isHistorySaved) {
            val playedPercentage = (position * 100) / duration

            if (
                position > 10000 &&
                playedPercentage >= 30
            ) {
                isHistorySaved = true

                historyRepository.savePlayedSong(
                    PlayedSong(
                        id = song.id,
                        name = htmlToText(song.name),
                        artist = song.artist,
                        album = song.album,
                        image = song.image,
                        duration = song.duration,
                        downloadUrl = song.downloadUrl,
                        songSource = song.songSource
                    )
                )
            }
        }

        val buffered = player.bufferedPosition

        _duration.value = duration.toInt()
        _progress.value = position.toInt()
        _buffer.value = buffered.toInt()
    }

    private fun servicePendingIntent(action: String): PendingIntent {
        val intent = Intent(this, MusicPlayerService::class.java).apply {
            this.action = action
        }

        return PendingIntent.getService(
            this,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private suspend fun loadAlbumArt(song: SongItem): Bitmap {
        artCache.get(song.id)?.let {
            return it
        }

        val placeholder = placeholderBitmap

        return try {
            val imageUrl = optimizeImage(
                song.image
                    .lastOrNull { it.url.isNotBlank() }
                    ?.url
                    .orEmpty()
            )

            Log.d("IMAGE_URL", imageUrl)

            if (imageUrl.isBlank()) {
                return placeholder
            }

            val request = ImageRequest.Builder(this)
                .data(imageUrl)
                .allowHardware(false)
                .memoryCachePolicy(CachePolicy.DISABLED)
                .diskCachePolicy(CachePolicy.DISABLED)
                .crossfade(false)
                .build()

            val bitmap = when (val result = imageLoader.execute(request)) {
                is SuccessResult -> {
                    when (val drawable = result.drawable) {
                        is BitmapDrawable -> drawable.bitmap
                        else -> drawable.toBitmap()
                    }
                }

                else -> placeholder
            }

            val rounded = bitmap.roundCorners(32f)

            artCache.put(song.id, rounded)

            rounded
        } catch (e: Exception) {
            Log.e("AlbumArt", "Load failed for ${song.name}", e)
            placeholder
        }
    }

    private fun Bitmap.roundCorners(radius: Float): Bitmap {
        val output = createBitmap(width, height)

        val canvas = Canvas(output)

        val paint = Paint().apply {
            isAntiAlias = true
        }

        val rect = RectF(
            0f,
            0f,
            width.toFloat(),
            height.toFloat()
        )

        canvas.drawRoundRect(rect, radius, radius, paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

        canvas.drawBitmap(this, 0f, 0f, paint)

        return output
    }

    private fun optimizeImage(url: String): String {
        return if (url.contains("i.ytimg.com/vi/")) {
            val videoId = url
                .substringAfter("/vi/")
                .substringBefore("/")

            "https://i.ytimg.com/vi/$videoId/maxresdefault.jpg"
        } else {
            url
                .replace(ytMusicRegex, "w520-h520")
                .replace(normalRegex, "500x500")
        }
    }

//    suspend fun getFreshAudioUrl(videoId: String): String {
//        return try {
//            val link = "https://www.youtube.com/watch?v=$videoId"
//
//            val extractor =
//                ServiceList.YouTube
//                    .getStreamExtractor(link)
//
//            extractor.fetchPage()
//
//            extractor.audioStreams
//                .filter { it.averageBitrate > 0 }
//                .maxByOrNull { it.averageBitrate }
//                ?.content ?: ""
//
//        } catch (e: Exception) {
//            Log.e("STREAM_ERROR",Log.getStackTraceString(e))
//
//            ""
//        }
//    }
}