package com.mustfa.mediaexplorer.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mustfa.mediaexplorer.media.MediaEngine

@Composable
fun MustfaApp(viewModel: MustfaViewModel, mediaEngine: MediaEngine) {
    Surface(modifier = Modifier.fillMaxSize()) { HomeScreen(viewModel, mediaEngine) }
}
