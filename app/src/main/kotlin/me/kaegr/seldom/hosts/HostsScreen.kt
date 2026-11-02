package me.kaegr.seldom.hosts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.UUID

@Composable
fun HostsScreen(
    hosts: List<Host>,
    onOpen: (Host) -> Unit,
    onSave: (Host) -> Unit,
    onDelete: (Host) -> Unit,
) {
    var editing by remember { mutableStateOf<Host?>(null) }
    var adding by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().safeDrawingPadding()) {
        if (hosts.isEmpty()) {
            Text("No hosts yet", Modifier.align(Alignment.Center))
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                items(hosts, key = { it.id }) { host ->
                    ListItem(
                        headlineContent = { Text(host.name) },
                        supportingContent = { Text(host.url) },
                        trailingContent = { TextButton(onClick = { editing = host }) { Text("Edit") } },
                        modifier = Modifier.clickable { onOpen(host) },
                    )
                }
            }
        }
        FloatingActionButton(
            onClick = { adding = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        ) { Text("+") }
    }

    if (adding || editing != null) {
        val existing = editing
        HostDialog(
            host = existing,
            onDismiss = { adding = false; editing = null },
            onSave = { name, url ->
                onSave(Host(existing?.id ?: UUID.randomUUID().toString(), name.trim(), HostValidator.normalize(url)))
                adding = false
                editing = null
            },
            onDelete = existing?.let { host ->
                { onDelete(host); editing = null }
            },
        )
    }
}

@Composable
private fun HostDialog(
    host: Host?,
    onDismiss: () -> Unit,
    onSave: (name: String, url: String) -> Unit,
    onDelete: (() -> Unit)?,
) {
    var name by remember { mutableStateOf(host?.name.orEmpty()) }
    var url by remember { mutableStateOf(host?.url ?: "https://") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (host == null) "Add host" else "Edit host") },
        text = {
            Column {
                OutlinedTextField(name, { name = it }, label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    url,
                    { url = it },
                    label = { Text("URL") },
                    singleLine = true,
                    isError = error != null,
                    supportingText = error?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )
            }
        },
        confirmButton = {
            Row {
                onDelete?.let { TextButton(onClick = it) { Text("Delete") } }
                TextButton(onClick = {
                    error = HostValidator.validate(name, url)
                    if (error == null) onSave(name, url)
                }) { Text("Save") }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
