package com.android.fortunaattendancesystem.ezeeHr;

import com.android.fortunaattendancesystem.ezeeHr.controller.EzeeHrEventGenerator;


/**
 * Created by fortuna on 30/7/18.
 */

public class EzeeHrController extends EzeeHrEventGenerator{

    private Thread thread;

    public EzeeHrController(String[] getUrls){
        super(getUrls);
        this.thread = new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    while (true){
                        fireEvent(queue.take());
                        //this.sleep (500);
                        this.wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public void scanStart(){
       super.start();
        this.thread.start();

    }

    public void scanStop(){
        this.thread.destroy();
        super.stop();
    }

    public void workDoneNotify(){
        this.thread.notify();
    }
    public void addListner(EzeeHrListener listener){
        super.addListener(listener);
    }

    public void removeListner(EzeeHrListener listener){
        super.removeListener(listener);
    }
}

