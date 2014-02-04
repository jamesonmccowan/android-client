package com.pedalportland.routetracker;

import android.location.Location;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * This class collects uploads route data to the portland observatory
 * @author Robin Murray
 * @version 1.0
 */
public class DataUploader extends Thread {

    final private String url;
    final private String version;
    private List<Location> locations;

    /**
     * Instantiates an instance of a <code>DataUploader</code>
     */
    public DataUploader(String pdxObservatory) {
        url = pdxObservatory;
        version = "0.3";
    }

    /**
     * Uploads the data to the observatory
     * @param locations The route data.
     */
    public void UploadData(List<Location> locations) {
        this.locations = locations;
        this.start();
    }

    private String toJSON(List<Location> locations) {
        JSONArray points = new JSONArray();
        //TimeZone tz = TimeZone.getDefault();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"); // ISO 8601
        //df.setTimeZone(tz);

        for(Location point : locations) {
            JSONObject obj = new JSONObject();
            obj.put("time", df.format(new Date(point.getTime()))); // convert from UTC time, in milliseconds since January 1, 1970 to ISO 8601
            obj.put("latitude", point.getLatitude());
            obj.put("longitude", point.getLongitude());
            obj.put("accuracy", point.getAccuracy());
            points.add(obj);
        }
        JSONObject route = new JSONObject();
        route.put("points", points);
        route.put("version", version);
        route.put("id", locations.hashCode());

        return route.toString();
    }

    @Override
    public void run() {
        while(locations != null) {
            HttpURLConnection connection;
            OutputStreamWriter request;

            URL url;
            String response;
            String json = toJSON(locations);
            String parameters = "json="+json;

            try
            {
                url = new URL(this.url);
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setRequestMethod("POST");

                request = new OutputStreamWriter(connection.getOutputStream());
                request.write(parameters);
                request.flush();
                request.close();
                String line;
                InputStreamReader isr = new InputStreamReader(connection.getInputStream());
                BufferedReader reader = new BufferedReader(isr);
                StringBuilder sb = new StringBuilder();
                sb.append(this.url).append("\n");
                while ((line = reader.readLine()) != null)
                {
                    sb.append(line).append("\n");
                }
                // Response from server after login process will be stored in response variable.
                response = sb.toString();

                isr.close();
                reader.close();
                locations = null;
            }
            catch(Exception ex) {
                try {
                    sleep(1000);
                }
                catch ( InterruptedException e ) {

                }
            }
        }
    }
}
