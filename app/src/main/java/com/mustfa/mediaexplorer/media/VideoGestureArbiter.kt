package com.mustfa.mediaexplorer.media

import com.mustfa.mediaexplorer.domain.GestureAction
import com.mustfa.mediaexplorer.domain.GestureDecision
import com.mustfa.mediaexplorer.domain.GestureInput
import kotlin.math.abs

sealed interface VideoGesture {
    data object None : VideoGesture
    data class Seek(val deltaMs: Long) : VideoGesture
    data class Brightness(val delta: Float) : VideoGesture
    data class Volume(val delta: Float) : VideoGesture
    data class DoubleTapSeek(val deltaMs: Long) : VideoGesture
}

class VideoGestureArbiter(
    private val decision: GestureDecision = GestureDecision(),
    private val seekScaleMsPerPixel: Long = 7_000L,
    private val doubleTapSeekMs: Long = 10_000L
) {
    fun drag(deltaX: Float, deltaY: Float, distance: Float, horizontalPosition: Float): VideoGesture = when (decision.decide(GestureInput(deltaX, deltaY, distance, horizontalPosition))) {
        GestureAction.SEEK -> VideoGesture.Seek((deltaX * seekScaleMsPerPixel / 100f).toLong())
        GestureAction.BRIGHTNESS -> VideoGesture.Brightness((-deltaY / 500f).coerceIn(-1f, 1f))
        GestureAction.VOLUME -> VideoGesture.Volume((-deltaY / 500f).coerceIn(-1f, 1f))
        else -> VideoGesture.None
    }

    fun doubleTap(horizontalPosition: Float): VideoGesture = VideoGesture.DoubleTapSeek(if (horizontalPosition < .5f) -doubleTapSeekMs else doubleTapSeekMs)

    fun isHorizontal(deltaX: Float, deltaY: Float): Boolean = abs(deltaX) > abs(deltaY)
}
