package com.android.fortunaattendancesystem.singleton;

import android.app.Activity;

/**
 * Created by fortuna on 4/12/18.
 */

public class ActivityInfo {

    private static ActivityInfo mInstance = null;
    private Activity activity;

    public static ActivityInfo getInstance() {
        if (mInstance == null) {
            mInstance = new ActivityInfo();
        }
        return mInstance;
    }


    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }
}
