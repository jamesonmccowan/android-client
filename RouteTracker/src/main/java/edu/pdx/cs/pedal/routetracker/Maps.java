package edu.pdx.cs.pedal.routetracker;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Minh Vu on 2/27/14.
 */
public class Maps extends Activity {
    GoogleMap map;
    MyApplication myApp;
    private RouteCalculator routetrip;
    private List<LatLng> trip = new ArrayList<LatLng>();
    LatLngBounds.Builder builder = new LatLngBounds.Builder();
    String rideId;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Intent intent = getIntent();

        rideId = intent.getStringExtra("rideId");
        if (null != (myApp = MyApplication.getInstance()))
            routetrip = MyApplication.getInstance().getDataLayer().getRide(rideId);
        setTrip(routetrip.getRoute());
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

        if (getTrip().size() == 0) {
            return;
        } else {
            map.addMarker(new MarkerOptions().position(getTrip().get(0)).title("This is the start point"));
            map.addMarker(new MarkerOptions().position(getTrip().get(getTrip().size() - 1)).title("This is the end point"));
            map.addPolyline(new PolylineOptions().addAll(getTrip()).width(6).
                    color(Color.BLUE).geodesic(true));

            for (int i = 0; i <=getTrip().size() -1; i++) {
                builder.include(getTrip().get(i));
            }

            trip.clear();

            map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                @Override
                public void onCameraChange(CameraPosition arg0) {
                    // Move camera.
                    map.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 10));
                    // Remove listener to prevent position reset on camera move.
                    map.setOnCameraChangeListener(null);
                }
            });
        }
    }

    public List<LatLng> getTrip() {
        return this.trip;
    }

    public List<LatLng> LocationToLatLng(List<Location> locations) {
        List<LatLng> trip = new ArrayList<LatLng>();
        for(int i=0;i<locations.size();i++) {
            trip.add(new LatLng(locations.get(i).getLatitude(), locations.get(i).getLongitude()));
        }
        return trip;
    }

    public void setTrip(List<Location> Trip) {
        this.trip = LocationToLatLng(Trip);
    }
}
