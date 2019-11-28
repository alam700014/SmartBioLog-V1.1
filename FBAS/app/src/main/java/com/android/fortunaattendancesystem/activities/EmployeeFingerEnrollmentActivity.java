package com.android.fortunaattendancesystem.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.acpl.access_computech_fm220_sdk.FM220_Scanner_Interface;
import com.acpl.access_computech_fm220_sdk.acpl_FM220_SDK;
import com.acpl.access_computech_fm220_sdk.fm220_Capture_Result;
import com.acpl.access_computech_fm220_sdk.fm220_Init_Result;
import com.android.fortunaattendancesystem.R;
import com.android.fortunaattendancesystem.adapter.CustomEnrollListAdapter;
import com.android.fortunaattendancesystem.constant.Constants;
import com.android.fortunaattendancesystem.forlinx.ForlinxGPIO;
import com.android.fortunaattendancesystem.info.ProcessInfo;
import com.android.fortunaattendancesystem.model.BasicEmployeeInfo;
import com.android.fortunaattendancesystem.model.EmployeeEnrollInfo;
import com.android.fortunaattendancesystem.model.EmployeeFingerInfo;
import com.android.fortunaattendancesystem.model.RemoteEnrollmentInfo;
import com.android.fortunaattendancesystem.model.StartekInfo;
import com.android.fortunaattendancesystem.singleton.EmployeeFingerEnrollInfo;
import com.android.fortunaattendancesystem.singleton.RC632ReaderConnection;
import com.android.fortunaattendancesystem.singleton.Settings;
import com.android.fortunaattendancesystem.singleton.SmartReaderConnection;
import com.android.fortunaattendancesystem.singleton.StarkTekConnection;
import com.android.fortunaattendancesystem.singleton.StartekDatabaseItems;
import com.android.fortunaattendancesystem.submodules.ForlinxGPIOCommunicator;
import com.android.fortunaattendancesystem.submodules.I2CCommunicator;
import com.android.fortunaattendancesystem.submodules.MorphoCommunicator;
import com.android.fortunaattendancesystem.submodules.SQLiteCommunicator;
import com.android.fortunaattendancesystem.usbconnection.USBConnectionCreator;
import com.forlinx.android.GetValueService;
import com.forlinx.android.HardwareInterface;
import com.friendlyarm.SmartReader.SmartFinger;
import com.morpho.morphosmart.sdk.MorphoDatabase;
import com.morpho.morphosmart.sdk.MorphoDevice;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by fortuna on 19/12/17.
 */

public class EmployeeFingerEnrollmentActivity extends USBConnectionCreator implements FM220_Scanner_Interface {

    private Handler mHandler = new Handler();
    private MorphoDevice morphoDevice = null;
    private MorphoDatabase morphoDatabase = null;
    private SmartFinger rc632ReaderConnection = null;
    private ArrayAdapter <String> adapter;
    private SQLiteCommunicator dbComm = new SQLiteCommunicator();
    private CustomEnrollListAdapter customEnrollListAdapteradapter;
    private ListView list;
    private SearchView searchView;
    public static boolean isFingerEnrollmentWindowVisisble = false;

    private boolean isSmartRcvRegisterd = false;
    private boolean isMorphoRcvRegistered = false;
    private boolean isMorphoSmartRcvRegisterd = false;

    private EmployeeFingerEnrollInfo empFingerInfo = null;
    private ImageView smart_reader, finger_reader;
    private TextView editTextEmpID, editTextCardID, editTextName, txtViewNoOfFingers, txtViewFirstFingerIndex, txtViewSecondFingerIndex, txtViewSecurityLevel, txtViewVerificationMode;

    private LinearLayout tbl_EmployeeDetails, tbl_EmployeeFingerDetails, tbl_EmployeeFingerUpdationDetails, tbl_Buttons, list_Header, list_Linear;
    private ImageView empImage;
    private CheckBox chkBoxUpdateTemplate;
    private RadioGroup noOfFingers;
    private Button btnEmpDetails, btnReset, btnFingerEnroll, btnsave, btnCloseAndQuit, btnReboot;

    private Spinner finger1, finger2, securityLevel, verificationMode, fingerIndexSpinner;
    private Spinner newfinger1, newfinger2;

    private static boolean isLCDBackLightOff = false;
    private Handler cHandler = new Handler();
    private Timer capReadTimer = null;
    private TimerTask capReadTimerTask = null;
    private boolean isBreakFound = true;


    //======================== Variables for Startek Finger Sensor ============================//

    private boolean isStartekRcvRegisterd = false;
    private String Telecom_Device_Key = "";
    private acpl_FM220_SDK FM220SDK;
    private UsbManager usbManager;
    private PendingIntent mPermissionIntent;

    private Handler hBrightness, hLCDBacklight;
    private Runnable rBrightness, rLCDBacklight;

    private ImageView ivChargeIcon, ivBatTop;
    private Intent intent;
    private AdcMessageBroadcastReceiver receiver;
    private TextView tvBatPer, tvPower;
    private ProgressBar pbBatPer;

    private Handler bHandler = new Handler();
    private Timer batReadTimer = null;
    private TimerTask batReadTimerTask = null;

    private int index = 0;
    double[] numArray = new double[Constants.ADC_READ_ARRAY_LENGTH];
    private float adcValue;
    private Handler adcHandler = new Handler();
    private Timer adcReadTimer = null;
    private TimerTask adcReadTimerTask = null;

    private static boolean isSDCalculated = false;
    private static double prevMean;
    int per = 0;

    boolean isADCReceiverUnregistered = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setContentView(R.layout.activity_employee_finger_enroll);

        // ActionBar actionBar = getActionBar();
        // actionBar.setDisplayHomeAsUpEnabled(true);

        resetEmpFingerInfo();

        initLayoutElements();

        if (!Constants.isTab) {
            if (HardwareInterface.class != null) {
                receiver = new AdcMessageBroadcastReceiver();
                registerReceiver(receiver, getIntentFilter());
                intent = new Intent();
                intent.setClass(EmployeeFingerEnrollmentActivity.this, GetValueService.class);
                intent.putExtra("mtype", "ADC");
                intent.putExtra("maction", "start");
                intent.putExtra("mfd", 1);
                EmployeeFingerEnrollmentActivity.this.startService(intent);
            } else {
                Toast.makeText(getApplicationContext(), "Load hardwareinterface library error!", Toast.LENGTH_LONG).show();
            }
        }

        initSearchView();
        disableSpinnersOnStartUp(false);
        disableEnableRadioGroup(false);
        setSpinnerItems();
        setDefaultEnrollInfo();

        final int fr, sr;
        Settings settings = Settings.getInstance();
        fr = settings.getFrTypeValue();
        sr = settings.getSrTypeValue();
        if (fr == 0 && sr == 1) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    initFingerSmart();//For Morpho and MicroSmart V2
                }
            }).start();
        } else if (fr == 2) {//For Startek
            new Thread(new Runnable() {
                @Override
                public void run() {
                    // initStartekFinger();
                }
            }).start();
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    initFingerReader(fr);
                }
            }).start();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    initSmartReader(sr);
                }
            }).start();
        }

        Intent intent = getIntent();
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                EmployeeEnrollInfo empInfo = bundle.getParcelable("EmployeeEnrollInfo");
                if (empInfo != null) {
                    setLayoutDimension(list_Linear, 0, 0);
                    setLayoutDimension(searchView, 0, 0);
                    setLayoutDimension(list_Header, 0, 0);
                    setLayoutDimension(tbl_EmployeeDetails, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    setLayoutDimension(tbl_EmployeeFingerDetails, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    setLayoutDimension(tbl_Buttons, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    removeSearchList();
                    refreshFingerDetailsPanel();
                    editTextEmpID.setText(empInfo.getEmpId());
                    editTextCardID.setText(empInfo.getCardId());
                    editTextName.setText(empInfo.getEmpName());
                    empFingerInfo.setEmpId(empInfo.getEmpId().trim());
                    empFingerInfo.setCardId(empInfo.getCardId().replaceAll("\\G0", " ").trim());
                    empFingerInfo.setEmpName(empInfo.getEmpName().trim());
                    byte[] imageData = empInfo.getPhoto();
                    if (imageData != null) {
                        empImage.setImageBitmap(BitmapFactory.decodeByteArray(imageData, 0, imageData.length));
                    } else {
                        empImage.setImageResource(R.drawable.dummyphoto);
                    }

                    //===========================  Mqtt Remote Enrollment  ===================================//

                    if (empInfo.isRemoteEnroll()) {
                        noOfFingers.check(R.id.twofingerEnroll);
                        empFingerInfo.setEnroll(true);//Is Finger Enroll
                        empFingerInfo.setOperation(1);//1 For Enroll Template
                        empFingerInfo.setNoOfFingers(2);//Two Templates Enroll

                        String firstFingerIndex = "R-Thumb";
                        String secondFingerIndex = "L-Thumb";
                        String securityLevel = "C";
                        String verificationMode = "1:N";

                        for (int count = 0; count < Constants.FINGER_INDEX_DISPLAY.length; count++) {
                            if (firstFingerIndex.equals(Constants.FINGER_INDEX_DISPLAY[count])) {
                                empFingerInfo.setStrFirstFingerIndex(Constants.FINGER_INDEX_DISPLAY[count]);
                                break;
                            }
                        }

                        for (int count = 0; count < Constants.FINGER_INDEX_DISPLAY.length; count++) {
                            if (secondFingerIndex.equals(Constants.FINGER_INDEX_DISPLAY[count])) {
                                empFingerInfo.setStrSecondFingerIndex(Constants.FINGER_INDEX_DISPLAY[count]);
                                break;
                            }
                        }

                        for (int count = 0; count < Constants.SECURITY_LEVEL_DISPLAY.length; count++) {
                            if (securityLevel.equals(Constants.SECURITY_LEVEL_DISPLAY[count])) {
                                empFingerInfo.setStrSecurityLevel(Constants.SECURITY_LEVEL_DISPLAY[count]);
                                break;
                            }
                        }

                        for (int count = 0; count < Constants.VERIFICATION_MODE_DISPLAY.length; count++) {
                            if (verificationMode.equals(Constants.VERIFICATION_MODE_DISPLAY[count])) {
                                empFingerInfo.setStrVerificationMode(Constants.VERIFICATION_MODE_DISPLAY[count]);
                                break;
                            }
                        }

                        morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
                        morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
                        if (morphoDevice != null && morphoDatabase != null) {
                            String strButtonText = btnFingerEnroll.getText().toString();
                            if (strButtonText.equalsIgnoreCase("Enroll Finger")) {
                                boolean isValid = false;
                                isValid = validateBasicInfo();
                                if (isValid) {
                                    isValid = validateSpinnerCheck(1);
                                    if (isValid) {
                                        Intent startEnrollProcess = new Intent(EmployeeFingerEnrollmentActivity.this, FingerEnrollUpdateDialogActivity.class);
                                        Bundle bundleEnroll = new Bundle();
                                        bundleEnroll.putString("ET", "RE");
                                        bundleEnroll.putString("JID", "");
                                        bundleEnroll.putBoolean("IE", empFingerInfo.isEnroll());//IsEnroll
                                        bundleEnroll.putInt("OP", empFingerInfo.getOperation());
                                        bundleEnroll.putInt("NOF", empFingerInfo.getNoOfFingers());
                                        bundleEnroll.putString("EID", empFingerInfo.getEmpId());
                                        bundleEnroll.putString("CID", empFingerInfo.getCardId());
                                        bundleEnroll.putString("EN", empFingerInfo.getEmpName());
                                        bundleEnroll.putString("FFI", empFingerInfo.getStrFirstFingerIndex());
                                        bundleEnroll.putString("SFI", empFingerInfo.getStrSecondFingerIndex());
                                        bundleEnroll.putString("SL", empFingerInfo.getStrSecurityLevel());
                                        bundleEnroll.putString("VM", empFingerInfo.getStrVerificationMode());
                                        startEnrollProcess.putExtras(bundleEnroll);
                                        startActivity(startEnrollProcess);
                                    }
                                }
                            }
                        } else {
                            showCustomAlertDialog(false, "Device Connection Status", "Please Connect Finger Reader");
                        }
                    }

                    //===============================================================================================//

                } else {
                    bundle = intent.getExtras();
                    if (bundle != null) {
                        RemoteEnrollmentInfo remoteEnrollInfo = bundle.getParcelable("RemoteEnrollInfo");
                        if (remoteEnrollInfo != null) {
                            setLayoutDimension(list_Linear, 0, 0);
                            setLayoutDimension(searchView, 0, 0);
                            setLayoutDimension(list_Header, 0, 0);
                            setLayoutDimension(tbl_EmployeeDetails, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            setLayoutDimension(tbl_EmployeeFingerDetails, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            setLayoutDimension(tbl_Buttons, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            removeSearchList();
                            refreshFingerDetailsPanel();
                            editTextEmpID.setText(remoteEnrollInfo.getEmpId());
                            editTextCardID.setText(remoteEnrollInfo.getCardId());
                            editTextName.setText(remoteEnrollInfo.getEmpName());

                            empFingerInfo.setEmpId(remoteEnrollInfo.getEmpId().trim());
                            empFingerInfo.setCardId(remoteEnrollInfo.getCardId().replaceAll("\\G0", " ").trim());
                            empFingerInfo.setEmpName(remoteEnrollInfo.getEmpName().trim());
                            byte[] imageData = null;
                            if (imageData != null && imageData.length > 0) {
                                empImage.setImageBitmap(BitmapFactory.decodeByteArray(imageData, 0, imageData.length));
                            } else {
                                empImage.setImageResource(R.drawable.dummyphoto);
                            }

                            noOfFingers.check(R.id.onefingerEnroll);
                            empFingerInfo.setEnroll(true);//Is Finger Enroll
                            empFingerInfo.setOperation(1);//For Enroll Template
                            empFingerInfo.setNoOfFingers(1);//One Template Enroll

                            String firstFingerIndex = "";
                            String secondFingerIndex = "";
                            String securityLevel = "";
                            String verificationMode = "";

                            String fingerType = remoteEnrollInfo.getFingerType();
                            if (fingerType != null && fingerType.trim().length() > 0 && fingerType.equals("F1")) {
                                firstFingerIndex = remoteEnrollInfo.getFingerIndex();
                            } else if (fingerType != null && fingerType.trim().length() > 0 && fingerType.equals("F2")) {
                                secondFingerIndex = remoteEnrollInfo.getFingerIndex();
                            }

                            securityLevel = remoteEnrollInfo.getSecurityLevel();
                            verificationMode = remoteEnrollInfo.getVerificationMode();

                            if (firstFingerIndex.trim().length() > 0) {
                                for (int count = 0; count < Constants.FINGER_INDEX_DISPLAY.length; count++) {
                                    if (firstFingerIndex.equals(Constants.FINGER_INDEX_DISPLAY[count])) {
                                        empFingerInfo.setStrFirstFingerIndex(Constants.FINGER_INDEX_DISPLAY[count]);
                                        break;
                                    }
                                }
                            }

                            if (secondFingerIndex.trim().length() > 0) {
                                for (int count = 0; count < Constants.FINGER_INDEX_DISPLAY.length; count++) {
                                    if (secondFingerIndex.equals(Constants.FINGER_INDEX_DISPLAY[count])) {
                                        empFingerInfo.setStrSecondFingerIndex(Constants.FINGER_INDEX_DISPLAY[count]);
                                        break;
                                    }
                                }
                            }

                            if (securityLevel != null && securityLevel.trim().length() > 0) {
                                for (int count = 0; count < Constants.SECURITY_LEVEL_DISPLAY.length; count++) {
                                    if (securityLevel.equals(Constants.SECURITY_LEVEL_DISPLAY[count])) {
                                        empFingerInfo.setStrSecurityLevel(Constants.SECURITY_LEVEL_DISPLAY[count]);
                                        break;
                                    }
                                }

                            }

                            if (verificationMode != null && verificationMode.trim().length() > 0) {
                                for (int count = 0; count < Constants.VERIFICATION_MODE_DISPLAY.length; count++) {
                                    if (verificationMode.equals(Constants.VERIFICATION_MODE_DISPLAY[count])) {
                                        empFingerInfo.setStrVerificationMode(Constants.VERIFICATION_MODE_DISPLAY[count]);
                                        break;
                                    }
                                }
                            }
                            morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
                            morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
                            if (morphoDevice != null && morphoDatabase != null) {
                                String strButtonText = btnFingerEnroll.getText().toString();
                                if (strButtonText.equalsIgnoreCase("Enroll Finger")) {
                                    boolean isValid = false;
                                    isValid = validateBasicInfo();
                                    if (isValid) {
                                        isValid = validateSpinnerCheck(fingerType);
                                        if (isValid) {
                                            Intent startEnrollProcess = new Intent(EmployeeFingerEnrollmentActivity.this, FingerEnrollUpdateDialogActivity.class);
                                            Bundle bundleEnroll = new Bundle();
                                            bundleEnroll.putString("ET", "RE");
                                            bundleEnroll.putString("JID", remoteEnrollInfo.getJobId());
                                            bundleEnroll.putBoolean("IE", empFingerInfo.isEnroll());//IsEnroll
                                            bundleEnroll.putInt("OP", empFingerInfo.getOperation());
                                            bundleEnroll.putInt("NOF", empFingerInfo.getNoOfFingers());
                                            bundleEnroll.putString("EID", empFingerInfo.getEmpId());
                                            bundleEnroll.putString("CID", empFingerInfo.getCardId());
                                            bundleEnroll.putString("EN", empFingerInfo.getEmpName());
                                            bundleEnroll.putString("FFI", empFingerInfo.getStrFirstFingerIndex());
                                            bundleEnroll.putString("SFI", empFingerInfo.getStrSecondFingerIndex());
                                            bundleEnroll.putString("SL", empFingerInfo.getStrSecurityLevel());
                                            bundleEnroll.putString("VM", empFingerInfo.getStrVerificationMode());
                                            startEnrollProcess.putExtras(bundleEnroll);
                                            startActivity(startEnrollProcess);
                                        }
                                    }
                                }
                            } else {
                                showCustomAlertDialog(false, "Device Connection Status", "Please Connect Finger Reader");
                            }
                        }
                    }
                }
            }
        }

        hBrightness = new Handler();
        hLCDBacklight = new Handler();

        rBrightness = new Runnable() {
            @Override
            public void run() {
                setScreenBrightness(Constants.BRIGHTNESS_OFF);
                isLCDBackLightOff = true;
            }
        };

        rLCDBacklight = new Runnable() {
            @Override
            public void run() {
                ForlinxGPIO.setLCDBackLightOff();
                isLCDBackLightOff = true;
            }
        };

        startHandler();
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        stopHandler();//stop first and then start
        startHandler();
        ForlinxGPIO.setLCDBackLightOn();
        setScreenBrightness(Constants.BRIGHTNESS_ON);
        isLCDBackLightOff = false;
    }

    public void startHandler() {
        hBrightness.postDelayed(rBrightness, Constants.BRIGHTNESS_OFF_DELAY); //for 10 seconds
        hLCDBacklight.postDelayed(rLCDBacklight, Constants.BACKLIGHT_OFF_DELAY); //for 20 seconds
    }

    public void stopHandler() {
        hBrightness.removeCallbacks(rBrightness);
        hLCDBacklight.removeCallbacks(rLCDBacklight);
    }

    // Change the screen brightness
    public void setScreenBrightness(int brightnessValue) {
        // Make sure brightness value between 0 to 255
        if (brightnessValue >= 0 && brightnessValue <= 255) {
            android.provider.Settings.System.putInt(
                    getApplicationContext().getContentResolver(),
                    android.provider.Settings.System.SCREEN_BRIGHTNESS,
                    brightnessValue
            );
        }
    }

    //============================ For Startek Finger Sensor =================================//

    private void initStartekFinger() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences sp = getSharedPreferences("last_FM220_type", Activity.MODE_PRIVATE);
                boolean oldDevType = sp.getBoolean("FM220type", true);

                usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
                final Intent piIntent = new Intent(Constants.ACTION_USB_PERMISSION);
                if (Build.VERSION.SDK_INT >= 16) piIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
                mPermissionIntent = PendingIntent.getBroadcast(getBaseContext(), 1, piIntent, 0);

                IntentFilter filter = new IntentFilter(Constants.ACTION_USB_PERMISSION);
                filter.addAction(Constants.ACTION_USB_PERMISSION);
                filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
                filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
                registerReceiver(mStartekReceiver, filter);
                isStartekRcvRegisterd = true;
                UsbDevice device = null;
                for (UsbDevice mdevice : usbManager.getDeviceList().values()) {
                    int pid, vid;
                    pid = mdevice.getProductId();
                    vid = mdevice.getVendorId();
                    boolean devType;
                    if ((pid == 0x8225) && (vid == 0x0bca)) {
                        FM220SDK = new acpl_FM220_SDK(getApplicationContext(), EmployeeFingerEnrollmentActivity.this, true);
                        devType = true;
                    } else if ((pid == 0x8220) && (vid == 0x0bca)) {
                        FM220SDK = new acpl_FM220_SDK(getApplicationContext(), EmployeeFingerEnrollmentActivity.this, false);
                        devType = false;
                    } else {
                        FM220SDK = new acpl_FM220_SDK(getApplicationContext(), EmployeeFingerEnrollmentActivity.this, oldDevType);
                        devType = oldDevType;
                    }
                    if (oldDevType != devType) {
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putBoolean("FM220type", devType);
                        editor.apply();
                    }
                    if ((pid == 0x8225 || pid == 0x8220) && (vid == 0x0bca)) {
                        device = mdevice;
                        if (!usbManager.hasPermission(device)) {
                            usbManager.requestPermission(device, mPermissionIntent);
                        } else {
                            if (FM220SDK != null) {
                                fm220_Init_Result res = FM220SDK.InitScannerFM220(usbManager, device, Telecom_Device_Key);
                                if (res.getResult()) {
                                    StarkTekConnection.getInstance().setFM220SDK(FM220SDK);
                                    updateFrConStatusToUI(true);
                                } else {
                                    updateFrConStatusToUI(false);
                                }
                            }
                        }
                        break;
                    }
                }
                if (device == null) {
                    FM220SDK = new acpl_FM220_SDK(getApplicationContext(), EmployeeFingerEnrollmentActivity.this, oldDevType);
                    StarkTekConnection.getInstance().setFM220SDK(FM220SDK);
                }
            }
        }).start();
    }


    private void resetEmpFingerInfo() {
        empFingerInfo = EmployeeFingerEnrollInfo.getInstance();
        if (empFingerInfo != null) {
            empFingerInfo.reset();
        }
    }

    class AdcMessageBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            String adcmessage = intent.getStringExtra("adc_value");
            if (adcmessage != null) {
                float level = Float.parseFloat(adcmessage);
                adcValue = level;
                if (isSDCalculated) {
                    isSDCalculated = false;
                    float out1 = scaleVal(Constants.X1, Constants.X2, Constants.Y1, Constants.Y2, (float) prevMean);
                    float out2 = scaleVal(Constants.XX1, Constants.XX2, Constants.YY1, Constants.YY2, out1);
                    per = (int) scaleVal(Constants.XXX1, Constants.XXX2, Constants.YYY1, Constants.YYY2, out2);
                    per = per * 20;

                    // Log.d("TEST", "adc:" + adcValue + " out1:" + out1 + " out2:" + out2 + " per:" + per);

                    tvBatPer.setText("" + per + "%");
                    pbBatPer.setProgress(per);
                }
            }
        }
    }

    IntentFilter getIntentFilter() {
        IntentFilter intent = new IntentFilter();
        intent.addAction("ADC_UPDATE");
        return intent;
    }

    float scaleVal(float x1, float x2, float y1, float y2, float xv) {
        if (x2 < xv) {
            xv = x2;
        }
        if (x1 > xv) {
            xv = x1;
        }
        return ((((y2 - y1) / (x2 - x1)) * (xv - x1)) + y1);
    }

    public void initFingerReader(int fingerReader) {

        switch (fingerReader) {

            case 0:

                //========================= Morpho Finger Reader =====================================//

                initUSBManagerReceiver();
                unregisterReceivers();
                registerBroadCastReceiver(1);
                initHardwareConnections(1);

                break;

            case 1:

                //======================== Aratek Finger Sensor ======================================//

                break;

            default:
                break;

        }
    }

    private void initSmartReader(int smartReader) {

        switch (smartReader) {

            case 0:

                //======================== RC632 SPI Smart Reader ======================================//

                initHardwareConnections(4);

                break;

            case 1:


                //========================= Micro Smart V2 Smart Reader =====================================//

                initUSBManagerReceiver();
                unregisterReceivers();
                registerBroadCastReceiver(0);
                initHardwareConnections(2);

                break;

            case 2:

                //========================== RC522 Smart Reader =============================//

                updateSrConStatusToUI(true);
                break;

            default:
                break;

        }

    }

    public void initFingerSmart() {
        initUSBManagerReceiver();
        unregisterReceivers();
        registerBroadCastReceiver(2);
        initHardwareConnections(3);
    }

    private void initHardwareConnections(int mode) {

        switch (mode) {

            case 1:

                //======================== Morpho Finger Reader ===========================//

                searchDevices(1);

//                mHandler.postDelayed(new Runnable() {
//                    @Override
//                    public synchronized void run() {
//                        searchDevices(1);
//                    }
//                }, 500);

                break;


            case 2:

                //======================== Micro Smart V2 Smart Reader ===========================//


                searchDevices(2);

//                mHandler.postDelayed(new Runnable() {
//                    @Override
//                    public synchronized void run() {
//                        searchDevices(2);
//                    }
//                }, 500);

                break;


            case 3:

                //======================== Morpho And Micro Smart V2 Smart Reader ===========================//

                searchDevices(3);

//                mHandler.postDelayed(new Runnable() {
//                    @Override
//                    public synchronized void run() {
//                        searchDevices(3);
//                    }
//                }, 500);

                break;

            case 4:

                //======================== RC632 SPI Smart Reader ===========================//

                rc632ReaderConnection = RC632ReaderConnection.getInstance().getSmartFinger();
                if (rc632ReaderConnection == null) {
                    String strPath = "/sys/class/gpio/gpio63/direction";
                    if (!new File(strPath).exists()) {
                        Thread rcInitThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                boolean initStatus = false;
                                initPins();
                                initStatus = initSpi();
                                if (initStatus) {
                                    updateSrConStatusToUI(true);
                                } else {
                                    updateSrConStatusToUI(false);
                                }
                            }
                        });
                        rcInitThread.start();
                    } else {
                        Thread rcInitThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                boolean initStatus = false;
                                initStatus = initSpi();
                                if (initStatus) {
                                    updateSrConStatusToUI(true);
                                } else {
                                    updateSrConStatusToUI(false);
                                }
                            }
                        });
                        rcInitThread.start();
                    }
                } else {
                    updateSrConStatusToUI(true);
                }

                break;

            default:
                break;

        }
    }

    public void initLayoutElements() {

        pbBatPer = (ProgressBar) findViewById(R.id.pbBatPer);
        tvBatPer = (TextView) findViewById(R.id.tvBatPer);
        tvPower = (TextView) findViewById(R.id.tvPower);

        ivChargeIcon = (ImageView) findViewById(R.id.ivChargeIcon);
        ivBatTop = (ImageView) findViewById(R.id.ivBatTop);


        searchView = (SearchView) findViewById(R.id.search);
        smart_reader = (ImageView) findViewById(R.id.smartreader);
        finger_reader = (ImageView) findViewById(R.id.fingerreader);


        editTextEmpID = (TextView) findViewById(R.id.EmployeeID);
        editTextCardID = (TextView) findViewById(R.id.CardID);
        editTextName = (TextView) findViewById(R.id.Name);
        empImage = (ImageView) findViewById(R.id.U_image);


        txtViewNoOfFingers = (TextView) findViewById(R.id.NoOfFingers);
        txtViewFirstFingerIndex = (TextView) findViewById(R.id.FirstFingerIndex);
        txtViewSecondFingerIndex = (TextView) findViewById(R.id.SecondFingerIndex);
        txtViewSecurityLevel = (TextView) findViewById(R.id.SecurityLevel);
        txtViewVerificationMode = (TextView) findViewById(R.id.VerificationMode);


        btnEmpDetails = (Button) findViewById(R.id.btn_empDetails);
        btnReset = (Button) findViewById(R.id.btn_Reset);

        btnFingerEnroll = (Button) findViewById(R.id.btn_enroll);
        btnReboot = (Button) findViewById(R.id.btn_rebootsoft);
        btnCloseAndQuit = (Button) findViewById(R.id.btn_closeandquit);
        btnsave = (Button) findViewById(R.id.btn_save);

        chkBoxUpdateTemplate = (CheckBox) findViewById(R.id.updateTemplate);

        finger1 = (Spinner) findViewById(R.id.exportfinger1);
        finger2 = (Spinner) findViewById(R.id.exportfinger2);

        newfinger1 = (Spinner) findViewById(R.id.newFirstFingerIndex);
        newfinger2 = (Spinner) findViewById(R.id.newSecondFingerIndex);

        fingerIndexSpinner = (Spinner) findViewById(R.id.fingerIndex);
        finger2.setEnabled(false);

        securityLevel = (Spinner) findViewById(R.id.securitylevel);
        verificationMode = (Spinner) findViewById(R.id.VarificationMode);

        noOfFingers = (RadioGroup) findViewById(R.id.fingernumber);

        noOfFingers.check(R.id.twofingerEnroll);

        tbl_EmployeeDetails = (LinearLayout) findViewById(R.id.tblEmpDetails);
        tbl_EmployeeFingerDetails = (LinearLayout) findViewById(R.id.tblEmpFingerDetails);
        tbl_EmployeeFingerUpdationDetails = (LinearLayout) findViewById(R.id.tblEmpFingerUpdationDetails);
        tbl_Buttons = (LinearLayout) findViewById(R.id.tblButtons);
        list_Header = (LinearLayout) findViewById(R.id.list_header);
        list_Linear = (LinearLayout) findViewById(R.id.list_linear);
    }


    public void initSearchView() {
        try {
            searchView.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
            searchView.setIconifiedByDefault(false);
            searchView.setQueryHint("Employee ID, Name, Card ID");
            searchView.requestFocusFromTouch();
            searchView.setClickable(true);
            list = (ListView) findViewById(R.id.Emplist);
            ArrayList <BasicEmployeeInfo> empList = null;
            empList = dbComm.getEnrolledSearchList(empList);
            if (empList != null) {
                customEnrollListAdapteradapter = new CustomEnrollListAdapter(this, empList);
                list.setAdapter(customEnrollListAdapteradapter);
                final Filter filter = customEnrollListAdapteradapter.getFilter();
                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView <?> parent, View view, int position, long id) {
                        final TextView tvEmpId = (TextView) view.findViewById(R.id.EmpID_list);
                        final String selectedEmpId = tvEmpId.getText().toString();
                        showEmployeeDetails(selectedEmpId);
                    }
                });

                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        filter.filter(newText);
                        list.setFilterText(newText);
                        return true;
                    }
                });
            } else {
                // showCustomAlertDialog(false, "Alert", "Employee list not available !");
            }
        } catch (Exception ex) {

        }
    }

    public void disableSpinnersOnStartUp(boolean isEnable) {
        finger1.setEnabled(isEnable);
        // finger2.setEnabled(isEnable);
        securityLevel.setEnabled(isEnable);
        verificationMode.setEnabled(isEnable);

    }

    public void disableEnableRadioGroup(boolean isEnable) {
        for (int i = 0; i < noOfFingers.getChildCount(); i++) {
            noOfFingers.getChildAt(i).setEnabled(isEnable);
        }
    }

    public void setSpinnerItems() {
        fillFingerIndex();
        fillSecurityLevel();
        fillVerificationMode();
    }

    public void fillFingerIndex() {
        adapter = new ArrayAdapter <String>(this, android.R.layout.simple_spinner_item, Constants.FINGER_INDEX_DISPLAY);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        finger1.setAdapter(adapter);
        finger2.setAdapter(adapter);
        newfinger1.setAdapter(adapter);
        newfinger2.setAdapter(adapter);

        finger1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView <?> arg0, View arg1, int arg2, long arg3) {
                //   Log.d("TEST", "***FFI***" + finger1.getSelectedItem().toString());
//                String firstFingerIndex = finger1.getSelectedItem().toString();
//                if (!firstFingerIndex.trim().equals("Select")) {
//                    empFingerInfo.setStrFirstFingerIndex(firstFingerIndex);
//                }
                searchView.clearFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
            }

            public void onNothingSelected(AdapterView <?> arg0) {
                // TODO Auto-generated method stub
            }
        });

        finger2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView <?> arg0, View arg1, int arg2, long arg3) {
                //   Log.d("TEST", "***SFI***" + finger2.getSelectedItem().toString());
//                String secondFingerIndex = finger2.getSelectedItem().toString();
//                if (!secondFingerIndex.trim().equals("Select")) {
//                    empFingerInfo.setStrSecondFingerIndex(secondFingerIndex);
//                }
                searchView.clearFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
            }

            public void onNothingSelected(AdapterView <?> arg0) {
                // TODO Auto-generated method stub
            }
        });


        newfinger1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView <?> arg0, View arg1, int arg2, long arg3) {
//                String strNewFirstFingerIndex = newfinger1.getSelectedItem().toString();
//                if (!strNewFirstFingerIndex.trim().equals("Select")) {
//                    empFingerInfo.setStrNewFirstFingerIndex(strNewFirstFingerIndex);
//                }
                searchView.clearFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
            }

            public void onNothingSelected(AdapterView <?> arg0) {
                // TODO Auto-generated method stub
            }
        });

        newfinger2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView <?> arg0, View arg1, int arg2, long arg3) {
//                String strNewSecondFingerIndex = newfinger2.getSelectedItem().toString();
//                if (!strNewSecondFingerIndex.trim().equals("Select")) {
//                    empFingerInfo.setStrNewSecondFingerIndex(strNewSecondFingerIndex);
//                }
                searchView.clearFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
            }

            public void onNothingSelected(AdapterView <?> arg0) {
                // TODO Auto-generated method stub
            }
        });
    }

    public void fillVerificationMode() {

        adapter = new ArrayAdapter <String>(this, android.R.layout.simple_spinner_item, Constants.VERIFICATION_MODE_DISPLAY);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        verificationMode.setAdapter(adapter);

        verificationMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView <?> arg0, View arg1, int arg2, long arg3) {
                //   Log.d("TEST", "***VM***" + verificationMode.getSelectedItem().toString());
                String strVerificationMode = verificationMode.getSelectedItem().toString();
                if (!strVerificationMode.trim().equals("Select")) {
                    // empFingerInfo.setStrVerificationMode(strVerificationMode);
                }
                searchView.clearFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
            }

            public void onNothingSelected(AdapterView <?> arg0) {
                // TODO Auto-generated method stub
            }
        });

    }

    public void fillSecurityLevel() {

        adapter = new ArrayAdapter <String>(this, android.R.layout.simple_spinner_item, Constants.SECURITY_LEVEL_DISPLAY);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        securityLevel.setAdapter(adapter);

        securityLevel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView <?> arg0, View arg1, int arg2, long arg3) {
                //   Log.d("TEST", "***SL***" + securityLevel.getSelectedItem().toString());
//                String strSecurityLevel = securityLevel.getSelectedItem().toString();
//                if (!strSecurityLevel.trim().equals("Select")) {
//                    empFingerInfo.setStrSecurityLevel(strSecurityLevel);
//                }
                searchView.clearFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
            }

            public void onNothingSelected(AdapterView <?> arg0) {
                // TODO Auto-generated method stub
            }
        });
    }

    public void refreshLayout() {
        searchView.setQuery("", false);
        searchView.clearFocus();

        setLayoutDimension(list_Linear, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        setLayoutDimension(searchView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        setLayoutDimension(list_Header, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        setLayoutDimension(tbl_EmployeeDetails, 0, 0);
        setLayoutDimension(tbl_EmployeeFingerDetails, 0, 0);
        setLayoutDimension(tbl_EmployeeFingerUpdationDetails, 0, 0);
        setLayoutDimension(tbl_Buttons, 0, 0);
    }


    private void showEmployeeDetails(String strSelectedEmpId) {

        setLayoutDimension(list_Linear, 0, 0);
        setLayoutDimension(searchView, 0, 0);
        setLayoutDimension(list_Header, 0, 0);

        Cursor resData = null;
        resData = dbComm.getEmpDetailsByEmpId(strSelectedEmpId);

        if (resData != null) {
            reset();
            int autoId = -1;
            while (resData.moveToNext()) {
                autoId = resData.getInt(0);
                String strCardId = resData.getString(1).replaceAll("\\G0", " ").trim();
                String strEmpName = resData.getString(2).trim();
                byte[] imageData = resData.getBlob(3);
                editTextEmpID.setText(strSelectedEmpId);
                editTextCardID.setText(strCardId);
                editTextName.setText(strEmpName);
                empFingerInfo.setEmpId(strSelectedEmpId.trim());
                empFingerInfo.setCardId(strCardId);
                empFingerInfo.setEmpName(strEmpName);
                if (imageData != null && imageData.length > 1) {
                    empImage.setImageBitmap(BitmapFactory.decodeByteArray(imageData, 0, imageData.length));
                } else {
                    empImage.setImageResource(R.drawable.dummyphoto);
                }
            }
            if (resData != null) {
                resData.close();
            }
            setLayoutDimension(tbl_EmployeeDetails, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            setLayoutDimension(tbl_Buttons, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            if (autoId != -1) {
                ArrayList <EmployeeFingerInfo> empFingerInfoList = null;
                empFingerInfoList = dbComm.getFingerDetailsByEmployeeEnrollmentNo(autoId, empFingerInfoList);
                if (empFingerInfoList != null) {
                    int size = empFingerInfoList.size();
                    for (int i = 0; i < size; i++) {
                        if (i == 0) {
                            txtViewFirstFingerIndex.setText(empFingerInfoList.get(i).getFingerIndex());
                        } else if (i == 1) {
                            txtViewSecondFingerIndex.setText(empFingerInfoList.get(i).getFingerIndex());
                        }
                        txtViewSecurityLevel.setText(empFingerInfoList.get(i).getSecurityLevel());
                        txtViewVerificationMode.setText(empFingerInfoList.get(i).getVerificationMode());
                    }
                    if (size == 1) {
                        txtViewNoOfFingers.setText("One");
                    } else if (size == 2) {
                        txtViewNoOfFingers.setText("Two");
                    }

                    //====================== Commented On 01-11-2019  ===========================//

                    //setLayoutDimension(tbl_EmployeeFingerDetails, 0, 0);
                    //setLayoutDimension(tbl_EmployeeFingerUpdationDetails, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    // newfinger1.setEnabled(false);
                    // newfinger2.setEnabled(false);
                    // initFingerIndexSpinner(size);

                    //============================================================================//

                    setLayoutDimension(tbl_EmployeeFingerUpdationDetails, 0, 0);
                    setLayoutDimension(tbl_EmployeeFingerDetails, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    btnFingerEnroll.setText("Update Finger");
                } else {
                    setLayoutDimension(tbl_EmployeeFingerUpdationDetails, 0, 0);
                    setLayoutDimension(tbl_EmployeeFingerDetails, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    btnFingerEnroll.setText("Enroll Finger");
                }
            }
        }
    }

    private void reset() {
        refresh();
        removeSearchList();
        refreshFingerDetailsPanel();
        refreshFingerUpdatePanel();
    }

    private void refresh() {
        empImage.setImageResource(R.drawable.dummyphoto);
        editTextEmpID.setText("");
        editTextCardID.setText("");
        editTextName.setText("");
        txtViewNoOfFingers.setText("NA");
        txtViewFirstFingerIndex.setText("NA");
        txtViewSecondFingerIndex.setText("NA");
        txtViewSecurityLevel.setText("NA");
        txtViewVerificationMode.setText("NA");
        disableSpinnersOnStartUp(false);
        disableEnableRadioGroup(false);
        enableDisableButtons(false);
    }

    private void removeSearchList() {
        tbl_EmployeeDetails.setVisibility(View.VISIBLE);
        tbl_EmployeeFingerDetails.setVisibility(View.VISIBLE);
        tbl_EmployeeFingerUpdationDetails.setVisibility(View.VISIBLE);
        tbl_Buttons.setVisibility(View.VISIBLE);
        enableDisableButtons(true);
        disableSpinnersOnStartUp(true);
        disableEnableRadioGroup(true);
    }

    private void refreshFingerDetailsPanel() {
        finger1.setSelection(0);
        finger2.setSelection(0);
        securityLevel.setSelection(0);
        verificationMode.setSelection(0);
        noOfFingers.check(R.id.twofingerEnroll);
        finger2.setEnabled(true);
        empFingerInfo.setNoOfFingers(2);
    }

    private void refreshFingerUpdatePanel() {
        chkBoxUpdateTemplate.setChecked(false);
        fingerIndexSpinner.setSelection(0);
        newfinger1.setSelection(0);
        newfinger2.setSelection(0);
        fingerIndexSpinner.setEnabled(false);
        newfinger1.setEnabled(false);
        newfinger2.setEnabled(false);
    }

    private void enableDisableButtons(boolean isEnable) {
        if (isEnable) {
            noOfFingers.setEnabled(isEnable);
            chkBoxUpdateTemplate.setEnabled(isEnable);
            btnEmpDetails.setEnabled(isEnable);
            btnEmpDetails.setTextColor(Color.parseColor("#FFFFFF"));
            btnReset.setEnabled(isEnable);
            btnReset.setTextColor(Color.parseColor("#FFFFFF"));
            btnFingerEnroll.setEnabled(isEnable);
            btnFingerEnroll.setTextColor(Color.parseColor("#FFFFFF"));
            noOfFingers.setEnabled(isEnable);
        } else {
            noOfFingers.setEnabled(isEnable);
            chkBoxUpdateTemplate.setEnabled(isEnable);
            btnEmpDetails.setEnabled(isEnable);
            btnEmpDetails.setTextColor(Color.parseColor("#474747"));
            btnReset.setEnabled(isEnable);
            btnReset.setTextColor(Color.parseColor("#474747"));
            btnFingerEnroll.setEnabled(isEnable);
            btnFingerEnroll.setTextColor(Color.parseColor("#474747"));
            noOfFingers.setEnabled(isEnable);
        }
    }

    public void setDefaultEnrollInfo() {
        initFingerNumber();
        initSavePKInDatabase();
        initUpdateTemplate();
    }

    private void initFingerNumber() {
        int fingerNb = empFingerInfo.getNoOfFingers();
        int id = R.id.onefingerEnroll;
        if (fingerNb == 2) {
            id = R.id.twofingerEnroll;
        }
        RadioButton rb = (RadioButton) findViewById(id);
        rb.setChecked(true);
    }

    private void initSavePKInDatabase() {
        CheckBox savePKinDatabase = (CheckBox) findViewById(R.id.savepkindatabase);
        savePKinDatabase.setChecked(empFingerInfo.isSavePKinDatabase());
    }

    private void initUpdateTemplate() {
        chkBoxUpdateTemplate = (CheckBox) findViewById(R.id.updateTemplate);
        chkBoxUpdateTemplate.setChecked(empFingerInfo.isUpdateTemplate());
    }

    public final void onNumberFingerClicked(View view) {
        RadioButton rb = (RadioButton) view;
        int action = 0;
        Spinner spinner = (Spinner) findViewById(R.id.fingerIndex);
        if (rb.getId() == R.id.onefingerEnroll) {
            action = 1;
            spinner.setEnabled(EmployeeFingerEnrollInfo.getInstance().isUpdateTemplate());
            finger2.setEnabled(false);
        } else if (rb.getId() == R.id.twofingerEnroll) {
            action = 2;
            spinner.setEnabled(false);
            finger2.setEnabled(true);
        }
        finger1.setSelection(0);
        finger2.setSelection(0);
        securityLevel.setSelection(0);
        verificationMode.setSelection(0);
        empFingerInfo.setNoOfFingers(action);
    }


    private void setLayoutDimension(LinearLayout linearLayout, int width, int height) {
        ViewGroup.LayoutParams paramsEmpDetails = linearLayout.getLayoutParams();
        paramsEmpDetails.width = width;
        paramsEmpDetails.height = height;
        linearLayout.setLayoutParams(paramsEmpDetails);
    }

    public void initFingerIndexSpinner(int noOfFingersEnroll) {

        CharSequence[] oneFingerUpdateArray = {"Select", "First Finger"};
        CharSequence[] twoFingerUpdateArray = {"Select", "First Finger", "Second Finger", "Both"};

        List <CharSequence> itemListOneFinger = new ArrayList <CharSequence>(Arrays.asList(oneFingerUpdateArray));
        List <CharSequence> itemListTwoFinger = new ArrayList <CharSequence>(Arrays.asList(twoFingerUpdateArray));

        ArrayAdapter adapterOneFinger = new ArrayAdapter(this, android.R.layout.simple_spinner_item, itemListOneFinger);
        ArrayAdapter adapterTwoFingers = new ArrayAdapter(this, android.R.layout.simple_spinner_item, itemListTwoFinger);

        adapterOneFinger.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapterTwoFingers.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        if (noOfFingersEnroll == 1) {
            fingerIndexSpinner.setAdapter(adapterOneFinger);
            TextView tvSecondFingerIndex = (TextView) findViewById(R.id.tvSecondFingerIndex);
            tvSecondFingerIndex.setVisibility(View.GONE);
            newfinger2.setVisibility(View.GONE);
        } else if (noOfFingersEnroll == 2) {
            fingerIndexSpinner.setAdapter(adapterTwoFingers);
            TextView tvSecondFingerIndex = (TextView) findViewById(R.id.tvSecondFingerIndex);
            tvSecondFingerIndex.setVisibility(View.VISIBLE);
            newfinger2.setVisibility(View.VISIBLE);
        }

        fingerIndexSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView <?> parent, View view, int pos, long id) {
                switch (pos) {
                    case 0:
                        newfinger1.setEnabled(false);
                        newfinger2.setEnabled(false);
                        newfinger1.setSelection(0);
                        newfinger2.setSelection(0);
                        break;
                    case 1://first finger finger index 1
                        newfinger1.setEnabled(true);
                        newfinger2.setEnabled(false);
                        newfinger2.setSelection(0);
                        //EmployeeFingerEnrollInfo.getInstance().setNoOfFingers(1);
                        //EmployeeFingerEnrollInfo.getInstance().setFingerIndex(1);//
                        break;
                    case 2://second finger finger index 2
                        newfinger1.setSelection(0);
                        newfinger1.setEnabled(false);
                        newfinger2.setEnabled(true);
                        // EmployeeFingerEnrollInfo.getInstance().setNoOfFingers(1);
                        // EmployeeFingerEnrollInfo.getInstance().setFingerIndex(2);
                        break;
                    case 3://both finger finger index 3
                        newfinger1.setSelection(0);
                        newfinger2.setSelection(0);
                        newfinger1.setEnabled(true);
                        newfinger2.setEnabled(true);
                        // EmployeeFingerEnrollInfo.getInstance().setNoOfFingers(2);
                        // EmployeeFingerEnrollInfo.getInstance().setFingerIndex(3);
                        break;
                    default:
                        break;
                }
                searchView.clearFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
            }

            public void onNothingSelected(AdapterView <?> parent) {
            }
        });
        fingerIndexSpinner.setEnabled(EmployeeFingerEnrollInfo.getInstance().isUpdateTemplate());
    }

    public void onUpdateTemplateClicked(View view) {
        chkBoxUpdateTemplate = (CheckBox) view;
        fingerIndexSpinner = (Spinner) findViewById(R.id.fingerIndex);
        boolean isUpdateChecked = chkBoxUpdateTemplate.isChecked();
        empFingerInfo.setUpdateTemplate(isUpdateChecked);
        fingerIndexSpinner.setEnabled(isUpdateChecked);
        if (!chkBoxUpdateTemplate.isChecked()) {
            fingerIndexSpinner.setSelection(0);
            newfinger1.setSelection(0);
            newfinger2.setSelection(0);
            newfinger1.setEnabled(false);
            newfinger2.setEnabled(false);
        }
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    public void Reset(View view) {
        refresh();
    }

    public void empFullDetails(View view) {
        showFullEmployeeDetails();
    }

    private void showFullEmployeeDetails() {

        final Context context = this;
        final Dialog dialog = new Dialog(context);
        dialog.setCanceledOnTouchOutside(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.emp_details_custom_dialog);

        ImageView icon = (ImageView) dialog.findViewById(R.id.image);
        TextView title = (TextView) dialog.findViewById(R.id.title);
        TextView empId = (TextView) dialog.findViewById(R.id.EmployeeID);
        TextView cardId = (TextView) dialog.findViewById(R.id.CardID);
        TextView empName = (TextView) dialog.findViewById(R.id.Name);
        TextView mobileNo = (TextView) dialog.findViewById(R.id.Mobile);
        TextView bloodGroup = (TextView) dialog.findViewById(R.id.BloodGroup);
        TextView dateOfBirth = (TextView) dialog.findViewById(R.id.DOB);
        TextView mailId = (TextView) dialog.findViewById(R.id.Email);
        TextView validUpto = (TextView) dialog.findViewById(R.id.ValidUpto);
        TextView pin = (TextView) dialog.findViewById(R.id.PIN);
        TextView groupId = (TextView) dialog.findViewById(R.id.GroupName);
        TextView siteCode = (TextView) dialog.findViewById(R.id.SiteCode);
        TextView enrollStatus = (TextView) dialog.findViewById(R.id.enrollstatus);
        TextView noOfFingers = (TextView) dialog.findViewById(R.id.nosfinger);
        TextView smardCardVer = (TextView) dialog.findViewById(R.id.SmartCardVersion);
        ImageView photo = (ImageView) dialog.findViewById(R.id.U_image);

        Button btn_Ok = (Button) dialog.findViewById(R.id.btnOk);
        icon.setImageResource(R.drawable.success);
        title.setText("Employee Details");

        String strEmpId = editTextEmpID.getText().toString();
        if (strEmpId.trim().length() >= 1) {
            EmployeeEnrollInfo employeeEnrollInfo = null;
            employeeEnrollInfo = dbComm.getEmployeeBasicDetails(strEmpId, employeeEnrollInfo);
            if (employeeEnrollInfo != null) {
                String value;
                value = employeeEnrollInfo.getEmpId();
                if (value != null && value.trim().length() > 0) {
                    empId.setText(value);
                }
                value = employeeEnrollInfo.getCardId();
                if (value != null && value.trim().length() > 0) {
                    cardId.setText(value.replaceAll("\\G0", " ").trim());
                }
                value = employeeEnrollInfo.getEmpName();
                if (value != null && value.trim().length() > 0) {
                    empName.setText(value);
                }
                value = employeeEnrollInfo.getBloodGroup();
                if (value != null && value.trim().length() > 0) {
                    bloodGroup.setText(value);
                }
                value = employeeEnrollInfo.getMobileNo();
                if (value != null && value.trim().length() > 0) {
                    mobileNo.setText(value);
                }
                value = employeeEnrollInfo.getEmailId();
                if (value != null && value.trim().length() > 0) {
                    mailId.setText(value);
                }
                value = employeeEnrollInfo.getPin();
                if (value != null && value.trim().length() > 0) {
                    pin.setText(value);
                }
                value = employeeEnrollInfo.getValidUpto();
                if (value != null && value.trim().length() > 0) {
                    validUpto.setText(value);
                }
                value = employeeEnrollInfo.getDateOfBirth();
                if (value != null && value.trim().length() > 0) {
                    dateOfBirth.setText(value);
                }
                value = employeeEnrollInfo.getSmartCardVer();
                if (value != null && value.trim().length() > 0) {
                    smardCardVer.setText(value);
                }
                byte[] imageData = employeeEnrollInfo.getPhoto();
                if (imageData != null) {
                    photo.setImageBitmap(BitmapFactory.decodeByteArray(imageData, 0, imageData.length));
                } else {
                    photo.setImageResource(R.drawable.dummyphoto);
                }

                boolean status = employeeEnrollInfo.isFingerEnrolled();
                if (status) {
                    enrollStatus.setText("Yes");
                } else {
                    enrollStatus.setText("No");
                }
                int noOfFingersEnrolled = employeeEnrollInfo.getNoOfFingersEnrolled();
                if (noOfFingersEnrolled > 0) {
                    noOfFingers.setText(Integer.toString(noOfFingersEnrolled));
                } else {
                    noOfFingers.setText("NA");
                }
                value = employeeEnrollInfo.getSiteCode();
                if (value != null && value.trim().length() > 0) {
                    String sc = dbComm.getSiteCodeById(value);
                    siteCode.setText(sc);
                }
                value = employeeEnrollInfo.getGroupId();
                if (value != null && value.trim().length() > 0) {
                    String strGroupId = dbComm.getGroupNameById(value);
                    groupId.setText(strGroupId);
                }
            }
        }

        btn_Ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                searchView.clearFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
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


    public final void enrollUpdate(View view) {

        int fingerReaderVal = Settings.getInstance().getFrTypeValue();
        switch (fingerReaderVal) {
            case 0://Morpho Finger Reader
//                if (ProcessInfo.getInstance().isStarted()) {
//                    this.stop(false);
//                } else {
                morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
                morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
                if (morphoDevice != null && morphoDatabase != null) {
                    String strButtonText = btnFingerEnroll.getText().toString();
                    if (strButtonText.equalsIgnoreCase("Enroll Finger")) {
                        boolean isValid = false;
                        isValid = validateBasicInfo();
                        if (isValid) {
                            fillFingerEnrollToSkeleton();
                            isValid = validateSpinnerCheck(1);
                            if (isValid) {
                                empFingerInfo.setEnroll(true);
                                empFingerInfo.setOperation(1);//1 For Enroll Template
                                String strNoOfFingers = "";
                                int noOfFingers = empFingerInfo.getNoOfFingers();
                                if (noOfFingers == 1) {
                                    strNoOfFingers = "One Finger";
                                } else if (noOfFingers == 2) {
                                    strNoOfFingers = "Two Fingers";
                                }
                                showFingerViewCustomDialog(true, "Finger Enrollment", "Do You Want To Enroll " + strNoOfFingers + " ?");
                            }
                        }
                    } else if (strButtonText.equalsIgnoreCase("Update Finger")) {
                        boolean isValid = false;
                        isValid = validateBasicInfo();
                        if (isValid) {
                            fillFingerEnrollToSkeleton();
                            isValid = validateSpinnerCheck(1);
                            if (isValid) {
                                empFingerInfo.setEnroll(false);
                                empFingerInfo.setOperation(1);//1 For Enroll Template
                                String strNoOfFingers = "";
                                int noOfFingers = empFingerInfo.getNoOfFingers();
                                if (noOfFingers == 1) {
                                    strNoOfFingers = "One Finger";
                                } else if (noOfFingers == 2) {
                                    strNoOfFingers = "Two Fingers";
                                }
                                showFingerViewCustomDialog(true, "Finger Updation", "Do You Want To Update " + strNoOfFingers + " ?");
                            }
                        }
                    }
                } else {
                    showCustomAlertDialog(false, "Device Connection Status", "Please Connect Finger Reader");
                }

                break;

            case 1://Aratek Finger Reader

                Toast.makeText(EmployeeFingerEnrollmentActivity.this, "Aratek Finger Reader Not Found", Toast.LENGTH_LONG).show();

                break;

            case 2://Startek Finger Reader

                FM220SDK = StarkTekConnection.getInstance().getFM220SDK();
                if (FM220SDK != null && FM220SDK.FM220Initialized()) {
                    String strButtonText = btnFingerEnroll.getText().toString();
                    if (strButtonText.equalsIgnoreCase("Enroll Finger")) {
                        boolean isValid = false;
                        isValid = validateBasicInfo();
                        if (isValid) {
                            fillFingerEnrollToSkeleton();
                            isValid = validateSpinnerCheck(1);
                            if (isValid) {
                                empFingerInfo.setOperation(1);//1 For Enroll Template
                                String strNoOfFingers = "";
                                int noOfFingers = empFingerInfo.getNoOfFingers();
                                if (noOfFingers == 1) {
                                    strNoOfFingers = "One Finger";
                                } else if (noOfFingers == 2) {
                                    strNoOfFingers = "Two Fingers";
                                }
                                showFingerViewCustomDialog(true, "Finger Enrollment", "Do You Want To Enroll " + strNoOfFingers + " ?");
                            }
                        }
                    } else if (strButtonText.equalsIgnoreCase("Update Finger")) {
                        boolean isValid = false;
                        isValid = validateBasicInfo();
                        if (isValid) {
                            fillFingerUpdateToSkeleton();
                            isValid = validateSpinnerCheck(2);
                            if (isValid) {
                                empFingerInfo.setOperation(2);//2 For Update Template
                                String strNoOfFingers = "";
                                int noOfFingers = empFingerInfo.getNoOfFingers();
                                if (noOfFingers == 1) {
                                    strNoOfFingers = "One Finger";
                                } else if (noOfFingers == 2) {
                                    strNoOfFingers = "Two Fingers";
                                }
                                showFingerViewCustomDialog(true, "Finger Updation", "Do You Want To Update " + strNoOfFingers + " ?");
                            }
                        }
                    }
                } else {
                    showCustomAlertDialog(false, "Device Connection Status", "Startek Finger Reader Not Initialized");
                }

                break;

            default:
                break;
        }
    }

    private void fillFingerEnrollToSkeleton() {
        String firstFingerIndex = finger1.getSelectedItem().toString();
        String secondFingerIndex = finger2.getSelectedItem().toString();
        String vm = verificationMode.getSelectedItem().toString();
        String sl = securityLevel.getSelectedItem().toString();
        if (firstFingerIndex != null && !firstFingerIndex.trim().equals("Select")) {
            empFingerInfo.getInstance().setStrFirstFingerIndex(firstFingerIndex);
        } else {
            empFingerInfo.getInstance().setStrFirstFingerIndex("");
        }
        if (secondFingerIndex != null && !secondFingerIndex.trim().equals("Select")) {
            empFingerInfo.getInstance().setStrSecondFingerIndex(secondFingerIndex);
        } else {
            empFingerInfo.getInstance().setStrSecondFingerIndex("");
        }
        if (vm != null && !vm.trim().equals("Select")) {
            empFingerInfo.getInstance().setStrVerificationMode(vm);
        } else {
            empFingerInfo.getInstance().setStrVerificationMode("");
        }
        if (sl != null && !sl.trim().equals("Select")) {
            empFingerInfo.getInstance().setStrSecurityLevel(sl);
        } else {
            empFingerInfo.getInstance().setStrSecurityLevel("");
        }
    }

    private void fillFingerUpdateToSkeleton() {
        String newFirstFingerIndex = newfinger1.getSelectedItem().toString();
        String newSecondFingerIndex = newfinger2.getSelectedItem().toString();
        if (newFirstFingerIndex != null && !newFirstFingerIndex.trim().equals("Select")) {
            empFingerInfo.getInstance().setStrNewFirstFingerIndex(newFirstFingerIndex);
        } else {
            empFingerInfo.getInstance().setStrNewFirstFingerIndex("");
        }
        if (newSecondFingerIndex != null && !newSecondFingerIndex.trim().equals("Select")) {
            empFingerInfo.getInstance().setStrNewSecondFingerIndex(newSecondFingerIndex);
        } else {
            empFingerInfo.getInstance().setStrNewSecondFingerIndex("");
        }
        String fingerIndexType = fingerIndexSpinner.getSelectedItem().toString();
        if (fingerIndexType != null && fingerIndexType.trim().equals("First Finger")) {
            empFingerInfo.getInstance().setNoOfFingers(1);
            empFingerInfo.getInstance().setFingerIndex(1);
        } else if (fingerIndexType != null && fingerIndexType.trim().equals("Second Finger")) {
            empFingerInfo.getInstance().setNoOfFingers(1);
            empFingerInfo.getInstance().setFingerIndex(2);
        } else if (fingerIndexType != null && fingerIndexType.trim().equals("Both")) {
            empFingerInfo.getInstance().setNoOfFingers(2);
            empFingerInfo.getInstance().setFingerIndex(3);
        }
        empFingerInfo.setStrVerificationMode(txtViewVerificationMode.getText().toString());
        empFingerInfo.setStrSecurityLevel(txtViewSecurityLevel.getText().toString());
    }

    private boolean validateBasicInfo() {
        String value;
        value = empFingerInfo.getEmpId();
        if (value != null && value.trim().length() == 0) {
            showCustomAlertDialog(false, "Error", "Employee Id Not Found");
            return false;
        }
        value = empFingerInfo.getCardId();
        if (value != null && value.trim().length() == 0) {
            showCustomAlertDialog(false, "Error", "Card Id Not Found");
            return false;
        }
        value = empFingerInfo.getEmpName();
        if (value != null && value.trim().length() == 0) {
            showCustomAlertDialog(false, "Error", "Employee Name Not Found");
            return false;
        }
        return true;
    }

    public boolean validateSpinnerCheck(String fingerType) {
        int checkedRbId = noOfFingers.getCheckedRadioButtonId();
        if (checkedRbId == R.id.onefingerEnroll) {
            if (fingerType.trim().length() > 0 && fingerType.equals("F1")) {
                if (empFingerInfo.getStrFirstFingerIndex().trim().length() == 0) {
                    showCustomAlertDialog(false, "Error", "Select First Finger Index");
                    return false;
                }
            } else if (fingerType.trim().length() > 0 && fingerType.equals("F2")) {
                if (empFingerInfo.getStrSecondFingerIndex().trim().length() == 0) {
                    showCustomAlertDialog(false, "Error", "Select Second Finger Index");
                    return false;
                }
            }
            if (empFingerInfo.getStrSecurityLevel().trim().length() == 0) {
                showCustomAlertDialog(false, "Error", "Select Security Level");
                return false;
            }
            if (empFingerInfo.getStrVerificationMode().trim().length() == 0) {
                showCustomAlertDialog(false, "Error", "Select Verification Mode");
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    public boolean validateSpinnerCheck(int mode) {//1 Enroll 2 Update
        //Validate spinner before Enroll operation
        if (mode == 1) {
            int checkedRbId = noOfFingers.getCheckedRadioButtonId();
            if (checkedRbId == R.id.onefingerEnroll) {
                if (empFingerInfo.getStrFirstFingerIndex().trim().length() == 0) {
                    showCustomAlertDialog(false, "Error", "Select First Finger Index");
                    return false;
                }
                if (empFingerInfo.getStrSecurityLevel().trim().length() == 0) {
                    showCustomAlertDialog(false, "Error", "Select Security Level");
                    return false;
                }
                if (empFingerInfo.getStrVerificationMode().trim().length() == 0) {
                    showCustomAlertDialog(false, "Error", "Select Verification Mode");
                    return false;
                }
                return true;
            } else if (checkedRbId == R.id.twofingerEnroll) {
                if (empFingerInfo.getStrFirstFingerIndex().trim().length() == 0) {
                    showCustomAlertDialog(false, "Error", "Select First Finger Index");
                    return false;
                }
                if (empFingerInfo.getStrFirstFingerIndex().trim().equals(empFingerInfo.getStrSecondFingerIndex().trim())) {
                    showCustomAlertDialog(false, "Error", "Duplicate Finger Index");
                    return false;
                }
                if (empFingerInfo.getStrSecondFingerIndex().trim().length() == 0) {
                    showCustomAlertDialog(false, "Error", "Select Second Finger Index");
                    return false;
                }
                if (empFingerInfo.getStrFirstFingerIndex().trim().equals(empFingerInfo.getStrSecondFingerIndex().trim())) {
                    showCustomAlertDialog(false, "Error", "Duplicate Finger Index");
                    return false;
                }
                if (empFingerInfo.getStrSecurityLevel().trim().length() == 0) {
                    showCustomAlertDialog(false, "Error", "Select Security Level");
                    return false;
                }
                if (empFingerInfo.getStrVerificationMode().trim().length() == 0) {
                    showCustomAlertDialog(false, "Error", "Select Verification Mode");
                    return false;
                }
                return true;
            } else {
                return false;
            }
            //Validate spinner before Update operation
        } else if (mode == 2) {
            boolean chkBoxStatus = chkBoxUpdateTemplate.isChecked();
            if (!chkBoxStatus) {
                showCustomAlertDialog(false, "Error", "Select Update Template");
                return false;
            }
            String strFingerIndex = fingerIndexSpinner.getSelectedItem().toString();
            if (strFingerIndex.equals("Select")) {
                showCustomAlertDialog(false, "Error", "Select Finger Index Type");
                return false;
            }
            if (strFingerIndex.equals("First Finger")) {
                if (empFingerInfo.getStrNewFirstFingerIndex().trim().length() == 0) {
                    showCustomAlertDialog(false, "Error", "Select First Finger Index");
                    return false;
                }
            }
            if (strFingerIndex.equals("Second Finger")) {
                if (empFingerInfo.getStrNewSecondFingerIndex().trim().length() == 0) {
                    showCustomAlertDialog(false, "Error", "Select Second Finger Index");
                    return false;
                }
            }
            if (strFingerIndex.equals("Both")) {
                if (empFingerInfo.getStrNewFirstFingerIndex().trim().length() == 0) {
                    showCustomAlertDialog(false, "Error", "Select First Finger Index");
                    return false;
                }
                if (empFingerInfo.getStrNewSecondFingerIndex().trim().length() == 0) {
                    showCustomAlertDialog(false, "Error", "Select Second Finger Index");
                    return false;
                }
                if (empFingerInfo.getStrNewFirstFingerIndex().trim().equals(empFingerInfo.getStrNewSecondFingerIndex().trim())) {
                    showCustomAlertDialog(false, "Error", "Duplicate Finger Index");
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public void showCustomAlertDialog(boolean status, String strTitle, String strMessage) {

        final Context context = this;
        final Dialog dialog = new Dialog(context);
        dialog.setCanceledOnTouchOutside(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_alert_dialog);

        ImageView icon = (ImageView) dialog.findViewById(R.id.image);
        TextView title = (TextView) dialog.findViewById(R.id.title);
        TextView message = (TextView) dialog.findViewById(R.id.message);
        Button btn_Ok = (Button) dialog.findViewById(R.id.btnOk);

        if (status == true) {
            icon.setImageResource(R.drawable.success);
        } else {
            icon.setImageResource(R.drawable.failure);
        }

        title.setText(strTitle);
        message.setText(strMessage);


        btn_Ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                searchView.clearFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
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

    AlertDialog alert = null;

    private void showCustomAlertDialog(int iconId, final String strTitle, final String strMessage, final boolean isProcessFinished) {
        AlertDialog.Builder builder = new AlertDialog.Builder(EmployeeFingerEnrollmentActivity.this);
        builder.setMessage(strMessage).setIcon(iconId)
                .setCancelable(false).setTitle(strTitle)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogs, int id) {
                        if (isProcessFinished) {
                            alert.dismiss();
                            dialog.dismiss();
                            stopADCReceiver();
                            Intent intent = new Intent(EmployeeFingerEnrollmentActivity.this, EmployeeFingerEnrollmentActivity.class);
                            startActivity(intent);
                            finish();
                            // startActivity(getIntent());
                        }
                    }
                });
        alert = builder.create();
        alert.show();
    }


    public void showFingerViewCustomDialog(boolean status, String strTitle, String strMessage) {

        final Context context = this;
        final Dialog dialog = new Dialog(context);
        dialog.setCanceledOnTouchOutside(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_confirm_dialog);

        ImageView icon = (ImageView) dialog.findViewById(R.id.image);
        TextView title = (TextView) dialog.findViewById(R.id.title);
        TextView message = (TextView) dialog.findViewById(R.id.message);
        Button btn_No = (Button) dialog.findViewById(R.id.btnNo);
        Button btn_Yes = (Button) dialog.findViewById(R.id.btnYes);

        if (status == true) {
            icon.setImageResource(R.drawable.success);
        } else {
            icon.setImageResource(R.drawable.failure);
        }

        title.setText(strTitle);
        message.setText(strMessage);

        btn_No.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });


        btn_Yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                int fingerType = Settings.getInstance().getFrTypeValue();
                switch (fingerType) {
                    case 0:// Morpho Finger Reader
                        int operation = empFingerInfo.getOperation();
                        switch (operation) {
                            case 1://Enroll Finger
                                String strButtonText = btnFingerEnroll.getText().toString();
                                if (strButtonText.equalsIgnoreCase("Enroll Finger")) {
                                    Intent startEnrollProcess = new Intent(EmployeeFingerEnrollmentActivity.this, FingerEnrollUpdateDialogActivity.class);
                                    Bundle bundleEnroll = new Bundle();
                                    bundleEnroll.putString("ET", "LE");
                                    bundleEnroll.putString("JID", "");
                                    bundleEnroll.putBoolean("IE", empFingerInfo.isEnroll());//IsEnroll
                                    bundleEnroll.putInt("OP", empFingerInfo.getOperation());
                                    bundleEnroll.putInt("NOF", empFingerInfo.getNoOfFingers());
                                    bundleEnroll.putString("EID", empFingerInfo.getEmpId());
                                    bundleEnroll.putString("CID", empFingerInfo.getCardId());
                                    bundleEnroll.putString("EN", empFingerInfo.getEmpName());
                                    bundleEnroll.putString("FFI", empFingerInfo.getStrFirstFingerIndex());
                                    bundleEnroll.putString("SFI", empFingerInfo.getStrSecondFingerIndex());
                                    bundleEnroll.putString("SL", empFingerInfo.getStrSecurityLevel());
                                    bundleEnroll.putString("VM", empFingerInfo.getStrVerificationMode());
                                    startActivity(startEnrollProcess);
                                } else if (strButtonText.equalsIgnoreCase("Update Finger")) {
                                    int ret = MorphoCommunicator.deleteMorphoUser(empFingerInfo.getEmpId());
                                    if (ret == 0) {
                                        int autoId = dbComm.getAutoIdByEmpId(empFingerInfo.getEmpId());
                                        if (autoId != -1) {
                                            ret = dbComm.deleteFingerRecordByAutoId(autoId);
                                            if (ret != -1) {
                                                ret = dbComm.updateFingerDataToEmpTable(autoId);
                                                if (ret != -1) {
                                                    Intent startEnrollProcess = new Intent(EmployeeFingerEnrollmentActivity.this, FingerEnrollUpdateDialogActivity.class);
                                                    Bundle bundleEnroll = new Bundle();
                                                    bundleEnroll.putString("ET", "LE");
                                                    bundleEnroll.putString("JID", "");
                                                    bundleEnroll.putBoolean("IE", empFingerInfo.isEnroll());//IsEnroll
                                                    bundleEnroll.putInt("OP", empFingerInfo.getOperation());
                                                    bundleEnroll.putInt("NOF", empFingerInfo.getNoOfFingers());
                                                    bundleEnroll.putString("EID", empFingerInfo.getEmpId());
                                                    bundleEnroll.putString("CID", empFingerInfo.getCardId());
                                                    bundleEnroll.putString("EN", empFingerInfo.getEmpName());
                                                    bundleEnroll.putString("FFI", empFingerInfo.getStrFirstFingerIndex());
                                                    bundleEnroll.putString("SFI", empFingerInfo.getStrSecondFingerIndex());
                                                    bundleEnroll.putString("SL", empFingerInfo.getStrSecurityLevel());
                                                    bundleEnroll.putString("VM", empFingerInfo.getStrVerificationMode());
                                                    startActivity(startEnrollProcess);
                                                } else {
                                                    showCustomAlertDialog(false, "Status", "Failed to update finger enroll status to employee table");
                                                }
                                            } else {
                                                showCustomAlertDialog(false, "Status", "Sqlite Delete Failure");
                                            }
                                        }
                                    } else {
                                        showCustomAlertDialog(false, "Status", "Morpho Delete Failure");
                                    }
                                }
                                break;
                            case 2://Update Finger
                                Intent startUpdateProcess = new Intent(EmployeeFingerEnrollmentActivity.this, FingerEnrollUpdateDialogActivity.class);
                                Bundle bundleUpdate = new Bundle();
                                bundleUpdate.putString("ET", "LE");
                                bundleUpdate.putString("JID", "");
                                bundleUpdate.putInt("OP", empFingerInfo.getOperation());
                                bundleUpdate.putInt("NOF", empFingerInfo.getNoOfFingers());
                                bundleUpdate.putString("EID", empFingerInfo.getEmpId());
                                bundleUpdate.putString("CID", empFingerInfo.getCardId());
                                bundleUpdate.putString("EN", empFingerInfo.getEmpName());
                                bundleUpdate.putString("NFFI", empFingerInfo.getStrNewFirstFingerIndex());
                                bundleUpdate.putString("NSFI", empFingerInfo.getStrNewSecondFingerIndex());
                                bundleUpdate.putInt("FIN", empFingerInfo.getFingerIndex());
                                startActivity(startUpdateProcess);
                                break;
                        }
                        break;
                    case 1:
                        break;
                    case 2:// Startek Finger Reader
                        operation = empFingerInfo.getOperation();
                        switch (operation) {
                            case 1://Enroll Finger
                                showStartekFingerEnrollCustomDialog();
                                break;
                            case 2://Update Finger
                                break;
                        }
                        break;
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

    public void showSecondFingerCaptureCustomDialog(boolean status, String strTitle, String strMessage) {

        final Context context = this;
        final Dialog dialog = new Dialog(context);
        dialog.setCanceledOnTouchOutside(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_alert_dialog_ok);

        ImageView icon = (ImageView) dialog.findViewById(R.id.image);
        TextView title = (TextView) dialog.findViewById(R.id.title);
        TextView message = (TextView) dialog.findViewById(R.id.message);
        Button btn_Ok = (Button) dialog.findViewById(R.id.btnOk);

        if (status == true) {
            icon.setImageResource(R.drawable.success);
        } else {
            icon.setImageResource(R.drawable.failure);
        }

        title.setText(strTitle);
        message.setText(strMessage);


        btn_Ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                btnSecondFingerEnroll.setBackgroundColor(Color.parseColor("#e63900"));
                btnSecondFingerEnroll.setTextColor(Color.parseColor("#FFFFFF"));
                btnSecondFingerEnroll.setEnabled(true);
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_view_all_employee, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.home:
                stopADCReceiver();
                Intent previous = new Intent(this, HomeActivity.class);
                startActivity(previous);
                overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
                finish();
                return true;
            case R.id.refresh:
                refreshLayout();
                initSearchView();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initializeTimerTask() {

        batReadTimerTask = new TimerTask() {
            public void run() {
                bHandler.post(new Runnable() {
                    public void run() {
                        char[] data = ForlinxGPIOCommunicator.readGPIO(Constants.CHARGE_DETECT);
                        if (data != null) {
                            String val = new String(data);
                            if (val != null) {
                                val = val.trim();
                                if (val.length() > 0) {
                                    if (val.equals("1")) {
                                        tvBatPer.setVisibility(View.GONE);
                                        ivBatTop.setVisibility(View.GONE);
                                        pbBatPer.setVisibility(View.GONE);
                                        tvPower.setVisibility(View.VISIBLE);
                                        ivChargeIcon.setVisibility(View.VISIBLE);
                                    } else if (val.equals("0")) {
                                        tvBatPer.setVisibility(View.VISIBLE);
                                        pbBatPer.setVisibility(View.VISIBLE);
                                        ivBatTop.setVisibility(View.VISIBLE);
                                        tvPower.setVisibility(View.GONE);
                                        ivChargeIcon.setVisibility(View.GONE);
                                        pbBatPer.setProgress(per);
                                        tvBatPer.setText("" + per + "%");
                                    }
                                }
                            }
                        }
                    }
                });
            }
        };

        capReadTimerTask = new TimerTask() {
            public void run() {
                cHandler.post(new Runnable() {
                    public void run() {
                        char[] val = I2CCommunicator.readI2C(Constants.CAP_READ_PATH);
                        if (val != null && val.length > 0) {
                            String capVal = new String(val);
                            capVal = capVal.trim();
                            switch (capVal) {
                                case "36":
                                    if (isBreakFound) {
                                        isBreakFound = false;
                                        if (isLCDBackLightOff) {
                                            stopHandler();//stop first and then start
                                            startHandler();
                                            ForlinxGPIO.setLCDBackLightOn();
                                            setScreenBrightness(Constants.BRIGHTNESS_ON);
                                            isLCDBackLightOff = false;
                                        } else {
                                            stopADCReceiver();
                                            Intent intent = new Intent(EmployeeFingerEnrollmentActivity.this, EmployeeAttendanceActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }
                                    break;
                                case "63":
                                    if (isBreakFound) {
                                        isBreakFound = false;
                                        if (isLCDBackLightOff) {
                                            stopHandler();//stop first and then start
                                            startHandler();
                                            ForlinxGPIO.setLCDBackLightOn();
                                            setScreenBrightness(Constants.BRIGHTNESS_ON);
                                            isLCDBackLightOff = false;
                                        } else {
                                            stopADCReceiver();
                                            Intent intent = new Intent(EmployeeFingerEnrollmentActivity.this, HomeActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }
                                    break;
                                case "33":
                                    break;
                                case "66":
                                    break;
                                case "ff":
                                    isBreakFound = true;
                                    break;
                            }
                        }
                    }
                });
            }
        };


        adcReadTimerTask = new TimerTask() {
            public void run() {
                adcHandler.post(new Runnable() {
                    public void run() {
                        if (index < numArray.length) {
                            numArray[index++] = adcValue;
                            if (index == numArray.length) {
                                calculateSD(numArray);
                                index = 0;
                            }
                        }
                    }
                });
            }
        };
    }

    public double calculateSD(double numArray[]) {
        double sum = 0.0, standardDeviation = 0.0;
        int length = numArray.length;
        for (double num : numArray) {
            sum += num;
        }
        double mean = sum / length;
        for (double num : numArray) {
            standardDeviation += Math.pow(num - mean, 2);
        }
        double sd = Math.sqrt(standardDeviation / length);
        if (prevMean == 0.0) {
            prevMean = mean;
        } else {
            if (sd < 40.0) {
                prevMean = mean;
            }
        }
        isSDCalculated = true;
        return sd;
    }

    public void registerBroadCastReceiver(int usbReader) {

        HandlerThread handlerThread = new HandlerThread("BroadCastReceiverThread", android.os.Process.THREAD_PRIORITY_BACKGROUND);
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        Handler handler = new Handler(looper);

        switch (usbReader) {

            case 0:

                //==================================== USB Smart Reader ================================================//

                if (!isSmartRcvRegisterd) {
                    isSmartRcvRegisterd = true;
                    registerReceiver(mSmartReceiver, filter, null, handler);
                }

                break;

            case 1:

                //==================================== Morpho Finger Reader ================================================//

                if (!isMorphoRcvRegistered) {
                    isMorphoRcvRegistered = true;
                    registerReceiver(mMorphoReceiver, filter, null, handler);
                }

                break;

            case 2:

                //==================================== USB Finger And Smart Reader ================================================//

                if (!isMorphoSmartRcvRegisterd) {
                    isMorphoSmartRcvRegisterd = true;
                    registerReceiver(mMorphoSmartReceiver, filter, null, handler);
                }

                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        isFingerEnrollmentWindowVisisble = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isFingerEnrollmentWindowVisisble = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        stopHandler();
        startHandler();
        startTimer();
    }

    public void startTimer() {
        if (capReadTimer == null && batReadTimer == null && adcReadTimer == null) {
            capReadTimer = new Timer();
            batReadTimer = new Timer();
            adcReadTimer = new Timer();
            initializeTimerTask();
            capReadTimer.schedule(capReadTimerTask, 0, 50);
            batReadTimer.schedule(batReadTimerTask, 0, 500); //
            adcReadTimer.schedule(adcReadTimerTask, 0, 50); //1000
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        stopHandler();
        stopTimer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceivers();
        stopADCReceiver();
        if (isStartekRcvRegisterd) {
            try {
                if (mStartekReceiver != null) {
                    unregisterReceiver(mStartekReceiver);
                    isStartekRcvRegisterd = false;
                    mStartekReceiver = null;
                }
            } catch (Exception e) {
                Toast.makeText(EmployeeFingerEnrollmentActivity.this, "error in unregister startek receiver:" + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
        LoginSplashActivity.isLoaded = false;
    }

    private void unregisterReceivers() {

        if (isSmartRcvRegisterd) {
            try {
                if (mSmartReceiver != null) {
                    unregisterReceiver(mSmartReceiver);
                    isSmartRcvRegisterd = false;
                    mSmartReceiver = null;
                }
            } catch (Exception e) {
                Toast.makeText(this, "error in unregister morpho smart receiver:" + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        if (isMorphoRcvRegistered) {
            try {
                if (mMorphoReceiver != null) {
                    unregisterReceiver(mMorphoReceiver);
                    isMorphoRcvRegistered = false;
                    mMorphoReceiver = null;
                }
            } catch (Exception e) {
                Toast.makeText(this, "error in unregister morpho receiver:" + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        if (isMorphoSmartRcvRegisterd) {
            try {
                if (mMorphoSmartReceiver != null) {
                    unregisterReceiver(mMorphoSmartReceiver);
                    isMorphoSmartRcvRegisterd = false;
                    mMorphoSmartReceiver = null;
                }
            } catch (Exception e) {
                Toast.makeText(this, "error in unregister morpho smart receiver:" + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }


    private void stopADCReceiver() {
        if (!isADCReceiverUnregistered) {
            isADCReceiverUnregistered = true;
            if (intent != null) {
                EmployeeFingerEnrollmentActivity.this.stopService(intent);
            }
            if (receiver != null) {
                unregisterReceiver(receiver);
            }
        }
    }

    private void stopTimer() {
        if (batReadTimer != null) {
            batReadTimer.cancel();
            batReadTimer.purge();
            batReadTimer = null;
        }

        if (capReadTimer != null) {
            capReadTimer.cancel();
            capReadTimer.purge();
            capReadTimer = null;
        }

        if (adcReadTimer != null) {
            adcReadTimer.cancel();
            adcReadTimer.purge();
            adcReadTimer = null;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!FingerEnrollUpdateDialogActivity.isEnrollStarted) {
                stopADCReceiver();
                Intent menu = new Intent(EmployeeFingerEnrollmentActivity.this, HomeActivity.class);
                startActivity(menu);
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    //============================ Startek Implementation Methods ============================//

    ImageView firstFingerimageView;
    ImageView secondFingerimageView;
    Button btnFirstFingerEnroll;
    Button btnSecondFingerEnroll;
    TextView tvSensorMsg;
    byte[] t1, t2, image1, image2;
    boolean isFirstFingerEnrolled = false;

    Dialog dialog = null;

    public void showStartekFingerEnrollCustomDialog() {

        final Context context = this;
        dialog = new Dialog(context);
        dialog.setCanceledOnTouchOutside(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        int noOfFingers = empFingerInfo.getNoOfFingers();
        if (noOfFingers == 1) {
            resetData();
            dialog.setContentView(R.layout.onefinger_customdialog_startek);
            TextView tvFingerIndex = (TextView) dialog.findViewById(R.id.finger1);
            String firstFingerIndex = empFingerInfo.getStrFirstFingerIndex().trim();
            if (firstFingerIndex.length() > 0) {
                tvFingerIndex.setText("Put Your " + firstFingerIndex);
            }
            firstFingerimageView = (ImageView) dialog.findViewById(R.id.imageView1);
            tvSensorMsg = (TextView) dialog.findViewById(R.id.textViewMessage);
            btnFirstFingerEnroll = (Button) dialog.findViewById(R.id.btnFirstFinger);
            btnFirstFingerEnroll.setEnabled(true);
            btnFirstFingerEnroll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FM220SDK.CaptureFM220(2, true, true);
                }
            });
        } else if (noOfFingers == 2) {
            resetData();
            dialog.setContentView(R.layout.twofinger_customdialog_startek);
            TextView tvFirstFingerIndex = (TextView) dialog.findViewById(R.id.finger1);
            TextView tvSecondFingerIndex = (TextView) dialog.findViewById(R.id.finger2);
            String firstFingerIndex = empFingerInfo.getStrFirstFingerIndex().trim();
            String secondFingerIndex = empFingerInfo.getStrSecondFingerIndex().trim();
            if (firstFingerIndex.length() > 0) {
                tvFirstFingerIndex.setText("Put Your " + firstFingerIndex);
            }
            if (secondFingerIndex.length() > 0) {
                tvSecondFingerIndex.setText("Put Your " + secondFingerIndex);
            }
            firstFingerimageView = (ImageView) dialog.findViewById(R.id.imageView1);
            secondFingerimageView = (ImageView) dialog.findViewById(R.id.imageView2);
            tvSensorMsg = (TextView) dialog.findViewById(R.id.textViewMessage);
            btnFirstFingerEnroll = (Button) dialog.findViewById(R.id.btnFirstFinger);
            btnSecondFingerEnroll = (Button) dialog.findViewById(R.id.btnSecondFinger);

            btnSecondFingerEnroll.setBackgroundColor(Color.parseColor("#D3D3D3"));
            btnSecondFingerEnroll.setTextColor(Color.parseColor("#000000"));
            btnSecondFingerEnroll.setEnabled(false);
            btnFirstFingerEnroll.setEnabled(true);
            btnFirstFingerEnroll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FM220SDK.CaptureFM220(2, true, true);
                }
            });

            btnSecondFingerEnroll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FM220SDK.CaptureFM220(2, true, true);
                }
            });
        }
        dialog.show();

        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        lp.dimAmount = 0.9f;
        lp.buttonBrightness = 1.0f;
        lp.screenBrightness = 1.0f;
        dialog.getWindow().setAttributes(lp);
        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }

    private void resetData() {
        t1 = null;
        t2 = null;
        image1 = null;
        image2 = null;
        isFirstFingerEnrolled = false;
    }

    @Override
    public void ScannerProgressFM220(final boolean DisplayImage, final Bitmap ScanImage, final boolean DisplayText, final String statusMessage) {
        FM220SDK = StarkTekConnection.getInstance().getFM220SDK();
        if (FM220SDK != null && FM220SDK.FM220Initialized()) {
            EmployeeFingerEnrollmentActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (DisplayText) {
                        tvSensorMsg.setText(statusMessage);
                        tvSensorMsg.invalidate();
                    }
                    if (DisplayImage) {
                        if (!isFirstFingerEnrolled) {
                            firstFingerimageView.setImageBitmap(ScanImage);
                            firstFingerimageView.invalidate();
                        } else {
                            secondFingerimageView.setImageBitmap(ScanImage);
                            secondFingerimageView.invalidate();
                        }
                    }

                }
            });
        }
    }

    @Override
    public void ScanCompleteFM220(final fm220_Capture_Result result) {
        FM220SDK = StarkTekConnection.getInstance().getFM220SDK();
        if (FM220SDK != null && FM220SDK.FM220Initialized()) {
            EmployeeFingerEnrollmentActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int noOfFingers = empFingerInfo.getNoOfFingers();
                    if (result.getResult()) {
                        if (noOfFingers == 1) {
                            if (t1 == null) {
                                firstFingerimageView.setImageBitmap(result.getScanImage());
                                t1 = result.getISO_Template();
                                empFingerInfo.setFirstFingerFMD(t1);
                                final StringBuilder builder = new StringBuilder();
                                for (byte b : t1) {
                                    builder.append(String.format("%02x", b));
                                }
                                String strFirstFingerDataHex = builder.toString().toUpperCase();
                                empFingerInfo.setStrFirstFingerDataHex(strFirstFingerDataHex);

                                Bitmap bitmap = result.getScanImage();
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                                image1 = baos.toByteArray();

                                empFingerInfo.setFirstFingerFID(image1);

                                enrollDialog("Finger Capture", "Finger Captured Successfully ! Do You Want To Save Finger Data?");
                            }
                        } else if (noOfFingers == 2) {
                            if (t1 == null) {
                                firstFingerimageView.setImageBitmap(result.getScanImage());
                                t1 = result.getISO_Template();

                                empFingerInfo.setFirstFingerFMD(t1);

                                final StringBuilder builder = new StringBuilder();
                                for (byte b : t1) {
                                    builder.append(String.format("%02x", b));
                                }
                                String strFirstFingerDataHex = builder.toString().toUpperCase();
                                empFingerInfo.setStrFirstFingerDataHex(strFirstFingerDataHex);


                                Bitmap bitmap = result.getScanImage();
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                                image1 = baos.toByteArray();
                                empFingerInfo.setFirstFingerFID(image1);


                                isFirstFingerEnrolled = true;
                                btnFirstFingerEnroll.setBackgroundColor(Color.parseColor("#D3D3D3"));
                                btnFirstFingerEnroll.setTextColor(Color.parseColor("#000000"));
                                btnFirstFingerEnroll.setEnabled(false);

                                showSecondFingerCaptureCustomDialog(true, "Finger Capture", "First Finger Captured Successfully ! Enroll Second Finger");

                            } else if (t2 == null) {
                                secondFingerimageView.setImageBitmap(result.getScanImage());
                                t2 = result.getISO_Template();

                                empFingerInfo.setSecondFingerFMD(t2);

                                final StringBuilder builder = new StringBuilder();
                                for (byte b : t2) {
                                    builder.append(String.format("%02x", b));
                                }
                                String strSecondFingerDataHex = builder.toString().toUpperCase();
                                empFingerInfo.setStrSecondFingerDataHex(strSecondFingerDataHex);

                                Bitmap bitmap = result.getScanImage();
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                                image2 = baos.toByteArray();

                                empFingerInfo.setSecondFingerFID(image2);

                                btnSecondFingerEnroll.setBackgroundColor(Color.parseColor("#D3D3D3"));
                                btnSecondFingerEnroll.setTextColor(Color.parseColor("#000000"));
                                btnSecondFingerEnroll.setEnabled(false);

                                enrollDialog("Finger Capture", "Finger Captured Successfully ! Do You Want To Save Finger Data?");
                            }
                        }
                        tvSensorMsg.setText("Success NFIQ:" + Integer.toString(result.getNFIQ()) + "  SrNo:" + result.getSerialNo());
                    } else {
                        if (noOfFingers == 1) {
                            if (t1 == null) {
                                firstFingerimageView.setImageBitmap(null);
                            }
                        } else if (noOfFingers == 2) {
                            if (t1 == null) {
                                firstFingerimageView.setImageBitmap(null);
                            } else if (t2 == null) {
                                secondFingerimageView.setImageBitmap(null);
                            }
                        }
                        tvSensorMsg.setText(result.getError());
                    }
                    if (noOfFingers == 1) {
                        firstFingerimageView.invalidate();
                        tvSensorMsg.invalidate();
                    } else if (noOfFingers == 2) {
                        firstFingerimageView.invalidate();
                        secondFingerimageView.invalidate();
                        tvSensorMsg.invalidate();
                    }
                }
            });

        }
    }

    @Override
    public void ScanMatchFM220(final fm220_Capture_Result _result) {
        //   EmployeeFingerEnrollmentActivity.this.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if (FM220SDK.FM220Initialized()) EnableCapture();
//                if (_result.getResult()) {
//                    imageView.setImageBitmap(_result.getScanImage());
//                    textMessage.setText("Finger matched\n" + "Success NFIQ:" + Integer.toString(_result.getNFIQ()));
//                } else {
//                    imageView.setImageBitmap(null);
//                    textMessage.setText("Finger not matched\n" + _result.getError());
//                }
//                imageView.invalidate();
//                textMessage.invalidate();
//            }
//
    }


    AlertDialog fingerEnrollAlert = null;

    private void enrollDialog(String title, String message) {

        AlertDialog.Builder builder = new AlertDialog.Builder(EmployeeFingerEnrollmentActivity.this);
        builder.setMessage(message).setTitle(title)
                .setIcon(R.drawable.success)
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        fingerEnrollAlert.dismiss();
                        EmployeeFingerEnrollInfo empFingerInfo = EmployeeFingerEnrollInfo.getInstance();
                        if (empFingerInfo != null) {
                            int empAutoId = -1;
                            SQLiteCommunicator dbComm = new SQLiteCommunicator();
                            String strVerificationMode = empFingerInfo.getStrVerificationMode();
                            int noOfFingers = empFingerInfo.getNoOfFingers();
                            switch (noOfFingers) {
                                case 1:
                                    empAutoId = dbComm.getAutoIdByEmpId(empFingerInfo.getEmpId());
                                    if (empAutoId != -1) {
                                        int empFingerId = -1;
                                        empFingerId = dbComm.insertOneTemplateToSqliteDb(empAutoId, "Startek");
                                        if (empFingerId != -1) {
                                            String enrollStatus = "Y";
                                            String isAadhaarVer = "N";
                                            int nof = dbComm.getNoFingersEnrolled(empAutoId);
                                            int status = dbComm.updateFingerDataToEmpTable(empAutoId, nof, enrollStatus, isAadhaarVer);
                                            if (status != -1) {
                                                ArrayList <StartekInfo> list = StartekDatabaseItems.getInstance().getDatabaseItemsList();
                                                if (list == null) {
                                                    list = new ArrayList <StartekInfo>();
                                                    StartekDatabaseItems.getInstance().setDatabaseItemsList(list);
                                                }
                                                list = StartekDatabaseItems.getInstance().getDatabaseItemsList();
                                                StartekInfo info = new StartekInfo();
                                                info.setAutoid(empAutoId);
                                                info.setTemplate(empFingerInfo.getFirstFingerFMD());
                                                list.add(info);
                                                StartekDatabaseItems.getInstance().setDatabaseItemsList(list);
                                                showCustomAlertDialog(R.drawable.success, "Finger Save Status", "Finger data saved successfully", true);
                                            } else {
                                                showCustomAlertDialog(R.drawable.failure, "Finger Save Status", "Failed to update finger data to employee table", true);
                                            }
                                        } else {
                                            showCustomAlertDialog(R.drawable.failure, "Finger Save Status", "Failed to insert finger data to finger table", true);
                                        }
                                    } else {
                                        showCustomAlertDialog(R.drawable.failure, "Finger Save Status", "Employee id not enrolled", true);
                                    }

                                    break;

                                case 2:
                                    empAutoId = dbComm.getAutoIdByEmpId(empFingerInfo.getEmpId());
                                    if (empAutoId != -1) {
                                        int empFingerId = -1;
                                        empFingerId = dbComm.insertTwoTemplatesToSqliteDb(empAutoId, "Startek");
                                        if (empFingerId != -1) {
                                            String enrollStatus = "Y";
                                            String isAadhaarVer = "N";
                                            int status = dbComm.updateFingerDataToEmpTable(empAutoId, enrollStatus, isAadhaarVer);
                                            if (status != -1) {
                                                ArrayList <StartekInfo> list = StartekDatabaseItems.getInstance().getDatabaseItemsList();
                                                if (list == null) {
                                                    list = new ArrayList <StartekInfo>();
                                                    StartekDatabaseItems.getInstance().setDatabaseItemsList(list);
                                                }
                                                list = StartekDatabaseItems.getInstance().getDatabaseItemsList();
                                                StartekInfo info = new StartekInfo();
                                                info.setAutoid(empAutoId);
                                                info.setTemplate(empFingerInfo.getFirstFingerFMD());
                                                list.add(info);
                                                info = new StartekInfo();
                                                info.setAutoid(empAutoId);
                                                info.setTemplate(empFingerInfo.getSecondFingerFMD());
                                                list.add(info);
                                                StartekDatabaseItems.getInstance().setDatabaseItemsList(list);
                                                showCustomAlertDialog(R.drawable.success, "Finger Save Status", "Finger data saved successfully", true);
                                            } else {
                                                showCustomAlertDialog(R.drawable.failure, "Finger Save Status", "Failed to update finger data to employee table", true);
                                            }
                                        } else {
                                            showCustomAlertDialog(R.drawable.failure, "Finger Save Status", "Failed to insert finger data to finger table", true);
                                        }
                                    } else {
                                        // showCustomAlertDialog(R.drawable.failure, "Finger Save Status", "Employee id not enrolled", true);
                                    }
                                    break;
                            }
                        } else {
                            // showCustomAlertDialog(R.drawable.failure, "Error", "Employee and finger details not found !", true);
                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        fingerEnrollAlert.dismiss();
                        Intent intent = new Intent(EmployeeFingerEnrollmentActivity.this, EmployeeFingerEnrollmentActivity.class);
                        EmployeeFingerEnrollmentActivity.this.startActivity(intent);
                    }
                });

        fingerEnrollAlert = builder.create();
        fingerEnrollAlert.setCanceledOnTouchOutside(false);
        fingerEnrollAlert.show();
    }


    //============================ Broadcast Receiver For Startek ===============================//

    public BroadcastReceiver mStartekReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Constants.ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(
                            UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            // call method to set up device communication
                            int pid, vid;
                            pid = device.getProductId();
                            vid = device.getVendorId();
                            if ((pid == 0x8225 || pid == 0x8220) && (vid == 0x0bca)) {
                                // acpl_FM220_SDK FM220SDK = StarkTekConnection.getInstance().getFM220SDK();
                                if (FM220SDK != null) {
                                    fm220_Init_Result res = FM220SDK.InitScannerFM220(usbManager, device, Telecom_Device_Key);
                                    if (res.getResult()) {
                                        StarkTekConnection.getInstance().setFM220SDK(FM220SDK);
                                        updateFrConStatusToUI(true);
                                    } else {
                                        updateFrConStatusToUI(false);
                                    }
                                }
                            }
                        }
                    } else {
                        Toast.makeText(EmployeeFingerEnrollmentActivity.this, "User Denied Permission for Finger Reader", Toast.LENGTH_LONG).show();
                    }
                }
            }
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (device != null) {
                        // call method to set up device communication
                        int pid, vid;
                        pid = device.getProductId();
                        vid = device.getVendorId();
                        if ((pid == 0x8225) && (vid == 0x0bca) && !FM220SDK.FM220isTelecom()) {
                            Toast.makeText(context, "Wrong device type application restart required!", Toast.LENGTH_LONG).show();
                            finish();
                        }
                        if ((pid == 0x8220) && (vid == 0x0bca) && FM220SDK.FM220isTelecom()) {
                            Toast.makeText(context, "Wrong device type application restart required!", Toast.LENGTH_LONG).show();
                            finish();
                        }

                        if ((pid == 0x8225 || pid == 0x8220) && (vid == 0x0bca)) {
                            if (!usbManager.hasPermission(device)) {
                                usbManager.requestPermission(device, mPermissionIntent);
                            } else {
                                if (FM220SDK != null) {
                                    fm220_Init_Result res = FM220SDK.InitScannerFM220(usbManager, device, Telecom_Device_Key);
                                    if (res.getResult()) {
                                        StarkTekConnection.getInstance().setFM220SDK(FM220SDK);
                                        updateFrConStatusToUI(true);
                                        Toast.makeText(EmployeeFingerEnrollmentActivity.this, "Startek Finger Reader Attached", Toast.LENGTH_LONG).show();
                                    } else {
                                        updateFrConStatusToUI(false);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                int pid, vid;
                pid = device.getProductId();
                vid = device.getVendorId();
                if ((pid == 0x8225 || pid == 0x8220) && (vid == 0x0bca)) {
                    if (FM220SDK != null) {
                        FM220SDK.stopCaptureFM220();
                        FM220SDK.unInitFM220();
                        updateFrConStatusToUI(false);
                        StarkTekConnection.getInstance().setFM220SDK(FM220SDK);
                        Toast.makeText(EmployeeFingerEnrollmentActivity.this, "Startek Finger Reader Detached", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    };


    public void updateFrConStatusToUI(final boolean status) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (status) {
                    finger_reader.setImageResource(R.drawable.correct);
                } else {
                    finger_reader.setImageResource(R.drawable.wrong);
                }
            }
        });

    }

    public void updateSrConStatusToUI(final boolean status) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (status) {
                    smart_reader.setImageResource(R.drawable.correct);
                } else {
                    smart_reader.setImageResource(R.drawable.wrong);
                }
            }
        });
    }

    @Override
    public void initIdentification() {
    }

    @Override
    public void initCardRead() {
    }

    @Override
    public void resetConnections() {
        ProcessInfo.getInstance().setMorphoDevice(null);
        ProcessInfo.getInstance().setMorphoDatabase(null);
        SmartReaderConnection.getInstance().setmConnection(null);
        SmartReaderConnection.getInstance().setIntf(null);
        SmartReaderConnection.getInstance().setInput(null);
        SmartReaderConnection.getInstance().setOutput(null);
    }
}
