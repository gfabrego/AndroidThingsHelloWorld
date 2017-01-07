package com.gfabrego.androidthingshelloworld

import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.PeripheralManagerService
import java.io.IOException

class HomeActivity : AppCompatActivity() {
    companion object {
        private val TAG = "HomeActivity"
        private val GPIO_PIN_NAME = "BCM6"
        private val BLINK_INTERVAL: Long = 1000
    }

    private val handler = Handler()
    lateinit var ledGpio: Gpio
    lateinit var blinkRunnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeBlinkRunnable()
        connectPeripheral()
        startPeripheralBlink()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(blinkRunnable)
        try {
            ledGpio.close()
        } catch (e: IOException) {
            Log.e(TAG, "Error closing peripheral", e)
        }
    }

    private fun initializeBlinkRunnable() {
        blinkRunnable = Runnable {
            try {
                ledGpio.value = !ledGpio.value
                handler.postDelayed(blinkRunnable, BLINK_INTERVAL)
            } catch (e: IOException) {
                Log.e(TAG, "Error updating peripheral value", e)
            }
        }
    }

    private fun connectPeripheral() {
        val service = PeripheralManagerService()
        Log.d(TAG, "Available GPIO:" + service.gpioList)

        try {
            ledGpio = service.openGpio(GPIO_PIN_NAME)
            ledGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
        } catch (e: IOException) {
            Log.e(TAG, "Error opening pheriperal", e)
        }
    }

    private fun startPeripheralBlink() {
        handler.postDelayed(blinkRunnable, BLINK_INTERVAL)
    }
}
