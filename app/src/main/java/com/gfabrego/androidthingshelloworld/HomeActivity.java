package com.gfabrego.androidthingshelloworld;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;
import java.io.IOException;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "HomeActivity";
    private static final String GPIO_PIN_NAME = "BCM6";
    private static final long BLINK_INTERVAL = 1000;

    private Handler handler = new Handler();
    private Gpio ledGpio;
    private Runnable blinkRunnable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeBlinkRunnable();
        connectPeripheral();
        startPeripheralBlink();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(blinkRunnable);
        if (ledGpio != null) {
            try {
                ledGpio.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing peripheral", e);
            }
        }
    }

    private void initializeBlinkRunnable() {
        blinkRunnable = new Runnable() {
            @Override
            public void run() {
                if (ledGpio == null) {
                    return;
                }
                try {
                    ledGpio.setValue(!ledGpio.getValue());
                    handler.postDelayed(blinkRunnable, BLINK_INTERVAL);
                } catch (IOException e) {
                    Log.e(TAG, "Error updating peripheral value", e);
                }
            }
        };
    }

    private void connectPeripheral() {
        PeripheralManagerService service = new PeripheralManagerService();
        Log.d(TAG, "Available GPIO:" + service.getGpioList());

        try {
            ledGpio = service.openGpio(GPIO_PIN_NAME);
            ledGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

        } catch (IOException e) {
            Log.e(TAG, "Error opening pheriperal", e);
        }
    }

    private void startPeripheralBlink() {
        handler.postDelayed(blinkRunnable, BLINK_INTERVAL);
    }
}
