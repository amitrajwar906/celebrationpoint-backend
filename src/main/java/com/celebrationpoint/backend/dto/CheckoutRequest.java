package com.celebrationpoint.backend.dto;

public class CheckoutRequest {

    private String fullName;
    private String phone;
    private String addressLine;
    private String city;
    private String state;
    private String pincode;

    public String getFullName() {
        return fullName;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddressLine() {
        return addressLine;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getPincode() {
        return pincode;
    }
}
