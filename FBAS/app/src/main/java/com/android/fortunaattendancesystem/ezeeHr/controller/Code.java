package com.android.fortunaattendancesystem.ezeeHr.controller;

/**
 * Created by fortuna on 30/7/18.
 */
public enum Code {
    ATTENDANCE_POST (1000),
    DEVICE_REG (1000),
    TEMPLATE_UPLOAD (2100),
    TEMPLATE_DOWNLOAD (2200),
    EMP_VALIDATION_DOWNLOAD (1700),
    REMORT_ENROLL (2300);

    int val;
    Code(int i) {
        this.val = i;
    }

    public int getValue(){
        return this.val;
    }
}
