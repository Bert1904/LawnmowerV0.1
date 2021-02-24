package com.example.lawnmower;

public class LawnmowerStatusData {
    private AppControlsProtos.LawnmowerStatus.Status status;
    private AppControlsProtos.LawnmowerStatus.Error error;
    private int batteryState;
    // maybe add latitude and longitude here so there is only one singeton
    private double latitude;
    private double longitude;
    private String error_msg;
    private boolean mowing_finished;
    private double mowing_process;
    private static final LawnmowerStatusData lawnmowerStatusData = new LawnmowerStatusData();

    private LawnmowerStatusData() {
    }

    public static LawnmowerStatusData getInstance() {
        return lawnmowerStatusData;
    }

    public void setStatus(AppControlsProtos.LawnmowerStatus.Status status) {
        this.status = status;
    }

    public void setError(AppControlsProtos.LawnmowerStatus.Error error) {
        this.error = error;
    }

    public void setBatteryState(int batteryState) {
        this.batteryState = batteryState;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setError_msg(String error_msg) {
        this.error_msg = error_msg;
    }

    public void setMowing_finished(boolean mowing_finished) {
        this.mowing_finished = mowing_finished;
    }
    public void setMowing_process(double process){this.mowing_process = mowing_process;}

    public AppControlsProtos.LawnmowerStatus.Status getStatus() {
        return status;
    }

    public AppControlsProtos.LawnmowerStatus.Error getError() {
        return error;
    }

    public int getBatteryState() {
        return batteryState;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getError_msg() {
        return error_msg;
    }

    public boolean getMowing_finished() {
        return mowing_finished;
    }
    public double getMowing_process(){return mowing_process;}

}