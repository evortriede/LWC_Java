package com.lwc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.boris.winrun4j.AbstractService;
import org.boris.winrun4j.ServiceException;

public class ModbusGatewayService extends AbstractService
{
	String plcHost="192.168.0.1";
	
	class Runner extends Thread
	{
		Socket sock;
		InputStream clientIns;
		OutputStream clientOuts;
		InputStream plcIns;
		OutputStream plcOuts;
		
		@Override
		public void run()
		{
			System.out.println("Runner started");
			try
			{
				System.out.println("connecting to plc "+plcHost);
				Socket s = new Socket(plcHost, 502);
				System.out.println("connected");
				plcIns=s.getInputStream();
				plcOuts=s.getOutputStream();
				clientIns=sock.getInputStream();
				clientOuts=sock.getOutputStream();
				while (true)
				{
					if (clientIns.available()>0)
					{
						
						byte[] rgIn=new byte[clientIns.available()];
						for (int i=0;i<rgIn.length;i++)
						{
							rgIn[i]=(byte)clientIns.read();
						}
						plcOuts.write(rgIn);
						plcOuts.flush();
					}
					if (plcIns.available()>0)
					{
						byte[] rgOut=new byte[plcIns.available()];
						for (int i=0;i<rgOut.length;i++)
						{
							rgOut[i]=(byte)plcIns.read();
						}
						clientOuts.write(rgOut);
						clientOuts.flush();
					}
					try
					{
						Thread.sleep(10);
					} catch (InterruptedException e)
					{
						s.close();
						sock.close();
					}
				}
			}
			catch (IOException ioe)
			{
				ioe.printStackTrace();
				return;
			}
		}
		
	}

	@Override
	public int serviceMain(String[] arg0) throws ServiceException
	{
		ServerSocket ss;
		try
		{
			ss = new ServerSocket(502);
		} catch (IOException e1)
		{
			e1.printStackTrace();
			throw new ServiceException(e1);
		}
		Thread t=new Thread()
		{
			@Override
			public void run()
			{
				while (!shutdown)
				{
					try
					{
						System.out.println("Creating runner");
						Runner r = new Runner();
						r.sock = ss.accept();
						System.out.println("Got a connection");
						r.setDaemon(true);
						r.start();
					}
					catch (Exception e)
					{
						return;
					}
				}
			}
		};
		t.setDaemon(true);
		t.start();
		while (!shutdown)
		{
			try
			{
				Thread.sleep(100);
			} catch (InterruptedException e)
			{
				
			}
		}
		try
		{
			t.interrupt();
			ss.close();
		} catch (IOException e)
		{
		}
		try
		{
			t.join(1000);
		} catch (InterruptedException e)
		{
		}
		return 0;
	}

}
