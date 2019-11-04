package com.tomtom.client;

public class CodeToAddress {
    private String code;
    private String address;


    public String getCode() {
        return code;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return getCode() + " --> " + getAddress();
    }
}
