package com.android.fortunaattendancesystem.ezeeHr.controller;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;

/**
 * Created by fortuna on 28/7/18.
 */

class GetRequestThread extends TimerTask{

    private String strServerUrl;//= "http://" + serverIP + ":" + serverPort + GET_JOB_URL + "?corporateid=" + CORPORATE_ID + "&cpuid=" + imei + "&DeviceToken=" + device_token + "&commnandtype=" + TEMPLATE_REMOTE_ENROLL_JOB_COMM + "&DataCount=1000";
    private BlockingQueue<String> queue;
    private String strJson;

    public GetRequestThread(String serverUrl, BlockingQueue<String> queue){
            this.strServerUrl = serverUrl;
            this.queue = queue;
        }

        private String convert(InputStream in) {

        String responseJSON = "";
        ByteArrayOutputStream buffer = null;
        try {
            buffer = new ByteArrayOutputStream();
            byte[] data = new byte[1024];
            int nRead;
            while ((nRead = in.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            byte[] bytes = buffer.toByteArray();
            in.close();
            responseJSON = new String(bytes, "UTF-8");
        } catch (Exception e) {
            //log(e.getMessage() + "\n");
        } finally {
            if (buffer != null) {
                try {
                    buffer.close();
                } catch (IOException e) {
                    // log(e.getMessage() + "\n");
                }
            }
        }
        return responseJSON;
    }

        private String getRemorteData(String strServerUrl) {
            String returnJson = "";
            InputStream is = null;
            HttpURLConnection conn = null;
            try {
                URL url = new URL(strServerUrl);
                try {
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(30000 /* milliseconds */);
                    conn.setConnectTimeout(30000 /* milliseconds */);
                    conn.setRequestMethod("GET");
                    conn.connect();
                    int response = conn.getResponseCode();

                    if (response == HttpURLConnection.HTTP_OK) {
                        is = conn.getInputStream();
                        returnJson = convert(is);
                    } else {
                        Log.d("TEST", "HTTP Server Response Not OK During Get Remorte Enroll");
                    }

                    if (is != null) {
                        is.close();
                    }
                    if (conn != null) {
                        conn.disconnect();
                    }
                } catch (IOException e) {
                    Log.d("TEST", "getRemorteEnrollData IOException:" + e.toString());
                }
            } catch (MalformedURLException e) {
                Log.d("TEST", "URL Exception:" + e.getMessage());
            }
            return returnJson;
        }

        @Override
        public void run() {
            this.strJson = this.getRemorteData(this.strServerUrl);
            if(!this.strJson.isEmpty()){
                this.queue.add(this.strJson);
            }
        }
    }

