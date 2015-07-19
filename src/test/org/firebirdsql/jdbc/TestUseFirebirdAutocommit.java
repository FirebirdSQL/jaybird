/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
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

import org.firebirdsql.common.FBJUnit4TestBase;
import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.jca.FBManagedConnectionFactory;
import org.junit.Test;

import java.sql.*;
import java.util.Properties;

import static org.firebirdsql.common.DdlHelper.executeCreateTable;
import static org.firebirdsql.common.DdlHelper.executeDDL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for "use Firebird autocommit" mode.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.2
 */
public class TestUseFirebirdAutocommit extends FBJUnit4TestBase {

    /**
     * Tests if the connection property {@code useFirebirdAutocommit} defaults to {@code false} if not set.
     */
    @Test
    public void connectionPropertyUseFirebirdAutocommit_notSpecified() throws Exception {
        String url = FBTestProperties.getUrl();
        checkFirebirdAutocommitValue(url, false);
    }

    /**
     * Tests if the connection property {@code useFirebirdAutocommit} is set to {@code false} if URL value is false.
     */
    @Test
    public void connectionPropertyUseFirebirdAutocommit_fromUrl_valueFalse() throws Exception {
        String url = FBTestProperties.getUrl() + "?useFirebirdAutocommit=false";
        checkFirebirdAutocommitValue(url, false);
    }

    /**
     * Tests if the connection property {@code useFirebirdAutocommit} is set to {@code true} from the URL without a value.
     */
    @Test
    public void connectionPropertyUseFirebirdAutocommit_fromUrl_noValue() throws Exception {
        String url = FBTestProperties.getUrl() + "?useFirebirdAutocommit";
        checkFirebirdAutocommitValue(url, true);
    }

    /**
     * Tests if the connection property {@code useFirebirdAutocommit} is set to {@code true} from the URL with
     * value {@code true}.
     */
    @Test
    public void connectionPropertyUseFirebirdAutocommit_fromUrl_valueTrue() throws Exception {
        String url = FBTestProperties.getUrl() + "?useFirebirdAutocommit=true";
        checkFirebirdAutocommitValue(url, true);
    }

    private void checkFirebirdAutocommitValue(String url, boolean expectedUseFirebirdAutocommit) throws SQLException {
        FBConnection connection = (FBConnection) DriverManager.getConnection(url, FBTestProperties.DB_USER,
                FBTestProperties.DB_PASSWORD);

        try {
            FBManagedConnectionFactory managedConnectionFactory = (FBManagedConnectionFactory) connection
                    .getManagedConnection().getManagedConnectionFactory();
            assertEquals("useFirebirdAutocommit",
                    expectedUseFirebirdAutocommit, managedConnectionFactory.isUseFirebirdAutocommit());
        } finally {
            connection.close();
        }
    }

    @Test
    public void connectionPropertyUseFirebirdAutocommit_fromProperties_valueFalse() throws Exception {
        Properties properties = FBTestProperties.getDefaultPropertiesForConnection();
        properties.put("useFirebirdAutocommit", "false");
        FirebirdConnection connection = (FirebirdConnection) DriverManager.getConnection(FBTestProperties.getUrl(),
                properties);
        try {
            assertFalse(connection.isUseFirebirdAutoCommit());
        } finally {
            connection.close();
        }
    }

    @Test
    public void connectionPropertyUseFirebirdAutocommit_fromProperties_valueTrue() throws Exception {
        Properties properties = FBTestProperties.getDefaultPropertiesForConnection();
        properties.put("useFirebirdAutocommit", "true");
        FirebirdConnection connection = (FirebirdConnection) DriverManager.getConnection(FBTestProperties.getUrl(),
                properties);
        try {
            assertTrue(connection.isUseFirebirdAutoCommit());
        } finally {
            connection.close();
        }
    }

    /**
     * Tests if an auto-commit still closes the result set,
     * <p>
     * Implementation note: the result set is only closed client-side until the real transaction
     * is ended.
     * </p>
     */
    @Test
    public void testResultSetClosedOnOtherStatementExecute() throws Exception {
        FirebirdConnection connection = getFirebirdAutocommitConnection();
        try {
            Statement stmt1 = connection.createStatement();
            Statement stmt2 = connection.createStatement();

            ResultSet rs1 = stmt1.executeQuery("select * from rdb$database");
            assertFalse("Expected open result set from stmt1", rs1.isClosed());

            ResultSet rs2 = stmt2.executeQuery("select * from rdb$database");
            assertTrue("Expected closed result set from stmt1", rs1.isClosed());
            assertFalse("Expected open result set from stmt2", rs2.isClosed());
        } finally {
            connection.close();
        }
    }

    /**
     * Transactions with isc_tpb_autocommit commit during a statement execute.
     * <p>
     * This test verifies if isc_tpb_autocommit is really used by executing a selectable stored procedure that inserts
     * on each read.
     * </p>
     */
    @Test
    public void firebirdAutoCommitCommitsDuringExecution() throws Exception {
        final Connection normalConnection = FBTestProperties.getConnectionViaDriverManager();
        try {
            class VerifyInsert {
                PreparedStatement verifyInsert;
                int count = 0;
                int previousCount = -1;

                VerifyInsert() throws SQLException {
                    verifyInsert = normalConnection.prepareStatement("select count(*) from trackselect");
                }

                int getCurrentCount() throws SQLException {
                    normalConnection.commit();
                    ResultSet rs = verifyInsert.executeQuery();
                    try {
                        rs.next();
                        return rs.getInt(1);
                    } finally {
                        rs.close();
                    }
                }

                void checkCurrentCountLargerThanPrevious() throws SQLException {
                    count = getCurrentCount();
                    // Firebird seems to prefetch occasionally, this might give spurious failures so we do
                    // a more convoluted check then simply count > previousCount
                    // this also prevents an additional check at the limit of the SP
                    assertTrue(String.format("%d + 1 should be larger than %d", count, previousCount),
                            count > 0 && count + 1 > previousCount);
                    previousCount = count;
                }
            }
            normalConnection.setAutoCommit(false);
            executeCreateTable(normalConnection, "CREATE TABLE trackselect (insertValue integer)");
            //@formatter:off
            executeDDL(normalConnection,
                    "CREATE PROCEDURE selectInsert " +
                    "  RETURNS (selected integer) " +
                    "AS " +
                    "  DECLARE counter integer; " +
                    "BEGIN " +
                    "  counter = 0; " +
                    "  while (counter < 100) do " +
                    "  begin " +
                    "    counter = counter + 1; " +
                    "    selected = counter; " +
                    "    insert into trackselect (insertValue) values (:counter); " +
                    "    suspend; " +
                    "  end " +
                    "END");
            //@formatter:on
            normalConnection.commit();

            FirebirdConnection fbAutocommitConnection = getFirebirdAutocommitConnection();
            try {
                assertTrue("Test should be in Firebird autocommit", fbAutocommitConnection.isUseFirebirdAutoCommit());
                VerifyInsert verifyInsert = new VerifyInsert();
                Statement selectInsert = fbAutocommitConnection.createStatement();
                selectInsert.setFetchSize(1);
                ResultSet rs = selectInsert.executeQuery("select selected from selectInsert");
                assertEquals(0, verifyInsert.getCurrentCount());

                while (rs.next()) {
                    verifyInsert.checkCurrentCountLargerThanPrevious();
                }

                assertEquals("Unexpected nr of items", 100, verifyInsert.getCurrentCount());
            } finally {
                fbAutocommitConnection.close();
            }
        } finally {
            normalConnection.close();
        }
    }

    private FirebirdConnection getFirebirdAutocommitConnection() throws SQLException {
        return (FirebirdConnection) DriverManager.getConnection(
                FBTestProperties.getUrl(), getFirebirdAutocommitProperties());
    }

    private static Properties getFirebirdAutocommitProperties() {
        Properties properties = FBTestProperties.getDefaultPropertiesForConnection();
        properties.put("useFirebirdAutocommit", "true");
        return properties;
    }

}
