package com.junyenhuang.birdhouse.items;

public class HouseEvent {
    private String timeString;
    private String key;
    private String description;
    private String value;
    private int key1;
    private int key2;
    private String houseName;
    private int priority;

    public HouseEvent() {
        timeString = "";
        key = "";
        description = "";
        value = "";
        key1 = 0;
        key2 = -1;
        houseName = "";
        priority = 0;
    }
    public void setTimeString(String value) {
        timeString = value;
    }

    public String getTimeString() {
        return timeString;
    }

    public void setEventKey(String value) {
        key = value;
    }

    public String getEventKey() {
        return key;
    }

    public void setDescription(String value) {
        description = value;
    }

    public String getDescription() {
        return description;
    }

    public void setEventValue(String value) {
        this.value = value;
    }

    public String getEventValue() {
        return value;
    }

    public void setElementKey(int key1) {
        this.key1 = key1;
    }

    public int getElementKey() {
        return key1;
    }

    public void setHouseKey(int key2) {
        this.key2 = key2;
    }

    public int getHouseKey() {
        return key2;
    }

    public void setHouseName(String name) {
        houseName = name;
    }

    public String getHouseName() {
        return houseName;
    }

    public void setPriority(int value) {
        priority = value;
    }

    public int getPriority() {
        return priority;
    }
}
