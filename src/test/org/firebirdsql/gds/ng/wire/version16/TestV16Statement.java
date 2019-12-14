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
package org.firebirdsql.gds.ng.wire.version16;

import org.firebirdsql.common.rules.RequireProtocol;
import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.FbStatement;
import org.firebirdsql.gds.ng.fields.RowDescriptor;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.gds.ng.wire.SimpleStatementListener;
import org.firebirdsql.gds.ng.wire.version15.TestV15Statement;
import org.junit.ClassRule;
import org.junit.Test;

import java.sql.SQLTimeoutException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.firebirdsql.common.matchers.SQLExceptionMatchers.errorCodeEquals;
import static org.firebirdsql.common.rules.RequireProtocol.requireProtocolVersion;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests for {@link org.firebirdsql.gds.ng.wire.version16.V16Statement} in the V16 protocol, reuses test for V15.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 4.0
 */
public class TestV16Statement extends TestV15Statement {

    @ClassRule
    public static final RequireProtocol requireProtocol = requireProtocolVersion(16);

    public TestV16Statement() {
        this(new V16CommonConnectionInfo());
    }

    protected TestV16Statement(V16CommonConnectionInfo commonConnectionInfo) {
        super(commonConnectionInfo);
    }

    @Test
    public void testStatementTimeout_sufficientForExecute() throws Exception {
        allocateStatement();
        statement.setTimeout(TimeUnit.MINUTES.toMillis(1));

        statement.prepare(SELECT_THEUTFVALUE);
        final RowDescriptor parametersSelect = statement.getParameterDescriptor();
        final RowValue parameterValuesSelect = RowValue.of(parametersSelect,
                db.getDatatypeCoder().encodeInt(1));
        final SimpleStatementListener statementListener = new SimpleStatementListener();
        statement.addStatementListener(statementListener);
        statement.execute(parameterValuesSelect);
        statement.fetchRows(1);

        final List<RowValue> rows = statementListener.getRows();
        assertEquals("Expected no row", 0, rows.size());
    }

    @Test
    public void testStatementTimeout_timeoutBetweenExecuteAndFetch() throws Exception {
        allocateStatement();
        statement.setTimeout(TimeUnit.MILLISECONDS.toMillis(20));

        statement.prepare(SELECT_THEUTFVALUE);
        final RowDescriptor parametersSelect = statement.getParameterDescriptor();
        final RowValue parameterValuesSelect = RowValue.of(parametersSelect,
                db.getDatatypeCoder().encodeInt(1));
        final SimpleStatementListener statementListener = new SimpleStatementListener();
        statement.addStatementListener(statementListener);
        statement.execute(parameterValuesSelect);

        expectedException.expect(SQLTimeoutException.class);
        expectedException.expect(errorCodeEquals(ISCConstants.isc_req_stmt_timeout));

        Thread.sleep(50);

        statement.fetchRows(1);
    }

    @Test
    public void testStatementTimeout_reuseAfterTimeout() throws Exception {
        allocateStatement();
        statement.setTimeout(TimeUnit.MILLISECONDS.toMillis(20));

        statement.prepare(SELECT_THEUTFVALUE);
        final RowDescriptor parametersSelect = statement.getParameterDescriptor();
        final RowValue parameterValuesSelect = RowValue.of(parametersSelect,
                db.getDatatypeCoder().encodeInt(1));
        final SimpleStatementListener statementListener = new SimpleStatementListener();
        statement.addStatementListener(statementListener);
        statement.execute(parameterValuesSelect);

        Thread.sleep(50);

        try {
            statement.fetchRows(1);
            fail("expected timeout to occur");
        } catch (SQLTimeoutException e) {
            // ignore
        }

        statement.setTimeout(0);
        statementListener.clear();
        statement.addStatementListener(statementListener);
        statement.execute(parameterValuesSelect);

        statement.fetchRows(1);
        final List<RowValue> rows = statementListener.getRows();
        assertEquals("Expected no row", 0, rows.size());
    }

    @Test
    public void testStatementTimeout_interleaveOperationWithDifferentStatement() throws Exception {
        // Checks if interleaving operations on another statement will not signal the timeout on that other statement
        allocateStatement();

        // Insert UTF8 columns
        statement.prepare(INSERT_THEUTFVALUE);
        final Encoding utf8Encoding = db.getEncodingFactory().getEncodingForFirebirdName("UTF8");
        final String aEuro = "a\u20AC";
        final byte[] insertFieldData = utf8Encoding.encodeToCharset(aEuro);
        final RowDescriptor parametersInsert = statement.getParameterDescriptor();
        final RowValue parameterValuesInsert = RowValue.of(parametersInsert,
                db.getDatatypeCoder().encodeInt(1),
                insertFieldData,
                insertFieldData);
        statement.execute(parameterValuesInsert);

        statement.setTimeout(TimeUnit.MILLISECONDS.toMillis(20));

        statement.prepare(SELECT_THEUTFVALUE);
        final RowDescriptor parametersSelect = statement.getParameterDescriptor();
        final RowValue parameterValuesSelect = RowValue.of(parametersSelect,
                db.getDatatypeCoder().encodeInt(1));
        final SimpleStatementListener statementListener = new SimpleStatementListener();
        statement.addStatementListener(statementListener);
        statement.execute(parameterValuesSelect);

        Thread.sleep(50);

        FbStatement statement2 = db.createStatement(getOrCreateTransaction());
        statement2.prepare(SELECT_THEUTFVALUE);
        final RowDescriptor parametersSelect2 = statement2.getParameterDescriptor();
        final RowValue parameterValuesSelect2 = RowValue.of(parametersSelect2,
                db.getDatatypeCoder().encodeInt(1));
        final SimpleStatementListener statementListener2 = new SimpleStatementListener();
        statement2.addStatementListener(statementListener2);
        statement2.execute(parameterValuesSelect2);
        statement2.fetchRows(1);
        assertEquals("Expected no row", 1, statementListener2.getRows().size());
        statement2.close();

        try {
            statement.fetchRows(1);
            fail("expected timeout to occur");
        } catch (SQLTimeoutException e) {
            // ignore
        }

        statement.setTimeout(0);
        statementListener.clear();
        statement.addStatementListener(statementListener);
        statement.execute(parameterValuesSelect);

        statement.fetchRows(1);
        final List<RowValue> rows = statementListener.getRows();
        assertEquals("Expected no row", 1, rows.size());
    }
}
