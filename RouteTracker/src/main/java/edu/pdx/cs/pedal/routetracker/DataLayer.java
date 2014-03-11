package edu.pdx.cs.pedal.routetracker;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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
    private String dataRootDir = null;
    private String ridesDirName = null;

    private UploadService uploadService;
    private RideInfoManager rideInfoManager = null;
    private UrlInfoManager urlInfoManager = null;

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
         * Called when a connection to the Service has been lost. This typically
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

        this.dataRootDir = dataRootDir;
        ridesDirName = dataRootDir + RIDE_FILES_DIR;
        File rideFilesDir = new File(ridesDirName);
        if (!rideFilesDir.exists())
            if (!rideFilesDir.mkdirs())
                throw new DataLayerException();
    }

    /**
     * This function initializes the directory values used by the DataLayer. Note that the
     * Uploader service must have been initialized before this function is called.
     * @param uploadRootDir The root directory for the UploadService
     */
    public void Init(String uploadRootDir) {
        String urlFileDir = uploadRootDir + UploadService.RESPONSES_DIR_NAME;
        urlInfoManager = new UrlInfoManager(urlFileDir);
        rideInfoManager = new RideInfoManager(dataRootDir + RIDE_INFOS_DIR);
        rideInfoManager.setUrls(urlInfoManager);
    }

    /**
     * Returns the name of the directory where the dataLayer stores ride files
     * @return Ride directory
     */
    public String getRidesDirName() {
        return ridesDirName;
    }

    /**
     * Stores a Ride to disk, creates a Ride Info file, then schedules it be uploaded
     * @param ride The ride object to be stored.
     */
    public void putRide(RouteCalculator ride) {

        // Create an identifier for this ride
        String rideId = makeRideId(ride);

        // Save the ride to disk and schedule it to be uploaded to the web site
        if (saveRideToDisk(ride, rideId)) {
            if (null != uploadService) {
                uploadService.uploadRide(rideId);
            }
        }
    }

    /**
     * Saves the ride to disk, and gives it the specified name
     * @param ride the ride to be saved
     * @param rideId the ride's identifier
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
                rideInfoManager.addRideInfo(ride, rideId);
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
     * @param ride the ride to create the identifier for
     * @return the unique identifier for this ride
     * @throws java.lang.NullPointerException
     * @throws java.lang.IllegalArgumentException
     */
    private String makeRideId(RouteCalculator ride) {
        return String.format("r%d", ride.getStartTime());
    }

    /**
     * Returns all of the ride information recorded for the individual rides
     * @return returns an array of RideInfo
     */
    public RideInfo[] getRideInfos() {
        return rideInfoManager.getRideInfos();
    }

    /**
     * Returns the ride information specified by rideId
     * @param rideId name of
     * @return Returns the RideInfo specified by rideId
     */
    public RideInfo getRideInfo(String rideId) {
        return rideInfoManager.getRideInfo(rideId);
    }

    /**
     * Gets all of the ride data (locations included) associated with a specified ride
     * @param rideId the identifier of the ride
     * @return Returns the Ride
     */
    public RouteCalculator getRide(String rideId) {
        return new RouteCalculator(new File(ridesDirName, rideId));
    }

    /**
     * Deletes the Ride and Ride Information specified by the ride identifier
     * @param rideId the identifier of the ride to delete
     */
    public void deleteRide(String rideId) {

        File rideFile = new File(ridesDirName, rideId);
        if (rideFile.exists()) {
            if (rideFile.delete()) {
                rideInfoManager.deleteRideInfo(rideId);
            }
            else
                Log.d(MODULE_TAG, "Could not delete ride: " + rideId);
        }
    }

    /**
     * Calls the upload service to check for files to upload to the web site
     */
    public void startRideUpload() {
        if (null != uploadService) {
            uploadService.startRideUpload();
        }
    }

    /**
     * Deletes all of the data stored by the application
     */
    public void deleteAll() {

        File ridesDir = new File(ridesDirName);
        File files[] = ridesDir.listFiles();
        if (null != files) {
            for (File file: files) {
                if (!file.delete()) {
                    Log.d(MODULE_TAG, "Could not delete file: " + file.getName());
                }
            }
        }

        rideInfoManager.deleteAll();
        urlInfoManager.deleteAll();
    }
}