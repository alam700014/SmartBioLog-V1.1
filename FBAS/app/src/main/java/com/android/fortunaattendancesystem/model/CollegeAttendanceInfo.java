package com.android.fortunaattendancesystem.model;

/**
 * Created by fortuna on 19/7/19.
 */

public class CollegeAttendanceInfo {

    private int attendanceId;
    private String eid_p;
    private String eid_s;
    private String sc;
    private String st;
    private String period;
    private String inOutMode;
    private String latitude;
    private String longitude;
    private String punchDate;
    private String punchTime;

    public int getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(int attendanceId) {
        this.attendanceId = attendanceId;
    }

    public String getEid_p() {
        return eid_p;
    }

    public void setEid_p(String eid_p) {
        this.eid_p = eid_p;
    }

    public String getEid_s() {
        return eid_s;
    }

    public void setEid_s(String eid_s) {
        this.eid_s = eid_s;
    }

    public String getSc() {
        return sc;
    }

    public void setSc(String sc) {
        this.sc = sc;
    }

    public String getSt() {
        return st;
    }

    public void setSt(String st) {
        this.st = st;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getInOutMode() {
        return inOutMode;
    }

    public void setInOutMode(String inOutMode) {
        this.inOutMode = inOutMode;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
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
