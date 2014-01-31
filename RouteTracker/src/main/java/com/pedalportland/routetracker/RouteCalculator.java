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

    private boolean tracking;                                           // whether app is currently tracking
    private long startTime;                                             // time (in milliseconds) when tracking starts
    private long distanceTraveled;                                      // total distance the user traveled
    private static final double MILLISECONDS_PER_HOUR = 1000 * 60 * 60; //
    private static final double MILES_PER_KILOMETER = 0.621371192;

    private List<Location> route = null;
    private Location previousLocation;                                  // previous reported location

    private double distanceKM;
    private double speedKM;
    private double distanceMI;
    private double speedMI;

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
        tracking = false;
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
        tracking = true;
        startTime = System.currentTimeMillis(); // get current time
    }

    /**
     * Stops route data collection, and performs calculation of distance travelled.
     */
    public void stop() {

        assert(tracking);

        // compute the total time we were tracking
        double expiredTime = (System.currentTimeMillis() - startTime) / MILLISECONDS_PER_HOUR;
        assert(expiredTime >= 0.0);

        tracking = false;
        distanceKM = distanceTraveled / 1000.0;
        speedKM = distanceKM / expiredTime;
        distanceMI = distanceKM * MILES_PER_KILOMETER;
        speedMI = distanceMI / expiredTime;
    }

    /**
     * Returns distance travelled in kilometers/hr.
     */
    public double getDistanceKM() {
        assert(!tracking);
        return distanceKM;
    }

    /**
     * Returns average speed in kilometers/hr.
     */
    public double getSpeedKM() {
        assert(!tracking);
        return speedKM;
    }

    /**
     * Returns distance travelled in miles/hr.
     */
    public double getDistanceMI() {
        assert(!tracking);
        return distanceMI;
    }

    /**
     * Returns average speed in miles/hr.
     */
    public double getSpeedMI() {
        assert(!tracking);
        return speedMI;
    }

    /**
     * Adds a new location to the route.
     */
    public void AddLocation(Location location) {
        assert(tracking);

        if (previousLocation != null)
        {
            // add to the total distanceTraveled
            distanceTraveled += location.distanceTo(previousLocation);
        }

        route.add(location);
        previousLocation = location;
    }

    /**
     * Returns the route travelled.
     */
    public List getRoute() {
        return route;
    }

}
