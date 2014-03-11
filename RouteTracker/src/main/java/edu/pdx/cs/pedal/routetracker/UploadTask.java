package edu.pdx.cs.pedal.routetracker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
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
    private static final int MAX_URL_RESPONSE_LENGTH = 1024;
    private static final int MAX_ERROR_RESPONSE_LENGTH = 2048;

    private final String url;
    private final String responseDirName;
    private final String ridesDirName;

    /**
     * Constructs a new Upload Task
     * @param url The URL to write the ride data to
     * @param ridesDirName The directory that contains the ride files
     * @param responseDirName The directory to write the response returned from the upload URL
     */
    public UploadTask(String url, String ridesDirName, String responseDirName) {
        super();
        this.url = url;
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
                else if (null == (json = loadFile(rideFile))) {
                    uploadFlag.delete();
                }
                else if (writeUrl(json, rideId)) {
                    uploadFlag.delete();
                }
            }
            catch(Exception ex) {
                Log.d(MODULE_TAG, ex.getMessage());
            }
        }
    }

    /**
     * Loads a file into a string
     * @param file the file to bring into memory
     * @return the file's data
     */
    private String loadFile(File file) {

        FileReader fr;
        char buff[] = new char[512];
        StringBuilder sb = new StringBuilder();
        String response = null;

        // Find the file
        try {
            fr = new FileReader(file);

            // Read all of the data
            try {
                int numBytes;

                while(-1 != (numBytes = fr.read(buff, 0, 512))) {
                    sb.append(buff, 0, numBytes);
                }
                response = sb.toString();
            }
            catch(IOException ex) {
                Log.d(MODULE_TAG, ex.getMessage());
            }
            finally { // close the reader
                try {
                    fr.close();
                }
                catch(IOException ex) {
                    Log.d(MODULE_TAG, ex.getMessage());
                }
            }
        }
        catch (FileNotFoundException ex) {
            Log.d(MODULE_TAG, ex.getMessage());
        }
        return response;
    }

    /**
     * Posts the JSON formatted data to the URL
     * @param json The data to post
     * @return true if data was posted successfully, false otherwise
     */
    private boolean writeUrl(String json, String fileName) {

        boolean uploaded = false;
        HttpURLConnection connection = null;
        OutputStreamWriter request;
        InputStream inputStream = null;
        InputStream errorStream = null;

        URL url;

        try {
            url = new URL(this.url);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestMethod("POST");

            // Send the request
            request = new OutputStreamWriter(connection.getOutputStream());
            request.write(json);
            request.flush();

            // Note: at this point, we say we are successful even though we have
            // not retrieved the response from the web site. If the data we sent
            // was bad, there is nothing we can do to fix it, so we need to not
            // upload the file any more

            uploaded = true;

            // Get the resulting response
            if (HttpURLConnection.HTTP_CREATED == connection.getResponseCode()) {

                try {
                    // Retrieve the response from the web site
                    String response;
                    UrlInfo urlInfo;

                    inputStream = connection.getInputStream();
                    if (null != (response = getResponse(inputStream, MAX_URL_RESPONSE_LENGTH))) {
                        try {
                            if (null != (urlInfo = new UrlInfo(responseDirName, fileName, response))) {
                                urlInfo.saveToDisk();
                            }
                        }
                        catch(Exception ex) {
                            Log.d(MODULE_TAG, ex.getMessage());
                        }
                    }
                }
                catch(IOException ex) {
                    Log.d(MODULE_TAG, ex.getMessage());
                }
            }
            else {
                try {
                    errorStream = connection.getInputStream();
                    Log.d(MODULE_TAG, getResponse(errorStream, MAX_ERROR_RESPONSE_LENGTH));
                }
                catch(IOException ex) {
                    Log.d(MODULE_TAG, ex.getMessage());
                }
            }
        }
        catch(Exception ex) {
            Log.d(MODULE_TAG, ex.getMessage());
        }
        finally {
            closeStream(inputStream);
            closeStream(errorStream);
            if (null != connection)
                connection.disconnect();
        }
        return uploaded;
    }

    /**
     * Reads the data in an input stream up to max bytes. If more than
     * maxBytes are read, the input is nullified
     * @param inputStream stream to read from
     * @param maxBytes maximum number of bytes to read
     * @return contents of inputStream
     */
    private String getResponse(InputStream inputStream, int maxBytes) {

        InputStreamReader isr = null;
        BufferedReader reader;
        String response = null;
        StringBuilder sb = new StringBuilder();
        char buff[] = new char[512];
        int numBytes;
        int totalBytes = 0;

        try {
            // Obtain stream
            if (null != inputStream) {
                if (null != (isr = new InputStreamReader(inputStream))) {
                    if (null != (reader = new BufferedReader(isr))) {
                        // Read and append data to buffer until EOF
                        while(-1 != (numBytes = reader.read(buff, 0, 512))) {
                            sb.append(buff, 0, numBytes);
                            totalBytes += numBytes;
                            if (totalBytes > maxBytes) {
                                return null;
                            }
                        }
                        response = sb.toString();
                    }
                }
            }
        }
        catch(Exception ex) {
            Log.d(MODULE_TAG, ex.getMessage());
        }
        finally {
            if (null != isr) {
                try {
                    isr.close();
                }
                catch(IOException ex) {
                    Log.d(MODULE_TAG, ex.getMessage());
                }
            }
        }
        return response;
    }

    /**
     * Closes an InputStream
     * @param inputStream the stream to close
     */
    private void closeStream(InputStream inputStream){
        try {
            if (null != inputStream) {
                inputStream.close();
            }
        }
        catch(IOException ex) {
            Log.d(MODULE_TAG, ex.getMessage());
        }
    }
}