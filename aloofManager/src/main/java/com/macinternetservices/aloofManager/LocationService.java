package com.macinternetservices.aloofManager;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.macinternetservices.aloofManager.model.GeoLoc;
import com.macinternetservices.aloofManager.room.DatabaseClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import com.macinternetservices.aloofManager.R;
import java.util.ArrayList;
import java.util.Random;

import static com.macinternetservices.aloofManager.MainFragment.trackedDevice;

public class LocationService extends Service {

    private static String LOG_TAG = "LocationService";
    private IBinder mBinder = new MyBinder();

    private int interval = 30000;
    public static Context mContext;

    Handler hnd;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        hnd = new Handler();
    }

    boolean startNotify = false;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("Monitoring","Geofence Monitoring Enabled");
        hnd.postDelayed(rnb,100);
        return START_NOT_STICKY;
    }

    public static final String CHANNEL_ID = "Location Channel";
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Location Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }



    @Override
    public IBinder onBind(Intent intent) {
        Log.v(LOG_TAG, "in onBind");
        return mBinder;
    }
    @Override
    public void onRebind(Intent intent) {
        Log.v(LOG_TAG, "in onRebind");
        super.onRebind(intent);
    }
    @Override
    public boolean onUnbind(Intent intent) {
        Log.v(LOG_TAG, "in onUnbind");
        return true;
    }
    @Override
    public void onDestroy() {
        try {
            hnd.removeCallbacks(rnb);
        } catch (Exception e){}

        super.onDestroy();
        Log.v(LOG_TAG, "in onDestroy");
    }

    public class MyBinder extends Binder {

    }

    Runnable rnb = new Runnable() {
        @Override
        public void run() {
            initiateFused();
        }
    };

    private FusedLocationProviderClient ff;

    @SuppressLint("MissingPermission")
    private void initiateFused(){
        ff = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest();
        ff.requestLocationUpdates(locationRequest,locationCallback, Looper.getMainLooper());
        //Log.e("ttt","initiated");
    }

    private LocationRequest locationRequest;

    private void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(500);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            //Log.e("ttt","result");
            if (locationResult == null) {
                return;
            }
            //Log.e("ttt","resultS");
            location = locationResult.getLocations().get(0);


            ff.removeLocationUpdates(locationCallback);

            GetGeoLoc getGeoLoc = new GetGeoLoc();
            getGeoLoc.execute();

            hnd.postDelayed(rnb,interval);


        };
    };

    private Location location;

    public ArrayList<GeoLoc> listData = new ArrayList<>();
    boolean enterAlert, exitAlert = false;

    class GetGeoLoc extends AsyncTask<Void,Void, ArrayList<GeoLoc>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                listData.clear();
            } catch (Exception e){}
        }

        @Override
        protected ArrayList<GeoLoc> doInBackground(Void... voids) {
            listData.addAll(DatabaseClient
                    .getInstance(LocationService.this)
                    .getAppDatabase()
                    .dataDao()
                    .getAll());
            return listData;
        }

        @Override
        protected void onPostExecute(ArrayList<GeoLoc> data) {
            super.onPostExecute(data);

            Double latitude = location.getLatitude();
            Double longitude = location.getLongitude();


            for(int i=0;i<data.size();i++) {
                GeoLoc geoLoc = data.get(i);

                if (distance(latitude, longitude, geoLoc.getLatitude(), geoLoc.getLongitude()) <= 500) {
                    geoLocation = geoLoc;
                    UpdateGeoLoc updateGeoLoc = new UpdateGeoLoc();
                    updateGeoLoc.execute();
                    //Log.e("Geofence Enter","Enter Checked");
                    if (/*data.get(i).getStatus().equals("1") && */ !enterAlert) {
                        enterNotification(LocationService.this, geoLoc.getName());
                        enterAlert = true;
                        exitAlert = false;
                    }
                } else if (distance(latitude, longitude, geoLoc.getLatitude(), geoLoc.getLongitude()) >= 500) {
                    geoLocation = geoLoc;
                    UpdateGeoLoc2 updateGeoLoc = new UpdateGeoLoc2();
                    updateGeoLoc.execute();
                    //Log.e("Geofence Exit","Exit Checked");
                    if (/*data.get(i).getStatus().equals("1") && */ !exitAlert) {
                        exitNotification(LocationService.this, geoLoc.getName());
                        exitAlert = true;
                        enterAlert = false;
                    }
                } else {
                    genNotification(LocationService.this, geoLoc.getName());
                }
            }
        }
    }

    private GeoLoc geoLocation;

    class UpdateGeoLoc extends AsyncTask<Void,Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            geoLocation.setStatus("1");
            DatabaseClient.getInstance(LocationService.this).getAppDatabase()
                    .dataDao()
                    .update(geoLocation);
            return null;
        }
    }

    class UpdateGeoLoc2 extends AsyncTask<Void,Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            geoLocation.setStatus("0");
            DatabaseClient.getInstance(LocationService.this).getAppDatabase()
                    .dataDao()
                    .update(geoLocation);
            return null;
        }
    }

    public static double distance(double lat1,double long1,double lat2,double long2){
        try {
            Location location1 = new Location("locationA");
            location1.setLatitude(lat1);
            location1.setLongitude(long1);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            Location location2 = new Location("locationB");
            location2.setLatitude(lat2);
            location2.setLongitude(long2);
            double distance = location1.distanceTo(location2);
            //Log.e("ttt","D"+distance);
            return distance;
        } catch (Exception e) {

            e.printStackTrace();

        }
        return 0;
    }

    public void enterNotification(final Context mContext,String name){
//        createNotificationChannel(mContext);
        Intent notificationIntent = new Intent(mContext, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                .setContentTitle("We're alerting you because")
                .setContentText(trackedDevice+" has arrived at "+name)
                .setSmallIcon(R.mipmap.ic_logo)
                .setContentIntent(pendingIntent)
                .build();
        NotificationManager notifManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notifManager.notify(new Random().nextInt(), notification);
    }
    public void exitNotification(final Context mContext,String name){
//        createNotificationChannel(mContext);
        Intent notificationIntent = new Intent(mContext, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                .setContentTitle("We're alerting you because")
                .setContentText(trackedDevice+" has left "+name)
                .setSmallIcon(R.mipmap.ic_logo)
                .setContentIntent(pendingIntent)
                .build();
        NotificationManager notifManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notifManager.notify(new Random().nextInt(), notification);
    }

    public void genNotification(final Context mContext,String name){
//        createNotificationChannel(mContext);
        Intent notificationIntent = new Intent(mContext, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                .setContentTitle("Location Alerting Enabled")
                .setContentText("Alerting locations for " +name)
                .setSmallIcon(R.mipmap.ic_logo)
                .setContentIntent(pendingIntent)
                .build();
        NotificationManager notifManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notifManager.notify(new Random().nextInt(), notification);
    }
}
