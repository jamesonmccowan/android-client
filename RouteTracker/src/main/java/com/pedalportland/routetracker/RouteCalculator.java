/**
 * Class: RouteCalculator
 *
 * Description: This class is used to coalesce route locations, and
 *     calculate total distance traveled and maximum speed.
 */

package com.pedalportland.routetracker;

import android.location.Location;
import java.util.List;
import java.util.ArrayList;

/**
 * Class: RouteCalculator
 *
 * Description: This class is used to coalesce route locations, and
 *     calculate total distance traveled and maximum speed.
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
     * Function: RouteCalculator
     *
     * Description: Class constructor - initializes class variables for collecting
     *     route information and distance and speed calculations.
     */
    public RouteCalculator() {
        route = new ArrayList<Location>();
        this.reset();
    }

    /**
     * Function: reset
     *
     * Description: Initializes class variables for collecting
     *     route information and distance and speed calculations.
     */
    public void reset(){
        tracking = false;
        route.clear();
        previousLocation = null;

        startTime = 0;
        distanceTraveled = 0;
        distanceKM = 0;
        speedKM = 0;
        distanceMI = 0;
        speedMI = 0;
    }

    /**
     * Function: start
     *
     * Description: records starting time of route and signals that
     *     route tracking is in progress.
     */
    public void start(){
        this.reset();
        tracking = true;
        startTime = System.currentTimeMillis(); // get current time
    }

    /**
     * Function: stop
     *
     * Description: Calculates route distance and speed
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
     * Function: getDistanceKM
     *
     * Description: Returns route distance in kilometers
     */
    public double getDistanceKM(){
        return distanceKM;
    }

    /**
     * Function: getDistanceKM
     *
     * Description: Returns route speed in kilometers/hour
     */
    public double getSpeedKM(){
        return speedKM;
    }

    /**
     * Function: getDistanceKM
     *
     * Description: Returns route distance in miles
     */
    public double getDistanceMI(){
        return distanceMI;
    }

    /**
     * Function: getDistanceKM
     *
     * Description: Returns route speed in miles/hour
     */
    public double getSpeedMI(){
        return speedMI;
    }

    /**
     * Function: AddLocation
     *
     * Description: Adds new location to route and increase distance travelled
     */
    public void AddLocation(Location location) {

        if (previousLocation != null)
        {
            // add to the total distanceTraveled
            distanceTraveled += location.distanceTo(previousLocation);
        }

        route.add(location);
        previousLocation = location;
    }

    /**
     * Function: getLocations
     *
     * Description: Returns list of locations constituting travelled route.
     */
    public List getRoute(){
        return route;
    }

}
