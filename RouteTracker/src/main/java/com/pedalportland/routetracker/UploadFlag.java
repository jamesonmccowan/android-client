package com.pedalportland.routetracker;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * This class is used to persist a flag identifying
 * which ride files need to be uploaded.
 * @author robin5 (Robin Murray)
 * @version 1.0
 * created 3/2/14
 */
public class UploadFlag {

    private static String rootDirectoryName = null;
    private final File flagFile;
    private final String rideId;

    /**
     *
     * @param name
     */
    public static void setDirectory(String name) {
        File markersDir = new File(name);
        if (!markersDir.exists())
            if (!markersDir.mkdirs())
                throw new UploadServiceException();
        rootDirectoryName = name;
    }

    /**
     *
     * @return
     */
    public static UploadFlag[] getFlags() {

        File files[] = (new File(rootDirectoryName)).listFiles();

        int numFiles = null == files ? 0 : files.length;


        UploadFlag[] uploadFlags = new UploadFlag[numFiles];
        for (int i = 0; i < numFiles; ++i) {
            uploadFlags[i] = new UploadFlag(files[i]);
        }

        return uploadFlags;
    }

    /**
     *
     * @param rideId
     */
    public UploadFlag(String rideId) {
        this.rideId = rideId;
        this.flagFile = new File(UploadFlag.rootDirectoryName, rideId);
    }

    /**
     *
     * @param file
     */
    public UploadFlag(File file) {
        this.rideId = file.getName();
        this.flagFile = new File(UploadFlag.rootDirectoryName, rideId);
    }

    /**
     * Saves a ride to disk in JSON format.  The name of the file is generated from
     * the hashcode of the ride
     */
    public void save() {

        if (flagFile.exists())
            return;

        FileWriter fw = null;
        // Make marker file
        try {
            if (null != (fw = new FileWriter(flagFile))) {
                fw.write("1");
            }
        }
        catch(IOException ex) {
            // Do nothing
        }
        finally {
            if (null != fw) {
                try {
                    fw.close();
                }
                catch(IOException ex) {
                    // do nothing
                }
            }
        }
    }

    /**
     *
     */
    public void delete() {
        flagFile.delete();
    }

    /**
     *
     * @return
     */
    public String getRideId() {
        return rideId;
    }
}
