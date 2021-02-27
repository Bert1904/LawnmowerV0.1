package com.example.lawnmower.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.example.lawnmower.R;
import com.example.lawnmower.activities.BaseAppCompatAcitivty;

public class InfoActivity extends BaseAppCompatAcitivty {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        OnClickListener ocl = new OnClickListener();
        ocl.run();
    }
    public class OnClickListener implements  Runnable {

        @Override
        public void run() {
            ImageButton roblabImageButton = (ImageButton)findViewById(R.id.lawnmower);
            roblabImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri uri = Uri.parse(getString(R.string.roblab_adresse));
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
            });


            ImageButton whsImageButton = (ImageButton) findViewById(R.id.whs);
            whsImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri uri = Uri.parse(getString(R.string.whs_adresse));
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
            });
        }
    }
}