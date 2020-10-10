/*
 * Copyright 2012 - 2019 Anton Tananaev (anton@traccar.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.macinternetservices.aloofClient;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import androidx.core.content.ContextCompat;

import android.os.PowerManager;
import android.util.Log;

public class TransitionService extends Service {

    private static final String TAG = TransitionService.class.getSimpleName();

    private PowerManager.WakeLock wakeLock;
    public static boolean transitionClientRunning = false;

    @SuppressLint("WakelockTimeout")
    @Override
    public void onCreate() {
        StatusActivity.addMessage(getString(R.string.status_service_create));

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED); {

            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
            wakeLock.acquire();

            Intent transitionService = new Intent(this,TransitionController.class);
            transitionService.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(transitionService);
            transitionClientRunning = true;
            Log.e(TAG, "service create");
            //addTransitionAlerts();
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            //ContextCompat.startForegroundService(this, new Intent(this, HideNotificationService.class));
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            AutostartReceiver.completeWakefulIntent(intent);
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "service destroy");
        StatusActivity.addMessage(getString(R.string.status_service_destroy));

        stopForeground(true);

        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        /*if (trackingController != null) {
            trackingController.stop();
        }
        clientRunning = false; */
    }
}
