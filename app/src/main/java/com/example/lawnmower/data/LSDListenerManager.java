package com.example.lawnmower.data;

import com.example.lawnmower.data.LawnmowerStatusDataChangedListener;

import java.util.ArrayList;
import java.util.List;

public class LSDListenerManager {

    private static List<LawnmowerStatusDataChangedListener> listeners = new ArrayList<LawnmowerStatusDataChangedListener>();

    public static void addListener(final LawnmowerStatusDataChangedListener listener) {
        if (null != listener) {
            listeners.add(listener);
        }
    }

    public static void removeListener(final LawnmowerStatusDataChangedListener listener) {
        if (null != listener) {
            listeners.remove(listener);
        }
    }

    public static void notifyOnChange() {
        for(LawnmowerStatusDataChangedListener listener : listeners) {
            listener.onLSDChange();
        }
    }

    private LSDListenerManager() {
    }
}
