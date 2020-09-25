/*
 * Copyright 2016 Anton Tananaev (anton.tananaev@gmail.com)
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

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.macinternetservices.aloofManager.model.Device;
import com.macinternetservices.aloofManager.model.Geofence;
import com.google.gson.JsonObject;

import com.macinternetservices.aloofManager.R;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

public class EditDeviceFragment extends Fragment {

    private EditText uniqueId;
    private EditText name;
    private EditText phone;
    private Button edtDeviceButton;
    private CheckBox checkBox;
    private Spinner geoFenceSpinner;
    private TextView newGeofence;
    private String spinnerItem = null;
    ArrayList<String> dataModels = new ArrayList<>();


    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edt_device, container, false);
        //Get Argument that passed from activity in "data" key value
        Bundle bundle = getArguments();

        uniqueId =  view.findViewById(R.id.input_uniqueId);
        phone =  view.findViewById(R.id.input_phone);
        name = view.findViewById(R.id.input_name);
        edtDeviceButton = view.findViewById(R.id.button_edt_device);
        checkBox = view.findViewById(R.id.checkBox);
        geoFenceSpinner = view.findViewById(R.id.geoFenceSpinner);
        newGeofence = view.findViewById(R.id.newGeofence);
        newGeofence.setVisibility(View.GONE);
        geoFenceSpinner.setVisibility(View.GONE);
        //geoFenceSpinner.setOnItemSelectedListener(new MyOnItemSelectedListener());


        uniqueId.setText(String.valueOf(bundle.getLong("deviceId")));
        phone.setText(bundle.getString("phone"));
        name.setText(bundle.getString("name"));

        final SharedPreferences data = PreferenceManager.getDefaultSharedPreferences(getContext());

        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkBox.isChecked()) {
                    geoFenceSpinner.setVisibility(View.VISIBLE);
                    newGeofence.setVisibility(View.VISIBLE);
                    createSpinnerDropDown();
                } else {
                    newGeofence.setVisibility(View.GONE);
                    geoFenceSpinner.setVisibility(View.GONE);
                }

            }
        });

        newGeofence.setOnClickListener(v -> {
            Intent geoFence = new Intent(getContext(), AddGeoFenceActivity.class);
            geoFence.putExtra(DevicesFragment.EXTRA_DEVICE_ID, ""+bundle.getLong("deviceId"));
            geoFence.putExtra(DevicesFragment.EXTRA_DEVICE_NAME, ""+bundle.getString("name"));
            getContext().startActivity(geoFence);
        });


        edtDeviceButton.setOnClickListener(v -> {

             data
                    .edit()
                    .putBoolean(MainApplication.PREFERENCE_AUTHENTICATED, true)
                    .putString(MainApplication.PREFERENCE_UNIQUEID, String.valueOf(bundle.getLong("deviceId")))
                    //.putString(MainApplication.PREFERENCE_NAME, name.getText().toString())
                    //.putString(MainApplication.PREFERENCE_PHONE, phone.getText().toString())
                    .apply();

            editDevice();
        });

        return view;
    }

    private void createSpinnerDropDown() {
        final ProgressDialog progress = new ProgressDialog(getContext());
        progress.setMessage(getString(R.string.app_loading));
        progress.setCancelable(false);
        progress.show();
        dataModels.clear();
        final MainApplication application = (MainApplication) getActivity().getApplication();
        application.getServiceAsync(new MainApplication.GetServiceCallback() {
            @Override
            public void onServiceReady(OkHttpClient client, Retrofit retrofit, WebService service) {
                if (progress.isShowing()) {
                    progress.dismiss();
                }

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
                String email = preferences.getString(MainApplication.PREFERENCE_EMAIL, null);
                final String password = preferences.getString(MainApplication.PREFERENCE_PASSWORD, null);

                //String auth = Credentials.basic(email, password);

                service.getGeofences().enqueue(new WebServiceCallback<List<Geofence>>(getContext()) {
                    @Override
                    public void onSuccess(Response<List<Geofence>> response) {
                        Log.e("Geofence", "Response: " + response.body().toString());
                        if (response.body() != null) {


                            for (Geofence geofence : response.body()) {
                                String fooArea = geofence.getArea();
                                String fooDescription = geofence.getDescription();
                                String fooName = geofence.getName();
                                String fooId = geofence.getId();
                                    dataModels.add(geofence.getName());
                            }
                            //create an ArrayAdaptar from the String Array
                            ArrayAdapter dataAdapter = new ArrayAdapter<>(getContext(),
                                    android.R.layout.simple_spinner_item, dataModels);
                            //set the view for the Drop down list
                            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            //set the ArrayAdapter to the spinner
                            geoFenceSpinner.setAdapter(dataAdapter);
                            //attach the listener to the spinner
                            geoFenceSpinner.setOnItemSelectedListener(new MyOnItemSelectedListener());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Geofence>> call, Throwable t) {
                        Log.e("Geofence", "FuBar: " + t);
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

    private class MyOnItemSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

            String selectedGeofence = parent.getItemAtPosition(pos).toString();

            //check which spinner triggered the listener
            switch (parent.getId()) {
                //country spinner
                case R.id.spinner:
                    //make sure the country was already selected during the onCreate
                    if(spinnerItem != null){
                        Toast.makeText(parent.getContext(), "You selected Geofence " + selectedGeofence,
                                Toast.LENGTH_LONG).show();
                    }
                    spinnerItem = selectedGeofence;
                    break;
            }


        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Do nothing.
        }
    }

    private void editDevice() {
        final ProgressDialog progress = new ProgressDialog(getContext());
        progress.setMessage(getString(R.string.app_loading));
        progress.setCancelable(false);
        progress.show();
        final MainApplication application = (MainApplication) getActivity().getApplication();
        application.getServiceAsync(new MainApplication.GetServiceCallback() {
            @Override
            public void onServiceReady(OkHttpClient client, Retrofit retrofit, WebService service) {
                if (progress.isShowing()) {
                    progress.dismiss();
                }

                JsonObject requestBody = new JsonObject();
                requestBody.addProperty("uniqueId", uniqueId.getText().toString());
                requestBody.addProperty("name", name.getText().toString());
                requestBody.addProperty("phone", phone.getText().toString());
                //requestBody.addProperty("disabled", false);

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
                String email = preferences.getString(MainApplication.PREFERENCE_EMAIL, null);
                final String password = preferences.getString(MainApplication.PREFERENCE_PASSWORD, null);

                String auth = Credentials.basic(email, password);

//                Map<String, String> headers = new HashMap<>();
//                headers.put("Content-Type",  "application/json");
//                headers.put("Authorization",  auth);
                RequestBody requestBody2=RequestBody.create(MediaType.parse("application/json; charset=utf-8"),requestBody.toString());
                service.editDevice("application/json", requestBody2).enqueue(new WebServiceCallback<Device>(getContext()) {
                    @Override
                    public void onSuccess(Response<Device> response) {
                        Toast.makeText(getContext(), "Device " +uniqueId.getText().toString()+ " information has been updated", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(getContext(), MainActivity.class));
                        getActivity().finish();
                    }

                    @Override
                    public void onFailure(Call<Device> call, Throwable t) {
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
