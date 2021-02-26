package com.example.lawnmower;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import android.widget.ImageView;
import android.widget.TextView;

import java.net.Socket;


public class HomeActivity extends BaseAppCompatAcitivty implements LawnmowerStatusDataChangedListener{
    private ImageButton buttonMow;
    private ImageButton buttonInfo;
    private ImageButton buttonControl;
    private ImageButton buttonSettings;
    private ImageButton buttonMap;
    private ImageButton buttonWeather;
    private ImageView batteryStatusIcon;
    private TextView batteryStatus;
    private BatteryStatusHandler bshandler;
    private Socket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        buttonSettings=   findViewById(R.id.buttonSettings);
        batteryStatusIcon = findViewById(R.id.batteryStatusIconMow);
        batteryStatus = findViewById(R.id.batteryStatusMow);
        bshandler = new BatteryStatusHandler(batteryStatusIcon);
        buttonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSetting();
            }
        });
        /*String status = "Not Connected";
        if(socket.isConnected()) {
            status = "Connected";
        }
        statusView.setText(status);*/

        buttonControl=   findViewById(R.id.buttonControl);
        buttonControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openControl();
            }
        });

        buttonInfo =  findViewById(R.id.buttonInfo);
        buttonInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openInfo();
            }
        });

        buttonMow = findViewById(R.id.buttonMow);
        buttonMow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMeinmaeher();
            }
        });


        buttonMap =(ImageButton) findViewById(R.id.buttonMap);
        buttonMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMap();
            }
        });

        buttonWeather =(ImageButton) findViewById(R.id.buttonWeather);
        buttonWeather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWeather();
            }
        });
        init();
    }
    /*public void openMap(){
        Intent intent = new Intent(this, com.example.lawnmower.map.MapMain.class);
        startActivity(intent);
    }*/
    public void openWeather(){
        Intent intent = new Intent(this, WeatherActivity.class);
        startActivity(intent);
    }
    public void openSetting(){
        Intent intent = new Intent(this, Einstellungen.class);
        startActivity(intent);
    }
    public void openInfo(){
        Intent intent = new Intent(this, Info.class);
        startActivity(intent);
    }
    public void openMeinmaeher(){
        Intent intent = new Intent(this, MeinMaeher.class);
        startActivity(intent);
    }
    public void openControl(){
        Intent intent = new Intent(this, Steuerung.class);
        startActivity(intent);
    }
    public void openMap(){
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    public void init() {
        if(!SocketService.getInstance().isConnected()) {
            batteryStatusIcon.setVisibility(View.INVISIBLE);
            batteryStatus.setVisibility(View.INVISIBLE);
        } else {
            batteryStatusIcon.setVisibility(View.VISIBLE);
            batteryStatus.setVisibility(View.VISIBLE);
        }
    }

    private void setBatteryState(float batteryState) {

        if(batteryState > 90.0f) {
            bshandler.setView(getResources().getIdentifier("@drawable/batteryfull", null, getPackageName()));
        } else if (batteryState > 70.0f) {
            bshandler.setView(getResources().getIdentifier("@drawable/battery80", null, getPackageName()));
        } else if (batteryState > 50.0f) {
            bshandler.setView(getResources().getIdentifier("@drawable/battery60", null, getPackageName()));
        } else if (batteryState > 30.0f) {
            bshandler.setView(getResources().getIdentifier("@drawable/battery40", null, getPackageName()));
        } else if (batteryState > 10.0f){
            bshandler.setView(getResources().getIdentifier("@drawable/battery20", null, getPackageName()));
        } else {
            bshandler.setView(getResources().getIdentifier("@drawable/battery0", null, getPackageName()));
        }
        if(batteryStatus.getVisibility() == View.INVISIBLE) {
            batteryStatus.setVisibility(View.VISIBLE);
        }
        batteryStatus.setText("" + (int)batteryState + "%");
    }

    @Override
    public void onLSDChange() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setBatteryState(LawnmowerStatusData.getInstance().getLawnmowerStatus().getBatteryState());
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        LSDListenerManager.removeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        LSDListenerManager.addListener(this);
    }
}