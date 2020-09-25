/*
 * Copyright 2015 - 2016 Anton Tananaev (anton.tananaev@gmail.com)
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
package com.macinternetservices.aloofManager;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionRequest;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.macinternetservices.aloofClient.StatusActivity;
import com.macinternetservices.aloofManager.model.DataModel;
import com.macinternetservices.aloofManager.model.Device;
import com.macinternetservices.aloofManager.model.GeoLoc;
import com.macinternetservices.aloofManager.model.Position;
import com.macinternetservices.aloofManager.model.Update;
import com.macinternetservices.aloofManager.model.User;
import com.macinternetservices.aloofManager.room.DatabaseClient;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import retrofit2.Retrofit;

import static com.macinternetservices.aloofManager.MainApplication.PREFERENCE_12HR_FORMAT;
import static com.macinternetservices.aloofManager.MainApplication.PREFERENCE_SPD_UNIT;
import static com.macinternetservices.aloofManager.MainApplication.PREFERENCE_URL;

public class MainFragment extends SupportMapFragment implements OnMapReadyCallback {

    public static final int REQUEST_DEVICE = 1;
    public static final int RESULT_SUCCESS = 1;
    private final int GEOFENCE_REQ_CODE = 0;

    public static GoogleMap map;

    private Handler handler = new Handler();
    private ObjectMapper objectMapper = new ObjectMapper();

    public static Map<Long, Device> devices = new HashMap<>();
    private Map<Long, Position> positions = new HashMap<>();
    private Map<Long, Marker> markers = new HashMap<>();

    private WebSocket webSocket;
    public static Boolean tracking = false;
    public static Boolean dautle = false;
    Boolean batAlert = false;
    Boolean firstRun = true;
    Boolean transitionAlert = false;
    public static String trackedDevice;

    private ViewGroup infoWindow;
    private TextView infoTitle, infoSnippet, tvShowGeofences, tvAddGeofence, tvDelGeofence;

    private Button infoButton, btnShowRoute, btnAddGeofence;
    private ImageView iv_online, iv_offline, iv_photo;
    private OnInfoWindowElemTouchListener infoButtonListener;
    LocationManager locationManager;
    private LocationManager locManager;
    private LocationListener locListener;
    boolean flag = true;
    LatLng prev = null;
    //Polyline polyline = null;


    //private static final String RECYCLER_VIEW = "RECYCLER_VIEW_MARKER";
    //private static final String FORM_VIEW = "FORM_VIEW_MARKER";
    String speedUnit;
    String direction = "course";
    String address = "address";
    String bldgno = "bldgno";
    String street = "street";
    String city = "city";
    String state = "state";
    String postalCode = "zipcode";
    Double speed;
    String country = null;
    String knownName = null;
    Boolean twelveHourFormat = true;
    SharedPreferences preferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getMapAsync(this);

        // We want to reuse the info window for all the markers,
        // so let's create only one class member instance
        infoWindow = (ViewGroup) getLayoutInflater().inflate(R.layout.view_info, null);
        infoTitle = infoWindow.findViewById(R.id.title);
        infoSnippet = infoWindow.findViewById(R.id.details);
        btnShowRoute = infoWindow.findViewById(R.id.btnShowRoute);
        btnAddGeofence = infoWindow.findViewById(R.id.btnAddGeofence);
        iv_online = infoWindow.findViewById(R.id.iv_online);
        iv_online.setVisibility(View.GONE);
        iv_offline = infoWindow.findViewById(R.id.iv_offline);
        iv_offline.setVisibility(View.GONE);
        preferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        btnAddGeofence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getContext(), AddGeoFenceActivity.class), REQUEST_DEVICE);
            }
        });

        if (preferences.contains(PREFERENCE_SPD_UNIT)) {
            speedUnit = preferences.getString(PREFERENCE_SPD_UNIT, null);
        } else {
            preferences.edit().putString(PREFERENCE_SPD_UNIT, "mph").apply();
            speedUnit = preferences.getString(PREFERENCE_SPD_UNIT, null);
        }

        if (!preferences.contains(PREFERENCE_12HR_FORMAT)) {
            preferences.edit().putBoolean(PREFERENCE_12HR_FORMAT, true).apply();
        }
        startClient(getContext());
    }

    private void startClient(Context context){
        try {
            /*
            start client MainActivity without showing preferences if client not already running
             */
            Intent clientIntent = new Intent(context, Class.forName("com.macinternetservices.aloofClient.MainActivity"));
                    /*
                    ...avoid setting flags as it will interfere with normal flow of event and history stack.
                    find a better way to do this
                    */
            clientIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(clientIntent );
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_devices:
                startActivityForResult(new Intent(getContext(), DevicesActivity.class), REQUEST_DEVICE);
                return true;

            case R.id.action_client:
                try {
                    Intent clientIntent = new Intent(getContext(),Class.forName("com.macinternetservices.aloofClient.MainActivity"));
                    /*
                    show client preferences without starting MainActivity
                    */
                    clientIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(clientIntent );
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                return true;

            case R.id.action_status:
                startActivityForResult(new Intent(getActivity(), com.macinternetservices.aloofClient.StatusActivity.class), REQUEST_DEVICE);
                return true;
            case R.id.action_add:
                startActivityForResult(new Intent(getContext(), AddDeviceActivity.class), REQUEST_DEVICE);
                return true;
            case R.id.action_add_geofence:
                startActivityForResult(new Intent(getContext(), AddGeoFenceActivity.class), REQUEST_DEVICE);
                return true;
            case R.id.action_edit_geofence:
                startActivityForResult(new Intent(getContext(), GeofencesActivity.class), REQUEST_DEVICE);
                return true;
            case R.id.action_clear:
                DeleteAllTask deleteTask = new DeleteAllTask();
                deleteTask.execute();
                startActivityForResult(new Intent(getContext(), MainActivity.class), REQUEST_DEVICE);
                return true;
            case R.id.action_preferences:
                View dialogView = getLayoutInflater().inflate(R.layout.view_settings, null);
                final EditText input = (EditText) dialogView.findViewById(R.id.input_url);
                input.setText(preferences.getString(PREFERENCE_URL, null));

                Button setPref = dialogView.findViewById(R.id.btn_setPref);

                RadioGroup speed = dialogView.findViewById(R.id.radioGroupSpeed);

                switch (speedUnit) {
                    case "kmh":
                    case "km/h":
                        speedUnit = "kmh";
                        speed.check(R.id.btn_kmh);
                        break;
                    case "kn":
                        speedUnit = "kn";
                        speed.check(R.id.btn_kn);
                        break;
                    default:
                        speedUnit = "mph";
                        speed.check(R.id.btn_mph);
                }
                speed.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup radioGroup, int i) {
                        int id = speed.getCheckedRadioButtonId();
                        switch (id) {
                            case R.id.btn_kmh:
                                speedUnit = "kmh";
                                speed.check(R.id.btn_kmh);
                                break;
                            case R.id.btn_kn:
                                speedUnit = "kn";
                                speed.check(R.id.btn_kn);
                                break;
                            default:
                                speedUnit = "mph";
                                speed.check(R.id.btn_mph);
                                break;
                        }
                    }
                });

                RadioGroup time = dialogView.findViewById(R.id.radioGroupTime);

                if (preferences.getBoolean(PREFERENCE_12HR_FORMAT, true)) {
                    time.check(R.id.btn_time12);
                } else {
                    time.check(R.id.btn_time24);
                }

                time.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup radioGroup, int i) {
                        int id = time.getCheckedRadioButtonId();
                        switch (id) {
                            case R.id.btn_time24:
                                twelveHourFormat = false;
                                time.check(R.id.btn_time24);
                                break;
                            default:
                                twelveHourFormat = true;
                                time.check(R.id.btn_time12);
                                break;
                        }
                    }
                });


                new AlertDialog.Builder(getContext())
                        .setTitle(R.string.settings_title)
                        .setView(dialogView)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String url = input.getText().toString();
                                //speed.check(MainApplication.PREFERENCE_SPD_UNIT);
                                //time.check(MainApplication.PREFERENCE_TIME_UNIT);
                                if (HttpUrl.parse(url) != null) {
                                    preferences.edit().putString(
                                            PREFERENCE_URL, url).apply();
                                } else {
                                    Toast.makeText(getContext(), R.string.error_invalid_url, Toast.LENGTH_LONG).show();
                                }
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
                setPref.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        preferences.edit().putString(PREFERENCE_SPD_UNIT, speedUnit).apply();
                        //String foo = preferences.getString(PREFERENCE_SPD_UNIT, null);
                        Log.e("Prefrences", "Spd Unit: " + speedUnit);
                        preferences.edit().putBoolean(MainApplication.PREFERENCE_12HR_FORMAT, twelveHourFormat).apply();
                        //Boolean foo12Hr = preferences.getBoolean(PREFERENCE_12HR_FORMAT.toString(), true);
                        Log.e("Prefrences", "Time Unit: " + twelveHourFormat);
                        Toast.makeText(getContext(),
                                "speed/time: " + preferences.getString(PREFERENCE_SPD_UNIT, null) + " / " + preferences.getBoolean(PREFERENCE_12HR_FORMAT, false), Toast.LENGTH_SHORT).show();
                    }
                });
                break;
            case R.id.action_transition:
                if(!transitionAlert) {
                    addTransitionAlerts();
                } else {
                    Toast.makeText(getContext(), "Transition alerts are active", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.action_logout:
                PreferenceManager.getDefaultSharedPreferences(getContext())
                        .edit().putBoolean(MainApplication.PREFERENCE_AUTHENTICATED, false).apply();
                ((MainApplication) getActivity().getApplication()).removeService();
                getActivity().finish();
                startActivity(new Intent(getContext(), LoginActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addTransitionAlerts(){
        List<ActivityTransition> transitions = new ArrayList<>();

        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.WALKING)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build());

        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.WALKING)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                        .build());

        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.STILL)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build());

        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.STILL)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                        .build());

        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.RUNNING)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build());

        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.RUNNING)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                        .build());

        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.IN_VEHICLE)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build());

        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.IN_VEHICLE)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                        .build());

        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.ON_BICYCLE)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build());

        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.ON_BICYCLE)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                        .build());

        ActivityTransitionRequest request = new ActivityTransitionRequest(transitions);

        Intent intent = new Intent(getContext(), ActivityTransitionBroadcastReceiver.class);
        intent.setAction(ActivityTransitionBroadcastReceiver.INTENT_ACTION);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Task<Void> task = ActivityRecognition.getClient(getContext())
                .requestActivityTransitionUpdates(request, pendingIntent);

        task.addOnSuccessListener(
                new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Log.e("Monitoring","Transition Monitoring Enabled");
                    }
                }
        );

        task.addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Log.e("Monitoring","Transition Monitoring ");
                    }
                }
        );
        transitionAlert = true;

    }

    public static final String CHANNEL_ID = "Transition Service Channel";
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Transition Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getContext().getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    class DeleteAllTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            DatabaseClient.getInstance(getActivity()).getAppDatabase()
                    .dataDao()
                    .deleteAll();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // when users selects a device
        if (requestCode == REQUEST_DEVICE && resultCode == RESULT_SUCCESS) {
            long deviceId = data.getLongExtra(DevicesFragment.EXTRA_DEVICE_ID, 0);
            Position position = positions.get(deviceId);
            // if device exists in api
            if (position != null && position.getAttributes().getMotion()) {
                // begin tracking device by deviceId
                tracking = true;
            } else if (position != null && !position.getAttributes().getMotion()) {
                tracking = true;
                dautle = true;

            }
        }
    }

    public ArrayList<GeoLoc> listData = new ArrayList<>();

    class GetGeoLoc extends AsyncTask<Void, Void, ArrayList<GeoLoc>> {

        @Override
        protected ArrayList<GeoLoc> doInBackground(Void... voids) {
            listData.addAll(DatabaseClient
                    .getInstance(getActivity())
                    .getAppDatabase()
                    .dataDao()
                    .getAll());
            return listData;
        }

        @Override
        protected void onPostExecute(ArrayList<GeoLoc> data) {
            super.onPostExecute(data);

            for (int i = 0; i < data.size(); i++) {
                GeoLoc geoLoc = data.get(i);
                map.addCircle(new CircleOptions()
                        .center(new LatLng(geoLoc.getLatitude(), geoLoc.getLongitude()))
                        .radius(GEOFENCE_RADIUS)
                        .fillColor(0x40ff0000)
                        .strokeColor(Color.GREEN)
                        .strokeWidth(2));
            }

        }
    }
    private static final float GEOFENCE_RADIUS = 500.0f;

    //private TextView showRoute;
    @Override
    public void onMapReady(GoogleMap googleMap) {

        map = googleMap;
        GetGeoLoc getGeoLoc = new GetGeoLoc();
        getGeoLoc.execute();


        map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                // Setting up the infoWindow with current's marker info
                infoTitle.setText(marker.getTitle());
                infoSnippet.setText(marker.getSnippet());
                trackedDevice = marker.getTitle();
                //infoButtonListener.setMarker(marker);

                return infoWindow;
            }
        });

        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                if (tracking) {
                    tracking = false;
                    trackedDevice = marker.getTitle();
                    LatLngBounds bounds = latLngBoundsBuilder.build();
                    CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(marker.getPosition().latitude, marker.getPosition().longitude));
                    CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);
                    map.moveCamera(center);
                    map.animateCamera(zoom);
                    //map.animateCamera(CameraUpdateFactory.);
                    map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
                    Toast.makeText(getContext(), "No longer tracking " + marker.getTitle(), Toast.LENGTH_SHORT).show();
                    marker.hideInfoWindow();
                    /*if(polyline != null) {
                        polyline.remove();
                    } */
                } else {
                    tracking = true;
                    trackedDevice = marker.getTitle();
                    CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(marker.getPosition().latitude, marker.getPosition().longitude));
                    CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);
                    marker.showInfoWindow();
                    map.moveCamera(center);
                    map.animateCamera(zoom);
                    marker.showInfoWindow();
                    displayRoute(/*getContext()*/);
                    Toast.makeText(getContext(), "Now tracking " + marker.getTitle(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                // Retrieve the data from the marker.
                Integer clickCount = (Integer) marker.getTag();

                // get address for device lat/lng from google api
                Geocoder geocoder;
                List<Address> addresses;
                geocoder = new Geocoder(getContext(), Locale.getDefault());
                address = "address";
                bldgno = "bldgno";
                street = "street";
                city = "city";
                state = "state";
                postalCode = "zipcode";
                country = null;
                knownName = null;
                try {
                    addresses = geocoder.getFromLocation(marker.getPosition().latitude, marker.getPosition().longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                    address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                    bldgno = addresses.get(0).getSubThoroughfare(); // building number
                    street = addresses.get(0).getThoroughfare(); //street name
                    city = addresses.get(0).getLocality();
                    state = addresses.get(0).getAdminArea();
                    country = addresses.get(0).getCountryName();
                    postalCode = addresses.get(0).getPostalCode();
                    knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Check if a click count was set, then display the click count.
                //if (clickCount != null) {
//                    clickCount = clickCount + 1;
//                    marker.setTag(clickCount);
                    Toast.makeText(getContext(),
                            marker.getTitle() +
                                    " is currently at " + address,
                            Toast.LENGTH_SHORT).show();
                //}

                // Return false to indicate that we have not consumed the event and that we wish
                // for the default behavior to occur (which is for the camera to move such that the
                // marker is centered and for the marker's info window to open, if it has one).

                return false;
            }
        });
        createWebSocket();

        Intent geofenceIntent = new Intent(getContext(), LocationService.class);
        ContextCompat.startForegroundService(getActivity(), geofenceIntent);

        Intent transitionIntent = new Intent(getContext(), ActivityTransitionBroadcastReceiver.class);
        ContextCompat.startForegroundService(getActivity(), transitionIntent);

        //Intent transitionServiceIntent = new Intent(getContext(), TransitionService.class);
        //ContextCompat.startForegroundService(getActivity(), transitionServiceIntent);

        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }


    @SuppressLint("MissingPermission")
    private void displayRoute(/*Context ctx*/) {
        locManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
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
                LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
                if(flag)  //when the first update comes, we have no previous points,hence this
                {
                    prev = current;
                    flag = false;
                }
                /*if(tracking) {
                    polyline = map.addPolyline(new PolylineOptions().add(prev, current).width(6).color(Color.BLUE).visible(true));
                    prev = current;
                    current = null;
                } */
            }
        };
        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1, locListener);
    }



    private String formatDetails(Position position) {
        //final MainApplication application = (MainApplication) getContext().getApplicationContext();
        //final User user = application.getUser();

        SimpleDateFormat dateFormat;
        if (twelveHourFormat) {
            dateFormat = new SimpleDateFormat("h:mm:ss a MM-dd-yyyy", Locale.US);
        } else {
            dateFormat = new SimpleDateFormat("HH:mm:ss MM-dd-yyyy", Locale.US);
        }

        //String speedUnit = "mph";
        double factor = 1;
        if (speedUnit != null) {
            switch (speedUnit) {
                case "kmh":
                case "km/h":
                    speedUnit = "kmh";
                    factor = 1.852;
                    break;
                case "kn":
                    speedUnit = getString(R.string.user_kn);
                    factor = 1;
                    break;
                default:
                    speedUnit = "mph";
                    factor = 1.15078;
                    break;
            }
        }
        speed = position.getSpeed() * factor;

        // get address for device lat/lng from google api
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(getContext(), Locale.getDefault());
        address = "address";
        bldgno = "bldgno";
        street = "street";
        city = "city";
        state = "state";
        postalCode = "zipcode";
        country = null;
        knownName = null;
        try {
            addresses = geocoder.getFromLocation(position.getLatitude(), position.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            bldgno = addresses.get(0).getSubThoroughfare(); // building number
            street = addresses.get(0).getThoroughfare(); //street name
            city = addresses.get(0).getLocality();
            state = addresses.get(0).getAdminArea();
            country = addresses.get(0).getCountryName();
            postalCode = addresses.get(0).getPostalCode();
            knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL
        } catch (IOException e) {
            e.printStackTrace();
        }

        //translate degree bearing to direction
        if (position.getCourse() >= 0 && position.getCourse() <= 21) {
            direction = "North";
        } else if (position.getCourse() >= 22 && position.getCourse() <= 44) {
            direction = "North Northeast";
        } else if (position.getCourse() >= 45 && position.getCourse() <= 66) {
            direction = "Northeast";
        } else if (position.getCourse() >= 67 && position.getCourse() <= 89) {
            direction = "East Northeast";
        } else if (position.getCourse() >= 90 && position.getCourse() <= 111) {
            direction = "East";
        } else if (position.getCourse() >= 112 && position.getCourse() <= 134) {
            direction = "East Southeast";
        } else if (position.getCourse() >= 135 && position.getCourse() <= 156) {
            direction = "Southeast";
        } else if (position.getCourse() >= 157 && position.getCourse() <= 179) {
            direction = "South Southeast";
        } else if (position.getCourse() >= 180 && position.getCourse() <= 201) {
            direction = "South";
        } else if (position.getCourse() >= 202 && position.getCourse() <= 224) {
            direction = "South Southwest";
        } else if (position.getCourse() >= 225 && position.getCourse() <= 246) {
            direction = "Southwest";
        } else if (position.getCourse() >= 247 && position.getCourse() <= 269) {
            direction = "West Southwest";
        } else if (position.getCourse() >= 270 && position.getCourse() <= 291) {
            direction = "West";
        } else if (position.getCourse() >= 292 && position.getCourse() <= 314) {
            direction = "West Northwest";
        } else if (position.getCourse() >= 315 && position.getCourse() <= 336) {
            direction = "Northwest";
        } else if (position.getCourse() >= 337 && position.getCourse() <= 360) {
            direction = "South";
        }

        //if the device is in motion
        if (position.getAttributes().getMotion() && position.getSpeed() > 0) {
            //build infoWindow txt
            return new StringBuilder()
                    .append("Heading").append(" ").append(direction).append('\n')
                    .append("On or near").append(" ").append(street).append('\n')
                    .append("at").append(" ").append(String.format("%.1f", speed)).append(" ").append(speedUnit).append('\n')
                    .append('\n')
                    .append("Battery").append(": ")
                    .append(position.getAttributes().getBatteryLevel()).append('%').append('\n')
                    .append(getString(R.string.position_time)).append(": ")
                    .append(dateFormat.format(position.getFixTime()))
                    .toString();

        } else if (position.getAttributes().getMotion() && dautle){
            return new StringBuilder()
                    .append("Heading").append(" ").append(direction).append('\n')
                    .append("On or near").append(" ").append(street).append('\n')
                    .append("is dautling!")
                    .append('\n')
                    .append("Battery").append(": ")
                    .append(position.getAttributes().getBatteryLevel()).append('%').append('\n')
                    .append(getString(R.string.position_time)).append(": ")
                    .append(dateFormat.format(position.getFixTime()))
                    .toString();
        } else {
            return new StringBuilder()
                    .append("Current Location").append(":").append('\n')
                    //.append(knownName).append('\n')
                    //.append('\n')
                    //.append("Address:").append('\n')
                    .append(bldgno).append(" ").append(street).append('\n')
                    .append(city).append(", ").append(state).append(" ").append(postalCode).append('\n')
                    .append(country).append('\n')
                    .append('\n')
                    .append("Battery").append(": ")
                    .append(position.getAttributes().getBatteryLevel()).append('%').append('\n')
                    .append(getString(R.string.position_time)).append(": ")
                    .append(dateFormat.format(position.getFixTime()))
                    .toString();
        }
    }

    public static LatLngBounds.Builder latLngBoundsBuilder = new LatLngBounds.Builder();

    public static ArrayList<DataModel> allDevicesArray = new ArrayList<>();
    //handle device api data
    private void handleMessage(String message) throws IOException {
        Log.e("handleMessage","Message: "+message);
        Update update = objectMapper.readValue(message, Update.class);
        if (update != null && update.positions != null) {
            //LatLngBounds.Builder builder = LatLngBounds.builder();
            // add map marker for each device
            for (Position position : update.positions) {
                long deviceId = position.getDeviceId(); //deviceId
                if (devices.containsKey(deviceId)) {
                    LatLng location = new LatLng(position.getLatitude(), position.getLongitude()); // get device lat/lng
                   // builder.include(location);
                    latLngBoundsBuilder.include(location);
                    Marker marker = markers.get(deviceId); // device marker
                    if (marker == null) { // if no marker exists
                        marker = map.addMarker(new MarkerOptions()
                                .title(devices.get(deviceId).getName()).position(location)); //add marker at device lat/lng on map
                        markers.put(deviceId, marker); // adds device to markers array
                    } else {
                        marker.setPosition(location); // add device marker position
                    }
                    marker.setSnippet(formatDetails(position)); //set device details
                    String foo = devices.get(deviceId).getName();

                   if(tracking && devices.get(deviceId).getName().equals(trackedDevice)) { // if tracking do not update view
                        CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(marker.getPosition().latitude, marker.getPosition().longitude));
                        CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);
                        map.moveCamera(center);
                        map.animateCamera(zoom);
                        marker.showInfoWindow();
                        //break;
                    } else if (firstRun){
                        LatLngBounds bounds = latLngBoundsBuilder.build();
                        CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(marker.getPosition().latitude, marker.getPosition().longitude));
                        CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);
                        map.moveCamera(center);
                        map.animateCamera(zoom);
                        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds,100));
                    }
                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(new Random().nextInt(360))); // make each marker a random color
                    positions.put(deviceId, position); // add to positions array
                }
                // alert on low battery
                if (position.getAttributes().getBatteryLevel() <= 25.0 && !batAlert) {
                    batAlert = true;
                    new AlertDialog.Builder(getContext())
                            .setTitle("Low Battery Alert")
                            .setMessage(devices.get(deviceId).getName() + " has " + position.getAttributes().getBatteryLevel() + "% battery charge")
                            .setPositiveButton(android.R.string.ok, null)
                            .show();
                }
                if(devices.get(deviceId).getStatus().equals("online")){
                    iv_online.setVisibility(View.VISIBLE);
					iv_offline.setVisibility(View.GONE);
                } else {
                    iv_offline.setVisibility(View.VISIBLE);
                    iv_online.setVisibility(View.GONE);
                }
            }
			firstRun = false;				 
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (webSocket != null) {
            webSocket.cancel();
        }
    }

    private void reconnectWebSocket() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (getActivity() != null) {
                    createWebSocket();
                }
            }
        });
    }

    private void createWebSocket() {
        final MainApplication application = (MainApplication) getActivity().getApplication();
        application.getServiceAsync(new MainApplication.GetServiceCallback() {
            @Override
            public void onServiceReady(final OkHttpClient client, final Retrofit retrofit, WebService service) {
                User user = application.getUser();

                service.getDevices().enqueue(new WebServiceCallback<List<Device>>(getContext()) {
                    @Override
                    public void onSuccess(retrofit2.Response<List<Device>> response) {
                        for (Device device : response.body()) {
                            if (device != null) {
                                devices.put(device.getId(), device);
                                allDevicesArray.add(new DataModel(device.getName(), device.getId(), device.getPhone(), device.getUniqueId()));
                            }
                        }

                        Request request = new Request.Builder().url(retrofit.baseUrl().url().toString() + "api/socket").build();
                        Log.e("WebSockets", "Headers: " + request.headers().toString());
                        WebSocketListener webSocketListener = new WebSocketListener() {
                            private static final int NORMAL_CLOSURE_STATUS = 1000;
                            @Override
                            public void onOpen(WebSocket webSocket, Response response) {
                                webSocket.send("{Auth-Token:secret-api-token-here}");
                                Log.e("WebSockets", "Connection accepted!");
                            }

                            @Override
                            public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
                                reconnectWebSocket();
                            }

                            @Override
                            public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
                                final String data = text;
                                //Log.e("WebSocket", "Receiving : " + text);
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            handleMessage(data);
                                        } catch (IOException e) {
                                            Log.w(MainFragment.class.getSimpleName(), e);
                                        }
                                    }
                                });
                            }
                            @Override
                            public void onMessage(WebSocket webSocket, ByteString bytes) {
                                Log.e("WebSockets", "Receiving bytes : " + bytes.hex());
                            }

                            @Override
                            public void onClosing(WebSocket webSocket, int code, String reason) {
                                webSocket.close(NORMAL_CLOSURE_STATUS, null);
                                Log.e("WebSockets", "Closing : " + code + " / " + reason);
                            }

                            @Override
                            public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                                Log.e("WebSockets", "Closed : " + code + " / " + reason);
                                Log.e("WebSockets", "Closed :  Relaunching ");
                                reconnectWebSocket();
                            }
                        };

                        webSocket = client.newWebSocket(request, webSocketListener);
                    }
                });
            }

            @Override
            public boolean onFailure() {
                return false;
            }
        });
    }
}
