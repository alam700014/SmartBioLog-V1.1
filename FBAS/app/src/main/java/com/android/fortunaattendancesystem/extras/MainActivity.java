package com.android.fortunaattendancesystem.extras;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.HashMap;


public class MainActivity extends BroadcastReceiver {

    HashMap<String,String> myMap=new HashMap<>();

    @Override
    public void onReceive(Context context, Intent intent) {

            Log.d("TEST","on receive called");

            String id = intent.getStringExtra("notificationID");
            Log.d("TEST","Id:"+id);
            if (myMap.get(id) != null)
                return;

            final String action = intent.getAction();
            if (action != null && Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
                Log.d("TEST","Alert Id:" +intent.getStringExtra("alert"));
                myMap.put(id,"123");
            }



//            String id = intent.getStringExtra("notificationID");
//            if (myMap.get(id) != null)
//                return;
//
//            final String action = intent.getAction();
//
//            if (action != null && Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
//
//                String strId = UUID.randomUUID().toString();
//                intent.putExtra("notificationID", strId);
//
//                myMap.put(strId, "123");
//
//                Intent login = new Intent(context,LoginActivity.class);
//                login.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                context.startActivity(login);
//
//
//            }
        }

    }


