package com.android.fortunaattendancesystem.ezeeHr.services;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by fortuna on 28/7/18.
 */

public class RemortEnroll {

    public String getPacketID() {
        return packetID;
    }

    public String getCommandType() {
        return commandType;
    }

    public String getCorporateId() {
        return corporateId;
    }

    public String getTerminalAddress() {
        return terminalAddress;
    }

    public String getUserId() {
        return userId;
    }

    public String getCardId() {
        return cardId;
    }

    public String getEmployeeID() {
        return employeeID;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public String getDateTimeOfEnrollment() {
        return dateTimeOfEnrollment;
    }

    public String getPacketCount() {
        return packetCount;
    }

    public String getSecurityLevel() {
        return securityLevel;
    }

    public String getFingerIndex() {
        return fingerIndex;
    }

    public String getVerificationMode() {
        return verificationMode;
    }

    public String getEnrollmentType() {
        return enrollmentType;
    }

    public String getFingerType() {
        return fingerType;
    }

    public String getAcquisitionQuality() {
        return acquisitionQuality;
    }

    private String packetID;                //pid;     //”Packet Id”,
    private String commandType;             //ct;      //”Command Type”,
    private String corporateId;             //coid;    //”Corporate Id”,
    private String terminalAddress;         //ta;      //”Terminal Address,
    private String userId;                  //uid;     //”UserCreationActivity Id”,
    private String cardId;                  //cid;     //”Card Id”,
    private String employeeID;              //eid;     //”Employee ID”,
    private String employeeName;            //en;      //”Employee Name”,
    private String dateTimeOfEnrollment;    //dtoe;    //”Date Time Of Enrollment”,
    private String packetCount;             //pc;      //”Packet Count”,
    private String securityLevel;           //sl;      //”Security Level”,
    private String fingerIndex;             //fi;      //”Finger Index”,
    private String verificationMode;        //vm;      // VM=”Verification Mode”,
    private String enrollmentType;          //et;      // ET = "EnrollmentType",
    private String fingerType;             //ft;      // FT = "Finger Type",
    private String acquisitionQuality;     //aq;      // AQ = "acquisition Quality"

    private JSONObject readJObject;


    public RemortEnroll(String jesonString) {
        try {
            this.readJObject = new JSONObject(jesonString);
            packetID = readJObject.getString("pid");
            commandType = readJObject.getString("ct");
            corporateId = readJObject.getString("coid");
            terminalAddress = readJObject.getString("ta");
            userId = readJObject.getString("uid");
            cardId = readJObject.getString("cid");
            employeeID = readJObject.getString("eid");
            employeeName = readJObject.getString("en");
            dateTimeOfEnrollment = readJObject.getString("dtoe");
            packetCount = readJObject.getString("pc");
            securityLevel = readJObject.getString("sl");
            fingerIndex = readJObject.getString("fi");
            verificationMode = readJObject.getString("vm");
            enrollmentType = readJObject.getString("et");
            fingerType = readJObject.getString("ft");
            acquisitionQuality = readJObject.getString("aq");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
