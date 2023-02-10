package com.lwc;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class CurrentRecorder
{
	public static PrintStream rawStream=System.err;
	public static PrintStream cookedStream=System.out;
	public static String outpath;
	public static int month; 
	
	public static Socket getSocket(String host, int port)
	{
		System.out.println("Getting socket");
		for (;;)
		{
			Socket soc;
			try
			{
				soc=new Socket(host, port);
				System.out.println("got a socket");
				return soc;
			}
			catch (Exception e)
			{
				e.printStackTrace();
				try
				{
					Thread.sleep(10000);
				} catch (InterruptedException e1)
				{
				}
			}
		}
	}

	static final SimpleDateFormat dsf=new SimpleDateFormat("EEE MM/dd/yy HH:mm:ss ");

	public static void report(PrintStream stream, String msg, long elappsed)
	{
		String s = String.format("\n%s %s %s %s", 
				dsf.format(new Date()), 
				String.valueOf(System.currentTimeMillis()),
				String.valueOf(System.currentTimeMillis()-elappsed),
				msg);
		stream.println(s);
		stream.flush();
/*
		stream.println();
		stream.print(dsf.format(new Date()));
		stream.print(System.currentTimeMillis());
		stream.print(" ");
		stream.print(System.currentTimeMillis()-elappsed);
		stream.print(" ");
		stream.println(msg);
*/
	}

	public static void report(String msg, long elappsed)
	{
		report(cookedStream(), msg, elappsed);
	}
	
	private static PrintStream rawStream()
	{
		if (outpath==null)
		{
			rawStream=System.err;
		}
		else
		{
			Calendar cal = Calendar.getInstance();
			if (cal.get(Calendar.MONTH)!=month)
			{
				rotateStreams();
			}
		}
		return rawStream;
	}
	
	private static PrintStream cookedStream()
	{
		if (outpath==null)
		{
			cookedStream=System.out;
		}
		else
		{
			Calendar cal = Calendar.getInstance();
			if (cal.get(Calendar.MONTH)!=month)
			{
				rotateStreams();
			}
		}
		return cookedStream;
	}

	private static void rotateStreams()
	{
		Calendar cal = Calendar.getInstance();
		month=cal.get(Calendar.MONTH);
		try
		{
			if (cookedStream!=System.out)
			{
				cookedStream.close();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		try
		{
			if (rawStream!=System.err)
			{
				rawStream.close();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		String prefix=String.format("%s/%d-%d", outpath, month+1, cal.get(Calendar.YEAR));
		try
		{
			cookedStream=new PrintStream(new FileOutputStream(prefix+"-cooked.txt",true));
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
			cookedStream=System.out;
		}
		try
		{
			rawStream=new PrintStream(new FileOutputStream(prefix+"-raw.txt",true));
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
			rawStream=System.err;
		}
	}

	public static Socket soc;
	
	public static void doIt(String host, int port) throws Exception
	{
		month=13;
		soc=getSocket(host, port);
		try
		{
			boolean fOn=false;
			boolean fReportTankLevel=true;
			int tankLevelTotal=0;
			int tankLevelCount=0;
			InputStreamReader isr = new InputStreamReader(soc.getInputStream());
			long watchdog=System.currentTimeMillis();
			long elappsed=watchdog;
			for (;;)
			{
				char[] buff = new char[100];
				if (isr.ready())
				{
					watchdog=System.currentTimeMillis();
					int len=isr.read(buff);
					buff[len]=0;
					//System.out.println(len);
					report(rawStream(), new String(buff,0,len), watchdog);
					if (buff[0]=='t') continue;
					if (buff[0]=='r' || buff[0]=='T' || buff[0]=='C' || buff[0]=='P')
					{ // raw tank reading, turbidity, chlorine or pump setting message
						int i = new String(buff).lastIndexOf(' ');
						try
						{
							i = Integer.parseInt(new String(buff,i,buff.length-i).trim());
						}
						catch (NumberFormatException e)
						{
							continue;
						}
						if (buff[0]=='T')
						{
							if (i !=0)
							{
								report("raw turbidity "+i,System.currentTimeMillis());
								fReportTankLevel=true;
							}
							continue;
						}
						if (buff[0]=='C')
						{
							if (i !=0)
							{
								report("Chlorine "+i,System.currentTimeMillis());
								fReportTankLevel=true;
							}
							continue;
						}
						if (buff[0]=='P')
						{
							if (i !=0)
							{
								report("Pump speed "+i,System.currentTimeMillis());
								fReportTankLevel=true;
							}
							continue;
						}
						tankLevelCount++;
						tankLevelTotal+=i;
						if (fReportTankLevel)
						{
							report("raw tank level "+i,watchdog);
							report(String.valueOf(((tankLevelTotal / tankLevelCount) * 719) / 100)+" gallons", watchdog);
							tankLevelCount=0;
							tankLevelTotal=0;
							fReportTankLevel=false;
						}
						continue;
					}
					if (fOn)
					{
						if (buff[0]=='n')
						{
							fOn=false;
							fReportTankLevel=true;
							report("0 - on to off transit", elappsed);
							elappsed=System.currentTimeMillis();
						}
					}
					else
					{
						if (buff[0]=='c')
						{
							fOn=true;
							fReportTankLevel=true;
							report("1 - off to on transit", elappsed);
							elappsed=System.currentTimeMillis();
						}
					}
				}
				else
				{
					if (System.currentTimeMillis()-watchdog > 600000)
					{
						System.out.println("watchdog timeout");
						isr.close();
						soc.close();
						return;
					}
					Thread.sleep(10);
				}
			}
		}
		catch (Exception e)
		{
			soc.close();
			throw e;
		}
	}

	public static void main(String[] args) throws Exception	
	{
		String host="192.168.0.227";
		if (args.length!=0)
		{
			host=args[0];
		}
		if (args.length>1)
		{
			outpath=args[1];
		}
		for (;;)
		{
			try
			{
				doIt(host, 23);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			if (outpath != null)
			{
				try
				{
					cookedStream.close();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				try
				{
					rawStream.close();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}

}
