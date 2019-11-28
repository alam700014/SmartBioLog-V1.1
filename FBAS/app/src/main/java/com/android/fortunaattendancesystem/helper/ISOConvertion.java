package com.android.fortunaattendancesystem.helper;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.android.fortunaattendancesystem.R;

import java.io.IOException;

import Decoder.BASE64Decoder;
import Decoder.BASE64Encoder;


public class ISOConvertion extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_isoconvertion);



        try
        {
            String strEncodedString="Rk1SACAyMAAAAAEOAAABAAGQAMUAxQEAAABAKIB9ANq4XYCGAOHEXUB2AKugXUA0AM6sXUCUAL6YXUBvAJGUXYCeAOHQUEA9AOq4XYCsALt0XUA5AKukXUCeAKF8XYDDANxkXUA2AJ2gXYA5ARXIXYCEAGeAV0A9AHyYXYAoARDAXYBJATFQXUBcAUvYXYC3ARDgXYCSAGf0V4CsAHdwXYATARC8XUBeAEGQXUAxAGsYXYDiAKTcXUAMAResXUCGAWBcXUA5AFKYXYDPAIVkXUA9AU9UXYA7AE0UXUAaATi8XUC6AGBsXYDKAG7gXUCNAXDoXYDUATFcXUAhAU9UV0BQAYFgXUBEACWQXQAA";//270(40)
            BASE64Decoder decoder=new BASE64Decoder();

            byte[] rawTemplate=decoder.decodeBuffer(strEncodedString);

            Log.d("TEST","ISO RAW TEMPLATE"+new String(rawTemplate));

            Log.d("TEST","ISO RAW TEMPLATE LEN"+rawTemplate.length);

            byte[] convertedTemp=convertTemplate(rawTemplate);


            Log.d("TEST","CONVERTED TEMPLATE"+new String(convertedTemp));

            Log.d("TEST","CONVERTED TEMPLATE LEN"+convertedTemp.length);

            BASE64Encoder encode=new BASE64Encoder();

            String strConvTempBase64=encode.encode(convertedTemp);

            Log.d("TEST","BASE 64:"+strConvTempBase64);


            String strHexTemplate="464D520020323000000000AE00000120016800C500C5010000000018808800C79C0080EB00C5CA0040EB007B6200806100FBC400805500FEBF00402B00BDA80080970035F700804E00460B00408300C1A10080DC0083E90080EB00F5C60040B700547A0080BF0047750040E8004E650080F001264300402400769A0080D300D2CB0080EB00866C0080900115410080AB011DB50080BF012EB90080EF004D660081020121BD00401D010CC0000000";

            //===========Hex To Raw Convertion============//

            int len = strHexTemplate.length();
            byte[] data = new byte[len / 2];
            for (int i = 0; i < len; i += 2) {
                data[i / 2] = (byte) ((Character.digit(strHexTemplate.charAt(i), 16) << 4) + Character.digit(strHexTemplate.charAt(i + 1), 16));
            }

            Log.d("TEST","RAW TEMPLATE LEN:"+data.length);

            //==============================================//


            //========Raw to Base 64==================//

            BASE64Encoder encoder=new BASE64Encoder();

            String strConvTempBase64Template=encoder.encode(data);

            Log.d("TEST","BASE 64 FINAL:"+strConvTempBase64Template);

            //==============================================//




            // dt_cnt=252;


        } catch (IOException e) {
            e.printStackTrace();
        }




    }

    /*
        # This function receive any size of template part through sptr & save in dptr buffer
        # dt_cnt - desire template cnt/template frame size
     */
    public byte[] convertTemplate(byte[] a)
    {

            byte[] b =null;
            int i=0,j=0;
            int a2;

            //this loop will decide whether template is < or > 256 byte

            if(a[10] == 0)													 //00 00 '00' FC
            {
                b=new byte[a[11]];

                for(a2=0; a2!=a[11]; a2++)
                {
                    b[i++]=a[j++];
                }

            }
            else
            {
                // template > 256

                b=new byte[252];

                a[11]=(byte)252;
                a[27]=(byte)37;

                for(a2=252; a2!=0; a2--)
                {
                    b[i++]=a[j++];
                }
            }

        return b;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_isoconvertion, menu);
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
