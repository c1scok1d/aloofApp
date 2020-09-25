package com.macinternetservices.aloofManager.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class Geofence {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("attributes")
    @Expose
    private Attributes attributes;
    @SerializedName("calendarId")
    @Expose
    private Integer calendarId;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("area")
    @Expose
    private String area;

    private Geofence(){
    }
    public Geofence( String area, String name) {
        this();
        this.id = id;
        // this.calendarId = calendarId;
        this.name = name;
        this.description = description;
        this.area = area;

    }

    public Geofence(String name) {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Attributes getAttributes() {
        return attributes;
    }

    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
    }

    public Integer getCalendarId() {
        return calendarId;
    }

    public void setCalendarId(Integer calendarId) {
        this.calendarId = calendarId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public class Attributes {

    }

}