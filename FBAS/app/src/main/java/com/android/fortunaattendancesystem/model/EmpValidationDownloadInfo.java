package com.android.fortunaattendancesystem.model;

/**
 * Created by fortuna on 31/10/18.
 */

public class EmpValidationDownloadInfo {

    private int enrollmentNo;

    private String empId;
    private String cardId;
    private String empName;
    private String bloodGrp;
    private String siteCode;

    public int getEnrollmentNo() {
        return enrollmentNo;
    }

    public void setEnrollmentNo(int enrollmentNo) {
        this.enrollmentNo = enrollmentNo;
    }

    private String mobileNo;
    private String emailId;
    private String pin;
    private String dob;
    private String dov;
    private String isBlackListed;

    private String jobCode;

    public String getJobCode() {
        return jobCode;
    }

    public void setJobCode(String jobCode) {
        this.jobCode = jobCode;
    }

    public String getEnrollSource() {
        return enrollSource;
    }

    public void setEnrollSource(String enrollSource) {
        this.enrollSource = enrollSource;
    }

    private String enrollSource;

    public int getDbStatus() {
        return dbStatus;
    }

    public void setDbStatus(int dbStatus) {
        this.dbStatus = dbStatus;
    }

    private String isLockOpen;

    private int dbStatus;

    private String ppl;

    public String getPpl() {
        return ppl;
    }

    public void setPpl(String ppl) {
        this.ppl = ppl;
    }

    public String getPp() {
        return pp;
    }

    public void setPp(String pp) {
        this.pp = pp;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    private String pp;
    private String jobId;
    private String pid;

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

    public String getBloodGrp() {
        return bloodGrp;
    }

    public void setBloodGrp(String bloodGrp) {
        this.bloodGrp = bloodGrp;
    }

    public String getSiteCode() {
        return siteCode;
    }

    public void setSiteCode(String siteCode) {
        this.siteCode = siteCode;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
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

    public String getIsBlackListed() {
        return isBlackListed;
    }

    public void setIsBlackListed(String isBlackListed) {
        this.isBlackListed = isBlackListed;
    }

    public String getIsLockOpen() {
        return isLockOpen;
    }

    public void setIsLockOpen(String isLockOpen) {
        this.isLockOpen = isLockOpen;
    }
}
