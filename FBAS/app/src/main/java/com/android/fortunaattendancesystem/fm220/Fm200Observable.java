package com.android.fortunaattendancesystem.fm220;

import android.graphics.Bitmap;
import android.util.Log;

import com.acpl.access_computech_fm220_sdk.fm220_Capture_Result;

import java.util.Observable;

/**
 * Created by suman-dhara on 20/10/17.
 */

public class Fm200Observable extends Observable {
    private String text;
    private Bitmap bitmap;
    private fm220_Capture_Result fm220_capture_result = null;
    private static Fm200Observable INSTANCE = null;


    public static Fm200Observable getFm200Observable() {
        if(INSTANCE == null){
            INSTANCE = new Fm200Observable();
        }
        return INSTANCE;
    }


    public void setValue(String text, Bitmap img){
        this.text = text;
        this.bitmap = img;
        setChanged();
        notifyObservers(text);
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        //setChanged();
        //notifyObservers(text);
    }

    public String getText() {

        return text;
    }

    public Bitmap getBitmap() {

        return bitmap;
    }

    public fm220_Capture_Result getFm220CaptureResult() {
        Log.d("TEST","GET: ScanCompleteFM220 "+this.fm220_capture_result.getSerialNo());
        fm220_Capture_Result temp = fm220_capture_result;
        if(fm220_capture_result != null){
            fm220_capture_result = null;
        }
        return temp;
    }

    public void setFm220CaptureResult(fm220_Capture_Result fm220_capture_result) {
        this.fm220_capture_result = fm220_capture_result;
        Log.d("TEST","SET: ScanCompleteFM220 "+this.fm220_capture_result.getSerialNo());
        setChanged();
        notifyObservers(text);
    }

    public boolean isCaptureFinish(){
        return (this.fm220_capture_result != null)?this.fm220_capture_result.getResult():false;
    }

    /**
     * must call before new capture start, as proper use of isCaptureFinish() to check whether
     * capturing process is going on or not.
     *
     * @return boolean, return true if clear stored capture data.
     */
    public boolean prevCaptureClear(){
        this.fm220_capture_result = null;
        return true;
    }
}
