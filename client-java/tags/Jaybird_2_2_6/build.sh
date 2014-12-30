#!/bin/sh

CLASSPATH=
#:${JAVA_HOME}/lib/tools.jar

TARGET_CLASSPATH=`echo ../../lib/*.jar | tr ' ' ':'`

TARGET_CLASSPATH=${TARGET_CLASSPATH}:${JAVA_HOME}/lib/tools.jar

ANT_HOME=.
ANT=$ANT_HOME/bin/ant

export ANT ANT_HOME ANT_OPTS

exec $ANT $ANT_OPTIONS "$@"
