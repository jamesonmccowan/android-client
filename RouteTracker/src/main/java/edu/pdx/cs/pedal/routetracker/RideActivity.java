package edu.pdx.cs.pedal.routetracker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import org.joda.time.DateTime;
import java.text.DecimalFormat;

public class RideActivity extends Activity {
    MyApplication myApp;
    RideInfo ride;
    String rideId;
    /**
     * Called when the activity is first created.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride);
        Button delete = (Button) findViewById(R.id.delete);
        Intent i = getIntent();

        rideId = i.getStringExtra("rideId");
        if (null != (myApp = MyApplication.getInstance())) {
            ride = MyApplication.getInstance().getDataLayer().getRideInfo(rideId);

            DecimalFormat oneDForm = new DecimalFormat("#.#");
            DecimalFormat twoDForm = new DecimalFormat("#.##");

            ((TextView) findViewById(R.id.date)).setText(new DateTime(ride.getStartTime())
                    .toString("MM/dd/yyyy' at 'h:mma"));

            ((TextView) findViewById(R.id.distance)).setText("Distance: " + ride.getDistanceMI());

            long time = ride.getRideTime();
            ((TextView) findViewById(R.id.time)).setText("Time: " + (time / (1000 * 60 * 60))
                    + (new DateTime(time)).toString(":mm:ss"));

            ((TextView) findViewById(R.id.avSpeed)).setText("Average Speed: "
                    + twoDForm.format(ride.getAvgSpeedMPH()) +" MPH");

            ((TextView) findViewById(R.id.mxSpeed)).setText("Max Speed: "
                    + twoDForm.format(ride.getMaxSpeedMPH()) +" MPH");

            ((TextView) findViewById(R.id.food)).setText("Donuts burned off: "
                    + oneDForm.format((time / (1000 * 60 * 60)) / 224));

        } else {
            ((TextView) findViewById(R.id.date)).setText("Error, was unable to display Ride");
        }

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myApp != null)
                    MyApplication.getInstance().getDataLayer().deleteRide(rideId);
                Intent i = new Intent(getApplicationContext(), RideListActivity.class);
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
