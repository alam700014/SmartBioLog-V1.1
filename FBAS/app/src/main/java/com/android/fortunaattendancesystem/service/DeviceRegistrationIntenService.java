package com.android.fortunaattendancesystem.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

/**
 * Created by suman-dhara on 18/9/17. It is extended from IntentService. It run when app is starting,
 * It will destroy after doing its worak at background.
 */

public class DeviceRegistrationIntenService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public DeviceRegistrationIntenService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        stopSelf();
    }

}