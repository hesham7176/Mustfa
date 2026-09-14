package com.mustfa.mediaexplorer

import com.mustfa.mediaexplorer.media.VideoGesture
import com.mustfa.mediaexplorer.media.VideoGestureArbiter
import org.junit.Assert.assertEquals
import org.junit.Test

class VideoGestureArbiterTest {
    private val arbiter = VideoGestureArbiter()

    @Test fun horizontalDragProducesSeekOnly() {
        assert(arbiter.drag(100f, 8f, 100f, .5f) is VideoGesture.Seek)
    }

    @Test fun edgeVerticalDragsControlSeparateProperties() {
        assert(arbiter.drag(3f, 100f, 100f, .05f) is VideoGesture.Brightness)
        assert(arbiter.drag(3f, 100f, 100f, .95f) is VideoGesture.Volume)
    }

    @Test fun doubleTapUsesSideForDirection() {
        assertEquals(-10_000L, (arbiter.doubleTap(.2f) as VideoGesture.DoubleTapSeek).deltaMs)
        assertEquals(10_000L, (arbiter.doubleTap(.8f) as VideoGesture.DoubleTapSeek).deltaMs)
    }
}
