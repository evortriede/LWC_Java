package com.lwc;

import java.io.IOException;

import org.boris.winrun4j.AbstractService;
import org.boris.winrun4j.ServiceException;

public class CurrentMonitorService extends AbstractService
{
	static String host="192.168.0.227"; 

	@Override
	public int serviceMain(String[] args) throws ServiceException
	{
		
		if (args.length!=0)
		{
			host=args[0];
		}
		if (args.length>1)
		{
			CurrentRecorder.outpath=args[1];
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
						CurrentRecorder.doIt(host, 23);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					if (CurrentRecorder.outpath != null)
					{
						try
						{
							CurrentRecorder.cookedStream.close();
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
						try
						{
							CurrentRecorder.rawStream.close();
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
			CurrentRecorder.soc.close();
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
