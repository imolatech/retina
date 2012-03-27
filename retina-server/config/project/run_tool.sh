#!/bin/sh

full_path() {
    cd $1 > /dev/null 2>&1
    pwd
}


CONFIG_DIR=`dirname $0`
CONFIG_DIR=`full_path $CONFIG_DIR`

ROOT_DIR=`dirname $CONFIG_DIR`
ROOT_DIR=`full_path $ROOT_DIR`

$JAVA_HOME/bin/java "-Dapp_log_dir=$CONFIG_DIR"  -classpath "$ROOT_DIR/lib/*:$CONFIG_DIR" "$@"
