package com.android.fortunaattendancesystem.mqtt;

import com.android.fortunaattendancesystem.helper.Utility;
import com.android.fortunaattendancesystem.model.EmployeeValidationBasicInfo;
import com.android.fortunaattendancesystem.model.EmployeeValidationFingerInfo;
import com.android.fortunaattendancesystem.model.MQTTHeaderInfo;
import com.android.fortunaattendancesystem.model.ProfessorStudentSubjectInfo;
import com.android.fortunaattendancesystem.model.ProfessorSubjectInfo;
import com.android.fortunaattendancesystem.model.SubjectInfo;
import com.android.fortunaattendancesystem.submodules.SQLiteCommunicator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by fortuna on 7/5/19.
 */

public class Validator {


    public static boolean validateHeaderData(JSONObject json, MQTTHeaderInfo headerInfo) {

        boolean isError = false;
        int error = 0;
        JSONArray he = new JSONArray();
        String value = headerInfo.getPid();
        if (value.equals("null") || value.trim().length() == 0) {
            he.put(-1);
            error++;
        }
        value = headerInfo.getCt();
        if (value.equals("null") || value.trim().length() == 0) {
            he.put(-3);
            error++;
        }
        value = headerInfo.getCst();
        if (value.equals("null") || value.trim().length() == 0) {
            he.put(-5);
            error++;
        }
        value = headerInfo.getDt();
        if (value.equals("null") || value.trim().length() == 0) {
            he.put(-7);
            error++;
        }
        value = headerInfo.getCpuid();
        if (value.equals("null") || value.trim().length() == 0) {
            he.put(-9);
            error++;
        }
        value = headerInfo.getImei();
        if (value.equals("null") || value.trim().length() == 0) {
            he.put(-11);
            error++;
        }
        value = headerInfo.getCoid();
        if (value.equals("null") || value.trim().length() == 0) {
            he.put(-13);
            error++;
        }
        value = headerInfo.getTa();
        if (value.equals("null") || value.trim().length() == 0) {
            he.put(-15);
            error++;
        }
        value = headerInfo.getPc();
        if (value.equals("null") || value.trim().length() == 0) {
            he.put(-48);
            error++;
        }
        try {
            json.put("HE", he);
            if (error > 0) {
                json.put("HES", true);
                isError = true;
            } else {
                json.put("HES", false);
                isError = false;
            }
        } catch (JSONException e) {
        }
        return isError;
    }

    public static boolean validateSubjectData(JSONObject jr, String pid, ArrayList <SubjectInfo> sl) {

        boolean isError = false;
        int error = 0;
        JSONArray be = new JSONArray();
        try {
            jr.put("PID", pid);
            int size = sl.size();
            for (int count = 0; count < size; count++) {
                JSONArray ec = new JSONArray();
                JSONObject bej = new JSONObject();
                SubjectInfo subInfo = sl.get(count);
                String jid = subInfo.getJobId();
                String sc = subInfo.getSubCode();
                String sn = subInfo.getSubName();
                String[] st = subInfo.getSubType();
                bej.put("JID", jid);
                if (sc.equals("null") || sc.trim().length() == 0) {
                    ec.put(-18);
                    error++;
                }
                if (sn.equals("null") || sn.trim().length() == 0) {
                    ec.put(-19);
                    error++;
                }
                if (st != null && st.length == 0) {
                    ec.put(-20);
                    error++;
                }
                bej.put("EC", ec);
                be.put(bej);
            }
            jr.put("BE", be);
            if (error > 0) {
                jr.put("BES", true);
                isError = true;
            } else {
                jr.put("BES", false);
                isError = false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return isError;
    }


    public static boolean validateSubjectData(JSONObject jr, JSONArray be, SubjectInfo subInfo) {

        boolean isError = false;
        int error = 0;

        try {
            JSONArray ec = new JSONArray();
            JSONObject bej = new JSONObject();
            String jid = subInfo.getJobId();
            String sc = subInfo.getSubCode();
            String sn = subInfo.getSubName();
            String[] st = subInfo.getSubType();
            bej.put("JID", jid);
            if (sc.equals("null") || sc.trim().length() == 0) {
                ec.put(-18);
                error++;
            }
            if (sn.equals("null") || sn.trim().length() == 0) {
                ec.put(-19);
                error++;
            }
            if (st != null && st.length == 0) {
                ec.put(-20);
                error++;
            }
            bej.put("EC", ec);
            be.put(bej);

            jr.put("BE", be);
            if (error > 0) {
                isError = true;
            } else {
                isError = false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return isError;
    }

    public static boolean validateProfSubData(JSONObject jr, String pid, ArrayList <ProfessorSubjectInfo> psl) {
        boolean isError = false;
        int error = 0;
        JSONArray be = new JSONArray();
        try {
            jr.put("PID", pid);
            int size = psl.size();
            for (int count = 0; count < size; count++) {
                JSONArray ec = new JSONArray();
                JSONObject bej = new JSONObject();
                ProfessorSubjectInfo profSubInfo = psl.get(count);
                String jid = profSubInfo.getJid();
                String empId = profSubInfo.getEmpId();
                String subCode = profSubInfo.getSubCode();
                String[] st = profSubInfo.getSubTypes();
                bej.put("JID", jid);
                if (empId.equals("null") || empId.trim().length() == 0) {
                    ec.put(-21);
                    error++;
                }
                if (subCode.equals("null") || subCode.trim().length() == 0) {
                    ec.put(-18);
                    error++;
                }
                if (st != null && st.length == 0) {
                    ec.put(-20);
                    error++;
                }
                bej.put("EC", ec);
                be.put(bej);
            }
            jr.put("BE", be);
            if (error > 0) {
                jr.put("BES", true);
                isError = true;
            } else {
                jr.put("BES", false);
                isError = false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return isError;

    }


    public static boolean validateProfSubData(JSONObject jr, JSONArray be, ProfessorSubjectInfo profSubInfo) {
        boolean isError = false;
        int error = 0;

        try {
            JSONArray ec = new JSONArray();
            JSONObject bej = new JSONObject();

            String jid = profSubInfo.getJid();
            String empId = profSubInfo.getEmpId();
            String subCode = profSubInfo.getSubCode();
            String[] st = profSubInfo.getSubTypes();
            bej.put("JID", jid);
            if (empId.equals("null") || empId.trim().length() == 0) {
                ec.put(-21);
                error++;
            }
            if (subCode.equals("null") || subCode.trim().length() == 0) {
                ec.put(-18);
                error++;
            }
            if (st != null && st.length == 0) {
                ec.put(-20);
                error++;
            }
            bej.put("EC", ec);
            be.put(bej);
            jr.put("BE", be);
            if (error > 0) {
                isError = true;
            } else {
                isError = false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return isError;

    }

    public static boolean validateProfStudentSubData(JSONObject jr, String pid, ArrayList <ProfessorStudentSubjectInfo> pssl) {

        boolean isError = false;
        int error = 0;
        JSONArray be = new JSONArray();
        try {
            jr.put("PID", pid);
            int size = pssl.size();
            for (int count = 0; count < size; count++) {
                JSONArray ec = new JSONArray();
                JSONObject bej = new JSONObject();
                ProfessorStudentSubjectInfo profStudentSubInfo = pssl.get(count);
                String jid = profStudentSubInfo.getJobId();
                String studentEID = profStudentSubInfo.getStudentEmpId();
                String profEID = profStudentSubInfo.getProfessorEmpId();
                String sc = profStudentSubInfo.getSubCode();
                bej.put("JID", jid);
                if (studentEID.equals("null") || studentEID.trim().length() == 0) {
                    ec.put(-23);
                    error++;
                }
                if (profEID.equals("null") || profEID.trim().length() == 0) {
                    ec.put(-25);
                    error++;
                }
                if (sc.equals("null") || sc.trim().length() == 0) {
                    ec.put(-18);
                    error++;
                }
                bej.put("EC", ec);
                be.put(bej);
            }
            jr.put("BE", be);
            if (error > 0) {
                jr.put("BES", true);
                isError = true;
            } else {
                jr.put("BES", false);
                isError = false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return isError;
    }


    public static boolean validateProfStudentSubData(JSONObject jr, JSONArray be, ProfessorStudentSubjectInfo profStudentSubInfo) {

        boolean isError = false;
        int error = 0;

        try {
            JSONArray ec = new JSONArray();
            JSONObject bej = new JSONObject();
            String jid = profStudentSubInfo.getJobId();
            String studentEID = profStudentSubInfo.getStudentEmpId();
            String profEID = profStudentSubInfo.getProfessorEmpId();
            String sc = profStudentSubInfo.getSubCode();
            bej.put("JID", jid);
            if (studentEID.equals("null") || studentEID.trim().length() == 0) {
                ec.put(-23);
                error++;
            }
            if (profEID.equals("null") || profEID.trim().length() == 0) {
                ec.put(-25);
                error++;
            }
            if (sc.equals("null") || sc.trim().length() == 0) {
                ec.put(-18);
                error++;
            }
            bej.put("EC", ec);
            be.put(bej);

            jr.put("BE", be);
            if (error > 0) {
                isError = true;
            } else {
                isError = false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return isError;
    }

    public static boolean validateEmpValData(JSONObject jr, String pid, ArrayList <EmployeeValidationBasicInfo> empInfoList) {

        boolean isError = false;
        int error = 0;
        JSONArray be = new JSONArray();
        try {
            jr.put("PID", pid);
            int size = empInfoList.size();
            for (int count = 0; count < size; count++) {
                JSONArray ec = new JSONArray();
                JSONObject bej = new JSONObject();

                EmployeeValidationBasicInfo empValInfo = empInfoList.get(count);
                String jid = empValInfo.getJobId();
                String eid = empValInfo.getEmpId();
                String cid = empValInfo.getCardId();
                String en = empValInfo.getEmpName();
                String vm = empValInfo.getVm();

                bej.put("JID", jid);
                if (eid.equals("null") || eid.trim().length() == 0) {
                    ec.put(-21);
                    error++;
                }

                if (!eid.equals("null") && eid.trim().length() > 0) {
                    SQLiteCommunicator dbComm = new SQLiteCommunicator();
                    int autoId = dbComm.getAutoIdByEmpId(eid);
                    if (autoId != -1) {
                        ec.put(-29);
                        error++;
                    }
                }

                if (cid.equals("null") || cid.trim().length() == 0) {
                    ec.put(-30);
                    error++;
                }

                if (!cid.equals("null") && cid.trim().length() > 0) {
                    SQLiteCommunicator dbComm = new SQLiteCommunicator();
                    boolean status = dbComm.isCardDataAvailableInDatabase(cid);
                    if (status) {
                        ec.put(-32);
                        error++;
                    }
                }

                if (en.equals("null") || en.trim().length() == 0) {
                    ec.put(-33);
                    error++;
                }

                boolean isValid = false;
                if (!vm.equals("null") && vm.trim().length() > 0) {
                    isValid = Utility.validateVerificationMode(vm);
                    if (!isValid) {
                        ec.put(-46);
                        error++;
                    }
                } else {
                    ec.put(-46);
                    error++;
                }

                ArrayList <EmployeeValidationFingerInfo> fInfo = empValInfo.getfInfoList();
                if (fInfo != null) {
                    int size1 = fInfo.size();
                    if (size1 > 0) {
                        for (int count1 = 0; count1 < size1; count1++) {
                            EmployeeValidationFingerInfo fingerInfo = fInfo.get(count1);
                            if (fingerInfo != null) {

                                String ft = fingerInfo.getFt();
                                String sl = fingerInfo.getSl();
                                String fi = fingerInfo.getFi();
                                String fq = fingerInfo.getFq();

                                String ts = fingerInfo.getTs();
                                String fmid = fingerInfo.getFmd();
                                String fid = fingerInfo.getFid();

                                if (!ft.equals("null") && ft.trim().length() > 0) {
                                    isValid = Utility.validateFingerType(ft);
                                    if (!isValid) {
                                        ec.put(-40);
                                        error++;
                                    }
                                } else {
                                    ec.put(-40);
                                    error++;
                                }

                                if (!sl.equals("null") && sl.trim().length() > 0) {
                                    isValid = Utility.validateSecurityLevel(sl);
                                    if (!isValid) {
                                        ec.put(-41);
                                        error++;
                                    }
                                } else {
                                    ec.put(-41);
                                    error++;
                                }

                                if (!fi.equals("null") && fi.trim().length() > 0) {
                                    isValid = Utility.validateFingerIndex(fi);
                                    if (!isValid) {
                                        ec.put(-42);
                                        error++;
                                    }
                                } else {
                                    ec.put(-42);
                                    error++;
                                }

                                if (!fq.equals("null") && fq.trim().length() > 0) {
                                    isValid = Utility.validateFingerQuality(fq);
                                    if (!isValid) {
                                        ec.put(-43);
                                        error++;
                                    }
                                } else {
                                    ec.put(-43);
                                    error++;
                                }

                                if (ts.equals("null") || ts.trim().length() == 0) {
                                    ec.put(-44);
                                    error++;
                                }

                                if (fmid.equals("null") || fmid.trim().length() == 0) {
                                    ec.put(-45);
                                    error++;
                                }

                                // if(fid.equals("null") || fid.trim().length()==0){
                                //     ec.put(-45);
                                // }
                            }
                        }
                    }
                }
                bej.put("EC", ec);
                be.put(bej);
            }
            jr.put("BE", be);
            if (error > 0) {
                jr.put("BES", true);
                isError = true;
            } else {
                jr.put("BES", false);
                isError = false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return isError;
    }

    public static boolean validateEmpValData(JSONObject jr, JSONArray be, EmployeeValidationBasicInfo empValInfo) {

        boolean isError = false;
        int error = 0;

        JSONArray ec = new JSONArray();
        JSONObject bej = new JSONObject();

        try {

            String jid = empValInfo.getJobId();
            String eid = empValInfo.getEmpId();
            String cid = empValInfo.getCardId();
            String en = empValInfo.getEmpName();
            String vm = empValInfo.getVm();

            bej.put("JID", jid);
            if (eid.equals("null") || eid.trim().length() == 0) {
                ec.put(-21);
                error++;
            }

            if (!eid.equals("null") && eid.trim().length() > 0) {
                SQLiteCommunicator dbComm = new SQLiteCommunicator();
                int autoId = dbComm.getAutoIdByEmpId(eid);
                if (autoId != -1) {
                    ec.put(-29);
                    error++;
                }
            }

            if (cid.equals("null") || cid.trim().length() == 0) {
                ec.put(-30);
                error++;
            }

            if (!cid.equals("null") && cid.trim().length() > 0) {
                SQLiteCommunicator dbComm = new SQLiteCommunicator();
                boolean status = dbComm.isCardDataAvailableInDatabase(cid);
                if (status) {
                    ec.put(-32);
                    error++;
                }
            }

            if (en.equals("null") || en.trim().length() == 0) {
                ec.put(-33);
                error++;
            }

            boolean isValid = false;
            if (!vm.equals("null") && vm.trim().length() > 0) {
                isValid = Utility.validateVerificationMode(vm);
                if (!isValid) {
                    ec.put(-46);
                    error++;
                }
            } else {
                ec.put(-46);
                error++;
            }

            ArrayList <EmployeeValidationFingerInfo> fInfo = empValInfo.getfInfoList();
            if (fInfo != null) {
                int size1 = fInfo.size();
                if (size1 > 0) {
                    for (int count1 = 0; count1 < size1; count1++) {
                        EmployeeValidationFingerInfo fingerInfo = fInfo.get(count1);
                        if (fingerInfo != null) {

                            String ft = fingerInfo.getFt();
                            String sl = fingerInfo.getSl();
                            String fi = fingerInfo.getFi();
                            String fq = fingerInfo.getFq();

                            String ts = fingerInfo.getTs();
                            String fmid = fingerInfo.getFmd();
                            String fid = fingerInfo.getFid();

                            if (!ft.equals("null") && ft.trim().length() > 0) {
                                isValid = Utility.validateFingerType(ft);
                                if (!isValid) {
                                    ec.put(-40);
                                    error++;
                                }
                            } else {
                                ec.put(-40);
                                error++;
                            }

                            if (!sl.equals("null") && sl.trim().length() > 0) {
                                isValid = Utility.validateSecurityLevel(sl);
                                if (!isValid) {
                                    ec.put(-41);
                                    error++;
                                }
                            } else {
                                ec.put(-41);
                                error++;
                            }

                            if (!fi.equals("null") && fi.trim().length() > 0) {
                                isValid = Utility.validateFingerIndex(fi);
                                if (!isValid) {
                                    ec.put(-42);
                                    error++;
                                }
                            } else {
                                ec.put(-42);
                                error++;
                            }

                            if (!fq.equals("null") && fq.trim().length() > 0) {
                                isValid = Utility.validateFingerQuality(fq);
                                if (!isValid) {
                                    ec.put(-43);
                                    error++;
                                }
                            } else {
                                ec.put(-43);
                                error++;
                            }

                            if (ts.equals("null") || ts.trim().length() == 0) {
                                ec.put(-44);
                                error++;
                            }

                            if (fmid.equals("null") || fmid.trim().length() == 0) {
                                ec.put(-45);
                                error++;
                            }

                            // if(fid.equals("null") || fid.trim().length()==0){
                            //     ec.put(-45);
                            // }
                        }
                    }
                }
            }
            bej.put("EC", ec);
            be.put(bej);
            jr.put("BE", be);
            if (error > 0) {
                isError = true;
            } else {
                isError = false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return isError;
    }

    public static boolean validateEmpValRemoteData(JSONObject jr, String pid, ArrayList <EmployeeValidationBasicInfo> empInfoList) {

        boolean isError = false;
        int error = 0;
        JSONArray be = new JSONArray();
        try {
            jr.put("PID", pid);
            int size = empInfoList.size();
            for (int count = 0; count < size; count++) {
                JSONArray ec = new JSONArray();
                JSONObject bej = new JSONObject();
                EmployeeValidationBasicInfo empValInfo = empInfoList.get(count);
                String jid = empValInfo.getJobId();
                String eid = empValInfo.getEmpId();
                String cid = empValInfo.getCardId();
                String en = empValInfo.getEmpName();
                String vm = empValInfo.getVm();
                bej.put("JID", jid);
                if (eid.equals("null") || eid.trim().length() == 0) {
                    ec.put(-21);
                    error++;
                }

                if (cid.equals("null") || cid.trim().length() == 0) {
                    ec.put(-30);
                    error++;
                }

                if (en.equals("null") || en.trim().length() == 0) {
                    ec.put(-33);
                    error++;
                }

                boolean isValid = false;
                if (!vm.equals("null") && vm.trim().length() > 0) {
                    isValid = Utility.validateVerificationMode(vm);
                    if (!isValid) {
                        ec.put(-46);
                        error++;
                    }
                } else {
                    ec.put(-46);
                    error++;
                }

                ArrayList <EmployeeValidationFingerInfo> fInfo = empValInfo.getfInfoList();
                if (fInfo != null) {
                    int size1 = fInfo.size();
                    if (size1 > 0) {
                        for (int count1 = 0; count1 < size1; count1++) {
                            EmployeeValidationFingerInfo fingerInfo = fInfo.get(count1);
                            if (fingerInfo != null) {

                                String ft = fingerInfo.getFt();
                                String sl = fingerInfo.getSl();
                                String fi = fingerInfo.getFi();
                                String fq = fingerInfo.getFq();

                                if (!ft.equals("null") && ft.trim().length() > 0) {
                                    isValid = Utility.validateFingerType(ft);
                                    if (!isValid) {
                                        ec.put(-40);
                                        error++;
                                    }
                                } else {
                                    ec.put(-40);
                                    error++;
                                }

                                if (!sl.equals("null") && sl.trim().length() > 0) {
                                    isValid = Utility.validateSecurityLevel(sl);
                                    if (!isValid) {
                                        ec.put(-41);
                                        error++;
                                    }
                                } else {
                                    ec.put(-41);
                                    error++;
                                }

                                if (!fi.equals("null") && fi.trim().length() > 0) {
                                    isValid = Utility.validateFingerIndex(fi);
                                    if (!isValid) {
                                        ec.put(-42);
                                        error++;
                                    }
                                } else {
                                    ec.put(-42);
                                    error++;
                                }

                                if (!fq.equals("null") && fq.trim().length() > 0) {
                                    isValid = Utility.validateFingerQuality(fq);
                                    if (!isValid) {
                                        ec.put(-43);
                                        error++;
                                    }
                                } else {
                                    ec.put(-43);
                                    error++;
                                }
                            }
                        }
                    }
                }
                bej.put("EC", ec);
                be.put(bej);
            }
            jr.put("BE", be);
            if (error > 0) {
                jr.put("BES", true);
                isError = true;
            } else {
                jr.put("BES", false);
                isError = false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return isError;
    }

    public static boolean validateEmpValRemoteData(JSONObject jr, JSONArray be, EmployeeValidationBasicInfo empValInfo) {
        int error = 0;
        boolean isError = false;
        JSONArray ec = new JSONArray();
        JSONObject bej = new JSONObject();
        try {
            String jid = empValInfo.getJobId();
            String eid = empValInfo.getEmpId();
            String cid = empValInfo.getCardId();
            String en = empValInfo.getEmpName();
            String vm = empValInfo.getVm();
            bej.put("JID", jid);
            if (eid.equals("null") || eid.trim().length() == 0) {
                ec.put(-21);
                error++;
            }

            if (cid.equals("null") || cid.trim().length() == 0) {
                ec.put(-30);
                error++;
            }

            if (en.equals("null") || en.trim().length() == 0) {
                ec.put(-33);
                error++;
            }

            boolean isValid = false;
            if (!vm.equals("null") && vm.trim().length() > 0) {
                isValid = Utility.validateVerificationMode(vm);
                if (!isValid) {
                    ec.put(-46);
                    error++;
                }
            } else {
                ec.put(-46);
                error++;
            }

            ArrayList <EmployeeValidationFingerInfo> fInfo = empValInfo.getfInfoList();
            if (fInfo != null) {
                int size1 = fInfo.size();
                if (size1 > 0) {
                    for (int count1 = 0; count1 < size1; count1++) {
                        EmployeeValidationFingerInfo fingerInfo = fInfo.get(count1);
                        if (fingerInfo != null) {

                            String ft = fingerInfo.getFt();
                            String sl = fingerInfo.getSl();
                            String fi = fingerInfo.getFi();
                            String fq = fingerInfo.getFq();

                            if (!ft.equals("null") && ft.trim().length() > 0) {
                                isValid = Utility.validateFingerType(ft);
                                if (!isValid) {
                                    ec.put(-40);
                                    error++;
                                }
                            } else {
                                ec.put(-40);
                                error++;
                            }

                            if (!sl.equals("null") && sl.trim().length() > 0) {
                                isValid = Utility.validateSecurityLevel(sl);
                                if (!isValid) {
                                    ec.put(-41);
                                    error++;
                                }
                            } else {
                                ec.put(-41);
                                error++;
                            }

                            if (!fi.equals("null") && fi.trim().length() > 0) {
                                isValid = Utility.validateFingerIndex(fi);
                                if (!isValid) {
                                    ec.put(-42);
                                    error++;
                                }
                            } else {
                                ec.put(-42);
                                error++;
                            }

                            if (!fq.equals("null") && fq.trim().length() > 0) {
                                isValid = Utility.validateFingerQuality(fq);
                                if (!isValid) {
                                    ec.put(-43);
                                    error++;
                                }
                            } else {
                                ec.put(-43);
                                error++;
                            }
                        }
                    }
                }
            }
            bej.put("EC", ec);
            be.put(bej);
            jr.put("BE", be);
            if (error > 0) {
                isError = true;
            } else {
                isError = false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return isError;
    }
}
