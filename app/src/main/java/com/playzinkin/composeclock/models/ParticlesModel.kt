package com.playzinkin.composeclock.models

import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private const val PI = kotlin.math.PI.toFloat()

data class ParticlesModel(
    val angleOffset: Float = 0f,
    val style: Style = Style.Background,
    val states: List<State> = emptyList(),
) {
    sealed class Style {
        abstract val startAngleOffsetRadians: Float
        abstract val endAngleOffsetRadians: Float
        abstract val minLengthModifier: Float
        abstract val maxLengthModifier: Float
        abstract val minAlphaThreshold: Float
        abstract val maxAlphaThreshold: Float
        abstract val minVelocity: Float
        abstract val maxVelocity: Float
        abstract val minSize: Dp
        abstract val maxSize: Dp
        abstract val density: Float

        object Background : Style() {
            override val startAngleOffsetRadians: Float = 0f
            override val endAngleOffsetRadians: Float = 2.0f * PI
            override val minLengthModifier: Float = 0.05f
            override val maxLengthModifier: Float = 0.85f
            override val minAlphaThreshold: Float = minLengthModifier + 0.15f
            override val maxAlphaThreshold: Float = maxLengthModifier - 0.15f
            override val minVelocity: Float = 0.6f
            override val maxVelocity: Float = 0.7f
            override val minSize: Dp = 4.dp
            override val maxSize: Dp = 8.dp
            override val density: Float = 800 / PI
        }

        object MinuteHand : Style() {
            override val startAngleOffsetRadians: Float = -PI / 60f
            override val endAngleOffsetRadians: Float = PI / 60f
            override val minLengthModifier: Float = 0.0f
            override val maxLengthModifier: Float = 0.70f
            override val minAlphaThreshold: Float = 0.05f
            override val maxAlphaThreshold: Float = 0.65f
            override val minVelocity: Float = 0.4f
            override val maxVelocity: Float = 0.8f
            override val minSize: Dp = 4.dp
            override val maxSize: Dp = 8.dp
            override val density: Float = 3000 / PI
        }

        object HourHand : Style() {
            override val startAngleOffsetRadians: Float = -PI / 60f
            override val endAngleOffsetRadians: Float = PI / 60f
            override val minLengthModifier: Float = 0.0f
            override val maxLengthModifier: Float = 0.45f
            override val minAlphaThreshold: Float = 0.05f
            override val maxAlphaThreshold: Float = 0.40f
            override val minVelocity: Float = 0.4f
            override val maxVelocity: Float = 0.8f
            override val minSize: Dp = 4.dp
            override val maxSize: Dp = 8.dp
            override val density: Float = 3000 / PI
        }
    }

    data class State(
        val length: Float = 0f,
        val angle: Float = 0f,
        val particleSize: Float = 0f,
        val alpha: Float = 1f,
        val velocity: Float = 0f,
        val drawStyle: DrawStyle = Fill,
        val millis: Long = 0,
    ) {
        companion object
    }

    companion object
}