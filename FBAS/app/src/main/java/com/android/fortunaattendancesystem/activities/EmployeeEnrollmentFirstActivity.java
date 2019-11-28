package com.android.fortunaattendancesystem.activities;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.fortunaattendancesystem.R;
import com.android.fortunaattendancesystem.constant.Constants;
import com.android.fortunaattendancesystem.forlinx.ForlinxGPIO;
import com.android.fortunaattendancesystem.helper.Utility;
import com.android.fortunaattendancesystem.helper.VerhoeffAlgorithm;
import com.android.fortunaattendancesystem.model.EmployeeEnrollInfo;
import com.android.fortunaattendancesystem.model.WiegandSettingsInfo;
import com.android.fortunaattendancesystem.submodules.I2CCommunicator;
import com.android.fortunaattendancesystem.submodules.SQLiteCommunicator;
import com.android.fortunaattendancesystem.submodules.WiegandCommunicator;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;


public class EmployeeEnrollmentFirstActivity extends Activity {

    private static final int CAMERA_REQUEST = 1888;
    private SQLiteCommunicator dbComm = new SQLiteCommunicator();
    private static InputMethodManager inputMethodManager;

    private byte[] byteimage = null;
    private ImageView image;
    private EditText editTextEmpid, editTextCardid, editTextName, editTextAadhaarId, editTextMobileNo, editTextMailId, editTextValidUpto, editTextBirthday;
    private Spinner spinnerBloodGroup;
    private ArrayAdapter <String> adapter;
    private final static String BLANK = "";


    private Handler rHandler = new Handler();
    private Timer wigReadTimer = null;
    private TimerTask wigReadTimerTask = null;
    boolean isWiegandReading = false;

    private static boolean isLCDBackLightOff = false;
    private Handler cHandler = new Handler();
    private Timer capReadTimer = null;
    private TimerTask capReadTimerTask = null;
    private boolean isBreakFound = true;

    private Handler hBrightness, hLCDBacklight;
    private Runnable rBrightness, rLCDBacklight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setContentView(R.layout.activity_form_fill_white);

        findViewById(android.R.id.content).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideSoftKeyboard(EmployeeEnrollmentFirstActivity.this);
                return false;
            }
        });
        findViewById(R.id.emp_enroll_scrollview).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideSoftKeyboard(EmployeeEnrollmentFirstActivity.this);
                return false;
            }
        });

        initLayoutElements();
        addListnersToEditText();
        fillSpinnerItems();

        hBrightness = new Handler();
        hLCDBacklight = new Handler();

        rBrightness = new Runnable() {
            @Override
            public void run() {
                setScreenBrightness(Constants.BRIGHTNESS_OFF);
                isLCDBackLightOff = true;
            }
        };

        rLCDBacklight = new Runnable() {
            @Override
            public void run() {
                ForlinxGPIO.setLCDBackLightOff();
                isLCDBackLightOff = true;
            }
        };

        startHandler();
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        stopHandler();//stop first and then start
        startHandler();
        ForlinxGPIO.setLCDBackLightOn();
        setScreenBrightness(Constants.BRIGHTNESS_ON);
        isLCDBackLightOff = false;
    }

    public void startHandler() {
        hBrightness.postDelayed(rBrightness, Constants.BRIGHTNESS_OFF_DELAY); //for 10 seconds
        hLCDBacklight.postDelayed(rLCDBacklight, Constants.BACKLIGHT_OFF_DELAY); //for 20 seconds
    }

    public void stopHandler() {
        hBrightness.removeCallbacks(rBrightness);
        hLCDBacklight.removeCallbacks(rLCDBacklight);
    }

    // Change the screen brightness
    public void setScreenBrightness(int brightnessValue) {
        // Make sure brightness value between 0 to 255
        if (brightnessValue >= 0 && brightnessValue <= 255) {
            android.provider.Settings.System.putInt(
                    getApplicationContext().getContentResolver(),
                    android.provider.Settings.System.SCREEN_BRIGHTNESS,
                    brightnessValue
            );
        }
    }

    public void initLayoutElements() {

        inputMethodManager = (InputMethodManager) EmployeeEnrollmentFirstActivity.this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        image = (ImageView) findViewById(R.id.imageView1);
        image.setClickable(true);

        editTextEmpid = (EditText) findViewById(R.id.EmployeeID);
        editTextCardid = (EditText) findViewById(R.id.CardID);
        editTextName = (EditText) findViewById(R.id.Name);
        editTextAadhaarId = (EditText) findViewById(R.id.AadhaarId);
        editTextMobileNo = (EditText) findViewById(R.id.MobileNo);
        editTextMailId = (EditText) findViewById(R.id.MailId);
        editTextValidUpto = (EditText) findViewById(R.id.ValidUpto);
        editTextBirthday = (EditText) findViewById(R.id.BirthDay);
        spinnerBloodGroup = (Spinner) findViewById(R.id.bloodgroup);

        editTextEmpid.setFilters(new InputFilter[]{new InputFilter.AllCaps(), Constants.SPECIAL_CHARACTER_FILTER, Constants.EMOJI_FILTER, new InputFilter.LengthFilter(16)});
        editTextCardid.setFilters(new InputFilter[]{new InputFilter.AllCaps(), Constants.EMOJI_FILTER, new InputFilter.LengthFilter(8)});
        editTextName.setFilters(new InputFilter[]{new InputFilter.AllCaps(), Constants.EMOJI_FILTER, new InputFilter.LengthFilter(16)});
        editTextMailId.setFilters(new InputFilter[]{Constants.EMOJI_FILTER});
    }

    public void addListnersToEditText() {
        editTextEmpid.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String empId = editTextEmpid.getText().toString();
                    if (empId != null && empId.trim().length() > 0) {
                        EmployeeEnrollInfo empInfo = new EmployeeEnrollInfo();
                        empInfo = dbComm.getEmpBasicDetails(empId, empInfo);
                        if (empInfo.getEnrollmentNo() != -1) {
                            addValuesToUi(empInfo);
                        }
                    }
                }
            }
        });
        editTextEmpid.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String strEmpid = editTextEmpid.getText().toString();
                if (strEmpid.trim().length() > 0) {
                    editTextEmpid.setError(null);
                }
            }
        });
        editTextCardid.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String strCardId = editTextCardid.getText().toString();
                if (strCardId.trim().length() > 0) {
                    editTextCardid.setError(null);
                }
            }
        });
        editTextName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String strEmpName = editTextName.getText().toString();
                if (strEmpName.trim().length() > 0) {
                    editTextName.setError(null);
                }
            }
        });
        editTextAadhaarId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String strAadhaarId = editTextAadhaarId.getText().toString();
                if (strAadhaarId.length() > 0) {
                    editTextAadhaarId.setError(null);
                }
            }
        });
        editTextMobileNo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String strMobileNo = editTextMobileNo.getText().toString();
                if (strMobileNo.length() > 0) {
                    editTextMobileNo.setError(null);
                }
            }
        });
        editTextMailId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String strEmailId = editTextMailId.getText().toString();
                if (strEmailId.length() > 0) {
                    editTextMailId.setError(null);
                }
            }
        });
        editTextValidUpto.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String strValidUpto = editTextValidUpto.getText().toString();
                if (strValidUpto.trim().length() > 0) {
                    boolean isValid = Utility.validateValidUptoDate(strValidUpto);
                    if (isValid) {
                        editTextValidUpto.setError(null);
                    }
                }
            }
        });
        editTextBirthday.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String strBirthday = editTextBirthday.getText().toString();
                if (strBirthday.trim().length() > 0) {
                    boolean isValid = Utility.validateBirthDate(strBirthday);
                    if (isValid) {
                        editTextBirthday.setError(null);
                    }
                }
            }
        });
    }

    public void fillSpinnerItems() {
        fillBloodGroup();
    }

    public void fillBloodGroup() {
        adapter = new ArrayAdapter <String>(this, android.R.layout.simple_spinner_item, Constants.BLOOD_GROUP_DISPLAY);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBloodGroup.setAdapter(adapter);
    }

    public void clearUiValues() {
        byteimage = null;
        image.setImageResource(R.drawable.dummyphoto);
        editTextEmpid.setText("");
        editTextCardid.setText("");
        editTextName.setText("");
        editTextAadhaarId.setText("");
        spinnerBloodGroup.setSelection(0);
        editTextMobileNo.setText("");
        editTextMailId.setText("");
        editTextValidUpto.setText("");
        editTextBirthday.setText("");

    }

    public void validUpto(View view) {
        Calendar mcurrentDate = Calendar.getInstance();
        int mYear = mcurrentDate.get(Calendar.YEAR);
        int mMonth = mcurrentDate.get(Calendar.MONTH);
        int mDay = mcurrentDate.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog mDatePicker = new DatePickerDialog(EmployeeEnrollmentFirstActivity.this, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                // TODO Auto-generated method stub
                    /*      Your code   to get date and time    */
                selectedmonth++;
                String yy = String.valueOf(selectedyear);
                String dd = String.valueOf(selectedday);
                String mm = String.valueOf(selectedmonth);
                if (dd.length() == 2) {
                    dd = dd;
                } else {
                    dd = "0" + dd;
                }
                if (mm.length() == 2) {
                    mm = mm;
                } else {
                    mm = "0" + mm;
                }
                //yy = yy.substring(2,4);

                yy = yy.substring(0, 4);
                editTextValidUpto.setText(dd + "-" + mm + "-" + yy);

            }
        }, mYear, mMonth, mDay);
        mDatePicker.setTitle("Select date");
        mDatePicker.show();
    }

    public void dateOfBirth(View view) {
        Calendar mcurrentDate = Calendar.getInstance();
        int mYear = mcurrentDate.get(Calendar.YEAR);
        int mMonth = mcurrentDate.get(Calendar.MONTH);
        int mDay = mcurrentDate.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog mDatePicker = new DatePickerDialog(EmployeeEnrollmentFirstActivity.this, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                // TODO Auto-generated method stub
                    /*      Your code   to get date and time    */
                selectedmonth++;
                String yy = String.valueOf(selectedyear);
                String dd = String.valueOf(selectedday);
                String mm = String.valueOf(selectedmonth);
                if (dd.length() == 2) {
                    dd = dd;
                } else {
                    dd = "0" + dd;
                }
                if (mm.length() == 2) {
                    mm = mm;
                } else {
                    mm = "0" + mm;
                }
                //yy = yy.substring(2,4);

                yy = yy.substring(0, 4);


                editTextBirthday.setText(dd + "-" + mm + "-" + yy);
            }
        }, mYear, mMonth, mDay);
        mDatePicker.setTitle("Select date");
        mDatePicker.show();
    }

    public void next(View view) {
        boolean isValidated = false;
        isValidated = validateUserInputs();
        if (isValidated) {
            EmployeeEnrollInfo info = new EmployeeEnrollInfo();
            info = fillEmpEnrollData(info);
            Intent intent = new Intent(EmployeeEnrollmentFirstActivity.this, EmployeeEnrollmentSecondActivity.class);
            intent.putExtra("EmployeeEnrollInfo", info);
            startActivity(intent);
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        }
    }

    private EmployeeEnrollInfo fillEmpEnrollData(EmployeeEnrollInfo info) {
        String value;
        info.setEmpId(editTextEmpid.getText().toString());
        info.setCardId(editTextCardid.getText().toString());
        info.setEmpName(editTextName.getText().toString());
        info.setAadhaarId(editTextAadhaarId.getText().toString());
        info.setMobileNo(editTextMobileNo.getText().toString());
        info.setEmailId(editTextMailId.getText().toString());
        value = spinnerBloodGroup.getSelectedItem().toString();
        if (!value.equals("Select")) {
            info.setBloodGroup(value);
        } else {
            info.setBloodGroup(BLANK);
        }
        info.setValidUpto(editTextValidUpto.getText().toString());
        info.setDateOfBirth(editTextBirthday.getText().toString());

        info.setEnrollSource("L");//For Local Employee Enrollment
        info.setJobCode("0000");//For Local Employee Enrollment
        info.setPhoto(byteimage);
        return info;
    }

    private boolean validateUserInputs() {
        String strEmpid = editTextEmpid.getText().toString();
        if (strEmpid.trim().length() == 0) {
            editTextEmpid.setError("Employee Id Cannot Be Left Blank");
            editTextEmpid.requestFocus();
            return false;
        }
        String strCardId = editTextCardid.getText().toString();
        if (strCardId.trim().length() == 0) {
            editTextCardid.setError("Card Id Cannot Be Left Blank");
            editTextCardid.requestFocus();
            return false;
        } else {
            boolean isCardIdUnique = false;
            isCardIdUnique = checkForDuplicateCardId(strEmpid, strCardId);
            if (!isCardIdUnique) {
                editTextCardid.setError("Card Id Already Exists.Enter New Card Id");
                editTextCardid.requestFocus();
                return false;
            }
        }
        String strEmpName = editTextName.getText().toString();
        if (strEmpName.trim().length() == 0) {
            editTextName.setError("Employee Name Cannot Be Left Blank");
            editTextName.requestFocus();
            return false;
        }
        String strAadhaarId = editTextAadhaarId.getText().toString();
        if (strAadhaarId.trim().length() > 0) {
            if (strAadhaarId.trim().length() != 12) {
                editTextAadhaarId.setError("Please Enter 12 digit Aadhaar ID");
                editTextAadhaarId.requestFocus();
                return false;
            } else {
                boolean isValid = VerhoeffAlgorithm.validateVerhoeff(strAadhaarId);
                if (!isValid) {
                    editTextAadhaarId.setError("Please Enter A Valid Aadhaar ID");
                    editTextAadhaarId.requestFocus();
                    return false;
                } else {
                    boolean isAadhaarIdUnique = false;
                    isAadhaarIdUnique = checkForDuplicateAadhaarId(strEmpid, strAadhaarId);
                    if (!isAadhaarIdUnique) {
                        editTextAadhaarId.setError("Aadhaar Id Already Enrolled.Please Enter New Aadhaar ID.");
                        editTextAadhaarId.requestFocus();
                        return false;
                    }
                }
            }
        }
        String strMobileNo = editTextMobileNo.getText().toString();
        if (strMobileNo.trim().length() > 0) {
            if (strMobileNo.trim().length() != 10) {
                editTextMobileNo.setError("Please Enter 10 digit Mobile No");
                editTextMobileNo.requestFocus();
                return false;
            }
        }
//        String strEmailId = editTextMailId.getText().toString();
//        if (strEmailId.trim().length() == 0) {
//            editTextMailId.setError("Email Id Cannot Be Left Blank");
//            editTextMailId.requestFocus();
//            return false;
//        } else {
//            boolean isValid = Utility.validateEmailId(strEmailId);
//            if (!isValid) {
//                editTextMailId.setError("Please Enter A Valid Email Id");
//                editTextMailId.requestFocus();
//                return false;
//            }
//        }

        String strEmailId = editTextMailId.getText().toString();
        if (strEmailId.trim().length() > 0) {
            boolean isValid = Utility.validateEmailId(strEmailId);
            if (!isValid) {
                editTextMailId.setError("Please Enter A Valid Email Id");
                editTextMailId.requestFocus();
                return false;
            }
        }


        String strCurrentDate = editTextValidUpto.getText().toString();
        if (strCurrentDate.trim().length() > 0) {
            boolean isValidUpto = Utility.validateValidUptoDate(strCurrentDate);
            if (!isValidUpto) {
                editTextValidUpto.setError("Valid Date Should Be greater Than Or Equal To Current Date");
                editTextValidUpto.requestFocus();
                return false;
            }
        }
        String strBirthDate = editTextBirthday.getText().toString();
        if (strBirthDate.trim().length() > 0) {
            boolean isValidBirthDate = Utility.validateBirthDate(strBirthDate);
            if (!isValidBirthDate) {
                editTextBirthday.setError("Birth Date Should Be Less Than Or Equal To Current Date");
                editTextBirthday.requestFocus();
                return false;
            }
        }
//        if (byteimage == null) {
//            Toast.makeText(EmployeeEnrollmentFirstActivity.this, "Picture cannot be left blank", Toast.LENGTH_LONG).show();
//            image.requestFocus();
//            return false;
//        }

        return true;
    }

    private boolean checkForDuplicateCardId(String strEmpId, String strCardId) {
        String strPaddedEmpId = Utility.paddEmpId(strEmpId);
        int AutoId = -1;
        AutoId = dbComm.isDataAvailableInDatabase(strPaddedEmpId);
        if (AutoId == -1) {
            boolean isCardIdExists = false;
            isCardIdExists = dbComm.isCardDataAvailableInDatabase(strCardId);
            return !isCardIdExists;
        } else {
            String strExistingCardId = dbComm.getCardIdByEmpId(strPaddedEmpId);
            if (!strExistingCardId.equals(strCardId)) {
                boolean isCardIdExists = false;
                isCardIdExists = dbComm.isCardDataAvailableInDatabase(strCardId);
                return !isCardIdExists;
            } else {
                return true;
            }
        }
    }

    private boolean checkForDuplicateAadhaarId(String strEmpId, String strAadhaarId) {
        String strPaddedEmpId = Utility.paddEmpId(strEmpId);
        int AutoId = -1;
        AutoId = dbComm.isDataAvailableInDatabase(strPaddedEmpId);
        if (AutoId == -1) {
            String strAutoId = "";
            strAutoId = dbComm.isAadhaarIdEnrolled(strAadhaarId);
            return strAutoId.trim().length() == 0;
        } else {
            String strExistingAadhaarId = dbComm.getAadhaarId(AutoId);
            if (!strExistingAadhaarId.equalsIgnoreCase(strAadhaarId)) {
                String strAutoId = "";
                strAutoId = dbComm.isAadhaarIdEnrolled(strAadhaarId);
                return strAutoId.trim().length() == 0;
            } else {
                return true;
            }
        }
    }

    private void addValuesToUi(EmployeeEnrollInfo empInfo) {
        String strColumnValue;
        int intColumnValue;
        strColumnValue = empInfo.getCardId();
        if (strColumnValue != null && strColumnValue.trim().length() > 0) {
            editTextCardid.setText(strColumnValue.replaceAll("\\G0", " ").trim());
        } else {
            editTextCardid.setText("");
        }
        strColumnValue = empInfo.getEmpName();
        if (strColumnValue != null && strColumnValue.trim().length() > 0) {
            editTextName.setText(strColumnValue);
        } else {
            editTextName.setText("");
        }
        strColumnValue = empInfo.getBloodGroup();
        if (strColumnValue != null && strColumnValue.trim().length() > 0) {
            spinnerBloodGroup.setSelection(((ArrayAdapter <String>) spinnerBloodGroup.getAdapter()).getPosition(strColumnValue));
        } else {
            spinnerBloodGroup.setSelection(0);
        }
        strColumnValue = empInfo.getMobileNo();
        if (strColumnValue != null && strColumnValue.trim().length() > 0) {
            editTextMobileNo.setText(strColumnValue);
        } else {
            editTextMobileNo.setText("");
        }
        strColumnValue = empInfo.getEmailId();
        if (strColumnValue != null && strColumnValue.trim().length() > 0) {
            editTextMailId.setText(strColumnValue);
        } else {
            editTextMailId.setText("");
        }
        strColumnValue = empInfo.getValidUpto();
        if (strColumnValue != null && strColumnValue.trim().length() > 0) {
            editTextValidUpto.setText(strColumnValue);
        } else {
            editTextValidUpto.setText("");
        }
        strColumnValue = empInfo.getDateOfBirth();
        if (strColumnValue != null && strColumnValue.trim().length() > 0) {
            editTextBirthday.setText(strColumnValue);
        } else {
            editTextBirthday.setText("");
        }
        byteimage = empInfo.getPhoto();
        if (byteimage != null && byteimage.length > 1) {
            image.setImageBitmap(BitmapFactory.decodeByteArray(byteimage, 0, byteimage.length));
        } else {
            image.setImageResource(R.drawable.dummyphoto);
        }
        intColumnValue = empInfo.getEnrollmentNo();
        strColumnValue = dbComm.getAadhaarId(intColumnValue);
        if (strColumnValue.trim().length() > 0) {
            editTextAadhaarId.setText(strColumnValue);
        } else {
            editTextAadhaarId.setText("");
        }
    }

    public void minimizeKeyBoard(View view) {
        hideSoftKeyboard(EmployeeEnrollmentFirstActivity.this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent menu = new Intent(EmployeeEnrollmentFirstActivity.this, HomeActivity.class);
            startActivity(menu);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //=================================  Camera Photo Capture  =====================================================================================//

    public void Camera(View view) {

//        Intent intent = new Intent(EmployeeEnrollmentFirstActivity.this, CameraPreviewActivity.class);
//        startActivity(intent);


//        hasCamera = checkCameraHardware(EmployeeEnrollmentFirstActivity.this);
//        if (hasCamera) {
//            mCamera = Camera.open();
//            if (mCamera != null) {
//                setCameraDisplayOrientation(this,1,mCamera);
//                try {
//                    mCamera.setPreviewDisplay(sHolder);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                mCamera.startPreview();
//            }
//        }

//        sv.setVisibility(View.VISIBLE);
//        mCamera.takePicture(null, null, mCall);

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, CAMERA_REQUEST);
        }
    }

    //============================================= Convert the image to byte ======================================================================================================================//
    //========================================== Default Function For Camera Activity ==============================================================================================================//

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        Log.d("TEST", "On Activity Result");
//
//        Log.d("TEST", "Request Code:" + requestCode);
//        Log.d("TEST", "Result Code:" + resultCode);
//
//        Log.d("TEST", "Photo Data Len:" + resultCode);
//
//
//        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
//
//            // Read the jpeg data
//            byte[] jpegData = PhotoData.getInstance().getCapturedPhotoData();
//
//            Bitmap bmp = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length);
//            // ImageView image = (ImageView) findViewById(R.id.imageView1);
//
//            // image.setImageBitmap(Bitmap.createScaledBitmap(bmp, image.getWidth(),
//            //        image.getHeight(), false));
//
//            //App.log("" + jpegData.length);
//
//            // Do stuff
//
//            // Don't forget to release it


        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                Bitmap bmp = (Bitmap) data.getExtras().get("data");
                image.setImageBitmap(bmp);
                Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byteimage = stream.toByteArray();
                if (byteimage != null) {
                    image.setImageBitmap(BitmapFactory.decodeByteArray(byteimage, 0, byteimage.length));
                } else {
                    image.setImageResource(R.drawable.dummyphoto);
                }
            } else {
                image.setImageResource(R.drawable.dummyphoto);
                Toast.makeText(EmployeeEnrollmentFirstActivity.this, "Failed to get image", Toast.LENGTH_LONG).show();
            }
        }




        //            PhotoData.getInstance().setCapturedPhotoData(null);
//
//
////            if (hasCamera) {
////                //stop the preview
////               // mCamera.stopPreview();
////                //release the camera
////                mCamera.release();
////                //unbind the camera from this object
////                mCamera = null;
////                // previewing = false;
////            }
//        }
    }


    //    public static void setCameraDisplayOrientation(Activity activity, int cameraId, android.hardware.Camera camera) {
//        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
//        android.hardware.Camera.getCameraInfo(cameraId, info);
//        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
//        int degrees = 0;
//        switch (rotation) {
//            case Surface.ROTATION_0:
//                degrees = 0;
//                break;
//            case Surface.ROTATION_90:
//                degrees = 90;
//                break;
//            case Surface.ROTATION_180:
//                degrees = 180;
//                break;
//            case Surface.ROTATION_270:
//                degrees = 270;
//                break;
//        }
//
//        int result;
//        //int currentapiVersion = android.os.Build.VERSION.SDK_INT;
//        // do something for phones running an SDK before lollipop
//        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
//            result = (info.orientation + degrees) % 360;
//            result = (360 - result) % 360; // compensate the mirror
//        } else { // back-facing
//            result = (info.orientation - degrees + 360) % 360;
//        }
//
//        camera.setDisplayOrientation(result);
//    }

    public static boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            return true;
        } else {
            return false;
        }
    }

    public void modifyActionBar() {
        getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#e63900")));
        getActionBar().setTitle(Html.fromHtml("<b><font face='Calibri' color='#FFFFFF'>Employee Enrollment</font></b>"));
    }

    public void hideSoftKeyboard(Activity activity) {
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_form_fill, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.home) {
            Intent previous = new Intent(EmployeeEnrollmentFirstActivity.this, HomeActivity.class);
            startActivity(previous);
            overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            finish();
            return true;
        }
        if (id == R.id.refresh) {
            clearUiValues();
            return true;
        }
        if (id == R.id.preview) {
            boolean isValidated = false;
            isValidated = validateUserInputs();
            if (isValidated) {
                EmployeeEnrollInfo info = new EmployeeEnrollInfo();
                info = fillEmpEnrollData(info);
                Intent intent = new Intent(EmployeeEnrollmentFirstActivity.this, EmployeeEnrollmentPreviewActivity.class);
                intent.putExtra("EmployeeEnrollInfo", info);
                overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                startActivity(intent);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void initializeTimerTask() {
        wigReadTimerTask = new TimerTask() {
            public void run() {
                rHandler.post(new Runnable() {
                    public void run() {
                        //.26:9f948b:10458251:01001111110010100100010110.
                        if (!isWiegandReading) {
                            char[] wigData = WiegandCommunicator.readWiegand(Constants.WIEGAND_READ_PATH);
                            if (wigData != null && wigData.length > 0) {
                                isWiegandReading = true;
                                boolean status = WiegandCommunicator.clearWiegand(Constants.WIEGAND_WRITE_PATH, "1");
                                if (status) {
                                    String strWigData = new String(wigData);
                                    //Log.d("TEST", "Wiegand Data:" + strWigData);
                                    String[] arr = strWigData.split(":");
                                    if (arr != null && arr.length == 4) {
                                        WiegandSettingsInfo info = null;
                                        String val = arr[0].substring(1).trim();
                                        int wiegandType = Integer.parseInt(val);
                                        switch (wiegandType) {
                                            case Constants.WIEGAND_TYPE_1:
                                                info = dbComm.getWiegandSettings(info);
                                                if (info != null) {
                                                    String cardVal = "", rawData = "";
                                                    val = info.getIsHexToDecEnabled();
                                                    switch (val) {
                                                        case "Y":
                                                            boolean isValid = false;
                                                            rawData = arr[3].substring(0, 26);
                                                            try {
                                                                int data = Integer.parseInt(rawData, 2);
                                                                isValid = true;
                                                            } catch (NumberFormatException ne) {
                                                            }
                                                            if (isValid) {
                                                                new Thread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        ForlinxGPIO.runGPIOLEDForWiegandRead();
                                                                    }
                                                                }).start();
                                                                rawData = rawData.substring(1, 25);
                                                                val = info.getIsSiteCodeEnabled();
                                                                switch (val) {
                                                                    case "Y":
                                                                        String siteCode, cardNo;
                                                                        int siteCodeDec, cardNoDec;
                                                                        val = info.getCardNoType();
                                                                        switch (val) {
                                                                            case "0"://16 bit Hex to Dec
                                                                                rawData = rawData.substring(8);
                                                                                Log.d("TEST", "YY0 Raw Data:" + rawData + " Len:" + rawData.length());
                                                                                cardNoDec = Integer.parseInt(rawData, 2);
                                                                                cardVal = Integer.toString(cardNoDec);
                                                                                editTextCardid.setText(cardVal);
                                                                                // editTextName.setText("HTDYSCY16BHTD");
                                                                                break;
                                                                            case "1"://24 bit Hex to Dec
                                                                                rawData = rawData.substring(0);
                                                                                Log.d("TEST", "YY1 Raw Data:" + rawData + " Len:" + rawData.length());
                                                                                cardNoDec = Integer.parseInt(rawData, 2);
                                                                                cardVal = Integer.toString(cardNoDec);
                                                                                editTextCardid.setText(cardVal);
                                                                                //  editTextName.setText("HTDYSCY24BHTD");
                                                                                break;
                                                                            case "2"://32 bit Hex to Dec
                                                                                break;
                                                                            case "3":// Site Code + 16 bit
                                                                                siteCode = rawData.substring(0, 8);
                                                                                cardNo = rawData.substring(8);
                                                                                Log.d("TEST", "YY3SC+16B Raw Data:" + rawData + " Len:" + rawData.length());
                                                                                siteCodeDec = Integer.parseInt(siteCode, 2);
                                                                                cardNoDec = Integer.parseInt(cardNo, 2);
                                                                                cardVal = Integer.toString(siteCodeDec) + Integer.toString(cardNoDec);
                                                                                editTextCardid.setText(cardVal);
                                                                                // editTextName.setText("HTDYSCYSC16BHTD");
                                                                                break;
                                                                            case "4"://Site Code + 24 bit
                                                                                break;

                                                                        }
                                                                        break;
                                                                    case "N":
                                                                        val = info.getCardNoType();
                                                                        switch (val) {
                                                                            case "0"://16 bit Hex to Dec
                                                                                rawData = rawData.substring(8);
                                                                                Log.d("TEST", "YN016B Raw Data:" + rawData + " Len:" + rawData.length());
                                                                                cardNoDec = Integer.parseInt(rawData, 2);
                                                                                cardVal = Integer.toString(cardNoDec);
                                                                                editTextCardid.setText(cardVal);
                                                                                // editTextName.setText("HTDYSCN16BHTD");
                                                                                break;
                                                                            case "1"://24 bit Hex to Dec
                                                                                rawData = rawData.substring(0);
                                                                                Log.d("TEST", "YN124B Raw Data:" + rawData + " Len:" + rawData.length());
                                                                                cardNoDec = Integer.parseInt(rawData, 2);
                                                                                cardVal = Integer.toString(cardNoDec);
                                                                                editTextCardid.setText(cardVal);
                                                                                //  editTextName.setText("HTDYSCN24BHTD");
                                                                                break;
                                                                            case "2"://32 bit Hex to Dec
                                                                                break;
                                                                        }
                                                                        break;
                                                                }

                                                            }
                                                            break;
                                                        case "N":
                                                            new Thread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    ForlinxGPIO.runGPIOLEDForWiegandRead();
                                                                }
                                                            }).start();
                                                            cardVal = arr[1].trim().toUpperCase();
                                                            editTextCardid.setText(cardVal);
                                                            // editTextName.setText("HTDN");
                                                            break;
                                                    }
                                                }
                                                break;
                                            case Constants.WIEGAND_TYPE_2:
                                                info = dbComm.getWiegandSettings(info);
                                                if (info != null) {
                                                    boolean isValid = false;
                                                    String cardVal = "";
                                                    String rawData = "";
                                                    val = info.getIsHexToDecEnabled();
                                                    switch (val) {
                                                        case "Y":
                                                            rawData = arr[3].substring(0, 34);
                                                            try {
                                                                int data = Integer.parseInt(rawData, 2);
                                                                isValid = true;
                                                            } catch (NumberFormatException ne) {
                                                            }
                                                            if (isValid) {
                                                                new Thread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        ForlinxGPIO.runGPIOLEDForWiegandRead();
                                                                    }
                                                                }).start();
                                                                rawData = rawData.substring(1, 33);
                                                                val = info.getIsSiteCodeEnabled();
                                                                switch (val) {
                                                                    case "Y":
                                                                        String siteCode, cardNo;
                                                                        int siteCodeDec, cardNoDec;
                                                                        val = info.getCardNoType();
                                                                        switch (val) {
                                                                            case "0"://16 bit Hex to Dec
                                                                                rawData = rawData.substring(16);
                                                                                cardNoDec = Integer.parseInt(rawData, 2);
                                                                                cardVal = Integer.toString(cardNoDec);
                                                                                editTextCardid.setText(cardVal);
                                                                                //  editTextName.setText("HTDYSCY16BHTD");
                                                                                break;
                                                                            case "1"://24 bit Hex to Dec
                                                                                rawData = rawData.substring(8);
                                                                                cardNoDec = Integer.parseInt(rawData, 2);
                                                                                cardVal = Integer.toString(cardNoDec);
                                                                                editTextCardid.setText(cardVal);
                                                                                //  editTextName.setText("HTDYSCY24BHTD");
                                                                                break;
                                                                            case "2"://32 bit Hex to Dec
                                                                                rawData = rawData.substring(0);
                                                                                cardNoDec = Integer.parseInt(rawData, 2);
                                                                                cardVal = Integer.toString(cardNoDec);
                                                                                editTextCardid.setText(cardVal);
                                                                                //   editTextName.setText("HTDYSCY32BHTD");
                                                                                break;
                                                                            case "3":// Site Code + 16 bit
                                                                                siteCode = rawData.substring(0, 8);
                                                                                cardNo = rawData.substring(16);
                                                                                siteCodeDec = Integer.parseInt(siteCode, 2);
                                                                                cardNoDec = Integer.parseInt(cardNo, 2);
                                                                                cardVal = Integer.toString(siteCodeDec) + Integer.toString(cardNoDec);
                                                                                editTextCardid.setText(cardVal);
                                                                                //  editTextName.setText("HTDYSCYSC16BHTD");
                                                                                break;
                                                                            case "4"://Site Code + 24 bit
                                                                                siteCode = rawData.substring(0, 8);
                                                                                cardNo = rawData.substring(8);
                                                                                siteCodeDec = Integer.parseInt(siteCode, 2);
                                                                                cardNoDec = Integer.parseInt(cardNo, 2);
                                                                                cardVal = Integer.toString(siteCodeDec) + Integer.toString(cardNoDec);
                                                                                editTextCardid.setText(cardVal);
                                                                                //   editTextName.setText("HTDYSCYSC24BHTD");
                                                                                break;

                                                                        }
                                                                        break;
                                                                    case "N":
                                                                        val = info.getCardNoType();
                                                                        switch (val) {
                                                                            case "0"://16 bit Hex to Dec
                                                                                rawData = rawData.substring(16);
                                                                                cardNoDec = Integer.parseInt(rawData, 2);
                                                                                cardVal = Integer.toString(cardNoDec);
                                                                                editTextCardid.setText(cardVal);
                                                                                // editTextName.setText("HTDYSCN16BHTD");
                                                                                break;
                                                                            case "1"://24 bit Hex to Dec
                                                                                rawData = rawData.substring(8);
                                                                                cardNoDec = Integer.parseInt(rawData, 2);
                                                                                cardVal = Integer.toString(cardNoDec);
                                                                                editTextCardid.setText(cardVal);
                                                                                // editTextName.setText("HTDYSCN24BHTD");
                                                                                break;
                                                                            case "2"://32 bit Hex to Dec
                                                                                rawData = rawData.substring(0);
                                                                                cardNoDec = Integer.parseInt(rawData, 2);
                                                                                cardVal = Integer.toString(cardNoDec);
                                                                                editTextCardid.setText(cardVal);
                                                                                // editTextName.setText("HTDYSCN32BHTD");
                                                                                break;
                                                                        }
                                                                        break;
                                                                }
                                                            }
                                                            break;
                                                        case "N":
                                                            new Thread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    ForlinxGPIO.runGPIOLEDForWiegandRead();
                                                                }
                                                            }).start();
                                                            cardVal = arr[1].trim().toUpperCase();
                                                            editTextCardid.setText(cardVal);
                                                            // editTextName.setText("HTDN");
                                                            break;
                                                    }
                                                }
                                                break;
                                        }
                                    }
                                }
                                isWiegandReading = false;
                            }
                        }
                    }
                });
            }
        };

        capReadTimerTask = new TimerTask() {
            public void run() {
                cHandler.post(new Runnable() {
                    public void run() {
                        char[] val = I2CCommunicator.readI2C(Constants.CAP_READ_PATH);
                        if (val != null && val.length > 0) {
                            String capVal = new String(val);
                            capVal = capVal.trim();
                            switch (capVal) {
                                case "36":
                                    if (isBreakFound) {
                                        isBreakFound = false;
                                        if (isLCDBackLightOff) {
                                            stopHandler();//stop first and then start
                                            startHandler();
                                            ForlinxGPIO.setLCDBackLightOn();
                                            setScreenBrightness(Constants.BRIGHTNESS_ON);
                                            isLCDBackLightOff = false;
                                        } else {
                                            Intent intent = new Intent(EmployeeEnrollmentFirstActivity.this, EmployeeAttendanceActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }
                                    break;
                                case "63":
                                    if (isBreakFound) {
                                        isBreakFound = false;
                                        if (isLCDBackLightOff) {
                                            stopHandler();//stop first and then start
                                            startHandler();
                                            ForlinxGPIO.setLCDBackLightOn();
                                            setScreenBrightness(Constants.BRIGHTNESS_ON);
                                            isLCDBackLightOff = false;
                                        } else {
                                            Intent intent = new Intent(EmployeeEnrollmentFirstActivity.this, HomeActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }
                                    break;
                                case "33":
                                    break;
                                case "66":
                                    break;
                                case "ff":
                                    isBreakFound = true;
                                    break;
                            }
                        }
                    }
                });
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        stopHandler();
        startHandler();
        startTimer();

//        byte[] jpegData = PhotoData.getInstance().getCapturedPhotoData();
//        if (jpegData != null) {
//            Bitmap bmp = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length);
//            Bitmap resized = Bitmap.createScaledBitmap(bmp, 120, 135, true);
//            image.setImageBitmap(resized);
//            Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
//            ByteArrayOutputStream stream = new ByteArrayOutputStream();
//            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
//            byteimage = stream.toByteArray();
//            if (byteimage != null) {
//                image.setImageBitmap(BitmapFactory.decodeByteArray(byteimage, 0, byteimage.length));
//            } else {
//                image.setImageResource(R.drawable.dummyphoto);
//            }
//            PhotoData.getInstance().setCapturedPhotoData(null);
//        } else {
//            image.setImageResource(R.drawable.dummyphoto);
//        }
    }

    public void startTimer() {
        if (wigReadTimer == null && capReadTimer == null) {
            wigReadTimer = new Timer();
            capReadTimer = new Timer();
            initializeTimerTask();
            wigReadTimer.schedule(wigReadTimerTask, 0, 300);//50
            capReadTimer.schedule(capReadTimerTask, 0, 250);//50
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopHandler();
        stopTimer();
    }

    public void stopTimer() {

        if (wigReadTimer != null) {
            wigReadTimer.cancel();
            wigReadTimer.purge();
            wigReadTimer = null;
        }
        if (capReadTimer != null) {
            capReadTimer.cancel();
            capReadTimer.purge();
            capReadTimer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LoginSplashActivity.isLoaded = false;
    }
}