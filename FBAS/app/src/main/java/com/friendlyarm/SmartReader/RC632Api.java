package com.friendlyarm.SmartReader;

import android.util.Log;

import com.friendlyarm.AndroidSDK.SPI;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by fortuna on 22/5/17.
 */
public class RC632Api {


    final static int resetPinNo = 63;

    byte cmnd = RC632Headers.cmnd;
    byte page0 = RC632Headers.page0;
    byte irq_config = RC632Headers.irq_config;
    byte cntrl = RC632Headers.cntrl;
    byte tmr_clk = RC632Headers.tmr_clk;
    byte tmr_reld = RC632Headers.tmr_reld;
    byte tmr_cntrl = RC632Headers.tmr_cntrl;


    boolean status=false;

    public int spi_fd = -1;
    SPI spiApi = new SPI();

    public RC632Api() {

    }

    public RC632Api(int spi_fd, SPI spiApi) {
        this.spi_fd = spi_fd;
        this.spiApi = spiApi;
    }

    public boolean rs632Init() {

        long j = 0;
        long k, l = 0;        //+++++for testing purpose+++++


        //--------------------------------------------------------------//
        for (j = 0; j < 10000; j++) {
            ;
        }//unsigned long  j = 0;
         rc632Reset(1);
        //rc632Reset("/sys/class/gpio/gpio" + Integer.toString(resetPinNo) + "/value", "1");

        //rc632Reset("/sys/class/gpio/gpio63/value","1");


        for (j = 0; j < 10000; j++) {
            ;
        }//unsigned long  j = 0;

        rc632Reset(0);



       // rc632Reset("/sys/class/gpio/gpio" + Integer.toString(resetPinNo) + "/value", "0");

       // rc632Reset("/sys/class/gpio/gpio63/value","0");



        j = 0xffff;

        while ((rc632ByteIn(cmnd) & 0x3f) != 0x00) {
            if ((--j) == 0) {
                break;
            }
        }

        k = j;        //+++++for testing purpose+++++
        if (j != 0) {
            rc632ByteOut(page0, (byte) 0x80);
            j = 0xffff;

            while (rc632ByteIn(cmnd) != 0x00) {
                if ((--j) == 0) {
                    break;
                }
            }

            l = j;//+++++for testing purpose+++++
            if (j != 0) {

                rc632ByteOut(page0, (byte) 0x00);

                byte value = (byte) ((rc632ByteIn(irq_config) | 0x01) & 0xfd);

                //  value=0x02;

                rc632ByteOut(irq_config, value);

                // value = rc632ByteIn(irq_config);

                //Log.d("TEST", "IRQ Config Value:" + value);


                // rc632ByteOut(irq_config, (byte) ((rc632ByteIn(irq_config) |  0x01) &  0xfd));
            }
        }
        //--------------------------------------------------------------//
        // lfcr();

        if (j != 0) {
            //display("RC632 Initialised Successfully");
           // Log.d("TEST", "RC632 Initialised Successfully");
            // lfcr();
            status = true;


        }

        return status;

        //---------FOLLOWING BLOCK IS FOR TESTING PURPOSE-----------
        //lfcr();
        // srl0_put_word(k);
        // srl0_put_char('.');
        // srl0_put_word(l);
        // lfcr();
        //---------------------------------------------------------
    }

    public static int hex2decimal(String s) {
        String digits = "0123456789ABCDEF";
        s = s.toUpperCase();
        int val = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            int d = digits.indexOf(c);
            val = 16 * val + d;
        }
        return val;
    }

//    unsigned char irq_632(void)
//    {
//        #if ((MY_DEVICE == CPU14D) || (MY_DEVICE == CPU14F) || (MY_DEVICE == CPU29A))
//        if(!(LPC_GPIO1->FIOPIN & ( 1 << 25))) return 0;
//        else  return 1;
//        #elif MY_DEVICE == CPU25B
//        if(!(LPC_GPIO0->FIOPIN & ( 1 << 27))) return 0;
//        else  return 1;
//        #elif MY_DEVICE == CPU21B
//        if(!(LPC_GPIO1->FIOPIN & ( 1 << 24))) return 0;
//        else  return 1;
//        #else
//        return 1;		//Do Nothing
//        #endif
//    }

    private static String readCommand = String.format("cat /sys/class/gpio/%s/value", "gpio62");
    private static String[] test = new String[]{"su", "-c", readCommand};

//    public int rc632IRQ() {
//
//        Process p = null;
//        BufferedReader reader = null;
//        int pinValue = -1;
//        try {
//            p = Runtime.getRuntime().exec(test);
//
//            if (p != null) {
//
//                reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
//                String strGpioVal = reader.readLine();
//                pinValue = Integer.parseInt(strGpioVal);
//
//            }
//        } catch (IOException e) {
//            Log.d("TEST","***************IRQ Read Exception***************:"+e.getMessage());
//            e.printStackTrace();
//        }
//
//        //Log.d("TEST", "RC632 IRQ Value:" + pinValue);
//
//        return pinValue;
//    }

//    public int rc632IRQ() {
//
//        Process p = null;
//        BufferedReader reader = null;
//        int pinValue = -1;
//        InputStream is = null;
//        BufferedReader br = null;
//        InputStreamReader ir = null;
//        try {
//            p = Runtime.getRuntime().exec(test);
//
//            if (p != null) {
//
//                is = p.getInputStream();
//                ir = new InputStreamReader(is);
//                br = new BufferedReader(ir);
//                // reader = new BufferedReader(new InputStreamReader(is));
//                String strGpioVal = br.readLine();
//                pinValue = Integer.parseInt(strGpioVal);
//
//            }
//        } catch (IOException e) {
//            Log.d("TEST", "***************IRQ Read Exception***************:" + e.getMessage());
//            e.printStackTrace();
//        } finally {
//            if (is != null) {
//                try {
//                    is.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            if (ir != null) {
//                try {
//                    ir.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            if (br != null) {
//                try {
//                    br.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            if (p != null) {
//                p.destroy();
//            }
//        }


    final static String irqPath = "/sys/class/gpio/gpio62/value";

    public int rc632IRQ() { //读取数据，并转化为String

        int pinValue = -1;
        FileReader fr = null;
        BufferedReader br = null;

        if (!new File(irqPath).exists()) {
            Log.e("TEST", "File not found: " + irqPath);
            return pinValue;
        }

       // Log.i("TEST", "readSysfs path:" + irqPath);

        try {
            fr = new FileReader(irqPath);
            br = new BufferedReader(fr);
            String strGpioVal = br.readLine();
            pinValue = Integer.parseInt(strGpioVal);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally{

            if(fr!=null){
                try {
                    fr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if(br!=null){
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

      //  Log.d("TEST", "RC632 IRQ Value:" + pinValue);

        return pinValue;
    }


    public boolean rc632Reset(String path, String value) { //对文件进行写操作

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



    public void rc632Reset(int value) {

        int status = -1;
        String commandlow = String.format("echo %d > /sys/class/gpio/%s/value", value, "gpio63");
        try {
            String[] test = new String[]{"su", "-c", commandlow};
            Process proc = Runtime.getRuntime().exec(test);
            status = proc.waitFor();
            Log.d("TEST", "Wait For Status:" + status);
            status = proc.exitValue();
            Log.d("TEST", "Exit Value Status:" + status);
        } catch (IOException e) {
            Log.d("TEST", "Exception During Chip Select Low--->" + e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void rc632ChipSelect(int value) {


    }

    public void rc632ByteOut(byte addr, byte value) {

        //spiApi.spiWriteByte((byte) ((addr << 1) & 0x7E));
        //spiApi.spiWriteByte(value);

//        Log.d("TEST","-----------------RC632 Byte Out Start------------------");
//
//        spiApi.spiReadWrite((byte) ((addr << 1) & 0x7E));
//        spiApi.spiReadWrite(value);
//
//        Log.d("TEST", "-----------------RC632 Byte Out End------------------");

        //==================================Latest RC632 Byte Out===========================================================//

//        int status = -1;
//
//        addr = (byte) ((addr << 1) & 0x7E);
//
//        byte[] data = new byte[3];
//        data[0] = addr;
//        data[1] = value;
//
//        Log.d("TEST","-----------------RC632 Byte Out Start------------------");
//
//      //  status = spiApi.spiReadWriteTest(data);
//
//        status=spiApi.spiWrite(data);
//
//        Log.d("TEST", "-----------------RC632 Byte Out End------------------");
//

        //===================================================================================================================//

        // Log.d("TEST", "-----------------RC632 Byte Out Start------------------");

        addr = (byte) ((addr << 1) & 0x7E);

//        String s1 = String.format("%8s", Integer.toBinaryString(addr & 0xFF)).replace(' ', '0');
//        Log.d("TEST", "Write Byte Binary Format:" + s1); // 10000001


        byte[] dataWrite = {addr, value};
        spiApi.spiWrite(dataWrite);

        // Log.d("TEST", "-----------------RC632 Byte Out Start------------------");


//        Log.d("TEST","-----------------RC632 Byte Out Start------------------");
//
//        addr = (byte) ((addr << 1) & 0x7E);
//
//        spiApi.spiByteWrite(addr);
//        spiApi.spiByteWrite(value);
//
//        Log.d("TEST", "-----------------RC632 Byte Out Start------------------");


    }


    public byte rc632ByteIn(byte addr) {


//        byte val = -1;
//        addr = (byte) (((addr << 1) & 0x7E) |  0x80);
//
//        Log.d("TEST","-----------------RC632 Byte In Start------------------");
//
//        spiApi.spiReadWrite(addr); //add
//        val = spiApi.spiReadWrite((byte) 0x00); //signal last byte and read it and then return it
//
//        Log.d("TEST","-----------------RC632 Byte In End------------------");

        //==================================Latest RC632 Byte In===========================================================//

//        byte val = -1;
//
//        addr = (byte) (((addr << 1) & 0x7E) |  0x80);
//        byte[] data = new byte[2];
//        data[0] = addr;
//        data[1] = 0x00;
//
//        Log.d("TEST","-----------------RC632 Byte In Start------------------");
//
//       // val=spiApi.spiReadWriteTest(data);
//
//        val=spiApi.spiRead(data);
//
//        Log.d("TEST","Return Byte Value:"+val);
//
//        Log.d("TEST","-----------------RC632 Byte In End------------------");


//        byte val = -1;
//        addr = (byte) (((addr << 1) & (byte) 0x7E) | (byte) 0x80);
//
//        spiApi.spiReadWrite(addr);
//        spiApi.spiReadWrite((byte) 0x00);
//
//        val = spiApi.spiReadWrite((byte) 0x00);
        // val=spiApi.spiWriteTest(addr);


        //  spiApi.spiWriteByte(addr);
        //spiApi.spiReadWrite((byte) 0x00);


        //  spiApi.spiWriteByte((byte) 0x00);

        //  spiApi.spiReadByte();


        //   spiApi.spiReadWrite(addr); //add
        //  val = spiApi.spiReadWrite((byte) 0x00); //signal last byte and read it and then return it

        // Log.d("TEST", "-----------------RC632 Byte In Start------------------");

//        int binnum[] = new int[100];
//        int decnum, i = 1, j;

        addr = (byte) (((addr << 1) & 0x7E) | 0x80);
//        String s1 = String.format("%8s", Integer.toBinaryString(addr & 0xFF)).replace(' ', '0');
//        Log.d("TEST", "Read Byte Binary Format:" + s1); // 10000001


//        decnum=hex2decimal(Byte.toString(addr));
//
//        Log.d("TEST","Decimal Number:"+decnum);
//
//        while(decnum != 0)
//        {
//            binnum[i++] = decnum%2;
//            decnum = decnum/2;
//        }
//
//        Log.d("TEST", "Equivalent Binary Number is:");
//        for(j=i-1; j>0; j--)
//        {
//            System.out.print(binnum[j]);
//        }
//
//        Log.d("TEST","Binary Value:"+Arrays.toString(binnum));


        //  byte[] dataRead={addr,0x7E,0x00};


        byte[] dataRead = {addr, 0x00};

        byte ret = spiApi.spiWrite(dataRead);


        //   Log.d("TEST", "-----------------RC632 Byte In End------------------");
//


//        Log.d("TEST","-----------------RC632 Byte In Start------------------");
//
//        addr = (byte) (((addr << 1) & 0x7E) |  0x80);
//
//        //byte[] dataRead={addr,0x7E,0x00};
//
//        spiApi.spiByteWrite(addr);
//        spiApi.spiByteWrite((byte)0x00);
//
//        byte ret=spiApi.spiByteRead();
//
//
//        Log.d("TEST","-----------------RC632 Byte In End------------------");


        return ret;


    }

    public void rc632ByteInTest(byte addr) {


        byte val = -1;

        addr = (byte) (((addr << 1) & (byte) 0x7E) | (byte) 0x80);
        byte[] data = new byte[2];
        data[0] = addr;
        data[1] = 0x00;

        Log.d("TEST", "##############Before Byte Read##################");

        val = spiApi.spiReadWriteTest(data);

        Log.d("TEST", "##############After Byte Write##################");


        // Log.d("TEST", "Spi Write Byte Out Return Value 2 :" + status);


        // status = spiApi.spiWriteTest(data[0]);
        // Log.d("TEST", "Spi Write Byte In Return Value 1 : " + status);

        //  status = spiApi.spiWriteTest(data[1]);
        //  Log.d("TEST", "Spi Write Byte In Return Value 2 :" + status);


        //status=spiApi.spiReadWriteTest(data);
        //Log.d("TEST", "Spi Write Byte In Return Value:" + status);


//        status=spiApi.spiWriteTest(data);
//        Log.d("TEST", "Spi Write Byte In Return Value:" + status);
//
//        status=spiApi.spiRead();
//        Log.d("TEST", "Spi Read Byte In Return Value:" + status);

    }

    public void rc632ByteOutTest(byte addr, byte value) {

        int status = -1;

        addr = (byte) ((addr << 1) & 0x7E);

        byte[] data = new byte[2];
        data[0] = addr;
        data[1] = value;

        Log.d("TEST", "##############Before Byte Write##################");

        status = spiApi.spiReadWriteTest(data);

        Log.d("TEST", "##############After Byte Write##################");


//        status = spiApi.spiWriteTest(data[0]);
//        Log.d("TEST", "Spi Write Byte Out Return Value 1 : " + status);
//
//        status = spiApi.spiWriteTest(data[1]);
//        Log.d("TEST", "Spi Write Byte Out Return Value 2 :" + status);

    }


    public void rc632FIFORead(int length, byte[] buffer) {

//       cs_632(0);
//
//        x = 0x02;	//address of the fifo
//        x = x<<1;
//        x &=0x7e; //format add
//        x |=0x80;
//        z = x;
//
//        spi_read_write(z); //add
//
//        for(x=0;x<length-1;x++)
//        {
//            (*buff++)=spi_read_write(z);
//        }
//        *buff=spi_read_write(0x00);
//
//        cs_632(1);


        byte[] data = new byte[6];
        byte x = 0x00;
        byte z = 0x00;

        x = 0x02;    //address of the fifo
        x = (byte) (x << 1);
        x &= 0x7e; //format add
        x |= 0x80;
        z = x;

        //   spiApi.spiReadWrite(z);


        int counter = 0;

        for (counter = 0; counter < length - 1; counter++) {
            buffer[counter] = rc632ByteIn((byte) 0x02);
        }

        buffer[counter] = rc632ByteIn((byte) 0x02);


//        for (counter = 0; counter < length; counter++) {
//            data[counter] = z;
//        }
//        data[counter] = 0;
//
//        spiApi.spiRead(data);
//
//        int len = data.length;
//
//        for (int i = 0; i < len; i++) {
//            Log.d("TEST", "Fifo Read Buffer[" + i + "]:" + (data[i] & 0xFF));
//            buffer[i]=data[i];
//        }

    }


//    void rc632_fifo_write(unsigned char length, char *buff)
//    {
//
//        unsigned char x;
//
//        cs_632(0);
//
//        spi_read_write((unsigned char)((0x02<<1) & 0x7e)); //address of fifo is 0x02 and is formatted in write mode
//
//        for(x=0;x<length;x++)
//        {
//            spi_read_write(*buff++); //value
//        }
//
//        cs_632(1);
//    }


    public void rc632FIFOWrite(int len, byte[] buff) {

//        unsigned char x;
//
//        cs_632(0);
//
//        spi_read_write((unsigned char)((0x02<<1) & 0x7e)); //address of fifo is 0x02 and is formatted in write mode
//
//        for(x=0;x<length;x++)
//        {
//            spi_read_write(*buff++); //value
//        }
//
//        cs_632(1);

        // spiApi.spiReadWrite((byte) ((0x02 << 1) & 0x7e));


        byte addr = (byte) ((0x02 << 1) & 0x7E);

        byte[] data = new byte[len + 1];
        data[0] = addr;

        for (int x = 0; x < len; x++) {
            //spiApi.spiReadWrite(buff[x]);

            data[x + 1] = buff[x];
            // rc632ByteOut(addr,buff[x]);
        }
        spiApi.spiWrite(data);

    }

    //----------------------------------------------------------//
    public void rc632FlashFIFO() {
        rc632SetBitMask(cntrl, (byte) 0x01);
    }

    //----------------------------------------------------------//
    void rc632SetBitMask(byte adr, byte val) {

        // Log.d("TEST", "S-B-M-A:" + adr);
        byte value = rc632ByteIn(adr);
        // Log.d("TEST", "S-B-M-1:" + value);

        rc632ByteOut(adr, (byte) (val | value));

        value = rc632ByteIn(adr);
        //  Log.d("TEST", "SET BIT MASK 2:" + value);


        //rc632ByteOut(adr, (byte) (val | rc632ByteIn(adr)));
    }

    //----------------------------------------------------------//
    void rs632ClearBitMask(byte adr, byte val) {

        //   Log.d("TEST", "C-B-M-A:" + adr);
        byte value = rc632ByteIn(adr);
        // Log.d("TEST", "CLEAR BIT MASK 1:" + value);

        rc632ByteOut(adr, (byte) (~val & value));

        value = rc632ByteIn(adr);
        //  Log.d("TEST", "CLEAR BIT MASK 2:" + value);


        //rc632ByteOut(adr, (byte) (~val & rc632ByteIn(adr)));


    }

//----------------------------------------------------------//

    public void test() {

        byte x = 0x04;
        byte y = (byte) (0x02);

        byte[] dataWrite = {x, y};
        byte[] dataRead = {x, 0x00};

        spiApi.spiWrite(dataWrite);
        spiApi.spiWrite(dataRead);

    }

//    void rc632_timer_start(int x)
//    {
//        rc632_byte_out(tmr_cntrl, 0x02);
//
//        switch(x)
//        {
//            case 0x01:
//            {
//                rc632_byte_out(tmr_clk, 0x07);  //1 ms
//                rc632_byte_out(tmr_reld, 0x6a);
//                break;
//            }
//            case 0x02:
//            {
//                rc632_byte_out(tmr_clk, 0x07);  //1.5 ms
//                rc632_byte_out(tmr_reld, 0xa0);
//                break;
//            }
//            case 0x03:
//            {
//                rc632_byte_out(tmr_clk, 0x09); //6 ms
//                rc632_byte_out(tmr_reld, 0xa0);
//                break;
//            }
//            default:
//            {
//                rc632_byte_out(tmr_clk, 0x09);  //9.6 ms
//                rc632_byte_out(tmr_reld, 0xff);
//                break;
//            }
//        }
//    }

    public void RC632TimerStart(int x) {

        rc632ByteOut(tmr_cntrl, (byte) 0x02);

        switch (x) {
            case 0x01: {
                rc632ByteOut(tmr_clk, (byte) 0x07);  //1 ms
                rc632ByteOut(tmr_reld, (byte) 0x6a);
                break;
            }
            case 0x02: {
                rc632ByteOut(tmr_clk, (byte) 0x07);  //1.5 ms
                rc632ByteOut(tmr_reld, (byte) 0xa0);
                break;
            }
            case 0x03: {
                rc632ByteOut(tmr_clk, (byte) 0x09); //6 ms
                rc632ByteOut(tmr_reld, (byte) 0xa0);
                break;
            }
            default: {
                rc632ByteOut(tmr_clk, (byte) 0x09);  //9.6 ms
                rc632ByteOut(tmr_reld, (byte) 0xff);
                break;
            }
        }
    }

    void RC632TimerStop() {
        rc632SetBitMask(cntrl, (byte) 0x04);
    }


}
