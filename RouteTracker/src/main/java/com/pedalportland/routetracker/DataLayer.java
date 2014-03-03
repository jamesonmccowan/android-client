package com.pedalportland.routetracker;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;


/**
 * This class implements the data layer.
 * This class is used to maintain persistent application data.
 * @author robin5 (Robin Murray)
 * @version 1.0
 * created 2/2/15
 */
public class DataLayer {

    private static final String MODULE_TAG = "DataLayer";
    private static final String RIDE_FILES_DIR = "/rides";
    private static final String RIDE_INFOS_DIR = "/infos";
    private String ridesDirName = null;

    private UploadService uploadService;

    /**
     * Connection to the server
     */
    public final ServiceConnection uploaderServiceConnection = new ServiceConnection() {

        /**
         * Called when a connection to the Service has been established, with
         * the {@link android.os.IBinder} of the communication channel to the
         * Service.
         *
         * @param name The concrete component name of the service that has
         * been connected.
         *
         * @param service The IBinder of the Service's communication channel,
         * which you can now make calls on.
         */
        public void onServiceConnected(ComponentName name, IBinder service) {
            uploadService = ((UploadService.MyBinder)service).getService();
        }

        /**
         * Called when a connection to the Service has been lost.  This typically
         * happens when the process hosting the service has crashed or been killed.
         * This does <em>not</em> remove the ServiceConnection itself -- this
         * binding to the service will remain active, and you will receive a call
         * to {@link #onServiceConnected} when the Service is next running.
         *
         * @param name The concrete component name of the service whose
         * connection has been lost.
         */
        public void onServiceDisconnected(ComponentName name) {
            uploadService = null;
        }
    };

    /**
     * Creates an instance of the dataLayer
     * @param dataRootDir root directory for dataLayer to store it's files
     * @throws DataLayerException
     * @throws RideInfoMgrException
     */
    public DataLayer(String dataRootDir) {

        ridesDirName = dataRootDir + RIDE_FILES_DIR;
        File rideFilesDir = new File(ridesDirName);
        if (!rideFilesDir.exists())
            if (!rideFilesDir.mkdirs())
                throw new DataLayerException();
    }

    /**
     * Returns the name of the directory where the dataLayer stores ride files
     * @return Ride directory
     */
    public String getRidesDirName() {
        return ridesDirName;
    }

    /**
     * Stores a Ride to disk, creates a Ride Info file, then starts the upload thread
     * @param ride
     */
    public void putRide(RouteCalculator ride) {

        String rideId = makeRideId(ride);

        if (saveRideToDisk(ride, rideId)) {
            if (null != uploadService) {
                uploadService.uploadRide(rideId);
            }
        }
    }

    /**
     * Saves the ride to disk, and gives it the specified name
     * @param ride
     * @param rideId
     * @return true if ride was saved to disk, false otherwise.
     */
    private boolean saveRideToDisk(RouteCalculator ride, String rideId){

        // If a file already exists, delete it.
        File file = new File(ridesDirName, rideId);
        if (file.exists())
            if (!file.delete())
                return false;

        // Create the file and copy the rides JSON translation to it
        try {
            FileWriter fw;
            if (null != (fw = new FileWriter(file))) {
                fw.write(RouteCalculator.toJSON(ride.getRoute()));
                fw.close();
                return true;
            }
        }
        catch(IOException ex) {
            Log.e(MODULE_TAG, ex.getMessage());
        }
        return false;
    }

    /**
     * Creates the identifier name to be associated with this ride
     * @param ride
     * @return
     * @throws java.lang.NullPointerException
     * @throws java.lang.IllegalArgumentException
     */
    private String makeRideId(RouteCalculator ride) {
        return String.format("r%d", ride.getStartTime());
    }

    /**
     *
     * @param rideId
     * @return
     */
    public RouteCalculator getRide(String rideId) {
        RouteCalculator ride = new RouteCalculator(ridesDirName, rideId);
        ride.loadFromDisk();
        return ride;
    }

    /**
     *
     * @param name
     */
    public void deleteRide(String name) {

        (new File(ridesDirName, name)).delete();
    }

    /**
     *
     */
    public void startRideUpload() {
        if (null != uploadService) {
            uploadService.startRideUpload();
        }
    }
}
