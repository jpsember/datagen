#!/usr/bin/env sh
set -eu
java -Dfile.encoding=UTF-8 -classpath /Users/jeff/bin/datagen-1.0-jar-with-dependencies.jar datagen.Datagen "$@"
