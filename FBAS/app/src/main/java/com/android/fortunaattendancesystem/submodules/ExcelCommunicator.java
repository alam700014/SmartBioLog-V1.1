package com.android.fortunaattendancesystem.submodules;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.android.fortunaattendancesystem.constant.Constants;
import com.android.fortunaattendancesystem.R;
import com.android.fortunaattendancesystem.error.FBASErrorCodes;
import com.android.fortunaattendancesystem.helper.Utility;
import com.android.fortunaattendancesystem.helper.VerhoeffAlgorithm;
import com.android.fortunaattendancesystem.model.ExcelColumnInfo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import jxl.Cell;
import jxl.CellView;
import jxl.Sheet;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

/**
 * Created by fortuna on 22/9/18.
 */

public class ExcelCommunicator {

    Context context;

    public ExcelCommunicator(Context context) {
        this.context = context;
    }

    //================================= Create Fortuna Excel Format for Employee Details fill up ===============================================//

    public boolean exportDataToExcel(Uri uri) {

        boolean isFileCreated = true;

        String note = "Note: Please read the below points before filling up the excel\n" +
                "1) Employee Id,Card Id,Employee Name are mandatory fields and rest fields are optional\n" +
                "2) Employee Id should be alphanumeric and length should be of maximum 16 digits\n" +
                "3) Card Id should be numeric and length should be of maximum 8 digits\n" +
                "4) Employee Name should be alphabetic and length should be of maximum 15 digits\n" +
                "5) Aadhaar Id should be numeric and length should be of 12 digits\n" +
                "6) Blood Group accepted values are A+, A-, B+, B-, AB+, AB-, O+, O-\n" +
                "7) Mobile Number should be numeric and length should be of 10 digits\n" +
                "8) Email Id should be a valid email id\n" +
                "9) Valid Upto Date should be greater than or equal to current date and acceptable format is date-month-year example 04-05-2017\n" +
                "10) Date of Birth should be less than or equal to current date and acceptable format is date-month-year example 04-05-2017\n" +
                "11) Pin should be numeric and length should be of maximum 4 digits\n";

        ParcelFileDescriptor pfd = null;
        WritableWorkbook workbook = null;
        FileOutputStream fileOutputStream = null;
        WritableSheet sheet = null;

        try {

            pfd = context.getContentResolver().openFileDescriptor(uri, "w");
            fileOutputStream = new FileOutputStream(pfd.getFileDescriptor());
            workbook = Workbook.createWorkbook(fileOutputStream);
            sheet = workbook.createSheet("BasicEmployeeInfo", 0);

            try {

                WritableFont noteCellFont = new WritableFont(WritableFont.TIMES, 11);
                noteCellFont.setColour(Colour.BLACK);

                WritableCellFormat noteCellFormat = new WritableCellFormat(noteCellFont);
                noteCellFormat.setWrap(true);
                noteCellFormat.setBackground(Colour.GRAY_25);
                noteCellFormat.setAlignment(Alignment.LEFT);
                //noteCellFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
                noteCellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);


                sheet.mergeCells(0, 0, 11, 13);
                //This makes the cell at row1 and col1 span the next two cells
                //Write content at the marge cell
                sheet.addCell(new Label(0, 0, note, noteCellFormat));


                WritableFont headerFont = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD);
                headerFont.setColour(Colour.WHITE);

                WritableFont bodyCellFont = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD);
                bodyCellFont.setColour(Colour.BLACK);

                WritableCellFormat headerCellFormat = new WritableCellFormat(headerFont);
                headerCellFormat.setAlignment(Alignment.CENTRE);
                headerCellFormat.setWrap(true);
                headerCellFormat.setBackground(Colour.GRAY_80);
                headerCellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);


                WritableCellFormat bodyCellFormat = new WritableCellFormat(bodyCellFont);
                bodyCellFormat.setAlignment(Alignment.CENTRE);
                bodyCellFormat.setWrap(true);
                bodyCellFormat.setBackground(Colour.WHITE);
                bodyCellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);


                CellView headerCellView = new CellView();
                headerCellView.setFormat(headerCellFormat);
                headerCellView.setAutosize(true);


                CellView bodyCellView = new CellView();
                bodyCellView.setFormat(bodyCellFormat);
                bodyCellView.setAutosize(true);


                //Set cell width in CHARS

                String heading = "Employee Id";
                sheet.setColumnView(0, 18, bodyCellFormat);
                sheet.addCell(new Label(0, 15, heading, headerCellFormat));


                heading = "Card Id";
                sheet.setColumnView(1, bodyCellView);
                sheet.addCell(new Label(1, 15, heading, headerCellFormat));

                heading = "Employee Name";
                sheet.setColumnView(2, bodyCellView);
                sheet.addCell(new Label(2, 15, heading, headerCellFormat));

                heading = "Aadhaar Id";
                sheet.setColumnView(3, bodyCellView);
                sheet.addCell(new Label(3, 15, heading, headerCellFormat));

                heading = "Blood Group";
                sheet.setColumnView(4, bodyCellView);
                sheet.addCell(new Label(4, 15, heading, headerCellFormat));

                heading = "Mobile Number";
                sheet.setColumnView(5, bodyCellView);
                sheet.addCell(new Label(5, 15, heading, headerCellFormat));

                heading = "Email Id";
                sheet.setColumnView(6, bodyCellView);
                sheet.addCell(new Label(6, 15, heading, headerCellFormat));

                heading = "Valid Upto";
                sheet.setColumnView(7, bodyCellView);
                sheet.addCell(new Label(7, 15, heading, headerCellFormat));

                heading = "Date Of Birth";
                sheet.setColumnView(8, bodyCellView);
                sheet.addCell(new Label(8, 15, heading, headerCellFormat));

                heading = "Pin";
                sheet.setColumnView(9, bodyCellView);
                sheet.addCell(new Label(9, 15, heading, headerCellFormat));

                heading = "Save Status";
                sheet.setColumnView(10, bodyCellView);
                sheet.addCell(new Label(10, 15, heading, headerCellFormat));

                heading = "Failure Reason";
                sheet.setColumnView(11, bodyCellView);
                sheet.addCell(new Label(11, 15, heading, headerCellFormat));

            } catch (RowsExceededException e) {
                e.printStackTrace();
                isFileCreated = false;

            } catch (WriteException e) {
                e.printStackTrace();
                isFileCreated = false;
            }

            workbook.write();

        } catch (IOException e) {
            e.printStackTrace();
            isFileCreated = false;
        } finally {

            if (workbook != null) {
                try {
                    workbook.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    isFileCreated = false;
                } catch (WriteException e) {
                    e.printStackTrace();
                    isFileCreated = false;
                }
            }

            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    isFileCreated = false;
                }
            }
        }

        return isFileCreated;
    }


    //================================= Read Excel Sheet ===============================================//

    public ArrayList <ArrayList <ExcelColumnInfo>> importDataFromExcelToList(Uri uri,ArrayList <ArrayList <ExcelColumnInfo>> rowDetailsList) {

        ArrayList <ExcelColumnInfo> columnDetailsList = new ArrayList <ExcelColumnInfo>();
        Workbook readWorkBook = null;

        try {
            String strPath = getPath(uri);
            File file = new File(strPath);
            readWorkBook = Workbook.getWorkbook(file);
            Sheet sheet = readWorkBook.getSheet(0);
            int errorCode = -1;
            String strCellData = "";
            String errorDesc = "";
            ExcelColumnInfo columnDetails;
            int sheetRows = sheet.getRows();
            int sheetColumns = sheet.getColumns();
            for (int readRow = 16; readRow < sheetRows; readRow++) {
                columnDetailsList = new ArrayList <ExcelColumnInfo>();
                for (int readColumn = 0; readColumn < sheetColumns - 2; readColumn++) {
                    columnDetails = new ExcelColumnInfo();
                    Cell cell = sheet.getCell(readColumn, readRow);
                    strCellData = cell.getContents().trim();
                    errorCode = validate(readColumn, strCellData);
                    errorDesc = getErrorDescription(errorCode);
                    columnDetails.setRowNo(readRow);
                    columnDetails.setColumnNo(readColumn);
                    columnDetails.setStrCellData(strCellData);
                    columnDetails.setStrStatus(errorDesc);
                    columnDetailsList.add(columnDetails);
                }
                rowDetailsList.add(columnDetailsList);
            }
            readWorkBook.close();
        } catch (BiffException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (readWorkBook != null) {
                readWorkBook.close();
            }
        }
        return rowDetailsList;
    }

    public void validateModifyExcel(Uri uri, ArrayList <ArrayList <ExcelColumnInfo>> rowDetailsList) {

        WritableWorkbook writeWorkbook = null;
        WritableSheet writableSheet = null;

        try {

            ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "w");
            FileOutputStream fileOutputStream = new FileOutputStream(pfd.getFileDescriptor());

            writeWorkbook = Workbook.createWorkbook(fileOutputStream);
            writableSheet = writeWorkbook.createSheet("BasicEmployeeInfo", 0);

            createBasicFormat(writableSheet);

            int rowsToRead = rowDetailsList.size();
            int totalColumns = 12;

            ArrayList <ExcelColumnInfo> columnDetailsList = null;
            Label label = null;

            WritableFont cellFont = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD);
            cellFont.setColour(Colour.BLACK);

            WritableCellFormat success = new WritableCellFormat(cellFont);
            success.setAlignment(Alignment.CENTRE);
            success.setWrap(true);
            success.setBackground(Colour.WHITE);
            success.setBorder(Border.ALL, BorderLineStyle.THIN);

            WritableCellFormat failure = new WritableCellFormat(cellFont);
            failure.setAlignment(Alignment.CENTRE);
            failure.setWrap(true);
            failure.setBackground(Colour.RED);
            failure.setBorder(Border.ALL, BorderLineStyle.THIN);
            failure.setBorder(Border.ALL, BorderLineStyle.THICK, Colour.RED);

            boolean isValidData = true;
            StringBuffer columnFailureReason = null;
            for (int i = 0; i < rowsToRead; i++) {
                columnFailureReason = new StringBuffer();
                isValidData = true;
                columnDetailsList = rowDetailsList.get(i);
                ExcelColumnInfo columnData = null;
                for (int j = 0; j < totalColumns - 2; j++) {
                    columnData = columnDetailsList.get(j);
                    int row = columnData.getRowNo();
                    int column = columnData.getColumnNo();
                    String cellData = columnData.getStrCellData();
                    String status = columnData.getStrStatus();
                    if (status.equals(FBASErrorCodes.ERROR_OK)) {
                        label = new Label(column, row, cellData, success);
                        writableSheet.addCell(label);
                    } else {
                        columnFailureReason.append(status + "\n");
                        label = new Label(column, row, cellData, failure);
                        writableSheet.addCell(label);
                        isValidData = false;
                    }
                }
                if (!isValidData) {
                    if (columnData != null) {
                        int row = columnData.getRowNo();
                        label = new Label(10, row, "Failure", success);
                        writableSheet.addCell(label);
                        label = new Label(11, row, columnFailureReason.toString(), success);
                        writableSheet.addCell(label);
                    }
                } else {
                    if (columnData != null) {
                        int row = columnData.getRowNo();
                        label = new Label(10, row, "Success", success);
                        writableSheet.addCell(label);
                        columnDetailsList = rowDetailsList.get(i);
                        ArrayList <String> validRowData = new ArrayList <String>();
                        for (int counter = 0; counter < columnDetailsList.size(); counter++) {
                            String cellData = columnDetailsList.get(counter).getStrCellData().trim();
                            if (cellData != null && cellData.length() > 0) {
                                validRowData.add(cellData);
                            } else {
                                validRowData.add("");
                            }
                        }

                        if (validRowData != null && validRowData.size() > 0) {
                            int insertStatus = -1;
                            SQLiteCommunicator dbComm=new SQLiteCommunicator();
                            insertStatus=dbComm.insertEmployeeDataFromExcel(validRowData);
                        }
                    }
                }
            }
            writeWorkbook.write();
            writeWorkbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RowsExceededException e) {
            e.printStackTrace();
        } catch (WriteException e) {
            e.printStackTrace();
        }
    }

    private void createBasicFormat(WritableSheet writableSheet) {

        String note = "Note: Please read the below points before filling up the excel\n" +
                "1) Employee Id,Card Id,Employee Name are mandatory fields and rest fields are optional\n" +
                "2) Employee Id should be alphanumeric and length should be of maximum 16 digits\n" +
                "3) Card Id should be numeric and length should be of maximum 8 digits\n" +
                "4) Employee Name should be alphabetic and length should be of maximum 15 digits\n" +
                "5) Aadhaar Id should be numeric and length should be of 12 digits\n" +
                "6) Blood Group accepted values are A+, A-, B+, B-, AB+, AB-, O+, O-\n" +
                "7) Mobile Number should be numeric and length should be of 10 digits\n" +
                "8) Email Id should be a valid email id\n" +
                "9) Valid Upto Date should be greater than or equal to current date and acceptable format is date-month-year example 04-05-2017\n" +
                "10) Date of Birth should be less than or equal to current date and acceptable format is date-month-year example 04-05-2017\n" +
                "11) Pin should be numeric and length should be of maximum 4 digits\n";


        WritableFont noteCellFont = null;
        WritableCellFormat noteCellFormat = null;
        WritableFont headerFont = null;
        WritableFont bodyCellFont = null;

        WritableCellFormat headerCellFormat = null;
        WritableCellFormat bodyCellFormat = null;

        try {

            noteCellFont = new WritableFont(WritableFont.TIMES, 11);
            noteCellFont.setColour(Colour.BLACK);

            noteCellFormat = new WritableCellFormat(noteCellFont);
            noteCellFormat.setWrap(true);
            noteCellFormat.setBackground(Colour.GRAY_25);
            noteCellFormat.setAlignment(Alignment.LEFT);
            noteCellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);

            writableSheet.mergeCells(0, 0, 11, 13);
            writableSheet.addCell(new Label(0, 0, note, noteCellFormat));

            headerFont = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD);
            headerFont.setColour(Colour.WHITE);

            bodyCellFont = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD);
            bodyCellFont.setColour(Colour.BLACK);

            headerCellFormat = new WritableCellFormat(headerFont);
            headerCellFormat.setAlignment(Alignment.CENTRE);
            headerCellFormat.setWrap(true);
            headerCellFormat.setBackground(Colour.GRAY_80);
            headerCellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);

            bodyCellFormat = new WritableCellFormat(bodyCellFont);
            bodyCellFormat.setAlignment(Alignment.CENTRE);
            bodyCellFormat.setWrap(true);
            bodyCellFormat.setBackground(Colour.WHITE);
            bodyCellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);

            CellView headerCellView = new CellView();
            headerCellView.setFormat(headerCellFormat);
            headerCellView.setAutosize(true);


            CellView bodyCellView = new CellView();
            bodyCellView.setFormat(bodyCellFormat);
            bodyCellView.setAutosize(true);


            //Set cell width in CHARS

            String heading = "Employee Id";
            writableSheet.setColumnView(0, 18, bodyCellFormat);
            writableSheet.addCell(new Label(0, 15, heading, headerCellFormat));


            heading = "Card Id";
            writableSheet.setColumnView(1, bodyCellView);
            writableSheet.addCell(new Label(1, 15, heading, headerCellFormat));

            heading = "Employee Name";
            writableSheet.setColumnView(2, bodyCellView);
            writableSheet.addCell(new Label(2, 15, heading, headerCellFormat));

            heading = "Aadhaar Id";
            writableSheet.setColumnView(3, bodyCellView);
            writableSheet.addCell(new Label(3, 15, heading, headerCellFormat));

            heading = "Blood Group";
            writableSheet.setColumnView(4, bodyCellView);
            writableSheet.addCell(new Label(4, 15, heading, headerCellFormat));

            heading = "Mobile Number";
            writableSheet.setColumnView(5, bodyCellView);
            writableSheet.addCell(new Label(5, 15, heading, headerCellFormat));

            heading = "Email Id";
            writableSheet.setColumnView(6, bodyCellView);
            writableSheet.addCell(new Label(6, 15, heading, headerCellFormat));

            heading = "Valid Upto";
            writableSheet.setColumnView(7, bodyCellView);
            writableSheet.addCell(new Label(7, 15, heading, headerCellFormat));

            heading = "Date Of Birth";
            writableSheet.setColumnView(8, bodyCellView);
            writableSheet.addCell(new Label(8, 15, heading, headerCellFormat));

            heading = "Pin";
            writableSheet.setColumnView(9, bodyCellView);
            writableSheet.addCell(new Label(9, 15, heading, headerCellFormat));

            heading = "Save Status";
            writableSheet.setColumnView(10, bodyCellView);
            writableSheet.addCell(new Label(10, 15, heading, headerCellFormat));

            heading = "Failure Reason";
            writableSheet.setColumnView(11, bodyCellView);
            writableSheet.addCell(new Label(11, 15, heading, headerCellFormat));


        } catch (WriteException e) {
            e.printStackTrace();
        }

    }



    private String getErrorDescription(int errorCode) {

        switch (errorCode) {

            case FBASErrorCodes.ERROR_OK:
                return context.getString(R.string.FBAS_OK);
            case FBASErrorCodes.DUPLICATE_EMP_ID:
                return context.getString(R.string.DUPLICATE_EMP_ID);
            case FBASErrorCodes.INVALID_EMP_ID_LEN:
                return context.getString(R.string.INVALID_EMP_ID_LEN);
            case FBASErrorCodes.COMPULSARY_EMP_ID:
                return context.getString(R.string.COMPULSARY_EMP_ID);
            case  FBASErrorCodes.DUPLICATE_CARD_ID:
                return context.getString(R.string.DUPLICATE_CARD_ID);
            case FBASErrorCodes.CARD_ID_HOTLIST:
                return context.getString(R.string.CARD_ID_HOTLIST);
            case FBASErrorCodes.NON_NUMERIC_CARD_ID:
                return context.getString(R.string.NON_NUMERIC_CARD_ID);
            case FBASErrorCodes.INVALID_CARD_ID_LEN:
                return context.getString(R.string.INVALID_CARD_ID_LEN);
            case FBASErrorCodes.COMPULSARY_CARD_ID:
                return context.getString(R.string.COMPULSARY_CARD_ID);
            case FBASErrorCodes.INVALID_EMP_NAME_LEN:
                return context.getString(R.string.INVALID_EMP_NAME_LEN);
            case FBASErrorCodes.EMP_NAME_NUMERIC:
                return context.getString(R.string.EMP_NAME_NUMERIC);
            case FBASErrorCodes.COMPULSARY_EMP_NAME:
                return context.getString(R.string.COMPULSARY_EMP_NAME);
            case FBASErrorCodes.INVALID_AADHAAR_ID:
                return context.getString(R.string.INVALID_AADHAAR_ID);
            case FBASErrorCodes.NON_NUMERIC_AADHAAR_ID:
                return context.getString(R.string.NON_NUMERIC_AADHAAR_ID);
            case FBASErrorCodes.INVALID_AADHAAR_ID_LEN:
                return context.getString(R.string.INVALID_AADHAAR_ID_LEN);
            case FBASErrorCodes.INVALID_BLOOD_GROUP:
                return context.getString(R.string.INVALID_BLOOD_GROUP);
            case FBASErrorCodes.NON_NUMERIC_MOBILE_NO:
                return context.getString(R.string.NON_NUMERIC_MOBILE_NO);
            case FBASErrorCodes.INVALID_MOBILE_NO_LEN:
                return context.getString(R.string.INVALID_MOBILE_NO_LEN);
            case FBASErrorCodes.INVALID_EMAIL_ID:
                return context.getString(R.string.INVALID_EMAIL_ID);
            case FBASErrorCodes.INVALID_VALID_UPTO_DATE:
                return context.getString(R.string.INVALID_VALID_UPTO_DATE);
            case FBASErrorCodes.INVALID_DATE_OF_BIRTH:
                return context.getString(R.string.INVALID_DATE_OF_BIRTH);
            case FBASErrorCodes.NON_NUMERIC_PIN_NUMBER:
                return context.getString(R.string.NON_NUMERIC_PIN_NUMBER);
            case FBASErrorCodes.INVALID_PIN_NO_LEN:
                return context.getString(R.string.INVALID_PIN_NO_LEN);
            default:
                return context.getString(R.string.UNKNOWN_ERROR);
        }
    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) return null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String s = cursor.getString(column_index);
        cursor.close();
        return s;
    }

    private int validate(int column, String strCellData) {

        int ret = -1;
        switch (column) {

            case 0:

                //Employee Id validation

                if (strCellData.trim().length() > 0) {
                    if (strCellData.trim().length() <= 16) {
                        boolean isExists = false;
                        SQLiteCommunicator dbComm = new SQLiteCommunicator();
                        isExists = dbComm.isEmpIdEnrolled(strCellData);
                        if (!isExists) {
                            ret = 0;
                        } else {
                            ret = 1;
                        }
                    } else {
                        ret = 2;
                    }
                } else {
                    ret = 3;
                }

                break;

            case 1:

                // Card Id validation

                if (strCellData.trim().length() > 0) {
                    if (strCellData.trim().length() <= 8) {
                        boolean isDigit = TextUtils.isDigitsOnly(strCellData);
                        if (isDigit) {
                            boolean isCardIdHotlisted = false;
                            SQLiteCommunicator dbComm = new SQLiteCommunicator();
                            isCardIdHotlisted = dbComm.isCardIdHotlisted(strCellData);
                            if (!isCardIdHotlisted) {
                                boolean isExists = false;
                                isExists = dbComm.isCardDataAvailableInDatabase(strCellData);
                                if (!isExists) {
                                    ret = 0;
                                } else {
                                    ret = 4;
                                }
                            } else {
                                ret = 5;
                            }
                        } else {
                            ret = 6;
                        }
                    } else {
                        ret = 7;
                    }
                } else {
                    ret = 8;
                }

                break;


            case 2:

                //Employee Name Validation

                if (strCellData.length() > 0) {
                    boolean isDigit = TextUtils.isDigitsOnly(strCellData);
                    if (!isDigit) {
                        if(strCellData.length()<=16){
                            ret=0;
                        }else{
                            ret=9;
                        }
                    } else {
                        ret = 10;
                    }
                } else {
                    ret = 11;
                }

                break;

            case 3:

                //Aadhaar Id Validation

                if (strCellData.trim().length() > 0) {
                    if (strCellData.trim().length() <= 12) {
                        boolean isDigit = TextUtils.isDigitsOnly(strCellData);
                        if (isDigit) {
                            boolean isValid = false;
                            isValid = VerhoeffAlgorithm.validateVerhoeff(strCellData);
                            if (isValid) {
                                ret = 0;
                            } else {
                                ret = 12;
                            }
                        } else {
                            ret = 13;
                        }
                    } else {
                        ret = 14;
                    }
                } else {
                    ret = 0;
                }

                break;

            case 4:

                // Blood Group Validation

                if (strCellData.trim().length() > 0) {
                    int length = Constants.BLOOD_GROUPS.length;
                    boolean isValid = false;
                    for (int i = 0; i < length; i++) {
                        if (strCellData.equals(Constants.BLOOD_GROUPS[i])) {
                            isValid = true;
                        }
                    }
                    if (isValid) {
                        ret = 0;
                    } else {
                        ret = 15;
                    }
                } else {
                    ret = 0;
                }
                break;

            case 5:

                // Mobile Number Validation

                if (strCellData.trim().length() > 0) {
                    if (strCellData.trim().length() == 10) {
                        boolean isDigit = TextUtils.isDigitsOnly(strCellData);
                        if (isDigit) {
                            ret = 0;
                        } else {
                            ret = 16;
                        }
                    } else {
                        ret = 17;
                    }
                } else {
                    ret = 0;
                }

                break;


            case 6:

                // Email Id Validation

                if (strCellData.trim().length() > 0) {
                    boolean isValid = false;
                    isValid = Utility.validateEmailId(strCellData);
                    if (isValid) {
                        ret = 0;
                    } else {
                        ret = 18;
                    }
                } else {
                    ret = 0;
                }

                break;

            case 7:

                // Valid Upto Validation

                if (strCellData.trim().length() > 0) {
                    boolean isValid = false;
                    isValid = Utility.validateValidUptoDate(strCellData);
                    if (isValid) {
                        ret = 0;
                    } else {
                        ret = 19;
                    }
                } else {
                    ret = 0;
                }

                break;

            case 8:

                // DOB Validation

                if (strCellData.trim().length() > 0) {
                    boolean isValid = false;
                    isValid = Utility.validateBirthDate(strCellData);
                    if (isValid) {
                        ret = 0;
                    } else {
                        ret = 20;
                    }
                } else {
                    ret = 0;
                }

                break;

            case 9:

                // Pin Validation

                if (strCellData.trim().length() > 0) {
                    if (strCellData.trim().length() == 4) {
                        boolean isDigit = TextUtils.isDigitsOnly(strCellData);
                        if (isDigit) {
                            ret = 0;
                        } else {
                            ret = 21;
                        }
                    } else {
                        ret = 22;
                    }
                } else {
                    ret = 0;
                }
        }
        return ret;
    }


}
