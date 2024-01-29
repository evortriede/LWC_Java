#!/bin/bash
pushd ..
pushd src/com/lwc
javac -cp ../../../resources/winrun4j.jar *.java
popd
jar -c -f bin/lwc.jar -C src .
popd
