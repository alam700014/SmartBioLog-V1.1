package com.android.fortunaattendancesystem.submodules;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by fortuna on 18/1/19.
 */

public class WiegandCommunicator {

    public static char[] readWiegand(String path) {
        byte[] byteData = null;
        char[] charData = null;
        File file = new File(path);
        if (file.exists()) {
            BufferedInputStream inputStream = null;
            try {
                int size = (int) file.length();
                byteData = new byte[size];
                charData = new char[size];
                inputStream = new BufferedInputStream(new FileInputStream(file));
                inputStream.read(byteData, 0, byteData.length);
                for (int i = 0; i < size; i++) {
                    charData[i] = (char) byteData[i];
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return charData;
    }

    public static boolean clearWiegand(String path, String val) {
        boolean status = false;
        File file = new File(path);
        if (file.exists()) {
            BufferedOutputStream outputStream = null;
            byte[] data = val.getBytes();
            int size = data.length;
            try {
                outputStream = new BufferedOutputStream(new FileOutputStream(file));
                outputStream.write(data, 0, size);
                outputStream.flush();
                status = true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return status;
    }
}
