package com.android.fortunaattendancesystem.mqtt;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;

import com.android.fortunaattendancesystem.activities.EmployeeAttendanceActivity;
import com.android.fortunaattendancesystem.activities.EmployeeFingerEnrollmentActivity;
import com.android.fortunaattendancesystem.constant.Constants;
import com.android.fortunaattendancesystem.helper.Utility;
import com.android.fortunaattendancesystem.info.ProcessInfo;
import com.android.fortunaattendancesystem.model.CollegeAttendanceInfo;
import com.android.fortunaattendancesystem.model.ContractorInfo;
import com.android.fortunaattendancesystem.model.EmployeeTypeInfo;
import com.android.fortunaattendancesystem.model.EmployeeValidationBasicInfo;
import com.android.fortunaattendancesystem.model.EmployeeValidationFingerInfo;
import com.android.fortunaattendancesystem.model.MQTTHeaderInfo;
import com.android.fortunaattendancesystem.model.PeriodInfo;
import com.android.fortunaattendancesystem.model.ProfessorStudentSubjectInfo;
import com.android.fortunaattendancesystem.model.ProfessorSubjectInfo;
import com.android.fortunaattendancesystem.model.RemoteEnrollmentInfo;
import com.android.fortunaattendancesystem.model.SubjectInfo;
import com.android.fortunaattendancesystem.service.JSONCreatorParser;
import com.android.fortunaattendancesystem.singleton.MqttClientInfo;
import com.android.fortunaattendancesystem.submodules.MorphoCommunicator;
import com.android.fortunaattendancesystem.submodules.SQLiteCommunicator;
import com.morpho.morphosmart.sdk.ErrorCodes;
import com.morpho.morphosmart.sdk.MorphoDatabase;
import com.morpho.morphosmart.sdk.MorphoDevice;
import com.morpho.morphosmart.sdk.Template;
import com.morpho.morphosmart.sdk.TemplateList;
import com.morpho.morphosmart.sdk.TemplateType;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.android.fortunaattendancesystem.activities.EmployeeAttendanceActivity.isAttendanceWindowVisisble;

public class MqttMessageService extends Service {

    private final static String TAG = "TEST";

    private SQLiteCommunicator dbComm = new SQLiteCommunicator();
    private Random random = new Random();

    private static MqttAndroidClient mqttAndroidClient = null;
    private MqttApi mqttApi = null;
    private MorphoCommunicator morphoComm = null;
    private static HashMap <String, String> map = new HashMap <String, String>();

    private static ExecutorService executorService = null;
    AutoTemplateUpload autoTemplateUpload = new AutoTemplateUpload();
    DashBoardDataUpload dashBoardDataUpload = new DashBoardDataUpload();
    AttendanceDataUpload attendanceDataUpload = new AttendanceDataUpload();

    private String imei = "";
    private String brokerIP = "";
    private String brokerPort = "";
    private String username = "";
    private String password = "";
    private String subTopic = "";
    private String pubTopic = "";

    private boolean isTemplateDownloadComplete = true;
    private boolean isDeleteEmployeeCompleted = true;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            imei = intent.getStringExtra("IMEI");
            brokerIP = intent.getStringExtra("BrokerIP");
            brokerPort = intent.getStringExtra("BrokerPort");
            username = intent.getStringExtra("Username");
            password = intent.getStringExtra("Password");
            subTopic = intent.getStringExtra("SubTopic");
            pubTopic = intent.getStringExtra("PubTopic");

            mqttApi = new MqttApi(getApplicationContext());
            morphoComm = new MorphoCommunicator(getApplicationContext());

            if (brokerIP.trim().length() > 0 && brokerPort.trim().length() > 0) {
                String url = "tcp://" + brokerIP + ":" + brokerPort;
                mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), url, imei);
                setCallback();
                try {
                    IMqttToken token = mqttAndroidClient.connect(getMqttConnectionOption());
                    token.setActionCallback(new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
                            mqttAndroidClient.setBufferOpts(getDisconnectedBufferOptions());
                            Log.d(TAG, "Connection onSuccess");
                        }

                        @Override
                        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                            if (exception != null) {
                                Log.d(TAG, "Connection onFailure:" + exception.getMessage());
                            }
                        }
                    });
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        }
        return START_NOT_STICKY;
    }

    private void setCallback() {
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String brokerUrl) {
                Log.d(TAG, "Connection Complete");
                MqttClientInfo.getInstance().setMqttAndroidClient(mqttAndroidClient);
                String topic = subTopic + "/" + imei;
                try {
                    mqttApi.subscribe(mqttAndroidClient, topic, 2);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
                if (executorService == null) {
                    executorService = Executors.newFixedThreadPool(3);
                    executorService.submit(autoTemplateUpload);
                    executorService.submit(attendanceDataUpload);
                    executorService.submit(dashBoardDataUpload);
                }
            }

            @Override
            public void connectionLost(Throwable throwable) {
                if (throwable != null) {
                    Log.d(TAG, "Connection connectionLost:" + throwable.getMessage());
                }
                MqttClientInfo.getInstance().setMqttAndroidClient(mqttAndroidClient);
            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                Log.d(TAG, "Payload:" + new String(mqttMessage.getPayload()));
                String payload = new String(mqttMessage.getPayload());
                JSONObject json = null;
                String commandType = "";
                try {
                    json = new JSONObject(payload);
                    String pid = json.getString("PID");
                    MQTTHeaderInfo headerInfo = null;
                    headerInfo = JSONCreatorParser.parseHeaderData(json, headerInfo);
                    if (headerInfo != null) {
                        JSONObject jr = new JSONObject();
                        boolean isError = Validator.validateHeaderData(jr, headerInfo);
                        if (!isError) {
                            commandType = headerInfo.getCt();
                            switch (commandType) {
                                case Constants.SUBJECT_DOWNLOAD_COMMAND:
                                    ArrayList <SubjectInfo> sl = null;
                                    sl = JSONCreatorParser.parseSubjectJson(json, sl);
                                    if (sl != null) {
                                        String cst = json.getString("CST");
                                        switch (cst) {
                                            case Constants.COMMAND_SUB_TYPE_1:
                                                int size = sl.size();
                                                boolean isErrFound = false;
                                                jr.put("PID", pid);
                                                JSONArray be = new JSONArray();
                                                for (int i = 0; i < size; i++) {
                                                    SubjectInfo info = sl.get(i);
                                                    isError = Validator.validateSubjectData(jr, be, info);
                                                    if (!isError) {
                                                        String subTypes[] = info.getSubType();
                                                        if (subTypes != null) {
                                                            int len = subTypes.length;
                                                            for (int j = 0; j < len; j++) {
                                                                String st = subTypes[j];
                                                                boolean isValid = false;
                                                                isValid = Utility.isSubDataValid(info, st);
                                                                if (isValid) {
                                                                    boolean exists = false;
                                                                    exists = dbComm.isSubDataAvailable(info, st);
                                                                    if (!exists) {
                                                                        int status = dbComm.insertSubData(info, st);
                                                                        if (status != -1) {
                                                                            Log.d(TAG, "Inserted successfully");
                                                                        } else {
                                                                            Log.d(TAG, "Insertion failure");
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    } else {
                                                        isErrFound = true;
                                                    }
                                                }
                                                if (isErrFound) {
                                                    jr.put("BES", true);
                                                } else {
                                                    jr.put("BES", false);
                                                }
                                                publishData(jr);//Publish success json
                                                break;
                                            case Constants.COMMAND_SUB_TYPE_2:
                                                size = sl.size();
                                                isErrFound = false;
                                                jr.put("PID", pid);
                                                be = new JSONArray();
                                                for (int i = 0; i < size; i++) {
                                                    SubjectInfo info = sl.get(i);
                                                    isError = Validator.validateSubjectData(jr, be, info);
                                                    if (!isError) {
                                                        String subTypes[] = info.getSubType();
                                                        if (subTypes != null) {
                                                            int len = subTypes.length;
                                                            for (int j = 0; j < len; j++) {
                                                                String st = subTypes[j];
                                                                boolean isValid = false;
                                                                isValid = Utility.isSubDataValid(info, st);
                                                                if (isValid) {
                                                                    boolean exists = false;
                                                                    exists = dbComm.isSubDataAvailable(info, st);
                                                                    if (exists) {
                                                                        int status = dbComm.deleteSubData(info, st);
                                                                        if (status != -1) {
                                                                            Log.d(TAG, "Deleted successfully");
                                                                        } else {
                                                                            Log.d(TAG, "Deleted failure");
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    } else {
                                                        isErrFound = true;
                                                    }
                                                }
                                                if (isErrFound) {
                                                    jr.put("BES", true);
                                                } else {
                                                    jr.put("BES", false);
                                                }
                                                publishData(jr);//Publish success json
                                                break;
                                            case Constants.COMMAND_SUB_TYPE_3:
                                                break;
                                            default:
                                                int[] he = {-6};//Invalid command sub type
                                                int[] boe = {};
                                                boolean status = publishError(pid, he, boe);
                                                break;
                                        }
                                    } else {
                                        int[] he = {-50};//Missing body parameter
                                        int[] be = {};
                                        boolean status = publishError(pid, he, be);
                                    }
                                    break;
                                case Constants.PROFESSOR_SUBJECT_DOWNLOAD_COMMAND:
                                    ArrayList <ProfessorSubjectInfo> psl = null;
                                    psl = JSONCreatorParser.parseProfSubJson(json, psl);
                                    if (psl != null) {
                                        String cst = json.getString("CST");
                                        switch (cst) {
                                            case Constants.COMMAND_SUB_TYPE_1:
                                                int size = psl.size();
                                                boolean isErrFound = false;
                                                jr.put("PID", pid);
                                                JSONArray be = new JSONArray();
                                                for (int i = 0; i < size; i++) {
                                                    ProfessorSubjectInfo profSubInfo = psl.get(i);
                                                    isError = Validator.validateProfSubData(jr, be, profSubInfo);
                                                    if (!isError) {
                                                        JSONArray objArr = jr.getJSONArray("BE");
                                                        JSONObject obj = (JSONObject) objArr.get(i);
                                                        JSONArray ecArr = obj.getJSONArray("EC");
                                                        String subTypes[] = profSubInfo.getSubTypes();
                                                        if (subTypes != null) {
                                                            int len = subTypes.length;
                                                            for (int j = 0; j < len; j++) {
                                                                String st = subTypes[j];
                                                                boolean isValid = false;
                                                                isValid = Utility.isProfSubDataValid(profSubInfo, st);
                                                                if (isValid) {
                                                                    boolean exists = false;
                                                                    int subId = dbComm.getSubjectId(profSubInfo, st);
                                                                    if (subId != -1) {
                                                                        Log.d(TAG, "SubId:" + subId);
                                                                        exists = dbComm.isProfSubDataAvailable(profSubInfo, subId);
                                                                        if (!exists) {
                                                                            //ecArr.put(0);
                                                                            int status = dbComm.insertProfSubData(profSubInfo, subId);
                                                                            if (status != -1) {
                                                                                Log.d(TAG, "Insertion successful");
                                                                            } else {
                                                                                Log.d(TAG, "Insertion failure");
                                                                            }
                                                                        } else {
                                                                            ecArr.put(-52);//prof subject already found
                                                                        }
                                                                    } else {
                                                                        ecArr.put(-51);//Subject not found
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    } else {
                                                        isErrFound = true;
                                                    }
                                                }
                                                if (isErrFound) {
                                                    jr.put("BES", true);
                                                } else {
                                                    jr.put("BES", false);
                                                }
                                                publishData(jr);//Publish success json
                                                break;
                                            case Constants.COMMAND_SUB_TYPE_2:
                                                size = psl.size();
                                                for (int i = 0; i < size; i++) {
                                                    ProfessorSubjectInfo profSubInfo = psl.get(i);
                                                    String subTypes[] = profSubInfo.getSubTypes();
                                                    if (subTypes != null) {
                                                        int len = subTypes.length;
                                                        for (int j = 0; j < len; j++) {
                                                            String st = subTypes[j];
                                                            boolean isValid = false;
                                                            isValid = Utility.isProfSubDataValid(profSubInfo, st);
                                                            if (isValid) {
                                                                boolean exists = false;
                                                                int subId = dbComm.getSubjectId(profSubInfo, st);
                                                                if (subId != -1) {
                                                                    exists = dbComm.isProfSubDataAvailable(profSubInfo, subId);
                                                                    if (exists) {
                                                                        int status = dbComm.deleteProfSubData(profSubInfo, subId);
                                                                        if (status != -1) {
                                                                            Log.d(TAG, "Deletion successful");
                                                                        } else {
                                                                            Log.d(TAG, "Deletion failure");
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                                publishData(jr);//Publish success json
                                                break;
                                            case Constants.COMMAND_SUB_TYPE_3:
                                                break;
                                            default:
                                                int[] he = {-6};//Invalid command sub type
                                                int[] boe = {};
                                                boolean status = publishError(pid, he, boe);
                                                break;
                                        }
                                    } else {
                                        int[] he = {-50};//Missing body parameter
                                        int[] be = {};
                                        boolean status = publishError(pid, he, be);
                                    }
                                    break;
                                case Constants.PROFESSOR_STUDENT_SUBJECT_DOWNLOAD_COMMAND:
                                    ArrayList <ProfessorStudentSubjectInfo> pssl = null;
                                    pssl = JSONCreatorParser.parseProfStuSubJson(json, pssl);
                                    if (pssl != null) {
                                        String cst = json.getString("CST");
                                        switch (cst) {
                                            case Constants.COMMAND_SUB_TYPE_1:
                                                int size = pssl.size();
                                                boolean isErrFound = false;
                                                jr.put("PID", pid);
                                                JSONArray be = new JSONArray();
                                                for (int i = 0; i < size; i++) {
                                                    ProfessorStudentSubjectInfo profStudentSubInfo = pssl.get(i);
                                                    isError = Validator.validateProfStudentSubData(jr, be, profStudentSubInfo);
                                                    if (!isError) {
                                                        JSONArray objArr = jr.getJSONArray("BE");
                                                        JSONObject obj = (JSONObject) objArr.get(i);
                                                        JSONArray ecArr = obj.getJSONArray("EC");
                                                        boolean isValid = false;
                                                        isValid = Utility.isProfStuSubValid(profStudentSubInfo);
                                                        if (isValid) {
                                                            boolean exists = false;
                                                            exists = dbComm.isProfStuSubDataAvailable(profStudentSubInfo);
                                                            if (!exists) {
                                                                String profId = profStudentSubInfo.getProfessorEmpId();
                                                                if (!profId.equals(Constants.ANY_PROFESSOR)) {
                                                                    int status = dbComm.insertProfStuSubData(profStudentSubInfo);
                                                                    if (status != -1) {
                                                                        Log.d(TAG, "Data inserted successfully");
                                                                    } else {
                                                                        Log.d(TAG, "Data insertion failure");
                                                                    }
                                                                } else if (profId.equals(Constants.ANY_PROFESSOR)) {
                                                                    ArrayList <String> subIdList = null;
                                                                    String subCode = profStudentSubInfo.getSubCode();
                                                                    subIdList = dbComm.getSubjectIdList(subCode, subIdList);
                                                                    if (subIdList != null) {
                                                                        int len = subIdList.size();
                                                                        if (len > 0) {
                                                                            ArrayList <String> professorIdList = new ArrayList <String>();
                                                                            for (int j = 0; j < len; j++) {
                                                                                professorIdList = dbComm.getProfessorListFromSub(Integer.parseInt(subIdList.get(j)), professorIdList);
                                                                            }
                                                                            ArrayList <String> uniqueProfIdList = new ArrayList <String>();
                                                                            int l = professorIdList.size();
                                                                            if (l > 0) {
                                                                                for (int k = 0; k < l; k++) {
                                                                                    if (!uniqueProfIdList.contains(professorIdList.get(k))) {
                                                                                        uniqueProfIdList.add(professorIdList.get(k));
                                                                                    }
                                                                                }
                                                                                int l1 = uniqueProfIdList.size();
                                                                                if (l1 > 0) {
                                                                                    for (int m = 0; m < l1; m++) {
                                                                                        exists = false;
                                                                                        profStudentSubInfo.setProfessorEmpId(uniqueProfIdList.get(m));
                                                                                        exists = dbComm.isProfStuSubDataAvailable(profStudentSubInfo);
                                                                                        if (!exists) {
                                                                                            int status = dbComm.insertProfStuSubData(profStudentSubInfo);
                                                                                            if (status != -1) {
                                                                                                Log.d(TAG, "Data inserted successfully any professor");
                                                                                            } else {
                                                                                                Log.d(TAG, "Data insertion failure any professor");
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                }
                                                                            } else {
                                                                                ecArr.put(-54);//No professor found against the subject
                                                                            }
                                                                        }
                                                                    } else {
                                                                        ecArr.put(-51);//Subject not found
                                                                    }
                                                                }
                                                            } else {
                                                                ecArr.put(-53);//Professor Student Subject already exists
                                                            }
                                                        }
                                                    } else {
                                                        isErrFound = true;
                                                    }
                                                }
                                                if (isErrFound) {
                                                    jr.put("BES", true);
                                                } else {
                                                    jr.put("BES", false);
                                                }
                                                publishData(jr);//Publish success json
                                                break;
                                            case Constants.COMMAND_SUB_TYPE_2:
                                                size = pssl.size();
                                                for (int i = 0; i < size; i++) {
                                                    ProfessorStudentSubjectInfo profStuSubInfo = pssl.get(i);
                                                    if (profStuSubInfo != null) {
                                                        boolean isValid = false;
                                                        isValid = Utility.isProfStuSubValid(profStuSubInfo);
                                                        if (isValid) {
                                                            boolean exists = false;
                                                            exists = dbComm.isProfStuSubDataAvailable(profStuSubInfo);
                                                            if (exists) {
                                                                int status = dbComm.deleteProfStuSubData(profStuSubInfo);
                                                                if (status != -1) {
                                                                    Log.d(TAG, "Data deleted successfully");
                                                                } else {
                                                                    Log.d(TAG, "Data deleteion failure");
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                                publishData(jr);//Publish success json
                                                break;
                                            case Constants.COMMAND_SUB_TYPE_3:
                                                break;
                                            default:
                                                int[] he = {-6};//Invalid command sub type
                                                int[] boe = {};
                                                boolean status = publishError(pid, he, boe);
                                                break;
                                        }
                                    } else {
                                        int[] he = {-50};//Missing body parameter
                                        int[] be = {};
                                        boolean status = publishError(pid, he, be);
                                    }
                                    break;
                                case Constants.EMPLOYEE_TYPE_DOWNLOAD_COMMAND:
                                    ArrayList <EmployeeTypeInfo> etList = null;
                                    etList = JSONCreatorParser.parseEmployeeTypeJson(json, etList);
                                    if (etList != null) {
                                        String cst = json.getString("CST");
                                        int size;
                                        switch (cst) {
                                            case Constants.COMMAND_SUB_TYPE_1:
                                                size = etList.size();
                                                for (int i = 0; i < size; i++) {
                                                    EmployeeTypeInfo etInfo = etList.get(i);
                                                    if (etInfo != null) {
                                                        boolean isValid = false;
                                                        isValid = Utility.isEmpTypeValid(etInfo);
                                                        if (isValid) {
                                                            boolean exists = false;
                                                            exists = dbComm.isEmpTypeDataAvailable(etInfo);
                                                            if (!exists) {
                                                                Log.d(TAG, "ET:" + etInfo.getEmpType());
                                                                if (etInfo.getEmpType().trim().length() > 0) {
                                                                    int status = dbComm.insertEmpTypeData(etInfo);
                                                                    if (status != -1) {
                                                                        Log.d(TAG, "Data inserted successfully");
                                                                    } else {
                                                                        Log.d(TAG, "Data insertion failure");
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                                break;
                                            case Constants.COMMAND_SUB_TYPE_2:
                                                size = etList.size();
                                                for (int i = 0; i < size; i++) {
                                                    EmployeeTypeInfo etInfo = etList.get(i);
                                                    if (etInfo != null) {
                                                        boolean isValid = false;
                                                        isValid = Utility.isEmpTypeValid(etInfo);
                                                        if (isValid) {
                                                            boolean exists = false;
                                                            exists = dbComm.isEmpTypeDataAvailable(etInfo);
                                                            if (exists) {
                                                                int status = dbComm.deleteEmpTypeData(etInfo);
                                                                if (status != -1) {
                                                                    Log.d(TAG, "Data deleted successfully");
                                                                } else {
                                                                    Log.d(TAG, "Data deletion failure");
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                                break;
                                            case Constants.COMMAND_SUB_TYPE_3:
                                                break;
                                            default:
                                                int[] he = {-6};//Invalid command sub type
                                                int[] be = {};
                                                boolean status = publishError(pid, he, be);
                                                break;
                                        }
                                    } else {
                                        int[] he = {-50};//Missing body parameter
                                        int[] be = {};
                                        boolean status = publishError(pid, he, be);
                                    }
                                    break;

                                case Constants.CONTRACTOR_DOWNLOAD_COMMAND:
                                    ArrayList <ContractorInfo> ctList = null;
                                    ctList = JSONCreatorParser.parseContractorJson(json, ctList);
                                    if (ctList != null) {
                                        String cst = json.getString("CST");
                                        int size;
                                        switch (cst) {
                                            case Constants.COMMAND_SUB_TYPE_1:
                                                size = ctList.size();
                                                for (int i = 0; i < size; i++) {
                                                    ContractorInfo ctInfo = ctList.get(i);
                                                    if (ctInfo != null) {
                                                        boolean isValid = false;
                                                        isValid = Utility.isContractorValid(ctInfo);
                                                        if (isValid) {
                                                            boolean exists = false;
                                                            exists = dbComm.isContractorDataAvailable(ctInfo);
                                                            if (!exists) {
                                                                int status = dbComm.insertContractorData(ctInfo);
                                                                if (status != -1) {
                                                                    Log.d(TAG, "Data inserted successfully");
                                                                } else {
                                                                    Log.d(TAG, "Data insertion failure");
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                                break;
                                            case Constants.COMMAND_SUB_TYPE_2:
                                                size = ctList.size();
                                                for (int i = 0; i < size; i++) {
                                                    ContractorInfo ctInfo = ctList.get(i);
                                                    if (ctInfo != null) {
                                                        boolean isValid = false;
                                                        isValid = Utility.isContractorValid(ctInfo);
                                                        if (isValid) {
                                                            boolean exists = false;
                                                            exists = dbComm.isContractorDataAvailable(ctInfo);
                                                            if (exists) {
                                                                int status = dbComm.deleteContractorData(ctInfo);
                                                                if (status != -1) {
                                                                    Log.d(TAG, "Data deleted successfully");
                                                                } else {
                                                                    Log.d(TAG, "Data deletion failure");
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                                break;
                                            case Constants.COMMAND_SUB_TYPE_3:
                                                break;
                                            default:
                                                int[] he = {-6};//Invalid command sub type
                                                int[] be = {};
                                                boolean status = publishError(pid, he, be);
                                                break;
                                        }
                                    } else {
                                        int[] he = {-50};//Missing body parameter
                                        int[] be = {};
                                        boolean status = publishError(pid, he, be);
                                    }
                                    break;

                                case Constants.PERIOD_DOWNLOAD_COMMAND:
                                    ArrayList <PeriodInfo> pdList = null;
                                    pdList = JSONCreatorParser.parsePeriodJson(json, pdList);
                                    if (pdList != null) {
                                        String cst = json.getString("CST");
                                        int size;
                                        switch (cst) {
                                            case Constants.COMMAND_SUB_TYPE_1:
                                                size = pdList.size();
                                                for (int i = 0; i < size; i++) {
                                                    PeriodInfo pdInfo = pdList.get(i);
                                                    if (pdInfo != null) {
                                                        boolean isValid = false;
                                                        isValid = Utility.isPeriodValid(pdInfo);
                                                        if (isValid) {
                                                            boolean exists = false;
                                                            exists = dbComm.isPeriodDataAvailable(pdInfo);
                                                            if (!exists) {
                                                                int status = dbComm.insertPeriodData(pdInfo);
                                                                if (status != -1) {
                                                                    Log.d(TAG, "Data inserted successfully");
                                                                } else {
                                                                    Log.d(TAG, "Data insertion failure");
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                                break;
                                            case Constants.COMMAND_SUB_TYPE_2:
                                                size = pdList.size();
                                                for (int i = 0; i < size; i++) {
                                                    PeriodInfo pdInfo = pdList.get(i);
                                                    if (pdInfo != null) {
                                                        boolean isValid = false;
                                                        isValid = Utility.isPeriodValid(pdInfo);
                                                        if (isValid) {
                                                            boolean exists = false;
                                                            exists = dbComm.isPeriodDataAvailable(pdInfo);
                                                            if (exists) {
                                                                int status = dbComm.deletePeriodData(pdInfo);
                                                                if (status != -1) {
                                                                    Log.d(TAG, "Data deletion successfully");
                                                                } else {
                                                                    Log.d(TAG, "Data deletion failure");
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                                break;
                                            case Constants.COMMAND_SUB_TYPE_3:
                                                break;
                                            default:
                                                int[] he = {-6};//Invalid command sub type
                                                int[] be = {};
                                                boolean status = publishError(pid, he, be);
                                                break;
                                        }
                                    } else {
                                        int[] he = {-50};//Missing body parameter
                                        int[] be = {};
                                        boolean status = publishError(pid, he, be);
                                    }
                                    break;
                                case Constants.VALIDATION_TEMPLATE_DOWNLOAD:
                                    if (!EmployeeFingerEnrollmentActivity.isFingerEnrollmentWindowVisisble && isTemplateDownloadComplete && isDeleteEmployeeCompleted) {
                                        EmployeeAttendanceActivity.stopHandler();
                                        isTemplateDownloadComplete = false;
                                        ArrayList <EmployeeValidationBasicInfo> empInfoList = null;
                                        empInfoList = JSONCreatorParser.parseEmpValJson(json, empInfoList);
                                        if (empInfoList != null) {
                                            boolean isErrFound = false;
                                            int capacity = empInfoList.size();
                                            jr.put("PID", pid);
                                            JSONArray be = new JSONArray();
                                            for (int count = 0; count < capacity; count++) {
                                                EmployeeValidationBasicInfo info = empInfoList.get(count);
                                                isError = Validator.validateEmpValData(jr, be, info);
                                                if (!isError) {
                                                    int error = Utility.validateBasicInfo(info);
                                                    if (error == 0) {
                                                        ArrayList <EmployeeValidationFingerInfo> fl = info.getfInfoList();
                                                        if (fl != null) {
                                                            int sizel = fl.size();
                                                            if (sizel > 0) {
                                                                info.setVm(Utility.getVerificationModeByVal(info.getVm()));
                                                                for (int k = 0; k < sizel; k++) {
                                                                    EmployeeValidationFingerInfo fInfo = fl.get(k);
                                                                    error = Utility.validateFingerInfo(fInfo);
                                                                    if (error == 0) {
                                                                        fInfo.setSl(Utility.getSecurityLevelByVal(fInfo.getSl()));
                                                                        fInfo.setFi(Utility.getFingerIndexByVal(fInfo.getFi()));
                                                                        fInfo.setFq(Utility.getFingerQualityByVal(fInfo.getFq()));
                                                                        int autoId = dbComm.getAutoIdByEmpId(info.getEmpId());//Database Communication
                                                                        if (autoId == -1) {
                                                                            String empType = info.getEmpType();
                                                                            if (empType != null && empType.trim().length() > 0) {
                                                                                int empTypeId = dbComm.getEmployeeTypeId(empType);
                                                                                if (empTypeId != -1) {
                                                                                    info.setEmpType(Integer.toString(empTypeId));
                                                                                } else {
                                                                                    info.setEmpType("");
                                                                                }
                                                                            }
                                                                            info = dbComm.insertRemotelyEnrolledEmployeeData(info);//Database Communication
                                                                        } else {
                                                                            info.setEnrollmentNo(autoId);
                                                                            info = dbComm.updateEmployeeData(info);//Database Communication
                                                                        }
                                                                        int enrollNo = info.getEnrollmentNo();
                                                                        if (enrollNo != -1) {
                                                                            int id = -1;
                                                                            String fingerType = fInfo.getFt();
                                                                            if (fingerType != null && fingerType.equals("F1")) {
                                                                                id = dbComm.checkTemplateExistsByFT(enrollNo, "1");//Database Communication
                                                                                if (id == -1) {
                                                                                    id = dbComm.insertRemoteEnrolledTemplate(info, fInfo);//Database Communication
                                                                                    if (id != -1) {
                                                                                        MorphoDevice morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
                                                                                        MorphoDatabase morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
                                                                                        if (morphoDevice != null && morphoDatabase != null) {
                                                                                            boolean isIdentificationStarted = ProcessInfo.getInstance().isIdentificationStarted();
                                                                                            if (isIdentificationStarted) {
                                                                                                morphoComm.stopFingerIdentification();
                                                                                            }
                                                                                            TemplateList tempList = new TemplateList();
                                                                                            byte[] templateData = Utility.hexStringToByteArray(fInfo.getFmd());
                                                                                            Template template = new Template();
                                                                                            template.setTemplateType(TemplateType.MORPHO_PK_ISO_FMR);
                                                                                            template.setData(templateData);
                                                                                            template.setDataIndex(0);
                                                                                            tempList.putTemplate(template);
                                                                                            int ret = morphoComm.insertOneRemoteTemplateToMorphoDB(tempList, info, fInfo);
                                                                                            Log.d("TEST", fingerType + " Morpho Insert Result:" + ret);
                                                                                            if (ret == 0) {
                                                                                                id = dbComm.updateFingerEnrolledStatusToEmpTbl(enrollNo, info, fInfo);//Database Communication
                                                                                                if (id != -1) {
                                                                                                    Log.d(TAG, "Update Successful");
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                } else {
                                                                                    int status = dbComm.updateOneRemoteEnrolledTemplate(id, info, fInfo);
                                                                                    if (status != -1) {
                                                                                        MorphoDevice morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
                                                                                        MorphoDatabase morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
                                                                                        if (morphoDevice != null && morphoDatabase != null) {
                                                                                            boolean isIdentificationStarted = ProcessInfo.getInstance().isIdentificationStarted();
                                                                                            if (isIdentificationStarted) {
                                                                                                morphoComm.stopFingerIdentification();
                                                                                            }
                                                                                            TemplateList tempList = new TemplateList();
                                                                                            byte[] templateData = Utility.hexStringToByteArray(fInfo.getFmd());
                                                                                            Template template = new Template();
                                                                                            template.setTemplateType(TemplateType.MORPHO_PK_ISO_FMR);
                                                                                            template.setData(templateData);
                                                                                            template.setDataIndex(0);
                                                                                            tempList.putTemplate(template);
                                                                                            int ret = morphoComm.insertOneRemoteTemplateToMorphoDB(tempList, info, fInfo);
                                                                                            Log.d("TEST", fingerType + " Morpho Update Result:" + ret);
                                                                                            if (ret == 0) {
                                                                                                id = dbComm.updateFingerEnrolledStatusToEmpTbl(enrollNo, info, fInfo);//Database Communication
                                                                                                if (id != -1) {
                                                                                                    Log.d(TAG, "Update Successful");
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                }
                                                                            } else if (fingerType != null && fingerType.equals("F2")) {
                                                                                id = dbComm.checkTemplateExistsByFT(autoId, "2");//Database Communication
                                                                                if (id == -1) {
                                                                                    id = dbComm.insertRemoteEnrolledTemplate(info, fInfo);//Database Communication
                                                                                    if (id != -1) {
                                                                                        MorphoDevice morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
                                                                                        MorphoDatabase morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
                                                                                        if (morphoDevice != null && morphoDatabase != null) {
                                                                                            boolean isIdentificationStarted = ProcessInfo.getInstance().isIdentificationStarted();
                                                                                            if (isIdentificationStarted) {
                                                                                                morphoComm.stopFingerIdentification();
                                                                                            }
                                                                                            TemplateList tempList = new TemplateList();
                                                                                            byte[] templateData = Utility.hexStringToByteArray(fInfo.getFmd());
                                                                                            Template template = new Template();
                                                                                            template.setTemplateType(TemplateType.MORPHO_PK_ISO_FMR);
                                                                                            template.setData(templateData);
                                                                                            template.setDataIndex(0);
                                                                                            tempList.putTemplate(template);
                                                                                            int ret = morphoComm.insertOneRemoteTemplateToMorphoDB(tempList, info, fInfo);
                                                                                            Log.d("TEST", fingerType + " Morpho Insert Result:" + ret);
                                                                                            if (ret == 0) {
                                                                                                id = dbComm.updateFingerEnrolledStatusToEmpTbl(enrollNo, info, fInfo);//Database Communication
                                                                                                if (id != -1) {
                                                                                                    Log.d(TAG, "Update Successful");
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                } else {
                                                                                    int status = dbComm.updateOneRemoteEnrolledTemplate(id, info, fInfo);
                                                                                    if (status != -1) {
                                                                                        MorphoDevice morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
                                                                                        MorphoDatabase morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
                                                                                        if (morphoDevice != null && morphoDatabase != null) {
                                                                                            boolean isIdentificationStarted = ProcessInfo.getInstance().isIdentificationStarted();
                                                                                            if (isIdentificationStarted) {
                                                                                                morphoComm.stopFingerIdentification();
                                                                                            }
                                                                                            TemplateList tempList = new TemplateList();
                                                                                            byte[] templateData = Utility.hexStringToByteArray(fInfo.getFmd());
                                                                                            Template template = new Template();
                                                                                            template.setTemplateType(TemplateType.MORPHO_PK_ISO_FMR);
                                                                                            template.setData(templateData);
                                                                                            template.setDataIndex(0);
                                                                                            tempList.putTemplate(template);
                                                                                            int ret = morphoComm.insertOneRemoteTemplateToMorphoDB(tempList, info, fInfo);
                                                                                            Log.d("TEST", fingerType + " Morpho Update Result:" + ret);
                                                                                            if (ret == 0) {
                                                                                                id = dbComm.updateFingerEnrolledStatusToEmpTbl(enrollNo, info, fInfo);//Database Communication
                                                                                                if (id != -1) {
                                                                                                    Log.d(TAG, "Update Successful");
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        } else {
                                                            int autoId = dbComm.getAutoIdByEmpId(info.getEmpId());//Database Communication
                                                            if (autoId == -1) {
                                                                String empType = info.getEmpType();
                                                                if (empType != null && empType.trim().length() > 0) {
                                                                    int empTypeId = dbComm.getEmployeeTypeId(empType);
                                                                    if (empTypeId != -1) {
                                                                        info.setEmpType(Integer.toString(empTypeId));
                                                                    } else {
                                                                        info.setEmpType("");
                                                                    }
                                                                }
                                                                dbComm.insertRemotelyEnrolledEmployeeData(info);//Database Communication
                                                            } else {
                                                                info.setEnrollmentNo(autoId);
                                                                String empType = info.getEmpType();
                                                                if (empType != null && empType.trim().length() > 0) {
                                                                    int empTypeId = dbComm.getEmployeeTypeId(empType);
                                                                    if (empTypeId != -1) {
                                                                        info.setEmpType(Integer.toString(empTypeId));
                                                                    } else {
                                                                        info.setEmpType("");
                                                                    }
                                                                }
                                                                dbComm.updateEmployeeData(info);//Database Communication
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    isErrFound = true;
                                                }
                                            }
                                            if (isErrFound) {
                                                jr.put("BES", true);
                                            } else {
                                                jr.put("BES", false);
                                            }
                                            publishData(jr);
                                        } else {
                                            int[] he = {-50};//Missing body parameter
                                            int[] be = {};
                                            boolean status = publishError(pid, he, be);
                                        }
                                        isTemplateDownloadComplete = true;
                                        if (isAttendanceWindowVisisble) {
                                            boolean isIdentificationStarted = ProcessInfo.getInstance().isIdentificationStarted();
                                            boolean isVerificationStarted = ProcessInfo.getInstance().isVerificationStarted();
                                            boolean isBioCommandStarted = ProcessInfo.getInstance().isCommandBioStart();
                                            if (!isIdentificationStarted && !isVerificationStarted && !isBioCommandStarted) {
                                                EmployeeAttendanceActivity.stopHandler();
                                                EmployeeAttendanceActivity.startHandler();
                                            }
                                        }
                                    }
                                    break;
                                case Constants.VALIDATION_TEMPLATE_UPLOAD:
                                    JSONArray arr = json.getJSONArray("CD");
                                    if (arr != null) {
                                        int length = arr.length();
                                        if (length > 0) {
                                            for (int i = 0; i < length; i++) {
                                                JSONObject obj = (JSONObject) arr.get(i);
                                                String ut = obj.getString("UT");
                                                if (!ut.equals("null") && ut.trim().length() > 0) {
                                                    switch (ut) {
                                                        case "ALL":
                                                            ArrayList <EmployeeValidationBasicInfo> empBasicInfoList = null;
                                                            empBasicInfoList = dbComm.getAllEnrolledTemplatesForMqtt(empBasicInfoList);
                                                            if (empBasicInfoList != null) {
                                                                int size = empBasicInfoList.size();
                                                                if (size > 0) {
                                                                    for (int count = 0; count < size; count++) {
                                                                        EmployeeValidationBasicInfo info = empBasicInfoList.get(count);
                                                                        ArrayList <EmployeeValidationFingerInfo> fi = info.getfInfoList();
                                                                        if (fi != null && fi.size() > 0) {
                                                                            String templateJson = JSONCreatorParser.createJsonForEnrolledTemplates(info);
                                                                            MqttMessage message = new MqttMessage(templateJson.getBytes());
                                                                            message.setQos(2);
                                                                            try {
                                                                                mqttAndroidClient.publish(Constants.GET_TEMPLATE_TOPIC, message);
                                                                                int status = dbComm.updateFingerUploadStatus(info);
                                                                                if (status != -1) {
                                                                                    Log.d("TEST", "Update successful");
                                                                                }
                                                                            } catch (MqttException e) {
                                                                                e.printStackTrace();
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                            break;
                                                        default://Invalid Upload Type
                                                            int[] he = {};//Invalid command sub type
                                                            int[] be = {-47};
                                                            boolean status = publishError(pid, he, be);
                                                            break;
                                                    }
                                                } else {//Upload Type Blank
                                                    int[] he = {};//Invalid command sub type
                                                    int[] be = {-47};
                                                    boolean status = publishError(pid, he, be);
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                    break;
                                case Constants.REMOTE_ENROLL:
                                    if (!EmployeeFingerEnrollmentActivity.isFingerEnrollmentWindowVisisble && isTemplateDownloadComplete && isDeleteEmployeeCompleted) {
                                        String cst = json.getString("CST");
                                        switch (cst) {
                                            case "E1"://Remote Enroll
                                                ArrayList <EmployeeValidationBasicInfo> remoteEnrollInfoList = null;
                                                remoteEnrollInfoList = JSONCreatorParser.parseRemoteEnrollJson(json, remoteEnrollInfoList);
                                                if (remoteEnrollInfoList != null) {
                                                    boolean isErrFound = false;
                                                    jr.put("PID", pid);
                                                    JSONArray be = new JSONArray();
                                                    int capacity = remoteEnrollInfoList.size();
                                                    if (capacity > 0) {
                                                        MorphoDevice morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
                                                        MorphoDatabase morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
                                                        if (morphoDevice != null && morphoDatabase != null) {
                                                            boolean isIdentificationStarted = ProcessInfo.getInstance().isIdentificationStarted();
                                                            if (isIdentificationStarted) {
                                                                morphoComm.stopFingerIdentification();
                                                                try {
                                                                    Thread.sleep(100);
                                                                } catch (InterruptedException ie) {
                                                                }
                                                            }
                                                            for (int count = 0; count < capacity; count++) {
                                                                EmployeeValidationBasicInfo info = remoteEnrollInfoList.get(count);
                                                                isError = Validator.validateEmpValRemoteData(jr, be, info);
                                                                if (!isError) {
                                                                    EmployeeAttendanceActivity.stopHandler();
                                                                    ArrayList <EmployeeValidationFingerInfo> fInfoList = info.getfInfoList();
                                                                    EmployeeValidationFingerInfo fInfo = fInfoList.get(0);
                                                                    RemoteEnrollmentInfo remoteEnrollInfo = new RemoteEnrollmentInfo();
                                                                    remoteEnrollInfo.setEmpId(info.getEmpId().trim());
                                                                    remoteEnrollInfo.setCardId(info.getCardId().replaceAll("\\G0", " ").trim());
                                                                    remoteEnrollInfo.setEmpName(info.getEmpName().trim());
                                                                    remoteEnrollInfo.setFingerType(fInfo.getFt());
                                                                    remoteEnrollInfo.setVerificationMode(Utility.getVerificationModeByVal(info.getVm()));
                                                                    remoteEnrollInfo.setFingerIndex(Utility.getFingerIndexByVal(fInfo.getFi()));
                                                                    remoteEnrollInfo.setFingerQuality(Utility.getFingerQualityByVal(fInfo.getFq()));
                                                                    remoteEnrollInfo.setSecurityLevel(Utility.getSecurityLevelByVal(fInfo.getSl()));
                                                                    Intent intent = new Intent(getApplicationContext(), EmployeeFingerEnrollmentActivity.class);
                                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                    intent.putExtra("RemoteEnrollInfo", remoteEnrollInfo);
                                                                    startActivity(intent);
                                                                } else {
                                                                    isErrFound = true;
                                                                }
                                                            }
                                                            if (isErrFound) {
                                                                jr.put("BES", true);
                                                            } else {
                                                                jr.put("BES", false);
                                                            }
                                                            publishData(jr);
                                                        }
                                                    }
                                                } else {
                                                    int[] he = {-50};//Missing body parameter
                                                    int[] be = {};
                                                    boolean status = publishError(pid, he, be);
                                                }
                                                break;
                                            case "E2"://Re-Enroll
                                                ArrayList <EmployeeValidationBasicInfo> remoteReEnrollInfoList = null;
                                                remoteEnrollInfoList = JSONCreatorParser.parseRemoteEnrollJson(json, remoteReEnrollInfoList);
                                                if (remoteEnrollInfoList != null) {
                                                    boolean isErrFound = false;
                                                    jr.put("PID", pid);
                                                    JSONArray be = new JSONArray();
                                                    int capacity = remoteEnrollInfoList.size();
                                                    if (capacity > 0) {
                                                        MorphoDevice morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
                                                        MorphoDatabase morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
                                                        if (morphoDevice != null && morphoDatabase != null) {
                                                            boolean isIdentificationStarted = ProcessInfo.getInstance().isIdentificationStarted();
                                                            if (isIdentificationStarted) {
                                                                morphoComm.stopFingerIdentification();
                                                                try {
                                                                    Thread.sleep(100);
                                                                } catch (InterruptedException ie) {
                                                                }
                                                            }
                                                            if (!EmployeeFingerEnrollmentActivity.isFingerEnrollmentWindowVisisble) {
                                                                for (int count = 0; count < capacity; count++) {
                                                                    EmployeeValidationBasicInfo info = remoteEnrollInfoList.get(count);
                                                                    isError = Validator.validateEmpValRemoteData(jr, be, info);
                                                                    if (!isError) {
                                                                        EmployeeAttendanceActivity.stopHandler();
                                                                        ArrayList <EmployeeValidationFingerInfo> fInfoList = info.getfInfoList();
                                                                        EmployeeValidationFingerInfo fInfo = fInfoList.get(0);
                                                                        String ft = fInfo.getFt();
                                                                        if (ft != null && ft.equals("F1")) {
                                                                            String empId = info.getEmpId().trim();
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
                                                                        RemoteEnrollmentInfo remoteEnrollInfo = new RemoteEnrollmentInfo();
                                                                        remoteEnrollInfo.setEmpId(info.getEmpId().trim());
                                                                        remoteEnrollInfo.setCardId(info.getCardId().replaceAll("\\G0", " ").trim());
                                                                        remoteEnrollInfo.setEmpName(info.getEmpName().trim());
                                                                        remoteEnrollInfo.setFingerType(fInfo.getFt());
                                                                        remoteEnrollInfo.setVerificationMode(Utility.getVerificationModeByVal(info.getVm()));
                                                                        remoteEnrollInfo.setFingerIndex(Utility.getFingerIndexByVal(fInfo.getFi()));
                                                                        remoteEnrollInfo.setFingerQuality(Utility.getFingerQualityByVal(fInfo.getFq()));
                                                                        remoteEnrollInfo.setSecurityLevel(Utility.getSecurityLevelByVal(fInfo.getSl()));
                                                                        Intent intent = new Intent(getApplicationContext(), EmployeeFingerEnrollmentActivity.class);
                                                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                        intent.putExtra("RemoteEnrollInfo", remoteEnrollInfo);
                                                                        startActivity(intent);
                                                                    } else {
                                                                        isErrFound = true;
                                                                    }
                                                                }
                                                                if (isErrFound) {
                                                                    jr.put("BES", true);
                                                                } else {
                                                                    jr.put("BES", false);
                                                                }
                                                                publishData(jr);
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    int[] he = {-50};//Missing body parameter
                                                    int[] be = {};
                                                    boolean status = publishError(pid, he, be);
                                                }
                                                break;
                                            case "E3"://Remote Dummy Enroll
                                                break;
                                            default:
                                                int[] he = {};//Invalid command sub type
                                                int[] be = {-6};
                                                boolean status = publishError(pid, he, be);
                                                break;
                                        }
                                    }
                                    break;
                                case Constants.DELETE_EMPLOYEE:
                                    if (!EmployeeFingerEnrollmentActivity.isFingerEnrollmentWindowVisisble && isTemplateDownloadComplete) {
                                        isDeleteEmployeeCompleted = false;
                                        EmployeeAttendanceActivity.stopHandler();
                                        JSONArray jsonArray = json.getJSONArray("CD");
                                        if (jsonArray != null) {
                                            int length = jsonArray.length();
                                            if (length > 0) {
                                                MorphoDevice morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
                                                MorphoDatabase morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
                                                if (morphoDevice != null && morphoDatabase != null) {
                                                    boolean isIdentificationStarted = ProcessInfo.getInstance().isIdentificationStarted();
                                                    if (isIdentificationStarted) {
                                                        morphoComm.stopFingerIdentification();
                                                    }
                                                    for (int i = 0; i < length; i++) {
                                                        JSONObject jsonObj = (JSONObject) jsonArray.get(i);
                                                        String isEmpId = jsonObj.getString("IsEmpId");
                                                        if (isEmpId != null && isEmpId.equals("Y")) {
                                                            String empId = jsonObj.getString("Id");
                                                            int autoId = dbComm.getAutoIdByEmpId(empId);
                                                            if (autoId != -1) {
                                                                int status = dbComm.deleteEmployeeDataByAutoId(autoId);
                                                                if (status != -1) {
                                                                    status = dbComm.deleteFingerRecordByAutoId(autoId);
                                                                    if (status != -1) {
                                                                        int ret = morphoComm.deleteMorphoUser(empId.trim());
                                                                        if (ret == ErrorCodes.MORPHO_OK) {
                                                                            Log.d("TEST", "Delete Success");
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        } else if (isEmpId != null && isEmpId.equals("N")) {
                                                            String cardId = jsonObj.getString("Id");
                                                            int autoId = dbComm.getAutoIdByCardId(cardId);
                                                            if (autoId != -1) {
                                                                String empId = dbComm.getEmpIdByAutoId(autoId);
                                                                int status = dbComm.deleteEmployeeDataByAutoId(autoId);
                                                                if (status != -1) {
                                                                    status = dbComm.deleteFingerRecordByAutoId(autoId);
                                                                    if (status != -1) {
                                                                        int ret = morphoComm.deleteMorphoUser(empId.trim());
                                                                        if (ret == ErrorCodes.MORPHO_OK) {
                                                                            Log.d("TEST", "Delete Success");
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                    try {
                                                        Thread.sleep(100);
                                                    } catch (InterruptedException ie) {
                                                    }
                                                }
                                            }
                                        } else {
                                            int[] he = {-50};//Missing body parameter
                                            int[] be = {};
                                            boolean status = publishError(pid, he, be);
                                        }
                                        isDeleteEmployeeCompleted = true;
                                        if (isAttendanceWindowVisisble) {
                                            boolean isBioCommandStarted = ProcessInfo.getInstance().isCommandBioStart();
                                            boolean isIdentificationStarted = ProcessInfo.getInstance().isIdentificationStarted();
                                            if (isIdentificationStarted) {
                                                EmployeeAttendanceActivity.startHandler();
                                            }
                                        }
                                    }
                                    break;
                                default:
                                    int[] he = {-4};//Invalid command type
                                    int[] be = {};
                                    boolean status = publishError(pid, he, be);
                                    break;
                            }
                        } else { //Invalid value of header
                            JSONArray be = new JSONArray();
                            jr.put("PID", pid);
                            jr.put("BE", be);
                            jr.put("BES", false);
                            MqttMessage message = new MqttMessage(jr.toString().getBytes());
                            message.setQos(2);
                            try {
                                mqttAndroidClient.publish(Constants.ERROR_PUBLISH_TOPIC, message);
                            } catch (MqttException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        int[] he = {-49};//Header parameter missing
                        int[] be = {};
                        boolean status = publishError(pid, he, be);
                    }
                } catch (Exception e) {
                    Log.d("TEST", "Exception x:" + e.getMessage());
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                if (iMqttDeliveryToken != null) {
                    MqttMessage mqttMsg = null;
                    try {
                        mqttMsg = iMqttDeliveryToken.getMessage();
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void publishData(JSONObject jr) {
        MqttMessage message = new MqttMessage(jr.toString().getBytes());
        message.setQos(2);
        try {
            mqttAndroidClient.publish(Constants.ERROR_PUBLISH_TOPIC, message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private MqttConnectOptions getMqttConnectionOption() {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
//        mqttConnectOptions.setCleanSession(false);

        //  mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setCleanSession(true);

        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setKeepAliveInterval(1000);
        mqttConnectOptions.setConnectionTimeout(6000);
        //String willMsg = "MSF MQTT Client With Imei No:" + imei;
        //mqttConnectOptions.setWill(client.getTopic("Hive"), willMsg.getBytes(), 2, false);
        return mqttConnectOptions;
    }

    @NonNull
    private DisconnectedBufferOptions getDisconnectedBufferOptions() {
        DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
        disconnectedBufferOptions.setBufferEnabled(true);
        disconnectedBufferOptions.setBufferSize(100);
        disconnectedBufferOptions.setPersistBuffer(false);
        disconnectedBufferOptions.setDeleteOldestMessages(false);
        return disconnectedBufferOptions;
    }

    public void subscribeToBroker(@NonNull MqttAndroidClient client, @NonNull final String topic, int qos) throws MqttException {
        IMqttToken token = client.subscribe(topic, qos);
        token.setActionCallback(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken iMqttToken) {
            }

            @Override
            public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
            }
        });
    }

    public void unSubscribeToBroker(@NonNull MqttAndroidClient client, @NonNull final String topic) throws MqttException {
        IMqttToken token = client.unsubscribe(topic);
        token.setActionCallback(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken iMqttToken) {
            }

            @Override
            public void onFailure(IMqttToken iMqttToken, Throwable throwable) {

            }
        });
    }

    public class AutoTemplateUpload implements Runnable {
        @Override
        public void run() {
            while (true) {
                if (mqttAndroidClient != null && mqttAndroidClient.isConnected()) {
                    ArrayList <EmployeeValidationBasicInfo> empBasicInfoList = null;
                    empBasicInfoList = dbComm.getAutoEnrolledTemplatesForMqtt(empBasicInfoList);
                    if (empBasicInfoList != null) {
                        int size = empBasicInfoList.size();
                        if (size > 0) {
                            for (int count = 0; count < size; count++) {
                                EmployeeValidationBasicInfo info = empBasicInfoList.get(count);
                                ArrayList <EmployeeValidationFingerInfo> fi = info.getfInfoList();
                                if (fi != null && fi.size() > 0) {
                                    String empType = info.getEmpType();
                                    if (empType != null && empType.trim().length() > 0) {
                                        try {
                                            int empTypeId = Integer.parseInt(empType);
                                            SQLiteCommunicator dbComm = new SQLiteCommunicator();
                                            empType = dbComm.getEmpTypeByAutoId(empTypeId);
                                            if (empType != null && empType.trim().length() > 0) {
                                                info.setEmpType(empType);
                                            } else {
                                                info.setEmpType("");
                                            }
                                        } catch (NumberFormatException ne) {
                                            info.setEmpType("");
                                        }
                                    } else {
                                        info.setEmpType("");
                                    }
                                    String templateJson = JSONCreatorParser.createJsonForEnrolledTemplates(info);
                                    MqttMessage message = new MqttMessage(templateJson.getBytes());
                                    message.setQos(2);
                                    try {
                                        mqttAndroidClient.publish(pubTopic, message);
                                        dbComm.updateFingerUploadStatus(info);
                                    } catch (MqttException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public class DashBoardDataUpload implements Runnable {
        @Override
        public void run() {
            while (true) {
                if (mqttAndroidClient != null && mqttAndroidClient.isConnected()) {
                    String dashBoardJson = JSONCreatorParser.createDashBoardData();
                    MqttMessage message = new MqttMessage(dashBoardJson.getBytes());
                    message.setQos(2);
                    try {
                        mqttAndroidClient.publish(pubTopic, message);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    public class AttendanceDataUpload implements Runnable {
        @Override
        public void run() {
            while (true) {
                if (mqttAndroidClient != null && mqttAndroidClient.isConnected()) {
                    CollegeAttendanceInfo attendanceInfo = null;
                    attendanceInfo = dbComm.getCollegeAttendanceData(attendanceInfo);
                    if (attendanceInfo != null) {
                        String attendanceJson = JSONCreatorParser.createCollegeAttendanceJson(attendanceInfo);
                        MqttMessage message = new MqttMessage(attendanceJson.getBytes());
                        message.setQos(2);
                        try {
                            mqttAndroidClient.publish(pubTopic, message);
                            int status = dbComm.updateUploadStatus(attendanceInfo.getAttendanceId());
                            if (status != -1) {
                                Log.d("TEST", "Attendance Upload Successfully");
                            }
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    private boolean publishError(String pid, int[] herr, int[] berr) {
        boolean status = false;
        JSONObject obj = new JSONObject();
        JSONArray he = new JSONArray();
        JSONArray be = new JSONArray();
        try {
            obj.put("PID", pid);
            for (int i = 0; i < herr.length; i++) {
                he.put(herr[i]);
            }
            for (int i = 0; i < berr.length; i++) {
                be.put(berr[i]);
            }
            if (herr.length > 0) {
                obj.put("HES", true);
            } else {
                obj.put("HES", false);
            }
            if (berr.length > 0) {
                obj.put("BES", true);
            } else {
                obj.put("BES", false);
            }
            obj.put("HE", he);
            obj.put("BE", be);
            MqttMessage message = new MqttMessage(obj.toString().getBytes());
            message.setQos(2);
            try {
                mqttAndroidClient.publish(Constants.ERROR_PUBLISH_TOPIC, message);
                status = true;
            } catch (MqttException e) {
                e.printStackTrace();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return status;
    }


    @Override
    public void onTaskRemoved(Intent rootIntent) {
        try {
            if (mqttAndroidClient != null && mqttAndroidClient.isConnected()) {
                mqttAndroidClient.disconnect();
                mqttAndroidClient = null;
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
        Log.d("TEST", "Mqtt Message Service Destroyed");
        stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        Log.d("TEST", "Service on bind");
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
