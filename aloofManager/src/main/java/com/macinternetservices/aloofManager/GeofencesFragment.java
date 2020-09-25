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

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.macinternetservices.aloofManager.model.DataModel;
import com.macinternetservices.aloofManager.model.Device;
import com.macinternetservices.aloofManager.model.GeoLoc;
import com.macinternetservices.aloofManager.room.DatabaseClient;

import com.macinternetservices.aloofManager.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GeofencesFragment extends Fragment {
    private Map<Long, Device> devices = new HashMap<>();
    ArrayList<DataModel> dataModels;// = new ArrayList<>();
    RecyclerView mRecyckerView;
    private GeofencesRecyclerAdapter adapter;
    public static final String EXTRA_DEVICE_ID = "deviceId";
    public static final String EXTRA_DEVICE_NAME = "deviceName";
    Boolean batAlert = false;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_geofences, container, false);
        mRecyckerView = (RecyclerView) view.findViewById(R.id.recycler_view_geofences);
        adapter = new GeofencesRecyclerAdapter(getContext());
        mRecyckerView.setHasFixedSize(true);
        mRecyckerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyckerView.setAdapter(adapter);

        return view;
    }


    ArrayList<GeoLoc> geofenceNames = new ArrayList<>();

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

                geofenceNames.add(new GeoLoc(geoLoc.getName(), geoLoc.getLatitude(), geoLoc.getLongitude(), geoLoc.getId()));
            }
            adapter.notifyDataChange(geofenceNames);

        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        GetGeoLoc getGeoLoc = new GetGeoLoc();
        getGeoLoc.execute();
    }
}
