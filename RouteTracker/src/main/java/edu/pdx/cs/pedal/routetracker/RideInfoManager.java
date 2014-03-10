package edu.pdx.cs.pedal.routetracker;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class is used to maintain persistent ride information data.
 * @author robin5 (Robin Murray)
 * @version 1.0
 * created 3/1/14
 */
public class RideInfoManager {

    private final Map<String, RideInfo> rideInfos = new TreeMap<String, RideInfo>();
    private String rideInfoDirName = null;

    /**
     * Constructs a new instance of RideInfoManager
     * @param rideInfoDirName The directory where RideInfo files will be maintained
     * @throws RideInfoMgrException
     */
    public RideInfoManager(String rideInfoDirName) {

        File rideInfoDir = new File(rideInfoDirName);
        if (!rideInfoDir.exists())
            if (!rideInfoDir.mkdirs())
                throw new RideInfoMgrException();

        this.rideInfoDirName = rideInfoDirName;
        loadRideInfoDir();
    }

    /**
     *
     * @throws RideInfoMgrException
     */
    private void loadRideInfoDir() {

        String [] fileNames ;
        RideInfo rideInfo;

        try {
            rideInfos.clear();

            if (null != (fileNames = (new File(rideInfoDirName)).list())) {
                for(String fileName: fileNames) {
                    rideInfo = new RideInfo(rideInfoDirName, fileName);
                    if (rideInfo.loadFromDisk()) {
                        rideInfos.put(rideInfo.getRideId(), rideInfo);
                    }
                }
            }
        }
        catch(Exception ex) {
            throw new RideInfoMgrException(ex);
        }
    }

    /**
     * Scans the RideInfo entries for blank url fields, and fills them if they exist
     * @param urlInfoManager The object that manages the URLs returned from the web site
     */
    public void setUrls(UrlInfoManager urlInfoManager) {

        String url;

        for (RideInfo r: rideInfos.values()) {
            if (null == r.getURL()) {
                if (null != (url = urlInfoManager.getUrl(r.getRideId()))) {
                    r.setURL(url);
                    if (r.saveToDisk()) {
                        urlInfoManager.deleteUrl(url);
                    }
                }
            }
        }
    }

    /**
     * Adds a ride information record and persist the data to disk
     * @param ride the ride to get the information from
     * @param fileName the file to persist the data to
     * @return returns a reference to the new information record
     */
    public RideInfo addRideInfo(RouteCalculator ride, String fileName) {
        RideInfo rideInfo = new RideInfo(ride, rideInfoDirName, fileName);
        rideInfo.saveToDisk();
        rideInfos.put(fileName, rideInfo);
        return rideInfo;
    }

    /**
     * Returns the list of all rideInfo records
     * @return Returns the list of all rideInfo records
     */
    public RideInfo[] getRideInfos() {

        int size = rideInfos.values().size();

        RideInfo aRideInfos[] = new RideInfo[size];
        int i = 0;
        for (RideInfo r: rideInfos.values()) {
            aRideInfos[i++] = new RideInfo(r);
        }

        return aRideInfos;
    }

    /**
     * Returns the rideInfo record specified by rideId
     * @return Returns the rideInfo record specified by rideId
     */
    public RideInfo getRideInfo(String rideId) {
        return rideInfos.get(rideId);
    }

    /**
     * Deletes the rideInfo record specified by rideId
     * @param rideId specifies which RideInfo record to remove
     */
    public void deleteRideInfo(String rideId) {

        if (rideInfos.containsKey(rideId)) {
            RideInfo r = rideInfos.remove(rideId);
            r.removeFromDisk();
        }
    }

    /**
     *
     */
    public void deleteAll() {

        String [] fileNames;
        File f;

        try {
            if (null != (fileNames = (new File(rideInfoDirName)).list())) {
                for(String fileName: fileNames) {
                    f = new File(rideInfoDirName, fileName);
                    f.delete();
                }
            }
            rideInfos.clear();
        }
        catch(Exception ex) {
            throw new RideInfoMgrException(ex);
        }
    }
}
