package com.pedalportland.routetracker;

import android.location.Location;
import java.util.List;
import java.util.ArrayList;

/**
 * This class collects route data and implements the calculation of total distance travelled
 * and average speed
 * @author Robin Murray
 * @version 1.0
 */
public class RouteCalculator {

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

    /**
     * Instantiates an instance of a <code>RouteCalculator</code>
     */
    public RouteCalculator() {
        route = new ArrayList<Location>();
        this.reset();
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
}
