package com.devyouup.shutterflysample.domain.state

import androidx.annotation.DrawableRes
import androidx.compose.ui.unit.IntSize
import com.devyouup.shutterflysample.domain.model.DraggedImage
import com.devyouup.shutterflysample.domain.model.ImageOnCanvas


data class EditorUiState(
    val canvasImages: List<ImageOnCanvas> = emptyList(),
    @DrawableRes val sampleImages: List<Int> = emptyList(),
    val selectedImageId: String? = null,
    val isUndoEnabled: Boolean = false,
    val isRedoEnabled: Boolean = false,
    val error: String? = null,
    val draggedImage: DraggedImage? = null,
    val canvasSize: IntSize = IntSize.Zero
)