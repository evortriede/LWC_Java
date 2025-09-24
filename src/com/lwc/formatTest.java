package com.lwc;

public class formatTest
{
  public static void main(String[] args) throws Exception
  {
    ModbusDataRecorder.sendToCloud(ModbusDataRecorder.formatStatus(20416,-800,-20416,3.5f,0.081f,1.0f,130));
    Thread.sleep(3000);
  }
}