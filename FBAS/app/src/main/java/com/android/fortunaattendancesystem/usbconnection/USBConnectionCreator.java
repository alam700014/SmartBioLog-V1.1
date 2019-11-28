package com.android.fortunaattendancesystem.usbconnection;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.widget.Toast;

import com.acpl.access_computech_fm220_sdk.acpl_FM220_SDK;
import com.android.fortunaattendancesystem.constant.Constants;
import com.android.fortunaattendancesystem.fm220.FM220callBack;
import com.android.fortunaattendancesystem.info.ProcessInfo;
import com.android.fortunaattendancesystem.singleton.RC632ReaderConnection;
import com.android.fortunaattendancesystem.singleton.SmartReaderConnection;
import com.android.fortunaattendancesystem.submodules.MorphoCommunicator;
import com.android.fortunaattendancesystem.tools.MorphoTools;
import com.friendlyarm.AndroidSDK.SPI;
import com.friendlyarm.AndroidSDK.ShellUtils;
import com.friendlyarm.SmartReader.RC632Api;
import com.friendlyarm.SmartReader.SmartCardApi;
import com.friendlyarm.SmartReader.SmartFinger;
import com.morpho.morphosmart.sdk.MorphoDatabase;
import com.morpho.morphosmart.sdk.MorphoDevice;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by fortuna on 21/6/16.
 */


public abstract class USBConnectionCreator extends Activity {

    private UsbDeviceConnection fingerReaderConnection, smartReaderConnection;
    private UsbDevice morphoUSBDevice, fortunaUSBDevice, startekUSBFM200UDevice;

    private MorphoDevice morphoDevice = new MorphoDevice();
    private MorphoDatabase morphoDatabase = new MorphoDatabase();

    private UsbEndpoint output = null;
    private UsbEndpoint input = null;
    private UsbInterface intf = null;

    public static PendingIntent mPermissionIntent = null;
    public static UsbManager mUsbManager = null;
    public static IntentFilter filter = null;


    /***************************************************
     * this is use at FM220_SDK
     * if you are use telecom/Locked device set the "Telecom_Device_Key" as your provided key otherwise send "" ;
     */
    private acpl_FM220_SDK FM220SDK;
    final String Telecom_Device_Key = "";
    boolean isStartekFm200uRcvRegisterd = false;
    public boolean isFingerReaderFound = false;
    FM220callBack fm220callBack;
    // private static final String ACTION_USB_PERMISSION = "com.access.testappfm220.USB_PERMISSION";

    public void initUSBManagerReceiver() {
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(Constants.ACTION_USB_PERMISSION), 0);
        filter = new IntentFilter(Constants.ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
    }

    public void searchDevices(int deviceType) {

        //  USBManager.getInstance().initialize(USBConnectionCreator.this,"com.android.fortunaattendancesystem.USB_ACTION");

        UsbManager usbManager = (UsbManager) this.getSystemService(Context.USB_SERVICE);
        HashMap <String, UsbDevice> usbDeviceList = usbManager.getDeviceList();
        Iterator <UsbDevice> usbDeviceIterator = usbDeviceList.values().iterator();

        switch (deviceType) {

            //======================== Morpho Finger Reader ===========================//

            case 1:

                while (usbDeviceIterator.hasNext()) {
                    UsbDevice usbDevice = usbDeviceIterator.next();
                    if (usbDevice != null) {
                        if (MorphoTools.isSupported(usbDevice.getVendorId(), usbDevice.getProductId())) {
                            morphoUSBDevice = usbDevice;
                            boolean hasPermission = mUsbManager.hasPermission(usbDevice);
                            if (!hasPermission) {
                                mUsbManager.requestPermission(usbDevice, mPermissionIntent);
                            } else {
                                morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
                                morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
                                if (morphoDevice == null && morphoDatabase == null) {
                                    morphoDevice = new MorphoDevice();
                                    morphoDatabase = new MorphoDatabase();
                                    fingerReaderConnection = mUsbManager.openDevice(usbDevice);
                                    if (fingerReaderConnection != null) {
                                        boolean isMorphoDeviceFound = false;
                                        isMorphoDeviceFound = MorphoCommunicator.getMorphoUSBConnection(fingerReaderConnection, usbDevice, morphoDevice);
                                        if (isMorphoDeviceFound) {
                                            boolean foundMorphoDataBase = false;
                                            foundMorphoDataBase = MorphoCommunicator.getMorphoDataBaseConnection(morphoDevice, morphoDatabase);
                                            if (foundMorphoDataBase) {
                                                updateFrConStatusToUI(true);
                                                ProcessInfo.getInstance().setMorphoDevice(morphoDevice);
                                                ProcessInfo.getInstance().setMorphoDatabase(morphoDatabase);
                                                //enableDisableHardwareOperationButtons(true);
                                            } else {
                                                Toast.makeText(USBConnectionCreator.this, "Finger Reader Database Not Found", Toast.LENGTH_LONG).show();
                                            }
                                        } else {
                                            Toast.makeText(USBConnectionCreator.this, "Failed To Find Finger Reader", Toast.LENGTH_LONG).show();
                                        }
                                    } else {
                                        Toast.makeText(USBConnectionCreator.this, "Finger Reader Connection Failure", Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    updateFrConStatusToUI(true);
                                }
                            }
                        }
                    }
                }

                break;

            //======================== Micro Smart V2 Smart Reader ===========================//

            case 2:

                while (usbDeviceIterator.hasNext()) {
                    UsbDevice usbDevice = usbDeviceIterator.next();
                    if (usbDevice != null) {
                        if (usbDevice.getProductId() == 8194) {
                            fortunaUSBDevice = usbDevice;
                            boolean hasPermission = mUsbManager.hasPermission(usbDevice);
                            if (!hasPermission) {
                                mUsbManager.requestPermission(usbDevice, mPermissionIntent);
                            } else {
                                try {
                                    smartReaderConnection = SmartReaderConnection.getInstance().getmConnection();
                                    intf = SmartReaderConnection.getInstance().getIntf();
                                    input = SmartReaderConnection.getInstance().getInput();
                                    output = SmartReaderConnection.getInstance().getOutput();
                                    if (smartReaderConnection == null && intf == null && input == null && output == null) {
                                        for (int i = 0; i < fortunaUSBDevice.getInterfaceCount(); i++) { // count interface
                                            if (fortunaUSBDevice.getInterface(i).getInterfaceClass() == UsbConstants.USB_CLASS_CDC_DATA) {   // Searching USB device class
                                                intf = fortunaUSBDevice.getInterface(i); // get interface
                                                for (int j = 0; j < intf.getEndpointCount(); j++) {  // find endpoint
                                                    if (intf.getEndpoint(j).getDirection() == UsbConstants.USB_DIR_OUT && intf.getEndpoint(j).getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                                                        output = intf.getEndpoint(j); // get output endpoint
                                                    }
                                                    if (intf.getEndpoint(j).getDirection() == UsbConstants.USB_DIR_IN && intf.getEndpoint(j).getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                                                        input = intf.getEndpoint(j); // get input endpoint
                                                    }
                                                }
                                            }
                                        }
                                        smartReaderConnection = mUsbManager.openDevice(fortunaUSBDevice);  //device open
                                        if ((smartReaderConnection != null) && (smartReaderConnection.claimInterface(intf, true)) && intf != null && input != null && output != null) {   //connection checking
                                            SmartReaderConnection.getInstance().setmConnection(smartReaderConnection);
                                            SmartReaderConnection.getInstance().setIntf(intf);
                                            SmartReaderConnection.getInstance().setInput(input);
                                            SmartReaderConnection.getInstance().setOutput(output);
                                            updateSrConStatusToUI(true);
                                            //enableDisableHardwareOperationButtons(true);

                                        } else {
                                            Toast.makeText(USBConnectionCreator.this, "Smart Reader Connection Failure", Toast.LENGTH_SHORT).show();
                                            SmartReaderConnection.getInstance().setmConnection(null);
                                            SmartReaderConnection.getInstance().setIntf(null);
                                            SmartReaderConnection.getInstance().setInput(null);
                                            SmartReaderConnection.getInstance().setOutput(null);
                                        }
                                    } else {
                                        updateSrConStatusToUI(true);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }

                break;

            //======================== Morpho Finger Reader + Smart Reader ===========================//

            case 3:

                while (usbDeviceIterator.hasNext()) {
                    UsbDevice usbDevice = usbDeviceIterator.next();
                    if (usbDevice != null) {
                        if (MorphoTools.isSupported(usbDevice.getVendorId(), usbDevice.getProductId())) {
                            morphoUSBDevice = usbDevice;
                            boolean hasPermission = mUsbManager.hasPermission(usbDevice);
                            if (!hasPermission) {
                                mUsbManager.requestPermission(usbDevice, mPermissionIntent);
                            } else {
                                morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
                                morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
                                if (morphoDevice == null && morphoDatabase == null) {
                                    morphoDevice = new MorphoDevice();
                                    morphoDatabase = new MorphoDatabase();
                                    fingerReaderConnection = mUsbManager.openDevice(usbDevice);
                                    if (fingerReaderConnection != null) {
                                        boolean isMorphoDeviceFound = false;
                                        isMorphoDeviceFound = MorphoCommunicator.getMorphoUSBConnection(fingerReaderConnection, usbDevice, morphoDevice);
                                        if (isMorphoDeviceFound) {
                                            boolean foundMorphoDataBase = false;
                                            foundMorphoDataBase = MorphoCommunicator.getMorphoDataBaseConnection(morphoDevice, morphoDatabase);
                                            if (foundMorphoDataBase) {
                                                updateFrConStatusToUI(true);
                                                ProcessInfo.getInstance().setMorphoDevice(morphoDevice);
                                                ProcessInfo.getInstance().setMorphoDatabase(morphoDatabase);
                                                //enableDisableHardwareOperationButtons(true);
                                            } else {
                                                Toast.makeText(USBConnectionCreator.this, "Finger Reader Database Not Found", Toast.LENGTH_LONG).show();
                                            }
                                        } else {
                                            Toast.makeText(USBConnectionCreator.this, "Failed To Find Finger Reader", Toast.LENGTH_LONG).show();
                                        }
                                    } else {
                                        Toast.makeText(USBConnectionCreator.this, "Finger Reader Connection Failure", Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    updateFrConStatusToUI(true);
                                }
                            }
                        } else if (usbDevice.getProductId() == 8194) {
                            fortunaUSBDevice = usbDevice;
                            boolean hasPermission = mUsbManager.hasPermission(usbDevice);
                            if (!hasPermission) {
                                mUsbManager.requestPermission(usbDevice, mPermissionIntent);
                            } else {
                                try {
                                    smartReaderConnection = SmartReaderConnection.getInstance().getmConnection();
                                    intf = SmartReaderConnection.getInstance().getIntf();
                                    input = SmartReaderConnection.getInstance().getInput();
                                    output = SmartReaderConnection.getInstance().getOutput();
                                    if (smartReaderConnection == null && intf == null && input == null && output == null) {
                                        if (fortunaUSBDevice != null) {
                                            for (int i = 0; i < fortunaUSBDevice.getInterfaceCount(); i++) { // count interface
                                                if (fortunaUSBDevice.getInterface(i).getInterfaceClass() == UsbConstants.USB_CLASS_CDC_DATA) {   // Searching USB device class
                                                    intf = fortunaUSBDevice.getInterface(i); // get interface
                                                    for (int j = 0; j < intf.getEndpointCount(); j++) {  // find endpoint
                                                        if (intf.getEndpoint(j).getDirection() == UsbConstants.USB_DIR_OUT && intf.getEndpoint(j).getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                                                            output = intf.getEndpoint(j); // get output endpoint
                                                        }
                                                        if (intf.getEndpoint(j).getDirection() == UsbConstants.USB_DIR_IN && intf.getEndpoint(j).getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                                                            input = intf.getEndpoint(j); // get input endpoint
                                                        }
                                                    }
                                                }
                                            }
                                            smartReaderConnection = mUsbManager.openDevice(fortunaUSBDevice);  //device open
                                            if ((smartReaderConnection != null) && (smartReaderConnection.claimInterface(intf, true)) && intf != null && input != null && output != null) {   //connection checking
                                                SmartReaderConnection.getInstance().setmConnection(smartReaderConnection);
                                                SmartReaderConnection.getInstance().setIntf(intf);
                                                SmartReaderConnection.getInstance().setInput(input);
                                                SmartReaderConnection.getInstance().setOutput(output);
                                                updateSrConStatusToUI(true);
                                                //enableDisableHardwareOperationButtons(true);

                                            } else {
                                                Toast.makeText(USBConnectionCreator.this, "Smart Reader Connection Failure", Toast.LENGTH_SHORT).show();
                                                SmartReaderConnection.getInstance().setmConnection(null);
                                                SmartReaderConnection.getInstance().setIntf(null);
                                                SmartReaderConnection.getInstance().setInput(null);
                                                SmartReaderConnection.getInstance().setOutput(null);
                                            }
                                        }
                                    } else {
                                        updateSrConStatusToUI(true);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }

                break;

            case 4:
                break;

            default:
                break;
        }
    }

    public BroadcastReceiver mMorphoSmartReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Constants.ACTION_USB_PERMISSION.equals(action)) {
                UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (usbDevice != null) {
                    int productId = usbDevice.getProductId();
                    synchronized (this) {
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            if (MorphoTools.isSupported(usbDevice.getVendorId(), usbDevice.getProductId())) {
                                boolean hasPermission = mUsbManager.hasPermission(usbDevice);
                                if (hasPermission) {
                                    fingerReaderConnection = mUsbManager.openDevice(usbDevice);
                                    if (fingerReaderConnection != null) {
                                        boolean isMorphoDeviceFound = false;
                                        isMorphoDeviceFound = MorphoCommunicator.getMorphoUSBConnection(fingerReaderConnection, usbDevice, morphoDevice);
                                        if (isMorphoDeviceFound) {
                                            boolean foundMorphoDataBase = false;
                                            foundMorphoDataBase = MorphoCommunicator.getMorphoDataBaseConnection(morphoDevice, morphoDatabase);
                                            if (foundMorphoDataBase) {
                                                ProcessInfo.getInstance().setMorphoDevice(morphoDevice);
                                                ProcessInfo.getInstance().setMorphoDatabase(morphoDatabase);
                                                updateFrConStatusToUI(true);
                                                initIdentification();
                                                //enableDisableHardwareOperationButtons(true);
                                                if (fortunaUSBDevice != null) {
                                                    boolean hasReaderPermission = mUsbManager.hasPermission(fortunaUSBDevice);
                                                    if (!hasReaderPermission) {
                                                        mUsbManager.requestPermission(fortunaUSBDevice, mPermissionIntent);
                                                    }
                                                }
                                            } else {
                                                Toast.makeText(USBConnectionCreator.this, "Failed To Find Finger Reader Database", Toast.LENGTH_LONG).show();
                                            }

                                        } else {
                                            Toast.makeText(USBConnectionCreator.this, "Finger Reader Not Found", Toast.LENGTH_LONG).show();
                                        }
                                    } else {
                                        Toast.makeText(USBConnectionCreator.this, "Finger Reader Connection Failure", Toast.LENGTH_LONG).show();
                                    }

                                } else {
                                    Toast.makeText(USBConnectionCreator.this, "Finger Reader Permission Failure", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                //==============================FORTUNA READER PRODUCT ID===============================//

                                if (productId == 8194) {
                                    boolean hasPermission = mUsbManager.hasPermission(usbDevice);
                                    if (hasPermission) {
                                        try {
                                            if (fortunaUSBDevice != null) {
                                                int interfaceCount = fortunaUSBDevice.getInterfaceCount();
                                                for (int i = 0; i < fortunaUSBDevice.getInterfaceCount(); i++) { // count interface
                                                    if (fortunaUSBDevice.getInterface(i).getInterfaceClass() == UsbConstants.USB_CLASS_CDC_DATA) {   // Searching USB device class
                                                        intf = fortunaUSBDevice.getInterface(i); // get interface
                                                        for (int j = 0; j < intf.getEndpointCount(); j++) {  // find endpoint
                                                            if (intf.getEndpoint(j).getDirection() == UsbConstants.USB_DIR_OUT && intf.getEndpoint(j).getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                                                                output = intf.getEndpoint(j); // get output endpoint
                                                            }
                                                            if (intf.getEndpoint(j).getDirection() == UsbConstants.USB_DIR_IN && intf.getEndpoint(j).getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                                                                input = intf.getEndpoint(j); // get input endpoint
                                                            }
                                                        }
                                                    }
                                                }
                                                smartReaderConnection = mUsbManager.openDevice(fortunaUSBDevice);  //device open
                                                if ((smartReaderConnection != null) && (smartReaderConnection.claimInterface(intf, true)) && intf != null && input != null && output != null) {   //connection checking
                                                    SmartReaderConnection.getInstance().setmConnection(smartReaderConnection);
                                                    SmartReaderConnection.getInstance().setIntf(intf);
                                                    SmartReaderConnection.getInstance().setInput(input);
                                                    SmartReaderConnection.getInstance().setOutput(output);
                                                    updateSrConStatusToUI(true);
                                                    initCardRead();
                                                    //enableDisableHardwareOperationButtons(true);
                                                    if (morphoUSBDevice != null) {
                                                        boolean hasMorphoPermission = mUsbManager.hasPermission(morphoUSBDevice);
                                                        if (!hasMorphoPermission) {
                                                            mUsbManager.requestPermission(morphoUSBDevice, mPermissionIntent);
                                                        }
                                                    }
                                                } else {
                                                    Toast.makeText(USBConnectionCreator.this, "Smart Reader Connection Failure", Toast.LENGTH_SHORT).show();
                                                    SmartReaderConnection.getInstance().setmConnection(null);
                                                    SmartReaderConnection.getInstance().setIntf(null);
                                                    SmartReaderConnection.getInstance().setInput(null);
                                                    SmartReaderConnection.getInstance().setOutput(null);
                                                }
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        Toast.makeText(USBConnectionCreator.this, "Smart Reader Permission Failure", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }
                        // User has granted permission
                        // ... Setup your UsbDeviceConnection via mUsbManager.openDevice(usbDevice) ...

                        else {
                            if (MorphoTools.isSupported(usbDevice.getVendorId(), usbDevice.getProductId())) {
                                Toast.makeText(USBConnectionCreator.this, "User Denied Permission for Finger Reader", Toast.LENGTH_LONG).show();
                            } else if (usbDevice.getProductId() == 8194) {
                                Toast.makeText(USBConnectionCreator.this, "User Denied Permission for Smart Reader", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }
            }
            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (usbDevice != null) {
                    synchronized (this) {
                        if (MorphoTools.isSupported(usbDevice.getVendorId(), usbDevice.getProductId())) {
                            morphoUSBDevice = null;
                            ProcessInfo.getInstance().setStarted(false);
                            ProcessInfo.getInstance().setCommandBioStart(false);
                            ProcessInfo.getInstance().setIdentificationStarted(false);
                            morphoDevice.cancelLiveAcquisition();
                           // morphoDevice.closeDevice();
                            ProcessInfo.getInstance().setMorphoDevice(null);
                            ProcessInfo.getInstance().setMorphoDatabase(null);
                            //  enableDisableHardwareOperationButtons(false);
                            updateFrConStatusToUI(false);
                            Toast.makeText(USBConnectionCreator.this, "Finger Reader Detached", Toast.LENGTH_LONG).show();
                        } else if (usbDevice.getProductId() == 8194) {
                            fortunaUSBDevice = null;
                            SmartReaderConnection.getInstance().setIntf(null);
                            SmartReaderConnection.getInstance().setInput(null);
                            SmartReaderConnection.getInstance().setOutput(null);
                            SmartReaderConnection.getInstance().setmConnection(null);
                            //enableDisableHardwareOperationButtons(false);
                            updateSrConStatusToUI(false);
                            Toast.makeText(USBConnectionCreator.this, "Smart Reader Detached", Toast.LENGTH_LONG).show();
                        }
                    }
                }

            }
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (usbDevice != null) {
                    synchronized (this) {
                        if (MorphoTools.isSupported(usbDevice.getVendorId(), usbDevice.getProductId())) {
                            Toast.makeText(USBConnectionCreator.this, "Finger Reader Attached", Toast.LENGTH_LONG).show();
                            morphoUSBDevice = usbDevice;
                            boolean hasPermission = mUsbManager.hasPermission(usbDevice);
                            if (!hasPermission) {
                                mUsbManager.requestPermission(usbDevice, mPermissionIntent);
                            }
                        }
                        //8194=0x2002
                        else if (usbDevice.getProductId() == 8194) {
                            Toast.makeText(USBConnectionCreator.this, "Smart Reader Attached", Toast.LENGTH_LONG).show();
                            fortunaUSBDevice = usbDevice;
                            boolean hasPermission = mUsbManager.hasPermission(usbDevice);
                            if (!hasPermission) {
                                mUsbManager.requestPermission(usbDevice, mPermissionIntent);
                            }
                        }
                    }
                }
            }
        }
    };

    public BroadcastReceiver mSmartReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Constants.ACTION_USB_PERMISSION.equals(action)) {
                UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (usbDevice != null) {
                    int productId = usbDevice.getProductId();
                    synchronized (this) {
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {

                            //==============================FORTUNA READER PRODUCT ID===============================//

                            if (productId == 8194) {
                                boolean hasPermission = mUsbManager.hasPermission(usbDevice);
                                if (hasPermission) {
                                    try {
                                        if (fortunaUSBDevice != null) {
                                            for (int i = 0; i < fortunaUSBDevice.getInterfaceCount(); i++) { // count interface
                                                if (fortunaUSBDevice.getInterface(i).getInterfaceClass() == UsbConstants.USB_CLASS_CDC_DATA) {   // Searching USB device class
                                                    intf = fortunaUSBDevice.getInterface(i); // get interface
                                                    for (int j = 0; j < intf.getEndpointCount(); j++) {  // find endpoint
                                                        if (intf.getEndpoint(j).getDirection() == UsbConstants.USB_DIR_OUT && intf.getEndpoint(j).getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                                                            output = intf.getEndpoint(j); // get output endpoint
                                                        }
                                                        if (intf.getEndpoint(j).getDirection() == UsbConstants.USB_DIR_IN && intf.getEndpoint(j).getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                                                            input = intf.getEndpoint(j); // get input endpoint
                                                        }
                                                    }
                                                }
                                            }
                                            smartReaderConnection = mUsbManager.openDevice(fortunaUSBDevice);  //device open
                                            if ((smartReaderConnection != null) && (smartReaderConnection.claimInterface(intf, true)) && intf != null && input != null && output != null) {   //connection checking
                                                SmartReaderConnection.getInstance().setmConnection(smartReaderConnection);
                                                SmartReaderConnection.getInstance().setIntf(intf);
                                                SmartReaderConnection.getInstance().setInput(input);
                                                SmartReaderConnection.getInstance().setOutput(output);
                                                updateSrConStatusToUI(true);
                                                initCardRead();
                                                //enableDisableHardwareOperationButtons(true);
                                            } else {
                                                SmartReaderConnection.getInstance().setmConnection(null);
                                                SmartReaderConnection.getInstance().setIntf(null);
                                                SmartReaderConnection.getInstance().setInput(null);
                                                SmartReaderConnection.getInstance().setOutput(null);
                                                Toast.makeText(USBConnectionCreator.this, "Smart Reader Connection Failure", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    Toast.makeText(USBConnectionCreator.this, "Smart Reader Permission Failure", Toast.LENGTH_SHORT).show();
                                }
                            }

                        }
                        // User has granted permission
                        // ... Setup your UsbDeviceConnection via mUsbManager.openDevice(usbDevice) ...
                        else {
                            if (usbDevice.getProductId() == 8194) {
                                Toast.makeText(USBConnectionCreator.this, "User Denied Permission for Smart Reader", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }
            }
            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (usbDevice != null) {
                    synchronized (this) {
                        if (usbDevice.getProductId() == 8194) {
                            fortunaUSBDevice = null;
                            SmartReaderConnection.getInstance().setIntf(null);
                            SmartReaderConnection.getInstance().setInput(null);
                            SmartReaderConnection.getInstance().setOutput(null);
                            SmartReaderConnection.getInstance().setmConnection(null);
                            //enableDisableHardwareOperationButtons(false);
                            updateSrConStatusToUI(false);
                            Toast.makeText(USBConnectionCreator.this, "Smart Reader Detached", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (usbDevice != null) {
                    synchronized (this) {
                        //8194=0x2002
                        if (usbDevice.getProductId() == 8194) {
                            Toast.makeText(USBConnectionCreator.this, "Smart Reader Attached", Toast.LENGTH_LONG).show();
                            fortunaUSBDevice = usbDevice;
                            fortunaUSBDevice = usbDevice;
                            boolean hasPermission = mUsbManager.hasPermission(usbDevice);
                            if (!hasPermission) {
                                mUsbManager.requestPermission(usbDevice, mPermissionIntent);
                            }
                        }
                    }
                }
            }
        }
    };


    public BroadcastReceiver mMorphoReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Constants.ACTION_USB_PERMISSION.equals(action)) {
                UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (usbDevice != null) {
                    int productId = usbDevice.getProductId();
                    synchronized (this) {
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            if (MorphoTools.isSupported(usbDevice.getVendorId(), usbDevice.getProductId())) {
                                boolean hasPermission = mUsbManager.hasPermission(usbDevice);
                                if (hasPermission) {
                                    fingerReaderConnection = mUsbManager.openDevice(usbDevice);
                                    if (fingerReaderConnection != null) {
                                        boolean isMorphoDeviceFound = false;
                                        isMorphoDeviceFound = MorphoCommunicator.getMorphoUSBConnection(fingerReaderConnection, usbDevice, morphoDevice);
                                        if (isMorphoDeviceFound) {
                                            boolean foundMorphoDataBase = false;
                                            foundMorphoDataBase = MorphoCommunicator.getMorphoDataBaseConnection(morphoDevice, morphoDatabase);
                                            if (foundMorphoDataBase) {
                                                ProcessInfo.getInstance().setMorphoDevice(morphoDevice);
                                                ProcessInfo.getInstance().setMorphoDatabase(morphoDatabase);
                                                updateFrConStatusToUI(true);
                                                initIdentification();
                                            } else {
                                                Toast.makeText(USBConnectionCreator.this, "Failed To Find Finger Reader Database", Toast.LENGTH_LONG).show();
                                            }

                                        } else {
                                            Toast.makeText(USBConnectionCreator.this, "Finger Reader Not Found", Toast.LENGTH_LONG).show();
                                        }
                                    } else {
                                        Toast.makeText(USBConnectionCreator.this, "Finger Reader Connection Failure", Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    Toast.makeText(USBConnectionCreator.this, "Finger Reader Permission Failure", Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                        // User has granted permission
                        // ... Setup your UsbDeviceConnection via mUsbManager.openDevice(usbDevice) ...

                        else {
                            if (MorphoTools.isSupported(usbDevice.getVendorId(), usbDevice.getProductId())) {
                                Toast.makeText(USBConnectionCreator.this, "User Denied Permission for Finger Reader", Toast.LENGTH_LONG).show();
                            }

                        }
                    }
                }
            }
            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (usbDevice != null) {
                    synchronized (this) {
                        if (MorphoTools.isSupported(usbDevice.getVendorId(), usbDevice.getProductId())) {
                            morphoUSBDevice = null;

                            ProcessInfo.getInstance().setStarted(false);
                            ProcessInfo.getInstance().setCommandBioStart(false);
                            ProcessInfo.getInstance().setIdentificationStarted(false);

                            morphoDevice.cancelLiveAcquisition();

                            //morphoDevice.closeDevice();

                            ProcessInfo.getInstance().setMorphoDevice(null);
                            ProcessInfo.getInstance().setMorphoDatabase(null);
                            //  enableDisableHardwareOperationButtons(false);
                            updateFrConStatusToUI(false);
                            Toast.makeText(USBConnectionCreator.this, "Finger Reader Detached", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (usbDevice != null) {
                    synchronized (this) {
                        if (MorphoTools.isSupported(usbDevice.getVendorId(), usbDevice.getProductId())) {
                            Toast.makeText(USBConnectionCreator.this, "Finger Reader Attached", Toast.LENGTH_LONG).show();
                            morphoUSBDevice = usbDevice;
                            boolean hasPermission = mUsbManager.hasPermission(usbDevice);
                            if (!hasPermission) {
                                mUsbManager.requestPermission(usbDevice, mPermissionIntent);
                            }
                        }
                    }
                }
            }
        }
    };


    //====================Init Methods For Spi Smart Reader RC632===========//


    final static int irqPinNo = 62;
    final static int resetPinNo = 63;

    public boolean initSpi() {
        boolean initStatus = false;
        int spi_fd = -1, status = -1;
        SPI spiApi = new SPI();
        spi_fd = spiApi.begin();
        if (spi_fd > 0) {
            status = spiApi.setDataMode(0);
            if (status == 0) {
                status = spiApi.setSpiSpeed(7500000);
                if (status == 0) {
                    RC632Api rc632Api = new RC632Api(spi_fd, spiApi);
                    initStatus = rc632Api.rs632Init();
                    if (initStatus) {
                        SmartCardApi smartCardApi = new SmartCardApi(rc632Api);
                        SmartFinger smartFinger = new SmartFinger(smartCardApi);
                        RC632ReaderConnection.getInstance().setSmartFinger(smartFinger);
                        updateSrConStatusToUI(true);
                    } else {
                        updateSrConStatusToUI(false);
                    }
                } else {
                    updateSrConStatusToUI(false);
                }
            } else {
                updateSrConStatusToUI(false);
            }
        } else {
            updateSrConStatusToUI(false);
        }
        return initStatus;
    }

    public void initPins() {

        ShellUtils.execCommand("chmod 0777" + " /dev/spidev0.0", true);

        ShellUtils.execCommand("chmod 0777" + " /sys/class/gpio/export", true);
        writeSysfs("/sys/class/gpio/export", Integer.toString(irqPinNo));
        writeSysfs("/sys/class/gpio/export", Integer.toString(resetPinNo));

        ShellUtils.execCommand("chmod 0777" + " /sys/class/gpio/gpio" + Integer.toString(irqPinNo) + "/direction", true);
        ShellUtils.execCommand("chmod 0777" + " /sys/class/gpio/gpio" + Integer.toString(irqPinNo) + "/value", true);

        ShellUtils.execCommand("chmod 0777" + " /sys/class/gpio/gpio" + Integer.toString(resetPinNo) + "/direction", true);
        ShellUtils.execCommand("chmod 0777" + " /sys/class/gpio/gpio" + Integer.toString(resetPinNo) + "/value", true);

        writeSysfs("/sys/class/gpio/gpio" + Integer.toString(irqPinNo) + "/direction", "in");
        writeSysfs("/sys/class/gpio/gpio" + Integer.toString(resetPinNo) + "/direction", "out");
    }


    public boolean writeSysfs(String path, String value) { //对文件进行写操作
        BufferedWriter writer = null;
        if (!new File(path).exists()) {
            return false;
        }
        try {
            writer = new BufferedWriter(new FileWriter(path), 64);
            writer.write(value);
            writer.flush();
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //Abstract Methods

    abstract public void updateFrConStatusToUI(boolean status);
    abstract public void updateSrConStatusToUI(boolean status);
    abstract public void initIdentification();
    abstract public void initCardRead();
    abstract public void resetConnections();

//    abstract public void enableDisableHardwareOperationButtons(boolean isEnable);

}

