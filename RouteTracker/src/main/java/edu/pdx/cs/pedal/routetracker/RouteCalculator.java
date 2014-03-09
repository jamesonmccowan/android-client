package edu.pdx.cs.pedal.routetracker;

import android.content.pm.LabeledIntent;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.ArrayList;
import org.joda.time.DateTime;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * This class collects route data and implements the calculation of total distance travelled
 * and average speed
 * @author Robin Murray
 * @version 1.0
 */
public class RouteCalculator {

    private static final String JSON_VERSION = "0.4";
    private boolean isTracking;     // Whether app is currently tracking
    private long startTime;         // Time (in milliseconds) when tracking starts
    private long distanceTraveled;  // Distance traveled by user in meters

    // Constants used in calculating total route distance and average speed
    private static final double MILLISECONDS_PER_HOUR = 1000 * 60 * 60;
    private static final double MILES_PER_KILOMETER = 0.621371192;

    private List<Location> route = null;    // List of locations determining route
    private Location previousLocation;      // previous reported location

    private double distanceKM;  // Total distance in kilometers
    private double speedKM;     // Average speed in kilometers/Hour
    private double distanceMI;  // Total distance in miles
    private double speedMI;     // Average speed in miles/Hour
    private static double startpoint;
    private static double endpoint;

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
    public RouteCalculator(String fileDirectory, String fileName) {
        // Load JSON file here
    }

    /**
     *
     */
    public void loadFromDisk() {

    }

    /**
     * Resets route data calculations
     */
    public void reset() {
        isTracking = false;
        route.clear();
        previousLocation = null;

        startTime = 0;
        distanceTraveled = 0;
        distanceKM = 0.0;
        speedKM = 0.0;
        distanceMI = 0.0;
        speedMI = 0.0;
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
            double expiredTime = ((double)currentTime - (double)startTime) / MILLISECONDS_PER_HOUR;
            assert(expiredTime >= 0.0);

            isTracking = false;
            distanceKM = distanceTraveled / 1000.0;
            speedKM = distanceKM / expiredTime;
            distanceMI = distanceKM * MILES_PER_KILOMETER;
            speedMI = distanceMI / expiredTime;
        }
    }

    /**
     *
     * @return
     */
    public long getStartTime() {
        return startTime;
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
     * Adds a new location to the route.
     */
    public void AddLocation(Location location) {

        assert(isTracking);
        if (isTracking) {
            if (previousLocation != null) {
                // add to the total distanceTraveled
                distanceTraveled += location.distanceTo(previousLocation);
            }
            route.add(location);
            previousLocation = location;
        }
    }

    /**
     * Returns the route travelled.
     */
    public List getRoute() {
        return route;
    }

    /**
     *
     * @param locations
     * @return
     */
    public static String toJSON(List<Location> locations) {
        JSONArray points = new JSONArray();
        locations = clipping(locations);
        for(Location point : locations) {
            JSONObject obj = new JSONObject();
            obj.put("time", (new DateTime(point.getTime())).toString()); // convert from UTC time, in milliseconds since January 1, 1970 to ISO 8601
            obj.put("latitude", point.getLatitude());
            obj.put("longitude", point.getLongitude());
            obj.put("accuracy", point.getAccuracy());
            points.add(obj);}

        JSONObject route = new JSONObject();
        route.put("points", points);
        route.put("version", JSON_VERSION);
        route.put("hash", locations.hashCode());

        return route.toString();
    }

    public static double converttoRad(double x) {return x * Math.PI/180;}

    public static List<Location> clipping(List<Location> locations){
        double R = 3958.756; // Radius of earth in miles
        double distance_end = 0;
        double distance_start = 0;

        // this is the method to clip the route from start point
        for (int index = 0; index <= locations.size()- 1; index++){
            double dLat = converttoRad(locations.get(index +1).getLatitude() -
                    locations.get(index).getLatitude());
            double dLong = converttoRad(locations.get(index +1).getLongitude() -
                    locations.get(index).getLongitude());
            double curr_distance = Math.pow(Math.sin(dLat/2),2) +
                    Math.cos(converttoRad(locations.get(index).getLatitude())) *
                            Math.cos(converttoRad(locations.get(index+1).getLatitude())) *
                            Math.pow(Math.sin(dLong/2),2);
            double curr1_distance = 2 * Math.atan2(Math.sqrt(curr_distance),Math.sqrt(1-curr_distance));
            distance_start += R * curr1_distance;

            if(distance_start >= startpoint){
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
            double dLat = converttoRad(locations.get(index -1).getLatitude() -
                    locations.get(index).getLatitude());
            double dLong = converttoRad(locations.get(index -1).getLongitude() -
                    locations.get(index).getLongitude());
            double curr_distance = Math.pow(Math.sin(dLat/2),2) +
                    Math.cos(converttoRad(locations.get(index).getLatitude())) *
                    Math.cos(converttoRad(locations.get(index - 1).getLatitude())) *
                    Math.pow(Math.sin(dLong/2),2);
            double curr1_distance = 2 * Math.atan2(Math.sqrt(curr_distance),Math.sqrt(1-curr_distance));
            distance_end += R * curr1_distance;

            if (distance_end >= endpoint)
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

    public double getStartpoint () {return this.startpoint;}

    public void setStartpoint (double StartPoint){ this.startpoint = StartPoint;
    System.out.println(this.startpoint);}

    public double getEndpoint () {return this.endpoint;}

    public void setEndpoint(double EndPoint) {this.endpoint = EndPoint;
        System.out.println(this.startpoint);}
}
