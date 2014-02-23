package com.pedalportland.routetracker;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import java.util.Arrays;

/**
 * @author Minh Vu on 2/18/14.
 */
public class Maps extends Activity {

    public LatLng trip[] = {new LatLng(45.51355928,-122.68487634),
            new LatLng(45.51354346,-122.68515974),
            new LatLng(45.51349448,-122.68542416),
            new LatLng(45.51319437,-122.68564974),
            new LatLng(45.51266601,-122.68383533),
            new LatLng(45.51231659,-122.68241821),
            new LatLng(45.51225868,-122.68231447),
            new LatLng(45.51208805,-122.68217059),
            new LatLng(45.51201439,-122.68208911),
            new LatLng(45.511911370,-122.68197378)};
    public LatLng PORTLANDSU = new LatLng(45.512778, -122.685278);
    public LatLng PORTLANDSQ = new LatLng(45.51887, -122.6793);
    public LatLng portland = new LatLng(45.52, -122.681944);

    /**
     * The desired zoom level, in the range of 2.0 to 21.0.
     * Values below this range are set to 2.0, and values above it are set to 21.0.
     * Increase the value to zoom in.
     * Not all areas have tiles at the largest zoom levels.
     */
    public float zoomLevel = 13;
    int lineWidth = 4;

    GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
                .getMap();
        Marker PortlandSU = map.addMarker(new MarkerOptions().position(PORTLANDSU)
                .title("Portland State University,OR"));
        Marker PioneerSQ = map.addMarker(new MarkerOptions()
                .position(PORTLANDSQ)
                .title("PioneerSQ")
                .snippet("Heart of portland")
                .icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.ic_launcher)));

        map.addPolyline(new PolylineOptions().addAll(Arrays.asList(trip)).width(lineWidth).
                color(Color.BLUE).geodesic(true));
        Arrays.fill(trip, null);

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(portland, zoomLevel));
    }
}
