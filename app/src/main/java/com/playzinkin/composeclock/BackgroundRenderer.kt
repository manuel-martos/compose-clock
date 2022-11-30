package com.playzinkin.composeclock

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.opengl.GLSurfaceView
import androidx.compose.runtime.Immutable
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

@Immutable
class BackgroundRenderer(
    private val sensorManager: SensorManager
) : GLSurfaceView.Renderer, SensorEventListener {

    private val cube: Cube = Cube()

    private val rotationVectorSensor: Sensor =
        sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)
    private val rotationMatrix = FloatArray(16)

    fun start() {
        sensorManager.registerListener(this, rotationVectorSensor, 10000)
    }

    fun stop() {
        // make sure to turn our sensor off when the activity is paused
        sensorManager.unregisterListener(this)
    }

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig?) {
        // dither is enabled by default, we don't need it
        gl.glDisable(GL10.GL_DITHER)
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST)
        // clear screen in white
        gl.glClearColor(0f, 0f, 0f, 0f)
//        gl.glClearDepthf(10f)
        gl.glEnable(GL10.GL_CULL_FACE)
        gl.glShadeModel(GL10.GL_SMOOTH)
        gl.glEnable(GL10.GL_DEPTH_TEST)
    }

    override fun onDrawFrame(gl: GL10) {
        // clear screen
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT or GL10.GL_DEPTH_BUFFER_BIT)
        // set-up modelview matrix
        gl.glMatrixMode(GL10.GL_MODELVIEW)
        gl.glLoadIdentity()
        gl.glTranslatef(0f, 0f, -3.0f)
        gl.glMultMatrixf(rotationMatrix, 0)
        // draw our object
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY)
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY)
        cube.draw(gl)
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        // set view-port
        gl.glViewport(0, 0, width, height)
        // set projection matrix
        val ratio = width.toFloat() / height
        gl.glMatrixMode(GL10.GL_PROJECTION)
        gl.glLoadIdentity()
        gl.glFrustumf(-ratio, ratio, -1f, 1f, 1f, 10f)
    }

    override fun onSensorChanged(event: SensorEvent) {
        // we received a sensor event. it is a good practice to check
        // that we received the proper event
        if (event.sensor.type == Sensor.TYPE_GAME_ROTATION_VECTOR) {
            // convert the rotation-vector to a 4x4 matrix. the matrix
            // is interpreted by Open GL as the inverse of the
            // rotation-vector, which is what we want.

            SensorManager.getRotationMatrixFromVector(
                rotationMatrix, event.values.map { -it }.toFloatArray()
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

internal class Cube {
    // initialize our cube
    private val mVertexBuffer: FloatBuffer
    private val mColorBuffer: FloatBuffer
    private val mIndexBuffer: ByteBuffer
    fun draw(gl: GL10) {
        gl.glEnable(GL10.GL_CULL_FACE)
        gl.glFrontFace(GL10.GL_CW)
        gl.glShadeModel(GL10.GL_SMOOTH)
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffer)
        gl.glColorPointer(4, GL10.GL_FLOAT, 0, mColorBuffer)
        gl.glDrawElements(GL10.GL_TRIANGLES, 36, GL10.GL_UNSIGNED_BYTE, mIndexBuffer)
    }

    init {
        val vertices = floatArrayOf(
            -1f,
            -1f,
            -1f,
            1f,
            -1f,
            -1f,
            1f,
            1f,
            -1f,
            -1f,
            1f,
            -1f,
            -1f,
            -1f,
            1f,
            1f,
            -1f,
            1f,
            1f,
            1f,
            1f,
            -1f,
            1f,
            1f
        )
        val colors = floatArrayOf(
            0f,
            0f,
            0f,
            1f,
            1f,
            0f,
            0f,
            1f,
            1f,
            1f,
            0f,
            1f,
            0f,
            1f,
            0f,
            1f,
            0f,
            0f,
            1f,
            1f,
            1f,
            0f,
            1f,
            1f,
            1f,
            1f,
            1f,
            1f,
            0f,
            1f,
            1f,
            1f
        )
        val indices = byteArrayOf(
            0, 4, 5, 0, 5, 1,
            1, 5, 6, 1, 6, 2,
            2, 6, 7, 2, 7, 3,
            3, 7, 4, 3, 4, 0,
            4, 7, 6, 4, 6, 5,
            3, 0, 1, 3, 1, 2
        )
        val vbb: ByteBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
        vbb.order(ByteOrder.nativeOrder())
        mVertexBuffer = vbb.asFloatBuffer()
        mVertexBuffer.put(vertices)
        mVertexBuffer.position(0)
        val cbb: ByteBuffer = ByteBuffer.allocateDirect(colors.size * 4)
        cbb.order(ByteOrder.nativeOrder())
        mColorBuffer = cbb.asFloatBuffer()
        mColorBuffer.put(colors)
        mColorBuffer.position(0)
        mIndexBuffer = ByteBuffer.allocateDirect(indices.size)
        mIndexBuffer.put(indices)
        mIndexBuffer.position(0)
    }
}
