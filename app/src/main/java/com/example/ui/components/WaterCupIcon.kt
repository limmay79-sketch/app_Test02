package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.WaterCupActiveStroke
import com.example.ui.theme.WaterCupEmptyBg
import com.example.ui.theme.WaterCupFill
import com.example.ui.theme.WaterCupFillTop
import com.example.ui.theme.WaterCupStroke

@Composable
fun WaterCupIcon(
  cupIndex: Int,
  isFilled: Boolean,
  modifier: Modifier = Modifier,
  cupWidth: Dp = 54.dp,
  cupHeight: Dp = 64.dp,
) {
  val fillProgress by
    animateFloatAsState(
      targetValue = if (isFilled) 1f else 0f,
      animationSpec =
        spring(
          dampingRatio = Spring.DampingRatioMediumBouncy,
          stiffness = Spring.StiffnessMediumLow,
        ),
      label = "waterCupFill",
    )

  val stateText =
    if (isFilled) stringResource(R.string.state_filled) else stringResource(R.string.state_empty)
  val accessibilityLabel = stringResource(R.string.cd_water_cup, cupIndex + 1, stateText)

  Canvas(
    modifier =
      modifier.size(width = cupWidth, height = cupHeight).semantics {
        contentDescription = accessibilityLabel
      }
  ) {
    val w = size.width
    val h = size.height
    val strokeWidth = w * 0.065f

    val topLeftX = w * 0.14f
    val topRightX = w * 0.86f
    val bottomLeftX = w * 0.24f
    val bottomRightX = w * 0.76f
    val topY = h * 0.14f
    val bottomY = h * 0.90f

    val glassPath =
      Path().apply {
        moveTo(topLeftX, topY)
        lineTo(topRightX, topY)
        lineTo(bottomRightX, bottomY - h * 0.05f)
        quadraticTo(bottomRightX, bottomY, bottomRightX - w * 0.06f, bottomY)
        lineTo(bottomLeftX + w * 0.06f, bottomY)
        quadraticTo(bottomLeftX, bottomY, bottomLeftX, bottomY - h * 0.05f)
        close()
      }

    // Soft glass background
    drawPath(path = glassPath, color = WaterCupEmptyBg)

    // Animated water fill clipped inside the glass silhouette
    if (fillProgress > 0.01f) {
      val usableHeight = (bottomY - topY) * 0.80f
      val waterTopY = bottomY - (usableHeight * fillProgress)

      clipPath(glassPath) {
        val waterPath =
          Path().apply {
            moveTo(0f, waterTopY)
            quadraticTo(w * 0.28f, waterTopY - h * 0.045f, w * 0.50f, waterTopY)
            quadraticTo(w * 0.72f, waterTopY + h * 0.045f, w, waterTopY)
            lineTo(w, h)
            lineTo(0f, h)
            close()
          }

        drawPath(
          path = waterPath,
          brush =
            Brush.verticalGradient(
              colors = listOf(WaterCupFillTop, WaterCupFill),
              startY = waterTopY,
              endY = bottomY,
            ),
        )

        // Crisp subtle highlight on the left edge of water
        drawRoundRect(
          color = Color.White.copy(alpha = 0.28f),
          topLeft = Offset(w * 0.26f, waterTopY + h * 0.08f),
          size = Size(w * 0.08f, (bottomY - waterTopY - h * 0.16f).coerceAtLeast(0f)),
          cornerRadius = CornerRadius(w * 0.04f, w * 0.04f),
        )
      }
    }

    // Crisp outer glass stroke
    drawPath(
      path = glassPath,
      color = if (isFilled) WaterCupActiveStroke else WaterCupStroke,
      style =
        Stroke(
          width = strokeWidth,
          cap = StrokeCap.Round,
          join = StrokeJoin.Round,
        ),
    )

    // Top rim ellipse accent
    drawLine(
      color = if (isFilled) WaterCupActiveStroke else WaterCupStroke,
      start = Offset(topLeftX - w * 0.02f, topY),
      end = Offset(topRightX + w * 0.02f, topY),
      strokeWidth = strokeWidth * 1.1f,
      cap = StrokeCap.Round,
    )
  }
}
