package com.macinternetservices.aloofManager.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Route implements Parcelable {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("lastTransitionStartTime")
    @Expose
    private String lastTransitionStartTime;
    @SerializedName("lastTransitionEndTime")
    @Expose
    private String lastTransitionEndTime;
    @SerializedName("attributes")
    @Expose
    private Attributes attributes;
    @SerializedName("deviceId")
    @Expose
    private Integer deviceId;
    @SerializedName("type")
    @Expose
    private Object type;
    @SerializedName("protocol")
    @Expose
    private String protocol;
    @SerializedName("serverTime")
    @Expose
    private String serverTime;
    @SerializedName("deviceTime")
    @Expose
    private String deviceTime;
    @SerializedName("fixTime")
    @Expose
    private String fixTime;
    @SerializedName("outdated")
    @Expose
    private Boolean outdated;
    @SerializedName("valid")
    @Expose
    private Boolean valid;
    @SerializedName("latitude")
    @Expose
    private Double latitude;
    @SerializedName("longitude")
    @Expose
    private Double longitude;
    @SerializedName("altitude")
    @Expose
    private Double altitude;
    @SerializedName("speed")
    @Expose
    private Double speed;
    @SerializedName("course")
    @Expose
    private Double course;
    @SerializedName("address")
    @Expose
    private Object address;
    @SerializedName("accuracy")
    @Expose
    private Double accuracy;
    @SerializedName("network")
    @Expose
    private Object network;

    protected Route(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readInt();
        }
        if (in.readByte() == 0) {
            deviceId = null;
        } else {
            deviceId = in.readInt();
        }
        lastTransitionEndTime = in.readString();
        lastTransitionEndTime = in.readString();
        protocol = in.readString();
        serverTime = in.readString();
        deviceTime = in.readString();
        fixTime = in.readString();
        byte tmpOutdated = in.readByte();
        outdated = tmpOutdated == 0 ? null : tmpOutdated == 1;
        byte tmpValid = in.readByte();
        valid = tmpValid == 0 ? null : tmpValid == 1;
        if (in.readByte() == 0) {
            latitude = null;
        } else {
            latitude = in.readDouble();
        }
        if (in.readByte() == 0) {
            longitude = null;
        } else {
            longitude = in.readDouble();
        }
        if (in.readByte() == 0) {
            altitude = null;
        } else {
            altitude = in.readDouble();
        }
        if (in.readByte() == 0) {
            speed = null;
        } else {
            speed = in.readDouble();
        }
        if (in.readByte() == 0) {
            course = null;
        } else {
            course = in.readDouble();
        }
        if (in.readByte() == 0) {
            accuracy = null;
        } else {
            accuracy = in.readDouble();
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(id);
        }
        if (deviceId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(deviceId);
        }
        dest.writeString(lastTransitionEndTime);
        dest.writeString(lastTransitionEndTime);
        dest.writeString(protocol);
        dest.writeString(serverTime);
        dest.writeString(deviceTime);
        dest.writeString(fixTime);
        dest.writeByte((byte) (outdated == null ? 0 : outdated ? 1 : 2));
        dest.writeByte((byte) (valid == null ? 0 : valid ? 1 : 2));
        if (latitude == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(latitude);
        }
        if (longitude == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(longitude);
        }
        if (altitude == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(altitude);
        }
        if (speed == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(speed);
        }
        if (course == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(course);
        }
        if (accuracy == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(accuracy);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Route> CREATOR = new Creator<Route>() {
        @Override
        public Route createFromParcel(Parcel in) {
            return new Route(in);
        }

        @Override
        public Route[] newArray(int size) {
            return new Route[size];
        }
    };

    public Integer getId() {
        return id;
    }

    public String getlastTransitionStartTime(){ return lastTransitionStartTime;}

    public String getlastTransitionEndTime(){ return lastTransitionEndTime; }

    public void setId(Integer id) {
        this.id = id;
    }

    public Attributes getAttributes() {
        return attributes;
    }

    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
    }

    public Integer getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Integer deviceId) {
        this.deviceId = deviceId;
    }

    public Object getType() {
        return type;
    }

    public void setType(Object type) {
        this.type = type;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getServerTime() {
        return serverTime;
    }

    public void setServerTime(String serverTime) {
        this.serverTime = serverTime;
    }

    public String getDeviceTime() {
        return deviceTime;
    }

    public void setDeviceTime(String deviceTime) {
        this.deviceTime = deviceTime;
    }

    public String getFixTime() {
        return fixTime;
    }

    public void setFixTime(String fixTime) {
        this.fixTime = fixTime;
    }

    public Boolean getOutdated() {
        return outdated;
    }

    public void setOutdated(Boolean outdated) {
        this.outdated = outdated;
    }

    public Boolean getValid() {
        return valid;
    }

    public void setValid(Boolean valid) {
        this.valid = valid;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getAltitude() {
        return altitude;
    }

    public void setAltitude(Double altitude) {
        this.altitude = altitude;
    }

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public Double getCourse() {
        return course;
    }

    public void setCourse(Double course) {
        this.course = course;
    }

    public Object getAddress() {
        return address;
    }

    public void setAddress(Object address) {
        this.address = address;
    }

    public Double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Double accuracy) {
        this.accuracy = accuracy;
    }

    public Object getNetwork() {
        return network;
    }

    public void setNetwork(Object network) {
        this.network = network;
    }

    public class Attributes {

        @SerializedName("batteryLevel")
        @Expose
        private Double batteryLevel;
        @SerializedName("distance")
        @Expose
        private Double distance;
        @SerializedName("totalDistance")
        @Expose
        private Double totalDistance;
        @SerializedName("motion")
        @Expose
        private Boolean motion;

        public Double getBatteryLevel() {
            return batteryLevel;
        }

        public void setBatteryLevel(Double batteryLevel) {
            this.batteryLevel = batteryLevel;
        }

        public Double getDistance() {
            return distance;
        }

        public void setDistance(Double distance) {
            this.distance = distance;
        }

        public Double getTotalDistance() {
            return totalDistance;
        }

        public void setTotalDistance(Double totalDistance) {
            this.totalDistance = totalDistance;
        }

        public Boolean getMotion() {
            return motion;
        }

        public void setMotion(Boolean motion) {
            this.motion = motion;
        }

    }

}
