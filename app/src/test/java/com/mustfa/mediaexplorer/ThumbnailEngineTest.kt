package com.mustfa.mediaexplorer

import com.mustfa.mediaexplorer.thumbnails.ThumbnailEngine
import org.junit.Assert.assertTrue
import org.junit.Test

class ThumbnailEngineTest {
    @Test fun classifiesSupportedMediaExtensions() {
        assertTrue("jpg" in ThumbnailEngine.IMAGE_EXTENSIONS)
        assertTrue("mp4" in ThumbnailEngine.VIDEO_EXTENSIONS)
        assertTrue("flac" in ThumbnailEngine.AUDIO_EXTENSIONS)
    }
}
