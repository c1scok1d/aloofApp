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

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.macinternetservices.aloofManager.R;

import com.macinternetservices.aloofManager.model.DataModel;
import com.macinternetservices.aloofManager.model.Device;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.OkHttpClient;
import retrofit2.Response;
import retrofit2.Retrofit;

public class DevicesFragment extends Fragment {
    private Map<Long, Device> devices = new HashMap<>();
    public static ArrayList<DataModel> dataModels;// = new ArrayList<>();
    RecyclerView mRecyckerView;
    private DevicesRecyclerAdapter adapter;
    public static final String EXTRA_DEVICE_ID = "deviceId";
    public static final String EXTRA_DEVICE_NAME = "deviceName";
    Boolean batAlert = false;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_devices, container, false);
        mRecyckerView = (RecyclerView) view.findViewById(R.id.recycler_view_devices);
        adapter = new DevicesRecyclerAdapter(getContext());
        mRecyckerView.setHasFixedSize(true);
        mRecyckerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyckerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        dataModels= new ArrayList<>();
        final MainApplication application = (MainApplication) getActivity().getApplication();
        application.getServiceAsync(new MainApplication.GetServiceCallback() {
            @Override
            public void onServiceReady(OkHttpClient client, Retrofit retrofit, WebService service) {
                service.getDevices().enqueue(new WebServiceCallback<List<Device>>(getContext()) {
                    @Override
                    public void onSuccess(Response<List<Device>> response) {
                        for (Device device : response.body()) {
                            if (device != null) {
                                //devices.put(device.getId(), device);
                                dataModels.add(new DataModel(device.getName(), device.getId(), device.getPhone(), device.getUniqueId()));
                            }
                        }
                        adapter.notifyDataChange(dataModels);
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
