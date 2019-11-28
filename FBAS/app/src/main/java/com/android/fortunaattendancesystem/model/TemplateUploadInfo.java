package com.android.fortunaattendancesystem.model;

/**
 * Created by fortuna on 1/11/18.
 */

public class TemplateUploadInfo {

    private String empId;
    private String cardId;
    private String empName;
    private String fingerId;
    private String templateSrNo;

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

    public String getFingerId() {
        return fingerId;
    }

    public void setFingerId(String fingerId) {
        this.fingerId = fingerId;
    }

    public String getTemplateSrNo() {
        return templateSrNo;
    }

    public void setTemplateSrNo(String templateSrNo) {
        this.templateSrNo = templateSrNo;
    }

    public String getFingerIndex() {
        return fingerIndex;
    }

    public void setFingerIndex(String fingerIndex) {
        this.fingerIndex = fingerIndex;
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

    public String getFingerQuality() {
        return fingerQuality;
    }

    public void setFingerQuality(String fingerQuality) {
        this.fingerQuality = fingerQuality;
    }

    public String getDtoe() {
        return dtoe;
    }

    public void setDtoe(String dtoe) {
        this.dtoe = dtoe;
    }

    public String getFmd() {
        return fmd;
    }

    public void setFmd(String fmd) {
        this.fmd = fmd;
    }

    public byte[] getFid() {
        return fid;
    }

    public void setFid(byte[] fid) {
        this.fid = fid;
    }

    private String fingerIndex;
    private String securityLevel;
    private String verificationMode;
    private String fingerQuality;
    private String dtoe;
    private String fmd;
    private byte[] fid;
}
