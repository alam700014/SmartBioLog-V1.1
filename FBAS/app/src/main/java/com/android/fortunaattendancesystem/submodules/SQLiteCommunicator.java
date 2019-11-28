package com.android.fortunaattendancesystem.submodules;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.android.fortunaattendancesystem.adapter.DatabaseItem;
import com.android.fortunaattendancesystem.constant.Constants;
import com.android.fortunaattendancesystem.helper.Utility;
import com.android.fortunaattendancesystem.model.AttendanceInfo;
import com.android.fortunaattendancesystem.model.BasicEmployeeInfo;
import com.android.fortunaattendancesystem.model.CollegeAttendanceInfo;
import com.android.fortunaattendancesystem.model.ContractorInfo;
import com.android.fortunaattendancesystem.model.EmpValidationDownloadInfo;
import com.android.fortunaattendancesystem.model.EmployeeEnrollInfo;
import com.android.fortunaattendancesystem.model.EmployeeFingerInfo;
import com.android.fortunaattendancesystem.model.EmployeeInfo;
import com.android.fortunaattendancesystem.model.EmployeeTypeInfo;
import com.android.fortunaattendancesystem.model.EmployeeValidationBasicInfo;
import com.android.fortunaattendancesystem.model.EmployeeValidationFingerInfo;
import com.android.fortunaattendancesystem.model.PeriodInfo;
import com.android.fortunaattendancesystem.model.ProfessorStudentSubjectInfo;
import com.android.fortunaattendancesystem.model.ProfessorSubjectInfo;
import com.android.fortunaattendancesystem.model.SmartCardInfo;
import com.android.fortunaattendancesystem.model.StartekInfo;
import com.android.fortunaattendancesystem.model.SubInfo;
import com.android.fortunaattendancesystem.model.SubjectInfo;
import com.android.fortunaattendancesystem.model.TemplateDownloadInfo;
import com.android.fortunaattendancesystem.model.TemplateUploadInfo;
import com.android.fortunaattendancesystem.model.WiegandSettingsInfo;
import com.android.fortunaattendancesystem.singleton.EmployeeFingerEnrollInfo;
import com.android.fortunaattendancesystem.singleton.Settings;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import Decoder.BASE64Decoder;
import Decoder.BASE64Encoder;

/**
 * Created by fortuna on 15/9/18.
 */

public class SQLiteCommunicator {

    //SQLiteDatabase db;

    public boolean checkIsCardHotListed(String strCardSerialNo) {
        boolean isCardHotlisted = false;
        SQLiteDatabase db = null;
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select AutoId from " + Constants.HOTLIST_TABLE + " where CSN='" + strCardSerialNo + "'", null);
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

    public Cursor getSectorAndKeyForReadCard() {
        SQLiteDatabase db = null;
        Cursor resSectorKeyData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resSectorKeyData = db.rawQuery("select SectorNo,KeyB from " + Constants.SECTOR_KEY_TABLE, null);
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


    public int insertAttendanceData(String strEmpId, String strCardId, String strAttendanceDateTime, String strInOutMode, String verificationMode, String strLatitude, String strLongitude, byte[] image) {
        SQLiteDatabase db = null;
        int status = -1;
        try {
            String[] splitDateAndTime = strAttendanceDateTime.split(" ");
            ContentValues initialValues = new ContentValues();
            initialValues.put("Addr", "00");
            initialValues.put("EstablishmentCode", "00000001");
            initialValues.put("EmployeeID", Utility.paddEmpId(strEmpId));
            initialValues.put("CardID", Utility.paddCardId(strCardId));
            initialValues.put("PunchDate", splitDateAndTime[0].trim());
            initialValues.put("PunchTime", splitDateAndTime[1].replace(":", "").trim());
            initialValues.put("InOutMode", strInOutMode);
            initialValues.put("VerificationMode", verificationMode);
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
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            status = (int) db.insert(Constants.ATTENDANCE_TABLE, null, initialValues);
        } catch (SQLiteException e) {
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return status;
    }

    public Cursor getEmployeePhoto(String empId) {
        SQLiteDatabase db = null;
        Cursor resData = null;
        try {
            String strPaddedEmpId = Utility.paddEmpId(empId);
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select Photo from " + Constants.EMPLOYEE_TABLE + " where EmployeeId='" + strPaddedEmpId + "'", null);
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

    public int isDataAvailableInDatabase(String strEmployeeId) {
        SQLiteDatabase db = null;
        Cursor resData = null;
        int AutoId = -1;
        try {
            String strPaddedEmpId = Utility.paddEmpId(strEmployeeId);
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select AutoId from " + Constants.EMPLOYEE_TABLE + " where EmployeeId='" + strPaddedEmpId + "'", null);
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

    public int isDataAvailableInDatabaseByCardId(String strCardId) {
        SQLiteDatabase db = null;
        Cursor resData = null;
        int AutoId = -1;
        try {
            String strPaddedCardId = Utility.paddCardId(strCardId);
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select AutoId from " + Constants.EMPLOYEE_TABLE + " where CardId='" + strPaddedCardId + "'", null);
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


    public String getCardPinForVerification(int autoId) {
        SQLiteDatabase db = null;
        Cursor resData = null;
        String strCardPin = "";
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("SELECT PIN FROM " + Constants.EMPLOYEE_TABLE + " WHERE  AutoId=" + autoId, null);
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

    public ArrayList <String> getEmployeeFingerData(int AutoId) {
        SQLiteDatabase db = null;
        Cursor resFingerData = null;
        ArrayList <String> fingerData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resFingerData = db.rawQuery("select FingerIndex,Template from " + Constants.FINGER_TABLE + " where AutoId='" + AutoId + "'", null);
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

    public EmployeeInfo getEmployeeInfoByAutoId(int autoId, EmployeeInfo empInfo) {
        SQLiteDatabase db = null;
        Cursor rs = null;
        String selectQuery = "";
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            selectQuery = "SELECT EmployeeId,CardId,Name,Photo FROM " + Constants.EMPLOYEE_TABLE + " WHERE " + " AutoId=" + autoId + "";
            rs = db.rawQuery(selectQuery, null);
            if (rs != null && rs.getCount() > 0) {
                empInfo = new EmployeeInfo();
                while (rs.moveToNext()) {
                    empInfo.setEmpId(rs.getString(0));
                    empInfo.setCardId(rs.getString(1));
                    empInfo.setEmpName(rs.getString(2));
                    empInfo.setPhoto(rs.getBlob(3));
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
        return empInfo;
    }

    public SmartCardInfo getEmployeeInfoByAutoId(int autoId, SmartCardInfo empInfo) {
        SQLiteDatabase db = null;
        Cursor rs = null;
        String selectQuery = "";
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            selectQuery = "SELECT EmployeeId,CardId,Name,ValidUpto,BirthDay,PIN FROM " + Constants.EMPLOYEE_TABLE + " WHERE " + " AutoId=" + autoId + "";
            rs = db.rawQuery(selectQuery, null);
            if (rs != null && rs.getCount() > 0) {
                while (rs.moveToNext()) {
                    empInfo.setEmployeeId(rs.getString(0).trim());
                    empInfo.setCardId(rs.getString(1).replaceAll("\\G0", " ").trim());
                    empInfo.setEmpName(rs.getString(2).trim());
                    empInfo.setValidUpto(rs.getString(3).trim());
                    empInfo.setBirthDate(rs.getString(4).trim());
                    empInfo.setPin(rs.getString(5).trim());
                    empInfo.setSmartCardVer("");
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
        return empInfo;
    }

    public ArrayList getInTimeRange() {
        SQLiteDatabase db = null;
        Cursor resData = null;
        ArrayList <String> list = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select FromTime,ToTime from " + Constants.IN_OUT_MODE_TABLE + " where InOutMode='00' ", null);
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


    public ArrayList <String> getOutTimeRange() {
        SQLiteDatabase db = null;
        Cursor resData = null;
        ArrayList <String> list = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select FromTime,ToTime from " + Constants.IN_OUT_MODE_TABLE + " where InOutMode='01' ", null);
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

    public int getNoOfRecordsToBeSendToServer() {
        SQLiteDatabase db = null;
        Cursor resData = null;
        int noOfRecords = 0;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select count(*) from " + Constants.ATTENDANCE_TABLE + " where Uploaded='00'", null);
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

    public boolean isPasswordValid(String strPassword) {
        SQLiteDatabase db = null;
        Cursor resData = null;
        boolean isValid = false;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select AutoId from " + Constants.USER_TABLE + " where Password='" + strPassword + "'", null);
            if (resData != null && resData.getCount() > 0) {
                isValid = true;
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

        return isValid;
    }

    public String getFPEnrollMode() {
        SQLiteDatabase db = null;
        Cursor resData = null;
        String strMode = "";
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("SELECT Mode FROM " + Constants.FINGER_ENROLL_MODE_TABLE + " WHERE  AutoId=(SELECT MAX(AutoId) FROM " + Constants.FINGER_ENROLL_MODE_TABLE + ")", null);
            if (resData != null && resData.getCount() > 0) {
                while (resData.moveToNext()) {
                    strMode = resData.getString(0).trim();
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
        return strMode;
    }

    public int insertFingerEnrollMode(String mode, String strDateTime) {
        SQLiteDatabase db = null;
        int insertStatus = -1;
        try {
            ContentValues initialValues = new ContentValues();
            initialValues.put("Mode", mode);
            initialValues.put("ModeChangeOn", strDateTime);
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            insertStatus = (int) db.insert(Constants.FINGER_ENROLL_MODE_TABLE, null, initialValues);
        } catch (SQLiteException e) {

        } finally {
            if (db != null) {
                db.close();
            }
        }
        return insertStatus;
    }

    public Cursor getSettings() {
        SQLiteDatabase db = null;
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select SettingsParamName,ValueDetails from " + Constants.SETTINGS_TABLE, null);
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

    public boolean isEmpIdEnrolled(String strCellData) {
        SQLiteDatabase db = null;
        boolean isExists = false;
        Cursor resData = null;
        try {
            String strEmpId = Utility.paddEmpId(strCellData);
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select EmployeeId from " + Constants.EMPLOYEE_TABLE + " where EmployeeId='" + strEmpId + "'", null);
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
        SQLiteDatabase db = null;
        boolean isExists = false;
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select CardId from " + Constants.HOTLIST_TABLE + " where CardId='" + Utility.paddCardId(strCellData) + "'", null);
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

    public boolean isCardDataAvailableInDatabase(String strCardId) {
        SQLiteDatabase db = null;
        boolean isExists = false;
        Cursor resData = null;
        try {
            String strPaddedCardId = Utility.paddCardId(strCardId);
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select CardId from " + Constants.EMPLOYEE_TABLE + " where CardId='" + strPaddedCardId + "'", null);
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
        SQLiteDatabase db = null;
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

            //Admin Rights//
            initialValues.put("VerificationMode", "");
            initialValues.put("IsBlacklisted", "");
            initialValues.put("IsAccessRightEnabled", "");
            initialValues.put("IsAccessRightEnabled", "");
            initialValues.put("IsLockOpenWhenAllowed", "");
            initialValues.put("Photo", image);

            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            insertStatus = (int) db.insert(Constants.EMPLOYEE_TABLE, null, initialValues);

        } catch (SQLiteException e) {
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return insertStatus;
    }

    public int getNoOfRecordsToBeSend() {
        SQLiteDatabase db = null;
        Cursor resData = null;
        int noOfRecords = 0;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select count(*) from " + Constants.ATTENDANCE_TABLE + " where Uploaded='00'", null);
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

    public int deleteModeTime(String strMode) {
        SQLiteDatabase db = null;
        int deleteStaus = -1;
        String strCondition = "";
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            strCondition = "InOutMode" + "='" + strMode + "'";
            deleteStaus = db.delete(Constants.IN_OUT_MODE_TABLE, strCondition, null);
        } catch (SQLiteException e) {

        } finally {
            if (db != null) {
                db.close();
            }
        }
        return deleteStaus;
    }

    public int insertModeTime(String strDigitStartTime, String strDigitEndTime, String strInOutMode) {
        SQLiteDatabase db = null;
        int insertStatus = -1;
        try {
            ContentValues initialValues = new ContentValues();
            initialValues.put("FromTime", strDigitStartTime);
            initialValues.put("ToTime", strDigitEndTime);
            initialValues.put("InOutMode", strInOutMode);
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            insertStatus = (int) db.insert(Constants.IN_OUT_MODE_TABLE, null, initialValues);
        } catch (SQLiteException e) {
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return insertStatus;
    }


    public EmployeeEnrollInfo getEmpBasicDetails(String empId, EmployeeEnrollInfo empInfo) {
        SQLiteDatabase db = null;
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select * from " + Constants.EMPLOYEE_TABLE + " where EmployeeId='" + Utility.paddEmpId(empId) + "'", null);
            if (resData != null && resData.getCount() > 0) {
                while (resData.moveToNext()) {
                    int intColumnValue;
                    String strColumnValue;
                    intColumnValue = resData.getInt(resData.getColumnIndex("AutoId"));
                    empInfo.setEnrollmentNo(intColumnValue);
                    strColumnValue = resData.getString(resData.getColumnIndex("EmployeeId"));
                    if (strColumnValue != null && strColumnValue.trim().length() > 0) {
                        empInfo.setEmpId(strColumnValue);
                    }
                    strColumnValue = resData.getString(resData.getColumnIndex("CardId"));
                    if (strColumnValue != null && strColumnValue.trim().length() > 0) {
                        empInfo.setCardId(strColumnValue);
                    }
                    strColumnValue = resData.getString(resData.getColumnIndex("Name"));
                    if (strColumnValue != null && strColumnValue.trim().length() > 0) {
                        empInfo.setEmpName(strColumnValue);
                    }
                    strColumnValue = resData.getString(resData.getColumnIndex("MobileNo"));
                    if (strColumnValue != null && strColumnValue.trim().length() > 0) {
                        empInfo.setMobileNo(strColumnValue);
                    }
                    strColumnValue = resData.getString(resData.getColumnIndex("BloodGroup"));
                    if (strColumnValue != null && strColumnValue.trim().length() > 0) {
                        empInfo.setBloodGroup(strColumnValue);
                    }
                    strColumnValue = resData.getString(resData.getColumnIndex("MailId"));
                    if (strColumnValue != null && strColumnValue.trim().length() > 0) {
                        empInfo.setEmailId(strColumnValue);
                    }
                    strColumnValue = resData.getString(resData.getColumnIndex("ValidUpto"));
                    if (strColumnValue != null && strColumnValue.trim().length() > 0) {
                        empInfo.setValidUpto(strColumnValue);
                    }
                    strColumnValue = resData.getString(resData.getColumnIndex("BirthDay"));
                    if (strColumnValue != null && strColumnValue.trim().length() > 0) {
                        empInfo.setDateOfBirth(strColumnValue);
                    }
                    byte[] image = null;
                    image = resData.getBlob(resData.getColumnIndex("Photo"));
                    if (image != null) {
                        empInfo.setPhoto(image);
                    }
                }
            }
        } catch (Exception e) {
            return null;
        } finally {
            if (resData != null) {
                resData.close();
            }
            if (db != null) {
                db.close();
            }
        }

        return empInfo;
    }

    public EmployeeEnrollInfo setEmpExtraDetails(EmployeeEnrollInfo empInfo) {
        SQLiteDatabase db = null;
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select * from " + Constants.EMPLOYEE_TABLE + " where EmployeeId='" + Utility.paddEmpId(empInfo.getEmpId()) + "'", null);
            if (resData != null && resData.getCount() > 0) {
                while (resData.moveToNext()) {
                    int intColumnValue;
                    String strColumnValue = resData.getString(resData.getColumnIndex("PIN"));
                    if (strColumnValue != null && strColumnValue.trim().length() > 0) {
                        empInfo.setPin(strColumnValue);
                    }
                    strColumnValue = resData.getString(resData.getColumnIndex("GroupId"));
                    if (strColumnValue != null && strColumnValue.trim().length() > 0) {
                        empInfo.setGroupId(strColumnValue);
                    }
                    strColumnValue = resData.getString(resData.getColumnIndex("SiteCode"));
                    if (strColumnValue != null && strColumnValue.trim().length() > 0) {
                        empInfo.setSiteCode(strColumnValue);
                    }
                    strColumnValue = resData.getString(resData.getColumnIndex("fkTrainingCenter"));
                    if (strColumnValue != null && strColumnValue.trim().length() > 0) {
                        empInfo.setTrainingCenterId(strColumnValue);
                    }
                    strColumnValue = resData.getString(resData.getColumnIndex("fkBatchCenter"));
                    if (strColumnValue != null && strColumnValue.trim().length() > 0) {
                        empInfo.setBatchId(strColumnValue);
                    }
                    strColumnValue = resData.getString(resData.getColumnIndex("EnrollStatus"));
                    if (strColumnValue != null && strColumnValue.trim().length() > 0) {
                        if (strColumnValue.equals("Y")) {
                            empInfo.setFingerEnrolled(true);
                        } else if (strColumnValue.equals("N")) {
                            empInfo.setFingerEnrolled(false);
                        }
                    }
                    strColumnValue = resData.getString(resData.getColumnIndex("NosFinger"));
                    if (strColumnValue != null && strColumnValue.trim().length() > 0) {
                        intColumnValue = Integer.parseInt(strColumnValue);
                        empInfo.setNoOfFingersEnrolled(intColumnValue);
                    }
                    strColumnValue = resData.getString(resData.getColumnIndex("VerificationMode"));
                    if (strColumnValue != null && strColumnValue.trim().length() > 0) {
                        empInfo.setVerificationMode(strColumnValue);
                    }
                    strColumnValue = resData.getString(resData.getColumnIndex("SmartCardVersion"));
                    if (strColumnValue != null && strColumnValue.trim().length() > 0 && !strColumnValue.equals(Constants.DEFAULT_SMART_CARD_VERSION)) {
                        empInfo.setSmartCardVer(strColumnValue);
                    }
                    strColumnValue = resData.getString(resData.getColumnIndex("SmartCardSerialNo"));
                    if (strColumnValue != null && strColumnValue.trim().length() > 0) {
                        empInfo.setCSN(strColumnValue);
                    }
                    intColumnValue = resData.getInt(resData.getColumnIndex("IsBlacklisted"));
                    if (intColumnValue != -1) {
                        empInfo.setIsBlackListed(intColumnValue);
                    }
                    intColumnValue = resData.getInt(resData.getColumnIndex("IsAccessRightEnabled"));
                    if (intColumnValue != -1) {
                        empInfo.setIsAccessRightEnabled(intColumnValue);
                    }
                    intColumnValue = resData.getInt(resData.getColumnIndex("IsLockOpenWhenAllowed"));
                    if (intColumnValue != -1) {
                        empInfo.setIsLockOpen(intColumnValue);
                    }
                }
            }
        } catch (Exception e) {
            return null;
        } finally {
            if (resData != null) {
                resData.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return empInfo;
    }

    public EmployeeEnrollInfo getEmployeeBasicDetails(String empId, EmployeeEnrollInfo empInfo) {
        SQLiteDatabase db = null;
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select * from " + Constants.EMPLOYEE_TABLE + " where EmployeeId='" + Utility.paddEmpId(empId) + "'", null);
            if (resData != null && resData.getCount() > 0) {
                empInfo = new EmployeeEnrollInfo();
                while (resData.moveToNext()) {
                    int intColumnValue;
                    String strColumnValue;
                    intColumnValue = resData.getInt(resData.getColumnIndex("AutoId"));
                    empInfo.setEnrollmentNo(intColumnValue);
                    strColumnValue = resData.getString(resData.getColumnIndex("EmployeeId"));
                    if (strColumnValue != null && strColumnValue.trim().length() > 0) {
                        empInfo.setEmpId(strColumnValue);
                    }
                    strColumnValue = resData.getString(resData.getColumnIndex("CardId"));
                    if (strColumnValue != null && strColumnValue.trim().length() > 0) {
                        empInfo.setCardId(strColumnValue);
                    }
                    strColumnValue = resData.getString(resData.getColumnIndex("Name"));
                    if (strColumnValue != null && strColumnValue.trim().length() > 0) {
                        empInfo.setEmpName(strColumnValue);
                    }
                    strColumnValue = resData.getString(resData.getColumnIndex("MobileNo"));
                    if (strColumnValue != null && strColumnValue.trim().length() > 0) {
                        empInfo.setMobileNo(strColumnValue);
                    }
                    strColumnValue = resData.getString(resData.getColumnIndex("BloodGroup"));
                    if (strColumnValue != null && strColumnValue.trim().length() > 0) {
                        empInfo.setBloodGroup(strColumnValue);
                    }
                    strColumnValue = resData.getString(resData.getColumnIndex("MailId"));
                    if (strColumnValue != null && strColumnValue.trim().length() > 0) {
                        empInfo.setEmailId(strColumnValue);
                    }
                    strColumnValue = resData.getString(resData.getColumnIndex("ValidUpto"));
                    if (strColumnValue != null && strColumnValue.trim().length() > 0) {
                        empInfo.setValidUpto(strColumnValue);
                    }
                    strColumnValue = resData.getString(resData.getColumnIndex("BirthDay"));
                    if (strColumnValue != null && strColumnValue.trim().length() > 0) {
                        empInfo.setDateOfBirth(strColumnValue);
                    }

                    strColumnValue = resData.getString(resData.getColumnIndex("PIN"));
                    if (strColumnValue != null && strColumnValue.trim().length() > 0) {
                        empInfo.setPin(strColumnValue);
                    }
                    strColumnValue = resData.getString(resData.getColumnIndex("GroupId"));
                    if (strColumnValue != null && strColumnValue.trim().length() > 0) {
                        empInfo.setGroupId(strColumnValue);
                    }
                    strColumnValue = resData.getString(resData.getColumnIndex("SiteCode"));
                    if (strColumnValue != null && strColumnValue.trim().length() > 0) {
                        empInfo.setSiteCode(strColumnValue);
                    }
                    strColumnValue = resData.getString(resData.getColumnIndex("fkTrainingCenter"));
                    if (strColumnValue != null && strColumnValue.trim().length() > 0) {
                        empInfo.setTrainingCenterId(strColumnValue);
                    }
                    strColumnValue = resData.getString(resData.getColumnIndex("fkBatchCenter"));
                    if (strColumnValue != null && strColumnValue.trim().length() > 0) {
                        empInfo.setBatchId(strColumnValue);
                    }
                    strColumnValue = resData.getString(resData.getColumnIndex("EnrollStatus"));
                    if (strColumnValue != null && strColumnValue.trim().length() > 0) {
                        if (strColumnValue.equals("Y")) {
                            empInfo.setFingerEnrolled(true);
                        } else if (strColumnValue.equals("N")) {
                            empInfo.setFingerEnrolled(false);
                        }
                    }
                    strColumnValue = resData.getString(resData.getColumnIndex("NosFinger"));
                    if (strColumnValue != null && strColumnValue.trim().length() > 0) {
                        intColumnValue = Integer.parseInt(strColumnValue);
                        empInfo.setNoOfFingersEnrolled(intColumnValue);
                    }
                    strColumnValue = resData.getString(resData.getColumnIndex("VerificationMode"));
                    if (strColumnValue != null && strColumnValue.trim().length() > 0) {
                        empInfo.setVerificationMode(strColumnValue);
                    }
                    strColumnValue = resData.getString(resData.getColumnIndex("SmartCardVersion"));
                    if (strColumnValue != null && strColumnValue.trim().length() > 0 && !strColumnValue.equals(Constants.DEFAULT_SMART_CARD_VERSION)) {
                        empInfo.setSmartCardVer(strColumnValue);
                    }
                    strColumnValue = resData.getString(resData.getColumnIndex("SmartCardSerialNo"));
                    if (strColumnValue != null && strColumnValue.trim().length() > 0) {
                        empInfo.setCSN(strColumnValue);
                    }
                    intColumnValue = resData.getInt(resData.getColumnIndex("IsBlacklisted"));
                    if (intColumnValue != -1) {
                        empInfo.setIsBlackListed(intColumnValue);
                    }
                    intColumnValue = resData.getInt(resData.getColumnIndex("IsAccessRightEnabled"));
                    if (intColumnValue != -1) {
                        empInfo.setIsAccessRightEnabled(intColumnValue);
                    }
                    intColumnValue = resData.getInt(resData.getColumnIndex("IsLockOpenWhenAllowed"));
                    if (intColumnValue != -1) {
                        empInfo.setIsLockOpen(intColumnValue);
                    }
                    byte[] image = null;
                    image = resData.getBlob(resData.getColumnIndex("Photo"));
                    if (image != null) {
                        empInfo.setPhoto(image);
                    }
                }
            }
        } catch (Exception e) {
            return null;
        } finally {
            if (resData != null) {
                resData.close();
            }
            if (db != null) {
                db.close();
            }
        }

        return empInfo;
    }

    public String getGroupNameById(String groupId) {
        SQLiteDatabase db = null;
        String strGroupName = "";
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select Name from " + Constants.GROUP_TABLE + " where ID='" + groupId + "'", null);
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
        SQLiteDatabase db = null;
        String strSiteCode = "";
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select Code from " + Constants.SITE_TABLE + " where ID='" + siteId + "'", null);
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
        SQLiteDatabase db = null;
        String strTrainingCenterName = "";
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select CenterName from " + Constants.TRAINING_TABLE + " where ID='" + trainingCode + "'", null);
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
        SQLiteDatabase db = null;
        String strBatchName = "";
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select BatchName from " + Constants.BATCH_TABLE + " where ID='" + batchCode + "'", null);
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

    public String getAadhaarId(int empAutoId) {
        SQLiteDatabase db = null;
        Cursor resData = null;
        String strAdhaarId = "";
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select AadhaarId from " + Constants.AADHAARAUTH_TABLE + " where fkEmpId='" + Integer.toString(empAutoId) + "'", null);
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

    public int getBatchNamesCount() {
        SQLiteDatabase db = null;
        int noOfRecords = 0;
        Cursor resBatchNamesData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resBatchNamesData = db.rawQuery("select * from " + Constants.BATCH_TABLE, null);
            if (resBatchNamesData != null) {
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

    public void fillBatchNames(String[] batchNames, int[] pkBatchNames) {
        SQLiteDatabase db = null;
        Cursor resBatchNamesData = null;
        int i = 1;
        int noOfRecords = 0;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resBatchNamesData = db.rawQuery("select ID,BatchName from " + Constants.BATCH_TABLE, null);
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

    public int getTrainingCenterCount() {
        SQLiteDatabase db = null;
        int noOfRecords = 0;
        Cursor resTrainingCenterData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resTrainingCenterData = db.rawQuery("select * from " + Constants.TRAINING_TABLE, null);
            if (resTrainingCenterData != null) {
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

    public void fillTrainingCenters(String[] trainingCenters, int[] pkTrainingCenters) {
        SQLiteDatabase db = null;
        Cursor resTrainingCenterData = null;
        int i = 1;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resTrainingCenterData = db.rawQuery("select ID,CenterName from " + Constants.TRAINING_TABLE, null);
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

    public int getSiteCodeCount() {
        SQLiteDatabase db = null;
        Cursor resSiteCodeData = null;
        int noOfRecords = 0;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resSiteCodeData = db.rawQuery("select * from " + Constants.SITE_TABLE, null);
            if (resSiteCodeData != null) {
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

    public void fillSiteCodes(String[] siteCodes, int[] pkSiteCodes) {
        SQLiteDatabase db = null;
        Cursor resSiteCodeData = null;
        int i = 1;
        int noOfRecords = 0;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resSiteCodeData = db.rawQuery("select ID,Code from " + Constants.SITE_TABLE, null);
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

    public int getGroupNamesCount() {
        SQLiteDatabase db = null;
        Cursor resGroupData = null;
        int noOfRecords = 0;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resGroupData = db.rawQuery("select * from " + Constants.GROUP_TABLE, null);
            if (resGroupData != null) {
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

    public void fillGroupNames(String[] groupNames, int[] pkGroupNames) {
        SQLiteDatabase db = null;
        Cursor resGroupData = null;
        int i = 1;
        int noOfRecords = 0;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resGroupData = db.rawQuery("select ID,Name from " + Constants.GROUP_TABLE, null);
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

    public String getCardIdByEmpId(String empId) {
        SQLiteDatabase db = null;
        Cursor resData = null;
        String strCardId = "";
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select CardId from " + Constants.EMPLOYEE_TABLE + " where EmployeeId='" + Utility.paddEmpId(empId) + "'", null);
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

    public String isAadhaarIdEnrolled(String strAadhaarId) {
        SQLiteDatabase db = null;
        Cursor resData = null;
        String autoId = "";
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select AutoId from " + Constants.AADHAARAUTH_TABLE + " where AadhaarId='" + strAadhaarId + "'", null);
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

    public int insertLocallyEnrolledEmployeeData(EmployeeFingerEnrollInfo info) {
        SQLiteDatabase db = null;
        int insertVal = -1;
        String value;
        int len;
        try {
            ContentValues initialValues = new ContentValues();
            value = info.getEmpId();
            if (value != null) {
                len = value.length();
                if (len < 16) {
                    String strPaddedEmpId = "";
                    strPaddedEmpId = Utility.paddEmpId(info.getEmpId());
                    initialValues.put("EmployeeID", strPaddedEmpId);
                } else {
                    initialValues.put("EmployeeID", value);
                }
            } else {
                return -1;
            }
            value = info.getCardId();
            if (value != null) {
                len = value.length();
                if (len < 8) {
                    String strPaddedCardId = "";
                    strPaddedCardId = Utility.paddCardId(value);
                    initialValues.put("CardId", strPaddedCardId);
                } else {
                    initialValues.put("CardId", value);
                }
            } else {
                return -1;
            }
            value = info.getEmpName();
            if (value != null) {
                len = value.length();
                if (len < 16) {
                    String strPaddedEmpName = "";
                    strPaddedEmpName = Utility.paddEmpName(value);
                    initialValues.put("Name", strPaddedEmpName);
                } else {
                    initialValues.put("Name", value);
                }
            } else {
                return -1;
            }
            initialValues.put("BloodGroup", "");
            initialValues.put("SiteCode", "");
            initialValues.put("MobileNo", "");
            initialValues.put("MailId", "");
            initialValues.put("ValidUpto", "");
            initialValues.put("BirthDay", "");
            initialValues.put("PIN", info.getPin());
            initialValues.put("GroupId", "");
            initialValues.put("fkTrainingCenter", "");
            initialValues.put("fkBatchCenter", "");

            //Non Editable Fields//
            boolean status = false;
            if (status) {
                initialValues.put("EnrollStatus", "Y");
            } else {
                initialValues.put("EnrollStatus", "N");
            }
            initialValues.put("NosFinger", "");
            initialValues.put("VerificationMode", "");
            initialValues.put("SmartCardSerialNo", "");
            initialValues.put("SmartCardVersion", Constants.DEFAULT_SMART_CARD_VERSION);
            //Non Editable Fields//

            //Admin Rights//
            initialValues.put("IsBlacklisted", "");
            initialValues.put("IsAccessRightEnabled", "");
            initialValues.put("IsLockOpenWhenAllowed", "");
            //Admin Rights//

            initialValues.put("EnrollSource", "L");
            initialValues.put("JobCode", "");
            initialValues.put("Photo", "");

            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            insertVal = (int) db.insert(Constants.EMPLOYEE_TABLE, null, initialValues);

        } catch (SQLiteException e) {
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return insertVal;
    }

    public EmployeeEnrollInfo insertLocallyEnrolledEmployeeData(EmployeeEnrollInfo info) {
        SQLiteDatabase db = null;
        int insertVal = -1;
        String value;
        int len;
        try {
            info.setDbStatus(-1);
            ContentValues initialValues = new ContentValues();
            value = info.getEmpId();
            if (value != null) {
                len = value.length();
                if (len < 16) {
                    String strPaddedEmpId = "";
                    strPaddedEmpId = Utility.paddEmpId(info.getEmpId());
                    initialValues.put("EmployeeID", strPaddedEmpId);
                } else {
                    initialValues.put("EmployeeID", value);
                }
            } else {
                return info;
            }
            value = info.getCardId();
            if (value != null) {
                len = value.length();
                if (len < 8) {
                    String strPaddedCardId = "";
                    strPaddedCardId = Utility.paddCardId(value);
                    initialValues.put("CardId", strPaddedCardId);
                } else {
                    initialValues.put("CardId", value);
                }
            } else {
                return info;
            }
            value = info.getEmpName();
            if (value != null) {
                len = value.length();
                if (len < 16) {
                    String strPaddedEmpName = "";
                    strPaddedEmpName = Utility.paddEmpName(value);
                    initialValues.put("Name", strPaddedEmpName);
                } else {
                    initialValues.put("Name", value);
                }
            } else {
                return info;
            }
            initialValues.put("BloodGroup", info.getBloodGroup());
            initialValues.put("SiteCode", info.getSiteCode());
            initialValues.put("MobileNo", info.getMobileNo());
            initialValues.put("MailId", info.getEmailId());
            initialValues.put("ValidUpto", info.getValidUpto());
            initialValues.put("BirthDay", info.getDateOfBirth());
            initialValues.put("PIN", info.getPin());
            initialValues.put("GroupId", info.getGroupId());
            initialValues.put("fkTrainingCenter", info.getTrainingCenterId());
            initialValues.put("fkBatchCenter", info.getBatchId());

            //Non Editable Fields//
            boolean status = false;
            if (status) {
                initialValues.put("EnrollStatus", "Y");
            } else {
                initialValues.put("EnrollStatus", "N");
            }
            initialValues.put("NosFinger", "");
            initialValues.put("VerificationMode", "");
            initialValues.put("SmartCardSerialNo", "");
            initialValues.put("SmartCardVersion", Constants.DEFAULT_SMART_CARD_VERSION);
            //Non Editable Fields//

            //Admin Rights//
            initialValues.put("IsBlacklisted", info.getIsBlackListed());
            initialValues.put("IsAccessRightEnabled", info.getIsAccessRightEnabled());
            initialValues.put("IsLockOpenWhenAllowed", info.getIsLockOpen());
            //Admin Rights//

            initialValues.put("EnrollSource", "L");
            initialValues.put("JobCode", "0000");

            byte[] image = info.getPhoto();
            if (image != null && image.length > 1) {
                initialValues.put("Photo", image);
            } else {
                image = null;
                initialValues.put("Photo", image);
            }

            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            insertVal = (int) db.insert(Constants.EMPLOYEE_TABLE, null, initialValues);

            info.setEnrollmentNo(insertVal);
            info.setDbStatus(insertVal);

        } catch (SQLiteException e) {
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return info;
    }


    public EmpValidationDownloadInfo insertRemotelyEnrolledEmployeeData(EmpValidationDownloadInfo info) {
        SQLiteDatabase db = null;
        int insertVal = -1;
        String value;
        int len;
        try {
            info.setDbStatus(-1);//reset
            ContentValues initialValues = new ContentValues();
            value = info.getEmpId();
            if (value != null) {
                len = value.length();
                if (len < 16) {
                    String strPaddedEmpId = "";
                    strPaddedEmpId = Utility.paddEmpId(info.getEmpId());
                    initialValues.put("EmployeeID", strPaddedEmpId);
                } else {
                    initialValues.put("EmployeeID", value);
                }
            } else {
                return info;
            }
            value = info.getCardId();
            if (value != null) {
                len = value.length();
                if (len < 8) {
                    String strPaddedCardId = "";
                    strPaddedCardId = Utility.paddCardId(value);
                    initialValues.put("CardId", strPaddedCardId);
                } else {
                    initialValues.put("CardId", value);
                }
            } else {
                return info;
            }
            value = info.getEmpName();
            if (value != null) {
                len = value.length();
                if (len < 16) {
                    String strPaddedEmpName = "";
                    strPaddedEmpName = Utility.paddEmpName(value);
                    initialValues.put("Name", strPaddedEmpName);
                } else {
                    initialValues.put("Name", value);
                }
            } else {
                return info;
            }
            initialValues.put("BloodGroup", info.getBloodGrp());
            initialValues.put("SiteCode", info.getSiteCode());
            initialValues.put("MobileNo", info.getMobileNo());
            initialValues.put("MailId", info.getEmailId());
            initialValues.put("ValidUpto", info.getDov());
            initialValues.put("BirthDay", info.getDob());
            initialValues.put("PIN", info.getPin());
            initialValues.put("GroupId", "");
            initialValues.put("fkTrainingCenter", "");
            initialValues.put("fkBatchCenter", "");

            //Non Editable Fields//
            boolean status = false;
            if (status) {
                initialValues.put("EnrollStatus", "Y");
            } else {
                initialValues.put("EnrollStatus", "N");
            }
            initialValues.put("NosFinger", "");
            initialValues.put("VerificationMode", "");
            initialValues.put("SmartCardSerialNo", "");
            initialValues.put("SmartCardVersion", Constants.DEFAULT_SMART_CARD_VERSION);
            //Non Editable Fields//

            //Admin Rights//
            initialValues.put("IsBlacklisted", info.getIsBlackListed());
            initialValues.put("IsAccessRightEnabled", "");
            initialValues.put("IsLockOpenWhenAllowed", info.getIsLockOpen());
            //Admin Rights//

            initialValues.put("EnrollSource", info.getEnrollSource());
            initialValues.put("JobCode", info.getJobCode());

            byte[] image = null;
            String photo = info.getPp();
            if (photo != null && photo.trim().length() > 0) {
                BASE64Decoder decoder = new BASE64Decoder();
                try {
                    image = decoder.decodeBuffer(photo);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                initialValues.put("Photo", image);
            } else {

                initialValues.put("Photo", image);
            }

            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            insertVal = (int) db.insert(Constants.EMPLOYEE_TABLE, null, initialValues);

            info.setDbStatus(insertVal);
            info.setEnrollmentNo(insertVal);

        } catch (SQLiteException e) {
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return info;
    }

    public EmployeeValidationBasicInfo insertRemotelyEnrolledEmployeeData(EmployeeValidationBasicInfo info) {
        SQLiteDatabase db = null;
        int insertVal = -1;
        String value;
        int len;
        try {
            ContentValues initialValues = new ContentValues();
            value = info.getEmpId();
            if (value != null) {
                len = value.length();
                if (len < 16) {
                    String strPaddedEmpId = "";
                    strPaddedEmpId = Utility.paddEmpId(info.getEmpId());
                    initialValues.put("EmployeeID", strPaddedEmpId);
                } else {
                    initialValues.put("EmployeeID", value);
                }
            } else {
                return info;
            }
            value = info.getCardId();
            if (value != null) {
                len = value.length();
                if (len < 8) {
                    String strPaddedCardId = "";
                    strPaddedCardId = Utility.paddCardId(value);
                    initialValues.put("CardId", strPaddedCardId);
                } else {
                    initialValues.put("CardId", value);
                }
            } else {
                return info;
            }
            value = info.getEmpName();
            if (value != null) {
                len = value.length();
                if (len < 16) {
                    String strPaddedEmpName = "";
                    strPaddedEmpName = Utility.paddEmpName(value);
                    initialValues.put("Name", strPaddedEmpName);
                } else {
                    initialValues.put("Name", value);
                }
            } else {
                return info;
            }

            initialValues.put("EmpTypeId", info.getEmpType());
            initialValues.put("BloodGroup", info.getBg());
            initialValues.put("SiteCode", info.getSc());
            initialValues.put("MobileNo", info.getMn());
            initialValues.put("MailId", info.getEid());
            initialValues.put("ValidUpto", info.getDov());
            initialValues.put("BirthDay", info.getDob());
            initialValues.put("PIN", info.getPin());
            initialValues.put("GroupId", "");
            initialValues.put("fkTrainingCenter", "");
            initialValues.put("fkBatchCenter", "");

            //Non Editable Fields//
            boolean status = false;
            if (status) {
                initialValues.put("EnrollStatus", "Y");
            } else {
                initialValues.put("EnrollStatus", "N");
            }
            initialValues.put("NosFinger", "");
            initialValues.put("VerificationMode", "");
            initialValues.put("SmartCardSerialNo", "");
            initialValues.put("SmartCardVersion", Constants.DEFAULT_SMART_CARD_VERSION);
            //Non Editable Fields//

            //Admin Rights//
            initialValues.put("IsBlacklisted", info.getIsBlackListed());
            initialValues.put("IsAccessRightEnabled", "");
            initialValues.put("IsLockOpenWhenAllowed", info.getIsLockOpen());
            //Admin Rights//

            initialValues.put("EnrollSource", "R");
            initialValues.put("Photo", "");

            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            insertVal = (int) db.insert(Constants.EMPLOYEE_TABLE, null, initialValues);

            info.setEnrollmentNo(insertVal);

        } catch (SQLiteException e) {
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return info;
    }


    public TemplateDownloadInfo insertRemotelyEnrolledEmployeeData(TemplateDownloadInfo info) {
        SQLiteDatabase db = null;
        int insertVal = -1;
        String value;
        int len;
        try {
            info.setDbStatus(-1);//reset
            ContentValues initialValues = new ContentValues();
            value = info.getEmpId();
            if (value != null) {
                len = value.length();
                if (len < 16) {
                    String strPaddedEmpId = "";
                    strPaddedEmpId = Utility.paddEmpId(info.getEmpId());
                    initialValues.put("EmployeeID", strPaddedEmpId);
                } else {
                    initialValues.put("EmployeeID", value);
                }
            } else {
                return info;
            }
            value = info.getCardId();
            if (value != null) {
                len = value.length();
                if (len < 8) {
                    String strPaddedCardId = "";
                    strPaddedCardId = Utility.paddCardId(value);
                    initialValues.put("CardId", strPaddedCardId);
                } else {
                    initialValues.put("CardId", value);
                }
            } else {
                return info;
            }
            value = info.getEmpName();
            if (value != null) {
                len = value.length();
                if (len < 16) {
                    String strPaddedEmpName = "";
                    strPaddedEmpName = Utility.paddEmpName(value);
                    initialValues.put("Name", strPaddedEmpName);
                } else {
                    initialValues.put("Name", value);
                }
            } else {
                return info;
            }
            initialValues.put("BloodGroup", info.getBloodGrp());
            initialValues.put("SiteCode", info.getSiteCode());
            initialValues.put("MobileNo", info.getMobileNo());
            initialValues.put("MailId", info.getEmailId());
            initialValues.put("ValidUpto", info.getDov());
            initialValues.put("BirthDay", info.getDob());
            initialValues.put("PIN", info.getPin());
            initialValues.put("GroupId", "");
            initialValues.put("fkTrainingCenter", "");
            initialValues.put("fkBatchCenter", "");

            //Non Editable Fields//
            boolean status = false;
            if (status) {
                initialValues.put("EnrollStatus", "Y");
            } else {
                initialValues.put("EnrollStatus", "N");
            }
            initialValues.put("NosFinger", "");
            initialValues.put("VerificationMode", "");
            initialValues.put("SmartCardSerialNo", "");
            initialValues.put("SmartCardVersion", Constants.DEFAULT_SMART_CARD_VERSION);
            //Non Editable Fields//

            //Admin Rights//
            initialValues.put("IsBlacklisted", info.getIsBlackListed());
            initialValues.put("IsAccessRightEnabled", "");
            initialValues.put("IsLockOpenWhenAllowed", info.getIsLockOpen());
            //Admin Rights//

            initialValues.put("EnrollSource", "R");
            initialValues.put("JobCode", info.getCommand());

            initialValues.put("Photo", "");

            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            insertVal = (int) db.insert(Constants.EMPLOYEE_TABLE, null, initialValues);

            info.setDbStatus(insertVal);
            info.setEnrollmentNo(insertVal);

        } catch (SQLiteException e) {

        } finally {
            if (db != null) {
                db.close();
            }
        }
        return info;
    }


    public int insertToAadhaarAuthTable(int pkEmpId, String strAadhaarId) {
        SQLiteDatabase db = null;
        int status = -1;
        try {
            ContentValues initialValues = new ContentValues();
            initialValues.put("fkEmpId", Integer.toString(pkEmpId).trim());
            initialValues.put("AadhaarId", strAadhaarId);
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            status = (int) db.insert(Constants.AADHAARAUTH_TABLE, null, initialValues);
        } catch (SQLiteException e) {

        } finally {
            if (db != null) {
                db.close();
            }
        }
        return status;
    }


    public EmployeeEnrollInfo updateEmployeeData(EmployeeEnrollInfo empInfo) {
        SQLiteDatabase db = null;
        int updateStatus = -1;
        String value;
        int len;
        try {
            empInfo.setDbStatus(-1);
            ContentValues initialValues = new ContentValues();
            value = empInfo.getEmpId();
            if (value != null) {
                len = value.length();
                if (len < 16) {
                    String strPaddedEmpId = "";
                    strPaddedEmpId = Utility.paddEmpId(empInfo.getEmpId());
                    initialValues.put("EmployeeID", strPaddedEmpId);
                } else {
                    initialValues.put("EmployeeID", value);
                }
            } else {
                return empInfo;
            }
            value = empInfo.getCardId();
            if (value != null) {
                len = value.length();
                if (len < 8) {
                    String strPaddedCardId = "";
                    strPaddedCardId = Utility.paddCardId(value);
                    initialValues.put("CardId", strPaddedCardId);
                } else {
                    initialValues.put("CardId", value);
                }
            } else {
                return empInfo;
            }
            value = empInfo.getEmpName();
            if (value != null) {
                len = value.length();
                if (len < 16) {
                    String strPaddedEmpName = "";
                    strPaddedEmpName = Utility.paddEmpName(value);
                    initialValues.put("Name", strPaddedEmpName);
                } else {
                    initialValues.put("Name", value);
                }
            } else {
                return empInfo;
            }
            initialValues.put("BloodGroup", empInfo.getBloodGroup());
            initialValues.put("SiteCode", empInfo.getSiteCode());
            initialValues.put("MobileNo", empInfo.getMobileNo());
            initialValues.put("MailId", empInfo.getEmailId());
            initialValues.put("ValidUpto", empInfo.getValidUpto());
            initialValues.put("BirthDay", empInfo.getDateOfBirth());
            initialValues.put("PIN", empInfo.getPin());
            initialValues.put("GroupId", empInfo.getGroupId());
            initialValues.put("fkTrainingCenter", empInfo.getTrainingCenterId());
            initialValues.put("fkBatchCenter", empInfo.getBatchId());

            //Non Editable Fields//
            boolean status = empInfo.isFingerEnrolled();
            if (status) {
                initialValues.put("EnrollStatus", "Y");
            } else {
                initialValues.put("EnrollStatus", "N");
            }
            initialValues.put("NosFinger", Integer.toString(empInfo.getNoOfFingersEnrolled()));
            initialValues.put("VerificationMode", empInfo.getVerificationMode());

            if (empInfo.getCSN().trim().length() == 0) {
                initialValues.put("SmartCardSerialNo", "");
            } else {
                initialValues.put("SmartCardSerialNo", empInfo.getCSN().trim());
            }
            if (empInfo.getSmartCardVer().trim().length() == 0) {
                initialValues.put("SmartCardVersion", Constants.DEFAULT_SMART_CARD_VERSION);
            } else {
                initialValues.put("SmartCardVersion", empInfo.getSmartCardVer());
            }
            //Non Editable Fields//

            //Admin Rights//
            initialValues.put("IsBlacklisted", empInfo.getIsBlackListed());
            initialValues.put("IsAccessRightEnabled", empInfo.getIsAccessRightEnabled());
            initialValues.put("IsLockOpenWhenAllowed", empInfo.getIsLockOpen());
            //Admin Rights//

            initialValues.put("EnrollSource", empInfo.getEnrollSource());
            initialValues.put("Photo", empInfo.getPhoto());

            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            updateStatus = db.update(Constants.EMPLOYEE_TABLE, initialValues, "AutoId=" + empInfo.getEnrollmentNo(), null);
            empInfo.setDbStatus(updateStatus);

        } catch (SQLiteException e) {

        } finally {
            if (db != null) {
                db.close();
            }
        }
        return empInfo;
    }

    public TemplateDownloadInfo updateEmployeeData(TemplateDownloadInfo empInfo) {
        SQLiteDatabase db = null;
        int updateStatus = -1;
        String value;
        int len;
        try {
            empInfo.setDbStatus(-1);
            ContentValues initialValues = new ContentValues();
            value = empInfo.getEmpId();
            if (value != null) {
                len = value.length();
                if (len < 16) {
                    String strPaddedEmpId = "";
                    strPaddedEmpId = Utility.paddEmpId(empInfo.getEmpId());
                    initialValues.put("EmployeeID", strPaddedEmpId);
                } else {
                    initialValues.put("EmployeeID", value);
                }
            } else {
                return empInfo;
            }
            value = empInfo.getCardId();
            if (value != null) {
                len = value.length();
                if (len < 8) {
                    String strPaddedCardId = "";
                    strPaddedCardId = Utility.paddCardId(value);
                    initialValues.put("CardId", strPaddedCardId);
                } else {
                    initialValues.put("CardId", value);
                }
            } else {
                return empInfo;
            }
            value = empInfo.getEmpName();
            if (value != null) {
                len = value.length();
                if (len < 16) {
                    String strPaddedEmpName = "";
                    strPaddedEmpName = Utility.paddEmpName(value);
                    initialValues.put("Name", strPaddedEmpName);
                } else {
                    initialValues.put("Name", value);
                }
            } else {
                return empInfo;
            }
            initialValues.put("BloodGroup", empInfo.getBloodGrp());
            initialValues.put("SiteCode", empInfo.getSiteCode());
            initialValues.put("MobileNo", empInfo.getMobileNo());
            initialValues.put("MailId", empInfo.getEmailId());
            initialValues.put("ValidUpto", empInfo.getDov());
            initialValues.put("BirthDay", empInfo.getDob());
            initialValues.put("PIN", empInfo.getPin());
            initialValues.put("GroupId", "");
            initialValues.put("fkTrainingCenter", "");
            initialValues.put("fkBatchCenter", "");

            //Non Editable Fields//
            boolean status = false;
            if (status) {
                initialValues.put("EnrollStatus", "Y");
            } else {
                initialValues.put("EnrollStatus", "N");
            }
            initialValues.put("NosFinger", "");
            initialValues.put("VerificationMode", empInfo.getVerificationMode());
            initialValues.put("SmartCardSerialNo", "");
            initialValues.put("SmartCardVersion", Constants.DEFAULT_SMART_CARD_VERSION);

            //Non Editable Fields//

            //Admin Rights//
            initialValues.put("IsBlacklisted", empInfo.getIsBlackListed());
            initialValues.put("IsAccessRightEnabled", "");
            initialValues.put("IsLockOpenWhenAllowed", empInfo.getIsLockOpen());
            //Admin Rights//

            initialValues.put("EnrollSource", "R");
            initialValues.put("Photo", "");

            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            updateStatus = db.update(Constants.EMPLOYEE_TABLE, initialValues, "AutoId=" + empInfo.getEnrollmentNo(), null);
            empInfo.setDbStatus(updateStatus);

        } catch (SQLiteException e) {

        } finally {
            if (db != null) {
                db.close();
            }
        }
        return empInfo;
    }

    public EmployeeValidationBasicInfo updateEmployeeData(EmployeeValidationBasicInfo empInfo) {
        SQLiteDatabase db = null;
        int updateStatus = -1;
        String value;
        int len;
        try {
            ContentValues initialValues = new ContentValues();
            value = empInfo.getEmpId();
            if (value != null) {
                len = value.length();
                if (len < 16) {
                    String strPaddedEmpId = "";
                    strPaddedEmpId = Utility.paddEmpId(empInfo.getEmpId());
                    initialValues.put("EmployeeID", strPaddedEmpId);
                } else {
                    initialValues.put("EmployeeID", value);
                }
            } else {
                return empInfo;
            }
            value = empInfo.getCardId();
            if (value != null) {
                len = value.length();
                if (len < 8) {
                    String strPaddedCardId = "";
                    strPaddedCardId = Utility.paddCardId(value);
                    initialValues.put("CardId", strPaddedCardId);
                } else {
                    initialValues.put("CardId", value);
                }
            } else {
                return empInfo;
            }
            value = empInfo.getEmpName();
            if (value != null) {
                len = value.length();
                if (len < 16) {
                    String strPaddedEmpName = "";
                    strPaddedEmpName = Utility.paddEmpName(value);
                    initialValues.put("Name", strPaddedEmpName);
                } else {
                    initialValues.put("Name", value);
                }
            } else {
                return empInfo;
            }
            initialValues.put("EmpTypeId", empInfo.getEmpType());

            initialValues.put("BloodGroup", empInfo.getBg());
            initialValues.put("SiteCode", empInfo.getSc());
            initialValues.put("MobileNo", empInfo.getMn());
            initialValues.put("MailId", empInfo.getEid());

            initialValues.put("ValidUpto", empInfo.getDov());
            initialValues.put("BirthDay", empInfo.getDob());

            initialValues.put("PIN", empInfo.getPin());
            initialValues.put("GroupId", "");
            initialValues.put("fkTrainingCenter", "");
            initialValues.put("fkBatchCenter", "");

            //Non Editable Fields//

//            boolean status = false;
//            if (status) {
//                initialValues.put("EnrollStatus", "Y");
//            } else {
//                initialValues.put("EnrollStatus", "N");
//            }
//            initialValues.put("NosFinger", "");

            // initialValues.put("VerificationMode", empInfo.getVm());

            initialValues.put("SmartCardSerialNo", "");
            initialValues.put("SmartCardVersion", Constants.DEFAULT_SMART_CARD_VERSION);

            //Non Editable Fields//

            //Admin Rights//
            initialValues.put("IsBlacklisted", empInfo.getIsBlackListed());
            initialValues.put("IsAccessRightEnabled", "");
            initialValues.put("IsLockOpenWhenAllowed", empInfo.getIsLockOpen());
            //Admin Rights//

            initialValues.put("EnrollSource", "R");
            initialValues.put("Photo", "");

            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            updateStatus = db.update(Constants.EMPLOYEE_TABLE, initialValues, "AutoId=" + empInfo.getEnrollmentNo(), null);

        } catch (SQLiteException e) {

        } finally {
            if (db != null) {
                db.close();
            }
        }
        return empInfo;
    }


    public int checkTemplateExistsByFT(int autoId, String fingerType) {
        SQLiteDatabase db = null;
        Cursor resFingerData = null;
        int id = -1;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resFingerData = db.rawQuery("select ID from " + Constants.FINGER_TABLE + " where AutoID='" + Integer.toString(autoId) + "' and TemplateSrNo='" + fingerType + "'", null);
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

    public int isDataAvailableInAadhaarTable(String strAadhaarId) {
        SQLiteDatabase db = null;
        int AutoId = -1;
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select AutoId from " + Constants.AADHAARAUTH_TABLE + " where AadhaarId='" + strAadhaarId + "'", null);
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

    public int updateToAadhaarAuthTable(int AutoId, String strAadhaarId) {
        SQLiteDatabase db = null;
        int updateStatus = -1;
        try {
            ContentValues initialValues = new ContentValues();
            initialValues.put("AadhaarId", strAadhaarId);
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            updateStatus = db.update(Constants.AADHAARAUTH_TABLE, initialValues, "AutoId=" + AutoId, null);
        } catch (SQLiteException e) {

        } finally {
            if (db != null) {
                db.close();
            }
        }
        return updateStatus;
    }

    public int isDataAvailableInAadhaarTableByEmpId(int empId) {
        SQLiteDatabase db = null;
        int AutoId = -1;
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select AutoId from " + Constants.AADHAARAUTH_TABLE + " where fkEmpId='" + Integer.toString(empId) + "'", null);
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

    public boolean isTemplateAadhaarVerified(int enrollmentNo) {
        SQLiteDatabase db = null;
        boolean isAadhaarVerified = false;
        Cursor resFingerData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resFingerData = db.rawQuery("select isTemplateAadhaarVerifiedYorN from " + Constants.EMPLOYEE_TABLE + " where AutoId='" + Integer.toString(enrollmentNo) + "'", null);
            if (resFingerData != null) {
                if (resFingerData.getCount() > 0) {
                    while (resFingerData.moveToNext()) {
                        String value = resFingerData.getString(3);
                        if (value != null && value.trim().length() > 0) {
                            if (value.equals("Y")) {
                                isAadhaarVerified = true;
                            }
                        }
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
        return isAadhaarVerified;
    }

    public ArrayList <BasicEmployeeInfo> getEnrolledSearchList(ArrayList <BasicEmployeeInfo> empList) {
        SQLiteDatabase db = null;
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select EmployeeId, CardId, Name, EnrollStatus,nosFinger from " + Constants.EMPLOYEE_TABLE, null);
            if (resData != null && resData.getCount() > 0) {
                empList = new ArrayList <BasicEmployeeInfo>();
                while (resData.moveToNext()) {
                    BasicEmployeeInfo employeeDetails = new BasicEmployeeInfo();
                    employeeDetails.setEmployeeID(resData.getString(0).trim());
                    employeeDetails.setCardID(resData.getString(1).trim());
                    employeeDetails.setEmployeeName(resData.getString(2).trim());
                    String enrollStatus = resData.getString(3).trim();
                    if (enrollStatus.trim().equals("Y")) {
                        employeeDetails.setNosFinger(resData.getString(4).trim());
                    } else {
                        employeeDetails.setNosFinger("");
                    }
                    employeeDetails.setEnrolledStatus(enrollStatus);
                    empList.add(employeeDetails);
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
        return empList;
    }

    public Cursor getEmpDetailsByEmpId(String empId) {
        SQLiteDatabase db = null;
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select AutoId,CardId,Name,Photo from " + Constants.EMPLOYEE_TABLE + " where EmployeeId='" + Utility.paddEmpId(empId) + "'", null);
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

    public int deleteEmployeeDataByAutoId(int autoId) {
        SQLiteDatabase db = null;
        int status = -1;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            status = db.delete(Constants.EMPLOYEE_TABLE, "AutoID=" + autoId, null);
        } catch (SQLiteException e) {

        } finally {
            if (db != null) {
                db.close();
            }
        }
        return status;
    }

    public ArrayList <EmployeeFingerInfo> getFingerDetailsByEmployeeEnrollmentNo(int intAutoId, ArrayList <EmployeeFingerInfo> empFingerInfoList) {
        SQLiteDatabase db = null;
        Cursor resFingerData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resFingerData = db.rawQuery("select ID,TemplateSrNo,FingerIndex,SecurityLevel,VerificationMode,Quality,Template from " + Constants.FINGER_TABLE + " where AutoId='" + intAutoId + "'", null);
            if (resFingerData != null) {
                if (resFingerData.getCount() > 0) {
                    empFingerInfoList = new ArrayList <EmployeeFingerInfo>();
                    boolean isFirstRecord = true;
                    while (resFingerData.moveToNext()) {
                        EmployeeFingerInfo empFingerInfo = new EmployeeFingerInfo();
                        if (isFirstRecord) {
                            empFingerInfo.setFingerId(resFingerData.getInt(0));
                            empFingerInfo.setTemplateSrNo(resFingerData.getString(1));
                            empFingerInfo.setFingerIndex(resFingerData.getString(2));
                            empFingerInfo.setSecurityLevel(resFingerData.getString(3));
                            empFingerInfo.setVerificationMode(resFingerData.getString(4));
                            empFingerInfo.setFingerQuality(resFingerData.getString(5));
                            empFingerInfo.setFingerHexData(resFingerData.getString(6));
                            isFirstRecord = false;
                            empFingerInfoList.add(empFingerInfo);
                        } else {
                            empFingerInfo.setFingerId(resFingerData.getInt(0));
                            empFingerInfo.setTemplateSrNo(resFingerData.getString(1));
                            empFingerInfo.setFingerIndex(resFingerData.getString(2));
                            empFingerInfo.setSecurityLevel(resFingerData.getString(3));
                            empFingerInfo.setVerificationMode(resFingerData.getString(4));
                            empFingerInfo.setFingerQuality(resFingerData.getString(5));
                            empFingerInfo.setFingerHexData(resFingerData.getString(6));
                            empFingerInfoList.add(empFingerInfo);
                        }
                    }
                }
            }
        } catch (Exception e) {
        } finally {
            if (db != null) {
                db.close();
            }
        }

        return empFingerInfoList;
    }


    public SmartCardInfo getFingerDetailsByAutoId(int intAutoId, SmartCardInfo empInfo) {
        SQLiteDatabase db = null;
        Cursor resFingerData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resFingerData = db.rawQuery("select FingerIndex,VerificationMode,Template from " + Constants.FINGER_TABLE + " where AutoId='" + intAutoId + "'", null);
            if (resFingerData != null) {
                if (resFingerData.getCount() > 0) {
                    boolean isFirstRecord = true;
                    while (resFingerData.moveToNext()) {
                        if (isFirstRecord) {
                            empInfo.setFirstFingerIndex(resFingerData.getString(0));
                            empInfo.setFirstFingerVerificationMode(resFingerData.getString(1));
                            empInfo.setFirstFingerTemplate(resFingerData.getString(2));
                            empInfo.setNoOfTemplates(1);
                            isFirstRecord = false;
                        } else {
                            empInfo.setSecondFingerIndex(resFingerData.getString(0));
                            empInfo.setSecondFingerVerificationMode(resFingerData.getString(1));
                            empInfo.setSecondFingerTemplate(resFingerData.getString(2));
                            empInfo.setNoOfTemplates(2);
                        }
                    }
                }
            }
        } catch (Exception e) {
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return empInfo;
    }

    public int getAutoIdByEmpId(String empId) {
        SQLiteDatabase db = null;
        Cursor resData = null;
        int AutoId = -1;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select AutoId from " + Constants.EMPLOYEE_TABLE + " where EmployeeId='" + Utility.paddEmpId(empId) + "'", null);
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


    public int getAutoIdByCardId(String cardId) {
        SQLiteDatabase db = null;
        int AutoId = -1;
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select AutoId from " + Constants.EMPLOYEE_TABLE + " where CardId='" + Utility.paddCardId(cardId) + "'", null);
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

    public String getEmpIdByAutoId(int autoId) {
        SQLiteDatabase db = null;
        String empId = "";
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select EmployeeId from " + Constants.EMPLOYEE_TABLE + " where AutoId=" + autoId, null);
            if (resData != null && resData.getCount() > 0) {
                while (resData.moveToNext()) {
                    empId = resData.getString(0);
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
        return empId;
    }

    public int insertOneTemplateToSqliteDb(int autoId, String sensorType) {
        SQLiteDatabase db = null;
        int status = -1;
        Cursor c = null;
        try {
            Calendar ca = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("ddMMyyyyHHmmss");
            String enrolledDateTime = df.format(ca.getTime());
            EmployeeFingerEnrollInfo empFingerInfo = EmployeeFingerEnrollInfo.getInstance();
            if (empFingerInfo != null) {
                if (autoId > 0) {
                    String firstFingerIndex = empFingerInfo.getInstance().getStrFirstFingerIndex();
                    String secondFingerIndex = empFingerInfo.getInstance().getStrSecondFingerIndex();
                    ContentValues firstTemplate = new ContentValues();
                    firstTemplate.put("AutoID", Integer.toString(autoId));
                    firstTemplate.put("SensorType", sensorType);
                    firstTemplate.put("TermplateType", "iso");
                    if (firstFingerIndex != null && firstFingerIndex.trim().length() > 0) {
                        firstTemplate.put("TemplateSrNo", "1");
                        firstTemplate.put("FingerIndex", empFingerInfo.getStrFirstFingerIndex());
                    } else if (secondFingerIndex != null && secondFingerIndex.trim().length() > 0) {
                        firstTemplate.put("TemplateSrNo", "2");
                        firstTemplate.put("FingerIndex", empFingerInfo.getStrSecondFingerIndex());
                    }
                    firstTemplate.put("SecurityLevel", empFingerInfo.getStrSecurityLevel());
                    firstTemplate.put("VerificationMode", empFingerInfo.getStrVerificationMode());
                    firstTemplate.put("Quality", "C");//By default finger quliaty to C for morpho finger sensor
                    firstTemplate.put("EnrolledOn", enrolledDateTime);  //date & time at the time of finger enrollment
                    firstTemplate.put("Template", empFingerInfo.getStrFirstFingerDataHex() + "00000000");  //adjust 4 byte 252 + 4 = 256
                    firstTemplate.put("FingerImage", empFingerInfo.getFirstFingerFID());
                    firstTemplate.put("isAadhaarVerifiedYorN", "N");
                    firstTemplate.put("EnrollSource", "L");
                    firstTemplate.put("JobCode", "0000");
                    db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
                    status = (int) db.insert(Constants.FINGER_TABLE, null, firstTemplate);
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
        return status;
    }

    public int insertRemoteEnrolledTemplate(EmployeeValidationBasicInfo info, EmployeeValidationFingerInfo fInfo) {
        SQLiteDatabase db = null;
        int status = -1;
        Cursor c = null;
        DateFormat originalFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss", Locale.ENGLISH);
        DateFormat targetFormat = new SimpleDateFormat("ddMMyyyyhhmmss");
        String formattedDate = "";
        String fingerType = "";
        try {
            int autoId = info.getEnrollmentNo();
            if (autoId > 0) {
                ContentValues firstTemplate = new ContentValues();
                firstTemplate.put("AutoID", Integer.toString(autoId));
                firstTemplate.put("TermplateType", "iso");
                fingerType = fInfo.getFt();
                if (fingerType != null && fingerType.trim().length() > 0 && fingerType.equals("F1")) {
                    firstTemplate.put("TemplateSrNo", "1");
                } else if (fingerType != null && fingerType.trim().length() > 0 && fingerType.equals("F2")) {
                    firstTemplate.put("TemplateSrNo", "2");
                }
                firstTemplate.put("FingerIndex", fInfo.getFi());
                firstTemplate.put("SecurityLevel", fInfo.getSl());
                firstTemplate.put("VerificationMode", info.getVm());
                firstTemplate.put("Quality", "C");//By default finger quliaty to C for morpho finger sensor
//                String dtoe = templateDownloadInfo.getDtoe();
//                if (dtoe != null && dtoe.trim().length() > 0) {
//                    Date date = originalFormat.parse(dtoe);
//                    formattedDate = targetFormat.format(date);
//                }
                firstTemplate.put("EnrolledOn", formattedDate);  //date & time at the time of finger enrollment
                firstTemplate.put("Template", fInfo.getFmd());  //adjust 4 byte 252 + 4 = 256
                firstTemplate.put("FingerImage", fInfo.getFid());
                firstTemplate.put("isAadhaarVerifiedYorN", "N");
                firstTemplate.put("EnrollSource", "R");
                db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
                status = (int) db.insert(Constants.FINGER_TABLE, null, firstTemplate);
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
        return status;
    }


    public TemplateDownloadInfo insertRemoteEnrolledTemplate(TemplateDownloadInfo templateDownloadInfo) {
        SQLiteDatabase db = null;
        int status = -1;
        Cursor c = null;
        DateFormat originalFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss", Locale.ENGLISH);
        DateFormat targetFormat = new SimpleDateFormat("ddMMyyyyhhmmss");
        String formattedDate = "";
        String fingerType = "";
        try {
            if (templateDownloadInfo != null) {
                templateDownloadInfo.setDbStatus(-1);
                int autoId = templateDownloadInfo.getEnrollmentNo();
                if (autoId > 0) {
                    ContentValues firstTemplate = new ContentValues();
                    firstTemplate.put("AutoID", Integer.toString(autoId));
                    firstTemplate.put("TermplateType", "iso");
                    fingerType = templateDownloadInfo.getFingerType();
                    if (fingerType != null && fingerType.trim().length() > 0 && fingerType.equals("F1")) {
                        firstTemplate.put("TemplateSrNo", "1");
                    } else if (fingerType != null && fingerType.trim().length() > 0 && fingerType.equals("F2")) {
                        firstTemplate.put("TemplateSrNo", "2");
                    }
                    firstTemplate.put("FingerIndex", templateDownloadInfo.getFingerIndex());
                    firstTemplate.put("SecurityLevel", templateDownloadInfo.getSecurityLevel());
                    firstTemplate.put("VerificationMode", templateDownloadInfo.getVerificationMode());
                    firstTemplate.put("Quality", "C");//By default finger quliaty to C for morpho finger sensor
                    String dtoe = templateDownloadInfo.getDtoe();
                    if (dtoe != null && dtoe.trim().length() > 0) {
                        Date date = originalFormat.parse(dtoe);
                        formattedDate = targetFormat.format(date);
                    }
                    firstTemplate.put("EnrolledOn", formattedDate);  //date & time at the time of finger enrollment
                    firstTemplate.put("Template", templateDownloadInfo.getFmd());  //adjust 4 byte 252 + 4 = 256
                    firstTemplate.put("FingerImage", templateDownloadInfo.getFid());
                    firstTemplate.put("isAadhaarVerifiedYorN", "N");
                    firstTemplate.put("EnrollSource", "R");
                    firstTemplate.put("JobCode", templateDownloadInfo.getCommand());
                    db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
                    status = (int) db.insert(Constants.FINGER_TABLE, null, firstTemplate);
                    templateDownloadInfo.setDbStatus(status);
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
        return templateDownloadInfo;
    }

    public TemplateDownloadInfo insertOneTemplateToSqlite(TemplateDownloadInfo templateDownloadInfo) {
        SQLiteDatabase db = null;
        int status = -1;
        Cursor c = null;
        DateFormat originalFormat = new SimpleDateFormat("dd/MMM/yyyy hh:mm:ss", Locale.ENGLISH);
        DateFormat targetFormat = new SimpleDateFormat("ddMMyyyyhhmmss");
        String formattedDate = "";
        String fingerType = "";
        try {
            if (templateDownloadInfo != null) {
                templateDownloadInfo.setDbStatus(-1);
                int autoId = templateDownloadInfo.getEnrollmentNo();
                if (autoId > 0) {
                    ContentValues firstTemplate = new ContentValues();
                    firstTemplate.put("AutoID", Integer.toString(autoId));
                    firstTemplate.put("TermplateType", "iso");
                    fingerType = templateDownloadInfo.getFingerType();
                    if (fingerType != null && fingerType.trim().length() > 0 && fingerType.equals("F1")) {
                        firstTemplate.put("TemplateSrNo", "1");
                    } else if (fingerType != null && fingerType.trim().length() > 0 && fingerType.equals("F2")) {
                        firstTemplate.put("TemplateSrNo", "2");
                    }
                    firstTemplate.put("FingerIndex", templateDownloadInfo.getFingerIndex());
                    firstTemplate.put("SecurityLevel", templateDownloadInfo.getSecurityLevel());
                    firstTemplate.put("VerificationMode", templateDownloadInfo.getVerificationMode());
                    firstTemplate.put("Quality", "C");//By default finger quliaty to C for morpho finger sensor
                    String dtoe = templateDownloadInfo.getDtoe();
                    // Log.d("TEST","Date:"+dtoe);
                    if (dtoe != null && dtoe.trim().length() > 0) {
                        Date date = originalFormat.parse(dtoe);
                        formattedDate = targetFormat.format(date);
                    }
                    firstTemplate.put("EnrolledOn", formattedDate);  //date & time at the time of finger enrollment
                    firstTemplate.put("Template", templateDownloadInfo.getFmd());  //adjust 4 byte 252 + 4 = 256
                    firstTemplate.put("FingerImage", templateDownloadInfo.getFid());
                    firstTemplate.put("isAadhaarVerifiedYorN", "N");
                    firstTemplate.put("EnrollSource", "R");
                    firstTemplate.put("JobCode", templateDownloadInfo.getCommand());
                    db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
                    status = (int) db.insert(Constants.FINGER_TABLE, null, firstTemplate);
                    templateDownloadInfo.setDbStatus(status);
                }
            }
        } catch (Exception e) {
            //Log.d("TEST","Insert Exception:"+e.getMessage());
        } finally {
            if (c != null) {
                c.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return templateDownloadInfo;
    }


    public TemplateDownloadInfo updateOneRemoteEnrolledTemplate(int fingerId, TemplateDownloadInfo templateDownloadInfo) {
        SQLiteDatabase db = null;
        int status = -1;
        Cursor c = null;
        DateFormat originalFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss", Locale.ENGLISH);
        DateFormat targetFormat = new SimpleDateFormat("ddMMyyyyhhmmss");
        String formattedDate = "";
        String fingerType = "";
        try {
            if (templateDownloadInfo != null) {
                templateDownloadInfo.setDbStatus(-1);
                int autoId = templateDownloadInfo.getEnrollmentNo();
                if (autoId > 0) {
                    ContentValues firstTemplate = new ContentValues();
                    firstTemplate.put("AutoID", Integer.toString(autoId));
                    firstTemplate.put("TermplateType", "iso");
                    fingerType = templateDownloadInfo.getFingerType();
                    if (fingerType != null && fingerType.trim().length() > 0 && fingerType.equals("F1")) {
                        firstTemplate.put("TemplateSrNo", "1");
                    } else if (fingerType != null && fingerType.trim().length() > 0 && fingerType.equals("F2")) {
                        firstTemplate.put("TemplateSrNo", "2");
                    }
                    firstTemplate.put("FingerIndex", templateDownloadInfo.getFingerIndex());
                    firstTemplate.put("SecurityLevel", templateDownloadInfo.getSecurityLevel());
                    firstTemplate.put("VerificationMode", templateDownloadInfo.getVerificationMode());
                    firstTemplate.put("Quality", "C");//By default finger quliaty to C for morpho finger sensor
                    String dtoe = templateDownloadInfo.getDtoe();
                    if (dtoe != null && dtoe.trim().length() > 0) {
                        Date date = originalFormat.parse(dtoe);
                        formattedDate = targetFormat.format(date);
                    }
                    firstTemplate.put("EnrolledOn", formattedDate);  //date & time at the time of finger enrollment
                    firstTemplate.put("Template", templateDownloadInfo.getFmd());  //adjust 4 byte 252 + 4 = 256
                    firstTemplate.put("FingerImage", templateDownloadInfo.getFid());
                    firstTemplate.put("isAadhaarVerifiedYorN", "N");
                    firstTemplate.put("EnrollSource", "R");
                    firstTemplate.put("JobCode", templateDownloadInfo.getCommand());
                    db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
                    status = (int) db.update(Constants.FINGER_TABLE, firstTemplate, "ID=" + fingerId, null);
                    templateDownloadInfo.setDbStatus(status);
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
        return templateDownloadInfo;
    }

    public int updateOneRemoteEnrolledTemplate(int fingerId, EmployeeValidationBasicInfo info, EmployeeValidationFingerInfo fInfo) {
        SQLiteDatabase db = null;
        int status = -1;
        Cursor c = null;
        DateFormat originalFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss", Locale.ENGLISH);
        DateFormat targetFormat = new SimpleDateFormat("ddMMyyyyhhmmss");
        String formattedDate = "";
        String fingerType = "";
        try {
            int autoId = info.getEnrollmentNo();
            if (autoId > 0) {
                ContentValues firstTemplate = new ContentValues();
                firstTemplate.put("AutoID", Integer.toString(autoId));
                firstTemplate.put("TermplateType", "iso");
                fingerType = fInfo.getFt();
                if (fingerType != null && fingerType.trim().length() > 0 && fingerType.equals("F1")) {
                    firstTemplate.put("TemplateSrNo", "1");
                } else if (fingerType != null && fingerType.trim().length() > 0 && fingerType.equals("F2")) {
                    firstTemplate.put("TemplateSrNo", "2");
                }
                firstTemplate.put("FingerIndex", fInfo.getFi());
                firstTemplate.put("SecurityLevel", fInfo.getSl());
                firstTemplate.put("VerificationMode", info.getVm());
                firstTemplate.put("Quality", "C");//By default finger quliaty to C for morpho finger sensor
//                String dtoe = templateDownloadInfo.getDtoe();
//                if (dtoe != null && dtoe.trim().length() > 0) {
//                    Date date = originalFormat.parse(dtoe);
//                    formattedDate = targetFormat.format(date);
//                }
                firstTemplate.put("EnrolledOn", formattedDate);  //date & time at the time of finger enrollment
                firstTemplate.put("Template", fInfo.getFmd());  //adjust 4 byte 252 + 4 = 256
                firstTemplate.put("FingerImage", fInfo.getFid());
                firstTemplate.put("isAadhaarVerifiedYorN", "N");
                firstTemplate.put("EnrollSource", "R");
                db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
                status = (int) db.update(Constants.FINGER_TABLE, firstTemplate, "ID=" + fingerId, null);

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
        return status;
    }

    public int updateOneRemoteEnrolledTemplate(int empAutoId, int fingerId, EmployeeFingerEnrollInfo empFingerInfo) {
        SQLiteDatabase db = null;
        int status = -1;
        Cursor c = null;
        String formattedDate = "";
        try {
            if (empAutoId > 0) {
                ContentValues firstTemplate = new ContentValues();
                firstTemplate.put("AutoID", Integer.toString(empAutoId));
                firstTemplate.put("TermplateType", "iso");
                String firstFingerIndex = empFingerInfo.getStrFirstFingerIndex();
                String secondFingerIndex = empFingerInfo.getStrSecondFingerIndex();
                if (firstFingerIndex != null && firstFingerIndex.trim().length() > 0) {
                    firstTemplate.put("TemplateSrNo", "1");
                    firstTemplate.put("FingerIndex", firstFingerIndex);
                    firstTemplate.put("Template", empFingerInfo.getStrFirstFingerDataHex() + "00000000");  //adjust 4 byte 252 + 4 = 256
                    firstTemplate.put("FingerImage", empFingerInfo.getFirstFingerFID());
                } else if (secondFingerIndex != null && secondFingerIndex.trim().length() > 0) {
                    firstTemplate.put("TemplateSrNo", "2");
                    firstTemplate.put("FingerIndex", secondFingerIndex);
                    firstTemplate.put("Template", empFingerInfo.getStrFirstFingerDataHex() + "00000000");  //adjust 4 byte 252 + 4 = 256
                    firstTemplate.put("FingerImage", empFingerInfo.getFirstFingerFID());
                }

                firstTemplate.put("SecurityLevel", empFingerInfo.getStrSecurityLevel());
                firstTemplate.put("VerificationMode", empFingerInfo.getStrVerificationMode());
                firstTemplate.put("Quality", "C");//By default finger quliaty to C for morpho finger sensor

                firstTemplate.put("EnrolledOn", formattedDate);  //date & time at the time of finger enrollment
                firstTemplate.put("isAadhaarVerifiedYorN", "N");
                firstTemplate.put("EnrollSource", "R");
                db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
                status = (int) db.update(Constants.FINGER_TABLE, firstTemplate, "ID=" + fingerId, null);
            }

        } catch (Exception e) {
            Log.d("TEST", "Exception update:" + e.getMessage());
            e.printStackTrace();
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


    public int insertTwoTemplatesToSqliteDb(int autoId, String sensorType) {
        SQLiteDatabase db = null;
        int status = -1;
        try {
            Calendar ca = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("ddMMyyyyHHmmss");
            String enrolledDateTime = df.format(ca.getTime());
            EmployeeFingerEnrollInfo empFingerInfo = EmployeeFingerEnrollInfo.getInstance();
            if (empFingerInfo != null) {
                if (autoId > 0) {
                    db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
                    ContentValues firstTemplate = new ContentValues();
                    firstTemplate.put("AutoID", Integer.toString(autoId));
                    firstTemplate.put("SensorType", sensorType);
                    firstTemplate.put("TermplateType", "iso");
                    firstTemplate.put("TemplateSrNo", "1");
                    firstTemplate.put("FingerIndex", empFingerInfo.getStrFirstFingerIndex());
                    firstTemplate.put("SecurityLevel", empFingerInfo.getStrSecurityLevel());
                    firstTemplate.put("VerificationMode", empFingerInfo.getStrVerificationMode());
                    firstTemplate.put("Quality", "C");//By default finger quliaty to C for morpho finger sensor
                    firstTemplate.put("EnrolledOn", enrolledDateTime);  //date & time at the time of finger enrollment
                    firstTemplate.put("Template", empFingerInfo.getStrFirstFingerDataHex() + "00000000");  //adjust 4 byte 252 + 4 = 256
                    firstTemplate.put("FingerImage", empFingerInfo.getFirstFingerFID());
                    firstTemplate.put("isAadhaarVerifiedYorN", "N");
                    firstTemplate.put("EnrollSource", "L");
                    firstTemplate.put("JobCode", "0000");
                    status = (int) db.insert(Constants.FINGER_TABLE, null, firstTemplate);
                    if (status != -1) {
                        ContentValues secondTemplate = new ContentValues();
                        secondTemplate.put("AutoID", Integer.toString(autoId));
                        secondTemplate.put("SensorType", sensorType);
                        secondTemplate.put("TermplateType", "iso");
                        secondTemplate.put("TemplateSrNo", "2");
                        secondTemplate.put("FingerIndex", empFingerInfo.getStrSecondFingerIndex());
                        secondTemplate.put("SecurityLevel", empFingerInfo.getStrSecurityLevel());
                        secondTemplate.put("VerificationMode", empFingerInfo.getStrVerificationMode());
                        secondTemplate.put("Quality", "C");//By default finger quliaty to C for morpho finger sensor
                        secondTemplate.put("EnrolledOn", enrolledDateTime);  //date & time at the time of finger enrollment
                        secondTemplate.put("Template", empFingerInfo.getStrSecondFingerDataHex() + "00000000");  //adjust 4 byte 252 + 4 = 256
                        secondTemplate.put("FingerImage", empFingerInfo.getSecondFingerFID());
                        secondTemplate.put("isAadhaarVerifiedYorN", "N");
                        secondTemplate.put("EnrollSource", "L");
                        secondTemplate.put("JobCode", "0000");
                        status = (int) db.insert(Constants.FINGER_TABLE, null, secondTemplate);
                    }
                }
            }
        } catch (Exception e) {

        } finally {
            if (db != null) {
                db.close();
            }
        }
        return status;
    }

    public int updateOneTemplateToSqliteDb(int autoId) {
        SQLiteDatabase db = null;
        int status = -1;
        try {
            Calendar ca = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("ddMMyyyyHHmmss");
            String datime = df.format(ca.getTime());
            EmployeeFingerEnrollInfo empFingerInfo = EmployeeFingerEnrollInfo.getInstance();
            if (empFingerInfo != null) {
                db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
                int noOfFingers = empFingerInfo.getNoOfFingers();
                if (noOfFingers == 1) {
                    int fingerIndex = empFingerInfo.getFingerIndex();
                    if (fingerIndex == 1) {//update first finger
                        ContentValues firstTemplate = new ContentValues();
                        firstTemplate.put("FingerIndex", empFingerInfo.getStrNewFirstFingerIndex());
                        firstTemplate.put("EnrolledOn", datime);  //date & time at the time of finger enrollment
                        firstTemplate.put("Template", empFingerInfo.getStrFirstFingerDataHex() + "00000000");  //adjust 4 byte 252 + 4 = 256
                        firstTemplate.put("FingerImage", empFingerInfo.getFirstFingerFID());
                        firstTemplate.put("EnrollSource", "L");
                        firstTemplate.put("IsUpdatedToServer", "0");
                        String condition = "AutoID='" + Integer.toString(autoId) + "' and TemplateSrNo='1'";
                        status = db.update(Constants.FINGER_TABLE, firstTemplate, condition, null);
                    } else if (fingerIndex == 2) {//update second finger
                        ContentValues secondTemplate = new ContentValues();
                        secondTemplate.put("FingerIndex", empFingerInfo.getStrNewSecondFingerIndex());
                        secondTemplate.put("EnrolledOn", datime);  //date & time at the time of finger enrollment
                        secondTemplate.put("Template", empFingerInfo.getStrFirstFingerDataHex() + "00000000");  //adjust 4 byte 252 + 4 = 256
                        secondTemplate.put("FingerImage", empFingerInfo.getFirstFingerFID());
                        secondTemplate.put("EnrollSource", "L");
                        secondTemplate.put("IsUpdatedToServer", "0");
                        String condition = "AutoID='" + Integer.toString(autoId) + "' and TemplateSrNo='2'";
                        status = db.update(Constants.FINGER_TABLE, secondTemplate, condition, null);
                    }
                }
            }
        } catch (Exception e) {

        } finally {
            if (db != null) {
                db.close();
            }
        }

        return status;
    }

    public int updateTwoTemplatesToSqliteDb(int autoId) {
        SQLiteDatabase db = null;
        int status = -1;
        try {
            Calendar ca = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("ddMMyyyyHHmmss");
            String datime = df.format(ca.getTime());
            EmployeeFingerEnrollInfo empFingerInfo = EmployeeFingerEnrollInfo.getInstance();
            if (empFingerInfo != null) {
                db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
                int noOfFingers = empFingerInfo.getNoOfFingers();
                if (noOfFingers == 2) {
                    int fingerIndex = empFingerInfo.getFingerIndex();
                    if (fingerIndex == 3) {//Update both finger indexes
                        ContentValues firstTemplate = new ContentValues();
                        firstTemplate.put("FingerIndex", empFingerInfo.getStrNewFirstFingerIndex());
                        firstTemplate.put("EnrolledOn", datime);  //date & time at the time of finger enrollment
                        firstTemplate.put("Template", empFingerInfo.getStrFirstFingerDataHex() + "00000000");  //adjust 4 byte 252 + 4 = 256
                        firstTemplate.put("FingerImage", empFingerInfo.getFirstFingerFID());
                        firstTemplate.put("EnrollSource", "L");
                        firstTemplate.put("IsUpdatedToServer", "0");
                        String condition = "AutoID='" + Integer.toString(autoId) + "' and TemplateSrNo='1'";
                        status = db.update(Constants.FINGER_TABLE, firstTemplate, condition, null);
                        if (status != -1) {
                            ContentValues secondTemplate = new ContentValues();
                            secondTemplate.put("FingerIndex", empFingerInfo.getStrNewSecondFingerIndex());
                            secondTemplate.put("EnrolledOn", datime);  //date & time at the time of finger enrollment
                            secondTemplate.put("Template", empFingerInfo.getStrSecondFingerDataHex() + "00000000");  //adjust 4 byte 252 + 4 = 256
                            secondTemplate.put("FingerImage", empFingerInfo.getSecondFingerFID());
                            secondTemplate.put("EnrollSource", "L");
                            secondTemplate.put("IsUpdatedToServer", "0");
                            condition = "AutoID='" + Integer.toString(autoId) + "' and TemplateSrNo='2'";
                            status = db.update(Constants.FINGER_TABLE, secondTemplate, condition, null);
                        }
                    }
                }
            }
        } catch (Exception e) {

        } finally {
            if (db != null) {
                db.close();
            }
        }
        return status;
    }


    public int updateFingerDataToEmpTable(int autoId, String enrollStatus, String strIsTemplateAadhaarVerified) {
        SQLiteDatabase db = null;
        int status = -1;
        try {
            EmployeeFingerEnrollInfo empFingerInfo = EmployeeFingerEnrollInfo.getInstance();
            if (empFingerInfo != null) {
                ContentValues fingerDetails = new ContentValues();
                fingerDetails.put("EnrollStatus", enrollStatus);
                fingerDetails.put("NosFinger", Integer.toString(empFingerInfo.getNoOfFingers()));
                fingerDetails.put("VerificationMode", empFingerInfo.getStrVerificationMode());
                fingerDetails.put("isTemplateAadhaarVerifiedYorN", strIsTemplateAadhaarVerified);
                db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
                status = db.update(Constants.EMPLOYEE_TABLE, fingerDetails, "AutoId=" + autoId, null);
            }
        } catch (Exception e) {

        }
        return status;
    }


    public int updateFingerDataToEmpTable(int autoId, int nof, String enrollStatus, String strIsTemplateAadhaarVerified) {
        SQLiteDatabase db = null;
        int status = -1;
        try {
            EmployeeFingerEnrollInfo empFingerInfo = EmployeeFingerEnrollInfo.getInstance();
            if (empFingerInfo != null) {
                ContentValues fingerDetails = new ContentValues();
                fingerDetails.put("EnrollStatus", enrollStatus);
                fingerDetails.put("NosFinger", Integer.toString(nof));
                fingerDetails.put("VerificationMode", empFingerInfo.getStrVerificationMode());
                fingerDetails.put("isTemplateAadhaarVerifiedYorN", strIsTemplateAadhaarVerified);
                db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
                status = db.update(Constants.EMPLOYEE_TABLE, fingerDetails, "AutoId=" + autoId, null);
            }
        } catch (Exception e) {

        }
        return status;
    }

    public int updateFingerDataToEmpTableByEmpId(int autoId) {
        SQLiteDatabase db = null;
        int status = -1;
        try {
            EmployeeFingerEnrollInfo empFingerInfo = EmployeeFingerEnrollInfo.getInstance();
            if (empFingerInfo != null) {
                ContentValues fingerDetails = new ContentValues();
                fingerDetails.put("EnrollStatus", "N");
                fingerDetails.put("NosFinger", "");
                fingerDetails.put("VerificationMode", "");
                fingerDetails.put("isTemplateAadhaarVerifiedYorN", "N");
                db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
                status = db.update(Constants.EMPLOYEE_TABLE, fingerDetails, "AutoId=" + autoId, null);
            }
        } catch (Exception e) {

        }
        return status;
    }


    public int insertCardVerificationPin(int autoId, String pin) {
        SQLiteDatabase db = null;
        int status = -1;
        try {
            ContentValues initialValues = new ContentValues();
            initialValues.put("AutoID", Integer.toString(autoId));
            initialValues.put("CardPin", pin);
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            status = (int) db.insert(Constants.CARD_VER_PIN_TABLE, null, initialValues);
        } catch (Exception e) {
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return status;
    }

    public int deleteFingerRecordByAutoId(int autoId) {
        SQLiteDatabase db = null;
        int status = -1;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            status = db.delete(Constants.FINGER_TABLE, "AutoID='" + Integer.toString(autoId) + "'", null);
        } catch (SQLiteException e) {

        } finally {
            if (db != null) {
                db.close();
            }
        }
        return status;
    }

    public int deleteFingerRecords() {
        SQLiteDatabase db = null;
        int status = -1;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            status = db.delete(Constants.FINGER_TABLE, "", null);
        } catch (SQLiteException e) {

        } finally {
            if (db != null) {
                db.close();
            }
        }
        return status;
    }

    public int deleteEmployeeRecords() {
        SQLiteDatabase db = null;
        int status = -1;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            status = db.delete(Constants.EMPLOYEE_TABLE, "", null);
        } catch (SQLiteException e) {

        } finally {
            if (db != null) {
                db.close();
            }
        }
        return status;
    }

    public int getCardIssuedStatus(String strCSN) {
        SQLiteDatabase db = null;
        int autoId = -1;
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select AutoId from " + Constants.EMPLOYEE_TABLE + " where SmartCardSerialNo='" + strCSN + "'", null);
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

    public String getEmployeeCSN(int intAutoId) {
        SQLiteDatabase db = null;
        String strEmpCSN = "";
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select SmartCardSerialNo from " + Constants.EMPLOYEE_TABLE + " where AutoId=" + intAutoId + "", null);
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

    public int getSmartCardIssuedVer(int autoEmpId) {
        SQLiteDatabase db = null;
        int smartCardVer = -1;
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select SmartCardVersion from " + Constants.EMPLOYEE_TABLE + " where AutoId=" + autoEmpId, null);
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

    public boolean isCardReadCreatedLocal(String strCRCSN) {
        SQLiteDatabase db = null;
        boolean isCardCreatedLocal = false;
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select SmartCardSerialNo from " + Constants.EMPLOYEE_TABLE + " where SmartCardSerialNo='" + strCRCSN + "'", null);
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

    public boolean checkIsCardCreatedLocal(String strCSN, String strCardId) {
        SQLiteDatabase db = null;
        boolean isCardCreatedLocal = false;
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select AutoId from " + Constants.EMPLOYEE_TABLE + " where SmartCardSerialNo='" + strCSN + "' and CardId='" + Utility.paddCardId(strCardId) + "'", null);
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
        SQLiteDatabase db = null;
        int autoId = -1;
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select AutoId from " + Constants.EMPLOYEE_TABLE + " where SmartCardSerialNo='" + strCSN + "' and CardId='" + Utility.paddCardId(strReadCardId) + "'", null);
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

    public int isNewCardIdExists(String cardId) {
        SQLiteDatabase db = null;
        int autoId = -1;
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select AutoId from " + Constants.EMPLOYEE_TABLE + " where CardId='" + Utility.paddCardId(cardId) + "'", null);
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

    public Cursor getSectorAndKeyForWriteCard() {
        SQLiteDatabase db = null;
        Cursor resSectorKeyData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resSectorKeyData = db.rawQuery("select SectorNo,KeyA,KeyB from " + Constants.SECTOR_KEY_TABLE, null);

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

    public Cursor getSectorAndKeyForRC632CardInit() {
        SQLiteDatabase db = null;
        Cursor resSectorKeyData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resSectorKeyData = db.rawQuery("select SectorNo,KeyA1,AccessCode,KeyB1 from " + Constants.SECTOR_KEY_CARD_INIT, null);
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
        SQLiteDatabase db = null;
        Cursor resSectorKeyData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resSectorKeyData = db.rawQuery("select SectorNo,KeyA,AccessCode,KeyB from " + Constants.SECTOR_KEY_CARD_INIT, null);
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
        SQLiteDatabase db = null;
        Cursor resSectorKeyData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);

            resSectorKeyData = db.rawQuery("select SectorNo,KeyA1,KeyB1 from " + Constants.SECTOR_KEY_TABLE, null);
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
        SQLiteDatabase db = null;
        Cursor resSectorKeyData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);

            //Cursor resSectorKeyData=db.rawQuery("select SectorNo,KeyA from "+SECTOR_KEY,null);
            //Cursor resSectorKeyData=db.rawQuery("select SectorNo,KeyA from SmartKeyNew",null);
            //Cursor resSectorKeyData=db.rawQuery("select SectorNo,KeyA from SmartKeyFortuna",null);

            resSectorKeyData = db.rawQuery("select SectorNo,KeyB1 from " + Constants.SECTOR_KEY_TABLE, null);
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


    public int insertIntoSmartCardOperationLog(int loginId, int empAutoId, String strCardSerialNo, String cardId, String cardVer, String isCardCreatedLocally, String strOperation, String strStatus, String strDateTime) {
        SQLiteDatabase db = null;
        int insertStatus = -1;
        try {
            ContentValues initialValues = new ContentValues();
            initialValues.put("LoginID", loginId);
            initialValues.put("EmpAutoId", empAutoId);
            initialValues.put("CSN", strCardSerialNo);
            initialValues.put("CardOperation", strOperation);
            initialValues.put("OriginalCardId", Utility.paddCardId(cardId));
            initialValues.put("OriginalCardIDVer", cardVer);
            initialValues.put("Status", strStatus);
            initialValues.put("isCardCreatedLocally", isCardCreatedLocally);
            initialValues.put("DateTime", strDateTime);

            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            insertStatus = (int) db.insert(Constants.SMART_CARD_OPERATION_TABLE, null, initialValues);

        } catch (SQLiteException e) {

        } finally {
            if (db != null) {
                db.close();
            }
        }
        return insertStatus;
    }

    public int updateSmartCardVer(int intAutoId, String strCSN, String strSmartCardVersion) {
        SQLiteDatabase db = null;
        ContentValues initialValues = null;
        String strCondition = "";
        int updateStatus = -1;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            initialValues = new ContentValues();
            initialValues.put("SmartCardSerialNo", strCSN);
            initialValues.put("SmartCardVersion", strSmartCardVersion);
            strCondition = "AutoId" + "=" + intAutoId + "";
            updateStatus = db.update(Constants.EMPLOYEE_TABLE, initialValues, strCondition, null);
        } catch (Exception e) {
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return updateStatus;
    }


    public int insertIntoSmartCardOperationLog(int loginId, int empAutoId, String strCardSerialNo, String strOldCardId, int oldCardVersion, String strNewCardId, int newCardVersion, String strOperation, String strStatus, String isCardCreatedLocally, String strDateTime) {
        SQLiteDatabase db = null;
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

            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            insertStatus = (int) db.insert(Constants.SMART_CARD_OPERATION_TABLE, null, initialValues);

        } catch (SQLiteException e) {

        } finally {
            if (db != null) {
                db.close();
            }
        }
        return insertStatus;
    }


    public int getAutoId(String readCSN) {
        SQLiteDatabase db = null;
        int autoId = -1;
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select AutoId from " + Constants.EMPLOYEE_TABLE + " where SmartCardSerialNo='" + readCSN.trim() + "'", null);
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

    public int insertIntoHotListLog(int loginId, int empAutoId, String strCardSerialNo, String strOldCardId, int strOldCardVer, String strOperation, String strStatus, String isCardCreatedLocally, String strReason, String strDateTime) {
        SQLiteDatabase db = null;
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

            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            insertStatus = (int) db.insert(Constants.HOTLIST_TABLE, null, initialValues);

        } catch (SQLiteException e) {

        } finally {
            if (db != null) {
                db.close();
            }
        }
        return insertStatus;
    }

    public String getSmartCardSrlNo(String enrollmentNo) {
        SQLiteDatabase db = null;
        String csn = "";
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select SmartCardSerialNo from " + Constants.EMPLOYEE_TABLE + " where AutoId='" + enrollmentNo + "'", null);
            if (resData != null && resData.getCount() > 0) {
                while (resData.moveToNext()) {
                    csn = resData.getString(0);
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
        return csn;
    }

    public AttendanceInfo getAttData(AttendanceInfo attInfo) {
        SQLiteDatabase db = null;
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            resData = db.rawQuery("select ID,EmployeeID,PunchDate,PunchTime,InOutMode,Lat,Long from " + Constants.ATTENDANCE_TABLE + " where Uploaded='00' limit 1", null); // limit 1
            if (resData != null && resData.getCount() > 0) {
                attInfo = new AttendanceInfo();
                while (resData.moveToNext()) {
                    int attendanceId = resData.getInt(0);
                    String empId = resData.getString(1);
                    String strPunchDate = resData.getString(2);
                    String strPunchTime = resData.getString(3);
                    String mode = resData.getString(4);
                    String strLat = resData.getString(5);
                    String strLong = resData.getString(6);
                    String com = strLat + "," + strLong;

                    strPunchDate = strPunchDate.replaceAll("-", "").trim();
                    String date = strPunchDate.substring(0, 2);
                    String month = strPunchDate.substring(2, 4);
                    String year = strPunchDate.substring(4);

                    String hr = strPunchTime.substring(0, 2);
                    String min = strPunchTime.substring(2, 4);
                    String sec = strPunchTime.substring(4, 6);
                    String tempPunchTime = hr + ":" + min + ":" + sec;

                    strPunchDate = date + "/" + month + "/" + year;
                    strPunchTime = tempPunchTime; //finalDateFormat.format(initialDateFormat.parse(strPunchTime));

                    attInfo.setEmpId(empId);
                    attInfo.setLatLong(com);
                    attInfo.setPunchDate(strPunchDate);
                    attInfo.setPunchTime(strPunchTime);
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
        return attInfo;
    }


    public AttendanceInfo getAttendanceData(AttendanceInfo attInfo) {
        SQLiteDatabase db = null;
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            resData = db.rawQuery("select ID,EmployeeID,PunchDate,PunchTime,InOutMode,Lat,Long from " + Constants.ATTENDANCE_TABLE + " where Uploaded='00' limit 1", null); // limit 1
            if (resData != null && resData.getCount() > 0) {
                attInfo = new AttendanceInfo();
                while (resData.moveToNext()) {
                    int attendanceId = resData.getInt(0);
                    String empId = resData.getString(1);
                    String strPunchDate = resData.getString(2);
                    String strPunchTime = resData.getString(3);
                    String mode = resData.getString(4);
                    String strLat = resData.getString(5);
                    String strLong = resData.getString(6);
                    String com = strLat + "," + strLong;

                    strPunchDate = strPunchDate.replaceAll("-", "").trim();
                    String date = strPunchDate.substring(0, 2);
                    String month = strPunchDate.substring(2, 4);
                    String year = strPunchDate.substring(4);

                    String hr = strPunchTime.substring(0, 2);
                    String min = strPunchTime.substring(2, 4);
                    String sec = strPunchTime.substring(4, 6);
                    String tempPunchTime = hr + ":" + min + ":" + sec;

                    strPunchDate = date + "/" + month + "/" + year;
                    strPunchTime = tempPunchTime; //finalDateFormat.format(initialDateFormat.parse(strPunchTime));

                    int imgLen = 0;
                    String strBase64Photo = "";
                    if (empId != null && empId.length() > 0) {
                        db.close();
                        db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
                        resData = db.rawQuery("select Photo from " + Constants.EMPLOYEE_TABLE + " where EmployeeID='" + Utility.paddEmpId(empId) + "'", null);
                        while (resData.moveToNext()) {
                            byte[] photo = resData.getBlob(0);
                            if (photo != null) {  /*&& strEmail != null && strEmail.trim().length() > 0*/
                                BASE64Encoder encoder = new BASE64Encoder();
                                String strPhoto = photo.toString();
                                strBase64Photo = encoder.encode(strPhoto.getBytes());
                                imgLen = strBase64Photo.length();
                            }
                        }
                    }
                    if (attendanceId > 0) {
                        ContentValues initialValues = new ContentValues();
                        initialValues.put("IsDataFetched", "Y");
                        String strCondition = "ID" + "=" + attendanceId;
                        int status = db.update(Constants.ATTENDANCE_TABLE, initialValues, strCondition, null);
                        if (status != -1) {
                            attInfo.setEmpId(empId);
                            attInfo.setImageLen(imgLen);
                            attInfo.setImageBase64(strBase64Photo);
                            attInfo.setLatLong(com);
                            attInfo.setPunchDate(strPunchDate);
                            attInfo.setPunchTime(strPunchTime);
                            attInfo.setId(attendanceId);
                        }
                    }
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
        return attInfo;
    }

    public int updateAttendanceTable(int id) {
        SQLiteDatabase db = null;
        ContentValues initialValues = null;
        String strCondition = "";
        int updateStatus = -1;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            initialValues = new ContentValues();
            initialValues.put("Uploaded", "01");
            strCondition = "IsDataFetched" + "='Y' and ID=" + id;
            updateStatus = db.update(Constants.ATTENDANCE_TABLE, initialValues, strCondition, null);
        } catch (Exception e) {
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return updateStatus;
    }


    public EmpValidationDownloadInfo getEmployeeData(EmpValidationDownloadInfo empInfo) {
        SQLiteDatabase db = null;
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select EmployeeId,CardId,Name,BloodGroup,SiteCode,MobileNo,MailId,PIN,ValidUpto,BirthDay,isBlackListed,isLockOpenWhenAllowed from " + Constants.EMPLOYEE_TABLE + " where isUpdatedToServer=0 limit 1", null);
            if (resData != null && resData.getCount() > 0) {
                empInfo = new EmpValidationDownloadInfo();
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

                    if (empId != null && empId.length() > 0) {
                        empInfo.setEmpId(empId);
                    } else {
                        empInfo.setEmpId("");
                    }

                    if (cardId != null && cardId.trim().length() > 0) {
                        empInfo.setCardId(cardId);
                    } else {
                        empInfo.setCardId("");
                    }

                    if (empName != null && empName.trim().length() > 0) {
                        empInfo.setEmpName(empName);
                    } else {
                        empInfo.setEmpName("");
                    }

                    if (bloodGrp != null && bloodGrp.trim().length() > 0) {
                        int blood = Utility.getBloodGrValByName(bloodGrp);
                        if (blood != -1) {
                            empInfo.setBloodGrp(Integer.toString(blood));
                        } else {
                            empInfo.setBloodGrp("");
                        }
                    } else {
                        empInfo.setBloodGrp("");
                    }

                    if (siteCode != null && siteCode.trim().length() > 0) {
                        empInfo.setSiteCode(siteCode);
                    } else {
                        empInfo.setSiteCode("");
                    }

                    if (mobileNo != null && mobileNo.trim().length() > 0) {
                        empInfo.setMobileNo(mobileNo);
                    } else {
                        empInfo.setMobileNo("");
                    }

                    if (maildId != null && maildId.trim().length() > 0) {
                        empInfo.setEmailId(maildId);
                    } else {
                        empInfo.setEmailId("");
                    }

                    if (pin != null && pin.trim().length() > 0) {
                        empInfo.setPin(pin);
                    } else {
                        empInfo.setPin("");
                    }

                    if (dob != null && dob.trim().length() > 0) {
                        empInfo.setDob(dob);
                    } else {
                        empInfo.setDob("");
                    }

                    if (dov != null && dov.trim().length() > 0) {
                        empInfo.setDov(dov);
                    } else {
                        empInfo.setDov("");
                    }

                    if (isBlackListed != null && isBlackListed.trim().length() > 0) {
                        empInfo.setIsBlackListed(isBlackListed);
                    } else {
                        empInfo.setIsBlackListed("");
                    }

                    if (isLockOpen != null && isLockOpen.trim().length() > 0) {
                        empInfo.setIsLockOpen(isLockOpen);
                    } else {
                        empInfo.setIsLockOpen("");
                    }
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
        return empInfo;
    }

    public ArrayList <TemplateUploadInfo> getTemplateForAutoUpload(ArrayList <TemplateUploadInfo> templateUploadInfoList) {
        SQLiteDatabase db = null;
        Cursor resEmployeeData = null;
        Cursor resFingerData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            resEmployeeData = db.rawQuery("select AutoId,EmployeeId,CardId,Name from " + Constants.EMPLOYEE_TABLE, null);
            if (resEmployeeData != null && resEmployeeData.getCount() > 0) {
                templateUploadInfoList = new ArrayList <TemplateUploadInfo>();
                while (resEmployeeData.moveToNext()) {
                    int autoId = resEmployeeData.getInt(0);
                    String empId = resEmployeeData.getString(1);
                    String cardId = resEmployeeData.getString(2);
                    String empName = resEmployeeData.getString(3);
                    if (autoId != -1) {
                        db.close();
                        db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
                        resFingerData = db.rawQuery("select ID,TemplateSrNo,FingerIndex,SecurityLevel,VerificationMode,Quality,EnrolledOn,Template,FingerImage from " + Constants.FINGER_TABLE + " where AutoId='" + Integer.toString(autoId) + "' and EnrollSource='L' and isUpdatedToServer='0' limit 1", null);
                        if (resFingerData != null && resFingerData.getCount() > 0) {
                            while (resFingerData.moveToNext()) {
                                TemplateUploadInfo templateInfo = new TemplateUploadInfo();
                                String fingerId = resFingerData.getString(0).trim();
                                String templateSrNo = resFingerData.getString(1).trim();
                                String fingerIndex = resFingerData.getString(2).trim();
                                String securityLevel = resFingerData.getString(3).trim();
                                String verificationMode = resFingerData.getString(4).trim();
                                String fingerQuality = resFingerData.getString(5).trim();
                                String enrolledOn = resFingerData.getString(6).trim();
                                String fmd = resFingerData.getString(7).trim();
                                byte[] fid = resFingerData.getBlob(8);

                                String date = enrolledOn.substring(0, 2);
                                String month = enrolledOn.substring(2, 4);
                                String year = enrolledOn.substring(4, 8);
                                String hr = enrolledOn.substring(8, 10);
                                String min = enrolledOn.substring(10, 12);
                                String sec = enrolledOn.substring(12);

                                enrolledOn = date + "/" + month + "/" + year;
                                String Dateformat = Utility.DateFormatChange(enrolledOn);
                                String Timeformat = hr + ":" + min + ":" + sec;

                                enrolledOn = Dateformat + " " + Timeformat;

                                if (empId != null && empId.trim().length() > 0) {
                                    templateInfo.setEmpId(empId);
                                } else {
                                    templateInfo.setEmpId("");
                                }

                                if (cardId != null && cardId.trim().length() > 0) {
                                    templateInfo.setCardId(cardId);
                                } else {
                                    templateInfo.setCardId("");
                                }

                                if (empName != null && empName.trim().length() > 0) {
                                    templateInfo.setEmpName(empName);
                                } else {
                                    templateInfo.setEmpName("");
                                }

                                if (fingerId != null && fingerId.trim().length() > 0) {
                                    templateInfo.setFingerId(fingerId);
                                } else {
                                    templateInfo.setFingerId("");
                                }

                                if (templateSrNo != null && templateSrNo.trim().length() > 0) {
                                    templateInfo.setTemplateSrNo(templateSrNo);
                                } else {
                                    templateInfo.setTemplateSrNo("");
                                }

                                if (fingerIndex != null && fingerIndex.trim().length() > 0) {
                                    templateInfo.setFingerIndex(fingerIndex);
                                } else {
                                    templateInfo.setFingerIndex("");
                                }

                                if (securityLevel != null && securityLevel.trim().length() > 0) {
                                    templateInfo.setSecurityLevel(securityLevel);
                                } else {
                                    templateInfo.setSecurityLevel("");
                                }

                                if (verificationMode != null && verificationMode.trim().length() > 0) {
                                    templateInfo.setVerificationMode(verificationMode);
                                } else {
                                    templateInfo.setVerificationMode("");
                                }

                                if (fingerQuality != null && fingerQuality.trim().length() > 0) {
                                    templateInfo.setFingerQuality(fingerQuality);
                                } else {
                                    templateInfo.setFingerQuality("");
                                }

                                if (fmd != null && fmd.trim().length() > 0) {
                                    templateInfo.setFmd(fmd);
                                } else {
                                    templateInfo.setFmd("");
                                }

                                if (fid != null && fid.length > 0) {
                                    templateInfo.setFid(fid);
                                } else {
                                    templateInfo.setFid(null);
                                }

                                if (enrolledOn != null && enrolledOn.trim().length() > 0) {
                                    templateInfo.setDtoe(enrolledOn);
                                } else {
                                    templateInfo.setDtoe("");
                                }

                                templateUploadInfoList.add(templateInfo);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {

        } finally {
            if (resEmployeeData != null) {
                resEmployeeData.close();
            }
            if (resFingerData != null) {
                resFingerData.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return templateUploadInfoList;
    }


    public ArrayList <TemplateUploadInfo> getAllEnrolledTemplates(ArrayList <TemplateUploadInfo> templateUploadInfoList) {
        SQLiteDatabase db = null;
        Cursor resEmployeeData = null;
        Cursor resFingerData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            resEmployeeData = db.rawQuery("select AutoId,EmployeeId,CardId,Name from " + Constants.EMPLOYEE_TABLE, null);
            if (resEmployeeData != null && resEmployeeData.getCount() > 0) {
                templateUploadInfoList = new ArrayList <TemplateUploadInfo>();
                while (resEmployeeData.moveToNext()) {
                    int autoId = resEmployeeData.getInt(0);
                    String empId = resEmployeeData.getString(1);
                    String cardId = resEmployeeData.getString(2);
                    String empName = resEmployeeData.getString(3);
                    if (autoId != -1) {
                        db.close();
                        db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
                        resFingerData = db.rawQuery("select ID,TemplateSrNo,FingerIndex,SecurityLevel,VerificationMode,Quality,EnrolledOn,Template,FingerImage from " + Constants.FINGER_TABLE + " where AutoId='" + Integer.toString(autoId) + "'", null);
                        if (resFingerData != null && resFingerData.getCount() > 0) {
                            while (resFingerData.moveToNext()) {
                                TemplateUploadInfo templateInfo = new TemplateUploadInfo();
                                String fingerId = resFingerData.getString(0).trim();
                                String templateSrNo = resFingerData.getString(1).trim();
                                String fingerIndex = resFingerData.getString(2).trim();
                                String securityLevel = resFingerData.getString(3).trim();
                                String verificationMode = resFingerData.getString(4).trim();
                                String fingerQuality = resFingerData.getString(5).trim();
                                String enrolledOn = resFingerData.getString(6).trim();
                                String fmd = resFingerData.getString(7).trim();
                                byte[] fid = resFingerData.getBlob(8);

                                String date = enrolledOn.substring(0, 2);
                                String month = enrolledOn.substring(2, 4);
                                String year = enrolledOn.substring(4, 8);
                                String hr = enrolledOn.substring(8, 10);
                                String min = enrolledOn.substring(10, 12);
                                String sec = enrolledOn.substring(12);

                                enrolledOn = date + "/" + month + "/" + year;
                                String Dateformat = Utility.DateFormatChange(enrolledOn);
                                String Timeformat = hr + ":" + min + ":" + sec;

                                enrolledOn = Dateformat + " " + Timeformat;

                                if (empId != null && empId.trim().length() > 0) {
                                    templateInfo.setEmpId(empId);
                                } else {
                                    templateInfo.setEmpId("");
                                }

                                if (cardId != null && cardId.trim().length() > 0) {
                                    templateInfo.setCardId(cardId);
                                } else {
                                    templateInfo.setCardId("");
                                }

                                if (empName != null && empName.trim().length() > 0) {
                                    templateInfo.setEmpName(empName);
                                } else {
                                    templateInfo.setEmpName("");
                                }

                                if (fingerId != null && fingerId.trim().length() > 0) {
                                    templateInfo.setFingerId(fingerId);
                                } else {
                                    templateInfo.setFingerId("");
                                }

                                if (templateSrNo != null && templateSrNo.trim().length() > 0) {
                                    templateInfo.setTemplateSrNo(templateSrNo);
                                } else {
                                    templateInfo.setTemplateSrNo("");
                                }

                                if (fingerIndex != null && fingerIndex.trim().length() > 0) {
                                    templateInfo.setFingerIndex(fingerIndex);
                                } else {
                                    templateInfo.setFingerIndex("");
                                }

                                if (securityLevel != null && securityLevel.trim().length() > 0) {
                                    templateInfo.setSecurityLevel(securityLevel);
                                } else {
                                    templateInfo.setSecurityLevel("");
                                }

                                if (verificationMode != null && verificationMode.trim().length() > 0) {
                                    templateInfo.setVerificationMode(verificationMode);
                                } else {
                                    templateInfo.setVerificationMode("");
                                }

                                if (fingerQuality != null && fingerQuality.trim().length() > 0) {
                                    templateInfo.setFingerQuality(fingerQuality);
                                } else {
                                    templateInfo.setFingerQuality("");
                                }

                                if (fmd != null && fmd.trim().length() > 0) {
                                    templateInfo.setFmd(fmd);
                                } else {
                                    templateInfo.setFmd("");
                                }

                                if (fid != null && fid.length > 0) {
                                    templateInfo.setFid(fid);
                                } else {
                                    templateInfo.setFid(null);
                                }

                                if (enrolledOn != null && enrolledOn.trim().length() > 0) {
                                    templateInfo.setDtoe(enrolledOn);
                                } else {
                                    templateInfo.setDtoe("");
                                }

                                templateUploadInfoList.add(templateInfo);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {

        } finally {
            if (resEmployeeData != null) {
                resEmployeeData.close();
            }
            if (resFingerData != null) {
                resFingerData.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return templateUploadInfoList;
    }

    public ArrayList <TemplateUploadInfo> getNewEnrolledTemplates(ArrayList <TemplateUploadInfo> templateUploadInfoList) {
        SQLiteDatabase db = null;
        Cursor resEmployeeData = null;
        Cursor resFingerData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            resFingerData = db.rawQuery("select ID,AutoId,TemplateSrNo,FingerIndex,SecurityLevel,VerificationMode,Quality,EnrolledOn,Template,FingerImage from " + Constants.FINGER_TABLE + " where IsUpdatedToServer='0'", null);
            if (resFingerData != null && resFingerData.getCount() > 0) {
                templateUploadInfoList = new ArrayList <TemplateUploadInfo>();
                while (resFingerData.moveToNext()) {
                    TemplateUploadInfo templateInfo = new TemplateUploadInfo();
                    String fingerId = resFingerData.getString(0).trim();
                    String autoEmpId = resFingerData.getString(1).trim();

                    String templateSrNo = resFingerData.getString(2).trim();
                    String fingerIndex = resFingerData.getString(3).trim();
                    String securityLevel = resFingerData.getString(4).trim();
                    String verificationMode = resFingerData.getString(5).trim();
                    String fingerQuality = resFingerData.getString(6).trim();
                    String enrolledOn = resFingerData.getString(7).trim();
                    String fmd = resFingerData.getString(8).trim();
                    byte[] fid = resFingerData.getBlob(9);

                    String date = enrolledOn.substring(0, 2);
                    String month = enrolledOn.substring(2, 4);
                    String year = enrolledOn.substring(4, 8);
                    String hr = enrolledOn.substring(8, 10);
                    String min = enrolledOn.substring(10, 12);
                    String sec = enrolledOn.substring(12);

                    enrolledOn = date + "/" + month + "/" + year;
                    String Dateformat = Utility.DateFormatChange(enrolledOn);
                    String Timeformat = hr + ":" + min + ":" + sec;

                    enrolledOn = Dateformat + " " + Timeformat;

                    if (fingerId != null && fingerId.trim().length() > 0) {
                        templateInfo.setFingerId(fingerId);
                    } else {
                        templateInfo.setFingerId("");
                    }

                    if (templateSrNo != null && templateSrNo.trim().length() > 0) {
                        templateInfo.setTemplateSrNo(templateSrNo);
                    } else {
                        templateInfo.setTemplateSrNo("");
                    }

                    if (fingerIndex != null && fingerIndex.trim().length() > 0) {
                        templateInfo.setFingerIndex(fingerIndex);
                    } else {
                        templateInfo.setFingerIndex("");
                    }

                    if (securityLevel != null && securityLevel.trim().length() > 0) {
                        templateInfo.setSecurityLevel(securityLevel);
                    } else {
                        templateInfo.setSecurityLevel("");
                    }

                    if (verificationMode != null && verificationMode.trim().length() > 0) {
                        templateInfo.setVerificationMode(verificationMode);
                    } else {
                        templateInfo.setVerificationMode("");
                    }

                    if (fingerQuality != null && fingerQuality.trim().length() > 0) {
                        templateInfo.setFingerQuality(fingerQuality);
                    } else {
                        templateInfo.setFingerQuality("");
                    }

                    if (fmd != null && fmd.trim().length() > 0) {
                        templateInfo.setFmd(fmd);
                    } else {
                        templateInfo.setFmd("");
                    }

                    if (fid != null && fid.length > 0) {
                        templateInfo.setFid(fid);
                    } else {
                        templateInfo.setFid(null);
                    }

                    if (enrolledOn != null && enrolledOn.trim().length() > 0) {
                        templateInfo.setDtoe(enrolledOn);
                    } else {
                        templateInfo.setDtoe("");
                    }
                    db.close();
                    db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
                    resEmployeeData = db.rawQuery("select EmployeeID,CardId,Name from " + Constants.EMPLOYEE_TABLE + " where AutoId='" + autoEmpId + "'", null);
                    if (resEmployeeData != null && resEmployeeData.getCount() > 0) {
                        while (resEmployeeData.moveToNext()) {
                            String empId = resEmployeeData.getString(0).trim();
                            String cardId = resEmployeeData.getString(1);
                            String empName = resEmployeeData.getString(2);

                            if (empId != null && empId.trim().length() > 0) {
                                templateInfo.setEmpId(empId);
                            } else {
                                templateInfo.setEmpId("");
                            }

                            if (cardId != null && cardId.trim().length() > 0) {
                                templateInfo.setCardId(cardId);
                            } else {
                                templateInfo.setCardId("");
                            }

                            if (empName != null && empName.trim().length() > 0) {
                                templateInfo.setEmpName(empName);
                            } else {
                                templateInfo.setEmpName("");
                            }
                        }
                    } else {
                        templateInfo.setEmpId("");
                        templateInfo.setCardId("");
                        templateInfo.setEmpName("");
                    }
                    templateUploadInfoList.add(templateInfo);
                }
            }
        } catch (Exception e) {

        } finally {
            if (resEmployeeData != null) {
                resEmployeeData.close();
            }
            if (resFingerData != null) {
                resFingerData.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return templateUploadInfoList;
    }

    public ArrayList <TemplateUploadInfo> geTemplateEmployeeWise(String empId, String cardId, String empName, ArrayList <TemplateUploadInfo> templateUploadInfoList) {
        SQLiteDatabase db = null;
        Cursor resEmployeeData = null;
        Cursor resFingerData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            resEmployeeData = db.rawQuery("select AutoId from " + Constants.EMPLOYEE_TABLE + " where EmployeeId='" + Utility.paddEmpId(empId) + "'", null);
            if (resEmployeeData != null && resEmployeeData.getCount() > 0) {
                while (resEmployeeData.moveToNext()) {
                    int autoId = resEmployeeData.getInt(0);
                    if (autoId != -1) {
                        db.close();
                        db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
                        resFingerData = db.rawQuery("select ID,TemplateSrNo,FingerIndex,SecurityLevel,VerificationMode,Quality,EnrolledOn,Template,FingerImage from " + Constants.FINGER_TABLE + " where AutoId='" + Integer.toString(autoId) + "'", null);
                        if (resFingerData != null && resFingerData.getCount() > 0) {
                            templateUploadInfoList = new ArrayList <TemplateUploadInfo>();
                            while (resFingerData.moveToNext()) {
                                TemplateUploadInfo templateInfo = new TemplateUploadInfo();
                                String fingerId = resFingerData.getString(0).trim();
                                String templateSrNo = resFingerData.getString(1).trim();
                                String fingerIndex = resFingerData.getString(2).trim();
                                String securityLevel = resFingerData.getString(3).trim();
                                String verificationMode = resFingerData.getString(4).trim();
                                String fingerQuality = resFingerData.getString(5).trim();
                                String enrolledOn = resFingerData.getString(6).trim();
                                String fmd = resFingerData.getString(7).trim();
                                byte[] fid = resFingerData.getBlob(8);

                                String date = enrolledOn.substring(0, 2);
                                String month = enrolledOn.substring(2, 4);
                                String year = enrolledOn.substring(4, 8);
                                String hr = enrolledOn.substring(8, 10);
                                String min = enrolledOn.substring(10, 12);
                                String sec = enrolledOn.substring(12);

                                enrolledOn = date + "/" + month + "/" + year;
                                String Dateformat = Utility.DateFormatChange(enrolledOn);
                                String Timeformat = hr + ":" + min + ":" + sec;

                                enrolledOn = Dateformat + " " + Timeformat;

                                if (empId != null && empId.trim().length() > 0) {
                                    templateInfo.setEmpId(empId);
                                } else {
                                    templateInfo.setEmpId("");
                                }

                                if (cardId != null && cardId.trim().length() > 0) {
                                    templateInfo.setCardId(cardId);
                                } else {
                                    templateInfo.setCardId("");
                                }

                                if (empName != null && empName.trim().length() > 0) {
                                    templateInfo.setEmpName(empName);
                                } else {
                                    templateInfo.setEmpName("");
                                }

                                if (fingerId != null && fingerId.trim().length() > 0) {
                                    templateInfo.setFingerId(fingerId);
                                } else {
                                    templateInfo.setFingerId("");
                                }

                                if (templateSrNo != null && templateSrNo.trim().length() > 0) {
                                    templateInfo.setTemplateSrNo(templateSrNo);
                                } else {
                                    templateInfo.setTemplateSrNo("");
                                }

                                if (fingerIndex != null && fingerIndex.trim().length() > 0) {
                                    templateInfo.setFingerIndex(fingerIndex);
                                } else {
                                    templateInfo.setFingerIndex("");
                                }

                                if (securityLevel != null && securityLevel.trim().length() > 0) {
                                    templateInfo.setSecurityLevel(securityLevel);
                                } else {
                                    templateInfo.setSecurityLevel("");
                                }

                                if (verificationMode != null && verificationMode.trim().length() > 0) {
                                    templateInfo.setVerificationMode(verificationMode);
                                } else {
                                    templateInfo.setVerificationMode("");
                                }

                                if (fingerQuality != null && fingerQuality.trim().length() > 0) {
                                    templateInfo.setFingerQuality(fingerQuality);
                                } else {
                                    templateInfo.setFingerQuality("");
                                }

                                if (fmd != null && fmd.trim().length() > 0) {
                                    templateInfo.setFmd(fmd);
                                } else {
                                    templateInfo.setFmd("");
                                }

                                if (fid != null && fid.length > 0) {
                                    templateInfo.setFid(fid);
                                } else {
                                    templateInfo.setFid(null);
                                }

                                if (enrolledOn != null && enrolledOn.trim().length() > 0) {
                                    templateInfo.setDtoe(enrolledOn);
                                } else {
                                    templateInfo.setDtoe("");
                                }

                                templateUploadInfoList.add(templateInfo);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {

        } finally {
            if (resEmployeeData != null) {
                resEmployeeData.close();
            }
            if (resFingerData != null) {
                resFingerData.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return templateUploadInfoList;
    }

    public int updateFingerTemplateTable(String fingerId) {
        SQLiteDatabase db = null;
        ContentValues initialValues = null;
        int updateStatus = -1;
        try {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat mdformat = new SimpleDateFormat("ddMMyyyy");
            String strDate = mdformat.format(calendar.getTime());
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            initialValues = new ContentValues();
            initialValues.put("UploadedOn", strDate);
            initialValues.put("IsUpdatedToServer", "1");
            updateStatus = db.update(Constants.FINGER_TABLE, initialValues, "Id='" + fingerId + "'", null);
        } catch (Exception e) {

        } finally {
            if (db != null) {
                db.close();
            }
        }
        return updateStatus;
    }


    public TemplateDownloadInfo updateFingerEnrolledStatusToEmpTbl(int autoId, TemplateDownloadInfo templateDownloadInfo) {
        SQLiteDatabase db = null;
        ContentValues initialValues = null;
        int updateStatus = -1;
        try {
            templateDownloadInfo.setDbStatus(-1);
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            initialValues = new ContentValues();
            initialValues.put("EnrollStatus", "Y");
            String fingerType = templateDownloadInfo.getFingerType();
            if (fingerType != null && fingerType.equals("F1")) {
                initialValues.put("NosFinger", "1");
            } else if (fingerType != null && fingerType.equals("F2")) {
                initialValues.put("NosFinger", "2");
            }
            initialValues.put("VerificationMode", templateDownloadInfo.getVerificationMode());
            updateStatus = db.update(Constants.EMPLOYEE_TABLE, initialValues, "AutoId=" + autoId, null);
            templateDownloadInfo.setDbStatus(updateStatus);
        } catch (Exception e) {

        } finally {
            if (db != null) {
                db.close();
            }
        }
        return templateDownloadInfo;
    }

    public int updateFingerEnrolledStatusToEmpTbl(int autoId, EmployeeValidationBasicInfo info, EmployeeValidationFingerInfo fInfo) {
        SQLiteDatabase db = null;
        ContentValues initialValues = null;
        int status = -1;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            initialValues = new ContentValues();
            initialValues.put("EnrollStatus", "Y");
            String fingerType = fInfo.getFt();
            if (fingerType != null && fingerType.equals("F1")) {
                initialValues.put("NosFinger", "1");
            } else if (fingerType != null && fingerType.equals("F2")) {
                initialValues.put("NosFinger", "2");
            }
            initialValues.put("VerificationMode", info.getVm());
            status = db.update(Constants.EMPLOYEE_TABLE, initialValues, "AutoId=" + autoId, null);
        } catch (Exception e) {

        } finally {
            if (db != null) {
                db.close();
            }
        }
        return status;
    }

    public boolean isSmartCardReaderInstalled() {
        SQLiteDatabase db = null;
        Cursor rs = null;
        boolean isReaderPresent = false;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            rs = db.rawQuery("select SettingsParamVal from " + Constants.SETTINGS_TABLE + " where SettingsHeaderName='Smart Reader' and SettingsParamName='Smart Reader Type'", null);
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
        SQLiteDatabase db = null;
        Cursor rs = null;
        int value = -1;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            rs = db.rawQuery("SELECT count(distinct(AutoId)) from " + Constants.FINGER_TABLE, null);
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
        SQLiteDatabase db = null;
        Cursor rs = null;
        int value = -1;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            rs = db.rawQuery("SELECT count(*) from " + Constants.EMPLOYEE_TABLE, null);
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

    public int getTotalEnrolledUsers(String isFingerEnrolled) {
        SQLiteDatabase db = null;
        Cursor rs = null;
        int value = -1;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            rs = db.rawQuery("SELECT count(*) from " + Constants.EMPLOYEE_TABLE + " where EnrollStatus='" + isFingerEnrolled + "'", null);
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
        SQLiteDatabase db = null;
        Cursor rs = null;
        int value = -1;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            rs = db.rawQuery("SELECT count(*) from " + Constants.ATTENDANCE_TABLE + " where Uploaded='00'", null);
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

    public int getNoFingersEnrolled(int autoId) {
        SQLiteDatabase db = null;
        Cursor rs = null;
        int value = 0;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            rs = db.rawQuery("SELECT count(*) from " + Constants.FINGER_TABLE + " where AutoId='" + Integer.toString(autoId) + "'", null);
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

    public ArrayList <StartekInfo> loadDatabaseItems(ArrayList <StartekInfo> list) {
        SQLiteDatabase db = null;
        Cursor rs = null;
        int value = 0;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            rs = db.rawQuery("SELECT AutoId,Template from " + Constants.FINGER_TABLE + " where SensorType='Startek'", null);
            if (rs != null) {
                list = new ArrayList <StartekInfo>();
                String autoId = "";
                String hexTemplate = "";
                byte[] rawTemplate = null;
                while (rs.moveToNext()) {
                    StartekInfo info = new StartekInfo();
                    autoId = rs.getString(0);
                    hexTemplate = rs.getString(1);
                    rawTemplate = Utility.hexStringToByteArray(hexTemplate);
                    info.setAutoid(Integer.parseInt(autoId));
                    info.setTemplate(rawTemplate);
                    list.add(info);
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
        return list;
    }

    public boolean getEmployeeInfo(int autoId, EmployeeInfo empInfo) {
        SQLiteDatabase db = null;
        Cursor rs = null;
        String selectQuery = "";
        boolean isFingerEnrolled = false;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            selectQuery = "SELECT EmployeeId,CardId,Name,Photo FROM " + Constants.EMPLOYEE_TABLE + " WHERE " + " AutoId=" + autoId + "";
            rs = db.rawQuery(selectQuery, null);
            if (rs != null && rs.getCount() > 0) {
                while (rs.moveToNext()) {
                    empInfo.setEmpId(rs.getString(0));
                    empInfo.setCardId(rs.getString(1));
                    empInfo.setEmpName(rs.getString(2));
                    empInfo.setPhoto(rs.getBlob(3));
                }
            }
            db.close();
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            selectQuery = "SELECT TemplateSrNo,FingerIndex,Template FROM " + Constants.FINGER_TABLE + " WHERE " + " AutoId=" + autoId + "";
            rs = db.rawQuery(selectQuery, null);
            if (rs != null && rs.getCount() > 0) {
                int noOfTemplates = 0;
                while (rs.moveToNext()) {
                    String tempSrNo = rs.getString(0);
                    if (tempSrNo != null && tempSrNo.trim().length() > 0) {
                        int tempNo = Integer.parseInt(tempSrNo);
                        if (tempNo == 1) {
                            isFingerEnrolled = true;
                            empInfo.setFirstFingerIndex(rs.getString(1));
                            empInfo.setFirstFingerTemplate(rs.getString(2));
                            noOfTemplates++;
                        } else if (tempNo == 2) {
                            isFingerEnrolled = true;
                            empInfo.setSecondFingerIndex(rs.getString(1));
                            empInfo.setSecondFingerTemplate(rs.getString(2));
                            noOfTemplates++;
                        }
                    }
                }
                empInfo.setNoOfTemplates(noOfTemplates);
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
        return isFingerEnrolled;
    }

    public boolean getAppSettings() {
        SQLiteDatabase db = null;
        boolean isDataFound = false;
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            resData = db.rawQuery("select SettingsParamName,SettingsParamVal from " + Constants.SETTINGS_TABLE, null);
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
                            case "App Type":
                                val = Integer.parseInt(resData.getString(1));
                                settings.setAppType(val);
                                break;
                            case "App SubType":
                                val = Integer.parseInt(resData.getString(1));
                                settings.setAppSubType(val);
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        } catch (SQLiteException e) {
            Log.d("TEST", "getAppSettings Exception:" + e.getMessage());
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
        SQLiteDatabase db = null;
        boolean isDataFound = false;
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            resData = db.rawQuery("select IPAddress,Port,Domain,Url from " + Constants.ATTENDANCE_SERVER_TABLE, null);
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

    public Cursor getUserData(String strUsername, String strPassword) {
        SQLiteDatabase db = null;
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select AutoId,Name,Photo,isAdmin from " + Constants.USER_TABLE + " where UserName='" + strUsername + "' and Password='" + strPassword + "'", null);
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

    public boolean isSettingsFound(String header, String key) {
        SQLiteDatabase db = null;
        Cursor resData = null;
        boolean found = false;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            resData = db.rawQuery("select count(*) from " + Constants.SETTINGS_TABLE + " where SettingsHeaderName='" + header + "' and SettingsParamName='" + key + "'", null);
            if (resData != null && resData.getCount() > 0) {
                found = true;
            }
        } catch (SQLiteException e) {
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
        SQLiteDatabase db = null;
        int status = -1;
        String strCondition = "";
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            strCondition = "SettingsHeaderName='" + header + "' and SettingsParamName='" + key + "'";
            status = db.delete(Constants.SETTINGS_TABLE, strCondition, null);
        } catch (SQLiteException e) {
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return status;
    }

    public int insertSettingsData(String header, String key, String val) {
        SQLiteDatabase db = null;
        int status = -1;
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            resData = db.rawQuery("select ValueDetails from " + Constants.SETTINGS_INI_TABLE + " where SettingsHeaderName='" + header + "' and SettingsParamName='" + key + "' and SettingsParamVal='" + val + "'", null);
            if (resData != null && resData.getCount() > 0) {
                while (resData.moveToNext()) {
                    String strValueDetails = resData.getString(0).trim();
                    ContentValues content = new ContentValues();
                    content.put("SettingsHeaderName", header);
                    content.put("SettingsParamName", key);
                    content.put("SettingsParamVal", val);
                    content.put("ValueDetails", strValueDetails);
                    status = (int) db.insert(Constants.SETTINGS_TABLE, null, content);
                }
            }
        } catch (SQLiteException e) {
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

    public ArrayList getAtServerIPPort() {
        SQLiteDatabase db = null;
        Cursor resData = null;
        ArrayList <String> list = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select IPAddress,Port from " + Constants.ATTENDANCE_SERVER_TABLE, null);
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

    public int deleteAtServerIPPort() {
        SQLiteDatabase db = null;
        int deleteStaus = -1;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            deleteStaus = db.delete(Constants.ATTENDANCE_SERVER_TABLE, "", null);
        } catch (SQLiteException e) {
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return deleteStaus;
    }

    public int insertAtServerDetails(String strAaServerIP, String strAaServerPort, String strServerDomain, String strUrl) {
        SQLiteDatabase db = null;
        int insertStatus = -1;
        try {
            ContentValues initialValues = new ContentValues();
            initialValues.put("IPAddress", strAaServerIP);
            initialValues.put("Port", strAaServerPort);
            initialValues.put("Domain", strServerDomain);
            initialValues.put("Url", strUrl);
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            insertStatus = (int) db.insert(Constants.ATTENDANCE_SERVER_TABLE, null, initialValues);
        } catch (SQLiteException e) {

        } finally {
            if (db != null) {
                db.close();
            }
        }
        return insertStatus;
    }

    public ArrayList getAaServerIPPort() {
        SQLiteDatabase db = null;
        Cursor resData = null;
        ArrayList <String> list = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select IPAddress,Port from " + Constants.AADHAAR_SERVER_TABLE, null);
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


    public int deleteAaServerIPPort() {
        SQLiteDatabase db = null;
        int deleteStaus = -1;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            deleteStaus = db.delete(Constants.AADHAAR_SERVER_TABLE, "", null);
        } catch (SQLiteException e) {

        } finally {
            if (db != null) {
                db.close();
            }
        }
        return deleteStaus;
    }

    public int insertAaServerDetails(String strAtServerIP, String strAtServerPort, String strServerDomain, String strUrl) {
        SQLiteDatabase db = null;
        int insertStatus = -1;
        try {
            ContentValues initialValues = new ContentValues();
            initialValues.put("IPAddress", strAtServerIP);
            initialValues.put("Port", strAtServerPort);
            initialValues.put("Domain", strServerDomain);
            initialValues.put("Url", strUrl);
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            insertStatus = (int) db.insert(Constants.AADHAAR_SERVER_TABLE, null, initialValues);
        } catch (SQLiteException e) {

        } finally {
            if (db != null) {
                db.close();
            }
        }
        return insertStatus;
    }


    public int saveGroupData(String strGroupName, String strGroupProperty) {
        SQLiteDatabase db = null;
        int insertStatus = -1;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            ContentValues initialValues = new ContentValues();
            initialValues.put("Name", strGroupName);
            initialValues.put("Property", strGroupProperty);
            insertStatus = (int) db.insert(Constants.GROUP_TABLE, null, initialValues);
        } catch (SQLiteException e) {
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return insertStatus;
    }

    public int saveSiteData(String strSiteCode) {
        SQLiteDatabase db = null;
        int insertStatus = -1;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            ContentValues initialValues = new ContentValues();
            initialValues.put("Code", strSiteCode);
            insertStatus = (int) db.insert(Constants.SITE_TABLE, null, initialValues);
        } catch (SQLiteException e) {
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return insertStatus;
    }

    public int saveBatchData(String strBatchNo, String strBatchName) {
        SQLiteDatabase db = null;
        int insertStatus = -1;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            ContentValues initialValues = new ContentValues();
            initialValues.put("BatchNo", strBatchNo);
            initialValues.put("BatchName", strBatchName);
            insertStatus = (int) db.insert(Constants.BATCH_TABLE, null, initialValues);
        } catch (SQLiteException e) {

        } finally {
            if (db != null) {
                db.close();
            }
        }
        return insertStatus;
    }

    public int saveTrainingData(String strTrainingNo, String strTrainingName) {
        SQLiteDatabase db = null;
        int insertStatus = -1;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            ContentValues initialValues = new ContentValues();
            initialValues.put("CenterNo", strTrainingNo);
            initialValues.put("CenterName", strTrainingName);
            insertStatus = (int) db.insert(Constants.TRAINING_TABLE, null, initialValues);
        } catch (SQLiteException e) {

        } finally {
            if (db != null) {
                db.close();
            }
        }
        return insertStatus;
    }

    public int isWiegandSettingsAvailable() {
        SQLiteDatabase db = null;
        Cursor resData = null;
        int noOfRecords = 0;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select count(*) from " + Constants.WIEGAND_TABLE, null);
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

    public int deleteWiegandSettings() {
        SQLiteDatabase db = null;
        int status = -1;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            status = db.delete(Constants.WIEGAND_TABLE, null, null);
        } catch (SQLiteException e) {

        } finally {
            if (db != null) {
                db.close();
            }
        }
        return status;
    }

    public int insertWiegandSettings(String hexToDec, String siteCodeEnabled, String cardNo) {
        SQLiteDatabase db = null;
        int status = -1;
        try {
            ContentValues initialValues = new ContentValues();
            initialValues.put("isHexToDecEnabled", hexToDec);
            initialValues.put("isSiteCodeEnabled", siteCodeEnabled);
            initialValues.put("cardNoType", cardNo);
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            status = (int) db.insert(Constants.WIEGAND_TABLE, null, initialValues);
        } catch (SQLiteException e) {

        } finally {
            if (db != null) {
                db.close();
            }
        }
        return status;
    }

    public WiegandSettingsInfo getWiegandSettings(WiegandSettingsInfo info) {
        SQLiteDatabase db = null;
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select isHexToDecEnabled,isSiteCodeEnabled,cardNoType from " + Constants.WIEGAND_TABLE, null);
            if (resData != null && resData.getCount() > 0) {
                while (resData.moveToNext()) {
                    info = new WiegandSettingsInfo();
                    info.setIsHexToDecEnabled(resData.getString(0).trim());
                    info.setIsSiteCodeEnabled(resData.getString(1).trim());
                    info.setCardNoType(resData.getString(2).trim());
                }
            }
        } catch (Exception e) {
        } finally {
            if (db != null) {
                db.close();
            }
            if (resData != null) {
                resData.close();
            }
        }
        return info;
    }

    public int insertUserDetails(String strName, String strAadhaarId, String strMobileNo, String strEmailId, String strUsername, String strPassword, String strIsAdmin, byte[] byteimage) {
        SQLiteDatabase db = null;
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

            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            insertStatus = (int) db.insert(Constants.USER_TABLE, null, initialValues);

        } catch (SQLiteException e) {

        } finally {
            if (db != null) {
                db.close();
            }
        }
        return insertStatus;
    }

    public boolean checkUserName(String strUsername) {
        SQLiteDatabase db = null;
        boolean isUserExist = false;
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select AutoId from " + Constants.USER_TABLE + " where UserName='" + strUsername + "'", null);
            if (resData != null && resData.getCount() > 0) {
                isUserExist = true;
            }
        } catch (Exception e) {
        } finally {
            if (db != null) {
                db.close();
            }
            if (resData != null) {
                resData.close();
            }
        }
        return isUserExist;

    }

    public List <DatabaseItem> getAllFingerEnrolledUser(List <DatabaseItem> databaseItems) {
        SQLiteDatabase db = null;
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select EmployeeId,CardId,Name from " + Constants.EMPLOYEE_TABLE + " where EnrollStatus='Y'", null);
            if (resData != null && resData.getCount() > 0) {
                while (resData.moveToNext()) {
                    String employeeId = resData.getString(0).trim();
                    String cardId = resData.getString(1).replaceAll("\\G0", " ").trim();
                    String empName = resData.getString(2).trim();
                    DatabaseItem item = new DatabaseItem(employeeId, cardId, empName);
                    databaseItems.add(item);
                }
            }
        } catch (Exception e) {
        } finally {
            if (db != null) {
                db.close();
            }
            if (resData != null) {
                resData.close();
            }
        }
        return databaseItems;
    }


    public int insertPubDetails(String type, String packetId, String topic, int qos, String payload, String status) {
        SQLiteDatabase db = null;
        int out = -1;
        try {
            ContentValues initialValues = new ContentValues();
            initialValues.put("type", type);
            initialValues.put("packetid", packetId);
            initialValues.put("topic", topic);
            initialValues.put("qos", Integer.toString(qos));
            initialValues.put("payload", payload);
            initialValues.put("status", status);
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            out = (int) db.insert(Constants.MQTT_TABLE, null, initialValues);
        } catch (SQLiteException e) {
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return out;
    }

    public int updatePubStatus(int id, String status) {
        SQLiteDatabase db = null;
        ContentValues initialValues = null;
        int updStatus = -1;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            initialValues = new ContentValues();
            initialValues.put("status", status);
            updStatus = db.update(Constants.MQTT_TABLE, initialValues, "packetid='" + Integer.toString(id) + "'", null);
        } catch (Exception e) {
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return updStatus;
    }

    public int insertDeviceRegStatus(String imei) {
        SQLiteDatabase db = null;
        int out = -1;
        try {
            String dateTime = new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime());
            ContentValues initialValues = new ContentValues();
            initialValues.put("imei", imei);
            initialValues.put("DateTime", dateTime);
            initialValues.put("isOnlineRegistered", "Y");
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            out = (int) db.insert(Constants.MQTT_DEV_REG_STATUS_TABLE, null, initialValues);
        } catch (SQLiteException e) {
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return out;
    }

    public String getDeviceRegStatus(String imei) {
        SQLiteDatabase db = null;
        Cursor resData = null;
        String status = "";
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select IsOnlineRegistered from " + Constants.MQTT_DEV_REG_STATUS_TABLE + " where IMEI='" + imei + "'", null);
            if (resData != null && resData.getCount() > 0) {
                while (resData.moveToNext()) {
                    status = resData.getString(0);
                }
            }
        } catch (Exception e) {
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

    public ArrayList <TemplateUploadInfo> getTemplateForAutoUploadByEmpId(int empAutoId, ArrayList <TemplateUploadInfo> templateUploadInfoList) {
        SQLiteDatabase db = null;
        Cursor resEmployeeData = null;
        Cursor resFingerData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            resEmployeeData = db.rawQuery("select EmployeeId,CardId,Name from " + Constants.EMPLOYEE_TABLE + " where AutoId=" + empAutoId, null);
            if (resEmployeeData != null && resEmployeeData.getCount() > 0) {
                templateUploadInfoList = new ArrayList <TemplateUploadInfo>();
                while (resEmployeeData.moveToNext()) {
                    String empId = resEmployeeData.getString(0);
                    String cardId = resEmployeeData.getString(1);
                    String empName = resEmployeeData.getString(2);
                    if (empAutoId != -1) {
                        db.close();
                        db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
                        resFingerData = db.rawQuery("select ID,TemplateSrNo,FingerIndex,SecurityLevel,VerificationMode,Quality,EnrolledOn,Template,FingerImage from " + Constants.FINGER_TABLE + " where AutoID='" + Integer.toString(empAutoId) + "'", null);
                        if (resFingerData != null && resFingerData.getCount() > 0) {
                            while (resFingerData.moveToNext()) {
                                TemplateUploadInfo templateInfo = new TemplateUploadInfo();
                                String fingerId = resFingerData.getString(0).trim();
                                String templateSrNo = resFingerData.getString(1).trim();
                                String fingerIndex = resFingerData.getString(2).trim();
                                String securityLevel = resFingerData.getString(3).trim();
                                String verificationMode = resFingerData.getString(4).trim();
                                String fingerQuality = resFingerData.getString(5).trim();
                                String enrolledOn = resFingerData.getString(6).trim();
                                String fmd = resFingerData.getString(7).trim();
                                byte[] fid = resFingerData.getBlob(8);

                                String date = enrolledOn.substring(0, 2);
                                String month = enrolledOn.substring(2, 4);
                                String year = enrolledOn.substring(4, 8);
                                String hr = enrolledOn.substring(8, 10);
                                String min = enrolledOn.substring(10, 12);
                                String sec = enrolledOn.substring(12);

                                enrolledOn = date + "/" + month + "/" + year;
                                String Dateformat = Utility.DateFormatChange(enrolledOn);
                                String Timeformat = hr + ":" + min + ":" + sec;

                                enrolledOn = Dateformat + " " + Timeformat;

                                if (empId != null && empId.trim().length() > 0) {
                                    templateInfo.setEmpId(empId);
                                } else {
                                    templateInfo.setEmpId("");
                                }

                                if (cardId != null && cardId.trim().length() > 0) {
                                    templateInfo.setCardId(cardId);
                                } else {
                                    templateInfo.setCardId("");
                                }

                                if (empName != null && empName.trim().length() > 0) {
                                    templateInfo.setEmpName(empName);
                                } else {
                                    templateInfo.setEmpName("");
                                }

                                if (fingerId != null && fingerId.trim().length() > 0) {
                                    templateInfo.setFingerId(fingerId);
                                } else {
                                    templateInfo.setFingerId("");
                                }

                                if (templateSrNo != null && templateSrNo.trim().length() > 0) {
                                    templateInfo.setTemplateSrNo(templateSrNo);
                                } else {
                                    templateInfo.setTemplateSrNo("");
                                }

                                if (fingerIndex != null && fingerIndex.trim().length() > 0) {
                                    templateInfo.setFingerIndex(fingerIndex);
                                } else {
                                    templateInfo.setFingerIndex("");
                                }

                                if (securityLevel != null && securityLevel.trim().length() > 0) {
                                    templateInfo.setSecurityLevel(securityLevel);
                                } else {
                                    templateInfo.setSecurityLevel("");
                                }

                                if (verificationMode != null && verificationMode.trim().length() > 0) {
                                    templateInfo.setVerificationMode(verificationMode);
                                } else {
                                    templateInfo.setVerificationMode("");
                                }

                                if (fingerQuality != null && fingerQuality.trim().length() > 0) {
                                    templateInfo.setFingerQuality(fingerQuality);
                                } else {
                                    templateInfo.setFingerQuality("");
                                }

                                if (fmd != null && fmd.trim().length() > 0) {
                                    templateInfo.setFmd(fmd);
                                } else {
                                    templateInfo.setFmd("");
                                }

                                if (fid != null && fid.length > 0) {
                                    templateInfo.setFid(fid);
                                } else {
                                    templateInfo.setFid(null);
                                }

                                if (enrolledOn != null && enrolledOn.trim().length() > 0) {
                                    templateInfo.setDtoe(enrolledOn);
                                } else {
                                    templateInfo.setDtoe("");
                                }
                                templateUploadInfoList.add(templateInfo);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.d("TEST", "Exception:" + e.getMessage());
        } finally {
            if (resEmployeeData != null) {
                resEmployeeData.close();
            }
            if (resFingerData != null) {
                resFingerData.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return templateUploadInfoList;
    }

    public int deleteAllValidationRecords() {
        SQLiteDatabase db = null;
        int status = -1;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            status = db.delete(Constants.EMPLOYEE_TABLE, null, null);
        } catch (SQLiteException e) {
            Log.d("TEST", "Exception:" + e.getMessage());
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return status;
    }

    public int deleteAllFingerRecords() {
        SQLiteDatabase db = null;
        int status = -1;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            status = db.delete(Constants.FINGER_TABLE, null, null);
        } catch (SQLiteException e) {
            Log.d("TEST", "Exception:" + e.getMessage());
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return status;
    }

    public int updateFingerDataToEmpTable() {
        SQLiteDatabase db = null;
        int status = -1;
        try {
            EmployeeFingerEnrollInfo empFingerInfo = EmployeeFingerEnrollInfo.getInstance();
            if (empFingerInfo != null) {
                ContentValues fingerDetails = new ContentValues();
                fingerDetails.put("EnrollStatus", "N");
                fingerDetails.put("NosFinger", "");
                fingerDetails.put("VerificationMode", "");
                fingerDetails.put("isTemplateAadhaarVerifiedYorN", "");
                db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
                status = db.update(Constants.EMPLOYEE_TABLE, fingerDetails, null, null);
            }
        } catch (Exception e) {

        }
        return status;
    }

    public boolean isSubDataAvailable(SubjectInfo subInfo, String st) {
        SQLiteDatabase db = null;
        boolean exists = false;
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select Sub_Id from " + Constants.SUBJECT_TABLE + " where Sub_Code='" + subInfo.getSubCode() + "' and Sub_Name='" + subInfo.getSubName() + "' and Sub_Type='" + st + "'", null);
            if (resData != null && resData.getCount() > 0) {
                exists = true;
            }
        } catch (Exception e) {
        } finally {
            if (db != null) {
                db.close();
            }
            if (resData != null) {
                resData.close();
            }
        }
        return exists;
    }

    public int insertSubData(SubjectInfo subInfo, String st) {
        SQLiteDatabase db = null;
        int out = -1;
        try {
            String createdOn = new SimpleDateFormat("ddMMyyyyhhmmss").format(Calendar.getInstance().getTime());
            ContentValues conValues = new ContentValues();
            conValues.put("Sub_Code", subInfo.getSubCode());
            conValues.put("Sub_Name", subInfo.getSubName());
            conValues.put("Sub_Type", st);
            conValues.put("Created_On", createdOn);
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            out = (int) db.insert(Constants.SUBJECT_TABLE, null, conValues);
        } catch (SQLiteException e) {
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return out;
    }

    public int deleteSubData(SubjectInfo subInfo, String st) {
        SQLiteDatabase db = null;
        int out = -1;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            out = db.delete(Constants.SUBJECT_TABLE, "Sub_Code='" + subInfo.getSubCode() + "' and Sub_Name='" + subInfo.getSubName() + "' and Sub_Type='" + st + "'", null);
        } catch (SQLiteException e) {
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return out;
    }

    public int getSubjectId(ProfessorSubjectInfo profSubInfo, String st) {
        SQLiteDatabase db = null;
        int subId = -1;
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select Sub_Id from " + Constants.SUBJECT_TABLE + " where Sub_Code='" + profSubInfo.getSubCode() + "' and Sub_Type='" + st + "'", null);
            if (resData != null) {
                int count = resData.getCount();
                if (count > 0) {
                    while (resData.moveToNext()) {
                        subId = resData.getInt(0);
                    }
                }
            }
        } catch (Exception e) {
        } finally {
            if (db != null) {
                db.close();
            }
            if (resData != null) {
                resData.close();
            }
        }
        return subId;

    }

    public boolean isProfSubDataAvailable(ProfessorSubjectInfo profSubInfo, int subId) {
        SQLiteDatabase db = null;
        boolean exists = false;
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select Auto_Id from " + Constants.PROFESSOR_SUBJECT_TABLE + " where Employee_Id='" + Utility.paddCardId(profSubInfo.getEmpId()) + "' and Sub_Id=" + subId, null);
            if (resData != null && resData.getCount() > 0) {
                exists = true;
            }
        } catch (Exception e) {
        } finally {
            if (db != null) {
                db.close();
            }
            if (resData != null) {
                resData.close();
            }
        }
        return exists;
    }


    public int insertProfSubData(ProfessorSubjectInfo profSubInfo, int subId) {
        SQLiteDatabase db = null;
        int out = -1;
        try {
            String createdOn = new SimpleDateFormat("ddMMyyyyhhmmss").format(Calendar.getInstance().getTime());
            ContentValues conValues = new ContentValues();
            conValues.put("Employee_Id", Utility.paddCardId(profSubInfo.getEmpId()));
            conValues.put("Sub_Id", subId);
            conValues.put("Created_On", createdOn);
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            out = (int) db.insert(Constants.PROFESSOR_SUBJECT_TABLE, null, conValues);
        } catch (SQLiteException e) {
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return out;
    }

    public int deleteProfSubData(ProfessorSubjectInfo profSubInfo, int subId) {
        SQLiteDatabase db = null;
        int out = -1;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            out = db.delete(Constants.PROFESSOR_SUBJECT_TABLE, "Employee_Id='" + Utility.paddCardId(profSubInfo.getEmpId()) + "' and Sub_Id=" + subId, null);
        } catch (SQLiteException e) {
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return out;

    }

    public boolean isProfStuSubDataAvailable(ProfessorStudentSubjectInfo profStuSubInfo) {
        SQLiteDatabase db = null;
        boolean exists = false;
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select Auto_Id from " + Constants.PROFESSOR_STUDENT_SUBJECT_TABLE + " where Employee_Id_P='" + Utility.paddCardId(profStuSubInfo.getProfessorEmpId()) + "' and Employee_Id_S='" + Utility.paddCardId(profStuSubInfo.getStudentEmpId()) + "' and Sub_Code='" + profStuSubInfo.getSubCode() + "'", null);
            if (resData != null && resData.getCount() > 0) {
                exists = true;
            }
        } catch (Exception e) {
            Log.d("TEST", "Exception PSS:" + e.getMessage());
        } finally {
            if (db != null) {
                db.close();
            }
            if (resData != null) {
                resData.close();
            }
        }
        return exists;
    }

    public int insertProfStuSubData(ProfessorStudentSubjectInfo profStuSubInfo) {
        SQLiteDatabase db = null;
        int out = -1;
        try {
            String createdOn = new SimpleDateFormat("ddMMyyyyhhmmss").format(Calendar.getInstance().getTime());
            ContentValues conValues = new ContentValues();
            conValues.put("Employee_Id_P", Utility.paddCardId(profStuSubInfo.getProfessorEmpId()));
            conValues.put("Employee_Id_S", Utility.paddCardId(profStuSubInfo.getStudentEmpId()));
            conValues.put("Sub_Code", profStuSubInfo.getSubCode());
            conValues.put("Created_On", createdOn);
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            out = (int) db.insert(Constants.PROFESSOR_STUDENT_SUBJECT_TABLE, null, conValues);
        } catch (SQLiteException e) {
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return out;
    }

    public int deleteProfStuSubData(ProfessorStudentSubjectInfo profStuSubInfo) {
        SQLiteDatabase db = null;
        int out = -1;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            out = db.delete(Constants.PROFESSOR_STUDENT_SUBJECT_TABLE, "Employee_Id_P='" + Utility.paddCardId(profStuSubInfo.getProfessorEmpId()) + "' and Employee_Id_S='" + Utility.paddCardId(profStuSubInfo.getStudentEmpId()) + "' and Sub_Code='" + profStuSubInfo.getSubCode() + "'", null);
        } catch (SQLiteException e) {
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return out;
    }

    public boolean isEmpTypeDataAvailable(EmployeeTypeInfo etInfo) {
        SQLiteDatabase db = null;
        boolean exists = false;
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select AutoId from " + Constants.EMPLOYEE_TYPE_TABLE + " where EmpType='" + etInfo.getEmpType() + "'", null);
            if (resData != null && resData.getCount() > 0) {
                exists = true;
            }
        } catch (Exception e) {
        } finally {
            if (db != null) {
                db.close();
            }
            if (resData != null) {
                resData.close();
            }
        }
        return exists;
    }

    public int insertEmpTypeData(EmployeeTypeInfo etInfo) {
        SQLiteDatabase db = null;
        int out = -1;
        try {
            String createdOn = new SimpleDateFormat("ddMMyyyyhhmmss").format(Calendar.getInstance().getTime());
            ContentValues conValues = new ContentValues();
            conValues.put("EmpType", etInfo.getEmpType());
            conValues.put("CreatedOn", createdOn);
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            out = (int) db.insert(Constants.EMPLOYEE_TYPE_TABLE, null, conValues);
        } catch (SQLiteException e) {
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return out;
    }

    public int deleteEmpTypeData(EmployeeTypeInfo etInfo) {
        SQLiteDatabase db = null;
        int out = -1;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            out = db.delete(Constants.EMPLOYEE_TYPE_TABLE, "EmpType='" + etInfo.getEmpType() + "'", null);
        } catch (SQLiteException e) {
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return out;
    }

    public boolean isContractorDataAvailable(ContractorInfo ctInfo) {
        SQLiteDatabase db = null;
        boolean exists = false;
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select AutoId from " + Constants.CONTRACTOR_TABLE + " where ContractorName='" + ctInfo.getContractorName() + "' and CompanyName='" + ctInfo.getCompanyName() + "'", null);
            if (resData != null && resData.getCount() > 0) {
                exists = true;
            }
        } catch (Exception e) {
        } finally {
            if (db != null) {
                db.close();
            }
            if (resData != null) {
                resData.close();
            }
        }
        return exists;
    }

    public int insertContractorData(ContractorInfo ctInfo) {
        SQLiteDatabase db = null;
        int out = -1;
        try {
            String createdOn = new SimpleDateFormat("ddMMyyyyhhmmss").format(Calendar.getInstance().getTime());
            ContentValues conValues = new ContentValues();
            conValues.put("ContractorName", ctInfo.getContractorName());
            conValues.put("CompanyName", ctInfo.getCompanyName());
            conValues.put("CreatedOn", createdOn);
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            out = (int) db.insert(Constants.CONTRACTOR_TABLE, null, conValues);
        } catch (SQLiteException e) {
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return out;
    }

    public int deleteContractorData(ContractorInfo ctInfo) {
        SQLiteDatabase db = null;
        int out = -1;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            out = db.delete(Constants.CONTRACTOR_TABLE, "ContractorName='" + ctInfo.getContractorName() + "' and CompanyName='" + ctInfo.getCompanyName() + "'", null);
        } catch (SQLiteException e) {
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return out;
    }

    public boolean isPeriodDataAvailable(PeriodInfo pdInfo) {
        SQLiteDatabase db = null;
        boolean exists = false;
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select Auto_Id from " + Constants.PERIOD_TABLE + " where Period='" + pdInfo.getPeriod() + "' and From_Time='" + pdInfo.getFromTime() + "' and To_Time='" + pdInfo.getToTime() + "' and Sub_Type='" + pdInfo.getSubType() + "'", null);
            if (resData != null && resData.getCount() > 0) {
                exists = true;
            }
        } catch (Exception e) {
        } finally {
            if (db != null) {
                db.close();
            }
            if (resData != null) {
                resData.close();
            }
        }
        return exists;
    }

    public int insertPeriodData(PeriodInfo pdInfo) {
        SQLiteDatabase db = null;
        int out = -1;
        try {
            String createdOn = new SimpleDateFormat("ddMMyyyyhhmmss").format(Calendar.getInstance().getTime());
            ContentValues conValues = new ContentValues();
            conValues.put("Period", pdInfo.getPeriod());
            conValues.put("From_Time", pdInfo.getFromTime());
            conValues.put("To_Time", pdInfo.getToTime());
            conValues.put("Sub_Type", pdInfo.getSubType());
            conValues.put("Created_On", createdOn);
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            out = (int) db.insert(Constants.PERIOD_TABLE, null, conValues);
        } catch (SQLiteException e) {
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return out;
    }

    public int deletePeriodData(PeriodInfo pdInfo) {
        SQLiteDatabase db = null;
        int out = -1;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            out = db.delete(Constants.PERIOD_TABLE, "Period='" + pdInfo.getPeriod() + "' and From_Time='" + pdInfo.getFromTime() + "' and To_Time='" + pdInfo.getToTime() + "' and Sub_Type='" + pdInfo.getSubType() + "'", null);
        } catch (SQLiteException e) {
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return out;
    }

    public boolean isEtValid(String value) {
        SQLiteDatabase db = null;
        boolean exists = false;
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select AutoId from " + Constants.EMPLOYEE_TYPE_TABLE + " where EmpType='" + value + "'", null);
            if (resData != null && resData.getCount() > 0) {
                exists = true;
            }
        } catch (Exception e) {
        } finally {
            if (db != null) {
                db.close();
            }
            if (resData != null) {
                resData.close();
            }
        }
        return exists;
    }

    public int getAutoIdByEmpType(String empType) {
        SQLiteDatabase db = null;
        int autoId = -1;
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select AutoId from " + Constants.EMPLOYEE_TYPE_TABLE + " where EmpType='" + empType + "'", null);
            if (resData != null) {
                if (resData.getCount() > 0) {
                    while (resData.moveToNext()) {
                        autoId = resData.getInt(0);
                    }
                }
            }
        } catch (Exception e) {
        } finally {
            if (db != null) {
                db.close();
            }
            if (resData != null) {
                resData.close();
            }
        }
        return autoId;
    }

    public int checkIsProfessor(String userId) {
        SQLiteDatabase db = null;
        int autoId = -1;
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select Auto_Id from " + Constants.PROFESSOR_SUBJECT_TABLE + " where Employee_Id='" + userId + "' limit 1", null);
            if (resData != null) {
                if (resData.getCount() > 0) {
                    while (resData.moveToNext()) {
                        autoId = resData.getInt(0);
                    }
                }
            }
        } catch (Exception e) {
        } finally {
            if (db != null) {
                db.close();
            }
            if (resData != null) {
                resData.close();
            }
        }
        return autoId;
    }

    public ArrayList <String> getSubjectIdList(String userId) {
        SQLiteDatabase db = null;
        ArrayList <String> subIdList = null;
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select Sub_Id from " + Constants.PROFESSOR_SUBJECT_TABLE + " where Employee_Id='" + userId + "'", null);
            if (resData != null) {
                if (resData.getCount() > 0) {
                    subIdList = new ArrayList <String>();
                    while (resData.moveToNext()) {
                        subIdList.add(resData.getString(0));
                    }
                }
            }
        } catch (Exception e) {
        } finally {
            if (db != null) {
                db.close();
            }
            if (resData != null) {
                resData.close();
            }
        }
        return subIdList;
    }

    public ArrayList <SubInfo> getSubjectName(ArrayList <String> subList, ArrayList <SubInfo> subInfoList) {
        SQLiteDatabase db = null;
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);

            //SELECT  distinct sub_code,sub_name FROM subjectm where sub_id in(1,2,3,4


            //resData = db.rawQuery("select distinct sub_code,sub_name from " + Constants.SUBJECT_TABLE + " where Sub_Id in(1,2,3,4)" , null);

            subInfoList = new ArrayList <SubInfo>();
            for (int i = 0; i < subList.size(); i++) {
                resData = db.rawQuery("select Sub_Code,Sub_Name,Sub_Type from " + Constants.SUBJECT_TABLE + " where Sub_Id='" + subList.get(i) + "'", null);
                if (resData != null) {
                    if (resData.getCount() > 0) {
                        while (resData.moveToNext()) {
                            SubInfo subInfo = new SubInfo();
                            subInfo.setSubCode(resData.getString(0));
                            subInfo.setSubName(resData.getString(1));
                            subInfo.setSubType(resData.getString(2));
                            subInfoList.add(subInfo);
                        }
                    }
                }


            }

        } catch (Exception e) {
        } finally {
            if (db != null) {
                db.close();
            }
            if (resData != null) {
                resData.close();
            }
        }
        return subInfoList;
    }

    public ArrayList <String> getSubTypes(String subCode, String subName, ArrayList <String> subTypesList) {
        SQLiteDatabase db = null;
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select Sub_Type from " + Constants.SUBJECT_TABLE + " where Sub_Code='" + subCode + "' and Sub_Name='" + subName + "'", null);
            if (resData != null) {
                if (resData.getCount() > 0) {
                    subTypesList = new ArrayList <String>();
                    while (resData.moveToNext()) {
                        subTypesList.add(resData.getString(0));
                    }
                }
            }


        } catch (Exception e) {
        } finally {
            if (db != null) {
                db.close();
            }
            if (resData != null) {
                resData.close();
            }
        }
        return subTypesList;
    }

    public boolean isStudentValid(String subCode, String profId, String studentId) {
        SQLiteDatabase db = null;
        boolean exists = false;
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select Auto_Id from " + Constants.PROFESSOR_STUDENT_SUBJECT_TABLE + " where Employee_Id_P='" + profId + "' and Employee_Id_S='" + studentId + "' and Sub_Code='" + subCode + "'", null);
            if (resData != null && resData.getCount() > 0) {
                exists = true;
            }
        } catch (Exception e) {
            Log.d("TEST", "Exception:" + e.getMessage());
        } finally {
            if (db != null) {
                db.close();
            }
            if (resData != null) {
                resData.close();
            }
        }
        return exists;
    }

    public int getSubId(String subCode) {
        SQLiteDatabase db = null;
        Cursor resData = null;
        int autoId = -1;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select Sub_Id from " + Constants.SUBJECT_TABLE + " where Sub_Code='" + subCode + "'", null);
            if (resData != null) {
                if (resData.getCount() > 0) {
                    while (resData.moveToNext()) {
                        autoId = resData.getInt(0);
                    }
                }
            }
        } catch (Exception e) {
        } finally {
            if (db != null) {
                db.close();
            }
            if (resData != null) {
                resData.close();
            }
        }
        return autoId;
    }

    public ArrayList <String> getProfessorListFromSub(int subId, ArrayList <String> professorList) {
        SQLiteDatabase db = null;
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select Employee_Id from " + Constants.PROFESSOR_SUBJECT_TABLE + " where Sub_Id='" + Integer.toString(subId) + "'", null);
            if (resData != null) {
                if (resData.getCount() > 0) {
                    professorList = new ArrayList <String>();
                    while (resData.moveToNext()) {
                        professorList.add(resData.getString(0));
                    }
                }
            }
        } catch (Exception e) {
        } finally {
            if (db != null) {
                db.close();
            }
            if (resData != null) {
                resData.close();
            }
        }
        return professorList;
    }

    public ArrayList <String> getSubjectIdList(String subCode, ArrayList <String> subIdList) {
        SQLiteDatabase db = null;
        Cursor resData = null;
        int autoId = -1;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select Sub_Id from " + Constants.SUBJECT_TABLE + " where Sub_Code='" + subCode + "'", null);
            if (resData != null) {
                if (resData.getCount() > 0) {
                    subIdList = new ArrayList <String>();
                    while (resData.moveToNext()) {
                        subIdList.add(Integer.toString(resData.getInt(0)));
                    }
                }
            }
        } catch (Exception e) {
        } finally {
            if (db != null) {
                db.close();
            }
            if (resData != null) {
                resData.close();
            }
        }
        return subIdList;
    }

    public void getEmpValAndTempForUpload() {

    }

    public ArrayList <EmployeeValidationBasicInfo> getAllEnrolledTemplatesForMqtt(ArrayList <EmployeeValidationBasicInfo> empBasicInfoList) {
        SQLiteDatabase db = null;
        Cursor resEmployeeData = null;
        Cursor resFingerData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            resEmployeeData = db.rawQuery("select AutoId,EmployeeId,CardId,Name,Pin,BirthDay,ValidUpto,isLockOpenWhenAllowed,MobileNo,BloodGroup,MailId,SiteCode,IsBlackListed,VerificationMode from " + Constants.EMPLOYEE_TABLE, null);
            if (resEmployeeData != null && resEmployeeData.getCount() > 0) {
                empBasicInfoList = new ArrayList <EmployeeValidationBasicInfo>();
                while (resEmployeeData.moveToNext()) {
                    EmployeeValidationBasicInfo basicInfo = new EmployeeValidationBasicInfo();
                    int autoId = resEmployeeData.getInt(0);
                    String empId = resEmployeeData.getString(1);
                    String cardId = resEmployeeData.getString(2);
                    String empName = resEmployeeData.getString(3);
                    String pin = resEmployeeData.getString(4);
                    String dob = resEmployeeData.getString(5);
                    String dov = resEmployeeData.getString(6);
                    String isLockOpen = resEmployeeData.getString(7);
                    String mobileNo = resEmployeeData.getString(8);
                    String bloodGroup = resEmployeeData.getString(9);
                    String mailId = resEmployeeData.getString(10);
                    String siteCode = resEmployeeData.getString(11);
                    String isBlackListed = resEmployeeData.getString(12);

                    String vm = resEmployeeData.getString(13);

                    basicInfo.setEnrollmentNo(autoId);

                    if (empId != null && empId.trim().length() > 0) {
                        basicInfo.setEmpId(empId);
                    } else {
                        basicInfo.setEmpId("");
                    }

                    if (cardId != null && cardId.trim().length() > 0) {
                        basicInfo.setCardId(cardId);
                    } else {
                        basicInfo.setCardId("");
                    }

                    if (empName != null && empName.trim().length() > 0) {
                        basicInfo.setEmpName(empName);
                    } else {
                        basicInfo.setEmpName("");
                    }

                    if (pin != null && pin.trim().length() > 0) {
                        basicInfo.setPin(pin);
                    } else {
                        basicInfo.setPin("");
                    }

                    if (dob != null && dob.trim().length() > 0) {
                        basicInfo.setDob(dob);
                    } else {
                        basicInfo.setDob("");
                    }

                    if (dov != null && dov.trim().length() > 0) {
                        basicInfo.setDov(dov);
                    } else {
                        basicInfo.setDov("");
                    }

                    if (isLockOpen != null && isLockOpen.trim().length() > 0) {
                        basicInfo.setIsLockOpen(isLockOpen);
                    } else {
                        basicInfo.setIsLockOpen("");
                    }

                    if (mobileNo != null && mobileNo.trim().length() > 0) {
                        basicInfo.setMn(mobileNo);
                    } else {
                        basicInfo.setMn("");
                    }

                    if (bloodGroup != null && bloodGroup.trim().length() > 0) {
                        basicInfo.setBg(bloodGroup);
                    } else {
                        basicInfo.setBg("");
                    }

                    if (mailId != null && mailId.trim().length() > 0) {
                        basicInfo.setEid(mailId);
                    } else {
                        basicInfo.setEid("");
                    }

                    if (siteCode != null && siteCode.trim().length() > 0) {
                        basicInfo.setSc(siteCode);
                    } else {
                        basicInfo.setSc("");
                    }

                    if (isBlackListed != null && isBlackListed.trim().length() > 0) {
                        basicInfo.setIsBlackListed(isBlackListed);
                    } else {
                        basicInfo.setIsBlackListed("");
                    }

                    if (vm != null && vm.trim().length() > 0) {
                        int value = Utility.getVerificationModeValByName(vm);
                        basicInfo.setVm(Integer.toString(value));
                    } else {
                        basicInfo.setVm("");
                    }

                    ArrayList <EmployeeValidationFingerInfo> fiList = null;

                    if (autoId != -1) {
                        db.close();
                        db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
                        resFingerData = db.rawQuery("select TemplateSrNo,SecurityLevel,FingerIndex,Quality,Template from " + Constants.FINGER_TABLE + " where AutoId='" + Integer.toString(autoId) + "'", null);
                        if (resFingerData != null && resFingerData.getCount() > 0) {
                            fiList = new ArrayList <EmployeeValidationFingerInfo>();
                            while (resFingerData.moveToNext()) {
                                EmployeeValidationFingerInfo templateInfo = new EmployeeValidationFingerInfo();
                                String templateSrNo = resFingerData.getString(0).trim();
                                String securityLevel = resFingerData.getString(1).trim();
                                String fingerIndex = resFingerData.getString(2).trim();
                                String fingerQuality = resFingerData.getString(3).trim();
                                String fmd = resFingerData.getString(4).trim();

                                if (templateSrNo != null && templateSrNo.trim().length() > 0) {
                                    if (templateSrNo.equals("1")) {
                                        templateInfo.setFt("F1");
                                    } else if (templateSrNo.equals("2")) {
                                        templateInfo.setFt("F2");
                                    }
                                } else {
                                    templateInfo.setFt("");
                                }

                                int value = -1;

                                if (securityLevel != null && securityLevel.trim().length() > 0) {
                                    value = Utility.getSecurityLvlValByName(securityLevel);
                                    templateInfo.setSl(Integer.toString(value));
                                } else {
                                    templateInfo.setSl("");
                                }

                                if (fingerIndex != null && fingerIndex.trim().length() > 0) {
                                    value = Utility.getFingerIndexValByName(fingerIndex);
                                    templateInfo.setFi(Integer.toString(value));
                                } else {
                                    templateInfo.setFi("");
                                }

                                if (fingerQuality != null && fingerQuality.trim().length() > 0) {
                                    value = Utility.getFingerQualityValByName(fingerQuality);
                                    templateInfo.setFq(Integer.toString(value));
                                } else {
                                    templateInfo.setFq("");
                                }

                                if (fmd != null && fmd.trim().length() > 0) {
                                    templateInfo.setFmd(fmd);
                                } else {
                                    templateInfo.setFmd("");
                                }
                                fiList.add(templateInfo);
                            }
                        }
                    }
                    basicInfo.setfInfoList(fiList);
                    empBasicInfoList.add(basicInfo);
                }
            }
        } catch (Exception e) {
            Log.d("TEST", "Exception:" + e.getMessage());
        } finally {
            if (resEmployeeData != null) {
                resEmployeeData.close();
            }
            if (resFingerData != null) {
                resFingerData.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return empBasicInfoList;
    }


    public ArrayList <EmployeeValidationBasicInfo> getAutoEnrolledTemplatesForMqtt(ArrayList <EmployeeValidationBasicInfo> empBasicInfoList) {
        SQLiteDatabase db = null;
        Cursor resEmployeeData = null;
        Cursor resFingerData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            resEmployeeData = db.rawQuery("select AutoId,EmployeeId,CardId,Name,EmpTypeId,Pin,BirthDay,ValidUpto,isLockOpenWhenAllowed,MobileNo,BloodGroup,MailId,SiteCode,IsBlackListed,VerificationMode from " + Constants.EMPLOYEE_TABLE, null);
            if (resEmployeeData != null && resEmployeeData.getCount() > 0) {
                empBasicInfoList = new ArrayList <EmployeeValidationBasicInfo>();
                while (resEmployeeData.moveToNext()) {
                    EmployeeValidationBasicInfo basicInfo = new EmployeeValidationBasicInfo();
                    int autoId = resEmployeeData.getInt(0);
                    String empId = resEmployeeData.getString(1);
                    String cardId = resEmployeeData.getString(2);
                    String empName = resEmployeeData.getString(3);
                    String empType = resEmployeeData.getString(4);
                    String pin = resEmployeeData.getString(5);
                    String dob = resEmployeeData.getString(6);
                    String dov = resEmployeeData.getString(7);
                    String isLockOpen = resEmployeeData.getString(8);
                    String mobileNo = resEmployeeData.getString(9);
                    String bloodGroup = resEmployeeData.getString(10);
                    String mailId = resEmployeeData.getString(11);
                    String siteCode = resEmployeeData.getString(12);
                    String isBlackListed = resEmployeeData.getString(13);

                    String vm = resEmployeeData.getString(14);

                    basicInfo.setEnrollmentNo(autoId);

                    if (empId != null && empId.trim().length() > 0) {
                        basicInfo.setEmpId(empId);
                    } else {
                        basicInfo.setEmpId("");
                    }

                    if (cardId != null && cardId.trim().length() > 0) {
                        basicInfo.setCardId(cardId);
                    } else {
                        basicInfo.setCardId("");
                    }

                    if (empName != null && empName.trim().length() > 0) {
                        basicInfo.setEmpName(empName);
                    } else {
                        basicInfo.setEmpName("");
                    }

                    if (empType != null && empType.trim().length() > 0) {
                        basicInfo.setEmpType(empType);
                    } else {
                        basicInfo.setEmpType("");
                    }

                    if (pin != null && pin.trim().length() > 0) {
                        basicInfo.setPin(pin);
                    } else {
                        basicInfo.setPin("");
                    }

                    if (dob != null && dob.trim().length() > 0) {
                        basicInfo.setDob(dob);
                    } else {
                        basicInfo.setDob("");
                    }

                    if (dov != null && dov.trim().length() > 0) {
                        basicInfo.setDov(dov);
                    } else {
                        basicInfo.setDov("");
                    }

                    if (isLockOpen != null && isLockOpen.trim().length() > 0) {
                        basicInfo.setIsLockOpen(isLockOpen);
                    } else {
                        basicInfo.setIsLockOpen("");
                    }

                    if (mobileNo != null && mobileNo.trim().length() > 0) {
                        basicInfo.setMn(mobileNo);
                    } else {
                        basicInfo.setMn("");
                    }

                    if (bloodGroup != null && bloodGroup.trim().length() > 0) {
                        basicInfo.setBg(bloodGroup);
                    } else {
                        basicInfo.setBg("");
                    }

                    if (mailId != null && mailId.trim().length() > 0) {
                        basicInfo.setEid(mailId);
                    } else {
                        basicInfo.setEid("");
                    }

                    if (siteCode != null && siteCode.trim().length() > 0) {
                        basicInfo.setSc(siteCode);
                    } else {
                        basicInfo.setSc("");
                    }

                    if (isBlackListed != null && isBlackListed.trim().length() > 0) {
                        basicInfo.setIsBlackListed(isBlackListed);
                    } else {
                        basicInfo.setIsBlackListed("");
                    }

                    if (vm != null && vm.trim().length() > 0) {
                        int value = Utility.getVerificationModeValByName(vm);
                        basicInfo.setVm(Integer.toString(value));
                    } else {
                        basicInfo.setVm("");
                    }

                    ArrayList <EmployeeValidationFingerInfo> fiList = null;

                    if (autoId != -1) {
                        db.close();
                        db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
                        resFingerData = db.rawQuery("select TemplateSrNo,SecurityLevel,FingerIndex,Quality,Template from " + Constants.FINGER_TABLE + " where AutoId='" + Integer.toString(autoId) + "' and isUpdatedToServer='0'", null);
                        if (resFingerData != null && resFingerData.getCount() > 0) {
                            fiList = new ArrayList <EmployeeValidationFingerInfo>();
                            while (resFingerData.moveToNext()) {
                                EmployeeValidationFingerInfo templateInfo = new EmployeeValidationFingerInfo();
                                String templateSrNo = resFingerData.getString(0).trim();
                                String securityLevel = resFingerData.getString(1).trim();
                                String fingerIndex = resFingerData.getString(2).trim();
                                String fingerQuality = resFingerData.getString(3).trim();
                                String fmd = resFingerData.getString(4).trim();

                                if (templateSrNo != null && templateSrNo.trim().length() > 0) {
                                    if (templateSrNo.equals("1")) {
                                        templateInfo.setFt("F1");
                                    } else if (templateSrNo.equals("2")) {
                                        templateInfo.setFt("F2");
                                    }
                                } else {
                                    templateInfo.setFt("");
                                }

                                int value = -1;

                                if (securityLevel != null && securityLevel.trim().length() > 0) {
                                    value = Utility.getSecurityLvlValByName(securityLevel);
                                    templateInfo.setSl(Integer.toString(value));
                                } else {
                                    templateInfo.setSl("");
                                }

                                if (fingerIndex != null && fingerIndex.trim().length() > 0) {
                                    value = Utility.getFingerIndexValByName(fingerIndex);
                                    if (value == 10) {
                                        templateInfo.setFi("A");
                                    } else {
                                        templateInfo.setFi(Integer.toString(value));
                                    }
                                } else {
                                    templateInfo.setFi("");
                                }

                                if (fingerQuality != null && fingerQuality.trim().length() > 0) {
                                    value = Utility.getFingerQualityValByName(fingerQuality);
                                    templateInfo.setFq(Integer.toString(value));
                                } else {
                                    templateInfo.setFq("");
                                }

                                if (fmd != null && fmd.trim().length() > 0) {
                                    templateInfo.setFmd(fmd);
                                } else {
                                    templateInfo.setFmd("");
                                }
                                fiList.add(templateInfo);
                            }
                        }
                    }
                    basicInfo.setfInfoList(fiList);
                    empBasicInfoList.add(basicInfo);
                }
            }
        } catch (Exception e) {
            Log.d("TEST", "Exception:" + e.getMessage());
        } finally {
            if (resEmployeeData != null) {
                resEmployeeData.close();
            }
            if (resFingerData != null) {
                resFingerData.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return empBasicInfoList;
    }

    public String getEmpTypeByAutoId(int empTypeId) {
        SQLiteDatabase db = null;
        Cursor resData = null;
        String empType = "";
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select EmpType from " + Constants.EMPLOYEE_TYPE_TABLE + " where AutoId=" + empTypeId, null);
            if (resData != null && resData.getCount() > 0) {
                while (resData.moveToNext()) {
                    empType = resData.getString(0);
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
        return empType;

    }

    public int updateFingerUploadStatus(EmployeeValidationBasicInfo info) {
        SQLiteDatabase db = null;
        ContentValues initialValues = null;
        String strCondition = "";
        int updateStatus = -1;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            initialValues = new ContentValues();
            initialValues.put("isUpdatedToServer", "1");
            strCondition = "AutoId='" + info.getEnrollmentNo() + "'";
            updateStatus = db.update(Constants.FINGER_TABLE, initialValues, strCondition, null);
        } catch (Exception e) {
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return updateStatus;
    }

    public int getEmployeeTypeId(String empType) {
        SQLiteDatabase db = null;
        int AutoId = -1;
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select AutoId from " + Constants.EMPLOYEE_TYPE_TABLE + " where EmpType='" + empType + "'", null);
            if (resData != null && resData.getCount() > 0) {
                while (resData.moveToNext()) {
                    AutoId = resData.getInt(0);
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
        return AutoId;
    }

    public int getTotalEnrolledProfessorOrStudent(int autoId) {
        SQLiteDatabase db = null;
        int total = -1;
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select count(*) from " + Constants.EMPLOYEE_TABLE + " where EmpTypeId=" + autoId, null);
            if (resData != null && resData.getCount() > 0) {
                while (resData.moveToNext()) {
                    total = resData.getInt(0);
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
        return total;
    }

    public int getTotalSubjects() {
        SQLiteDatabase db = null;
        Cursor resData = null;
        int total = -1;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select count(distinct Sub_Name) from " + Constants.SUBJECT_TABLE, null);
            if (resData != null && resData.getCount() > 0) {
                while (resData.moveToNext()) {
                    total = resData.getInt(0);
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
        return total;
    }

    public int insertCollegeAttendanceData(String profEID, String profCID, String studentEID, String studentCID, String subjectCode, String subType, String inOutMode, String latitude, String longitude) {
        SQLiteDatabase db = null;
        int status = -1;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("ddMMyyyy HHmmss"); //Modified By Sanjay Shyamal
        String strDateTime = mdformat.format(calendar.getTime());
        try {
            String[] splitDateAndTime = strDateTime.split(" ");
            ContentValues initialValues = new ContentValues();

            initialValues.put("EIDP", profEID);
            if (profCID.trim().length() > 0) {
                initialValues.put("CIDP", Utility.paddCardId(profCID));
            } else {
                initialValues.put("CIDP", "");
            }

            initialValues.put("EIDS", studentEID);
            if (studentCID.trim().length() > 0) {
                initialValues.put("CIDS", Utility.paddCardId(studentCID));
            } else {
                initialValues.put("CIDS", "");
            }

            initialValues.put("SC", subjectCode);
            initialValues.put("SST", subType);
            initialValues.put("InOutMode", inOutMode);
            initialValues.put("Lat", latitude);
            initialValues.put("Long", longitude);
            initialValues.put("PunchDate", splitDateAndTime[0]);
            initialValues.put("PunchTime", splitDateAndTime[1]);
            initialValues.put("Uploaded", 0);
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            status = (int) db.insert(Constants.ATTENDANCE_COLLEGE_TABLE, null, initialValues);
        } catch (SQLiteException e) {
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return status;
    }

    public int insertLoginStatus(String profEID, String profCID, String subjectCode, String subType, String isLogin, int nosp) {
        SQLiteDatabase db = null;
        int status = -1;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("ddMMyyyyHHmmss");
        String strDateTime = mdformat.format(calendar.getTime());
        try {
            ContentValues initialValues = new ContentValues();
            initialValues.put("EIDP", profEID);
            initialValues.put("CIDP", Utility.paddCardId(profCID));
            initialValues.put("SC", subjectCode);
            initialValues.put("SST", subType);
            initialValues.put("NOSP", nosp);
            initialValues.put("IsLogin", isLogin);
            initialValues.put("DateTime", strDateTime);
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            status = (int) db.insert(Constants.LOGIN_STATUS_TABLE, null, initialValues);
        } catch (SQLiteException e) {
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return status;
    }

    public int getLastLoginStatusId() {
        SQLiteDatabase db = null;
        int id = -1;
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select id from " + Constants.LOGIN_STATUS_TABLE + " order by id DESC LIMIT 1", null);
            if (resData != null && resData.getCount() > 0) {
                while (resData.moveToNext()) {
                    id = resData.getInt(0);
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
        return id;
    }

    public int updateLoginStatus(int loginId) {
        SQLiteDatabase db = null;
        ContentValues initialValues = null;
        String strCondition = "";
        int updateStatus = -1;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            initialValues = new ContentValues();
            initialValues.put("IsLogin", "N");
            initialValues.put("NOSP", 0);
            strCondition = "Id=" + loginId;
            updateStatus = db.update(Constants.LOGIN_STATUS_TABLE, initialValues, strCondition, null);
        } catch (Exception e) {
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return updateStatus;
    }

    public int getNoOfStudentsPunched(int loginId) {
        SQLiteDatabase db = null;
        int count = -1;
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select nosp from " + Constants.LOGIN_STATUS_TABLE + " where Id=" + loginId, null);
            if (resData != null && resData.getCount() > 0) {
                while (resData.moveToNext()) {
                    count = resData.getInt(0);
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
        return count;
    }

    public int updateNoOfStudentsPunched(int loginId, int count) {
        SQLiteDatabase db = null;
        ContentValues initialValues = null;
        String strCondition = "";
        int updateStatus = -1;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            initialValues = new ContentValues();
            initialValues.put("NOSP", count);
            strCondition = "Id=" + loginId;
            updateStatus = db.update(Constants.LOGIN_STATUS_TABLE, initialValues, strCondition, null);
        } catch (Exception e) {
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return updateStatus;
    }

    public ArrayList <String> getLastLoginStatus(int loginId) {
        SQLiteDatabase db = null;
        ArrayList <String> loginStatusList = null;
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select IsLogin,NOSP from " + Constants.LOGIN_STATUS_TABLE + " where Id=" + loginId, null);
            if (resData != null && resData.getCount() > 0) {
                loginStatusList = new ArrayList <String>();
                while (resData.moveToNext()) {
                    loginStatusList.add(resData.getString(0));
                    loginStatusList.add(Integer.toString(resData.getInt(1)));
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
        return loginStatusList;
    }

    public CollegeAttendanceInfo getCollegeAttendanceData(CollegeAttendanceInfo attendanceInfo) {
        SQLiteDatabase db = null;
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select ID,EIDP,EIDS,SC,SST,InOutMode,Lat,Long,PunchDate,PunchTime from " + Constants.ATTENDANCE_COLLEGE_TABLE + " where Uploaded='0' limit 1", null); // limit 1
            if (resData != null && resData.getCount() > 0) {
                attendanceInfo = new CollegeAttendanceInfo();
                while (resData.moveToNext()) {
                    int attendanceId = resData.getInt(0);
                    String profId = resData.getString(1);
                    String studentId = resData.getString(2);
                    String subjectCode = resData.getString(3);
                    String subjectType = resData.getString(4);

                    String inOutMode = resData.getString(5);
                    String latitude = resData.getString(6);
                    String longitude = resData.getString(7);

                    String date = resData.getString(8);
                    String time = resData.getString(9);

                    attendanceInfo.setAttendanceId(attendanceId);

                    if (profId != null && profId.trim().length() > 0) {
                        attendanceInfo.setEid_p(profId);
                    } else {
                        attendanceInfo.setEid_p("");
                    }

                    if (studentId != null && studentId.trim().length() > 0) {
                        attendanceInfo.setEid_s(studentId);
                    } else {
                        attendanceInfo.setEid_s("");
                    }

                    if (subjectCode != null && subjectCode.trim().length() > 0) {
                        attendanceInfo.setSc(subjectCode);
                    } else {
                        attendanceInfo.setSc("");
                    }

                    if (subjectType != null && subjectType.trim().length() > 0) {
                        attendanceInfo.setSt(subjectType);
                    } else {
                        attendanceInfo.setSt("");
                    }


                    if (inOutMode != null && inOutMode.trim().length() > 0) {
                        attendanceInfo.setInOutMode(inOutMode);
                    } else {
                        attendanceInfo.setInOutMode("");
                    }


                    if (latitude != null && latitude.trim().length() > 0) {
                        attendanceInfo.setLatitude(latitude);
                    } else {
                        attendanceInfo.setLatitude("");
                    }

                    if (longitude != null && longitude.trim().length() > 0) {
                        attendanceInfo.setLongitude(longitude);
                    } else {
                        attendanceInfo.setLongitude("");
                    }

                    if (date != null && date.trim().length() > 0) {
                        attendanceInfo.setPunchDate(date);
                    } else {
                        attendanceInfo.setPunchDate("");
                    }

                    if (time != null && time.trim().length() > 0) {
                        attendanceInfo.setPunchTime(time);
                    } else {
                        attendanceInfo.setPunchTime("");
                    }
                }
            }
        } catch (Exception e) {
            Log.d("TEST", "Exception get attendance data:" + e.getMessage());
        } finally {
            if (resData != null) {
                resData.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return attendanceInfo;
    }

    public int updateUploadStatus(int attendanceId) {
        SQLiteDatabase db = null;
        ContentValues initialValues = null;
        String strCondition = "";
        int updateStatus = -1;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            initialValues = new ContentValues();
            initialValues.put("Uploaded", 1);
            strCondition = "Id=" + attendanceId;
            updateStatus = db.update(Constants.ATTENDANCE_COLLEGE_TABLE, initialValues, strCondition, null);
        } catch (Exception e) {
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return updateStatus;
    }


    public int isBrokerDetailsAvailable() {
        SQLiteDatabase db = null;
        Cursor resData = null;
        int autoId = -1;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select AutoId from " + Constants.BROKER_DETAILS_TABLE, null);
            if (resData != null && resData.getCount() > 0) {
                while (resData.moveToNext()) {
                    autoId = resData.getInt(0);
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
        return autoId;
    }

    public int insertBrokerDetails(String tokenNo, String brokerIP, String brokerPort, String brokerUsername, String brokerPassword, String subTopic, String pubTopic, String strDate) {
        SQLiteDatabase db = null;
        int status = -1;
        try {
            ContentValues initialValues = new ContentValues();
            initialValues.put("TokenNo", tokenNo);
            initialValues.put("BrokerIP", brokerIP);
            initialValues.put("BrokerPort", brokerPort);
            initialValues.put("Username", brokerUsername);
            initialValues.put("Password", brokerPassword);
            initialValues.put("subTopic", subTopic);
            initialValues.put("pubTopic", pubTopic);
            initialValues.put("IsDeviceRegistered", "Y");
            initialValues.put("DateOfRegistration", strDate);
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            status = (int) db.insert(Constants.BROKER_DETAILS_TABLE, null, initialValues);
        } catch (SQLiteException e) {
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return status;
    }

    public int updateBrokerDetails(int id, String tokenNo, String brokerIP, String brokerPort, String brokerUsername, String brokerPassword, String subTopic, String pubTopic, String strDate) {
        SQLiteDatabase db = null;
        ContentValues initialValues = null;
        String strCondition = "";
        int updateStatus = -1;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            initialValues = new ContentValues();
            initialValues.put("TokenNo", tokenNo);
            initialValues.put("BrokerIP", brokerIP);
            initialValues.put("BrokerPort", brokerPort);
            initialValues.put("Username", brokerUsername);
            initialValues.put("Password", brokerPassword);
            initialValues.put("subTopic", subTopic);
            initialValues.put("pubTopic", pubTopic);
            initialValues.put("IsDeviceRegistered", "Y");
            initialValues.put("DateOfRegistration", strDate);
            strCondition = "AutoId=" + id;
            updateStatus = db.update(Constants.BROKER_DETAILS_TABLE, initialValues, strCondition, null);
        } catch (Exception e) {
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return updateStatus;
    }

    public ArrayList <String> getBrokerDetails(ArrayList <String> brokerDetailsList) {
        SQLiteDatabase db = null;
        Cursor resData = null;
        int autoId = -1;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select BrokerIP,BrokerPort,Username,Password,subTopic,pubTopic from " + Constants.BROKER_DETAILS_TABLE, null);
            if (resData != null && resData.getCount() > 0) {
                brokerDetailsList = new ArrayList <String>();
                while (resData.moveToNext()) {
                    brokerDetailsList.add(resData.getString(0));
                    brokerDetailsList.add(resData.getString(1));
                    brokerDetailsList.add(resData.getString(2));
                    brokerDetailsList.add(resData.getString(3));
                    brokerDetailsList.add(resData.getString(4));
                    brokerDetailsList.add(resData.getString(5));
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
        return brokerDetailsList;
    }

    public int insertHttpServerDetails(String name, String domain, String ip, String port) {
        SQLiteDatabase db = null;
        int status = -1;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("ddMMyyyyHHmmss");
        String date = mdformat.format(calendar.getTime());
        try {
            ContentValues initialValues = new ContentValues();
            initialValues.put("Name", name);
            initialValues.put("Domain", domain);
            initialValues.put("IPAddress", ip);
            initialValues.put("Port", port);
            initialValues.put("Date", date);
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            status = (int) db.insert(Constants.HTTP_SERVER_DETAILS, null, initialValues);
        } catch (SQLiteException e) {
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return status;
    }

    public int insertMqttDetails(String ip, String port, String username, String password) {
        SQLiteDatabase db = null;
        int status = -1;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("ddMMyyyyHHmmss");
        String date = mdformat.format(calendar.getTime());
        try {
            ContentValues initialValues = new ContentValues();
            initialValues.put("IPAddress", ip);
            initialValues.put("Port", port);
            initialValues.put("Username", username);
            initialValues.put("Password", password);
            initialValues.put("Date", date);
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            status = (int) db.insert(Constants.BROKER_DETAILS, null, initialValues);
        } catch (SQLiteException e) {
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return status;
    }

    public int insertProtocolDetails(String protocol, String isEnabled) {
        SQLiteDatabase db = null;
        int status = -1;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("ddMMyyyyHHmmss");
        String date = mdformat.format(calendar.getTime());
        try {
            ContentValues initialValues = new ContentValues();
            initialValues.put("ProtocolName", protocol);
            initialValues.put("IsEnabled", isEnabled);
            initialValues.put("Date", date);
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            status = (int) db.insert(Constants.PROTOCOL_DETAILS, null, initialValues);
        } catch (SQLiteException e) {
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return status;
    }

    public Cursor getProtocolDetails() {
        SQLiteDatabase db = null;
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select ProtocolName from " + Constants.PROTOCOL_DETAILS + " ORDER BY AutoId DESC LIMIT 1", null);
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

    public Cursor getHttpServerDetails() {
        SQLiteDatabase db = null;
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select Name,Domain,IPAddress,Port from " + Constants.HTTP_SERVER_DETAILS + " ORDER BY ID DESC LIMIT 1", null);
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

    public Cursor getMqttBrokerDetails() {
        SQLiteDatabase db = null;
        Cursor resData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select IPAddress,Port,Username,Password from " + Constants.BROKER_DETAILS + " ORDER BY ID DESC LIMIT 1", null);
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


    public int updateFingerDataToEmpTable(int autoId) {
        SQLiteDatabase db = null;
        int status = -1;
        try {
            ContentValues fingerDetails = new ContentValues();
            fingerDetails.put("EnrollStatus", "N");
            fingerDetails.put("NosFinger", "");
            fingerDetails.put("VerificationMode", "");
            fingerDetails.put("isTemplateAadhaarVerifiedYorN", "");
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            status = db.update(Constants.EMPLOYEE_TABLE, fingerDetails, "AutoId=" + autoId, null);
        } catch (Exception e) {
        }
        return status;
    }

    public String getVMByAutoId(int autoId) {
        SQLiteDatabase db = null;
        Cursor resData = null;
        String vm = "";
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select VerificationMode from " + Constants.EMPLOYEE_TABLE + " where  AutoId=" + autoId, null);
            if (resData != null) {
                if (resData.getCount() > 0) {
                    while (resData.moveToNext()) {
                        vm = resData.getString(0);
                    }
                }
            }
        } catch (Exception e) {
        } finally {
            if (db != null) {
                db.close();
            }
            if (resData != null) {
                resData.close();
            }
        }
        return vm;
    }


    public Cursor getSectorKeyForReadCard(int sectorNo) {
        SQLiteDatabase db = null;
        Cursor resSectorKeyData = null;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resSectorKeyData = db.rawQuery("select KeyB from " + Constants.SECTOR_KEY_TABLE + " where SectorNo='" + Integer.toString(sectorNo) + "'", null);
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

    public int getTotalLocalFingerTemplates() {
        SQLiteDatabase db = null;
        Cursor rs = null;
        int value = -1;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            rs = db.rawQuery("SELECT count(*) from " + Constants.FINGER_TABLE, null);
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

    public int getUnsendFingerRecords() {
        SQLiteDatabase db = null;
        Cursor rs = null;
        int value = -1;
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            rs = db.rawQuery("SELECT count(*) from " + Constants.FINGER_TABLE + " where IsUpdatedToServer='0'", null);
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


    public int insertGVM(String gvm) {
        SQLiteDatabase db = null;
        int status = -1;
        try {
            ContentValues initialValues = new ContentValues();
            initialValues.put("VerificationMode", gvm);
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            status = (int) db.insert(Constants.GVM_TABLE, null, initialValues);
        } catch (SQLiteException e) {
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return status;
    }


    public String getCurrentGVM() {
        SQLiteDatabase db = null;
        Cursor resData = null;
        String gvm = "";
        try {
            db = SQLiteDatabase.openDatabase(Constants.DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            resData = db.rawQuery("select VerificationMode from " + Constants.GVM_TABLE + " ORDER BY ID DESC LIMIT 1", null);
            if (resData != null) {
                if (resData.getCount() > 0) {
                    while (resData.moveToNext()) {
                        gvm = resData.getString(0);
                    }
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
        return gvm;
    }


}