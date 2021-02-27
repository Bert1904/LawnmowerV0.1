package com.example.lawnmower.data;

import com.example.lawnmower.AppControlsProtos;

public class LawnmowerStatusData {

    private AppControlsProtos.LawnmowerStatus lawnmowerStatus = null;

    private static final LawnmowerStatusData lawnmowerStatusData = new LawnmowerStatusData();

    private LawnmowerStatusData() {
    }

    public static LawnmowerStatusData getInstance() {
        return lawnmowerStatusData;
    }

    public void setLawnmowerStatus(AppControlsProtos.LawnmowerStatus lawnmowerStatus) {
        this.lawnmowerStatus = lawnmowerStatus;
        LSDListenerManager.notifyOnChange();
    }

    public AppControlsProtos.LawnmowerStatus getLawnmowerStatus() {
        return lawnmowerStatus;
    }
}