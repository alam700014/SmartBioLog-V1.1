package com.android.fortunaattendancesystem.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.fortunaattendancesystem.R;
import com.android.fortunaattendancesystem.constant.Constants;
import com.android.fortunaattendancesystem.forlinx.ForlinxGPIO;
import com.android.fortunaattendancesystem.model.EmployeeEnrollInfo;
import com.android.fortunaattendancesystem.singleton.Settings;
import com.android.fortunaattendancesystem.submodules.I2CCommunicator;
import com.android.fortunaattendancesystem.submodules.SQLiteCommunicator;

import java.util.Timer;
import java.util.TimerTask;


public class EmployeeEnrollmentPreviewActivity extends Activity {

    private TextView textViewEmpid, textViewCardid, textViewName, textViewAadhaarId, textViewSitecode, textViewEnrollStatus, textViewNosFinger, textViewMobileNo, textViewPin, textViewMailId, textViewValidUpto, textViewBirthday, textViewGroupId, textViewSmtcardVer;
    private TextView textViewBloodGroup, textViewVerificationMode, textViewIsAccessRightEnabled, textViewIsBlacklisted, textViewIsLockOpenWhenAllowed;
    private TextView textViewTrainingCenterName, textViewBatchName;
    private ImageView image;
    private byte[] byteImage = null;
    private final SQLiteCommunicator dbComm = new SQLiteCommunicator();
    private EmployeeEnrollInfo empInfo = null;

    private Handler hBrightness, hLCDBacklight;
    private Runnable rBrightness, rLCDBacklight;

    private static boolean isLCDBackLightOff = false;
    private static boolean isBreakFound = true;
    private Handler cHandler = new Handler();
    private Timer capReadTimer = null;
    private TimerTask capReadTimerTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_view_white);

//        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
//        TelephonyManager tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
//        String deviceIMEI = tel.getDeviceId();
//        mqttApi=new MqttApi(EmployeeEnrollmentPreviewActivity.this);
//        mqttAndroidClient= MqttClientInfo.getInstance().getMqttAndroidClient();
//        if(mqttAndroidClient==null || (mqttAndroidClient!=null && !mqttAndroidClient.isConnected())){
//            mqttAndroidClient=mqttApi.getMqttClient(Constants.MQTT_BROKER_URL,deviceIMEI);
//        }

        initLayoutElements();
        Intent intent = getIntent();
        if (intent != null) {
            empInfo = intent.getExtras().getParcelable("EmployeeEnrollInfo");
            populateDataToUI(empInfo);
        }


        //========================== For Mqtt ============================//

        //Generate unique client id for MQTT broker connection
//        Random r = new Random();
//        int i1 = r.nextInt(5000 - 1) + 1;
//        clientId = "mqtt" + i1;

//        mqttApi = new MqttApi(EmployeeEnrollmentPreviewActivity.this);
//        mqttAndroidClient = mqttApi.getMqttClient(Constants.MQTT_BROKER_URL, clientId);

        //Create listener for MQTT messages.
        // mqttCallback();

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

    public void populateDataToUI(EmployeeEnrollInfo empInfo) {

        textViewEmpid.setText(empInfo.getEmpId());
        textViewCardid.setText(empInfo.getCardId());
        textViewName.setText(empInfo.getEmpName());
        textViewAadhaarId.setText(empInfo.getAadhaarId());
        textViewBloodGroup.setText(empInfo.getBloodGroup());
        textViewSitecode.setText(empInfo.getSiteName());
        boolean status = empInfo.isFingerEnrolled();
        if (status) {
            textViewEnrollStatus.setText("Yes");
        } else {
            textViewEnrollStatus.setText("No");
        }
        int noOfFingersEnrolled = empInfo.getNoOfFingersEnrolled();
        if (noOfFingersEnrolled != 0) {
            textViewNosFinger.setText(Integer.toString(noOfFingersEnrolled));
        } else {
            textViewNosFinger.setText("NA");
        }
        String val = empInfo.getVerificationMode();
        if (val!=null && val.trim().length() > 0) {
            textViewVerificationMode.setText(val);
        } else {
            textViewVerificationMode.setText("NA");
        }
        textViewMobileNo.setText(empInfo.getMobileNo());
        textViewMailId.setText(empInfo.getEmailId());
        textViewPin.setText(empInfo.getPin());
        textViewValidUpto.setText(empInfo.getValidUpto());
        textViewBirthday.setText(empInfo.getDateOfBirth());
        textViewGroupId.setText(empInfo.getGroupName());
        textViewTrainingCenterName.setText(empInfo.getTrainingCenterName());
        textViewBatchName.setText(empInfo.getBatchName());
        int value = empInfo.getIsBlackListed();
        if (value == 0) {
            textViewIsBlacklisted.setText("No");
        } else if (value == 1) {
            textViewIsBlacklisted.setText("Yes");
        }
        value = empInfo.getIsAccessRightEnabled();
        if (value == 0) {
            textViewIsAccessRightEnabled.setText("No");
        } else if (value == 1) {
            textViewIsAccessRightEnabled.setText("Yes");
        }
        value = empInfo.getIsLockOpen();
        if (value == 0) {
            textViewIsLockOpenWhenAllowed.setText("No");
        } else if (value == 1) {
            textViewIsLockOpenWhenAllowed.setText("Yes");
        }
        val = empInfo.getSmartCardVer();
        if (val !=null && val.trim().length() > 0 && !val.equals("-1")) {
            textViewSmtcardVer.setText(val);
        } else {
            textViewSmtcardVer.setText("NA");
        }
        byteImage = empInfo.getPhoto();
        if (byteImage != null && byteImage.length > 1) {
            image.setImageBitmap(BitmapFactory.decodeByteArray(byteImage, 0, byteImage.length));
        } else {
            image.setImageResource(R.drawable.dummyphoto);
        }
    }

    public void initLayoutElements() {

        image = (ImageView) findViewById(R.id.imageView1);
        textViewEmpid = (TextView) findViewById(R.id.EmployeeID);
        textViewCardid = (TextView) findViewById(R.id.CardID);
        textViewName = (TextView) findViewById(R.id.Name);
        textViewAadhaarId = (TextView) findViewById(R.id.AadhaarId);
        textViewSitecode = (TextView) findViewById(R.id.sitecode);
        textViewEnrollStatus = (TextView) findViewById(R.id.enrollstatus);
        textViewNosFinger = (TextView) findViewById(R.id.nosfinger);
        textViewMobileNo = (TextView) findViewById(R.id.MobileNo);
        textViewPin = (TextView) findViewById(R.id.PIN);
        textViewMailId = (TextView) findViewById(R.id.MailId);
        textViewValidUpto = (TextView) findViewById(R.id.ValidUpto);
        textViewBirthday = (TextView) findViewById(R.id.BirthDay);
        textViewGroupId = (TextView) findViewById(R.id.GroupId);
        textViewTrainingCenterName = (TextView) findViewById(R.id.TrainingCenterName);
        textViewBatchName = (TextView) findViewById(R.id.BatchName);
        textViewSmtcardVer = (TextView) findViewById(R.id.SmartCardVersion);
        textViewBloodGroup = (TextView) findViewById(R.id.bloodgroup);
        textViewVerificationMode = (TextView) findViewById(R.id.Varification);
        textViewIsAccessRightEnabled = (TextView) findViewById(R.id.IsAccessRightEnabled);
        textViewIsBlacklisted = (TextView) findViewById(R.id.IsBlacklisted);
        textViewIsLockOpenWhenAllowed = (TextView) findViewById(R.id.IsLockOpenWhenAllowed);
    }

    public void modifyActionBar() {
        getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#e63900")));
        getActionBar().setTitle(Html.fromHtml("<b><font face='Calibri' color='#FFFFFF'>Employee Details Preview</font></b>"));
    }

    public void previous(View view) {
        finish();
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
            Intent previous = new Intent(EmployeeEnrollmentPreviewActivity.this, HomeActivity.class);
            startActivity(previous);
            overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            finish();
            return true;
        }
        if (id == R.id.back) {
            clearUiValues();
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    int packetId;

    public void saveData(View view) {
        Settings settings = Settings.getInstance();
        int mode = settings.getFingerEnrollmentModeValue();
        switch (mode) {
            case 0:
                try {
                    if (empInfo != null) {
                        String empId = empInfo.getEmpId();
                        if (empId.length() >= 1) {
                            int autoId;
                            autoId = dbComm.isDataAvailableInDatabase(empId);
                            empInfo.setEnrollmentNo(autoId);//Add Employee Auto Id
                            if (autoId == -1) {
                                empInfo = dbComm.insertLocallyEnrolledEmployeeData(empInfo);
                                autoId = empInfo.getDbStatus();
                                if (autoId != -1) {

                                    //======================== Mqtt =======================//

//                                    MqttAndroidClient mqttAndroidClient = MqttClientInfo.getInstance().getMqttAndroidClient();
//                                    if (mqttAndroidClient != null && mqttAndroidClient.isConnected()) {
//                                        Random r = new Random();
//                                        packetId = r.nextInt(5000 - 1) + 1;
//                                        String photo = Base64.encodeToString(empInfo.getPhoto(), Base64.DEFAULT);
//                                        //Toast.makeText(EmployeeEnrollmentPreviewActivity.this,"Photo len:"+photo.length(),Toast.LENGTH_LONG).show();
//                                        String payload = empInfo.getEmpId() + "^" + empInfo.getCardId() + "^" + empInfo.getEmpName() + "^" + photo;
//                                        MqttMessage message = new MqttMessage();
//                                        message.setId(packetId);
//                                        message.setQos(2);
//                                        message.setPayload(payload.getBytes());
//                                        String pubTopic = "/empinfo";
//                                        try {
//                                            mqttAndroidClient.publish(pubTopic,message);
//                                        } catch (MqttException e) {
//                                            e.printStackTrace();
//                                        }
//                                    } else {
//                                        Toast.makeText(EmployeeEnrollmentPreviewActivity.this, "Broker Not Connected", Toast.LENGTH_LONG).show();
//                                    }


                                    //=========================================================//

                                    String aadhaarId = empInfo.getAadhaarId();
                                    if (aadhaarId.trim().length() > 0) {
                                        autoId = dbComm.insertToAadhaarAuthTable(autoId, aadhaarId);
                                    }

                                    showCustomConfirmDialog(true, "Save Status", "Data Saved Successfully ! Do You Want To Enroll Finger?", Constants.SAVE_VALUE, mode, empInfo);
                                } else {
                                    showCustomAlertDialog(false, "Save Status", "Failed To Save Employee Data");
                                }
                            } else {
                                showCustomConfirmDialog(false, "Data Status", "Data Already Exists. \nDo You Want To Update Data?", Constants.UPDATE_VALUE, mode, empInfo);
                            }
                        } else {
                            showCustomAlertDialog(false, "Employee Details", "Invalid Employee Id Found");
                        }
                    } else {
                        showCustomAlertDialog(false, "Employee Details", "Employee Details Not Found");
                    }
                } catch (SQLiteException ex) {
                    showCustomAlertDialog(false, "Database Insert Error", ex.getMessage());
                }

                break;

            case 1:
                try {
                    if (empInfo != null) {
                        String empId = empInfo.getEmpId();
                        if (empId.length() >= 1) {
                            int autoId;
                            autoId = dbComm.isDataAvailableInDatabase(empId);
                            empInfo.setEnrollmentNo(autoId);//Add Employee AutoId
                            if (autoId == -1) {
                                String aadhaarId = empInfo.getAadhaarId();
                                if (aadhaarId.trim().length() > 0) {
                                    empInfo = dbComm.insertLocallyEnrolledEmployeeData(empInfo);
                                    autoId = empInfo.getDbStatus();
                                    if (autoId != -1) {
                                        autoId = dbComm.insertToAadhaarAuthTable(autoId, aadhaarId);
                                        if (autoId != -1) {
                                            showCustomConfirmDialog(true, "Data Save Status", "Data Saved Successfully ! Do You Want To Enroll Finger?", Constants.SAVE_VALUE, mode, empInfo);
                                        } else {
                                            showCustomAlertDialog(false, "Data Save Status", "Failed To Save Employee Aadhaar Data");
                                        }
                                    } else {
                                        showCustomAlertDialog(false, "Data Save Status", "Failed To Save Employee Data");
                                    }
                                } else {
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
                                    message.setTextColor(Color.parseColor("#e60000"));
                                    icon.setImageResource(R.drawable.failure);
                                    String strTitle = "Missing Aadhaar ID";
                                    String strMessage = "Do You Want To Save Personal Details Without Aadhaar ID?";
                                    title.setText(strTitle);
                                    message.setText(strMessage);
                                    btn_No.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            dialog.dismiss();
                                            clearUiValues();
                                            finish();
                                        }
                                    });

                                    btn_Yes.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            dialog.dismiss();
                                            if (empInfo != null) {
                                                int autoId = -1;
                                                empInfo = dbComm.insertLocallyEnrolledEmployeeData(empInfo);
                                                autoId = empInfo.getDbStatus();
                                                if (autoId != -1) {
                                                    showCustomConfirmDialog(true, "Data Save Status", "Data Saved Successfully ! Do You Want To Enroll Finger?", Constants.SAVE_VALUE, 0, empInfo);//0:Normal Mode If Aadhaar Id is blank
                                                } else {
                                                    showCustomAlertDialog(false, "Data Save Status", "Failed To Save Employee Data");
                                                }
                                            } else {
                                                showCustomAlertDialog(false, "Employee Details", "Employee Details Not Found");
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
                            } else {
                                showCustomConfirmDialog(false, "Employee Data Status", "Employee Data Already Exists. \nDo You Want To Update Data?", Constants.UPDATE_VALUE, mode, empInfo);
                            }
                        } else {
                            showCustomAlertDialog(false, "Employee Details", "Invalid Employee Id Found");
                        }
                    } else {
                        showCustomAlertDialog(false, "Employee Details", "Employee Details Not Found");
                    }
                } catch (SQLiteException ex) {
                    showCustomAlertDialog(false, "Database Error", ex.getMessage());
                }

                break;

            default:
                break;
        }
    }

    public void showCustomConfirmDialog(boolean status, String strTitle, String strMessage, final int operation, final int fingerEnrollMode, final EmployeeEnrollInfo empInfo) {

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
            message.setTextColor(Color.parseColor("#006400"));
            icon.setImageResource(R.drawable.success);
        } else {
            message.setTextColor(Color.parseColor("#e60000"));
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

                switch (operation) {

                    case Constants.SAVE_VALUE:
                        if (fingerEnrollMode == 0) {//Normal Mode
                            empInfo.setRemoteEnroll(false);
                            Intent intent = new Intent(EmployeeEnrollmentPreviewActivity.this, EmployeeFingerEnrollmentActivity.class);
                            intent.putExtra("EmployeeEnrollInfo", empInfo);
                            startActivity(intent);
                            finish();
                        } else if (fingerEnrollMode == 1) {//Aadhaar Mode
                            // Intent intent = new Intent(EmployeeEnrollmentPreviewActivity.this, AadhaarFingerEnrollment.class);
                            // intent.putExtra("EmployeeEnrollInfo", empInfo);
                            // startActivity(intent);
                        }

                        break;

                    case Constants.UPDATE_VALUE:

                        EmployeeEnrollInfo info = dbComm.updateEmployeeData(empInfo);
                        int status = info.getDbStatus();
                        if (status != -1) {

                            //=========================Insert/Update To Aadhaar Table=============================================//

                            String aadhaarId = empInfo.getAadhaarId();
                            if (aadhaarId.trim().length() > 0) {
                                int autoId = -1;
                                autoId = dbComm.isDataAvailableInAadhaarTable(aadhaarId);
                                if (autoId != -1) {
                                    autoId = dbComm.updateToAadhaarAuthTable(autoId, aadhaarId);
                                } else {
                                    autoId = dbComm.insertToAadhaarAuthTable(empInfo.getEnrollmentNo(), aadhaarId);
                                }
                            } else {
                                int autoId = -1;
                                autoId = dbComm.isDataAvailableInAadhaarTableByEmpId(empInfo.getEnrollmentNo());
                                if (autoId != -1) {
                                    autoId = dbComm.updateToAadhaarAuthTable(autoId, "");
                                }
                            }
                            int noOfFingersEnrolled = empInfo.getNoOfFingersEnrolled();
                            if (noOfFingersEnrolled == 0) {
                                if (fingerEnrollMode == 0) {
                                    showCustomConfirmDialog(true, "Data Save Status", "Data Updated Successfully..\nDo You Want To Enroll Finger?", Constants.SAVE_VALUE, fingerEnrollMode, empInfo);
                                } else if (fingerEnrollMode == 1) {
                                    aadhaarId = empInfo.getAadhaarId();
                                    if (aadhaarId.trim().length() > 0) {
                                        showCustomConfirmDialog(true, "Data Save Status", "Data Updated Successfully..\nDo You Want To Enroll Finger?", Constants.SAVE_VALUE, fingerEnrollMode, empInfo);
                                    } else {
                                        showCustomConfirmDialog(true, "Data Save Status", "Data Updated Successfully..\nDo You Want To Enroll Finger?", Constants.SAVE_VALUE, fingerEnrollMode, empInfo);
                                    }
                                }
                            } else {
                                aadhaarId = empInfo.getAadhaarId();
                                if (aadhaarId.trim().length() > 0) {
                                    int autoId = -1;
                                    autoId = dbComm.isDataAvailableInAadhaarTable(aadhaarId);
                                    if (autoId != -1) {
                                        int enrollmentNo = empInfo.getEnrollmentNo();
                                        if (enrollmentNo != 0) {
                                            boolean isAadhaarVerified = false;
                                            isAadhaarVerified = dbComm.isTemplateAadhaarVerified(empInfo.getEnrollmentNo());
                                            if (!isAadhaarVerified) {
                                                showCustomConfirmDialog(true, "Normal Finger Enrolled", "Data Updated Successfully..\nDo You Want To Update Template Using Aadhaar Mode?", Constants.SAVE_VALUE, fingerEnrollMode, empInfo);
                                            } else {
                                                showCustomAlertDialog(true, "Data Updation Status", "Data Updated Successfully");
                                            }
                                        }
                                    }
                                } else {
                                    showCustomAlertDialog(true, "Data Updation Status", "Data Updated Successfully");
                                }
                            }
                        } else {
                            showCustomAlertDialog(false, "Data Updation Status", "Failed To Update Employee Data!!!!!");
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
            clearUiValues();
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void clearUiValues() {
        image.setImageResource(R.drawable.dummyphoto);
        textViewEmpid.setText("");
        textViewCardid.setText("");
        textViewName.setText("");
        textViewAadhaarId.setText("");
        textViewSitecode.setText("");
        textViewEnrollStatus.setText("");
        textViewNosFinger.setText("");
        textViewMobileNo.setText("");
        textViewPin.setText("");
        textViewMailId.setText("");
        textViewValidUpto.setText("");
        textViewBirthday.setText("");
        textViewGroupId.setText("");
        textViewTrainingCenterName.setText("");
        textViewBatchName.setText("");
        textViewSmtcardVer.setText("");
        textViewBloodGroup.setText("");
        textViewVerificationMode.setText("");
        textViewIsAccessRightEnabled.setText("");
        textViewIsBlacklisted.setText("");
        textViewIsLockOpenWhenAllowed.setText("");
    }

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
            capReadTimer.schedule(capReadTimerTask, 0, 100); //50
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
                                            Intent intent = new Intent(EmployeeEnrollmentPreviewActivity.this, EmployeeAttendanceActivity.class);
                                            startActivity(intent);
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
                                            Intent intent = new Intent(EmployeeEnrollmentPreviewActivity.this, HomeActivity.class);
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
