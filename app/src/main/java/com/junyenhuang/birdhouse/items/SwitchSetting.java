package com.junyenhuang.birdhouse.items;

public class SwitchSetting {
    private int humi_max;
    private int humi_min;
    private int period_interval_hour;
    private int period_duration_minute;
    private String start;
    private String stop;
    private int duration_hr;
    private int duration_min;
    private int mode;

    public void setHumidityMax(int max) {
        humi_max = max;
    }

    public int getHumidityMax() {
        return humi_max;
    }

    public void setHumidityMin(int min) {
        humi_min = min;
    }

    public int getHumidityMin() {
        return humi_min;
    }

    public void setInterval(int hour) {
        period_interval_hour = hour;
    }

    public int getInterval() {
        return period_interval_hour;
    }

    public void setDuration(int min) {
        period_duration_minute = min;
    }

    public int getDuration() {
        return period_duration_minute;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getStart() {
        return start;
    }

    public void setStop(String stop) {
        this.stop = stop;
    }

    public String getStop() {
        return stop;
    }

    public void setDurationHour(int hour) {
        duration_hr = hour;
    }

    public int getDurationHour() {
        return duration_hr;
    }

    public void setDurationMin(int minute) {
        duration_min = minute;
    }

    public int getDurationMin() {
        return duration_min;
    }

    public int getTotalDurationMinutes() {
        return (duration_hr * 60) + duration_min;
    }

    public void setTotalDurationMinutes(int total) {
        duration_hr = total / 60;
        duration_min = total % 60;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getMode() {
        return mode;
    }
}
