package com.lwc;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ModbusResponse
{
	short transId;
	short protocolId;
	short length;
	byte unitId;
	byte functionCode;
	byte respLen;
	short[] value;
	ModbusResponse()
	{
		
	}
	public void readExternal(InputStream ins) throws IOException
	{
		byte[] rgb=ins.readNBytes(6);
		ByteArrayInputStream bais=new ByteArrayInputStream(rgb);
		DataInputStream in=new DataInputStream(bais);
		transId=in.readShort();
		protocolId=in.readShort();
		length=in.readShort();
		System.out.println(length);
		readExternal(ins, length);
	}
	public void readExternal(InputStream ins, int len) throws IOException
	{
		byte[] rgb=ins.readNBytes(len);
		ByteArrayInputStream bais=new ByteArrayInputStream(rgb);
		DataInputStream in=new DataInputStream(bais);
		unitId=in.readByte();
		functionCode=in.readByte();
		respLen=in.readByte();
		if (functionCode==2)
		{
			value=new short[1];
			value[0]=(short)in.readByte();
		}
		else
		{
			value=new short[respLen/2];
			for (int i=0;i<respLen/2;i++)
			{
				value[i]=in.readShort();
			}
		}
	}
}
