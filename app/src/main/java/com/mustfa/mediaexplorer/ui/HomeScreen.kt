package com.mustfa.mediaexplorer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mustfa.mediaexplorer.R
import com.mustfa.mediaexplorer.data.FileEntry
import com.mustfa.mediaexplorer.data.SortField
import com.mustfa.mediaexplorer.domain.FolderCoverResolver
import java.io.File
import java.text.DateFormat
import java.util.Date

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun HomeScreen(viewModel: MustfaViewModel) {
    val directory by viewModel.directory.collectAsState()
    val entries by viewModel.entries.collectAsState()
    val storage by viewModel.storage.collectAsState()
    val query by viewModel.query.collectAsState()
    var showCreateFolder by remember { mutableStateOf(false) }
    var showSort by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (directory.parentFile == null) stringResource(R.string.home) else directory.name) },
                navigationIcon = {
                    if (directory.parentFile != null) IconButton(onClick = viewModel::back) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { showSort = true }) { Icon(Icons.Default.Sort, contentDescription = stringResource(R.string.sort)) }
                    IconButton(onClick = { showCreateFolder = true }) { Icon(Icons.Default.CreateNewFolder, contentDescription = stringResource(R.string.new_folder)) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::setQuery,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                placeholder = { Text(stringResource(R.string.search)) }
            )
            storage?.let { summary -> StorageCard(summary) }
            Breadcrumbs(directory, viewModel)
            if (entries.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(stringResource(R.string.empty_folder)) }
            } else {
                LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp)) {
                    items(entries, key = { it.file.absolutePath }) { entry -> FileRow(entry, viewModel::open) }
                }
            }
        }
    }
    if (showCreateFolder) {
        CreateFolderDialog(onDismiss = { showCreateFolder = false }, onCreate = { viewModel.createFolder(it); showCreateFolder = false })
    }
    if (showSort) {
        SortDialog(onDismiss = { showSort = false }, onSelect = { viewModel.setSort(it); showSort = false })
    }
}

@Composable
private fun StorageCard(summary: com.mustfa.mediaexplorer.data.StorageSummary) {
    Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.padding(16.dp)) {
            Text(stringResource(R.string.storage), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(formatBytes(summary.freeBytes) + " free of " + formatBytes(summary.totalBytes), style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(progress = { summary.usageRatio }, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun Breadcrumbs(directory: File, viewModel: MustfaViewModel) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(directory.absolutePath, style = MaterialTheme.typography.labelLarge, maxLines = 1)
    }
    Divider()
}

@Composable
private fun FileRow(entry: FileEntry, onOpen: (FileEntry) -> Unit) {
    val cover = remember(entry.file.absolutePath) { if (entry.isDirectory) FolderCoverResolver().resolve(entry.file) else null }
    Row(
        Modifier.fillMaxWidth().clickable { onOpen(entry) }.padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (cover != null) {
            AsyncImage(model = cover, contentDescription = entry.name, modifier = Modifier.size(52.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
        } else {
            Icon(fileIcon(entry), contentDescription = null, modifier = Modifier.size(40.dp), tint = if (entry.isDirectory) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(entry.name, maxLines = 1, style = MaterialTheme.typography.bodyLarge)
            Text(if (entry.isDirectory) "Folder" else formatBytes(entry.size) + "  " + DateFormat.getDateInstance().format(Date(entry.modified)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Default.MoreVert, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun fileIcon(entry: FileEntry) = when {
    entry.isDirectory -> Icons.Default.Folder
    entry.extension in setOf("jpg", "jpeg", "png", "webp", "gif") -> Icons.Default.Image
    entry.extension in setOf("mp4", "mkv", "avi", "mov") -> Icons.Default.VideoFile
    entry.extension in setOf("mp3", "wav", "flac", "m4a", "ogg") -> Icons.Default.AudioFile
    else -> Icons.Default.Description
}

@Composable
private fun CreateFolderDialog(onDismiss: () -> Unit, onCreate: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(stringResource(R.string.new_folder)) }, text = {
        OutlinedTextField(value = name, onValueChange = { name = it }, singleLine = true, label = { Text(stringResource(R.string.folder_name)) })
    }, confirmButton = { Button(onClick = { onCreate(name) }, enabled = name.isNotBlank()) { Text(stringResource(R.string.create)) } }, dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } })
}

@Composable
private fun SortDialog(onDismiss: () -> Unit, onSelect: (SortField) -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text(stringResource(R.string.sort)) }, text = {
        Column { SortField.entries.forEach { field -> TextButton(onClick = { onSelect(field) }, modifier = Modifier.fillMaxWidth()) { Text(field.name.lowercase().replaceFirstChar(Char::uppercase)) } } }
    }, confirmButton = {})
}

private fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val units = arrayOf("KB", "MB", "GB", "TB")
    var value = bytes.toDouble()
    var index = -1
    while (value >= 1024 && index < units.lastIndex) { value /= 1024; index++ }
    return "%.1f %s".format(value, units[index])
}
