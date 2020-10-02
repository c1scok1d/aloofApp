package com.macinternetservices.aloofManager.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class Points implements Parcelable {
    private String lastTransitionEndTime, lastTransitionStartTime, deviceId;


    //@SerializedName("Points")
    //@Expose
    private List<Points> points = null;

    public Points(String lastTransitionEndTime, String lastTransitionStartTime, String deviceId) {
        this.lastTransitionEndTime = lastTransitionEndTime;
        this.lastTransitionStartTime = lastTransitionStartTime;
        this.deviceId = deviceId;

    }

    protected Points(Parcel in) {
        lastTransitionEndTime = in.readString();
        lastTransitionStartTime = in.readString();
        deviceId = in.readString();
        points = in.createTypedArrayList(Points.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(lastTransitionEndTime);
        dest.writeString(lastTransitionStartTime);
        dest.writeString(deviceId);
        dest.writeTypedList(points);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Points> CREATOR = new Creator<Points>() {
        @Override
        public Points createFromParcel(Parcel in) {
            return new Points(in);
        }

        @Override
        public Points[] newArray(int size) {
            return new Points[size];
        }
    };

    public List<Points> getPoints() {
        return points;
    }

    public void setPoints(List<Points> points) {
        this.points = points;
    }


    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getlastTransitionStartTime() {
        return lastTransitionStartTime;
    }

    public void setlastTransitionStartTime(String lastTransitionStartTime) {
        this.lastTransitionEndTime = lastTransitionEndTime;
    }

    public String getlastTransitionEndTime() {
        return lastTransitionEndTime;
    }

    public void setlastTransitionEndTime(String lastTransitionEndTime) {
        this.lastTransitionEndTime = lastTransitionEndTime;
    }
}