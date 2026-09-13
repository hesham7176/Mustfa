package com.mustfa.mediaexplorer.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.mustfa.mediaexplorer.R
import com.mustfa.mediaexplorer.data.CategoryLocation
import com.mustfa.mediaexplorer.data.StorageLocation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: MustfaViewModel) {
    val locations by viewModel.locations.collectAsState()
    val safTreeUris by viewModel.safTreeUris.collectAsState()
    var selectedSafUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val safPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION) }
        viewModel.rememberSafTree(uri.toString())
    }
    if (selectedSafUri != null) {
        SafBrowserScreen(selectedSafUri!!, onBack = { selectedSafUri = null })
        return
    }
    Scaffold(topBar = {
        TopAppBar(
            title = { Column { Text(stringResource(R.string.home)); Text(stringResource(R.string.dashboard_subtitle), style = MaterialTheme.typography.labelSmall) } },
            actions = { IconButton(onClick = { viewModel.openLocation(locations.firstOrNull() ?: return@IconButton) }) { Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search)) } }
        )
    }) { padding ->
        LazyColumn(contentPadding = PaddingValues(bottom = 24.dp), modifier = Modifier.padding(padding), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Text(stringResource(R.string.storage_devices), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) }
            item { Card(onClick = { safPicker.launch(null) }, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) { Text(stringResource(R.string.add_removable_storage), modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.primary) } }
            if (locations.isEmpty()) item { Text(stringResource(R.string.no_storage), modifier = Modifier.padding(horizontal = 16.dp)) }
            items(locations, key = { it.id }) { location -> StorageLocationCard(location) { viewModel.openLocation(location) } }
            items(safTreeUris, key = { it }) { uri -> Card(onClick = { selectedSafUri = Uri.parse(uri) }, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) { Text(stringResource(R.string.removable_storage) + "\n" + uri, modifier = Modifier.padding(16.dp), maxLines = 2) } }
            item { Text(stringResource(R.string.categories), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) }
            locations.firstOrNull()?.let { location -> items(viewModel.categories(location), key = { it.id }) { category -> CategoryCard(category) { viewModel.openCategory(category) } } }
        }
    }
}

@Composable
private fun StorageLocationCard(location: StorageLocation, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(if (location.removable) Icons.Default.Storage else Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(32.dp))
                Column(Modifier.padding(start = 12.dp)) {
                    Text(if (location.removable) stringResource(R.string.removable_storage) else stringResource(R.string.internal_storage), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(location.root.absolutePath, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                }
            }
            Text(stringResource(R.string.free_of, formatBytes(location.freeBytes), formatBytes(location.totalBytes)), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 12.dp))
            LinearProgressIndicator(progress = { location.usageRatio }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
        }
    }
}

@Composable
private fun CategoryCard(category: CategoryLocation, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(categoryIcon(category.iconKey), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Column(Modifier.padding(start = 14.dp)) {
                Text(stringResource(category.titleRes), style = MaterialTheme.typography.titleMedium)
                Text(category.directory.absolutePath, style = MaterialTheme.typography.bodySmall, maxLines = 1)
            }
        }
    }
}

private fun categoryIcon(key: String) = when (key) {
    "image" -> Icons.Default.Image
    "video" -> Icons.Default.VideoLibrary
    "music" -> Icons.Default.LibraryMusic
    "download" -> Icons.Default.Download
    "document" -> Icons.Default.Description
    "app" -> Icons.Default.Apps
    "audio" -> Icons.Default.AudioFile
    else -> Icons.Default.InsertDriveFile
}

private fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val units = arrayOf("KB", "MB", "GB", "TB")
    var value = bytes.toDouble()
    var index = -1
    while (value >= 1024 && index < units.lastIndex) { value /= 1024; index++ }
    return "%.1f %s".format(value, units[index])
}
