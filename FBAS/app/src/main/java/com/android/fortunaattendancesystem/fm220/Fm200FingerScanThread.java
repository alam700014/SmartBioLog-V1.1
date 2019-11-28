package com.android.fortunaattendancesystem.fm220;

import com.acpl.access_computech_fm220_sdk.acpl_FM220_SDK;

import java.util.TimerTask;

/**
 * Created by suman-dhara on 26/10/17.
 */

public class Fm200FingerScanThread extends TimerTask {

    private acpl_FM220_SDK fm220_sdk;
    private final int NFIQ = 1;

    public Fm200FingerScanThread(acpl_FM220_SDK fm220_sdk) {
        this.fm220_sdk = fm220_sdk;
    }

    /**
     * The task to run should be specified in the implementation of the {@code run()}
     * method.
     */
    @Override
    public void run() {
            this.fm220_sdk.CaptureFM220(this.NFIQ,true,true);

    }
}
