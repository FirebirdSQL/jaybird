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

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.jaybird.xca.FBManagedConnectionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.sql.*;
import java.util.Properties;

import static java.lang.String.format;
import static org.firebirdsql.common.DdlHelper.executeCreateTable;
import static org.firebirdsql.common.DdlHelper.executeDDL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for "use Firebird autocommit" mode.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.2
 */
class UseFirebirdAutocommitTest {

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll();

    @ParameterizedTest
    @CsvSource({
            "?encoding=NONE,                             false",
            "?encoding=NONE&useFirebirdAutocommit=false, false",
            "?encoding=NONE&useFirebirdAutocommit,       true",
            "?encoding=NONE&useFirebirdAutocommit=true,  true"
    })
    void checkFirebirdAutocommitValue(String properties, boolean expectedUseFirebirdAutocommit) throws SQLException {
        String url = FBTestProperties.getUrl() + properties;
        try (FBConnection connection = (FBConnection) DriverManager.getConnection(url, FBTestProperties.DB_USER,
                FBTestProperties.DB_PASSWORD)) {
            FBManagedConnectionFactory managedConnectionFactory = connection
                    .getManagedConnection().getManagedConnectionFactory();
            assertEquals(expectedUseFirebirdAutocommit, managedConnectionFactory.isUseFirebirdAutocommit(),
                    "useFirebirdAutocommit");
        }
    }

    @Test
    void connectionPropertyUseFirebirdAutocommit_fromProperties_valueFalse() throws Exception {
        Properties properties = FBTestProperties.getDefaultPropertiesForConnection();
        properties.put("useFirebirdAutocommit", "false");
        try (FirebirdConnection connection = (FirebirdConnection) DriverManager.getConnection(FBTestProperties.getUrl(),
                properties)) {
            assertFalse(connection.isUseFirebirdAutoCommit());
        }
    }

    @Test
    void connectionPropertyUseFirebirdAutocommit_fromProperties_valueTrue() throws Exception {
        Properties properties = FBTestProperties.getDefaultPropertiesForConnection();
        properties.put("useFirebirdAutocommit", "true");
        try (FirebirdConnection connection = (FirebirdConnection) DriverManager.getConnection(FBTestProperties.getUrl(),
                properties)) {
            assertTrue(connection.isUseFirebirdAutoCommit());
        }
    }

    @Test
    void connectionPropertyUseFirebirdAutocommit_fromProperties_valueEmpty() throws Exception {
        Properties properties = FBTestProperties.getDefaultPropertiesForConnection();
        properties.put("useFirebirdAutocommit", "");
        try (FirebirdConnection connection = (FirebirdConnection) DriverManager.getConnection(FBTestProperties.getUrl(),
                properties)) {
            assertTrue(connection.isUseFirebirdAutoCommit());
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
    void testResultSetClosedOnOtherStatementExecute() throws Exception {
        try (FirebirdConnection connection = getFirebirdAutocommitConnection()) {
            Statement stmt1 = connection.createStatement();
            Statement stmt2 = connection.createStatement();

            ResultSet rs1 = stmt1.executeQuery("select * from rdb$database");
            assertFalse(rs1.isClosed(), "Expected open result set from stmt1");

            ResultSet rs2 = stmt2.executeQuery("select * from rdb$database");
            assertTrue(rs1.isClosed(), "Expected closed result set from stmt1");
            assertFalse(rs2.isClosed(), "Expected open result set from stmt2");
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
    void firebirdAutoCommitCommitsDuringExecution() throws Exception {
        try (final Connection normalConnection = FBTestProperties.getConnectionViaDriverManager()) {
            class VerifyInsert {
                final PreparedStatement verifyInsert;
                int count = 0;
                int previousCount = -1;

                VerifyInsert() throws SQLException {
                    verifyInsert = normalConnection.prepareStatement("select count(*) from trackselect");
                }

                int getCurrentCount() throws SQLException {
                    normalConnection.commit();
                    try (ResultSet rs = verifyInsert.executeQuery()) {
                        rs.next();
                        return rs.getInt(1);
                    }
                }

                void checkCurrentCountLargerThanPrevious() throws SQLException {
                    count = getCurrentCount();
                    // Firebird seems to prefetch occasionally, this might give spurious failures, so we do
                    // a more convoluted check than simply count > previousCount
                    // this also prevents an additional check at the limit of the SP
                    assertTrue(count > 0 && count + 1 > previousCount,
                            () -> format("%d + 1 should be larger than %d", count, previousCount));
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

            try (FirebirdConnection fbAutocommitConnection = getFirebirdAutocommitConnection()) {
                assertTrue(fbAutocommitConnection.isUseFirebirdAutoCommit(), "Test should be in Firebird autocommit");
                VerifyInsert verifyInsert = new VerifyInsert();
                Statement selectInsert = fbAutocommitConnection.createStatement();
                selectInsert.setFetchSize(1);
                ResultSet rs = selectInsert.executeQuery("select selected from selectInsert");
                assertThat(verifyInsert.getCurrentCount(), lessThan(2));

                while (rs.next()) {
                    verifyInsert.checkCurrentCountLargerThanPrevious();
                }

                assertEquals(100, verifyInsert.getCurrentCount(), "Unexpected nr of items");
            }
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
