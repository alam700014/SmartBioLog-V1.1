package com.android.fortunaattendancesystem.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by fortuna on 24/9/18.
 */

public class EmployeeEnrollInfo implements Parcelable {

    private int enrollmentNo;
    private String empId;
    private String cardId;
    private String empName;
    private String aadhaarId;
    private String mobileNo;
    private String bloodGroup;
    private String emailId;
    private String validUpto;
    private String dateOfBirth;
    private String pin;
    private String groupId;
    private String groupName;
    private String siteCode;
    private String siteName;
    private String trainingCenterId;
    private String trainingCenterName;
    private String batchId;
    private String batchName;
    private boolean isFingerEnrolled;
    private int noOfFingersEnrolled;
    private String verificationMode;
    private String smartCardVer;
    private int isBlackListed ;//0=Not BlackListed,1=Blacklisted
    private int isAccessRightEnabled ;//0=Access Right Not Enabled,1=Access Right Enabled
    private int isLockOpen ;//0=Lock Not Open,1=Lock Open
    private String CSN;
    byte[] photo;
    int dbStatus;
    private String enrollSource;
    private String jobCode;

    private boolean isRemoteEnroll;

    protected EmployeeEnrollInfo(Parcel in) {
        enrollmentNo = in.readInt();
        empId = in.readString();
        cardId = in.readString();
        empName = in.readString();
        aadhaarId = in.readString();
        mobileNo = in.readString();
        bloodGroup = in.readString();
        emailId = in.readString();
        validUpto = in.readString();
        dateOfBirth = in.readString();
        pin = in.readString();
        groupId = in.readString();
        groupName = in.readString();
        siteCode = in.readString();
        siteName = in.readString();
        trainingCenterId = in.readString();
        trainingCenterName = in.readString();
        batchId = in.readString();
        batchName = in.readString();
        isFingerEnrolled = in.readByte() != 0;
        noOfFingersEnrolled = in.readInt();
        verificationMode = in.readString();
        smartCardVer = in.readString();
        isBlackListed = in.readInt();
        isAccessRightEnabled = in.readInt();
        isLockOpen = in.readInt();
        CSN = in.readString();
        photo = in.createByteArray();
        dbStatus = in.readInt();
        enrollSource = in.readString();
        jobCode = in.readString();
        isRemoteEnroll = in.readByte() != 0;
    }

    public static final Creator<EmployeeEnrollInfo> CREATOR = new Creator<EmployeeEnrollInfo>() {
        @Override
        public EmployeeEnrollInfo createFromParcel(Parcel in) {
            return new EmployeeEnrollInfo(in);
        }

        @Override
        public EmployeeEnrollInfo[] newArray(int size) {
            return new EmployeeEnrollInfo[size];
        }
    };

    public boolean isRemoteEnroll() {
        return isRemoteEnroll;
    }

    public void setRemoteEnroll(boolean remoteEnroll) {
        isRemoteEnroll = remoteEnroll;
    }

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



    public int getDbStatus() {
        return dbStatus;
    }

    public void setDbStatus(int dbStatus) {
        this.dbStatus = dbStatus;
    }

    public int getEnrollmentNo() {
        return enrollmentNo;
    }

    public void setEnrollmentNo(int enrollmentNo) {
        this.enrollmentNo = enrollmentNo;
    }

    public String getCSN() {
        return CSN;
    }

    public void setCSN(String CSN) {
        this.CSN = CSN;
    }

    public byte[] getPhoto() {
        return photo;
    }

    public void setPhoto(byte[] photo) {
        this.photo = photo;
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

    public String getAadhaarId() {
        return aadhaarId;
    }

    public void setAadhaarId(String aadhaarId) {
        this.aadhaarId = aadhaarId;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getValidUpto() {
        return validUpto;
    }

    public void setValidUpto(String validUpto) {
        this.validUpto = validUpto;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getPin() {
        return pin;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getTrainingCenterName() {
        return trainingCenterName;
    }

    public void setTrainingCenterName(String trainingCenterName) {
        this.trainingCenterName = trainingCenterName;
    }

    public String getBatchName() {
        return batchName;
    }

    public void setBatchName(String batchName) {
        this.batchName = batchName;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getTrainingCenterId() {
        return trainingCenterId;
    }

    public void setTrainingCenterId(String trainingCenterId) {
        this.trainingCenterId = trainingCenterId;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public String getSiteCode() {
        return siteCode;
    }

    public void setSiteCode(String siteCode) {
        this.siteCode = siteCode;
    }

    public boolean isFingerEnrolled() {
        return isFingerEnrolled;
    }

    public void setFingerEnrolled(boolean fingerEnrolled) {
        isFingerEnrolled = fingerEnrolled;
    }

    public int getNoOfFingersEnrolled() {
        return noOfFingersEnrolled;
    }

    public void setNoOfFingersEnrolled(int noOfFingersEnrolled) {
        this.noOfFingersEnrolled = noOfFingersEnrolled;
    }
    public String getVerificationMode() {
        return verificationMode;
    }

    public void setVerificationMode(String verificationMode) {
        this.verificationMode = verificationMode;
    }

    public String getSmartCardVer() {
        return smartCardVer;
    }

    public void setSmartCardVer(String smartCardVer) {
        this.smartCardVer = smartCardVer;
    }

    public int getIsBlackListed() {
        return isBlackListed;
    }

    public void setIsBlackListed(int isBlackListed) {
        this.isBlackListed = isBlackListed;
    }

    public int getIsAccessRightEnabled() {
        return isAccessRightEnabled;
    }

    public void setIsAccessRightEnabled(int isAccessRightEnabled) {
        this.isAccessRightEnabled = isAccessRightEnabled;
    }
    public int getIsLockOpen() {
        return isLockOpen;
    }

    public void setIsLockOpen(int isLockOpen) {
        this.isLockOpen = isLockOpen;
    }

    public EmployeeEnrollInfo() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(enrollmentNo);
        parcel.writeString(empId);
        parcel.writeString(cardId);
        parcel.writeString(empName);
        parcel.writeString(aadhaarId);
        parcel.writeString(mobileNo);
        parcel.writeString(bloodGroup);
        parcel.writeString(emailId);
        parcel.writeString(validUpto);
        parcel.writeString(dateOfBirth);
        parcel.writeString(pin);
        parcel.writeString(groupId);
        parcel.writeString(groupName);
        parcel.writeString(siteCode);
        parcel.writeString(siteName);
        parcel.writeString(trainingCenterId);
        parcel.writeString(trainingCenterName);
        parcel.writeString(batchId);
        parcel.writeString(batchName);
        parcel.writeByte((byte) (isFingerEnrolled ? 1 : 0));
        parcel.writeInt(noOfFingersEnrolled);
        parcel.writeString(verificationMode);
        parcel.writeString(smartCardVer);
        parcel.writeInt(isBlackListed);
        parcel.writeInt(isAccessRightEnabled);
        parcel.writeInt(isLockOpen);
        parcel.writeString(CSN);
        parcel.writeByteArray(photo);
        parcel.writeInt(dbStatus);
        parcel.writeString(enrollSource);
        parcel.writeString(jobCode);
        parcel.writeByte((byte) (isRemoteEnroll ? 1 : 0));
    }
}
