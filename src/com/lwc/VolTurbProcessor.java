package com.lwc;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;

public class VolTurbProcessor
{
	FileInputStream fis; // = new FileInputStream(args[0]);
	InputStreamReader isr; // = new InputStreamReader(fis);
	BufferedReader bsr; // = new BufferedReader(isr);
	static SimpleDateFormat sdt = new SimpleDateFormat("E MM/dd/yy HH:mm:ss");
	
  public VolTurbProcessor(String[] args) throws Exception
  {
		fis = new FileInputStream(args[0]);
		isr = new InputStreamReader(fis);
		bsr = new BufferedReader(isr);
  	
  }
  
  Hashtable<String,String> hm=new Hashtable<String,String>();
  
  String getCorrectedLine(String line, String suffix) throws Exception
  {
  	return line+suffix;
/*
   	int gall = getGallFromLine(line);
  	return line.substring(0,38) + ((((gall*10)/59)*719)/100) + " gallons"+suffix;
*/
  }
  
  public static final long aDayInMsec=(24*60*60*1000);
  
  void getStartAndEndTimes() throws Exception
  {
  	long lastTurb=0;
  	long lastStop=0;
  	long lastStart=0;
  	int[] sample=new int[16];
  	int iSample=0;
  	int avg=0;
  	int total=0;
  	boolean fOn=false;
  	int month=13;
  	bsr.mark(30000000);
  	while (bsr.ready())
  	{
  		String line=bsr.readLine();
  		if (line.length() < 6) continue;
  		try
  		{
	  		if (month != Integer.parseInt(line.substring(4,6)))
	  		{
	  			month = Integer.parseInt(line.substring(4,6));
	  			if (fOn)
	  			{
	  				hm.put(getTsTextFromLine(line),line.substring(0, 36) + " start");
	  				System.err.println(getCorrectedLine(line," start"));
	  			}
	  		}
  		}
  		catch (NumberFormatException e)
  		{
  			continue;
  		}
  		if (line.contains("gall"))
  		{
  			long lineTime=Long.parseLong(getTsTextFromLine(line));
  			if (!fOn && (lineTime-lastTurb)>180000)
  			{
  				lastTurb=0;
  			}
  			int value=getGallFromLine(line);
  			total=total - sample[iSample] + value;
  			sample[iSample]=value;
  			iSample = (iSample+1)%16;
  			int newAvg=total/16;
  			int delta=newAvg-avg;
  			if (fOn)
  			{
  				if (delta<0 && (lineTime-lastTurb)>120000) // transition from on to off
  				{
	  				fOn=false;
	  				hm.put(getTsTextFromLine(line),line.substring(0, 36) + " stop");
	  				System.err.println(getCorrectedLine(line," stop"));
	  				lastTurb=0;
	  				lastStop=lineTime;
  				}
  			}
  			else if (delta>=0 && lastTurb!=0 && (lineTime-lastStop)>aDayInMsec) // transition from off to on
  			{
  				fOn=true;
  				hm.put(getTsTextFromLine(line),line.substring(0, 36) + " start");
  				System.err.println(getCorrectedLine(line," start"));
  				lastStart=lineTime;
  			}
  			avg=newAvg;
  		}
			else if (line.contains("turbidity"))
			{
				lastTurb=Long.parseLong(getTsTextFromLine(line));
			}
  	}
  	bsr.reset();
  }
  
  int lastGallons=0;
  
  // Tue 01/17/23 17:45:59  1674006359022 12 9411 gallons
  //                                        j    i
  private int getGallFromLine(String line) throws Exception
	{
		int i = new String(line).lastIndexOf(' ');
		int j = line.substring(0, i).lastIndexOf(' ');
		int gallons = Integer.parseInt(line.substring(j,i).trim());
		if (gallons > 24000) // get rid of spurious values
		{
			gallons=lastGallons;
		}
		else
		{
			lastGallons = gallons;
		}
		return gallons;
	}

	private String getTsTextFromLine(String line)
	{
		return line.substring(22, 36).trim();
	}

	void doIt() throws Exception
  {
		getStartAndEndTimes();
		long lastTurbidityTime=0;
		while (bsr.ready())
		{
			String line = bsr.readLine();
			if (line.length()<10) continue;
			if (line.contains("gall"))
			{
				System.out.println(line);
				if (hm.containsKey(this.getTsTextFromLine(line)))
				{
					System.out.println(hm.get(getTsTextFromLine(line)));
				}
			}
			else if (line.contains("turb"))
			{
				long time=Long.parseLong(line.substring(22, 36).trim());
				long diff=time-lastTurbidityTime;
				if (diff < 21600000) // meter is running but we may be missing data
				{
					if (diff > 120000) // we should have readings every minute
					{
						lastTurbidityTime += 60000;
						while (lastTurbidityTime <= time)
						{
							System.out.printf("%s %s %s\n", 
									sdt.format(new Date(lastTurbidityTime)),
									String.valueOf(lastTurbidityTime),
									line.substring(36));
							lastTurbidityTime += 60000;
						}
					}
				}
				lastTurbidityTime=time;
				System.out.println(line);
			}
			else
			{
				System.out.println(line);
			}
		}
		bsr.close();
  	
  }
  
	public static void main(String[] args) throws Exception
	{
		new VolTurbProcessor(args).doIt();
	}

}
