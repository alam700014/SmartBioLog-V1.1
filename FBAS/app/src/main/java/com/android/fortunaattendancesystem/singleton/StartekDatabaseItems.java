package com.android.fortunaattendancesystem.singleton;

import com.android.fortunaattendancesystem.model.StartekInfo;

import java.util.ArrayList;

/**
 * Created by fortuna on 7/12/18.
 */

public class StartekDatabaseItems {

    private static StartekDatabaseItems mInstance = null;
    private ArrayList <StartekInfo> databaseItemsList;

    public static StartekDatabaseItems getInstance() {
        if (mInstance == null) {
            mInstance = new StartekDatabaseItems();
            mInstance.reset();
        }
        return mInstance;
    }

    private void reset() {
        databaseItemsList = null;
    }

    public ArrayList <StartekInfo> getDatabaseItemsList() {
        return databaseItemsList;
    }

    public void setDatabaseItemsList(ArrayList <StartekInfo> databaseItemsList) {
        this.databaseItemsList = databaseItemsList;
    }

}
