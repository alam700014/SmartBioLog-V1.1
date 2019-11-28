package com.android.fortunaattendancesystem.singleton;

import org.eclipse.paho.android.service.MqttAndroidClient;

/**
 * Created by fortuna on 4/10/18.
 */

public class MqttClientInfo {

    private static MqttClientInfo mInstance = null;
    private MqttAndroidClient mqttAndroidClient=null;

    public static MqttClientInfo getInstance() {
        if (mInstance == null) {
            mInstance = new MqttClientInfo();
            mInstance.reset();
        }
        return mInstance;
    }

    public MqttAndroidClient getMqttAndroidClient() {
        return mqttAndroidClient;
    }

    public void setMqttAndroidClient(MqttAndroidClient mqttAndroidClient) {
        this.mqttAndroidClient = mqttAndroidClient;
    }

    private void reset() {
        mqttAndroidClient=null;
    }
}
