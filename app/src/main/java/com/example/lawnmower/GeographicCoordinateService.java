package com.example.lawnmower;

import android.util.Log;

public class GeographicCoordinateService {

    public double latitude;
    public double longitude;

    private static GeographicCoordinateService  geographicCoordinateService= new GeographicCoordinateService ();

    public void setLatitude(double latitude) {
        this.latitude = latitude;
        Log.i("GeographicCoor startet", "setLatitude " + this.latitude );
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
        Log.i("GeographicCoor startet", "setlongitude " + this.longitude);
    }

    public double getlatitude() {
        return latitude;
    }

    public double getlongitude() {
        return longitude;
    }
    public static GeographicCoordinateService getGeographicCoordinateService(){return geographicCoordinateService;}
}