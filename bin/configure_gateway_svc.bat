@echo off
echo . >blank
echo This script configures WinRun4J which is used to run the Modbus Gateway as a Windows service.
echo Running Modbus Gateway as a service is important in that if the computer that is hosting 
echo the Modbus Gateway reboots (think power failure, etc.) we want the Modbus Gateway to restart
echo when the computer restarts.
type blank
echo After configuring, the service can be registered by running the register_gateway_svc.bat script. 
type blank
echo If necessary, the service can be unregistered with the unregister_gateway_svc.bat script
type blank
echo WinRun4J is fully described here: https://winrun4j.sourceforge.net/
type blank
del blank
pushd ..

mkdir service

echo working.directory=%cd% > bin\ModbusGatewayService.txt
echo log=%cd%\service\mbgwlog.txt >> bin\ModbusGatewayService.txt
echo service.class=com.lwc.ModbusGatewayService >> bin\ModbusGatewayService.txt
echo service.id=LWCModbusGatewayService >> bin\ModbusGatewayService.txt
echo service.name=LWC Modbus Gateway Service >> bin\ModbusGatewayService.txt
echo service.description=London Water Co-op Modbus Gateway. >> bin\ModbusGatewayService.txt
echo classpath.1=%cd%\bin\*.jar >> bin\ModbusGatewayService.txt
echo classpath.2=%cd%\resources\*.jar >> bin\ModbusGatewayService.txt
echo vmarg.1=-Xdebug >> bin\ModbusGatewayService.txt
echo vmarg.2=-Xnoagent >> bin\ModbusGatewayService.txt
echo vmarg.3=-Xrunjdwp:transport=dt_socket,address=8787,server=y,suspend=n >> bin\ModbusGatewayService.txt

echo arg.1=192.168.0.1 >> bin\ModbusGatewayService.txt

rem The following line appends "vm.location" without a line ending character to the file
echo|set /p="vm.location=">>bin\ModbusGatewayService.txt
rem the following line appends the full path to jvm.dll to the file
dir /B /S "%programfiles%\java" | find "jvm.dll" >> bin\ModbusGatewayService.txt
rem the results of the previous two lines (not including the remarks) should be "jvm.location=c:\Program Files\Java\..."

echo service.startup=auto >> bin\ModbusGatewayService.txt

popd

del ModbusGatewayService.ini
ren ModbusGatewayService.txt ModbusGatewayService.ini
type ModbusGatewayService.ini