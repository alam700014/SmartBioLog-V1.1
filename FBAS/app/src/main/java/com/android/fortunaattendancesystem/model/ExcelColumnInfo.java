package com.android.fortunaattendancesystem.model;

/**
 * Created by fortuna on 5/5/17.
 */
public class ExcelColumnInfo {

    int columnNo;
    int rowNo;
    String strCellData;
    String strStatus;

    public String getStrCellData() {
        return strCellData;
    }

    public void setStrCellData(String strCellData) {
        this.strCellData = strCellData;
    }

    public int getColumnNo() {
        return columnNo;
    }

    public void setColumnNo(int columnNo) {
        this.columnNo = columnNo;
    }

    public int getRowNo() {
        return rowNo;
    }

    public void setRowNo(int rowNo) {
        this.rowNo = rowNo;
    }

    public String getStrStatus() {
        return strStatus;
    }

    public void setStrStatus(String strStatus) {
        this.strStatus = strStatus;
    }


}
