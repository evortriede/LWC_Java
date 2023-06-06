@echo off
echo . >blank
echo This script configures WinRun4J which is used to run the Modbus Data Recorder as a Windows service.
echo Running Modbus Data Recorder as a service is important in that if the computer that is hosting 
echo the Modbus Data Recorder reboots (think power failure, etc.) we want the Modbus Data Recorder to restart
echo when the computer restarts.
type blank
echo After configuring, the service can be registered by running the register_modbus_recorder_svc.bat script. 
type blank
echo If necessary, the service can be unregistered with the unregister_modbus_recorder_svc.bat script
type blank
echo WinRun4J is fully described here: https://winrun4j.sourceforge.net/
type blank
del blank
pushd ..

mkdir service

echo working.directory=%cd% > bin\ModbusDataRecorderService.txt
echo log=%cd%\service\mbdrlog.txt >> bin\ModbusDataRecorderService.txt
echo service.class=com.lwc.ModbusDataRecorderService >> bin\ModbusDataRecorderService.txt
echo service.id=LWCModbusDataRecorderService >> bin\ModbusDataRecorderService.txt
echo service.name=LWC Modbus Data Recorder Service >> bin\ModbusDataRecorderService.txt
echo service.description=London Water Co-op Modbus Data Recorder. >> bin\ModbusDataRecorderService.txt
echo classpath.1=%cd%\bin\*.jar >> bin\ModbusDataRecorderService.txt
echo classpath.2=%cd%\resources\*.jar >> bin\ModbusDataRecorderService.txt
echo vmarg.1=-Xdebug >> bin\ModbusDataRecorderService.txt
echo vmarg.2=-Xnoagent >> bin\ModbusDataRecorderService.txt
echo vmarg.3=-Xrunjdwp:transport=dt_socket,address=8787,server=y,suspend=n >> bin\ModbusDataRecorderService.txt

echo arg.1=%cd%\service/ >> bin\ModbusDataRecorderService.txt
echo arg.2=192.168.0.1 >> bin\ModbusDataRecorderService.txt
echo arg.3=LWCWTPMonitor.local >> bin\ModbusDataRecorderService.txt

rem The following line appends "vm.location" without a line ending character to the file
echo|set /p="vm.location=">>bin\ModbusDataRecorderService.txt
rem the following line appends the full path to jvm.dll to the file
dir /B /S "%programfiles%\java" | find "jvm.dll" >> bin\ModbusDataRecorderService.txt
rem the results of the previous two lines (not including the remarks) should be "jvm.location=c:\Program Files\Java\..."

echo service.startup=auto >> bin\ModbusDataRecorderService.txt

popd

del ModbusDataRecorderService.ini
ren ModbusDataRecorderService.txt ModbusDataRecorderService.ini
type ModbusDataRecorderService.ini