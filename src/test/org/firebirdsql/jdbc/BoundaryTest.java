/*
 * Firebird Open Source JDBC Driver
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
 * can be obtained from a source control history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.jdbc;

import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoundaryTest {

    // TODO: document what this test is supposed to test
    // TODO: Consider removing this test

    //@formatter:off
    private static final String CREATE_META_ONE =
            "CREATE TABLE COMMUNICATIONS_FIT ( \n"
            + "ID INTEGER NOT NULL, \n"
            + "GUIDID CHAR(16), \n"
            + "NAME VARCHAR(64) CHARACTER SET ISO8859_1 COLLATE EN_UK, \n"
            + "SDESC VARCHAR(256) CHARACTER SET ISO8859_1 COLLATE EN_UK, \n"
            + "LDESC BLOB SUB_TYPE 1, \n"
            + "STATUS INTEGER, \n"
            + "PRIMARY KEY (ID) \n"
            + ")";
    //@formatter:on

    @RegisterExtension
    final UsesDatabaseExtension.UsesDatabaseForEach usesDatabase = UsesDatabaseExtension.usesDatabase(
            CREATE_META_ONE);

    @Test
    void testLockUp() {
        final Results results = createResults();
        final Thread thread = new Thread(() -> {
            try {
                performLockupSequence();
                results.setIsOk();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, "Lockup test thread.");
        thread.start();

        assertTrue(results.waitForCompletionOrTimeout(), "Operation should have completed by now");
    }

    @Test
    void testNoLockUp() {
        final Results results = createResults();
        final Thread thread = new Thread(() -> {
            try {
                performSimilarButNoLockupSequence();
                results.setIsOk();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, "Lockup test thread.");
        thread.start();

        assertTrue(results.waitForCompletionOrTimeout(), "Operation should have completed by now");
    }

    private Results createResults() {
        return new Results();
    }

    private static class Results {

        private boolean isOk = false;

        private synchronized void setIsOk() {
            isOk = true;
            notifyAll();
        }

        private synchronized boolean waitForCompletionOrTimeout() {
            final long startTime = System.currentTimeMillis();

            while (!isOk && (System.currentTimeMillis() - startTime) < 10000) {
                try {
                    wait(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return isOk;
        }
    }

    private void performLockupSequence() throws SQLException {
        try (Connection conn = getConnectionViaDriverManager();
             PreparedStatement statement = conn.prepareStatement("INSERT INTO COMMUNICATIONS_FIT ( \n "
                     + "GUIDID, \n"
                     + "NAME, \n"
                     + "SDESC, \n"
                     + "LDESC, \n"
                     + "STATUS, \n"
                     + "ID \n"
                     + ") \n"
                     + "VALUES ( ?, ?, ?, ?, ?, ? ) \n")) {
            statement.clearParameters();

            byte[] guid = new byte[16];
            for (int i = 0; i < guid.length; i++) {
                guid[i] = (byte) (i + 65);
            }
            statement.setBytes(1, guid);
            statement.setString(2, "Further");
            statement.setString(3, "Further infomation field");
            statement.setString(4, "Field to provide Further infomation capture");
            statement.setInt(5, 2);
            statement.setInt(6, 1);
            statement.executeUpdate(); // <---- WE WILL LOCK
        }
    }

    private void performSimilarButNoLockupSequence() throws SQLException {
        try (Connection conn = getConnectionViaDriverManager();
             PreparedStatement statement = conn.prepareStatement("INSERT INTO COMMUNICATIONS_FIT ( \n "
                     //+ "GUIDID, \n"
                     + "NAME, \n"
                     + "SDESC, \n"
                     + "LDESC, \n"
                     + "STATUS, \n"
                     + "ID \n"
                     + ") \n"
                     + "VALUES ( ?, ?, ?, ?, ? ) \n")) {

            statement.clearParameters();
            // statement.setBytes( 1, new byte[16] );
            statement.setString(1, "Further");
            statement.setString(2, "Further infomation field");
            statement.setString(3, "Field to provide Further infomation capture");
            statement.setInt(4, 2);
            statement.setInt(5, 1);
            statement.executeUpdate(); // <---- WE WILL LOCK
        }
    }

}
