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
package org.firebirdsql.gds.ng.jna;

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.extension.GdsTypeExtension;
import org.firebirdsql.gds.ng.AbstractStatementTest;
import org.firebirdsql.gds.ng.DatatypeCoder;
import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.gds.ng.wire.SimpleStatementListener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for JNA statement.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
class JnaStatementTest extends AbstractStatementTest {

    @RegisterExtension
    static final GdsTypeExtension testType = GdsTypeExtension.supportsNativeOnly();

    private final AbstractNativeDatabaseFactory factory =
            (AbstractNativeDatabaseFactory) FBTestProperties.getFbDatabaseFactory();

    @Override
    protected Class<? extends FbDatabase> getExpectedDatabaseType() {
        return JnaDatabase.class;
    }

    @Override
    protected FbDatabase createDatabase() throws SQLException {
        return factory.connect(connectionInfo);
    }

    @Test
    @Override
    public void testSelect_NoParameters_Execute_and_Fetch() throws Exception {
        allocateStatement();
        statement.prepare(SELECT_FROM_RDB$DATABASE);

        final SimpleStatementListener statementListener = new SimpleStatementListener();
        statement.addStatementListener(statementListener);

        statement.execute(RowValue.EMPTY_ROW_VALUE);

        assertEquals(Boolean.TRUE, statementListener.hasResultSet(), "Expected hasResultSet to be set to true");
        assertEquals(Boolean.FALSE, statementListener.hasSingletonResult(), "Expected hasSingletonResult to be set to false");
        assertNotEquals(Boolean.TRUE, statementListener.isAfterLast(), "Expected afterLast not set yet");
        assertEquals(0, statementListener.getRows().size(), "Expected no rows to be fetched yet");

        // JNAStatement only executes a single fetch to prevent problems with positioned updates,
        // so this doesn't get all rows fetched immediately
        statement.fetchRows(10);

        assertNotEquals(Boolean.TRUE, statementListener.isAfterLast(), "Expected afterLast to haven't been called yet");
        assertEquals(1, statementListener.getRows().size(), "Expected a single row to have been fetched");

        statement.fetchRows(1);

        assertEquals(Boolean.TRUE, statementListener.isAfterLast(), "Expected afterLast to be set to true");
        assertEquals(1, statementListener.getRows().size(), "Expected a single row to have been fetched");
    }

    @Test
    public void testMultipleExecute() throws Exception {
        allocateStatement();
        statement.prepare(SELECT_FROM_RDB$DATABASE);

        final SimpleStatementListener statementListener = new SimpleStatementListener();
        statement.addStatementListener(statementListener);

        statement.execute(RowValue.EMPTY_ROW_VALUE);

        assertEquals(Boolean.TRUE, statementListener.hasResultSet(), "Expected hasResultSet to be set to true");
        assertEquals(Boolean.FALSE, statementListener.hasSingletonResult(), "Expected hasSingletonResult to be set to false");
        assertNotEquals(Boolean.TRUE, statementListener.isAfterLast(), "Expected afterLast not set yet");
        assertEquals(0, statementListener.getRows().size(), "Expected no rows to be fetched yet");

        // JNAStatement only executes a single fetch to prevent problems with positioned updates,
        // so this doesn't get all rows fetched immediately
        statement.fetchRows(10);

        assertNotEquals(Boolean.TRUE, statementListener.isAfterLast(), "Expected afterLast to haven't been called yet");
        assertEquals(1, statementListener.getRows().size(), "Expected a single row to have been fetched");

        statement.fetchRows(1);

        assertEquals(Boolean.TRUE, statementListener.isAfterLast(), "Expected afterLast to be set to true");
        assertEquals(1, statementListener.getRows().size(), "Expected a single row to have been fetched");

        statement.closeCursor();
        final SimpleStatementListener statementListener2 = new SimpleStatementListener();
        statement.addStatementListener(statementListener2);
        statement.execute(RowValue.EMPTY_ROW_VALUE);

        assertEquals(Boolean.TRUE, statementListener2.hasResultSet(), "Expected hasResultSet to be set to true");
        assertEquals(Boolean.FALSE, statementListener2.hasSingletonResult(), "Expected hasSingletonResult to be set to false");
        assertNotEquals(Boolean.TRUE, statementListener2.isAfterLast(), "Expected afterLast not set yet");
        assertEquals(0, statementListener2.getRows().size(), "Expected no rows to be fetched yet");

        // JNAStatement only executes a single fetch to prevent problems with positioned updates,
        // so this doesn't get all rows fetched immediately
        statement.fetchRows(10);

        assertNotEquals(Boolean.TRUE, statementListener2.isAfterLast(), "Expected afterLast to haven't been called yet");
        assertEquals(1, statementListener2.getRows().size(), "Expected a single row to have been fetched");

        statement.fetchRows(1);

        assertEquals(Boolean.TRUE, statementListener2.isAfterLast(), "Expected afterLast to be set to true");
        assertEquals(1, statementListener2.getRows().size(), "Expected a single row to have been fetched");
    }

    @Test
    public void testMultiplePrepare() throws Exception {
        allocateStatement();
        statement.prepare(SELECT_FROM_RDB$DATABASE);

        final SimpleStatementListener statementListener = new SimpleStatementListener();
        statement.addStatementListener(statementListener);

        statement.execute(RowValue.EMPTY_ROW_VALUE);

        assertEquals(Boolean.TRUE, statementListener.hasResultSet(), "Expected hasResultSet to be set to true");
        assertEquals(Boolean.FALSE, statementListener.hasSingletonResult(), "Expected hasSingletonResult to be set to false");
        assertNotEquals(Boolean.TRUE, statementListener.isAfterLast(), "Expected afterLast not set yet");
        assertEquals(0, statementListener.getRows().size(), "Expected no rows to be fetched yet");

        // JNAStatement only executes a single fetch to prevent problems with positioned updates,
        // so this doesn't get all rows fetched immediately
        statement.fetchRows(10);

        assertNotEquals(Boolean.TRUE, statementListener.isAfterLast(), "Expected afterLast to haven't been called yet");
        assertEquals(1, statementListener.getRows().size(), "Expected a single row to have been fetched");

        statement.fetchRows(1);

        assertEquals(Boolean.TRUE, statementListener.isAfterLast(), "Expected afterLast to be set to true");
        assertEquals(1, statementListener.getRows().size(), "Expected a single row to have been fetched");

        statement.closeCursor();

        statement.prepare(SELECT_FROM_RDB$DATABASE);

        final SimpleStatementListener statementListener2 = new SimpleStatementListener();
        statement.addStatementListener(statementListener2);
        statement.execute(RowValue.EMPTY_ROW_VALUE);

        assertEquals(Boolean.TRUE, statementListener2.hasResultSet(), "Expected hasResultSet to be set to true");
        assertEquals(Boolean.FALSE, statementListener2.hasSingletonResult(), "Expected hasSingletonResult to be set to false");
        assertNotEquals(Boolean.TRUE, statementListener2.isAfterLast(), "Expected afterLast not set yet");
        assertEquals(0, statementListener2.getRows().size(), "Expected no rows to be fetched yet");

        // JNAStatement only executes a single fetch to prevent problems with positioned updates,
        // so this doesn't get all rows fetched immediately
        statement.fetchRows(10);

        assertNotEquals(Boolean.TRUE, statementListener2.isAfterLast(), "Expected afterLast to haven't been called yet");
        assertEquals(1, statementListener2.getRows().size(), "Expected a single row to have been fetched");

        statement.fetchRows(1);

        assertEquals(Boolean.TRUE, statementListener2.isAfterLast(), "Expected afterLast to be set to true");
        assertEquals(1, statementListener2.getRows().size(), "Expected a single row to have been fetched");
    }

    @Test
    public void testSelect_WithParameters_Execute_and_Fetch() throws Exception {
        allocateStatement();
        statement.addStatementListener(listener);
        statement.prepare(SELECT_CHARSET_BY_ID_OR_SIZE);

        final DatatypeCoder coder = db.getDatatypeCoder();
        RowValue rowValue = RowValue.of(
                coder.encodeShort(3),  // smallint = 3 (id of UNICODE_FSS)
                coder.encodeShort(1)); // smallint = 1 (single byte character sets)

        statement.execute(rowValue);

        assertEquals(Boolean.TRUE, listener.hasResultSet(), "Expected hasResultSet to be set to true");
        assertEquals(Boolean.FALSE, listener.hasSingletonResult(), "Expected hasSingletonResult to be set to false");
        assertNotEquals(Boolean.TRUE, listener.isAfterLast(), "Expected afterLast not set yet");
        assertEquals(0, listener.getRows().size(), "Expected no rows to be fetched yet");
        assertNull(listener.getSqlCounts(), "Expected no SQL counts yet");

        // JNAStatement only executes a single fetch to prevent problems with positioned updates,
        // so this doesn't get all rows fetched immediately
        statement.fetchRows(100);

        assertNotEquals(Boolean.TRUE, listener.isAfterLast(), "Expected afterLast to haven't been called yet");
        assertEquals(1, listener.getRows().size(), "Expected a single row to have been fetched");
        assertEquals(1, listener.getLastFetchCount(), "Expected a single row to have been fetched");

        // 100 should be sufficient to fetch all character sets; limit to prevent infinite loop with bugs in fetchRows
        int count = 0;
        while(listener.isAfterLast() != Boolean.TRUE && count < 100) {
            statement.fetchRows(1);
            count++;
        }

        assertEquals(Boolean.TRUE, listener.isAfterLast(), "Expected afterLast to be set to true");
        // Number is database dependent (unicode_fss + all single byte character sets)
        assertTrue(listener.getRows().size() > 2, "Expected more than two rows");
        assertEquals(1, listener.getLastFetchCount(), "Expected the last fetch to have been a single row");

        assertNull(listener.getSqlCounts(), "expected no SQL counts immediately after retrieving all rows");

        statement.getSqlCounts();

        assertNotNull(listener.getSqlCounts(), "Expected SQL counts");
        assertEquals(listener.getRows().size(), listener.getSqlCounts().getLongSelectCount(), "Unexpected select count");
    }
}
