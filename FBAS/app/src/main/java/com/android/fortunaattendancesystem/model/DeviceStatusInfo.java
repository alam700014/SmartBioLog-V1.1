package com.android.fortunaattendancesystem.model;

/**
 * Created by fortuna on 3/11/18.
 */

public class DeviceStatusInfo {

    private String corporateId;
    private String imei;
    private String pid;
    private String commandType;
    private String deviceAdd;
    private String gvm;
    private String deviceToken;
    private String isSmartReaderInstalled;
    private String totalEnrolledUsers;
    private String estdCode;
    private String firmware;
    private String firmwareId;
    private String gprsOperator;
    private String gprsSignal;
    private String ipAddress;
    private String simNo;
    private String timeZone;
    private String totalTemplate;
    private String totalUser;
    private String unCapRecord;


    public String getCorporateId() {
        return corporateId;
    }

    public void setCorporateId(String corporateId) {
        this.corporateId = corporateId;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }

    public String getDeviceAdd() {
        return deviceAdd;
    }

    public void setDeviceAdd(String deviceAdd) {
        this.deviceAdd = deviceAdd;
    }

    public String getGvm() {
        return gvm;
    }

    public void setGvm(String gvm) {
        this.gvm = gvm;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public String getIsSmartReaderInstalled() {
        return isSmartReaderInstalled;
    }

    public void setIsSmartReaderInstalled(String isSmartReaderInstalled) {
        this.isSmartReaderInstalled = isSmartReaderInstalled;
    }

    public String getTotalEnrolledUsers() {
        return totalEnrolledUsers;
    }

    public void setTotalEnrolledUsers(String totalEnrolledUsers) {
        this.totalEnrolledUsers = totalEnrolledUsers;
    }

    public String getEstdCode() {
        return estdCode;
    }

    public void setEstdCode(String estdCode) {
        this.estdCode = estdCode;
    }

    public String getFirmware() {
        return firmware;
    }

    public void setFirmware(String firmware) {
        this.firmware = firmware;
    }

    public String getFirmwareId() {
        return firmwareId;
    }

    public void setFirmwareId(String firmwareId) {
        this.firmwareId = firmwareId;
    }

    public String getGprsOperator() {
        return gprsOperator;
    }

    public void setGprsOperator(String gprsOperator) {
        this.gprsOperator = gprsOperator;
    }

    public String getGprsSignal() {
        return gprsSignal;
    }

    public void setGprsSignal(String gprsSignal) {
        this.gprsSignal = gprsSignal;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getSimNo() {
        return simNo;
    }

    public void setSimNo(String simNo) {
        this.simNo = simNo;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public String getTotalTemplate() {
        return totalTemplate;
    }

    public void setTotalTemplate(String totalTemplate) {
        this.totalTemplate = totalTemplate;
    }

    public String getTotalUser() {
        return totalUser;
    }

    public void setTotalUser(String totalUser) {
        this.totalUser = totalUser;
    }

    public String getUnCapRecord() {
        return unCapRecord;
    }

    public void setUnCapRecord(String unCapRecord) {
        this.unCapRecord = unCapRecord;
    }
}

//    deviceStatusJson.put("COID",CORPORATE_ID);
//            deviceStatusJson.put("CPUID",imei);
//            deviceStatusJson.put("PID",pid);
//            deviceStatusJson.put("CommandType",POST_DEVICE_STATUS_COMM);
//            deviceStatusJson.put("DeviceAdd","01");//temporary value
//            deviceStatusJson.put("GVM","02");//1:N
//            deviceStatusJson.put("DeviceToken",device_token);
//
//    isReaderPresent =dbLayer.isSmartCardReaderInstalled();
//            if(isReaderPresent)
//
//    {
//        deviceStatusJson.put("SmartCard", "1");
//    } else
//
//    {
//        deviceStatusJson.put("SmartCard", "0");
//    }
//
//    totalFingerEnrolledUsers =dbLayer.getTotalFingerEnrolledUser();
//            if(totalFingerEnrolledUsers !=-1)
//
//    {
//        deviceStatusJson.put("EnrolledUser", Integer.toString(totalFingerEnrolledUsers));
//    } else
//
//    {
//        deviceStatusJson.put("EnrolledUser", 0);
//    }
//
//            deviceStatusJson.put("EstdCode","00000001");//test val
//            deviceStatusJson.put("Firmware","V040626");//test val
//            deviceStatusJson.put("FirmwareID","MBBV3-H27-I10411241E");//test val
//
//            deviceStatusJson.put("GPRSOperator","AIRTEL");//test val
//            deviceStatusJson.put("GPRSSignal","93");//test val
//
//    IPAddress =Utility.getDeviceIPAddress();
//            if(IPAddress !=null&&IPAddress.trim().
//
//    length() >0)
//
//    {
//        deviceStatusJson.put("IPAddress", IPAddress);//test val
//    } else
//
//    {
//        deviceStatusJson.put("IPAddress", IPAddress);//test val
//    }
//
//
//            deviceStatusJson.put("SIMNo","9563987634");//test val
//            deviceStatusJson.put("TimeZone","+0530");//test val
//
//    morphoDevice =ProcessInfo.getInstance().
//
//    getMorphoDevice();
//
//    morphoDatabase =ProcessInfo.getInstance().
//
//    getMorphoDatabase();
//            if(morphoDevice !=null&&morphoDatabase !=null)
//
//    {
//        Long l = new Long(0);
//        int ret = morphoDatabase.getNbUsedRecord(l);
//        if (ret == 0) {
//            deviceStatusJson.put("TotalTemplate", Long.toString(l));
//        } else {
//            deviceStatusJson.put("TotalTemplate", "0");
//        }
//    } else
//
//    {
//        deviceStatusJson.put("TotalTemplate", "0");
//    }
//
//    totalEnrolledUsers =dbLayer.getTotalEnrolledUsers();
//            if(totalEnrolledUsers !=-1)
//
//    {
//        deviceStatusJson.put("TotalUser", Integer.toString(totalEnrolledUsers));
//    } else
//
//    {
//        deviceStatusJson.put("TotalUser", "0");
//    }
//
//    totalUnSendAttendanceRecords =dbLayer.getTotalUnSendRecords();
//            if(totalUnSendAttendanceRecords !=-1)
//
//    {
//        deviceStatusJson.put("UncapRec", Integer.toString(totalUnSendAttendanceRecords));//test val
//    } else
//
//    {
//        deviceStatusJson.put("UncapRec", "0");//test val
//
//
//    }
