#!/bin/sh

set -e

cd $(dirname $0)
cd ..

JAR=$(ls target/mvnmon*.jar)

java --enable-preview -cp target/lib/*:$JAR mck.mvnmon.MvnMonApplication $@
