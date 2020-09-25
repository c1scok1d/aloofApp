package com.macinternetservices.aloofManager;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.macinternetservices.aloofManager.R;
public class DelDeviceActivity extends AppCompatActivity {
    private static FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout);



        //String uniqueId = bundle.getString("deviceId");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        fragmentManager = getSupportFragmentManager();//Get Fragment Manager


        if (savedInstanceState == null) {

            Bundle bundle = getIntent().getExtras();
            /*Long foo = bundle.getLong("deviceId");
            String name = bundle.getString("name");
            String phone = bundle.getString("phone");*/
            Fragment argumentFragment = new DelDeviceFragment();//Get Fragment Instance
            Bundle data = new Bundle();//Use bundle to pass data
            data.putLong("deviceId", bundle.getLong("deviceId"));//put string, int, etc in bundle with a key value
            data.putString("name", bundle.getString("name"));
            data.putString("phone", bundle.getString("phone"));
            argumentFragment.setArguments(data);//Finally
            //argumentFragment.setTargetFragment(DelDeviceFragment,);

            fragmentManager
                    .beginTransaction().replace(R.id.content_layout, argumentFragment)
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
