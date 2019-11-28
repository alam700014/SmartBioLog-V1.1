package com.android.fortunaattendancesystem.activities;

/*
 * *****************************************************************************
 * C O P Y R I G H T  A N D  C O N F I D E N T I A L I T Y  N O T I C E
 * <p>
 * Copyright Â© 2008-2009 Access Computech Pvt. Ltd. All rights reserved.
 * This is proprietary information of Access Computech Pvt. Ltd.and is
 * subject to applicable licensing agreements. Unauthorized reproduction,
 * transmission or distribution of this file and its contents is a
 * violation of applicable laws.
 * *****************************************************************************
 * <p>
 * project FM220_Android_SDK
 */

import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.acpl.access_computech_fm220_sdk.FM220_Scanner_Interface;
import com.acpl.access_computech_fm220_sdk.acpl_FM220_SDK;
import com.acpl.access_computech_fm220_sdk.fm220_Capture_Result;
import com.acpl.access_computech_fm220_sdk.fm220_Init_Result;
import com.android.fortunaattendancesystem.R;
import com.android.fortunaattendancesystem.helper.Utility;
import com.android.fortunaattendancesystem.model.EmployeeInfo;
import com.android.fortunaattendancesystem.model.StartekInfo;
import com.android.fortunaattendancesystem.singleton.StarkTekConnection;
import com.android.fortunaattendancesystem.singleton.StartekDatabaseItems;
import com.android.fortunaattendancesystem.submodules.SQLiteCommunicator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


public class EmployeeAttendanceStartekActivity extends Activity implements FM220_Scanner_Interface {


    private Button btn_In, btn_Out;
    private TextView tvSensorMsg;
    private ImageView imageView;


    private ImageView smart_reader, finger_reader;

    Dialog successEmpDetailsDialog = null;
    Dialog failureEmpDetailsDialog = null;

    private Handler pHandler = new Handler();

    /***************************************************
     * if you are use telecom/Locked device set the "Telecom_Device_Key" as your provided key otherwise send "" ;
     */

    //region USB intent and functions

    private static final String ACTION_USB_PERMISSION = "com.android.fortunaattendancesystem.activities.USB_PERMISSION";


    //======================== Variables for Startek Finger Sensor ============================//

    private boolean isStartekRcvRegisterd = false;
    private String Telecom_Device_Key = "";
    private acpl_FM220_SDK FM220SDK;
    private UsbManager usbManager;
    private PendingIntent mPermissionIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setContentView(R.layout.activity_employee_attendance_startek);
        initLayoutElements();
        initStartekFinger();
    }

    private void initLayoutElements() {

        smart_reader = (ImageView) findViewById(R.id.smartreader);
        finger_reader = (ImageView) findViewById(R.id.fingerreader);
        tvSensorMsg = (TextView) findViewById(R.id.textViewMessage);
        imageView = (ImageView) findViewById(R.id.imageView1);

        btn_In = (Button) findViewById(R.id.btnIn);
        btn_Out = (Button) findViewById(R.id.btnOut);

        btn_In.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // FM220SDK.CaptureFM220(2, true, true);
            }
        });
        btn_Out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //FM220SDK.CaptureFM220(2, true, true);
            }
        });
    }

    private void initStartekFinger() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences sp = getSharedPreferences("last_FM220_type", Activity.MODE_PRIVATE);
                boolean oldDevType = sp.getBoolean("FM220type", true);

                usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
                final Intent piIntent = new Intent(ACTION_USB_PERMISSION);
                if (Build.VERSION.SDK_INT >= 16) piIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
                mPermissionIntent = PendingIntent.getBroadcast(getBaseContext(), 1, piIntent, 0);

                IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
                filter.addAction(ACTION_USB_PERMISSION);
                filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
                filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
                registerReceiver(mStartekReceiver, filter);
                UsbDevice device = null;
                for (UsbDevice mdevice : usbManager.getDeviceList().values()) {
                    int pid, vid;
                    pid = mdevice.getProductId();
                    vid = mdevice.getVendorId();
                    boolean devType;
                    if ((pid == 0x8225) && (vid == 0x0bca)) {
                        FM220SDK = new acpl_FM220_SDK(getApplicationContext(), EmployeeAttendanceStartekActivity.this, true);
                        devType = true;
                    } else if ((pid == 0x8220) && (vid == 0x0bca)) {
                        FM220SDK = new acpl_FM220_SDK(getApplicationContext(), EmployeeAttendanceStartekActivity.this, false);
                        devType = false;
                    } else {
                        FM220SDK = new acpl_FM220_SDK(getApplicationContext(), EmployeeAttendanceStartekActivity.this, oldDevType);
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
                            final fm220_Init_Result res = FM220SDK.InitScannerFM220(usbManager, device, Telecom_Device_Key);
                            if (res.getResult()) {
                                StarkTekConnection.getInstance().setFM220SDK(FM220SDK);
                                updateFingerReaderUI(true);
                                FM220SDK.CaptureFM220(2, true, true);
                            }
                        }
                        break;
                    }
                }
                if (device == null) {
                    EmployeeAttendanceStartekActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvSensorMsg.setText("Pl connect FM220");
                        }
                    });
                    FM220SDK = new acpl_FM220_SDK(getApplicationContext(), EmployeeAttendanceStartekActivity.this, oldDevType);
                }
            }
        }).start();
    }

    @Override
    public void ScannerProgressFM220(final boolean DisplayImage, final Bitmap ScanImage, final boolean DisplayText, final String statusMessage) {
        EmployeeAttendanceStartekActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                Log.d("TEST", "Status Message:" + statusMessage);
                if (DisplayText) {
                    tvSensorMsg.setText(statusMessage);
                    tvSensorMsg.invalidate();
                }
                if (DisplayImage) {
                    imageView.setImageBitmap(ScanImage);
                    imageView.invalidate();
                }
            }
        });
    }


    @Override
    public void ScanCompleteFM220(final fm220_Capture_Result result) {
        EmployeeAttendanceStartekActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (result.getResult()) {
                    imageView.setImageBitmap(result.getScanImage());
                    byte[] captureTemplate = result.getISO_Template();   // ISO TEMPLET of FingerPrint.....
                    ArrayList <StartekInfo> list = StartekDatabaseItems.getInstance().getDatabaseItemsList();
                    if (list != null) {
                        int size = list.size();
                        Log.d("TEST","List Size in attendance:"+size);
                        if (size > 0) {
                            AsyncTaskIdentify task = new AsyncTaskIdentify(captureTemplate);
                            task.execute();
                        }
                    }
                } else {
                    String errorCode = result.getError();
                    switch (errorCode) {
                        case "500 :- Capture fail or Timeout.":
                            FM220SDK.CaptureFM220(2, true, true);
                            break;
                    }
                    imageView.setImageBitmap(null);
                    tvSensorMsg.setText(result.getError());
                }
                imageView.invalidate();
                tvSensorMsg.invalidate();
            }
        });
    }


    @Override
    public void ScanMatchFM220(final fm220_Capture_Result _result) {
        Log.d("TEST", "Scan match called");
    }

    private class AsyncTaskIdentify extends AsyncTask <Void, Void, Integer> {

        ProgressDialog mypDialog;
        byte[] capturedTemplate;

        AsyncTaskIdentify(byte[] capturedTemplate) {
            this.capturedTemplate = capturedTemplate;
        }

        @Override
        protected void onPreExecute() {
            mypDialog = new ProgressDialog(EmployeeAttendanceStartekActivity.this);
            mypDialog.setMessage("Finger Matching Wait...");
            mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mypDialog.setCanceledOnTouchOutside(false);
            mypDialog.show();
        }

        @Override
        protected Integer doInBackground(Void... voids) {

            int id = -1;
            boolean isMatched = false;
            ArrayList <StartekInfo> list = StartekDatabaseItems.getInstance().getDatabaseItemsList();
            if (list != null) {
                int size = list.size();
                for (int i = 0; i < size; i++) {
                    StartekInfo info = list.get(i);
                    if (FM220SDK.MatchFM220(capturedTemplate, info.getTemplate())) {
                        id = info.getAutoid();
                        break;
                    } else {
                        continue;
                    }
                }
            }
            return id;
        }

        @Override
        protected void onPostExecute(final Integer id) {
            mypDialog.cancel();
            if (id != -1) {
                if (successEmpDetailsDialog != null && successEmpDetailsDialog.isShowing()) {
                    successEmpDetailsDialog.cancel();
                }
                showSuccessIdentificationCustomDialog(id,"Identification Success");
                pHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        successEmpDetailsDialog.cancel();
                        FM220SDK.CaptureFM220(2, true, true);

                    }
                }, 4000);
            } else {
                if (failureEmpDetailsDialog != null && failureEmpDetailsDialog.isShowing()) {
                    failureEmpDetailsDialog.cancel();
                }
                showFailureCustomDialog("Identification failure", "Finger Print Did Not Match");
                pHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        failureEmpDetailsDialog.cancel();
                        FM220SDK.CaptureFM220(2, true, true);
                    }
                }, 4000);
            }
        }
    }


    //================================ View Identification Success dialog =====================================//

    private void showSuccessIdentificationCustomDialog(int autoId, String titleText) {

        successEmpDetailsDialog = new Dialog(EmployeeAttendanceStartekActivity.this);
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
        title.setText(titleText);

        if (autoId != -1) {
            SQLiteCommunicator dbComm = new SQLiteCommunicator();
            EmployeeInfo empInfo = null;
            empInfo = dbComm.getEmployeeInfoByAutoId(autoId, empInfo);
            if (empInfo != null) {
                int insertStaus = -1;
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat mdformat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss"); //Modified By Sanjay Shyamal
                String strDateTime = mdformat.format(calendar.getTime());
                String strEmpId = empInfo.getEmpId().trim();
                String strCardId = empInfo.getCardId().replaceAll("\\G0", " ").trim();
                String strName = empInfo.getEmpName().trim();

                attendanceTime.setText(strDateTime);
                empId.setText(strEmpId);
                empCardId.setText(strCardId);
                empName.setText(strName);

                byte[] byteImage = empInfo.getPhoto();
                if (byteImage != null && byteImage.length > 1) {
                    empPhoto.setImageBitmap(BitmapFactory.decodeByteArray(byteImage, 0, byteImage.length));
                } else {
                    empPhoto.setImageResource(R.drawable.dummyphoto);
                }
                String strLatitude = "";
                String strLongitude = "";
//                Location mLastLocation = EmployeeAttendanceActivity.mLastLocation;
//                if (mLastLocation != null) {
//                    strLatitude = Double.toString(mLastLocation.getLatitude());
//                    strLongitude = Double.toString(mLastLocation.getLongitude());
//                    Log.d("TEST", "Latitude:" + strLatitude + "Longitude:" + strLongitude);
//                    // getAddressByLatLong(mLastLocation.getLatitude(), mLastLocation.getLongitude());
//                }
                TextView tvStateofToggleButton = (TextView) EmployeeAttendanceStartekActivity.this.findViewById(getResIdFromContext("tvstate"));
                String strInOutModeText = tvStateofToggleButton.getText().toString();
                String strInOutMode = Utility.getInOutValue(strInOutModeText);
                insertStaus = dbComm.insertAttendanceData(strEmpId, strCardId, strDateTime, strInOutMode,"", strLatitude, strLongitude, null);
                if (insertStaus != -1) {
                    if (strInOutModeText.trim().equals("IN") || strInOutModeText.trim().equals("OUT")) {
                        Toast.makeText(EmployeeAttendanceStartekActivity.this, "Employee Attendance " + strInOutModeText + " Time Captured Successfully", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(EmployeeAttendanceStartekActivity.this, "Employee Attendance Captured Successfully", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(EmployeeAttendanceStartekActivity.this, "Attendance Capture Failure", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(EmployeeAttendanceStartekActivity.this, "Employee Data Not Found In Sqlite", Toast.LENGTH_LONG).show();
            }
        }else{
            Toast.makeText(EmployeeAttendanceStartekActivity.this, "Employee Data Not Found In Sqlite", Toast.LENGTH_LONG).show();
        }


        successEmpDetailsDialog.show();

        WindowManager.LayoutParams lp = successEmpDetailsDialog.getWindow().getAttributes();
        lp.dimAmount = 0.9f;
        lp.buttonBrightness = 1.0f;
        lp.screenBrightness = 1.0f;
        successEmpDetailsDialog.getWindow().setAttributes(lp);
        successEmpDetailsDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }

    private void showFailureCustomDialog(String strTitle, String strMessage) {

        failureEmpDetailsDialog = new Dialog(EmployeeAttendanceStartekActivity.this);
        failureEmpDetailsDialog.setCanceledOnTouchOutside(false);
        failureEmpDetailsDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        failureEmpDetailsDialog.setContentView(R.layout.finger_failure_custom_dialog);
        failureEmpDetailsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // set the custom dialog components - text, image and button

        TextView title = (TextView) failureEmpDetailsDialog.findViewById(R.id.title);
        TextView message = (TextView) failureEmpDetailsDialog.findViewById(R.id.message);
        title.setText(strTitle);
        message.setText(strMessage);

        Activity activity = (Activity) EmployeeAttendanceStartekActivity.this;
        TextView textViewPutFingerMessage = activity.findViewById(getResIdFromContext("putFingerMessage"));
        textViewPutFingerMessage.clearAnimation();

        failureEmpDetailsDialog.show();

        WindowManager.LayoutParams lp = failureEmpDetailsDialog.getWindow().getAttributes();
        lp.dimAmount = 0.9f;
        lp.buttonBrightness = 1.0f;
        lp.screenBrightness = 1.0f;
        failureEmpDetailsDialog.getWindow().setAttributes(lp);
        failureEmpDetailsDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }


    private int getResIdFromContext(String resName) {
        int resID = EmployeeAttendanceStartekActivity.this.getResources().getIdentifier(resName,
                "id", EmployeeAttendanceStartekActivity.this.getPackageName());
        return resID;
    }

    private final BroadcastReceiver mStartekReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                int pid, vid;
                pid = device.getProductId();
                vid = device.getVendorId();
                if ((pid == 0x8225 || pid == 0x8220) && (vid == 0x0bca)) {
                    FM220SDK.stopCaptureFM220();
                    FM220SDK.unInitFM220();
                    tvSensorMsg.setText("FM220 disconnected");
                }
            }
            if (ACTION_USB_PERMISSION.equals(action)) {
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
                                fm220_Init_Result res = FM220SDK.InitScannerFM220(usbManager, device, Telecom_Device_Key);
                                if (res.getResult()) {
                                    tvSensorMsg.setText("FM220 ready. " + res.getSerialNo());
                                } else {
                                    tvSensorMsg.setText("Error :-" + res.getError());
                                }
                            }
                        }
                    } else {
                        tvSensorMsg.setText("User Blocked USB connection");
                        tvSensorMsg.setText("FM220 ready");
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
                                tvSensorMsg.setText("FM220 requesting permission");
                                usbManager.requestPermission(device, mPermissionIntent);
                            } else {
                                fm220_Init_Result res = FM220SDK.InitScannerFM220(usbManager, device, Telecom_Device_Key);
                                if (res.getResult()) {
                                    tvSensorMsg.setText("FM220 ready. " + res.getSerialNo());
                                } else {
                                    tvSensorMsg.setText("Error :-" + res.getError());
                                }
                            }
                        }
                    }
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        try {
            if (isStartekRcvRegisterd) {
                unregisterReceiver(mStartekReceiver);
            }
            FM220SDK.unInitFM220();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
    //endregion

    public void updateFingerReaderUI(final boolean status) {
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

    public void updateSmartReaderUI(final boolean status) {

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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent menu = new Intent(EmployeeAttendanceStartekActivity.this, HomeActivity.class);
            startActivity(menu);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_emp_finger_identify, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.home) {
            showPasswordDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showPasswordDialog() {

        final Context context = EmployeeAttendanceStartekActivity.this;
        final Dialog dialog = new Dialog(context);
        dialog.setCanceledOnTouchOutside(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.password_dialog);

        TextView title = (TextView) dialog.findViewById(R.id.title);
        final EditText et_Password = (EditText) dialog.findViewById(R.id.etPassword);

        Button btn_Ok = (Button) dialog.findViewById(R.id.btn_Ok);
        ImageButton btn_Cancel = (ImageButton) dialog.findViewById(R.id.image);

        title.setText("Password Entry:");
        btn_Cancel.setImageResource(R.drawable.failure);

        btn_Ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                String strPassword = et_Password.getText().toString().trim();
                if (strPassword != null && strPassword.trim().length() > 0) {
                    boolean isValid = false;
                    SQLiteCommunicator dbComm = new SQLiteCommunicator();
                    isValid = dbComm.isPasswordValid(strPassword);
                    if (isValid) {
                        FM220SDK.stopCaptureFM220();
                        Intent previous = new Intent(EmployeeAttendanceStartekActivity.this, HomeActivity.class);
                        startActivity(previous);
                        finish();
                    } else {
                       // showCustomAlertDialog(false, "Error", "Invalid Password !!!");
                    }
                } else {
                   // showCustomAlertDialog(false, "Error", "Password cannot be left blank !!!");
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


}
