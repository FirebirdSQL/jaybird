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
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.function.Executable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.errorCodeEquals;
import static org.firebirdsql.jaybird.fb.constants.TpbItems.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TableReservationTest {

    private static final String CREATE_TABLE_1 = ""
            + "CREATE TABLE table_1("
            + "  ID INTEGER NOT NULL PRIMARY KEY"
            + ")";

    private static final String CREATE_TABLE_2 = ""
            + "CREATE TABLE table_2("
            + "  ID INTEGER NOT NULL PRIMARY KEY"
            + ")";

    private static final String INSERT_TABLE_1 =
            "INSERT INTO table_1 VALUES(?)";

    private static final String SELECT_TABLE_1 =
            "SELECT id FROM table_1 WHERE id = ?";

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll(
            CREATE_TABLE_1,
            CREATE_TABLE_2);

    private FirebirdConnection connection1;
    private FirebirdConnection connection2;

    @BeforeEach
    void setUp() throws Exception {
        connection1 = getConnectionViaDriverManager();
        try (Statement stmt = connection1.createStatement()) {
            stmt.execute("delete from table_1");
            stmt.execute("delete from table_2");
        }
        connection1.setAutoCommit(false);

        connection2 = getConnectionViaDriverManager();
        connection2.setAutoCommit(false);
    }

    @AfterEach
    void tearDown() throws Exception {
        //noinspection EmptyTryBlock
        try (Connection ignored1 = connection1;
             Connection ignored2 = connection2) {
            // closing connections
        }
    }

    private void execute(Connection connection, String sql, Object[] params) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            boolean query = stmt.execute();
            if (query) {
                ResultSet rs = stmt.getResultSet();
                while (rs.next()) {
                    rs.getObject(1);
                }
            }
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void prepareTPB(FirebirdConnection connection,
            int isolationLevel, int lockMode, String tableName, int lockType, boolean readOnly)
            throws SQLException {
        TransactionParameterBuffer tpb = connection.createTransactionParameterBuffer();

        // specify new isolation level
        tpb.addArgument(isolationLevel);
        if (isolationLevel == isc_tpb_read_committed)
            tpb.addArgument(isc_tpb_rec_version);

        tpb.addArgument(!readOnly ? isc_tpb_write : isc_tpb_read);
        tpb.addArgument(isc_tpb_nowait);

        tpb.addArgument(lockMode, tableName);
        tpb.addArgument(lockType);

        connection.setTransactionParameters(tpb);
    }

    @Test
    void testProtectedWriteProtectedWrite() throws Exception {
        prepareTPB(connection1, isc_tpb_consistency, isc_tpb_lock_write, "TABLE_1", isc_tpb_protected, false);
        prepareTPB(connection2, isc_tpb_consistency, isc_tpb_lock_write, "TABLE_1", isc_tpb_protected, false);

        execute(connection1, INSERT_TABLE_1, new Object[] { 1 });

        assertLockConflict(() -> execute(connection2, SELECT_TABLE_1, new Object[] { 1 }));
    }

    @Test
    void testProtectedWriteProtectedRead() throws Exception {
        prepareTPB(connection1, isc_tpb_consistency, isc_tpb_lock_write, "TABLE_1", isc_tpb_protected, false);
        prepareTPB(connection2, isc_tpb_consistency, isc_tpb_lock_read, "TABLE_1", isc_tpb_protected, false);

        execute(connection1, INSERT_TABLE_1, new Object[] { 1 });
        assertLockConflict(() -> execute(connection2, SELECT_TABLE_1, new Object[] { 1 }));
    }

    @Test
    void testProtectedWriteSharedWrite() throws Exception {
        prepareTPB(connection1, isc_tpb_consistency, isc_tpb_lock_write, "TABLE_1", isc_tpb_protected, false);
        prepareTPB(connection2, isc_tpb_consistency, isc_tpb_lock_write, "TABLE_1", isc_tpb_shared, false);

        execute(connection1, INSERT_TABLE_1, new Object[] { 1 });
        assertLockConflict(() -> execute(connection2, SELECT_TABLE_1, new Object[] { 1 }));
    }

    @Test
    void testProtectedWriteSharedRead() throws Exception {
        prepareTPB(connection1, isc_tpb_consistency, isc_tpb_lock_write, "TABLE_1", isc_tpb_protected, false);
        prepareTPB(connection2, isc_tpb_consistency, isc_tpb_lock_read, "TABLE_1", isc_tpb_shared, false);

        execute(connection1, INSERT_TABLE_1, new Object[] { 1 });
        assertLockConflict(() -> execute(connection2, SELECT_TABLE_1, new Object[] { 1 }));
    }

    @Test
    void testSharedWriteSharedRead() throws Exception {
        prepareTPB(connection1, isc_tpb_consistency, isc_tpb_lock_write, "TABLE_1", isc_tpb_shared, false);
        prepareTPB(connection2, isc_tpb_consistency, isc_tpb_lock_read, "TABLE_1", isc_tpb_shared, false);

        execute(connection1, INSERT_TABLE_1, new Object[] { 1 });
        assertLockConflict(() -> execute(connection2, SELECT_TABLE_1, new Object[] { 1 }));
    }

    @Test
    void testSharedReadSharedRead() throws Exception {
        prepareTPB(connection1, isc_tpb_consistency, isc_tpb_lock_read, "TABLE_1", isc_tpb_shared, false);
        prepareTPB(connection2, isc_tpb_consistency, isc_tpb_lock_read, "TABLE_1", isc_tpb_shared, false);

        execute(connection1, SELECT_TABLE_1, new Object[] { 1 });
        execute(connection2, SELECT_TABLE_1, new Object[] { 1 });
    }

    @Test
    void testProtectedReadSharedRead() throws Exception {
        prepareTPB(connection1, isc_tpb_consistency, isc_tpb_lock_read, "TABLE_1", isc_tpb_protected, false);
        prepareTPB(connection2, isc_tpb_consistency, isc_tpb_lock_read, "TABLE_1", isc_tpb_shared, false);

        execute(connection1, SELECT_TABLE_1, new Object[] { 1 });
        execute(connection2, SELECT_TABLE_1, new Object[] { 1 });
    }

    @Test
    void testSharedWriteSharedWrite() throws Exception {
        prepareTPB(connection1, isc_tpb_consistency, isc_tpb_lock_write, "TABLE_1", isc_tpb_shared, false);
        prepareTPB(connection2, isc_tpb_consistency, isc_tpb_lock_write, "TABLE_1", isc_tpb_shared, false);

        execute(connection1, SELECT_TABLE_1, new Object[] { 1 });
        execute(connection2, SELECT_TABLE_1, new Object[] { 1 });
    }

    @Test
    void testSharedWriteProtectedRead() throws Exception {
        prepareTPB(connection1, isc_tpb_consistency, isc_tpb_lock_write, "TABLE_1", isc_tpb_shared, false);
        prepareTPB(connection2, isc_tpb_consistency, isc_tpb_lock_read, "TABLE_1", isc_tpb_protected, false);

        execute(connection1, SELECT_TABLE_1, new Object[] { 1 });
        assertLockConflict(() -> execute(connection2, SELECT_TABLE_1, new Object[] { 1 }));
    }

    @Test
    void testProtectedReadProtectedRead() throws Exception {
        prepareTPB(connection1, isc_tpb_consistency, isc_tpb_lock_read, "TABLE_1", isc_tpb_protected, false);
        prepareTPB(connection2, isc_tpb_consistency, isc_tpb_lock_read, "TABLE_1", isc_tpb_protected, false);

        execute(connection1, SELECT_TABLE_1, new Object[] { 1 });
        execute(connection2, SELECT_TABLE_1, new Object[] { 1 });
    }

    private void assertLockConflict(Executable executable) {
        SQLException exception = assertThrows(SQLException.class, executable);
        assertThat(exception, errorCodeEquals(ISCConstants.isc_lock_conflict));
    }

}
