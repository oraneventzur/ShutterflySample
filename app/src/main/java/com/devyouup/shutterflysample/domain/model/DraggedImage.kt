package com.devyouup.shutterflysample.domain.model

import androidx.annotation.DrawableRes
import androidx.compose.ui.geometry.Offset

data class DraggedImage(
    @DrawableRes val resId: Int,
    val offset: Offset
)
