package com.android.fortunaattendancesystem.forlinx;

import com.android.fortunaattendancesystem.constant.Constants;

import static android.os.SystemClock.sleep;

/**
 * Created by fortuna on 29/11/18.
 */

public class ForlinxLED {

    public static void showSuccessLED() {
        ForlinxHardwareController.setGreenLedValueJNI(Constants.GledOnn, "1");
        sleep(2000);
        ForlinxHardwareController.setGreenLedValueJNI(Constants.GledOff, "0");
    }

    public static void showFailureLED() {
        ForlinxHardwareController.setRedLedValueJNI(Constants.RledOnn, "1");
        sleep(2000);
        ForlinxHardwareController.setRedLedValueJNI(Constants.RledOff, "0");
    }
}
