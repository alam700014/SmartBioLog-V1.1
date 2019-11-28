// The present software is not subject to the US Export Administration Regulations (no exportation license required), May 2012
// The present software is not subject to the US Export Administration Regulations (no exportation license required), May 2012
package com.android.fortunaattendancesystem.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.widget.TextView;

import com.android.fortunaattendancesystem.R;
import com.android.fortunaattendancesystem.constant.Constants;
import com.android.fortunaattendancesystem.forlinx.ForlinxGPIO;
import com.android.fortunaattendancesystem.info.ProcessInfo;
import com.android.fortunaattendancesystem.singleton.EmployeeFingerEnrollInfo;
import com.android.fortunaattendancesystem.singleton.Settings;
import com.android.fortunaattendancesystem.submodules.I2CCommunicator;
import com.android.fortunaattendancesystem.submodules.MorphoCommunicator;
import com.morpho.morphosmart.sdk.MorphoDatabase;
import com.morpho.morphosmart.sdk.MorphoDevice;

import java.util.Timer;
import java.util.TimerTask;

public class FingerEnrollUpdateDialogActivity extends Activity {

    private String enrollmentType = "";
    private String jobId = "";
    private String strFingerStatus = "";
    private String strVerificationMode = "";
    private String strFingerQuality = "";
    private String strSecurityLevel = "";
    private String strPin = "";
    private String strFirstFingerIndex = "";
    private String strSecondFingerIndex = "";
    private String strCorporateId = "";
    private String strImei = "";
    private String strDeviceToken = "";
    private String strServerIP = "";
    private String strServerPort = "";
    private String strUrl = "";

    public static boolean isEnrollStarted;
    private static Context context;

    private MorphoDevice morphoDevice;
    private MorphoDatabase morphoDatabase;
    private EmployeeFingerEnrollInfo empFingerInfo = null;
    private MorphoCommunicator morphoComm = null;
    public static int currentCaptureBitmapId = 0;
    private AlertDialog alertDialog = null;

    private static Handler hBrightness;
    private static Runnable rBrightness;

    private static boolean isLCDBackLightOff = false;
    private Handler cHandler = new Handler();
    private Timer capReadTimer = null;
    private TimerTask capReadTimerTask = null;
    private boolean isBreakFound = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process);

//        int fingerReader = Settings.getInstance().getFingerReaderTypeValue();
//        if (fingerReader == 2) {
//            AsyncTaskInit task = new AsyncTaskInit();
//            task.execute();
//        }

        context = getApplicationContext();

        Intent intent = getIntent();
        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
//                strFingerStatus = extras.getString("FS");
//                enrollmentType = extras.getString("ET");
//                strCorporateId = extras.getString("COID");
//                strImei = extras.getString("IMEI");
//                strDeviceToken = extras.getString("DT");
//                strServerIP = extras.getString("SIP");
//                strServerPort = extras.getString("SP");
//                strUrl = extras.getString("URL");
//                strFirstFingerIndex = extras.getString("FFI");
//                strSecondFingerIndex = extras.getString("SFI");
//                strFingerQuality = extras.getString("FQ");
//                strVerificationMode = extras.getString("VM");
//                strSecurityLevel = extras.getString("SL");
//                strPin = extras.getString("Pin");
//                jobId = extras.getString("JID");

                empFingerInfo = EmployeeFingerEnrollInfo.getInstance();
                int operation = extras.getInt("OP");
                if (operation == 1) { // Enroll
                    empFingerInfo.setEnrollType(extras.getString("ET"));
                    empFingerInfo.setRemoteEnrollJobId(extras.getString("JID"));
                    empFingerInfo.setEnroll(extras.getBoolean("IE"));
                    empFingerInfo.setNoOfFingers(extras.getInt("NOF"));
                    empFingerInfo.setEmpId(extras.getString("EID"));
                    empFingerInfo.setCardId(extras.getString("CID"));
                    empFingerInfo.setEmpName(extras.getString("EN"));
                    empFingerInfo.setStrFirstFingerIndex(extras.getString("FFI"));
                    empFingerInfo.setStrSecondFingerIndex(extras.getString("SFI"));
                    empFingerInfo.setStrSecurityLevel(extras.getString("SL"));
                    empFingerInfo.setStrVerificationMode(extras.getString("VM"));
                } else if (operation == 2) { // Update
                    empFingerInfo.setEnrollType(extras.getString("ET"));
                    empFingerInfo.setRemoteEnrollJobId(extras.getString("JID"));
                    empFingerInfo.setEnroll(extras.getBoolean("IE"));
                    empFingerInfo.setNoOfFingers(extras.getInt("NOF"));
                    empFingerInfo.setEmpId(extras.getString("EID"));
                    empFingerInfo.setCardId(extras.getString("CID"));
                    empFingerInfo.setEmpName(extras.getString("EN"));
                    empFingerInfo.setStrNewFirstFingerIndex(extras.getString("NFFI"));
                    empFingerInfo.setStrNewSecondFingerIndex(extras.getString("NSFI"));
                    empFingerInfo.setFingerIndex(extras.getInt("FIN"));
                }
            }
        }
        setFinishOnTouchOutside(false);
        morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
        morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
        morphoComm = new MorphoCommunicator(FingerEnrollUpdateDialogActivity.this);

        if (hBrightness == null) {
            hBrightness = new Handler();
        }

        rBrightness = new Runnable() {
            @Override
            public void run() {
                setScreenBrightness(Constants.BRIGHTNESS_OFF);
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

    public static void startHandler() {
        hBrightness.postDelayed(rBrightness, Constants.BRIGHTNESS_OFF_DELAY); //for 10 seconds
    }

    public static void stopHandler() {
        hBrightness.removeCallbacks(rBrightness);
    }

    // Change the screen brightness
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

    private void initializeEnrollUpdate() {
        int fingerReader = Settings.getInstance().getFrTypeValue();
        switch (fingerReader) {
            case 0://Morpho Finger Reader
                if (morphoDevice != null && morphoDatabase != null) {
                    empFingerInfo = EmployeeFingerEnrollInfo.getInstance();
                    if (empFingerInfo != null) {
                        currentCaptureBitmapId = R.id.imageView1;
                        int operation = empFingerInfo.getOperation();
                        if (operation == 1) {
                            boolean isEnroll = empFingerInfo.isEnroll();
                            if (isEnroll) {
                                setTitle("Finger Enroll");
                            } else {
                                setTitle("Finger Update");
                            }
                            int noOfFingers = empFingerInfo.getNoOfFingers();
                            if (noOfFingers == 1) {
                                setContentView(R.layout.onefinger_customdialog);
                                TextView tvFingerIndex = (TextView) findViewById(R.id.finger1);
                                String firstFingerIndex = empFingerInfo.getStrFirstFingerIndex().trim();
                                String secondFingerIndex = empFingerInfo.getStrSecondFingerIndex().trim();
                                if (firstFingerIndex.length() > 0) {
                                    tvFingerIndex.setText("Put Your " + firstFingerIndex);
                                } else if (secondFingerIndex.length() > 0) {
                                    tvFingerIndex.setText("Put Your " + secondFingerIndex);
                                }
                            } else if (noOfFingers == 2) {
                                setContentView(R.layout.twofinger_customdialog);
                                TextView tvFirstFingerIndex = (TextView) findViewById(R.id.finger1);
                                TextView tvSecondFingerIndex = (TextView) findViewById(R.id.finger2);
                                String firstFingerIndex = empFingerInfo.getStrFirstFingerIndex().trim();
                                String secondFingerIndex = empFingerInfo.getStrSecondFingerIndex().trim();
                                if (firstFingerIndex.length() > 0) {
                                    tvFirstFingerIndex.setText("Put Your " + firstFingerIndex);
                                }
                                if (secondFingerIndex.length() > 0) {
                                    tvSecondFingerIndex.setText("Put Your " + secondFingerIndex);
                                }
                            }
                            morphoComm.startFingerEnroll();
                        } else if (operation == 2) {
                            setTitle("Finger Update");
                            int noOfFingers = empFingerInfo.getNoOfFingers();
                            if (noOfFingers == 1) {
                                setContentView(R.layout.onefinger_customdialog);
                                TextView tvFingerIndex = (TextView) findViewById(R.id.finger1);
                                String firstFingerIndex = empFingerInfo.getStrNewFirstFingerIndex().trim();
                                String secondFingerIndex = empFingerInfo.getStrNewSecondFingerIndex().trim();
                                if (firstFingerIndex.length() > 0) {
                                    tvFingerIndex.setText("Put Your " + firstFingerIndex);
                                } else if (secondFingerIndex.length() > 0) {
                                    tvFingerIndex.setText("Put Your " + secondFingerIndex);
                                }
                            } else if (noOfFingers == 2) {
                                setContentView(R.layout.twofinger_customdialog);
                                TextView tvFirstFingerIndex = (TextView) findViewById(R.id.finger1);
                                TextView tvSecondFingerIndex = (TextView) findViewById(R.id.finger2);
                                String firstFingerIndex = empFingerInfo.getStrNewFirstFingerIndex().trim();
                                String secondFingerIndex = empFingerInfo.getStrNewSecondFingerIndex().trim();
                                if (firstFingerIndex.length() > 0) {
                                    tvFirstFingerIndex.setText("Put Your " + firstFingerIndex);
                                }
                                if (secondFingerIndex.length() > 0) {
                                    tvSecondFingerIndex.setText("Put Your " + secondFingerIndex);
                                }
                            }
                            morphoComm.startFingerUpdate();
                        }
                    } else {
                        alert(R.drawable.failure, "Error", "Employee finger info not found !");
                    }
                } else {
                    alert(R.drawable.failure, "Device Connection Status", "Finger Reader Not Found !");
                }
                break;

            case 1://For Aratek Finger Sensor
                break;

            case 2://For Startek Finger Sensor
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_process, menu);
        return true;
    }

    public void alert(int iconId, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message).setIcon(iconId)
                .setCancelable(false).setTitle(title)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        alertDialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
        alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        stopHandler();
        startHandler();
        startTimer();
        isEnrollStarted = true;
        initializeEnrollUpdate();
    }

    public void startTimer() {
        if (capReadTimer == null) {
            capReadTimer = new Timer();
            initializeTimerTask();
            capReadTimer.schedule(capReadTimerTask, 0, 50);
        }
    }

    private void initializeTimerTask() {
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

    @Override
    protected void onStop() {
        super.onStop();
        stopHandler();
        stopTimer();
        isEnrollStarted = false;
        morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
        morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
        if (morphoDevice != null && morphoDatabase != null) {
            if (ProcessInfo.getInstance().isCommandBioStart()) {
                morphoDevice.cancelLiveAcquisition();
            }
        }
    }

    private void stopTimer() {
        if (capReadTimer != null) {
            capReadTimer.cancel();
            capReadTimer.purge();
            capReadTimer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LoginSplashActivity.isLoaded = false;
    }
}

