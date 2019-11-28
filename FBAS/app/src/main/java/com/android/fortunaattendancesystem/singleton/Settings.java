package com.android.fortunaattendancesystem.singleton;

/**
 * Created by fortuna on 21/7/17.
 */
public class Settings {

    private int deviceTypeTypeValue = -1;
    private int srTypeValue = -1;
    private int frTypeValue = -1;
    private int fingerEnrollmentModeValue = -1;
    private int serverTypeValue = -1;
    private int employeeEnrollmentValue = -1;
    private int masterDateEntryValue = -1;
    private int pioValue = -1;
    private int excelImportExportVal = -1;
    private int appType=-1;
    private int appSubType=-1;
    private String attendanaceSIP = "";
    private String attendancePort = "";
    private String attendanceDomain = "";
    private String attendanceUrl = "";
    private String aadhaarSIP = "";
    private String aadhaarPort = "";
    private String aadhaarDomain = "";
    private String aadhaarUrl = "";

    private static Settings mInstance = null;

    public int getSrTypeValue() {
        return srTypeValue;
    }

    public void setSrTypeValue(int srTypeValue) {
        this.srTypeValue = srTypeValue;
    }

    public int getFrTypeValue() {
        return frTypeValue;
    }

    public void setFrTypeValue(int frTypeValue) {
        this.frTypeValue = frTypeValue;
    }

    public static Settings getInstance() {
        if (mInstance == null) {
            mInstance = new Settings();
            mInstance.reset();

        }
        return mInstance;
    }

    public int getDeviceTypeTypeValue() {
        return deviceTypeTypeValue;
    }

    public void setDeviceTypeTypeValue(int deviceTypeTypeValue) {
        this.deviceTypeTypeValue = deviceTypeTypeValue;
    }



    public int getFingerEnrollmentModeValue() {
        return fingerEnrollmentModeValue;
    }

    public void setFingerEnrollmentModeValue(int fingerEnrollmentModeValue) {
        this.fingerEnrollmentModeValue = fingerEnrollmentModeValue;
    }

    public int getServerTypeValue() {
        return serverTypeValue;
    }

    public void setServerTypeValue(int serverTypeValue) {
        this.serverTypeValue = serverTypeValue;
    }

    public int getEmployeeEnrollmentValue() {
        return employeeEnrollmentValue;
    }

    public void setEmployeeEnrollmentValue(int employeeEnrollmentValue) {
        this.employeeEnrollmentValue = employeeEnrollmentValue;
    }

    public int getMasterDateEntryValue() {
        return masterDateEntryValue;
    }

    public void setMasterDateEntryValue(int masterDateEntryValue) {
        this.masterDateEntryValue = masterDateEntryValue;
    }

    public int getPioValue() {
        return pioValue;
    }

    public void setPioValue(int pioValue) {
        this.pioValue = pioValue;
    }

    public int getExcelImportExportVal() {
        return excelImportExportVal;
    }

    public void setExcelImportExportVal(int excelImportExportVal) {
        this.excelImportExportVal = excelImportExportVal;
    }

    public int getAppType() {
        return appType;
    }

    public void setAppType(int appType) {
        this.appType = appType;
    }

    public int getAppSubType() {
        return appSubType;
    }

    public void setAppSubType(int appSubType) {
        this.appSubType = appSubType;
    }

    public String getAttendanaceSIP() {
        return attendanaceSIP;
    }

    public void setAttendanaceSIP(String attendanaceSIP) {
        this.attendanaceSIP = attendanaceSIP;
    }

    public String getAttendancePort() {
        return attendancePort;
    }

    public void setAttendancePort(String attendancePort) {
        this.attendancePort = attendancePort;
    }

    public String getAttendanceDomain() {
        return attendanceDomain;
    }

    public void setAttendanceDomain(String attendanceDomain) {
        this.attendanceDomain = attendanceDomain;
    }

    public String getAttendanceUrl() {
        return attendanceUrl;
    }

    public void setAttendanceUrl(String attendanceUrl) {
        this.attendanceUrl = attendanceUrl;
    }

    public String getAadhaarSIP() {
        return aadhaarSIP;
    }

    public void setAadhaarSIP(String aadhaarSIP) {
        this.aadhaarSIP = aadhaarSIP;
    }

    public String getAadhaarPort() {
        return aadhaarPort;
    }

    public void setAadhaarPort(String aadhaarPort) {
        this.aadhaarPort = aadhaarPort;
    }

    public String getAadhaarDomain() {
        return aadhaarDomain;
    }

    public void setAadhaarDomain(String aadhaarDomain) {
        this.aadhaarDomain = aadhaarDomain;
    }

    public String getAadhaarUrl() {
        return aadhaarUrl;
    }

    public void setAadhaarUrl(String aadhaarUrl) {
        this.aadhaarUrl = aadhaarUrl;
    }

    private void reset() {
        deviceTypeTypeValue = -1;
        srTypeValue = -1;
        frTypeValue = -1;
        fingerEnrollmentModeValue = -1;
        serverTypeValue = -1;
        employeeEnrollmentValue = -1;
        masterDateEntryValue = -1;
        pioValue = -1;
        excelImportExportVal = -1;
        attendanaceSIP = "";
        attendancePort = "";
        attendanceDomain = "";
        attendanceUrl = "";
        aadhaarSIP = "";
        aadhaarPort = "";
        aadhaarDomain = "";
        aadhaarUrl = "";
    }



}
