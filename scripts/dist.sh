#!/bin/sh

set -ex

cd $(dirname $0)
cd ..

rm -rf dist/

VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
DIST="mvnmon-$VERSION"

mkdir -p dist/$DIST/lib
mkdir -p dist/$DIST/extlib
mkdir -p dist/$DIST/bin

# assemble
cp -R target/mvnmon*.jar dist/$DIST/lib/
cp -R target/lib/* dist/$DIST/extlib/
cp -R doc/ dist/$DIST/doc
cp src/main/bin/mvnmon dist/$DIST/bin/

# archive
zip -r dist/$DIST.zip dist/$DIST
tar -pczf dist/$DIST.tar.gz dist/$DIST