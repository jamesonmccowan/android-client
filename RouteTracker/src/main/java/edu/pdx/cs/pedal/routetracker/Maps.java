package edu.pdx.cs.pedal.routetracker;

/**
 * Created by Minh Vu on 3/6/14.
 */
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;


import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

/**
 * Created by Minh Vu on 2/27/14.
 */
public class Maps extends Activity {
    GoogleMap map;
    private RouteCalculator routetrip = new RouteCalculator();
    private ArrayList<LatLng> trip = new ArrayList<LatLng>();
    LatLngBounds.Builder builder = new LatLngBounds.Builder();

    public Maps()
    {
    }

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        setTrip(RouteCalculator.gettrip());
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
                .getMap();

        if (getTrip().size() == 0)
        {
            return;
        }

        else{
            map.addMarker(new MarkerOptions().position(getTrip().get(0))
                    .title("This is the start point"));

            map.addMarker(new MarkerOptions().position(getTrip().get(getTrip().size() - 1))
                    .title("This is the end point"));


            map.addPolyline(new PolylineOptions().addAll(getTrip()).width(6).
                    color(Color.BLUE).geodesic(true));

            for (int i = 0; i <=getTrip().size() -1; i++)
            {
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

    public ArrayList<LatLng> getTrip() {return this.trip;}

    public void setTrip(ArrayList<LatLng> Trip)
    {
        this.trip = Trip;
    }
}
