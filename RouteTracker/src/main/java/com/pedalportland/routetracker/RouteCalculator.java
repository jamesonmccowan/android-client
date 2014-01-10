package com.pedalportland.routetracker;

import android.location.Location;
import java.util.ArrayList;

/**
 * Created by Robin on 1/9/14.
 */
public class RouteCalculator {

    private boolean _tracking;                                           // whether app is currently tracking
    private long _startTime;                                             // time (in milliseconds) when tracking starts
    private long _distanceTraveled;                                      // total distance the user traveled
    private static final double MILLISECONDS_PER_HOUR = 1000 * 60 * 60; //
    private static final double MILES_PER_KILOMETER = 0.621371192;

    private ArrayList<Location> _locations = null;
    private Location _previousLocation;                                  // previous reported location

    double _distanceKM;
    double _speedKM;
    double _distanceMI;
    double _speedMI;

    public RouteCalculator() {
        _locations = new ArrayList<Location>();
        this.reset();
    }

    public void reset(){
        _tracking = false;
        _locations.clear();
        _previousLocation = null;

        _startTime = 0;
        _distanceTraveled = 0;
        _distanceKM = 0;
        _speedKM = 0;
        _distanceMI = 0;
        _speedMI = 0;
    }

    public void start(){
        this.reset();
        _tracking=true;
        _startTime = System.currentTimeMillis(); // get current time
    }

    public void stop(){

        assert(_tracking);

        // compute the total time we were tracking
        double expiredTime = (System.currentTimeMillis() - _startTime) / MILLISECONDS_PER_HOUR;
        assert(expiredTime >= 0.0);

        _tracking = false;
        _distanceKM = _distanceTraveled / 1000.0;
        _speedKM = _distanceKM / expiredTime;
        _distanceMI = _distanceKM * MILES_PER_KILOMETER;
        _speedMI = _distanceMI / expiredTime;
    }

    public double getDistanceKM(){
        return _distanceKM;
    }

    public double getSpeedKM(){
        return _speedKM;
    }

    public double getDistanceMI(){
        return _distanceMI;
    }

    public double getSpeedMI(){
        return _speedMI;
    }

    public void AddLocation(Location location){

        if (_previousLocation != null)
        {
            // add to the total distanceTraveled
            _distanceTraveled += location.distanceTo(_previousLocation);
        }

        _locations.add(location);
        _previousLocation = location;
    }

    public Object[] getLocations(){
        return _locations.toArray();
    }

}
