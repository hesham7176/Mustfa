package com.mustfa.mediaexplorer.ui

import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.ViewComfy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mustfa.mediaexplorer.R
import com.mustfa.mediaexplorer.data.FileEntry
import com.mustfa.mediaexplorer.data.FileFilter
import com.mustfa.mediaexplorer.data.SortField
import com.mustfa.mediaexplorer.data.ViewMode
import com.mustfa.mediaexplorer.domain.FolderCoverResolver
import com.mustfa.mediaexplorer.thumbnails.ThumbnailEngine
import java.io.File
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileBrowserScreen(viewModel: MustfaViewModel) {
    val directory by viewModel.directory.collectAsState()
    val entries by viewModel.entries.collectAsState()
    val storage by viewModel.storage.collectAsState()
    val query by viewModel.query.collectAsState()
    val mode by viewModel.viewMode.collectAsState()
    val selected by viewModel.selectedPaths.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val history by viewModel.history.collectAsState()
    val filter by viewModel.filter.collectAsState()
    var showFolderDialog by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    var showViewDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var properties by remember { mutableStateOf<FileEntry?>(null) }
    var renameTarget by remember { mutableStateOf<FileEntry?>(null) }
    var operationMove by remember { mutableStateOf(false) }
    var showOperationDialog by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(topBar = {
        TopAppBar(
            title = { Text(directory.name.ifBlank { stringResource(R.string.storage) }, maxLines = 1) },
            navigationIcon = { IconButton(onClick = viewModel::navigateHome) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.home)) } },
            actions = {
                IconButton(onClick = viewModel::refresh) { Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.refresh)) }
                IconButton(onClick = { showSortDialog = true }) { Icon(Icons.Default.Sort, contentDescription = stringResource(R.string.sort)) }
                IconButton(onClick = { showFilterDialog = true }) { Icon(Icons.Default.FilterList, contentDescription = stringResource(R.string.filter)) }
                IconButton(onClick = { showHistoryDialog = true }) { Icon(Icons.Default.History, contentDescription = stringResource(R.string.history)) }
                IconButton(onClick = { showViewDialog = true }) { Icon(Icons.Default.ViewComfy, contentDescription = stringResource(R.string.view_mode)) }
                IconButton(onClick = { showFolderDialog = true }) { Icon(Icons.Default.CreateNewFolder, contentDescription = stringResource(R.string.new_folder)) }
            }
        )
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(value = query, onValueChange = viewModel::setQuery, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), singleLine = true, placeholder = { Text(stringResource(R.string.search)) })
            Breadcrumbs(directory, viewModel)
            storage?.let { summary -> LinearProgressIndicator(progress = { summary.usageRatio }, modifier = Modifier.fillMaxWidth()) }
            if (selected.isNotEmpty()) SelectionBar(selected.size, viewModel, onDelete = { showDeleteDialog = true }, onCopy = { operationMove = false; showOperationDialog = true }, onMove = { operationMove = true; showOperationDialog = true }, onShare = { shareSelected(context, selected.first()) })
            when {
                loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(stringResource(R.string.loading)) }
                entries.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(stringResource(R.string.empty_folder)) }
                mode.grid -> FileGrid(entries, mode, selected, viewModel)
                else -> FileList(entries, mode, selected, viewModel) { properties = it }
            }
        }
    }
    if (showFolderDialog) CreateFolderDialog(onDismiss = { showFolderDialog = false }) { viewModel.createFolder(it); showFolderDialog = false }
    if (showSortDialog) SortDialog(viewModel, onDismiss = { showSortDialog = false })
    if (showFilterDialog) FilterDialog(filter, viewModel, onDismiss = { showFilterDialog = false })
    if (showHistoryDialog) HistoryDialog(history, viewModel, onDismiss = { showHistoryDialog = false })
    if (showViewDialog) ViewModeDialog(mode, viewModel, onDismiss = { showViewDialog = false })
    if (showDeleteDialog) AlertDialog(onDismissRequest = { showDeleteDialog = false }, title = { Text(stringResource(R.string.delete)) }, text = { Text(stringResource(R.string.selected_count, selected.size)) }, confirmButton = { TextButton(onClick = { viewModel.deleteSelected(); showDeleteDialog = false }) { Text(stringResource(R.string.delete)) } }, dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text(stringResource(R.string.cancel)) } })
    properties?.let { entry -> PropertiesDialog(entry, onRename = { renameTarget = entry; properties = null }) { properties = null } }
    renameTarget?.let { entry -> RenameDialog(entry, viewModel) { renameTarget = null } }
    if (showOperationDialog) OperationDialog(viewModel, operationMove, onDismiss = { showOperationDialog = false })
}

@Composable
private fun SelectionBar(count: Int, viewModel: MustfaViewModel, onDelete: () -> Unit, onCopy: () -> Unit, onMove: () -> Unit, onShare: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(stringResource(R.string.selected_count, count), modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
        TextButton(onClick = viewModel::clearSelection) { Text(stringResource(R.string.cancel)) }
        TextButton(onClick = onCopy) { Text(stringResource(R.string.copy)) }
        TextButton(onClick = onMove) { Text(stringResource(R.string.move)) }
        TextButton(onClick = onShare, enabled = count == 1) { Text(stringResource(R.string.share)) }
        TextButton(onClick = onDelete) { Text(stringResource(R.string.delete)) }
    }
}

@Composable
private fun Breadcrumbs(directory: File, viewModel: MustfaViewModel) {
    val scroll = rememberScrollState()
    val segments = remember(directory.absolutePath) { directory.absolutePath.split(File.separator).filter { it.isNotBlank() } }
    Row(Modifier.fillMaxWidth().horizontalScroll(scroll).padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = { viewModel.openHistory(File(File.separator)) }) { Text(File.separator) }
        var path = File.separator
        segments.forEach { segment ->
            path = if (path == File.separator) path + segment else "$path$segment"
            Text("/", color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextButton(onClick = { viewModel.openHistory(File(path)) }) { Text(segment, maxLines = 1) }
        }
    }
    HorizontalDivider()
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FileGrid(entries: List<FileEntry>, mode: ViewMode, selected: Set<String>, viewModel: MustfaViewModel) {
    LazyVerticalGrid(columns = GridCells.Adaptive(if (mode == ViewMode.LARGE_GRID) 180.dp else if (mode == ViewMode.SMALL_GRID) 100.dp else 140.dp), contentPadding = PaddingValues(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(entries, key = { it.file.absolutePath }) { entry ->
            FileCard(entry, selected.contains(entry.file.absolutePath), mode, onClick = { if (entry.isDirectory) viewModel.open(entry) else viewModel.openMedia(entry) }, onLongClick = { viewModel.toggleSelection(entry) })
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FileCard(entry: FileEntry, isSelected: Boolean, mode: ViewMode, onClick: () -> Unit, onLongClick: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var thumbnail by remember(entry.file.absolutePath) { mutableStateOf<com.mustfa.mediaexplorer.thumbnails.ThumbnailResult>(com.mustfa.mediaexplorer.thumbnails.ThumbnailResult.None) }
    LaunchedEffect(entry.file.absolutePath, entry.modified) { thumbnail = ThumbnailEngine(context).resolve(entry) }
    Card(modifier = Modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick), shape = RoundedCornerShape(10.dp)) {
        Column(Modifier.padding(if (mode == ViewMode.SMALL_GRID) 8.dp else 12.dp)) {
            if (thumbnail is com.mustfa.mediaexplorer.thumbnails.ThumbnailResult.Image) AsyncImage(model = (thumbnail as com.mustfa.mediaexplorer.thumbnails.ThumbnailResult.Image).file, contentDescription = entry.name, modifier = Modifier.fillMaxWidth().height(if (mode == ViewMode.LARGE_GRID) 130.dp else 90.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
            else Icon(fileIcon(entry), contentDescription = null, modifier = Modifier.size(if (mode == ViewMode.SMALL_GRID) 36.dp else 52.dp), tint = MaterialTheme.colorScheme.primary)
            Text(entry.name, maxLines = 2, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
            if (mode != ViewMode.SMALL_GRID) Text(if (entry.isDirectory) "Folder" else formatBytes(entry.size), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun FileList(entries: List<FileEntry>, mode: ViewMode, selected: Set<String>, viewModel: MustfaViewModel, onProperties: (FileEntry) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
        items(entries, key = { it.file.absolutePath }) { entry ->
            FileListRow(entry, mode, selected.contains(entry.file.absolutePath), viewModel::open, viewModel::openMedia, viewModel::toggleSelection, onProperties)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FileListRow(entry: FileEntry, mode: ViewMode, selected: Boolean, onOpen: (FileEntry) -> Unit, onPlay: (FileEntry) -> Unit, onLongClick: (FileEntry) -> Unit, onProperties: (FileEntry) -> Unit) {
    Row(Modifier.fillMaxWidth().combinedClickable(onClick = { if (entry.isDirectory) onOpen(entry) else onPlay(entry) }, onLongClick = { onLongClick(entry) }).padding(horizontal = 16.dp, vertical = if (mode.compact) 8.dp else 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(fileIcon(entry), contentDescription = null, modifier = Modifier.size(if (mode.compact) 30.dp else 40.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(entry.name, maxLines = 1, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
            if (mode != ViewMode.SMALL_LIST) Text(if (entry.isDirectory) "Folder" else "${formatBytes(entry.size)}  ${DateFormat.getDateInstance().format(Date(entry.modified))}", style = MaterialTheme.typography.bodySmall)
        }
        IconButton(onClick = { onProperties(entry) }) { Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.properties)) }
    }
    HorizontalDivider()
}

@Composable
private fun SortDialog(viewModel: MustfaViewModel, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text(stringResource(R.string.sort)) }, text = { Column {
        SortField.entries.forEach { field -> TextButton(onClick = { viewModel.setSort(field); onDismiss() }, modifier = Modifier.fillMaxWidth()) { Text(field.name.lowercase().replaceFirstChar(Char::uppercase)) } }
        TextButton(onClick = { viewModel.toggleSortDirection() }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.sort_direction)) }
    } }, confirmButton = {})
}

@Composable
private fun FilterDialog(current: FileFilter, viewModel: MustfaViewModel, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text(stringResource(R.string.filter)) }, text = { Column {
        FileFilter.entries.forEach { value -> TextButton(onClick = { viewModel.setFilter(value); onDismiss() }, modifier = Modifier.fillMaxWidth()) { Text(value.name.lowercase().replaceFirstChar(Char::uppercase) + if (value == current) "  ✓" else "") } }
    } }, confirmButton = {})
}

@Composable
private fun HistoryDialog(history: List<File>, viewModel: MustfaViewModel, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text(stringResource(R.string.history)) }, text = { Column {
        if (history.isEmpty()) Text(stringResource(R.string.empty_history))
        history.asReversed().forEach { path -> TextButton(onClick = { viewModel.openHistory(path); onDismiss() }, modifier = Modifier.fillMaxWidth()) { Text(path.absolutePath, maxLines = 1) } }
    } }, confirmButton = {})
}

@Composable
private fun ViewModeDialog(current: ViewMode, viewModel: MustfaViewModel, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text(stringResource(R.string.view_mode)) }, text = { Column {
        ViewMode.entries.forEach { mode -> TextButton(onClick = { viewModel.setViewMode(mode); onDismiss() }, modifier = Modifier.fillMaxWidth()) { Text(mode.name.replace('_', ' ').lowercase().replaceFirstChar(Char::uppercase) + if (mode == current) "  ✓" else "") } }
    } }, confirmButton = {})
}

@Composable
private fun CreateFolderDialog(onDismiss: () -> Unit, onCreate: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(stringResource(R.string.new_folder)) }, text = { OutlinedTextField(value = name, onValueChange = { name = it }, singleLine = true, label = { Text(stringResource(R.string.folder_name)) }) }, confirmButton = { Button(onClick = { onCreate(name) }, enabled = name.isNotBlank()) { Text(stringResource(R.string.create)) } }, dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } })
}

@Composable
private fun PropertiesDialog(entry: FileEntry, onRename: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text(stringResource(R.string.properties)) }, text = { Column {
        Text(entry.name, fontWeight = FontWeight.Bold)
        Text(entry.file.absolutePath)
        Text(if (entry.isDirectory) "Folder" else formatBytes(entry.size))
        Text(DateFormat.getDateInstance().format(Date(entry.modified)))
    } }, confirmButton = { Row { TextButton(onClick = onRename) { Text(stringResource(R.string.rename)) }; TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } } })
}

@Composable
private fun RenameDialog(entry: FileEntry, viewModel: MustfaViewModel, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf(entry.name) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(stringResource(R.string.rename)) }, text = { OutlinedTextField(value = name, onValueChange = { name = it }, singleLine = true) }, confirmButton = { Button(onClick = { viewModel.rename(entry, name); onDismiss() }, enabled = name.isNotBlank()) { Text(stringResource(R.string.create)) } }, dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } })
}

@Composable
private fun OperationDialog(viewModel: MustfaViewModel, move: Boolean, onDismiss: () -> Unit) {
    var path by remember { mutableStateOf("") }
    var invalid by remember { mutableStateOf(false) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(if (move) stringResource(R.string.move) else stringResource(R.string.copy)) }, text = { Column {
        OutlinedTextField(value = path, onValueChange = { path = it; invalid = false }, singleLine = true, label = { Text(stringResource(R.string.destination_path)) })
        if (invalid) Text(stringResource(R.string.invalid_destination), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    } }, confirmButton = { Button(onClick = { val destination = File(path); if (destination.isDirectory) { viewModel.copySelected(destination, move); onDismiss() } else invalid = true }, enabled = path.isNotBlank()) { Text(stringResource(R.string.create)) } }, dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } })
}

private fun shareSelected(context: android.content.Context, path: String) {
    val file = File(path)
    if (!file.isFile) return
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type = "application/octet-stream"; putExtra(Intent.EXTRA_STREAM, uri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }, null))
}

private fun fileIcon(entry: FileEntry) = when {
    entry.isDirectory -> Icons.Default.Folder
    entry.extension in setOf("jpg", "jpeg", "png", "webp", "gif") -> Icons.Default.Image
    else -> Icons.Default.Description
}

private fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val units = arrayOf("KB", "MB", "GB", "TB")
    var value = bytes.toDouble()
    var index = -1
    while (value >= 1024 && index < units.lastIndex) { value /= 1024; index++ }
    return "%.1f %s".format(value, units[index])
}
