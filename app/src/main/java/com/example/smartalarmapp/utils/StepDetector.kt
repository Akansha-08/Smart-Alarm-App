package com.example.smartalarmapp.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class StepDetector(
    private val context: Context,
    private val requiredSteps: Int,              // steps needed to dismiss alarm
    private val onStepCountChanged: (Int) -> Unit, // callback to update UI with current steps
    private val onGoalReached: () -> Unit          // callback when required steps are walked
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var stepCountAtStart: Int = -1         // baseline step count when alarm starts
    private var currentSteps: Int = 0              // steps walked since alarm started

    // start listening to step counter sensor
    fun start() {
        // STEP_COUNTER = cumulative steps since last reboot (more accurate than STEP_DETECTOR)
        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepSensor != null) {
            sensorManager.registerListener(
                this,
                stepSensor,
                SensorManager.SENSOR_DELAY_FASTEST  // fastest updates for responsiveness
            )
        }
    }

    // stop listening to sensor - call when alarm dismissed
    fun stop() {
        sensorManager.unregisterListener(this)
    }

    // called every time sensor reports new step count
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val totalStepsSinceReboot = event.values[0].toInt()

            // first reading - set baseline so we count from 0
            if (stepCountAtStart == -1) {
                stepCountAtStart = totalStepsSinceReboot
            }

            // steps walked since alarm started
            currentSteps = totalStepsSinceReboot - stepCountAtStart

            // update UI with current step count
            onStepCountChanged(currentSteps)

            // check if goal reached
            if (currentSteps >= requiredSteps) {
                onGoalReached()  // trigger dismiss
            }
        }
    }

    override fun onAccuracyChanged(sensor: android.hardware.Sensor?, accuracy: Int) {
        // not needed but must be implemented
    }
}