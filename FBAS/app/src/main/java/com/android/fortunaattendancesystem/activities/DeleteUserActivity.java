package com.android.fortunaattendancesystem.activities;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.text.Html;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.fortunaattendancesystem.R;
import com.android.fortunaattendancesystem.adapter.DatabaseArrayAdapter;
import com.android.fortunaattendancesystem.adapter.DatabaseItem;
import com.android.fortunaattendancesystem.constant.Constants;
import com.android.fortunaattendancesystem.forlinx.ForlinxGPIO;
import com.android.fortunaattendancesystem.info.ProcessInfo;
import com.android.fortunaattendancesystem.singleton.RC632ReaderConnection;
import com.android.fortunaattendancesystem.singleton.Settings;
import com.android.fortunaattendancesystem.singleton.SmartReaderConnection;
import com.android.fortunaattendancesystem.submodules.ForlinxGPIOCommunicator;
import com.android.fortunaattendancesystem.submodules.I2CCommunicator;
import com.android.fortunaattendancesystem.submodules.MorphoCommunicator;
import com.android.fortunaattendancesystem.submodules.SQLiteCommunicator;
import com.android.fortunaattendancesystem.usbconnection.USBConnectionCreator;
import com.forlinx.android.GetValueService;
import com.forlinx.android.HardwareInterface;
import com.friendlyarm.SmartReader.SmartFinger;
import com.morpho.morphosmart.sdk.ErrorCodes;
import com.morpho.morphosmart.sdk.MorphoDatabase;
import com.morpho.morphosmart.sdk.MorphoDevice;
import com.morpho.morphosmart.sdk.MorphoUser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class DeleteUserActivity extends USBConnectionCreator {

    private Handler mHandler = new Handler();
    private MorphoDevice morphoDevice = null;
    private MorphoDatabase morphoDatabase = null;
    private MorphoCommunicator morphoComm = null;
    private SmartFinger rc632ReaderConnection = null;

    boolean isMorphoSmartRcvRegisterd = false;
    boolean isMorphoRcvRegistered = false;
    private boolean isSmartRcvRegisterd = false;
    SQLiteCommunicator dbComm = new SQLiteCommunicator();

    private boolean isDeleteSuccess = false;
    private DatabaseArrayAdapter databaseArrayAdapter;
    static List <DatabaseItem> databaseItems = null;
    private ListView databaseListView = null;
    Button btnRemoveUser, btnDeleteAll;
    ImageView smart_reader, finger_reader;
    int selectedIndex = -1;

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

        /*modifyActionBar();*/
        setContentView(R.layout.activity_delete_user);

        initLayoutElements();

        if (!Constants.isTab) {
            if (HardwareInterface.class != null) {
                receiver = new AdcMessageBroadcastReceiver();
                registerReceiver(receiver, getIntentFilter());
                intent = new Intent();
                intent.setClass(DeleteUserActivity.this, GetValueService.class);
                intent.putExtra("mtype", "ADC");
                intent.putExtra("maction", "start");
                intent.putExtra("mfd", 1);
                DeleteUserActivity.this.startService(intent);
            } else {
                Toast.makeText(getApplicationContext(), "Load hardwareinterface library error!", Toast.LENGTH_LONG).show();
            }
        }


        int fingerReader = -1, smartReader = -1;
        Settings settings = Settings.getInstance();
        fingerReader = settings.getFrTypeValue();
        smartReader = settings.getSrTypeValue();
        if (fingerReader == 0 && smartReader == 1) {
            initFingerSmart();
        } else {
            initFingerReader(fingerReader);
            initSmartReader(smartReader);
        }

        morphoComm = new MorphoCommunicator(DeleteUserActivity.this);
        morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();

        loadDatabaseItem();
        initDatabaseItem();

        // AsyncTaskLoadMorphoDatabase task = new AsyncTaskLoadMorphoDatabase();
        // task.execute();

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

            case 2:

                //======================== Startek FM200U Finger Sensor ===============================//
                //initUSBManagerReceiver();
                //unregisterReceivers();
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

                //========================= RC522 =====================================//

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

            case 5:

                //======================== Startek FM200U Reader ===========================//

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public synchronized void run() {
                        /**
                         * 4 => search only Startek Usb finger scanner
                         * */
                        searchDevices(4);
                    }
                }, 500);

                break;

            default:
                break;

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        stopHandler();
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
    protected void onStop() {
        super.onStop();
        stopHandler();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceivers();
        stopTimer();
        stopADCReceiver();
        LoginSplashActivity.isLoaded = false;
    }

    public void stopADCReceiver() {
        if (!isADCReceiverUnregistered) {
            isADCReceiverUnregistered = true;
            if (intent != null) {
                DeleteUserActivity.this.stopService(intent);
            }
            if (receiver != null) {
                unregisterReceiver(receiver);
            }
        }
    }

    private void stopTimer() {

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
                                            Intent intent = new Intent(DeleteUserActivity.this, EmployeeAttendanceActivity.class);
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
                                            Intent intent = new Intent(DeleteUserActivity.this, HomeActivity.class);
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

    private void unregisterReceivers() {

        if (isSmartRcvRegisterd) {
            try {
                if (mSmartReceiver != null) {
                    unregisterReceiver(mSmartReceiver);
                    isSmartRcvRegisterd = false;
                    mSmartReceiver = null;
                }
            } catch (Exception e) {
                Toast.makeText(this, "error in unregister smart receiver:" + e.getMessage(), Toast.LENGTH_LONG).show();
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

    public void initLayoutElements() {

        pbBatPer = (ProgressBar) findViewById(R.id.pbBatPer);
        tvBatPer = (TextView) findViewById(R.id.tvBatPer);
        tvPower = (TextView) findViewById(R.id.tvPower);

        ivChargeIcon = (ImageView) findViewById(R.id.ivChargeIcon);
        ivBatTop = (ImageView) findViewById(R.id.ivBatTop);

        databaseListView = (ListView) findViewById(R.id.databaselist);

        btnRemoveUser = (Button) findViewById(R.id.btn_removeuser);
        btnDeleteAll = (Button) findViewById(R.id.btn_deleteAll);

        smart_reader = (ImageView) findViewById(R.id.smartreader);
        finger_reader = (ImageView) findViewById(R.id.fingerreader);
    }

//    public int loadDatabaseItem() {
//        int ret = 0;
//        databaseItems = new ArrayList <DatabaseItem>();
//        int[] indexDescriptor = new int[3];
//        indexDescriptor[0] = 0;
//        indexDescriptor[1] = 1;
//        indexDescriptor[2] = 2;
//
//        MorphoUserList morphoUserList = new MorphoUserList();
//        ret = morphoDatabase.readPublicFields(indexDescriptor, morphoUserList);
//
//        if (ret == 0) {
//            int l_nb_user = morphoUserList.getNbUser();
//            for (int i = 0; i < l_nb_user; i++) {
//                MorphoUser morphoUser = morphoUserList.getUser(i);
//                String userID = morphoUser.getField(0);
//                String firstName = morphoUser.getField(1);
//                String lastName = morphoUser.getField(2);
//                databaseItems.add(new DatabaseItem(userID, firstName, lastName));
//            }
//        }
//        ProcessInfo.getInstance().setDatabaseItems(databaseItems);
//        ProcessInfo.getInstance().setCurrentNumberOfRecordValue(databaseItems.size());
//        return ret;
//    }

    public void loadDatabaseItem() {
        databaseItems = new ArrayList <DatabaseItem>();
        databaseItems = dbComm.getAllFingerEnrolledUser(databaseItems);
        ProcessInfo.getInstance().setDatabaseItems(databaseItems);
        ProcessInfo.getInstance().setCurrentNumberOfRecordValue(databaseItems.size());
    }


    private void initDatabaseItem() {
        final List <DatabaseItem> databaseItems = ProcessInfo.getInstance().getDatabaseItems();
        databaseArrayAdapter = new DatabaseArrayAdapter(this, R.layout.database_view, databaseItems);
        databaseListView.setAdapter(databaseArrayAdapter);
        databaseListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView <?> arg0, View view, int position, long id) {
                DatabaseItem selected = (DatabaseItem) databaseListView.getItemAtPosition(position);
                if (databaseItems != null && selected != null) {
                    int size = databaseItems.size();
                    for (int i = 0; i < size; i++) {
                        databaseItems.get(i).setSelected(false);
                        if (selected.compareTo(databaseItems.get(i)) == 0) {
                            selectedIndex = i;
                            ProcessInfo.getInstance().setDatabaseSelectedIndex(i);
                            databaseItems.get(i).setSelected(true);
                        }
                    }
                    for (int i = 0; i < databaseListView.getChildCount(); i++) {
                        View v = databaseListView.getChildAt(i);
                        if (v != null) {
                            v.setBackgroundColor(Color.TRANSPARENT);
                        }
                    }
                    view.setBackgroundColor(Color.CYAN);
                }
            }
        });

        ProcessInfo.getInstance().setCurrentNumberOfRecordValue(databaseItems.size());
        if (databaseItems.size() == 0) {
            btnRemoveUser.setEnabled(false);
            btnDeleteAll.setEnabled(false);
            btnRemoveUser.setTextColor(Color.parseColor("#474747"));
            btnDeleteAll.setTextColor(Color.parseColor("#474747"));
        } else {
            btnRemoveUser.setEnabled(true);
            btnDeleteAll.setEnabled(true);
            btnRemoveUser.setTextColor(Color.parseColor("#FFFFFF"));
            btnDeleteAll.setTextColor(Color.parseColor("#FFFFFF"));
        }
    }

    public void onDeleteUser(View view) {
        morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
        morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
        if (morphoDevice != null && morphoDatabase != null) {
            try {
                selectedIndex = ProcessInfo.getInstance().getDatabaseSelectedIndex();
                if (selectedIndex != -1) {
                    showCustomConfirmDialog(false, "Delete User", "Do You Want To Delete User ?");
                } else {
                    showCustomAlertDialog(false, "Error", "You Must Select User First");
                }
            } catch (Exception e) {
            } finally {
                ProcessInfo.getInstance().setDatabaseSelectedIndex(-1);
            }
        } else {
            showCustomAlertDialog(false, "Device Status", "Finger Reader Not Connected");
        }
    }


    public void onDeleteAll(View view) {
        morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
        morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
        if (morphoDevice != null && morphoDatabase != null) {
            final List <DatabaseItem> databaseItems = ProcessInfo.getInstance().getDatabaseItems();
            if (databaseItems != null && databaseItems.size() > 0) {
                showDialogForDeleteAll("Delete All", "Do You Want To Delete All Records ?");
            } else {
                Toast.makeText(this, "No Record Found", Toast.LENGTH_SHORT).show();
            }
        } else {
            showCustomAlertDialog(false, "Device Status", "Finger Reader Not Connected");
        }
    }

    public void modifyActionBar() {
        getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#e63900")));
        getActionBar().setTitle(Html.fromHtml("<b><font face='Calibri' color='#FFFFFF'>User Deletion</font></b>"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_delete_user, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.home) {
            stopADCReceiver();
            Intent previous = new Intent(DeleteUserActivity.this, HomeActivity.class);
            startActivity(previous);
            overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //===========================Custom Alert Dialog==============================================//

    int action = 1;

    //===========================Custom Alert Dialog==============================================//

    public void showCustomConfirmDialog(boolean status, String strTitle, String strMessage) {

        final Context context = this;
        final Dialog dialog = new Dialog(context);
        dialog.setCanceledOnTouchOutside(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_confirm_dialog_user_delete);

        ImageView icon = (ImageView) dialog.findViewById(R.id.image);
        TextView title = (TextView) dialog.findViewById(R.id.title);
        TextView message = (TextView) dialog.findViewById(R.id.message);
        RadioGroup rgDelete = (RadioGroup) dialog.findViewById(R.id.rgDelete);
        RadioButton rbEmpData = (RadioButton) dialog.findViewById(R.id.rbEmpData);
        RadioButton rbFingerData = (RadioButton) dialog.findViewById(R.id.rbFingerData);
        Button btn_No = (Button) dialog.findViewById(R.id.btnNo);
        Button btn_Yes = (Button) dialog.findViewById(R.id.btnYes);


        if (status == true) {
            icon.setImageResource(R.drawable.success);
        } else {
            icon.setImageResource(R.drawable.failure);
        }

        title.setText(strTitle);
        message.setText(strMessage);

        action = 1;
        rgDelete.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rbEmpData) {
                    action = 1;
                } else if (checkedId == R.id.rbFingerData) {
                    action = 2;
                }
            }
        });


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
                MorphoUser morphoUser = null;
                String empId = "";
                int ret = -1;
                switch (action) {
                    case 1://Emp and Finger Data Delete
                        morphoUser = new MorphoUser();
                        empId = ProcessInfo.getInstance().getDatabaseItems().get(selectedIndex).getId();
                        ret = morphoDatabase.getUser(empId, morphoUser);
                        if (ret == 0) {
                            ret = morphoUser.dbDelete();
                            if (ret == 0) {
                                int autoId = -1;
                                autoId = dbComm.getAutoIdByEmpId(empId);
                                if (autoId != -1) {
                                    int deletionStatus = -1;
                                    deletionStatus = dbComm.deleteEmployeeDataByAutoId(autoId);
                                    if (deletionStatus != -1) {
                                        deletionStatus = dbComm.deleteFingerRecordByAutoId(autoId);
                                        if (deletionStatus != -1) {
                                            isDeleteSuccess = true;
                                            showCustomAlertDialog(true, "User Deletion Status", "User Deleted Successfully");
                                        } else {
                                            showCustomAlertDialog(true, "User Deletion Status", "Failed To Delete User");
                                        }
                                    }
                                } else {
                                    showCustomAlertDialog(false, "Error", "User Finger Record Does Not Exist in Local Database");
                                }
                            } else {
                                showCustomAlertDialog(false, "Error", "Failed To Delete User From Morpho");
                            }
                        } else {
                            showCustomAlertDialog(false, "Error", Integer.toString(morphoDevice.getInternalError()));
                        }
                        break;
                    case 2://Finger Data Delete
                        morphoUser = new MorphoUser();
                        empId = ProcessInfo.getInstance().getDatabaseItems().get(selectedIndex).getId();
                        ret = morphoDatabase.getUser(empId, morphoUser);
                        if (ret == 0) {
                            ret = morphoUser.dbDelete();
                            if (ret == 0) {
                                int autoId = -1;
                                autoId = dbComm.getAutoIdByEmpId(empId);
                                if (autoId != -1) {
                                    int deletionStatus = -1;
                                    deletionStatus = dbComm.deleteFingerRecordByAutoId(autoId);
                                    if (deletionStatus != -1) {
                                        int updateStatus = -1;
                                        updateStatus = dbComm.updateFingerDataToEmpTableByEmpId(autoId);
                                        if (updateStatus != -1) {
                                            isDeleteSuccess = true;
                                            showCustomAlertDialog(true, "User Deletion Status", "User Deleted Successfully");
                                        } else {
                                            showCustomAlertDialog(true, "Error", "Failed to update finger data to employee table");
                                        }
                                    } else {
                                        showCustomAlertDialog(true, "User Deletion Status", "Failed To Delete User");
                                    }
                                } else {
                                    showCustomAlertDialog(false, "Error", "User Finger Record Does Not Exist in Local Database");
                                }
                            } else {
                                showCustomAlertDialog(false, "Error", "Failed To Delete User From Morpho");
                            }
                        } else {
                            showCustomAlertDialog(false, "Error", Integer.toString(morphoDevice.getInternalError()));
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

    public void showDialogForDeleteAll(String dialogTitle, String dialogMessage) {

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
                dialog.dismiss();
            }
        });

        btn_Yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                showPwdDialog();
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
                if (isDeleteSuccess) {
                    isDeleteSuccess = false;
                    ProcessInfo.getInstance().getDatabaseItems().remove(selectedIndex);
                    databaseArrayAdapter.notifyDataSetChanged();
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

    private void showPwdDialog() {

        final Context context = DeleteUserActivity.this;
        final Dialog passwordDialog = new Dialog(context);
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
                // isValid = true;
                //  strUsername = "admin";
                //  strPassword = "admin";
                isValid = validate(et_Username, et_Password);
                if (isValid) {
                    Cursor userData = null;
                    userData = dbComm.getUserData(strUsername, strPassword);
                    if (userData != null) {
                        if (userData.getCount() == 1) {
                            while (userData.moveToNext()) {
                                passwordDialog.dismiss();
                                int status = dbComm.deleteAllValidationRecords();
                                if (status != -1) {
                                    status = dbComm.deleteAllFingerRecords();
                                    if (status != -1) {
                                        status = MorphoCommunicator.deleteMorphoDatabase();
                                        if (status == ErrorCodes.MORPHO_OK) {
                                            databaseItems.clear();
                                            databaseArrayAdapter.notifyDataSetChanged();
                                            showCustomAlertDialog(true, "Status", "All Records Deleted Successfully !");
                                        } else {
                                            showCustomAlertDialog(false, "Status", "Failed To Delete From Morpho !");
                                        }
                                    } else {
                                        showCustomAlertDialog(false, "Status", "Failed To Delete From SQLite !");
                                    }
                                }
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
                passwordDialog.dismiss();

            }
        });


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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            stopADCReceiver();
//            Intent menu = new Intent(DeleteUserActivity.this, HomeActivity.class);
//            startActivity(menu);
//            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

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
