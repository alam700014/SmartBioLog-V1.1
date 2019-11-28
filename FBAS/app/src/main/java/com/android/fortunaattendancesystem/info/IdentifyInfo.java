// The present software is not subject to the US Export Administration Regulations (no exportation license required), May 2012
package com.android.fortunaattendancesystem.info;

public class IdentifyInfo extends MorphoInfo {
    private static IdentifyInfo mInstance = null;

    public static IdentifyInfo getInstance() {
        if (mInstance == null) {
            mInstance = new IdentifyInfo();
            mInstance.reset();
        }
        return mInstance;
    }

    private IdentifyInfo() {
    }

    public String toString() {
        return "IndentifyInfo : No Data Stored";
    }

    public void reset() {
    }

}
