package com.pedalportland.routetracker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import android.util.Log;

/**
 * A {@code UploadTask} is a concurrent unit of execution. It has its own call stack
 * for methods being invoked, their arguments and local variables. Each application
 * has at least one thread running when it is started, the main thread, in the main
 * {@link ThreadGroup}. The runtime keeps its own threads in the system thread
 * group.
 *
 * <p>There are two ways to execute code in a new thread.
 * You can either subclass {@code Thread} and overriding its {@link #run()} method,
 * or construct a new {@code Thread} and pass a {@link Runnable} to the constructor.
 * In either case, the {@link #start()} method must be called to actually execute
 * the new {@code Thread}.
 *
 * <p>Each {@code Thread} has an integer priority that affect how the thread is
 * scheduled by the OS. A new thread inherits the priority of its parent.
 * A thread's priority can be set using the {@link #setPriority(int)} method.
 */
public class UploadTask extends Thread {

    private static final String MODULE_TAG = "UploadTask";

    private final String url;
    private final String responseDirName;
    private final String ridesDirName;

    /**
     * Constructs a new UploadTask setting the specified pdx Observatory url and
     * cache directory name.
     * @param pdxObservatory
     * @param ridesDirName
     * @param responseDirName
     */
    public UploadTask(String pdxObservatory, String ridesDirName, String responseDirName) {
        super();
        this.url = pdxObservatory;
        this.ridesDirName = ridesDirName;
        this.responseDirName = responseDirName;
    }

    /**
     * Reads all of the files in the cache directory and attempts
     * to upload them to the url.
     *
     * @see Thread#start
     */
    @Override
    public void run() {

        UploadFlag[] uploadFlags = UploadFlag.getFlags();

        for (UploadFlag uploadFlag : uploadFlags) {
            try {
                String rideId = uploadFlag.getRideId();
                String json;
                File rideFile;

                if (null == (rideFile = new File(ridesDirName, rideId))) {
                    uploadFlag.delete();
                }
                else if (!rideFile.exists()) {
                    uploadFlag.delete();
                }
                else if (null == (json = loadJSON(rideFile))) {
                    uploadFlag.delete();
                }
                else if (writeUrl(json, rideId)) {
                    uploadFlag.delete();
                }
            }
            catch(Exception ex) {
                // Do nothing
            }
        }
    }

    /**
     *
     * @param file
     * @return
     */
    private String loadJSON(File file) {

        FileReader fr;
        char buff[] = new char[512];
        StringBuilder sb = new StringBuilder();

        // Find the file
        try {
            fr = new FileReader(file);
        }
        catch (FileNotFoundException ex) {
            return null;
        }

        // Read all of the data
        try {
            while(-1 != fr.read(buff, 0, 512)) {
                sb.append(buff);
            }
            return sb.toString();
        }
        catch(IOException ex) {
            return null;
        }
        finally {
            // close the reader
            try {
                fr.close();
            }
            catch(IOException ex) {
                // ignore it, we already got the data.
            }
        }
    }

    /**
     *
     * @param json
     * @return
     */
    private boolean writeUrl(String json, String fileName) {

        boolean uploaded = false;
        HttpURLConnection connection;
        OutputStreamWriter request;

        URL url;
        String response;

        try {
            url = new URL(this.url);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestMethod("POST");

            request = new OutputStreamWriter(connection.getOutputStream());
            request.write(json);
            request.flush();
            request.close();

            // Note: we say we are successful at this point (eventhough we have
            // not retrieved the response from the web site so we don't continue
            // to upload the same data

            uploaded = true;

            // Retrieve the response from the web site
            String line;

            InputStream in = connection.getInputStream();
            InputStreamReader isr = new InputStreamReader(in);
            BufferedReader reader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            sb.append(this.url).append("\n");
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            // Response from server after login process will be stored in response file.
            response = sb.toString();
            writeResult(response, fileName);

            isr.close();
            reader.close();
        }
        catch(Exception ex) {
            Log.e("UploadTask", ex.getMessage());
        }
        return uploaded;
    }

    /**
     *
     * @param result
     * @param fileName
     */
    private void writeResult(String result, String fileName) {

        File file;
        FileWriter fw = null;

        // Find the file
        try {
            if (null != (file = new File(responseDirName, fileName))) {
                if (null != (fw = new FileWriter(file))) {
                    fw.write(result);
                }
            }
        }
        catch (java.io.IOException ex) {
            Log.e(MODULE_TAG, ex.getMessage());
        }
        finally {
            // close the reader
            try {
                if (fw != null)
                    fw.close();
            }
            catch(IOException ex) {
                // ignore it, we already got the data.
            }
        }

    }
}
