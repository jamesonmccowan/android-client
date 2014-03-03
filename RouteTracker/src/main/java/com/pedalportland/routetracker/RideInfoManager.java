package com.pedalportland.routetracker;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is used to maintain persistent ride information data.
 * @author robin5 (Robin Murray)
 * @version 1.0
 * created 3/1/14
 */
public class RideInfoManager {

    private final Map<String, RideInfo> rideInfos = new HashMap<String, RideInfo>();
    private String rideInfoDirName = null;

    /**
     *
     * @param rideInfoDirName
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
                    if (rideInfo.loadFromDisk())
                        rideInfos.put(rideInfo.getName(), rideInfo);
                }
            }
        }
        catch(Exception ex) {
            throw new RideInfoMgrException(ex);
        }
    }

    /**
     *
     * @param ride
     * @param fileName
     * @return
     */
    public RideInfo addRideInfo(RouteCalculator ride, String fileName) {
        RideInfo rideInfo = new RideInfo(ride, rideInfoDirName, fileName);
        rideInfo.saveToDisk();
        rideInfos.put(fileName, rideInfo);
        return rideInfo;
    }

    /**
     *
     * @return
     */
    public List<RideInfo> getRideInfos() {

        // Create List
        List<RideInfo> rideInfoList = new ArrayList<RideInfo>(rideInfos.size());

        for (RideInfo r: rideInfos.values())
            rideInfoList.add(new RideInfo(r));
        return rideInfoList;
    }

    /**
     *
     * @param key
     */
    public void deleteRideInfo(String key) {

        if (rideInfos.containsKey(key)) {
            RideInfo r = rideInfos.remove(key);
            r.removeFromDisk();
        }
    }
}
