package com.pedalportland.routetracker;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.location.LocationManager;
import android.os.PowerManager;
import android.util.Log;

/**
 * This class extends the <code>Application<code/> class, and implements it as a singleton.
 * This class is used to maintain global application state.
 * @author robin5 (Robin Murray)
 * @version 1.0
 * @see <code>Application<code/> class.
 * @created 2/2/14
 */
public class MyApplication extends Application {

    private static final String MODULE_TAG = "MyApplication";

    // Reference to class instance
    private static MyApplication myApp = null;

    // Reference to RouteTracker instance
    private RouteTracker routeTracker = null;

    // Reference to DataUploader instance
    private DataUploader dataUploader = null;

    // Returns the application instance
    public static MyApplication getInstance() {
        return myApp;
    }

    /**
     * Called when the application is starting, before any activity, service,
     * or receiver objects (excluding content providers) have been created.
     */
    @Override
    public final void onCreate() {
        super.onCreate();
        try {
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);

            routeTracker = new RouteTracker(locationManager, powerManager);
            dataUploader = new DataUploader(getResources().getString(R.string.default_pedal_portland_uri));
            myApp = this;
        }
        catch(Exception ex) {
            Log.e(MODULE_TAG, ex.getMessage());
        }
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
        assert(routeTracker != null);
        return routeTracker;
    }

    /**
     * Returns a reference to the <>DataUploader</>
     * @see <code>DataUploader<code/> class.
     */
    public DataUploader getDataUploader() {
        assert(dataUploader != null);
        return dataUploader;
    }

    /**
     * Called when the application is terminating, and local
     * resources and objects should be reclaimed.
     */
    public void shutDown() {

        // Reclaim routeTracker resources
        if (null != routeTracker) {
            routeTracker.shutDown();
            routeTracker = null;
        }

        // Reclaim dataUploader resources
        if (null != dataUploader) {
            dataUploader = null;
        }
    }
}
