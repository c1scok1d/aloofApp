package com.macinternetservices.aloofManager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.material.snackbar.Snackbar;

import com.macinternetservices.aloofManager.R;

import com.macinternetservices.aloofManager.model.GeoLoc;
import com.macinternetservices.aloofManager.room.DatabaseClient;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static com.macinternetservices.aloofManager.DevicesFragment.EXTRA_DEVICE_ID;

public class GeofencesRecyclerAdapter extends RecyclerView.Adapter<GeofencesRecyclerAdapter.ViewHolder> {

    private ArrayList<GeoLoc> mDataSet = new ArrayList<>();
    //private Map<Long, Device> devices = new HashMap<>();
    private Context mContext;
    Activity activity = new Activity();
    SimpleDateFormat sdf =  new SimpleDateFormat("h:mm:ss a MM-dd-yyyy", Locale.US);

    final Calendar myCalendar = Calendar.getInstance();
    String startTime = null, endTime = null;


    GeofencesRecyclerAdapter(Context mContext) {
        this.mContext = mContext;
    }

    void notifyDataChange(ArrayList<GeoLoc> dataSet) {
        mDataSet.clear();
        mDataSet.addAll(dataSet);
        notifyDataSetChanged();

    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.geofence_row_item, parent, false);
        return new ViewHolder(listItem);
    }

    String address = "address";
    String bldgno = "bldgno";
    String street = "street";
    String city = "city";
    String state = "state";
    String postalCode = "zipcode";
    //Double speed;
    String country = null;
    Geocoder geocoder;
    List<Address> addresses;

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        // get address for device lat/lng from google api
        geocoder = new Geocoder(mContext, Locale.getDefault());
        final GeoLoc dataModel = mDataSet.get(position);

        address = "address";
        bldgno = "bldgno";
        street = "street";
        city = "city";
        state = "state";
        postalCode = "zipcode";
        country = null;
        try {
            addresses = geocoder.getFromLocation(dataModel.getLatitude(), dataModel.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            bldgno = addresses.get(0).getSubThoroughfare(); // building number
            street = addresses.get(0).getThoroughfare(); //street name
            city = addresses.get(0).getLocality();
            state = addresses.get(0).getAdminArea();
            country = addresses.get(0).getCountryName();
            postalCode = addresses.get(0).getPostalCode();
        } catch (IOException e) {
            e.printStackTrace();
        }

        holder.txtName.setText(dataModel.getName());
        holder.txtBldgNo.setText(bldgno);
        holder.txtStreet.setText(street);
        holder.txtCity.setText(city);
        holder.txtState.setText(state);
        holder.txtZip.setText(postalCode);


        holder.edit.setOnClickListener(view -> {
                Snackbar.make(view, "Edit: " + dataModel.getId(), Snackbar.LENGTH_LONG)
                .setAction("No action", null).show();
            Intent editDevice = new Intent(mContext, EditDeviceActivity.class);
            editDevice.putExtra(EXTRA_DEVICE_ID, dataModel.getId());
            editDevice.putExtra("lat", dataModel.getLatitude());
            editDevice.putExtra("lng", dataModel.getLongitude());
            mContext.startActivity(editDevice);
        });

        holder.delete.setOnClickListener(view -> {
            Snackbar.make(view, "Delete: " + dataModel.getId(), Snackbar.LENGTH_LONG)
                    .setAction("No action", null).show();


            class deleteGeofence extends AsyncTask<Void, Void, Void> {

                @Override
                protected Void doInBackground(Void... voids) {

                    DatabaseClient.getInstance(mContext).getAppDatabase()
                            .dataDao()
                            .delete(dataModel);

                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);

                }
            }

            deleteGeofence deleteTask = new deleteGeofence();
            deleteTask.execute();

           Intent mainActivity = new Intent(mContext, MainActivity.class);
            mContext.startActivity(mainActivity);
        });

        holder.devicesImage.setOnClickListener(v -> {
           // if (activity != null) {
                //Device device = (Device) dataModel.getId(position);
                activity.setResult(
                        MainFragment.RESULT_SUCCESS, new Intent().putExtra(EXTRA_DEVICE_ID, dataModel.getId()));
                activity.finish();
            //}
        });
    }
    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    // View lookup cache
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtName;
        TextView  txtRoute, txtCity, txtState, txtZip, txtBldgNo, txtStreet;
        //TextView txtPhone;
        ImageView edit, delete, geofence, devicesImage;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
          //  txtPhone = (TextView) itemView.findViewById(R.id.phoneNo);
            txtBldgNo = (TextView) itemView.findViewById(R.id.txtBldgNo);
            txtStreet = (TextView) itemView.findViewById(R.id.txtStreet);
            txtCity = (TextView) itemView.findViewById(R.id.txtCity);
            txtState = (TextView) itemView.findViewById(R.id.txtState);
            txtZip = (TextView) itemView.findViewById(R.id.txtZip);

            //txtType = (TextView) itemView.findViewById(R.id.type);
            txtName = (TextView) itemView.findViewById(R.id.name);
            txtRoute = itemView.findViewById(R.id.txtRoute);
            edit = (ImageView) itemView.findViewById(R.id.btnEdit);
            delete = (ImageView) itemView.findViewById(R.id.btndel);
            devicesImage = itemView.findViewById(R.id.devicesImage);
  //          alert = itemView.findViewById(R.id.alert);
//            alert.setVisibility(View.GONE);
            geofence = itemView.findViewById(R.id.btngeofence);
        }
    }
}
