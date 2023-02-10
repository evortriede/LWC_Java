@echo off
echo . >blank
echo This script configures WinRun4J which is used to run the Current Recorder as a Windows service.
echo Running Current Recorder as a service is important in that if the computer that is hosting 
echo the Current Recorder reboots (think power failure, etc.) we want the Current Recorder to restart
echo when the computer restarts.
type blank
echo After configuring, the service can be registered by running the registersvc.bat script. 
type blank
echo If necessary, the service can be unregistered with the unregistersvc.bat script
type blank
echo WinRun4J is fully described here: https://winrun4j.sourceforge.net/
type blank
del blank
pushd ..

echo working.directory=%cd% > bin\CurrentMonitorService.txt
echo log=%cd%\service\log.txt >> bin\CurrentMonitorService.txt
echo service.class=com.lwc.CurrentMonitorService >> bin\CurrentMonitorService.txt
echo service.id=LWCService >> bin\CurrentMonitorService.txt
echo service.name=LWC Current Monitor Service >> bin\CurrentMonitorService.txt
echo service.description=London Water Co-op metric recorder. >> bin\CurrentMonitorService.txt
echo classpath.1=%cd%\bin\*.jar >> bin\CurrentMonitorService.txt
echo classpath.2=%cd%\resources\*.jar >> bin\CurrentMonitorService.txt
echo vmarg.1=-Xdebug >> bin\CurrentMonitorService.txt
echo vmarg.2=-Xnoagent >> bin\CurrentMonitorService.txt
echo vmarg.3=-Xrunjdwp:transport=dt_socket,address=8787,server=y,suspend=n >> bin\CurrentMonitorService.txt
echo arg.2=%cd%/service/ >> bin\CurrentMonitorService.txt

echo arg.1=CurrentRecorder.local >> bin\CurrentMonitorService.txt

echo|set /p="vm.location=">>bin\CurrentMonitorService.txt
dir /B /S "%programfiles%\java" | find "jvm.dll" >> bin\CurrentMonitorService.txt
echo service.startup=auto >> bin\CurrentMonitorService.txt

popd

del CurrentMonitorService.ini
ren CurrentMonitorService.txt CurrentMonitorService.ini
type CurrentMonitorService.ini