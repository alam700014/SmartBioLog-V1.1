package com.android.fortunaattendancesystem.forlinx;

import com.android.fortunaattendancesystem.constant.Constants;
import com.android.fortunaattendancesystem.submodules.ForlinxGPIOCommunicator;

import static android.os.SystemClock.sleep;

/**
 * Created by fortuna on 29/11/18.
 */

public class ForlinxGPIO {

    public static void installRC522() {
        ForlinxHardwareController.installRC522JNI(Constants.RC522_INSTALL, "0");
    }

    public static void runGPIOForPressed() {
        ForlinxGPIOCommunicator.setGPIO(Constants.BUZZ_PATH, "0");
        sleep(100);
        ForlinxGPIOCommunicator.setGPIO(Constants.BUZZ_PATH, "1");
        sleep(100);
        ForlinxGPIOCommunicator.setGPIO(Constants.BUZZ_PATH, "0");
        sleep(100);
        ForlinxGPIOCommunicator.setGPIO(Constants.BUZZ_PATH, "1");
        sleep(500);
//        ForlinxHardwareController.setGPIOValueJNI(Constants.B_Off, "0");
//        sleep(100);
//        ForlinxHardwareController.setGPIOValueJNI(Constants.B_Onn, "1");
//        sleep(100);
//        ForlinxHardwareController.setGPIOValueJNI(Constants.B_Off, "0");
//        sleep(100);
//        ForlinxHardwareController.setGPIOValueJNI(Constants.B_Onn, "1");
//        sleep(500);
    }

    public static void runGPIOLEDForSuccess() {
//        ForlinxGPIOCommunicator.setGPIO(Constants.BUZZ_PATH,"0");
//        ForlinxGPIOCommunicator.setGPIO(Constants.GREEN_LED_PATH, "1");
//        sleep(1000);
//        ForlinxGPIOCommunicator.setGPIO(Constants.BUZZ_PATH,"1");
//        ForlinxGPIOCommunicator.setGPIO(Constants.GREEN_LED_PATH, "0");


        ForlinxGPIOCommunicator.setGPIO(Constants.BUZZ_PATH, "0");
        ForlinxGPIOCommunicator.setGPIO(Constants.GREEN_LED_PATH, "0");
        sleep(1000);
        ForlinxGPIOCommunicator.setGPIO(Constants.BUZZ_PATH, "1");
        ForlinxGPIOCommunicator.setGPIO(Constants.GREEN_LED_PATH, "1");


//        ForlinxHardwareController.setGPIOValueJNI(Constants.B_Off, "0");
//        ForlinxHardwareController.setGreenLedValueJNI(Constants.GledOnn, "1");
//        sleep(500);
//        ForlinxHardwareController.setGPIOValueJNI(Constants.B_Onn, "1");
//        ForlinxHardwareController.setGreenLedValueJNI(Constants.GledOff, "0");
    }

    public static void runGPIOLEDForCardRead() {
//        ForlinxGPIOCommunicator.setGPIO(Constants.BUZZ_PATH,"0");
//        ForlinxGPIOCommunicator.setGPIO(Constants.GREEN_LED_PATH, "1");
//        sleep(500);
//        ForlinxGPIOCommunicator.setGPIO(Constants.BUZZ_PATH,"1");
//        ForlinxGPIOCommunicator.setGPIO(Constants.GREEN_LED_PATH, "0");


        ForlinxGPIOCommunicator.setGPIO(Constants.BUZZ_PATH, "0");
        ForlinxGPIOCommunicator.setGPIO(Constants.GREEN_LED_PATH, "0");
        sleep(500);
        ForlinxGPIOCommunicator.setGPIO(Constants.BUZZ_PATH, "1");
        ForlinxGPIOCommunicator.setGPIO(Constants.GREEN_LED_PATH, "1");


//        ForlinxHardwareController.setGPIOValueJNI(Constants.B_Off, "0");
//        ForlinxHardwareController.setGreenLedValueJNI(Constants.GledOnn, "1");
//        sleep(500);
//        ForlinxHardwareController.setGPIOValueJNI(Constants.B_Onn, "1");
//        ForlinxHardwareController.setGreenLedValueJNI(Constants.GledOff, "0");
    }


    public static void runGPIOLEDForWiegandRead() {
//        ForlinxGPIOCommunicator.setGPIO(Constants.BUZZ_PATH,"0");
//        ForlinxGPIOCommunicator.setGPIO(Constants.GREEN_LED_PATH, "1");
//        sleep(500);
//        ForlinxGPIOCommunicator.setGPIO(Constants.BUZZ_PATH,"1");
//        ForlinxGPIOCommunicator.setGPIO(Constants.GREEN_LED_PATH, "0");


        ForlinxGPIOCommunicator.setGPIO(Constants.BUZZ_PATH, "0");
        ForlinxGPIOCommunicator.setGPIO(Constants.GREEN_LED_PATH, "0");
        sleep(500);
        ForlinxGPIOCommunicator.setGPIO(Constants.BUZZ_PATH, "1");
        ForlinxGPIOCommunicator.setGPIO(Constants.GREEN_LED_PATH, "1");


//        ForlinxHardwareController.setGPIOValueJNI(Constants.B_Off, "0");
//        ForlinxHardwareController.setGreenLedValueJNI(Constants.GledOnn, "1");
//        sleep(500);
//        ForlinxHardwareController.setGPIOValueJNI(Constants.B_Onn, "1");
//        ForlinxHardwareController.setGreenLedValueJNI(Constants.GledOff, "0");
    }


    public static void runGPIOForEnroll() {
        ForlinxGPIOCommunicator.setGPIO(Constants.BUZZ_PATH, "0");
        sleep(100);
        ForlinxGPIOCommunicator.setGPIO(Constants.BUZZ_PATH, "1");

//        ForlinxHardwareController.setGPIOValueJNI(Constants.B_Off, "0");
//        sleep(100);
//        ForlinxHardwareController.setGPIOValueJNI(Constants.B_Onn, "1");
    }

    public static void runGPIOLEDForFailure() {
//        ForlinxGPIOCommunicator.setGPIO(Constants.BUZZ_PATH,"0");
//        ForlinxGPIOCommunicator.setGPIO(Constants.RED_LED_PATH,"1");
//        sleep(150);
//        ForlinxGPIOCommunicator.setGPIO(Constants.BUZZ_PATH,"1");
//        ForlinxGPIOCommunicator.setGPIO(Constants.RED_LED_PATH,"0");
//        sleep(75);
//        ForlinxGPIOCommunicator.setGPIO(Constants.BUZZ_PATH,"0");
//        ForlinxGPIOCommunicator.setGPIO(Constants.RED_LED_PATH,"1");
//        sleep(150);
//        ForlinxGPIOCommunicator.setGPIO(Constants.BUZZ_PATH,"1");
//        ForlinxGPIOCommunicator.setGPIO(Constants.RED_LED_PATH,"0");
//        sleep(75);
//        ForlinxGPIOCommunicator.setGPIO(Constants.BUZZ_PATH,"0");
//        ForlinxGPIOCommunicator.setGPIO(Constants.RED_LED_PATH,"1");
//        sleep(150);
//        ForlinxGPIOCommunicator.setGPIO(Constants.BUZZ_PATH,"1");
//        ForlinxGPIOCommunicator.setGPIO(Constants.RED_LED_PATH,"0");
//        sleep(75);
//        ForlinxGPIOCommunicator.setGPIO(Constants.BUZZ_PATH,"0");
//        ForlinxGPIOCommunicator.setGPIO(Constants.RED_LED_PATH,"1");
//        sleep(150);
//        ForlinxGPIOCommunicator.setGPIO(Constants.BUZZ_PATH,"1");
//        ForlinxGPIOCommunicator.setGPIO(Constants.RED_LED_PATH,"0");
//        sleep(75);
//        ForlinxGPIOCommunicator.setGPIO(Constants.BUZZ_PATH,"0");
//        ForlinxGPIOCommunicator.setGPIO(Constants.RED_LED_PATH,"1");
//        sleep(150);
//        ForlinxGPIOCommunicator.setGPIO(Constants.BUZZ_PATH,"1");
//        ForlinxGPIOCommunicator.setGPIO(Constants.RED_LED_PATH,"0");


        ForlinxGPIOCommunicator.setGPIO(Constants.BUZZ_PATH, "0");
        ForlinxGPIOCommunicator.setGPIO(Constants.RED_LED_PATH, "0");
        sleep(150);
        ForlinxGPIOCommunicator.setGPIO(Constants.BUZZ_PATH, "1");
        ForlinxGPIOCommunicator.setGPIO(Constants.RED_LED_PATH, "1");
        sleep(75);
        ForlinxGPIOCommunicator.setGPIO(Constants.BUZZ_PATH, "0");
        ForlinxGPIOCommunicator.setGPIO(Constants.RED_LED_PATH, "0");
        sleep(150);
        ForlinxGPIOCommunicator.setGPIO(Constants.BUZZ_PATH, "1");
        ForlinxGPIOCommunicator.setGPIO(Constants.RED_LED_PATH, "1");
        sleep(75);
        ForlinxGPIOCommunicator.setGPIO(Constants.BUZZ_PATH, "0");
        ForlinxGPIOCommunicator.setGPIO(Constants.RED_LED_PATH, "0");
        sleep(150);
        ForlinxGPIOCommunicator.setGPIO(Constants.BUZZ_PATH, "1");
        ForlinxGPIOCommunicator.setGPIO(Constants.RED_LED_PATH, "1");
        sleep(75);
        ForlinxGPIOCommunicator.setGPIO(Constants.BUZZ_PATH, "0");
        ForlinxGPIOCommunicator.setGPIO(Constants.RED_LED_PATH, "0");
        sleep(150);
        ForlinxGPIOCommunicator.setGPIO(Constants.BUZZ_PATH, "1");
        ForlinxGPIOCommunicator.setGPIO(Constants.RED_LED_PATH, "1");
        sleep(75);
        ForlinxGPIOCommunicator.setGPIO(Constants.BUZZ_PATH, "0");
        ForlinxGPIOCommunicator.setGPIO(Constants.RED_LED_PATH, "0");
        sleep(150);
        ForlinxGPIOCommunicator.setGPIO(Constants.BUZZ_PATH, "1");
        ForlinxGPIOCommunicator.setGPIO(Constants.RED_LED_PATH, "1");


//        ForlinxHardwareController.setGPIOValueJNI(Constants.B_Off, "0");
//        ForlinxHardwareController.setRedLedValueJNI(Constants.RledOnn, "1");
//        sleep(150);
//        ForlinxHardwareController.setGPIOValueJNI(Constants.B_Onn, "1");
//        ForlinxHardwareController.setRedLedValueJNI(Constants.RledOff, "0");
//        sleep(75);
//        ForlinxHardwareController.setGPIOValueJNI(Constants.B_Off, "0");
//        ForlinxHardwareController.setRedLedValueJNI(Constants.RledOnn, "1");
//        sleep(150);
//        ForlinxHardwareController.setGPIOValueJNI(Constants.B_Onn, "1");
//        ForlinxHardwareController.setRedLedValueJNI(Constants.RledOff, "0");
//        sleep(75);
//        ForlinxHardwareController.setGPIOValueJNI(Constants.B_Off, "0");
//        ForlinxHardwareController.setRedLedValueJNI(Constants.RledOnn, "1");
//        sleep(150);
//        ForlinxHardwareController.setGPIOValueJNI(Constants.B_Onn, "1");
//        ForlinxHardwareController.setRedLedValueJNI(Constants.RledOff, "0");
//        sleep(75);
//        ForlinxHardwareController.setGPIOValueJNI(Constants.B_Off, "0");
//        ForlinxHardwareController.setRedLedValueJNI(Constants.RledOnn, "1");
//        sleep(150);
//        ForlinxHardwareController.setGPIOValueJNI(Constants.B_Onn, "1");
//        ForlinxHardwareController.setRedLedValueJNI(Constants.RledOff, "0");
//        sleep(75);
//        ForlinxHardwareController.setGPIOValueJNI(Constants.B_Off, "0");
//        ForlinxHardwareController.setRedLedValueJNI(Constants.RledOnn, "1");
//        sleep(150);
//        ForlinxHardwareController.setGPIOValueJNI(Constants.B_Onn, "1");
//        ForlinxHardwareController.setRedLedValueJNI(Constants.RledOff, "0");


//        ForlinxHardwareController.setGPIOValueJNI(Constants.B_Off, "0");
//        ForlinxHardwareController.setRedLedValueJNI(Constants.RledOnn, "1");
//        sleep(150);
//        ForlinxHardwareController.setGPIOValueJNI(Constants.B_Onn, "1");
//        ForlinxHardwareController.setRedLedValueJNI(Constants.RledOff, "0");
//        sleep(75);
//        ForlinxHardwareController.setGPIOValueJNI(Constants.B_Off, "0");
//        ForlinxHardwareController.setRedLedValueJNI(Constants.RledOnn, "1");
//        sleep(150);
//        ForlinxHardwareController.setGPIOValueJNI(Constants.B_Onn, "1");
//        ForlinxHardwareController.setRedLedValueJNI(Constants.RledOff, "0");
//        sleep(75);
//        ForlinxHardwareController.setGPIOValueJNI(Constants.B_Off, "0");
//        ForlinxHardwareController.setRedLedValueJNI(Constants.RledOnn, "1");
//        sleep(150);
//        ForlinxHardwareController.setGPIOValueJNI(Constants.B_Onn, "1");
//        ForlinxHardwareController.setRedLedValueJNI(Constants.RledOff, "0");
//        sleep(75);
//        ForlinxHardwareController.setGPIOValueJNI(Constants.B_Off, "0");
//        ForlinxHardwareController.setRedLedValueJNI(Constants.RledOnn, "1");
//        sleep(150);
//        ForlinxHardwareController.setGPIOValueJNI(Constants.B_Onn, "1");
//        ForlinxHardwareController.setRedLedValueJNI(Constants.RledOff, "0");
//        sleep(75);
//        ForlinxHardwareController.setGPIOValueJNI(Constants.B_Off, "0");
//        ForlinxHardwareController.setRedLedValueJNI(Constants.RledOnn, "1");
//        sleep(150);
//        ForlinxHardwareController.setGPIOValueJNI(Constants.B_Onn, "1");
//        ForlinxHardwareController.setRedLedValueJNI(Constants.RledOff, "0");

    }

    public static void setLCDBackLightOn() {
        ForlinxGPIOCommunicator.setGPIO(Constants.LCD_BACKLIGHT_EN_PATH, "0");
    }

    public static void setLCDBackLightOff() {
        ForlinxGPIOCommunicator.setGPIO(Constants.LCD_BACKLIGHT_EN_PATH, "1");
    }

}
