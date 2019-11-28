package com.android.fortunaattendancesystem.model;

/**
 * Created by fortuna on 31/10/18.
 */

public class AttendanceInfo {

    private int id;
    private String empId;
    private int imageLen;
    private String latLong;
    private String punchDate;
    private String punchTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    private String imageBase64;

    public String getEmpId() {
        return empId;
    }

    public void setEmpId(String empId) {
        this.empId = empId;
    }

    public int getImageLen() {
        return imageLen;
    }

    public void setImageLen(int imageLen) {
        this.imageLen = imageLen;
    }

    public String getLatLong() {
        return latLong;
    }

    public void setLatLong(String latLong) {
        this.latLong = latLong;
    }

    public String getPunchDate() {
        return punchDate;
    }

    public void setPunchDate(String punchDate) {
        this.punchDate = punchDate;
    }

    public String getPunchTime() {
        return punchTime;
    }

    public void setPunchTime(String punchTime) {
        this.punchTime = punchTime;
    }
}
