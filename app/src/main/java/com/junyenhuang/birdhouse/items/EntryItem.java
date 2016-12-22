package com.junyenhuang.birdhouse.items;

import java.util.ArrayList;

public class EntryItem {
    private int id;
    private String name;
    private ArrayList<HouseEvent> criticals;
    private int highestPriority = 0;

    public EntryItem() {
        id = 0;
        name = "aaa";
        criticals = new ArrayList<>();
    }

    public void setHouseID(int id) {
        this.id = id;
    }

    public int getHouseID() {
        return id;
    }

    public void setHouseName(String name) {
        this.name = name;
    }

    public String getHouseName() {
        return name;
    }

    public void setCriticalEvents(ArrayList<HouseEvent> events) {
        criticals = events;
    }

    public ArrayList<HouseEvent> getCriticalEvents() {
        return criticals;
    }

    public void setHighestPriority(int value) {
        highestPriority = value;
    }

    public int getHighestPriority() {
        return highestPriority;
    }
}
