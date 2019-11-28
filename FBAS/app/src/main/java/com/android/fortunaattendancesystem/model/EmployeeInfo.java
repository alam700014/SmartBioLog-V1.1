package com.android.fortunaattendancesystem.model;

/**
 * Created by fortuna on 19/9/18.
 */

public class EmployeeInfo {

    private String empId;
    private String cardId;
    private String empName;
    private byte[] photo;

    private String firstFingerIndex;
    private String firstFingerTemplate;

    private String secondFingerIndex;
    private String secondFingerTemplate;

    private int noOfTemplates;

    public int getNoOfTemplates() {
        return noOfTemplates;
    }

    public void setNoOfTemplates(int noOfTemplates) {
        this.noOfTemplates = noOfTemplates;
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

    public byte[] getPhoto() {
        return photo;
    }

    public void setPhoto(byte[] photo) {
        this.photo = photo;
    }

    public String getFirstFingerIndex() {
        return firstFingerIndex;
    }

    public void setFirstFingerIndex(String firstFingerIndex) {
        this.firstFingerIndex = firstFingerIndex;
    }

    public String getFirstFingerTemplate() {
        return firstFingerTemplate;
    }

    public void setFirstFingerTemplate(String firstFingerTemplate) {
        this.firstFingerTemplate = firstFingerTemplate;
    }

    public String getSecondFingerIndex() {
        return secondFingerIndex;
    }

    public void setSecondFingerIndex(String secondFingerIndex) {
        this.secondFingerIndex = secondFingerIndex;
    }

    public String getSecondFingerTemplate() {
        return secondFingerTemplate;
    }

    public void setSecondFingerTemplate(String secondFingerTemplate) {
        this.secondFingerTemplate = secondFingerTemplate;
    }
}
