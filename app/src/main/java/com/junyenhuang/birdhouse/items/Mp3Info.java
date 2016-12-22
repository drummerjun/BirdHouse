package com.junyenhuang.birdhouse.items;

public class Mp3Info {
    private int _id;
    private int house_id;
    private int mp3_id;
    private String start;
    private String stop;
    private int duration_hr;
    private int duration_min;
    private int vol;

    public Mp3Info() {
        mp3_id = 1;
        start = "12:00";
        stop = "13:00";
        vol = 15;
        duration_hr = 1;
        duration_min = 0;
    }

    public void setHouseId(int id) {
        house_id = id;
    }

    public int getHouseId() {
        return house_id;
    }

    public void setSongId(int id) {
        mp3_id = id;
    }

    public int getSongId() {
        return mp3_id;
    }

    public void setId(int id) {
        _id = id;
    }

    public int getId() {
        return _id;
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

    public void setTotalDurationMinutes(int total) {
        duration_hr = total / 60;
        duration_min = total % 60;
    }

    public int getTotalDurationMinutes() {
        return (duration_hr * 60) + duration_min;
    }

    public void setVol(int vol) {
        this.vol = vol;
    }

    public int getVol() {
        return vol;
    }
}
