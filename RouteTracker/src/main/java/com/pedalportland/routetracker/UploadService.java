package com.pedalportland.routetracker;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import java.io.File;

/**
 * This class implements a service for uploading files to the web site
 * @author robin5 (Robin Murray)
 * @version 1.0
 * created 2/15/14
 */
public class UploadService extends Service {

    public static final String EXTRA_RIDES_DIR_NAME = "EXTRA_RIDES_DIR_NAME";
    public static final String EXTRA_UPLOAD_ROOT_DIR_NAME = "EXTRA_UPLOAD_ROOT_DIR_NAME";
    public static final String EXTRA_UPLOAD_URL = "EXTRA_UPLOAD_URL";
    public static final String FLAGS_DIR_NAME = "/flags";
    public static final String RESPONSES_DIR_NAME = "/responses";

    private final IBinder binder = new MyBinder();
    private String ridesDirName;    // Directory where ride files are kept
    private String responseDirName;
    private String url;
    private Thread uploadTask;
    private boolean filesPending = false;

    /**
     * Called by the system when the service is first created.  Do not call this method directly.
     */
    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     * Return the communication channel to the service.  May return null if
     * clients can not bind to the service.  The returned
     * {@link android.os.IBinder} is usually for a complex interface
     * that has been <a href="{@docRoot}guide/components/aidl.html">described using
     * aidl</a>.
     *
     * <p><em>Note that unlike other application components, calls on to the
     * IBinder interface returned here may not happen on the main thread
     * of the process</em>.  More information about the main thread can be found in
     * <a href="{@docRoot}guide/topics/fundamentals/processes-and-threads.html">Processes and
     * Threads</a>.</p>
     *
     * @param intent The Intent that was used to bind to this service,
     * as given to {@link android.content.Context#bindService
     * Context.bindService}.  Note that any extras that were included with
     * the Intent at that point will <em>not</em> be seen here.
     *
     * @return Return an IBinder through which clients can call on to the
     *         service.
     */
    @Override
    public IBinder onBind(Intent intent) {
        LoadConfig(intent);
        return binder;
    }

    /**
     * Loads the following Extras values sent in via the intent:
     *     EXTRA_UPLOAD_ROOT_DIR_NAME
     *
     * @param intent
     * @throws UploadServiceException
     */
    private void LoadConfig(Intent intent) {

        if (null != intent) {

            String uploaderRootDirName;

            // The cache directory must be defined
            if (null == (uploaderRootDirName = intent.getStringExtra(EXTRA_UPLOAD_ROOT_DIR_NAME))) {
                throw new UploadServiceException();
            }

            // Make the markers directory if it does not exist
            UploadFlag.setDirectory(uploaderRootDirName + FLAGS_DIR_NAME);

            // Make the responses directory if it does not exist
            responseDirName = uploaderRootDirName + RESPONSES_DIR_NAME;
            File responseDir = new File(responseDirName);
            if (!responseDir.exists())
               if (!responseDir.mkdirs())
                    throw new UploadServiceException();

            // Record the rides directory
            if (null == (ridesDirName = intent.getStringExtra(EXTRA_RIDES_DIR_NAME))) {
                throw new UploadServiceException();
            }

            // Determine the URL to upload the data to
            if (null == (url = intent.getStringExtra(EXTRA_UPLOAD_URL))) {
                throw new UploadServiceException();
            }
        }
    }

    /**
     * Binding object for this service
     */
    public class MyBinder extends Binder {

        /**
         * This function returns a reference to the bound service
         *
         * @return Reference to the bound service
         */
        public UploadService getService() {
            return UploadService.this;
        }
    }

    /**
     * Called by the system every time a client explicitly starts the service by calling
     * {@link android.content.Context#startService}, providing the arguments it supplied and a
     * unique integer token representing the start request.  Do not call this method directly.
     *
     * <p>For backwards compatibility, the default implementation calls
     * {@link #onStart} and returns either {@link #START_STICKY}
     * or {@link #START_STICKY_COMPATIBILITY}.
     *
     * <p>If you need your application to run on platform versions prior to API
     * level 5, you can use the following model to handle the older {@link #onStart}
     * callback in that case.  The <code>handleCommand</code> method is implemented by
     * you as appropriate:
     *
     * {@sample development/samples/ApiDemos/src/com/example/android/apis/app/ForegroundService.java
     *   start_compatibility}
     *
     * <p class="caution">Note that the system calls this on your
     * service's main thread.  A service's main thread is the same
     * thread where UI operations take place for Activities running in the
     * same process.  You should always avoid stalling the main
     * thread's event loop.  When doing long-running operations,
     * network calls, or heavy disk I/O, you should kick off a new
     * thread, or use {@link android.os.AsyncTask}.</p>
     *
     * @param intent The Intent supplied to {@link android.content.Context#startService},
     * as given.  This may be null if the service is being restarted after
     * its process has gone away, and it had previously returned anything
     * except {@link #START_STICKY_COMPATIBILITY}.
     * @param flags Additional data about this start request.  Currently either
     * 0, {@link #START_FLAG_REDELIVERY}, or {@link #START_FLAG_RETRY}.
     * @param startId A unique integer representing this specific request to
     * start.  Use with {@link #stopSelfResult(int)}.
     *
     * @return The return value indicates what semantics the system should
     * use for the service's current started state.  It may be one of the
     * constants associated with the {@link #START_CONTINUATION_MASK} bits.
     *
     * @see #stopSelfResult(int)
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LoadConfig(intent);
        return Service.START_REDELIVER_INTENT;
    }

    /**
     * starts the upload process for rides
     * @param rideId the ride to be uploaded
     */
    public void uploadRide(String rideId) {

        // create an upload flag for the new ride
        UploadFlag f = new UploadFlag(rideId);
        f.save();

        // Start the upload task
        startRideUpload();
    }

    /**
     * Starts the Ride upload thread, or if thread is already active
     * records that there are more files to be uploaded
     */
    public void startRideUpload() {

        // If the thread has never been created or is finished, we can create a new one.
        if ((null == uploadTask) || (Thread.State.TERMINATED == uploadTask.getState())) {
            filesPending = false;
            uploadTask = new UploadTask(url, ridesDirName, responseDirName);
            uploadTask.start();
        }
        // else notify this object that there is a new file to be uploaded
        else {
            filesPending = true;
            scheduleFutureUpload();
        }
    }

    /**
     * Indicates the status of fles pending upload to the web site
     * @return
     */
    public boolean getFilesPending() {
        return filesPending;
    }

    /**
     * USes AlarmManager to schedule future upload attemts to the web site
     */
    public void scheduleFutureUpload() {
    }
}

