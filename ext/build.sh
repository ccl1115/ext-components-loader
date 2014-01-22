#!/bin/bash

TARGET_DIR='../host/assets'
OUT_DIR='../out/ext/classes'
OUT_JAR_DIR='../out/ext'
BUILD_TOOLS_VERSION='19.0.1'

mkdir -p ${OUT_JAR_DIR}
mkdir -p ${OUT_DIR}

javac -d ${OUT_DIR} -cp $ANDROID_HOME/platforms/android-19/android.jar src/com/simon/*
jar cf ext.jar -C ${OUT_DIR} com/simon/ExternalActivity.class \
               -C ${OUT_DIR} com/simon/ExternalService.class \
               -C ${OUT_DIR} com/simon/ExternalView.class

$ANDROID_HOME/build-tools/${BUILD_TOOLS_VERSION}/dx --dex --output=classes.dex ext.jar
zip ${TARGET_DIR}/ext.jar classes.dex
rm classes.dex