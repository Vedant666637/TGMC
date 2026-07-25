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
): Modifier = this.then(
    Modifier
        .drawBehind {
            val radiusPx = cornerRadius.toPx()
            val blurRadius = elevation.toPx().coerceAtLeast(1f)
            val xPx = offsetX.toPx()
            val yPx = offsetY.toPx()

            drawIntoCanvas { canvas ->
                val frameworkCanvas = canvas.nativeCanvas

                // Dark Shadow (Bottom Right)
                val darkPaint = android.graphics.Paint().apply {
                    color = darkShadowColor.toArgb()
                    maskFilter = android.graphics.BlurMaskFilter(blurRadius, android.graphics.BlurMaskFilter.Blur.NORMAL)
                }
                frameworkCanvas.drawRoundRect(
                    xPx, yPx, size.width + xPx, size.height + yPx,
                    radiusPx, radiusPx, darkPaint
                )

                // Light Shadow (Top Left)
                val lightPaint = android.graphics.Paint().apply {
                    color = lightShadowColor.toArgb()
                    maskFilter = android.graphics.BlurMaskFilter(blurRadius, android.graphics.BlurMaskFilter.Blur.NORMAL)
                }
                frameworkCanvas.drawRoundRect(
                    -xPx, -yPx, size.width - xPx, size.height - yPx,
                    radiusPx, radiusPx, lightPaint
                )
            }
        }
        .background(backgroundColor, RoundedCornerShape(cornerRadius))
        .clip(RoundedCornerShape(cornerRadius))
)

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
): Modifier = this.then(
    Modifier
        .background(backgroundColor, RoundedCornerShape(cornerRadius))
        .drawWithContent {
            drawContent()
            val radiusPx = cornerRadius.toPx()
            val blurRadius = elevation.toPx().coerceAtLeast(1f)
            val xPx = offsetX.toPx()
            val yPx = offsetY.toPx()
            val strokeWidth = 8.dp.toPx()

            val clipPath = Path().apply {
                addRoundRect(androidx.compose.ui.geometry.RoundRect(0f, 0f, size.width, size.height, CornerRadius(radiusPx, radiusPx)))
            }

            clipPath(clipPath) {
                drawIntoCanvas { canvas ->
                    val frameworkCanvas = canvas.nativeCanvas

                    // Dark inner shadow (comes from top-left, pushed bottom-right)
                    val darkPaint = android.graphics.Paint().apply {
                        color = darkShadowColor.toArgb()
                        maskFilter = android.graphics.BlurMaskFilter(blurRadius, android.graphics.BlurMaskFilter.Blur.NORMAL)
                        style = android.graphics.Paint.Style.STROKE
                        this.strokeWidth = strokeWidth
                    }
                    frameworkCanvas.drawRoundRect(
                        -xPx, -yPx, size.width + xPx, size.height + yPx,
                        radiusPx, radiusPx, darkPaint
                    )

                    // Light inner shadow (comes from bottom-right, pushed top-left)
                    val lightPaint = android.graphics.Paint().apply {
                        color = lightShadowColor.toArgb()
                        maskFilter = android.graphics.BlurMaskFilter(blurRadius, android.graphics.BlurMaskFilter.Blur.NORMAL)
                        style = android.graphics.Paint.Style.STROKE
                        this.strokeWidth = strokeWidth
                    }
                    frameworkCanvas.drawRoundRect(
                        xPx, yPx, size.width - xPx, size.height - yPx,
                        radiusPx, radiusPx, lightPaint
                    )
                }
            }
        }
        .clip(RoundedCornerShape(cornerRadius))
)
