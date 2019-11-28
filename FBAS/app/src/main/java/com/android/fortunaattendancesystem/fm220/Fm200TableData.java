package com.android.fortunaattendancesystem.fm220;

import android.database.Cursor;
import android.provider.BaseColumns;
import android.util.Base64;


/**
 * Created by suman-dhara on 16/10/17.
 */

public class Fm200TableData {
    private long tempalte_id;
    private long employee_id;
    private String template_b64;
    private byte[] template_byte_arr;
    private String template_type;
    private int finger_index;


    public long getTempalteId() {
        return tempalte_id;
    }

    public long getEmployeeId() {
        return employee_id;
    }

    public String getTemplateB64() {
        return template_b64;
    }

    public byte[] getTemplateByteArr() {
        return template_byte_arr;
    }

    public String getStemplateType() {
        return template_type;
    }

    public int getFingerIndex() {
        return finger_index;
    }

    public Fm200TableData(Cursor matched_cursor) {
        this.tempalte_id = matched_cursor.getLong(matched_cursor.getColumnIndex(Fm200TableInfo._ID));
        this.employee_id = matched_cursor.getLong(matched_cursor.getColumnIndex(Fm200TableInfo.EMPLOYEE_ID));
        this.template_b64 = matched_cursor.getString(matched_cursor.getColumnIndex(Fm200TableInfo.TEMPLATE_B64));
        this.template_byte_arr = Base64.decode(this.template_b64, Base64.DEFAULT);
        this.finger_index = matched_cursor.getInt(matched_cursor.getColumnIndex(Fm200TableInfo.FINGER_INDEX));
        this.template_type = matched_cursor.getColumnName(matched_cursor.getColumnIndex(Fm200TableInfo.TEMPLATE_TYPE));
    }

    public abstract static class Fm200TableInfo implements BaseColumns {
        public static final String EMPLOYEE_ID = "employee_id";
        public static final String TEMPLATE_B64 = "template_b64";
        public static final String TEMPLATE_TYPE = "template_type"; // ISO, NONISO
        public static final String FINGER_INDEX = "finger_index";
        public static final String TABLE_NAME = "fm200_template";
    }
}
