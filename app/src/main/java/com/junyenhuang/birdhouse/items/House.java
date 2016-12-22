package com.junyenhuang.birdhouse.items;

import java.util.ArrayList;

public class House {
    private int id;
    private String name;
    private boolean online = false;
    private String type;
    private String mac;
    private String elements_jsonstring;
    private ArrayList<Element> elements;
    private String settings_jsonstring;
    private SettingGroup settingGroup;

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setOnline(boolean value) {
        online = value;
    }

    public boolean isOnline() {
        return online;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setSettingString(String json) {
        settings_jsonstring = json;
    }

    public String getSettingString() {
        return settings_jsonstring;
    }

    public void setSettingGroup(SettingGroup newSettings) {
        settingGroup = newSettings;
    }

    public SettingGroup getSettingGroup() {
        return settingGroup;
    }

    public void setElements(ArrayList<Element> list) {
        elements = list;
    }

    public ArrayList<Element> getElements() {
        return elements;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public void setElementString(String json) {
        elements_jsonstring = json;
    }

    public String getElementString() {
        return elements_jsonstring;
    }
}
