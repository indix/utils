#!/bin/bash

set -e

BUILD_FOLDER="build/"


if [ ! -d "$BUILD_FOLDER" ]; then
  git clone -b master git@github.com:ind9/utils $DEPLOY_FOLDER
fi

cd $BUILD_FOLDER
git pull origin gh-pages

sbt unidoc ghpagesPushSite

