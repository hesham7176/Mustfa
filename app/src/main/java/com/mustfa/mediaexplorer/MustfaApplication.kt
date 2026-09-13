package com.mustfa.mediaexplorer

import android.app.Application
import com.mustfa.mediaexplorer.data.FileRepository
import com.mustfa.mediaexplorer.media.MediaEngine

class MustfaApplication : Application() {
    val fileRepository by lazy { FileRepository(applicationContext) }
    val mediaEngine by lazy { MediaEngine(applicationContext) }
}
