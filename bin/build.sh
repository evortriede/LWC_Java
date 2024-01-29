#!/bin/bash
pushd ..
pushd src/com/lwc
javac -cp ../../../resources/WinRun4J.jar *.java
popd
jar -c -f bin/lwc.jar -C src .
popd
