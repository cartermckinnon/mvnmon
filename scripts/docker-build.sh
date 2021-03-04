#!/bin/sh

set -ex

cd $(dirname $0)
cd ..

docker build . -t mckdev/mvnmon
