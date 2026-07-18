package com.lwc;

import java.io.DataInputStream;
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
import java.net.HttpURLConnection;
import java.net.URL;

public class ReadModbusRegister
{
  public static void main(String args[]) throws Exception
  {
    Socket socket;
    InputStream is;
    OutputStream os;
    String host=args[1];
    int port=(args.length==3)?Integer.parseInt(args[2]):502;
    
    socket=new Socket(host,port);
    is=socket.getInputStream();
    os=socket.getOutputStream();
    
    socket.setTcpNoDelay(false);
    socket.setSoTimeout(5000);

    ModbusRequest req=new ModbusRequest((short)1, (short)Integer.parseInt(args[0]));
    req.unitId=1;
    req.writeExternal(os);
    ModbusResponse resp=new ModbusResponse();
    resp.readExternal(is);
    
    System.out.println(resp.value[0]);
    
    is.close();
    os.close();
    socket.close();
  }


}