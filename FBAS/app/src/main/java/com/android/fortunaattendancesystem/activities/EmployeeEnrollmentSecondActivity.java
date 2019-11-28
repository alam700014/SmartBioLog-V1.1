package com.android.fortunaattendancesystem.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.fortunaattendancesystem.R;
import com.android.fortunaattendancesystem.constant.Constants;
import com.android.fortunaattendancesystem.forlinx.ForlinxGPIO;
import com.android.fortunaattendancesystem.model.EmployeeEnrollInfo;
import com.android.fortunaattendancesystem.singleton.UserDetails;
import com.android.fortunaattendancesystem.submodules.I2CCommunicator;
import com.android.fortunaattendancesystem.submodules.SQLiteCommunicator;

import java.util.Timer;
import java.util.TimerTask;

public class EmployeeEnrollmentSecondActivity extends Activity {

    private static InputMethodManager inputMethodManager;
    private EditText editTextEnrollStatus, editTextNosFinger, editTextPin, editTextSmtcardVer, editTextVerificationMode;
    private Spinner spinnerGroupNames, spinnerSiteCode, spinnerTrainingCenter, spinnerBatch, spinnerBloodGroup, spinnerIsAccessRightEnabled, spinnerIsBlacklisted, spinnerIsLockOpenWhenAllowed;
    private TextView tvCSN;
    private ArrayAdapter <String> adapter;
    private String[] groupNames = null;
    private int[] pkGroupNames = null;
    private String[] siteCodes = null;
    private int[] pkSiteCodes = null;
    private String[] trainingCenters = null;
    private int[] pkTrainingCenters = null;
    private String[] batchNames = null;
    private int[] pkBatchNames = null;
    private String strSelectedGrpName, strSelectedSiteCodeName, strSelectedTrainingCenterName, strSelectedBatchName;
    private int intSelectedGrpPk, intSelectedSitePk, intSelectedTrainingPk, intSelectedBatckPk;
    private final static String BLANK = "";

    private SQLiteCommunicator dbComm = new SQLiteCommunicator();

    EmployeeEnrollInfo empInfo = null;

    private boolean isBreakFound = true;
    private static boolean isLCDBackLightOff = false;
    private Handler cHandler = new Handler();
    private Timer capReadTimer = null;
    private TimerTask capReadTimerTask = null;

    private Handler hBrightness, hLCDBacklight;
    private Runnable rBrightness, rLCDBacklight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setContentView(R.layout.activity_employee_enrollment_second);
        findViewById(android.R.id.content).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideSoftKeyboard(EmployeeEnrollmentSecondActivity.this);
                return false;
            }
        });
        findViewById(R.id.emp_enroll_scrollview).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideSoftKeyboard(EmployeeEnrollmentSecondActivity.this);
                return false;
            }
        });
        initLayoutElements();
        fillSpinnerItems();
        disableItemsOnStartUp();

        Intent intent = getIntent();
        if (intent != null) {
            empInfo = new EmployeeEnrollInfo();
            empInfo = intent.getExtras().getParcelable("EmployeeEnrollInfo");
            empInfo = dbComm.setEmpExtraDetails(empInfo);
            addValuesToUI(empInfo);
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

    private void initLayoutElements() {
        inputMethodManager = (InputMethodManager) EmployeeEnrollmentSecondActivity.this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        editTextPin = (EditText) findViewById(R.id.PIN);
        editTextEnrollStatus = (EditText) findViewById(R.id.enrollstatus);
        editTextNosFinger = (EditText) findViewById(R.id.nosfinger);
        editTextSmtcardVer = (EditText) findViewById(R.id.SmartCardVersion);
        tvCSN = (TextView) findViewById(R.id.tvcsn);
        editTextVerificationMode = (EditText) findViewById(R.id.Varification);

        spinnerSiteCode = (Spinner) findViewById(R.id.sitecode);
        spinnerGroupNames = (Spinner) findViewById(R.id.GroupId);
        spinnerIsAccessRightEnabled = (Spinner) findViewById(R.id.IsAccessRightEnabled);
        spinnerIsBlacklisted = (Spinner) findViewById(R.id.IsBlacklisted);
        spinnerIsLockOpenWhenAllowed = (Spinner) findViewById(R.id.IsLockOpenWhenAllowed);
        spinnerTrainingCenter = (Spinner) findViewById(R.id.TrainingCenterName);
        spinnerBatch = (Spinner) findViewById(R.id.BatchName);
    }

    private void fillSpinnerItems() {
        fillIsBlacklstd();   // isblacklisted
        fillIsAccessRightEnb();  // Is access Right Enable
        fillIsLockOpenWhenAllow(); // IsLockOpenWhenAllow
        fillBatch();
        fillTrainingCenters();
        fillSiteCodes();
        fillGroupNames();
    }

    private void disableItemsOnStartUp() {
        editTextEnrollStatus.setEnabled(false);
        editTextNosFinger.setEnabled(false);
        editTextSmtcardVer.setEnabled(false);
        editTextVerificationMode.setEnabled(false);

        //Default values IsBlackListed=No,IsAccessRightEnabled=Yes,IsLockOpenWhenAllowed=Yes //

        spinnerIsBlacklisted.setSelection(2);
        spinnerIsAccessRightEnabled.setSelection(1);
        spinnerIsLockOpenWhenAllowed.setSelection(1);

        String role = UserDetails.getInstance().getRole();
        if (role.equals("Y")) {
            spinnerIsBlacklisted.setEnabled(true);
            spinnerIsAccessRightEnabled.setEnabled(true);
            spinnerIsLockOpenWhenAllowed.setEnabled(true);
        } else if (role.equals("N")) {
            spinnerIsBlacklisted.setEnabled(false);
            spinnerIsAccessRightEnabled.setEnabled(false);
            spinnerIsLockOpenWhenAllowed.setEnabled(false);
        }

    }

    public void addValuesToUI(EmployeeEnrollInfo empInfo) {
        String strColumnValue = empInfo.getPin();
        if (strColumnValue != null && strColumnValue.trim().length() > 0) {
            editTextPin.setText(strColumnValue);
        } else {
            editTextPin.setText("");
        }

        int intColumnValue = empInfo.getIsBlackListed();
        if (intColumnValue != 0) {
            spinnerIsBlacklisted.setSelection(intColumnValue);
        } else {
            spinnerIsBlacklisted.setSelection(intColumnValue + 2);
        }
        intColumnValue = empInfo.getIsAccessRightEnabled();
        if (intColumnValue != 0) {
            spinnerIsAccessRightEnabled.setSelection(intColumnValue);
        } else {
            spinnerIsAccessRightEnabled.setSelection(intColumnValue + 2);
        }
        intColumnValue = empInfo.getIsLockOpen();
        if (intColumnValue != 0) {
            spinnerIsLockOpenWhenAllowed.setSelection(intColumnValue);
        } else {
            spinnerIsLockOpenWhenAllowed.setSelection(intColumnValue + 2);
        }

        strColumnValue = empInfo.getGroupId();
        if (strColumnValue != null && strColumnValue.trim().length() > 0) {
            String strGroupName = dbComm.getGroupNameById(strColumnValue);
            if (strGroupName.trim().length() > 0) {
                spinnerGroupNames.setSelection(((ArrayAdapter <String>) spinnerGroupNames.getAdapter()).getPosition(strGroupName.trim()));
            } else {
                spinnerGroupNames.setSelection(0);
            }
        } else {
            spinnerGroupNames.setSelection(0);
        }
        strColumnValue = empInfo.getSiteCode();
        if (strColumnValue != null && strColumnValue.trim().length() > 0) {
            String strSiteCode = dbComm.getSiteCodeById(strColumnValue);
            if (strSiteCode.trim().length() > 0) {
                spinnerSiteCode.setSelection(((ArrayAdapter <String>) spinnerSiteCode.getAdapter()).getPosition(strSiteCode.trim()));
            } else {
                spinnerSiteCode.setSelection(0);
            }
        } else {
            spinnerSiteCode.setSelection(0);
        }
        strColumnValue = empInfo.getTrainingCenterId();
        if (strColumnValue != null && strColumnValue.trim().length() > 0) {
            String trainingCode = dbComm.getTrainingCenterNameById(strColumnValue);
            if (trainingCode.trim().length() > 0) {
                spinnerTrainingCenter.setSelection(((ArrayAdapter <String>) spinnerTrainingCenter.getAdapter()).getPosition(trainingCode.trim()));
            } else {
                spinnerTrainingCenter.setSelection(0);
            }
        } else {
            spinnerTrainingCenter.setSelection(0);
        }
        strColumnValue = empInfo.getBatchId();
        if (strColumnValue != null && strColumnValue.trim().length() > 0) {
            String strBatchName = dbComm.getBatchNameById(strColumnValue);
            if (strBatchName.trim().length() > 0) {
                spinnerBatch.setSelection(((ArrayAdapter <String>) spinnerBatch.getAdapter()).getPosition(strBatchName.trim()));
            } else {
                spinnerBatch.setSelection(0);
            }
        } else {
            spinnerBatch.setSelection(0);
        }
        strColumnValue = empInfo.getSmartCardVer();
        if (strColumnValue != null && strColumnValue.trim().length() > 0 && !strColumnValue.equals("-1")) {
            editTextSmtcardVer.setText(strColumnValue);
        } else {
            editTextSmtcardVer.setText("");
        }
        strColumnValue = empInfo.getCSN();
        if (strColumnValue != null && strColumnValue.trim().length() > 0) {
            tvCSN.setText(strColumnValue);
        } else {
            tvCSN.setText("");
        }
        boolean status = empInfo.isFingerEnrolled();
        if (status) {
            editTextEnrollStatus.setText("Yes");
        } else {
            editTextEnrollStatus.setText("No");
        }
        intColumnValue = empInfo.getNoOfFingersEnrolled();
        if (intColumnValue > 0) {
            editTextNosFinger.setText(Integer.toString(intColumnValue));
        } else {
            editTextNosFinger.setText("NA");
        }
        strColumnValue = empInfo.getVerificationMode();
        if (strColumnValue != null && strColumnValue.trim().length() > 0) {
            editTextVerificationMode.setText(strColumnValue);
        } else {
            editTextVerificationMode.setText("NA");
        }

    }

    public void fillBatch() {
        int noOfRecords = -1;
        noOfRecords = dbComm.getBatchNamesCount();
        if (noOfRecords > 0) {
            batchNames = new String[noOfRecords + 1];
            pkBatchNames = new int[noOfRecords + 1];
            batchNames[0] = "Select";
            pkBatchNames[0] = -1;
            dbComm.fillBatchNames(batchNames, pkBatchNames);
        } else {
            batchNames = new String[1];
            pkBatchNames = new int[1];
            batchNames[0] = "Select";
            pkBatchNames[0] = -1;
        }
        adapter = new ArrayAdapter <String>(this, android.R.layout.simple_spinner_dropdown_item, batchNames);
        spinnerBatch.setAdapter(adapter);
        spinnerBatch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView <?> arg0, View arg1,
                                       int arg2, long arg3) {
                strSelectedBatchName = batchNames[arg2];
                intSelectedBatckPk = pkBatchNames[arg2];
            }

            @Override
            public void onNothingSelected(AdapterView <?> arg0) {
                // TODO Auto-generated method stub
            }
        });

    }

    public void fillTrainingCenters() {
        int noOfRecords = -1;
        noOfRecords = dbComm.getTrainingCenterCount();
        if (noOfRecords > 0) {
            trainingCenters = new String[noOfRecords + 1];
            pkTrainingCenters = new int[noOfRecords + 1];
            trainingCenters[0] = "Select";
            pkTrainingCenters[0] = -1;
            dbComm.fillTrainingCenters(trainingCenters, pkTrainingCenters);
        } else {
            trainingCenters = new String[1];
            pkTrainingCenters = new int[1];
            trainingCenters[0] = "Select";
            pkTrainingCenters[0] = -1;
        }
        adapter = new ArrayAdapter <String>(this, android.R.layout.simple_spinner_dropdown_item, trainingCenters);
        spinnerTrainingCenter.setAdapter(adapter);
        spinnerTrainingCenter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView <?> arg0, View arg1,
                                       int arg2, long arg3) {
                strSelectedTrainingCenterName = trainingCenters[arg2];
                intSelectedTrainingPk = pkTrainingCenters[arg2];
            }

            @Override
            public void onNothingSelected(AdapterView <?> arg0) {
                // TODO Auto-generated method stub
            }
        });
    }

    public void fillSiteCodes() {
        int noOfRecords = -1;
        noOfRecords = dbComm.getSiteCodeCount();
        if (noOfRecords > 0) {
            siteCodes = new String[noOfRecords + 1];
            pkSiteCodes = new int[noOfRecords + 1];
            siteCodes[0] = "Select";
            pkSiteCodes[0] = -1;
            dbComm.fillSiteCodes(siteCodes, pkSiteCodes);
        } else {
            siteCodes = new String[1];
            pkSiteCodes = new int[1];
            siteCodes[0] = "Select";
            pkSiteCodes[0] = -1;

        }
        adapter = new ArrayAdapter <String>(this, android.R.layout.simple_spinner_dropdown_item, siteCodes);
        spinnerSiteCode.setAdapter(adapter);
        spinnerSiteCode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView <?> arg0, View arg1,
                                       int arg2, long arg3) {
                strSelectedSiteCodeName = siteCodes[arg2];
                intSelectedSitePk = pkSiteCodes[arg2];
            }

            @Override
            public void onNothingSelected(AdapterView <?> arg0) {
                // TODO Auto-generated method stub
            }
        });
    }

    public void fillGroupNames() {
        int noOfRecords = -1;
        noOfRecords = dbComm.getGroupNamesCount();
        if (noOfRecords > 0) {
            groupNames = new String[noOfRecords + 1];
            pkGroupNames = new int[noOfRecords + 1];
            groupNames[0] = "Select";
            pkGroupNames[0] = -1;
            dbComm.fillGroupNames(groupNames, pkGroupNames);
        } else {
            groupNames = new String[1];
            pkGroupNames = new int[1];
            groupNames[0] = "Select";
            pkGroupNames[0] = -1;
        }
        adapter = new ArrayAdapter <String>(this, android.R.layout.simple_spinner_dropdown_item, groupNames);
        spinnerGroupNames.setAdapter(adapter);
        spinnerGroupNames.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView <?> arg0, View arg1,
                                       int arg2, long arg3) {
                strSelectedGrpName = groupNames[arg2];
                intSelectedGrpPk = pkGroupNames[arg2];
            }

            @Override
            public void onNothingSelected(AdapterView <?> arg0) {
                // TODO Auto-generated method stub
            }
        });
    }

    public void fillIsBlacklstd() {
        adapter = new ArrayAdapter <String>(this, android.R.layout.simple_spinner_item, Constants.IS_BLACK_LISTED);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerIsBlacklisted.setAdapter(adapter);
    }

    public void fillIsAccessRightEnb() {
        adapter = new ArrayAdapter <String>(this, android.R.layout.simple_spinner_item, Constants.IS_ACCESS_RIGHT_ENABLED);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerIsAccessRightEnabled.setAdapter(adapter);
    }

    public void fillIsLockOpenWhenAllow() {
        adapter = new ArrayAdapter <String>(this, android.R.layout.simple_spinner_item, Constants.IS_LOCK_OPEN_WHEN_ALLOWED);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerIsLockOpenWhenAllowed.setAdapter(adapter);
    }

    public void fillEmployeeEnrollData() {
        String value = editTextPin.getText().toString();
        if (value != null && value.trim().length() > 0) {
            empInfo.setPin(value);
        } else {
            empInfo.setPin(BLANK);
        }
        if (!strSelectedGrpName.equals("Select") && intSelectedGrpPk != -1) {
            empInfo.setGroupId(Integer.toString(intSelectedGrpPk));
            empInfo.setGroupName(strSelectedGrpName);
        } else {
            empInfo.setGroupId(BLANK);
            empInfo.setGroupName(BLANK);
        }
        if (!strSelectedSiteCodeName.equals("Select") && intSelectedSitePk != -1) {
            empInfo.setSiteCode(Integer.toString(intSelectedSitePk));
            empInfo.setSiteName(strSelectedSiteCodeName);
        } else {
            empInfo.setSiteCode(BLANK);
            empInfo.setSiteName(BLANK);
        }
        if (!strSelectedBatchName.equals("Select") && intSelectedBatckPk != -1) {
            empInfo.setBatchId(Integer.toString(intSelectedBatckPk));
            empInfo.setBatchName(strSelectedBatchName);
        } else {
            empInfo.setBatchId(BLANK);
            empInfo.setBatchName(BLANK);
        }
        if (!strSelectedTrainingCenterName.equals("Select") && intSelectedTrainingPk != -1) {
            empInfo.setTrainingCenterId(Integer.toString(intSelectedTrainingPk));
            empInfo.setTrainingCenterName(strSelectedTrainingCenterName);
        } else {
            empInfo.setTrainingCenterId(BLANK);
            empInfo.setTrainingCenterName(BLANK);
        }
        value = spinnerIsAccessRightEnabled.getSelectedItem().toString();
        if (!value.equals("Select")) {
            if (value.equals("Yes")) {
                empInfo.setIsAccessRightEnabled(1);
            } else if (value.equals("No")) {
                empInfo.setIsAccessRightEnabled(0);
            }
        } else {
            empInfo.setIsAccessRightEnabled(1);//by default Yes
        }
        value = spinnerIsBlacklisted.getSelectedItem().toString();
        if (!value.equals("Select")) {
            if (value.equals("Yes")) {
                empInfo.setIsBlackListed(1);
            } else if (value.equals("No")) {
                empInfo.setIsBlackListed(0);
            }
        } else {
            empInfo.setIsBlackListed(0);//by default No
        }
        value = spinnerIsLockOpenWhenAllowed.getSelectedItem().toString();
        if (!value.equals("Select")) {
            if (value.equals("Yes")) {
                empInfo.setIsLockOpen(1);
            } else if (value.equals("No")) {
                empInfo.setIsLockOpen(0);
            }
        } else {
            empInfo.setIsLockOpen(1);//by Default Yes
        }
        value = editTextEnrollStatus.getText().toString();
        if (value.trim().length() > 0 && value.equals("Yes")) {
            empInfo.setFingerEnrolled(true);
        } else {
            empInfo.setFingerEnrolled(false);
        }
        value = editTextNosFinger.getText().toString();
        if (value.trim().length() > 0) {
            try {
                empInfo.setNoOfFingersEnrolled(Integer.parseInt(value));
            } catch (NumberFormatException ne) {
                empInfo.setNoOfFingersEnrolled(0);
            }
        } else {
            empInfo.setNoOfFingersEnrolled(0);
        }

        empInfo.setCSN(tvCSN.getText().toString());
        empInfo.setSmartCardVer(editTextSmtcardVer.getText().toString());
        empInfo.setVerificationMode(editTextVerificationMode.getText().toString());
    }

    public void clearUIValues() {
        spinnerSiteCode.setSelection(0);
        editTextPin.setText("");
        spinnerGroupNames.setSelection(0);
        editTextEnrollStatus.setText("");
        editTextNosFinger.setText("");
        editTextSmtcardVer.setText("");
        editTextVerificationMode.setText("");
        spinnerIsBlacklisted.setSelection(0);
        spinnerIsAccessRightEnabled.setSelection(0);
        spinnerIsLockOpenWhenAllowed.setSelection(0);
        spinnerTrainingCenter.setSelection(0);
        spinnerBatch.setSelection(0);
    }

    public void next(View view) {
        fillEmployeeEnrollData();
        Intent intent = new Intent(EmployeeEnrollmentSecondActivity.this, EmployeeEnrollmentPreviewActivity.class);
        intent.putExtra("EmployeeEnrollInfo", empInfo);
        startActivity(intent);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
    }


    public void previous(View view) {
        finish();
    }

    public void hideSoftKeyboard(Activity activity) {
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_form_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.home) {
            Intent previous = new Intent(EmployeeEnrollmentSecondActivity.this, HomeActivity.class);
            startActivity(previous);
            overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            finish();
            return true;
        }
        if (id == R.id.back) {
            clearUIValues();
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onResume() {
        super.onResume();
        stopHandler();
        startHandler();
        startTimer();
    }

    public void startTimer() {
        if (capReadTimer == null) {
            capReadTimer = new Timer();
            initializeTimerTask();
            capReadTimer.schedule(capReadTimerTask, 0, 250);//50
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
                                        } else {
                                            Intent intent = new Intent(EmployeeEnrollmentSecondActivity.this, EmployeeAttendanceActivity.class);
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
                                            Intent intent = new Intent(EmployeeEnrollmentSecondActivity.this, HomeActivity.class);
                                            startActivity(intent);
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
    }

    public void stopTimer() {
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
