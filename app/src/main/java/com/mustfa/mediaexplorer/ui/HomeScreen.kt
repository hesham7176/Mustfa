package com.mustfa.mediaexplorer.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun HomeScreen(viewModel: MustfaViewModel) {
    val isHome by viewModel.isHome.collectAsState()
    if (isHome) DashboardScreen(viewModel) else FileBrowserScreen(viewModel)
}
