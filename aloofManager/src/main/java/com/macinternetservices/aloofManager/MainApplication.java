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

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import androidx.multidex.MultiDexApplication;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.macinternetservices.aloofManager.model.User;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class MainApplication extends MultiDexApplication {

    public static final String PREFERENCE_AUTHENTICATED = "authenticated";
    public static final String PREFERENCE_URL = "url";
    public static final String PREFERENCE_EMAIL = "email";
    public static final String PREFERENCE_PASSWORD = "password";
    public static final String PREFERENCE_UNIQUEID = "uniqueId";
    public static final String PREFERENCE_NAME ="name";
    public static final String PREFERENCE_PHONE = "phone";
    public static final String PREFERENCE_SPD_UNIT = "spd";
    public static final String PREFERENCE_12HR_FORMAT = "timeFormat";
    public static final String PREFERENCE_GEOFENCE_LAT = "GEOFENCE_LAT";
    public static final String PREFERENCE_GEOFENCE_LON = "GEOFENCE_LON";
    private static final String DEFAULT_SERVER = "https://demo.traccar.org"; // local - http://10.0.2.2:8082

    public interface GetServiceCallback {
        void onServiceReady(OkHttpClient client, Retrofit retrofit, WebService service);
        boolean onFailure();
    }

    private SharedPreferences preferences;
    //private SharedPreferences data;

    private OkHttpClient createDefaultOkHttpClient() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        interceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        return new OkHttpClient().newBuilder()
                .addInterceptor(interceptor)
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .cookieJar(new JavaNetCookieJar(cookieManager)).build();
    }


    private OkHttpClient client = createDefaultOkHttpClient();
    private WebService service;
    private Retrofit retrofit;
    private User user;
    //private Device device;

    private final List<GetServiceCallback> callbacks = new LinkedList<>();

    public void getServiceAsync(GetServiceCallback callback) {
        if (service != null) {
            callback.onServiceReady(client, retrofit, service);
        } else {
            if (callbacks.isEmpty()) {
                initService();
            }
            callbacks.add(callback);
        }
    }

    public WebService getService() { return service; }
    public User getUser() { return user; }
    //public Device getDevice(){ return device;}

    public void removeService() {
        service = null;
        user = null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        //data = PreferenceManager.getDefaultSharedPreferences(this);

        //if (!preferences.contains(PREFERENCE_URL)) {
            preferences.edit().putString(PREFERENCE_URL, DEFAULT_SERVER).apply();
    }

    private void initService() {
        final String url = preferences.getString(PREFERENCE_URL, null);
        String email = preferences.getString(PREFERENCE_EMAIL, null);
        final String password = preferences.getString(PREFERENCE_PASSWORD, null);
       // startClient();
       // String uniqueId = data.getString(PREFERENCE_UNIQUEID, null);
       // String phone = data.getString(PREFERENCE_PHONE, null);
        //String name = data.getString(PREFERENCE_NAME, null);

        client = createDefaultOkHttpClient();
        //Log.e("ttt",url);
        try {
            retrofit = new Retrofit.Builder()
                    .client(client)
                    .baseUrl(url)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(JacksonConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            for (GetServiceCallback callback : callbacks) {
                callback.onFailure();
            }
            callbacks.clear();
        }

        final WebService service = retrofit.create(WebService.class);


        service.addSession(email, password).enqueue(new WebServiceCallback<User>(this) {
            @Override
            public void onSuccess(Response<User> response) {
                MainApplication.this.service = service;
                MainApplication.this.user = response.body();
                for (GetServiceCallback callback : callbacks) {
                    callback.onServiceReady(client, retrofit, service);
                }
                callbacks.clear();
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                boolean handled = false;
                for (GetServiceCallback callback : callbacks) {
                    handled = callback.onFailure();
                }
                callbacks.clear();
                if (!handled) {
                    super.onFailure(call, t);
                    Log.e("addSession", "Failed:" +t);
                }
            }
        });
    }

    public void startClient() {
        try {
            Intent clientIntent = new Intent(getApplicationContext(),Class.forName("com.macinternetservices.aloofClient.MainActivity"));
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

}
