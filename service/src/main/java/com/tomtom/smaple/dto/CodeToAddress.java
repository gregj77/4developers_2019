package com.tomtom.smaple.dto;

public class CodeToAddress {
    private String code;
    private String address;

    public CodeToAddress(String code, String address) {
        this.code = code;
        this.address = address;
    }

    public String getCode() {
        return code;
    }

    public String getAddress() {
        return address;
    }
}
