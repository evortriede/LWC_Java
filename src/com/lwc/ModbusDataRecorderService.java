package com.lwc;

import java.io.IOException;

import org.boris.winrun4j.AbstractService;
import org.boris.winrun4j.ServiceException;

public class ModbusDataRecorderService extends AbstractService
{

	@Override
	public int serviceMain(String[] args) throws ServiceException
	{
		ModbusDataRecorder recorder=new ModbusDataRecorder(args);
		Thread t=new Thread()
		{
			@Override
			public void run()
			{
				while (!shutdown)
				{
					try
					{
						recorder.process();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					if (recorder.outpath != null)
					{
						try
						{
							recorder.cookedStream.close();
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}
				}
			}
		};
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
			recorder.socket.close();
		} catch (IOException e)
		{
		}
		try
		{
			t.join();
		} catch (InterruptedException e)
		{
		}
		return 0;
	}

}
