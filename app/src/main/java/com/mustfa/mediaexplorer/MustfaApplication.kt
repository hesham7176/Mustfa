package com.mustfa.mediaexplorer

import android.app.Application
import com.mustfa.mediaexplorer.data.FileRepository

class MustfaApplication : Application() {
    val fileRepository by lazy { FileRepository(applicationContext) }
}
