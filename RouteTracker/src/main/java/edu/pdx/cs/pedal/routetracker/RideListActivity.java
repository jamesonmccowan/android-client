package edu.pdx.cs.pedal.routetracker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import edu.pdx.cs.pedal.routetracker.R;

import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static java.lang.Math.round;

public class RideListActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_list);
        ListView lv = (ListView)findViewById(R.id.listview);

        // create the grid item mapping
        String[] from = new String[] {"date", "distance", "time", "food"};
        int[] to = new int[] { R.id.date, R.id.distance, R.id.time, R.id.food };

        MyApplication myApp = null;
        List<RideInfo> rides = null;

        if (null != (myApp = MyApplication.getInstance())) {
            rides = myApp.getDataLayer().getRideInfos();
        }

        // prepare the list of all records
        List<HashMap<String, String>> fillMaps = new ArrayList<HashMap<String, String>>();
        if (rides != null) {
            DecimalFormat oneDForm = new DecimalFormat("#.#");
            DecimalFormat twoDForm = new DecimalFormat("#.##");
            Format formatter = new SimpleDateFormat( ":mm:ss" );
            double time;

            for(int i = 0; i < rides.size(); i++){
                HashMap<String, String> map = new HashMap<String, String>();
                if (rides.get(i).getDate() != null) {
                    map.put("date", rides.get(i).getDate().toString("MM/dd/yyyy' @ 'h:mma"));
                } else {
                    map.put("date", "Date Unknown");
                }
                map.put("distance", twoDForm.format(rides.get(i).getDistanceMI()) + " Miles");
                time = rides.get(i).getTime();
                map.put("time", "" + ((long) time) + formatter.format(new Date((long) (time * 1000 * 60 * 60))));
                map.put("food", " x " + oneDForm.format((time * 654) / 224)); //calories per hour ~= 654, donut 224 Cal
                fillMaps.add(map);
            }

            // fill in the grid_item layout
            SimpleAdapter adapter = new SimpleAdapter(this, fillMaps, R.layout.fragment_ride_list, from, to);
            lv.setAdapter(adapter);

            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    // position is like array index
                    // Start your Activity according to the item just clicked.
                    Intent i = new Intent(getApplicationContext(), RideActivity.class);
                    i.putExtra("index", position);
                    startActivity(i);
                }
            });
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "Ride Tracker");
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
        }
        return false;
    }
}
