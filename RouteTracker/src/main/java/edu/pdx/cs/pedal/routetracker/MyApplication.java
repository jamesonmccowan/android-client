package edu.pdx.cs.pedal.routetracker;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.LocationManager;
import android.os.PowerManager;

import java.io.File;

/**
 * This class extends the <code>Application<code/> class, and implements it as a singleton.
 * This class is used to maintain global application state.
 * @author robin5 (Robin Murray) and Dan Catalano
 * @version 1.0
 * @see <code>Application<code/> class.
 * created 2/2/14
 */
public class MyApplication extends Application {

    private static final String DATA_ROOT_DIR = "/data";
    private static final String UPLOAD_ROOT_DIR = "/upload";

    // Reference to class instance
    private static MyApplication myApp = null;

    // Reference to RouteTracker instance
    private RouteTracker routeTracker = null;

    // Reference to DataLayer instance
    private DataLayer dataLayer = null;

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

        LocationManager locationManager;
        PowerManager powerManager;
        NotificationManager notificationManager;
        Notification.Builder builder;

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
            builder.setSmallIcon(R.drawable.pedalpdx)
                    .setContentTitle("PedalPDX")
                    .setContentText("Actively collecting location data")
                    .setWhen(System.currentTimeMillis());

            Intent resultIntent = new Intent(MyApplication.getInstance(), MainActivity.class);
            resultIntent.setAction("android.intent.action.MAIN");
            PendingIntent resultPendingIntent = PendingIntent.getActivity(
                    MyApplication.getInstance(),
                    MainActivity.NOTIFICATION_CODE,
                    resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );

            builder.setContentIntent(resultPendingIntent);
            builder.setOngoing(true);

        } catch (Exception ex) {
            setInitErrorMessage(ex.getMessage());
            return;
        }

        // Create a RouteTracker object and maintain a reference to it
        try {
            routeTracker = new RouteTracker(locationManager, powerManager, notificationManager, builder);
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
            File dir;
            String dataRootDir;
            if (null == (dir = getFilesDir())) {
                throw new NullPointerException();
            }
            dataRootDir = dir.getAbsolutePath() + DATA_ROOT_DIR;
            dataLayer = new DataLayer(dataRootDir);
        }
        catch(NullPointerException ex) {
            setInitErrorMessage(getResources().getString(R.string.dl_null_pointer));
        }
        catch(DataLayerException ex) {
            setInitErrorMessage(getResources().getString(R.string.dl_error_directories));
        }
        catch(RideInfoMgrException ex) {
            setInitErrorMessage(getResources().getString(R.string.rim_error_directories));
        }

        String uploadRootDir = null;
        // Connect DataLayer and rideUploader service
        try {
            uploadRootDir = getFilesDir().getAbsolutePath() + UPLOAD_ROOT_DIR;
            String ridesDirName = dataLayer.getRidesDirName();
            Intent intent = new Intent(this, UploadService.class);
            intent.putExtra(UploadService.EXTRA_RIDES_DIR_NAME, ridesDirName);
            intent.putExtra(UploadService.EXTRA_UPLOAD_ROOT_DIR_NAME, uploadRootDir);
            intent.putExtra(UploadService.EXTRA_UPLOAD_URL,
                    getResources().getString(R.string.pedalpdx_url));
            bindService(intent, dataLayer.uploaderServiceConnection, Context.BIND_AUTO_CREATE);
        }
        catch(NullPointerException ex) {
            setInitErrorMessage(getResources().getString(R.string.du_null_pointer));
        }
        catch(SecurityException ex) {
            setInitErrorMessage(getResources().getString(R.string.dl_security_exception));
        }

        // Some final initialization has to happen within the DataLayer
        dataLayer.Init(uploadRootDir);
        //dataLayer.deleteAll();
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
     * Returns a reference to the <>DataLayer</>
     * @see <code>DataLayer<code/> class.
     */
    public DataLayer getDataLayer() {
        return dataLayer;
    }

}