package com.pedalportland.routetracker;

import android.preference.PreferenceActivity;

import java.util.List;

/**
 * Created by Robin on 1/12/14.
 */
public class UserPreferenceActivity extends PreferenceActivity {

    @Override
    public void onBuildHeaders(List<Header> target){
        loadHeadersFromResource(R.xml.userpreferenceheaders, target);
    }

}
