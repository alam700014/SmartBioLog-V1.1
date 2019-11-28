package com.friendlyarm.SmartReader;

import android.util.Log;

import com.friendlyarm.AndroidSDK.FileCtlEnum;
import com.friendlyarm.AndroidSDK.HardwareControler;
import com.friendlyarm.AndroidSDK.SPIEnum;

/**
 * Created by fortuna on 22/5/17.
 */
public class SPIApi {


    //private int spi_bits = 8;
    //private int spidelay = 0;
    //private int spi_speed = 500000;
    //private int spi_byte_order = SPIEnum.LSBFIRST;

    //7.5Mhz


    private static final String devName = "/dev/spidev0.0";
    private int spi_fd = -1;
    private int devFD = -1;

    private int spiMode = 0;
    static final char spiBits = 8;
    static final int spiSpeed = 50000;
    private int spidelay = 0;

    private int spi_byte_order = SPIEnum.LSBFIRST;




    public int begin() {

        int status = -1;

        spi_fd = HardwareControler.open(devName, FileCtlEnum.O_RDWR);
        if (spi_fd >= 0) {

            Log.d("TEST", "Spi Device Open Successfully");

			/* spi init */
            status = HardwareControler.setSPIWriteBitsPerWord(spi_fd, spiBits);

            if (status == 0) {
                status = HardwareControler.setSPIReadBitsPerWord(spi_fd, spiBits);
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


    public int setBitOrder(int order) {

        int status = -1;

        if (spi_fd < 0) {
            return status;
        }
        spi_byte_order = SPIEnum.MSBFIRST;
        if (spi_byte_order == SPIEnum.LSBFIRST) {
            spiMode |= SPIEnum.SPI_LSB_FIRST;
        } else {
            spiMode &= ~SPIEnum.SPI_LSB_FIRST;
        }
        status = HardwareControler.setSPIBitOrder(spi_fd, spi_byte_order);
        return status;

    }

    public int setDataMode(int mode) {

        int status = -1;

        if (spi_fd < 0) {
            return status;
        }
        switch (mode) {
            case SPIEnum.SPI_MODE0:
                spiMode &= ~(SPIEnum.SPI_CPHA | SPIEnum.SPI_CPOL);
                break;
            case SPIEnum.SPI_MODE1:
                spiMode &= ~(SPIEnum.SPI_CPOL);
                spiMode |= (SPIEnum.SPI_CPHA);
                break;
            case SPIEnum.SPI_MODE2:
                spiMode |= (SPIEnum.SPI_CPOL);
                spiMode &= ~(SPIEnum.SPI_CPHA);
                break;
            case SPIEnum.SPI_MODE3:
                spiMode |= (SPIEnum.SPI_CPHA | SPIEnum.SPI_CPOL);
                break;
            default:
                Log.e("TEST", "error data mode");
        }

        status = HardwareControler.setSPIDataMode(spi_fd, spiMode);
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



    public int spiInit() {

      //  devFD = HardwareControler.open( devName, FileCtlEnum.O_RDWR );


        devFD = HardwareControler.open( devName, FileCtlEnum.O_RDWR );


        if (devFD >= 0) {

            Log.d("TEST","Device Open " + devName + "Ok!");

			/* spi init */
            //HardwareControler.setSPIWriteBitsPerWord( devFD, spiBits );
           // HardwareControler.setSPIReadBitsPerWord( devFD, spiBits );

            if (HardwareControler.setSPIDataMode(devFD, spiMode) != 0) {
                return -1;
            }

            if (HardwareControler.setSPIWriteBitsPerWord(devFD, spiBits) != 0) {
                return -1;
            }


            if (HardwareControler.setSPIReadBitsPerWord(devFD, spiBits) != 0) {
                return -1;
            }

            if (HardwareControler.setSPIMaxSpeed(devFD, spiSpeed) == -1 ) {
                return -1;
            }

            return 0;

        } else {
            Log.d("TEST", "open " + devName + "failed!");
            return -1;
        }

    }



    public byte spiReadWrite(int spi_fd,byte value){
        if (spi_fd < 0) {
            return 0;
        }
        return (byte) HardwareControler.SPItransferOneByte(devFD, value, spidelay, spiSpeed, spiBits);
    }

//    public byte spiReadWrite(byte value){
//
//        Log.d("TEST","Device File Descriptor"+devFD);
//
//
//        if (devFD < 0) {
//            return 0;
//        }
//        byte[] writeData=new byte[1];
//        writeData[0]=value;
//        byte[] readData=new byte[16];
//
//        int status=-1;
//
//
//        status=HardwareControler.SPItransferBytes(devFD,writeData,readData,spidelay, spiSpeed, spiBits);
//
//        Log.d("TEST","Status:"+status);
//        Log.d("TEST","Read Value:"+readData);
//
//        return (byte)status;
//
//
//
//        //return (byte) HardwareControler.SPItransferOneByte(devFD, value, spidelay, spiSpeed, spiBits);
//
//
//    }

}
