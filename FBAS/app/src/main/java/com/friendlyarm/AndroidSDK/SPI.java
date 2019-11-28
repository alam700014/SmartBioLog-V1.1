package com.friendlyarm.AndroidSDK;

import android.util.Log;

public class SPI {
    private static final String TAG = "com.friendlyarm.AndroidSDK.SPI";
    private int spi_mode = 0;
    private int spi_bits = 8;
    //	private int spi_delay = 1000;
    private int spi_delay = 0;
    private int spi_speed = 500000;
    private int spi_byte_order = SPIEnum.LSBFIRST;

    private static final String devName = "/dev/spidev0.0";
    private int spi_fd = -1;

    public int begin() {

        int status = -1;

        spi_fd = HardwareControler.open(devName, FileCtlEnum.O_RDWR);
        if (spi_fd >= 0) {

            Log.d("TEST", "Spi Device Open Successfully");

			/* spi init */
            status = HardwareControler.setSPIWriteBitsPerWord(spi_fd, spi_bits);

            if (status == 0) {
                status = HardwareControler.setSPIReadBitsPerWord(spi_fd, spi_bits);
            } else {
                status = -1;
            }

        } else {
            Log.d("TEST", "Spi Device Open Failure");
            spi_fd = -1;
            status = -1;
        }

        return spi_fd;
    }

    public void end() {
        if (spi_fd != -1) {
            HardwareControler.close(spi_fd);
            spi_fd = -1;
        }
    }

    public int setBitOrder(int order) {

        int status = -1;

        if (spi_fd < 0) {
            return status;
        }
        // spi_byte_order = SPIEnum.MSBFIRST;
        spi_byte_order = SPIEnum.LSBFIRST;

        if (spi_byte_order == SPIEnum.LSBFIRST) {
            spi_mode |= SPIEnum.SPI_LSB_FIRST;
        } else {
            spi_mode &= ~SPIEnum.SPI_LSB_FIRST;
        }
        status = HardwareControler.setSPIBitOrder(spi_fd, spi_byte_order);
        return status;

    }

    public int setClockDivider(int divider) {

        int status = -1;
        if (spi_fd < 0) {
            return status;
        }
        spi_speed = 66666666 / (2 * (divider + 1));
        if (spi_speed > 500000) {
            spi_speed = 500000;
        }
        status = HardwareControler.setSPIClockDivider(spi_fd, divider);
        return status;
    }

    public int setSpiSpeed(int speed) {

        int status = -1;
        if (spi_fd < 0) {
            return status;
        }
        status = HardwareControler.setSPIMaxSpeed(spi_fd, speed);
        return status;
    }

    public int setDataMode(int mode) {

        int status = -1;

        if (spi_fd < 0) {
            return status;
        }
        switch (mode) {
            case SPIEnum.SPI_MODE0:
                spi_mode &= ~(SPIEnum.SPI_CPHA | SPIEnum.SPI_CPOL);
                break;
            case SPIEnum.SPI_MODE1:
                spi_mode &= ~(SPIEnum.SPI_CPOL);
                spi_mode |= (SPIEnum.SPI_CPHA);
                break;
            case SPIEnum.SPI_MODE2:
                spi_mode |= (SPIEnum.SPI_CPOL);
                spi_mode &= ~(SPIEnum.SPI_CPHA);
                break;
            case SPIEnum.SPI_MODE3:
                spi_mode |= (SPIEnum.SPI_CPHA | SPIEnum.SPI_CPOL);
                break;
            default:
                Log.e(TAG, "error data mode");
        }

        status = HardwareControler.setSPIDataMode(spi_fd, spi_mode);
        return status;
    }

    public void setChipSelectPolarity(int cs, int active) {

    }

    public void chipSelect(int cs) {

    }

    public byte transfer(byte value) {
        if (spi_fd < 0) {
            return 0;
        }
        return (byte) HardwareControler.SPItransferOneByte(spi_fd, value, spi_delay, spi_speed, spi_bits);
    }

    public byte spiWrite(byte[] data) {

        byte[] readBytes = new byte[data.length];
        int status = -1;

        if (spi_fd < 0) {
            return 0;
        }

        status = HardwareControler.SPItransferBytes(spi_fd, data, readBytes, spi_delay, spi_speed, spi_bits);

        //Log.d("TEST", "Spi Write Status :" + status);
        //Log.d("TEST", "First Byte Write:" + data[0] + " Second Byte Write:" + data[1]);
        //Log.d("TEST", "First Byte Read:" + readBytes[0] + " Second Byte Read :" + readBytes[1]);

        //Log.d("TEST", "Return Read Value:" + readBytes[1]);

        return readBytes[1];

    }

    public int spiRead(byte[] data) {

        int dlen = data.length - 1;
        byte[] readBytes = new byte[dlen + 1];
        int status = -1;

        if (spi_fd < 0) {
            return 0;
        }

        status = HardwareControler.SPItransferBytes(spi_fd, data, readBytes, spi_delay, spi_speed, spi_bits);

        for (int counter = 0; counter != dlen; counter++) {
            data[counter] = readBytes[counter + 1];
        }

        return 1;

    }


//    public byte spiRead(byte[] data){
//
//        byte[] readBytes=new byte[2];
//
//        int status=-1;
//
//        if (spi_fd < 0) {
//            return 0;
//        }
//
//        status=HardwareControler.SPItransferBytes(spi_fd,data,readBytes,spi_delay,spi_speed,spi_bits);
//
//        Log.d("TEST","Spi Write Status :"+status);
//        Log.d("TEST","Byte Address:"+data[0]+" Byte Value:"+data[1]);
//        Log.d("TEST","First Read Value:"+readBytes[0]+" Second Read Value :"+readBytes[1]);
//
//
//
//        //   Log.d("TEST","Spi Read Value 1:"+readBytes[0]+" Spi Read Value 2:"+readBytes[1]);
//
//
//
//        return readBytes[0];
//
//    }


    public int spiWriteByte(byte value) {


        int status = -1;
        byte[] writeData = new byte[1];
        writeData[0] = value;

        byte[] readData = {0x01};


        if (spi_fd < 0) {
            return 0;
        }

        status = HardwareControler.SPItransferOneByte(spi_fd, value, spi_delay, spi_speed, spi_bits);

        Log.d("TEST", "Spi Write Status:" + status);

        return status;


        //return (byte) HardwareControler.SPItransferOneByte(devFD, value, spidelay, spiSpeed, spiBits);

    }

    public byte spiReadByte() {


        int status = -1;

        byte[] readData = {0x01};


        if (spi_fd < 0) {
            return 0;
        }

        status = HardwareControler.readBytesFromSPI(spi_fd, readData, spi_delay, spi_speed, spi_bits);

        Log.d("TEST", "Spi Read Status:" + status);
        Log.d("TEST", "Spi Read Value:" + readData[0]);

        return readData[0];

    }


    public byte spiReadWrite(byte value) {

        if (spi_fd < 0) {
            return 0;
        }

        int status = -1;
        byte[] writeData = new byte[1];
        writeData[0] = value;

        byte[] readData = {0x01};

        status = HardwareControler.SPItransferBytes(spi_fd, writeData, readData, spi_delay, spi_speed, spi_bits);

        Log.d("TEST", "Command Execution Status:" + status);
        Log.d("TEST", "Write Byte:" + value);
        Log.d("TEST", "Read Byte:" + readData[0]);

        return readData[0];


        //return (byte) HardwareControler.SPItransferOneByte(devFD, value, spidelay, spiSpeed, spiBits);


    }

    public byte spiWriteTest(byte data) {

        int status = -1;

        if (spi_fd < 0) {
            return 0;
        }

        status = HardwareControler.SPItransferOneByte(spi_fd, data, spi_delay, spi_speed, spi_bits);
        Log.d("TEST", "Spi Write Status After Write:" + status);

        byte[] read = new byte[1];
        read[0] = 0x01;

        status = HardwareControler.readBytesFromSPI(spi_fd, read, spi_delay, spi_speed, spi_bits);
        Log.d("TEST", "Spi Read Status After Write:" + status);

        Log.d("TEST", "Spi Read Value After Write:" + read[0]);

        return read[0];
    }

    public byte spiReadWriteTest(byte[] data) {

        byte[] readBytes = new byte[2];

        int status = -1;

        if (spi_fd < 0) {
            return 0;
        }

        status = HardwareControler.SPItransferBytes(spi_fd, data, readBytes, spi_delay, spi_speed, spi_bits);

        Log.d("TEST", "Spi Write Status :" + status);

        Log.d("TEST", "Write Value After Write Byte Address:" + byteToDecimal(data[0]) + " Byte Value:" + byteToDecimal(data[1]));

        Log.d("TEST", "Read Value First Byte Value : " + byteToDecimal(readBytes[0]) + " Second Byte Value:" + byteToDecimal(readBytes[1]));


        //   Log.d("TEST","Spi Read Value 1:"+readBytes[0]+" Spi Read Value 2:"+readBytes[1]);

        return readBytes[0];


        //return (byte)status;
    }

    public int byteToDecimal(byte b) {
        int val = b & 0xff;
        return val;
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


    public byte spiRead() {

        int status = -1;
        byte[] readData = new byte[2];

        if (spi_fd < 0) {
            return 0;
        }

        status = HardwareControler.readBytesFromSPI(spi_fd, readData, spi_delay, spi_speed, spi_bits);

        Log.d("TEST", "Spi Read Value 1:" + readData[0] + " Spi Read Value 2:" + readData[1]);

        return (byte) status;
    }


    //=======================SPI Read Write======================//


    public int spiByteWrite(byte data) {

        if (spi_fd < 0) {
            return 0;
        }

        int status = -1;
        status = HardwareControler.SPItransferOneByte(spi_fd, data, spi_delay, spi_speed, spi_bits);

        Log.d("TEST", "Write Command Execution Status:" + status);
        Log.d("TEST", "Write Byte:" + data);

        return status;

    }

    public byte spiByteRead() {

        byte[] res = new byte[1];

        if (spi_fd < 0) {
            return 0;
        }

        int status = -1;

        status = HardwareControler.readBytesFromSPI(spi_fd, res, spi_delay, spi_speed, spi_bits);

        Log.d("TEST", "Read Command Execution Status:" + status);
        Log.d("TEST", "Read Byte:" + res[0]);

        return res[0];

    }

}
