/*
 * Copyright 2015 Anton Tananaev (anton.tananaev@gmail.com)
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
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import com.macinternetservices.aloofManager.model.Points;

public class TrackActivity extends AppCompatActivity {
    private static FragmentManager fragmentManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        fragmentManager = getSupportFragmentManager();//Get Fragment Manager

        Points points = getIntent().getParcelableExtra("transitionData");
        String fooBar = points.getDeviceId();

        TrackFragment foo = new TrackFragment();
        Bundle transitionDataBundle = getIntent().getExtras();
        //transitionDataBundle.putString("lastTransitionEndTime", points.getlastTransitionEndTime());
        //transitionDataBundle.putString("lastTransitionStartTime", points.getlastTransitionEndTime());
        //transitionDataBundle.putString("deviceId", points.getDeviceId());

        foo.setArguments(transitionDataBundle);

        /* Points points = getIntent().getParcelableExtra("transitionData");
        String deviceId = getIntent().getDataString();

        TrackFragment foo = new TrackFragment();
        Bundle transitionDataBundle = new Bundle();
        //transitionDataBundle.putString("lastTransitionEndTime", points.getlastTransitionEndTime());
        //transitionDataBundle.putString("lastTransitionStartTime", points.getlastTransitionEndTime());
        transitionDataBundle.putString("deviceId", getIntent().getDataString());

        foo.setArguments(transitionDataBundle); */

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.content_layout, foo)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
