package com.example.lawnmower.activities;

import android.content.Intent;
import android.os.Bundle;


import android.os.Handler;

import com.example.lawnmower.R;

public class LoadingScreen extends BaseAppCompatAcitivty {
    public final int LOAD_TIME = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loadingscreen);

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