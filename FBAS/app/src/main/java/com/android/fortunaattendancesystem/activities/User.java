package com.android.fortunaattendancesystem.activities;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.fortunaattendancesystem.R;
import com.android.fortunaattendancesystem.constant.Constants;
import com.android.fortunaattendancesystem.forlinx.ForlinxGPIO;
import com.android.fortunaattendancesystem.helper.VerhoeffAlgorithm;
import com.android.fortunaattendancesystem.submodules.I2CCommunicator;
import com.android.fortunaattendancesystem.submodules.SQLiteCommunicator;

import java.io.ByteArrayOutputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

public class User extends Activity {

    private static final int CAMERA_REQUEST = 1888;
    private static final String EMAIL_REGEX = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    ImageView image_photo;
    byte[] byteimage = null;
    EditText et_name, et_aadhaarid, et_mobileno, et_emailid, et_username, et_password, et_confirmpassword;
    CheckBox chk_isadmin;
    Button btn_Save;

    String strName, strAadhaarId, strMobileNo, strEmailId, strUsername, strPassword, strConfirmPassword, strIsAdmin;

    private static boolean isLCDBackLightOff = false;
    private Handler cHandler = new Handler();
    private Timer capReadTimer = null;
    private TimerTask capReadTimerTask = null;
    private boolean isBreakFound = true;

    SQLiteCommunicator dbComm = new SQLiteCommunicator();

    private Handler hBrightness, hLCDBacklight;
    private Runnable rBrightness, rLCDBacklight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        /*modifyActionBar();*/
        setContentView(R.layout.activity_user);

        //Added By Sanjay Shyamal 22/11/17
        findViewById(R.id.user_activity).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideSoftKeyboard(User.this);
                return false;
            }
        });

        findViewById(R.id.scrollView_user).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideSoftKeyboard(User.this);
                return false;
            }
        });

        initLayoutElements();
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

    //Added By Sanjay Shyamal 22/11/17
    public static void hideSoftKeyboard(Activity activity) {
        if (activity.getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    private void initLayoutElements() {
        image_photo = (ImageView) findViewById(R.id.photo);
        et_name = (EditText) findViewById(R.id.etname);
        et_aadhaarid = (EditText) findViewById(R.id.etaadhaarid);
        et_mobileno = (EditText) findViewById(R.id.etmobileno);
        et_emailid = (EditText) findViewById(R.id.etemailid);
        et_username = (EditText) findViewById(R.id.etusername);
        et_password = (EditText) findViewById(R.id.etpassword);
        et_confirmpassword = (EditText) findViewById(R.id.etconfirmpassword);
        chk_isadmin = (CheckBox) findViewById(R.id.role);
        btn_Save = (Button) findViewById(R.id.btnSave);

        btn_Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                boolean isvalid = false;
                isvalid = validate();
                if (isvalid) {
                    if (chk_isadmin.isChecked()) {
                        strIsAdmin = "Y";
                    } else {
                        strIsAdmin = "N";
                    }
                    int insertStatus = -1;
                    insertStatus = dbComm.insertUserDetails(strName, strAadhaarId, strMobileNo, strEmailId, strUsername, strPassword, strIsAdmin, byteimage);
                    if (insertStatus != -1) {
                        Toast.makeText(User.this, "Data Saved Successfully", Toast.LENGTH_LONG).show();
                        clear();
                    } else {
                        Toast.makeText(User.this, "Failed To Save Data", Toast.LENGTH_LONG).show();
                    }
                }

            }
        });

        et_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (charSequence.length() > 0) {
                    et_name.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        et_aadhaarid.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (charSequence.length() > 0) {
                    et_aadhaarid.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        et_mobileno.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (charSequence.length() > 0) {
                    et_mobileno.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        et_emailid.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (charSequence.length() > 0) {
                    et_emailid.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        et_username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (charSequence.length() > 0) {
                    et_username.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        et_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (charSequence.length() > 0) {
                    et_password.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        et_confirmpassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (charSequence.length() > 0) {
                    et_confirmpassword.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    private void clear() {
        et_name.setText("");
        et_aadhaarid.setText("");
        et_mobileno.setText("");
        et_emailid.setText("");
        et_username.setText("");
        et_password.setText("");
        et_confirmpassword.setText("");
        image_photo.setImageResource(R.drawable.dummyphoto);
    }

    private boolean validate() {

        boolean isvalid = true;

        strName = et_name.getText().toString().trim();
        strAadhaarId = et_aadhaarid.getText().toString().trim();
        strMobileNo = et_mobileno.getText().toString().trim();
        strEmailId = et_emailid.getText().toString().trim();
        strUsername = et_username.getText().toString().trim();
        strPassword = et_password.getText().toString().trim();
        strConfirmPassword = et_confirmpassword.getText().toString().trim();

        if (strName != null && strName.trim().length() == 0) {
            isvalid = false;
            et_name.requestFocus();
            et_name.setError("Name Cannot Be Left Blank");
            return isvalid;
        }

        if (strAadhaarId != null && strAadhaarId.trim().length() == 0) {
            et_aadhaarid.requestFocus();
            et_aadhaarid.setError("Aadhaar Id Cannot Be Left Blank");
            isvalid = false;
            return isvalid;
        }

        if (strAadhaarId.trim().length() < 12) {
            et_aadhaarid.requestFocus();
            et_aadhaarid.setError("Please Enter 12 digit Aadhaar Id");
            isvalid = false;
            return isvalid;
        }


        boolean isValidAadhaarId = VerhoeffAlgorithm.validateVerhoeff(strAadhaarId);
        if (!isValidAadhaarId) {
            et_aadhaarid.requestFocus();
            et_aadhaarid.setError("Please Enter A Valid Aadhaar Id");
            isvalid = false;
            return isvalid;
        }


        if (strMobileNo != null && strMobileNo.length() == 0) {
            et_mobileno.requestFocus();
            et_mobileno.setError("Mobile No Cannot Be Left Blank");
            isvalid = false;
            return isvalid;
        }

        if (strMobileNo.trim().length() < 10) {
            et_mobileno.requestFocus();
            et_mobileno.setError("Please Enter 10 Digit Mobile No");
            isvalid = false;
            return isvalid;
        }

        if (strEmailId != null && strEmailId.length() == 0) {
            et_emailid.requestFocus();
            et_emailid.setError("Email Id Cannot Be Left Blank");
            isvalid = false;
            return isvalid;
        }

        boolean isEmailIdValid = false;
        isEmailIdValid = validateEmailId(strEmailId);

        if (!isEmailIdValid) {
            et_emailid.requestFocus();
            et_emailid.setError("Please Enter A Valid Email Id");
            isvalid = false;
            return isvalid;
        }

        if (strUsername != null && strUsername.length() == 0) {
            et_username.requestFocus();
            et_username.setError("Username Cannot Be Left Blank");
            isvalid = false;
            return isvalid;
        }

        if (strUsername != null && strUsername.length() < 6) {
            et_username.requestFocus();
            et_username.setError("Username Should Be Of Minimum 6 Characters");
            isvalid = false;
            return isvalid;
        }


        boolean isExists = dbComm.checkUserName(strUsername);
        if (isExists) {
            et_username.requestFocus();
            et_username.setError("Username Already Exists ! Enter New Username");
            isvalid = false;
            return isvalid;
        }


        if (strPassword != null && strPassword.trim().length() == 0) {
            et_password.requestFocus();
            et_password.setError("Password Cannot Be Left Blank");
            isvalid = false;
            return isvalid;
        }

        if (strPassword != null && strPassword.length() < 6) {
            et_password.requestFocus();
            et_password.setError("Password Should Be Of Minimum 6 Characters");
            isvalid = false;
            return isvalid;
        }

        if (strConfirmPassword != null && strConfirmPassword.length() == 0) {
            et_confirmpassword.requestFocus();
            et_confirmpassword.setError("Confirm Password Cannot Be Left Blank");
            isvalid = false;
            return isvalid;
        }

        if (!strConfirmPassword.equals(strPassword)) {
            et_confirmpassword.requestFocus();
            et_confirmpassword.setError("Confirm Password Doesn't Match With Password");
            isvalid = false;
            return isvalid;
        }

        return isvalid;
    }

    public boolean validateEmailId(String strEmailId) {
        return Pattern.matches(EMAIL_REGEX, strEmailId);
    }

    public void modifyActionBar() {
        getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#e63900")));
        getActionBar().setTitle(Html.fromHtml("<b><font face='Calibri' color='#FFFFFF'>User Registration</font></b>"));
    }

    public void Capture(View view) {
        try {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, CAMERA_REQUEST);
            }
        } catch (SQLiteException e) {
            Toast.makeText(User.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            image_photo.setImageBitmap(photo);
            Bitmap bitmap = ((BitmapDrawable) image_photo.getDrawable()).getBitmap();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byteimage = stream.toByteArray();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        stopHandler();
        startHandler();
        startTimer();
    }

    private void startTimer() {
        if (capReadTimer == null) {
            capReadTimer = new Timer();
            initializeTimerTask();
            capReadTimer.schedule(capReadTimerTask, 0, 50);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopHandler();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
        LoginSplashActivity.isLoaded = false;
    }

    private void stopTimer() {
        if (capReadTimer != null) {
            capReadTimer.cancel();
            capReadTimer.purge();
            capReadTimer = null;
        }
    }


    private void initializeTimerTask() {
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
                                            Intent intent = new Intent(User.this, EmployeeAttendanceActivity.class);
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
                                            Intent intent = new Intent(User.this, HomeActivity.class);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_user, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.home) {
            Intent previous = new Intent(User.this, com.android.fortunaattendancesystem.activities.HomeActivity.class);
            startActivity(previous);
            overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            finish();
            return true;

        }
        if (id == R.id.refresh) {
            clear(); //Added By Sanjay Shyamal
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent menu = new Intent(User.this, HomeActivity.class);
            startActivity(menu);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
