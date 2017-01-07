package com.gfabrego.androidthingshelloworld

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.android.things.contrib.driver.bmx280.Bmx280SensorDriver
import com.google.android.things.contrib.driver.ht16k33.AlphanumericDisplay
import java.io.IOException

class TemperatureActivity : AppCompatActivity() {

    companion object {
        private val TAG = TemperatureActivity.javaClass.simpleName
        private val BUS_NAME = "I2C1"
    }

    private lateinit var sensorManager: SensorManager
    private lateinit var dynamicSensorCallback: SensorManager.DynamicSensorCallback
    private lateinit var temperatureListener : SensorEventListener
    private lateinit var environmentSensorDriver : Bmx280SensorDriver
    private lateinit var numericDisplay : AlphanumericDisplay

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        initializeDisplay()
        initializeSensorDriver()
        initializeSensorCallback()
        initializeTemperatureListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        // TODO 07/01/2017: add required try / catch
        sensorManager.unregisterListener(temperatureListener)
        sensorManager.unregisterDynamicSensorCallback(dynamicSensorCallback)
        environmentSensorDriver.close()
        numericDisplay.clear()
        numericDisplay.setEnabled(false)
        numericDisplay.close()
    }

    private fun initializeDisplay() {
        try {
            numericDisplay = AlphanumericDisplay(BUS_NAME)
            numericDisplay.setEnabled(true)
            numericDisplay.clear()
        } catch (e: IOException) {
            Log.e(TAG, "Error initializing display", e)
            // TODO 07/01/2017: display error using a LED or throw runtime exception
        }
    }

    private fun initializeSensorDriver() {
        try {
            environmentSensorDriver = Bmx280SensorDriver(BUS_NAME)
            environmentSensorDriver.registerTemperatureSensor()
        } catch (e: IOException) {
            Log.e(TAG, "Error initializing sensor driver", e)
            // TODO 07/01/2017: display error using a LED or throw runtime exception
        }
    }

    private fun initializeTemperatureListener() {
        temperatureListener = object: SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // TODO 07/01/2017
            }

            override fun onSensorChanged(event: SensorEvent?) {
                val lastTemperature = event?.values?.first()
                showTemperature(lastTemperature)
            }
        }
    }

    private fun showTemperature(lastTemperature: Float?) {
        if (lastTemperature != null) {
            // TODO 07/01/2017: display just one decimal
            numericDisplay.display(lastTemperature.toDouble())
        }
    }

    private fun initializeSensorCallback() {
        dynamicSensorCallback = object: SensorManager.DynamicSensorCallback() {
            override fun onDynamicSensorConnected(sensor: Sensor?) {
                if (sensor != null) {
                    when (sensor.type) {
                        Sensor.TYPE_AMBIENT_TEMPERATURE -> registerTemperatureListener(sensor)
                        else -> super.onDynamicSensorConnected(sensor)
                    }
                }
                super.onDynamicSensorConnected(sensor)
            }
        }
        sensorManager.registerDynamicSensorCallback(dynamicSensorCallback)
    }

    private fun registerTemperatureListener(sensor: Sensor) {
        sensorManager.registerListener(temperatureListener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
    }
}