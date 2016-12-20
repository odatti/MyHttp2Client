#!/bin/bash

cd pd3

javac -d ../../out/ $(find ./ -name "*.java")

cd ../

if [ "$1" = "run" ]
then
	cd ../out
	java pd3.PD3Main $2 $3
	cd ../src
fi
