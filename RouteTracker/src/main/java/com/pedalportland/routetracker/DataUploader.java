package com.pedalportland.routetracker;

import android.location.Location;

/**
 * Created by Robin on 1/10/14.
 */
public class DataUploader {

    private String _url;

    public DataUploader(String url) {
        _url = url;
    }

    public void UploadData(Object[] locations){
        try {

            Location[] points = (Location[]) locations;
            // upload data to web site here
        }
        catch(Exception ex) {

        }
    }
}
