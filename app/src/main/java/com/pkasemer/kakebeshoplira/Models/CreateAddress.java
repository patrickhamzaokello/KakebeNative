package com.pkasemer.kakebeshoplira.Models;


import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class  CreateAddress {

    @SerializedName("user_id")
    @Expose
    private String userId;
    @SerializedName("district")
    @Expose
    private String district;
    @SerializedName("location")
    @Expose
    private String location;
    @SerializedName("phone")
    @Expose
    private String phone;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

}