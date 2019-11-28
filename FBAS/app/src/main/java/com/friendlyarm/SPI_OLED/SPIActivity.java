package com.friendlyarm.SPI_OLED;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.android.fortunaattendancesystem.R;
import com.friendlyarm.AndroidSDK.SPI;
import com.friendlyarm.AndroidSDK.ShellUtils;
import com.friendlyarm.SmartReader.RC632Api;
import com.friendlyarm.SmartReader.SmartCardApi;
import com.friendlyarm.SmartReader.SmartFinger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class SPIActivity extends Activity {

    final static int irqPinNo = 62;
    final static int resetPinNo = 63;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spi);


        //initPins();


        int spi_fd = -1;
        int status = -1;

        SPI spiApi = new SPI();
        spi_fd = spiApi.begin();
        Log.d("TEST", "Spi Begin Status:" + spi_fd);

        if (spi_fd > 0) {

//            status = spiApi.setBitOrder(0);
//            Log.d("TEST", "Spi Bit Order Status:" + status);

            // if (status == 0) {
            status = spiApi.setDataMode(0);
            Log.d("TEST", "Spi Data Mode Status:" + status);

            if (status == 0) {

                // status = spi.setClockDivider(SPIEnum.SPI_CLOCK_DIV64);

                // status = spiApi.setSpiSpeed(50000);

                //status=spiApi.setSpiSpeed(13560000);

                //status=spiApi.setSpiSpeed(4700000);

                status = spiApi.setSpiSpeed(7500000);

                Log.d("TEST", "Spi Clock Divider Status:" + status);

                if (status == 0) {

                    Log.d("TEST", "Spi initialized successfully");

                    RC632Api rc632Api = new RC632Api(spi_fd, spiApi);
                    rc632Api.rs632Init();

                    SmartCardApi smartCardApi = new SmartCardApi(rc632Api);

                    //======================================CSN Read Function Call=======================================//

                    byte[] charBuff = new byte[5];
                    //  while(true){
                    smartCardApi.smart_card_get_info(charBuff);
                    //   }

                    //===================================================================================================//

                    SmartFinger smartFinger = new SmartFinger(smartCardApi);
//                    //706572696E66
//                    int keyFlag = 0;
//                    byte[] readBuff = new byte[16];
//                    byte[] key_2 = new byte[]{0x70, 0x65, 0x72, 0x69, 0x6E, 0x66};
//
                    //   int error;

                    //=================================Block Read Function Call===================================//

//                        error=smartFinger.blockRead(keyFlag,key_2,(byte)0x08,readBuff);
//                        Log.d("TEST","ERROR 1:"+error);
//
//                        try {
//                            Log.d("TEST","Sector 2 Block 0:"+new String(readBuff, "UTF-8"));
//                        } catch (UnsupportedEncodingException e) {
//                            e.printStackTrace();
//                        }
//
//                        readBuff=new byte[16];
//                        error=smartFinger.blockRead(keyFlag,key_2,(byte)0x09,readBuff);
//                        Log.d("TEST","ERROR 2:"+error);
//
//                        try {
//                            Log.d("TEST","Sector 2 Block 1:"+new String(readBuff, "UTF-8"));
//                        } catch (UnsupportedEncodingException e) {
//                            e.printStackTrace();
//                        }
//
//                        readBuff=new byte[16];
//                        error=smartFinger.blockRead(keyFlag,key_2,(byte)0x0A,readBuff);
//                        Log.d("TEST","ERROR 3:"+error);
//
//                        try {
//                            Log.d("TEST","Sector 2 Block 2:"+new String(readBuff, "UTF-8"));
//                        } catch (UnsupportedEncodingException e) {
//                            e.printStackTrace();
//                        }

                    //==================================Block Write Function Call===================================//

//                    String strWrite="123456AnkitKumar";
//                    byte[] writeData=strWrite.getBytes();
//
//                    Log.d("TEST","Write Data Len:"+writeData.length);
//
//                    error=smartFinger.blockWrite(keyFlag,key_2,(byte)0x08,writeData);
//                    Log.d("TEST","Write Error No:"+error);


                    //======================================================================================//


                    //============================Sector Read Function Call================================================//

//                    byte[] readData = null;
//                    byte[] key_2 = new byte[]{0x31, 0x32, 0x33, 0x34, 0x35, 0x36};
//
//                    while (true) {
//
//                    readData=new byte[48];
//
//                        error = smartFinger.sectorRead(keyFlag, key_2, (byte) 0x02, readData);
//
//                        Log.d("TEST", "ERROR 1:" + error);
//
//                        try {
//                            Log.d("TEST", "Sector 2 Data:" + new String(readData, "UTF-8"));
//                        } catch (UnsupportedEncodingException e) {
//                            e.printStackTrace();
//                        }
//
//                    }


                    //==========================================================================================//


                    //==========================Sector Write Function Call======================================//

//                    String strWrite="5678934567876534AnkitKumarGuptas0106180106880123";
//                    byte[] writeData=strWrite.getBytes();
//                    Log.d("TEST","Write Data Length:"+writeData.length);
//
//                    error=smartFinger.sectorWrite(keyFlag,key_2,(byte)0x02,writeData);
//                    Log.d("TEST", "Sector Write Error No:" + error);

                    //==========================================================================================//


                    //==================================Key B Write Function Call================================================//

//                    String strNewKeyB="123456";
//                    byte[] keyb=strNewKeyB.getBytes();
//
//                    boolean stat=false;
//                    stat=smartFinger.changeKeyB((byte)0x02,key_2);
//                    Log.d("TEST","Key B Change Status:"+stat);

                    //=========================================================================================//


                    //===================================Sector Clean Function Call===========================================//

                    //   error=smartFinger.sectorClean(keyFlag,key_2,(byte)0x02);
                    //   Log.d("TEST","Sector Clean Error:"+error);

                    //==========================================================================================//


                    //=======================Finger Template Count Funstion Call===============================//

                    //RESSEC(524553534543)

//                    int keyFlag_0 = 0;
//                    byte[] readBuff_0 = new byte[48];
//                    byte[] key_0 = new byte[]{0x52, 0x45, 0x53, 0x53, 0x45, 0x43};
//
//                    error = smartFinger.sectorRead(keyFlag_0, key_0, (byte) 0x00, readBuff_0);
//
//                    Log.d("TEST", "ERROR 1:" + error);
//
//                    Log.d("TEST","Sector 0 Data:"+bytesToHex(readBuff_0));
//
//                    //0217ED35CD8804008500B42EF0BB6AA8(Sector 0 Block 0)
//                    // 55010005000000004802480248024802(Sector 0 Block 1)
//                    // 48024802480248024802480248024802(Sector 0 Block 3)
//
//                    int fingerNo;
//                    fingerNo=smartFinger.fingerTemplateCount(keyFlag_0,key_0,readBuff_0);
//
//                    if (fingerNo == 0) {
//                        //No finger Found In Card
//                        Log.d("TEST","No Finger Found In Card");
//                    } else if (fingerNo == 16) {
//                        //First Finger Found in Card
//                        Log.d("TEST","First Finger Found In Card");
//                    } else if (fingerNo == 17) {
//                        //Two Fingers Found In Card
//                        Log.d("TEST","Two Fingers Found In Card");
//                    }else if(fingerNo==1){
//                        //Second Finger Found In Card
//                        Log.d("TEST","Second Finger Found In Card");
//                    }
//                    Log.d("TEST","Finger Number Count:"+fingerNo);

                    //=========================================================================================//


                    //===============================Sector Key Get Function Call===================================//

//                    int keyFlagx=0;
//                    byte sectorVal=0x04;
//                    byte[] temp=new byte[6];
//
//                    smartFinger.sectorKey(keyFlag,sectorVal,temp);
//                    Log.d("TEST","Key Value:"+bytesToHex(temp));

                    //==========================================================================================//


                    //==========================Read Finger Template Function Call=====================================//

                    //  while(true){

//                        byte[] template=new byte[580];
//                        long startTime=System.currentTimeMillis();
//                        smartFinger.readFingerTemplate(keyFlag, template);
//                        long endTime=System.currentTimeMillis();
//                        long timeTaken=endTime-startTime;
//
//                        Log.d("TEST","Time Taken:"+ TimeUnit.MILLISECONDS.toSeconds(timeTaken));
//                        Log.d("TEST", "Hex Template:" + bytesToHex(template));

                    //   }

                    //==================================================================================================//

                    //==================================Write Finger Template Function Call=====================================//

                    //==============================Write First Template====================================================//

//                    Total Finger Data Write=48*6=288 bytes
//                    Finger Data=256 bytes//
//                    Finger Details(Finger Number+Security Level+Finger Index+Finger Quality+Verification Mode)=4 bytes//
//                    byte fingerNumber=0x01;
//                    byte securityLevel=0x32;
//                    byte fingerQuality=0x41;
//                    byte verificationMode=0x02;
//                    Dummy Data=28 bytes
//                    String strDummy="00000000000000000000000000000000000000000000000000000000";

//                    int error = -1;
//
//                    String strRWKey = "012345";
//
//                    int keyType = 1;
//                    byte[] rwKey = new byte[6];
//                    rwKey = strRWKey.getBytes();
//
//                    byte[] firstFingerData = null;
//                    byte[] secondFingerData = null;
//                    String strFirstFinger = "464D520020323000000000FC00000258025800C500C5010000001525812600F7D03C814500FE5843410800F3BC43811F00CF9C50415F0103403C40F500F0B44A8145012D084341390140F83C40E200F3B043816800F5B84A810800BF9C50417D0101C45080DB0126B443416D0124A4508155013B8443413B00B8805040E70137BC4340EE00CDA45041420161044380B600FA384380BA011B344341740130A44A410801686043419C010FC84341630147244340EE0155D83C416D00CDD0508187012BB44A813401827C3C415F015F884380F50171603C4101017D743C80DB015AC83C419C00E7D04A81140197803C411D006F883C80BD01453C430000000000000132410200000000000000000000000000000000000000000000000000000000";
//
//                    String strSecondFinger = "464D520020323000000000FC00000258025800C500C5010000002A254118011D8043810F01290057411B0137745D411600FE284A80FA0118D85741300103A450414A011BB45D81080101345040EC0111C85781580124B85D814201429C5D8111015F7450413700E5D45D40E20108BC5D413000D2F05D40FC015AEC4A412B016F145D412900C8F85D417D011FC85D411B017B785D81580145B05D80FE00D6B05D414E00E5D05D416300F5CC5D812200B3005D80AF011B3C438187012BCC5D40C400FEB45040BD0137BC5D815800D4E45040E900CDAC5D416600DB60504137018E0C5D817B00E9D45D80FA018EF457411D01B17C4A8184014EC45D0000000000000237420200000000000000000000000000000000000000000000000000000000";
//
//                    firstFingerData = hexStringToByteArray(strFirstFinger);
//                    secondFingerData = hexStringToByteArray(strSecondFinger);
//
//                    long startTime1 = System.currentTimeMillis();
//                    error = smartFinger.writeFingerTemplate(keyType, rwKey, firstFingerData, 1);
//                    long endTime1 = System.currentTimeMillis();
//                    long timeTaken1 = endTime1 - startTime1;
//
//                    Log.d("TEST", "First Finger Time Taken In Milliseconds:" + timeTaken1);
//                    Log.d("TEST", "First Finger Time Taken In Seconds:" + TimeUnit.MILLISECONDS.toSeconds(timeTaken1));
//                    Log.d("TEST", "First Finger Error No:" + error);
//
//                    long startTime2 = System.currentTimeMillis();
//                    error = smartFinger.writeFingerTemplate(keyType, rwKey, secondFingerData, 2);
//                    long endTime2 = System.currentTimeMillis();
//                    long timeTaken2 = endTime2 - startTime2;
//
//                    Log.d("TEST", "Second Finger Time Taken In Milliseconds:" + timeTaken2);
//                    Log.d("TEST", "Second Finger Time Taken In Seconds:" + TimeUnit.MILLISECONDS.toSeconds(timeTaken2));
//                    Log.d("TEST", "Second Finger Error No:" + error);

                    //======================================================================================================//

                    //===========================================Erase Finger Template==============================================//

//
//                    int error=-1;
//
//                    long startTime1=System.currentTimeMillis();
//                    error=smartFinger.eraseTemplate(1);
//                    long endTime1=System.currentTimeMillis();
//                    long timeTaken1=endTime1-startTime1;
//
//                    Log.d("TEST","First Finger Time Taken In Milliseconds:"+ timeTaken1);
//                    Log.d("TEST","First Finger Time Taken In Seconds:"+ TimeUnit.MILLISECONDS.toSeconds(timeTaken1));
//                    Log.d("TEST","First Finger Error No:"+error);
//
//                    long startTime2=System.currentTimeMillis();
//                    error=smartFinger.eraseTemplate(2);
//                    long endTime2=System.currentTimeMillis();
//                    long timeTaken2=endTime2-startTime2;
//
//                    Log.d("TEST","First Finger Time Taken In Milliseconds:"+ timeTaken2);
//                    Log.d("TEST","First Finger Time Taken In Seconds:"+ TimeUnit.MILLISECONDS.toSeconds(timeTaken2));
//                    Log.d("TEST","First Finger Error No:"+error);


                    //==============================================================================================================//


                    //===============================Card Initialization Process========================================//

                    //Step 1:Ask UserCreationActivity For 8 bytes Card Id
                    //Step 2:Check whether Card Is already initialized,Read Sector 1 Block 0 using Key-A(Fortuna Key-A[012345]),if success card is already initialized,else start card init process follow Step 3
                    //Step 3:Read Block 0 of Sector 0 with Key-A(FFFFFFFFFFFF),if success follow Step 4
                    //Step 4:Start initializing all sectors trailing block from 0 to 15 one by one,change Key-A,Access Code,Key-B
                    //Step 5:Read trailing block of sector n where n=0 to 15 with Key-A(Factory Key A[FFFFFFFFFFFF])
                    //Step 6:Write Fortuna Key A ,Access Code,Key-B in trailing block with Key-A(Factory Key A[FFFFFFFFFFFF])
                    //Access code for all sectors except 1 is 7F0788C1 and  for Sector 1 is 787788C1
                    //Step 7:Sector 0 Write/MAD Write.Write Block 1 and Block 2 of Sector 0 with 5501000500000000000000000000000000000000000000000000000000000000 with Key-B(Fortuna Key-B[RESSEC])
                    //Step 8:Sector 1 Write/Card Id Write.Write 8 bytes Card Id in Sector 0 Block 0 and rest 8 bytes with default value ie 464F5254554E4120 with Key-B(Fortuna Key-B[CPS ID])
                    //Step 9:Sector 2 to 15 Write.Write 48 bytes with zeroes to each sector with Fortuna Key-B of respective sectors.

                    int error;
//                    int keyFlag = 1;
//                    String strFortunaKeyA = "EDCBA@";
//                    byte[] keyA = new byte[6];
//                    keyA = strFortunaKeyA.getBytes();
//
//                    byte[] readBuff = new byte[16];
//
//                    String strCardId = "12345678";
//                    byte[] cardId = new byte[8];
//                    cardId = strCardId.getBytes();
//
//                    error = smartFinger.blockRead(keyFlag, keyA, (byte) 0x04, readBuff);
//
//                    if (error == 0) {
//                        Log.d("TEST", "Card is already initialized");
//                    } else {
//                        Log.d("TEST", "Card is to be initialized");
//                        byte[] factKeyA = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
//                        error = smartFinger.blockRead(keyFlag, factKeyA, (byte) 0x00, readBuff);
//                        if (error == 0) {
//                            Log.d("TEST", "Factory Card Found");
//                            for(int i=0;i<16;i++){
//                                error=smartFinger.cardInit(true,(byte)i,cardId);
//                                Log.d("TEST","Init Sector No:"+(byte)i+"Error No:"+error);
//                            }
//                        } else {
//                            Log.d("TEST", "Fortuna Card Found");
//                        }
//                    }


                    //==================================================================================================//


                    //======================================Init Card To Factory Card Process========================================//


//                    for (int i = 0; i < 16; i++) {
//                        error = smartFinger.initCardToFactoryCard((byte) i);
//                        Log.d("TEST", "Init Sector No:" + (byte) i + "Error No:" + error);
//                    }

                    //=================================================================================================//


                    //======================================Fortuna Card To Factory Card Process========================================//


//                    for (int i = 0; i < 16; i++) {
//                        error = smartFinger.fortunaCardToFactoryCard((byte) i);
//                        Log.d("TEST", "Init Sector No:" + (byte) i + "Error No:" + error);
//                    }

                    //=================================================================================================//


                    //=====================================Card Refresh Process===============================================//
//
//                    String strCardId = "87654321";
//                    byte[] cardId = new byte[8];
//                    cardId = strCardId.getBytes();
//
//                    for (int i = 0; i < 16; i++) {
//                        error = smartFinger.cardRefresh((byte) i, cardId);
//                        Log.d("TEST", "Refresh Sector No:" + (byte) i + "Error No:" + error);
//                    }

                    //=======================================================================================================//


                    //=====================================Full Card Write Process===========================================//

                    //Step 1: Sector 2 Write Basic info(Employee Id,Employee Name,DOV,DOB,Site Code,Blood Group,Smart Card Version)
                    //Step 2:Sector 4 to 9 Write First Template
                    //Step 3:Sector 10 to 15 Write Second Template


                    //=======================Sector 2 Write using Default Key B=====================//
//
//                    int keyFlag=0;
//                    String strDfltKey="543210";
//                    String strFortunaKeyB="perinf";
//                    byte[] keyBDflt=new byte[6];
//                    byte[] fortunaKeyB=new byte[6];
//                    keyBDflt=strDfltKey.getBytes();
//                    fortunaKeyB=strFortunaKeyB.getBytes();
//
//                    byte sectorVal=0x02;
//
//                    String strBasicInfo="         FIPL355AnkitKumarSaha  0106180106880121";
//                    byte[] info=new byte[48];
//
//                    info=strBasicInfo.getBytes();
//
//                    error=smartFinger.sectorWrite(keyFlag, keyBDflt, sectorVal, info);
//                    Log.d("TEST","Sector 2 Write Error No:"+error);


                    //======Sector 2 Change Default Key B to Fortuna Key B=====//
//
//                    boolean keyBChange=false;
//                    keyBChange=smartFinger.changeKeyB(sectorVal,fortunaKeyB);
//                    Log.d("TEST","Sector 2 Key B Change Status:"+keyBChange);

                    //============================Finger Template Write======================//

//
//                    String strRWKey = "012345";
//
//                    int keyType = 1;
//                    byte[] rwKey = new byte[6];
//                    rwKey = strRWKey.getBytes();
//
//                    byte[] firstFingerData = null;
//                    byte[] secondFingerData = null;
//                    String strFirstFinger = "464D520020323000000000FC00000258025800C500C5010000001525812600F7D03C814500FE5843410800F3BC43811F00CF9C50415F0103403C40F500F0B44A8145012D084341390140F83C40E200F3B043816800F5B84A810800BF9C50417D0101C45080DB0126B443416D0124A4508155013B8443413B00B8805040E70137BC4340EE00CDA45041420161044380B600FA384380BA011B344341740130A44A410801686043419C010FC84341630147244340EE0155D83C416D00CDD0508187012BB44A813401827C3C415F015F884380F50171603C4101017D743C80DB015AC83C419C00E7D04A81140197803C411D006F883C80BD01453C430000000000000132410200000000000000000000000000000000000000000000000000000000";
//
//                    String strSecondFinger = "464D520020323000000000FC00000258025800C500C5010000002A254118011D8043810F01290057411B0137745D411600FE284A80FA0118D85741300103A450414A011BB45D81080101345040EC0111C85781580124B85D814201429C5D8111015F7450413700E5D45D40E20108BC5D413000D2F05D40FC015AEC4A412B016F145D412900C8F85D417D011FC85D411B017B785D81580145B05D80FE00D6B05D414E00E5D05D416300F5CC5D812200B3005D80AF011B3C438187012BCC5D40C400FEB45040BD0137BC5D815800D4E45040E900CDAC5D416600DB60504137018E0C5D817B00E9D45D80FA018EF457411D01B17C4A8184014EC45D0000000000000237420200000000000000000000000000000000000000000000000000000000";
//
//                    firstFingerData = hexStringToByteArray(strFirstFinger);
//                    secondFingerData = hexStringToByteArray(strSecondFinger);
//
//                    error = smartFinger.writeFingerTemplate(keyType, rwKey, firstFingerData, 1);
//                    Log.d("TEST","First Finger Template Write Error No:"+error);
//
//                    error = smartFinger.writeFingerTemplate(keyType, rwKey, secondFingerData, 2);
//                    Log.d("TEST","Second Finger Template Write Error No:"+error);

                    //=======================================================================================================//

                }
            }
        }

    }

    private void initPins() {

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

    public String bytesToHex(byte[] data) {

        String strHexData = "";
        StringBuilder builder1 = new StringBuilder();
        for (byte b : data) {
            builder1.append(String.format("%02x", b));
        }
        strHexData = builder1.toString().toUpperCase();

        return strHexData;
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    private String asciiToHex(String asciiValue) {
        char[] chars = asciiValue.toCharArray();
        StringBuffer hex = new StringBuffer();
        for (int i = 0; i < chars.length; i++) {
            hex.append(Integer.toHexString((int) chars[i]));
        }
        return hex.toString();
    }


    public boolean writeSysfs(String path, String value) { //对文件进行写操作

        Log.i("TEST", "writeSysfs path:" + path + " value:" + value);

        BufferedWriter writer = null;

        if (!new File(path).exists()) {
            Log.e("TEST", "File not found: " + path);
            return false;
        }

        try {
            writer = new BufferedWriter(new FileWriter(path), 64);
            writer.write(value);
//            writer.flush();
            return true;
        } catch (IOException e) {
            Log.e("TEST", "IO Exception when write: " + path, e);
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_spi, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
