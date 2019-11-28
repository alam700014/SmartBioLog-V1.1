package com.android.fortunaattendancesystem.singleton;

import com.acpl.access_computech_fm220_sdk.acpl_FM220_SDK;

/**
 * Created by fortuna on 4/12/18.
 */

public class StarkTekConnection {

    private static StarkTekConnection mInstance = null;
    private acpl_FM220_SDK FM220SDK;

    public static StarkTekConnection getInstance() {
        if (mInstance == null) {
            mInstance = new StarkTekConnection();
            mInstance.reset();
        }
        return mInstance;
    }

    public void setFM220SDK(acpl_FM220_SDK FM220SDK) {
        this.FM220SDK = FM220SDK;
    }

    public acpl_FM220_SDK getFM220SDK() {
        return this.FM220SDK;
    }

    public void reset() {
        FM220SDK = null;
    }
}
