package com.junyenhuang.birdhouse.items;

public class Element {
    private int id;
    private String name;
    private String type;
    private String unit = "";
    private Object value;
    private int iconID;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public int getIconID() {
        return iconID;
    }

    public void setIconID(int id) {
        iconID = id;
    }
}
