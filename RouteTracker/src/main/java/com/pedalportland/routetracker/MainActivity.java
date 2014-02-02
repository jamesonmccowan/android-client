package com.pedalportland.routetracker;

import com.pedalportland.routetracker.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.util.Log;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class MainActivity extends Activity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;

    private LocationManager locationManager;                            // gives location data
    private boolean tracking;                                           // whether app is currently tracking
    private PowerManager.WakeLock wakeLock; // used to prevent device sleep
    private boolean gpsFix; // whether we have a GPS fix for accurate data
    private RouteCalculator routeCalculator = new RouteCalculator();
    private static final String MODULE_TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(R.id.fullscreen_content);

        // ------------------------------------------------------------------------------
        // Set up an instance of SystemUiHider to control the system UI for this activity
        // ------------------------------------------------------------------------------

        mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider.setOnVisibilityChangeListener(new View_OnVisibilityChangeListener(controlsView));

        // Set up the user interaction to manually show or hide the system UI.
        contentView.setOnClickListener(new ContentView_ViewOnClickListener());

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        ToggleButton trackingToggleButton = (ToggleButton) findViewById(R.id.trackingToggleButton);

        trackingToggleButton.setOnTouchListener(mDelayHideTouchListener);

        // register listener for trackingToggleButton
        trackingToggleButton.setOnCheckedChangeListener(trackingToggleButtonListener);
    }

    // listener for trackingToggleButton's events
    CompoundButton.OnCheckedChangeListener trackingToggleButtonListener =
            new CompoundButton_MyOnCheckedChangeListener();


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }


    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View_OnTouchListener();

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new SystemUiHider_Runnable();

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    // ***************************************************************
    // * Function: onStart
    // * Description: called when Activity becoming visible to the user
    // ***************************************************************
    @Override
    public void onStart()
    {
        try {
            super.onStart(); // call super's onStart method

            // create Criteria object to specify location provider's settings
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE); // fine location data
            criteria.setBearingRequired(true); // need bearing to rotate map
            criteria.setCostAllowed(true); // OK to incur monetary cost
            criteria.setPowerRequirement(Criteria.POWER_LOW); // try to conserve
            criteria.setAltitudeRequired(false); // don't need altitude data

            // get the LocationManager
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            // register listener to determine whether we have a GPS fix
            locationManager.addGpsStatusListener(gpsStatusListener);

            // get the best provider based on our Criteria
            String provider = locationManager.getBestProvider(criteria, true);

            // listen for changes in location as often as possible
            locationManager.requestLocationUpdates(provider, 0, 0, locationListener);

            // get the app's power manager
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);

            // get a wakelock preventing the device from sleeping
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "No sleep");
            wakeLock.acquire(); // acquire the wake lock
        }
        catch (Exception ex){
            Log.e(MODULE_TAG, ex.getMessage());
        }

    }

    // ********************************************************************
    // * Function: onStop
    // * Description: called when Activity is no longer visible to the user
    // ********************************************************************

    @Override
    public void onStop()
    {
        try {
            super.onStop(); // call the super method
            wakeLock.release(); // release the wakelock
        }
        catch (Exception ex){
            Log.e(MODULE_TAG, ex.getMessage());
        }
    }

    // ********************************************************************
    // * Function: updateLocation
    // * Description: Receives location updates from location provider
    // ********************************************************************

    public void updateLocation(Location location)
    {
        // location not null; have GPS fix
        if (location != null && gpsFix) {
            routeCalculator.AddLocation(location);
        }

    }

    // ********************************************************************
    // * Function: GpsStatus.Listener (anonymous inner class)
    // * Description: determine whether we have GPS fix
    // ********************************************************************

    GpsStatus.Listener gpsStatusListener = new GpsStatus_MyGpsStatusListener();

    // responds to events from the LocationManager
    private final LocationListener locationListener = new LocationManager_LocationListener();

    /**
     * Inner Class: ContentView_ViewOnClickListener
     *
     * Description:  Class used to toggle display of UI elements in view
     */
    private class ContentView_ViewOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if (TOGGLE_ON_CLICK) {
                mSystemUiHider.toggle();
            } else {
                mSystemUiHider.show();
            }
        }
    }

    /**
     * Inner Class: LocationManager_LocationListener
     *
     * Description:  Class handles LocationListener events for LocationManager
     */
    private class LocationManager_LocationListener
            implements LocationListener {

        // when the location is changed
        public void onLocationChanged(Location location)
        {
            gpsFix = true; // if getting Locations, then we have a GPS fix

            if (tracking) // if we're currently tracking
                updateLocation(location); // update the location
        } // end onLocationChanged

        public void onProviderDisabled(String provider)
        {
        } // end onProviderDisabled

        public void onProviderEnabled(String provider)
        {
        } // end onProviderEnabled

        public void onStatusChanged(String provider,
                                    int status, Bundle extras)
        {
        } // end onStatusChanged
    }

    /**
     * Inner Class: GpsStatus_MyGpsStatusListener
     *
     * Description:  Class handles LocationListener events for GpsStatus
     */
    private class GpsStatus_MyGpsStatusListener
            implements GpsStatus.Listener {
        public void onGpsStatusChanged(int event)
        {
            if (event == GpsStatus.GPS_EVENT_FIRST_FIX)
            {
                gpsFix = true;
                Toast results = Toast.makeText(MainActivity.this, getResources().getString(R.string.toast_signal_acquired), Toast.LENGTH_SHORT);

                // center the Toast in the screen
                results.setGravity(Gravity.CENTER, results.getXOffset() / 2, results.getYOffset() / 2);
                results.show(); // display the results
            }
        }
    }

    /**
     * Inner Class: View_OnVisibilityChangeListener
     *
     * Description:  Class handles OnVisibilityChangeListener events for View
     */
    private class View_OnVisibilityChangeListener
            implements SystemUiHider.OnVisibilityChangeListener {
        private final View controlsView;
        // Cached values.
        int mControlsHeight;
        int mShortAnimTime;

        public View_OnVisibilityChangeListener(View controlsView) {
            this.controlsView = controlsView;
        }

        @Override
        @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
        public void onVisibilityChange(boolean visible) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                // If the ViewPropertyAnimator API is available
                // (Honeycomb MR2 and later), use it to animate the
                // in-layout UI controls at the bottom of the
                // screen.
                if (mControlsHeight == 0) {
                    mControlsHeight = controlsView.getHeight();
                }
                if (mShortAnimTime == 0) {
                    mShortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
                }
                controlsView.animate().translationY(visible ? 0 : mControlsHeight).setDuration(mShortAnimTime);
            } else {
                // If the ViewPropertyAnimator APIs aren't
                // available, simply show or hide the in-layout UI
                // controls.
                controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
            }

            if (visible && AUTO_HIDE) {
                // Schedule a hide().
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
        }
    }

    /**
     * Inner Class: View_OnVisibilityChangeListener
     *
     * Description:  Class handles OnTouchListener events for View
     */
    private class View_OnTouchListener
            implements View.OnTouchListener {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    }

    /**
     * Inner Class: CompoundButton_MyOnCheckedChangeListener
     *
     * Description:  Class handles OnCheckedChangeListener events for CompoundButton
     */
    private class CompoundButton_MyOnCheckedChangeListener
            implements CompoundButton.OnCheckedChangeListener {
        // called when user toggles tracking state

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
        {
            // if app is currently tracking
            if (!isChecked)
            {
                tracking = false; // just stopped tracking locations
                routeCalculator.stop();

                // create a dialog displaying the results
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
                dialogBuilder.setTitle(R.string.results);

                // display distanceTraveled traveled and average speed
                dialogBuilder.setMessage(String.format(getResources().getString(R.string.results_format),
                        routeCalculator.getDistanceKM(),
                        routeCalculator.getDistanceMI(),
                        routeCalculator.getSpeedKM(),
                        routeCalculator.getSpeedMI()));
                dialogBuilder.setPositiveButton(R.string.button_ok, null);
                dialogBuilder.show(); // display the dialog

                // Todo: long operations like this should be put on a different thread.
                try {
                    // upload route
                    /*String upload = dataUploader.UploadData(routeCalculator.getRoute());
                    // create a dialog displaying if the message reached the server
                    dialogBuilder = new AlertDialog.Builder(MainActivity.this);
                    dialogBuilder.setTitle("Sending Route Information");

                    // display distanceTraveled traveled and average speed
                    dialogBuilder.setMessage(upload);
                    dialogBuilder.setPositiveButton(R.string.button_ok, null);
                    dialogBuilder.show(); // display the dialog*/
                    DataUploader dataUploader = new DataUploader(getResources().getString(R.string.default_pedal_portland_url));
                    dataUploader.UploadData(routeCalculator.getRoute());
                }
                catch(Exception ex) {

                }


            } // end if
            else
            {
                tracking = true; // app is now tracking
                //startTime = System.currentTimeMillis(); // get current time
                routeCalculator.start();
            } // end else
        } // end method onCheckChanged
    }

    /**
     * Inner Class: SystemUiHider_Runnable
     *
     * Description:  Class implements a runnable for hiding he UI
     */
    private class SystemUiHider_Runnable implements Runnable {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    }
}
