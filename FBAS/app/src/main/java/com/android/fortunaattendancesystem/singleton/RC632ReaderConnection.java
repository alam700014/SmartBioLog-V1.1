package com.android.fortunaattendancesystem.singleton;

import com.friendlyarm.SmartReader.SmartFinger;

/**
 * Created by fortuna on 23/6/17.
 */
public class RC632ReaderConnection {


    private static RC632ReaderConnection mInstance = null;
    private SmartFinger smartFinger = null;


    public static RC632ReaderConnection getInstance() {
        if (mInstance == null) {
            mInstance = new RC632ReaderConnection();
        }
        return mInstance;
    }

    public void setSmartFinger(SmartFinger smartFinger) {
        this.smartFinger = smartFinger;
    }

    public SmartFinger getSmartFinger() {
        return smartFinger;
    }
}
