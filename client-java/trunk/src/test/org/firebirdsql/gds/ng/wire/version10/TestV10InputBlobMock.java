/*
 * $Id$
 *
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
package org.firebirdsql.gds.ng.wire.version10;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.listeners.DatabaseListener;
import org.firebirdsql.gds.ng.listeners.TransactionListener;
import org.firebirdsql.gds.ng.wire.FbWireDatabase;
import org.firebirdsql.gds.ng.wire.FbWireTransaction;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.SQLNonTransientException;

import static org.firebirdsql.common.matchers.SQLExceptionMatchers.errorCodeEquals;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.fbMessageEquals;
import static org.hamcrest.CoreMatchers.allOf;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class TestV10InputBlobMock {

    @Rule
    public final JUnitRuleMockery context = new JUnitRuleMockery();

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testPutSegment() throws Exception {
        expectedException.expect(SQLNonTransientException.class);
        expectedException.expect(
                allOf(errorCodeEquals(ISCConstants.isc_segstr_no_write),
                        fbMessageEquals(ISCConstants.isc_segstr_no_write)));

        final FbWireDatabase db = context.mock(FbWireDatabase.class);
        final FbWireTransaction transaction = context.mock(FbWireTransaction.class);
        context.checking(new Expectations() {{
            allowing(transaction).addTransactionListener(with(any(TransactionListener.class)));
            allowing(db).addDatabaseListener(with(any(DatabaseListener.class)));
        }});

        V10InputBlob blob = new V10InputBlob(db, transaction, 1);

        blob.putSegment(new byte[] { 1, 2, 3, 4 });
    }

}
