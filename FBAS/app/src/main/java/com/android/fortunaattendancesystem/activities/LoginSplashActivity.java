package com.android.fortunaattendancesystem.activities;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.Log;
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
import com.android.fortunaattendancesystem.constant.Constants;
import com.android.fortunaattendancesystem.forlinx.ForlinxGPIO;
import com.android.fortunaattendancesystem.helper.RealPathUtils;
import com.android.fortunaattendancesystem.model.StartekInfo;
import com.android.fortunaattendancesystem.singleton.Settings;
import com.android.fortunaattendancesystem.singleton.StarkTekConnection;
import com.android.fortunaattendancesystem.singleton.StartekDatabaseItems;
import com.android.fortunaattendancesystem.submodules.RC522Communicator;
import com.android.fortunaattendancesystem.submodules.SQLiteCommunicator;
import com.android.fortunaattendancesystem.usbconnection.USBConnectionCreator;
import com.bluetooth.ChatController;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class LoginSplashActivity extends USBConnectionCreator implements FM220_Scanner_Interface {

    //========================For Runtime Permissions===============//

    private final static int PERMS_REQUEST_CODE = 200;

    String[] permissions = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.INTERNET,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN
    };


    String firstHeader, secondHeader, thirdHeader, forthHeader, fifthHeader, sixthHeader, seventhHeader, eighthHeader;
    String keyDt, valueDt, keySR, valueSR, keyFR, valueFR, keyFEM, valueFEM, keyAtSCIP, valueAtSCIP, keyAtSCPort, valueAtSCPort, keyAtSCDm, valueAtSCDm, keyAtSCSType, valueAtSCSType, keyAtSCUrl, valueAtSCUrl, keyAaSCIP, valueAaSCIP, keyAaSCPort, valueAaSCPort, keyAaSCDm, valueAaSCDm, keyAaSCUrl, valueAaSCUrl, keyMAVEmpEnroll, valueMAVEmpEnroll, keyMAVMDE, valueMAVMDE, keyMAVPIO, valueMAVPIO, keyMAVExcel, valueMAVExcel;

    String keyAppType, valueAppType, keyAppSubType, valueAppSubType;

    private Pattern pattern;
    private Matcher matcher;

    private SQLiteCommunicator dbComm = new SQLiteCommunicator();

    private static final int EDIT_REQUEST_CODE = 44;


    //======================== Variables for Startek Finger Sensor ============================//

    private boolean isStartekRcvRegisterd = false;
    private String Telecom_Device_Key = "";
    private acpl_FM220_SDK FM220SDK;
    private UsbManager usbManager;
    private PendingIntent mPermissionIntent;


    private Handler hBrightness, hLCDBacklight;
    private Runnable rBrightness, rLCDBacklight;

    //============================ Bluetooth Variables ===================================//

    private static final int REQUEST_ENABLE_BLUETOOTH = 1;
    private String imei = "";
    private TelephonyManager tel;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice connectingDevice;
    private ChatController chatController;

    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_OBJECT = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final String DEVICE_OBJECT = "device_name";

    public static boolean isLoaded = false;

    // TextView tv1,tv2;

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        setContentView(R.layout.activity_login_page_splash_screen);

        getActionBar().setTitle(Html.fromHtml("<font color=\"#FFCC69\">" + "Smart Bio Log" + "</font>"));

//        boolean settingsCanWrite = android.provider.Settings.System.canWrite(getApplicationContext());
//
//        if (!settingsCanWrite) {
//            // If do not have write settings permission then open the Can modify system settings panel.
//            Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
//            startActivity(intent);
//        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, PERMS_REQUEST_CODE);
        } else {
            boolean isFound = dbComm.getAppSettings();
            if (isFound) {

                hBrightness = new Handler();
                hLCDBacklight = new Handler();

                rBrightness = new Runnable() {
                    @Override
                    public void run() {
                        setScreenBrightness(Constants.BRIGHTNESS_OFF);
                    }
                };

                rLCDBacklight = new Runnable() {
                    @Override
                    public void run() {
                        ForlinxGPIO.setLCDBackLightOff();
                    }
                };

                startHandler();

                tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                if (ActivityCompat.checkSelfPermission(LoginSplashActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }

                imei = tel.getDeviceId();

                //=========================== Init RC522 ===================================//

                RC522Communicator comm = new RC522Communicator();
                boolean status = comm.writeRC522(Constants.RC522_INIT_COMMAND);

                //===========================================================================//

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!isLoaded) {
                            isLoaded = true;
                            Intent i = new Intent(LoginSplashActivity.this, EmployeeAttendanceActivity.class);
                            startActivity(i);
                            overridePendingTransition(R.anim.enter, R.anim.exit);
                            finish();
                        }
                    }
                }, Constants.SPLASH_TIME_OUT);

            } else {
                showCustomAlertDialog(false, "App Settings", "App Settings Not Found", false);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults) {

        switch (permsRequestCode) {

            case PERMS_REQUEST_CODE:
                boolean isFound = dbComm.getAppSettings();
                if (isFound) {
                    hBrightness = new Handler();
                    hLCDBacklight = new Handler();

                    rBrightness = new Runnable() {
                        @Override
                        public void run() {
                            setScreenBrightness(Constants.BRIGHTNESS_OFF);
                        }
                    };

                    rLCDBacklight = new Runnable() {
                        @Override
                        public void run() {
                            ForlinxGPIO.setLCDBackLightOff();
                        }
                    };
                    startHandler();

                    tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    if (ActivityCompat.checkSelfPermission(LoginSplashActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    imei = tel.getDeviceId();

                    //=========================== Init RC522 ===================================//

                    RC522Communicator comm = new RC522Communicator();
                    boolean status = comm.writeRC522(Constants.RC522_INIT_COMMAND);

                    //===========================================================================//

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!isLoaded) {
                                isLoaded = true;
                                Intent i = new Intent(LoginSplashActivity.this, EmployeeAttendanceActivity.class);
                                startActivity(i);
                                overridePendingTransition(R.anim.enter, R.anim.exit);
                                finish();
                            }
                        }
                    }, Constants.SPLASH_TIME_OUT);

                } else {
                    showCustomAlertDialog(false, "App Settings", "App Settings Not Found", false);
                }
                break;
        }
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        stopHandler();
        startHandler();
        ForlinxGPIO.setLCDBackLightOn();
        setScreenBrightness(Constants.BRIGHTNESS_ON);
    }

    public void startHandler() {
        if (hBrightness != null && hLCDBacklight != null) {
            hBrightness.postDelayed(rBrightness, Constants.BRIGHTNESS_OFF_DELAY); //for 10 seconds
            hLCDBacklight.postDelayed(rLCDBacklight, Constants.BACKLIGHT_OFF_DELAY); //for 20 seconds
        }
    }

    public void stopHandler() {
        if (hBrightness != null && hLCDBacklight != null) {
            hBrightness.removeCallbacks(rBrightness);
            hLCDBacklight.removeCallbacks(rLCDBacklight);
        }
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
                        FM220SDK = new acpl_FM220_SDK(getApplicationContext(), LoginSplashActivity.this, true);
                        devType = true;
                    } else if ((pid == 0x8220) && (vid == 0x0bca)) {
                        FM220SDK = new acpl_FM220_SDK(getApplicationContext(), LoginSplashActivity.this, false);
                        devType = false;
                    } else {
                        FM220SDK = new acpl_FM220_SDK(getApplicationContext(), LoginSplashActivity.this, oldDevType);
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
                    FM220SDK = new acpl_FM220_SDK(getApplicationContext(), LoginSplashActivity.this, oldDevType);
                    StarkTekConnection.getInstance().setFM220SDK(FM220SDK);
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList <StartekInfo> databaseItemsList = null;
                SQLiteCommunicator dbComm = new SQLiteCommunicator();
                databaseItemsList = dbComm.loadDatabaseItems(databaseItemsList);
                StartekDatabaseItems.getInstance().setDatabaseItemsList(databaseItemsList);
            }
        }).start();
    }


    public void showCustomAlertDialog(boolean status, String strTitle, String strMessage, final boolean isResetRequired) {

        final Context context = this;
        final Dialog dialog = new Dialog(context);
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
                if (isResetRequired) {
                    resetIniSettings();
                    startActivity(getIntent());
                    finish();
                }
            }
        });

        dialog.show();
    }

    private void resetIniSettings() {

        Settings settings = Settings.getInstance();
        settings.setDeviceTypeTypeValue(-1);
        settings.setSrTypeValue(-1);
        settings.setFrTypeValue(-1);
        settings.setFingerEnrollmentModeValue(-1);
        settings.setServerTypeValue(-1);
        settings.setEmployeeEnrollmentValue(-1);
        settings.setMasterDateEntryValue(-1);
        settings.setPioValue(-1);
        settings.setExcelImportExportVal(-1);
        settings.setAttendanceDomain("");
        settings.setAttendanceUrl("");
        settings.setAttendanaceSIP("");
        settings.setAttendancePort("");
        settings.setAadhaarDomain("");
        settings.setAadhaarUrl("");
        settings.setAadhaarSIP("");
        settings.setAadhaarPort("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // getMenuInflater().inflate(R.menu.menu_login_page, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//        if (id == R.id.settings) {
//            showPasswordDialog();
//            return true;
//        }
//        if (id == R.id.bluetooth) {
//            if (bluetoothAdapter != null && bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
//                Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
//                startActivity(discoverableIntent);
//            }
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }


    private void showPasswordDialog() {

        final Context context = LoginSplashActivity.this;
        final Dialog dialog = new Dialog(context);
        dialog.setCanceledOnTouchOutside(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.password_dialog);

        TextView title = (TextView) dialog.findViewById(R.id.title);
        final EditText et_Password = (EditText) dialog.findViewById(R.id.etPassword);

        Button btn_Ok = (Button) dialog.findViewById(R.id.btn_Ok);
        ImageButton btn_Cancel = (ImageButton) dialog.findViewById(R.id.image);

        title.setText("Password Entry");
        btn_Cancel.setImageResource(R.drawable.closel);

        btn_Ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                boolean isValid = false;
                String strPassword = et_Password.getText().toString().trim();
                if (strPassword != null && strPassword.trim().length() > 0) {
                    isValid = dbComm.isPasswordValid(strPassword);
                    if (isValid) {
                        showImportIniDialog();
                    } else {
                        showCustomAlertDialog(false, "Error", "Invalid Password !!!", false);
                    }
                } else {
                    showCustomAlertDialog(false, "Error", "Password cannot be left blank !!!", false);
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


    private void showImportIniDialog() {

        final Context context = LoginSplashActivity.this;
        final Dialog dialog = new Dialog(context);
        dialog.setCanceledOnTouchOutside(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.import_ini_dialog);

        TextView title = (TextView) dialog.findViewById(R.id.title);
        ImageButton cancel = (ImageButton) dialog.findViewById(R.id.image);
        cancel.setImageResource(R.drawable.closel);
        title.setText("Import Ini");

        Button btnImportIni = (Button) dialog.findViewById(R.id.importIni);
        btnImportIni.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                importIniFile();
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

    private void importIniFile() {

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        // startActivityForResult(intent, OPEN_REQUEST_CODE);

        startActivityForResult(intent, EDIT_REQUEST_CODE);

//        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
//        intent.addCategory(Intent.CATEGORY_OPENABLE);
//        intent.setType("application/x-excel");
//        startActivityForResult(intent, OPEN_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        try {
            Uri currentUri = null;
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == EDIT_REQUEST_CODE) {
                    if (resultData != null) {
                        currentUri = resultData.getData();
                        String strPath = null;
                        File usbPath = null;
                        strPath = getPath(currentUri);
                        if (strPath == null) {
                            strPath = RealPathUtils.getRealPathFromURI_API19(LoginSplashActivity.this, currentUri);
                            String[] split = strPath.split("/");
                            File folders = new File("/storage");
                            if (folders.isDirectory()) {
                                File[] files = folders.listFiles();
                                for (int i = 0; i < files.length; i++) {
                                    usbPath = new File(files[i].getAbsolutePath() + "/" + split[3] + "/" + split[4]);
                                    if (usbPath.exists()) {
                                        if (usbPath != null && usbPath.getName().equals("FBAS.ini")) {
                                            strPath = usbPath.getAbsolutePath();
                                            ImportIniFile importFile = new ImportIniFile();
                                            importFile.execute(strPath);
                                        } else {
                                            showCustomAlertDialog(false, "Ini File Import Status", "Invalid Ini File !!!", false);
                                        }
                                    }
                                }
                            }
                        } else {
                            usbPath = new File(strPath);
                            if (usbPath != null && usbPath.getName().equals("FBAS.ini")) {
                                strPath = usbPath.getAbsolutePath();
                                ImportIniFile importFile = new ImportIniFile();
                                importFile.execute(strPath);
                            } else {
                                showCustomAlertDialog(false, "Ini File Import Status", "Invalid Ini File !!!", false);
                            }
                        }
                    }
                }
//                else if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
//                    chatController = new ChatController(this, bluetoothHandler);
//                }
            }
        } catch (Exception ex) {
            showCustomAlertDialog(false, "Error !", "" + ex.getMessage(), false);
        }
    }

    private class ImportIniFile extends AsyncTask <String, Void, Integer> {

        ProgressDialog mypDialog;

        @Override
        protected void onPreExecute() {
            mypDialog = new ProgressDialog(LoginSplashActivity.this);
            mypDialog.setMessage("Importing File Wait...");
            mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mypDialog.setCanceledOnTouchOutside(false);
            mypDialog.show();
        }

        @Override
        protected Integer doInBackground(String... path) {
            int error = -1;
            String strPath = path[0];
            error = readIniFile(strPath);
            return error;
        }

        @Override
        protected void onPostExecute(Integer error) {
            mypDialog.cancel();
            if (error != -1) {
                if (error == 0) {
                    showCustomAlertDialog(true, "Ini File Import Status", "Application settings imported successfully", true);
                } else {
                    showCustomAlertDialog(false, "Ini File Import Status", "Ini File Import Failed !!! Error Description:" + getErrorCodeDescription(error), false);
                }
            } else {
                showCustomAlertDialog(false, "Ini File Import Status", "Unknown Error", false);
            }
        }
    }

    private int readIniFile(String strPath) {
        int error = -1;
        File file = new File(strPath);
        if (file != null && file.getName().equals("FBAS.ini")) {
            byte[] fileData = new byte[(int) file.length()];
            FileInputStream fi = null;
            try {
                fi = new FileInputStream(file);
                fi.read(fileData, 0, fileData.length);
                String data = new String(fileData);
                String[] totalLines = data.split("\n");
                if (totalLines != null && totalLines.length == 27) {
                    error = validateIniFile(totalLines);
                    if (error == 0) {

                        if (keyDt != null && keyDt.trim().length() > 0 && valueDt != null && valueDt.trim().length() > 0) {
                            error = importIniSettings(1);
                        }

                        if (keySR != null && keySR.trim().length() > 0 && valueSR != null && valueSR.trim().length() > 0) {
                            error = importIniSettings(2);
                        } else if (keySR != null && keySR.trim().length() > 0) {
                            error = importIniSettings(2);
                        }

                        if (keyFR != null && keyFR.trim().length() > 0 && valueFR != null && valueFR.trim().length() > 0) {
                            error = importIniSettings(3);
                        }

                        if (keyFEM != null && keyFEM.trim().length() > 0 && valueFEM != null && valueFEM.trim().length() > 0) {
                            error = importIniSettings(4);
                        }

//                        if (valueAtSCIP != null && valueAtSCIP.trim().length() > 0 && valueAtSCPort != null && valueAtSCPort.trim().length() > 0) {
//                            error = insertAttendanceServerSettingsData();
//                        }

                        error = importIniSettings(6);

//                        if (valueAaSCIP != null && valueAaSCIP.trim().length() > 0 && valueAaSCPort != null && valueAaSCPort.trim().length() > 0) {
//                            error = insertAadhaarServerSettingsData();
//                        }

                        error = importIniSettings(7);

                        if (keyAtSCSType != null && keyAtSCSType.trim().length() > 0 && valueAtSCSType != null && valueAtSCSType.trim().length() > 0) {
                            error = importIniSettings(5);
                        }

                        if (keyMAVEmpEnroll != null && keyMAVEmpEnroll.trim().length() > 0 && valueMAVEmpEnroll != null && valueMAVEmpEnroll.trim().length() > 0) {
                            error = importIniSettings(8);
                        }

                        if (keyMAVMDE != null && keyMAVMDE.trim().length() > 0 && valueMAVMDE != null && valueMAVMDE.trim().length() > 0) {
                            error = importIniSettings(9);
                        }

                        if (keyMAVPIO != null && keyMAVPIO.trim().length() > 0 && valueMAVPIO != null && valueMAVPIO.trim().length() > 0) {
                            error = importIniSettings(10);
                        }

                        if (keyMAVExcel != null && keyMAVExcel.trim().length() > 0 && valueMAVExcel != null && valueMAVExcel.trim().length() > 0) {
                            error = importIniSettings(11);
                        }

                        if (keyAppType != null && keyAppType.trim().length() > 0 && valueAppType != null && valueAppType.trim().length() > 0) {
                            error = importIniSettings(12);
                        }

                        if (keyAppSubType != null && keyAppSubType.trim().length() > 0 && valueAppSubType != null && valueAppSubType.trim().length() > 0) {
                            error = importIniSettings(13);
                        }
                        if (error != -1) {
                            error = 0;
                        }
                    }
                } else {
                    error = 60;
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fi != null) {
                    try {
                        fi.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            error = 59;
        }
        return error;
    }


    private int importIniSettings(int type) {
        int status = -1;
        boolean found = false;
        switch (type) {

            case 1://Insert Device Type
                found = dbComm.isSettingsFound(firstHeader, keyDt);
                if (found) {
                    status = dbComm.deleteSettings(firstHeader, keyDt);
                    if (status != -1) {
                        status = dbComm.insertSettingsData(firstHeader, keyDt, valueDt);
                    }
                } else {
                    status = dbComm.insertSettingsData(firstHeader, keyDt, valueDt);
                }
                break;

            case 2:// Smart Reader Type
                found = dbComm.isSettingsFound(secondHeader, keySR);
                if (found) {
                    status = dbComm.deleteSettings(secondHeader, keySR);
                    if (status != -1) {
                        if (valueSR != null && valueSR.trim().length() > 0) {
                            status = dbComm.insertSettingsData(secondHeader, keySR, valueSR);
                        }
                    }
                } else {
                    if (valueSR != null && valueSR.trim().length() > 0) {
                        status = dbComm.insertSettingsData(secondHeader, keySR, valueSR);
                    }
                }
                break;

            case 3:// Finger Reader Type
                found = dbComm.isSettingsFound(thirdHeader, keyFR);
                if (found) {
                    status = dbComm.deleteSettings(thirdHeader, keyFR);
                    if (status != -1) {
                        status = dbComm.insertSettingsData(thirdHeader, keyFR, valueFR);
                    }
                } else {
                    status = dbComm.insertSettingsData(thirdHeader, keyFR, valueFR);
                }
                break;

            case 4://Finger Enrollment Type (Normal/Aadhaar)
                found = dbComm.isSettingsFound(forthHeader, keyFEM);
                if (found) {
                    status = dbComm.deleteSettings(forthHeader, keyFEM);
                    if (status != -1) {
                        status = dbComm.insertSettingsData(forthHeader, keyFEM, valueFEM);
                    }
                } else {
                    status = dbComm.insertSettingsData(forthHeader, keyFEM, valueFEM);
                }
                break;

            case 5:// Server Type
                found = dbComm.isSettingsFound(fifthHeader, keyAtSCSType);
                if (found) {
                    status = dbComm.deleteSettings(fifthHeader, keyAtSCSType);
                    if (status != -1) {
                        status = dbComm.insertSettingsData(fifthHeader, keyAtSCSType, valueAtSCSType);
                    }
                } else {
                    status = dbComm.insertSettingsData(fifthHeader, keyAtSCSType, valueAtSCSType);
                }
                break;

            case 6:// Attendance Server Credentials
                ArrayList list = dbComm.getAtServerIPPort();
                if (list != null) {
                    status = dbComm.deleteAtServerIPPort();
                    if (status != -1) {
                        status = dbComm.insertAtServerDetails(valueAtSCIP, valueAtSCPort, valueAtSCDm, valueAtSCUrl);
                    }
                } else {
                    status = dbComm.insertAtServerDetails(valueAtSCIP, valueAtSCPort, valueAtSCDm, valueAtSCUrl);
                }
                break;

            case 7:// Aadhaar Server Credentials
                list = dbComm.getAaServerIPPort();
                if (list != null) {
                    status = dbComm.deleteAaServerIPPort();
                    if (status != -1) {
                        status = dbComm.insertAaServerDetails(valueAaSCIP, valueAaSCPort, valueAaSCDm, valueAaSCUrl);
                    }
                } else {
                    status = dbComm.insertAaServerDetails(valueAaSCIP, valueAaSCPort, valueAaSCDm, valueAaSCUrl);
                }
                break;

            case 8:// Employee Enrollment Settings
                found = dbComm.isSettingsFound(seventhHeader, keyMAVEmpEnroll);
                if (found) {
                    status = dbComm.deleteSettings(seventhHeader, keyMAVEmpEnroll);
                    if (status != -1) {
                        status = dbComm.insertSettingsData(seventhHeader, keyMAVEmpEnroll, valueMAVEmpEnroll);
                    }
                } else {
                    status = dbComm.insertSettingsData(seventhHeader, keyMAVEmpEnroll, valueMAVEmpEnroll);
                }
                break;

            case 9:// Master Data Entry Settings
                found = dbComm.isSettingsFound(seventhHeader, keyMAVMDE);
                if (found) {
                    status = dbComm.deleteSettings(seventhHeader, keyMAVMDE);
                    if (status != -1) {
                        status = dbComm.insertSettingsData(seventhHeader, keyMAVMDE, valueMAVMDE);
                    }
                } else {
                    status = dbComm.insertSettingsData(seventhHeader, keyMAVMDE, valueMAVMDE);
                }
                break;

            case 10:// Programmable IN/OUT Settings
                found = dbComm.isSettingsFound(seventhHeader, keyMAVPIO);
                if (found) {
                    status = dbComm.deleteSettings(seventhHeader, keyMAVPIO);
                    if (status != -1) {
                        status = dbComm.insertSettingsData(seventhHeader, keyMAVPIO, valueMAVPIO);
                    }
                } else {
                    status = dbComm.insertSettingsData(seventhHeader, keyMAVPIO, valueMAVPIO);
                }
                break;

            case 11://Excel Export Import
                found = dbComm.isSettingsFound(seventhHeader, keyMAVExcel);
                if (found) {
                    status = dbComm.deleteSettings(seventhHeader, keyMAVExcel);
                    if (status != -1) {
                        status = dbComm.insertSettingsData(seventhHeader, keyMAVExcel, valueMAVExcel);
                    }
                } else {
                    status = dbComm.insertSettingsData(seventhHeader, keyMAVExcel, valueMAVExcel);
                }
                break;

            case 12://Application Settings
                found = dbComm.isSettingsFound(eighthHeader, keyAppType);
                if (found) {
                    status = dbComm.deleteSettings(eighthHeader, keyAppType);
                    if (status != -1) {
                        status = dbComm.insertSettingsData(eighthHeader, keyAppType, valueAppType);
                    }
                } else {
                    status = dbComm.insertSettingsData(eighthHeader, keyAppType, valueAppType);
                }
                break;

            case 13://Application Settings
                found = dbComm.isSettingsFound(eighthHeader, keyAppSubType);
                if (found) {
                    status = dbComm.deleteSettings(eighthHeader, keyAppSubType);
                    if (status != -1) {
                        status = dbComm.insertSettingsData(eighthHeader, keyAppSubType, valueAppSubType);
                    }
                } else {
                    status = dbComm.insertSettingsData(eighthHeader, keyAppSubType, valueAppSubType);
                }
                break;

            default:
                break;
        }

        return status;
    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) return null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String s = cursor.getString(column_index);
        cursor.close();
        return s;
    }

    //=====================================Ini File Validation======================================//

    public int validateIniFile(String[] lines) {

        String header;
        int error;
        for (int i = 0; i < 27; i++) {

            switch (i) {

                case 0:

                    //===============Installation Device Type Header==========//

                    firstHeader = lines[i].substring(1, 25).trim();
                    if (!firstHeader.equals(Constants.INI_HEADERS[0])) {
                        return 1;
                    }
                    break;

                case 1:

                    //===============Installation Device Type Key Value==========//

                    error = validateIniDeviceTypeKeyValue(lines[i], 0, Constants.DEVICE_TYPE_VAL);
                    if (error != 0) {
                        return error;
                    }
                    break;


                case 2:

                    //===============Smart Reader Header==========//

                    secondHeader = lines[i].substring(1, 13).trim();
                    if (!secondHeader.equals(Constants.INI_HEADERS[1])) {
                        return 5;
                    }
                    break;

                case 3:

                    //===============Smart Reader Key Value==========//

                    error = validateIniSmartReaderTypeKeyValue(lines[i], 1, Constants.SMART_READER_TYPE_VAL);
                    if (error != 0) {
                        return error;
                    }
                    break;

                case 4:

                    //===============Finger Reader Header==========//

                    thirdHeader = lines[i].substring(1, 14).trim();
                    if (!thirdHeader.equals(Constants.INI_HEADERS[2])) {
                        return 9;
                    }
                    break;

                case 5:

                    //===============Finger Reader Key Value==========//

                    error = validateIniFingerReaderTypeKeyValue(lines[i], 2, Constants.FINGER_READER_TYPE_VAL);
                    if (error != 0) {
                        return error;
                    }
                    break;

                case 6:

                    //================Finger Enrollment Mode Header===========//

                    forthHeader = lines[i].substring(1, 23).trim();
                    if (!forthHeader.equals(Constants.INI_HEADERS[3])) {
                        return 13;
                    }
                    break;

                case 7:

                    //================Finger Enrollment Key Value===========//

                    error = validateIniFingerEnrollmentKeyValue(lines[i], 3, Constants.FINGER_ENROLLMENT_MODE_VAL);
                    if (error != 0) {
                        return error;
                    }

                    break;

                case 8:

                    //================Attendance Server Credentials Header===========//

                    fifthHeader = lines[i].substring(1, 30).trim();
                    if (!fifthHeader.equals(Constants.INI_HEADERS[4])) {
                        return 17;
                    }

                    break;

                case 9:

                    //================Attendance Server Credentials Server IP Key Value===========//

                    error = validateIniAttendanceSerIPKeyValue(lines[i], 4);
                    if (error != 0) {
                        return error;
                    }
                    break;

                case 10:

                    //================Attendance Server Credentials Server Port Key Value===========//

                    error = validateIniAttendanceSerPortKeyValue(lines[i], 5);
                    if (error != 0) {
                        return error;
                    }
                    break;

                case 11:

                    //================Attendance Server Credentials Domain Key Value===========//

                    error = validateIniAttendanceSerDomainKeyValue(lines[i], 6);
                    if (error != 0) {
                        return error;
                    }
                    break;

                case 12:

                    //================Attendance Server Credentials Server Type Key Value===========//

                    error = validateIniServerTypeKeyValue(lines[i], 7, Constants.SERVER_TYPE_VAL);
                    if (error != 0) {
                        return error;
                    }
                    break;


                case 13:

                    //================Attendance Server Credentials Url Key Value===========//

                    error = validateIniAttendanceSerUrlKeyValue(lines[i], 8);
                    if (error != 0) {
                        return error;
                    }
                    break;


                case 14:

                    //================Aadhaar Server Credentials Header==================//

                    sixthHeader = lines[i].substring(1, 27).trim();
                    if (!sixthHeader.equals(Constants.INI_HEADERS[5])) {
                        return 33;
                    }
                    break;


                case 15:

                    //================Aadhaar Server Credentials Server IP Key Value===========//

                    error = validateIniAadhaarSerIPKeyValue(lines[i], 4);
                    if (error != 0) {
                        return error;
                    }
                    break;

                case 16:

                    //================Aadhaar Server Credentials Server IP Port Key Value===========//

                    error = validateIniAadhaarSerPortKeyValue(lines[i], 5);
                    if (error != 0) {
                        return error;
                    }
                    break;

                case 17:

                    //================Aadhaar Server Credentials Domain Key Value===========//

                    error = validateIniAadhaarSerDomainKeyValue(lines[i], 6);
                    if (error != 0) {
                        return error;
                    }
                    break;

                case 18:

                    //================Aadhaar Server Credentials Url Key Value===========//

                    error = validateIniAadhaarSerUrlKeyValue(lines[i], 8);
                    if (error != 0) {
                        return error;
                    }
                    break;

                case 19:

                    //================Menu Availability Header==================//

                    seventhHeader = lines[i].substring(1, 18).trim();
                    if (!seventhHeader.equals(Constants.INI_HEADERS[6])) {
                        return 46;
                    }
                    break;

                case 20:

                    //================Employee Enrollment key Value==================//

                    error = validateIniEmpEnrollmentKeyValue(lines[i], 9, Constants.EMPLOYEE_ENROLLMENT_TYPE_VAL);
                    if (error != 0) {
                        return error;
                    }
                    break;

                case 21:

                    //================Master Data Entry key Value==================//

                    error = validateIniMasterDataEntryKeyValue(lines[i], 10, Constants.MASTER_DATA_ENTERY_TYPE_VAL);
                    if (error != 0) {
                        return error;
                    }
                    break;

                case 22:

                    //================Programmable InOut key Value==================//

                    error = validateIniProgInOutKeyValue(lines[i], 11, Constants.PROGRAMMABLE_INOUT_STYPE_VAL);
                    if (error != 0) {
                        return error;
                    }
                    break;

                case 23:

                    //================Excel Import/Export InOut key Value==================//

                    error = validateIniExcelImpExpValue(lines[i], 12, Constants.EXCEL_IMPORT_EXPORT);
                    if (error != 0) {
                        return error;
                    }
                    break;

                case 24:

                    eighthHeader = lines[i].substring(1, 21).trim();
                    if (!eighthHeader.equals(Constants.INI_HEADERS[7])) {
                        return 61;
                    }

                    break;

                case 25:

                    error = validateIniAppTypeValue(lines[i], 13, Constants.APP_TYPE);
                    if (error != 0) {
                        return error;
                    }

                    break;

                case 26:

                    error = validateIniAppSubTypeValue(lines[i], 14, Constants.APP_TYPE);
                    if (error != 0) {
                        return error;
                    }

                    break;

                default:
                    break;
            }
        }

        return 0;

    }

    private int validateIniAppSubTypeValue(String keyValue, int keyIndex, String[] array) {
        String[] arrKeyValue = keyValue.split("=");
        if (arrKeyValue.length == 2) {
            boolean isValValid = false;
            keyAppSubType = arrKeyValue[0].trim();
            valueAppSubType = arrKeyValue[1].trim();
            if (keyAppSubType.equals(Constants.KEY[keyIndex])) {
                isValValid = false;
                for (int i = 0; i < array.length; i++) {
                    if (valueAppSubType.equals(array[i])) {
                        isValValid = true;
                    }
                }
                if (!isValValid) {
                    return 67;//invalid key value
                }
            } else {
                return 66;//invalid key name
            }
        } else {
            return 65;//invalid key value separation pattern in ini file
        }
        return 0;
    }

    private int validateIniAppTypeValue(String keyValue, int keyIndex, String[] array) {
        String[] arrKeyValue = keyValue.split("=");
        if (arrKeyValue.length == 2) {
            boolean isValValid = false;
            keyAppType = arrKeyValue[0].trim();
            valueAppType = arrKeyValue[1].trim();
            if (keyAppType.equals(Constants.KEY[keyIndex])) {
                isValValid = false;
                for (int i = 0; i < array.length; i++) {
                    if (valueAppType.equals(array[i])) {
                        isValValid = true;
                    }
                }
                if (!isValValid) {
                    return 64;//invalid key value
                }
            } else {
                return 63;//invalid key name
            }
        } else {
            return 62;//invalid key value separation pattern in ini file
        }
        return 0;
    }

    private int validateIniDeviceTypeKeyValue(String keyValue, int keyIndex, String[] array) {

        String[] arrKeyValue = keyValue.split("=");

        if (arrKeyValue.length == 2) {

            //String keyDt, valueDt;
            boolean isValValid = false;

            keyDt = arrKeyValue[0].trim();
            valueDt = arrKeyValue[1].trim();

            if (keyDt.equals(Constants.KEY[keyIndex])) {

                isValValid = false;
                for (int i = 0; i < array.length; i++) {
                    if (valueDt.equals(array[i])) {
                        isValValid = true;
                    }
                }

                if (!isValValid) {
                    return 4;//invalid key value
                }

            } else {
                return 3;//invalid key name
            }
        } else {
            return 2;//invalid key value separation pattern in ini file
        }

        return 0;
    }

    private int validateIniSmartReaderTypeKeyValue(String keyValue, int keyIndex, String[] array) {

        String[] arrKeyValue = keyValue.split("=");

        if (arrKeyValue.length == 2) {

            //String keyDt, valueDt;
            boolean isValValid = false;

            keySR = arrKeyValue[0].trim();
            valueSR = arrKeyValue[1].trim();

            if (keySR.equals(Constants.KEY[keyIndex])) {

                //==================Smart Reader Type Value Not Compulsory================//

                isValValid = false;

                for (int i = 0; i < array.length; i++) {
                    if (valueSR.equals(array[i])) {
                        isValValid = true;
                    }
                }

                if (!isValValid) {
                    return 8;//invalid key value
                }

            } else {
                return 7;//invalid key name
            }
        } else {
            keySR = arrKeyValue[0].trim();
            if (keySR != null && !keySR.equals(Constants.KEY[keyIndex])) {
                return 7;
            }
        }

//        else {
//            return 6;//invalid key value separation pattern in ini file
//        }

        return 0;
    }


    private int validateIniFingerReaderTypeKeyValue(String keyValue, int keyIndex, String[] array) {

        String[] arrKeyValue = keyValue.split("=");

        if (arrKeyValue.length == 2) {

            //String keyDt, valueDt;
            boolean isValValid = false;

            keyFR = arrKeyValue[0].trim();
            valueFR = arrKeyValue[1].trim();

            if (keyFR.equals(Constants.KEY[keyIndex])) {

                isValValid = false;
                for (int i = 0; i < array.length; i++) {
                    if (valueFR.equals(array[i])) {
                        isValValid = true;
                    }
                }

                if (!isValValid) {
                    return 12;//invalid key value
                }

            } else {
                return 11;//invalid key name
            }
        } else {
            return 10;//invalid key value separation pattern in ini file
        }

        return 0;
    }

    private int validateIniFingerEnrollmentKeyValue(String keyValue, int keyIndex, String[] array) {

        String[] arrKeyValue = keyValue.split("=");

        if (arrKeyValue.length == 2) {

            //String key, value;
            boolean isValValid = false;

            keyFEM = arrKeyValue[0].trim();
            valueFEM = arrKeyValue[1].trim();

            if (keyFEM.equals(Constants.KEY[keyIndex])) {

                isValValid = false;
                for (int i = 0; i < array.length; i++) {
                    if (valueFEM.equals(array[i])) {
                        isValValid = true;
                    }
                }

                if (!isValValid) {
                    return 16;//invalid key value
                }

            } else {
                return 15;//invalid key name
            }
        } else {
            return 14;//invalid key value separation pattern in ini file
        }

        return 0;
    }

    private int validateIniAttendanceSerIPKeyValue(String keyValue, int keyIndex) {

        String[] arrKeyValue = keyValue.split("=");

        if (arrKeyValue.length == 2) {

            // String key, value;
            boolean isValValid = false;

            keyAtSCIP = arrKeyValue[0].trim();
            valueAtSCIP = arrKeyValue[1].trim();

            if (keyAtSCIP.equals(Constants.KEY[keyIndex])) {

                isValValid = false;

                if (valueAtSCIP.trim().length() > 0) {
                    isValValid = isIPValid(valueAtSCIP);
                    if (!isValValid) {
                        return 20;//invalid key value
                    }
                }
            } else {
                return 19;//invalid key name
            }
        } else {
            //return 18;//invalid key value separation pattern in ini file
        }

        return 0;
    }

    private int validateIniAttendanceSerPortKeyValue(String keyValue, int keyIndex) {

        String[] arrKeyValue = keyValue.split("=");

        if (arrKeyValue.length == 2) {

            String key, value;
            boolean isValValid = false;

            keyAtSCPort = arrKeyValue[0].trim();
            valueAtSCPort = arrKeyValue[1].trim();

            if (keyAtSCPort.equals(Constants.KEY[keyIndex])) {

                isValValid = false;

                if (valueAtSCPort.trim().length() > 0) {
//                    isValValid = isPortNoValid(valueAtSCPort);
//                    if (!isValValid) {
//                        return 23;//invalid key value
//                    }
                }

            } else {
                return 22;//invalid key name
            }
        } else {
            // return 21;//invalid key value separation pattern in ini file
        }

        return 0;
    }


    private int validateIniAttendanceSerDomainKeyValue(String keyValue, int keyIndex) {


        String[] arrKeyValue = keyValue.split("=");

        if (arrKeyValue.length == 2) {

            // String key, value;
            boolean isValValid = false;

            keyAtSCDm = arrKeyValue[0].trim();
            valueAtSCDm = arrKeyValue[1].trim();

            if (keyAtSCDm.equals(Constants.KEY[keyIndex])) {

                isValValid = false;

                if (valueAtSCDm.trim().length() > 0) {

                    //Validate Domain

//                    isValValid = isIPValid(valueAtSCDm);
//                    if (!isValValid) {
//                        return 26;//invalid key value
//                    }
                }

            } else {
                return 25;//invalid key name
            }
        } else {
            // return 24;//invalid key value separation pattern in ini file
        }

        return 0;

    }


    private int validateIniServerTypeKeyValue(String keyValue, int keyIndex, String[] array) {

        String[] arrKeyValue = keyValue.split("=");

        if (arrKeyValue.length == 2) {

            // String key, value;
            boolean isValValid = false;

            keyAtSCSType = arrKeyValue[0].trim();
            valueAtSCSType = arrKeyValue[1].trim();

            if (keyAtSCSType.equals(Constants.KEY[keyIndex])) {

                isValValid = false;
                for (int i = 0; i < array.length; i++) {
                    if (valueAtSCSType.equals(array[i])) {
                        isValValid = true;
                    }
                }

                if (!isValValid) {
                    return 29;//invalid key value
                }

            } else {
                return 28;//invalid key name
            }
        } else {
            //return 27;//invalid key value separation pattern in ini file
        }

        return 0;
    }


    private int validateIniAttendanceSerUrlKeyValue(String keyValue, int keyIndex) {


        String[] arrKeyValue = keyValue.split("=");

        if (arrKeyValue.length == 2) {

            // String key, value;
            boolean isValValid = false;

            keyAtSCUrl = arrKeyValue[0].trim();
            valueAtSCUrl = arrKeyValue[1].trim();

            if (keyAtSCUrl.equals(Constants.KEY[keyIndex])) {

                isValValid = false;

                if (valueAtSCUrl.trim().length() > 0) {

                    //Validate Url

//                    isValValid = isIPValid(valueAtSCDm);
//                    if (!isValValid) {
//                        return 32;//invalid key value
//                    }
                }

            } else {
                return 31;//invalid key name
            }
        } else {
            // return 30;//invalid key value separation pattern in ini file
        }

        return 0;

    }


    private int validateIniAadhaarSerIPKeyValue(String keyValue, int keyIndex) {

        String[] arrKeyValue = keyValue.split("=");
        if (arrKeyValue.length == 2) {
            //String key, value;
            boolean isValValid = false;
            keyAaSCIP = arrKeyValue[0].trim();
            valueAaSCIP = arrKeyValue[1].trim();
            if (keyAaSCIP.equals(Constants.KEY[keyIndex])) {
                isValValid = false;
                if (valueAaSCIP.trim().length() > 0) {
                    isValValid = isIPValid(valueAaSCIP);
                    if (!isValValid) {
                        return 36;//invalid key value
                    }
                }
            } else {
                return 35;//invalid key name
            }
        } else {
            //  return 34;//invalid key value separation pattern in ini file
        }

        return 0;
    }

    private int validateIniAadhaarSerPortKeyValue(String keyValue, int keyIndex) {

        String[] arrKeyValue = keyValue.split("=");
        if (arrKeyValue.length == 2) {
            //  String key, value;
            boolean isValValid = false;
            keyAaSCPort = arrKeyValue[0].trim();
            valueAaSCPort = arrKeyValue[1].trim();
            if (keyAaSCPort.equals(Constants.KEY[keyIndex])) {
                isValValid = false;
                if (valueAaSCPort.trim().length() > 0) {
//                    isValValid = isPortNoValid(valueAaSCPort);
//                    if (!isValValid) {
//                        return 39;//invalid key value
//                    }
                }
            } else {
                return 38;//invalid key name
            }
        } else {
            // return 37;//invalid key value separation pattern in ini file
        }

        return 0;
    }


    private int validateIniAadhaarSerDomainKeyValue(String keyValue, int keyIndex) {
        String[] arrKeyValue = keyValue.split("=");
        if (arrKeyValue.length == 2) {
            // String key, value;
            boolean isValValid = false;
            keyAaSCDm = arrKeyValue[0].trim();
            valueAaSCDm = arrKeyValue[1].trim();
            if (keyAaSCDm.equals(Constants.KEY[keyIndex])) {
                isValValid = false;
                if (valueAaSCDm.trim().length() > 0) {

                    //Validate Domain

//                    isValValid = isIPValid(valueAtSCDm);
//                    if (!isValValid) {
//                        return 42;//invalid key value
//                    }
                }

            } else {
                return 41;//invalid key name
            }
        } else {
            // return 40;//invalid key value separation pattern in ini file
        }
        return 0;
    }

    private int validateIniAadhaarSerUrlKeyValue(String keyValue, int keyIndex) {

        String[] arrKeyValue = keyValue.split("=");
        if (arrKeyValue.length == 2) {
            // String key, value;
            boolean isValValid = false;
            keyAaSCUrl = arrKeyValue[0].trim();
            valueAaSCUrl = arrKeyValue[1].trim();
            if (keyAaSCUrl.equals(Constants.KEY[keyIndex])) {
                isValValid = false;
                if (valueAaSCUrl.trim().length() > 0) {

                    //Validate Url

//                    isValValid = isIPValid(valueAtSCDm);
//                    if (!isValValid) {
//                        return 45;//invalid key value
//                    }
                }

            } else {
                return 44;//invalid key name
            }
        } else {
            // return 43;//invalid key value separation pattern in ini file
        }
        return 0;
    }

    private int validateIniEmpEnrollmentKeyValue(String keyValue, int keyIndex, String[] array) {
        String[] arrKeyValue = keyValue.split("=");
        if (arrKeyValue.length == 2) {

            //String keyDt, valueDt;
            boolean isValValid = false;
            keyMAVEmpEnroll = arrKeyValue[0].trim();
            valueMAVEmpEnroll = arrKeyValue[1].trim();
            if (keyMAVEmpEnroll.equals(Constants.KEY[keyIndex])) {
                isValValid = false;
                for (int i = 0; i < array.length; i++) {
                    if (valueMAVEmpEnroll.equals(array[i])) {
                        isValValid = true;
                    }
                }
                if (!isValValid) {
                    return 49;//invalid key value
                }
            } else {
                return 48;//invalid key name
            }
        } else {
            return 47;//invalid key value separation pattern in ini file
        }
        return 0;
    }

    private int validateIniMasterDataEntryKeyValue(String keyValue, int keyIndex, String[] array) {
        String[] arrKeyValue = keyValue.split("=");
        if (arrKeyValue.length == 2) {
            //String keyDt, valueDt;
            boolean isValValid = false;
            keyMAVMDE = arrKeyValue[0].trim();
            valueMAVMDE = arrKeyValue[1].trim();
            if (keyMAVMDE.equals(Constants.KEY[keyIndex])) {
                isValValid = false;
                for (int i = 0; i < array.length; i++) {
                    if (valueMAVMDE.equals(array[i])) {
                        isValValid = true;
                    }
                }
                if (!isValValid) {
                    return 52;//invalid key value
                }
            } else {
                return 51;//invalid key name
            }
        } else {
            return 50;//invalid key value separation pattern in ini file
        }
        return 0;
    }


    private int validateIniExcelImpExpValue(String keyValue, int keyIndex, String[] array) {
        String[] arrKeyValue = keyValue.split("=");
        if (arrKeyValue.length == 2) {
            //String keyDt, valueDt;
            boolean isValValid = false;
            keyMAVExcel = arrKeyValue[0].trim();
            valueMAVExcel = arrKeyValue[1].trim();
            if (keyMAVExcel.equals(Constants.KEY[keyIndex])) {
                isValValid = false;
                for (int i = 0; i < array.length; i++) {
                    if (valueMAVExcel.equals(array[i])) {
                        isValValid = true;
                    }
                }
                if (!isValValid) {
                    return 58;//invalid key value
                }
            } else {
                return 57;//invalid key name
            }
        } else {
            return 56;//invalid key value separation pattern in ini file
        }
        return 0;
    }


    private int validateIniProgInOutKeyValue(String keyValue, int keyIndex, String[] array) {

        String[] arrKeyValue = keyValue.split("=");
        if (arrKeyValue.length == 2) {
            //String keyDt, valueDt;
            boolean isValValid = false;
            keyMAVPIO = arrKeyValue[0].trim();
            valueMAVPIO = arrKeyValue[1].trim();
            if (keyMAVPIO.equals(Constants.KEY[keyIndex])) {
                isValValid = false;
                for (int i = 0; i < array.length; i++) {
                    if (valueMAVPIO.equals(array[i])) {
                        isValValid = true;
                    }
                }
                if (!isValValid) {
                    return 55;//invalid key value
                }
            } else {
                return 54;//invalid key name
            }
        } else {
            return 53;//invalid key value separation pattern in ini file
        }
        return 0;
    }


    private String getErrorCodeDescription(int error) {

        String strDescription = "";
        switch (error) {
            case 1:
                strDescription = "Invalid Header Found For Device Type";
                break;
            case 2:
                strDescription = "Invalid Key Value Pattern Found For Device Type";
                break;
            case 3:
                strDescription = "Invalid Key Found For Device Type";
                break;
            case 4:
                strDescription = "Invalid Value Found For Device Type";
                break;
            case 5:
                strDescription = "Invalid Header Found For Smart Reader";
                break;
            case 6:
                strDescription = "Invalid Key Value Pattern Found For Smart Reader";
                break;
            case 7:
                strDescription = "Invalid Key Found For Smart Reader";
                break;
            case 8:
                strDescription = "Invalid Value Found For Smart Reader";
                break;
            case 9:
                strDescription = "Invalid Header Found For Finger Reader";
                break;
            case 10:
                strDescription = "Invalid Key Value Pattern Found For Finger Reader";
                break;
            case 11:
                strDescription = "Invalid Key Found For Finger Reader";
                break;
            case 12:
                strDescription = "Invalid Value Found For Finger Reader";
                break;
            case 13:
                strDescription = "Invalid Header Found For Finger Enrollment Mode";
                break;
            case 14:
                strDescription = "Invalid Key Value Pattern Found For Finger Enrollment Mode";
                break;
            case 15:
                strDescription = "Invalid Key Found For Finger Enrollment Mode";
                break;
            case 16:
                strDescription = "Invalid Value Found For Finger Enrollment Mode";
                break;
            case 17:
                strDescription = "Invalid Header Found For Attendance Server Credentials";
                break;
            case 18:
                strDescription = "Invalid Key Value Pattern Found For Attendance Server IP";
                break;
            case 19:
                strDescription = "Invalid Key Found For Attendance Server IP";
                break;
            case 20:
                strDescription = "Invalid Attendance Server IP Found";
                break;
            case 21:
                strDescription = "Invalid Key Value Pattern Found For Attendance Server Port";
                break;
            case 22:
                strDescription = "Invalid Key Found For Attendance Server Port";
                break;
            case 23:
                strDescription = "Invalid Attendance Server Port Found";
                break;
            case 24:
                strDescription = "Invalid Key Value Pattern Found For Attendance Server Domain";
                break;
            case 25:
                strDescription = "Invalid Key Found For Attendance Server Domain";
                break;
            case 26:
                strDescription = "Invalid Attendance Server Domain Found";
                break;
            case 27:
                strDescription = "Invalid Key Value Pattern Found For Attendance Server Type";
                break;
            case 28:
                strDescription = "Invalid Key Found For Attendance Server Type";
                break;
            case 29:
                strDescription = "Invalid Value Found For Attendance Server Type";
                break;
            case 30:
                strDescription = "Invalid Key Value Pattern Found For Attendance Server Url";
                break;
            case 31:
                strDescription = "Invalid Key Found For Attendance Server Url";
                break;
            case 32:
                strDescription = "Invalid Value Found For Attendance Server Url";
                break;
            case 33:
                strDescription = "Invalid Header Found For Aadhaar Server Credentials";
                break;
            case 34:
                strDescription = "Invalid Key Value Pattern Found For Aadhaar Server IP";
                break;
            case 35:
                strDescription = "Invalid Key Found For Aadhaar Server IP";
                break;
            case 36:
                strDescription = "Invalid Aadhaar Server IP Found";
                break;
            case 37:
                strDescription = "Invalid Key Value Pattern Found For Aadhaar Server Port";
                break;
            case 38:
                strDescription = "Invalid Key Found For Aadhaar Server Port";
                break;
            case 39:
                strDescription = "Invalid Aadhaar Server Port Found";
                break;
            case 40:
                strDescription = "Invalid Key Value Pattern Found For Aadhaar Server Domain";
                break;
            case 41:
                strDescription = "Invalid Key Found For Aadhaar Server Domain";
                break;
            case 42:
                strDescription = "Invalid Aadhaar Server Domain Found";
                break;
            case 43:
                strDescription = "Invalid Key Value Pattern Found For Aadhaar Server Url";
                break;
            case 44:
                strDescription = "Invalid Key Found For Aadhaar Server Url";
                break;
            case 45:
                strDescription = "Invalid Value Found For Aadhaar Server Url";
                break;
            case 46:
                strDescription = "Invalid Header Found For Menu Availability";
                break;
            case 47:
                strDescription = "Invalid Key Value Pattern Found For Employee Enrollment";
                break;
            case 48:
                strDescription = "Invalid Key Found For Employee Enrollment";
                break;
            case 49:
                strDescription = "Invalid Value Found For Employee Enrollment";
                break;
            case 50:
                strDescription = "Invalid Key Value Pattern Found For Master Data Entry";
                break;
            case 51:
                strDescription = "Invalid Key Found For Master Data Entry";
                break;
            case 52:
                strDescription = "Invalid Value Found For Master Data Entry";
                break;
            case 53:
                strDescription = "Invalid Key Value Pattern Found For Programmable InOut";
                break;
            case 54:
                strDescription = "Invalid Key Found For Programmable InOut";
                break;
            case 55:
                strDescription = "Invalid Value Found For Programmable InOut";
                break;
            case 56:
                strDescription = "Invalid Key Value Pattern Found For Excel Export/Import";
                break;
            case 57:
                strDescription = "Invalid Key Found For Excel Export/Import";
                break;
            case 58:
                strDescription = "Invalid Value Found For Excel Export/Import";
                break;
            case 59:
                strDescription = "Invalid File Selected";
                break;
            case 60:
                strDescription = "Invalid Ini File";
                break;
            case 61:
                strDescription = "Invalid Header Found For Application Settings";
                break;
            case 62:
                strDescription = "Invalid Key Value Pattern Found For App Type";
                break;
            case 63:
                strDescription = "Invalid Key Found For App Type";
                break;
            case 64:
                strDescription = "Invalid Value Found For App Type";
                break;
            case 65:
                strDescription = "Invalid Key Value Pattern Found For App Sub Type";
                break;
            case 66:
                strDescription = "Invalid Key Found For App Sub Type";
                break;
            case 67:
                strDescription = "Invalid Value Found For App Sub Type";
                break;
            default:
                break;

        }
        return strDescription;
    }


    //========================Valid IP Address Checking===============================//

    public boolean isIPValid(String ipAddress) {
        pattern = Pattern.compile(Constants.IPADDRESS_PATTERN);
        matcher = pattern.matcher(ipAddress);
        return matcher.matches();
    }

    public boolean isPortNoValid(String portNo) {
        Pattern pattern = Pattern.compile(Constants.PORT_PATTERN, Pattern.CASE_INSENSITIVE);
        return pattern.matcher(portNo).matches();
    }


    @Override
    protected void onResume() {
        super.onResume();
        stopHandler();
        startHandler();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopHandler();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isStartekRcvRegisterd) {
            try {
                if (mStartekReceiver != null) {
                    unregisterReceiver(mStartekReceiver);
                    isStartekRcvRegisterd = false;
                    mStartekReceiver = null;
                }
            } catch (Exception e) {
                Toast.makeText(LoginSplashActivity.this, "error in unregister startek receiver:" + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
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
                        Toast.makeText(LoginSplashActivity.this, "User Denied Permission for Finger Reader", Toast.LENGTH_LONG).show();
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
                                        Toast.makeText(LoginSplashActivity.this, "Startek Finger Reader Attached", Toast.LENGTH_LONG).show();
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
                Log.d("TEST", "Detached Called");
                if ((pid == 0x8225 || pid == 0x8220) && (vid == 0x0bca)) {
                    if (FM220SDK != null) {
                        FM220SDK.stopCaptureFM220();
                        FM220SDK.unInitFM220();
                        updateFrConStatusToUI(false);
                        StarkTekConnection.getInstance().setFM220SDK(FM220SDK);
                        Toast.makeText(LoginSplashActivity.this, "Startek Finger Reader Detached", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    };

    //============================ Startek Interface Methods ===============================//


    @Override
    public void ScannerProgressFM220(boolean b, Bitmap bitmap, boolean b1, String s) {

    }

    @Override
    public void ScanCompleteFM220(fm220_Capture_Result fm220_capture_result) {

    }

    @Override
    public void ScanMatchFM220(fm220_Capture_Result fm220_capture_result) {

    }


    //===========================Hardware Connection Abstract Methods=====================================

    public void updateFrConStatusToUI(final boolean status) {
    }

    public void updateSrConStatusToUI(final boolean status) {
    }

    @Override
    public void initIdentification() {
    }

    @Override
    public void initCardRead() {
    }

    @Override
    public void resetConnections() {
    }

    //============================ Bluetooth Functionality ===========================//

//    @Override
//    protected void onStart() {
//        super.onStart();
//        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
//            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH);
//        } else {
//            chatController = new ChatController(this, bluetoothHandler);
//        }
//    }

//    private void sendMessage(String message) {
//        if (chatController.getState() != ChatController.STATE_CONNECTED) {
//            Toast.makeText(this, "Connection was lost!", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        if (message.length() > 0) {
//            byte[] send = message.getBytes();
//            chatController.write(send);
//        }
//    }

//    private Handler bluetoothHandler = new Handler(new Handler.Callback() {
//        @Override
//        public boolean handleMessage(Message msg) {
//            switch (msg.what) {
//                case MESSAGE_STATE_CHANGE:
//                    switch (msg.arg1) {
//                        case ChatController.STATE_CONNECTED:
//                            //Toast.makeText(LoginActivity.this, "Connected", Toast.LENGTH_LONG).show();
//                            break;
//                        case ChatController.STATE_CONNECTING:
//                            //Toast.makeText(LoginActivity.this, "Connecting", Toast.LENGTH_LONG).show();
//                            break;
//                        case ChatController.STATE_LISTEN:
//                        case ChatController.STATE_NONE:
//                            //Toast.makeText(LoginActivity.this, "Not Connected", Toast.LENGTH_LONG).show();
//                            break;
//                    }
//                    break;
//                case MESSAGE_WRITE:
//                    byte[] writeBuf = (byte[]) msg.obj;
//                    String writeMessage = new String(writeBuf);
//                    break;
//                case MESSAGE_READ:
//                    byte[] readBuf = (byte[]) msg.obj;
//                    String readMessage = new String(readBuf, 0, msg.arg1);
//                    Toast.makeText(LoginSplashActivity.this, readMessage, Toast.LENGTH_LONG).show();
//                    DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSS");
//                    Date date = new Date();
//                    Random random = new Random();
//                    String randomNum = String.format("%04d", random.nextInt(10000));
//                    String strDate = dateFormat.format(date);
//                    String pid = strDate + randomNum;
//                    JSONObject reqJson = null;
//                    JSONObject resJson = null;
//                    try {
//                        reqJson = new JSONObject(readMessage);
//                        String ct = reqJson.getString("CT");
//                        switch (ct) {
//                            case "SIMEI"://Send IMEI
//                                Log.d("TEST", "SIMEI");
//                                String imei = "1234567ABCDEFG";//Dummy IMEI
//                                resJson = new JSONObject();
//                                resJson.put("PID", pid);
//                                resJson.put("IMEI", imei);
//                                resJson.put("Status", false);
//                                resJson.put("Error Code", "");
//                                resJson.put("Error Description", "");
//                                sendMessage(resJson.toString());
//                                break;
//                            case "RBD"://Receive Token No and Broker Details
//                                Log.d("TEST", "RBD");
//                                String tokenNo = reqJson.getString("TokenNo");
//                                String brokerIP = reqJson.getString("BrokerIP");
//                                String brokerPort = reqJson.getString("BrokerPort");
//                                String brokerUsername = reqJson.getString("BrokerUsername");
//                                String brokerPassword = reqJson.getString("BrokerPassword");
//                                String subTopic = "", pubTopic = "";
//                                JSONArray array = reqJson.getJSONArray("TopicList");
//                                if (array != null) {
//                                    int size = array.length();
//                                    if (size > 0 && size == 2) {
//                                        JSONObject obj1 = (JSONObject) array.get(0);
//                                        JSONObject obj2 = (JSONObject) array.get(1);
//                                        subTopic = obj1.getString("topicName");
//                                        pubTopic = obj2.getString("topicName");
//                                    }
//                                }
//                                int id = dbComm.isBrokerDetailsAvailable();
//                                if (id == -1) {
//                                    id = dbComm.insertBrokerDetails(tokenNo, brokerIP, brokerPort, brokerUsername, brokerPassword, subTopic, pubTopic, strDate);
//                                    if (id != -1) {
//                                        Log.d("TEST", "Inserted successfully");
//                                    }
//                                } else {
//                                    id = dbComm.updateBrokerDetails(id, tokenNo, brokerIP, brokerPort, brokerUsername, brokerPassword, subTopic, pubTopic, strDate);
//                                    if (id != -1) {
//                                        Log.d("TEST", "Updated successfully");
//                                    }
//                                }
//                                break;
//                            default:
//                                break;
//                        }
//                    } catch (JSONException e) {
//                        Log.d("TEST", "Exception:" + e.getMessage());
//                    }
//                    break;
//                case MESSAGE_DEVICE_OBJECT:
//                    connectingDevice = msg.getData().getParcelable(DEVICE_OBJECT);
//                    Toast.makeText(getApplicationContext(), "Connected to " + connectingDevice.getName(),
//                            Toast.LENGTH_SHORT).show();
//                    break;
//                case MESSAGE_TOAST:
//                    Toast.makeText(getApplicationContext(), msg.getData().getString("toast"),
//                            Toast.LENGTH_SHORT).show();
//                    break;
//            }
//            return false;
//        }
//    });
}
