package com.android.fortunaattendancesystem.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by fortuna on 3/11/18.
 */


public class RemoteEnrollmentInfo implements Parcelable {

    private String jobId;
    private String packetId;
    private String enrollmentType;
    private String empId;
    private String cardId;
    private String empName;

    public String getPacketId() {
        return packetId;
    }

    public void setPacketId(String packetId) {
        this.packetId = packetId;
    }

    private String dtoe;
    private String fingerType;
    private String fingerIndex;
    private String fingerQuality;
    private String securityLevel;
    private String verificationMode;

    public RemoteEnrollmentInfo(){

    }

    public RemoteEnrollmentInfo(Parcel in) {
        jobId = in.readString();
        packetId=in.readString();
        enrollmentType = in.readString();
        empId = in.readString();
        cardId = in.readString();
        empName = in.readString();
        dtoe = in.readString();
        fingerType = in.readString();
        fingerIndex = in.readString();
        fingerQuality = in.readString();
        securityLevel = in.readString();
        verificationMode = in.readString();
    }

    public static final Creator <RemoteEnrollmentInfo> CREATOR = new Creator <RemoteEnrollmentInfo>() {
        @Override
        public RemoteEnrollmentInfo createFromParcel(Parcel in) {
            return new RemoteEnrollmentInfo(in);
        }

        @Override
        public RemoteEnrollmentInfo[] newArray(int size) {
            return new RemoteEnrollmentInfo[size];
        }
    };

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getEnrollmentType() {
        return enrollmentType;
    }

    public void setEnrollmentType(String enrollmentType) {
        this.enrollmentType = enrollmentType;
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

    public String getDtoe() {
        return dtoe;
    }

    public void setDtoe(String dtoe) {
        this.dtoe = dtoe;
    }

    public String getFingerType() {
        return fingerType;
    }

    public void setFingerType(String fingerType) {
        this.fingerType = fingerType;
    }

    public String getFingerIndex() {
        return fingerIndex;
    }

    public void setFingerIndex(String fingerIndex) {
        this.fingerIndex = fingerIndex;
    }

    public String getFingerQuality() {
        return fingerQuality;
    }

    public void setFingerQuality(String fingerQuality) {
        this.fingerQuality = fingerQuality;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(jobId);
        parcel.writeString(packetId);
        parcel.writeString(enrollmentType);
        parcel.writeString(empId);
        parcel.writeString(cardId);
        parcel.writeString(empName);
        parcel.writeString(dtoe);
        parcel.writeString(fingerType);
        parcel.writeString(fingerIndex);
        parcel.writeString(fingerQuality);
        parcel.writeString(securityLevel);
        parcel.writeString(verificationMode);
    }
}
