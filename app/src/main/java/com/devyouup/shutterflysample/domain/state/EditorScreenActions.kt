package com.devyouup.shutterflysample.domain.state

import androidx.annotation.DrawableRes
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize

sealed interface EditorScreenActions {

    data class ImageSelected(val imageId: String?) : EditorScreenActions
    data object DeleteSelectedImage : EditorScreenActions
    data object TransformGestureStarted : EditorScreenActions
    data object TransformGestureEnded : EditorScreenActions
    data class TransformChanged(val pan: Offset, val zoom: Float) : EditorScreenActions

    data class OnCarouselDragStart(@DrawableRes val resId: Int, val startOffset: Offset) : EditorScreenActions
    data class OnCarouselDrag(val dragAmount: Offset) : EditorScreenActions
    data object OnCarouselDragEnd : EditorScreenActions

    data class OnImageSizeChanged(val imageId: String, val size: IntSize) : EditorScreenActions
    data class OnCanvasSizeChanged(val size: IntSize) : EditorScreenActions
    data object Undo : EditorScreenActions
    data object Redo : EditorScreenActions
    data object DismissError : EditorScreenActions

}