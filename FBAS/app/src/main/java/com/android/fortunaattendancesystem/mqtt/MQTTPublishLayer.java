package com.android.fortunaattendancesystem.mqtt;

import com.android.fortunaattendancesystem.singleton.MqttClientInfo;
import com.android.fortunaattendancesystem.submodules.SQLiteCommunicator;

import org.eclipse.paho.android.service.MqttAndroidClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by fortuna on 27/5/19.
 */

public class MQTTPublishLayer {

    private static ExecutorService executorService = null;

    public void init() {
        if (executorService == null) {
            executorService = Executors.newFixedThreadPool(1);
            AutoEmpValAndTempUpload autoEmpValAndTempUpload = new AutoEmpValAndTempUpload();
            executorService.submit(autoEmpValAndTempUpload);
        }
    }

    private class AutoEmpValAndTempUpload implements Runnable {
        @Override
        public void run() {
            MqttAndroidClient client= MqttClientInfo.getInstance().getMqttAndroidClient();
            if(client!=null && client.isConnected()){

                SQLiteCommunicator dbComm=new SQLiteCommunicator();
                dbComm.getEmpValAndTempForUpload();


            }
        }
    }

}
