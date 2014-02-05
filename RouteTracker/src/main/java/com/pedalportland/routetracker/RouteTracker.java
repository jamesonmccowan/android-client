package com.pedalportland.routetracker;

import android.os.Bundle;
import android.location.Criteria;
import android.location.LocationManager;
import android.os.Handler;
import android.os.PowerManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.util.Log;

/**
 * This class implements route tracking.
 * @author robin5 (Robin Murray)
 * @version 1.0
 * @created 2/3/14
 */
public class RouteTracker {

    private static final String MODULE_TAG = "RouteTracker";

    private boolean mIsTracking = false;            // whether app is currently mIsTracking
    private LocationManager locationManager;        // gives location data
    private PowerManager powerManager = null;
    private PowerManager.WakeLock wakeLock = null;  // used to prevent device sleep
    private boolean gpsFix;                         // whether we have a GPS fix for accurate data
    private RouteCalculator route = new RouteCalculator();
    private String provider = null;

    /**
     * <code>RouteTracker<code/> constructor. This procedure initializes the class instance,
     * and copies references to the LocationManager and PowerManager instances instantiated
     * by the main application.
     */
    public RouteTracker(LocationManager locationManagerIn, PowerManager powerManagerIn) {

        try {

            // Copy reference to LocationManager
            locationManager = locationManagerIn;

            // Copy reference to PowerManager
            powerManager = powerManagerIn;

            // create Criteria object to specify location provider's settings
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE); // fine location data
            criteria.setBearingRequired(true); // need bearing to rotate map
            criteria.setCostAllowed(true); // OK to incur monetary cost
            criteria.setPowerRequirement(Criteria.POWER_LOW); // try to conserve
            criteria.setAltitudeRequired(false); // don't need altitude data

            // register listener to determine whether we have a GPS fix
            locationManager.addGpsStatusListener(gpsStatusListener);

            // get the best provider based on our Criteria
            provider = locationManager.getBestProvider(criteria, true);

            // get a wakelock preventing the device from sleeping
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "No sleep");
        }
        catch(Exception ex) {
            Log.e(MODULE_TAG, ex.getMessage());
        }
    }

    /**
     * This method initiates route tracking
     */
    public void startTracking() {

        // acquire the wake lock preventing device from sleeping
        wakeLock.acquire();

        // Reset route information
        route.start();

        // Listen for changes in location as often as possible
        locationManager.requestLocationUpdates(provider, 0, 0, locationListener);

        // Set flag for tracking in progress
        mIsTracking = true;
    }

    /**
     * This method terminates route tracking
     */
    public void stopTracking() {

        // Remove listener
        locationManager.removeUpdates(locationListener);

        // Release the wakelock
        wakeLock.release();

        // Set flag denoting we are no longer tracking route
        mIsTracking = false;

        // Terminate collection of route information.  Note thatthis also cause some
        // calculations to occur in RouteCalculator class
        route.stop();
    }

    /**
     * Flag signifying route tracking is in progress
     */
    private boolean isTracking() {
        return mIsTracking;
    }

    /**
     * This method returns the route
     */
    public RouteCalculator getRoute(){
        return route;
    }

    /**
     * Reference to an object for recording GPS status changes
     */
    private GpsStatus.Listener gpsStatusListener = new GpsStatus_MyGpsStatusListener();

    /**
     * Reference to an object for recording locations returned by LocationManager
     */
    private final LocationListener locationListener = new LocationManager_LocationListener();

    /**
     * This class handles LocationListener events for LocationManager
     */
    private class LocationManager_LocationListener implements LocationListener {

        // when the location is changed
        public void onLocationChanged(Location location) {

            gpsFix = true; // if getting Locations, then we have a GPS fix

            if (mIsTracking) // if we're currently mIsTracking
                updateLocation(location); // update the location
        } // end onLocationChanged

        public void onProviderDisabled(String provider) {
        } // end onProviderDisabled

        public void onProviderEnabled(String provider) {
        } // end onProviderEnabled

        public void onStatusChanged(String provider, int status, Bundle extras) {
        } // end onStatusChanged
    }

    /**
     * This class handles LocationListener events for GpsStatus
     */
    private class GpsStatus_MyGpsStatusListener implements GpsStatus.Listener {

        public void onGpsStatusChanged(int event) {

            if (event == GpsStatus.GPS_EVENT_FIRST_FIX) {
                gpsFix = true;
            }
        }
    }

    /**
     * This method returns GPS Fix Status
     */
    public boolean getGpsFix() {
        return gpsFix;
    }

    /**
     * This method adds locations to the route
     */
    private void updateLocation(Location location) {
        // location not null; have GPS fix
        if (location != null && gpsFix) {
            route.AddLocation(location);
        }
    }

    /**
     * This method releases resources used by the <code>RouteTracker<code/>
     */
    public void shutDown() {

        try {
            if (null != wakeLock)
                if (wakeLock.isHeld())
                    wakeLock.release();

            if (null != locationManager) {

                if (null != locationListener) {
                    locationManager.removeUpdates(locationListener);
                }

                if (null != gpsStatusListener) {
                    locationManager.removeGpsStatusListener(gpsStatusListener);
                }
            }
        }
        catch(Exception ex) {
            Log.e(MODULE_TAG, ex.getMessage());
        }
    }
}

