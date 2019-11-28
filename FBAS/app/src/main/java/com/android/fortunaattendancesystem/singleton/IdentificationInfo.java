package com.android.fortunaattendancesystem.singleton;

/**
 * Created by fortuna on 18/9/18.
 */

public class IdentificationInfo {

    private static IdentificationInfo mInstance = null;
    private String userId="";
    private String firstName="";
    private String lastName="";
    private int identifyValue=-111;
    private int internalError=-111;

    public static IdentificationInfo getInstance() {
        if (mInstance == null) {
            mInstance = new IdentificationInfo();
            mInstance.reset();
        }
        return mInstance;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getInternalError() {
        return internalError;
    }

    public void setInternalError(int internalError) {
        this.internalError = internalError;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getIdentifyValue() {
        return identifyValue;
    }

    public void setIdentifyValue(int identifyValue) {
        this.identifyValue = identifyValue;
    }

    public void reset() {
        userId="";
        firstName="";
        lastName="";
        identifyValue=-111;
        internalError=-111;
    }
}
