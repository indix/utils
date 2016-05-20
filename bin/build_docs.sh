#!/bin/bash

set -e

BUILD_FOLDER="build/"


if [ ! -d "$BUILD_FOLDER" ]; then
  git clone git@github.com:ind9/utils $BUILD_FOLDER
fi

cd $BUILD_FOLDER

sbt unidoc ghpagesPushSite

