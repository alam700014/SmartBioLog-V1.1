package com.android.fortunaattendancesystem.submodules;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.fortunaattendancesystem.R;
import com.android.fortunaattendancesystem.activities.EmployeeAttendanceActivity;
import com.android.fortunaattendancesystem.activities.EmployeeFingerEnrollmentActivity;
import com.android.fortunaattendancesystem.activities.FingerEnrollUpdateDialogActivity;
import com.android.fortunaattendancesystem.activities.SmartCardActivity;
import com.android.fortunaattendancesystem.constant.Constants;
import com.android.fortunaattendancesystem.forlinx.ForlinxGPIO;
import com.android.fortunaattendancesystem.helper.Utility;
import com.android.fortunaattendancesystem.info.IdentifyInfo;
import com.android.fortunaattendancesystem.info.MorphoInfo;
import com.android.fortunaattendancesystem.info.ProcessInfo;
import com.android.fortunaattendancesystem.info.VerifyInfo;
import com.android.fortunaattendancesystem.model.EmployeeInfo;
import com.android.fortunaattendancesystem.model.EmployeeValidationBasicInfo;
import com.android.fortunaattendancesystem.model.EmployeeValidationFingerInfo;
import com.android.fortunaattendancesystem.model.SmartCardInfo;
import com.android.fortunaattendancesystem.model.SubInfo;
import com.android.fortunaattendancesystem.model.TemplateDownloadInfo;
import com.android.fortunaattendancesystem.model.TemplateUploadInfo;
import com.android.fortunaattendancesystem.mqtt.MqttApi;
import com.android.fortunaattendancesystem.service.EzeeHrLiteCommunicator;
import com.android.fortunaattendancesystem.service.JSONCreatorParser;
import com.android.fortunaattendancesystem.singleton.EmployeeFingerEnrollInfo;
import com.android.fortunaattendancesystem.singleton.IdentificationInfo;
import com.android.fortunaattendancesystem.singleton.MqttClientInfo;
import com.android.fortunaattendancesystem.singleton.Settings;
import com.android.fortunaattendancesystem.singleton.UserDetails;
import com.android.fortunaattendancesystem.tools.MorphoTools;
import com.morpho.morphosmart.sdk.CallbackMask;
import com.morpho.morphosmart.sdk.CallbackMessage;
import com.morpho.morphosmart.sdk.Coder;
import com.morpho.morphosmart.sdk.CompressionAlgorithm;
import com.morpho.morphosmart.sdk.DetectionMode;
import com.morpho.morphosmart.sdk.EnrollmentType;
import com.morpho.morphosmart.sdk.ErrorCodes;
import com.morpho.morphosmart.sdk.FalseAcceptanceRate;
import com.morpho.morphosmart.sdk.FieldAttribute;
import com.morpho.morphosmart.sdk.LatentDetection;
import com.morpho.morphosmart.sdk.MatchingStrategy;
import com.morpho.morphosmart.sdk.MorphoDatabase;
import com.morpho.morphosmart.sdk.MorphoDevice;
import com.morpho.morphosmart.sdk.MorphoField;
import com.morpho.morphosmart.sdk.MorphoImage;
import com.morpho.morphosmart.sdk.MorphoTypeDeletion;
import com.morpho.morphosmart.sdk.MorphoUser;
import com.morpho.morphosmart.sdk.MorphoWakeUpMode;
import com.morpho.morphosmart.sdk.ResultMatching;
import com.morpho.morphosmart.sdk.Template;
import com.morpho.morphosmart.sdk.TemplateFVPType;
import com.morpho.morphosmart.sdk.TemplateList;
import com.morpho.morphosmart.sdk.TemplateType;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

import static com.android.fortunaattendancesystem.service.EzeeHrLiteCommunicator.imei;

/**
 * Created by fortuna on 18/9/18.
 */

public class MorphoCommunicator implements Observer {

    private MorphoDevice morphoDevice = null;
    private MorphoDatabase morphoDatabase = null;

    String strMessage = new String();
    private boolean isCaptureVerif = false;
    private int index;

    private Handler mHandler = new Handler();
    private Handler pHandler = new Handler();

    Dialog alertDialog = null;
    Dialog successEmpDetailsDialog = null;
    Dialog failureEmpDetailsDialog = null;

    AlertDialog fingerEnrollAlert = null;
    AlertDialog fingerUpdateAlert = null;

    int identifyRetvalue;
    public Context context;

    private static HashMap <String, Template> map = new HashMap <String, Template>();
    TelephonyManager tel = null;

    ArrayList <String> finalList = new ArrayList <String>();
    ArrayList <SubInfo> allList = new ArrayList <SubInfo>();
    public static boolean isLoggedIn = false;
    int tempId = -1;

    String profEID = "", profCID = "";
    String studentEID = "", studentCID = "";
    String subCode_subName = "", subType = "";

    SQLiteCommunicator dbComm = new SQLiteCommunicator();

    public MorphoCommunicator() {
    }

    public MorphoCommunicator(Context context) {
        this.context = context;
        tel = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    }

    //======================== Morpho Finger Enroll Start  =============================//

    public void startFingerEnroll() {
        morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
        morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
        if (morphoDevice != null && morphoDatabase != null) {
            Long noOfFreeRecords = new Long(10);
            int ret = morphoDatabase.getNbFreeRecord(noOfFreeRecords);
            if (ret == 0 && noOfFreeRecords > 0) {
                EmployeeFingerEnrollInfo empFingerInfo = retreiveEnrollSettings();
                if (empFingerInfo != null) {
                    int resId = getResIdFromContext("imageView1");
                    FingerEnrollUpdateDialogActivity.currentCaptureBitmapId = resId;
                    enroll(this);//Start Finger Enroll
                    try {
                        ProcessInfo.getInstance().setCommandBioStart(true);
                        ProcessInfo.getInstance().setStarted(true);
                    } catch (Exception e) {
                    }
                } else {
                    showCustomAlertDialog(R.drawable.failure, "Device Connection Status", "Employee info not found !", false);
                }
            } else {
                showCustomAlertDialog(R.drawable.failure, "Device Connection Status", "Morpho Database Full !", false);
            }
        } else {
            showCustomAlertDialog(R.drawable.failure, "Device Connection Status", "Finger Reader Not Found", false);
        }
    }

    //==============================  Morpho Finger Update Start  ================================//

    public void startFingerUpdate() {
        morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
        morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
        if (morphoDevice != null && morphoDatabase != null) {
            Long noOfFreeRecords = new Long(10);
            int ret = morphoDatabase.getNbFreeRecord(noOfFreeRecords);
            if (ret == 0 && noOfFreeRecords > 0) {
                EmployeeFingerEnrollInfo empFingerInfo = retreiveEnrollSettings();
                if (empFingerInfo != null) {
                    int resId = getResIdFromContext("imageView1");
                    FingerEnrollUpdateDialogActivity.currentCaptureBitmapId = resId;
                    update(this);//Start Finger Update
                    try {
                        ProcessInfo.getInstance().setCommandBioStart(true);
                        ProcessInfo.getInstance().setStarted(true);
                    } catch (Exception e) {
                    }
                } else {
                    showCustomAlertDialog(R.drawable.failure, "Device Connection Status", "Employee info not found !", false);
                }
            } else {
                showCustomAlertDialog(R.drawable.failure, "Device Connection Status", "Morpho Database Full !", false);
            }
        } else {
            showCustomAlertDialog(R.drawable.failure, "Device Connection Status", "Finger Reader Not Found", false);
        }
    }

    //============================  Morpho Finger Identify Start  ==================================//

    public void startFingerIdentification() {
        morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
        morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
        if (morphoDevice != null && morphoDatabase != null) {
            Log.d("TEST", "***************** Start Finger Identification *******************");
            int internalError = morphoDevice.getInternalError();
            Log.d("TEST", "Morpho Internal Error:" + internalError);
            if (identifyRetvalue != -1 || identifyRetvalue != -2 || identifyRetvalue != -3) {//-1:MORPHOERR_INTERNAL,-2:MORPHOERR_PROTOCOLE,-3:MORPHOERR_CONNECT
                boolean isIdentificationStarted = ProcessInfo.getInstance().isIdentificationStarted();
                boolean isVerificationStarted = ProcessInfo.getInstance().isVerificationStarted();
                boolean isBioCommandStarted = ProcessInfo.getInstance().isCommandBioStart();
                Log.d("TEST", " Is Identification Started:" + isIdentificationStarted);
                Log.d("TEST", "Is Verification Started:" + isVerificationStarted);
                Log.d("TEST", "Is Bio Command Started:" + isBioCommandStarted);
                Log.d("TEST", "*************************************************************");
                if (!isIdentificationStarted && !isVerificationStarted && !isBioCommandStarted) {
                    Long noOfRecords = new Long(10);
                    int ret = morphoDatabase.getNbUsedRecord(noOfRecords);
                    if (ret == 0 && noOfRecords > 0) {
                        if (context != null) {
                            final Activity activity = (Activity) context;
                            if (activity != null && activity instanceof EmployeeAttendanceActivity && !activity.isFinishing()) {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        TextView textViewPutFingerMessage = activity.findViewById(getResIdFromContext("putFingerMessage"));
                                        textViewPutFingerMessage.setText("Put Finger On Sensor To Be Identified");
                                    }
                                });
                            }
                        }
                        MorphoInfo info = retrieveIdentifySettings();
                        if (info != null) {
                            ProcessInfo.getInstance().setMorphoInfo(info);
                            int resId = getResIdFromContext("imageView1");
                            EmployeeAttendanceActivity.currentCaptureBitmapId = resId;
                            identify(this);
                            try {
                                IdentificationInfo.getInstance().reset();//Reset Identification Info Singleton class
                                ProcessInfo.getInstance().setReStartIdentification(true);
                                ProcessInfo.getInstance().setIdentificationStarted(true);
                                ProcessInfo.getInstance().setCommandBioStart(true);
                                ProcessInfo.getInstance().setStarted(true);
                            } catch (Exception e) {
                            }
                        }
                    } else {
                        if (context != null) {
                            final Activity activity = (Activity) context;
                            if (activity != null && activity instanceof EmployeeAttendanceActivity && !activity.isFinishing()) {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Activity activity = (Activity) context;
                                        TextView textViewPutFingerMessage = activity.findViewById(getResIdFromContext("putFingerMessage"));
                                        textViewPutFingerMessage.setText("Morpho Database Empty ! ");
                                    }
                                });
                            }
                        }
                    }
                }
            } else {
                showCustomAlertDialog(R.drawable.failure, "Device Connection Status", convertToInternationalMessage(identifyRetvalue, internalError), false);
            }
        } else {
            showCustomAlertDialog(R.drawable.failure, "Device Connection Status", "Finger Reader Not Found", false);
        }
    }

    //============================= Morpho Finger Identify Stop  ==================================//

    public void stopFingerIdentification() {
        int fr = Settings.getInstance().getFrTypeValue();
        if (fr == 0) {
            morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
            morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
            if (morphoDevice != null && morphoDatabase != null) {
                boolean isIdentificationStarted = ProcessInfo.getInstance().isIdentificationStarted();
                boolean isVerificationStarted = ProcessInfo.getInstance().isVerificationStarted();
                boolean isBioCommandStarted = ProcessInfo.getInstance().isCommandBioStart();
                Log.d("TEST", "******************* Stop Identification ****************");
                Log.d("TEST", "Is Identification Started:" + isIdentificationStarted);
                Log.d("TEST", "Is Verification Started:" + isVerificationStarted);
                Log.d("TEST", "Is Bio Command Started:" + isBioCommandStarted);
                Log.d("TEST", "********************************************************");
                if ((isIdentificationStarted || isVerificationStarted) && isBioCommandStarted) {
                    ProcessInfo.getInstance().setIdentificationStarted(false);
                    ProcessInfo.getInstance().setVerificationStarted(false);
                    ProcessInfo.getInstance().setCommandBioStart(false);
                    morphoDevice.cancelLiveAcquisition();
                }
            }
        }
    }

    //============================== Morpho Finger Verify Start ===================================//

    public void startFingerVerification(int mode, Object details) {
        switch (mode) {
            case Constants.VERIFY_BY_CARD_MODE:
                morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
                morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
                if (morphoDevice != null && morphoDatabase != null) {
                    MorphoInfo info = retrieveVerifySettings();
                    if (info != null) {
                        SmartCardInfo cardInfo = (SmartCardInfo) details;
                        ProcessInfo.getInstance().setMorphoInfo(info);
                        int resId = getResIdFromContext("imageView1");
                        EmployeeAttendanceActivity.currentCaptureBitmapId = resId;
                        verify(this, mode, cardInfo);
                        try {
                            ProcessInfo.getInstance().setReStartIdentification(false);
                            ProcessInfo.getInstance().setVerificationStarted(true);
                            ProcessInfo.getInstance().setCommandBioStart(true);
                            ProcessInfo.getInstance().setStarted(true);
                        } catch (Exception e) {
                        }
                    }
                } else {
                    showCustomAlertDialog(R.drawable.failure, "Device Connection Status", "Finger Reader Not Found", false);
                }
                break;
            case Constants.VERIFY_BY_LOCAL_DATABASE_MODE:
                morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
                morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
                if (morphoDevice != null && morphoDatabase != null) {
                    MorphoInfo info = retrieveVerifySettings();
                    if (info != null) {
                        EmployeeInfo empInfo = (EmployeeInfo) details;
                        ProcessInfo.getInstance().setMorphoInfo(info);
                        int resId = getResIdFromContext("imageView1");
                        EmployeeAttendanceActivity.currentCaptureBitmapId = resId;
                        verify(this, mode, empInfo);
                        try {
                            ProcessInfo.getInstance().setReStartIdentification(false);
                            ProcessInfo.getInstance().setVerificationStarted(true);
                            ProcessInfo.getInstance().setCommandBioStart(true);
                            ProcessInfo.getInstance().setStarted(true);
                        } catch (Exception e) {
                        }
                    }
                } else {
                    showCustomAlertDialog(R.drawable.failure, "Device Connection Status", "Finger Reader Not Found", false);
                }
                break;
            default:
                break;

        }
    }

    //===============================  Morpho Finger Enroll ==================================//

    private void enroll(final Observer observer) {

        Thread commandThread = (new Thread(new Runnable() {
            @Override
            public void run() {
                String message = "";
                final TemplateList templateList = new TemplateList();
                final EmployeeFingerEnrollInfo empFingerInfo = EmployeeFingerEnrollInfo.getInstance();
                if (empFingerInfo != null) {

                    int timeout;
                    int acquisitionThreshold = 0;
                    int advancedSecurityLevelsRequired = 0;
                    TemplateType templateType = TemplateType.MORPHO_PK_ISO_FMR;
                    TemplateFVPType templateFVPType = TemplateFVPType.MORPHO_NO_PK_FVP;
                    EnrollmentType enrollType = EnrollmentType.THREE_ACQUISITIONS;

                    boolean exportFVP = false, exportFP = false;
                    LatentDetection latentDetection;
                    Coder coderChoice;
                    int detectModeChoice;

                    //==========For Template Size 0f 252 bytes set maxSizeTemplate=37(Number of Minutae)===========//
                    int maxSizeTemplate = 37;

                    //=============================================================================================//
                    //==========For Template Size 0f 228 bytes set maxSizeTemplate=33(Number of Minutae)===========//
                    //int maxSizeTemplate = 33;
                    //=============================================================================================//
                    //int maxSizeTemplate=1;


                    ProcessInfo processInfo = ProcessInfo.getInstance();
                    timeout = processInfo.getEnrollTimeout();
                    if (processInfo.isFingerprintQualityThreshold()) {
                        acquisitionThreshold = processInfo.getFingerprintQualityThresholdvalue();
                    }
                    if (templateFVPType != TemplateFVPType.MORPHO_NO_PK_FVP) {
                        exportFVP = true;
                    }
                    latentDetection = LatentDetection.LATENT_DETECT_DISABLE;
                    coderChoice = processInfo.getCoder();
                    detectModeChoice = DetectionMode.MORPHO_ENROLL_DETECT_MODE.getValue();
                    if (processInfo.isForceFingerPlacementOnTop()) {
                        detectModeChoice |= DetectionMode.MORPHO_FORCE_FINGER_ON_TOP_DETECT_MODE.getValue();
                    }
                    if (processInfo.isWakeUpWithLedOff()) {
                        detectModeChoice |= MorphoWakeUpMode.MORPHO_WAKEUP_LED_OFF.getCode();
                    }
                    CompressionAlgorithm compressAlgo = empFingerInfo.getCompressionAlgorithm();
                    if (!compressAlgo.equals(CompressionAlgorithm.NO_IMAGE)) {
                        templateList.setActivateFullImageRetrieving(true);
                    }
                    int callbackCmd = ProcessInfo.getInstance().getCallbackCmd();

                    int noOfFingers = empFingerInfo.getNoOfFingers();
                    int ret = morphoDevice.setStrategyAcquisitionMode(ProcessInfo.getInstance().getStrategyAcquisitionMode());

                    if (ret == ErrorCodes.MORPHO_OK) {
                        ret = morphoDevice.capture(timeout, acquisitionThreshold, advancedSecurityLevelsRequired,
                                noOfFingers, templateType, templateFVPType, maxSizeTemplate, enrollType,
                                latentDetection, coderChoice, detectModeChoice, CompressionAlgorithm.MORPHO_NO_COMPRESS, 0, templateList, callbackCmd, observer);
                    }

                    ProcessInfo.getInstance().setCommandBioStart(false);

                    if (ret == ErrorCodes.MORPHO_OK) {
                        if (noOfFingers == 1) {
                            TemplateList firstFingerTempList = new TemplateList();
                            Template firstFingerTemplate = templateList.getTemplate(0);
                            byte[] firstFingerTemplateData = firstFingerTemplate.getData();
                            firstFingerTemplate.setTemplateType(TemplateType.MORPHO_PK_ISO_FMR);
                            firstFingerTemplate.setData(firstFingerTemplateData);
                            firstFingerTemplate.setDataIndex(0);
                            firstFingerTempList.putTemplate(firstFingerTemplate);
                            MorphoUser firstUser = new MorphoUser();
                            ResultMatching firstUserMatch = new ResultMatching();
                            ret = morphoDatabase.identifyMatch(FalseAcceptanceRate.MORPHO_FAR_5, firstFingerTempList, firstUser, firstUserMatch);
                            if (ret == -8 || ret == -11) {//-8 Finger Identification Failed , -11 Morpho Database Empty
                                ret = ErrorCodes.MORPHO_OK;
                                boolean l_activateFullImageRetrieve = templateList.isActivateFullImageRetrieving();
                                try {
                                    for (int fingerIndex = 0; fingerIndex < noOfFingers; ++fingerIndex) {
                                        if (fingerIndex == 0) {//First Finger Index
                                            if (ret == ErrorCodes.MORPHO_OK && ((templateType != TemplateType.MORPHO_NO_PK_FP))) {
                                                byte[] firstFingerFMD = new byte[252];
                                                for (int i = 0; i < 252; i++) firstFingerFMD[i] = 0;
                                                Template t = templateList.getTemplate(fingerIndex);
                                                firstFingerFMD = t.getData();
                                                final StringBuilder builder = new StringBuilder();
                                                for (byte b : firstFingerFMD) {
                                                    builder.append(String.format("%02x", b));
                                                }
                                                String strFirstFingerDataHex = builder.toString().toUpperCase();
                                                if (strFirstFingerDataHex != null && strFirstFingerDataHex.length() < 504) {
                                                    int len;
                                                    while ((len = strFirstFingerDataHex.length()) < 504) {
                                                        strFirstFingerDataHex = strFirstFingerDataHex + "0";
                                                    }
                                                }
                                                empFingerInfo.setFirstFingerFMD(firstFingerFMD);
                                                empFingerInfo.setStrFirstFingerDataHex(strFirstFingerDataHex);
                                            }
                                            if (ret == ErrorCodes.MORPHO_OK && l_activateFullImageRetrieve) {
                                                if (compressAlgo.equals(CompressionAlgorithm.MORPHO_COMPRESS_WSQ) || compressAlgo.equals(CompressionAlgorithm.MORPHO_COMPRESS_V1)) {
                                                    //Case of WSQ or morpho_v1 image
                                                    //data = templateList.getImage(fingerIndex).getCompressedImage();
                                                } else {
                                                    //Case of RAW Image
                                                    byte[] firstFingerFID = templateList.getImage(fingerIndex).getImage();
                                                    empFingerInfo.setFirstFingerFID(firstFingerFID);
                                                    message = "Finger Enrolled Successfully!!!!!";
                                                }
                                            }

                                        }

                                    }
                                } catch (Exception e) {
                                    Log.i("Enroll Error:", e.getMessage());
                                }
                            } else {
                                empFingerInfo.setTemplateExists(true);
                                message += "Finger Template Already Exists Against : \n\n";
                                message += "Employee Details : \n";
                                message += "Employee Id : " + firstUser.getField(0) + "\n";
                                message += "Card Id : " + firstUser.getField(1) + "\n";
                                message += "Name : " + firstUser.getField(2);
                            }
                        } else if (noOfFingers == 2) {
                            TemplateList firstFingerTempList = new TemplateList();
                            Template firstFingerTemplate = templateList.getTemplate(0);
                            byte[] firstFingerTemplateData = firstFingerTemplate.getData();
                            firstFingerTemplate.setTemplateType(TemplateType.MORPHO_PK_ISO_FMR);
                            firstFingerTemplate.setData(firstFingerTemplateData);
                            firstFingerTemplate.setDataIndex(0);
                            firstFingerTempList.putTemplate(firstFingerTemplate);
                            MorphoUser firstUser = new MorphoUser();
                            ResultMatching firstUserMatch = new ResultMatching();
                            ret = morphoDatabase.identifyMatch(FalseAcceptanceRate.MORPHO_FAR_5, firstFingerTempList, firstUser, firstUserMatch);
                            if (ret == -8 || ret == -11) {//-8 Finger Identification Failed , -11 Morpho Database Empty
                                TemplateList secondFingerTempList = new TemplateList();
                                Template secondFingerTemplate = templateList.getTemplate(1);
                                byte[] secondFingerTemplateData = secondFingerTemplate.getData();
                                secondFingerTemplate.setTemplateType(TemplateType.MORPHO_PK_ISO_FMR);
                                secondFingerTemplate.setData(secondFingerTemplateData);
                                secondFingerTemplate.setDataIndex(0);
                                secondFingerTempList.putTemplate(secondFingerTemplate);
                                MorphoUser secondUser = new MorphoUser();
                                ResultMatching secondUserMatch = new ResultMatching();
                                ret = morphoDatabase.identifyMatch(FalseAcceptanceRate.MORPHO_FAR_5, secondFingerTempList, secondUser, secondUserMatch);
                                if (ret == -8 || ret == -11) {//-8 Finger Identification Failed , -11 Morpho Database Empty
                                    ret = ErrorCodes.MORPHO_OK;
                                    if (ret == ErrorCodes.MORPHO_OK) {
                                        boolean l_activateFullImageRetrieve = templateList.isActivateFullImageRetrieving();
                                        try {
                                            for (int fingerIndex = 0; fingerIndex < noOfFingers; ++fingerIndex) {
                                                if (fingerIndex == 0) {
                                                    if (ret == ErrorCodes.MORPHO_OK && ((templateType != TemplateType.MORPHO_NO_PK_FP))) {
                                                        byte[] firstFingerFMD = new byte[252];
                                                        for (int i = 0; i < 252; i++)
                                                            firstFingerFMD[i] = 0;
                                                        Template t = templateList.getTemplate(fingerIndex);
                                                        firstFingerFMD = t.getData();
                                                        final StringBuilder builder = new StringBuilder();
                                                        for (byte b : firstFingerFMD) {
                                                            builder.append(String.format("%02x", b));
                                                        }
                                                        String strFirstFingerDataHex = builder.toString().toUpperCase();
                                                        if (strFirstFingerDataHex != null && strFirstFingerDataHex.length() < 504) {
                                                            int len;
                                                            while ((len = strFirstFingerDataHex.length()) < 504) {
                                                                strFirstFingerDataHex = strFirstFingerDataHex + "0";
                                                            }
                                                        }
                                                        empFingerInfo.setFirstFingerFMD(firstFingerFMD);
                                                        empFingerInfo.setStrFirstFingerDataHex(strFirstFingerDataHex);
                                                    }
                                                    if (ret == ErrorCodes.MORPHO_OK && l_activateFullImageRetrieve) {
                                                        if (compressAlgo.equals(CompressionAlgorithm.MORPHO_COMPRESS_WSQ) || compressAlgo.equals(CompressionAlgorithm.MORPHO_COMPRESS_V1)) {
                                                            //Case of WSQ or morpho_v1 image
                                                            //data = templateList.getImage(fingerIndex).getCompressedImage();
                                                        } else {
                                                            //Case of RAW Image
                                                            byte[] firstFingerFID = templateList.getImage(fingerIndex).getImage();
                                                            empFingerInfo.setFirstFingerFID(firstFingerFID);
                                                            message = "Finger Enrolled Successfully!!!!!";

                                                        }
                                                    }
                                                } else if (fingerIndex == 1) {//Second Finger Index
                                                    if (ret == ErrorCodes.MORPHO_OK && ((templateType != TemplateType.MORPHO_NO_PK_FP))) {
                                                        Template t = templateList.getTemplate(fingerIndex);
                                                        byte[] secondFingerFMD = new byte[252];
                                                        for (int i = 0; i < 252; i++)
                                                            secondFingerFMD[i] = 0;
                                                        secondFingerFMD = t.getData();
                                                        final StringBuilder builder = new StringBuilder();
                                                        for (byte b : secondFingerFMD) {
                                                            builder.append(String.format("%02x", b));
                                                        }
                                                        String strSecondFingerDataHex = builder.toString().toUpperCase();
                                                        if (strSecondFingerDataHex != null && strSecondFingerDataHex.length() < 504) {
                                                            int len;
                                                            while ((len = strSecondFingerDataHex.length()) < 504) {
                                                                strSecondFingerDataHex = strSecondFingerDataHex + "0";
                                                            }
                                                        }
                                                        empFingerInfo.setSecondFingerFMD(secondFingerFMD);
                                                        empFingerInfo.setStrSecondFingerDataHex(strSecondFingerDataHex);
                                                    }
                                                    if (ret == ErrorCodes.MORPHO_OK && l_activateFullImageRetrieve) {
                                                        if (compressAlgo.equals(CompressionAlgorithm.MORPHO_COMPRESS_WSQ) || compressAlgo.equals(CompressionAlgorithm.MORPHO_COMPRESS_V1)) {
                                                            //Case of WSQ or morpho_v1 image
                                                            //data = templateList.getImage(fingerIndex).getCompressedImage();
                                                        } else {
                                                            //Case of RAW Image
                                                            byte[] secondFingerFID = templateList.getImage(fingerIndex).getImage();
                                                            empFingerInfo.setSecondFingerFID(secondFingerFID);
                                                            message = "Finger Enrolled Successfully!!!!!";
                                                        }
                                                    }
                                                }
                                            }
                                        } catch (Exception e) {
                                            Log.i("Enroll Error:", e.getMessage());
                                        }
                                    }
                                } else {
                                    empFingerInfo.setTemplateExists(true);
                                    message += "Second Finger Template Already Exists Against : \n\n";
                                    message += "Employee Details : \n";
                                    message += "Employee Id : " + secondUser.getField(0) + "\n";
                                    message += "Card Id : " + secondUser.getField(1) + "\n";
                                    message += "Name : " + secondUser.getField(2);
                                }
                            } else {
                                empFingerInfo.setTemplateExists(true);
                                message += "First Finger Template Already Exists Against : \n\n";
                                message += "Employee Details : \n";
                                message += "Employee Id : " + firstUser.getField(0) + "\n";
                                message += "Card Id : " + firstUser.getField(1) + "\n";
                                message += "Name : " + firstUser.getField(2);
                            }
                        }
                    }

                    final String alertMessage = message;
                    final int internalError = morphoDevice.getInternalError();
                    final int retvalue = ret;
                    final boolean duplicateTempFound = empFingerInfo.isTemplateExists();

                    mHandler.post(new Runnable() {
                        @Override
                        public synchronized void run() {
                            if (context != null) {
                                Activity activity = (Activity) context;
                                if (!activity.isFinishing()) {
                                    if (retvalue == ErrorCodes.MORPHO_OK && duplicateTempFound) {
                                        String enrollType = empFingerInfo.getEnrollType();
                                        if (enrollType != null && enrollType.trim().length() > 0 && enrollType.equals("RE")) {
                                            String statusMsg = "0000";
                                            String url = "http://" + EzeeHrLiteCommunicator.serverIP + ":" + EzeeHrLiteCommunicator.serverPort + Constants.POST_UNFINISHED_JOB_URL;
                                            String jid = empFingerInfo.getRemoteEnrollJobId();
                                            String postJobJson = JSONCreatorParser.getPostJobJson(Constants.CORPORATE_ID, imei, EzeeHrLiteCommunicator.deviceToken, jid, statusMsg);
                                            AsyncTaskPostUnfinishedJob task = new AsyncTaskPostUnfinishedJob();
                                            task.execute(url, postJobJson);
                                        }
                                        alert(-111, internalError, "Enroll Error", alertMessage);//User Defined Error For Template Exits=-111
                                    } else if (retvalue == ErrorCodes.MORPHO_OK && !duplicateTempFound) {
                                        enrollDialog("Enroll Status", alertMessage, templateList);
                                    } else {
                                        String enrollType = empFingerInfo.getEnrollType();
                                        if (enrollType != null && enrollType.trim().length() > 0 && enrollType.equals("RE")) {
                                            String statusMsg = "0000";
                                            String url = "http://" + EzeeHrLiteCommunicator.serverIP + ":" + EzeeHrLiteCommunicator.serverPort + Constants.POST_UNFINISHED_JOB_URL;
                                            String jid = empFingerInfo.getRemoteEnrollJobId();
                                            String postJobJson = JSONCreatorParser.getPostJobJson(Constants.CORPORATE_ID, imei, EzeeHrLiteCommunicator.deviceToken, jid, statusMsg);
                                            AsyncTaskPostUnfinishedJob task = new AsyncTaskPostUnfinishedJob();
                                            task.execute(url, postJobJson);
                                        }
                                        alert(retvalue, internalError, "Enroll Error", alertMessage);
                                    }
                                }
                            }
                        }
                    });
                    notifyEnrollUpdateEndProcess();
                }
            }
        }));
        commandThread.start();
    }

    //================================  Morpho Finger Update =====================================//

    private void update(final Observer observer) {

        Thread commandThread = (new Thread(new Runnable() {
            @Override
            public void run() {
                String message = "";
                final TemplateList templateList = new TemplateList();
                EmployeeFingerEnrollInfo empFingerInfo = EmployeeFingerEnrollInfo.getInstance();
                if (empFingerInfo != null) {
                    int timeout;
                    int acquisitionThreshold = 0;
                    int advancedSecurityLevelsRequired = 0;
                    EnrollmentType enrollType = EnrollmentType.THREE_ACQUISITIONS;
                    TemplateType templateType = TemplateType.MORPHO_PK_ISO_FMR;
                    TemplateFVPType templateFVPType = TemplateFVPType.MORPHO_NO_PK_FVP;
                    LatentDetection latentDetection;
                    Coder coderChoice;
                    int detectModeChoice;

                    boolean exportFVP = false, exportFP = false;

                    //======================For Template Size 0f 252 bytes set maxSizeTemplate=37(Number of Minutae)===========//
                    int maxSizeTemplate = 37;
                    //=============================================================================================//

                    //========================For Template Size 0f 228 bytes set maxSizeTemplate=33(Number of Minutae)===========//
                    //int maxSizeTemplate = 33;
                    //=============================================================================================//


                    ProcessInfo processInfo = ProcessInfo.getInstance();
                    timeout = processInfo.getEnrollTimeout();
                    if (processInfo.isFingerprintQualityThreshold()) {
                        acquisitionThreshold = processInfo.getFingerprintQualityThresholdvalue();
                    }
                    if (templateFVPType != TemplateFVPType.MORPHO_NO_PK_FVP) {
                        exportFVP = true;
                    }
                    latentDetection = LatentDetection.LATENT_DETECT_DISABLE;
                    coderChoice = processInfo.getCoder();
                    detectModeChoice = DetectionMode.MORPHO_ENROLL_DETECT_MODE.getValue();
                    if (processInfo.isForceFingerPlacementOnTop()) {
                        detectModeChoice |= DetectionMode.MORPHO_FORCE_FINGER_ON_TOP_DETECT_MODE.getValue();
                    }
                    if (processInfo.isWakeUpWithLedOff()) {
                        detectModeChoice |= MorphoWakeUpMode.MORPHO_WAKEUP_LED_OFF.getCode();
                    }
                    CompressionAlgorithm compressAlgo = empFingerInfo.getCompressionAlgorithm();
                    if (!compressAlgo.equals(CompressionAlgorithm.NO_IMAGE)) {
                        templateList.setActivateFullImageRetrieving(true);
                    }
                    int callbackCmd = ProcessInfo.getInstance().getCallbackCmd();
                    int noOfFingers = empFingerInfo.getNoOfFingers();
                    int ret = morphoDevice.setStrategyAcquisitionMode(ProcessInfo.getInstance().getStrategyAcquisitionMode());
                    if (ret == 0) {
                        ret = morphoDevice.capture(timeout, acquisitionThreshold, advancedSecurityLevelsRequired,
                                noOfFingers, templateType, templateFVPType, maxSizeTemplate, enrollType,
                                latentDetection, coderChoice, detectModeChoice, CompressionAlgorithm.MORPHO_NO_COMPRESS, 0, templateList, callbackCmd, observer);
                    }

                    ProcessInfo.getInstance().setCommandBioStart(false);

                    if (ret == ErrorCodes.MORPHO_OK) {
                        if (noOfFingers == 1) {
                            TemplateList firstFingerTempList = new TemplateList();
                            Template firstFingerTemplate = templateList.getTemplate(0);
                            byte[] firstFingerTemplateData = firstFingerTemplate.getData();
                            firstFingerTemplate.setTemplateType(TemplateType.MORPHO_PK_ISO_FMR);
                            firstFingerTemplate.setData(firstFingerTemplateData);
                            firstFingerTemplate.setDataIndex(0);
                            firstFingerTempList.putTemplate(firstFingerTemplate);
                            MorphoUser firstUser = new MorphoUser();
                            ResultMatching firstUserMatch = new ResultMatching();
                            ret = morphoDatabase.identifyMatch(FalseAcceptanceRate.MORPHO_FAR_5, firstFingerTempList, firstUser, firstUserMatch);
                            if (ret == ErrorCodes.MORPHO_OK) {//If Match Found
                                //Check if matched  emp id and emp id to be updated is same if found same proceed with update
                                if (firstUser.getField(0).equals(empFingerInfo.getEmpId()) && firstUser.getField(1).equals(empFingerInfo.getCardId()) && firstUser.getField(2).equals(empFingerInfo.getEmpName())) {
                                    boolean l_activateFullImageRetrieve = templateList.isActivateFullImageRetrieving();
                                    try {
                                        for (int fingerIndex = 0; fingerIndex < noOfFingers; ++fingerIndex) {
                                            if (fingerIndex == 0) {
                                                if (ret == ErrorCodes.MORPHO_OK && ((templateType != TemplateType.MORPHO_NO_PK_FP))) {
                                                    byte[] firstFingerFMD = new byte[252];
                                                    for (int i = 0; i < 252; i++)
                                                        firstFingerFMD[i] = 0;
                                                    Template t = templateList.getTemplate(fingerIndex);
                                                    firstFingerFMD = t.getData();
                                                    final StringBuilder builder = new StringBuilder();
                                                    for (byte b : firstFingerFMD) {
                                                        builder.append(String.format("%02x", b));
                                                    }
                                                    String strFirstFingerDataHex = builder.toString().toUpperCase();
                                                    if (strFirstFingerDataHex != null && strFirstFingerDataHex.length() < 504) {
                                                        int len;
                                                        while ((len = strFirstFingerDataHex.length()) < 504) {
                                                            strFirstFingerDataHex = strFirstFingerDataHex + "0";
                                                        }
                                                    }
                                                    empFingerInfo.setFirstFingerFMD(firstFingerFMD);
                                                    empFingerInfo.setStrFirstFingerDataHex(strFirstFingerDataHex);
                                                }

                                                if (ret == ErrorCodes.MORPHO_OK && l_activateFullImageRetrieve) {
                                                    if (compressAlgo.equals(CompressionAlgorithm.MORPHO_COMPRESS_WSQ) || compressAlgo.equals(CompressionAlgorithm.MORPHO_COMPRESS_V1)) {
                                                        //Case of WSQ or morpho_v1 image
                                                        //data = templateList.getImage(fingerIndex).getCompressedImage();
                                                    } else {
                                                        //Case of RAW Image
                                                        byte[] firstFingerFID = templateList.getImage(fingerIndex).getImage();
                                                        empFingerInfo.setFirstFingerFID(firstFingerFID);
                                                        message = "Finger Updated Successfully!!!!!";
                                                    }
                                                }
                                            }
                                        }
                                    } catch (Exception e) {
                                        Log.i("Update Error:", e.getMessage());
                                    }
                                } else {
                                    empFingerInfo.setTemplateExists(true);
                                    message += "Finger Template Already Exists Against : \n\n";
                                    message += "Employee Details : \n";
                                    message += "Employee Id : " + firstUser.getField(0) + "\n";
                                    message += "Card Id : " + firstUser.getField(1) + "\n";
                                    message += "Name : " + firstUser.getField(2);
                                }
                            } else if (ret == -8 || ret == -11) {//-8 Finger did not match , -11 Morpho Database Empty
                                boolean l_activateFullImageRetrieve = templateList.isActivateFullImageRetrieving();
                                try {
                                    ret = ErrorCodes.MORPHO_OK;
                                    for (int fingerIndex = 0; fingerIndex < noOfFingers; ++fingerIndex) {
                                        if (fingerIndex == 0) {
                                            if (ret == ErrorCodes.MORPHO_OK && ((templateType != TemplateType.MORPHO_NO_PK_FP))) {
                                                byte[] firstFingerFMD = new byte[252];
                                                for (int i = 0; i < 252; i++) firstFingerFMD[i] = 0;
                                                Template t = templateList.getTemplate(fingerIndex);
                                                firstFingerFMD = t.getData();
                                                final StringBuilder builder = new StringBuilder();
                                                for (byte b : firstFingerFMD) {
                                                    builder.append(String.format("%02x", b));
                                                }
                                                String strFirstFingerDataHex = builder.toString().toUpperCase();
                                                if (strFirstFingerDataHex != null && strFirstFingerDataHex.length() < 504) {
                                                    int len;
                                                    while ((len = strFirstFingerDataHex.length()) < 504) {
                                                        strFirstFingerDataHex = strFirstFingerDataHex + "0";
                                                    }
                                                }
                                                empFingerInfo.setFirstFingerFMD(firstFingerFMD);
                                                empFingerInfo.setStrFirstFingerDataHex(strFirstFingerDataHex);
                                            }
                                            if (ret == ErrorCodes.MORPHO_OK && l_activateFullImageRetrieve) {
                                                if (compressAlgo.equals(CompressionAlgorithm.MORPHO_COMPRESS_WSQ) || compressAlgo.equals(CompressionAlgorithm.MORPHO_COMPRESS_V1)) {
                                                    //Case of WSQ or morpho_v1 image
                                                    //data = templateList.getImage(fingerIndex).getCompressedImage();
                                                } else {
                                                    //Case of RAW Image
                                                    byte[] firstFingerFID = templateList.getImage(fingerIndex).getImage();
                                                    empFingerInfo.setFirstFingerFID(firstFingerFID);
                                                    message = "Finger Updated Successfully!!!!!";
                                                }
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.i("Update Error", e.getMessage());
                                }
                            }
                        } else if (noOfFingers == 2) {
                            TemplateList firstFingerTempList = new TemplateList();
                            Template firstFingerTemplate = templateList.getTemplate(0);
                            byte[] firstFingerTemplateData = firstFingerTemplate.getData();
                            firstFingerTemplate.setTemplateType(TemplateType.MORPHO_PK_ISO_FMR);
                            firstFingerTemplate.setData(firstFingerTemplateData);
                            firstFingerTemplate.setDataIndex(0);
                            firstFingerTempList.putTemplate(firstFingerTemplate);
                            MorphoUser firstUser = new MorphoUser();
                            ResultMatching firstUserMatch = new ResultMatching();
                            ret = morphoDatabase.identifyMatch(FalseAcceptanceRate.MORPHO_FAR_5, firstFingerTempList, firstUser, firstUserMatch);
                            if (ret == ErrorCodes.MORPHO_OK) {//Match Found
                                //Check if matched  emp id and emp id to be updated is same if found same proceed with update
                                if (firstUser.getField(0).equals(empFingerInfo.getEmpId()) && firstUser.getField(1).equals(empFingerInfo.getCardId()) && firstUser.getField(2).equals(empFingerInfo.getEmpName())) {
                                    TemplateList secondFingerTempList = new TemplateList();
                                    Template secondFingerTemplate = templateList.getTemplate(1);
                                    byte[] secondFingerTemplateData = secondFingerTemplate.getData();
                                    secondFingerTemplate.setTemplateType(TemplateType.MORPHO_PK_ISO_FMR);
                                    secondFingerTemplate.setData(secondFingerTemplateData);
                                    secondFingerTemplate.setDataIndex(0);
                                    secondFingerTempList.putTemplate(secondFingerTemplate);
                                    MorphoUser secondUser = new MorphoUser();
                                    ResultMatching secondUserMatch = new ResultMatching();
                                    ret = morphoDatabase.identifyMatch(FalseAcceptanceRate.MORPHO_FAR_5, secondFingerTempList, secondUser, secondUserMatch);
                                    if (ret == ErrorCodes.MORPHO_OK) {//Match Found
                                        //Check if matched  emp id and emp id to be updated is same if found same proceed with update
                                        if (secondUser.getField(0).equals(empFingerInfo.getEmpId()) && secondUser.getField(1).equals(empFingerInfo.getCardId()) && secondUser.getField(2).equals(empFingerInfo.getEmpName())) {
                                            boolean l_activateFullImageRetrieve = templateList.isActivateFullImageRetrieving();
                                            try {
                                                for (int fingerIndex = 0; fingerIndex < noOfFingers; ++fingerIndex) {
                                                    if (fingerIndex == 0) {
                                                        if (ret == ErrorCodes.MORPHO_OK && ((templateType != TemplateType.MORPHO_NO_PK_FP))) {
                                                            byte[] firstFingerFMD = new byte[252];
                                                            for (int i = 0; i < 252; i++)
                                                                firstFingerFMD[i] = 0;
                                                            Template t = templateList.getTemplate(fingerIndex);
                                                            firstFingerFMD = t.getData();
                                                            final StringBuilder builder = new StringBuilder();
                                                            for (byte b : firstFingerFMD) {
                                                                builder.append(String.format("%02x", b));
                                                            }
                                                            String strFirstFingerDataHex = builder.toString().toUpperCase();
                                                            if (strFirstFingerDataHex != null && strFirstFingerDataHex.length() < 504) {
                                                                int len;
                                                                while ((len = strFirstFingerDataHex.length()) < 504) {
                                                                    strFirstFingerDataHex = strFirstFingerDataHex + "0";
                                                                }
                                                            }
                                                            empFingerInfo.setFirstFingerFMD(firstFingerFMD);
                                                            empFingerInfo.setStrFirstFingerDataHex(strFirstFingerDataHex);
                                                        }
                                                        if (ret == ErrorCodes.MORPHO_OK && l_activateFullImageRetrieve) {
                                                            if (compressAlgo.equals(CompressionAlgorithm.MORPHO_COMPRESS_WSQ) || compressAlgo.equals(CompressionAlgorithm.MORPHO_COMPRESS_V1)) {
                                                                //Case of WSQ or morpho_v1 image
                                                                //data = templateList.getImage(fingerIndex).getCompressedImage();
                                                            } else {
                                                                //Case of RAW Image
                                                                byte[] firstFingerFID = templateList.getImage(fingerIndex).getImage();
                                                                empFingerInfo.setFirstFingerFID(firstFingerFID);
                                                            }
                                                        }
                                                    } else if (fingerIndex == 1) {
                                                        if (ret == ErrorCodes.MORPHO_OK && ((templateType != TemplateType.MORPHO_NO_PK_FP))) {
                                                            Template t = templateList.getTemplate(fingerIndex);
                                                            byte[] secondFingerFMD = new byte[252];
                                                            for (int i = 0; i < 252; i++)
                                                                secondFingerFMD[i] = 0;
                                                            secondFingerFMD = t.getData();
                                                            final StringBuilder builder = new StringBuilder();
                                                            for (byte b : secondFingerFMD) {
                                                                builder.append(String.format("%02x", b));
                                                            }
                                                            String strSecondFingerDataHex = builder.toString().toUpperCase();
                                                            if (strSecondFingerDataHex != null && strSecondFingerDataHex.length() < 504) {
                                                                int len;
                                                                while ((len = strSecondFingerDataHex.length()) < 504) {
                                                                    strSecondFingerDataHex = strSecondFingerDataHex + "0";
                                                                }
                                                            }
                                                            empFingerInfo.setSecondFingerFMD(secondFingerFMD);
                                                            empFingerInfo.setStrSecondFingerDataHex(strSecondFingerDataHex);
                                                        }

                                                        if (ret == ErrorCodes.MORPHO_OK && l_activateFullImageRetrieve) {
                                                            if (compressAlgo.equals(CompressionAlgorithm.MORPHO_COMPRESS_WSQ) || compressAlgo.equals(CompressionAlgorithm.MORPHO_COMPRESS_V1)) {
                                                                //Case of WSQ or morpho_v1 image
                                                                //data = templateList.getImage(fingerIndex).getCompressedImage();
                                                            } else {
                                                                //Case of RAW Image
                                                                byte[] secondFingerFID = templateList.getImage(fingerIndex).getImage();
                                                                empFingerInfo.setSecondFingerFID(secondFingerFID);
                                                                message = "Finger Updated Successfully!!!!!";
                                                            }
                                                        }
                                                    }
                                                }
                                            } catch (Exception e) {
                                                Log.i("Update Error:", e.getMessage());
                                            }
                                        } else {
                                            empFingerInfo.setTemplateExists(true);
                                            message += "Second Finger Template Already Exists Against : \n\n";
                                            message += "Employee Details : \n";
                                            message += "Employee Id : " + secondUser.getField(0) + "\n";
                                            message += "Card Id : " + secondUser.getField(1) + "\n";
                                            message += "Name : " + secondUser.getField(2);
                                        }
                                    } else if (ret == -8 || ret == -11) {//-8 Finger Identification Failed , -11 Morpho Database Empty
                                        boolean l_activateFullImageRetrieve = templateList.isActivateFullImageRetrieving();
                                        try {
                                            ret = ErrorCodes.MORPHO_OK;
                                            for (int fingerIndex = 0; fingerIndex < noOfFingers; ++fingerIndex) {
                                                if (fingerIndex == 0) {
                                                    if (ret == ErrorCodes.MORPHO_OK && ((templateType != TemplateType.MORPHO_NO_PK_FP))) {
                                                        byte[] firstFingerFMD = new byte[252];
                                                        for (int i = 0; i < 252; i++)
                                                            firstFingerFMD[i] = 0;
                                                        Template t = templateList.getTemplate(fingerIndex);
                                                        firstFingerFMD = t.getData();
                                                        final StringBuilder builder = new StringBuilder();
                                                        for (byte b : firstFingerFMD) {
                                                            builder.append(String.format("%02x", b));
                                                        }
                                                        String strFirstFingerDataHex = builder.toString().toUpperCase();
                                                        if (strFirstFingerDataHex != null && strFirstFingerDataHex.length() < 504) {
                                                            int len;
                                                            while ((len = strFirstFingerDataHex.length()) < 504) {
                                                                strFirstFingerDataHex = strFirstFingerDataHex + "0";
                                                            }
                                                        }
                                                        empFingerInfo.setFirstFingerFMD(firstFingerFMD);
                                                        empFingerInfo.setStrFirstFingerDataHex(strFirstFingerDataHex);
                                                    }
                                                    if (ret == ErrorCodes.MORPHO_OK && l_activateFullImageRetrieve) {
                                                        if (compressAlgo.equals(CompressionAlgorithm.MORPHO_COMPRESS_WSQ) || compressAlgo.equals(CompressionAlgorithm.MORPHO_COMPRESS_V1)) {
                                                            //Case of WSQ or morpho_v1 image
                                                            //data = templateList.getImage(fingerIndex).getCompressedImage();
                                                        } else {
                                                            //Case of RAW Image
                                                            byte[] firstFingerFID = templateList.getImage(fingerIndex).getImage();
                                                            empFingerInfo.setFirstFingerFID(firstFingerFID);
                                                        }
                                                    }
                                                } else if (fingerIndex == 1) {
                                                    if (ret == ErrorCodes.MORPHO_OK && ((templateType != TemplateType.MORPHO_NO_PK_FP))) {
                                                        Template t = templateList.getTemplate(fingerIndex);
                                                        byte[] secondFingerFMD = new byte[252];
                                                        for (int i = 0; i < 252; i++)
                                                            secondFingerFMD[i] = 0;
                                                        secondFingerFMD = t.getData();
                                                        final StringBuilder builder = new StringBuilder();
                                                        for (byte b : secondFingerFMD) {
                                                            builder.append(String.format("%02x", b));
                                                        }
                                                        String strSecondFingerDataHex = builder.toString().toUpperCase();
                                                        if (strSecondFingerDataHex != null && strSecondFingerDataHex.length() < 504) {
                                                            int len;
                                                            while ((len = strSecondFingerDataHex.length()) < 504) {
                                                                strSecondFingerDataHex = strSecondFingerDataHex + "0";
                                                            }
                                                        }
                                                        empFingerInfo.setSecondFingerFMD(secondFingerFMD);
                                                        empFingerInfo.setStrSecondFingerDataHex(strSecondFingerDataHex);
                                                    }

                                                    if (ret == ErrorCodes.MORPHO_OK && l_activateFullImageRetrieve) {
                                                        if (compressAlgo.equals(CompressionAlgorithm.MORPHO_COMPRESS_WSQ) || compressAlgo.equals(CompressionAlgorithm.MORPHO_COMPRESS_V1)) {
                                                            //Case of WSQ or morpho_v1 image
                                                            //data = templateList.getImage(fingerIndex).getCompressedImage();
                                                        } else {
                                                            //Case of RAW Image
                                                            byte[] secondFingerFID = templateList.getImage(fingerIndex).getImage();
                                                            empFingerInfo.setSecondFingerFID(secondFingerFID);
                                                            message = "Finger Updated Successfully!!!!!";
                                                        }
                                                    }
                                                }
                                            }
                                        } catch (Exception e) {
                                            Log.i("Update Error:", e.getMessage());
                                        }
                                    }
                                } else {
                                    empFingerInfo.setTemplateExists(true);
                                    message += "Finger Template Already Exists Against : \n\n";
                                    message += "Employee Details : \n";
                                    message += "Employee Id:" + firstUser.getField(0) + "\n";
                                    message += "Card Id : " + firstUser.getField(1) + "\n";
                                    message += "Name : " + firstUser.getField(2);
                                }
                            } else if (ret == -8 || ret == -11) {//-8 Finger Identification Failed , -11 Morpho Database Empty
                                TemplateList secondFingerTempList = new TemplateList();
                                Template secondFingerTemplate = templateList.getTemplate(1);
                                byte[] secondFingerTemplateData = secondFingerTemplate.getData();
                                secondFingerTemplate.setTemplateType(TemplateType.MORPHO_PK_ISO_FMR);
                                secondFingerTemplate.setData(secondFingerTemplateData);
                                secondFingerTemplate.setDataIndex(0);
                                secondFingerTempList.putTemplate(secondFingerTemplate);
                                MorphoUser secondUser = new MorphoUser();
                                ResultMatching secondUserMatch = new ResultMatching();
                                ret = morphoDatabase.identifyMatch(FalseAcceptanceRate.MORPHO_FAR_5, secondFingerTempList, secondUser, secondUserMatch);
                                if (ret == ErrorCodes.MORPHO_OK) {//Matched Found
                                    //Check if matched  emp id and emp id to be updated is same if found same proceed with update
                                    if (secondUser.getField(0).equals(empFingerInfo.getEmpId()) && secondUser.getField(1).equals(empFingerInfo.getCardId()) && secondUser.getField(2).equals(empFingerInfo.getEmpName())) {
                                        boolean l_activateFullImageRetrieve = templateList.isActivateFullImageRetrieving();
                                        try {
                                            for (int fingerIndex = 0; fingerIndex < noOfFingers; ++fingerIndex) {
                                                if (fingerIndex == 0) {
                                                    if (ret == ErrorCodes.MORPHO_OK && ((templateType != TemplateType.MORPHO_NO_PK_FP))) {
                                                        byte[] firstFingerFMD = new byte[252];
                                                        for (int i = 0; i < 252; i++)
                                                            firstFingerFMD[i] = 0;
                                                        Template t = templateList.getTemplate(fingerIndex);
                                                        firstFingerFMD = t.getData();
                                                        final StringBuilder builder = new StringBuilder();
                                                        for (byte b : firstFingerFMD) {
                                                            builder.append(String.format("%02x", b));
                                                        }
                                                        String strFirstFingerDataHex = builder.toString().toUpperCase();
                                                        if (strFirstFingerDataHex != null && strFirstFingerDataHex.length() < 504) {
                                                            int len;
                                                            while ((len = strFirstFingerDataHex.length()) < 504) {
                                                                strFirstFingerDataHex = strFirstFingerDataHex + "0";
                                                            }
                                                        }
                                                        empFingerInfo.setFirstFingerFMD(firstFingerFMD);
                                                        empFingerInfo.setStrFirstFingerDataHex(strFirstFingerDataHex);
                                                    }
                                                    if (ret == ErrorCodes.MORPHO_OK && l_activateFullImageRetrieve) {
                                                        if (compressAlgo.equals(CompressionAlgorithm.MORPHO_COMPRESS_WSQ) || compressAlgo.equals(CompressionAlgorithm.MORPHO_COMPRESS_V1)) {
                                                            //Case of WSQ or morpho_v1 image
                                                            //data = templateList.getImage(fingerIndex).getCompressedImage();
                                                        } else {
                                                            //Case of RAW Image
                                                            byte[] firstFingerFID = templateList.getImage(fingerIndex).getImage();
                                                            empFingerInfo.setFirstFingerFID(firstFingerFID);
                                                        }
                                                    }
                                                } else if (fingerIndex == 1) {
                                                    if (ret == ErrorCodes.MORPHO_OK && ((templateType != TemplateType.MORPHO_NO_PK_FP))) {
                                                        Template t = templateList.getTemplate(fingerIndex);
                                                        byte[] secondFingerFMD = new byte[252];
                                                        for (int i = 0; i < 252; i++)
                                                            secondFingerFMD[i] = 0;
                                                        secondFingerFMD = t.getData();
                                                        final StringBuilder builder = new StringBuilder();
                                                        for (byte b : secondFingerFMD) {
                                                            builder.append(String.format("%02x", b));
                                                        }
                                                        String strSecondFingerDataHex = builder.toString().toUpperCase();
                                                        if (strSecondFingerDataHex != null && strSecondFingerDataHex.length() < 504) {
                                                            int len;
                                                            while ((len = strSecondFingerDataHex.length()) < 504) {
                                                                strSecondFingerDataHex = strSecondFingerDataHex + "0";
                                                            }
                                                        }
                                                        empFingerInfo.setSecondFingerFMD(secondFingerFMD);
                                                        empFingerInfo.setStrSecondFingerDataHex(strSecondFingerDataHex);
                                                    }

                                                    if (ret == ErrorCodes.MORPHO_OK && l_activateFullImageRetrieve) {
                                                        if (compressAlgo.equals(CompressionAlgorithm.MORPHO_COMPRESS_WSQ) || compressAlgo.equals(CompressionAlgorithm.MORPHO_COMPRESS_V1)) {
                                                            //Case of WSQ or morpho_v1 image
                                                            //data = templateList.getImage(fingerIndex).getCompressedImage();
                                                        } else {
                                                            //Case of RAW Image
                                                            byte[] secondFingerFID = templateList.getImage(fingerIndex).getImage();
                                                            empFingerInfo.setSecondFingerFID(secondFingerFID);
                                                            message = "Finger Updated Successfully!!!!!";
                                                        }
                                                    }
                                                }
                                            }
                                        } catch (Exception e) {
                                            Log.i("UPDATE", e.getMessage());
                                        }
                                    } else {
                                        empFingerInfo.setTemplateExists(true);
                                        message += "Second Finger Template Already Exists Against : \n\n";
                                        message += "Employee Details : \n";
                                        message += "Employee Id : " + secondUser.getField(0) + "\n";
                                        message += "Card Id : " + secondUser.getField(1) + "\n";
                                        message += "Name : " + secondUser.getField(2);
                                    }
                                } else if (ret == -8 || ret == -11) {//-8 Finger Did Not Match , -11 Morpho Database Empty
                                    boolean l_activateFullImageRetrieve = templateList.isActivateFullImageRetrieving();
                                    try {
                                        ret = ErrorCodes.MORPHO_OK;
                                        for (int fingerIndex = 0; fingerIndex < noOfFingers; ++fingerIndex) {
                                            if (fingerIndex == 0) {
                                                if (ret == ErrorCodes.MORPHO_OK && ((templateType != TemplateType.MORPHO_NO_PK_FP))) {
                                                    byte[] firstFingerFMD = new byte[252];
                                                    for (int i = 0; i < 252; i++)
                                                        firstFingerFMD[i] = 0;
                                                    Template t = templateList.getTemplate(fingerIndex);
                                                    firstFingerFMD = t.getData();
                                                    final StringBuilder builder = new StringBuilder();
                                                    for (byte b : firstFingerFMD) {
                                                        builder.append(String.format("%02x", b));
                                                    }
                                                    String strFirstFingerDataHex = builder.toString().toUpperCase();
                                                    if (strFirstFingerDataHex != null && strFirstFingerDataHex.length() < 504) {
                                                        int len;
                                                        while ((len = strFirstFingerDataHex.length()) < 504) {
                                                            strFirstFingerDataHex = strFirstFingerDataHex + "0";
                                                        }
                                                    }
                                                    empFingerInfo.setFirstFingerFMD(firstFingerFMD);
                                                    empFingerInfo.setStrFirstFingerDataHex(strFirstFingerDataHex);
                                                }
                                                if (ret == ErrorCodes.MORPHO_OK && l_activateFullImageRetrieve) {
                                                    if (compressAlgo.equals(CompressionAlgorithm.MORPHO_COMPRESS_WSQ) || compressAlgo.equals(CompressionAlgorithm.MORPHO_COMPRESS_V1)) {
                                                        //Case of WSQ or morpho_v1 image
                                                        //data = templateList.getImage(fingerIndex).getCompressedImage();
                                                    } else {
                                                        //Case of RAW Image
                                                        byte[] firstFingerFID = templateList.getImage(fingerIndex).getImage();
                                                        empFingerInfo.setFirstFingerFID(firstFingerFID);
                                                    }
                                                }
                                            } else if (fingerIndex == 1) {
                                                if (ret == ErrorCodes.MORPHO_OK && ((templateType != TemplateType.MORPHO_NO_PK_FP))) {
                                                    Template t = templateList.getTemplate(fingerIndex);
                                                    byte[] secondFingerFMD = new byte[252];
                                                    for (int i = 0; i < 252; i++)
                                                        secondFingerFMD[i] = 0;
                                                    secondFingerFMD = t.getData();
                                                    final StringBuilder builder = new StringBuilder();
                                                    for (byte b : secondFingerFMD) {
                                                        builder.append(String.format("%02x", b));
                                                    }
                                                    String strSecondFingerDataHex = builder.toString().toUpperCase();
                                                    if (strSecondFingerDataHex != null && strSecondFingerDataHex.length() < 504) {
                                                        int len;
                                                        while ((len = strSecondFingerDataHex.length()) < 504) {
                                                            strSecondFingerDataHex = strSecondFingerDataHex + "0";
                                                        }
                                                    }
                                                    empFingerInfo.setSecondFingerFMD(secondFingerFMD);
                                                    empFingerInfo.setStrSecondFingerDataHex(strSecondFingerDataHex);
                                                }
                                                if (ret == ErrorCodes.MORPHO_OK && l_activateFullImageRetrieve) {
                                                    if (compressAlgo.equals(CompressionAlgorithm.MORPHO_COMPRESS_WSQ) || compressAlgo.equals(CompressionAlgorithm.MORPHO_COMPRESS_V1)) {
                                                        //Case of WSQ or morpho_v1 image
                                                        //data = templateList.getImage(fingerIndex).getCompressedImage();
                                                    } else {
                                                        //Case of RAW Image
                                                        byte[] secondFingerFID = templateList.getImage(fingerIndex).getImage();
                                                        empFingerInfo.setSecondFingerFID(secondFingerFID);
                                                        message = "Finger Updated Successfully!!!!!";
                                                    }
                                                }
                                            }
                                        }
                                    } catch (Exception e) {
                                        Log.i("Update Error:", e.getMessage());
                                    }
                                }
                            }
                        }
                    }

                    final String alertMessage = message;
                    final int internalError = morphoDevice.getInternalError();
                    final int retvalue = ret;
                    final boolean duplicateTempFound = empFingerInfo.isTemplateExists();

                    mHandler.post(new Runnable() {
                        @Override
                        public synchronized void run() {
                            if (context != null) {
                                Activity activity = (Activity) context;
                                if (!activity.isFinishing()) {
                                    if (retvalue == 0 && duplicateTempFound) {
                                        alert(-111, internalError, "Update Error", alertMessage);//User Defined Error For Template Exits=-111
                                    } else if (retvalue == 0 && !duplicateTempFound) {
                                        updateDialog("Update Status", alertMessage, templateList);
                                    } else {
                                        alert(retvalue, internalError, "Update Error", alertMessage);
                                    }
                                }
                            }
                        }
                    });

                    notifyEnrollUpdateEndProcess();
                }
            }
        }));

        commandThread.start();
    }

    //=============================== Morpho Finger Verify ======================================//

    private void verify(final Observer observer, final int mode, final Object obj) {

        Thread commandThread = (new Thread(new Runnable() {
            @Override
            public void run() {
                if (context != null) {
                    final Activity activity = (Activity) context;
                    if (activity != null && activity instanceof EmployeeAttendanceActivity) {
                        TemplateList templateList = null;
                        int timeOut = 15;
                        int far = FalseAcceptanceRate.MORPHO_FAR_5;
                        Coder coderChoice = Coder.MORPHO_DEFAULT_CODER;
                        int detectModeChoice = DetectionMode.MORPHO_VERIF_DETECT_MODE.getValue();
                        int matchingStrategy = 0;
                        int callbackCmd = ProcessInfo.getInstance().getCallbackCmd();
                        callbackCmd &= ~CallbackMask.MORPHO_CALLBACK_ENROLLMENT_CMD.getValue();
                        ResultMatching resultMatching = new ResultMatching();
                        int ret = -1;
                        switch (mode) {
                            case Constants.VERIFY_BY_CARD_MODE:
                                final SmartCardInfo cardDetails = (SmartCardInfo) obj;
                                if (cardDetails != null) {
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Activity activity = (Activity) context;
                                            TextView textViewPutFingerMessage = activity.findViewById(getResIdFromContext("putFingerMessage"));
                                            String firstFingerIndex = cardDetails.getFirstFingerIndex();
                                            String secondFingerIndex = cardDetails.getSecondFingerIndex();
                                            if (firstFingerIndex != null && secondFingerIndex != null) {
                                                textViewPutFingerMessage.setText("Put " + firstFingerIndex + " or " + secondFingerIndex + " To Be Verified");
                                            } else if (firstFingerIndex != null && secondFingerIndex == null) {
                                                textViewPutFingerMessage.setText("Put " + firstFingerIndex + " To Be Verified");
                                            }
                                            ForlinxGPIO.runGPIOLEDForCardRead();
                                        }
                                    });
                                    templateList = new TemplateList();
                                    String firstTemplate = cardDetails.getFirstFingerTemplate();
                                    if (firstTemplate != null) {
                                        int len1 = cardDetails.getFirstFingerTemplate().length();
                                        if (len1 > 0) {
                                            String firstFingerTemplate = cardDetails.getFirstFingerTemplate();
                                            byte[] data = new byte[len1 / 2];
                                            for (int i = 0; i < len1; i += 2) {
                                                data[i / 2] = (byte) ((Character.digit(firstFingerTemplate.charAt(i), 16) << 4) + Character.digit(firstFingerTemplate.charAt(i + 1), 16));
                                            }
                                            Template template1 = new Template();
                                            template1.setTemplateType(TemplateType.MORPHO_PK_ISO_FMR);
                                            template1.setData(data);
                                            template1.setDataIndex(0);
                                            templateList.putTemplate(template1);
                                        }
                                    }
                                    String secondTemplate = cardDetails.getSecondFingerTemplate();
                                    if (secondTemplate != null) {
                                        int len1 = cardDetails.getSecondFingerTemplate().length();
                                        if (len1 > 0) {
                                            String secondFingerTemplate = cardDetails.getSecondFingerTemplate();
                                            byte[] data1 = new byte[len1 / 2];
                                            for (int i = 0; i < len1; i += 2) {
                                                data1[i / 2] = (byte) ((Character.digit(secondFingerTemplate.charAt(i), 16) << 4) + Character.digit(secondFingerTemplate.charAt(i + 1), 16));
                                            }
                                            Template template2 = new Template();
                                            template2.setTemplateType(TemplateType.MORPHO_PK_ISO_FMR);
                                            template2.setData(data1);
                                            template2.setDataIndex(0);
                                            templateList.putTemplate(template2);
                                        }
                                    }
                                    ret = morphoDevice.setStrategyAcquisitionMode(ProcessInfo.getInstance().getStrategyAcquisitionMode());
                                    if (ret == 0) {
                                        ret = morphoDevice.verify(timeOut, far, coderChoice, detectModeChoice, matchingStrategy, templateList, callbackCmd, observer, resultMatching);
                                        if (ret != -26) {//Verification command aborted
                                            ForlinxGPIO.runGPIOForPressed();
                                        }
                                    }

                                    ProcessInfo.getInstance().setVerificationStarted(false);
                                    ProcessInfo.getInstance().setCommandBioStart(false);

                                    final int retValueCardVerify = ret;

                                    switch (EmployeeAttendanceActivity.gvm) {
                                        case "CARD-BASED-VERIFY":
                                            switch (retValueCardVerify) {
                                                case 0:
                                                    ProcessInfo.getInstance().setReStartIdentification(true);//Verification Match
                                                    break;
                                                case -8:
                                                    ProcessInfo.getInstance().setReStartIdentification(true);//Verification Mismatch
                                                    break;
                                                case -19:
                                                    ProcessInfo.getInstance().setReStartIdentification(true);//Verification Time Out
                                                    break;
                                                case -26:
                                                    ProcessInfo.getInstance().setReStartIdentification(false);//Verification Command Aborted
                                                    break;
                                                default:
                                                    ProcessInfo.getInstance().setReStartIdentification(false);//Other Error
                                                    break;
                                            }
                                            break;
                                        case "1:N":
                                            switch (retValueCardVerify) {
                                                case 0:
                                                    ProcessInfo.getInstance().setReStartIdentification(true);//Verification Match
                                                    break;
                                                case -8:
                                                    ProcessInfo.getInstance().setReStartIdentification(true);//Verification Mismatch
                                                    break;
                                                case -19:
                                                    ProcessInfo.getInstance().setReStartIdentification(true);//Verification Time Out
                                                    break;
                                                case -26:
                                                    ProcessInfo.getInstance().setReStartIdentification(false);//Verification Command Aborted
                                                    break;
                                                default:
                                                    ProcessInfo.getInstance().setReStartIdentification(false);//Other Error
                                                    break;
                                            }
                                            break;
                                    }


                                    final int internalErrorCardVerify = morphoDevice.getInternalError();

                                    mHandler.post(new Runnable() {
                                        @Override
                                        public synchronized void run() {
                                            if (context != null) {
                                                Activity activity = (Activity) context;
                                                if (activity != null && activity instanceof EmployeeAttendanceActivity) {
                                                    if (!activity.isFinishing()) {
                                                        if (retValueCardVerify == 0) {
                                                            if (successEmpDetailsDialog != null && successEmpDetailsDialog.isShowing()) {
                                                                successEmpDetailsDialog.cancel();
                                                            }
                                                            if (EmployeeAttendanceActivity.isAttendanceWindowVisisble) {
                                                                switch (EmployeeAttendanceActivity.gvm) {
                                                                    case "CARD-BASED-VERIFY":
                                                                        if (cardDetails.getFirstFingerVerificationMode().equals("CARD+PIN+FINGER")) {
                                                                            EmployeeAttendanceActivity.isDUI = true;
                                                                            showCardPinDialog(cardDetails);
                                                                        } else {
                                                                            new Thread(new Runnable() {
                                                                                @Override
                                                                                public void run() {
                                                                                    ForlinxGPIO.runGPIOLEDForSuccess();
                                                                                }
                                                                            }).start();
                                                                            notifyVerificationEndProcess();
                                                                            showSuccessCustomDialogForCardVerification("Verification Success", cardDetails);
                                                                            pHandler.postDelayed(new Runnable() {
                                                                                @Override
                                                                                public void run() {
                                                                                    successEmpDetailsDialog.cancel();
                                                                                }
                                                                            }, 3000);//4000
                                                                        }
                                                                        break;
                                                                    case "1:N":
                                                                        new Thread(new Runnable() {
                                                                            @Override
                                                                            public void run() {
                                                                                ForlinxGPIO.runGPIOLEDForSuccess();
                                                                            }
                                                                        }).start();
                                                                        notifyVerificationEndProcess();
                                                                        showSuccessCustomDialogForCardVerification("Verification Success", cardDetails);
                                                                        pHandler.postDelayed(new Runnable() {
                                                                            @Override
                                                                            public void run() {
                                                                                successEmpDetailsDialog.cancel();
                                                                            }
                                                                        }, 3000);//4000
                                                                        break;
                                                                    case "CARD+FINGER":
                                                                        new Thread(new Runnable() {
                                                                            @Override
                                                                            public void run() {
                                                                                ForlinxGPIO.runGPIOLEDForSuccess();
                                                                            }
                                                                        }).start();
                                                                        notifyVerificationEndProcess();
                                                                        showSuccessCustomDialogForCardVerification("Verification Success", cardDetails);
                                                                        pHandler.postDelayed(new Runnable() {
                                                                            @Override
                                                                            public void run() {
                                                                                successEmpDetailsDialog.cancel();
                                                                            }
                                                                        }, 3000);//4000
                                                                        break;
                                                                }
                                                            }
                                                        } else if (retValueCardVerify == -8) {
                                                            if (failureEmpDetailsDialog != null && failureEmpDetailsDialog.isShowing()) {
                                                                failureEmpDetailsDialog.cancel();
                                                            }
                                                            if (EmployeeAttendanceActivity.isAttendanceWindowVisisble) {
                                                                new Thread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        ForlinxGPIO.runGPIOLEDForFailure();
                                                                    }
                                                                }).start();
                                                                notifyVerificationEndProcess();
                                                                showFailureCustomDialog("Verification Failed", "Finger Print Did Not Match");
                                                                pHandler.postDelayed(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        failureEmpDetailsDialog.cancel();
                                                                    }
                                                                }, 2000);
                                                            }
                                                            notifyVerificationEndProcess();
                                                        } else if (retValueCardVerify == -19) {
                                                            if (failureEmpDetailsDialog != null && failureEmpDetailsDialog.isShowing()) {
                                                                failureEmpDetailsDialog.cancel();
                                                            }
                                                            if (EmployeeAttendanceActivity.isAttendanceWindowVisisble) {
                                                                new Thread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        ForlinxGPIO.runGPIOLEDForFailure();
                                                                    }
                                                                }).start();
                                                                notifyVerificationEndProcess();
                                                                showFailureCustomDialog("Verification Failed", "Finger Verification Time Out");
                                                                pHandler.postDelayed(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        failureEmpDetailsDialog.cancel();
                                                                    }

                                                                }, 2000);
                                                            }
                                                            notifyVerificationEndProcess();
                                                        } else if (retValueCardVerify == -26) {
                                                            notifyVerificationEndProcess();
                                                            // Toast.makeText(EmployeeAttendanceActivity.this,"Verification command aborted",Toast.LENGTH_LONG).show();
                                                        } else {
                                                            if (failureEmpDetailsDialog != null && failureEmpDetailsDialog.isShowing()) {
                                                                failureEmpDetailsDialog.cancel();
                                                            }
                                                            if (EmployeeAttendanceActivity.isAttendanceWindowVisisble) {
                                                                new Thread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        ForlinxGPIO.runGPIOLEDForFailure();
                                                                    }
                                                                }).start();
                                                                notifyVerificationEndProcess();
                                                                showFailureCustomDialog("Verification Error", convertToInternationalMessage(retValueCardVerify, internalErrorCardVerify));
                                                                pHandler.postDelayed(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        failureEmpDetailsDialog.cancel();
                                                                    }

                                                                }, 2000);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    });
                                }
                                break;
                            case Constants.VERIFY_BY_LOCAL_DATABASE_MODE:
                                final EmployeeInfo empInfo = (EmployeeInfo) obj;
                                if (empInfo != null) {
                                    final int noOfTemplates = empInfo.getNoOfTemplates();
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            TextView textViewPutFingerMessage = activity.findViewById(getResIdFromContext("putFingerMessage"));
                                            if (noOfTemplates == 1) {
                                                textViewPutFingerMessage.setText("Put " + empInfo.getFirstFingerIndex() + " To Be Verified");
                                            } else if (noOfTemplates == 2) {
                                                textViewPutFingerMessage.setText("Put " + empInfo.getFirstFingerIndex() + " or " + empInfo.getSecondFingerIndex() + " To Be Verified");
                                            }
                                        }
                                    });
                                    int len2;
                                    templateList = new TemplateList();
                                    if (noOfTemplates == 1) {
                                        String firstTemplate = empInfo.getFirstFingerTemplate();
                                        if (firstTemplate != null) {
                                            len2 = firstTemplate.length();
                                            if (len2 > 0) {
                                                byte[] data = new byte[len2 / 2];
                                                for (int i = 0; i < len2; i += 2) {
                                                    data[i / 2] = (byte) ((Character.digit(firstTemplate.charAt(i), 16) << 4) + Character.digit(firstTemplate.charAt(i + 1), 16));
                                                }
                                                Template template1 = new Template();
                                                template1.setTemplateType(TemplateType.MORPHO_PK_ISO_FMR);
                                                template1.setData(data);
                                                template1.setDataIndex(0);
                                                templateList.putTemplate(template1);
                                            }
                                        }
                                    } else if (noOfTemplates == 2) {
                                        String firstTemplate = empInfo.getFirstFingerTemplate();
                                        if (firstTemplate != null) {
                                            len2 = firstTemplate.length();
                                            if (len2 > 0) {
                                                byte[] data = new byte[len2 / 2];
                                                for (int i = 0; i < len2; i += 2) {
                                                    data[i / 2] = (byte) ((Character.digit(firstTemplate.charAt(i), 16) << 4) + Character.digit(firstTemplate.charAt(i + 1), 16));
                                                }
                                                Template template1 = new Template();
                                                template1.setTemplateType(TemplateType.MORPHO_PK_ISO_FMR);
                                                template1.setData(data);
                                                template1.setDataIndex(0);
                                                templateList.putTemplate(template1);
                                            }
                                        }
                                        String secondTemplate = empInfo.getSecondFingerTemplate();
                                        if (secondTemplate != null) {
                                            len2 = secondTemplate.length();
                                            if (len2 > 0) {
                                                byte[] data1 = new byte[len2 / 2];
                                                for (int i = 0; i < len2; i += 2) {
                                                    data1[i / 2] = (byte) ((Character.digit(secondTemplate.charAt(i), 16) << 4) + Character.digit(secondTemplate.charAt(i + 1), 16));
                                                }
                                                Template template2 = new Template();
                                                template2.setTemplateType(TemplateType.MORPHO_PK_ISO_FMR);
                                                template2.setData(data1);
                                                template2.setDataIndex(0);
                                                templateList.putTemplate(template2);
                                            }
                                        }
                                    }
                                    ret = morphoDevice.setStrategyAcquisitionMode(ProcessInfo.getInstance().getStrategyAcquisitionMode());
                                    if (ret == 0) {
                                        ret = morphoDevice.verify(timeOut, far, coderChoice, detectModeChoice, matchingStrategy, templateList, callbackCmd, observer, resultMatching);
                                        ForlinxGPIO.runGPIOForPressed();
                                    }

                                    ProcessInfo.getInstance().setVerificationStarted(false);
                                    ProcessInfo.getInstance().setCommandBioStart(false);

                                    final int retValueLocalDbVerify = ret;

                                    switch (EmployeeAttendanceActivity.gvm) {
                                        case "CARD-BASED-VERIFY":
                                            switch (retValueLocalDbVerify) {
                                                case 0:
                                                    ProcessInfo.getInstance().setReStartIdentification(true);//Verification Match
                                                    break;
                                                case -8:
                                                    ProcessInfo.getInstance().setReStartIdentification(true);//Verification Mismatch
                                                    break;
                                                case -19:
                                                    ProcessInfo.getInstance().setReStartIdentification(true);//Verification Time Out
                                                    break;
                                                case -26:
                                                    ProcessInfo.getInstance().setReStartIdentification(false);//Verification Command Aborted
                                                    break;
                                                default:
                                                    ProcessInfo.getInstance().setReStartIdentification(false);//Other Error
                                                    break;
                                            }
                                            break;
                                        case "1:N":
                                            switch (retValueLocalDbVerify) {
                                                case 0:
                                                    ProcessInfo.getInstance().setReStartIdentification(true);//Verification Match
                                                    break;
                                                case -8:
                                                    ProcessInfo.getInstance().setReStartIdentification(true);//Verification Mismatch
                                                    break;
                                                case -19:
                                                    ProcessInfo.getInstance().setReStartIdentification(true);//Verification Time Out
                                                    break;
                                                case -26:
                                                    ProcessInfo.getInstance().setReStartIdentification(false);//Verification Command Aborted
                                                    break;
                                                default:
                                                    ProcessInfo.getInstance().setReStartIdentification(false);//Other Error
                                                    break;
                                            }
                                            break;
                                    }

                                    final int internalErrorLocalDbVerify = morphoDevice.getInternalError();

                                    mHandler.post(new Runnable() {
                                        @Override
                                        public synchronized void run() {
                                            if (context != null) {
                                                Activity activity = (Activity) context;
                                                if (activity != null && activity instanceof EmployeeAttendanceActivity) {
                                                    if (!activity.isFinishing()) {
                                                        if (retValueLocalDbVerify == 0) {
                                                            if (successEmpDetailsDialog != null && successEmpDetailsDialog.isShowing()) {
                                                                successEmpDetailsDialog.cancel();
                                                            }
                                                            if (EmployeeAttendanceActivity.isAttendanceWindowVisisble) {
                                                                new Thread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        ForlinxGPIO.runGPIOLEDForSuccess();
                                                                    }
                                                                }).start();
                                                                notifyVerificationEndProcess();
                                                                showSuccessCustomDialogForLocalDbVerification("Verification Status", empInfo);
                                                                pHandler.postDelayed(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        successEmpDetailsDialog.cancel();
                                                                    }
                                                                }, 3000);//4000
                                                            }

                                                        } else if (retValueLocalDbVerify == -8) {
                                                            if (failureEmpDetailsDialog != null && failureEmpDetailsDialog.isShowing()) {
                                                                failureEmpDetailsDialog.cancel();
                                                            }
                                                            if (EmployeeAttendanceActivity.isAttendanceWindowVisisble) {
                                                                new Thread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        ForlinxGPIO.runGPIOLEDForFailure();
                                                                    }
                                                                }).start();
                                                                notifyVerificationEndProcess();
                                                                showFailureCustomDialog("Verification Failed", "Finger Print Did Not Match");
                                                                pHandler.postDelayed(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        failureEmpDetailsDialog.cancel();
                                                                    }

                                                                }, 2000);
                                                            }
                                                        } else if (retValueLocalDbVerify == -19) {
                                                            if (failureEmpDetailsDialog != null && failureEmpDetailsDialog.isShowing()) {
                                                                failureEmpDetailsDialog.cancel();
                                                            }
                                                            if (EmployeeAttendanceActivity.isAttendanceWindowVisisble) {
                                                                new Thread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        ForlinxGPIO.runGPIOLEDForFailure();
                                                                    }
                                                                }).start();
                                                                notifyVerificationEndProcess();
                                                                showFailureCustomDialog("Verification Failed", "Finger Verification Time Out");
                                                                pHandler.postDelayed(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        failureEmpDetailsDialog.cancel();
                                                                    }

                                                                }, 2000);
                                                            }
                                                        } else if (retValueLocalDbVerify == -26) {
                                                            notifyVerificationEndProcess();
                                                            // Toast.makeText(EmployeeAttendanceActivity.this,"Verification Command Aborted",Toast.LENGTH_LONG).show();
                                                        } else {
                                                            if (failureEmpDetailsDialog != null && failureEmpDetailsDialog.isShowing()) {
                                                                failureEmpDetailsDialog.cancel();
                                                            }
                                                            if (EmployeeAttendanceActivity.isAttendanceWindowVisisble) {
                                                                new Thread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        ForlinxGPIO.runGPIOLEDForFailure();
                                                                    }
                                                                }).start();
                                                                notifyVerificationEndProcess();
                                                                showFailureCustomDialog("Verification Error", convertToInternationalMessage(retValueLocalDbVerify, internalErrorLocalDbVerify));
                                                                pHandler.postDelayed(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        failureEmpDetailsDialog.cancel();
                                                                    }

                                                                }, 2000);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    });
                                }
                                break;
                        }
                    }
                }
            }
        }));
        commandThread.start();
    }

    //================================  Morpho Finger Identify ===================================//

    private void identify(final Observer observer) {
        try {
            Thread commandThread = (new Thread(new Runnable() {
                @Override
                public void run() {
                    if (context != null) {
                        Activity activity = (Activity) context;
                        if (activity != null && activity instanceof EmployeeAttendanceActivity) {

                            //Log.d("TEST", "Identification called");

                            index = 0;
                            int timeout = ProcessInfo.getInstance().getIdentifyTimeout();

                            int far = ProcessInfo.getInstance().getMatchingThreshold();
                            final Coder coder = ProcessInfo.getInstance().getCoder();
                            int detectModeChoice;

                            MatchingStrategy matchingStrategy = ProcessInfo.getInstance().getMatchingStrategy();
                            int callbackCmd = ProcessInfo.getInstance().getCallbackCmd();
                            callbackCmd &= ~CallbackMask.MORPHO_CALLBACK_ENROLLMENT_CMD.getValue();
                            ResultMatching resultMatching = new ResultMatching();
                            final MorphoUser morphoUser = new MorphoUser();
                            if (ProcessInfo.getInstance().isForceFingerPlacementOnTop()) {
                                detectModeChoice = DetectionMode.MORPHO_ENROLL_DETECT_MODE.getValue();
                                detectModeChoice |= DetectionMode.MORPHO_FORCE_FINGER_ON_TOP_DETECT_MODE.getValue();
                            } else {
                                detectModeChoice = DetectionMode.MORPHO_VERIF_DETECT_MODE.getValue();
                                if (ProcessInfo.getInstance().isWakeUpWithLedOff()) {
                                    detectModeChoice |= MorphoWakeUpMode.MORPHO_WAKEUP_LED_OFF.getCode();
                                }
                            }

                            int ret = -111;

                            if (ProcessInfo.getInstance().isCommandBioStart()) {
                                ret = morphoDevice.setStrategyAcquisitionMode(ProcessInfo.getInstance().getStrategyAcquisitionMode());
                                if (ret == 0) {
                                    ret = morphoDatabase.identify(timeout, far, coder, detectModeChoice, matchingStrategy, callbackCmd, observer, resultMatching, 2, morphoUser);
                                    if (ret != -26) {//Identification Aborted
                                        ForlinxGPIO.runGPIOForPressed();
                                    }
                                }
                            }

                            ProcessInfo.getInstance().setIdentificationStarted(false);
                            ProcessInfo.getInstance().setCommandBioStart(false);

                            identifyRetvalue = ret;

                            Log.d("TEST","****************************************************");
                            Log.d("TEST","Identify Ret Value:"+identifyRetvalue);
                            Log.d("TEST","****************************************************");

                            switch (identifyRetvalue) {
                                case 0:
                                    ProcessInfo.getInstance().setReStartIdentification(true);//Identification Match
                                    break;
                                case -8:
                                    ProcessInfo.getInstance().setReStartIdentification(true);//Identification Mismatch
                                    break;
                                case -26:
                                    ProcessInfo.getInstance().setReStartIdentification(false);//Identification Aborted
                                    break;
                                default:
                                    ProcessInfo.getInstance().setReStartIdentification(false);//Other Error
                                    break;
                            }

                            final int internalError = morphoDevice.getInternalError();//null object

                            int appType = Settings.getInstance().getAppType();

                            switch (appType) {
                                case 0://Normal Attendance Mode
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public synchronized void run() {
                                            if (context != null) {
                                                Activity activity = (Activity) context;
                                                if (activity != null && activity instanceof EmployeeAttendanceActivity) {
                                                    if (!activity.isFinishing()) {
                                                        if (identifyRetvalue == 0) {
                                                            if (EmployeeAttendanceActivity.isAttendanceWindowVisisble) {
                                                                if (successEmpDetailsDialog != null && successEmpDetailsDialog.isShowing()) {
                                                                    successEmpDetailsDialog.cancel();
                                                                }

                                                                IdentificationInfo.getInstance().setIdentifyValue(identifyRetvalue);
                                                                IdentificationInfo.getInstance().setUserId(morphoUser.getField(0));
                                                                IdentificationInfo.getInstance().setFirstName(morphoUser.getField(1));
                                                                IdentificationInfo.getInstance().setLastName(morphoUser.getField(2));
                                                                IdentificationInfo.getInstance().setInternalError(internalError);

                                                                switch (EmployeeAttendanceActivity.gvm) {
                                                                    case "CARD-BASED-VERIFY":
                                                                        String empId = morphoUser.getField(0);
                                                                        if (empId != null && empId.trim().length() > 0) {
                                                                            int autoId = dbComm.getAutoIdByEmpId(empId);
                                                                            if (autoId != -1) {
                                                                                String vm = dbComm.getVMByAutoId(autoId);
                                                                                if (vm != null && vm.trim().length() > 0) {
                                                                                    String cardId = "";
                                                                                    switch (vm) {
                                                                                        case "1:N":
                                                                                            new Thread(new Runnable() {
                                                                                                @Override
                                                                                                public void run() {
                                                                                                    ForlinxGPIO.runGPIOLEDForSuccess();
                                                                                                }
                                                                                            }).start();
                                                                                            notifyIdentificationEndProcess();
                                                                                            showSuccessIdentificationCustomDialog("Identification Success");
                                                                                            pHandler.postDelayed(new Runnable() {
                                                                                                @Override
                                                                                                public void run() {
                                                                                                    successEmpDetailsDialog.cancel();
                                                                                                }
                                                                                            }, 2000);//4000//1000
                                                                                            break;
                                                                                        case "CARD-ONLY":
                                                                                            new Thread(new Runnable() {
                                                                                                @Override
                                                                                                public void run() {
                                                                                                    ForlinxGPIO.runGPIOLEDForSuccess();
                                                                                                }
                                                                                            }).start();
                                                                                            notifyIdentificationEndProcess();
                                                                                            showSuccessIdentificationCustomDialog("Identification Success");
                                                                                            pHandler.postDelayed(new Runnable() {
                                                                                                @Override
                                                                                                public void run() {
                                                                                                    successEmpDetailsDialog.cancel();
                                                                                                }
                                                                                            }, 2000);//4000//1000
                                                                                            break;
                                                                                        case "CARD+FINGER":
                                                                                            EmployeeAttendanceActivity.isDUI = true;//Disable on user Interaction
                                                                                            EmployeeAttendanceActivity.stopHandler();
                                                                                            cardId = morphoUser.getField(1);
                                                                                            showCardIdDialog(cardId, false);
                                                                                            break;
                                                                                        case "CARD+PIN+FINGER":
                                                                                            EmployeeAttendanceActivity.isDUI = true;//Disable on user Interaction
                                                                                            EmployeeAttendanceActivity.stopHandler();
                                                                                            cardId = morphoUser.getField(1);
                                                                                            showCardIdDialog(cardId, true);
                                                                                            break;
                                                                                        default:
                                                                                            notifyIdentificationEndProcess();
                                                                                            break;
                                                                                    }
                                                                                } else {
                                                                                    notifyIdentificationEndProcess();
                                                                                    Toast.makeText(context, "Verification Mode Not Found In SQLite", Toast.LENGTH_LONG).show();
                                                                                }
                                                                            } else {
                                                                                notifyIdentificationEndProcess();
                                                                                Toast.makeText(context, "Employee Data Not Found In SQLite", Toast.LENGTH_LONG).show();
                                                                            }
                                                                        }
                                                                        break;
                                                                    case "1:N":
                                                                        new Thread(new Runnable() {
                                                                            @Override
                                                                            public void run() {
                                                                                ForlinxGPIO.runGPIOLEDForSuccess();
                                                                            }
                                                                        }).start();
                                                                        notifyIdentificationEndProcess();
                                                                        showSuccessIdentificationCustomDialog("Identification Success");
                                                                        pHandler.postDelayed(new Runnable() {
                                                                            @Override
                                                                            public void run() {
                                                                                successEmpDetailsDialog.cancel();
                                                                            }
                                                                        }, 2000);//4000//1000
                                                                        break;
                                                                }
                                                            }
                                                        } else if (identifyRetvalue == -8) {
                                                            if (EmployeeAttendanceActivity.isAttendanceWindowVisisble) {
                                                                if (failureEmpDetailsDialog != null && failureEmpDetailsDialog.isShowing()) {
                                                                    failureEmpDetailsDialog.cancel();
                                                                }
                                                                new Thread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        ForlinxGPIO.runGPIOLEDForFailure();
                                                                    }
                                                                }).start();
                                                                notifyIdentificationEndProcess();
                                                                showFailureCustomDialog("Identification Failed", "Finger Print Did Not Match");
                                                                pHandler.postDelayed(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        failureEmpDetailsDialog.cancel();
                                                                    }
                                                                }, 2000);
                                                            }
                                                        } else if (identifyRetvalue == -26) {
                                                            //Toast.makeText(EmployeeAttendanceActivity.this,"IDENTIFICATION COMMAND ABORTED",Toast.LENGTH_LONG).show();
                                                            notifyIdentificationEndProcess();
                                                        } else {
                                                            if (EmployeeAttendanceActivity.isAttendanceWindowVisisble) {
                                                                if (failureEmpDetailsDialog != null && failureEmpDetailsDialog.isShowing()) {
                                                                    failureEmpDetailsDialog.cancel();
                                                                }
                                                                new Thread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        ForlinxGPIO.runGPIOLEDForFailure();
                                                                    }
                                                                }).start();
                                                                notifyIdentificationEndProcess();
                                                                showFailureCustomDialog("Identification Error", convertToInternationalMessage(identifyRetvalue, internalError));
                                                                pHandler.postDelayed(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        failureEmpDetailsDialog.cancel();
                                                                    }
                                                                }, 2000);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    });
                                    break;

                                case 1://College Attendance Mode
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public synchronized void run() {
                                            if (context != null) {
                                                Activity activity = (Activity) context;
                                                if (activity != null && activity instanceof EmployeeAttendanceActivity) {
                                                    if (!activity.isFinishing()) {
                                                        if (identifyRetvalue == 0) {
                                                            //restartIdentification=true;
                                                            // String userId = morphoUser.getField(0);

                                                            String eid = morphoUser.getField(0);
                                                            String cardId = morphoUser.getField(1);
                                                            cardId = Utility.paddCardId(cardId);
                                                            SQLiteCommunicator dbComm = new SQLiteCommunicator();
                                                            int id = dbComm.checkIsProfessor(cardId);
                                                            if (id != -1) {
                                                                if (!isLoggedIn) {
                                                                    new Thread(new Runnable() {
                                                                        @Override
                                                                        public void run() {
                                                                            ForlinxGPIO.runGPIOLEDForSuccess();
                                                                        }
                                                                    }).start();
                                                                    isLoggedIn = true;
                                                                    tempId = id;
                                                                    profEID = eid;
                                                                    profCID = cardId;
                                                                } else {
                                                                    if (id == tempId) {
                                                                        ProcessInfo.getInstance().setReStartIdentification(true);
                                                                        String[] sc_sn = subCode_subName.split(",");

                                                                        int status = dbComm.insertCollegeAttendanceData(profEID, profCID, "", "", sc_sn[0], subType, "OUT", EmployeeAttendanceActivity.latitude, EmployeeAttendanceActivity.longitude);
                                                                        if (status != -1) {
                                                                            int loginId = dbComm.getLastLoginStatusId();
                                                                            if (loginId != -1) {
                                                                                status = dbComm.updateLoginStatus(loginId);
                                                                            }
                                                                        }

                                                                        // EmployeeAttendanceActivity.empIdToVerfiy.setVisibility(View.VISIBLE);

                                                                        EmployeeAttendanceActivity.empIdToVerfiy.setEnabled(true);
                                                                        EmployeeAttendanceActivity.tButton.setEnabled(true);
                                                                        EmployeeAttendanceActivity.menuItem.setEnabled(true);
                                                                        EmployeeAttendanceActivity.menuItem.setVisible(true);

                                                                        new Thread(new Runnable() {
                                                                            @Override
                                                                            public void run() {
                                                                                ForlinxGPIO.runGPIOLEDForSuccess();
                                                                            }
                                                                        }).start();

                                                                        showFailureCustomDialog("Log out", "Professor Logged Out");
                                                                        pHandler.postDelayed(new Runnable() {
                                                                            @Override
                                                                            public void run() {
                                                                                profEID = "";
                                                                                profCID = "";
                                                                                isLoggedIn = false;
                                                                                tempId = -1;
                                                                                failureEmpDetailsDialog.cancel();
                                                                            }
                                                                        }, 2000);
                                                                        return;
                                                                    } else {
                                                                        ProcessInfo.getInstance().setReStartIdentification(true);
                                                                        new Thread(new Runnable() {
                                                                            @Override
                                                                            public void run() {
                                                                                ForlinxGPIO.runGPIOLEDForFailure();
                                                                            }
                                                                        }).start();
                                                                        showFailureCustomDialog("Error", "Professor Id Mismatch");
                                                                        pHandler.postDelayed(new Runnable() {
                                                                            @Override
                                                                            public void run() {
                                                                                failureEmpDetailsDialog.cancel();
                                                                            }
                                                                        }, 2000);
                                                                        return;
                                                                    }
                                                                }
                                                            }

                                                            if (isLoggedIn) {
                                                                if (successEmpDetailsDialog != null && successEmpDetailsDialog.isShowing()) {
                                                                    successEmpDetailsDialog.cancel();
                                                                }
                                                                IdentificationInfo.getInstance().setIdentifyValue(identifyRetvalue);
                                                                IdentificationInfo.getInstance().setUserId(morphoUser.getField(0));
                                                                IdentificationInfo.getInstance().setFirstName(morphoUser.getField(1));
                                                                IdentificationInfo.getInstance().setLastName(morphoUser.getField(2));
                                                                IdentificationInfo.getInstance().setInternalError(internalError);
                                                                if (id != -1) {
                                                                    ArrayList <String> subList = null;
                                                                    subList = dbComm.getSubjectIdList(cardId);
                                                                    if (subList != null) {
                                                                        int size = subList.size();
                                                                        if (size > 0) {
                                                                            ArrayList <SubInfo> subInfoList = null;
                                                                            ArrayList <String> uniqueList = new ArrayList <String>();
                                                                            subInfoList = dbComm.getSubjectName(subList, subInfoList);
                                                                            if (subInfoList != null) {
                                                                                size = subInfoList.size();
                                                                                if (size > 0) {
                                                                                    allList.clear();
                                                                                    finalList.clear();
                                                                                    for (int i = 0; i < size; i++) {
                                                                                        allList.add(subInfoList.get(i));
                                                                                        if (!uniqueList.contains(subInfoList.get(i).getSubCode())) {
                                                                                            uniqueList.add(subInfoList.get(i).getSubCode());
                                                                                            finalList.add(subInfoList.get(i).getSubCode() + " , " + subInfoList.get(i).getSubName());
                                                                                        }
                                                                                    }
                                                                                    if (finalList.size() > 0) {
                                                                                        stopFingerIdentification();
                                                                                        EmployeeAttendanceActivity.stopHandler();
                                                                                        showSubList("Subjects", finalList);
                                                                                    }
                                                                                } else {
                                                                                    ProcessInfo.getInstance().setReStartIdentification(true);
                                                                                    new Thread(new Runnable() {
                                                                                        @Override
                                                                                        public void run() {
                                                                                            ForlinxGPIO.runGPIOLEDForFailure();
                                                                                        }
                                                                                    }).start();
                                                                                    showFailureCustomDialog("Error", "No subject found");
                                                                                    pHandler.postDelayed(new Runnable() {
                                                                                        @Override
                                                                                        public void run() {
                                                                                            failureEmpDetailsDialog.cancel();
                                                                                        }
                                                                                    }, 2000);
                                                                                }
                                                                            } else {
                                                                                ProcessInfo.getInstance().setReStartIdentification(true);
                                                                                new Thread(new Runnable() {
                                                                                    @Override
                                                                                    public void run() {
                                                                                        ForlinxGPIO.runGPIOLEDForFailure();
                                                                                    }
                                                                                }).start();
                                                                                showFailureCustomDialog("Error", "No subject found");
                                                                                pHandler.postDelayed(new Runnable() {
                                                                                    @Override
                                                                                    public void run() {
                                                                                        failureEmpDetailsDialog.cancel();
                                                                                    }
                                                                                }, 2000);
                                                                            }
                                                                        } else {
                                                                            ProcessInfo.getInstance().setReStartIdentification(true);
                                                                            new Thread(new Runnable() {
                                                                                @Override
                                                                                public void run() {
                                                                                    ForlinxGPIO.runGPIOLEDForFailure();
                                                                                }
                                                                            }).start();
                                                                            showFailureCustomDialog("Error", "No subject found");
                                                                            pHandler.postDelayed(new Runnable() {
                                                                                @Override
                                                                                public void run() {
                                                                                    failureEmpDetailsDialog.cancel();
                                                                                }
                                                                            }, 2000);
                                                                        }
                                                                    } else {
                                                                        ProcessInfo.getInstance().setReStartIdentification(true);
                                                                        new Thread(new Runnable() {
                                                                            @Override
                                                                            public void run() {
                                                                                ForlinxGPIO.runGPIOLEDForFailure();
                                                                            }
                                                                        }).start();
                                                                        showFailureCustomDialog("Error", "No subject found");
                                                                        pHandler.postDelayed(new Runnable() {
                                                                            @Override
                                                                            public void run() {
                                                                                failureEmpDetailsDialog.cancel();
                                                                            }
                                                                        }, 2000);
                                                                    }
                                                                } else {
                                                                    if (EmployeeAttendanceActivity.isAttendanceWindowVisisble) {
                                                                        int appSubType = Settings.getInstance().getAppSubType();
                                                                        switch (appSubType) {
                                                                            case 0:
                                                                                ProcessInfo.getInstance().setReStartIdentification(true);
                                                                                new Thread(new Runnable() {
                                                                                    @Override
                                                                                    public void run() {
                                                                                        ForlinxGPIO.runGPIOLEDForSuccess();
                                                                                    }
                                                                                }).start();
                                                                                showSuccessIdentificationCustomDialog("Identification Success");
                                                                                pHandler.postDelayed(new Runnable() {
                                                                                    @Override
                                                                                    public void run() {
                                                                                        successEmpDetailsDialog.cancel();
                                                                                    }
                                                                                }, 2000);//4000//2000
                                                                                break;
                                                                            case 1:
                                                                                ProcessInfo.getInstance().setReStartIdentification(true);
                                                                                String[] data = subCode_subName.split(",");
                                                                                if (data != null && data.length == 2) {
                                                                                    //  String studentId = morphoUser.getField(0);
                                                                                    String studentId = morphoUser.getField(1);
                                                                                    studentId = Utility.paddCardId(studentId);
                                                                                    String subCode = data[0].trim();
                                                                                    boolean isValid = dbComm.isStudentValid(subCode, profCID, studentId);
                                                                                    if (isValid) {
                                                                                        new Thread(new Runnable() {
                                                                                            @Override
                                                                                            public void run() {
                                                                                                ForlinxGPIO.runGPIOLEDForSuccess();
                                                                                            }
                                                                                        }).start();
                                                                                        showSuccessIdentificationCustomDialog("Identification Success");
                                                                                        pHandler.postDelayed(new Runnable() {
                                                                                            @Override
                                                                                            public void run() {
                                                                                                successEmpDetailsDialog.cancel();
                                                                                            }
                                                                                        }, 2000);//4000/1000
                                                                                    } else {
                                                                                        new Thread(new Runnable() {
                                                                                            @Override
                                                                                            public void run() {
                                                                                                ForlinxGPIO.runGPIOLEDForFailure();
                                                                                            }
                                                                                        }).start();
                                                                                        showFailureCustomDialog("Error", "Professor Subject Mismatch");
                                                                                        pHandler.postDelayed(new Runnable() {
                                                                                            @Override
                                                                                            public void run() {
                                                                                                failureEmpDetailsDialog.cancel();
                                                                                            }
                                                                                        }, 2000);
                                                                                    }
                                                                                }
                                                                                break;
                                                                            case 3:
                                                                                ProcessInfo.getInstance().setReStartIdentification(true);
                                                                                new Thread(new Runnable() {
                                                                                    @Override
                                                                                    public void run() {
                                                                                        ForlinxGPIO.runGPIOLEDForSuccess();
                                                                                    }
                                                                                }).start();
                                                                                showSuccessIdentificationCustomDialog("Identification Success");
                                                                                pHandler.postDelayed(new Runnable() {
                                                                                    @Override
                                                                                    public void run() {
                                                                                        successEmpDetailsDialog.cancel();
                                                                                    }
                                                                                }, 2000);//4000//1000
                                                                                break;
                                                                            default:
                                                                                ProcessInfo.getInstance().setReStartIdentification(true);
                                                                                new Thread(new Runnable() {
                                                                                    @Override
                                                                                    public void run() {
                                                                                        ForlinxGPIO.runGPIOLEDForSuccess();
                                                                                    }
                                                                                }).start();
                                                                                showSuccessIdentificationCustomDialog("Identification Success");
                                                                                pHandler.postDelayed(new Runnable() {
                                                                                    @Override
                                                                                    public void run() {
                                                                                        successEmpDetailsDialog.cancel();
                                                                                    }
                                                                                }, 2000);//4000/1000
                                                                                break;
                                                                        }
                                                                    }
                                                                }
                                                            } else {
                                                                if (id == -1) {
                                                                    ProcessInfo.getInstance().setReStartIdentification(true);
                                                                    new Thread(new Runnable() {
                                                                        @Override
                                                                        public void run() {
                                                                            ForlinxGPIO.runGPIOLEDForFailure();
                                                                        }
                                                                    }).start();
                                                                    showFailureCustomDialog("Error", "Invalid Professor Id");
                                                                    pHandler.postDelayed(new Runnable() {
                                                                        @Override
                                                                        public void run() {
                                                                            failureEmpDetailsDialog.cancel();
                                                                        }
                                                                    }, 2000);
                                                                }
                                                            }
                                                        } else if (identifyRetvalue == -8) {
                                                            ProcessInfo.getInstance().setReStartIdentification(true);
                                                            if (failureEmpDetailsDialog != null && failureEmpDetailsDialog.isShowing()) {
                                                                failureEmpDetailsDialog.cancel();
                                                            }
                                                            if (EmployeeAttendanceActivity.isAttendanceWindowVisisble) {
                                                                new Thread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        ForlinxGPIO.runGPIOLEDForFailure();
                                                                    }
                                                                }).start();
                                                                showFailureCustomDialog("Identification Failed", "Finger Print Did Not Match");
                                                                pHandler.postDelayed(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        failureEmpDetailsDialog.cancel();
                                                                    }
                                                                }, 2000);
                                                            }
                                                        } else if (identifyRetvalue == -26) {
                                                            ProcessInfo.getInstance().setReStartIdentification(false);
                                                            //Toast.makeText(EmployeeAttendanceActivity.this,"IDENTIFICATION COMMAND ABORTED",Toast.LENGTH_LONG).show();
                                                        } else {
                                                            ProcessInfo.getInstance().setReStartIdentification(false);
                                                            if (failureEmpDetailsDialog != null && failureEmpDetailsDialog.isShowing()) {
                                                                failureEmpDetailsDialog.cancel();
                                                            }
                                                            if (EmployeeAttendanceActivity.isAttendanceWindowVisisble) {
                                                                new Thread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        ForlinxGPIO.runGPIOLEDForFailure();
                                                                    }
                                                                }).start();
                                                                showFailureCustomDialog("Identification Error", convertToInternationalMessage(identifyRetvalue, internalError));
                                                                pHandler.postDelayed(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        failureEmpDetailsDialog.cancel();
                                                                    }
                                                                }, 2000);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    });
                                    break;
                                default://Normal Attendance Mode
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public synchronized void run() {
                                            if (context != null) {
                                                Activity activity = (Activity) context;
                                                if (activity != null && activity instanceof EmployeeAttendanceActivity) {
                                                    if (!activity.isFinishing()) {
                                                        if (identifyRetvalue == 0) {
                                                            if (EmployeeAttendanceActivity.isAttendanceWindowVisisble) {
                                                                if (successEmpDetailsDialog != null && successEmpDetailsDialog.isShowing()) {
                                                                    successEmpDetailsDialog.cancel();
                                                                }

                                                                IdentificationInfo.getInstance().setIdentifyValue(identifyRetvalue);
                                                                IdentificationInfo.getInstance().setUserId(morphoUser.getField(0));
                                                                IdentificationInfo.getInstance().setFirstName(morphoUser.getField(1));
                                                                IdentificationInfo.getInstance().setLastName(morphoUser.getField(2));
                                                                IdentificationInfo.getInstance().setInternalError(internalError);

                                                                switch (EmployeeAttendanceActivity.gvm) {
                                                                    case "CARD-BASED-VERIFY":
                                                                        String empId = morphoUser.getField(0);
                                                                        if (empId != null && empId.trim().length() > 0) {
                                                                            int autoId = dbComm.getAutoIdByEmpId(empId);
                                                                            if (autoId != -1) {
                                                                                String vm = dbComm.getVMByAutoId(autoId);
                                                                                if (vm != null && vm.trim().length() > 0) {
                                                                                    String cardId = "";
                                                                                    switch (vm) {
                                                                                        case "1:N":
                                                                                            new Thread(new Runnable() {
                                                                                                @Override
                                                                                                public void run() {
                                                                                                    ForlinxGPIO.runGPIOLEDForSuccess();
                                                                                                }
                                                                                            }).start();
                                                                                            notifyIdentificationEndProcess();
                                                                                            showSuccessIdentificationCustomDialog("Identification Success");
                                                                                            pHandler.postDelayed(new Runnable() {
                                                                                                @Override
                                                                                                public void run() {
                                                                                                    successEmpDetailsDialog.cancel();
                                                                                                }
                                                                                            }, 2000);//4000//1000
                                                                                            break;
                                                                                        case "CARD-ONLY":
                                                                                            new Thread(new Runnable() {
                                                                                                @Override
                                                                                                public void run() {
                                                                                                    ForlinxGPIO.runGPIOLEDForSuccess();
                                                                                                }
                                                                                            }).start();
                                                                                            notifyIdentificationEndProcess();
                                                                                            showSuccessIdentificationCustomDialog("Identification Success");
                                                                                            pHandler.postDelayed(new Runnable() {
                                                                                                @Override
                                                                                                public void run() {
                                                                                                    successEmpDetailsDialog.cancel();
                                                                                                }
                                                                                            }, 2000);//4000//1000
                                                                                            break;
                                                                                        case "CARD+FINGER":
                                                                                            EmployeeAttendanceActivity.isDUI = true;//Disable on user Interaction
                                                                                            EmployeeAttendanceActivity.stopHandler();
                                                                                            cardId = morphoUser.getField(1);
                                                                                            showCardIdDialog(cardId, false);
                                                                                            break;
                                                                                        case "CARD+PIN+FINGER":
                                                                                            EmployeeAttendanceActivity.isDUI = true;//Disable on user Interaction
                                                                                            EmployeeAttendanceActivity.stopHandler();
                                                                                            cardId = morphoUser.getField(1);
                                                                                            showCardIdDialog(cardId, true);
                                                                                            break;
                                                                                        default:
                                                                                            notifyIdentificationEndProcess();
                                                                                            break;
                                                                                    }
                                                                                } else {
                                                                                    notifyIdentificationEndProcess();
                                                                                    Toast.makeText(context, "Verification Mode Not Found In SQLite", Toast.LENGTH_LONG).show();
                                                                                }
                                                                            } else {
                                                                                notifyIdentificationEndProcess();
                                                                                Toast.makeText(context, "Employee Data Not Found In SQLite", Toast.LENGTH_LONG).show();
                                                                            }
                                                                        }
                                                                        break;
                                                                    case "1:N":
                                                                        new Thread(new Runnable() {
                                                                            @Override
                                                                            public void run() {
                                                                                ForlinxGPIO.runGPIOLEDForSuccess();
                                                                            }
                                                                        }).start();
                                                                        notifyIdentificationEndProcess();
                                                                        showSuccessIdentificationCustomDialog("Identification Success");
                                                                        pHandler.postDelayed(new Runnable() {
                                                                            @Override
                                                                            public void run() {
                                                                                successEmpDetailsDialog.cancel();
                                                                            }
                                                                        }, 2000);//4000//1000
                                                                        break;
                                                                }
                                                            }
                                                        } else if (identifyRetvalue == -8) {
                                                            if (EmployeeAttendanceActivity.isAttendanceWindowVisisble) {
                                                                if (failureEmpDetailsDialog != null && failureEmpDetailsDialog.isShowing()) {
                                                                    failureEmpDetailsDialog.cancel();
                                                                }
                                                                new Thread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        ForlinxGPIO.runGPIOLEDForFailure();
                                                                    }
                                                                }).start();
                                                                notifyIdentificationEndProcess();
                                                                showFailureCustomDialog("Identification Failed", "Finger Print Did Not Match");
                                                                pHandler.postDelayed(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        failureEmpDetailsDialog.cancel();
                                                                    }
                                                                }, 2000);
                                                            }
                                                        } else if (identifyRetvalue == -26) {
                                                            //Toast.makeText(EmployeeAttendanceActivity.this,"IDENTIFICATION COMMAND ABORTED",Toast.LENGTH_LONG).show();
                                                            notifyIdentificationEndProcess();
                                                        } else {
                                                            if (EmployeeAttendanceActivity.isAttendanceWindowVisisble) {
                                                                if (failureEmpDetailsDialog != null && failureEmpDetailsDialog.isShowing()) {
                                                                    failureEmpDetailsDialog.cancel();
                                                                }
                                                                new Thread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        ForlinxGPIO.runGPIOLEDForFailure();
                                                                    }
                                                                }).start();
                                                                notifyIdentificationEndProcess();
                                                                showFailureCustomDialog("Identification Error", convertToInternationalMessage(identifyRetvalue, internalError));
                                                                pHandler.postDelayed(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        failureEmpDetailsDialog.cancel();
                                                                    }
                                                                }, 2000);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    });
                                    break;
                            }


//                            switch (Constants.GLOBAL_VERIFICATION_MODE) {
//                                case "CARD-BASED-VERIFY":
//                                    break;
//                                default:
//                                    notifyIdentificationEndProcess();
//                                    break;
//                            }


                        }
                    }
                }
            }));
            commandThread.start();
        } catch (Exception e) {
            showCustomAlertDialog(R.drawable.failure, "morphoDatabaseIdentify Exception", e.getMessage(), false);
        }
    }

    public static boolean isCDV = false; //isCardIdDialogVisible=false;
    public static EditText iCardId = null;

    private void showCardIdDialog(final String mCardId, final boolean isCPFMode) {//isCPFMode:IsCardPinFingerMode

        successEmpDetailsDialog = new Dialog(context);
        successEmpDetailsDialog.setCanceledOnTouchOutside(false);
        successEmpDetailsDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        successEmpDetailsDialog.setContentView(R.layout.card_id_dialog);

        ImageView icon = (ImageView) successEmpDetailsDialog.findViewById(R.id.image);
        TextView title = (TextView) successEmpDetailsDialog.findViewById(R.id.title);

        iCardId = (EditText) successEmpDetailsDialog.findViewById(R.id.cardId);

        Button btn_Save = (Button) successEmpDetailsDialog.findViewById(R.id.btn_Save);
        Button btn_Cancel = (Button) successEmpDetailsDialog.findViewById(R.id.btn_Cancel);

        icon.setImageResource(R.drawable.success);
        title.setText("Card Id");

        btn_Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String strCardId = iCardId.getText().toString().trim();
                if (strCardId.equals(mCardId)) {
                    if (!isCPFMode) {
                        isCDV = false;
                        successEmpDetailsDialog.cancel();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                ForlinxGPIO.runGPIOLEDForSuccess();
                            }
                        }).start();
                        notifyIdentificationEndProcess();
                        showSuccessIdentificationCustomDialog("Identification Success");
                        pHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                successEmpDetailsDialog.cancel();
                            }
                        }, 2000);//4000//1000
                    } else {
                        isCDV = false;
                        successEmpDetailsDialog.cancel();
                        showCardPinDialog(mCardId);
                    }
                } else {
                    isCDV = false;
                    successEmpDetailsDialog.cancel();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            ForlinxGPIO.runGPIOLEDForFailure();
                        }
                    }).start();
                    notifyIdentificationEndProcess();
                    showFailureCustomDialog("Identification Failed", "Card Id Mismatch");
                    pHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            failureEmpDetailsDialog.cancel();
                        }
                    }, 2000);
                }
            }
        });

        btn_Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isCDV = false;
                successEmpDetailsDialog.cancel();
                notifyIdentificationEndProcess();
            }
        });

        isCDV = true;
        successEmpDetailsDialog.show();

        WindowManager.LayoutParams lp = successEmpDetailsDialog.getWindow().getAttributes();
        lp.dimAmount = 0.9f;
        lp.buttonBrightness = 1.0f;
        lp.screenBrightness = 1.0f;
        successEmpDetailsDialog.getWindow().setAttributes(lp);
        successEmpDetailsDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }

    private void showCardPinDialog(final String mCardId) {

        successEmpDetailsDialog = new Dialog(context);
        successEmpDetailsDialog.setCanceledOnTouchOutside(false);
        successEmpDetailsDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        successEmpDetailsDialog.setContentView(R.layout.card_pin_dialog);

        ImageView icon = (ImageView) successEmpDetailsDialog.findViewById(R.id.image);
        TextView title = (TextView) successEmpDetailsDialog.findViewById(R.id.title);

        final EditText pin = (EditText) successEmpDetailsDialog.findViewById(R.id.Pin);

        Button btn_Save = (Button) successEmpDetailsDialog.findViewById(R.id.btn_Save);
        Button btn_Cancel = (Button) successEmpDetailsDialog.findViewById(R.id.btn_Cancel);

        icon.setImageResource(R.drawable.success);
        title.setText("Card Pin Details");

        btn_Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String strCurrentPin = pin.getText().toString().trim();
                if (strCurrentPin != null && strCurrentPin.trim().length() > 0) {
                    SQLiteCommunicator dbComm = new SQLiteCommunicator();
                    int autoId = dbComm.getAutoIdByCardId(mCardId);
                    if (autoId != -1) {
                        String pin = dbComm.getCardPinForVerification(autoId);
                        if (pin != null && pin.trim().length() > 0) {
                            if (pin.equals(strCurrentPin)) {
                                successEmpDetailsDialog.cancel();
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ForlinxGPIO.runGPIOLEDForSuccess();
                                    }
                                }).start();
                                notifyIdentificationEndProcess();
                                showSuccessIdentificationCustomDialog("Identification Success");
                                pHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        successEmpDetailsDialog.cancel();
                                    }
                                }, 2000);//4000//1000
                            } else {
                                successEmpDetailsDialog.cancel();
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ForlinxGPIO.runGPIOLEDForFailure();
                                    }
                                }).start();
                                notifyIdentificationEndProcess();
                                showFailureCustomDialog("Identification Failed", "Pin Mismatch");
                                pHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        failureEmpDetailsDialog.cancel();
                                    }
                                }, 2000);
                            }
                        } else {
                            successEmpDetailsDialog.cancel();
                            notifyIdentificationEndProcess();
                            Toast.makeText(context, "Pin Not Found In SQLite", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        successEmpDetailsDialog.cancel();
                        notifyIdentificationEndProcess();
                        Toast.makeText(context, "Card Data Not Found In SQLite", Toast.LENGTH_LONG).show();
                    }
                } else {
                    successEmpDetailsDialog.cancel();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            ForlinxGPIO.runGPIOLEDForFailure();
                        }
                    }).start();
                    notifyIdentificationEndProcess();
                    showFailureCustomDialog("Identification Failed", "Pin Mismatch");
                    pHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            failureEmpDetailsDialog.cancel();
                        }
                    }, 2000);
                }
            }
        });

        btn_Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                successEmpDetailsDialog.dismiss();
                notifyIdentificationEndProcess();
            }
        });


        successEmpDetailsDialog.show();

        WindowManager.LayoutParams lp = successEmpDetailsDialog.getWindow().getAttributes();
        lp.dimAmount = 0.9f;
        lp.buttonBrightness = 1.0f;
        lp.screenBrightness = 1.0f;
        successEmpDetailsDialog.getWindow().setAttributes(lp);
        successEmpDetailsDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);


    }


    private void showSubList(String disTitle, ArrayList <String> subList) {

        final Dialog dialog = new Dialog(context);
        dialog.setCanceledOnTouchOutside(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.subject_list_custom_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView title = (TextView) dialog.findViewById(R.id.title);
        title.setText(disTitle);

        final RadioGroup rg = (RadioGroup) dialog.findViewById(R.id.rg);

        Button btnCancel = (Button) dialog.findViewById(R.id.btnCancel);
        Button btnProceed = (Button) dialog.findViewById(R.id.btnProceed);

        for (int i = 0; i < subList.size(); i++) {
            RadioButton radioButton = null;
            radioButton = new RadioButton(context);
            radioButton.setText(subList.get(i));
            radioButton.setId(i);
            if (i == 0) {
                radioButton.setChecked(true);
            }
            radioButton.setTextSize(25.0f);
            rg.addView(radioButton);
        }


        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                isLoggedIn = false;
                tempId = -1;
                EmployeeAttendanceActivity.startHandler();
                startFingerIdentification();
            }
        });
        btnProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                int checkedRadioButtonId = rg.getCheckedRadioButtonId();
                RadioButton radioBtn = (RadioButton) dialog.findViewById(checkedRadioButtonId);
                subCode_subName = radioBtn.getText().toString();
                if (subCode_subName.trim().length() > 0) {
                    String[] subCodeName = subCode_subName.split(",");
                    if (subCodeName != null && subCodeName.length == 2) {
                        ArrayList <String> subTypesList = null;
                        int size = allList.size();
                        if (size > 0) {
                            subTypesList = new ArrayList <String>();
                            for (int i = 0; i < size; i++) {
                                SubInfo info = allList.get(i);
                                if (info.getSubCode().trim().equals(subCodeName[0].trim()) && info.getSubName().trim().equals(subCodeName[1].trim())) {
                                    subTypesList.add(info.getSubType());
                                }
                            }
                            size = subTypesList.size();
                            if (size > 1) {
                                showSubTypeList("Subject Types", subTypesList);
                            } else if (size == 1) {
                                subType = subTypesList.get(0).trim();
                                Calendar calendar = Calendar.getInstance();
                                SimpleDateFormat mdformat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss"); //Modified By Sanjay Shyamal
                                String strDateTime = mdformat.format(calendar.getTime());
                                String[] sc_sn = subCode_subName.split(",");

                                SQLiteCommunicator dbComm = new SQLiteCommunicator();
                                int status = dbComm.insertCollegeAttendanceData(profEID, profCID, "", "", sc_sn[0], subType, "IN", EmployeeAttendanceActivity.latitude, EmployeeAttendanceActivity.longitude);
                                if (status != -1) {
                                    dbComm.insertLoginStatus(profEID, profCID, sc_sn[0], subType, "Y", 0);
                                }

                                //EmployeeAttendanceActivity.empIdToVerfiy.setVisibility(View.INVISIBLE);

                                EmployeeAttendanceActivity.empIdToVerfiy.setEnabled(false);
                                EmployeeAttendanceActivity.tButton.setEnabled(false);
                                EmployeeAttendanceActivity.menuItem.setEnabled(false);
                                EmployeeAttendanceActivity.menuItem.setVisible(false);

                                EmployeeAttendanceActivity.startHandler();
                                startFingerIdentification();
                            }
                        }
                    }
                }
            }
        });

        dialog.show();
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        lp.dimAmount = 0.9f;
        lp.buttonBrightness = 1.0f;
        lp.screenBrightness = 1.0f;
        dialog.getWindow().setAttributes(lp);
        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }

    private void showSubTypeList(String disTitle, ArrayList <String> subTypesList) {

        final Dialog dialog = new Dialog(context);
        dialog.setCanceledOnTouchOutside(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.subject_type_list);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView title = (TextView) dialog.findViewById(R.id.title);
        title.setText(disTitle);

        final RadioGroup rg = (RadioGroup) dialog.findViewById(R.id.rg);

        Button btnCancel = (Button) dialog.findViewById(R.id.btnCancel);
        Button btnProceed = (Button) dialog.findViewById(R.id.btnProceed);

        for (int i = 0; i < subTypesList.size(); i++) {
            RadioButton radioButton = null;
            radioButton = new RadioButton(context);
            radioButton.setText(subTypesList.get(i));
            radioButton.setId(i);
            if (i == 0) {
                radioButton.setChecked(true);
            }
            radioButton.setTextSize(25.0f);
            rg.addView(radioButton);
        }


        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                showSubList("Subjects", finalList);
            }
        });
        btnProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                int checkedRadioButtonId = rg.getCheckedRadioButtonId();
                RadioButton radioBtn = (RadioButton) dialog.findViewById(checkedRadioButtonId);
                subType = radioBtn.getText().toString();

                SQLiteCommunicator dbComm = new SQLiteCommunicator();
                String[] sc_sn = subCode_subName.split(",");
                int status = dbComm.insertCollegeAttendanceData(profEID, profCID, "", "", sc_sn[0], subType, "IN", EmployeeAttendanceActivity.latitude, EmployeeAttendanceActivity.longitude);
                if (status != -1) {
                    dbComm.insertLoginStatus(profEID, profCID, sc_sn[0], subType, "Y", 0);
                }

                //EmployeeAttendanceActivity.empIdToVerfiy.setVisibility(View.INVISIBLE);

                EmployeeAttendanceActivity.empIdToVerfiy.setEnabled(false);
                EmployeeAttendanceActivity.tButton.setEnabled(false);
                EmployeeAttendanceActivity.menuItem.setEnabled(false);
                EmployeeAttendanceActivity.menuItem.setVisible(false);

                EmployeeAttendanceActivity.startHandler();
                startFingerIdentification();
            }
        });

        dialog.show();
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        lp.dimAmount = 0.9f;
        lp.buttonBrightness = 1.0f;
        lp.screenBrightness = 1.0f;
        dialog.getWindow().setAttributes(lp);
        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }

    //================================ Enroll Update Process End  ==============================//

    private void notifyEnrollUpdateEndProcess() {
        mHandler.post(new Runnable() {
            @Override
            public synchronized void run() {
                stopProcess(false);
            }
        });
    }

    //================================ Verification Process End  ================================//


    private void notifyVerificationEndProcess() {
        mHandler.post(new Runnable() {
            @Override
            public synchronized void run() {
                try {
                    if (context != null) {
                        Activity activity = (Activity) context;
                        if (activity != null && activity instanceof EmployeeAttendanceActivity) {
                            TextView textViewPutFingerMessage = activity.findViewById(getResIdFromContext("putFingerMessage"));
                            switch (EmployeeAttendanceActivity.gvm) {
                                case "CARD-BASED-VERIFY":
                                    textViewPutFingerMessage.setText("Put Finger On Sensor To Be Identified");
                                    break;
                                case "1:N":
                                    textViewPutFingerMessage.setText("Put Finger On Sensor To Be Identified");
                                    break;
                                case "CARD+FINGER":
                                    textViewPutFingerMessage.setText("Show Card");
                                    break;
                                case "CARD-ONLY":
                                    textViewPutFingerMessage.setText("Show Card");
                                    break;
                            }

                            TextView tvEmpIdToVerify = activity.findViewById(getResIdFromContext("empId"));
                            tvEmpIdToVerify.setText("");

                            ToggleButton tButton = activity.findViewById(getResIdFromContext("toggleButton1"));
                            tButton.setEnabled(true);

                            EmployeeAttendanceActivity.isCardReadingBlocked = false;
                            EmployeeAttendanceActivity.stopModeUpdate = false;

                            WiegandCommunicator.clearWiegand(Constants.WIEGAND_WRITE_PATH, "1");
                            EmployeeAttendanceActivity.isWiegandReading = false;

                            EmployeeAttendanceActivity.isDUI = false;//Enable on user Interaction

                            if (context != null) {
                                Activity act = (Activity) context;
                                if (act != null && act instanceof EmployeeAttendanceActivity) {
                                    updateSensorProgressBar(0);
                                    updateSensorMessage("Sensor Messages...");
                                }
                            }

                            boolean restart = ProcessInfo.getInstance().isReStartIdentification();
                            stopProcess(restart);
                        }
                    }
                } catch (Exception e) {
                    Log.d("TEST", "notify end process error:" + e.getMessage());
                }
            }
        });
    }

    //================================ Identification Process End  ==============================//

    private void notifyIdentificationEndProcess() {
        mHandler.post(new Runnable() {
            @Override
            public synchronized void run() {
                try {
                    EmployeeAttendanceActivity.isDUI = false;//Enable on user Interaction
                    boolean reStart = ProcessInfo.getInstance().isReStartIdentification();
                    Log.d("TEST", "********************* Restart *************");
                    Log.d("TEST", "Restart:" + reStart);
                    Log.d("TEST", "*******************************************");
                    stopProcess(reStart);
                    //EmployeeAttendanceActivity.stopModeUpdate = false;
                    //stopProcess(true);
                } catch (Exception e) {
                    showCustomAlertDialog(R.drawable.failure, "notifyEndProcess Exception", e.getMessage(), false);
                }
            }
        });
    }

    //============================== Common stop process  ==================================//

    private void stopProcess(boolean restart) {
        stop(restart);
    }

    //================================= stop process  =======================================//

    private void stop(boolean restart) {
        if (restart) {
            int value = Settings.getInstance().getFrTypeValue();
            if (value == 0) {
                morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
                morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
                if (morphoDevice != null && morphoDatabase != null) {
                    Log.d("TEST", "********************* Restart Identification ****************** ");
                    boolean isIdentificationStarted = ProcessInfo.getInstance().isIdentificationStarted();
                    boolean isVerificationStarted = ProcessInfo.getInstance().isVerificationStarted();
                    boolean isBioCommandStarted = ProcessInfo.getInstance().isCommandBioStart();
                    Log.d("TEST", " Is Identification Started:" + isIdentificationStarted);
                    Log.d("TEST", "Is Verification Started:" + isVerificationStarted);
                    Log.d("TEST", "Is Bio Command Started:" + isBioCommandStarted);
                    Log.d("TEST", "*********************************************");
                    if (!isIdentificationStarted && !isVerificationStarted && !isBioCommandStarted) {
                        pHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startFingerIdentification();
                                EmployeeAttendanceActivity.stopHandler();
                                EmployeeAttendanceActivity.startHandler();
                            }
                        }, 500);//1500//1000
                    }
                } else {
                    showCustomAlertDialog(R.drawable.failure, "Device Connection Status", "Finger Reader Not Found", false);
                }
            }
        }
    }

    //=============================== Observer Interface method  ===================================//

    @Override
    public void update(Observable observable, Object arg) {
        try {
            if (context != null) {
                Activity activity = (Activity) context;
                if (activity instanceof FingerEnrollUpdateDialogActivity) {
                    ForlinxGPIO.runGPIOForEnroll();
                }
            }
            // convert the object to a callback back message.
            CallbackMessage message = (CallbackMessage) arg;
            int type = message.getMessageType();

            switch (type) {

                case 1:
                    // message is a command.
                    Integer command = (Integer) message.getMessage();

                    // Analyze the command.
                    switch (command) {
                        case 0:
                            strMessage = "move-no-finger";
                            break;
                        case 1:
                            strMessage = "move-finger-up";
                            break;
                        case 2:
                            strMessage = "move-finger-down";
                            break;
                        case 3:
                            strMessage = "move-finger-left";
                            break;
                        case 4:
                            strMessage = "move-finger-right";
                            break;
                        case 5:
                            strMessage = "press-harder";
                            break;
                        case 6:
                            strMessage = "move-latent";
                            break;
                        case 7:
                            strMessage = "remove-finger";
                            break;
                        case 8:
                            strMessage = "finger-ok";

                            // switch live acquisition ImageView
                            if (isCaptureVerif) {
                                isCaptureVerif = false;
                                index = 4; //R.id.imageView5;
                            } else {
                                index++;
                            }

                            int resId;
                            switch (index) {
                                case 0:
                                    resId = getResIdFromContext("imageView1");
                                    if (context != null) {
                                        Activity activity = (Activity) context;
                                        if (activity instanceof EmployeeAttendanceActivity) {
                                            EmployeeAttendanceActivity.currentCaptureBitmapId = resId;
                                        } else if (activity instanceof FingerEnrollUpdateDialogActivity) {
                                            FingerEnrollUpdateDialogActivity.currentCaptureBitmapId = resId;
                                        }
                                    }
                                    break;
                                case 1:
                                    resId = getResIdFromContext("imageView2");
                                    if (context != null) {
                                        Activity activity = (Activity) context;
                                        if (activity instanceof EmployeeAttendanceActivity) {
                                            EmployeeAttendanceActivity.currentCaptureBitmapId = resId;
                                        } else if (activity instanceof FingerEnrollUpdateDialogActivity) {
                                            ForlinxGPIO.runGPIOForPressed();
                                            FingerEnrollUpdateDialogActivity.currentCaptureBitmapId = resId;
                                        }
                                    }
                                    break;
                                case 2:
                                    resId = getResIdFromContext("imageView3");
                                    if (context != null) {
                                        Activity activity = (Activity) context;
                                        if (activity instanceof EmployeeAttendanceActivity) {
                                            EmployeeAttendanceActivity.currentCaptureBitmapId = resId;
                                        } else if (activity instanceof FingerEnrollUpdateDialogActivity) {
                                            ForlinxGPIO.runGPIOForPressed();
                                            FingerEnrollUpdateDialogActivity.currentCaptureBitmapId = resId;
                                        }
                                    }
                                    break;
                                case 3:
                                    resId = getResIdFromContext("imageView4");
                                    if (context != null) {
                                        Activity activity = (Activity) context;
                                        if (activity instanceof EmployeeAttendanceActivity) {
                                            EmployeeAttendanceActivity.currentCaptureBitmapId = resId;
                                        } else if (activity instanceof FingerEnrollUpdateDialogActivity) {
                                            ForlinxGPIO.runGPIOForPressed();
                                            FingerEnrollUpdateDialogActivity.currentCaptureBitmapId = resId;
                                        }
                                    }
                                    break;
                                case 4:
                                    resId = getResIdFromContext("imageView5");
                                    if (context != null) {
                                        Activity activity = (Activity) context;
                                        if (activity instanceof EmployeeAttendanceActivity) {
                                            EmployeeAttendanceActivity.currentCaptureBitmapId = resId;
                                        } else if (activity instanceof FingerEnrollUpdateDialogActivity) {
                                            ForlinxGPIO.runGPIOForPressed();
                                            FingerEnrollUpdateDialogActivity.currentCaptureBitmapId = resId;
                                        }
                                    }
                                    break;
                                case 5:
                                    resId = getResIdFromContext("imageView6");
                                    if (context != null) {
                                        Activity activity = (Activity) context;
                                        if (activity instanceof EmployeeAttendanceActivity) {
                                            EmployeeAttendanceActivity.currentCaptureBitmapId = resId;
                                        } else if (activity instanceof FingerEnrollUpdateDialogActivity) {
                                            ForlinxGPIO.runGPIOForPressed();
                                            FingerEnrollUpdateDialogActivity.currentCaptureBitmapId = resId;
                                        }
                                    }
                                    break;
                                default:
                                    if (context != null) {
                                        Activity activity = (Activity) context;
                                        if (activity instanceof FingerEnrollUpdateDialogActivity) {
                                            ForlinxGPIO.runGPIOForPressed();
                                        }
                                    }
                                    break;
                            }
                            break;
                    }
                    mHandler.post(new Runnable() {
                        @Override
                        public synchronized void run() {
                            updateSensorMessage(strMessage);
                        }
                    });

                    break;
                case 2:
                    // message is a low resolution image, display it.
                    byte[] image = (byte[]) message.getMessage();
                    MorphoImage morphoImage = MorphoImage.getMorphoImageFromLive(image);
                    int imageRowNumber = morphoImage.getMorphoImageHeader().getNbRow();
                    int imageColumnNumber = morphoImage.getMorphoImageHeader().getNbColumn();
                    final Bitmap imageBmp = Bitmap.createBitmap(imageColumnNumber, imageRowNumber, Bitmap.Config.ALPHA_8);
                    imageBmp.copyPixelsFromBuffer(ByteBuffer.wrap(morphoImage.getImage(), 0, morphoImage.getImage().length));


//                    Log.d("TEST", "Height:" + imageBmp.getHeight() + " Width:" + imageBmp.getWidth());
//
//                    Log.d("TEST", "ROW NO:" + imageRowNumber + " COL NO:" + imageColumnNumber);
//
//                    int density = imageBmp.getDensity();
//                    Log.d("TEST", "Density:" + density);
//
//                    Log.d("TEST", "Pixel:" + morphoImage.getMorphoImageHeader().getNbBitsPerPixel());
//
//
//                    Log.d("TEST", "HAS ALPHA:" + imageBmp.hasAlpha());
//
//                    byte[] data = morphoImage.getCompressedImage();
//                    Log.d("TEST", "Data:" + data);
//                    if (data != null) {
//                        Log.d("TEST", "Data len:" + data.length);
//                    }
//
//                    if (image != null) {
//                        Log.d("TEST", "Image Len:" + image.length);
//                    }
//
//                    Log.d("TEST", "Image Bitmap Byte Count:" + imageBmp.getByteCount());

                    mHandler.post(new Runnable() {
                        @Override
                        public synchronized void run() {
                            if (context != null) {
                                Activity activity = (Activity) context;
                                if (activity != null && activity instanceof FingerEnrollUpdateDialogActivity) {
                                    updateImage(imageBmp, FingerEnrollUpdateDialogActivity.currentCaptureBitmapId);
                                } else if (activity != null && activity instanceof EmployeeAttendanceActivity) {
                                    //updateImage(imageBmp, EmployeeAttendanceActivity.currentCaptureBitmapId);
                                    id = EmployeeAttendanceActivity.currentCaptureBitmapId;
                                }
                            }
                            //updateImage(imageBmp, EmployeeAttendanceActivity.currentCaptureBitmapId);
                        }
                    });
                    break;
                case 3:
                    // message is the coded image quality.
                    final Integer quality = (Integer) message.getMessage();
                    mHandler.post(new Runnable() {
                        @Override
                        public synchronized void run() {
                            updateSensorProgressBar(quality);
                        }
                    });
                    break;
                //case 4:
                //byte[] enrollcmd = (byte[]) message.getMessage();
            }
        } catch (Exception e) {
            showCustomAlertDialog(R.drawable.failure, "observer update exception", e.getMessage(), false);
        }
    }


    //============================ Morpho defined user functions for updating UI  ===============================//

    int id;

    @SuppressWarnings("deprecation")
    private void updateSensorProgressBar(int level) {
        try {
            Activity activity = (Activity) context;
            ProgressBar progressBar = (ProgressBar) (activity.findViewById(R.id.vertical_progressbar));

            //ProgressBar progressBar = (ProgressBar)findViewById(R.id.vertical_progressbar);

            final float[] roundedCorners = new float[]{5, 5, 5, 5, 5, 5, 5, 5};
            ShapeDrawable pgDrawable = new ShapeDrawable(new RoundRectShape(roundedCorners, null, null));

            int color = Color.GREEN;

            if (level <= 25) {
                color = Color.RED;
            } else if (level <= 50) {
                color = Color.YELLOW;
            }
            pgDrawable.getPaint().setColor(color);
            ClipDrawable progress = new ClipDrawable(pgDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
            progressBar.setProgressDrawable(progress);
            progressBar.setBackgroundDrawable(context.getResources().getDrawable(android.R.drawable.progress_horizontal));
            progressBar.setProgress(level);

            if (activity instanceof EmployeeAttendanceActivity) {

                // EmployeeAttendanceActivity.stopHandler();
                // EmployeeAttendanceActivity.startHandler();

                EmployeeAttendanceActivity.setScreenBrightness(150);
                ForlinxGPIO.setLCDBackLightOn();

                if (level == 0) {
                    // View bar = (View) activity.findViewById(R.id.bar);
                    //  bar.setBackgroundColor(activity.getResources().getColor(R.color.red));
                    if (context != null) {
                        Activity act = (Activity) context;
                        if (act != null && act instanceof EmployeeAttendanceActivity) {
                            ImageView iv = (ImageView) act.findViewById(id);
                            iv.setImageResource(0);
                        }
                    }
                } else {
                    //  View bar = (View) activity.findViewById(R.id.bar);
                    // bar.setBackgroundColor(activity.getResources().getColor(R.color.dark_green));
                    if (context != null) {
                        Activity act = (Activity) context;
                        if (act != null && act instanceof EmployeeAttendanceActivity) {
                            ImageView iv = (ImageView) act.findViewById(id);
                            iv.setImageResource(R.drawable.fingerscan2);
                        }
                    }
                }
            } else if (activity instanceof FingerEnrollUpdateDialogActivity) {
                FingerEnrollUpdateDialogActivity.stopHandler();
                FingerEnrollUpdateDialogActivity.startHandler();
                FingerEnrollUpdateDialogActivity.setScreenBrightness(150);
                ForlinxGPIO.setLCDBackLightOn();
            }
        } catch (Exception e) {
            e.getMessage();
        }
    }

    private void updateSensorMessage(String sensorMessage) {
        try {
            Activity activity = (Activity) context;
            TextView tv = (TextView) (activity.findViewById(R.id.textViewMessage));
            tv.setText(sensorMessage);
        } catch (Exception e) {
            e.getMessage();
        }
    }

    public void updateImage(Bitmap bitmap, int id) {
        try {
            if (context != null) {
                Activity activity = (Activity) context;
                if (activity != null && (activity instanceof FingerEnrollUpdateDialogActivity || activity instanceof EmployeeAttendanceActivity)) {
                    ImageView iv = (ImageView) activity.findViewById(id);
                    iv.setImageBitmap(bitmap);
                }
            }
        } catch (Exception e) {
            e.getMessage();
        }
    }


    //========================= Custom Dialogs for various process ========================//


    //================================ enroll dialog =====================================//

    private void enrollDialog(String title, String message, final TemplateList templateList) {
        final EmployeeFingerEnrollInfo empFingerInfo = EmployeeFingerEnrollInfo.getInstance();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Finger Captured Successfully. Do You Want To Save Finger Data?").setTitle(title)
                .setIcon(R.drawable.success)
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        fingerEnrollAlert.dismiss();
                        if (empFingerInfo != null) {
                            SQLiteCommunicator dbComm = new SQLiteCommunicator();
                            int noOfFingers = empFingerInfo.getNoOfFingers();
                            String strVerificationMode = empFingerInfo.getStrVerificationMode();
                            int ret;
                            switch (noOfFingers) {
                                case 1:
                                    ret = -111;//Custom defined error
                                    switch (strVerificationMode) {
                                        case "1:N"://For 1:N Verification Mode Insert Template to morpho and sliqte db
                                            int empAutoId = dbComm.getAutoIdByEmpId(empFingerInfo.getEmpId());
                                            String enrollType = empFingerInfo.getEnrollType();
                                            if (enrollType != null && enrollType.trim().length() > 0 && enrollType.equals("RE")) {
                                                if (empAutoId == -1) {
                                                    ret = dbComm.insertLocallyEnrolledEmployeeData(empFingerInfo);
                                                    if (ret != -1) {
                                                        empAutoId = ret;
                                                    }
                                                }
                                            }
                                            if (empAutoId != -1) {
                                                ret = insertOneTemplateToMorphoDB(templateList);
                                                if (ret == ErrorCodes.MORPHO_OK) {
                                                    int empFingerId = -1;
                                                    String firstFingerIndex = empFingerInfo.getStrFirstFingerIndex();
                                                    String secondFingerIndex = empFingerInfo.getStrSecondFingerIndex();
                                                    if (firstFingerIndex != null && firstFingerIndex.trim().length() > 0) {
                                                        int fingerId = dbComm.checkTemplateExistsByFT(empAutoId, "1");//First Finger Type
                                                        if (fingerId == -1) {
                                                            empFingerId = dbComm.insertOneTemplateToSqliteDb(empAutoId, "Morpho");
                                                            if (empFingerId != -1) {
                                                                String enrollStatus = "Y";
                                                                String isAadhaarVer = "N";
                                                                int nof = dbComm.getNoFingersEnrolled(empAutoId);
                                                                fingerId = dbComm.updateFingerDataToEmpTable(empAutoId, nof, enrollStatus, isAadhaarVer);
                                                                if (fingerId != -1) {
                                                                    if (enrollType != null && enrollType.trim().length() > 0 && enrollType.equals("RE")) {
                                                                        String statusMsg = "0000";
                                                                        String url = "http://" + EzeeHrLiteCommunicator.serverIP + ":" + EzeeHrLiteCommunicator.serverPort + Constants.POST_UNFINISHED_JOB_URL;
                                                                        String jid = empFingerInfo.getRemoteEnrollJobId();
                                                                        String postJobJson = JSONCreatorParser.getPostJobJson(Constants.CORPORATE_ID, imei, EzeeHrLiteCommunicator.deviceToken, jid, statusMsg);
                                                                        AsyncTaskPostUnfinishedJob task = new AsyncTaskPostUnfinishedJob();
                                                                        task.execute(url, postJobJson);
                                                                    }
                                                                    showCustomAlertDialog(R.drawable.success, "Finger Save Status", "Finger Data Saved Successfully", true);
                                                                } else {
                                                                    showCustomAlertDialog(R.drawable.failure, "Finger Save Status", "Failed to update finger data to employee table", true);
                                                                }
                                                            } else {
                                                                showCustomAlertDialog(R.drawable.failure, "Finger Save Status", "Failed to insert finger data to finger table", true);
                                                            }
                                                        } else {
                                                            int status = dbComm.updateOneRemoteEnrolledTemplate(empAutoId, fingerId, empFingerInfo);
                                                            if (status != -1) {
                                                                String enrollStatus = "Y";
                                                                String isAadhaarVer = "N";
                                                                int nof = dbComm.getNoFingersEnrolled(empAutoId);
                                                                status = dbComm.updateFingerDataToEmpTable(empAutoId, nof, enrollStatus, isAadhaarVer);
                                                                if (status != -1) {
                                                                    if (enrollType != null && enrollType.trim().length() > 0 && enrollType.equals("RE")) {
                                                                        String statusMsg = "0000";
                                                                        String url = "http://" + EzeeHrLiteCommunicator.serverIP + ":" + EzeeHrLiteCommunicator.serverPort + Constants.POST_UNFINISHED_JOB_URL;
                                                                        String jid = empFingerInfo.getRemoteEnrollJobId();
                                                                        String postJobJson = JSONCreatorParser.getPostJobJson(Constants.CORPORATE_ID, imei, EzeeHrLiteCommunicator.deviceToken, jid, statusMsg);
                                                                        AsyncTaskPostUnfinishedJob task = new AsyncTaskPostUnfinishedJob();
                                                                        task.execute(url, postJobJson);
                                                                    }
                                                                    showCustomAlertDialog(R.drawable.success, "Finger Save Status", "Finger Data Saved Successfully", true);
                                                                } else {
                                                                    showCustomAlertDialog(R.drawable.failure, "Finger Save Status", "Failed to update finger data to employee table", true);
                                                                }
                                                            }
                                                        }
                                                    } else if (secondFingerIndex != null && secondFingerIndex.trim().length() > 0) {
                                                        int fingerId = dbComm.checkTemplateExistsByFT(empAutoId, "2");//First Finger Type
                                                        if (fingerId == -1) {
                                                            empFingerId = dbComm.insertOneTemplateToSqliteDb(empAutoId, "Morpho");
                                                            if (empFingerId != -1) {
                                                                String enrollStatus = "Y";
                                                                String isAadhaarVer = "N";
                                                                int nof = dbComm.getNoFingersEnrolled(empAutoId);
                                                                fingerId = dbComm.updateFingerDataToEmpTable(empAutoId, nof, enrollStatus, isAadhaarVer);
                                                                if (fingerId != -1) {
                                                                    if (enrollType != null && enrollType.trim().length() > 0 && enrollType.equals("RE")) {
                                                                        String statusMsg = "0000";
                                                                        String url = "http://" + EzeeHrLiteCommunicator.serverIP + ":" + EzeeHrLiteCommunicator.serverPort + Constants.POST_UNFINISHED_JOB_URL;
                                                                        String jid = empFingerInfo.getRemoteEnrollJobId();
                                                                        String postJobJson = JSONCreatorParser.getPostJobJson(Constants.CORPORATE_ID, imei, EzeeHrLiteCommunicator.deviceToken, jid, statusMsg);
                                                                        AsyncTaskPostUnfinishedJob task = new AsyncTaskPostUnfinishedJob();
                                                                        task.execute(url, postJobJson);
                                                                    }
                                                                    showCustomAlertDialog(R.drawable.success, "Finger Save Status", "Finger Data Saved Successfully", true);
                                                                } else {
                                                                    showCustomAlertDialog(R.drawable.failure, "Finger Save Status", "Failed to update finger data to employee table", true);
                                                                }
                                                            } else {
                                                                showCustomAlertDialog(R.drawable.failure, "Finger Save Status", "Failed to insert finger data to finger table", true);
                                                            }
                                                        } else {
                                                            int status = dbComm.updateOneRemoteEnrolledTemplate(empAutoId, fingerId, empFingerInfo);
                                                            if (status != -1) {
                                                                String enrollStatus = "Y";
                                                                String isAadhaarVer = "N";
                                                                int nof = dbComm.getNoFingersEnrolled(empAutoId);
                                                                status = dbComm.updateFingerDataToEmpTable(empAutoId, nof, enrollStatus, isAadhaarVer);
                                                                if (status != -1) {
                                                                    if (enrollType != null && enrollType.trim().length() > 0 && enrollType.equals("RE")) {
                                                                        String statusMsg = "0000";
                                                                        String url = "http://" + EzeeHrLiteCommunicator.serverIP + ":" + EzeeHrLiteCommunicator.serverPort + Constants.POST_UNFINISHED_JOB_URL;
                                                                        String jid = empFingerInfo.getRemoteEnrollJobId();
                                                                        String postJobJson = JSONCreatorParser.getPostJobJson(Constants.CORPORATE_ID, imei, EzeeHrLiteCommunicator.deviceToken, jid, statusMsg);
                                                                        AsyncTaskPostUnfinishedJob task = new AsyncTaskPostUnfinishedJob();
                                                                        task.execute(url, postJobJson);
                                                                    }
                                                                    showCustomAlertDialog(R.drawable.success, "Finger Save Status", "Finger Data Saved Successfully", true);
                                                                } else {
                                                                    showCustomAlertDialog(R.drawable.failure, "Finger Save Status", "Failed to update finger data to employee table", true);
                                                                }
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    int internalError = -111;
                                                    if (morphoDevice != null) {
                                                        internalError = morphoDevice.getInternalError();
                                                    }
                                                    showCustomAlertDialog(R.drawable.failure, "Finger Save Status", "Failed to insert finger data to morpho database\nError Reason:" + convertToInternationalMessage(ret, internalError), false);
                                                }
                                            } else {
                                                showCustomAlertDialog(R.drawable.failure, "Finger Save Status", "Employee id not enrolled", true);
                                            }
                                            break;
                                        default:
                                            empAutoId = dbComm.getAutoIdByEmpId(empFingerInfo.getEmpId());
                                            enrollType = empFingerInfo.getEnrollType();
                                            if (enrollType != null && enrollType.trim().length() > 0 && enrollType.equals("RE")) {
                                                if (enrollType != null && enrollType.trim().length() > 0 && enrollType.equals("RE")) {
                                                    if (empAutoId == -1) {
                                                        ret = dbComm.insertLocallyEnrolledEmployeeData(empFingerInfo);
                                                        if (ret != -1) {
                                                            empAutoId = ret;
                                                        }
                                                    }
                                                }
                                            }
                                            ret = insertOneTemplateToMorphoDB(templateList);
                                            if (ret == ErrorCodes.MORPHO_OK) {
                                                String firstFingerIndex = empFingerInfo.getStrFirstFingerIndex();
                                                String secondFingerIndex = empFingerInfo.getStrSecondFingerIndex();
                                                if (firstFingerIndex != null && firstFingerIndex.trim().length() > 0) {
                                                    int fingerId = dbComm.checkTemplateExistsByFT(empAutoId, "1");//First Finger Type
                                                    if (fingerId == -1) {
                                                        int empFingerId = -1;
                                                        empFingerId = dbComm.insertOneTemplateToSqliteDb(empAutoId, "Morpho");
                                                        if (empFingerId != -1) {
                                                            String enrollStatus = "Y";
                                                            String isAadhaarVer = "N";
                                                            int nof = dbComm.getNoFingersEnrolled(empAutoId);
                                                            fingerId = dbComm.updateFingerDataToEmpTable(empAutoId, nof, enrollStatus, isAadhaarVer);
                                                            if (fingerId != -1) {
                                                                if (enrollType != null && enrollType.trim().length() > 0 && enrollType.equals("RE")) {
                                                                    String statusMsg = "0000";
                                                                    String url = "http://" + EzeeHrLiteCommunicator.serverIP + ":" + EzeeHrLiteCommunicator.serverPort + Constants.POST_UNFINISHED_JOB_URL;
                                                                    String jid = empFingerInfo.getRemoteEnrollJobId();
                                                                    String postJobJson = JSONCreatorParser.getPostJobJson(Constants.CORPORATE_ID, imei, EzeeHrLiteCommunicator.deviceToken, jid, statusMsg);
                                                                    AsyncTaskPostUnfinishedJob task = new AsyncTaskPostUnfinishedJob();
                                                                    task.execute(url, postJobJson);
                                                                }
                                                                showCustomAlertDialog(R.drawable.success, "Finger Save Status", "Finger Data Saved Successfully", true);
                                                            } else {
                                                                showCustomAlertDialog(R.drawable.failure, "Finger Save Status", "Failed to update finger data to employee table", true);
                                                            }
                                                        } else {
                                                            showCustomAlertDialog(R.drawable.failure, "Finger Save Status", "Failed to insert finger data to finger table", true);
                                                        }
                                                    } else {
                                                        int status = dbComm.updateOneRemoteEnrolledTemplate(empAutoId, fingerId, empFingerInfo);
                                                        if (status != -1) {
                                                            String enrollStatus = "Y";
                                                            String isAadhaarVer = "N";
                                                            int nof = dbComm.getNoFingersEnrolled(empAutoId);
                                                            status = dbComm.updateFingerDataToEmpTable(empAutoId, nof, enrollStatus, isAadhaarVer);
                                                            if (status != -1) {
                                                                if (enrollType != null && enrollType.trim().length() > 0 && enrollType.equals("RE")) {
                                                                    String statusMsg = "0000";
                                                                    String url = "http://" + EzeeHrLiteCommunicator.serverIP + ":" + EzeeHrLiteCommunicator.serverPort + Constants.POST_UNFINISHED_JOB_URL;
                                                                    String jid = empFingerInfo.getRemoteEnrollJobId();
                                                                    String postJobJson = JSONCreatorParser.getPostJobJson(Constants.CORPORATE_ID, imei, EzeeHrLiteCommunicator.deviceToken, jid, statusMsg);
                                                                    AsyncTaskPostUnfinishedJob task = new AsyncTaskPostUnfinishedJob();
                                                                    task.execute(url, postJobJson);
                                                                }
                                                                showCustomAlertDialog(R.drawable.success, "Finger Save Status", "Finger Data Saved Successfully", true);
                                                            } else {
                                                                showCustomAlertDialog(R.drawable.failure, "Finger Save Status", "Failed to update finger data to employee table", true);
                                                            }
                                                        }
                                                    }
                                                } else if (secondFingerIndex != null && secondFingerIndex.trim().length() > 0) {
                                                    int fingerId = dbComm.checkTemplateExistsByFT(empAutoId, "2");//First Finger Type
                                                    if (fingerId == -1) {
                                                        int empFingerId = -1;
                                                        empFingerId = dbComm.insertOneTemplateToSqliteDb(empAutoId, "Morpho");
                                                        if (empFingerId != -1) {
                                                            String enrollStatus = "Y";
                                                            String isAadhaarVer = "N";
                                                            int nof = dbComm.getNoFingersEnrolled(empAutoId);
                                                            fingerId = dbComm.updateFingerDataToEmpTable(empAutoId, nof, enrollStatus, isAadhaarVer);
                                                            if (fingerId != -1) {
                                                                if (enrollType != null && enrollType.trim().length() > 0 && enrollType.equals("RE")) {
                                                                    String statusMsg = "0000";
                                                                    String url = "http://" + EzeeHrLiteCommunicator.serverIP + ":" + EzeeHrLiteCommunicator.serverPort + Constants.POST_UNFINISHED_JOB_URL;
                                                                    String jid = empFingerInfo.getRemoteEnrollJobId();
                                                                    String postJobJson = JSONCreatorParser.getPostJobJson(Constants.CORPORATE_ID, imei, EzeeHrLiteCommunicator.deviceToken, jid, statusMsg);
                                                                    AsyncTaskPostUnfinishedJob task = new AsyncTaskPostUnfinishedJob();
                                                                    task.execute(url, postJobJson);
                                                                }
                                                                showCustomAlertDialog(R.drawable.success, "Finger Save Status", "Finger Data Saved Successfully", true);
                                                            } else {
                                                                showCustomAlertDialog(R.drawable.failure, "Finger Save Status", "Failed to update finger data to employee table", true);
                                                            }
                                                        } else {
                                                            showCustomAlertDialog(R.drawable.failure, "Finger Save Status", "Failed to insert finger data to finger table", true);
                                                        }
                                                    } else {
                                                        int status = dbComm.updateOneRemoteEnrolledTemplate(empAutoId, fingerId, empFingerInfo);
                                                        if (status != -1) {
                                                            String enrollStatus = "Y";
                                                            String isAadhaarVer = "N";
                                                            int nof = dbComm.getNoFingersEnrolled(empAutoId);
                                                            status = dbComm.updateFingerDataToEmpTable(empAutoId, nof, enrollStatus, isAadhaarVer);
                                                            if (status != -1) {
                                                                if (enrollType != null && enrollType.trim().length() > 0 && enrollType.equals("RE")) {
                                                                    String statusMsg = "0000";
                                                                    String url = "http://" + EzeeHrLiteCommunicator.serverIP + ":" + EzeeHrLiteCommunicator.serverPort + Constants.POST_UNFINISHED_JOB_URL;
                                                                    String jid = empFingerInfo.getRemoteEnrollJobId();
                                                                    String postJobJson = JSONCreatorParser.getPostJobJson(Constants.CORPORATE_ID, imei, EzeeHrLiteCommunicator.deviceToken, jid, statusMsg);
                                                                    AsyncTaskPostUnfinishedJob task = new AsyncTaskPostUnfinishedJob();
                                                                    task.execute(url, postJobJson);
                                                                }
                                                                showCustomAlertDialog(R.drawable.success, "Finger Save Status", "Finger Data Saved Successfully", true);
                                                            } else {
                                                                showCustomAlertDialog(R.drawable.failure, "Finger Save Status", "Failed to update finger data to employee table", true);
                                                            }
                                                        }
                                                    }
                                                }
                                            } else {
                                                int internalError = -111;
                                                if (morphoDevice != null) {
                                                    internalError = morphoDevice.getInternalError();
                                                }
                                                showCustomAlertDialog(R.drawable.failure, "Finger Save Status", "Failed to insert finger data to morpho database\nError Reason:" + convertToInternationalMessage(ret, internalError), false);
                                            }
                                            break;
                                    }
                                    break;
                                case 2:
                                    ret = -111;
                                    switch (strVerificationMode) {
                                        case "1:N":
                                            String empId = empFingerInfo.getEmpId();
                                            int empAutoId = dbComm.getAutoIdByEmpId(empId);
                                            if (empAutoId != -1) {
                                                ret = insertTwoTemplatesToMorphoDB(templateList);
                                                if (ret == ErrorCodes.MORPHO_OK) {
                                                    int empFingerId = -1;
                                                    empFingerId = dbComm.insertTwoTemplatesToSqliteDb(empAutoId, "Morpho");
                                                    if (empFingerId != -1) {
                                                        String enrollStatus = "Y";
                                                        String isAadhaarVer = "N";
                                                        int status = dbComm.updateFingerDataToEmpTable(empAutoId, enrollStatus, isAadhaarVer);
                                                        if (status != -1) {
                                                            // publishFingerData(empAutoId);
                                                            String role = UserDetails.getInstance().getRole();//Y=Admin
                                                            int smartReader = Settings.getInstance().getSrTypeValue();
                                                            if (role.equals("Y") && (smartReader == 0 || smartReader == 1 || smartReader == 2)) {//0=RC632,1=MicroSmartV2//2=RC522
                                                                cardWriteConfirmDialog(R.drawable.success, "Finger Save Status", "Finger Data Saved Successfully ! Do You Want To Write Data Into Smart Card ?");
                                                            } else {
                                                                showCustomAlertDialog(R.drawable.success, "Finger Save Status", "Finger Data Saved Successfully", true);
                                                            }
                                                        } else {
                                                            showCustomAlertDialog(R.drawable.failure, "Finger Save Status", "Failed to update finger data to employee table", true);
                                                        }
                                                    } else {
                                                        showCustomAlertDialog(R.drawable.failure, "Finger Save Status", "Failed to insert finger data to finger table", true);
                                                    }
                                                } else {
                                                    int internalError = -111;
                                                    if (morphoDevice != null) {
                                                        internalError = morphoDevice.getInternalError();
                                                    }
                                                    showCustomAlertDialog(R.drawable.failure, "Finger Save Status", "Failed to insert finger data to morpho database\nError Reason:" + convertToInternationalMessage(ret, internalError), true);
                                                }
                                            } else {
                                                showCustomAlertDialog(R.drawable.failure, "Finger Save Status", "Employee id not enrolled", true);
                                            }
                                            break;
                                        default:
                                            empId = empFingerInfo.getEmpId();
                                            empAutoId = dbComm.getAutoIdByEmpId(empId);
                                            if (empAutoId != -1) {
                                                ret = insertTwoTemplatesToMorphoDB(templateList);
                                                if (ret == ErrorCodes.MORPHO_OK) {
                                                    int empFingerId = -1;
                                                    empFingerId = dbComm.insertTwoTemplatesToSqliteDb(empAutoId, "Morpho");
                                                    if (empFingerId != -1) {
                                                        String enrollStatus = "Y";
                                                        String isAadhaarVer = "N";
                                                        int status = dbComm.updateFingerDataToEmpTable(empAutoId, enrollStatus, isAadhaarVer);
                                                        if (status != -1) {
                                                            String role = UserDetails.getInstance().getRole();//Y=Admin
                                                            int smartReader = Settings.getInstance().getSrTypeValue();
                                                            if (role.equals("Y") && (smartReader == 0 || smartReader == 1 || smartReader == 2)) {//0=RC632,1=MicroSmartV2//2=RC522
                                                                cardWriteConfirmDialog(R.drawable.success, "Finger Save Status", "Finger Data Saved Successfully ! Do You Want To Write Data Into Smart Card ?");
                                                            } else {
                                                                showCustomAlertDialog(R.drawable.success, "Finger Save Status", "Finger Data Saved Successfully", true);
                                                            }
                                                        } else {
                                                            showCustomAlertDialog(R.drawable.failure, "Finger Save Status", "Failed to update finger data to employee table", true);
                                                        }
                                                    } else {
                                                        showCustomAlertDialog(R.drawable.failure, "Finger Save Status", "Failed to insert finger data to finger table", true);
                                                    }
                                                } else {
                                                    int internalError = -111;
                                                    if (morphoDevice != null) {
                                                        internalError = morphoDevice.getInternalError();
                                                    }
                                                    showCustomAlertDialog(R.drawable.failure, "Finger Save Status", "Failed to insert finger data to morpho database\nError Reason:" + convertToInternationalMessage(ret, internalError), true);
                                                }
                                            } else {
                                                showCustomAlertDialog(R.drawable.failure, "Finger Save Status", "Employee id not enrolled", true);
                                            }
                                            break;
                                    }
                                    break;
                            }
                        } else {
                            showCustomAlertDialog(R.drawable.failure, "Error", "Employee and finger details not found !", true);
                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        fingerEnrollAlert.dismiss();
                        String enrollType = empFingerInfo.getEnrollType();
                        if (enrollType != null && enrollType.trim().length() > 0 && enrollType.equals("RE")) {
                            String statusMsg = "0000";
                            String url = "http://" + EzeeHrLiteCommunicator.serverIP + ":" + EzeeHrLiteCommunicator.serverPort + Constants.POST_UNFINISHED_JOB_URL;
                            String jid = empFingerInfo.getRemoteEnrollJobId();
                            String postJobJson = JSONCreatorParser.getPostJobJson(Constants.CORPORATE_ID, imei, EzeeHrLiteCommunicator.deviceToken, jid, statusMsg);
                            AsyncTaskPostUnfinishedJob task = new AsyncTaskPostUnfinishedJob();
                            task.execute(url, postJobJson);
                        }
                        Intent intent = new Intent(context, EmployeeFingerEnrollmentActivity.class);
                        context.startActivity(intent);
                    }
                });

        fingerEnrollAlert = builder.create();
        fingerEnrollAlert.setCanceledOnTouchOutside(false);
        fingerEnrollAlert.show();
    }

    private void publishFingerData(int id) {
        SQLiteCommunicator dbComm = new SQLiteCommunicator();
        ArrayList <TemplateUploadInfo> templateUploadInfoList = null;
        templateUploadInfoList = dbComm.getTemplateForAutoUploadByEmpId(id, templateUploadInfoList);//Database Communication
        if (templateUploadInfoList != null) {
            int size = templateUploadInfoList.size();
            if (size > 0) {
                String deviceToken = "ABCD1234";
                Random random = new Random();
                String packetId = String.format("%04d", random.nextInt(10000));
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                String imei = tel.getDeviceId();
                for (int i = 0; i < size; i++) {
                    String payload = JSONCreatorParser.createTemplateUploadJsonForMqtt(imei, deviceToken, Constants.TEMPLATE_UPLOAD_JOB_COMM, Constants.CORPORATE_ID, packetId, templateUploadInfoList.get(i));
                    if (payload != null && payload.trim().length() > 0) {
                        MqttAndroidClient mqttClient = MqttClientInfo.getInstance().getMqttAndroidClient();
                        if (mqttClient != null && mqttClient.isConnected()) {
                            MqttApi mqttApi = new MqttApi(context);
                            MqttMessage message = new MqttMessage();
                            message.setPayload(payload.getBytes());
                            try {
                                String templatePubTopic = Constants.TEMPLATE_RECV_TOPIC + Constants.OU + "/" + Constants.DEV_TYPE + "/" + Constants.DEV_COMP_NAME + "/CTS/" + imei;
                                Log.d("TEST", "Template Pub Topic:" + templatePubTopic);
                                try {
                                    mqttApi.publish(mqttClient, templatePubTopic, message);
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                            } catch (MqttException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

    //================================ update dialog =====================================//

    private void updateDialog(String title, String message, final TemplateList templateList) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Finger Captured Successfully. Do You Want To Update Finger Data?").setTitle(title)
                .setIcon(R.drawable.success)
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        fingerUpdateAlert.dismiss();
                        EmployeeFingerEnrollInfo empFingerInfo = EmployeeFingerEnrollInfo.getInstance();
                        if (empFingerInfo != null) {
                            int ret = -1;
                            SQLiteCommunicator dbComm = new SQLiteCommunicator();
                            String strVerificationMode = empFingerInfo.getStrVerificationMode();
                            int noOfFingers = empFingerInfo.getNoOfFingers();
                            if (noOfFingers == 1) {
                                switch (strVerificationMode) {
                                    case "1:N":
                                        ret = updateOneTemplateToMorphoDB(templateList);
                                        if (ret == ErrorCodes.MORPHO_OK) {
                                            String empId = empFingerInfo.getEmpId();
                                            int empAutoId = dbComm.getAutoIdByEmpId(empId);
                                            if (empAutoId != -1) {
                                                int empFingerId = -1;
                                                empFingerId = dbComm.updateOneTemplateToSqliteDb(empAutoId);
                                                if (empFingerId != -1) {
                                                    showCustomAlertDialog(R.drawable.success, "Finger Update Status", "Finger Data Updated Successfully !", true);
                                                } else {
                                                    showCustomAlertDialog(R.drawable.failure, "Finger Update Status", "Failed to update finger data to finger table", true);
                                                }
                                            } else {
                                                showCustomAlertDialog(R.drawable.failure, "Finger Update Status", "Employee id not enrolled", true);
                                            }
                                        } else {
                                            int internalError = -111;
                                            if (morphoDevice != null) {
                                                internalError = morphoDevice.getInternalError();
                                            }
                                            showCustomAlertDialog(R.drawable.failure, "Finger Save Status", "Failed to insert finger data to morpho database\nError Reason:" + convertToInternationalMessage(ret, internalError), true);
                                        }
                                        break;
                                    default:
                                        ret = updateOneTemplateToMorphoDB(templateList);
                                        if (ret == ErrorCodes.MORPHO_OK) {
                                            String empId = empFingerInfo.getEmpId();
                                            int empAutoId = dbComm.getAutoIdByEmpId(empId);
                                            if (empAutoId != -1) {
                                                int empFingerId = -1;
                                                empFingerId = dbComm.updateOneTemplateToSqliteDb(empAutoId);
                                                if (empFingerId != -1) {
                                                    showCustomAlertDialog(R.drawable.success, "Finger Update Status", "Finger Data Updated Successfully !", true);
                                                } else {
                                                    showCustomAlertDialog(R.drawable.failure, "Finger Update Status", "Failed to update finger data to finger table", true);
                                                }
                                            } else {
                                                showCustomAlertDialog(R.drawable.failure, "Finger Update Status", "Employee id not enrolled", false);
                                            }
                                        } else {
                                            int internalError = -111;
                                            if (morphoDevice != null) {
                                                internalError = morphoDevice.getInternalError();
                                            }
                                            showCustomAlertDialog(R.drawable.failure, "Finger Save Status", "Failed to insert finger data to morpho database\nError Reason:" + convertToInternationalMessage(ret, internalError), true);
                                        }
                                        break;
                                }
                            } else if (noOfFingers == 2) {
                                switch (strVerificationMode) {
                                    case "1:N":
                                        ret = updateTwoTemplatesToMorphoDB(templateList);
                                        if (ret == ErrorCodes.MORPHO_OK) {
                                            String empId = empFingerInfo.getEmpId();
                                            int empAutoId = dbComm.getAutoIdByEmpId(empId);
                                            if (empAutoId != -1) {
                                                int empFingerId = -1;
                                                empFingerId = dbComm.updateTwoTemplatesToSqliteDb(empAutoId);
                                                if (empFingerId != -1) {
                                                    showCustomAlertDialog(R.drawable.success, "Finger Update Status", "Finger Data Updated Successfully !", true);
                                                } else {
                                                    showCustomAlertDialog(R.drawable.failure, "Finger Update Status", "Failed to update finger data to finger table", true);
                                                }
                                            } else {
                                                showCustomAlertDialog(R.drawable.failure, "Finger Update Status", "Employee id not enrolled", false);
                                            }
                                        } else {
                                            int internalError = -111;
                                            if (morphoDevice != null) {
                                                internalError = morphoDevice.getInternalError();
                                            }
                                            showCustomAlertDialog(R.drawable.failure, "Finger Save Status", "Failed to insert finger data to morpho database\nError Reason:" + convertToInternationalMessage(ret, internalError), true);
                                        }
                                        break;
                                    default:
                                        ret = updateTwoTemplatesToMorphoDB(templateList);
                                        if (ret == ErrorCodes.MORPHO_OK) {
                                            String empId = empFingerInfo.getEmpId();
                                            int empAutoId = dbComm.getAutoIdByEmpId(empId);
                                            if (empAutoId != -1) {
                                                int empFingerId = -1;
                                                empFingerId = dbComm.updateTwoTemplatesToSqliteDb(empAutoId);
                                                if (empFingerId != -1) {
//                                                String role = UserDetails.getInstance().getRole();
//                                                int smartReader = Settings.getInstance().getSrTypeValue();
//                                                if (role.equals("Y") && (smartReader == 0 || smartReader == 1 || smartReader==2)) {//0=RC632,1=MicroSmartV2//2=RC522
//                                                    cardWriteConfirmDialog(R.drawable.success, "Finger Update Status", "Finger Data Updated Successfully ! Do You Want To Write Data Into Smart Card ?");
//                                                } else {
//                                                    showCustomAlertDialog(R.drawable.success, "Finger Update Status", "Finger Data Updated Successfully !", true);
//                                                }
                                                    showCustomAlertDialog(R.drawable.success, "Finger Update Status", "Finger Data Updated Successfully !", true);
                                                } else {
                                                    showCustomAlertDialog(R.drawable.failure, "Finger Update Status", "Failed to update finger data to finger table", true);
                                                }
                                            } else {
                                                showCustomAlertDialog(R.drawable.failure, "Finger Update Status", "Employee id not enrolled", false);
                                            }
                                        } else {
                                            int internalError = -111;
                                            if (morphoDevice != null) {
                                                internalError = morphoDevice.getInternalError();
                                            }
                                            showCustomAlertDialog(R.drawable.failure, "Finger Save Status", "Failed to insert finger data to morpho database\nError Reason:" + convertToInternationalMessage(ret, internalError), true);
                                        }
                                        break;
                                }
                            }
                        } else {
                            showCustomAlertDialog(R.drawable.failure, "Error", "Employee and finger details not found !", true);
                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        fingerUpdateAlert.dismiss();
                        Intent intent = new Intent(context, EmployeeFingerEnrollmentActivity.class);
                        context.startActivity(intent);

                    }
                });

        fingerUpdateAlert = builder.create();
        fingerUpdateAlert.setCanceledOnTouchOutside(false);
        fingerUpdateAlert.show();

    }

    //================================ card write dialog =====================================//

    private void cardWriteConfirmDialog(int iconId, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message).setIcon(iconId)
                .setCancelable(false).setTitle(title)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (context != null) {
                            Activity activity = (Activity) context;
                            if (activity != null && activity instanceof FingerEnrollUpdateDialogActivity) {
                                EmployeeFingerEnrollInfo info = EmployeeFingerEnrollInfo.getInstance();
                                Intent intent = new Intent(context, SmartCardActivity.class);
                                intent.putExtra("EID", info.getEmpId());
                                context.startActivity(intent);
                                activity.finish();
                            }
                        }
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (context != null) {
                    Activity activity = (Activity) context;
                    if (activity != null && activity instanceof FingerEnrollUpdateDialogActivity) {
                        Intent intent = new Intent(context, EmployeeFingerEnrollmentActivity.class);
                        context.startActivity(intent);
                        activity.finish();
                    }
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
        fingerEnrollAlert = builder.create();
        fingerEnrollAlert.setCanceledOnTouchOutside(false);
        fingerEnrollAlert.show();
    }

    //================================ View Identification Success dialog =====================================//

    private void showSuccessIdentificationCustomDialog(String strTitle) {

        successEmpDetailsDialog = new Dialog(context);
        successEmpDetailsDialog.setCanceledOnTouchOutside(false);
        successEmpDetailsDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        successEmpDetailsDialog.setContentView(R.layout.finger_success_custom_dialog);
        successEmpDetailsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView title = (TextView) successEmpDetailsDialog.findViewById(R.id.title);
        ImageView empPhoto = (ImageView) successEmpDetailsDialog.findViewById(R.id.U_image);
        TextView empId = (TextView) successEmpDetailsDialog.findViewById(R.id.EmployeeID);
        TextView empCardId = (TextView) successEmpDetailsDialog.findViewById(R.id.CardID);
        TextView empName = (TextView) successEmpDetailsDialog.findViewById(R.id.Name);
        TextView attendanceTime = (TextView) successEmpDetailsDialog.findViewById(R.id.AttendanceTime);
        title.setText(strTitle);

        int identifyVal = IdentificationInfo.getInstance().getIdentifyValue();
        if (identifyVal == 0) {
            String userId = IdentificationInfo.getInstance().getUserId();
            String cardId = IdentificationInfo.getInstance().getFirstName();
            SQLiteCommunicator dbComm = new SQLiteCommunicator();
            Cursor empData = dbComm.getEmpDetailsByEmpId(userId);
            if (empData != null) {
                if (empData.getCount() > 0) {
                    while (empData.moveToNext()) {
                        int insertStaus = -1;
                        Calendar calendar = Calendar.getInstance();
                        SimpleDateFormat mdformat = new SimpleDateFormat("ddMMyyyy HH:mm:ss"); //Modified By Sanjay Shyamal
                        String strDateTime = mdformat.format(calendar.getTime());
                        String strCardId = empData.getString(1).replaceAll("\\G0", " ").trim();
                        attendanceTime.setText(strDateTime);
                        empId.setText(userId.trim());
                        empCardId.setText(strCardId);
                        empName.setText(empData.getString(2));
                        byte[] byteImage = empData.getBlob(3);
                        if (byteImage != null && byteImage.length > 1) {
                            empPhoto.setImageBitmap(BitmapFactory.decodeByteArray(byteImage, 0, byteImage.length));
                        } else {
                            empPhoto.setImageResource(R.drawable.dummyphoto);
                        }

                        String strLatitude = "", strLongitude = "";
                        strLatitude = EmployeeAttendanceActivity.latitude;
                        strLongitude = EmployeeAttendanceActivity.longitude;

//                        Location mLastLocation = EmployeeAttendanceActivity.mLastLocation;
//                        if (mLastLocation != null) {
//                            strLatitude = Double.toString(mLastLocation.getLatitude());
//                            strLongitude = Double.toString(mLastLocation.getLongitude());
//                            getAddressByLatLong(mLastLocation.getLatitude(), mLastLocation.getLongitude());
//                        }

                        Activity activity = (Activity) context;
                        if (activity != null && activity instanceof EmployeeAttendanceActivity) {
                            TextView tvStateToggleButton = EmployeeAttendanceActivity.tvStateofToggleButton;
                            String strInOutModeText = tvStateToggleButton.getText().toString();
                            // String strInOutMode = Utility.getInOutValue(strInOutModeText);
                            String vm = "1:N";//1:N
                            insertStaus = dbComm.insertAttendanceData(userId, strCardId, strDateTime, strInOutModeText, vm, strLatitude, strLongitude, null);
                            if (insertStaus != -1) {
                                if (strInOutModeText.trim().equals("IN") || strInOutModeText.trim().equals("OUT")) {
                                    int appType = Settings.getInstance().getAppType();
                                    switch (appType) {
                                        case 0:
                                            //Toast.makeText(context, " " + strInOutModeText + " Time Captured Successfully", Toast.LENGTH_LONG).show();
                                            break;
                                        case 1:
                                            String[] sc_sn = subCode_subName.split(",");
                                            int status = dbComm.insertCollegeAttendanceData(profEID, profCID, userId, cardId, sc_sn[0], subType, "IN", strLatitude, strLongitude);
                                            if (status != -1) {
                                                int loginId = dbComm.getLastLoginStatusId();
                                                if (loginId != -1) {
                                                    int count = dbComm.getNoOfStudentsPunched(loginId);
                                                    if (count != -1) {
                                                        count++;
                                                        dbComm.updateNoOfStudentsPunched(loginId, count);
                                                    }
                                                }
                                            }
                                            break;
                                        default:
                                            //Toast.makeText(context, "Employee Attendance " + strInOutModeText + " Time Captured Successfully", Toast.LENGTH_LONG).show();
                                            break;
                                    }
                                } else {
                                    int appType = Settings.getInstance().getAppType();
                                    switch (appType) {
                                        case 0:
                                            //Toast.makeText(context, "Employee Attendance Captured Successfully", Toast.LENGTH_LONG).show();
                                            break;
                                        case 1:
                                            String[] sc_sn = subCode_subName.split(",");
                                            int status = dbComm.insertCollegeAttendanceData(profEID, profCID, userId, cardId, sc_sn[0], subType, "IN", strLatitude, strLongitude);
                                            if (status != -1) {
                                                int loginId = dbComm.getLastLoginStatusId();
                                                if (loginId != -1) {
                                                    int count = dbComm.getNoOfStudentsPunched(loginId);
                                                    if (count != -1) {
                                                        count++;
                                                        dbComm.updateNoOfStudentsPunched(loginId, count);
                                                    }
                                                }
                                            }
                                            break;
                                        default:
                                            //Toast.makeText(context, "Employee Attendance Captured Successfully", Toast.LENGTH_LONG).show();
                                            break;
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Attendance Capture Failure", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                    if (empData != null) {
                        empData.close();
                    }
                } else {
                    Toast.makeText(context, "Employee Data Not Found In Sqlite", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(context, "Employee Data Not Found In Sqlite", Toast.LENGTH_LONG).show();
            }
        }

        EmployeeAttendanceActivity.stopModeUpdate = false;

        successEmpDetailsDialog.show();

        WindowManager.LayoutParams lp = successEmpDetailsDialog.getWindow().getAttributes();
        lp.dimAmount = 0.9f;
        lp.buttonBrightness = 1.0f;
        lp.screenBrightness = 1.0f;
        successEmpDetailsDialog.getWindow().setAttributes(lp);
        successEmpDetailsDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }

    //================================ View Card Verification Success dialog =====================================//

    private void showSuccessCustomDialogForCardVerification(String strTitle, SmartCardInfo cardDetails) {

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("ddMMyyyy HH:mm:ss");
        String strDateTime = mdformat.format(calendar.getTime());

        successEmpDetailsDialog = new Dialog(context);
        successEmpDetailsDialog.setCanceledOnTouchOutside(true);
        successEmpDetailsDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        successEmpDetailsDialog.setContentView(R.layout.emp_details_verify_dialog);
        successEmpDetailsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView textViewEmpID = (TextView) successEmpDetailsDialog.findViewById(R.id.EmployeeID);
        TextView textViewCardId = (TextView) successEmpDetailsDialog.findViewById(R.id.CardID);
        TextView textViewName = (TextView) successEmpDetailsDialog.findViewById(R.id.Name);
        TextView textViewDOB = (TextView) successEmpDetailsDialog.findViewById(R.id.DOB);
        TextView textViewValidUpto = (TextView) successEmpDetailsDialog.findViewById(R.id.ValidUpto);
        TextView textViewSmartCardVer = (TextView) successEmpDetailsDialog.findViewById(R.id.SmartCardVer);
        TextView attendanceTime = (TextView) successEmpDetailsDialog.findViewById(R.id.AttendanceTime);
        ImageView empImage = (ImageView) successEmpDetailsDialog.findViewById(R.id.U_image);

        //  ImageView icon = (ImageView) successEmpDetailsDialog.findViewById(R.id.image);
        TextView title = (TextView) successEmpDetailsDialog.findViewById(R.id.title);

        //  icon.setImageResource(R.drawable.success);

        title.setText(strTitle);

        textViewEmpID.setText(cardDetails.getEmployeeId().trim());
        textViewCardId.setText(cardDetails.getCardId().replaceAll("\\G0", " ").trim());
        textViewName.setText(cardDetails.getEmpName().trim());
        textViewDOB.setText(cardDetails.getBirthDate().trim());
        textViewValidUpto.setText(cardDetails.getValidUpto().trim());
        textViewSmartCardVer.setText(cardDetails.getSmartCardVer().trim());
        attendanceTime.setText(strDateTime);
        empImage.setImageResource(R.drawable.dummyphoto);

        SQLiteCommunicator dbComm = new SQLiteCommunicator();
        Cursor empData = dbComm.getEmployeePhoto(cardDetails.getEmployeeId());
        if (empData != null) {
            if (empData.getCount() > 0) {
                while (empData.moveToNext()) {
                    int insertStaus = -1;
                    byte[] byteImage = empData.getBlob(0);
                    if (byteImage != null && byteImage.length > 1) {
                        empImage.setImageBitmap(BitmapFactory.decodeByteArray(byteImage, 0, byteImage.length));
                    } else {
                        empImage.setImageResource(R.drawable.dummyphoto);
                    }

//                    Location mLastLocation = EmployeeAttendanceActivity.mLastLocation;
//                    if (mLastLocation != null) {
//                        strLatitude = Double.toString(mLastLocation.getLatitude());
//                        strLongitude = Double.toString(mLastLocation.getLongitude());
//                    }

                    String strLatitude = EmployeeAttendanceActivity.latitude;
                    String strLongitude = EmployeeAttendanceActivity.longitude;


                    String employeeId = cardDetails.getEmployeeId();
                    String cardId = cardDetails.getCardId();
                    String vm = cardDetails.getFirstFingerVerificationMode();
                    TextView tvStateToggleButton = EmployeeAttendanceActivity.tvStateofToggleButton;
                    String strInOutModeText = tvStateToggleButton.getText().toString();
                    // String strInOutMode = Utility.getInOutValue(strInOutModeText);
                    insertStaus = dbComm.insertAttendanceData(employeeId, cardId, strDateTime, strInOutModeText, vm, strLatitude, strLongitude, null);
                    if (insertStaus != -1) {
                        if (strInOutModeText.trim().equals("IN") || strInOutModeText.trim().equals("OUT")) {
                            //Toast.makeText(context, "Employee Attendance " + strInOutModeText + " Time Captured Successfully", Toast.LENGTH_LONG).show();
                        } else {
                            // Toast.makeText(context, "Employee Attendance Captured Successfully", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(context, "Attendance Capture Failure", Toast.LENGTH_LONG).show();
                    }
                }
            }
            if (empData != null) {
                empData.close();
            }
        } else {
            int insertStaus = -1;

            //           Location mLastLocation = EmployeeAttendanceActivity.mLastLocation;
//            if (mLastLocation != null) {
//                strLatitude = Double.toString(mLastLocation.getLatitude());
//                strLongitude = Double.toString(mLastLocation.getLongitude());
//            }

            String strLatitude = EmployeeAttendanceActivity.latitude;
            String strLongitude = EmployeeAttendanceActivity.longitude;

            String employeeId = cardDetails.getEmployeeId();
            String cardId = cardDetails.getCardId();
            String vm = cardDetails.getFirstFingerVerificationMode();
            TextView tvStateToggleButton = EmployeeAttendanceActivity.tvStateofToggleButton;
            String strInOutModeText = tvStateToggleButton.getText().toString();
            // String strInOutMode = Utility.getInOutValue(strInOutModeText);
            insertStaus = dbComm.insertAttendanceData(employeeId, cardId, strDateTime, strInOutModeText, vm, strLatitude, strLongitude, null);
            if (insertStaus != -1) {
                if (strInOutModeText.trim().equals("IN") || strInOutModeText.trim().equals("OUT")) {
                    // Toast.makeText(context, "Employee Attendance " + strInOutModeText + " Time Captured Successfully", Toast.LENGTH_LONG).show();
                } else {
                    //  Toast.makeText(context, "Employee Attendance Captured Successfully", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(context, "Attendance Capture Failure", Toast.LENGTH_LONG).show();
            }
        }

        successEmpDetailsDialog.show();

        WindowManager.LayoutParams lp = successEmpDetailsDialog.getWindow().getAttributes();
        lp.dimAmount = 0.9f;
        lp.buttonBrightness = 1.0f;
        lp.screenBrightness = 1.0f;
        successEmpDetailsDialog.getWindow().setAttributes(lp);
        successEmpDetailsDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }

    //================================ View Local Database VerificationSuccess dialog =====================================//

    private void showSuccessCustomDialogForLocalDbVerification(String strTitle, EmployeeInfo info) {

        successEmpDetailsDialog = new Dialog(context);
        successEmpDetailsDialog.setCanceledOnTouchOutside(false);
        successEmpDetailsDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        successEmpDetailsDialog.setContentView(R.layout.finger_success_custom_dialog);
        successEmpDetailsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView title = (TextView) successEmpDetailsDialog.findViewById(R.id.title);
        ImageView empPhoto = (ImageView) successEmpDetailsDialog.findViewById(R.id.U_image);
        TextView empId = (TextView) successEmpDetailsDialog.findViewById(R.id.EmployeeID);
        TextView empCardId = (TextView) successEmpDetailsDialog.findViewById(R.id.CardID);
        TextView empName = (TextView) successEmpDetailsDialog.findViewById(R.id.Name);
        TextView attendanceTime = (TextView) successEmpDetailsDialog.findViewById(R.id.AttendanceTime);
        title.setText(strTitle);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("ddMMyyyy HH:mm:ss"); //Modified By Sanjay Shyamal
        String strDateTime = mdformat.format(calendar.getTime());
        attendanceTime.setText(strDateTime);
        empId.setText(info.getEmpId().trim());
        empCardId.setText(info.getCardId().replaceAll("\\G0", " ").trim());
        empName.setText(info.getEmpName());

        byte[] byteImage = info.getPhoto();
        if (byteImage != null && byteImage.length > 1) {
            empPhoto.setImageBitmap(BitmapFactory.decodeByteArray(byteImage, 0, byteImage.length));
        } else {
            empPhoto.setImageResource(R.drawable.dummyphoto);
        }

//        Location mLastLocation = EmployeeAttendanceActivity.mLastLocation;
//        if (mLastLocation != null) {
//            strLatitude = Double.toString(mLastLocation.getLatitude());
//            strLongitude = Double.toString(mLastLocation.getLongitude());
//        }

        String strLatitude = EmployeeAttendanceActivity.latitude;
        String strLongitude = EmployeeAttendanceActivity.longitude;

        Activity activity = (Activity) context;

        String employeeId = info.getEmpId();
        String cardId = info.getCardId();
        String vm = "LOCAL-DB";
        TextView tvStateToggleButton = EmployeeAttendanceActivity.tvStateofToggleButton;
        String strInOutModeText = tvStateToggleButton.getText().toString();
        //  String strInOutMode = Utility.getInOutValue(strInOutModeText);
        SQLiteCommunicator dbComm = new SQLiteCommunicator();
        int insertStaus = dbComm.insertAttendanceData(employeeId, cardId, strDateTime, strInOutModeText, vm, strLatitude, strLongitude, null);
        if (insertStaus != -1) {
            if (strInOutModeText.trim().equals("IN") || strInOutModeText.trim().equals("OUT")) {
                // Toast.makeText(context, "Employee Attendance " + strInOutModeText + " Time Captured Successfully", Toast.LENGTH_LONG).show();
            } else {
                // Toast.makeText(context, "Employee Attendance Captured Successfully", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(context, "Attendance Capture Failure", Toast.LENGTH_LONG).show();
        }

        successEmpDetailsDialog.show();

        WindowManager.LayoutParams lp = successEmpDetailsDialog.getWindow().getAttributes();
        lp.dimAmount = 0.9f;
        lp.buttonBrightness = 1.0f;
        lp.screenBrightness = 1.0f;
        successEmpDetailsDialog.getWindow().setAttributes(lp);
        successEmpDetailsDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

    }

    //================================ Card Pin dialog =====================================//

    private void showCardPinDialog(final SmartCardInfo cardDetails) {

        successEmpDetailsDialog = new Dialog(context);
        successEmpDetailsDialog.setCanceledOnTouchOutside(false);
        successEmpDetailsDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        successEmpDetailsDialog.setContentView(R.layout.card_pin_dialog);

        ImageView icon = (ImageView) successEmpDetailsDialog.findViewById(R.id.image);
        TextView title = (TextView) successEmpDetailsDialog.findViewById(R.id.title);

        final EditText pin = (EditText) successEmpDetailsDialog.findViewById(R.id.Pin);

        Button btn_Save = (Button) successEmpDetailsDialog.findViewById(R.id.btn_Save);
        Button btn_Cancel = (Button) successEmpDetailsDialog.findViewById(R.id.btn_Cancel);

        icon.setImageResource(R.drawable.success);
        title.setText("Card Pin Details");

        btn_Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                successEmpDetailsDialog.dismiss();
                String strCurrentPin = pin.getText().toString().trim();
                SQLiteCommunicator dbComm = new SQLiteCommunicator();
                int autoId = dbComm.isDataAvailableInDatabase(cardDetails.getEmployeeId());
                if (autoId != -1) {
                    // String strCardPin = dbComm.getCardPinForVerification(autoId);
                    String strCardPin = cardDetails.getPin();
                    if (strCardPin != null && strCardPin.trim().length() > 0 && strCurrentPin.equals(strCardPin)) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                ForlinxGPIO.runGPIOLEDForSuccess();
                            }
                        }).start();
                        notifyVerificationEndProcess();
                        showSuccessCustomDialogForCardVerification("Verification Success", cardDetails);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                successEmpDetailsDialog.cancel();
                            }

                        }, 4000);//4000
                    } else {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                ForlinxGPIO.runGPIOLEDForFailure();
                            }
                        }).start();
                        notifyVerificationEndProcess();
                        showFailureCustomDialog("Verification Failed", "Pin Mismatch");
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                failureEmpDetailsDialog.cancel();
                            }

                        }, 2000);
                    }
                } else {
                    notifyVerificationEndProcess();
                }
            }
        });

        btn_Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                successEmpDetailsDialog.dismiss();
                notifyVerificationEndProcess();
            }
        });


        successEmpDetailsDialog.show();

        WindowManager.LayoutParams lp = successEmpDetailsDialog.getWindow().getAttributes();
        lp.dimAmount = 0.9f;
        lp.buttonBrightness = 1.0f;
        lp.screenBrightness = 1.0f;
        successEmpDetailsDialog.getWindow().setAttributes(lp);
        successEmpDetailsDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }

    //================================ Failure dialog =====================================//

    private void showFailureCustomDialog(String strTitle, String strMessage) {

        failureEmpDetailsDialog = new Dialog(context);
        failureEmpDetailsDialog.setCanceledOnTouchOutside(false);
        failureEmpDetailsDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        failureEmpDetailsDialog.setContentView(R.layout.finger_failure_custom_dialog);
        failureEmpDetailsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // set the custom dialog components - text, image and button

        TextView title = (TextView) failureEmpDetailsDialog.findViewById(R.id.title);
        TextView message = (TextView) failureEmpDetailsDialog.findViewById(R.id.message);
        title.setText(strTitle);
        message.setText(strMessage);

        EmployeeAttendanceActivity.stopModeUpdate = false;

        failureEmpDetailsDialog.show();

        WindowManager.LayoutParams lp = failureEmpDetailsDialog.getWindow().getAttributes();
        lp.dimAmount = 0.9f;
        lp.buttonBrightness = 1.0f;
        lp.screenBrightness = 1.0f;
        failureEmpDetailsDialog.getWindow().setAttributes(lp);
        failureEmpDetailsDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }

    //================================ Alert dialog =====================================//

    private void showCustomAlertDialog(int iconId, final String strTitle, final String strMessage, final boolean isProcessFinished) {

        if (context != null) {
            final Activity activity = (Activity) context;
            if (activity instanceof EmployeeAttendanceActivity) {
                alertDialog = new Dialog(context);
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                alertDialog.setContentView(R.layout.custom_alert_dialog);
                ImageView icon = (ImageView) alertDialog.findViewById(R.id.image);
                TextView title = (TextView) alertDialog.findViewById(R.id.title);
                TextView message = (TextView) alertDialog.findViewById(R.id.message);
                Button btn_Ok = (Button) alertDialog.findViewById(R.id.btnOk);

                icon.setImageResource(iconId);

                title.setText(strTitle);
                message.setText(strMessage);

                btn_Ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                    }
                });

                alertDialog.show();

                WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                lp.dimAmount = 0.9f;
                lp.buttonBrightness = 1.0f;
                lp.screenBrightness = 1.0f;
                alertDialog.getWindow().setAttributes(lp);
                alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);


            } else if (activity instanceof FingerEnrollUpdateDialogActivity) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(strMessage).setIcon(iconId)
                        .setCancelable(false).setTitle(strTitle)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (isProcessFinished) {
                                    //EmployeeFingerEnrollInfo.getInstance().reset();//reset values
                                    Intent intent = new Intent(context, EmployeeFingerEnrollmentActivity.class);
                                    context.startActivity(intent);
                                    activity.finish();
                                }
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        }
    }

    //================================ Normal alert dialog =====================================//

    private void alert(int codeError, int internalError, String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(title);
        alertDialog.setCanceledOnTouchOutside(false);
        String msg;
        String errorInternationalization = convertToInternationalMessage(codeError, internalError);
        msg = "Operation Failed" + "\n" + errorInternationalization;
        msg += "\n" + message;
        alertDialog.setMessage(msg);
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (context != null) {
                    Activity activity = (Activity) context;
                    if (activity != null && activity instanceof FingerEnrollUpdateDialogActivity) {
                        Intent intent = new Intent(context, EmployeeFingerEnrollmentActivity.class);
                        context.startActivity(intent);
                        activity.finish();
                    }
                }
            }
        });
        alertDialog.show();
    }

    //======================== Morpho defined error codes ========================//

    @SuppressLint("DefaultLocale")
    private String convertToInternationalMessage(int iCodeError, int internalError) {
        switch (iCodeError) {
            case ErrorCodes.MORPHO_OK:
                return context.getString(R.string.MORPHO_OK);
            case ErrorCodes.MORPHOERR_INTERNAL:
                return context.getString(R.string.MORPHOERR_INTERNAL);
            case ErrorCodes.MORPHOERR_PROTOCOLE:
                return context.getString(R.string.MORPHOERR_PROTOCOLE);
            case ErrorCodes.MORPHOERR_CONNECT:
                return context.getString(R.string.MORPHOERR_CONNECT);
            case ErrorCodes.MORPHOERR_CLOSE_COM:
                return context.getString(R.string.MORPHOERR_CLOSE_COM);
            case ErrorCodes.MORPHOERR_BADPARAMETER:
                return context.getString(R.string.MORPHOERR_BADPARAMETER);
            case ErrorCodes.MORPHOERR_MEMORY_PC:
                return context.getString(R.string.MORPHOERR_MEMORY_PC);
            case ErrorCodes.MORPHOERR_MEMORY_DEVICE:
                return context.getString(R.string.MORPHOERR_MEMORY_DEVICE);
            case ErrorCodes.MORPHOERR_NO_HIT:
                return context.getString(R.string.MORPHOERR_NO_HIT);
            case ErrorCodes.MORPHOERR_STATUS:
                return context.getString(R.string.MORPHOERR_STATUS);
            case ErrorCodes.MORPHOERR_DB_FULL:
                return context.getString(R.string.MORPHOERR_DB_FULL);
            case ErrorCodes.MORPHOERR_DB_EMPTY:
                return context.getString(R.string.MORPHOERR_DB_EMPTY);
            case ErrorCodes.MORPHOERR_ALREADY_ENROLLED:
                return "Finger Template Already Exists";
            case ErrorCodes.MORPHOERR_BASE_NOT_FOUND:
                return context.getString(R.string.MORPHOERR_BASE_NOT_FOUND);
            case ErrorCodes.MORPHOERR_BASE_ALREADY_EXISTS:
                return context.getString(R.string.MORPHOERR_BASE_ALREADY_EXISTS);
            case ErrorCodes.MORPHOERR_NO_ASSOCIATED_DB:
                return context.getString(R.string.MORPHOERR_NO_ASSOCIATED_DB);
            case ErrorCodes.MORPHOERR_NO_ASSOCIATED_DEVICE:
                return context.getString(R.string.MORPHOERR_NO_ASSOCIATED_DEVICE);
            case ErrorCodes.MORPHOERR_INVALID_TEMPLATE:
                return context.getString(R.string.MORPHOERR_INVALID_TEMPLATE);
            case ErrorCodes.MORPHOERR_NOT_IMPLEMENTED:
                return context.getString(R.string.MORPHOERR_NOT_IMPLEMENTED);
            case ErrorCodes.MORPHOERR_TIMEOUT:
                return context.getString(R.string.MORPHOERR_TIMEOUT);
            case ErrorCodes.MORPHOERR_NO_REGISTERED_TEMPLATE:
                return context.getString(R.string.MORPHOERR_NO_REGISTERED_TEMPLATE);
            case ErrorCodes.MORPHOERR_FIELD_NOT_FOUND:
                return context.getString(R.string.MORPHOERR_FIELD_NOT_FOUND);
            case ErrorCodes.MORPHOERR_CORRUPTED_CLASS:
                return context.getString(R.string.MORPHOERR_CORRUPTED_CLASS);
            case ErrorCodes.MORPHOERR_TO_MANY_TEMPLATE:
                return context.getString(R.string.MORPHOERR_TO_MANY_TEMPLATE);
            case ErrorCodes.MORPHOERR_TO_MANY_FIELD:
                return context.getString(R.string.MORPHOERR_TO_MANY_FIELD);
            case ErrorCodes.MORPHOERR_MIXED_TEMPLATE:
                return context.getString(R.string.MORPHOERR_MIXED_TEMPLATE);
            case ErrorCodes.MORPHOERR_CMDE_ABORTED:
                return context.getString(R.string.MORPHOERR_CMDE_ABORTED);
            case ErrorCodes.MORPHOERR_INVALID_PK_FORMAT:
                return context.getString(R.string.MORPHOERR_INVALID_PK_FORMAT);
            case ErrorCodes.MORPHOERR_SAME_FINGER:
                return context.getString(R.string.MORPHOERR_SAME_FINGER);
            case ErrorCodes.MORPHOERR_OUT_OF_FIELD:
                return context.getString(R.string.MORPHOERR_OUT_OF_FIELD);
            case ErrorCodes.MORPHOERR_INVALID_USER_ID:
                return context.getString(R.string.MORPHOERR_INVALID_USER_ID);
            case ErrorCodes.MORPHOERR_INVALID_USER_DATA:
                return context.getString(R.string.MORPHOERR_INVALID_USER_DATA);
            case ErrorCodes.MORPHOERR_FIELD_INVALID:
                return context.getString(R.string.MORPHOERR_FIELD_INVALID);
            case ErrorCodes.MORPHOERR_USER_NOT_FOUND:
                return context.getString(R.string.MORPHOERR_USER_NOT_FOUND);
            case ErrorCodes.MORPHOERR_COM_NOT_OPEN:
                return context.getString(R.string.MORPHOERR_COM_NOT_OPEN);
            case ErrorCodes.MORPHOERR_ELT_ALREADY_PRESENT:
                return context.getString(R.string.MORPHOERR_ELT_ALREADY_PRESENT);
            case ErrorCodes.MORPHOERR_NOCALLTO_DBQUERRYFIRST:
                return context.getString(R.string.MORPHOERR_NOCALLTO_DBQUERRYFIRST);
            case ErrorCodes.MORPHOERR_USER:
                return context.getString(R.string.MORPHOERR_USER);
            case ErrorCodes.MORPHOERR_BAD_COMPRESSION:
                return context.getString(R.string.MORPHOERR_BAD_COMPRESSION);
            case ErrorCodes.MORPHOERR_SECU:
                return context.getString(R.string.MORPHOERR_SECU);
            case ErrorCodes.MORPHOERR_CERTIF_UNKNOW:
                return context.getString(R.string.MORPHOERR_CERTIF_UNKNOW);
            case ErrorCodes.MORPHOERR_INVALID_CLASS:
                return context.getString(R.string.MORPHOERR_INVALID_CLASS);
            case ErrorCodes.MORPHOERR_USB_DEVICE_NAME_UNKNOWN:
                return context.getString(R.string.MORPHOERR_USB_DEVICE_NAME_UNKNOWN);
            case ErrorCodes.MORPHOERR_CERTIF_INVALID:
                return context.getString(R.string.MORPHOERR_CERTIF_INVALID);
            case ErrorCodes.MORPHOERR_SIGNER_ID:
                return context.getString(R.string.MORPHOERR_SIGNER_ID);
            case ErrorCodes.MORPHOERR_SIGNER_ID_INVALID:
                return context.getString(R.string.MORPHOERR_SIGNER_ID_INVALID);
            case ErrorCodes.MORPHOERR_FFD:
                return context.getString(R.string.MORPHOERR_FFD);
            case ErrorCodes.MORPHOERR_MOIST_FINGER:
                return context.getString(R.string.MORPHOERR_MOIST_FINGER);
            case ErrorCodes.MORPHOERR_NO_SERVER:
                return context.getString(R.string.MORPHOERR_NO_SERVER);
            case ErrorCodes.MORPHOERR_OTP_NOT_INITIALIZED:
                return context.getString(R.string.MORPHOERR_OTP_NOT_INITIALIZED);
            case ErrorCodes.MORPHOERR_OTP_PIN_NEEDED:
                return context.getString(R.string.MORPHOERR_OTP_PIN_NEEDED);
            case ErrorCodes.MORPHOERR_OTP_REENROLL_NOT_ALLOWED:
                return context.getString(R.string.MORPHOERR_OTP_REENROLL_NOT_ALLOWED);
            case ErrorCodes.MORPHOERR_OTP_ENROLL_FAILED:
                return context.getString(R.string.MORPHOERR_OTP_ENROLL_FAILED);
            case ErrorCodes.MORPHOERR_OTP_IDENT_FAILED:
                return context.getString(R.string.MORPHOERR_OTP_IDENT_FAILED);
            case ErrorCodes.MORPHOERR_NO_MORE_OTP:
                return context.getString(R.string.MORPHOERR_NO_MORE_OTP);
            case ErrorCodes.MORPHOERR_OTP_NO_HIT:
                return context.getString(R.string.MORPHOERR_OTP_NO_HIT);
            case ErrorCodes.MORPHOERR_OTP_ENROLL_NEEDED:
                return context.getString(R.string.MORPHOERR_OTP_ENROLL_NEEDED);
            case ErrorCodes.MORPHOERR_DEVICE_LOCKED:
                return context.getString(R.string.MORPHOERR_DEVICE_LOCKED);
            case ErrorCodes.MORPHOERR_DEVICE_NOT_LOCK:
                return context.getString(R.string.MORPHOERR_DEVICE_NOT_LOCK);
            case ErrorCodes.MORPHOERR_OTP_LOCK_GEN_OTP:
                return context.getString(R.string.MORPHOERR_OTP_LOCK_GEN_OTP);
            case ErrorCodes.MORPHOERR_OTP_LOCK_SET_PARAM:
                return context.getString(R.string.MORPHOERR_OTP_LOCK_SET_PARAM);
            case ErrorCodes.MORPHOERR_OTP_LOCK_ENROLL:
                return context.getString(R.string.MORPHOERR_OTP_LOCK_ENROLL);
            case ErrorCodes.MORPHOERR_FVP_MINUTIAE_SECURITY_MISMATCH:
                return context.getString(R.string.MORPHOERR_FVP_MINUTIAE_SECURITY_MISMATCH);
            case ErrorCodes.MORPHOERR_FVP_FINGER_MISPLACED_OR_WITHDRAWN:
                return context.getString(R.string.MORPHOERR_FVP_FINGER_MISPLACED_OR_WITHDRAWN);
            case ErrorCodes.MORPHOERR_LICENSE_MISSING:
                return context.getString(R.string.MORPHOERR_LICENSE_MISSING);
            case ErrorCodes.MORPHOERR_CANT_GRAN_PERMISSION_USB:
                return context.getString(R.string.MORPHOERR_CANT_GRAN_PERMISSION_USB);
            case -111:
                return "";
            default:
                return String.format("Unknown error %d, Internal Error = %d", iCodeError, internalError);
        }
    }

    //=============================== It creates morpho usb connection ============================//

    public static boolean getMorphoUSBConnection(UsbDeviceConnection connection, UsbDevice morphoUsbDevice, MorphoDevice morphoDevice) {
        int ret = -1;
        int sensorBus = -1;
        int sensorAddress = -1;
        int sensorFileDescriptor = -1;
        sensorFileDescriptor = connection.getFileDescriptor();
        String name = morphoUsbDevice.getDeviceName();
        String[] elts = name.split("/");
        if (elts.length >= 5) {
            sensorBus = Integer.parseInt(elts[4].toString());
            sensorAddress = Integer.parseInt(elts[5].toString());
        }
        if (sensorBus > 0 && sensorAddress > 0 && sensorFileDescriptor > 0) {
            ProcessInfo.getInstance().setSensorBus(sensorBus);
            ProcessInfo.getInstance().setSensorAddress(sensorAddress);
            ProcessInfo.getInstance().setSensorFileDescriptor(sensorFileDescriptor);
            ret = morphoDevice.openUsbDeviceFD(sensorBus, sensorAddress, sensorFileDescriptor, 0);
        }
        return ret == 0;
    }

    //==================== It fetches morpho database ================//

    public static boolean getMorphoDataBaseConnection(MorphoDevice morphoDevice, MorphoDatabase morphoDatabase) {
        boolean isMorphoDatabaseInitSucc = false;
        int ret = morphoDevice.getDatabase(0, morphoDatabase);
        if (ret != ErrorCodes.MORPHO_OK) {
            if (ret == ErrorCodes.MORPHOERR_BASE_NOT_FOUND) {
                ret = createMorphoDataBase(morphoDatabase);
                isMorphoDatabaseInitSucc = ret == ErrorCodes.MORPHO_OK;
            }
        } else {
            isMorphoDatabaseInitSucc = true;
        }
        return isMorphoDatabaseInitSucc;
    }

    //==================== It creates morpho database ================//

    public static int createMorphoDataBase(MorphoDatabase morphoDatabase) {
        int ret = -1;
        Integer index = new Integer(0);
        MorphoField morphoFieldFirstName = new MorphoField();
        morphoFieldFirstName.setName(Constants.MORPHO_DATABASE_FIRST_FIELD_NAME);
        morphoFieldFirstName.setMaxSize(Constants.MORPHO_FIELD_MAX_SIZE);
        morphoFieldFirstName.setFieldAttribute(FieldAttribute.MORPHO_PUBLIC_FIELD);
        morphoDatabase.putField(morphoFieldFirstName, index);
        MorphoField morphoFieldLastName = new MorphoField();
        morphoFieldLastName.setName(Constants.MORPHO_DATABASE_LAST_FIELD_NAME);
        morphoFieldLastName.setMaxSize(Constants.MORPHO_FIELD_MAX_SIZE);
        morphoFieldLastName.setFieldAttribute(FieldAttribute.MORPHO_PUBLIC_FIELD);
        morphoDatabase.putField(morphoFieldLastName, index);
        ret = morphoDatabase.dbCreate(Constants.MORPHO_MAX_RECORD, Constants.MORPHO_MAX_FINGER, TemplateType.MORPHO_PK_COMP, 0, Constants.MORPHO_ENCRYPT_DATABASE);
        return ret;
    }

    //==================== It deletes morpho record by empid ================//

    public static int deleteMorphoUser(String empId) {
        int ret = -1;
        MorphoDatabase morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
        if (morphoDatabase != null) {
            MorphoUser user = new MorphoUser();
            ret = morphoDatabase.getUser(empId, user);
            ret = user.dbDelete();
        }
        return ret;
    }


    //==================== It deletes records of morpho database ================//

    public static int deleteMorphoDatabase() {
        int ret = -1;
        MorphoDatabase morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
        if (morphoDatabase != null) {
            ret = morphoDatabase.dbDelete(MorphoTypeDeletion.MORPHO_ERASE_BASE);
        }
        return ret;
    }

    //==================== It deletes morpho database ================//

    public static int destroyMorphoDatabase() {
        int ret = -1;
        MorphoDatabase morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
        if (morphoDatabase != null) {
            ret = morphoDatabase.dbDelete(MorphoTypeDeletion.MORPHO_DESTROY_BASE);
        }
        return ret;
    }

    //==================== Insert One Template To Morpho Database =====================//

    private int insertOneTemplateToMorphoDB(TemplateList templatelist) {
        int ret = -111;
        String empId = "";
        String cardId = "";
        String empName = "";
        EmployeeFingerEnrollInfo empFingerInfo = EmployeeFingerEnrollInfo.getInstance();
        if (empFingerInfo != null) {
            empId = empFingerInfo.getEmpId();
            cardId = empFingerInfo.getCardId();
            empName = empFingerInfo.getEmpName();
            if (empId.trim().length() == 0 || cardId.trim().length() == 0 || empName.trim().length() == 0) {
                return ret;
            }
        } else {
            return ret;
        }
        String strFirstFingerIndex = empFingerInfo.getStrFirstFingerIndex();
        String strSecondFingerIndex = empFingerInfo.getStrSecondFingerIndex();
        MorphoDatabase morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
        MorphoUser newMorphoUser = null;
        if (strFirstFingerIndex != null && strFirstFingerIndex.trim().length() > 0) {
            map.clear();
            Template firstTemplate = new Template();
            firstTemplate = templatelist.getTemplate(0);
            newMorphoUser = new MorphoUser();
            ret = morphoDatabase.getUser(empId, newMorphoUser);
            if (ret == ErrorCodes.MORPHO_OK) {
                ret = newMorphoUser.dbDelete();
                if (ret == ErrorCodes.MORPHO_OK || ret == -33) {//User Not Found
                    newMorphoUser = new MorphoUser();
                    ret = morphoDatabase.getUser(empId, newMorphoUser);
                    if (ret == ErrorCodes.MORPHO_OK) {
                        Integer i = new Integer(0);
                        ret = newMorphoUser.putTemplate(firstTemplate, i);
                        if (ret == ErrorCodes.MORPHO_OK) {
                            ret = newMorphoUser.putField(1, MorphoTools.checkfield(cardId, false));
                            if (ret == ErrorCodes.MORPHO_OK) {
                                ret = newMorphoUser.putField(2, MorphoTools.checkfield(empName, false));
                            }
                            if (ret == ErrorCodes.MORPHO_OK) {
                                ret = newMorphoUser.setNoCheckOnTemplateForDBStore(true);
                            }
                            if (ret == ErrorCodes.MORPHO_OK) {
                                ret = newMorphoUser.dbStore();
                                map.put(empId, firstTemplate);
                            }
                        }
                    }
                }
            }
        }
        if (strSecondFingerIndex != null && strSecondFingerIndex.trim().length() > 0) {
            Template firstTemplate = map.get(empId);
            if (firstTemplate != null) {
                int len = firstTemplate.getData().length;
                if (len > 0) {
                    newMorphoUser = new MorphoUser();
                    ret = morphoDatabase.getUser(empId, newMorphoUser);
                    if (ret == 0) {
                        ret = newMorphoUser.dbDelete();
                        if (ret == ErrorCodes.MORPHO_OK || ret == -33) {
                            newMorphoUser = new MorphoUser();
                            ret = morphoDatabase.getUser(empId, newMorphoUser);
                            if (ret == ErrorCodes.MORPHO_OK) {
                                Integer i = new Integer(0);
                                ret = newMorphoUser.putTemplate(firstTemplate, i);
                                if (ret == ErrorCodes.MORPHO_OK) {
                                    Integer j = new Integer(1);
                                    Template secondTemplate = templatelist.getTemplate(0);
                                    ret = newMorphoUser.putTemplate(secondTemplate, j);
                                    if (ret == ErrorCodes.MORPHO_OK) {
                                        ret = newMorphoUser.putField(1, MorphoTools.checkfield(cardId, false));
                                        if (ret == ErrorCodes.MORPHO_OK) {
                                            ret = newMorphoUser.putField(2, MorphoTools.checkfield(empName, false));
                                        }
                                        if (ret == ErrorCodes.MORPHO_OK) {
                                            ret = newMorphoUser.setNoCheckOnTemplateForDBStore(true);
                                        }
                                        if (ret == ErrorCodes.MORPHO_OK) {
                                            ret = newMorphoUser.dbStore();
                                            map.clear();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return ret;
    }

    public int insertOneRemoteTemplateToMorphoDB(TemplateList templatelist, EmployeeValidationBasicInfo info, EmployeeValidationFingerInfo fInfo) {
        int ret = -111;
        String empId = "";
        String cardId = "";
        String empName = "";
        if (info != null) {
            empId = info.getEmpId().trim();
            cardId = info.getCardId().replaceAll("\\G0", " ").trim();
            empName = info.getEmpName().trim();
            if (empId.trim().length() == 0 || cardId.trim().length() == 0 || empName.trim().length() == 0) {
                return ret;
            }
        } else {
            return ret;
        }
        MorphoDatabase morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
        MorphoUser newMorphoUser = null;
        String fingerType = fInfo.getFt();
        if (fingerType != null && fingerType.trim().length() > 0 && fingerType.equals("F1")) {
            map.clear();
            Template firstTemplate = new Template();
            firstTemplate = templatelist.getTemplate(0);
            newMorphoUser = new MorphoUser();
            ret = morphoDatabase.getUser(empId, newMorphoUser);
            if (ret == ErrorCodes.MORPHO_OK) {
                ret = newMorphoUser.dbDelete();
                if (ret == ErrorCodes.MORPHO_OK || ret == -33) {//User Not Found
                    newMorphoUser = new MorphoUser();
                    ret = morphoDatabase.getUser(empId, newMorphoUser);
                    if (ret == ErrorCodes.MORPHO_OK) {
                        Integer i = new Integer(0);
                        ret = newMorphoUser.putTemplate(firstTemplate, i);
                        if (ret == ErrorCodes.MORPHO_OK) {
                            ret = newMorphoUser.putField(1, MorphoTools.checkfield(cardId, false));
                            if (ret == ErrorCodes.MORPHO_OK) {
                                ret = newMorphoUser.putField(2, MorphoTools.checkfield(empName, false));
                            }
                            if (ret == ErrorCodes.MORPHO_OK) {
                                ret = newMorphoUser.setNoCheckOnTemplateForDBStore(true);
                            }
                            if (ret == ErrorCodes.MORPHO_OK) {
                                ret = newMorphoUser.dbStore();
                                map.put(empId, firstTemplate);
                            }
                        }
                    }
                }
            }
        }
        if (fingerType != null && fingerType.trim().length() > 0 && fingerType.equals("F2")) {
            Template firstTemplate = map.get(empId);
            if (firstTemplate != null) {
                int len = firstTemplate.getData().length;
                if (len > 0) {
                    newMorphoUser = new MorphoUser();
                    ret = morphoDatabase.getUser(empId, newMorphoUser);
                    if (ret == 0) {
                        ret = newMorphoUser.dbDelete();
                        if (ret == ErrorCodes.MORPHO_OK || ret == -33) {
                            newMorphoUser = new MorphoUser();
                            ret = morphoDatabase.getUser(empId, newMorphoUser);
                            if (ret == ErrorCodes.MORPHO_OK) {
                                Integer i = new Integer(0);
                                ret = newMorphoUser.putTemplate(firstTemplate, i);
                                if (ret == ErrorCodes.MORPHO_OK) {
                                    Integer j = new Integer(1);
                                    Template secondTemplate = templatelist.getTemplate(0);
                                    ret = newMorphoUser.putTemplate(secondTemplate, j);
                                    if (ret == ErrorCodes.MORPHO_OK) {
                                        ret = newMorphoUser.putField(1, MorphoTools.checkfield(cardId, false));
                                        if (ret == ErrorCodes.MORPHO_OK) {
                                            ret = newMorphoUser.putField(2, MorphoTools.checkfield(empName, false));
                                        }
                                        if (ret == ErrorCodes.MORPHO_OK) {
                                            ret = newMorphoUser.setNoCheckOnTemplateForDBStore(true);
                                        }
                                        if (ret == ErrorCodes.MORPHO_OK) {
                                            ret = newMorphoUser.dbStore();
                                            map.clear();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return ret;
    }


    public int insertOneRemoteTemplateToMorphoDB(TemplateList templatelist, TemplateDownloadInfo templateDownloadInfo) {
        int ret = -111;
        String empId = "";
        String cardId = "";
        String empName = "";
        if (templateDownloadInfo != null) {
            empId = templateDownloadInfo.getEmpId().trim();
            cardId = templateDownloadInfo.getCardId().replaceAll("\\G0", " ").trim();
            empName = templateDownloadInfo.getEmpName().trim();
            if (empId.trim().length() == 0 || cardId.trim().length() == 0 || empName.trim().length() == 0) {
                return ret;
            }
        } else {
            return ret;
        }
        MorphoDatabase morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
        MorphoUser newMorphoUser = null;
        String fingerType = templateDownloadInfo.getFingerType();
        if (fingerType != null && fingerType.trim().length() > 0 && fingerType.equals("F1")) {
            map.clear();
            Template firstTemplate = new Template();
            firstTemplate = templatelist.getTemplate(0);
            newMorphoUser = new MorphoUser();
            ret = morphoDatabase.getUser(empId, newMorphoUser);
            if (ret == ErrorCodes.MORPHO_OK) {
                ret = newMorphoUser.dbDelete();
                if (ret == ErrorCodes.MORPHO_OK || ret == -33) {//User Not Found
                    newMorphoUser = new MorphoUser();
                    ret = morphoDatabase.getUser(empId, newMorphoUser);
                    if (ret == ErrorCodes.MORPHO_OK) {
                        Integer i = new Integer(0);
                        ret = newMorphoUser.putTemplate(firstTemplate, i);
                        if (ret == ErrorCodes.MORPHO_OK) {
                            ret = newMorphoUser.putField(1, MorphoTools.checkfield(cardId, false));
                            if (ret == ErrorCodes.MORPHO_OK) {
                                ret = newMorphoUser.putField(2, MorphoTools.checkfield(empName, false));
                            }
                            if (ret == ErrorCodes.MORPHO_OK) {
                                ret = newMorphoUser.setNoCheckOnTemplateForDBStore(true);
                            }
                            if (ret == ErrorCodes.MORPHO_OK) {
                                ret = newMorphoUser.dbStore();
                                map.put(empId, firstTemplate);
                            }
                        }
                    }
                }
            }
        }
        if (fingerType != null && fingerType.trim().length() > 0 && fingerType.equals("F2")) {
            Template firstTemplate = map.get(empId);
            if (firstTemplate != null) {
                int len = firstTemplate.getData().length;
                if (len > 0) {
                    newMorphoUser = new MorphoUser();
                    ret = morphoDatabase.getUser(empId, newMorphoUser);
                    if (ret == 0) {
                        ret = newMorphoUser.dbDelete();
                        if (ret == ErrorCodes.MORPHO_OK || ret == -33) {
                            newMorphoUser = new MorphoUser();
                            ret = morphoDatabase.getUser(empId, newMorphoUser);
                            if (ret == ErrorCodes.MORPHO_OK) {
                                Integer i = new Integer(0);
                                ret = newMorphoUser.putTemplate(firstTemplate, i);
                                if (ret == ErrorCodes.MORPHO_OK) {
                                    Integer j = new Integer(1);
                                    Template secondTemplate = templatelist.getTemplate(0);
                                    ret = newMorphoUser.putTemplate(secondTemplate, j);
                                    if (ret == ErrorCodes.MORPHO_OK) {
                                        ret = newMorphoUser.putField(1, MorphoTools.checkfield(cardId, false));
                                        if (ret == ErrorCodes.MORPHO_OK) {
                                            ret = newMorphoUser.putField(2, MorphoTools.checkfield(empName, false));
                                        }
                                        if (ret == ErrorCodes.MORPHO_OK) {
                                            ret = newMorphoUser.setNoCheckOnTemplateForDBStore(true);
                                        }
                                        if (ret == ErrorCodes.MORPHO_OK) {
                                            ret = newMorphoUser.dbStore();
                                            map.clear();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return ret;
    }


    //==================== Insert Two Templates To Morpho Database =====================//

    private int insertTwoTemplatesToMorphoDB(TemplateList templateList) {
        int ret = -111;
        String empId = "";
        String cardId = "";
        String empName = "";
        EmployeeFingerEnrollInfo empFingerInfo = EmployeeFingerEnrollInfo.getInstance();
        if (empFingerInfo != null) {
            empId = empFingerInfo.getEmpId();
            cardId = empFingerInfo.getCardId();
            empName = empFingerInfo.getEmpName();
            if (empId.trim().length() == 0 || cardId.trim().length() == 0 || empName.trim().length() == 0) {
                return ret;
            }
        } else {
            return ret;
        }
        MorphoDatabase morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
        MorphoUser newMorphoUser = new MorphoUser();
        ret = morphoDatabase.getUser(empId, newMorphoUser);
        if (ret == ErrorCodes.MORPHO_OK) {
            Integer i = new Integer(0);
            ret = newMorphoUser.putTemplate(templateList.getTemplate(0), i);
            if (ret == ErrorCodes.MORPHO_OK) {
                Integer j = new Integer(1);
                ret = newMorphoUser.putTemplate(templateList.getTemplate(1), j);
            }
        }
        if (ret == ErrorCodes.MORPHO_OK) {
            ret = newMorphoUser.putField(1, MorphoTools.checkfield(cardId, false));
        }
        if (ret == ErrorCodes.MORPHO_OK) {
            ret = newMorphoUser.putField(2, MorphoTools.checkfield(empName, false));
        }
        if (ret == ErrorCodes.MORPHO_OK) {
            ret = newMorphoUser.setNoCheckOnTemplateForDBStore(true);
        }
        if (ret == ErrorCodes.MORPHO_OK) {
            ret = newMorphoUser.dbStore();
        }
        return ret;
    }

    //=================== Update One Template to Morpho Database ================//

    private int updateOneTemplateToMorphoDB(TemplateList templateList) {
        int ret = -111;
        String empId = "";
        String cardId = "";
        String empName = "";
        EmployeeFingerEnrollInfo empFingerInfo = EmployeeFingerEnrollInfo.getInstance();
        if (empFingerInfo != null) {
            empId = empFingerInfo.getEmpId();
            cardId = empFingerInfo.getCardId();
            empName = empFingerInfo.getEmpName();
            if (empId.trim().length() == 0 || cardId.trim().length() == 0 || empName.trim().length() == 0) {
                return ret;
            }
        } else {
            return ret;
        }
        MorphoDatabase morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
        MorphoUser newMorphoUser = new MorphoUser();
        ret = morphoDatabase.getUser(empId, newMorphoUser);
        if (ret == ErrorCodes.MORPHO_OK) {
            Integer i = new Integer(0);
            ret = newMorphoUser.putTemplate(templateList.getTemplate(0), i);
        }
        if (ret == ErrorCodes.MORPHO_OK) {
            ret = newMorphoUser.putField(1, MorphoTools.checkfield(cardId, true));
        }
        if (ret == ErrorCodes.MORPHO_OK) {
            ret = newMorphoUser.putField(2, MorphoTools.checkfield(empName, true));
        }
        if (ret == ErrorCodes.MORPHO_OK) {
            ret = newMorphoUser.setNoCheckOnTemplateForDBStore(true);
        }
        // Prepare finger update if necessary
        int index = empFingerInfo.getFingerIndex();
        boolean isUpdateTemplate = empFingerInfo.isUpdateTemplate();
        if (isUpdateTemplate) {
            if (index == 1) {// Update first finger index only
                boolean[] mask = {true};
                ret = newMorphoUser.setTemplateUpdateMask(mask);
            } else if (index == 2) {// Update second finger index
                boolean[] mask = {false, true};
                ret = newMorphoUser.setTemplateUpdateMask(mask);
            }
        }
        if (ret == ErrorCodes.MORPHO_OK) {
            ret = newMorphoUser.dbStore();
        }
        return ret;
    }


    //=================== Update Two Templates to Morpho Database ================//

    private int updateTwoTemplatesToMorphoDB(TemplateList templateList) {
        int ret = -111;
        String empId = "";
        String cardId = "";
        String empName = "";
        EmployeeFingerEnrollInfo empFingerInfo = EmployeeFingerEnrollInfo.getInstance();
        if (empFingerInfo != null) {
            empId = empFingerInfo.getEmpId();
            cardId = empFingerInfo.getCardId();
            empName = empFingerInfo.getEmpName();
            if (empId.trim().length() == 0 || cardId.trim().length() == 0 || empName.trim().length() == 0) {
                return ret;
            }
        } else {
            return ret;
        }
        MorphoDatabase morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
        MorphoUser newMorphoUser = new MorphoUser();
        ret = morphoDatabase.getUser(empId, newMorphoUser);
        if (ret == ErrorCodes.MORPHO_OK) {
            Integer i = new Integer(0);
            ret = newMorphoUser.putTemplate(templateList.getTemplate(0), i);
            if (ret == ErrorCodes.MORPHO_OK) {
                Integer j = new Integer(1);
                ret = newMorphoUser.putTemplate(templateList.getTemplate(1), j);
            }
        }
        if (ret == ErrorCodes.MORPHO_OK) {
            ret = newMorphoUser.putField(1, MorphoTools.checkfield(cardId, true));
        }
        if (ret == ErrorCodes.MORPHO_OK) {
            ret = newMorphoUser.putField(2, MorphoTools.checkfield(empName, true));
        }
        if (ret == ErrorCodes.MORPHO_OK) {
            ret = newMorphoUser.setNoCheckOnTemplateForDBStore(true);
        }
        boolean isUpdateTemplate = empFingerInfo.isUpdateTemplate();
        if (isUpdateTemplate) {
            int fingerIndex = empFingerInfo.getFingerIndex();
            if (fingerIndex == 3) {// Update both finger only
                boolean[] mask = {true, true};
                ret = newMorphoUser.setTemplateUpdateMask(mask);
            }
        }
        if (ret == ErrorCodes.MORPHO_OK) {
            ret = newMorphoUser.dbStore();
        }
        return ret;
    }


    public void referenceMethod() {

//        MorphoDevice morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
//        MorphoDatabase morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
//
//        if (morphoDevice != null && morphoDatabase != null) {
//            int ret = -1;
//            ret=morphoDatabase.dbDelete(MorphoTypeDeletion.MORPHO_ERASE_BASE);
//            Long l = new Long(10);
//            Integer i = new Integer(0);
//            ret = morphoDatabase.getNbUsedRecord(l);//12
//            ret = morphoDatabase.getNbTotalRecord(l);//500
//            ret = morphoDatabase.getNbFreeRecord(l);//488
//            ret = morphoDatabase.getNbFinger(i);//2
//            //TE29556
//            String empId = "TE29556";//Right Left Index
//            empId = "FIPL345";//Right Left Index
//            ret = deleteEnrolledMorphoUser(empId);
//            if (ret == 0) {
//                ret = dbLayer.getAutoIdByEmpId(empId);
//                if (ret != -1) {
//                    boolean status = false;
//                    status = dbLayer.deleteEmployeeDetails(ret);
//                    if (status) {
//                        Log.d("TEST", "Morpho Local Delete Status:" + status);
//                    } else {
//                        Log.d("TEST", "Morpho Local Delete Status:" + status);
//                    }
//                }
//            }

//            empId = "TE156";
//
//            ret = deleteEnrolledMorphoUser(empId);
//            if (ret == 0) {
//                ret = dbLayer.getAutoIdByEmpId(empId);
//                if (ret != -1) {
//                    boolean status = false;
//                    status = dbLayer.deleteEmployeeDetails(ret);
//                    if (status) {
//                        Log.d("TEST", "Morpho Local Delete Status:" + status);
//                    } else {
//                        Log.d("TEST", "Morpho Local Delete Status:" + status);
//                    }
//                }
//            }
//        }
    }


    private MorphoInfo retrieveIdentifySettings() {
        return IdentifyInfo.getInstance();
    }

    private MorphoInfo retrieveVerifySettings() {
        return VerifyInfo.getInstance();
    }

    private EmployeeFingerEnrollInfo retreiveEnrollSettings() {
        return EmployeeFingerEnrollInfo.getInstance();
    }

    private int getResIdFromContext(String resName) {
        int resID = this.context.getResources().getIdentifier(resName,
                "id", context.getPackageName());
        return resID;
    }

    private String getAddressByLatLong(double latitude, double longitude) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List <Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && addresses.size() > 0) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");
                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                Log.d("TEST", "My Current loction address" + strReturnedAddress.toString());
            } else {
                Log.d("TEST", "No address");
            }
        } catch (Exception e) {
            Log.d("TEST", "Cannot Get Address:Reason" + e.getMessage());
        }
        return strAdd;
    }

    public void reboot() {
        morphoDevice.rebootSoft(0, this);
    }

    public class AsyncTaskPostUnfinishedJob extends AsyncTask <String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String result = "";
            String response = "";
            String url = params[0].trim();
            String reqJson = params[1].trim();
            try {
                response = httpPostRequest(url, reqJson);
                if (response.trim().length() > 0) {
                    JSONObject reader = new JSONObject(response);
                    result = reader.get("Result").toString();
                    String message = reader.get("Message").toString();
                    String extraInfo = reader.get("ExtraInfo").toString();
                }
            } catch (JSONException e) {
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {

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
    }
}


//    String finalJson = JSONCreatorParser.createCollegeAttendanceJson(profEID, profCID, "", "", sc_sn[0].trim(), subType, "IN", EmployeeAttendanceActivity.latitude, EmployeeAttendanceActivity.longitude);
//    MqttAndroidClient client = MqttClientInfo.getInstance().getMqttAndroidClient();
//                if (client != null && client.isConnected()) {
//                        String subTopic = "ATTENDANCE/SAMSUNG/VIVO/V6/DTS/";
//                        MqttMessage msg = new MqttMessage();
//                        msg.setPayload(finalJson.getBytes());
//                        MqttApi api = new MqttApi(context);
//                        try {
//                        api.publish(client, subTopic, msg);
//                        } catch (MqttException e) {
//                        e.printStackTrace();
//                        } catch (UnsupportedEncodingException e) {
//                        e.printStackTrace();
//                        }
//                        }