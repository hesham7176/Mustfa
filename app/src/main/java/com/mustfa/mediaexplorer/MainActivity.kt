package com.mustfa.mediaexplorer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mustfa.mediaexplorer.ui.MustfaApp
import com.mustfa.mediaexplorer.ui.MustfaTheme
import com.mustfa.mediaexplorer.ui.MustfaViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = (application as MustfaApplication).fileRepository
        setContent {
            MustfaTheme { MustfaApp(viewModel(factory = MustfaViewModel.factory(repository))) }
        }
    }
}
