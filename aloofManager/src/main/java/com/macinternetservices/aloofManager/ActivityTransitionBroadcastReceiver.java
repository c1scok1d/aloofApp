package com.macinternetservices.aloofManager;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.maps.model.LatLng;
import com.macinternetservices.aloofManager.model.Points;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import static com.macinternetservices.aloofManager.MainFragment.trackedDevice;
import static com.macinternetservices.aloofManager.MainFragment.tracking;

public class ActivityTransitionBroadcastReceiver extends BroadcastReceiver {

    public static final String INTENT_ACTION = "org.traccar.manager" +
            ".ACTION_PROCESS_ACTIVITY_TRANSITIONS";

    private static LocationManager locManager;
    private static LocationListener locListener;
    public static ArrayList walkPoints = new ArrayList();
    public static ArrayList runPoints = new ArrayList();
    public static ArrayList bikePoints = new ArrayList();
    public static ArrayList drivePoints = new ArrayList<>();
    public static ArrayList walkSpeed = new ArrayList();
    public static ArrayList runSpeed = new ArrayList();
    public static ArrayList bikeSpeed = new ArrayList();
    public static ArrayList driveSpeed = new ArrayList<>();
    Boolean stillNotified = false;
    public static Date stillStartTime, walkStartTime, walkEndTime, runStartTime, runEndTime, bikeStartTime,
    bikeEndTime, driveStartTime, driveEndTime, lastTransitionStartTime, lastTransitionEndTime;;
    String deviceId = "1694";

    //get deviceId
    //String deviceId = whatever.getDeviceId

    final String[] knownName = {null};
    final String[] bldgno = {null};
    final String[] street = {null};
    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("ActivityTransition","Transition Received");
        if (intent != null && INTENT_ACTION.equals(intent.getAction())) {
            if (ActivityTransitionResult.hasResult(intent)) {
                ActivityTransitionResult intentResult = ActivityTransitionResult
                        .extractResult(intent);
                // handle activity transition result ...
                for (ActivityTransitionEvent event : intentResult.getTransitionEvents()) {
                    // chronological sequence of events....
                    if (event.getActivityType() == DetectedActivity.WALKING && event.getTransitionType() == ActivityTransition.ACTIVITY_TRANSITION_ENTER){
                        walkStartTime = Calendar.getInstance().getTime();
                        //Date lastTransitionStartTime = walkStartTime;
                        transitionStartNotification(context,trackedDevice+" is walking", deviceId);

                        locManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                        locListener = new LocationListener() {
                            @Override
                            public void onStatusChanged(String provider, int status,
                                                        Bundle extras) {
                            }

                            @Override
                            public void onProviderEnabled(String provider) {
                            }

                            @Override
                            public void onProviderDisabled(String provider) {
                            }
                            @Override
                            public void onLocationChanged(Location location) {
                                walkPoints.add(new LatLng(location.getLatitude(), location.getLongitude()));
                                walkSpeed.add(location.getSpeed() * 1.15078);

                            }
                        };
                        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1, locListener);
                        stillNotified = false;
                    } else if (event.getActivityType() == DetectedActivity.WALKING && event.getTransitionType() == ActivityTransition.ACTIVITY_TRANSITION_EXIT){
                        walkEndTime = Calendar.getInstance().getTime();
                        //Date lastTransitionEndTime = walkEndTime;
                        //Double min = (Double) Collections.min(walkSpeed);
                        Double max = (Double) Collections.max(walkSpeed);
                        /*
                        add walkStartTime, walkEndTime and walkPoints array to rooms db named transitions
                        only store data for last 5 transitions in db
                         */
                        if(walkStartTime != null && walkEndTime != null) {
                            transitionExitNotification(context, trackedDevice + " stopped walking!", " Max Speed: " + String.format("%.0f", max) + "MPH", walkStartTime, walkEndTime);
                        } else {
                            //do stuff
                        }
                    } else if (event.getActivityType() == DetectedActivity.RUNNING && event.getTransitionType() == ActivityTransition.ACTIVITY_TRANSITION_ENTER){
                        locManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                        runStartTime = Calendar.getInstance().getTime();
                        Date lastTransitionStartTime = runStartTime;
                        transitionStartNotification(context,trackedDevice+" is running", deviceId);
                        stillNotified = false;

                        locListener = new LocationListener() {
                            @Override
                            public void onStatusChanged(String provider, int status,
                                                        Bundle extras) {
                            }

                            @Override
                            public void onProviderEnabled(String provider) {
                            }

                            @Override
                            public void onProviderDisabled(String provider) {
                            }
                            @Override
                            public void onLocationChanged(Location location) {
                                runPoints.add(new LatLng(location.getLatitude(), location.getLongitude()) );
                                runSpeed.add(location.getSpeed() * 1.15078);
                            }
                        };
                        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1, locListener);
                    } else if (event.getActivityType() == DetectedActivity.RUNNING && event.getTransitionType() == ActivityTransition.ACTIVITY_TRANSITION_EXIT){
                        runEndTime = Calendar.getInstance().getTime();
                        //lastTransitionEndTime = runEndTime;
                        //Double min = (Double) Collections.min(runSpeed);
                        Double max = (Double) Collections.max(runSpeed);
                        if(runStartTime != null && runEndTime != null) {
                            transitionExitNotification(context, trackedDevice + " stopped running!", " Max Speed: " + String.format("%.0f", max) + "MPH", runStartTime, runEndTime);
                        } else {
                            //do stuff
                        }
                    } else if (event.getActivityType() == DetectedActivity.IN_VEHICLE && event.getTransitionType() == ActivityTransition.ACTIVITY_TRANSITION_ENTER){
                        driveStartTime = Calendar.getInstance().getTime();
                        //lastTransitionStartTime = driveStartTime;
                        transitionStartNotification(context,trackedDevice+" is driving", deviceId);
                        stillNotified = false;

                        locManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                        locListener = new LocationListener() {
                            @Override
                            public void onStatusChanged(String provider, int status,
                                                        Bundle extras) {
                            }

                            @Override
                            public void onProviderEnabled(String provider) {
                            }

                            @Override
                            public void onProviderDisabled(String provider) {
                            }

                            @Override
                            public void onLocationChanged(Location location) {
                                drivePoints.add(new LatLng(location.getLatitude(), location.getLongitude()));
                                driveSpeed.add(location.getSpeed() * 1.15078);
                            }
                        };
                        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1, locListener);
                    } else if (event.getActivityType() == DetectedActivity.IN_VEHICLE && event.getTransitionType() == ActivityTransition.ACTIVITY_TRANSITION_EXIT){
                        driveEndTime = Calendar.getInstance().getTime();
                        //lastTransitionEndTime = driveEndTime;
                        //Double min = (Double) Collections.min(driveSpeed);
                        Double max = (Double) Collections.max(driveSpeed);
                        if(driveStartTime != null && driveEndTime != null) {
                            transitionExitNotification(context, trackedDevice + " stopped driving!", " Max Speed: " + String.format("%.0f", max) + "MPH", driveStartTime, driveEndTime);
                        } else {
                            //do stuff
                        }
                    } else if (event.getActivityType() == DetectedActivity.ON_BICYCLE && event.getTransitionType() == ActivityTransition.ACTIVITY_TRANSITION_ENTER){
                        bikeStartTime = Calendar.getInstance().getTime();
                        //lastTransitionStartTime = bikeStartTime;
                        transitionStartNotification(context,trackedDevice+" is biking", deviceId);
                        stillNotified = false;

                        locManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                        locListener = new LocationListener() {
                            @Override
                            public void onStatusChanged(String provider, int status,
                                                        Bundle extras) {
                            }

                            @Override
                            public void onProviderEnabled(String provider) {
                            }

                            @Override
                            public void onProviderDisabled(String provider) {
                            }
                            @Override
                            public void onLocationChanged(Location location) {
                                bikePoints.add(new LatLng(location.getLatitude(), location.getLongitude()) );
                                bikeSpeed.add(location.getSpeed() * 1.15078);
                            }
                        };
                        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1, locListener);
                    } else if (event.getActivityType() == DetectedActivity.ON_BICYCLE && event.getTransitionType() == ActivityTransition.ACTIVITY_TRANSITION_EXIT){
                        bikeEndTime = Calendar.getInstance().getTime();
                        //lastTransitionEndTime = bikeEndTime;
                        //Double min = (Double) Collections.min(bikeSpeed);
                        Double max = (Double) Collections.max(bikeSpeed);
                        if(bikeStartTime != null && bikeEndTime != null) {
                            transitionExitNotification(context, trackedDevice + " stopped biking", " Max Speed: " + String.format("%.0f", max) + "MPH", bikeStartTime, bikeEndTime);
                        } else {
                            //do stuff
                        }
                    } else if (event.getActivityType() == DetectedActivity.STILL && event.getTransitionType() == ActivityTransition.ACTIVITY_TRANSITION_ENTER) {
                        locManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                        locListener = new LocationListener() {
                                @Override public void onStatusChanged(String provider, int status,
                                                            Bundle extras) {
                                }

                                @Override
                                public void onProviderEnabled(String provider) {
                                }

                                @Override
                                public void onProviderDisabled(String provider) {
                                }

                                @Override
                                public void onLocationChanged(Location location) {
                                    Geocoder geocoder;
                                    List<Address> addresses;
                                    geocoder = new Geocoder(context, Locale.getDefault());
                                    try {
                                        addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                                        bldgno[0] = addresses.get(0).getSubThoroughfare(); // building number
                                        street[0] = addresses.get(0).getThoroughfare(); //street name
                                        knownName[0] = addresses.get(0).getFeatureName(); // Only if available else return NULL
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    if (!stillNotified) {
                                        if(!bldgno[0].equals(knownName[0])) {
                                            stillStartTime = Calendar.getInstance().getTime();
                                            transitionStillNotification(context, trackedDevice + " is at "+bldgno[0]+" "+street[0]+" ("+knownName[0]+")");
                                            stillNotified = true;
                                        } else {
                                            stillStartTime = Calendar.getInstance().getTime();
                                            transitionStillNotification(context, trackedDevice + " is at " + bldgno[0] + " " + street[0]);
                                            stillNotified = true;
                                        }

                                    } else if (event.getActivityType() == DetectedActivity.STILL && event.getTransitionType() == ActivityTransition.ACTIVITY_TRANSITION_EXIT) {
                                        // do stuff
                                    }
                                }
                            };
                            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1, locListener);
                    }
                }
            }
        }
    }

    private void transitionStartNotification(final Context mContext,final String message, String deviceId){
        createNotificationChannel(mContext);

        tracking = true;
        trackedDevice = deviceId;
        Intent notificationIntent = new Intent(mContext, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(mContext,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                .setContentTitle(message)
                .setContentText("Tap to track")
                .setSmallIcon(R.mipmap.ic_logo)
                .setContentIntent(pendingIntent)
                .build();
        NotificationManager notifManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notifManager.notify(new Random().nextInt(), notification);
    }
//figure out how to get deviceId

    private void transitionExitNotification(final Context mContext,final String message, final String message2, Date lastTransitionStartTime, Date lastTransitionEndTime){
        @SuppressLint("SimpleDateFormat") SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        createNotificationChannel(mContext);
        //lastTransitionStartTime = this.lastTransitionStartTime;
        //lastTransitionEndTime = this.lastTransitionEndTime;

        long different = lastTransitionEndTime.getTime() - lastTransitionStartTime.getTime();
        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long elapsedMinutes = different / minutesInMilli;

        //if(elapsedMinutes >= 3){
            Intent notificationIntent = new Intent(mContext, RouteActivity.class); //start route activity put start/end time as intent extras
            Points foo = new Points(fmt.format(lastTransitionEndTime),fmt.format(lastTransitionStartTime), deviceId);
            Bundle transitionDataBundle = new Bundle();
            transitionDataBundle.putString("lastTransitionEndTime", lastTransitionEndTime.toString());
            transitionDataBundle.putString("lastTransitionStartTime", lastTransitionStartTime.toString());
            transitionDataBundle.putString("deviceId", deviceId);
            notificationIntent.putExtras(transitionDataBundle);
            notificationIntent.putExtra("transitionData", foo);

            PendingIntent pendingIntent = PendingIntent.getActivity(mContext,
                    0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            Notification notification = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                    .setContentTitle(message)
                    .setContentText(message2)
                    .setContentText("Tap for route...")
                    .setSmallIcon(R.mipmap.ic_logo)
                    .setContentIntent(pendingIntent)
                    .build();
            NotificationManager notifManager =
                    (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            notifManager.notify(new Random().nextInt(), notification);
       /* } else {
            Notification notification = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                    .setContentTitle(message)
                    .setContentText(message2)
                    .setSmallIcon(R.mipmap.ic_logo)
                    .build();
            NotificationManager notifManager =
                    (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            notifManager.notify(new Random().nextInt(), notification);
        } */
    }

    private void transitionStillNotification(final Context mContext,final String message){
        createNotificationChannel(mContext);
            Notification notification = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                    .setContentTitle(message)
                    .setContentText("Swipe to close")
                    .setSmallIcon(R.mipmap.ic_logo)
                    .build();
            NotificationManager notifManager =
                    (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            notifManager.notify(new Random().nextInt(), notification);
    }

    public static final String CHANNEL_ID = "Transition Channel";
    private void createNotificationChannel(final Context mContext) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Transition Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = mContext.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
}
