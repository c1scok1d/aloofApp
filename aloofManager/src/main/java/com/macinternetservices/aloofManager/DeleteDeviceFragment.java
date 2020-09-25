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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.macinternetservices.aloofManager.model.Device;
import com.google.gson.JsonObject;

import com.macinternetservices.aloofManager.R;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.macinternetservices.aloofManager.MainApplication.PREFERENCE_EMAIL;
import static com.macinternetservices.aloofManager.MainApplication.PREFERENCE_PASSWORD;

public class DeleteDeviceFragment extends Fragment {

    private TextView uniqueId;
    private TextView name;
    private TextView phone;
    private View addDeviceButton;

    private TextWatcher textWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            addDeviceButton.setEnabled(
                    uniqueId.getText().length() > 0 && phone.getText().length() > 0 && name.getText().length() > 0);
        }

    };

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_device, container, false);

        uniqueId =  view.findViewById(R.id.input_uniqueId);
        phone =  view.findViewById(R.id.input_phone);
        name = view.findViewById(R.id.input_name);
        addDeviceButton = view.findViewById(R.id.button_add_device);

        uniqueId.addTextChangedListener(textWatcher);
        phone.addTextChangedListener(textWatcher);
        name.addTextChangedListener(textWatcher);

        final SharedPreferences data = PreferenceManager.getDefaultSharedPreferences(getContext());

        addDeviceButton.setOnClickListener(v -> {

             data
                    .edit()
                    .putBoolean(MainApplication.PREFERENCE_AUTHENTICATED, true)
                    .putString(MainApplication.PREFERENCE_UNIQUEID, uniqueId.getText().toString())
                    .putString(MainApplication.PREFERENCE_NAME, name.getText().toString())
                     .putString(MainApplication.PREFERENCE_PHONE, phone.getText().toString())
                    .apply();

            addDevice();
        });

        return view;
    }

    private void addDevice() {
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
                requestBody.addProperty("disabled", false);

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
                String email = preferences.getString(PREFERENCE_EMAIL, null);
                final String password = preferences.getString(PREFERENCE_PASSWORD, null);

                String auth = Credentials.basic(email, password);

//                Map<String, String> headers = new HashMap<>();
//                headers.put("Content-Type",  "application/json");
//                headers.put("Authorization",  auth);
                RequestBody requestBody2=RequestBody.create(MediaType.parse("application/json; charset=utf-8"),requestBody.toString());
                service.addDevice("application/json", requestBody2).enqueue(new WebServiceCallback<Device>(getContext()) {
                    @Override
                    public void onSuccess(Response<Device> response) {
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
