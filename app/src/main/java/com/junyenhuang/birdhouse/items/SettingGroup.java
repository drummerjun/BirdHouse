package com.junyenhuang.birdhouse.items;

import java.util.ArrayList;

public class SettingGroup {
    public ArrayList<FrontIrs> front;
    public ArrayList<IrsSetting> irs;
    //public ArrayList<Mp3Info> mp3_list;
    public ArrayList<SwitchSetting> switches;
    private int id;
    private int nh3Limit;
    private String houseName;
    private String phoneNumber;
    private String[] smsNumbers;
    private boolean powerOff;
    private boolean powerOn;

    public SettingGroup() {
        front = new ArrayList<>();
        irs = new ArrayList<>();
        //mp3_list = new ArrayList<>();
        switches = new ArrayList<>();
        id = 0;
        nh3Limit = 1000;
        phoneNumber = "";
        powerOff = true;
        powerOn = true;
        smsNumbers = new String[] {"", "", "", "", ""};
    }

    public void setHouseId(int id) {
        this.id = id;
    }

    public int getHouseId() {
        return id;
    }

    public void setNh3Limit(int limit) {
        nh3Limit = limit;
    }

    public int getNh3Limit() {
        return nh3Limit;
    }

    public void setHouseName(String name) {
        houseName = name;
    }

    public String getHouseName() {
        return houseName;
    }

    public void setPhoneNumber(String phone) {
        phoneNumber = phone;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPowerOff(boolean value) {
        powerOff = value;
    }

    public boolean getPowerOff() {
        return powerOff;
    }

    public void setPowerOn(boolean value) {
        powerOn = value;
    }

    public boolean getPowerOn() {
        return powerOn;
    }

    public void setSmsNumbers(String[] nums) {
        smsNumbers = nums;
    }

    public String[] getSmsNumbers() {
        return smsNumbers;
    }
}
