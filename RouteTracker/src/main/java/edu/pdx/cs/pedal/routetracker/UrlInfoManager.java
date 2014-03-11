package edu.pdx.cs.pedal.routetracker;

import android.util.Log;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * This class manages UrlInfo files that were generated from the response from the API
 * and keeps a list of URLs that have not been recorded into the RideInfo files
 * @author robin5 (Robin Murray)
 * @version 1.0
 * created 3/5/14
 */
public class UrlInfoManager {

    private static final String MODULE_TAG = "UrlInfoManager";
    private final Map<String, UrlInfo> urlInfos = new HashMap<String, UrlInfo>();
    private String urlDirName = null;

    /**
     * Creates an instance of the UrlInfoManager
     * @param urlDirName Directory where UrlInfo files are kept
     * @throws RideInfoMgrException
     */
    public UrlInfoManager(String urlDirName) {

        File urlDir = new File(urlDirName);
        if (!urlDir.exists())
            if (!urlDir.mkdirs())
                throw new RideInfoMgrException();

        this.urlDirName = urlDirName;
        loadUrls();
    }

    /**
     * Loads all of the UrlInfo entries
     * @throws RideInfoMgrException
     */
    private void loadUrls() {

        String [] fileNames ;
        UrlInfo urlInfo;

        try {
            urlInfos.clear();

            if (null != (fileNames = (new File(urlDirName)).list())) {
                for(String rideId: fileNames) {
                    urlInfo = new UrlInfo(urlDirName, rideId);
                    if (urlInfo.loadFromDisk()) {
                        urlInfos.put(rideId, urlInfo);
                    }
                }
            }
        }
        catch(Exception ex) {
            throw new RideInfoMgrException(ex);
        }
    }

    /**
     * Returns the Url associated with rideId
     * @return Returns the Url associated with rideId
     */
    public String getUrl(String rideId) {

        if (urlInfos.containsKey(rideId)) {
            return urlInfos.get(rideId).getUrl();
        }
        return null;
    }

    /**
     * Deletes a UrlInfo entry (and backing persistent file).
     * @param rideId The file to delete
     */
    public void deleteUrl(String rideId) {
        File file;
        if (urlInfos.containsKey(rideId)) {
            urlInfos.remove(rideId);
            try {
                file = new File(urlDirName, rideId);
                if (!file.delete()) {
                    Log.d(MODULE_TAG, "Could not delete UrlInfo file");
                }
            }
            catch (NullPointerException ex) {
                Log.d(MODULE_TAG, ex.getMessage());
            }
        }
    }

    /**
     * Deletes all of the UrlInfo entries (and backing persistent files) in the UrlInfo directory
     */
    public void deleteAll() {
        String [] fileNames;
        File f;

        try {
            if (null != (fileNames = (new File(urlDirName)).list())) {
                for(String fileName: fileNames) {
                    f = new File(urlDirName, fileName);
                    if (!f.delete())
                        Log.d(MODULE_TAG, "Could not delete UrlInfo file: " + fileName);
                }
            }
            urlInfos.clear();
        }
        catch(Exception ex) {
            throw new RideInfoMgrException(ex);
        }
    }
}