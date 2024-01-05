package com.lwc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class StatusGateway
{
	public class Worker extends Thread
	{
		private Socket clSock;
		private Socket svSock;
		private InputStream clIn;
		private OutputStream clOut;
		private OutputStream svOut;
		private InputStream svIn;

		public Worker(Socket clSock)
		{
			this.clSock=clSock;
		}
		public void run()
		{
			try
			{
				System.out.println("connecting "+host);
				svSock=new Socket(host,80);
				System.out.println("connected");
				clIn=clSock.getInputStream();
				clOut=clSock.getOutputStream();
				svOut=svSock.getOutputStream();
				svIn=svSock.getInputStream();
				Thread t=new Thread()
				{
					public void run()
					{
						for (;;)
						{
							try
							{
								svOut.write(clIn.readAllBytes());
								/*
								int cin=clIn.read();
								if (cin >= 0)
								{
									svOut.write(cin);
								}
								*/
							} catch (IOException e)
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				};
				t.setDaemon(true);
				t.start();
				for (;;)
				{
					clOut.write(svIn.readAllBytes());
					/*
					int sin=svIn.read();
					if (sin >= 0)
					{
						clOut.write(sin);
					}
					*/
				}
			} catch (IOException e)
			{
				e.printStackTrace();
				try
				{
					clSock.close();
				} catch (IOException e1)
				{
					e1.printStackTrace();
				}
				return;
			}
		}
	}
	private ServerSocket ss;
	private String host;
	public StatusGateway(String[] args) throws Exception
	{
		host=args[0];
		ss = new ServerSocket(65080);
	}
	public static void main(String[] args) throws Exception
	{
		new StatusGateway(args).process();
	}
	public void process() throws Exception
	{
		for (;;)
		{
			Worker w = new Worker(ss.accept());
			System.out.println("got one");
			w.setDaemon(true);
			w.start();
		}
		
	}
}
