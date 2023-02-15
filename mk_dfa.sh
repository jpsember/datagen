#!/usr/bin/env bash
set -eu

dev dfa tokens.rxp src/main/resources/datagen/tokens.dfa ids src/main/java/datagen/ParseTools.java
