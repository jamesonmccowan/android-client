package com.pedalportland.routetracker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

public class Ride extends Activity {
    /**
     * Called when the activity is first created.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride);

        Button delete = (Button) findViewById(R.id.delete);
        //delete.setOnTouchListener();

        Intent i = getIntent();

        ((TextView) findViewById(R.id.date)    ).setText(i.getStringExtra("date") + " at 4:30pm");
        ((TextView) findViewById(R.id.distance)).setText("Distance: " + i.getStringExtra("distance"));
        ((TextView) findViewById(R.id.time)    ).setText("Time: " + i.getStringExtra("time"));
        ((TextView) findViewById(R.id.avSpeed) ).setText("Average Speed: 18 MPH");
        ((TextView) findViewById(R.id.mxSpeed) ).setText("Max Speed: 23 MPH");
        ((TextView) findViewById(R.id.food)    ).setText("Burritos burned off: " + i.getStringExtra("food"));
    }

    /* Creates the menu items */
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "Ride Tracker");
        menu.add(0, 1, 0, "List of Rides");
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
