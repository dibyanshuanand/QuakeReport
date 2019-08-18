package com.example.android.quakereport;

import android.content.Context;

public class Quake {

    private double mMagnitude;
    private String mLocation;
    private String mDate;
    private String mTime;
    private String mUrl;

    /** Constructor for class Quake
     *
     * @param magnitude Magnitude of earthquake
     * @param location Location of earthquake
     * @param date Date of occurence of earthquake
     */
    public Quake(double magnitude, String location, String date, String time, String url) {
        mMagnitude = magnitude;
        mLocation = location;
        mDate = date;
        mTime = time;
        mUrl = url;
    }

    public double getMagnitude(Context context) {
        return mMagnitude;
    }

    public String getLocation(Context context) {
        return mLocation;
    }

    public String getDate(Context context) {
        return mDate;
    }

    public String getTime(Context context) {
        return mTime;
    }

    public String getUrl(Context context) {
        return mUrl;
    }
}
