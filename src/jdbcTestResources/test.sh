#! /bin/sh

CLASSPATH=$CLASSPATH:./jdbcTest.jar:../dist/firebirdsql.jar:../src/lib/connector.jar

java -classpath $CLASSPATH jdbcTest.JDBCTest

#jdbc:firebirdsql:localhost:/usr/local/firebird/fbjdbctest/fbmctest.gdb

