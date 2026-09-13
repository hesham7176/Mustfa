package com.mustfa.mediaexplorer.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import com.mustfa.mediaexplorer.R

@Composable
fun SafBrowserScreen(rootUri: Uri, onBack: () -> Unit) {
    val context = LocalContext.current
    var currentUri by remember(rootUri) { mutableStateOf(rootUri) }
    var children by remember { mutableStateOf<List<DocumentFile>>(emptyList()) }
    var unavailable by remember { mutableStateOf(false) }
    LaunchedEffect(currentUri) {
        val directory = DocumentFile.fromTreeUri(context, currentUri) ?: DocumentFile.fromSingleUri(context, currentUri)
        if (directory == null || !directory.exists()) { unavailable = true; children = emptyList() } else { unavailable = false; children = directory.listFiles().toList().sortedWith(compareByDescending<DocumentFile> { it.isDirectory }.thenBy { it.name.orEmpty().lowercase() }) }
    }
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text(stringResource(R.string.home)) }
            Text(currentUri.lastPathSegment.orEmpty(), maxLines = 1, modifier = Modifier.weight(1f))
        }
        when {
            unavailable -> Text(stringResource(R.string.saf_unavailable), modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.error)
            children.isEmpty() -> Text(stringResource(R.string.empty_folder), modifier = Modifier.padding(16.dp))
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                items(children, key = { it.uri.toString() }) { file ->
                    Row(Modifier.fillMaxWidth().clickable {
                        if (file.isDirectory) currentUri = file.uri
                        else context.startActivity(Intent(Intent.ACTION_VIEW).apply { data = file.uri; type = file.type ?: "application/octet-stream"; addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) })
                    }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (file.isDirectory) Icons.Default.Folder else if (file.type?.startsWith("image") == true) Icons.Default.Image else Icons.Default.Description, contentDescription = null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
                        Text(file.name ?: file.uri.lastPathSegment.orEmpty(), modifier = Modifier.padding(start = 14.dp))
                    }
                }
            }
        }
    }
}
