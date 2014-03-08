package com.pedalportland.routetracker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

        // prepare the list of all records
        List<HashMap<String, String>> fillMaps = new ArrayList<HashMap<String, String>>();
        for(int i = 1; i < 10; i++){
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("date", "1/0" + i + "/2014");
            map.put("distance", "" + i + " miles");
            map.put("time", "0:0" + i + ":00");
            map.put("food", " x " + i);
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

                i.putExtra("date", "1/0" + position + "/2014");
                i.putExtra("distance", "" + position + " miles");
                i.putExtra("time", "0:0" + position + ":00");
                i.putExtra("food", "" + position);

                startActivity(i);
            }
        });
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
