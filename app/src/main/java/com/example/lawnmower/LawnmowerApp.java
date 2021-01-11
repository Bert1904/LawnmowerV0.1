package com.example.lawnmower;

import android.app.Application;

public class LawnmowerApp extends Application {

    private static boolean visible = false;

    public static void onPauseActivity() {
       visible = false;
        System.out.println("LawnmowerApp visible: " + visible);
    }

    public static void onResumeActivity() {
        visible = true;
        System.out.println("LawnmowerApp visible: " + visible);
    }

    public static boolean isVisibile() {
        return visible;
    }
}
