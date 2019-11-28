package com.android.fortunaattendancesystem.extras;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.android.fortunaattendancesystem.constant.Constants;
import com.android.fortunaattendancesystem.info.ProcessInfo;
import com.android.fortunaattendancesystem.submodules.ForlinxGPIOCommunicator;
import com.android.fortunaattendancesystem.submodules.MorphoCommunicator;
import com.morpho.morphosmart.sdk.MorphoDatabase;
import com.morpho.morphosmart.sdk.MorphoDevice;


public class ShutdownReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        final String action = intent.getAction();
        if (action != null && Intent.ACTION_SHUTDOWN.equals(intent.getAction())) {

            ForlinxGPIOCommunicator.setGPIO(Constants.BUZZ_PATH, "1");
            ForlinxGPIOCommunicator.setGPIO(Constants.GREEN_LED_PATH, "1");
            ForlinxGPIOCommunicator.setGPIO(Constants.RED_LED_PATH, "1");

            MorphoDevice morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
            MorphoDatabase morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
            if (morphoDevice != null && morphoDatabase != null) {
                boolean bioCommandStart = ProcessInfo.getInstance().isCommandBioStart();
                if (bioCommandStart) {
                    MorphoCommunicator morphoComm = new MorphoCommunicator();
                    morphoComm.stopFingerIdentification();
                }
            }
        }
    }
}


