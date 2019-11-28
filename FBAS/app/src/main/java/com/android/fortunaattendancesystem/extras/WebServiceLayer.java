package com.android.fortunaattendancesystem.extras;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

/**
 * Created by fortuna on 8/7/16.
 */
public class WebServiceLayer {



    private static String NAMESPACE="http://com.fortuna.aadhaarwebservice/";
    private static String URL ="http://192.168.0.103:8080/AadhaarWebService/aadhaarWebServices?wsdl";
    private static String SOAP_ACTION = "http://com.fortuna.aadhaarwebservice/";


    private static String NAMESPACEKYC="http://com.fortuna.aadhaarwebservice.ekyc/";
    private static String URLKYC ="http://192.168.0.103:8080/AadhaarKycService/aadhaarWebServices?wsdl";
    private static String SOAP_ACTIONKYC = "http://com.fortuna.aadhaarwebservice.ekyc/";


    public static String testWebService(String strData)
    {

        String resTxt="";

        SoapObject request = new SoapObject(NAMESPACE,"testWebService");

        PropertyInfo objProp = new PropertyInfo();

        objProp.setName("arg0");
        objProp.setValue(strData);
        objProp.setType(String.class);
        request.addProperty(objProp);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(request);
        HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);

        try
        {
            androidHttpTransport.call(SOAP_ACTION+"testWebService",envelope);
            SoapPrimitive response =(SoapPrimitive)envelope.getResponse();
            resTxt = response.toString();

        } catch (Exception e) {
            resTxt= e.getMessage();


        }

        return resTxt;

    }

    public static String authenticateAadhaarData(String strAadhaarId,String strAAdhaarName)
    {

        String resTxt="";

        SoapObject request = new SoapObject(NAMESPACE,"authenticateAaadharUserName");

        PropertyInfo objProp = new PropertyInfo();

        objProp.setName("arg0");
        objProp.setValue(strAadhaarId);
        objProp.setType(String.class);
        request.addProperty(objProp);

        objProp=new PropertyInfo();
        objProp.setName("arg1");
        objProp.setValue(strAAdhaarName);
        objProp.setType(String.class);
        request.addProperty(objProp);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(request);
        HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);

        try
        {
            androidHttpTransport.call(SOAP_ACTION+"authenticateAaadharUserName",envelope);
            SoapPrimitive response =(SoapPrimitive)envelope.getResponse();
            resTxt = response.toString();

        } catch (Exception e) {
            resTxt= e.getMessage();


        }

        return resTxt;

    }



    public static String authenticateAadhaarFingerData(String strAadhaarId,String strFingerIndex,String strHexFingerData)
    {

        String resTxt="";

        SoapObject request = new SoapObject(NAMESPACE,"authenticateAaadharFingerData");

        PropertyInfo objProp = new PropertyInfo();

        objProp.setName("arg0");
        objProp.setValue(strAadhaarId);
        objProp.setType(String.class);
        request.addProperty(objProp);

        objProp=new PropertyInfo();
        objProp.setName("arg1");
        objProp.setValue(strFingerIndex);
        objProp.setType(String.class);
        request.addProperty(objProp);

        objProp=new PropertyInfo();
        objProp.setName("arg2");
        objProp.setValue(strHexFingerData);
        objProp.setType(String.class);
        request.addProperty(objProp);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(request);
        HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);

        try
        {
            androidHttpTransport.call(SOAP_ACTION+"authenticateAaadharFingerData",envelope);
            SoapPrimitive response =(SoapPrimitive)envelope.getResponse();
            resTxt = response.toString();

        } catch (Exception e) {
            resTxt= e.getMessage();
        }

        return resTxt;

    }


    public static String authenticateAadhaarTemplate(String strAadhaarServerUrl,String strAadhaarId,String strHexFingerData)
    {

        String resTxt="";
        SoapObject request = new SoapObject(NAMESPACEKYC,"authenticateAadhaarData");
        PropertyInfo objProp = new PropertyInfo();

        objProp.setName("arg0");
        objProp.setValue(strAadhaarId);
        objProp.setType(String.class);
        request.addProperty(objProp);


        objProp=new PropertyInfo();
        objProp.setName("arg1");
        objProp.setValue(strHexFingerData);
        objProp.setType(String.class);
        request.addProperty(objProp);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(request);
        HttpTransportSE androidHttpTransport = new HttpTransportSE(strAadhaarServerUrl);

        try
        {
            androidHttpTransport.call(SOAP_ACTIONKYC+"authenticateAadhaarData",envelope);
            SoapPrimitive response =(SoapPrimitive)envelope.getResponse();
            resTxt = response.toString();

        }catch(Exception e)
        {
            return "Exception:"+e.getMessage();
        }

        return resTxt;
    }
}
