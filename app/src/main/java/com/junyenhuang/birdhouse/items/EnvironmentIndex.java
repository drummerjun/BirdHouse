package com.junyenhuang.birdhouse.items;

public class EnvironmentIndex {
    private String timeString = "";
    private String temp = "";
    private String humidity = "";
    private String nh3 = "";

    public EnvironmentIndex() {
        String timeString = "";
        String temp = "";
        String humidity = "";
        String nh3 = "";
    }

    public void setTimeString(String time) {
        timeString = time;
    }

    public String getTimeString() {
        return timeString;
    }

    public void setTemperature(String temp) {
        this.temp = temp;
    }

    public String getTemperature() {
        return temp;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getNh3() {
        return nh3;
    }

    public void setNh3(String nh3) {
        this.nh3 = nh3;
    }
}
