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
package org.firebirdsql.gds.ng;

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Generic tests for FbTransaction.
 * <p>
 * This abstract class is subclassed by the tests for specific FbTransaction implementations.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public abstract class AbstractTransactionTest {

    //@formatter:off
    protected static final String CREATE_KEY_VALUE_TABLE =
            "CREATE TABLE keyvalue ( " +
            " thekey INTEGER, " +
            " thevalue VARCHAR(255)" +
            ")";
    //@formatter:on

    @RegisterExtension
    public static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll(
            CREATE_KEY_VALUE_TABLE);

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final FbConnectionProperties connectionInfo = FBTestProperties.getDefaultFbConnectionProperties();

    protected FbDatabase db;

    @BeforeEach
    public final void setUp() throws Exception {
        try (Connection con = FBTestProperties.getConnectionViaDriverManager();
             Statement stmt = con.createStatement()) {
            stmt.execute("delete from keyvalue");
        }

        db = createDatabase();
        assertEquals(getExpectedDatabaseType(), db.getClass(), "Unexpected FbDatabase implementation");

        db.attach();
    }

    @AfterEach
    public final void tearDown() throws Exception {
        if (db != null) {
            try {
                db.close();
            } catch (SQLException ex) {
                log.debug("Exception on detach", ex);
            }
        }
    }

    protected abstract FbDatabase createDatabase() throws SQLException;

    protected abstract Class<? extends FbDatabase> getExpectedDatabaseType();

    @Test
    public void testBasicCommit() throws Exception {
        FbTransaction transaction = getTransaction();
        assertEquals(TransactionState.ACTIVE, transaction.getState());
        final int key = 23;
        final String value = "TheValueIs23";
        insertKeyValue(transaction, key, value);
        transaction.commit();
        assertEquals(TransactionState.COMMITTED, transaction.getState());

        assertValueForKey(key, true, value);
    }

    @Test
    public void testBasicRollback() throws Exception {
        FbTransaction transaction = getTransaction();
        assertEquals(TransactionState.ACTIVE, transaction.getState());
        final int key = 23;
        final String value = "TheValueIs23";
        insertKeyValue(transaction, key, value);
        transaction.rollback();
        assertEquals(TransactionState.ROLLED_BACK, transaction.getState());

        assertValueForKey(key, false, null);
    }

    @Test
    public void testBasicPrepareAndCommit() throws Exception {
        FbTransaction transaction = getTransaction();
        assertEquals(TransactionState.ACTIVE, transaction.getState());
        final int key = 23;
        final String value = "TheValueIs23";
        insertKeyValue(transaction, key, value);

        transaction.prepare(new byte[0]);

        assertEquals(TransactionState.PREPARED, transaction.getState());
        assertValueForKey(key, false, null);

        transaction.commit();

        assertEquals(TransactionState.COMMITTED, transaction.getState());
        assertValueForKey(key, true, value);
    }

    @Test
    public void testBasicPrepareAndRollback() throws Exception {
        FbTransaction transaction = getTransaction();
        assertEquals(TransactionState.ACTIVE, transaction.getState());
        final int key = 23;
        final String value = "TheValueIs23";
        insertKeyValue(transaction, key, value);

        transaction.prepare(new byte[] { 68, 69, 70 });

        assertEquals(TransactionState.PREPARED, transaction.getState());
        assertValueForKey(key, false, null);

        transaction.rollback();

        assertEquals(TransactionState.ROLLED_BACK, transaction.getState());
        assertValueForKey(key, false, null);
    }

    @SuppressWarnings("SameParameterValue")
    protected final void assertValueForKey(int key, boolean hasRow, String expectedValue) throws SQLException {
        try (Connection connection = getConnectionViaDriverManager();
             PreparedStatement pstmt = connection.prepareStatement("SELECT thevalue FROM keyvalue WHERE thekey = ?")) {
            pstmt.setInt(1, key);
            try (ResultSet rs = pstmt.executeQuery()) {
                assertEquals(hasRow, rs.next(), "Unexpected value for rs.next()");
                if (hasRow) {
                    assertEquals(expectedValue, rs.getString(1), "Unexpected value for rs.getString(1)");
                }
            }
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void insertKeyValue(FbTransaction transaction, int key, String value) throws SQLException {
        try (FbStatement statement = db.createStatement(transaction)) {
            statement.prepare("INSERT INTO keyvalue (thekey, thevalue) VALUES (?, ?)");

            RowValue rowValue = RowValue.of(
                    db.getDatatypeCoder().encodeInt(key),
                    db.getEncoding().encodeToCharset(value));

            statement.execute(rowValue);
        }
    }

    protected final FbTransaction getTransaction() throws SQLException {
        return db.startTransaction(getDefaultTpb());
    }
}
