package com.mustfa.mediaexplorer.media

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface PlaybackStatus {
    data object Idle : PlaybackStatus
    data object Buffering : PlaybackStatus
    data object Ready : PlaybackStatus
    data object Ended : PlaybackStatus
    data class Failed(val message: String) : PlaybackStatus
}

data class PlaybackState(
    val status: PlaybackStatus = PlaybackStatus.Idle,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val currentUri: Uri? = null,
    val queue: List<Uri> = emptyList()
)

class MediaEngine(context: Context, private val positions: PlaybackPositionStore = PlaybackPositionStore(context)) {
    private val player = ExoPlayer.Builder(context).build()
    val exoPlayer: ExoPlayer get() = player
    private val _state = MutableStateFlow(PlaybackState())
    val state: StateFlow<PlaybackState> = _state.asStateFlow()

    init {
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                val status = when (playbackState) {
                    Player.STATE_BUFFERING -> PlaybackStatus.Buffering
                    Player.STATE_READY -> PlaybackStatus.Ready
                    Player.STATE_ENDED -> PlaybackStatus.Ended
                    else -> PlaybackStatus.Idle
                }
                publish(status = status)
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) { publish(isPlaying = isPlaying) }
            override fun onPlayerError(error: PlaybackException) { publish(status = PlaybackStatus.Failed(error.localizedMessage ?: "Unable to play media")) }
        })
    }

    fun setQueue(uris: List<Uri>, startIndex: Int = 0) {
        if (uris.isEmpty()) { player.clearMediaItems(); publish(queue = emptyList(), currentUri = null); return }
        player.setMediaItems(uris.map(MediaItem::fromUri), startIndex, positions.read(uris[startIndex].toString()))
        publish(queue = uris, currentUri = uris[startIndex], positionMs = positions.read(uris[startIndex].toString()))
        player.prepare()
    }

    fun play() { player.play() }
    fun pause() { player.pause(); persistPosition() }
    fun seekTo(positionMs: Long) { player.seekTo(positionMs); publish(positionMs = positionMs) }
    fun skipNext() { player.seekToNextMediaItem() }
    fun skipPrevious() { player.seekToPreviousMediaItem() }
    fun setPlaybackSpeed(speed: Float) { player.setPlaybackSpeed(speed.coerceIn(0.25f, 3f)) }
    fun updatePosition() { publish(positionMs = player.currentPosition, durationMs = player.duration.coerceAtLeast(0L), currentUri = player.currentMediaItem?.localConfiguration?.uri) }
    fun release() { persistPosition(); player.release() }

    private fun persistPosition() { player.currentMediaItem?.localConfiguration?.uri?.let { positions.save(it.toString(), player.currentPosition) } }

    private fun publish(
        status: PlaybackStatus = _state.value.status,
        isPlaying: Boolean = player.isPlaying,
        positionMs: Long = player.currentPosition,
        durationMs: Long = player.duration.coerceAtLeast(0L),
        currentUri: Uri? = player.currentMediaItem?.localConfiguration?.uri ?: _state.value.currentUri,
        queue: List<Uri> = _state.value.queue
    ) { _state.value = PlaybackState(status, isPlaying, positionMs, durationMs, currentUri, queue) }
}
