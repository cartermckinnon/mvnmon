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
cp -R target/site/apidocs dist/$DIST/doc/javadoc
cp src/main/bin/mvnmon dist/$DIST/bin/

# pack (optionally)
if [ "$#" -eq 1 ] && [ "$1" = "pack" ]
then
  cd dist/
  zip -r $DIST.zip $DIST
  sha512sum $DIST.zip > $DIST.zip.sha512
  tar -pczf $DIST.tar.gz $DIST
  sha512sum $DIST.tar.gz > $DIST.tar.gz.sha512
fi