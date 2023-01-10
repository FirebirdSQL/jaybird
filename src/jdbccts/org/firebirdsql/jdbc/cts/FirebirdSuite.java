/*
 * Firebird Open Source J2ee connector - jdbc driver
 *
 * Distributable under LGPL license.
 * You may obtain a copy of the License at http://www.gnu.org/copyleft/lgpl.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * LGPL License for more details.
 *
 * This file was created by members of the firebird development team.
 * All individual contributions remain the Copyright (C) of those
 * individuals.  Contributors to this file are either listed here or
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.jdbc.cts;

import java.io.File;

import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Firebird JDBC CTS suite.
 * 
 * @author Roman Rokytskyy
 */
public class FirebirdSuite {
    
    public static final String[] BATCH_UPDATE_SUITE_TESTS = new String[] {
        "com.sun.cts.tests.jdbc.ee.batchUpdate.batchUpdateClient"
    };

    public static final String[] CALLABLE_STATEMENT_SUITE_TESTS = new String[] {
        "com.sun.cts.tests.jdbc.ee.callStmt.callStmt1.callStmtClient1",
        "com.sun.cts.tests.jdbc.ee.callStmt.callStmt2.callStmtClient2",
        "com.sun.cts.tests.jdbc.ee.callStmt.callStmt3.callStmtClient3",
        "com.sun.cts.tests.jdbc.ee.callStmt.callStmt4.callStmtClient4",
        "com.sun.cts.tests.jdbc.ee.callStmt.callStmt5.callStmtClient5",
        "com.sun.cts.tests.jdbc.ee.callStmt.callStmt6.callStmtClient6",
        "com.sun.cts.tests.jdbc.ee.callStmt.callStmt7.callStmtClient7",
        "com.sun.cts.tests.jdbc.ee.callStmt.callStmt8.callStmtClient8",
        "com.sun.cts.tests.jdbc.ee.callStmt.callStmt9.callStmtClient9",
        "com.sun.cts.tests.jdbc.ee.callStmt.callStmt10.callStmtClient10",
        "com.sun.cts.tests.jdbc.ee.callStmt.callStmt11.callStmtClient11",
        "com.sun.cts.tests.jdbc.ee.callStmt.callStmt12.callStmtClient12",
        "com.sun.cts.tests.jdbc.ee.callStmt.callStmt13.callStmtClient13",
        "com.sun.cts.tests.jdbc.ee.callStmt.callStmt14.callStmtClient14",
        "com.sun.cts.tests.jdbc.ee.callStmt.callStmt15.callStmtClient15",
        "com.sun.cts.tests.jdbc.ee.callStmt.callStmt16.callStmtClient16",
        "com.sun.cts.tests.jdbc.ee.callStmt.callStmt17.callStmtClient17",
        "com.sun.cts.tests.jdbc.ee.callStmt.callStmt18.callStmtClient18",
        "com.sun.cts.tests.jdbc.ee.callStmt.callStmt19.callStmtClient19",
        "com.sun.cts.tests.jdbc.ee.callStmt.callStmt20.callStmtClient20",
        "com.sun.cts.tests.jdbc.ee.callStmt.callStmt21.callStmtClient21",
        "com.sun.cts.tests.jdbc.ee.callStmt.callStmt22.callStmtClient22"
    };

    public static final String[] CONNECTION_SUITE_TESTS = new String[] {
        "com.sun.cts.tests.jdbc.ee.connection.connection1.connectionClient1"
    };

    public static final String[] DATE_TIME_SUITE_TESTS = new String[] {
        "com.sun.cts.tests.jdbc.ee.dateTime.dateTime1.dateTimeClient1",
        "com.sun.cts.tests.jdbc.ee.dateTime.dateTime2.dateTimeClient2",
        "com.sun.cts.tests.jdbc.ee.dateTime.dateTime3.dateTimeClient3"
    };
      
    public static final String[] DB_META_SUITE_TESTS = new String[] {
        "com.sun.cts.tests.jdbc.ee.dbMeta.dbMeta1.dbMetaClient1",
        "com.sun.cts.tests.jdbc.ee.dbMeta.dbMeta2.dbMetaClient2",
        "com.sun.cts.tests.jdbc.ee.dbMeta.dbMeta3.dbMetaClient3",
        "com.sun.cts.tests.jdbc.ee.dbMeta.dbMeta4.dbMetaClient4",
        "com.sun.cts.tests.jdbc.ee.dbMeta.dbMeta5.dbMetaClient5",
        "com.sun.cts.tests.jdbc.ee.dbMeta.dbMeta6.dbMetaClient6",
        "com.sun.cts.tests.jdbc.ee.dbMeta.dbMeta7.dbMetaClient7",
        "com.sun.cts.tests.jdbc.ee.dbMeta.dbMeta8.dbMetaClient8",
        "com.sun.cts.tests.jdbc.ee.dbMeta.dbMeta9.dbMetaClient9",
        "com.sun.cts.tests.jdbc.ee.dbMeta.dbMeta10.dbMetaClient10"
    };
      
    public static final String[] ESCAPE_SYNTAX_SUITE_TESTS = new String[] {
        "com.sun.cts.tests.jdbc.ee.escapeSyntax.scalar1.scalarClient1",
        "com.sun.cts.tests.jdbc.ee.escapeSyntax.scalar2.scalarClient2",
        "com.sun.cts.tests.jdbc.ee.escapeSyntax.scalar3.scalarClient3",
        "com.sun.cts.tests.jdbc.ee.escapeSyntax.scalar4.scalarClient4"
    };
    
    public static final String[] EXCEPTION_SUITE_TESTS = new String[] {            
        "com.sun.cts.tests.jdbc.ee.exception.batUpdExcept.batUpdExceptClient",
        "com.sun.cts.tests.jdbc.ee.exception.sqlException.sqlExceptionClient"
    };
      
    public static final String[] PREP_STMT_SUITE_TESTS = new String[] {
        "com.sun.cts.tests.jdbc.ee.prepStmt.prepStmt1.prepStmtClient1",
        "com.sun.cts.tests.jdbc.ee.prepStmt.prepStmt2.prepStmtClient2",
        "com.sun.cts.tests.jdbc.ee.prepStmt.prepStmt3.prepStmtClient3",
        "com.sun.cts.tests.jdbc.ee.prepStmt.prepStmt4.prepStmtClient4",
        "com.sun.cts.tests.jdbc.ee.prepStmt.prepStmt5.prepStmtClient5",
        "com.sun.cts.tests.jdbc.ee.prepStmt.prepStmt6.prepStmtClient6",
        "com.sun.cts.tests.jdbc.ee.prepStmt.prepStmt7.prepStmtClient7",
        "com.sun.cts.tests.jdbc.ee.prepStmt.prepStmt8.prepStmtClient8",
        "com.sun.cts.tests.jdbc.ee.prepStmt.prepStmt9.prepStmtClient9",
        "com.sun.cts.tests.jdbc.ee.prepStmt.prepStmt10.prepStmtClient10",
        "com.sun.cts.tests.jdbc.ee.prepStmt.prepStmt11.prepStmtClient11",
        "com.sun.cts.tests.jdbc.ee.prepStmt.prepStmt12.prepStmtClient12",
        "com.sun.cts.tests.jdbc.ee.prepStmt.prepStmt13.prepStmtClient13",
        "com.sun.cts.tests.jdbc.ee.prepStmt.prepStmt14.prepStmtClient14",
        "com.sun.cts.tests.jdbc.ee.prepStmt.prepStmt15.prepStmtClient15"
    };

    public static final String[] RESULT_SET_SUITE_TESTS = new String[] {
        "com.sun.cts.tests.jdbc.ee.resultSet.resultSet1.resultSetClient1",
        "com.sun.cts.tests.jdbc.ee.resultSet.resultSet7.resultSetClient7",
        "com.sun.cts.tests.jdbc.ee.resultSet.resultSet10.resultSetClient10",
        "com.sun.cts.tests.jdbc.ee.resultSet.resultSet11.resultSetClient11",
        "com.sun.cts.tests.jdbc.ee.resultSet.resultSet12.resultSetClient12",
        "com.sun.cts.tests.jdbc.ee.resultSet.resultSet13.resultSetClient13",
        "com.sun.cts.tests.jdbc.ee.resultSet.resultSet14.resultSetClient14",
        "com.sun.cts.tests.jdbc.ee.resultSet.resultSet16.resultSetClient16",
        "com.sun.cts.tests.jdbc.ee.resultSet.resultSet17.resultSetClient17",
        "com.sun.cts.tests.jdbc.ee.resultSet.resultSet18.resultSetClient18",
        "com.sun.cts.tests.jdbc.ee.resultSet.resultSet41.resultSetClient41",
        "com.sun.cts.tests.jdbc.ee.resultSet.resultSet45.resultSetClient45",
        "com.sun.cts.tests.jdbc.ee.resultSet.resultSet47.resultSetClient47",
        "com.sun.cts.tests.jdbc.ee.resultSet.resultSet49.resultSetClient49"
    };

    public static final String[] RS_META_SUITE_TESTS = new String[] {
        "com.sun.cts.tests.jdbc.ee.rsMeta.rsMetaClient"
    };

    public static final String[] STMT_SUITE_TESTS = new String[] {
        "com.sun.cts.tests.jdbc.ee.stmt.stmt1.stmtClient1",
        "com.sun.cts.tests.jdbc.ee.stmt.stmt2.stmtClient2",
        "com.sun.cts.tests.jdbc.ee.stmt.stmt3.stmtClient3"
    };

    public static TestSuite suite() {
        TestSuite suite = new TestSuite();
        
        suite.addTest(BatchUpdatesSuite.suite());
        suite.addTest(CallableStatementSuite.suite());
        suite.addTest(ConnectionSuite.suite());
        suite.addTest(DateTimeSuite.suite());
        suite.addTest(DbMetaSuite.suite());
        suite.addTest(EscapeSyntaxSuite.suite());
        suite.addTest(ExceptionSuite.suite());
        suite.addTest(PreparedStatementSuite.suite());
        suite.addTest(ResultSetSuite.suite());
        suite.addTest(RsMetaSuite.suite());
        suite.addTest(StatementSuite.suite());
        
        return suite;
    }
    
    protected static TestSuite getSuite(String[] classes) {
        TestSuite suite = new TestSuite();
        
        for (int i = 0; i < classes.length; i++) {
            try {
                suite.addTest(new CTSTestSuite(Class.forName(classes[i])));
            } catch(ClassNotFoundException ex) {
                suite.addTest(CTSTestSuite.warning(
                                "Class " + classes[i] + " not found."));
            }
        }
        
        return suite;
    }
    
    public static void main(String[] args) {
        
        File workingDir = new File(".");
        String workingDirStr = workingDir.getAbsolutePath();
        
        System.setProperty("cts.config.resource", "dml.properties");
        System.setProperty("cts.excludes.resource", "excludes.properties");
        System.setProperty("cts.db.url", "jdbc:firebirdsql:localhost/3050:" + workingDirStr +"/jdbccts.gdb");
        System.setProperty("cts.db.username", "SYSDBA");
        System.setProperty("cts.db.password", "masterkey");
        
        TestRunner.run(suite());
    }
}
