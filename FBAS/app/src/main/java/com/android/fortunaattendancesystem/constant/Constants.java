package com.android.fortunaattendancesystem.constant;

import android.text.InputFilter;
import android.text.Spanned;

/**
 * Created by fortuna on 12/9/18.
 */


public class Constants {

    public final static boolean isTab = false;

    public final static String APP_VERSION="1.1";

    //=============================== MQTT Broker Credentials ===============================//

    //public static final String MQTT_BROKER_URL = "tcp://192.168.1.15:1883";
    public static final String MQTT_BROKER_URL = "tcp://192.168.0.35:1883";

    //public static final String MQTT_BROKER_URL ="tcp://192.168.0.94:1884";

    // public static final String MQTT_BROKER_URL ="tcp://182.72.247.210:1884";
    public static final String OU = "FORTUNA";
    public static final String DEV_TYPE = "TAB";
    public static final String DEV_COMP_NAME = "WISHTEL";
    public static final String DEV_REG_TOPIC = "DeviceRegistration/CTS";

    public static final String COMMON_TOPIC = "ATTENDANCE/SAMSUNG/VIVO/V6/STD/";
    public static final String GET_TEMPLATE_TOPIC = "ATTENDANCE/SAMSUNG/VIVO/V6/DTS/getTemplate";


    public static final String COMMON_PUBLISH_TOPIC = "ATTENDANCE/SAMSUNG/VIVO/V6/DTS";
    public static final String ERROR_PUBLISH_TOPIC = "ATTENDANCE/SAMSUNG/VIVO/V6/DTS/Error";


    public static final int SPLASH_TIME_OUT = 5000;


//    public static final String SUBJECT_DOWNLOAD_COMMAND="83A1";
//    public static final String PROFESSOR_SUBJECT_DOWNLOAD_COMMAND="83A2";
//    public static final String PROFESSOR_STUDENT_SUBJECT_DOWNLOAD_COMMAND="83A3";
//    public static final String EMPLOYEE_TYPE_DOWNLOAD_COMMAND="1720";
//    public static final String CONTRACTOR_DOWNLOAD_COMMAND="1730";
//    public static final String PERIOD_DOWNLOAD_COMMAND="83A4";
//    public static final String VALIDATION_TEMPLATE_DOWNLOAD="2100";


    public static final String SUBJECT_DOWNLOAD_COMMAND = "8310";
    public static final String PROFESSOR_SUBJECT_DOWNLOAD_COMMAND = "8320";
    public static final String PROFESSOR_STUDENT_SUBJECT_DOWNLOAD_COMMAND = "8330";
    public static final String EMPLOYEE_TYPE_DOWNLOAD_COMMAND = "1720";
    public static final String CONTRACTOR_DOWNLOAD_COMMAND = "1730";
    public static final String PERIOD_DOWNLOAD_COMMAND = "83A4";
    public static final String VALIDATION_TEMPLATE_DOWNLOAD = "2100";
    public static final String VALIDATION_TEMPLATE_UPLOAD = "2201";
    public static final String AUTO_TEMPLATE_UPLOAD = "2202";//Code created by ankit
    public static final String REMOTE_ENROLL = "7800";
    public static final String DELETE_EMPLOYEE = "2101";

    public static final String DASHBOARD_DATA = "0800";//Code created by ankit
    public static final String ATTENDANCE_DATA_UPLOAD = "0100";//Code created by ankit


    public static final String ANY_PROFESSOR = "00000000";


    public static final String COMMAND_SUB_TYPE_1 = "Add";
    public static final String COMMAND_SUB_TYPE_2 = "Sub";
    public static final String COMMAND_SUB_TYPE_3 = "Mod";


    public static final String DEV_REG_STATUS_TOPIC = "DeviceRegistration/STC";
    public static final String EMP_VAL_DOWNLOAD_TOPIC = "EmployeeValidationDownload/";
    public static final String ATTENDANCE_RECV_TOPIC = "Attendance/";
    public static final String TEMPLATE_RECV_TOPIC = "TemplateUpload/";
    public static final String EMP_TEMP_DOWNLOAD_TOPIC = "TemplateDownload/";


    public static final String MQTT_TABLE = "mqttm";
    public static final String MQTT_DEV_REG_STATUS_TABLE = "mqttDeviceRegStatus";


    public static final String PUBLISH_TOPIC = "linux";
    public static final String CLIENT_ID = "androidkt";


    public static final int DATA_TRANSFER_MODE_HTTP = 0;
    public static final int DATA_TRANSFER_PROTOCAL_MQTT = 1;


    //=============================== Headers For Settings Ini File  ===============================//

    public final static String ACTION_USB_PERMISSION = "com.android.fortunaattendancesystem.USB_PERMISSION";
    // public static final String PROJECT_FILES_PATH = "/mnt/sdcard/project_data";
    //public static final String DB_PATH = "/mnt/sdcard/project_data/Android.db";

    public static final String DB_PATH = "/data/tmp/Android.db";

    //=============================== FBAS.ini file Settings Credentials ========================================//

    public static final String[] INI_HEADERS = {"Installation Device Type", "Smart Reader", "Finger Reader", "Finger Enrollment Mode", "Attendance Server Credentials", "Aadhaar Server Credentials", "Menu Availability", "Application Settings"};
    public static final String[] KEY = {"Device Type", "Smart Reader Type", "Finger Reader Type", "Enrollment Mode", "Server IP", "Server Port", "Domain", "Server Type", "Url", "Employee Enrollment", "Master Data Entry", "Programmable InOut", "Excel Export/Import", "App Type", "App SubType"};
    public static final String[] DEVICE_TYPE_VAL = {"0", "1"};
    public static final String[] SMART_READER_TYPE_VAL = {"0", "1"};
    public static final String[] FINGER_READER_TYPE_VAL = {"0", "1", "2"};
    public static final String[] FINGER_ENROLLMENT_MODE_VAL = {"0", "1"};
    public static final String[] SERVER_TYPE_VAL = {"0", "1", "2"};
    public static final String[] EMPLOYEE_ENROLLMENT_TYPE_VAL = {"0", "1"};
    public static final String[] MASTER_DATA_ENTERY_TYPE_VAL = {"0", "1"};
    public static final String[] PROGRAMMABLE_INOUT_STYPE_VAL = {"0", "1"};
    public static final String[] EXCEL_IMPORT_EXPORT = {"0", "1"};
    public static final String[] APP_TYPE = {"0", "1"};


    //============================= Smart Card Credentials  ========================================================//

    public final static int BUFFER_SIZE = 64;

    public static final String MAD_ONE_DATA_FOR_TWO_TEMPLATES = "48024802480248024802480248024802";
    public static final String MAD_ONE_DATA_FOR_ONE_TEMPLATE = "48024802000000000000000000000000";

    public static final String READ_CARD_ID_COMMAND = "#FF0003";
    public static final String WRITE_CARD_ID_COMMAND = "#FF0000";
    public static final String READ_CSN_COMMAND = "#FF4601A00152455353454300";
    public static final String CHECK_CARD_INIT_COMMAND = "#FF4601A10143505320494400";
    public static final String CHECK_CARD_TYPE_COMMAND = "#FF4601A20135343332313001";
    public static final String CHECK_CARD_IS_REFRESHED = "#FF4601A20135343332313001";

    public static final String DEFAULT_SMART_CARD_VERSION = "-1";

    public static final String FIRST_FINGER_QUALITY = "41";
    public static final String SECOND_FINGER_QUALITY = "42";

    public static final String[] SECTOR_READ_COMM = {"#FF4601A2", "#FF4601A4", "#FF4601A5", "#FF4601A6", "#FF4601A7", "#FF4601A8", "#FF4601A9", "#FF4601AA", "#FF4601AB", "#FF4601AC", "#FF4601AD", "#FF4601AE", "#FF4601AF"};
    public static final String[] FACTORY_SEC_READ = {"#FF4601A0", "#FF4601A1", "#FF4601A2", "#FF4601A3", "#FF4601A4", "#FF4601A5", "#FF4601A6", "#FF4601A7", "#FF4601A8", "#FF4601A9", "#FF4601AA", "#FF4601AB", "#FF4601AC", "#FF4601AD", "#FF4601AE", "#FF4601AF"};
    public static final String[] SECTOR_WRITE_COMM = {"#FF4701A201", "#FF4701A301", "#FF4701A401", "#FF4701A501", "#FF4701A601", "#FF4701A701", "#FF4701A801", "#FF4701A901", "#FF4701AA01", "#FF4701AB01", "#FF4701AC01", "#FF4701AD01", "#FF4701AE01", "#FF4701AF01"};

    public static final String KEY_A = "00";
    public static final String KEY_B = "01";
    public static final String ASCII_READ_WRITE = "01";
    public static final String HEX_READ_WRITE = "00";

    //========================================  Sqlite Database Tables  ===============================================//

    public static final String HOTLIST_TABLE = "HotlistLog";
    public static final String SECTOR_KEY_TABLE = "SmartKey";
    public static String SECTOR_KEY_CARD_INIT = "SmartKeyCardInit";
    public static final String EMPLOYEE_TABLE = "EmployeeM";
    public static final String FINGER_TABLE = "FingerTemplateX";
    public static final String ATTENDANCE_TABLE = "AttendanceT";
    public static final String CARD_VER_PIN_TABLE = "CardVerificationPin";
    public static final String IN_OUT_MODE_TABLE = "InOutTimeM";
    public static final String USER_TABLE = "UserM";
    public static final String FINGER_ENROLL_MODE_TABLE = "FingerEnrollMode";
    public static final String SETTINGS_TABLE = "SettingsM";
    public static final String GROUP_TABLE = "GroupM";
    public static final String SITE_TABLE = "SiteCodeM";
    public static final String TRAINING_TABLE = "TrainingCenterM";
    public static final String BATCH_TABLE = "BatchM";
    public static final String AADHAARAUTH_TABLE = "AadhaarAuthT";
    public static final String SMART_CARD_OPERATION_TABLE = "SmartCardOperationLog";
    public static final String ATTENDANCE_SERVER_TABLE = "AttendanceServer";
    public static final String SETTINGS_INI_TABLE = "SettingsIniM";
    public static final String AADHAAR_SERVER_TABLE = "AadhaarServer";
    public static final String WIEGAND_TABLE = "WiegandM";


    public static final String SUBJECT_TABLE = "SubjectM";
    public static final String PROFESSOR_SUBJECT_TABLE = "ProfessorSubjectM";
    public static final String PROFESSOR_STUDENT_SUBJECT_TABLE = "ProfessorStudentSubjectM";

    public static final String EMPLOYEE_TYPE_TABLE = "EmployeeTypeM";
    public static final String CONTRACTOR_TABLE = "ContractorM";
    public static final String PERIOD_TABLE = "PeriodM";


    public static final String ATTENDANCE_COLLEGE_TABLE = "AttendanceC";
    public static final String LOGIN_STATUS_TABLE = "LoginStatus";

    public static final String BROKER_DETAILS_TABLE = "MQTTBrokerDetails";


    public static final String HTTP_SERVER_DETAILS = "HTTPServerDetails";
    public static final String BROKER_DETAILS = "BrokerDetails";
    public static final String PROTOCOL_DETAILS = "ProtocolM";

    public static final String GVM_TABLE = "GVM";


    //================== Blood Group,Security Levels,Finger Indexes,Quality,Verification Mode For Card ===========//

    public static final String BLOOD_GROUPS[] = {"", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-", "", ""};
    public static final String SECURITY_LEVELS[] = {"", "A", "B", "C", "D", "E1", "E2", "E4", "E8", "", ""};
    public static final String FINGER_INDEXES[] = {"", "R-Thumb", "R-Index", "R-Middle", "R-Ring", "R-Little", "L-Thumb", "L-Index", "L-Middle", "L-Ring", "L-Little"};
    public static final String QUALITY[] = {"", "A", "B", "C", "D", "E", "", "", "", "", ""};
    public static final String VERIFICATION_MODES[] = {"CARD-ONLY", "", "1:N", "", "FORCE ENROLL", "", "", "", "CARD+FINGER", "CARD+PIN+FINGER", ""};

    //================== Blood Group,Security Levels,Finger Indexes,Quality,Verification Mode For Spinners ===========//

    public static final String BLOOD_GROUP_DISPLAY[] = {"Select", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
    public static final String FINGER_INDEX_DISPLAY[] = {"Select", "R-Thumb", "R-Index", "R-Middle", "R-Ring", "R-Little", "L-Thumb", "L-Index", "L-Middle", "L-Ring", "L-Little"};
    public static final String SECURITY_LEVEL_DISPLAY[] = {"C", "A", "B", "D", "E1", "E2", "E4", "E8"};
    public static final String VERIFICATION_MODE_DISPLAY[] = {"1:N", "CARD-ONLY", "CARD+FINGER", "CARD+PIN+FINGER"};//Removed Force Enroll


    //================== Global Verification Mode For Spinners ===========//

    public static final String GVM_DISPLAY[] = {"1:N", "CARD-ONLY", "CARD+FINGER", "CARD-BASED-VERIFY"};


    //public static final String VERIFICATION_MODE_DISPLAY[] = {"1:N", "CARD-ONLY", "FORCE ENROLL", "CARD+FINGER", "CARD+PIN+FINGER",};


    //================== Is Access Right Enabled,Is Black Listed,Is Lock Open When Allowed Values For Spinners ===========//

    public static final String IS_ACCESS_RIGHT_ENABLED[] = {"Select", "Yes", "No"};
    public static final String IS_BLACK_LISTED[] = {"Select", "Yes", "No"};
    public static final String IS_LOCK_OPEN_WHEN_ALLOWED[] = {"Select", "Yes", "No"};


    public static final int TEMPLATE_SIZE = 512;
    public static final int VERIFY_BY_CARD_MODE = 0;
    public static final int VERIFY_BY_LOCAL_DATABASE_MODE = 1;

    //=========================================== Morpho Credentials =====================================================//

    public final static int MORPHO_MAX_RECORD = 500;
    public final static int MORPHO_MAX_FINGER = 2;
    public final static boolean MORPHO_ENCRYPT_DATABASE = false;
    public final static String MORPHO_DATABASE_FIRST_FIELD_NAME = "First";
    public final static String MORPHO_DATABASE_LAST_FIELD_NAME = "Last";
    public final static int MORPHO_FIELD_MAX_SIZE = 16;

    //========================================== Application Modules ==================================================//

    //public final static String[] HOME_MENU_ITEM_NAMES = {"Employee Enrollment", "Finger Enrollment", "Finger Enrollment (Aadhaar Mode)", "Attendance", "Smart Card", "Master Data Entry", "Programmable In-Out", "Settings(Finger Enrollment Mode)", "Server Settings", "Hardware Settings", "Excel Export/Import", "User Creation", "Delete User", "Server Settings", "Wiegand Settings", "System Info", "Log Out"};

    public final static String[] HOME_MENU_ITEM_NAMES = {"Employee Enrollment", "Finger Enrollment","Delete User","Smart Card","Attendance","Programmable In-Out","Server Settings", "Wiegand Settings","GVM Settings","User Info","System Info","User Creation","Master Data Entry","Finger Enrollment (Aadhaar Mode)","Settings(Finger Enrollment Mode)", "Hardware Settings", "Excel Export/Import", "Log Out"};

    public final static String ATTENDANCE_MODES[] = {"Select", "In", "Out"};

    public final static String IPADDRESS_PATTERN =
            "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

    //  public static final String PORT_PATTERN = "^(6553[0-5]|655[0-2]\\d|65[0-4]\\d\\d|6[0-4]\\d{3}|[1-5]\\d{4}|[2-9]\\d{3}|1[1-9]\\d{2}|10[3-9]\\d|102[4-9])$";

    public static final String PORT_PATTERN = "^([0-9]{1,4}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])$";


    public static final String EMAIL_REGEX = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    public static final String[] IMAGE_FILE_EXTENSIONS = new String[]{
            "png", "jpg", "bmp", "gif"// and other formats you need
    };

    public static final String IN_MODE_VALUE = "00";
    public static final String OUT_MODE_VALUE = "01";

    public static final int SAVE_VALUE = 0;
    public static final int UPDATE_VALUE = 1;


    //=========================  Smiley Hider ================================//

    public static InputFilter EMOJI_FILTER = new InputFilter() {

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            for (int index = start; index < end; index++) {
                int type = Character.getType(source.charAt(index));
                if (type == Character.SURROGATE) {
                    return "";
                }
            }
            return null;
        }
    };

    public static InputFilter SPECIAL_CHARACTER_FILTER = new InputFilter() {
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            for (int i = start; i < end; i++) {
                if (!Character.isLetterOrDigit(source.charAt(i))) {
                    return "";
                }
            }
            return null;
        }
    };


    //=============================  EzeeHrLite Url and Commands  ==========================================//

    //================================ CORPORATE ID  =====================================//

    public final static String CORPORATE_ID = "T0000000020";

    //=============================================  EzeeHrLite URL ===========================================================//

    public final static String BASE_URL = "/RESTServices/RESTeZeeHRLiteService.svc/rest/";

    public final static String DEVICE_REG_URL = "/RESTServices/RESTeZeeHRLiteService.svc/rest/DeviceRegistration";
    public final static String GET_JOB_URL = "/RESTServices/RESTeZeeHRLiteService.svc/rest/GetCurrentJob";
    public final static String TEMPLATE_UPLOAD_URL = "/RESTServices/RESTeZeeHRLiteService.svc/rest/TemplateUploadFromDevice";
    public final static String TEMPLATE_DOWNLOAD_URL = "/RESTServices/RESTeZeeHRLiteService.svc/rest/TemplateUploadFromDevice";
    public final static String GET_DEVICE_TOKEN_URL = "/RESTServices/RESTeZeeHRLiteService.svc/rest/GetDeviceToken?";
    public final static String ATTENDANCE_UPLOAD_URL = "/RESTServices/RESTeZeeHRLiteService.svc/rest/AttendnaceUpload";
    public final static String GET_TIME_SYNC_URL = "/RESTServices/RESTeZeeHRLiteService.svc/rest/GetFormattedDateTime?"; //corporateid={CORPORATEID}
    public final static String POST_UNFINISHED_JOB_URL = "/RESTServices/RESTeZeeHRLiteService.svc/rest/UpdateunfinishedJobStatus";
    public final static String POST_DEVICE_DELETE_URL = "/RESTServices/RESTeZeeHRLiteService.svc/rest/DeviceDelete";
    public final static String POST_DEVICE_STATUS_URL = "/RESTServices/RESTeZeeHRLiteService.svc/rest/DeviceStatus";

    public final static String GET_DATE_TIME_URL = "/RESTServices/RESTeZeeHRLiteService.svc/rest/GetFormattedDateTime";

    //================================ COMMAND TYPE  =====================================//

    public final static String ATTENDANCE_POST_COMM = "1000";
    public final static String DEVICE_REG_COMM = "1000";
    public final static String TEMPLATE_UPLOAD_JOB_COMM = "2100";
    public final static String TEMPLATE_DOWNLOAD_JOB_COMM = "2200";
    public final static String EMP_VALIDATION_JOB_DOWNLOAD_COMM = "1700";
    public final static String REMOTE_ENROLLMENT_JOB_COMM = "7800";
    public final static String POST_DEVICE_STATUS_COMM = "1080";
    public final static String GET_SIGN_ON_MESSAGE_COMM = "0123";
    public final static String EMP_VALIDATION_JOB_UPLOAD_COMM = "1701";

    public final static String EMPLOYEE_VALIDATION_GET_JOB = "65";
    public final static String TEMPLATE_DOWNLOAD_GET_JOB = "66";
    public final static String TEMPLATE_UPLOAD_GET_JOB = "67";
    public final static String REMOTE_ENROLLMENT_GET_JOB = "68";

    //================================ Forlinx GPIO,LED ==================================//

    public final static String RC522_WRITE_FILE_PATH = "/sys/kernel/rc522/write";
    public final static String RC522_READ_FILE_PATH = "/sys/kernel/rc522/read";


    public final static String RC522_CARD_NOT_PRESENT_VAL = "50636452";//PcdR

    public final static String RC522_READ_CARD_ID_COMM = "6 0 543210 B";

    public final static String RC522_BLOCK_READ_COMMAND = "3";
    public final static String RC522_WRITE_COMMAND = "4";

    public final static String RC522_SECTOR_READ_COMMAND = "8";
    public final static String RC522_CHANGE_KEY_COMMAND = "5";

    public final static String MAD_DATA_FOR_TWO_TEMPLATES = "48024802480248024802480248024802";
    public final static String MAD_DATA_FOR_ONE_TEMPLATE = "48024802000000000000000000000000";

    //init RC522
    public final static String RC522_INIT_COMMAND = "7 0 123456789123 B";

    //Read CSN
    public final static String RC522_READ_CSN_COMMAND = "6 0 524553534543 B";
    public final static String RC522_READ_CARDID_COMMAND = "3 4 435053204944 B";

    //Check Card Is refreshed
    public final static String RC522_CHECK_CARD_IS_REFRESHED = "3 8 353433323130 B";

    // Read Sector 2 block 8 with 3534323130 if read is success then card init/refresh card found
    public final static String RC522_CHECK_KEY_TYPE_COMMAND = "3 8 353433323130 B";

    public final static String RC522_CHECK_CARD_INIT = "3 4 435053204944 B";//Read Sector 1 Block 4

    public final static String RC522_KEY_TYPE_A = "A";
    public final static String RC522_KEY_TYPE_B = "B";

    public final static String RC522_KEY_B = "PERINF";

//    public final static String B_Onn = "sh /data/tmp/buzz_onn.sh";
//    public final static String B_Off = "sh /data/tmp/buzz_off.sh";

    //======================= Path Of System File For Buzzer On Off in Forlinx ===========================//

    public final static String B_Onn = "sh /system/etc/tmp/buzz_onn.sh";
    public final static String B_Off = "sh /system/etc/tmp/buzz_off.sh";


    public final static String RC522_INSTALL = "sh /data/tmp/rc522.sh";


    //======================= Path Of System File For Red Led On Off in Forlinx ===========================//

    public final static String RledOnn = "sh /system/etc/tmp/rled_onn.sh";
    public final static String RledOff = "sh /system/etc/tmp/rled_off.sh";

    //======================= Path Of System File For Green Led On Off in Forlinx ===========================//

    public final static String GledOnn = "sh /system/etc/tmp/gled_onn.sh";
    public final static String GledOff = "sh /system/etc/tmp/gled_off.sh";


    public final static String BUZZ_PATH = "/sys/devices/platform/nxp-gpio.1/gpio/gpio50/value";
    //0-Green On,1-Green Off
    // public final static String GREEN_LED_PATH="/sys/devices/platform/nxp-gpio.1/gpio/gpio40/value";

    //need to replace in init.rc
    public final static String GREEN_LED_PATH = "/sys/devices/platform/nxp-gpio.1/gpio/gpio38/value";
    //0-Red Off,1-Red On
    public final static String RED_LED_PATH = "/sys/devices/platform/nxp-gpio.2/gpio/gpio91/value";

    public final static String LCD_BACKLIGHT_EN_PATH = "/sys/devices/platform/nxp-gpio.3/gpio/gpio98/value";


    public final static String CHARGE_DETECT = "/sys/devices/platform/nxp-gpio.2/gpio/gpio68/value";
    public final static String POWER_DETECT = "/sys/devices/platform/nxp-gpio.4/gpio/gpio132/value";

    //  public final static String BATTERY_DETECT="/sys/devices/platform/nxp-gpio.4/gpio/gpio132/value";

    //OFF 0 ON 1
    public final static String WIFI_ENABLE_DISABLE = "/sys/devices/platform/nxp-gpio.0/gpio/gpio30/value";

    public final static int WIEGAND_TYPE_1 = 26;
    public final static int WIEGAND_TYPE_2 = 34;
    public final static String WIEGAND_READ_PATH = "/sys/kernel/wig_in/read";
    public final static String WIEGAND_WRITE_PATH = "/sys/kernel/wig_in/write";

    public final static String CAP_READ_PATH = "/sys/kernel/i2c_kbd/read";

//    public final static int BRIGHTNESS_OFF_DELAY=30000;
//    public final static int BACKLIGHT_OFF_DELAY=60000;
//    public final static int IDENTIFICATION_RESTART_DELAY=1000;

    public final static int ADC_READ_ARRAY_LENGTH = 50;

    public final static int BRIGHTNESS_OFF_DELAY = 120000;//30000
    public final static int BACKLIGHT_OFF_DELAY = 240000;//60000
    public final static int IDENTIFICATION_RESTART_DELAY = 500;//2500//1000//500

    public final static int BRIGHTNESS_ON = 150;
    public final static int BRIGHTNESS_OFF = 0;

    public final static float X1 = 0;
    public final static float X2 = 4095;
    public final static float Y1 = 0;
    public final static float Y2 = 1800; //1800 - 1532 = 268

    public final static float XX1 = Y1;
    public final static float XX2 = Y2 - 260;//300 is the adj value
    public final static float YY1 = 0;
    public final static float YY2 = 4300;//3950

    public final static float XXX1 = YY1 + 3500; //lower voltage 3.3v
    public final static float XXX2 = 3900;//4000
    public final static float YYY1 = 0;
    public final static float YYY2 = 5;


    public final static float DEVIATION_FACTOR = 20.0F;


    //RELAY
    // echo 0 > /sys/devices/platform/nxp-gpio.1/gpio/gpio46/value //On
    // echo 1 > /sys/devices/platform/nxp-gpio.1/gpio/gpio46/value //Off

    //LOCK-FEEDBACK
    // cat /sys/devices/platform/nxp-gpio.4/gpio/gpio159/value

    //915916
    public final static int DELEY = 50;//50

    public final static String DEFAULT_GVM = "1:N";

}
