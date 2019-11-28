package com.forlinx.android;

import android.util.Log;

/**
 * Created by Administrator on 14-5-6.
 */
public class HardwareInterface {
    //ADC
    static public native int readADC();

    static {
        try {
            System.loadLibrary("forlinx-hardware");
        } catch (UnsatisfiedLinkError e){
            Log.e("forlinux-hardware", "load library error");
        }
    }
}
