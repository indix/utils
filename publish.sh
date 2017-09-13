#!/usr/bin/env bash

set -ex

SCALA_VERSION=${TRAVIS_SCALA_VERSION:-2.11.11}

sbt ++${SCALA_VERSION} publishSigned
sbt sonatypeRelease

echo "Released"