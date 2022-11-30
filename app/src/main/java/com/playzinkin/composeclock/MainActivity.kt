package com.playzinkin.composeclock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.playzinkin.composeclock.models.ParticlesModel
import com.playzinkin.composeclock.models.SecondModel
import com.playzinkin.composeclock.models.create
import com.playzinkin.composeclock.models.next
import com.playzinkin.composeclock.ui.theme.ComposeClockTheme
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.isActive
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

private const val PI = kotlin.math.PI.toFloat()
private const val PI_DIV_2 = PI / 2f

val LocalRandom = compositionLocalOf { Random(System.currentTimeMillis()) }

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setDecorFitsSystemWindows(false)
        setContent {
            ComposeClockTheme {
                val systemUiController = rememberSystemUiController()
                SideEffect {
                    systemUiController.setStatusBarColor(color = Color.Transparent)
                }
                ComposeClock()
            }
        }
    }
}

@Composable
fun ComposeClock(
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier,
    ) {
        ClockSphere()
        ClockHourAndMinuteMarks()
        ClockParticles()
        ClockHoursHand()
        ClockMinutesHand()
        ClockSecondsHand()
    }
}

@Composable
fun ClockSphere(
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val clockRadius = 0.9f * size.minDimension / 2f
        drawCircle(
            color = Color.White.copy(alpha = 0.5f),
            radius = clockRadius,
            style = Stroke(
                width = 3.dp.toPx()
            ),
        )
    }
}

@Composable
fun ClockHourAndMinuteMarks(
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val hourMarkStyle = Fill
        val minuteMarkStyle = Stroke(width = 1.dp.toPx())
        val hourMarkRadius = 5.dp.toPx()
        val minuteMarkRadius = 2.dp.toPx()
        repeat(60) {
            val clockRadius = 0.95f * size.minDimension / 2f
            val initialDegrees = -PI_DIV_2
            val secondsToRadians = PI / 30f
            val degree = initialDegrees + it * secondsToRadians
            val x = center.x + cos(degree) * clockRadius
            val y = center.y + sin(degree) * clockRadius
            val isHourMark = it % 5 == 0
            val style = if (isHourMark) hourMarkStyle else minuteMarkStyle
            val radius = if (isHourMark) hourMarkRadius else minuteMarkRadius
            drawCircle(
                color = Color.White,
                radius = radius,
                style = style,
                center = Offset(x, y)
            )
        }
    }
}

@Composable
fun ClockParticles(
    modifier: Modifier = Modifier,
) {
    val random = LocalRandom.current
    var particlesModel by remember { mutableStateOf(ParticlesModel()) }
    FrameEffect(Unit) { period ->
        particlesModel = particlesModel.next(random, period, 0f)
    }
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .alpha(0.6f)
            .onSizeChanged {
                particlesModel = ParticlesModel.create(
                    style = ParticlesModel.Style.Background,
                    random = random,
                )
            }
    ) {
        drawParticles(particlesModel)
    }
}

@Composable
fun ClockHoursHand(
    modifier: Modifier = Modifier,
) {
    val random = LocalRandom.current
    var particlesModel by remember { mutableStateOf(ParticlesModel()) }
    FrameEffect(Unit) { period ->
        particlesModel =
            particlesModel.next(random, period, System.currentTimeMillis().toHourRadians())
    }
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged {
                particlesModel = ParticlesModel.create(
                    random = random,
                    style = ParticlesModel.Style.HourHand,
                    angleOffset = System
                        .currentTimeMillis()
                        .toHourRadians(),
                )
            }
    ) {
        drawParticles(particlesModel)
    }
}

@Composable
fun ClockMinutesHand(
    modifier: Modifier = Modifier,
) {
    val random = LocalRandom.current
    var particlesModel by remember { mutableStateOf(ParticlesModel()) }
    FrameEffect(Unit) { period ->
        particlesModel =
            particlesModel.next(random, period, System.currentTimeMillis().toMinuteRadians())
    }
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged {
                particlesModel = ParticlesModel.create(
                    random = random,
                    style = ParticlesModel.Style.MinuteHand,
                    angleOffset = System
                        .currentTimeMillis()
                        .toMinuteRadians(),
                )
            }
    ) {
        drawParticles(particlesModel)
    }
}

@Composable
fun ClockSecondsHand(
    modifier: Modifier = Modifier,
) {
    var secondModel by remember { mutableStateOf(SecondModel.create()) }
    FrameEffect(Unit) { period ->
        secondModel = secondModel.next(period)
    }
    Canvas(modifier = modifier.fillMaxSize()) {
        val interpolator = FastOutSlowInEasing
        val animatedSecond =
            secondModel.state.seconds + interpolator.transform((secondModel.state.millis % 1000) / 1000f)
        val initialDegrees = -PI_DIV_2
        val secondsToRadians = PI / 30f
        val degree = initialDegrees + animatedSecond * secondsToRadians
        val clockRadius = 0.9f * size.minDimension / 2f
        val x = center.x + cos(degree) * clockRadius
        val y = center.y + sin(degree) * clockRadius
        drawCircle(
            color = Color.White,
            radius = 4.dp.toPx(),
            center = Offset(x, y)
        )
    }
}

fun DrawScope.drawParticles(particlesModel: ParticlesModel) {
    particlesModel.states.forEach {
        drawParticle(it, particlesModel.angleOffset)
    }
}

fun DrawScope.drawParticle(state: ParticlesModel.State, angleOffset: Float) {
    val radius = min(center.x, center.y)
    drawCircle(
        color = Color.White,
        center = Offset(
            center.x + radius * state.offset * cos(state.angle + angleOffset),
            center.y + radius * state.offset * sin(state.angle + angleOffset),
        ),
        style = state.drawStyle,
        radius = state.particleSize,
        alpha = state.alpha,
    )
}

@Composable
fun FrameEffect(
    key1: Any?,
    block: (period: Long) -> Unit
) {
    LaunchedEffect(key1) {
        var lastFrame = 0L
        while (isActive) {
            val nextFrame = TimeUnit.NANOSECONDS.toMillis(awaitFrame())
            if (lastFrame != 0L) {
                val period = nextFrame - lastFrame
                block.invoke(period)
            }
            lastFrame = nextFrame
        }
    }
}

fun Long.toMinuteRadians() =
    PI * ((TimeUnit.MILLISECONDS.toMinutes(this) % 60 + ((TimeUnit.MILLISECONDS.toSeconds(this) % 60) / 60f)) / 30f) - PI_DIV_2

fun Long.toHourRadians() =
    PI * ((Calendar.getInstance().get(Calendar.HOUR) % 12
            + ((TimeUnit.MILLISECONDS.toMinutes(this) % 60) / 60f)
            + ((TimeUnit.MILLISECONDS.toSeconds(this) % 60) / 3600f)) / 6f) - PI_DIV_2

