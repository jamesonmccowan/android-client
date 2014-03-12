package edu.pdx.cs.pedal.routetracker;

import android.location.Location;
import android.util.Log;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import com.google.android.gms.maps.model.LatLng;

import java.lang.reflect.Array;
import java.util.List;
import java.util.ArrayList;
import org.joda.time.DateTime;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * This class collects route data and implements the calculation of total distance travelled
 * and average speed
 * @author Robin Murray
 * @version 1.0
 */
public class RouteCalculator {

    private static final String MODULE_TAG = "RouteCalculator";
    private static final String JSON_VERSION = "0.4";
    private static final String JSON_FIELD_VERSION = "version";
    private static final String JSON_FIELD_HASH = "hash";
    private static final String JSON_FIELD_POINTS = "points";
    private static final String JSON_FIELD_TIME = "time";
    private static final String JSON_FIELD_LATITUDE = "latitude";
    private static final String JSON_FIELD_LONGITUDE = "longitude";
    private static final String JSON_FIELD_ACCURACY = "accuracy";

    private boolean isTracking; // Whether app is currently tracking
    private long startTime; // Time (in milliseconds) when tracking starts
    private long rideTime; // Time (in milliseconds) when tracking starts
    private long distanceTraveled; // Distance traveled by user in meters

    // Constants used in calculating total route distance and average speed
    private static final double MILLISECONDS_PER_HOUR = 1000 * 60 * 60;
    private static final double MILES_PER_KILOMETER = 0.621371192;

    private List<Location> route = null; // List of locations determining route
    private Location previousLocation; // previous reported location

    private double distanceKM; // Total distance in kilometers
    private double speedKM; // Average speed in kilometers/Hour
    private double distanceMI; // Total distance in miles
    private double speedMI; // Average speed in miles/Hour
    private double maxSpeedMPH; // Maximum speed in miles/Hour
    private static double start_point; // get start point
    private static double end_point; // get end point
    // get list point for original route
    private static ArrayList<LatLng> trip = new ArrayList<LatLng>();
    // get list point for clipping route
    private static ArrayList<LatLng> trip_unclipped = new ArrayList<LatLng>();

    /**
     * Instantiates an instance of a <code>RouteCalculator</code>
     */
    public RouteCalculator() {
        route = new ArrayList<Location>();
        this.reset();
    }

    /**
     * Instantiates an instance of a <code>RouteCalculator</code> from a file
     */
    public RouteCalculator(File file) {
        route = new ArrayList<Location>();
        this.reset();
        loadFromDisk(file);
    }

    /**
     * Load the ride information from the persisted file.
     */
    public boolean loadFromDisk(File file) {

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

            if (!(jsonObject.containsKey(JSON_FIELD_VERSION) &&
                    jsonObject.containsKey(JSON_FIELD_POINTS) &&
                    jsonObject.containsKey(JSON_FIELD_HASH))) {
                return false;
            }

            String version = (String) jsonObject.get(JSON_FIELD_VERSION);
            if (!version.equals(JSON_VERSION)) {
                return false;
            }

            route.clear();
            JSONArray points = (JSONArray)jsonObject.get(JSON_FIELD_POINTS);

            int size = points.size();
            double longitude;
            double latitude;
            DateTime dateTime;
            Float accuracy;

            for (Object point : points) {
                JSONObject entry = (JSONObject) point;

                dateTime = new DateTime(entry.get(JSON_FIELD_TIME));
                latitude = (Double) entry.get(JSON_FIELD_LATITUDE);
                longitude = (Double) entry.get(JSON_FIELD_LONGITUDE);
                accuracy = (float) ((Double) entry.get(JSON_FIELD_ACCURACY)).doubleValue();

                Location location = new Location("");
                location.setAccuracy(accuracy);
                location.setLongitude(longitude);
                location.setLatitude(latitude);

                long millis = dateTime.getMillis();
                location.setTime(millis);

                route.add(location);
            }
            loaded = true;
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
     * Resets route data calculations
     */
    public void reset() {
        isTracking = false;
        route.clear();
        previousLocation = null;

        startTime = 0;
        rideTime = 0;
        distanceTraveled = 0;
        distanceKM = 0.0;
        speedKM = 0.0;
        distanceMI = 0.0;
        speedMI = 0.0;
        maxSpeedMPH = 0.0;
    }

    /**
     * Resets and starts route data collection.
     */
    public void start() {
        this.reset();
        isTracking = true;
        startTime = System.currentTimeMillis(); // get current time
    }

    /**
     * Stops route data collection, and performs calculation of distance travelled.
     */
    public void stop() {

        assert(isTracking);

        if (isTracking) {
            // compute the total time we were isTracking
            long currentTime = System.currentTimeMillis();
            rideTime = currentTime - startTime;
            double expiredTimeHrs = ((double)currentTime - (double)startTime) / MILLISECONDS_PER_HOUR;
            assert(expiredTimeHrs >= 0.0);

            isTracking = false;
            distanceKM = distanceTraveled / 1000.0;
            speedKM = distanceKM / expiredTimeHrs;
            distanceMI = distanceKM * MILES_PER_KILOMETER;
            speedMI = distanceMI / expiredTimeHrs;
        }
    }

    /**
     * Returns the start time of the ride
     * @return the start time of the ride
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * Returns the ride time of the ride
     * @return the ride time of the ride
     */
    public long getRideTime() {
        return rideTime;
    }

    /**
     * Returns distance travelled in kilometers/hr.
     */
    public double getDistanceKM() {
        assert(!isTracking);
        return distanceKM;
    }

    /**
     * Returns average speed in kilometers/hr.
     */
    public double getSpeedKM() {
        assert(!isTracking);
        return speedKM;
    }

    /**
     * Returns distance travelled in miles/hr.
     */
    public double getDistanceMI() {
        assert(!isTracking);
        return distanceMI;
    }

    /**
     * Returns average speed in miles/hr.
     */
    public double getSpeedMI() {
        assert(!isTracking);
        return speedMI;
    }

    /**
     * Returns average speed in miles/hr.
     */
    public double getMaxSpeedMI() {
        assert(!isTracking);
        return maxSpeedMPH;
    }

    /**
     * Adds a new location to the route.
     */
    public void AddLocation(Location location) {

        assert(isTracking);
        if (isTracking) {

            // add to the total distanceTraveled
            if (previousLocation != null) {
                distanceTraveled += location.distanceTo(previousLocation);
            }

            // If speed was calculated for location, update maximum speed if appropriate
            if (location.hasSpeed()) {
                double speedMPH = location.getSpeed() * 2.236936292;
                if (speedMPH > maxSpeedMPH) {
                    maxSpeedMPH = speedMPH;
                }
            }

            // Add the location to the ride
            route.add(location);
            previousLocation = location;
        }
    }

    /**
     * Calculates the speed in miles/hr. given a distance change in meters, and time in milliseconds
     * @param d distance travelled inmeters
     * @param t0 initial tick of the clock in milliseconds
     * @param t1 final tick of the clock in milliseconds
     * @return speed in Miles/Hr.
     */
    private double calcSpeedMPH(double d, double t0, double t1) {

        double cf = 2236.936292; // (1 mi/1609.344 meters) * (3,600,000 ms/hr) = (mi*ms)/(meters*hr)
        return (t1 == t0) ? 0 : (d * 2236.936292) / (t1 - t0);
    }

    /**
     * Calculates the speed in miles/hr. from the location getSpeed() field returned by the location
     * @param location the location field
     * @return
     */
    private double calcSpeedMPH2(Location location) {

        return location.getSpeed() * 2.236936292;
    }

    /**
     * Returns the route travelled.
     */
    public List<Location> getRoute() {
        return route;
    }

    /**
     * Translates the ride data into JSON
     * @param locations the list of ride locations
     * @return the JSON string
     */
    public static String toJSON(List<Location> locations) {
        JSONArray points = new JSONArray();

        for (Location point : locations){
            trip_unclipped.add(new LatLng(point.getLatitude(),point.getLongitude()));
        }

        locations = clipping(locations);

        for (Location point : locations){
            trip.add(new LatLng(point.getLatitude(),point.getLongitude()));
        }

        for(Location point : locations) {
            JSONObject obj = new JSONObject();
            obj.put("time", (new DateTime(point.getTime())).toString()); // convert from UTC time, in milliseconds since January 1, 1970 to ISO 8601
            obj.put("latitude", point.getLatitude());
            obj.put("longitude", point.getLongitude());
            obj.put("accuracy", point.getAccuracy());
            points.add(obj);
        }
        JSONObject route = new JSONObject();
        route.put("points", points);
        route.put("version", JSON_VERSION);
        route.put("hash", locations.hashCode());

        return route.toString();
    }

    public static double convertToRad(double x) {return x * Math.PI/180;}


    public static List<Location> clipping(List<Location> locations){
        /*double R = 3958.756; // Radius of earth in miles
        double distance_end = 0;
        double distance_start = 0;

        // this is the method to clip the route from start point
        for (int index = 0; index <= locations.size()- 1; index++){
            double dLat = convertToRad(locations.get(index +1).getLatitude() -
                    locations.get(index).getLatitude());
            double dLong = convertToRad(locations.get(index +1).getLongitude() -
                    locations.get(index).getLongitude());
            double curr_distance = Math.pow(Math.sin(dLat/2),2) +
                    Math.cos(convertToRad(locations.get(index).getLatitude())) *
                            Math.cos(convertToRad(locations.get(index+1).getLatitude())) *
                            Math.pow(Math.sin(dLong/2),2);
            double curr1_distance = 2 * Math.atan2(Math.sqrt(curr_distance),Math.sqrt(1-curr_distance));
            distance_start += R * curr1_distance;

            if(distance_start >= start_point){
                if (index == 0) {
                    locations.remove(0);
                    break;}
                else{
                    List<Location> sublocation = new ArrayList<Location>(
                            locations.subList(0,index));
                    locations.removeAll(sublocation);
                    break;}}
        }

        // this is the method to clip the route from end point
        for (int index = locations.size() -1; index >= 0; index--){
            double dLat = convertToRad(locations.get(index -1).getLatitude() -
                    locations.get(index).getLatitude());
            double dLong = convertToRad(locations.get(index -1).getLongitude() -
                    locations.get(index).getLongitude());
            double curr_distance = Math.pow(Math.sin(dLat/2),2) +
                    Math.cos(convertToRad(locations.get(index).getLatitude())) *
                            Math.cos(convertToRad(locations.get(index - 1).getLatitude())) *
                            Math.pow(Math.sin(dLong/2),2);
            double curr1_distance = 2 * Math.atan2(Math.sqrt(curr_distance),Math.sqrt(1-curr_distance));
            distance_end += R * curr1_distance;


            if (distance_end >= end_point)
            {
                if(index == locations.size() - 1) {
                    locations.remove(locations.size() - 1);
                    break;}
                else{
                    List<Location> sublocation = new ArrayList<Location>(
                            locations.subList(index +1, locations.size()-1));
                    locations.removeAll(sublocation);
                    break;}}
        }*/
        double distance_end = 0;
        double distance_start = 0;

        // this is the method to clip the route from start point
        for (int index = 0; index <= locations.size()- 1; index++){
            distance_start += ((double) locations.get(index +1).distanceTo(locations.get(index))) * MILES_PER_KILOMETER / 1000;

            if(distance_start >= start_point){
                if (index == 0) {
                    locations.remove(0);
                    break;}
                else{
                    List<Location> sublocation = new ArrayList<Location>(
                            locations.subList(0,index));
                    locations.removeAll(sublocation);
                    break;}}
        }

        // this is the method to clip the route from end point
        for (int index = locations.size() -1; index >= 0; index--){
            distance_end += ((double) locations.get(index -1).distanceTo(locations.get(index))) * MILES_PER_KILOMETER / 1000;

            if (distance_end >= end_point)
            {
                if(index == locations.size() - 1) {
                    locations.remove(locations.size() - 1);
                    break;}
                else{
                    List<Location> sublocation = new ArrayList<Location>(
                            locations.subList(index +1, locations.size()-1));
                    locations.removeAll(sublocation);
                    break;}}
        }
        return locations;
    }

    public double getStartpoint () {return this.start_point;}

    public void setStartpoint (double StartPoint){ this.start_point = StartPoint; }

    public double getEndpoint () {return this.end_point;}

    public void setEndpoint(double EndPoint) {this.end_point = EndPoint; }

    public static ArrayList<LatLng> getTrip() {return trip; }

    public static ArrayList<LatLng> getTrip_unclipped () {return trip_unclipped; }
}