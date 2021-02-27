package com.example.lawnmower.viewhandler;

import android.widget.ImageView;

/**
 * Updates the ui and set imageview on "MeinMaeher" if lawnmower status is changed
 */
public class BatteryStatusHandler extends Thread {
    public ImageView BatteryState;

    public BatteryStatusHandler(ImageView BatteryState) {
        this.BatteryState = BatteryState;
    }

    /*
     * Set suitable imageResource of the status
     * Updates ui via setVibility
     */
    public void setView(int imgageResource) {
        this.BatteryState.setVisibility(BatteryState.GONE);
        this.BatteryState.setImageResource(imgageResource);
        BatteryState.setVisibility(BatteryState.VISIBLE);
    }
}