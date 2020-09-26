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
    Date lastTransitionEndTime, stillEndTime, stillStartTime, walkStartTime, walkEndTime, runStartTime, runEndTime, bikeStartTime,
    bikeEndTime, driveStartTime, driveEndTime, lastTransitionStartTime;
    String deviceId = "1051";
    Points foo;

    //get deviceId
    //String deviceId = whatever.getDeviceId

    final String[] knownName = {null};
    final String[] bldgno = {null};
    final String[] street = {null};
    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("Transition","Transition Received");
        if (intent != null && INTENT_ACTION.equals(intent.getAction())) {
            if (ActivityTransitionResult.hasResult(intent)) {
                ActivityTransitionResult intentResult = ActivityTransitionResult
                        .extractResult(intent);
                // handle activity transition result ...
                for (ActivityTransitionEvent event : intentResult.getTransitionEvents()) {
                    // chronological sequence of events....
                    if (event.getActivityType() == DetectedActivity.STILL && event.getTransitionType() == ActivityTransition.ACTIVITY_TRANSITION_ENTER) {
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
                                Geocoder geocoder;
                                List<Address> addresses;
                                geocoder = new Geocoder(context, Locale.getDefault());
                                try {
                                    addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                                    //address[0] = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                                    bldgno[0] = addresses.get(0).getSubThoroughfare(); // building number
                                    street[0] = addresses.get(0).getThoroughfare(); //street name
                                    ///city[0] = addresses.get(0).getLocality();
                                    //state[0] = addresses.get(0).getAdminArea();
                                    //country[0] = addresses.get(0).getCountryName();
                                    //postalCode[0] = addresses.get(0).getPostalCode();
                                    knownName[0] = addresses.get(0).getFeatureName(); // Only if available else return NULL
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                if (!stillNotified) {
                                    if(!bldgno[0].equals(knownName[0])) {
                                        stillStartTime = Calendar.getInstance().getTime();
                                        lastTransitionStartTime = stillStartTime;
                                        transitionStillNotification(context, trackedDevice + " is at "+bldgno[0]+" "+street[0]+" ("+knownName[0]+")");
                                        stillNotified = true;
                                    } else {
                                        stillStartTime = Calendar.getInstance().getTime();
                                        lastTransitionStartTime = stillStartTime;
                                        transitionStillNotification(context, trackedDevice + " is at " + bldgno[0] + " " + street[0]);
                                        stillNotified = true;
                                    }

                                } else if (event.getActivityType() == DetectedActivity.STILL && event.getTransitionType() == ActivityTransition.ACTIVITY_TRANSITION_EXIT) {
                                    stillEndTime = Calendar.getInstance().getTime();
                                    lastTransitionEndTime = stillEndTime;
                                    //Double min = (Double) Collections.min(walkSpeed);
                                    //Double max = (Double) Collections.max(walkSpeed);
                                    /*
                                    add walkStartTime, walkEndTime and walkPoints array to rooms db named transitions
                                    only store data for last 5 transitions in db
                                     */
                                }
                            }
                        };
                        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1, locListener);
                    } else if (event.getActivityType() == DetectedActivity.WALKING && event.getTransitionType() == ActivityTransition.ACTIVITY_TRANSITION_ENTER){
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
                        walkStartTime = Calendar.getInstance().getTime();
                        lastTransitionStartTime = walkStartTime;
                        transitionStartNotification(context,trackedDevice+" is walking", deviceId);
                        stillNotified = false;
                    } else if (event.getActivityType() == DetectedActivity.WALKING && event.getTransitionType() == ActivityTransition.ACTIVITY_TRANSITION_EXIT){
                        walkEndTime = Calendar.getInstance().getTime();
                        lastTransitionEndTime = walkEndTime;
                        //Double min = (Double) Collections.min(walkSpeed);
                        Double max = (Double) Collections.max(walkSpeed);
                        /*
                        add walkStartTime, walkEndTime and walkPoints array to rooms db named transitions
                        only store data for last 5 transitions in db
                         */
                        transitionExitNotification(context,trackedDevice+" stopped walking!"," Max Speed: "+String.format("%.0f", max)+"MPH");
                    } else if (event.getActivityType() == DetectedActivity.RUNNING && event.getTransitionType() == ActivityTransition.ACTIVITY_TRANSITION_ENTER){
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
                                runPoints.add(new LatLng(location.getLatitude(), location.getLongitude()) );
                                runSpeed.add(location.getSpeed() * 1.15078);
                            }
                        };
                        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1, locListener);
                        runStartTime = Calendar.getInstance().getTime();
                        lastTransitionStartTime = runStartTime;
                        transitionStartNotification(context,trackedDevice+" is running", deviceId);
                        stillNotified = false;
                    } else if (event.getActivityType() == DetectedActivity.RUNNING && event.getTransitionType() == ActivityTransition.ACTIVITY_TRANSITION_EXIT){
                        runEndTime = Calendar.getInstance().getTime();
                        lastTransitionEndTime = runEndTime;
                        //Double min = (Double) Collections.min(runSpeed);
                        Double max = (Double) Collections.max(runSpeed);
                        transitionExitNotification(context,trackedDevice+" stopped running!","Max Speed: "+String.format("%.0f", max) );
                    } else if (event.getActivityType() == DetectedActivity.IN_VEHICLE && event.getTransitionType() == ActivityTransition.ACTIVITY_TRANSITION_ENTER){
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
                        driveStartTime = Calendar.getInstance().getTime();
                        lastTransitionStartTime = driveStartTime;
                        transitionStartNotification(context,trackedDevice+" is driving", deviceId);
                        stillNotified = false;
                    } else if (event.getActivityType() == DetectedActivity.IN_VEHICLE && event.getTransitionType() == ActivityTransition.ACTIVITY_TRANSITION_EXIT){
                        driveEndTime = Calendar.getInstance().getTime();
                        lastTransitionEndTime = driveEndTime;
                        //Double min = (Double) Collections.min(driveSpeed);
                        Double max = (Double) Collections.max(driveSpeed);
                        transitionExitNotification(context,trackedDevice+" stopped driving!", "Max Speed: "+String.format("%.0f", max));
                    } else if (event.getActivityType() == DetectedActivity.ON_BICYCLE && event.getTransitionType() == ActivityTransition.ACTIVITY_TRANSITION_ENTER){
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
                        bikeStartTime = Calendar.getInstance().getTime();
                        lastTransitionStartTime = bikeStartTime;
                        transitionStartNotification(context,trackedDevice+" is biking", deviceId);
                        stillNotified = false;
                    } else if (event.getActivityType() == DetectedActivity.ON_BICYCLE && event.getTransitionType() == ActivityTransition.ACTIVITY_TRANSITION_EXIT){
                        bikeEndTime = Calendar.getInstance().getTime();
                        lastTransitionEndTime = bikeEndTime;
                        //Double min = (Double) Collections.min(bikeSpeed);
                        Double max = (Double) Collections.max(bikeSpeed);
                        transitionExitNotification(context,trackedDevice+" stopped biking", "Max Speed: "+String.format("%.0f", max));
                    }
                }
            }
        }
    }

    private void transitionStartNotification(final Context mContext,final String message, String deviceId){

        createNotificationChannel(mContext);
        Intent notificationIntent = new Intent(mContext, TrackActivity.class);

        Bundle transitionDataBundle = new Bundle();

        transitionDataBundle.putString("deviceId", deviceId);
        notificationIntent.putExtras(transitionDataBundle);
        notificationIntent.putExtra("transitionData", foo);

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

    private void transitionExitNotification(final Context mContext,final String message, final String message2){
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

        createNotificationChannel(mContext);
        if(lastTransitionEndTime != null && lastTransitionStartTime != null) {
            Intent notificationIntent = new Intent(mContext, RouteActivity.class); //start route activity put start/end time as intent extras
            foo = new Points(fmt.format(lastTransitionEndTime),fmt.format(lastTransitionStartTime), deviceId);
            Bundle transitionDataBundle = new Bundle();
            transitionDataBundle.putString("lastTransitionEndTime", fmt.format(lastTransitionEndTime));
            transitionDataBundle.putString("lastTransitionStartTime", fmt.format(lastTransitionStartTime));
            transitionDataBundle.putString("deviceId", deviceId);
            notificationIntent.putExtras(transitionDataBundle);
            notificationIntent.putExtra("transitionData", foo);

            PendingIntent pendingIntent = PendingIntent.getActivity(mContext,
                    0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            Notification notification = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                    .setContentTitle(message)
                    .setContentText("Tap for route")
                    .setSmallIcon(R.mipmap.ic_logo)
                    .setContentIntent(pendingIntent)
                    .build();
            NotificationManager notifManager =
                    (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            notifManager.notify(new Random().nextInt(), notification);
        }
    }

    private void transitionStillNotification(final Context mContext,final String message){
        createNotificationChannel(mContext);
        //if(lastTransitionEndTime == null && lastTransitionStartTime == null) {
            Notification notification = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                    .setContentTitle(message)
                    .setContentText("Swipe to close")
                    .setSmallIcon(R.mipmap.ic_logo)
                    .build();
            NotificationManager notifManager =
                    (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            notifManager.notify(new Random().nextInt(), notification);
        //}
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
