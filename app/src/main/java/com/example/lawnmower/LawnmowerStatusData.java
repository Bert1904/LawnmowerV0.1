package com.example.lawnmower;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class LawnmowerStatusData {
    /*private int batteryState;
    private double mowing_progress;
    private AppControlsProtos.LawnmowerStatus.Status status;
    private AppControlsProtos.LawnmowerStatus.Error error;
    // maybe add latitude and longitude here so there is only one singeton
    private double latitude;
    private double longitude;
    private String error_msg;
    private boolean mowing_finished;*/
    private AppControlsProtos.LawnmowerStatus lawnmowerStatus = null;

    private static final LawnmowerStatusData lawnmowerStatusData = new LawnmowerStatusData();

    private LawnmowerStatusData() {
    }

    public static LawnmowerStatusData getInstance() {
        return lawnmowerStatusData;
    }

    public void setLawnmowerStatus(AppControlsProtos.LawnmowerStatus lawnmowerStatus1) {
        Log.i("LawnmowerStatusData","LawnmowerStatusData changed");
        LSDListenerManager.notifyOnChange();
        this.lawnmowerStatus = lawnmowerStatus1;
    }

    public AppControlsProtos.LawnmowerStatus getLawnmowerStatus() {
        return lawnmowerStatus;
    }

    /*public void setStatus(AppControlsProtos.LawnmowerStatus.Status status) {
        notifyOnChangeListeners();
        this.status = status;
    }

    public void setError(AppControlsProtos.LawnmowerStatus.Error error) {
        notifyOnChangeListeners();
        this.error = error;
    }

    public void setBatteryState(int batteryState) {
        notifyOnChangeListeners();
        this.batteryState = batteryState;
    }

    public void setLatitude(double latitude) {
        notifyOnChangeListeners();
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        notifyOnChangeListeners();
        this.longitude = longitude;
    }

    public void setError_msg(String error_msg) {
        notifyOnChangeListeners();
        this.error_msg = error_msg;
    }

    public void setMowing_finished(boolean mowing_finished) {
        this.mowing_finished = mowing_finished;
    }

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
    }*/
}