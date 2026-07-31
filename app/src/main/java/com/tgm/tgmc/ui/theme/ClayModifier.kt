package com.tgm.tgmc.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

import androidx.compose.ui.composed
import androidx.compose.runtime.remember

/**
 * Applies a Professional Claymorphism (Neumorphism) effect with soft, dual-directional blur shadows.
 */
fun Modifier.clay(
    backgroundColor: Color,
    cornerRadius: Dp = 24.dp,
    elevation: Dp = 12.dp,
    lightShadowColor: Color = Color.White.copy(alpha = 0.9f),
    darkShadowColor: Color = Color(0xFFA6B4C8).copy(alpha = 0.6f),
    offsetX: Dp = 8.dp,
    offsetY: Dp = 8.dp
): Modifier = composed {
    val radiusPx = cornerRadius.value * androidx.compose.ui.platform.LocalDensity.current.density
    val blurRadius = elevation.value * androidx.compose.ui.platform.LocalDensity.current.density
    val xPx = offsetX.value * androidx.compose.ui.platform.LocalDensity.current.density
    val yPx = offsetY.value * androidx.compose.ui.platform.LocalDensity.current.density
    
    val darkPaint = remember(darkShadowColor, blurRadius) {
        android.graphics.Paint().apply {
            color = darkShadowColor.toArgb()
            maskFilter = android.graphics.BlurMaskFilter(blurRadius.coerceAtLeast(1f), android.graphics.BlurMaskFilter.Blur.NORMAL)
        }
    }
    val lightPaint = remember(lightShadowColor, blurRadius) {
        android.graphics.Paint().apply {
            color = lightShadowColor.toArgb()
            maskFilter = android.graphics.BlurMaskFilter(blurRadius.coerceAtLeast(1f), android.graphics.BlurMaskFilter.Blur.NORMAL)
        }
    }

    this.then(
        Modifier
            .drawBehind {
                drawIntoCanvas { canvas ->
                    val frameworkCanvas = canvas.nativeCanvas
                    frameworkCanvas.drawRoundRect(
                        xPx, yPx, size.width + xPx, size.height + yPx,
                        radiusPx, radiusPx, darkPaint
                    )
                    frameworkCanvas.drawRoundRect(
                        -xPx, -yPx, size.width - xPx, size.height - yPx,
                        radiusPx, radiusPx, lightPaint
                    )
                }
            }
            .background(backgroundColor, RoundedCornerShape(cornerRadius))
            .clip(RoundedCornerShape(cornerRadius))
    )
}

/**
 * Applies an inset Claymorphism effect (inner shadow) for input fields.
 */
fun Modifier.insetClay(
    backgroundColor: Color,
    cornerRadius: Dp = 16.dp,
    elevation: Dp = 8.dp,
    lightShadowColor: Color = Color.White.copy(alpha = 0.9f),
    darkShadowColor: Color = Color(0xFFA6B4C8).copy(alpha = 0.5f),
    offsetX: Dp = 4.dp,
    offsetY: Dp = 4.dp
): Modifier = composed {
    val radiusPx = cornerRadius.value * androidx.compose.ui.platform.LocalDensity.current.density
    val blurRadius = elevation.value * androidx.compose.ui.platform.LocalDensity.current.density
    val xPx = offsetX.value * androidx.compose.ui.platform.LocalDensity.current.density
    val yPx = offsetY.value * androidx.compose.ui.platform.LocalDensity.current.density
    val strokeWidth = 8.dp.value * androidx.compose.ui.platform.LocalDensity.current.density

    val darkPaint = remember(darkShadowColor, blurRadius) {
        android.graphics.Paint().apply {
            color = darkShadowColor.toArgb()
            maskFilter = android.graphics.BlurMaskFilter(blurRadius.coerceAtLeast(1f), android.graphics.BlurMaskFilter.Blur.NORMAL)
            style = android.graphics.Paint.Style.STROKE
            this.strokeWidth = strokeWidth
        }
    }
    val lightPaint = remember(lightShadowColor, blurRadius) {
        android.graphics.Paint().apply {
            color = lightShadowColor.toArgb()
            maskFilter = android.graphics.BlurMaskFilter(blurRadius.coerceAtLeast(1f), android.graphics.BlurMaskFilter.Blur.NORMAL)
            style = android.graphics.Paint.Style.STROKE
            this.strokeWidth = strokeWidth
        }
    }

    this.then(
        Modifier
            .background(backgroundColor, RoundedCornerShape(cornerRadius))
            .drawWithContent {
                drawContent()
                val clipPath = Path().apply {
                    addRoundRect(androidx.compose.ui.geometry.RoundRect(0f, 0f, size.width, size.height, CornerRadius(radiusPx, radiusPx)))
                }
                clipPath(clipPath) {
                    drawIntoCanvas { canvas ->
                        val frameworkCanvas = canvas.nativeCanvas
                        frameworkCanvas.drawRoundRect(
                            -xPx, -yPx, size.width + xPx, size.height + yPx,
                            radiusPx, radiusPx, darkPaint
                        )
                        frameworkCanvas.drawRoundRect(
                            xPx, yPx, size.width - xPx, size.height - yPx,
                            radiusPx, radiusPx, lightPaint
                        )
                    }
                }
            }
            .clip(RoundedCornerShape(cornerRadius))
    )
}
