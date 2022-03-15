#!/usr/bin/env bash
set -e

APP=datagen

BINDIR="$HOME/bin"
if [ ! -d $BINDIR ]
then
  echo "Directory doesn't exist: $BINDIR"
  exit 1
fi
LINK=$BINDIR/$APP


##################################################
# Parse arguments:
#   [clean | skiptest]
##################################################

CLEAN=""
NOTEST=""
DONEARGS=0

while [ "$DONEARGS" -eq 0 ]; do
  if [ "$1" == "" ]; then
    DONEARGS=1
  elif [ "$1" == "clean" ]; then
    CLEAN="clean"
    shift 1
  elif [ "$1" == "skiptest" ]; then
    NOTEST="-DskipTests"
    shift 1
  else
    echo "Unrecognized argument: $1"
    exit 1
  fi
done


##################################################
# Perform clean, if requested
#
if [ "$CLEAN" != "" ]; then
  echo "...cleaning $APP"
  mvn clean
  if [ -f $LINK ]; then
    rm $LINK
  fi
fi




##################################################
# Compile and test
#
if [ "$NOTEST" != "" ]; then
  echo "...skipping tests"
fi

mvn install $NOTEST

if [ ! -f $LINK ]; then
  cp .jsproject/driver.sh $LINK
fi
