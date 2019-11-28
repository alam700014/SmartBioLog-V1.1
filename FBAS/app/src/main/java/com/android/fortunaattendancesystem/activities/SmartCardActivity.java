package com.android.fortunaattendancesystem.activities;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.text.Html;
import android.text.InputFilter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.fortunaattendancesystem.R;
import com.android.fortunaattendancesystem.constant.Constants;
import com.android.fortunaattendancesystem.forlinx.ForlinxGPIO;
import com.android.fortunaattendancesystem.helper.Utility;
import com.android.fortunaattendancesystem.info.ProcessInfo;
import com.android.fortunaattendancesystem.model.EmployeeEnrollInfo;
import com.android.fortunaattendancesystem.model.EmployeeFingerInfo;
import com.android.fortunaattendancesystem.model.SmartCardInfo;
import com.android.fortunaattendancesystem.singleton.RC632ReaderConnection;
import com.android.fortunaattendancesystem.singleton.Settings;
import com.android.fortunaattendancesystem.singleton.SmartReaderConnection;
import com.android.fortunaattendancesystem.singleton.UserDetails;
import com.android.fortunaattendancesystem.submodules.ForlinxGPIOCommunicator;
import com.android.fortunaattendancesystem.submodules.I2CCommunicator;
import com.android.fortunaattendancesystem.submodules.MicroSmartV2Communicator;
import com.android.fortunaattendancesystem.submodules.RC522Communicator;
import com.android.fortunaattendancesystem.submodules.SQLiteCommunicator;
import com.android.fortunaattendancesystem.usbconnection.USBConnectionCreator;
import com.forlinx.android.GetValueService;
import com.forlinx.android.HardwareInterface;
import com.friendlyarm.SmartReader.SmartFinger;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import static android.os.SystemClock.sleep;


public class SmartCardActivity extends USBConnectionCreator {

    private SQLiteCommunicator dbComm = new SQLiteCommunicator();
    private Handler mHandler = new Handler();

    private EditText etEmpId;
    private Button btn_ReadDB, btn_CardRead, btn_CardWrite, btn_CardInit, btn_CardRefresh, btn_CardIdChange, btn_NewCardIssue;
    private ImageView smart_reader, finger_reader;

    private UsbEndpoint output = null;
    private UsbEndpoint input = null;
    private UsbInterface intf = null;
    private UsbDeviceConnection smartReaderConnection;
    private SmartFinger rc632ReaderConnection = null;

    private boolean isSmartRcvRegisterd = false;
    private boolean isMorphoRcvRegistered = false;
    private boolean isMorphoSmartRcvRegisterd = false;

    private TextView db_Enrodll, db_CardSerial, db_EmpID, db_CardID, db_Name, db_Aadhaar, db_BloodGroup, db_dob, db_validity, db_sitecode, db_card_version;
    private TextView db_Fnumber_one, db_Findex_one, db_Fquality_one, db_Fnumber_two, db_Findex_two, db_Fquality_two, db_SecurityLevel, db_VerificationMode;
    private TextView db_Fdata, db_Sdata;
    private LinearLayout tbl_CardDetails;


    private static boolean isLCDBackLightOff = false;
    private Handler cHandler = new Handler();
    private Timer capReadTimer = null;
    private TimerTask capReadTimerTask = null;
    private boolean isBreakFound = true;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_smart_card_read_write_white);

        findViewById(R.id.smartcd).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideSoftKeyboard(SmartCardActivity.this);
                return false;
            }
        });

        findViewById(R.id.scrollViewcd).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideSoftKeyboard(SmartCardActivity.this);
                return false;
            }
        });

        findViewById(R.id.scrollViewcd2).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideSoftKeyboard(SmartCardActivity.this);
                return false;
            }
        });/*Added by Sanjay Shyamal on 22/11/2017*/


        initLayoutElements();

        if (!Constants.isTab) {
            if (HardwareInterface.class != null) {
                receiver = new AdcMessageBroadcastReceiver();
                registerReceiver(receiver, getIntentFilter());
                intent = new Intent();
                intent.setClass(SmartCardActivity.this, GetValueService.class);
                intent.putExtra("mtype", "ADC");
                intent.putExtra("maction", "start");
                intent.putExtra("mfd", 1);
                SmartCardActivity.this.startService(intent);
            } else {
                Toast.makeText(getApplicationContext(), "Load hardwareinterface library error!", Toast.LENGTH_LONG).show();
            }
        }

        initButtonListener();

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
            String eid = intent.getStringExtra("EID");
            if (eid != null && eid.trim().length() > 0) {
                etEmpId.setText(eid);
                EmployeeEnrollInfo empInfo = null;
                empInfo = dbComm.getEmployeeBasicDetails(eid, empInfo);
                if (empInfo != null) {
                    int autoId = empInfo.getEnrollmentNo();
                    ArrayList <EmployeeFingerInfo> empFingerInfoList = null;
                    empFingerInfoList = dbComm.getFingerDetailsByEmployeeEnrollmentNo(autoId, empFingerInfoList);
                    setMessageForDBRead(empInfo, empFingerInfoList);
                } else {
                    showCustomAlertDialog(false, "Error", "User Not Enrolled");
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

                break;


            case 2:

                //======================== Micro Smart V2 Smart Reader ===========================//

                searchDevices(2);

                break;


            case 3:

                //======================== Morpho And Micro Smart V2 Smart Reader ===========================//

                searchDevices(3);

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

                //==================================== USB Finger Reader and Micro Smart V2 ================================================//

                if (!isMorphoSmartRcvRegisterd) {
                    isMorphoSmartRcvRegisterd = true;
                    registerReceiver(mMorphoSmartReceiver, filter, null, handler);
                }

                break;

            default:
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
                Toast.makeText(SmartCardActivity.this, "error in unregister morpho smart receiver:" + e.getMessage(), Toast.LENGTH_LONG).show();
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
                Toast.makeText(SmartCardActivity.this, "error in unregister morpho receiver:" + e.getMessage(), Toast.LENGTH_LONG).show();
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
                Toast.makeText(SmartCardActivity.this, "error in unregister morpho smart receiver:" + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        stopHandler();
        startHandler();
        startTimer();
    }

    public void startTimer() {
        if (batReadTimer == null && capReadTimer == null && adcReadTimer == null) {
            batReadTimer = new Timer();
            capReadTimer = new Timer();
            adcReadTimer = new Timer();
            initializeTimerTask();
            batReadTimer.schedule(batReadTimerTask, 0, 500); //
            capReadTimer.schedule(capReadTimerTask, 0, 50);
            adcReadTimer.schedule(adcReadTimerTask, 0, 50); //1000
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopHandler();
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
                                            Intent intent = new Intent(SmartCardActivity.this, EmployeeAttendanceActivity.class);
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
                                            Intent intent = new Intent(SmartCardActivity.this, HomeActivity.class);
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

    public void modifyActionBar() {
        getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#e63900")));
        getActionBar().setTitle(Html.fromHtml("<b><font face='Calibri' color='#FFFFFF'>Smart Card</font></b>"));
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    public void initLayoutElements() {

        pbBatPer = (ProgressBar) findViewById(R.id.pbBatPer);
        tvBatPer = (TextView) findViewById(R.id.tvBatPer);
        tvPower = (TextView) findViewById(R.id.tvPower);

        ivChargeIcon = (ImageView) findViewById(R.id.ivChargeIcon);
        ivBatTop = (ImageView) findViewById(R.id.ivBatTop);

        smart_reader = (ImageView) findViewById(R.id.smartreader);
        finger_reader = (ImageView) findViewById(R.id.fingerreader);
        etEmpId = (EditText) findViewById(R.id.empid);
        etEmpId.setFilters(new InputFilter[]{Constants.EMOJI_FILTER, new InputFilter.LengthFilter(16)});
        btn_ReadDB = (Button) findViewById(R.id.readdb);
        btn_CardRead = (Button) findViewById(R.id.cardread);
        btn_CardWrite = (Button) findViewById(R.id.cardwrite);
        btn_CardInit = (Button) findViewById(R.id.cardinitialize);
        btn_CardRefresh = (Button) findViewById(R.id.cardrefresh);
        btn_CardIdChange = (Button) findViewById(R.id.cardidchange);
        btn_NewCardIssue = (Button) findViewById(R.id.newcard);
        /*editTextCardIdci = (EditText) findViewById(R.id.cardId);*/ //Add By Sanjay Shyamal
        db_Enrodll = (TextView) findViewById(R.id.smartcard_db_enroll);
        db_CardSerial = (TextView) findViewById(R.id.smartcard_db_sno);
        db_EmpID = (TextView) findViewById(R.id.smartcard_db_empid);
        db_CardID = (TextView) findViewById(R.id.smartcard_db_cardid);
        db_Name = (TextView) findViewById(R.id.smartcard_db_name);
        db_Aadhaar = (TextView) findViewById(R.id.smartcard_db_aadhaar);
        db_BloodGroup = (TextView) findViewById(R.id.smartcard_db_bloodgroup);
        db_dob = (TextView) findViewById(R.id.smartcard_db_dob);
        db_validity = (TextView) findViewById(R.id.smartcard_db_validity);
        db_sitecode = (TextView) findViewById(R.id.smartcard_db_sitecode);
        db_card_version = (TextView) findViewById(R.id.smartcard_db_cardversion);

        db_Fnumber_one = (TextView) findViewById(R.id.smartcard_db_fnumber);
        db_Findex_one = (TextView) findViewById(R.id.smartcard_db_findex);
        db_Fquality_one = (TextView) findViewById(R.id.smartcard_db_fquality);
        db_Fnumber_two = (TextView) findViewById(R.id.smartcard_db_fnumber_two);
        db_Findex_two = (TextView) findViewById(R.id.smartcard_db_findex_two);
        db_Fquality_two = (TextView) findViewById(R.id.smartcard_db_fquality_two);
        db_SecurityLevel = (TextView) findViewById(R.id.smartcard_db_security);
        db_VerificationMode = (TextView) findViewById(R.id.smartcard_db_vmode);
        db_Fdata = (TextView) findViewById(R.id.db_firstfingerhex);
        db_Sdata = (TextView) findViewById(R.id.db_secondfingerhex);

        tbl_CardDetails = (LinearLayout) findViewById(R.id.smartcard_read);


        //=============================  For Admin Rights  ================================//

        String strRole = UserDetails.getInstance().getRole();
        if (!strRole.equals("Y")) {
            LinearLayout.LayoutParams paramsRead = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);   //Modified By Sanjay Shyamal On 20/11/17
            paramsRead.setMargins(30, 30, 30, 30); //Modified By Sanjay Shyamal On 20/11/17
            btn_CardRead.setLayoutParams(paramsRead);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, 0);
            params.setMargins(0, 0, 0, 0);
            btn_CardWrite.setLayoutParams(params);
            btn_CardInit.setLayoutParams(params);
            btn_CardRefresh.setLayoutParams(params);
            btn_CardIdChange.setLayoutParams(params);
            btn_NewCardIssue.setLayoutParams(params);
            btn_CardWrite.setVisibility(View.INVISIBLE);
            btn_CardInit.setVisibility(View.INVISIBLE);
            btn_CardRefresh.setVisibility(View.INVISIBLE);
            btn_CardIdChange.setVisibility(View.INVISIBLE);
            btn_NewCardIssue.setVisibility(View.INVISIBLE);
        }
    }

    public void initButtonListener() {

        //========================= Read Data from Local Sqlite Database ==========================//

        btn_ReadDB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideSoftKeyboard(SmartCardActivity.this);
                String empId = etEmpId.getText().toString();
                if (empId.trim().length() > 0) {
                    EmployeeEnrollInfo empInfo = null;
                    empInfo = dbComm.getEmployeeBasicDetails(empId, empInfo);
                    if (empInfo != null) {
                        int autoId = empInfo.getEnrollmentNo();
                        ArrayList <EmployeeFingerInfo> empFingerInfoList = null;
                        empFingerInfoList = dbComm.getFingerDetailsByEmployeeEnrollmentNo(autoId, empFingerInfoList);
                        setMessageForDBRead(empInfo, empFingerInfoList);
                    } else {
                        showCustomAlertDialog(false, "Error", "User Not Enrolled");
                    }
                } else {
                    showCustomAlertDialog(false, "Error", "Please Enter Employee Id");
                }
            }
        });

        //============================================Card Read====================================================//

        btn_CardRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clear();
                int smartReaderType = Settings.getInstance().getSrTypeValue();
                switch (smartReaderType) {
                    case 0:// RC632 Smart Reader
                        try {
                            rc632ReaderConnection = RC632ReaderConnection.getInstance().getSmartFinger();
                            if (rc632ReaderConnection != null) {
                                int error = -1;
                                byte[] charBuff = new byte[5];
                                error = rc632ReaderConnection.getSmartCardApi().smart_card_get_info(charBuff);
                                if (error == 0) {
                                    //  error = checkCardIsInit(2);
                                    if (error == 0) {
                                        //  parseCardReadCSN(charBuff);
                                        showDialogForCardRead("Card Read Status", "Do You Want To Read Data From Card ?", rc632ReaderConnection, 2);
                                    } else {
                                        showCustomAlertDialog(false, "Error", "Card Is Not Initialised");
                                    }
                                } else {
                                    showCustomAlertDialog(false, "Error", "Card Not Present On Terminal");
                                }
                            } else {
                                showCustomAlertDialog(false, "Device Connection Status", "RC632 Smart Reader Not Found");
                            }
                        } catch (Exception e) {
                            Toast.makeText(getBaseContext(), "Card Read Error In Button Click:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        break;

                    case 1:// Micro Smart V2 Smart Reader
                        try {
                            smartReaderConnection = SmartReaderConnection.getInstance().getmConnection();
                            intf = SmartReaderConnection.getInstance().getIntf();
                            input = SmartReaderConnection.getInstance().getInput();
                            output = SmartReaderConnection.getInstance().getOutput();
                            if (smartReaderConnection != null && intf != null && input != null && output != null) {
                                MicroSmartV2Communicator mSmartV2Comm = new MicroSmartV2Communicator(smartReaderConnection, intf, input, output);
                                String command = Utility.addCheckSum(Constants.READ_CARD_ID_COMMAND);
                                String strCardId = mSmartV2Comm.readCardId(command.getBytes());
                                int cardIdLen = strCardId.trim().length();
                                if (cardIdLen > 0) {
                                    showDialogForCardRead("Card Read Status", "Do You Want To Read Data From Card ?", mSmartV2Comm, 1);//1:USB Read
                                } else {
                                    showCustomAlertDialog(false, "Error", "Card Not Present On Terminal");
                                }
                            } else {
                                showCustomAlertDialog(false, "Device Connection Status", "Micro Smart V2 Reader Not Found");
                            }
                        } catch (Exception e) {
                            Toast.makeText(getBaseContext(), "Card Read Error In Button Click:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        break;

                    case 2:// RC522 //23c0e835//92d4b9cc
                        btn_CardRead.setEnabled(false);
                        boolean status = false;
                        RC522Communicator comm = new RC522Communicator();
                        status = comm.writeRC522(Constants.RC522_READ_CSN_COMMAND);
                        if (status) {
                            SmartCardInfo cardInfo = new SmartCardInfo();
                            char[] data = comm.readRC522();
                            setCSN(data, cardInfo);
                            if (!cardInfo.getReadCSN().equals(Constants.RC522_CARD_NOT_PRESENT_VAL)) {
                                showDialogForCardRead("Card Read Status", "Do You Want To Read Data From Card ?", comm, 3);//1:USB Read
                            } else {
                                btn_CardRead.setEnabled(true);
                                showCustomAlertDialog(false, "Error", "Card Not Present On Terminal");
                            }
                        } else {
                            btn_CardRead.setEnabled(true);
                            showCustomAlertDialog(false, "Device Connection Status", "RC522 Driver Not Found!!!");
                        }
                        break;
                    default:
                        showCustomAlertDialog(false, "Device Connection Status", "Smart Card Reader Not Configured!!!");
                        break;
                }
            }
        });

        //=============================================================Card Write==========================================================//

        btn_CardWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int smartReaderType = Settings.getInstance().getSrTypeValue();
                switch (smartReaderType) {
                    case 0:// RC632 Card Write
                        try {
                            rc632ReaderConnection = RC632ReaderConnection.getInstance().getSmartFinger();
                            if (rc632ReaderConnection != null) {
                                int error = -1;
                                byte[] charBuff = new byte[5];
                                error = rc632ReaderConnection.getSmartCardApi().smart_card_get_info(charBuff);
                                if (error == 0) {
                                    //  error = checkCardIsInit(2);
                                    if (error == 0) {
//                                        if (strEmployeeId.trim().length() > 0) {
//                                            parseCardWriteCSN(charBuff);
//                                            // EncodeDataBeforeCardWrite();
//                                            error = checkForCardKeyType(2);
////                                            if (error == 0) {
////                                                showDialogForCardWrite("Card Write Status", "Do You Want To Write Data Into Card?", intAutoId, strCSN, strDBCardId, true, 2);
////                                            } else {
////                                                showDialogForCardWrite("Card Write Status", "Do You Want To Write Data Into Card?", intAutoId, strCSN, strDBCardId, false, 2);
////                                            }
//                                        } else {
//                                            showCustomAlertDialog(false, "Error", "No Data To Write Into Card");
//                                        }
                                    } else {
                                        showCustomAlertDialog(false, "Card Init Status", "Card Is Not Initialized");
                                    }
                                } else {
                                    showCustomAlertDialog(false, "Error", "Card Not Present On Terminal");
                                }
                            } else {
                                showCustomAlertDialog(false, "Device Connection Status", "RC632 Smart Reader Not Found");
                            }
                        } catch (Exception e) {
                            Toast.makeText(getBaseContext(), "Card Write Error In Button Click:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 1:// Micro Smart V2 Card Write
                        try {
                            smartReaderConnection = SmartReaderConnection.getInstance().getmConnection();
                            intf = SmartReaderConnection.getInstance().getIntf();
                            input = SmartReaderConnection.getInstance().getInput();
                            output = SmartReaderConnection.getInstance().getOutput();
                            if (smartReaderConnection != null && intf != null && input != null && output != null) {
                                MicroSmartV2Communicator mSmartV2Comm = new MicroSmartV2Communicator(smartReaderConnection, intf, input, output);
                                String command = Utility.addCheckSum(Constants.READ_CARD_ID_COMMAND);
                                String strCardId = mSmartV2Comm.readCardId(command.getBytes());
                                strCardId = strCardId.replaceAll("[^\\d.]", "");
                                int cardIdLen = strCardId.trim().length();
                                if (cardIdLen > 0) {
                                    SmartCardInfo cardInfo = new SmartCardInfo();
                                    cardInfo.setCardId(strCardId);
                                    command = Utility.addCheckSum(Constants.CHECK_CARD_INIT_COMMAND);
                                    byte[] data = mSmartV2Comm.readSector(1, command.getBytes());
                                    if (data != null) {
                                        String empId = db_EmpID.getText().toString();
                                        if (empId.trim().length() > 0) {
                                            int fLen = db_Fdata.getText().toString().trim().length();
                                            int sLen = db_Sdata.getText().toString().trim().length();
                                            if (fLen > 0 || sLen > 0) {
                                                command = Utility.addCheckSum(Constants.READ_CSN_COMMAND);
                                                data = mSmartV2Comm.readSector(0, command.getBytes());
                                                if (data != null) {
                                                    boolean parseStatus = mSmartV2Comm.parseSectorData(0, data, cardInfo);
                                                    if (parseStatus) {
                                                        cardInfo = EncodeDataBeforeCardWrite(cardInfo);
                                                        boolean isCardHotListed = false;
                                                        isCardHotListed = dbComm.checkIsCardHotListed(cardInfo.getDbCSN());
                                                        if (!isCardHotListed) {
                                                            String pkId = db_Enrodll.getText().toString();
                                                            if (pkId.trim().length() > 0) { // Card Write is data from Sqlite
                                                                cardInfo.setEnrollmentNo(pkId);

                                                                //============================ Checks If Placed Card Is Not Issued To Any Employee  =====================//

                                                                int autoId = -1;
                                                                autoId = dbComm.getCardIssuedStatus(cardInfo.getDbCSN());
                                                                if (autoId == -1) {

                                                                    //======================= Checks If Entered Employee Is Not Issued With Any Card  ========================//

                                                                    String strEmpCSN = "";
                                                                    strEmpCSN = dbComm.getEmployeeCSN(Integer.parseInt(pkId));
                                                                    if (strEmpCSN.trim().length() == 0) {

                                                                        //===============================Checks If Employee Card Was Hotlisted Before===============================//

                                                                        int smartCardIssuedVer = 0;
                                                                        smartCardIssuedVer = dbComm.getSmartCardIssuedVer(Integer.parseInt(pkId));

                                                                        if (smartCardIssuedVer == -1) {
                                                                            cardInfo.setSmartCardVer("0");
                                                                            command = Utility.addCheckSum(Constants.CHECK_CARD_TYPE_COMMAND);
                                                                            data = mSmartV2Comm.readSector(2, command.getBytes());
                                                                            if (data != null) {//Key found in refreshed/init state
                                                                                showDialogForCardWrite("Card Write Status", "Do You Want To Write Data Into Card?", 1, true, true, mSmartV2Comm, cardInfo);
                                                                            } else {//Key found in after write state
                                                                                showDialogForCardWrite("Card Write Status", "Do You Want To Write Data Into Card?", 1, false, true, mSmartV2Comm, cardInfo);
                                                                            }
                                                                        } else {
                                                                            showCustomAlertDialog(false, "Write Error", "Employee Card Was Hot-Listed.Issue New Card To Employee And Then Proceed With Card Write ");
                                                                        }
                                                                    } else {
                                                                        showCustomAlertDialog(false, "Write Error", "Card Mismatch Found ! Placed Card Is Not Issued To Employee");
                                                                    }
                                                                } else {

                                                                    //===========================  Checks If Placed Card Is Issued To The Entered Employee   ==================//

                                                                    String cardVer = db_card_version.getText().toString();
                                                                    if (cardVer.trim().length() > 0) {
                                                                        cardInfo.setSmartCardVer(cardVer);
                                                                    } else {
                                                                        cardInfo.setSmartCardVer("0");
                                                                    }

                                                                    String strEmpCSN = "";
                                                                    strEmpCSN = dbComm.getEmployeeCSN(Integer.parseInt(pkId));
                                                                    if (cardInfo.getDbCSN().trim().equals(strEmpCSN)) {
                                                                        command = Utility.addCheckSum(Constants.CHECK_CARD_TYPE_COMMAND);
                                                                        data = mSmartV2Comm.readSector(2, command.getBytes());
                                                                        if (data != null) {//Key found in refreshed/init state
                                                                            showDialogForCardWrite("Card Write Status", "Do You Want To Write Data Into Card?", 1, true, true, mSmartV2Comm, cardInfo);
                                                                        } else {//Key found in after write state
                                                                            showDialogForCardWrite("Card Write Status", "Do You Want To Write Data Into Card?", 1, false, true, mSmartV2Comm, cardInfo);
                                                                        }
                                                                    } else {
                                                                        showCustomAlertDialog(false, "Write Error", "Card Duplication Found ! Placed Card Is Already Issued Against An Employee");
                                                                    }
                                                                }
                                                            } else {// Card Write data is from card read

                                                                //============================================= Checks If Card To Be Written Is Created Locally  ======================================//

                                                                String cardVer = db_card_version.getText().toString();
                                                                if (cardVer.trim().length() > 0) {
                                                                    cardInfo.setSmartCardVer(cardVer);
                                                                } else {
                                                                    cardInfo.setSmartCardVer("0");
                                                                }

                                                                boolean isCardReadCreatedLocal = false;
                                                                isCardReadCreatedLocal = dbComm.isCardReadCreatedLocal(cardInfo.getDbCSN());
                                                                if (isCardReadCreatedLocal) {
                                                                    int id = dbComm.getAutoId(cardInfo.getDbCSN());
                                                                    if (id != -1) {
                                                                        cardInfo.setEnrollmentNo(Integer.toString(id));
                                                                    }

                                                                    //============================================= Checks If Card Read And Card Write Is Same============================================//

                                                                    String readCsn = db_CardSerial.getText().toString();
                                                                    if (readCsn.trim().length() > 0) {
                                                                        if (readCsn.equals(cardInfo.getDbCSN())) {
                                                                            command = Utility.addCheckSum(Constants.CHECK_CARD_TYPE_COMMAND);
                                                                            data = mSmartV2Comm.readSector(2, command.getBytes());
                                                                            if (data != null) {//Key found in refreshed/init state
                                                                                showDialogForCardWrite("Card Write Status", "Do You Want To Write Data Into Card?", 1, true, false, mSmartV2Comm, cardInfo);
                                                                            } else {//Key found in after write state
                                                                                showDialogForCardWrite("Card Write Status", "Do You Want To Write Data Into Card?", 1, false, false, mSmartV2Comm, cardInfo);
                                                                            }
                                                                        } else {
                                                                            showCustomAlertDialog(false, "Write Error", "Card Misplaced ! Please Put Same Card To Write");
                                                                        }
                                                                    } else {
                                                                        showCustomAlertDialog(false, "Write Error", "Failed to read placed card csn");
                                                                    }
                                                                } else {
                                                                    showCustomAlertDialog(false, "Write Error", "Invalid Data Found ! Data To Be Written Is Not Enrolled On This Device");
                                                                }
                                                            }
                                                        } else {
                                                            showCustomAlertDialog(false, "Error", "Card Is Hot-Listed");
                                                        }
                                                    } else {
                                                        showCustomAlertDialog(false, "Error", "Failed To Parse CSN");
                                                    }
                                                } else {
                                                    showCustomAlertDialog(false, "Error", "Failed To Read CSN");
                                                }
                                            } else {
                                                showCustomAlertDialog(false, "Error", "Finger Not Enrolled ! Cannot Write Data");
                                            }
                                        } else {
                                            showCustomAlertDialog(false, "Error", "No Data To Write Into Card");
                                        }
                                    } else {
                                        showCustomAlertDialog(false, "Card Init Status", "Card Is Not Initialized");
                                    }
                                } else {
                                    showCustomAlertDialog(false, "Error", "Card Not Present On Terminal");
                                }
                            } else {
                                showCustomAlertDialog(false, "Device Connection Status", "Micro Smart V2 Reader Not Found");
                            }
                        } catch (Exception e) {
                            Toast.makeText(getBaseContext(), "Card Write Error In Button Click:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 2:// RC522 Card Write
                        btn_CardWrite.setEnabled(false);
                        boolean status = false;
                        RC522Communicator comm = new RC522Communicator();
                        status = comm.writeRC522(Constants.RC522_READ_CSN_COMMAND);
                        if (status) {
                            char[] data = comm.readRC522();
                            if (data != null && data.length > 0) {
                                String strData = new String(data);
                                String arr[] = strData.trim().split(":");
                                if (arr != null && arr.length == 2) {
                                    if (!arr[1].equals(Constants.RC522_CARD_NOT_PRESENT_VAL)) {
                                        String empId = db_EmpID.getText().toString();
                                        if (empId != null && empId.trim().length() > 0) {
                                            String isDBRead = db_Enrodll.getText().toString();
                                            if (isDBRead.trim().length() > 0) {
                                                SmartCardInfo cardInfo = new SmartCardInfo();
                                                cardInfo.setReadCSN(arr[1].trim().toUpperCase());
                                                cardInfo = EncodeDataBeforeCardWrite(cardInfo);
                                                status = comm.writeRC522(Constants.RC522_CHECK_KEY_TYPE_COMMAND);
                                                if (status) {
                                                    data = comm.readRC522();
                                                    if (data != null && data.length > 0) {
                                                        strData = new String(data);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (!strData.equals("RD-FAIL")) {
                                                                showDialogForCardWrite("Card Write Status", "Do You Want To Write Data Into Card?", 3, true, false, comm, cardInfo);
                                                            } else {
                                                                showDialogForCardWrite("Card Write Status", "Do You Want To Write Data Into Card?", 3, false, false, comm, cardInfo);
                                                            }
                                                        } else {
                                                            btn_CardWrite.setEnabled(true);
                                                        }
                                                    } else {
                                                        btn_CardWrite.setEnabled(true);
                                                    }
                                                } else {
                                                    btn_CardWrite.setEnabled(true);
                                                    showCustomAlertDialog(false, "Device Connection Status", "RC522 Driver Not Found!!!");
                                                }
                                            } else {
                                                btn_CardWrite.setEnabled(true);
                                                showCustomAlertDialog(false, "Error", "Invalid Data Found");
                                            }
                                        } else {
                                            btn_CardWrite.setEnabled(true);
                                            showCustomAlertDialog(false, "Error", "No Data To Write Into Card");
                                        }
                                    } else {
                                        btn_CardWrite.setEnabled(true);
                                        showCustomAlertDialog(false, "Error", "Card Not Present On Terminal");
                                    }
                                } else {
                                    btn_CardWrite.setEnabled(true);
                                }
                            } else {
                                btn_CardWrite.setEnabled(true);
                            }
                        } else {
                            btn_CardWrite.setEnabled(true);
                        }
                        break;
                    default:
                        showCustomAlertDialog(false, "Device Connection Status", "Smart Card Reader Not Configured!!!");
                        break;
                }
            }
        });

        //=========================================================Card Init================================================================//

        btn_CardInit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clear();
                int smartReaderType = Settings.getInstance().getSrTypeValue();
                switch (smartReaderType) {
                    case 0:// RC632 Card Initialize
                        try {
                            rc632ReaderConnection = RC632ReaderConnection.getInstance().getSmartFinger();
                            if (rc632ReaderConnection != null) {
                                int error = -1;
                                byte[] charBuff = new byte[5];
                                error = rc632ReaderConnection.getSmartCardApi().smart_card_get_info(charBuff);
                                if (error == 0) {
                                    // error = checkCardIsInit(2);
                                    if (error != 0) {
                                        //  showDialogForCardInit("Card Initialize", "Do You Want To Initialize Card With New Card ID?", 2);
                                    } else {
                                        showCustomAlertDialog(false, "Error", "Card Is Already Initialised");
                                    }
                                } else {
                                    showCustomAlertDialog(false, "Error", "Card Not Present On Terminal");
                                }
                            } else {
                                showCustomAlertDialog(false, "Device Connection Status", "RC632 Smart Reader Not Found");
                            }
                        } catch (Exception e) {
                            Toast.makeText(getBaseContext(), "Card init error In Button Click:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 1:// Micro Smart V2 Card Initialize
                        try {
                            smartReaderConnection = SmartReaderConnection.getInstance().getmConnection();
                            intf = SmartReaderConnection.getInstance().getIntf();
                            input = SmartReaderConnection.getInstance().getInput();
                            output = SmartReaderConnection.getInstance().getOutput();
                            if (smartReaderConnection != null && intf != null && input != null && output != null) {
                                MicroSmartV2Communicator mSmartV2Comm = new MicroSmartV2Communicator(smartReaderConnection, intf, input, output);
                                String command = Utility.addCheckSum(Constants.READ_CARD_ID_COMMAND);
                                String strCardId = mSmartV2Comm.readCardId(command.getBytes());
                                strCardId = strCardId.replaceAll("[^\\d.]", "");
                                int cardIdLen = strCardId.trim().length();
                                if (cardIdLen > 0) {
                                    SmartCardInfo cardInfo = new SmartCardInfo();
                                    cardInfo.setCardId(strCardId);
                                    command = Utility.addCheckSum(Constants.CHECK_CARD_INIT_COMMAND);
                                    byte[] data = mSmartV2Comm.readSector(1, command.getBytes());
                                    if (data == null) {
                                        showDialogForCardInit("Card Initialize", "Do You Want To Initialize Card With New Card ID?", 1, mSmartV2Comm);
                                    } else {
                                        showCustomAlertDialog(false, "Error", "Card is already initialized");
                                    }
                                } else {
                                    showCustomAlertDialog(false, "Error", "Card is not present on terminal");
                                }
                            } else {
                                showCustomAlertDialog(false, "Device Connection Status", "Micro Smart V2 Reader Not Found");
                            }

                        } catch (Exception e) {
                            Toast.makeText(getBaseContext(), "Card init error In Button Click:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 2:// RC522 Card Initialize
                        btn_CardInit.setEnabled(false);
                        boolean status = false;
                        RC522Communicator comm = new RC522Communicator();
                        status = comm.writeRC522(Constants.RC522_READ_CSN_COMMAND);
                        if (status) {
                            char[] data = comm.readRC522();
                            if (data != null && data.length > 0) {
                                String strData = new String(data);
                                String arr[] = strData.trim().split(":");
                                if (arr != null && arr.length == 2) {
                                    strData = arr[1].trim();
                                    if (strData.equals(Constants.RC522_CARD_NOT_PRESENT_VAL)) {
                                        showCustomAlertDialog(false, "Error", "Card Not Present On Terminal");
                                        btn_CardInit.setEnabled(true);
                                        return;
                                    }
                                    status = comm.writeRC522(Constants.RC522_CHECK_CARD_INIT);
                                    if (status) {
                                        data = comm.readRC522();
                                        if (data != null && data.length > 0) {
                                            strData = new String(data);
                                            String arr1[] = strData.trim().split(":");
                                            if (arr1 != null && arr1.length == 3) {
                                                strData = arr1[2].trim();
                                                if (strData.equals("RD-FAIL")) {
                                                    showDialogForCardInit("Card Initialize", "Do You Want To Initialize Card With New Card ID?", 3, comm);
                                                } else {
                                                    btn_CardInit.setEnabled(true);
                                                    showCustomAlertDialog(false, "Error", "Card Is Already Initialized");
                                                }
                                            } else {
                                                btn_CardInit.setEnabled(true);
                                            }
                                        } else {
                                            btn_CardInit.setEnabled(true);
                                        }
                                    } else {
                                        btn_CardInit.setEnabled(true);
                                    }
                                } else {
                                    btn_CardInit.setEnabled(true);
                                }
                            } else {
                                btn_CardInit.setEnabled(true);
                            }
                        } else {
                            btn_CardInit.setEnabled(true);
                            showCustomAlertDialog(false, "Device Connection Status", "RC522 Driver Not Found!!!");
                        }
                        break;
                    default:
                        showCustomAlertDialog(false, "Device Connection Status", "Smart Card Reader Not Configured!!!");
                        break;
                }
            }
        });


        //======================================================Card Refresh================================================================//

        btn_CardRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clear();
                int smartReaderType = Settings.getInstance().getSrTypeValue();
                switch (smartReaderType) {
                    case 0:// RC632 Card Refresh
//                        try {
//                            rc632ReaderConnection = RC632ReaderConnection.getInstance().getSmartFinger();
//                            if (rc632ReaderConnection != null) {
//                                int error = -1;
//                                byte[] charBuff = new byte[5];
//                                error = rc632ReaderConnection.getSmartCardApi().smart_card_get_info(charBuff);
//                                if (error == 0) {
//                                    error = checkCardIsInit(2);
//                                    if (error == 0) {
//                                        parseCardWriteCSN(charBuff);
//                                        if (strCSN != null && strCSN.trim().length() > 0) {
//                                            String strCardId = getCardId(2);
//                                            if (strCardId != null && strCardId.trim().length() > 0) {
//                                                boolean isCardHotListed = false;
//                                                isCardHotListed = dbLayer.checkIsCardHotListed(strCSN);
//                                                if (!isCardHotListed) {
//                                                    boolean isCardCreatedLocal = false;
//                                                    isCardCreatedLocal = dbLayer.checkIsCardCreatedLocal(strCSN, strCardId);
//                                                    if (isCardCreatedLocal) {
//                                                        strCardId = strCardId.replaceAll("\\G0", " ").trim();
//                                                        error = checkCardIsRefreshed(2);
//                                                        if (error != 0) {
//                                                            int empAutoId = dbLayer.getEmpAutoId(strCSN, strCardId);
//                                                            if (empAutoId != -1) {
//                                                                //         showDialogForCardRefresh("Card Refresh", "Do You Want To Refresh Card With New Card ID?", empAutoId, strCSN, strCardId, "Y", 2);
//                                                            } else {
//                                                                showCustomAlertDialog(false, "Error", "Employee Data Not Found");
//                                                            }
//                                                        } else {
//                                                            showCustomAlertDialog(false, "Error", "Card Is Already Refreshed");
//                                                        }
//                                                    } else {
//                                                        strCardId = strCardId.replaceAll("\\G0", " ").trim();
//                                                        showCustomAlertDialog(false, "Error", "Card Id Not Enrolled On Device");
//                                                        // showDialogForInitAndRefresh("Card Refresh", "Card Not Enrolled On This Device.\nDo You Want To Refresh Card With New Card ID?", "Card Refresh", strCSN, strReadCardId, "N");
//                                                    }
//                                                } else {
//                                                    showCustomAlertDialog(false, "Error", "Card Is Hot-Listed");
//                                                }
//                                            } else {
//                                                showCustomAlertDialog(false, "Error", "Failed To Read Card Id");
//                                            }
//                                        } else {
//                                            showCustomAlertDialog(false, "Error", "Failed To Parse CSN");
//                                        }
//                                    } else {
//                                        showCustomAlertDialog(false, "Error", "Card Is Not Initialised");
//                                    }
//                                } else {
//                                    showCustomAlertDialog(false, "Error", "Card Not Present On Terminal");
//                                }
//                            } else {
//                                showCustomAlertDialog(false, "Device Connection Status", "RC632 Smart Reader Not Found");
//                            }
//
//                        } catch (Exception e) {
//                            Toast.makeText(getBaseContext(), "Card Refresh Error In Refresh Button Click:" + e.getMessage(), Toast.LENGTH_SHORT).show();
//                        }

                        break;
                    case 1: // Micro Smart V2 Card Refresh
                        try {
                            smartReaderConnection = SmartReaderConnection.getInstance().getmConnection();
                            intf = SmartReaderConnection.getInstance().getIntf();
                            input = SmartReaderConnection.getInstance().getInput();
                            output = SmartReaderConnection.getInstance().getOutput();
                            if (smartReaderConnection != null && intf != null && input != null && output != null) {
                                MicroSmartV2Communicator mSmartV2Comm = new MicroSmartV2Communicator(smartReaderConnection, intf, input, output);
                                String command = Utility.addCheckSum(Constants.READ_CARD_ID_COMMAND);
                                String strCardId = mSmartV2Comm.readCardId(command.getBytes());
                                strCardId = strCardId.replaceAll("[^\\d.]", "");
                                int cardIdLen = strCardId.trim().length();
                                if (cardIdLen > 0) {
                                    SmartCardInfo cardInfo = new SmartCardInfo();
                                    cardInfo.setCardId(strCardId);
                                    command = Utility.addCheckSum(Constants.CHECK_CARD_INIT_COMMAND);
                                    byte[] data = mSmartV2Comm.readSector(1, command.getBytes());
                                    if (data != null) {
                                        command = Utility.addCheckSum(Constants.READ_CSN_COMMAND);
                                        data = mSmartV2Comm.readSector(0, command.getBytes());
                                        if (data != null) {
                                            boolean parseStatus = mSmartV2Comm.parseSectorData(0, data, cardInfo);
                                            if (parseStatus) {
                                                boolean isCardHotListed = false;
                                                isCardHotListed = dbComm.checkIsCardHotListed(cardInfo.getReadCSN());
                                                if (!isCardHotListed) {
                                                    boolean isCardCreatedLocal = false;
                                                    isCardCreatedLocal = dbComm.checkIsCardCreatedLocal(cardInfo.getReadCSN().trim(), cardInfo.getCardId());
                                                    // isCardCreatedLocal = true;//demo purpose
                                                    if (isCardCreatedLocal) {
                                                        cardInfo.setCardCreatedLocally(true);
                                                        command = Utility.addCheckSum(Constants.CHECK_CARD_IS_REFRESHED);
                                                        data = mSmartV2Comm.readSector(2, command.getBytes());
                                                        if (data == null) {
                                                            int empAutoId = dbComm.getEmpAutoId(cardInfo.getReadCSN().trim(), cardInfo.getCardId().trim());
                                                            //   empAutoId = 2;//demo purpose
                                                            if (empAutoId != -1) {
                                                                cardInfo.setEnrollmentNo(Integer.toString(empAutoId));
                                                                int version = dbComm.getSmartCardIssuedVer(empAutoId);
                                                                if (version != -1) {
                                                                    cardInfo.setSmartCardVer(Integer.toString(version));
                                                                    showDialogForCardRefresh("Card Refresh", "Do You Want To Refresh Card With New Card ID?", 1, mSmartV2Comm, cardInfo);
                                                                }
                                                            } else {
                                                                showCustomAlertDialog(false, "Error", "Employee Data Not Found");
                                                            }
                                                        } else {
                                                            showCustomAlertDialog(false, "Error", "Card is already refreshed");
                                                        }
                                                    } else {
                                                        showCustomAlertDialog(false, "Error", "Card Is Not Enrolled On Device");
                                                    }
                                                } else {
                                                    showCustomAlertDialog(false, "Error", "Card is hotlisted");
                                                }
                                            } else {
                                                showCustomAlertDialog(false, "Error", "Failed to parse Card Serial no");
                                            }
                                        } else {
                                            showCustomAlertDialog(false, "Error", "Failed to read Card Serial no");
                                        }
                                    } else {
                                        showCustomAlertDialog(false, "Error", "Card is not initialized");
                                    }
                                } else {
                                    showCustomAlertDialog(false, "Error", "Card not present on terminal");
                                }
                            } else {
                                showCustomAlertDialog(false, "Device Connection Status", "Micro Smart V2 Reader Not Found");
                            }
                        } catch (Exception e) {
                            Toast.makeText(getBaseContext(), "Card Refresh Error In Refresh Button Click:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        break;

                    case 2:// RC522 Card Refresh
                        btn_CardRefresh.setEnabled(false);
                        boolean status = false;
                        RC522Communicator comm = new RC522Communicator();
                        status = comm.writeRC522(Constants.RC522_READ_CSN_COMMAND);
                        if (status) {
                            char[] data = comm.readRC522();
                            if (data != null && data.length > 0) {
                                String strData = new String(data);
                                String arr[] = strData.trim().split(":");
                                if (arr != null && arr.length == 2) {
                                    strData = arr[1].trim();
                                    if (!strData.equals(Constants.RC522_CARD_NOT_PRESENT_VAL)) {
                                        SmartCardInfo cardInfo = new SmartCardInfo();
                                        cardInfo.setReadCSN(arr[1].trim().toUpperCase());
                                        status = comm.writeRC522(Constants.RC522_READ_CARDID_COMMAND);
                                        if (status) {
                                            char[] readData = comm.readRC522();
                                            if (readData != null && readData.length > 0) {
                                                setCardId(readData, cardInfo);
                                                status = comm.writeRC522(Constants.RC522_CHECK_CARD_IS_REFRESHED);
                                                if (status) {
                                                    data = comm.readRC522();
                                                    if (data != null && data.length > 0) {
                                                        strData = new String(data);
                                                        arr = strData.trim().split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("RD-FAIL")) {//READ_CARD:8:RD-FAIL
                                                                showDialogForCardRefresh("Card Refresh", "Do You Want To Refresh Card With New Card ID?", 3, comm, cardInfo);
                                                            } else {
                                                                showCustomAlertDialog(false, "Error", "Card is already refreshed");
                                                                btn_CardRefresh.setEnabled(true);
                                                            }
                                                        } else {
                                                            btn_CardRefresh.setEnabled(true);
                                                        }
                                                    } else {
                                                        btn_CardRefresh.setEnabled(true);
                                                    }
                                                } else {
                                                    btn_CardRefresh.setEnabled(true);
                                                }
                                            } else {
                                                btn_CardRefresh.setEnabled(true);
                                            }
                                        } else {
                                            btn_CardRefresh.setEnabled(true);
                                        }
                                    } else {
                                        btn_CardRefresh.setEnabled(true);
                                        showCustomAlertDialog(false, "Error", "Card Not Present On Terminal");
                                    }
                                } else {
                                    btn_CardRefresh.setEnabled(true);
                                }
                            } else {
                                btn_CardRefresh.setEnabled(true);
                            }
                        } else {
                            btn_CardRefresh.setEnabled(true);
                        }
                        break;
                    default:
                        showCustomAlertDialog(false, "Device Connection Status", "Smart Card Reader Not Configured!!!");
                        break;
                }
            }
        });

        //===============================================Card Id Change===============================================//

        btn_CardIdChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clear();
                int smartReaderType = Settings.getInstance().getSrTypeValue();
                smartReaderType = 2;
                switch (smartReaderType) {
                    case 0:
                        try {
                            rc632ReaderConnection = RC632ReaderConnection.getInstance().getSmartFinger();
                            if (rc632ReaderConnection != null) {
                                int error = -1;
                                byte[] charBuff = new byte[5];
                                error = rc632ReaderConnection.getSmartCardApi().smart_card_get_info(charBuff);
                                if (error == 0) {
                                    //error = checkCardIsInit(2);
                                    if (error == 0) {
                                        //  showDialogForCardIdChange("Card ID Change", "Do You Want To Change Card ID?", 2);
                                    } else {
                                        showCustomAlertDialog(false, "Error", "Card Is Not Initialised");
                                    }
                                } else {
                                    showCustomAlertDialog(false, "Error", "Card Not Present On Terminal");
                                }
                            } else {
                                showCustomAlertDialog(false, "Device Connection Status", "RC632 Smart Reader Not Found");
                            }
                        } catch (Exception e) {
                            Toast.makeText(getBaseContext(), "Card Id Change Error In Refresh Button Click:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 1:
                        try {
                            smartReaderConnection = SmartReaderConnection.getInstance().getmConnection();
                            intf = SmartReaderConnection.getInstance().getIntf();
                            input = SmartReaderConnection.getInstance().getInput();
                            output = SmartReaderConnection.getInstance().getOutput();
                            if (smartReaderConnection != null && intf != null && input != null && output != null) {
                                MicroSmartV2Communicator mSmartV2Comm = new MicroSmartV2Communicator(smartReaderConnection, intf, input, output);
                                String command = Utility.addCheckSum(Constants.READ_CARD_ID_COMMAND);
                                String strCardId = mSmartV2Comm.readCardId(command.getBytes());
                                strCardId = strCardId.replaceAll("[^\\d.]", "");
                                int cardIdLen = strCardId.trim().length();
                                if (cardIdLen > 0) {
                                    SmartCardInfo cardInfo = new SmartCardInfo();
                                    cardInfo.setCardId(strCardId);
                                    command = Utility.addCheckSum(Constants.CHECK_CARD_INIT_COMMAND);
                                    byte[] data = mSmartV2Comm.readSector(1, command.getBytes());
                                    if (data != null) {
                                        command = Utility.addCheckSum(Constants.READ_CSN_COMMAND);
                                        data = mSmartV2Comm.readSector(0, command.getBytes());
                                        if (data != null) {
                                            boolean parseStatus = mSmartV2Comm.parseSectorData(0, data, cardInfo);
                                            if (parseStatus) {
                                                showDialogForCardIdChange("Card ID Change", "Do You Want To Change Card ID ?", 1, mSmartV2Comm, cardInfo);
                                            } else {
                                                showCustomAlertDialog(false, "Error", "Failed to parse Card Serial no");
                                            }
                                        } else {
                                            showCustomAlertDialog(false, "Error", "Failed to read Card Serial no");
                                        }
                                    } else {
                                        showCustomAlertDialog(false, "Error", "Card is not initialized");
                                    }
                                } else {
                                    showCustomAlertDialog(false, "Error", "Card not present on terminal");
                                }
                            } else {
                                showCustomAlertDialog(false, "Device Connection Status", "Micro Smart V2 Reader Not Found");
                            }
                        } catch (Exception e) {
                            Toast.makeText(getBaseContext(), "Card Id Change Error In Refresh Button Click:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 2:
                        btn_CardIdChange.setEnabled(false);
                        boolean status = false;
                        RC522Communicator comm = new RC522Communicator();
                        status = comm.writeRC522(Constants.RC522_READ_CSN_COMMAND);
                        if (status) {
                            char[] data = comm.readRC522();
                            if (data != null && data.length > 0) {
                                String strData = new String(data);
                                String arr[] = strData.trim().split(":");
                                if (arr != null && arr.length == 2) {
                                    strData = arr[1].trim();
                                    if (!strData.equals(Constants.RC522_CARD_NOT_PRESENT_VAL)) {
                                        SmartCardInfo cardInfo = new SmartCardInfo();
                                        cardInfo.setReadCSN(arr[1].trim().toUpperCase());
                                        showDialogForCardIdChange("Card ID Change", "Do You Want To Change Card ID ?", 3, comm, cardInfo);
                                    } else {
                                        btn_CardIdChange.setEnabled(true);
                                        showCustomAlertDialog(false, "Error", "Card Not Present On Terminal");
                                    }
                                } else {
                                    btn_CardIdChange.setEnabled(true);
                                }
                            } else {
                                btn_CardIdChange.setEnabled(true);
                            }
                        } else {
                            btn_CardIdChange.setEnabled(true);
                        }
                        break;
                    default:
                        showCustomAlertDialog(false, "Device Connection Status", "Smart Card Reader Type Not Configured!!!");
                        break;
                }

                //=========================Test Function Call To Change Fortuna Access Code to Factory Card Access Code========//
//                rc632ReaderConnection = RC632ReaderConnection.getInstance().getSmartFinger();
//                if (rc632ReaderConnection != null) {
//                    int error = -1;
//                    byte[] charBuff = new byte[5];
//                    error = rc632ReaderConnection.getSmartCardApi().smart_card_get_info(charBuff);
//
//                    if (error == 0) {
//
//                        int blockVal=7;
//                        error = rc632ReaderConnection.testblockWrite(blockVal);
//                        Log.d("TEST", "Block No:" + (byte) blockVal + "Error No:" + error);
//
//                        blockVal=39;
//                        error = rc632ReaderConnection.testblockWrite(blockVal);
//                        Log.d("TEST", "Block No:" + (byte) blockVal + "Error No:" + error);
//
//                        blockVal=51;
//                        error = rc632ReaderConnection.testblockWrite(blockVal);
//                        Log.d("TEST", "Block No:" + (byte) blockVal + "Error No:" + error);
//
//                    } else {
//                        showCustomAlertDialog(false, "Error", "Card Not Present On Terminal");
//                    }
//                } else {
//                    showCustomAlertDialog(false, "Device Connection Status", "Card Reader Not Found");
//                }

                //======================================================================================================//


                //=====================Fortuna Card To Factory Card============================//

//                rc632ReaderConnection = RC632ReaderConnection.getInstance().getSmartFinger();
//
//                if (rc632ReaderConnection != null) {
//                    int error = -1;
//                    byte[] charBuff = new byte[5];
//                    error = rc632ReaderConnection.getSmartCardApi().smart_card_get_info(charBuff);
//
//                    if (error == 0) {
//                        for (int i = 0; i < 16; i++) {
//                            error = rc632ReaderConnection.fortunaCardToFactoryCard((byte) i);
//                            Log.d("TEST", "Init Sector No:" + (byte) i + "Error No:" + error);
//                        }
//                    } else {
//                        showCustomAlertDialog(false, "Error", "Card Not Present On Terminal");
//                    }
//                } else {
//                    showCustomAlertDialog(false, "Device Connection Status", "Card Reader Not Found");
//                }

                //==============================================================================//


                //=====================Init Card To Factory Card============================//

//                rc632ReaderConnection = RC632ReaderConnection.getInstance().getSmartFinger();
//
//                if (rc632ReaderConnection != null) {
//                    int error = -1;
//                    byte[] charBuff = new byte[5];
//                    error = rc632ReaderConnection.getSmartCardApi().smart_card_get_info(charBuff);
//
//                    if (error == 0) {
//                        for (int i = 0; i < 16; i++) {
//                            error = rc632ReaderConnection.initCardToFactoryCard((byte) i);
//                            Log.d("TEST", "Init Sector No:" + (byte) i + "Error No:" + error);
//                        }
//                    } else {
//                        showCustomAlertDialog(false, "Error", "Card Not Present On Terminal");
//                    }
//                } else {
//                    showCustomAlertDialog(false, "Device Connection Status", "Card Reader Not Found");
//                }

                //==============================================================================//
            }

        });

        //=======================================================New Card Issue===============================================================//

        btn_NewCardIssue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //try {


//                RC522Communicator comm = new RC522Communicator();
//                AsyncTaskRC522FactoryCard task = new AsyncTaskRC522FactoryCard(comm);
//                task.execute();


                // smartReaderConnection = SmartReaderConnection.getInstance().getmConnection();
//                    intf = SmartReaderConnection.getInstance().getIntf();
//                    input = SmartReaderConnection.getInstance().getInput();
//                    output = SmartReaderConnection.getInstance().getOutput();
//                    if (smartReaderConnection != null && intf != null && input != null && output != null) {
//                        MicroSmartV2Communicator mSmartV2Comm = new MicroSmartV2Communicator(smartReaderConnection, intf, input, output);
//                        String command = Utility.addCheckSum(Constants.READ_CARD_ID_COMMAND);
//                        String strCardId = mSmartV2Comm.readCardId(command.getBytes());
//                        strCardId = strCardId.replaceAll("[^\\d.]", "");
//                        int cardIdLen = strCardId.trim().length();
//                        if (cardIdLen > 0) {
//                            final SmartCardInfo cardInfo = new SmartCardInfo();
//                            cardInfo.setCardId(strCardId);
//                            command = Utility.addCheckSum(Constants.CHECK_CARD_INIT_COMMAND);
//                            byte[] data = mSmartV2Comm.readSector(1, command.getBytes());
//                            if (data != null) {
//                                command = Utility.addCheckSum(Constants.READ_CSN_COMMAND);
//                                data = mSmartV2Comm.readSector(0, command.getBytes());
//                                if (data != null) {
//                                    boolean parseStatus = mSmartV2Comm.parseSectorData(0, data, cardInfo);
//                                    if (parseStatus) {
//
//                                        //==============================Card ID Dialog=======================================//
//
//                                        final Context context = SmartCardActivity.this;
//                                        final Dialog employeeDialog = new Dialog(context);
//                                        employeeDialog.setCanceledOnTouchOutside(true);
//                                        employeeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//                                        employeeDialog.setContentView(R.layout.employeeid_dialog);
//
//                                        ImageView icon = (ImageView) employeeDialog.findViewById(R.id.image);
//                                        TextView title = (TextView) employeeDialog.findViewById(R.id.title);
//                                        final EditText editTextEmployeeId = (EditText) employeeDialog.findViewById(R.id.empId);
//                                        editTextEmployeeId.setFilters(new InputFilter[]{Constants.EMOJI_FILTER, new InputFilter.LengthFilter(16)});
//
//                                        Button btn_Ok = (Button) employeeDialog.findViewById(R.id.btnOk);
//                                        Button btn_Cancel = (Button) employeeDialog.findViewById(R.id.btnCancel);
//                                        icon.setImageResource(R.drawable.success);
//                                        title.setText("Employee Id Entry");
//
//                                        btn_Ok.setOnClickListener(new View.OnClickListener() {
//                                            @Override
//                                            public void onClick(View view) {
//                                                try {
//                                                    String strEmployeeId = "";
//                                                    strEmployeeId = editTextEmployeeId.getText().toString().trim();
//                                                    final String empId = strEmployeeId;
//                                                    Log.d("TEST", "Employee Id In New Card Issue:" + empId);
//                                                    if (empId.trim().length() > 0) {
//
//                                                        //============================ Check If Employee Id is Enrolled ==========================//
//
//                                                        int autoEmpId = -1;
//                                                        autoEmpId = dbComm.getAutoIdByEmpId(empId);
//                                                        final int pkEmpId = autoEmpId;
//                                                        if (pkEmpId != -1) {
//                                                            cardInfo.setEmployeeId(empId);
//                                                            cardInfo.setEnrollmentNo(Integer.toString(pkEmpId));
//
//                                                            String strEmpCSN = "";
//                                                            strEmpCSN = dbComm.getEmployeeCSN(pkEmpId);
//                                                            final String empCSN = strEmpCSN;
//
//                                                            //========================== Check If Card To Be Issued Is Not Hotlisted =====================//
//
//                                                            boolean isCardHotlisted = false;
//                                                            isCardHotlisted = dbComm.checkIsCardHotListed(cardInfo.getReadCSN().trim());
//                                                            if (!isCardHotlisted) {
//
//                                                                //========================== Check If Old Card And New Card Are Not Same ============//
//
//                                                                if (!cardInfo.getReadCSN().trim().equals(empCSN)) {
//
//                                                                    //====================== Check If Card Is Not Issued Against Any Employee ======================//
//
//                                                                    int cardIssuedEmpId = -1;
//                                                                    cardIssuedEmpId = dbComm.getCardIssuedStatus(cardInfo.getReadCSN().trim());
//                                                                    if (cardIssuedEmpId == -1) {
//                                                                        int oldCardVersion = -1;
//                                                                        oldCardVersion = dbComm.getSmartCardIssuedVer(pkEmpId);
//                                                                        final int cardVersion = oldCardVersion;
//                                                                        if (cardVersion != -1) {
//                                                                            employeeDialog.dismiss();
//                                                                            final int newCardVersion = cardVersion + 1;
//                                                                            cardInfo.setSmartCardVer(Integer.toString(newCardVersion));
//                                                                            final String reasons[] = {"Select", "Card Lost", "Card Damaged", "Card Refreshed"};
//                                                                            final Context context = SmartCardActivity.this;
//                                                                            final Dialog newCardDialog = new Dialog(context);
//                                                                            newCardDialog.setCanceledOnTouchOutside(true);
//                                                                            newCardDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//                                                                            newCardDialog.setContentView(R.layout.newcard_dialog);
//
//                                                                            ImageView icon = (ImageView) newCardDialog.findViewById(R.id.image);
//                                                                            TextView title = (TextView) newCardDialog.findViewById(R.id.title);
//
//                                                                            EditText editTextEmpId = (EditText) newCardDialog.findViewById(R.id.empId);
//                                                                            TextView oldCardVer = (TextView) newCardDialog.findViewById(R.id.oldCardVer);
//                                                                            TextView newCardVer = (TextView) newCardDialog.findViewById(R.id.newCardVer);
//                                                                            final Spinner reasonSpinner = (Spinner) newCardDialog.findViewById(R.id.reason);
//
//                                                                            Button btn_Ok = (Button) newCardDialog.findViewById(R.id.btnOk);
//                                                                            Button btn_Cancel = (Button) newCardDialog.findViewById(R.id.btnCancel);
//                                                                            icon.setImageResource(R.drawable.success);
//                                                                            title.setText("Issue New Card");
//
//                                                                            ArrayAdapter adapter = new ArrayAdapter <String>(context, android.R.layout.simple_spinner_item, reasons);
//                                                                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                                                                            reasonSpinner.setAdapter(adapter);
//
//                                                                            oldCardVer.setText(Integer.toString(oldCardVersion));
//                                                                            newCardVer.setText(Integer.toString(newCardVersion));
//                                                                            editTextEmpId.setText(empId);
//
//                                                                            btn_Ok.setOnClickListener(new View.OnClickListener() {
//                                                                                @Override
//                                                                                public void onClick(View view) {
//                                                                                    String strReason = reasonSpinner.getSelectedItem().toString();
//                                                                                    if (!strReason.equals(reasons[0])) {
//                                                                                        newCardDialog.dismiss();
//                                                                                        showDialogForNewCardIssue(true, "New Card Issue", "New Card Issue Will Hot-List Prevoius Card ! Are You Sure You Want To Proceed ?", strReason, cardInfo);
//                                                                                    } else {
//                                                                                        showCustomAlertDialog(false, "Error", "Please Select Hot-List Reason");
//                                                                                    }
//                                                                                }
//                                                                            });
//
//                                                                            btn_Cancel.setOnClickListener(new View.OnClickListener() {
//                                                                                @Override
//                                                                                public void onClick(View view) {
//                                                                                    newCardDialog.dismiss();
//                                                                                }
//                                                                            });
//
//                                                                            newCardDialog.show();
//                                                                            WindowManager.LayoutParams lp = newCardDialog.getWindow().getAttributes();
//                                                                            lp.dimAmount = 0.9f;
//                                                                            lp.buttonBrightness = 1.0f;
//                                                                            lp.screenBrightness = 1.0f;
//                                                                            newCardDialog.getWindow().setAttributes(lp);
//                                                                            newCardDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
//
//                                                                        } else {
//                                                                            showCustomAlertDialog(false, "Error", "Card Not Issued ! Old Card Not Found Against Employee");
//                                                                        }
//                                                                    } else {
//                                                                        showCustomAlertDialog(false, "Error", "Card Duplication Found ! Placed Card Is Already Issued Against An Employee");
//                                                                    }
//                                                                } else {
//                                                                    showCustomAlertDialog(false, "Error", "Same Card Cannot Be Issued As New Card");
//                                                                }
//                                                            } else {
//                                                                showCustomAlertDialog(false, "Error", "Card Is Hot-Listed");
//                                                            }
//                                                        } else {
//                                                            showCustomAlertDialog(false, "Error", "Employee Id Not Enrolled");
//                                                        }
//
//                                                    } else {
//                                                        showCustomAlertDialog(false, "Error", "Employee Id Cannot Be Left Blank");
//
//                                                    }
//
//                                                } catch (Exception e) {
//                                                    Toast.makeText(getBaseContext(), "Card Refresh Error In Yes:" + e.getMessage(), Toast.LENGTH_SHORT).show();
//                                                }
//
//                                                //========================================================================================//
//
//
//                                            }
//                                        });
//
//                                        btn_Cancel.setOnClickListener(new View.OnClickListener() {
//                                            @Override
//                                            public void onClick(View view) {
//                                                employeeDialog.dismiss();
//                                            }
//                                        });
//
//                                        employeeDialog.show();
//
//                                        WindowManager.LayoutParams lp = employeeDialog.getWindow().getAttributes();
//                                        lp.dimAmount = 0.9f;
//                                        lp.buttonBrightness = 1.0f;
//                                        lp.screenBrightness = 1.0f;
//                                        employeeDialog.getWindow().setAttributes(lp);
//                                        employeeDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
//
//                                    } else {
//                                        showCustomAlertDialog(false, "Error", "Failed to parse Card Serial no");
//                                    }
//                                } else {
//                                    showCustomAlertDialog(false, "Error", "Failed to read Card Serial no");
//                                }
//                            } else {
//                                showCustomAlertDialog(false, "Error", "Card is not initialized");
//                            }
//
//
//                        } else {
//                            showCustomAlertDialog(false, "Error", "Card Not Present On Terminal");
//                        }
//                    } else {
//                        showCustomAlertDialog(false, "Device Connection Status", "Card Reader Not Found");
//                    }
//                } catch (Exception e) {
//                    Toast.makeText(getBaseContext(), "Card Refresh Error In Refresh Button Click:" + e.getMessage(), Toast.LENGTH_SHORT).show();
//                }
            }
        });


//        btn_FactoryCard.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                try {
//                    smartReaderConnection = SmartReaderConnection.getInstance().getmConnection();
//                    intf = SmartReaderConnection.getInstance().getIntf();
//                    input = SmartReaderConnection.getInstance().getInput();
//                    output = SmartReaderConnection.getInstance().getOutput();
//
//                    if (smartReaderConnection != null && intf != null && input != null && output != null) {
//
//                        strReadCardId = checkCardPresent();
//
//                        if (strReadCardId.trim().length() > 0) {
//
//                            //showDialogForInitAndRefresh("Card Refresh","Do You Want To Refresh Card With New Card ID?", "Card Refresh");
//
//                            AsynTaskFactoryCard factoryCard = new AsynTaskFactoryCard();
//                            factoryCard.execute();
//
//
//                        } else {
//                            showCustomAlertDialog(false, "Error", "Card Not Present");
//                        }
//
//                    } else {
//                        showCustomAlertDialog(false, "Device Connection Status", "Card Reader Not Found");
//                    }
//                } catch (Exception e) {
//                    Toast.makeText(getBaseContext(), "Card Refresh Error In Refresh Button Click:" + e.getMessage(), Toast.LENGTH_SHORT).show();
//                }
//
//
//            }
//        });
    }

    private void setCSN(char[] readData, SmartCardInfo cardInfo) {
        String strData = new String(readData);
        String arr[] = strData.trim().split(":");
        if (arr != null && arr.length == 2) {
            strData = arr[1].trim();
            cardInfo.setReadCSN(strData.toUpperCase());
        }
    }

    private void setCardId(char[] readData, SmartCardInfo cardInfo) {
        String cardId = new String(readData);
        String arr[] = cardId.split(":");
        if (arr != null && arr.length == 3) {
            cardId = arr[2].trim();
            if (cardId.length() >= 17) {
                cardId = cardId.substring(0, 16);
                cardId = Utility.hexToAscii(cardId);
                cardInfo.setCardId(cardId);
            } else {
                cardInfo.setCardId("");
            }
        }
    }

    public void clear() {
        etEmpId.setText("");
        db_Enrodll.setText("");
        db_CardSerial.setText("");
        db_EmpID.setText("");
        db_CardID.setText("");
        db_Name.setText("");
        db_Aadhaar.setText("");
        db_BloodGroup.setText("");
        db_dob.setText("");
        db_validity.setText("");
        db_sitecode.setText("");
        db_card_version.setText("");

        db_Fnumber_one.setText("");
        db_Findex_one.setText("");
        db_Fquality_one.setText("");
        db_Fdata.setText("");

        db_Fnumber_two.setText("");
        db_Findex_two.setText("");
        db_Fquality_two.setText("");
        db_Sdata.setText("");

        db_SecurityLevel.setText("");
        db_VerificationMode.setText("");

        tbl_CardDetails.setVisibility(View.INVISIBLE);

    }

    public void setMessageForDBRead(EmployeeEnrollInfo
                                            empEnrollInfo, ArrayList <EmployeeFingerInfo> empFingerInfoList) {

        ViewGroup.LayoutParams paramsEmpDetails = tbl_CardDetails.getLayoutParams();
        paramsEmpDetails.width = ViewGroup.LayoutParams.MATCH_PARENT;   //Modified By Sanjay Shyamal On 27/11/17
        paramsEmpDetails.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        tbl_CardDetails.setLayoutParams(paramsEmpDetails);
        tbl_CardDetails.setVisibility(View.VISIBLE);

        db_Enrodll.setText(Integer.toString(empEnrollInfo.getEnrollmentNo()));
        db_CardSerial.setText(empEnrollInfo.getCSN());
        db_EmpID.setText(empEnrollInfo.getEmpId().trim());
        db_CardID.setText(empEnrollInfo.getCardId().replaceAll("\\G0", " ").trim());
        db_Name.setText(empEnrollInfo.getEmpName().trim());
        db_Aadhaar.setText(empEnrollInfo.getAadhaarId());
        db_BloodGroup.setText(empEnrollInfo.getBloodGroup());
        db_dob.setText(empEnrollInfo.getDateOfBirth());
        db_validity.setText(empEnrollInfo.getValidUpto());
        db_sitecode.setText(empEnrollInfo.getSiteCode());
        db_card_version.setText(empEnrollInfo.getSmartCardVer());

        if (empFingerInfoList != null) {
            int size = empFingerInfoList.size();
            for (int i = 0; i < size; i++) {
                if (i == 0) {
                    db_Fnumber_one.setText("0" + empFingerInfoList.get(i).getTemplateSrNo());
                    db_Findex_one.setText(empFingerInfoList.get(i).getFingerIndex());
                    db_Fquality_one.setText(empFingerInfoList.get(i).getFingerQuality());
                    db_Fdata.setText(empFingerInfoList.get(i).getFingerHexData());
                } else if (i == 1) {
                    db_Fnumber_two.setText("0" + empFingerInfoList.get(i).getTemplateSrNo());
                    db_Findex_two.setText(empFingerInfoList.get(i).getFingerIndex());
                    db_Fquality_two.setText(empFingerInfoList.get(i).getFingerQuality());
                    db_Sdata.setText(empFingerInfoList.get(i).getFingerHexData());
                }
                db_SecurityLevel.setText(empFingerInfoList.get(i).getSecurityLevel());
                db_VerificationMode.setText(empFingerInfoList.get(i).getVerificationMode());
            }
        }
    }

    public void setMessageForCardRead(SmartCardInfo cardDetails) {

        ViewGroup.LayoutParams paramsEmpDetails = tbl_CardDetails.getLayoutParams();
        paramsEmpDetails.width = ViewGroup.LayoutParams.MATCH_PARENT;   //Modified By Sanjay Shyamal On 27/11/17
        paramsEmpDetails.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        tbl_CardDetails.setLayoutParams(paramsEmpDetails);
        tbl_CardDetails.setVisibility(View.VISIBLE);

        String value = cardDetails.getReadCSN();
        if (value != null) {
            db_CardSerial.setText(value.trim());
        }
        value = cardDetails.getEmployeeId();
        if (value != null) {
            db_EmpID.setText(value.trim());
        }
        value = cardDetails.getCardId();
        if (value != null) {
            db_CardID.setText(value.replaceAll("\\G0", " ").trim());
        }
        value = cardDetails.getEmpName();
        if (value != null) {
            db_Name.setText(value.trim());
        }
        db_Aadhaar.setText("");//aadhaar Id will be written in sector 3 in future
        value = cardDetails.getBloodGroup();
        if (value != null) {
            db_BloodGroup.setText(value.trim());
        }
        value = cardDetails.getBirthDate();
        if (value != null) {
            db_dob.setText(value.trim());
        }
        value = cardDetails.getValidUpto();
        if (value != null) {
            db_validity.setText(value.trim());
        }
        value = cardDetails.getSiteCode();
        if (value != null) {
            db_sitecode.setText(value.trim());
        }
        value = cardDetails.getSmartCardVer();
        if (value != null) {
            db_card_version.setText(value.trim());
        }
        value = cardDetails.getFirstFingerNo();
        if (value != null) {
            db_Fnumber_one.setText(value.trim());
        }
        value = cardDetails.getFirstFingerIndex();
        if (value != null) {
            db_Findex_one.setText(value.trim());
        }
        value = cardDetails.getFirstFingerQuality();
        if (value != null) {
            db_Fquality_one.setText(value.trim());
        }
        value = cardDetails.getSecondFingerNo();
        if (value != null) {
            db_Fnumber_two.setText(value.trim());
        }
        value = cardDetails.getSecondFingerIndex();
        if (value != null) {
            db_Findex_two.setText(value.trim());
        }
        value = cardDetails.getSecondFingerQuality();
        if (value != null) {
            db_Fquality_two.setText(value.trim());
        }
        value = cardDetails.getFirstFingerSecurityLevel();
        if (value != null) {
            db_SecurityLevel.setText(value.trim());
        }
        value = cardDetails.getFirstFingerVerificationMode();
        if (value != null) {
            db_VerificationMode.setText(value.trim());
        }
    }

    //==================================  Encode Data for writing into card  ==============================================//

    public SmartCardInfo EncodeDataBeforeCardWrite(SmartCardInfo cardInfo) {

        try {
            String value = db_EmpID.getText().toString();
            value = Utility.paddEmpId(value);
            cardInfo.setEmployeeId(value);

            value = db_CardID.getText().toString();
            value = Utility.paddCardId(value);
            cardInfo.setCardId(value);

            value = db_CardSerial.getText().toString();
            if (value.trim().length() > 0) {
                cardInfo.setDbCSN(value);
            } else {
                cardInfo.setDbCSN(cardInfo.getReadCSN());
            }


            value = db_Name.getText().toString();
            value = Utility.paddEmpName(value);
            cardInfo.setEmpName(value);

            value = db_validity.getText().toString();
            String strValidUptoForCard = "";
            if (value.length() > 0) {
                if (value.length() == 10) {
                    strValidUptoForCard = value.replaceAll("-", "").trim();
                    String strDateMonth = strValidUptoForCard.substring(0, 4);
                    String strYear = strValidUptoForCard.substring(6);
                    strValidUptoForCard = strDateMonth + strYear;
                } else {
                    strValidUptoForCard = value;
                }
            } else {
                strValidUptoForCard = "000000";
            }
            cardInfo.setValidUpto(strValidUptoForCard);


            value = db_dob.getText().toString();
            String strBirthDayForCard = "";
            if (value.length() > 0) {
                if (value.length() == 10) {
                    strBirthDayForCard = value.replaceAll("-", "").trim();
                    String strDateMonth = strBirthDayForCard.substring(0, 4);
                    String strYear = strBirthDayForCard.substring(6);
                    strBirthDayForCard = strDateMonth + strYear;
                } else {
                    strBirthDayForCard = value;
                }
            } else {
                strBirthDayForCard = "000000";
            }

            cardInfo.setBirthDate(strBirthDayForCard);

            cardInfo.setFirstFingerQuality(Constants.FIRST_FINGER_QUALITY);
            cardInfo.setSecondFingerQuality(Constants.SECOND_FINGER_QUALITY);

            value = db_BloodGroup.getText().toString();
            String bloodGroup = "0";
            if (value.trim().length() > 0) {
                for (int j = 0; j < 11; j++) {
                    if (value.equals(Constants.BLOOD_GROUPS[j])) {
                        bloodGroup = Integer.toString(j);
                    }
                }
            } else {
                bloodGroup = "0";
            }
            cardInfo.setBloodGroup(bloodGroup);

            value = db_sitecode.getText().toString();
            if (value.trim().length() > 0) {
                cardInfo.setSiteCode(value);
            } else {
                cardInfo.setSiteCode("00");
            }


            value = db_card_version.getText().toString();
            if (value.trim().length() > 0) {
                cardInfo.setSmartCardVer(value);
            } else {
                cardInfo.setSmartCardVer("0");
            }

            value = db_Findex_one.getText().toString();
            String firstFingerIndex = "";
            if (value.trim().length() > 0) {
                for (int j = 0; j < 11; j++) {
                    if (value.equals(Constants.FINGER_INDEXES[j])) {
                        firstFingerIndex = Integer.toString(j);
                        if (firstFingerIndex.trim().equals("10")) {
                            firstFingerIndex = "A";
                        }
                    }
                }
            }
            cardInfo.setFirstFingerIndex(firstFingerIndex);
            cardInfo.setFirstFingerNo("01");


            value = db_SecurityLevel.getText().toString();
            String securityLevel = "";
            if (value.trim().length() > 0) {
                for (int j = 0; j < 11; j++) {
                    if (value.equals(Constants.SECURITY_LEVELS[j])) {
                        securityLevel = Integer.toString(j);
                    }
                }
            }
            cardInfo.setFirstFingerSecurityLevel(securityLevel);
            cardInfo.setSecondFingerSecurityLevel(securityLevel);

            value = db_VerificationMode.getText().toString();
            String verificationMode = "";
            if (value.trim().length() > 0) {
                for (int j = 0; j < 11; j++) {
                    if (value.equals(Constants.VERIFICATION_MODES[j])) {
                        verificationMode = Integer.toString(j);
                    }
                }
            }
            cardInfo.setFirstFingerVerificationMode(verificationMode);
            cardInfo.setSecondFingerVerificationMode(verificationMode);

            value = db_Findex_two.getText().toString();
            String secondFingerIndex = "";
            if (value.trim().length() > 0) {
                for (int j = 0; j < 11; j++) {
                    if (value.equals(Constants.FINGER_INDEXES[j])) {
                        secondFingerIndex = Integer.toString(j);
                        if (secondFingerIndex.trim().equals("10")) {
                            secondFingerIndex = "A";
                        }
                    }
                }
            }
            cardInfo.setSecondFingerIndex(secondFingerIndex);
            cardInfo.setSecondFingerNo("02");

            int noOfTemplates = 0;

            value = db_Fdata.getText().toString();
            int len = value.trim().length();
            if (len > 0 && len == Constants.TEMPLATE_SIZE) {
                cardInfo.setFirstSlot(value.substring(0, 96));
                cardInfo.setSecondSlot(value.substring(96, 192));
                cardInfo.setThirdSlot(value.substring(192, 288));
                cardInfo.setForthSlot(value.substring(288, 384));
                cardInfo.setFifthSlot(value.substring(384, 480));
                cardInfo.setSixthSlot(value.substring(480, 512));
                noOfTemplates++;
            }

            value = db_Sdata.getText().toString();
            len = value.trim().length();
            if (len > 0 && len == Constants.TEMPLATE_SIZE) {
                cardInfo.setSeventhSlot(value.substring(0, 96));
                cardInfo.setEighthSlot(value.substring(96, 192));
                cardInfo.setNinethSlot(value.substring(192, 288));
                cardInfo.setTenthSlot(value.substring(288, 384));
                cardInfo.setEleventhSlot(value.substring(384, 480));
                cardInfo.setTwelvethSlot(value.substring(480, 512));
                noOfTemplates++;
            }

            cardInfo.setNoOfTemplates(noOfTemplates);

//        while (strAadhaarId.length() < 16) {
//            strAadhaarId = " " + strAadhaarId; // AadhaarID must be 16 char
//        }

        } catch (SQLiteException e) {
            Toast.makeText(SmartCardActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        return cardInfo;
    }

    //===================================== Async Task USB Card Read ===================================//

    private class AsyncTaskUsbCardRead extends AsyncTask <String, Void, Boolean> {

        ProgressDialog mypDialog;
        MicroSmartV2Communicator mSmartV2Comm;
        SmartCardInfo cardDetails = null;

        AsyncTaskUsbCardRead(MicroSmartV2Communicator mSmartV2Comm) {
            this.mSmartV2Comm = mSmartV2Comm;
        }

        @Override
        protected void onPreExecute() {
            mypDialog = new ProgressDialog(SmartCardActivity.this);
            mypDialog.setMessage("Card Reading Wait...");
            mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mypDialog.setCanceledOnTouchOutside(false);
            mypDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {

            Boolean readStatus = false;

            //======================  Read Sector 0 for Mad Block For Number of Templates Present  =================================//

            String command = Utility.addCheckSum(Constants.READ_CSN_COMMAND);
            byte[] sector_0_Data = mSmartV2Comm.readSector(0, command.getBytes());
            if (sector_0_Data != null) {
                cardDetails = new SmartCardInfo();
                boolean parseStatus = mSmartV2Comm.parseSectorData(0, sector_0_Data, cardDetails);
                if (parseStatus) {
                    String madOne = cardDetails.getMadOne();
                    if (madOne.equals(Constants.MAD_ONE_DATA_FOR_TWO_TEMPLATES)) {
                        Cursor sectorKeyData = null;
                        sectorKeyData = dbComm.getSectorAndKeyForReadCard();
                        if (sectorKeyData != null) {
                            command = "";
                            while (sectorKeyData.moveToNext()) {
                                String strSectorNo = sectorKeyData.getString(0);
                                String strKey = sectorKeyData.getString(1);
                                int sectorNo = Integer.parseInt(strSectorNo);
                                switch (sectorNo) {

                                    case 1:// Sector 1(Read Card Id)

                                        command = Utility.addCheckSum(Constants.READ_CARD_ID_COMMAND);
                                        String strCardId = mSmartV2Comm.readCardId(command.getBytes());
                                        cardDetails.setCardId(strCardId);

                                        break;

                                    case 2:   // Sector 2
                                        command = Constants.SECTOR_READ_COMM[0] + Constants.KEY_B + strKey + Constants.ASCII_READ_WRITE;
                                        command = Utility.addCheckSum(command);
                                        byte[] sector_2_Data = mSmartV2Comm.readSector(2, command.getBytes());
                                        if (sector_2_Data != null) {
                                            readStatus = mSmartV2Comm.parseSectorData(2, sector_2_Data, cardDetails);
                                        }
                                        break;
                                    case 4:   // Sector 4
                                        command = Constants.SECTOR_READ_COMM[1] + Constants.KEY_B + strKey + Constants.HEX_READ_WRITE;
                                        command = Utility.addCheckSum(command);
                                        byte[] sector_4_Data = mSmartV2Comm.readSector(4, command.getBytes());
                                        if (sector_4_Data != null) {
                                            readStatus = mSmartV2Comm.parseSectorData(4, sector_4_Data, cardDetails);
                                        }
                                        break;
                                    case 5:          // Sector 5
                                        command = Constants.SECTOR_READ_COMM[2] + Constants.KEY_B + strKey + Constants.HEX_READ_WRITE;
                                        command = Utility.addCheckSum(command);
                                        byte[] sector_5_Data = mSmartV2Comm.readSector(5, command.getBytes());
                                        if (sector_5_Data != null) {
                                            readStatus = mSmartV2Comm.parseSectorData(5, sector_5_Data, cardDetails);
                                        }
                                        break;
                                    case 6:      // Sector 6
                                        command = Constants.SECTOR_READ_COMM[3] + Constants.KEY_B + strKey + Constants.HEX_READ_WRITE;
                                        command = Utility.addCheckSum(command);
                                        byte[] sector_6_Data = mSmartV2Comm.readSector(6, command.getBytes());
                                        if (sector_6_Data != null) {
                                            readStatus = mSmartV2Comm.parseSectorData(6, sector_6_Data, cardDetails);
                                        }
                                        break;
                                    case 7:      // Sector 7
                                        command = Constants.SECTOR_READ_COMM[4] + Constants.KEY_B + strKey + Constants.HEX_READ_WRITE;
                                        command = Utility.addCheckSum(command);
                                        byte[] sector_7_Data = mSmartV2Comm.readSector(7, command.getBytes());
                                        if (sector_7_Data != null) {
                                            readStatus = mSmartV2Comm.parseSectorData(7, sector_7_Data, cardDetails);
                                        }
                                        break;
                                    case 8:   // Sector 8
                                        command = Constants.SECTOR_READ_COMM[5] + Constants.KEY_B + strKey + Constants.HEX_READ_WRITE;
                                        command = Utility.addCheckSum(command);
                                        byte[] sector_8_Data = mSmartV2Comm.readSector(8, command.getBytes());
                                        if (sector_8_Data != null) {
                                            readStatus = mSmartV2Comm.parseSectorData(8, sector_8_Data, cardDetails);
                                        }
                                        break;
                                    case 9:      // Sector 9
                                        command = Constants.SECTOR_READ_COMM[6] + Constants.KEY_B + strKey + Constants.HEX_READ_WRITE;
                                        command = Utility.addCheckSum(command);
                                        byte[] sector_9_Data = mSmartV2Comm.readSector(9, command.getBytes());
                                        if (sector_9_Data != null) {
                                            readStatus = mSmartV2Comm.parseSectorData(9, sector_9_Data, cardDetails);
                                        }
                                        break;
                                    case 10:     // Sector A
                                        command = Constants.SECTOR_READ_COMM[7] + Constants.KEY_B + strKey + Constants.HEX_READ_WRITE;
                                        command = Utility.addCheckSum(command);
                                        byte[] sector_10_Data = mSmartV2Comm.readSector(10, command.getBytes());
                                        if (sector_10_Data != null) {
                                            readStatus = mSmartV2Comm.parseSectorData(10, sector_10_Data, cardDetails);
                                        }
                                        break;
                                    case 11:     // Sector B
                                        command = Constants.SECTOR_READ_COMM[8] + Constants.KEY_B + strKey + Constants.HEX_READ_WRITE;
                                        command = Utility.addCheckSum(command);
                                        byte[] sector_11_Data = mSmartV2Comm.readSector(11, command.getBytes());
                                        if (sector_11_Data != null) {
                                            readStatus = mSmartV2Comm.parseSectorData(11, sector_11_Data, cardDetails);
                                        }
                                        break;
                                    case 12:     // Sector C
                                        command = Constants.SECTOR_READ_COMM[9] + Constants.KEY_B + strKey + Constants.HEX_READ_WRITE;
                                        command = Utility.addCheckSum(command);
                                        byte[] sector_12_Data = mSmartV2Comm.readSector(12, command.getBytes());
                                        if (sector_12_Data != null) {
                                            readStatus = mSmartV2Comm.parseSectorData(12, sector_12_Data, cardDetails);
                                        }
                                        break;
                                    case 13:     // Sector D
                                        command = Constants.SECTOR_READ_COMM[10] + Constants.KEY_B + strKey + Constants.HEX_READ_WRITE;
                                        command = Utility.addCheckSum(command);
                                        byte[] sector_13_Data = mSmartV2Comm.readSector(13, command.getBytes());
                                        if (sector_13_Data != null) {
                                            readStatus = mSmartV2Comm.parseSectorData(13, sector_13_Data, cardDetails);
                                        }
                                        break;
                                    case 14:     // Sector E
                                        command = Constants.SECTOR_READ_COMM[11] + Constants.KEY_B + strKey + Constants.HEX_READ_WRITE;
                                        command = Utility.addCheckSum(command);
                                        byte[] sector_14_Data = mSmartV2Comm.readSector(14, command.getBytes());
                                        if (sector_14_Data != null) {
                                            readStatus = mSmartV2Comm.parseSectorData(14, sector_14_Data, cardDetails);
                                        }
                                        break;
                                    case 15:     // Sector F
                                        command = Constants.SECTOR_READ_COMM[12] + Constants.KEY_B + strKey + Constants.HEX_READ_WRITE;
                                        command = Utility.addCheckSum(command);
                                        byte[] sector_15_Data = mSmartV2Comm.readSector(15, command.getBytes());
                                        if (sector_15_Data != null) {
                                            readStatus = mSmartV2Comm.parseSectorData(15, sector_15_Data, cardDetails);
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
                    } else if (madOne.equals(Constants.MAD_ONE_DATA_FOR_ONE_TEMPLATE)) {

                        Cursor sectorKeyData = null;
                        sectorKeyData = dbComm.getSectorAndKeyForReadCard();
                        if (sectorKeyData != null) {
                            command = "";
                            while (sectorKeyData.moveToNext()) {
                                String strSectorNo = sectorKeyData.getString(0);
                                String strKey = sectorKeyData.getString(1);
                                int sectorNo = Integer.parseInt(strSectorNo);
                                switch (sectorNo) {

                                    case 1: // Sector 1(Read Card Id)

                                        command = Utility.addCheckSum(Constants.READ_CARD_ID_COMMAND);
                                        String strCardId = mSmartV2Comm.readCardId(command.getBytes());
                                        cardDetails.setCardId(strCardId);

                                        break;

                                    case 2:   // Sector 2
                                        command = Constants.SECTOR_READ_COMM[0] + Constants.KEY_B + strKey + Constants.ASCII_READ_WRITE;
                                        command = Utility.addCheckSum(command);
                                        byte[] sector_2_Data = mSmartV2Comm.readSector(2, command.getBytes());
                                        if (sector_2_Data != null) {
                                            readStatus = mSmartV2Comm.parseSectorData(2, sector_2_Data, cardDetails);
                                        }
                                        break;
                                    case 4:   // Sector 4
                                        command = Constants.SECTOR_READ_COMM[1] + Constants.KEY_B + strKey + Constants.HEX_READ_WRITE;
                                        command = Utility.addCheckSum(command);
                                        byte[] sector_4_Data = mSmartV2Comm.readSector(4, command.getBytes());
                                        if (sector_4_Data != null) {
                                            readStatus = mSmartV2Comm.parseSectorData(4, sector_4_Data, cardDetails);
                                        }
                                        break;
                                    case 5:          // Sector 5
                                        command = Constants.SECTOR_READ_COMM[2] + Constants.KEY_B + strKey + Constants.HEX_READ_WRITE;
                                        command = Utility.addCheckSum(command);
                                        byte[] sector_5_Data = mSmartV2Comm.readSector(5, command.getBytes());
                                        if (sector_5_Data != null) {
                                            readStatus = mSmartV2Comm.parseSectorData(5, sector_5_Data, cardDetails);
                                        }
                                        break;
                                    case 6:      // Sector 6
                                        command = Constants.SECTOR_READ_COMM[3] + Constants.KEY_B + strKey + Constants.HEX_READ_WRITE;
                                        command = Utility.addCheckSum(command);
                                        byte[] sector_6_Data = mSmartV2Comm.readSector(6, command.getBytes());
                                        if (sector_6_Data != null) {
                                            readStatus = mSmartV2Comm.parseSectorData(6, sector_6_Data, cardDetails);
                                        }
                                        break;
                                    case 7:      // Sector 7
                                        command = Constants.SECTOR_READ_COMM[4] + Constants.KEY_B + strKey + Constants.HEX_READ_WRITE;
                                        command = Utility.addCheckSum(command);
                                        byte[] sector_7_Data = mSmartV2Comm.readSector(7, command.getBytes());
                                        if (sector_7_Data != null) {
                                            readStatus = mSmartV2Comm.parseSectorData(7, sector_7_Data, cardDetails);
                                        }
                                        break;
                                    case 8:   // Sector 8
                                        command = Constants.SECTOR_READ_COMM[5] + Constants.KEY_B + strKey + Constants.HEX_READ_WRITE;
                                        command = Utility.addCheckSum(command);
                                        byte[] sector_8_Data = mSmartV2Comm.readSector(8, command.getBytes());
                                        if (sector_8_Data != null) {
                                            readStatus = mSmartV2Comm.parseSectorData(8, sector_8_Data, cardDetails);
                                        }
                                        break;
                                    case 9:      // Sector 9
                                        command = Constants.SECTOR_READ_COMM[6] + Constants.KEY_B + strKey + Constants.HEX_READ_WRITE;
                                        command = Utility.addCheckSum(command);
                                        byte[] sector_9_Data = mSmartV2Comm.readSector(9, command.getBytes());
                                        if (sector_9_Data != null) {
                                            readStatus = mSmartV2Comm.parseSectorData(9, sector_9_Data, cardDetails);
                                        }
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
                    } else {
                        readStatus = false;
                    }
                }
            }
            return readStatus;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            mypDialog.cancel();
            if (result) {
                if (cardDetails != null) {
                    setMessageForCardRead(cardDetails);
                }
            } else {
                showCustomAlertDialog(false, "Card Read Status", "Invalid Card ! Read Card Again");
            }
        }
    }

    //===================================== Async Task SPI Card Read ===================================//

    private class AsyncTaskSpiCardRead extends AsyncTask <Void, Void, Integer> {

        ProgressDialog mypDialog;

        @Override
        protected void onPreExecute() {
            mypDialog = new ProgressDialog(SmartCardActivity.this);
            mypDialog.setMessage("Card Reading Wait...");
            mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mypDialog.setCanceledOnTouchOutside(false);
            mypDialog.show();
        }

        @Override
        protected Integer doInBackground(Void... voids) {

            int error = -1;

            Cursor sectorKeyData = dbComm.getSectorAndKeyForRC632CardRead();
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
                                //  parseSector_1(cardReadData);
                            }

                            break;

                        case 2:   // Sector 2

                            cardReadData = new byte[48];
                            keyB = strKey.getBytes();
                            error = rc632ReaderConnection.sectorRead(keyFlag, keyB, (byte) sectorNo, cardReadData);
                            if (error == 0) {
                                //parseSector_2(cardReadData);
                            }

                            break;

                        case 4:   // Sector 4

                            cardReadData = new byte[48];
                            keyB = strKey.getBytes();
                            error = rc632ReaderConnection.sectorRead(keyFlag, keyB, (byte) sectorNo, cardReadData);
                            if (error == 0) {
                                //parseSector_4_8(cardReadData);
                            }

                            break;

                        case 5:          // Sector 5

                            cardReadData = new byte[48];
                            keyB = strKey.getBytes();
                            error = rc632ReaderConnection.sectorRead(keyFlag, keyB, (byte) sectorNo, cardReadData);
                            if (error == 0) {
                                //parseSector_4_8(cardReadData);
                            }
                            break;

                        case 6:      // Sector 6

                            cardReadData = new byte[48];
                            keyB = strKey.getBytes();
                            error = rc632ReaderConnection.sectorRead(keyFlag, keyB, (byte) sectorNo, cardReadData);
                            if (error == 0) {
                                //parseSector_4_8(cardReadData);
                            }

                            break;

                        case 7:      // Sector 7

                            cardReadData = new byte[48];
                            keyB = strKey.getBytes();
                            error = rc632ReaderConnection.sectorRead(keyFlag, keyB, (byte) sectorNo, cardReadData);
                            if (error == 0) {
                                //parseSector_4_8(cardReadData);
                            }

                            break;

                        case 8:   // Sector 8

                            cardReadData = new byte[48];
                            keyB = strKey.getBytes();
                            error = rc632ReaderConnection.sectorRead(keyFlag, keyB, (byte) sectorNo, cardReadData);
                            if (error == 0) {
                                //parseSector_4_8(cardReadData);
                            }

                            break;

                        case 9:      // Sector 9

                            cardReadData = new byte[48];
                            keyB = strKey.getBytes();
                            error = rc632ReaderConnection.sectorRead(keyFlag, keyB, (byte) sectorNo, cardReadData);
                            if (error == 0) {
                                //parseSector_9(cardReadData);
                            }

                            break;

                        case 10:     // Sector A

                            cardReadData = new byte[48];
                            keyB = strKey.getBytes();
                            error = rc632ReaderConnection.sectorRead(keyFlag, keyB, (byte) sectorNo, cardReadData);
                            if (error == 0) {
                                //parseSector_10_14(cardReadData);
                            }

                            break;

                        case 11:     // Sector B

                            cardReadData = new byte[48];
                            keyB = strKey.getBytes();
                            error = rc632ReaderConnection.sectorRead(keyFlag, keyB, (byte) sectorNo, cardReadData);
                            if (error == 0) {
                                //parseSector_10_14(cardReadData);
                            }

                            break;

                        case 12:     // Sector C

                            cardReadData = new byte[48];
                            keyB = strKey.getBytes();
                            error = rc632ReaderConnection.sectorRead(keyFlag, keyB, (byte) sectorNo, cardReadData);
                            if (error == 0) {
                                //parseSector_10_14(cardReadData);
                            }

                            break;

                        case 13:     // Sector D

                            cardReadData = new byte[48];
                            keyB = strKey.getBytes();
                            error = rc632ReaderConnection.sectorRead(keyFlag, keyB, (byte) sectorNo, cardReadData);
                            if (error == 0) {
                                //parseSector_10_14(cardReadData);
                            }

                            break;

                        case 14:     // Sector E

                            cardReadData = new byte[48];
                            keyB = strKey.getBytes();
                            error = rc632ReaderConnection.sectorRead(keyFlag, keyB, (byte) sectorNo, cardReadData);
                            if (error == 0) {
                                //parseSector_10_14(cardReadData);
                            }

                            break;

                        case 15:     // Sector F

                            cardReadData = new byte[48];
                            keyB = strKey.getBytes();
                            error = rc632ReaderConnection.sectorRead(keyFlag, keyB, (byte) sectorNo, cardReadData);
                            if (error == 0) {
                                //parseSector_15(cardReadData);
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
                sectorKeyData.close();
            }
            return error;
        }


        @Override
        protected void onPostExecute(Integer result) {
            mypDialog.cancel();
            if (result == 0) {
                //setMessageForCardRead();
            } else {
                showCustomAlertDialog(false, "Card Read Status", "Invalid Card ! Read Card Again");
            }
        }
    }


    private class AsyncTaskRC522CardRead extends AsyncTask <Void, Void, Boolean> {

        ProgressDialog mypDialog;
        RC522Communicator comm;
        SmartCardInfo cardInfo = null;

        AsyncTaskRC522CardRead(RC522Communicator comm) {
            this.comm = comm;
        }

        @Override
        protected void onPreExecute() {
            mypDialog = new ProgressDialog(SmartCardActivity.this);
            mypDialog.setMessage("Card Reading Wait...");
            mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mypDialog.setCanceledOnTouchOutside(false);
            mypDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            boolean parseStatus = false;
            cardInfo = new SmartCardInfo();
            Cursor sectorKeyData = null;
            sectorKeyData = dbComm.getSectorAndKeyForReadCard();
            if (sectorKeyData != null) {
                String wrCommand = "";
                while (sectorKeyData.moveToNext()) {
                    String strSectorNo = sectorKeyData.getString(0);
                    String strKey = sectorKeyData.getString(1);
                    int sectorNo = Integer.parseInt(strSectorNo);
                    boolean status = false;
                    String cardData = "";
                    StringBuffer sb = new StringBuffer();
                    switch (sectorNo) {
                        case 0:
                            parseStatus = false;
                            status = comm.writeRC522(Constants.RC522_READ_CSN_COMMAND);
                            if (status) {
                                char[] data = comm.readRC522();
                                setCSN(data, cardInfo);
                                parseStatus = true;
                            }
                            break;
                        case 1:
                            parseStatus = false;
                            status = comm.writeRC522(Constants.RC522_READ_CARDID_COMMAND);
                            if (status) {
                                char[] readData = comm.readRC522();
                                if (readData != null && readData.length > 0) {
                                    setCardId(readData, cardInfo);
                                    parseStatus = true;
                                }
                            }
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
                                            perInfo = perInfo.toUpperCase();
                                            parseStatus = comm.parseSectorData(2, perInfo.getBytes(), cardInfo);
                                        }
                                    }
                                }
                            }
//                            sb.append("00");
//                            for (int j = 0; j < 3; j++) {
//                                wrCommand = Constants.RC522_READ_COMMAND + " " + Integer.toString(sectorNo * 4 + j) + " " + strKey + " " + Constants.RC522_KEY_TYPE_B;
//                                boolean status = comm.writeRC522(wrCommand);
//                                if (status) {
//                                    char[] data = comm.readRC522();
//                                    if (data != null && data.length > 0) {
//                                        String strData = new String(data);
//                                        String[] arr = strData.split(":");
//                                        if (arr != null && arr.length == 3) {
//                                            sb.append(arr[2].trim());
//                                        }
//                                    }
//                                }
//                            }
//                            String perInfo = Utility.hexToAscii(sb.toString());
//                            perInfo = Utility.removeNonAscii(perInfo);
//                            parseStatus = comm.parseSectorData(2, perInfo.getBytes(), cardInfo);
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
//                            for (int j = 0; j < 3; j++) {
//                                wrCommand = Constants.RC522_READ_COMMAND + " " + Integer.toString(sectorNo * 4 + j) + " " + strKey + " " + Constants.RC522_KEY_TYPE_B;
//                                boolean status = comm.writeRC522(wrCommand);
//                                if (status) {
//                                    char[] data = comm.readRC522();
//                                    if (data != null && data.length > 0) {
//                                        String strData = new String(data);
//                                        String[] arr = strData.split(":");
//                                        if (arr != null && arr.length == 3) {
//                                            sb.append(arr[2].trim());
//                                        }
//                                    }
//                                }
//                            }
//                            parseStatus = comm.parseSectorData(4, sb.toString().getBytes(), cardInfo);
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
//                            sb.append("0");
//                            for (int j = 0; j < 3; j++) {
//                                wrCommand = Constants.RC522_READ_COMMAND + " " + Integer.toString(sectorNo * 4 + j) + " " + strKey + " " + Constants.RC522_KEY_TYPE_B;
//                                boolean status = comm.writeRC522(wrCommand);
//                                if (status) {
//                                    char[] data = comm.readRC522();
//                                    if (data != null && data.length > 0) {
//                                        String strData = new String(data);
//                                        String[] arr = strData.split(":");
//                                        if (arr != null && arr.length == 3) {
//                                            sb.append(arr[2].trim());
//                                        }
//                                    }
//                                }
//                            }
//                            parseStatus = comm.parseSectorData(5, sb.toString().getBytes(), cardInfo);
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
//                            sb.append("0");
//                            for (int j = 0; j < 3; j++) {
//                                wrCommand = Constants.RC522_READ_COMMAND + " " + Integer.toString(sectorNo * 4 + j) + " " + strKey + " " + Constants.RC522_KEY_TYPE_B;
//                                boolean status = comm.writeRC522(wrCommand);
//                                if (status) {
//                                    char[] data = comm.readRC522();
//                                    if (data != null && data.length > 0) {
//                                        String strData = new String(data);
//                                        String[] arr = strData.split(":");
//                                        if (arr != null && arr.length == 3) {
//                                            sb.append(arr[2].trim());
//                                        }
//                                    }
//                                }
//                            }
//                            parseStatus = comm.parseSectorData(6, sb.toString().getBytes(), cardInfo);
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
//                            sb.append("0");
//                            for (int j = 0; j < 3; j++) {
//                                wrCommand = Constants.RC522_READ_COMMAND + " " + Integer.toString(sectorNo * 4 + j) + " " + strKey + " " + Constants.RC522_KEY_TYPE_B;
//                                boolean status = comm.writeRC522(wrCommand);
//                                if (status) {
//                                    char[] data = comm.readRC522();
//                                    if (data != null && data.length > 0) {
//                                        String strData = new String(data);
//                                        String[] arr = strData.split(":");
//                                        if (arr != null && arr.length == 3) {
//                                            sb.append(arr[2].trim());
//                                        }
//                                    }
//                                }
//                            }
//                            parseStatus = comm.parseSectorData(7, sb.toString().getBytes(), cardInfo);
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
//                            sb.append("0");
//                            for (int j = 0; j < 3; j++) {
//                                wrCommand = Constants.RC522_READ_COMMAND + " " + Integer.toString(sectorNo * 4 + j) + " " + strKey + " " + Constants.RC522_KEY_TYPE_B;
//                                boolean status = comm.writeRC522(wrCommand);
//                                if (status) {
//                                    char[] data = comm.readRC522();
//                                    if (data != null && data.length > 0) {
//                                        String strData = new String(data);
//                                        String[] arr = strData.split(":");
//                                        if (arr != null && arr.length == 3) {
//                                            sb.append(arr[2].trim());
//                                        }
//                                    }
//                                }
//                            }
//                            parseStatus = comm.parseSectorData(8, sb.toString().getBytes(), cardInfo);
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
//                            sb.append("0");
//                            for (int j = 0; j < 3; j++) {
//                                wrCommand = Constants.RC522_READ_COMMAND + " " + Integer.toString(sectorNo * 4 + j) + " " + strKey + " " + Constants.RC522_KEY_TYPE_B;
//                                boolean status = comm.writeRC522(wrCommand);
//                                if (status) {
//                                    char[] data = comm.readRC522();
//                                    if (data != null && data.length > 0) {
//                                        String strData = new String(data);
//                                        String[] arr = strData.split(":");
//                                        if (arr != null && arr.length == 3) {
//                                            sb.append(arr[2].trim());
//                                        }
//                                    }
//                                }
//                            }
//                            parseStatus = comm.parseSectorData(9, sb.toString().getBytes(), cardInfo);
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
//                            sb.append("0");
//                            for (int j = 0; j < 3; j++) {
//                                wrCommand = Constants.RC522_READ_COMMAND + " " + Integer.toString(sectorNo * 4 + j) + " " + strKey + " " + Constants.RC522_KEY_TYPE_B;
//                                boolean status = comm.writeRC522(wrCommand);
//                                if (status) {
//                                    char[] data = comm.readRC522();
//                                    if (data != null && data.length > 0) {
//                                        String strData = new String(data);
//                                        String[] arr = strData.split(":");
//                                        if (arr != null && arr.length == 3) {
//                                            sb.append(arr[2].trim());
//                                        }
//                                    }
//                                }
//                            }
//                            parseStatus = comm.parseSectorData(10, sb.toString().getBytes(), cardInfo);
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
//                            sb.append("0");
//                            for (int j = 0; j < 3; j++) {
//                                wrCommand = Constants.RC522_READ_COMMAND + " " + Integer.toString(sectorNo * 4 + j) + " " + strKey + " " + Constants.RC522_KEY_TYPE_B;
//                                boolean status = comm.writeRC522(wrCommand);
//                                if (status) {
//                                    char[] data = comm.readRC522();
//                                    if (data != null && data.length > 0) {
//                                        String strData = new String(data);
//                                        String[] arr = strData.split(":");
//                                        if (arr != null && arr.length == 3) {
//                                            sb.append(arr[2].trim());
//                                        }
//                                    }
//                                }
//                            }
//                            parseStatus = comm.parseSectorData(11, sb.toString().getBytes(), cardInfo);
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
//                            sb.append("0");
//                            for (int j = 0; j < 3; j++) {
//                                wrCommand = Constants.RC522_READ_COMMAND + " " + Integer.toString(sectorNo * 4 + j) + " " + strKey + " " + Constants.RC522_KEY_TYPE_B;
//                                boolean status = comm.writeRC522(wrCommand);
//                                if (status) {
//                                    char[] data = comm.readRC522();
//                                    if (data != null && data.length > 0) {
//                                        String strData = new String(data);
//                                        String[] arr = strData.split(":");
//                                        if (arr != null && arr.length == 3) {
//                                            sb.append(arr[2].trim());
//                                        }
//                                    }
//                                }
//                            }
//                            parseStatus = comm.parseSectorData(12, sb.toString().getBytes(), cardInfo);
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
//                            sb.append("0");
//                            for (int j = 0; j < 3; j++) {
//                                wrCommand = Constants.RC522_READ_COMMAND + " " + Integer.toString(sectorNo * 4 + j) + " " + strKey + " " + Constants.RC522_KEY_TYPE_B;
//                                boolean status = comm.writeRC522(wrCommand);
//                                if (status) {
//                                    char[] data = comm.readRC522();
//                                    if (data != null && data.length > 0) {
//                                        String strData = new String(data);
//                                        String[] arr = strData.split(":");
//                                        if (arr != null && arr.length == 3) {
//                                            sb.append(arr[2].trim());
//                                        }
//                                    }
//                                }
//                            }
//                            parseStatus = comm.parseSectorData(13, sb.toString().getBytes(), cardInfo);
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
//                            sb.append("0");
//                            for (int j = 0; j < 3; j++) {
//                                wrCommand = Constants.RC522_READ_COMMAND + " " + Integer.toString(sectorNo * 4 + j) + " " + strKey + " " + Constants.RC522_KEY_TYPE_B;
//                                boolean status = comm.writeRC522(wrCommand);
//                                if (status) {
//                                    char[] data = comm.readRC522();
//                                    if (data != null && data.length > 0) {
//                                        String strData = new String(data);
//                                        String[] arr = strData.split(":");
//                                        if (arr != null && arr.length == 3) {
//                                            sb.append(arr[2].trim());
//                                        }
//                                    }
//                                }
//                            }
//                            parseStatus = comm.parseSectorData(14, sb.toString().getBytes(), cardInfo);
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
//                            sb.append("0");
//                            for (int j = 0; j < 3; j++) {
//                                wrCommand = Constants.RC522_READ_COMMAND + " " + Integer.toString(sectorNo * 4 + j) + " " + strKey + " " + Constants.RC522_KEY_TYPE_B;
//                                boolean status = comm.writeRC522(wrCommand);
//                                if (status) {
//                                    char[] data = comm.readRC522();
//                                    if (data != null && data.length > 0) {
//                                        String strData = new String(data);
//                                        String[] arr = strData.split(":");
//                                        if (arr != null && arr.length == 3) {
//                                            sb.append(arr[2].trim());
//                                        }
//                                    }
//                                }
//                            }
//                            parseStatus = comm.parseSectorData(15, sb.toString().getBytes(), cardInfo);
                            break;
                    }
                    if (!parseStatus) {
                        break;
                    }
                }
                sectorKeyData.close();
            }
            return parseStatus;
        }

        @Override
        protected void onPostExecute(Boolean status) {
            mypDialog.cancel();
            btn_CardRead.setEnabled(true);
            if (status) {
                setMessageForCardRead(cardInfo);
            } else {
                showCustomAlertDialog(false, "Card Read Status", "Invalid Card ! Read Card Again");
            }
        }
    }


    //===================================== Async Task USB Card Write And Key Update ===================================//

    private class AsyncTaskUsbCardWriteAndKeyUpdate extends AsyncTask <Void, Void, Integer> {

        ProgressDialog mypDialog;
        String strLog = "";
        int dataWriteStatus = -1;
        MicroSmartV2Communicator mSmartV2Comm;
        SmartCardInfo cardInfo;

        AsyncTaskUsbCardWriteAndKeyUpdate(MicroSmartV2Communicator mSmartV2Comm, SmartCardInfo cardInfo) {
            this.mSmartV2Comm = mSmartV2Comm;
            this.cardInfo = cardInfo;
        }

        @Override
        protected void onPreExecute() {
            mypDialog = new ProgressDialog(SmartCardActivity.this);
            mypDialog.setMessage("Card Writing Wait...");
            mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mypDialog.setCanceledOnTouchOutside(false);
            mypDialog.show();
        }


        @Override
        protected Integer doInBackground(Void... params) {
            int status = -1;
            String command = "";
            String cardId = cardInfo.getCardId();
            if (cardId != null && cardId.trim().length() > 0) {
                command = Utility.addCheckSum(Constants.WRITE_CARD_ID_COMMAND + cardId);
                status = mSmartV2Comm.writeCardId(command.getBytes());
            }
            if (status == -1) {
                strLog += "Failed to write card id";
                return status;
            } else {
                strLog += "Card id write successful";
            }

            int noOfTemplates = cardInfo.getNoOfTemplates();
            if (noOfTemplates == 1) {
                String strEmptyData = "000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
                Cursor sectorKeyData = dbComm.getSectorAndKeyForWriteCard();
                if (sectorKeyData != null) {
                    String strFinalKeyA = "303132333435";
                    String strInitialKeyB = "353433323130";
                    String strAccessCode = "7F0788C1";
                    String dummy = "00000000000000000000000000000000000000000000000000000000"; //dummy string for writing Sector 9 & F
                    String msg = "";
                    while (sectorKeyData.moveToNext()) {
                        String strSectorNo = sectorKeyData.getString(0).trim();
                        String strFinalKeyB = sectorKeyData.getString(2).trim();
                        int sectorNo = Integer.parseInt(strSectorNo);

                        switch (sectorNo) {

                            case 2:
                                msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[0] + strInitialKeyB + Constants.ASCII_READ_WRITE + cardInfo.getEmployeeId() + cardInfo.getEmpName() + cardInfo.getValidUpto() + cardInfo.getBirthDate() + cardInfo.getBloodGroup() + cardInfo.getSmartCardVer());
                                dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                                if (dataWriteStatus != -1) {
                                    strLog += "Write Successfull To Sector 2\n";
                                    dataWriteStatus = mSmartV2Comm.keyUpdate(11, strFinalKeyA, strAccessCode, strFinalKeyB);
                                    if (dataWriteStatus != -1) {
                                        strLog += "Sector 2 Key Updated Successfully\n";
                                    } else {
                                        strLog += "Sector 2 Key Updation Failure\n";
                                    }
                                } else {
                                    strLog += "Failed To Write To Sector 2\n";
                                }
                                break;

//                            case 3:
//                                msg = Add + sec[1] + strInitialKeyB + "01" + strAadhaarId;
//                                a2 = msg.toCharArray();
//                                checksum = 0;
//                                for (i = 0; i < a2.length; i++) {
//                                    checksum ^= a2[i];
//                                }
//                                c2 = checksum;
//                                if(Integer.toHexString(c2).toUpperCase().length() != 2) {
//                                    msg = msg + "0" + Integer.toHexString(c2).toUpperCase() + "%";
//                                }else {
//                                    msg = msg + Integer.toHexString(c2).toUpperCase() + "%";
//                                }
//
//                                dataWriteStatus=Sector_3_Write(msg, "3");
//
//                                if(dataWriteStatus!=-1) {
//
//                                    strLog+="Write Successfull To Sector 3\n";
//
//                                    keyWriteStatus=CardKeyUpdate(3,15,strFinalKeyA,strAccessCode,strFinalKeyB);
//
//                                    if(keyWriteStatus!=-1) {
//
//                                        strLog+="Sector 3 Key Updated Successfully\n";
//                                    }
//                                    else {
//
//                                        strLog+="Sector 3 Key Updation Failure\n";
//                                    }
//                                }
//                                else {
//
//                                    strLog+="Failed To Write To Sector 3\n";
//                                }
//                                break;

                            case 4:
                                msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[2] + strInitialKeyB + Constants.HEX_READ_WRITE + cardInfo.getFirstSlot());
                                dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                                if (dataWriteStatus != -1) {
                                    strLog += "Write Successfull To Sector 4\n";
                                    dataWriteStatus = mSmartV2Comm.keyUpdate(19, strFinalKeyA, strAccessCode, strFinalKeyB);
                                    if (dataWriteStatus != -1) {
                                        strLog += "Sector 4 Key Updated Successfully\n";
                                    } else {
                                        strLog += "Sector 4 Key Updation Failure\n";
                                    }
                                } else {
                                    strLog += "Failed To Write To Sector 4\n";
                                }
                                break;
                            case 5:
                                msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[3] + strInitialKeyB + Constants.HEX_READ_WRITE + cardInfo.getSecondSlot());
                                dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                                if (dataWriteStatus != -1) {
                                    strLog += "Write Successfull To Sector 5\n";
                                    dataWriteStatus = mSmartV2Comm.keyUpdate(23, strFinalKeyA, strAccessCode, strFinalKeyB);
                                    if (dataWriteStatus != -1) {
                                        strLog += "Sector 5 Key Updated Successfully\n";
                                    } else {
                                        strLog += "Sector 5 Key Updation Failure\n";
                                    }
                                } else {
                                    strLog += "Failed To Write To Sector 5\n";
                                }
                                break;
                            case 6:
                                msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[4] + strInitialKeyB + Constants.HEX_READ_WRITE + cardInfo.getThirdSlot());
                                dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                                if (dataWriteStatus != -1) {
                                    strLog += "Write Successfull To Sector 6\n";
                                    dataWriteStatus = mSmartV2Comm.keyUpdate(27, strFinalKeyA, strAccessCode, strFinalKeyB);
                                    if (dataWriteStatus != -1) {
                                        strLog += "Sector 6 Key Updated Successfully\n";
                                    } else {
                                        strLog += "Sector 6 Key Updation Failure\n";
                                    }
                                } else {
                                    strLog += "Failed To Write To Sector 6\n";
                                }
                                break;
                            case 7:
                                msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[5] + strInitialKeyB + Constants.HEX_READ_WRITE + cardInfo.getForthSlot());
                                dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                                if (dataWriteStatus != -1) {
                                    strLog += "Write Successfull To Sector 7\n";
                                    dataWriteStatus = mSmartV2Comm.keyUpdate(31, strFinalKeyA, strAccessCode, strFinalKeyB);
                                    if (dataWriteStatus != -1) {
                                        strLog += "Sector 7 Key Updated Successfully\n";
                                    } else {
                                        strLog += "Sector 7 Key Updation Failure\n";
                                    }
                                } else {
                                    strLog += "Failed To Write To Sector 7\n";
                                }
                                break;
                            case 8:
                                msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[6] + strInitialKeyB + Constants.HEX_READ_WRITE + cardInfo.getFifthSlot());
                                dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                                if (dataWriteStatus != -1) {
                                    strLog += "Write Successfull To Sector 8\n";
                                    dataWriteStatus = mSmartV2Comm.keyUpdate(35, strFinalKeyA, strAccessCode, strFinalKeyB);
                                    if (dataWriteStatus != -1) {
                                        strLog += "Sector 8 Key Updated Successfully\n";
                                    } else {
                                        strLog += "Sector 8 Key Updation Failure\n";
                                    }
                                } else {
                                    strLog += "Failed To Write To Sector 8\n";
                                }
                                break;
                            case 9:
                                msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[7] + strInitialKeyB + Constants.HEX_READ_WRITE + cardInfo.getSixthSlot() + cardInfo.getFirstFingerNo() + cardInfo.getFirstFingerSecurityLevel() + cardInfo.getFirstFingerIndex() + cardInfo.getFirstFingerQuality() + "0" + cardInfo.getFirstFingerVerificationMode() + dummy);
                                dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                                if (dataWriteStatus != -1) {
                                    strLog += "Write Successfull To Sector 9\n";
                                    dataWriteStatus = mSmartV2Comm.keyUpdate(39, strFinalKeyA, strAccessCode, strFinalKeyB);
                                    if (dataWriteStatus != -1) {
                                        strLog += "Sector 9 Key Updated Successfully\n";
                                    } else {
                                        strLog += "Sector 9 Key Updation Failure\n";
                                    }
                                } else {
                                    strLog += "Failed To Write To Sector 9\n";
                                }
                                break;
                            case 10:
                                msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[8] + strInitialKeyB + Constants.HEX_READ_WRITE + strEmptyData);
                                dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                                if (dataWriteStatus != -1) {
                                    strLog += "Write Successfull To Sector 10\n";
                                    dataWriteStatus = mSmartV2Comm.keyUpdate(43, strFinalKeyA, strAccessCode, strFinalKeyB);
                                    if (dataWriteStatus != -1) {
                                        strLog += "Sector 10 Key Updated Successfully\n";
                                    } else {
                                        strLog += "Sector 10 Key Updation Failure\n";
                                    }
                                } else {
                                    strLog += "Failed To Write To Sector 10\n";
                                }
                                break;
                            case 11:
                                msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[9] + strInitialKeyB + Constants.HEX_READ_WRITE + strEmptyData);
                                dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                                if (dataWriteStatus != -1) {
                                    strLog += "Write Successfull To Sector 11\n";
                                    dataWriteStatus = mSmartV2Comm.keyUpdate(47, strFinalKeyA, strAccessCode, strFinalKeyB);
                                    if (dataWriteStatus != -1) {
                                        strLog += "Sector 11 Key Updated Successfully\n";
                                    } else {
                                        strLog += "Sector 11 Key Updation Failure\n";
                                    }
                                } else {
                                    strLog += "Failed To Write To Sector 11\n";
                                }
                                break;
                            case 12:
                                msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[10] + strInitialKeyB + Constants.HEX_READ_WRITE + strEmptyData);
                                dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                                if (dataWriteStatus != -1) {
                                    strLog += "Write Successfull To Sector 12\n";
                                    dataWriteStatus = mSmartV2Comm.keyUpdate(51, strFinalKeyA, strAccessCode, strFinalKeyB);
                                    if (dataWriteStatus != -1) {
                                        strLog += "Sector 12 Key Updated Successfully\n";
                                    } else {
                                        strLog += "Sector 12 Key Updation Failure\n";
                                    }
                                } else {
                                    strLog += "Failed To Write To Sector 12\n";
                                }
                                break;
                            case 13:
                                msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[11] + strInitialKeyB + Constants.HEX_READ_WRITE + strEmptyData);
                                dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                                if (dataWriteStatus != -1) {
                                    strLog += "Write Successfull To Sector 13\n";
                                    dataWriteStatus = mSmartV2Comm.keyUpdate(55, strFinalKeyA, strAccessCode, strFinalKeyB);
                                    if (dataWriteStatus != -1) {
                                        strLog += "Sector 13 Key Updated Successfully\n";
                                    } else {
                                        strLog += "Sector 13 Key Updation Failure\n";
                                    }
                                } else {
                                    strLog += "Failed To Write To Sector 13\n";
                                }
                                break;
                            case 14:
                                msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[12] + strInitialKeyB + Constants.HEX_READ_WRITE + strEmptyData);
                                dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                                if (dataWriteStatus != -1) {
                                    strLog += "Write Successfull To Sector 14\n";
                                    dataWriteStatus = mSmartV2Comm.keyUpdate(59, strFinalKeyA, strAccessCode, strFinalKeyB);
                                    if (dataWriteStatus != -1) {
                                        strLog += "Sector 14 Key Updated Successfully\n";
                                    } else {
                                        strLog += "Sector 14 Key Updation Failure\n";
                                    }
                                } else {
                                    strLog += "Failed To Write To Sector 14\n";
                                }
                                break;
                            case 15:
                                msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[13] + strInitialKeyB + Constants.HEX_READ_WRITE + strEmptyData);
                                dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                                if (dataWriteStatus != -1) {
                                    strLog += "Write Successfull To Sector 15\n";
                                    dataWriteStatus = mSmartV2Comm.keyUpdate(63, strFinalKeyA, strAccessCode, strFinalKeyB);
                                    if (dataWriteStatus != -1) {
                                        strLog += "Sector 15 Key Updated Successfully\n";
                                    } else {
                                        strLog += "Sector 15 Key Updation Failure\n";
                                    }
                                } else {
                                    strLog += "Failed To Write To Sector 15\n";
                                }
                                break;
                            default:
                                break;
                        }

                        if (dataWriteStatus != -1) {
                            continue;
                        } else {
                            break;
                        }
                    }

                    if (sectorKeyData != null) {
                        sectorKeyData.close();
                    }

                    //======================================Update Mad Block Of Sector 0=============================================//

                    if (dataWriteStatus != -1) {
                        String strSectorNo = "01A001";
                        int blockno1 = 01;
                        int blockno2 = 02;
                        String strData_01 = "55010005000000004802480248024802";
                        String strData_02 = "48024802000000000000000000000000";
                        dataWriteStatus = mSmartV2Comm.madUpdate(strSectorNo, blockno1, "524553534543", strData_01);
                        if (dataWriteStatus != -1) {
                            dataWriteStatus = mSmartV2Comm.madUpdate(strSectorNo, blockno2, "524553534543", strData_02);
                        }
                    }

                    //===============================================================================================//
                }


            } else if (noOfTemplates == 2) {

                Cursor sectorKeyData = dbComm.getSectorAndKeyForWriteCard();
                if (sectorKeyData != null) {

                    String strFinalKeyA = "303132333435";
                    String strInitialKeyB = "353433323130";
                    String strAccessCode = "7F0788C1";
                    String dummy = "00000000000000000000000000000000000000000000000000000000"; //dummy string for writing Sector 9 & F
                    String msg = "";

                    while (sectorKeyData.moveToNext()) {
                        String strSectorNo = sectorKeyData.getString(0).trim();
                        String strFinalKeyB = sectorKeyData.getString(2).trim();
                        int sectorNo = Integer.parseInt(strSectorNo);

                        switch (sectorNo) {

                            case 2:
                                msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[0] + strInitialKeyB + Constants.ASCII_READ_WRITE + cardInfo.getEmployeeId() + cardInfo.getEmpName() + cardInfo.getValidUpto() + cardInfo.getBirthDate() + cardInfo.getBloodGroup() + cardInfo.getSmartCardVer());
                                dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                                if (dataWriteStatus != -1) {
                                    strLog += "Write Successfull To Sector 2\n";
                                    dataWriteStatus = mSmartV2Comm.keyUpdate(11, strFinalKeyA, strAccessCode, strFinalKeyB);
                                    if (dataWriteStatus != -1) {
                                        strLog += "Sector 2 Key Updated Successfully\n";
                                    } else {
                                        strLog += "Sector 2 Key Updation Failure\n";
                                    }
                                } else {
                                    strLog += "Failed To Write To Sector 2\n";
                                }
                                break;

//                            case 3:
//                                msg = Add + sec[1] + strInitialKeyB + "01" + strAadhaarId;
//                                a2 = msg.toCharArray();
//                                checksum = 0;
//                                for (i = 0; i < a2.length; i++) {
//                                    checksum ^= a2[i];
//                                }
//                                c2 = checksum;
//                                if(Integer.toHexString(c2).toUpperCase().length() != 2) {
//                                    msg = msg + "0" + Integer.toHexString(c2).toUpperCase() + "%";
//                                }else {
//                                    msg = msg + Integer.toHexString(c2).toUpperCase() + "%";
//                                }
//
//                                dataWriteStatus=Sector_3_Write(msg, "3");
//
//                                if(dataWriteStatus!=-1) {
//
//                                    strLog+="Write Successfull To Sector 3\n";
//
//                                    keyWriteStatus=CardKeyUpdate(3,15,strFinalKeyA,strAccessCode,strFinalKeyB);
//
//                                    if(keyWriteStatus!=-1) {
//
//                                        strLog+="Sector 3 Key Updated Successfully\n";
//                                    }
//                                    else {
//
//                                        strLog+="Sector 3 Key Updation Failure\n";
//                                    }
//                                }
//                                else {
//
//                                    strLog+="Failed To Write To Sector 3\n";
//                                }
//                                break;

                            case 4:
                                msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[2] + strInitialKeyB + Constants.HEX_READ_WRITE + cardInfo.getFirstSlot());
                                dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                                if (dataWriteStatus != -1) {
                                    strLog += "Write Successfull To Sector 4\n";
                                    dataWriteStatus = mSmartV2Comm.keyUpdate(19, strFinalKeyA, strAccessCode, strFinalKeyB);
                                    if (dataWriteStatus != -1) {
                                        strLog += "Sector 4 Key Updated Successfully\n";
                                    } else {
                                        strLog += "Sector 4 Key Updation Failure\n";
                                    }
                                } else {
                                    strLog += "Failed To Write To Sector 4\n";
                                }
                                break;
                            case 5:
                                msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[3] + strInitialKeyB + Constants.HEX_READ_WRITE + cardInfo.getSecondSlot());
                                dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                                if (dataWriteStatus != -1) {
                                    strLog += "Write Successfull To Sector 5\n";
                                    dataWriteStatus = mSmartV2Comm.keyUpdate(23, strFinalKeyA, strAccessCode, strFinalKeyB);
                                    if (dataWriteStatus != -1) {
                                        strLog += "Sector 5 Key Updated Successfully\n";
                                    } else {
                                        strLog += "Sector 5 Key Updation Failure\n";
                                    }
                                } else {
                                    strLog += "Failed To Write To Sector 5\n";
                                }
                                break;
                            case 6:
                                msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[4] + strInitialKeyB + Constants.HEX_READ_WRITE + cardInfo.getThirdSlot());
                                dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                                if (dataWriteStatus != -1) {
                                    strLog += "Write Successfull To Sector 6\n";
                                    dataWriteStatus = mSmartV2Comm.keyUpdate(27, strFinalKeyA, strAccessCode, strFinalKeyB);
                                    if (dataWriteStatus != -1) {
                                        strLog += "Sector 6 Key Updated Successfully\n";
                                    } else {
                                        strLog += "Sector 6 Key Updation Failure\n";
                                    }
                                } else {
                                    strLog += "Failed To Write To Sector 6\n";
                                }
                                break;
                            case 7:
                                msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[5] + strInitialKeyB + Constants.HEX_READ_WRITE + cardInfo.getForthSlot());
                                dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                                if (dataWriteStatus != -1) {
                                    strLog += "Write Successfull To Sector 7\n";
                                    dataWriteStatus = mSmartV2Comm.keyUpdate(31, strFinalKeyA, strAccessCode, strFinalKeyB);
                                    if (dataWriteStatus != -1) {
                                        strLog += "Sector 7 Key Updated Successfully\n";
                                    } else {
                                        strLog += "Sector 7 Key Updation Failure\n";
                                    }
                                } else {
                                    strLog += "Failed To Write To Sector 7\n";
                                }
                                break;
                            case 8:
                                msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[6] + strInitialKeyB + Constants.HEX_READ_WRITE + cardInfo.getFifthSlot());
                                dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                                if (dataWriteStatus != -1) {
                                    strLog += "Write Successfull To Sector 8\n";
                                    dataWriteStatus = mSmartV2Comm.keyUpdate(35, strFinalKeyA, strAccessCode, strFinalKeyB);
                                    if (dataWriteStatus != -1) {
                                        strLog += "Sector 8 Key Updated Successfully\n";
                                    } else {
                                        strLog += "Sector 8 Key Updation Failure\n";
                                    }
                                } else {
                                    strLog += "Failed To Write To Sector 8\n";
                                }
                                break;
                            case 9:
                                msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[7] + strInitialKeyB + Constants.HEX_READ_WRITE + cardInfo.getSixthSlot() + cardInfo.getFirstFingerNo() + cardInfo.getFirstFingerSecurityLevel() + cardInfo.getFirstFingerIndex() + cardInfo.getFirstFingerQuality() + "0" + cardInfo.getFirstFingerVerificationMode() + dummy);
                                dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                                if (dataWriteStatus != -1) {
                                    strLog += "Write Successfull To Sector 9\n";
                                    dataWriteStatus = mSmartV2Comm.keyUpdate(39, strFinalKeyA, strAccessCode, strFinalKeyB);
                                    if (dataWriteStatus != -1) {
                                        strLog += "Sector 9 Key Updated Successfully\n";
                                    } else {
                                        strLog += "Sector 9 Key Updation Failure\n";
                                    }
                                } else {
                                    strLog += "Failed To Write To Sector 9\n";
                                }
                                break;
                            case 10:
                                msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[8] + strInitialKeyB + Constants.HEX_READ_WRITE + cardInfo.getSeventhSlot());
                                dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                                if (dataWriteStatus != -1) {
                                    strLog += "Write Successfull To Sector 10\n";
                                    dataWriteStatus = mSmartV2Comm.keyUpdate(43, strFinalKeyA, strAccessCode, strFinalKeyB);
                                    if (dataWriteStatus != -1) {
                                        strLog += "Sector 10 Key Updated Successfully\n";
                                    } else {
                                        strLog += "Sector 10 Key Updation Failure\n";
                                    }
                                } else {
                                    strLog += "Failed To Write To Sector 10\n";
                                }
                                break;
                            case 11:
                                msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[9] + strInitialKeyB + Constants.HEX_READ_WRITE + cardInfo.getEighthSlot());
                                dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                                if (dataWriteStatus != -1) {
                                    strLog += "Write Successfull To Sector 11\n";
                                    dataWriteStatus = mSmartV2Comm.keyUpdate(47, strFinalKeyA, strAccessCode, strFinalKeyB);
                                    if (dataWriteStatus != -1) {
                                        strLog += "Sector 11 Key Updated Successfully\n";
                                    } else {
                                        strLog += "Sector 11 Key Updation Failure\n";
                                    }
                                } else {
                                    strLog += "Failed To Write To Sector 11\n";
                                }
                                break;
                            case 12:
                                msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[10] + strInitialKeyB + Constants.HEX_READ_WRITE + cardInfo.getNinethSlot());
                                dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                                if (dataWriteStatus != -1) {
                                    strLog += "Write Successfull To Sector 12\n";
                                    dataWriteStatus = mSmartV2Comm.keyUpdate(51, strFinalKeyA, strAccessCode, strFinalKeyB);
                                    if (dataWriteStatus != -1) {
                                        strLog += "Sector 12 Key Updated Successfully\n";
                                    } else {
                                        strLog += "Sector 12 Key Updation Failure\n";
                                    }
                                } else {
                                    strLog += "Failed To Write To Sector 12\n";
                                }
                                break;
                            case 13:
                                msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[11] + strInitialKeyB + Constants.HEX_READ_WRITE + cardInfo.getTenthSlot());
                                dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                                if (dataWriteStatus != -1) {
                                    strLog += "Write Successfull To Sector 13\n";
                                    dataWriteStatus = mSmartV2Comm.keyUpdate(55, strFinalKeyA, strAccessCode, strFinalKeyB);
                                    if (dataWriteStatus != -1) {
                                        strLog += "Sector 13 Key Updated Successfully\n";
                                    } else {
                                        strLog += "Sector 13 Key Updation Failure\n";
                                    }
                                } else {
                                    strLog += "Failed To Write To Sector 13\n";
                                }
                                break;
                            case 14:
                                msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[12] + strInitialKeyB + Constants.HEX_READ_WRITE + cardInfo.getEleventhSlot());
                                dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                                if (dataWriteStatus != -1) {
                                    strLog += "Write Successfull To Sector 14\n";
                                    dataWriteStatus = mSmartV2Comm.keyUpdate(59, strFinalKeyA, strAccessCode, strFinalKeyB);
                                    if (dataWriteStatus != -1) {
                                        strLog += "Sector 14 Key Updated Successfully\n";
                                    } else {
                                        strLog += "Sector 14 Key Updation Failure\n";
                                    }
                                } else {
                                    strLog += "Failed To Write To Sector 14\n";
                                }
                                break;
                            case 15:
                                msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[13] + strInitialKeyB + Constants.HEX_READ_WRITE + cardInfo.getTwelvethSlot() + cardInfo.getSecondFingerNo() + cardInfo.getSecondFingerSecurityLevel() + cardInfo.getSecondFingerIndex() + cardInfo.getSecondFingerQuality() + "0" + cardInfo.getSecondFingerVerificationMode() + dummy);
                                dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                                if (dataWriteStatus != -1) {
                                    strLog += "Write Successfull To Sector 15\n";
                                    dataWriteStatus = mSmartV2Comm.keyUpdate(63, strFinalKeyA, strAccessCode, strFinalKeyB);
                                    if (dataWriteStatus != -1) {
                                        strLog += "Sector 15 Key Updated Successfully\n";
                                    } else {
                                        strLog += "Sector 15 Key Updation Failure\n";
                                    }
                                } else {
                                    strLog += "Failed To Write To Sector 15\n";
                                }
                                break;

                            default:
                                break;
                        }

                        if (dataWriteStatus != -1) {
                            continue;
                        } else {
                            break;
                        }
                    }

                    if (sectorKeyData != null) {
                        sectorKeyData.close();
                    }

                    //======================================Update Mad Block Of Sector 0=============================================//

                    if (dataWriteStatus != -1) {
                        String strSectorNo = "01A001";
                        int blockno1 = 01;
                        int blockno2 = 02;
                        String strData_01 = "55010005000000004802480248024802";
                        String strData_02 = "48024802480248024802480248024802";
                        dataWriteStatus = mSmartV2Comm.madUpdate(strSectorNo, blockno1, "524553534543", strData_01);
                        if (dataWriteStatus != -1) {
                            dataWriteStatus = mSmartV2Comm.madUpdate(strSectorNo, blockno2, "524553534543", strData_02);
                        }
                    }

                    //===============================================================================================//
                }
            }
            command = Utility.addCheckSum(Constants.READ_CARD_ID_COMMAND);
            String strCardId = mSmartV2Comm.readCardId(command.getBytes());
            return dataWriteStatus;
        }

        @Override
        protected void onPostExecute(Integer result) {
            mypDialog.cancel();
            clear();//Clear Ui fields.
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat mdformat = new SimpleDateFormat("ddMMyyyy HH:mm:ss");
            String strDateTime = mdformat.format(calendar.getTime());
            if (dataWriteStatus != -1) {
                int loginId = UserDetails.getInstance().getLoginId();
                String strOperation = "Card Key Write";
                String strStatus = "S";
                String autoId = cardInfo.getEnrollmentNo();
                if (autoId != null) {
                    int id = Integer.parseInt(autoId);
                    int insertStatus = -1;
                    insertStatus = dbComm.insertIntoSmartCardOperationLog(loginId, id, cardInfo.getDbCSN(), cardInfo.getCardId(), cardInfo.getSmartCardVer(), "Y", strOperation, strStatus, strDateTime);
                    if (insertStatus != -1) {
                        int updateStatus = -1;
                        updateStatus = dbComm.updateSmartCardVer(id, cardInfo.getDbCSN(), cardInfo.getSmartCardVer());
                        if (updateStatus != -1) {
                            Log.d("TEST", "Card Data Updated Successfully");
                        }
                        Log.d("TEST", "Smart Card Operation Log Inserted Successfully");
                    } else {
                        Log.d("TEST", "Smart Card Operation Log Insertion Failure");
                    }
                }
                Log.d("TEST", "Success Log Details:\n" + strLog);
                showCustomAlertDialog(true, "Card Write Status", "Card Write Successfully");
            } else {

                //==================================Insert Into Smart Card Operation Log==================================//

                int loginId = UserDetails.getInstance().getLoginId();
                String strOperation = "Card Key Write";
                String strStatus = "F";
                String autoId = cardInfo.getEnrollmentNo();
                if (autoId != null) {
                    int id = Integer.parseInt(autoId);
                    int insertStatus = -1;
                    insertStatus = dbComm.insertIntoSmartCardOperationLog(loginId, id, cardInfo.getDbCSN(), cardInfo.getCardId(), cardInfo.getSmartCardVer(), "Y", strOperation, strStatus, strDateTime);
                    if (insertStatus != -1) {
                        int updateStatus = -1;
                        updateStatus = dbComm.updateSmartCardVer(id, cardInfo.getDbCSN(), cardInfo.getSmartCardVer());
                        if (updateStatus != -1) {
                            Log.d("TEST", "Card Data Updated Successfully");
                        }
                        Log.d("TEST", "Smart Card Operation Log Inserted Successfully");
                    } else {
                        Log.d("TEST", "Smart Card Operation Log Insertion Failure");
                    }
                }
                Log.d("TEST", "Failure Log Details:\n" + strLog);
                showCustomAlertDialog(false, "Card Write Status", "Failed To Write To Card");
            }
        }
    }


    //===================================== Async Task USB Card Write ===================================//

    private class AsyncTaskUsbCardWrite extends AsyncTask <String, Void, Integer> {

        ProgressDialog mypDialog;
        String strLog = "";
        int dataWriteStatus = -1;

        MicroSmartV2Communicator mSmartV2Comm;
        SmartCardInfo cardInfo;

        AsyncTaskUsbCardWrite(MicroSmartV2Communicator mSmartV2Comm, SmartCardInfo cardInfo) {
            this.mSmartV2Comm = mSmartV2Comm;
            this.cardInfo = cardInfo;
        }

        @Override
        protected void onPreExecute() {
            mypDialog = new ProgressDialog(SmartCardActivity.this);
            mypDialog.setMessage("Card Writing Wait...");
            mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mypDialog.setCanceledOnTouchOutside(false);
            mypDialog.show();
        }

        @Override
        protected Integer doInBackground(String... params) {

            int status = -1;
            String command = "";
            String cardId = cardInfo.getCardId();
            if (cardId != null && cardId.trim().length() > 0) {
                command = Utility.addCheckSum(Constants.WRITE_CARD_ID_COMMAND + cardId);
                status = mSmartV2Comm.writeCardId(command.getBytes());
            }
            if (status == -1) {
                return status;
            }

            int noOfTemplates = cardInfo.getNoOfTemplates();
            if (noOfTemplates == 1) {
                Cursor sectorKeyData = dbComm.getSectorAndKeyForWriteCard();
                if (sectorKeyData != null) {
                    String strEmptyData = "000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
                    String dummy = "00000000000000000000000000000000000000000000000000000000"; //dummy string for writing Sector 9 & F
                    String msg = "";
                    while (sectorKeyData.moveToNext()) {
                        String strSectorNo = sectorKeyData.getString(0).trim();
                        String strKeyB = sectorKeyData.getString(2).trim();
                        int sectorNo = Integer.parseInt(strSectorNo);
                        switch (sectorNo) {
                            case 2:
                                msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[0] + strKeyB + Constants.ASCII_READ_WRITE + cardInfo.getEmployeeId() + cardInfo.getEmpName() + cardInfo.getValidUpto() + cardInfo.getBirthDate() + cardInfo.getSiteCode() + cardInfo.getBloodGroup() + cardInfo.getSmartCardVer());
                                dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                                if (dataWriteStatus != -1) {
                                    strLog += "Write Successfull To Sector 2\n";
                                } else {
                                    strLog += "Failed To Write To Sector 2\n";
                                }
                                break;
//
//                            case 3:
//
//                                msg = Add + sec[1] + strKeyB + "01" + strAadhaarId;
//                                a2 = msg.toCharArray();
//                                checksum = 0;
//                                for (i = 0; i < a2.length; i++) {
//                                    checksum ^= a2[i];
//                                }
//                                c2 = checksum;
//                                if(Integer.toHexString(c2).toUpperCase().length() != 2) {
//                                    msg = msg + "0" + Integer.toHexString(c2).toUpperCase() + "%";
//                                }else {
//                                    msg = msg + Integer.toHexString(c2).toUpperCase() + "%";
//                                }
//
//                                dataWriteStatus=Sector_3_Write(msg, "3");
//
//                                if(dataWriteStatus!=-1) {
//                                    strLog+="Write Successfull To Sector 3\n";
//                                }
//                                else {
//                                    strLog+="Failed To Write To Sector 3\n";
//                                }
//                                break;

                            case 4:
                                msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[2] + strKeyB + Constants.HEX_READ_WRITE + cardInfo.getFirstSlot());
                                dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                                if (dataWriteStatus != -1) {
                                    strLog += "Write Successfull To Sector 4\n";
                                } else {
                                    strLog += "Failed To Write To Sector 4\n";
                                }
                                break;
                            case 5:
                                msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[3] + strKeyB + Constants.HEX_READ_WRITE + cardInfo.getSecondSlot());
                                dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                                if (dataWriteStatus != -1) {
                                    strLog += "Write Successfull To Sector 5\n";
                                } else {
                                    strLog += "Failed To Write To Sector 5\n";
                                }
                                break;
                            case 6:
                                msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[4] + strKeyB + Constants.HEX_READ_WRITE + cardInfo.getThirdSlot());
                                dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                                if (dataWriteStatus != -1) {
                                    strLog += "Write Successfull To Sector 6\n";
                                } else {
                                    strLog += "Failed To Write To Sector 6\n";
                                }
                                break;
                            case 7:
                                msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[5] + strKeyB + Constants.HEX_READ_WRITE + cardInfo.getForthSlot());
                                dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                                if (dataWriteStatus != -1) {
                                    strLog += "Write Successfull To Sector 7\n";
                                } else {
                                    strLog += "Failed To Write To Sector 7\n";
                                }
                                break;
                            case 8:
                                msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[6] + strKeyB + Constants.HEX_READ_WRITE + cardInfo.getFifthSlot());
                                dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                                if (dataWriteStatus != -1) {
                                    strLog += "Write Successfull To Sector 8\n";
                                } else {
                                    strLog += "Failed To Write To Sector 8\n";
                                }
                                break;
                            case 9:
                                msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[7] + strKeyB + Constants.HEX_READ_WRITE + cardInfo.getSixthSlot() + cardInfo.getFirstFingerNo() + cardInfo.getFirstFingerSecurityLevel() + cardInfo.getFirstFingerIndex() + cardInfo.getFirstFingerQuality() + "0" + cardInfo.getFirstFingerVerificationMode() + dummy);
                                dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                                if (dataWriteStatus != -1) {
                                    strLog += "Write Successfull To Sector 9\n";
                                } else {
                                    strLog += "Failed To Write To Sector 9\n";
                                }
                                break;
                            case 10:
                                msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[8] + strKeyB + Constants.HEX_READ_WRITE + strEmptyData);
                                dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                                if (dataWriteStatus != -1) {
                                    strLog += "Write Successfull To Sector 10\n";
                                } else {
                                    strLog += "Failed To Write To Sector 10\n";
                                }
                                break;
                            case 11:
                                msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[9] + strKeyB + Constants.HEX_READ_WRITE + strEmptyData);
                                dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                                if (dataWriteStatus != -1) {
                                    strLog += "Write Successfull To Sector 11\n";
                                } else {
                                    strLog += "Failed To Write To Sector 11\n";
                                }
                                break;
                            case 12:
                                msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[10] + strKeyB + Constants.HEX_READ_WRITE + strEmptyData);
                                dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                                if (dataWriteStatus != -1) {
                                    strLog += "Write Successfull To Sector 12\n";
                                } else {
                                    strLog += "Failed To Write To Sector 12\n";
                                }

                                break;
                            case 13:
                                msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[11] + strKeyB + Constants.HEX_READ_WRITE + strEmptyData);
                                dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                                if (dataWriteStatus != -1) {
                                    strLog += "Write Successfull To Sector 13\n";
                                } else {
                                    strLog += "Failed To Write To Sector 13\n";
                                }
                                break;
                            case 14:
                                msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[12] + strKeyB + Constants.HEX_READ_WRITE + strEmptyData);
                                dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                                if (dataWriteStatus != -1) {
                                    strLog += "Write Successfull To Sector 14\n";
                                } else {
                                    strLog += "Failed To Write To Sector 14\n";
                                }
                                break;
                            case 15:
                                msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[13] + strKeyB + Constants.HEX_READ_WRITE + strEmptyData);
                                dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                                if (dataWriteStatus != -1) {
                                    strLog += "Write Successfull To Sector 15\n";
                                } else {
                                    strLog += "Failed To Write To Sector 15\n";
                                }
                                break;
                            default:
                                break;
                        }
                        if (dataWriteStatus != -1) {
                            continue;
                        } else {
                            break;
                        }
                    }

                    if (sectorKeyData != null) {
                        sectorKeyData.close();
                    }

                    if (dataWriteStatus != -1) {
                        //==================================  Update Mad Block Of Sector 0  =============================================//
                        String sectorNo = "01A001";
                        int blockno1 = 01;
                        int blockno2 = 02;
                        String strSec_0_Block_1 = "55010005000000004802480248024802";
                        String strSec_0_Block_2 = "48024802000000000000000000000000";
                        dataWriteStatus = mSmartV2Comm.madUpdate(sectorNo, blockno1, "524553534543", strSec_0_Block_1);
                        if (dataWriteStatus != -1) {
                            dataWriteStatus = mSmartV2Comm.madUpdate(sectorNo, blockno2, "524553534543", strSec_0_Block_2);
                        }
                    }
                }
            } else if (noOfTemplates == 2) {

                Cursor sectorKeyData = dbComm.getSectorAndKeyForWriteCard();
                if (sectorKeyData != null) {
                    String strEmptyData = "000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
                    String dummy = "00000000000000000000000000000000000000000000000000000000"; //dummy string for writing Sector 9 & F
                    String msg = "";
                    while (sectorKeyData.moveToNext()) {
                        String strSectorNo = sectorKeyData.getString(0).trim();
                        String strKeyB = sectorKeyData.getString(2).trim();
                        int sectorNo = Integer.parseInt(strSectorNo);
                        switch (sectorNo) {
                            case 2:
                                msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[0] + strKeyB + Constants.ASCII_READ_WRITE + cardInfo.getEmployeeId() + cardInfo.getEmpName() + cardInfo.getValidUpto() + cardInfo.getBirthDate() + cardInfo.getSiteCode() + cardInfo.getBloodGroup() + cardInfo.getSmartCardVer());
                                dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                                if (dataWriteStatus != -1) {
                                    strLog += "Write Successfull To Sector 2\n";
                                } else {
                                    strLog += "Failed To Write To Sector 2\n";
                                }
                                break;
//
//                            case 3:
//
//                                msg = Add + sec[1] + strKeyB + "01" + strAadhaarId;
//                                a2 = msg.toCharArray();
//                                checksum = 0;
//                                for (i = 0; i < a2.length; i++) {
//                                    checksum ^= a2[i];
//                                }
//                                c2 = checksum;
//                                if(Integer.toHexString(c2).toUpperCase().length() != 2) {
//                                    msg = msg + "0" + Integer.toHexString(c2).toUpperCase() + "%";
//                                }else {
//                                    msg = msg + Integer.toHexString(c2).toUpperCase() + "%";
//                                }
//
//                                dataWriteStatus=Sector_3_Write(msg, "3");
//
//                                if(dataWriteStatus!=-1) {
//                                    strLog+="Write Successfull To Sector 3\n";
//                                }
//                                else {
//                                    strLog+="Failed To Write To Sector 3\n";
//                                }
//                                break;

                            case 4:
                                msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[2] + strKeyB + Constants.HEX_READ_WRITE + cardInfo.getFirstSlot());
                                dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                                if (dataWriteStatus != -1) {
                                    strLog += "Write Successfull To Sector 4\n";
                                } else {
                                    strLog += "Failed To Write To Sector 4\n";
                                }
                                break;
                            case 5:
                                msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[3] + strKeyB + Constants.HEX_READ_WRITE + cardInfo.getSecondSlot());
                                dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                                if (dataWriteStatus != -1) {
                                    strLog += "Write Successfull To Sector 5\n";
                                } else {
                                    strLog += "Failed To Write To Sector 5\n";
                                }
                                break;
                            case 6:
                                msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[4] + strKeyB + Constants.HEX_READ_WRITE + cardInfo.getThirdSlot());
                                dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                                if (dataWriteStatus != -1) {
                                    strLog += "Write Successfull To Sector 6\n";
                                } else {
                                    strLog += "Failed To Write To Sector 6\n";
                                }
                                break;
                            case 7:
                                msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[5] + strKeyB + Constants.HEX_READ_WRITE + cardInfo.getForthSlot());
                                dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                                if (dataWriteStatus != -1) {
                                    strLog += "Write Successfull To Sector 7\n";
                                } else {
                                    strLog += "Failed To Write To Sector 7\n";
                                }
                                break;
                            case 8:
                                msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[6] + strKeyB + Constants.HEX_READ_WRITE + cardInfo.getFifthSlot());
                                dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                                if (dataWriteStatus != -1) {
                                    strLog += "Write Successfull To Sector 8\n";
                                } else {
                                    strLog += "Failed To Write To Sector 8\n";
                                }
                                break;
                            case 9:
                                msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[7] + strKeyB + Constants.HEX_READ_WRITE + cardInfo.getSixthSlot() + cardInfo.getFirstFingerNo() + cardInfo.getFirstFingerSecurityLevel() + cardInfo.getFirstFingerIndex() + cardInfo.getFirstFingerQuality() + "0" + cardInfo.getFirstFingerVerificationMode() + dummy);
                                dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                                if (dataWriteStatus != -1) {
                                    strLog += "Write Successfull To Sector 9\n";
                                } else {
                                    strLog += "Failed To Write To Sector 9\n";
                                }
                                break;
                            case 10:
                                msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[8] + strKeyB + Constants.HEX_READ_WRITE + cardInfo.getSeventhSlot());
                                dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                                if (dataWriteStatus != -1) {
                                    strLog += "Write Successfull To Sector 10\n";
                                } else {
                                    strLog += "Failed To Write To Sector 10\n";
                                }
                                break;
                            case 11:
                                msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[9] + strKeyB + Constants.HEX_READ_WRITE + cardInfo.getEighthSlot());
                                dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                                if (dataWriteStatus != -1) {
                                    strLog += "Write Successfull To Sector 11\n";
                                } else {
                                    strLog += "Failed To Write To Sector 11\n";
                                }
                                break;
                            case 12:
                                msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[10] + strKeyB + Constants.HEX_READ_WRITE + cardInfo.getNinethSlot());
                                dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                                if (dataWriteStatus != -1) {
                                    strLog += "Write Successfull To Sector 12\n";
                                } else {
                                    strLog += "Failed To Write To Sector 12\n";
                                }

                                break;
                            case 13:
                                msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[11] + strKeyB + Constants.HEX_READ_WRITE + cardInfo.getTenthSlot());
                                dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                                if (dataWriteStatus != -1) {
                                    strLog += "Write Successfull To Sector 13\n";
                                } else {
                                    strLog += "Failed To Write To Sector 13\n";
                                }
                                break;
                            case 14:
                                msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[12] + strKeyB + Constants.HEX_READ_WRITE + cardInfo.getEleventhSlot());
                                dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                                if (dataWriteStatus != -1) {
                                    strLog += "Write Successfull To Sector 14\n";
                                } else {
                                    strLog += "Failed To Write To Sector 14\n";
                                }
                                break;
                            case 15:
                                msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[13] + strKeyB + Constants.HEX_READ_WRITE + cardInfo.getTwelvethSlot() + cardInfo.getSecondFingerNo() + cardInfo.getSecondFingerSecurityLevel() + cardInfo.getSecondFingerIndex() + cardInfo.getSecondFingerQuality() + "0" + cardInfo.getSecondFingerVerificationMode() + dummy);
                                dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                                if (dataWriteStatus != -1) {
                                    strLog += "Write Successfull To Sector 15\n";
                                } else {
                                    strLog += "Failed To Write To Sector 15\n";
                                }
                                break;
                            default:
                                break;
                        }
                        if (dataWriteStatus != -1) {
                            continue;
                        } else {
                            break;
                        }
                    }
                    if (sectorKeyData != null) {
                        sectorKeyData.close();
                    }
                    if (dataWriteStatus != -1) {
                        //==================================  Update Mad Block Of Sector 0  =============================================//
                        String sectorNo = "01A001";
                        int blockno1 = 01;
                        int blockno2 = 02;
                        String strSectorNo = "01A001";
                        String strSec_0_Block_1 = "55010005000000004802480248024802";
                        String strSec_0_Block_2 = "48024802480248024802480248024802";
                        dataWriteStatus = mSmartV2Comm.madUpdate(sectorNo, blockno1, "524553534543", strSec_0_Block_1);
                        if (dataWriteStatus != -1) {
                            dataWriteStatus = mSmartV2Comm.madUpdate(sectorNo, blockno2, "524553534543", strSec_0_Block_2);
                        }
                    }
                }

            }

            command = Utility.addCheckSum(Constants.READ_CARD_ID_COMMAND);
            String strCardId = mSmartV2Comm.readCardId(command.getBytes());

            return dataWriteStatus;
        }

        @Override
        protected void onPostExecute(Integer result) {
            mypDialog.cancel();
            clear();
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat mdformat = new SimpleDateFormat("ddMMyyyy HH:mm:ss");
            String strDateTime = mdformat.format(calendar.getTime());
            if (dataWriteStatus != -1) {
                int loginId = UserDetails.getInstance().getLoginId();
                String strOperation = "Card Write";
                String strStatus = "S";
                String autoId = cardInfo.getEnrollmentNo();
                if (autoId != null) {
                    int id = Integer.parseInt(autoId);
                    int insertStatus = -1;
                    insertStatus = dbComm.insertIntoSmartCardOperationLog(loginId, id, cardInfo.getDbCSN(), cardInfo.getCardId(), cardInfo.getSmartCardVer(), "Y", strOperation, strStatus, strDateTime);
                    if (insertStatus != -1) {
                        int updateStatus = -1;
                        updateStatus = dbComm.updateSmartCardVer(id, cardInfo.getDbCSN(), cardInfo.getSmartCardVer());
                        if (updateStatus != -1) {
                            Log.d("TEST", "Card Data Updated Successfully");
                        }
                        Log.d("TEST", "Smart Card Operation Log Inserted Successfully");
                    } else {
                        Log.d("TEST", "Smart Card Operation Log Insertion Failure");
                    }
                    showCustomAlertDialog(true, "Card Write Status", "Card Write Successful");
                }
            } else {
                int loginId = UserDetails.getInstance().getLoginId();
                String strOperation = "Card Write";
                String strStatus = "F";
                String autoId = cardInfo.getEnrollmentNo();
                if (autoId != null) {
                    int id = Integer.parseInt(autoId);
                    int insertStatus = -1;
                    insertStatus = dbComm.insertIntoSmartCardOperationLog(loginId, id, cardInfo.getDbCSN(), cardInfo.getCardId(), cardInfo.getSmartCardVer(), "Y", strOperation, strStatus, strDateTime);
                    if (insertStatus != -1) {
                        int updateStatus = -1;
                        updateStatus = dbComm.updateSmartCardVer(id, cardInfo.getDbCSN(), cardInfo.getSmartCardVer());
                        if (updateStatus != -1) {
                            Log.d("TEST", "Card Data Updated Successfully");
                        }
                        Log.d("TEST", "Smart Card Operation Log Inserted Successfully");
                    } else {
                        Log.d("TEST", "Smart Card Operation Log Insertion Failure");
                    }
                }
                showCustomAlertDialog(false, "Card Write Status", "Failed To Write To Card");
            }
        }
    }


    private class AsyncTaskRC522CardWrite extends AsyncTask <Void, Void, Boolean> {

        ProgressDialog mypDialog;
        RC522Communicator rc522Comm;
        SmartCardInfo cardInfo;

        AsyncTaskRC522CardWrite(RC522Communicator rc522Comm, SmartCardInfo cardInfo) {
            this.rc522Comm = rc522Comm;
            this.cardInfo = cardInfo;
        }

        @Override
        protected void onPreExecute() {
            mypDialog = new ProgressDialog(SmartCardActivity.this);
            mypDialog.setMessage("Card Writing Wait...");
            mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mypDialog.setCanceledOnTouchOutside(false);
            mypDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            boolean status = false;
            boolean opStatus = false;
            ArrayList <String> list = null;
            String writeData = "";
            String msg = "";
            String dummyData = "000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
            String dummy = "00000000000000000000000000000000000000000000000000000000"; //dummy string for writing Sector 9 & F
            Cursor sectorKeyData = null;
            int noOfTemplates = cardInfo.getNoOfTemplates();
            switch (noOfTemplates) {
                case 1:
                    sectorKeyData = dbComm.getSectorAndKeyForWriteCard();
                    if (sectorKeyData != null) {
                        while (sectorKeyData.moveToNext()) {
                            String strSectorNo = sectorKeyData.getString(0).trim();
                            String strKeyB = sectorKeyData.getString(2).trim();
                            int sectorNo = Integer.parseInt(strSectorNo);
                            switch (sectorNo) {
                                case 0:
                                    opStatus = true;
                                    break;
                                case 1:
                                    opStatus = writeSectorOne(cardInfo.getCardId());
                                    break;
                                case 2:
                                    opStatus = false;
                                    writeData = cardInfo.getEmployeeId() + cardInfo.getEmpName() + cardInfo.getValidUpto() + cardInfo.getBirthDate() + cardInfo.getSiteCode() + cardInfo.getBloodGroup() + cardInfo.getSmartCardVer();
                                    writeData = Utility.asciiToHex(writeData);
                                    writeData = writeData.toUpperCase();
                                    list = Utility.splitStringBySize(writeData, 32);
                                    if (list != null && list.size() == 3) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(0);
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            char[] data = rc522Comm.readRC522();
                                            String strData = new String(data);
                                            String[] arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(1);
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(2);
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    data = rc522Comm.readRC522();
                                                                    strData = new String(data);
                                                                    arr = strData.split(":");
                                                                    if (arr != null && arr.length == 3) {
                                                                        strData = arr[2].trim();
                                                                        if (strData.equals("SUCCESS")) {
                                                                            opStatus = true;
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
                                    break;

                                case 3:
                                    opStatus = true;
                                    break;
                                case 4:
                                    opStatus = false;
                                    writeData = cardInfo.getFirstSlot();
                                    list = Utility.splitStringBySize(writeData, 32);
                                    if (list != null && list.size() == 3) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(0);
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            char[] data = rc522Comm.readRC522();
                                            String strData = new String(data);
                                            String[] arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(1);
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(2);
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    data = rc522Comm.readRC522();
                                                                    strData = new String(data);
                                                                    arr = strData.split(":");
                                                                    if (arr != null && arr.length == 3) {
                                                                        strData = arr[2].trim();
                                                                        if (strData.equals("SUCCESS")) {
                                                                            opStatus = true;
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
                                    break;
                                case 5:
                                    opStatus = false;
                                    writeData = cardInfo.getSecondSlot();
                                    list = Utility.splitStringBySize(writeData, 32);
                                    if (list != null && list.size() == 3) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(0);
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            char[] data = rc522Comm.readRC522();
                                            String strData = new String(data);
                                            String[] arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(1);
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(2);
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    data = rc522Comm.readRC522();
                                                                    strData = new String(data);
                                                                    arr = strData.split(":");
                                                                    if (arr != null && arr.length == 3) {
                                                                        strData = arr[2].trim();
                                                                        if (strData.equals("SUCCESS")) {
                                                                            opStatus = true;
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
                                    break;
                                case 6:
                                    opStatus = false;
                                    writeData = cardInfo.getThirdSlot();
                                    list = Utility.splitStringBySize(writeData, 32);
                                    if (list != null && list.size() == 3) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(0);
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            char[] data = rc522Comm.readRC522();
                                            String strData = new String(data);
                                            String[] arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(1);
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(2);
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    data = rc522Comm.readRC522();
                                                                    strData = new String(data);
                                                                    arr = strData.split(":");
                                                                    if (arr != null && arr.length == 3) {
                                                                        strData = arr[2].trim();
                                                                        if (strData.equals("SUCCESS")) {
                                                                            opStatus = true;
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
                                    break;
                                case 7:
                                    opStatus = false;
                                    writeData = cardInfo.getForthSlot();
                                    list = Utility.splitStringBySize(writeData, 32);
                                    if (list != null && list.size() == 3) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(0);
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            char[] data = rc522Comm.readRC522();
                                            String strData = new String(data);
                                            String[] arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(1);
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(2);
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    data = rc522Comm.readRC522();
                                                                    strData = new String(data);
                                                                    arr = strData.split(":");
                                                                    if (arr != null && arr.length == 3) {
                                                                        strData = arr[2].trim();
                                                                        if (strData.equals("SUCCESS")) {
                                                                            opStatus = true;
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
                                    break;
                                case 8:
                                    opStatus = false;
                                    writeData = cardInfo.getFifthSlot();
                                    list = Utility.splitStringBySize(writeData, 32);
                                    if (list != null && list.size() == 3) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(0);
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            char[] data = rc522Comm.readRC522();
                                            String strData = new String(data);
                                            String[] arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(1);
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(2);
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    data = rc522Comm.readRC522();
                                                                    strData = new String(data);
                                                                    arr = strData.split(":");
                                                                    if (arr != null && arr.length == 3) {
                                                                        strData = arr[2].trim();
                                                                        if (strData.equals("SUCCESS")) {
                                                                            opStatus = true;
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
                                    break;
                                case 9:
                                    opStatus = false;
                                    writeData = cardInfo.getSixthSlot() + cardInfo.getFirstFingerNo() + cardInfo.getFirstFingerSecurityLevel() + cardInfo.getFirstFingerIndex() + cardInfo.getFirstFingerQuality() + "0" + cardInfo.getFirstFingerVerificationMode() + dummy;
                                    list = Utility.splitStringBySize(writeData, 32);
                                    if (list != null && list.size() == 3) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(0);
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            char[] data = rc522Comm.readRC522();
                                            String strData = new String(data);
                                            String[] arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(1);
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(2);
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    data = rc522Comm.readRC522();
                                                                    strData = new String(data);
                                                                    arr = strData.split(":");
                                                                    if (arr != null && arr.length == 3) {
                                                                        strData = arr[2].trim();
                                                                        if (strData.equals("SUCCESS")) {
                                                                            opStatus = true;
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
                                    break;
                                case 10:
                                    opStatus = false;
                                    list = Utility.splitStringBySize(dummyData, 32);
                                    if (list != null && list.size() == 3) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(0);
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            char[] data = rc522Comm.readRC522();
                                            String strData = new String(data);
                                            String[] arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(1);
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(2);
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    data = rc522Comm.readRC522();
                                                                    strData = new String(data);
                                                                    arr = strData.split(":");
                                                                    if (arr != null && arr.length == 3) {
                                                                        strData = arr[2].trim();
                                                                        if (strData.equals("SUCCESS")) {
                                                                            opStatus = true;
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
                                    break;
                                case 11:
                                    opStatus = false;
                                    list = Utility.splitStringBySize(dummyData, 32);
                                    if (list != null && list.size() == 3) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(0);
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            char[] data = rc522Comm.readRC522();
                                            String strData = new String(data);
                                            String[] arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(1);
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(2);
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    data = rc522Comm.readRC522();
                                                                    strData = new String(data);
                                                                    arr = strData.split(":");
                                                                    if (arr != null && arr.length == 3) {
                                                                        strData = arr[2].trim();
                                                                        if (strData.equals("SUCCESS")) {
                                                                            opStatus = true;
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
                                    break;
                                case 12:
                                    opStatus = false;
                                    list = Utility.splitStringBySize(dummyData, 32);
                                    if (list != null && list.size() == 3) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(0);
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            char[] data = rc522Comm.readRC522();
                                            String strData = new String(data);
                                            String[] arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(1);
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(2);
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    data = rc522Comm.readRC522();
                                                                    strData = new String(data);
                                                                    arr = strData.split(":");
                                                                    if (arr != null && arr.length == 3) {
                                                                        strData = arr[2].trim();
                                                                        if (strData.equals("SUCCESS")) {
                                                                            opStatus = true;
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
                                    break;
                                case 13:
                                    opStatus = false;
                                    list = Utility.splitStringBySize(dummyData, 32);
                                    if (list != null && list.size() == 3) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(0);
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            char[] data = rc522Comm.readRC522();
                                            String strData = new String(data);
                                            String[] arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(1);
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(2);
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    data = rc522Comm.readRC522();
                                                                    strData = new String(data);
                                                                    arr = strData.split(":");
                                                                    if (arr != null && arr.length == 3) {
                                                                        strData = arr[2].trim();
                                                                        if (strData.equals("SUCCESS")) {
                                                                            opStatus = true;
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
                                    break;
                                case 14:
                                    opStatus = false;
                                    list = Utility.splitStringBySize(dummyData, 32);
                                    if (list != null && list.size() == 3) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(0);
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            char[] data = rc522Comm.readRC522();
                                            String strData = new String(data);
                                            String[] arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(1);
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(2);
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    data = rc522Comm.readRC522();
                                                                    strData = new String(data);
                                                                    arr = strData.split(":");
                                                                    if (arr != null && arr.length == 3) {
                                                                        strData = arr[2].trim();
                                                                        if (strData.equals("SUCCESS")) {
                                                                            opStatus = true;
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
                                    break;
                                case 15:
                                    opStatus = false;
                                    list = Utility.splitStringBySize(dummyData, 32);
                                    if (list != null && list.size() == 3) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(0);
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            char[] data = rc522Comm.readRC522();
                                            String strData = new String(data);
                                            String[] arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(1);
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(2);
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    data = rc522Comm.readRC522();
                                                                    strData = new String(data);
                                                                    arr = strData.split(":");
                                                                    if (arr != null && arr.length == 3) {
                                                                        strData = arr[2].trim();
                                                                        if (strData.equals("SUCCESS")) {
                                                                            opStatus = true;
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
                                    break;
                            }
                            if (!opStatus) {
                                break;
                            }
                        }
                        sectorKeyData.close();
                        if (opStatus) {
                            opStatus = false;
                            String strData_01 = "55010005000000004802480248024802";
                            String strData_02 = "48024802000000000000000000000000";
                            opStatus = updateMAD(strData_01, strData_02);
                        }
                    }
                    break;

                case 2:
                    sectorKeyData = dbComm.getSectorAndKeyForWriteCard();
                    if (sectorKeyData != null) {
                        while (sectorKeyData.moveToNext()) {
                            String strSectorNo = sectorKeyData.getString(0).trim();
                            String strKeyB = sectorKeyData.getString(2).trim();
                            int sectorNo = Integer.parseInt(strSectorNo);
                            switch (sectorNo) {
                                case 0:
                                    opStatus = true;
                                    break;
                                case 1:
                                    opStatus = writeSectorOne(cardInfo.getCardId());
                                    break;
                                case 2:
                                    opStatus = false;
                                    writeData = cardInfo.getEmployeeId() + cardInfo.getEmpName() + cardInfo.getValidUpto() + cardInfo.getBirthDate() + cardInfo.getSiteCode() + cardInfo.getBloodGroup() + cardInfo.getSmartCardVer();
                                    writeData = Utility.asciiToHex(writeData);
                                    writeData = writeData.toUpperCase();
                                    list = Utility.splitStringBySize(writeData, 32);
                                    if (list != null && list.size() == 3) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(0);
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            char[] data = rc522Comm.readRC522();
                                            String strData = new String(data);
                                            String[] arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(1);
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(2);
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    data = rc522Comm.readRC522();
                                                                    strData = new String(data);
                                                                    arr = strData.split(":");
                                                                    if (arr != null && arr.length == 3) {
                                                                        strData = arr[2].trim();
                                                                        if (strData.equals("SUCCESS")) {
                                                                            opStatus = true;
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
                                    break;
                                case 3:
                                    opStatus = true;
                                    break;
                                case 4:
                                    opStatus = false;
                                    writeData = cardInfo.getFirstSlot();
                                    list = Utility.splitStringBySize(writeData, 32);
                                    if (list != null && list.size() == 3) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(0);
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            char[] data = rc522Comm.readRC522();
                                            String strData = new String(data);
                                            String[] arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(1);
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(2);
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    data = rc522Comm.readRC522();
                                                                    strData = new String(data);
                                                                    arr = strData.split(":");
                                                                    if (arr != null && arr.length == 3) {
                                                                        strData = arr[2].trim();
                                                                        if (strData.equals("SUCCESS")) {
                                                                            opStatus = true;
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
                                    break;
                                case 5:
                                    opStatus = false;
                                    writeData = cardInfo.getSecondSlot();
                                    list = Utility.splitStringBySize(writeData, 32);
                                    if (list != null && list.size() == 3) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(0);
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            char[] data = rc522Comm.readRC522();
                                            String strData = new String(data);
                                            String[] arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(1);
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(2);
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    data = rc522Comm.readRC522();
                                                                    strData = new String(data);
                                                                    arr = strData.split(":");
                                                                    if (arr != null && arr.length == 3) {
                                                                        strData = arr[2].trim();
                                                                        if (strData.equals("SUCCESS")) {
                                                                            opStatus = true;
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
                                    break;
                                case 6:
                                    opStatus = false;
                                    writeData = cardInfo.getThirdSlot();
                                    list = Utility.splitStringBySize(writeData, 32);
                                    if (list != null && list.size() == 3) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(0);
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            char[] data = rc522Comm.readRC522();
                                            String strData = new String(data);
                                            String[] arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(1);
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(2);
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    data = rc522Comm.readRC522();
                                                                    strData = new String(data);
                                                                    arr = strData.split(":");
                                                                    if (arr != null && arr.length == 3) {
                                                                        strData = arr[2].trim();
                                                                        if (strData.equals("SUCCESS")) {
                                                                            opStatus = true;
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
                                    break;
                                case 7:
                                    opStatus = false;
                                    writeData = cardInfo.getForthSlot();
                                    list = Utility.splitStringBySize(writeData, 32);
                                    if (list != null && list.size() == 3) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(0);
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            char[] data = rc522Comm.readRC522();
                                            String strData = new String(data);
                                            String[] arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(1);
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(2);
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    data = rc522Comm.readRC522();
                                                                    strData = new String(data);
                                                                    arr = strData.split(":");
                                                                    if (arr != null && arr.length == 3) {
                                                                        strData = arr[2].trim();
                                                                        if (strData.equals("SUCCESS")) {
                                                                            opStatus = true;
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
                                    break;
                                case 8:
                                    opStatus = false;
                                    writeData = cardInfo.getFifthSlot();
                                    list = Utility.splitStringBySize(writeData, 32);
                                    if (list != null && list.size() == 3) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(0);
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            char[] data = rc522Comm.readRC522();
                                            String strData = new String(data);
                                            String[] arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(1);
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(2);
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    data = rc522Comm.readRC522();
                                                                    strData = new String(data);
                                                                    arr = strData.split(":");
                                                                    if (arr != null && arr.length == 3) {
                                                                        strData = arr[2].trim();
                                                                        if (strData.equals("SUCCESS")) {
                                                                            opStatus = true;
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
                                    break;
                                case 9:
                                    opStatus = false;
                                    writeData = cardInfo.getSixthSlot() + cardInfo.getFirstFingerNo() + cardInfo.getFirstFingerSecurityLevel() + cardInfo.getFirstFingerIndex() + cardInfo.getFirstFingerQuality() + "0" + cardInfo.getFirstFingerVerificationMode() + dummy;
                                    list = Utility.splitStringBySize(writeData, 32);
                                    if (list != null && list.size() == 3) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(0);
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            char[] data = rc522Comm.readRC522();
                                            String strData = new String(data);
                                            String[] arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(1);
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(2);
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    data = rc522Comm.readRC522();
                                                                    strData = new String(data);
                                                                    arr = strData.split(":");
                                                                    if (arr != null && arr.length == 3) {
                                                                        strData = arr[2].trim();
                                                                        if (strData.equals("SUCCESS")) {
                                                                            opStatus = true;
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
                                    break;
                                case 10:
                                    opStatus = false;
                                    writeData = cardInfo.getSeventhSlot();
                                    list = Utility.splitStringBySize(writeData, 32);
                                    if (list != null && list.size() == 3) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(0);
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            char[] data = rc522Comm.readRC522();
                                            String strData = new String(data);
                                            String[] arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(1);
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(2);
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    data = rc522Comm.readRC522();
                                                                    strData = new String(data);
                                                                    arr = strData.split(":");
                                                                    if (arr != null && arr.length == 3) {
                                                                        strData = arr[2].trim();
                                                                        if (strData.equals("SUCCESS")) {
                                                                            opStatus = true;
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
                                    break;
                                case 11:
                                    opStatus = false;
                                    writeData = cardInfo.getEighthSlot();
                                    list = Utility.splitStringBySize(writeData, 32);
                                    if (list != null && list.size() == 3) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(0);
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            char[] data = rc522Comm.readRC522();
                                            String strData = new String(data);
                                            String[] arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(1);
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(2);
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    data = rc522Comm.readRC522();
                                                                    strData = new String(data);
                                                                    arr = strData.split(":");
                                                                    if (arr != null && arr.length == 3) {
                                                                        strData = arr[2].trim();
                                                                        if (strData.equals("SUCCESS")) {
                                                                            opStatus = true;
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
                                    break;
                                case 12:
                                    opStatus = false;
                                    writeData = cardInfo.getNinethSlot();
                                    list = Utility.splitStringBySize(writeData, 32);
                                    if (list != null && list.size() == 3) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(0);
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            char[] data = rc522Comm.readRC522();
                                            String strData = new String(data);
                                            String[] arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(1);
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(2);
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    data = rc522Comm.readRC522();
                                                                    strData = new String(data);
                                                                    arr = strData.split(":");
                                                                    if (arr != null && arr.length == 3) {
                                                                        strData = arr[2].trim();
                                                                        if (strData.equals("SUCCESS")) {
                                                                            opStatus = true;
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
                                    break;
                                case 13:
                                    opStatus = false;
                                    writeData = cardInfo.getTenthSlot();
                                    list = Utility.splitStringBySize(writeData, 32);
                                    if (list != null && list.size() == 3) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(0);
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            char[] data = rc522Comm.readRC522();
                                            String strData = new String(data);
                                            String[] arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(1);
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(2);
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    data = rc522Comm.readRC522();
                                                                    strData = new String(data);
                                                                    arr = strData.split(":");
                                                                    if (arr != null && arr.length == 3) {
                                                                        strData = arr[2].trim();
                                                                        if (strData.equals("SUCCESS")) {
                                                                            opStatus = true;
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
                                    break;
                                case 14:
                                    opStatus = false;
                                    writeData = cardInfo.getEleventhSlot();
                                    list = Utility.splitStringBySize(writeData, 32);
                                    if (list != null && list.size() == 3) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(0);
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            char[] data = rc522Comm.readRC522();
                                            String strData = new String(data);
                                            String[] arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(1);
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(2);
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    data = rc522Comm.readRC522();
                                                                    strData = new String(data);
                                                                    arr = strData.split(":");
                                                                    if (arr != null && arr.length == 3) {
                                                                        strData = arr[2].trim();
                                                                        if (strData.equals("SUCCESS")) {
                                                                            opStatus = true;
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
                                    break;
                                case 15:
                                    opStatus = false;
                                    writeData = cardInfo.getTwelvethSlot() + cardInfo.getSecondFingerNo() + cardInfo.getSecondFingerSecurityLevel() + cardInfo.getSecondFingerIndex() + cardInfo.getSecondFingerQuality() + "0" + cardInfo.getSecondFingerVerificationMode() + dummy;
                                    list = Utility.splitStringBySize(writeData, 32);
                                    if (list != null && list.size() == 3) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(0);
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            char[] data = rc522Comm.readRC522();
                                            String strData = new String(data);
                                            String[] arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(1);
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strKeyB + " " + Constants.RC522_KEY_TYPE_B + list.get(2);
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    data = rc522Comm.readRC522();
                                                                    strData = new String(data);
                                                                    arr = strData.split(":");
                                                                    if (arr != null && arr.length == 3) {
                                                                        strData = arr[2].trim();
                                                                        if (strData.equals("SUCCESS")) {
                                                                            opStatus = true;
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
                                    break;
                            }
                            if (!opStatus) {
                                break;
                            }
                        }
                        sectorKeyData.close();
                        if (opStatus) {
                            opStatus = false;
                            String strData_01 = "55010005000000004802480248024802";
                            String strData_02 = "48024802480248024802480248024802";
                            opStatus = updateMAD(strData_01, strData_02);
                        }
                    }

                    break;
            }
            return opStatus;
        }

        @Override
        protected void onPostExecute(Boolean status) {
            mypDialog.cancel();
            clear();
            btn_CardWrite.setEnabled(true);
            if (status) {
                showCustomAlertDialog(true, "Card Write Status", "Card Write Successful");
            } else {
                showCustomAlertDialog(false, "Card Write Status", "Failed To Write To Card");
            }
        }
    }

    private class AsyncTaskRC522CardWriteAndKeyUpdate extends AsyncTask <Void, Void, Boolean> {

        ProgressDialog mypDialog;
        RC522Communicator rc522Comm;
        SmartCardInfo cardInfo;

        AsyncTaskRC522CardWriteAndKeyUpdate(RC522Communicator rc522Comm, SmartCardInfo cardInfo) {
            this.rc522Comm = rc522Comm;
            this.cardInfo = cardInfo;
        }

        @Override
        protected void onPreExecute() {
            mypDialog = new ProgressDialog(SmartCardActivity.this);
            mypDialog.setMessage("Card Writing Wait...");
            mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mypDialog.setCanceledOnTouchOutside(false);
            mypDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            boolean status = false;
            boolean opStatus = false;
            ArrayList <String> list = null;
            String writeData = "";
            String dummyData = "000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
            int noOfTemplates = cardInfo.getNoOfTemplates();
            switch (noOfTemplates) {
                case 1:
                    Cursor sectorKeyData = null;
                    sectorKeyData = dbComm.getSectorAndKeyForWriteCard();
                    if (sectorKeyData != null) {
                        String strFinalKeyA = "303132333435";
                        String strInitialKeyB = "353433323130";
                        String strAccessCode = "7F0788C1";
                        String dummy = "00000000000000000000000000000000000000000000000000000000"; //dummy string for writing Sector 9 & F
                        String msg = "";
                        while (sectorKeyData.moveToNext()) {
                            String strSectorNo = sectorKeyData.getString(0).trim();
                            String strInitialKeyA = sectorKeyData.getString(1).trim();
                            String strFinalKeyB = sectorKeyData.getString(2).trim();
                            int sectorNo = Integer.parseInt(strSectorNo);
                            switch (sectorNo) {
                                case 0:
                                    opStatus = true;
                                    break;
                                case 1:
                                    opStatus = writeSectorOne(cardInfo.getCardId());
                                    break;
                                case 2:
                                    opStatus = false;
                                    writeData = cardInfo.getEmployeeId() + cardInfo.getEmpName() + cardInfo.getValidUpto() + cardInfo.getBirthDate() + cardInfo.getSiteCode() + cardInfo.getBloodGroup() + cardInfo.getSmartCardVer();
                                    writeData = Utility.asciiToHex(writeData);
                                    writeData = writeData.toUpperCase();
                                    list = Utility.splitStringBySize(writeData, 32);
                                    if (list != null && list.size() == 3) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(0);
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            sleep(Constants.DELEY);
                                            char[] data = rc522Comm.readRC522();
                                            String strData = new String(data);
                                            String[] arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(1);
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        sleep(Constants.DELEY);
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(2);
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    sleep(Constants.DELEY);
                                                                    data = rc522Comm.readRC522();
                                                                    strData = new String(data);
                                                                    arr = strData.split(":");
                                                                    if (arr != null && arr.length == 3) {
                                                                        strData = arr[2].trim();
                                                                        if (strData.equals("SUCCESS")) {
                                                                            writeData = Constants.RC522_CHANGE_KEY_COMMAND + " 11 " + strInitialKeyB + " B " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                                                            status = rc522Comm.writeRC522(writeData);
                                                                            if (status) {
                                                                                sleep(Constants.DELEY);
                                                                                char[] readData = rc522Comm.readRC522();
                                                                                if (readData != null && readData.length > 0) {
                                                                                    String strReadData = new String(readData);
                                                                                    strReadData = strReadData.trim();
                                                                                    arr = strReadData.split(":");
                                                                                    if (arr != null && arr.length == 3) {
                                                                                        strReadData = arr[2].trim();
                                                                                        Log.d("TEST", "Read:" + strReadData);
                                                                                        if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                                                            Log.d("TEST", "true");
                                                                                            opStatus = true;
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
                                                }
                                            }
                                        }
                                    }
                                    break;
                                case 3:
                                    opStatus = false;
                                    list = Utility.splitStringBySize(dummyData, 32);
                                    if (list != null && list.size() == 3) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(0);
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            sleep(Constants.DELEY);
                                            char[] data = rc522Comm.readRC522();
                                            String strData = new String(data);
                                            String[] arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(1);
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        sleep(Constants.DELEY);
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(2);
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    sleep(Constants.DELEY);
                                                                    data = rc522Comm.readRC522();
                                                                    strData = new String(data);
                                                                    arr = strData.split(":");
                                                                    if (arr != null && arr.length == 3) {
                                                                        strData = arr[2].trim();
                                                                        if (strData.equals("SUCCESS")) {
                                                                            writeData = Constants.RC522_CHANGE_KEY_COMMAND + " 15 " + strInitialKeyB + " B " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                                                            status = rc522Comm.writeRC522(writeData);
                                                                            if (status) {
                                                                                sleep(Constants.DELEY);
                                                                                char[] readData = rc522Comm.readRC522();
                                                                                if (readData != null && readData.length > 0) {
                                                                                    String strReadData = new String(readData);
                                                                                    strReadData = strReadData.trim();
                                                                                    arr = strReadData.split(":");
                                                                                    if (arr != null && arr.length == 3) {
                                                                                        strReadData = arr[2].trim();
                                                                                        Log.d("TEST", "Read:" + strReadData);
                                                                                        if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                                                            Log.d("TEST", "true");
                                                                                            opStatus = true;
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
                                                }
                                            }
                                        }
                                    }
                                    break;
                                case 4:
                                    opStatus = false;
                                    writeData = cardInfo.getFirstSlot();
                                    list = Utility.splitStringBySize(writeData, 32);
                                    if (list != null && list.size() == 3) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(0);
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            sleep(Constants.DELEY);
                                            char[] data = rc522Comm.readRC522();
                                            String strData = new String(data);
                                            String[] arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(1);
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        sleep(Constants.DELEY);
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(2);
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    sleep(Constants.DELEY);
                                                                    data = rc522Comm.readRC522();
                                                                    strData = new String(data);
                                                                    arr = strData.split(":");
                                                                    if (arr != null && arr.length == 3) {
                                                                        strData = arr[2].trim();
                                                                        if (strData.equals("SUCCESS")) {
                                                                            writeData = Constants.RC522_CHANGE_KEY_COMMAND + " 19 " + strInitialKeyB + " B " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                                                            status = rc522Comm.writeRC522(writeData);
                                                                            if (status) {
                                                                                sleep(Constants.DELEY);
                                                                                char[] readData = rc522Comm.readRC522();
                                                                                if (readData != null && readData.length > 0) {
                                                                                    String strReadData = new String(readData);
                                                                                    strReadData = strReadData.trim();
                                                                                    arr = strReadData.split(":");
                                                                                    if (arr != null && arr.length == 3) {
                                                                                        strReadData = arr[2].trim();
                                                                                        Log.d("TEST", "Read:" + strReadData);
                                                                                        if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                                                            Log.d("TEST", "true");
                                                                                            opStatus = true;
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
                                                }
                                            }
                                        }
                                    }
                                    break;
                                case 5:
                                    opStatus = false;
                                    writeData = cardInfo.getSecondSlot();
                                    list = Utility.splitStringBySize(writeData, 32);
                                    if (list != null && list.size() == 3) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(0);
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            sleep(Constants.DELEY);
                                            char[] data = rc522Comm.readRC522();
                                            String strData = new String(data);
                                            String[] arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(1);
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        sleep(Constants.DELEY);
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(2);
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    sleep(Constants.DELEY);
                                                                    data = rc522Comm.readRC522();
                                                                    strData = new String(data);
                                                                    arr = strData.split(":");
                                                                    if (arr != null && arr.length == 3) {
                                                                        strData = arr[2].trim();
                                                                        if (strData.equals("SUCCESS")) {
                                                                            writeData = Constants.RC522_CHANGE_KEY_COMMAND + " 23 " + strInitialKeyB + " B " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                                                            status = rc522Comm.writeRC522(writeData);
                                                                            if (status) {
                                                                                sleep(Constants.DELEY);
                                                                                char[] readData = rc522Comm.readRC522();
                                                                                if (readData != null && readData.length > 0) {
                                                                                    String strReadData = new String(readData);
                                                                                    strReadData = strReadData.trim();
                                                                                    arr = strReadData.split(":");
                                                                                    if (arr != null && arr.length == 3) {
                                                                                        strReadData = arr[2].trim();
                                                                                        Log.d("TEST", "Read:" + strReadData);
                                                                                        if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                                                            Log.d("TEST", "true");
                                                                                            opStatus = true;
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
                                                }
                                            }
                                        }
                                    }
                                    break;
                                case 6:
                                    opStatus = false;
                                    writeData = cardInfo.getThirdSlot();
                                    list = Utility.splitStringBySize(writeData, 32);
                                    if (list != null && list.size() == 3) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(0);
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            sleep(Constants.DELEY);
                                            char[] data = rc522Comm.readRC522();
                                            String strData = new String(data);
                                            String[] arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(1);
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        sleep(Constants.DELEY);
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(2);
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    sleep(Constants.DELEY);
                                                                    data = rc522Comm.readRC522();
                                                                    strData = new String(data);
                                                                    arr = strData.split(":");
                                                                    if (arr != null && arr.length == 3) {
                                                                        strData = arr[2].trim();
                                                                        if (strData.equals("SUCCESS")) {
                                                                            writeData = Constants.RC522_CHANGE_KEY_COMMAND + " 27 " + strInitialKeyB + " B " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                                                            status = rc522Comm.writeRC522(writeData);
                                                                            if (status) {
                                                                                sleep(Constants.DELEY);
                                                                                char[] readData = rc522Comm.readRC522();
                                                                                if (readData != null && readData.length > 0) {
                                                                                    String strReadData = new String(readData);
                                                                                    strReadData = strReadData.trim();
                                                                                    arr = strReadData.split(":");
                                                                                    if (arr != null && arr.length == 3) {
                                                                                        strReadData = arr[2].trim();
                                                                                        Log.d("TEST", "Read:" + strReadData);
                                                                                        if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                                                            Log.d("TEST", "true");
                                                                                            opStatus = true;
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
                                                }
                                            }
                                        }
                                    }
                                    break;
                                case 7:
                                    opStatus = false;
                                    writeData = cardInfo.getForthSlot();
                                    list = Utility.splitStringBySize(writeData, 32);
                                    if (list != null && list.size() == 3) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(0);
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            sleep(Constants.DELEY);
                                            char[] data = rc522Comm.readRC522();
                                            String strData = new String(data);
                                            String[] arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(1);
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        sleep(Constants.DELEY);
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(2);
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    sleep(Constants.DELEY);
                                                                    data = rc522Comm.readRC522();
                                                                    strData = new String(data);
                                                                    arr = strData.split(":");
                                                                    if (arr != null && arr.length == 3) {
                                                                        strData = arr[2].trim();
                                                                        if (strData.equals("SUCCESS")) {
                                                                            writeData = Constants.RC522_CHANGE_KEY_COMMAND + " 31 " + strInitialKeyB + " B " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                                                            status = rc522Comm.writeRC522(writeData);
                                                                            if (status) {
                                                                                sleep(Constants.DELEY);
                                                                                char[] readData = rc522Comm.readRC522();
                                                                                if (readData != null && readData.length > 0) {
                                                                                    String strReadData = new String(readData);
                                                                                    strReadData = strReadData.trim();
                                                                                    arr = strReadData.split(":");
                                                                                    if (arr != null && arr.length == 3) {
                                                                                        strReadData = arr[2].trim();
                                                                                        Log.d("TEST", "Read:" + strReadData);
                                                                                        if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                                                            Log.d("TEST", "true");
                                                                                            opStatus = true;
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
                                                }
                                            }
                                        }
                                    }
                                    break;
                                case 8:
                                    opStatus = false;
                                    writeData = cardInfo.getFifthSlot();
                                    list = Utility.splitStringBySize(writeData, 32);
                                    if (list != null && list.size() == 3) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(0);
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            sleep(Constants.DELEY);
                                            char[] data = rc522Comm.readRC522();
                                            String strData = new String(data);
                                            String[] arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(1);
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        sleep(Constants.DELEY);
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(2);
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    sleep(Constants.DELEY);
                                                                    data = rc522Comm.readRC522();
                                                                    strData = new String(data);
                                                                    arr = strData.split(":");
                                                                    if (arr != null && arr.length == 3) {
                                                                        strData = arr[2].trim();
                                                                        if (strData.equals("SUCCESS")) {
                                                                            writeData = Constants.RC522_CHANGE_KEY_COMMAND + " 35 " + strInitialKeyB + " B " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                                                            status = rc522Comm.writeRC522(writeData);
                                                                            if (status) {
                                                                                sleep(Constants.DELEY);
                                                                                char[] readData = rc522Comm.readRC522();
                                                                                if (readData != null && readData.length > 0) {
                                                                                    String strReadData = new String(readData);
                                                                                    strReadData = strReadData.trim();
                                                                                    arr = strReadData.split(":");
                                                                                    if (arr != null && arr.length == 3) {
                                                                                        strReadData = arr[2].trim();
                                                                                        Log.d("TEST", "Read:" + strReadData);
                                                                                        if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                                                            Log.d("TEST", "true");
                                                                                            opStatus = true;
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
                                                }
                                            }
                                        }
                                    }
                                    break;
                                case 9:
                                    opStatus = false;
                                    writeData = cardInfo.getSixthSlot() + cardInfo.getFirstFingerNo() + cardInfo.getFirstFingerSecurityLevel() + cardInfo.getFirstFingerIndex() + cardInfo.getFirstFingerQuality() + "0" + cardInfo.getFirstFingerVerificationMode() + dummy;
                                    list = Utility.splitStringBySize(writeData, 32);
                                    if (list != null && list.size() == 3) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(0);
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            sleep(Constants.DELEY);
                                            char[] data = rc522Comm.readRC522();
                                            String strData = new String(data);
                                            String[] arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(1);
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        sleep(Constants.DELEY);
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(2);
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    sleep(Constants.DELEY);
                                                                    data = rc522Comm.readRC522();
                                                                    strData = new String(data);
                                                                    arr = strData.split(":");
                                                                    if (arr != null && arr.length == 3) {
                                                                        strData = arr[2].trim();
                                                                        if (strData.equals("SUCCESS")) {
                                                                            writeData = Constants.RC522_CHANGE_KEY_COMMAND + " 39 " + strInitialKeyB + " B " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                                                            status = rc522Comm.writeRC522(writeData);
                                                                            if (status) {
                                                                                sleep(Constants.DELEY);
                                                                                char[] readData = rc522Comm.readRC522();
                                                                                if (readData != null && readData.length > 0) {
                                                                                    String strReadData = new String(readData);
                                                                                    strReadData = strReadData.trim();
                                                                                    arr = strReadData.split(":");
                                                                                    if (arr != null && arr.length == 3) {
                                                                                        strReadData = arr[2].trim();
                                                                                        Log.d("TEST", "Read:" + strReadData);
                                                                                        if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                                                            Log.d("TEST", "true");
                                                                                            opStatus = true;
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
                                                }
                                            }
                                        }
                                    }
                                    break;
                                case 10:
                                    opStatus = false;
                                    list = Utility.splitStringBySize(dummyData, 32);
                                    if (list != null && list.size() == 3) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(0);
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            sleep(Constants.DELEY);
                                            char[] data = rc522Comm.readRC522();
                                            String strData = new String(data);
                                            String[] arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(1);
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        sleep(Constants.DELEY);
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(2);
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    sleep(Constants.DELEY);
                                                                    data = rc522Comm.readRC522();
                                                                    strData = new String(data);
                                                                    arr = strData.split(":");
                                                                    if (arr != null && arr.length == 3) {
                                                                        strData = arr[2].trim();
                                                                        if (strData.equals("SUCCESS")) {
                                                                            writeData = Constants.RC522_CHANGE_KEY_COMMAND + " 43 " + strInitialKeyB + " B " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                                                            status = rc522Comm.writeRC522(writeData);
                                                                            if (status) {
                                                                                sleep(Constants.DELEY);
                                                                                char[] readData = rc522Comm.readRC522();
                                                                                if (readData != null && readData.length > 0) {
                                                                                    String strReadData = new String(readData);
                                                                                    strReadData = strReadData.trim();
                                                                                    arr = strReadData.split(":");
                                                                                    if (arr != null && arr.length == 3) {
                                                                                        strReadData = arr[2].trim();
                                                                                        Log.d("TEST", "Read:" + strReadData);
                                                                                        if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                                                            Log.d("TEST", "true");
                                                                                            opStatus = true;
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
                                                }
                                            }
                                        }
                                    }
                                    break;
                                case 11:
                                    opStatus = false;
                                    list = Utility.splitStringBySize(dummyData, 32);
                                    if (list != null && list.size() == 3) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(0);
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            sleep(Constants.DELEY);
                                            char[] data = rc522Comm.readRC522();
                                            String strData = new String(data);
                                            String[] arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(1);
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        sleep(Constants.DELEY);
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(2);
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    sleep(Constants.DELEY);
                                                                    data = rc522Comm.readRC522();
                                                                    strData = new String(data);
                                                                    arr = strData.split(":");
                                                                    if (arr != null && arr.length == 3) {
                                                                        strData = arr[2].trim();
                                                                        if (strData.equals("SUCCESS")) {
                                                                            writeData = Constants.RC522_CHANGE_KEY_COMMAND + " 47 " + strInitialKeyB + " B " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                                                            status = rc522Comm.writeRC522(writeData);
                                                                            if (status) {
                                                                                sleep(Constants.DELEY);
                                                                                char[] readData = rc522Comm.readRC522();
                                                                                if (readData != null && readData.length > 0) {
                                                                                    String strReadData = new String(readData);
                                                                                    strReadData = strReadData.trim();
                                                                                    arr = strReadData.split(":");
                                                                                    if (arr != null && arr.length == 3) {
                                                                                        strReadData = arr[2].trim();
                                                                                        Log.d("TEST", "Read:" + strReadData);
                                                                                        if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                                                            Log.d("TEST", "true");
                                                                                            opStatus = true;
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
                                                }
                                            }
                                        }
                                    }
                                    break;
                                case 12:
                                    opStatus = false;
                                    list = Utility.splitStringBySize(dummyData, 32);
                                    if (list != null && list.size() == 3) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(0);
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            sleep(Constants.DELEY);
                                            char[] data = rc522Comm.readRC522();
                                            String strData = new String(data);
                                            String[] arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(1);
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        sleep(Constants.DELEY);
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(2);
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    sleep(Constants.DELEY);
                                                                    data = rc522Comm.readRC522();
                                                                    strData = new String(data);
                                                                    arr = strData.split(":");
                                                                    if (arr != null && arr.length == 3) {
                                                                        strData = arr[2].trim();
                                                                        if (strData.equals("SUCCESS")) {
                                                                            writeData = Constants.RC522_CHANGE_KEY_COMMAND + " 51 " + strInitialKeyB + " B " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                                                            status = rc522Comm.writeRC522(writeData);
                                                                            if (status) {
                                                                                sleep(Constants.DELEY);
                                                                                char[] readData = rc522Comm.readRC522();
                                                                                if (readData != null && readData.length > 0) {
                                                                                    String strReadData = new String(readData);
                                                                                    strReadData = strReadData.trim();
                                                                                    arr = strReadData.split(":");
                                                                                    if (arr != null && arr.length == 3) {
                                                                                        strReadData = arr[2].trim();
                                                                                        Log.d("TEST", "Read:" + strReadData);
                                                                                        if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                                                            Log.d("TEST", "true");
                                                                                            opStatus = true;
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
                                                }
                                            }
                                        }
                                    }
                                    break;
                                case 13:
                                    opStatus = false;
                                    list = Utility.splitStringBySize(dummyData, 32);
                                    if (list != null && list.size() == 3) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(0);
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            sleep(Constants.DELEY);
                                            char[] data = rc522Comm.readRC522();
                                            String strData = new String(data);
                                            String[] arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(1);
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        sleep(Constants.DELEY);
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(2);
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    sleep(Constants.DELEY);
                                                                    data = rc522Comm.readRC522();
                                                                    strData = new String(data);
                                                                    arr = strData.split(":");
                                                                    if (arr != null && arr.length == 3) {
                                                                        strData = arr[2].trim();
                                                                        if (strData.equals("SUCCESS")) {
                                                                            writeData = Constants.RC522_CHANGE_KEY_COMMAND + " 55 " + strInitialKeyB + " B " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                                                            status = rc522Comm.writeRC522(writeData);
                                                                            if (status) {
                                                                                sleep(Constants.DELEY);
                                                                                char[] readData = rc522Comm.readRC522();
                                                                                if (readData != null && readData.length > 0) {
                                                                                    String strReadData = new String(readData);
                                                                                    strReadData = strReadData.trim();
                                                                                    arr = strReadData.split(":");
                                                                                    if (arr != null && arr.length == 3) {
                                                                                        strReadData = arr[2].trim();
                                                                                        Log.d("TEST", "Read:" + strReadData);
                                                                                        if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                                                            Log.d("TEST", "true");
                                                                                            opStatus = true;
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
                                                }
                                            }
                                        }
                                    }
                                    break;
                                case 14:
                                    opStatus = false;
                                    list = Utility.splitStringBySize(dummyData, 32);
                                    if (list != null && list.size() == 3) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(0);
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            sleep(Constants.DELEY);
                                            char[] data = rc522Comm.readRC522();
                                            String strData = new String(data);
                                            String[] arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(1);
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        sleep(Constants.DELEY);
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(2);
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    sleep(Constants.DELEY);
                                                                    data = rc522Comm.readRC522();
                                                                    strData = new String(data);
                                                                    arr = strData.split(":");
                                                                    if (arr != null && arr.length == 3) {
                                                                        strData = arr[2].trim();
                                                                        if (strData.equals("SUCCESS")) {
                                                                            writeData = Constants.RC522_CHANGE_KEY_COMMAND + " 59 " + strInitialKeyB + " B " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                                                            status = rc522Comm.writeRC522(writeData);
                                                                            if (status) {
                                                                                sleep(Constants.DELEY);
                                                                                char[] readData = rc522Comm.readRC522();
                                                                                if (readData != null && readData.length > 0) {
                                                                                    String strReadData = new String(readData);
                                                                                    strReadData = strReadData.trim();
                                                                                    arr = strReadData.split(":");
                                                                                    if (arr != null && arr.length == 3) {
                                                                                        strReadData = arr[2].trim();
                                                                                        Log.d("TEST", "Read:" + strReadData);
                                                                                        if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                                                            Log.d("TEST", "true");
                                                                                            opStatus = true;
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
                                                }
                                            }
                                        }
                                    }
                                    break;
                                case 15:
                                    opStatus = false;
                                    list = Utility.splitStringBySize(dummyData, 32);
                                    if (list != null && list.size() == 3) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(0);
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            sleep(Constants.DELEY);
                                            char[] data = rc522Comm.readRC522();
                                            String strData = new String(data);
                                            String[] arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(1);
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        sleep(Constants.DELEY);
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(2);
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    sleep(Constants.DELEY);
                                                                    data = rc522Comm.readRC522();
                                                                    strData = new String(data);
                                                                    arr = strData.split(":");
                                                                    if (arr != null && arr.length == 3) {
                                                                        strData = arr[2].trim();
                                                                        if (strData.equals("SUCCESS")) {
                                                                            writeData = Constants.RC522_CHANGE_KEY_COMMAND + " 63 " + strInitialKeyB + " B " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                                                            status = rc522Comm.writeRC522(writeData);
                                                                            if (status) {
                                                                                sleep(Constants.DELEY);
                                                                                char[] readData = rc522Comm.readRC522();
                                                                                if (readData != null && readData.length > 0) {
                                                                                    String strReadData = new String(readData);
                                                                                    strReadData = strReadData.trim();
                                                                                    arr = strReadData.split(":");
                                                                                    if (arr != null && arr.length == 3) {
                                                                                        strReadData = arr[2].trim();
                                                                                        Log.d("TEST", "Read:" + strReadData);
                                                                                        if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                                                            Log.d("TEST", "true");
                                                                                            opStatus = true;
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
                                                }
                                            }
                                        }
                                    }
                                    break;
                            }
                            if (!opStatus) {
                                break;
                            }
                        }

                        sectorKeyData.close();

                        //============================== MAD UPDATE =================================//

                        if (opStatus) {
                            opStatus = false;
                            String strData_01 = "55010005000000004802480248024802";
                            String strData_02 = "48024802000000000000000000000000";
                            opStatus = updateMAD(strData_01, strData_02);
                        }
                    }
                    break;
                case 2:
                    sectorKeyData = dbComm.getSectorAndKeyForWriteCard();
                    if (sectorKeyData != null) {
                        String strFinalKeyA = "303132333435";
                        String strInitialKeyB = "353433323130";
                        String strAccessCode = "7F0788C1";
                        String dummy = "00000000000000000000000000000000000000000000000000000000"; //dummy string for writing Sector 9 & F
                        String msg = "";
                        while (sectorKeyData.moveToNext()) {
                            String strSectorNo = sectorKeyData.getString(0).trim();
                            String strInitialKeyA = sectorKeyData.getString(1).trim();
                            String strFinalKeyB = sectorKeyData.getString(2).trim();
                            int sectorNo = Integer.parseInt(strSectorNo);
                            switch (sectorNo) {
                                case 0:
                                    opStatus = true;
                                    break;
                                case 1:
                                    opStatus = writeSectorOne(cardInfo.getCardId());
                                    break;
                                case 2:
                                    opStatus = false;
                                    writeData = cardInfo.getEmployeeId() + cardInfo.getEmpName() + cardInfo.getValidUpto() + cardInfo.getBirthDate() + cardInfo.getSiteCode() + cardInfo.getBloodGroup() + cardInfo.getSmartCardVer();
                                    writeData = Utility.asciiToHex(writeData);
                                    writeData = writeData.toUpperCase();
                                    list = Utility.splitStringBySize(writeData, 32);
                                    if (list != null && list.size() == 3) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(0);
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            sleep(Constants.DELEY);
                                            char[] data = rc522Comm.readRC522();
                                            String strData = new String(data);
                                            String[] arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(1);
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        sleep(Constants.DELEY);
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(2);
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    sleep(Constants.DELEY);
                                                                    data = rc522Comm.readRC522();
                                                                    strData = new String(data);
                                                                    arr = strData.split(":");
                                                                    if (arr != null && arr.length == 3) {
                                                                        strData = arr[2].trim();
                                                                        if (strData.equals("SUCCESS")) {
                                                                            writeData = Constants.RC522_CHANGE_KEY_COMMAND + " 11 " + strInitialKeyB + " B " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                                                            status = rc522Comm.writeRC522(writeData);
                                                                            if (status) {
                                                                                sleep(Constants.DELEY);
                                                                                char[] readData = rc522Comm.readRC522();
                                                                                if (readData != null && readData.length > 0) {
                                                                                    String strReadData = new String(readData);
                                                                                    strReadData = strReadData.trim();
                                                                                    arr = strReadData.split(":");
                                                                                    if (arr != null && arr.length == 3) {
                                                                                        strReadData = arr[2].trim();
                                                                                        Log.d("TEST", "Read:" + strReadData);
                                                                                        if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                                                            Log.d("TEST", "true");
                                                                                            opStatus = true;
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
                                                }
                                            }
                                        }
                                    }
                                    break;
                                case 3:
                                    opStatus = false;
                                    list = Utility.splitStringBySize(dummyData, 32);
                                    if (list != null && list.size() == 3) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(0);
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            sleep(Constants.DELEY);
                                            char[] data = rc522Comm.readRC522();
                                            String strData = new String(data);
                                            String[] arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(1);
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        sleep(Constants.DELEY);
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(2);
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    sleep(Constants.DELEY);
                                                                    data = rc522Comm.readRC522();
                                                                    strData = new String(data);
                                                                    arr = strData.split(":");
                                                                    if (arr != null && arr.length == 3) {
                                                                        strData = arr[2].trim();
                                                                        if (strData.equals("SUCCESS")) {
                                                                            writeData = Constants.RC522_CHANGE_KEY_COMMAND + " 15 " + strInitialKeyB + " B " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                                                            status = rc522Comm.writeRC522(writeData);
                                                                            if (status) {
                                                                                sleep(Constants.DELEY);
                                                                                char[] readData = rc522Comm.readRC522();
                                                                                if (readData != null && readData.length > 0) {
                                                                                    String strReadData = new String(readData);
                                                                                    strReadData = strReadData.trim();
                                                                                    arr = strReadData.split(":");
                                                                                    if (arr != null && arr.length == 3) {
                                                                                        strReadData = arr[2].trim();
                                                                                        Log.d("TEST", "Read:" + strReadData);
                                                                                        if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                                                            Log.d("TEST", "true");
                                                                                            opStatus = true;
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
                                                }
                                            }
                                        }
                                    }
                                    break;
                                case 4:
                                    opStatus = false;
                                    writeData = cardInfo.getFirstSlot();
                                    list = Utility.splitStringBySize(writeData, 32);
                                    if (list != null && list.size() == 3) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(0);
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            sleep(Constants.DELEY);
                                            char[] data = rc522Comm.readRC522();
                                            String strData = new String(data);
                                            String[] arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(1);
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        sleep(Constants.DELEY);
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(2);
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    sleep(Constants.DELEY);
                                                                    data = rc522Comm.readRC522();
                                                                    strData = new String(data);
                                                                    arr = strData.split(":");
                                                                    if (arr != null && arr.length == 3) {
                                                                        strData = arr[2].trim();
                                                                        if (strData.equals("SUCCESS")) {
                                                                            writeData = Constants.RC522_CHANGE_KEY_COMMAND + " 19 " + strInitialKeyB + " B " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                                                            status = rc522Comm.writeRC522(writeData);
                                                                            if (status) {
                                                                                sleep(Constants.DELEY);
                                                                                char[] readData = rc522Comm.readRC522();
                                                                                if (readData != null && readData.length > 0) {
                                                                                    String strReadData = new String(readData);
                                                                                    strReadData = strReadData.trim();
                                                                                    arr = strReadData.split(":");
                                                                                    if (arr != null && arr.length == 3) {
                                                                                        strReadData = arr[2].trim();
                                                                                        Log.d("TEST", "Read:" + strReadData);
                                                                                        if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                                                            Log.d("TEST", "true");
                                                                                            opStatus = true;
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
                                                }
                                            }
                                        }
                                    }
                                    break;
                                case 5:
                                    opStatus = false;
                                    writeData = cardInfo.getSecondSlot();
                                    list = Utility.splitStringBySize(writeData, 32);
                                    if (list != null && list.size() == 3) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(0);
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            sleep(Constants.DELEY);
                                            char[] data = rc522Comm.readRC522();
                                            String strData = new String(data);
                                            String[] arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(1);
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        sleep(Constants.DELEY);
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(2);
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    sleep(Constants.DELEY);
                                                                    data = rc522Comm.readRC522();
                                                                    strData = new String(data);
                                                                    arr = strData.split(":");
                                                                    if (arr != null && arr.length == 3) {
                                                                        strData = arr[2].trim();
                                                                        if (strData.equals("SUCCESS")) {
                                                                            writeData = Constants.RC522_CHANGE_KEY_COMMAND + " 23 " + strInitialKeyB + " B " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                                                            status = rc522Comm.writeRC522(writeData);
                                                                            if (status) {
                                                                                sleep(Constants.DELEY);
                                                                                char[] readData = rc522Comm.readRC522();
                                                                                if (readData != null && readData.length > 0) {
                                                                                    String strReadData = new String(readData);
                                                                                    strReadData = strReadData.trim();
                                                                                    arr = strReadData.split(":");
                                                                                    if (arr != null && arr.length == 3) {
                                                                                        strReadData = arr[2].trim();
                                                                                        Log.d("TEST", "Read:" + strReadData);
                                                                                        if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                                                            Log.d("TEST", "true");
                                                                                            opStatus = true;
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
                                                }
                                            }
                                        }
                                    }
                                    break;
                                case 6:
                                    opStatus = false;
                                    writeData = cardInfo.getThirdSlot();
                                    list = Utility.splitStringBySize(writeData, 32);
                                    if (list != null && list.size() == 3) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(0);
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            sleep(Constants.DELEY);
                                            char[] data = rc522Comm.readRC522();
                                            String strData = new String(data);
                                            String[] arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(1);
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        sleep(Constants.DELEY);
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(2);
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    sleep(Constants.DELEY);
                                                                    data = rc522Comm.readRC522();
                                                                    strData = new String(data);
                                                                    arr = strData.split(":");
                                                                    if (arr != null && arr.length == 3) {
                                                                        strData = arr[2].trim();
                                                                        if (strData.equals("SUCCESS")) {
                                                                            writeData = Constants.RC522_CHANGE_KEY_COMMAND + " 27 " + strInitialKeyB + " B " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                                                            status = rc522Comm.writeRC522(writeData);
                                                                            if (status) {
                                                                                sleep(Constants.DELEY);
                                                                                char[] readData = rc522Comm.readRC522();
                                                                                if (readData != null && readData.length > 0) {
                                                                                    String strReadData = new String(readData);
                                                                                    strReadData = strReadData.trim();
                                                                                    arr = strReadData.split(":");
                                                                                    if (arr != null && arr.length == 3) {
                                                                                        strReadData = arr[2].trim();
                                                                                        Log.d("TEST", "Read:" + strReadData);
                                                                                        if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                                                            Log.d("TEST", "true");
                                                                                            opStatus = true;
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
                                                }
                                            }
                                        }
                                    }
                                    break;
                                case 7:
                                    opStatus = false;
                                    writeData = cardInfo.getForthSlot();
                                    list = Utility.splitStringBySize(writeData, 32);
                                    if (list != null && list.size() == 3) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(0);
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            sleep(Constants.DELEY);
                                            char[] data = rc522Comm.readRC522();
                                            String strData = new String(data);
                                            String[] arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(1);
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        sleep(Constants.DELEY);
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(2);
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    sleep(Constants.DELEY);
                                                                    data = rc522Comm.readRC522();
                                                                    strData = new String(data);
                                                                    arr = strData.split(":");
                                                                    if (arr != null && arr.length == 3) {
                                                                        strData = arr[2].trim();
                                                                        if (strData.equals("SUCCESS")) {
                                                                            writeData = Constants.RC522_CHANGE_KEY_COMMAND + " 31 " + strInitialKeyB + " B " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                                                            status = rc522Comm.writeRC522(writeData);
                                                                            if (status) {
                                                                                sleep(Constants.DELEY);
                                                                                char[] readData = rc522Comm.readRC522();
                                                                                if (readData != null && readData.length > 0) {
                                                                                    String strReadData = new String(readData);
                                                                                    strReadData = strReadData.trim();
                                                                                    arr = strReadData.split(":");
                                                                                    if (arr != null && arr.length == 3) {
                                                                                        strReadData = arr[2].trim();
                                                                                        Log.d("TEST", "Read:" + strReadData);
                                                                                        if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                                                            Log.d("TEST", "true");
                                                                                            opStatus = true;
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
                                                }
                                            }
                                        }
                                    }
                                    break;
                                case 8:
                                    opStatus = false;
                                    writeData = cardInfo.getFifthSlot();
                                    list = Utility.splitStringBySize(writeData, 32);
                                    if (list != null && list.size() == 3) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(0);
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            sleep(Constants.DELEY);
                                            char[] data = rc522Comm.readRC522();
                                            String strData = new String(data);
                                            String[] arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(1);
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        sleep(Constants.DELEY);
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(2);
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    sleep(Constants.DELEY);
                                                                    data = rc522Comm.readRC522();
                                                                    strData = new String(data);
                                                                    arr = strData.split(":");
                                                                    if (arr != null && arr.length == 3) {
                                                                        strData = arr[2].trim();
                                                                        if (strData.equals("SUCCESS")) {
                                                                            writeData = Constants.RC522_CHANGE_KEY_COMMAND + " 35 " + strInitialKeyB + " B " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                                                            status = rc522Comm.writeRC522(writeData);
                                                                            if (status) {
                                                                                sleep(Constants.DELEY);
                                                                                char[] readData = rc522Comm.readRC522();
                                                                                if (readData != null && readData.length > 0) {
                                                                                    String strReadData = new String(readData);
                                                                                    strReadData = strReadData.trim();
                                                                                    arr = strReadData.split(":");
                                                                                    if (arr != null && arr.length == 3) {
                                                                                        strReadData = arr[2].trim();
                                                                                        Log.d("TEST", "Read:" + strReadData);
                                                                                        if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                                                            Log.d("TEST", "true");
                                                                                            opStatus = true;
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
                                                }
                                            }
                                        }
                                    }
                                    break;
                                case 9:
                                    opStatus = false;
                                    writeData = cardInfo.getSixthSlot() + cardInfo.getFirstFingerNo() + cardInfo.getFirstFingerSecurityLevel() + cardInfo.getFirstFingerIndex() + cardInfo.getFirstFingerQuality() + "0" + cardInfo.getFirstFingerVerificationMode() + dummy;
                                    list = Utility.splitStringBySize(writeData, 32);
                                    if (list != null && list.size() == 3) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(0);
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            sleep(Constants.DELEY);
                                            char[] data = rc522Comm.readRC522();
                                            String strData = new String(data);
                                            String[] arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(1);
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        sleep(Constants.DELEY);
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(2);
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    sleep(Constants.DELEY);
                                                                    data = rc522Comm.readRC522();
                                                                    strData = new String(data);
                                                                    arr = strData.split(":");
                                                                    if (arr != null && arr.length == 3) {
                                                                        strData = arr[2].trim();
                                                                        if (strData.equals("SUCCESS")) {
                                                                            writeData = Constants.RC522_CHANGE_KEY_COMMAND + " 39 " + strInitialKeyB + " B " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                                                            status = rc522Comm.writeRC522(writeData);
                                                                            if (status) {
                                                                                sleep(Constants.DELEY);
                                                                                char[] readData = rc522Comm.readRC522();
                                                                                if (readData != null && readData.length > 0) {
                                                                                    String strReadData = new String(readData);
                                                                                    strReadData = strReadData.trim();
                                                                                    arr = strReadData.split(":");
                                                                                    if (arr != null && arr.length == 3) {
                                                                                        strReadData = arr[2].trim();
                                                                                        Log.d("TEST", "Read:" + strReadData);
                                                                                        if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                                                            Log.d("TEST", "true");
                                                                                            opStatus = true;
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
                                                }
                                            }
                                        }
                                    }
                                    break;
                                case 10:
                                    opStatus = false;
                                    writeData = cardInfo.getSeventhSlot();
                                    list = Utility.splitStringBySize(writeData, 32);
                                    if (list != null && list.size() == 3) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(0);
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            sleep(Constants.DELEY);
                                            char[] data = rc522Comm.readRC522();
                                            String strData = new String(data);
                                            String[] arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(1);
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        sleep(Constants.DELEY);
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(2);
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    sleep(Constants.DELEY);
                                                                    data = rc522Comm.readRC522();
                                                                    strData = new String(data);
                                                                    arr = strData.split(":");
                                                                    if (arr != null && arr.length == 3) {
                                                                        strData = arr[2].trim();
                                                                        if (strData.equals("SUCCESS")) {
                                                                            writeData = Constants.RC522_CHANGE_KEY_COMMAND + " 43 " + strInitialKeyB + " B " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                                                            status = rc522Comm.writeRC522(writeData);
                                                                            if (status) {
                                                                                sleep(Constants.DELEY);
                                                                                char[] readData = rc522Comm.readRC522();
                                                                                if (readData != null && readData.length > 0) {
                                                                                    String strReadData = new String(readData);
                                                                                    strReadData = strReadData.trim();
                                                                                    arr = strReadData.split(":");
                                                                                    if (arr != null && arr.length == 3) {
                                                                                        strReadData = arr[2].trim();
                                                                                        Log.d("TEST", "Read:" + strReadData);
                                                                                        if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                                                            Log.d("TEST", "true");
                                                                                            opStatus = true;
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
                                                }
                                            }
                                        }
                                    }
                                    break;
                                case 11:
                                    opStatus = false;
                                    writeData = cardInfo.getEighthSlot();
                                    list = Utility.splitStringBySize(writeData, 32);
                                    if (list != null && list.size() == 3) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(0);
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            sleep(Constants.DELEY);
                                            char[] data = rc522Comm.readRC522();
                                            String strData = new String(data);
                                            String[] arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(1);
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        sleep(Constants.DELEY);
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(2);
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    sleep(Constants.DELEY);
                                                                    data = rc522Comm.readRC522();
                                                                    strData = new String(data);
                                                                    arr = strData.split(":");
                                                                    if (arr != null && arr.length == 3) {
                                                                        strData = arr[2].trim();
                                                                        if (strData.equals("SUCCESS")) {
                                                                            writeData = Constants.RC522_CHANGE_KEY_COMMAND + " 47 " + strInitialKeyB + " B " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                                                            status = rc522Comm.writeRC522(writeData);
                                                                            if (status) {
                                                                                sleep(Constants.DELEY);
                                                                                char[] readData = rc522Comm.readRC522();
                                                                                if (readData != null && readData.length > 0) {
                                                                                    String strReadData = new String(readData);
                                                                                    strReadData = strReadData.trim();
                                                                                    arr = strReadData.split(":");
                                                                                    if (arr != null && arr.length == 3) {
                                                                                        strReadData = arr[2].trim();
                                                                                        Log.d("TEST", "Read:" + strReadData);
                                                                                        if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                                                            Log.d("TEST", "true");
                                                                                            opStatus = true;
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
                                                }
                                            }
                                        }
                                    }
                                    break;
                                case 12:
                                    opStatus = false;
                                    writeData = cardInfo.getNinethSlot();
                                    list = Utility.splitStringBySize(writeData, 32);
                                    if (list != null && list.size() == 3) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(0);
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            sleep(Constants.DELEY);
                                            char[] data = rc522Comm.readRC522();
                                            String strData = new String(data);
                                            String[] arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(1);
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        sleep(Constants.DELEY);
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(2);
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    sleep(Constants.DELEY);
                                                                    data = rc522Comm.readRC522();
                                                                    strData = new String(data);
                                                                    arr = strData.split(":");
                                                                    if (arr != null && arr.length == 3) {
                                                                        strData = arr[2].trim();
                                                                        if (strData.equals("SUCCESS")) {
                                                                            writeData = Constants.RC522_CHANGE_KEY_COMMAND + " 51 " + strInitialKeyB + " B " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                                                            status = rc522Comm.writeRC522(writeData);
                                                                            if (status) {
                                                                                sleep(Constants.DELEY);
                                                                                char[] readData = rc522Comm.readRC522();
                                                                                if (readData != null && readData.length > 0) {
                                                                                    String strReadData = new String(readData);
                                                                                    strReadData = strReadData.trim();
                                                                                    arr = strReadData.split(":");
                                                                                    if (arr != null && arr.length == 3) {
                                                                                        strReadData = arr[2].trim();
                                                                                        Log.d("TEST", "Read:" + strReadData);
                                                                                        if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                                                            Log.d("TEST", "true");
                                                                                            opStatus = true;
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
                                                }
                                            }
                                        }
                                    }
                                    break;
                                case 13:
                                    opStatus = false;
                                    writeData = cardInfo.getTenthSlot();
                                    list = Utility.splitStringBySize(writeData, 32);
                                    if (list != null && list.size() == 3) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(0);
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            sleep(Constants.DELEY);
                                            char[] data = rc522Comm.readRC522();
                                            String strData = new String(data);
                                            String[] arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(1);
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        sleep(Constants.DELEY);
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(2);
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    sleep(Constants.DELEY);
                                                                    data = rc522Comm.readRC522();
                                                                    strData = new String(data);
                                                                    arr = strData.split(":");
                                                                    if (arr != null && arr.length == 3) {
                                                                        strData = arr[2].trim();
                                                                        if (strData.equals("SUCCESS")) {
                                                                            writeData = Constants.RC522_CHANGE_KEY_COMMAND + " 55 " + strInitialKeyB + " B " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                                                            status = rc522Comm.writeRC522(writeData);
                                                                            if (status) {
                                                                                sleep(Constants.DELEY);
                                                                                char[] readData = rc522Comm.readRC522();
                                                                                if (readData != null && readData.length > 0) {
                                                                                    String strReadData = new String(readData);
                                                                                    strReadData = strReadData.trim();
                                                                                    arr = strReadData.split(":");
                                                                                    if (arr != null && arr.length == 3) {
                                                                                        strReadData = arr[2].trim();
                                                                                        Log.d("TEST", "Read:" + strReadData);
                                                                                        if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                                                            Log.d("TEST", "true");
                                                                                            opStatus = true;
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
                                                }
                                            }
                                        }
                                    }
                                    break;
                                case 14:
                                    opStatus = false;
                                    writeData = cardInfo.getEleventhSlot();
                                    list = Utility.splitStringBySize(writeData, 32);
                                    if (list != null && list.size() == 3) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(0);
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            sleep(Constants.DELEY);
                                            char[] data = rc522Comm.readRC522();
                                            String strData = new String(data);
                                            String[] arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(1);
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        sleep(Constants.DELEY);
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(2);
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    sleep(Constants.DELEY);
                                                                    data = rc522Comm.readRC522();
                                                                    strData = new String(data);
                                                                    arr = strData.split(":");
                                                                    if (arr != null && arr.length == 3) {
                                                                        strData = arr[2].trim();
                                                                        if (strData.equals("SUCCESS")) {
                                                                            writeData = Constants.RC522_CHANGE_KEY_COMMAND + " 59 " + strInitialKeyB + " B " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                                                            status = rc522Comm.writeRC522(writeData);
                                                                            if (status) {
                                                                                sleep(Constants.DELEY);
                                                                                char[] readData = rc522Comm.readRC522();
                                                                                if (readData != null && readData.length > 0) {
                                                                                    String strReadData = new String(readData);
                                                                                    strReadData = strReadData.trim();
                                                                                    arr = strReadData.split(":");
                                                                                    if (arr != null && arr.length == 3) {
                                                                                        strReadData = arr[2].trim();
                                                                                        Log.d("TEST", "Read:" + strReadData);
                                                                                        if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                                                            Log.d("TEST", "true");
                                                                                            opStatus = true;
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
                                                }
                                            }
                                        }
                                    }
                                    break;
                                case 15:
                                    opStatus = false;
                                    writeData = cardInfo.getTwelvethSlot() + cardInfo.getSecondFingerNo() + cardInfo.getSecondFingerSecurityLevel() + cardInfo.getSecondFingerIndex() + cardInfo.getSecondFingerQuality() + "0" + cardInfo.getSecondFingerVerificationMode() + dummy;
                                    list = Utility.splitStringBySize(writeData, 32);
                                    if (list != null && list.size() == 3) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(0);
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            sleep(Constants.DELEY);
                                            char[] data = rc522Comm.readRC522();
                                            String strData = new String(data);
                                            String[] arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(1);
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        sleep(Constants.DELEY);
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + list.get(2);
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    sleep(Constants.DELEY);
                                                                    data = rc522Comm.readRC522();
                                                                    strData = new String(data);
                                                                    arr = strData.split(":");
                                                                    if (arr != null && arr.length == 3) {
                                                                        strData = arr[2].trim();
                                                                        if (strData.equals("SUCCESS")) {
                                                                            writeData = Constants.RC522_CHANGE_KEY_COMMAND + " 63 " + strInitialKeyB + " B " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                                                            status = rc522Comm.writeRC522(writeData);
                                                                            if (status) {
                                                                                sleep(Constants.DELEY);
                                                                                char[] readData = rc522Comm.readRC522();
                                                                                if (readData != null && readData.length > 0) {
                                                                                    String strReadData = new String(readData);
                                                                                    strReadData = strReadData.trim();
                                                                                    arr = strReadData.split(":");
                                                                                    if (arr != null && arr.length == 3) {
                                                                                        strReadData = arr[2].trim();
                                                                                        Log.d("TEST", "Read:" + strReadData);
                                                                                        if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                                                            Log.d("TEST", "true");
                                                                                            opStatus = true;
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
                                                }
                                            }
                                        }
                                    }
                                    break;
                            }
                            if (!opStatus) {
                                break;
                            }
                        }

                        sectorKeyData.close();

                        //============================== MAD UPDATE =================================//

                        if (opStatus) {
                            opStatus = false;
                            String strData_01 = "55010005000000004802480248024802";
                            String strData_02 = "48024802480248024802480248024802";
                            opStatus = updateMAD(strData_01, strData_02);
                        }
                    }
                    break;
            }


            return opStatus;
        }

        @Override
        protected void onPostExecute(Boolean status) {
            mypDialog.cancel();
            clear();
            btn_CardWrite.setEnabled(true);
            if (status) {
                showCustomAlertDialog(true, "Card Write Status", "Card Write Successful");
            } else {
                showCustomAlertDialog(false, "Card Write Status", "Failed To Write To Card");
            }
        }
    }


//    private class AsyncTaskSpiCardWriteAndKeyUpdate extends AsyncTask<Void, Void, Integer> {
//
//        ProgressDialog mypDialog = null;
//
//        @Override
//        protected void onPreExecute() {
//            mypDialog = new ProgressDialog(SmartCardActivity.this);
//            mypDialog.setMessage("Card Writing Wait...");
//            mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//            mypDialog.setCanceledOnTouchOutside(false);
//            mypDialog.show();
//        }
//
//        @Override
//        protected Integer doInBackground(Void... voids) {
//
//            int error = -1;
//            DataBaseLayer dbLayer = new DataBaseLayer();
//            Cursor rs = dbLayer.getSectorAndKeyForRC632CardRead();
//
//            if (rs != null && rs.getCount() > 0) {
//
//                byte[] keyB = new byte[6];
//                byte[] keyBDflt = new byte[6];
//                byte[] writeData = new byte[48];
//                int keyFlag = 0;
//
//                String dummy = "00000000000000000000000000000000000000000000000000000000"; //dummy string for writing Sector 9 & F
//                String strFinalKeyA = "012345";
//                String strInitialKeyB = "543210";
//                String strAccessCode = "7F0788C1";
//
//                while (rs.moveToNext()) {
//
//                    String strSectorNo = rs.getString(0).trim();
//                    String strFinalKeyB = rs.getString(1).trim();
//                    int sectorNo = Integer.parseInt(strSectorNo);
//
//                    switch (sectorNo) {
//
//                        case 1:
//
//                            keyB = strFinalKeyB.getBytes();
//
//                            String strHexCardId = asciiToHex(strWriteCardId);
//
//                            String strData_10 = strHexCardId + "464F5254554E4120";
//                            String strData_11 = "11303030AAFFFFFFFFFFFFFFFFFFFFFF";
//                            String strData_12 = "00000000000000000000000000000000";
//
//                            String x = strData_10 + strData_11 + strData_12;
//
//                            writeData = hexStringToByteArray(x);
//                            error = rc632ReaderConnection.sectorWrite(keyFlag, keyB, (byte) sectorNo, writeData);
//                            Log.d("TEST", "Sector 1 Write Error No:" + error);
//
//                            break;
//
//                        case 2:
//
//                            keyB = strFinalKeyB.getBytes();
//                            keyBDflt = strInitialKeyB.getBytes();
//
//                            String strBasicInfo = strEmployeeId + strName + strValidUptoForCard + strBirthDayForCard + strSiteCode + bloodgroup + strSmartCardVersion;
//
//                            writeData = strBasicInfo.getBytes();
//
//                            error = rc632ReaderConnection.sectorWrite(keyFlag, keyBDflt, (byte) sectorNo, writeData);
//                            Log.d("TEST", "Sector 2 Write Error No:" + error);
//
//                            boolean keyBChange = false;
//                            keyBChange = rc632ReaderConnection.changeKeyB((byte) sectorNo, keyB);
//
//                            if (keyBChange) {
//                                error = 0;
//                            } else {
//                                error = 1;
//                            }
//
//                            Log.d("TEST", "Sector 2 Key B Change Status:" + keyBChange);
//
//                            break;
//
//                        case 4:
//
//                            keyB = strFinalKeyB.getBytes();
//                            keyBDflt = strInitialKeyB.getBytes();
//
//                            writeData = hexStringToByteArray(s4);
//                            error = rc632ReaderConnection.sectorWrite(keyFlag, keyBDflt, (byte) sectorNo, writeData);
//                            Log.d("TEST", "Sector 4 Write Error No:" + error);
//
//                            error = rc632ReaderConnection.writeTemplateKey((byte) 19, keyB);
//                            Log.d("TEST", "Sector 4 Key B Change Status:" + error);
//
//                            break;
//
//
//                        case 5:
//
//                            keyB = strFinalKeyB.getBytes();
//                            keyBDflt = strInitialKeyB.getBytes();
//
//
//                            writeData = hexStringToByteArray(s5);
//                            error = rc632ReaderConnection.sectorWrite(keyFlag, keyBDflt, (byte) sectorNo, writeData);
//                            Log.d("TEST", "Sector 5 Write Error No:" + error);
//
//                            error = rc632ReaderConnection.writeTemplateKey((byte) 23, keyB);
//                            Log.d("TEST", "Sector 5 Key B Change Status:" + error);
//
//                            break;
//
//                        case 6:
//
//                            keyB = strFinalKeyB.getBytes();
//                            keyBDflt = strInitialKeyB.getBytes();
//
//
//                            writeData = hexStringToByteArray(s6);
//                            error = rc632ReaderConnection.sectorWrite(keyFlag, keyBDflt, (byte) sectorNo, writeData);
//                            Log.d("TEST", "Sector 6 Write Error No:" + error);
//
//                            error = rc632ReaderConnection.writeTemplateKey((byte) 27, keyB);
//                            Log.d("TEST", "Sector 6 Key B Change Status:" + error);
//
//                            break;
//
//                        case 7:
//
//                            keyB = strFinalKeyB.getBytes();
//                            keyBDflt = strInitialKeyB.getBytes();
//
//
//                            writeData = hexStringToByteArray(s7);
//                            error = rc632ReaderConnection.sectorWrite(keyFlag, keyBDflt, (byte) sectorNo, writeData);
//                            Log.d("TEST", "Sector 7 Write Error No:" + error);
//
//                            error = rc632ReaderConnection.writeTemplateKey((byte) 31, keyB);
//                            Log.d("TEST", "Sector 7 Key B Change Status:" + error);
//
//                            break;
//
//                        case 8:
//
//                            keyB = strFinalKeyB.getBytes();
//                            keyBDflt = strInitialKeyB.getBytes();
//
//
//                            writeData = hexStringToByteArray(s8);
//                            error = rc632ReaderConnection.sectorWrite(keyFlag, keyBDflt, (byte) sectorNo, writeData);
//                            Log.d("TEST", "Sector 8 Write Error No:" + error);
//
//                            error = rc632ReaderConnection.writeTemplateKey((byte) 35, keyB);
//                            Log.d("TEST", "Sector 8 Key B Change Status:" + error);
//
//                            break;
//
//                        case 9:
//
//                            keyB = strFinalKeyB.getBytes();
//                            keyBDflt = strInitialKeyB.getBytes();
//
//                            String strFingerDetails1 = s9 + strFirstFingerNo + securitylevel + fingerindex1 + strFirstFingerQuality + "0" + verificationmode + dummy;
//                            writeData = hexStringToByteArray(strFingerDetails1);
//                            error = rc632ReaderConnection.sectorWrite(keyFlag, keyBDflt, (byte) sectorNo, writeData);
//                            Log.d("TEST", "Sector 9 Write Error No:" + error);
//
//                            error = rc632ReaderConnection.writeTemplateKey((byte) 39, keyB);
//                            Log.d("TEST", "Sector 9 Key B Change Status:" + error);
//
//                            break;
//
//                        case 10:
//
//                            keyB = strFinalKeyB.getBytes();
//                            keyBDflt = strInitialKeyB.getBytes();
//
//
//                            writeData = hexStringToByteArray(sA);
//                            error = rc632ReaderConnection.sectorWrite(keyFlag, keyBDflt, (byte) sectorNo, writeData);
//                            Log.d("TEST", "Sector 10 Write Error No:" + error);
//
//                            error = rc632ReaderConnection.writeTemplateKey((byte) 43, keyB);
//                            Log.d("TEST", "Sector 10 Key B Change Status:" + error);
//
//                            break;
//
//                        case 11:
//
//                            keyB = strFinalKeyB.getBytes();
//                            keyBDflt = strInitialKeyB.getBytes();
//
//
//                            writeData = hexStringToByteArray(sB);
//                            error = rc632ReaderConnection.sectorWrite(keyFlag, keyBDflt, (byte) sectorNo, writeData);
//                            Log.d("TEST", "Sector 11 Write Error No:" + error);
//
//                            error = rc632ReaderConnection.writeTemplateKey((byte) 47, keyB);
//                            Log.d("TEST", "Sector 11 Key B Change Status:" + error);
//
//                            break;
//
//                        case 12:
//
//                            keyB = strFinalKeyB.getBytes();
//                            keyBDflt = strInitialKeyB.getBytes();
//
//                            writeData = hexStringToByteArray(sC);
//                            error = rc632ReaderConnection.sectorWrite(keyFlag, keyBDflt, (byte) sectorNo, writeData);
//                            Log.d("TEST", "Sector 12 Write Error No:" + error);
//
//                            error = rc632ReaderConnection.writeTemplateKey((byte) 51, keyB);
//                            Log.d("TEST", "Sector 12 Key B Change Status:" + error);
//
//                            break;
//
//                        case 13:
//
//                            keyB = strFinalKeyB.getBytes();
//                            keyBDflt = strInitialKeyB.getBytes();
//
//
//                            writeData = hexStringToByteArray(sD);
//                            error = rc632ReaderConnection.sectorWrite(keyFlag, keyBDflt, (byte) sectorNo, writeData);
//                            Log.d("TEST", "Sector 13 Write Error No:" + error);
//
//                            error = rc632ReaderConnection.writeTemplateKey((byte) 55, keyB);
//                            Log.d("TEST", "Sector 13 Key B Change Status:" + error);
//
//                            break;
//
//                        case 14:
//
//                            keyB = strFinalKeyB.getBytes();
//                            keyBDflt = strInitialKeyB.getBytes();
//
//                            writeData = hexStringToByteArray(sE);
//                            error = rc632ReaderConnection.sectorWrite(keyFlag, keyBDflt, (byte) sectorNo, writeData);
//                            Log.d("TEST", "Sector 14 Write Error No:" + error);
//
//                            error = rc632ReaderConnection.writeTemplateKey((byte) 59, keyB);
//                            Log.d("TEST", "Sector 14 Key B Change Status:" + error);
//
//                            break;
//
//                        case 15:
//
//                            keyB = strFinalKeyB.getBytes();
//                            keyBDflt = strInitialKeyB.getBytes();
//
//
//                            String strFingerDetails2 = sF + strSecondFingerNo + securitylevel + fingerindex2 + strSecondFingerQuality + "0" + verificationmode + dummy;
//                            writeData = hexStringToByteArray(strFingerDetails2);
//                            error = rc632ReaderConnection.sectorWrite(keyFlag, keyBDflt, (byte) sectorNo, writeData);
//                            Log.d("TEST", "Sector 15 Write Error No:" + error);
//
//                            error = rc632ReaderConnection.writeTemplateKey((byte) 63, keyB);
//                            Log.d("TEST", "Sector 15 Key B Change Status:" + error);
//
//                            break;
//
//                        default:
//                            break;
//
//                    }
//                }
//                rs.close();
//
//                if (error == 0) {
//
//                    error = rc632ReaderConnection.madOperation(0);
//                    Log.d("TEST", "Mad Opeartion 0:" + error);
//                    error = rc632ReaderConnection.madOperation(2);
//                    Log.d("TEST", "Mad Opeartion 2:" + error);
//
//                }
//
//            }
//
//            return error;
//        }
//
//        @Override
//        protected void onPostExecute(Integer result) {
//            mypDialog.cancel();
//
//            if (result == 0) {
//                showCustomAlertDialog(true, "Card Write Status", "Card Write Successfully");
//            } else {
//                showCustomAlertDialog(false, "Card Write Status", "Failed To Write To Card");
//            }
//
//            clear();
//        }
//
//    }


//    private class AsyncTaskSpiCardWrite extends AsyncTask<Void, Void, Integer> {
//
//        ProgressDialog mypDialog = null;
//        long start;
//        long end;
//
//        @Override
//        protected void onPreExecute() {
//            start = System.currentTimeMillis();
//            mypDialog = new ProgressDialog(SmartCardActivity.this);
//            mypDialog.setMessage("Card Writing Wait...");
//            mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//            mypDialog.setCanceledOnTouchOutside(false);
//            mypDialog.show();
//        }
//
//        @Override
//        protected Integer doInBackground(Void... voids) {
//
//            int error = -1;
//            DataBaseLayer dbLayer = new DataBaseLayer();
//            Cursor rs = dbLayer.getSectorAndKeyForRC632CardRead();
//
//            if (rs != null && rs.getCount() > 0) {
//
//                byte[] keyB = new byte[6];
//                byte[] writeData = new byte[48];
//                int keyFlag = 0;
//
//                String dummy = "00000000000000000000000000000000000000000000000000000000"; //dummy string for writing Sector 9 & F
//
//                while (rs.moveToNext()) {
//
//                    String strSectorNo = rs.getString(0).trim();
//                    String strInitialKeyB = rs.getString(1).trim();
//                    int sectorNo = Integer.parseInt(strSectorNo);
//
//                    switch (sectorNo) {
//
//
//                        case 1:
//
//                            keyB = strInitialKeyB.getBytes();
//
//                            String strHexCardId = asciiToHex(strWriteCardId);
//
//                            String strData_10 = strHexCardId + "464F5254554E4120";
//                            String strData_11 = "11303030AAFFFFFFFFFFFFFFFFFFFFFF";
//                            String strData_12 = "00000000000000000000000000000000";
//
//                            String x = strData_10 + strData_11 + strData_12;
//
//                            writeData = hexStringToByteArray(x);
//                            error = rc632ReaderConnection.sectorWrite(keyFlag, keyB, (byte) sectorNo, writeData);
//                            Log.d("TEST", "Sector 1 Write Error No:" + error);
//
//                            break;
//
//                        case 2:
//
//                            keyB = strInitialKeyB.getBytes();
//
//                            String strBasicInfo = strEmployeeId + strName + strValidUptoForCard + strBirthDayForCard + strSiteCode + bloodgroup + strSmartCardVersion;
//                            writeData = strBasicInfo.getBytes();
//
//                            error = rc632ReaderConnection.sectorWrite(keyFlag, keyB, (byte) sectorNo, writeData);
//                            Log.d("TEST", "Sector 2 Write Error No:" + error);
//
//                            break;
//
//
//                        case 4:
//
//                            keyB = strInitialKeyB.getBytes();
//
//                            writeData = hexStringToByteArray(s4);
//                            error = rc632ReaderConnection.sectorWrite(keyFlag, keyB, (byte) sectorNo, writeData);
//                            Log.d("TEST", "Sector 4 Write Error No:" + error);
//
//                            break;
//
//
//                        case 5:
//
//                            keyB = strInitialKeyB.getBytes();
//
//                            writeData = hexStringToByteArray(s5);
//                            error = rc632ReaderConnection.sectorWrite(keyFlag, keyB, (byte) sectorNo, writeData);
//                            Log.d("TEST", "Sector 5 Write Error No:" + error);
//
//                            break;
//
//                        case 6:
//
//                            keyB = strInitialKeyB.getBytes();
//
//                            writeData = hexStringToByteArray(s6);
//                            error = rc632ReaderConnection.sectorWrite(keyFlag, keyB, (byte) sectorNo, writeData);
//                            Log.d("TEST", "Sector 6 Write Error No:" + error);
//
//                            break;
//
//                        case 7:
//
//                            keyB = strInitialKeyB.getBytes();
//
//                            writeData = hexStringToByteArray(s7);
//                            error = rc632ReaderConnection.sectorWrite(keyFlag, keyB, (byte) sectorNo, writeData);
//                            Log.d("TEST", "Sector 7 Write Error No:" + error);
//
//                            break;
//
//                        case 8:
//
//                            keyB = strInitialKeyB.getBytes();
//
//                            writeData = hexStringToByteArray(s8);
//                            error = rc632ReaderConnection.sectorWrite(keyFlag, keyB, (byte) sectorNo, writeData);
//                            Log.d("TEST", "Sector 8 Write Error No:" + error);
//
//                            break;
//
//                        case 9:
//
//                            keyB = strInitialKeyB.getBytes();
//
//                            String strFingerDetails1 = s9 + strFirstFingerNo + securitylevel + fingerindex1 + strFirstFingerQuality + "0" + verificationmode + dummy;
//                            writeData = hexStringToByteArray(strFingerDetails1);
//                            error = rc632ReaderConnection.sectorWrite(keyFlag, keyB, (byte) sectorNo, writeData);
//                            Log.d("TEST", "Sector 9 Write Error No:" + error);
//
//                            break;
//
//                        case 10:
//
//                            keyB = strInitialKeyB.getBytes();
//
//                            writeData = hexStringToByteArray(sA);
//                            error = rc632ReaderConnection.sectorWrite(keyFlag, keyB, (byte) sectorNo, writeData);
//                            Log.d("TEST", "Sector 10 Write Error No:" + error);
//
//
//                            break;
//
//                        case 11:
//
//                            keyB = strInitialKeyB.getBytes();
//
//                            writeData = hexStringToByteArray(sB);
//                            error = rc632ReaderConnection.sectorWrite(keyFlag, keyB, (byte) sectorNo, writeData);
//                            Log.d("TEST", "Sector 11 Write Error No:" + error);
//
//                            break;
//
//                        case 12:
//
//                            keyB = strInitialKeyB.getBytes();
//
//                            writeData = hexStringToByteArray(sC);
//                            error = rc632ReaderConnection.sectorWrite(keyFlag, keyB, (byte) sectorNo, writeData);
//                            Log.d("TEST", "Sector 12 Write Error No:" + error);
//
//                            break;
//
//                        case 13:
//
//                            keyB = strInitialKeyB.getBytes();
//
//                            writeData = hexStringToByteArray(sD);
//                            error = rc632ReaderConnection.sectorWrite(keyFlag, keyB, (byte) sectorNo, writeData);
//                            Log.d("TEST", "Sector 13 Write Error No:" + error);
//
//                            break;
//
//                        case 14:
//
//                            keyB = strInitialKeyB.getBytes();
//
//                            writeData = hexStringToByteArray(sE);
//                            error = rc632ReaderConnection.sectorWrite(keyFlag, keyB, (byte) sectorNo, writeData);
//                            Log.d("TEST", "Sector 14 Write Error No:" + error);
//
//
//                            break;
//
//                        case 15:
//
//                            keyB = strInitialKeyB.getBytes();
//
//                            String strFingerDetails2 = sF + strSecondFingerNo + securitylevel + fingerindex2 + strSecondFingerQuality + "0" + verificationmode + dummy;
//                            writeData = hexStringToByteArray(strFingerDetails2);
//                            error = rc632ReaderConnection.sectorWrite(keyFlag, keyB, (byte) sectorNo, writeData);
//                            Log.d("TEST", "Sector 15 Write Error No:" + error);
//
//
//                            break;
//
//                        default:
//                            break;
//
//                    }
//                }
//                rs.close();
//
//                if (error == 0) {
//
//                    error = rc632ReaderConnection.madOperation(0);
//                    Log.d("TEST", "Mad Opeartion 0:" + error);
//                    error = rc632ReaderConnection.madOperation(2);
//                    Log.d("TEST", "Mad Opeartion 2:" + error);
//
//                }
//
//            }
//
//
//            return error;
//        }
//
//        @Override
//        protected void onPostExecute(Integer result) {
//            mypDialog.cancel();
//            end = System.currentTimeMillis();
//            long timeTaken = end - start;
//
//            Log.d("TEST", "Time Taken In Milliseconds:" + timeTaken);
//            Log.d("TEST", "Time Taken In Seconds :" + TimeUnit.MILLISECONDS.toSeconds(timeTaken));
//
//            if (result == 0) {
//                showCustomAlertDialog(true, "Card Write Status", "Card Write Successfully");
//            } else {
//                showCustomAlertDialog(false, "Card Write Status", "Failed To Write To Card");
//            }
//            clear();
//        }
//
//    }

    //===================================== Async Task SPI Card Initialize ===================================//

    private class AsynTaskSpiCardInitialize extends AsyncTask <String, Void, Integer> {

        ProgressDialog mypDialog;

        @Override
        protected void onPreExecute() {
            mypDialog = new ProgressDialog(SmartCardActivity.this);
            mypDialog.setMessage("Card Initializing Wait...");
            mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mypDialog.setCanceledOnTouchOutside(false);
            mypDialog.show();
        }

        @Override
        protected Integer doInBackground(String... strings) {

            String strCardId = strings[0];

            int error = -1;

            Cursor sectorKeyData = dbComm.getSectorAndKeyForRC632CardInit();
            if (sectorKeyData != null) {

                String strFactoryKey = "FFFFFFFFFFFF";

                byte[] keyaf = new byte[6];
                byte[] accesscode = new byte[4];
                byte[] keybf = new byte[6];
                byte[] factKey = new byte[6];

                while (sectorKeyData.moveToNext()) {

                    String strSectorNo = sectorKeyData.getString(0);
                    String strFinalKeyA = sectorKeyData.getString(1);
                    String strAccessCode = sectorKeyData.getString(2);
                    String strFinalKeyB = sectorKeyData.getString(3);

                    int sectorNo = Integer.parseInt(strSectorNo);

                    switch (sectorNo) {

                        case 0:

                            factKey = Utility.hexStringToByteArray(strFactoryKey);
                            keyaf = strFinalKeyA.getBytes();
                            accesscode = Utility.hexStringToByteArray(strAccessCode);
                            keybf = strFinalKeyB.getBytes();

                            error = rc632ReaderConnection.sectorInit((byte) sectorNo, factKey, keyaf, accesscode, keybf);
                            Log.d("TEST", "Card Init Sector 0:" + error);

                            break;


                        case 1:

                            byte[] cardId = new byte[8];
                            factKey = Utility.hexStringToByteArray(strFactoryKey);
                            keyaf = strFinalKeyA.getBytes();
                            accesscode = Utility.hexStringToByteArray(strAccessCode);
                            keybf = strFinalKeyB.getBytes();

                            cardId = strCardId.getBytes();

                            error = rc632ReaderConnection.sectorInit_1((byte) sectorNo, factKey, keyaf, accesscode, keybf, cardId);
                            Log.d("TEST", "Card Init Sector 1:" + error);

                            break;

                        case 2:

                            factKey = Utility.hexStringToByteArray(strFactoryKey);
                            keyaf = strFinalKeyA.getBytes();
                            accesscode = Utility.hexStringToByteArray(strAccessCode);
                            keybf = strFinalKeyB.getBytes();

                            error = rc632ReaderConnection.sectorInit((byte) sectorNo, factKey, keyaf, accesscode, keybf);
                            Log.d("TEST", "Card Init Sector 2:" + error);

                            break;

                        case 3:

                            factKey = Utility.hexStringToByteArray(strFactoryKey);
                            keyaf = strFinalKeyA.getBytes();
                            accesscode = Utility.hexStringToByteArray(strAccessCode);
                            keybf = strFinalKeyB.getBytes();

                            error = rc632ReaderConnection.sectorInit((byte) sectorNo, factKey, keyaf, accesscode, keybf);
                            Log.d("TEST", "Card Init Sector 3:" + error);

                            break;

                        case 4:

                            factKey = Utility.hexStringToByteArray(strFactoryKey);
                            keyaf = strFinalKeyA.getBytes();
                            accesscode = Utility.hexStringToByteArray(strAccessCode);
                            keybf = strFinalKeyB.getBytes();

                            error = rc632ReaderConnection.sectorInit((byte) sectorNo, factKey, keyaf, accesscode, keybf);
                            Log.d("TEST", "Card Init Sector 4:" + error);

                            break;

                        case 5:

                            factKey = Utility.hexStringToByteArray(strFactoryKey);
                            keyaf = strFinalKeyA.getBytes();
                            accesscode = Utility.hexStringToByteArray(strAccessCode);
                            keybf = strFinalKeyB.getBytes();

                            error = rc632ReaderConnection.sectorInit((byte) sectorNo, factKey, keyaf, accesscode, keybf);
                            Log.d("TEST", "Card Init Sector 5:" + error);

                            break;

                        case 6:

                            factKey = Utility.hexStringToByteArray(strFactoryKey);
                            keyaf = strFinalKeyA.getBytes();
                            accesscode = Utility.hexStringToByteArray(strAccessCode);
                            keybf = strFinalKeyB.getBytes();

                            error = rc632ReaderConnection.sectorInit((byte) sectorNo, factKey, keyaf, accesscode, keybf);
                            Log.d("TEST", "Card Init Sector 6:" + error);

                            break;

                        case 7:

                            factKey = Utility.hexStringToByteArray(strFactoryKey);
                            keyaf = strFinalKeyA.getBytes();
                            accesscode = Utility.hexStringToByteArray(strAccessCode);
                            keybf = strFinalKeyB.getBytes();

                            error = rc632ReaderConnection.sectorInit((byte) sectorNo, factKey, keyaf, accesscode, keybf);
                            Log.d("TEST", "Card Init Sector 7:" + error);

                            break;

                        case 8:

                            factKey = Utility.hexStringToByteArray(strFactoryKey);
                            keyaf = strFinalKeyA.getBytes();
                            accesscode = Utility.hexStringToByteArray(strAccessCode);
                            keybf = strFinalKeyB.getBytes();

                            error = rc632ReaderConnection.sectorInit((byte) sectorNo, factKey, keyaf, accesscode, keybf);
                            Log.d("TEST", "Card Init Sector 8:" + error);


                            break;

                        case 9:

                            factKey = Utility.hexStringToByteArray(strFactoryKey);
                            keyaf = strFinalKeyA.getBytes();
                            accesscode = Utility.hexStringToByteArray(strAccessCode);
                            keybf = strFinalKeyB.getBytes();

                            error = rc632ReaderConnection.sectorInit((byte) sectorNo, factKey, keyaf, accesscode, keybf);
                            Log.d("TEST", "Card Init Sector 9:" + error);

                            break;

                        case 10:

                            factKey = Utility.hexStringToByteArray(strFactoryKey);
                            keyaf = strFinalKeyA.getBytes();
                            accesscode = Utility.hexStringToByteArray(strAccessCode);
                            keybf = strFinalKeyB.getBytes();

                            error = rc632ReaderConnection.sectorInit((byte) sectorNo, factKey, keyaf, accesscode, keybf);
                            Log.d("TEST", "Card Init Sector 10:" + error);

                            break;

                        case 11:

                            factKey = Utility.hexStringToByteArray(strFactoryKey);
                            keyaf = strFinalKeyA.getBytes();
                            accesscode = Utility.hexStringToByteArray(strAccessCode);
                            keybf = strFinalKeyB.getBytes();

                            error = rc632ReaderConnection.sectorInit((byte) sectorNo, factKey, keyaf, accesscode, keybf);
                            Log.d("TEST", "Card Init Sector 11:" + error);

                            break;

                        case 12:

                            factKey = Utility.hexStringToByteArray(strFactoryKey);
                            keyaf = strFinalKeyA.getBytes();
                            accesscode = Utility.hexStringToByteArray(strAccessCode);
                            keybf = strFinalKeyB.getBytes();

                            error = rc632ReaderConnection.sectorInit((byte) sectorNo, factKey, keyaf, accesscode, keybf);
                            Log.d("TEST", "Card Init Sector 12:" + error);

                            break;

                        case 13:

                            factKey = Utility.hexStringToByteArray(strFactoryKey);
                            keyaf = strFinalKeyA.getBytes();
                            accesscode = Utility.hexStringToByteArray(strAccessCode);
                            keybf = strFinalKeyB.getBytes();

                            error = rc632ReaderConnection.sectorInit((byte) sectorNo, factKey, keyaf, accesscode, keybf);
                            Log.d("TEST", "Card Init Sector 13:" + error);

                            break;

                        case 14:

                            factKey = Utility.hexStringToByteArray(strFactoryKey);
                            keyaf = strFinalKeyA.getBytes();
                            accesscode = Utility.hexStringToByteArray(strAccessCode);
                            keybf = strFinalKeyB.getBytes();

                            error = rc632ReaderConnection.sectorInit((byte) sectorNo, factKey, keyaf, accesscode, keybf);
                            Log.d("TEST", "Card Init Sector 14:" + error);

                            break;

                        case 15:

                            factKey = Utility.hexStringToByteArray(strFactoryKey);
                            keyaf = strFinalKeyA.getBytes();
                            accesscode = Utility.hexStringToByteArray(strAccessCode);
                            keybf = strFinalKeyB.getBytes();

                            error = rc632ReaderConnection.sectorInit((byte) sectorNo, factKey, keyaf, accesscode, keybf);
                            Log.d("TEST", "Card Init Sector 15:" + error);

                            break;

                        default:
                            break;
                    }
                }
                if (sectorKeyData != null) {
                    sectorKeyData.close();
                }
            }
            return error;
        }

        @Override
        protected void onPostExecute(Integer result) {
            mypDialog.cancel();
            if (result == 0) {
                showCustomAlertDialog(true, "Card Init Status", "Card Initialised Successfully");
            } else {
                showCustomAlertDialog(true, "Card Init Status", "Failed To Initialise Card");
            }
            clear();
        }

    }

    //===================================== Async Task USB Card Initialize ===================================//

    private class AsynTaskUsbCardInitialize extends AsyncTask <Void, Void, Integer> {

        ProgressDialog mypDialog;
        MicroSmartV2Communicator mSmartV2Comm;
        String cardId;
        String strLog = "";

        AsynTaskUsbCardInitialize(MicroSmartV2Communicator mSmartV2Comm, String cardId) {
            this.mSmartV2Comm = mSmartV2Comm;
            this.cardId = cardId;
        }

        @Override
        protected void onPreExecute() {
            mypDialog = new ProgressDialog(SmartCardActivity.this);
            mypDialog.setMessage("Card Initializing Wait...");
            mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mypDialog.setCanceledOnTouchOutside(false);
            mypDialog.show();
        }


        @Override
        protected Integer doInBackground(Void... params) {
            int status = -1;
            Cursor sectorKeyData = dbComm.getSectorAndKeyForCardInit();
            if (sectorKeyData != null) {
                String strInitialKeyA = "FFFFFFFFFFFF";
                String msg = "";
                byte[] data = null;
                while (sectorKeyData.moveToNext()) {
                    String strSectorNo = sectorKeyData.getString(0).trim();
                    String strFinalKeyA = sectorKeyData.getString(1).trim();
                    String strAccessCode = sectorKeyData.getString(2).trim();
                    String strFinalKeyB = sectorKeyData.getString(3).trim();
                    int sectorNo = Integer.parseInt(strSectorNo);
                    switch (sectorNo) {
                        case 0:
                            msg = Utility.addCheckSum(Constants.FACTORY_SEC_READ[0] + Constants.KEY_A + strInitialKeyA + Constants.HEX_READ_WRITE);
                            data = mSmartV2Comm.readSector(sectorNo, msg.getBytes());
                            if (data != null) {
                                status = mSmartV2Comm.keyUpdate(3, strFinalKeyA, strAccessCode, strFinalKeyB);
                                if (status != -1) {
                                    strLog += "Sector 0 Key Updated Successfully First Time\n";
                                } else {
                                    strLog += "Sector 0 Key Updated Failure First Time\n";
                                }
                            } else {
                                strLog += "Failed To Read Sector 0\n";
                            }
                            break;
                        case 1:
                            msg = Utility.addCheckSum(Constants.FACTORY_SEC_READ[1] + Constants.KEY_A + strInitialKeyA + Constants.HEX_READ_WRITE);
                            data = mSmartV2Comm.readSector(sectorNo, msg.getBytes());
                            if (data != null) {
                                status = mSmartV2Comm.keyUpdate(7, strFinalKeyA, strAccessCode, strFinalKeyB);
                                if (status != -1) {
                                    strLog += "Sector 1 Key Updated Successfully First Time\n";
                                } else {
                                    strLog += "Sector 1 Key Updated Failure First Time\n";
                                }
                            } else {
                                strLog += "Failed To Read Sector 1\n";
                            }
                            break;
                        case 2:
                            msg = Utility.addCheckSum(Constants.FACTORY_SEC_READ[2] + Constants.KEY_A + strInitialKeyA + Constants.HEX_READ_WRITE);
                            data = mSmartV2Comm.readSector(sectorNo, msg.getBytes());
                            if (data != null) {
                                status = mSmartV2Comm.keyUpdate(11, strFinalKeyA, strAccessCode, strFinalKeyB);
                                if (status != -1) {
                                    strLog += "Sector 2 Key Updated Successfully First Time\n";
                                } else {
                                    strLog += "Sector 2 Key Updated Failure First Time\n";
                                }
                            } else {
                                strLog += "Failed To Read Sector 2\n";
                            }
                            break;
                        case 3:
                            msg = Utility.addCheckSum(Constants.FACTORY_SEC_READ[3] + Constants.KEY_A + strInitialKeyA + Constants.HEX_READ_WRITE);
                            data = mSmartV2Comm.readSector(sectorNo, msg.getBytes());
                            if (data != null) {
                                status = mSmartV2Comm.keyUpdate(15, strFinalKeyA, strAccessCode, strFinalKeyB);
                                if (status != -1) {
                                    strLog += "Sector 3 Key Updated Successfully First Time\n";
                                } else {
                                    strLog += "Sector 3 Key Updated Failure First Time\n";
                                }
                            } else {
                                strLog += "Failed To Read Sector 3\n";
                            }
                            break;
                        case 4:
                            msg = Utility.addCheckSum(Constants.FACTORY_SEC_READ[4] + Constants.KEY_A + strInitialKeyA + Constants.HEX_READ_WRITE);
                            data = mSmartV2Comm.readSector(sectorNo, msg.getBytes());
                            if (data != null) {
                                status = mSmartV2Comm.keyUpdate(19, strFinalKeyA, strAccessCode, strFinalKeyB);
                                if (status != -1) {
                                    strLog += "Sector 4 Key Updated Successfully First Time\n";
                                } else {
                                    strLog += "Sector 4 Key Updated Failure First Time\n";
                                }
                            } else {
                                strLog += "Failed To Read Sector 4\n";
                            }
                            break;
                        case 5:
                            msg = Utility.addCheckSum(Constants.FACTORY_SEC_READ[5] + Constants.KEY_A + strInitialKeyA + Constants.HEX_READ_WRITE);
                            data = mSmartV2Comm.readSector(sectorNo, msg.getBytes());
                            if (data != null) {
                                status = mSmartV2Comm.keyUpdate(23, strFinalKeyA, strAccessCode, strFinalKeyB);
                                if (status != -1) {
                                    strLog += "Sector 5 Key Updated Successfully First Time\n";
                                } else {
                                    strLog += "Sector 5 Key Updated Failure First Time\n";
                                }
                            } else {
                                strLog += "Failed To Read Sector 5\n";
                            }
                            break;
                        case 6:
                            msg = Utility.addCheckSum(Constants.FACTORY_SEC_READ[6] + Constants.KEY_A + strInitialKeyA + Constants.HEX_READ_WRITE);
                            data = mSmartV2Comm.readSector(sectorNo, msg.getBytes());
                            if (data != null) {
                                status = mSmartV2Comm.keyUpdate(27, strFinalKeyA, strAccessCode, strFinalKeyB);
                                if (status != -1) {
                                    strLog += "Sector 6 Key Updated Successfully First Time\n";
                                } else {
                                    strLog += "Sector 6 Key Updated Failure First Time\n";
                                }
                            } else {
                                strLog += "Failed To Read Sector 6\n";
                            }
                            break;
                        case 7:
                            msg = Utility.addCheckSum(Constants.FACTORY_SEC_READ[7] + Constants.KEY_A + strInitialKeyA + Constants.HEX_READ_WRITE);
                            data = mSmartV2Comm.readSector(sectorNo, msg.getBytes());
                            if (data != null) {
                                status = mSmartV2Comm.keyUpdate(31, strFinalKeyA, strAccessCode, strFinalKeyB);
                                if (status != -1) {
                                    strLog += "Sector 7 Key Updated Successfully First Time\n";
                                } else {
                                    strLog += "Sector 7 Key Updated Failure First Time\n";
                                }
                            } else {
                                strLog += "Failed To Read Sector 7\n";
                            }
                            break;
                        case 8:
                            msg = Utility.addCheckSum(Constants.FACTORY_SEC_READ[8] + Constants.KEY_A + strInitialKeyA + Constants.HEX_READ_WRITE);
                            data = mSmartV2Comm.readSector(sectorNo, msg.getBytes());
                            if (data != null) {
                                status = mSmartV2Comm.keyUpdate(35, strFinalKeyA, strAccessCode, strFinalKeyB);
                                if (status != -1) {
                                    strLog += "Sector 8 Key Updated Successfully First Time\n";
                                } else {
                                    strLog += "Sector 8 Key Updated Failure First Time\n";
                                }
                            } else {
                                strLog += "Failed To Read Sector 8\n";
                            }
                            break;
                        case 9:
                            msg = Utility.addCheckSum(Constants.FACTORY_SEC_READ[9] + Constants.KEY_A + strInitialKeyA + Constants.HEX_READ_WRITE);
                            data = mSmartV2Comm.readSector(sectorNo, msg.getBytes());
                            if (data != null) {
                                status = mSmartV2Comm.keyUpdate(39, strFinalKeyA, strAccessCode, strFinalKeyB);
                                if (status != -1) {
                                    strLog += "Sector 9 Key Updated Successfully First Time\n";
                                } else {
                                    strLog += "Sector 9 Key Updated Failure First Time\n";
                                }
                            } else {
                                strLog += "Failed To Read Sector 9\n";
                            }
                            break;
                        case 10:
                            msg = Utility.addCheckSum(Constants.FACTORY_SEC_READ[10] + Constants.KEY_A + strInitialKeyA + Constants.HEX_READ_WRITE);
                            data = mSmartV2Comm.readSector(sectorNo, msg.getBytes());
                            if (data != null) {
                                status = mSmartV2Comm.keyUpdate(43, strFinalKeyA, strAccessCode, strFinalKeyB);
                                if (status != -1) {
                                    strLog += "Sector 10 Key Updated Successfully First Time\n";
                                } else {
                                    strLog += "Sector 10 Key Updated Failure First Time\n";
                                }
                            } else {
                                strLog += "Failed To Read Sector 10\n";
                            }
                            break;
                        case 11:
                            msg = Utility.addCheckSum(Constants.FACTORY_SEC_READ[11] + Constants.KEY_A + strInitialKeyA + Constants.HEX_READ_WRITE);
                            data = mSmartV2Comm.readSector(sectorNo, msg.getBytes());
                            if (data != null) {
                                status = mSmartV2Comm.keyUpdate(47, strFinalKeyA, strAccessCode, strFinalKeyB);
                                if (status != -1) {
                                    strLog += "Sector 11 Key Updated Successfully First Time\n";
                                } else {
                                    strLog += "Sector 11 Key Updated Failure First Time\n";
                                }
                            } else {
                                strLog += "Failed To Read Sector 11\n";
                            }
                            break;
                        case 12:
                            msg = Utility.addCheckSum(Constants.FACTORY_SEC_READ[12] + Constants.KEY_A + strInitialKeyA + Constants.HEX_READ_WRITE);
                            data = mSmartV2Comm.readSector(sectorNo, msg.getBytes());
                            if (data != null) {
                                status = mSmartV2Comm.keyUpdate(51, strFinalKeyA, strAccessCode, strFinalKeyB);
                                if (status != -1) {
                                    strLog += "Sector 12 Key Updated Successfully First Time\n";
                                } else {
                                    strLog += "Sector 12 Key Updated Failure First Time\n";
                                }
                            } else {
                                strLog += "Failed To Read Sector 12\n";
                            }
                            break;
                        case 13:
                            msg = Utility.addCheckSum(Constants.FACTORY_SEC_READ[13] + Constants.KEY_A + strInitialKeyA + Constants.HEX_READ_WRITE);
                            data = mSmartV2Comm.readSector(sectorNo, msg.getBytes());
                            if (data != null) {
                                status = mSmartV2Comm.keyUpdate(55, strFinalKeyA, strAccessCode, strFinalKeyB);
                                if (status != -1) {
                                    strLog += "Sector 13 Key Updated Successfully First Time\n";
                                } else {
                                    strLog += "Sector 13 Key Updated Failure First Time\n";
                                }
                            } else {
                                strLog += "Failed To Read Sector 13\n";
                            }
                            break;
                        case 14:
                            msg = Utility.addCheckSum(Constants.FACTORY_SEC_READ[14] + Constants.KEY_A + strInitialKeyA + Constants.HEX_READ_WRITE);
                            data = mSmartV2Comm.readSector(sectorNo, msg.getBytes());
                            if (data != null) {
                                status = mSmartV2Comm.keyUpdate(59, strFinalKeyA, strAccessCode, strFinalKeyB);
                                if (status != -1) {
                                    strLog += "Sector 14 Key Updated Successfully First Time\n";
                                } else {
                                    strLog += "Sector 14 Key Updated Failure First Time\n";
                                }
                            } else {
                                strLog += "Failed To Read Sector 14\n";
                            }
                            break;
                        case 15:
                            msg = Utility.addCheckSum(Constants.FACTORY_SEC_READ[15] + Constants.KEY_A + strInitialKeyA + Constants.HEX_READ_WRITE);
                            data = mSmartV2Comm.readSector(sectorNo, msg.getBytes());
                            if (data != null) {
                                status = mSmartV2Comm.keyUpdate(63, strFinalKeyA, strAccessCode, strFinalKeyB);
                                if (status != -1) {
                                    strLog += "Sector 15 Key Updated Successfully First Time\n";
                                } else {
                                    strLog += "Sector 15 Key Updated Failure First Time\n";
                                }
                            } else {
                                strLog += "Failed To Read Sector 15\n";
                            }
                            break;
                        default:
                            break;
                    }
                    if (data == null || status == -1) {
                        break;
                    } else {
                        continue;
                    }
                }


                if (data != null && status != -1) {

                    //=============================================Update MAD Blocks With Default Value And Write Card Id=========================================//

                    String strSectorNo = "01A001";
                    int blockno1 = 01;
                    int blockno2 = 02;
                    String strData_01 = "55010005000000000000000000000000";
                    String strData_02 = "00000000000000000000000000000000";

                    status = mSmartV2Comm.madUpdate(strSectorNo, blockno1, "524553534543", strData_01);
                    if (status != -1) {
                        status = mSmartV2Comm.madUpdate(strSectorNo, blockno2, "524553534543", strData_02);
                        if (status != -1) {
                            cardId = Utility.paddCardId(cardId);
                            String strHexCardId = Utility.asciiToHex(cardId);
                            String strData_10 = strHexCardId + "464F5254554E4120";
                            String strData_11 = "11303030AAFFFFFFFFFFFFFFFFFFFFFF";
                            String strData_12 = "00000000000000000000000000000000";
                            msg = Utility.addCheckSum("#FF4701A10143505320494400" + strData_10 + strData_11 + strData_12);
                            status = mSmartV2Comm.sectorWrite(msg, 1);
                        }
                    }
                }
            }

            String command = Utility.addCheckSum(Constants.READ_CARD_ID_COMMAND);
            String strCardId = mSmartV2Comm.readCardId(command.getBytes());
            return status;
        }

        @Override
        protected void onPostExecute(Integer result) {
            mypDialog.cancel();
            Log.d("TEST", "Log Details:\n" + strLog);
            if (result != -1) {
                showCustomAlertDialog(true, "Card Init Status", "Card Initialised Successfully");
            } else {
                showCustomAlertDialog(false, "Card Init Status", "Failed To Initialise Card");
            }
        }
    }


    private class AsyncTaskRC522CardInit extends AsyncTask <Void, Void, Boolean> {

        ProgressDialog mypDialog;
        RC522Communicator rc522Comm;
        String cardId;

        AsyncTaskRC522CardInit(RC522Communicator rc522Comm, String cardId) {
            this.rc522Comm = rc522Comm;
            this.cardId = cardId;
        }

        @Override
        protected void onPreExecute() {
            mypDialog = new ProgressDialog(SmartCardActivity.this);
            mypDialog.setMessage("Card Initializing Wait...");
            mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mypDialog.setCanceledOnTouchOutside(false);
            mypDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            //Read Data:WRITE_CARD:3:KEY_wr_SUCCESS
            boolean operation = false;
            Cursor sectorKeyData = dbComm.getSectorAndKeyForCardInit();
            if (sectorKeyData != null) {
                String strInitialKeyA = "FFFFFFFFFFFF";
                String strReadData = null;
                String writeData = "";
                char[] readData = null;
                boolean status = false;
                while (sectorKeyData.moveToNext()) {
                    String strSectorNo = sectorKeyData.getString(0).trim();
                    String strFinalKeyA = sectorKeyData.getString(1).trim();
                    String strAccessCode = sectorKeyData.getString(2).trim();
                    String strFinalKeyB = sectorKeyData.getString(3).trim();
                    int sectorNo = Integer.parseInt(strSectorNo);
                    switch (sectorNo) {
                        case 0:
                            for (int i = 0; i < 3; i++) {
                                operation = false;
                                writeData = Constants.RC522_CHANGE_KEY_COMMAND + " 3 " + strInitialKeyA + " A " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                status = rc522Comm.writeRC522(writeData);
                                if (status) {
                                    sleep(Constants.DELEY);//Added on 12082019
                                    readData = rc522Comm.readRC522();
                                    if (readData != null && readData.length > 0) {
                                        strReadData = new String(readData);
                                        strReadData = strReadData.trim();
                                        Log.d("TEST", "Read:" + strReadData);
                                        String arr[] = strReadData.split(":");
                                        if (arr != null && arr.length == 3) {
                                            strReadData = arr[2].trim();
                                            if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                operation = true;
                                                break;
                                            } else {
                                                continue;
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                        case 1:
                            for (int i = 0; i < 3; i++) {
                                operation = false;
                                writeData = Constants.RC522_CHANGE_KEY_COMMAND + " 7 " + strInitialKeyA + " A " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                status = rc522Comm.writeRC522(writeData);
                                if (status) {
                                    sleep(Constants.DELEY);//Added on 12082019
                                    readData = rc522Comm.readRC522();
                                    if (readData != null && readData.length > 0) {
                                        strReadData = new String(readData);
                                        strReadData = strReadData.trim();
                                        Log.d("TEST", "Read:" + strReadData);
                                        String arr[] = strReadData.split(":");
                                        if (arr != null && arr.length == 3) {
                                            strReadData = arr[2].trim();
                                            if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                operation = true;
                                                break;
                                            } else {
                                                continue;
                                            }
                                        }
                                    }
                                }
                            }

                            break;
                        case 2:
                            for (int i = 0; i < 3; i++) {
                                operation = false;
                                writeData = Constants.RC522_CHANGE_KEY_COMMAND + " 11 " + strInitialKeyA + " A " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                status = rc522Comm.writeRC522(writeData);
                                if (status) {
                                    sleep(Constants.DELEY);//Added on 12082019
                                    readData = rc522Comm.readRC522();
                                    if (readData != null && readData.length > 0) {
                                        strReadData = new String(readData);
                                        strReadData = strReadData.trim();
                                        Log.d("TEST", "Read:" + strReadData);
                                        String arr[] = strReadData.split(":");
                                        if (arr != null && arr.length == 3) {
                                            strReadData = arr[2].trim();
                                            if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                operation = true;
                                                break;
                                            } else {
                                                continue;
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                        case 3:
                            for (int i = 0; i < 3; i++) {
                                operation = false;
                                writeData = Constants.RC522_CHANGE_KEY_COMMAND + " 15 " + strInitialKeyA + " A " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                status = rc522Comm.writeRC522(writeData);
                                if (status) {
                                    sleep(Constants.DELEY);
                                    readData = rc522Comm.readRC522();
                                    if (readData != null && readData.length > 0) {
                                        strReadData = new String(readData);
                                        strReadData = strReadData.trim();
                                        Log.d("TEST", "Read:" + strReadData);
                                        String arr[] = strReadData.split(":");
                                        if (arr != null && arr.length == 3) {
                                            strReadData = arr[2].trim();
                                            if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                operation = true;
                                                break;
                                            } else {
                                                continue;
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                        case 4:
                            for (int i = 0; i < 3; i++) {
                                operation = false;
                                writeData = Constants.RC522_CHANGE_KEY_COMMAND + " 19 " + strInitialKeyA + " A " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                status = rc522Comm.writeRC522(writeData);
                                if (status) {
                                    sleep(Constants.DELEY);
                                    readData = rc522Comm.readRC522();
                                    if (readData != null && readData.length > 0) {
                                        strReadData = new String(readData);
                                        strReadData = strReadData.trim();
                                        Log.d("TEST", "Read:" + strReadData);
                                        String arr[] = strReadData.split(":");
                                        if (arr != null && arr.length == 3) {
                                            strReadData = arr[2].trim();
                                            if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                operation = true;
                                                break;
                                            } else {
                                                continue;
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                        case 5:
                            for (int i = 0; i < 3; i++) {
                                operation = false;
                                writeData = Constants.RC522_CHANGE_KEY_COMMAND + " 23 " + strInitialKeyA + " A " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                status = rc522Comm.writeRC522(writeData);
                                if (status) {
                                    sleep(Constants.DELEY);
                                    readData = rc522Comm.readRC522();
                                    if (readData != null && readData.length > 0) {
                                        strReadData = new String(readData);
                                        strReadData = strReadData.trim();
                                        Log.d("TEST", "Read:" + strReadData);
                                        String arr[] = strReadData.split(":");
                                        if (arr != null && arr.length == 3) {
                                            strReadData = arr[2].trim();
                                            if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                operation = true;
                                                break;
                                            } else {
                                                continue;
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                        case 6:
                            for (int i = 0; i < 3; i++) {
                                operation = false;
                                writeData = Constants.RC522_CHANGE_KEY_COMMAND + " 27 " + strInitialKeyA + " A " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                status = rc522Comm.writeRC522(writeData);
                                if (status) {
                                    sleep(Constants.DELEY);
                                    readData = rc522Comm.readRC522();
                                    if (readData != null && readData.length > 0) {
                                        strReadData = new String(readData);
                                        strReadData = strReadData.trim();
                                        Log.d("TEST", "Read:" + strReadData);
                                        String arr[] = strReadData.split(":");
                                        if (arr != null && arr.length == 3) {
                                            strReadData = arr[2].trim();
                                            if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                operation = true;
                                                break;
                                            } else {
                                                continue;
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                        case 7:
                            for (int i = 0; i < 3; i++) {
                                operation = false;
                                writeData = Constants.RC522_CHANGE_KEY_COMMAND + " 31 " + strInitialKeyA + " A " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                status = rc522Comm.writeRC522(writeData);
                                if (status) {
                                    sleep(Constants.DELEY);
                                    readData = rc522Comm.readRC522();
                                    if (readData != null && readData.length > 0) {
                                        strReadData = new String(readData);
                                        strReadData = strReadData.trim();
                                        Log.d("TEST", "Read:" + strReadData);
                                        String arr[] = strReadData.split(":");
                                        if (arr != null && arr.length == 3) {
                                            strReadData = arr[2].trim();
                                            if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                operation = true;
                                                break;
                                            } else {
                                                continue;
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                        case 8:
                            for (int i = 0; i < 3; i++) {
                                operation = false;
                                writeData = Constants.RC522_CHANGE_KEY_COMMAND + " 35 " + strInitialKeyA + " A " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                status = rc522Comm.writeRC522(writeData);
                                if (status) {
                                    sleep(Constants.DELEY);
                                    readData = rc522Comm.readRC522();
                                    if (readData != null && readData.length > 0) {
                                        strReadData = new String(readData);
                                        strReadData = strReadData.trim();
                                        Log.d("TEST", "Read:" + strReadData);
                                        String arr[] = strReadData.split(":");
                                        if (arr != null && arr.length == 3) {
                                            strReadData = arr[2].trim();
                                            if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                operation = true;
                                                break;
                                            } else {
                                                continue;
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                        case 9:
                            for (int i = 0; i < 3; i++) {
                                operation = false;
                                writeData = Constants.RC522_CHANGE_KEY_COMMAND + " 39 " + strInitialKeyA + " A " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                status = rc522Comm.writeRC522(writeData);
                                if (status) {
                                    sleep(Constants.DELEY);
                                    readData = rc522Comm.readRC522();
                                    if (readData != null && readData.length > 0) {
                                        strReadData = new String(readData);
                                        strReadData = strReadData.trim();
                                        Log.d("TEST", "Read:" + strReadData);
                                        String arr[] = strReadData.split(":");
                                        if (arr != null && arr.length == 3) {
                                            strReadData = arr[2].trim();
                                            if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                operation = true;
                                                break;
                                            } else {
                                                continue;
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                        case 10:
                            for (int i = 0; i < 3; i++) {
                                operation = false;
                                writeData = Constants.RC522_CHANGE_KEY_COMMAND + " 43 " + strInitialKeyA + " A " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                status = rc522Comm.writeRC522(writeData);
                                if (status) {
                                    sleep(Constants.DELEY);
                                    readData = rc522Comm.readRC522();
                                    if (readData != null && readData.length > 0) {
                                        strReadData = new String(readData);
                                        strReadData = strReadData.trim();
                                        Log.d("TEST", "Read:" + strReadData);
                                        String arr[] = strReadData.split(":");
                                        if (arr != null && arr.length == 3) {
                                            strReadData = arr[2].trim();
                                            if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                operation = true;
                                                break;
                                            } else {
                                                continue;
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                        case 11:
                            for (int i = 0; i < 3; i++) {
                                operation = false;
                                writeData = Constants.RC522_CHANGE_KEY_COMMAND + " 47 " + strInitialKeyA + " A " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                status = rc522Comm.writeRC522(writeData);
                                if (status) {
                                    sleep(Constants.DELEY);
                                    readData = rc522Comm.readRC522();
                                    if (readData != null && readData.length > 0) {
                                        strReadData = new String(readData);
                                        strReadData = strReadData.trim();
                                        Log.d("TEST", "Read:" + strReadData);
                                        String arr[] = strReadData.split(":");
                                        if (arr != null && arr.length == 3) {
                                            strReadData = arr[2].trim();
                                            if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                operation = true;
                                                break;
                                            } else {
                                                continue;
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                        case 12:
                            for (int i = 0; i < 3; i++) {
                                operation = false;
                                writeData = Constants.RC522_CHANGE_KEY_COMMAND + " 51 " + strInitialKeyA + " A " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                status = rc522Comm.writeRC522(writeData);
                                if (status) {
                                    sleep(Constants.DELEY);
                                    readData = rc522Comm.readRC522();
                                    if (readData != null && readData.length > 0) {
                                        strReadData = new String(readData);
                                        strReadData = strReadData.trim();
                                        Log.d("TEST", "Read:" + strReadData);
                                        String arr[] = strReadData.split(":");
                                        if (arr != null && arr.length == 3) {
                                            strReadData = arr[2].trim();
                                            if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                operation = true;
                                                break;
                                            } else {
                                                continue;
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                        case 13:
                            for (int i = 0; i < 3; i++) {
                                operation = false;
                                writeData = Constants.RC522_CHANGE_KEY_COMMAND + " 55 " + strInitialKeyA + " A " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                status = rc522Comm.writeRC522(writeData);
                                if (status) {
                                    sleep(Constants.DELEY);
                                    readData = rc522Comm.readRC522();
                                    if (readData != null && readData.length > 0) {
                                        strReadData = new String(readData);
                                        strReadData = strReadData.trim();
                                        Log.d("TEST", "Read:" + strReadData);
                                        String arr[] = strReadData.split(":");
                                        if (arr != null && arr.length == 3) {
                                            strReadData = arr[2].trim();
                                            if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                operation = true;
                                                break;
                                            } else {
                                                continue;
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                        case 14:
                            for (int i = 0; i < 3; i++) {
                                operation = false;
                                writeData = Constants.RC522_CHANGE_KEY_COMMAND + " 59 " + strInitialKeyA + " A " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                status = rc522Comm.writeRC522(writeData);
                                if (status) {
                                    sleep(Constants.DELEY);
                                    readData = rc522Comm.readRC522();
                                    if (readData != null && readData.length > 0) {
                                        strReadData = new String(readData);
                                        strReadData = strReadData.trim();
                                        Log.d("TEST", "Read:" + strReadData);
                                        String arr[] = strReadData.split(":");
                                        if (arr != null && arr.length == 3) {
                                            strReadData = arr[2].trim();
                                            if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                operation = true;
                                                break;
                                            } else {
                                                continue;
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                        case 15:
                            for (int i = 0; i < 3; i++) {
                                operation = false;
                                writeData = Constants.RC522_CHANGE_KEY_COMMAND + " 63 " + strInitialKeyA + " A " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                status = rc522Comm.writeRC522(writeData);
                                if (status) {
                                    sleep(Constants.DELEY);
                                    readData = rc522Comm.readRC522();
                                    if (readData != null && readData.length > 0) {
                                        strReadData = new String(readData);
                                        strReadData = strReadData.trim();
                                        Log.d("TEST", "Read:" + strReadData);
                                        String arr[] = strReadData.split(":");
                                        if (arr != null && arr.length == 3) {
                                            strReadData = arr[2].trim();
                                            if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                operation = true;
                                                break;
                                            } else {
                                                continue;
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                        default:
                            break;
                    }
                    if (!operation) {
                        break;
                    }
                }

                sectorKeyData.close();

                if (operation) {

                    //============================== MAD UPDATE =================================//

                    operation = false;
                    String strData_01 = "55010005000000000000000000000000";
                    String strData_02 = "00000000000000000000000000000000";
                    operation = updateMAD(strData_01, strData_02);

                    //=================== UPDATE SECTOR 1 AND WRITE CARD ID =======================//

                    if (operation) {
                        if (cardId.trim().length() > 0) {
                            operation = writeSectorOne(cardId);
                        }
                    }
                }
            }
            return operation;
        }

        @Override
        protected void onPostExecute(Boolean status) {
            mypDialog.cancel();
            clear();
            btn_CardInit.setEnabled(true);
            if (status) {
                showCustomAlertDialog(true, "Card Init Status", "Card Initialised Successfully");
            } else {
                showCustomAlertDialog(false, "Card Init Status", "Failed To Initialise Card");
            }
        }
    }

    private class AsyncTaskRC522FactoryCard extends AsyncTask <Void, Void, Boolean> {

        ProgressDialog mypDialog;
        RC522Communicator rc522Comm;

        AsyncTaskRC522FactoryCard(RC522Communicator rc522Comm) {
            this.rc522Comm = rc522Comm;
        }

        @Override
        protected void onPreExecute() {
            mypDialog = new ProgressDialog(SmartCardActivity.this);
            mypDialog.setMessage("Card Converting Wait...");
            mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mypDialog.setCanceledOnTouchOutside(false);
            mypDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            //Read Data:WRITE_CARD:3:KEY_wr_SUCCESS
            boolean operation = false;
            Cursor sectorKeyData = dbComm.getSectorAndKeyForWriteCard();
            if (sectorKeyData != null) {
                String strFinalKeyA = "FFFFFFFFFFFF";
                String strFinalKeyB = "FFFFFFFFFFFF";
                String strReadData = null;
                String writeData = "";
                char[] readData = null;
                boolean status = false;
                while (sectorKeyData.moveToNext()) {
                    String strSectorNo = sectorKeyData.getString(0).trim();
                    String strInitialKeyA = sectorKeyData.getString(1).trim();
                    String strInitialKeyB = sectorKeyData.getString(2).trim();
                    int sectorNo = Integer.parseInt(strSectorNo);
                    String strAccessCode = "FF078069";
                    switch (sectorNo) {
                        case 0:
                            for (int i = 0; i < 3; i++) {
                                operation = false;
                                writeData = Constants.RC522_CHANGE_KEY_COMMAND + " 3 " + strInitialKeyB + " B " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                Log.d("TEST", "Sector 0:" + writeData);
                                status = rc522Comm.writeRC522(writeData);
                                if (status) {
                                    sleep(Constants.DELEY);
                                    readData = rc522Comm.readRC522();
                                    if (readData != null && readData.length > 0) {
                                        strReadData = new String(readData);
                                        strReadData = strReadData.trim();
                                        Log.d("TEST", "Read:" + strReadData);
                                        String arr[] = strReadData.split(":");
                                        if (arr != null && arr.length == 3) {
                                            strReadData = arr[2].trim();
                                            if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                operation = true;
                                                break;
                                            } else {
                                                continue;
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                        case 1:
                            for (int i = 0; i < 3; i++) {
                                operation = false;
                                writeData = Constants.RC522_CHANGE_KEY_COMMAND + " 7 " + strInitialKeyB + " B " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                Log.d("TEST", "Sector 1:" + writeData);
                                status = rc522Comm.writeRC522(writeData);
                                if (status) {
                                    sleep(Constants.DELEY);
                                    readData = rc522Comm.readRC522();
                                    if (readData != null && readData.length > 0) {
                                        strReadData = new String(readData);
                                        strReadData = strReadData.trim();
                                        Log.d("TEST", "Read:" + strReadData);
                                        String arr[] = strReadData.split(":");
                                        if (arr != null && arr.length == 3) {
                                            strReadData = arr[2].trim();
                                            if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                operation = true;
                                                break;
                                            } else {
                                                continue;
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                        case 2:
                            for (int i = 0; i < 3; i++) {
                                operation = false;
                                writeData = Constants.RC522_CHANGE_KEY_COMMAND + " 11 " + strInitialKeyB + " B " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                Log.d("TEST", "Sector 2:" + writeData);
                                status = rc522Comm.writeRC522(writeData);
                                if (status) {
                                    sleep(Constants.DELEY);
                                    readData = rc522Comm.readRC522();
                                    if (readData != null && readData.length > 0) {
                                        strReadData = new String(readData);
                                        strReadData = strReadData.trim();
                                        Log.d("TEST", "Read:" + strReadData);
                                        String arr[] = strReadData.split(":");
                                        if (arr != null && arr.length == 3) {
                                            strReadData = arr[2].trim();
                                            if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                operation = true;
                                                break;
                                            } else {
                                                continue;
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                        case 3:
                            for (int i = 0; i < 3; i++) {
                                operation = false;
                                writeData = Constants.RC522_CHANGE_KEY_COMMAND + " 15 " + strInitialKeyB + " B " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                Log.d("TEST", "Sector 3:" + writeData);
                                status = rc522Comm.writeRC522(writeData);
                                if (status) {
                                    sleep(Constants.DELEY);
                                    readData = rc522Comm.readRC522();
                                    if (readData != null && readData.length > 0) {
                                        strReadData = new String(readData);
                                        strReadData = strReadData.trim();
                                        Log.d("TEST", "Read:" + strReadData);
                                        String arr[] = strReadData.split(":");
                                        if (arr != null && arr.length == 3) {
                                            strReadData = arr[2].trim();
                                            if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                operation = true;
                                                break;
                                            } else {
                                                continue;
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                        case 4:
                            for (int i = 0; i < 3; i++) {
                                operation = false;
                                writeData = Constants.RC522_CHANGE_KEY_COMMAND + " 19 " + strInitialKeyB + " B " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                Log.d("TEST", "Sector 4:" + writeData);
                                status = rc522Comm.writeRC522(writeData);
                                if (status) {
                                    sleep(Constants.DELEY);
                                    readData = rc522Comm.readRC522();
                                    if (readData != null && readData.length > 0) {
                                        strReadData = new String(readData);
                                        strReadData = strReadData.trim();
                                        Log.d("TEST", "Read:" + strReadData);
                                        String arr[] = strReadData.split(":");
                                        if (arr != null && arr.length == 3) {
                                            strReadData = arr[2].trim();
                                            if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                operation = true;
                                                break;
                                            } else {
                                                continue;
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                        case 5:
                            for (int i = 0; i < 3; i++) {
                                operation = false;
                                writeData = Constants.RC522_CHANGE_KEY_COMMAND + " 23 " + strInitialKeyB + " B " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                Log.d("TEST", "Sector 5:" + writeData);
                                status = rc522Comm.writeRC522(writeData);
                                if (status) {
                                    sleep(Constants.DELEY);
                                    readData = rc522Comm.readRC522();
                                    if (readData != null && readData.length > 0) {
                                        strReadData = new String(readData);
                                        strReadData = strReadData.trim();
                                        Log.d("TEST", "Read:" + strReadData);
                                        String arr[] = strReadData.split(":");
                                        if (arr != null && arr.length == 3) {
                                            strReadData = arr[2].trim();
                                            if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                operation = true;
                                                break;
                                            } else {
                                                continue;
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                        case 6:
                            for (int i = 0; i < 3; i++) {
                                operation = false;
                                writeData = Constants.RC522_CHANGE_KEY_COMMAND + " 27 " + strInitialKeyB + " B " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                Log.d("TEST", "Sector 6:" + writeData);
                                status = rc522Comm.writeRC522(writeData);
                                if (status) {
                                    sleep(Constants.DELEY);
                                    readData = rc522Comm.readRC522();
                                    if (readData != null && readData.length > 0) {
                                        strReadData = new String(readData);
                                        strReadData = strReadData.trim();
                                        Log.d("TEST", "Read:" + strReadData);
                                        String arr[] = strReadData.split(":");
                                        if (arr != null && arr.length == 3) {
                                            strReadData = arr[2].trim();
                                            if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                operation = true;
                                                break;
                                            } else {
                                                continue;
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                        case 7:
                            for (int i = 0; i < 3; i++) {
                                operation = false;
                                writeData = Constants.RC522_CHANGE_KEY_COMMAND + " 31 " + strInitialKeyB + " B " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                Log.d("TEST", "Sector 7:" + writeData);
                                status = rc522Comm.writeRC522(writeData);
                                if (status) {
                                    sleep(Constants.DELEY);
                                    readData = rc522Comm.readRC522();
                                    if (readData != null && readData.length > 0) {
                                        strReadData = new String(readData);
                                        strReadData = strReadData.trim();
                                        Log.d("TEST", "Read:" + strReadData);
                                        String arr[] = strReadData.split(":");
                                        if (arr != null && arr.length == 3) {
                                            strReadData = arr[2].trim();
                                            if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                operation = true;
                                                break;
                                            } else {
                                                continue;
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                        case 8:
                            for (int i = 0; i < 3; i++) {
                                operation = false;
                                writeData = Constants.RC522_CHANGE_KEY_COMMAND + " 35 " + strInitialKeyB + " B " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                Log.d("TEST", "Sector 8:" + writeData);
                                status = rc522Comm.writeRC522(writeData);
                                if (status) {
                                    sleep(Constants.DELEY);
                                    readData = rc522Comm.readRC522();
                                    if (readData != null && readData.length > 0) {
                                        strReadData = new String(readData);
                                        strReadData = strReadData.trim();
                                        Log.d("TEST", "Read:" + strReadData);
                                        String arr[] = strReadData.split(":");
                                        if (arr != null && arr.length == 3) {
                                            strReadData = arr[2].trim();
                                            if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                operation = true;
                                                break;
                                            } else {
                                                continue;
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                        case 9:
                            for (int i = 0; i < 3; i++) {
                                operation = false;
                                writeData = Constants.RC522_CHANGE_KEY_COMMAND + " 39 " + strInitialKeyB + " B " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                Log.d("TEST", "Sector 9:" + writeData);
                                status = rc522Comm.writeRC522(writeData);
                                if (status) {
                                    sleep(Constants.DELEY);
                                    readData = rc522Comm.readRC522();
                                    if (readData != null && readData.length > 0) {
                                        strReadData = new String(readData);
                                        strReadData = strReadData.trim();
                                        Log.d("TEST", "Read:" + strReadData);
                                        String arr[] = strReadData.split(":");
                                        if (arr != null && arr.length == 3) {
                                            strReadData = arr[2].trim();
                                            if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                operation = true;
                                                break;
                                            } else {
                                                continue;
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                        case 10:
                            for (int i = 0; i < 3; i++) {
                                operation = false;
                                writeData = Constants.RC522_CHANGE_KEY_COMMAND + " 43 " + strInitialKeyB + " B " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                Log.d("TEST", "Sector 10:" + writeData);
                                status = rc522Comm.writeRC522(writeData);
                                if (status) {
                                    sleep(Constants.DELEY);
                                    readData = rc522Comm.readRC522();
                                    if (readData != null && readData.length > 0) {
                                        strReadData = new String(readData);
                                        strReadData = strReadData.trim();
                                        Log.d("TEST", "Read:" + strReadData);
                                        String arr[] = strReadData.split(":");
                                        if (arr != null && arr.length == 3) {
                                            strReadData = arr[2].trim();
                                            if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                operation = true;
                                                break;
                                            } else {
                                                continue;
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                        case 11:
                            for (int i = 0; i < 3; i++) {
                                operation = false;
                                writeData = Constants.RC522_CHANGE_KEY_COMMAND + " 47 " + strInitialKeyB + " B " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                Log.d("TEST", "Sector 11:" + writeData);
                                status = rc522Comm.writeRC522(writeData);
                                if (status) {
                                    sleep(Constants.DELEY);
                                    readData = rc522Comm.readRC522();
                                    if (readData != null && readData.length > 0) {
                                        strReadData = new String(readData);
                                        strReadData = strReadData.trim();
                                        Log.d("TEST", "Read:" + strReadData);
                                        String arr[] = strReadData.split(":");
                                        if (arr != null && arr.length == 3) {
                                            strReadData = arr[2].trim();
                                            if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                operation = true;
                                                break;
                                            } else {
                                                continue;
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                        case 12:
                            for (int i = 0; i < 3; i++) {
                                operation = false;
                                writeData = Constants.RC522_CHANGE_KEY_COMMAND + " 51 " + strInitialKeyB + " B " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                Log.d("TEST", "Sector 12:" + writeData);
                                status = rc522Comm.writeRC522(writeData);
                                if (status) {
                                    sleep(Constants.DELEY);
                                    readData = rc522Comm.readRC522();
                                    if (readData != null && readData.length > 0) {
                                        strReadData = new String(readData);
                                        strReadData = strReadData.trim();
                                        Log.d("TEST", "Read:" + strReadData);
                                        String arr[] = strReadData.split(":");
                                        if (arr != null && arr.length == 3) {
                                            strReadData = arr[2].trim();
                                            if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                operation = true;
                                                break;
                                            } else {
                                                continue;
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                        case 13:
                            for (int i = 0; i < 3; i++) {
                                operation = false;
                                writeData = Constants.RC522_CHANGE_KEY_COMMAND + " 55 " + strInitialKeyB + " B " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                Log.d("TEST", "Sector 13:" + writeData);
                                status = rc522Comm.writeRC522(writeData);
                                if (status) {
                                    sleep(Constants.DELEY);
                                    readData = rc522Comm.readRC522();
                                    if (readData != null && readData.length > 0) {
                                        strReadData = new String(readData);
                                        strReadData = strReadData.trim();
                                        Log.d("TEST", "Read:" + strReadData);
                                        String arr[] = strReadData.split(":");
                                        if (arr != null && arr.length == 3) {
                                            strReadData = arr[2].trim();
                                            if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                operation = true;
                                                break;
                                            } else {
                                                continue;
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                        case 14:
                            for (int i = 0; i < 3; i++) {
                                operation = false;
                                writeData = Constants.RC522_CHANGE_KEY_COMMAND + " 59 " + strInitialKeyB + " B " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                Log.d("TEST", "Sector 14:" + writeData);
                                status = rc522Comm.writeRC522(writeData);
                                if (status) {
                                    sleep(Constants.DELEY);
                                    readData = rc522Comm.readRC522();
                                    if (readData != null && readData.length > 0) {
                                        strReadData = new String(readData);
                                        strReadData = strReadData.trim();
                                        Log.d("TEST", "Read:" + strReadData);
                                        String arr[] = strReadData.split(":");
                                        if (arr != null && arr.length == 3) {
                                            strReadData = arr[2].trim();
                                            if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                operation = true;
                                                break;
                                            } else {
                                                continue;
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                        case 15:
                            for (int i = 0; i < 3; i++) {
                                operation = false;
                                writeData = Constants.RC522_CHANGE_KEY_COMMAND + " 63 " + strInitialKeyB + " B " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                Log.d("TEST", "Sector 15:" + writeData);
                                status = rc522Comm.writeRC522(writeData);
                                if (status) {
                                    sleep(Constants.DELEY);
                                    readData = rc522Comm.readRC522();
                                    if (readData != null && readData.length > 0) {
                                        strReadData = new String(readData);
                                        strReadData = strReadData.trim();
                                        Log.d("TEST", "Read:" + strReadData);
                                        String arr[] = strReadData.split(":");
                                        if (arr != null && arr.length == 3) {
                                            strReadData = arr[2].trim();
                                            if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                operation = true;
                                                break;
                                            } else {
                                                continue;
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                        default:
                            break;
                    }

                    if (!operation) {
                        break;
                    }
                }

                sectorKeyData.close();

                if (operation) {

                    //============================== MAD UPDATE =================================//

//                    operation = false;
//                    String strData_01 = "55010005000000000000000000000000";
//                    String strData_02 = "00000000000000000000000000000000";
//                    operation = updateMAD(strData_01, strData_02);

                    //=================== UPDATE SECTOR 1 AND WRITE CARD ID =======================//

//                    if (operation) {
//                        if (cardId.trim().length() > 0) {
//                            operation = writeSectorOne(cardId);
//                        }
//                    }
                }
            }
            return operation;
        }

        @Override
        protected void onPostExecute(Boolean status) {
            mypDialog.cancel();
            clear();
            if (status) {
                showCustomAlertDialog(true, "Card Convert Status", "Card Converted Successfully");
            } else {
                showCustomAlertDialog(false, "Card Convert Status", "Failed To Converte Card");
            }
        }
    }


    private boolean updateMAD(String strData_01, String strData_02) {

        //========================== UPDATE BLOCK 1 MAD ZERO ========================//

        RC522Communicator rc522Comm = new RC522Communicator();
        boolean operation = false;
        String writeData = Constants.RC522_WRITE_COMMAND + " 1 " + "524553534543" + " B " + strData_01;
        boolean status = rc522Comm.writeRC522(writeData);
        if (status) {
            char[] readData = rc522Comm.readRC522();
            if (readData != null && readData.length > 0) {
                String strReadData = new String(readData);
                strReadData = strReadData.trim();
                String arr[] = strReadData.split(":");
                if (arr != null && arr.length == 3) {
                    strReadData = arr[2].trim();
                    if (strReadData.equals("SUCCESS")) {
                        operation = true;
                    }
                }
            }
        }

        //========================== UPDATE BLOCK 2 MAD ONE ============================//

        operation = false;
        writeData = Constants.RC522_WRITE_COMMAND + " 2 " + "524553534543" + " B " + strData_02;
        status = rc522Comm.writeRC522(writeData);
        if (status) {
            char[] readData = rc522Comm.readRC522();
            if (readData != null && readData.length > 0) {
                String strReadData = new String(readData);
                strReadData = strReadData.trim();
                String arr[] = strReadData.split(":");
                if (arr != null && arr.length == 3) {
                    strReadData = arr[2].trim();
                    if (strReadData.equals("SUCCESS")) {
                        operation = true;
                    }
                }
            }
        }

        return operation;
    }

    private boolean writeSectorOne(String cardId) {

        //=================== UPDATE SECTOR 1 AND WRITE CARD ID =======================//

        cardId = Utility.paddCardId(cardId);
        String strHexCardId = Utility.asciiToHex(cardId);
        String strData_10 = strHexCardId + "464F5254554E4120";
        String strData_11 = "11303030AAFFFFFFFFFFFFFFFFFFFFFF";
        String strData_12 = "00000000000000000000000000000000";

        //========================== UPDATE BLOCK 4 ==================================//

        RC522Communicator rc522Comm = new RC522Communicator();
        boolean operation = false;
        String writeData = Constants.RC522_WRITE_COMMAND + " 4 " + "435053204944" + " B " + strData_10;
        boolean status = rc522Comm.writeRC522(writeData);
        if (status) {
            char[] readData = rc522Comm.readRC522();
            if (readData != null && readData.length > 0) {
                String strReadData = new String(readData);
                strReadData = strReadData.trim();
                String arr[] = strReadData.split(":");
                if (arr != null && arr.length == 3) {
                    strReadData = arr[2].trim();
                    if (strReadData.equals("SUCCESS")) {
                        operation = true;
                    }
                }
            }
        }

        //========================== UPDATE BLOCK 5 ==================================//

        operation = false;
        writeData = Constants.RC522_WRITE_COMMAND + " 5 " + "435053204944" + " B " + strData_11;
        status = rc522Comm.writeRC522(writeData);
        if (status) {
            char[] readData = rc522Comm.readRC522();
            if (readData != null && readData.length > 0) {
                String strReadData = new String(readData);
                strReadData = strReadData.trim();
                String arr[] = strReadData.split(":");
                if (arr != null && arr.length == 3) {
                    strReadData = arr[2].trim();
                    Log.d("TEST", "Read:" + strReadData);
                    if (strReadData.equals("SUCCESS")) {
                        operation = true;
                    }
                }
            }
        }


        //========================== UPDATE BLOCK 6 ==================================//
        operation = false;
        writeData = Constants.RC522_WRITE_COMMAND + " 6 " + "435053204944" + " B " + strData_12;
        status = rc522Comm.writeRC522(writeData);
        if (status) {
            char[] readData = rc522Comm.readRC522();
            if (readData != null && readData.length > 0) {
                String strReadData = new String(readData);
                strReadData = strReadData.trim();
                String arr[] = strReadData.split(":");
                if (arr != null && arr.length == 3) {
                    strReadData = arr[2].trim();
                    Log.d("TEST", "Read:" + strReadData);
                    if (strReadData.equals("SUCCESS")) {
                        operation = true;
                    }
                }
            }
        }

        return operation;
    }


    //===================================== Async Task Card ID Change ===================================//

    private class AsyncTaskUsbCardIdChange extends AsyncTask <String, Void, Integer> {

        ProgressDialog mypDialog;
        MicroSmartV2Communicator mSmartV2Comm;
        SmartCardInfo cardInfo;
        String newCardId;

        AsyncTaskUsbCardIdChange(MicroSmartV2Communicator mSmartV2Comm, SmartCardInfo cardInfo, String newCardId) {
            this.mSmartV2Comm = mSmartV2Comm;
            this.cardInfo = cardInfo;
            this.newCardId = newCardId;
        }

        @Override
        protected void onPreExecute() {
            mypDialog = new ProgressDialog(SmartCardActivity.this);
            mypDialog.setMessage("Card Id Changing Wait...");
            mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mypDialog.setCanceledOnTouchOutside(false);
            mypDialog.show();
        }

        @Override
        protected Integer doInBackground(String... params) {
            int result = -1;
            String strHexCardId = Utility.asciiToHex(Utility.paddCardId(newCardId));
            String strData_10 = strHexCardId + "464F5254554E4120";
            String strData_11 = "11303030AAFFFFFFFFFFFFFFFFFFFFFF";
            String strData_12 = "00000000000000000000000000000000";
            String command = "#FF4701A10143505320494400" + strData_10 + strData_11 + strData_12;
            String msg = Utility.addCheckSum(command);
            result = mSmartV2Comm.sectorWrite(msg, 1);
            command = Utility.addCheckSum(Constants.READ_CARD_ID_COMMAND);
            String strCardId = mSmartV2Comm.readCardId(command.getBytes());
            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {
            mypDialog.cancel();
            clear();
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat mdformat = new SimpleDateFormat("ddMMyyyy HH:mm:ss");
            String strDateTime = mdformat.format(calendar.getTime());
            if (result != -1) {
                String strOperation = "Card Id Change";
                String strReason = "Card Id Change";
                String strStatus = "Y";
                int loginId = UserDetails.getInstance().getLoginId();

                int autoId = -1;
                autoId = dbComm.getEmpAutoId(cardInfo.getReadCSN().trim(), cardInfo.getCardId().trim());
                int cardVersion = dbComm.getSmartCardIssuedVer(autoId);
                int newCardVersion = cardVersion + 1;


                //============================================= Insert Into Smart Card Opeartion Log =========================================================//

                int insertStatus = -1;
                insertStatus = dbComm.insertIntoSmartCardOperationLog(loginId, autoId, cardInfo.getReadCSN(), cardInfo.getCardId(), cardVersion, newCardId, newCardVersion, strOperation, strStatus, "Y", strDateTime);
                if (insertStatus != -1) {
                    Log.d("TEST", "Smart Card Operation Log Inserted Successfully");
                } else {
                    Log.d("TEST", "Smart Card Operation Log Insertion Failure");
                }

                //============================================= Insert Into Hot List Log =========================================================//

                insertStatus = dbComm.insertIntoHotListLog(loginId, autoId, cardInfo.getReadCSN(), cardInfo.getCardId(), cardVersion, strOperation, strStatus, "Y", strReason, strDateTime);
                if (insertStatus != -1) {
                    Log.d("TEST", "Hot List Log Inserted Successfully");
                } else {
                    Log.d("TEST", "Hot List Log Insertion Failure");
                }

                //============================================= Update Employee CSN =========================================================//

                int isDataUpdated = -1;
                isDataUpdated = dbComm.updateSmartCardVer(autoId, cardInfo.getReadCSN(), Integer.toString(newCardVersion));
                if (isDataUpdated != -1) {
                    Log.d("TEST", "Employee CSN Updated Successfully");
                } else {
                    Log.d("TEST", "Failed To Update Employee CSN");
                }
                showCustomAlertDialog(true, "Card ID Changed", "Card Id Changed Successfully");
            } else {
                showCustomAlertDialog(false, "Card ID Changed", "Failed To Change Card Id");
            }
        }
    }

    //================================  Async Task SPI Card Id Change  =========================================//

    private class AsyncTaskSpiCardIdChange extends AsyncTask <String, Void, Integer> {

        ProgressDialog mypDialog;

        @Override
        protected void onPreExecute() {
            mypDialog = new ProgressDialog(SmartCardActivity.this);
            mypDialog.setMessage("Card Id Changing Wait...");
            mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mypDialog.setCanceledOnTouchOutside(false);
            mypDialog.show();
        }

        @Override
        protected Integer doInBackground(String... params) {

            int error = -1;
            String strCardId = params[0].trim();
            byte[] keya = new byte[]{0x45, 0x44, 0x43, 0x42, 0x41, 0x40};
            byte[] keyb = new byte[]{0x43, 0x50, 0x53, 0x20, 0x49, 0x44};


            strCardId = Utility.paddCardId(strCardId);
            byte[] cardId = strCardId.getBytes();

            error = rc632ReaderConnection.sector_1_Refresh((byte) 1, keya, keyb, cardId);
            Log.d("TEST", "Card id change error no:" + error);

            return error;
        }

        @Override
        protected void onPostExecute(Integer result) {
            mypDialog.cancel();
            if (result != -1) {
                showCustomAlertDialog(true, "Card ID Changed", "Card ID Changed Successfully");
            } else {
                showCustomAlertDialog(false, "Card ID Changed", "Failed To Change Card Id");
            }
            clear();
        }
    }

    //================================  Async Task RC522 Card Id Change  =========================================//

    private class AsyncTaskRC522CardIdChange extends AsyncTask <String, Void, Boolean> {

        ProgressDialog mypDialog;

        @Override
        protected void onPreExecute() {
            mypDialog = new ProgressDialog(SmartCardActivity.this);
            mypDialog.setMessage("Card Id Changing Wait...");
            mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mypDialog.setCanceledOnTouchOutside(false);
            mypDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            boolean status = false;
            status = writeSectorOne(params[0].trim());
            return status;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            mypDialog.cancel();
            btn_CardIdChange.setEnabled(true);
            clear();
            if (result) {
                showCustomAlertDialog(true, "Card ID Changed", "Card ID Changed Successfully");
            } else {
                showCustomAlertDialog(false, "Card ID Changed", "Failed To Change Card Id");
            }

        }
    }


    //================================  Async Task For Card Refresh  =========================================//


    private class AsyncTaskUsbCardRefresh extends AsyncTask <Void, Void, Integer> {


        ProgressDialog mypDialog;
        String strLog;
        int dataWriteStatus = -1;
        MicroSmartV2Communicator mSmartV2Comm;
        SmartCardInfo cardInfo;
        String newCardId;

        AsyncTaskUsbCardRefresh(MicroSmartV2Communicator mSmartV2Comm, SmartCardInfo cardInfo, String newCardId) {
            this.mSmartV2Comm = mSmartV2Comm;
            this.cardInfo = cardInfo;
            this.newCardId = newCardId;
        }

        @Override
        protected void onPreExecute() {
            mypDialog = new ProgressDialog(SmartCardActivity.this);
            mypDialog.setMessage("Card Refreshing Wait...");
            mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mypDialog.setCanceledOnTouchOutside(false);
            mypDialog.show();
        }

        @Override
        protected Integer doInBackground(Void... params) {

            Cursor sectorKeyData = dbComm.getSectorAndKeyForWriteCard();
            if (sectorKeyData != null) {

                String strEmptyData = "000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
                String msg = "";
                String strFinalKeyB = "353433323130";
                String strAccessCode = "7F0788C1";

                while (sectorKeyData.moveToNext()) {
                    String strSectorNo = sectorKeyData.getString(0);
                    String strFinalKeyA = sectorKeyData.getString(1);
                    String strInitialKeyB = sectorKeyData.getString(2);
                    int sectorNo = Integer.parseInt(strSectorNo);
                    switch (sectorNo) {
                        case 2:
                            msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[0] + strInitialKeyB + Constants.HEX_READ_WRITE + strEmptyData);
                            dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                            if (dataWriteStatus != -1) {
                                strLog += "Write Successfull To Sector 2\n";
                                dataWriteStatus = mSmartV2Comm.keyUpdate(11, strFinalKeyA, strAccessCode, strFinalKeyB);
                                if (dataWriteStatus != -1) {
                                    strLog += "Sector 2 Key Updated Successfully\n";
                                } else {
                                    strLog += "Sector 2 Key Updation Failure\n";
                                }
                            } else {
                                strLog += "Failed To Write To Sector 2\n";
                            }
                            break;

//                            case 3:
//                                msg = Add + sec[1] + strInitialKeyB + "01" + strAadhaarId;
//                                a2 = msg.toCharArray();
//                                checksum = 0;
//                                for (i = 0; i < a2.length; i++) {
//                                    checksum ^= a2[i];
//                                }
//                                c2 = checksum;
//                                if(Integer.toHexString(c2).toUpperCase().length() != 2) {
//                                    msg = msg + "0" + Integer.toHexString(c2).toUpperCase() + "%";
//                                }else {
//                                    msg = msg + Integer.toHexString(c2).toUpperCase() + "%";
//                                }
//
//                                dataWriteStatus=Sector_3_Write(msg, "3");
//
//                                if(dataWriteStatus!=-1) {
//
//                                    strLog+="Write Successfull To Sector 3\n";
//
//                                    keyWriteStatus=CardKeyUpdate(3,15,strFinalKeyA,strAccessCode,strFinalKeyB);
//
//                                    if(keyWriteStatus!=-1) {
//
//                                        strLog+="Sector 3 Key Updated Successfully\n";
//                                    }
//                                    else {
//
//                                        strLog+="Sector 3 Key Updation Failure\n";
//                                    }
//                                }
//                                else {
//
//                                    strLog+="Failed To Write To Sector 3\n";
//                                }
//                                break;

                        case 4:
                            msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[2] + strInitialKeyB + Constants.HEX_READ_WRITE + strEmptyData);
                            dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                            if (dataWriteStatus != -1) {
                                strLog += "Write Successfull To Sector 4\n";
                                dataWriteStatus = mSmartV2Comm.keyUpdate(19, strFinalKeyA, strAccessCode, strFinalKeyB);
                                if (dataWriteStatus != -1) {
                                    strLog += "Sector 4 Key Updated Successfully\n";
                                } else {
                                    strLog += "Sector 4 Key Updation Failure\n";
                                }
                            } else {
                                strLog += "Failed To Write To Sector 4\n";
                            }
                            break;
                        case 5:
                            msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[3] + strInitialKeyB + Constants.HEX_READ_WRITE + strEmptyData);
                            dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                            if (dataWriteStatus != -1) {
                                strLog += "Write Successfull To Sector 5\n";
                                dataWriteStatus = mSmartV2Comm.keyUpdate(23, strFinalKeyA, strAccessCode, strFinalKeyB);
                                if (dataWriteStatus != -1) {
                                    strLog += "Sector 5 Key Updated Successfully\n";
                                } else {
                                    strLog += "Sector 5 Key Updation Failure\n";
                                }
                            } else {
                                strLog += "Failed To Write To Sector 5\n";
                            }
                            break;
                        case 6:
                            msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[4] + strInitialKeyB + Constants.HEX_READ_WRITE + strEmptyData);
                            dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                            if (dataWriteStatus != -1) {
                                strLog += "Write Successfull To Sector 6\n";
                                dataWriteStatus = mSmartV2Comm.keyUpdate(27, strFinalKeyA, strAccessCode, strFinalKeyB);
                                if (dataWriteStatus != -1) {
                                    strLog += "Sector 6 Key Updated Successfully\n";
                                } else {
                                    strLog += "Sector 6 Key Updation Failure\n";
                                }
                            } else {
                                strLog += "Failed To Write To Sector 6\n";
                            }
                            break;
                        case 7:
                            msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[5] + strInitialKeyB + Constants.HEX_READ_WRITE + strEmptyData);
                            dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                            if (dataWriteStatus != -1) {
                                strLog += "Write Successfull To Sector 7\n";
                                dataWriteStatus = mSmartV2Comm.keyUpdate(31, strFinalKeyA, strAccessCode, strFinalKeyB);
                                if (dataWriteStatus != -1) {
                                    strLog += "Sector 7 Key Updated Successfully\n";
                                } else {
                                    strLog += "Sector 7 Key Updation Failure\n";
                                }
                            } else {
                                strLog += "Failed To Write To Sector 7\n";
                            }
                            break;
                        case 8:
                            msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[6] + strInitialKeyB + Constants.HEX_READ_WRITE + strEmptyData);
                            dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                            if (dataWriteStatus != -1) {
                                strLog += "Write Successfull To Sector 8\n";
                                dataWriteStatus = mSmartV2Comm.keyUpdate(35, strFinalKeyA, strAccessCode, strFinalKeyB);
                                if (dataWriteStatus != -1) {
                                    strLog += "Sector 8 Key Updated Successfully\n";
                                } else {
                                    strLog += "Sector 8 Key Updation Failure\n";
                                }
                            } else {
                                strLog += "Failed To Write To Sector 8\n";
                            }
                            break;
                        case 9:
                            msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[7] + strInitialKeyB + Constants.HEX_READ_WRITE + strEmptyData);
                            dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                            if (dataWriteStatus != -1) {
                                strLog += "Write Successfull To Sector 9\n";
                                dataWriteStatus = mSmartV2Comm.keyUpdate(39, strFinalKeyA, strAccessCode, strFinalKeyB);
                                if (dataWriteStatus != -1) {
                                    strLog += "Sector 9 Key Updated Successfully\n";
                                } else {
                                    strLog += "Sector 9 Key Updation Failure\n";
                                }
                            } else {
                                strLog += "Failed To Write To Sector 9\n";
                            }
                            break;
                        case 10:
                            msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[8] + strInitialKeyB + Constants.HEX_READ_WRITE + strEmptyData);
                            dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                            if (dataWriteStatus != -1) {
                                strLog += "Write Successfull To Sector 10\n";
                                dataWriteStatus = mSmartV2Comm.keyUpdate(43, strFinalKeyA, strAccessCode, strFinalKeyB);
                                if (dataWriteStatus != -1) {
                                    strLog += "Sector 10 Key Updated Successfully\n";
                                } else {
                                    strLog += "Sector 10 Key Updation Failure\n";
                                }
                            } else {
                                strLog += "Failed To Write To Sector 10\n";
                            }
                            break;
                        case 11:
                            msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[9] + strInitialKeyB + Constants.HEX_READ_WRITE + strEmptyData);
                            dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                            if (dataWriteStatus != -1) {
                                strLog += "Write Successfull To Sector 11\n";
                                dataWriteStatus = mSmartV2Comm.keyUpdate(47, strFinalKeyA, strAccessCode, strFinalKeyB);
                                if (dataWriteStatus != -1) {
                                    strLog += "Sector 11 Key Updated Successfully\n";
                                } else {
                                    strLog += "Sector 11 Key Updation Failure\n";
                                }
                            } else {
                                strLog += "Failed To Write To Sector 11\n";
                            }
                            break;
                        case 12:
                            msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[10] + strInitialKeyB + Constants.HEX_READ_WRITE + strEmptyData);
                            dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                            if (dataWriteStatus != -1) {
                                strLog += "Write Successfull To Sector 12\n";
                                dataWriteStatus = mSmartV2Comm.keyUpdate(51, strFinalKeyA, strAccessCode, strFinalKeyB);
                                if (dataWriteStatus != -1) {
                                    strLog += "Sector 12 Key Updated Successfully\n";
                                } else {
                                    strLog += "Sector 12 Key Updation Failure\n";
                                }
                            } else {
                                strLog += "Failed To Write To Sector 12\n";
                            }
                            break;
                        case 13:
                            msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[11] + strInitialKeyB + Constants.HEX_READ_WRITE + strEmptyData);
                            dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                            if (dataWriteStatus != -1) {
                                strLog += "Write Successfull To Sector 13\n";
                                dataWriteStatus = mSmartV2Comm.keyUpdate(55, strFinalKeyA, strAccessCode, strFinalKeyB);
                                if (dataWriteStatus != -1) {
                                    strLog += "Sector 13 Key Updated Successfully\n";
                                } else {
                                    strLog += "Sector 13 Key Updation Failure\n";
                                }
                            } else {
                                strLog += "Failed To Write To Sector 13\n";
                            }
                            break;
                        case 14:
                            msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[12] + strInitialKeyB + Constants.HEX_READ_WRITE + strEmptyData);
                            dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                            if (dataWriteStatus != -1) {
                                strLog += "Write Successfull To Sector 14\n";
                                dataWriteStatus = mSmartV2Comm.keyUpdate(59, strFinalKeyA, strAccessCode, strFinalKeyB);
                                if (dataWriteStatus != -1) {
                                    strLog += "Sector 14 Key Updated Successfully\n";
                                } else {
                                    strLog += "Sector 14 Key Updation Failure\n";
                                }
                            } else {
                                strLog += "Failed To Write To Sector 14\n";
                            }
                            break;
                        case 15:
                            msg = Utility.addCheckSum(Constants.SECTOR_WRITE_COMM[13] + strInitialKeyB + Constants.HEX_READ_WRITE + strEmptyData);
                            dataWriteStatus = mSmartV2Comm.sectorWrite(msg, sectorNo);
                            if (dataWriteStatus != -1) {
                                strLog += "Write Successfull To Sector 15\n";
                                dataWriteStatus = mSmartV2Comm.keyUpdate(63, strFinalKeyA, strAccessCode, strFinalKeyB);
                                if (dataWriteStatus != -1) {
                                    strLog += "Sector 15 Key Updated Successfully\n";
                                } else {
                                    strLog += "Sector 15 Key Updation Failure\n";
                                }
                            } else {
                                strLog += "Failed To Write To Sector 15\n";
                            }
                            break;
                        default:
                            break;
                    }
                    if (dataWriteStatus != -1) {
                        continue;
                    } else {
                        break;
                    }
                }
                if (sectorKeyData != null) {
                    sectorKeyData.close();
                }
            }
            if (dataWriteStatus != -1) {

                //==========================Update MAD Block Of Sector 0 And Write Card Id To Sector 1===========================================//

                String strSectorNo = "01A001";
                int blockno1 = 01;
                int blockno2 = 02;

                String strSec_0_Block_1 = "55010005000000000000000000000000";
                String strSec_0_Block_2 = "00000000000000000000000000000000";

                dataWriteStatus = mSmartV2Comm.madUpdate(strSectorNo, blockno1, "524553534543", strSec_0_Block_1);
                if (dataWriteStatus != -1) {
                    dataWriteStatus = mSmartV2Comm.madUpdate(strSectorNo, blockno2, "524553534543", strSec_0_Block_2);
                    if (dataWriteStatus != -1) {
                        if (newCardId.trim().length() > 0) {

                            //================================Card Id Write To Sector 1===============================//

                            newCardId = Utility.paddCardId(newCardId);
                            String strHexCardId = Utility.asciiToHex(newCardId);
                            String strData_10 = strHexCardId + "464F5254554E4120";
                            String strData_11 = "11303030AAFFFFFFFFFFFFFFFFFFFFFF";
                            String strData_12 = "00000000000000000000000000000000";
                            String msg = Utility.addCheckSum("#FF4701A10143505320494400" + strData_10 + strData_11 + strData_12);
                            dataWriteStatus = mSmartV2Comm.sectorWrite(msg, 1);
                        }
                    }
                }
            }
            String command = Utility.addCheckSum(Constants.READ_CARD_ID_COMMAND);
            String strCardId = mSmartV2Comm.readCardId(command.getBytes());
            return dataWriteStatus;
        }

        @Override
        protected void onPostExecute(Integer result) {
            mypDialog.cancel();
            clear();
            Log.d("TEST", "Card Refresh Log:" + strLog);
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat mdformat = new SimpleDateFormat("ddMMyyyy HH:mm:ss");
            String strDateTime = mdformat.format(calendar.getTime());
            if (dataWriteStatus != -1) {
                int loginId = UserDetails.getInstance().getLoginId();
                String strOperation = "Card Refresh";
                String strReason = "";
                String strStatus = "S";
                String strOldCardId = Utility.paddCardId(cardInfo.getCardId());
                if (newCardId.trim().length() > 0 && !strOldCardId.equals(newCardId)) {
                    strReason = "Card Refresh With Card Id Change";
                    String enrollNo = cardInfo.getEnrollmentNo();
                    if (enrollNo != null && enrollNo.trim().length() > 0) {
                        int id = Integer.parseInt(enrollNo);
                        int cardVersion = dbComm.getSmartCardIssuedVer(id);
                        int newCardVersion = cardVersion + 1;
                        int insertStatus = -1;

                        //============================================= Insert Into Smart Card Opeartion Log =========================================================//

                        insertStatus = dbComm.insertIntoSmartCardOperationLog(loginId, id, cardInfo.getReadCSN(), strOldCardId, cardVersion, newCardId, newCardVersion, strOperation, strStatus, "Y", strDateTime);
                        if (insertStatus != -1) {
                            Log.d("TEST", "Smart Card Operation Log Inserted Successfully");
                        } else {
                            Log.d("TEST", "Smart Card Operation Log Insertion Failure");
                        }

                        //============================================= Insert Into Hot List Log =========================================================//

                        insertStatus = dbComm.insertIntoHotListLog(loginId, id, cardInfo.getReadCSN(), strOldCardId, cardVersion, strOperation, strStatus, "Y", strReason, strDateTime);
                        if (insertStatus != -1) {
                            Log.d("TEST", "Hot List Log Inserted Successfully");
                        } else {
                            Log.d("TEST", "Hot List Log Insertion Failure");
                        }

                        //============================================= Update Employee CSN =========================================================//

                        int isDataUpdated = -1;
                        isDataUpdated = dbComm.updateSmartCardVer(id, cardInfo.getReadCSN(), Integer.toString(newCardVersion));
                        if (isDataUpdated != -1) {
                            Log.d("TEST", "Employee CSN Updated Successfully");
                        } else {
                            Log.d("TEST", "Failed To Update Employee CSN");
                        }
                    }
                } else {
                    String enrollNo = cardInfo.getEnrollmentNo();
                    if (enrollNo != null && enrollNo.trim().length() > 0) {
                        int id = Integer.parseInt(enrollNo);
                        int cardVersion = dbComm.getSmartCardIssuedVer(id);

                        //=============================================Insert Into Smart Card Opeartion Log=========================================================//

                        int insertStatus = -1;
                        insertStatus = dbComm.insertIntoSmartCardOperationLog(loginId, id, cardInfo.getReadCSN(), strOldCardId, cardVersion, "", cardVersion, strOperation, strStatus, "Y", strDateTime);
                        if (insertStatus != -1) {
                            Log.d("TEST", "Smart Card Operation Log Inserted Successfully");
                        } else {
                            Log.d("TEST", "Smart Card Operation Log Insertion Failure");
                        }
                    }
                }
                showCustomAlertDialog(true, "Card Reresh Status", "Card Refreshed Successfully");
            } else {
                int loginId = UserDetails.getInstance().getLoginId();
                String strOperation = "Card Refresh";
                String strStatus = "F";
                String strReason = "";
                String strOldCardId = Utility.paddCardId(cardInfo.getCardId());
                if (newCardId.trim().length() > 0 && !strOldCardId.equals(newCardId)) {
                    strReason = "Card Refresh With Card Id Change";
                    String enrollNo = cardInfo.getEnrollmentNo();
                    if (enrollNo != null && enrollNo.trim().length() > 0) {
                        int id = Integer.parseInt(enrollNo);
                        int cardVersion = dbComm.getSmartCardIssuedVer(id);
                        int newCardVersion = cardVersion + 1;
                        int insertStatus = -1;

                        //=============================================Insert Into Smart Card Opeartion Log=========================================================//

                        insertStatus = dbComm.insertIntoSmartCardOperationLog(loginId, id, cardInfo.getReadCSN(), strOldCardId, cardVersion, newCardId, newCardVersion, strOperation, strStatus, "Y", strDateTime);
                        if (insertStatus != -1) {
                            Log.d("TEST", "Smart Card Operation Log Inserted Successfully");
                        } else {
                            Log.d("TEST", "Smart Card Operation Log Insertion Failure");
                        }

                        //=============================================Insert Into Hot List Log=========================================================//

                        insertStatus = dbComm.insertIntoHotListLog(loginId, id, cardInfo.getReadCSN(), strOldCardId, cardVersion, strOperation, strStatus, "Y", strReason, strDateTime);
                        if (insertStatus != -1) {
                            Log.d("TEST", "Hot List Log Inserted Successfully");
                        } else {
                            Log.d("TEST", "Hot List Log Insertion Failure");
                        }

                        //=============================================Update Employee CSN=========================================================//

                        int isDataUpdated = -1;
                        isDataUpdated = dbComm.updateSmartCardVer(id, cardInfo.getReadCSN(), Integer.toString(newCardVersion));
                        if (isDataUpdated != -1) {
                            Log.d("TEST", "Employee CSN Updated Successfully");
                        } else {
                            Log.d("TEST", "Failed To Update Employee CSN");
                        }
                    }
                } else {
                    String enrollNo = cardInfo.getEnrollmentNo();
                    if (enrollNo != null && enrollNo.trim().length() > 0) {
                        int id = Integer.parseInt(enrollNo);
                        int cardVersion = dbComm.getSmartCardIssuedVer(id);

                        //=============================================Insert Into Smart Card Opeartion Log=========================================================//

                        int insertStatus = -1;
                        insertStatus = dbComm.insertIntoSmartCardOperationLog(loginId, id, cardInfo.getReadCSN(), strOldCardId, cardVersion, "", cardVersion, strOperation, strStatus, "Y", strDateTime);
                        if (insertStatus != -1) {
                            Log.d("TEST", "Smart Card Operation Log Inserted Successfully");
                        } else {
                            Log.d("TEST", "Smart Card Operation Log Insertion Failure");
                        }
                    }
                }
                showCustomAlertDialog(false, "Card Write Status", "Failed To Refresh Card");
            }
        }
    }

    //=============================== Async Task SPI Card Refresh  =====================================//

    private class AsyncTaskSpiCardRefresh extends AsyncTask <String, Void, Integer> {


        ProgressDialog mypDialog;
        String strLog;
        int dataWriteStatus = -1;
        int keyWriteStatus = -1;

        int empAutoId;
        String strCardSerialNo;
        String strOldCardId;
        String strNewCardId;
        String isCardCreatedLocally;

        AsyncTaskSpiCardRefresh(int empAutoId, String strCardSerialNo, String strOldCardId, String strNewCardId, String isCardCreatedLocally) {

            this.empAutoId = empAutoId;
            this.strCardSerialNo = strCardSerialNo;
            this.strOldCardId = strOldCardId;
            this.strNewCardId = strNewCardId;
            this.isCardCreatedLocally = isCardCreatedLocally;

        }

        @Override
        protected void onPreExecute() {
            mypDialog = new ProgressDialog(SmartCardActivity.this);
            mypDialog.setMessage("Card Refreshing Wait...");
            mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mypDialog.setCanceledOnTouchOutside(false);
            mypDialog.show();
        }


        @Override
        protected Integer doInBackground(String... params) {


            int error = -1;

            Cursor sectorKeyData = dbComm.getSectorAndKeyForRC632CardRefresh();
            if (sectorKeyData != null) {

                String strDefaultKeyB = "543210";

                byte[] keya = new byte[6];
                byte[] keyb = new byte[6];
                byte[] keybdflt = new byte[6];

                byte[] cardId = null;

                while (sectorKeyData.moveToNext()) {

                    String strSectorNo = sectorKeyData.getString(0);
                    String strInitialKeyA = sectorKeyData.getString(1);
                    String strInitialKeyB = sectorKeyData.getString(2);

                    int sectorNo = Integer.parseInt(strSectorNo);

                    switch (sectorNo) {


                        case 0:

                            keya = strInitialKeyA.getBytes();
                            keyb = strInitialKeyB.getBytes();
                            keybdflt = strDefaultKeyB.getBytes();

                            error = rc632ReaderConnection.sectorRefresh((byte) 0, keya, keyb, keybdflt);
                            Log.d("TEST", "Card 0 refresh error no:" + error);

                            break;

                        case 1:

                            if (strNewCardId != null && strNewCardId.trim().length() > 0) {

                                keya = strInitialKeyA.getBytes();
                                keyb = strInitialKeyB.getBytes();
                                keybdflt = strDefaultKeyB.getBytes();
                                strNewCardId = Utility.paddCardId(strNewCardId);
                                cardId = strNewCardId.getBytes();
                                error = rc632ReaderConnection.sector_1_Refresh((byte) 1, keya, keyb, cardId);
                                Log.d("TEST", "Card 1 refresh error no:" + error);

                            }

                            break;

                        case 2:

                            keya = strInitialKeyA.getBytes();
                            keyb = strInitialKeyB.getBytes();
                            keybdflt = strDefaultKeyB.getBytes();

                            error = rc632ReaderConnection.sectorRefresh((byte) 2, keya, keyb, keybdflt);
                            Log.d("TEST", "Card 2 refresh error no:" + error);

                            break;

                        case 3:

                            break;

                        case 4:

                            keya = strInitialKeyA.getBytes();
                            keyb = strInitialKeyB.getBytes();
                            keybdflt = strDefaultKeyB.getBytes();

                            error = rc632ReaderConnection.sectorRefresh((byte) 4, keya, keyb, keybdflt);
                            Log.d("TEST", "Card 4 refresh error no:" + error);

                            break;

                        case 5:

                            keya = strInitialKeyA.getBytes();
                            keyb = strInitialKeyB.getBytes();
                            keybdflt = strDefaultKeyB.getBytes();

                            error = rc632ReaderConnection.sectorRefresh((byte) 5, keya, keyb, keybdflt);
                            Log.d("TEST", "Card 5 refresh error no:" + error);

                            break;

                        case 6:

                            keya = strInitialKeyA.getBytes();
                            keyb = strInitialKeyB.getBytes();
                            keybdflt = strDefaultKeyB.getBytes();

                            error = rc632ReaderConnection.sectorRefresh((byte) 6, keya, keyb, keybdflt);
                            Log.d("TEST", "Card 6 refresh error no:" + error);

                            break;


                        case 7:

                            keya = strInitialKeyA.getBytes();
                            keyb = strInitialKeyB.getBytes();
                            keybdflt = strDefaultKeyB.getBytes();

                            error = rc632ReaderConnection.sectorRefresh((byte) 7, keya, keyb, keybdflt);
                            Log.d("TEST", "Card 7 refresh error no:" + error);

                            break;

                        case 8:

                            keya = strInitialKeyA.getBytes();
                            keyb = strInitialKeyB.getBytes();
                            keybdflt = strDefaultKeyB.getBytes();

                            error = rc632ReaderConnection.sectorRefresh((byte) 8, keya, keyb, keybdflt);
                            Log.d("TEST", "Card 8 refresh error no:" + error);


                            break;

                        case 9:

                            keya = strInitialKeyA.getBytes();
                            keyb = strInitialKeyB.getBytes();
                            keybdflt = strDefaultKeyB.getBytes();

                            error = rc632ReaderConnection.sectorRefresh((byte) 9, keya, keyb, keybdflt);
                            Log.d("TEST", "Card 9 refresh error no:" + error);

                            break;

                        case 10:

                            keya = strInitialKeyA.getBytes();
                            keyb = strInitialKeyB.getBytes();
                            keybdflt = strDefaultKeyB.getBytes();

                            error = rc632ReaderConnection.sectorRefresh((byte) 10, keya, keyb, keybdflt);
                            Log.d("TEST", "Card 10 refresh error no:" + error);

                            break;

                        case 11:

                            keya = strInitialKeyA.getBytes();
                            keyb = strInitialKeyB.getBytes();
                            keybdflt = strDefaultKeyB.getBytes();

                            error = rc632ReaderConnection.sectorRefresh((byte) 11, keya, keyb, keybdflt);
                            Log.d("TEST", "Card 11 refresh error no:" + error);

                            break;

                        case 12:

                            keya = strInitialKeyA.getBytes();
                            keyb = strInitialKeyB.getBytes();
                            keybdflt = strDefaultKeyB.getBytes();

                            error = rc632ReaderConnection.sectorRefresh((byte) 12, keya, keyb, keybdflt);
                            Log.d("TEST", "Card 12 refresh error no:" + error);


                            break;

                        case 13:

                            keya = strInitialKeyA.getBytes();
                            keyb = strInitialKeyB.getBytes();
                            keybdflt = strDefaultKeyB.getBytes();

                            error = rc632ReaderConnection.sectorRefresh((byte) 13, keya, keyb, keybdflt);
                            Log.d("TEST", "Card 13 refresh error no:" + error);

                            break;

                        case 14:

                            keya = strInitialKeyA.getBytes();
                            keyb = strInitialKeyB.getBytes();
                            keybdflt = strDefaultKeyB.getBytes();

                            error = rc632ReaderConnection.sectorRefresh((byte) 14, keya, keyb, keybdflt);
                            Log.d("TEST", "Card 14 refresh error no:" + error);

                            break;

                        case 15:

                            keya = strInitialKeyA.getBytes();
                            keyb = strInitialKeyB.getBytes();
                            keybdflt = strDefaultKeyB.getBytes();

                            error = rc632ReaderConnection.sectorRefresh((byte) 15, keya, keyb, keybdflt);
                            Log.d("TEST", "Card 15 refresh error no:" + error);

                            break;

                        default:
                            break;
                    }

//                    if(dataWriteStatus!=-1 && keyWriteStatus!=-1){
//                        continue;
//                    }else{
//                        break;
//                    }
                }

                if (sectorKeyData != null) {
                    sectorKeyData.close();
                }
            }
            return error;
        }

        @Override
        protected void onPostExecute(Integer result) {
            mypDialog.cancel();
            if (result == 0) {
                showCustomAlertDialog(true, "Card Reresh Status", "Card Refreshed Successfully");
            } else {
                showCustomAlertDialog(true, "Card Write Status", "Failed To Refresh Card");
            }


//            Calendar calendar = Calendar.getInstance();
//            SimpleDateFormat mdformat = new SimpleDateFormat("ddMMyyyy HH:mm:ss");
//            String strDateTime = mdformat.format(calendar.getTime());
//
//            if (dataWriteStatus != -1 && keyWriteStatus != -1) {
//
//                int loginId = UserDetails.getInstance().getLoginId();
//                String strOperation = "Card Refresh";
//                String strReason = "";
//                String strStatus = "S";
//
//                strOldCardId = Utility.paddCardId(strOldCardId);
//
//                if (strNewCardId.trim().length() > 0 && !strOldCardId.equals(strNewCardId)) {
//
//                    strReason = "Card Id Change";
//
//                    int cardVersion = dbLayer.getSmartCardIssuedVer(empAutoId);
//                    int newCardVersion = cardVersion + 1;
//
//
//                    //=============================================Insert Into Smart Card Opeartion Log=========================================================//
//
//                    int insertStatus = -1;
//                    insertStatus = dbLayer.insertIntoSmartCardOperationLog(loginId, empAutoId, strCardSerialNo, strOldCardId, cardVersion, strNewCardId, newCardVersion, strOperation, strStatus, isCardCreatedLocally, strDateTime);
//
//                    if (insertStatus != -1) {
//                        Log.d("TEST", "Smart Card Operation Log Inserted Successfully");
//                    } else {
//                        Log.d("TEST", "Smart Card Operation Log Insertion Failure");
//                    }
//
//                    //=============================================Insert Into Hot List Log=========================================================//
//
//                    insertStatus = dbLayer.insertIntoHotListLog(loginId, empAutoId, strCardSerialNo, strOldCardId, cardVersion, strOperation, strStatus, isCardCreatedLocally, strReason, strDateTime);
//
//                    if (insertStatus != -1) {
//                        Log.d("TEST", "Hot List Log Inserted Successfully");
//                    } else {
//                        Log.d("TEST", "Hot List Log Insertion Failure");
//                    }
//
//                    if (isCardCreatedLocally.equals("Y")) {
//
//                        //=============================================Update Employee CSN=========================================================//
//
//                        int isDataUpdated = -1;
//                        isDataUpdated = dbLayer.updateEmpCSN(strCardSerialNo, strOldCardId);
//
//                        if (isDataUpdated != -1) {
//                            Log.d("TEST", "Employee CSN Updated Successfully");
//                        } else {
//                            Log.d("TEST", "Failed To Update Employee CSN");
//                        }
//                    }
//
//                } else {
//
//                    int cardVersion = dbLayer.getSmartCardIssuedVer(empAutoId);
//
//                    //=============================================Insert Into Smart Card Opeartion Log=========================================================//
//
//                    int insertStatus = -1;
//                    insertStatus = dbLayer.insertIntoSmartCardOperationLog(loginId, empAutoId, strCardSerialNo, strOldCardId, cardVersion, strNewCardId, 0, strOperation, strStatus, isCardCreatedLocally, strDateTime);
//                    if (insertStatus != -1) {
//                        Log.d("TEST", "Smart Card Operation Log Inserted Successfully");
//                    } else {
//                        Log.d("TEST", "Smart Card Operation Log Insertion Failure");
//                    }
//                }
//
//                Log.d("TEST", "Log Details:\n" + strLog);
//                showCustomAlertDialog(true, "Card Reresh Status", "Card Refreshed Successfully");
//
//            } else {
//
//                int loginId = UserDetails.getInstance().getLoginId();
//                String strOperation = "Card Refresh";
//                String strStatus = "F";
//
//                int cardVersion = dbLayer.getSmartCardIssuedVer(empAutoId);
//
//                //=============================================Insert Into Smart Card Opeartion Log=========================================================//
//
//                int insertStatus = -1;
//                insertStatus = dbLayer.insertIntoSmartCardOperationLog(loginId, empAutoId, strCardSerialNo, strOldCardId, cardVersion, strNewCardId, 0, strOperation, strStatus, isCardCreatedLocally, strDateTime);
//
//                if (insertStatus != -1) {
//                    Log.d("TEST", "Smart Card Operation Log Inserted Successfully");
//                } else {
//                    Log.d("TEST", "Smart Card Operation Log Insertion Failure");
//                }
//
//                Log.d("TEST", "Error Log Details:\n" + strLog);
//                showCustomAlertDialog(true, "Card Write Status", "Failed To Refresh Card");
//            }
//
            clear();
        }

    }


    private class AsyncTaskRC522CardRefresh extends AsyncTask <Void, Void, Boolean> {

        ProgressDialog mypDialog;
        RC522Communicator rc522Comm;
        SmartCardInfo cardInfo;
        String newCardId;

        AsyncTaskRC522CardRefresh(RC522Communicator rc522Comm, SmartCardInfo cardInfo, String newCardId) {
            this.rc522Comm = rc522Comm;
            this.cardInfo = cardInfo;
            this.newCardId = newCardId;
        }

        @Override
        protected void onPreExecute() {
            mypDialog = new ProgressDialog(SmartCardActivity.this);
            mypDialog.setMessage("Card Refreshing Wait...");
            mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mypDialog.setCanceledOnTouchOutside(false);
            mypDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            boolean opStatus = true;
            Cursor sectorKeyData = dbComm.getSectorAndKeyForWriteCard();
            if (sectorKeyData != null) {
                String strEmptyData = "00000000000000000000000000000000";
                String msg = "";
                String strFinalKeyB = "353433323130";
                String strAccessCode = "7F0788C1";
                boolean status = false;
                while (sectorKeyData.moveToNext()) {
                    String strSectorNo = sectorKeyData.getString(0);
                    String strFinalKeyA = sectorKeyData.getString(1);
                    String strInitialKeyB = sectorKeyData.getString(2);
                    int sectorNo = Integer.parseInt(strSectorNo);
                    switch (sectorNo) {
                        case 0:
                            opStatus = true;
                            break;
                        case 1:
                            opStatus = true;
                            break;
                        case 2:
                            opStatus = false;
                            msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + strEmptyData;
                            status = rc522Comm.writeRC522(msg);
                            if (status) {
                                sleep(Constants.DELEY);
                                char[] data = rc522Comm.readRC522();
                                String strData = new String(data);
                                Log.d("TEST", "Read After Write Sector 2 Block 8:" + strData);
                                String[] arr = strData.split(":");
                                if (arr != null && arr.length == 3) {
                                    strData = arr[2].trim();
                                    if (strData.equals("SUCCESS")) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + strEmptyData;
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            sleep(Constants.DELEY);
                                            data = rc522Comm.readRC522();
                                            strData = new String(data);
                                            Log.d("TEST", "Read After Write Sector 2 Block 9:" + strData);
                                            arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + strEmptyData;
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        sleep(Constants.DELEY);
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        Log.d("TEST", "Read After Write Sector 2 Block 10:" + strData);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_CHANGE_KEY_COMMAND + " 11 " + strInitialKeyB + " B " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    sleep(Constants.DELEY);
                                                                    char[] readData = rc522Comm.readRC522();
                                                                    if (readData != null && readData.length > 0) {
                                                                        String strReadData = new String(readData);
                                                                        Log.d("TEST", "Read After Key Change Sector 2:" + strReadData);
                                                                        strReadData = strReadData.trim();
                                                                        arr = strReadData.split(":");
                                                                        if (arr != null && arr.length == 3) {
                                                                            strReadData = arr[2].trim();
                                                                            Log.d("TEST", "Key Write 2:" + strReadData);
                                                                            if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                                                Log.d("TEST", "true");
                                                                                opStatus = true;
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
                                    }
                                }
                            }
                            break;
                        case 3:
                            opStatus = false;
                            msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + strEmptyData;
                            status = rc522Comm.writeRC522(msg);
                            if (status) {
                                sleep(Constants.DELEY);
                                char[] data = rc522Comm.readRC522();
                                String strData = new String(data);
                                Log.d("TEST", "Read After Write Sector 3 Block 12:" + strData);
                                String[] arr = strData.split(":");
                                if (arr != null && arr.length == 3) {
                                    strData = arr[2].trim();
                                    if (strData.equals("SUCCESS")) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + strEmptyData;
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            sleep(Constants.DELEY);
                                            data = rc522Comm.readRC522();
                                            strData = new String(data);
                                            Log.d("TEST", "Read After Write Sector 3 Block 13:" + strData);
                                            arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + strEmptyData;
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        sleep(Constants.DELEY);
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        Log.d("TEST", "Read After Write Sector 3 Block 14:" + strData);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_CHANGE_KEY_COMMAND + " 15 " + strInitialKeyB + " B " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    sleep(Constants.DELEY);
                                                                    char[] readData = rc522Comm.readRC522();
                                                                    if (readData != null && readData.length > 0) {
                                                                        String strReadData = new String(readData);
                                                                        Log.d("TEST", "Read After Key Change Sector 3:" + strReadData);
                                                                        strReadData = strReadData.trim();
                                                                        arr = strReadData.split(":");
                                                                        if (arr != null && arr.length == 3) {
                                                                            strReadData = arr[2].trim();
                                                                            Log.d("TEST", "Key Write 2:" + strReadData);
                                                                            if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                                                Log.d("TEST", "true");
                                                                                opStatus = true;
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
                                    }
                                }
                            }
                            break;
                        case 4:
                            opStatus = false;
                            msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + strEmptyData;
                            status = rc522Comm.writeRC522(msg);
                            if (status) {
                                sleep(Constants.DELEY);
                                char[] data = rc522Comm.readRC522();
                                String strData = new String(data);
                                Log.d("TEST", "Read After Write Sector 4 Block 16:" + strData);
                                String[] arr = strData.split(":");
                                if (arr != null && arr.length == 3) {
                                    strData = arr[2].trim();
                                    if (strData.equals("SUCCESS")) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + strEmptyData;
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            sleep(Constants.DELEY);
                                            data = rc522Comm.readRC522();
                                            strData = new String(data);
                                            Log.d("TEST", "Read After Write Sector 4 Block 17:" + strData);
                                            arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + strEmptyData;
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        sleep(Constants.DELEY);
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        Log.d("TEST", "Read After Write Sector 4 Block 18:" + strData);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_CHANGE_KEY_COMMAND + " 19 " + strInitialKeyB + " B " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    sleep(Constants.DELEY);
                                                                    char[] readData = rc522Comm.readRC522();
                                                                    if (readData != null && readData.length > 0) {
                                                                        String strReadData = new String(readData);
                                                                        Log.d("TEST", "Read After Key Change Sector 4:" + strReadData);
                                                                        strReadData = strReadData.trim();
                                                                        arr = strReadData.split(":");
                                                                        if (arr != null && arr.length == 3) {
                                                                            strReadData = arr[2].trim();
                                                                            Log.d("TEST", "Key Write 4:" + strReadData);
                                                                            if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                                                Log.d("TEST", "true");
                                                                                opStatus = true;
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
                                    }
                                }
                            }
                            break;
                        case 5:
                            opStatus = false;
                            msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + strEmptyData;
                            status = rc522Comm.writeRC522(msg);
                            if (status) {
                                sleep(Constants.DELEY);
                                char[] data = rc522Comm.readRC522();
                                String strData = new String(data);
                                Log.d("TEST", "Read After Write Sector 5 Block 20:" + strData);
                                String[] arr = strData.split(":");
                                if (arr != null && arr.length == 3) {
                                    strData = arr[2].trim();
                                    if (strData.equals("SUCCESS")) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + strEmptyData;
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            sleep(Constants.DELEY);
                                            data = rc522Comm.readRC522();
                                            strData = new String(data);
                                            Log.d("TEST", "Read After Write Sector 5 Block 21:" + strData);
                                            arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + strEmptyData;
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        sleep(Constants.DELEY);
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        Log.d("TEST", "Read After Write Sector 5 Block 22:" + strData);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_CHANGE_KEY_COMMAND + " 23 " + strInitialKeyB + " B " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    sleep(Constants.DELEY);
                                                                    char[] readData = rc522Comm.readRC522();
                                                                    if (readData != null && readData.length > 0) {
                                                                        String strReadData = new String(readData);
                                                                        strReadData = strReadData.trim();
                                                                        Log.d("TEST", "Read After Key Change Sector 5:" + strReadData);
                                                                        arr = strReadData.split(":");
                                                                        if (arr != null && arr.length == 3) {
                                                                            strReadData = arr[2].trim();
                                                                            Log.d("TEST", "Key Write 5:" + strReadData);
                                                                            if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                                                Log.d("TEST", "true");
                                                                                opStatus = true;
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
                                    }
                                }
                            }

                            break;
                        case 6:
                            opStatus = false;
                            msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + strEmptyData;
                            status = rc522Comm.writeRC522(msg);
                            if (status) {
                                sleep(Constants.DELEY);
                                char[] data = rc522Comm.readRC522();
                                String strData = new String(data);
                                Log.d("TEST", "Read After Write Sector 6 Block 24:" + strData);
                                String[] arr = strData.split(":");
                                if (arr != null && arr.length == 3) {
                                    strData = arr[2].trim();
                                    if (strData.equals("SUCCESS")) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + strEmptyData;
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            sleep(Constants.DELEY);
                                            data = rc522Comm.readRC522();
                                            strData = new String(data);
                                            Log.d("TEST", "Read After Write Sector 6 Block 25:" + strData);
                                            arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + strEmptyData;
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        sleep(Constants.DELEY);
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        Log.d("TEST", "Read After Write Sector 6 Block 26:" + strData);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_CHANGE_KEY_COMMAND + " 27 " + strInitialKeyB + " B " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    sleep(Constants.DELEY);
                                                                    char[] readData = rc522Comm.readRC522();
                                                                    if (readData != null && readData.length > 0) {
                                                                        String strReadData = new String(readData);
                                                                        strReadData = strReadData.trim();
                                                                        Log.d("TEST", "Read After Key Change Sector 6:" + strReadData);
                                                                        arr = strReadData.split(":");
                                                                        if (arr != null && arr.length == 3) {
                                                                            strReadData = arr[2].trim();
                                                                            Log.d("TEST", "Key Write 6:" + strReadData);
                                                                            if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                                                Log.d("TEST", "true");
                                                                                opStatus = true;
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
                                    }
                                }
                            }

                            break;
                        case 7:
                            opStatus = false;
                            msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + strEmptyData;
                            status = rc522Comm.writeRC522(msg);
                            if (status) {
                                sleep(Constants.DELEY);
                                char[] data = rc522Comm.readRC522();
                                String strData = new String(data);
                                Log.d("TEST", "Read After Write Sector 7 Block 28:" + strData);
                                String[] arr = strData.split(":");
                                if (arr != null && arr.length == 3) {
                                    strData = arr[2].trim();
                                    if (strData.equals("SUCCESS")) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + strEmptyData;
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            sleep(Constants.DELEY);
                                            data = rc522Comm.readRC522();
                                            strData = new String(data);
                                            Log.d("TEST", "Read After Write Sector 7 Block 29:" + strData);
                                            arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + strEmptyData;
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        sleep(Constants.DELEY);
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        Log.d("TEST", "Read After Write Sector 7 Block 30:" + strData);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_CHANGE_KEY_COMMAND + " 31 " + strInitialKeyB + " B " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    sleep(Constants.DELEY);
                                                                    char[] readData = rc522Comm.readRC522();
                                                                    if (readData != null && readData.length > 0) {
                                                                        String strReadData = new String(readData);
                                                                        strReadData = strReadData.trim();
                                                                        Log.d("TEST", "Read After Key Change Sector 7:" + strReadData);
                                                                        arr = strReadData.split(":");
                                                                        if (arr != null && arr.length == 3) {
                                                                            strReadData = arr[2].trim();
                                                                            Log.d("TEST", "Key Write 7:" + strReadData);
                                                                            if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                                                Log.d("TEST", "true");
                                                                                opStatus = true;
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
                                    }
                                }
                            }

                            break;
                        case 8:
                            opStatus = false;
                            msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + strEmptyData;
                            status = rc522Comm.writeRC522(msg);
                            if (status) {
                                sleep(Constants.DELEY);
                                char[] data = rc522Comm.readRC522();
                                String strData = new String(data);
                                Log.d("TEST", "Read After Write Sector 8 Block 32:" + strData);
                                String[] arr = strData.split(":");
                                if (arr != null && arr.length == 3) {
                                    strData = arr[2].trim();
                                    if (strData.equals("SUCCESS")) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + strEmptyData;
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            sleep(Constants.DELEY);
                                            data = rc522Comm.readRC522();
                                            strData = new String(data);
                                            Log.d("TEST", "Read After Write Sector 8 Block 33:" + strData);
                                            arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + strEmptyData;
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        sleep(Constants.DELEY);
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        Log.d("TEST", "Read After Write Sector 8 Block 34:" + strData);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_CHANGE_KEY_COMMAND + " 35 " + strInitialKeyB + " B " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    sleep(Constants.DELEY);
                                                                    char[] readData = rc522Comm.readRC522();
                                                                    if (readData != null && readData.length > 0) {
                                                                        String strReadData = new String(readData);
                                                                        strReadData = strReadData.trim();
                                                                        Log.d("TEST", "Read After Key Change Sector 8:" + strReadData);
                                                                        arr = strReadData.split(":");
                                                                        if (arr != null && arr.length == 3) {
                                                                            strReadData = arr[2].trim();
                                                                            Log.d("TEST", "Key Write 8:" + strReadData);
                                                                            if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                                                Log.d("TEST", "true");
                                                                                opStatus = true;
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
                                    }
                                }
                            }

                            break;
                        case 9:
                            opStatus = false;
                            msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + strEmptyData;
                            status = rc522Comm.writeRC522(msg);
                            if (status) {
                                sleep(Constants.DELEY);
                                char[] data = rc522Comm.readRC522();
                                String strData = new String(data);
                                Log.d("TEST", "Read After Write Sector 9 Block 36:" + strData);
                                String[] arr = strData.split(":");
                                if (arr != null && arr.length == 3) {
                                    strData = arr[2].trim();
                                    if (strData.equals("SUCCESS")) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + strEmptyData;
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            sleep(Constants.DELEY);
                                            data = rc522Comm.readRC522();
                                            strData = new String(data);
                                            Log.d("TEST", "Read After Write Sector 9 Block 37:" + strData);
                                            arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + strEmptyData;
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        sleep(Constants.DELEY);
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        Log.d("TEST", "Read After Write Sector 9 Block 38:" + strData);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_CHANGE_KEY_COMMAND + " 39 " + strInitialKeyB + " B " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    sleep(Constants.DELEY);
                                                                    char[] readData = rc522Comm.readRC522();
                                                                    if (readData != null && readData.length > 0) {
                                                                        String strReadData = new String(readData);
                                                                        strReadData = strReadData.trim();
                                                                        Log.d("TEST", "Read After Key Change Sector 9:" + strReadData);
                                                                        arr = strReadData.split(":");
                                                                        if (arr != null && arr.length == 3) {
                                                                            strReadData = arr[2].trim();
                                                                            Log.d("TEST", "Key Write 9:" + strReadData);
                                                                            if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                                                Log.d("TEST", "true");
                                                                                opStatus = true;
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
                                    }
                                }
                            }

                            break;
                        case 10:
                            opStatus = false;
                            msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + strEmptyData;
                            status = rc522Comm.writeRC522(msg);
                            if (status) {
                                sleep(Constants.DELEY);
                                char[] data = rc522Comm.readRC522();
                                String strData = new String(data);
                                Log.d("TEST", "Read After Write Sector 10 Block 40:" + strData);
                                String[] arr = strData.split(":");
                                if (arr != null && arr.length == 3) {
                                    strData = arr[2].trim();
                                    if (strData.equals("SUCCESS")) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + strEmptyData;
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            sleep(Constants.DELEY);
                                            data = rc522Comm.readRC522();
                                            strData = new String(data);
                                            Log.d("TEST", "Read After Write Sector 10 Block 41:" + strData);
                                            arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + strEmptyData;
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        sleep(Constants.DELEY);
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        Log.d("TEST", "Read After Write Sector 10 Block 42:" + strData);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_CHANGE_KEY_COMMAND + " 43 " + strInitialKeyB + " B " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    sleep(Constants.DELEY);
                                                                    char[] readData = rc522Comm.readRC522();
                                                                    if (readData != null && readData.length > 0) {
                                                                        String strReadData = new String(readData);
                                                                        strReadData = strReadData.trim();
                                                                        Log.d("TEST", "Read After Key Change Sector 10:" + strReadData);
                                                                        arr = strReadData.split(":");
                                                                        if (arr != null && arr.length == 3) {
                                                                            strReadData = arr[2].trim();
                                                                            Log.d("TEST", "Key Write 10:" + strReadData);
                                                                            if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                                                Log.d("TEST", "true");
                                                                                opStatus = true;
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
                                    }
                                }
                            }

                            break;
                        case 11:
                            opStatus = false;
                            msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + strEmptyData;
                            status = rc522Comm.writeRC522(msg);
                            if (status) {
                                sleep(Constants.DELEY);
                                char[] data = rc522Comm.readRC522();
                                String strData = new String(data);
                                Log.d("TEST", "Read After Write Sector 11 Block 44:" + strData);
                                String[] arr = strData.split(":");
                                if (arr != null && arr.length == 3) {
                                    strData = arr[2].trim();
                                    if (strData.equals("SUCCESS")) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + strEmptyData;
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            sleep(Constants.DELEY);
                                            data = rc522Comm.readRC522();
                                            strData = new String(data);
                                            Log.d("TEST", "Read After Write Sector 11 Block 45:" + strData);
                                            arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + strEmptyData;
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        sleep(Constants.DELEY);
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        Log.d("TEST", "Read After Write Sector 11 Block 46:" + strData);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_CHANGE_KEY_COMMAND + " 47 " + strInitialKeyB + " B " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    sleep(Constants.DELEY);
                                                                    char[] readData = rc522Comm.readRC522();
                                                                    if (readData != null && readData.length > 0) {
                                                                        String strReadData = new String(readData);
                                                                        strReadData = strReadData.trim();
                                                                        Log.d("TEST", "Read After Key Change Sector 11:" + strReadData);
                                                                        arr = strReadData.split(":");
                                                                        if (arr != null && arr.length == 3) {
                                                                            strReadData = arr[2].trim();
                                                                            Log.d("TEST", "Key Write 11:" + strReadData);
                                                                            if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                                                Log.d("TEST", "true");
                                                                                opStatus = true;
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
                                    }
                                }
                            }

                            break;
                        case 12:
                            opStatus = false;
                            msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + strEmptyData;
                            status = rc522Comm.writeRC522(msg);
                            if (status) {
                                sleep(Constants.DELEY);
                                char[] data = rc522Comm.readRC522();
                                String strData = new String(data);
                                Log.d("TEST", "Read After Write Sector 12 Block 48:" + strData);
                                String[] arr = strData.split(":");
                                if (arr != null && arr.length == 3) {
                                    strData = arr[2].trim();
                                    if (strData.equals("SUCCESS")) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + strEmptyData;
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            sleep(Constants.DELEY);
                                            data = rc522Comm.readRC522();
                                            strData = new String(data);
                                            Log.d("TEST", "Read After Write Sector 12 Block 49:" + strData);
                                            arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + strEmptyData;
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        sleep(Constants.DELEY);
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        Log.d("TEST", "Read After Write Sector 12 Block 50:" + strData);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_CHANGE_KEY_COMMAND + " 51 " + strInitialKeyB + " B " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    sleep(Constants.DELEY);
                                                                    char[] readData = rc522Comm.readRC522();
                                                                    if (readData != null && readData.length > 0) {
                                                                        String strReadData = new String(readData);
                                                                        strReadData = strReadData.trim();
                                                                        Log.d("TEST", "Read After Key Change Sector 12:" + strReadData);
                                                                        arr = strReadData.split(":");
                                                                        if (arr != null && arr.length == 3) {
                                                                            strReadData = arr[2].trim();
                                                                            Log.d("TEST", "Key Write 12:" + strReadData);
                                                                            if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                                                Log.d("TEST", "true");
                                                                                opStatus = true;
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
                                    }
                                }
                            }

                            break;
                        case 13:
                            opStatus = false;
                            msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + strEmptyData;
                            status = rc522Comm.writeRC522(msg);
                            if (status) {
                                sleep(Constants.DELEY);
                                char[] data = rc522Comm.readRC522();
                                String strData = new String(data);
                                Log.d("TEST", "Read After Write Sector 13 Block 52:" + strData);
                                String[] arr = strData.split(":");
                                if (arr != null && arr.length == 3) {
                                    strData = arr[2].trim();
                                    if (strData.equals("SUCCESS")) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + strEmptyData;
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            sleep(Constants.DELEY);
                                            data = rc522Comm.readRC522();
                                            strData = new String(data);
                                            Log.d("TEST", "Read After Write Sector 13 Block 53:" + strData);
                                            arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + strEmptyData;
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        sleep(Constants.DELEY);
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        Log.d("TEST", "Read After Write Sector 13 Block 54:" + strData);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_CHANGE_KEY_COMMAND + " 55 " + strInitialKeyB + " B " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    sleep(Constants.DELEY);
                                                                    char[] readData = rc522Comm.readRC522();
                                                                    if (readData != null && readData.length > 0) {
                                                                        String strReadData = new String(readData);
                                                                        strReadData = strReadData.trim();
                                                                        Log.d("TEST", "Read After Key Change Sector 13:" + strReadData);
                                                                        arr = strReadData.split(":");
                                                                        if (arr != null && arr.length == 3) {
                                                                            strReadData = arr[2].trim();
                                                                            Log.d("TEST", "Key Write 13:" + strReadData);
                                                                            if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                                                Log.d("TEST", "true");
                                                                                opStatus = true;
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
                                    }
                                }
                            }
                            break;
                        case 14:
                            opStatus = false;
                            msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + strEmptyData;
                            status = rc522Comm.writeRC522(msg);
                            if (status) {
                                sleep(Constants.DELEY);
                                char[] data = rc522Comm.readRC522();
                                String strData = new String(data);
                                Log.d("TEST", "Read After Write Sector 14 Block 56:" + strData);
                                String[] arr = strData.split(":");
                                if (arr != null && arr.length == 3) {
                                    strData = arr[2].trim();
                                    if (strData.equals("SUCCESS")) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + strEmptyData;
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            sleep(Constants.DELEY);
                                            data = rc522Comm.readRC522();
                                            strData = new String(data);
                                            Log.d("TEST", "Read After Write Sector 14 Block 57:" + strData);
                                            arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + strEmptyData;
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        sleep(Constants.DELEY);
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        Log.d("TEST", "Read After Write Sector 14 Block 57:" + strData);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_CHANGE_KEY_COMMAND + " 59 " + strInitialKeyB + " B " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    sleep(Constants.DELEY);
                                                                    char[] readData = rc522Comm.readRC522();
                                                                    if (readData != null && readData.length > 0) {
                                                                        String strReadData = new String(readData);
                                                                        strReadData = strReadData.trim();
                                                                        Log.d("TEST", "Read After Key Change Sector 14:" + strReadData);
                                                                        arr = strReadData.split(":");
                                                                        if (arr != null && arr.length == 3) {
                                                                            strReadData = arr[2].trim();
                                                                            Log.d("TEST", "Key Write 14:" + strReadData);
                                                                            if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                                                Log.d("TEST", "true");
                                                                                opStatus = true;
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
                                    }
                                }
                            }
                            break;
                        case 15:
                            opStatus = false;
                            msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 0) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + strEmptyData;
                            status = rc522Comm.writeRC522(msg);
                            if (status) {
                                sleep(Constants.DELEY);
                                char[] data = rc522Comm.readRC522();
                                String strData = new String(data);
                                Log.d("TEST", "Read After Write Sector 15 Block 60:" + strData);
                                String[] arr = strData.split(":");
                                if (arr != null && arr.length == 3) {
                                    strData = arr[2].trim();
                                    if (strData.equals("SUCCESS")) {
                                        msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 1) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + strEmptyData;
                                        status = rc522Comm.writeRC522(msg);
                                        if (status) {
                                            sleep(Constants.DELEY);
                                            data = rc522Comm.readRC522();
                                            strData = new String(data);
                                            Log.d("TEST", "Read After Write Sector 15 Block 61:" + strData);
                                            arr = strData.split(":");
                                            if (arr != null && arr.length == 3) {
                                                strData = arr[2].trim();
                                                if (strData.equals("SUCCESS")) {
                                                    msg = Constants.RC522_WRITE_COMMAND + " " + Integer.toString(sectorNo * 4 + 2) + " " + strInitialKeyB + " " + Constants.RC522_KEY_TYPE_B + " " + strEmptyData;
                                                    status = rc522Comm.writeRC522(msg);
                                                    if (status) {
                                                        sleep(Constants.DELEY);
                                                        data = rc522Comm.readRC522();
                                                        strData = new String(data);
                                                        Log.d("TEST", "Read After Write Sector 15 Block 62:" + strData);
                                                        arr = strData.split(":");
                                                        if (arr != null && arr.length == 3) {
                                                            strData = arr[2].trim();
                                                            if (strData.equals("SUCCESS")) {
                                                                msg = Constants.RC522_CHANGE_KEY_COMMAND + " 63 " + strInitialKeyB + " B " + strFinalKeyA + strAccessCode + strFinalKeyB;
                                                                status = rc522Comm.writeRC522(msg);
                                                                if (status) {
                                                                    sleep(Constants.DELEY);
                                                                    char[] readData = rc522Comm.readRC522();
                                                                    if (readData != null && readData.length > 0) {
                                                                        String strReadData = new String(readData);
                                                                        strReadData = strReadData.trim();
                                                                        Log.d("TEST", "Read After Key Change Sector 15:" + strReadData);
                                                                        arr = strReadData.split(":");
                                                                        if (arr != null && arr.length == 3) {
                                                                            strReadData = arr[2].trim();
                                                                            Log.d("TEST", "Key Write 15:" + strReadData);
                                                                            if (strReadData.equals("KEY_wr_SUCCESS")) {
                                                                                Log.d("TEST", "true");
                                                                                opStatus = true;
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
                                    }
                                }
                            }
                            break;
                    }
                    if (!opStatus) {
                        break;
                    }
                }

                sectorKeyData.close();

                if (opStatus) {
                    //============================== MAD UPDATE =================================//

                    String strData_01 = "55010005000000000000000000000000";
                    String strData_02 = "00000000000000000000000000000000";

                    //========================== UPDATE BLOCK 1 MAD ZERO ========================//

                    opStatus = false;
                    String writeData = Constants.RC522_WRITE_COMMAND + " 1 " + "524553534543" + " B " + strData_01;
                    status = rc522Comm.writeRC522(writeData);
                    if (status) {
                        char[] readData = rc522Comm.readRC522();
                        if (readData != null && readData.length > 0) {
                            String strReadData = new String(readData);
                            strReadData = strReadData.trim();
                            String arr[] = strReadData.split(":");
                            if (arr != null && arr.length == 3) {
                                strReadData = arr[2].trim();
                                Log.d("TEST", "Read:" + strReadData);
                                if (strReadData.equals("SUCCESS")) {
                                    opStatus = true;
                                }
                            }
                        }
                    }

                    //========================== UPDATE BLOCK 2 MAD ZERO ============================//

                    opStatus = false;
                    writeData = Constants.RC522_WRITE_COMMAND + " 2 " + "524553534543" + " B " + strData_02;
                    status = rc522Comm.writeRC522(writeData);
                    if (status) {
                        char[] readData = rc522Comm.readRC522();
                        if (readData != null && readData.length > 0) {
                            String strReadData = new String(readData);
                            strReadData = strReadData.trim();
                            String arr[] = strReadData.split(":");
                            if (arr != null && arr.length == 3) {
                                strReadData = arr[2].trim();
                                Log.d("TEST", "Read:" + strReadData);
                                if (strReadData.equals("SUCCESS")) {
                                    opStatus = true;
                                }
                            }
                        }
                    }

                    //==============================================================================================//


                    //======================== UPDATE SECTOR 1 AND WRITE CARD ID =======================//

                    if (newCardId != null && newCardId.trim().length() > 0) {
                        newCardId = Utility.paddCardId(newCardId);
                        String strHexCardId = Utility.asciiToHex(newCardId);
                        String strData_10 = strHexCardId + "464F5254554E4120";
                        String strData_11 = "11303030AAFFFFFFFFFFFFFFFFFFFFFF";
                        String strData_12 = "00000000000000000000000000000000";

                        //============================== UPDATE BLOCK 4 ==================================//

                        opStatus = false;
                        writeData = Constants.RC522_WRITE_COMMAND + " 4 " + "435053204944" + " B " + strData_10;
                        status = rc522Comm.writeRC522(writeData);
                        if (status) {
                            char[] readData = rc522Comm.readRC522();
                            if (readData != null && readData.length > 0) {
                                String strReadData = new String(readData);
                                strReadData = strReadData.trim();
                                String arr[] = strReadData.split(":");
                                if (arr != null && arr.length == 3) {
                                    strReadData = arr[2].trim();
                                    Log.d("TEST", "Read:" + strReadData);
                                    if (strReadData.equals("SUCCESS")) {
                                        opStatus = true;
                                    }
                                }
                            }
                        }

                        //========================== UPDATE BLOCK 5 ==================================//

                        opStatus = false;
                        writeData = Constants.RC522_WRITE_COMMAND + " 5 " + "435053204944" + " B " + strData_11;
                        status = rc522Comm.writeRC522(writeData);
                        if (status) {
                            char[] readData = rc522Comm.readRC522();
                            if (readData != null && readData.length > 0) {
                                String strReadData = new String(readData);
                                strReadData = strReadData.trim();
                                String arr[] = strReadData.split(":");
                                if (arr != null && arr.length == 3) {
                                    strReadData = arr[2].trim();
                                    Log.d("TEST", "Read:" + strReadData);
                                    if (strReadData.equals("SUCCESS")) {
                                        opStatus = true;
                                    }
                                }
                            }
                        }


                        //========================== UPDATE BLOCK 6 ==================================//

                        opStatus = false;
                        writeData = Constants.RC522_WRITE_COMMAND + " 6 " + "435053204944" + " B " + strData_12;
                        status = rc522Comm.writeRC522(writeData);
                        if (status) {
                            char[] readData = rc522Comm.readRC522();
                            if (readData != null && readData.length > 0) {
                                String strReadData = new String(readData);
                                strReadData = strReadData.trim();
                                String arr[] = strReadData.split(":");
                                if (arr != null && arr.length == 3) {
                                    strReadData = arr[2].trim();
                                    Log.d("TEST", "Read:" + strReadData);
                                    if (strReadData.equals("SUCCESS")) {
                                        opStatus = true;
                                    }
                                }
                            }
                        }

                        //=================================================================================//
                    }

                }
            }
            return opStatus;
        }

        @Override
        protected void onPostExecute(Boolean status) {
            mypDialog.cancel();
            btn_CardRefresh.setEnabled(true);
            if (status) {
                showCustomAlertDialog(true, "Card Reresh Status", "Card Refreshed Successfully");
            } else {
                showCustomAlertDialog(true, "Card Reresh Status", "Failed To Refresh Card");
            }
        }
    }


    //================================= Custom Dialog For Card Read ======================================//

    public void showDialogForCardRead(String dialogTitle, String dialogMessage,
                                      final Object readerConn, final int readerType) {

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

        title.setText(dialogTitle);
        message.setText(dialogMessage);

        btn_No.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    btn_CardRead.setEnabled(true);
                    dialog.dismiss();
                } catch (Exception e) {
                    Toast.makeText(getBaseContext(), "Card Read Dialog dismiss error" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_Yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                switch (readerType) {
                    case 1:
                        MicroSmartV2Communicator mSmartV2Comm = (MicroSmartV2Communicator) readerConn;
                        try {
                            AsyncTaskUsbCardRead cardRead = new AsyncTaskUsbCardRead(mSmartV2Comm);
                            cardRead.execute();
                        } catch (Exception e) {
                            Toast.makeText(getBaseContext(), "Card read error : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 2:
                        try {
                            AsyncTaskSpiCardRead spiCardRead = new AsyncTaskSpiCardRead();
                            spiCardRead.execute();
                        } catch (Exception e) {
                            Toast.makeText(getBaseContext(), "Card read error in yes click:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 3:
                        try {
                            RC522Communicator comm = (RC522Communicator) readerConn;
                            new AsyncTaskRC522CardRead(comm).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        } catch (Exception e) {
                            Toast.makeText(getBaseContext(), "Card read error in yes click:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    default:
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

    //================================= Custom Dialog For Card Write ======================================//

    public void showDialogForCardWrite(String dialogTitle, String dialogMessage,
                                       final int readerType, final boolean defaultKeyWrite,
                                       final boolean isDbRead
            , final Object comm, final SmartCardInfo cardInfo) {

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

        title.setText(dialogTitle);
        message.setText(dialogMessage);

        btn_No.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btn_CardWrite.setEnabled(true);
                dialog.dismiss();
            }
        });

        btn_Yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                switch (readerType) {
                    case 1: // Micro Smart V2
                        MicroSmartV2Communicator mSmartV2Comm = (MicroSmartV2Communicator) comm;
                        try {
                            if (defaultKeyWrite) {
                                AsyncTaskUsbCardWriteAndKeyUpdate cardWriteAndUpdate = null;
                                int noOfTemplates = cardInfo.getNoOfTemplates();
                                switch (noOfTemplates) {
                                    case 1:
                                        cardWriteAndUpdate = new AsyncTaskUsbCardWriteAndKeyUpdate(mSmartV2Comm, cardInfo);
                                        cardWriteAndUpdate.execute();
                                        break;
                                    case 2:
                                        cardWriteAndUpdate = new AsyncTaskUsbCardWriteAndKeyUpdate(mSmartV2Comm, cardInfo);
                                        cardWriteAndUpdate.execute();
                                        break;
                                    default:
                                        showCustomAlertDialog(false, "Data Status", "Finger Template Not Equal To 512");
                                        break;
                                }
                            } else {
                                AsyncTaskUsbCardWrite cardWrite = null;
                                int noOfTemplates = cardInfo.getNoOfTemplates();
                                switch (noOfTemplates) {
                                    case 1:
                                        cardWrite = new AsyncTaskUsbCardWrite(mSmartV2Comm, cardInfo);
                                        cardWrite.execute();
                                        break;
                                    case 2:
                                        cardWrite = new AsyncTaskUsbCardWrite(mSmartV2Comm, cardInfo);
                                        cardWrite.execute();
                                        break;
                                    default:
                                        showCustomAlertDialog(false, "Data Status", "Finger Template Not Equal To 512");
                                        break;
                                }
                            }

                        } catch (Exception e) {
                            Toast.makeText(getBaseContext(), "Card write error in yes:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 2://RC632
                        try {
                            if (defaultKeyWrite) {
//                                if (strFirstFingerTemplate != null && strFirstFingerTemplate.trim().length() == 512 && strSecondFingerTemplate != null && strSecondFingerTemplate.trim().length() == 512) {
//                                    AsyncTaskSpiCardWriteAndKeyUpdate cardWrite = new AsyncTaskSpiCardWriteAndKeyUpdate();
//                                    cardWrite.execute();
//                                } else if (strFirstFingerTemplate != null && strFirstFingerTemplate.trim().length() == 512) {
//
//                                }
                            } else {
//                                if (strFirstFingerTemplate != null && strFirstFingerTemplate.trim().length() == 512 && strSecondFingerTemplate != null && strSecondFingerTemplate.trim().length() == 512) {
//                                    AsyncTaskSpiCardWrite cardWrite = new AsyncTaskSpiCardWrite();
//                                    cardWrite.execute();
//                                } else if (strFirstFingerTemplate != null && strFirstFingerTemplate.trim().length() == 512) {
//
//                                }
                            }
                        } catch (Exception e) {
                            Toast.makeText(getBaseContext(), "Card write error in yes:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 3://RC522
                        if (defaultKeyWrite) {
                            RC522Communicator rcComm = (RC522Communicator) comm;
                            new AsyncTaskRC522CardWriteAndKeyUpdate(rcComm, cardInfo).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        } else {
                            RC522Communicator rcComm = (RC522Communicator) comm;
                            new AsyncTaskRC522CardWrite(rcComm, cardInfo).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }
                        break;
                    default:
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


    //================================= Custom Dialog For Card Initialize ======================================//

    public void showDialogForCardInit(String dialogTitle, String dialogMessage,
                                      final int readerType, final Object comm) {

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

        icon.setImageResource(R.drawable.success);
        title.setText(dialogTitle);
        message.setText(dialogMessage);

        btn_No.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                switch (readerType) {
                    case 1: // Card Init For Usb Card Reader With Default Card Id 00000001
                        try {
                            MicroSmartV2Communicator mSmartV2Comm = (MicroSmartV2Communicator) comm;
                            AsynTaskUsbCardInitialize cardInitialize = new AsynTaskUsbCardInitialize(mSmartV2Comm, "00000001");
                            cardInitialize.execute();
                            clear();
                        } catch (Exception e) {
                            Toast.makeText(getBaseContext(), "Error in card init no click showDialogForInitAndRefresh::" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 2: // Card Initialize With Spi Card Reader With Default Card Id 00000001
                        try {
                            AsynTaskSpiCardInitialize cardInitialize = new AsynTaskSpiCardInitialize();
                            cardInitialize.execute("00000001");
                            clear();
                        } catch (Exception e) {
                            Toast.makeText(getBaseContext(), "Error in card init no click showDialogForInitAndRefresh::" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 3:// Card Initialize With RC522 Card Reader With Default Card Id 00000001
                        try {
                            RC522Communicator rc522Comm = (RC522Communicator) comm;
                            new AsyncTaskRC522CardInit(rc522Comm, "00000001").executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            clear();
                        } catch (Exception e) {
                            Toast.makeText(getBaseContext(), "Error in card init no click showDialogForInitAndRefresh::" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    default:
                        break;
                }
            }
        });


        btn_Yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                switch (readerType) {
                    case 1://Card Init For Usb Card Reader With New Card Id
                        try {
                            final Context contextci = SmartCardActivity.this;
                            final Dialog dialogci = new Dialog(contextci);
                            dialogci.setCanceledOnTouchOutside(true);
                            dialogci.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            dialogci.setContentView(R.layout.cardid_dialog);
                            ImageView iconci = (ImageView) dialogci.findViewById(R.id.image);
                            TextView titleci = (TextView) dialogci.findViewById(R.id.title);
                            final EditText editTextCardIdci = (EditText) dialogci.findViewById(R.id.cardId);
                            Button btn_Ok_ci = (Button) dialogci.findViewById(R.id.btnOk);
                            iconci.setImageResource(R.drawable.success);
                            titleci.setText("Card ID");

                            btn_Ok_ci.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    try {
                                        MicroSmartV2Communicator mSmartV2Comm = (MicroSmartV2Communicator) comm;
                                        dialogci.dismiss();
                                        String strNewCardId = editTextCardIdci.getText().toString().trim();
                                        if (strNewCardId != null && strNewCardId.trim().length() > 0) {
                                            int autoId = -1;
                                            autoId = dbComm.isNewCardIdExists(strNewCardId);
                                            if (autoId == -1) {
                                                AsynTaskUsbCardInitialize cardInitialize = new AsynTaskUsbCardInitialize(mSmartV2Comm, strNewCardId);
                                                cardInitialize.execute();
                                            } else {
                                                showCustomAlertDialog(false, "Error", "Card Id already exists");
                                            }
                                        } else {
                                            showCustomAlertDialog(false, "Error", "Card Id cannot be left blank");
                                        }
                                    } catch (Exception e) {
                                        Toast.makeText(getBaseContext(), "Card init error in yes:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }

                            });

                            dialogci.show();

                            WindowManager.LayoutParams lpci = dialogci.getWindow().getAttributes();
                            lpci.dimAmount = 0.9f;
                            lpci.buttonBrightness = 1.0f;
                            lpci.screenBrightness = 1.0f;
                            dialogci.getWindow().setAttributes(lpci);
                            dialogci.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

                            clear();

                        } catch (Exception e) {
                            Toast.makeText(getBaseContext(), "Card init error in yes:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                        break;

                    case 2://Card Initialize For Spi Card Reader With New Card Id
                        try {
                            final Context contextci = SmartCardActivity.this;
                            final Dialog dialogci = new Dialog(contextci);
                            dialogci.setCanceledOnTouchOutside(true);
                            dialogci.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            dialogci.setContentView(R.layout.cardid_dialog);
                            ImageView iconci = (ImageView) dialogci.findViewById(R.id.image);
                            TextView titleci = (TextView) dialogci.findViewById(R.id.title);
                            final EditText editTextCardIdci = (EditText) dialogci.findViewById(R.id.cardId);
                            Button btn_Ok_ci = (Button) dialogci.findViewById(R.id.btnOk);
                            iconci.setImageResource(R.drawable.success);
                            titleci.setText("Card ID");

                            btn_Ok_ci.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    try {
                                        dialogci.dismiss();
                                        String strCardId = editTextCardIdci.getText().toString().trim();
                                        String strNewCardId = Utility.paddCardId(strCardId);
                                        if (strNewCardId != null && strNewCardId.trim().length() > 0) {
                                            int autoId = -1;
                                            autoId = dbComm.isNewCardIdExists(strNewCardId);
                                            if (autoId == -1) {
                                                AsynTaskSpiCardInitialize cardInitialize = new AsynTaskSpiCardInitialize();
                                                cardInitialize.execute(strCardId);
                                            } else {
                                                showCustomAlertDialog(false, "Error", "Card Id already exists");
                                            }
                                        } else {
                                            showCustomAlertDialog(false, "Error", "Card Id cannot be left blank");
                                        }
                                    } catch (Exception e) {
                                        Toast.makeText(getBaseContext(), "Card init error in yes:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }

                            });
                            dialogci.show();

                            WindowManager.LayoutParams lpci = dialogci.getWindow().getAttributes();
                            lpci.dimAmount = 0.9f;
                            lpci.buttonBrightness = 1.0f;
                            lpci.screenBrightness = 1.0f;
                            dialogci.getWindow().setAttributes(lpci);
                            dialogci.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

                            clear();

                        } catch (Exception e) {
                            Toast.makeText(getBaseContext(), "Card init error in yes:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        break;


                    case 3:// Card Initialize For RC522 With New Card Id
                        try {
                            final Context contextci = SmartCardActivity.this;
                            final Dialog dialogci = new Dialog(contextci);
                            dialogci.setCanceledOnTouchOutside(true);
                            dialogci.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            dialogci.setContentView(R.layout.cardid_dialog);
                            ImageView iconci = (ImageView) dialogci.findViewById(R.id.image);
                            TextView titleci = (TextView) dialogci.findViewById(R.id.title);
                            final EditText editTextCardIdci = (EditText) dialogci.findViewById(R.id.cardId);
                            Button btn_Ok_ci = (Button) dialogci.findViewById(R.id.btnOk);
                            iconci.setImageResource(R.drawable.success);
                            titleci.setText("Card ID");

                            btn_Ok_ci.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    try {
                                        RC522Communicator rc522Comm = (RC522Communicator) comm;
                                        dialogci.dismiss();
                                        String strCardId = editTextCardIdci.getText().toString().trim();
                                        String strNewCardId = Utility.paddCardId(strCardId);
                                        if (strNewCardId != null && strNewCardId.trim().length() > 0) {
                                            int autoId = -1;
                                            autoId = dbComm.isNewCardIdExists(strNewCardId);
                                            if (autoId == -1) {
                                                new AsyncTaskRC522CardInit(rc522Comm, strNewCardId).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                            } else {
                                                btn_CardInit.setEnabled(true);
                                                showCustomAlertDialog(false, "Error", "Card Id already exists");
                                            }
                                        } else {
                                            btn_CardInit.setEnabled(true);
                                            showCustomAlertDialog(false, "Error", "Card Id cannot be left blank");
                                        }
                                    } catch (Exception e) {
                                        btn_CardInit.setEnabled(true);
                                        Toast.makeText(getBaseContext(), "Card init error in yes:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                            dialogci.show();

                            WindowManager.LayoutParams lpci = dialogci.getWindow().getAttributes();
                            lpci.dimAmount = 0.9f;
                            lpci.buttonBrightness = 1.0f;
                            lpci.screenBrightness = 1.0f;
                            dialogci.getWindow().setAttributes(lpci);
                            dialogci.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

                            clear();

                        } catch (Exception e) {
                            Toast.makeText(getBaseContext(), "Card init error in yes:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    default:
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

    //================================= Custom Dialog For Card Refresh ======================================//

    public void showDialogForCardRefresh(String dialogTitle, String dialogMessage,
                                         final int readerType, final Object comm, final SmartCardInfo cardInfo) {
        final Context context = this;
        final Dialog dialog = new Dialog(context);
        dialog.setCanceledOnTouchOutside(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_confirm_dialog);

        // set the custom dialog components - text, image and button

        ImageView icon = (ImageView) dialog.findViewById(R.id.image);

        TextView title = (TextView) dialog.findViewById(R.id.title);
        TextView message = (TextView) dialog.findViewById(R.id.message);

        Button btn_No = (Button) dialog.findViewById(R.id.btnNo);
        Button btn_Yes = (Button) dialog.findViewById(R.id.btnYes);

        icon.setImageResource(R.drawable.success);
        title.setText(dialogTitle);
        message.setText(dialogMessage);

        btn_No.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                switch (readerType) {

                    case 1://Card Refresh For Usb Card Reader With No Card Id Change
                        try {

//                            String strNewCardId = "";
                            //AsyncTaskUsbCardRefresh cardRefresh = new AsyncTaskUsbCardRefresh(empAutoId, strCardSerailNo, strOldCardId, strNewCardId, isCardCreatedLocally);
                            //cardRefresh.execute();

                            MicroSmartV2Communicator mComm = (MicroSmartV2Communicator) comm;
                            String strNewCardId = "";
                            AsyncTaskUsbCardRefresh cardRefresh = new AsyncTaskUsbCardRefresh(mComm, cardInfo, strNewCardId);
                            cardRefresh.execute();

                        } catch (Exception e) {
                            Toast.makeText(getBaseContext(), "Error in card refresh no click showDialogForInitAndRefresh:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        break;

                    case 2: //Card Refresh For Spi Card Reader With No Card Id Change

                        try {

                            //String strNewCardId = "";
                            // AsyncTaskSpiCardRefresh cardRefresh = new AsyncTaskSpiCardRefresh(empAutoId, strCardSerailNo, strOldCardId, strNewCardId, isCardCreatedLocally);
                            // cardRefresh.execute();

                        } catch (Exception e) {
                            Toast.makeText(getBaseContext(), "Error in card refresh no click showDialogForInitAndRefresh:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        break;

                    case 3://RC522

                        String strNewCardId = "";
                        RC522Communicator mComm = (RC522Communicator) comm;
                        new AsyncTaskRC522CardRefresh(mComm, cardInfo, strNewCardId).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        break;

                    default:
                        break;
                }

            }
        });


        btn_Yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                switch (readerType) {

                    case 1: // Card Refresh For MicroSmartV2 Card Reader With Card Id Change

                        final Context contextusb = SmartCardActivity.this;
                        final Dialog dialogusb = new Dialog(contextusb);
                        dialogusb.setCanceledOnTouchOutside(true);
                        dialogusb.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialogusb.setContentView(R.layout.cardid_dialog);

                        ImageView iconusb = (ImageView) dialogusb.findViewById(R.id.image);
                        TextView titleusb = (TextView) dialogusb.findViewById(R.id.title);
                        final EditText etNewCardId = (EditText) dialogusb.findViewById(R.id.cardId);
                        Button btn_Ok_usb = (Button) dialogusb.findViewById(R.id.btnOk);

                        iconusb.setImageResource(R.drawable.success);
                        titleusb.setText("Card ID");

                        btn_Ok_usb.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialogusb.dismiss();
                                MicroSmartV2Communicator mComm = (MicroSmartV2Communicator) comm;
                                try {
                                    String strNewCardId = "";
                                    strNewCardId = etNewCardId.getText().toString().trim();
                                    if (strNewCardId != null && strNewCardId.trim().length() > 0) {
                                        int autoId = -1;
                                        autoId = dbComm.isNewCardIdExists(strNewCardId);
                                        if (autoId == -1 || cardInfo.getCardId().equals(strNewCardId)) {
                                            AsyncTaskUsbCardRefresh cardRefresh = new AsyncTaskUsbCardRefresh(mComm, cardInfo, strNewCardId);
                                            cardRefresh.execute();
                                        } else {
                                            showCustomAlertDialog(false, "Error", "Card Id Already Exists");
                                        }
                                    } else {
                                        showCustomAlertDialog(false, "Error", "Card Id Cannot Be Left Blank");
                                    }
                                } catch (Exception e) {
                                    Toast.makeText(getBaseContext(), "Card refesh error in yes:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        dialogusb.show();
                        WindowManager.LayoutParams lpusb = dialogusb.getWindow().getAttributes();
                        lpusb.dimAmount = 0.9f;
                        lpusb.buttonBrightness = 1.0f;
                        lpusb.screenBrightness = 1.0f;
                        dialogusb.getWindow().setAttributes(lpusb);
                        dialogusb.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

                        break;

                    case 2: // Card Refresh For Spi Card Reader With Card Id Change

                        final Context contextspi = SmartCardActivity.this;
                        final Dialog dialogspi = new Dialog(contextspi);
                        dialogspi.setCanceledOnTouchOutside(true);
                        dialogspi.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialogspi.setContentView(R.layout.cardid_dialog);

                        ImageView iconspi = (ImageView) dialogspi.findViewById(R.id.image);
                        TextView titlespi = (TextView) dialogspi.findViewById(R.id.title);
                        final EditText editTextCardIdSpi = (EditText) dialogspi.findViewById(R.id.cardId);
                        Button btn_Ok_spi = (Button) dialogspi.findViewById(R.id.btnOk);

                        iconspi.setImageResource(R.drawable.success);
                        titlespi.setText("Card ID");

                        btn_Ok_spi.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                try {

                                    dialogspi.dismiss();

                                    String strNewCardId = "";
                                    strNewCardId = editTextCardIdSpi.getText().toString().trim();

                                    if (strNewCardId != null && strNewCardId.trim().length() > 0) {

                                        int isPresent = -1;
                                        isPresent = dbComm.isNewCardIdExists(strNewCardId);

                                        //                                    if (isPresent == -1 || strOldCardId.equals(strNewCardId)) {

//                                            AsyncTaskSpiCardRefresh cardRefresh = new AsyncTaskSpiCardRefresh(empAutoId, strCardSerailNo, strOldCardId, strNewCardId, isCardCreatedLocally);
                                        //                                          cardRefresh.execute();
//
                                        //                                      } else {
                                        showCustomAlertDialog(false, "New Card Id", "Card Id Already Exists");
                                        //                                    }

                                    } else {
                                        showCustomAlertDialog(false, "New Card Id", "Card Id Cannot Be Left Blank");
                                    }

                                } catch (Exception e) {
                                    Toast.makeText(getBaseContext(), "Card refesh error in yes:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        dialogspi.show();

                        WindowManager.LayoutParams lpspi = dialogspi.getWindow().getAttributes();
                        lpspi.dimAmount = 0.9f;
                        lpspi.buttonBrightness = 1.0f;
                        lpspi.screenBrightness = 1.0f;
                        dialogspi.getWindow().setAttributes(lpspi);
                        dialogspi.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

                        break;

                    case 3://RC522 // Card Refresh For RC522 Card Reader With Card Id Change

                        final Context contextRC522 = SmartCardActivity.this;
                        final Dialog dialogRC522 = new Dialog(contextRC522);
                        dialogRC522.setCanceledOnTouchOutside(true);
                        dialogRC522.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialogRC522.setContentView(R.layout.cardid_dialog);

                        ImageView icon = (ImageView) dialogRC522.findViewById(R.id.image);
                        TextView title = (TextView) dialogRC522.findViewById(R.id.title);
                        final EditText etCardID = (EditText) dialogRC522.findViewById(R.id.cardId);
                        Button btn_Ok = (Button) dialogRC522.findViewById(R.id.btnOk);
                        icon.setImageResource(R.drawable.success);
                        title.setText("Card ID");

                        btn_Ok.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialogRC522.dismiss();
                                RC522Communicator mComm = (RC522Communicator) comm;
                                try {
                                    String strNewCardId = "";
                                    strNewCardId = etCardID.getText().toString().trim();
                                    if (strNewCardId != null && strNewCardId.trim().length() > 0) {
                                        new AsyncTaskRC522CardRefresh(mComm, cardInfo, strNewCardId).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                    } else {
                                        btn_CardRefresh.setEnabled(true);
                                        showCustomAlertDialog(false, "Error", "Card Id Cannot Be Left Blank");
                                    }
                                } catch (Exception e) {
                                    btn_CardRefresh.setEnabled(true);
                                    Toast.makeText(getBaseContext(), "Card refesh error in yes:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        dialogRC522.show();
                        WindowManager.LayoutParams lp = dialogRC522.getWindow().getAttributes();
                        lp.dimAmount = 0.9f;
                        lp.buttonBrightness = 1.0f;
                        lp.screenBrightness = 1.0f;
                        dialogRC522.getWindow().setAttributes(lp);
                        dialogRC522.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

                        break;

                    default:
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

    //================================= Custom Dialog For Card Id Change ======================================//

    public void showDialogForCardIdChange(String dialogTitle, String dialogMessage,
                                          final int readerType, final Object comm, final SmartCardInfo cardInfo) {
        final Context context = this;
        final Dialog dialog = new Dialog(context);
        dialog.setCanceledOnTouchOutside(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_confirm_dialog);

        // set the custom dialog components - text, image and button

        ImageView icon = (ImageView) dialog.findViewById(R.id.image);

        TextView title = (TextView) dialog.findViewById(R.id.title);
        TextView message = (TextView) dialog.findViewById(R.id.message);

        Button btn_No = (Button) dialog.findViewById(R.id.btnNo);
        Button btn_Yes = (Button) dialog.findViewById(R.id.btnYes);

        icon.setImageResource(R.drawable.success);
        title.setText(dialogTitle);
        message.setText(dialogMessage);

        btn_No.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btn_CardIdChange.setEnabled(true);
                dialog.dismiss();
            }
        });

        btn_Yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                switch (readerType) {
                    case 1: // Card Id Change For Usb Card Reader

                        final Context contextusb = SmartCardActivity.this;
                        final Dialog dialogusb = new Dialog(contextusb);
                        dialogusb.setCanceledOnTouchOutside(true);
                        dialogusb.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialogusb.setContentView(R.layout.cardid_dialog);

                        ImageView iconusb = (ImageView) dialogusb.findViewById(R.id.image);
                        TextView titleusb = (TextView) dialogusb.findViewById(R.id.title);
                        final EditText editTextCardIdUsb = (EditText) dialogusb.findViewById(R.id.cardId);
                        Button btn_Ok_usb = (Button) dialogusb.findViewById(R.id.btnOk);

                        iconusb.setImageResource(R.drawable.success);
                        titleusb.setText("Card ID");

                        btn_Ok_usb.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                try {
                                    MicroSmartV2Communicator mSmartV2Comm = (MicroSmartV2Communicator) comm;
                                    dialogusb.dismiss();
                                    String newCardId = editTextCardIdUsb.getText().toString().trim();
                                    if (newCardId.trim().length() > 0) {
                                        int isPresent = -1;
                                        isPresent = dbComm.isNewCardIdExists(newCardId);
                                        if (isPresent == -1) {
                                            AsyncTaskUsbCardIdChange cardIdChange = new AsyncTaskUsbCardIdChange(mSmartV2Comm, cardInfo, newCardId);
                                            cardIdChange.execute();
                                        } else {
                                            showCustomAlertDialog(false, "Card ID Change", "Card ID Already Exists");
                                        }
                                    } else {
                                        showCustomAlertDialog(false, "Card ID Change", "Card ID Cannot Be Left Blank");
                                    }
                                } catch (Exception e) {
                                    Toast.makeText(getBaseContext(), "Card id change error in yes:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        dialogusb.show();

                        WindowManager.LayoutParams lpusb = dialogusb.getWindow().getAttributes();
                        lpusb.dimAmount = 0.9f;
                        lpusb.buttonBrightness = 1.0f;
                        lpusb.screenBrightness = 1.0f;
                        dialogusb.getWindow().setAttributes(lpusb);
                        dialogusb.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

                        break;

                    case 2://Card Id Change For Spi Card Reader

                        final Context contextspi = SmartCardActivity.this;
                        final Dialog dialogspi = new Dialog(contextspi);
                        dialogspi.setCanceledOnTouchOutside(true);
                        dialogspi.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialogspi.setContentView(R.layout.cardid_dialog);

                        ImageView iconspi = (ImageView) dialogspi.findViewById(R.id.image);
                        TextView titlespi = (TextView) dialogspi.findViewById(R.id.title);
                        final EditText editTextCardIdSpi = (EditText) dialogspi.findViewById(R.id.cardId);
                        Button btn_Ok_spi = (Button) dialogspi.findViewById(R.id.btnOk);

                        iconspi.setImageResource(R.drawable.success);
                        titlespi.setText("Card ID");

                        btn_Ok_spi.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                try {
                                    dialogspi.dismiss();
                                    String strCardId = editTextCardIdSpi.getText().toString().trim();
                                    if (strCardId.trim().length() > 0) {
                                        AsyncTaskSpiCardIdChange cardIdChange = new AsyncTaskSpiCardIdChange();
                                        cardIdChange.execute(strCardId);
                                    } else {
                                        showCustomAlertDialog(false, "Card ID Change", "Card ID Cannot Be Left Blank");
                                    }
                                } catch (Exception e) {
                                    Toast.makeText(getBaseContext(), "Card id change error in yes:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        dialogspi.show();

                        WindowManager.LayoutParams lpspi = dialogspi.getWindow().getAttributes();
                        lpspi.dimAmount = 0.9f;
                        lpspi.buttonBrightness = 1.0f;
                        lpspi.screenBrightness = 1.0f;
                        dialogspi.getWindow().setAttributes(lpspi);
                        dialogspi.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

                        break;

                    case 3:

                        RC522Communicator rcComm = (RC522Communicator) comm;
                        final Context contextrc522 = SmartCardActivity.this;
                        final Dialog dialogrc522 = new Dialog(contextrc522);
                        dialogrc522.setCanceledOnTouchOutside(true);
                        dialogrc522.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialogrc522.setContentView(R.layout.cardid_dialog);

                        ImageView iconrc522 = (ImageView) dialogrc522.findViewById(R.id.image);
                        TextView titlerc522 = (TextView) dialogrc522.findViewById(R.id.title);
                        final EditText editTextCardIdrc522 = (EditText) dialogrc522.findViewById(R.id.cardId);
                        Button btn_Ok_rc522 = (Button) dialogrc522.findViewById(R.id.btnOk);

                        iconrc522.setImageResource(R.drawable.success);
                        titlerc522.setText("Card ID");

                        btn_Ok_rc522.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                try {
                                    dialogrc522.dismiss();
                                    String strCardId = editTextCardIdrc522.getText().toString().trim();
                                    if (strCardId.trim().length() > 0) {
                                        AsyncTaskRC522CardIdChange cardIdChangeTask = new AsyncTaskRC522CardIdChange();
                                        cardIdChangeTask.execute(strCardId);
                                    } else {
                                        btn_CardIdChange.setEnabled(true);
                                        showCustomAlertDialog(false, "Card ID Change", "Card ID Cannot Be Left Blank");
                                    }
                                } catch (Exception e) {
                                    btn_CardIdChange.setEnabled(true);
                                    Toast.makeText(getBaseContext(), "Card id change error in yes:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        dialogrc522.show();

                        WindowManager.LayoutParams lprc522 = dialogrc522.getWindow().getAttributes();
                        lprc522.dimAmount = 0.9f;
                        lprc522.buttonBrightness = 1.0f;
                        lprc522.screenBrightness = 1.0f;
                        dialogrc522.getWindow().setAttributes(lprc522);
                        dialogrc522.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

                        break;

                    default:
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


    //================================= Custom Dialog For New Card Issue ======================================//

    public void showDialogForNewCardIssue(boolean status, String strTitle, String strMessage,
                                          final String strReason, final SmartCardInfo cardInfo) {

        final Context context = this;
        final Dialog dialog = new Dialog(context);
        dialog.setCanceledOnTouchOutside(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_confirm_dialog);

        // set the custom dialog components - text, image and button

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
                try {
                    dialog.dismiss();
                    String empCardId = dbComm.getCardIdByEmpId(cardInfo.getEmployeeId());
                    if (empCardId.length() > 0) {
                        Calendar calendar = Calendar.getInstance();
                        SimpleDateFormat mdformat = new SimpleDateFormat("ddMMyyyy HH:mm:ss");
                        String strDateTime = mdformat.format(calendar.getTime());

                        int loginId = UserDetails.getInstance().getLoginId();
                        String isCardCreatedLocally = "Y";
                        String strNewCardId = "";
                        String strOperation = "New Card Issue";
                        String strStatus = "S";
                        int insertStatus = -1;
                        int newCardVersion = Integer.parseInt(cardInfo.getSmartCardVer());
                        int oldCardVersion = newCardVersion - 1;

                        insertStatus = dbComm.insertIntoSmartCardOperationLog(loginId, Integer.parseInt(cardInfo.getEnrollmentNo()), cardInfo.getReadCSN(), empCardId, oldCardVersion, strNewCardId, newCardVersion, strOperation, strStatus, isCardCreatedLocally, strDateTime);
                        if (insertStatus != -1) {
                            Log.d("TEST", "Smart Card Operation Log Inserted Successfully");
                        } else {
                            Log.d("TEST", "Smart Card Operation Log Insertion Failure");
                        }

                        String empCSN = dbComm.getSmartCardSrlNo(cardInfo.getEnrollmentNo());

                        insertStatus = dbComm.insertIntoHotListLog(loginId, Integer.parseInt(cardInfo.getEnrollmentNo()), empCSN, empCardId, oldCardVersion, strOperation, strStatus, isCardCreatedLocally, strReason, strDateTime);
                        if (insertStatus != -1) {
                            Log.d("TEST", "Hot List Log Inserted Successfully");
                        } else {
                            Log.d("TEST", "Hot List Log Insertion Failure");
                        }

                        int updateStatus = -1;
                        updateStatus = dbComm.updateSmartCardVer(Integer.parseInt(cardInfo.getEnrollmentNo()), cardInfo.getReadCSN(), cardInfo.getSmartCardVer());
                        if (updateStatus != -1) {
                            showCustomAlertDialog(true, "New Card Issue", "New Card Issued Successfully To Employee.");
                        } else {
                            showCustomAlertDialog(false, "Error", "Failed To Update New Card Details");
                        }
                    } else {
                        showCustomAlertDialog(false, "Error", "Employee Card Id Not Found");
                    }

                } catch (Exception e) {
                    Toast.makeText(getBaseContext(), "Card Read Error In Yes:" + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    //======================================== Custom Alert Dialog  ==========================================//

    public void showCustomAlertDialog(boolean status, String strTitle, final String strMessage) {

        final Context context = this;
        final Dialog dialog = new Dialog(context);
        dialog.setCanceledOnTouchOutside(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_alert_dialog);

        // set the custom dialog components - text, image and button

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

    //=============================================  Unregister Receiver  =================================================================

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceivers();
        stopADCReceiver();
        stopTimer();
        LoginSplashActivity.isLoaded = false;
    }

    public void stopTimer() {

        if (capReadTimer != null) {
            capReadTimer.cancel();
            capReadTimer.purge();
            capReadTimer = null;
        }

        if (batReadTimer != null) {
            batReadTimer.cancel();
            batReadTimer.purge();
            batReadTimer = null;
        }

        if (adcReadTimer != null) {
            adcReadTimer.cancel();
            adcReadTimer.purge();
            adcReadTimer = null;
        }
    }

    public void stopADCReceiver() {
        if (!isADCReceiverUnregistered) {
            isADCReceiverUnregistered = true;
            if (intent != null) {
                SmartCardActivity.this.stopService(intent);
            }
            if (receiver != null) {
                unregisterReceiver(receiver);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_smart_card_read_write, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.home) {
            stopADCReceiver();
            Intent previous = new Intent(SmartCardActivity.this, HomeActivity.class);
            startActivity(previous);
            overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            finish();
            return true;

        }
        if (id == R.id.refresh) {
            clear();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            stopADCReceiver();
            Intent menu = new Intent(SmartCardActivity.this, HomeActivity.class);
            startActivity(menu);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //===========================  HardwareConnection Methods  ================================================

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

    //========================  Extra Functions ===============================//

    //=========MOdified On 15-06-2017=======//

//    private class AsynTaskFactoryCard extends AsyncTask <String, Void, Void> {
//
//        ProgressDialog mypDialog;
//        String strLog = "";
//
//        @Override
//        protected void onPreExecute() {
//            mypDialog = new ProgressDialog(SmartCardActivity.this);
//            mypDialog.setMessage("Changing Card To Factory Card Wait...");
//            mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//            mypDialog.setCanceledOnTouchOutside(false);
//            mypDialog.show();
//        }
//
//
//        @Override
//        protected Void doInBackground(String... params) {
//
//
//            // keyWriteStatus=CardKeyUpdate(2,sec[0],11,strInitialKeyB,strFinalKeyA,strAccessCode,strFinalKeyB);
//
//            // String strNewCardId=params[0].trim();
//
//            Cursor sectorKeyData = dbComm.getSectorAndKeyForWriteCard();
//
//            if (sectorKeyData != null) {
//                int writeStatus = -1;
//                int readStatus = -1;
//
//                String strFinalKeyA = "FFFFFFFFFFFF";
//                String strFinalKeyB = "FFFFFFFFFFFF";
//
//
//                String AddRead = "#FF46";
//                String AddWrite = "#FF47";
//                String msg = "";
//
////                String[] secRead = {"01A0", "01A1", "01A2", "01A4", "01A5", "01A6", "01A7", "01A8", "01A9", "01AA", "01AB", "01AC", "01AD", "01AE", "01AF"};
////                String secWrite[] = {"01A001", "01A101", "01A201", "01A401", "01A501", "01A601", "01A701", "01A801", "01A901", "01AA01", "01AB01", "01AC01", "01AD01", "01AE01", "01AF01"};
//
//
//                String[] secRead = {"01A0", "01A1", "01A2", "01A3", "01A4", "01A5", "01A6", "01A7", "01A8", "01A9", "01AA", "01AB", "01AC", "01AD", "01AE", "01AF"};
//
//                String secWrite[] = {"01A001", "01A101", "01A201", "01A301", "01A401", "01A501", "01A601", "01A701", "01A801", "01A901", "01AA01", "01AB01", "01AC01", "01AD01", "01AE01", "01AF01"};
//
//
//                int checksum;
//
//                while (sectorKeyData.moveToNext()) {
//
//                    String strSectorNo = sectorKeyData.getString(0).trim();
//                    String strInitialKeyA = sectorKeyData.getString(1).trim();
//                    String strInitialKeyB = sectorKeyData.getString(2).trim();
////
////                    String strSectorNo = sectorKeyData.getString(0).trim();
////                    String strInitialKeyA = "FFFFFFFFFFFF";
////                    String strInitialKeyB = "FFFFFFFFFFFF";
//
//
//                    int sectorNo = Integer.parseInt(strSectorNo);
//
//                    switch (sectorNo) {
//
//                        case 0:
//
//                            msg = AddRead + secRead[0] + "00" + strInitialKeyA + "01";
//
//                            checksum = Checksum(msg);
//                            msg = msg + Integer.toHexString(checksum) + "%";
//                            if (Integer.toHexString(checksum).toUpperCase().length() != 2) {
//                                msg = msg + "0" + Integer.toHexString(checksum).toUpperCase() + "%";
//
//                            } else {
//                                msg = msg + Integer.toHexString(checksum).toUpperCase() + "%";
//                            }
//
//                            //  readStatus = validateSectorRead(msg);
//
//                            if (readStatus != -1) {
//                                strLog += "Sector 0 Read Successfull With Fortuna Key\n";
//
//                                writeStatus = CardKeyUpdateInit(0, secWrite[0], 03, strInitialKeyB, strFinalKeyA, "7F0788C1", strFinalKeyB);
//
//                                //  writeStatus = CardKeyUpdateInit(0, secWrite[0], 03, strInitialKeyB, strFinalKeyA, "FFFFFFFF", strFinalKeyB);
//
//                                if (writeStatus != -1) {
//                                    strLog += "Sector 0 Key Updated Successfully First Time\n";
//                                } else {
//                                    strLog += "Sector 0 Key Updated Failure First Time\n";
//
//                                    writeStatus = CardKeyUpdateInit(0, secWrite[0], 03, strInitialKeyB, strFinalKeyA, "7F0788C1", strFinalKeyB);
//
//                                    // writeStatus = CardKeyUpdateInit(0, secWrite[0], 03, strInitialKeyB, strFinalKeyA, "FFFFFFFF", strFinalKeyB);
//
//                                    if (writeStatus != -1) {
//                                        strLog += "Sector 0 Key Updated Successfully Second Time\n";
//                                    } else {
//                                        strLog += "Sector 0 Key Updation Failure Second Time\n";
//                                    }
//
//                                }
//
//                            } else {
//                                strLog += "Failed To Read To Sector 0\n";
//                            }
//
//                            break;
//
//                        case 1:
//
//                            msg = AddRead + secRead[1] + "00" + strInitialKeyA + "01";
//
//                            checksum = Checksum(msg);
//                            msg = msg + Integer.toHexString(checksum) + "%";
//                            if (Integer.toHexString(checksum).toUpperCase().length() != 2) {
//                                msg = msg + "0" + Integer.toHexString(checksum).toUpperCase() + "%";
//
//                            } else {
//                                msg = msg + Integer.toHexString(checksum).toUpperCase() + "%";
//                            }
//
//                            //    readStatus = validateSectorRead(msg);
//
//                            if (readStatus != -1) {
//                                strLog += "Sector 1 Read Successfull With Fortuna Key\n";
//
//                                writeStatus = CardKeyUpdateInit(1, secWrite[1], 07, strInitialKeyB, strFinalKeyA, "787788C1", strFinalKeyB);
//
//                                // writeStatus = CardKeyUpdateInit(1, secWrite[1], 07, strInitialKeyB, strFinalKeyA, "FFFFFFFF", strFinalKeyB);
//
//                                if (writeStatus != -1) {
//                                    strLog += "Sector 1 Key Updated Successfully First Time\n";
//
//                                } else {
//                                    strLog += "Sector 1 Key Updation Failure First Time\n";
//
//                                    writeStatus = CardKeyUpdateInit(1, secWrite[1], 07, strInitialKeyB, strFinalKeyA, "787788C1", strFinalKeyB);
//
//                                    //   writeStatus = CardKeyUpdateInit(1, secWrite[1], 07, strInitialKeyB, strFinalKeyA, "FFFFFFFF", strFinalKeyB);
//
//                                    if (writeStatus != -1) {
//                                        strLog += "Sector 1 Key Updated Successfully Second Time\n";
//                                    } else {
//                                        strLog += "Sector 1 Key Updation Failure Second Time\n";
//                                    }
//                                }
//                            } else {
//                                strLog += "Failed To Read To Sector 1\n";
//                            }
//
//                            break;
//
//                        case 2:
//
//                            msg = AddRead + secRead[2] + "00" + strInitialKeyA + "01";
//                            checksum = Checksum(msg);
//                            msg = msg + Integer.toHexString(checksum) + "%";
//                            if (Integer.toHexString(checksum).toUpperCase().length() != 2) {
//                                msg = msg + "0" + Integer.toHexString(checksum).toUpperCase() + "%";
//
//                            } else {
//                                msg = msg + Integer.toHexString(checksum).toUpperCase() + "%";
//                            }
//
//                            //     readStatus = validateSectorRead(msg);
//
//                            if (readStatus != -1) {
//                                strLog += "Sector 2 Read Successfull With Fortuna Key\n";
//
//                                writeStatus = CardKeyUpdateInit(2, secWrite[2], 11, strInitialKeyB, strFinalKeyA, "7F0788C1", strFinalKeyB);
//
//                                //writeStatus = CardKeyUpdateInit(2, secWrite[2], 11, strInitialKeyB, strFinalKeyA, "FFFFFFFF", strFinalKeyB);
//
//                                if (writeStatus != -1) {
//                                    strLog += "Sector 2 Key Updated Successfully First Time\n";
//                                } else {
//                                    strLog += "Sector 2 Key Updation Failure First Time\n";
//
//                                    writeStatus = CardKeyUpdateInit(2, secWrite[2], 11, strInitialKeyB, strFinalKeyA, "7F0788C1", strFinalKeyB);
//
//                                    //  writeStatus = CardKeyUpdateInit(2, secWrite[2], 11, strInitialKeyB, strFinalKeyA, "FFFFFFFF", strFinalKeyB);
//
//                                    if (writeStatus != -1) {
//                                        strLog += "Sector 2 Key Updated Successfully Second Time\n";
//                                    } else {
//                                        strLog += "Sector 2 Key Updation Failure Second Time\n";
//                                    }
//                                }
//
//                                ///////////////////////////////////////////////////////////////////////////////////////
//
//                                //=====================Refresh Sector Using Factory Key======================//
////
////                                msg = AddWrite + secWrite[2] + strDefaultKeyB + "00" +strEmptyData;
////
////                                checksum = Checksum(msg);
////                                if(Integer.toHexString(checksum).toUpperCase().length() != 2) {
////                                    msg = msg + "0" + Integer.toHexString(checksum).toUpperCase() + "%";
////                                }else {
////                                    msg = msg + Integer.toHexString(checksum).toUpperCase() + "%";
////                                }
//
//
////                                writeStatus=SectorWrite(msg,"2");
////
////                                if(writeStatus!=-1)
////                                {
////                                    strLog+="Sector 2 Refreshed Successfully First Time\n";
////
////                                    writeStatus=CardKeyUpdate(2,secWrite[2],11,strKeyA,strDefaultKeyB,strAccessCode,strKeyB);
////
////                                    if(writeStatus!=-1)
////                                    {
////                                        strLog+="Sector 2 Key Updated Successfully First Time\n";
////                                    }
////                                    else
////                                    {
////                                        strLog+="Sector 2 Key Updation Failure First Time\n";
////
////                                        writeStatus=CardKeyUpdate(2,secWrite[2],11,strKeyA,strDefaultKeyB,strAccessCode,strKeyB);
////
////                                        if(writeStatus!=-1)
////                                        {
////                                            strLog+="Sector 2 Key Updated Successfully Second Time\n";
////                                        }
////                                        else
////                                        {
////                                            strLog+="Sector 2 Key Updation Failure Second Time\n";
////                                        }
////
////                                    }
////
////                                }
////                                else
////                                {
////                                    strLog+="Sector 2 Refreshed Failure First Time\n";
////
////                                    writeStatus=SectorWrite(msg, "2");
////
////                                    if(writeStatus!=-1)
////                                    {
////                                        strLog+="Sector 2 Refreshed Successfully Second Time\n";
////
////                                        writeStatus=CardKeyUpdate(2,secWrite[2],11,strKeyA,strDefaultKeyB,strAccessCode,strKeyB);
////
////                                        if(writeStatus!=-1)
////                                        {
////                                            strLog+="Sector 2 Key Updated Successfully First Time\n";
////                                        }
////                                        else
////                                        {
////                                            strLog+="Sector 2 Key Updation Failure First Time\n";
////
////                                            writeStatus=CardKeyUpdate(2,secWrite[2],11,strKeyA,strDefaultKeyB,strAccessCode,strKeyB);
////
////                                            if(writeStatus!=-1)
////                                            {
////                                                strLog+="Sector 2 Key Updated Successfully Second Time\n";
////                                            }
////                                            else
////                                            {
////                                                strLog+="Sector 2 Key Updation Failure Second Time\n";
////                                            }
////                                        }
////
////                                    }else
////                                    {
////                                        strLog+="Sector 2 Refreshed Failure Second Time\n";
////                                    }
////
////                                  }
//
//                            } else {
//                                strLog += "Failed To Read To Sector 2\n";
//                            }
//                            break;
//
//                        case 3:
//
//                            msg = AddRead + secRead[3] + "00" + strInitialKeyA + "01";
//                            checksum = Checksum(msg);
//                            msg = msg + Integer.toHexString(checksum) + "%";
//                            if (Integer.toHexString(checksum).toUpperCase().length() != 2) {
//                                msg = msg + "0" + Integer.toHexString(checksum).toUpperCase() + "%";
//
//                            } else {
//                                msg = msg + Integer.toHexString(checksum).toUpperCase() + "%";
//                            }
//
//                            //  readStatus = validateSectorRead(msg);
//
//                            if (readStatus != -1) {
//                                strLog += "Sector 3 Read Successfull With Fortuna Key\n";
//
//                                writeStatus = CardKeyUpdateInit(3, secWrite[3], 15, strInitialKeyB, strFinalKeyA, "7F0788C1", strFinalKeyB);
//
//                                // writeStatus = CardKeyUpdateInit(3, secWrite[3], 15, strInitialKeyB, strFinalKeyA, "FFFFFFFF", strFinalKeyB);
//
//                                if (writeStatus != -1) {
//                                    strLog += "Sector 3 Key Updated Successfully First Time\n";
//                                } else {
//                                    strLog += "Sector 3 Key Updation Failure First Time\n";
//
//                                    writeStatus = CardKeyUpdateInit(3, secWrite[3], 15, strInitialKeyB, strFinalKeyA, "7F0788C1", strFinalKeyB);
//
//                                    // writeStatus = CardKeyUpdateInit(3, secWrite[3], 15, strInitialKeyB, strFinalKeyA, "FFFFFFFF", strFinalKeyB);
//
//                                    if (writeStatus != -1) {
//                                        strLog += "Sector 3 Key Updated Successfully Second Time\n";
//                                    } else {
//                                        strLog += "Sector 3 Key Updation Failure Second Time\n";
//                                    }
//
//                                }
//
//                                ///////////////////////////////////////////////////////////////////////////////////////
//
//                                //=====================Refresh Sector Using Factory Key======================//
////
////                                msg = AddWrite + secWrite[2] + strDefaultKeyB + "00" +strEmptyData;
////
////                                checksum = Checksum(msg);
////                                if(Integer.toHexString(checksum).toUpperCase().length() != 2) {
////                                    msg = msg + "0" + Integer.toHexString(checksum).toUpperCase() + "%";
////                                }else {
////                                    msg = msg + Integer.toHexString(checksum).toUpperCase() + "%";
////                                }
//
//
////                                writeStatus=SectorWrite(msg,"2");
////
////                                if(writeStatus!=-1)
////                                {
////                                    strLog+="Sector 2 Refreshed Successfully First Time\n";
////
////                                    writeStatus=CardKeyUpdate(2,secWrite[2],11,strKeyA,strDefaultKeyB,strAccessCode,strKeyB);
////
////                                    if(writeStatus!=-1)
////                                    {
////                                        strLog+="Sector 2 Key Updated Successfully First Time\n";
////                                    }
////                                    else
////                                    {
////                                        strLog+="Sector 2 Key Updation Failure First Time\n";
////
////                                        writeStatus=CardKeyUpdate(2,secWrite[2],11,strKeyA,strDefaultKeyB,strAccessCode,strKeyB);
////
////                                        if(writeStatus!=-1)
////                                        {
////                                            strLog+="Sector 2 Key Updated Successfully Second Time\n";
////                                        }
////                                        else
////                                        {
////                                            strLog+="Sector 2 Key Updation Failure Second Time\n";
////                                        }
////
////                                    }
////
////                                }
////                                else
////                                {
////                                    strLog+="Sector 2 Refreshed Failure First Time\n";
////
////                                    writeStatus=SectorWrite(msg, "2");
////
////                                    if(writeStatus!=-1)
////                                    {
////                                        strLog+="Sector 2 Refreshed Successfully Second Time\n";
////
////                                        writeStatus=CardKeyUpdate(2,secWrite[2],11,strKeyA,strDefaultKeyB,strAccessCode,strKeyB);
////
////                                        if(writeStatus!=-1)
////                                        {
////                                            strLog+="Sector 2 Key Updated Successfully First Time\n";
////                                        }
////                                        else
////                                        {
////                                            strLog+="Sector 2 Key Updation Failure First Time\n";
////
////                                            writeStatus=CardKeyUpdate(2,secWrite[2],11,strKeyA,strDefaultKeyB,strAccessCode,strKeyB);
////
////                                            if(writeStatus!=-1)
////                                            {
////                                                strLog+="Sector 2 Key Updated Successfully Second Time\n";
////                                            }
////                                            else
////                                            {
////                                                strLog+="Sector 2 Key Updation Failure Second Time\n";
////                                            }
////                                        }
////
////                                    }else
////                                    {
////                                        strLog+="Sector 2 Refreshed Failure Second Time\n";
////                                    }
////
////                                  }
//
//                            } else {
//                                strLog += "Failed To Read To Sector 2\n";
//                            }
//
//                            break;
//
//                        case 4:
//
//                            msg = AddRead + secRead[4] + "00" + strInitialKeyA + "01";
//
//                            checksum = Checksum(msg);
//                            msg = msg + Integer.toHexString(checksum) + "%";
//                            if (Integer.toHexString(checksum).toUpperCase().length() != 2) {
//                                msg = msg + "0" + Integer.toHexString(checksum).toUpperCase() + "%";
//
//                            } else {
//                                msg = msg + Integer.toHexString(checksum).toUpperCase() + "%";
//                            }
//
//                            //readStatus = validateSectorRead(msg);
//                            if (readStatus != -1) {
//                                strLog += "Sector 4 Read Successfull With Fortuna Key\n";
//
//                                writeStatus = CardKeyUpdateInit(4, secWrite[4], 19, strInitialKeyB, strFinalKeyA, "7F0788C1", strFinalKeyB);
//
//                                // writeStatus = CardKeyUpdateInit(4, secWrite[4], 19, strInitialKeyB, strFinalKeyA, "FFFFFFFF", strFinalKeyB);
//
//                                if (writeStatus != -1) {
//                                    strLog += "Sector 4 Key Updated Successfully First Time\n";
//                                } else {
//                                    strLog += "Sector 4 Key Updation Failure First Time\n";
//
//                                    writeStatus = CardKeyUpdateInit(4, secWrite[4], 19, strInitialKeyB, strFinalKeyA, "7F0788C1", strFinalKeyB);
//
//                                    //  writeStatus = CardKeyUpdateInit(4, secWrite[4], 19, strInitialKeyB, strFinalKeyA, "FFFFFFFF", strFinalKeyB);
//
//                                    if (writeStatus != -1) {
//                                        strLog += "Sector 4 Key Updated Successfully Second Time\n";
//                                    } else {
//                                        strLog += "Sector 4 Key Updation Failure Second Time\n";
//                                    }
//
//                                }
//
//                                ////////////////////////////////////////////////////////////////////////////////////
//
//                                //=====================Refresh Sector Using Factory Key======================//
//
////                                msg = AddWrite + secWrite[3] + strDefaultKeyB + "00" +strEmptyData;
////                                checksum = Checksum(msg);
////                                if(Integer.toHexString(checksum).toUpperCase().length() != 2) {
////                                    msg = msg + "0" + Integer.toHexString(checksum).toUpperCase() + "%";
////                                }else {
////                                    msg = msg + Integer.toHexString(checksum).toUpperCase() + "%";
////                                }
//
//
////                                writeStatus=SectorWrite(msg, "4");
////
////                                if(writeStatus!=-1)
////                                {
////                                    strLog+="Sector 4 Refreshed Successfully First Time\n";
////
////                                    writeStatus=CardKeyUpdate(4,secWrite[3],19,strKeyA,strDefaultKeyB,strAccessCode,strKeyB);
////
////                                    if(writeStatus!=-1)
////                                    {
////                                        strLog+="Sector 4 Key Updated Successfully First Time\n";
////                                    }
////                                    else
////                                    {
////                                        strLog+="Sector 4 Key Updation Failure First Time\n";
////
////                                        writeStatus=CardKeyUpdate(4,secWrite[3],19,strKeyA,strDefaultKeyB,strAccessCode,strKeyB);
////
////                                        if(writeStatus!=-1)
////                                        {
////                                            strLog+="Sector 4 Key Updated Successfully Second Time\n";
////                                        }
////                                        else
////                                        {
////                                            strLog+="Sector 4 Key Updation Failure Second Time\n";
////                                        }
////
////                                    }
////
////                                }
////                                else
////                                {
////                                    strLog+="Sector 4 Refreshed Failure First Time\n";
////
////                                    writeStatus=SectorWrite(msg, "4");
////
////                                    if(writeStatus!=-1)
////                                    {
////                                        strLog+="Sector 4 Refreshed Successfully Second Time\n";
////
////                                        writeStatus=CardKeyUpdate(4,secWrite[3],19,strKeyA,strDefaultKeyB,strAccessCode,strKeyB);
////
////                                        if(writeStatus!=-1)
////                                        {
////                                            strLog+="Sector 4 Key Updated Successfully First Time\n";
////                                        }
////                                        else
////                                        {
////                                            strLog+="Sector 4 Key Updation Failure First Time\n";
////
////                                            writeStatus=CardKeyUpdate(4,secWrite[3],19,strKeyA,strDefaultKeyB,strAccessCode,strKeyB);
////
////                                            if(writeStatus!=-1)
////                                            {
////                                                strLog+="Sector 4 Key Updated Successfully Second Time\n";
////                                            }
////                                            else
////                                            {
////                                                strLog+="Sector 4 Key Updation Failure Second Time\n";
////                                            }
////                                        }
////
////                                    }else
////                                    {
////                                        strLog+="Sector 4 Refreshed Failure Second Time\n";
////                                    }
////
////                                }
//
//                            } else {
//                                strLog += "Failed To Read To Sector 4\n";
//                            }
//
//                            break;
//
//
//                        case 5:
//
//                            msg = AddRead + secRead[5] + "00" + strInitialKeyA + "01";
//
//                            checksum = Checksum(msg);
//                            msg = msg + Integer.toHexString(checksum) + "%";
//                            if (Integer.toHexString(checksum).toUpperCase().length() != 2) {
//                                msg = msg + "0" + Integer.toHexString(checksum).toUpperCase() + "%";
//
//                            } else {
//                                msg = msg + Integer.toHexString(checksum).toUpperCase() + "%";
//                            }
//
//                            // readStatus = validateSectorRead(msg);
//
//                            if (readStatus != -1) {
//                                strLog += "Sector 5 Read Successfull With Fortuna Key\n";
//
//                                writeStatus = CardKeyUpdateInit(5, secWrite[5], 23, strInitialKeyB, strFinalKeyA, "7F0788C1", strFinalKeyB);
//                                // writeStatus = CardKeyUpdateInit(5, secWrite[5], 23, strInitialKeyB, strFinalKeyA, "FFFFFFFF", strFinalKeyB);
//
//                                if (writeStatus != -1) {
//                                    strLog += "Sector 5 Key Updated Successfully First Time\n";
//                                } else {
//                                    strLog += "Sector 5 Key Updation Failure First Time\n";
//
//                                    writeStatus = CardKeyUpdateInit(5, secWrite[5], 23, strInitialKeyB, strFinalKeyA, "7F0788C1", strFinalKeyB);
//                                    // writeStatus = CardKeyUpdateInit(5, secWrite[5], 23, strInitialKeyB, strFinalKeyA, "FFFFFFFF", strFinalKeyB);
//
//                                    if (writeStatus != -1) {
//                                        strLog += "Sector 5 Key Updated Successfully Second Time\n";
//                                    } else {
//                                        strLog += "Sector 5 Key Updation Failure Second Time\n";
//                                    }
//
//                                }
//
//                                ///////////////////////////////////////////////////////////////////////////////////
//
//                                //=====================Refresh Sector Using Factory Key======================//
//
////                                msg = AddWrite + secWrite[4] + strDefaultKeyB + "00" +strEmptyData;
////                                checksum = Checksum(msg);
////                                if(Integer.toHexString(checksum).toUpperCase().length() != 2) {
////                                    msg = msg + "0" + Integer.toHexString(checksum).toUpperCase() + "%";
////                                }else {
////                                    msg = msg + Integer.toHexString(checksum).toUpperCase() + "%";
////                                }
//
//
//                                //                               writeStatus=SectorWrite(msg, "5");
//
//                                //=============================================================================//
//
////                                if(writeStatus!=-1)
////                                {
////                                    strLog+="Sector 5 Refreshed Successfully First Time\n";
////
////                                    writeStatus=CardKeyUpdate(5,secWrite[4],23,strKeyA,strDefaultKeyB,strAccessCode,strKeyB);
////
////                                    if(writeStatus!=-1)
////                                    {
////                                        strLog+="Sector 5 Key Updated Successfully First Time\n";
////                                    }
////                                    else
////                                    {
////                                        strLog+="Sector 5 Key Updation Failure First Time\n";
////
////                                        writeStatus=CardKeyUpdate(5,secWrite[4],23,strKeyA,strDefaultKeyB,strAccessCode,strKeyB);
////
////                                        if(writeStatus!=-1)
////                                        {
////                                            strLog+="Sector 5 Key Updated Successfully Second Time\n";
////                                        }
////                                        else
////                                        {
////                                            strLog+="Sector 5 Key Updation Failure Second Time\n";
////                                        }
////
////                                    }
////
////                                }
////                                else
////                                {
////                                    strLog+="Sector 5 Refreshed Failure First Time\n";
////
////                                    writeStatus=SectorWrite(msg, "5");
////
////                                    if(writeStatus!=-1)
////                                    {
////                                        strLog+="Sector 5 Refreshed Successfully Second Time\n";
////
////                                        writeStatus=CardKeyUpdate(5,secWrite[4],23,strKeyA,strDefaultKeyB,strAccessCode,strKeyB);
////
////                                        if(writeStatus!=-1)
////                                        {
////                                            strLog+="Sector 5 Key Updated Successfully First Time\n";
////                                        }
////                                        else
////                                        {
////                                            strLog+="Sector 5 Key Updation Failure First Time\n";
////
////                                            writeStatus=CardKeyUpdate(5,secWrite[4],23,strKeyA,strDefaultKeyB,strAccessCode,strKeyB);
////
////                                            if(writeStatus!=-1)
////                                            {
////                                                strLog+="Sector 5 Key Updated Successfully Second Time\n";
////                                            }
////                                            else
////                                            {
////                                                strLog+="Sector 5 Key Updation Failure Second Time\n";
////                                            }
////                                        }
////
////                                    }else
////                                    {
////                                        strLog+="Sector 5 Refreshed Failure Second Time\n";
////                                    }
////
////                                }
//
//
//                            } else {
//                                strLog += "Failed To Read To Sector 5\n";
//                            }
//
//                            break;
//
//                        case 6:
//
//                            msg = AddRead + secRead[6] + "00" + strInitialKeyA + "01";
//                            checksum = Checksum(msg);
//                            msg = msg + Integer.toHexString(checksum) + "%";
//                            if (Integer.toHexString(checksum).toUpperCase().length() != 2) {
//                                msg = msg + "0" + Integer.toHexString(checksum).toUpperCase() + "%";
//
//                            } else {
//                                msg = msg + Integer.toHexString(checksum).toUpperCase() + "%";
//                            }
//
//                            //  readStatus = validateSectorRead(msg);
//
//                            if (readStatus != -1) {
//                                strLog += "Sector 6 Read Successfull With Fortuna Key\n";
//
//                                writeStatus = CardKeyUpdateInit(6, secWrite[6], 27, strInitialKeyB, strFinalKeyA, "7F0788C1", strFinalKeyB);
//
//                                // writeStatus = CardKeyUpdateInit(6, secWrite[6], 27, strInitialKeyB, strFinalKeyA, "FFFFFFFF", strFinalKeyB);
//                                if (writeStatus != -1) {
//                                    strLog += "Sector 6 Key Updated Successfully First Time\n";
//                                } else {
//                                    strLog += "Sector 6 Key Updation Failure First Time\n";
//
//                                    writeStatus = CardKeyUpdateInit(6, secWrite[6], 27, strInitialKeyB, strFinalKeyA, "7F0788C1", strFinalKeyB);
//
//                                    //   writeStatus = CardKeyUpdateInit(6, secWrite[6], 27, strInitialKeyB, strFinalKeyA, "FFFFFFFF", strFinalKeyB);
//
//                                    if (writeStatus != -1) {
//                                        strLog += "Sector 6 Key Updated Successfully Second Time\n";
//                                    } else {
//                                        strLog += "Sector 6 Key Updation Failure Second Time\n";
//                                    }
//
//                                }
//
//                                /////////////////////////////////////////////////////////////////////////////////
//
//                                //=====================Refresh Sector Using Factory Key======================//
//
////                                msg = AddWrite + secWrite[5] + strDefaultKeyB + "00" +strEmptyData;
////                                checksum = Checksum(msg);
////                                if(Integer.toHexString(checksum).toUpperCase().length() != 2) {
////                                    msg = msg + "0" + Integer.toHexString(checksum).toUpperCase() + "%";
////                                }else {
////                                    msg = msg + Integer.toHexString(checksum).toUpperCase() + "%";
////                                }
//
////                                writeStatus=SectorWrite(msg, "6");
////
////                                //=============================================================================//
////
////                                if(writeStatus!=-1)
////                                {
////                                    strLog+="Sector 6 Refreshed Successfully First Time\n";
////
////                                    writeStatus=CardKeyUpdate(6,secWrite[5],27,strKeyA,strDefaultKeyB,strAccessCode,strKeyB);
////
////                                    if(writeStatus!=-1)
////                                    {
////                                        strLog+="Sector 6 Key Updated Successfully First Time\n";
////                                    }
////                                    else
////                                    {
////                                        strLog+="Sector 6 Key Updation Failure First Time\n";
////
////                                        writeStatus=CardKeyUpdate(6,secWrite[5],27,strKeyA,strDefaultKeyB,strAccessCode,strKeyB);
////
////                                        if(writeStatus!=-1)
////                                        {
////                                            strLog+="Sector 6 Key Updated Successfully Second Time\n";
////                                        }
////                                        else
////                                        {
////                                            strLog+="Sector 6 Key Updation Failure Second Time\n";
////                                        }
////
////                                    }
////
////                                }
////                                else
////                                {
////                                    strLog+="Sector 6 Refreshed Failure First Time\n";
////
////                                    writeStatus=SectorWrite(msg, "6");
////
////                                    if(writeStatus!=-1)
////                                    {
////                                        strLog+="Sector 6 Refreshed Successfully Second Time\n";
////
////                                        writeStatus=CardKeyUpdate(6,secWrite[5],27,strKeyA,strDefaultKeyB,strAccessCode,strKeyB);
////
////                                        if(writeStatus!=-1)
////                                        {
////                                            strLog+="Sector 6 Key Updated Successfully First Time\n";
////                                        }
////                                        else
////                                        {
////                                            strLog+="Sector 6 Key Updation Failure First Time\n";
////
////                                            writeStatus=CardKeyUpdate(6,secWrite[5],27,strKeyA,strDefaultKeyB,strAccessCode,strKeyB);
////
////                                            if(writeStatus!=-1)
////                                            {
////                                                strLog+="Sector 6 Key Updated Successfully Second Time\n";
////                                            }
////                                            else
////                                            {
////                                                strLog+="Sector 6 Key Updation Failure Second Time\n";
////                                            }
////                                        }
////
////                                    }else
////                                    {
////                                        strLog+="Sector 6 Refreshed Failure Second Time\n";
////                                    }
////
////                                }
//
//                            } else {
//                                strLog += "Failed To Read To Sector 6\n";
//                            }
//
//                            break;
//
//                        case 7:
//
//                            msg = AddRead + secRead[7] + "00" + strInitialKeyA + "01";
//                            checksum = Checksum(msg);
//                            msg = msg + Integer.toHexString(checksum) + "%";
//                            if (Integer.toHexString(checksum).toUpperCase().length() != 2) {
//                                msg = msg + "0" + Integer.toHexString(checksum).toUpperCase() + "%";
//
//                            } else {
//                                msg = msg + Integer.toHexString(checksum).toUpperCase() + "%";
//                            }
//
//                            // readStatus = validateSectorRead(msg);
//
//                            if (readStatus != -1) {
//                                strLog += "Sector 7 Read Successfull With Fortuna Key\n";
//
//                                writeStatus = CardKeyUpdateInit(7, secWrite[7], 31, strInitialKeyB, strFinalKeyA, "7F0788C1", strFinalKeyB);
//                                //  writeStatus = CardKeyUpdateInit(7, secWrite[7], 31, strInitialKeyB, strFinalKeyA, "FFFFFFFF", strFinalKeyB);
//
//                                if (writeStatus != -1) {
//                                    strLog += "Sector 7 Key Updated Successfully First Time\n";
//                                } else {
//                                    strLog += "Sector 7 Key Updation Failure First Time\n";
//
//                                    writeStatus = CardKeyUpdateInit(7, secWrite[7], 31, strInitialKeyB, strFinalKeyA, "7F0788C1", strFinalKeyB);
//                                    // writeStatus = CardKeyUpdateInit(7, secWrite[7], 31, strInitialKeyB, strFinalKeyA, "FFFFFFFF", strFinalKeyB);
//
//                                    if (writeStatus != -1) {
//                                        strLog += "Sector 7 Key Updated Successfully Second Time\n";
//                                    } else {
//                                        strLog += "Sector 7 Key Updation Failure Second Time\n";
//                                    }
//
//                                }
//
//                                ///////////////////////////////////////////////////////////////////////////////
//
//                                //=====================Refresh Sector Using Factory Key======================//
//
////                                msg = AddWrite + secWrite[6] + strDefaultKeyB + "00" +strEmptyData;
////                                checksum = Checksum(msg);
////                                if(Integer.toHexString(checksum).toUpperCase().length() != 2) {
////                                    msg = msg + "0" + Integer.toHexString(checksum).toUpperCase() + "%";
////                                }else {
////                                    msg = msg + Integer.toHexString(checksum).toUpperCase() + "%";
////                                }
//
//
////                                writeStatus=SectorWrite(msg, "7");
////
////                                //=============================================================================//
////
////                                if(writeStatus!=-1)
////                                {
////                                    strLog+="Sector 7 Refreshed Successfully First Time\n";
////
////                                    writeStatus=CardKeyUpdate(7,secWrite[6],31,strKeyA,strDefaultKeyB,strAccessCode,strKeyB);
////
////                                    if(writeStatus!=-1)
////                                    {
////                                        strLog+="Sector 7 Key Updated Successfully First Time\n";
////                                    }
////                                    else
////                                    {
////                                        strLog+="Sector 7 Key Updation Failure First Time\n";
////
////                                        writeStatus=CardKeyUpdate(7,secWrite[6],31,strKeyA,strDefaultKeyB,strAccessCode,strKeyB);
////
////                                        if(writeStatus!=-1)
////                                        {
////                                            strLog+="Sector 7 Key Updated Successfully Second Time\n";
////                                        }
////                                        else
////                                        {
////                                            strLog+="Sector 7 Key Updation Failure Second Time\n";
////                                        }
////
////                                    }
////
////                                }
////                                else
////                                {
////                                    strLog+="Sector 7 Refreshed Failure First Time\n";
////
////                                    writeStatus=SectorWrite(msg, "7");
////
////                                    if(writeStatus!=-1)
////                                    {
////                                        strLog+="Sector 7 Refreshed Successfully Second Time\n";
////
////                                        writeStatus=CardKeyUpdate(7,secWrite[6],31,strKeyA,strDefaultKeyB,strAccessCode,strKeyB);
////
////                                        if(writeStatus!=-1)
////                                        {
////                                            strLog+="Sector 7 Key Updated Successfully First Time\n";
////                                        }
////                                        else
////                                        {
////                                            strLog+="Sector 7 Key Updation Failure First Time\n";
////
////                                            writeStatus=CardKeyUpdate(7,secWrite[6],31,strKeyA,strDefaultKeyB,strAccessCode,strKeyB);
////
////                                            if(writeStatus!=-1)
////                                            {
////                                                strLog+="Sector 7 Key Updated Successfully Second Time\n";
////                                            }
////                                            else
////                                            {
////                                                strLog+="Sector 7 Key Updation Failure Second Time\n";
////                                            }
////                                        }
////
////                                    }else
////                                    {
////                                        strLog+="Sector 7 Refreshed Failure Second Time\n";
////                                    }
////
////                                }
//
//                            } else {
//                                strLog += "Failed To Read To Sector 7\n";
//                            }
//
//                            break;
//
//                        case 8:
//
//                            msg = AddRead + secRead[8] + "00" + strInitialKeyA + "01";
//                            checksum = Checksum(msg);
//                            msg = msg + Integer.toHexString(checksum) + "%";
//                            if (Integer.toHexString(checksum).toUpperCase().length() != 2) {
//                                msg = msg + "0" + Integer.toHexString(checksum).toUpperCase() + "%";
//
//                            } else {
//                                msg = msg + Integer.toHexString(checksum).toUpperCase() + "%";
//                            }
//
//                            // readStatus = validateSectorRead(msg);
//
//                            if (readStatus != -1) {
//                                strLog += "Sector 8 Read Successfull With Fortuna Key\n";
//
//                                writeStatus = CardKeyUpdateInit(8, secWrite[8], 35, strInitialKeyB, strFinalKeyA, "7F0788C1", strFinalKeyB);
//
//                                // writeStatus = CardKeyUpdateInit(8, secWrite[8], 35, strInitialKeyB, strFinalKeyA, "FFFFFFFF", strFinalKeyB);
//
//                                if (writeStatus != -1) {
//                                    strLog += "Sector 8 Key Updated Successfully First Time\n";
//                                } else {
//                                    strLog += "Sector 8 Key Updation Failure First Time\n";
//
//                                    writeStatus = CardKeyUpdateInit(8, secWrite[8], 35, strInitialKeyB, strFinalKeyA, "7F0788C1", strFinalKeyB);
//
//                                    //  writeStatus = CardKeyUpdateInit(8, secWrite[8], 35, strInitialKeyB, strFinalKeyA, "FFFFFFFF", strFinalKeyB);
//
//                                    if (writeStatus != -1) {
//                                        strLog += "Sector 8 Key Updated Successfully Second Time\n";
//                                    } else {
//                                        strLog += "Sector 8 Key Updation Failure Second Time\n";
//                                    }
//
//                                }
//
//                                ////////////////////////////////////////////////////////////////////////////////
//
//
//                                //=====================Refresh Sector Using Factory Key======================//
//
////                                msg = AddWrite + secWrite[7] + strDefaultKeyB + "00" +strEmptyData;
////                                checksum = Checksum(msg);
////                                if(Integer.toHexString(checksum).toUpperCase().length() != 2) {
////                                    msg = msg + "0" + Integer.toHexString(checksum).toUpperCase() + "%";
////                                }else {
////                                    msg = msg + Integer.toHexString(checksum).toUpperCase() + "%";
////                                }
//
//
////                                writeStatus=SectorWrite(msg, "8");
////
////                                //=============================================================================//
////
////                                if(writeStatus!=-1)
////                                {
////                                    strLog+="Sector 8 Refreshed Successfully First Time\n";
////
////                                    writeStatus=CardKeyUpdate(8,secWrite[7],35,strKeyA,strDefaultKeyB,strAccessCode,strKeyB);
////
////                                    if(writeStatus!=-1)
////                                    {
////                                        strLog+="Sector 8 Key Updated Successfully First Time\n";
////                                    }
////                                    else
////                                    {
////                                        strLog+="Sector 8 Key Updation Failure First Time\n";
////
////                                        writeStatus=CardKeyUpdate(8,secWrite[7],35,strKeyA,strDefaultKeyB,strAccessCode,strKeyB);
////
////                                        if(writeStatus!=-1)
////                                        {
////                                            strLog+="Sector 8 Key Updated Successfully Second Time\n";
////                                        }
////                                        else
////                                        {
////                                            strLog+="Sector 8 Key Updation Failure Second Time\n";
////                                        }
////
////                                    }
////
////                                }
////                                else
////                                {
////                                    strLog+="Sector 8 Refreshed Failure First Time\n";
////
////                                    writeStatus=SectorWrite(msg, "8");
////
////                                    if(writeStatus!=-1)
////                                    {
////                                        strLog+="Sector 8 Refreshed Successfully Second Time\n";
////
////                                        writeStatus=CardKeyUpdate(8,secWrite[7],35,strKeyA,strDefaultKeyB,strAccessCode,strKeyB);
////
////                                        if(writeStatus!=-1)
////                                        {
////                                            strLog+="Sector 8 Key Updated Successfully First Time\n";
////                                        }
////                                        else
////                                        {
////                                            strLog+="Sector 8 Key Updation Failure First Time\n";
////
////                                            writeStatus=CardKeyUpdate(8,secWrite[7],35,strKeyA,strDefaultKeyB,strAccessCode,strKeyB);
////
////                                            if(writeStatus!=-1)
////                                            {
////                                                strLog+="Sector 8 Key Updated Successfully Second Time\n";
////                                            }
////                                            else
////                                            {
////                                                strLog+="Sector 8 Key Updation Failure Second Time\n";
////                                            }
////                                        }
////
////                                    }else
////                                    {
////                                        strLog+="Sector 8 Refreshed Failure Second Time\n";
////                                    }
////
////                                }
//
//                            } else {
//                                strLog += "Failed To Read To Sector 8\n";
//                            }
//
//                            break;
//
//
//                        case 9:
//
//                            msg = AddRead + secRead[9] + "00" + strInitialKeyA + "01";
//                            checksum = Checksum(msg);
//                            msg = msg + Integer.toHexString(checksum) + "%";
//                            if (Integer.toHexString(checksum).toUpperCase().length() != 2) {
//                                msg = msg + "0" + Integer.toHexString(checksum).toUpperCase() + "%";
//
//                            } else {
//                                msg = msg + Integer.toHexString(checksum).toUpperCase() + "%";
//                            }
//
//                            // readStatus = validateSectorRead(msg);
//
//                            if (readStatus != -1) {
//                                strLog += "Sector 9 Read Successfull With Fortuna Key\n";
//
//                                writeStatus = CardKeyUpdateInit(9, secWrite[9], 39, strInitialKeyB, strFinalKeyA, "7F0788C1", strFinalKeyB);
//
//                                // writeStatus = CardKeyUpdateInit(9, secWrite[9], 39, strInitialKeyB, strFinalKeyA, "FFFFFFFF", strFinalKeyB);
//
//
//                                if (writeStatus != -1) {
//                                    strLog += "Sector 9 Key Updated Successfully First Time\n";
//                                } else {
//                                    strLog += "Sector 9 Key Updation Failure First Time\n";
//
//                                    writeStatus = CardKeyUpdateInit(9, secWrite[9], 39, strInitialKeyB, strFinalKeyA, "7F0788C1", strFinalKeyB);
//
//                                    //  writeStatus = CardKeyUpdateInit(9, secWrite[9], 39, strInitialKeyB, strFinalKeyA, "FFFFFFFF", strFinalKeyB);
//
//
//                                    if (writeStatus != -1) {
//                                        strLog += "Sector 9 Key Updated Successfully Second Time\n";
//                                    } else {
//                                        strLog += "Sector 9 Key Updation Failure Second Time\n";
//                                    }
//
//                                }
//
//
//                                /////////////////////////////////////////////////////////////////////////////////
//
//                                //=====================Refresh Sector Using Factory Key======================//
//
////                                msg = AddWrite + secWrite[8] + strDefaultKeyB + "00" +strEmptyData;
////                                checksum = Checksum(msg);
////                                if(Integer.toHexString(checksum).toUpperCase().length() != 2) {
////                                    msg = msg + "0" + Integer.toHexString(checksum).toUpperCase() + "%";
////                                }else {
////                                    msg = msg + Integer.toHexString(checksum).toUpperCase() + "%";
////                                }
//
////
////                                writeStatus=SectorWrite(msg, "9");
////
////                                //=============================================================================//
////
////                                if(writeStatus!=-1)
////                                {
////                                    strLog+="Sector 9 Refreshed Successfully First Time\n";
////
////                                    writeStatus=CardKeyUpdate(9,secWrite[8],39,strKeyA,strDefaultKeyB,strAccessCode,strKeyB);
////
////                                    if(writeStatus!=-1)
////                                    {
////                                        strLog+="Sector 9 Key Updated Successfully First Time\n";
////                                    }
////                                    else
////                                    {
////                                        strLog+="Sector 9 Key Updation Failure First Time\n";
////
////                                        writeStatus=CardKeyUpdate(9,secWrite[8],39,strKeyA,strDefaultKeyB,strAccessCode,strKeyB);
////
////                                        if(writeStatus!=-1)
////                                        {
////                                            strLog+="Sector 9 Key Updated Successfully Second Time\n";
////                                        }
////                                        else
////                                        {
////                                            strLog+="Sector 9 Key Updation Failure Second Time\n";
////                                        }
////
////                                    }
////
////                                }
////                                else
////                                {
////                                    strLog+="Sector 9 Refreshed Failure First Time\n";
////
////                                    writeStatus=SectorWrite(msg, "9");
////
////                                    if(writeStatus!=-1)
////                                    {
////                                        strLog+="Sector 9 Refreshed Successfully Second Time\n";
////
////                                        writeStatus=CardKeyUpdate(9,secWrite[8],39,strKeyA,strDefaultKeyB,strAccessCode,strKeyB);
////
////                                        if(writeStatus!=-1)
////                                        {
////                                            strLog+="Sector 9 Key Updated Successfully First Time\n";
////                                        }
////                                        else
////                                        {
////                                            strLog+="Sector 9 Key Updation Failure First Time\n";
////
////                                            writeStatus=CardKeyUpdate(9,secWrite[8],39,strKeyA,strDefaultKeyB,strAccessCode,strKeyB);
////
////                                            if(writeStatus!=-1)
////                                            {
////                                                strLog+="Sector 9 Key Updated Successfully Second Time\n";
////                                            }
////                                            else
////                                            {
////                                                strLog+="Sector 9 Key Updation Failure Second Time\n";
////                                            }
////                                        }
////
////                                    }else
////                                    {
////                                        strLog+="Sector 9 Refreshed Failure Second Time\n";
////                                    }
////
////                                }
//
//                            } else {
//                                strLog += "Failed To Read To Sector 9\n";
//                            }
//
//                            break;
//
//
//                        case 10:
//
//                            msg = AddRead + secRead[10] + "00" + strInitialKeyA + "01";
//                            checksum = Checksum(msg);
//                            msg = msg + Integer.toHexString(checksum) + "%";
//                            if (Integer.toHexString(checksum).toUpperCase().length() != 2) {
//                                msg = msg + "0" + Integer.toHexString(checksum).toUpperCase() + "%";
//
//                            } else {
//                                msg = msg + Integer.toHexString(checksum).toUpperCase() + "%";
//                            }
//
//                            // readStatus = validateSectorRead(msg);
//
//                            if (readStatus != -1) {
//                                strLog += "Sector 10 Read Successfull With Fortuna Key\n";
//
//                                writeStatus = CardKeyUpdateInit(10, secWrite[10], 43, strInitialKeyB, strFinalKeyA, "7F0788C1", strFinalKeyB);
//                                // writeStatus = CardKeyUpdateInit(10, secWrite[10], 43, strInitialKeyB, strFinalKeyA, "FFFFFFFF", strFinalKeyB);
//
//                                if (writeStatus != -1) {
//                                    strLog += "Sector 10 Key Updated Successfully First Time\n";
//                                } else {
//                                    strLog += "Sector 10 Key Updation Failure First Time\n";
//
//                                    writeStatus = CardKeyUpdateInit(10, secWrite[10], 43, strInitialKeyB, strFinalKeyA, "7F0788C1", strFinalKeyB);
//
//                                    // writeStatus = CardKeyUpdateInit(10, secWrite[10], 43, strInitialKeyB, strFinalKeyA, "FFFFFFFF", strFinalKeyB);
//
//                                    if (writeStatus != -1) {
//                                        strLog += "Sector 10 Key Updated Successfully Second Time\n";
//                                    } else {
//                                        strLog += "Sector 10 Key Updation Failure Second Time\n";
//                                    }
//
//                                }
//
//                                ////////////////////////////////////////////////////////////////////////
//
//                                //=====================Refresh Sector Using Factory Key======================//
//
////                                msg = AddWrite + secWrite[9] + strDefaultKeyB + "00" +strEmptyData;
////                                checksum = Checksum(msg);
////                                if(Integer.toHexString(checksum).toUpperCase().length() != 2) {
////                                    msg = msg + "0" + Integer.toHexString(checksum).toUpperCase() + "%";
////                                }else {
////                                    msg = msg + Integer.toHexString(checksum).toUpperCase() + "%";
////                                }
////
////                                writeStatus=SectorWrite(msg, "10");
////
////                                //=============================================================================//
////
////                                if(writeStatus!=-1)
////                                {
////                                    strLog+="Sector 10 Refreshed Successfully First Time\n";
////
////                                    writeStatus=CardKeyUpdate(10,secWrite[9],43,strKeyA,strDefaultKeyB,strAccessCode,strKeyB);
////
////                                    if(writeStatus!=-1)
////                                    {
////                                        strLog+="Sector 10 Key Updated Successfully First Time\n";
////                                    }
////                                    else
////                                    {
////                                        strLog+="Sector 10 Key Updation Failure First Time\n";
////
////                                        writeStatus=CardKeyUpdate(10,secWrite[9],43,strKeyA,strDefaultKeyB,strAccessCode,strKeyB);
////
////                                        if(writeStatus!=-1)
////                                        {
////                                            strLog+="Sector 10 Key Updated Successfully Second Time\n";
////                                        }
////                                        else
////                                        {
////                                            strLog+="Sector 10 Key Updation Failure Second Time\n";
////                                        }
////
////                                    }
////
////                                }
////                                else
////                                {
////                                    strLog+="Sector 10 Refreshed Failure First Time\n";
////
////                                    writeStatus=SectorWrite(msg, "10");
////
////                                    if(writeStatus!=-1)
////                                    {
////                                        strLog+="Sector 10 Refreshed Successfully Second Time\n";
////
////                                        writeStatus=CardKeyUpdate(10,secWrite[9],43,strKeyA,strDefaultKeyB,strAccessCode,strKeyB);
////
////                                        if(writeStatus!=-1)
////                                        {
////                                            strLog+="Sector 10 Key Updated Successfully First Time\n";
////                                        }
////                                        else
////                                        {
////                                            strLog+="Sector 10 Key Updation Failure First Time\n";
////
////                                            writeStatus=CardKeyUpdate(10,secWrite[9],43,strKeyA,strDefaultKeyB,strAccessCode,strKeyB);
////
////                                            if(writeStatus!=-1)
////                                            {
////                                                strLog+="Sector 10 Key Updated Successfully Second Time\n";
////                                            }
////                                            else
////                                            {
////                                                strLog+="Sector 10 Key Updation Failure Second Time\n";
////                                            }
////                                        }
////
////                                    }else
////                                    {
////                                        strLog+="Sector 10 Refreshed Failure Second Time\n";
////                                    }
////
////                                }
//
//                            } else {
//                                strLog += "Failed To Read To Sector 10\n";
//                            }
//
//                            break;
//
//
//                        case 11:
//
//                            msg = AddRead + secRead[11] + "00" + strInitialKeyA + "01";
//                            checksum = Checksum(msg);
//                            msg = msg + Integer.toHexString(checksum) + "%";
//                            if (Integer.toHexString(checksum).toUpperCase().length() != 2) {
//                                msg = msg + "0" + Integer.toHexString(checksum).toUpperCase() + "%";
//
//                            } else {
//                                msg = msg + Integer.toHexString(checksum).toUpperCase() + "%";
//                            }
//
//                            // readStatus = validateSectorRead(msg);
//
//                            if (readStatus != -1) {
//                                strLog += "Sector 11 Read Successfull With Fortuna Key\n";
//
//                                writeStatus = CardKeyUpdateInit(11, secWrite[11], 47, strInitialKeyB, strFinalKeyA, "7F0788C1", strFinalKeyB);
//
//                                // writeStatus = CardKeyUpdateInit(11, secWrite[11], 47, strInitialKeyB, strFinalKeyA, "FFFFFFFF", strFinalKeyB);
//
//                                if (writeStatus != -1) {
//                                    strLog += "Sector 11 Key Updated Successfully First Time\n";
//                                } else {
//                                    strLog += "Sector 11 Key Updation Failure First Time\n";
//
//                                    writeStatus = CardKeyUpdateInit(11, secWrite[11], 47, strInitialKeyB, strFinalKeyA, "7F0788C1", strFinalKeyB);
//
//                                    //  writeStatus = CardKeyUpdateInit(11, secWrite[11], 47, strInitialKeyB, strFinalKeyA, "FFFFFFFF", strFinalKeyB);
//                                    if (writeStatus != -1) {
//                                        strLog += "Sector 11 Key Updated Successfully Second Time\n";
//                                    } else {
//                                        strLog += "Sector 11 Key Updation Failure Second Time\n";
//                                    }
//
//                                }
//
//                                ///////////////////////////////////////////////////////////////////////////
//
//                                //=====================Refresh Sector Using Factory Key======================//
//
////                                msg = AddWrite + secWrite[10] + strDefaultKeyB + "00" +strEmptyData;
////                                checksum = Checksum(msg);
////                                if(Integer.toHexString(checksum).toUpperCase().length() != 2) {
////                                    msg = msg + "0" + Integer.toHexString(checksum).toUpperCase() + "%";
////                                }else {
////                                    msg = msg + Integer.toHexString(checksum).toUpperCase() + "%";
////                                }
////
////                                writeStatus=SectorWrite(msg, "11");
////
////                                //=============================================================================//
////
////                                if(writeStatus!=-1)
////                                {
////                                    strLog+="Sector 11 Refreshed Successfully First Time\n";
////
////                                    writeStatus=CardKeyUpdate(11,secWrite[10],47,strKeyA,strDefaultKeyB,strAccessCode,strKeyB);
////
////                                    if(writeStatus!=-1)
////                                    {
////                                        strLog+="Sector 11 Key Updated Successfully First Time\n";
////                                    }
////                                    else
////                                    {
////                                        strLog+="Sector 11 Key Updation Failure First Time\n";
////
////                                        writeStatus=CardKeyUpdate(11,secWrite[10],47,strKeyA,strDefaultKeyB,strAccessCode,strKeyB);
////
////                                        if(writeStatus!=-1)
////                                        {
////                                            strLog+="Sector 11 Key Updated Successfully Second Time\n";
////                                        }
////                                        else
////                                        {
////                                            strLog+="Sector 11 Key Updation Failure Second Time\n";
////                                        }
////
////                                    }
////
////                                }
////                                else
////                                {
////                                    strLog+="Sector 11 Refreshed Failure First Time\n";
////
////                                    writeStatus=SectorWrite(msg, "11");
////
////                                    if(writeStatus!=-1)
////                                    {
////                                        strLog+="Sector 11 Refreshed Successfully Second Time\n";
////
////                                        writeStatus=CardKeyUpdate(11,secWrite[10],47,strKeyA,strDefaultKeyB,strAccessCode,strKeyB);
////
////                                        if(writeStatus!=-1)
////                                        {
////                                            strLog+="Sector 11 Key Updated Successfully First Time\n";
////                                        }
////                                        else
////                                        {
////                                            strLog+="Sector 11 Key Updation Failure First Time\n";
////
////                                            writeStatus=CardKeyUpdate(11,secWrite[10],47,strKeyA,strDefaultKeyB,strAccessCode,strKeyB);
////
////                                            if(writeStatus!=-1)
////                                            {
////                                                strLog+="Sector 11 Key Updated Successfully Second Time\n";
////                                            }
////                                            else
////                                            {
////                                                strLog+="Sector 11 Key Updation Failure Second Time\n";
////                                            }
////                                        }
////
////                                    }else
////                                    {
////                                        strLog+="Sector 11 Refreshed Failure Second Time\n";
////                                    }
////
////                                }
//
//                            } else {
//                                strLog += "Failed To Read To Sector 11\n";
//                            }
//
//                            break;
//
//
//                        case 12:
//
//                            msg = AddRead + secRead[12] + "00" + strInitialKeyA + "01";
//                            checksum = Checksum(msg);
//                            msg = msg + Integer.toHexString(checksum) + "%";
//                            if (Integer.toHexString(checksum).toUpperCase().length() != 2) {
//                                msg = msg + "0" + Integer.toHexString(checksum).toUpperCase() + "%";
//
//                            } else {
//                                msg = msg + Integer.toHexString(checksum).toUpperCase() + "%";
//                            }
//
//                            //  readStatus = validateSectorRead(msg);
//
//                            if (readStatus != -1) {
//                                strLog += "Sector 12 Read Successfull With Fortuna Key\n";
//
//                                writeStatus = CardKeyUpdateInit(12, secWrite[12], 51, strInitialKeyB, strFinalKeyA, "7F0788C1", strFinalKeyB);
//
//                                // writeStatus = CardKeyUpdateInit(12, secWrite[12], 51, strInitialKeyB, strFinalKeyA, "FFFFFFFF", strFinalKeyB);
//
//                                if (writeStatus != -1) {
//                                    strLog += "Sector 12 Key Updated Successfully First Time\n";
//                                } else {
//                                    strLog += "Sector 12 Key Updation Failure First Time\n";
//
//                                    writeStatus = CardKeyUpdateInit(12, secWrite[12], 51, strInitialKeyB, strFinalKeyA, "7F0788C1", strFinalKeyB);
//                                    // writeStatus = CardKeyUpdateInit(12, secWrite[12], 51, strInitialKeyB, strFinalKeyA, "FFFFFFFF", strFinalKeyB);
//
//                                    if (writeStatus != -1) {
//                                        strLog += "Sector 12 Key Updated Successfully Second Time\n";
//                                    } else {
//                                        strLog += "Sector 12 Key Updation Failure Second Time\n";
//                                    }
//
//                                }
//
//
//                                ////////////////////////////////////////////////////////////////////////
//
//
//                                //=====================Refresh Sector Using Factory Key======================//
//
////                                msg = AddWrite + secWrite[11] + strDefaultKeyB + "00" +strEmptyData;
////                                checksum = Checksum(msg);
////                                if(Integer.toHexString(checksum).toUpperCase().length() != 2) {
////                                    msg = msg + "0" + Integer.toHexString(checksum).toUpperCase() + "%";
////                                }else {
////                                    msg = msg + Integer.toHexString(checksum).toUpperCase() + "%";
////                                }
////
////                                writeStatus=SectorWrite(msg, "12");
////
////                                //=============================================================================//
////
////                                if(writeStatus!=-1)
////                                {
////                                    strLog+="Sector 12 Refreshed Successfully First Time\n";
////
////                                    writeStatus=CardKeyUpdate(12,secWrite[11],51,strKeyA,strDefaultKeyB,strAccessCode,strKeyB);
////
////                                    if(writeStatus!=-1)
////                                    {
////                                        strLog+="Sector 12 Key Updated Successfully First Time\n";
////                                    }
////                                    else
////                                    {
////                                        strLog+="Sector 12 Key Updation Failure First Time\n";
////
////                                        writeStatus=CardKeyUpdate(12,secWrite[11],51,strKeyA,strDefaultKeyB,strAccessCode,strKeyB);
////
////                                        if(writeStatus!=-1)
////                                        {
////                                            strLog+="Sector 12 Key Updated Successfully Second Time\n";
////                                        }
////                                        else
////                                        {
////                                            strLog+="Sector 12 Key Updation Failure Second Time\n";
////                                        }
////
////                                    }
////
////                                }
////                                else
////                                {
////                                    strLog+="Sector 12 Refreshed Failure First Time\n";
////
////                                    writeStatus=SectorWrite(msg, "12");
////
////                                    if(writeStatus!=-1)
////                                    {
////                                        strLog+="Sector 12 Refreshed Successfully Second Time\n";
////
////                                        writeStatus=CardKeyUpdate(12,secWrite[11],51,strKeyA,strDefaultKeyB,strAccessCode,strKeyB);
////
////                                        if(writeStatus!=-1)
////                                        {
////                                            strLog+="Sector 12 Key Updated Successfully First Time\n";
////                                        }
////                                        else
////                                        {
////                                            strLog+="Sector 12 Key Updation Failure First Time\n";
////
////                                            writeStatus=CardKeyUpdate(12,secWrite[11],51,strKeyA,strDefaultKeyB,strAccessCode,strKeyB);
////
////                                            if(writeStatus!=-1)
////                                            {
////                                                strLog+="Sector 12 Key Updated Successfully Second Time\n";
////                                            }
////                                            else
////                                            {
////                                                strLog+="Sector 12 Key Updation Failure Second Time\n";
////                                            }
////                                        }
////
////                                    }else
////                                    {
////                                        strLog+="Sector 12 Refreshed Failure Second Time\n";
////                                    }
////
////                                }
//
//                            } else {
//                                strLog += "Failed To Read To Sector 12\n";
//                            }
//
//                            break;
//
//                        case 13:
//
//                            msg = AddRead + secRead[13] + "00" + strInitialKeyA + "01";
//                            checksum = Checksum(msg);
//                            msg = msg + Integer.toHexString(checksum) + "%";
//                            if (Integer.toHexString(checksum).toUpperCase().length() != 2) {
//                                msg = msg + "0" + Integer.toHexString(checksum).toUpperCase() + "%";
//
//                            } else {
//                                msg = msg + Integer.toHexString(checksum).toUpperCase() + "%";
//                            }
//
//                            // readStatus = validateSectorRead(msg);
//
//                            if (readStatus != -1) {
//                                strLog += "Sector 13 Read Successfull With Fortuna Key\n";
//
//                                writeStatus = CardKeyUpdateInit(13, secWrite[13], 55, strInitialKeyB, strFinalKeyA, "7F0788C1", strFinalKeyB);
//                                // writeStatus = CardKeyUpdateInit(13, secWrite[13], 55, strInitialKeyB, strFinalKeyA, "FFFFFFFF", strFinalKeyB);
//
//                                if (writeStatus != -1) {
//                                    strLog += "Sector 13 Key Updated Successfully First Time\n";
//                                } else {
//                                    strLog += "Sector 13 Key Updation Failure First Time\n";
//
//                                    writeStatus = CardKeyUpdateInit(13, secWrite[13], 55, strInitialKeyB, strFinalKeyA, "7F0788C1", strFinalKeyB);
//
//                                    // writeStatus = CardKeyUpdateInit(13, secWrite[13], 55, strInitialKeyB, strFinalKeyA, "FFFFFFFF", strFinalKeyB);
//
//                                    if (writeStatus != -1) {
//                                        strLog += "Sector 13 Key Updated Successfully Second Time\n";
//                                    } else {
//                                        strLog += "Sector 13 Key Updation Failure Second Time\n";
//                                    }
//
//                                }
//
//                                ///////////////////////////////////////////////////////////////////////////////
//
//
//                                //=====================Refresh Sector Using Factory Key======================//
//
////                                msg = AddWrite + secWrite[12] + strDefaultKeyB + "00" +strEmptyData;
////                                checksum = Checksum(msg);
////                                if(Integer.toHexString(checksum).toUpperCase().length() != 2) {
////                                    msg = msg + "0" + Integer.toHexString(checksum).toUpperCase() + "%";
////                                }else {
////                                    msg = msg + Integer.toHexString(checksum).toUpperCase() + "%";
////                                }
////
////                                writeStatus=SectorWrite(msg, "13");
////
////                                //=============================================================================//
////
////                                if(writeStatus!=-1)
////                                {
////                                    strLog+="Sector 13 Refreshed Successfully First Time\n";
////
////                                    writeStatus=CardKeyUpdate(13,secWrite[12],55,strKeyA,strDefaultKeyB,strAccessCode,strKeyB);
////
////                                    if(writeStatus!=-1)
////                                    {
////                                        strLog+="Sector 13 Key Updated Successfully First Time\n";
////                                    }
////                                    else
////                                    {
////                                        strLog+="Sector 13 Key Updation Failure First Time\n";
////
////                                        writeStatus=CardKeyUpdate(13,secWrite[12],55,strKeyA,strDefaultKeyB,strAccessCode,strKeyB);
////
////                                        if(writeStatus!=-1)
////                                        {
////                                            strLog+="Sector 13 Key Updated Successfully Second Time\n";
////                                        }
////                                        else
////                                        {
////                                            strLog+="Sector 13 Key Updation Failure Second Time\n";
////                                        }
////
////                                    }
////
////                                }
////                                else
////                                {
////                                    strLog+="Sector 13 Refreshed Failure First Time\n";
////
////                                    writeStatus=SectorWrite(msg, "13");
////
////                                    if(writeStatus!=-1)
////                                    {
////                                        strLog+="Sector 13 Refreshed Successfully Second Time\n";
////
////                                        writeStatus=CardKeyUpdate(13,secWrite[12],55,strKeyA,strDefaultKeyB,strAccessCode,strKeyB);
////
////                                        if(writeStatus!=-1)
////                                        {
////                                            strLog+="Sector 13 Key Updated Successfully First Time\n";
////                                        }
////                                        else
////                                        {
////                                            strLog+="Sector 13 Key Updation Failure First Time\n";
////
////                                            writeStatus=CardKeyUpdate(13,secWrite[12],55,strKeyA,strDefaultKeyB,strAccessCode,strKeyB);
////
////                                            if(writeStatus!=-1)
////                                            {
////                                                strLog+="Sector 13 Key Updated Successfully Second Time\n";
////                                            }
////                                            else
////                                            {
////                                                strLog+="Sector 13 Key Updation Failure Second Time\n";
////                                            }
////                                        }
////
////                                    }else
////                                    {
////                                        strLog+="Sector 13 Refreshed Failure Second Time\n";
////                                    }
////
////                                }
//
//                            } else {
//                                strLog += "Failed To Read To Sector 13\n";
//                            }
//
//                            break;
//
//                        case 14:
//
//                            msg = AddRead + secRead[14] + "00" + strInitialKeyA + "01";
//                            checksum = Checksum(msg);
//                            msg = msg + Integer.toHexString(checksum) + "%";
//                            if (Integer.toHexString(checksum).toUpperCase().length() != 2) {
//                                msg = msg + "0" + Integer.toHexString(checksum).toUpperCase() + "%";
//
//                            } else {
//                                msg = msg + Integer.toHexString(checksum).toUpperCase() + "%";
//                            }
//
//                            // readStatus = validateSectorRead(msg);
//
//                            if (readStatus != -1) {
//                                strLog += "Sector 14 Read Successfull With Fortuna Key\n";
//
//                                writeStatus = CardKeyUpdateInit(14, secWrite[14], 59, strInitialKeyB, strFinalKeyA, "7F0788C1", strFinalKeyB);
//
//                                // writeStatus = CardKeyUpdateInit(14, secWrite[14], 59, strInitialKeyB, strFinalKeyA, "FFFFFFFF", strFinalKeyB);
//
//                                if (writeStatus != -1) {
//                                    strLog += "Sector 14 Key Updated Successfully First Time\n";
//                                } else {
//                                    strLog += "Sector 14 Key Updation Failure First Time\n";
//
//                                    writeStatus = CardKeyUpdateInit(14, secWrite[14], 59, strInitialKeyB, strFinalKeyA, "7F0788C1", strFinalKeyB);
//                                    // writeStatus = CardKeyUpdateInit(14, secWrite[14], 59, strInitialKeyB, strFinalKeyA, "FFFFFFFF", strFinalKeyB);
//
//                                    if (writeStatus != -1) {
//                                        strLog += "Sector 14 Key Updated Successfully Second Time\n";
//                                    } else {
//                                        strLog += "Sector 14 Key Updation Failure Second Time\n";
//                                    }
//
//                                }
//
//
//                                ///////////////////////////////////////////////////////////////////////////////
//
//
//                                //=====================Refresh Sector Using Factory Key======================//
//
////                                msg = AddWrite + secWrite[13] + strDefaultKeyB + "00" +strEmptyData;
////                                checksum = Checksum(msg);
////                                if(Integer.toHexString(checksum).toUpperCase().length() != 2) {
////                                    msg = msg + "0" + Integer.toHexString(checksum).toUpperCase() + "%";
////                                }else {
////                                    msg = msg + Integer.toHexString(checksum).toUpperCase() + "%";
////                                }
////
////                                writeStatus=SectorWrite(msg, "14");
////
////                                //=============================================================================//
////
////                                if(writeStatus!=-1)
////                                {
////                                    strLog+="Sector 14 Refreshed Successfully First Time\n";
////
////                                    writeStatus=CardKeyUpdate(14,secWrite[13],59,strKeyA,strDefaultKeyB,strAccessCode,strKeyB);
////
////                                    if(writeStatus!=-1)
////                                    {
////                                        strLog+="Sector 14 Key Updated Successfully First Time\n";
////                                    }
////                                    else
////                                    {
////                                        strLog+="Sector 14 Key Updation Failure First Time\n";
////
////                                        writeStatus=CardKeyUpdate(14,secWrite[13],59,strKeyA,strDefaultKeyB,strAccessCode,strKeyB);
////
////                                        if(writeStatus!=-1)
////                                        {
////                                            strLog+="Sector 14 Key Updated Successfully Second Time\n";
////                                        }
////                                        else
////                                        {
////                                            strLog+="Sector 14 Key Updation Failure Second Time\n";
////                                        }
////
////                                    }
////
////                                }
////                                else
////                                {
////                                    strLog+="Sector 14 Refreshed Failure First Time\n";
////
////                                    writeStatus=SectorWrite(msg, "14");
////
////                                    if(writeStatus!=-1)
////                                    {
////                                        strLog+="Sector 14 Refreshed Successfully Second Time\n";
////
////                                        writeStatus=CardKeyUpdate(14,secWrite[13],59,strKeyA,strDefaultKeyB,strAccessCode,strKeyB);
////
////                                        if(writeStatus!=-1)
////                                        {
////                                            strLog+="Sector 14 Key Updated Successfully First Time\n";
////                                        }
////                                        else
////                                        {
////                                            strLog+="Sector 14 Key Updation Failure First Time\n";
////
////                                            writeStatus=CardKeyUpdate(14,secWrite[13],59,strKeyA,strDefaultKeyB,strAccessCode,strKeyB);
////
////                                            if(writeStatus!=-1)
////                                            {
////                                                strLog+="Sector 14 Key Updated Successfully Second Time\n";
////                                            }
////                                            else
////                                            {
////                                                strLog+="Sector 14 Key Updation Failure Second Time\n";
////                                            }
////                                        }
////
////                                    }else
////                                    {
////                                        strLog+="Sector 14 Refreshed Failure Second Time\n";
////                                    }
////
////                                }
//
//                            } else {
//                                strLog += "Failed To Read To Sector 14\n";
//                            }
//
//                            break;
//
//                        case 15:
//
//                            msg = AddRead + secRead[15] + "00" + strInitialKeyA + "01";
//                            checksum = Checksum(msg);
//                            msg = msg + Integer.toHexString(checksum) + "%";
//                            if (Integer.toHexString(checksum).toUpperCase().length() != 2) {
//                                msg = msg + "0" + Integer.toHexString(checksum).toUpperCase() + "%";
//
//                            } else {
//                                msg = msg + Integer.toHexString(checksum).toUpperCase() + "%";
//                            }
//
//                            //readStatus = validateSectorRead(msg);
//
//                            if (readStatus != -1) {
//                                strLog += "Sector 15 Read Successfull With Fortuna Key\n";
//
//                                writeStatus = CardKeyUpdateInit(15, secWrite[15], 63, strInitialKeyB, strFinalKeyA, "7F0788C1", strFinalKeyB);
//                                // writeStatus = CardKeyUpdateInit(15, secWrite[15], 63, strInitialKeyB, strFinalKeyA, "FFFFFFFF", strFinalKeyB);
//
//                                if (writeStatus != -1) {
//                                    strLog += "Sector 15 Key Updated Successfully First Time\n";
//                                } else {
//                                    strLog += "Sector 15 Key Updation Failure First Time\n";
//
//                                    writeStatus = CardKeyUpdateInit(15, secWrite[15], 63, strInitialKeyB, strFinalKeyA, "7F0788C1", strFinalKeyB);
//
//                                    // writeStatus = CardKeyUpdateInit(15, secWrite[15], 63, strInitialKeyB, strFinalKeyA, "FFFFFFFF", strFinalKeyB);
//
//                                    if (writeStatus != -1) {
//                                        strLog += "Sector 15 Key Updated Successfully Second Time\n";
//                                    } else {
//                                        strLog += "Sector 15 Key Updation Failure Second Time\n";
//                                    }
//
//                                }
//
//                                ///////////////////////////////////////////////////////////////////////////////
//
//                                //=====================Refresh Sector Using Factory Key======================//
//
////                                msg = AddWrite + secWrite[14] + strDefaultKeyB + "00" +strEmptyData;
////                                checksum = Checksum(msg);
////                                if(Integer.toHexString(checksum).toUpperCase().length() != 2) {
////                                    msg = msg + "0" + Integer.toHexString(checksum).toUpperCase() + "%";
////                                }else {
////                                    msg = msg + Integer.toHexString(checksum).toUpperCase() + "%";
////                                }
////
////                                writeStatus=SectorWrite(msg, "15");
////
////                                //=============================================================================//
////
////                                if(writeStatus!=-1)
////                                {
////                                    strLog+="Sector 14 Refreshed Successfully First Time\n";
////
////                                    writeStatus=CardKeyUpdate(15,secWrite[14],63,strKeyA,strDefaultKeyB,strAccessCode,strKeyB);
////
////                                    if(writeStatus!=-1)
////                                    {
////                                        strLog+="Sector 14 Key Updated Successfully First Time\n";
////                                    }
////                                    else
////                                    {
////                                        strLog+="Sector 14 Key Updation Failure First Time\n";
////
////                                        writeStatus=CardKeyUpdate(15,secWrite[14],63,strKeyA,strDefaultKeyB,strAccessCode,strKeyB);
////
////                                        if(writeStatus!=-1)
////                                        {
////                                            strLog+="Sector 14 Key Updated Successfully Second Time\n";
////                                        }
////                                        else
////                                        {
////                                            strLog+="Sector 14 Key Updation Failure Second Time\n";
////                                        }
////
////                                    }
////
////                                }
////                                else
////                                {
////                                    strLog+="Sector 14 Refreshed Failure First Time\n";
////
////                                    writeStatus=SectorWrite(msg, "14");
////
////                                    if(writeStatus!=-1)
////                                    {
////                                        strLog+="Sector 14 Refreshed Successfully Second Time\n";
////
////                                        writeStatus=CardKeyUpdate(15,secWrite[14],63,strKeyA,strDefaultKeyB,strAccessCode,strKeyB);
////
////                                        if(writeStatus!=-1)
////                                        {
////                                            strLog+="Sector 14 Key Updated Successfully First Time\n";
////                                        }
////                                        else
////                                        {
////                                            strLog+="Sector 14 Key Updation Failure First Time\n";
////
////                                            writeStatus=CardKeyUpdate(15,secWrite[14],63,strKeyA,strDefaultKeyB,strAccessCode,strKeyB);
////
////                                            if(writeStatus!=-1)
////                                            {
////                                                strLog+="Sector 14 Key Updated Successfully Second Time\n";
////                                            }
////                                            else
////                                            {
////                                                strLog+="Sector 14 Key Updation Failure Second Time\n";
////                                            }
////                                        }
////
////                                    }else
////                                    {
////                                        strLog+="Sector 14 Refreshed Failure Second Time\n";
////                                    }
////
////                                }
//
//                            } else {
//                                strLog += "Failed To Read To Sector 15\n";
//                            }
//
//                            break;
//
//                        default:
//
//                            break;
//                    }
//                }
//
//
//                //===================================  Update MAD Blocks  =================================//
//
//                String strSectorNo = "01A001";
//
//                int blockno1 = 01;
//                int blockno2 = 02;
//
//
//                String strSec_0_Block_1 = "00000000000000000000000000000000";
//
//                String strSec_0_Block_2 = "00000000000000000000000000000000";
//
//
//                madUpdate(0, strSectorNo, blockno1, "FFFFFFFFFFFF", strSec_0_Block_1);
//
//                madUpdate(0, strSectorNo, blockno2, "FFFFFFFFFFFF", strSec_0_Block_2);
//
//
//                //======================================================================================//
//
//
//                //===================================Change Card Id======================================//
//
////                Log.d("TEST","Card Id:"+strCardId);
////
////
////                strNewCardId=Utility.paddCardId(strNewCardId);
////
////                String strHexCardId=asciiToHex(strNewCardId);
//
//                String strSector_1_Block_1 = "00000000000000000000000000000000";
//                String strSector_1_Block_2 = "00000000000000000000000000000000";
//                String strSector_1_Block_3 = "00000000000000000000000000000000";
//
//
//                // blockWrite(1, "01A101", 04, "FFFFFFFFFFFF", strSector_1_Block_1);
//
//                //  blockWrite(1, "01A101", 05, "FFFFFFFFFFFF", strSector_1_Block_2);
//
//                //blockWrite(1, "01A101", 06, "FFFFFFFFFFFF", strSector_1_Block_3);
//
//
//                //========================================================================================//
//
//
//            }
//
//            // checkCardPresent();
//            return null;
//
//
//        }
//
//        @Override
//        protected void onPostExecute(Void result) {
//            mypDialog.cancel();
//
//            Log.d("TEST", "Log Details:\n" + strLog);
//
//
////            if(dataWriteStatus!=-1 && keyWriteStatus!=-1)
////            {
////                showCustomAlertDialog(true, "Card Write Status", "Card Written And Key Updated Successfully");
////
////            }
////            else
////            {
////                showCustomAlertDialog(true, "Card Write Status","Failed To Write To Card");
////                Log.d("TEST", "Error Log Details:\n"+strLog);
////            }
//
//            //clear();
//
//        }
//
//    }


}
