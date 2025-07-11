package com.devyouup.shutterflysample.domain.model

import androidx.annotation.DrawableRes
import java.util.UUID

data class ImageOnCanvas(
    val id: String = UUID.randomUUID().toString(),
    @DrawableRes val drawableResId: Int,
    val scale: Float = 1f,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val zIndex: Float = 0f,
    val width: Int = 0,
    val height: Int = 0
)