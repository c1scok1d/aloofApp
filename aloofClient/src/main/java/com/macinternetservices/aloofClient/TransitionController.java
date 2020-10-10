package com.macinternetservices.aloofClient;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionRequest;
import com.google.android.gms.location.ActivityTransitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class TransitionController extends Service {


    private final static String TAG = TransitionController.class.getSimpleName();

    // TODO: Review check for devices with Android 10 (29+).
    private boolean runningQOrLater =
            android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q;

    private boolean activityTrackingEnabled;
    private Boolean stillNotified = false;
    public static Date stillStartTime, walkStartTime, walkEndTime, runStartTime, runEndTime, bikeStartTime,
            bikeEndTime, driveStartTime, driveEndTime, lastTransitionStartTime, lastTransitionEndTime;
    ;

    private List<ActivityTransition> activityTransitionList;

    // Action fired when transitions are triggered.
    private final String TRANSITIONS_RECEIVER_ACTION =
            "com.macinternetservices.aloofClient" + "TRANSITIONS_RECEIVER_ACTION";

    private PendingIntent mActivityTransitionsPendingIntent;
    private TransitionsReceiver mTransitionsReceiver;
    public static ArrayList walkPoints = new ArrayList();
    public static ArrayList runPoints = new ArrayList();
    public static ArrayList bikePoints = new ArrayList();
    public static ArrayList drivePoints = new ArrayList<>();
    public static ArrayList walkSpeed = new ArrayList();
    public static ArrayList runSpeed = new ArrayList();
    public static ArrayList bikeSpeed = new ArrayList();
    public static ArrayList driveSpeed = new ArrayList<>();
    String trackedDevice, deviceId = "foo";


    private static String toActivityString(int activity) {
        switch (activity) {
            case DetectedActivity.STILL:
                return "STILL";
            case DetectedActivity.WALKING:
                return "WALKING";
            default:
                return "UNKNOWN";
        }
    }

    private static String toTransitionType(int transitionType) {
        switch (transitionType) {
            case ActivityTransition.ACTIVITY_TRANSITION_ENTER:
                return "ENTER";
            case ActivityTransition.ACTIVITY_TRANSITION_EXIT:
                return "EXIT";
            default:
                return "UNKNOWN";
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //super.onCreate(savedInstanceState);

        /*setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mLogFragment =
                (LogFragment) getSupportFragmentManager().findFragmentById(R.id.log_fragment);*/

        activityTrackingEnabled = false;

        // List of activity transitions to track.
        activityTransitionList = new ArrayList<>();

        // TODO: Add activity transitions to track.
        activityTransitionList.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.WALKING)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build());

        activityTransitionList.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.WALKING)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                        .build());

        activityTransitionList.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.STILL)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build());

        activityTransitionList.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.STILL)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                        .build());

        activityTransitionList.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.RUNNING)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build());

        activityTransitionList.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.RUNNING)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                        .build());

        activityTransitionList.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.IN_VEHICLE)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build());

        activityTransitionList.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.IN_VEHICLE)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                        .build());

        activityTransitionList.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.ON_BICYCLE)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build());

        activityTransitionList.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.ON_BICYCLE)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                        .build());

        // TODO: Initialize PendingIntent that will be triggered when a activity transition occurs.
        Intent transitionPendingintent = new Intent(TRANSITIONS_RECEIVER_ACTION);
        mActivityTransitionsPendingIntent =
                PendingIntent.getBroadcast(this, 0, transitionPendingintent, 0);

        // TODO: Create a BroadcastReceiver to listen for activity transitions.
        // The receiver listens for the PendingIntent above that is triggered by the system when an
        // activity transition occurs.
        mTransitionsReceiver = new TransitionsReceiver();
        transitionMonitor();
        Log.e(TAG,"Client Initialized.");

        // TODO: Register the BroadcastReceiver to listen for activity transitions.
        registerReceiver(mTransitionsReceiver, new IntentFilter(TRANSITIONS_RECEIVER_ACTION));

        return Service.START_NOT_STICKY;
    }

    /*@Override
    protected void onPause() {

        // TODO: Disable activity transitions when user leaves the app.
       /* if (activityTrackingEnabled) {
            disableActivityTransitions();
        }
        super.onPause();
    }


    @Override
    protected void onStop() {

        // TODO: Unregister activity transition receiver when user leaves the app.
        //unregisterReceiver(mTransitionsReceiver);

        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // Start activity recognition if the permission was approved.
        if (activityRecognitionPermissionApproved() && !activityTrackingEnabled) {
            enableActivityTransitions();
        }

        super.onActivityResult(requestCode, resultCode, data);
    } */

    /**
     * Registers callbacks for {@link ActivityTransition} events via a custom
     * {@link BroadcastReceiver}
     */
    private void enableActivityTransitions() {

        Log.e(TAG, "enableActivityTransitions()");


        // TODO: Create request and listen for activity changes.
        ActivityTransitionRequest request = new ActivityTransitionRequest(activityTransitionList);

        // Register for Transitions Updates.
        Task<Void> task = ActivityRecognition.getClient(this)
                .requestActivityTransitionUpdates(request, mActivityTransitionsPendingIntent);

        task.addOnSuccessListener(
                new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        activityTrackingEnabled = true;
                        Log.e(TAG,"Transitions Api was successfully registered.");

                    }
                });

        task.addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //printToScreen("Transitions Api could NOT be registered: " + e);
                        Log.e(TAG, "Transitions Api could NOT be registered: " + e);

                    }
                });
    }


    /**
     * Unregisters callbacks for {@link ActivityTransition} events via a custom
     * {@link BroadcastReceiver}
     */
   /* private void disableActivityTransitions() {

        Log.e(TAG, "disableActivityTransitions()");


        // TODO: Stop listening for activity changes.
        ActivityRecognition.getClient(this).removeActivityTransitionUpdates(mActivityTransitionsPendingIntent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        activityTrackingEnabled = false;
                        Log.e(TAG, "Transitions successfully unregistered.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //printToScreen("Transitions could not be unregistered: " + e);
                        Log.e(TAG, "Transitions could not be unregistered: " + e);
                    }
                });
    } */

    /**
     * On devices Android 10 and beyond (29+), you need to ask for the ACTIVITY_RECOGNITION via the
     * run-time permissions.
     */
    private boolean activityRecognitionPermissionApproved() {

        // TODO: Review permission check for 29+.
        if (runningQOrLater) {

            return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACTIVITY_RECOGNITION
            );
        } else {
            return true;
        }
    }


    private void transitionMonitor() {

        // TODO: Enable/Disable activity tracking and ask for permissions if needed.
        if (activityRecognitionPermissionApproved()) {
            if (!activityTrackingEnabled) {
                enableActivityTransitions();
            }

        } else {
            // Request permission and start activity for result. If the permission is approved, we
            // want to make sure we start activity recognition tracking.
            Intent startIntent = new Intent(this, PermissionRationalActivity.class);
            startActivity(startIntent);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Handles intents from from the Transitions API.
     */
    public class TransitionsReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            Log.e(TAG, "onReceive(): " + intent);

            if (!TextUtils.equals(TRANSITIONS_RECEIVER_ACTION, intent.getAction())) {

                Log.e(TAG,"Received an unsupported action in TransitionsReceiver: action = " +
                        intent.getAction());
                return;
            }

            // TODO: Extract activity transition information from listener.
            if (ActivityTransitionResult.hasResult(intent)) {
                ActivityTransitionResult result = ActivityTransitionResult.extractResult(intent);
                for (ActivityTransitionEvent event : result.getTransitionEvents()) {
                    //Log.e(TAG, "Transition: " +event.getActivityType());
                    // chronological sequence of events....
                    if (event.getActivityType() == DetectedActivity.WALKING && event.getTransitionType() == ActivityTransition.ACTIVITY_TRANSITION_ENTER) {
                        walkStartTime = Calendar.getInstance().getTime();
                        //Date lastTransitionStartTime = walkStartTime;
                        transitionStartNotification(context, trackedDevice + " is walking", deviceId);

                        LocationManager locManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                        LocationListener locListener = new LocationListener() {
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
                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1, locListener);
                        stillNotified = false;
                        transitionStartNotification(context, trackedDevice + " is walking", deviceId);
                    } else if (event.getActivityType() == DetectedActivity.WALKING && event.getTransitionType() == ActivityTransition.ACTIVITY_TRANSITION_EXIT) {
                        walkEndTime = Calendar.getInstance().getTime();
                        //Date lastTransitionEndTime = walkEndTime;
                        //Double min = (Double) Collections.min(walkSpeed);
                        Double max = (Double) Collections.max(walkSpeed);
                        /*
                        add walkStartTime, walkEndTime and walkPoints array to rooms db named transitions
                        only store data for last 5 transitions in db
                         */
                        if (walkStartTime != null && walkEndTime != null) {
                            transitionExitNotification(context, trackedDevice + " stopped walking!", " Max Speed: " + String.format("%.0f", max) + "MPH", walkStartTime, walkEndTime);
                        }
                    } else if (event.getActivityType() == DetectedActivity.RUNNING && event.getTransitionType() == ActivityTransition.ACTIVITY_TRANSITION_ENTER) {
                        LocationManager locManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                        runStartTime = Calendar.getInstance().getTime();
                        Date lastTransitionStartTime = runStartTime;
                        transitionStartNotification(context, trackedDevice + " is running", deviceId);
                        stillNotified = false;

                        LocationListener locListener = new LocationListener() {
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
                                runPoints.add(new LatLng(location.getLatitude(), location.getLongitude()));
                                runSpeed.add(location.getSpeed() * 1.15078);
                            }
                        };
                        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1, locListener);
                    } else if (event.getActivityType() == DetectedActivity.RUNNING && event.getTransitionType() == ActivityTransition.ACTIVITY_TRANSITION_EXIT) {
                        runEndTime = Calendar.getInstance().getTime();
                        //lastTransitionEndTime = runEndTime;
                        //Double min = (Double) Collections.min(runSpeed);
                        Double max = (Double) Collections.max(runSpeed);
                        if (runStartTime != null && runEndTime != null) {
                            transitionExitNotification(context, trackedDevice + " stopped running!", " Max Speed: " + String.format("%.0f", max) + "MPH", runStartTime, runEndTime);
                        } else {
                            //do stuff
                        }
                    } else if (event.getActivityType() == DetectedActivity.IN_VEHICLE && event.getTransitionType() == ActivityTransition.ACTIVITY_TRANSITION_ENTER) {
                        driveStartTime = Calendar.getInstance().getTime();
                        //lastTransitionStartTime = driveStartTime;
                        transitionStartNotification(context, trackedDevice + " is driving", deviceId);
                        stillNotified = false;

                        LocationManager locManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                        LocationListener locListener = new LocationListener() {
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
                    } else if (event.getActivityType() == DetectedActivity.IN_VEHICLE && event.getTransitionType() == ActivityTransition.ACTIVITY_TRANSITION_EXIT) {
                        driveEndTime = Calendar.getInstance().getTime();
                        //lastTransitionEndTime = driveEndTime;
                        //Double min = (Double) Collections.min(driveSpeed);
                        Double max = (Double) Collections.max(driveSpeed);
                        if (driveStartTime != null && driveEndTime != null) {
                            transitionExitNotification(context, trackedDevice + " stopped driving!", " Max Speed: " + String.format("%.0f", max) + "MPH", driveStartTime, driveEndTime);
                        } else {
                            //do stuff
                        }
                    } else if (event.getActivityType() == DetectedActivity.ON_BICYCLE && event.getTransitionType() == ActivityTransition.ACTIVITY_TRANSITION_ENTER) {
                        bikeStartTime = Calendar.getInstance().getTime();
                        //lastTransitionStartTime = bikeStartTime;
                        transitionStartNotification(context, trackedDevice + " is biking", deviceId);
                        stillNotified = false;

                        LocationManager locManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                        LocationListener locListener = new LocationListener() {
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
                                bikePoints.add(new LatLng(location.getLatitude(), location.getLongitude()));
                                bikeSpeed.add(location.getSpeed() * 1.15078);
                            }
                        };
                        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1, locListener);
                    } else if (event.getActivityType() == DetectedActivity.ON_BICYCLE && event.getTransitionType() == ActivityTransition.ACTIVITY_TRANSITION_EXIT) {
                        bikeEndTime = Calendar.getInstance().getTime();
                        //lastTransitionEndTime = bikeEndTime;
                        //Double min = (Double) Collections.min(bikeSpeed);
                        Double max = (Double) Collections.max(bikeSpeed);
                        if (bikeStartTime != null && bikeEndTime != null) {
                            transitionExitNotification(context, trackedDevice + " stopped biking", " Max Speed: " + String.format("%.0f", max) + "MPH", bikeStartTime, bikeEndTime);
                        } else {
                            //do stuff
                        }
                    } else if (event.getActivityType() == DetectedActivity.STILL && event.getTransitionType() == ActivityTransition.ACTIVITY_TRANSITION_ENTER) {
                        LocationManager locManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                        LocationListener locListener = new LocationListener() {
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
                                final String[] knownName = {null};
                                final String[] bldgno = {null};
                                final String[] street = {null};
                                geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                                try {
                                    addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                                    bldgno[0] = addresses.get(0).getSubThoroughfare(); // building number
                                    street[0] = addresses.get(0).getThoroughfare(); //street name
                                    knownName[0] = addresses.get(0).getFeatureName(); // Only if available else return NULL
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                if (!stillNotified) {
                                    if (!bldgno[0].equals(knownName[0])) {
                                        stillStartTime = Calendar.getInstance().getTime();
                                        transitionStillNotification(getApplicationContext(), trackedDevice + " is at " + bldgno[0] + " " + street[0] + " (" + knownName[0] + ")");
                                        stillNotified = true;
                                    } else {
                                        stillStartTime = Calendar.getInstance().getTime();
                                        transitionStillNotification(getApplicationContext(), trackedDevice + " is at " + bldgno[0] + " " + street[0]);
                                        stillNotified = true;
                                    }
                                }
                            }
                        };
                        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1, locListener);

                    } else if (event.getActivityType() == DetectedActivity.STILL && event.getTransitionType() == ActivityTransition.ACTIVITY_TRANSITION_EXIT) {
                        // do stuff
                    }
                }
            };
        }

        private void transitionStartNotification(final Context mContext,final String message, String deviceId){
            createNotificationChannel(mContext);

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
            /*Intent notificationIntent = new Intent(mContext, RouteActivity.class); //start route activity put start/end time as intent extras
            Points foo = new Points(fmt.format(lastTransitionEndTime),fmt.format(lastTransitionStartTime), deviceId);
            Bundle transitionDataBundle = new Bundle();
            transitionDataBundle.putString("lastTransitionEndTime", lastTransitionEndTime.toString());
            transitionDataBundle.putString("lastTransitionStartTime", lastTransitionStartTime.toString());
            transitionDataBundle.putString("deviceId", deviceId);
            notificationIntent.putExtras(transitionDataBundle);
            notificationIntent.putExtra("transitionData", foo);

            PendingIntent pendingIntent = PendingIntent.getActivity(mContext,
                    0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT); */
            Notification notification = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                    .setContentTitle(message)
                    .setContentText(message2)
                    .setContentText("Tap for route...")
                    .setSmallIcon(R.mipmap.ic_logo)
                   // .setContentIntent(pendingIntent)
                    .build();
            NotificationManager notifManager =
                    (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            notifManager.notify(new Random().nextInt(), notification);
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
}
