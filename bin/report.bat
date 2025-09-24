echo off
rem Check id command extensions are enabled and if not, start a new shell with them enabled
if %tmp%==!tmp! goto doit
call cmd /V:ON /C %0 %*
goto done
:doit
rem saving the current directory, change to the service directory using the path to this script as a refrence
set egv_=%~p0
pushd %egv_:~0,-4%service\
rem if parameters have been given they are month and year and optionally a flag to indicate that 
rem only the fixed data is to be processed
if "%1"=="" goto c0
set egv_mm=%1
set egv_mm2=0!egv_mm!
set egv_mm2=!egv_mm2:~-2!
set egv_yy=%2
if "%3"=="" goto c2
goto c3
:c0
rem no parameters have been given, so assume that the previous month is to be processed
if not "01"=="%date:~-10,2%" goto c1
rem it's January so we have to process December of last year
set egv_mm=12
set egv_mm2=12
set /A egv_yy=%date:~-4%-1
goto c2
:c1
rem egv_mm gets the previous month as a numeric value and egv_mm2 gets a two digit month number for last month
set /A egv_mm=%date:~-10,2%-1
set egv_mm2=0!egv_mm!
set egv_mm2=!egv_mm2:~-2!
set egv_yy=%date:~-4%
:c2
echo file = !egv_mm!-!egv_yy!-cooked.txt
pscp -pwfile "!egv_!pw.txt" lwc@lwc-MINI-S:/home/lwc/ModbusDataRecorder/!egv_mm!-!egv_yy!-cooked.txt .
java -cp ..\bin\lwc.jar com.lwc.VolTurbProcessor !egv_mm!-!egv_yy!-cooked.txt > !egv_mm!-!egv_yy!-fixed.txt
:c3
java -cp ..\bin\lwc.jar com.lwc.LWCMonthlyReport !egv_mm!-!egv_yy!-fixed.txt !egv_mm2! ..\reports/
popd
echo done
:done
