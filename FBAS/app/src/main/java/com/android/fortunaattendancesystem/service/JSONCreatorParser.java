package com.android.fortunaattendancesystem.service;

import android.util.Log;

import com.android.fortunaattendancesystem.activities.EmployeeAttendanceActivity;
import com.android.fortunaattendancesystem.constant.Constants;
import com.android.fortunaattendancesystem.helper.Utility;
import com.android.fortunaattendancesystem.info.ProcessInfo;
import com.android.fortunaattendancesystem.model.AttendanceInfo;
import com.android.fortunaattendancesystem.model.CollegeAttendanceInfo;
import com.android.fortunaattendancesystem.model.ContractorInfo;
import com.android.fortunaattendancesystem.model.DeviceStatusInfo;
import com.android.fortunaattendancesystem.model.EmpValidationDownloadInfo;
import com.android.fortunaattendancesystem.model.EmployeeTypeInfo;
import com.android.fortunaattendancesystem.model.EmployeeValidationBasicInfo;
import com.android.fortunaattendancesystem.model.EmployeeValidationFingerInfo;
import com.android.fortunaattendancesystem.model.MQTTHeaderInfo;
import com.android.fortunaattendancesystem.model.PeriodInfo;
import com.android.fortunaattendancesystem.model.ProfessorStudentSubjectInfo;
import com.android.fortunaattendancesystem.model.ProfessorSubjectInfo;
import com.android.fortunaattendancesystem.model.RemoteEnrollmentInfo;
import com.android.fortunaattendancesystem.model.SignOnMessageInfo;
import com.android.fortunaattendancesystem.model.SubjectInfo;
import com.android.fortunaattendancesystem.model.TemplateDownloadInfo;
import com.android.fortunaattendancesystem.model.TemplateUploadInfo;
import com.android.fortunaattendancesystem.model.TemplateUploadTypeInfo;
import com.android.fortunaattendancesystem.singleton.UserDetails;
import com.android.fortunaattendancesystem.submodules.SQLiteCommunicator;
import com.morpho.morphosmart.sdk.MorphoDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by fortuna on 31/10/18.
 */

public class JSONCreatorParser {


    public static String getDeviceRegistrationJsonData(String imei, String pid, String strCOID, String strCT) {

        String reqJson = "";
        JSONObject devReg = new JSONObject();
        try {
            devReg.put("PID", pid);
            devReg.put("CPUID", imei);
            devReg.put("COID", strCOID);
            devReg.put("CT", strCT); //1000
            devReg.put("DeviceCode", "50");
            devReg.put("DevType", "54");
            devReg.put("TT", "AN");
            devReg.put("Tech Type", "A");   /*A->Finger, B->Face*/
            devReg.put("ST", "B");          /* B->Morpho*/
            devReg.put("Temp Type", "I");
            devReg.put("CMT", "G");
            reqJson = devReg.toString();
        } catch (JSONException e) {
        }
        return reqJson;
    }

    public static String getAttendanceJsonData(String strPacketId, String strIMEI, String strCorporateId, String strDeviceToken, String strCommandType, AttendanceInfo attInfo) {

        String reqJson = "";
        JSONObject jAttendance = new JSONObject();
        JSONObject jAttendanceDatas = new JSONObject();
        JSONArray jAttendanceDataArray = new JSONArray();
        JSONArray jPhoto = new JSONArray();

        try {
            jAttendance.put("COID", strCorporateId);
            jAttendance.put("PID", strPacketId);
            jAttendance.put("CPUID", strIMEI);
            jAttendance.put("DeviceToken", strDeviceToken);
            jAttendance.put("CT", strCommandType);
            jAttendance.put("OR", "D");
            jAttendance.put("PC", "1");
            jAttendance.put("TA", "01");

            jPhoto.put(attInfo.getImageBase64());

            String Dateformat = Utility.DateFormatChange(attInfo.getPunchDate());
            String strDateTime = Dateformat + " " + attInfo.getPunchTime();

            jAttendanceDatas.put("ContentLength", Integer.toString(attInfo.getImageLen()));
            // jAttendanceDatas.put("EmpID", "960052");//for testing purpose empid 960052 exists on server
            jAttendanceDatas.put("EmpID", attInfo.getEmpId());
            jAttendanceDatas.put("attendancedate", Dateformat);
            jAttendanceDatas.put("COID", strCorporateId);
            jAttendanceDatas.put("fileExtension", "PNG");
            jAttendanceDatas.put("punchdatetime", strDateTime);
            jAttendanceDatas.put("latlong", attInfo.getLatLong());
            jAttendanceDatas.put("pictureBinary", jPhoto);
            jAttendanceDatas.put("reason", "test attendance");

            jAttendanceDataArray.put(jAttendanceDatas);
            jAttendance.put("AD", jAttendanceDataArray);

            reqJson = jAttendance.toString();

        } catch (JSONException e) {
        }
        return reqJson;
    }

    public static String getEmpValUploadJsonData(String pid, String imei, String empValidationJobUploadComm, EmpValidationDownloadInfo empInfo) {

        String resJson = "";

        JSONObject finalJson = new JSONObject();
        JSONArray customJobModeList = new JSONArray();
        JSONObject outerJson = new JSONObject();
        JSONObject innerJson = new JSONObject();

        try {
            innerJson.put("EID", empInfo.getEmpId());
            innerJson.put("CID", empInfo.getCardId());
            innerJson.put("EN", empInfo.getEmpName());
            innerJson.put("BG", empInfo.getBloodGrp());
            innerJson.put("SC", empInfo.getSiteCode());
            innerJson.put("MN", empInfo.getMobileNo());
            innerJson.put("EMID", empInfo.getEmailId());
            innerJson.put("PIN", empInfo.getPin());
            innerJson.put("DOB", empInfo.getDob());
            innerJson.put("DOV", empInfo.getDov());
            innerJson.put("IsBlackListed", empInfo.getIsBlackListed());
            innerJson.put("IsLockOpen", empInfo.getIsLockOpen());

            innerJson.put("AID", "");
            innerJson.put("PPL", "");
            innerJson.put("PP", "");

            outerJson.put("CPUID", imei);
            outerJson.put("Command", empValidationJobUploadComm);
            outerJson.put("CommandNo", "0");
            outerJson.put("CreatedOn", "");
            outerJson.put("PID", pid);
            outerJson.put("JobID", pid);

            outerJson.put("CommandData", innerJson.toString());
            customJobModeList.put(0, outerJson);
            finalJson.put("CustomJobModellist", customJobModeList);
            finalJson.put("ResponseMessage", "");
            resJson = finalJson.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resJson;
    }


    public static ArrayList <EmpValidationDownloadInfo> parseEmpValidationDownloadJson(String json, ArrayList <EmpValidationDownloadInfo> empInfoList) {
        try {
            JSONObject jsonReader = new JSONObject(json);
            JSONArray jsonArray = jsonReader.getJSONArray("CustomJobModellist");
            if (jsonArray != null) {
                int len = jsonArray.length();
                if (len > 0) {
                    empInfoList = new ArrayList <EmpValidationDownloadInfo>();
                    for (int n = 0; n < len; n++) {
                        EmpValidationDownloadInfo empInfo = new EmpValidationDownloadInfo();
                        JSONObject object = jsonArray.getJSONObject(n);
                        String command = object.getString("Command");
                        String strCommandDatas = object.getString("CommandData");
                        JSONObject jsonCommandData = new JSONObject(strCommandDatas);

                        String empId = jsonCommandData.optString("EID").toString();
                        String empName = jsonCommandData.optString("EN").toString();
                        String cardId = jsonCommandData.optString("CID").toString();
                        String maildId = jsonCommandData.optString("EMID").toString();
                        String pin = jsonCommandData.optString("PIN").toString();
                        String dob = jsonCommandData.optString("DOB").toString();
                        String dov = jsonCommandData.optString("DOV").toString();
                        String bloodGrp = jsonCommandData.optString("BG").toString();
                        String mobileNo = jsonCommandData.optString("MN").toString();
                        String isLockOpen = jsonCommandData.optString("IsLockOpen").toString();
                        String isBlackListed = jsonCommandData.optString("IsBlackListed").toString();
                        String siteCode = jsonCommandData.optString("SC");
                        String ppl = jsonCommandData.optString("PPL");
                        String pp = jsonCommandData.optString("PP");

                        String jobId = object.optString("JobID").toString();
                        String pid = object.optString("PID").toString();


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
                            String strBloodGroup = Utility.getBloodGrValByNumber(bloodGrp);
                            if (strBloodGroup.trim().length() > 0) {
                                empInfo.setBloodGrp(strBloodGroup);
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

                        if (ppl != null && ppl.trim().length() > 0) {
                            empInfo.setPpl(ppl);
                        } else {
                            empInfo.setPpl("");
                        }

                        if (pp != null && pp.trim().length() > 0) {
                            empInfo.setPp(pp);
                        } else {
                            empInfo.setPp("");
                        }

                        if (jobId != null && jobId.trim().length() > 0) {
                            empInfo.setJobId(jobId);
                        } else {
                            empInfo.setJobId("");
                        }

                        if (pid != null && pid.trim().length() > 0) {
                            empInfo.setPid(pid);
                        } else {
                            empInfo.setPid("");
                        }

                        empInfo.setEnrollSource("R");
                        empInfo.setJobCode(command);

                        empInfoList.add(empInfo);
                    }
                }
            }

        } catch (JSONException e) {
            Log.d("TEST", "Exception:" + e.getMessage());
        }
        return empInfoList;
    }


    public static String getPostJobJson(String corporateId, String imei, String deviceToken, String strJobId, String statusMsg) {
        String postJobJson = "";
        JSONArray arr = new JSONArray();
        JSONObject arrObj = new JSONObject();
        JSONObject obj = new JSONObject();
        try {
            obj.put("COID", corporateId);
            obj.put("CPUID", imei);
            obj.put("DeviceToken", deviceToken);
            arrObj.put("JobID", strJobId);
            arrObj.put("StatusMsg", statusMsg);
            arr.put(arrObj);
            obj.put("JobStatusList", arr);
            postJobJson = obj.toString();
        } catch (Exception e) {
        }
        return postJobJson;
    }

    public static ArrayList <TemplateUploadTypeInfo> parseTemplateUploadJson(String response, ArrayList <TemplateUploadTypeInfo> templateUploadInfoList) {
        try {
            JSONObject jsonReader = new JSONObject(response);
            JSONArray jsonArray = jsonReader.getJSONArray("CustomJobModellist");
            if (jsonArray != null) {
                int len = jsonArray.length();
                if (len > 0) {
                    templateUploadInfoList = new ArrayList <TemplateUploadTypeInfo>();
                    for (int n = 0; n < len; n++) {
                        TemplateUploadTypeInfo templateUploadInfo = new TemplateUploadTypeInfo();
                        JSONObject object = jsonArray.getJSONObject(n);
                        String strCommandDatas = object.getString("CommandData");
                        String jobId = object.getString("JobID");

                        Random random = new Random();
                        String packetId = String.format("%04d", random.nextInt(10000));

                        // String packetId = object.getString("PID");

                        JSONObject jsonCommandData = new JSONObject(strCommandDatas);
                        String strUploadCmd = jsonCommandData.optString("UploadCmd").toString();
                        JSONObject jsonUploadCmd = new JSONObject(strUploadCmd);
                        String uploadType = jsonUploadCmd.optString("UploadType").toString();

                        String empId = jsonCommandData.optString("EID").toString();
                        String cardId = jsonCommandData.optString("CID").toString();
                        String empName = jsonCommandData.optString("EN").toString();

                        templateUploadInfo.setJobId(jobId);
                        templateUploadInfo.setPacketId(packetId);
                        templateUploadInfo.setUploadType(uploadType);
                        templateUploadInfo.setEmpId(empId);
                        templateUploadInfo.setCardId(cardId);
                        templateUploadInfo.setEmpName(empName);
                        templateUploadInfoList.add(templateUploadInfo);
                    }
                }
            }
        } catch (JSONException je) {
        }
        return templateUploadInfoList;
    }

    public static HashMap <Integer, String> createTemplateUploadJson(String imei, String deviceToken, String tempUploadCommand, String corporateId, String packetId, ArrayList <TemplateUploadInfo> templateUploadInfoList, HashMap <Integer, String> fingerIdFingerDataMap) {

        String fingerDataJson = "";
        JSONObject templateUploadJson = new JSONObject();
        JSONObject FD = new JSONObject();
        try {
            if (templateUploadInfoList != null) {
                int size = templateUploadInfoList.size();
                if (size > 0) {
                    fingerIdFingerDataMap = new HashMap <Integer, String>();
                    for (int i = 0; i < size; i++) {
                        TemplateUploadInfo templateInfo = templateUploadInfoList.get(i);
                        if (templateInfo != null) {
                            templateUploadJson.put("COID", corporateId);
                            templateUploadJson.put("CPUID", imei);
                            templateUploadJson.put("CT", tempUploadCommand);
                            templateUploadJson.put("DeviceToken", deviceToken);
                            templateUploadJson.put("OR", "D");
                            templateUploadJson.put("PC", "1");
                            templateUploadJson.put("PID", packetId);
                            templateUploadJson.put("TA", "01");
                            templateUploadJson.put("UID", Integer.toString(UserDetails.getInstance().getLoginId()));

                            templateUploadJson.put("EID", templateInfo.getEmpId());
                            templateUploadJson.put("CID", templateInfo.getCardId());
                            templateUploadJson.put("EN", templateInfo.getEmpName());
                            templateUploadJson.put("Pin", templateInfo.getCardId().substring(4));
                            templateUploadJson.put("DTOE", templateInfo.getDtoe());

                            int val = -1;
                            String value = templateInfo.getTemplateSrNo();
                            if (value != null && value.length() > 0) {
                                int tempSerialNo = Integer.parseInt(value);
                                if (tempSerialNo == 1) {
                                    FD.put("FT", "F1");
                                } else if (tempSerialNo == 2) {
                                    FD.put("FT", "F2");
                                }
                            } else {
                                FD.put("FT", "");
                            }

                            value = templateInfo.getFingerIndex();
                            if (value != null && value.length() > 0) {
                                val = Utility.getFingerIndexValByName(value);
                                if (val != -1) {
                                    FD.put("FI", Integer.toHexString(val).toUpperCase());
                                }
                            } else {
                                FD.put("FI", "");
                            }

                            value = templateInfo.getSecurityLevel();
                            if (value != null && value.length() > 0) {
                                val = Utility.getSecurityLvlValByName(value);
                                if (val != -1) {
                                    FD.put("SL", Integer.toString(val));
                                }
                            } else {
                                FD.put("SL", "");
                            }

                            value = templateInfo.getFingerQuality();
                            if (value != null && value.length() > 0) {
                                val = Utility.getFingerQualityValByName(value);
                                if (val != -1) {
                                    FD.put("FQ", Integer.toString(val));
                                }
                            } else {
                                FD.put("FQ", "A");
                            }

                            value = templateInfo.getFmd();
                            if (value != null && value.length() > 0) {
                                String t_size = Integer.toString(value.length()); //Sanjay
                                FD.put("TS", t_size);
                            } else {
                                FD.put("TS", "");
                            }

                            if (value != null && value.length() > 0) {
                                FD.put("FMID", value);
                            } else {
                                FD.put("FMID", "");
                            }

                            byte[] fid = templateInfo.getFid();
                            if (fid != null && fid.length > 0) {
                                //String strFID = encoder.encode(fid);
                                //FD.put("FID", strFID);
                                /* Fot HTTP Error Code 413 Request Data Too Large Send Blank FID */
                                FD.put("FID", "");

                            } else {
                                FD.put("FID", "");
                            }

                            value = templateInfo.getVerificationMode();
                            if (value != null && value.trim().length() > 0) {
                                val = Utility.getVerificationModeValByName(value);
                                if (val != -1) {
                                    templateUploadJson.put("VM", Integer.toString(val));
                                }
                            } else {
                                templateUploadJson.put("VM", "");
                            }

                            templateUploadJson.put("FD", FD);
                            fingerDataJson = templateUploadJson.toString();
                            fingerIdFingerDataMap.put(Integer.valueOf(templateInfo.getFingerId()), fingerDataJson);
                        }
                    }
                }
            }
        } catch (JSONException je) {
        }
        return fingerIdFingerDataMap;
    }

    public static ArrayList <TemplateDownloadInfo> parseTemplateDownloadJson(String response, ArrayList <TemplateDownloadInfo> tempDownloadInfoList) {
        try {
            JSONObject jsonReader = new JSONObject(response);
            JSONArray jsonArray = jsonReader.getJSONArray("CustomJobModellist");
            if (jsonArray != null) {
                int len = jsonArray.length();
                if (len > 0) {
                    tempDownloadInfoList = new ArrayList <TemplateDownloadInfo>();
                    for (int count = 0; count < len; count++) {
                        TemplateDownloadInfo templateDownloadInfo = new TemplateDownloadInfo();
                        JSONObject detailJson = (JSONObject) jsonArray.get(count);
                        String empDetails = detailJson.get("CommandData").toString();
                        String jobId = detailJson.get("JobID").toString();
                        String pid = detailJson.get("PID").toString();

                        JSONObject empDetailsJson = new JSONObject(empDetails);
                        String empId = empDetailsJson.getString("EID");
                        String cardId = empDetailsJson.getString("CID");
                        String empName = empDetailsJson.getString("EN");
                        String dtoe = empDetailsJson.getString("DTOE");
                        String pin = empDetailsJson.optString("PIN").toString();

                        String emailId = "";
                        String dob = "";
                        String dov = "";
                        String bloodGrp = "";
                        String siteCode = "";
                        String enrollStatus = "";
                        String enrollNoOfFingers = "";
                        String mobileNo = "";
                        String isLockOpen = "";
                        String isBlackListed = "";

                        String fingerDetails = empDetailsJson.get("FD").toString();
                        JSONObject fingerDetailsJson = new JSONObject(fingerDetails);

                        String fingerType = fingerDetailsJson.getString("FT");
                        String securityLevel = fingerDetailsJson.getString("SL");
                        String fingerIndex = fingerDetailsJson.getString("FI");
                        String fingerQuality = fingerDetailsJson.getString("FQ");
                        String fmd = fingerDetailsJson.getString("FMID");
                        String fid = fingerDetailsJson.getString("FID");
                        String vm = empDetailsJson.getString("VM");

                        if (jobId != null && jobId.trim().length() > 0) {
                            templateDownloadInfo.setJobId(jobId);
                        } else {
                            templateDownloadInfo.setJobId("");
                        }

                        if (pid != null && pid.trim().length() > 0) {
                            templateDownloadInfo.setPacketId(pid);
                        } else {
                            templateDownloadInfo.setPacketId("");
                        }

                        if (empId != null && empId.trim().length() > 0) {
                            templateDownloadInfo.setEmpId(empId);
                        } else {
                            templateDownloadInfo.setEmpId("");
                        }

                        if (cardId != null && cardId.trim().length() > 0) {
                            templateDownloadInfo.setCardId(cardId);
                        } else {
                            templateDownloadInfo.setCardId("");
                        }

                        if (empName != null && empName.trim().length() > 0) {
                            templateDownloadInfo.setEmpName(empName);
                        } else {
                            templateDownloadInfo.setEmpName("");
                        }

                        if (dtoe != null && dtoe.trim().length() > 0) {
                            templateDownloadInfo.setDtoe(dtoe);
                        } else {
                            templateDownloadInfo.setDtoe("");
                        }

                        if (pin != null && pin.trim().length() > 0) {
                            templateDownloadInfo.setPin(pin);
                        } else {
                            templateDownloadInfo.setPin("");
                        }

                        if (emailId != null && emailId.trim().length() > 0) {
                            templateDownloadInfo.setEmailId(emailId);
                        } else {
                            templateDownloadInfo.setEmailId("");
                        }

                        if (dob != null && dob.trim().length() > 0) {
                            templateDownloadInfo.setDob(dob);
                        } else {
                            templateDownloadInfo.setDob("");
                        }

                        if (dov != null && dov.trim().length() > 0) {
                            templateDownloadInfo.setDov(dov);
                        } else {
                            templateDownloadInfo.setDov("");
                        }

                        if (bloodGrp != null && bloodGrp.trim().length() > 0) {
                            templateDownloadInfo.setBloodGrp(bloodGrp);
                        } else {
                            templateDownloadInfo.setBloodGrp("");
                        }

                        if (siteCode != null && siteCode.trim().length() > 0) {
                            templateDownloadInfo.setSiteCode(siteCode);
                        } else {
                            templateDownloadInfo.setSiteCode("");
                        }

                        if (enrollStatus != null && enrollStatus.trim().length() > 0) {
                            templateDownloadInfo.setEnrollStatus(enrollStatus);
                        } else {
                            templateDownloadInfo.setEnrollStatus("");
                        }

                        if (enrollNoOfFingers != null && enrollNoOfFingers.trim().length() > 0) {
                            templateDownloadInfo.setEnrollNoOfFingers(enrollNoOfFingers);
                        } else {
                            templateDownloadInfo.setEnrollNoOfFingers("");
                        }

                        if (mobileNo != null && mobileNo.trim().length() > 0) {
                            templateDownloadInfo.setMobileNo(mobileNo);
                        } else {
                            templateDownloadInfo.setMobileNo("");
                        }

                        if (isLockOpen != null && isLockOpen.trim().length() > 0) {
                            templateDownloadInfo.setIsLockOpen(isLockOpen);
                        } else {
                            templateDownloadInfo.setIsLockOpen("");
                        }

                        if (isBlackListed != null && isBlackListed.trim().length() > 0) {
                            templateDownloadInfo.setIsBlackListed(isBlackListed);
                        } else {
                            templateDownloadInfo.setIsBlackListed("");
                        }

                        if (fingerType != null && fingerType.trim().length() > 0) {
                            templateDownloadInfo.setFingerType(fingerType);
                        } else {
                            templateDownloadInfo.setFingerType("");
                        }

                        if (securityLevel != null && securityLevel.trim().length() > 0) {
                            templateDownloadInfo.setSecurityLevel(securityLevel);
                        } else {
                            templateDownloadInfo.setSecurityLevel("");
                        }

                        if (fingerIndex != null && fingerIndex.trim().length() > 0) {
                            templateDownloadInfo.setFingerIndex(fingerIndex);
                        } else {
                            templateDownloadInfo.setFingerIndex("");
                        }

                        if (fingerQuality != null && fingerQuality.trim().length() > 0) {
                            templateDownloadInfo.setFingerQuality(fingerQuality);
                        } else {
                            templateDownloadInfo.setFingerQuality("");
                        }

                        if (fmd != null && fmd.trim().length() > 0) {
                            templateDownloadInfo.setFmd(fmd);
                        } else {
                            templateDownloadInfo.setFmd("");
                        }

                        if (fid != null && fid.trim().length() > 0) {
                            templateDownloadInfo.setFid(fid);
                        } else {
                            templateDownloadInfo.setFid("");
                        }

                        if (vm != null && vm.trim().length() > 0) {
                            templateDownloadInfo.setVerificationMode(vm);
                        } else {
                            templateDownloadInfo.setVerificationMode("");
                        }

                        templateDownloadInfo.setCommand(Constants.TEMPLATE_DOWNLOAD_JOB_COMM);
                        tempDownloadInfoList.add(templateDownloadInfo);
                    }
                }
            }
        } catch (Exception e) {
            Log.d("TEST", "Exception:" + e.getMessage());
        }
        return tempDownloadInfoList;
    }

    public static RemoteEnrollmentInfo parseRemoteEnrollData(String response, RemoteEnrollmentInfo remoteEnrollInfo) {
        JSONObject responseJson = null;
        try {
            responseJson = new JSONObject(response);
            if (responseJson != null) {
                JSONArray resJsonArr = responseJson.getJSONArray("CustomJobModellist");
                if (resJsonArr != null) {
                    int len = resJsonArr.length();
                    if (len > 0) {
                        remoteEnrollInfo = new RemoteEnrollmentInfo();
                        for (int count = 0; count < len; count++) {
                            JSONObject detailJson = (JSONObject) resJsonArr.get(count);
                            String commandDetails = detailJson.getString("CommandData");
                            String jobId = detailJson.getString("JobID");
                            JSONObject empDetails = new JSONObject(commandDetails);
                            String enrollmentType = empDetails.getString("ET");

                            String empId = empDetails.getString("EID");
                            String cardId = empDetails.getString("CID");
                            String empName = empDetails.getString("EN");
                            String dtoe = empDetails.getString("DTOE");
                            String fingerType = empDetails.getString("FT");
                            String fingerIndex = empDetails.getString("FI");
                            String fingerQuality = empDetails.getString("AQ");
                            String securityLevel = empDetails.getString("SL");
                            String verificationMode = empDetails.getString("VM");

                            String packetId = empDetails.getString("PID");

                            if (jobId != null && jobId.trim().length() > 0) {
                                remoteEnrollInfo.setJobId(jobId);
                            } else {
                                remoteEnrollInfo.setJobId("");
                            }

                            if (packetId != null && packetId.trim().length() > 0) {
                                remoteEnrollInfo.setPacketId(packetId);
                            } else {
                                remoteEnrollInfo.setPacketId("");
                            }

                            if (enrollmentType != null && enrollmentType.trim().length() > 0) {
                                remoteEnrollInfo.setEnrollmentType(enrollmentType);
                            } else {
                                remoteEnrollInfo.setEnrollmentType("");
                            }

                            if (empId != null && empId.trim().length() > 0) {
                                remoteEnrollInfo.setEmpId(empId);
                            } else {
                                remoteEnrollInfo.setEmpId("");
                            }

                            if (cardId != null && cardId.trim().length() > 0) {
                                remoteEnrollInfo.setCardId(cardId);
                            } else {
                                remoteEnrollInfo.setCardId("");
                            }

                            if (empName != null && empName.trim().length() > 0) {
                                remoteEnrollInfo.setEmpName(empName);
                            } else {
                                remoteEnrollInfo.setEmpName("");
                            }

                            if (dtoe != null && dtoe.trim().length() > 0) {
                                remoteEnrollInfo.setDtoe(dtoe);
                            } else {
                                remoteEnrollInfo.setDtoe("");
                            }

                            if (fingerType != null && fingerType.trim().length() > 0) {
                                remoteEnrollInfo.setFingerType(fingerType);
                            } else {
                                remoteEnrollInfo.setFingerType("");
                            }

                            if (fingerIndex != null && fingerIndex.trim().length() > 0) {
                                remoteEnrollInfo.setFingerIndex(fingerIndex);
                            } else {
                                remoteEnrollInfo.setFingerIndex("");
                            }

                            if (fingerQuality != null && fingerQuality.trim().length() > 0) {
                                remoteEnrollInfo.setFingerQuality(fingerQuality);
                            } else {
                                remoteEnrollInfo.setFingerQuality("");
                            }

                            if (securityLevel != null && securityLevel.trim().length() > 0) {
                                remoteEnrollInfo.setSecurityLevel(securityLevel);
                            } else {
                                remoteEnrollInfo.setSecurityLevel("");
                            }

                            if (verificationMode != null && verificationMode.trim().length() > 0) {
                                remoteEnrollInfo.setVerificationMode(verificationMode);
                            } else {
                                remoteEnrollInfo.setVerificationMode("");
                            }
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return remoteEnrollInfo;
    }

    public static String createDeviceStatusJson(DeviceStatusInfo deviceStatusInfo) {
        String strJson = "";
        JSONObject deviceStatusJson = new JSONObject();
        try {
            deviceStatusJson.put("COID", deviceStatusInfo.getCorporateId());
            deviceStatusJson.put("CPUID", deviceStatusInfo.getImei());
            deviceStatusJson.put("PID", deviceStatusInfo.getPid());
            deviceStatusJson.put("CommandType", deviceStatusInfo.getCommandType());
            deviceStatusJson.put("DeviceAdd", deviceStatusInfo.getDeviceAdd());//temporary value
            deviceStatusJson.put("GVM", deviceStatusInfo.getGvm());//1:N
            deviceStatusJson.put("DeviceToken", deviceStatusInfo.getDeviceToken());
            deviceStatusJson.put("SmartCard", deviceStatusInfo.getIsSmartReaderInstalled());
            deviceStatusJson.put("EnrolledUser", deviceStatusInfo.getTotalEnrolledUsers());
            deviceStatusJson.put("EstdCode", deviceStatusInfo.getEstdCode());//test val
            deviceStatusJson.put("Firmware", deviceStatusInfo.getFirmware());//test val
            deviceStatusJson.put("FirmwareID", deviceStatusInfo.getFirmwareId());//test val
            deviceStatusJson.put("GPRSOperator", deviceStatusInfo.getGprsOperator());//test val
            deviceStatusJson.put("GPRSSignal", deviceStatusInfo.getGprsSignal());//test val
            deviceStatusJson.put("IPAddress", deviceStatusInfo.getIpAddress());//test val
            deviceStatusJson.put("SIMNo", deviceStatusInfo.getSimNo());//test val
            deviceStatusJson.put("TimeZone", deviceStatusInfo.getTimeZone());//test val
            deviceStatusJson.put("TotalTemplate", deviceStatusInfo.getTotalTemplate());
            deviceStatusJson.put("TotalUser", deviceStatusInfo.getTotalUser());
            deviceStatusJson.put("UncapRec", deviceStatusInfo.getUnCapRecord());//test val
            strJson = deviceStatusJson.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return strJson;
    }

    public static SignOnMessageInfo parseSignOnMessageJson(String response, SignOnMessageInfo signOnMessageInfo) {
        JSONObject jsonReader = null;
        JSONArray jsonArray = null;
        try {
            jsonReader = new JSONObject(response);
            jsonArray = jsonReader.getJSONArray("CustomJobModellist");
            if (jsonArray != null) {
                int len = jsonArray.length();
                if (len > 0) {
                    signOnMessageInfo = new SignOnMessageInfo();
                    for (int n = 0; n < len; n++) {
                        JSONObject object = jsonArray.getJSONObject(n);
                        String strCommandDatas = object.getString("CommandData");
                        JSONObject jsonCommandData = new JSONObject(strCommandDatas);
                        String pid = jsonCommandData.getString("PID");
                        String message = jsonCommandData.getString("SM");

                        if (pid != null && pid.trim().length() > 0) {
                            signOnMessageInfo.setPid(pid);
                        } else {
                            signOnMessageInfo.setPid("");
                        }

                        if (message != null && message.trim().length() > 0) {
                            signOnMessageInfo.setMessage(message);
                        } else {
                            signOnMessageInfo.setMessage("");
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return signOnMessageInfo;
    }

    public static String createTemplateUploadJsonForMqtt(String imei, String deviceToken, String templateUploadJobComm, String corporateId, String packetId, TemplateUploadInfo templateUploadInfo) {

        String fingerDataJson = "";
        JSONObject templateUploadJson = new JSONObject();
        JSONObject FD = new JSONObject();
        try {
            if (templateUploadInfo != null) {
                templateUploadJson.put("COID", corporateId);
                templateUploadJson.put("CPUID", imei);
                templateUploadJson.put("CT", templateUploadJobComm);
                templateUploadJson.put("DeviceToken", deviceToken);
                templateUploadJson.put("OR", "D");
                templateUploadJson.put("PC", "1");
                templateUploadJson.put("PID", packetId);
                templateUploadJson.put("TA", "01");
                templateUploadJson.put("UID", Integer.toString(UserDetails.getInstance().getLoginId()));

                templateUploadJson.put("EID", templateUploadInfo.getEmpId());
                templateUploadJson.put("CID", templateUploadInfo.getCardId());
                templateUploadJson.put("EN", templateUploadInfo.getEmpName());
                templateUploadJson.put("Pin", templateUploadInfo.getCardId().substring(4));
                templateUploadJson.put("DTOE", templateUploadInfo.getDtoe());

                int val = -1;
                String value = templateUploadInfo.getTemplateSrNo();
                if (value != null && value.length() > 0) {
                    int tempSerialNo = Integer.parseInt(value);
                    if (tempSerialNo == 1) {
                        FD.put("FT", "F1");
                    } else if (tempSerialNo == 2) {
                        FD.put("FT", "F2");
                    }
                } else {
                    FD.put("FT", "");
                }

                value = templateUploadInfo.getFingerIndex();
                if (value != null && value.length() > 0) {
                    val = Utility.getFingerIndexValByName(value);
                    if (val != -1) {
                        FD.put("FI", Integer.toHexString(val).toUpperCase());
                    }
                } else {
                    FD.put("FI", "");
                }

                value = templateUploadInfo.getSecurityLevel();
                if (value != null && value.length() > 0) {
                    val = Utility.getSecurityLvlValByName(value);
                    if (val != -1) {
                        FD.put("SL", Integer.toString(val));
                    }
                } else {
                    FD.put("SL", "");
                }

                value = templateUploadInfo.getFingerQuality();
                if (value != null && value.length() > 0) {
                    val = Utility.getFingerQualityValByName(value);
                    if (val != -1) {
                        FD.put("FQ", Integer.toString(val));
                    }
                } else {
                    FD.put("FQ", "A");
                }

                value = templateUploadInfo.getFmd();
                if (value != null && value.length() > 0) {
                    String t_size = Integer.toString(value.length()); //Sanjay
                    FD.put("TS", t_size);
                } else {
                    FD.put("TS", "");
                }

                if (value != null && value.length() > 0) {
                    FD.put("FMID", value);
                } else {
                    FD.put("FMID", "");
                }

                byte[] fid = templateUploadInfo.getFid();
                if (fid != null && fid.length > 0) {
                    //String strFID = encoder.encode(fid);
                    //FD.put("FID", strFID);
                                /* Fot HTTP Error Code 413 Request Data Too Large Send Blank FID */
                    FD.put("FID", "");
                } else {
                    FD.put("FID", "");
                }

                value = templateUploadInfo.getVerificationMode();
                if (value != null && value.trim().length() > 0) {
                    val = Utility.getVerificationModeValByName(value);
                    if (val != -1) {
                        templateUploadJson.put("VM", Integer.toString(val));
                    }
                } else {
                    templateUploadJson.put("VM", "");
                }

                templateUploadJson.put("FD", FD);
                fingerDataJson = templateUploadJson.toString();
            }


        } catch (JSONException je) {
        }
        return fingerDataJson;
    }

    public static ArrayList <SubjectInfo> parseSubjectJson(JSONObject json, ArrayList <SubjectInfo> subList) {
        JSONArray jsonArray = null;
        try {
            jsonArray = json.getJSONArray("CD");
            if (jsonArray != null) {
                int rootLen = jsonArray.length();
                if (rootLen > 0) {
                    subList = new ArrayList <SubjectInfo>();
                    for (int i = 0; i < rootLen; i++) {
                        SubjectInfo subInfo = new SubjectInfo();
                        JSONObject obj = (JSONObject) jsonArray.get(i);
                        String jid = obj.getString("JID");
                        String subCode = obj.getString("SC");
                        String subName = obj.getString("SN");
                        subInfo.setJobId(jid);
                        subInfo.setSubCode(subCode);
                        subInfo.setSubName(subName);
                        JSONArray jsonArr = obj.getJSONArray("ST");
                        if (jsonArr != null) {
                            int innerLen = jsonArr.length();
                            if (innerLen > 0) {
                                String[] arr = new String[innerLen];
                                for (int j = 0; j < innerLen; j++) {
                                    String subType = (String) jsonArr.get(j);
                                    arr[j] = subType;
                                }
                                subInfo.setSubType(arr);
                                subList.add(subInfo);
                            }
                        }
                    }
                }
            }
        } catch (JSONException e) {
            subList = null;
        }
        return subList;
    }

    public static ArrayList <ProfessorSubjectInfo> parseProfSubJson(JSONObject json, ArrayList <ProfessorSubjectInfo> psl) {
        JSONArray jsonArray = null;
        try {
            jsonArray = json.getJSONArray("CD");
            if (jsonArray != null) {
                int rootLen = jsonArray.length();
                if (rootLen > 0) {
                    psl = new ArrayList <ProfessorSubjectInfo>();
                    for (int i = 0; i < rootLen; i++) {
                        ProfessorSubjectInfo profSubInfo = new ProfessorSubjectInfo();
                        JSONObject obj = (JSONObject) jsonArray.get(i);
                        String jid = obj.getString("JID");
                        String empId = obj.getString("EID");
                        String subCode = obj.getString("SC");
                        profSubInfo.setJid(jid);
                        profSubInfo.setEmpId(empId);
                        profSubInfo.setSubCode(subCode);
                        JSONArray jsonArr = obj.getJSONArray("ST");
                        if (jsonArr != null) {
                            int innerLen = jsonArr.length();
                            if (innerLen > 0) {
                                String[] arr = new String[innerLen];
                                for (int j = 0; j < innerLen; j++) {
                                    String subType = (String) jsonArr.get(j);
                                    arr[j] = subType;
                                }
                                profSubInfo.setSubTypes(arr);
                                psl.add(profSubInfo);
                            }
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Log.d("TEST","Exception:"+e.getMessage());
            psl = null;
        }
        return psl;
    }

    public static ArrayList <ProfessorStudentSubjectInfo> parseProfStuSubJson(JSONObject json, ArrayList <ProfessorStudentSubjectInfo> pssl) {
        JSONArray jsonArray = null;
        try {
            jsonArray = json.getJSONArray("CD");
            if (jsonArray != null) {
                int rootLen = jsonArray.length();
                if (rootLen > 0) {
                    pssl = new ArrayList <ProfessorStudentSubjectInfo>();
                    for (int i = 0; i < rootLen; i++) {
                        ProfessorStudentSubjectInfo profStuSubInfo = new ProfessorStudentSubjectInfo();
                        JSONObject obj = (JSONObject) jsonArray.get(i);
                        String jobId = obj.getString("JID");
                        String profEmpId = obj.getString("EID_P");
                        String stuEmpId = obj.getString("EID_S");
                        String subCode = obj.getString("SC");
                        profStuSubInfo.setJobId(jobId);
                        profStuSubInfo.setProfessorEmpId(profEmpId.trim());
                        profStuSubInfo.setStudentEmpId(stuEmpId.trim());
                        profStuSubInfo.setSubCode(subCode.trim());
                        pssl.add(profStuSubInfo);
                    }
                }
            }
        } catch (JSONException e) {
            pssl = null;
        }
        return pssl;
    }

    public static ArrayList <EmployeeTypeInfo> parseEmployeeTypeJson(JSONObject json, ArrayList <EmployeeTypeInfo> etList) {
        JSONArray jsonArray = null;
        try {
            jsonArray = json.getJSONArray("CD");
            if (jsonArray != null) {
                int rootLen = jsonArray.length();
                if (rootLen > 0) {
                    etList = new ArrayList <EmployeeTypeInfo>();
                    for (int i = 0; i < rootLen; i++) {
                        EmployeeTypeInfo etInfo = new EmployeeTypeInfo();
                        JSONObject obj = (JSONObject) jsonArray.get(i);
                        String et = obj.getString("ET");
                        etInfo.setEmpType(et);
                        etList.add(etInfo);
                    }
                }
            }
        } catch (JSONException e) {
            etList = null;
        }
        return etList;
    }

    public static ArrayList <ContractorInfo> parseContractorJson(JSONObject json, ArrayList <ContractorInfo> ctList) {
        JSONArray jsonArray = null;
        try {
            jsonArray = json.getJSONArray("CD");
            if (jsonArray != null) {
                int rootLen = jsonArray.length();
                if (rootLen > 0) {
                    ctList = new ArrayList <ContractorInfo>();
                    for (int i = 0; i < rootLen; i++) {
                        ContractorInfo ctInfo = new ContractorInfo();
                        JSONObject obj = (JSONObject) jsonArray.get(i);
                        String cn = obj.getString("CN");
                        String ccn = obj.getString("CCN");
                        ctInfo.setContractorName(cn);
                        ctInfo.setCompanyName(ccn);
                        ctList.add(ctInfo);
                    }
                }
            }
        } catch (JSONException e) {
            ctList = null;
        }
        return ctList;
    }

    public static ArrayList <PeriodInfo> parsePeriodJson(JSONObject json, ArrayList <PeriodInfo> pdList) {
        JSONArray jsonArray = null;
        try {
            jsonArray = json.getJSONArray("CD");
            if (jsonArray != null) {
                int rootLen = jsonArray.length();
                if (rootLen > 0) {
                    pdList = new ArrayList <PeriodInfo>();
                    for (int i = 0; i < rootLen; i++) {
                        PeriodInfo pdInfo = new PeriodInfo();
                        JSONObject obj = (JSONObject) jsonArray.get(i);
                        String ft = obj.getString("FT");
                        String tt = obj.getString("TT");
                        String st = obj.getString("ST");
                        String pr = obj.getString("PR");
                        pdInfo.setFromTime(ft);
                        pdInfo.setToTime(tt);
                        pdInfo.setSubType(st);
                        pdInfo.setPeriod(pr);
                        pdList.add(pdInfo);
                    }
                }
            }
        } catch (JSONException e) {
            pdList = null;
        }
        return pdList;

    }

    public static ArrayList <EmployeeValidationBasicInfo> parseEmpValJson(JSONObject json, ArrayList <EmployeeValidationBasicInfo> empInfoList) {

        ArrayList <EmployeeValidationFingerInfo> fingerInfoList = null;
        JSONArray jsonArray = null;
        try {
            jsonArray = json.getJSONArray("CD");
            if (jsonArray != null) {
                int rootLen = jsonArray.length();
                if (rootLen > 0) {
                    empInfoList = new ArrayList <EmployeeValidationBasicInfo>();
                    for (int i = 0; i < rootLen; i++) {
                        EmployeeValidationBasicInfo emInfo = new EmployeeValidationBasicInfo();
                        JSONObject obj = (JSONObject) jsonArray.get(i);
                        String jobId = obj.getString("JID");
                        String eid = obj.getString("EID");
                        String cid = obj.getString("CID");
                        String en = obj.getString("EN");
                        String et = obj.getString("ET");
                        String pin = obj.getString("Pin");
                        String dob = obj.getString("DOB");
                        String dov = obj.getString("DOV");
                        String isLockOpen = obj.getString("IsLockOpen");
                        String mn = obj.getString("MN");
                        String bg = obj.getString("BG");
                        String emid = obj.getString("EMID");
                        String sc = obj.getString("SC");
                        String isBlackListed = obj.getString("IsBlackListed");
                        String vm = obj.getString("VM");

                        emInfo.setJobId(jobId);

                        if (!eid.equals("null") && eid.trim().length() > 0) {
                            emInfo.setEmpId(eid);
                        } else {
                            emInfo.setEmpId("");
                        }

                        if (!cid.equals("null") && cid.trim().length() > 0) {
                            emInfo.setCardId(cid);
                        } else {
                            emInfo.setCardId("");
                        }

                        if (!en.equals("null") && en.trim().length() > 0) {
                            emInfo.setEmpName(en);
                        } else {
                            emInfo.setEmpName("");
                        }

                        if (!et.equals("null") && et.trim().length() > 0) {
                            emInfo.setEmpType(et);
                        } else {
                            emInfo.setEmpType("");
                        }

                        if (!pin.equals("null") && pin.trim().length() > 0) {
                            emInfo.setPin(pin);
                        } else {
                            emInfo.setPin("");
                        }

                        if (!dob.equals("null") && dob.trim().length() > 0) {
                            emInfo.setDob(dob);
                        } else {
                            emInfo.setDob("");
                        }

                        if (!dov.equals("null") && dov.trim().length() > 0) {
                            emInfo.setDov(dov);
                        } else {
                            emInfo.setDov("");
                        }

                        if (!isLockOpen.equals("null") && isLockOpen.trim().length() > 0) {
                            emInfo.setIsLockOpen(isLockOpen);
                        } else {
                            emInfo.setIsLockOpen("");
                        }

                        if (!mn.equals("null") && mn.trim().length() > 0) {
                            emInfo.setMn(mn);
                        } else {
                            emInfo.setMn("");
                        }

                        if (!bg.equals("null") && bg.trim().length() > 0) {
                            emInfo.setBg(bg);
                        } else {
                            emInfo.setBg("");
                        }

                        if (!emid.equals("null") && emid.trim().length() > 0) {
                            emInfo.setEid(emid);
                        } else {
                            emInfo.setEid("");
                        }

                        if (!sc.equals("null") && sc.trim().length() > 0) {
                            emInfo.setSc(sc);
                        } else {
                            emInfo.setSc("");
                        }

                        if (!isBlackListed.equals("null") && isBlackListed.trim().length() > 0) {
                            emInfo.setIsBlackListed(isBlackListed);
                        } else {
                            emInfo.setIsBlackListed("");
                        }

                        if (!vm.equals("null") && vm.trim().length() > 0) {
                            emInfo.setVm(vm);
                        } else {
                            emInfo.setVm("");
                        }

                        JSONArray fArray = obj.getJSONArray("CD");
                        if (fArray != null) {
                            int size = fArray.length();
                            if (size > 0) {
                                for (int j = 0; j < size; j++) {
                                    JSONObject fobj = (JSONObject) fArray.get(j);
                                    if (fobj != null) {
                                        if (j == 0) {
                                            JSONArray fdobj = fobj.getJSONArray("Finger Data");
                                            if (fdobj != null) {
                                                int sizel = fdobj.length();
                                                if (sizel > 0) {
                                                    fingerInfoList = new ArrayList <EmployeeValidationFingerInfo>();
                                                    for (int k = 0; k < sizel; k++) {
                                                        EmployeeValidationFingerInfo finfo = new EmployeeValidationFingerInfo();
                                                        JSONObject fdataobj = (JSONObject) fdobj.get(k);

                                                        String ft = fdataobj.getString("FT");
                                                        String sl = fdataobj.getString("SL");
                                                        String fi = fdataobj.getString("FI");
                                                        String fq = fdataobj.getString("FQ");
                                                        String ts = fdataobj.getString("TS");
                                                        String fmid = fdataobj.getString("FMID");
                                                        String fid = fdataobj.getString("FID");

                                                        if (!ft.equals("null") && ft.trim().length() > 0) {
                                                            finfo.setFt(ft);
                                                        } else {
                                                            finfo.setFt("");
                                                        }

                                                        if (!sl.equals("null") && sl.trim().length() > 0) {
                                                            finfo.setSl(sl);
                                                        } else {
                                                            finfo.setSl("");
                                                        }

                                                        if (!fi.equals("null") && fi.trim().length() > 0) {
                                                            finfo.setFi(fi);
                                                        } else {
                                                            finfo.setFi("");
                                                        }

                                                        if (!fq.equals("null") && fq.trim().length() > 0) {
                                                            finfo.setFq(fq);
                                                        } else {
                                                            finfo.setFq("");
                                                        }

                                                        if (!ts.equals("null") && ts.trim().length() > 0) {
                                                            finfo.setTs(ts);
                                                        } else {
                                                            finfo.setTs("");
                                                        }

                                                        if (!fmid.equals("null") && fmid.trim().length() > 0) {
                                                            finfo.setFmd(fmid);
                                                        } else {
                                                            finfo.setFmd("");
                                                        }

                                                        if (!fid.equals("null") && fid.trim().length() > 0) {
                                                            finfo.setFid(fid);
                                                        } else {
                                                            finfo.setFid("");
                                                        }

                                                        fingerInfoList.add(finfo);
                                                    }
                                                    emInfo.setfInfoList(fingerInfoList);
                                                }
                                            }
                                            // empInfoList.add(emInfo);
                                        } else if (j == 1) {
                                            //Face Data
                                        } else if (j == 2) {
                                            //Iris Data
                                        }
                                    }
                                }
                            }
                        }
                        empInfoList.add(emInfo);
                    }
                }
            }
        } catch (JSONException e) {
            Log.d("TEST","Exception:"+e.getMessage());
            empInfoList = null;
        }
        return empInfoList;
    }

    public static ArrayList <EmployeeValidationBasicInfo> parseRemoteEnrollJson(JSONObject json, ArrayList <EmployeeValidationBasicInfo> empInfoList) {

        ArrayList <EmployeeValidationFingerInfo> fingerInfoList = null;
        JSONArray jsonArray = null;
        try {
            jsonArray = json.getJSONArray("CD");
            if (jsonArray != null) {
                int rootLen = jsonArray.length();
                if (rootLen > 0) {
                    empInfoList = new ArrayList <EmployeeValidationBasicInfo>();
                    for (int i = 0; i < rootLen; i++) {
                        EmployeeValidationBasicInfo emInfo = new EmployeeValidationBasicInfo();
                        JSONObject obj = (JSONObject) jsonArray.get(i);
                        String jobId = obj.getString("JID");
                        String eid = obj.getString("EID");
                        String cid = obj.getString("CID");
                        String en = obj.getString("EN");
                        String et = obj.getString("ET");
                        String pin = obj.getString("Pin");
                        String dob = obj.getString("DOB");
                        String dov = obj.getString("DOV");
                        String isLockOpen = obj.getString("IsLockOpen");
                        String mn = obj.getString("MN");
                        String bg = obj.getString("BG");
                        String emid = obj.getString("EMID");
                        String sc = obj.getString("SC");
                        String isBlackListed = obj.getString("IsBlackListed");
                        String vm = obj.getString("VM");

                        String ft = obj.getString("FT");
                        String sl = obj.getString("SL");
                        String fi = obj.getString("FI");
                        String fq = obj.getString("FQ");

                        emInfo.setJobId(jobId);

                        if (!eid.equals("null") && eid.trim().length() > 0) {
                            emInfo.setEmpId(eid);
                        } else {
                            emInfo.setEmpId("");
                        }

                        if (!cid.equals("null") && cid.trim().length() > 0) {
                            emInfo.setCardId(cid);
                        } else {
                            emInfo.setCardId("");
                        }

                        if (!en.equals("null") && en.trim().length() > 0) {
                            emInfo.setEmpName(en);
                        } else {
                            emInfo.setEmpName("");
                        }

                        if (!et.equals("null") && et.trim().length() > 0) {
                            emInfo.setEmpType(et);
                        } else {
                            emInfo.setEmpType("");
                        }

                        if (!pin.equals("null") && pin.trim().length() > 0) {
                            emInfo.setPin(pin);
                        } else {
                            emInfo.setPin("");
                        }

                        if (!dob.equals("null") && dob.trim().length() > 0) {
                            emInfo.setDob(dob);
                        } else {
                            emInfo.setDob("");
                        }

                        if (!dov.equals("null") && dov.trim().length() > 0) {
                            emInfo.setDov(dov);
                        } else {
                            emInfo.setDov("");
                        }

                        if (!isLockOpen.equals("null") && isLockOpen.trim().length() > 0) {
                            emInfo.setIsLockOpen(isLockOpen);
                        } else {
                            emInfo.setIsLockOpen("");
                        }

                        if (!mn.equals("null") && mn.trim().length() > 0) {
                            emInfo.setMn(mn);
                        } else {
                            emInfo.setMn("");
                        }

                        if (!bg.equals("null") && bg.trim().length() > 0) {
                            emInfo.setBg(bg);
                        } else {
                            emInfo.setBg("");
                        }

                        if (!emid.equals("null") && emid.trim().length() > 0) {
                            emInfo.setEid(emid);
                        } else {
                            emInfo.setEid("");
                        }

                        if (!sc.equals("null") && sc.trim().length() > 0) {
                            emInfo.setSc(sc);
                        } else {
                            emInfo.setSc("");
                        }

                        if (!isBlackListed.equals("null") && isBlackListed.trim().length() > 0) {
                            emInfo.setIsBlackListed(isBlackListed);
                        } else {
                            emInfo.setIsBlackListed("");
                        }

                        if (!vm.equals("null") && vm.trim().length() > 0) {
                            emInfo.setVm(vm);
                        } else {
                            emInfo.setVm("");
                        }

                        fingerInfoList = new ArrayList <EmployeeValidationFingerInfo>();

                        EmployeeValidationFingerInfo finfo = new EmployeeValidationFingerInfo();

                        if (!ft.equals("null") && ft.trim().length() > 0) {
                            finfo.setFt(ft);
                        } else {
                            finfo.setFt("");
                        }

                        if (!sl.equals("null") && sl.trim().length() > 0) {
                            finfo.setSl(sl);
                        } else {
                            finfo.setSl("");
                        }

                        if (!fi.equals("null") && fi.trim().length() > 0) {
                            finfo.setFi(fi);
                        } else {
                            finfo.setFi("");
                        }

                        if (!fq.equals("null") && fq.trim().length() > 0) {
                            finfo.setFq(fq);
                        } else {
                            finfo.setFq("");
                        }

                        fingerInfoList.add(finfo);
                        emInfo.setfInfoList(fingerInfoList);
                        empInfoList.add(emInfo);
                    }
                }
            }
        } catch (JSONException e) {
            empInfoList = null;
        }
        return empInfoList;
    }

    public static String createCollegeAttendanceJson(CollegeAttendanceInfo attendanceInfo) {

        JSONObject inner = new JSONObject();
        JSONObject outer = new JSONObject();
        JSONArray arr = new JSONArray();

        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSS");
        Date date = new Date();
        Random random = new Random();
        String randomNum = String.format("%04d", random.nextInt(10000));
        String pid = dateFormat.format(date) + randomNum;

        String finalJson = "";

        try {
            outer.put("PID", pid);
            outer.put("CT",Constants.ATTENDANCE_DATA_UPLOAD ); //1000
            outer.put("CST", "00");
            outer.put("DT", "365418");
            outer.put("CPUID", "123456789ABCD");
            outer.put("IMEI", "123456789ABCD");
            outer.put("COID", "T0000000020");
            outer.put("TA", "00");   /*A->Finger, B->Face*/
            outer.put("PC", "1");

            inner.put("JID", randomNum);
            inner.put("AttendanceDateTime", attendanceInfo.getPunchDate()+attendanceInfo.getPunchTime());
            inner.put("EID_P", attendanceInfo.getEid_p());
            inner.put("EID_S", attendanceInfo.getEid_s());
            inner.put("SC", attendanceInfo.getSc());
            inner.put("ST", attendanceInfo.getSt());
            inner.put("PR", "1");
            inner.put("InOutMode", attendanceInfo.getInOutMode());
            inner.put("ReasonCode", "FF");
            inner.put("Status", "S");
            inner.put("Lat", attendanceInfo.getLatitude());
            inner.put("Long", attendanceInfo.getLongitude());
            inner.put("Cap_Dump", "");
            inner.put("Type", "Finger");
            inner.put("ContentLength", "");
            inner.put("PictureBinary", "");
            inner.put("FileExtension", "");

            arr.put(inner);

            outer.put("CD", arr);

            finalJson = outer.toString();

            Log.d("TEST", "Json:" + finalJson);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return finalJson;
    }

    public static MQTTHeaderInfo parseHeaderData(JSONObject json, MQTTHeaderInfo headerInfo) {
        try {
            headerInfo = new MQTTHeaderInfo();
            headerInfo.setPid(json.getString("PID"));
            headerInfo.setCt(json.getString("CT"));
            headerInfo.setCst(json.getString("CST"));
            headerInfo.setDt(json.getString("DT"));
            headerInfo.setCpuid(json.getString("CPUID"));
            headerInfo.setImei(json.getString("IMEI"));
            headerInfo.setCoid(json.getString("COID"));
            headerInfo.setTa(json.getString("TA"));
            headerInfo.setPc(json.getString("PC"));
        } catch (JSONException e) {
            headerInfo = null;
        }
        return headerInfo;
    }

    public static String createJsonForEnrolledTemplates(JSONObject headerJson, EmployeeValidationBasicInfo info) {

        String response = "";

        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSS");
        Date date = new Date();
        String dateTime = dateFormat.format(date);

        Random random = new Random();
        String last4Digit = String.format("%04d", random.nextInt(10000));

        String pid = dateTime + last4Digit;

        // headerJson.remove("CD");

        JSONObject root = new JSONObject();
        JSONArray outerCD = new JSONArray();
        JSONArray innerCD = new JSONArray();

        JSONObject basicInfo = new JSONObject();
        try {

            root.put("PID", pid);
            root.put("CT", Constants.AUTO_TEMPLATE_UPLOAD);
            root.put("CST", "Add");
            root.put("DT", "365418");
            root.put("CPUID", "1234ABCD213");
            root.put("IMEI", "123456789012345");
            root.put("COID", "T0000000020");
            root.put("TA", "00");
            root.put("PC", "1");

            basicInfo.put("JID", "1111");
            basicInfo.put("EID", info.getEmpId());
            basicInfo.put("CID", info.getCardId());
            basicInfo.put("EN", info.getEmpName());
            basicInfo.put("ET", "Professor");
            basicInfo.put("Pin", info.getPin());
            basicInfo.put("DOB", info.getDob());
            basicInfo.put("DOV", info.getDov());
            basicInfo.put("IsLockOpen", info.getIsLockOpen());
            basicInfo.put("MN", info.getMn());
            basicInfo.put("AID", info.getAid());
            basicInfo.put("BG", info.getBg());
            basicInfo.put("EMID", info.getEid());
            basicInfo.put("SC", info.getSc());
            basicInfo.put("IsBlackListed", info.getIsBlackListed());
            basicInfo.put("VM", info.getVm());


            JSONArray fdArray = new JSONArray();

            JSONObject faceData = new JSONObject();
            JSONObject irisData = new JSONObject();
            JSONObject fingerData = new JSONObject();

            ArrayList <EmployeeValidationFingerInfo> fInfoList = info.getfInfoList();
            if (fInfoList != null) {
                int size = fInfoList.size();
                if (size > 0) {
                    for (int count = 0; count < size; count++) {
                        EmployeeValidationFingerInfo fInfo = fInfoList.get(count);
                        JSONObject fObj = new JSONObject();
                        fObj.put("FT", fInfo.getFt());
                        fObj.put("SL", fInfo.getSl());
                        fObj.put("FI", fInfo.getFi());
                        fObj.put("FQ", fInfo.getFq());
                        fObj.put("TS", "512");
                        fObj.put("FMID", fInfo.getFmd());
                        fObj.put("FID", "");
                        fdArray.put(fObj);
                    }
                    fingerData.put("Finger Data", fdArray);
                }
                faceData.put("Face Data", new JSONArray());
                irisData.put("Iris Data", new JSONArray());
                innerCD.put(fingerData);
                innerCD.put(faceData);
                innerCD.put(irisData);
            }

            basicInfo.put("CD", innerCD);
            outerCD.put(basicInfo);
            root.put("CD", outerCD);
            response = root.toString();

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return response;
    }

    public static String createJsonForEnrolledTemplates(EmployeeValidationBasicInfo info) {

        String response = "";

        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSS");
        Date date = new Date();
        String dateTime = dateFormat.format(date);

        Random random = new Random();
        String last4Digit = String.format("%04d", random.nextInt(10000));

        String pid = dateTime + last4Digit;

        JSONObject json = new JSONObject();
        JSONArray outerCD = new JSONArray();
        JSONArray innerCD = new JSONArray();

        JSONObject basicInfo = new JSONObject();
        try {

            json.put("PID", pid);
            json.put("CT", Constants.AUTO_TEMPLATE_UPLOAD);
            json.put("CST", "Add");
            json.put("DT", "365418");
            json.put("CPUID", "1234ABCD213");
            json.put("IMEI", "123456789012345");
            json.put("COID", "T0000000020");
            json.put("TA", "00");
            json.put("PC", "1");

            basicInfo.put("JID", last4Digit);
            basicInfo.put("EID", info.getEmpId());
            basicInfo.put("CID", info.getCardId());
            basicInfo.put("EN", info.getEmpName());

            basicInfo.put("ET", info.getEmpType());
            basicInfo.put("Pin", info.getPin());
            basicInfo.put("DOB", info.getDob());
            basicInfo.put("DOV", info.getDov());
            basicInfo.put("IsLockOpen", info.getIsLockOpen());
            basicInfo.put("MN", info.getMn());
            basicInfo.put("AID", info.getAid());
            basicInfo.put("BG", info.getBg());
            basicInfo.put("EMID", info.getEid());
            basicInfo.put("SC", info.getSc());
            basicInfo.put("IsBlackListed", info.getIsBlackListed());
            basicInfo.put("VM", info.getVm());


            JSONArray fdArray = new JSONArray();

            JSONObject faceData = new JSONObject();
            JSONObject irisData = new JSONObject();
            JSONObject fingerData = new JSONObject();

            ArrayList <EmployeeValidationFingerInfo> fInfoList = info.getfInfoList();
            if (fInfoList != null) {
                int size = fInfoList.size();
                if (size > 0) {
                    for (int count = 0; count < size; count++) {
                        EmployeeValidationFingerInfo fInfo = fInfoList.get(count);
                        JSONObject fObj = new JSONObject();
                        fObj.put("FT", fInfo.getFt());
                        fObj.put("SL", fInfo.getSl());
                        fObj.put("FI", fInfo.getFi());
                        fObj.put("FQ", fInfo.getFq());
                        fObj.put("TS", "512");
                        fObj.put("FMID", fInfo.getFmd());
                        fObj.put("FID", "");
                        fdArray.put(fObj);
                    }
                    fingerData.put("Finger Data", fdArray);
                }
                faceData.put("Face Data", new JSONArray());
                irisData.put("Iris Data", new JSONArray());
                innerCD.put(fingerData);
                innerCD.put(faceData);
                innerCD.put(irisData);
            }

            basicInfo.put("CD", innerCD);
            outerCD.put(basicInfo);
            json.put("CD", outerCD);
            response = json.toString();

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return response;
    }

    public static String createDashBoardData() {

        String response = "";

        SQLiteCommunicator dbComm = new SQLiteCommunicator();

        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSS");
        Date date = new Date();
        String dateTime = dateFormat.format(date);

        Random random = new Random();
        String last4Digit = String.format("%04d", random.nextInt(10000));

        String pid = dateTime + last4Digit;

        JSONObject root = new JSONObject();

        JSONArray payload = new JSONArray();
        JSONObject jsonCD = new JSONObject();

        try {
            root.put("PID", pid);
            root.put("CT", Constants.DASHBOARD_DATA);
            root.put("CST", "Add");
            root.put("DT", "365418");
            root.put("CPUID", "1234ABCD213");
            root.put("IMEI", "123456789012345");
            root.put("COID", "T0000000020");
            root.put("TA", "00");
            root.put("PC", "1");


            jsonCD.put("JID", last4Digit);
            jsonCD.put("DRS", "R");
            jsonCD.put("M/C", "Online");
            jsonCD.put("DateTime", dateTime);

            int val = dbComm.getAutoIdByEmpType("Professor");
            if (val != -1) {
                int count = dbComm.getTotalEnrolledProfessorOrStudent(val);
                if (count != -1) {
                    jsonCD.put("No of EID_P", count);
                } else {
                    jsonCD.put("No of EID_P", 0);
                }
            } else {
                jsonCD.put("No of EID_P", 0);
            }

            val = dbComm.getAutoIdByEmpType("Student");
            if (val != -1) {
                int count = dbComm.getTotalEnrolledProfessorOrStudent(val);
                if (count != -1) {
                    jsonCD.put("No of EID_S", count);
                } else {
                    jsonCD.put("No of EID_S", 0);
                }
            } else {
                jsonCD.put("No of EID_S", 0);
            }

            val = dbComm.getTotalSubjects();
            if (val != -1) {
                jsonCD.put("No of Subject", val);
            } else {
                jsonCD.put("No of Subject", 0);
            }

            val = dbComm.getLastLoginStatusId();
            if (val != -1) {
                ArrayList <String> list = dbComm.getLastLoginStatus(val);
                if (list != null && list.size() == 2) {
                    jsonCD.put("Prof Login", list.get(0));
                    jsonCD.put("No of Student Punched", list.get(1));
                } else {
                    jsonCD.put("Prof Login", "");
                    jsonCD.put("No of Student Punched", 0);
                }
            } else {
                jsonCD.put("Prof Login", "");
                jsonCD.put("No of Student Punched", 0);
            }

            MorphoDatabase morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
            if (morphoDatabase != null) {
                Long noOfRecords = new Long(10);
                int ret = morphoDatabase.getNbUsedRecord(noOfRecords);
                if (ret == 0 && noOfRecords > 0) {
                    jsonCD.put("Total Enrolled Templates", noOfRecords);
                } else {
                    jsonCD.put("Total Enrolled Templates", 0);
                }
            } else {
                jsonCD.put("Total Enrolled Templates", 0);
            }

            val = dbComm.getTotalEnrolledUsers("Y");
            if (val != -1) {
                jsonCD.put("Total Enrolled User", val);
            } else {
                jsonCD.put("Total Enrolled User", 0);
            }

            val = dbComm.getTotalEnrolledUsers("N");
            if (val != -1) {
                jsonCD.put("Unenrolled User", val);
            } else {
                jsonCD.put("Unenrolled User", 0);
            }

            jsonCD.put("No of Uncaptured Record", 0);
            jsonCD.put("Device Type", "Smart");
            jsonCD.put("App/Firmware Version", "1.0");
            jsonCD.put("SFM Alive/Dead", "");
            jsonCD.put("Error Code", "");


            jsonCD.put("Global Verification Mode", "");
            jsonCD.put("Sensor Type", "Morpho");

            jsonCD.put("Battery Status", "");
            jsonCD.put("SSID", "");
            jsonCD.put("SIM No", "");
            jsonCD.put("GPRS Signal Strength", "");
            jsonCD.put("APN", "");
            jsonCD.put("Temp Type", "ISO");
            jsonCD.put("Time Zone Info", "");

            jsonCD.put("Lat", EmployeeAttendanceActivity.latitude);
            jsonCD.put("Long", EmployeeAttendanceActivity.longitude);

            payload.put(0, jsonCD);
            root.put("CD", payload);
            response = root.toString();

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return response;
    }
}
