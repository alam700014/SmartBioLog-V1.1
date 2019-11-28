package com.android.fortunaattendancesystem.model;

/**
 * Created by fortuna on 30/3/19.
 */

public class ProfessorStudentSubjectInfo {

    private String studentEmpId;
    private String professorEmpId;
    private String subCode;
    private String jobId;

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getStudentEmpId() {
        return studentEmpId;
    }

    public void setStudentEmpId(String studentEmpId) {
        this.studentEmpId = studentEmpId;
    }

    public String getProfessorEmpId() {
        return professorEmpId;
    }

    public void setProfessorEmpId(String professorEmpId) {
        this.professorEmpId = professorEmpId;
    }

    public String getSubCode() {
        return subCode;
    }

    public void setSubCode(String subCode) {
        this.subCode = subCode;
    }
}
