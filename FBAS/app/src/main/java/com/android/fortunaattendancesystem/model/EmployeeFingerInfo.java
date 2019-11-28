package com.android.fortunaattendancesystem.model;

public class EmployeeFingerInfo {

    private int fingerId;
    private String templateSrNo;
    private String fingerIndex;
    private String fingerHexData;
    private String fingerQuality;
    private String securityLevel;
    private String verificationMode;

    public String getFingerIndex() {
        return fingerIndex;
    }

    public void setFingerIndex(String fingerIndex) {
        this.fingerIndex = fingerIndex;
    }

    public String getFingerHexData() {
        return fingerHexData;
    }

    public void setFingerHexData(String fingerHexData) {
        this.fingerHexData = fingerHexData;
    }

    public String getFingerQuality() {
        return fingerQuality;
    }

    public void setFingerQuality(String fingerQuality) {
        this.fingerQuality = fingerQuality;
    }

    public int getFingerId() {
        return fingerId;
    }

    public void setFingerId(int fingerId) {
        this.fingerId = fingerId;
    }

    public String getTemplateSrNo() {
        return templateSrNo;
    }

    public void setTemplateSrNo(String templateSrNo) {
        this.templateSrNo = templateSrNo;
    }


    public String getSecurityLevel() {
        return securityLevel;
    }

    public void setSecurityLevel(String securityLevel) {
        this.securityLevel = securityLevel;
    }

    public String getVerificationMode() {
        return verificationMode;
    }

    public void setVerificationMode(String verificationMode) {
        this.verificationMode = verificationMode;
    }
}
