package com.playzinkin.composeclock.models

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.playzinkin.composeclock.nextFloat
import kotlin.random.Random

fun ParticlesModel.State.next(
    random: Random,
    style: ParticlesModel.Style,
    period: Long
): ParticlesModel.State {
    val transformedMillis = FastOutSlowInEasing.transform(millis / 1000f)
    val unifiedPeriod = period / 10000f
    val acceleration = transformedMillis / 2.0f
    val accelerationPeriod = period * period / 200000f
    val newOffset = offset + velocity * unifiedPeriod + acceleration * accelerationPeriod
    return if (newOffset < style.maxPosition) {
        copy(
            offset = newOffset,
            alpha = ParticlesModel.State.calcAlpha(style, offset),
            millis = (millis + period) % 1000,
        )
    } else {
        ParticlesModel.State.create(random, style, style.minPosition)
    }
}

fun ParticlesModel.State.Companion.create(
    random: Random,
    style: ParticlesModel.Style,
    offset: Float = calcOffset(random, style),
    millis: Long = System.currentTimeMillis() % 1000,
): ParticlesModel.State =
    ParticlesModel.State(
        offset = offset,
        angle = calcAngle(random, style),
        particleSize = calcSize(random, style),
        velocity = calcVelocity(random, style),
        drawStyle = calcDrawStyle(random),
        alpha = calcAlpha(style, offset),
        millis = millis,
    )

private fun ParticlesModel.State.Companion.calcOffset(random: Random, style: ParticlesModel.Style) =
    random.nextFloat(style.minPosition, style.maxPosition)

private fun ParticlesModel.State.Companion.calcAngle(random: Random, style: ParticlesModel.Style) =
    random.nextFloat(style.startAngleInRadians, style.endAngleInRadians)

private fun ParticlesModel.State.Companion.calcSize(random: Random, style: ParticlesModel.Style) =
    random.nextFloat(style.minSize.value, style.maxSize.value)

private fun ParticlesModel.State.Companion.calcVelocity(
    random: Random,
    style: ParticlesModel.Style
) =
    random.nextFloat(style.minVelocity, style.maxVelocity)

private fun ParticlesModel.State.Companion.calcDrawStyle(random: Random): DrawStyle =
    if (random.nextFloat() > 0.6f) {
        Stroke(width = 1.0f)
    } else {
        Fill
    }

private fun ParticlesModel.State.Companion.calcAlpha(
    style: ParticlesModel.Style,
    offset: Float
): Float {
    return when {
        offset > style.maxAlphaThreshold -> {
            (1f - ((offset - style.maxAlphaThreshold) / (style.maxPosition - style.maxAlphaThreshold)))
                .coerceAtLeast(0f)
        }

        offset < style.minAlphaThreshold -> {
            (1f - ((style.minAlphaThreshold - offset) / (style.minAlphaThreshold - style.minPosition)))
                .coerceIn(0f, 1f)
        }

        else -> 1f
    }
}

fun ParticlesModel.next(random: Random, period: Long, angleOffset: Float): ParticlesModel =
    copy(
        angleOffset = angleOffset,
        states = states.map { it.next(random, style, period) }
    )

fun ParticlesModel.Companion.create(
    style: ParticlesModel.Style = ParticlesModel.Style.Background,
    numParticles: Int = calculateNumParticles(style),
    random: Random,
    angleOffset: Float = 0f
): ParticlesModel {
    val states = mutableListOf<ParticlesModel.State>()
    val millis = System.currentTimeMillis() % 1000
    repeat(numParticles) {
        states.add(
            ParticlesModel.State.create(
                random = random,
                style = style,
                millis = millis,
            )
        )
    }
    return ParticlesModel(
        style = style,
        states = states,
        angleOffset = angleOffset,
    )
}

private fun ParticlesModel.Companion.calculateNumParticles(style: ParticlesModel.Style): Int =
    (style.density * (style.endAngleInRadians - style.startAngleInRadians)).toInt()