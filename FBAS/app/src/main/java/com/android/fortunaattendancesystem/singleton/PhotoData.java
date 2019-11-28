package com.android.fortunaattendancesystem.singleton;

/**
 * Created by fortuna on 3/12/18.
 */

public class PhotoData{
    private static PhotoData mInstance;
    private byte[] mCapturedPhotoData;

    // Singleton code
    public static PhotoData getInstance() {
        if (mInstance == null) {
            mInstance = new PhotoData();
            mInstance.reset();
        }
        return mInstance;
    }

    // Getters & Setters
    public byte[] getCapturedPhotoData() {
        return mCapturedPhotoData;
    }

    public void setCapturedPhotoData(byte[] capturedPhotoData) {
        mCapturedPhotoData = capturedPhotoData;
    }

    private void reset() {
        mCapturedPhotoData=null;
    }
}
