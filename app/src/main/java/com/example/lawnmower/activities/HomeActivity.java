package com.example.lawnmower.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import android.widget.ImageView;
import android.widget.TextView;

import com.example.lawnmower.AppControlsProtos;
import com.example.lawnmower.viewhandler.BatteryStatusHandler;
import com.example.lawnmower.data.LSDListenerManager;
import com.example.lawnmower.data.LawnmowerStatusData;
import com.example.lawnmower.data.LawnmowerStatusDataChangedListener;
import com.example.lawnmower.R;
import com.example.lawnmower.data.SocketService;

import java.net.Socket;


public class HomeActivity extends BaseAppCompatAcitivty implements LawnmowerStatusDataChangedListener {
    private ImageButton buttonMow;
    private ImageButton buttonInfo;
    private ImageButton buttonControl;
    private ImageButton buttonSettings;
    private ImageButton buttonMap;
    private ImageButton buttonWeather;
    private ImageView batteryStatusIcon;
    private ImageView errorIcon;
    private TextView batteryStatus;
    private ImageView connectionStatus;
    private TextView lawnmowerStatus;
    private TextView errorMsg;
    private BatteryStatusHandler bshandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        buttonSettings=   findViewById(R.id.buttonSettings);
        batteryStatusIcon = findViewById(R.id.batteryStatusIconHome);
        batteryStatus = findViewById(R.id.batteryStatusHome);
        bshandler = new BatteryStatusHandler(batteryStatusIcon);
        lawnmowerStatus = findViewById(R.id.lawnmowerStatus);
        connectionStatus = findViewById(R.id.connectionStatus);
        errorIcon = findViewById(R.id.errorIcon);
        errorMsg = findViewById(R.id.errorMsg);
        buttonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSetting();
            }
        });

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

    private class testThread extends Thread {
        @Override
        public void run() {
            try {
                sleep(500);
                AppControlsProtos.LawnmowerStatus status;
                status = AppControlsProtos.LawnmowerStatus.newBuilder().setStatus(AppControlsProtos.LawnmowerStatus.Status.READY).setBatteryState(80f).setMowingProgress(20).build();
                LawnmowerStatusData.getInstance().setLawnmowerStatus(status);
                sleep(2000);
                status = AppControlsProtos.LawnmowerStatus.newBuilder().setStatus(AppControlsProtos.LawnmowerStatus.Status.MOWING).setBatteryState(79f).setMowingProgress(20).build();
                LawnmowerStatusData.getInstance().setLawnmowerStatus(status);
                sleep(2000);
                status = AppControlsProtos.LawnmowerStatus.newBuilder().setStatus(AppControlsProtos.LawnmowerStatus.Status.PAUSED).setBatteryState(78f).setMowingProgress(20).build();
                LawnmowerStatusData.getInstance().setLawnmowerStatus(status);
                sleep(2000);
                status = AppControlsProtos.LawnmowerStatus.newBuilder().setStatus(AppControlsProtos.LawnmowerStatus.Status.MANUAL).setBatteryState(77f).setMowingProgress(20).build();
                LawnmowerStatusData.getInstance().setLawnmowerStatus(status);
                sleep(2000);
                status = AppControlsProtos.LawnmowerStatus.newBuilder().setStatus(AppControlsProtos.LawnmowerStatus.Status.TRACKING).setBatteryState(76f).setMowingProgress(20).build();
                LawnmowerStatusData.getInstance().setLawnmowerStatus(status);
                sleep(2000);
                status = AppControlsProtos.LawnmowerStatus.newBuilder().setStatus(AppControlsProtos.LawnmowerStatus.Status.LOW_LIGHT).setBatteryState(75f).setMowingProgress(20).build();
                LawnmowerStatusData.getInstance().setLawnmowerStatus(status);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void openWeather(){
        Intent intent = new Intent(this, WeatherActivity.class);
        startActivity(intent);
    }
    public void openSetting(){
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
    public void openInfo(){
        Intent intent = new Intent(this, InfoActivity.class);
        startActivity(intent);
    }
    public void openMeinmaeher(){
        Intent intent = new Intent(this, MyMowerActivity.class);
        startActivity(intent);
    }
    public void openControl(){
        Intent intent = new Intent(this, ControlActivity.class);
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
            connectionStatus.setVisibility(View.GONE);
            connectionStatus.setImageResource(getResources().getIdentifier("@drawable/notconnected", null, getPackageName()));
            lawnmowerStatus.setText("Nicht verbunden");
        } else {
            batteryStatusIcon.setVisibility(View.VISIBLE);
            batteryStatus.setVisibility(View.VISIBLE);
            connectionStatus.setVisibility(View.GONE);
            connectionStatus.setImageResource(getResources().getIdentifier("@drawable/connected", null, getPackageName()));
        }
        errorMsg.setVisibility(View.INVISIBLE);
        errorIcon.setVisibility(View.INVISIBLE);
        connectionStatus.setVisibility(View.VISIBLE);
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

    private void updateLawnmowerStatus(AppControlsProtos.LawnmowerStatus.Status status) {
        String s;
        if(LawnmowerStatusData.getInstance().getLawnmowerStatus().getError() != AppControlsProtos.LawnmowerStatus.Error.NO_ERROR) {
            if (status == AppControlsProtos.LawnmowerStatus.Status.READY) {
                s = "Status: Bereit";
            } else if (status == AppControlsProtos.LawnmowerStatus.Status.MOWING) {
                s = "Status: Mähen";
            } else if (status == AppControlsProtos.LawnmowerStatus.Status.PAUSED) {
                s = "Status: Pause";
            } else if (status == AppControlsProtos.LawnmowerStatus.Status.MANUAL) {
                s = "Status: Manuell";
            } else if (status == AppControlsProtos.LawnmowerStatus.Status.TRACKING) {
                s = "Status: Tracking";
            } else {
                s = "Wenig Licht";
            }
        } else {
            s = "Status: Error";
        }
        lawnmowerStatus.setText(s);
    }

    private void handleError() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(LawnmowerStatusData.getInstance().getLawnmowerStatus().getError().getNumber() != AppControlsProtos.LawnmowerStatus.Error.NO_ERROR_VALUE) {
                    if(errorIcon.getVisibility() != View.VISIBLE) {
                        errorIcon.setVisibility(View.VISIBLE);
                    }
                    if(errorMsg.getVisibility() != View.VISIBLE) {
                        errorMsg.setVisibility(View.VISIBLE);
                    }
                    if(LawnmowerStatusData.getInstance().getLawnmowerStatus().getError() == AppControlsProtos.LawnmowerStatus.Error.ROBOT_STUCK) {
                        //robot stuck
                        errorMsg.setText("Fehler: Roboter steckt fest!");
                    } else if(LawnmowerStatusData.getInstance().getLawnmowerStatus().getError() == AppControlsProtos.LawnmowerStatus.Error.BLADE_STUCK) {
                        //robotblade stuck
                        errorMsg.setText("Fehler: Klinge steckt fest!");
                    } else if(LawnmowerStatusData.getInstance().getLawnmowerStatus().getError() == AppControlsProtos.LawnmowerStatus.Error.PICKUP) {
                        errorMsg.setText("Roboter wird angehoben");
                        //robot pickup
                    } else if(LawnmowerStatusData.getInstance().getLawnmowerStatus().getError() == AppControlsProtos.LawnmowerStatus.Error.LOST) {
                        errorMsg.setText("Fehler: Orientierung verloren!");
                        //robot lost
                    } else {
                        errorMsg.setText("Ein unerwarteter Fehler ist aufgetreten!");
                        //unrecognized error
                    }
                } else {
                    if(errorIcon.getVisibility() != View.INVISIBLE) {
                        errorIcon.setVisibility(View.INVISIBLE);
                    }
                    if(errorMsg.getVisibility() != View.INVISIBLE) {
                        errorMsg.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });
    }

    @Override
    public void onLSDChange() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setBatteryState(LawnmowerStatusData.getInstance().getLawnmowerStatus().getBatteryState());
                updateLawnmowerStatus(LawnmowerStatusData.getInstance().getLawnmowerStatus().getStatus());
                handleError();
            }
        });
    }

    @Override
    public void onPause() {
        Log.i("Home","onPause");
        super.onPause();
        LSDListenerManager.removeListener(this);
    }

    @Override
    public void onResume() {
        Log.i("Home","onResume");
        super.onResume();
        LSDListenerManager.addListener(this);
    }
}