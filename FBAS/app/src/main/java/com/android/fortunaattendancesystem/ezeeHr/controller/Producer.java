package com.android.fortunaattendancesystem.ezeeHr.controller;

import java.util.Timer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by fortuna on 30/7/18.
 */

abstract class Producer {
    protected BlockingQueue<String> queue;
    private Timer timer;
    private GetRequestThread[] getRemortEnroll;
    private String getUrls[];

    public Producer(String[] getUrls){
        this.queue = new ArrayBlockingQueue<String>(10);
        this.timer = new Timer();
        this.getUrls = getUrls;
        this.getRemortEnroll = new GetRequestThread[getUrls.length];
    }

    public void start(){
        for(GetRequestThread g : this.getRemortEnroll)
            this.timer.scheduleAtFixedRate(g,0,500);
    }

    public void stop(){
        this.timer.cancel();
    }

}
