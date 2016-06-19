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
import org.firebirdsql.gds.ng.TransactionState;
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

import java.sql.SQLException;
import java.sql.SQLNonTransientException;

import static org.firebirdsql.common.matchers.SQLExceptionMatchers.*;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.jmock.Expectations.returnValue;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link org.firebirdsql.gds.ng.wire.version10.V10InputBlob} that don't require
 * a connection to the database.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class TestV10InputBlobMock {

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
            allowing(db).addDatabaseListener(with(any(DatabaseListener.class)));
        }});
    }


    /**
     * Test if calling {@link org.firebirdsql.gds.ng.wire.version10.V10InputBlob#putSegment(byte[])} throws
     * a {@link java.sql.SQLNonTransientException} with error {@link org.firebirdsql.gds.ISCConstants#isc_segstr_no_write}.
     */
    @Test
    public void testPutSegment() throws Exception {
        expectedException.expect(SQLNonTransientException.class);
        expectedException.expect(allOf(
                errorCodeEquals(ISCConstants.isc_segstr_no_write),
                fbMessageStartsWith(ISCConstants.isc_segstr_no_write)
        ));

        V10InputBlob blob = new V10InputBlob(db, transaction, null, 1);

        blob.putSegment(new byte[] { 1, 2, 3, 4 });
    }

    /**
     * Test if {@link org.firebirdsql.gds.ng.wire.version10.V10InputBlob#getSegment(int)} with zero
     * throws an exception
     */
    @Test
    public void testGetSegment_requestedSizeZero() throws Exception {
        expectedException.expect(SQLException.class);
        //noinspection RedundantTypeArguments
        expectedException.expect(
                message(startsWith("getSegment called with sizeRequested 0, should be > 0")));

        V10InputBlob blob = new V10InputBlob(db, transaction, null, 1);

        blob.getSegment(0);
    }

    /**
     * Test if {@link org.firebirdsql.gds.ng.wire.version10.V10InputBlob#getSegment(int)} with less than
     * zero throws an exception
     */
    @Test
    public void testGetSegment_requestedSizeLessThanZero() throws Exception {
        expectedException.expect(SQLException.class);
        //noinspection RedundantTypeArguments
        expectedException.expect(
                message(startsWith("getSegment called with sizeRequested -1, should be > 0")));

        V10InputBlob blob = new V10InputBlob(db, transaction, null, 1);

        blob.getSegment(-1);
    }

    /**
     * Test if {@link org.firebirdsql.gds.ng.wire.version10.V10InputBlob#getSegment(int)} on closed blob
     * throws exception
     */
    @Test
    public void testGetSegment_blobClosed() throws Exception {
        expectedException.expect(SQLNonTransientException.class);
        expectedException.expect(allOf(
                errorCodeEquals(ISCConstants.isc_bad_segstr_handle),
                fbMessageStartsWith(ISCConstants.isc_bad_segstr_handle)
        ));

        V10InputBlob blob = new V10InputBlob(db, transaction, null, 1);

        final Expectations exp = new Expectations();
        exp.oneOf(db).isAttached();
        exp.will(returnValue(true));
        exp.oneOf(transaction).getState();
        exp.will(returnValue(TransactionState.ACTIVE));
        context.checking(exp);

        blob.getSegment(1);
    }

    @Test
    public void testIsEof_newBlob() {
        V10InputBlob blob = new V10InputBlob(db, transaction, null, 1);

        assertTrue("Expected new input blob to be EOF", blob.isEof());
    }
}
