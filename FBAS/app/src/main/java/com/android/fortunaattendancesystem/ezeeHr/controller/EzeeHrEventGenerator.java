package com.android.fortunaattendancesystem.ezeeHr.controller;

import com.android.fortunaattendancesystem.ezeeHr.EzeeHrEvent;
import com.android.fortunaattendancesystem.ezeeHr.EzeeHrListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by fortuna on 28/7/18.
 */

public abstract class EzeeHrEventGenerator extends Producer{

    private EzeeHrEvent ezeeHrEvent;
    private List listener;

    public EzeeHrEventGenerator(String[] getUrls) {
        super(getUrls);
        this.listener = new ArrayList();
    }


   protected void addListener(EzeeHrListener listener){
        this.listener.add(listener);
    }

    protected void removeListener(EzeeHrListener listener){
        this.listener.remove(listener);
    }

    protected synchronized void fireEvent(String jeson){
        Iterator iterator = this.listener.iterator();
        this.ezeeHrEvent = new EzeeHrEvent(this,jeson);
        switch (this.ezeeHrEvent.getCode()){
            case REMORT_ENROLL:
                while (iterator.hasNext())
                    ((EzeeHrListener)iterator.next()).remortEnrollReceived(this.ezeeHrEvent);
                break;

        }
    }
}
