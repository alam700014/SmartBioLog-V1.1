package com.android.fortunaattendancesystem.fm220;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.android.fortunaattendancesystem.extras.DataBaseLayer;
import com.android.fortunaattendancesystem.R;
import com.android.fortunaattendancesystem.activities.SmartCardActivity;
import com.android.fortunaattendancesystem.singleton.EmployeeFingerEnrollInfo;
import com.android.fortunaattendancesystem.singleton.UserDetails;
import com.android.fortunaattendancesystem.info.MorphoInfo;
import com.android.fortunaattendancesystem.info.ProcessInfo;

import java.util.ArrayList;

/**
 * Created by suman-dhara on 20/10/17.
 */

public class FM200PopUp{
    private final Context contex;
    private AlertDialog fingerUpdateAlert = null;
    private AlertDialog fingerEnrollAlert = null;
    private AlertDialog cardWriteAlert;
    private String strVerificationMode;
    private String strSecurityLevel;
    private String strPin;

    public FM200PopUp(Context context, String strVerificationMode, String strSecurityLevel, String strPin){
        this.contex = context;
    }

    public void fingerUpdateDialog(String title, String message, ArrayList<byte[]> templateList) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(contex);
        builder.setMessage("Finger Captured Successfully..Do You Want To Update Finger Data?").setTitle(title)
                .setIcon(R.drawable.success)
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        fingerUpdateAlert.dismiss();

                        String strRole = UserDetails.getInstance().getRole();

                        DataBaseLayer dbLayer = new DataBaseLayer();
                        int noOfFingers=-1 ;

                                ////FingerDataDetails.getInstance().getNoOfFingers();

                        if (noOfFingers == 1) {

                            if (strVerificationMode.equals("1:N")) {

                                int ret = 0;
                                //ret = updateOneTemplateToMorphoDB(templateList);

                                if (ret == 0) {

                                    boolean updateStatus = false;

                                   /// updateStatus = dbLayer.updateOneTemplateToDB(strVerificationMode, strSecurityLevel);

                                    if (updateStatus) {

                                     ///   FingerDataDetails.getInstance().setEnrollStatusValue(ret);
                                        if (strRole.equals("Y")) {
                                            //cardWriteConfirmDialog("Finger Update Status", "Finger Data Updated Successfully ! Do You Want To Write Data Into Smart Card ?");
                                        } else {
                                            showAlertDialog(contex, "Finger Update Status", "Finger Data Updated Successfully", true);
                                        }

                                    } else {

                                        showAlertDialog(contex, "Finger Update Status", "Failed To Update Finger Data", false);
                                    }
                                } else {
                                    int internalError = 0;//morphoDevice.getInternalError();
                                   //// String strEmpId = FingerDataDetails.getInstance().getStrEmpId();

                                   //// showAlertDialog(contex, "Finger Update Status", "Failed To Update Finger Data\nFailure Reason:" + convertToInternationalMessage(ret, internalError, strEmpId), false);
                                }

                            } else {

                                MorphoInfo morphoInfo = ProcessInfo.getInstance().getMorphoInfo();

                                if (((EmployeeFingerEnrollInfo) morphoInfo).isUpdateTemplate()) {
                                    if (((EmployeeFingerEnrollInfo) morphoInfo).getFingerIndex() == 1) {// Update first finger only
                                       //// FingerDataDetails.getInstance().setFingerIndexNo(1);
                                    } else if (((EmployeeFingerEnrollInfo) morphoInfo).getFingerIndex() == 2) {// Update second finger
                                      ////  FingerDataDetails.getInstance().setFingerIndexNo(2);
                                    }
                                }


                                boolean updateStatus = false;

                              ////  updateStatus = dbLayer.updateOneTemplateToDB(strVerificationMode, strSecurityLevel);

                                if (updateStatus) {

                                  ////  FingerDataDetails.getInstance().setEnrollStatusValue(0);
                                    if (strRole.equals("Y")) {
                                        cardWriteConfirmDialog("Finger Update Status", "Finger Data Updated Successfully ! Do You Want To Write Data Into Smart Card ?");
                                    } else {
                                        showAlertDialog(contex, "Finger Update Status", "Finger Data Updated Successfully", true);
                                    }

                                } else {

                                    showAlertDialog(contex, "Finger Update Status", "Failed To Update Finger Data", false);
                                }

                            }

                        } else if (noOfFingers == 2) {

                            if (strVerificationMode.equals("1:N")) {

                                int ret = 0;
                                //ret = updateTwoTemplateToMorphoDB(templateList);

                                Log.d("TEST", "Update Status:" + ret);

                                if (ret == 0) {

                                    boolean updateStatus = false;

                                 ////   updateStatus = dbLayer.updateTwoTemplateToDB(strVerificationMode, strSecurityLevel);

                                    if (updateStatus) {

                                     ////   FingerDataDetails.getInstance().setEnrollStatusValue(ret);
                                        if (strRole.equals("Y")) {
                                            cardWriteConfirmDialog("Finger Update Status", "Finger Data Updated Successfully ! Do You Want To Write Data Into Smart Card ?");
                                        } else {
                                            showAlertDialog(contex, "Finger Update Status", "Finger Data Updated Successfully", true);
                                        }

                                    } else {

                                        showAlertDialog(contex, "Finger Update Status", "Failed To Update Finger Data", false);
                                    }

                                } else {
                                    int internalError = 0;//morphoDevice.getInternalError();
                                   //// String strEmpId = FingerDataDetails.getInstance().getStrEmpId();
                                  ////  showAlertDialog(contex, "Finger Update Status", "Failed To Update Finger Data\nFailure Reason:" + convertToInternationalMessage(ret, internalError, strEmpId), false);
                                }

                            } else {

                                MorphoInfo morphoInfo = ProcessInfo.getInstance().getMorphoInfo();

                                if (((EmployeeFingerEnrollInfo) morphoInfo).isUpdateTemplate()) {

                                    if (((EmployeeFingerEnrollInfo) morphoInfo).getFingerIndex() == 1) {// Update first finger only

                                     ////   FingerDataDetails.getInstance().setFingerIndexNo(1);

                                    } else if (((EmployeeFingerEnrollInfo) morphoInfo).getFingerIndex() == 2) {// Update second finger

                                      ////  FingerDataDetails.getInstance().setFingerIndexNo(2);
                                    }
                                }

                                boolean updateStatus = false;

                              ////  updateStatus = dbLayer.updateOneTemplateToDB(strVerificationMode, strSecurityLevel);

                                if (updateStatus) {

                                  ////  FingerDataDetails.getInstance().setEnrollStatusValue(0);
                                    if (strRole.equals("Y")) {
                                        cardWriteConfirmDialog("Finger Update Status", "Finger Data Updated Successfully ! Do You Want To Write Data Into Smart Card ?");
                                    } else {
                                        showAlertDialog(contex, "Finger Update Status", "Finger Data Updated Successfully", true);
                                    }

                                } else {

                                    showAlertDialog(contex, "Finger Update Status", "Failed To Update Finger Data", false);
                                }

                            }

                        }
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.cancel();
                        //fingerUpdateAlert.dismiss();
                    }
                });
        Log.d("TEST","FM200PopUp : fingerUpdateDialog");
        fingerUpdateAlert = builder.create();
        fingerUpdateAlert.setCanceledOnTouchOutside(false);
        Log.d("TEST","fingerUpdateAlert show");
        fingerUpdateAlert.show();

    }

    protected void fingerEnrollDialog(String title, String message, final ArrayList<byte[]> templateList) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this.contex);
        builder.setMessage("Finger Captured Successfully..Do You Want To Save Finger Data?").setTitle(title)
                .setIcon(R.drawable.success)
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        fingerEnrollAlert.dismiss();

                        String strRole = UserDetails.getInstance().getRole();

                        boolean insertStatus = false;
                        DataBaseLayer dbLayer = new DataBaseLayer();

                       int noOfFingers = -1;

                      /// FingerDataDetails.getInstance().getNoOfFingers();

                        Log.d("TEST", "Number Of Fingers To be Enrolled:" + noOfFingers);

                        if (noOfFingers == 1) {

                            Log.d("TEST", "Verification Mode:" + strVerificationMode);

                            int ret = -1;

                            if (strVerificationMode.equalsIgnoreCase("1:N")) {

                                //ret = saveOneTemplateToMorphoDB(templateList);

                                Log.d("TEST", "Morpho Save Template Status:" + ret);

                                if (ret == 0) {

                                    insertStatus = dbLayer.saveOneEnrolledTemplate(strVerificationMode, strSecurityLevel);

                                    Log.d("TEST", "Sqlite Save Template Status:" + insertStatus);

                                    if (insertStatus) {

                                    ////    FingerDataDetails.getInstance().setEnrollStatusValue(ret);

                                        if (strRole.equals("Y")) {
                                            cardWriteConfirmDialog("Finger Save Status", "Finger Data Saved Successfully ! Do You Want To Write Data Into Smart Card ?");
                                        } else {
                                            showAlertDialog(contex, "Finger Save Status", "Finger Data Saved Successfully", true);
                                        }

                                    } else {
                                        showAlertDialog(contex, "Finger Save Status", "Failed To Save Finger Data", false);
                                    }

                                } else {

                                    int internalError = 0;//morphoDevice.getInternalError();
                                   //// String strEmpId = FingerDataDetails.getInstance().getStrEmpId();
                                   //// showAlertDialog(contex, "Finger Save Status", "Failed To Save Finger Data\nFailure Reason:" + convertToInternationalMessage(ret, internalError, strEmpId), false);
                                }

                            } else {

                                insertStatus = dbLayer.saveOneEnrolledTemplate(strVerificationMode, strSecurityLevel);
                                if (insertStatus) {

                                    if (strVerificationMode.equalsIgnoreCase("CARD+PIN+FINGER")) {

                                        if (strPin != null && strPin.trim().length() > 0) {

                                            int pinInsertStaus = -1;

                                            pinInsertStaus = dbLayer.insertCardVerficationPin(strPin);

                                            if (pinInsertStaus != -1) {

                                            ////    FingerDataDetails.getInstance().setEnrollStatusValue(0);

                                                if (strRole.equals("Y")) {
                                                    cardWriteConfirmDialog("Finger Save Status", "Finger And Pin Data Saved Successfully ! Do You Want To Write Data Into Smart Card ?");
                                                } else {
                                                    showAlertDialog(contex, "Finger Save Status", "Finger And Pin Data Saved Successfully", true);
                                                }

                                            } else {

                                                showAlertDialog(contex, "Finger Save Status", "Failed To Save Card Pin Data", false);
                                            }
                                        }
                                    } else {

                                     ////   FingerDataDetails.getInstance().setEnrollStatusValue(0);
                                        if (strRole.equals("Y")) {
                                            cardWriteConfirmDialog("Finger Save Status", "Finger Data Saved Successfully ! Do You Want To Write Data Into Smart Card ?");
                                        } else {
                                            showAlertDialog(contex, "Finger Save Status", "Finger Data Saved Successfully", true);
                                        }
                                    }

                                } else {
                                    showAlertDialog(contex, "Finger Save Status", "Failed To Save Finger Data", false);
                                }
                            }

                        } else if (noOfFingers == 2) {

                            Log.d("TEST", "Verification Mode:" + strVerificationMode);

                            int ret = -1;

                            if (strVerificationMode.equalsIgnoreCase("1:N")) {

                                //ret = saveTwoTemplateToMorphoDB(templateList);
                                Log.d("TEST", "Morpho Save Template Status:" + ret);

                                if (ret == 0) {

                                   //// insertStatus = dbLayer.saveTwoEnrolledTemplate(strVerificationMode, strSecurityLevel);

                                    Log.d("TEST", "Sqlite Save Template Status:" + insertStatus);

                                    if (insertStatus) {

                                     ////   FingerDataDetails.getInstance().setEnrollStatusValue(ret);
                                        if (strRole.equals("Y")) {
                                            cardWriteConfirmDialog("Finger Save Status", "Finger Data Saved Successfully ! Do You Want To Write Data Into Smart Card ?");
                                        } else {
                                            showAlertDialog(contex, "Finger Save Status", "Finger Data Saved Successfully", true);
                                        }

                                    } else {
                                        showAlertDialog(contex, "Finger Save Status", "Failed To Save Finger Data", false);
                                    }

                                } else {

                                    int internalError = 0;//morphoDevice.getInternalError();
                                   //// String strEmpId = FingerDataDetails.getInstance().getStrEmpId();
                                   //// showAlertDialog(contex, "Finger Save Status", "Failed To Save Finger Data\nFailure Reason:" + convertToInternationalMessage(ret, internalError, strEmpId), false);
                                }

                            } else {

                               //// insertStatus = dbLayer.saveTwoEnrolledTemplate(strVerificationMode, strSecurityLevel);
                                if (insertStatus) {

                                    if (strVerificationMode.equalsIgnoreCase("CARD+PIN+FINGER")) {

                                        if (strPin != null && strPin.trim().length() > 0) {

                                            int pinInsertStaus = -1;

                                            pinInsertStaus = dbLayer.insertCardVerficationPin(strPin);

                                            if (pinInsertStaus != -1) {

                                           ////     FingerDataDetails.getInstance().setEnrollStatusValue(0);
                                                if (strRole.equals("Y")) {
                                                    cardWriteConfirmDialog("Finger Save Status", "Finger And Pin Data Saved Successfully ! Do You Want To Write Data Into Smart Card ?");
                                                } else {
                                                    showAlertDialog(contex, "Finger Save Status", "Finger And Pin Data Saved Successfully", true);
                                                }

                                            } else {
                                                showAlertDialog(contex, "Finger Save Status", "Failed To Save Card Pin Data", false);
                                            }
                                        }

                                    } else {

                                       //// FingerDataDetails.getInstance().setEnrollStatusValue(0);
                                        if (strRole.equals("Y")) {
                                            cardWriteConfirmDialog("Finger Save Status", "Finger Data Saved Successfully ! Do You Want To Write Data Into Smart Card ?");
                                        } else {
                                            showAlertDialog(contex, "Finger Save Status", "Finger Data Saved Successfully", true);
                                        }
                                    }

                                } else {
                                    showAlertDialog(contex, "Finger Save Status", "Failed To Save Finger Data", false);
                                }
                            }
                        }

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        fingerEnrollAlert.dismiss();
                    }
                });
        fingerEnrollAlert = builder.create();
        fingerEnrollAlert.setCanceledOnTouchOutside(false);
        fingerEnrollAlert.show();
    }


    protected void cardWriteConfirmDialog(String title, String message) {

        AlertDialog.Builder builder = new AlertDialog.Builder(contex);
        builder.setMessage(message).setTitle(title)
                .setIcon(R.drawable.success)
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        cardWriteAlert.dismiss();

                   ////     String strEmpId = FingerDataDetails.getInstance().getStrEmpId();

                        Intent cardWriteActivity = new Intent(contex, SmartCardActivity.class);
                        Bundle extras = new Bundle();
                  ////      extras.putString("EmployeeId", strEmpId);
                        cardWriteActivity.putExtras(extras);
                        contex.startActivity(cardWriteActivity);

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        cardWriteAlert.dismiss();
                    }
                });

        cardWriteAlert = builder.create();
        cardWriteAlert.setCanceledOnTouchOutside(false);
        cardWriteAlert.show();

    }

    public void showAlertDialog(Context context, String title, String message, Boolean status) {

        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();

        alertDialog.setCanceledOnTouchOutside(false);

        alertDialog.setTitle(title);

        alertDialog.setMessage(message);

        alertDialog.setIcon((status) ? R.drawable.success : R.drawable.failure);

        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                //notifyEndProcess(false);
                alertDialog.cancel();
            }
        });

        alertDialog.show();
    }


    protected void alert(int codeError, int internalError, String title, String message, String strEmpId) {
        final AlertDialog alertDialog = new AlertDialog.Builder(this.contex).create();
        alertDialog.setCancelable(false);
        alertDialog.setTitle(title);
        alertDialog.setIcon(R.drawable.failure);
        String msg;
        if (codeError == 0) {
            msg = contex.getString(R.string.OP_SUCCESS);
        } else {
            String errorInternationalization = convertToInternationalMessage(codeError, internalError, strEmpId);
            msg = contex.getString(R.string.OP_FAILED) + "\n" + errorInternationalization;
        }
        msg += ((message.equalsIgnoreCase("")) ? "" : "\n" + message);

        alertDialog.setMessage(msg);

        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                //notifyEndProcess(false);
                alertDialog.cancel();
            }
        });

        alertDialog.show();
    }

    private String convertToInternationalMessage(int codeError, int internalError, String strEmpId) {
        return null;
    }

}
