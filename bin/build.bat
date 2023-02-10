echo off
if exist src (goto fromsrc)
if exist build.bat (goto frombin)
echo current directory must be LWC_Java or LWC_Java\bin
return
:fromsrc
pushd .
goto doit
:frombin
pushd ..
:doit
pushd src\com\lwc
javac -cp ../../../resources/winrun4j.jar *.java
popd
jar -c -f bin\lwc.jar -C src .
popd
