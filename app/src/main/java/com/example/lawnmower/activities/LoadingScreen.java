package com.example.lawnmower.activities;

import android.content.Intent;
import android.os.Bundle;


import android.os.Handler;

import com.example.lawnmower.AppControlsProtos;
import com.example.lawnmower.R;
import com.example.lawnmower.data.LawnmowerStatusData;

public class LoadingScreen extends BaseAppCompatAcitivty {
    public final int LOAD_TIME = 3000;
    //private AppControlsProtos.LawnmowerStatus testStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loadingscreen);

        /*testStatus = AppControlsProtos.LawnmowerStatus.newBuilder().setStatus(AppControlsProtos.LawnmowerStatus.Status.PAUSED)
                .setError(AppControlsProtos.LawnmowerStatus.Error.NO_ERROR)
                .setBatteryState(65f)
                .setMowingProgress(0)
                .setLatitude(6.9642606)
                .setLongitude(51.6559681)
                .setErrorMsg("Motoren drehen durch")
                .setMowingFinished(false).build();
        LawnmowerStatusData.getInstance().setLawnmowerStatus(testStatus);*/

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent= new Intent(LoadingScreen.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        },LOAD_TIME);

    }
}