#/bin/sh

CLASSPATH=
#:${JAVA_HOME}/lib/tools.jar

TARGET_CLASSPATH=`echo ../../lib/*.jar | tr ' ' ':'`

TARGET_CLASSPATH=${TARGET_CLASSPATH}:../../build/classes:${JAVA_HOME}/lib/tools.jar

java -classpath $TARGET_CLASSPATH org.apache.tools.ant.Main $*


#java -classpath ${TARGET_CLASSPATH}:/usr/local/firebird/dev/interclient/20/interclient.jar junit.textui.TestRunner org.firebirdsql.jsql.test.MainTest
#java -classpath ${TARGET_CLASSPATH}:/usr/local/firebird/dev/interclient/20/interclient.jar junit.textui.TestRunner org.firebirdsql.jsql.test.ConnectionFactoryTest


#java -classpath ${TARGET_CLASSPATH}:/usr/local/firebird/dev/interclient/20/interclient.jar  org.firebirdsql.jsql.Main jsql3-r.xml
