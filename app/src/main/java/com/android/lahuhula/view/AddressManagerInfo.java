package com.android.lahuhula.view;

/**
 * Created by lenovo on 2017/3/21.
 */

public class AddressManagerInfo {

    private String name;
    private String phoneNumber;
    private String address;
    private boolean isDefault;

    public AddressManagerInfo(String name, String number, String address) {
        this(name, number, address, false);
    }

    public AddressManagerInfo(String name, String number, String address, boolean isDefault) {
        this.name = name;
        this.phoneNumber = number;
        this.address = address;
        this.isDefault = isDefault;
    }

    public String getName() {
        return this.name;
    }

    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    public String getAddress() {
        return this.address;
    }

    public boolean isDefault() {
        return this.isDefault;
    }
}
