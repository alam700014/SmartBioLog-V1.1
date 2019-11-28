/*
* Copyright (C) 2015 LeMaker Community <support@lemaker.org>
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
 */

package com.friendlyarm.AndroidSDK;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import com.friendlyarm.AndroidSDK.ShellUtils.CommandResult;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class SimpleJNI extends Activity {

    public int flag = 0;
    public String TAG = "GPIO";
    public int BananaPro_port[] =          //BOARD MODE
        {
        -1, // 0
        -1, -1, //1, 2
        45, -1, //3, 4
        44, -1, //5, 6
        169, 171, //7, 8
        -1, 172, //9, 10
        220, 204, //11, 12
        219, -1, //13, 14
        218, 187, //15, 16
        -1, 188, //17, 18
        213, -1, //19, 20
        214, 217, //21, 22
        212, 211, //23, 24
        -1, 215, //25, 26
        202, 201, //27, 28
        27, -1, //29, 30
        222, 221, //31, 32
        37, -1, //33, 34
        31, 30, //35, 36
        29, 36, //37, 38
        -1, 32, //39, 40
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, //41-> 55
        -1, -1, -1, -1, -1, -1, -1, -1 // 56-> 63
        };
public int Guitar_port[] =          //BOARD MODE		
{
	-1,          // 0
	-1,     -1,   //1,           2
	131,    -1,   //3(SDA2),      4
	130,    -1,   //5(SCK2),      6
	50,     91,   //7(B18),       8(UART0_TX)
	-1,     90,   //9,           10(UART0_RX)
	64,     40,   //11(C0),      12(B8-PWM)
	65,    -1,   //13(C1),       14
	68,     25,   //15(C4),      16(A25)
	-1,     70,   //17,          18(B9)
	89,     -1,   //19(MISO),     20
	88,     69,   //21(MOSI),    22(C5)
	86,     87,   //23(SCLK),    24(B19)
	-1,     51,   //25,          26(CE0)
	48,     46,   //27(B16),     28(B14)
	47,     -1,   //29(B15),     30
	42,     45,   //31(B10),     32(B13)      
	32,     -1,   //33(B0),      34
	33,     28,   //35(B1),      36(A28)
	34,     31,   //37(B2),      38(A31)
	-1,     27,   //39,          40(A27)
	-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, //41-> 55
	-1, -1, -1, -1, -1, -1, -1, -1 // 56-> 63
} ;

    Handler handler = null;

    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TextView tv = new TextView(this);
		tv.setText("Led gpio test pins=[7,11,13,15,16,18,22]");
		setContentView(tv);
		/*修改export,unexport的权限为可读写，可执行权限*/
		String[] commands = new String[] { "chmod 0777 /sys/class/gpio/export", "chmod 0777 /sys/class/gpio/unexport"}; 
		CommandResult result = ShellUtils.execCommand(commands, true);
		/**********************************************/
		/*初始化指定数组里的管脚为OUTPUT状态*/
		init_Output_Export();
		/************************************/
		handler = new Handler();
		handler.postDelayed(Led, 1000);
    }
	
	Runnable Led = new Runnable(){
		public void run(){
			if(flag == 0){
				flag = 1;
				LedOpen();
			}else{
				flag = 0;
				LedClose();
			}
		handler.postDelayed(this, 1000);
		}
	};
//-------------------------------------------------------------------------------------
	public String readSysfs(String path) { //读取数据，并转化为String

		if (!new File(path).exists()) {
			Log.e(TAG, "File not found: " + path);
			return null;
		}

		String str = null;
		StringBuilder value = new StringBuilder();

		Log.i(TAG, "readSysfs path:" + path);

		try {
		FileReader fr = new FileReader(path);
		BufferedReader br = new BufferedReader(fr);
		try {
		while ((str = br.readLine()) != null) {
		if(str != null)
			value.append(str);
		};
		fr.close();
		br.close();
		if(value != null)
			return value.toString();
		else
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	public boolean writeSysfs(String path, String value) { //对文件进行写操作

        Log.i(TAG, "writeSysfs path:" + path + " value:" + value);

        if (!new File(path).exists()) {
            Log.e(TAG, "File not found: " + path);
            return false;
        }

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(path), 64);
            try {
                writer.write(value);
            } finally {
                writer.close();
            }
            return true;

        } catch (IOException e) {
            Log.e(TAG, "IO Exception when write: " + path, e);
            return false;
        }
    }
//-------------------------------------------------------------------------------------


	//初始化指定数组里的管脚为OUTPUT状态
    public void init_Output_Export(){
        int port[] = {7,11,13,15,16,18,22};
		for (int i = 0;i < 7;i++){
			writeSysfs("/sys/class/gpio/export",Integer.toString(Guitar_port[port[i]]));
			ShellUtils.execCommand("chmod 0777"+" /sys/class/gpio/gpio"+Integer.toString(Guitar_port[port[i]])+"/direction", true);
			writeSysfs("/sys/class/gpio/gpio"+Integer.toString(Guitar_port[port[i]])+"/direction","out");
			ShellUtils.execCommand("chmod 0777"+" /sys/class/gpio/gpio"+Integer.toString(Guitar_port[port[i]])+"/value", true);
		}
    }
	//初始化单个管脚为OUTPUT状态
	public void init_OutputPort_Export(int port){   
		writeSysfs("/sys/class/gpio/export",Integer.toString(Guitar_port[port]));
		ShellUtils.execCommand("chmod 0777"+" /sys/class/gpio/gpio"+Integer.toString(Guitar_port[port])+"/direction", true);
		ShellUtils.execCommand("chmod 0777"+" /sys/class/gpio/gpio"+Integer.toString(Guitar_port[port])+"/value", true);
		writeSysfs("/sys/class/gpio/gpio"+Integer.toString(Guitar_port[port])+"/direction","out");
		writeSysfs("/sys/class/gpio/gpio"+Integer.toString(Guitar_port[port])+"/value","0");
    }
	
	//初始化指定数组里的管脚为INPUT状态
	public void init_Input_Export(){
        int port[] = {7,11,13,15,16,18,22};
		for (int i = 0;i < 7;i++){
			writeSysfs("/sys/class/gpio/export",Integer.toString(Guitar_port[port[i]]));
			ShellUtils.execCommand("chmod 0777"+" /sys/class/gpio/gpio"+Integer.toString(Guitar_port[port[i]])+"/direction", true);			
			writeSysfs("/sys/class/gpio/gpio"+Integer.toString(Guitar_port[port[i]])+"/direction","in");
		}
    }
	//初始化单个管脚为INPUT状态
	public void init_InputPort_Export(int port){
		writeSysfs("/sys/class/gpio/export",Integer.toString(Guitar_port[port]));
		ShellUtils.execCommand("chmod 0777"+" /sys/class/gpio/gpio"+Integer.toString(Guitar_port[port])+"/direction", true);	
		writeSysfs("/sys/class/gpio/gpio"+Integer.toString(Guitar_port[port])+"/direction","in");
	}
	//获取指定INPUT管脚的值
	public int getInputVal(int port){
		return 	Integer.parseInt(readSysfs("/sys/class/gpio/gpio"+Integer.toString(Guitar_port[port])+"/value"));
	}
	
	//卸载指定数组里所以GPIO口
	public void unExport_port(){
		int port[] = {7,11,13,15,16,18,22};
		for (int i = 0;i < 7;i++){
			writeSysfs("/sys/class/gpio/unexport",Integer.toString(Guitar_port[port[i]]));
		}
	}
	
	//卸载指定GPIO口
	public void unExport_Only_port(int port){
		writeSysfs("/sys/class/gpio/unexport",Integer.toString(Guitar_port[port]));
	}
	
	
	//打开7,11,13,15,16,18,22号led灯
    public void LedOpen(){
        int port[] = {7,11,13,15,16,18,22};
	for(int i = 0;i < 7;i++){
                writeSysfs("/sys/class/gpio/gpio"+Integer.toString(Guitar_port[port[i]])+"/value","1");
        }
    }
	//关闭7,11,13,15,16,18,22号led灯
    public void LedClose(){
        int port[] = {7,11,13,15,16,18,22};
	for(int i = 0;i < 7;i++){
               writeSysfs("/sys/class/gpio/gpio"+Integer.toString(Guitar_port[port[i]])+"/value","0");
        }
    }
	
}
