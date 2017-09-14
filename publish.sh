#!/usr/bin/env bash

set -ex

sbt "project coreUtils" +publishSigned
sbt "project sparkUtils" +publishSigned
sbt "project storeUtils" +publishSigned
sbt sonatypeReleaseAll

echo "Released"