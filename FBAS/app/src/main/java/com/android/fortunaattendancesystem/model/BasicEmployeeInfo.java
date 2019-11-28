package com.android.fortunaattendancesystem.model;

/**
 * Created by fortuna on 14/2/18.
 */

public class BasicEmployeeInfo {
    private String EmployeeID;
    private String CardID;
    private String EmployeeName;
    private String EnrolledStatus;
    private String nosFinger;

    public String getNosFinger() {
        return nosFinger;
    }

    public void setNosFinger(String nosFinger) {
        this.nosFinger = nosFinger;
    }

    public BasicEmployeeInfo() {
    }

    public BasicEmployeeInfo(String employeeID, String cardID, String employeeName,String nosFinger) {
        EmployeeID = employeeID;
        CardID = cardID;
        EmployeeName = employeeName;
    }

    public String getEmployeeID() {
        return EmployeeID;
    }

    public void setEmployeeID(String employeeID) {
        EmployeeID = employeeID;
    }

    public String getCardID() {
        return CardID;
    }

    public void setCardID(String cardID) {
        CardID = cardID;
    }

    public String getEmployeeName() {
        return EmployeeName;
    }

    public void setEmployeeName(String employeeName) {
        EmployeeName = employeeName;
    }

    public String getEnrolledStatus() {
        return EnrolledStatus;
    }

    public void setEnrolledStatus(String enrolledStatus) {
        EnrolledStatus = enrolledStatus;
    }

}
