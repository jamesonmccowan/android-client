package edu.pdx.cs.pedal.routetracker;

import java.io.File;
import org.joda.time.DateTime;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import android.location.Location;
import android.util.Log;

/**
 * This class is used to maintain persistent ride information data.
 * @author robin5 (Robin Murray)
 * @version 1.0
 * created 3/1/14
 */
public class RideInfo {

    private static final String MODULE_TAG = "RideInfo";
    private static final String JSON_FIELD_VERSION = "version";
    private static final String JSON_FIELD_DISTANCE_KM = "distanceKM";
    private static final String JSON_FIELD_DISTANCE_MI = "distanceMI";
    private static final String JSON_FIELD_DATE = "date";
    private static final String JSON_FIELD_TIME = "time";
    private static final String JSON_FIELD_SPEED_KM = "speedKM";
    private static final String JSON_FIELD_SPEED_MI = "speedMI";

    private final String name;
    private final String dirName;

    // Parameters to save
    private final static String VERSION = "0.1";

    private double distanceKM = 0.0;
    private double distanceMI = 0.0;
    private DateTime date = null;
    private double time = 0.0;
    private double speedKM = 0.0;
    private double speedMI = 0.0;

    /**
     * Creates an instance of RideInfo
     * @param ride
     * @param dirName
     * @param name
     */
    public RideInfo(RouteCalculator ride, String dirName, String name) {
        this.name = name;
        this.dirName = dirName;
        distanceKM = ride.getDistanceKM();
        distanceMI = ride.getDistanceMI();
        speedKM = ride.getSpeedKM();
        speedMI = ride.getSpeedMI();
        time = ride.getExpiredTime();
        try {
            date = new DateTime(((List<Location>) ride.getRoute()).get(0).getTime());
        } catch (Exception ex) {
            date = null;
        }
    }

    /**
     *
     * @param dirName
     * @param name
     */
    public RideInfo(String dirName, String name) {
        this.name = name;
        this.dirName = dirName;
    }

    /**
     *
     * @param r
     */
    public RideInfo(RideInfo r) {
        this.name = r.name;
        this.dirName = r.dirName;
        this.distanceKM = r.distanceKM;
        this.distanceKM = r.distanceMI;
        this.speedKM = r.speedKM;
        this.speedMI = r.speedMI;
        this.time = r.time;
        this.date = r.date;
    }

    /**
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @return
     */
    public double getDistanceKM() { return distanceKM; }

    /**
     *
     * @return
     */
    public double getDistanceMI() {
        return distanceMI;
    }

    /**
     *
     * @return
     */
    public double getSpeedMI() {
        return speedMI;
    }

    /**
     *
     * @return
     */
    public double getSpeedKM() {
        return speedKM;
    }

    /**
     *
     * @return
     */
    public DateTime getDate() { return date; }

    /**
     *
     * @return
     */
    public double getTime() { return time; }

    /**
     *
     * @return
     */
    private String toJSON() {
        JSONObject rideInfo = new JSONObject();
        rideInfo.put(JSON_FIELD_VERSION, VERSION);
        rideInfo.put(JSON_FIELD_DISTANCE_KM, distanceKM);
        rideInfo.put(JSON_FIELD_DISTANCE_MI, distanceMI);
        if(date != null)
            rideInfo.put(JSON_FIELD_DATE, date.getMillis());
        rideInfo.put(JSON_FIELD_TIME, time);
        rideInfo.put(JSON_FIELD_SPEED_KM, speedKM);
        rideInfo.put(JSON_FIELD_SPEED_MI, speedMI);
        return rideInfo.toString();
    }

    /**
     *
     */
    public boolean loadFromDisk() {

        File file = new File(dirName, name);
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

            // Validate presence of JSON fields
            if (jsonObject.containsKey(JSON_FIELD_VERSION) &&
                jsonObject.containsKey(JSON_FIELD_DISTANCE_KM) &&
                jsonObject.containsKey(JSON_FIELD_SPEED_KM)) {

                String version = (String) jsonObject.get(JSON_FIELD_VERSION);
                distanceKM = ((Double) jsonObject.get(JSON_FIELD_DISTANCE_KM)).doubleValue();
                speedKM = ((Double) jsonObject.get(JSON_FIELD_SPEED_KM)).doubleValue();

                if (jsonObject.containsKey(JSON_FIELD_DISTANCE_MI))
                    distanceMI = ((Double) jsonObject.get(JSON_FIELD_DISTANCE_MI)).doubleValue();

                if (jsonObject.containsKey(JSON_FIELD_SPEED_MI))
                    speedMI = ((Double) jsonObject.get(JSON_FIELD_SPEED_MI)).doubleValue();

                if (jsonObject.containsKey(JSON_FIELD_DATE))
                    date = new DateTime(((Double) jsonObject.get(JSON_FIELD_DATE)).longValue());

                if (jsonObject.containsKey(JSON_FIELD_TIME))
                    time = ((Double) jsonObject.get(JSON_FIELD_TIME)).doubleValue();

                if (version.equals(VERSION))
                    loaded = true;
            }
        }
        catch(Exception ex) {
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
     *
     */
    public void saveToDisk() {

        // If a file already exists, delete it.
        File file = new File(dirName, name);
        if (file.exists())
            if (!file.delete())
                return;

        // Write data to file (JSON format)
        FileWriter fw = null;
        try {
            if (null != (fw = new FileWriter(file))) {
                String toJson = this.toJSON();
                fw.write(toJson);
            }
        }
        catch(IOException ex) {
            throw new RideInfoException(ex);
        }
        finally {
            try {
                if (null != fw)
                    fw.close();
            }
            catch(IOException ex) {
                Log.e(MODULE_TAG, ex.getMessage());
            }
        }
    }

    /**
     *
     */
    public void removeFromDisk() {
        File file = new File(dirName, name);
        file.delete();
    }
}
