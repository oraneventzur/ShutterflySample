package com.devyouup.shutterflysample.presentation.ui.screen.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopActionBar(
    isUndoEnabled: Boolean,
    isRedoEnabled: Boolean,
    isDeleteEnabled: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onDelete: () -> Unit
) {
    TopAppBar(
        title = { Text("Shutterfly Sample") },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary
        ),
        actions = {
            IconButton(onClick = onUndo, enabled = isUndoEnabled) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Undo")
            }
            IconButton(onClick = onRedo, enabled = isRedoEnabled) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Redo")
            }
            IconButton(onClick = onDelete, enabled = isDeleteEnabled) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    )
}