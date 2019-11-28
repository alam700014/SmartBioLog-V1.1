package com.android.fortunaattendancesystem.service;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.android.fortunaattendancesystem.activities.EmployeeAttendanceActivity;
import com.android.fortunaattendancesystem.activities.EmployeeFingerEnrollmentActivity;
import com.android.fortunaattendancesystem.constant.Constants;
import com.android.fortunaattendancesystem.helper.Utility;
import com.android.fortunaattendancesystem.info.ProcessInfo;
import com.android.fortunaattendancesystem.model.AttendanceInfo;
import com.android.fortunaattendancesystem.model.DeviceStatusInfo;
import com.android.fortunaattendancesystem.model.EmpValidationDownloadInfo;
import com.android.fortunaattendancesystem.model.RemoteEnrollmentInfo;
import com.android.fortunaattendancesystem.model.SignOnMessageInfo;
import com.android.fortunaattendancesystem.model.TemplateDownloadInfo;
import com.android.fortunaattendancesystem.model.TemplateUploadInfo;
import com.android.fortunaattendancesystem.model.TemplateUploadTypeInfo;
import com.android.fortunaattendancesystem.submodules.MorphoCommunicator;
import com.android.fortunaattendancesystem.submodules.SQLiteCommunicator;
import com.morpho.morphosmart.sdk.ErrorCodes;
import com.morpho.morphosmart.sdk.MorphoDatabase;
import com.morpho.morphosmart.sdk.MorphoDevice;
import com.morpho.morphosmart.sdk.Template;
import com.morpho.morphosmart.sdk.TemplateList;
import com.morpho.morphosmart.sdk.TemplateType;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by fortuna on 31/10/18.
 */

public class EzeeHrLiteCommunicator {

    private static ExecutorService executorService = null;
    private Context context;
    public static String deviceToken = "";
    public static String serverIP, serverPort, imei;

    private SQLiteCommunicator dbComm = new SQLiteCommunicator();
    private MorphoCommunicator morphoComm = null;

    boolean isTemplateDownloadComplete = true;
    boolean isEmpValDownloadComplete = true;
    boolean isTemplateUploadComplete = true;
    boolean isAutoTemplateUploadComplete = true;
    boolean isDateFetchComplete = true;

    boolean isTemplateDownloadRunning = false;


    private static HashMap <String, String> map = new HashMap <String, String>();

    private static boolean isTokenReceived = false;
    private static boolean isDeviceRegistered = false;

    public EzeeHrLiteCommunicator(Context context, String serverIP, String serverPort, String imei) {
        this.context = context;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.imei = imei;
    }

    public void MakeThreadCall() {
        if (executorService == null) {
            executorService = Executors.newFixedThreadPool(12);
        }
        morphoComm = new MorphoCommunicator(context);
        boolean status = false;
        status = isNetworkAvailable();
        if (status) {
            status = isInternetWorking();
            if (status) {
                DeviceRegistrationThread deviceRegistrationThread = new DeviceRegistrationThread();
                executorService.submit(deviceRegistrationThread);
            }
        }
    }


    //================================ Device Registration Thread Start  ===============================================//

    private class DeviceRegistrationThread implements Runnable {
        @Override
        public void run() {
            while (!isDeviceRegistered) {
                Random random = new Random();
                String pid = String.format("%04d", random.nextInt(10000));
                String strJson = JSONCreatorParser.getDeviceRegistrationJsonData(imei, pid, Constants.CORPORATE_ID, Constants.DEVICE_REG_COMM);
                if (!strJson.isEmpty() && strJson.trim().length() > 0) {
                    String strServerUrl = "http://" + serverIP + ":" + serverPort + Constants.DEVICE_REG_URL;
                    AsyncTaskPostDeviceRegistration postDeviceRegistrationTask = new AsyncTaskPostDeviceRegistration();
                    postDeviceRegistrationTask.execute(strServerUrl, strJson, pid);
                }
                try {
                    Thread.sleep(20000);
                } catch (InterruptedException ie) {
                }
            }
        }
    }

    private class AsyncTaskPostDeviceRegistration extends AsyncTask <String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... strings) {
            String returnJson;
            String strServerUrl = strings[0];
            String strJson = strings[1];
            String pid = strings[2];
            boolean response = false;
            try {
                returnJson = httpPostRequest(strServerUrl, strJson);
                if (!returnJson.isEmpty() && returnJson.trim().length() > 0) {
                    JSONObject reader = new JSONObject(returnJson);
                    String result = reader.get("Result").toString();
                    if (result.equals(pid)) {
                        response = true;
                    }
                }
            } catch (JSONException e) {
            }
            return response;
        }

        @Override
        protected void onPostExecute(Boolean response) {
            Log.d("TEST","Response:"+response);
            if (response) {
                isDeviceRegistered = true;
                GetDeviceTokenThread deviceTokenThread = new GetDeviceTokenThread();
                executorService.submit(deviceTokenThread);
            }
        }
    }


    // =============================== Get Device Token Thread Start  ============================== */

    class GetDeviceTokenThread implements Runnable {
        @Override
        public void run() {
            boolean status = false;
            while (!isTokenReceived) {
                try {
                    status = isNetworkAvailable();
                    if (status) {
                        status = isInternetWorking();
                        if (status) {
                            String serverUrl = "http://" + serverIP + ":" + serverPort + Constants.GET_DEVICE_TOKEN_URL + "corporateid=" + Constants.CORPORATE_ID + "&" + "cpuid=" + imei;
                            AyncTaskGetDeviceToken getDeviceTokenTask = new AyncTaskGetDeviceToken();
                            getDeviceTokenTask.execute(serverUrl);
                        }
                    }
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ie) {
                    }
                } catch (Exception e) {
                }
            }
        }
    }

    class AyncTaskGetDeviceToken extends AsyncTask <String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String serverUrl = strings[0].trim();
            String JsonDeviceToken = httpGetRequest(serverUrl);
            try {
                JSONObject reader = new JSONObject(JsonDeviceToken);
                deviceToken = reader.get("Result").toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return deviceToken;
        }

        @Override
        protected void onPostExecute(String deviceToken) {

            Log.d("TEST", "Device Token:" + deviceToken);

            if (!deviceToken.isEmpty() && deviceToken.trim().length() > 0 && deviceToken.length() == 8) {

                isTokenReceived = true;

                AttendanceDataTransferThread attendanceDataTransferThread = new AttendanceDataTransferThread();
                EmployeeValidationDownloadThread employeeValidationDownloadThread = new EmployeeValidationDownloadThread();
                TemplateUploadRequestThread templateUploadRequestThread = new TemplateUploadRequestThread();
                AutoTemplateUploadThread autoTemplateUploadThread = new AutoTemplateUploadThread();
                TemplateDownloadRequestThread templateDownloadRequestThread = new TemplateDownloadRequestThread();
                RemoteEnrollmentRequestThread remoteEnrollmentRequestThread = new RemoteEnrollmentRequestThread();
                DateTimeSyncThread dateTimeRequestThread = new DateTimeSyncThread();

                executorService.submit(attendanceDataTransferThread);
                executorService.submit(employeeValidationDownloadThread);
                executorService.submit(templateUploadRequestThread);
                executorService.submit(autoTemplateUploadThread);
                executorService.submit(templateDownloadRequestThread);
                executorService.submit(remoteEnrollmentRequestThread);
                executorService.submit(dateTimeRequestThread);


                //AutoEmployeeValidationUploadThread employeeValidationUploadThread = new AutoEmployeeValidationUploadThread();
                //DeviceStatusSendThread deviceStatusSendThread = new DeviceStatusSendThread();
                //GetSignOnMessageThread getSignOnMessageThread = new GetSignOnMessageThread();


                //executorService.submit(employeeValidationUploadThread);
                //executorService.submit(deviceStatusSendThread);
                //executorService.submit(getSignOnMessageThread);
            }
        }
    }


    //============================  Employee Validation Download Thread  ================================//

    private class DateTimeSyncThread implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    boolean status = false;
                    status = isNetworkAvailable();
                    if (status) {
                        status = isInternetWorking();
                        if (status) {
                            if (isDateFetchComplete) {
                                isDateFetchComplete = false;
                                GetDateTimeTask task = new GetDateTimeTask();
                                task.execute();
                            }
                        }
                    }
                } catch (Exception e) {
                }
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ie) {
                }
            }
        }
    }

    private class GetDateTimeTask extends AsyncTask <Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            String responseJson = "";
            String serverUrl = "http://" + serverIP + ":" + serverPort + Constants.GET_DATE_TIME_URL + "?corporateid=" + Constants.CORPORATE_ID;
            responseJson = httpGetRequest(serverUrl);
            return responseJson;
        }

        @Override
        protected void onPostExecute(String response) {
            if (response.trim().length() > 0) {
            }
            isDateFetchComplete = true;
        }
    }


    //=============================== Post Attendance Data Thread Start =================================//

    public class AttendanceDataTransferThread implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    boolean status = false;
                    status = isNetworkAvailable();
                    if (status) {
                        status = isInternetWorking();
                        if (status) {
                            AttendanceInfo attInfo = null;
                            attInfo = dbComm.getAttendanceData(attInfo);//Database Communication
                            if (attInfo != null && attInfo.getId() != -1) {
                                Random random = new Random();
                                String sendPid = String.format("%04d", random.nextInt(10000));
                                String reqJson = JSONCreatorParser.getAttendanceJsonData(sendPid, imei, Constants.CORPORATE_ID, deviceToken, Constants.ATTENDANCE_POST_COMM, attInfo);
                                if (!reqJson.isEmpty() && reqJson.trim().length() > 0) {
                                    String serverUrl = "http://" + serverIP + ":" + serverPort + Constants.ATTENDANCE_UPLOAD_URL;
                                    String response = httpPostRequest(serverUrl, reqJson);
                                    if (!response.isEmpty() && response.trim().length() > 0) {
                                        try {
                                            JSONObject reader = new JSONObject(response);
                                            String recvPid = reader.getString("Result");
                                            if (!recvPid.contains("null")) {
                                                int recvPidVal = Integer.parseInt(recvPid);
                                                int sendPidVal = Integer.parseInt(sendPid);
                                                if (recvPidVal == sendPidVal) {
                                                    dbComm.updateAttendanceTable(attInfo.getId());//Database Communication
                                                }
                                            }
                                        } catch (JSONException e) {
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                }
            }
        }
    }

    //============================  Employee Validation Download Thread  ================================//

    private class EmployeeValidationDownloadThread implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    boolean status = false;
                    status = isNetworkAvailable();
                    if (status) {
                        status = isInternetWorking();
                        if (status) {
                            if (isEmpValDownloadComplete) {
                                isEmpValDownloadComplete = false;
                                AsyncTaskGetEmployeeValidation getEmployeeValidationTask = new AsyncTaskGetEmployeeValidation();
                                getEmployeeValidationTask.execute();
                            }
                        }
                    }
                } catch (Exception e) {
                }
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ie) {
                }
            }
        }
    }

    private class AsyncTaskGetEmployeeValidation extends AsyncTask <Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... voids) {
            int dbStatus = -1;
            String responseJson = "";
            String serverUrl = "http://" + serverIP + ":" + serverPort + Constants.GET_JOB_URL + "?corporateid=" + Constants.CORPORATE_ID + "&cpuid=" + imei + "&DeviceToken=" + deviceToken + "&commnandtype=" + Constants.EMP_VALIDATION_JOB_DOWNLOAD_COMM + "&DataCount=100";
            responseJson = httpGetRequest(serverUrl);
            if (!responseJson.isEmpty() && responseJson.trim().length() > 0) {
                ArrayList <EmpValidationDownloadInfo> empInfoList = null;
                empInfoList = JSONCreatorParser.parseEmpValidationDownloadJson(responseJson, empInfoList);
                if (empInfoList != null) {
                    int size = empInfoList.size();
                    if (size > 0) {
                        for (int i = 0; i < size; i++) {
                            EmpValidationDownloadInfo info = empInfoList.get(i);
                            String statusMsg = validateRecvData(info);
                            if (statusMsg != null && statusMsg.equals("0000")) {
                                int autoId = dbComm.getAutoIdByEmpId(info.getEmpId());
                                if (autoId == -1) {
                                    info = dbComm.insertRemotelyEnrolledEmployeeData(info);//Database Communication
                                    dbStatus = info.getDbStatus();
                                    if (dbStatus != -1) {
                                        String strJobID = info.getJobId();
                                        String reqJson = JSONCreatorParser.getPostJobJson(Constants.CORPORATE_ID, imei, deviceToken, strJobID, statusMsg);
                                        String url = "http://" + serverIP + ":" + serverPort + Constants.POST_UNFINISHED_JOB_URL;
                                        AsyncTaskJobCompletion postUnfinishedJobsTask = new AsyncTaskJobCompletion();
                                        postUnfinishedJobsTask.execute(url, reqJson);
                                    } else {
                                        String strJobID = info.getJobId();
                                        String reqJson = JSONCreatorParser.getPostJobJson(Constants.CORPORATE_ID, imei, deviceToken, strJobID, statusMsg);
                                        String url = "http://" + serverIP + ":" + serverPort + Constants.POST_UNFINISHED_JOB_URL;
                                        AsyncTaskJobCompletion postUnfinishedJobsTask = new AsyncTaskJobCompletion();
                                        postUnfinishedJobsTask.execute(url, reqJson);
                                    }
                                } else {
                                    String strJobID = info.getJobId();
                                    String reqJson = JSONCreatorParser.getPostJobJson(Constants.CORPORATE_ID, imei, deviceToken, strJobID, statusMsg);
                                    String url = "http://" + serverIP + ":" + serverPort + Constants.POST_UNFINISHED_JOB_URL;
                                    AsyncTaskJobCompletion postUnfinishedJobsTask = new AsyncTaskJobCompletion();
                                    postUnfinishedJobsTask.execute(url, reqJson);
                                }
                            } else {
                                String strJobID = info.getJobId();
                                String reqJson = JSONCreatorParser.getPostJobJson(Constants.CORPORATE_ID, imei, deviceToken, strJobID, statusMsg);
                                String url = "http://" + serverIP + ":" + serverPort + Constants.POST_UNFINISHED_JOB_URL;
                                AsyncTaskJobCompletion postUnfinishedJobsTask = new AsyncTaskJobCompletion();
                                postUnfinishedJobsTask.execute(url, reqJson);
                            }
                        }
                    }
                }
            }
            return dbStatus;
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (result != -1) {
                Log.d("TEST", "Employee Validation Download Successfully");
            }
            isEmpValDownloadComplete = true;
        }
    }

    //============================== Auto Template Upload Request Thread Start  =================================//

    private class AutoTemplateUploadThread implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    boolean status = false;
                    status = isNetworkAvailable();
                    if (status) {
                        status = isInternetWorking();
                        if (status) {
                            if (isAutoTemplateUploadComplete) {
                                isAutoTemplateUploadComplete = false;
                                AsyncTaskGetTemplateForUpload getTemplateForUploadRequestTask = new AsyncTaskGetTemplateForUpload();
                                getTemplateForUploadRequestTask.execute();
                            }
                        }
                    }
                } catch (Exception e) {
                }
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ie) {
                }
            }
        }
    }

    private class AsyncTaskGetTemplateForUpload extends AsyncTask <String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            HashMap <Integer, String> fingerIdFingerDataMap = null;
            String url = "http://" + serverIP + ":" + serverPort + Constants.TEMPLATE_UPLOAD_URL;
            ArrayList <TemplateUploadInfo> templateUploadInfoList = null;
            templateUploadInfoList = dbComm.getTemplateForAutoUpload(templateUploadInfoList);//Database Communication
            if (templateUploadInfoList != null) {
                int size = templateUploadInfoList.size();
                if (size > 0) {
                    Random random = new Random();
                    String packetId = String.format("%04d", random.nextInt(10000));
                    fingerIdFingerDataMap = JSONCreatorParser.createTemplateUploadJson(imei, deviceToken, Constants.TEMPLATE_UPLOAD_JOB_COMM, Constants.CORPORATE_ID, packetId, templateUploadInfoList, fingerIdFingerDataMap);
                    if (fingerIdFingerDataMap != null) {
                        size = fingerIdFingerDataMap.size();
                        if (size > 0) {
                            for (Map.Entry <Integer, String> entry : fingerIdFingerDataMap.entrySet()) {
                                AutoPostEnrolledTemplate post = new AutoPostEnrolledTemplate();
                                post.execute(url, packetId, entry.getKey().toString(), entry.getValue());
                            }
                        }
                    }
                }
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            isAutoTemplateUploadComplete = true;
        }
    }


    private class AutoPostEnrolledTemplate extends AsyncTask <String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            int status = -1;
            String url = params[0];
            String jobId = params[1];
            String fingerId = params[2];
            String fingerData = params[3];
            String response = httpPostRequest(url, fingerData);
            if (!response.isEmpty() && response.trim().length() > 0) {
                try {
                    JSONObject jPID = new JSONObject(fingerData);
                    String sendPid = jPID.getString("PID");
                    JSONObject reader = new JSONObject(response);
                    String recvPid = reader.getString("Result");
                    if (!recvPid.contains("null")) {
                        if (recvPid.equals(sendPid)) {
                            status = dbComm.updateFingerTemplateTable(fingerId);//Database Communication
                            String statusMsg = "0000";
                            String postJobJson = JSONCreatorParser.getPostJobJson(Constants.CORPORATE_ID, imei, deviceToken, jobId, statusMsg);
                            if (postJobJson.trim().length() > 0) {
                                url = "http://" + serverIP + ":" + serverPort + Constants.POST_UNFINISHED_JOB_URL;
                                AsyncTaskJobCompletion postUnfinishedJobsTask = new AsyncTaskJobCompletion();
                                postUnfinishedJobsTask.execute(url, postJobJson);
                            }
                        }
                    }
                } catch (JSONException e) {

                }
            }
            return status;
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (result != -1) {
                Log.d("TEST", "Template Upload Successfully");
            } else {
                Log.d("TEST", "Template Upload Failure");
            }
        }
    }


    //============================== Template Upload Request Thread Start  =================================//

    private class TemplateUploadRequestThread implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    boolean status = false;
                    status = isNetworkAvailable();
                    if (status) {
                        status = isInternetWorking();
                        if (status) {
                            if (isTemplateUploadComplete) {
                                isTemplateUploadComplete = false;
                                AsyncTaskGetTemplateUploadRequest getTemplateUploadRequestTask = new AsyncTaskGetTemplateUploadRequest();
                                getTemplateUploadRequestTask.execute();
                            }
                        }
                    }
                } catch (Exception e) {
                }
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ie) {
                }
            }
        }
    }

    private class AsyncTaskGetTemplateUploadRequest extends AsyncTask <Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            String response = "";
            String url = "http://" + serverIP + ":" + serverPort + Constants.GET_JOB_URL + "?corporateid=" + Constants.CORPORATE_ID + "&cpuid=" + imei + "&DeviceToken=" + deviceToken + "&commnandtype=" + Constants.TEMPLATE_UPLOAD_JOB_COMM + "&DataCount=1";//1000
            response = httpGetRequest(url);
            if (!response.isEmpty() && response.trim().length() > 0) {
                ArrayList <TemplateUploadTypeInfo> tempUploadTypeInfoList = null;
                tempUploadTypeInfoList = JSONCreatorParser.parseTemplateUploadJson(response, tempUploadTypeInfoList);
                if (tempUploadTypeInfoList != null) {
                    int size = tempUploadTypeInfoList.size();
                    if (size > 0) {
                        for (int i = 0; i < size; i++) {
                            TemplateUploadTypeInfo templateUploadInfo = tempUploadTypeInfoList.get(i);
                            String uploadType = templateUploadInfo.getUploadType();
                            if (uploadType != null && uploadType.trim().length() > 0) {
                                String statusMsg = "";
                                String jobId = "", packetId = "";
                                switch (uploadType) {
                                    case "1":
                                        //========================== upload all template =====================//
                                        jobId = templateUploadInfo.getJobId();
                                        packetId = templateUploadInfo.getPacketId();
                                        AsyncTaskPostAllEmployeeFingerTemplate task1 = new AsyncTaskPostAllEmployeeFingerTemplate();
                                        task1.execute(jobId, packetId);
                                        break;
                                    case "2":
                                        //======================= upload new template =======================//
                                        jobId = templateUploadInfo.getJobId();
                                        packetId = templateUploadInfo.getPacketId();
                                        AsyncTaskPostNewFingerTemplate task2 = new AsyncTaskPostNewFingerTemplate();
                                        task2.execute(jobId, packetId);
                                        break;
                                    case "3":
                                        //=====================  upload employee wise template ===============//
                                        String empId = "", cardId = "", empName = "";
                                        jobId = templateUploadInfo.getJobId();
                                        packetId = templateUploadInfo.getPacketId();
                                        empId = templateUploadInfo.getEmpId();
                                        cardId = templateUploadInfo.getCardId();
                                        empName = templateUploadInfo.getEmpName();
                                        statusMsg = validateRecvData(templateUploadInfo);
                                        if (statusMsg.equals("0000")) {
                                            AsyncTaskPostEmployeeWiseEnrolledTemplate task3 = new AsyncTaskPostEmployeeWiseEnrolledTemplate();
                                            task3.execute(jobId, packetId, empId, cardId, empName);
                                        } else {
                                            jobId = templateUploadInfo.getJobId();
                                            String postJobJson = JSONCreatorParser.getPostJobJson(Constants.CORPORATE_ID, imei, deviceToken, jobId, statusMsg);
                                            if (postJobJson.trim().length() > 0) {
                                                url = "http://" + serverIP + ":" + serverPort + Constants.POST_UNFINISHED_JOB_URL;
                                                AsyncTaskJobCompletion postUnfinishedJobsTask = new AsyncTaskJobCompletion();
                                                postUnfinishedJobsTask.execute(url, postJobJson);
                                            }
                                            isTemplateUploadComplete = true;
                                        }
                                        break;
                                    default:
                                        statusMsg = "6711";//Invalid Upload Type
                                        jobId = templateUploadInfo.getJobId();
                                        String postJobJson = JSONCreatorParser.getPostJobJson(Constants.CORPORATE_ID, imei, deviceToken, jobId, statusMsg);
                                        if (postJobJson.trim().length() > 0) {
                                            url = "http://" + serverIP + ":" + serverPort + Constants.POST_UNFINISHED_JOB_URL;
                                            AsyncTaskJobCompletion postUnfinishedJobsTask = new AsyncTaskJobCompletion();
                                            postUnfinishedJobsTask.execute(url, postJobJson);
                                        }
                                        isTemplateUploadComplete = true;
                                        break;
                                }
                            } else {
                                String statusMsg = "6711";//Invalid Upload Type
                                String jobId = templateUploadInfo.getJobId();
                                String postJobJson = JSONCreatorParser.getPostJobJson(Constants.CORPORATE_ID, imei, deviceToken, jobId, statusMsg);
                                if (postJobJson.trim().length() > 0) {
                                    url = "http://" + serverIP + ":" + serverPort + Constants.POST_UNFINISHED_JOB_URL;
                                    AsyncTaskJobCompletion postUnfinishedJobsTask = new AsyncTaskJobCompletion();
                                    postUnfinishedJobsTask.execute(url, postJobJson);
                                }
                                isTemplateUploadComplete = true;
                            }
                        }
                    } else {
                        isTemplateUploadComplete = true;
                    }
                } else {
                    isTemplateUploadComplete = true;
                }
            } else {
                isTemplateUploadComplete = true;
            }
            return null;
        }
    }

    private class AsyncTaskPostAllEmployeeFingerTemplate extends AsyncTask <String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            HashMap <Integer, String> fingerIdFingerDataMap = null;
            String jobId = params[0];
            String packetId = params[1];
            String url = "http://" + serverIP + ":" + serverPort + Constants.TEMPLATE_UPLOAD_URL;
            ArrayList <TemplateUploadInfo> templateUploadInfoList = null;
            templateUploadInfoList = dbComm.getAllEnrolledTemplates(templateUploadInfoList);//Database Communication
            if (templateUploadInfoList != null) {
                int size = templateUploadInfoList.size();
                if (size > 0) {
                    fingerIdFingerDataMap = JSONCreatorParser.createTemplateUploadJson(imei, deviceToken, Constants.TEMPLATE_UPLOAD_JOB_COMM, Constants.CORPORATE_ID, packetId, templateUploadInfoList, fingerIdFingerDataMap);
                    if (fingerIdFingerDataMap != null) {
                        size = fingerIdFingerDataMap.size();
                        if (size > 0) {
                            for (Map.Entry <Integer, String> entry : fingerIdFingerDataMap.entrySet()) {
                                PostOnDemandEnrolledTemplate post = new PostOnDemandEnrolledTemplate();
                                post.execute(url, jobId, entry.getKey().toString(), entry.getValue());
                            }
                        }
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            isTemplateUploadComplete = true;
        }
    }

    private class AsyncTaskPostNewFingerTemplate extends AsyncTask <String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            HashMap <Integer, String> fingerIdFingerDataMap = null;
            String jobId = params[0];
            String packetId = params[1];
            String url = "http://" + serverIP + ":" + serverPort + Constants.TEMPLATE_UPLOAD_URL;
            ArrayList <TemplateUploadInfo> templateUploadInfoList = null;
            templateUploadInfoList = dbComm.getNewEnrolledTemplates(templateUploadInfoList);//Database Communication
            if (templateUploadInfoList != null) {
                int size = templateUploadInfoList.size();
                if (size > 0) {
                    fingerIdFingerDataMap = JSONCreatorParser.createTemplateUploadJson(imei, deviceToken, Constants.TEMPLATE_UPLOAD_JOB_COMM, Constants.CORPORATE_ID, packetId, templateUploadInfoList, fingerIdFingerDataMap);
                    if (fingerIdFingerDataMap != null) {
                        size = fingerIdFingerDataMap.size();
                        if (size > 0) {
                            for (Map.Entry <Integer, String> entry : fingerIdFingerDataMap.entrySet()) {
                                PostOnDemandEnrolledTemplate post = new PostOnDemandEnrolledTemplate();
                                post.execute(url, jobId, entry.getKey().toString(), entry.getValue());
                            }
                        }
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            isTemplateUploadComplete = true;
        }
    }


    private class AsyncTaskPostEmployeeWiseEnrolledTemplate extends AsyncTask <String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            HashMap <Integer, String> fingerIdFingerDataMap = null;
            String jobId = params[0];
            String packetId = params[1];
            String empId = params[2];
            String cardId = params[3];
            String empName = params[4];
            String url = "http://" + serverIP + ":" + serverPort + Constants.TEMPLATE_UPLOAD_URL;
            ArrayList <TemplateUploadInfo> templateUploadInfoList = null;
            templateUploadInfoList = dbComm.geTemplateEmployeeWise(empId, cardId, empName, templateUploadInfoList);//Database Communication
            if (templateUploadInfoList != null) {
                int size = templateUploadInfoList.size();
                if (size > 0) {
                    fingerIdFingerDataMap = JSONCreatorParser.createTemplateUploadJson(imei, deviceToken, Constants.TEMPLATE_UPLOAD_JOB_COMM, Constants.CORPORATE_ID, packetId, templateUploadInfoList, fingerIdFingerDataMap);
                    if (fingerIdFingerDataMap != null) {
                        size = fingerIdFingerDataMap.size();
                        if (size > 0) {
                            for (Map.Entry <Integer, String> entry : fingerIdFingerDataMap.entrySet()) {
                                PostOnDemandEnrolledTemplate post = new PostOnDemandEnrolledTemplate();
                                post.execute(url, jobId, entry.getKey().toString(), entry.getValue());
                            }
                        }
                    }
                }
            } else {
                String statusMsg = "6712";//Template not found for received card Id
                String postJobJson = JSONCreatorParser.getPostJobJson(Constants.CORPORATE_ID, imei, deviceToken, jobId, statusMsg);
                if (postJobJson.trim().length() > 0) {
                    url = "http://" + serverIP + ":" + serverPort + Constants.POST_UNFINISHED_JOB_URL;
                    AsyncTaskJobCompletion postUnfinishedJobsTask = new AsyncTaskJobCompletion();
                    postUnfinishedJobsTask.execute(url, postJobJson);
                }
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            isTemplateUploadComplete = true;
        }
    }


    private class PostOnDemandEnrolledTemplate extends AsyncTask <String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            int status = -1;
            String url = params[0];
            String jobId = params[1];
            String fingerId = params[2];
            String fingerData = params[3];
            String response = httpPostRequest(url, fingerData);
            if (!response.isEmpty() && response.trim().length() > 0) {
                try {
                    JSONObject jPID = new JSONObject(fingerData);
                    String sendPid = jPID.getString("PID");
                    JSONObject reader = new JSONObject(response);
                    String recvPid = reader.getString("Result");
                    if (!recvPid.contains("null")) {
                        if (recvPid.equals(sendPid)) {
                            status = dbComm.updateFingerTemplateTable(fingerId);//Database Communication
                            String statusMsg = "0000";
                            String postJobJson = JSONCreatorParser.getPostJobJson(Constants.CORPORATE_ID, imei, deviceToken, jobId, statusMsg);
                            if (postJobJson.trim().length() > 0) {
                                url = "http://" + serverIP + ":" + serverPort + Constants.POST_UNFINISHED_JOB_URL;
                                AsyncTaskJobCompletion postUnfinishedJobsTask = new AsyncTaskJobCompletion();
                                postUnfinishedJobsTask.execute(url, postJobJson);
                            }
                        }
                    }
                } catch (JSONException e) {
                }
            }
            return status;
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (result != -1) {
                Log.d("TEST", "Template Upload Successfully");
            } else {
                Log.d("TEST", "Template Upload Failure");
            }
        }
    }


    //===============================  Template Download Thread Start ====================================//


    private class TemplateDownloadRequestThread implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    boolean status = false;
                    status = isNetworkAvailable();
                    if (status) {
                        status = isInternetWorking();
                        if (status) {
                            if (isTemplateDownloadComplete && !EmployeeFingerEnrollmentActivity.isFingerEnrollmentWindowVisisble) {
                                isTemplateDownloadComplete = false;
                                GetTemplateDownloadRequestTask getTemplateDownloadRequestTask = new GetTemplateDownloadRequestTask();
                                getTemplateDownloadRequestTask.execute();
                            }
                        }
                    }
                } catch (Exception e) {
                }
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ie) {
                }
            }
        }
    }


    private class GetTemplateDownloadRequestTask extends AsyncTask <Void, Void, Void> {

        ProgressDialog mypDialog;
        boolean isTemplateInserted = false;

        @Override
        protected Void doInBackground(Void... voids) {
            String response = "";
            String strServerUrl = "http://" + serverIP + ":" + serverPort + Constants.GET_JOB_URL + "?corporateid=" + Constants.CORPORATE_ID + "&cpuid=" + imei + "&DeviceToken=" + deviceToken + "&commnandtype=" + Constants.TEMPLATE_DOWNLOAD_JOB_COMM + "&DataCount=1000";
            response = httpGetRequest(strServerUrl);
            if (response != null && !response.isEmpty() && response.trim().length() > 0) {
                isTemplateDownloadRunning = true;
                ArrayList <TemplateDownloadInfo> tempDownloadInfoList = null;
                tempDownloadInfoList = JSONCreatorParser.parseTemplateDownloadJson(response, tempDownloadInfoList);
                if (tempDownloadInfoList != null) {
                    int size = tempDownloadInfoList.size();
                    if (size > 0) {
                        EmployeeAttendanceActivity.stopHandler();
                        final Context context = EmployeeAttendanceActivity.context;
                        if (context != null) {
                            final Activity activity = (Activity) context;
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (!activity.isFinishing()) {
                                        mypDialog = new ProgressDialog(context);
                                        mypDialog.setMessage("Template Downloading Wait...");
                                        mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                        mypDialog.setCanceledOnTouchOutside(false);
                                        mypDialog.show();
                                    }
                                }
                            });
                        }
                        String jobCode = Constants.TEMPLATE_DOWNLOAD_GET_JOB;
                        for (int i = 0; i < size; i++) {
                            TemplateDownloadInfo templateDownloadInfo = tempDownloadInfoList.get(i);
                            if (templateDownloadInfo != null) {
                                String statusMsg = validateFmd(jobCode, templateDownloadInfo.getFmd());
                                if (statusMsg.equals("0000")) {
                                    statusMsg = validateRecvData(jobCode, templateDownloadInfo);
                                    if (statusMsg.equals("0000")) {
                                        String sl = Utility.getSecurityLevelByVal(templateDownloadInfo.getSecurityLevel());
                                        String fi = Utility.getFingerIndexByVal(templateDownloadInfo.getFingerIndex());
                                        String vm = Utility.getVerificationModeByVal(templateDownloadInfo.getVerificationMode());
                                        String fq = Utility.getFingerQualityByVal(templateDownloadInfo.getFingerQuality());
                                        templateDownloadInfo.setSecurityLevel(sl);//Set Security Level
                                        templateDownloadInfo.setFingerIndex(fi);//Set Finger Index
                                        templateDownloadInfo.setVerificationMode(vm);//Set Verification Mode
                                        templateDownloadInfo.setFingerQuality(fq);//Set Finger Quality
                                        int autoId = dbComm.getAutoIdByEmpId(templateDownloadInfo.getEmpId());//Database Communication
                                        if (autoId == -1) {
                                            templateDownloadInfo = dbComm.insertRemotelyEnrolledEmployeeData(templateDownloadInfo);//Database Communication
                                        } else {
                                            templateDownloadInfo.setEnrollmentNo(autoId);
                                            templateDownloadInfo = dbComm.updateEmployeeData(templateDownloadInfo);//Database Communication
                                        }
                                        if (templateDownloadInfo.getEnrollmentNo() != -1) {
                                            int id = -1;
                                            String fingerType = templateDownloadInfo.getFingerType();
                                            if (fingerType != null && fingerType.equals("F1")) {
                                                id = dbComm.checkTemplateExistsByFT(autoId, "1");//Database Communication
                                                if (id == -1) {
                                                    templateDownloadInfo = dbComm.insertRemoteEnrolledTemplate(templateDownloadInfo);//Database Communication
                                                    MorphoDevice morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
                                                    MorphoDatabase morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
                                                    if (morphoDevice != null && morphoDatabase != null) {
                                                        boolean isIdentificationStarted=ProcessInfo.getInstance().isIdentificationStarted();
                                                        if (isIdentificationStarted) {
                                                            morphoComm.stopFingerIdentification();
                                                        }
                                                        TemplateList tempList = new TemplateList();
                                                        byte[] templateData = Utility.hexStringToByteArray(templateDownloadInfo.getFmd());
                                                        Template template = new Template();
                                                        template.setTemplateType(TemplateType.MORPHO_PK_ISO_FMR);
                                                        template.setData(templateData);
                                                        template.setDataIndex(0);
                                                        tempList.putTemplate(template);
                                                        int ret = morphoComm.insertOneRemoteTemplateToMorphoDB(tempList, templateDownloadInfo);
                                                        Log.d("TEST", fingerType + " Morpho Insert Result:" + ret);
                                                        if (ret == 0) {
                                                            isTemplateInserted=true;
                                                            templateDownloadInfo = dbComm.updateFingerEnrolledStatusToEmpTbl(autoId, templateDownloadInfo);//Database Communication
                                                            statusMsg = "0000";
                                                            String url = "http://" + serverIP + ":" + serverPort + Constants.POST_UNFINISHED_JOB_URL;
                                                            String postJobJson = JSONCreatorParser.getPostJobJson(Constants.CORPORATE_ID, imei, deviceToken, templateDownloadInfo.getJobId(), statusMsg);
                                                            AsyncTaskJobCompletion postUnfinishedJobsTask = new AsyncTaskJobCompletion();
                                                            postUnfinishedJobsTask.execute(url, postJobJson);
                                                        } else {
                                                            statusMsg = "0000";
                                                            String url = "http://" + serverIP + ":" + serverPort + Constants.POST_UNFINISHED_JOB_URL;
                                                            String postJobJson = JSONCreatorParser.getPostJobJson(Constants.CORPORATE_ID, imei, deviceToken, templateDownloadInfo.getJobId(), statusMsg);
                                                            AsyncTaskJobCompletion postUnfinishedJobsTask = new AsyncTaskJobCompletion();
                                                            postUnfinishedJobsTask.execute(url, postJobJson);
                                                        }
                                                    }
                                                } else {
                                                    templateDownloadInfo = dbComm.updateOneRemoteEnrolledTemplate(id, templateDownloadInfo);//Database Communication
                                                    MorphoDevice morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
                                                    MorphoDatabase morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
                                                    if (morphoDevice != null && morphoDatabase != null) {
                                                        boolean isIdentificationStarted=ProcessInfo.getInstance().isIdentificationStarted();
                                                        if (isIdentificationStarted) {
                                                            morphoComm.stopFingerIdentification();
                                                        }
                                                        TemplateList tempList = new TemplateList();
                                                        byte[] templateData = Utility.hexStringToByteArray(templateDownloadInfo.getFmd());
                                                        Template template = new Template();
                                                        template.setTemplateType(TemplateType.MORPHO_PK_ISO_FMR);
                                                        template.setData(templateData);
                                                        template.setDataIndex(0);
                                                        tempList.putTemplate(template);
                                                        int ret = morphoComm.insertOneRemoteTemplateToMorphoDB(tempList, templateDownloadInfo);
                                                        Log.d("TEST", fingerType + " Morpho Update Result:" + ret);
                                                        if (ret == 0) {
                                                            isTemplateInserted=true;
                                                            templateDownloadInfo = dbComm.updateFingerEnrolledStatusToEmpTbl(autoId, templateDownloadInfo);//Database Communication
                                                            statusMsg = "0000";
                                                            String url = "http://" + serverIP + ":" + serverPort + Constants.POST_UNFINISHED_JOB_URL;
                                                            String postJobJson = JSONCreatorParser.getPostJobJson(Constants.CORPORATE_ID, imei, deviceToken, templateDownloadInfo.getJobId(), statusMsg);
                                                            AsyncTaskJobCompletion postUnfinishedJobsTask = new AsyncTaskJobCompletion();
                                                            postUnfinishedJobsTask.execute(url, postJobJson);
                                                        } else {
                                                            statusMsg = "0000";
                                                            String url = "http://" + serverIP + ":" + serverPort + Constants.POST_UNFINISHED_JOB_URL;
                                                            String postJobJson = JSONCreatorParser.getPostJobJson(Constants.CORPORATE_ID, imei, deviceToken, templateDownloadInfo.getJobId(), statusMsg);
                                                            AsyncTaskJobCompletion postUnfinishedJobsTask = new AsyncTaskJobCompletion();
                                                            postUnfinishedJobsTask.execute(url, postJobJson);
                                                        }
                                                    }
                                                }

                                            } else if (fingerType != null && fingerType.equals("F2")) {
                                                id = dbComm.checkTemplateExistsByFT(autoId, "2");//Database Communication
                                                if (id == -1) {
                                                    templateDownloadInfo = dbComm.insertRemoteEnrolledTemplate(templateDownloadInfo);//Database Communication
                                                    MorphoDevice morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
                                                    MorphoDatabase morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
                                                    if (morphoDevice != null && morphoDatabase != null) {
                                                        boolean isIdentificationStarted=ProcessInfo.getInstance().isIdentificationStarted();
                                                        if (isIdentificationStarted) {
                                                            morphoComm.stopFingerIdentification();
                                                        }
                                                        TemplateList tempList = new TemplateList();
                                                        byte[] templateData = Utility.hexStringToByteArray(templateDownloadInfo.getFmd());
                                                        Template template = new Template();
                                                        template.setTemplateType(TemplateType.MORPHO_PK_ISO_FMR);
                                                        template.setData(templateData);
                                                        template.setDataIndex(0);
                                                        tempList.putTemplate(template);
                                                        int ret = morphoComm.insertOneRemoteTemplateToMorphoDB(tempList, templateDownloadInfo);
                                                        Log.d("TEST", fingerType + " Morpho Insert Result:" + ret);
                                                        if (ret == 0) {
                                                            isTemplateInserted=true;
                                                            templateDownloadInfo = dbComm.updateFingerEnrolledStatusToEmpTbl(autoId, templateDownloadInfo);//Database Communication
                                                            statusMsg = "0000";
                                                            String url = "http://" + serverIP + ":" + serverPort + Constants.POST_UNFINISHED_JOB_URL;
                                                            String postJobJson = JSONCreatorParser.getPostJobJson(Constants.CORPORATE_ID, imei, deviceToken, templateDownloadInfo.getJobId(), statusMsg);
                                                            AsyncTaskJobCompletion postUnfinishedJobsTask = new AsyncTaskJobCompletion();
                                                            postUnfinishedJobsTask.execute(url, postJobJson);
                                                        } else {
                                                            statusMsg = "0000";
                                                            String url = "http://" + serverIP + ":" + serverPort + Constants.POST_UNFINISHED_JOB_URL;
                                                            String postJobJson = JSONCreatorParser.getPostJobJson(Constants.CORPORATE_ID, imei, deviceToken, templateDownloadInfo.getJobId(), statusMsg);
                                                            AsyncTaskJobCompletion postUnfinishedJobsTask = new AsyncTaskJobCompletion();
                                                            postUnfinishedJobsTask.execute(url, postJobJson);
                                                        }
                                                    }
                                                } else {
                                                    templateDownloadInfo = dbComm.updateOneRemoteEnrolledTemplate(id, templateDownloadInfo);//Database Communication
                                                    MorphoDevice morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
                                                    MorphoDatabase morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
                                                    if (morphoDevice != null && morphoDatabase != null) {
                                                        boolean isIdentificationStarted=ProcessInfo.getInstance().isIdentificationStarted();
                                                        if (isIdentificationStarted) {
                                                            morphoComm.stopFingerIdentification();
                                                        }
                                                        TemplateList tempList = new TemplateList();
                                                        byte[] templateData = Utility.hexStringToByteArray(templateDownloadInfo.getFmd());
                                                        Template template = new Template();
                                                        template.setTemplateType(TemplateType.MORPHO_PK_ISO_FMR);
                                                        template.setData(templateData);
                                                        template.setDataIndex(0);
                                                        tempList.putTemplate(template);
                                                        int ret = morphoComm.insertOneRemoteTemplateToMorphoDB(tempList, templateDownloadInfo);
                                                        Log.d("TEST", fingerType + " Morpho Update Result:" + ret);
                                                        if (ret == 0) {
                                                            isTemplateInserted=true;
                                                            templateDownloadInfo = dbComm.updateFingerEnrolledStatusToEmpTbl(autoId, templateDownloadInfo);//Database Communication
                                                            statusMsg = "0000";
                                                            String url = "http://" + serverIP + ":" + serverPort + Constants.POST_UNFINISHED_JOB_URL;
                                                            String postJobJson = JSONCreatorParser.getPostJobJson(Constants.CORPORATE_ID, imei, deviceToken, templateDownloadInfo.getJobId(), statusMsg);
                                                            AsyncTaskJobCompletion postUnfinishedJobsTask = new AsyncTaskJobCompletion();
                                                            postUnfinishedJobsTask.execute(url, postJobJson);
                                                        } else {
                                                            statusMsg = "0000";
                                                            String url = "http://" + serverIP + ":" + serverPort + Constants.POST_UNFINISHED_JOB_URL;
                                                            String postJobJson = JSONCreatorParser.getPostJobJson(Constants.CORPORATE_ID, imei, deviceToken, templateDownloadInfo.getJobId(), statusMsg);
                                                            AsyncTaskJobCompletion postUnfinishedJobsTask = new AsyncTaskJobCompletion();
                                                            postUnfinishedJobsTask.execute(url, postJobJson);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        String url = "http://" + serverIP + ":" + serverPort + Constants.POST_UNFINISHED_JOB_URL;
                                        String postJobJson = JSONCreatorParser.getPostJobJson(Constants.CORPORATE_ID, imei, deviceToken, templateDownloadInfo.getJobId(), statusMsg);
                                        AsyncTaskJobCompletion postUnfinishedJobsTask = new AsyncTaskJobCompletion();
                                        postUnfinishedJobsTask.execute(url, postJobJson);
                                    }
                                } else {
                                    String url = "http://" + serverIP + ":" + serverPort + Constants.POST_UNFINISHED_JOB_URL;
                                    String postJobJson = JSONCreatorParser.getPostJobJson(Constants.CORPORATE_ID, imei, deviceToken, templateDownloadInfo.getJobId(), statusMsg);
                                    AsyncTaskJobCompletion postUnfinishedJobsTask = new AsyncTaskJobCompletion();
                                    postUnfinishedJobsTask.execute(url, postJobJson);
                                }
                            }
                        }
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (mypDialog != null && mypDialog.isShowing()) {
                mypDialog.cancel();
            }
            if (context != null) {
                Activity activity = (Activity) context;
                if (activity != null && activity instanceof EmployeeAttendanceActivity && EmployeeAttendanceActivity.isAttendanceWindowVisisble) {
                    if (isTemplateInserted) {
                        EmployeeAttendanceActivity.stopHandler();
                        EmployeeAttendanceActivity.startHandler();
                    }
                }
            }
            isTemplateDownloadComplete = true;
            isTemplateDownloadRunning = false;
        }
    }


    //=============================  Remote Enrollment Request Thread Start =========================//

    class RemoteEnrollmentRequestThread implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    boolean status = false;
                    status = isNetworkAvailable();
                    if (status) {
                        status = isInternetWorking();
                        if (status) {
                            if (!EmployeeFingerEnrollmentActivity.isFingerEnrollmentWindowVisisble && !isTemplateDownloadRunning) {
                                RemoteEnrollmentAsyncTask task = new RemoteEnrollmentAsyncTask();
                                task.execute();
                            }
                        }
                    }
                } catch (Exception e) {
                }
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ie) {
                }
            }
        }
    }

    public class RemoteEnrollmentAsyncTask extends AsyncTask <Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... voids) {
            String response = "";
            String serverUrl = "http://" + serverIP + ":" + serverPort + Constants.GET_JOB_URL + "?corporateid=" + Constants.CORPORATE_ID + "&cpuid=" + imei + "&DeviceToken=" + deviceToken + "&commnandtype=" + Constants.REMOTE_ENROLLMENT_JOB_COMM + "&DataCount=1";
            response = httpGetRequest(serverUrl);
            if (response != null && response.trim().length() > 0) {
                RemoteEnrollmentInfo remoteEnrollInfo = null;
                remoteEnrollInfo = JSONCreatorParser.parseRemoteEnrollData(response, remoteEnrollInfo);
                if (remoteEnrollInfo != null) {
                    String jobCode = Constants.REMOTE_ENROLLMENT_GET_JOB;
                    String statusMsg = validateRecvData(jobCode, remoteEnrollInfo);
                    if (statusMsg.equals("0000")) {
                        MorphoDevice morphoDevice = null;
                        MorphoDatabase morphoDatabase = null;
                        String et = remoteEnrollInfo.getEnrollmentType();
                        switch (et) {
                            case "E1"://Remote Enroll
                                remoteEnrollInfo.setFingerIndex(Utility.getFingerIndexByVal(remoteEnrollInfo.getFingerIndex()));
                                remoteEnrollInfo.setSecurityLevel(Utility.getSecurityLevelByVal(remoteEnrollInfo.getSecurityLevel()));
                                remoteEnrollInfo.setVerificationMode(Utility.getVerificationModeByVal(remoteEnrollInfo.getVerificationMode()));
                                morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
                                morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
                                if (morphoDevice != null && morphoDatabase != null) {
                                    boolean isIdentificationStarted=ProcessInfo.getInstance().isIdentificationStarted();
                                    boolean isVerificationStarted=ProcessInfo.getInstance().isVerificationStarted();
                                    boolean isBioCommandStarted=ProcessInfo.getInstance().isCommandBioStart();
                                    if ((isIdentificationStarted || isVerificationStarted) && isBioCommandStarted) {
                                        morphoComm.stopFingerIdentification();
                                        try {
                                            Thread.sleep(500);//1000
                                        } catch (InterruptedException ie) {
                                        }
                                    }
                                    if (context != null) {
                                        Activity activity = (Activity) context;
                                        EmployeeAttendanceActivity.stopHandler();
                                        Intent intent = new Intent(activity, EmployeeFingerEnrollmentActivity.class);
                                        intent.putExtra("RemoteEnrollInfo", remoteEnrollInfo);
                                        activity.startActivity(intent);
                                    }
                                }
                                break;
                            case "E2":
                                remoteEnrollInfo.setFingerIndex(Utility.getFingerIndexByVal(remoteEnrollInfo.getFingerIndex()));
                                remoteEnrollInfo.setSecurityLevel(Utility.getSecurityLevelByVal(remoteEnrollInfo.getSecurityLevel()));
                                remoteEnrollInfo.setVerificationMode(Utility.getVerificationModeByVal(remoteEnrollInfo.getVerificationMode()));
                                morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
                                morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
                                if (morphoDevice != null && morphoDatabase != null) {
                                    boolean isIdentificationStarted=ProcessInfo.getInstance().isIdentificationStarted();
                                    boolean isVerificationStarted=ProcessInfo.getInstance().isVerificationStarted();
                                    boolean isBioCommandStarted=ProcessInfo.getInstance().isCommandBioStart();
                                    if ((isIdentificationStarted || isVerificationStarted) && isBioCommandStarted) {
                                        morphoComm.stopFingerIdentification();
                                        try {
                                            Thread.sleep(500);//1000
                                        } catch (InterruptedException ie) {
                                        }
                                    }
                                    String ft = remoteEnrollInfo.getFingerType();
                                    if (ft != null && ft.equals("F1")) {
                                        String empId = remoteEnrollInfo.getEmpId().trim();
                                        int autoId = dbComm.getAutoIdByEmpId(empId);
                                        if (autoId != -1) {
                                            map.clear();
                                            int status = dbComm.deleteEmployeeDataByAutoId(autoId);
                                            if (status != -1) {
                                                status = dbComm.deleteFingerRecordByAutoId(autoId);
                                                if (status != -1) {
                                                    int ret = morphoComm.deleteMorphoUser(empId);
                                                    if (ret == ErrorCodes.MORPHO_OK) {
                                                        map.put("EmpId", empId);
                                                    }
                                                }
                                            }
                                        }
                                    } else if (ft != null && ft.equals("F2")) {
                                        String mapEmpId = map.get("EmpId");
                                        if (mapEmpId != null) {
                                            map.clear();
                                        }
                                    }
                                    if (context != null) {
                                        Activity activity = (Activity) context;
                                        EmployeeAttendanceActivity.stopHandler();
                                        Intent intent = new Intent(activity, EmployeeFingerEnrollmentActivity.class);
                                        intent.putExtra("RemoteEnrollInfo", remoteEnrollInfo);
                                        activity.startActivity(intent);
                                    }
                                }
                                break;
                            case "E3"://Remote Dummy Enroll
                                break;
                            default:
                                statusMsg = "6815";
                                String url = "http://" + serverIP + ":" + serverPort + Constants.POST_UNFINISHED_JOB_URL;
                                String jobId = remoteEnrollInfo.getJobId();
                                String postJobJson = JSONCreatorParser.getPostJobJson(Constants.CORPORATE_ID, imei, deviceToken, jobId, statusMsg);
                                AsyncTaskJobCompletion task = new AsyncTaskJobCompletion();
                                task.execute(url, postJobJson);
                                break;
                        }
                    } else {
                        String url = "http://" + serverIP + ":" + serverPort + Constants.POST_UNFINISHED_JOB_URL;
                        String jobId = remoteEnrollInfo.getJobId();
                        String postJobJson = JSONCreatorParser.getPostJobJson(Constants.CORPORATE_ID, imei, deviceToken, jobId, statusMsg);
                        AsyncTaskJobCompletion task = new AsyncTaskJobCompletion();
                        task.execute(url, postJobJson);
                    }
                }
            }
            return null;
        }
    }

    //============================== Device Status Send ================================//

    class DeviceStatusSendThread implements Runnable {
        @Override
        public void run() {
            while (true) {
                DeviceStatusSendTask task = new DeviceStatusSendTask();
                task.execute();
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException ie) {
                }
            }
        }
    }

    class DeviceStatusSendTask extends AsyncTask <Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            Boolean status = false;
            String requestJson = "", responseJson = "";
            DeviceStatusInfo deviceStatusInfo = new DeviceStatusInfo();
            deviceStatusInfo = fillDeviceInfoStatus(deviceStatusInfo);
            if (deviceStatusInfo != null) {
                requestJson = JSONCreatorParser.createDeviceStatusJson(deviceStatusInfo);
                if (requestJson != null && !requestJson.isEmpty() && requestJson.trim().length() > 0) {
                    String url = "http://" + serverIP + ":" + serverPort + Constants.POST_DEVICE_STATUS_URL;
                    responseJson = httpPostRequest(url, requestJson);
                    JSONObject responseJsonObj = null;
                    try {
                        responseJsonObj = new JSONObject(responseJson);
                        String recvPid = responseJsonObj.getString("ExtraInfo");
                        String recvMessage = responseJsonObj.getString("Message");
                        if (recvPid != null && recvPid.trim().length() > 0) {
                            String sendPid = deviceStatusInfo.getPid();
                            if (recvPid.equals(sendPid) && recvMessage.equals("Device status update successfully")) {
                                status = true;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            return status;
        }

        @Override
        protected void onPostExecute(Boolean status) {
            super.onPostExecute(status);
            if (status) {
                Log.d("TEST", "Device Status Send Successfully");
            } else {
                Log.d("TEST", "Device Status Send Failure");
            }
        }

        private DeviceStatusInfo fillDeviceInfoStatus(DeviceStatusInfo deviceStatusInfo) {

            Random random = new Random();
            String pid = String.format("%04d", random.nextInt(10000));
            deviceStatusInfo.setCorporateId(Constants.CORPORATE_ID);
            deviceStatusInfo.setImei(imei);
            deviceStatusInfo.setPid(pid);
            deviceStatusInfo.setCommandType(Constants.POST_DEVICE_STATUS_COMM);
            deviceStatusInfo.setDeviceAdd("01");
            deviceStatusInfo.setGvm("02");
            deviceStatusInfo.setDeviceToken(deviceToken);
            boolean isReaderInstalled = dbComm.isSmartCardReaderInstalled();//Database Communication
            if (isReaderInstalled) {
                deviceStatusInfo.setIsSmartReaderInstalled("1");
            } else {
                deviceStatusInfo.setIsSmartReaderInstalled("0");
            }
            int totalFingerEnrolledUsers = dbComm.getTotalFingerEnrolledUser();//Database Communication
            if (totalFingerEnrolledUsers != -1) {
                deviceStatusInfo.setTotalEnrolledUsers(Integer.toString(totalFingerEnrolledUsers));
            } else {
                deviceStatusInfo.setTotalEnrolledUsers("0");
            }
            deviceStatusInfo.setEstdCode("00000001");
            deviceStatusInfo.setFirmware("V040626");
            deviceStatusInfo.setFirmwareId("MBBV3-H27-I10411241E");
            deviceStatusInfo.setGprsOperator("AIRTEL");
            deviceStatusInfo.setGprsSignal("93");
            String IPAddress = Utility.getDeviceIPAddress();
            if (IPAddress != null && IPAddress.trim().length() > 0) {
                deviceStatusInfo.setIpAddress(IPAddress);
            } else {
                deviceStatusInfo.setIpAddress("");
            }
            deviceStatusInfo.setSimNo("9563987634");
            deviceStatusInfo.setTimeZone("+0530");
            MorphoDevice morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
            MorphoDatabase morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
            if (morphoDevice != null && morphoDatabase != null) {
                Long l = new Long(0);
                int ret = morphoDatabase.getNbUsedRecord(l);
                if (ret == 0) {
                    deviceStatusInfo.setTotalTemplate(Long.toString(l));
                } else {
                    deviceStatusInfo.setTotalTemplate("0");
                }
            } else {
                deviceStatusInfo.setTotalTemplate("0");
            }
            int totalEnrolledUsers = dbComm.getTotalEnrolledUsers();//Database Communication
            if (totalEnrolledUsers != -1) {
                deviceStatusInfo.setTotalUser(Integer.toString(totalEnrolledUsers));
            } else {
                deviceStatusInfo.setTotalUser("0");
            }
            int totalUnSendAttendanceRecords = dbComm.getTotalUnSendRecords();//Database Communication
            if (totalUnSendAttendanceRecords != -1) {
                deviceStatusInfo.setUnCapRecord(Integer.toString(totalUnSendAttendanceRecords));
            } else {
                deviceStatusInfo.setUnCapRecord("0");
            }
            return deviceStatusInfo;
        }
    }

    //============================= Get Sign On Message Thread ========================//

    class GetSignOnMessageThread implements Runnable {
        @Override
        public void run() {
            while (true) {
                GetSignOnMessageTask task = new GetSignOnMessageTask();
                task.execute();
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException ie) {
                }
            }
        }
    }


    class GetSignOnMessageTask extends AsyncTask <Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            String response = "";
            String url = "http://" + serverIP + ":" + serverPort + Constants.GET_JOB_URL + "?corporateid=" + Constants.CORPORATE_ID + "&cpuid=" + imei + "&DeviceToken=" + deviceToken + "&commnandtype=" + Constants.GET_SIGN_ON_MESSAGE_COMM + "&DataCount=1000";
            response = httpGetRequest(url);
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            if (response != null && !response.isEmpty() && response.trim().length() > 0) {
                SignOnMessageInfo signOnMessageInfo = null;
                signOnMessageInfo = JSONCreatorParser.parseSignOnMessageJson(response, signOnMessageInfo);
                if (signOnMessageInfo != null) {
                    String pid = signOnMessageInfo.getPid();
                    String message = signOnMessageInfo.getMessage();
                    String msg = "0000";
                    String postJobJson = JSONCreatorParser.getPostJobJson(Constants.CORPORATE_ID, imei, deviceToken, pid, msg);
                    String url = "http://" + serverIP + ":" + serverPort + Constants.POST_UNFINISHED_JOB_URL;
                    AsyncTaskJobCompletion postUnfinishedJobsTask = new AsyncTaskJobCompletion();
                    postUnfinishedJobsTask.execute(url, postJobJson);
                }
            }
        }
    }

    //================================== Job Completion =====================================//

    private class AsyncTaskJobCompletion extends AsyncTask <String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            String response = "";
            String url = params[0].trim();
            String reqJson = params[1].trim();
            response = httpPostRequest(url, reqJson);
            return null;
        }
    }


    //============================== Employee Validation Upload Thread =====================//

    //============================== Not implemented in server side  ============================//

    class AutoEmployeeValidationUploadThread implements Runnable {
        @Override
        public void run() {
            while (true) {
                Random random = new Random();
                String pid = String.format("%04d", random.nextInt(10000));
                EmpValidationDownloadInfo empInfo = null;
                empInfo = dbComm.getEmployeeData(empInfo);//Database Communication
                if (empInfo != null) {
                    String reqJson = JSONCreatorParser.getEmpValUploadJsonData(pid, imei, Constants.EMP_VALIDATION_JOB_UPLOAD_COMM, empInfo);
                    if (reqJson != null && reqJson.trim().length() > 0) {
                        String serverUrl = "";//Need to be declared by ezee hr team
                        if (!serverUrl.isEmpty() && serverUrl.trim().length() > 0) {
                            AsyncTaskEmployeeValidationUpload task = new AsyncTaskEmployeeValidationUpload();
                            task.execute(serverUrl, pid, reqJson);
                        }
                    }
                }
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException ie) {
                }
            }
        }
    }

    class AsyncTaskEmployeeValidationUpload extends AsyncTask <String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            String status = "";
            String response = "";
            String url = params[0].trim();
            String pid = params[1].trim();
            String reqJson = params[2].trim();
            response = httpPostRequest(url, reqJson);
            if (!response.isEmpty() && response.trim().length() > 0) {
                try {
                    JSONObject reader = new JSONObject(response);
                    String recvPid = reader.getString("Result");
                    if (!recvPid.contains("null")) {
                        int recvPidVal = Integer.parseInt(recvPid);
                        int sendPidVal = Integer.parseInt(pid);
                        if (recvPidVal == sendPidVal) {
                            status = "S";
                        } else {
                            status = "F";
                        }
                    }
                } catch (JSONException e) {
                    //log(e.getMessage() + "\n");
                }
            }
            return status;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
        }
    }

    //==============================  Validation Received Json Data From EzeeHrLite Service Api  ==================================//


    private String validateRecvData(TemplateUploadTypeInfo templateUploadInfo) {

        String statusMsg = "0000";

        if (templateUploadInfo.getEmpId() != null && (templateUploadInfo.getEmpId().trim().length() == 0 || templateUploadInfo.getEmpId().trim().length() > 16)) {
            statusMsg = "6501";
            return statusMsg;
        }

        if (templateUploadInfo.getCardId() != null && (templateUploadInfo.getCardId().trim().length() == 0 || templateUploadInfo.getCardId().trim().length() > 8)) {
            statusMsg = "6502";
            return statusMsg;
        }

        if (templateUploadInfo.getEmpName() != null && (templateUploadInfo.getEmpName().trim().length() == 0 || templateUploadInfo.getEmpName().trim().length() > 16)) {
            statusMsg = "6503";
            return statusMsg;
        }
        return statusMsg;
    }


    private String validateRecvData(EmpValidationDownloadInfo empInfo) {

        String statusMsg = "0000";

        if (empInfo.getEmpId() != null && (empInfo.getEmpId().trim().length() == 0 || empInfo.getEmpId().trim().length() > 16)) {
            statusMsg = "6501";
            return statusMsg;
        }

        if (empInfo.getCardId() != null && (empInfo.getCardId().trim().length() == 0 || empInfo.getCardId().trim().length() > 8)) {
            statusMsg = "6502";
            return statusMsg;
        }

        if (empInfo.getEmpName() != null && (empInfo.getEmpName().trim().length() == 0 || empInfo.getEmpName().trim().length() > 16)) {
            statusMsg = "6503";
            return statusMsg;
        }
        return statusMsg;
    }

    private String validateRecvData(String jobCode, TemplateDownloadInfo templateDownloadInfo) {

        String statusMsg = "0000";
        boolean isValid = false;
        int len = 0;

        String value = templateDownloadInfo.getEmpId();
        if (value != null) {
            len = value.trim().length();
            if (len == 0 || len > 16) {
                statusMsg = jobCode + "01";
                return statusMsg;
            }
        } else {
            statusMsg = jobCode + "01";
            return statusMsg;
        }

        value = templateDownloadInfo.getCardId();
        if (value != null) {
            len = value.trim().length();
            if (len == 0 || len > 8) {
                statusMsg = jobCode + "02";
                return statusMsg;
            }
        } else {
            statusMsg = jobCode + "02";
            return statusMsg;
        }

        value = templateDownloadInfo.getEmpName();
        if (value != null) {
            len = value.trim().length();
            if (len == 0 || len > 16) {
                statusMsg = jobCode + "03";
                return statusMsg;
            }
        } else {
            statusMsg = jobCode + "03";
            return statusMsg;
        }

        value = templateDownloadInfo.getFingerType();
        if (value != null && value.trim().length() > 0) {
            isValid = Utility.validateFingerType(value);
            if (!isValid) {
                statusMsg = jobCode + "06";
                return statusMsg;
            }
        } else {
            statusMsg = jobCode + "06";
            return statusMsg;
        }

        value = templateDownloadInfo.getFingerIndex();
        if (value != null && value.trim().length() > 0) {
            isValid = Utility.validateFingerIndex(value);
            if (!isValid) {
                statusMsg = jobCode + "08";
                return statusMsg;
            }
        } else {
            statusMsg = jobCode + "08";
            return statusMsg;
        }

        value = templateDownloadInfo.getFingerQuality();
        if (value != null && value.trim().length() > 0) {
            isValid = Utility.validateFingerQuality(value);
            if (!isValid) {
                statusMsg = jobCode + "09";
                return statusMsg;
            }
        } else {
            statusMsg = jobCode + "09";
            return statusMsg;
        }

        value = templateDownloadInfo.getSecurityLevel();
        if (value != null && value.trim().length() > 0) {
            isValid = Utility.validateSecurityLevel(value);
            if (!isValid) {
                statusMsg = jobCode + "07";
                return statusMsg;
            }
        } else {
            statusMsg = jobCode + "07";
            return statusMsg;
        }

        value = templateDownloadInfo.getVerificationMode();
        if (value != null && value.trim().length() > 0) {
            isValid = Utility.validateVerificationMode(value);
            if (!isValid) {
                statusMsg = jobCode + "05";
                return statusMsg;
            }
        } else {
            statusMsg = jobCode + "05";
            return statusMsg;
        }

        return statusMsg;
    }


    private String validateRecvData(String jobCode, RemoteEnrollmentInfo remoteEnrollInfo) {

        String statusMsg = "0000";
        boolean isValid = false;
        int len = 0;

        String value = remoteEnrollInfo.getEmpId();
        if (value != null) {
            len = value.trim().length();
            if (len == 0 || len > 16) {
                statusMsg = jobCode + "01";
                return statusMsg;
            }
        } else {
            statusMsg = jobCode + "01";
            return statusMsg;
        }

        value = remoteEnrollInfo.getCardId();
        if (value != null) {
            len = value.trim().length();
            if (len == 0 || len > 8) {
                statusMsg = jobCode + "02";
                return statusMsg;
            }
        } else {
            statusMsg = jobCode + "02";
            return statusMsg;
        }

        value = remoteEnrollInfo.getEmpName();
        if (value != null) {
            len = value.trim().length();
            if (len == 0 || len > 16) {
                statusMsg = jobCode + "03";
                return statusMsg;
            }
        } else {
            statusMsg = jobCode + "03";
            return statusMsg;
        }

        value = remoteEnrollInfo.getFingerType();
        if (value != null && value.trim().length() > 0) {
            isValid = Utility.validateFingerType(value);
            if (!isValid) {
                statusMsg = jobCode + "06";
                return statusMsg;
            }
        } else {
            statusMsg = jobCode + "06";
            return statusMsg;
        }

        value = remoteEnrollInfo.getFingerIndex();
        if (value != null && value.trim().length() > 0) {
            isValid = Utility.validateFingerIndex(value);
            if (!isValid) {
                statusMsg = jobCode + "08";
                return statusMsg;
            }
        } else {
            statusMsg = jobCode + "08";
            return statusMsg;
        }

        value = remoteEnrollInfo.getFingerQuality();
        if (value != null && value.trim().length() > 0) {
            isValid = Utility.validateFingerQuality(value);
            if (!isValid) {
                statusMsg = jobCode + "09";
                return statusMsg;
            }
        } else {
            statusMsg = jobCode + "09";
            return statusMsg;
        }

        value = remoteEnrollInfo.getSecurityLevel();
        if (value != null && value.trim().length() > 0) {
            isValid = Utility.validateSecurityLevel(value);
            if (!isValid) {
                statusMsg = jobCode + "07";
                return statusMsg;
            }
        } else {
            statusMsg = jobCode + "07";
            return statusMsg;
        }

        value = remoteEnrollInfo.getVerificationMode();
        if (value != null && value.trim().length() > 0) {
            isValid = Utility.validateVerificationMode(value);
            if (!isValid) {
                statusMsg = jobCode + "05";
                return statusMsg;
            }
        } else {
            statusMsg = jobCode + "05";
            return statusMsg;
        }
        return statusMsg;
    }


    private String validateFmd(String jobCode, String fmd) {

        String statsuMsg = "0000";
        int len = 0;

        if (fmd != null) {
            len = fmd.length();
            if (len == 0 || len > 512) {
                statsuMsg = jobCode + "10";
                return statsuMsg;
            }
        } else {
            statsuMsg = jobCode + "10";
            return statsuMsg;
        }

        return statsuMsg;
    }


    //==============================  Http Get Request  ==============================//

    public String httpGetRequest(String serverUrl) {
        // synchronized (this) {
        InputStream is = null;
        String response = "";
        HttpURLConnection conn = null;
        try {
            URL url = new URL(serverUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(30000 /* milliseconds */);
            conn.setConnectTimeout(30000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.connect();
            int resCode = conn.getResponseCode();
            if (resCode == HttpURLConnection.HTTP_OK) {
                is = conn.getInputStream();
                response = readInputStream(is);
            } else {
                //log("HTTP Server Response Not OK During Attendance Post\n");
            }
        } catch (Exception e) {
            // log(e.getMessage() + "\n");
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                    //log(e.getMessage() + "\n");
                }
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
        return response;
        // }
    }

    //==============================  Http Post Request  ==============================//

    private String httpPostRequest(String strServerUrl, String strJson) {
        // synchronized (this) {
        String responseJson = "";
        URL url;
        HttpURLConnection conn = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;
        BufferedWriter bufferedWriter = null;
        try {
            url = new URL(strServerUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(30000 /* milliseconds */);
            conn.setConnectTimeout(30000 /* milliseconds */);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestMethod("POST");
            conn.connect();
            //Write
            outputStream = conn.getOutputStream();
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            bufferedWriter.write(strJson);
            bufferedWriter.flush();
            outputStream.close();
            bufferedWriter.close();
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                inputStream = conn.getInputStream();
                responseJson = readInputStream(inputStream);
            } else {
                //log("HTTP Server Response Not OK During Attendance Post\n");
            }
        } catch (Exception e) {
            Log.d("TEST", "Post Exception:" + e.getMessage());
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
        return responseJson;
        // }
    }

    //==============================  Read Input Stream  ==============================//

    public String readInputStream(InputStream in) {

        String responseJSON = "";
        ByteArrayOutputStream buffer = null;
        try {
            buffer = new ByteArrayOutputStream();
            byte[] data = new byte[1024];
            int nRead;
            while ((nRead = in.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            byte[] bytes = buffer.toByteArray();
            in.close();
            responseJSON = new String(bytes, "UTF-8");
        } catch (Exception e) {
            //log(e.getMessage() + "\n");
        } finally {
            if (buffer != null) {
                try {
                    buffer.close();
                } catch (IOException e) {
                    // log(e.getMessage() + "\n");
                }
            }
        }
        return responseJSON;
    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private boolean isInternetWorking() {
        try {
            Process p1 = java.lang.Runtime.getRuntime().exec("ping -c 1 www.google.com");
            int returnVal = p1.waitFor();
            boolean reachable = (returnVal == 0);
            return reachable;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

}
