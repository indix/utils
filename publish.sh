#!/usr/bin/env bash

set -ex

sbt +publishSigned
sbt sonatypeRelease

echo "Released"