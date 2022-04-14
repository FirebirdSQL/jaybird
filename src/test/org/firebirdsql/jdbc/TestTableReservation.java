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

import org.firebirdsql.common.FBJUnit4TestBase;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.firebirdsql.common.DdlHelper.executeCreateTable;
import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.jaybird.fb.constants.TpbItems.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TestTableReservation extends FBJUnit4TestBase {

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

    private FirebirdConnection connection1;
    private FirebirdConnection connection2;

    @Before
    public void setUp() throws Exception {
        try (Connection connection = getConnectionViaDriverManager()) {
            executeCreateTable(connection, CREATE_TABLE_1);
            executeCreateTable(connection, CREATE_TABLE_2);
        }

        connection1 = getConnectionViaDriverManager();
        connection1.setAutoCommit(false);

        connection2 = getConnectionViaDriverManager();
        connection2.setAutoCommit(false);
    }

    @After
    public void tearDown() throws Exception {
        try (Connection ignored1 = connection1;
             Connection ignored2 = connection2) {
            log.debug("closing connections");
        }
    }

    protected void execute(Connection connection, String sql, Object[] params) throws SQLException {
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

    protected void prepareTPB(FirebirdConnection connection,
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
    public void testProtectedWriteProtectedWrite() {
        try {
            prepareTPB(connection1, isc_tpb_consistency, isc_tpb_lock_write, "TABLE_1", isc_tpb_protected, false);
            prepareTPB(connection2, isc_tpb_consistency, isc_tpb_lock_write, "TABLE_1", isc_tpb_protected, false);

            execute(connection1, INSERT_TABLE_1, new Object[] { 1 });
            execute(connection2, SELECT_TABLE_1, new Object[] { 1 });

            fail();
        } catch (SQLException ex) {
            int fbErrorCode = ex.getErrorCode();
            assertEquals(ISCConstants.isc_lock_conflict, fbErrorCode);
        }
    }

    @Test
    public void testProtectedWriteProtectedRead() {
        try {
            prepareTPB(connection1, isc_tpb_consistency, isc_tpb_lock_write, "TABLE_1", isc_tpb_protected, false);
            prepareTPB(connection2, isc_tpb_consistency, isc_tpb_lock_read, "TABLE_1", isc_tpb_protected, false);

            execute(connection1, INSERT_TABLE_1, new Object[] { 1 });
            execute(connection2, SELECT_TABLE_1, new Object[] { 1 });

            fail();
        } catch (SQLException ex) {
            int fbErrorCode = ex.getErrorCode();
            assertEquals(ISCConstants.isc_lock_conflict, fbErrorCode);
        }
    }

    @Test
    public void testProtectedWriteSharedWrite() {
        try {
            prepareTPB(connection1, isc_tpb_consistency, isc_tpb_lock_write, "TABLE_1", isc_tpb_protected, false);
            prepareTPB(connection2, isc_tpb_consistency, isc_tpb_lock_write, "TABLE_1", isc_tpb_shared, false);

            execute(connection1, INSERT_TABLE_1, new Object[] { 1 });
            execute(connection2, SELECT_TABLE_1, new Object[] { 1 });

            fail();
        } catch (SQLException ex) {
            int fbErrorCode = ex.getErrorCode();
            assertEquals(ISCConstants.isc_lock_conflict, fbErrorCode);
        }
    }

    @Test
    public void testProtectedWriteSharedRead() {
        try {
            prepareTPB(connection1, isc_tpb_consistency, isc_tpb_lock_write, "TABLE_1", isc_tpb_protected, false);
            prepareTPB(connection2, isc_tpb_consistency, isc_tpb_lock_read, "TABLE_1", isc_tpb_shared, false);

            execute(connection1, INSERT_TABLE_1, new Object[] { 1 });
            execute(connection2, SELECT_TABLE_1, new Object[] { 1 });

            fail();
        } catch (SQLException ex) {
            int fbErrorCode = ex.getErrorCode();
            assertEquals(ISCConstants.isc_lock_conflict, fbErrorCode);
        }
    }

    @Test
    public void testSharedWriteSharedRead() {
        try {
            prepareTPB(connection1, isc_tpb_consistency, isc_tpb_lock_write, "TABLE_1", isc_tpb_shared, false);
            prepareTPB(connection2, isc_tpb_consistency, isc_tpb_lock_read, "TABLE_1", isc_tpb_shared, false);

            execute(connection1, INSERT_TABLE_1, new Object[] { 1 });
            execute(connection2, SELECT_TABLE_1, new Object[] { 1 });

            fail();
        } catch (SQLException ex) {
            int fbErrorCode = ex.getErrorCode();
            assertEquals(ISCConstants.isc_lock_conflict, fbErrorCode);
        }
    }

    @Test
    public void testSharedReadSharedRead() throws SQLException {
        prepareTPB(connection1, isc_tpb_consistency, isc_tpb_lock_read, "TABLE_1", isc_tpb_shared, false);
        prepareTPB(connection2, isc_tpb_consistency, isc_tpb_lock_read, "TABLE_1", isc_tpb_shared, false);

        execute(connection1, SELECT_TABLE_1, new Object[] { 1 });
        execute(connection2, SELECT_TABLE_1, new Object[] { 1 });
    }

    @Test
    public void testProtectedReadSharedRead() throws SQLException {
        prepareTPB(connection1, isc_tpb_consistency, isc_tpb_lock_read, "TABLE_1", isc_tpb_protected, false);
        prepareTPB(connection2, isc_tpb_consistency, isc_tpb_lock_read, "TABLE_1", isc_tpb_shared, false);

        execute(connection1, SELECT_TABLE_1, new Object[] { 1 });
        execute(connection2, SELECT_TABLE_1, new Object[] { 1 });
    }

    @Test
    public void testSharedWriteSharedWrite() throws SQLException {
        prepareTPB(connection1, isc_tpb_consistency, isc_tpb_lock_write, "TABLE_1", isc_tpb_shared, false);
        prepareTPB(connection2, isc_tpb_consistency, isc_tpb_lock_write, "TABLE_1", isc_tpb_shared, false);

        execute(connection1, SELECT_TABLE_1, new Object[] { 1 });
        execute(connection2, SELECT_TABLE_1, new Object[] { 1 });
    }

    @Test
    public void testSharedWriteProtectedRead() {
        try {
            prepareTPB(connection1, isc_tpb_consistency, isc_tpb_lock_write, "TABLE_1", isc_tpb_shared, false);
            prepareTPB(connection2, isc_tpb_consistency, isc_tpb_lock_read, "TABLE_1", isc_tpb_protected, false);

            execute(connection1, SELECT_TABLE_1, new Object[] { 1 });
            execute(connection2, SELECT_TABLE_1, new Object[] { 1 });

            fail();
        } catch (SQLException ex) {
            int fbErrorCode = ex.getErrorCode();
            assertEquals(ISCConstants.isc_lock_conflict, fbErrorCode);
        }
    }

    @Test
    public void testProtectedReadProtectedRead() throws SQLException {
        prepareTPB(connection1, isc_tpb_consistency, isc_tpb_lock_read, "TABLE_1", isc_tpb_protected, false);
        prepareTPB(connection2, isc_tpb_consistency, isc_tpb_lock_read, "TABLE_1", isc_tpb_protected, false);

        execute(connection1, SELECT_TABLE_1, new Object[] { 1 });
        execute(connection2, SELECT_TABLE_1, new Object[] { 1 });
    }

}
