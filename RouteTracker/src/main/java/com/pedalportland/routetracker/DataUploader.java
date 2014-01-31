package com.pedalportland.routetracker;

import android.location.Location;
import java.util.List;

/**
 * This class collects uploads route data to the portland observatory
 * @author Robin Murray
 * @version 1.0
 */
public class DataUploader {

    final private String url;

    /**
     * Instantiates an instance of a <code>DataUploader</code>
     */
    public DataUploader(String pdxObservatory) {
        url = pdxObservatory;
    }

    /**
     * Uploads the data to the observatory
     * @param locations The route data.
     */
    public void UploadData(List<Location> locations) {
        try {
            // upload data to web site here
        }
        catch(Exception ex) {

        }
    }
}
