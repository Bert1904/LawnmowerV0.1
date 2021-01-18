package com.example.lawnmower;

import androidx.appcompat.app.AppCompatActivity;

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
