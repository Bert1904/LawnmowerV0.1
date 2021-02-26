package com.example.lawnmower;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import org.w3c.dom.Text;

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
        batteryStatusIcon = findViewById(R.id.batteryStatusIcon);
        batteryStatus = findViewById(R.id.batteryStatus);
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
        //Intent intent = new Intent(this, MapsActivity.class);
        //startActivity(intent);
        new testThread().start();
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

    private class testThread extends Thread {
        @Override
        public void run() {
            for(int i = 0; i < 5; i++) {
                AppControlsProtos.LawnmowerStatus lawnmowerStatus = AppControlsProtos.LawnmowerStatus.newBuilder()
                        .setBatteryState(100-(i*20))
                        .setMowingProgress(i*20)
                        .setStatus(AppControlsProtos.LawnmowerStatus.Status.Ready)
                        .setFinishedMowing(false)
                        .setError(AppControlsProtos.LawnmowerStatus.Error.NO_ERROR)
                        .build();
                LawnmowerStatusData.getInstance().setLawnmowerStatus(lawnmowerStatus);
                try {
                    Thread.sleep(2000);
                } catch(InterruptedException e) {
                    Log.i("testThread","sleeping not successful");
                }
            }
        }
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