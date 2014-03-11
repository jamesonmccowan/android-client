package edu.pdx.cs.pedal.routetracker;

import android.util.Log;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Robin on 3/6/14.
 */
public class UrlInfo {

    private static final String MODULE_TAG = "UrlInfo";
    private static final String JSON_VERSION_0_0 = "0.0";
    private static final String JSON_FIELD_VERSION = "version";
    private static final String JSON_FIELD_RIDE_URL = "RideURL";

    private final String name;
    private final String dirName;

    private String version;
    private String url;

    /**
     *
     * @param text
     * @return
     */
    public static String Parse(String text) {

        String url = null;

        try {
            // Parse JSON data
            JSONObject jsonObject;
            JSONParser parser = new JSONParser();
            jsonObject = (JSONObject) parser.parse(text);

            if (jsonObject.containsKey(JSON_FIELD_RIDE_URL)) {
                url = (String) jsonObject.get(JSON_FIELD_RIDE_URL);
            }
        }
        catch (Exception ex) {
            Log.d(MODULE_TAG, ex.getMessage());
        }
        return url;
    }

    /**
     *
     * @param dirName
     * @param name
     */
    public UrlInfo(String dirName, String name) {
        this.dirName = dirName;
        this.name = name;
    }

    public UrlInfo(String dirName, String name, String json) {
        this.dirName = dirName;
        this.name = name;
        this.url = UrlInfo.Parse(json);
    }

    public String getUrl() {
        return url;
    }

    /**
     * Writes 'result' to the file specified by fileName
     */
    public void saveToDisk() {

        File file;
        FileWriter fw = null;

        // Find the file
        try {
            if (null != (file = new File(dirName, name))) {
                if (null != (fw = new FileWriter(file))) {
                    fw.write(toJSON());
                }
            }
        }
        catch (java.io.IOException ex) {
            Log.d(MODULE_TAG, ex.getMessage());
        }
        finally {
            // close the reader
            try {
                if (fw != null)
                    fw.close();
            }
            catch(IOException ex) {
                Log.d(MODULE_TAG, ex.getMessage());
            }
        }
    }

    public String toJSON() {
        JSONObject jsonUrl = new JSONObject();
        jsonUrl.put(JSON_FIELD_RIDE_URL, url);
        return jsonUrl.toString();
    }

    public boolean loadFromDisk() {

        File file = new File(dirName, name);
        FileReader fr;
        char buff[] = new char[512];
        StringBuilder sb = new StringBuilder();
        boolean loaded = false;
        int numBytes;

        try {
            // Open the file for reading
            if (null != (fr = new FileReader(file))) {

                try {
                    // Read and append data to buffer until EOF
                    while(-1 != (numBytes = fr.read(buff, 0, 512))) {
                        sb.append(buff, 0, numBytes);
                    }

                    // Close the reader
                    try {
                        fr.close();
                    }
                    catch(IOException ex) {
                        Log.d(MODULE_TAG, ex.getMessage());
                    }

                    // Parse the file
                    try {
                        this.url = UrlInfo.Parse(sb.toString());
                        loaded = true;
                    }
                    catch(Exception ex) {
                        if (!file.delete()) {
                            Log.d(MODULE_TAG, "Could not delete UrlInfo file");
                        }
                    }
                }
                catch(IOException ex) {
                    Log.d(MODULE_TAG, ex.getMessage());
                }
            }
        }
        catch(Exception ex) {
            Log.d(MODULE_TAG, ex.getMessage());
        }
        return loaded;
    }
}