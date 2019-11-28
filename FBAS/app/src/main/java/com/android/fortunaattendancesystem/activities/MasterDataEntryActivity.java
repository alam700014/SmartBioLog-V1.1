package com.android.fortunaattendancesystem.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.InputFilter;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.fortunaattendancesystem.R;
import com.android.fortunaattendancesystem.constant.Constants;
import com.android.fortunaattendancesystem.forlinx.ForlinxGPIO;
import com.android.fortunaattendancesystem.submodules.I2CCommunicator;
import com.android.fortunaattendancesystem.submodules.SQLiteCommunicator;

import java.util.Timer;
import java.util.TimerTask;


public class MasterDataEntryActivity extends Activity {

    EditText groupName, groupProperty, siteCode, batchNo, batchName, trainingCenterNo, trainingCenterName;
    Button btnGroup, btnSite, btnBatch, btnTraining;
    String strGroupName, strGroupProperty, strSiteCode, strBatchNo, strBatchName, strTrainingNo, strTraningName;
    SQLiteCommunicator dbComm = new SQLiteCommunicator();

    private static boolean isLCDBackLightOff = false;
    private Handler cHandler = new Handler();
    private Timer capReadTimer = null;
    private TimerTask capReadTimerTask = null;
    private boolean isBreakFound = true;

    private Handler hBrightness, hLCDBacklight;
    private Runnable rBrightness, rLCDBacklight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        /*modifyActionBar();*/

        setContentView(R.layout.activity_master_data_entry);

        /*findViewById(android.R.id.content).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                hideSoftKeyboard(MasterDataEntryActivity.this);

                return false;
            }
        });*/ /*Modified by Sanjay Shyamal on 22/11/2017*/

        findViewById(R.id.scrollView_master_data).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideSoftKeyboard(MasterDataEntryActivity.this);
                return false;
            }
        }); /*Addeed by Sanjay Shyamal on 22/11/2017*/

        initLayoutElements();
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

    public void initLayoutElements() {

        groupName = (EditText) findViewById(R.id.GroupName);
        groupProperty = (EditText) findViewById(R.id.GroupProperty);

        siteCode = (EditText) findViewById(R.id.SiteCode);

        batchNo = (EditText) findViewById(R.id.BatchNumber);
        batchName = (EditText) findViewById(R.id.BatchName);

        trainingCenterNo = (EditText) findViewById(R.id.TrainigCenterNo);
        trainingCenterName = (EditText) findViewById(R.id.TrainingCenterName);

        groupName.setFilters(new InputFilter[]{new InputFilter.AllCaps(), Constants.EMOJI_FILTER, new InputFilter.LengthFilter(8)});
        batchNo.setFilters(new InputFilter[]{new InputFilter.AllCaps(), Constants.EMOJI_FILTER, new InputFilter.LengthFilter(16)});
        batchName.setFilters(new InputFilter[]{new InputFilter.AllCaps(), Constants.EMOJI_FILTER, new InputFilter.LengthFilter(25)});
        trainingCenterNo.setFilters(new InputFilter[]{new InputFilter.AllCaps(), Constants.EMOJI_FILTER, new InputFilter.LengthFilter(16)});
        trainingCenterName.setFilters(new InputFilter[]{new InputFilter.AllCaps(), Constants.EMOJI_FILTER, new InputFilter.LengthFilter(25)});

        btnGroup = (Button) findViewById(R.id.btnSaveGroup);
        btnSite = (Button) findViewById(R.id.btnSaveSite);
        btnBatch = (Button) findViewById(R.id.btnSaveBatch);
        btnTraining = (Button) findViewById(R.id.btnSaveTraining);

        btnGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                strGroupName = groupName.getText().toString();
                strGroupProperty = groupProperty.getText().toString();
                if (strGroupName != null && strGroupName.trim().length() > 0) {
                    if (strGroupProperty != null && strGroupProperty.trim().length() > 0) {
                        showCustomConfirmDialog(true, "Data Save", "Do You Want To Save Group Data?", "Group");
                    } else {
                        showCustomAlertDialog(false, "Error", "Enter Group Property", "");
                    }
                } else {
                    showCustomAlertDialog(false, "Error", "Enter Group Name", "");
                }
            }
        });

        btnSite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                strSiteCode = siteCode.getText().toString();
                if (strSiteCode != null && strSiteCode.trim().length() > 0) {
                    showCustomConfirmDialog(true, "Data Save", "Do You Want To Save Site Code?", "Site");
                } else {
                    showCustomAlertDialog(false, "Error", "Enter Site Code", "");
                }
            }
        });

        btnBatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                strBatchNo = batchNo.getText().toString();
                strBatchName = batchName.getText().toString();
                if (strBatchNo != null && strBatchNo.trim().length() > 0) {
                    if (strBatchName != null && strBatchName.trim().length() > 0) {
                        showCustomConfirmDialog(true, "Data Save", "Do You Want To Save Batch Data?", "Batch");
                    } else {
                        showCustomAlertDialog(false, "Error", "Enter Batch Name", "");
                    }
                } else {
                    showCustomAlertDialog(false, "Error", "Enter Batch No", "");
                }
            }
        });

        btnTraining.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                strTrainingNo = trainingCenterNo.getText().toString();
                strTraningName = trainingCenterName.getText().toString();
                if (strTrainingNo != null && strTrainingNo.trim().length() > 0) {
                    if (strTraningName != null && strTraningName.trim().length() > 0) {
                        showCustomConfirmDialog(true, "Data Save", "Do You Want To Save Training Center Data?", "Training");
                    } else {
                        showCustomAlertDialog(false, "Error", "Enter Training Center Name", "");
                    }
                } else {
                    showCustomAlertDialog(false, "Error", "Enter Training Center No", "");
                }
            }
        });
    }

    public void minimizeKeyBoard(View view) {
        hideSoftKeyboard(MasterDataEntryActivity.this);
    }

    public void modifyActionBar() {
        getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#e63900")));
        getActionBar().setTitle(Html.fromHtml("<b><font face='Calibri' color='#FFFFFF'>Master Data Entry</font></b>"));
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_master_data_entry, menu);
        return true;
    }


    @Override
    protected void onResume() {
        super.onResume();
        stopHandler();
        startHandler();
        startTimer();
    }

    private void startTimer() {
        if (capReadTimer == null) {
            capReadTimer = new Timer();
            initializeTimerTask();
            capReadTimer.schedule(capReadTimerTask, 0, 50);
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
        stopTimer();
        LoginSplashActivity.isLoaded = false;
    }

    private void stopTimer() {
        if (capReadTimer != null) {
            capReadTimer.cancel();
            capReadTimer.purge();
            capReadTimer = null;
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
                                            Intent intent = new Intent(MasterDataEntryActivity.this, EmployeeAttendanceActivity.class);
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
                                            Intent intent = new Intent(MasterDataEntryActivity.this, HomeActivity.class);
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


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.home) {
            Intent previous = new Intent(MasterDataEntryActivity.this, HomeActivity.class);
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

    public void clear() {
        groupName.setText("");
        groupProperty.setText("");
        siteCode.setText("");
        batchNo.setText("");
        batchName.setText("");
        trainingCenterNo.setText("");
        trainingCenterName.setText("");
    }

    public void showCustomConfirmDialog(boolean status, String strTitle, String strMessage, final String strOperation) {

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
                int insertStatus = -1;
                switch (strOperation) {
                    case "Group":
                        insertStatus = dbComm.saveGroupData(strGroupName, strGroupProperty);
                        if (insertStatus != -1) {
                            showCustomAlertDialog(true, "Data Saved Status", "Data Saved Successfully", "Group");
                        } else {
                            showCustomAlertDialog(false, "Data Saved Status", "Failed To Save Data", "");
                        }
                        break;
                    case "Site":
                        insertStatus = dbComm.saveSiteData(strSiteCode);
                        if (insertStatus != -1) {
                            showCustomAlertDialog(true, "Data Saved Status", "Data Saved Successfully", "Site");
                        } else {
                            showCustomAlertDialog(false, "Data Saved Status", "Failed To Save Data", "");
                        }
                        break;
                    case "Batch":
                        insertStatus = dbComm.saveBatchData(strBatchNo, strBatchName);
                        if (insertStatus != -1) {
                            showCustomAlertDialog(true, "Data Saved Status", "Data Saved Successfully", "Batch");
                        } else {
                            showCustomAlertDialog(false, "Data Saved Status", "Failed To Save Data", "");
                        }
                        break;
                    case "Training":
                        insertStatus = dbComm.saveTrainingData(strTrainingNo, strTraningName);
                        if (insertStatus != -1) {
                            showCustomAlertDialog(true, "Data Saved Status", "Data Saved Successfully", "Training");
                        } else {
                            showCustomAlertDialog(false, "Data Saved Status", "Failed To Save Data", "");
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

    public void showCustomAlertDialog(boolean status, String strTitle, String strMessage, final String strOperation) {

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
                switch (strOperation) {
                    case "Group":
                        groupName.setText("");
                        groupProperty.setText("");
                        break;
                    case "Site":
                        siteCode.setText("");
                        break;
                    case "Batch":
                        batchNo.setText("");
                        batchName.setText("");
                        break;
                    case "Training":
                        trainingCenterNo.setText("");
                        trainingCenterName.setText("");
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent menu = new Intent(MasterDataEntryActivity.this, HomeActivity.class);
            startActivity(menu);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
