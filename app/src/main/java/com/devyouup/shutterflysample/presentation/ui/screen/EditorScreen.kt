package com.devyouup.shutterflysample.presentation.ui.screen

import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.devyouup.shutterflysample.domain.state.EditorScreenActions
import com.devyouup.shutterflysample.domain.state.EditorUiState
import com.devyouup.shutterflysample.presentation.ui.screen.components.CanvasArea
import com.devyouup.shutterflysample.presentation.ui.screen.components.ImageCarousel
import com.devyouup.shutterflysample.presentation.ui.screen.components.TopActionBar
import com.devyouup.shutterflysample.presentation.viewmodel.EditorViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun EditorScreenRoot(
    viewModel: EditorViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    EditorScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    state: EditorUiState,
    onAction: (EditorScreenActions) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.error) {
        state.error?.let {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = it,
                    duration = SnackbarDuration.Short
                )
                onAction(EditorScreenActions.DismissError)
            }
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val constraints = this.constraints
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                TopActionBar(
                    isUndoEnabled = state.isUndoEnabled,
                    isRedoEnabled = state.isRedoEnabled,
                    isDeleteEnabled = state.selectedImageId != null,
                    onUndo = { onAction(EditorScreenActions.Undo) },
                    onRedo = { onAction(EditorScreenActions.Redo) },
                    onDelete = { onAction(EditorScreenActions.DeleteSelectedImage) }
                )
            },
            bottomBar = {
                ImageCarousel(
                    images = state.sampleImages,
                    onDragStart = { resId, offset ->
                        onAction(EditorScreenActions.OnCarouselDragStart(resId, offset))
                    },
                    onDrag = { dragAmount ->
                        onAction(EditorScreenActions.OnCarouselDrag(dragAmount))
                    },
                    onDragEnd = {
                        onAction(EditorScreenActions.OnCarouselDragEnd)
                    }
                )
            },
        ) { paddingValues ->
            // Main content area
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .onSizeChanged { onAction(EditorScreenActions.OnCanvasSizeChanged(it)) }
                    .clipToBounds()
            ) {
                CanvasArea(
                    images = state.canvasImages,
                    selectedImageId = state.selectedImageId,
                    onAction = onAction
                )
            }
        }

        // Render the dragged image on top of everything if it exists
        state.draggedImage?.let { draggedImage ->
            val density = LocalDensity.current
            val imageSizeDp = with(density) { 80.dp.toPx() }
            Image(
                painter = painterResource(id = draggedImage.resId),
                contentDescription = "Dragged Image",
                modifier = Modifier
                    .zIndex(100f)
                    .offset {
                        IntOffset(
                            (draggedImage.offset.x - imageSizeDp / 2).toInt(),
                            (draggedImage.offset.y - imageSizeDp / 2).toInt()
                        )
                    }
                    .size(80.dp)
            )
        }
    }
}