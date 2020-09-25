package com.macinternetservices.aloofManager.model;

import java.util.Date;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Position {
    //private Date fixTime;


    @SerializedName("positions")
    @Expose
    private List<Position> positions = null;

    public List<Position> getPositions() {
        return positions;
    }

    public void setPositions(List<Position> positions) {
        this.positions = positions;
    }




    @SerializedName("id")
    @Expose
    private Integer id;
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
    private Date deviceTime;
    @SerializedName("fixTime")
    @Expose
    private Date fixTime;
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

    public Integer getId() {
        return id;
    }

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

    public Date getDeviceTime() {
        return deviceTime;
    }

        public void setDeviceTime(Date time) {
            this.deviceTime = time;
        }



    public Date getFixTime() {
        if (fixTime != null) {
            return new Date(fixTime.getTime());
        } else {
            return null;
        }
    }

    public void setTime(Date time) {
        setDeviceTime(time);
        setFixTime(time);
    }

    public void setFixTime(Date fixTime) {
        if (fixTime != null) {
            this.fixTime = new Date(fixTime.getTime());
        } else {
            this.fixTime = null;
        }
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