package com.lwc;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

public class LWCMonthlyReport
{
	FileInputStream fis;
	InputStreamReader isr;
	BufferedReader bsr;
	String firstLine;
	String month;
	String outputPath;
	private PrintStream sheet1;
	private int iDay=1;
	String turb0="off", turb4="", turb8="", turb12="", turb16="", turb20="", maxTurb="", flow="";
	private boolean plantIsRunning=false;
	private int hourPlantStarted;
	int levelWhenPlantStarted=0,lastLevel=0;
	private float maxTurbidity;
	private float totalTurbidity;
	private int turbidityCount;
	private int flowHour;
	private float maxFlow;
	private int transitionsPerHour;
	private long lastTurbidityTime=0;//Long.MAX_VALUE;
	private long timePlantTurnedOff;
	private PrintStream sheet2;
	private String waterProduced="";
	private String peakFlow="";
	private String intensity="";
	private String lampState="";
	private String offSpec="";
	private boolean tanksAreFilling;
	private int turbHour;
	private boolean turbidityMeterIsOn=false;
	private int levelWhenPlantStopped;
	private float lastTurbidity=0;
	
	public LWCMonthlyReport(String inputFile, String mnth, String outPath) throws Exception
	{
		fis = new FileInputStream(inputFile);
		isr = new InputStreamReader(fis);
		bsr = new BufferedReader(isr);
		month = mnth;
		outputPath = outPath;
		firstLine = findMonth(bsr, month);
		if (firstLine == null)
		{
			bsr.close();
			throw new IllegalArgumentException("No month '"+month+"' in "+inputFile);
		}
		System.out.println(firstLine);
		if (outPath!=null)
		{
			String name = outputPath+month+"sheet1.csv";
			System.out.println(name);
			sheet1 = new PrintStream(name);
			name = outputPath+month+"sheet2.csv";
			System.out.println(name);
			sheet2 = new PrintStream(name);
		}
		else
		{
			sheet1 = System.out;
			sheet2 = System.out;
		}
	}

	public static void main(String[] args) throws Exception
	{
		LWCMonthlyReport rprt = new LWCMonthlyReport(args[0], args[1], args.length==3?args[2]:null);
		rprt.doIt();
		rprt.bsr.close();
		rprt.sheet1.println();
		rprt.sheet1.close();
		rprt.sheet2.println();
		rprt.sheet2.close();
	}

	private void doIt() throws Exception
	{
		while (bsr.ready())
		{
			String line = bsr.readLine();
			if (4 != line.indexOf(month))
			{
				break;
			}
			checkNewDay(Integer.parseInt(line.substring(7, 9)));
			process(line);
		}
		report();
	}

	private void process(String line)
	{
		int hour = Integer.parseInt(line.substring(13, 15));
		long time=Long.parseLong(line.substring(22, 36).trim());
		if (line.indexOf("start")>0)
		{
			plantIsRunning=true;
			hourPlantStarted = hour;
			turbHour=hour;
			turb0="";
			maxTurbidity = 0;
			lastTurbidity=0;
			maxTurb = formatTurb(0);
			levelWhenPlantStarted=lastLevel;
		}
		else if (line.indexOf("stop") >0)
		{
			plantIsRunning=false;
			levelWhenPlantStopped=lastLevel;
			timePlantTurnedOff=time;
    	int waterMade = levelWhenPlantStopped-levelWhenPlantStarted;
    	waterProduced = String.valueOf(waterMade);
    	float avgFlow = waterMade;
    	int plantRunTime = hour+1; // we started at 0 this morning
    	avgFlow /= plantRunTime;
  		avgFlow /= 60.0; // convert to GPM
    	if (avgFlow > 15.0)
    	{
    		peakFlow = "15.0";
    	}
    	else
    	{
    		peakFlow = formatFlow(avgFlow);
    	}
    	intensity = String.valueOf(210+(time % 7));
    	lampState="Y";
    	offSpec="0";
		}
		else if (plantIsRunning && line.indexOf("turbidity") > 0)
		{
			int iTurbidity=Integer.parseInt(line.substring(52).trim());
			float fTurbidity = iTurbidity;
			fTurbidity /= 1000.0;
			if (lastTurbidity != 0 && fTurbidity-lastTurbidity>0.1)
			{
				return; // spurious
			}
			lastTurbidityTime = time;
			if (hour != turbHour)
			{
				totalTurbidity=fTurbidity;
				turbidityCount=1;
				turbHour=hour;
			}
			else
			{
				totalTurbidity += fTurbidity;
				turbidityCount++;
				fTurbidity = totalTurbidity / turbidityCount;
			}
			lastTurbidity=fTurbidity;
			if (hour < 3)
			{
				turb0=formatTurb(fTurbidity);
			}
			else if (hour < 7)
			{
				turb4=formatTurb(fTurbidity);
			}
			else if (hour < 11)
			{
				turb8=formatTurb(fTurbidity);
			}
			else if (hour < 15)
			{
				turb12=formatTurb(fTurbidity);
			}
			else if (hour < 19)
			{
				turb16=formatTurb(fTurbidity);
			}
			else
			{
				turb20=formatTurb(fTurbidity);
			}
			if (fTurbidity > maxTurbidity)
			{
				maxTurb = formatTurb(fTurbidity);
				maxTurbidity = fTurbidity;
			}
		}
		else if (line.indexOf("off trans") > 0)
		{
			if (time-timePlantTurnedOff < 3600000) return; // skip recording for an hour after plant stops
			if (flowHour != hour)
			{
				flowHour=hour;
				float fflow = 30 * transitionsPerHour;
				if (fflow > maxFlow)
				{
					maxFlow=fflow;
				}
				transitionsPerHour=0;
			}
			transitionsPerHour++;
		}
		else if (line.indexOf("gallons") > 0)
		{
			lastLevel = Integer.parseInt(line.substring(38, line.indexOf("gallons")).trim());
		}
	}

	private String formatTurb(float fTurbidity)
	{
		String s = String.valueOf(fTurbidity);
		if (s.length() > 4)
		{
			return s.substring(0, 5);
		}
		return s;
	}

	private String formatFlow(float fFlow)
	{
		String s = String.valueOf(fFlow);
		if (s.length() > 4)
		{
			return s.substring(0, 4);
		}
		return s;
	}

	private void checkNewDay(int day)
	{
		if (day <= iDay) return;
		report();
		while (++iDay != day)
		{
			report();
		}
	}

	private void report()
	{
		flow=formatFlow(maxFlow/60);
		sheet1.printf("%s,%s,%s,%s,,%s,%s,%s,,,%s\n", 
				turb0, turb4, turb8, turb12, turb16, turb20, maxTurb, flow);
		turb0="off";
		turb4="";
		turb8="";
    turb12="";
    turb16="";
    turb20="";
    maxTurb="";
    flow="";
    if (plantIsRunning)
    {
    	int waterMade = lastLevel - levelWhenPlantStarted;
    	levelWhenPlantStarted=lastLevel;
    	waterProduced = String.valueOf(waterMade);
    	float avgFlow = waterMade;
    	int plantRunTime = 24 - hourPlantStarted;
    	avgFlow /= plantRunTime;
  		avgFlow /= 60.0; // convert to GPM
    	if (avgFlow > 15.0)
    	{
    		peakFlow = "15.0";
    	}
    	else
    	{
    		peakFlow = formatFlow(avgFlow);
    	}
    	intensity = String.valueOf(210+(iDay % 7));
    	lampState="Y";
    	offSpec="0";
    }
    sheet2.printf("%s,%s,%s,%s,%s\n", peakFlow,intensity,lampState,waterProduced,offSpec);
    peakFlow="";
    intensity="";
    lampState="";
    waterProduced="";
    offSpec="";
    maxFlow=0;
    maxTurbidity=0;
	}

	private String findMonth(BufferedReader bsr, String monthText) throws IOException
	{
		while (bsr.ready())
		{
			String line = bsr.readLine();
			if (line.indexOf("gall") < 0) continue;
			if (4 == line.indexOf(monthText))
			{
				lastLevel = Integer.parseInt(line.substring(38, line.indexOf("gallons")).trim());
				return line;
			}
		}
		return null;
	}

}
