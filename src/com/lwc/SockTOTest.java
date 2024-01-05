package com.lwc;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class SockTOTest
{

	public static void main(String[] args) throws Exception
	{
		Thread t=new Thread()
		{
			public void run()
			{
				try
				{
					ServerSocket ss=new ServerSocket(12345);
					Socket svrSock=ss.accept();
					InputStream is=svrSock.getInputStream();
					for (;;)
					{
						is.read();
					}
				} catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		t.setDaemon(true);
		t.start();
		Socket s = new Socket("127.0.0.1",12345);
		s.setSoTimeout(10000);
		InputStream ins=s.getInputStream();
		DataInputStream dis=new DataInputStream(ins);
		for (;;)
		{
			System.out.println(dis.readByte());
		}
	}

}
