package com.android.fortunaattendancesystem.activities;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.android.fortunaattendancesystem.R;
import com.android.fortunaattendancesystem.adapter.DrawerItem;
import com.android.fortunaattendancesystem.adapter.DrawerItemCustomAdapter;
import com.android.fortunaattendancesystem.constant.Constants;
import com.android.fortunaattendancesystem.forlinx.ForlinxGPIO;
import com.android.fortunaattendancesystem.helper.Utility;
import com.android.fortunaattendancesystem.info.ProcessInfo;
import com.android.fortunaattendancesystem.model.ExcelColumnInfo;
import com.android.fortunaattendancesystem.singleton.RC632ReaderConnection;
import com.android.fortunaattendancesystem.singleton.Settings;
import com.android.fortunaattendancesystem.singleton.SmartReaderConnection;
import com.android.fortunaattendancesystem.singleton.UserDetails;
import com.android.fortunaattendancesystem.submodules.ExcelCommunicator;
import com.android.fortunaattendancesystem.submodules.ForlinxGPIOCommunicator;
import com.android.fortunaattendancesystem.submodules.I2CCommunicator;
import com.android.fortunaattendancesystem.submodules.SQLiteCommunicator;
import com.android.fortunaattendancesystem.usbconnection.USBConnectionCreator;
import com.forlinx.android.GetValueService;
import com.forlinx.android.HardwareInterface;
import com.friendlyarm.SmartReader.SmartFinger;
import com.morpho.morphosmart.sdk.MorphoDatabase;
import com.morpho.morphosmart.sdk.MorphoDevice;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;


public class HomeActivity extends USBConnectionCreator {

    private static final int WRITE_REQUEST_CODE = 43;
    private static final int EDIT_REQUEST_CODE = 44;

    private ImageView smart_reader, finger_reader;
    private ListView mDrawerList;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerItemCustomAdapter adapter;
    private DrawerItem[] drawerItem = null;

    private TextView date, time, recordCount, serverConfig, msg;
    private ViewFlipper viewFlipper;

    int gallery_grid_Images[] = {R.raw.image1, R.raw.image2, R.raw.image3, R.raw.image4, R.raw.image5, R.raw.image6, R.raw.image7, R.raw.image8, R.raw.image9, R.raw.image10};

    private SmartFinger rc632ReaderConnection = null;
    private Handler mHandler = new Handler();

    private SQLiteCommunicator dbComm = new SQLiteCommunicator();

    private Timer recordUpdateTimer, dateTimeUpdateTimer;
    private TimerTask recordUpdateTimerTask, dateTimeUpdateTimerTask;

    private Handler rHandler = new Handler();
    private Handler dHandler = new Handler();

    private boolean isSmartRcvRegisterd = false;
    private boolean isMorphoRcvRegistered = false;
    private boolean isMorphoSmartRcvRegisterd = false;

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

    private int index = 0;
    double[] numArray = new double[Constants.ADC_READ_ARRAY_LENGTH];
    private float adcValue;
    private Handler adcHandler = new Handler();
    private Timer adcReadTimer = null;
    private TimerTask adcReadTimerTask = null;

    private static boolean isSDCalculated = false;
    private static double prevMean;
    int per = 0;


    private Handler bHandler = new Handler();
    private Timer batReadTimer = null;
    private TimerTask batReadTimerTask = null;

    boolean isADCReceiverUnregistered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*modifyActionBar();*/
        setContentView(R.layout.menu_list_white);

        initLayoutElements();

        if (!Constants.isTab) {
            if (HardwareInterface.class != null) {
                receiver = new AdcMessageBroadcastReceiver();
                registerReceiver(receiver, getIntentFilter());
                intent = new Intent();
                intent.setClass(HomeActivity.this, GetValueService.class);
                intent.putExtra("mtype", "ADC");
                intent.putExtra("maction", "start");
                intent.putExtra("mfd", 1);
                HomeActivity.this.startService(intent);
            } else {
                Toast.makeText(getApplicationContext(), "Load hardwareinterface library error!", Toast.LENGTH_LONG).show();
            }
        }

        initDrawerLayout();
        addDrawerItems();

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

            case 2:

                //======================== Startek FM200U Finger Sensor ===============================//

                initUSBManagerReceiver();
                unregisterReceivers();
                /**
                 * 5-> search only Startek USB finger scanner only
                 * @mode
                 * */
                initHardwareConnections(5);

                /**
                 * 1-> USB finger scanner only. and register BroadCastReceiver for startek device.
                 * must all after initHardwareConnections();
                 * @usbReader
                 * */
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
            case 5:     //======================== Startek FM200U Reader ===========================//

                /**
                 * 4 => search only Startek Usb finger scanner
                 * */
                searchDevices(4);

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

                /**
                 * isStartekFm200uRcvRegisterd it is flag set by it's super class USBConnectionCreator
                 * at device search time. deviceSearch function call by this class.
                 * added by suman dhara
                 * */
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

    private void unregisterReceivers() {

        if (isSmartRcvRegisterd) {
            try {
                if (mSmartReceiver != null) {
                    unregisterReceiver(mSmartReceiver);
                    isSmartRcvRegisterd = false;
                    mSmartReceiver = null;
                }
            } catch (Exception e) {
                Toast.makeText(HomeActivity.this, "error in unregister morpho smart receiver:" + e.getMessage(), Toast.LENGTH_LONG).show();
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
                Toast.makeText(HomeActivity.this, "error in unregister morpho receiver:" + e.getMessage(), Toast.LENGTH_LONG).show();
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
                Toast.makeText(HomeActivity.this, "error in unregister morpho smart receiver:" + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }


    public void startTimer() {
        if (recordUpdateTimer == null && dateTimeUpdateTimer == null && capReadTimer == null && batReadTimer == null && adcReadTimer == null) {
            recordUpdateTimer = new Timer();
            dateTimeUpdateTimer = new Timer();
            capReadTimer = new Timer();
            batReadTimer = new Timer();
            adcReadTimer = new Timer();
            initializeTimerTask();
            recordUpdateTimer.schedule(recordUpdateTimerTask, 0, 5000); //
            dateTimeUpdateTimer.schedule(dateTimeUpdateTimerTask, 0, 5000);
            capReadTimer.schedule(capReadTimerTask, 0, 50);
            batReadTimer.schedule(batReadTimerTask, 0, 500); //
            adcReadTimer.schedule(adcReadTimerTask, 0, 50); //1000
        }
    }

    public void initializeTimerTask() {
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

        recordUpdateTimerTask = new TimerTask() {
            public void run() {
                rHandler.post(new Runnable() {
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

                        int noOfRecords = dbComm.getNoOfRecordsToBeSend();
                        recordCount.setText("" + Integer.toString(noOfRecords));
                    }
                });
            }
        };

        dateTimeUpdateTimerTask = new TimerTask() {
            public void run() {
                dHandler.post(new Runnable() {
                    public void run() {
                        Calendar calendar = Calendar.getInstance();
                        SimpleDateFormat mdformat = new SimpleDateFormat("dd MMM yyyy,EEEE,hh:mm a");
                        String strDateTime = mdformat.format(calendar.getTime());
                        String[] splitDateAndTime = strDateTime.split(",");
                        if (splitDateAndTime != null && splitDateAndTime.length == 3) {
                            date.setText(splitDateAndTime[0] + ", " + splitDateAndTime[1]);
                            time.setText(splitDateAndTime[2]);
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
                                    if (isLCDBackLightOff) {
                                        stopHandler();//stop first and then start
                                        startHandler();
                                        ForlinxGPIO.setLCDBackLightOn();
                                        setScreenBrightness(Constants.BRIGHTNESS_ON);
                                        isLCDBackLightOff = false;
                                    }
                                    break;
                                case "63":
                                    if (isLCDBackLightOff) {
                                        stopHandler();//stop first and then start
                                        startHandler();
                                        ForlinxGPIO.setLCDBackLightOn();
                                        setScreenBrightness(Constants.BRIGHTNESS_ON);
                                        isLCDBackLightOff = false;
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
        getActionBar().setTitle(Html.fromHtml("<b><font face='Calibri' color='#FFFFFF'>Home</font></b>"));
    }

    // filter to identify images based on their extensions
    static final FilenameFilter IMAGE_FILTER = new FilenameFilter() {
        @Override
        public boolean accept(final File dir, final String name) {
            for (final String ext : Constants.IMAGE_FILE_EXTENSIONS) {
                if (name.endsWith("." + ext)) {
                    return true;
                }
            }
            return false;
        }
    };

    public void initLayoutElements() {

        pbBatPer = (ProgressBar) findViewById(R.id.pbBatPer);
        tvBatPer = (TextView) findViewById(R.id.tvBatPer);
        tvPower = (TextView) findViewById(R.id.tvPower);

        ivChargeIcon = (ImageView) findViewById(R.id.ivChargeIcon);
        ivBatTop = (ImageView) findViewById(R.id.ivBatTop);

        smart_reader = (ImageView) findViewById(R.id.smartreader);
        finger_reader = (ImageView) findViewById(R.id.fingerreader);
        viewFlipper = (ViewFlipper) findViewById(R.id.viewflipper);

        date = (TextView) findViewById(R.id.etdate);
        time = (TextView) findViewById(R.id.ettime);
        recordCount = (TextView) findViewById(R.id.recordCount);
        serverConfig = (TextView) findViewById(R.id.serverConfig);
        //statusView = (StatusView) findViewById(R.id.status);


        for (int i = 0; i < gallery_grid_Images.length; i++) {
            //  This will create dynamic image view and add them to ViewFlipper
            setFlipperImage(gallery_grid_Images[i]);
        }


//        File dir = new File(Constants.PROJECT_FILES_PATH);
//        if (dir != null && dir.exists()) {
//            File[] dirFiles = dir.listFiles(IMAGE_FILTER);
//            if (dirFiles != null) {
//                int dirLength = dirFiles.length;
//                for (int i = 0; i < dirLength; i++) {
//                    ImageView imgView = new ImageView(this);
//                    Bitmap bitmap = BitmapFactory.decodeFile(dir.listFiles(IMAGE_FILTER)[i].getAbsolutePath());
//                    if (bitmap != null) {
//                        imgView.setImageBitmap(bitmap);
//                        viewFlipper.addView(imgView);
//                    }
//                }
//            }
//        }

        // Declare in and out animations and load them using AnimationUtils class
        Animation in = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        Animation out = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        viewFlipper.setInAnimation(in);
        viewFlipper.setOutAnimation(out);
    }

    private void setFlipperImage(int res) {
        ImageView image = new ImageView(getApplicationContext());
        image.setBackgroundResource(res);
        viewFlipper.addView(image);
    }

//    "Employee Enrollment", 0
//            "Finger Enrollment",1
//            "Delete User",2
//            "Smart Card",3
//            "Attendance",4
//            "Programmable In-Out",5
//            "Server Settings",6
//            "Wiegand Settings", 7
//            "User Creation", 8
//            "System Info",9
//            "Finger Enrollment (Aadhaar Mode)",10
//            "Master Data Entry", 11
//            "Settings(Finger Enrollment Mode)", 12
//            "Hardware Settings", 13
//            "Excel Export/Import", 14
//            "Log Out",15


    public void addDrawerItems() {

        int drawerSize = 0;
        int smartReaderType = -1, empEnroll = -1, fingerEnrollMode = -1, masterDataEntry = -1, pio = -1, excelImportExport = -1;

        Settings settings = Settings.getInstance();
        String isAdmin = UserDetails.getInstance().getRole();
        empEnroll = settings.getEmployeeEnrollmentValue();
        fingerEnrollMode = settings.getFingerEnrollmentModeValue();
        smartReaderType = settings.getSrTypeValue();
        masterDataEntry = settings.getMasterDateEntryValue();
        pio = settings.getPioValue();
        excelImportExport = settings.getExcelImportExportVal();

        //================================= Employee Enrollment ====================================//

        if (isAdmin != null && isAdmin.trim().equals("Y")) {
            if (empEnroll == 1) {
                drawerSize++;
            }
        }

        //================================= Employee Finger Enrollment ====================================//

//        if (isAdmin != null && isAdmin.trim().equals("Y")) {
//            if (fingerEnrollMode == 0) {
//                drawerSize += 2;
//            } else {
//                drawerSize += 1;
//            }
//        }


        if (isAdmin != null && isAdmin.trim().equals("Y")) {
            drawerSize += 1;
        }

        //=================================  Delete User =====================================//

        if (isAdmin != null && isAdmin.trim().equals("Y")) {
            drawerSize += 1;
        }

        //================================= Smart Card ====================================//

        if (smartReaderType != -1) {
            drawerSize++;
        }

        //================================= Employee Attendance   ====================================//

        drawerSize += 1;

        //================================= Programmable In-Out =====================================//


        if (isAdmin != null && isAdmin.trim().equals("Y")) {
            if (pio == 1) {
                drawerSize++;
            }
        }

        //================================= Server Settings, Wiegand Settings ,Global Verification Mode, System Info ,User Info,Log Out  =====================================//

        //drawerSize += 2;

        drawerSize += 6;

        //=================================  User Creation =====================================//

        if (isAdmin != null && isAdmin.trim().equals("Y")) {
            drawerSize += 1;
        }

        //================================= Master Data Entry =====================================//

        if (isAdmin != null && isAdmin.trim().equals("Y")) {
            if (masterDataEntry == 1) {
                drawerSize++;
            }
        }


        drawerItem = new DrawerItem[drawerSize];

        int index = 0;


        //================================= Excel Import/Export =====================================//


//        if (excelImportExport == 1) {
//            drawerSize++;
//        }


//        if (isAdmin != null && isAdmin.trim().equals("Y")) {
//            if (excelImportExport == 1) {
//                drawerSize++;
//            }
//        }


        //================================= Employee Enrollment ====================================//

        if (isAdmin != null && isAdmin.trim().equals("Y")) {
            if (empEnroll == 1) {
                drawerItem[index++] = new DrawerItem(R.drawable.pointer, Constants.HOME_MENU_ITEM_NAMES[0]);
            }
        }

        //================================= Employee Finger Enrollment ====================================//

//        if (isAdmin != null && isAdmin.trim().equals("Y")) {
//            if (fingerEnrollMode == 0) {
//                drawerItem[index++] = new DrawerItem(R.drawable.pointer, Constants.HOME_MENU_ITEM_NAMES[1]);
//                drawerItem[index++] = new DrawerItem(R.drawable.pointer, Constants.HOME_MENU_ITEM_NAMES[2]);
//            } else if (fingerEnrollMode == 1) {
//                drawerItem[index++] = new DrawerItem(R.drawable.pointer, Constants.HOME_MENU_ITEM_NAMES[2]);
//            }
//        }


        if (isAdmin != null && isAdmin.trim().equals("Y")) {
            drawerItem[index++] = new DrawerItem(R.drawable.pointer, Constants.HOME_MENU_ITEM_NAMES[1]);
        }


        //================================= Delete User =====================================//

        if (isAdmin != null && isAdmin.trim().equals("Y")) {
            drawerItem[index++] = new DrawerItem(R.drawable.pointer, Constants.HOME_MENU_ITEM_NAMES[2]);
        }

        //================================= Smart Card =====================================//

        if (smartReaderType != -1) {
            drawerItem[index++] = new DrawerItem(R.drawable.pointer, Constants.HOME_MENU_ITEM_NAMES[3]);
        }


        //================================= Employee Attendance ====================================//

        drawerItem[index++] = new DrawerItem(R.drawable.pointer, Constants.HOME_MENU_ITEM_NAMES[4]);

        //================================= Programmable In-Out =====================================//

        if (isAdmin != null && isAdmin.trim().equals("Y")) {
            if (pio == 1) {
                drawerItem[index++] = new DrawerItem(R.drawable.pointer, Constants.HOME_MENU_ITEM_NAMES[5]);
            }
        }

        //================================= Server Settings =========================================//

        if (isAdmin != null && isAdmin.trim().equals("Y")) {
            drawerItem[index++] = new DrawerItem(R.drawable.pointer, Constants.HOME_MENU_ITEM_NAMES[6]);
        }


        //================================= Wiegand Settings ==================================//

        if (isAdmin != null && isAdmin.trim().equals("Y")) {
            drawerItem[index++] = new DrawerItem(R.drawable.pointer, Constants.HOME_MENU_ITEM_NAMES[7]);
        }


        //================================= Global Verification Mode =====================================//

        drawerItem[index++] = new DrawerItem(R.drawable.pointer, Constants.HOME_MENU_ITEM_NAMES[8]);


        //================================= User Info =====================================//

        drawerItem[index++] = new DrawerItem(R.drawable.pointer, Constants.HOME_MENU_ITEM_NAMES[9]);

        //================================= System Info =====================================//

        drawerItem[index++] = new DrawerItem(R.drawable.pointer, Constants.HOME_MENU_ITEM_NAMES[10]);


        //================================= User Creation =====================================//

        if (isAdmin != null && isAdmin.trim().equals("Y")) {
            drawerItem[index++] = new DrawerItem(R.drawable.pointer, Constants.HOME_MENU_ITEM_NAMES[11]);
        }

        //================================= Master Data Entry =====================================//

        if (isAdmin != null && isAdmin.trim().equals("Y")) {
            if (masterDataEntry == 1) {
                drawerItem[index++] = new DrawerItem(R.drawable.pointer, Constants.HOME_MENU_ITEM_NAMES[12]);
            }
        }

        //================================= Excel Import/Export =====================================//

//        if (excelImportExport == 1) {
//            drawerItem[index++] = new DrawerItem(R.drawable.pointer, Constants.HOME_MENU_ITEM_NAMES[10]);
//        }

//        if (isAdmin != null && isAdmin.trim().equals("Y")) {
//            if (excelImportExport == 1) {
//                drawerItem[index++] = new DrawerItem(R.drawable.pointer, mNavigationDrawerItemTitles[10]);
//            }
//        }


        //================================= Log Out ==============================================//

        drawerItem[index++] = new DrawerItem(R.drawable.pointer, Constants.HOME_MENU_ITEM_NAMES[17]);

        adapter = new DrawerItemCustomAdapter(this, R.layout.row, drawerItem);
        mDrawerList.setAdapter(adapter);

    }

    public void initDrawerLayout() {

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        LayoutInflater inflater = getLayoutInflater();
        View listHeaderView = inflater.inflate(R.layout.header_list, null, false);
        CircleImageView imageView = (CircleImageView) listHeaderView.findViewById(R.id.circleView);
        TextView tvname = (TextView) listHeaderView.findViewById(R.id.name);

        String strName = UserDetails.getInstance().getName();
        byte[] imageData = UserDetails.getInstance().getPhoto();

        if (imageData != null) {
            imageView.setImageBitmap(BitmapFactory.decodeByteArray(imageData, 0, imageData.length));
        } else {
            imageView.setImageResource(R.drawable.dummyphoto);
        }

        tvname.setText(strName);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.addHeaderView(listHeaderView);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.drawable.listarrow,
                R.string.drawer_open,
                R.string.drawer_close
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getActionBar().setTitle(Html.fromHtml("<b><font face='Calibri' color='#FFFFFF'>Home</font></b>"));
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActionBar().setTitle(Html.fromHtml("<b><font face='Calibri' color='#FFFFFF'>Home</font></b>"));
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }


    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView <?> parent, View view, int position, long id) {
            try {
                selectItem(drawerItem[position - 1].name, position);
            } catch (Exception e) {
            }
        }
    }


    private void selectItem(String moduleName, int position) {

        //private String[] mNavigationDrawerItemTitles = {"Employee Enrollment", "Finger Enrollment (Normal Mode)", "Finger Enrollment (Aadhaar Mode)", "Employee Attendance", "Smart Card (Read+Write+Refresh)", "Master Data Entry", "Programmable In-Out", "Settings(Finger Enrollment Mode)", "Server Settings", "Hardware Settings", "Excel Export/Import", "User Creation", "Log Out"};

        switch (moduleName) {

            case "Employee Enrollment":

                Intent formFillIntent = new Intent(HomeActivity.this, EmployeeEnrollmentFirstActivity.class);
                startActivity(formFillIntent);
                overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                finish();
                break;

            case "Finger Enrollment":

                stopADCReceiver();
                Intent fingerEnrollIntent = new Intent(HomeActivity.this, EmployeeFingerEnrollmentActivity.class);
                startActivity(fingerEnrollIntent);
                overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                finish();

                break;

            case "Finger Enrollment (Aadhaar Mode)":

                // finish();
                //  Intent aadhaarVerificationIntent = new Intent(HomeActivity.this, AadhaarFingerEnrollment.class);
                // startActivity(aadhaarVerificationIntent);
                break;

            case "Attendance":

                int ftype = Settings.getInstance().getFrTypeValue();
                if (ftype == 2) {
                    stopADCReceiver();
                    Intent fingerIdentifyIntent = new Intent(HomeActivity.this, EmployeeAttendanceStartekActivity.class);
                    startActivity(fingerIdentifyIntent);
                    overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                    finish();
                } else {
                    stopADCReceiver();
                    Intent fingerIdentifyIntent = new Intent(HomeActivity.this, EmployeeAttendanceActivity.class);
                    startActivity(fingerIdentifyIntent);
                    overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                    finish();
                }


                break;

            case "Smart Card":

                stopADCReceiver();
                Intent smartCardIntent = new Intent(HomeActivity.this, SmartCardActivity.class);
                startActivity(smartCardIntent);
                overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                finish();

                break;

            case "Master Data Entry":

                stopADCReceiver();
                Intent matserDataEntryIntent = new Intent(HomeActivity.this, MasterDataEntryActivity.class);
                startActivity(matserDataEntryIntent);
                overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                finish();

                break;

            case "Programmable In-Out":
                showInOutModeDialog();
                break;

            case "Excel Export/Import":
                showExcelImportExportDialog();
                break;

            case "User Creation":

                stopADCReceiver();
                Intent sign = new Intent(HomeActivity.this, User.class);
                startActivity(sign);
                overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                finish();

                break;

            case "Delete User":

                stopADCReceiver();
                Intent userDelete = new Intent(HomeActivity.this, DeleteUserActivity.class);
                startActivity(userDelete);
                overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                finish();

                break;

            case "Server Settings":

                showServerSettingsDialog();
                break;

            case "Wiegand Settings":

                showWiegandSettings();
                break;

            case "GVM Settings":

                showGVMSettings();
                break;

            case "User Info":

                showUserInfoDialog();
                break;

            case "System Info":

                showSystemInfoDialog();
                break;


            case "Log Out":

                stopADCReceiver();
                LoginSplashActivity.isLoaded = false;
                UserDetails.getInstance().reset();
                Intent logout = new Intent(HomeActivity.this, LoginSplashActivity.class);
                startActivity(logout);
                overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                finish();

                break;

        }

        if (position > 0) {
            mDrawerList.setItemChecked(position - 1, true);
            mDrawerList.setSelection(position - 1);
            getActionBar().setTitle(Constants.HOME_MENU_ITEM_NAMES[position - 1]);
            mDrawerLayout.closeDrawer(mDrawerList);
        }
    }

    private void showGVMSettings() {

        final Context context = HomeActivity.this;
        final Dialog dialog = new Dialog(context);
        dialog.setCanceledOnTouchOutside(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.gvm_dialog);

        TextView title = (TextView) dialog.findViewById(R.id.title);
        final Spinner spGVM = (Spinner) dialog.findViewById(R.id.spGVM);

        Button btn_Save = (Button) dialog.findViewById(R.id.btn_Ok);
        ImageButton btn_Cancel = (ImageButton) dialog.findViewById(R.id.image);

        title.setText("GVM Settings");

        ArrayAdapter adapter = new ArrayAdapter <String>(this, android.R.layout.simple_spinner_item, Constants.GVM_DISPLAY);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spGVM.setAdapter(adapter);

        String gvm = dbComm.getCurrentGVM();
        if (gvm.trim().length() > 0) {
            int len = Constants.GVM_DISPLAY.length;
            for (int i = 0; i < len; i++) {
                if (gvm.equals(Constants.GVM_DISPLAY[i])) {
                    spGVM.setSelection(i);
                    break;
                }
            }
        }

        btn_Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String gvm = spGVM.getSelectedItem().toString();
                int status = dbComm.insertGVM(gvm);
                if (status != -1) {
                    showCustomAlertDialog(true, "Save", "GVM Saved Successfully");
                } else {
                    showCustomAlertDialog(false, "Save", "Failed to save data");
                }
            }
        });

        btn_Cancel.setOnClickListener(new View.OnClickListener() {
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

    private void showUserInfoDialog() {

        final Context context = HomeActivity.this;
        final Dialog dialog = new Dialog(context);
        dialog.setCanceledOnTouchOutside(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.user_info_dialog);

        TextView title = (TextView) dialog.findViewById(R.id.title);
        EditText etTV = (EditText) dialog.findViewById(R.id.etTV);
        EditText etTEU = (EditText) dialog.findViewById(R.id.etTEU);
        EditText etTUEU = (EditText) dialog.findViewById(R.id.etTUEU);
        EditText etTFTSQL = (EditText) dialog.findViewById(R.id.etTFTSQL);
        EditText etTSC = (EditText) dialog.findViewById(R.id.etTSC);
        EditText etTFTSEN = (EditText) dialog.findViewById(R.id.etTFTSEN);
        EditText etUFT = (EditText) dialog.findViewById(R.id.etUFT);

        Button btn_Ok = (Button) dialog.findViewById(R.id.btn_Ok);
        ImageButton btn_Cancel = (ImageButton) dialog.findViewById(R.id.image);

        title.setText("User Info");

        int count = -1;

        count = dbComm.getTotalEnrolledUsers();
        if (count != -1) {
            etTV.setText(Integer.toString(count));
        } else {
            etTV.setText("0");
        }

        count = dbComm.getTotalEnrolledUsers("Y");//Total Enrolled User
        if (count != -1) {
            etTEU.setText(Integer.toString(count));
        } else {
            etTEU.setText("0");
        }

        count = dbComm.getTotalEnrolledUsers("N");//Total Unenrolled User
        if (count != -1) {
            etTUEU.setText(Integer.toString(count));
        } else {
            etTUEU.setText("0");
        }

        count = dbComm.getTotalLocalFingerTemplates();//Total Finger Templates in SQLite
        if (count != -1) {
            etTFTSQL.setText(Integer.toString(count));
        } else {
            etTFTSQL.setText("0");
        }

        MorphoDevice morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
        MorphoDatabase morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();

        if (morphoDevice != null && morphoDatabase != null) {
            int ret = -1;
            Integer maxUser = new Integer(10);
            Integer maxFingerPerUser = new Integer(10);
            ret = morphoDatabase.getMaxUser(maxUser, maxFingerPerUser);
            if (ret == 0) {
                int totalTemplates = maxUser * maxFingerPerUser;
                etTSC.setText(Integer.toString(totalTemplates));
            }
        }

        count = dbComm.getTotalLocalFingerTemplates();//Total Finger Templates in SQLite
        if (count != -1) {
            etTFTSEN.setText(Integer.toString(count));
        } else {
            etTFTSEN.setText("0");
        }

        count = dbComm.getUnsendFingerRecords();
        if (count != -1) {
            etUFT.setText(Integer.toString(count));
        } else {
            etUFT.setText("0");
        }


        btn_Ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        btn_Cancel.setOnClickListener(new View.OnClickListener() {
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

    private void showSystemInfoDialog() {

        final Context context = HomeActivity.this;
        final Dialog dialog = new Dialog(context);
        dialog.setCanceledOnTouchOutside(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.system_info_dialog);

        TextView title = (TextView) dialog.findViewById(R.id.title);

        EditText appVersion = (EditText) dialog.findViewById(R.id.etAppVersion);
        EditText deviceType = (EditText) dialog.findViewById(R.id.etDeviceType);
        EditText smartReader = (EditText) dialog.findViewById(R.id.etSmartReader);
        EditText fingerReader = (EditText) dialog.findViewById(R.id.etFingerReader);
        EditText fingerEnrollMode = (EditText) dialog.findViewById(R.id.etFingerEnrollMode);

        EditText gvm = (EditText) dialog.findViewById(R.id.etGVM);

        EditText protocol = (EditText) dialog.findViewById(R.id.etProtocol);

        EditText serverName = (EditText) dialog.findViewById(R.id.etAtSName);
        EditText serverDomain = (EditText) dialog.findViewById(R.id.etAtSDomain);
        EditText serverIP = (EditText) dialog.findViewById(R.id.etAtSIP);
        EditText serverPort = (EditText) dialog.findViewById(R.id.etAtSPort);

        EditText brokerIP = (EditText) dialog.findViewById(R.id.etBrokerIP);
        EditText brokerPort = (EditText) dialog.findViewById(R.id.etBrokerPort);
        EditText brokerUsername = (EditText) dialog.findViewById(R.id.etBrokerUsername);
        EditText brokerPassword = (EditText) dialog.findViewById(R.id.etBrokerPassword);

        Button btn_Ok = (Button) dialog.findViewById(R.id.btn_Ok);
        ImageButton btn_Cancel = (ImageButton) dialog.findViewById(R.id.image);

        // final RadioGroup rgWifi = (RadioGroup) dialog.findViewById(R.id.rgWifi);

        title.setText("System Info");

        appVersion.setText(Constants.APP_VERSION);

//        rgWifi.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(RadioGroup group, int checkedId) {
//                View radioButton = rgWifi.findViewById(checkedId);
//                int index = rgWifi.indexOfChild(radioButton);
//                switch (index) {
//                    case 0:
//                        ForlinxGPIOCommunicator.setGPIO(Constants.WIFI_ENABLE_DISABLE, "1");
//                        break;
//                    case 1:
//                        ForlinxGPIOCommunicator.setGPIO(Constants.WIFI_ENABLE_DISABLE, "0");
//                        break;
//                }
//            }
//        });

        Cursor rs = dbComm.getProtocolDetails();
        if (rs != null) {
            if (rs.getCount() > 0) {
                while (rs.moveToNext()) {
                    protocol.setText(rs.getString(0).trim());
                }
            }
            rs.close();
        }

        rs = dbComm.getSettings();
        if (rs != null && rs.getCount() > 0) {
            while (rs.moveToNext()) {
                String strParamName = rs.getString(0);
                if (strParamName != null && strParamName.trim().length() > 0) {
                    switch (strParamName) {
                        case "Device Type":
                            deviceType.setText(rs.getString(1));
                            break;
                        case "Smart Reader Type":
                            smartReader.setText(rs.getString(1));
                            break;
                        case "Finger Reader Type":
                            fingerReader.setText(rs.getString(1));
                            break;
                        case "Enrollment Mode":
                            fingerEnrollMode.setText(rs.getString(1));
                            break;
                        case "Server Type":
                            serverName.setText(rs.getString(1));
                            break;
                        default:
                            break;
                    }
                }
            }
            rs.close();
        }

        String strGVM = dbComm.getCurrentGVM();
        gvm.setText(strGVM);

        rs = dbComm.getHttpServerDetails();
        if (rs != null) {
            if (rs.getCount() > 0) {
                while (rs.moveToNext()) {
                    serverName.setText(rs.getString(0).trim());
                    serverDomain.setText(rs.getString(1).trim());
                    serverIP.setText(rs.getString(2).trim());
                    serverPort.setText(rs.getString(3).trim());
                }
            }
            rs.close();
        }

        rs = dbComm.getMqttBrokerDetails();
        if (rs != null) {
            if (rs.getCount() > 0) {
                while (rs.moveToNext()) {
                    brokerIP.setText(rs.getString(0).trim());
                    brokerPort.setText(rs.getString(1).trim());
                    brokerUsername.setText(rs.getString(2).trim());
                    brokerPassword.setText(rs.getString(3).trim());
                }
            }
            rs.close();
        }


        btn_Ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        btn_Cancel.setOnClickListener(new View.OnClickListener() {
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

    private void showServerSettingsDialog() {

        final Context context = HomeActivity.this;
        final Dialog dialog = new Dialog(context);
        dialog.setCanceledOnTouchOutside(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.server_settings_dialog);

        ImageButton btnClose = (ImageButton) dialog.findViewById(R.id.image);

        TextView title = (TextView) dialog.findViewById(R.id.title);

        final RadioGroup rgServer = (RadioGroup) dialog.findViewById(R.id.rgServer);

        final LinearLayout llHttp = (LinearLayout) dialog.findViewById(R.id.llHttp);
        final LinearLayout llMqtt = (LinearLayout) dialog.findViewById(R.id.llMqtt);

        final EditText etHttpServerName = (EditText) dialog.findViewById(R.id.etAtSName);
        final EditText etHttpServerDomain = (EditText) dialog.findViewById(R.id.etAtSDomain);
        final EditText etServerIP = (EditText) dialog.findViewById(R.id.etAtSIP);
        final EditText etServerPort = (EditText) dialog.findViewById(R.id.etAtSPort);

        final EditText etBrokerIP = (EditText) dialog.findViewById(R.id.etBrokerIP);
        final EditText etBrokerPort = (EditText) dialog.findViewById(R.id.etBrokerPort);
        final EditText etBrokerUsername = (EditText) dialog.findViewById(R.id.etBrokerUsername);
        final EditText etBrokerPassword = (EditText) dialog.findViewById(R.id.etBrokerPassword);

        title.setText("Server Settings");

        Button btnHttp = (Button) dialog.findViewById(R.id.btn_Http);
        Button btnMqtt = (Button) dialog.findViewById(R.id.btn_Mqtt);


        //  llHttp.setVisibility(View.VISIBLE);
        //  llMqtt.setVisibility(View.GONE);


//        Cursor rs = dbComm.getHttpServerDetails();
//        if (rs != null) {
//            if (rs.getCount() > 0) {
//                while (rs.moveToNext()) {
//                    etHttpServerName.setText(rs.getString(0).trim());
//                    etHttpServerDomain.setText(rs.getString(1).trim());
//                    etServerIP.setText(rs.getString(2).trim());
//                    etServerPort.setText(rs.getString(3).trim());
//                }
//                rs.close();
//            }
//        }

        Cursor rs = dbComm.getProtocolDetails();
        if (rs != null) {
            if (rs.getCount() > 0) {
                while (rs.moveToNext()) {
                    String cp = rs.getString(0).trim();
                    switch (cp) {
                        case "HTTP":
                            llHttp.setVisibility(View.VISIBLE);
                            rgServer.check(R.id.rbHttp);
                            Cursor rsHttp = dbComm.getHttpServerDetails();
                            if (rsHttp != null) {
                                if (rsHttp.getCount() > 0) {
                                    while (rsHttp.moveToNext()) {
                                        etHttpServerName.setText(rsHttp.getString(0).trim());
                                        etHttpServerDomain.setText(rsHttp.getString(1).trim());
                                        etServerIP.setText(rsHttp.getString(2).trim());
                                        etServerPort.setText(rsHttp.getString(3).trim());
                                    }
                                    rsHttp.close();
                                }
                            }
                            break;
                        case "MQTT":
                            llMqtt.setVisibility(View.VISIBLE);
                            rgServer.check(R.id.rbMqtt);
                            Cursor rsMqtt = dbComm.getMqttBrokerDetails();
                            if (rsMqtt != null) {
                                if (rsMqtt.getCount() > 0) {
                                    while (rsMqtt.moveToNext()) {
                                        etBrokerIP.setText(rsMqtt.getString(0).trim());
                                        etBrokerPort.setText(rsMqtt.getString(1).trim());
                                        etBrokerUsername.setText(rsMqtt.getString(2).trim());
                                        etBrokerPassword.setText(rsMqtt.getString(3).trim());
                                    }
                                    rsMqtt.close();
                                }
                            }
                            break;
                    }
                }
            }
            rs.close();
        } else {
            llHttp.setVisibility(View.VISIBLE);//No Server Settings Found
        }


        rgServer.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                View radioButton = rgServer.findViewById(checkedId);
                int index = rgServer.indexOfChild(radioButton);
                Cursor rs = null;
                switch (index) {
                    case 0:
                        llHttp.setVisibility(View.VISIBLE);
                        llMqtt.setVisibility(View.GONE);
                        rs = dbComm.getHttpServerDetails();
                        if (rs != null) {
                            if (rs.getCount() > 0) {
                                while (rs.moveToNext()) {
                                    etHttpServerName.setText(rs.getString(0).trim());
                                    etHttpServerDomain.setText(rs.getString(1).trim());
                                    etServerIP.setText(rs.getString(2).trim());
                                    etServerPort.setText(rs.getString(3).trim());
                                }
                                rs.close();
                            }
                        }
                        break;
                    case 1:
                        llMqtt.setVisibility(View.VISIBLE);
                        llHttp.setVisibility(View.GONE);
                        rs = dbComm.getMqttBrokerDetails();
                        if (rs != null) {
                            if (rs.getCount() > 0) {
                                while (rs.moveToNext()) {
                                    etBrokerIP.setText(rs.getString(0).trim());
                                    etBrokerPort.setText(rs.getString(1).trim());
                                    etBrokerUsername.setText(rs.getString(2).trim());
                                    etBrokerPassword.setText(rs.getString(3).trim());
                                }
                                rs.close();
                            }
                        }
                        break;
                }
            }
        });

        btnHttp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = etHttpServerName.getText().toString();
                String domain = etHttpServerDomain.getText().toString();
                String ip = etServerIP.getText().toString();
                String port = etServerPort.getText().toString();
                boolean isValid = validateHttpDetails(name, domain, ip, port);
                if (isValid) {
                    SQLiteCommunicator dbComm = new SQLiteCommunicator();
                    int status = dbComm.insertHttpServerDetails(name, domain, ip, port);
                    if (status != -1) {
                        status = dbComm.insertProtocolDetails("HTTP", "Y");
                        if (status != -1) {
                            showCustomAlertDialog(true, "Status", "Data Saved Successfully");
                        } else {
                            showCustomAlertDialog(true, "Status", "Failed To Save Protocol Data");
                        }
                    } else {
                        showCustomAlertDialog(true, "Status", "Failed To Save Server Data");
                    }
                }
            }
        });

        btnMqtt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ip = etBrokerIP.getText().toString();
                String port = etBrokerPort.getText().toString();
                String username = etBrokerUsername.getText().toString();
                String password = etBrokerPassword.getText().toString();
                boolean isValid = validateMqttDetails(ip, port);
                if (isValid) {
                    SQLiteCommunicator dbComm = new SQLiteCommunicator();
                    int status = dbComm.insertMqttDetails(ip, port, username, password);
                    if (status != -1) {
                        status = dbComm.insertProtocolDetails("MQTT", "Y");
                        if (status != -1) {
                            showCustomAlertDialog(true, "Status", "Data Saved Successfully");
                        } else {
                            showCustomAlertDialog(true, "Status", "Failed To Save Protocol Data");
                        }
                    } else {
                        showCustomAlertDialog(true, "Status", "Failed To Save Server Data");
                    }
                }
            }
        });

        btnClose.setOnClickListener(new View.OnClickListener() {
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

    private boolean validateHttpDetails(String name, String domain, String ip, String port) {

        if (name != null && name.trim().length() == 0) {
            showCustomAlertDialog(false, "Error", "Server Name cannot be left blank");
            return false;
        }

        if (ip != null && ip.trim().length() == 0) {
            showCustomAlertDialog(false, "Error", "Server IP cannot be left blank");
            return false;
        }

        boolean isValid = Utility.isValidInet4Address(ip);
        if (!isValid) {
            showCustomAlertDialog(false, "Error", "Invalid Server IP Address");
            return false;
        }

        if (port != null && port.trim().length() == 0) {
            showCustomAlertDialog(false, "Error", "Server Port cannot be left blank");
            return false;
        }

        isValid = Utility.validatePortNumber(port);
        if (!isValid) {
            showCustomAlertDialog(false, "Error", "Invalid Server Port No");
            return false;
        }
        return true;
    }


    private boolean validateMqttDetails(String ip, String port) {
        if (ip != null && ip.trim().length() == 0) {
            showCustomAlertDialog(false, "Error", "Server IP cannot be left blank");
            return false;
        }
        boolean isValid = Utility.isValidInet4Address(ip);
        if (!isValid) {
            showCustomAlertDialog(false, "Error", "Invalid Server IP Address");
            return false;
        }
        if (port != null && port.trim().length() == 0) {
            showCustomAlertDialog(false, "Error", "Server Port cannot be left blank");
            return false;
        }
        isValid = Utility.validatePortNumber(port);
        if (!isValid) {
            showCustomAlertDialog(false, "Error", "Invalid Server Port No");
            return false;
        }


        return true;
    }

    String hexToDec = "N";
    String siteCodeEnabled = "N";
    String cardNo = "0";

    private void showWiegandSettings() {

        final Context context = HomeActivity.this;
        final Dialog dialog = new Dialog(context);
        dialog.setCanceledOnTouchOutside(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.wiegand_settings_dialog);
        Window window = dialog.getWindow();
        window.setGravity(Gravity.CENTER_HORIZONTAL);


        TextView title = (TextView) dialog.findViewById(R.id.title);
        ImageButton close = (ImageButton) dialog.findViewById(R.id.image);

        int screenSize = 0;

        if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE) {
            screenSize = 1;
            window.setLayout(400, 250);
        } else if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_NORMAL) {
            screenSize = 2;
            window.setLayout(400, 180);
        } else if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_SMALL) {
            screenSize = 3;
        } else {
            screenSize = 4;
        }

        title.setText("Wiegand Settings Details");

        final LinearLayout llHexToDec = (LinearLayout) dialog.findViewById(R.id.llHexToDec);
        final LinearLayout llSiteCode = (LinearLayout) dialog.findViewById(R.id.llSiteCode);
        final LinearLayout llCardNo = (LinearLayout) dialog.findViewById(R.id.llCardNo);

        final RadioGroup rgHexToDec = (RadioGroup) dialog.findViewById(R.id.onHexToDec);
        final RadioGroup rgSiteCodeEnabled = (RadioGroup) dialog.findViewById(R.id.onSiteCodeEnabled);
        final RadioGroup rgCardNo = (RadioGroup) dialog.findViewById(R.id.onCardNo);

        final RadioButton rbHexToDecYes = (RadioButton) dialog.findViewById(R.id.onHexToDecYes);
        final RadioButton rbHexToDecNo = (RadioButton) dialog.findViewById(R.id.onHexToDecNo);

        final RadioButton rbSiteCodeEnabledYes = (RadioButton) dialog.findViewById(R.id.onSiteCodeEnabledYes);
        final RadioButton rbSiteCodeEnabledNo = (RadioButton) dialog.findViewById(R.id.onSiteCodeEnabledNo);


        final RadioButton rb32bit = (RadioButton) dialog.findViewById(R.id.thirtytwobit);
        final RadioButton rb24bit = (RadioButton) dialog.findViewById(R.id.twentyfourbit);
        final RadioButton rb16bit = (RadioButton) dialog.findViewById(R.id.sixteenbit);
        final RadioButton rb16bitSiteCode = (RadioButton) dialog.findViewById(R.id.sixteenbitAndSiteCode);
        final RadioButton rb24bitSiteCode = (RadioButton) dialog.findViewById(R.id.twentyfourbitAndSiteCode);

        Button btnSave = (Button) dialog.findViewById(R.id.btn_Save);
        Button btnCancel = (Button) dialog.findViewById(R.id.btn_Cancel);

        rb32bit.setVisibility(View.GONE);
        rb24bit.setVisibility(View.GONE);
        rb16bit.setVisibility(View.GONE);
        rb16bitSiteCode.setVisibility(View.GONE);
        rb24bitSiteCode.setVisibility(View.GONE);

        llSiteCode.setVisibility(View.GONE);
        llCardNo.setVisibility(View.GONE);


        final int ss = screenSize;
        rgHexToDec.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                View radioButton = rgHexToDec.findViewById(checkedId);
                int index = rgHexToDec.indexOfChild(radioButton);
                switch (index) {
                    case 0:
                        hexToDec = "Y";
                        llSiteCode.setVisibility(View.VISIBLE);
                        llCardNo.setVisibility(View.VISIBLE);
                        rb32bit.setVisibility(View.VISIBLE);
                        rb24bit.setVisibility(View.VISIBLE);
                        rb16bit.setVisibility(View.VISIBLE);
                        rb16bitSiteCode.setVisibility(View.GONE);
                        rb24bitSiteCode.setVisibility(View.GONE);
                        rbSiteCodeEnabledNo.setChecked(true);
                        if (ss == 1) {
                            dialog.getWindow().setLayout(400, 400);
                        } else if (ss == 2) {
                            dialog.getWindow().setLayout(400, 320);
                        }

                        break;
                    case 1:
                        hexToDec = "N";
                        llSiteCode.setVisibility(View.GONE);
                        llCardNo.setVisibility(View.GONE);
                        if (ss == 1) {
                            dialog.getWindow().setLayout(400, 250);
                        } else if (ss == 2) {
                            dialog.getWindow().setLayout(400, 180);
                        }

                        break;
                }
            }
        });

        rgSiteCodeEnabled.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                View radioButton = rgSiteCodeEnabled.findViewById(checkedId);
                int index = rgSiteCodeEnabled.indexOfChild(radioButton);
                switch (index) {
                    case 0:
                        siteCodeEnabled = "Y";
                        llCardNo.setVisibility(View.VISIBLE);
                        rb32bit.setVisibility(View.VISIBLE);
                        rb24bit.setVisibility(View.VISIBLE);
                        rb16bit.setVisibility(View.VISIBLE);
                        rb16bitSiteCode.setVisibility(View.VISIBLE);
                        rb24bitSiteCode.setVisibility(View.VISIBLE);
                        if (ss == 1) {
                            dialog.getWindow().setLayout(400, 480);
                        } else if (ss == 2) {
                            dialog.getWindow().setLayout(400, 380);
                        }

                        break;
                    case 1:
                        siteCodeEnabled = "N";
                        llCardNo.setVisibility(View.VISIBLE);
                        rb32bit.setVisibility(View.VISIBLE);
                        rb24bit.setVisibility(View.VISIBLE);
                        rb16bit.setVisibility(View.VISIBLE);
                        rb16bitSiteCode.setVisibility(View.GONE);
                        rb24bitSiteCode.setVisibility(View.GONE);
                        if (ss == 1) {
                            dialog.getWindow().setLayout(400, 400);
                        } else if (ss == 2) {
                            dialog.getWindow().setLayout(400, 330);
                        }

                        break;
                }
            }
        });

        rgCardNo.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                View radioButton = rgCardNo.findViewById(checkedId);
                int index = rgCardNo.indexOfChild(radioButton);
                switch (index) {
                    case 0://16
                        cardNo = "0";
                        break;
                    case 1:
                        cardNo = "1";
                        break;
                    case 2:
                        cardNo = "2";
                        break;
                    case 3:
                        cardNo = "3";
                        break;
                    case 4:
                        cardNo = "4";
                        break;
                }
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SQLiteCommunicator dbComm = new SQLiteCommunicator();
                int count = dbComm.isWiegandSettingsAvailable();
                if (count > 0) {
                    count = dbComm.deleteWiegandSettings();
                    if (count > 0) {
                        count = dbComm.insertWiegandSettings(hexToDec, siteCodeEnabled, cardNo);
                        if (count != -1) {
                            showCustomAlertDialog(true, "Save Status", "Wiegand Data Saved Successfully");
                        }
                    }
                } else {
                    count = dbComm.insertWiegandSettings(hexToDec, siteCodeEnabled, cardNo);
                    if (count != -1) {
                        showCustomAlertDialog(true, "Save Status", "Wiegand Data Saved Successfully");
                    }
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
                hexToDec = "";
                siteCodeEnabled = "";
                cardNo = "";
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
                hexToDec = "";
                siteCodeEnabled = "";
                cardNo = "";
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

//    public final void onNumberFingerClicked(View view) {
//        RadioButton rb = (RadioButton) view;
//        int action = 0;
//        Spinner spinner = (Spinner) findViewById(R.id.fingerIndex);
//        if (rb.getId() == R.id.onefingerEnroll) {
//            action = 1;
//            spinner.setEnabled(EmployeeFingerEnrollInfo.getInstance().isUpdateTemplate());
//            finger2.setEnabled(false);
//        } else if (rb.getId() == R.id.twofingerEnroll) {
//            action = 2;
//            spinner.setEnabled(false);
//            finger2.setEnabled(true);
//        }
//        finger1.setSelection(0);
//        finger2.setSelection(0);
//        securityLevel.setSelection(0);
//        verificationMode.setSelection(0);
//        editTextPin.setText("");
//        empFingerInfo.setNoOfFingers(action);
//    }

    private void showExcelImportExportDialog() {

        final Context context = HomeActivity.this;
        final Dialog dialog = new Dialog(context);
        dialog.setCanceledOnTouchOutside(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.excel_import_export_dialog);

        TextView title = (TextView) dialog.findViewById(R.id.title);
        ImageButton cancel = (ImageButton) dialog.findViewById(R.id.image);
        /*cancel.setImageResource(R.drawable.failure);*/ //Modified by Sanjay Shyamal on 23/11/2017
        title.setText("Excel Export Import");

        Button buttonCreateWrite = (Button) dialog.findViewById(R.id.createWrite);
        Button buttonReadWrite = (Button) dialog.findViewById(R.id.readWrite);

        String isAdmin = UserDetails.getInstance().getRole();

        if (isAdmin != null && !isAdmin.trim().equals("Y")) {
            buttonReadWrite.setEnabled(false);
            buttonReadWrite.setVisibility(View.INVISIBLE);
        }

        buttonCreateWrite.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                createWrite("application/vnd.ms-excel", "ValidationTest.xls");
            }
        });

        buttonReadWrite.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                readWrite();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
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

    private void createWrite(String mimeType, String fileName) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        // Filter to only show results that can be "opened", such as
        // a file (as opposed to a list of contacts or timezones).
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        // Create a file with the requested MIME type.
        intent.setType(mimeType);
        intent.putExtra(Intent.EXTRA_TITLE, fileName);
        startActivityForResult(intent, WRITE_REQUEST_CODE);

    }

    private void readWrite() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, EDIT_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        Uri currentUri = null;
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == WRITE_REQUEST_CODE) {
                if (resultData != null) {
                    currentUri = resultData.getData();
                    boolean isFormatCreated = false;
                    ExcelCommunicator excelComm = new ExcelCommunicator(HomeActivity.this);
                    isFormatCreated = excelComm.exportDataToExcel(currentUri);
                    if (isFormatCreated) {
                        showCustomAlertDialog(true, "Excel Creation", "Excel file created successfully");
                    } else {
                        showCustomAlertDialog(true, "Excel Creation", "Failed to create excel file");
                    }
                }
            } else if (requestCode == EDIT_REQUEST_CODE) {
                if (resultData != null) {
                    currentUri = resultData.getData();
                    ArrayList <ArrayList <ExcelColumnInfo>> rowDetailsList = new ArrayList <ArrayList <ExcelColumnInfo>>();
                    ExcelCommunicator excelComm = new ExcelCommunicator(HomeActivity.this);
                    rowDetailsList = excelComm.importDataFromExcelToList(currentUri, rowDetailsList);
                    if (rowDetailsList != null && rowDetailsList.size() > 0) {
                        excelComm.validateModifyExcel(currentUri, rowDetailsList);
                    }
                }
            }
        }
    }

    private void showInOutModeDialog() {

        final Context context = HomeActivity.this;
        final Dialog dialog = new Dialog(context);
        dialog.setCanceledOnTouchOutside(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.in_out_mode);

        ImageView icon = (ImageView) dialog.findViewById(R.id.image);
        TextView title = (TextView) dialog.findViewById(R.id.title);

        final Spinner spinner_mode = (Spinner) dialog.findViewById(R.id.modeSpinner);
        final EditText etstart_Time = (EditText) dialog.findViewById(R.id.etStartTime);
        final EditText etend_Time = (EditText) dialog.findViewById(R.id.etEndTime);
        final ImageButton btn_StartTime = (ImageButton) dialog.findViewById(R.id.imageButton1);
        final ImageButton btn_EndTime = (ImageButton) dialog.findViewById(R.id.imageButton2);
        final Button btn_Save = (Button) dialog.findViewById(R.id.btn_Save);

        Button btn_Cancel = (Button) dialog.findViewById(R.id.btn_Cancel);

        icon.setImageResource(R.drawable.clock);
        title.setText("In Out Time");

        btn_Save.setTextColor(Color.parseColor("#474747"));
        btn_Save.setEnabled(false);

        btn_StartTime.setEnabled(false);
        btn_EndTime.setEnabled(false);

        btn_StartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(HomeActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {

                        String am_pm;
                        String strSelectedHour = Integer.toString(selectedHour);
                        String strSelectedMin = Integer.toString(selectedMinute);
                        if (strSelectedHour.trim().length() == 1) {
                            strSelectedHour = "0" + strSelectedHour;
                        }
                        if (strSelectedMin.trim().length() == 1) {
                            strSelectedMin = "0" + strSelectedMin;
                        }
                        if (selectedHour > 12) {
                            am_pm = "PM";
                        } else {
                            am_pm = "AM";
                        }
                        etstart_Time.setText(strSelectedHour + " : " + strSelectedMin + " " + am_pm);

                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Start Time");
                mTimePicker.show();

            }
        });

        btn_EndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(HomeActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        String am_pm;
                        String strSelectedHour = Integer.toString(selectedHour);
                        String strSelectedMin = Integer.toString(selectedMinute);
                        if (strSelectedHour.trim().length() == 1) {
                            strSelectedHour = "0" + strSelectedHour;
                        }
                        if (strSelectedMin.trim().length() == 1) {
                            strSelectedMin = "0" + strSelectedMin;
                        }
                        if (selectedHour > 12) {
                            am_pm = "PM";
                        } else {
                            am_pm = "AM";
                        }
                        etend_Time.setText(strSelectedHour + " : " + strSelectedMin + " " + am_pm);

                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select End Time");
                mTimePicker.show();
            }

        });

        ArrayAdapter adapter = new ArrayAdapter <String>(this, android.R.layout.simple_spinner_dropdown_item, Constants.ATTENDANCE_MODES);
        spinner_mode.setAdapter(adapter);
        spinner_mode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView <?> arg0, View arg1,
                                       int index, long arg3) {

                String strMode = Constants.ATTENDANCE_MODES[index];
                if (!strMode.equals("Select")) {
                    if (strMode.equals("In")) {
                        ArrayList <String> listInRange = dbComm.getInTimeRange();
                        if (listInRange != null) {
                            String strStartAM_PM;
                            String strEndAM_PM;
                            String strInStartime = listInRange.get(0).trim();
                            String strInEndTime = listInRange.get(1).trim();
                            int hourInStart = Integer.parseInt(strInStartime.substring(0, 2));
                            int minuteInStart = Integer.parseInt(strInStartime.substring(2));
                            int hourInEnd = Integer.parseInt(strInEndTime.substring(0, 2));
                            int minuteInEnd = Integer.parseInt(strInEndTime.substring(2));
                            if (hourInStart > 12) {
                                strStartAM_PM = "PM";
                            } else {
                                strStartAM_PM = "AM";
                            }
                            if (hourInEnd > 12) {
                                strEndAM_PM = "PM";
                            } else {
                                strEndAM_PM = "AM";
                            }
                            etstart_Time.setText(strInStartime.substring(0, 2) + " : " + strInStartime.substring(2) + " " + strStartAM_PM);
                            etend_Time.setText(strInEndTime.substring(0, 2) + " : " + strInEndTime.substring(2) + " " + strEndAM_PM);
                        } else {
                            etstart_Time.setText("");
                            etend_Time.setText("");

                        }
                    } else if (strMode.equalsIgnoreCase("Out")) {
                        ArrayList <String> listInRange = dbComm.getOutTimeRange();
                        if (listInRange != null) {
                            String strStartAM_PM;
                            String strEndAM_PM;
                            String strOutStartime = listInRange.get(0).trim();
                            String strOutEndTime = listInRange.get(1).trim();
                            int hourOutStart = Integer.parseInt(strOutStartime.substring(0, 2));
                            int minuteOutStart = Integer.parseInt(strOutStartime.substring(2));
                            int hourOutEnd = Integer.parseInt(strOutEndTime.substring(0, 2));
                            int minuteOutEnd = Integer.parseInt(strOutEndTime.substring(2));
                            if (hourOutStart > 12) {
                                strStartAM_PM = "PM";
                            } else {
                                strStartAM_PM = "AM";
                            }
                            if (hourOutEnd > 12) {
                                strEndAM_PM = "PM";
                            } else {
                                strEndAM_PM = "AM";
                            }
                            etstart_Time.setText(strOutStartime.substring(0, 2) + " : " + strOutStartime.substring(2) + " " + strStartAM_PM);
                            etend_Time.setText(strOutEndTime.substring(0, 2) + " : " + strOutEndTime.substring(2) + " " + strEndAM_PM);
                        } else {
                            etstart_Time.setText("");
                            etend_Time.setText("");
                        }
                    }
                    btn_Save.setTextColor(Color.parseColor("#FFFFFF"));
                    btn_Save.setEnabled(true);
                    btn_StartTime.setEnabled(true);
                    btn_EndTime.setEnabled(true);
                } else {
                    btn_Save.setTextColor(Color.parseColor("#474747"));
                    btn_Save.setEnabled(false);
                    btn_StartTime.setEnabled(false);
                    btn_EndTime.setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView <?> arg0) {
                // TODO Auto-generated method stub
            }
        });


        btn_Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String strSelectedMode = spinner_mode.getSelectedItem().toString().trim();
                SimpleDateFormat dateFormat = new SimpleDateFormat("HHmm");
                String strStartTime = etstart_Time.getText().toString().trim();
                String strEndTime = etend_Time.getText().toString().trim();
                try {
                    String strDigitStartTime = strStartTime.replaceAll("[^\\d.]", "").trim();
                    String strDigitEndTime = strEndTime.replaceAll("[^\\d.]", "").trim();
                    Date dtStartTime = dateFormat.parse(strDigitStartTime);
                    Date dtEndTime = dateFormat.parse(strDigitEndTime);
                    if ((strStartTime.contains("AM") && strEndTime.contains("AM")) || (strStartTime.contains("PM") && strEndTime.contains("PM")) || (strStartTime.contains("AM") && strEndTime.contains("PM"))) {
                        if (dtStartTime.before(dtEndTime)) {
                            if (strSelectedMode.equals("In")) {
                                ArrayList <String> listOutRange = dbComm.getOutTimeRange();
                                if (listOutRange != null) {
                                    String strOutStartime = listOutRange.get(0).trim();
                                    String strOutEndTime = listOutRange.get(1).trim();
                                    if (!Utility.isTimeBetweenTwoTime(strOutStartime, strOutEndTime, strDigitStartTime)) {
                                        if (!Utility.isTimeBetweenTwoTime(strOutStartime, strOutEndTime, strDigitEndTime)) {
                                            int status = -1;
                                            status = dbComm.deleteModeTime(Constants.IN_MODE_VALUE);
                                            if (status != -1) {
                                                status = dbComm.insertModeTime(strDigitStartTime, strDigitEndTime, Constants.IN_MODE_VALUE);
                                                if (status != -1) {
                                                    showCustomAlertDialog(true, "In Mode Time", "In Mode Time Saved Successfully");
                                                } else {
                                                    showCustomAlertDialog(false, "Error", "Failed to save in mode time");
                                                }
                                            } else {
                                                showCustomAlertDialog(false, "Error", "Failed to delete in mode time");
                                            }
                                        } else {
                                            showCustomAlertDialog(false, "Error", "In Mode End Time Is Within Range Of Out Mode Time");
                                        }
                                    } else {
                                        showCustomAlertDialog(false, "Error", "In Mode Start Time Is Within Range Of Out Mode Time");
                                    }
                                } else {
                                    int status = -1;
                                    status = dbComm.insertModeTime(strDigitStartTime, strDigitEndTime, Constants.IN_MODE_VALUE);
                                    if (status != -1) {
                                        showCustomAlertDialog(true, "In Mode Time", "In Mode Time Saved Successfully");
                                    } else {
                                        showCustomAlertDialog(false, "Error", "Failed to save in mode time");
                                    }
                                }
                            } else if (strSelectedMode.equals("Out")) {
                                ArrayList <String> listInRange = dbComm.getInTimeRange();
                                if (listInRange != null) {
                                    String strInStartime = listInRange.get(0).trim();
                                    String strInEndTime = listInRange.get(1).trim();
                                    if (!Utility.isTimeBetweenTwoTime(strInStartime, strInEndTime, strDigitStartTime)) {
                                        if (!Utility.isTimeBetweenTwoTime(strInStartime, strInEndTime, strDigitEndTime)) {
                                            int status = -1;
                                            status = dbComm.deleteModeTime(Constants.OUT_MODE_VALUE);
                                            if (status != -1) {
                                                status = dbComm.insertModeTime(strDigitStartTime, strDigitEndTime, Constants.OUT_MODE_VALUE);
                                                if (status != -1) {
                                                    showCustomAlertDialog(true, "Out Mode Time", "Out Mode Time Saved Successfully");
                                                } else {
                                                    showCustomAlertDialog(false, "Error", "Failed to save out mode time");
                                                }
                                            } else {
                                                showCustomAlertDialog(false, "Error", "Failed to delete out mode time");
                                            }
                                        } else {
                                            showCustomAlertDialog(false, "Error", "Out Mode End Time Is Within Range Of In Mode Time");
                                        }
                                    } else {
                                        showCustomAlertDialog(false, "Error", "Out Mode Start Time Is Within Range Of In Mode Time");
                                    }
                                } else {
                                    int status = -1;
                                    status = dbComm.insertModeTime(strDigitStartTime, strDigitEndTime, Constants.OUT_MODE_VALUE);
                                    if (status != -1) {
                                        showCustomAlertDialog(true, "Out Mode Time", "Out Mode Time Saved Successfully");
                                    } else {
                                        showCustomAlertDialog(false, "Error", "Failed to save out mode time");
                                    }
                                }
                            }
                        } else {
                            showCustomAlertDialog(false, "Error", "Start Time Cannot Be Greater Than Or Equal To End Time");
                        }
                    } else {
                        if (strSelectedMode.equals("In")) {
                            ArrayList <String> listOutRange = dbComm.getOutTimeRange();
                            if (listOutRange != null) {
                                String strOutStartime = listOutRange.get(0).trim();
                                String strOutEndTime = listOutRange.get(1).trim();
                                if (!Utility.isTimeBetweenTwoTime(strOutStartime, strOutEndTime, strDigitStartTime)) {
                                    if (!Utility.isTimeBetweenTwoTime(strOutStartime, strOutEndTime, strDigitEndTime)) {
                                        int status = -1;
                                        status = dbComm.deleteModeTime(Constants.IN_MODE_VALUE);
                                        if (status != -1) {
                                            status = dbComm.insertModeTime(strDigitStartTime, strDigitEndTime, Constants.IN_MODE_VALUE);
                                            if (status != -1) {
                                                showCustomAlertDialog(true, "In Mode Time", "In Mode Time Saved Successfully");
                                            } else {
                                                showCustomAlertDialog(false, "Error", "Failed to save in mode time");
                                            }
                                        } else {
                                            showCustomAlertDialog(false, "Error", "Failed to delete in mode time");
                                        }
                                    } else {
                                        showCustomAlertDialog(false, "Error", "In Mode End Time Is Within Range Of Out Mode Time");
                                    }
                                } else {
                                    showCustomAlertDialog(false, "Error", "In Mode Start Time Is Within Range Of Out Mode Time");
                                }

                            } else {
                                int status = -1;
                                status = dbComm.insertModeTime(strDigitStartTime, strDigitEndTime, Constants.IN_MODE_VALUE);
                                if (status != -1) {
                                    showCustomAlertDialog(true, "In Mode Time", "In Mode Time Saved Successfully");
                                } else {
                                    showCustomAlertDialog(false, "Error", "Failed to save in mode time");
                                }
                            }
                        } else if (strSelectedMode.equals("Out")) {
                            ArrayList <String> listInRange = dbComm.getInTimeRange();
                            if (listInRange != null) {
                                String strInStartime = listInRange.get(0).trim();
                                String strInEndTime = listInRange.get(1).trim();
                                if (!Utility.isTimeBetweenTwoTime(strInStartime, strInEndTime, strDigitStartTime)) {
                                    if (!Utility.isTimeBetweenTwoTime(strInStartime, strInEndTime, strDigitEndTime)) {
                                        int status = -1;
                                        status = dbComm.deleteModeTime(Constants.OUT_MODE_VALUE);
                                        if (status != -1) {
                                            status = dbComm.insertModeTime(strDigitStartTime, strDigitEndTime, Constants.OUT_MODE_VALUE);
                                            if (status != -1) {
                                                showCustomAlertDialog(true, "Out Mode Time", "Time Saved Successfully");
                                            } else {
                                                showCustomAlertDialog(false, "Error", "Failed to delete out mode time");
                                            }
                                        } else {
                                            showCustomAlertDialog(false, "Error", "Failed to delete out mode time");
                                        }
                                    } else {
                                        showCustomAlertDialog(false, "Error", "Out Mode End Time Is Within Range Of In Mode Time");
                                    }

                                } else {
                                    showCustomAlertDialog(false, "Error", "Out Mode Start Time Is Within Range Of In Mode Time");
                                }
                            } else {
                                int status = -1;
                                status = dbComm.insertModeTime(strDigitStartTime, strDigitEndTime, Constants.OUT_MODE_VALUE);
                                if (status != -1) {
                                    showCustomAlertDialog(true, "Out Mode Time", "Time Saved Successfully");
                                } else {
                                    showCustomAlertDialog(false, "Error", "Failed to delete out mode time");
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(HomeActivity.this, "Error:" + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        btn_Cancel.setOnClickListener(new View.OnClickListener() {
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

    /**
     * @return true is network is on else false, Added by Suman Dhara
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * @return true if internet is working else false, Added by Suman Dhara
     */
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
            super.onPostExecute(status);
            if (status) {
                updateInternetConnection("Internet Connected", true);
            } else {
                updateInternetConnection("No Internet Connection", false);
            }
        }
    }


    public void updateInternetConnection(final String message, final boolean status) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (status) {
                    serverConfig.setBackgroundColor(Color.parseColor("#72bf00"));
                    serverConfig.setTextColor(Color.parseColor("#ffffff"));
                    serverConfig.setPadding(5, 2, 5, 2);
                    serverConfig.setText(message);
                } else {
                    serverConfig.setBackgroundColor(Color.parseColor("#ff70d6"));
                    serverConfig.setTextColor(Color.parseColor("#ffffff"));
                    serverConfig.setPadding(5, 2, 5, 2);
                    serverConfig.setText(message);
                }
            }
        });
    }

    public void showCustomAlertDialog(boolean status, String strTitle, final String strMessage) {

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
            message.setTextColor(Color.parseColor("#006400"));
            icon.setImageResource(R.drawable.success);
        } else {
            message.setTextColor(Color.parseColor("#e60000"));
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

    @Override
    protected void onStop() {
        super.onStop();
        stopHandler();
    }

    @Override
    protected void onResume() {
        super.onResume();
        stopHandler();
        startTimer();
        startHandler();
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

    public void stopADCReceiver() {
        if (!isADCReceiverUnregistered) {
            isADCReceiverUnregistered = true;
            if (intent != null) {
                HomeActivity.this.stopService(intent);
            }
            if (receiver != null) {
                unregisterReceiver(receiver);
            }
        }
    }

    private void stopTimer() {

        if (recordUpdateTimer != null) {
            recordUpdateTimer.cancel();
            recordUpdateTimer.purge();
            recordUpdateTimer = null;
        }

        if (dateTimeUpdateTimer != null) {
            dateTimeUpdateTimer.cancel();
            dateTimeUpdateTimer.purge();
            dateTimeUpdateTimer = null;
        }

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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
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
