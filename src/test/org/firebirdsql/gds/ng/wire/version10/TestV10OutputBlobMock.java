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
package org.firebirdsql.gds.ng.wire.version10;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.FbBlob;
import org.firebirdsql.gds.ng.listeners.DatabaseListener;
import org.firebirdsql.gds.ng.listeners.TransactionListener;
import org.firebirdsql.gds.ng.wire.FbWireDatabase;
import org.firebirdsql.gds.ng.wire.FbWireTransaction;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.SQLNonTransientException;

import static org.firebirdsql.common.matchers.SQLExceptionMatchers.errorCodeEquals;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.fbMessageStartsWith;
import static org.hamcrest.CoreMatchers.allOf;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link org.firebirdsql.gds.ng.wire.version10.V10OutputBlob} that don't require
 * a connection to the database.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class TestV10OutputBlobMock {

    @Rule
    public final JUnitRuleMockery context = new JUnitRuleMockery();

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private FbWireDatabase db;
    private FbWireTransaction transaction;

    @Before
    public void setUp() {
        db = context.mock(FbWireDatabase.class);
        transaction = context.mock(FbWireTransaction.class);
        context.checking(new Expectations() {{
            allowing(db).getSynchronizationObject();
            will(returnValue(new Object()));
            allowing(transaction).addTransactionListener(with(any(TransactionListener.class)));
            allowing(transaction).addWeakTransactionListener(with(any(TransactionListener.class)));
            allowing(db).addDatabaseListener(with(any(DatabaseListener.class)));
            allowing(db).addWeakDatabaseListener(with(any(DatabaseListener.class)));
        }});
    }

    /**
     * Test if calling {@link org.firebirdsql.gds.ng.wire.version10.V10OutputBlob#getSegment(int)} throws
     * a {@link java.sql.SQLNonTransientException} with error {@link org.firebirdsql.gds.ISCConstants#isc_segstr_no_read}.
     */
    @Test
    public final void testGetSegment() throws Exception {
        expectedException.expect(SQLNonTransientException.class);
        expectedException.expect(allOf(
                errorCodeEquals(ISCConstants.isc_segstr_no_read),
                fbMessageStartsWith(ISCConstants.isc_segstr_no_read)
        ));

        V10OutputBlob blob = new V10OutputBlob(db, transaction, null);

        blob.getSegment(1);
    }

    /**
     * Test if calling {@link org.firebirdsql.gds.ng.wire.version10.V10OutputBlob#seek(int, org.firebirdsql.gds.ng.FbBlob.SeekMode)}
     * throws a {@link java.sql.SQLNonTransientException} with error {@link org.firebirdsql.gds.ISCConstants#isc_segstr_no_read}.
     */
    @Test
    public final void testSeek() throws Exception {
        expectedException.expect(SQLNonTransientException.class);
        expectedException.expect(allOf(
                errorCodeEquals(ISCConstants.isc_segstr_no_read),
                fbMessageStartsWith(ISCConstants.isc_segstr_no_read)
        ));

        V10OutputBlob blob = new V10OutputBlob(db, transaction, null);

        blob.seek(0, FbBlob.SeekMode.ABSOLUTE);
    }

    @Test
    public void testNewBlob_eof() {
        V10OutputBlob blob = new V10OutputBlob(db, transaction, null);

        assertTrue("Expected new output blob to be EOF", blob.isEof());
    }
}
