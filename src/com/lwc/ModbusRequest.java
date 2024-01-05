package com.lwc;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ModbusRequest
{
	short transId;
	short protocolId=0;
	short length=6;
	byte unitId=0;
	byte functionCode=3;
	short regNo;
	short respLen=1;
	ModbusRequest(short tid, short reg, byte fnCode)
	{
		transId=tid;
		regNo=reg;
		functionCode=fnCode;
	}
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
	
	public static void main(String[] args) throws Exception
	{
		Socket socket;
		InputStream is;
		OutputStream os;

		socket=new Socket(args[0],502);
		is=socket.getInputStream();
		os=socket.getOutputStream();
		
		socket.setTcpNoDelay(false);

		ModbusRequest req=new ModbusRequest(
				(short)1,
				(short)Integer.parseInt(args[2]), 
				(byte)Integer.parseInt(args[1]));
		req.writeExternal(os);
		ModbusResponse resp=new ModbusResponse();
		if (args[1].equalsIgnoreCase("2"))
		{
			resp.readExternal(is, 10);
		}
		else
		{
			resp.readExternal(is, 11);
		}
		
		System.out.println(resp.value);

		socket.close();
	}
}
