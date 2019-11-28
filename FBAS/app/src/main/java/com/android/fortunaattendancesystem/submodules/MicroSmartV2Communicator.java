package com.android.fortunaattendancesystem.submodules;

import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.util.Log;

import com.android.fortunaattendancesystem.constant.Constants;
import com.android.fortunaattendancesystem.helper.Utility;
import com.android.fortunaattendancesystem.model.SmartCardInfo;

import java.io.ByteArrayOutputStream;

import static android.os.SystemClock.sleep;

/**
 * Created by fortuna on 14/9/18.
 */

public class MicroSmartV2Communicator {

    UsbDeviceConnection usbConn;
    UsbInterface usbInterface;
    UsbEndpoint usbInput;
    UsbEndpoint usbOutput;

    public MicroSmartV2Communicator(UsbDeviceConnection usbConn, UsbInterface usbInterface, UsbEndpoint usbInput, UsbEndpoint usbOutput) {
        this.usbConn = usbConn;
        this.usbInterface = usbInterface;
        this.usbInput = usbInput;
        this.usbOutput = usbOutput;
    }

    public String readCardId(byte[] command) {

        synchronized (this) {
            sleep(500);

            int status = -1;
            byte[] readBytes = new byte[Constants.BUFFER_SIZE];
            byte[] readBytes1 = new byte[Constants.BUFFER_SIZE];

            status = usbConn.bulkTransfer(usbOutput, command, command.length, 0);
            status = usbConn.bulkTransfer(usbInput, readBytes, readBytes.length, 0100);
            status = usbConn.bulkTransfer(usbInput, readBytes1, readBytes1.length, 0100);

            String strReadCardId = new String(readBytes);
            char[] c = strReadCardId.trim().toCharArray();

            if (strReadCardId.contains("!?01")) {
                strReadCardId = "";
            } else {
                if (c.length > 0) {
                    if (c[1] == 'F' && c[2] == 'F') {
                        strReadCardId = strReadCardId.substring(6, 14);
                    } else if (c[1] != ' ' && c[2] != ' ') {
                        strReadCardId = strReadCardId.substring(4, 13);
                    }
                }
            }
            return strReadCardId;
        }
    }

    public byte[] readSector(int sectorNo, byte[] command) {

        if (sectorNo == 2) {//For ASCII Read
            try {
                byte[] readBytes = new byte[Constants.BUFFER_SIZE];
                int r1 = usbConn.bulkTransfer(usbOutput, command, command.length, 0500);
                int r = usbConn.bulkTransfer(usbInput, readBytes, readBytes.length, 0500);
                if (readBytes[0] == '!' && readBytes[4] == '|' && r == 5) {
                    return null;
                } else {
                    return readBytes;
                }
            } catch (Exception e) {
                return null;
            }
        } else { //For HEX Read
            try {
                byte[] readBytes = new byte[Constants.BUFFER_SIZE];
                int r1 = usbConn.bulkTransfer(usbOutput, command, command.length, 0500);
                int r = usbConn.bulkTransfer(usbInput, readBytes, readBytes.length, 0500);
                if (readBytes[0] == '!' && readBytes[4] == '|' && r == 5) {
                    return null;
                } else {
                    byte[] readBytes1 = new byte[Constants.BUFFER_SIZE];
                    int r2 = usbConn.bulkTransfer(usbInput, readBytes1, readBytes1.length, 0500);
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    outputStream.write(readBytes);
                    outputStream.write(readBytes1);
                    byte[] data = outputStream.toByteArray();
                    return data;
                }
            } catch (Exception e) {
                return null;
            }
        }
    }

    //=============================== Write Card Id ====================================//

    public int writeCardId(byte[] command) {

        int status = -1;
        byte[] readBytes = new byte[64];
        sleep(500);
        status = usbConn.bulkTransfer(usbOutput, command, command.length, 0);
        status = usbConn.bulkTransfer(usbInput, readBytes, readBytes.length, 0100);
        String strReadCardId = new String(readBytes);
        char[] c = strReadCardId.trim().toCharArray();
        if (c.length > 0 && c.length == 5) {
            status = -1;
        } else {
            status = 1;
        }

        return status;
    }

    //=============================== Sector Write =====================================//

    public int sectorWrite(String data, final int strSectorNo) {

        synchronized (this) {

            int status = -1;
            try {
                sleep(500);
                byte[] readBytes = new byte[Constants.BUFFER_SIZE];
                String strDataFirst = data.substring(0, 64);
                String strDataSecond = data.substring(64);
                byte[] messageFirst = strDataFirst.getBytes();
                byte[] messageSecond = strDataSecond.getBytes();
                status = usbConn.bulkTransfer(usbOutput, messageFirst, messageFirst.length, 0);
                status = usbConn.bulkTransfer(usbOutput, messageSecond, messageSecond.length, 0);
                status = usbConn.bulkTransfer(usbInput, readBytes, readBytes.length, 0100);
                String strResponse = new String(readBytes);
                char[] c = strResponse.trim().toCharArray();
                if (c != null && c.length > 0 && c.length == 5) {
                    status = -1;
                } else {
                    status = 1;
                }
            } catch (final Exception e) {
                status = -1;
            }
            if (status != -1) {
                Log.d("TEST", "Sector " + strSectorNo + " Write Successfully");
            } else {
                Log.d("TEST", "Sector " + strSectorNo + " Write Failed");
            }
            return status;
        }
    }

    //===============================================  Mad Update  ===========================================================//

    public int madUpdate(String sectorNo, int blockNo, String initialKeyB, String data) {

        synchronized (this) {
            sleep(500);
            int status = -1;
            byte[] readBytes = new byte[Constants.BUFFER_SIZE];
            String strCommand = "";
            byte[] byteCommand = null;
            strCommand=Utility.addCheckSum("#FF" + sectorNo + initialKeyB);
            byteCommand = strCommand.getBytes();
            status = usbConn.bulkTransfer(usbOutput, byteCommand, byteCommand.length, 0);
            status = usbConn.bulkTransfer(usbInput, readBytes, readBytes.length, 0100);
            char[] c = new String(readBytes).trim().toCharArray();
            if (c != null && c.length > 0 && c.length == 5) {
                status = -1;
            }
            if (status != -1) {
                sleep(500);
                String strBlockNo = Integer.toHexString(blockNo).toUpperCase();
                if (strBlockNo.trim().length() != 2) {
                    strBlockNo = "0" + strBlockNo;
                }
                strCommand=Utility.addCheckSum("#FF02" + strBlockNo + "00");
                byteCommand = strCommand.getBytes();
                status = usbConn.bulkTransfer(usbOutput, byteCommand, byteCommand.length, 0);
                status = usbConn.bulkTransfer(usbInput, readBytes, readBytes.length, 0100);
                c = new String(readBytes).trim().toCharArray();
                if (c != null && c.length > 0 && c.length == 5) {
                    status = -1;
                }
                if (status != -1) {
                    sleep(500);
                    strCommand=Utility.addCheckSum("#FF" + sectorNo + initialKeyB);
                    byteCommand = strCommand.getBytes();
                    status = usbConn.bulkTransfer(usbOutput, byteCommand, byteCommand.length, 0);
                    status = usbConn.bulkTransfer(usbInput, readBytes, readBytes.length, 0100);
                    c = new String(readBytes).trim().toCharArray();
                    if (c != null && c.length > 0 && c.length == 5) {
                        status = -1;
                    }
                    if (status != -1) {
                        sleep(500);
                        String strBlock = Integer.toHexString(blockNo).toUpperCase();
                        if (strBlock.trim().length() != 2) {
                            strBlock = "0" + strBlock;
                        }
                        strCommand=Utility.addCheckSum("#FF03" + strBlock + "00" + data);
                        byteCommand = strCommand.getBytes();
                        status = usbConn.bulkTransfer(usbOutput, byteCommand, byteCommand.length, 0);
                        status = usbConn.bulkTransfer(usbInput, readBytes, readBytes.length, 0100);
                        c = new String(readBytes).trim().toCharArray();
                        if (c != null && c.length > 0 && c.length == 5) {
                            status = -1;
                        }
                        if (status != -1) {
                            Log.d("TEST", "Sector " + sectorNo + " Key Updated Successfully");
                        } else {
                            Log.d("TEST", "Sector " + sectorNo + " Key Updation Failed");
                        }
                    } else {
                        Log.d("TEST", "Sector " + sectorNo + "Authentication Block Fail Second Time");
                    }
                } else {
                    Log.d("TEST", "Sector " + sectorNo + "Read Block Fail");
                }
            } else {
                Log.d("TEST", "Sector " + sectorNo + "Authentication Block Fail First Time");
            }
            return status;
        }
    }


    public int keyUpdate(int blockNo, String finalKeyA, String strAccessCode, String finalKeyB) {
        synchronized (this) {
            int status = -1;
            byte[] readBytes = new byte[Constants.BUFFER_SIZE];
            String strCommand = "";
            byte[] byteCommand = null;
            sleep(500);
            String strBlock = Integer.toHexString(blockNo).toUpperCase();
            if (strBlock.trim().length() != 2) {
                strBlock = "0" + strBlock;
            }
            strCommand = Utility.addCheckSum("#FF03" + strBlock + "00" + finalKeyA + strAccessCode + finalKeyB);
            byteCommand = strCommand.getBytes();
            status = usbConn.bulkTransfer(usbOutput, byteCommand, byteCommand.length, 0);
            status = usbConn.bulkTransfer(usbInput, readBytes, readBytes.length, 0100);
            char[] c = new String(readBytes).trim().toCharArray();
            if (c != null && c.length > 0 && c.length == 5) {
                status = -1;
            } else {
                status = 1;
            }
            if (status != -1) {
                Log.d("TEST", "Block " + blockNo + " Key Updated Successfully");
            } else {
                Log.d("TEST", "Block " + blockNo + " Key Updation Failed");
            }
            return status;
        }
    }

    //==================== Parse Sector Data Sector Wise and fill data to model SmartCardInfo  ==========================//

    public boolean parseSectorData(int sectorNo, byte[] sectorData, SmartCardInfo cardDetails) {

        boolean parseStatus = false;
        switch (sectorNo) {
            case 0:
                try {
                    byte[] fcsn = new byte[8];
                    int j = 0;
                    String strData = new String(sectorData);
                    String strCSN = strData.substring(1, 9).trim();
                    String strMADZero = strData.substring(33, 65).trim();
                    String strMADOne = strData.substring(65, 97).trim();
                    byte[] csn = strCSN.getBytes();
                    for (int i = 7; i > 0; i -= 2) {
                        fcsn[j] = csn[i - 1];
                        fcsn[j + 1] = csn[i];
                        j += 2;
                    }
                    strCSN = new String(fcsn);
                    cardDetails.setReadCSN(strCSN);
                    cardDetails.setMadZero(strMADZero);
                    cardDetails.setMadOne(strMADOne);
                    parseStatus = true;
                } catch (Exception e) {
                }
                break;
            case 1:
                break;
            case 2:
                try {
                    String str = new String(sectorData);
                    String strEmployeeId = str.substring(1, 17);//employee Id
                    String strName = str.substring(17, 33);     //name
                    String strValidUpto = str.substring(33, 39);   //validity date
                    String strBirthDay = str.substring(39, 45);    ////Date of Birth
                    String strSiteCode = str.substring(45, 47);  //Site code
                    String bloodgroup = str.substring(47, 48); //blood group
                    String strBloodGroup = "";
                    try {
                        strBloodGroup = Constants.BLOOD_GROUPS[Integer.parseInt(bloodgroup)];
                    } catch (NumberFormatException e) {

                    }
                    String strSmartCardVersion = str.substring(48, 49); //version information
                    cardDetails.setEmployeeId(strEmployeeId);
                    cardDetails.setEmpName(strName);
                    cardDetails.setValidUpto(strValidUpto);
                    cardDetails.setBirthDate(strBirthDay);
                    cardDetails.setSiteCode(strSiteCode);
                    cardDetails.setBloodGroup(strBloodGroup);
                    cardDetails.setSmartCardVer(strSmartCardVersion);
                    parseStatus = true;
                } catch (Exception e) {
                }
                break;
            case 3:
                break;
            case 4:
                try {
                    String template = "";
                    String str = new String(sectorData);
                    String temp1 = str.substring(1, 97);
                    template = template + temp1;
                    cardDetails.setFirstFingerTemplate(template);
                    parseStatus = true;
                } catch (Exception e) {
                }
                break;
            case 5:
                try {
                    String template = "";
                    String str = new String(sectorData);
                    String temp1 = str.substring(1, 97);
                    template = cardDetails.getFirstFingerTemplate();
                    template = template + temp1;
                    cardDetails.setFirstFingerTemplate(template);
                    parseStatus = true;
                } catch (Exception e) {
                }
                break;

            case 6:
                try {
                    String template = "";
                    String str = new String(sectorData);
                    String temp1 = str.substring(1, 97);
                    template = cardDetails.getFirstFingerTemplate();
                    template = template + temp1;
                    cardDetails.setFirstFingerTemplate(template);
                    parseStatus = true;
                } catch (Exception e) {
                }
                break;
            case 7:
                try {
                    String template = "";
                    String str = new String(sectorData);
                    String temp1 = str.substring(1, 97);
                    template = cardDetails.getFirstFingerTemplate();
                    template = template + temp1;
                    cardDetails.setFirstFingerTemplate(template);
                    parseStatus = true;
                } catch (Exception e) {
                }
                break;
            case 8:
                try {
                    String template = "";
                    String str = new String(sectorData);
                    String temp1 = str.substring(1, 97);
                    template = cardDetails.getFirstFingerTemplate();
                    template = template + temp1;
                    cardDetails.setFirstFingerTemplate(template);
                    parseStatus = true;
                } catch (Exception e) {
                }
                break;
            case 9:
                try {
                    String str = new String(sectorData);

                    String template = str.substring(1, 33); // 16 byte remaining template
                    template = cardDetails.getFirstFingerTemplate() + template;
                    cardDetails.setFirstFingerTemplate(template);

                    String firstFingerNo = "";
                    String firstFingerSecurityLevel = "";
                    String firstFingerIndex = "";
                    String firstFingerQuality = "";
                    String firstFingerVerificationMode = "";

                    firstFingerNo = str.substring(33, 35); // no of finger  -
                    firstFingerSecurityLevel = str.substring(35, 36); // security level F1_Slevel
                    firstFingerIndex = str.substring(36, 37); // finger index   F1_index
                    firstFingerQuality = str.substring(38, 39); //finger quality  F1_quality
                    firstFingerVerificationMode = str.substring(40, 41); // Verification mode Vmode

                    boolean isValid = false;
                    int index = -1;
                    try {
                        index = Integer.parseInt(firstFingerSecurityLevel);
                        isValid = true;
                    } catch (NumberFormatException e) {
                        isValid = false;
                    }

                    if (isValid && index < Constants.SECURITY_LEVELS.length) {
                        firstFingerSecurityLevel = Constants.SECURITY_LEVELS[index];
                    } else {
                        firstFingerSecurityLevel = "";
                    }

                    index = -1;
                    isValid = false;

                    if (!firstFingerIndex.equals("A")) {
                        try {
                            index = Integer.parseInt(firstFingerIndex);
                            isValid = true;
                        } catch (NumberFormatException e) {
                            isValid = false;
                        }
                    } else {
                        firstFingerIndex = Constants.FINGER_INDEXES[10];
                    }

                    if (isValid && index < Constants.FINGER_INDEXES.length) {
                        firstFingerIndex = Constants.FINGER_INDEXES[index];
                    } else {
                        firstFingerIndex = "";
                    }

                    index = -1;
                    isValid = false;

                    try {
                        index = Integer.parseInt(firstFingerQuality);
                        isValid = true;
                    } catch (NumberFormatException e) {
                        isValid = false;
                    }

                    if (isValid && index < Constants.QUALITY.length) {
                        firstFingerQuality = Constants.QUALITY[index];
                    } else {
                        firstFingerQuality = "";
                    }


                    index = -1;
                    isValid = false;

                    try {
                        index = Integer.parseInt(firstFingerVerificationMode);
                        isValid = true;
                    } catch (NumberFormatException e) {
                        isValid = false;
                    }

                    if (isValid && index < Constants.VERIFICATION_MODES.length) {
                        firstFingerVerificationMode = Constants.VERIFICATION_MODES[index];
                    } else {
                        firstFingerVerificationMode = "";
                    }

                    cardDetails.setFirstFingerNo(firstFingerNo);
                    cardDetails.setFirstFingerSecurityLevel(firstFingerSecurityLevel);
                    cardDetails.setFirstFingerIndex(firstFingerIndex);
                    cardDetails.setFirstFingerQuality(firstFingerQuality);
                    cardDetails.setFirstFingerVerificationMode(firstFingerVerificationMode);
                    parseStatus = true;
                } catch (Exception e) {
                }
                break;
            case 10:
                try {
                    String template = "";
                    String str = new String(sectorData);
                    String temp1 = str.substring(1, 97);
                    template = template + temp1;
                    cardDetails.setSecondFingerTemplate(template);
                    parseStatus = true;
                } catch (Exception e) {
                }
                break;
            case 11:
                try {
                    String template = "";
                    String str = new String(sectorData);
                    String temp1 = str.substring(1, 97);
                    template = cardDetails.getSecondFingerTemplate();
                    template = template + temp1;
                    cardDetails.setSecondFingerTemplate(template);
                    parseStatus = true;
                } catch (Exception e) {
                }
                break;
            case 12:
                try {
                    String template = "";
                    String str = new String(sectorData);
                    String temp1 = str.substring(1, 97);
                    template = cardDetails.getSecondFingerTemplate();
                    template = template + temp1;
                    cardDetails.setSecondFingerTemplate(template);
                    parseStatus = true;
                } catch (Exception e) {
                }
                break;
            case 13:
                try {
                    String template = "";
                    String str = new String(sectorData);
                    String temp1 = str.substring(1, 97);
                    template = cardDetails.getSecondFingerTemplate();
                    template = template + temp1;
                    cardDetails.setSecondFingerTemplate(template);
                    parseStatus = true;
                } catch (Exception e) {
                }
                break;
            case 14:
                try {
                    String template = "";
                    String str = new String(sectorData);
                    String temp1 = str.substring(1, 97);
                    template = cardDetails.getSecondFingerTemplate();
                    template = template + temp1;
                    cardDetails.setSecondFingerTemplate(template);
                    parseStatus = true;
                } catch (Exception e) {
                }
                break;
            case 15:
                try {
                    String template = "";
                    String str = new String(sectorData);
                    template = str.substring(1, 33); // 16 byte remaining template
                    template = cardDetails.getSecondFingerTemplate() + template;
                    cardDetails.setSecondFingerTemplate(template);

                    String secondFingerNo = "";
                    String secondFingerSecurityLevel = "";
                    String secondFingerIndex = "";
                    String secondFingerQuality = "";
                    String secondFingerVerificationMode = "";

                    secondFingerNo = str.substring(33, 35); // no of finger  -
                    secondFingerSecurityLevel = str.substring(35, 36); // security level F1_Slevel
                    secondFingerIndex = str.substring(36, 37); // finger index   F1_index
                    secondFingerQuality = str.substring(38, 39); //finger quality  F1_quality
                    secondFingerVerificationMode = str.substring(40, 41); // Verification mode Vmode

                    boolean isValid = false;
                    int index = -1;
                    try {
                        index = Integer.parseInt(secondFingerSecurityLevel);
                        isValid = true;
                    } catch (NumberFormatException e) {
                        isValid = false;
                    }

                    if (isValid && index < Constants.SECURITY_LEVELS.length) {
                        secondFingerSecurityLevel = Constants.SECURITY_LEVELS[index];
                    } else {
                        secondFingerSecurityLevel = "";
                    }

                    index = -1;
                    isValid = false;

                    if (!secondFingerIndex.equals("A")) {
                        try {
                            index = Integer.parseInt(secondFingerIndex);
                            isValid = true;
                        } catch (NumberFormatException e) {
                            isValid = false;
                        }
                    } else {
                        isValid = true;
                        index = 10;
                    }

                    if (isValid && index < Constants.FINGER_INDEXES.length) {
                        secondFingerIndex = Constants.FINGER_INDEXES[index];
                    } else {
                        secondFingerIndex = "";
                    }

                    index = -1;
                    isValid = false;

                    try {
                        index = Integer.parseInt(secondFingerQuality);
                        isValid = true;
                    } catch (NumberFormatException e) {
                        isValid = false;
                    }

                    if (isValid && index < Constants.QUALITY.length) {
                        secondFingerQuality = Constants.QUALITY[index];
                    } else {
                        secondFingerQuality = "";
                    }

                    index = -1;
                    isValid = false;

                    try {
                        index = Integer.parseInt(secondFingerVerificationMode);
                        isValid = true;
                    } catch (NumberFormatException e) {
                        isValid = false;
                    }

                    if (isValid && index < Constants.VERIFICATION_MODES.length) {
                        secondFingerVerificationMode = Constants.VERIFICATION_MODES[index];
                    } else {
                        secondFingerVerificationMode = "";
                    }

                    cardDetails.setSecondFingerNo(secondFingerNo);
                    cardDetails.setSecondFingerSecurityLevel(secondFingerSecurityLevel);
                    cardDetails.setSecondFingerIndex(secondFingerIndex);
                    cardDetails.setSecondFingerQuality(secondFingerQuality);
                    cardDetails.setSecondFingerVerificationMode(secondFingerVerificationMode);
                    parseStatus = true;
                } catch (Exception e) {
                }
                break;
            default:
                break;
        }
        return parseStatus;
    }

}
