package edu.pdx.cs.pedal.routetracker;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import android.util.Log;

/**
 * This class is used to persist ride data.
 * @author robin5 (Robin Murray)
 * @version 1.0
 * created 3/1/14
 */
public class RideInfo {

    private static final String MODULE_TAG = "RideInfo";
    private static final String JSON_FIELD_VERSION = "version";
    private static final String JSON_FIELD_START_TIME = "startTime";
    private static final String JSON_FIELD_RIDE_TIME = "rideTime";
    private static final String JSON_FIELD_DISTANCE_MI = "distanceMI";
    private static final String JSON_FIELD_AVG_SPEED_MPH = "avgSpeedMPH";
    private static final String JSON_FIELD_MAX_SPEED_MPH = "maxSpeedMPH";
    private static final String JSON_FIELD_URL = "url";

    private static final String JSON_FIELD_DISTANCE_KM = "distanceKM";
    private static final String JSON_FIELD_AVG_SPEED_KPH = "speedKM";

    private static final double MILES_PER_KILOMETER = 0.621371192;

    private final String rideId;
    private final String dirName;

    // Parameters to save
    private static final String VERSION_0_0 = "0.0";
    private static final String VERSION_0_1 = "0.1";
    private static final String CURRENT_VERSION = VERSION_0_1;

    private long startTime = 0;
    private long rideTime = 0;
    private double distanceMI = 0.0;
    private double avgSpeedMPH = 0.0;
    private double maxSpeedMPH = 0.0;
    private String url = null;

    /**
     * Creates an instance of RideInfo
     * @param ride the ride to get the information from
     * @param dirName the directory where this file should be stored when persisted
     * @param rideId The ride identifier and rideId of file that will be persisted
     */
    public RideInfo(RouteCalculator ride, String dirName, String rideId) {
        this.rideId = rideId;
        this.dirName = dirName;
        this.startTime = ride.getStartTime();
        this.rideTime = ride.getRideTime();
        this.distanceMI = ride.getDistanceMI();
        this.avgSpeedMPH = ride.getSpeedMI();
        this.maxSpeedMPH = ride.getMaxSpeedMI();
    }

    /**
     * Constructs an instance of the class specifying directory and rideId to use for file
     * @param dirName The directory where the file is stored
     * @param rideId The rideId is used as the name of the persisted file.
     */
    public RideInfo(String dirName, String rideId) {
        this.rideId = rideId;
        this.dirName = dirName;
    }

    /**
     * Constructs an instance copy of the class
     * @param r the instance to copy
     */
    public RideInfo(RideInfo r) {
        this.rideId = r.rideId;
        this.dirName = r.dirName;
        this.startTime = r.startTime;
        this.rideTime = r.rideTime;
        this.distanceMI = r.distanceMI;
        this.avgSpeedMPH = r.avgSpeedMPH;
        this.maxSpeedMPH = r.maxSpeedMPH;
        this.url = r.url;
    }

    /**
     * Returns the identifier associated with the ride
     * @return Returns the identifier associated with the ride
     */
    public String getRideId() {
        return rideId;
    }

    /**
     * Returns the startTime of the ride.
     * @return Returns the startTime of the ride.
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * Returns the rideTime of the ride.
     * @return Returns the rideTime of the ride.
     */
    public long getRideTime() {
        return rideTime;
    }

    /**
     * Returns the ride distance in miles.
     * @return Returns the ride distance in miles.
     */
    public double getDistanceMI() {
        return distanceMI;
    }

    /**
     * Returns the average speed of the ride in miles per hour.
     * @return Returns the average speed of the ride in miles per hour.
     */
    public double getAvgSpeedMPH() {
        return avgSpeedMPH;
    }

    /**
     * Returns the max speed of the ride in miles per hour.
     * @return Returns the max speed of the ride in miles per hour.
     */
    public double getMaxSpeedMPH() {
        return maxSpeedMPH;
    }

    /**
     * Returns the url returned from the web site.
     * @return Returns the url returned from the web site.
     */
    public String getURL() {
        return url;
    }

    /**
     * Sets the url returned from the web site.
     */
    public void setURL(String url) {
        this.url = url;
    }

    /**
     * Translates this object into JSON
     * @return Returns the JSON translation
     */
    private String toJSON() {
        JSONObject json = new JSONObject();
        json.put(JSON_FIELD_VERSION, CURRENT_VERSION);
        json.put(JSON_FIELD_START_TIME, startTime);
        json.put(JSON_FIELD_RIDE_TIME, rideTime);
        json.put(JSON_FIELD_DISTANCE_MI, distanceMI);
        json.put(JSON_FIELD_AVG_SPEED_MPH, avgSpeedMPH);
        json.put(JSON_FIELD_MAX_SPEED_MPH, maxSpeedMPH);
        json.put(JSON_FIELD_URL, url);
        return json.toString();
    }

    /**
     * Load the ride information from the persisted file.
     */
    public boolean loadFromDisk() {

        File file = new File(dirName, rideId);
        FileReader fr = null;
        char buff[] = new char[512];
        StringBuilder sb = new StringBuilder();
        boolean loaded = false;

        try {
            fr = new FileReader(file);
            int numBytes;

            // Read and append data to buffer until EOF
            while(-1 != (numBytes = fr.read(buff, 0, 512))) {
                sb.append(buff, 0, numBytes);
            }

            // Parse JSON data
            JSONObject jsonObject;
            JSONParser parser = new JSONParser();
            jsonObject = (JSONObject) parser.parse(sb.toString());

            if (!jsonObject.containsKey(JSON_FIELD_VERSION)) {
                return false;
            }
            String version = (String) jsonObject.get(JSON_FIELD_VERSION);

            if (version.equals(VERSION_0_0)) {
                // Validate presence of JSON fields
                if (jsonObject.containsKey(JSON_FIELD_DISTANCE_KM) &&
                        jsonObject.containsKey(JSON_FIELD_AVG_SPEED_KPH)) {

                    startTime = 0;
                    rideTime = 0;
                    distanceMI = ((Double) jsonObject.get(JSON_FIELD_DISTANCE_KM)).doubleValue() * MILES_PER_KILOMETER;
                    avgSpeedMPH = ((Double) jsonObject.get(JSON_FIELD_AVG_SPEED_KPH)).doubleValue() * MILES_PER_KILOMETER;
                    maxSpeedMPH = 0;
                    url = null;
                    loaded = true;
                }
            }
            else if (version.equals(VERSION_0_1)) {
                if (jsonObject.containsKey(JSON_FIELD_START_TIME) &&
                        jsonObject.containsKey(JSON_FIELD_RIDE_TIME) &&
                        jsonObject.containsKey(JSON_FIELD_DISTANCE_MI) &&
                        jsonObject.containsKey(JSON_FIELD_AVG_SPEED_MPH) &&
                        jsonObject.containsKey(JSON_FIELD_MAX_SPEED_MPH) &&
                        jsonObject.containsKey(JSON_FIELD_URL)) {

                    startTime = ((Long) jsonObject.get(JSON_FIELD_START_TIME)).longValue();
                    rideTime = ((Long) jsonObject.get(JSON_FIELD_RIDE_TIME)).longValue();
                    distanceMI = ((Double) jsonObject.get(JSON_FIELD_DISTANCE_MI)).doubleValue();
                    avgSpeedMPH = ((Double) jsonObject.get(JSON_FIELD_AVG_SPEED_MPH)).doubleValue();
                    maxSpeedMPH = ((Double) jsonObject.get(JSON_FIELD_MAX_SPEED_MPH)).doubleValue();
                    url = (String) jsonObject.get(JSON_FIELD_URL);
                    loaded = true;
                }
            }
        }
        catch(Exception ex) {
            Log.d(MODULE_TAG, ex.getMessage());
        }
        finally {
            // close the reader
            try {
                if (null != fr)
                    fr.close();
            }
            catch(IOException ex) {
                Log.e(MODULE_TAG, ex.getMessage());
            }
        }
        return loaded;
    }

    /**
     * Saves this object to disk
     */
    public boolean saveToDisk() {

        // If a file already exists, delete it.
        File file = new File(dirName, rideId);
        if (file.exists())
            if (!file.delete())
                return false;

        // Write data to file (JSON format)
        FileWriter fw;
        try {
            if (null != (fw = new FileWriter(file))) {
                String toJson = this.toJSON();
                fw.write(toJson);
                fw.close();
                return true;
            }
        }
        catch(IOException ex) {
            throw new RideInfoException(ex);
        }
        return false;
    }

    /**
     * Removes the file associated with this object from disk
     */
    public void removeFromDisk() {
        File file = new File(dirName, rideId);
        if (file.exists())
            if (!file.delete())
                Log.d(MODULE_TAG, "Could not delete file: " + rideId);
    }
}