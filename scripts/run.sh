#!/bin/sh

cd $(dirname $0)
cd ..

java --enable-preview -jar target/mvnmon*.jar $@
