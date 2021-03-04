#!/bin/sh

set -ex

cd $(dirname $0)
cd ..

rm -rf dist/
rm -rf .tmp/

cp -R target/maven-jlink/classifiers/runtime/ .tmp/
cp -R doc/ .tmp/doc

mkdir dist/

zip -r dist/dist.zip .tmp/

tar -pczf dist/dist.tar.gz .tmp/

rm -rf .tmp/