package com.android.fortunaattendancesystem.model;

/**
 * Created by fortuna on 29/3/19.
 */

public class SubjectInfo {

    private String subCode;
    private String subName;
    private String[] subType;
    private String jobId;

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getSubCode() {
        return subCode;
    }

    public void setSubCode(String subCode) {
        this.subCode = subCode;
    }

    public String getSubName() {
        return subName;
    }

    public void setSubName(String subName) {
        this.subName = subName;
    }

    public String[] getSubType() {
        return subType;
    }

    public void setSubType(String[] subType) {
        this.subType = subType;
    }
}
