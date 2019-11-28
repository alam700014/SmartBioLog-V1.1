package com.android.fortunaattendancesystem.model;

/**
 * Created by fortuna on 29/3/19.
 */

public class ProfessorSubjectInfo {

    private String jid;
    private String empId;
    private String subCode;
    private String[] subTypes;

    public String getJid() {
        return jid;
    }

    public void setJid(String jid) {
        this.jid = jid;
    }

    public String getEmpId() {
        return empId;
    }

    public void setEmpId(String empId) {
        this.empId = empId;
    }

    public String getSubCode() {
        return subCode;
    }

    public void setSubCode(String subCode) {
        this.subCode = subCode;
    }

    public String[] getSubTypes() {
        return subTypes;
    }

    public void setSubTypes(String[] subTypes) {
        this.subTypes = subTypes;
    }
}
