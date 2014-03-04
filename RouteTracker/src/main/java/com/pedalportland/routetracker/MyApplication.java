package com.pedalportland.routetracker;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.LocationManager;
import android.os.PowerManager;

/**
 * This class extends the <code>Application<code/> class, and implements it as a singleton.
 * This class is used to maintain global application state.
 * @author robin5 (Robin Murray) and Dan Catalano
 * @version 1.0
 * @see <code>Application<code/> class.
 * created 2/2/14
 */
public class MyApplication extends Application {

    private static final String MODULE_TAG = "MyApplication";

    // Reference to class instance
    private static MyApplication myApp = null;

    // Reference to RouteTracker instance
    private RouteTracker routeTracker = null;

    // Reference to DataUploader instance
    private DataUploader dataUploader = null;

    private String initErrorMessage = null;

    // Returns the application instance
    public static MyApplication getInstance() {
        return myApp;
    }

    /**
     * Called when the application is starting, before any activity, service,
     * or receiver objects (excluding content providers) have been created.
     * @throws java.lang.OutOfMemoryError
     */
    @Override
    public final void onCreate() {
        super.onCreate();

        LocationManager locationManager = null;
        PowerManager powerManager = null;
        NotificationManager notificationManager = null;
        Notification.Builder builder = null;

        myApp = this;

        // Obtain a reference to the LocationManager service
        try {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (null == locationManager) {
                setInitErrorMessage(getResources().getString(R.string.ex_no_location_mgr));
                return;
            }
        }
        catch(Exception ex) {
            setInitErrorMessage(getResources().getString(R.string.ex_no_location_mgr));
            return;
        }

        // Obtain a reference to the PowerManager service
        try {
            powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (null == powerManager) {
                setInitErrorMessage(getResources().getString(R.string.ex_no_power_mgr));
                return;
            }
        }
        catch(Exception ex) {
            setInitErrorMessage(getResources().getString(R.string.ex_no_power_mgr));
            return;
        }

        // Create a NotificationManager & a builder object then build the builder
        try {
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            builder = new Notification.Builder(MyApplication.getInstance());
            builder.setSmallIcon(R.drawable.pedal_portland)
                    .setContentTitle("PedalPDX")
                    .setContentText("Actively collecting location data")
                    .setWhen(System.currentTimeMillis());

            Intent resultIntent = new Intent(MyApplication.getInstance(), MainActivity.class);
            resultIntent.setAction("android.intent.action.MAIN");
            PendingIntent resultPendingIntent = PendingIntent.getActivity(MyApplication.getInstance(),
                    MainActivity.NOTIFICATION_CODE,
                    resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            builder.setContentIntent(resultPendingIntent);
            builder.setOngoing(true);

        } catch (Exception ex) {
            setInitErrorMessage(ex.getMessage());
            return;
        }

        // Create a RouteTracker object and maintain a reference to it
        try {
            routeTracker = new RouteTracker(locationManager, powerManager, notificationManager, builder);
            if (null == routeTracker) {
                setInitErrorMessage(getResources().getString(R.string.ex_error_out_of_memory));
                return;
            }
        }
        catch(Exception ex) {
            setInitErrorMessage(ex.getMessage());
            return;
        }

        // Initialize the RouteTracker
        try {
            routeTracker.Init();
        }
        catch(RouteTrackerExceptionMemory ex) {
            setInitErrorMessage(getResources().getString(R.string.rt_error_out_of_mem));
            routeTracker = null;
            return;
        }
        catch(RouteTrackerExceptionBadArgAccuracy ex) {
            setInitErrorMessage(getResources().getString(R.string.rt_error_bad_arg_accuracy));
            routeTracker = null;
            return;
        }
        catch(RouteTrackerExceptionBadArgBearing ex) {
            setInitErrorMessage(getResources().getString(R.string.rt_error_bad_arg_bearing));
            routeTracker = null;
            return;
        }
        catch(RouteTrackerExceptionSecurity ex) {
            setInitErrorMessage(getResources().getString(R.string.rt_error_security));
            routeTracker = null;
            return;
        }
        catch(RouteTrackerExceptionNoProvider ex) {
            setInitErrorMessage(getResources().getString(R.string.rt_error_no_provider));
            routeTracker = null;
            return;
        }
        catch(RouteTrackerExceptionWakeLock ex) {
            setInitErrorMessage(getResources().getString(R.string.rt_error_wakelock));
            routeTracker = null;
            return;
        }

        // create a DataUploader object and maintain a reference to it
        try {
            dataUploader = new DataUploader(
                getResources().getString(R.string.default_pedal_portland_url));
        }
        catch(NullPointerException ex) {
            setInitErrorMessage(getResources().getString(R.string.du_null_pointer));
        }
    }

    /**
     * Sets the modules exception message.
     */
    private void setInitErrorMessage(String message) {
        initErrorMessage = message;
    }

    /**
     * Returns the modules exception message.
     */
    public String getInitErrorMessage() {
        return initErrorMessage;
    }

    /**
     * This is called when the overall system is running low on memory, and
     * actively running processes should trim their memory usage.
     */
    @Override
    public final void onLowMemory() {
        super.onLowMemory();
    }

    /**
     * Called when the operating system has determined that it is a good
     * time for a process to trim unneeded memory from its process.
     */
    @Override
    public final void onTrimMemory(int level) {
        super.onTrimMemory(level);
    }

    /**
     * Called by the system when the device configuration changes while
     * the component is running.
     */
    @Override
    public final void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /**
     * Returns a reference to the <>RouteTracker</>
     * @see <code>RouteTracker<code/> class.
     */
    public RouteTracker getRouteTracker() {
        return routeTracker;
    }

    /**
     * Returns a reference to the <>DataUploader</>
     * @see <code>DataUploader<code/> class.
     */
    public DataUploader getDataUploader() {
        return dataUploader;
    }

}
