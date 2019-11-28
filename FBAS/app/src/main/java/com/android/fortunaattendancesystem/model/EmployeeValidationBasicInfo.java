package com.android.fortunaattendancesystem.model;

import java.util.ArrayList;

/**
 * Created by fortuna on 1/4/19.
 */

public class EmployeeValidationBasicInfo  {

    private String jobId;

    private int enrollmentNo;
    private String empId;
    private String cardId;
    private String empName;
    private String empType;
    private String pin;
    private String dob;
    private String dov;
    private String isLockOpen;
    private String pp;
    private String mn;
    private String aid;
    private String bg;
    private String eid;
    private String sc;
    private String isBlackListed;
    private String vm;
    private ArrayList<EmployeeValidationFingerInfo> fInfoList;

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public int getEnrollmentNo() {
        return enrollmentNo;
    }

    public void setEnrollmentNo(int enrollmentNo) {
        this.enrollmentNo = enrollmentNo;
    }

    public String getEmpId() {
        return empId;
    }

    public void setEmpId(String empId) {
        this.empId = empId;
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public String getEmpName() {
        return empName;
    }

    public void setEmpName(String empName) {
        this.empName = empName;
    }

    public String getEmpType() {
        return empType;
    }

    public void setEmpType(String empType) {
        this.empType = empType;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getDov() {
        return dov;
    }

    public void setDov(String dov) {
        this.dov = dov;
    }

    public String getIsLockOpen() {
        return isLockOpen;
    }

    public void setIsLockOpen(String isLockOpen) {
        this.isLockOpen = isLockOpen;
    }

    public String getPp() {
        return pp;
    }

    public void setPp(String pp) {
        this.pp = pp;
    }

    public String getMn() {
        return mn;
    }

    public void setMn(String mn) {
        this.mn = mn;
    }

    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }

    public String getBg() {
        return bg;
    }

    public void setBg(String bg) {
        this.bg = bg;
    }

    public String getEid() {
        return eid;
    }

    public void setEid(String eid) {
        this.eid = eid;
    }

    public String getSc() {
        return sc;
    }

    public void setSc(String sc) {
        this.sc = sc;
    }

    public String getIsBlackListed() {
        return isBlackListed;
    }

    public void setIsBlackListed(String isBlackListed) {
        this.isBlackListed = isBlackListed;
    }

    public String getVm() {
        return vm;
    }

    public void setVm(String vm) {
        this.vm = vm;
    }

    public ArrayList <EmployeeValidationFingerInfo> getfInfoList() {
        return fInfoList;
    }

    public void setfInfoList(ArrayList <EmployeeValidationFingerInfo> fInfoList) {
        this.fInfoList = fInfoList;
    }
}
