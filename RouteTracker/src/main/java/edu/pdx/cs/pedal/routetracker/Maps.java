package edu.pdx.cs.pedal.routetracker;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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
 * Created by Minh Vu on 3/10/14.
 */
public class Maps extends Activity {
    GoogleMap map;
    MyApplication myApp;
    private RouteCalculator routeTrip = new RouteCalculator();
    private List<LatLng> trip;
    LatLngBounds.Builder builder = new LatLngBounds.Builder();
    String rideId;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Intent intent = getIntent();

        rideId = intent.getStringExtra("rideId");
        if (rideId != null && null != (myApp = MyApplication.getInstance())) {
            routeTrip = MyApplication.getInstance().getDataLayer().getRide(rideId);
            setTrip(routeTrip.getRoute());

        } else {
            trip = routeTrip.getTrip_unclipped();
        }
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

    /* Creates the menu items */
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, R.string.menu_track);
        menu.add(0, 1, 0, R.string.menu_rideList);
        return true;
    }

    /* Handles item selections */
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        switch (item.getItemId()) {
            case 0:
                i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
                return true;
            case 1:
                i = new Intent(getApplicationContext(), RideListActivity.class);
                startActivity(i);
                return true;
        }
        return false;
    }
}