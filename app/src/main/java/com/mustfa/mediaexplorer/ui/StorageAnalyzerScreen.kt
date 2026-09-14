package com.mustfa.mediaexplorer.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mustfa.mediaexplorer.R
import com.mustfa.mediaexplorer.analyzer.StorageAnalysis
import com.mustfa.mediaexplorer.analyzer.StorageAnalyzer
import java.io.File

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun StorageAnalyzerScreen(root: File, onBack: () -> Unit) {
    var analysis by remember(root) { mutableStateOf<StorageAnalysis?>(null) }
    var failed by remember(root) { mutableStateOf(false) }
    LaunchedEffect(root) { runCatching { StorageAnalyzer().analyze(root) }.onSuccess { analysis = it }.onFailure { failed = true } }
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.storage_analyzer)) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) } }) }) { padding ->
        when {
            failed -> Text(stringResource(R.string.error), modifier = Modifier.padding(padding).padding(16.dp), color = MaterialTheme.colorScheme.error)
            analysis == null -> Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) { Text(stringResource(R.string.loading)); LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = 12.dp)) }
            else -> LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(padding)) {
                item { Text(formatBytes(analysis!!.usedBytes) + " used", style = MaterialTheme.typography.titleLarge) }
                item { Text(stringResource(R.string.storage_categories), style = MaterialTheme.typography.titleMedium) }
                items(analysis!!.categories, key = { it.id }) { category -> Card(Modifier.fillMaxWidth()) { Text("${category.id}: ${formatBytes(category.bytes)} (${category.fileCount})", Modifier.padding(14.dp)) } }
                item { Text(stringResource(R.string.largest_files), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp)) }
                items(analysis!!.largestFiles, key = { it.absolutePath }) { file -> Text(file.absolutePath + "  " + formatBytes(file.length()), Modifier.padding(vertical = 6.dp)) }
            }
        }
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val units = arrayOf("KB", "MB", "GB", "TB")
    var value = bytes.toDouble()
    var index = -1
    while (value >= 1024 && index < units.lastIndex) { value /= 1024; index++ }
    return "%.1f %s".format(value, units[index])
}
