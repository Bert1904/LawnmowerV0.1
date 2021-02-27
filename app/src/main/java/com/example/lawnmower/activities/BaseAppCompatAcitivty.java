package com.example.lawnmower.activities;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lawnmower.LawnmowerApp;

public abstract class BaseAppCompatAcitivty extends AppCompatActivity {

    @Override
    protected void onPause() {
        super.onPause();
        LawnmowerApp.onPauseActivity();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LawnmowerApp.onResumeActivity();
    }
}
