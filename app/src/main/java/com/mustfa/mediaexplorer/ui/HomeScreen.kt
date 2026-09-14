package com.mustfa.mediaexplorer.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.mustfa.mediaexplorer.media.MediaEngine

@Composable
fun HomeScreen(viewModel: MustfaViewModel, mediaEngine: MediaEngine) {
    val isHome by viewModel.isHome.collectAsState()
    val playerVisible by viewModel.playerVisible.collectAsState()
    when {
        playerVisible -> MediaPlayerScreen(mediaEngine, viewModel::closePlayer)
        isHome -> DashboardScreen(viewModel)
        else -> FileBrowserScreen(viewModel)
    }
}
