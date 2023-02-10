package com.lwc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ModbusSim
{

	public static void main(String[] args) throws Exception
	{
		for (;;)
		{
			ServerSocket ss = new ServerSocket(502);
			try
			{
				System.out.println("waiting for connection");
				Socket s = ss.accept();
				System.out.println("connected");
				InputStream is=s.getInputStream();
				OutputStream os=s.getOutputStream();
				int volumeValue=3200;
				int turbidityValue=300;
				int value=0;
				long onTime=System.currentTimeMillis();
				int onOff=1;
				for (;;)
				{
					byte[] rgb = new byte[12];
					is.read(rgb);
					if (rgb[7]==2)
					{
						if (onTime<=System.currentTimeMillis())
						{
							onOff = onOff ^ 1;
							onTime = System.currentTimeMillis()+65000L;
						}
						value=onOff;
					}
					else if (rgb[9]==-117)
					{
						value=volumeValue;
						volumeValue-=1;
						if (volumeValue<250) volumeValue=3200;
					}
					else
					{
						value=turbidityValue;
						turbidityValue+=((turbidityValue&1)==0)?1:-1;
					}
					System.out.println("Got one "+value);
					rgb[5]=5;
					rgb[8]=2;
					rgb[9]=(byte) (value>>>8);
					rgb[10]=(byte)(value&0xff);
					os.write(rgb, 0, 11);
				}
				
			} catch (IOException e)
			{
				ss.close();
			}
		}
	}

}
