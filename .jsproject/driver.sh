#!/usr/bin/env sh
set -eu

MVN=$HOME/.m2/repository
java -Dfile.encoding=UTF-8 -classpath $MVN/com/jsbase/datagen/1.0/datagen-1.0.jar:$MVN/commons-io/commons-io/2.6/commons-io-2.6.jar:$MVN/com/jsbase/base/1.0/base-1.0.jar datagen.Datagen "$@"
