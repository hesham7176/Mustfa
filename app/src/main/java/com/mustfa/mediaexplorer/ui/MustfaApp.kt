package com.mustfa.mediaexplorer.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun MustfaApp(viewModel: MustfaViewModel) {
    Surface(modifier = Modifier.fillMaxSize()) { HomeScreen(viewModel) }
}
