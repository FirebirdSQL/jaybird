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
import org.firebirdsql.common.extension.RequireFeatureExtension;
import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.gds.ng.wire.SimpleStatementListener;
import org.firebirdsql.util.FirebirdSupportInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.errorCodeEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Shared test for statement timeout (Firebird 4).
 *
 * @author Mark Rotteveel
 */
public abstract class AbstractStatementTimeoutTest {

    @RegisterExtension
    @Order(1)
    public static final RequireFeatureExtension requireFeature = RequireFeatureExtension
            .withFeatureCheck(FirebirdSupportInfo::supportsStatementTimeouts, "Requires statement timeout support")
            .build();

    @RegisterExtension
    public static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll();

    protected final SimpleStatementListener listener = new SimpleStatementListener();
    protected FbDatabase db;
    private FbTransaction transaction;
    protected FbStatement statement;
    protected final FbConnectionProperties connectionInfo = FBTestProperties.getDefaultFbConnectionProperties();

    protected abstract Class<? extends FbDatabase> getExpectedDatabaseType();

    @BeforeEach
    public final void setUp() throws Exception {
        db = createDatabase();
        assertEquals(getExpectedDatabaseType(), db.getClass(), "Unexpected FbDatabase implementation");

        db.attach();
    }

    protected abstract FbDatabase createDatabase() throws SQLException;

    @Test
    public void testStatementTimeout_sufficientForExecute() throws Exception {
        allocateStatement();
        statement.setTimeout(TimeUnit.MINUTES.toMillis(1));

        // use 'for update' to force individual fetch
        statement.prepare("SELECT * FROM RDB$RELATIONS FOR UPDATE");
        final SimpleStatementListener statementListener = new SimpleStatementListener();
        statement.addStatementListener(statementListener);
        statement.execute(RowValue.EMPTY_ROW_VALUE);
        statement.fetchRows(1);

        Thread.sleep(100);

        statement.fetchRows(1);

        final List<RowValue> rows = statementListener.getRows();
        assertEquals(2, rows.size(), "Expected no row");
    }

    @Test
    public void testStatementTimeout_timeoutBetweenExecuteAndFetch() throws Exception {
        allocateStatement();
        statement.setTimeout(TimeUnit.MILLISECONDS.toMillis(75));

        // use 'for update' to force individual fetch
        statement.prepare("SELECT * FROM RDB$RELATIONS FOR UPDATE");
        final SimpleStatementListener statementListener = new SimpleStatementListener();
        statement.addStatementListener(statementListener);
        statement.execute(RowValue.EMPTY_ROW_VALUE);
        // fbclient will delay execute until fetch for remote connections
        statement.fetchRows(1);

        Thread.sleep(100);

        SQLException exception = assertThrows(SQLTimeoutException.class, () -> statement.fetchRows(1));
        assertThat(exception, errorCodeEquals(ISCConstants.isc_req_stmt_timeout));
    }

    @Test
    public void testStatementTimeout_reuseAfterTimeout() throws Exception {
        allocateStatement();

        // use 'for update' to force individual fetch
        statement.prepare("SELECT * FROM RDB$RELATIONS FOR UPDATE");
        statement.setTimeout(TimeUnit.MILLISECONDS.toMillis(75));
        final SimpleStatementListener statementListener = new SimpleStatementListener();
        statement.addStatementListener(statementListener);
        statement.execute(RowValue.EMPTY_ROW_VALUE);
        // fbclient will delay execute until fetch for remote connections
        statement.fetchRows(1);

        Thread.sleep(100);

        SQLException exception = assertThrows(SQLTimeoutException.class, () -> statement.fetchRows(1));
        assertThat(exception, errorCodeEquals(ISCConstants.isc_req_stmt_timeout));

        statement.setTimeout(0);
        statementListener.clear();
        statement.addStatementListener(statementListener);
        statement.execute(RowValue.EMPTY_ROW_VALUE);

        statement.fetchRows(1);
        final List<RowValue> rows = statementListener.getRows();
        assertThat("Expected rows", rows, not(empty()));
    }

    @Test
    public void testStatementTimeout_interleaveOperationWithDifferentStatement() throws Exception {
        // Checks if interleaving operations on another statement will not signal the timeout on that other statement
        allocateStatement();

        statement.setTimeout(TimeUnit.MILLISECONDS.toMillis(75));

        // use 'for update' to force individual fetch
        statement.prepare("SELECT * FROM RDB$RELATIONS FOR UPDATE");
        final SimpleStatementListener statementListener = new SimpleStatementListener();
        statement.addStatementListener(statementListener);
        statement.execute(RowValue.EMPTY_ROW_VALUE);
        // fbclient will delay execute until fetch for remote connections
        statement.fetchRows(1);

        Thread.sleep(100);

        FbStatement statement2 = db.createStatement(getOrCreateTransaction());
        // use 'for update' to force individual fetch
        statement2.prepare("SELECT * FROM RDB$RELATIONS FOR UPDATE");
        final SimpleStatementListener statementListener2 = new SimpleStatementListener();
        statement2.addStatementListener(statementListener2);
        statement2.execute(RowValue.EMPTY_ROW_VALUE);
        statement2.fetchRows(1);
        assertEquals(1, statementListener2.getRows().size(), "Expected no row");
        statement2.close();

        SQLException exception = assertThrows(SQLTimeoutException.class, () -> statement.fetchRows(1));
        assertThat(exception, errorCodeEquals(ISCConstants.isc_req_stmt_timeout));

        statement.setTimeout(0);
        statementListener.clear();
        statement.addStatementListener(statementListener);
        statement.execute(RowValue.EMPTY_ROW_VALUE);

        statement.fetchRows(1);
        final List<RowValue> rows = statementListener.getRows();
        assertEquals(1, rows.size(), "Expected a row");
    }

    private FbTransaction getTransaction() throws SQLException {
        return db.startTransaction(getDefaultTpb());
    }

    protected FbTransaction getOrCreateTransaction() throws SQLException {
        if (transaction == null || transaction.getState() != TransactionState.ACTIVE) {
            transaction = getTransaction();
        }
        return transaction;
    }

    protected void allocateStatement() throws SQLException {
        if (transaction == null || transaction.getState() != TransactionState.ACTIVE) {
            transaction = getTransaction();
        }
        statement = db.createStatement(transaction);
    }

    @AfterEach
    public final void tearDown() {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException ex) {
                System.out.println("Exception on statement close");
                ex.printStackTrace();
            }
        }
        if (transaction != null) {
            try {
                transaction.commit();
            } catch (SQLException ex) {
                System.out.println("Exception on transaction commit");
                ex.printStackTrace();
            }
        }
        if (db != null) {
            try {
                db.close();
            } catch (SQLException ex) {
                System.out.println("Exception on detach");
                ex.printStackTrace();
            }
        }
    }
}
