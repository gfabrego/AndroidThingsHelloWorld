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
import java.io.RandomAccessFile
import java.math.RoundingMode
import java.text.DecimalFormat

class TemperatureActivity : AppCompatActivity() {

    companion object {
        private val TAG = TemperatureActivity::class.java.simpleName
        private val CPU_TEMPERATURE_FILE = "/sys/class/thermal/thermal_zone0/temp"
        private val BUS_NAME = "I2C1"
    }

    private lateinit var sensorManager: SensorManager
    private lateinit var dynamicSensorCallback: SensorManager.DynamicSensorCallback
    private lateinit var temperatureListener : SensorEventListener
    private lateinit var environmentSensorDriver : Bmx280SensorDriver
    private lateinit var numericDisplay : AlphanumericDisplay
    private var cpuTemperature: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        initializeDisplay()
        initializeSensorDriver()
        initializeSensorCallback()
        initializeTemperatureListener()
    }

    override fun onDestroy() {
        sensorManager.unregisterListener(temperatureListener)
        sensorManager.unregisterDynamicSensorCallback(dynamicSensorCallback)
        try {
            environmentSensorDriver.close()
        } catch (e: IOException) {
            Log.e(TAG, "Error closing sensor driver", e)
        }
        try {
            numericDisplay.clear()
            numericDisplay.setEnabled(false)
            numericDisplay.close()
        } catch (e: IOException) {
            Log.e(TAG, "Error closing numeric display", e)
        }

        super.onDestroy()
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
                // TODO 07/01/2017: show a led error if accuracy goes below a given treshold and ignore results
            }

            override fun onSensorChanged(event: SensorEvent?) {
                // TODO 15/01/2017: refactor
                val lastTemperature = event?.values?.first()
                readCpuTemperature()
                // TODO 15/01/2017: save and return last known value instead of 0 if no last value
                val valueToDisplay = calculateTemperature(lastTemperature, cpuTemperature)
                showTemperature(valueToDisplay)
            }
        }
    }

    private fun calculateTemperature(lastTemperature: Float?, cpuTemperature: Double): Double? {
        // From https://shop.pimoroni.com/products/rainbow-hat-for-android-things notes
        if (lastTemperature != null) {
            return lastTemperature - (cpuTemperature - lastTemperature)
            // Dividing by 2 as suggested in the notes doesn't seem to give correct results
            // return lastTemperature - (cpuTemperature - lastTemperature) / 2
        }
        return 0.0
    }

    private fun readCpuTemperature() {
        val cpuTemperatureFile = RandomAccessFile(CPU_TEMPERATURE_FILE, "r")
        val cpuTemperatureAsString = cpuTemperatureFile.readLine()
        if (cpuTemperatureAsString != null) {
            cpuTemperature = cpuTemperatureAsString.toDouble() / 1000
        }
        cpuTemperatureFile.close()
    }

    private fun showTemperature(temperature: Double?) {
        if (temperature != null) {
            val decimalFormat = DecimalFormat("#.0")
            decimalFormat.roundingMode = RoundingMode.UP
            val temperatureAsString = decimalFormat.format(temperature.toDouble())
            numericDisplay.display(temperatureAsString)
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