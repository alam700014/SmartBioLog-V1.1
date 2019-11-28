package com.example.spi;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by fortuna on 3/1/19.
 */

public class Spi {

    private String result;
    private String spiPath = "/dev/spidev0.0";

    static {
        System.loadLibrary("Spi");
    }

    private native String spiopen(String buf);

    public String spiWrite(byte addr, byte value) {
        addr = (byte) ((addr << 1) & 0x7E);
        byte[] dataWrite = {addr, value};

//        File file=new File(spiPath);
//        FileOutputStream fo=null;
//        try {
//            fo=new FileOutputStream(file);
//            fo.write(dataWrite);
//            fo.flush();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }finally {
//            if(fo!=null){
//                try {
//                    fo.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        return "";

        String writeBuf = new String(dataWrite);
        result = spiopen(writeBuf);
        Log.d("TEST", "Spi Write Result:" + result);
        return result;
    }

    public String spiWrite(byte[] data) {

//        File file=new File(spiPath);
//        FileOutputStream fo=null;
//        try {
//            fo=new FileOutputStream(file);
//            fo.write(data);
//            fo.flush();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }finally {
//            if(fo!=null){
//                try {
//                    fo.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//
//        return "";


        String writeBuf = new String(data);
        result = spiopen(writeBuf);
        Log.d("TEST", "Spi Write Return Result Single Byte::" + result);
        return result;
    }

    public byte spiRead(byte addr) {
        addr = (byte) (((addr << 1) & 0x7E) | 0x80);
       // byte[] dataRead = {addr, 0x00};
        byte[] dataRead = {addr};
        byte[] readBytes = new byte[dataRead.length];

        File file=new File(spiPath);
//        FileOutputStream fo=null;
        FileInputStream fi=null;
//        try {
//            fo=new FileOutputStream(file);
//            fo.write(dataRead);
//            fo.flush();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }finally {
//            if(fo!=null){
//                try {
//                    fo.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//
//        try {
//            fi=new FileInputStream(file);
//            fi.read(readBytes,0,readBytes.length);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }finally {
//            if(fi!=null){
//                try {
//                    fi.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//
//        Log.d("TEST","Read After Write Byte In:"+readBytes[1]);
//        return readBytes[1];


        String writeBuf = new String(dataRead);
        result = spiopen(writeBuf);

        try {
            fi = new FileInputStream(file);
            fi.read(readBytes, 0, readBytes.length);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fi != null) {
                try {
                    fi.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


//        byte[] returnData = result.getBytes();
//        Log.d("TEST", "Spi Read Return Result:" + Arrays.toString(returnData));

        Log.d("TEST","Spi Read Value:"+  Arrays.toString(readBytes));

        return readBytes[0];


//        Log.d("TEST","Result 2:"+result);
//        byte[] readBytes = new byte[dataRead.length];

    }

}
