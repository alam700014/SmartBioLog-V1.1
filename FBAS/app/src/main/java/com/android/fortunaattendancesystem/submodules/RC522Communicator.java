package com.android.fortunaattendancesystem.submodules;

import android.util.Log;

import com.android.fortunaattendancesystem.constant.Constants;
import com.android.fortunaattendancesystem.model.SmartCardInfo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by fortuna on 8/1/19.
 */

public class RC522Communicator {

    public boolean writeRC522(String command) {
//        boolean status = false;
//        File file = new File(Constants.WRITE_FILE_PATH);
//        BufferedWriter bw = null;
//        FileWriter fw = null;
//        try {
//            fw = new FileWriter(file);
//            bw = new BufferedWriter(fw);
//            bw.write(command);
//            bw.flush();
//            status = true;
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            if (fw != null) {
//                try {
//                    fw.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            if (bw != null) {
//                try {
//                    bw.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        return status;

//        boolean status=false;
//        RandomAccessFile memoryMappedFile = null;
//        FileChannel inChannel = null;
//        try {
//            memoryMappedFile = new RandomAccessFile(Constants.WRITE_FILE_PATH, "rw");
//            //Mapping a file into memory
//            inChannel = memoryMappedFile.getChannel();
//            MappedByteBuffer out = inChannel.map(FileChannel.MapMode.READ_WRITE, 0, inChannel.size());
//            out.put(command.getBytes());
//            status=true;
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//       return status;


//        boolean status=false;
//        FileOutputStream fileOutputStream = null;
//        FileChannel fileChannel=null;
//        File file=new File(Constants.WRITE_FILE_PATH);
//        try {
//            fileOutputStream = new FileOutputStream(
//                    file);
//            fileChannel = fileOutputStream.getChannel();
//            int size=(int)file.length();
//            ByteBuffer byteBuffer=ByteBuffer.allocate(size);
//            byteBuffer.flip();
//            fileChannel.write(byteBuffer);
//            status=true;
//        } catch (FileNotFoundException e) {
//            Log.d("TEST","Write File Not Found Exception:"+e.getMessage());
//            e.printStackTrace();
//        } catch (IOException e) {
//            Log.d("TEST","Write IO Exception:"+e.getMessage());
//            e.printStackTrace();
//        }finally {
//            try {
//                fileChannel.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        return status;

        //=============================== Direct BufferedOutputStream ==================================//

        boolean status = false;
        File file = new File(Constants.RC522_WRITE_FILE_PATH);
        if (file.exists()) {
            BufferedOutputStream outputStream = null;
            byte[] data = command.getBytes();
            int size = data.length;
            try {
                outputStream = new BufferedOutputStream(new FileOutputStream(file));
                outputStream.write(data, 0, size);
                outputStream.flush();
                status = true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return status;
    }


    public char[] readRC522() {
//        File file = new File(Constants.READ_FILE_PATH);
//        char[] readData = null;
//        FileReader fr = null;
//        try {
//            readData = new char[(int) file.length()];
//            fr = new FileReader(file);
//            fr.read(readData, 0, readData.length);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            if (fr != null) {
//                try {
//                    fr.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        return readData;


//        RandomAccessFile aFile = null;
//        FileChannel inChannel = null;
//        char[] convertedChar = null;
//        try {
//            aFile = new RandomAccessFile
//                    (Constants.READ_FILE_PATH, "r");
//
//            inChannel = aFile.getChannel();
//            MappedByteBuffer buffer = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, inChannel.size());
//            buffer.load();
//
//            byte[] data = new byte[(int) aFile.length()];
//            int size = data.length;
//            buffer.get(data, 0, size);
//            buffer.clear(); // do something with the data and clear/compact it.
//
//            convertedChar = new char[size];
//            for (int i = 0; i < size; i++) {
//                convertedChar[i] = (char) data[i];
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                inChannel.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            try {
//                aFile.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        return convertedChar;


//        FileInputStream fileInputStream = null;
//        FileChannel fileChannel=null;
//        char[] convertedChar=null;
//        File file=new File(Constants.READ_FILE_PATH);
//        try {
//            fileInputStream = new FileInputStream(
//                    file);
//            fileChannel = fileInputStream.getChannel();
//            int size=(int)file.length();
//            ByteBuffer byteBuffer=ByteBuffer.allocate(size);
//            fileChannel.read(byteBuffer);
//            byte[] data=new byte[size];
//            byteBuffer.flip();
//            byteBuffer.get(data,0,data.length);
//            convertedChar = new char[size];
//            for (int i = 0; i < size; i++) {
//                convertedChar[i] = (char) data[i];
//            }
//        } catch (FileNotFoundException e) {
//            Log.d("TEST","Read File Not Found Exception:"+e.getMessage());
//            e.printStackTrace();
//        } catch (IOException e) {
//            Log.d("TEST","Read IO Exception:"+e.getMessage());
//            e.printStackTrace();
//        }finally {
//            try {
//                fileChannel.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        return convertedChar;


//        CharBuffer charBuffer = null;
//        Path pathToRead = null;
//        try {
//            pathToRead = getFileURIFromResources(Constants.READ_FILE_PATH);
//
//            FileChannel fileChannel=null;
//
//
//
//            try (fileChannel (FileChannel)Files.newByteChannel(
//                    pathToRead, EnumSet.of(StandardOpenOption.READ))){
//
//
//                MappedByteBuffer mappedByteBuffer = fileChannel
//                        .map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
//
//                if (mappedByteBuffer != null) {
//                    charBuffer = Charset.forName("UTF-8").decode(mappedByteBuffer);
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }


        //=============================== Direct BufferedInputStream ==================================//

        byte[] byteData = null;
        char[] charData = null;
        File file = new File(Constants.RC522_READ_FILE_PATH);
        if (file.exists()) {
            BufferedInputStream inputStream = null;
            try {
                int size = (int) file.length();
                byteData = new byte[size];
                charData = new char[size];
                inputStream = new BufferedInputStream(new FileInputStream(file));
                inputStream.read(byteData, 0, byteData.length);
                // charData = byteData.toString().toCharArray();
                for (int i = 0; i < size; i++) {
                    charData[i] = (char) byteData[i];
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return charData;
    }

//    Path getFileURIFromResources(String fileName) throws Exception {
//        ClassLoader classLoader = getClass().getClassLoader();
//        return Paths.get(classLoader.getResource(fileName).getPath());
//    }


    //==================== Parse Sector Data Sector Wise and fill data to model SmartCardInfo  ==========================//

    public boolean parseSectorData(int sectorNo, byte[] sectorData, SmartCardInfo cardDetails) {

        boolean parseStatus = false;
        switch (sectorNo) {
            case 0:
                try {
                    byte[] fcsn = new byte[8];
                    int j = 0;
                    String strData = new String(sectorData);
                    String strCSN = strData.substring(1, 9).trim();
                    String strMADZero = strData.substring(33, 65).trim();
                    String strMADOne = strData.substring(65, 97).trim();
                    byte[] csn = strCSN.getBytes();
                    for (int i = 7; i > 0; i -= 2) {
                        fcsn[j] = csn[i - 1];
                        fcsn[j + 1] = csn[i];
                        j += 2;
                    }
                    strCSN = new String(fcsn);
                    cardDetails.setReadCSN(strCSN);
                    cardDetails.setMadZero(strMADZero);
                    cardDetails.setMadOne(strMADOne);
                    parseStatus = true;
                } catch (Exception e) {
                }
                break;
            case 1:
                break;
            case 2:
                try {
                    String str = new String(sectorData);
                    String strEmployeeId = str.substring(1, 17);//employee Id
                    String strName = str.substring(17, 33);     //name
                    String strValidUpto = str.substring(33, 39);   //validity date
                    String strBirthDay = str.substring(39, 45);    ////Date of Birth
                    String strSiteCode = str.substring(45, 47);  //Site code
                    String bloodgroup = str.substring(47, 48); //blood group

//                    String strEmployeeId = str.substring(0, 16);//employee Id
//                    String strName = str.substring(16, 32);     //name
//                    String strValidUpto = str.substring(32, 38);   //validity date
//                    String strBirthDay = str.substring(38, 44);    ////Date of Birth
//                    String strSiteCode = str.substring(44, 46);  //Site code
//                    String bloodgroup = str.substring(46, 47); //blood group

                    String strBloodGroup = "";

                    try {
                        strBloodGroup = Constants.BLOOD_GROUPS[Integer.parseInt(bloodgroup)];
                    } catch (NumberFormatException e) {

                    }
                    String strSmartCardVersion = str.substring(47, 48); //version information
                    cardDetails.setEmployeeId(strEmployeeId);
                    cardDetails.setEmpName(strName);
                    cardDetails.setValidUpto(strValidUpto);
                    cardDetails.setBirthDate(strBirthDay);
                    cardDetails.setSiteCode(strSiteCode);
                    cardDetails.setBloodGroup(strBloodGroup);
                    cardDetails.setSmartCardVer(strSmartCardVersion);
                    parseStatus = true;
                } catch (Exception e) {
                    Log.d("TEST", "Exception:" + e.getMessage());
                }
                break;
            case 3:
                break;
            case 4:
                try {
                    String template = "";
                    String str = new String(sectorData);
                    String temp1 = str.substring(1, 97);
                    template = template + temp1;
                    cardDetails.setFirstFingerTemplate(template);
                    parseStatus = true;
                } catch (Exception e) {
                }
                break;
            case 5:
                try {
                    String template = "";
                    String str = new String(sectorData);
                    String temp1 = str.substring(1, 97);
                    template = cardDetails.getFirstFingerTemplate();
                    template = template + temp1;
                    cardDetails.setFirstFingerTemplate(template);
                    parseStatus = true;
                } catch (Exception e) {
                }
                break;

            case 6:
                try {
                    String template = "";
                    String str = new String(sectorData);
                    String temp1 = str.substring(1, 97);
                    template = cardDetails.getFirstFingerTemplate();
                    template = template + temp1;
                    cardDetails.setFirstFingerTemplate(template);
                    parseStatus = true;
                } catch (Exception e) {
                }
                break;
            case 7:
                try {
                    String template = "";
                    String str = new String(sectorData);
                    String temp1 = str.substring(1, 97);
                    template = cardDetails.getFirstFingerTemplate();
                    template = template + temp1;
                    cardDetails.setFirstFingerTemplate(template);
                    parseStatus = true;
                } catch (Exception e) {
                }
                break;
            case 8:
                try {
                    String template = "";
                    String str = new String(sectorData);
                    String temp1 = str.substring(1, 97);
                    template = cardDetails.getFirstFingerTemplate();
                    template = template + temp1;
                    cardDetails.setFirstFingerTemplate(template);
                    parseStatus = true;
                } catch (Exception e) {
                }
                break;
            case 9:
                try {
                    String str = new String(sectorData);

                    String template = str.substring(1, 33); // 16 byte remaining template
                    template = cardDetails.getFirstFingerTemplate() + template;
                    cardDetails.setFirstFingerTemplate(template);

                    String firstFingerNo = "";
                    String firstFingerSecurityLevel = "";
                    String firstFingerIndex = "";
                    String firstFingerQuality = "";
                    String firstFingerVerificationMode = "";

                    firstFingerNo = str.substring(33, 35); // no of finger  -
                    firstFingerSecurityLevel = str.substring(35, 36); // security level F1_Slevel
                    firstFingerIndex = str.substring(36, 37); // finger index   F1_index
                    firstFingerQuality = str.substring(38, 39); //finger quality  F1_quality
                    firstFingerVerificationMode = str.substring(40, 41); // Verification mode Vmode

                    boolean isValid = false;
                    int index = -1;
                    try {
                        index = Integer.parseInt(firstFingerSecurityLevel);
                        isValid = true;
                    } catch (NumberFormatException e) {
                        isValid = false;
                    }

                    if (isValid && index < Constants.SECURITY_LEVELS.length) {
                        firstFingerSecurityLevel = Constants.SECURITY_LEVELS[index];
                    } else {
                        firstFingerSecurityLevel = "";
                    }

                    index = -1;
                    isValid = false;

                    if (!firstFingerIndex.equals("A")) {
                        try {
                            index = Integer.parseInt(firstFingerIndex);
                            isValid = true;
                        } catch (NumberFormatException e) {
                            isValid = false;
                        }
                    } else {
                        firstFingerIndex = Constants.FINGER_INDEXES[10];
                    }

                    if (isValid && index < Constants.FINGER_INDEXES.length) {
                        firstFingerIndex = Constants.FINGER_INDEXES[index];
                    } else {
                        firstFingerIndex = "";
                    }

                    index = -1;
                    isValid = false;

                    try {
                        index = Integer.parseInt(firstFingerQuality);
                        isValid = true;
                    } catch (NumberFormatException e) {
                        isValid = false;
                    }

                    if (isValid && index < Constants.QUALITY.length) {
                        firstFingerQuality = Constants.QUALITY[index];
                    } else {
                        firstFingerQuality = "";
                    }


                    index = -1;
                    isValid = false;

                    try {
                        index = Integer.parseInt(firstFingerVerificationMode);
                        isValid = true;
                    } catch (NumberFormatException e) {
                        isValid = false;
                    }

                    if (isValid && index < Constants.VERIFICATION_MODES.length) {
                        firstFingerVerificationMode = Constants.VERIFICATION_MODES[index];
                    } else {
                        firstFingerVerificationMode = "";
                    }

                    cardDetails.setFirstFingerNo(firstFingerNo);
                    cardDetails.setFirstFingerSecurityLevel(firstFingerSecurityLevel);
                    cardDetails.setFirstFingerIndex(firstFingerIndex);
                    cardDetails.setFirstFingerQuality(firstFingerQuality);
                    cardDetails.setFirstFingerVerificationMode(firstFingerVerificationMode);
                    parseStatus = true;
                } catch (Exception e) {
                }
                break;
            case 10:
                try {
                    String template = "";
                    String str = new String(sectorData);
                    String temp1 = str.substring(1, 97);
                    template = template + temp1;
                    cardDetails.setSecondFingerTemplate(template);
                    parseStatus = true;
                } catch (Exception e) {
                }
                break;
            case 11:
                try {
                    String template = "";
                    String str = new String(sectorData);
                    String temp1 = str.substring(1, 97);
                    template = cardDetails.getSecondFingerTemplate();
                    template = template + temp1;
                    cardDetails.setSecondFingerTemplate(template);
                    parseStatus = true;
                } catch (Exception e) {
                }
                break;
            case 12:
                try {
                    String template = "";
                    String str = new String(sectorData);
                    String temp1 = str.substring(1, 97);
                    template = cardDetails.getSecondFingerTemplate();
                    template = template + temp1;
                    cardDetails.setSecondFingerTemplate(template);
                    parseStatus = true;
                } catch (Exception e) {
                }
                break;
            case 13:
                try {
                    String template = "";
                    String str = new String(sectorData);
                    String temp1 = str.substring(1, 97);
                    template = cardDetails.getSecondFingerTemplate();
                    template = template + temp1;
                    cardDetails.setSecondFingerTemplate(template);
                    parseStatus = true;
                } catch (Exception e) {
                }
                break;
            case 14:
                try {
                    String template = "";
                    String str = new String(sectorData);
                    String temp1 = str.substring(1, 97);
                    template = cardDetails.getSecondFingerTemplate();
                    template = template + temp1;
                    cardDetails.setSecondFingerTemplate(template);
                    parseStatus = true;
                } catch (Exception e) {
                }
                break;
            case 15:
                try {
                    String template = "";
                    String str = new String(sectorData);
                    template = str.substring(1, 33); // 16 byte remaining template
                    template = cardDetails.getSecondFingerTemplate() + template;
                    cardDetails.setSecondFingerTemplate(template);

                    String secondFingerNo = "";
                    String secondFingerSecurityLevel = "";
                    String secondFingerIndex = "";
                    String secondFingerQuality = "";
                    String secondFingerVerificationMode = "";

                    secondFingerNo = str.substring(33, 35); // no of finger  -
                    secondFingerSecurityLevel = str.substring(35, 36); // security level F1_Slevel
                    secondFingerIndex = str.substring(36, 37); // finger index   F1_index
                    secondFingerQuality = str.substring(38, 39); //finger quality  F1_quality
                    secondFingerVerificationMode = str.substring(40, 41); // Verification mode Vmode

                    boolean isValid = false;
                    int index = -1;
                    try {
                        index = Integer.parseInt(secondFingerSecurityLevel);
                        isValid = true;
                    } catch (NumberFormatException e) {
                        isValid = false;
                    }

                    if (isValid && index < Constants.SECURITY_LEVELS.length) {
                        secondFingerSecurityLevel = Constants.SECURITY_LEVELS[index];
                    } else {
                        secondFingerSecurityLevel = "";
                    }

                    index = -1;
                    isValid = false;

                    if (!secondFingerIndex.equals("A")) {
                        try {
                            index = Integer.parseInt(secondFingerIndex);
                            isValid = true;
                        } catch (NumberFormatException e) {
                            isValid = false;
                        }
                    } else {
                        isValid = true;
                        index = 10;
                    }

                    if (isValid && index < Constants.FINGER_INDEXES.length) {
                        secondFingerIndex = Constants.FINGER_INDEXES[index];
                    } else {
                        secondFingerIndex = "";
                    }

                    index = -1;
                    isValid = false;

                    try {
                        index = Integer.parseInt(secondFingerQuality);
                        isValid = true;
                    } catch (NumberFormatException e) {
                        isValid = false;
                    }

                    if (isValid && index < Constants.QUALITY.length) {
                        secondFingerQuality = Constants.QUALITY[index];
                    } else {
                        secondFingerQuality = "";
                    }

                    index = -1;
                    isValid = false;

                    try {
                        index = Integer.parseInt(secondFingerVerificationMode);
                        isValid = true;
                    } catch (NumberFormatException e) {
                        isValid = false;
                    }

                    if (isValid && index < Constants.VERIFICATION_MODES.length) {
                        secondFingerVerificationMode = Constants.VERIFICATION_MODES[index];
                    } else {
                        secondFingerVerificationMode = "";
                    }

                    cardDetails.setSecondFingerNo(secondFingerNo);
                    cardDetails.setSecondFingerSecurityLevel(secondFingerSecurityLevel);
                    cardDetails.setSecondFingerIndex(secondFingerIndex);
                    cardDetails.setSecondFingerQuality(secondFingerQuality);
                    cardDetails.setSecondFingerVerificationMode(secondFingerVerificationMode);
                    parseStatus = true;
                } catch (Exception e) {
                }
                break;
            default:
                break;
        }
        return parseStatus;
    }
}
