package com.android.fortunaattendancesystem.extras;

/**
 * Created by fortuna on 30/11/16.
 */

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by fortuna on 19/2/16.
 */

public class PurpleMoorePlusLayer {


    SimpleDateFormat df = new SimpleDateFormat("ddMMyyyy");
    ExecutorService executorService;
    String gURL = "http://122.160.53.175:83/PurpleMoorPlus/api/DXSWebApi?CommandID=03&CPUID=123456&TokenNo=123456789";
    String pURL = "http://122.160.53.175:83/PurpleMoorPlus/api/DXSWebApi";
    String strDate, strTime;
    InputStream in = null;
    String log = "";
    DataBaseLayer dbLayer = new DataBaseLayer();


    String serverIP,serverPort,url;


    PurpleMoorePlusLayer(String serverIP, String serverPort, String url) {
        this.serverIP=serverIP;
        this.serverPort=serverPort;
        this.url=url;
    }


    //==================================Thread Services=========================================================================================

    public void MakeThreadCall() {
        executorService = Executors.newFixedThreadPool(2);
        AttendanceDataTransferThread attendanceDataTransferThread = new AttendanceDataTransferThread();
        DateTimeSyncThread dateTimeSyncThread = new DateTimeSyncThread();
        executorService.submit(dateTimeSyncThread);
        executorService.submit(attendanceDataTransferThread);
    }

    public class AttendanceDataTransferThread implements Runnable {
        @Override
        public void run() {

            while (true) {

                try {

                    String strJSONData = "";
                    strJSONData = dbLayer.getAttendanceDataForPM();

                    Log.d("TEST", "Request JSON Data:" + strJSONData);

                    if (strJSONData != null && strJSONData.trim().length() > 0) {
                        String strServerUrl = "http://" + serverIP + ":" + serverPort + url;
                        new PostAttendanceData().execute(strServerUrl, strJSONData);
                    }

                    Thread.sleep(15000);

                } catch (Exception e) {
                    log(e.getMessage() + "\n");
                }
            }
        }
    }

    //--------------------------------------------------------------------------------------------------------------------------------------------------
//    public class AttendanceDataTransferThread implements Runnable {
//        @Override
//        public void run() {
//
//            while (true) {
//
//                try {
//                    String strJSONData = "";
//                    strJSONData = dbLayer.getAttendanceDataForPM();
//
//                    Log.d("TEST", "Request JSON Data:" + strJSONData);
//
//                    if (strJSONData != null && strJSONData.trim().length() > 0) {
//                        ArrayList list = dbLayer.getAtServerIPPort();
//                        if (list != null) {
//                            String strServerUrl = "http://" + list.get(0).toString().trim() + ":" + list.get(1).toString().trim() + "/PurpleMoorPlus/api/DXSWebApi";
//                            new PostAttendanceData().execute(strServerUrl, strJSONData);
//                        } else {
//                            log("Attendance Server Configuration Not Found\n");
//                        }
//                    }
//
//                    Thread.sleep(15000);
//
//                } catch (Exception e) {
//                    log(e.getMessage() + "\n");
//                }
//            }
//        }
//    }

    //-------------------------Sync Date Time With Server-----------------------------------------------------------------------
    public class DateTimeSyncThread implements Runnable {
        @Override
        public void run() {

            while (true) {
                try {
                    ArrayList list = dbLayer.getAtServerIPPort();
                    if (list != null) {
                        String strServerUrl = "http://" + list.get(0).toString().trim() + ":" + list.get(1).toString().trim() + "/PurpleMoorPlus/api/DXSWebApi?CommandID=03&CPUID=123456&TokenNo=123456789";
                        new SyncServerDateTime().execute(strServerUrl);
                    } else {
                        log("Attendance Server Configuration Not Found\n");
                    }

                    Thread.sleep(120000);

                } catch (Exception e) {
                    log(e.getMessage() + "\n");
                }
            }
        }
    }

    //==================================== Send Request for Date & Time ==================================================================================================================
    public class SyncServerDateTime extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {

            String strDateTimeChange = "";
            int status = -1;

            String strResponse = getServerDateTime(params[0]);

            Log.d("TEST", "Response Value:" + strResponse);

            if (strResponse.trim().length() > 0) {
                status = parseDateTime(strResponse);
            }
            if (status != 0) {
                strDateTimeChange = "Date Time Updation Failure\n";
            }

            return strDateTimeChange;
        }

        // displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            log(result);
        }
    }

    //--------------------------------------------------------------------------------------------------------------------------------------
    private String getServerDateTime(String myurl) {

        HttpURLConnection conn = null;
        InputStream is = null;
        String contentAsString = "";
        try {
            URL url = new URL(myurl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();
            int response = conn.getResponseCode();

            if (response == HttpURLConnection.HTTP_OK) {
                is = conn.getInputStream();
                contentAsString = convert(is);
            } else {
                log("HTTP Response Not Ok\n");
            }

        } catch (IOException e) {

            log(e.getMessage() + "\n");

        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    log(e.getMessage() + "\n");
                }
            }
            if (conn != null) {
                conn.disconnect();
            }
        }

        return contentAsString;
    }

    public int parseDateTime(String JSONString) {

        int status = -1;
        try {
            JSONObject reader = new JSONObject(JSONString);
            String CommandString = reader.getString("CommandString");
            JSONObject sys = new JSONObject(CommandString);
            strTime = sys.getString("TIME");
            strDate = sys.getString("DATE");
            String dd = strDate.substring(0, 2);
            String mm = strDate.substring(2, 4);
            String yyyy = strDate.substring(4);
            String hh1 = strTime.substring(0, 2);
            String MM1 = strTime.substring(2, 4);
            int t1 = Integer.parseInt(hh1);
            int t2 = Integer.parseInt(MM1);
            String ss = strTime.substring(4);
            String hh = String.valueOf(t1 + 05);
            String MM = String.valueOf(t2 + 30);

            //SimpleDateFormat df=new SimpleDateFormat("ddMMyyyyHHmmss");

//            SimpleDateFormat df=new SimpleDateFormat("ddMMyyyy");
//
//            try{
//                //Date server=df.parse(dd+mm+yyyy+hh+MM+ss);
//
//                Date server=df.parse(dd+mm+yyyy);
//                Date local=df.parse(df.format(Calendar.getInstance().getTime()));
//
//                Log.d("TEST","Server Date:"+server);
//                Log.d("TEST","Local Date:"+local);
//
//                if(server.compareTo(local)==0){
//                    Log.d("TEST","Server And Local Date Time Are Same");
//                }else{
//                    Log.d("TEST","Server And Local Date Time Are Different");
//                }
//
//
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }


            status = changeSystemTime(yyyy, mm, dd, hh, MM, ss);

        } catch (JSONException e) {
            log(e.getMessage());
        }
        return status;
    }

    private void compareDates() {

    }


    private int changeSystemTime(String year, String month, String day, String hour, String minute, String second) {

        int status = -1;

        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            String command = "date -s " + year + month + day + "." + hour + minute + second + "\n";
            Log.e("command", command);
            os.writeBytes(command);
            os.flush();
            os.writeBytes("exit\n");
            os.flush();
            status = process.waitFor();
            Log.d("TEST", "Process Wait Value:" + status);
        } catch (InterruptedException e) {
            log(e.getMessage());
            Log.d("TEST", "Exception 1:" + e.getMessage());
        } catch (IOException e) {
            log(e.getMessage());
            Log.d("TEST", "Exception 2:" + e.getMessage());
        }

        return status;
    }


    //========================================= POST JSON Data TO Server ==============================================================================================================================
    public class PostAttendanceData extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {

            String strServerUrl = strings[0];
            String strJson = strings[1];
            int updateStatus = -1;

            String strPostAttendance = "";

            String strJSONAttendanceData = postAttendanceData(strServerUrl, strJson);

            Log.d("TEST", "Server Response Value:" + strJSONAttendanceData);

            if (strJSONAttendanceData.trim().length() > 0) {

                try {
                    JSONObject reader = new JSONObject(strJSONAttendanceData);
                    String parsedResponse = reader.getString("CommandString");

                    if (parsedResponse.trim().length() == 0) {

                        //===============Update Table After Data Send To Server=============//
                        updateStatus = dbLayer.updateAttendanceTable();
                        Log.d("TEST", "Update Status:" + updateStatus);
                        //=================================================================//
                    } else {

                        String strCmdStatus = reader.getString("CmdStatDesc");
                        log(strCmdStatus + "\n");
                    }
                } catch (JSONException e) {
                    log(e.getMessage() + "\n");
                }
            }

            if (updateStatus == -1) {
                strPostAttendance = "Attendance Data Updation Faliure";
            }


            return strPostAttendance;
        }

        // onPostExecute displays the results of the AsyncTask.

        @Override
        protected void onPostExecute(String result) {
            log(result + "\n");
        }
    }

    //----------------------------------------------------------------------------------------------------------------------------
    private String postAttendanceData(String strServerUrl, String strJSONData) {

        InputStream is = null;
        String contentAsString = "";
        HttpURLConnection conn = null;
        OutputStream os = null;
        BufferedWriter writer = null;

        try {
            URL url = new URL(strServerUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(30000 /* milliseconds */);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestMethod("POST");
            conn.connect();

            //Write
            os = conn.getOutputStream();
            writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(strJSONData);
            writer.flush();
            writer.close();
            os.close();

            int response = conn.getResponseCode();

            Log.d("TEST", "Response Code:" + response);

            if (response == HttpURLConnection.HTTP_OK) {
                is = conn.getInputStream();
                contentAsString = convert(is);
            } else {

                log("HTTP Server Response Not OK During Attendance Post\n");
            }

        } catch (Exception e) {
            log(e.getMessage() + "\n");
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                    log(e.getMessage() + "\n");
                }
            }

            if (os != null) {

                try {
                    os.close();
                } catch (IOException e) {
                    log(e.getMessage() + "\n");
                }
            }

            if (writer != null) {

                try {
                    writer.close();
                } catch (IOException e) {
                    log(e.getMessage() + "\n");
                }
            }

            if (conn != null) {
                conn.disconnect();
            }
        }

        return contentAsString;
    }


    //----------------------------Parsing JSON String ----------------------------------------------------------------------------------------------------------
//    public String getCommand(String JSONString) {
//
//        String serverResponse="failure";
//
//
//
//    }
    //======================================== Convert InputStream To JSON String =========================================================================================================================

    public String convert(InputStream in) {
        String JSONString = "";
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
            JSONString = new String(bytes, "UTF-8");
        } catch (Exception e) {
            log(e.getMessage() + "\n");
        } finally {
            if (buffer != null) {
                try {
                    buffer.close();
                } catch (IOException e) {
                    log(e.getMessage() + "\n");
                }
            }
        }
        return JSONString;
    }

    //===========================================================================================================================================================================
    public void log(String logData) {

        FileOutputStream fout = null;
        OutputStreamWriter myoutWriter = null;
        try {

            Calendar calendar = Calendar.getInstance();
            String strDate = df.format(calendar.getTime());

          //  Log.d("TEST", "Date:" + strDate);

            File myFile = new File("/sdcard/project_data/Communication_log_" + strDate + ".txt");
            if (!myFile.exists()) {
                myFile.createNewFile();
            }
            fout = new FileOutputStream(myFile, true);
            myoutWriter = new OutputStreamWriter(fout);
            myoutWriter.append(logData);
            myoutWriter.close();
            fout.close();
        } catch (Exception e) {
        } finally {

            if (fout != null) {
                try {
                    fout.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (myoutWriter != null) {

                try {
                    myoutWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}

