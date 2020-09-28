package com.macinternetservices.aloofManager;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.macinternetservices.aloofManager.model.Tracking;

import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.Response;
import retrofit2.Retrofit;
import static com.macinternetservices.aloofManager.MainFragment.trackedDevice;



public class TrackFragment extends SupportMapFragment implements OnMapReadyCallback {
    private GoogleMap map;
    private String deviceId;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setHasOptionsMenu(true);
        getMapAsync(this);

        Bundle transitionDataBundle = this.getArguments();
        if(transitionDataBundle != null) {

            deviceId = getArguments().getString("deviceId");
            //String lastTransitionEndTime = getArguments().getString("lastTransitionEndTime");
            //String lastTransitionStartTime = getArguments().getString("lastTransitionStartTime");
        } else{
            Log.e("RouteFragment", "transitionDataBundle is null");
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                View view = getLayoutInflater(null).inflate(R.layout.view_info, null);
                ((TextView) view.findViewById(R.id.title)).setText(marker.getTitle());
                ((TextView) view.findViewById(R.id.details)).setText(marker.getSnippet());
                return view;
            }
        });
        tracking();
    }


    //Polyline polyline = null;
    private void tracking(){

        //dataModels= new ArrayList<>();
        final MainApplication application = (MainApplication) getActivity().getApplication();
        application.getServiceAsync(new MainApplication.GetServiceCallback() {

            @Override
            public void onServiceReady(OkHttpClient client, Retrofit retrofit, WebService service) {
                service.getDeviceTracking(deviceId).enqueue(new WebServiceCallback<List<Tracking>>(getContext()) {
                    @Override
                    public void onSuccess(Response<List<Tracking>> response) {
                        Log.e("Tracking", "Response: " +response.body());
                        if(response.body() != null){
                        for (Tracking tracking : response.body()) {
                            //tracking = true;
                            trackedDevice = tracking.getDeviceId().toString();
                            CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(tracking.getLatitude(), tracking.getLongitude()));
                            CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);
                            //marker.showInfoWindow();
                            map.moveCamera(center);
                            map.animateCamera(zoom);
                            //marker.showInfoWindow();
                            //displayRoute(/*getContext()*/);
                            }
                            Toast.makeText(getContext(), "Now Tracking: " + trackedDevice, Toast.LENGTH_SHORT).show();
                        }
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