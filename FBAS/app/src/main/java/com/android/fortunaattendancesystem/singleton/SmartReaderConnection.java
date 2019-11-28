package com.android.fortunaattendancesystem.singleton;

import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;

/**
 * Created by fortuna on 26/7/16.
 */
public class SmartReaderConnection {

    private UsbDeviceConnection mConnection=null;
    private UsbEndpoint output=null;
    private UsbEndpoint input=null;
    private UsbInterface intf=null;

    private static SmartReaderConnection mInstance = null;

    public static SmartReaderConnection getInstance() {
        if (mInstance == null) {
            mInstance = new SmartReaderConnection();
            mInstance.reset();
        }
        return mInstance;
    }

    private void reset() {
        mConnection=null;
        output=null;
        input=null;
        intf=null;
    }

    public UsbDeviceConnection getmConnection() {
        return mConnection;
    }

    public void setmConnection(UsbDeviceConnection mConnection) {
        this.mConnection = mConnection;
    }

    public UsbEndpoint getOutput() {
        return output;
    }

    public void setOutput(UsbEndpoint output) {
        this.output = output;
    }

    public UsbEndpoint getInput() {
        return input;
    }

    public void setInput(UsbEndpoint input) {
        this.input = input;
    }

    public UsbInterface getIntf() {
        return intf;
    }

    public void setIntf(UsbInterface intf) {
        this.intf = intf;
    }

}
