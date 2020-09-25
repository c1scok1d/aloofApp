package com.macinternetservices.aloofManager;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

import com.macinternetservices.aloofManager.model.DataModel;
//import com.github.florent37.singledateandtimepicker.SingleDateAndTimePicker;
//import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog;
import com.google.android.material.snackbar.Snackbar;

import com.macinternetservices.aloofManager.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class DevicesRecyclerAdapter extends RecyclerView.Adapter<DevicesRecyclerAdapter.ViewHolder> {

    private ArrayList<DataModel> mDataSet = new ArrayList<>();
    //private Map<Long, Device> devices = new HashMap<>();
    private Context mContext;
    Activity activity = new Activity();
    SimpleDateFormat sdf =  new SimpleDateFormat("h:mm:ss a MM-dd-yyyy", Locale.US);

    final Calendar myCalendar = Calendar.getInstance();
    String startTime = null, endTime = null;


    DevicesRecyclerAdapter(Context mContext) {
        this.mContext = mContext;
    }

    void notifyDataChange(ArrayList<DataModel> dataSet) {
        mDataSet.clear();
        mDataSet.addAll(dataSet);
        notifyDataSetChanged();

    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.row_item, parent, false);
        //dialogView = View.inflate(activity, R.layout.date_time_picker, null);
        //alertDialog = new AlertDialog.Builder(activity).create();
        return new ViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final DataModel dataModel = mDataSet.get(position);
        //Log.e("ttt","D"+dataModel.getId());
        holder.txtName.setText(dataModel.getName());
        holder.txtType.setText(String.valueOf(dataModel.getUniqueId()));
        holder.txtPhone.setText(dataModel.getPhone());


        holder.edit.setOnClickListener(view -> {
                Snackbar.make(view, "Edit: " + dataModel.getId(), Snackbar.LENGTH_LONG)
                .setAction("No action", null).show();
            Intent editDevice = new Intent(mContext, EditDeviceActivity.class);
            editDevice.putExtra(DevicesFragment.EXTRA_DEVICE_ID, dataModel.getId());
            editDevice.putExtra("phone", dataModel.getPhone());
            editDevice.putExtra("name", dataModel.getName());
            mContext.startActivity(editDevice);
        });


        holder.txtRoute.setOnClickListener(view -> {
            SimpleDateFormat sdf =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ", Locale.US);
          /*  new SingleDateAndTimePickerDialog.Builder(mContext)
                    //.bottomSheet()
                    //.curved()
                    //.stepSizeMinutes(15)
                    //.displayHours(false)
                    //.displayMinutes(false)
                    //.todayText("aujourd'hui")
                    .displayListener(new SingleDateAndTimePickerDialog.DisplayListener() {
                        @Override
                        public void onDisplayed(SingleDateAndTimePicker picker) {
                            //retrieve the SingleDateAndTimePicker
                            //Log.e("DatePicker", " Start Date: " + sdf.format(picker.getDate()));
                        }
                    })

                    .title("Start")
                    .listener(new SingleDateAndTimePickerDialog.Listener() {
                        @Override
                        public void onDateSelected(Date date) {
                            startTime = sdf.format(date.getTime());
                            Log.e("DatePicker", " Start Date: " + sdf.format(date.getTime()));

                            Snackbar.make(view, "Show route for: " + dataModel.getId(), Snackbar.LENGTH_LONG)
                                    .setAction("No action", null).show();
                            Intent showRoute = new Intent(mContext, RouteActivity.class);
                            showRoute.putExtra(DevicesFragment.EXTRA_DEVICE_ID, dataModel.getId());
                            showRoute.putExtra("startTime", startTime);
                            showRoute.putExtra("endTime", endTime);
                            mContext.startActivity(showRoute);

                        }
                    }).display(); */
        });

        holder.geofence.setOnClickListener(view -> {
            Snackbar.make(view, "Show Route: " + dataModel.getId(), Snackbar.LENGTH_LONG)
                    .setAction("No action", null).show();
        });

        holder.delete.setOnClickListener(view -> {
            Snackbar.make(view, "Delete: " + dataModel.getId(), Snackbar.LENGTH_LONG)
                    .setAction("No action", null).show();

            Intent delDevice = new Intent(mContext, DelDeviceActivity.class);
            delDevice.putExtra(DevicesFragment.EXTRA_DEVICE_ID, dataModel.getId());
            delDevice.putExtra("phone", dataModel.getPhone());
            delDevice.putExtra("name", dataModel.getName());
            mContext.startActivity(delDevice);
        });

        holder.devicesImage.setOnClickListener(v -> {
           // if (activity != null) {
                //Device device = (Device) dataModel.getId(position);
                activity.setResult(
                        MainFragment.RESULT_SUCCESS, new Intent().putExtra(DevicesFragment.EXTRA_DEVICE_ID, dataModel.getId()));
                activity.finish();
            //}
        });
    }

    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth/*, int minute, int hour*/) {
            // TODO Auto-generated method stub
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            //myCalendar.set(Calendar.MINUTE, minute);
            //myCalendar.set(Calendar.HOUR_OF_DAY, hour);
            updateLabel();
        }

    };

    private void updateLabel() {
        //String myFormat = "MM/dd/yy"; //In which you need put here

        Log.e("DatePicker", "Start Date: " + sdf.format(myCalendar.getTime()));
        //edittext.setText(sdf.format(myCalendar.getTime()));
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    // View lookup cache
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtName;
        TextView txtType, txtRoute;
        TextView txtPhone;
        ImageView edit, delete, alert, geofence, devicesImage;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtPhone = (TextView) itemView.findViewById(R.id.phoneNo);
            txtType = (TextView) itemView.findViewById(R.id.type);
            txtName = (TextView) itemView.findViewById(R.id.name);
            txtRoute = itemView.findViewById(R.id.txtRoute);
            edit = (ImageView) itemView.findViewById(R.id.btnEdit);
            delete = (ImageView) itemView.findViewById(R.id.btndel);
            devicesImage = itemView.findViewById(R.id.devicesImage);
            alert = itemView.findViewById(R.id.alert);
            alert.setVisibility(View.GONE);
            geofence = itemView.findViewById(R.id.btngeofence);
        }
    }
}
