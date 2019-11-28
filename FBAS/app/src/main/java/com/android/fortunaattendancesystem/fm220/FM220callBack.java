package com.android.fortunaattendancesystem.fm220;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.acpl.access_computech_fm220_sdk.acpl_FM220_SDK;
import com.android.fortunaattendancesystem.R;
import com.android.fortunaattendancesystem.singleton.EmployeeFingerEnrollInfo;
import com.android.fortunaattendancesystem.info.MorphoInfo;
import com.android.fortunaattendancesystem.info.ProcessInfo;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

//import com.google.android.gms.appindexing.Action;

/**
 * Created by suman-dhara on 11/10/17.
 */

public class FM220callBack extends AppCompatActivity implements Observer{
    TextView outPutText;
    ImageView scannigPreViewImg;
    ProgressBar scanQualityBar;
    acpl_FM220_SDK acpl_fm220_sdk;
    String textMsg = "";
    boolean flag = true;
    private Fm200Observable fm200Observable;
    private ArrayList<byte[]> templateList;
    private FM200PopUp fm200PopUp;
    private String message;
    private AlertDialog fingerUpdateAlert = null;
    private String strFingerStatus;
    private String strVerificationMode;
    private String strSecurityLevel;
    private String strPin;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fm200_scan_activity);
        fm200Observable =  Fm200Observable.getFm200Observable();
        fm200Observable.addObserver(this);
        this.acpl_fm220_sdk = ProcessInfo.getInstance().getFm220Sdk();
        outPutText = (TextView) findViewById(R.id.fm_textOutput);
        Log.d("TEST",outPutText.getText().toString());
        scannigPreViewImg = (ImageView) findViewById(R.id.fm_scanImg);
        scanQualityBar = (ProgressBar) findViewById(R.id.fm_imgQualityBar);
        this.acpl_fm220_sdk.FM220Initialized();

        Intent intent = getIntent();
        if (intent != null) {

            Bundle extras = intent.getExtras();

            if (extras != null) {

                strFingerStatus = extras.getString("FingerStatus", "");
                strVerificationMode = extras.getString("VerificationMode", "");
                strSecurityLevel = extras.getString("SecurityLevel", "");
                strPin = extras.getString("Pin", "");
            }

        }
        fm200PopUp = new FM200PopUp(this,strVerificationMode,strSecurityLevel,strPin);
        Log.d("TEST","FM220callBack");
        setFinishOnTouchOutside(false);
        this.acpl_fm220_sdk.SetRegistration(1);
        this.acpl_fm220_sdk.CaptureFM220(1,true,true);

    }
    @Override
    public void onResume() {
        super.onResume();
        Log.d("TEST","FM220callBack : onResume");

    }

    /**
     * This method is called if the specified {@code Observable} object's
     * {@code notifyObservers} method is called (because the {@code Observable}
     * object has been updated.
     *
     * @param observable the {@link Observable} object.
     * @param data       the data passed to {@link Observable#notifyObservers(Object)}.
     */
    @Override
    public void update(final Observable observable, final Object data) {
        final Fm200Observable fm200Observable = (Fm200Observable) observable;
        this.textMsg = data.toString();
        Log.d("TEST","update ScannerProgressFM220");
        FM220callBack.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                outPutText.setText(fm200Observable.getText());
                scannigPreViewImg.setImageBitmap(fm200Observable.getBitmap());

            }
        });
        if(strFingerStatus.equalsIgnoreCase("Update") && this.textMsg.equalsIgnoreCase("Success")){
            Log.d("TEST","strFingerStatus.equalsIgnoreCase(\"Update\") ");
            try {
                Thread.sleep(1000*3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            finish();
        }

    }

    private void fingerUpdate(){

       // FingerDataDetails.getInstance().setIsTemplateExists(false);
      //  FingerDataDetails.getInstance().setEnrollStatusValue(-1);

        MorphoInfo morphoInfo = ProcessInfo.getInstance().getMorphoInfo();

        String strEmpId = ((EmployeeFingerEnrollInfo) morphoInfo).getEmpId();
        String strCardId = ((EmployeeFingerEnrollInfo) morphoInfo).getCardId();
        String strEmpName = ((EmployeeFingerEnrollInfo) morphoInfo).getEmpName();

        if (strEmpId != null && strEmpId.trim().length() > 0) {
          ////  FingerDataDetails.getInstance().setStrEmpId(strEmpId);
        }

        if (strCardId != null && strCardId.trim().length() > 0) {
           //// FingerDataDetails.getInstance().setStrFirstName(strCardId);
        }

        if (strEmpName != null && strEmpName.trim().length() > 0) {
           //// FingerDataDetails.getInstance().setStrLastName(strEmpName);
        }

        int callbackCmd = ProcessInfo.getInstance().getCallbackCmd();

        int fingerNumber = ((EmployeeFingerEnrollInfo) morphoInfo).getNoOfFingers();

        if (fingerNumber == 1) {
            /* now mtch with db */

            /*****/
            //if(newUser){
                //FingerDataDetails.getInstance().setFirstFingerFID(firstFingerFID);
              //// FingerDataDetails.getInstance().setNoOfFingers(fingerNumber);
                message = "Finger Updated Successfully!!!!!";
            //}else{
               //// FingerDataDetails.getInstance().setIsTemplateExists(true);
                //message += "Finger Template Already Exists Against:\n";
                //message += "Employee Details:\n";
                //message += "Employee UserCreationActivity Id:" + firstUser.getField(0) + "\n";
                //message += "Employee First Name:" + firstUser.getField(1) + "\n";
                //message += "Employee Last Name:" + firstUser.getField(2);
            //}

        }
        if (fingerNumber == 2) {
            /* now first mtch with db */

            /*****/
            //if(newUser){
                //FingerDataDetails.getInstance().setFirstFingerFID(firstFingerFID);
               //// FingerDataDetails.getInstance().setNoOfFingers(fingerNumber);
            //}else{
               //// FingerDataDetails.getInstance().setIsTemplateExists(true);
                //message += "Finger Template Already Exists Against:\n";
                //message += "Employee Details:\n";
                //message += "Employee UserCreationActivity Id:" + firstUser.getField(0) + "\n";
                //message += "Employee First Name:" + firstUser.getField(1) + "\n";
                //message += "Employee Last Name:" + firstUser.getField(2);
            //}
            /* now second mtch with db */

            /*****/
            //if(newUser){
            //FingerDataDetails.getInstance().setFirstFingerFID(firstFingerFID);
         ////   FingerDataDetails.getInstance().setNoOfFingers(fingerNumber);
            //}else{
               //// FingerDataDetails.getInstance().setIsTemplateExists(true);
                message += "Second Finger Template Already Exists Against:\n";
                message += "Employee Details:\n";
                //message += "Employee UserCreationActivity Id:" + secondUser.getField(0) + "\n";
                //message += "Employee Card Id:" + secondUser.getField(1) + "\n";
                //message += "Employee Name:" + secondUser.getField(2);
            //}
        }
        FM220callBack.this.runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        fm200PopUp.fingerUpdateDialog("Updation Status", message, templateList);
                    }
                }

        );

        //this.finish();
    }



}
