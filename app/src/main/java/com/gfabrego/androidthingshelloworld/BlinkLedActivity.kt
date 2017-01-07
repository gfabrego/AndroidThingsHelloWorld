package com.gfabrego.androidthingshelloworld

import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.KeyEvent
import com.google.android.things.contrib.driver.button.Button
import com.google.android.things.contrib.driver.button.ButtonInputDriver
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.PeripheralManagerService
import java.io.IOException

class BlinkLedActivity : AppCompatActivity() {
    companion object {
        private val TAG = BlinkLedActivity.javaClass.simpleName
        private val GPIO_LED_NAME = "BCM6"
        private val GPIO_BUTTON_NAME = "BCM21"
        private val BLINK_INTERVAL_1000_MS: Long = 1000
    }

    private lateinit var ledGpio: Gpio
    private lateinit var blinkRunnable: Runnable
    private lateinit var buttonInputDriver: ButtonInputDriver
    private val handler = Handler()
    private var isBlinking = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeBlinkRunnable()
        connectPeripherals()
    }

    override fun onDestroy() {
        stopLedBlink()
        closePeripherals()
        super.onDestroy()
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_SPACE) {
            toggleBlink()
            return true
        }
        return super.onKeyUp(keyCode, event)
    }

    private fun initializeBlinkRunnable() {
        blinkRunnable = Runnable {
            try {
                ledGpio.value = !ledGpio.value
                handler.postDelayed(blinkRunnable, BLINK_INTERVAL_1000_MS)
            } catch (e: IOException) {
                Log.e(TAG, "Error updating peripheral value", e)
            }
        }
    }

    private fun connectPeripherals() {
        val service = PeripheralManagerService()
        Log.d(TAG, "Available GPIO:" + service.gpioList)

        try {
            ledGpio = service.openGpio(GPIO_LED_NAME)
            ledGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
            buttonInputDriver = ButtonInputDriver(
                    GPIO_BUTTON_NAME, Button.LogicState.PRESSED_WHEN_LOW, KeyEvent.KEYCODE_SPACE)
            buttonInputDriver.register()

        } catch (e: IOException) {
            Log.e(TAG, "Error configuring pheriperals", e)
        }
    }

    private fun toggleBlink() {
        if (!isBlinking) {
            startLedBlink()
        } else {
            stopLedBlink()
        }
    }

    private fun startLedBlink() {
        handler.postDelayed(blinkRunnable, BLINK_INTERVAL_1000_MS)
        isBlinking = true
    }

    private fun stopLedBlink() {
        handler.removeCallbacks(blinkRunnable)
        ledGpio.value = false
        isBlinking = false
    }

    private fun closePeripherals() {
        buttonInputDriver.unregister()
        try {
            ledGpio.close()
            buttonInputDriver.close()
        } catch (e: IOException) {
            Log.e(TAG, "Error closing peripherals", e)
        }
    }
}
