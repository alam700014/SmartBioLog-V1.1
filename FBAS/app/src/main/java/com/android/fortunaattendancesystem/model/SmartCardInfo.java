package com.android.fortunaattendancesystem.model;

/**
 * Created by fortuna on 15/9/18.
 */

public class SmartCardInfo {


    private String enrollmentNo;
    private String cardId;
    private String pin;

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public boolean isCardCreatedLocally() {
        return isCardCreatedLocally;
    }

    public void setCardCreatedLocally(boolean cardCreatedLocally) {
        isCardCreatedLocally = cardCreatedLocally;
    }

    private boolean isCardCreatedLocally;

    public String getEnrollmentNo() {
        return enrollmentNo;
    }

    public void setEnrollmentNo(String enrollmentNo) {
        this.enrollmentNo = enrollmentNo;
    }

    private String readCSN;
    private String dbCSN;
    private String madZero;
    private String madOne;

    private String employeeId;
    private String empName;
    private String validUpto;
    private String birthDate;
    private String siteCode;

    private String bloodGroup;
    private String smartCardVer;

    private String firstFingerTemplate;
    private String firstFingerNo;
    private String firstFingerSecurityLevel;
    private String firstFingerIndex;
    private String firstFingerQuality;
    private String firstFingerVerificationMode;

    private String secondFingerTemplate;
    private String secondFingerNo;
    private String secondFingerSecurityLevel;
    private String secondFingerIndex;
    private String secondFingerQuality;
    private String secondFingerVerificationMode;


    private int noOfTemplates;
    private String firstSlot;
    private String secondSlot;

    public int getNoOfTemplates() {
        return noOfTemplates;
    }

    public void setNoOfTemplates(int noOfTemplates) {
        this.noOfTemplates = noOfTemplates;
    }

    private String thirdSlot;
    private String forthSlot;
    private String fifthSlot;
    private String sixthSlot;

    public String getFirstSlot() {
        return firstSlot;
    }

    public void setFirstSlot(String firstSlot) {
        this.firstSlot = firstSlot;
    }

    public String getSecondSlot() {
        return secondSlot;
    }

    public void setSecondSlot(String secondSlot) {
        this.secondSlot = secondSlot;
    }

    public String getThirdSlot() {
        return thirdSlot;
    }

    public void setThirdSlot(String thirdSlot) {
        this.thirdSlot = thirdSlot;
    }

    public String getForthSlot() {
        return forthSlot;
    }

    public void setForthSlot(String forthSlot) {
        this.forthSlot = forthSlot;
    }

    public String getFifthSlot() {
        return fifthSlot;
    }

    public void setFifthSlot(String fifthSlot) {
        this.fifthSlot = fifthSlot;
    }

    public String getSixthSlot() {
        return sixthSlot;
    }

    public void setSixthSlot(String sixthSlot) {
        this.sixthSlot = sixthSlot;
    }

    public String getSeventhSlot() {
        return seventhSlot;
    }

    public void setSeventhSlot(String seventhSlot) {
        this.seventhSlot = seventhSlot;
    }

    public String getEighthSlot() {
        return eighthSlot;
    }

    public void setEighthSlot(String eighthSlot) {
        this.eighthSlot = eighthSlot;
    }

    public String getNinethSlot() {
        return ninethSlot;
    }

    public void setNinethSlot(String ninethSlot) {
        this.ninethSlot = ninethSlot;
    }

    public String getTenthSlot() {
        return tenthSlot;
    }

    public void setTenthSlot(String tenthSlot) {
        this.tenthSlot = tenthSlot;
    }

    public String getEleventhSlot() {
        return eleventhSlot;
    }

    public void setEleventhSlot(String eleventhSlot) {
        this.eleventhSlot = eleventhSlot;
    }

    public String getTwelvethSlot() {
        return twelvethSlot;
    }

    public void setTwelvethSlot(String twelvethSlot) {
        this.twelvethSlot = twelvethSlot;
    }

    private String seventhSlot;
    private String eighthSlot;
    private String ninethSlot;
    private String tenthSlot;
    private String eleventhSlot;
    private String twelvethSlot;




    public String getSecondFingerNo() {
        return secondFingerNo;
    }

    public void setSecondFingerNo(String secondFingerNo) {
        this.secondFingerNo = secondFingerNo;
    }

    public String getSecondFingerSecurityLevel() {
        return secondFingerSecurityLevel;
    }

    public void setSecondFingerSecurityLevel(String secondFingerSecurityLevel) {
        this.secondFingerSecurityLevel = secondFingerSecurityLevel;
    }

    public String getSecondFingerIndex() {
        return secondFingerIndex;
    }

    public void setSecondFingerIndex(String secondFingerIndex) {
        this.secondFingerIndex = secondFingerIndex;
    }

    public String getSecondFingerQuality() {
        return secondFingerQuality;
    }

    public void setSecondFingerQuality(String secondFingerQuality) {
        this.secondFingerQuality = secondFingerQuality;
    }

    public String getSecondFingerVerificationMode() {
        return secondFingerVerificationMode;
    }

    public void setSecondFingerVerificationMode(String secondFingerVerificationMode) {
        this.secondFingerVerificationMode = secondFingerVerificationMode;
    }

    public String getSecondFingerTemplate() {
        return secondFingerTemplate;
    }

    public void setSecondFingerTemplate(String secondFingerTemplate) {
        this.secondFingerTemplate = secondFingerTemplate;
    }

    public String getFirstFingerNo() {
        return firstFingerNo;
    }

    public void setFirstFingerNo(String firstFingerNo) {
        this.firstFingerNo = firstFingerNo;
    }

    public String getFirstFingerSecurityLevel() {
        return firstFingerSecurityLevel;
    }

    public void setFirstFingerSecurityLevel(String firstFingerSecurityLevel) {
        this.firstFingerSecurityLevel = firstFingerSecurityLevel;
    }

    public String getFirstFingerIndex() {
        return firstFingerIndex;
    }

    public void setFirstFingerIndex(String firstFingerIndex) {
        this.firstFingerIndex = firstFingerIndex;
    }

    public String getFirstFingerQuality() {
        return firstFingerQuality;
    }

    public void setFirstFingerQuality(String firstFingerQuality) {
        this.firstFingerQuality = firstFingerQuality;
    }

    public String getFirstFingerVerificationMode() {
        return firstFingerVerificationMode;
    }

    public void setFirstFingerVerificationMode(String firstFingerVerificationMode) {
        this.firstFingerVerificationMode = firstFingerVerificationMode;
    }

    public String getFirstFingerTemplate() {
        return firstFingerTemplate;
    }

    public void setFirstFingerTemplate(String firstFingerTemplate) {
        this.firstFingerTemplate = firstFingerTemplate;
    }




    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmpName() {
        return empName;
    }

    public void setEmpName(String empName) {
        this.empName = empName;
    }

    public String getValidUpto() {
        return validUpto;
    }

    public void setValidUpto(String validUpto) {
        this.validUpto = validUpto;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getSiteCode() {
        return siteCode;
    }

    public void setSiteCode(String siteCode) {
        this.siteCode = siteCode;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getSmartCardVer() {
        return smartCardVer;
    }

    public void setSmartCardVer(String smartCardVer) {
        this.smartCardVer = smartCardVer;
    }

    public String getMadZero() {
        return madZero;
    }

    public void setMadZero(String madZero) {
        this.madZero = madZero;
    }

    public String getMadOne() {
        return madOne;
    }

    public void setMadOne(String madOne) {
        this.madOne = madOne;
    }


    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public String getReadCSN() {
        return readCSN;
    }

    public void setReadCSN(String readCSN) {
        this.readCSN = readCSN;
    }

    public String getDbCSN() {
        return dbCSN;
    }

    public void setDbCSN(String dbCSN) {
        this.dbCSN = dbCSN;
    }
}
