package com.example.lawnmower;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import org.w3c.dom.Text;
import android.widget.TextView;

import java.net.Socket;


public class HomeActivity extends BaseAppCompatAcitivty {
    private ImageButton buttonMow;
    private ImageButton buttonInfo;
    private ImageButton buttonControl;
    private ImageButton buttonSettings;
    private ImageButton buttonMap;
    private ImageButton buttonWeather;
    private Socket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        TextView statusView = findViewById(R.id.statusView);
        socket = SocketService.getSocket();
        buttonSettings=   findViewById(R.id.buttonSettings);
        buttonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSetting();
            }
        });
        statusView.setAlpha(0.0f);
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
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
}
}