package com.android.fortunaattendancesystem.ezeeHr;

import com.android.fortunaattendancesystem.ezeeHr.controller.Code;
import com.android.fortunaattendancesystem.ezeeHr.services.RemortEnroll;

import java.util.EventObject;

public class EzeeHrEvent extends EventObject {

    private String jesonString;
    private Code code;

    public EzeeHrEvent(Object source, String jesonString) {
        super(source);
        this.jesonString = jesonString;
        this.code = Code.REMORT_ENROLL;
    }

    public Object getEventData() {
        Object object = null;
        switch (code) {
            case REMORT_ENROLL:
                object = new RemortEnroll(this.jesonString);
                break;
        }
        return object;
    }

   public Code getCode() {
        return this.code;
    }
}