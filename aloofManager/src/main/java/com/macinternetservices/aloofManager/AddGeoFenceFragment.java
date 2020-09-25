
package com.macinternetservices.aloofManager;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.macinternetservices.aloofManager.model.GeoLoc;
import com.macinternetservices.aloofManager.room.DatabaseClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.gson.JsonObject;

import com.macinternetservices.aloofManager.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

import static android.content.ContentValues.TAG;
import static com.macinternetservices.aloofManager.MainApplication.PREFERENCE_EMAIL;
import static com.macinternetservices.aloofManager.MainApplication.PREFERENCE_GEOFENCE_LAT;
import static com.macinternetservices.aloofManager.MainApplication.PREFERENCE_GEOFENCE_LON;
import static com.macinternetservices.aloofManager.MainApplication.PREFERENCE_PASSWORD;

public class AddGeoFenceFragment extends Fragment {

    private TextView name, description, address, tvDeviceId;
    //Double latitude, longitude;
    private View addGeofenceButton;
    private static final int AUTOCOMPLETE_REQUEST_CODE = 4077;
    SharedPreferences preferences;
    private GeofencingClient geofencingClient;
    public Context mContext;
    private Spinner geoFenceSpinner;
    private TextView newGeofence;
    private String spinnerItem = null;
    ArrayList<com.macinternetservices.aloofManager.model.Geofence> geofences = new ArrayList<>();
    private CheckBox checkBox;
    RelativeLayout addGeofenceLayout;
    LinearLayout addGeofenceInfoLayout;



    private TextWatcher textWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            addGeofenceButton.setEnabled(
                    description.getText().length() > 0 && address.getText().length() > 0 && name.getText().length() > 0);
        }

    };

    private static final String NOTIFICATION_MSG = "NOTIFICATION MSG";

    // Create a Intent send by the notification
    public static Intent makeNotificationIntent(Context context, String msg) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(NOTIFICATION_MSG, msg);
        return intent;
    }

    List<Place.Field> fields;

    private String deviceId,deviceName;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = getActivity();
        Intent intent = getActivity().getIntent();
         deviceId = intent.getStringExtra(DevicesFragment.EXTRA_DEVICE_ID); //if it's a string you stored.
         deviceName = intent.getStringExtra(DevicesFragment.EXTRA_DEVICE_NAME); //if it's a string you stored.
        View view = inflater.inflate(R.layout.fragment_add_geofence, container, false);
        geofencingClient = LocationServices.getGeofencingClient(getActivity());
        tvDeviceId = view.findViewById(R.id.tvDeviceId);
        tvDeviceId.setText(deviceId);
        name = view.findViewById(R.id.input_name);
        description = view.findViewById(R.id.input_description);
        address = view.findViewById(R.id.input_address);
        addGeofenceButton = view.findViewById(R.id.button_add_geofence);
        geoFenceSpinner = view.findViewById(R.id.geoFenceSpinner);
        checkBox = view.findViewById(R.id.checkBox);
        addGeofenceInfoLayout = view.findViewById(R.id.addGeofenceInfoLayout);
        addGeofenceInfoLayout.setVisibility(View.GONE);

        //createSpinnerDropDown();
        AddGeoFenceFragment.GetGeoLoc getGeoLoc = new GetGeoLoc();
        getGeoLoc.execute();

        description.addTextChangedListener(textWatcher);
        address.addTextChangedListener(textWatcher);
        name.addTextChangedListener(textWatcher);

        preferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        final SharedPreferences data = PreferenceManager.getDefaultSharedPreferences(getContext());

        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkBox.isChecked()) {
                    description.addTextChangedListener(textWatcher);
                    //address.addTextChangedListener(textWatcher);
                    name.addTextChangedListener(textWatcher);
                    addGeofenceInfoLayout.setVisibility(View.VISIBLE);
                    geoFenceSpinner.setVisibility(View.GONE);
                } else {
                    addGeofenceInfoLayout.setVisibility(View.GONE);
                    geoFenceSpinner.setVisibility(View.VISIBLE);
                }

            }
        });

        address.setOnClickListener(v -> {
            // @Override
            //public void onClick(View view) {
            /**
             * Initialize Places. For simplicity, the API key is hard-coded. In a production
             * environment we recommend using a secure mechanism to manage API keys.
             */
            if (!Places.isInitialized()) {
                Places.initialize(getContext(), getResources().getString(R.string.google_api));
            }
            // Set the fields to specify which types of place data to return.
            fields = Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG);

            // Start the autocomplete intent.
            Intent autoCompleteIntent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields).build(getContext());
            startActivityForResult(autoCompleteIntent, AUTOCOMPLETE_REQUEST_CODE);
            //}
        });

        addGeofenceButton.setOnClickListener(v -> {

            data
                    .edit()
                    .putBoolean(MainApplication.PREFERENCE_AUTHENTICATED, true)
                    //.putString(MainApplication.PREFERENCE_UNIQUEID, uniqueId.getText().toString())
                    .putString(MainApplication.PREFERENCE_NAME, name.getText().toString())
                    //.putString(MainApplication.PREFERENCE_PHONE, phone.getText().toString())
                    .apply();

            startGeofence();
        });

        return view;
    }

    ArrayList<String> geofenceNames = new ArrayList<>();

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

            if(data.size() > 0) {
                for (int i = 0; i < data.size(); i++) {
                    GeoLoc geoLoc = data.get(i);

                    geofenceNames.add(geoLoc.getName());

                    //create an ArrayAdaptar from the String Array
                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getContext(),
                            android.R.layout.simple_spinner_item, geofenceNames);
                    //set the view for the Drop down list
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    //set the ArrayAdapter to the spinner
                    geoFenceSpinner.setAdapter(dataAdapter);
                    //attach the listener to the spinner
                    geoFenceSpinner.setOnItemSelectedListener(new MyOnItemSelectedListener());
                }
            } else {
                addGeofenceInfoLayout.setVisibility(View.VISIBLE);
                geoFenceSpinner.setVisibility(View.GONE);
                checkBox.setVisibility(View.GONE);
            }

        }
    }

    String selectedGeofence;
    private class MyOnItemSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

            selectedGeofence = parent.getItemAtPosition(pos).toString();

            if (selectedGeofence != null) {
                addGeofenceButton.setEnabled(selectedGeofence != null);
                Toast.makeText(parent.getContext(), "You selected Geofence " + selectedGeofence,
                        Toast.LENGTH_LONG).show();
            }
            //spinnerItem = selectedGeofence;

            for (int i = 0; i < listData.size(); i++) {
               // String nameFoo = listData.get(i).getName();
                //Double lat = listData.get(i).getLatitude();
                //Double lng = listData.get(i).getLongitude();
                //LatLng latlng = listData(listData.get(i).getLatitude(), listData.get(i).getLongitude());

                if (parent.getItemAtPosition(pos).toString().equals(listData.get(i).getName())) {
                    String nameFoo = listData.get(i).getName();
                    //Double fooLng = listData.get(i).getLongitude();
                    locationLatLng = new LatLng(listData.get(i).getLatitude(), listData.get(i).getLongitude());
                    Log.e("Geofence", "Selected Name: " + nameFoo + " Area: " + locationLatLng);
                    /*String[] parts = fooArea.split("^\\(([^\\)]+)\\)");
                    for (String t : parts)
                        System.out.println(t);
                    String fooDescription = geofences.get(i).getDescription();
                    String fooName = geofences.get(i).getName();
                    String fooId = geofences.get(i).getId();
                    Log.e("Geofence", "Selected Name: " + fooName + " Area: " + fooArea); */
                    //Geofence geofence = geofences.get(i).getArea();
                }
            }
        }
        public void onNothingSelected(AdapterView<?> parent) {
            // Do nothing.
        }
    }

    LatLng locationLatLng;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // when users selects a device
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                locationLatLng = place.getLatLng();
                address.setText(place.getAddress());
        }
    }

    // Start Geofence creation process
    private void startGeofence() {
        Log.i(TAG, "startGeofence()");
        if (locationLatLng != null) {
            Geofence geofence = createGeofence(locationLatLng, GEOFENCE_RADIUS);
            //GeofencingRequest geofenceRequest = createGeofenceRequest(geofence);
            //String foo = geofence.toString();
            addGeofence(locationLatLng);
            //Log.e(TAG, "Geofence marker is" + geofenceRequest);
        } else {
            Log.e(TAG, "Geofence marker is null");
        }
        //startActivity(new Intent(getContext(), MainActivity.class));
        //getActivity().finish();
    }

    private static final long GEO_DURATION = 12 * 60 * 60 * 1000;
    //private static final String GEOFENCE_REQ_ID = "My Geofence";
    private static final float GEOFENCE_RADIUS = 500.0f; // in metersna


    // Create a Geofence
    private Geofence createGeofence(LatLng latLng, float radius) {
        Log.e(TAG, "createGeofence");
        return new Geofence.Builder()
                .setRequestId(name.getText().toString())
                .setCircularRegion(latLng.latitude, latLng.longitude, GEOFENCE_RADIUS)
                .setExpirationDuration(GEO_DURATION)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER
                        | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();
    }

    // Create a Geofence Request
    private GeofencingRequest createGeofenceRequest(Geofence geofence) {
        Log.e(TAG, "createGeofenceRequest");
        return new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build();
    }
    private PendingIntent geoFencePendingIntent;
    private final int GEOFENCE_REQ_CODE = 0;

    private PendingIntent createGeofencePendingIntent() {
        Log.e(TAG, "createGeofencePendingIntent");
        if (geoFencePendingIntent != null)
            return geoFencePendingIntent;

        Intent intent = new Intent(getContext(), GeofenceTransitionService.class);
        return PendingIntent.getService(
                getContext(), GEOFENCE_REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
    // Draw Geofence circle on GoogleMap
    private Circle geoFenceLimits;

    private void drawGeofence() {
        Log.e("drawGeofence", "drawGeofence");

        if (geoFenceLimits != null)
            geoFenceLimits.remove();

        CircleOptions circleOptions = new CircleOptions()
                .center(locationLatLng)
                .strokeColor(Color.argb(50, 70, 70, 70))
                .fillColor(Color.argb(100, 255, 153, 153))
                .radius(GEOFENCE_RADIUS);

        geoFenceLimits = MainFragment.map.addCircle(new CircleOptions()
                .center(locationLatLng)
                .strokeColor(Color.argb(50, 70, 70, 70))
                .fillColor(Color.argb(100, 255, 153, 153))
                .radius(GEOFENCE_RADIUS));
        //String Foo = geoFenceLimits;
    }

    private final String KEY_GEOFENCE_LAT = null;
    private final String KEY_GEOFENCE_LON = null;

    // Saving GeoFence marker with prefs mng

    private ArrayList<Geofence> geofenceList = new ArrayList<>();

    private void saveGeofence(String id) {
        Log.e("addGeofences","Id"+id+"L"+locationLatLng.latitude+"L"+locationLatLng.longitude);

        preferences.edit().putString(PREFERENCE_GEOFENCE_LAT, String.valueOf(locationLatLng.latitude)).apply();
        preferences.edit().putString(PREFERENCE_GEOFENCE_LON, String.valueOf(locationLatLng.longitude)).apply();

        SaveTask st = new SaveTask();
        st.execute();

    }

    class SaveTask extends AsyncTask<Void,Void,Void> {

        GeoLoc dd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dd = new GeoLoc();
            dd.setDeviceid(deviceId);
            dd.setName(name.getText().toString());
		//	dd.setStatus("0");
            dd.setNotified(false);
            dd.setLatitude(locationLatLng.latitude);
            dd.setLongitude(locationLatLng.longitude);
        }

        @Override
        protected Void doInBackground(Void... voids) {

            DatabaseClient.getInstance(getActivity()).getAppDatabase()
                    .dataDao()
                    .insert(dd);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (progress.isShowing()) {
                progress.dismiss();
            }

            Intent serviceIntent = new Intent(getActivity(), LocationService.class);
            ContextCompat.startForegroundService(getActivity(), serviceIntent);

            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
			}
		}

        private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geofenceList);
        return builder.build();
    }

    private PendingIntent geofencePendingIntent;

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }

        Intent intent = new Intent(getContext(), GeofenceTransitionService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        geofencePendingIntent = PendingIntent.getBroadcast(getActivity(), 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return PendingIntent.getService(
                getContext(), GEOFENCE_REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    // Recovering last Geofence marker
    private void recoverGeofenceMarker() {
        Log.e(TAG, "recoverGeofenceMarker");

        if ( preferences.contains( PREFERENCE_GEOFENCE_LAT ) && preferences.contains( PREFERENCE_GEOFENCE_LON )) {
            double lat = Double.longBitsToDouble( preferences.getLong( KEY_GEOFENCE_LAT, -1 ));
            double lon = Double.longBitsToDouble( preferences.getLong( KEY_GEOFENCE_LON, -1 ));
            LatLng latLng = new LatLng( lat, lon );
            //markerForGeofence(latLng);
            drawGeofence();
        }
    }

    // Clear Geofence
    private void clearGeofence() {
                    removeGeofenceDraw();

    }

    private void removeGeofenceDraw() {
        Log.e(TAG, "removeGeofenceDraw()");
//        if ( geoFenceMarker != null)
//            geoFenceMarker.remove();
        if ( geoFenceLimits != null )
            geoFenceLimits.remove();
    }

    // Upload Geofence to API
    private ProgressDialog progress;
    private void addGeofence(LatLng locationLatLng) {
//        saveGeofence("bcdd");
         progress = new ProgressDialog(getContext());
        progress.setMessage(getString(R.string.app_loading));
        progress.setCancelable(false);
        progress.show();
        final MainApplication application = (MainApplication) getActivity().getApplication();
        application.getServiceAsync(new MainApplication.GetServiceCallback() {
            @Override
            public void onServiceReady(OkHttpClient client, Retrofit retrofit, WebService service) {

                //String foo ="CIRCLE (" +locationLatLng.latitude +locationLatLng.longitude+", 50)";
                JsonObject requestBody = new JsonObject();
                //requestBody.addProperty("id", id.getText().toString());
                requestBody.addProperty("name", name.getText().toString());
                requestBody.addProperty("description", description.getText().toString());
                requestBody.addProperty("area", "CIRCLE (" +locationLatLng.latitude +" " +locationLatLng.longitude+", 50)");
                //requestBody.addProperty("calendarId", false);
                //requestBody.addProperty("area", false);

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
                String email = preferences.getString(PREFERENCE_EMAIL, null);
                final String password = preferences.getString(PREFERENCE_PASSWORD, null);

                //String auth = Credentials.basic(email, password);

//                Map<String, String> headers = new HashMap<>();
//                headers.put("Content-Type",  "application/json");
//                headers.put("Authorization",  auth);
                RequestBody requestBody2=RequestBody.create(MediaType.parse("application/json; charset=utf-8"),requestBody.toString());
                service.addGeoFence("application/json", requestBody2).enqueue(new WebServiceCallback<com.macinternetservices.aloofManager.model.Geofence>(getContext()) {
                    @Override
                    public void onSuccess(Response<com.macinternetservices.aloofManager.model.Geofence> response) {
                        Toast.makeText(getContext(), "Geo Fence Created " + response,
                                Toast.LENGTH_LONG).show();
                        saveGeofence(response.body().getId());
                    }

                    @Override
                    public void onFailure(Call<com.macinternetservices.aloofManager.model.Geofence> call, Throwable t) {
                        Log.e("addGeofence failed: ", "API 4 shit: " +t);
                        if (progress.isShowing()) {
                            progress.dismiss();
                        }
                        super.onFailure(call, t);
                    }
                });
            }

            @Override
            public boolean onFailure() {
                if (progress.isShowing()) {
                    progress.dismiss();
                }
                return false;
            }
        });
    }

}
