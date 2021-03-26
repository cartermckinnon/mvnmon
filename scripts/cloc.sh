#!/bin/sh

cd $(dirname $0)/..

docker run --rm -v $PWD:/code aldanial/cloc /code
