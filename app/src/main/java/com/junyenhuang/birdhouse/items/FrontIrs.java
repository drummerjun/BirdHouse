package com.junyenhuang.birdhouse.items;

public class FrontIrs {
    private int action = 0;
    private String start_time;
    private int stop_mins;

    public FrontIrs() {
        action = 0;
        start_time = "08:00";
        stop_mins = 599;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public int getAction() {
        return action;
    }

    public void setStartTime(String time) {
        start_time = time;
    }

    public String getStartTime() {
        return start_time;
    }

    public void setDurationTotalMinutes(int minutes) {
        stop_mins = minutes;
    }

    public int getDurationTotalMinutes() {
        return stop_mins;
    }
}
