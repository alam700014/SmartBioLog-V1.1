package com.android.fortunaattendancesystem.extras;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by fortuna on 19/2/16.
 */
public class Communication{

    ExecutorService executorService;

    String gURL = "http://122.160.53.175:83/PurpleMoorPlus/api/DXSWebApi?CommandID=03&CPUID=123456&TokenNo=123456789";
    String pURL = "http://122.160.53.175:83/PurpleMoorPlus/api/DXSWebApi";

    InputStream in = null;
    String JsonString = "",Date, Time;
    String log="";

    private static String DB_PATH = "/mnt/sdcard/project_data/Android.db";
    private static String table1 = "AttendanceT";
    private static String table2 = "FingerTemplateX";
    SQLiteDatabase db;

//==================================Thread Services=========================================================================================
    public void MakeThreadCall(){

        executorService = Executors.newFixedThreadPool(4);
        Thread1 thread1 = new Thread1();



      //  Thread2 thread2 = new Thread2();



       // Thread3 thread3 = new Thread3();
       // Thread4 thread4 = new Thread4();
       // executorService.submit(thread1);
         executorService.submit(thread1);
       // executorService.submit(thread3);
       // executorService.submit(thread4);
    }
//--------------------------------------------------------------------------------------------------------------------------------------------------
    public class Thread1 implements Runnable {
        @Override
        public void run(){
            while (true){
                try {

                    new SendRequest().execute(gURL);

                    Thread.sleep(5000);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
//--------------------------------------------------------------------------------------------------------------------------------------------------
    public class Thread2 implements Runnable {
        @Override
        public void run() {
            while(true) {
                try{

                    PostAttendanceT();

                    Thread.sleep(5000);

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
//--------------------------------------------------------------------------------------------------------------------------------------------------
    public class Thread3 implements Runnable {
        @Override
        public void run(){
            while (true){
                try{
                    //PostDeviceStatus();
                    Thread.sleep(5000);
                //} catch (JSONException e) {
                 //   e.printStackTrace();
                //} catch (IOException e) {
               //     e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
//--------------------------------------------------------------------------------------------------------------------------------------------------
    public class Thread4 implements Runnable {
        @Override
        public void run(){
            while (true){
                try{

                    PostNewlyEnrollTemplates();
                    Thread.sleep(5000);

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
//==================================== Send Request for Date & Time ==================================================================================================================
    public class SendRequest extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            try {
                return getrequest(gURL);
            } catch (IOException e) {
                return "Internate Connection Failed !!!";
            }
        }
        // displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            if (result == "Internate Connection Failed !!!") {
                //status.setText(result);
            } else {
                jsonParser(result);
            }
        }
    }
//--------------------------------------------------------------------------------------------------------------------------------------
    private String getrequest(String myurl) throws IOException {
        InputStream is = null;
        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();

            if (response == HttpURLConnection.HTTP_OK) {
                is = conn.getInputStream();
            } else {
                return "Internate Connection Failed !!!";
            }
            // Convert the InputStream into a string
            String contentAsString = convert(is);
            return contentAsString;

        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    //----------------------------Parsing JSON String ----------------------------------------------------------------------------------------------------------
    public void jsonParser(String JSONString) {



        Log.d("TEST","DATE TIME:"+JSONString);


        try
        {
            JSONObject reader = new JSONObject(JSONString);
            String CommandString = reader.getString("CommandString");
            JSONObject sys = new JSONObject(CommandString);
            Time = sys.getString("TIME");
            Date = sys.getString("DATE");

            String dd = Date.substring(0, 2);
            String mm = Date.substring(2, 4);
            String yyyy = Date.substring(4);
            String hh1 = Time.substring(0, 2);
            String MM1 = Time.substring(2, 4);
            int t1 = Integer.parseInt(hh1);
            int t2 = Integer.parseInt(MM1);
            String ss = Time.substring(4);
            String hh = String.valueOf(t1 + 05);
            String MM = String.valueOf(t2 + 30);
            changeSystemTime(yyyy, mm, dd, hh, MM, ss);
        }catch (JSONException e) {
            //Toast.makeText(Communication.this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }
    //----------------------------------SET Receive Server TIME To Device-----------------------------------------------------------------------------------------------------------------------------

    private void changeSystemTime(String year, String month, String day, String hour, String minute, String second){
        try{
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            String command = "date -s " + year + month + day + "." + hour + minute + second + "\n";
            Log.e("command", command);
            os.writeBytes(command);
            os.flush();
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//========================================= POST JSON Data TO Server ==============================================================================================================================
    public class PostJSON extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            try
            {

                return postjson(pURL);

            } catch (IOException e) {
                return "Internate Connection Failed !!!";
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            if (result == "Internate Connection Failed !!!") {
                //status.append("\n\n" +result);
                log = log + result;
                log();
            } else {
                //status.append("\n\n" +result);
                log = log + result;
                log();
            }
        }
    }
//----------------------------------------------------------------------------------------------------------------------------
    private String postjson(String myurl) throws IOException {
        InputStream is = null;
        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestMethod("POST");
            conn.connect();
            //Write
            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

            Log.d("TEST","JSON Data:"+JsonString);

            writer.write(JsonString);


            writer.close();
            os.close();
            int response = conn.getResponseCode();

            Log.d("TEST","Response Code:"+response);

            if (response == HttpURLConnection.HTTP_OK) {
                is = conn.getInputStream();
            } else {
                return "Internate Connection Failed !!!";
            }
            // Convert the InputStream into a string

            String contentAsString = convert(is);
            Log.d("TEST","Response Value:"+contentAsString);



            return contentAsString;

        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

//======================================== Convert InputStream To JSON String =========================================================================================================================
    public String convert(InputStream in)
    {
        try
        {

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            int sizeOfJSONFile = in.available();

            byte[] data = new byte[sizeOfJSONFile];

            int nRead;

            while((nRead=in.read(data,0,data.length))!=-1)
            {
                buffer.write(data,0,nRead);
            }

            buffer.flush();

            byte[] bytes=buffer.toByteArray();

            in.close();

            String JSONString = new String(bytes, "UTF-8");

            return JSONString;

        } catch (Exception e) {
            //Toast.makeText(Communication.this, e.toString(), Toast.LENGTH_LONG).show();
        }
        return null;
    }
//=========================================================== POST Device Status =========================================================================================================
    /*public void PostDeviceStatus() throws JSONException, IOException {
        String[] columns = {"ID", "Addr", "EstbCD", "DevType", "DeviceStat", "IPAddress", "GPRSSignal", "GPRSOperator",
                "GPRSTower", "SIMNo", "UncapRec", "EnrolledUser", "TotalUser", "TotalTemplate", "Firmware",
                "TimeZone", "FtemplteType", "TemplateStoringMode"};
        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
            String selectQuery = "SELECT  * FROM " + table1;
            Cursor c = db.rawQuery(selectQuery, null);

            int ai = c.getColumnIndex("ID");
            int emp = c.getColumnIndex("Addr");
            int nm = c.getColumnIndex("EstbCD");
            int mn = c.getColumnIndex("DevType");
            int mi = c.getColumnIndex("DeviceStat");
            int pn = c.getColumnIndex("IPAddress");
            int vm = c.getColumnIndex("GPRSSignal");
            int vu = c.getColumnIndex("GPRSOperator");
            int ve = c.getColumnIndex("GPRSTower");
            int fq = c.getColumnIndex("SIMNo");
            int fn1 = c.getColumnIndex("UncapRec");
            int sl = c.getColumnIndex("EnrolledUser");
            int ix = c.getColumnIndex("TotalUser");
            int tp = c.getColumnIndex("TotalTemplate");
            int ve2 = c.getColumnIndex("Firmware");
            int fn2 = c.getColumnIndex("TimeZone");
            int fq2 = c.getColumnIndex("FtemplteType");
            int sl2 = c.getColumnIndex("TemplateStoringMode");

            if (c != null) {

                while (c.moveToNext()) {

                    columns[0] = c.getString(ai);
                    columns[1] = c.getString(emp);  //empid
                    columns[2] = c.getString(nm);    //name
                    columns[3] = c.getString(mn);    //validupto
                    columns[4] = c.getString(mi);   //birthday
                    columns[5] = c.getString(pn);     //sitecode
                    columns[6] = c.getString(vm);    //bloodgroup
                    columns[7] = c.getString(vu);   //smartversion
                    columns[8] = c.getString(ve);   //verification mode
                    columns[9] = c.getString(fq);    //f1 finger
                    columns[10] = c.getString(fn1);    //f1-quality
                    columns[11] = c.getString(sl);   //f1-security level
                    columns[12] = c.getString(ix);   //f1- index
                    columns[13] = c.getString(tp);    //f1- template
                    columns[14] = c.getString(ve2);    //verification mode 2
                    columns[15] = c.getString(fn2);    //f2 finger
                    columns[16] = c.getString(fq2);    //f2-quality
                    columns[17] = c.getString(sl2);    //f2-security level
                }
            }
            db.close();
        } catch (SQLiteException e) {
            //Toast.makeText(Communication.this, e.toString(), Toast.LENGTH_SHORT).show();
        }
        List<JSONObject> pt_1 = new ArrayList<JSONObject>();
        JSONObject obj = new JSONObject();
        JSONObject pt = new JSONObject();

        obj.put("TokenNo", "123456");
        obj.put("CPUID", "Deb123456");
        obj.put("CommandID", "00");
        obj.put("IsEncrypted", "0");

        pt.put("Add", columns[1]);
        pt.put("EstbCD", columns[2]);
        pt.put("DevType", columns[3]);
        pt.put("DeviceStat", columns[4]);
        pt.put("IPAddress", columns[5]);
        pt.put("GPRSSignal", columns[6]);
        pt.put("GPRSOperator", columns[7]);
        pt.put("GPRSTower", columns[8]);
        pt.put("SIMNo", columns[9]);
        pt.put("UncapRec", columns[10]);
        pt.put("EnrolledUser", columns[11]);
        pt.put("TotalUser", columns[12]);
        pt.put("TotalTemplate", columns[13]);
        pt.put("Firmware", columns[14]);
        pt.put("TimeZone", columns[15]);
        pt.put("FTemplteType", columns[16]);
        pt.put("TemplateStoringMode", columns[17]);

        pt_1.add(pt);
        obj.put("CommandString", pt_1);
        JsonString = obj.toString();
        JsonString = JsonString.replace("[", "");
        JsonString = JsonString.replace("]", "");

        new PostJSON().execute(pURL);
    }*/

    //==================================================POST Attendance Data============================================================================================
    public void PostAttendanceT() throws JSONException, IOException {

        List<JSONObject> pt_1 = new ArrayList<JSONObject>();


        try
        {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            String[] columns = {"ID", "Addr", "EstablishmentCode", "EmployeeID", "CardID", "PunchDate", "PunchTime", "InOutMode", "ReasonCode", "Lat", "Long"};
            String selectQuery = "SELECT  * FROM " + table1;
            Cursor c = db.rawQuery(selectQuery, null);

            int ai = c.getColumnIndex("ID");
            int emp = c.getColumnIndex("Addr");
            int nm = c.getColumnIndex("EstablishmentCode");
            int mn = c.getColumnIndex("EmployeeID");
            int mi = c.getColumnIndex("CardID");
            int pn = c.getColumnIndex("PunchDate");
            int vm = c.getColumnIndex("PunchTime");
            int vu = c.getColumnIndex("InOutMode");
            int ve = c.getColumnIndex("ReasonCode");
            int fq = c.getColumnIndex("Lat");
            int fn1 = c.getColumnIndex("Long");

            if (c != null)
            {
                while (c.moveToNext())
                {
                    columns[0] = c.getString(ai);
                    columns[1] = c.getString(emp);
                    columns[2] = c.getString(nm);
                    columns[3] = c.getString(mn);
                    columns[4] = c.getString(mi);
                    columns[5] = c.getString(pn);
                    columns[6] = c.getString(vm);
                    columns[7] = c.getString(vu);
                    columns[8] = c.getString(ve);
                    columns[9] = c.getString(fq);
                    columns[10] = c.getString(fn1);

                    JSONObject pt = new JSONObject();

                    pt.put("ADD", columns[1]);
                    pt.put("ESTBCODE",columns[2]);

                    pt.put("EID", columns[3]);
                    pt.put("CID", columns[4]);

                    pt.put("DATE", columns[5]);
                    pt.put("TIME", columns[6]);
                    //pt.put("IOMODE", columns[7]);

                    pt.put("IOMODE","I");

                    pt.put("REASONCODE", columns[8]);
                   // pt.put("LAT", columns[9]);
                    pt.put("LAT", "99");
                   // pt.put("LONG", columns[10]);
                    pt.put("LONG", "88");


                    pt_1.add(pt);
                }
            }
            db.close();
        } catch (SQLiteException e) {
            //Toast.makeText(Communication.this, e.toString(), Toast.LENGTH_SHORT).show();
        }

        JSONObject obj = new JSONObject();

        obj.put("TokenNo","Deb123456");
        obj.put("CPUID","Deb123456");
        obj.put("CommandID","05");
        obj.put("IsEncrypted","0");
        obj.put("CommandString", pt_1);

        JsonString = obj.toString();

        Log.d("TEST","JSON DATA FORMAT:"+JsonString);

        new PostJSON().execute(pURL);
        // status.append(JsonString);
    }
//==================================================================== POST New Enroll Template ========================================================================================================

    public void PostNewlyEnrollTemplates() throws JSONException {
        List<JSONObject> pt_1 = new ArrayList<JSONObject>();
        JSONObject obj = new JSONObject();
        JSONObject pt = new JSONObject();
        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
            String[] columns = {"ID", "AutoID", "SensorType", "TermplateType", "TemplateSrNo", "FingerIndex", "SecurityLevel", "VerificationMode", "Quality", "EnrolledOn", "Template"};
            String selectQuery = "SELECT  * FROM " + table2;
            Cursor c = db.rawQuery(selectQuery, null);

            int ai = c.getColumnIndex("ID");
            int emp = c.getColumnIndex("AutoID");
            int nm = c.getColumnIndex("SensorType");
            int mn = c.getColumnIndex("TermplateType");
            int mi = c.getColumnIndex("TemplateSrNo");
            int pn = c.getColumnIndex("FingerIndex");
            int vm = c.getColumnIndex("SecurityLevel");
            int vu = c.getColumnIndex("VerificationMode");
            int ve = c.getColumnIndex("Quality");
            int fq = c.getColumnIndex("EnrolledOn");
            int fn1 = c.getColumnIndex("Template");

            if (c != null) {
                while (c.moveToNext()) {
                    columns[0] = c.getString(ai);
                    columns[1] = c.getString(emp);
                    columns[2] = c.getString(nm);
                    columns[3] = c.getString(mn);
                    columns[4] = c.getString(mi);
                    columns[5] = c.getString(pn);
                    columns[6] = c.getString(vm);
                    columns[7] = c.getString(vu);
                    columns[8] = c.getString(ve);
                    columns[9] = c.getString(fq);
                    columns[10] = c.getString(fn1);

                    pt.put("AutoID", columns[1]);
                    pt.put("SensorType", columns[2]);
                    pt.put("TermplateType", columns[3]);
                    pt.put("TemplateSrNo", columns[4]);
                    pt.put("FingerIndex", columns[5]);
                    pt.put("SecurityLevel", columns[6]);
                    pt.put("VerificationMode", columns[7]);
                    pt.put("Quality", columns[8]);
                    pt.put("EnrolledOn", columns[9]);
                    pt.put("Template", columns[10]);
                    pt.put("ActiveYorN", "Y");
                    pt_1.add(pt);
                }
            }
            db.close();
        } catch (SQLiteException e) {
            //Toast.makeText(Communication.this, e.toString(), Toast.LENGTH_SHORT).show();
        }
        obj.put("TokenNo", "123456");
        obj.put("CPUID", "Deb123456");
        obj.put("CommandID", "05");
        obj.put("IsEncrypted", "0");
        obj.put("CommandString", pt_1);
        JsonString = obj.toString();
        // status.append(JsonString);

        new PostJSON().execute(pURL);
    }
//===========================================================================================================================================================================
    public void log(){
        try
        {
            File myFile = new File("/sdcard/project_data/Communication_log.txt");
            myFile.createNewFile();
            FileOutputStream fOut = new FileOutputStream(myFile);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(log);
            myOutWriter.close();
            fOut.close();
            //Toast.makeText(getBaseContext(), "Done writing SD 'mysdfile.txt'", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            //Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show();
        }
    }
}

