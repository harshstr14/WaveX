package com.example.wavex.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.text.Html
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleService
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Player.Listener
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import coil.ImageLoader
import coil.request.ImageRequest
import com.example.wavex.R
import com.example.wavex.homeScreen.PlayerManager
import com.example.wavex.homeScreen.RecentlyPlayedManager
import com.example.wavex.homeScreen.SongItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class  MusicPlayerService : LifecycleService() {
    companion object {
        const val CHANNEL_ID = "music_player_channel"
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
    }

    inner class LocalBinder : Binder() {
        fun getService(): MusicPlayerService = this@MusicPlayerService
    }

    private val binder = LocalBinder()
    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaSessionCompat
    private val handler = Handler(Looper.getMainLooper())
    private val progressRefreshMs = 100L
    private var currentAlbumArt: Bitmap? = null
    private var currentArtSongId: String? = null
    var qualityIndex = 4
    private lateinit var qualityRef: DatabaseReference
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val imageLoader by lazy { ImageLoader(this) }

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

    private val shuffleOrder: MutableList<Int> = mutableListOf()
    private var shufflePointer = 0
    private var isQueueOnlyMode = false
    private var queuePointer = 0
    private var queueShuffleOrder: MutableList<Int> = mutableListOf()
    private var queueShufflePointer = 0

    override fun onCreate() {
        super.onCreate()

        ServiceLocator.musicService = this
        createNotificationChannel()

        val userId = FirebaseAuth.getInstance().currentUser?.uid

        qualityRef = FirebaseDatabase.getInstance()
            .getReference("Users")
            .child(userId.toString())
            .child("streamingQuality")

        listenForQualityChanges()

        val placeholderNotification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("WaveX is starting…")
            .setContentText("Preparing your music")
            .setSmallIcon(R.drawable.headset_icon)
            .build()
        startForeground(NOTIFICATION_ID, placeholderNotification)

        initPlayer()
        initMediaSession()

        serviceScope.launch {
            _currentSong
                .filterNotNull()
                .distinctUntilChangedBy { it.id }
                .collect { song ->
                    RecentlyPlayedManager.add(this@MusicPlayerService, song)
                }
        }
    }

    @OptIn(UnstableApi::class)
    private fun initPlayer() {
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                30000,  // minBufferMs: total buffer duration before rebuffering (30s)
                60000,  // maxBufferMs: total buffer size (60s)
                1500,   // bufferForPlaybackMs: how much to buffer before starting playback (1.5s)
                3000    // bufferForPlaybackAfterRebufferMs: after buffering again (3s)
            )
            .build()
        player = ExoPlayer.Builder(this)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .setLoadControl(loadControl)
            .build()
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()
        player.setAudioAttributes(audioAttributes, true)
        player.setHandleAudioBecomingNoisy(true)

        player.addListener(object : Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY && currentIndex in playlist.indices) {
                    val song = playlist[currentIndex]
                    val duration = player.duration.coerceAtLeast(song.duration.toLong()) // Use actual player duration if available

                    _duration.value = duration.toInt()
                    _buffer.value = player.bufferedPosition.toInt()

                    val bitmap = BitmapFactory.decodeResource(resources, R.drawable.playlist)
                    updateMetadata(song, bitmap)
                    CoroutineScope(Dispatchers.Main).launch {
                        updateNotification()
                    }
                }
                else if (state == Player.STATE_ENDED) {
                    if (!isQueueOnlyMode) {
                        currentQueuedSong?.let { queuedSong ->
                            queue.removeAll { it.id == queuedSong.id }
                            _queue.value = queue.toList()
                            currentQueuedSong = null

                            updateUpNext()
                        }
                    }

                    next()
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
                if (isPlaying) handler.post(progressRunnable) else handler.removeCallbacks(progressRunnable)
                updatePlaybackState()
                CoroutineScope(Dispatchers.Main).launch {
                    updateNotification()
                }
            }

            override fun onIsLoadingChanged(isLoading: Boolean) {
                super.onIsLoadingChanged(isLoading)
                _buffer.value = player.bufferedPosition.toInt()
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.e("MusicPlayerService", "Player error: ${error.message}")
            }
        })
    }

    private fun listenForQualityChanges() {
        qualityRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val quality = snapshot.getValue(String::class.java) ?: return

                val index = when (quality) {
                    "Low" -> 2
                    "Normal" -> 3
                    "High" -> 4
                    else -> 4
                }

                if (qualityIndex != index) {
                    setQuality(index)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MusicService", "Quality listener cancelled: ${error.message}")
            }
        })
    }

    private fun initMediaSession() {
        mediaSession = MediaSessionCompat(this, "MusicPlayerService")
        mediaSession.setCallback(object : MediaSessionCompat.Callback() {
            override fun onPlay() { resume() }
            override fun onPause() { togglePlayPause() }
            override fun onSkipToNext() { next() }
            override fun onSkipToPrevious() { previous() }
            override fun onStop() { stopServiceAndNotification() }
            override fun onSeekTo(pos: Long) { seekTo(pos) }
        })
        mediaSession.isActive = true
        updatePlaybackState()
    }

    private fun updatePlaybackState() {
        val stateBuilder = PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_PLAY_PAUSE or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                        PlaybackStateCompat.ACTION_STOP or
                        PlaybackStateCompat.ACTION_SEEK_TO
            )
        val state = if (::player.isInitialized && player.isPlaying) {
            PlaybackStateCompat.STATE_PLAYING
        } else {
            PlaybackStateCompat.STATE_PAUSED
        }
        stateBuilder.setState(state, player.currentPosition, 1.0f)
        mediaSession.setPlaybackState(stateBuilder.build())
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
                    val playlist = PlayerManager.currentPlaylist

                    if (playlist.isNotEmpty() && index in playlist.indices) {
                        setPlaylist(playlist, index)
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
            }
        }

        if (!::player.isInitialized) {
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("WaveX is running…")
                .setContentText("Preparing your music")
                .setSmallIcon(R.drawable.headset_icon)
                .build()
            startForeground(NOTIFICATION_ID, notification)
        }

        return START_STICKY
    }

    fun setPlaylist(songs: List<SongItem>?, startAtIndex: Int = 0) {
        shuffleOrder.clear()
        shufflePointer = 0
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

        currentIndex = index
        _currentIndexFlow.value = index

        if (isShuffle.value) {
            val pointer = shuffleOrder.indexOf(index)
            if (pointer != -1) shufflePointer = pointer
        }

        val song = playlist[index]
        _currentSong.value = song

        updateUpNext()
        prepareAndPlay(song)
    }

    private fun prepareAndPlay(song: SongItem) {
        currentAlbumArt = null
        currentArtSongId = null

        player.stop()
        player.clearMediaItems()
        val mediaItem = MediaItem.fromUri(song.downloadUrl[qualityIndex].url.toUri())
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()

        startForegroundWithNotification(song)
        updatePlaybackState()
    }

    fun getCurrentPosition(): Int {
        return player.currentPosition.toInt()
    }

    fun getDuration(): Int {
        return player.duration.toInt()
    }

    fun togglePlayPause() {
        if (::player.isInitialized) {
            player.playWhenReady = !player.playWhenReady
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

        if (player.repeatMode == Player.REPEAT_MODE_ONE) {
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
                    if (queueShuffleOrder.isEmpty()) {
                        regenerateQueueShuffle()
                    }

                    if (_currentSong.value != null) {
                        queueShufflePointer++
                    }

                    if (queueShufflePointer >= queueShuffleOrder.size) {
                        if (player.repeatMode == Player.REPEAT_MODE_ALL) {
                            regenerateQueueShuffle()
                        } else {
                            return
                        }
                    }

                    queue[queueShuffleOrder[queueShufflePointer]]
                } else {
                    if (_currentSong.value != null) {
                        queuePointer++
                    }

                    if (queuePointer >= queue.size) {
                        if (player.repeatMode == Player.REPEAT_MODE_ALL) {
                            queuePointer = 0
                        } else {
                            return
                        }
                    }

                    queue[queuePointer]
                }

                _currentSong.value = nextSong
                prepareAndPlay(nextSong)

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
            }

            return
        }

        if (playlist.isEmpty()) return

        if (isShuffle.value) {
            if (shuffleOrder.isEmpty()) {
                regenerateShuffleKeepingCurrent()
            }

            shufflePointer++

            if (shufflePointer >= shuffleOrder.size) {
                if (player.repeatMode == Player.REPEAT_MODE_ALL) {
                    shufflePointer = 0
                } else {
                    updateUpNext()
                    return
                }
            }

            playIndex(shuffleOrder[shufflePointer])
        } else {
            val nextIndex = currentIndex + 1

            if (nextIndex in playlist.indices) {
                playIndex(nextIndex)
            } else {
                if (player.repeatMode == Player.REPEAT_MODE_ALL) {
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

        if (player.repeatMode == Player.REPEAT_MODE_ONE) {
            player.seekTo(0)
            player.play()
            return
        }

        if (isQueueOnlyMode && queue.isNotEmpty()) {
            if (isShuffle.value) {
                if (queueShuffleOrder.isEmpty()) {
                    regenerateQueueShuffle()
                }

                queueShufflePointer--

                if (queueShufflePointer < 0) {
                    if (player.repeatMode == Player.REPEAT_MODE_ALL) {
                        queueShufflePointer = queueShuffleOrder.lastIndex
                    } else {
                        player.seekTo(0)
                        return
                    }
                }

                val prevIndex = queueShuffleOrder[queueShufflePointer]
                val prevSong = queue[prevIndex]

                _currentSong.value = prevSong
                prepareAndPlay(prevSong)
            } else {
                queuePointer--

                if (queuePointer < 0) {
                    if (player.repeatMode == Player.REPEAT_MODE_ALL) {
                        queuePointer = queue.lastIndex
                    } else {
                        player.seekTo(0)
                        return
                    }
                }

                val prevSong = queue[queuePointer]

                _currentSong.value = prevSong
                prepareAndPlay(prevSong)
            }

            return
        }

        if (playlist.isEmpty()) return

        if (history.isNotEmpty()) {
            val previousSong = history.removeAt(history.lastIndex)

            val index = playlist.indexOfFirst { it.id == previousSong.id }
            if (index != -1) {
                playIndex(index)
            }
            return
        }

        if (isShuffle.value) {
            if (shuffleOrder.isEmpty()) {
                regenerateShuffleKeepingCurrent()
            }

            shufflePointer--

            if (shufflePointer < 0) {
                if (player.repeatMode == Player.REPEAT_MODE_ALL) {
                    shufflePointer = shuffleOrder.lastIndex
                } else {
                    player.seekTo(0)
                    return
                }
            }

            playIndex(shuffleOrder[shufflePointer])
        } else {
            val prevIndex = currentIndex - 1

            if (prevIndex >= 0) {
                playIndex(prevIndex)

            } else {
                if (player.repeatMode == Player.REPEAT_MODE_ALL) {
                    playIndex(playlist.lastIndex)
                } else {
                    player.seekTo(0)
                }
            }
        }
    }

    fun shuffleToggle() {
        val newState = !_isShuffle.value
        _isShuffle.value = newState

        if (newState) {
            if (isQueueOnlyMode) {
                regenerateQueueShuffle()

                val current = _currentSong.value
                val index = queue.indexOfFirst { it.id == current?.id }
                if (index != -1) {
                    queueShufflePointer =
                        queueShuffleOrder.indexOf(index)
                }

            } else {
                regenerateShuffleKeepingCurrent()
            }
        } else {
            if (isQueueOnlyMode) {
                val current = _currentSong.value
                val index = queue.indexOfFirst { it.id == current?.id }
                if (index != -1) {
                    queuePointer = index
                }

                queueShuffleOrder.clear()
            } else {
                val current = _currentSong.value
                val index = playlist.indexOfFirst { it.id == current?.id }
                if (index != -1) {
                    currentIndex = index
                }

                shuffleOrder.clear()
            }
        }

        updateUpNext()
        updateNotification()
    }

    private fun regenerateShuffleKeepingCurrent() {
        if (playlist.isEmpty() || currentIndex !in playlist.indices) return

        shuffleOrder.clear()

        val current = currentIndex

        val shuffled = playlist.indices
            .filter { it != current }
            .shuffled()

        shuffleOrder.add(current)
        shuffleOrder.addAll(shuffled)

        shufflePointer = 0
    }

    private fun regenerateQueueShuffle() {
        if (queue.isEmpty()) return

        val current = _currentSong.value
        val currentIndexInQueue = queue.indexOfFirst { it.id == current?.id }

        queueShuffleOrder.clear()

        if (currentIndexInQueue == -1) {
            queueShuffleOrder.addAll(queue.indices.shuffled())
            queueShufflePointer = 0
            return
        }

        val shuffled = queue.indices
            .filter { it != currentIndexInQueue }
            .shuffled()

        queueShuffleOrder.add(currentIndexInQueue)
        queueShuffleOrder.addAll(shuffled)

        queueShufflePointer = 0
    }

    fun repeatToggle() {
        val newMode = when (player.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_OFF
            else -> Player.REPEAT_MODE_OFF
        }

        player.repeatMode = newMode
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

    fun setQuality(index: Int) {
        val currentSong = _currentSong.value ?: return
        val currentPosition = player.currentPosition
        val wasPlaying = player.isPlaying

        qualityIndex = index

        player.stop()
        player.clearMediaItems()

        val mediaItem = MediaItem.fromUri(
            currentSong.downloadUrl[qualityIndex].url.toUri()
        )

        player.setMediaItem(mediaItem)
        player.prepare()
        player.seekTo(currentPosition)

        if (wasPlaying) player.play()
    }

    fun addToQueue(song: SongItem) {
        if (queue.any { it.id == song.id }) return

        queue.add(song)
        _queue.value = queue.toList()

        if (playlist.isEmpty() && _currentSong.value == null) {
            queuePointer = 0
            isQueueOnlyMode = true
            next()
        }

        updateUpNext()
    }

    fun isInQueue(songId: String): Boolean {
        return queue.any { it.id == songId }
    }

    fun playNext(song: SongItem) {
        if (!queue.any { it.id == song.id }) {
            queue.add(0, song)
            _queue.value = queue.toList()
        }

        updateUpNext()
    }

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
        song?.let { song ->
            history.removeAll { it.id == song.id }
            history.add(song)
        }
    }

//    fun insertNext(song: SongItem) {
//        if (currentIndex !in playlist.indices) return
//
//        val oldIndex = playlist.indexOfFirst { it.id == song.id }
//
//        if (oldIndex != -1 && oldIndex < currentIndex) {
//            currentIndex--
//        }
//
//        playlist.removeAll { it.id == song.id }
//
//        val insertIndex = (currentIndex + 1).coerceAtMost(playlist.size)
//
//        playlist.add(insertIndex, song)
//
//        _playlistFlow.value = playlist.toList()
//        _currentIndexFlow.value = currentIndex
//
//        updateUpNext()
//
//        if (isShuffle.value) regenerateShuffleKeepingCurrent()
//    }
//
//    fun moveSongToNext(song: SongItem) {
//        val oldIndex = playlist.indexOfFirst { it.id == song.id }
//
//        if (oldIndex != -1 && oldIndex < currentIndex) {
//            currentIndex--
//        }
//
//        playlist.removeAll { it.id == song.id }
//
//        val insertIndex = (currentIndex + 1).coerceAtMost(playlist.size)
//
//        playlist.add(insertIndex, song)
//
//        _playlistFlow.value = playlist.toList()
//        _currentIndexFlow.value = currentIndex
//
//        updateUpNext()
//
//        if (isShuffle.value) regenerateShuffleKeepingCurrent()
//    }
//
//    fun removeFromPlaylistAt(index: Int) {
//        if (index !in playlist.indices) return
//
//        if (index < currentIndex) {
//            currentIndex--
//        }
//
//        if (index == currentIndex) {
//            val hasNext = currentIndex < playlist.lastIndex
//
//            playlist.removeAt(index)
//
//            if (playlist.isEmpty()) {
//                player.stop()
//                currentIndex = -1
//                _currentSong.value = null
//            } else {
//                currentIndex = if (hasNext) currentIndex else playlist.lastIndex
//                playIndex(currentIndex)
//            }
//
//            _playlistFlow.value = playlist.toList()
//            return
//        }
//
//        playlist.removeAt(index)
//
//        _playlistFlow.value = playlist.toList()
//        _currentIndexFlow.value = currentIndex
//
//        updateUpNext()
//
//        if (isShuffle.value) regenerateShuffleKeepingCurrent()
//    }

    private fun stopServiceAndNotification() {
        try {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        } catch (t: Throwable) { /* ignore */ }
    }

    override fun onDestroy() {
        Log.w("MusicPlayerService", "Service destroyed by system")
        handler.removeCallbacks(progressRunnable)
        if (::player.isInitialized) player.release()
        if (::mediaSession.isInitialized) mediaSession.release()
        try { stopForeground(STOP_FOREGROUND_REMOVE) } catch (e: Exception) {}
        serviceScope.cancel()
        ServiceLocator.musicService = null
        super.onDestroy()
    }

    private val placeholderBitmap by lazy {
        BitmapFactory.decodeResource(resources, R.drawable.playlist)
    }

    private fun startForegroundWithNotification(song: SongItem) {
        val placeholder = placeholderBitmap
        val notif = buildNotification(song, placeholder)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startForeground(NOTIFICATION_ID, notif)
        }

        if (song.id == currentArtSongId && currentAlbumArt != null) {
            val notif = buildNotification(song, currentAlbumArt!!)
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notif)
            }
            return
        }

        // 2️⃣ Load real album art in background
        serviceScope.launch(Dispatchers.IO) {
            val bitmap = try {
                if (song.image.size > 2 && song.image[2].url.isNotBlank()) {

                    val request = ImageRequest.Builder(this@MusicPlayerService)
                        .data(song.image[2].url)
                        .size(512, 512)
                        .allowHardware(false)   // REQUIRED to get Bitmap
                        .build()

                    val result = imageLoader.execute(request)

                    (result.drawable as BitmapDrawable).bitmap
                } else {
                    placeholder
                }
            } catch (e: Exception) {
                Log.e("AlbumArt", "Error loading album art", e)
                placeholder
            }

            // 🔁 Back to main thread
            withContext(Dispatchers.Main) {
                currentAlbumArt = bitmap
                currentArtSongId = song.id

                updateMetadata(song, bitmap)
                val updatedNotif = buildNotification(song, bitmap)

                val notificationManager =
                    getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(NOTIFICATION_ID, updatedNotif)
            }
        }
    }

    fun updateNotification() {
        val song = _currentSong.value ?: return

        if (song.id == currentArtSongId && currentAlbumArt != null) {
            val notif = buildNotification(song, currentAlbumArt!!)
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                NotificationManagerCompat.from(this)
                    .notify(NOTIFICATION_ID, notif)
            }
            return
        }

        serviceScope.launch(Dispatchers.IO) {

            val placeholder = placeholderBitmap

            val bitmap = try {
                if (song.image.size > 2 && song.image[2].url.isNotBlank()) {

                    val request = ImageRequest.Builder(this@MusicPlayerService)
                        .data(song.image[2].url)
                        .size(512)
                        .allowHardware(false) // REQUIRED for notifications
                        .build()

                    val result = imageLoader.execute(request)
                    val drawable = result.drawable

                    if (drawable is BitmapDrawable) drawable.bitmap else placeholder
                } else {
                    placeholder
                }
            } catch (e: Exception) {
                Log.e("AlbumArt", "Error loading image", e)
                placeholder
            }

            withContext(Dispatchers.Main) {
                currentAlbumArt = bitmap
                currentArtSongId = song.id

                updateMetadata(song, bitmap)

                val notif = buildNotification(song, bitmap)
                if (ActivityCompat.checkSelfPermission(
                        this@MusicPlayerService,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    NotificationManagerCompat.from(this@MusicPlayerService)
                        .notify(NOTIFICATION_ID, notif)
                } else {
                    Log.w(
                        "MusicPlayerService",
                        "Notification permission denied, cannot show notification"
                    )
                }
            }
        }
    }

    private fun buildNotification(song: SongItem, bitmap: Bitmap): Notification {
//        val playIntent = Intent(this, NotificationActionReceiver::class.java).setAction(ACTION_PLAY)
//        val pauseIntent = Intent(this, NotificationActionReceiver::class.java).setAction(ACTION_PAUSE)
//        val nextIntent = Intent(this, NotificationActionReceiver::class.java).setAction(ACTION_NEXT)
//        val prevIntent = Intent(this, NotificationActionReceiver::class.java).setAction(ACTION_PREV)
//        val shuffleIntent = Intent(this, NotificationActionReceiver::class.java).setAction(ACTION_SHUFFLE)
//        val repeatIntent = Intent(this, NotificationActionReceiver::class.java).setAction(ACTION_REPEAT)

        val playPending    = servicePendingIntent(ACTION_PLAY)
        val pausePending   = servicePendingIntent(ACTION_PAUSE)
        val nextPending    = servicePendingIntent(ACTION_NEXT)
        val prevPending    = servicePendingIntent(ACTION_PREV)
        val shufflePending = servicePendingIntent(ACTION_SHUFFLE)
        val repeatPending  = servicePendingIntent(ACTION_REPEAT)

        //val openIntent = Intent(this, PlaySong::class.java)
        //val openPending = PendingIntent.getActivity(this, 200, openIntent, PendingIntent.FLAG_IMMUTABLE)

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

        val repeatAction = when (player.repeatMode) {
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
        updateMetadata(song, bitmap)

        val playbackState = PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                PlaybackStateCompat.ACTION_PAUSE or
                PlaybackStateCompat.ACTION_PLAY_PAUSE or
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                PlaybackStateCompat.ACTION_SEEK_TO
            )
            .setState(
                if (isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
                getCurrentPosition().toLong(),
                1f)
            .build()

        mediaSession.setPlaybackState(playbackState)

        val notifBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(songName)
            .setContentText(artistsNameList)
            .setSmallIcon(R.drawable.headset_icon)
            .setLargeIcon(bitmap)
            //.setContentIntent(openPending)
            .setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(shuffleAction)
            .addAction(NotificationCompat.Action(R.drawable.notificationprevbutton, "Prev", prevPending))
            .addAction(playPauseAction)
            .addAction(NotificationCompat.Action(R.drawable.notificationnextbutton, "Next", nextPending))
            .addAction(repeatAction)
            .setStyle(MediaStyle().setMediaSession(mediaSession.sessionToken).setShowActionsInCompactView(0, 1, 2, 3, 4))

        return notifBuilder.build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Music Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Music player notifications"
            }

            val manager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun updateMetadata(song: SongItem, bitmap: Bitmap) {
        val duration = if (::player.isInitialized && player.duration > 0) player.duration else song.duration.toLong()
        val songName = Html.fromHtml(song.name,Html.FROM_HTML_MODE_LEGACY)
        val artistsName = song.artist
            .takeIf { it.isNotEmpty() }     // only proceed if list not empty
            ?.joinToString(", ") { it.name } // join all artist names
            ?: "Unknown Artist"
        val artistsNameList = Html.fromHtml(artistsName.ifEmpty { "Unknown Artist" },Html.FROM_HTML_MODE_LEGACY)

        mediaSession.setMetadata(
            android.support.v4.media.MediaMetadataCompat.Builder()
                .putString(android.support.v4.media.MediaMetadataCompat.METADATA_KEY_TITLE, songName.toString())
                .putString(android.support.v4.media.MediaMetadataCompat.METADATA_KEY_ARTIST, artistsNameList.toString())
                .putBitmap(android.support.v4.media.MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)
                .putLong(android.support.v4.media.MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                .build()
        )
    }

    private fun updatePlaybackInfo() {
        val song = _currentSong.value ?: return
        if (!::player.isInitialized) return

        val duration = player.duration.coerceAtLeast(song.duration.toLong())
        val position = player.currentPosition
        val buffered = player.bufferedPosition

        // Update LiveData
        _duration.value = duration.toInt()
        _progress.value = position.toInt()
        _buffer.value = buffered.toInt()

        // Update playback state
        val playbackState = PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_PLAY_PAUSE or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                        PlaybackStateCompat.ACTION_SEEK_TO
            )
            .setState(
                if (player.isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
                position,
                1f
            )
            .build()
        mediaSession.setPlaybackState(playbackState)
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
}