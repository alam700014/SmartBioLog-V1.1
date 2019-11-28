package com.forlinx.android;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.fortunaattendancesystem.R;


public class AdcActivity extends Activity {
    private ImageButton quitbtn;
    private TextView textView;
    private Intent intent;
    private int mlinenum;


    private AdcMessageBroadcastReceiver receiver;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.adcnew);
        textView = (TextView) findViewById(R.id.valuetext);
        textView.setText("ADC Value: 0");
        mlinenum = 1;

        quitbtn = (ImageButton) findViewById(R.id.quit);

        quitbtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                if (intent != null) {
                    AdcActivity.this.stopService(intent);
                    intent = null;
                }

                if (receiver != null) {
                    unregisterReceiver(receiver);
                    receiver = null;
                }
                AdcActivity.this.finish();
            }

        });
        if (HardwareInterface.class != null) {
            receiver = new AdcMessageBroadcastReceiver();
            registerReceiver(receiver, getIntentFilter());
            intent = new Intent();
            intent.setClass(AdcActivity.this, GetValueService.class);
            intent.putExtra("mtype", "ADC");
            intent.putExtra("maction", "start");
            intent.putExtra("mfd", 1);
            AdcActivity.this.startService(intent);
        } else {
            Toast.makeText(getApplicationContext(), "Load hardwareinterface library error!", Toast.LENGTH_LONG).show();
        }
    }

    IntentFilter getIntentFilter() {
        IntentFilter intent = new IntentFilter();
        intent.addAction("ADC_UPDATE");
        return intent;
    }

    class AdcMessageBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            // TODO Auto-generated method stub
            String adcmessage = intent.getStringExtra("adc_value");
            if (adcmessage != null) {
                if ((textView.length() > 160) || (mlinenum > 7)) {

                    textView.setText(null);
                    //textView.setTextSize(0);
                    mlinenum = 0;
                }
                textView.append("\n" + "ADC Value: " + adcmessage);
                mlinenum++;
            } else {

            }
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub

        if (intent != null) {
            AdcActivity.this.stopService(intent);
        }

        if (receiver != null) {
            unregisterReceiver(receiver);
        }

        super.onDestroy();
    }

}
