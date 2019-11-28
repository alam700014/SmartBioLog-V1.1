package com.android.fortunaattendancesystem.helper;


import android.text.TextUtils;
import android.util.Log;

import com.android.fortunaattendancesystem.constant.Constants;
import com.android.fortunaattendancesystem.model.ContractorInfo;
import com.android.fortunaattendancesystem.model.EmployeeTypeInfo;
import com.android.fortunaattendancesystem.model.EmployeeValidationBasicInfo;
import com.android.fortunaattendancesystem.model.EmployeeValidationFingerInfo;
import com.android.fortunaattendancesystem.model.PeriodInfo;
import com.android.fortunaattendancesystem.model.ProfessorStudentSubjectInfo;
import com.android.fortunaattendancesystem.model.ProfessorSubjectInfo;
import com.android.fortunaattendancesystem.model.SubjectInfo;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by fortuna on 25/6/16.
 */
public class Utility {

    private static String BloodGroups[] = {"null", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-", "", ""};
    private static String SecurityLevels[] = {"null", "A", "B", "C", "D", "E1", "E2", "E4", "E8", "", ""};
    private static String FingerIndex[] = {"null", "R-Thumb", "R-Index", "R-Middle", "R-Ring", "R-Little", "L-Thumb", "L-Index", "L-Middle", "L-Ring", "L-Little"};
    private static String Quality[] = {"null", "A", "B", "C", "D", "E", "", "", "", "", ""};
    private static String VerificationModes[] = {"CARD-ONLY", "CARD+PIN", "1:N", "", "FORCE ENROLL", "", "", "", "CARD+FINGER", "CARD+PIN+FINGER", ""};

    public static String paddEmpId(String strEmpId) {

        String paddEmpId = "";
        int x = 16 - strEmpId.length();
        for (int len = 0; len < x; len++) {
            paddEmpId += " ";
        }
        paddEmpId += strEmpId;
        return paddEmpId;
    }

    public static String paddCardId(String strCardId) {

        String paddCardId = "";
        int x = 8 - strCardId.length();
        for (int len = 0; len < x; len++) {
            paddCardId += "0";
        }
        paddCardId += strCardId;
        return paddCardId;
    }

    public static String paddEmpName(String strEmpName) {

        String paddEmpName = "";
        int x = 16 - strEmpName.length();
        for (int len = 0; len < x; len++) {
            paddEmpName += " ";
        }
        strEmpName += paddEmpName;
        return strEmpName;
    }


    public static int getBloodGrValByName(String strBloodGroup) {
        int value = -1;
        int len = BloodGroups.length;
        for (int i = 0; i < len; i++) {
            if (BloodGroups[i].equals(strBloodGroup)) {
                value = i;
                break;
            }
        }
        return value;
    }

    public static String getBloodGrValByNumber(String strBloodGroup) {
        int value = -1;
        String rtnBloodGroup = "";
        int intBloodGroup = Integer.parseInt(strBloodGroup);
        int len = BloodGroups.length;
        for (int bgLength = len; value < bgLength; bgLength--) {
            if (bgLength == intBloodGroup) {
                rtnBloodGroup = BloodGroups[intBloodGroup];
                break;
            }
        }
        return rtnBloodGroup;
    }

    public static int getSecurityLvlValByName(String strSecurityLevel) {

        int value = -1;
        int len = SecurityLevels.length;
        for (int i = 0; i < len; i++) {
            if (SecurityLevels[i].equals(strSecurityLevel)) {
                value = i;
                break;
            }
        }
        return value;
    }

    public static String setSecurityLvlValByNumber(String strSecurityLevel) {

        String value = "";
        int intSL = Integer.parseInt(strSecurityLevel);
        value = SecurityLevels[intSL].toString();
        return value;
    }

    public static int getFingerIndexValByName(String strFingerIndex) {

        int value = -1;
        int len = FingerIndex.length;
        for (int i = 0; i < len; i++) {
            if (FingerIndex[i].equals(strFingerIndex)) {
                value = i;
                break;
            }
        }
        return value;
    }

    public static String setFingerIndexValByHex(String strFingerIndex) {
        String value = "";
        int intFingerValue = Integer.parseInt(strFingerIndex, 16);
        value = FingerIndex[intFingerValue].toString();
        return value;
    }


    public static int getFingerQualityValByName(String strFingerQuality) {

        int value = -1;
        int len = Quality.length;
        for (int i = 0; i < len; i++) {
            if (Quality[i].equals(strFingerQuality)) {
                value = i;
                break;
            }
        }
        return value;
    }

    public static int getVerificationModeValByName(String strVerificationMode) {

        int value = -1;
        int len = VerificationModes.length;
        for (int i = 0; i < len; i++) {
            if (VerificationModes[i].equals(strVerificationMode)) {
                value = i;
                break;
            }
        }
        return value;
    }

    public static String setVerificationModeValByNumber(String strVerificationMode) {
        String value = "";
        int intVM = Integer.parseInt(strVerificationMode);
        value = VerificationModes[intVM].toString();
        return value;
    }


    public static String getVerificationModeByVal(String verificationMode) {

        String strVm = "";

        try {
            int val = Integer.parseInt(verificationMode);
            strVm = VerificationModes[val];
        } catch (NumberFormatException ne) {
        }
        return strVm;
    }

    public static String getSecurityLevelByVal(String securityLevel) {
        String strSecurityLevel = "";
        try {
            int val = Integer.parseInt(securityLevel);
            strSecurityLevel = SecurityLevels[val];
        } catch (NumberFormatException ne) {
        }
        return strSecurityLevel;
    }

    public static String getFingerIndexByVal(String fingerIndex) {

        String strFingerIndex = "";

        try {
            if (!fingerIndex.equals("A")) {
                strFingerIndex = FingerIndex[Integer.parseInt(fingerIndex)];
            } else {
                strFingerIndex = FingerIndex[10];
            }
        } catch (NumberFormatException ne) {
        }
        return strFingerIndex;
    }

    public static String getFingerQualityByVal(String fingerQuality) {

        String strFingerQuality = "";

        try {
            int val = Integer.parseInt(fingerQuality);
            strFingerQuality = Quality[val];
        } catch (NumberFormatException ne) {
        }

        return strFingerQuality;
    }

    public static boolean validateFingerType(String fingerType) {

        boolean isValid = false;
        switch (fingerType) {
            case "F1":
                isValid = true;
                break;
            case "F2":
                isValid = true;
                break;
            default:
                break;
        }
        return isValid;
    }

    public static boolean validateFingerIndex(String fingerIndex) {

        boolean isValid = false;

        switch (fingerIndex) {

            case "1":
                isValid = true;
                break;
            case "2":
                isValid = true;
                break;
            case "3":
                isValid = true;
                break;
            case "4":
                isValid = true;
                break;
            case "5":
                isValid = true;
                break;
            case "6":
                isValid = true;
                break;
            case "7":
                isValid = true;
                break;
            case "8":
                isValid = true;
                break;
            case "9":
                isValid = true;
                break;
            case "A":
                isValid = true;
                break;
            default:
                break;
        }

        return isValid;
    }


    public static boolean validateFingerQuality(String fingerQuality) {

        boolean isValid = false;

        switch (fingerQuality) {

            case "1":
                isValid = true;
                break;

            case "2":
                isValid = true;
                break;

            case "3":
                isValid = true;
                break;

            case "4":
                isValid = true;
                break;

            case "A":
                isValid = true;
                break;

            case "B":
                isValid = true;
                break;

            case "C":
                isValid = true;
                break;

            case "D":
                isValid = true;
                break;

            case "E":
                isValid = true;
                break;

            default:
                break;
        }

        return isValid;
    }

    public static boolean validateSecurityLevel(String securityLevel) {

        boolean isValid = false;
        switch (securityLevel) {

            case "1":
                isValid = true;
                break;
            case "2":
                isValid = true;
                break;
            case "3":
                isValid = true;
                break;
            case "4":
                isValid = true;
                break;
            case "5":
                isValid = true;
                break;
            default:
                break;
        }

        return isValid;
    }

    public static boolean validateVerificationMode(String verificationMode) {

        boolean isValid = false;

        switch (verificationMode) {
            case "0":
                isValid = true;
                break;
            case "1":
                isValid = true;
                break;
            case "2":
                isValid = true;
                break;
            case "4":
                isValid = true;
                break;
            case "8":
                isValid = true;
                break;
            case "9":
                isValid = true;
                break;
            case "00":
                isValid = true;
                break;
            case "01":
                isValid = true;
                break;
            case "02":
                isValid = true;
                break;
            case "04":
                isValid = true;
                break;
            case "08":
                isValid = true;
                break;
            case "09":
                isValid = true;
                break;
            default:
                break;
        }
        return isValid;
    }

    public static String getDeviceIPAddress() {
        try {
            for (Enumeration <NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface networkInterface = en.nextElement();
                for (Enumeration <InetAddress> enumIpAddr = networkInterface.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        String host = inetAddress.getHostAddress();
                        if (!TextUtils.isEmpty(host)) {
                            return host;
                        }
                    }
                }
            }
        } catch (Exception ex) {
        }
        return null;
    }

    public static String getInOutValue(String strInOutMode) {

        String strMode = "";

        switch (strInOutMode) {
            case "IN":
                strMode = "00";
                break;
            case "OUT":
                strMode = "01";
                break;
            case "NUL":
                strMode = "02";
                break;
        }
        return strMode;
    }

    public static String addCheckSum(String command) {
        int checksum = Checksum(command);
        if (Integer.toHexString(checksum).toUpperCase().length() != 2) {
            command = command + "0" + Integer.toHexString(checksum).toUpperCase() + "%";
        } else {
            command = command + Integer.toHexString(checksum).toUpperCase() + "%";
        }
        return command;
    }

    public static int Checksum(String msg) {
        char[] a2 = msg.toCharArray();
        char checksum = 0;
        for (int i = 0; i < a2.length; i++) {
            checksum ^= a2[i];
        }
        int c2 = checksum;
        return c2;
    }

    public static boolean validateEmailId(String strEmailId) {
        return Pattern.matches(Constants.EMAIL_REGEX, strEmailId);
    }

    public static boolean validateValidUptoDate(String strUserDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        Date dtCurrentDate = null;
        Date dtUserDate = null;
        try {
            dtUserDate = sdf.parse(strUserDate);
            dtCurrentDate = sdf.parse(sdf.format(new Date()));
            return (dtUserDate.compareTo(dtCurrentDate) > 0) || (dtUserDate.compareTo(dtCurrentDate) == 0);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean validateBirthDate(String strUserDate) {

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        Date dtCurrentDate = null;
        Date dtUserDate = null;

        try {
            dtUserDate = sdf.parse(strUserDate);
            dtCurrentDate = sdf.parse(sdf.format(new Date()));
            return (dtUserDate.compareTo(dtCurrentDate) < 0) || (dtUserDate.compareTo(dtCurrentDate) == 0);
        } catch (Exception e) {
            return false;
        }

    }

    public static boolean isTimeBetweenTwoTime(String argStartTime,
                                               String argEndTime, String argCurrentTime) throws ParseException {
        String reg = "^([0-1][0-9]|2[0-3])([0-5][0-9])$";
        //
        if (argStartTime.matches(reg) && argEndTime.matches(reg)
                && argCurrentTime.matches(reg)) {
            boolean valid = false;
            // Start Time
            java.util.Date startTime = new SimpleDateFormat("HHmm")
                    .parse(argStartTime);
            Calendar startCalendar = Calendar.getInstance();
            startCalendar.setTime(startTime);

            // Current Time
            java.util.Date currentTime = new SimpleDateFormat("HHmm")
                    .parse(argCurrentTime);
            Calendar currentCalendar = Calendar.getInstance();
            currentCalendar.setTime(currentTime);

            // End Time
            java.util.Date endTime = new SimpleDateFormat("HHmm")
                    .parse(argEndTime);
            Calendar endCalendar = Calendar.getInstance();
            endCalendar.setTime(endTime);

            if (currentTime.compareTo(endTime) < 0) {
                currentCalendar.add(Calendar.DATE, 1);
                currentTime = currentCalendar.getTime();
            }
            if (startTime.compareTo(endTime) < 0) {
                startCalendar.add(Calendar.DATE, 1);
                startTime = startCalendar.getTime();
            }
            if (currentTime.before(startTime)) {
                valid = false;

            } else {
                if (currentTime.after(endTime)) {
                    endCalendar.add(Calendar.DATE, 1);
                    endTime = endCalendar.getTime();
                }
                if (currentTime.before(endTime)) {
                    valid = true;
                } else {
                    valid = false;
                }
            }
            return valid;
        } else {
            throw new IllegalArgumentException("Not a valid time, expecting HH:MM:SS format");
        }

    }


    public static String asciiToHex(String asciiValue) {
        char[] chars = asciiValue.toCharArray();
        StringBuffer hex = new StringBuffer();
        for (int i = 0; i < chars.length; i++) {
            hex.append(Integer.toHexString((int) chars[i]));
        }
        return hex.toString();
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static String hexToAscii(String hexStr) {
        StringBuilder output = new StringBuilder("");
        for (int i = 0; i < hexStr.length(); i += 2) {
            String str = hexStr.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }
        return output.toString();
    }

    public static String removeNonAscii(String perInfo) {
        perInfo = perInfo.replaceAll("[^\\x00-\\x7F]", "0");
        perInfo = perInfo.replaceAll("[\\p{C}]", "0");
        perInfo = perInfo.replaceAll("[\\p{Cntrl}\\p{Cc}\\p{Cf}\\p{Co}\\p{Cn}]", "0");
        perInfo = perInfo.replaceAll("[\\r\\n\\t]", "0");
        return perInfo;
    }


    public static ArrayList <String> splitStringBySize(String str, int size) {
        ArrayList <String> split = new ArrayList <>();
        for (int i = 0; i < str.length() / size; i++) {
            split.add(str.substring(i * size, Math.min((i + 1) * size, str.length())));
        }
        return split;
    }

    public static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for (byte b : a)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }


    public static boolean isStrBinary(String input) {
        int len = input.length();

        // Find first occurrence of 1 in s[]
        int first = 0;
        for (int i = 0; i < len; i++) {
            if (input.charAt(i) == '1') {
                first = i;
                break;
            }
        }

        // Find last occurrence of 1 in s[]
        int last = 0;
        for (int i = len - 1; i >= 0; i--) {
            if (input.charAt(i) == '1') {
                last = i;
                break;
            }
        }

        // Check if there is any 0 in range
        for (int i = first; i <= last; i++)
            if (input.charAt(i) == '0')
                return false;

        return true;
    }


    public String TimeFormatChange(String PunchTime) {
        //String input = "23/12/2014 10:22:12 PM";
        //Format of the date defined in the input String
        String returnTime = "";

        DateFormat df = new SimpleDateFormat("hh:mm:ss aa");
        //Desired format: 24 hour format: Change the pattern as per the need
        DateFormat outputformat = new SimpleDateFormat("HHmmss");
        Date date = null;
        String output = null;
        try {
            //Converting the input String to Date
            date = df.parse(PunchTime);
            //Changing the format of date and storing it in String
            output = outputformat.format(date);
            //Displaying the date
            Log.d("TEST", "TimeFormatChange | Date :" + output);
        } catch (ParseException pe) {
            pe.printStackTrace();
        }

        return returnTime = output;

    }

    public static String DateFormatChange(String PunchDate) {
        //String input = "23/12/2014 10:22:12 PM";
        //Format of the date defined in the input String
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        DateFormat outputformat = new SimpleDateFormat("dd/MMM/yyyy");
        Date date = null;
        String returnDate = "";
        try {
            //Converting the input String to Date
            date = df.parse(PunchDate);
            //Changing the format of date and storing it in String
            returnDate = outputformat.format(date);
            //Displaying the date
        } catch (ParseException pe) {
            pe.printStackTrace();
        }
        return returnDate;
    }

    public static boolean isSubDataValid(SubjectInfo subInfo, String st) {
        String value = subInfo.getSubCode();
        if (value != null && value.trim().length() == 0) {
            return false;
        }
        value = subInfo.getSubName();
        if (value != null && value.trim().length() == 0) {
            return false;
        }
        if (st != null && st.trim().length() == 0) {
            return false;
        }
        return true;
    }

    public static boolean isProfSubDataValid(ProfessorSubjectInfo profSubInfo, String st) {
        String value = profSubInfo.getEmpId();
        if (value != null && value.trim().length() == 0) {
            return false;
        }
        value = profSubInfo.getSubCode();
        if (value != null && value.trim().length() == 0) {
            return false;
        }
        if (st != null && st.trim().length() == 0) {
            return false;
        }
        return true;
    }

    public static boolean isProfStuSubValid(ProfessorStudentSubjectInfo profStuSubInfo) {
        String value = profStuSubInfo.getProfessorEmpId();
        if (value != null && value.trim().length() == 0) {
            return false;
        }
        value = profStuSubInfo.getStudentEmpId();
        if (value != null && value.trim().length() == 0) {
            return false;
        }
        value = profStuSubInfo.getSubCode();
        if (value != null && value.trim().length() == 0) {
            return false;
        }
        return true;
    }

    public static boolean isEmpTypeValid(EmployeeTypeInfo etInfo) {
        String value = etInfo.getEmpType();
        if (value != null && value.trim().length() == 0) {
            return false;
        }
        return true;
    }

    public static boolean isContractorValid(ContractorInfo ctInfo) {
        String value = ctInfo.getContractorName();
        if (value != null && value.trim().length() == 0) {
            return false;
        }
        value = ctInfo.getCompanyName();
        if (value != null && value.trim().length() == 0) {
            return false;
        }
        return true;
    }

    public static boolean isPeriodValid(PeriodInfo pdInfo) {
        String value = pdInfo.getFromTime();
        if (value != null && value.trim().length() == 0) {
            return false;
        }
        value = pdInfo.getToTime();
        if (value != null && value.trim().length() == 0) {
            return false;
        }
        value = pdInfo.getSubType();
        if (value != null && value.trim().length() == 0) {
            return false;
        }
        value = pdInfo.getPeriod();
        if (value != null && value.trim().length() == 0) {
            return false;
        }
        return true;
    }

    public static int validateBasicInfo(EmployeeValidationBasicInfo info) {
        String value = info.getEmpId();
        if (value == null || (value != null && value.trim().length() == 0)) {
            return 1;
        }
        if (value != null && value.trim().length() > 16) {
            return 2;
        }
        value = info.getCardId();
        if (value == null || (value != null && value.trim().length() == 0)) {
            return 3;
        }
        if (value != null && value.trim().length() > 8) {
            return 4;
        }
        value = info.getEmpName();
        if (value == null || (value != null && value.trim().length() == 0)) {
            return 5;
        }
//        if (value != null && value.trim().length() > 16) {
//            return 6;
//        }
//
//        value = info.getEmpType();
//        if (value != null && value.trim().length() > 0) {
//            boolean isValid = false;
//            SQLiteCommunicator dbComm = new SQLiteCommunicator();
//            isValid = dbComm.isEtValid(value);
//            if (!isValid) {
//                return 7;
//            }
//        }
//        value = info.getDob();
//        if (value != null && value.trim().length() > 0) {
//            boolean isValid = false;
//            String format = "ddMMyyyy";
//            isValid = isValidFormat(format, value);
//            if (!isValid) {
//                return 8;
//            }
//        }
//        value = info.getDov();
//        if (value != null && value.trim().length() > 0) {
//            boolean isValid = false;
//            String format = "ddMMyyyy";
//            isValid = isValidFormat(format, value);
//            if (!isValid) {
//                return 9;
//            }
//        }
//        value = info.getIsLockOpen();
//        if (value != null && value.trim().length() > 0) {
//            boolean isValid = false;
//            String[] values = {"Y", "N"};
//            for (int i = 0; i < values.length; i++) {
//                if (values[i].equals(value)) {
//                    isValid = true;
//                    break;
//                }
//            }
//            if (!isValid) {
//                return 10;
//            }
//        }
//        value = info.getMn();
//        if (value != null && value.trim().length() > 0 && value.trim().length() != 10) {
//            return 11;
//        }
//        if (!TextUtils.isDigitsOnly(value.trim())) {
//            return 12;
//        }
//        value = info.getAid();
//        if (value != null && value.trim().length() > 0 && value.trim().length() != 12) {
//            return 13;
//        }
//        if (value != null && value.trim().length() > 0 && value.trim().length() == 12) {
//            if (!TextUtils.isDigitsOnly(value.trim())) {
//                return 14;
//            }
//            if (!VerhoeffAlgorithm.validateVerhoeff(value.trim())) {
//                return 15;
//            }
//        }
//        value = info.getEid();
//        if (value != null && value.trim().length() > 0 && !(validateEmailId(value.trim()))) {
//            return 16;
//        }
//        value = info.getIsBlackListed();
//        if (value != null && value.trim().length() > 0) {
//            boolean isValid = false;
//            String[] values = {"Y", "N"};
//            for (int i = 0; i < values.length; i++) {
//                if (values[i].equals(value)) {
//                    isValid = true;
//                    break;
//                }
//            }
//            if (!isValid) {
//                return 17;
//            }
//        }
//        value = info.getVm();
//        if (value != null && value.trim().length() > 0 && !validateVerificationMode(value.trim())) {
//            return 18;
//        }
        return 0;
    }

    public static int validateFingerInfo(EmployeeValidationFingerInfo employeeValidationFingerInfo) {
        String value = employeeValidationFingerInfo.getFt();
        if (value == null || (value != null && value.trim().length() == 0)) {
            return 19;
        }

        if (value != null && value.trim().length() > 0) {
            if (!validateFingerType(value.trim())) {
                return 20;
            }
        }
        value = employeeValidationFingerInfo.getSl();
        if (value == null || (value != null && value.trim().length() == 0)) {
            return 21;
        }
        if (value != null && value.trim().length() > 0) {
            if (!validateSecurityLevel(value.trim())) {
                return 22;
            }
        }
        value = employeeValidationFingerInfo.getFi();
        if (value == null || (value != null && value.trim().length() == 0)) {
            return 23;
        }
        if (value != null && value.trim().length() > 0) {
            if (!validateFingerIndex(value.trim())) {
                return 24;
            }
        }
        value = employeeValidationFingerInfo.getFq();
        if (value == null || (value != null && value.trim().length() == 0)) {
            return 25;
        }
        if (value != null && value.trim().length() > 0) {
            if (!validateFingerQuality(value.trim())) {
                return 26;
            }
        }
        value = employeeValidationFingerInfo.getFmd();
        if (value == null || (value != null && value.trim().length() == 0)) {
            return 27;
        }
        if (value != null) {
            int len=value.trim().length();
            if (len!=Constants.TEMPLATE_SIZE) {
                return 28;
            }
        }
        return 0;
    }


    public static boolean isValidInet4Address(String ip) {
        if (ip == null) {
            return false;
        }
        Pattern IPv4_PATTERN = Pattern.compile(Constants.IPADDRESS_PATTERN);
        Matcher matcher = IPv4_PATTERN.matcher(ip);
        return matcher.matches();
    }
    public static boolean validatePortNumber(String port){
        if(port==null){
            return false;
        }
        Pattern VALID_PORT_PATTERN = Pattern.compile(Constants.PORT_PATTERN, Pattern.CASE_INSENSITIVE);
        Matcher matcher = VALID_PORT_PATTERN.matcher(port);
        return matcher.matches();
    }



    public static boolean isValidFormat(String format, String value) {
        boolean isValid = false;
        Date date = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            date = sdf.parse(value);
            if (!value.equals(sdf.format(date))) {
                date = null;
            }
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        if (date == null) {
            isValid = false;
        } else {
            isValid = true;
        }
        return isValid;
    }

    public static String getErrorDescription(int error) {

        String desc = "";
        switch (error) {
            case 0:
                desc = "No Error";
                break;
            case 1:
                desc = "Employee Id is blank";
                break;
            case 2:
                desc = "Employee Id Length is greater than 16 characters";
                break;
            case 3:
                desc = "Card Id is blank";
                break;
            case 4:
                desc = "Card Id Length is greater than 8 characters";
                break;
            case 5:
                desc = "Employee Name is blank";
                break;
            case 6:
                desc = "Employee Name Length is greater than 16 characters";
                break;
            case 7:
                desc = "Invalid Employee Type";
                break;
            case 8:
                desc = "Invalid DOB format";
                break;
            case 9:
                desc = "Invalid DOV format";
                break;
            case 10:
                desc = "Invalid IsLockOpen Value";
                break;
            case 11:
                desc = "Invalid Mobile No Length";
                break;
            case 12:
                desc = "Mobile no should be numeric";
                break;
            case 13:
                desc = "Invalid Aadhaar Id Length";
                break;
            case 14:
                desc = "Aadhaar Id should be numeric";
                break;
            case 15:
                desc = "Invalid Aadhaar Id";
                break;
            case 16:
                desc = "Invalid Email Id";
                break;
            case 17:
                desc = "Invalid IsBlackListed Value";
                break;
            case 18:
                desc = "Invalid Verification Mode Value";
                break;
            case 19:
                desc = "Finger Type is blank";
                break;
            case 20:
                desc = "Invalid Finger Type value";
                break;
            case 21:
                desc = "Security Level is blank";
                break;
            case 22:
                desc = "Invalid Security Level value";
                break;
            case 23:
                desc = "Finger Index is blank";
                break;
            case 24:
                desc = "Invalid Finger Index value";
                break;
            case 25:
                desc = "Finger Quality is blank";
                break;
            case 26:
                desc = "Invalid Finger Quality value";
                break;
            case 27:
                desc = "Finger Template is blank";
                break;
            case 28:
                desc = "Invalid Finger Template Length";
                break;
            default:
                desc = "Unknown";
                break;
        }
        return desc;
    }


}
