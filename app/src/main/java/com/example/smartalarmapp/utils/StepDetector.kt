package com.example.smartalarmapp.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

class StepDetector(
    private val context: Context,
    private val requiredSteps: Int,
    private val onStepCountChanged: (Int) -> Unit,
    private val onGoalReached: () -> Unit,
    private val onInvalidStep: () -> Unit = {}
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private var currentSteps: Int = 0              // steps validated and counted
    private var lastStepTime: Long = 0L            // time of last valid step
    private var lastAccelMagnitude: Float = 0f     // latest accelerometer reading

    // rolling window of last 3 - step intervals to check rhythm
    private val stepIntervals = mutableListOf<Long>()

    fun start() {
        // TYPE_STEP_DETECTOR fires once per step — lets us validate each step individually
        val stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        if (stepDetectorSensor != null) {
            sensorManager.registerListener(
                this,
                stepDetectorSensor,
                SensorManager.SENSOR_DELAY_FASTEST
            )
        }

        // accelerometer to cross-check motion at time of each step
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accelerometer != null) {
            sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_GAME
            )
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        when (event?.sensor?.type) {

            Sensor.TYPE_ACCELEROMETER -> {
                // continuously update acceleration magnitude for cross-checking
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                // subtract gravity (9.8) to get net body movement force
                lastAccelMagnitude = sqrt((x * x + y * y + z * z).toDouble()).toFloat() - 9.8f
            }

            Sensor.TYPE_STEP_DETECTOR -> {
                // fires exactly once per detected step - now we validate it

                val currentTime = System.currentTimeMillis()

                // anti-cheat 1: acceleration check
                // normal walking = 0.5-4g net force
                // tapping/shaking = >5g sudden spike
                if (Math.abs(lastAccelMagnitude) > 5f) {
                    onInvalidStep()  // too much force = not walking
                    return
                }

                // anti-cheat 2: time between steps
                // normal walking pace = one step every 200-1000ms
                // tapping = steps < 200ms apart
                val timeSinceLastStep = currentTime - lastStepTime
                if (lastStepTime != 0L && timeSinceLastStep < 200) {
                    onInvalidStep()  // too fast = not walking
                    return
                }

                // anti-cheat 3: rhythm check using rolling window
                // walking has consistent rhythm, tapping is random bursts
                if (lastStepTime != 0L) {
                    stepIntervals.add(timeSinceLastStep)
                    if (stepIntervals.size > 3) stepIntervals.removeAt(0)

                    // if we have 3 intervals, check consistency
                    if (stepIntervals.size == 3) {
                        val avg = stepIntervals.average()
                        val maxDeviation = stepIntervals.maxOf { Math.abs(it - avg) }
                        // if any interval deviates more than 800ms from average = not walking
                        if (maxDeviation > 800) {
                            onInvalidStep()  // irregular rhythm = tapping burst
                            return
                        }
                    }
                }

                // all checks passed - count as valid step
                lastStepTime = currentTime
                currentSteps++
                onStepCountChanged(currentSteps)

                if (currentSteps >= requiredSteps) {
                    onGoalReached()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: android.hardware.Sensor?, accuracy: Int) {
        // not needed
    }
}