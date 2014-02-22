/**
 * Created by Minh Vu on 2/20/14.
 */
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
import com.pedalportland.routetracker.R;

import java.util.Arrays;


/**
 * Created by Minh Vu on 2/18/14.
 */
public class Maps extends Activity {

    Double[] position = {45.49216607, -122.79390729, 45.49212721, -122.79416967,
            45.49209675, -122.79431325, 45.49201231, -122.79448298,
            45.49242192, -122.79465708, 45.4927973, -122.79473063};
    LatLng PORTLANDSU = new LatLng(45.49216607, -122.79390729);
    LatLng PORTLANDSQ = new LatLng(45.49212721, -122.79416967);

    int test = 0;
    LatLng[] point = new LatLng[2];

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

        for(int i = 0, y = 1, k = 0; k <= (position.length -3); i+=2, y+=2, k+=1)
        {
            if (test == 0)
            {
                LatLng a = new LatLng(position[i], position[y]);
                point[0] = a;
                test++;
            }
            else if (test == 1)
            {
                LatLng b = new LatLng(position[i], position[y]);
                point[1] = b;
                test = 0;
                map.addPolyline(new PolylineOptions().add(point[0], point[1]).width(2).
                        color(Color.BLUE).geodesic(true));
                Arrays.fill(point, null);
                i-=2;
                y-=2;
            }
        }
        // Zoom in, animating the camera.
        map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
    }
}