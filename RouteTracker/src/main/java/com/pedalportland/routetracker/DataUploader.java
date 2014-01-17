/**
 * Class: DataUploader
 *
 * Description: This class Uploads route data to portland observatory
 */

package com.pedalportland.routetracker;

import java.util.List;
import android.location.Location;

/**
 * Class: DataUploader
 *
 * Description: This class Uploads route data to portland observatory
 */

public class DataUploader {

    /**
     * Function: UploadData
     *
     * Description: Class constructor
     */
    public DataUploader(String url) {
    }

    /**
     * Function: UploadData
     *
     * Description: Uploads route data to portland observatory.  If not
     *     connected to internet, route data is stored for later
     *     upload.
     */
    public void UploadData(List route) {
        try {
            // upload data to web site here
        }
        catch(Exception ex) {

        }
    }
}
