package com.macinternetservices.aloofManager.model;

public class DataModel {

    Long id;
    String name, area;
    String uniqueId;
    String phone;

    public DataModel(String name, String area) {
        this.name=name;
        this.area=area;
        this.phone=phone;
        this.uniqueId=uniqueId;

    }

    public DataModel(String name, long id, String phone, String uniqueId) {
        this.name = name;
        this .id = id;
        this.phone = phone;
        this.uniqueId = uniqueId;
    }

    public String getName() {
        return name;
    }

    public Long getId() {
        return id;
    }

    public String getArea() {
        return area;
    }

    public String getPhone(){return phone;}

    public String getUniqueId() {
        return uniqueId;
    }

}
