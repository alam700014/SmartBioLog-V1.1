package com.forlinx.android;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import java.text.DecimalFormat;

//import device.v210.irda.Irda;
//import device.v210.temperature.Temperature;

public class GetValueService extends Service {
	private static UpdateTimeCallBack updatetimecallback = null;
	private boolean isPaused=false;
	private long begin=0;
	//private long spacetime=0;
	private long CurrentTimeMill=0;
	private long NextTimeMill=0;
	private Handler handler=null;
	
	private String mtype;
	private String maction;
	private	int mfd;
	//private	Adc adc;
	private boolean isSwitching=false;
	private String adc_short;
	private DecimalFormat df;
	private	double adcf;
//	private	Temperature temperature;
	//private	String temf;
	//private	Irda	irda;
	//private	int irda_fd=-1;
	//private	int adc_fd=-1;
	//private	int tem_fd=-1;
	//private	String irdaf;
	
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		handler=new Handler();
		mtype= (String) intent.getSerializableExtra("mtype");
		maction=(String) intent.getSerializableExtra("maction");
		mfd=intent.getIntExtra("mfd", -1);
		if((mtype!=null)&&(mfd>=0))
		{
			if(maction.equals("start")){
				begin(mtype,mfd);
			}else if(maction.equals("stop")){
				//stop(mtype);
			}else
			{
			}
		}else{
		}
		return super.onStartCommand(intent, flags, startId);
	}
	
	void begin(String apptype, int mfd){
		if(apptype.equals("ADC")){
			//adc = new Adc();
			//adc_fd=mfd;
			df=new DecimalFormat(".###");
			ValuePrepare(apptype);
			handler.postDelayed(updatetimecallback, 5);
			isSwitching=true;
			begin= System.currentTimeMillis();
			isPaused=false;
		}else if(apptype.equals("TEM")){
		/*	temperature = new Temperature();
			tem_fd=mfd;
			ValuePrepare(apptype);
			handler.postDelayed(updatetimecallback, 5);
			isSwitching=true;
			begin=System.currentTimeMillis();
			isPaused=false;*/
		}else if(apptype.equals("IRDA")){
		/*	irda = new Irda();
			irda_fd=mfd;
			ValuePrepare(apptype);
			handler.postDelayed(updatetimecallback, 5);
			isSwitching=true;
			begin=System.currentTimeMillis();
			isPaused=false;*/
		}else
		{}
	}
	
	/*void stop(String apptype){
		
			if(isSwitching){
				if(!isPaused){
					//stop
					handler.removeCallbacks(updatetimecallback);
					isPaused=true;
					spacetime=System.currentTimeMillis();
				}else{
					//continue
					handler.postDelayed(updatetimecallback, 5);
					isPaused=false;
					System.out.println("pause go on");
					begin=System.currentTimeMillis()-spacetime+begin;
				}
			}
	}*/
	
	private void ValuePrepare(String lrcname) {
		// TODO Auto-generated method stub
		try{
			updatetimecallback=new UpdateTimeCallBack();
			begin=0;
			CurrentTimeMill=0;
			NextTimeMill=0;
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	class UpdateTimeCallBack implements Runnable {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			long offset= System.currentTimeMillis()-begin;
			if(isSwitching){ 
				if(isPaused){
					offset=0;
				}
			}
			if(CurrentTimeMill==0){
				
				if(mtype.equals("ADC")){
					
			      //  adcf=adc.getAdcValue(adc_fd);
					adcf = HardwareInterface.readADC();
			        adc_short=df.format(adcf);

			        Intent intent=new Intent();
					intent.putExtra("adc_value",adc_short);
					intent.setAction("ADC_UPDATE");
					sendBroadcast(intent);
				}else if(mtype.equals("TEM")){

					/*temf = temperature.getTemperatureValue(tem_fd)+"";
			        Intent intent=new Intent();
					intent.putExtra("tem_value",temf);
					intent.setAction("TEM_UPDATE");
					sendBroadcast(intent);*/
				}else if(mtype.equals("IRDA")){

				/*	irdaf = irda.read(irda_fd)+"";
			        Intent intent=new Intent();
					intent.putExtra("irda_value",irdaf);
					intent.setAction("IRDA_UPDATE");
					sendBroadcast(intent);*/
				}else
				{}

				if(mtype.equals("ADC")){
					NextTimeMill=offset+500;
				}else if(mtype.equals("TEM")){
					NextTimeMill=offset+200;
				}else if(mtype.equals("IRDA")){
					NextTimeMill=offset+50;
				}else
				{}

			}
			else if(offset>NextTimeMill){

				if(mtype.equals("ADC")){
			        //adcf=adc.getAdcValue(adc_fd);
					adcf= HardwareInterface.readADC();
			        adc_short=df.format(adcf);
			        
			        Intent intent=new Intent();
					intent.putExtra("adc_value",adc_short);
					intent.setAction("ADC_UPDATE");
					sendBroadcast(intent);
				}else if(mtype.equals("TEM")){
					/*temf = temperature.getTemperatureValue(tem_fd)+"";
			        Intent intent=new Intent();
					intent.putExtra("tem_value",temf);
					intent.setAction("TEM_UPDATE");
					sendBroadcast(intent);*/
				}else if(mtype.equals("IRDA")){
					/*irdaf = irda.read(irda_fd)+"";
			        Intent intent=new Intent();
					intent.putExtra("irda_value",irdaf);
					intent.setAction("IRDA_UPDATE");
					sendBroadcast(intent);*/
				}else
				{}
				if(mtype.equals("ADC")){
					NextTimeMill=offset+500;
				}else if(mtype.equals("TEM")){
					NextTimeMill=offset+200;
				}else if(mtype.equals("IRDA")){
					NextTimeMill=offset+50;
				}else
				{}
				
			}
			CurrentTimeMill=CurrentTimeMill+10;
			handler.postDelayed(updatetimecallback, 10);
		}
	}
	
	@Override
	public void onDestroy() {  
		// TODO Auto-generated method stub
		super.onDestroy();
	/*	//if(irda_fd>0){
		//	irda.close(irda_fd);
		//}
		//if(adc_fd>0){
		//	adc.close(adc_fd);
		//}
		if(tem_fd>0){
			temperature.close(tem_fd);
		}*/
		
		handler.removeCallbacks(updatetimecallback);
	}
	
}
