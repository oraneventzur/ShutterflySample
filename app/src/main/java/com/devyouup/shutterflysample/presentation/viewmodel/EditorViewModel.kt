package com.devyouup.shutterflysample.presentation.viewmodel

import androidx.annotation.DrawableRes
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devyouup.shutterflysample.domain.model.DraggedImage
import com.devyouup.shutterflysample.domain.state.ActionState
import com.devyouup.shutterflysample.domain.state.ImageAction
import com.devyouup.shutterflysample.domain.state.EditorUiState
import com.devyouup.shutterflysample.domain.model.ImageOnCanvas
import com.devyouup.shutterflysample.domain.state.EditorScreenActions
import com.devyouup.shutterflysample.domain.usecase.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs

class EditorViewModel(
    private val getSampleImagesUseCase: GetSampleImagesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private val undoStack = ArrayDeque<ImageAction>()
    private val redoStack = ArrayDeque<ImageAction>()
    private var currentTransformAction: ImageAction.Transform? = null

    init {
        loadSampleImages()
    }

    fun onAction(action: EditorScreenActions) {
        when (action) {
            is EditorScreenActions.ImageSelected -> onImageSelected(action.imageId)
            is EditorScreenActions.OnCarouselDragStart -> onCarouselDragStart(action.resId, action.startOffset)
            is EditorScreenActions.OnCarouselDrag -> onCarouselDrag(action.dragAmount)
            is EditorScreenActions.OnCarouselDragEnd -> onCarouselDragEnd()
            is EditorScreenActions.OnCanvasSizeChanged -> onCanvasSizeChanged(action.size)
            is EditorScreenActions.OnImageSizeChanged -> onImageSizeChanged(action.imageId, action.size)
            is EditorScreenActions.TransformChanged -> onTransformChanged(action.pan, action.zoom)
            EditorScreenActions.DeleteSelectedImage -> deleteSelectedImage()
            EditorScreenActions.TransformGestureEnded -> onTransformGestureEnded()
            EditorScreenActions.TransformGestureStarted -> onTransformGestureStarted()
            EditorScreenActions.DismissError -> dismissError()
            EditorScreenActions.Redo -> onRedo()
            EditorScreenActions.Undo -> onUndo()
        }
    }

    private fun onCanvasSizeChanged(size: IntSize) {
        _uiState.update { it.copy(canvasSize = size) }
    }

    private fun onImageSizeChanged(imageId: String, size: IntSize) {
        _uiState.update { state ->
            state.copy(
                canvasImages = state.canvasImages.map {
                    if (it.id == imageId && (it.width != size.width || it.height != size.height)) {
                        it.copy(width = size.width, height = size.height)
                    } else {
                        it
                    }
                }
            )
        }
    }

    private fun onCarouselDragStart(@DrawableRes resId: Int, startOffset: Offset) {
        _uiState.update {
            it.copy(draggedImage = DraggedImage(resId, startOffset))
        }
    }

    private fun onCarouselDrag(dragAmount: Offset) {
        _uiState.value.draggedImage?.let {
            _uiState.update {
                it.copy(draggedImage = it.draggedImage?.copy(offset = it.draggedImage.offset + dragAmount))
            }
        }
    }

    private fun onCarouselDragEnd() {
        _uiState.value.draggedImage?.let { dragged ->
            if (_uiState.value.canvasSize.height > 0 && dragged.offset.y < _uiState.value.canvasSize.height) {
                addImageToCanvas(dragged.resId, dragged.offset)
            }
        }
        _uiState.update { it.copy(draggedImage = null) }
    }

    private fun addImageToCanvas(@DrawableRes resId: Int, dropOffset: Offset) {
        val canvasCenter = Offset(
            _uiState.value.canvasSize.width / 2f,
            _uiState.value.canvasSize.height / 2f
        )
        val newImage = ImageOnCanvas(
            drawableResId = resId,
            // Position the new image centered on the drop location
            offsetX = dropOffset.x - canvasCenter.x,
            offsetY = dropOffset.y - canvasCenter.y,
            zIndex = (_uiState.value.canvasImages.maxOfOrNull { it.zIndex } ?: 0f) + 1
        )
        val newImages = _uiState.value.canvasImages + newImage
        val action = ImageAction.Add(newImage)
        pushToUndoStack(action)

        _uiState.update { it.copy(canvasImages = newImages) }
        onImageSelected(newImage.id)
    }

    private fun onImageSelected(imageId: String?) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedImageId = imageId,
                canvasImages = currentState.canvasImages.map { img ->
                    if (img.id == imageId) {
                        img.copy(zIndex = (currentState.canvasImages.maxOfOrNull { i -> i.zIndex } ?: 0f) + 1)
                    } else {
                        img
                    }
                }.sortedBy { it.zIndex }
            )
        }
    }

    private fun deleteSelectedImage() {
        val selectedId = _uiState.value.selectedImageId ?: return
        val imageToDelete = _uiState.value.canvasImages.find { it.id == selectedId } ?: return

        val newImages = _uiState.value.canvasImages.filterNot { it.id == selectedId }
        val action = ImageAction.Delete(imageToDelete)
        pushToUndoStack(action)

        _uiState.update {
            it.copy(canvasImages = newImages, selectedImageId = null)
        }
    }

    private fun onTransformGestureStarted() {
        val selectedId = _uiState.value.selectedImageId ?: return
        val image = _uiState.value.canvasImages.find { it.id == selectedId } ?: return
        currentTransformAction = ImageAction.Transform(
            beforeState = ActionState(image.id, image.scale, image.offsetX, image.offsetY),
            afterState = ActionState(image.id, image.scale, image.offsetX, image.offsetY)
        )
    }

    private fun onTransformGestureEnded() {
        currentTransformAction?.let {
            if (it.beforeState != it.afterState) {
                pushToUndoStack(it)
            }
        }
        currentTransformAction = null
    }

    private fun onTransformChanged(pan: Offset, zoom: Float) {
        val selectedId = _uiState.value.selectedImageId ?: return
        _uiState.update { state ->
            val newImages = state.canvasImages.map { image ->
                if (image.id == selectedId) {
                    val newScale = (image.scale * zoom).coerceIn(0.2f, 5f)

                    val imageWidthPx = image.width * newScale
                    val imageHeightPx = image.height * newScale

                    val canvasWidth = state.canvasSize.width.toFloat()
                    val canvasHeight = state.canvasSize.height.toFloat()

                    val xBound = abs(canvasWidth - imageWidthPx) / 2f
                    val yBound = abs(canvasHeight - imageHeightPx) / 2f

                    image.copy(
                        scale = newScale,
                        offsetX = (image.offsetX + pan.x).coerceIn(-xBound, xBound),
                        offsetY = (image.offsetY + pan.y).coerceIn(-yBound, yBound)
                    )
                } else {
                    image
                }
            }
            val updatedImage = newImages.find { it.id == selectedId }
            if (updatedImage != null) {
                currentTransformAction = currentTransformAction?.copy(
                    afterState = ActionState(
                        updatedImage.id,
                        updatedImage.scale,
                        updatedImage.offsetX,
                        updatedImage.offsetY
                    )
                )
            }
            state.copy(canvasImages = newImages)
        }
    }

    private fun loadSampleImages() {
        viewModelScope.launch {
            _uiState.update { it.copy(sampleImages = getSampleImagesUseCase()) }
        }
    }

    private fun onUndo() {
        if (undoStack.isEmpty()) return
        val lastAction = undoStack.removeLast()
        redoStack.addLast(lastAction)
        _uiState.update { applyAction(it, lastAction, isUndo = true) }
        updateUndoRedoState()
    }

    private fun onRedo() {
        if (redoStack.isEmpty()) return
        val nextAction = redoStack.removeLast()
        undoStack.addLast(nextAction)
        _uiState.update { applyAction(it, nextAction, isUndo = false) }
        updateUndoRedoState()
    }

    private fun pushToUndoStack(action: ImageAction) {
        undoStack.addLast(action)
        redoStack.clear()
        updateUndoRedoState()
    }

    private fun updateUndoRedoState() {
        _uiState.update {
            it.copy(
                isUndoEnabled = undoStack.isNotEmpty(),
                isRedoEnabled = redoStack.isNotEmpty()
            )
        }
    }

    private fun applyAction(currentState: EditorUiState, action: ImageAction, isUndo: Boolean): EditorUiState {
        return when (action) {
            is ImageAction.Add -> if (isUndo) {
                currentState.copy(
                    canvasImages = currentState.canvasImages.filterNot { it.id == action.imageId },
                    selectedImageId = null
                )
            } else {
                currentState.copy(canvasImages = currentState.canvasImages + action.addedImage)
            }
            is ImageAction.Delete -> if (isUndo) {
                currentState.copy(canvasImages = (currentState.canvasImages + action.deletedImage).sortedBy { it.zIndex })
            } else {
                currentState.copy(canvasImages = currentState.canvasImages.filterNot { it.id == action.imageId })
            }
            is ImageAction.Transform -> {
                val stateToApply = if (isUndo) action.beforeState else action.afterState
                currentState.copy(
                    canvasImages = currentState.canvasImages.map {
                        if (it.id == action.imageId) it.copy(
                            scale = stateToApply.scale,
                            offsetX = stateToApply.offsetX,
                            offsetY = stateToApply.offsetY
                        ) else it
                    }
                )
            }
        }
    }

    private fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}