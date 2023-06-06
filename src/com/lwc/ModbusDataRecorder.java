package com.lwc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ModbusDataRecorder
{
	public Socket socket;
	public InputStream is;
	public OutputStream os;
	private String host="127.0.0.1";
	private int port=502;
	public String monitorHost="127.0.0.1";
	public int monitorPort=9023;

	public PrintStream cookedStream=System.out;
	public String outpath;
	public int month=13;
	static ModbusDataRecorder instance; 
	
	public Socket getSocket(String host, int port)
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

	public void report(PrintStream stream, String msg, long elappsed)
	{
		String s = String.format("%s %s %s %s", 
				dsf.format(new Date()), 
				String.valueOf(System.currentTimeMillis()),
				String.valueOf(elappsed),
				msg);
		stream.println(s);
		stream.flush();
	}

	public void report(String msg, long elappsed)
	{
		report(cookedStream(), msg, elappsed);
	}
	
	private PrintStream cookedStream()
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

	private void rotateStreams()
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
		String prefix=String.format("%s/%d-%d", outpath, month+1, cal.get(Calendar.YEAR));
		try
		{
			cookedStream=new PrintStream(new FileOutputStream(prefix+"-cooked.txt",true));
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
			cookedStream=System.out;
		}
	}

	public class ModbusRequest
	{
		short transId;
		short protocolId=0;
		short length=6;
		byte unitId=0;
		byte functionCode=3;
		short regNo;
		short respLen=1;
		ModbusRequest(short tid, short reg)
		{
			transId=tid;
			regNo=reg;
		}
		ModbusRequest()
		{}

		public void writeExternal(OutputStream os) throws IOException
		{
			ByteArrayOutputStream baos=new ByteArrayOutputStream();
			DataOutputStream s=new DataOutputStream(baos);
			s.writeShort(transId);
			s.writeShort(protocolId);
			s.writeShort(length);
			s.writeByte(unitId);
			s.writeByte(functionCode);
			s.writeShort(regNo);
			s.writeShort(respLen);
			os.write(baos.toByteArray());
		}
	}
	public class ModbusResponse
	{
		short transId;
		short protocolId;
		short length;
		byte unitId;
		byte functionCode;
		byte respLen;
		short value;
		ModbusResponse()
		{
			
		}
		public void readExternal(InputStream ins) throws IOException
		{
			byte[] rgb=ins.readNBytes(11);
			ByteArrayInputStream bais=new ByteArrayInputStream(rgb);
			DataInputStream in=new DataInputStream(bais);
			transId=in.readShort();
			protocolId=in.readShort();
			length=in.readShort();
			unitId=in.readByte();
			functionCode=in.readByte();
			respLen=in.readByte();
			value=in.readShort();
		}
	}
	
	public ModbusDataRecorder(String[] args)
	{
		if (args.length>0)
		{
			outpath=args[0];
		}
		if (args.length>1)
		{
			host=args[1];
		}
		if (args.length>2)
		{
			monitorHost=args[2];
		}
		instance = this;
	}
	
	class MonitorThread extends Thread
	{
		Socket monSocket;
		
		@Override
		public void run()
		{
			try
			{
				for (;;)
				{
					try
					{
						System.out.println("connecting to "+monitorHost+":"+monitorPort);
						monSocket = new Socket(monitorHost, monitorPort);
						monSocket.setSoTimeout(90000);//if no traffic in 1.5 minutes we want to restart the connection
						InputStream monIs=monSocket.getInputStream();
						monProcess(monIs);
					}
					catch (IOException ioe)
					{
						ioe.printStackTrace();
						Thread.sleep(1000);
					}
				}
			}
			catch (InterruptedException ie)
			{
				
			}
		}

		private void monProcess(InputStream monIs) throws IOException, InterruptedException
		{
			boolean fOn=false;
			DataInputStream dis=new DataInputStream(monIs);
			for (;;)
			{
				switch (dis.readByte())
				{
				case 'r':
					short rawTank=swapEndian(dis.readShort());
					instance.report(String.valueOf((rawTank * 719) / 100)+" gallons", 0);
					instance.report("raw tank level "+rawTank, 0);
					//System.out.println("raw tank level "+rawTank);
					break;
				case 'T':
					short rawTurb=swapEndian(dis.readShort());
					if (rawTurb != 0)
					{
						instance.report("raw turbidity "+rawTurb, 0);
						//System.out.println("raw turbidity "+rawTurb);
					}
					break;
				case 'C':
					short rawCL17=swapEndian(dis.readShort());
					if (rawCL17!=0)
					{
						instance.report("Chlorine "+rawCL17, 0);
						//System.out.println("Chlorine "+rawCL17);
					}
					break;
				case 'P':
					short pump=swapEndian(dis.readShort());
					if (pump!=0)
					{
						instance.report("Pump speed "+pump, 0);
						//System.out.println("Pump speed "+pump);
					}
					break;
				case 'c':
					int onTime=swapEndian(dis.readInt());
					int offTime=swapEndian(dis.readInt());
					if (!fOn)
					{
						fOn=true;
						instance.report("1 - off to on transit", offTime);
						//System.out.println("1 - off to on transit "+offTime);
					}
					break;
				case 'n':
					onTime=swapEndian(dis.readInt());
					offTime=swapEndian(dis.readInt());
					if (fOn)
					{
						fOn=false;
						instance.report("0 - on to off transit", onTime);
						//System.out.println("0 - on to off transit "+onTime);
					}
					break;
				default:
					break;
				}
				Thread.sleep(1000);
			}
		}
		
	}
	
	public void process()
	{
		try
		{
			new MonitorThread().start();
			socket=new Socket(host,port);
			is=socket.getInputStream();
			os=socket.getOutputStream();
			
			socket.setTcpNoDelay(false);

			
			for(short tid=0;;tid++)
			{
				ModbusRequest req=new ModbusRequest(tid, (short)139);
				req.writeExternal(os);
				ModbusResponse resp=new ModbusResponse();
				resp.readExternal(is);
				report("raw tank level "+resp.value,0);
				Thread.sleep(10000l);
				req.regNo=0x64;
				req.writeExternal(os);
				resp.readExternal(is);
				if (resp.value!=0)
				{
					report("raw turbidity "+resp.value,0);
				}
				Thread.sleep(50000);
			}
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		} 
		catch (InterruptedException e)
		{
			e.printStackTrace();
		} 
		finally
		{
			try
			{
				socket.close();
			}
			catch (Exception ex)
			{
				
			}
		}
	}

	static int swapEndian(int i)
	{
		return ((i & 0xff) << 24) | ((i & 0xff00) << 8) | ((i >>> 8) & 0xff00) | ((i >>> 24) & 0xff);
	}
	
	static short swapEndian(short i)
	{
		return (short) (((i & 0xff) << 8) | ((i >>> 8) & 0xff));
	}
	
	public static void main(String[] args)
	{
		new ModbusDataRecorder(args).process();
	}

}
