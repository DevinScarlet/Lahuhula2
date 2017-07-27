package com.android.lahuhula.bean;

import java.io.Serializable;

/**
 * Created by Devin on 2017/6/18.
 */
public class MyAddress implements Serializable {

    private String mUserName = "";
    private String mUserNum = "";
    private String isDefaultAddress = "";
    private String mAddress = "";

    public MyAddress() {
    }

    public MyAddress(String userName, String userNum, String isAddress, String address) {
        mUserName = userName;
        mUserNum = userNum;
        isDefaultAddress = isAddress;
        mAddress = address;
    }

    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String userName) {
        mUserName = userName;
    }

    public String getUserNum() {
        return mUserNum;
    }

    public void setUserNum(String userNum) {
        mUserNum = userNum;
    }

    public String getDefaultAddress() {
        return isDefaultAddress;
    }

    public void setIsDefaultAddress(String isDefaultAddress) {
        this.isDefaultAddress = isDefaultAddress;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String address) {
        mAddress = address;
    }

    @Override
    public String toString() {
        return "MyAddress{" +
                "mUserName='" + mUserName + '\'' +
                ", mUserNum='" + mUserNum + '\'' +
                ", isDefaultAddress=" + isDefaultAddress +
                ", mAddress='" + mAddress + '\'' +
                '}';
    }
}
