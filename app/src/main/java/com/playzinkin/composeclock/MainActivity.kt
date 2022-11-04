package com.playzinkin.composeclock

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.SensorManager.*
import android.os.Bundle
import android.view.OrientationEventListener
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import com.playzinkin.composeclock.models.ParticlesModel
import com.playzinkin.composeclock.models.SecondModel
import com.playzinkin.composeclock.models.create
import com.playzinkin.composeclock.models.next
import com.playzinkin.composeclock.ui.theme.ComposeClockTheme
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.isActive
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

private const val PI = kotlin.math.PI.toFloat()
private const val PI_DIV_2 = PI / 2f

class MainActivity : ComponentActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager

    private val gameRotationReading = FloatArray(4)

    private val rotationMatrix = FloatArray(16)
    private val orientationAngles = FloatArray(3)

    private var orientationFromSensor: Int = 0

    private lateinit var orientationEventListener: OrientationEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        orientationEventListener = object : OrientationEventListener(this) {
            override fun onOrientationChanged(orientation: Int) {
                orientationFromSensor = orientation
            }
        }
        setContent {
            ComposeClockTheme {
                ComposeClock(
                    random = Random(System.currentTimeMillis())
                ) {
                    updateOrientationAngles()
                    rotationMatrix
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)?.also { sensor ->
            sensorManager.registerListener(
                this,
                sensor,
                10_000
            )
        }
        orientationEventListener.enable()
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
        orientationEventListener.disable()
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_GAME_ROTATION_VECTOR) {
            System.arraycopy(event.values, 0, gameRotationReading, 0, gameRotationReading.size)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun updateOrientationAngles() {
        val tempRotationMatrix = FloatArray(16)
        getRotationMatrixFromVector(
            tempRotationMatrix,
            gameRotationReading.map { -it }.toFloatArray()
        )
        remapCoordinateSystem(tempRotationMatrix, AXIS_MINUS_Y, AXIS_Z, rotationMatrix)
        getOrientation(rotationMatrix, orientationAngles)
    }
}

@Composable
fun ComposeClock(random: Random, calcSensorAngle: () -> FloatArray) {
    ClockBackground()
    ClockSphere()
    ClockHourAndMinuteMarks()
    ClockParticles(random, calcSensorAngle)
    ClockHoursHand(random)
    ClockMinutesHand(random)
    ClockSecondsHand()
    ClockSensor(calcSensorAngle)
}

@Composable
fun ClockBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(Color.Black)
    }
}

@Composable
fun ClockSphere() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val clockRadius = 0.9f * size.minDimension / 2.0f
        drawCircle(
            color = Color(255, 255, 255, 128),
            radius = clockRadius,
            style = Stroke(
                width = 3.dp.toPx()
            ),
        )
    }
}

@Composable
fun ClockHourAndMinuteMarks() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        repeat(60) {
            val clockRadius = 0.95f * size.minDimension / 2.0f
            val initialDegrees = -PI_DIV_2
            val secondsToRadians = PI / 30.0f
            val degree = initialDegrees + it * secondsToRadians
            val x = center.x + cos(degree) * clockRadius
            val y = center.y + sin(degree) * clockRadius
            val isHourMark = it % 5 == 0
            val style = if (isHourMark) Fill else Stroke(width = 1.dp.toPx())
            val radius = if (isHourMark) 5.dp.toPx() else 2.dp.toPx()
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
fun ClockParticles(random: Random, calcSensorAngle: () -> FloatArray) {
    var particlesModel by remember { mutableStateOf(ParticlesModel()) }
    FrameEffect { period ->
        particlesModel = particlesModel.next(random, period, 0.0f)
    }
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .alpha(0.8f)
            .onSizeChanged {
                particlesModel = ParticlesModel.create(
                    style = ParticlesModel.Style.Background,
                    random = random,
                )
            }
    ) {

        drawIntoCanvas { canvas ->
            val matrix = android.graphics.Matrix()
            matrix.setValues(calcSensorAngle.invoke())
            canvas.nativeCanvas.setMatrix(matrix)
            drawParticles(particlesModel)
        }
    }
}

@Composable
fun ClockHoursHand(random: Random) {
    var particlesModel by remember { mutableStateOf(ParticlesModel()) }
    FrameEffect { period ->
        particlesModel =
            particlesModel.next(random, period, System.currentTimeMillis().toHourRadians())
    }
    Canvas(
        modifier = Modifier
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
fun ClockMinutesHand(random: Random) {
    var particlesModel by remember { mutableStateOf(ParticlesModel()) }
    FrameEffect { period ->
        particlesModel =
            particlesModel.next(random, period, System.currentTimeMillis().toMinuteRadians())
    }
    Canvas(
        modifier = Modifier
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
fun ClockSecondsHand() {
    var secondModel by remember { mutableStateOf(SecondModel.create()) }
    FrameEffect { period ->
        secondModel = secondModel.next(period)
    }
    Canvas(modifier = Modifier.fillMaxSize()) {
        val interpolator = FastOutSlowInEasing
        val animatedSecond =
            secondModel.state.seconds + interpolator.transform((secondModel.state.millis % 1000) / 1000f)
        val initialDegrees = -PI_DIV_2
        val secondsToRadians = PI / 30.0f
        val degree = initialDegrees + animatedSecond * secondsToRadians
        val clockRadius = 0.9f * size.minDimension / 2.0f
        val x = center.x + cos(degree) * clockRadius
        val y = center.y + sin(degree) * clockRadius
        drawCircle(
            color = Color.White,
            radius = 4.dp.toPx(),
            center = Offset(x, y)
        )
    }
}

@Composable
fun ClockSensor(calcSensorAngle: () -> FloatArray) {
    var sensorAngle by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        var lastFrame = 0L
        while (isActive) {
            val nextFrame = awaitFrame()
            if (lastFrame != 0L) {
                sensorAngle = 0f//calcSensorAngle.invoke()
            }
            lastFrame = nextFrame
        }
    }
    Canvas(modifier = Modifier.fillMaxSize()) {
        val radius = min(center.x, center.y) * 0.85f
        drawCircle(
            color = Color.Magenta,
            center = Offset(
                center.x + radius * cos(sensorAngle),
                center.y + radius * sin(sensorAngle),
            ),
            radius = 4.dp.toPx(),
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
            center.x + radius * state.length * cos(state.angle + angleOffset),
            center.y + radius * state.length * sin(state.angle + angleOffset),
        ),
        style = state.drawStyle,
        radius = state.particleSize,
        alpha = state.alpha,
    )
}

@Composable
@NonRestartableComposable
fun FrameEffect(
    block: (period: Long) -> Unit
) {
    LaunchedEffect(Unit) {
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

