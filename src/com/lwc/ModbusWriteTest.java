package com.lwc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ModbusWriteTest
{
	public class ModbusRequest
	{
		short transId;
		short protocolId=0;
		short length=6;
		byte unitId=0;
		byte functionCode=3;
		ModbusRequest(short tid, short length, byte fnCode)
		{
			transId=tid;
			this.length=length;
			functionCode=fnCode;
		}
		ModbusRequest()
		{}

		public void write(OutputStream os) throws IOException
		{
			DataOutputStream s=new DataOutputStream(os);
			s.writeShort(transId);
			s.writeShort(protocolId);
			s.writeShort(length);
			s.writeByte(unitId);
			s.writeByte(functionCode);
		}
	}

	public class ModbusReadRegistersRequest extends ModbusRequest
	{
		short regNo;
		short respLen=1;
		
		public ModbusReadRegistersRequest()
		{
			super();
		}
		
		public ModbusReadRegistersRequest(short transId, short regNo, short respLen)
		{
			super(transId, (short)6, (byte)3);
			this.regNo=regNo;
			this.respLen=respLen;
		}

		public void write(OutputStream os) throws IOException
		{
			super.write(os);
			DataOutputStream s=new DataOutputStream(os);
			s.writeShort(regNo);
			s.writeShort(respLen);
		}
	}
	
	public class ModbusResponse
	{
		private InputStream is;
		private short transId;
		private short protocolId;
		private short len;
		private byte unitId;
		private byte fnCode;

		public ModbusResponse(InputStream is)
		{
			this.is = is;
		}
		
		public DataInputStream read() throws IOException
		{
			DataInputStream dis = new DataInputStream(is);
			this.transId=dis.readShort();
			this.protocolId=dis.readShort();
			this.len=dis.readShort();
			this.unitId=dis.readByte();
			this.fnCode=dis.readByte();
			return dis;
		}

		public InputStream getIs()
		{
			return is;
		}

		public short getTransId()
		{
			return transId;
		}

		public short getProtocolId()
		{
			return protocolId;
		}

		public short getLen()
		{
			return len;
		}

		public byte getUnitId()
		{
			return unitId;
		}

		public byte getFnCode()
		{
			return fnCode;
		}
	}
	
	public class ModbusReadRegistersResponse
	{
		short[] rgRegs;
		public ModbusReadRegistersResponse()
		{
		}
		
		public void read(DataInputStream dis) throws IOException
		{
			byte len=dis.readByte();
			rgRegs=new short[len/2];
			for (int i=0;i<rgRegs.length;i++)
			{
				rgRegs[i]=dis.readShort();
			}
		}
	}
	
	void doit() throws IOException
	{
		ModbusReadRegistersRequest mbreq = new ModbusReadRegistersRequest((short)2, (short)10, (short)1);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		mbreq.write(baos);
		byte[] req=baos.toByteArray();
		for (int i=0;i<req.length;i++)
		{
			System.out.printf("%1$02x ",req[i]);
		}
		System.out.println();
		
		byte[] resp=new byte[] {0, 1, 0, 0, 0, 5, 0, 3, 2, 0, 5 };
		ByteArrayInputStream bais = new ByteArrayInputStream(resp);
		ModbusResponse mbr=new ModbusResponse(bais);
		DataInputStream dis=mbr.read();
		System.out.println(" "+mbr.getTransId()+" "+mbr.getLen()+" "+mbr.fnCode);
		ModbusReadRegistersResponse rrr = new ModbusReadRegistersResponse();
		rrr.read(dis);
		System.out.println(" "+rrr.rgRegs.length+" "+rrr.rgRegs[0]);
	}
	
	public static void main(String[] args) throws Exception
	{
		ModbusWriteTest mbwt=new ModbusWriteTest();
		mbwt.doit();
		for (;;)
		{
			System.out.print((char)System.in.read());
		}
	}
}
