package com.android.fortunaattendancesystem.forlinx;

/**
 * Created by fortuna on 27/11/18.
 */

public class ForlinxHardwareController {

    static {
        System.loadLibrary("native-lib");
    }


    public static  native String installRC522JNI(String str,String val);


    public static native String setDateTimeJNI (String str,String val);

    public static native String stringFromJNI();
    public static native int intFromJNI(int value);
    public static native int intSumFromJNI(int value1, int value2);
    public static native int intBuzzJNI(int bval);

    //===================== Function for On Off GPIO Pin ==========================//
    public static native String setGPIOValueJNI(String str, String val);

    //===================== Function for On Off Green LED ==========================//
    public static native String setGreenLedValueJNI (String str,String val);

    //===================== Function for On Off Red LED ==========================//
    public static native String setRedLedValueJNI (String str,String val);

    //===================== Function to get Wiegand Value ==========================//
    public static native String getWiegandValueJNI (String str);


    //===================== Function to get Wiegand Value ==========================//
    public static native String wigJNI (String str);


    //===================== Function for Reset ==========================//
    public static native String resetJNI (String str,String val);

}
