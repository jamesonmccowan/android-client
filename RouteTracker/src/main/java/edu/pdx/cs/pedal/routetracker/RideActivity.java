package edu.pdx.cs.pedal.routetracker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import edu.pdx.cs.pedal.routetracker.R;

public class RideActivity extends Activity {
    RideInfo ride;
    /**
     * Called when the activity is first created.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride);
        Button delete = (Button) findViewById(R.id.delete);
        Intent i = getIntent();
        MyApplication myApp;

        if (null != (myApp = MyApplication.getInstance())) {
            ride = myApp.getDataLayer().getRideInfos().get(i.getIntExtra("index", 0));
        } else {
            ride = null;
        }

        DecimalFormat oneDForm = new DecimalFormat("#.#");
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        Format formatter = new SimpleDateFormat( ":mm:ss" );

        if (ride.getDate() != null) {
            ((TextView) findViewById(R.id.date)).setText(ride.getDate().toString("MM/dd/yyyy' at 'h:mma"));
        } else {
            ((TextView) findViewById(R.id.date)).setText("Date Unknown");
        }
        ((TextView) findViewById(R.id.distance)).setText("Distance: " + ride.getDistanceMI());
        double time = ride.getTime();
        ((TextView) findViewById(R.id.time)    ).setText("Time: " + ((long) time) + formatter.format(new Date((long) (time * 1000 * 60 * 60))));
        ((TextView) findViewById(R.id.avSpeed) ).setText("Average Speed: " + twoDForm.format(ride.getSpeedMI()) +" MPH");
        ((TextView) findViewById(R.id.food)    ).setText("Donuts burned off: " + oneDForm.format((time * 654) / 224));

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ride != null)
                    ride.removeFromDisk();
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
            }
        });
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
