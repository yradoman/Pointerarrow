package com.pointarrow.nav.data.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface
import android.view.WindowManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class OrientationSensorSource(private val context: Context) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
    private val rotationSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            ?: sensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR)

    val orientationFlow: Flow<Float> = callbackFlow {
        if (rotationSensor == null) {
            trySend(0f)
            close()
            return@callbackFlow
        }

        val rawRotationMatrix = FloatArray(9)
        val remappedMatrix = FloatArray(9)
        val orientationAngles = FloatArray(3)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == rotationSensor.type) {
                    SensorManager.getRotationMatrixFromVector(rawRotationMatrix, event.values)

                    // Компенсація нахилу пристрою (SensorManager.remapCoordinateSystem):
                    // Забезпечує точний азимут при портретному або горизонтальному хваті
                    val displayRotation = windowManager?.defaultDisplay?.rotation ?: Surface.ROTATION_0
                    var axisX = SensorManager.AXIS_X
                    var axisY = SensorManager.AXIS_Z

                    when (displayRotation) {
                        Surface.ROTATION_0 -> {
                            axisX = SensorManager.AXIS_X
                            axisY = SensorManager.AXIS_Z
                        }
                        Surface.ROTATION_90 -> {
                            axisX = SensorManager.AXIS_Z
                            axisY = SensorManager.AXIS_MINUS_X
                        }
                        Surface.ROTATION_180 -> {
                            axisX = SensorManager.AXIS_MINUS_X
                            axisY = SensorManager.AXIS_MINUS_Z
                        }
                        Surface.ROTATION_270 -> {
                            axisX = SensorManager.AXIS_MINUS_Z
                            axisY = SensorManager.AXIS_X
                        }
                    }

                    if (SensorManager.remapCoordinateSystem(rawRotationMatrix, axisX, axisY, remappedMatrix)) {
                        SensorManager.getOrientation(remappedMatrix, orientationAngles)
                    } else {
                        SensorManager.getOrientation(rawRotationMatrix, orientationAngles)
                    }

                    val azimuthRad = orientationAngles[0]
                    val azimuthDeg = ((Math.toDegrees(azimuthRad.toDouble()) + 360.0) % 360.0).toFloat()
                    trySend(azimuthDeg)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_GAME)

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
}
