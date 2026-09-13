package com.mustfa.mediaexplorer.ui

import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.PlayerView
import com.mustfa.mediaexplorer.media.MediaEngine
import com.mustfa.mediaexplorer.media.PlaybackStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaPlayerScreen(mediaEngine: MediaEngine, onBack: () -> Unit) {
    val state by mediaEngine.state.collectAsState()
    val context = LocalContext.current
    DisposableEffect(Unit) {
        onDispose { mediaEngine.pause() }
    }
    Scaffold(topBar = {
        TopAppBar(title = { Text(state.currentUri?.lastPathSegment ?: "Media") }, navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
        })
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding), verticalArrangement = Arrangement.Center) {
            AndroidView(factory = {
                PlayerView(context).apply {
                    player = mediaEngine.exoPlayer
                    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                }
            }, modifier = Modifier.fillMaxWidth())
            val statusText = when (val status = state.status) {
                PlaybackStatus.Buffering -> "Buffering"
                PlaybackStatus.Ready -> "Ready"
                PlaybackStatus.Ended -> "Playback ended"
                is PlaybackStatus.Failed -> status.message
                PlaybackStatus.Idle -> "Preparing media"
            }
            Text(statusText, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(16.dp))
        }
    }
}
