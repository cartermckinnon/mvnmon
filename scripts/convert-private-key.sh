#!/bin/sh

if [ "$#" -ne 2 ]
then
  echo "You must specify the private key PEM file and output DER file."
  exit 1
fi

PEM_FILE=$1
DER_FILE=$2

openssl pkcs8 -topk8 -inform PEM -outform DER -in $PEM_FILE -out $DER_FILE -nocrypt
