package com.macinternetservices.aloofManager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import com.macinternetservices.aloofManager.R;
import java.util.Random;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.getErrorCode());
            Log.e("addGeofences","E"+errorMessage);
            return;
        }
        int geofenceTransition = geofencingEvent.getGeofenceTransition();
//        geofencingEvent.getTriggeringLocation().get
        // Test that the reported transition was of interest.
        Log.e("addGeofences","S");
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ) {
            Log.e("addGeofences","E");
            sendNotification(context);

        }
    }

    public void sendNotification(final Context mContext){
        createNotificationChannel(mContext);
        Intent notificationIntent = new Intent(mContext, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                .setContentTitle(mContext.getResources().getString(R.string.app_name))
                //.setContentText("You are In")
                .setSmallIcon(R.mipmap.ic_logo)
                .setContentIntent(pendingIntent)
                .build();
        NotificationManager notifManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notifManager.notify(new Random().nextInt(), notification);
    }

    public static final String CHANNEL_ID = "Location Channel";
    private void createNotificationChannel(final Context mContext) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Location Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = mContext.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

}
