package com.example.lawnmower;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

public class WeatherActivity extends BaseAppCompatAcitivty {

    //API Key https://openweathermap.org
    String Key = "d0176161d0931b842c90de6b5678406a";

    Geocoder geocoder;
    private final double latitute = 51.574534546129726d;
    private final double longitude = 7.026503346726579d;
    private List<Address> addresses;
    String CityName;
    String lawnmower = "Lawnmower Position";
    TextView txtCityLocation, txtTime, txtValueTemp, txtValueFeelLike, txtValueHumidity, txtValueClouds;

    String nameIcon = "10d";

    String description="";


    Button btnLoading;

    ImageView imgIcon;

    RelativeLayout relativeLayoutMain;
    RelativeLayout relativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        geocoder= new Geocoder(this,Locale.getDefault());
        try {
          addresses=geocoder.getFromLocation(latitute,longitude,1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        CityName =  addresses.get(0).getLocality();


        txtCityLocation = findViewById(R.id.txtCityLocation);
        txtCityLocation.setText(lawnmower+ CityName);
        txtTime = findViewById(R.id.txtTime);

        txtValueTemp = findViewById(R.id.txtTempValue);

        txtValueFeelLike = findViewById(R.id.txtValueFeelLike);

        txtValueHumidity = findViewById(R.id.txtValueHumidity);

        txtValueClouds = findViewById(R.id.txtValueVision);

        imgIcon = findViewById(R.id.imgIcon);

        btnLoading = findViewById(R.id.btnLoading);

        relativeLayout = findViewById(R.id.rlWeather);

        relativeLayoutMain = findViewById(R.id.rlMain_Ac);
    }

    public class DownloadImage extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... strings) {
            Bitmap bitmap = null;

            URL url;

            HttpURLConnection httpURLConnection;

            InputStream inputStream;

            try {
                Log.i("LINK", strings[0]);
                url = new URL(strings[0]);

                httpURLConnection = (HttpURLConnection) url.openConnection();

                inputStream = httpURLConnection.getInputStream();

                bitmap = BitmapFactory.decodeStream(inputStream);


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return bitmap;
        }
    }

    public class DownloadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {

            String result = "";

            URL url;

            HttpURLConnection httpURLConnection;

            InputStream inputStream;

            InputStreamReader inputStreamReader;

            try {

                url = new URL(strings[0]);

                httpURLConnection = (HttpURLConnection) url.openConnection();

                inputStream = httpURLConnection.getInputStream();

                inputStreamReader = new InputStreamReader(inputStream);

                int data = inputStreamReader.read();

                while (data != -1) {

                    result += (char) data;

                    data = inputStreamReader.read();

                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return result;
        }
    }

    public void loading(View view) {


        txtCityLocation.setVisibility(View.VISIBLE);
        btnLoading.setVisibility(View.INVISIBLE);
        relativeLayout.setVisibility(View.VISIBLE);
        relativeLayoutMain.setBackgroundColor(Color.parseColor("#E6E6E6"));



        String url = "https://api.openweathermap.org/data/2.5/weather?q=" + CityName + "&units=metric&appid=" + Key+"&lang=de";

        DownloadTask downloadTask = new DownloadTask();

        try {

            String result = "abc";


            result = downloadTask.execute(url).get();

            Log.i("Result:", result);

            JSONObject jsonObject = new JSONObject(result);

            JSONObject main = jsonObject.getJSONObject("main");
            String feels_like = main.getString("feels_like");
            String humidity = main.getString("humidity");
            String temp = main.getString("temp");


            String visibility = jsonObject.getString("visibility");

            nameIcon = jsonObject.getJSONArray("weather").getJSONObject(0).getString("icon");
            description=jsonObject.getJSONArray("weather").getJSONObject(0).getString("description");
            Log.i("Name Icon", nameIcon);

            Long time = jsonObject.getLong("dt");

           TimeZone timeZone = TimeZone.getTimeZone("CET");
            //String sTime = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss", Locale.GERMAN).format(new Date(time * 1000));
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");

            // Initial timezone set in central european time
            sdf.setTimeZone(TimeZone.getTimeZone("CET"));


            //Set  timezone
            sdf.setTimeZone(TimeZone.getDefault());
            String current_time = sdf.format(new Date());


            txtValueTemp.setText(temp+"°");
            txtTime.setText(current_time);
            txtValueFeelLike.setText(feels_like + "°");
            txtCityLocation.setText(visibility);
            txtCityLocation.setText(lawnmower+CityName);
            txtValueClouds.setText(description);
            txtValueHumidity.setText(humidity+"%");


            DownloadImage downloadImage = new DownloadImage();

            String urlIcon = " https://openweathermap.org/img/wn/" + nameIcon + "@2x.png";

            Bitmap bitmap = downloadImage.execute(urlIcon).get();

            imgIcon.setImageBitmap(bitmap);

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


}