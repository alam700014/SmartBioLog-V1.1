package com.android.fortunaattendancesystem.extras;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.acpl.access_computech_fm220_sdk.acpl_FM220_SDK;
import com.android.fortunaattendancesystem.model.BasicEmployeeInfo;
import com.android.fortunaattendancesystem.singleton.Settings;
import com.android.fortunaattendancesystem.singleton.UserDetails;
import com.android.fortunaattendancesystem.helper.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Decoder.BASE64Decoder;
import Decoder.BASE64Encoder;

/**
 * Created by fortuna on 10/6/16.
 */
public class DataBaseLayer {


    private static String DB_PATH = "/mnt/sdcard/project_data/Android.db";
    private static String EMPLOYEE_TABLE = "EmployeeM";
    private static String FINGER_TABLE = "FingerTemplateX";
    private static String ATTENDANCE_TABLE = "AttendanceT";
    private static String SECTOR_KEY = "SmartKey";
    private static String SECTOR_KEY_CARD_INIT = "SmartKeyCardInit";
    private static String GROUP_TABLE = "GroupM";
    private static String SITE_TABLE = "SiteCodeM";
    private static String BATCH_TABLE = "BatchM";
    private static String TRAINING_TABLE = "TrainingCenterM";
    private static String AADHAAR_TABLE = "AadhaarT";
    private static String TEST_AADHAAR_FINGER_TEMPLATE_X = "AadhaarFingerTemplatex";
    private static String AADHAARAUTH_TABLE = "AadhaarAuthT";
    private static String FINGER_ENROLL_MODE_TABLE = "FingerEnrollMode";
    private static String CARD_VER_PIN_TABLE = "CardVerificationPin";
    private static String IN_OUT_MODE_TABLE = "InOutTimeM";
    private static String ATTENDANCE_SERVER_TABLE = "AttendanceServer";
    private static String AADHAAR_SERVER_TABLE = "AadhaarServer";
    private static String USER_TABLE = "UserM";
    private static String SMART_CARD_OPERATION_TABLE = "SmartCardOperationLog";
    private static String HOTLIST_TABLE = "HotlistLog";
    private static String HARDWARE_MODULES_TABLE = "HardwareModulesM";
    private static String HARDWARE_SETTINGS_TABLE = "HardwareSettings";

    private static String SETTINGS_INI_TABLE = "SettingsIniM";
    private static String SETTINGS_TABLE = "SettingsM";

    SQLiteDatabase db;
    BASE64Encoder encoder = new BASE64Encoder();

    public int isDataAvailableInDatabase(String strEmployeeId) {
        int AutoId = -1;
        Cursor resData = null;
        try {
            String strPaddedEmpId = Utility.paddEmpId(strEmployeeId);
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select AutoId from " + EMPLOYEE_TABLE + " where EmployeeId='" + strPaddedEmpId + "'", null);
            if (resData != null && resData.getCount() > 0) {
                while (resData.moveToNext()) {
                    AutoId = resData.getInt(0);
                }
            }
        } catch (Exception e) {

        } finally {
            if (resData != null) {
                resData.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return AutoId;
    }

    public boolean isCardDataAvailableInDatabase(String strCardId) {
        boolean isExists = false;
        Cursor resData = null;
        try {
            String strPaddedCardId = Utility.paddCardId(strCardId);
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select CardId from " + EMPLOYEE_TABLE + " where CardId='" + strPaddedCardId + "'", null);
            if (resData != null && resData.getCount() > 0) {
                isExists = true;
            }

        } catch (Exception e) {

        } finally {
            if (resData != null) {
                resData.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return isExists;
    }


    public int insertEmployeeDataFromExcel(ArrayList <String> empDataList) {

        int insertStatus = -1;
        byte[] image = null;

        try {

            ContentValues initialValues = new ContentValues();
            String strEmpId = empDataList.get(0).trim().toUpperCase();

            if (strEmpId.trim().length() < 16) {
                String strPaddedEmpId = "";
                strPaddedEmpId = Utility.paddEmpId(strEmpId);
                initialValues.put("EmployeeID", strPaddedEmpId);
            } else {
                initialValues.put("EmployeeID", strEmpId);
            }

            String strCardId = empDataList.get(1).toUpperCase();

            if (strCardId.trim().length() < 8) {
                String strPaddedCardId = "";
                strPaddedCardId = Utility.paddCardId(strCardId);
                initialValues.put("CardId", strPaddedCardId);
            } else {
                initialValues.put("CardId", strCardId);
            }

            String strName = empDataList.get(2).toUpperCase();

            initialValues.put("Name", strName);

            String strBloodGroup = empDataList.get(4);

            if (!strBloodGroup.trim().equalsIgnoreCase("Select")) {
                initialValues.put("BloodGroup", strBloodGroup);
            } else {
                initialValues.put("BloodGroup", "");
            }

            initialValues.put("SiteCode", "");

            String strMobileNo = empDataList.get(5);
            initialValues.put("MobileNo", strMobileNo);


            String strMailId = empDataList.get(6);
            initialValues.put("MailId", strMailId);

            String strValidUpto = empDataList.get(7);
            initialValues.put("ValidUpto", strValidUpto);

            String strBirthday = empDataList.get(8);
            initialValues.put("BirthDay", strBirthday);

            String strPin = empDataList.get(9);
            initialValues.put("PIN", strPin);

            initialValues.put("GroupId", "");
            initialValues.put("fkTrainingCenter", "");
            initialValues.put("fkBatchCenter", "");


            //Non Editable Fields//

            initialValues.put("EnrollStatus", "");
            initialValues.put("NosFinger", "");
            initialValues.put("SmartCardSerialNo", "");
            initialValues.put("SmartCardVersion", 0);

            //Non Editable Fields//


            //Admin Rights//

            initialValues.put("VerificationMode", "");
            initialValues.put("IsBlacklisted", "");
            initialValues.put("IsAccessRightEnabled", "");
            initialValues.put("IsAccessRightEnabled", "");
            initialValues.put("IsLockOpenWhenAllowed", "");
            initialValues.put("Photo", image);


            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            insertStatus = (int) db.insert(EMPLOYEE_TABLE, null, initialValues);


        } catch (SQLiteException e) {

        } finally {
            if (db != null) {
                db.close();
            }
        }

        return insertStatus;

    }

    public int insertEmployeeData(String strEmpId, String strCardId, String strName, String strBloodGroup, String strSiteCode, String strEnrollStatus, String strEnrollNoFinger, String strMobileNo, String strMailId, String strPin, String strVerification, String strValidUpto, String strBirthday, String strGroupId, String strTrainingCenter, String strBatch, String strIsBlockStatus, String strIsAccess, String strIsLock, String strSmartCardVer, byte[] imageData) {

        int insertStatus = -1;

        try {

            ContentValues initialValues = new ContentValues();

            if (strEmpId.trim().length() < 16) {
                String strPaddedEmpId = "";
                strPaddedEmpId = Utility.paddEmpId(strEmpId);
                initialValues.put("EmployeeID", strPaddedEmpId);
            } else {
                initialValues.put("EmployeeID", strEmpId);
            }

            if (strCardId.trim().length() < 8) {
                String strPaddedCardId = "";
                strPaddedCardId = Utility.paddCardId(strCardId);
                initialValues.put("CardId", strPaddedCardId);
            } else {
                initialValues.put("CardId", strCardId);
            }

            initialValues.put("Name", strName);


            if (!strBloodGroup.trim().equalsIgnoreCase("Select")) {
                initialValues.put("BloodGroup", strBloodGroup);
            } else {
                initialValues.put("BloodGroup", "");
            }

            initialValues.put("SiteCode", strSiteCode);
            initialValues.put("MobileNo", strMobileNo);
            initialValues.put("MailId", strMailId);
            initialValues.put("ValidUpto", strValidUpto);
            initialValues.put("BirthDay", strBirthday);
            initialValues.put("PIN", strPin);
            initialValues.put("GroupId", strGroupId);
            initialValues.put("fkTrainingCenter", strTrainingCenter);
            initialValues.put("fkBatchCenter", strBatch);


            //Non Editable Fields//

            initialValues.put("EnrollStatus", strEnrollStatus);
            initialValues.put("NosFinger", strEnrollNoFinger);
            initialValues.put("SmartCardSerialNo", "");
            initialValues.put("SmartCardVersion", 0);

            //Non Editable Fields//


            //Admin Rights//


            if (!strVerification.trim().equalsIgnoreCase("Select")) {
                initialValues.put("VerificationMode", strVerification);
            } else {
                initialValues.put("VerificationMode", "");
            }

            if (!strIsBlockStatus.trim().equalsIgnoreCase("Select")) {
                initialValues.put("IsBlacklisted", strIsBlockStatus);
            } else {
                initialValues.put("IsBlacklisted", "");
            }

            if (!strIsAccess.trim().equalsIgnoreCase("Select")) {
                initialValues.put("IsAccessRightEnabled", strIsAccess);

            } else {
                initialValues.put("IsAccessRightEnabled", "");
            }

            if (!strIsLock.trim().equalsIgnoreCase("Select")) {
                initialValues.put("IsLockOpenWhenAllowed", strIsLock);
            } else {
                initialValues.put("IsLockOpenWhenAllowed", "");
            }

            //Admin Rights//


            initialValues.put("Photo", imageData);

            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            insertStatus = (int) db.insert(EMPLOYEE_TABLE, null, initialValues);

        } catch (SQLiteException e) {

        } finally {
            if (db != null) {
                db.close();
            }
        }

        return insertStatus;
    }

    public int insertEmployeeDataFromServer(String strEmpId, String strCardId, String strName, String strMailId, String strMobileNo, String strBloodGroup, String strValidUpto, String strBirthday, String strPin, String strIsBlockStatus, String strIsLock) {
        int insertStatus = -1;
        try {
            ContentValues initialValues = new ContentValues();
            if (strEmpId.trim().length() < 16) {
                String strPaddedEmpId = "";
                strPaddedEmpId = Utility.paddEmpId(strEmpId);
                initialValues.put("EmployeeID", strPaddedEmpId);
            } else {
                initialValues.put("EmployeeID", strEmpId);
            }

            if (strCardId.trim().length() < 8) {
                String strPaddedCardId = "";
                strPaddedCardId = Utility.paddCardId(strCardId);
                initialValues.put("CardId", strPaddedCardId);
            } else {
                initialValues.put("CardId", strCardId);
            }

            initialValues.put("Name", strName);
            initialValues.put("BloodGroup", Utility.getBloodGrValByNumber(strBloodGroup));
            initialValues.put("MobileNo", strMobileNo);
            initialValues.put("MailId", strMailId);
            initialValues.put("ValidUpto", strValidUpto);
            initialValues.put("BirthDay", strBirthday);
            initialValues.put("PIN", strPin);
            initialValues.put("IsBlacklisted", strIsBlockStatus);
            initialValues.put("IsLockOpenWhenAllowed", strIsLock);
            initialValues.put("SiteCode", "");
            initialValues.put("GroupId", "");
            initialValues.put("fkTrainingCenter", "");
            initialValues.put("fkBatchCenter", "");
            initialValues.put("IsAccessRightEnabled", "");
            initialValues.put("SmartCardSerialNo", "");
            //initialValues.put("Photo", "");

            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            insertStatus = (int) db.insert(EMPLOYEE_TABLE, null, initialValues);

        } catch (SQLiteException e) {

        } finally {
            if (db != null) {
                db.close();
            }
        }

        return insertStatus;
    }


    public int deleteEmployeeData(String strEmpId) {

        int deleteStaus = -1;

        try {

            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            String strCondition = "EmployeeID" + "='" + strEmpId + "'";
            deleteStaus = db.delete(EMPLOYEE_TABLE, strCondition, null);

        } catch (SQLiteException e) {

        } finally {
            if (db != null) {
                db.close();
            }
        }

        return deleteStaus;

    }

    public Cursor getEmployeeBasicDetails(String strEmpId) {
        Cursor resData = null;
        try {

            String strPaddedEmpId = Utility.paddEmpId(strEmpId);
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select CardId,Name,Photo from " + EMPLOYEE_TABLE + " where EmployeeId='" + strPaddedEmpId + "'", null);
            if (resData != null && resData.getCount() > 0) {
                return resData;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public Cursor getEmployeeFullDetails(String strEmpId) {

        Cursor resData = null;

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select EmployeeId,CardId,Name,BloodGroup,SiteCode,MobileNo,MailId,PIN,ValidUpto,BirthDay,GroupId from " + EMPLOYEE_TABLE + " where EmployeeId='" + strEmpId + "'", null);
            if (resData != null && resData.getCount() > 0) {
                return resData;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }


    public boolean saveOneEnrolledTemplate(String strVerificationMode, String strSecurityLevel) {

//        String strAutoId = "";
//        int insertStaus = -1;
//        int updateStatus = -1;
//        Cursor c = null;
        boolean status = false;
//
//        try {
//
//            Calendar ca = Calendar.getInstance();
//            SimpleDateFormat df = new SimpleDateFormat("ddMMyyyyHHmmss");
//
//            String enrolledDateTime = df.format(ca.getTime());
//
//            String strEmpId = FingerDataDetails.getInstance().getStrEmpId();
//            String strPaddedEmpId = Utility.paddEmpId(strEmpId);
//
//            byte[] firstFingerFID = FingerDataDetails.getInstance().getFirstFingerFID();
//            String strFirstFingerDataHex = FingerDataDetails.getInstance().getStrFirstFingerDataHex();
//
//            String strFirstFingerIndex = FingerDataDetails.getInstance().getStrFirstFingerIndex();
//
//            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
//            String selectQuery = "SELECT AutoId FROM " + EMPLOYEE_TABLE + " WHERE " + " EmployeeId='" + strPaddedEmpId + "'";
//            c = db.rawQuery(selectQuery, null);
//
//            if (c != null) {
//                if (c.getCount() > 0) {
//                    while (c.moveToNext()) {
//                        strAutoId = c.getString(0);
//                    }
//                }
//            }
//
//            if (strAutoId.trim().length() > 0) {
//
//                ContentValues firstTemplate = new ContentValues();
//                firstTemplate.put("AutoID", strAutoId);
//                firstTemplate.put("TermplateType", "iso");
//                firstTemplate.put("TemplateSrNo", "1");
//
//                firstTemplate.put("FingerIndex", strFirstFingerIndex);
//
//                if (!strSecurityLevel.trim().equalsIgnoreCase("Select")) {
//                    firstTemplate.put("SecurityLevel", strSecurityLevel);
//                } else {
//                    firstTemplate.put("SecurityLevel", "");
//                }
//
//                if (!strVerificationMode.trim().equalsIgnoreCase("Select")) {
//                    firstTemplate.put("VerificationMode", strVerificationMode);
//                } else {
//                    firstTemplate.put("VerificationMode", "");
//                }
//
//                firstTemplate.put("Quality", "");
//                firstTemplate.put("EnrolledOn", enrolledDateTime);  //date & time at the time of finger enrollment
//                firstTemplate.put("Template", strFirstFingerDataHex + "00000000");  //adjust 4 byte 252 + 4 = 256
//                firstTemplate.put("FingerImage", firstFingerFID);
//                firstTemplate.put("isAadhaarVerifiedYorN", "N");
//
//                insertStaus = (int) db.insert(FINGER_TABLE, null, firstTemplate);
//
//                if (insertStaus != -1) {
//                    updateStatus = updateFingerDataToEmpTable(strAutoId, "Yes", "1", strVerificationMode, "N");
//                    if (updateStatus != -1) {
//                        status = true;
//                    }
//                }
//            }
//
//        } catch (Exception e) {
//
//        } finally {
//            if (c != null) {
//                c.close();
//            }
//            if (db != null) {
//                db.close();
//            }
//        }
//
        return status;
    }

    /* -------------------------------------------- save one template | Added By Sanjay  --------------------------------------------  */


    public int insertFpToAadhaarFingerTemplateX(int fingerNumber, String strAadhaarId, String strEmpId, String strFirstFingerIndex, String strHexFirstFingerData, byte[] firstFingerImageData, String strSecondFingerIndex, String strHexSecondFingerData, byte[] secondFingerImageData, String strSecurityLevel, String strVerificationMode, String aadhaarEnrolledId) {

        String strAutoId = "";
        int insertStaus = -1;
        int updateStatus = -1;

        Cursor c = null;

        try {

            Calendar ca = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("ddMMyyyyHHmmss");
            String datime = df.format(ca.getTime());

            String strPaddedEmpId = Utility.paddEmpId(strEmpId);

            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);

            String selectQuery = "SELECT AutoId FROM " + EMPLOYEE_TABLE + " WHERE " + " EmployeeId='" + strPaddedEmpId + "'";
            c = db.rawQuery(selectQuery, null);

            if (c != null) {
                if (c.getCount() > 0) {
                    while (c.moveToNext()) {
                        strAutoId = c.getString(0);
                    }
                }
            }

            if (strAutoId.trim().length() > 0) {

                if (fingerNumber == 1) {
                    ContentValues firstTemplate = new ContentValues();
                    firstTemplate.put("AutoID", strAutoId);
                    firstTemplate.put("AadhaarNo", strAadhaarId);
                    firstTemplate.put("TermplateType", "iso");
                    firstTemplate.put("TemplateSrNo", "1");
                    firstTemplate.put("FingerIndex", strFirstFingerIndex);
                    firstTemplate.put("SecurityLevel", strSecurityLevel);
                    firstTemplate.put("VerificationMode", strVerificationMode);
                    firstTemplate.put("Quality", "");
                    firstTemplate.put("EnrolledOn", datime);  //date & time at the time of finger enrollment
                    firstTemplate.put("Template", strHexFirstFingerData);  //adjust 4 byte 252 + 4 = 256
                    firstTemplate.put("FingerImage", firstFingerImageData);

                    insertStaus = (int) db.insert(TEST_AADHAAR_FINGER_TEMPLATE_X, null, firstTemplate);

//                    Log.d("TEST", "First Finger Insert Val:" + insertStaus);
//
//                    updateStatus=updateAadhaarAuthTbl("first",insertStaus,aadhaarEnrolledId);
//
//                    Log.d("TEST","First Finger Update Aadhaar Table Val:"+updateStatus);
//
//                    updateStatus=updateFingerDataToEmpTable(strAutoId, "Yes", "1", strVerificationMode);

                } else if (fingerNumber == 2) {

                    ContentValues firstTemplate = new ContentValues();
                    firstTemplate.put("AutoID", strAutoId);
                    firstTemplate.put("AadhaarNo", strAadhaarId);
                    firstTemplate.put("TermplateType", "iso");
                    firstTemplate.put("TemplateSrNo", "1");
                    firstTemplate.put("FingerIndex", strFirstFingerIndex);
                    firstTemplate.put("SecurityLevel", strSecurityLevel);
                    firstTemplate.put("VerificationMode", strVerificationMode);
                    firstTemplate.put("Quality", "");
                    firstTemplate.put("EnrolledOn", datime);  //date & time at the time of finger enrollment
                    firstTemplate.put("Template", strHexFirstFingerData);  //adjust 4 byte 252 + 4 = 256
                    firstTemplate.put("FingerImage", firstFingerImageData);

                    insertStaus = (int) db.insert(TEST_AADHAAR_FINGER_TEMPLATE_X, null, firstTemplate);


//
//                    Log.d("TEST","First Finger Insert Val:"+insertStaus);
//
//                    updateStatus=updateAadhaarAuthTbl("first",insertStaus,aadhaarEnrolledId);
//
//                    Log.d("TEST","First Finger Update Aadhaar Table Val:"+updateStatus);


                    //   updateStatus=updateFingerDataToEmpTable(strAutoId, "Yes", "1", strVerificationMode);


                    ContentValues secondTemplate = new ContentValues();
                    secondTemplate.put("AutoID", strAutoId);
                    secondTemplate.put("AadhaarNo", strAadhaarId);
                    secondTemplate.put("TermplateType", "iso");
                    secondTemplate.put("TemplateSrNo", "2");
                    secondTemplate.put("FingerIndex", strSecondFingerIndex);
                    secondTemplate.put("SecurityLevel", strSecurityLevel);
                    secondTemplate.put("VerificationMode", strVerificationMode);
                    secondTemplate.put("Quality", "");
                    secondTemplate.put("EnrolledOn", datime);  //date & time at the time of finger enrollment
                    secondTemplate.put("Template", strHexSecondFingerData);  //adjust 4 byte 252 + 4 = 256
                    secondTemplate.put("FingerImage", secondFingerImageData);

                    insertStaus = (int) db.insert(TEST_AADHAAR_FINGER_TEMPLATE_X, null, secondTemplate);

//                    Log.d("TEST","Second Finger Insert Val:"+insertStaus);
//
//                    updateStatus=updateAadhaarAuthTbl("second",insertStaus,aadhaarEnrolledId);
//
//                    Log.d("TEST","Second Finger Update Aadhaar Table Val:"+updateStatus);
//
//                    updateStatus=updateFingerDataToEmpTable(strAutoId,"Yes","2",strVerificationMode);


                }


            }

            if (insertStaus != -1) {
                return insertStaus;

            } else {
                return -1;
            }


        } catch (Exception e) {

            return -1;

        } finally {

            if (c != null) {

                c.close();
            }
            if (db != null) {

                db.close();

            }
        }

    }


    public int saveAadhaarTemplateToLocalDb(int fingerNumber, String strEmpId, String strFirstFingerIndex, String strHexFirstFingerData, byte[] firstFingerImageData, String strSecondFingerIndex, String strHexSecondFingerData, byte[] secondFingerImageData, String strSecurityLevel, String strVerificationMode, String aadhaarEnrolledId) {

//        String strAutoId = "";
        int insertStaus = -1;
//        int updateStatus = -1;
//        Cursor c = null;

//        try {
//
//            Calendar ca = Calendar.getInstance();
//            SimpleDateFormat df = new SimpleDateFormat("ddMMyyyyHHmmss");
//            String datime = df.format(ca.getTime());
//
//            String strPaddedEmpId = Utility.paddEmpId(strEmpId);
//            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
//            String selectQuery = "SELECT AutoId FROM " + EMPLOYEE_TABLE + " WHERE " + " EmployeeId='" + strPaddedEmpId + "'";
//            c = db.rawQuery(selectQuery, null);
//
//            if (c != null && c.getCount() > 0) {
//                while (c.moveToNext()) {
//                    strAutoId = c.getString(0);
//                }
//            }
//
//            if (strAutoId.trim().length() > 0) {
//
//                if (fingerNumber == 1) {
//
//                    ContentValues firstTemplate = new ContentValues();
//                    firstTemplate.put("AutoID", strAutoId);
//                    firstTemplate.put("TermplateType", "iso");
//                    firstTemplate.put("TemplateSrNo", "1");
//                    firstTemplate.put("FingerIndex", strFirstFingerIndex);
//                    firstTemplate.put("SecurityLevel", strSecurityLevel);
//                    firstTemplate.put("VerificationMode", strVerificationMode);
//                    firstTemplate.put("Quality", "");
//                    firstTemplate.put("EnrolledOn", datime);  //date & time at the time of finger enrollment
//                    firstTemplate.put("Template", strHexFirstFingerData);  //adjust 4 byte 252 + 4 = 256
//                    firstTemplate.put("FingerImage", firstFingerImageData);
//                    firstTemplate.put("isAadhaarVerifiedYorN", "Y");
//
//                    insertStaus = (int) db.insert(FINGER_TABLE, null, firstTemplate);
//                    updateStatus = updateAadhaarAuthTbl("first", insertStaus, aadhaarEnrolledId);
//
//                    updateStatus = updateFingerDataToEmpTable(strAutoId, "Yes", "1", strVerificationMode, "Y");
//
//                } else if (fingerNumber == 2) {
//
//                    ContentValues firstTemplate = new ContentValues();
//                    firstTemplate.put("AutoID", strAutoId);
//                    firstTemplate.put("TermplateType", "iso");
//                    firstTemplate.put("TemplateSrNo", "1");
//                    firstTemplate.put("FingerIndex", strFirstFingerIndex);
//                    firstTemplate.put("SecurityLevel", strSecurityLevel);
//                    firstTemplate.put("VerificationMode", strVerificationMode);
//                    firstTemplate.put("Quality", "");
//                    firstTemplate.put("EnrolledOn", datime);  //date & time at the time of finger enrollment
//                    firstTemplate.put("Template", strHexFirstFingerData);  //adjust 4 byte 252 + 4 = 256
//                    firstTemplate.put("FingerImage", firstFingerImageData);
//                    firstTemplate.put("isAadhaarVerifiedYorN", "Y");
//
//                    insertStaus = (int) db.insert(FINGER_TABLE, null, firstTemplate);
//                    updateStatus = updateAadhaarAuthTbl("first", insertStaus, aadhaarEnrolledId);
//
//
//                    //   updateStatus=updateFingerDataToEmpTable(strAutoId, "Yes", "1", strVerificationMode);
//
//
//                    ContentValues secondTemplate = new ContentValues();
//                    secondTemplate.put("AutoID", strAutoId);
//                    secondTemplate.put("TermplateType", "iso");
//                    secondTemplate.put("TemplateSrNo", "2");
//                    secondTemplate.put("FingerIndex", strSecondFingerIndex);
//                    secondTemplate.put("SecurityLevel", strSecurityLevel);
//                    secondTemplate.put("VerificationMode", strVerificationMode);
//                    secondTemplate.put("Quality", "");
//                    secondTemplate.put("EnrolledOn", datime);  //date & time at the time of finger enrollment
//                    secondTemplate.put("Template", strHexSecondFingerData);  //adjust 4 byte 252 + 4 = 256
//                    secondTemplate.put("FingerImage", secondFingerImageData);
//                    secondTemplate.put("isAadhaarVerifiedYorN", "Y");
//
//                    insertStaus = (int) db.insert(FINGER_TABLE, null, secondTemplate);
//                    updateStatus = updateAadhaarAuthTbl("second", insertStaus, aadhaarEnrolledId);
//
//                    updateStatus = updateFingerDataToEmpTable(strAutoId, "Yes", "2", strVerificationMode, "Y");
//
//                }
//            }
//
//        } catch (Exception e) {
//
//        } finally {
//            if (c != null) {
//                c.close();
//            }
//            if (db != null) {
//                db.close();
//            }
//        }
        return insertStaus;

    }


    public int updateFingerDataToEmpTableFromServer(String strAutoId, String enrollStatus, String noOfFingers, String strVerificationMode) {

        int updateStatus = -1;

        try {

            int AutoId = Integer.parseInt(strAutoId);
            ContentValues fingerDetails = new ContentValues();
            fingerDetails.put("EnrollStatus", enrollStatus);
            fingerDetails.put("NosFinger", noOfFingers);
            fingerDetails.put("VerificationMode", strVerificationMode);

            updateStatus = db.update(EMPLOYEE_TABLE, fingerDetails, "AutoId=" + AutoId, null);

        } catch (Exception e) {

        }

        return updateStatus;
    }

    /* -----------------------  Template save From EzeeHr -----------------------------  */
    public boolean saveEnrolledTemplateEzeeHr(String strEID, String strDOE, ArrayList <String> arrFT, ArrayList <String> arrSL, ArrayList <String> arrFI, ArrayList <String> arrFQ, ArrayList <String> arrTS, ArrayList <String> arrFMID, ArrayList <String> arrFID, int intNosFinger, String strVM) {

        String strAutoId = "";
        int insertStaus = -1;
        int updateStatus = -1;
        Cursor c = null;
        boolean status = false;
        boolean deletionStatus = false;
        BASE64Decoder decoder = new BASE64Decoder();

        try {

            String strEmpId = strEID;
            String strPaddedEmpId = Utility.paddEmpId(strEmpId);

            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            String selectQuery = "SELECT AutoId FROM " + EMPLOYEE_TABLE + " WHERE " + " EmployeeId='" + strPaddedEmpId + "'";
            c = db.rawQuery(selectQuery, null);

            if (c != null) {
                if (c.getCount() > 0) {
                    while (c.moveToNext()) {
                        strAutoId = c.getString(0);
                    }
                }
            }


            if (strAutoId.trim().length() > 0) {

                deletionStatus = deleteEmployeeFingerDetails(Integer.parseInt(strAutoId));
                Log.d("TEST", "saveEnrolledTemplateEzeeHr | deleteEmployeeFingerDetails : " + deletionStatus);

                String strFirstFid = arrFID.get(0);
                if (!strFirstFid.contains("null")) {
                    byte[] firstFingerFID = decoder.decodeBuffer(strFirstFid); // decode BASE64
                    strFirstFid = firstFingerFID.toString();
                }
                String strFirstFingerDataHex = arrFMID.get(0);
                String strFirstFingerIndex = arrFI.get(0);
                String strVerMode = Utility.setVerificationModeValByNumber(strVM);
                String enrolledDateTime = Utility.DateFormatChange(strDOE);
                String templateSerial = arrFT.get(0);
                if (templateSerial.contains("F1")) {
                    templateSerial = "1";
                } else {
                    templateSerial = "2";
                }

                ContentValues firstTemplate = new ContentValues();
                firstTemplate.put("AutoID", strAutoId);
                firstTemplate.put("TermplateType", "iso");
                firstTemplate.put("TemplateSrNo", templateSerial);
                firstTemplate.put("FingerIndex", Utility.setFingerIndexValByHex(strFirstFingerIndex));
                firstTemplate.put("SecurityLevel", Utility.setSecurityLvlValByNumber(arrSL.get(0)));
                firstTemplate.put("VerificationMode", strVerMode);
                firstTemplate.put("Quality", arrFQ.get(0));
                firstTemplate.put("EnrolledOn", enrolledDateTime);  //date & time at the time of finger enrollment
                firstTemplate.put("Template", strFirstFingerDataHex);  //adjust 4 byte 252 + 4 = 256
                firstTemplate.put("FingerImage", strFirstFid);
                firstTemplate.put("isAadhaarVerifiedYorN", "N");

                if (!db.isOpen())
                    db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
                insertStaus = (int) db.insert(FINGER_TABLE, null, firstTemplate);

                if (intNosFinger == 2) {

                    String strSecondFid = arrFID.get(1);
                    byte[] secondFingerFID = decoder.decodeBuffer(strSecondFid); // decode BASE64
                    String strSecondFingerDataHex = arrFMID.get(1);
                    String strSecondFingerIndex = arrFI.get(1);
                    templateSerial = arrFT.get(1);
                    if (templateSerial.contains("F1")) {
                        templateSerial = "1";
                    } else {
                        templateSerial = "2";
                    }

                    ContentValues secondTemplate = new ContentValues();
                    secondTemplate.put("AutoID", strAutoId);
                    secondTemplate.put("TermplateType", "iso");
                    secondTemplate.put("TemplateSrNo", templateSerial);
                    secondTemplate.put("FingerIndex", Utility.setFingerIndexValByHex(strSecondFingerIndex));
                    secondTemplate.put("SecurityLevel", Utility.setSecurityLvlValByNumber(arrSL.get(1)));
                    secondTemplate.put("VerificationMode", strVerMode);
                    secondTemplate.put("Quality", arrFQ.get(0));
                    secondTemplate.put("EnrolledOn", enrolledDateTime);  //date & time at the time of finger enrollment
                    secondTemplate.put("Template", strSecondFingerDataHex);  //adjust 4 byte 252 + 4 = 256
                    secondTemplate.put("FingerImage", secondFingerFID);
                    secondTemplate.put("isAadhaarVerifiedYorN", "N");

                    if (!db.isOpen())
                        db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
                    insertStaus = (int) db.insert(FINGER_TABLE, null, secondTemplate);
                }

                if (insertStaus != -1) {
                    updateStatus = updateFingerDataToEmpTableFromEzeeHr(strAutoId, "Yes", intNosFinger, strVerMode, "N");
                    if (updateStatus != -1) {
                        status = true;
                    }
                }
            }

        } catch (Exception e) {
            Log.d("TEST", "saveEnrolledTemplateEzeeHr | Exception : " + e);
        } finally {
            if (c != null) {
                c.close();
            }
            if (db != null) {
                db.close();
            }
        }

        return status;

    }


    public boolean updateOneTemplateToDBFromEzeeHr(String strVerificationMode, String strSecurityLevel) {

//        String strAutoId = "";
//        int updateFingerStatus = -1;
//        int updateEmpStatus = -1;
//        Cursor c = null;
//        Cursor resData = null;

        boolean status = false;

//        try {
//
//            Calendar ca = Calendar.getInstance();
//            SimpleDateFormat df = new SimpleDateFormat("ddMMyyyyHHmmss");
//            String datime = df.format(ca.getTime());
//
//            int noOfFingers = FingerDataDetails.getInstance().getNoOfFingers();
//
//            String strEmpId = FingerDataDetails.getInstance().getStrEmpId();
//            String strPaddedEmpId = Utility.paddEmpId(strEmpId);
//
//            String strFirstFingerIndex = FingerDataDetails.getInstance().getStrNewFirstFingerIndex();
//            String strSecondFingerIndex = FingerDataDetails.getInstance().getStrNewSecondFingerIndex();
//
//            byte[] firstFingerFID = FingerDataDetails.getInstance().getFirstFingerFID();
//            String strFirstFingerDataHex = FingerDataDetails.getInstance().getStrFirstFingerDataHex();
//
//            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
//
//            String selectQuery = "SELECT AutoId FROM " + EMPLOYEE_TABLE + " WHERE " + " EmployeeId='" + strPaddedEmpId + "'";
//            c = db.rawQuery(selectQuery, null);
//
//            if (c != null) {
//                if (c.getCount() > 0) {
//                    while (c.moveToNext()) {
//                        strAutoId = c.getString(0);
//                    }
//                }
//            }
//            Log.d("TEST", "strAutoId " + strAutoId);
//
//            if (strAutoId.trim().length() > 0) {
//
//                ArrayList <String> strIdList = new ArrayList <String>();
//                String strQuery = "SELECT ID FROM " + FINGER_TABLE + " WHERE " + " AutoId='" + strAutoId + "'";
//                resData = db.rawQuery(strQuery, null);
//
//                if (resData != null && resData.getCount() > 0) {
//                    while (resData.moveToNext()) {
//                        String strId = Integer.toString(resData.getInt(0));
//                        strIdList.add(strId);
//                    }
//                }
//                Log.d("TEST", "strIdList " + strIdList.toString());
//                Log.d("TEST", "noOfFingers " + noOfFingers);
//
//                if (noOfFingers == 1) {
//
//                    int fingerIndexNo = FingerDataDetails.getInstance().getFingerIndexNo();
//                    Log.d("TEST", "fingerIndexNo " + fingerIndexNo);
//                    if (fingerIndexNo == 1) {
//
//                        //===========================First Finger Index Update============================================//
//
//                        ContentValues firstTemplate = new ContentValues();
//                        firstTemplate.put("AutoID", strAutoId);
//                        firstTemplate.put("TermplateType", "iso");
//                        firstTemplate.put("TemplateSrNo", "1");
//
//                        firstTemplate.put("FingerIndex", strFirstFingerIndex);
//
//                        if (!strSecurityLevel.trim().equalsIgnoreCase("Select")) {
//                            firstTemplate.put("SecurityLevel", strSecurityLevel);
//                        } else {
//                            firstTemplate.put("SecurityLevel", "");
//                        }
//
//                        if (!strVerificationMode.trim().equalsIgnoreCase("Select")) {
//                            firstTemplate.put("VerificationMode", strVerificationMode);
//                        } else {
//                            firstTemplate.put("VerificationMode", "");
//                        }
//
//                        firstTemplate.put("Quality", "");
//                        firstTemplate.put("EnrolledOn", datime);  //date & time at the time of finger enrollment
//                        firstTemplate.put("Template", strFirstFingerDataHex + "00000000");  //adjust 4 byte 252 + 4 = 256
//                        firstTemplate.put("FingerImage", firstFingerFID);
//                        firstTemplate.put("isAadhaarVerifiedYorN", "N");
//                        Log.d("TEST", "firstTemplate table data :" + firstTemplate.toString());
//                        if (strFirstFingerDataHex.contains("null")) {
//                            Log.d("TEST", "Temptate Contain null");
//                            return false;
//                        }
//                        updateFingerStatus = db.update(FINGER_TABLE, firstTemplate, "ID=" + strIdList.get(0), null);
//
//                    } else if (fingerIndexNo == 2) {
//
//                        //===========================Second Finger Index Update============================================//
//
//                        ContentValues secondTemplate = new ContentValues();
//                        secondTemplate.put("AutoID", strAutoId);
//                        secondTemplate.put("TermplateType", "iso");
//                        secondTemplate.put("TemplateSrNo", "2");
//                        secondTemplate.put("FingerIndex", strSecondFingerIndex);
//
//                        if (!strSecurityLevel.trim().equalsIgnoreCase("Select")) {
//                            secondTemplate.put("SecurityLevel", strSecurityLevel);
//                        } else {
//                            secondTemplate.put("SecurityLevel", "");
//                        }
//
//                        if (!strSecurityLevel.trim().equalsIgnoreCase("Select")) {
//                            secondTemplate.put("VerificationMode", strVerificationMode);
//                        } else {
//                            secondTemplate.put("VerificationMode", "");
//                        }
//
//                        secondTemplate.put("Quality", "");
//                        secondTemplate.put("EnrolledOn", datime);  //date & time at the time of finger enrollment
//                        secondTemplate.put("Template", strFirstFingerDataHex + "00000000");  //adjust 4 byte 252 + 4 = 256
//                        secondTemplate.put("FingerImage", firstFingerFID);
//                        secondTemplate.put("isAadhaarVerifiedYorN", "N");
//
//                        updateFingerStatus = db.update(FINGER_TABLE, secondTemplate, "ID=" + strIdList.get(1), null);
//
//                    }
//
//                    if (updateFingerStatus != -1) {
//                        if (strIdList.size() == 1) {
//                            updateEmpStatus = updateFingerDataToEmpTable(strAutoId, "Yes", "1", strVerificationMode, "N");
//                        } else {
//                            updateEmpStatus = updateFingerDataToEmpTable(strAutoId, "Yes", "2", strVerificationMode, "N");
//                        }
//                    }
//                }
//            }
//
//            if (updateFingerStatus != -1 && updateEmpStatus != -1) {
//                status = true;
//            }
//
//        } catch (Exception e) {
//
//        } finally {
//            if (c != null) {
//                c.close();
//            }
//            if (resData != null) {
//                resData.close();
//            }
//            if (db != null) {
//                db.close();
//            }
//        }

        return status;
    }


    public boolean updateTwoTemplateToDBFromEzeeHr(String strVerificationMode, String strSecurityLevel) {

//        String strAutoId = "";
//        int updateFingerStatus = -1;
//        int updateEmpStatus = -1;
//        Cursor c = null;
//        Cursor resData = null;

        boolean status = false;

//        try {
//
//            Calendar ca = Calendar.getInstance();
//            SimpleDateFormat df = new SimpleDateFormat("ddMMyyyyHHmmss");
//            String datime = df.format(ca.getTime());
//
//            int noOfFingers = FingerDataDetails.getInstance().getNoOfFingers();
//
//            String strEmpId = FingerDataDetails.getInstance().getStrEmpId();
//            String strPaddedEmpId = Utility.paddEmpId(strEmpId);
//
//            String strFirstFingerIndex = FingerDataDetails.getInstance().getStrNewFirstFingerIndex();
//            String strSecondFingerIndex = FingerDataDetails.getInstance().getStrNewSecondFingerIndex();
//
//            byte[] firstFingerFID = FingerDataDetails.getInstance().getFirstFingerFID();
//            String strFirstFingerDataHex = FingerDataDetails.getInstance().getStrFirstFingerDataHex();
//
//            byte[] secondFingerFID = FingerDataDetails.getInstance().getSecondFingerFID();
//            String strSecondFingerDataHex = FingerDataDetails.getInstance().getStrSecondFingerDataHex();
//
//            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
//
//            String selectQuery = "SELECT AutoId FROM " + EMPLOYEE_TABLE + " WHERE " + " EmployeeId='" + strPaddedEmpId + "'";
//            c = db.rawQuery(selectQuery, null);
//
//            if (c != null) {
//                if (c.getCount() > 0) {
//                    while (c.moveToNext()) {
//                        strAutoId = c.getString(0);
//                    }
//                }
//            }
//
//            if (strAutoId.trim().length() > 0) {
//
//                ArrayList <String> strIdList = new ArrayList <String>();
//                String strQuery = "SELECT ID FROM " + FINGER_TABLE + " WHERE " + " AutoId='" + strAutoId + "'";
//                resData = db.rawQuery(strQuery, null);
//
//                if (resData != null) {
//                    if (resData.getCount() > 0) {
//                        while (resData.moveToNext()) {
//                            String strId = Integer.toString(resData.getInt(0));
//                            strIdList.add(strId);
//                        }
//                    }
//
//                }
//
//                if (noOfFingers == 2) {
//
//                    //===========================Both Finger Index Update============================================//
//
//                    ContentValues firstTemplate = new ContentValues();
//                    firstTemplate.put("AutoID", strAutoId);
//                    firstTemplate.put("TermplateType", "iso");
//                    firstTemplate.put("TemplateSrNo", "1");
//                    firstTemplate.put("FingerIndex", strFirstFingerIndex);
//
//                    if (!strSecurityLevel.trim().equalsIgnoreCase("Select")) {
//                        firstTemplate.put("SecurityLevel", strSecurityLevel);
//                    } else {
//                        firstTemplate.put("SecurityLevel", "");
//                    }
//
//                    if (!strSecurityLevel.trim().equalsIgnoreCase("Select")) {
//                        firstTemplate.put("VerificationMode", strVerificationMode);
//                    } else {
//                        firstTemplate.put("VerificationMode", "");
//                    }
//
//                    firstTemplate.put("Quality", "");
//                    firstTemplate.put("EnrolledOn", datime);  //date & time at the time of finger enrollment
//                    firstTemplate.put("Template", strFirstFingerDataHex + "00000000");  //adjust 4 byte 252 + 4 = 256
//                    firstTemplate.put("FingerImage", firstFingerFID);
//                    firstTemplate.put("isAadhaarVerifiedYorN", "N");
//
//                    updateFingerStatus = db.update(FINGER_TABLE, firstTemplate, "ID=" + strIdList.get(0), null);
//
//                    ContentValues secondTemplate = new ContentValues();
//                    secondTemplate.put("AutoID", strAutoId);
//                    secondTemplate.put("TermplateType", "iso");
//                    secondTemplate.put("TemplateSrNo", "2");
//                    secondTemplate.put("FingerIndex", strSecondFingerIndex);
//
//                    if (!strSecurityLevel.trim().equalsIgnoreCase("Select")) {
//                        secondTemplate.put("SecurityLevel", strSecurityLevel);
//                    } else {
//                        secondTemplate.put("SecurityLevel", "");
//                    }
//
//                    if (!strVerificationMode.trim().equalsIgnoreCase("Select")) {
//                        secondTemplate.put("VerificationMode", strVerificationMode);
//                    } else {
//                        secondTemplate.put("VerificationMode", "");
//                    }
//
//                    secondTemplate.put("Quality", "");
//                    secondTemplate.put("EnrolledOn", datime);  //date & time at the time of finger enrollment
//                    secondTemplate.put("Template", strSecondFingerDataHex + "00000000");  //adjust 4 byte 252 + 4 = 256
//                    secondTemplate.put("FingerImage", secondFingerFID);
//                    secondTemplate.put("isAadhaarVerifiedYorN", "N");
//
//                    updateFingerStatus = db.update(FINGER_TABLE, secondTemplate, "ID=" + strIdList.get(1), null);
//
//                    updateEmpStatus = updateFingerDataToEmpTable(strAutoId, "Yes", "2", strVerificationMode, "N");
//
//                }
//            }
//
//            if (updateFingerStatus != -1 && updateEmpStatus != -1) {
//                status = true;
//            }
//
//        } catch (Exception e) {
//
//        } finally {
//            if (c != null) {
//                c.close();
//            }
//            if (resData != null) {
//                resData.close();
//            }
//            if (db != null) {
//                db.close();
//            }
//        }
        return status;
    }

    public int updateFingerDataToEmpTableFromEzeeHr(String strAutoId, String enrollStatus, int noOfFingers, String strVerificationMode, String strIsTemplateAadhaarVerified) {

        int updateStatus = -1;


        try {

            int AutoId = Integer.parseInt(strAutoId);
            ContentValues fingerDetails = new ContentValues();
            fingerDetails.put("EnrollStatus", enrollStatus);
            fingerDetails.put("NosFinger", Integer.toString(noOfFingers));
            fingerDetails.put("VerificationMode", strVerificationMode);
            fingerDetails.put("isTemplateAadhaarVerifiedYorN", strIsTemplateAadhaarVerified);

            if (!db.isOpen())
                db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            updateStatus = db.update(EMPLOYEE_TABLE, fingerDetails, "AutoId=" + AutoId, null);
            Log.d("TEST", "updateFingerDataToEmpTableFromEzeeHr | updateStatus :" + updateStatus);

        } catch (Exception e) {
            Log.d("TEST", "updateFingerDataToEmpTableFromEzeeHr | Exception :" + e);
        }

        return updateStatus;
    }

    public Cursor getEnrolledEmployeeBasicDetails(String empId) {
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select * from " + EMPLOYEE_TABLE + " where EmployeeId='" + Utility.paddEmpId(empId) + "'", null);
            if (resData != null && resData.getCount() > 0) {
                return resData;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    //Sanjay Shyamal
    public ArrayList <BasicEmployeeInfo> getEnrolledSearchList() {

        ArrayList <BasicEmployeeInfo> empSearchList = null;
        Cursor resData = null;
        try {

            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select EmployeeId, CardId, Name, EnrollStatus from " + EMPLOYEE_TABLE, null);
            Log.d("TEST", "Employee Search Data Count:" + resData.getCount());
            if (resData != null && resData.getCount() > 0) {
                empSearchList = new ArrayList <BasicEmployeeInfo>();
                while (resData.moveToNext()) {
                    BasicEmployeeInfo employeeDetails = new BasicEmployeeInfo();
                    employeeDetails.setEmployeeID(resData.getString(0));
                    employeeDetails.setCardID(resData.getString(1));
                    employeeDetails.setEmployeeName(resData.getString(2));
                    employeeDetails.setEnrolledStatus(resData.getString(3));
                    empSearchList.add(employeeDetails);

                }
            }

        } catch (Exception e) {
            Log.e("TEST", "Employee Search Data Failed : " + e);

        } finally {
            if (resData != null) {
                resData.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return empSearchList;
    }

    public ArrayList <String> getEnrolledEmpIdList() {

        ArrayList <String> empIdList = null;
        Cursor resData = null;
        try {

            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select EmployeeId from " + EMPLOYEE_TABLE, null);

            if (resData != null && resData.getCount() > 0) {
                empIdList = new ArrayList <String>();
                while (resData.moveToNext()) {
                    empIdList.add(resData.getString(0).trim());
                }
            }

        } catch (Exception e) {

        } finally {
            if (resData != null) {
                resData.close();
            }
            if (db != null) {
                db.close();
            }
        }

        return empIdList;
    }


    public ArrayList <String> getEmpIdList(String strEmpId) {

        ArrayList <String> empIdList = null;
        Cursor resData = null;

        try {

            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select EmployeeId from " + EMPLOYEE_TABLE + " where EmployeeId Like '%" + strEmpId + "%'", null);
            if (resData != null && resData.getCount() > 0) {
                empIdList = new ArrayList <String>();
                while (resData.moveToNext()) {
                    empIdList.add(resData.getString(0).trim());
                }
            }

        } catch (Exception e) {

        } finally {
            if (resData != null) {
                resData.close();
            }
            if (db != null) {
                db.close();
            }
        }

        return empIdList;

    }

    public ArrayList <String> getEnrolledCardIdList() {

        ArrayList <String> empCardIdList = null;
        Cursor resData = null;

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select CardId from " + EMPLOYEE_TABLE, null);
            if (resData != null && resData.getCount() > 0) {
                empCardIdList = new ArrayList <String>();
                while (resData.moveToNext()) {
                    empCardIdList.add(resData.getString(0).replaceAll("\\G0", " ").trim());
                }
            }

        } catch (Exception e) {

        } finally {
            if (resData != null) {
                resData.close();
            }
            if (db != null) {
                db.close();
            }
        }

        return empCardIdList;

    }


    public ArrayList <String> getCardIdList(String strCardId) {

        ArrayList <String> empCardIdList = null;
        Cursor resData = null;

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select CardId from " + EMPLOYEE_TABLE + " where CardId Like '%" + strCardId + "%'", null);
            if (resData != null && resData.getCount() > 0) {
                empCardIdList = new ArrayList <String>();
                while (resData.moveToNext()) {
                    empCardIdList.add(resData.getString(0).replaceAll("\\G0", " ").trim());
                }
            }

        } catch (Exception e) {

        } finally {
            if (resData != null) {
                resData.close();
            }
            if (db != null) {
                db.close();
            }
        }

        return empCardIdList;

    }


    public ArrayList <String> getEnrolledEmpNameList() {

        ArrayList <String> empNameList = null;
        Cursor resData = null;

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select Name from " + EMPLOYEE_TABLE, null);
            if (resData != null && resData.getCount() > 0) {
                empNameList = new ArrayList <String>();
                while (resData.moveToNext()) {
                    empNameList.add(resData.getString(0).replaceAll("0", ""));
                }
            }

        } catch (Exception e) {

        } finally {
            if (resData != null) {
                resData.close();
            }
            if (db != null) {
                db.close();
            }
        }

        return empNameList;
    }

    public ArrayList <String> getEmpNameList(String strEmpName) {

        ArrayList <String> empNameList = null;
        Cursor resData = null;

        try {

            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select Name from " + EMPLOYEE_TABLE + " where Name Like '" + strEmpName + "%'", null);
            if (resData != null && resData.getCount() > 0) {
                empNameList = new ArrayList <String>();
                while (resData.moveToNext()) {
                    empNameList.add(resData.getString(0).replaceAll("0", ""));
                }
            }

        } catch (Exception e) {

        } finally {
            if (resData != null) {
                resData.close();
            }
            if (db != null) {
                db.close();
            }
        }

        return empNameList;
    }


    public Cursor getEmpDetailsByEmpId(String strPaddedEmpId) {

        Cursor resData = null;

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select AutoId,CardId,Name,Photo from " + EMPLOYEE_TABLE + " where EmployeeId='" + strPaddedEmpId + "'", null);
            if (resData != null && resData.getCount() > 0) {
                return resData;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }


    public Cursor getEmpDetailsByCardId(String strPaddedCardId) {

        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select AutoId,EmployeeId,Name,Photo from " + EMPLOYEE_TABLE + " where CardId='" + strPaddedCardId + "'", null);
            if (resData != null && resData.getCount() > 0) {
                return resData;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public Cursor getEmpDetailsByEmpName(String strEmpName) {

        Cursor resData = null;

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select AutoId,EmployeeId,CardId,Photo from " + EMPLOYEE_TABLE + " where Name='" + strEmpName + "'", null);
            if (resData != null && resData.getCount() > 0) {
                return resData;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }


    public ArrayList <String> getEmployeeFingerDataDetails(int empAutoId) {


        ArrayList <String> fingerDataList = null;
        Cursor resFingerData = null;

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resFingerData = db.rawQuery("select EnrollStatus,NosFinger,VerificationMode,isTemplateAadhaarVerifiedYorN from " + EMPLOYEE_TABLE + " where AutoId='" + empAutoId + "'", null);

            if (resFingerData != null && resFingerData.getCount() > 0) {

                while (resFingerData.moveToNext()) {
                    String strEnrollStatus = resFingerData.getString(0);
                    String strNoOfRecords = resFingerData.getString(1);
                    String strVerificationMode = resFingerData.getString(2);
                    String isTemplateAadhaarVerified = resFingerData.getString(3);

                    if (strEnrollStatus.trim().length() > 0) {
                        fingerDataList = new ArrayList <String>();
                        fingerDataList.add(strEnrollStatus);
                        fingerDataList.add(strNoOfRecords);
                        fingerDataList.add(strVerificationMode);
                        fingerDataList.add(isTemplateAadhaarVerified);
                    }
                }
            }

        } catch (Exception e) {

        } finally {

            if (resFingerData != null) {
                resFingerData.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return fingerDataList;
    }

    public int updateEmployeeData(int AutoId, String strEmpId, String strCardId, String strName, String strBloodGroup, String strSiteCode, String strEnrollStatus, String strEnrollNoFinger, String strMobileNo, String strMailId, String strPin, String strVerification, String strValidUpto, String strBirthday, String strGroupId, String strTrainingCenter, String strBatch, String strIsBlockStatus, String strIsAccess, String strIsLock, String strSmartCardVer, byte[] imageData) {

        int updateStatus = -1;

        try {


            ContentValues initialValues = new ContentValues();

            if (strEmpId.trim().length() < 16) {
                String strPaddedEmpId = "";
                strPaddedEmpId = Utility.paddEmpId(strEmpId);
                initialValues.put("EmployeeID", strPaddedEmpId);
            } else {
                initialValues.put("EmployeeID", strEmpId);
            }

            if (strCardId.trim().length() < 8) {
                String strPaddedCardId = "";
                strPaddedCardId = Utility.paddCardId(strCardId);
                initialValues.put("CardId", strPaddedCardId);
            } else {
                initialValues.put("CardId", strCardId);
            }

            initialValues.put("Name", strName);

            if (!strBloodGroup.trim().equalsIgnoreCase("Select")) {
                initialValues.put("BloodGroup", strBloodGroup);
            } else {
                initialValues.put("BloodGroup", "");
            }

            initialValues.put("SiteCode", strSiteCode);
            initialValues.put("MobileNo", strMobileNo);
            initialValues.put("MailId", strMailId);
            initialValues.put("ValidUpto", strValidUpto);
            initialValues.put("BirthDay", strBirthday);
            initialValues.put("PIN", strPin);
            initialValues.put("GroupId", strGroupId);
            initialValues.put("fkTrainingCenter", strTrainingCenter);
            initialValues.put("fkBatchCenter", strBatch);


            //Non Editable Fields//

            initialValues.put("EnrollStatus", strEnrollStatus);
            initialValues.put("NosFinger", strEnrollNoFinger);
            initialValues.put("SmartCardVersion", strSmartCardVer);

            //Non Editable Fields//


            //Admin Rights//


            if (!strVerification.trim().equalsIgnoreCase("Select")) {
                initialValues.put("VerificationMode", strVerification);
            } else {
                initialValues.put("VerificationMode", "");
            }

            if (!strIsBlockStatus.trim().equalsIgnoreCase("Select")) {
                initialValues.put("IsBlacklisted", strIsBlockStatus);
            } else {
                initialValues.put("IsBlacklisted", "");
            }

            if (!strIsAccess.trim().equalsIgnoreCase("Select")) {
                initialValues.put("IsAccessRightEnabled", strIsAccess);

            } else {
                initialValues.put("IsAccessRightEnabled", "");

            }

            if (!strIsLock.trim().equalsIgnoreCase("Select")) {
                initialValues.put("IsLockOpenWhenAllowed", strIsLock);
            } else {
                initialValues.put("IsLockOpenWhenAllowed", "");

            }

            initialValues.put("Photo", imageData);

            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            updateStatus = db.update(EMPLOYEE_TABLE, initialValues, "AutoId=" + AutoId, null);

        } catch (SQLiteException e) {

        } finally {
            if (db != null) {
                db.close();
            }
        }

        return updateStatus;

    }

    public boolean insertAttendanceData(String strEmpId, String strCardId, String strAttendanceDateTime, String strInOutMode, String strLatitude, String strLongitude, byte[] image) {

        int insertStatus = -1;
        boolean status = false;

        try {

            String[] splitDateAndTime = strAttendanceDateTime.split(" ");

            ContentValues initialValues = new ContentValues();
            initialValues.put("Addr", "00");
            initialValues.put("EstablishmentCode", "00000001");
            initialValues.put("EmployeeID", strEmpId);
            initialValues.put("CardID", strCardId);
            initialValues.put("PunchDate", splitDateAndTime[0].trim());
            initialValues.put("PunchTime", splitDateAndTime[1].replace(":", "").trim());
            initialValues.put("InOutMode", strInOutMode);
            initialValues.put("ReasonCode", "FF");
            initialValues.put("Lat", strLatitude);
            initialValues.put("Long", strLongitude);
            initialValues.put("CanteenCode", "00");
            initialValues.put("SB1", "00");
            initialValues.put("SB2", "00");
            initialValues.put("RFU1", "00");
            initialValues.put("RFU2", "00");
            initialValues.put("RFU3", "00");
            initialValues.put("Uploaded", "00");
            initialValues.put("Image", image);

            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            insertStatus = (int) db.insert(ATTENDANCE_TABLE, null, initialValues);

            if (insertStatus != -1) {
                status = true;
            }

        } catch (SQLiteException e) {

        } finally {
            if (db != null) {
                db.close();
            }
        }

        return status;

    }

    public Cursor getEmployeeDetailsForCard(String strEmpId) {

        Cursor resEmpData = null;

        try {
            String strPaddedEmpId = Utility.paddEmpId(strEmpId);
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resEmpData = db.rawQuery("select AutoId,EmployeeId,CardId,Name,BloodGroup,SiteCode,ValidUpto,BirthDay,SmartCardSerialNo,SmartCardVersion from " + EMPLOYEE_TABLE + " where EmployeeId='" + strPaddedEmpId + "'", null);
            if (resEmpData != null && resEmpData.getCount() > 0) {
                return resEmpData;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;

        } finally {
            if (db != null) {
                db.close();
            }
        }
    }


    public Cursor getFingerDataForCard(int intAutoId) {

        Cursor resFingerData = null;

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resFingerData = db.rawQuery("select SecurityLevel,FingerIndex,VerificationMode,Quality,Template from " + FINGER_TABLE + " where AutoId='" + intAutoId + "'", null);
            if (resFingerData != null && resFingerData.getCount() > 0) {
                return resFingerData;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        } finally {

            if (db != null) {
                db.close();
            }
        }
    }

    public Cursor getSectorAndKeyForWriteCard() {

        Cursor resSectorKeyData = null;

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resSectorKeyData = db.rawQuery("select SectorNo,KeyA,KeyB from " + SECTOR_KEY, null);

            //  Cursor resSectorKeyData=db.rawQuery("select SectorNo,KeyB from SmartKeyNew",null);
            //Cursor resSectorKeyData=db.rawQuery("select SectorNo,KeyB from SmartKeyFortuna",null);

            if (resSectorKeyData != null && resSectorKeyData.getCount() > 0) {
                return resSectorKeyData;
            } else {
                return null;
            }

        } catch (Exception e) {
            return null;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public Cursor getSectorAndKeyForCardInit() {


        Cursor resSectorKeyData = null;

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resSectorKeyData = db.rawQuery("select SectorNo,KeyA,AccessCode,KeyB from " + SECTOR_KEY_CARD_INIT, null);
            if (resSectorKeyData != null && resSectorKeyData.getCount() > 0) {
                return resSectorKeyData;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;

        } finally {
            if (db != null) {
                db.close();
            }
        }
    }


    public Cursor getSectorAndKeyForRC632CardInit() {

        Cursor resSectorKeyData = null;

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resSectorKeyData = db.rawQuery("select SectorNo,KeyA1,AccessCode,KeyB1 from " + SECTOR_KEY_CARD_INIT, null);
            if (resSectorKeyData != null && resSectorKeyData.getCount() > 0) {
                return resSectorKeyData;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;

        } finally {
            if (db != null) {
                db.close();
            }
        }
    }


    public Cursor getSectorAndKeyForReadCard() {

        Cursor resSectorKeyData = null;

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);

            //Cursor resSectorKeyData=db.rawQuery("select SectorNo,KeyA from "+SECTOR_KEY,null);
            //Cursor resSectorKeyData=db.rawQuery("select SectorNo,KeyA from SmartKeyNew",null);
            //Cursor resSectorKeyData=db.rawQuery("select SectorNo,KeyA from SmartKeyFortuna",null);

            resSectorKeyData = db.rawQuery("select SectorNo,KeyB from " + SECTOR_KEY, null);
            if (resSectorKeyData != null && resSectorKeyData.getCount() > 0) {
                return resSectorKeyData;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;

        } finally {

            if (db != null) {
                db.close();
            }
        }
    }


    public Cursor getSectorAndKeyForRC632CardRead() {

        Cursor resSectorKeyData = null;

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);

            //Cursor resSectorKeyData=db.rawQuery("select SectorNo,KeyA from "+SECTOR_KEY,null);
            //Cursor resSectorKeyData=db.rawQuery("select SectorNo,KeyA from SmartKeyNew",null);
            //Cursor resSectorKeyData=db.rawQuery("select SectorNo,KeyA from SmartKeyFortuna",null);

            resSectorKeyData = db.rawQuery("select SectorNo,KeyB1 from " + SECTOR_KEY, null);
            if (resSectorKeyData != null && resSectorKeyData.getCount() > 0) {
                return resSectorKeyData;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;

        } finally {

            if (db != null) {
                db.close();
            }
        }
    }

    public Cursor getSectorAndKeyForRC632CardRefresh() {

        Cursor resSectorKeyData = null;

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);

            resSectorKeyData = db.rawQuery("select SectorNo,KeyA1,KeyB1 from " + SECTOR_KEY, null);
            if (resSectorKeyData != null && resSectorKeyData.getCount() > 0) {
                return resSectorKeyData;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;

        } finally {

            if (db != null) {
                db.close();
            }
        }
    }

    public Cursor getEmployeePhoto(String strEmpId) {

        Cursor resData = null;

        try {
            String strPaddedEmpId = Utility.paddEmpId(strEmpId);
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select Photo from " + EMPLOYEE_TABLE + " where EmployeeId='" + strPaddedEmpId + "'", null);
            if (resData != null && resData.getCount() > 0) {
                return resData;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public String getCardId(String strEmployeeId) {

        String strCardId = "";
        Cursor resData = null;

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select CardId from " + EMPLOYEE_TABLE + " where EmployeeId='" + Utility.paddEmpId(strEmployeeId) + "'", null);
            if (resData != null && resData.getCount() > 0) {
                while (resData.moveToNext()) {
                    strCardId = resData.getString(0).replaceAll("\\G0", " ").trim();
                }
            }

        } catch (Exception e) {

        } finally {
            if (resData != null) {
                resData.close();
            }
            if (db != null) {
                db.close();
            }
        }

        return strCardId;

    }

    public ArrayList <String> getEmployeeFingerData(int AutoId) {

        ArrayList <String> fingerData = null;
        Cursor resFingerData = null;

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resFingerData = db.rawQuery("select FingerIndex,Template from " + FINGER_TABLE + " where AutoId='" + AutoId + "'", null);

            if (resFingerData != null && resFingerData.getCount() > 0) {

                fingerData = new ArrayList <String>();

                while (resFingerData.moveToNext()) {
                    fingerData.add(resFingerData.getString(0));
                    fingerData.add(resFingerData.getString(1));
                }
            }

        } catch (Exception e) {

        } finally {
            if (resFingerData != null) {
                resFingerData.close();
            }
            if (db != null) {
                db.close();
            }
        }

        return fingerData;
    }

    public int saveGroupData(String strGroupName, String strGroupProperty) {

        int insertStatus = -1;

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            ContentValues initialValues = new ContentValues();
            initialValues.put("Name", strGroupName);
            initialValues.put("Property", strGroupProperty);
            insertStatus = (int) db.insert(GROUP_TABLE, null, initialValues);
        } catch (SQLiteException e) {

        } finally {
            if (db != null) {
                db.close();
            }
        }
        return insertStatus;
    }

    public int saveSiteData(String strSiteCode) {

        int insertStatus = -1;

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            ContentValues initialValues = new ContentValues();
            initialValues.put("Code", strSiteCode);
            insertStatus = (int) db.insert(SITE_TABLE, null, initialValues);
        } catch (SQLiteException e) {

        } finally {
            if (db != null) {
                db.close();
            }
        }

        return insertStatus;
    }

    public int saveBatchData(String strBatchNo, String strBatchName) {

        int insertStatus = -1;
        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            ContentValues initialValues = new ContentValues();
            initialValues.put("BatchNo", strBatchNo);
            initialValues.put("BatchName", strBatchName);
            insertStatus = (int) db.insert(BATCH_TABLE, null, initialValues);

        } catch (SQLiteException e) {

        } finally {
            if (db != null) {
                db.close();
            }
        }

        return insertStatus;
    }

    public int saveTrainingData(String strTrainingNo, String strTrainingName) {

        int insertStatus = -1;

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            ContentValues initialValues = new ContentValues();
            initialValues.put("CenterNo", strTrainingNo);
            initialValues.put("CenterName", strTrainingName);
            insertStatus = (int) db.insert(TRAINING_TABLE, null, initialValues);

        } catch (SQLiteException e) {

        } finally {

            if (db != null) {
                db.close();
            }
        }

        return insertStatus;
    }

    public void getGroupNames(String[] groupNames, int[] pkGroupNames) {

        Cursor resGroupData = null;
        int i = 1;

        int noOfRecords = 0;

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resGroupData = db.rawQuery("select ID,Name from " + GROUP_TABLE, null);
            if (resGroupData != null && resGroupData.getCount() > 0) {
                noOfRecords = resGroupData.getCount();
                if (noOfRecords > 0) {
                    while (resGroupData.moveToNext()) {
                        pkGroupNames[i] = Integer.parseInt(resGroupData.getString(0));
                        groupNames[i] = resGroupData.getString(1);
                        i++;
                    }
                }
            }

        } catch (Exception e) {

        } finally {

            if (resGroupData != null) {
                resGroupData.close();
            }
            if (db != null) {
                db.close();
            }
        }
    }

    public int getGroupNamesCount() {

        int noOfRecords = 0;
        Cursor resGroupData = null;

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resGroupData = db.rawQuery("select * from " + GROUP_TABLE, null);
            if (resGroupData != null && resGroupData.getCount() > 0) {
                noOfRecords = resGroupData.getCount();
            }

        } catch (Exception e) {

        } finally {
            if (resGroupData != null) {
                resGroupData.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return noOfRecords;
    }

    public int getSiteCodeCount() {

        int noOfRecords = 0;
        Cursor resSiteCodeData = null;

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resSiteCodeData = db.rawQuery("select * from " + SITE_TABLE, null);
            if (resSiteCodeData != null && resSiteCodeData.getCount() > 0) {
                noOfRecords = resSiteCodeData.getCount();
            }

        } catch (Exception e) {

        } finally {
            if (resSiteCodeData != null) {
                resSiteCodeData.close();
            }
            if (db != null) {
                db.close();
            }
        }

        return noOfRecords;
    }

    public void getSiteCodes(String[] siteCodes, int[] pkSiteCodes) {
        Cursor resSiteCodeData = null;
        int i = 1;
        int noOfRecords = 0;
        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resSiteCodeData = db.rawQuery("select ID,Code from " + SITE_TABLE, null);
            if (resSiteCodeData != null && resSiteCodeData.getCount() > 0) {
                noOfRecords = resSiteCodeData.getCount();
                if (noOfRecords > 0) {
                    while (resSiteCodeData.moveToNext()) {
                        pkSiteCodes[i] = Integer.parseInt(resSiteCodeData.getString(0));
                        siteCodes[i] = resSiteCodeData.getString(1);
                        i++;
                    }
                }
            }
        } catch (Exception e) {

        } finally {
            if (resSiteCodeData != null) {
                resSiteCodeData.close();
            }
            if (db != null) {
                db.close();
            }
        }
    }

    //=========================Done As on 10-07-2017======================//


    public int getTrainingCenterCount() {

        int noOfRecords = 0;
        Cursor resTrainingCenterData = null;

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resTrainingCenterData = db.rawQuery("select * from " + TRAINING_TABLE, null);

            if (resTrainingCenterData != null && resTrainingCenterData.getCount() > 0) {
                noOfRecords = resTrainingCenterData.getCount();
            }

        } catch (Exception e) {

        } finally {
            if (resTrainingCenterData != null) {
                resTrainingCenterData.close();
            }
            if (db != null) {
                db.close();
            }
        }

        return noOfRecords;
    }

    public void getTrainingCenters(String[] trainingCenters, int[] pkTrainingCenters) {

        Cursor resTrainingCenterData = null;
        int i = 1;

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resTrainingCenterData = db.rawQuery("select ID,CenterName from " + TRAINING_TABLE, null);
            if (resTrainingCenterData != null && resTrainingCenterData.getCount() > 0) {
                while (resTrainingCenterData.moveToNext()) {
                    pkTrainingCenters[i] = Integer.parseInt(resTrainingCenterData.getString(0));
                    trainingCenters[i] = resTrainingCenterData.getString(1);
                    i++;
                }
            }
        } catch (Exception e) {

        } finally {

            if (resTrainingCenterData != null) {
                resTrainingCenterData.close();
            }
            if (db != null) {
                db.close();
            }
        }
    }

    public int getBatchNamesCount() {

        int noOfRecords = 0;
        Cursor resBatchNamesData = null;

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resBatchNamesData = db.rawQuery("select * from " + BATCH_TABLE, null);
            if (resBatchNamesData != null && resBatchNamesData.getCount() > 0) {
                noOfRecords = resBatchNamesData.getCount();
            }
        } catch (Exception e) {

        } finally {

            if (resBatchNamesData != null) {
                resBatchNamesData.close();
            }
            if (db != null) {
                db.close();
            }
        }

        return noOfRecords;
    }

    public void getBatchNames(String[] batchNames, int[] pkBatchNames) {

        Cursor resBatchNamesData = null;
        int i = 1;
        int noOfRecords = 0;

        try {

            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resBatchNamesData = db.rawQuery("select ID,BatchName from " + BATCH_TABLE, null);
            if (resBatchNamesData != null && resBatchNamesData.getCount() > 0) {
                noOfRecords = resBatchNamesData.getCount();
                if (noOfRecords > 0) {
                    while (resBatchNamesData.moveToNext()) {
                        pkBatchNames[i] = Integer.parseInt(resBatchNamesData.getString(0));
                        batchNames[i] = resBatchNamesData.getString(1);
                        i++;
                    }
                }
            }

        } catch (Exception e) {


        } finally {

            if (resBatchNamesData != null) {
                resBatchNamesData.close();
            }
            if (db != null) {
                db.close();
            }
        }
    }


    public String getGroupNameById(String groupId) {
        String strGroupName = "";
        Cursor resData = null;
        try {

            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select Name from " + GROUP_TABLE + " where ID='" + groupId + "'", null);
            if (resData != null && resData.getCount() > 0) {
                while (resData.moveToNext()) {
                    strGroupName = resData.getString(0);
                }
            }
        } catch (Exception e) {

        } finally {
            if (resData != null) {
                resData.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return strGroupName;
    }


    public String getSiteCodeById(String siteId) {
        String strSiteCode = "";
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select Code from " + SITE_TABLE + " where ID='" + siteId + "'", null);
            if (resData != null && resData.getCount() > 0) {
                while (resData.moveToNext()) {
                    strSiteCode = resData.getString(0);
                }
            }
        } catch (Exception e) {

        } finally {
            if (resData != null) {
                resData.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return strSiteCode;
    }


    public String getTrainingCenterNameById(String trainingCode) {


        String strTrainingCenterName = "";
        Cursor resData = null;

        try {

            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select CenterName from " + TRAINING_TABLE + " where ID='" + trainingCode + "'", null);
            if (resData != null && resData.getCount() > 0) {
                while (resData.moveToNext()) {
                    strTrainingCenterName = resData.getString(0);
                }
            }

        } catch (Exception e) {

        } finally {

            if (resData != null) {
                resData.close();
            }
            if (db != null) {
                db.close();
            }
        }

        return strTrainingCenterName;
    }


    public String getBatchNameById(String batchCode) {

        String strBatchName = "";
        Cursor resData = null;

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select BatchName from " + BATCH_TABLE + " where ID='" + batchCode + "'", null);
            if (resData != null && resData.getCount() > 0) {
                while (resData.moveToNext()) {
                    strBatchName = resData.getString(0);
                }
            }

        } catch (Exception e) {


        } finally {

            if (resData != null) {
                resData.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return strBatchName;

    }

    public boolean deleteEmployeeFingerDetails(int AutoId) {

//        int deleteStatus = -1;
//        int updateStatus = -1;
//        String strCondition = "";
        boolean status = false;

//        try {
//            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
//            strCondition = "AutoId" + "=" + AutoId + "";
//            deleteStatus = db.delete(FINGER_TABLE, strCondition, null);
//            if (deleteStatus != -1) {
//                updateStatus = updateFingerDataToEmpTable(Integer.toString(AutoId), "", "", "", "");
//                if (updateStatus != -1) {
//                    status = true;
//                }
//            }
//
//        } catch (SQLiteException e) {
//
//        } finally {
//            if (db != null) {
//                db.close();
//            }
//        }

        return status;
    }


    public boolean deleteEmployeeDetails(int AutoId) {

        int deleteStatus = -1;
        int updateStatus = -1;
        String strCondition = "";
        boolean status = false;

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            strCondition = "AutoId" + "=" + AutoId + "";
            deleteStatus = db.delete(EMPLOYEE_TABLE, strCondition, null);
            if (deleteStatus != -1) {
                deleteStatus = db.delete(FINGER_TABLE, strCondition, null);
                if (deleteStatus != -1) {
                    status = true;
//                    updateStatus = updateFingerDataToEmpTable(Integer.toString(AutoId), "", "", "", "");
//                    if (updateStatus != -1) {
//                        status = true;
//                    }
                }
            }
        } catch (SQLiteException e) {

        } finally {
            if (db != null) {
                db.close();
            }
        }

        return status;
    }

    public int getAutoIdByEmpId(String strEmpId) {

        int AutoId = -1;
        Cursor resData = null;
        String strPaddedEmpId = "";

        try {
            strPaddedEmpId = Utility.paddEmpId(strEmpId);
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select AutoId from " + EMPLOYEE_TABLE + " where EmployeeId='" + strPaddedEmpId + "'", null);
            if (resData != null && resData.getCount() > 0) {
                while (resData.moveToNext()) {
                    AutoId = resData.getInt(0);
                }
            }

        } catch (Exception e) {

        } finally {

            if (resData != null) {
                resData.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return AutoId;
    }

    public Cursor getEmployeeDetails(String strEmpId) {

        Cursor resData = null;

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select EmployeeId,CardId,Name,BloodGroup,SiteCode,MobileNo,MailId,PIN,ValidUpto,BirthDay,GroupId,SmartCardVersion,Photo,EnrollStatus,NosFinger from " + EMPLOYEE_TABLE + " where EmployeeId='" + strEmpId + "'", null);
            if (resData != null && resData.getCount() > 0) {
                return resData;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }


    public ArrayList <String> getFingerDataForAadhaarVerfication() {

        ArrayList <String> fingerData = null;
        Cursor resData = null;

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("SELECT FingerIndex,Template FROM fingertemplatex where AutoId='3' and TemplateSrNo='1'", null);
            if (resData != null && resData.getCount() > 0) {
                fingerData = new ArrayList <String>();
                while (resData.moveToNext()) {
                    fingerData.add(resData.getString(0));
                    fingerData.add(resData.getString(1));
                }
            }

        } catch (Exception e) {

        } finally {
            if (resData != null) {
                resData.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return fingerData;
    }


    public int insertToAadhaarTable(String strEmpId, String strCardId, String strName, String strAadhaarId) {

        int insertStatus = -1;

        try {
            ContentValues initialValues = new ContentValues();

            if (strEmpId.trim().length() < 16) {
                String strPaddedEmpId = "";
                strPaddedEmpId = Utility.paddEmpId(strEmpId);
                initialValues.put("EmployeeID", strPaddedEmpId);
            } else {
                initialValues.put("EmployeeID", strEmpId);
            }

            if (strCardId.trim().length() < 8) {
                String strPaddedCardId = "";
                strPaddedCardId = Utility.paddCardId(strCardId);
                initialValues.put("CardId", strPaddedCardId);
            } else {
                initialValues.put("CardId", strCardId);
            }

            initialValues.put("Name", strName);
            initialValues.put("AadhaarId", strAadhaarId);

            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            insertStatus = (int) db.insert(AADHAAR_TABLE, null, initialValues);

        } catch (SQLiteException e) {

        } finally {
            if (db != null) {
                db.close();
            }
        }

        return insertStatus;

    }


    public int updateToAadhaarAuthTable(int AutoId, String strAadhaarId) {

        int updateStatus = -1;

        try {
            ContentValues initialValues = new ContentValues();
            initialValues.put("AadhaarId", strAadhaarId);
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            updateStatus = db.update(AADHAARAUTH_TABLE, initialValues, "AutoId=" + AutoId, null);
        } catch (SQLiteException e) {

        } finally {
            if (db != null) {
                db.close();
            }
        }
        return updateStatus;
    }

    public int isDataAvailableInAadhaarTable(String strAadhaarId) {

        int AutoId = -1;
        Cursor resData = null;

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select AutoId from " + AADHAARAUTH_TABLE + " where AadhaarId='" + strAadhaarId + "'", null);
            if (resData != null && resData.getCount() > 0) {
                while (resData.moveToNext()) {
                    AutoId = resData.getInt(0);
                }
            }
        } catch (Exception e) {

        } finally {
            if (resData != null) {
                resData.close();
            }
            if (db != null) {
                db.close();
            }
        }

        return AutoId;

    }


    public int isDataAvailableInAadhaarTableByEmpId(int empId) {

        int AutoId = -1;
        Cursor resData = null;

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select AutoId from " + AADHAARAUTH_TABLE + " where fkEmpId='" + Integer.toString(empId) + "'", null);
            if (resData != null && resData.getCount() > 0) {
                while (resData.moveToNext()) {
                    AutoId = resData.getInt(0);
                }
            }
        } catch (Exception e) {

        } finally {
            if (resData != null) {
                resData.close();
            }
            if (db != null) {
                db.close();
            }
        }

        return AutoId;

    }


    public String getAadhaarId(int empAutoId) {

        Cursor resData = null;
        String strAdhaarId = "";

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select AadhaarId from " + AADHAARAUTH_TABLE + " where fkEmpId='" + Integer.toString(empAutoId) + "'", null);
            if (resData != null && resData.getCount() > 0) {
                while (resData.moveToNext()) {
                    strAdhaarId = resData.getString(0);
                }
            }

        } catch (Exception e) {

        } finally {
            if (resData != null) {
                resData.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return strAdhaarId;
    }

    public String isAadhaarIdEnrolled(String strAadhaarId) {

        Cursor resData = null;
        String autoId = "";

        try {

            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select AutoId from " + AADHAARAUTH_TABLE + " where AadhaarId='" + strAadhaarId + "'", null);
            if (resData != null && resData.getCount() > 0) {
                while (resData.moveToNext()) {
                    autoId = resData.getString(0).trim();
                }
            }

        } catch (Exception e) {

        } finally {
            if (resData != null) {
                resData.close();
            }
            if (db != null) {
                db.close();
            }
        }

        return autoId;

    }

    public boolean getEmpDetailsByAadhaarId(String strAadhaarId, ArrayList <String> list) {

        Cursor resData = null;
        String autoId = "";
        boolean status = false;

        try {

            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select fkEmpId from " + AADHAARAUTH_TABLE + " where AadhaarId='" + strAadhaarId + "'", null);
            if (resData != null && resData.getCount() > 0) {
                while (resData.moveToNext()) {
                    autoId = resData.getString(0).trim();
                }

                if (autoId.trim().length() > 0) {
                    resData = db.rawQuery("select EmployeeId,CardId,Name from " + EMPLOYEE_TABLE + " where AutoId='" + autoId + "'", null);
                    if (resData != null && resData.getCount() > 0) {
                        while (resData.moveToNext()) {
                            list.add(resData.getString(0).trim().trim());
                            list.add(resData.getString(1).trim().replaceAll("\\G0", " ").trim());
                            list.add(resData.getString(2).trim());
                        }
                    }
                }
            }
            if (list.size() > 0) {
                status = true;
            }

        } catch (Exception e) {

        } finally {
            if (resData != null) {
                resData.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return status;
    }

    public ArrayList <String> getAadhaarDetails(String strAadhaarId, ArrayList <String> list) {

        Cursor resData = null;

        try {

            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select EmployeeId,CardId,Name from " + AADHAAR_TABLE + " where AadhaarId='" + strAadhaarId + "'", null);
            if (resData != null && resData.getCount() > 0) {
                while (resData.moveToNext()) {
                    list.add(resData.getString(0).trim());
                    list.add(resData.getString(1).trim());
                    list.add(resData.getString(2).trim());
                }
            }

        } catch (Exception e) {

        } finally {
            if (resData != null) {
                resData.close();
            }
            if (db != null) {
                db.close();
            }
        }

        return list;
    }

    public int insertToAadhaarAuthTable(int pkEmpId, String strAadhaarId) {

        int insertStatus = -1;

        try {
            ContentValues initialValues = new ContentValues();
            initialValues.put("fkEmpId", Integer.toString(pkEmpId).trim());
            initialValues.put("AadhaarId", strAadhaarId);
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            insertStatus = (int) db.insert(AADHAARAUTH_TABLE, null, initialValues);
        } catch (SQLiteException e) {

        } finally {
            if (db != null) {
                db.close();
            }
        }

        return insertStatus;
    }

    public int updateAadhaarAuthTbl(String strFinger, int insertId, String aadhaarEnrolledId) {

        int updateStatus = -1;

        try {
            ContentValues initialValues = null;
            if (strFinger.equalsIgnoreCase("first")) {
                initialValues = new ContentValues();
                initialValues.put("fkFirstFingerId", insertId);
                updateStatus = db.update(AADHAARAUTH_TABLE, initialValues, "AutoId=" + aadhaarEnrolledId, null);
            }

            if (strFinger.equalsIgnoreCase("second")) {

                initialValues = new ContentValues();
                initialValues.put("fkSecondFingerId", insertId);
                updateStatus = db.update(AADHAARAUTH_TABLE, initialValues, "AutoId=" + aadhaarEnrolledId, null);
            }

        } catch (SQLiteException e) {

        }

        return updateStatus;
    }


    public String getFPEnrollMode() {

        Cursor resData = null;
        String strMode = "";

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("SELECT Mode FROM " + FINGER_ENROLL_MODE_TABLE + " WHERE  AutoId=(SELECT MAX(AutoId) FROM " + FINGER_ENROLL_MODE_TABLE + ")", null);
            if (resData != null && resData.getCount() > 0) {
                while (resData.moveToNext()) {
                    strMode = resData.getString(0).trim();
                }
            }
        } catch (Exception e) {
            return strMode;

        } finally {
            if (resData != null) {
                resData.close();
            }
            if (db != null) {
                db.close();
            }
        }

        return strMode;

    }

    public int insertFingerEnrollMode(String mode, String strDateTime) {

        int insertStatus = -1;

        try {
            ContentValues initialValues = new ContentValues();
            initialValues.put("Mode", mode);
            initialValues.put("ModeChangeOn", strDateTime);
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            insertStatus = (int) db.insert(FINGER_ENROLL_MODE_TABLE, null, initialValues);
        } catch (SQLiteException e) {

        } finally {
            if (db != null) {
                db.close();
            }
        }
        return insertStatus;
    }

    public int insertCardVerficationPin(String strPin) {


        Cursor c = null;

        // Calendar ca = Calendar.getInstance();
        // SimpleDateFormat df = new SimpleDateFormat("ddMMyyyyHHmmss");

        int insertStatus = -1;
//        String strAutoId = "";
//        String strEmpId = FingerDataDetails.getInstance().getStrEmpId();
//        String strPaddedEmpId = "";

//        try {
//            strPaddedEmpId = Utility.paddEmpId(strEmpId);
//            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
//            String selectQuery = "SELECT AutoId FROM " + EMPLOYEE_TABLE + " WHERE " + " EmployeeId='" + strPaddedEmpId + "'";
//            c = db.rawQuery(selectQuery, null);
//            if (c != null) {
//                if (c.getCount() > 0) {
//                    while (c.moveToNext()) {
//                        strAutoId = c.getString(0);
//                    }
//                }
//            }
//            if (strAutoId.trim().length() > 0) {
//                ContentValues initialValues = new ContentValues();
//                initialValues.put("AutoID", strAutoId);
//                initialValues.put("CardPin", strPin);
//                insertStatus = (int) db.insert(CARD_VER_PIN_TABLE, null, initialValues);
//            }
//
//        } catch (Exception e) {
//
//        } finally {
//            if (c != null) {
//                c.close();
//            }
//            if (db != null) {
//                db.close();
//            }
//        }

        return insertStatus;
    }

    public String getCardPinForVerification(int autoId) {

        Cursor resData = null;
        String strCardPin = "";

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("SELECT CardPin FROM " + CARD_VER_PIN_TABLE + " WHERE  AutoId=" + Integer.toString(autoId), null);
            if (resData != null && resData.getCount() > 0) {
                while (resData.moveToNext()) {
                    strCardPin = resData.getString(0).trim();
                }
            }
        } catch (Exception e) {


        } finally {
            if (resData != null) {
                resData.close();
            }
            if (db != null) {
                db.close();
            }
        }

        return strCardPin;
    }

    public String getEmpIdByAadhaarId(String strAadhaarId) {

        Cursor c = null;
        String strEmpAutoId = "";
        String strEmpId = "";
        String strQuery = "";

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            strQuery = "SELECT fkEmpId FROM " + AADHAARAUTH_TABLE + " WHERE " + " AadhaarId='" + strAadhaarId + "'";
            c = db.rawQuery(strQuery, null);
            if (c != null) {
                if (c.getCount() > 0) {
                    while (c.moveToNext()) {
                        strEmpAutoId = c.getString(0);
                    }
                }
            }
            if (strEmpAutoId.trim().length() > 0) {
                strQuery = "Select EmployeeId from " + EMPLOYEE_TABLE + " where AutoId=" + Integer.parseInt(strEmpAutoId);
                c = db.rawQuery(strQuery, null);
                if (c != null) {
                    if (c.getCount() > 0) {
                        while (c.moveToNext()) {
                            strEmpId = c.getString(0).trim();
                        }
                    }
                }
            }
        } catch (Exception e) {

        } finally {
            if (c != null) {
                c.close();
            }
            if (db != null) {
                db.close();
            }
        }

        return strEmpId;

    }

    public int insertModeTime(String strDigitStartTime, String strDigitEndTime, String strInOutMode) {

        int insertStatus = -1;

        try {
            ContentValues initialValues = new ContentValues();
            initialValues.put("FromTime", strDigitStartTime);
            initialValues.put("ToTime", strDigitEndTime);
            initialValues.put("InOutMode", strInOutMode);
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            insertStatus = (int) db.insert(IN_OUT_MODE_TABLE, null, initialValues);
        } catch (SQLiteException e) {

        } finally {
            if (db != null) {
                db.close();
            }
        }

        return insertStatus;
    }

    public ArrayList <String> getOutTimeRange() {

        Cursor resData = null;
        ArrayList <String> list = null;

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select FromTime,ToTime from " + IN_OUT_MODE_TABLE + " where InOutMode='01' ", null);
            if (resData != null && resData.getCount() > 0) {
                list = new ArrayList <String>();
                while (resData.moveToNext()) {
                    list.add(resData.getString(0).trim());
                    list.add(resData.getString(1).trim());
                }
            }
        } catch (Exception e) {

        } finally {
            if (resData != null) {
                resData.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return list;
    }

    public ArrayList getInTimeRange() {

        Cursor resData = null;
        ArrayList <String> list = null;

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select FromTime,ToTime from " + IN_OUT_MODE_TABLE + " where InOutMode='00' ", null);
            if (resData != null && resData.getCount() > 0) {
                list = new ArrayList <String>();
                while (resData.moveToNext()) {
                    list.add(resData.getString(0).trim());
                    list.add(resData.getString(1).trim());
                }
            }

        } catch (Exception e) {

        } finally {
            if (resData != null) {
                resData.close();
            }
            if (db != null) {
                db.close();
            }
        }

        return list;
    }

    public int deleteInModeTime(String strMode) {

        int deleteStaus = -1;
        String strCondition = "";

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            strCondition = "InOutMode" + "='" + strMode + "'";
            deleteStaus = db.delete(IN_OUT_MODE_TABLE, strCondition, null);
        } catch (SQLiteException e) {

        } finally {
            if (db != null) {
                db.close();
            }
        }
        return deleteStaus;
    }

    public ArrayList getAtServerIPPort() {

        Cursor resData = null;
        ArrayList <String> list = null;
        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select IPAddress,Port from " + ATTENDANCE_SERVER_TABLE, null);
            if (resData != null && resData.getCount() > 0) {
                list = new ArrayList <String>();
                while (resData.moveToNext()) {
                    list.add(resData.getString(0).trim());
                    list.add(resData.getString(1).trim());
                }
            }

        } catch (Exception e) {

        } finally {
            if (resData != null) {
                resData.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return list;
    }

    public int insertAtServerDetails(String strAaServerIP, String strAaServerPort, String strServerDomain, String strUrl) {
        int insertStatus = -1;
        try {
            ContentValues initialValues = new ContentValues();
            initialValues.put("IPAddress", strAaServerIP);
            initialValues.put("Port", strAaServerPort);
            initialValues.put("Domain", strServerDomain);
            initialValues.put("Url", strUrl);
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            insertStatus = (int) db.insert(ATTENDANCE_SERVER_TABLE, null, initialValues);
        } catch (SQLiteException e) {

        } finally {
            if (db != null) {
                db.close();
            }
        }
        return insertStatus;
    }

    public ArrayList getAaServerIPPort() {

        Cursor resData = null;
        ArrayList <String> list = null;

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select IPAddress,Port from " + AADHAAR_SERVER_TABLE, null);
            if (resData != null && resData.getCount() > 0) {
                list = new ArrayList <String>();
                while (resData.moveToNext()) {
                    list.add(resData.getString(0).trim());
                    list.add(resData.getString(1).trim());
                }
            }
        } catch (Exception e) {

        } finally {
            if (resData != null) {
                resData.close();
            }
            if (db != null) {
                db.close();
            }
        }

        return list;

    }

    public int insertAaServerDetails(String strAtServerIP, String strAtServerPort, String strServerDomain, String strUrl) {

        int insertStatus = -1;

        try {
            ContentValues initialValues = new ContentValues();
            initialValues.put("IPAddress", strAtServerIP);
            initialValues.put("Port", strAtServerPort);
            initialValues.put("Domain", strServerDomain);
            initialValues.put("Url", strUrl);


            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            insertStatus = (int) db.insert(AADHAAR_SERVER_TABLE, null, initialValues);

        } catch (SQLiteException e) {

        } finally {
            if (db != null) {
                db.close();
            }
        }
        return insertStatus;
    }

    public int deleteAtServerIPPort() {

        int deleteStaus = -1;

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            deleteStaus = db.delete(ATTENDANCE_SERVER_TABLE, "", null);
        } catch (SQLiteException e) {

        } finally {
            if (db != null) {
                db.close();
            }
        }
        return deleteStaus;
    }

    public int deleteAaServerIPPort() {

        int deleteStaus = -1;

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            deleteStaus = db.delete(AADHAAR_SERVER_TABLE, "", null);
        } catch (SQLiteException e) {

        } finally {
            if (db != null) {
                db.close();
            }
        }

        return deleteStaus;
    }
    //added by sanjay shyamal
    /*public ArrayList<String> getAttendanceData()
    {
        ArrayList<String> attendance_data = null;
        Cursor cursor = null;


        try
        {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            cursor = db.rawQuery("select CardID from " + ATTENDANCE_TABLE + " where Uploaded='00' limit 50",null);

            Log.d("TEST", "Attendance Data Count:" + cursor.getCount());

            *//*if (cursor != null)
            {
                cursor.moveToFirst();
            }*//*


                //return  cursor;
                while (cursor.moveToNext())
                {
                    attendance_data.add(cursor.getString(0).trim());
                    Log.d("TEST", "Attendance Data Loop"+attendance_data.size());
                }


        }
        catch (Exception ex)
        {
            Log.d("TEST", "Attendance Data Issue : "+ex);
        }
        finally {
            if (db!=null)
            db.close();
        }

        return attendance_data;
    }*/

    public String getAttendanceDataForPM() {

        Cursor resData = null;
        ArrayList <String> list = null;
        String strJSON = "";
        List <JSONObject> totalAttendanceData = new ArrayList <JSONObject>();
        String strCondition = "";

        int status = -1;

        try {

            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            resData = db.rawQuery("select ID,Addr,EstablishmentCode,EmployeeID,CardID,PunchDate,PunchTime,InOutMode,ReasonCode,Lat,Long from " + ATTENDANCE_TABLE + " where Uploaded='00' limit 50", null);

            Log.d("TEST", "Attendance Data Count:" + resData.getCount());

            if (resData != null && resData.getCount() > 0) {

                while (resData.moveToNext()) {

                    JSONObject eachJsonData = new JSONObject();
                    int id = resData.getInt(0);

                    eachJsonData.put("ADD", resData.getString(1));
                    eachJsonData.put("ESTBCODE", resData.getString(2));
                    eachJsonData.put("EID", resData.getString(3));
                    eachJsonData.put("CID", resData.getString(4));
                    eachJsonData.put("DATE", resData.getString(5));
                    eachJsonData.put("TIME", resData.getString(6));
                    eachJsonData.put("IOMODE", resData.getString(7));
                    eachJsonData.put("REASONCODE", resData.getString(8));
                    eachJsonData.put("LAT", resData.getString(9));
                    eachJsonData.put("LONG", resData.getString(10));

                    if (id > 0) {
                        ContentValues initialValues = new ContentValues();
                        initialValues.put("IsDataFetched", "Y");
                        strCondition = "ID" + "=" + id;
                        status = db.update(ATTENDANCE_TABLE, initialValues, strCondition, null);
                    }

                    totalAttendanceData.add(eachJsonData);
                }

                JSONObject obj = new JSONObject();
                obj.put("TokenNo", "Deb123456");
                obj.put("CPUID", "Deb123456");
                obj.put("CommandID", "05");
                obj.put("IsEncrypted", "0");
                obj.put("CommandString", totalAttendanceData);

                strJSON = obj.toString();
            }


        } catch (Exception e) {

        } finally {
            if (resData != null) {
                resData.close();
            }
            if (db != null) {
                db.close();
            }
        }

        return strJSON;
    }

    public int updateAttendanceTable() {

        ContentValues initialValues = null;
        String strCondition = "";
        int updateStatus = -1;

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            initialValues = new ContentValues();
            initialValues.put("Uploaded", "01");
            strCondition = "IsDataFetched" + "='Y'";
            updateStatus = db.update(ATTENDANCE_TABLE, initialValues, strCondition, null);
        } catch (Exception e) {

        } finally {
            if (db != null) {
                db.close();
            }
        }

        return updateStatus;
    }

    public int updateFingerTemplateTable(String fingerId, String strDate) {

        ContentValues initialValues = null;
        int updateStatus = -1;

        try {

            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            initialValues = new ContentValues();
            initialValues.put("UploadedOn", strDate);
            initialValues.put("IsUpdatedToServer", "1");

            updateStatus = db.update(FINGER_TABLE, initialValues, "Id='" + fingerId + "'", null);

        } catch (Exception e) {

        } finally {
            if (db != null) {
                db.close();
            }
        }

        return updateStatus;
    }

    public int getNoOfRecordsToBeSend() {

        Cursor resData = null;
        int noOfRecords = 0;

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select count(*) from " + ATTENDANCE_TABLE + " where Uploaded='00'", null);
            if (resData != null && resData.getCount() > 0) {
                while (resData.moveToNext()) {
                    noOfRecords = resData.getInt(0);
                }
            }

        } catch (Exception e) {

        } finally {
            if (resData != null) {
                resData.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return noOfRecords;
    }

    public int insertUserDetails(String strName, String strAadhaarId, String strMobileNo, String strEmailId, String strUsername, String strPassword, String strIsAdmin, byte[] byteimage) {

//        CREATE TABLE UserM(
//                AutoId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL ,
//                Name TEXT NOT NULL  DEFAULT (null) ,
//                AadhaarId TEXT(12) DEFAULT(null),
//                MobileNo TEXT DEFAULT (null) ,
//                MailId TEXT DEFAULT (null) ,
//                UserName TEXT DEFAULT(null),
//                Password TEXT DEFAULT(null),
//                isAdmin TEXT(1) DEFAULT(null),
//                Photo BLOB DEFAULT (null))


        int insertStatus = -1;

        try {
            ContentValues initialValues = new ContentValues();
            initialValues.put("Name", strName);
            initialValues.put("AadhaarId", strAadhaarId);
            initialValues.put("MobileNo", strMobileNo);
            initialValues.put("MailId", strEmailId);
            initialValues.put("UserName", strUsername);
            initialValues.put("Password", strPassword);
            initialValues.put("isAdmin", strIsAdmin);
            initialValues.put("Photo", byteimage);

            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            insertStatus = (int) db.insert(USER_TABLE, null, initialValues);

        } catch (SQLiteException e) {

        } finally {
            if (db != null) {
                db.close();
            }
        }

        return insertStatus;
    }

    public Cursor getUserData(String strUsername, String strPassword) {

        Cursor resData = null;

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select AutoId,Name,Photo,isAdmin from " + USER_TABLE + " where UserName='" + strUsername + "' and Password='" + strPassword + "'", null);
            if (resData != null && resData.getCount() > 0) {
                return resData;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public boolean isPasswordValid(String strPassword) {

        Cursor resData = null;

        boolean isValid = false;

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select AutoId from " + USER_TABLE + " where Password='" + strPassword + "'", null);
            if (resData != null && resData.getCount() > 0) {
                isValid = true;
            }
        } catch (Exception e) {

        } finally {
            if (db != null) {
                db.close();
            }
        }

        return isValid;
    }

    public boolean checkIsCardHotListed(String strCardSerialNo) {

        boolean isCardHotlisted = false;
        Cursor resData = null;

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select AutoId from " + HOTLIST_TABLE + " where CSN='" + strCardSerialNo + "'", null);
            if (resData != null && resData.getCount() > 0) {
                isCardHotlisted = true;
            }

        } catch (Exception e) {

        } finally {
            if (resData != null) {
                resData.close();
            }
            if (db != null) {
                db.close();
            }
        }

        return isCardHotlisted;
    }


    public boolean checkIsCardCreatedLocal(String strCSN, String strCardId) {

        boolean isCardCreatedLocal = false;
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select AutoId from " + EMPLOYEE_TABLE + " where SmartCardSerialNo='" + strCSN + "' and CardId='" + Utility.paddCardId(strCardId) + "'", null);
            if (resData != null && resData.getCount() > 0) {
                isCardCreatedLocal = true;
            }
        } catch (Exception e) {
        } finally {
            if (resData != null) {
                resData.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return isCardCreatedLocal;
    }

    public int insertIntoSmartCardOperationLog(int loginId, int empAutoId, String strCardSerialNo, String strOldCardId, int oldCardVersion, String strNewCardId, int newCardVersion, String strOperation, String strStatus, String isCardCreatedLocally, String strDateTime) {

        //            CREATE TABLE SmartCardOperationLog(
//                    AutoId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL ,
//                    LoginID TEXT DEFAULT(null),
//                    CSN TEXT NOT NULL DEFAULT(null) ,
//                    CardOperation TEXT DEFAULT (null) ,
//                    OriginalCardId TEXT(8) DEFAULT(null),
//                    OriginalCardIDVer TEXT(1) DEFAULT (null),
//                    NewCardId TEXT(8) DEFAULT(null),
//                    NewCardIDVer TEXT(1) DEFAULT(null),
//                    Status TEXT(1) DEFAULT (null),
//                    isCardCreatedLocally TEXT(1) DEFAULT(null),
//                    DateTime  TEXT DEFAULT (null)
//            )


        int insertStatus = -1;

        try {
            ContentValues initialValues = new ContentValues();
            initialValues.put("LoginID", loginId);
            initialValues.put("EmpAutoId", empAutoId);
            initialValues.put("CSN", strCardSerialNo);
            initialValues.put("CardOperation", strOperation);

            if (strOldCardId.trim().length() > 0) {
                initialValues.put("OriginalCardId", Utility.paddCardId(strOldCardId));
            } else {
                initialValues.put("OriginalCardId", "");
            }

            if (oldCardVersion > 0) {
                initialValues.put("OriginalCardIDVer", Integer.toString(oldCardVersion));
            } else {
                initialValues.put("OriginalCardIDVer", "");
            }

            if (strNewCardId.trim().length() > 0) {
                initialValues.put("NewCardId", Utility.paddCardId(strNewCardId));
            } else {
                initialValues.put("NewCardId", "");
            }

            if (newCardVersion > 0) {
                initialValues.put("NewCardIDVer", Integer.toString(newCardVersion));
            } else {
                initialValues.put("NewCardIDVer", "");
            }

            initialValues.put("Status", strStatus);
            initialValues.put("isCardCreatedLocally", isCardCreatedLocally);
            initialValues.put("DateTime", strDateTime);

            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            insertStatus = (int) db.insert(SMART_CARD_OPERATION_TABLE, null, initialValues);

        } catch (SQLiteException e) {

        } finally {

            if (db != null) {
                db.close();
            }
        }
        return insertStatus;
    }

    public int insertIntoHotListLog(int loginId, int empAutoId, String strCardSerialNo, String strOldCardId, int strOldCardVer, String strOperation, String strStatus, String isCardCreatedLocally, String strReason, String strDateTime) {

        //            CREATE TABLE HotListLog(
//                    AutoId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL ,
//                    LoginID TEXT DEFAULT(null),
//                    CSN TEXT NOT NULL DEFAULT(null) ,
//                    CardId TEXT(8) DEFAULT(null),
//                    CardIDVer TEXT(1) DEFAULT (null),
//                    CardOperation TEXT DEFAULT(null),
//                    Status TEXT(1) DEFAULT (null),
//                    isCardCreatedLocally TEXT(1) DEFAULT(null),
//                    DateTime  TEXT DEFAULT (null)
//            )


        int insertStatus = -1;

        try {
            ContentValues initialValues = new ContentValues();
            initialValues.put("LoginID", loginId);
            initialValues.put("EmpAutoId", empAutoId);
            initialValues.put("CSN", strCardSerialNo);
            if (strOldCardId.trim().length() > 0) {
                initialValues.put("CardId", Utility.paddCardId(strOldCardId));
            }
            initialValues.put("CardIDVer", Integer.toString(strOldCardVer));

            initialValues.put("CardOperation", strOperation);
            initialValues.put("Status", strStatus);
            initialValues.put("isCardCreatedLocally", isCardCreatedLocally);
            initialValues.put("HotlistReason", strReason);
            initialValues.put("DateTime", strDateTime);

            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            insertStatus = (int) db.insert(HOTLIST_TABLE, null, initialValues);

        } catch (SQLiteException e) {

        } finally {
            if (db != null) {
                db.close();
            }
        }
        return insertStatus;
    }

    public String getEmployeeCSN(int intAutoId) {

        String strEmpCSN = "";
        Cursor resData = null;

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select SmartCardSerialNo from " + EMPLOYEE_TABLE + " where AutoId=" + intAutoId + "", null);
            if (resData != null && resData.getCount() > 0) {
                while (resData.moveToNext()) {
                    strEmpCSN = resData.getString(0).trim();
                }
            }

        } catch (Exception e) {

        } finally {
            if (resData != null) {
                resData.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return strEmpCSN;
    }

    public int updateEmpCSN(String strCardSerialNo, String strOldCardId) {

        ContentValues initialValues = null;
        String strCondition = "";
        int updateStatus = -1;

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            initialValues = new ContentValues();
            initialValues.put("SmartCardSerialNo", "");
            strCondition = "SmartCardSerialNo" + "='" + strCardSerialNo + "' and CardId='" + Utility.paddCardId(strOldCardId) + "'";
            updateStatus = db.update(EMPLOYEE_TABLE, initialValues, strCondition, null);

        } catch (Exception e) {

        } finally {
            if (db != null) {
                db.close();
            }
        }
        return updateStatus;
    }

    public int updateSmartCardVer(int intAutoId, String strCSN, String strSmartCardVersion) {
        ContentValues initialValues = null;
        String strCondition = "";
        int updateStatus = -1;
        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            initialValues = new ContentValues();
            initialValues.put("SmartCardSerialNo", strCSN);
            initialValues.put("SmartCardVersion", strSmartCardVersion);
            strCondition = "AutoId" + "=" + intAutoId + "";
            updateStatus = db.update(EMPLOYEE_TABLE, initialValues, strCondition, null);

        } catch (Exception e) {

        } finally {

            if (db != null) {
                db.close();
            }
        }
        return updateStatus;
    }

    public int getCardIssuedStatus(String strCSN) {

        int autoId = -1;

        Cursor resData = null;

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select AutoId from " + EMPLOYEE_TABLE + " where SmartCardSerialNo='" + strCSN + "'", null);
            if (resData != null && resData.getCount() > 0) {
                while (resData.moveToNext()) {
                    autoId = resData.getInt(0);
                }
            }

        } catch (Exception e) {

        } finally {

            if (resData != null) {
                resData.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return autoId;
    }

    public String getEmpIdByAutoId(int autoId) {

        String strEmpId = "";
        Cursor resData = null;

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select EmployeeId from " + EMPLOYEE_TABLE + " where AutoId=" + autoId, null);
            if (resData != null && resData.getCount() > 0) {
                while (resData.moveToNext()) {
                    strEmpId = resData.getString(0);
                }
            }

        } catch (Exception e) {

        } finally {
            if (resData != null) {
                resData.close();
            }
            if (db != null) {
                db.close();
            }
        }

        return strEmpId;

    }

    public boolean isCardReadCreatedLocal(String strCRCSN) {

        boolean isCardCreatedLocal = false;
        Cursor resData = null;

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select SmartCardSerialNo from " + EMPLOYEE_TABLE + " where SmartCardSerialNo='" + strCRCSN + "'", null);
            if (resData != null && resData.getCount() > 0) {
                isCardCreatedLocal = true;
            }

        } catch (Exception e) {

        } finally {
            if (resData != null) {
                resData.close();
            }
            if (db != null) {
                db.close();
            }
        }

        return isCardCreatedLocal;
    }

    public int getEmpAutoId(String strCSN, String strReadCardId) {
        int autoId = -1;
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select AutoId from " + EMPLOYEE_TABLE + " where SmartCardSerialNo='" + strCSN + "' and CardId='" + Utility.paddCardId(strReadCardId) + "'", null);
            if (resData != null && resData.getCount() > 0) {
                while (resData.moveToNext()) {
                    autoId = resData.getInt(0);
                }
            }
        } catch (Exception e) {
        } finally {
            if (resData != null) {
                resData.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return autoId;
    }

    public int getSmartCardIssuedVer(int autoEmpId) {

        int smartCardVer = -1;
        Cursor resData = null;

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select SmartCardVersion from " + EMPLOYEE_TABLE + " where AutoId=" + autoEmpId, null);
            if (resData != null && resData.getCount() > 0) {
                while (resData.moveToNext()) {
                    smartCardVer = resData.getInt(0);
                }
            }

        } catch (Exception e) {

        } finally {
            if (resData != null) {
                resData.close();
            }
            if (db != null) {
                db.close();
            }
        }

        return smartCardVer;
    }

    public int updateNewCardDetails(int autoEmpId, String strCSN, int smartCardVer) {

        ContentValues initialValues = null;
        String strCondition = "";
        int updateStatus = -1;

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            initialValues = new ContentValues();
            initialValues.put("SmartCardSerialNo", strCSN);
            initialValues.put("SmartCardVersion", smartCardVer);
            strCondition = "AutoId" + "=" + autoEmpId;
            updateStatus = db.update(EMPLOYEE_TABLE, initialValues, strCondition, null);

        } catch (Exception e) {

        } finally {
            if (db != null) {
                db.close();
            }
        }

        return updateStatus;
    }

    public int isNewCardIdExists(String cardId) {
        int autoId = -1;
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select AutoId from " + EMPLOYEE_TABLE + " where CardId='" + Utility.paddCardId(cardId) + "'", null);
            if (resData != null && resData.getCount() > 0) {
                while (resData.moveToNext()) {
                    autoId = resData.getInt(0);
                }
            }
        } catch (Exception e) {
        } finally {
            if (resData != null) {
                resData.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return autoId;
    }

    public boolean isEmpIdEnrolled(String strCellData) {

        boolean isExists = false;
        Cursor resData = null;

        try {

            String strEmpId = Utility.paddEmpId(strCellData);
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select EmployeeId from " + EMPLOYEE_TABLE + " where EmployeeId='" + strEmpId + "'", null);
            if (resData != null && resData.getCount() > 0) {
                while (resData.moveToNext()) {
                    isExists = true;
                }
            }

        } catch (Exception e) {

        } finally {
            if (resData != null) {
                resData.close();
            }
            if (db != null) {
                db.close();
            }
        }

        return isExists;
    }

    public boolean isCardIdHotlisted(String strCellData) {

        boolean isExists = false;
        Cursor resData = null;
        try {

            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select CardId from " + HOTLIST_TABLE + " where CardId='" + Utility.paddCardId(strCellData) + "'", null);
            if (resData != null && resData.getCount() > 0) {
                isExists = true;
            }

        } catch (Exception e) {

        } finally {
            if (resData != null) {
                resData.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return isExists;
    }

    public void getHardwareModules(Map <String, String> map) {

        Cursor resData = null;

        try {

            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select modulecode,modulename from " + HARDWARE_MODULES_TABLE, null);
            if (resData != null && resData.getCount() > 0) {
                while (resData.moveToNext()) {
                    map.put(resData.getString(0), resData.getString(1));
                }
            }

        } catch (Exception e) {

        } finally {
            if (resData != null) {
                resData.close();
            }
            if (db != null) {
                db.close();
            }
        }
    }

    public int saveHardwareModule(String strModuleCode) {

        int status = -1;

        try {

            ContentValues content = new ContentValues();
            content.put("ModuleCode", strModuleCode);
            content.put("HardwareType", "SR");
            content.put("IsActive", "Y");

            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            status = (int) db.insert(HARDWARE_SETTINGS_TABLE, null, content);

            if (status != -1) {
                content = new ContentValues();
                content.put("IsActive", "N");
                status = db.update(HARDWARE_SETTINGS_TABLE, content, "HardwareType='SR' and AutoId !=" + status, null);
            }

        } catch (SQLiteException e) {

        } finally {
            if (db != null) {
                db.close();
            }
        }
        return status;
    }

    public String getSelectedSmartReader() {

        Cursor resData = null;
        String moduleCode = "";

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("SELECT ModuleCode FROM " + HARDWARE_SETTINGS_TABLE + " WHERE HardwareType='SR' and isActive='Y'", null);
            if (resData != null && resData.getCount() > 0) {
                while (resData.moveToNext()) {
                    moduleCode = resData.getString(0).trim();
                }
            }
        } catch (Exception e) {

        } finally {
            if (resData != null) {
                resData.close();
            }
            if (db != null) {
                db.close();
            }
        }

        return moduleCode;
    }

    public String getAttendanceDataForEzeeHr(String strPacketId, String strIMEI, String strCorporateId, String strDeviceToken, String strCommandType) {

        String strJson = "";
        Cursor resData = null;

        JSONObject jAttendance = new JSONObject();
        JSONObject jAttendanceDatas = new JSONObject();
        JSONArray jAttendanceDataArray = new JSONArray();
        JSONArray jPhoto = new JSONArray();

        try {

            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            // resData = db.rawQuery("select ID,EmployeeID,PunchDate,PunchTime,InOutMode,Lat,Long,Image from " + ATTENDANCE_TABLE + " where Uploaded='00' limit 1", null);

            resData = db.rawQuery("select ID,EmployeeID,PunchDate,PunchTime,InOutMode,Lat,Long from " + ATTENDANCE_TABLE + " where Uploaded='00' limit 1", null); // limit 1
            if (resData != null && resData.getCount() > 0) {

                jAttendance.put("COID", strCorporateId);
                jAttendance.put("PID", strPacketId);
                jAttendance.put("CPUID", strIMEI);
                jAttendance.put("DeviceToken", strDeviceToken);
                jAttendance.put("CT", strCommandType);
                jAttendance.put("OR", "D");
                jAttendance.put("PC", "1");
                jAttendance.put("TA", "01");

                while (resData.moveToNext()) {


                    int attendanceId = resData.getInt(0);

//                    String strEmpId = resData.getString(1);

                    String strEmpId = resData.getString(1);
                    ;
                    String strPunchDate = resData.getString(2);
                    String strPunchTime = resData.getString(3);
                    String mode = resData.getString(4);
                    String strLat = resData.getString(5);
                    String strLong = resData.getString(6);
                    //byte[] photo=resData.getBlob(7);

                    String com = strLat + "," + strLong;


                    strPunchDate = strPunchDate.replaceAll("-", "").trim();

                    String date = strPunchDate.substring(0, 2);
                    String month = strPunchDate.substring(2, 4);
                    String year = strPunchDate.substring(4);

                    /*
                    int hr = Integer.parseInt(strPunchTime.substring(0, 2));
                    int min =Integer.parseInt(strPunchTime.substring(2, 4));
                    int sec =Integer.parseInt(strPunchTime.substring(4, 6));
                    //int intAmPm=Integer.parseInt(strPunchTime.substring(0, 2));
                      */

                    String hr = strPunchTime.substring(0, 2);
                    String min = strPunchTime.substring(2, 4);
                    String sec = strPunchTime.substring(4, 6);
                    String tempPunchTime = hr + ":" + min + ":" + sec;

                    strPunchDate = date + "/" + month + "/" + year;

                    strPunchTime = tempPunchTime; //finalDateFormat.format(initialDateFormat.parse(strPunchTime));

                    if (strEmpId != null && strEmpId.length() > 0) {

                        resData = db.rawQuery("select Photo from " + EMPLOYEE_TABLE + " where EmployeeID='" + Utility.paddEmpId(strEmpId) + "'", null);

                        while (resData.moveToNext()) {

                            byte[] photo = resData.getBlob(0);
                            int ImgLength = 0;

                            if (photo != null) {  /*&& strEmail != null && strEmail.trim().length() > 0*/
                                String strPhoto = photo.toString();
                                String strBase64Photo = encoder.encode(strPhoto.getBytes());
                                ImgLength = strBase64Photo.length();

                                jPhoto.put(strBase64Photo);

                            } else {
                                ImgLength = 0; //Sample Value
                                jPhoto.put("");
                            }


                            //String strDateTime = strPunchDate + " " + strPunchTime;
                            String Dateformat = DateFormatChange(strPunchDate);
                            //String Timeformat = TimeFormatChange(strPunchTime);
                            String strDateTime = Dateformat + " " + strPunchTime;

                            //DateTime modified to JSON format request body
                            jAttendanceDatas.put("ContentLength", Integer.toString(ImgLength));
                            //jAttendanceDatas.put("EmpID", strEmpId);
                            jAttendanceDatas.put("EmpID", "960052");
                            jAttendanceDatas.put("attendancedate", Dateformat);
                            jAttendanceDatas.put("COID", strCorporateId);
                            jAttendanceDatas.put("fileExtension", "PNG");
                            jAttendanceDatas.put("punchdatetime", strDateTime);
                            jAttendanceDatas.put("latlong", com);
                            jAttendanceDatas.put("pictureBinary", jPhoto);
                            jAttendanceDatas.put("reason", "test attendance");

                            jAttendanceDataArray.put(jAttendanceDatas);

                        }
                    }
                    jAttendance.put("AD", jAttendanceDataArray);
                    strJson = jAttendance.toString();

                    if (attendanceId > 0) {
                        ContentValues initialValues = new ContentValues();
                        initialValues.put("IsDataFetched", "Y");
                        String strCondition = "ID" + "=" + attendanceId;
                        int status = db.update(ATTENDANCE_TABLE, initialValues, strCondition, null);
                        Log.d("TEST", "Attendance Pulled Status:" + status);
                    }
                }
            }
        } catch (Exception e) {
            Log.d("TEST", "Exception:" + e.getMessage());
        } finally {
            if (resData != null) {
                resData.close();
            }
            if (db != null) {
                db.close();
            }
        }

        return strJson;
    }


    private static final int AM = 0;
    private static final int PM = 1;

    /**
     * Based on concept: day start from 00:00AM and ends at 11:59PM,
     * afternoon 12 is 12PM, 12:xxAM is basically 00:xxAM
     * hour12Format
     * amPm
     */
    private int get24FormatHour(int hour12Format, int amPm) {
        if (hour12Format == 12 && amPm == AM) {
            hour12Format = 0;
        }
        if (amPm == PM && hour12Format != 12) {
            hour12Format += 12;
        }
        return hour12Format;
    }

    private int minutesTillMidnight(int hour12Format, int minutes, int amPm) {
        int hour24Format = get24FormatHour(hour12Format, amPm);
        System.out.println("24 Format :" + hour24Format + ":" + minutes);
        return (hour24Format * 60) + minutes;
    }


    public String TimeFormatChange(String PunchTime) {
        //String input = "23/12/2014 10:22:12 PM";
        //Format of the date defined in the input String
        String returnTime = "";

        DateFormat df = new SimpleDateFormat("hh:mm:ss aa");
        //Desired format: 24 hour format: Change the pattern as per the need
        DateFormat outputformat = new SimpleDateFormat("HH:mm:ss");
        Date date = null;
        String output = null;
        try {
            //Converting the input String to Date
            date = df.parse(PunchTime);
            //Changing the format of date and storing it in String
            output = outputformat.format(date);
            //Displaying the date
            System.out.println(output);
        } catch (ParseException pe) {
            pe.printStackTrace();
        }

        return returnTime = output;

    }

    public String DateFormatChange(String PunchDate) {
        //String input = "23/12/2014 10:22:12 PM";
        //Format of the date defined in the input String
        String returnDate = "";

        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");

        DateFormat outputformat = new SimpleDateFormat("dd/MMM/yyyy");
        Date date = null;
        String output = null;
        try {
            //Converting the input String to Date
            date = df.parse(PunchDate);
            //Changing the format of date and storing it in String
            output = outputformat.format(date);
            //Displaying the date
            Log.d("TEST", "DateFormatChange | Date :" + output);
        } catch (ParseException pe) {
            pe.printStackTrace();
        }

        return returnDate = output;

    }

    public boolean isSettingsFound(String header, String key) {

        Cursor resData = null;
        boolean found = false;

        try {

            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            resData = db.rawQuery("select count(*) from " + SETTINGS_TABLE + " where SettingsHeaderName='" + header + "' and SettingsParamName='" + key + "'", null);

            if (resData != null && resData.getCount() > 0) {
                found = true;
            }

        } catch (SQLiteException e) {

            Log.d("TEST", "isSettingsFound-Insert Exception:" + e.getMessage());

        } finally {
            if (db != null) {
                db.close();
            }
            if (resData != null) {
                resData.close();
            }
        }
        return found;
    }

    public int deleteSettings(String header, String key) {
        int status = -1;
        String strCondition = "";
        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            strCondition = "SettingsHeaderName='" + header + "' and SettingsParamName='" + key + "'";
            status = db.delete(SETTINGS_TABLE, strCondition, null);
        } catch (SQLiteException e) {

        } finally {
            if (db != null) {
                db.close();
            }
        }
        return status;
    }

    public int insertSettingsData(String header, String key, String val) {

        int status = -1;
        Cursor resData = null;

        try {

            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            resData = db.rawQuery("select ValueDetails from " + SETTINGS_INI_TABLE + " where SettingsHeaderName='" + header + "' and SettingsParamName='" + key + "' and SettingsParamVal='" + val + "'", null);

            if (resData != null && resData.getCount() > 0) {

                while (resData.moveToNext()) {

                    String strValueDetails = resData.getString(0).trim();
                    ContentValues content = new ContentValues();
                    content.put("SettingsHeaderName", header);
                    content.put("SettingsParamName", key);
                    content.put("SettingsParamVal", val);
                    content.put("ValueDetails", strValueDetails);
                    status = (int) db.insert(SETTINGS_TABLE, null, content);

                }
            }

        } catch (SQLiteException e) {
            Log.d("TEST", "insertSettingsData-Insert Exception:" + e.getMessage());
        } finally {
            if (db != null) {
                db.close();
            }
            if (resData != null) {
                resData.close();
            }
        }
        return status;
    }

    public boolean getAppSettings() {
        boolean isDataFound = false;
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            resData = db.rawQuery("select SettingsParamName,SettingsParamVal from " + SETTINGS_TABLE, null);
            if (resData != null && resData.getCount() > 0) {
                isDataFound = true;
                Settings settings = Settings.getInstance();
                while (resData.moveToNext()) {
                    String strParamName = resData.getString(0);
                    if (strParamName != null && strParamName.trim().length() > 0) {
                        int val;
                        switch (strParamName) {

                            case "Device Type":
                                val = Integer.parseInt(resData.getString(1));
                                settings.setDeviceTypeTypeValue(val);
                                break;

                            case "Smart Reader Type":
                                val = Integer.parseInt(resData.getString(1));
                                settings.setSrTypeValue(val);
                                break;

                            case "Finger Reader Type":
                                val = Integer.parseInt(resData.getString(1));
                                settings.setFrTypeValue(val);
                                break;

                            case "Enrollment Mode":
                                val = Integer.parseInt(resData.getString(1));
                                settings.setFingerEnrollmentModeValue(val);
                                break;

                            case "Server Type":
                                val = Integer.parseInt(resData.getString(1));
                                settings.setServerTypeValue(val);
                                break;

                            case "Employee Enrollment":
                                val = Integer.parseInt(resData.getString(1));
                                settings.setEmployeeEnrollmentValue(val);
                                break;

                            case "Master Data Entry":
                                val = Integer.parseInt(resData.getString(1));
                                settings.setMasterDateEntryValue(val);
                                break;

                            case "Programmable InOut":
                                val = Integer.parseInt(resData.getString(1));
                                settings.setPioValue(val);
                                break;

                            case "Excel Export/Import":
                                val = Integer.parseInt(resData.getString(1));
                                settings.setExcelImportExportVal(val);
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        } catch (SQLiteException e) {
            Log.d("TEST", "getAppSettings-Insert Exception:" + e.getMessage());
        } finally {
            if (resData != null) {
                resData.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return isDataFound;
    }

    public boolean getAttendanceServerDetails() {
        boolean isDataFound = false;
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            resData = db.rawQuery("select IPAddress,Port,Domain,Url from " + ATTENDANCE_SERVER_TABLE, null);
            if (resData != null && resData.getCount() > 0) {
                isDataFound = true;
                Settings settings = Settings.getInstance();
                while (resData.moveToNext()) {
                    settings.setAttendanaceSIP(resData.getString(0));
                    settings.setAttendancePort(resData.getString(1));
                    settings.setAttendanceDomain(resData.getString(2));
                    settings.setAttendanceUrl(resData.getString(3));
                }
            }
        } catch (SQLiteException e) {
            Log.d("TEST", "getAttendanceServerDetails-Insert Exception:" + e.getMessage());
        } finally {
            if (db != null) {
                db.close();
            }
            if (resData != null) {
                resData.close();
            }
        }
        return isDataFound;
    }

    public boolean getAadhaarServerDetails() {
        boolean isDataFound = false;
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            resData = db.rawQuery("select IPAddress,Port,Domain,Url from " + AADHAAR_SERVER_TABLE, null);
            if (resData != null && resData.getCount() > 0) {
                isDataFound = true;
                Settings settings = Settings.getInstance();
                while (resData.moveToNext()) {
                    settings.setAadhaarSIP(resData.getString(0));
                    settings.setAadhaarPort(resData.getString(1));
                    settings.setAadhaarDomain(resData.getString(2));
                    settings.setAadhaarUrl(resData.getString(3));
                }
            }
        } catch (SQLiteException e) {
            Log.d("TEST", "getAadhaarServerDetails-Exception:" + e.getMessage());
        } finally {
            if (resData != null) {
                resData.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return isDataFound;
    }


    public Cursor getSettings() {
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select SettingsParamName,ValueDetails from " + SETTINGS_TABLE, null);
            if (resData != null && resData.getCount() > 0) {
                return resData;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }


    public String getEnrolledTemplateForServer(String strPacketId, String strIMEI, String strDeviceToken, String strCT, String strCOID) {

        JSONObject TemplateUpload = new JSONObject();
        JSONObject FD = new JSONObject();

        JSONArray arrFD = new JSONArray();

        String requestJson = "";
        Cursor resFingerData = null;
        Cursor resEmployeeData = null;

        String strDateTime = "";

        BASE64Encoder encoder = new BASE64Encoder();

        try {

            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            resFingerData = db.rawQuery("select AutoId,TemplateSrNo,FingerIndex,SecurityLevel,VerificationMode,Quality,EnrolledOn,Template,FingerImage from " + FINGER_TABLE + " where IsUpdatedToServer='0' limit 1", null);

            String autoId = "", templateSrNo = "", fingerIndex = "", securityLevel = "", verificationMode = "", fingerQuality = "", enrolledOn = "", fmd = "";

            byte[] fid = null;

            if (resFingerData != null && resFingerData.getCount() > 0) {

                while (resFingerData.moveToNext()) {

                    autoId = resFingerData.getString(0).trim();
                    templateSrNo = resFingerData.getString(1).trim();
                    fingerIndex = resFingerData.getString(2).trim();
                    securityLevel = resFingerData.getString(3).trim();
                    verificationMode = resFingerData.getString(4).trim();
                    fingerQuality = resFingerData.getString(5).trim();
                    enrolledOn = resFingerData.getString(6).trim();
                    fmd = resFingerData.getString(7).trim();
                    fid = resFingerData.getBlob(8);

                    String date = enrolledOn.substring(0, 2);
                    String month = enrolledOn.substring(2, 4);
                    String year = enrolledOn.substring(4, 8);
                    String hr = enrolledOn.substring(8, 10);
                    String min = enrolledOn.substring(10, 12);
                    String sec = enrolledOn.substring(12);

                    enrolledOn = date + "/" + month + "/" + year;

                    String Dateformat = DateFormatChange(enrolledOn);
                    String Timeformat = hr + ":" + min + ":" + sec;

                    strDateTime = Dateformat + " " + Timeformat;

                    int val = -1;

                    if (templateSrNo != null && templateSrNo.length() > 0) {

                        int tempSerialNo = Integer.parseInt(templateSrNo);

                        if (tempSerialNo == 1) {
                            FD.put("FT", "F1");
                        } else if (tempSerialNo == 2) {
                            FD.put("FT", "F2");
                        }

                    } else {
                        FD.put("FT", "");
                    }


                    if (fingerIndex != null && fingerIndex.length() > 0) {
                        val = Utility.getFingerIndexValByName(fingerIndex);
                        if (val != -1) {
                            FD.put("FI", Integer.toHexString(val).toUpperCase());
                        }
                    } else {
                        FD.put("FI", "");
                    }

                    if (securityLevel != null && securityLevel.length() > 0) {
                        val = Utility.getSecurityLvlValByName(securityLevel);
                        if (val != -1) {
                            FD.put("SL", Integer.toString(val));
                        }
                    } else {
                        FD.put("SL", "");
                    }

                    if (fingerQuality != null && fingerQuality.length() > 0) {
                        val = Utility.getFingerQualityValByName(fingerQuality);
                        if (val != -1) {
                            FD.put("FQ", Integer.toString(val));
                        }
                    } else {
                        FD.put("FQ", "A");
                    }


                    if (fmd != null && fmd.length() > 0) {
                        String t_size = Integer.toString(fmd.length()); //Sanjay
                        FD.put("TS", t_size);
                    } else {
                        FD.put("TS", "");
                    }


                    if (fmd != null && fmd.length() > 0) {
                        FD.put("FMID", fmd);
                    } else {
                        FD.put("FMID", "");
                    }

                    if (fid != null && fid.length > 0) {

                        //String strFID = encoder.encode(fid);
                        //FD.put("FID", strFID);

                    /* Fot HTTP Error Code 413 Request Data Too Large Send Blank FID */

                        FD.put("FID", "");

                    } else {
                        FD.put("FID", "");
                    }

                    arrFD.put(FD);
                }
            }


            db.close();

            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);

            if (autoId != null && autoId.trim().length() > 0) {

                resEmployeeData = db.rawQuery("select EmployeeID,CardId,Name from " + EMPLOYEE_TABLE + " where AutoId='" + autoId + "'", null);

                if (resEmployeeData != null && resEmployeeData.getCount() > 0) {

                    TemplateUpload.put("COID", strCOID);
                    TemplateUpload.put("CPUID", strIMEI);
                    TemplateUpload.put("CT", strCT);
                    TemplateUpload.put("DTOE", strDateTime);
                    TemplateUpload.put("DeviceToken", strDeviceToken);
                    TemplateUpload.put("OR", "D");
                    TemplateUpload.put("PC", "1");
                    TemplateUpload.put("PID", strPacketId);
                    TemplateUpload.put("TA", "01");
                    TemplateUpload.put("UID", Integer.toString(UserDetails.getInstance().getLoginId()));

                    if (verificationMode != null && verificationMode.trim().length() > 0) {
                        int val = -1;
                        val = Utility.getVerificationModeValByName(verificationMode);
                        if (val != -1) {
                            TemplateUpload.put("VM", Integer.toString(val));
                        }
                    } else {
                        TemplateUpload.put("VM", "");
                    }

                    while (resEmployeeData.moveToNext()) {

                        String strEmpId = resEmployeeData.getString(0);
                        String strCardId = resEmployeeData.getString(1);
                        String strEmpName = resEmployeeData.getString(2);

                        TemplateUpload.put("EID", strEmpId.trim());
                        TemplateUpload.put("CID", strCardId.trim());
                        TemplateUpload.put("EN", strEmpName.trim());
                        TemplateUpload.put("Pin", strCardId.substring(4));
                    }

                    TemplateUpload.put("FD", arrFD);
                    requestJson = TemplateUpload.toString();
                }

            }

        } catch (Exception e) {
            Log.d("TEST", "exception:" + e.getMessage());
        } finally {

            if (db != null) {
                db.close();
            }

            if (resEmployeeData != null) {
                resEmployeeData.close();
            }

            if (resFingerData != null) {
                resFingerData.close();
            }
        }

        return requestJson;
    }

    public String getEnrolledTemplate(String strPacketId, String strIMEI, String strDeviceToken, String strCT, String strCOID) {

        JSONObject TemplateUpload = new JSONObject();
        JSONObject FD = new JSONObject(); //EmpFingerInfo
        JSONArray Fid = new JSONArray();

        String strJSON = "";
        Cursor resFingerData = null;
        Cursor resEmployeeData = null;

        SimpleDateFormat initialDateFormat = new SimpleDateFormat("hhmmss");
        SimpleDateFormat finalDateFormat = new SimpleDateFormat("hh:mm:ss a");
        String strDateTime = "";
        try {

            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            resFingerData = db.rawQuery("select AutoId,TemplateSrNo,FingerIndex,SecurityLevel,VerificationMode,Quality,EnrolledOn,Template,FingerImage from " + FINGER_TABLE + " where IsUpdatedToServer='0' limit 1", null);

            String autoId = "", templateSrNo = "", fingerIndex = "", securityLevel = "", verificationMode = "", fingerQuality = "", enrolledOn = "", fmd = "";
            byte[] fid = null;
            String ffid;

            if (resFingerData != null && resFingerData.getCount() > 0) {

                while (resFingerData.moveToNext()) {

                    autoId = resFingerData.getString(0).trim();
//                    Log.d("TEST", "Auto Id:" + autoId);

                    templateSrNo = resFingerData.getString(1).trim();
                    fingerIndex = resFingerData.getString(2).trim();
                    securityLevel = resFingerData.getString(3).trim();
                    verificationMode = resFingerData.getString(4).trim();
                    fingerQuality = resFingerData.getString(5).trim();
                    enrolledOn = resFingerData.getString(6).trim();
                    fmd = resFingerData.getString(7).trim();
                    fid = resFingerData.getBlob(8);
                    ffid = fid.toString();
                    Log.d("TEST", "FID Byte :" + ffid);
//                    Log.d("TEST", "Finger No:" + templateSrNo);
//                    Log.d("TEST", "Finger Index:" + fingerIndex);
//                    Log.d("TEST", "Security Level:" + securityLevel);
//                    Log.d("TEST", "Verification Mode:" + verificationMode);
//                    Log.d("TEST", "Finger Quality:" + fingerQuality);
//                    Log.d("TEST", "Enrolled On:" + enrolledOn);
//                    Log.d("TEST", "FMD:" + fmd);

                    String date = enrolledOn.substring(0, 2);
                    String month = enrolledOn.substring(2, 4);
                    String year = enrolledOn.substring(4, 8);
                    String hr = enrolledOn.substring(8, 10);
                    String min = enrolledOn.substring(10, 12);
                    String sec = enrolledOn.substring(12);

                    enrolledOn = date + "/" + month + "/" + year;
                    //enrolledOn = finalDateFormat.format(initialDateFormat.parse(enrolledOn));
                    String Dateformat = DateFormatChange(enrolledOn);
                    String Timeformat = hr + ":" + min + ":" + sec;
                    strDateTime = Dateformat + " " + Timeformat;

                    int val = -1;
                    /*  Employee Finger Info */
                    if (templateSrNo != null && templateSrNo.length() > 0) {

                        int tempSerialNo = Integer.parseInt(templateSrNo);

                        if (tempSerialNo == 1) {
                            FD.put("FT", "F1");
                        } else if (tempSerialNo == 2) {
                            FD.put("FT", "F2");
                        }

                    } else {
                        FD.put("FT", "");
                    }


                    if (fingerIndex != null && fingerIndex.length() > 0) {
                        val = Utility.getFingerIndexValByName(fingerIndex);
                        if (val != -1) {
                            FD.put("FI", Integer.toHexString(val).toUpperCase());
                        }
                    } else {
                        FD.put("FI", "");
                    }

                    if (securityLevel != null && securityLevel.length() > 0) {
                        val = Utility.getSecurityLvlValByName(securityLevel);
                        if (val != -1) {
                            FD.put("SL", Integer.toString(val));
                        }
                    } else {
                        FD.put("SL", "");
                    }

                    if (fingerQuality != null && fingerQuality.length() > 0) {
                        val = Utility.getFingerQualityValByName(fingerQuality);
                        if (val != -1) {
                            FD.put("FQ", Integer.toString(val));
                        }
                    } else {
                        FD.put("FQ", "A");
                    }


                    if (fmd != null && fmd.length() > 0) {
                        String t_size = Integer.toString(fmd.length()); //Sanjay
                        FD.put("TS", t_size);
                    } else {
                        FD.put("TS", "");
                    }


                    if (fmd != null && fmd.length() > 0) {
                        FD.put("FMID", fmd);
                    } else {
                        FD.put("FMID", "");
                    }

                    if (fid != null && fid.length > 0) {
                        //Log.d("TEST", "FID Byte :" + fid);
                        String strFID = encoder.encode(ffid.getBytes());
                        Fid.put(strFID);
                        FD.put("FID", Fid);
                    } else {
                        FD.put("FID", "");
                    }
                }
            }


            db.close();
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);

            if (autoId != null && autoId.trim().length() > 0) {

                resEmployeeData = db.rawQuery("select EmployeeID,CardId,Name from " + EMPLOYEE_TABLE + " where AutoId='" + autoId + "'", null);

                if (resEmployeeData != null && resEmployeeData.getCount() > 0) {

                    TemplateUpload.put("COID", strCOID);
                    TemplateUpload.put("CPUID", strIMEI);
                    TemplateUpload.put("CT", strCT);
                    TemplateUpload.put("DTOE", strDateTime);
                    TemplateUpload.put("DeviceToken", strDeviceToken);
                    TemplateUpload.put("OR", "D");
                    TemplateUpload.put("PC", "1");
                    TemplateUpload.put("PID", strPacketId);
                    TemplateUpload.put("TA", "01");
                    TemplateUpload.put("UID", UserDetails.getInstance().getLoginId());

                    if (verificationMode != null && verificationMode.trim().length() > 0) {
                        int val = -1;
                        val = Utility.getVerificationModeValByName(verificationMode);
                        if (val != -1) {
                            TemplateUpload.put("VM", Integer.toString(val));
                        }
                    } else {
                        TemplateUpload.put("VM", "");
                    }

                    while (resEmployeeData.moveToNext()) {

                        String strEmpId = resEmployeeData.getString(0).trim();
                        String strCardId = resEmployeeData.getString(1);
                        String strEmpName = resEmployeeData.getString(2);

                        TemplateUpload.put("EID", strEmpId);
                        TemplateUpload.put("CID", strCardId);
                        TemplateUpload.put("EN", strEmpName);
                        TemplateUpload.put("Pin", strCardId.substring(4));

//                        Log.d("TEST", "Employee Id:" + strEmpId);
//                        Log.d("TEST", "Card Id:" + strCardId);
//                        Log.d("TEST", "Employee Name:" + strEmpName);
                    }

                    /*  Employee Finger Info */

                    TemplateUpload.put("FD", FD);
                    strJSON = TemplateUpload.toString();

                    // Log.d("TEST",strJSON);

                }

            }


        } catch (Exception e) {
            Log.d("TEST", "exception:" + e.getMessage());
        } finally {

            if (db != null) {
                db.close();
            }

            if (resEmployeeData != null) {
                resEmployeeData.close();
            }
            if (resFingerData != null) {
                resFingerData.close();
            }
        }

        //Log.d("TEST", "Template Uplaod Data :" + strJSON);
        return strJSON;


    }


    /*---------------------------------------------- Template Upload To Server ---------------------------------------------------------*/
    public String getEnrolledTemplateNew(String strPacketId, String strIMEI, String strDeviceToken, String strCT, String strCOID) {

        JSONObject TemplateUpload = new JSONObject();
        JSONObject FD = new JSONObject(); //EmpFingerInfo
        JSONArray Fid = new JSONArray();

        String strJSON = "";
        Cursor resFingerData = null;
        Cursor resEmployeeData = null;
        String autoId = "", templateSrNo = "", fingerIndex = "", securityLevel = "", verificationMode = "", fingerQuality = "", enrolledOn = "", fmd = "";

        SimpleDateFormat initialDateFormat = new SimpleDateFormat("hhmmss");
        SimpleDateFormat finalDateFormat = new SimpleDateFormat("hh:mm:ss a");
        String strDateTime = "";
        String strNosFinger = "";
        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);

            if (autoId != null && autoId.trim().length() > 0) {

                resEmployeeData = db.rawQuery("select EmployeeID,CardId,Name, NosFinger from " + EMPLOYEE_TABLE + " where EnrollStatus='Yes'", null);

                if (resEmployeeData != null && resEmployeeData.getCount() > 0) {


                    if (verificationMode != null && verificationMode.trim().length() > 0) {
                        int val = -1;
                        val = Utility.getVerificationModeValByName(verificationMode);
                        if (val != -1) {
                            TemplateUpload.put("VM", Integer.toString(val));
                        }
                    } else {
                        TemplateUpload.put("VM", "");
                    }

                    while (resEmployeeData.moveToNext()) {

                        String strEmpId = resEmployeeData.getString(0).trim();
                        String strCardId = resEmployeeData.getString(1);
                        String strEmpName = resEmployeeData.getString(2);
                        strNosFinger = resEmployeeData.getString(3);

                        TemplateUpload.put("EID", strEmpId);
                        TemplateUpload.put("CID", strCardId);
                        TemplateUpload.put("EN", strEmpName);
                        TemplateUpload.put("Pin", strCardId.substring(4));

                        TemplateUpload.put("COID", strCOID);
                        TemplateUpload.put("CPUID", strIMEI);
                        TemplateUpload.put("CT", strCT);
                        TemplateUpload.put("DTOE", strDateTime);
                        TemplateUpload.put("DeviceToken", strDeviceToken);
                        TemplateUpload.put("OR", "D");
                        TemplateUpload.put("PC", "1");
                        TemplateUpload.put("PID", strPacketId);
                        TemplateUpload.put("TA", "01");
                        TemplateUpload.put("UID", UserDetails.getInstance().getLoginId());

                    }

                }

            }
            db.close();

            /*---------------------------- Employee Finger Info ------------------------------------*/

            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            resFingerData = db.rawQuery("select AutoId,TemplateSrNo,FingerIndex,SecurityLevel,VerificationMode,Quality,EnrolledOn,Template,FingerImage from " + FINGER_TABLE + " where IsUpdatedToServer='0' limit 2", null);

            byte[] fid = null;
            String ffid;

            if (resFingerData != null && resFingerData.getCount() > 0) {

                while (resFingerData.moveToNext()) {

                    autoId = resFingerData.getString(0).trim();

                    templateSrNo = resFingerData.getString(1).trim();
                    fingerIndex = resFingerData.getString(2).trim();
                    securityLevel = resFingerData.getString(3).trim();
                    verificationMode = resFingerData.getString(4).trim();
                    fingerQuality = resFingerData.getString(5).trim();
                    enrolledOn = resFingerData.getString(6).trim();
                    fmd = resFingerData.getString(7).trim();
                    fid = resFingerData.getBlob(8);
                    ffid = fid.toString();

                    String date = enrolledOn.substring(0, 2);
                    String month = enrolledOn.substring(2, 4);
                    String year = enrolledOn.substring(4, 8);
                    String hr = enrolledOn.substring(8, 10);
                    String min = enrolledOn.substring(10, 12);
                    String sec = enrolledOn.substring(12);

                    enrolledOn = date + "/" + month + "/" + year;
                    //enrolledOn = finalDateFormat.format(initialDateFormat.parse(enrolledOn));
                    String Dateformat = DateFormatChange(enrolledOn);
                    String Timeformat = hr + ":" + min + ":" + sec;
                    strDateTime = Dateformat + " " + Timeformat;

                    int val = -1;
                    /*  Employee Finger Info */
                    if (templateSrNo != null && templateSrNo.length() > 0) {

                        int tempSerialNo = Integer.parseInt(templateSrNo);

                        if (tempSerialNo == 1) {
                            FD.put("FT", "F1");
                        } else if (tempSerialNo == 2) {
                            FD.put("FT", "F2");
                        }

                    } else {
                        FD.put("FT", "");
                    }


                    if (fingerIndex != null && fingerIndex.length() > 0) {
                        val = Utility.getFingerIndexValByName(fingerIndex);
                        if (val != -1) {
                            FD.put("FI", Integer.toHexString(val).toUpperCase());
                        }
                    } else {
                        FD.put("FI", "");
                    }

                    if (securityLevel != null && securityLevel.length() > 0) {
                        val = Utility.getSecurityLvlValByName(securityLevel);
                        if (val != -1) {
                            FD.put("SL", Integer.toString(val));
                        }
                    } else {
                        FD.put("SL", "");
                    }

                    if (fingerQuality != null && fingerQuality.length() > 0) {
                        val = Utility.getFingerQualityValByName(fingerQuality);
                        if (val != -1) {
                            FD.put("FQ", Integer.toString(val));
                        }
                    } else {
                        FD.put("FQ", "A");
                    }


                    if (fmd != null && fmd.length() > 0) {
                        String t_size = Integer.toString(fmd.length()); //Sanjay
                        FD.put("TS", t_size);
                    } else {
                        FD.put("TS", "");
                    }


                    if (fmd != null && fmd.length() > 0) {
                        FD.put("FMID", fmd);
                    } else {
                        FD.put("FMID", "");
                    }

                    if (fid != null && fid.length > 0) {
                        //Log.d("TEST", "FID Byte :" + fid);
                        String strFID = encoder.encode(ffid.getBytes());
                        Fid.put(strFID);
                        FD.put("FID", Fid);
                    } else {
                        FD.put("FID", "");
                    }
                }
            }

            /*  Employee Finger Info */

            TemplateUpload.put("FD", FD);
            strJSON = TemplateUpload.toString();

        } catch (Exception e) {
            Log.d("TEST", "exception:" + e.getMessage());
        } finally {

            if (db != null) {
                db.close();
            }

            if (resEmployeeData != null) {
                resEmployeeData.close();
            }
            if (resFingerData != null) {
                resFingerData.close();
            }
        }
        return strJSON;
    }

    /*---------------------------------------------- Template Upload To Server ---------------------------------------------------------*/

    public String getDeviceRegistrationData(String imei, String pid, String strCOID, String strCT) {

        /* {"PID":"0001","CT":"1000",
        "COID":"T0000000020","DeviceCode":"50",
        "DevType":"54","TT":"EM",
        "CPUID":"FA140E2C9066","Tech Type":"A",
        "ST":"A","Temp Type":"I",
        "CMT":"G"
        }
           */
        String reqJson = "";
        JSONObject registrationJsonObj = new JSONObject();
        try {
            registrationJsonObj.put("PID", pid);
            registrationJsonObj.put("CPUID", imei);
            registrationJsonObj.put("COID", strCOID);
            registrationJsonObj.put("CT", strCT); //1000
            registrationJsonObj.put("DeviceCode", "50");
            registrationJsonObj.put("DevType", "54");
            registrationJsonObj.put("TT", "AN");
            registrationJsonObj.put("Tech Type", "A");   /*A->Finger, B->Face*/
            registrationJsonObj.put("ST", "B");          /* B->Morpho*/
            registrationJsonObj.put("Temp Type", "I");
            registrationJsonObj.put("CMT", "G");
            reqJson = registrationJsonObj.toString();

        } catch (JSONException e) {
            Log.d("TEST", "exception:" + e.getMessage());

        }

        return reqJson;
    }

    public String getDeviceDeleteData(String imei, String pid, String strCOID, String strDeviceToken) {

      /*  {
                "COID":"String content",
                "CPUID":"String content",
                "DeviceToken":"String content"
        }*/

        String reqJson = "";
        JSONObject registrationJsonObj = new JSONObject();
        try {
            registrationJsonObj.put("COID", strCOID);
            registrationJsonObj.put("CPUID", imei);
            registrationJsonObj.put("DeviceToken", strDeviceToken);

            reqJson = registrationJsonObj.toString();
            Log.d("TEST", "Device Reg imei:" + reqJson);
            Log.d("TEST", "Device Reg pid:" + pid);
            Log.d("TEST", "Device Reg pid:" + strDeviceToken);
            Log.d("TEST", "Device Reg JSON:" + reqJson);


        } catch (JSONException e) {
            Log.d("TEST", "exception:" + e.getMessage());

        }

        return reqJson;
    }


    public String getEnrolledEmployeee() {

        Cursor resData = null;
        String moduleCode = "";

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("SELECT EnrollStatus FROM " + EMPLOYEE_TABLE + " WHERE EnrollStatus='Yes' and EnrollStatus='Y'", null);
            if (resData != null && resData.getCount() > 0) {
                while (resData.moveToNext()) {
                    moduleCode = resData.getString(0).trim();
                }
            }
        } catch (Exception e) {

        } finally {
            if (resData != null) {
                resData.close();
            }
            if (db != null) {
                db.close();
            }
        }

        return moduleCode;
    }

    /*For startek finger scanner
    * **************************************************************************************
    * */

    /**
     * @param fingerTempate
     * @param fm220_sdk
     * @return Cursor object of database it contain matched row of EMPLOYEE_TABLE.
     */
    public synchronized Cursor oneToNCompare(byte[] fingerTempate, acpl_FM220_SDK fm220_sdk) {
        Cursor returnCursor = null;
        Cursor result = null;
        try {
            if (db != null) {
                db.close();
                db = null;
            }
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            result = db.rawQuery("SELECT * FROM " + FINGER_TABLE, null);
            Log.d("TEST", Arrays.toString(fingerTempate));
            if (result.moveToFirst() && result.getCount() > 0) {
                do {
                    if (fm220_sdk.MatchFM220(fingerTempate, toByteArray(result.getString(result.getColumnIndex("Template"))))) {
                        returnCursor = db.rawQuery("SELECT * FROM " + EMPLOYEE_TABLE + " WHERE AutoId=?", new String[]{String.valueOf(result.getLong(1))});
                        break;
                    }
                } while (result.moveToNext());
            }
        } catch (Exception e) {
            Log.e("TEST", "Error At oneToNCompare " + e.getMessage());
        } finally {
            /* do not close db hare because we will work on db cursor outside of this function */
            if (db != null) {
                if (returnCursor != null && returnCursor.getCount() > 0) returnCursor.moveToFirst();
                db.close();
            }
        }
        return returnCursor;
    }

    public static byte[] toByteArray(String s) {
        if (s.contains("null")) {
            Log.d("TEST", "Template conatin null");
            return null;
        }
        byte[] decodedHex = new BigInteger(s, 16).toByteArray();
        return decodedHex;
    }

    public static byte[] hexStringToByteArray(String s) {
        byte[] b = new byte[s.length() / 2];
        for (int i = 0; i < b.length; i++) {
            int index = i * 2;
            int v = Integer.parseInt(s.substring(index, index + 2), 16);
            b[i] = (byte) v;
        }
        return b;
    }


    /* Added by Ankit Kumar on 08-08-2018  */

    public int getTotalFingerTemplates() {

        Cursor resData = null;
        int totalRecords = 0;

        try {

            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("SELECT count(*) FROM " + FINGER_TABLE, null);
            if (resData != null && resData.getCount() > 0) {
                while (resData.moveToNext()) {
                    String strTotalRecords = resData.getString(0).trim();
                    totalRecords = Integer.parseInt(strTotalRecords);
                }
            }

        } catch (Exception e) {

        } finally {
            if (resData != null) {
                resData.close();
            }
            if (db != null) {
                db.close();
            }
        }

        return totalRecords;
    }

    public HashMap <Integer, String> getAllEnrolledTemplates(String strIMEI, String strDeviceToken, String strTemplateUploadCommand, String strCorporateId, String strPacketId) {

        HashMap <Integer, String> tempIdMap = null;

        String fingerId = "", templateSrNo = "", fingerIndex = "", securityLevel = "", verificationMode = "", fingerQuality = "", enrolledOn = "", fmd = "";

        byte[] fid = null;

        String requestJson = "";
        Cursor resFingerData = null;
        Cursor resEmployeeData = null;

        String strDateTime = "";

        BASE64Encoder encoder = new BASE64Encoder();

        int autoId = -1;

        String strEmpId = "", strCardId = "", strEmpName = "";

        try {

            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            resEmployeeData = db.rawQuery("select AutoId,EmployeeId,CardId,Name from " + EMPLOYEE_TABLE, null);

            if (resEmployeeData != null && resEmployeeData.getCount() > 0) {

                tempIdMap = new HashMap <>();

                while (resEmployeeData.moveToNext()) {

                    autoId = resEmployeeData.getInt(0);

                    strEmpId = resEmployeeData.getString(1);
                    strCardId = resEmployeeData.getString(2);
                    strEmpName = resEmployeeData.getString(3);

                    if (autoId != -1) {

                        db.close();

                        db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
                        resFingerData = db.rawQuery("select ID,TemplateSrNo,FingerIndex,SecurityLevel,VerificationMode,Quality,EnrolledOn,Template,FingerImage from " + FINGER_TABLE + " where AutoId='" + Integer.toString(autoId) + "'", null);

                        if (resFingerData != null && resFingerData.getCount() > 0) {

                            while (resFingerData.moveToNext()) {

                                JSONObject TemplateUpload = new JSONObject();
                                JSONObject FD = new JSONObject();

                                TemplateUpload.put("COID", strCorporateId);
                                TemplateUpload.put("CPUID", strIMEI);
                                TemplateUpload.put("CT", strTemplateUploadCommand);

                                TemplateUpload.put("DeviceToken", strDeviceToken);
                                TemplateUpload.put("OR", "D");
                                TemplateUpload.put("PC", "1");
                                TemplateUpload.put("PID", strPacketId);
                                TemplateUpload.put("TA", "01");
                                TemplateUpload.put("UID", Integer.toString(UserDetails.getInstance().getLoginId()));

                                TemplateUpload.put("EID", strEmpId);
                                TemplateUpload.put("CID", strCardId);
                                TemplateUpload.put("EN", strEmpName);
                                TemplateUpload.put("Pin", strCardId.substring(4));

//                                Random random = new Random();
//                                String pid = String.format("%04d", random.nextInt(10000));


                                fingerId = resFingerData.getString(0).trim();
                                templateSrNo = resFingerData.getString(1).trim();
                                fingerIndex = resFingerData.getString(2).trim();
                                securityLevel = resFingerData.getString(3).trim();
                                verificationMode = resFingerData.getString(4).trim();
                                fingerQuality = resFingerData.getString(5).trim();
                                enrolledOn = resFingerData.getString(6).trim();
                                fmd = resFingerData.getString(7).trim();
                                fid = resFingerData.getBlob(8);

                                String date = enrolledOn.substring(0, 2);
                                String month = enrolledOn.substring(2, 4);
                                String year = enrolledOn.substring(4, 8);
                                String hr = enrolledOn.substring(8, 10);
                                String min = enrolledOn.substring(10, 12);
                                String sec = enrolledOn.substring(12);

                                enrolledOn = date + "/" + month + "/" + year;

                                String Dateformat = DateFormatChange(enrolledOn);
                                String Timeformat = hr + ":" + min + ":" + sec;

                                strDateTime = Dateformat + " " + Timeformat;

                                TemplateUpload.put("DTOE", strDateTime);

                                int val = -1;

                                if (templateSrNo != null && templateSrNo.length() > 0) {

                                    int tempSerialNo = Integer.parseInt(templateSrNo);

                                    if (tempSerialNo == 1) {
                                        FD.put("FT", "F1");
                                    } else if (tempSerialNo == 2) {
                                        FD.put("FT", "F2");
                                    }

                                } else {
                                    FD.put("FT", "");
                                }


                                if (fingerIndex != null && fingerIndex.length() > 0) {
                                    val = Utility.getFingerIndexValByName(fingerIndex);
                                    if (val != -1) {
                                        FD.put("FI", Integer.toHexString(val).toUpperCase());
                                    }
                                } else {
                                    FD.put("FI", "");
                                }

                                if (securityLevel != null && securityLevel.length() > 0) {
                                    val = Utility.getSecurityLvlValByName(securityLevel);
                                    if (val != -1) {
                                        FD.put("SL", Integer.toString(val));
                                    }
                                } else {
                                    FD.put("SL", "");
                                }

                                if (fingerQuality != null && fingerQuality.length() > 0) {
                                    val = Utility.getFingerQualityValByName(fingerQuality);
                                    if (val != -1) {
                                        FD.put("FQ", Integer.toString(val));
                                    }
                                } else {
                                    FD.put("FQ", "A");
                                }


                                if (fmd != null && fmd.length() > 0) {
                                    String t_size = Integer.toString(fmd.length()); //Sanjay
                                    FD.put("TS", t_size);
                                } else {
                                    FD.put("TS", "");
                                }


                                if (fmd != null && fmd.length() > 0) {
                                    FD.put("FMID", fmd);
                                } else {
                                    FD.put("FMID", "");
                                }

                                if (fid != null && fid.length > 0) {

                                    //String strFID = encoder.encode(fid);
                                    //FD.put("FID", strFID);
                                    /* Fot HTTP Error Code 413 Request Data Too Large Send Blank FID */

                                    FD.put("FID", "");

                                } else {
                                    FD.put("FID", "");
                                }

                                /* Get Employee Details Against Finger Data  */

                                db.close();


                                if (verificationMode != null && verificationMode.trim().length() > 0) {
                                    val = Utility.getVerificationModeValByName(verificationMode);
                                    if (val != -1) {
                                        TemplateUpload.put("VM", Integer.toString(val));
                                    }
                                } else {
                                    TemplateUpload.put("VM", "");
                                }


                                TemplateUpload.put("FD", FD);
                                requestJson = TemplateUpload.toString();

                                tempIdMap.put(Integer.valueOf(fingerId), requestJson);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {

        } finally {
            if (db != null) {
                db.close();
            }
            if (resEmployeeData != null) {
                resEmployeeData.close();
            }
            if (resFingerData != null) {
                resFingerData.close();
            }
        }
        return tempIdMap;
    }

    public HashMap <Integer, String> geTemplateEmployeeWise(String strIMEI, String strDeviceToken, String strTemplateUploadCommand, String strCorporateId, String packetId, String strEmpId, String strCardId, String strEmpName) {

        HashMap <Integer, String> tempIdMap = null;

        String fingerId = "", templateSrNo = "", fingerIndex = "", securityLevel = "", verificationMode = "", fingerQuality = "", enrolledOn = "", fmd = "";

        byte[] fid = null;

        String requestJson = "";
        Cursor resFingerData = null;
        Cursor resEmployeeData = null;

        String strDateTime = "";

        BASE64Encoder encoder = new BASE64Encoder();

        int autoId = -1;

        try {

            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);

            resEmployeeData = db.rawQuery("select AutoId from " + EMPLOYEE_TABLE + " where EmployeeId='" + Utility.paddEmpId(strEmpId) + "'", null);

            if (resEmployeeData != null && resEmployeeData.getCount() > 0) {

                tempIdMap = new HashMap <>();

                while (resEmployeeData.moveToNext()) {

                    autoId = resEmployeeData.getInt(0);

                    if (autoId != -1) {

                        db.close();

                        db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
                        resFingerData = db.rawQuery("select ID,TemplateSrNo,FingerIndex,SecurityLevel,VerificationMode,Quality,EnrolledOn,Template,FingerImage from " + FINGER_TABLE + " where AutoId='" + Integer.toString(autoId) + "'", null);

                        if (resFingerData != null && resFingerData.getCount() > 0) {

                            while (resFingerData.moveToNext()) {

                                JSONObject TemplateUpload = new JSONObject();
                                JSONObject FD = new JSONObject();

                                TemplateUpload.put("COID", strCorporateId);
                                TemplateUpload.put("CPUID", strIMEI);
                                TemplateUpload.put("CT", strTemplateUploadCommand);

                                TemplateUpload.put("DeviceToken", strDeviceToken);
                                TemplateUpload.put("OR", "D");
                                TemplateUpload.put("PC", "1");
                                TemplateUpload.put("PID", packetId);
                                TemplateUpload.put("TA", "01");
                                TemplateUpload.put("UID", Integer.toString(UserDetails.getInstance().getLoginId()));

                                TemplateUpload.put("EID", strEmpId);
                                TemplateUpload.put("CID", strCardId);
                                TemplateUpload.put("EN", strEmpName);
                                TemplateUpload.put("Pin", strCardId.substring(4));

//                                Random random = new Random();
//                                String pid = String.format("%04d", random.nextInt(10000));


                                fingerId = resFingerData.getString(0).trim();
                                templateSrNo = resFingerData.getString(1).trim();
                                fingerIndex = resFingerData.getString(2).trim();
                                securityLevel = resFingerData.getString(3).trim();
                                verificationMode = resFingerData.getString(4).trim();
                                fingerQuality = resFingerData.getString(5).trim();
                                enrolledOn = resFingerData.getString(6).trim();
                                fmd = resFingerData.getString(7).trim();
                                fid = resFingerData.getBlob(8);

                                String date = enrolledOn.substring(0, 2);
                                String month = enrolledOn.substring(2, 4);
                                String year = enrolledOn.substring(4, 8);
                                String hr = enrolledOn.substring(8, 10);
                                String min = enrolledOn.substring(10, 12);
                                String sec = enrolledOn.substring(12);

                                enrolledOn = date + "/" + month + "/" + year;

                                String Dateformat = DateFormatChange(enrolledOn);
                                String Timeformat = hr + ":" + min + ":" + sec;

                                strDateTime = Dateformat + " " + Timeformat;

                                TemplateUpload.put("DTOE", strDateTime);

                                int val = -1;

                                if (templateSrNo != null && templateSrNo.length() > 0) {

                                    int tempSerialNo = Integer.parseInt(templateSrNo);

                                    if (tempSerialNo == 1) {
                                        FD.put("FT", "F1");
                                    } else if (tempSerialNo == 2) {
                                        FD.put("FT", "F2");
                                    }

                                } else {
                                    FD.put("FT", "");
                                }


                                if (fingerIndex != null && fingerIndex.length() > 0) {
                                    val = Utility.getFingerIndexValByName(fingerIndex);
                                    if (val != -1) {
                                        FD.put("FI", Integer.toHexString(val).toUpperCase());
                                    }
                                } else {
                                    FD.put("FI", "");
                                }

                                if (securityLevel != null && securityLevel.length() > 0) {
                                    val = Utility.getSecurityLvlValByName(securityLevel);
                                    if (val != -1) {
                                        FD.put("SL", Integer.toString(val));
                                    }
                                } else {
                                    FD.put("SL", "");
                                }

                                if (fingerQuality != null && fingerQuality.length() > 0) {
                                    val = Utility.getFingerQualityValByName(fingerQuality);
                                    if (val != -1) {
                                        FD.put("FQ", Integer.toString(val));
                                    }
                                } else {
                                    FD.put("FQ", "A");
                                }


                                if (fmd != null && fmd.length() > 0) {
                                    String t_size = Integer.toString(fmd.length()); //Sanjay
                                    FD.put("TS", t_size);
                                } else {
                                    FD.put("TS", "");
                                }


                                if (fmd != null && fmd.length() > 0) {
                                    FD.put("FMID", fmd);
                                } else {
                                    FD.put("FMID", "");
                                }

                                if (fid != null && fid.length > 0) {

                                    //String strFID = encoder.encode(fid);
                                    //FD.put("FID", strFID);
                                    /* Fot HTTP Error Code 413 Request Data Too Large Send Blank FID */

                                    FD.put("FID", "");

                                } else {
                                    FD.put("FID", "");
                                }

                                /* Get Employee Details Against Finger Data  */

                                db.close();


                                if (verificationMode != null && verificationMode.trim().length() > 0) {
                                    val = Utility.getVerificationModeValByName(verificationMode);
                                    if (val != -1) {
                                        TemplateUpload.put("VM", Integer.toString(val));
                                    }
                                } else {
                                    TemplateUpload.put("VM", "");
                                }


                                TemplateUpload.put("FD", FD);
                                requestJson = TemplateUpload.toString();

                                tempIdMap.put(Integer.valueOf(fingerId), requestJson);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.d("TEST", "Exception:" + e.getMessage());
        } finally {

            if (db != null) {
                db.close();
            }

            if (resEmployeeData != null) {
                resEmployeeData.close();
            }

            if (resFingerData != null) {
                resFingerData.close();
            }
        }

        return tempIdMap;
    }

    public HashMap <Integer, String> getNewEnrolledTemplates(String strIMEI, String strDeviceToken, String strTemplateUploadCommand, String strCorporateId, String strPacketId) {

        HashMap <Integer, String> tempIdMap = null;

        JSONArray Fid = new JSONArray();

        String strJSON = "";
        Cursor resFingerData = null;
        Cursor resEmployeeData = null;

        SimpleDateFormat initialDateFormat = new SimpleDateFormat("hhmmss");
        SimpleDateFormat finalDateFormat = new SimpleDateFormat("hh:mm:ss a");
        String strDateTime = "";

        try {

            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            resFingerData = db.rawQuery("select ID,AutoId,TemplateSrNo,FingerIndex,SecurityLevel,VerificationMode,Quality,EnrolledOn,Template,FingerImage from " + FINGER_TABLE + " where IsUpdatedToServer='0'", null);

            String fingerId = "", autoId = "", templateSrNo = "", fingerIndex = "", securityLevel = "", verificationMode = "", fingerQuality = "", enrolledOn = "", fmd = "";
            byte[] fid = null;
            String ffid;

            if (resFingerData != null && resFingerData.getCount() > 0) {

                tempIdMap = new HashMap <>();

                while (resFingerData.moveToNext()) {

                    JSONObject TemplateUpload = new JSONObject();
                    JSONObject FD = new JSONObject();


                    TemplateUpload.put("COID", strCorporateId);
                    TemplateUpload.put("CPUID", strIMEI);
                    TemplateUpload.put("CT", strTemplateUploadCommand);

                    TemplateUpload.put("DeviceToken", strDeviceToken);
                    TemplateUpload.put("OR", "D");
                    TemplateUpload.put("PC", "1");
                    TemplateUpload.put("PID", strPacketId);
                    TemplateUpload.put("TA", "01");
                    TemplateUpload.put("UID", UserDetails.getInstance().getLoginId());


                    fingerId = resFingerData.getString(0);
                    autoId = resFingerData.getString(1).trim();

                    templateSrNo = resFingerData.getString(2).trim();
                    fingerIndex = resFingerData.getString(3).trim();
                    securityLevel = resFingerData.getString(4).trim();
                    verificationMode = resFingerData.getString(5).trim();
                    fingerQuality = resFingerData.getString(6).trim();
                    enrolledOn = resFingerData.getString(7).trim();
                    fmd = resFingerData.getString(8).trim();
                    fid = resFingerData.getBlob(9);

                    String date = enrolledOn.substring(0, 2);
                    String month = enrolledOn.substring(2, 4);
                    String year = enrolledOn.substring(4, 8);
                    String hr = enrolledOn.substring(8, 10);
                    String min = enrolledOn.substring(10, 12);
                    String sec = enrolledOn.substring(12);

                    enrolledOn = date + "/" + month + "/" + year;
                    String Dateformat = DateFormatChange(enrolledOn);
                    String Timeformat = hr + ":" + min + ":" + sec;
                    strDateTime = Dateformat + " " + Timeformat;

                    TemplateUpload.put("DTOE", strDateTime);

                    int val = -1;

                    if (templateSrNo != null && templateSrNo.length() > 0) {

                        int tempSerialNo = Integer.parseInt(templateSrNo);

                        if (tempSerialNo == 1) {
                            FD.put("FT", "F1");
                        } else if (tempSerialNo == 2) {
                            FD.put("FT", "F2");
                        }

                    } else {
                        FD.put("FT", "");
                    }


                    if (fingerIndex != null && fingerIndex.length() > 0) {
                        val = Utility.getFingerIndexValByName(fingerIndex);
                        if (val != -1) {
                            FD.put("FI", Integer.toHexString(val).toUpperCase());
                        }
                    } else {
                        FD.put("FI", "");
                    }

                    if (securityLevel != null && securityLevel.length() > 0) {
                        val = Utility.getSecurityLvlValByName(securityLevel);
                        if (val != -1) {
                            FD.put("SL", Integer.toString(val));
                        }
                    } else {
                        FD.put("SL", "");
                    }

                    if (fingerQuality != null && fingerQuality.length() > 0) {
                        val = Utility.getFingerQualityValByName(fingerQuality);
                        if (val != -1) {
                            FD.put("FQ", Integer.toString(val));
                        }
                    } else {
                        FD.put("FQ", "A");
                    }


                    if (fmd != null && fmd.length() > 0) {
                        String t_size = Integer.toString(fmd.length()); //Sanjay
                        FD.put("TS", t_size);
                    } else {
                        FD.put("TS", "");
                    }


                    if (fmd != null && fmd.length() > 0) {
                        FD.put("FMID", fmd);
                    } else {
                        FD.put("FMID", "");
                    }

                    if (fid != null && fid.length > 0) {

                        //String strFID = encoder.encode(fid);
                        //FD.put("FID", strFID);

                        /* Fot HTTP Error Code 413 Request Data Too Large Send Blank FID */

                        FD.put("FID", "");


                    } else {
                        FD.put("FID", "");
                    }

                    db.close();
                    db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);

                    if (autoId != null && autoId.trim().length() > 0) {

                        resEmployeeData = db.rawQuery("select EmployeeID,CardId,Name from " + EMPLOYEE_TABLE + " where AutoId='" + autoId + "'", null);

                        if (resEmployeeData != null && resEmployeeData.getCount() > 0) {

                            if (verificationMode != null && verificationMode.trim().length() > 0) {
                                val = Utility.getVerificationModeValByName(verificationMode);
                                if (val != -1) {
                                    TemplateUpload.put("VM", Integer.toString(val));
                                }
                            } else {
                                TemplateUpload.put("VM", "");
                            }

                            while (resEmployeeData.moveToNext()) {

                                String strEmpId = resEmployeeData.getString(0).trim();
                                String strCardId = resEmployeeData.getString(1);
                                String strEmpName = resEmployeeData.getString(2);

                                TemplateUpload.put("EID", strEmpId);
                                TemplateUpload.put("CID", strCardId);
                                TemplateUpload.put("EN", strEmpName);
                                TemplateUpload.put("Pin", strCardId.substring(4));
                            }

                            TemplateUpload.put("FD", FD);
                            strJSON = TemplateUpload.toString();

                            tempIdMap.put(Integer.valueOf(fingerId), strJSON);
                        }
                    }
                }
            }

        } catch (Exception e) {
            Log.d("TEST", "exception:" + e.getMessage());
        } finally {

            if (db != null) {
                db.close();
            }

            if (resEmployeeData != null) {
                resEmployeeData.close();
            }
            if (resFingerData != null) {
                resFingerData.close();
            }
        }

        return tempIdMap;
    }

    public boolean insertEnrolledTemplate(int autoId, String fingerType, String fingerIndex, String fingerQuality, String strVerificationMode, String strSecurityLevel, String fmd, byte[] fid, String dtoe) {


//        int insertStaus = -1;
//        int updateStatus = -1;
        boolean status = false;

//        Cursor c = null;
//
//        String noOfFingers = "";
//
//        String enrolledDateTime = "";
//
//        try {
//
//            //14-Aug-2018 11:01:47
//
//            DateFormat originalFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss");
//            DateFormat targetFormat = new SimpleDateFormat("ddmmyyyyhhmmss");
//            if (dtoe != null && dtoe.trim().length() > 0) {
//                Date date = originalFormat.parse(dtoe);
//                enrolledDateTime = targetFormat.format(date);  // 20120821
//            }
//
//            ContentValues firstTemplate = new ContentValues();
//            firstTemplate.put("AutoID", Integer.toString(autoId));
//            firstTemplate.put("TermplateType", "iso");
//
//            if (fingerType != null && fingerType.equals("F1")) {
//                firstTemplate.put("TemplateSrNo", "1");
//                noOfFingers = "1";
//            } else if (fingerType != null && fingerType.equals("F2")) {
//                firstTemplate.put("TemplateSrNo", "2");
//                noOfFingers = "2";
//            }
//
//            firstTemplate.put("FingerIndex", fingerIndex);
//
//            if (!strSecurityLevel.trim().equalsIgnoreCase("Select")) {
//                firstTemplate.put("SecurityLevel", strSecurityLevel);
//            } else {
//                firstTemplate.put("SecurityLevel", "");
//            }
//
//            if (!strVerificationMode.trim().equalsIgnoreCase("Select")) {
//                firstTemplate.put("VerificationMode", strVerificationMode);
//            } else {
//                firstTemplate.put("VerificationMode", "");
//            }
//
//            firstTemplate.put("Quality", fingerQuality);
//            firstTemplate.put("EnrolledOn", enrolledDateTime);  //date & time at the time of finger enrollment
//
//            int len = fmd.length();
//
//            if (len < 512) {
//                firstTemplate.put("Template", fmd + "00000000");  //adjust 4 byte 252 + 4 = 256
//            } else {
//                firstTemplate.put("Template", fmd);
//            }
//
//
//            firstTemplate.put("FingerImage", fid);
//            firstTemplate.put("isAadhaarVerifiedYorN", "N");
//
//            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
//            insertStaus = (int) db.insert(FINGER_TABLE, null, firstTemplate);
//
//            if (insertStaus != -1) {
//                updateStatus = updateFingerDataToEmpTable(Integer.toString(autoId), "Yes", noOfFingers, strVerificationMode, "N");
//                if (updateStatus != -1) {
//                    status = true;
//                }
//            }
//
//        } catch (Exception e) {
//        } finally {
//            if (c != null) {
//                c.close();
//            }
//            if (db != null) {
//                db.close();
//            }
//        }

        return status;
    }

    public int checkTemplateExistsByFT(int autoId, String fingerType) {

        Cursor resFingerData = null;
        int id = -1;

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resFingerData = db.rawQuery("select ID from " + FINGER_TABLE + " where AutoID='" + Integer.toString(autoId) + "' and TemplateSrNo='" + fingerType + "'", null);

            if (resFingerData != null) {
                while (resFingerData.moveToNext()) {
                    id = Integer.parseInt(resFingerData.getString(0));
                }
            }

        } catch (Exception e) {
        } finally {
            if (resFingerData != null) {
                resFingerData.close();
            }
            if (db != null) {
                db.close();
            }
        }

        return id;
    }


    public boolean updateEnrolledTemplate(int empId, int fingerId, String fingerType, String fingerIndex, String fingerQuality, String strVerificationMode, String strSecurityLevel, String fmd, byte[] fid, String dtoe) {

        int updateFingerStatus = -1;
        boolean status = false;

        String noOfFingers = "";
        String enrolledDateTime = "";

//        try {
//
//            DateFormat originalFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss");
//            DateFormat targetFormat = new SimpleDateFormat("ddmmyyyyyhhmmss");
//
//            if (dtoe != null && dtoe.trim().length() > 0) {
//                Date date = originalFormat.parse(dtoe);
//                enrolledDateTime = targetFormat.format(date);  // 20120821
//            }
//
//            ContentValues firstTemplate = new ContentValues();
//            firstTemplate.put("AutoID", Integer.toString(empId));
//            firstTemplate.put("TermplateType", "iso");
//
//            if (fingerType != null && fingerType.equals("F1")) {
//                firstTemplate.put("TemplateSrNo", "1");
//                noOfFingers = "1";
//            } else if (fingerType != null && fingerType.equals("F2")) {
//                firstTemplate.put("TemplateSrNo", "2");
//                noOfFingers = "2";
//            }
//
//            firstTemplate.put("FingerIndex", fingerIndex);
//
//            if (!strSecurityLevel.trim().equalsIgnoreCase("Select")) {
//                firstTemplate.put("SecurityLevel", strSecurityLevel);
//            } else {
//                firstTemplate.put("SecurityLevel", "");
//            }
//
//            if (!strVerificationMode.trim().equalsIgnoreCase("Select")) {
//                firstTemplate.put("VerificationMode", strVerificationMode);
//            } else {
//                firstTemplate.put("VerificationMode", "");
//            }
//
//            firstTemplate.put("Quality", fingerQuality);
//            firstTemplate.put("EnrolledOn", enrolledDateTime);  //date & time at the time of finger enrollment
//            firstTemplate.put("Template", fmd);  //adjust 4 byte 252 + 4 = 256
//            firstTemplate.put("FingerImage", fid);
//            firstTemplate.put("isAadhaarVerifiedYorN", "N");
//
//            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
//            updateFingerStatus = db.update(FINGER_TABLE, firstTemplate, "ID='" + Integer.toString(fingerId) + "'", null);
//
//            if (updateFingerStatus != -1) {
//                updateFingerStatus = updateFingerDataToEmpTable(Integer.toString(empId), "Yes", noOfFingers, strVerificationMode, "N");
//                if (updateFingerStatus != -1) {
//                    status = true;
//                }
//            }
//        } catch (Exception e) {
//            Log.d("TEST", "Exception:" + e.getMessage());
//        } finally {
//
//            if (db != null) {
//                db.close();
//            }
//        }

        return status;
    }

    public boolean isSmartCardReaderInstalled() {

        Cursor rs = null;
        boolean isReaderPresent = false;

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            rs = db.rawQuery("select SettingsParamVal from " + SETTINGS_TABLE + " where SettingsHeaderName='Smart Reader' and SettingsParamName='Smart Reader Type'", null);
            if (rs != null) {
                while (rs.moveToNext()) {
                    String val = rs.getString(0);
                    if (val != null && val.trim().length() > 0) {
                        try {
                            int value = Integer.parseInt(val);
                            if (value == 0 || value == 1) {
                                isReaderPresent = true;
                            }
                        } catch (NumberFormatException ne) {
                        }
                    }
                }
            }
        } catch (Exception e) {
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (db != null) {
                db.close();
            }
        }

        return isReaderPresent;
    }

    public int getTotalFingerEnrolledUser() {

        Cursor rs = null;
        boolean isReaderPresent = false;
        int value = -1;

        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            rs = db.rawQuery("SELECT count(distinct(AutoId)) from " + FINGER_TABLE, null);
            if (rs != null) {
                while (rs.moveToNext()) {
                    String val = rs.getString(0);
                    if (val != null && val.trim().length() > 0) {
                        try {
                            value = Integer.parseInt(val);
                        } catch (NumberFormatException ne) {
                        }
                    }
                }
            }
        } catch (Exception e) {
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return value;
    }

    public int getTotalEnrolledUsers() {
        Cursor rs = null;
        int value = -1;
        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            rs = db.rawQuery("SELECT count(*) from " + EMPLOYEE_TABLE, null);
            if (rs != null) {
                while (rs.moveToNext()) {
                    String val = rs.getString(0);
                    if (val != null && val.trim().length() > 0) {
                        try {
                            value = Integer.parseInt(val);
                        } catch (NumberFormatException ne) {
                        }
                    }
                }
            }
        } catch (Exception e) {
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return value;
    }

    public int getTotalUnSendRecords() {
        Cursor rs = null;
        int value = -1;
        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            rs = db.rawQuery("SELECT count(*) from " + ATTENDANCE_TABLE + " where Uploaded='00'", null);
            if (rs != null) {
                while (rs.moveToNext()) {
                    String val = rs.getString(0);
                    if (val != null && val.trim().length() > 0) {
                        try {
                            value = Integer.parseInt(val);
                        } catch (NumberFormatException ne) {
                        }
                    }
                }
            }
        } catch (Exception e) {
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return value;
    }

    public String getEmployeeDataJson(String pid, String imei, String command) {

        JSONObject innerJson = null, outerJson = null;
        JSONArray customJobModeList = null;
        JSONObject finalJson = null;
        Cursor resData = null;
        String responseJson = "";
        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select EmployeeId,CardId,Name,BloodGroup,SiteCode,MobileNo,MailId,PIN,ValidUpto,BirthDay,isBlackListed,isLockOpenWhenAllowed from " + EMPLOYEE_TABLE + " where isUpdatedToServer=0 limit 1", null);
            if (resData != null && resData.getCount() > 0) {
                finalJson = new JSONObject();
                customJobModeList = new JSONArray();
                outerJson = new JSONObject();
                innerJson = new JSONObject();
                while (resData.moveToNext()) {
                    String empId = resData.getString(0);
                    String cardId = resData.getString(1);
                    String empName = resData.getString(2);
                    String bloodGrp = resData.getString(3);
                    String siteCode = resData.getString(4);
                    String mobileNo = resData.getString(5);
                    String maildId = resData.getString(6);
                    String pin = resData.getString(7);
                    String dob = resData.getString(8);
                    String dov = resData.getString(9);
                    String isBlackListed = resData.getString(10);
                    String isLockOpen = resData.getString(11);

                    if (empId != null && empId.trim().length() > 0) {
                        innerJson.put("EID", empId);
                    } else {
                        innerJson.put("EID", "");
                    }

                    if (cardId != null && cardId.trim().length() > 0) {
                        innerJson.put("CID", cardId);
                    } else {
                        innerJson.put("CID", "");
                    }

                    if (empName != null && empName.trim().length() > 0) {
                        innerJson.put("EN", empName);
                    } else {
                        innerJson.put("EN", "");
                    }

                    if (bloodGrp != null && bloodGrp.trim().length() > 0) {
                        int blood = Utility.getBloodGrValByName(bloodGrp);
                        if (blood != -1) {
                            innerJson.put("BG", Integer.toString(blood));
                        } else {
                            innerJson.put("BG", "");
                        }
                    } else {
                        innerJson.put("BG", "");
                    }

                    if (siteCode != null && siteCode.trim().length() > 0) {
                        innerJson.put("SC", siteCode);
                    } else {
                        innerJson.put("SC", "");
                    }

                    if (mobileNo != null && mobileNo.trim().length() > 0) {
                        innerJson.put("MN", mobileNo);
                    } else {
                        innerJson.put("MN", "");
                    }

                    if (maildId != null && maildId.trim().length() > 0) {
                        innerJson.put("EMID", maildId);
                    } else {
                        innerJson.put("EMID", "");
                    }

                    if (pin != null && pin.trim().length() > 0) {
                        innerJson.put("PIN", pin);
                    } else {
                        innerJson.put("PIN", "");
                    }

                    if (dob != null && dob.trim().length() > 0) {
                        innerJson.put("DOB", dob);
                    } else {
                        innerJson.put("DOB", "");
                    }

                    if (dov != null && dov.trim().length() > 0) {
                        innerJson.put("DOV", dov);
                    } else {
                        innerJson.put("DOV", "");
                    }

                    if (isBlackListed != null && isBlackListed.trim().length() > 0) {
                        innerJson.put("IsBlackListed", isBlackListed);
                    } else {
                        innerJson.put("IsBlackListed", "");
                    }

                    if (isLockOpen != null && isLockOpen.trim().length() > 0) {
                        innerJson.put("IsLockOpen", isLockOpen);
                    } else {
                        innerJson.put("IsLockOpen", "");
                    }
                    innerJson.put("AID", "");
                    innerJson.put("PPL", "");
                    innerJson.put("PP", "");

                    outerJson.put("CPUID", imei);
                    outerJson.put("Command", command);
                    outerJson.put("CommandNo", "0");
                    outerJson.put("CreatedOn", "");
                    outerJson.put("PID", pid);
                    outerJson.put("JobID", pid);

                    outerJson.put("CommandData", innerJson.toString());
                    customJobModeList.put(0, outerJson);
                    finalJson.put("CustomJobModellist", customJobModeList);
                    finalJson.put("ResponseMessage", "");
                    responseJson = finalJson.toString();
                }
            }
        } catch (Exception e) {
            Log.d("TEST", "Exception:" + e.getMessage());

        } finally {
            if (resData != null) {
                resData.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return responseJson;
    }
}
