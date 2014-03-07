package com.pedalportland.routetracker;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.pedalportland.routetracker.util.SystemUiHider;

/**
 * This class extends the <code>Activity<code/> class, and implements
 * a full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @author robin5 (Robin Murray)
 * @version 1.0
 * @see <code>Activity<code/> class.
 * @see SystemUiHider
 * created 1/3/14
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

    private static final String MODULE_TAG = "MainActivity";

    private MyApplication myApp = null;
    private RouteTracker routeTracker = null;
    private DataLayer dataLayer = null;
    private Chronometer mChronometer;

    /**
     * Called when the activity is first created.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        setContentView(R.layout.activity_main);

        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(R.id.fullscreen_content);

        // Set up an instance of SystemUiHider to control the system UI for this activity
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

        if (null != (myApp = MyApplication.getInstance())) {

            // Initialize reference to RouteTracker
            routeTracker = myApp.getRouteTracker();
            if (null != routeTracker) {
                // Set button to match tracking state
                trackingToggleButton.setChecked(routeTracker.isTracking());
            }

            // Initialize reference to DataLayer
            dataLayer = myApp.getDataLayer();
        }

        // register listener for trackingToggleButton
        trackingToggleButton.setOnCheckedChangeListener(trackingToggleButtonListener);

        Button button;


        mChronometer = (Chronometer) findViewById(R.id.chronometer);
        mChronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            public void onChronometerTick(Chronometer times) {
                long timeElapsed = SystemClock.elapsedRealtime() - times.getBase();
                //long to=(1000*60*60*6)-t;

                int hours = (int) (timeElapsed / 3600000);
                int minutes = (int) (timeElapsed - hours * 3600000) / 60000;
                int seconds = (int) (timeElapsed - hours * 3600000 - minutes * 60000) / 1000;
               // int millis = (int) timeElapsed % 9;

                times.setText("TIME "+hours+":"+minutes+":"+seconds);

            }
        });

        // Watch for button clicks.

        button = (Button) findViewById(R.id.trackingToggleButton);

         // button.setOnClickListener(mStartListener);

        //button = (Button) findViewById(R.id.stop);
        // button.setOnClickListener(mStopListener);

        // button = (Button) findViewById(R.id.reset);
        // button.setOnClickListener(mResetListener);

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

    /**
     * Called when the <code>activity<code/> is becoming visible to the user.
     * Followed by <code>onResume()<code/> if the activity comes to the foreground,
     * or <code>onStop(<code/>) if it becomes hidden.
     * @see <code>onResume<code/> class.
     * @see <code>onStop<code/> class.
     */
    @Override
    public void onStart() {
        try {
            super.onStart(); // call super's onStart method

            if (null != dataLayer)
                dataLayer.startRideUpload();
        }
        catch (Exception ex){
            Log.e(MODULE_TAG, ex.getMessage());
        }
    }

    /**
     * Called when the <code>activity<code/> will start interacting with the user. At this point
     * the <code>activity<code/> is at the top of the <code>activity<code/> stack, with user
     * input going to it.  Always followed by <code>onPause()<code/>.
     * @see <code>onPause<code/> class.
     */
    @Override
    public void onResume() {
        try {
            super.onResume(); // call the super method
        }
        catch (Exception ex){
            Log.e(MODULE_TAG, ex.getMessage());
        }
    }

    /**
     * Called when the system is about to start resuming a previous <code>activity<code/>.
     * Followed by either <code>onResume(<code/> if the <code>activity<code/> returns back
     * to the front, or <code>onStop()<code/> if it becomes invisible to the user.
     * @see <code>onResume<code/> class.
     * @see <code>onStop<code/> class.
     */
    @Override
    public void onPause() {
        try {
            super.onPause(); // call the super method
        }
        catch (Exception ex){
            Log.e(MODULE_TAG, ex.getMessage());
        }
    }

    /**
     * Called when the <code>activity<code/> is no longer visible to the user, because another
     * <code>activity<code/> has been resumed and is covering this one. This may happen either
     * because a new <code>activity<code/> is being started, an existing one is being brought
     * in front of this one, or this one is being destroyed.  Followed by either
     * <code>onRestart<code/> if this <code>activity<code/> is coming back to interact with
     * the user, or <code>onDestroy<code/> if this <code>activity<code/> is going away.
     * @see <code>onRestart<code/> class.
     * @see <code>onDestroy<code/> class.
     */
    @Override
    public void onStop() {
        try {
            super.onStop(); // call the super method
        }
        catch (Exception ex){
            Log.e(MODULE_TAG, ex.getMessage());
        }
    }

    /**
     * This is the final call received before the <code>activity<code/> is destroyed. This can
     * happen either because the <code>activity<code/> is finishing (someone called
     * <code>finish()<code/> on it, or because the system is temporarily destroying this instance
     * of the <code>activity<code/> to save space. You can distinguish between these two scenarios
     * with the <code>isFinishing()<code/> method.
     * @see <code>finish<code/> class.
     * @see <code>isFinishing<code/> class.
     */
    @Override
    public void onDestroy() {
        try {
            // Check if currently tracking route. If so save it.
            if (null != routeTracker) {
                if (routeTracker.isTracking()) {
                    routeTracker.stopTracking();
                    RouteCalculator route = routeTracker.getRoute();
                    //todo: save route for later processing.
                }
            }
            super.onDestroy(); // call the super method
        }
        catch (Exception ex){
            Log.e(MODULE_TAG, ex.getMessage());
        }
    }

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
            try {
                if (null == routeTracker ) {
                    String message;
                    if (null != (message = myApp.getInitErrorMessage())) {
                        ErrorDialog(message);
                    }
                    else {
                        ErrorDialog(R.string.err_bad_route_tracker);
                    }
                    buttonView.setChecked(false);
                }
                else {
                    if (isChecked) {


                        mChronometer.start();

                        mChronometer.setBase(SystemClock.elapsedRealtime());


                        try {
                        // Start the route tracking
                        routeTracker.startTracking();
                        }
                        catch(RouteTrackerException ex) {
                            ErrorDialog(R.string.rt_error_start);
                            buttonView.setChecked(false);
                        }
                    }
                    else {

                        mChronometer.stop();



                        try {
                            // Stop the route tracking
                            routeTracker.stopTracking();

                            // Get the route information
                            RouteCalculator route = routeTracker.getRoute();

                            if (null == route) {
                                // Tell the user that something went wrong with the route information.
                                ErrorDialog(R.string.err_no_route_data);
                            }
                            else {
                                // Show the route results to the user.
                                showResult(route);

                                // Upload the route data
                                if (null != dataLayer) {
                                    dataLayer.putRide(route);
                                }
                            }
                        }
                        catch(RouteTrackerException ex) {
                            ErrorDialog(R.string.rt_error_stop);
                        }
                    }
                }
            }
            catch(Exception ex) {
                Log.e(MODULE_TAG, ex.getMessage());
            }
        }

        /**
         * Shows the result of the route tracking to the user
         */
        private void showResult(RouteCalculator route) {
            try {
            // create a dialog displaying the results
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
            dialogBuilder.setTitle(R.string.results);

            // display distanceTraveled traveled and average speed
            dialogBuilder.setMessage(String.format(getResources().getString(R.string.results_format),
                    route.getDistanceKM(),
                    route.getDistanceMI(),
                    route.getSpeedKM(),
                    route.getSpeedMI()));
            dialogBuilder.setPositiveButton(R.string.button_ok, null);
            dialogBuilder.show(); // display the dialog
            }
            catch(Exception ex) {
                Log.e(MODULE_TAG, ex.getMessage());
            }
        }
    }

    private void ErrorDialog(int messageId) {
        ErrorDialog(getResources().getString(messageId));
    }

    private void ErrorDialog(String message) {

        // create a dialog displaying the message
        AlertDialog.Builder alertDialog;

        if (null != (alertDialog = new AlertDialog.Builder(MainActivity.this))) {
            alertDialog.setTitle(R.string.error_dialog_title);
            alertDialog.setMessage(message);
            alertDialog.setPositiveButton(R.string.button_ok, null);
            alertDialog.show(); // display the dialog
        }
    }

    private void InfoDialog(int messageId) {

        // create a dialog displaying the message
        AlertDialog.Builder alertDialog;

        if (null != (alertDialog = new AlertDialog.Builder(MainActivity.this))) {
            alertDialog.setTitle(R.string.info_dialog_title);
            alertDialog.setMessage(getResources().getString(messageId));
            alertDialog.setPositiveButton(R.string.button_ok, null);
            alertDialog.show(); // display the dialog
        }
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
