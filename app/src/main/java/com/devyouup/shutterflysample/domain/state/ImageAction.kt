package com.devyouup.shutterflysample.domain.state

import com.devyouup.shutterflysample.domain.model.ImageOnCanvas

sealed class ImageAction {

    abstract val imageId: String

    data class Add(val addedImage: ImageOnCanvas) : ImageAction() {
        override val imageId: String get() = addedImage.id
    }

    data class Delete(val deletedImage: ImageOnCanvas) : ImageAction() {
        override val imageId: String get() = deletedImage.id
    }

    data class Transform(
        val beforeState: ActionState,
        val afterState: ActionState
    ) : ImageAction() {
        override val imageId: String get() = beforeState.imageId
    }
}