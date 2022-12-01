package com.playzinkin.composeclock.models

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private const val PI = kotlin.math.PI.toFloat()

@Stable
data class ParticlesModel(
    val angleOffset: Float = 0f,
    val style: Style = Style.Background,
    val states: List<State> = emptyList(),
) {
    @Stable
    sealed class Style {
        abstract val startAngle: Float
        abstract val endAngle: Float
        abstract val minPosition: Float
        abstract val maxPosition: Float
        abstract val minAlphaThreshold: Float
        abstract val maxAlphaThreshold: Float
        abstract val minVelocity: Float
        abstract val maxVelocity: Float
        abstract val minSize: Dp
        abstract val maxSize: Dp
        abstract val density: Float

        @Stable
        object Background : Style() {
            override val startAngle: Float = 0f
            override val endAngle: Float = 2.0f * PI
            override val minPosition: Float = 0.05f
            override val maxPosition: Float = 0.85f
            override val minAlphaThreshold: Float = 0.2f
            override val maxAlphaThreshold: Float = 0.7f
            override val minVelocity: Float = 0.6f
            override val maxVelocity: Float = 0.7f
            override val minSize: Dp = 4.dp
            override val maxSize: Dp = 8.dp
            override val density: Float = 800 / PI
        }

        @Stable
        object MinuteHand : Style() {
            override val startAngle: Float = -PI / 60f
            override val endAngle: Float = PI / 60f
            override val minPosition: Float = 0.0f
            override val maxPosition: Float = 0.70f
            override val minAlphaThreshold: Float = 0.1f
            override val maxAlphaThreshold: Float = 0.6f
            override val minVelocity: Float = 0.4f
            override val maxVelocity: Float = 0.8f
            override val minSize: Dp = 4.dp
            override val maxSize: Dp = 8.dp
            override val density: Float = 3000 / PI
        }

        @Stable
        object HourHand : Style() {
            override val startAngle: Float = -PI / 60f
            override val endAngle: Float = PI / 60f
            override val minPosition: Float = 0.0f
            override val maxPosition: Float = 0.5f
            override val minAlphaThreshold: Float = 0.1f
            override val maxAlphaThreshold: Float = 0.4f
            override val minVelocity: Float = 0.4f
            override val maxVelocity: Float = 0.8f
            override val minSize: Dp = 4.dp
            override val maxSize: Dp = 8.dp
            override val density: Float = 3000 / PI
        }
    }

    @Stable
    data class State(
        val offset: Float = 0f,
        val angle: Float = 0f,
        val particleSize: Float = 0f,
        val alpha: Float = 1f,
        val velocity: Float = 0f,
        val drawStyle: DrawStyle = Fill,
    ) {
        companion object
    }

    companion object
}