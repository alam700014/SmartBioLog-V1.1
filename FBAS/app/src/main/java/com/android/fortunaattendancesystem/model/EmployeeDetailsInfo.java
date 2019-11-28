package com.android.fortunaattendancesystem.model;

/**
 * Created by fortuna on 31/1/19.
 */

public class EmployeeDetailsInfo {

    private String employeeID;
    private String cardID;
    private String employeeName;
    private boolean isFingerEnrolled;


    public String getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(String employeeID) {
        this.employeeID = employeeID;
    }

    public String getCardID() {
        return cardID;
    }

    public void setCardID(String cardID) {
        this.cardID = cardID;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public boolean isFingerEnrolled() {
        return isFingerEnrolled;
    }

    public void setFingerEnrolled(boolean fingerEnrolled) {
        isFingerEnrolled = fingerEnrolled;
    }

}
