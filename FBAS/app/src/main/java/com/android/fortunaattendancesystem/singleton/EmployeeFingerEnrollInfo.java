// The present software is not subject to the US Export Administration Regulations (no exportation license required), May 2012
package com.android.fortunaattendancesystem.singleton;

import com.android.fortunaattendancesystem.info.MorphoInfo;
import com.morpho.morphosmart.sdk.CompressionAlgorithm;
import com.morpho.morphosmart.sdk.TemplateFVPType;
import com.morpho.morphosmart.sdk.TemplateType;

public class EmployeeFingerEnrollInfo extends MorphoInfo {

    private static EmployeeFingerEnrollInfo mInstance = null;
    private String empId = "";
    private String cardId = "";
    private String empName = "";
    private int noOfFingers = 2;
    private boolean savePKinDatabase = true;
    private CompressionAlgorithm compressionAlgorithm = CompressionAlgorithm.MORPHO_NO_COMPRESS;
    private TemplateType templateType = TemplateType.MORPHO_PK_ISO_FMR;
    private TemplateFVPType fvptemplateType = TemplateFVPType.MORPHO_NO_PK_FVP;
    private boolean updateTemplate = false;
    private int fingerIndexNo = 1;

    private String strSecurityLevel="";
    private String strVerificationMode="";
    private String strFirstFingerIndex="";
    private String strSecondFingerIndex="";
    private String strNewFirstFingerIndex="";
    private String strNewSecondFingerIndex="";

    private byte[] firstFingerFMD=null;
    private byte[] secondFingerFMD=null;
    private byte[] firstFingerFID=null;
    private byte[] secondFingerFID=null;
    private String strFirstFingerDataHex="";
    private String strSecondFingerDataHex="";

    private int operation=1;//1 For Enroll,2 For Update
    private boolean isTemplateExists=false;

    private String enrollType;
    private String remoteEnrollJobId;

    private boolean isEnroll;
    private String pin="";

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public boolean isEnroll() {
        return isEnroll;
    }

    public void setEnroll(boolean enroll) {
        isEnroll = enroll;
    }

    public String getRemoteEnrollJobId() {
        return remoteEnrollJobId;
    }

    public void setRemoteEnrollJobId(String remoteEnrollJobId) {
        this.remoteEnrollJobId = remoteEnrollJobId;
    }

    public String getEnrollType() {
        return enrollType;
    }

    public void setEnrollType(String enrollType) {
        this.enrollType = enrollType;
    }
    //private CompressionAlgorithm	compressionAlgorithm	= CompressionAlgorithm.NO_IMAGE;
    //private TemplateType			templateType			= TemplateType.MORPHO_NO_PK_FP;

    private EmployeeFingerEnrollInfo() {
    }

    public static EmployeeFingerEnrollInfo getInstance() {
        if (mInstance == null) {
            mInstance = new EmployeeFingerEnrollInfo();
            mInstance.reset();
        }
        return mInstance;
    }

    public int getOperation() {
        return operation;
    }

    public void setOperation(int operation) {
        this.operation = operation;
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

    public String getStrSecurityLevel() {
        return strSecurityLevel;
    }

    public void setStrSecurityLevel(String strSecurityLevel) {
        this.strSecurityLevel = strSecurityLevel;
    }

    public String getStrVerificationMode() {
        return strVerificationMode;
    }

    public void setStrVerificationMode(String strVerificationMode) {
        this.strVerificationMode = strVerificationMode;
    }

    public String getStrFirstFingerIndex() {
        return strFirstFingerIndex;
    }

    public void setStrFirstFingerIndex(String strFirstFingerIndex) {
        this.strFirstFingerIndex = strFirstFingerIndex;
    }

    public String getStrSecondFingerIndex() {
        return strSecondFingerIndex;
    }

    public void setStrSecondFingerIndex(String strSecondFingerIndex) {
        this.strSecondFingerIndex = strSecondFingerIndex;
    }

    public String getStrNewFirstFingerIndex() {
        return strNewFirstFingerIndex;
    }

    public void setStrNewFirstFingerIndex(String strNewFirstFingerIndex) {
        this.strNewFirstFingerIndex = strNewFirstFingerIndex;
    }

    public String getStrNewSecondFingerIndex() {
        return strNewSecondFingerIndex;
    }

    public void setStrNewSecondFingerIndex(String strNewSecondFingerIndex) {
        this.strNewSecondFingerIndex = strNewSecondFingerIndex;
    }

    public byte[] getFirstFingerFMD() {
        return firstFingerFMD;
    }

    public void setFirstFingerFMD(byte[] firstFingerFMD) {
        this.firstFingerFMD = firstFingerFMD;
    }

    public byte[] getSecondFingerFMD() {
        return secondFingerFMD;
    }

    public void setSecondFingerFMD(byte[] secondFingerFMD) {
        this.secondFingerFMD = secondFingerFMD;
    }

    public byte[] getFirstFingerFID() {
        return firstFingerFID;
    }

    public void setFirstFingerFID(byte[] firstFingerFID) {
        this.firstFingerFID = firstFingerFID;
    }

    public byte[] getSecondFingerFID() {
        return secondFingerFID;
    }

    public void setSecondFingerFID(byte[] secondFingerFID) {
        this.secondFingerFID = secondFingerFID;
    }

    public String getStrFirstFingerDataHex() {
        return strFirstFingerDataHex;
    }

    public void setStrFirstFingerDataHex(String strFirstFingerDataHex) {
        this.strFirstFingerDataHex = strFirstFingerDataHex;
    }

    public String getStrSecondFingerDataHex() {
        return strSecondFingerDataHex;
    }

    public void setStrSecondFingerDataHex(String strSecondFingerDataHex) {
        this.strSecondFingerDataHex = strSecondFingerDataHex;
    }

    public boolean isTemplateExists() {
        return isTemplateExists;
    }

    public void setTemplateExists(boolean templateExists) {
        isTemplateExists = templateExists;
    }

    public boolean isSavePKinDatabase() {
        return savePKinDatabase;
    }

    public boolean isUpdateTemplate() {
        return this.updateTemplate;
    }

    public void setSavePKinDatabase(boolean savePKinDatabase) {
        this.savePKinDatabase = savePKinDatabase;
    }

    /**
     * @return the templateType
     */
    public TemplateType getTemplateType() {
        return templateType;
    }

    /**
     * @return the fvpTemplateType
     */
    public TemplateFVPType getFVPTemplateType() {
        return fvptemplateType;
    }

    /**
     * @param templateType the templateType to set
     */
    public void setTemplateType(TemplateType templateType) {
        this.templateType = templateType;
    }

    public void setFVPTemplateType(TemplateFVPType fvptemplateType) {
        this.fvptemplateType = fvptemplateType;
    }

    /**
     * @return the compressionAlgorithm
     */
    public CompressionAlgorithm getCompressionAlgorithm() {
        return compressionAlgorithm;
    }

    /**
     * @param compressionAlgorithm the compressionAlgorithm to set
     */
    public void setCompressionAlgorithm(CompressionAlgorithm compressionAlgorithm) {
        this.compressionAlgorithm = compressionAlgorithm;
    }

    public void setUpdateTemplate(boolean updateTemplate) {
        this.updateTemplate = updateTemplate;
    }

    public int getFingerIndex() {
        return fingerIndexNo;
    }

    public void setFingerIndex(int fingerIndex) {
        this.fingerIndexNo = fingerIndex;
    }
    public int getNoOfFingers() {
        return noOfFingers;
    }

    public void setNoOfFingers(int noOfFingers) {
        this.noOfFingers = noOfFingers;
    }

    public void reset() {
        empId = "";
        cardId = "";
        empName = "";
        noOfFingers = 2;
        savePKinDatabase = true;
        compressionAlgorithm = CompressionAlgorithm.MORPHO_NO_COMPRESS;
        templateType = TemplateType.MORPHO_PK_ISO_FMR;
        updateTemplate = false;
        fingerIndexNo=1;
        //compressionAlgorithm = CompressionAlgorithm.NO_IMAGE;
        //templateType = TemplateType.MORPHO_NO_PK_FP;

        strSecurityLevel="";
        strVerificationMode="";
        strFirstFingerIndex="";
        strSecondFingerIndex="";
        strNewFirstFingerIndex="";
        strNewSecondFingerIndex="";

        firstFingerFMD=null;
        secondFingerFMD=null;
        firstFingerFID=null;
        secondFingerFID=null;

        strFirstFingerDataHex="";
        strSecondFingerDataHex="";

        isTemplateExists=false;
    }

    public String toString() {
        return "Emp Id" + empId + "\r\n" + "Card Id:\t" + cardId + "\r\n" + "Emp Name:\t" + empName + "\r\n" + "No Of Fingers:\t" + noOfFingers + "\r\n" + "savePKinDatabase:\t"
                + savePKinDatabase + "\r\n" + "exportImage:\t" + compressionAlgorithm.getLabel() + "\r\n" + "fpTemplateType:\t" + templateType;

    }
}
