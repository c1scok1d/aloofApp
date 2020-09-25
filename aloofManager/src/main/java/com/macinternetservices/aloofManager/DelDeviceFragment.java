package com.macinternetservices.aloofManager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

public class DelDeviceFragment extends Fragment {

    private TextView uniqueId;
    private TextView name;
    private TextView phone;
    private Button delDeviceButton;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_del_device, container, false);
        //Get Argument that passed from activity in "data" key value
        Bundle bundle = getArguments();

        uniqueId =  view.findViewById(R.id.input_uniqueId);
        phone =  view.findViewById(R.id.input_phone);
        name = view.findViewById(R.id.input_name);
        delDeviceButton = view.findViewById(R.id.button_del_device);

        uniqueId.setText(String.valueOf(bundle.getLong("deviceId")));
        phone.setText(bundle.getString("phone"));
        name.setText(bundle.getString("name"));

        final SharedPreferences data = PreferenceManager.getDefaultSharedPreferences(getContext());

        delDeviceButton.setOnClickListener(v -> {

             data
                    .edit()
                    .putBoolean(MainApplication.PREFERENCE_AUTHENTICATED, true)
                    .putString(MainApplication.PREFERENCE_UNIQUEID, uniqueId.getText().toString())
                    //.putString(MainApplication.PREFERENCE_NAME, name.getText().toString())
                    //.putString(MainApplication.PREFERENCE_PHONE, phone.getText().toString())
                    .apply();

            delDevice();
        });

        return view;
    }

    private void delDevice() {
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
               // requestBody.addProperty("name", name.getText().toString());
                //requestBody.addProperty("phone", phone.getText().toString());
                //requestBody.addProperty("disabled", false);

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
                String email = preferences.getString(PREFERENCE_EMAIL, null);
                final String password = preferences.getString(PREFERENCE_PASSWORD, null);

                String auth = Credentials.basic(email, password);

//                Map<String, String> headers = new HashMap<>();
//                headers.put("Content-Type",  "application/json");
//                headers.put("Authorization",  auth);
                RequestBody requestBody2=RequestBody.create(MediaType.parse("application/json; charset=utf-8"),requestBody.toString());
                service.delDevice("application/json", uniqueId.getText().toString()).enqueue(new WebServiceCallback<Device>(getContext()) {
                    @Override
                    public void onSuccess(Response<Device> response) {
                        Toast.makeText(getContext(), "Device " +uniqueId.getText().toString()+ " has been deleted", Toast.LENGTH_LONG).show();
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
