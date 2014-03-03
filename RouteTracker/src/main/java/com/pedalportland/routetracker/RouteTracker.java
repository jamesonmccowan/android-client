package com.pedalportland.routetracker;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.os.Bundle;
import android.location.Criteria;
import android.location.LocationManager;
import android.os.PowerManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.util.Log;

/**
 * This class implements route tracking.
 * @author robin5 (Robin Murray)
 * @version 1.0
 * created 2/3/14
 */
public class RouteTracker {

    private static final String MODULE_TAG = "RouteTracker";

    private boolean isTracking = false;            // whether app is currently isTracking
    private LocationManager locationManager;        // gives location data
    private PowerManager powerManager = null;
    private PowerManager.WakeLock wakeLock = null;  // used to prevent device sleep
    private boolean gpsFix;                         // whether we have a GPS fix for accurate data
    private RouteCalculator route = new RouteCalculator();
    private String provider = null;
    private NotificationManager notificationManager = null;
    private Notification.Builder builder = null;

    /**
     * <code>RouteTracker<code/> constructor. This procedure initializes the class instance,
     * and copies references to the LocationManager and PowerManager instances instantiated
     * by the main application.
     * @throws NullPointerException
     */
    public RouteTracker(LocationManager locationManager, PowerManager powerManager,
                        NotificationManager notificationManager) {

        if (null == locationManager) {
            throw new NullPointerException();
        }

        if (null == powerManager) {
            throw new NullPointerException();
        }

        if (null == notificationManager){
            throw new NullPointerException();
        }

        // Copy reference to LocationManager
        this.locationManager = locationManager;

        // Copy reference to PowerManager
        this.powerManager = powerManager;

        this.notificationManager = notificationManager;

        this.builder = builder;
    }

    /**
     * <code>RouteTracker<code/> constructor. This procedure initializes the class instance,
     * and copies references to the LocationManager and PowerManager instances instantiated
     * by the main application.
     * @throws RouteTrackerExceptionMemory
     * @throws RouteTrackerExceptionBadArgAccuracy
     * @throws RouteTrackerExceptionBadArgBearing
     * @throws RouteTrackerExceptionSecurity
     * @throws RouteTrackerExceptionNoProvider
     * @throws RouteTrackerExceptionWakeLock
     */
    public void Init() {

        Criteria criteria;

        // create Criteria object to specify location provider's settings
        if (null == (criteria = new Criteria())) {
            throw new RouteTrackerExceptionMemory();
        }

        // fine location data (throws java.lang.IllegalArgumentException)
        try {
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
        }
        catch(IllegalArgumentException ex) {
            throw new RouteTrackerExceptionBadArgAccuracy(ex);
        }

        // need bearing to rotate map (throws java.lang.IllegalArgumentException)
        try {
            criteria.setBearingRequired(true);
        }
        catch(IllegalArgumentException ex) {
            throw new RouteTrackerExceptionBadArgBearing(ex);
        }

        criteria.setCostAllowed(true); // OK to incur monetary cost
        criteria.setPowerRequirement(Criteria.POWER_LOW); // try to conserve
        criteria.setAltitudeRequired(false); // don't need altitude data

        // register listener to determine whether we have a GPS fix (throws java.lang.SecurityException)
        try {
            locationManager.addGpsStatusListener(gpsStatusListener);
        }
        catch(SecurityException ex) {
            throw new RouteTrackerExceptionSecurity(ex);
        }

        // get the best provider based on our Criteria
        provider = locationManager.getBestProvider(criteria, true);
        if (null == provider) {
            throw new RouteTrackerExceptionNoProvider();
        }

        // get a wakelock preventing the device from sleeping
        try {
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "No sleep");
        }
        catch(Exception ex) {
            throw new RouteTrackerExceptionWakeLock(ex);
        }

        try {
            builder = new Notification.Builder(MyApplication.getInstance());
            builder.setSmallIcon(R.drawable.pedal_portland)
                    .setContentTitle("PedalPDX")
                    .setContentText("Application actively collecting location data")
                    .setWhen(System.currentTimeMillis());

//            Intent resultIntent = new Intent(MyApplication.getInstance(), MainActivity.class);
//            TaskStackBuilder stackBuilder = TaskStackBuilder.create(MyApplication.getInstance());
//            // Adds the back stack for the Intent (but not the Intent itself)
//            stackBuilder.addParentStack(MainActivity.class);
//            // Adds the Intent that starts the Activity to the top of the stack
//            stackBuilder.addNextIntent(resultIntent);
//            PendingIntent resultPendingIntent =
//                    stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
//            builder.setContentIntent(resultPendingIntent);
//            builder.setOngoing(true);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * This method initiates route tracking
     * @throws RouteTrackerException
     */
    public void startTracking() {

        // acquire the wake lock preventing device from sleeping
        wakeLock.acquire();

        // Reset route information
        route.start();
        notificationManager.notify(0, builder.build());

        try {
            // Listen for changes in location as often as possible (throws java.lang.IllegalArgumentException)
            locationManager.requestLocationUpdates(provider, 0, 0, locationListener);

            // Set flag for tracking in progress
            isTracking = true;
        }
        catch(IllegalArgumentException ex) {
            throw new RouteTrackerException(ex);
        }
    }

    /**
     * This method terminates route tracking
     * @throws java.lang.IllegalArgumentException
     */
    public void stopTracking() {

        // Set flag denoting we are no longer tracking route
        isTracking = false;

        try {
            // Remove listener (throws java.lang.IllegalArgumentException)
            locationManager.removeUpdates(locationListener);
        }
        catch(IllegalArgumentException ex) {
            throw new RouteTrackerException(ex);
        }
        finally {
            // Release the wakelock
            wakeLock.release();
            notificationManager.cancel(0);

            // Terminate collection of route information.  Note that this causes
            // calculations to occur in RouteCalculator class
            route.stop();
        }
    }

    /**
     * Flag signifying route tracking is in progress
     */
    public boolean isTracking() {
        return this.isTracking;
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

            if (isTracking) // if we're currently isTracking
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
            if (isTracking)
                stopTracking();
        }
        catch(Exception ex) {
            Log.e(MODULE_TAG, ex.getMessage());
        }
    }
}

