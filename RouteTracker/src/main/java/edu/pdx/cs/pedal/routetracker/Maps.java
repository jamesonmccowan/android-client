package edu.pdx.cs.pedal.routetracker;

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

import java.util.Arrays;
/**
 * Created by Minh Vu on 2/27/14.
 */
public class Maps extends Activity {
    GoogleMap map;
    LatLngBounds.Builder builder = new LatLngBounds.Builder();

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        LatLng trip[] = {new LatLng(45.51355928,-122.68487634),
                new LatLng(45.51354346,-122.68515974),
                new LatLng(45.51349448,-122.68542416),
                new LatLng(45.51319437,-122.68564974),
                new LatLng(45.51266601,-122.68383533),
                new LatLng(45.51231659,-122.68241821),
                new LatLng(45.51225868,-122.68231447),
                new LatLng(45.51208805,-122.68217059),
                new LatLng(45.51201439,-122.68208911),
                new LatLng(45.511911370,-122.68197378),
                new LatLng(45.51324567,-122.68219067),
                new LatLng(45.51346769,-122.68456789),
                new LatLng(45.51201598,-122.68575123),
                new LatLng(45.51689784,-122.68123456)};

        LatLng PORTLANDSQ = new LatLng(45.51355928,-122.68487634);
        LatLng PORTLANDSU = new LatLng(45.511911370,-122.68197378);

        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
                .getMap();

        map.addMarker(new MarkerOptions().position(PORTLANDSU)
                .title("Portland State University,OR"));

        map.addMarker(new MarkerOptions().position(PORTLANDSQ).title("PioneerSQ")
                .snippet("Heart of portland").icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.pedalpdx)));

        map.addPolyline(new PolylineOptions().addAll(Arrays.asList(trip)).width(6).
                color(Color.BLUE).geodesic(true));

        for (int i = 0; i <=trip.length -1; i++)
        {
            builder.include(trip[i]);
        }

        Arrays.fill(trip, null);

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
