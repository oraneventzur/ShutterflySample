package com.devyouup.shutterflysample.presentation.ui.screen.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.devyouup.shutterflysample.domain.model.ImageOnCanvas
import com.devyouup.shutterflysample.domain.state.EditorScreenActions

@Composable
fun CanvasArea(
    images: List<ImageOnCanvas>,
    selectedImageId: String?,
    onAction: (EditorScreenActions) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown()
                    onAction(EditorScreenActions.ImageSelected(null))
                }
            }
    ) {

        if (images.isEmpty()) {
            Box(modifier = Modifier.align(Alignment.Center)){
                Text("Long press an image to drag here")
            }
        }

        images.forEach { image ->
            val isSelected = image.id == selectedImageId
            val borderModifier = if (isSelected) {
                Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RectangleShape)
            } else {
                Modifier
            }

            Box(
                modifier = Modifier
                    .zIndex(image.zIndex)
                    .graphicsLayer(
                        scaleX = image.scale,
                        scaleY = image.scale,
                        translationX = image.offsetX,
                        translationY = image.offsetY
                    )
                    .pointerInput(image.id) {
                        awaitEachGesture {
                            val down = awaitFirstDown()
                            onAction(EditorScreenActions.ImageSelected(image.id))
                            onAction(EditorScreenActions.TransformGestureStarted)
                            down.consume()

                            do {
                                val event = awaitPointerEvent()
                                val canceled = event.changes.any { it.isConsumed }
                                if (!canceled) {
                                    val zoom = event.calculateZoom()
                                    val pan = event.calculatePan()
                                    val adjustedPan = pan * image.scale
                                    onAction(EditorScreenActions.TransformChanged(adjustedPan, zoom))
                                }
                            } while (!canceled && event.changes.any { it.pressed })

                            onAction(EditorScreenActions.TransformGestureEnded)
                        }
                    }
                    .align(Alignment.Center)
                    .then(borderModifier)
                    .onSizeChanged {
                        onAction(EditorScreenActions.OnImageSizeChanged(image.id, it))
                    }

            ) {
                Image(
                    painter = painterResource(id = image.drawableResId),
                    contentDescription = "Canvas Image",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .sizeIn(maxWidth = 300.dp, maxHeight = 300.dp, minHeight = 100.dp, minWidth = 100.dp)
                        .clip(RectangleShape)
                )
            }
        }
    }
}
