#!/usr/bin/env bash
set -eu

dfa input tokens.rxp output src/main/resources/datagen/tokens.dfa ids src/main/java/datagen/ParseTools.java
