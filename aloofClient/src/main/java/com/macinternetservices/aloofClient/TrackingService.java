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

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.os.PowerManager;
import android.util.Log;

import java.util.Random;

public class TrackingService extends Service {

    private static final String TAG = TrackingService.class.getSimpleName();
    private static final int NOTIFICATION_ID = 1;

    private PowerManager.WakeLock wakeLock;
    private TrackingController trackingController;

    public static final String CHANNEL_ID = "Client Channel";
    private void createNotificationChannel(final Context mContext) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Client Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = mContext.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private void createNotification(Context context) {
        createNotificationChannel(context);
        Intent trackingIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                0, trackingIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("Tracking Client Enabled")
                .setContentText("Tap for settings...")
                .setSmallIcon(R.mipmap.ic_logo)
                .setContentIntent(pendingIntent)
                .build();
        NotificationManager notifManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notifManager.notify(new Random().nextInt(), notification);
    }

   public class HideNotificationService extends Service {
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public void onCreate() {
           createNotification(this);
           // stopForeground(true);
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            stopSelfResult(startId);
            return START_NOT_STICKY;
        }
    }

    @SuppressLint("WakelockTimeout")
    @Override
    public void onCreate() {
        Log.i(TAG, "service create");
        StatusActivity.addMessage(getString(R.string.status_service_create));

        createNotification(this);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
            wakeLock.acquire();

            trackingController = new TrackingController(this);
            trackingController.start();
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
        if (trackingController != null) {
            trackingController.stop();
        }
    }
}
