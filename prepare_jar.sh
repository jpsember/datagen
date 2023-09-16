#!/usr/bin/env bash
set -eu

echo "Copying jar file and driver to: jar_with_driver"

rm -rf jar_with_driver
mkdir jar_with_driver
cp target/datagen-1.0-jar-with-dependencies.jar jar_with_driver/datagen.jar
cp jar_driver.sh jar_with_driver/datagen
