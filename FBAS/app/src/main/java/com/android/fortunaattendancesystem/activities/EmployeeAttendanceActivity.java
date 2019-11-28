package com.android.fortunaattendancesystem.activities;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.acpl.access_computech_fm220_sdk.FM220_Scanner_Interface;
import com.acpl.access_computech_fm220_sdk.acpl_FM220_SDK;
import com.acpl.access_computech_fm220_sdk.fm220_Capture_Result;
import com.acpl.access_computech_fm220_sdk.fm220_Init_Result;
import com.android.fortunaattendancesystem.R;
import com.android.fortunaattendancesystem.constant.Constants;
import com.android.fortunaattendancesystem.extras.DataBaseLayer;
import com.android.fortunaattendancesystem.forlinx.ForlinxGPIO;
import com.android.fortunaattendancesystem.helper.Utility;
import com.android.fortunaattendancesystem.info.ProcessInfo;
import com.android.fortunaattendancesystem.model.EmployeeInfo;
import com.android.fortunaattendancesystem.model.SmartCardInfo;
import com.android.fortunaattendancesystem.model.WiegandSettingsInfo;
import com.android.fortunaattendancesystem.mqtt.MqttMessageService;
import com.android.fortunaattendancesystem.service.EzeeHrLiteCommunicator;
import com.android.fortunaattendancesystem.singleton.RC632ReaderConnection;
import com.android.fortunaattendancesystem.singleton.Settings;
import com.android.fortunaattendancesystem.singleton.SmartReaderConnection;
import com.android.fortunaattendancesystem.singleton.StarkTekConnection;
import com.android.fortunaattendancesystem.singleton.UserDetails;
import com.android.fortunaattendancesystem.submodules.ForlinxGPIOCommunicator;
import com.android.fortunaattendancesystem.submodules.I2CCommunicator;
import com.android.fortunaattendancesystem.submodules.MicroSmartV2Communicator;
import com.android.fortunaattendancesystem.submodules.MorphoCommunicator;
import com.android.fortunaattendancesystem.submodules.RC522Communicator;
import com.android.fortunaattendancesystem.submodules.SQLiteCommunicator;
import com.android.fortunaattendancesystem.submodules.WiegandCommunicator;
import com.android.fortunaattendancesystem.usbconnection.USBConnectionCreator;
import com.forlinx.android.GetValueService;
import com.forlinx.android.HardwareInterface;
import com.friendlyarm.SmartReader.SmartFinger;
import com.morpho.morphosmart.sdk.MorphoDatabase;
import com.morpho.morphosmart.sdk.MorphoDevice;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

//http://www.codesenior.com/en/tutorial/Android-GPS-Location-Example

//public class EmployeeAttendanceActivity extends USBConnectionCreator implements Observer, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
//        LocationListener, SurfaceHolder.Callback {

//public class EmployeeAttendanceActivity extends USBConnectionCreator implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
//        LocationListener, SurfaceHolder.Callback, FM220_Scanner_Interface {


public class EmployeeAttendanceActivity extends USBConnectionCreator implements LocationListener, SurfaceHolder.Callback, FM220_Scanner_Interface {

    public static Context context = null;

    Dialog alertDialog = null;
    Dialog successEmpDetailsDialog = null;
    Dialog failureEmpDetailsDialog = null;

    private MorphoDevice morphoDevice = null;
    private MorphoDatabase morphoDatabase = null;
    private MorphoCommunicator morphoComm = null;
    private SmartFinger rc632ReaderConnection = null;

    private UsbDeviceConnection smartReaderConnection;
    private UsbInterface intf = null;
    private UsbEndpoint input = null;
    private UsbEndpoint output = null;

    boolean isSmartRcvRegisterd = false;
    boolean isMorphoRcvRegistered = false;
    boolean isMorphoSmartRcvRegisterd = false;

    boolean modeSet = false;

    private boolean isCardRead = false;
    private ImageView smart_reader, finger_reader;

    TextView textViewPutFingerMessage, recordCount, tvInternetConn;
    public static TextView tvStateofToggleButton;
    public static ToggleButton tButton;
    public static EditText empIdToVerfiy;
    public static MenuItem menuItem;
    Button btn_In, btn_Out;
    ImageView fingerImage;

    //View bar;
    //Animation barAnimation;


    public static boolean isHttpDataTransferStarted = false;

    //============================= Location Manager Api =================================//

    private LocationManager locationManager;
    private Location location;
    public static String latitude = "", longitude = "";


    // private Preview mPreview;

    private SurfaceView sv;
    private SurfaceHolder sHolder;
    private Camera.Parameters parameters;

    public static Camera mCamera;
    public static byte[] pictureData;
    public static boolean safeToTakePicture = false;
    public static boolean hasCamera = false;

    private Timer httpDataTransferTimer, recordUpdateTimer, wigReadTimer, capReadTimer, batReadTimer, cardReadTimer, attendanceModeTimer, adcReadTimer;// mqttTimer;
    private TimerTask httpDataTransferTimerTask, recordUpdateTimerTask, wigReadTimerTask, capReadTimerTask, batReadTimerTask, cardReadTimerTask, attendanceModeTimerTask, adcReadTimerTask;// mqttTimerTask;

    private Handler wHandler = new Handler();
    private Handler cHandler = new Handler();
    private Handler bHandler = new Handler();
    private Handler cardHandler = new Handler();
    private Handler amHandler = new Handler();
    private Handler rHandler = new Handler();
    private Handler dHandler = new Handler();
    private Handler pHandler = new Handler();
    private Handler adcHandler = new Handler();

    private static boolean isBreakFound = true;
    public static boolean isWiegandReading = false;

    private static boolean isLCDBackLightOff = false;

    public static int currentCaptureBitmapId = 0;
    public static Animation anim = null;
    public static boolean isCardReadingBlocked = false;
    public static boolean stopModeUpdate = false;
    public static boolean isAttendanceWindowVisisble = false;

    //======================== Variables for Startek Finger Sensor ============================//

    private boolean isStartekRcvRegisterd = false;
    private String Telecom_Device_Key = "";
    private acpl_FM220_SDK FM220SDK;
    private UsbManager usbManager;
    private PendingIntent mPermissionIntent;

    //==========================================================================================//

    private static Handler hBrightness, hLCDBacklight, hIdentify;
    private static Runnable rBrightness, rLCDBacklight, rIdentify;

    private ImageView ivChargeIcon, ivBatTop;
    private Intent intent;
    private AdcMessageBroadcastReceiver receiver;
    private TextView tvBatPer, tvPower;
    ;
    private ProgressBar pbBatPer;

    private int index = 0;
    double[] numArray = new double[Constants.ADC_READ_ARRAY_LENGTH];
    private float adcValue;

    private static boolean isSDCalculated = false;
    private static double prevMean;
    int per = 0;

    boolean isPassDlgVisible = false;
    boolean isADCReceiverUnregistered = false;

    SQLiteCommunicator dbComm = new SQLiteCommunicator();

    public static String gvm = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*modifyActionBar();*/


        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);


        //setContentView(R.layout.activity_emp_finger_identify_black);


        setContentView(R.layout.activity_emp_finger_identify_white);

//        findViewById(R.id.emp_attendence).setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                hideSoftKeyboard(EmployeeAttendanceActivity.this);
//                return false;
//            }
//        });


        initLayoutElements();

        context = EmployeeAttendanceActivity.this;
        morphoComm = new MorphoCommunicator(context);

        gvm = dbComm.getCurrentGVM();
        if (gvm.trim().length() == 0) {
            gvm = Constants.DEFAULT_GVM;
        }

        if (!Constants.isTab) {
            if (HardwareInterface.class != null) {
                receiver = new AdcMessageBroadcastReceiver();
                registerReceiver(receiver, getIntentFilter());
                intent = new Intent();
                intent.setClass(EmployeeAttendanceActivity.this, GetValueService.class);
                intent.putExtra("mtype", "ADC");
                intent.putExtra("maction", "start");
                intent.putExtra("mfd", 1);
                EmployeeAttendanceActivity.this.startService(intent);
            } else {
                Toast.makeText(getApplicationContext(), "Load hardwareinterface library error!", Toast.LENGTH_LONG).show();
            }
        }

        int fr, sr;
        Settings settings = Settings.getInstance();
        fr = settings.getFrTypeValue();
        sr = settings.getSrTypeValue();
        if (fr == 0 && sr == 1) {
            initFingerSmart();//For Morpho and MicroSmart V2
        } else if (fr == 2) {//For Startek
            // initStartekFinger();
        } else {
            initFingerReader(fr);
            initSmartReader(sr);
        }

        hBrightness = new Handler();
        hLCDBacklight = new Handler();
        hIdentify = new Handler();

        rBrightness = new Runnable() {
            @Override
            public void run() {
                setScreenBrightness(Constants.BRIGHTNESS_OFF);
                isLCDBackLightOff = true;
                boolean isIdentificationStarted = ProcessInfo.getInstance().isIdentificationStarted();
                boolean isVerificationStarted = ProcessInfo.getInstance().isVerificationStarted();
                boolean isBioCommandStarted = ProcessInfo.getInstance().isCommandBioStart();
                switch (gvm) {
                    case "1:N":
                        morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
                        morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
                        if (morphoDevice != null && morphoDatabase != null) {
                            if ((isIdentificationStarted || isVerificationStarted) && isBioCommandStarted) {
                                morphoComm.stopFingerIdentification();
                            }
                        }
                        break;
                    case "CARD-BASED-VERIFY":
                        morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
                        morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
                        if (morphoDevice != null && morphoDatabase != null) {
                            if ((isIdentificationStarted || isVerificationStarted) && isBioCommandStarted) {
                                morphoComm.stopFingerIdentification();
                            }
                        }
                        break;
                    case "CARD+FINGER":
                        if ((isIdentificationStarted || isVerificationStarted) && isBioCommandStarted) {
                            morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
                            morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
                            if (morphoDevice != null && morphoDatabase != null) {
                                morphoComm.stopFingerIdentification();
                            }
                        }
                        break;
                }
            }
        };

        rLCDBacklight = new Runnable() {
            @Override
            public void run() {
                ForlinxGPIO.setLCDBackLightOff();
                isLCDBackLightOff = true;
            }
        };

        rIdentify = new Runnable() {
            @Override
            public void run() {
                switch (gvm) {
                    case "1:N":
                        morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
                        morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
                        if (morphoDevice != null && morphoDatabase != null) {
                            boolean isIdentificationStarted = ProcessInfo.getInstance().isIdentificationStarted();
                            boolean isVerificationStarted = ProcessInfo.getInstance().isVerificationStarted();
                            boolean isBioCommandStarted = ProcessInfo.getInstance().isCommandBioStart();
                            Log.d("TEST", "************* R Identify **************");
                            Log.d("TEST", " Is Identification Started:" + isIdentificationStarted);
                            Log.d("TEST", "Is Verification Started:" + isVerificationStarted);
                            Log.d("TEST", "Is Bio Command Started:" + isBioCommandStarted);
                            Log.d("TEST", "****************************************");
                            if (!isIdentificationStarted && !isVerificationStarted && !isBioCommandStarted) {
                                initIdentification();
                            }
                        }
                        break;
                    case "CARD-BASED-VERIFY":
                        morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
                        morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
                        if (morphoDevice != null && morphoDatabase != null) {
                            boolean isIdentificationStarted = ProcessInfo.getInstance().isIdentificationStarted();
                            boolean isVerificationStarted = ProcessInfo.getInstance().isVerificationStarted();
                            boolean isBioCommandStarted = ProcessInfo.getInstance().isCommandBioStart();
                            Log.d("TEST", "************* R Identify **************");
                            Log.d("TEST", " Is Identification Started:" + isIdentificationStarted);
                            Log.d("TEST", "Is Verification Started:" + isVerificationStarted);
                            Log.d("TEST", "Is Bio Command Started:" + isBioCommandStarted);
                            Log.d("TEST", "****************************************");
                            if (!isIdentificationStarted && !isVerificationStarted && !isBioCommandStarted) {
                                initIdentification();
                            }
                        }
                        break;
                }
            }
        };

        startHandler();

        locationManager = (LocationManager) getSystemService(Service.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 2, this);
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (location != null) {
                latitude = String.valueOf(location.getLatitude());
                longitude = String.valueOf(location.getLongitude());
            }
        }
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

            case 2:

                //======================== Startek FM200U Finger Sensor ===============================//
                //initUSBManagerReceiver();
                //unregisterReceivers();

                initHardwareConnections(5);
                registerBroadCastReceiver(1);

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

                break;


            case 2:

                //======================== Micro Smart V2 Smart Reader ===========================//

                searchDevices(2);
                smartReaderConnection = SmartReaderConnection.getInstance().getmConnection();
                intf = SmartReaderConnection.getInstance().getIntf();
                input = SmartReaderConnection.getInstance().getInput();
                output = SmartReaderConnection.getInstance().getOutput();
                if (smartReaderConnection != null && intf != null && input != null && output != null) {
                    updateSrConStatusToUI(true);
                } else {
                    updateSrConStatusToUI(false);
                }

                break;

            case 3:

                //======================== Morpho And Micro Smart V2 Smart Reader ===========================//

                searchDevices(3);
                smartReaderConnection = SmartReaderConnection.getInstance().getmConnection();
                intf = SmartReaderConnection.getInstance().getIntf();
                input = SmartReaderConnection.getInstance().getInput();
                output = SmartReaderConnection.getInstance().getOutput();
                if (smartReaderConnection != null && intf != null && input != null && output != null) {
                    updateSrConStatusToUI(true);
                } else {
                    updateSrConStatusToUI(false);
                }

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

            case 5:

                //======================== Startek FM200U Reader ===========================//

                searchDevices(4);
                if (EmployeeAttendanceActivity.super.isFingerReaderFound) {
                    EmployeeAttendanceActivity.this.isFingerReaderFound = true;
                    updateFrConStatusToUI(true);
                    // startFingerIdentification();
                } else {
                    updateFrConStatusToUI(false);
                }

                break;

            default:

                //Extra Utility functions

                //                mHandler.postDelayed(new Runnable() {
//                    @Override
//                    public synchronized void run() {
//                        searchDevices(3);
//                        morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
//                        morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
//                        smartReaderConnection = SmartReaderConnection.getInstance().getmConnection();
//                        intf = SmartReaderConnection.getInstance().getIntf();
//                        input = SmartReaderConnection.getInstance().getInput();
//                        output = SmartReaderConnection.getInstance().getOutput();
//                        if (morphoDevice != null && morphoDatabase != null) {
//                            updateFrConStatusToUI(true);
//                            initIdentification();
//                            // String empId="TE5956";
//                            // int ret=morphoComm.deleteMorphoUser(empId);
//                            // Log.d("TEST","Delete Status:"+ret);
////                            int ret = morphoComm.deleteMorphoDatabase();
////                            Log.d("TEST", "Database Delete Status:" + ret);
////                            morphoComm.destroyMorphoDatabase();
//                        } else {
//                            updateFrConStatusToUI(false);
//                            textViewPutFingerMessage.setText("Finger Reader Not Connected");
//                        }
//                        if (smartReaderConnection != null && intf != null && input != null && output != null) {
//                            updateSrConStatusToUI(true);
//                            executorService.submit(cardReadThread);
//                        } else {
//                            updateSrConStatusToUI(false);
//                        }
//                        executorService.submit(attendanceModeThread);
//                    }
//                }, 500);


                //                mHandler.postDelayed(new Runnable() {
//                    @Override
//                    public synchronized void run() {
//                        searchDevices(1);
//                        morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
//                        morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
//                        if (morphoDevice != null && morphoDatabase != null) {
//                            updateFrConStatusToUI(true);
//                            Log.d("TEST","Init Identification");
//                            initIdentification();
////                            MorphoUser morphoUser = new MorphoUser();
////                            String empId="TE5956";
////                            int ret = morphoDatabase.getUser(empId, morphoUser);
////                            if (ret == 0) {
////                                ret = morphoUser.dbDelete();
////                                Log.d("TEST","Ret:"+ret);
////                            }
//                            //  int ret=morphoComm.destroyMorphoDatabase();
//                            //  Log.d("TEST","Destroy Database:"+ret);
//                        } else {
//                            updateFrConStatusToUI(false);
//                            textViewPutFingerMessage.setText("Finger Reader Not Connected");
//                        }
//                        executorService.submit(attendanceModeThread);
//                    }
//                }, 500);

                break;

        }
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

                //==================================== USB Finger Reader ================================================//

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

    public static void startHandler() {
        hBrightness.postDelayed(rBrightness, Constants.BRIGHTNESS_OFF_DELAY); //for 10 seconds
        hLCDBacklight.postDelayed(rLCDBacklight, Constants.BACKLIGHT_OFF_DELAY); //for 20 seconds
        hIdentify.postDelayed(rIdentify, Constants.IDENTIFICATION_RESTART_DELAY); //for 20 seconds
    }

    public static void stopHandler() {
        Log.d("TEST", "Stop Handler");
        hBrightness.removeCallbacks(rBrightness);
        hLCDBacklight.removeCallbacks(rLCDBacklight);
        hIdentify.removeCallbacks(rIdentify);
    }

    public static void setScreenBrightness(int brightnessValue) {
        // Make sure brightness value between 0 to 255
        if (brightnessValue >= 0 && brightnessValue <= 255) {
            android.provider.Settings.System.putInt(
                    context.getContentResolver(),
                    android.provider.Settings.System.SCREEN_BRIGHTNESS,
                    brightnessValue
            );
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = String.valueOf(location.getLatitude());
        longitude = String.valueOf(location.getLongitude());
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

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

    public void readCardTemplate(int commType) {
        switch (commType) {
            case 0:
                try {
                    int error = -1;
                    byte[] charBuff = new byte[5];
                    if (!isCardReadingBlocked) {
                        error = rc632ReaderConnection.getSmartCardApi().smart_card_get_info(charBuff);
                        if (error == 0) {
                            if (!isCardRead) {
                                isCardRead = true;
                                isCardReadingBlocked = true;
                                //reset();
                                error = readSpiCardTemplate();
                                if (error == 0) {
                                    // int firstFingerTempLen = strFirstFingerTemplate.trim().length();
                                    // int secondFingerTempLen = strSecondFingerTemplate.trim().length();
                                    int firstFingerTempLen = 0, secondFingerTempLen = 0;
                                    if (firstFingerTempLen == Constants.TEMPLATE_SIZE && secondFingerTempLen == Constants.TEMPLATE_SIZE) { //Two Valid Templates Found
                                        MorphoDevice morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
                                        MorphoDatabase morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
                                        if (morphoDevice != null && morphoDatabase != null) {
                                            ProcessInfo.getInstance().setVerificationStarted(true);
                                            morphoComm.stopFingerIdentification();//Abort Finger Identification
                                            try {
                                                Thread.sleep(500);
                                            } catch (InterruptedException ex) {
                                            }
                                            // startVerificationByCard(cardDetails);//Start Finger Verification Using Card
                                        } else {
                                            if (failureEmpDetailsDialog != null && failureEmpDetailsDialog.isShowing()) {
                                                failureEmpDetailsDialog.cancel();
                                            }
                                            showFailureCustomDialog("Finger Reader Connection", "Finger Reader Not Found");
                                            pHandler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    failureEmpDetailsDialog.cancel();
                                                    isCardReadingBlocked = false;
                                                }
                                            }, 3000);
                                        }
                                    } else if (firstFingerTempLen == Constants.TEMPLATE_SIZE) { //One Valid Template Found
                                        MorphoDevice morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
                                        MorphoDatabase morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
                                        if (morphoDevice != null && morphoDatabase != null) {
                                            ProcessInfo.getInstance().setVerificationStarted(true);
                                            morphoComm.stopFingerIdentification();//Abort Finger Identification
                                            try {
                                                Thread.sleep(500);
                                            } catch (InterruptedException ex) {
                                            }
                                            // startVerificationByCard(cardDetails);//Start Finger Verification Using Card
                                        } else {
                                            if (failureEmpDetailsDialog != null && failureEmpDetailsDialog.isShowing()) {
                                                failureEmpDetailsDialog.cancel();
                                            }
                                            showFailureCustomDialog("Finger Reader Connection", "Finger Reader Not Found");
                                            pHandler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    failureEmpDetailsDialog.cancel();
                                                    isCardReadingBlocked = false;
                                                }
                                            }, 3000);
                                        }
                                    } else {//Invalid Template Found
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (failureEmpDetailsDialog != null && failureEmpDetailsDialog.isShowing()) {
                                                    failureEmpDetailsDialog.cancel();
                                                }
                                                showFailureCustomDialog("Card Read Status", "Invalid Template ! Put Card Again");
                                                pHandler.postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        failureEmpDetailsDialog.cancel();
                                                        isCardReadingBlocked = false;
                                                    }
                                                }, 3000);
                                            }
                                        });
                                    }
                                } else {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (failureEmpDetailsDialog != null && failureEmpDetailsDialog.isShowing()) {
                                                failureEmpDetailsDialog.cancel();
                                            }
                                            showFailureCustomDialog("Card Read Status", "Invalid Card ! Show Card Again");
                                            pHandler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    failureEmpDetailsDialog.cancel();
                                                    isCardReadingBlocked = false;
                                                }

                                            }, 3000);
                                        }
                                    });
                                }
                            }
                        } else {
                            isCardRead = false;
                        }
                    }
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getBaseContext(), "Card Data Received Error:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                break;
            case 1:
                try {
                    MicroSmartV2Communicator conn = null;
                    conn = getReaderConnection();
                    if (conn != null) {
                        if (!isCardReadingBlocked) {
                            String command = Utility.addCheckSum(Constants.READ_CARD_ID_COMMAND);
                            String strCardId = conn.readCardId(command.getBytes());
                            int cardIdLen = strCardId.trim().length();
                            if (cardIdLen > 0) {
                                strCardId = strCardId.replaceAll("\\G0", " ").trim();
                                if (!isCardRead) {
                                    isCardRead = true;
                                    isCardReadingBlocked = true;
                                    boolean status = false;
                                    SmartCardInfo cardDetails = new SmartCardInfo();
                                    cardDetails.setCardId(strCardId);
                                    command = Utility.addCheckSum(Constants.READ_CSN_COMMAND);
                                    byte[] sectZeroData = conn.readSector(0, command.getBytes());
                                    if (sectZeroData != null) {
                                        status = conn.parseSectorData(0, sectZeroData, cardDetails);
                                        if (status) {
                                            SQLiteCommunicator dbComm = new SQLiteCommunicator();
                                            status = dbComm.checkIsCardHotListed(cardDetails.getReadCSN());
                                            if (!status) {
                                                status = readUsbCardTemplate(conn, cardDetails);
                                                if (status) {
                                                    int firstFingerTempLen = cardDetails.getFirstFingerTemplate().trim().length();
                                                    int secondFingerTempLen = cardDetails.getSecondFingerTemplate().trim().length();
                                                    if (firstFingerTempLen == Constants.TEMPLATE_SIZE && secondFingerTempLen == Constants.TEMPLATE_SIZE) { //Two Valid Templates Found
                                                        MorphoDevice morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
                                                        MorphoDatabase morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
                                                        if (morphoDevice != null && morphoDatabase != null) {
                                                            ProcessInfo.getInstance().setVerificationStarted(true);
                                                            morphoComm.stopFingerIdentification();//Abort Finger Identification
                                                            try {
                                                                Thread.sleep(500);
                                                            } catch (InterruptedException ex) {
                                                            }
                                                            initCFVByVM(cardDetails);//Start Finger Verification Using Card
                                                        } else {
                                                            runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    if (failureEmpDetailsDialog != null && failureEmpDetailsDialog.isShowing()) {
                                                                        failureEmpDetailsDialog.cancel();
                                                                    }
                                                                    showFailureCustomDialog("Finger Reader Connection", "Finger Reader Not Found");
                                                                    pHandler.postDelayed(new Runnable() {
                                                                        @Override
                                                                        public void run() {
                                                                            failureEmpDetailsDialog.cancel();
                                                                            isCardReadingBlocked = false;
                                                                        }
                                                                    }, 3000);
                                                                }
                                                            });
                                                        }
                                                    } else if (firstFingerTempLen == Constants.TEMPLATE_SIZE) { //One Valid Template Found
                                                        MorphoDevice morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
                                                        MorphoDatabase morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
                                                        if (morphoDevice != null && morphoDatabase != null) {
                                                            ProcessInfo.getInstance().setVerificationStarted(true);
                                                            morphoComm.stopFingerIdentification();//Abort Finger Identification
                                                            try {
                                                                Thread.sleep(500);
                                                            } catch (InterruptedException ex) {
                                                            }
                                                            initCFVByVM(cardDetails);//Start Finger Verification Using Card
                                                        } else {
                                                            runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    if (failureEmpDetailsDialog != null && failureEmpDetailsDialog.isShowing()) {
                                                                        failureEmpDetailsDialog.cancel();
                                                                    }
                                                                    showFailureCustomDialog("Finger Reader Connection", "Finger Reader Not Found");
                                                                    pHandler.postDelayed(new Runnable() {
                                                                        @Override
                                                                        public void run() {
                                                                            failureEmpDetailsDialog.cancel();
                                                                            isCardReadingBlocked = false;
                                                                        }
                                                                    }, 3000);
                                                                }
                                                            });
                                                        }
                                                    } else {//Invalid Template Found
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                if (failureEmpDetailsDialog != null && failureEmpDetailsDialog.isShowing()) {
                                                                    failureEmpDetailsDialog.cancel();
                                                                }
                                                                showFailureCustomDialog("Card Read Status", "Invalid Template ! Put Card Again");
                                                                pHandler.postDelayed(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        failureEmpDetailsDialog.cancel();
                                                                        isCardReadingBlocked = false;
                                                                    }
                                                                }, 3000);
                                                            }
                                                        });

                                                    }
                                                } else {
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            if (failureEmpDetailsDialog != null && failureEmpDetailsDialog.isShowing()) {
                                                                failureEmpDetailsDialog.cancel();
                                                            }
                                                            showFailureCustomDialog("Card Read Status", "Invalid Card ! Put Card Again");
                                                            pHandler.postDelayed(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    failureEmpDetailsDialog.cancel();
                                                                    isCardReadingBlocked = false;
                                                                }
                                                            }, 3000);
                                                        }
                                                    });
                                                }


                                            } else {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        if (failureEmpDetailsDialog != null && failureEmpDetailsDialog.isShowing()) {
                                                            failureEmpDetailsDialog.cancel();
                                                        }
                                                        showFailureCustomDialog("Card Read Status", "Card Is Hot-Listed");
                                                        pHandler.postDelayed(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                failureEmpDetailsDialog.cancel();
                                                                isCardReadingBlocked = false;
                                                            }
                                                        }, 3000);

                                                    }
                                                });
                                            }
                                        } else {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (failureEmpDetailsDialog != null && failureEmpDetailsDialog.isShowing()) {
                                                        failureEmpDetailsDialog.cancel();
                                                    }
                                                    showFailureCustomDialog("Card Read Status", "Invalid CSN");
                                                    pHandler.postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            failureEmpDetailsDialog.cancel();
                                                            isCardReadingBlocked = false;
                                                        }

                                                    }, 3000);
                                                }
                                            });
                                        }
                                    } else {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (failureEmpDetailsDialog != null && failureEmpDetailsDialog.isShowing()) {
                                                    failureEmpDetailsDialog.cancel();
                                                }
                                                showFailureCustomDialog("Card Read Status", "Invalid Card ! Put Card Again");
                                                pHandler.postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        failureEmpDetailsDialog.cancel();
                                                        isCardReadingBlocked = false;
                                                    }

                                                }, 3000);
                                            }
                                        });
                                    }
                                }
                            } else {
                                isCardRead = false;
                            }
                        }
                    }
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getBaseContext(), "Card Data Received Error:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            isCardReadingBlocked = false;
                        }
                    });
                }
                break;
            case 2:
                boolean status = false;
                RC522Communicator comm = new RC522Communicator();
                if (!isCardReadingBlocked) {
                    status = comm.writeRC522(Constants.RC522_READ_CSN_COMMAND);
                    if (status) {
                        char[] data = comm.readRC522();
                        if (data != null && data.length > 0) {
                            String strData = new String(data);
                            String arr[] = strData.trim().split(":");
                            if (arr != null && arr.length == 2) {
                                strData = arr[1].trim();
                                if (!strData.equals(Constants.RC522_CARD_NOT_PRESENT_VAL)) {
                                    if (!isCardRead) {
                                        isCardRead = true;
                                        isCardReadingBlocked = true;
                                        final SmartCardInfo cardInfo = new SmartCardInfo();
                                        cardInfo.setReadCSN(strData.toUpperCase());
                                        status = comm.writeRC522(Constants.RC522_READ_CARDID_COMMAND);
                                        if (status) {
                                            data = comm.readRC522();
                                            if (data != null && data.length > 0) {
                                                strData = new String(data);
                                                arr = strData.trim().split(":");
                                                if (arr != null && arr.length == 3) {
                                                    strData = arr[2].trim();
                                                    if (!strData.equals("RD-FAIL")) {
                                                        String cardId = Utility.hexToAscii(strData.substring(0, 16));
                                                        cardInfo.setCardId(cardId);
                                                        switch (gvm) {
                                                            case "1:N":
                                                                status = readFullCardUsingRC522(comm, cardInfo);
                                                                if (status) {
                                                                    MorphoDevice morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
                                                                    MorphoDatabase morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
                                                                    if (morphoDevice != null && morphoDatabase != null) {
                                                                        String fft = cardInfo.getFirstFingerTemplate();
                                                                        String sft = cardInfo.getSecondFingerTemplate();
                                                                        if (fft != null && sft != null) {//Two finger template found
                                                                            int l1 = fft.trim().length();
                                                                            int l2 = sft.trim().length();
                                                                            if (l1 == Constants.TEMPLATE_SIZE && l2 == Constants.TEMPLATE_SIZE) { //Two Valid Templates Found
                                                                                boolean isIdentificationStarted = ProcessInfo.getInstance().isIdentificationStarted();
                                                                                boolean isVerificationStarted = ProcessInfo.getInstance().isVerificationStarted();
                                                                                boolean isBioCommandStarted = ProcessInfo.getInstance().isCommandBioStart();
                                                                                if ((isIdentificationStarted || isVerificationStarted) && isBioCommandStarted) {
                                                                                    morphoComm.stopFingerIdentification();//Abort Finger Identification
                                                                                }
                                                                                stopHandler();
                                                                                try {
                                                                                    Thread.sleep(500);
                                                                                } catch (InterruptedException ex) {
                                                                                }
                                                                                if (!isFinishing()) {
                                                                                    morphoComm.startFingerVerification(Constants.VERIFY_BY_CARD_MODE, cardInfo);
                                                                                }
                                                                            } else {
                                                                                showFailureDialog(1, "Card Read Status", "Invalid Template Length ! Put Card Again");
                                                                            }
                                                                        } else if (fft != null && sft == null) {//One finger template found
                                                                            int firstFingerTempLen = fft.trim().length();
                                                                            if (firstFingerTempLen == Constants.TEMPLATE_SIZE) {
                                                                                boolean isIdentificationStarted = ProcessInfo.getInstance().isIdentificationStarted();
                                                                                boolean isVerificationStarted = ProcessInfo.getInstance().isVerificationStarted();
                                                                                boolean isBioCommandStarted = ProcessInfo.getInstance().isCommandBioStart();
                                                                                if ((isIdentificationStarted || isVerificationStarted) && isBioCommandStarted) {
                                                                                    morphoComm.stopFingerIdentification();//Abort Finger Identification
                                                                                }
                                                                                stopHandler();
                                                                                try {
                                                                                    Thread.sleep(500);
                                                                                } catch (InterruptedException ex) {
                                                                                }
                                                                                if (!isFinishing()) {
                                                                                    morphoComm.startFingerVerification(Constants.VERIFY_BY_CARD_MODE, cardInfo);
                                                                                }
                                                                            } else {
                                                                                showFailureDialog(1, "Card Read Status", "Invalid Template Length ! Put Card Again");
                                                                            }
                                                                        } else if (fft == null && sft == null) {
                                                                            showFailureDialog(1, "Card Read Status", "No Template Found ! Put Card Again");
                                                                        }
                                                                    } else {
                                                                        showFailureDialog(1, "Finger Reader Connection", "Finger Reader Not Found");
                                                                    }
                                                                } else {
                                                                    showFailureDialog(1, "Card Read Status", "Invalid Card ! Put Card Again");
                                                                }
                                                                break;
                                                            case "CARD-BASED-VERIFY":
                                                                if (MorphoCommunicator.isCDV) {
                                                                    EditText etCardId = MorphoCommunicator.iCardId;
                                                                    if (etCardId != null) {
                                                                        etCardId.setText(cardInfo.getCardId().replaceAll("\\G0", " ").trim());
                                                                    }
                                                                    isCardReadingBlocked = false;
                                                                } else {
                                                                    status = readFullCardUsingRC522(comm, cardInfo);
                                                                    if (status) {
                                                                        MorphoDevice morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
                                                                        MorphoDatabase morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
                                                                        if (morphoDevice != null && morphoDatabase != null) {
                                                                            String fft = cardInfo.getFirstFingerTemplate();
                                                                            String sft = cardInfo.getSecondFingerTemplate();
                                                                            if (fft != null && sft != null) {//Two finger template found
                                                                                int l1 = fft.trim().length();
                                                                                int l2 = sft.trim().length();
                                                                                if (l1 == Constants.TEMPLATE_SIZE && l2 == Constants.TEMPLATE_SIZE) { //Two Valid Templates Found
                                                                                    boolean isIdentificationStarted = ProcessInfo.getInstance().isIdentificationStarted();
                                                                                    boolean isVerificationStarted = ProcessInfo.getInstance().isVerificationStarted();
                                                                                    boolean isBioCommandStarted = ProcessInfo.getInstance().isCommandBioStart();
                                                                                    if ((isIdentificationStarted || isVerificationStarted) && isBioCommandStarted) {
                                                                                        morphoComm.stopFingerIdentification();//Abort Finger Identification
                                                                                    }
                                                                                    stopHandler();
                                                                                    try {
                                                                                        Thread.sleep(500);
                                                                                    } catch (InterruptedException ex) {
                                                                                    }
                                                                                    initCFVByVM(cardInfo);//Start Finger Verification Using Card
                                                                                } else {
                                                                                    showFailureDialog(1, "Card Read Status", "Invalid Template Length ! Put Card Again");
                                                                                }
                                                                            } else if (fft != null && sft == null) {//One finger template found
                                                                                int firstFingerTempLen = fft.trim().length();
                                                                                if (firstFingerTempLen == Constants.TEMPLATE_SIZE) {
                                                                                    boolean isIdentificationStarted = ProcessInfo.getInstance().isIdentificationStarted();
                                                                                    boolean isVerificationStarted = ProcessInfo.getInstance().isVerificationStarted();
                                                                                    boolean isBioCommandStarted = ProcessInfo.getInstance().isCommandBioStart();
                                                                                    if ((isIdentificationStarted || isVerificationStarted) && isBioCommandStarted) {
                                                                                        morphoComm.stopFingerIdentification();//Abort Finger Identification
                                                                                    }
                                                                                    stopHandler();
                                                                                    try {
                                                                                        Thread.sleep(500);
                                                                                    } catch (InterruptedException ex) {
                                                                                    }
                                                                                    initCFVByVM(cardInfo); // Start Finger Verification Using Card
                                                                                } else {
                                                                                    showFailureDialog(1, "Card Read Status", "Invalid Template Length ! Put Card Again");
                                                                                }
                                                                            } else if (fft == null && sft == null) {
                                                                                showFailureDialog(1, "Card Read Status", "No Template Found ! Put Card Again");
                                                                            }
                                                                        } else {
                                                                            showFailureDialog(1, "Finger Reader Connection", "Finger Reader Not Found");
                                                                        }
                                                                    } else {
                                                                        showFailureDialog(1, "Card Read Status", "Invalid Card ! Put Card Again");
                                                                    }
                                                                }
                                                                break;
                                                            case "CARD-ONLY":
                                                                status = readSector2UsingRC522(comm, cardInfo);
                                                                if (status) {
                                                                    runOnUiThread(new Runnable() {
                                                                        @Override
                                                                        public void run() {
                                                                            if (!isFinishing()) {
                                                                                if (successEmpDetailsDialog != null && successEmpDetailsDialog.isShowing()) {
                                                                                    successEmpDetailsDialog.cancel();
                                                                                }
                                                                                new Thread(new Runnable() {
                                                                                    @Override
                                                                                    public void run() {
                                                                                        ForlinxGPIO.runGPIOLEDForSuccess();
                                                                                    }
                                                                                }).start();
                                                                                showSuccessCustomDialogForCardVerification("Verification Success", cardInfo);
                                                                                pHandler.postDelayed(new Runnable() {
                                                                                    @Override
                                                                                    public void run() {
                                                                                        successEmpDetailsDialog.cancel();
                                                                                        isCardReadingBlocked = false;
                                                                                    }
                                                                                }, 1000);
                                                                            }
                                                                        }
                                                                    });
                                                                } else {
                                                                    showFailureDialog(1, "Card Read Status", "Invalid Card ! Put Card Again");
                                                                }
                                                                break;
                                                            case "CARD+FINGER":
                                                                status = readFullCardUsingRC522(comm, cardInfo);
                                                                if (status) {
                                                                    MorphoDevice morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
                                                                    MorphoDatabase morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
                                                                    if (morphoDevice != null && morphoDatabase != null) {
                                                                        String fft = cardInfo.getFirstFingerTemplate();
                                                                        String sft = cardInfo.getSecondFingerTemplate();
                                                                        if (fft != null && sft != null) {//Two finger template found
                                                                            int l1 = fft.trim().length();
                                                                            int l2 = sft.trim().length();
                                                                            if (l1 == Constants.TEMPLATE_SIZE && l2 == Constants.TEMPLATE_SIZE) { //Two Valid Templates Found
                                                                                if (!isFinishing()) {
                                                                                    morphoComm.startFingerVerification(Constants.VERIFY_BY_CARD_MODE, cardInfo);
                                                                                }
                                                                            } else {
                                                                                showFailureDialog(1, "Card Read Status", "Invalid Template Length ! Put Card Again");
                                                                            }
                                                                        } else if (fft != null && sft == null) {//One finger template found
                                                                            int firstFingerTempLen = fft.trim().length();
                                                                            if (firstFingerTempLen == Constants.TEMPLATE_SIZE) {
                                                                                if (!isFinishing()) {
                                                                                    morphoComm.startFingerVerification(Constants.VERIFY_BY_CARD_MODE, cardInfo);
                                                                                }
                                                                            } else {
                                                                                showFailureDialog(1, "Card Read Status", "Invalid Template Length ! Put Card Again");
                                                                            }
                                                                        } else if (fft == null && sft == null) {
                                                                            showFailureDialog(1, "Card Read Status", "No Template Found ! Put Card Again");
                                                                        }
                                                                    } else {
                                                                        showFailureDialog(1, "Finger Reader Connection", "Finger Reader Not Found");
                                                                    }
                                                                } else {
                                                                    showFailureDialog(1, "Card Read Status", "Invalid Card ! Put Card Again");
                                                                }
                                                                break;
                                                        }
                                                    } else {
                                                        isCardReadingBlocked = false;
                                                        isCardRead = false;
                                                    }
                                                } else {
                                                    isCardReadingBlocked = false;
                                                    isCardRead = false;
                                                }
                                            } else {
                                                isCardReadingBlocked = false;
                                                isCardRead = false;
                                            }
                                        } else {
                                            isCardReadingBlocked = false;
                                            isCardRead = false;
                                        }
                                    }
                                } else {
                                    isCardReadingBlocked = false;
                                    isCardRead = false;
                                }
                            } else {
                                isCardReadingBlocked = false;
                                isCardRead = false;
                            }
                        } else {
                            isCardReadingBlocked = false;
                            isCardRead = false;
                        }
                    } else {
                        if (failureEmpDetailsDialog != null && failureEmpDetailsDialog.isShowing()) {
                            failureEmpDetailsDialog.cancel();
                        }
                        isCardRead = false;
                    }
                }
                break;
            default:
                break;
        }
    }

    private void showFailureDialog(int mode, final String title, final String reason) {
        switch (mode) {
            case 1:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!isFinishing()) {
                            if (failureEmpDetailsDialog != null && failureEmpDetailsDialog.isShowing()) {
                                failureEmpDetailsDialog.cancel();
                            }
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    ForlinxGPIO.runGPIOLEDForFailure();
                                }
                            }).start();
                            showFailureCustomDialog(title, reason);
                            pHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    failureEmpDetailsDialog.cancel();
                                    isCardReadingBlocked = false;
                                }
                            }, 1500);
                        }
                    }
                });

                break;
            case 2:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!isFinishing()) {
                            if (failureEmpDetailsDialog != null && failureEmpDetailsDialog.isShowing()) {
                                failureEmpDetailsDialog.cancel();
                            }
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    ForlinxGPIO.runGPIOLEDForFailure();
                                }
                            }).start();
                            showFailureCustomDialog(title, reason);
                            pHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    failureEmpDetailsDialog.cancel();
                                    WiegandCommunicator.clearWiegand(Constants.WIEGAND_WRITE_PATH, "1");
                                    isWiegandReading = false;
                                }
                            }, 1000);
                        }
                    }
                });
                break;
        }
    }


    private boolean readSector2UsingRC522(RC522Communicator comm, SmartCardInfo cardInfo) {//read Sector 2 For Card Only Global VM
        boolean parseStatus = false;
        Cursor sectorKeyData = null;
        SQLiteCommunicator dbComm = new SQLiteCommunicator();
        sectorKeyData = dbComm.getSectorKeyForReadCard(2);
        if (sectorKeyData != null) {
            int sectorNo = 2;
            boolean status = false;
            String wrCommand = "";
            while (sectorKeyData.moveToNext()) {
                String strKey = sectorKeyData.getString(0);
                wrCommand = Constants.RC522_SECTOR_READ_COMMAND + " " + Integer.toString(sectorNo) + " " + strKey + " " + Constants.RC522_KEY_TYPE_B;
                status = comm.writeRC522(wrCommand);
                if (status) {
                    char[] data = comm.readRC522();
                    if (data != null && data.length > 0) {
                        String strData = new String(data);
                        String[] arr = strData.split(":");
                        if (arr != null && arr.length == 3) {
                            String perInfo = arr[2].trim().toUpperCase();
                            if (!perInfo.equals("RD-FAIL")) {
                                perInfo = "00" + perInfo;
                                perInfo = Utility.hexToAscii(perInfo);
                                perInfo = Utility.removeNonAscii(perInfo);
                                parseStatus = comm.parseSectorData(2, perInfo.getBytes(), cardInfo);
                            }
                        }
                    }
                }
            }
            sectorKeyData.close();
        }
        return parseStatus;
    }

    private boolean readFullCardUsingRC522(RC522Communicator comm, SmartCardInfo cardInfo) {

        boolean parseStatus = false;
        Cursor sectorKeyData = null;
        SQLiteCommunicator dbComm = new SQLiteCommunicator();


        //========================= Read MAD 1 of Sector Zero ============================//

        String madData = "";
        String wrCommand = Constants.RC522_BLOCK_READ_COMMAND + " " + Integer.toString(2) + " " + "524553534543" + " " + Constants.RC522_KEY_TYPE_B;
        boolean status = comm.writeRC522(wrCommand);
        if (status) {
            char[] data = comm.readRC522();
            if (data != null && data.length > 0) {
                String strData = new String(data);
                String[] arr = strData.split(":");
                if (arr != null && arr.length == 3) {
                    madData = arr[2].trim().toUpperCase();
                }
            }
        }
        int length = madData.trim().length();
        if (length > 0 && madData.equals(Constants.MAD_DATA_FOR_TWO_TEMPLATES)) {
            sectorKeyData = dbComm.getSectorAndKeyForReadCard();
            if (sectorKeyData != null) {
                wrCommand = "";
                while (sectorKeyData.moveToNext()) {
                    String strSectorNo = sectorKeyData.getString(0);
                    String strKey = sectorKeyData.getString(1);
                    int sectorNo = Integer.parseInt(strSectorNo);
                    status = false;
                    String cardData = "";
                    StringBuffer sb = new StringBuffer();
                    switch (sectorNo) {
                        case 0:
                            parseStatus = true;
                            break;
                        case 1:
                            parseStatus = true;
                            break;
                        case 2:
                            parseStatus = false;
                            wrCommand = Constants.RC522_SECTOR_READ_COMMAND + " " + Integer.toString(sectorNo) + " " + strKey + " " + Constants.RC522_KEY_TYPE_B;
                            status = comm.writeRC522(wrCommand);
                            if (status) {
                                char[] data = comm.readRC522();
                                if (data != null && data.length > 0) {
                                    String strData = new String(data);
                                    String[] arr = strData.split(":");
                                    if (arr != null && arr.length == 3) {
                                        String perInfo = arr[2].trim().toUpperCase();
                                        if (!perInfo.equals("RD-FAIL")) {
                                            perInfo = "00" + perInfo;
                                            perInfo = Utility.hexToAscii(perInfo);
                                            perInfo = Utility.removeNonAscii(perInfo);
                                            parseStatus = comm.parseSectorData(2, perInfo.getBytes(), cardInfo);
                                        }
                                    }
                                }
                            }
                            break;
                        case 3:
                            parseStatus = true;
                            break;
                        case 4:
                            parseStatus = false;
                            wrCommand = Constants.RC522_SECTOR_READ_COMMAND + " " + Integer.toString(sectorNo) + " " + strKey + " " + Constants.RC522_KEY_TYPE_B;
                            status = comm.writeRC522(wrCommand);
                            if (status) {
                                char[] data = comm.readRC522();
                                if (data != null && data.length > 0) {
                                    String strData = new String(data);
                                    String[] arr = strData.split(":");
                                    if (arr != null && arr.length == 3) {
                                        cardData = arr[2].trim();
                                        if (!cardData.equals("RD-FAIL")) {
                                            cardData = "0" + cardData;
                                            parseStatus = comm.parseSectorData(4, cardData.getBytes(), cardInfo);
                                        }
                                    }
                                }
                            }
                            break;
                        case 5:
                            parseStatus = false;
                            wrCommand = Constants.RC522_SECTOR_READ_COMMAND + " " + Integer.toString(sectorNo) + " " + strKey + " " + Constants.RC522_KEY_TYPE_B;
                            status = comm.writeRC522(wrCommand);
                            if (status) {
                                char[] data = comm.readRC522();
                                if (data != null && data.length > 0) {
                                    String strData = new String(data);
                                    String[] arr = strData.split(":");
                                    if (arr != null && arr.length == 3) {
                                        cardData = arr[2].trim();
                                        if (!cardData.equals("RD-FAIL")) {
                                            cardData = "0" + cardData;
                                            parseStatus = comm.parseSectorData(5, cardData.getBytes(), cardInfo);
                                        }
                                    }
                                }
                            }
                            break;
                        case 6:
                            parseStatus = false;
                            wrCommand = Constants.RC522_SECTOR_READ_COMMAND + " " + Integer.toString(sectorNo) + " " + strKey + " " + Constants.RC522_KEY_TYPE_B;
                            status = comm.writeRC522(wrCommand);
                            if (status) {
                                char[] data = comm.readRC522();
                                if (data != null && data.length > 0) {
                                    String strData = new String(data);
                                    String[] arr = strData.split(":");
                                    if (arr != null && arr.length == 3) {
                                        cardData = arr[2].trim();
                                        if (!cardData.equals("RD-FAIL")) {
                                            cardData = "0" + cardData;
                                            parseStatus = comm.parseSectorData(6, cardData.getBytes(), cardInfo);
                                        }
                                    }
                                }
                            }
                            break;
                        case 7:
                            parseStatus = false;
                            wrCommand = Constants.RC522_SECTOR_READ_COMMAND + " " + Integer.toString(sectorNo) + " " + strKey + " " + Constants.RC522_KEY_TYPE_B;
                            status = comm.writeRC522(wrCommand);
                            if (status) {
                                char[] data = comm.readRC522();
                                if (data != null && data.length > 0) {
                                    String strData = new String(data);
                                    String[] arr = strData.split(":");
                                    if (arr != null && arr.length == 3) {
                                        cardData = arr[2].trim();
                                        if (!cardData.equals("RD-FAIL")) {
                                            cardData = "0" + cardData;
                                            parseStatus = comm.parseSectorData(7, cardData.getBytes(), cardInfo);
                                        }
                                    }
                                }
                            }
                            break;
                        case 8:
                            parseStatus = false;
                            wrCommand = Constants.RC522_SECTOR_READ_COMMAND + " " + Integer.toString(sectorNo) + " " + strKey + " " + Constants.RC522_KEY_TYPE_B;
                            status = comm.writeRC522(wrCommand);
                            if (status) {
                                char[] data = comm.readRC522();
                                if (data != null && data.length > 0) {
                                    String strData = new String(data);
                                    String[] arr = strData.split(":");
                                    if (arr != null && arr.length == 3) {
                                        cardData = arr[2].trim();
                                        if (!cardData.equals("RD-FAIL")) {
                                            cardData = "0" + cardData;
                                            parseStatus = comm.parseSectorData(8, cardData.getBytes(), cardInfo);
                                        }
                                    }
                                }
                            }
                            break;
                        case 9:
                            parseStatus = false;
                            wrCommand = Constants.RC522_SECTOR_READ_COMMAND + " " + Integer.toString(sectorNo) + " " + strKey + " " + Constants.RC522_KEY_TYPE_B;
                            status = comm.writeRC522(wrCommand);
                            if (status) {
                                char[] data = comm.readRC522();
                                if (data != null && data.length > 0) {
                                    String strData = new String(data);
                                    String[] arr = strData.split(":");
                                    if (arr != null && arr.length == 3) {
                                        cardData = arr[2].trim();
                                        if (!cardData.equals("RD-FAIL")) {
                                            cardData = "0" + cardData;
                                            parseStatus = comm.parseSectorData(9, cardData.getBytes(), cardInfo);
                                        }
                                    }
                                }
                            }
                            break;
                        case 10:
                            parseStatus = false;
                            wrCommand = Constants.RC522_SECTOR_READ_COMMAND + " " + Integer.toString(sectorNo) + " " + strKey + " " + Constants.RC522_KEY_TYPE_B;
                            status = comm.writeRC522(wrCommand);
                            if (status) {
                                char[] data = comm.readRC522();
                                if (data != null && data.length > 0) {
                                    String strData = new String(data);
                                    String[] arr = strData.split(":");
                                    if (arr != null && arr.length == 3) {
                                        cardData = arr[2].trim();
                                        if (!cardData.equals("RD-FAIL")) {
                                            cardData = "0" + cardData;
                                            parseStatus = comm.parseSectorData(10, cardData.getBytes(), cardInfo);
                                        }
                                    }
                                }
                            }
                            break;
                        case 11:
                            parseStatus = false;
                            wrCommand = Constants.RC522_SECTOR_READ_COMMAND + " " + Integer.toString(sectorNo) + " " + strKey + " " + Constants.RC522_KEY_TYPE_B;
                            status = comm.writeRC522(wrCommand);
                            if (status) {
                                char[] data = comm.readRC522();
                                if (data != null && data.length > 0) {
                                    String strData = new String(data);
                                    String[] arr = strData.split(":");
                                    if (arr != null && arr.length == 3) {
                                        cardData = arr[2].trim();
                                        if (!cardData.equals("RD-FAIL")) {
                                            cardData = "0" + cardData;
                                            parseStatus = comm.parseSectorData(11, cardData.getBytes(), cardInfo);
                                        }
                                    }
                                }
                            }
                            break;
                        case 12:
                            parseStatus = false;
                            wrCommand = Constants.RC522_SECTOR_READ_COMMAND + " " + Integer.toString(sectorNo) + " " + strKey + " " + Constants.RC522_KEY_TYPE_B;
                            status = comm.writeRC522(wrCommand);
                            if (status) {
                                char[] data = comm.readRC522();
                                if (data != null && data.length > 0) {
                                    String strData = new String(data);
                                    String[] arr = strData.split(":");
                                    if (arr != null && arr.length == 3) {
                                        cardData = arr[2].trim();
                                        if (!cardData.equals("RD-FAIL")) {
                                            cardData = "0" + cardData;
                                            parseStatus = comm.parseSectorData(12, cardData.getBytes(), cardInfo);
                                        }
                                    }
                                }
                            }
                            break;
                        case 13:
                            parseStatus = false;
                            wrCommand = Constants.RC522_SECTOR_READ_COMMAND + " " + Integer.toString(sectorNo) + " " + strKey + " " + Constants.RC522_KEY_TYPE_B;
                            status = comm.writeRC522(wrCommand);
                            if (status) {
                                char[] data = comm.readRC522();
                                if (data != null && data.length > 0) {
                                    String strData = new String(data);
                                    String[] arr = strData.split(":");
                                    if (arr != null && arr.length == 3) {
                                        cardData = arr[2].trim();
                                        if (!cardData.equals("RD-FAIL")) {
                                            cardData = "0" + cardData;
                                            parseStatus = comm.parseSectorData(13, cardData.getBytes(), cardInfo);
                                        }
                                    }
                                }
                            }
                            break;
                        case 14:
                            parseStatus = false;
                            wrCommand = Constants.RC522_SECTOR_READ_COMMAND + " " + Integer.toString(sectorNo) + " " + strKey + " " + Constants.RC522_KEY_TYPE_B;
                            status = comm.writeRC522(wrCommand);
                            if (status) {
                                char[] data = comm.readRC522();
                                if (data != null && data.length > 0) {
                                    String strData = new String(data);
                                    String[] arr = strData.split(":");
                                    if (arr != null && arr.length == 3) {
                                        cardData = arr[2].trim();
                                        if (!cardData.equals("RD-FAIL")) {
                                            cardData = "0" + cardData;
                                            parseStatus = comm.parseSectorData(14, cardData.getBytes(), cardInfo);
                                        }
                                    }
                                }
                            }
                            break;
                        case 15:
                            parseStatus = false;
                            wrCommand = Constants.RC522_SECTOR_READ_COMMAND + " " + Integer.toString(sectorNo) + " " + strKey + " " + Constants.RC522_KEY_TYPE_B;
                            status = comm.writeRC522(wrCommand);
                            if (status) {
                                char[] data = comm.readRC522();
                                if (data != null && data.length > 0) {
                                    String strData = new String(data);
                                    String[] arr = strData.split(":");
                                    if (arr != null && arr.length == 3) {
                                        cardData = arr[2].trim();
                                        if (!cardData.equals("RD-FAIL")) {
                                            cardData = "0" + cardData;
                                            parseStatus = comm.parseSectorData(15, cardData.getBytes(), cardInfo);
                                        }
                                    }
                                }
                            }
                            break;
                    }
                    if (!parseStatus) {
                        break;
                    }
                }
                sectorKeyData.close();
            }
        } else if (length > 0 && madData.equals(Constants.MAD_DATA_FOR_ONE_TEMPLATE)) {
            sectorKeyData = dbComm.getSectorAndKeyForReadCard();
            if (sectorKeyData != null) {
                wrCommand = "";
                while (sectorKeyData.moveToNext()) {
                    String strSectorNo = sectorKeyData.getString(0);
                    String strKey = sectorKeyData.getString(1);
                    int sectorNo = Integer.parseInt(strSectorNo);
                    status = false;
                    String cardData = "";
                    StringBuffer sb = new StringBuffer();
                    switch (sectorNo) {
                        case 0:
                            parseStatus = true;
                            break;
                        case 1:
                            parseStatus = true;
                            break;
                        case 2:
                            parseStatus = false;
                            wrCommand = Constants.RC522_SECTOR_READ_COMMAND + " " + Integer.toString(sectorNo) + " " + strKey + " " + Constants.RC522_KEY_TYPE_B;
                            status = comm.writeRC522(wrCommand);
                            if (status) {
                                char[] data = comm.readRC522();
                                if (data != null && data.length > 0) {
                                    String strData = new String(data);
                                    String[] arr = strData.split(":");
                                    if (arr != null && arr.length == 3) {
                                        String perInfo = arr[2].trim().toUpperCase();
                                        if (!perInfo.equals("RD-FAIL")) {
                                            perInfo = "00" + perInfo;
                                            perInfo = Utility.hexToAscii(perInfo);
                                            perInfo = Utility.removeNonAscii(perInfo);
                                            parseStatus = comm.parseSectorData(2, perInfo.getBytes(), cardInfo);
                                        }
                                    }
                                }
                            }
                            break;
                        case 3:
                            parseStatus = true;
                            break;
                        case 4:
                            parseStatus = false;
                            wrCommand = Constants.RC522_SECTOR_READ_COMMAND + " " + Integer.toString(sectorNo) + " " + strKey + " " + Constants.RC522_KEY_TYPE_B;
                            status = comm.writeRC522(wrCommand);
                            if (status) {
                                char[] data = comm.readRC522();
                                if (data != null && data.length > 0) {
                                    String strData = new String(data);
                                    String[] arr = strData.split(":");
                                    if (arr != null && arr.length == 3) {
                                        cardData = arr[2].trim();
                                        if (!cardData.equals("RD-FAIL")) {
                                            cardData = "0" + cardData;
                                            parseStatus = comm.parseSectorData(4, cardData.getBytes(), cardInfo);
                                        }
                                    }
                                }
                            }
                            break;
                        case 5:
                            parseStatus = false;
                            wrCommand = Constants.RC522_SECTOR_READ_COMMAND + " " + Integer.toString(sectorNo) + " " + strKey + " " + Constants.RC522_KEY_TYPE_B;
                            status = comm.writeRC522(wrCommand);
                            if (status) {
                                char[] data = comm.readRC522();
                                if (data != null && data.length > 0) {
                                    String strData = new String(data);
                                    String[] arr = strData.split(":");
                                    if (arr != null && arr.length == 3) {
                                        cardData = arr[2].trim();
                                        if (!cardData.equals("RD-FAIL")) {
                                            cardData = "0" + cardData;
                                            parseStatus = comm.parseSectorData(5, cardData.getBytes(), cardInfo);

                                        }
                                    }
                                }
                            }
                            break;
                        case 6:
                            parseStatus = false;
                            wrCommand = Constants.RC522_SECTOR_READ_COMMAND + " " + Integer.toString(sectorNo) + " " + strKey + " " + Constants.RC522_KEY_TYPE_B;
                            status = comm.writeRC522(wrCommand);
                            if (status) {
                                char[] data = comm.readRC522();
                                if (data != null && data.length > 0) {
                                    String strData = new String(data);
                                    String[] arr = strData.split(":");
                                    if (arr != null && arr.length == 3) {
                                        cardData = arr[2].trim();
                                        if (!cardData.equals("RD-FAIL")) {
                                            cardData = "0" + cardData;
                                            parseStatus = comm.parseSectorData(6, cardData.getBytes(), cardInfo);
                                        }
                                    }
                                }
                            }
                            break;
                        case 7:
                            parseStatus = false;
                            wrCommand = Constants.RC522_SECTOR_READ_COMMAND + " " + Integer.toString(sectorNo) + " " + strKey + " " + Constants.RC522_KEY_TYPE_B;
                            status = comm.writeRC522(wrCommand);
                            if (status) {
                                char[] data = comm.readRC522();
                                if (data != null && data.length > 0) {
                                    String strData = new String(data);
                                    String[] arr = strData.split(":");
                                    if (arr != null && arr.length == 3) {
                                        cardData = arr[2].trim();
                                        if (!cardData.equals("RD-FAIL")) {
                                            cardData = "0" + cardData;
                                            parseStatus = comm.parseSectorData(7, cardData.getBytes(), cardInfo);
                                        }
                                    }
                                }
                            }
                            break;
                        case 8:
                            parseStatus = false;
                            wrCommand = Constants.RC522_SECTOR_READ_COMMAND + " " + Integer.toString(sectorNo) + " " + strKey + " " + Constants.RC522_KEY_TYPE_B;
                            status = comm.writeRC522(wrCommand);
                            if (status) {
                                char[] data = comm.readRC522();
                                if (data != null && data.length > 0) {
                                    String strData = new String(data);
                                    String[] arr = strData.split(":");
                                    if (arr != null && arr.length == 3) {
                                        cardData = arr[2].trim();
                                        if (!cardData.equals("RD-FAIL")) {
                                            cardData = "0" + cardData;
                                            parseStatus = comm.parseSectorData(8, cardData.getBytes(), cardInfo);
                                        }
                                    }
                                }
                            }
                            break;
                        case 9:
                            parseStatus = false;
                            wrCommand = Constants.RC522_SECTOR_READ_COMMAND + " " + Integer.toString(sectorNo) + " " + strKey + " " + Constants.RC522_KEY_TYPE_B;
                            status = comm.writeRC522(wrCommand);
                            if (status) {
                                char[] data = comm.readRC522();
                                if (data != null && data.length > 0) {
                                    String strData = new String(data);
                                    String[] arr = strData.split(":");
                                    if (arr != null && arr.length == 3) {
                                        cardData = arr[2].trim();
                                        if (!cardData.equals("RD-FAIL")) {
                                            cardData = "0" + cardData;
                                            parseStatus = comm.parseSectorData(9, cardData.getBytes(), cardInfo);
                                        }
                                    }
                                }
                            }
                            break;
                        case 10:
                            parseStatus = true;
                            break;
                        case 11:
                            parseStatus = true;
                            break;
                        case 12:
                            parseStatus = true;
                            break;
                        case 13:
                            parseStatus = true;
                            break;
                        case 14:
                            parseStatus = true;
                            break;
                        case 15:
                            parseStatus = true;
                            break;
                    }
                    if (!parseStatus) {
                        break;
                    }
                }
                sectorKeyData.close();
            }
        } else {
            parseStatus = false;
        }
        return parseStatus;
    }

    private void initCFVByVM(final SmartCardInfo cardDetails) {//Init Card Finger Verification by Verification Mode
        String verificationMode = cardDetails.getFirstFingerVerificationMode();
        if (verificationMode != null) {
            switch (verificationMode) {
                case "CARD-ONLY":
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!isFinishing()) {
                                if (successEmpDetailsDialog != null && successEmpDetailsDialog.isShowing()) {
                                    successEmpDetailsDialog.cancel();
                                }
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ForlinxGPIO.runGPIOLEDForSuccess();
                                    }
                                }).start();
                                showSuccessCustomDialogForCardVerification("Verification Success", cardDetails);
                                pHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        successEmpDetailsDialog.cancel();
                                        isCardReadingBlocked = false;
                                        WiegandCommunicator.clearWiegand(Constants.WIEGAND_WRITE_PATH, "1");
                                        isWiegandReading = false;
                                        initIdentification();
                                    }

                                }, 1000);
                            }
                        }
                    });
                    break;
                case "FORCE ENROLL":
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!isFinishing()) {
                                if (successEmpDetailsDialog != null && successEmpDetailsDialog.isShowing()) {
                                    successEmpDetailsDialog.cancel();
                                }
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ForlinxGPIO.runGPIOLEDForSuccess();
                                    }
                                }).start();
                                showSuccessCustomDialogForCardVerification("Verification Success", cardDetails);
                                pHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        successEmpDetailsDialog.cancel();
                                        isCardReadingBlocked = false;
                                        isWiegandReading = false;
                                        initIdentification();
                                    }

                                }, 1000);
                            }
                        }
                    });
                    break;
                case "CARD+FINGER":
                    if (!isFinishing()) {
                        morphoComm.startFingerVerification(Constants.VERIFY_BY_CARD_MODE, cardDetails);
                    }
                    break;
                case "CARD+PIN+FINGER":
                    if (!isFinishing()) {
                        morphoComm.startFingerVerification(Constants.VERIFY_BY_CARD_MODE, cardDetails);
                    }
                    break;
                case "1:N":
                    if (!isFinishing()) {
                        morphoComm.startFingerVerification(Constants.VERIFY_BY_CARD_MODE, cardDetails);
                    }
                    break;
                default:
                    if (!isFinishing()) {
                        morphoComm.startFingerVerification(Constants.VERIFY_BY_CARD_MODE, cardDetails);
                    }
                    break;
            }
        }
    }


    private MicroSmartV2Communicator getReaderConnection() {
        MicroSmartV2Communicator conn = null;
        UsbDeviceConnection usbConn = SmartReaderConnection.getInstance().getmConnection();
        UsbInterface usbInterface = SmartReaderConnection.getInstance().getIntf();
        UsbEndpoint usbInput = SmartReaderConnection.getInstance().getInput();
        UsbEndpoint usbOutput = SmartReaderConnection.getInstance().getOutput();
        if (usbConn != null && usbInterface != null && usbInput != null && usbOutput != null) {
            conn = new MicroSmartV2Communicator(usbConn, usbInterface, usbInput, usbOutput);
        }
        return conn;
    }

    private int readSpiCardTemplate() {

        int error = -1;

        DataBaseLayer dbLayer = new DataBaseLayer();
        Cursor sectorKeyData = dbLayer.getSectorAndKeyForRC632CardRead();

        if (sectorKeyData != null) {

            byte[] cardReadData = null;
            int keyFlag = 0;

            while (sectorKeyData.moveToNext()) {

                String strSectorNo = sectorKeyData.getString(0);
                String strKey = sectorKeyData.getString(1);
                int sectorNo = Integer.parseInt(strSectorNo);
                byte[] keyB = new byte[6];

                switch (sectorNo) {

                    case 1://Sector 1 cannot be read with Key B because of different access code

                        int key_flag = 1;
                        byte[] keyA = new byte[]{0x45, 0x44, 0x43, 0x42, 0x41, 0x40};
                        cardReadData = new byte[48];
                        error = rc632ReaderConnection.sectorRead(key_flag, keyA, (byte) sectorNo, cardReadData);
                        if (error == 0) {
                            // parseSector_1(cardReadData);
                        }

                        break;

                    case 2:   // Sector 2

                        cardReadData = new byte[48];
                        keyB = strKey.getBytes();
                        error = rc632ReaderConnection.sectorRead(keyFlag, keyB, (byte) sectorNo, cardReadData);
                        if (error == 0) {
                            // parseSector_2(cardReadData);
                        }

                        break;

                    case 4:   // Sector 4

                        cardReadData = new byte[48];
                        keyB = strKey.getBytes();
                        error = rc632ReaderConnection.sectorRead(keyFlag, keyB, (byte) sectorNo, cardReadData);
                        if (error == 0) {
                            // parseSector_4_8(cardReadData);
                        }

                        break;

                    case 5:          // Sector 5

                        cardReadData = new byte[48];
                        keyB = strKey.getBytes();
                        error = rc632ReaderConnection.sectorRead(keyFlag, keyB, (byte) sectorNo, cardReadData);
                        if (error == 0) {
                            // parseSector_4_8(cardReadData);
                        }

                        break;

                    case 6:      // Sector 6

                        cardReadData = new byte[48];
                        keyB = strKey.getBytes();
                        error = rc632ReaderConnection.sectorRead(keyFlag, keyB, (byte) sectorNo, cardReadData);
                        if (error == 0) {
                            // parseSector_4_8(cardReadData);
                        }

                        break;

                    case 7:      // Sector 7

                        cardReadData = new byte[48];
                        keyB = strKey.getBytes();
                        error = rc632ReaderConnection.sectorRead(keyFlag, keyB, (byte) sectorNo, cardReadData);
                        if (error == 0) {
                            // parseSector_4_8(cardReadData);
                        }

                        break;

                    case 8:   // Sector 8

                        cardReadData = new byte[48];
                        keyB = strKey.getBytes();
                        error = rc632ReaderConnection.sectorRead(keyFlag, keyB, (byte) sectorNo, cardReadData);
                        if (error == 0) {
                            // parseSector_4_8(cardReadData);
                        }

                        break;

                    case 9:      // Sector 9

                        cardReadData = new byte[48];
                        keyB = strKey.getBytes();
                        error = rc632ReaderConnection.sectorRead(keyFlag, keyB, (byte) sectorNo, cardReadData);
                        if (error == 0) {
                            // parseSector_9(cardReadData);
                        }

                        break;

                    case 10:     // Sector A

                        cardReadData = new byte[48];
                        keyB = strKey.getBytes();
                        error = rc632ReaderConnection.sectorRead(keyFlag, keyB, (byte) sectorNo, cardReadData);
                        if (error == 0) {
                            //  parseSector_10_14(cardReadData);
                        }

                        break;

                    case 11:     // Sector B

                        cardReadData = new byte[48];
                        keyB = strKey.getBytes();
                        error = rc632ReaderConnection.sectorRead(keyFlag, keyB, (byte) sectorNo, cardReadData);
                        if (error == 0) {
                            // parseSector_10_14(cardReadData);
                        }

                        break;

                    case 12:     // Sector C

                        cardReadData = new byte[48];
                        keyB = strKey.getBytes();
                        error = rc632ReaderConnection.sectorRead(keyFlag, keyB, (byte) sectorNo, cardReadData);
                        if (error == 0) {
                            // parseSector_10_14(cardReadData);
                        }

                        break;

                    case 13:     // Sector D

                        cardReadData = new byte[48];
                        keyB = strKey.getBytes();
                        error = rc632ReaderConnection.sectorRead(keyFlag, keyB, (byte) sectorNo, cardReadData);
                        if (error == 0) {
                            // parseSector_10_14(cardReadData);
                        }

                        break;

                    case 14:     // Sector E

                        cardReadData = new byte[48];
                        keyB = strKey.getBytes();
                        error = rc632ReaderConnection.sectorRead(keyFlag, keyB, (byte) sectorNo, cardReadData);
                        if (error == 0) {
                            // parseSector_10_14(cardReadData);
                        }

                        break;

                    case 15:     // Sector F

                        cardReadData = new byte[48];
                        keyB = strKey.getBytes();
                        error = rc632ReaderConnection.sectorRead(keyFlag, keyB, (byte) sectorNo, cardReadData);
                        if (error == 0) {
                            // parseSector_15(cardReadData);
                        }

                        break;

                    default:
                        break;
                }
                if (error == 0) {
                    continue;
                } else {
                    break;
                }
            }
            if (sectorKeyData != null) {
                sectorKeyData.close();
            }
        }
        return error;
    }

    public void startTimer() {
        if (httpDataTransferTimer == null && recordUpdateTimer == null && wigReadTimer == null && capReadTimer == null && batReadTimer == null && cardReadTimer == null && attendanceModeTimer == null) {

            httpDataTransferTimer = new Timer();
            recordUpdateTimer = new Timer();
            wigReadTimer = new Timer();
            capReadTimer = new Timer();
            batReadTimer = new Timer();
            adcReadTimer = new Timer();
            cardReadTimer = new Timer();
            attendanceModeTimer = new Timer();

            initializeTimerTask();

            httpDataTransferTimer.schedule(httpDataTransferTimerTask, 0, 10000); //
            recordUpdateTimer.schedule(recordUpdateTimerTask, 0, 1000); //
            wigReadTimer.schedule(wigReadTimerTask, 0, 100);
            capReadTimer.schedule(capReadTimerTask, 0, 100);
            batReadTimer.schedule(batReadTimerTask, 0, 500);
            adcReadTimer.schedule(adcReadTimerTask, 0, 50); //100
            cardReadTimer.schedule(cardReadTimerTask, 0, 500); //1000
            attendanceModeTimer.schedule(attendanceModeTimerTask, 0, 1000); //100
        }
    }

    public void initializeTimerTask() {

        cardReadTimerTask = new TimerTask() {
            public void run() {
                cardHandler.post(new Runnable() {
                    public void run() {
                        int smartReader = Settings.getInstance().getSrTypeValue();
                        switch (smartReader) {
                            case 0:
                                readCardTemplate(smartReader);
                                break;
                            case 1:
                                readCardTemplate(smartReader);
                                break;
                            case 2:
                                if (!isWiegandReading) {
                                    readCardTemplate(smartReader);
                                }
                                break;
                            default:
                                showCustomAlertDialog(false, "Device Connection Status", "Hardware Settings Not Configured");
                                break;
                        }
                    }
                });
            }
        };


        attendanceModeTimerTask = new TimerTask() {
            public void run() {
                amHandler.post(new Runnable() {
                    public void run() {
                        if (!stopModeUpdate) {
                            modeSet = false;
                            ArrayList <String> list = dbComm.getInTimeRange();
                            if (list != null && list.size() == 2) {
                                String strInStartime = list.get(0).trim();
                                String strInEndTime = list.get(1).trim();
                                Calendar mcurrentTime = Calendar.getInstance();
                                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                                int minute = mcurrentTime.get(Calendar.MINUTE);
                                String strHour = Integer.toString(hour);
                                String strMin = Integer.toString(minute);

                                if (strHour.trim().length() == 1) {
                                    strHour = "0" + strHour;
                                }
                                if (strMin.trim().length() == 1) {
                                    strMin = "0" + strMin;
                                }
                                String strCurrentime = strHour + strMin;
                                try {
                                    if (!modeSet) {
                                        if (Utility.isTimeBetweenTwoTime(strInStartime, strInEndTime, strCurrentime)) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    tvStateofToggleButton.setText("IN");
                                                    tButton.setVisibility(View.VISIBLE);
                                                    tButton.setChecked(true);
                                                    modeSet = true;
                                                }
                                            });
                                        } else {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    tButton.setVisibility(View.INVISIBLE);
                                                    tvStateofToggleButton.setText("NUL");
                                                    modeSet = false;
                                                }
                                            });
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.d("TEST", "In Mode Update Error:" + e.getMessage());
                                }
                            }

                            list = dbComm.getOutTimeRange();
                            if (list != null && list.size() == 2) {
                                String strOutStartime = list.get(0).trim();
                                String strOutEndTime = list.get(1).trim();
                                Calendar mcurrentTime = Calendar.getInstance();
                                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                                int minute = mcurrentTime.get(Calendar.MINUTE);
                                String strHour = Integer.toString(hour);
                                String strMin = Integer.toString(minute);

                                if (strHour.trim().length() == 1) {
                                    strHour = "0" + strHour;
                                }
                                if (strMin.trim().length() == 1) {
                                    strMin = "0" + strMin;
                                }

                                String strCurrentime = strHour + strMin;
                                try {
                                    if (!modeSet) {
                                        if (Utility.isTimeBetweenTwoTime(strOutStartime, strOutEndTime, strCurrentime)) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    tButton.setVisibility(View.VISIBLE);
                                                    tButton.setChecked(false);
                                                    tvStateofToggleButton.setText("OUT");
                                                    modeSet = true;
                                                }
                                            });
                                        } else {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    tvStateofToggleButton.setText("NUL");
                                                    tButton.setVisibility(View.INVISIBLE);
                                                    modeSet = false;
                                                }
                                            });
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.d("TEST", "Out Mode Update Error:" + e.getMessage());
                                }
                            }
                        }
                    }
                });
            }
        };

        httpDataTransferTimerTask = new TimerTask() {
            public void run() {
                dHandler.post(new Runnable() {
                    public void run() {
                        try {
                            boolean isWifiAvailable = isNetworkAvailable();
                            if (isWifiAvailable) {
                                CheckInternetTask task = new CheckInternetTask();
                                task.execute();
                            } else {
                                updateInternetConnection("No Wifi Connection", false);
                            }
                        } catch (Exception ex) {
                        }
                    }
                });
            }
        };

        recordUpdateTimerTask = new TimerTask() {
            public void run() {
                rHandler.post(new Runnable() {
                    public void run() {
                        try {
                            SQLiteCommunicator dbComm = new SQLiteCommunicator();
                            int noOfRecords = dbComm.getNoOfRecordsToBeSendToServer();
                            recordCount.setText("" + Integer.toString(noOfRecords));
                        } catch (Exception ex) {
                            Log.d("TEST", "Exception:" + ex.getMessage());
                        }
                    }
                });
            }
        };

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


//        usbDetectTimerTask = new TimerTask() {
//            public void run() {
//                usbHandler.post(new Runnable() {
//                    public void run() {
//                        char[] data = ForlinxGPIOCommunicator.readGPIO(Constants.POWER_DETECT);
//                        if (data != null) {
//                            String val = new String(data);
//                            if (val != null) {
//                                val = val.trim();
//                               // Toast.makeText(EmployeeAttendanceActivity.this,"val:"+val,Toast.LENGTH_SHORT).show();
//                                if (val.length() > 0) {
//                                    if (val.equals("0")) {
//                                        tvBatPer.setVisibility(View.GONE);
//                                        ivBatTop.setVisibility(View.GONE);
//                                        pbBatPer.setVisibility(View.GONE);
//                                        tvPower.setVisibility(View.VISIBLE);
//                                        ivChargeIcon.setVisibility(View.VISIBLE);
//                                    } else if (val.equals("1")) {
//                                        tvBatPer.setVisibility(View.VISIBLE);
//                                        pbBatPer.setVisibility(View.VISIBLE);
//                                        ivBatTop.setVisibility(View.VISIBLE);
//                                        tvPower.setVisibility(View.GONE);
//                                        ivChargeIcon.setVisibility(View.GONE);
//                                        pbBatPer.setProgress(per);
//                                        tvBatPer.setText("" + per + "%");
//                                    }
//                                }
//                            }
//                        }
//                    }
//                });
//            }
//        };


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

        capReadTimerTask = new TimerTask() {
            public void run() {
                cHandler.post(new Runnable() {
                    public void run() {
                        char[] val = I2CCommunicator.readI2C(Constants.CAP_READ_PATH);
                        if (val != null && val.length > 0) {
                            String capVal = new String(val);
                            capVal = capVal.trim();
                            // Log.d("TEST","Cap val:"+capVal);
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
                                            stopModeUpdate = true;
                                            String strSelecetedMode = tvStateofToggleButton.getText().toString().trim();
                                            if (strSelecetedMode.equals("IN")) {
                                                tButton.setChecked(false);
                                                tvStateofToggleButton.setText("OUT");
                                            } else if (strSelecetedMode.equals("OUT")) {
                                                tButton.setChecked(true);
                                                tvStateofToggleButton.setText("IN");
                                            } else if (strSelecetedMode.equals("NUL")) {
                                                tButton.setVisibility(View.VISIBLE);
                                                tButton.setChecked(true);
                                                tvStateofToggleButton.setText("IN");
                                            }
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
                                            if (!isPassDlgVisible) {
                                                if (passwordDialog != null && passwordDialog.isShowing()) {
                                                    passwordDialog.dismiss();
                                                }
                                                showPwdDialog();
                                            } else {
                                                if (passwordDialog != null && passwordDialog.isShowing()) {
                                                    isPassDlgVisible = false;
                                                    passwordDialog.dismiss();
                                                }
                                            }
                                        }
                                    }
                                    break;
                                case "33":
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

        wigReadTimerTask = new TimerTask() {
            public void run() {
                wHandler.post(new Runnable() {
                    public void run() {
                        //.26:9f948b:10458251:01001111110010100100010110.
                        if (!isCardReadingBlocked) {
                            if (!isWiegandReading) {
                                char[] wigData = WiegandCommunicator.readWiegand(Constants.WIEGAND_READ_PATH);
                                if (wigData != null && wigData.length > 0) {
                                    boolean status = WiegandCommunicator.clearWiegand(Constants.WIEGAND_WRITE_PATH, "1");
                                    if (status) {
                                        String strWigData = new String(wigData);
                                        // Log.d("TEST","WIGEND DATA:"+strWigData);
                                        String[] arr = strWigData.split(":");
                                        if (arr != null && arr.length == 4) {
                                            String val = arr[0].substring(1).trim();
                                            if (val != null && val.equals("26")) {
                                                SQLiteCommunicator dbComm = new SQLiteCommunicator();
                                                WiegandSettingsInfo info = null;
                                                info = dbComm.getWiegandSettings(info);
                                                if (info != null) {
                                                    String cardVal = "";
                                                    String rawData = "";
                                                    val = info.getIsHexToDecEnabled();
                                                    switch (val) {
                                                        case "Y":
                                                            rawData = arr[3].substring(0, 26);
                                                            int rawWigVal;
                                                            boolean isValid = false;
                                                            try {
                                                                rawWigVal = Integer.parseInt(rawData, 2);
                                                                isValid = true;
                                                            } catch (NumberFormatException ne) {
                                                            }
                                                            if (isValid) {
                                                                isWiegandReading = true;
                                                                rawData = rawData.substring(1, 25);
                                                                val = info.getIsSiteCodeEnabled();
                                                                switch (val) {
                                                                    case "Y":
                                                                        String siteCode, cardNo;
                                                                        int siteCodeDec, cardNoDec;
                                                                        val = info.getCardNoType();
                                                                        switch (val) {
                                                                            case "0"://16 bit Hex to Dec
                                                                                rawData = rawData.substring(8);
                                                                                cardNoDec = Integer.parseInt(rawData, 2);
                                                                                cardVal = Integer.toString(cardNoDec);
                                                                                startWiegandVerify(cardVal);
                                                                                break;
                                                                            case "1"://24 bit Hex to Dec
                                                                                rawData = rawData.substring(0);
                                                                                cardNoDec = Integer.parseInt(rawData, 2);
                                                                                cardVal = Integer.toString(cardNoDec);
                                                                                startWiegandVerify(cardVal);
                                                                                break;
                                                                            case "2"://32 bit Hex to Dec
                                                                                WiegandCommunicator.clearWiegand(Constants.WIEGAND_WRITE_PATH, "1");
                                                                                isWiegandReading = false;
                                                                                break;
                                                                            case "3":// Site Code + 16 bit
                                                                                siteCode = rawData.substring(0, 8);
                                                                                cardNo = rawData.substring(8);
                                                                                siteCodeDec = Integer.parseInt(siteCode, 2);
                                                                                cardNoDec = Integer.parseInt(cardNo, 2);
                                                                                cardVal = Integer.toString(siteCodeDec) + Integer.toString(cardNoDec);
                                                                                startWiegandVerify(cardVal);
                                                                                break;
                                                                            case "4"://Site Code + 24 bit
                                                                                WiegandCommunicator.clearWiegand(Constants.WIEGAND_WRITE_PATH, "1");
                                                                                isWiegandReading = false;
                                                                                break;

                                                                            default:
                                                                                WiegandCommunicator.clearWiegand(Constants.WIEGAND_WRITE_PATH, "1");
                                                                                isWiegandReading = false;
                                                                                break;

                                                                        }
                                                                        break;
                                                                    case "N":
                                                                        val = info.getCardNoType();
                                                                        switch (val) {
                                                                            case "0"://16 bit Hex to Dec
                                                                                rawData = rawData.substring(8);
                                                                                cardNoDec = Integer.parseInt(rawData, 2);
                                                                                cardVal = Integer.toString(cardNoDec);
                                                                                startWiegandVerify(cardVal);
                                                                                break;
                                                                            case "1"://24 bit Hex to Dec
                                                                                rawData = rawData.substring(0);
                                                                                cardNoDec = Integer.parseInt(rawData, 2);
                                                                                cardVal = Integer.toString(cardNoDec);
                                                                                startWiegandVerify(cardVal);
                                                                                break;
                                                                            case "2"://32 bit Hex to Dec
                                                                                WiegandCommunicator.clearWiegand(Constants.WIEGAND_WRITE_PATH, "1");
                                                                                isWiegandReading = false;
                                                                                break;
                                                                            default:
                                                                                WiegandCommunicator.clearWiegand(Constants.WIEGAND_WRITE_PATH, "1");
                                                                                isWiegandReading = false;
                                                                                break;
                                                                        }
                                                                        break;
                                                                    default:
                                                                        WiegandCommunicator.clearWiegand(Constants.WIEGAND_WRITE_PATH, "1");
                                                                        isWiegandReading = false;
                                                                        break;
                                                                }
                                                            }
                                                            break;
                                                        case "N":
                                                            cardVal = arr[1].trim().toUpperCase();
                                                            isWiegandReading = true;
                                                            startWiegandVerify(cardVal);
                                                            break;
                                                        default:
                                                            WiegandCommunicator.clearWiegand(Constants.WIEGAND_WRITE_PATH, "1");
                                                            isWiegandReading = false;
                                                            break;
                                                    }
                                                }
                                            } else if (val != null && val.equals("34")) {
                                                boolean isValid = false;
                                                SQLiteCommunicator dbComm = new SQLiteCommunicator();
                                                WiegandSettingsInfo info = null;
                                                info = dbComm.getWiegandSettings(info);
                                                if (info != null) {
                                                    String cardVal = "";
                                                    String rawData = "";
                                                    val = info.getIsHexToDecEnabled();
                                                    switch (val) {
                                                        case "Y":
                                                            int rawWigVal;
                                                            rawData = arr[3].substring(0, 34);
                                                            try {
                                                                rawWigVal = Integer.parseInt(rawData, 2);
                                                                isValid = true;
                                                            } catch (NumberFormatException ne) {
                                                            }
                                                            if (isValid) {
                                                                isWiegandReading = true;
                                                                rawData = rawData.substring(1, 33);
                                                                val = info.getIsSiteCodeEnabled();
                                                                switch (val) {
                                                                    case "Y":
                                                                        String siteCode, cardNo;
                                                                        int siteCodeDec, cardNoDec;
                                                                        val = info.getCardNoType();
                                                                        switch (val) {
                                                                            case "0"://16 bit Hex to Dec
                                                                                rawData = rawData.substring(16);
                                                                                cardNoDec = Integer.parseInt(rawData, 2);
                                                                                cardVal = Integer.toString(cardNoDec);
                                                                                startWiegandVerify(cardVal);
                                                                                break;
                                                                            case "1"://24 bit Hex to Dec
                                                                                rawData = rawData.substring(8);
                                                                                cardNoDec = Integer.parseInt(rawData, 2);
                                                                                cardVal = Integer.toString(cardNoDec);
                                                                                startWiegandVerify(cardVal);
                                                                                break;
                                                                            case "2"://32 bit Hex to Dec
                                                                                rawData = rawData.substring(0);
                                                                                cardNoDec = Integer.parseInt(rawData, 2);
                                                                                cardVal = Integer.toString(cardNoDec);
                                                                                startWiegandVerify(cardVal);
                                                                                break;
                                                                            case "3":// Site Code + 16 bit
                                                                                siteCode = rawData.substring(0, 8);
                                                                                cardNo = rawData.substring(16);
                                                                                siteCodeDec = Integer.parseInt(siteCode, 2);
                                                                                cardNoDec = Integer.parseInt(cardNo, 2);
                                                                                cardVal = Integer.toString(siteCodeDec) + Integer.toString(cardNoDec);
                                                                                startWiegandVerify(cardVal);
                                                                                break;
                                                                            case "4"://Site Code + 24 bit
                                                                                siteCode = rawData.substring(0, 8);
                                                                                cardNo = rawData.substring(8);
                                                                                siteCodeDec = Integer.parseInt(siteCode, 2);
                                                                                cardNoDec = Integer.parseInt(cardNo, 2);
                                                                                cardVal = Integer.toString(siteCodeDec) + Integer.toString(cardNoDec);
                                                                                startWiegandVerify(cardVal);
                                                                                break;
                                                                        }
                                                                        break;
                                                                    case "N":
                                                                        val = info.getCardNoType();
                                                                        switch (val) {
                                                                            case "0"://16 bit Hex to Dec
                                                                                rawData = rawData.substring(16);
                                                                                cardNoDec = Integer.parseInt(rawData, 2);
                                                                                cardVal = Integer.toString(cardNoDec);
                                                                                startWiegandVerify(cardVal);
                                                                                break;
                                                                            case "1"://24 bit Hex to Dec
                                                                                rawData = rawData.substring(8);
                                                                                cardNoDec = Integer.parseInt(rawData, 2);
                                                                                cardVal = Integer.toString(cardNoDec);
                                                                                startWiegandVerify(cardVal);
                                                                                break;
                                                                            case "2"://32 bit Hex to Dec
                                                                                rawData = rawData.substring(0);
                                                                                cardNoDec = Integer.parseInt(rawData, 2);
                                                                                cardVal = Integer.toString(cardNoDec);
                                                                                startWiegandVerify(cardVal);
                                                                                break;
                                                                        }
                                                                        break;
                                                                }
                                                            }
                                                            break;
                                                        case "N":
                                                            cardVal = arr[1].trim().toUpperCase();
                                                            isWiegandReading = true;
                                                            startWiegandVerify(cardVal);
                                                            break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                });
            }
        };


        //================================= Commented On 22-01-2019 ================================//

//        wigReadTimerTask = new TimerTask() {
//            public void run() {
//                wHandler.post(new Runnable() {
//                    public void run() {
//                        //.26:9f948b:10458251:01001111110010100100010110.
//                        if (!isWiegandReading) {
//                            char[] wigData = WiegandCommunicator.readWiegand(Constants.WIEGAND_READ_PATH);
//                            if (wigData != null && wigData.length > 0) {
//                                boolean status = WiegandCommunicator.clearWiegand(Constants.WIEGAND_WRITE_PATH, "1");
//                                isWiegandReading = true;
//                                if (status) {
//                                    String strWigData = new String(wigData);
//                                    String[] arr = strWigData.split(":");
//                                    if (arr != null && arr.length == 4) {
//                                        String val = arr[0].substring(1).trim();
//                                        if (val != null && val.equals("26")) {
//                                            SQLiteCommunicator dbComm = new SQLiteCommunicator();
//                                            WiegandSettingsInfo info = null;
//                                            info = dbComm.getWiegandSettings(info);
//                                            if (info != null) {
//                                                String cardVal = "";
//                                                String rawData = "";
//                                                val = info.getIsHexToDecEnabled();
//                                                switch (val) {
//                                                    case "Y":
//                                                        rawData = arr[3].substring(0, 26);
//                                                        try {
//                                                            int check = Integer.parseInt(rawData, 2);
//                                                            new Thread(new Runnable() {
//                                                                @Override
//                                                                public void run() {
//                                                                    ForlinxGPIO.runGPIOLEDForWiegandRead();
//                                                                }
//                                                            }).start();
//                                                            rawData = rawData.substring(1, 25);
//                                                            val = info.getIsSiteCodeEnabled();
//                                                            switch (val) {
//                                                                case "Y":
//                                                                    int autoId = -1;
//                                                                    String siteCode, cardNo;
//                                                                    int siteCodeDec, cardNoDec;
//                                                                    val = info.getCardNoType();
//                                                                    switch (val) {
//                                                                        case "0"://16 bit Hex to Dec
//                                                                            rawData = rawData.substring(8);
//                                                                            Log.d("TEST", "YY0 Raw Data:" + rawData + " Len:" + rawData.length());
//                                                                            cardNoDec = Integer.parseInt(rawData, 2);
//                                                                            cardVal = Integer.toString(cardNoDec);
//
//                                                                            //======================= Verify using Card Id =====================//
//
//                                                                            autoId = dbComm.isDataAvailableInDatabaseByCardId(cardVal);
//                                                                            if (autoId != -1) {
//                                                                                EmployeeInfo empInfo = new EmployeeInfo();
//                                                                                boolean isFingerEnrolled = dbComm.getEmployeeInfo(autoId, empInfo);
//                                                                                if (isFingerEnrolled) {
//                                                                                    int noOfTemplates = empInfo.getNoOfTemplates();
//                                                                                    if (noOfTemplates == 1) {
//                                                                                        textViewPutFingerMessage.startAnimation(anim);
//                                                                                        textViewPutFingerMessage.setText("Put " + empInfo.getFirstFingerIndex() + " To Be Verified");
//                                                                                    } else if (noOfTemplates == 2) {
//                                                                                        textViewPutFingerMessage.startAnimation(anim);
//                                                                                        textViewPutFingerMessage.setText("Put " + empInfo.getFirstFingerIndex() + " Or " + empInfo.getSecondFingerIndex() + " To Be Verified");
//                                                                                    }
//
//                                                                                    stopModeUpdate = true;
//                                                                                    isVerificationStarted = true;
//                                                                                    morphoComm.stopFingerIdentification();//Abort Finger Identification
//
//                                                                                    try {
//                                                                                        Thread.sleep(500);
//                                                                                    } catch (InterruptedException ex) {
//                                                                                    }
//
//                                                                                    morphoComm.startFingerVerification(Constants.VERIFY_BY_LOCAL_DATABASE_MODE, empInfo);
//                                                                                }
//                                                                            }
//                                                                            break;
//
//                                                                        case "1"://24 bit Hex to Dec
//                                                                            rawData = rawData.substring(0);
//                                                                            Log.d("TEST", "YY1 Raw Data:" + rawData + " Len:" + rawData.length());
//                                                                            cardNoDec = Integer.parseInt(rawData, 2);
//                                                                            cardVal = Integer.toString(cardNoDec);
//
//                                                                            //======================= Verify using Card Id =====================//
//
//                                                                            autoId = dbComm.isDataAvailableInDatabaseByCardId(cardVal);
//                                                                            if (autoId != -1) {
//                                                                                EmployeeInfo empInfo = new EmployeeInfo();
//                                                                                boolean isFingerEnrolled = dbComm.getEmployeeInfo(autoId, empInfo);
//                                                                                if (isFingerEnrolled) {
//                                                                                    int noOfTemplates = empInfo.getNoOfTemplates();
//                                                                                    if (noOfTemplates == 1) {
//                                                                                        textViewPutFingerMessage.startAnimation(anim);
//                                                                                        textViewPutFingerMessage.setText("Put " + empInfo.getFirstFingerIndex() + " To Be Verified");
//                                                                                    } else if (noOfTemplates == 2) {
//                                                                                        textViewPutFingerMessage.startAnimation(anim);
//                                                                                        textViewPutFingerMessage.setText("Put " + empInfo.getFirstFingerIndex() + " Or " + empInfo.getSecondFingerIndex() + " To Be Verified");
//                                                                                    }
//
//                                                                                    stopModeUpdate = true;
//                                                                                    isVerificationStarted = true;
//                                                                                    morphoComm.stopFingerIdentification();//Abort Finger Identification
//
//                                                                                    try {
//                                                                                        Thread.sleep(500);
//                                                                                    } catch (InterruptedException ex) {
//                                                                                    }
//
//                                                                                    morphoComm.startFingerVerification(Constants.VERIFY_BY_LOCAL_DATABASE_MODE, empInfo);
//                                                                                }
//                                                                            }
//                                                                            break;
//                                                                        case "2"://32 bit Hex to Dec
//                                                                            break;
//                                                                        case "3":// Site Code + 16 bit
//                                                                            siteCode = rawData.substring(0, 8);
//                                                                            cardNo = rawData.substring(8);
//                                                                            Log.d("TEST", "YY3SC+16B Raw Data:" + rawData + " Len:" + rawData.length());
//                                                                            siteCodeDec = Integer.parseInt(siteCode, 2);
//                                                                            cardNoDec = Integer.parseInt(cardNo, 2);
//                                                                            cardVal = Integer.toString(siteCodeDec) + Integer.toString(cardNoDec);
//
//                                                                            //======================= Verify using Card Id =====================//
//
//                                                                            autoId = dbComm.isDataAvailableInDatabaseByCardId(cardVal);
//                                                                            if (autoId != -1) {
//                                                                                EmployeeInfo empInfo = new EmployeeInfo();
//                                                                                boolean isFingerEnrolled = dbComm.getEmployeeInfo(autoId, empInfo);
//                                                                                if (isFingerEnrolled) {
//                                                                                    int noOfTemplates = empInfo.getNoOfTemplates();
//                                                                                    if (noOfTemplates == 1) {
//                                                                                        textViewPutFingerMessage.startAnimation(anim);
//                                                                                        textViewPutFingerMessage.setText("Put " + empInfo.getFirstFingerIndex() + " To Be Verified");
//                                                                                    } else if (noOfTemplates == 2) {
//                                                                                        textViewPutFingerMessage.startAnimation(anim);
//                                                                                        textViewPutFingerMessage.setText("Put " + empInfo.getFirstFingerIndex() + " Or " + empInfo.getSecondFingerIndex() + " To Be Verified");
//                                                                                    }
//
//                                                                                    stopModeUpdate = true;
//                                                                                    isVerificationStarted = true;
//                                                                                    morphoComm.stopFingerIdentification();//Abort Finger Identification
//
//                                                                                    try {
//                                                                                        Thread.sleep(500);
//                                                                                    } catch (InterruptedException ex) {
//                                                                                    }
//
//                                                                                    morphoComm.startFingerVerification(Constants.VERIFY_BY_LOCAL_DATABASE_MODE, empInfo);
//                                                                                }
//                                                                            }
//
//                                                                            break;
//                                                                        case "4"://Site Code + 24 bit
//                                                                            break;
//
//                                                                    }
//                                                                    break;
//                                                                case "N":
//                                                                    val = info.getCardNoType();
//                                                                    switch (val) {
//                                                                        case "0"://16 bit Hex to Dec
//                                                                            rawData = rawData.substring(8);
//                                                                            Log.d("TEST", "YN016B Raw Data:" + rawData + " Len:" + rawData.length());
//                                                                            cardNoDec = Integer.parseInt(rawData, 2);
//                                                                            cardVal = Integer.toString(cardNoDec);
//
//                                                                            //======================= Verify using Card Id =====================//
//
//                                                                            autoId = dbComm.isDataAvailableInDatabaseByCardId(cardVal);
//                                                                            if (autoId != -1) {
//                                                                                EmployeeInfo empInfo = new EmployeeInfo();
//                                                                                boolean isFingerEnrolled = dbComm.getEmployeeInfo(autoId, empInfo);
//                                                                                if (isFingerEnrolled) {
//                                                                                    int noOfTemplates = empInfo.getNoOfTemplates();
//                                                                                    if (noOfTemplates == 1) {
//                                                                                        textViewPutFingerMessage.startAnimation(anim);
//                                                                                        textViewPutFingerMessage.setText("Put " + empInfo.getFirstFingerIndex() + " To Be Verified");
//                                                                                    } else if (noOfTemplates == 2) {
//                                                                                        textViewPutFingerMessage.startAnimation(anim);
//                                                                                        textViewPutFingerMessage.setText("Put " + empInfo.getFirstFingerIndex() + " Or " + empInfo.getSecondFingerIndex() + " To Be Verified");
//                                                                                    }
//
//                                                                                    stopModeUpdate = true;
//                                                                                    isVerificationStarted = true;
//                                                                                    morphoComm.stopFingerIdentification();//Abort Finger Identification
//
//                                                                                    try {
//                                                                                        Thread.sleep(500);
//                                                                                    } catch (InterruptedException ex) {
//                                                                                    }
//
//                                                                                    morphoComm.startFingerVerification(Constants.VERIFY_BY_LOCAL_DATABASE_MODE, empInfo);
//                                                                                }
//                                                                            }
//                                                                            break;
//                                                                        case "1"://24 bit Hex to Dec
//                                                                            rawData = rawData.substring(0);
//                                                                            Log.d("TEST", "YN124B Raw Data:" + rawData + " Len:" + rawData.length());
//                                                                            cardNoDec = Integer.parseInt(rawData, 2);
//                                                                            cardVal = Integer.toString(cardNoDec);
//
//                                                                            //======================= Verify using Card Id =====================//
//
//                                                                            autoId = dbComm.isDataAvailableInDatabaseByCardId(cardVal);
//                                                                            if (autoId != -1) {
//                                                                                EmployeeInfo empInfo = new EmployeeInfo();
//                                                                                boolean isFingerEnrolled = dbComm.getEmployeeInfo(autoId, empInfo);
//                                                                                if (isFingerEnrolled) {
//                                                                                    int noOfTemplates = empInfo.getNoOfTemplates();
//                                                                                    if (noOfTemplates == 1) {
//                                                                                        textViewPutFingerMessage.startAnimation(anim);
//                                                                                        textViewPutFingerMessage.setText("Put " + empInfo.getFirstFingerIndex() + " To Be Verified");
//                                                                                    } else if (noOfTemplates == 2) {
//                                                                                        textViewPutFingerMessage.startAnimation(anim);
//                                                                                        textViewPutFingerMessage.setText("Put " + empInfo.getFirstFingerIndex() + " Or " + empInfo.getSecondFingerIndex() + " To Be Verified");
//                                                                                    }
//
//                                                                                    stopModeUpdate = true;
//                                                                                    isVerificationStarted = true;
//                                                                                    morphoComm.stopFingerIdentification();//Abort Finger Identification
//
//                                                                                    try {
//                                                                                        Thread.sleep(500);
//                                                                                    } catch (InterruptedException ex) {
//                                                                                    }
//
//                                                                                    morphoComm.startFingerVerification(Constants.VERIFY_BY_LOCAL_DATABASE_MODE, empInfo);
//                                                                                }
//                                                                            }
//
//                                                                            break;
//                                                                        case "2"://32 bit Hex to Dec
//                                                                            break;
//                                                                    }
//                                                                    break;
//                                                            }
//                                                        } catch (NumberFormatException ne) {
//                                                        }
//                                                        break;
//                                                    case "N":
//                                                        new Thread(new Runnable() {
//                                                            @Override
//                                                            public void run() {
//                                                                ForlinxGPIO.runGPIOLEDForWiegandRead();
//                                                            }
//                                                        }).start();
//                                                        cardVal = arr[1].trim().toUpperCase();
//
//                                                        //======================= Verify using Card Id =====================//
//
//                                                        int autoId = -1;
//                                                        autoId = dbComm.isDataAvailableInDatabaseByCardId(cardVal);
//                                                        if (autoId != -1) {
//                                                            EmployeeInfo empInfo = new EmployeeInfo();
//                                                            boolean isFingerEnrolled = dbComm.getEmployeeInfo(autoId, empInfo);
//                                                            if (isFingerEnrolled) {
//                                                                int noOfTemplates = empInfo.getNoOfTemplates();
//                                                                if (noOfTemplates == 1) {
//                                                                    textViewPutFingerMessage.startAnimation(anim);
//                                                                    textViewPutFingerMessage.setText("Put " + empInfo.getFirstFingerIndex() + " To Be Verified");
//                                                                } else if (noOfTemplates == 2) {
//                                                                    textViewPutFingerMessage.startAnimation(anim);
//                                                                    textViewPutFingerMessage.setText("Put " + empInfo.getFirstFingerIndex() + " Or " + empInfo.getSecondFingerIndex() + " To Be Verified");
//                                                                }
//
//                                                                stopModeUpdate = true;
//                                                                isVerificationStarted = true;
//                                                                morphoComm.stopFingerIdentification();//Abort Finger Identification
//
//                                                                try {
//                                                                    Thread.sleep(500);
//                                                                } catch (InterruptedException ex) {
//                                                                }
//
//                                                                morphoComm.startFingerVerification(Constants.VERIFY_BY_LOCAL_DATABASE_MODE, empInfo);
//                                                            }
//                                                        }
//                                                        break;
//                                                }
//                                            }
//                                        } else if (val != null && val.equals("34")) {
//                                            SQLiteCommunicator dbComm = new SQLiteCommunicator();
//                                            WiegandSettingsInfo info = null;
//                                            info = dbComm.getWiegandSettings(info);
//                                            if (info != null) {
//                                                String cardVal = "";
//                                                String rawData = "";
//                                                int autoId = -1;
//                                                val = info.getIsHexToDecEnabled();
//                                                switch (val) {
//                                                    case "Y":
//                                                        rawData = arr[3].substring(0, 34);
//                                                        try {
//                                                            int check = Integer.parseInt(rawData, 2);
//                                                            new Thread(new Runnable() {
//                                                                @Override
//                                                                public void run() {
//                                                                    ForlinxGPIO.runGPIOLEDForWiegandRead();
//                                                                }
//                                                            }).start();
//                                                            rawData = rawData.substring(1, 33);
//                                                            val = info.getIsSiteCodeEnabled();
//                                                            switch (val) {
//                                                                case "Y":
//                                                                    String siteCode, cardNo;
//                                                                    int siteCodeDec, cardNoDec;
//                                                                    val = info.getCardNoType();
//                                                                    switch (val) {
//                                                                        case "0"://16 bit Hex to Dec
//                                                                            rawData = rawData.substring(16);
//                                                                            cardNoDec = Integer.parseInt(rawData, 2);
//                                                                            cardVal = Integer.toString(cardNoDec);
//
//                                                                            //======================= Verify using Card Id =====================//
//
//                                                                            autoId = dbComm.isDataAvailableInDatabaseByCardId(cardVal);
//                                                                            if (autoId != -1) {
//                                                                                EmployeeInfo empInfo = new EmployeeInfo();
//                                                                                boolean isFingerEnrolled = dbComm.getEmployeeInfo(autoId, empInfo);
//                                                                                if (isFingerEnrolled) {
//                                                                                    int noOfTemplates = empInfo.getNoOfTemplates();
//                                                                                    if (noOfTemplates == 1) {
//                                                                                        textViewPutFingerMessage.startAnimation(anim);
//                                                                                        textViewPutFingerMessage.setText("Put " + empInfo.getFirstFingerIndex() + " To Be Verified");
//                                                                                    } else if (noOfTemplates == 2) {
//                                                                                        textViewPutFingerMessage.startAnimation(anim);
//                                                                                        textViewPutFingerMessage.setText("Put " + empInfo.getFirstFingerIndex() + " Or " + empInfo.getSecondFingerIndex() + " To Be Verified");
//                                                                                    }
//
//                                                                                    stopModeUpdate = true;
//                                                                                    isVerificationStarted = true;
//                                                                                    morphoComm.stopFingerIdentification();//Abort Finger Identification
//
//                                                                                    try {
//                                                                                        Thread.sleep(500);
//                                                                                    } catch (InterruptedException ex) {
//                                                                                    }
//                                                                                    morphoComm.startFingerVerification(Constants.VERIFY_BY_LOCAL_DATABASE_MODE, empInfo);
//                                                                                } else {
//                                                                                    isWiegandReading = false;
//                                                                                }
//                                                                            } else {
//                                                                                isWiegandReading = false;
//                                                                            }
//
//
//                                                                            break;
//                                                                        case "1"://24 bit Hex to Dec
//                                                                            rawData = rawData.substring(8);
//                                                                            cardNoDec = Integer.parseInt(rawData, 2);
//                                                                            cardVal = Integer.toString(cardNoDec);
//
//                                                                            //======================= Verify using Card Id =====================//
//
//                                                                            autoId = dbComm.isDataAvailableInDatabaseByCardId(cardVal);
//                                                                            if (autoId != -1) {
//                                                                                EmployeeInfo empInfo = new EmployeeInfo();
//                                                                                boolean isFingerEnrolled = dbComm.getEmployeeInfo(autoId, empInfo);
//                                                                                if (isFingerEnrolled) {
//                                                                                    int noOfTemplates = empInfo.getNoOfTemplates();
//                                                                                    if (noOfTemplates == 1) {
//                                                                                        textViewPutFingerMessage.startAnimation(anim);
//                                                                                        textViewPutFingerMessage.setText("Put " + empInfo.getFirstFingerIndex() + " To Be Verified");
//                                                                                    } else if (noOfTemplates == 2) {
//                                                                                        textViewPutFingerMessage.startAnimation(anim);
//                                                                                        textViewPutFingerMessage.setText("Put " + empInfo.getFirstFingerIndex() + " Or " + empInfo.getSecondFingerIndex() + " To Be Verified");
//                                                                                    }
//
//                                                                                    stopModeUpdate = true;
//                                                                                    isVerificationStarted = true;
//                                                                                    morphoComm.stopFingerIdentification();//Abort Finger Identification
//
//                                                                                    try {
//                                                                                        Thread.sleep(500);
//                                                                                    } catch (InterruptedException ex) {
//                                                                                    }
//
//                                                                                    morphoComm.startFingerVerification(Constants.VERIFY_BY_LOCAL_DATABASE_MODE, empInfo);
//                                                                                } else {
//                                                                                    isWiegandReading = false;
//                                                                                }
//                                                                            } else {
//                                                                                isWiegandReading = false;
//                                                                            }
//
//                                                                            break;
//                                                                        case "2"://32 bit Hex to Dec
//                                                                            rawData = rawData.substring(0);
//                                                                            cardNoDec = Integer.parseInt(rawData, 2);
//                                                                            cardVal = Integer.toString(cardNoDec);
//
//                                                                            //======================= Verify using Card Id =====================//
//
//                                                                            autoId = dbComm.isDataAvailableInDatabaseByCardId(cardVal);
//                                                                            if (autoId != -1) {
//                                                                                EmployeeInfo empInfo = new EmployeeInfo();
//                                                                                boolean isFingerEnrolled = dbComm.getEmployeeInfo(autoId, empInfo);
//                                                                                if (isFingerEnrolled) {
//                                                                                    int noOfTemplates = empInfo.getNoOfTemplates();
//                                                                                    if (noOfTemplates == 1) {
//                                                                                        textViewPutFingerMessage.startAnimation(anim);
//                                                                                        textViewPutFingerMessage.setText("Put " + empInfo.getFirstFingerIndex() + " To Be Verified");
//                                                                                    } else if (noOfTemplates == 2) {
//                                                                                        textViewPutFingerMessage.startAnimation(anim);
//                                                                                        textViewPutFingerMessage.setText("Put " + empInfo.getFirstFingerIndex() + " Or " + empInfo.getSecondFingerIndex() + " To Be Verified");
//                                                                                    }
//
//                                                                                    stopModeUpdate = true;
//                                                                                    isVerificationStarted = true;
//                                                                                    morphoComm.stopFingerIdentification();//Abort Finger Identification
//
//                                                                                    try {
//                                                                                        Thread.sleep(500);
//                                                                                    } catch (InterruptedException ex) {
//                                                                                    }
//                                                                                    morphoComm.startFingerVerification(Constants.VERIFY_BY_LOCAL_DATABASE_MODE, empInfo);
//                                                                                } else {
//                                                                                    isWiegandReading = false;
//                                                                                }
//                                                                            } else {
//                                                                                isWiegandReading = false;
//                                                                            }
//
//                                                                            break;
//                                                                        case "3":// Site Code + 16 bit
//                                                                            siteCode = rawData.substring(0, 8);
//                                                                            cardNo = rawData.substring(16);
//                                                                            siteCodeDec = Integer.parseInt(siteCode, 2);
//                                                                            cardNoDec = Integer.parseInt(cardNo, 2);
//                                                                            cardVal = Integer.toString(siteCodeDec) + Integer.toString(cardNoDec);
//
//                                                                            //======================= Verify using Card Id =====================//
//
//                                                                            autoId = dbComm.isDataAvailableInDatabaseByCardId(cardVal);
//                                                                            if (autoId != -1) {
//                                                                                EmployeeInfo empInfo = new EmployeeInfo();
//                                                                                boolean isFingerEnrolled = dbComm.getEmployeeInfo(autoId, empInfo);
//                                                                                if (isFingerEnrolled) {
//                                                                                    int noOfTemplates = empInfo.getNoOfTemplates();
//                                                                                    if (noOfTemplates == 1) {
//                                                                                        textViewPutFingerMessage.startAnimation(anim);
//                                                                                        textViewPutFingerMessage.setText("Put " + empInfo.getFirstFingerIndex() + " To Be Verified");
//                                                                                    } else if (noOfTemplates == 2) {
//                                                                                        textViewPutFingerMessage.startAnimation(anim);
//                                                                                        textViewPutFingerMessage.setText("Put " + empInfo.getFirstFingerIndex() + " Or " + empInfo.getSecondFingerIndex() + " To Be Verified");
//                                                                                    }
//
//                                                                                    stopModeUpdate = true;
//                                                                                    isVerificationStarted = true;
//                                                                                    morphoComm.stopFingerIdentification();//Abort Finger Identification
//
//                                                                                    try {
//                                                                                        Thread.sleep(500);
//                                                                                    } catch (InterruptedException ex) {
//                                                                                    }
//
//                                                                                    morphoComm.startFingerVerification(Constants.VERIFY_BY_LOCAL_DATABASE_MODE, empInfo);
//                                                                                } else {
//                                                                                    isWiegandReading = false;
//                                                                                }
//                                                                            } else {
//                                                                                isWiegandReading = false;
//                                                                            }
//
//
//                                                                            break;
//                                                                        case "4"://Site Code + 24 bit
//                                                                            siteCode = rawData.substring(0, 8);
//                                                                            cardNo = rawData.substring(8);
//                                                                            siteCodeDec = Integer.parseInt(siteCode, 2);
//                                                                            cardNoDec = Integer.parseInt(cardNo, 2);
//                                                                            cardVal = Integer.toString(siteCodeDec) + Integer.toString(cardNoDec);
//
//                                                                            //======================= Verify using Card Id =====================//
//
//                                                                            autoId = dbComm.isDataAvailableInDatabaseByCardId(cardVal);
//                                                                            if (autoId != -1) {
//                                                                                EmployeeInfo empInfo = new EmployeeInfo();
//                                                                                boolean isFingerEnrolled = dbComm.getEmployeeInfo(autoId, empInfo);
//                                                                                if (isFingerEnrolled) {
//                                                                                    int noOfTemplates = empInfo.getNoOfTemplates();
//                                                                                    if (noOfTemplates == 1) {
//                                                                                        textViewPutFingerMessage.startAnimation(anim);
//                                                                                        textViewPutFingerMessage.setText("Put " + empInfo.getFirstFingerIndex() + " To Be Verified");
//                                                                                    } else if (noOfTemplates == 2) {
//                                                                                        textViewPutFingerMessage.startAnimation(anim);
//                                                                                        textViewPutFingerMessage.setText("Put " + empInfo.getFirstFingerIndex() + " Or " + empInfo.getSecondFingerIndex() + " To Be Verified");
//                                                                                    }
//
//                                                                                    stopModeUpdate = true;
//                                                                                    isVerificationStarted = true;
//                                                                                    morphoComm.stopFingerIdentification();//Abort Finger Identification
//
//                                                                                    try {
//                                                                                        Thread.sleep(500);
//                                                                                    } catch (InterruptedException ex) {
//                                                                                    }
//
//                                                                                    morphoComm.startFingerVerification(Constants.VERIFY_BY_LOCAL_DATABASE_MODE, empInfo);
//                                                                                } else {
//                                                                                    isWiegandReading = false;
//                                                                                }
//                                                                            } else {
//                                                                                isWiegandReading = false;
//                                                                            }
//
//
//                                                                            break;
//
//                                                                    }
//                                                                    break;
//                                                                case "N":
//                                                                    val = info.getCardNoType();
//                                                                    switch (val) {
//                                                                        case "0"://16 bit Hex to Dec
//                                                                            rawData = rawData.substring(16);
//                                                                            cardNoDec = Integer.parseInt(rawData, 2);
//                                                                            cardVal = Integer.toString(cardNoDec);
//
//                                                                            //======================= Verify using Card Id =====================//
//
//                                                                            autoId = dbComm.isDataAvailableInDatabaseByCardId(cardVal);
//                                                                            if (autoId != -1) {
//                                                                                EmployeeInfo empInfo = new EmployeeInfo();
//                                                                                boolean isFingerEnrolled = dbComm.getEmployeeInfo(autoId, empInfo);
//                                                                                if (isFingerEnrolled) {
//                                                                                    int noOfTemplates = empInfo.getNoOfTemplates();
//                                                                                    if (noOfTemplates == 1) {
//                                                                                        textViewPutFingerMessage.startAnimation(anim);
//                                                                                        textViewPutFingerMessage.setText("Put " + empInfo.getFirstFingerIndex() + " To Be Verified");
//                                                                                    } else if (noOfTemplates == 2) {
//                                                                                        textViewPutFingerMessage.startAnimation(anim);
//                                                                                        textViewPutFingerMessage.setText("Put " + empInfo.getFirstFingerIndex() + " Or " + empInfo.getSecondFingerIndex() + " To Be Verified");
//                                                                                    }
//
//                                                                                    stopModeUpdate = true;
//                                                                                    isVerificationStarted = true;
//                                                                                    morphoComm.stopFingerIdentification();//Abort Finger Identification
//
//                                                                                    try {
//                                                                                        Thread.sleep(500);
//                                                                                    } catch (InterruptedException ex) {
//                                                                                    }
//
//                                                                                    morphoComm.startFingerVerification(Constants.VERIFY_BY_LOCAL_DATABASE_MODE, empInfo);
//                                                                                } else {
//                                                                                    isWiegandReading = false;
//                                                                                }
//                                                                            } else {
//                                                                                isWiegandReading = false;
//                                                                            }
//
//
//                                                                            break;
//                                                                        case "1"://24 bit Hex to Dec
//                                                                            rawData = rawData.substring(8);
//                                                                            cardNoDec = Integer.parseInt(rawData, 2);
//                                                                            cardVal = Integer.toString(cardNoDec);
//
//                                                                            //======================= Verify using Card Id =====================//
//
//                                                                            autoId = dbComm.isDataAvailableInDatabaseByCardId(cardVal);
//                                                                            if (autoId != -1) {
//                                                                                EmployeeInfo empInfo = new EmployeeInfo();
//                                                                                boolean isFingerEnrolled = dbComm.getEmployeeInfo(autoId, empInfo);
//                                                                                if (isFingerEnrolled) {
//                                                                                    int noOfTemplates = empInfo.getNoOfTemplates();
//                                                                                    if (noOfTemplates == 1) {
//                                                                                        textViewPutFingerMessage.startAnimation(anim);
//                                                                                        textViewPutFingerMessage.setText("Put " + empInfo.getFirstFingerIndex() + " To Be Verified");
//                                                                                    } else if (noOfTemplates == 2) {
//                                                                                        textViewPutFingerMessage.startAnimation(anim);
//                                                                                        textViewPutFingerMessage.setText("Put " + empInfo.getFirstFingerIndex() + " Or " + empInfo.getSecondFingerIndex() + " To Be Verified");
//                                                                                    }
//
//                                                                                    stopModeUpdate = true;
//                                                                                    isVerificationStarted = true;
//                                                                                    morphoComm.stopFingerIdentification();//Abort Finger Identification
//
//                                                                                    try {
//                                                                                        Thread.sleep(500);
//                                                                                    } catch (InterruptedException ex) {
//                                                                                    }
//
//                                                                                    morphoComm.startFingerVerification(Constants.VERIFY_BY_LOCAL_DATABASE_MODE, empInfo);
//                                                                                } else {
//                                                                                    isWiegandReading = false;
//                                                                                }
//                                                                            } else {
//                                                                                isWiegandReading = false;
//                                                                            }
//
//
//                                                                            break;
//                                                                        case "2"://32 bit Hex to Dec
//                                                                            rawData = rawData.substring(0);
//                                                                            cardNoDec = Integer.parseInt(rawData, 2);
//                                                                            cardVal = Integer.toString(cardNoDec);
//
//                                                                            //======================= Verify using Card Id =====================//
//
//                                                                            autoId = dbComm.isDataAvailableInDatabaseByCardId(cardVal);
//                                                                            if (autoId != -1) {
//                                                                                EmployeeInfo empInfo = new EmployeeInfo();
//                                                                                boolean isFingerEnrolled = dbComm.getEmployeeInfo(autoId, empInfo);
//                                                                                if (isFingerEnrolled) {
//                                                                                    int noOfTemplates = empInfo.getNoOfTemplates();
//                                                                                    if (noOfTemplates == 1) {
//                                                                                        textViewPutFingerMessage.startAnimation(anim);
//                                                                                        textViewPutFingerMessage.setText("Put " + empInfo.getFirstFingerIndex() + " To Be Verified");
//                                                                                    } else if (noOfTemplates == 2) {
//                                                                                        textViewPutFingerMessage.startAnimation(anim);
//                                                                                        textViewPutFingerMessage.setText("Put " + empInfo.getFirstFingerIndex() + " Or " + empInfo.getSecondFingerIndex() + " To Be Verified");
//                                                                                    }
//
//                                                                                    stopModeUpdate = true;
//                                                                                    isVerificationStarted = true;
//                                                                                    morphoComm.stopFingerIdentification();//Abort Finger Identification
//
//                                                                                    try {
//                                                                                        Thread.sleep(500);
//                                                                                    } catch (InterruptedException ex) {
//                                                                                    }
//
//                                                                                    morphoComm.startFingerVerification(Constants.VERIFY_BY_LOCAL_DATABASE_MODE, empInfo);
//                                                                                } else {
//                                                                                    isWiegandReading = false;
//                                                                                }
//                                                                            } else {
//                                                                                isWiegandReading = false;
//                                                                            }
//
//
//                                                                            break;
//                                                                    }
//                                                                    break;
//                                                            }
//
//
//                                                        } catch (NumberFormatException ne) {
//                                                            isWiegandReading = false;
//                                                        }
//                                                        break;
//                                                    case "N":
//                                                        new Thread(new Runnable() {
//                                                            @Override
//                                                            public void run() {
//                                                                ForlinxGPIO.runGPIOLEDForWiegandRead();
//                                                            }
//                                                        }).start();
//                                                        cardVal = arr[1].trim().toUpperCase();
//
//                                                        //======================= Verify using Card Id =====================//
//
//                                                        autoId = dbComm.isDataAvailableInDatabaseByCardId(cardVal);
//                                                        if (autoId != -1) {
//                                                            EmployeeInfo empInfo = new EmployeeInfo();
//                                                            boolean isFingerEnrolled = dbComm.getEmployeeInfo(autoId, empInfo);
//                                                            if (isFingerEnrolled) {
//                                                                int noOfTemplates = empInfo.getNoOfTemplates();
//                                                                if (noOfTemplates == 1) {
//                                                                    textViewPutFingerMessage.startAnimation(anim);
//                                                                    textViewPutFingerMessage.setText("Put " + empInfo.getFirstFingerIndex() + " To Be Verified");
//                                                                } else if (noOfTemplates == 2) {
//                                                                    textViewPutFingerMessage.startAnimation(anim);
//                                                                    textViewPutFingerMessage.setText("Put " + empInfo.getFirstFingerIndex() + " Or " + empInfo.getSecondFingerIndex() + " To Be Verified");
//                                                                }
//
//                                                                stopModeUpdate = true;
//                                                                isVerificationStarted = true;
//                                                                morphoComm.stopFingerIdentification();//Abort Finger Identification
//
//                                                                try {
//                                                                    Thread.sleep(500);
//                                                                } catch (InterruptedException ex) {
//                                                                }
//
//                                                                morphoComm.startFingerVerification(Constants.VERIFY_BY_LOCAL_DATABASE_MODE, empInfo);
//                                                            } else {
//                                                                isWiegandReading = false;
//                                                            }
//                                                        } else {
//                                                            isWiegandReading = false;
//                                                        }
//                                                        break;
//                                                }
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                            isWiegandReading = false;
//                        }
//                    }
//                });
//            }
//        };


//        mqttTimerTask = new TimerTask() {
//            @Override
//            public void run() {
//                mqttHandler.post(new Runnable() {
//                    public void run() {
//                        try {
//                            boolean isWifiAvailable = isNetworkAvailable();
//                            if (isWifiAvailable) {
//                                int mode = 1;
//                                CheckInternetTask task = new CheckInternetTask(mode);
//                                task.execute();
//                            } else {
//                                updateInternetConnection(false);
//                            }
//                        } catch (Exception ex) {
//                        }
//                    }
//                });
//            }
//        };

    }

    private void startWiegandVerify(String cardVal) {
        int autoId = -1;
        switch (gvm) {
            case "CARD-BASED-VERIFY":
                if (MorphoCommunicator.isCDV) {
                    EditText etCardId = MorphoCommunicator.iCardId;
                    if (etCardId != null) {
                        etCardId.setText(cardVal);
                    }
                    isWiegandReading = false;
                } else {
                    autoId = dbComm.isDataAvailableInDatabaseByCardId(cardVal);
                    if (autoId != -1) {
                        SmartCardInfo cardInfo = new SmartCardInfo();
                        cardInfo = dbComm.getEmployeeInfoByAutoId(autoId, cardInfo);
                        int noOfFingerEnrolled = dbComm.getNoFingersEnrolled(autoId);
                        if (noOfFingerEnrolled > 0) {
                            cardInfo = dbComm.getFingerDetailsByAutoId(autoId, cardInfo);
                            boolean isIdentificationStarted = ProcessInfo.getInstance().isIdentificationStarted();
                            boolean isVerificationStarted = ProcessInfo.getInstance().isVerificationStarted();
                            boolean isBioCommandStarted = ProcessInfo.getInstance().isCommandBioStart();
                            if ((isIdentificationStarted || isVerificationStarted) && isBioCommandStarted) {
                                morphoComm.stopFingerIdentification();//Abort Finger Identification
                            }
                            stopHandler();
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException ex) {
                            }
                            initCFVByVM(cardInfo);//Start Finger Verification Using Card
                        } else {
                            showFailureDialog(2, "Wiegand Status", "Finger Not Enrolled");
                        }
                    } else {
                        showFailureDialog(2, "Wiegand Status", "Card Not Enrolled");
                    }
                }
                break;
            case "1:N":
                autoId = dbComm.isDataAvailableInDatabaseByCardId(cardVal);
                if (autoId != -1) {
                    SmartCardInfo cardInfo = new SmartCardInfo();
                    cardInfo = dbComm.getEmployeeInfoByAutoId(autoId, cardInfo);
                    int noOfFingerEnrolled = dbComm.getNoFingersEnrolled(autoId);
                    if (noOfFingerEnrolled > 0) {
                        cardInfo = dbComm.getFingerDetailsByAutoId(autoId, cardInfo);
                        boolean isIdentificationStarted = ProcessInfo.getInstance().isIdentificationStarted();
                        boolean isVerificationStarted = ProcessInfo.getInstance().isVerificationStarted();
                        boolean isBioCommandStarted = ProcessInfo.getInstance().isCommandBioStart();
                        if ((isIdentificationStarted || isVerificationStarted) && isBioCommandStarted) {
                            morphoComm.stopFingerIdentification();//Abort Finger Identification
                        }
                        stopHandler();
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException ex) {
                        }
                        if (!isFinishing()) {
                            morphoComm.startFingerVerification(Constants.VERIFY_BY_CARD_MODE, cardInfo);
                        }
                    } else {
                        showFailureDialog(2, "Wiegand Status", "Finger Not Enrolled");
                    }
                } else {
                    showFailureDialog(2, "Wiegand Status", "Card Not Enrolled");
                }
                break;
            case "CARD-ONLY":
                autoId = dbComm.isDataAvailableInDatabaseByCardId(cardVal);
                if (autoId != -1) {
                    final SmartCardInfo cardInfo = new SmartCardInfo();
                    dbComm.getEmployeeInfoByAutoId(autoId, cardInfo);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!isFinishing()) {
                                if (successEmpDetailsDialog != null && successEmpDetailsDialog.isShowing()) {
                                    successEmpDetailsDialog.cancel();
                                }
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ForlinxGPIO.runGPIOLEDForSuccess();
                                    }
                                }).start();
                                showSuccessCustomDialogForCardVerification("Verification Success", cardInfo);
                                pHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        successEmpDetailsDialog.cancel();
                                        WiegandCommunicator.clearWiegand(Constants.WIEGAND_WRITE_PATH, "1");
                                        isWiegandReading = false;
                                    }
                                }, 1000);
                            }
                        }
                    });
                } else {
                    showFailureDialog(2, "Wiegand Status", "Card Not Enrolled");
                }
                break;
            case "CARD+FINGER":
                autoId = dbComm.isDataAvailableInDatabaseByCardId(cardVal);
                if (autoId != -1) {
                    SmartCardInfo cardInfo = new SmartCardInfo();
                    cardInfo = dbComm.getEmployeeInfoByAutoId(autoId, cardInfo);
                    int noOfFingerEnrolled = dbComm.getNoFingersEnrolled(autoId);
                    if (noOfFingerEnrolled > 0) {
                        cardInfo = dbComm.getFingerDetailsByAutoId(autoId, cardInfo);
                        if (!isFinishing()) {
                            morphoComm.startFingerVerification(Constants.VERIFY_BY_CARD_MODE, cardInfo);
                        }
                    } else {
                        showFailureDialog(2, "Wiegand Status", "Finger Not Enrolled");
                    }
                } else {
                    showFailureDialog(2, "Wiegand Status", "Card Not Enrolled");
                }
                break;
        }
    }

    private boolean isMyServiceRunning(Class <?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
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


    private boolean readUsbCardTemplate(MicroSmartV2Communicator conn, SmartCardInfo cardDetails) {

        boolean readStatus = false;

        SQLiteCommunicator dbComm = new SQLiteCommunicator();
        String data = cardDetails.getMadOne();
        Cursor sectorKeyData = null;

        switch (data) {
            case Constants.MAD_ONE_DATA_FOR_TWO_TEMPLATES://Two Templates Found In Card//
                sectorKeyData = dbComm.getSectorAndKeyForReadCard();
                if (sectorKeyData != null) {
                    String command = "";
                    int checksum;
                    while (sectorKeyData.moveToNext()) {
                        String strSectorNo = sectorKeyData.getString(0);
                        String strKey = sectorKeyData.getString(1);
                        int sectorNo = Integer.parseInt(strSectorNo);
                        switch (sectorNo) {
                            case 2:   // Sector 2
                                command = Constants.SECTOR_READ_COMM[0] + Constants.KEY_B + strKey + Constants.ASCII_READ_WRITE;
                                command = Utility.addCheckSum(command);
                                byte[] sector_2_Data = conn.readSector(2, command.getBytes());
                                if (sector_2_Data != null) {
                                    readStatus = conn.parseSectorData(2, sector_2_Data, cardDetails);
                                }
                                break;
                            case 4:   // Sector 4
                                command = Constants.SECTOR_READ_COMM[1] + Constants.KEY_B + strKey + Constants.HEX_READ_WRITE;
                                command = Utility.addCheckSum(command);
                                byte[] sector_4_Data = conn.readSector(4, command.getBytes());
                                if (sector_4_Data != null) {
                                    readStatus = conn.parseSectorData(4, sector_4_Data, cardDetails);
                                }
                                break;
                            case 5:          // Sector 5
                                command = Constants.SECTOR_READ_COMM[2] + Constants.KEY_B + strKey + Constants.HEX_READ_WRITE;
                                command = Utility.addCheckSum(command);
                                byte[] sector_5_Data = conn.readSector(5, command.getBytes());
                                if (sector_5_Data != null) {
                                    readStatus = conn.parseSectorData(5, sector_5_Data, cardDetails);
                                }
                                break;
                            case 6:      // Sector 6
                                command = Constants.SECTOR_READ_COMM[3] + Constants.KEY_B + strKey + Constants.HEX_READ_WRITE;
                                command = Utility.addCheckSum(command);
                                byte[] sector_6_Data = conn.readSector(6, command.getBytes());
                                if (sector_6_Data != null) {
                                    readStatus = conn.parseSectorData(6, sector_6_Data, cardDetails);
                                }
                                break;
                            case 7:      // Sector 7
                                command = Constants.SECTOR_READ_COMM[4] + Constants.KEY_B + strKey + Constants.HEX_READ_WRITE;
                                command = Utility.addCheckSum(command);
                                byte[] sector_7_Data = conn.readSector(7, command.getBytes());
                                if (sector_7_Data != null) {
                                    readStatus = conn.parseSectorData(7, sector_7_Data, cardDetails);
                                }
                                break;
                            case 8:   // Sector 8
                                command = Constants.SECTOR_READ_COMM[5] + Constants.KEY_B + strKey + Constants.HEX_READ_WRITE;
                                command = Utility.addCheckSum(command);
                                byte[] sector_8_Data = conn.readSector(8, command.getBytes());
                                if (sector_8_Data != null) {
                                    readStatus = conn.parseSectorData(8, sector_8_Data, cardDetails);
                                }
                                break;
                            case 9:      // Sector 9
                                command = Constants.SECTOR_READ_COMM[6] + Constants.KEY_B + strKey + Constants.HEX_READ_WRITE;
                                command = Utility.addCheckSum(command);
                                byte[] sector_9_Data = conn.readSector(9, command.getBytes());
                                if (sector_9_Data != null) {
                                    readStatus = conn.parseSectorData(9, sector_9_Data, cardDetails);
                                }
                                break;
                            case 10:     // Sector A
                                command = Constants.SECTOR_READ_COMM[7] + Constants.KEY_B + strKey + Constants.HEX_READ_WRITE;
                                command = Utility.addCheckSum(command);
                                byte[] sector_10_Data = conn.readSector(10, command.getBytes());
                                if (sector_10_Data != null) {
                                    readStatus = conn.parseSectorData(10, sector_10_Data, cardDetails);
                                }
                                break;
                            case 11:     // Sector B
                                command = Constants.SECTOR_READ_COMM[8] + Constants.KEY_B + strKey + Constants.HEX_READ_WRITE;
                                command = Utility.addCheckSum(command);
                                byte[] sector_11_Data = conn.readSector(11, command.getBytes());
                                if (sector_11_Data != null) {
                                    readStatus = conn.parseSectorData(11, sector_11_Data, cardDetails);
                                }
                                break;
                            case 12:     // Sector C
                                command = Constants.SECTOR_READ_COMM[9] + Constants.KEY_B + strKey + Constants.HEX_READ_WRITE;
                                command = Utility.addCheckSum(command);
                                byte[] sector_12_Data = conn.readSector(12, command.getBytes());
                                if (sector_12_Data != null) {
                                    readStatus = conn.parseSectorData(12, sector_12_Data, cardDetails);
                                }
                                break;
                            case 13:     // Sector D
                                command = Constants.SECTOR_READ_COMM[10] + Constants.KEY_B + strKey + Constants.HEX_READ_WRITE;
                                command = Utility.addCheckSum(command);
                                byte[] sector_13_Data = conn.readSector(13, command.getBytes());
                                if (sector_13_Data != null) {
                                    readStatus = conn.parseSectorData(13, sector_13_Data, cardDetails);
                                }
                                break;
                            case 14:     // Sector E
                                command = Constants.SECTOR_READ_COMM[11] + Constants.KEY_B + strKey + Constants.HEX_READ_WRITE;
                                command = Utility.addCheckSum(command);
                                byte[] sector_14_Data = conn.readSector(14, command.getBytes());
                                if (sector_14_Data != null) {
                                    readStatus = conn.parseSectorData(14, sector_14_Data, cardDetails);
                                }
                                break;
                            case 15:     // Sector F
                                command = Constants.SECTOR_READ_COMM[12] + Constants.KEY_B + strKey + Constants.HEX_READ_WRITE;
                                command = Utility.addCheckSum(command);
                                byte[] sector_15_Data = conn.readSector(15, command.getBytes());
                                if (sector_15_Data != null) {
                                    readStatus = conn.parseSectorData(15, sector_15_Data, cardDetails);
                                }
                                break;
                            default:
                                break;
                        }

                        if (readStatus) {
                            continue;
                        } else {
                            break;
                        }
                    }
                    if (sectorKeyData != null) {
                        sectorKeyData.close();
                    }
                }

                break;

            case Constants.MAD_ONE_DATA_FOR_ONE_TEMPLATE:
                sectorKeyData = dbComm.getSectorAndKeyForReadCard();
                if (sectorKeyData != null) {
                    String command = "";
                    int checksum;
                    while (sectorKeyData.moveToNext()) {
                        String strSectorNo = sectorKeyData.getString(0);
                        String strKey = sectorKeyData.getString(1);
                        int sectorNo = Integer.parseInt(strSectorNo);
                        switch (sectorNo) {
                            case 2:   // Sector 2
                                command = Constants.SECTOR_READ_COMM[0] + Constants.KEY_B + strKey + Constants.ASCII_READ_WRITE;
                                command = Utility.addCheckSum(command);
                                byte[] sector_2_Data = conn.readSector(2, command.getBytes());
                                if (sector_2_Data != null) {
                                    readStatus = conn.parseSectorData(2, sector_2_Data, cardDetails);
                                }
                                break;
                            case 4:   // Sector 4
                                command = Constants.SECTOR_READ_COMM[1] + Constants.KEY_B + strKey + Constants.HEX_READ_WRITE;
                                command = Utility.addCheckSum(command);
                                byte[] sector_4_Data = conn.readSector(4, command.getBytes());
                                if (sector_4_Data != null) {
                                    readStatus = conn.parseSectorData(4, sector_4_Data, cardDetails);
                                }
                                break;
                            case 5:          // Sector 5
                                command = Constants.SECTOR_READ_COMM[2] + Constants.KEY_B + strKey + Constants.HEX_READ_WRITE;
                                command = Utility.addCheckSum(command);
                                byte[] sector_5_Data = conn.readSector(5, command.getBytes());
                                if (sector_5_Data != null) {
                                    readStatus = conn.parseSectorData(5, sector_5_Data, cardDetails);
                                }
                                break;
                            case 6:      // Sector 6
                                command = Constants.SECTOR_READ_COMM[3] + Constants.KEY_B + strKey + Constants.HEX_READ_WRITE;
                                command = Utility.addCheckSum(command);
                                byte[] sector_6_Data = conn.readSector(6, command.getBytes());
                                if (sector_6_Data != null) {
                                    readStatus = conn.parseSectorData(6, sector_6_Data, cardDetails);
                                }
                                break;
                            case 7:      // Sector 7
                                command = Constants.SECTOR_READ_COMM[4] + Constants.KEY_B + strKey + Constants.HEX_READ_WRITE;
                                command = Utility.addCheckSum(command);
                                byte[] sector_7_Data = conn.readSector(7, command.getBytes());
                                if (sector_7_Data != null) {
                                    readStatus = conn.parseSectorData(7, sector_7_Data, cardDetails);
                                }
                                break;
                            case 8:   // Sector 8
                                command = Constants.SECTOR_READ_COMM[5] + Constants.KEY_B + strKey + Constants.HEX_READ_WRITE;
                                command = Utility.addCheckSum(command);
                                byte[] sector_8_Data = conn.readSector(8, command.getBytes());
                                if (sector_8_Data != null) {
                                    readStatus = conn.parseSectorData(8, sector_8_Data, cardDetails);
                                }
                                break;
                            case 9:      // Sector 9
                                command = Constants.SECTOR_READ_COMM[6] + Constants.KEY_B + strKey + Constants.HEX_READ_WRITE;
                                command = Utility.addCheckSum(command);
                                byte[] sector_9_Data = conn.readSector(9, command.getBytes());
                                if (sector_9_Data != null) {
                                    readStatus = conn.parseSectorData(9, sector_9_Data, cardDetails);
                                }
                                break;
                            default:
                                break;
                        }

                        if (readStatus) {
                            continue;
                        } else {
                            break;
                        }
                    }
                    if (sectorKeyData != null) {
                        sectorKeyData.close();
                    }
                }
                break;
            default:
                readStatus = false;
                break;
        }
        return readStatus;
    }

    public void initLayoutElements() {

        pbBatPer = (ProgressBar) findViewById(R.id.pbBatPer);
        tvBatPer = (TextView) findViewById(R.id.tvBatPer);
        tvPower = (TextView) findViewById(R.id.tvPower);

        ivChargeIcon = (ImageView) findViewById(R.id.ivChargeIcon);
        ivBatTop = (ImageView) findViewById(R.id.ivBatTop);

        smart_reader = (ImageView) findViewById(R.id.smartreader);
        finger_reader = (ImageView) findViewById(R.id.fingerreader);

        textViewPutFingerMessage = (TextView) findViewById(R.id.putFingerMessage);

        anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(5);//You can manage the blinking time with this parameter
        anim.setStartOffset(20);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);

        empIdToVerfiy = (EditText) findViewById(R.id.empId);


        /*empIdToVerfiy.setCursorVisible(false);
        empIdToVerfiy.setFocusableInTouchMode(false);
        empIdToVerfiy.setFocusable(false);*/ //Modified By Sanjay Shyamal on 23/11/17


        btn_In = (Button) findViewById(R.id.btnIn);
        btn_Out = (Button) findViewById(R.id.btnOut);

        recordCount = (TextView) findViewById(R.id.recordCount);
        tvInternetConn = (TextView) findViewById(R.id.tv_internetconn);

        //========================  GIF Image Process ===========================//

        fingerImage = (ImageView) findViewById(R.id.imageView1);
        tButton = (ToggleButton) findViewById(R.id.toggleButton1);
        tvStateofToggleButton = (TextView) findViewById(R.id.tvstate);

        // ImageView imageView = (ImageView) findViewById(R.id.imageView);
//        GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(fingerImage);
//        Glide.with(this).load(R.drawable.fingerscanner).into(imageViewTarget);

        // bar = findViewById(R.id.bar);
        //  barAnimation = AnimationUtils.loadAnimation(EmployeeAttendanceActivity.this, R.anim.anim);
        // bar.startAnimation(barAnimation);

        //======================================================================//


        tvStateofToggleButton.setText("IN");
        tButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String strSelecetedMode = tvStateofToggleButton.getText().toString().trim();
                if (strSelecetedMode.equals("IN")) {
                    morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
                    morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
                    if (morphoDevice != null && morphoDatabase != null) {
                        String empId = empIdToVerfiy.getText().toString();
                        if (empId != null && empId.trim().length() > 0) {
                            int autoId = -1;
                            SQLiteCommunicator dbComm = new SQLiteCommunicator();
                            autoId = dbComm.isDataAvailableInDatabase(empId);
                            if (autoId != -1) {
                                EmployeeInfo empInfo = new EmployeeInfo();
                                boolean isFingerEnrolled = dbComm.getEmployeeInfo(autoId, empInfo);
                                if (isFingerEnrolled) {
                                    tvStateofToggleButton.setText("OUT");
                                    tButton.setChecked(false);
                                    tButton.setEnabled(false);
                                    int value = Settings.getInstance().getFrTypeValue();
                                    if (value == 0) {
                                        stopHandler();
                                        int noOfTemplates = empInfo.getNoOfTemplates();
                                        if (noOfTemplates == 1) {
                                            textViewPutFingerMessage.setText("Put " + empInfo.getFirstFingerIndex() + " To Be Verified");
                                        } else if (noOfTemplates == 2) {
                                            textViewPutFingerMessage.setText("Put " + empInfo.getFirstFingerIndex() + " Or " + empInfo.getSecondFingerIndex() + " To Be Verified");
                                        }
                                        stopModeUpdate = true;
                                        boolean isIdentificationStarted = ProcessInfo.getInstance().isIdentificationStarted();
                                        boolean isVerificationStarted = ProcessInfo.getInstance().isVerificationStarted();
                                        boolean isBioCommandStarted = ProcessInfo.getInstance().isCommandBioStart();
                                        if ((isIdentificationStarted || isVerificationStarted) && isBioCommandStarted) {
                                            morphoComm.stopFingerIdentification();
                                        }
                                        try {
                                            Thread.sleep(500);
                                        } catch (InterruptedException ex) {
                                        }
                                        if (!isFinishing()) {
                                            morphoComm.startFingerVerification(Constants.VERIFY_BY_LOCAL_DATABASE_MODE, empInfo);
                                        }
                                    }
                                } else {
                                    stopModeUpdate = false;
                                    empIdToVerfiy.setText("");
                                    showCustomAlertDialog(false, "Error", "User Finger Not Enrolled");
                                }
                            } else {
                                stopModeUpdate = false;
                                empIdToVerfiy.setText("");
                                showCustomAlertDialog(false, "Error", "User Not Enrolled");
                            }
                        } else {
                            stopModeUpdate = false;
                            showCustomAlertDialog(false, "Error", "Please Enter Employee Id");
                        }
                    } else {
                        stopModeUpdate = false;
                        showCustomAlertDialog(false, "Device Connection Status", "Connect Finger Reader");
                    }
                } else if (strSelecetedMode.equals("OUT")) {
                    morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
                    morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
                    if (morphoDevice != null && morphoDatabase != null) {
                        String empId = empIdToVerfiy.getText().toString();
                        if (empId != null && empId.trim().length() > 0) {
                            int autoId = -1;
                            SQLiteCommunicator dbComm = new SQLiteCommunicator();
                            autoId = dbComm.isDataAvailableInDatabase(empId);
                            if (autoId != -1) {
                                EmployeeInfo empInfo = new EmployeeInfo();
                                boolean isFingerEnrolled = dbComm.getEmployeeInfo(autoId, empInfo);
                                if (isFingerEnrolled) {
                                    tvStateofToggleButton.setText("IN");
                                    tButton.setChecked(true);
                                    tButton.setEnabled(false);
                                    int value = Settings.getInstance().getFrTypeValue();
                                    if (value == 0) {
                                        stopHandler();
                                        int noOfTemplates = empInfo.getNoOfTemplates();
                                        if (noOfTemplates == 1) {
                                            textViewPutFingerMessage.setText("Put " + empInfo.getFirstFingerIndex() + " To Be Verified");
                                        } else if (noOfTemplates == 2) {
                                            textViewPutFingerMessage.setText("Put " + empInfo.getFirstFingerIndex() + " Or " + empInfo.getSecondFingerIndex() + " To Be Verified");
                                        }
                                        stopModeUpdate = true;
                                        boolean isIdentificationStarted = ProcessInfo.getInstance().isIdentificationStarted();
                                        boolean isVerificationStarted = ProcessInfo.getInstance().isVerificationStarted();
                                        boolean isBioCommandStarted = ProcessInfo.getInstance().isCommandBioStart();
                                        if ((isIdentificationStarted || isVerificationStarted) && isBioCommandStarted) {
                                            morphoComm.stopFingerIdentification();
                                        }
                                        try {
                                            Thread.sleep(500);
                                        } catch (InterruptedException ex) {
                                        }
                                        if (!isFinishing()) {
                                            morphoComm.startFingerVerification(Constants.VERIFY_BY_LOCAL_DATABASE_MODE, empInfo);
                                        }
                                    }
                                } else {
                                    stopModeUpdate = false;
                                    empIdToVerfiy.setText("");
                                    showCustomAlertDialog(false, "Error", "User Finger Not Enrolled");
                                }
                            } else {
                                stopModeUpdate = false;
                                empIdToVerfiy.setText("");
                                showCustomAlertDialog(false, "Error", "User Not Enrolled");
                            }
                        } else {
                            stopModeUpdate = false;
                            showCustomAlertDialog(false, "Error", "Please Enter Employee Id");
                        }
                    } else {
                        stopModeUpdate = false;
                        showCustomAlertDialog(false, "Device Connection Status", "Connect Finger Reader");
                    }
                }
            }
        });


        //============For User Photo Click==========//

        // mPreview = new Preview(this);

//        sv = (SurfaceView) findViewById(R.id.surface_camera);
//
//        //Get a surface
//        sHolder = sv.getHolder();
//        //add the callback interface methods defined below as the Surface View callbacks
//        sHolder.addCallback(this);
//        //tells Android that this surface will have its data constantly replaced
//        sHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }


    public void modifyActionBar() {
        getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#e63900")));
        getActionBar().setTitle(Html.fromHtml("<b><font face='Calibri' color='#FFFFFF'>Employee Attendance</font></b>"));
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_emp_finger_identify, menu);
        MenuItem item = menu.findItem(R.id.home);
        menuItem = item;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.home) {
            if (!isPassDlgVisible) {
                if (passwordDialog != null && passwordDialog.isShowing()) {
                    passwordDialog.dismiss();
                }
                isDUI = true;
                showPwdDialog();
            }
            return true;
        }
//        if (id == R.id.reboot) {
//            morphoDevice=ProcessInfo.getInstance().getMorphoDevice();
//            morphoDatabase=ProcessInfo.getInstance().getMorphoDatabase();
//            if(morphoDevice!=null && morphoDatabase!=null){
//                boolean isIdentificationStarted = ProcessInfo.getInstance().isIdentificationStarted();
//                boolean isBioCommmandStarted = ProcessInfo.getInstance().isCommandBioStart();
//                if (isIdentificationStarted && isBioCommmandStarted) {
//                    stopHandler();
//                    morphoComm.stopFingerIdentification();
//                    morphoComm.reboot();
//                }
//            }
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }

    Dialog passwordDialog = null;

    private void showPwdDialog() {

        final Context context = EmployeeAttendanceActivity.this;
        passwordDialog = new Dialog(context);
        passwordDialog.setCanceledOnTouchOutside(false);
        passwordDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        passwordDialog.setContentView(R.layout.username_password_dialog);

        TextView title = (TextView) passwordDialog.findViewById(R.id.title);

        final EditText et_Username = (EditText) passwordDialog.findViewById(R.id.etUsername);
        final EditText et_Password = (EditText) passwordDialog.findViewById(R.id.etPassword);

        Button btn_Ok = (Button) passwordDialog.findViewById(R.id.btn_Ok);
        ImageButton btn_Cancel = (ImageButton) passwordDialog.findViewById(R.id.image);

        title.setText("Password Entry:");
        btn_Cancel.setImageResource(R.drawable.failure);

        btn_Ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String strUsername = et_Username.getText().toString().trim();
                String strPassword = et_Password.getText().toString().trim();
                SQLiteCommunicator dbComm = new SQLiteCommunicator();
                boolean isValid = false;
                isValid = true;
                strUsername = "admin";
                strPassword = "admin";
                //isValid = validate(et_Username, et_Password);
                if (isValid) {
                    Cursor userData = null;
                    userData = dbComm.getUserData(strUsername, strPassword);
                    if (userData != null) {
                        if (userData.getCount() == 1) {
                            while (userData.moveToNext()) {

                                UserDetails userDetails = UserDetails.getInstance();
                                userDetails.setLoginId(userData.getInt(0));
                                userDetails.setName(userData.getString(1));
                                userDetails.setPhoto(userData.getBlob(2));
                                userDetails.setRole(userData.getString(3));

                                stopHandler();
                                stopADCReceiver();

                                boolean isIdentificationStarted = ProcessInfo.getInstance().isIdentificationStarted();
                                boolean isVerificationStarted = ProcessInfo.getInstance().isVerificationStarted();
                                boolean isBioCommandStarted = ProcessInfo.getInstance().isCommandBioStart();

                                Log.d("TEST", "******************* Show Password Dialog ****************");
                                Log.d("TEST", "Is Identification Started:" + isIdentificationStarted);
                                Log.d("TEST", "Is Verification Started:" + isVerificationStarted);
                                Log.d("TEST", "Is Bio Command Started:" + isBioCommandStarted);
                                Log.d("TEST", "********************************************************");

                                switch (gvm) {
                                    case "CARD-BASED-VERIFY":
                                        if ((isIdentificationStarted || isVerificationStarted) && isBioCommandStarted) {
                                            morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
                                            morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
                                            if (morphoDevice != null && morphoDatabase != null) {
                                                morphoComm.stopFingerIdentification();
                                            }
                                        }
                                        break;
                                    case "1:N":
                                        if ((isIdentificationStarted || isVerificationStarted) && isBioCommandStarted) {
                                            morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
                                            morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
                                            if (morphoDevice != null && morphoDatabase != null) {
                                                morphoComm.stopFingerIdentification();
                                            }
                                        }
                                        break;
                                    case "CARD+FINGER":
                                        if ((isIdentificationStarted || isVerificationStarted) && isBioCommandStarted) {
                                            morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
                                            morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
                                            if (morphoDevice != null && morphoDatabase != null) {
                                                morphoComm.stopFingerIdentification();
                                            }
                                        }
                                        break;
                                }

                                isDUI = false;//Is Disable User Interaction
                                passwordDialog.dismiss();
                                isPassDlgVisible = false;

                                Intent intent = new Intent(EmployeeAttendanceActivity.this, HomeActivity.class);
                                startActivity(intent);
                                overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
                                finish();
                            }
                        }
                        userData.close();
                    } else {
                        showCustomAlertDialog(false, "Invalid User", "Invalid username or password !");
                    }
                }
            }
        });

        btn_Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isDUI = false;
                passwordDialog.dismiss();
                isPassDlgVisible = false;
            }
        });

        isPassDlgVisible = true;
        passwordDialog.show();

        WindowManager.LayoutParams lp = passwordDialog.getWindow().getAttributes();
        lp.dimAmount = 0.9f;
        lp.buttonBrightness = 1.0f;
        lp.screenBrightness = 1.0f;

        passwordDialog.getWindow().setAttributes(lp);
        passwordDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }

    private boolean validate(EditText username, EditText password) {
        String strUsername = username.getText().toString().trim();
        String strPassword = password.getText().toString().trim();
        if (strUsername != null && strUsername.trim().length() == 0) {
            username.requestFocus();
            username.setError("Username Cannot Be Left Blank");
            return false;
        }
        if (strPassword != null && strPassword.trim().length() == 0) {
            password.requestFocus();
            password.setError("Password Cannot Be Left Blank");
            return false;
        }
        return true;
    }


//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        Log.d("TEST", "KeyCode:" + keyCode);
//        if ((keyCode == KeyEvent.KEYCODE_HOME)) {
//            System.out.println("KEYCODE_HOME");
//            showDialog("'HOME'");
//            return true;
//        }
//        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
//            System.out.println("KEYCODE_BACK");
//            showDialog("'BACK'");
//            return true;
//        }
//        if ((keyCode == KeyEvent.KEYCODE_MENU)) {
//            System.out.println("KEYCODE_MENU");
//            showDialog("'MENU'");
//            return true;
//        }
//
//
//        return false;
//    }

//    void showDialog(String the_key) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setMessage("You have pressed the " + the_key + " button. Would you like to exit the app?")
//                .setCancelable(true)
//                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        dialog.cancel();
//                        finish();
//                    }
//                })
//                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        dialog.cancel();
//                    }
//                });
//        AlertDialog alert = builder.create();
//        alert.setTitle("CoderzHeaven.");
//        alert.show();
//    }

//    @Override
//    public void onAttachedToWindow() {
//        this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
//        super.onAttachedToWindow();
//    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            boolean isLogin = MorphoCommunicator.isLoggedIn;
            if (!isLogin) {
                // showPasswordDialog();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void showSuccessCustomDialogForCardVerification(String strTitle, SmartCardInfo cardDetails) {

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("ddMMyyyy HH:mm:ss");
        String strDateTime = mdformat.format(calendar.getTime());

        final Context context = this;
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

        textViewEmpID.setText(cardDetails.getEmployeeId());
        textViewCardId.setText(cardDetails.getCardId());
        textViewName.setText(cardDetails.getEmpName());
        textViewDOB.setText(cardDetails.getBirthDate());
        textViewValidUpto.setText(cardDetails.getValidUpto());
        textViewSmartCardVer.setText(cardDetails.getSmartCardVer());
        attendanceTime.setText(strDateTime);
        empImage.setImageResource(R.drawable.dummyphoto);

        SQLiteCommunicator dbComm = new SQLiteCommunicator();
        Cursor empData = dbComm.getEmployeePhoto(cardDetails.getEmployeeId());
        if (empData != null) {
            if (empData.getCount() > 0) {
                while (empData.moveToNext()) {
                    int insertStaus = -1;
                    byte[] byteImage = empData.getBlob(0);
                    if (byteImage != null) {
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
                    String strInOutModeText = tvStateofToggleButton.getText().toString();
                    // String strInOutMode = Utility.getInOutValue(strInOutModeText);
                    insertStaus = dbComm.insertAttendanceData(employeeId, cardId, strDateTime, strInOutModeText, vm, latitude, longitude, null);
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
            }
            if (empData != null) {
                empData.close();
            }

        } else {
            int insertStaus = -1;

            // Location mLastLocation = EmployeeAttendanceActivity.mLastLocation;
//            if (mLastLocation != null) {
//                strLatitude = Double.toString(mLastLocation.getLatitude());
//                strLongitude = Double.toString(mLastLocation.getLongitude());
//            }

            String strLatitude = EmployeeAttendanceActivity.latitude;
            String strLongitude = EmployeeAttendanceActivity.longitude;

            String employeeId = cardDetails.getEmployeeId();
            String cardId = cardDetails.getCardId();
            String vm = cardDetails.getFirstFingerVerificationMode();
            String strInOutModeText = tvStateofToggleButton.getText().toString();
            //String strInOutMode = Utility.getInOutValue(strInOutModeText);
            insertStaus = dbComm.insertAttendanceData(employeeId, cardId, strDateTime, strInOutModeText, vm, latitude, longitude, null);
            if (insertStaus != -1) {
                if (strInOutModeText.trim().equals("IN") || strInOutModeText.trim().equals("OUT")) {
                    // Toast.makeText(context, "Employee Attendance " + strInOutModeText + " Time Captured Successfully", Toast.LENGTH_LONG).show();
                } else {
                    // Toast.makeText(context, "Employee Attendance Captured Successfully", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(context, "Attendance Capture Failure", Toast.LENGTH_LONG).show();
            }
        }

        textViewPutFingerMessage.clearAnimation();

        successEmpDetailsDialog.show();

        WindowManager.LayoutParams lp = successEmpDetailsDialog.getWindow().getAttributes();
        lp.dimAmount = 0.9f;
        lp.buttonBrightness = 1.0f;
        lp.screenBrightness = 1.0f;
        successEmpDetailsDialog.getWindow().setAttributes(lp);
        successEmpDetailsDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
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


    class CheckInternetTask extends AsyncTask <Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            boolean status = false;
            status = isInternetWorking();
            return status;
        }

        @Override
        protected void onPostExecute(Boolean status) {
            if (status) {
                updateInternetConnection("Internet Connected", true);
                SQLiteCommunicator dbComm = new SQLiteCommunicator();
                Cursor rsProtocol = dbComm.getProtocolDetails();
                if (rsProtocol != null) {
                    while (rsProtocol.moveToNext()) {
                        String protocol = rsProtocol.getString(0).trim();
                        switch (protocol) {
                            case "HTTP":
                                if (!isHttpDataTransferStarted) {
                                    initHttpDataTransfer();
                                }
                                break;
                            case "MQTT":
                                boolean isServiceRunning = isMyServiceRunning(MqttMessageService.class);
                                if (!isServiceRunning) {
                                    initMqttDataTransfer();
                                }
                                break;
                        }
                    }
                    rsProtocol.close();
                }
            } else {
                updateInternetConnection("No Internet Connection", false);
            }
        }
    }

    private void initMqttDataTransfer() {
        SQLiteCommunicator dbComm = new SQLiteCommunicator();
        Cursor rsMqtt = dbComm.getMqttBrokerDetails();
        if (rsMqtt != null) {
            if (rsMqtt.getCount() > 0) {
                while (rsMqtt.moveToNext()) {

                    String imei = "911573951100631";
                    String subTopic = "ATTENDANCE/SAMSUNG/VIVO/V6/STD";
                    String pubTopic = "ATTENDANCE/SAMSUNG/VIVO/V6/DTS";

                    String brokerIP = rsMqtt.getString(0).trim();
                    String brokerPort = rsMqtt.getString(1).trim();
                    String brokerUsername = rsMqtt.getString(2).trim();
                    String brokerPassword = rsMqtt.getString(3).trim();

                    Intent intent = new Intent(EmployeeAttendanceActivity.this, MqttMessageService.class);
                    intent.putExtra("IMEI", imei);
                    intent.putExtra("BrokerIP", brokerIP);
                    intent.putExtra("BrokerPort", brokerPort);
                    intent.putExtra("Username", brokerUsername);
                    intent.putExtra("Password", brokerPassword);
                    intent.putExtra("SubTopic", subTopic);
                    intent.putExtra("PubTopic", pubTopic);
                    startService(intent);
                }
            }
            rsMqtt.close();
        }
    }


    private void initHttpDataTransfer() {
        SQLiteCommunicator dbComm = new SQLiteCommunicator();
        Cursor rs = dbComm.getHttpServerDetails();
        if (rs != null) {
            if (rs.getCount() == 1) {
                while (rs.moveToNext()) {
                    final String domain = rs.getString(1).trim();
                    final String ip = rs.getString(2).trim();
                    final String port = rs.getString(3).trim();
                    final String[] serverIP = {""};
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (domain != null && domain.trim().length() > 0) {//First priority connect using valid domain name
                                InetAddress address = null;
                                try {
                                    address = InetAddress.getByName(domain);
                                    serverIP[0] = address.getHostAddress();
                                    if (serverIP != null && serverIP.length > 0 && serverIP[0].trim().length() > 0) {//If domain name is valid
                                        final String imei = "911573951100631";
                                        if (imei != null && imei.trim().length() > 0) {
                                            isHttpDataTransferStarted = true;
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    EzeeHrLiteCommunicator ezeeHrCommunicator = new EzeeHrLiteCommunicator(EmployeeAttendanceActivity.this, serverIP[0], port, imei);
                                                    ezeeHrCommunicator.MakeThreadCall();
                                                }
                                            });
                                        } else {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    tvInternetConn.setText("Device IMEI Not Found");
                                                }
                                            });
                                        }
                                    }
                                } catch (UnknownHostException e) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            tvInternetConn.setText("Invalid Domain Found");
                                        }
                                    });
                                }
                            } else if (ip != null && ip.trim().length() > 0 && port != null && port.trim().length() > 0) {//Second priority connect using ip address
                                final String imei = "911573951100631";
                                if (imei != null && imei.trim().length() > 0) {
                                    isHttpDataTransferStarted = true;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            EzeeHrLiteCommunicator ezeeHrCommunicator = new EzeeHrLiteCommunicator(EmployeeAttendanceActivity.this, ip, port, imei);
                                            ezeeHrCommunicator.MakeThreadCall();
                                        }
                                    });
                                } else {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            tvInternetConn.setText("Device IMEI Not Found");
                                        }
                                    });
                                }
                            }
                        }
                    }).start();
//                    if (serverIP != null && serverIP.length > 0 && serverIP[0].trim().length() > 0) {//If domain name is valid
//                        String imei = "911573951100631";
//                        if (imei != null && imei.trim().length() > 0) {
//                            isHttpDataTransferStarted = true;
//                            EzeeHrLiteCommunicator ezeeHrCommunicator = new EzeeHrLiteCommunicator(EmployeeAttendanceActivity.this, serverIP[0], port, imei);
//                            ezeeHrCommunicator.MakeThreadCall();
//                        } else {
//                            tvInternetConn.setText("Device IMEI Not Found");
//                        }
//                    } else {
//                        String imei = "911573951100631";
//                        if (imei != null && imei.trim().length() > 0) {
//                            isHttpDataTransferStarted = true;
//                            EzeeHrLiteCommunicator ezeeHrCommunicator = new EzeeHrLiteCommunicator(EmployeeAttendanceActivity.this, ip, port, imei);
//                            ezeeHrCommunicator.MakeThreadCall();
//                        } else {
//                            tvInternetConn.setText("Device IMEI Not Found");
//                        }
//                    }
                }
            }
            rs.close();
        }
    }

    private void startHttpDataTransfer() {

        Settings settings = Settings.getInstance();
        int serverType = settings.getServerTypeValue();
        final String[] serverIP = {settings.getAttendanaceSIP()};
        final String serverPort = settings.getAttendancePort();
        final String serverDomain = settings.getAttendanceDomain();
        final String attendanceUrl = settings.getAttendanceUrl();

        switch (serverType) {

            case 0:

                //=============================== Transfer To Purple Moore =================================//

                if (serverIP[0] != null && serverIP[0].trim().length() > 0 && serverPort != null && serverPort.trim().length() > 0) {
                    if (attendanceUrl != null && attendanceUrl.trim().length() > 0) {
                        if (!isHttpDataTransferStarted) {
                            // isHttpDataTransferStarted = true;
                            // PurpleMoorePlusLayer purpleMoore = new PurpleMoorePlusLayer(serverIP[0], serverPort, attendanceUrl);
                            // purpleMoore.MakeThreadCall();
                        }
                    } else {
                        tvInternetConn.setText("Attendance Url Not Found");
                    }
                } else if (serverDomain != null && serverDomain.trim().length() > 0 && serverPort != null && serverPort.trim().length() > 0) {
                    if (attendanceUrl != null && attendanceUrl.trim().length() > 0) {
                        Thread t = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                InetAddress address = null;
                                try {
                                    address = InetAddress.getByName(serverDomain);
                                    serverIP[0] = address.getHostAddress();
                                    if (!isHttpDataTransferStarted) {
                                        //  isHttpDataTransferStarted = true;
                                        // PurpleMoorePlusLayer purpleMoore = new PurpleMoorePlusLayer(serverIP[0], serverPort, attendanceUrl);
                                        // purpleMoore.MakeThreadCall();
                                    }
                                } catch (UnknownHostException e) {
                                    tvInternetConn.setText("Invalid Domain Name");
                                }
                            }
                        });
                        t.start();
                    } else {
                        tvInternetConn.setText("Attendance Url Not Found");
                    }
                } else {
                    tvInternetConn.setText("Server IP/Port Not Found");
                }

                break;

            case 1:

                //============================ Transfer To EzeeHRLite ======================================//

                if (serverIP[0] != null && serverIP[0].trim().length() > 0 && serverPort != null && serverPort.trim().length() > 0) {
                    if (attendanceUrl != null && attendanceUrl.trim().length() > 0) {
                        if (!isHttpDataTransferStarted) {
//                            TelephonyManager tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
//                            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
//                                return;
//                            }
//                            String imei = tel.getDeviceId();
//                            Log.d("TEST", "IMEI:" + imei);
                            String imei = "911573951100631";
                            //String imei = "123456789ABCD";
                            if (imei != null && imei.trim().length() > 0) {
                                isHttpDataTransferStarted = true;
                                EzeeHrLiteCommunicator ezeeHrCommunicator = new EzeeHrLiteCommunicator(EmployeeAttendanceActivity.this, serverIP[0], serverPort, imei);
                                ezeeHrCommunicator.MakeThreadCall();
                            } else {
                                tvInternetConn.setText("Device IMEI Not Found");
                            }
                        }
                    } else {
                        tvInternetConn.setText("Attendance Url Not Found");
                    }
                } else if (serverDomain != null && serverDomain.trim().length() > 0 && serverPort != null && serverPort.trim().length() > 0) {
                    if (attendanceUrl != null && attendanceUrl.trim().length() > 0) {
                        Thread t = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                InetAddress address = null;
                                try {
                                    address = InetAddress.getByName(serverDomain);
                                    serverIP[0] = address.getHostAddress();
                                    if (!isHttpDataTransferStarted) {
//                                        TelephonyManager tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
//                                        if (ActivityCompat.checkSelfPermission(EmployeeAttendanceActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
//                                            return;
//                                        }
//                                        String imei = tel.getDeviceId();
//                                        Log.d("TEST", "IMEI:" + imei);
                                        String imei = "911573951100631";
                                        //String imei = "123456789ABCD";
                                        if (imei != null && imei.trim().length() > 0) {
                                            isHttpDataTransferStarted = true;
                                            EzeeHrLiteCommunicator ezeeHrCommunicator = new EzeeHrLiteCommunicator(EmployeeAttendanceActivity.this, serverIP[0], serverPort, imei);
                                            ezeeHrCommunicator.MakeThreadCall();
                                        } else {
                                            tvInternetConn.setText("Device IMEI Not Found");
                                        }
                                    }
                                } catch (UnknownHostException e) {
                                    tvInternetConn.setText("Invalid Domain Name");
                                }
                            }
                        });
                        t.start();
                    } else {
                        tvInternetConn.setText("Attendance Url Not Found");
                    }
                } else {
                    tvInternetConn.setText("Server IP/Port Not Found");
                }

                break;

            case 2:
                //================================ Transfer To OSEM =======================================//
                break;

            case 3:
                //=============================== Transfer to DXS/TCP-IP =================================//
                break;

            default:
                break;
        }
    }

    //========================= Google Play Services Methods ================================//

    public void showFailureCustomDialog(String strTitle, String strMessage) {

        if (isAttendanceWindowVisisble) {

            final Context context = this;
            failureEmpDetailsDialog = new Dialog(context);
            failureEmpDetailsDialog.setCanceledOnTouchOutside(false);
            failureEmpDetailsDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            failureEmpDetailsDialog.setContentView(R.layout.finger_failure_custom_dialog);
            failureEmpDetailsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            TextView title = (TextView) failureEmpDetailsDialog.findViewById(R.id.title);
            TextView message = (TextView) failureEmpDetailsDialog.findViewById(R.id.message);
            title.setText(strTitle);
            message.setText(strMessage);

            textViewPutFingerMessage.clearAnimation();

            failureEmpDetailsDialog.show();

            WindowManager.LayoutParams lp = failureEmpDetailsDialog.getWindow().getAttributes();
            lp.dimAmount = 0.9f;
            lp.buttonBrightness = 1.0f;
            lp.screenBrightness = 1.0f;
            failureEmpDetailsDialog.getWindow().setAttributes(lp);
            failureEmpDetailsDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }
    }

    public void showCustomAlertDialog(boolean status, String strTitle, final String strMessage) {

        if (isAttendanceWindowVisisble) {

            final Context context = this;
            alertDialog = new Dialog(context);
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            alertDialog.setContentView(R.layout.custom_alert_dialog);

            ImageView icon = (ImageView) alertDialog.findViewById(R.id.image);
            TextView title = (TextView) alertDialog.findViewById(R.id.title);
            TextView message = (TextView) alertDialog.findViewById(R.id.message);
            Button btn_Ok = (Button) alertDialog.findViewById(R.id.btnOk);

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
        }
    }

    public void updateInternetConnection(final String message, final boolean status) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (status) {
                    tvInternetConn.setBackgroundColor(Color.parseColor("#72bf00"));
                    tvInternetConn.setTextColor(Color.parseColor("#ffffff"));
                    tvInternetConn.setPadding(5, 2, 5, 2);
                    tvInternetConn.setText(message);
                } else {
                    tvInternetConn.setBackgroundColor(Color.parseColor("#ff70d6"));
                    tvInternetConn.setTextColor(Color.parseColor("#ffffff"));
                    tvInternetConn.setPadding(5, 2, 5, 2);
                    tvInternetConn.setText(message);
                }
            }
        });
    }

    public static boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            return true;
        } else {
            return false;
        }
    }

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
                        FM220SDK = new acpl_FM220_SDK(getApplicationContext(), EmployeeAttendanceActivity.this, true);
                        devType = true;
                    } else if ((pid == 0x8220) && (vid == 0x0bca)) {
                        FM220SDK = new acpl_FM220_SDK(getApplicationContext(), EmployeeAttendanceActivity.this, false);
                        devType = false;
                    } else {
                        FM220SDK = new acpl_FM220_SDK(getApplicationContext(), EmployeeAttendanceActivity.this, oldDevType);
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
                    FM220SDK = new acpl_FM220_SDK(getApplicationContext(), EmployeeAttendanceActivity.this, oldDevType);
                    StarkTekConnection.getInstance().setFM220SDK(FM220SDK);
                }
            }
        }).start();
    }


    @Override
    public void ScannerProgressFM220(final boolean DisplayImage, final Bitmap ScanImage, final boolean DisplayText, final String statusMessage) {
        FM220SDK = StarkTekConnection.getInstance().getFM220SDK();
        if (FM220SDK != null && FM220SDK.FM220Initialized()) {
            EmployeeAttendanceActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ImageView imageView1 = (ImageView) findViewById(R.id.imageView1);
                    TextView tvSensorMsg = (TextView) findViewById(R.id.textViewMessage);
                    if (DisplayText) {
                        tvSensorMsg.setText(statusMessage);
                        tvSensorMsg.invalidate();
                    }
                    if (DisplayImage) {
                        imageView1.setImageBitmap(ScanImage);
                        imageView1.invalidate();
                    }

                }
            });
        }
    }

    @Override
    public void ScanCompleteFM220(final fm220_Capture_Result result) {

        final ImageView imageView1 = (ImageView) findViewById(R.id.imageView1);
        final TextView tvSensorMsg = (TextView) findViewById(R.id.textViewMessage);

        FM220SDK = StarkTekConnection.getInstance().getFM220SDK();
        if (FM220SDK != null && FM220SDK.FM220Initialized()) {
            EmployeeAttendanceActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (result.getResult()) {
                        imageView1.setImageBitmap(result.getScanImage());
                        byte[] t1 = result.getISO_Template();
                        final StringBuilder builder = new StringBuilder();
                        for (byte b : t1) {
                            builder.append(String.format("%02x", b));
                        }
                        String strFirstFingerDataHex = builder.toString().toUpperCase();

                        Bitmap bitmap = result.getScanImage();
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                        byte[] image1 = baos.toByteArray();

                        tvSensorMsg.setText("Success NFIQ:" + Integer.toString(result.getNFIQ()) + "  SrNo:" + result.getSerialNo());


                    } else {
                        String errorCode = result.getError();
                        switch (errorCode) {
                            case "500 :- Capture fail or Timeout.":
                                //FM220SDK.CaptureFM220(2, true, true);
                                //   FM220SDK.CaptureFM220(2,true,true);
                                break;
                        }
                        imageView1.setImageBitmap(null);
                        tvSensorMsg.setText(result.getError());
                    }
                    imageView1.invalidate();
                    tvSensorMsg.invalidate();
                }
            });

        }


    }

    @Override
    public void ScanMatchFM220(fm220_Capture_Result fm220_capture_result) {
        Log.d("TEST", "Scan match");

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
                        Toast.makeText(EmployeeAttendanceActivity.this, "User Denied Permission for Finger Reader", Toast.LENGTH_LONG).show();
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
                                        Toast.makeText(EmployeeAttendanceActivity.this, "Startek Finger Reader Attached", Toast.LENGTH_LONG).show();
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
                        Toast.makeText(EmployeeAttendanceActivity.this, "Startek Finger Reader Detached", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    };


    //======================== Take Snapshot ==================================//

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

        // Log.d("TEST", "Employee Attendance On Surface Created");
        // The Surface has been created, acquire the camera and tell it where
        // to draw the preview.

        //  Log.d("TEST", "On surface created");

        boolean status = true;
        hasCamera = checkCameraHardware(EmployeeAttendanceActivity.this);
        hasCamera = false;//For Forlinx
        if (hasCamera) {
            try {
                mCamera = Camera.open();
                if (mCamera != null) {
                    try {
                        mCamera.setDisplayOrientation(90);
                        mCamera.setPreviewDisplay(surfaceHolder);
                        mCamera.startPreview();
                        safeToTakePicture = true;
                        // previewing = true;
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        mCamera.release();
                        mCamera = null;
                    }
                }
            } catch (RuntimeException e) {
                Log.d("TEST", "camera exception:" + e.getMessage());
            }
        }


//        mCamera = Camera.open();
//        try {
//            mCamera.setPreviewDisplay(surfaceHolder);
//        } catch (IOException exception) {
//            mCamera.release();
//            mCamera = null;
//        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        // Log.d("TEST", "on surface changed");
        if (hasCamera) {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.set("jpeg-quality", 100);
            parameters.set("orientation", "portrait");
            parameters.setRotation(90);
            // parameters.setPictureSize(320, 240);
            parameters.setPictureSize(250, 200);
            mCamera.setParameters(parameters);
            mCamera.startPreview();
            safeToTakePicture = true;
        }


        //List <Size> sizes = parameters.getSupportedPreviewSizes();
        //Camera.Size optimalSize = getOptimalPreviewSize(sizes, 300, 150);
        //parameters.setPreviewSize(100, 100);


        //get camera parameters
//        parameters = mCamera.getParameters();
//        parameters.setRotation(90);
//        parameters.setPictureSize(300, 150);
//        parameters.setJpegQuality(100);


//        parameters.setPictureSize(320, 240);

//        List<Camera.Size> sizes = parameters.getSupportedPictureSizes();
//        for(int count=0;count<sizes.size();count++){
//            Log.d("TEST","Width:"+sizes.get(count).width);
//            Log.d("TEST","Height"+sizes.get(count).height);
//        }
//        Camera.Size x = sizes.get(0);
//        Log.d("TEST", "Default Width:" + x.width + " Height:" + x.height);

//        mCamera.setParameters(parameters);


        //set camera parameters
//        mCamera.setParameters(parameters);
//        mCamera.startPreview();

        //sets what code should be executed after the picture is taken
//        mCall = new Camera.PictureCallback() {
//            @Override
//            public void onPictureTaken(byte[] data, Camera camera) {
//
//                mCamera.startPreview();
//                pictureData = new byte[data.length];
//                pictureData = data;
//
//                //Log.d("TEST","On Picture Taken");
//                // Log.d("TEST","Pic Data:"+data);
//                // Log.d("TEST","Pic Data Len:"+data.length);
//
//                //decode the data obtained by the camera into a Bitmap
//                // Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
//                //set the iv_image
//                // iv_image.setImageBitmap(bmp);
//            }
//        };

        // mCamera.takePicture(null, null, mCall);
    }


    public static Camera.PictureCallback mCall = new Camera.PictureCallback() {

        public void onPictureTaken(byte[] data, Camera arg1) {
            if (hasCamera) {
                mCamera.startPreview();
                safeToTakePicture = true;
                pictureData = null;
                pictureData = new byte[data.length];
                System.arraycopy(data, 0, pictureData, 0, data.length);
                Bitmap bitmapPicture = BitmapFactory.decodeByteArray(data, 0, data.length);
                Bitmap correctBmp = Bitmap.createBitmap(bitmapPicture, 0, 0, bitmapPicture.getWidth(), bitmapPicture.getHeight(), null, true);
            }
        }
    };


    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if (hasCamera) {
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("TEST", "On Start called");
        isAttendanceWindowVisisble = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("TEST", "On Stop called");
        isAttendanceWindowVisisble = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        stopHandler();
        startHandler();
        startTimer();
        int value = Settings.getInstance().getFrTypeValue();
        if (value == 0) {
            morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
            morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
            if (morphoDevice != null && morphoDatabase != null) {
                updateFrConStatusToUI(true);
                boolean isIdentificationStarted = ProcessInfo.getInstance().isIdentificationStarted();
                boolean isVerificationStarted = ProcessInfo.getInstance().isVerificationStarted();
                boolean isBioCommandStarted = ProcessInfo.getInstance().isCommandBioStart();
                switch (gvm) {
                    case "1:N":
                        Log.d("TEST", "************* On Resume **************");
                        Log.d("TEST", " Is Identification Started:" + isIdentificationStarted);
                        Log.d("TEST", "Is Verification Started:" + isVerificationStarted);
                        Log.d("TEST", "Is Bio Command Started:" + isBioCommandStarted);
                        Log.d("TEST", "**************************************");
                        if (!isIdentificationStarted && !isVerificationStarted && !isBioCommandStarted) {
                            initIdentification();
                        }
                        break;
                    case "CARD-BASED-VERIFY":
                        Log.d("TEST", "************* On Resume **************");
                        Log.d("TEST", " Is Identification Started:" + isIdentificationStarted);
                        Log.d("TEST", "Is Verification Started:" + isVerificationStarted);
                        Log.d("TEST", "Is Bio Command Started:" + isBioCommandStarted);
                        Log.d("TEST", "**************************************");
                        if (!isIdentificationStarted && !isVerificationStarted && !isBioCommandStarted) {
                            initIdentification();
                        }
                        break;
                    case "CARD-ONLY":
                        textViewPutFingerMessage.setText("Show Card");
                        break;
                    case "CARD+FINGER":
                        textViewPutFingerMessage.setText("Show Card");
                        break;
                }
            } else {
                updateFrConStatusToUI(false);
                textViewPutFingerMessage.setText("Finger Reader Not Connected");
            }
        }
    }

    public static boolean isDUI = false;//IsDisableUserInteraction=false

    @Override
    public void onUserInteraction() {
        Log.d("TEST", "On User Interaction Called");
        if (!isDUI) {
            stopHandler();
            startHandler();
            ForlinxGPIO.setLCDBackLightOn();
            setScreenBrightness(Constants.BRIGHTNESS_ON);
            isLCDBackLightOff = false;
        }
    }

    @Override
    protected void onUserLeaveHint() {
        stopHandler();
        boolean isIdentificationStarted = ProcessInfo.getInstance().isIdentificationStarted();
        boolean isVerificationStarted = ProcessInfo.getInstance().isVerificationStarted();
        boolean isBioCommandStarted = ProcessInfo.getInstance().isCommandBioStart();
        switch (gvm) {
            case "CARD-BASED-VERIFY":
                if ((isIdentificationStarted || isVerificationStarted) && isBioCommandStarted) {
                    morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
                    morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
                    if (morphoDevice != null && morphoDatabase != null) {
                        morphoComm.stopFingerIdentification();
                    }
                }
                break;
            case "1:N":
                if ((isIdentificationStarted || isVerificationStarted) && isBioCommandStarted) {
                    morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
                    morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
                    if (morphoDevice != null && morphoDatabase != null) {
                        morphoComm.stopFingerIdentification();
                    }
                }
                break;
            case "CARD+FINGER":
                if ((isIdentificationStarted || isVerificationStarted) && isBioCommandStarted) {
                    morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
                    morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
                    if (morphoDevice != null && morphoDatabase != null) {
                        morphoComm.stopFingerIdentification();
                    }
                }
                break;
        }
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
                Toast.makeText(EmployeeAttendanceActivity.this, "error in unregister morpho smart receiver:" + e.getMessage(), Toast.LENGTH_LONG).show();
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
                Toast.makeText(EmployeeAttendanceActivity.this, "error in unregister morpho receiver:" + e.getMessage(), Toast.LENGTH_LONG).show();
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
                Toast.makeText(EmployeeAttendanceActivity.this, "error in unregister morpho smart receiver:" + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
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

    @Override
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
        int value = Settings.getInstance().getFrTypeValue();
        if (value == 0) {
            morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
            morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
            if (morphoDevice != null && morphoDatabase != null) {
                updateFrConStatusToUI(true);
                boolean isIdentificationStarted = ProcessInfo.getInstance().isIdentificationStarted();
                boolean isVerificationStarted = ProcessInfo.getInstance().isVerificationStarted();
                boolean isBioCommandStarted = ProcessInfo.getInstance().isCommandBioStart();
                switch (gvm) {
                    case "CARD-BASED-VERIFY":
                        Log.d("TEST", "************* Init Identification **************");
                        Log.d("TEST", " Is Identification Started:" + isIdentificationStarted);
                        Log.d("TEST", "Is Verification Started:" + isVerificationStarted);
                        Log.d("TEST", "Is Bio Command Started:" + isBioCommandStarted);
                        Log.d("TEST", "************************************************");
                        if (!isIdentificationStarted && !isVerificationStarted && !isBioCommandStarted) {
                            morphoComm.startFingerIdentification();
                        }
                        break;
                    case "1:N":
                        Log.d("TEST", "************* Init Identification **************");
                        Log.d("TEST", " Is Identification Started:" + isIdentificationStarted);
                        Log.d("TEST", "Is Verification Started:" + isVerificationStarted);
                        Log.d("TEST", "Is Bio Command Started:" + isBioCommandStarted);
                        Log.d("TEST", "************************************************");
                        if (!isIdentificationStarted && !isVerificationStarted && !isBioCommandStarted) {
                            morphoComm.startFingerIdentification();
                        }
                        break;
                    case "CARD-ONLY":
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textViewPutFingerMessage.setText("Show Card");
                            }
                        });
                        break;
                    case "CARD+FINGER":
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textViewPutFingerMessage.setText("Show Card");
                            }
                        });
                        break;
                }
            } else {
                updateFrConStatusToUI(false);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textViewPutFingerMessage.setText("Finger Reader Not Connected");
                    }
                });
            }
        }
    }

    @Override
    public void initCardRead() {
        smartReaderConnection = SmartReaderConnection.getInstance().getmConnection();
        intf = SmartReaderConnection.getInstance().getIntf();
        input = SmartReaderConnection.getInstance().getInput();
        output = SmartReaderConnection.getInstance().getOutput();
        if (smartReaderConnection != null && intf != null && input != null && output != null) {
            updateSrConStatusToUI(true);
        } else {
            updateSrConStatusToUI(false);
        }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceivers();
        stopTimer();
        stopADCReceiver();
        stopHandler();//For Remote Enrollment

        boolean isIdentificationStarted = ProcessInfo.getInstance().isIdentificationStarted();
        boolean isVerificationStarted = ProcessInfo.getInstance().isVerificationStarted();
        boolean isBioCommandStarted = ProcessInfo.getInstance().isCommandBioStart();

        Log.d("TEST", "******************* On Destroy ****************");
        Log.d("TEST", "Is Identification Started:" + isIdentificationStarted);
        Log.d("TEST", "Is Verification Started:" + isVerificationStarted);
        Log.d("TEST", "Is Bio Command Started:" + isBioCommandStarted);
        Log.d("TEST", "********************************************************");

        switch (gvm) {
            case "CARD-BASED-VERIFY":
                if ((isIdentificationStarted || isVerificationStarted) && isBioCommandStarted) {
                    morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
                    morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
                    if (morphoDevice != null && morphoDatabase != null) {
                        morphoComm.stopFingerIdentification();
                    }
                }
                break;
            case "1:N":
                if ((isIdentificationStarted || isVerificationStarted) && isBioCommandStarted) {
                    morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
                    morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
                    if (morphoDevice != null && morphoDatabase != null) {
                        morphoComm.stopFingerIdentification();
                    }
                }
                break;
            case "CARD+FINGER":
                if ((isIdentificationStarted || isVerificationStarted) && isBioCommandStarted) {
                    morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
                    morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
                    if (morphoDevice != null && morphoDatabase != null) {
                        morphoComm.stopFingerIdentification();
                    }
                }
                break;
        }

        isCardReadingBlocked = false;
        stopModeUpdate = false;

        WiegandCommunicator.clearWiegand(Constants.WIEGAND_WRITE_PATH, "1");
        isWiegandReading = false;

        isDUI = false;//Enable on user Interaction
        LoginSplashActivity.isLoaded = false;

        stopStartekReceiver();
    }

    private void stopStartekReceiver() {
        if (isStartekRcvRegisterd) {
            try {
                if (mStartekReceiver != null) {
                    unregisterReceiver(mStartekReceiver);
                    isStartekRcvRegisterd = false;
                    mStartekReceiver = null;
                }
            } catch (Exception e) {
                Toast.makeText(EmployeeAttendanceActivity.this, "error in unregister startek receiver:" + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void stopADCReceiver() {
        if (!isADCReceiverUnregistered) {
            isADCReceiverUnregistered = true;
            if (intent != null) {
                EmployeeAttendanceActivity.this.stopService(intent);
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

        if (httpDataTransferTimer != null) {
            httpDataTransferTimer.cancel();
            httpDataTransferTimer.purge();
            httpDataTransferTimer = null;
        }

        if (recordUpdateTimer != null) {
            recordUpdateTimer.cancel();
            recordUpdateTimer.purge();
            recordUpdateTimer = null;
        }

        if (wigReadTimer != null) {
            wigReadTimer.cancel();
            wigReadTimer.purge();
            wigReadTimer = null;
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

        if (cardReadTimer != null) {
            cardReadTimer.cancel();
            cardReadTimer.purge();
            cardReadTimer = null;
        }

        if (attendanceModeTimer != null) {
            attendanceModeTimer.cancel();
            attendanceModeTimer.purge();
            attendanceModeTimer = null;
        }
    }


    //==========================================================================================//

    //    private boolean readCardTemplateUsingRC522(RC522Communicator comm, SmartCardInfo cardInfo) {
//        boolean parseStatus = false;
//        cardInfo = new SmartCardInfo();
//        Cursor sectorKeyData = null;
//        SQLiteCommunicator dbComm=new SQLiteCommunicator();
//        sectorKeyData = dbComm.getSectorAndKeyForReadCard();
//        if (sectorKeyData != null) {
//            String wrCommand = "";
//            while (sectorKeyData.moveToNext()) {
//                String strSectorNo = sectorKeyData.getString(0);
//                String strKey = sectorKeyData.getString(1);
//                int sectorNo = Integer.parseInt(strSectorNo);
//                StringBuffer sb = new StringBuffer();
//                switch (sectorNo) {
//                    case 0:
//                        parseStatus = true;
//                        break;
//                    case 1:
//                        parseStatus = true;
//                        break;
//                    case 2:
//                        sb.append("00");
//                        for (int j = 0; j < 3; j++) {
//                            wrCommand = Constants.RC522_BLOCK_READ_COMMAND + " " + Integer.toString(sectorNo * 4 + j) + " " + strKey + " " + Constants.RC522_KEY_TYPE_B;
//                            boolean status = comm.writeRC522(wrCommand);
//                            if (status) {
//                                char[] data = comm.readRC522();
//                                if (data != null && data.length > 0) {
//                                    String strData = new String(data);
//                                    String[] arr = strData.split(":");
//                                    if (arr != null && arr.length == 3) {
//                                        sb.append(arr[2].trim());
//                                    }
//                                }
//                            }
//                        }
//                        String perInfo = Utility.hexToAscii(sb.toString());
//                        perInfo = Utility.removeNonAscii(perInfo);
//                        parseStatus = comm.parseSectorData(2, perInfo.getBytes(), cardInfo);
//                        break;
//                    case 3:
//                        parseStatus=true;
//                        break;
//                    case 4:
//                        sb.append("0");
//                        for (int j = 0; j < 3; j++) {
//                            wrCommand = Constants.RC522_BLOCK_READ_COMMAND + " " + Integer.toString(sectorNo * 4 + j) + " " + strKey + " " + Constants.RC522_KEY_TYPE_B;
//                            boolean status = comm.writeRC522(wrCommand);
//                            if (status) {
//                                char[] data = comm.readRC522();
//                                if (data != null && data.length > 0) {
//                                    String strData = new String(data);
//                                    String[] arr = strData.split(":");
//                                    if (arr != null && arr.length == 3) {
//                                        sb.append(arr[2].trim());
//                                    }
//                                }
//                            }
//                        }
//                        parseStatus = comm.parseSectorData(4, sb.toString().getBytes(), cardInfo);
//                        break;
//                    case 5:
//                        sb.append("0");
//                        for (int j = 0; j < 3; j++) {
//                            wrCommand = Constants.RC522_BLOCK_READ_COMMAND + " " + Integer.toString(sectorNo * 4 + j) + " " + strKey + " " + Constants.RC522_KEY_TYPE_B;
//                            boolean status = comm.writeRC522(wrCommand);
//                            if (status) {
//                                char[] data = comm.readRC522();
//                                if (data != null && data.length > 0) {
//                                    String strData = new String(data);
//                                    String[] arr = strData.split(":");
//                                    if (arr != null && arr.length == 3) {
//                                        sb.append(arr[2].trim());
//                                    }
//                                }
//                            }
//                        }
//                        parseStatus = comm.parseSectorData(5, sb.toString().getBytes(), cardInfo);
//                        break;
//                    case 6:
//                        sb.append("0");
//                        for (int j = 0; j < 3; j++) {
//                            wrCommand = Constants.RC522_BLOCK_READ_COMMAND + " " + Integer.toString(sectorNo * 4 + j) + " " + strKey + " " + Constants.RC522_KEY_TYPE_B;
//                            boolean status = comm.writeRC522(wrCommand);
//                            if (status) {
//                                char[] data = comm.readRC522();
//                                if (data != null && data.length > 0) {
//                                    String strData = new String(data);
//                                    String[] arr = strData.split(":");
//                                    if (arr != null && arr.length == 3) {
//                                        sb.append(arr[2].trim());
//                                    }
//                                }
//                            }
//                        }
//                        parseStatus = comm.parseSectorData(6, sb.toString().getBytes(), cardInfo);
//                        break;
//                    case 7:
//                        sb.append("0");
//                        for (int j = 0; j < 3; j++) {
//                            wrCommand = Constants.RC522_BLOCK_READ_COMMAND + " " + Integer.toString(sectorNo * 4 + j) + " " + strKey + " " + Constants.RC522_KEY_TYPE_B;
//                            boolean status = comm.writeRC522(wrCommand);
//                            if (status) {
//                                char[] data = comm.readRC522();
//                                if (data != null && data.length > 0) {
//                                    String strData = new String(data);
//                                    String[] arr = strData.split(":");
//                                    if (arr != null && arr.length == 3) {
//                                        sb.append(arr[2].trim());
//                                    }
//                                }
//                            }
//                        }
//                        parseStatus = comm.parseSectorData(7, sb.toString().getBytes(), cardInfo);
//                        break;
//                    case 8:
//                        sb.append("0");
//                        for (int j = 0; j < 3; j++) {
//                            wrCommand = Constants.RC522_BLOCK_READ_COMMAND + " " + Integer.toString(sectorNo * 4 + j) + " " + strKey + " " + Constants.RC522_KEY_TYPE_B;
//                            boolean status = comm.writeRC522(wrCommand);
//                            if (status) {
//                                char[] data = comm.readRC522();
//                                if (data != null && data.length > 0) {
//                                    String strData = new String(data);
//                                    String[] arr = strData.split(":");
//                                    if (arr != null && arr.length == 3) {
//                                        sb.append(arr[2].trim());
//                                    }
//                                }
//                            }
//                        }
//                        parseStatus = comm.parseSectorData(8, sb.toString().getBytes(), cardInfo);
//                        break;
//                    case 9:
//                        sb.append("0");
//                        for (int j = 0; j < 3; j++) {
//                            wrCommand = Constants.RC522_BLOCK_READ_COMMAND + " " + Integer.toString(sectorNo * 4 + j) + " " + strKey + " " + Constants.RC522_KEY_TYPE_B;
//                            boolean status = comm.writeRC522(wrCommand);
//                            if (status) {
//                                char[] data = comm.readRC522();
//                                if (data != null && data.length > 0) {
//                                    String strData = new String(data);
//                                    String[] arr = strData.split(":");
//                                    if (arr != null && arr.length == 3) {
//                                        sb.append(arr[2].trim());
//                                    }
//                                }
//                            }
//                        }
//                        parseStatus = comm.parseSectorData(9, sb.toString().getBytes(), cardInfo);
//                        break;
//                    case 10:
//                        sb.append("0");
//                        for (int j = 0; j < 3; j++) {
//                            wrCommand = Constants.RC522_BLOCK_READ_COMMAND + " " + Integer.toString(sectorNo * 4 + j) + " " + strKey + " " + Constants.RC522_KEY_TYPE_B;
//                            boolean status = comm.writeRC522(wrCommand);
//                            if (status) {
//                                char[] data = comm.readRC522();
//                                if (data != null && data.length > 0) {
//                                    String strData = new String(data);
//                                    String[] arr = strData.split(":");
//                                    if (arr != null && arr.length == 3) {
//                                        sb.append(arr[2].trim());
//                                    }
//                                }
//                            }
//                        }
//                        parseStatus = comm.parseSectorData(10, sb.toString().getBytes(), cardInfo);
//                        break;
//                    case 11:
//                        sb.append("0");
//                        for (int j = 0; j < 3; j++) {
//                            wrCommand = Constants.RC522_BLOCK_READ_COMMAND + " " + Integer.toString(sectorNo * 4 + j) + " " + strKey + " " + Constants.RC522_KEY_TYPE_B;
//                            boolean status = comm.writeRC522(wrCommand);
//                            if (status) {
//                                char[] data = comm.readRC522();
//                                if (data != null && data.length > 0) {
//                                    String strData = new String(data);
//                                    String[] arr = strData.split(":");
//                                    if (arr != null && arr.length == 3) {
//                                        sb.append(arr[2].trim());
//                                    }
//                                }
//                            }
//                        }
//                        parseStatus = comm.parseSectorData(11, sb.toString().getBytes(), cardInfo);
//                        break;
//                    case 12:
//                        sb.append("0");
//                        for (int j = 0; j < 3; j++) {
//                            wrCommand = Constants.RC522_BLOCK_READ_COMMAND + " " + Integer.toString(sectorNo * 4 + j) + " " + strKey + " " + Constants.RC522_KEY_TYPE_B;
//                            boolean status = comm.writeRC522(wrCommand);
//                            if (status) {
//                                char[] data = comm.readRC522();
//                                if (data != null && data.length > 0) {
//                                    String strData = new String(data);
//                                    String[] arr = strData.split(":");
//                                    if (arr != null && arr.length == 3) {
//                                        sb.append(arr[2].trim());
//                                    }
//                                }
//                            }
//                        }
//                        parseStatus = comm.parseSectorData(12, sb.toString().getBytes(), cardInfo);
//                        break;
//                    case 13:
//                        sb.append("0");
//                        for (int j = 0; j < 3; j++) {
//                            wrCommand = Constants.RC522_BLOCK_READ_COMMAND + " " + Integer.toString(sectorNo * 4 + j) + " " + strKey + " " + Constants.RC522_KEY_TYPE_B;
//                            boolean status = comm.writeRC522(wrCommand);
//                            if (status) {
//                                char[] data = comm.readRC522();
//                                if (data != null && data.length > 0) {
//                                    String strData = new String(data);
//                                    String[] arr = strData.split(":");
//                                    if (arr != null && arr.length == 3) {
//                                        sb.append(arr[2].trim());
//                                    }
//                                }
//                            }
//                        }
//                        parseStatus = comm.parseSectorData(13, sb.toString().getBytes(), cardInfo);
//                        break;
//                    case 14:
//                        sb.append("0");
//                        for (int j = 0; j < 3; j++) {
//                            wrCommand = Constants.RC522_BLOCK_READ_COMMAND + " " + Integer.toString(sectorNo * 4 + j) + " " + strKey + " " + Constants.RC522_KEY_TYPE_B;
//                            boolean status = comm.writeRC522(wrCommand);
//                            if (status) {
//                                char[] data = comm.readRC522();
//                                if (data != null && data.length > 0) {
//                                    String strData = new String(data);
//                                    String[] arr = strData.split(":");
//                                    if (arr != null && arr.length == 3) {
//                                        sb.append(arr[2].trim());
//                                    }
//                                }
//                            }
//                        }
//                        parseStatus = comm.parseSectorData(14, sb.toString().getBytes(), cardInfo);
//                        break;
//                    case 15:
//                        sb.append("0");
//                        for (int j = 0; j < 3; j++) {
//                            wrCommand = Constants.RC522_BLOCK_READ_COMMAND + " " + Integer.toString(sectorNo * 4 + j) + " " + strKey + " " + Constants.RC522_KEY_TYPE_B;
//                            boolean status = comm.writeRC522(wrCommand);
//                            if (status) {
//                                char[] data = comm.readRC522();
//                                if (data != null && data.length > 0) {
//                                    String strData = new String(data);
//                                    String[] arr = strData.split(":");
//                                    if (arr != null && arr.length == 3) {
//                                        sb.append(arr[2].trim());
//                                    }
//                                }
//                            }
//                        }
//                        parseStatus = comm.parseSectorData(15, sb.toString().getBytes(), cardInfo);
//                        break;
//                }
//                if (!parseStatus) {
//                    break;
//                }
//            }
//            sectorKeyData.close();
//        }
//        return parseStatus;
//    }
}