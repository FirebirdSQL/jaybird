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
package org.firebirdsql.gds.ng.wire.version10;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.LockCloseable;
import org.firebirdsql.gds.ng.TransactionState;
import org.firebirdsql.gds.ng.wire.FbWireDatabase;
import org.firebirdsql.gds.ng.wire.FbWireTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.sql.SQLNonTransientException;

import static org.firebirdsql.common.matchers.SQLExceptionMatchers.*;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link V10InputBlob} that don't require a connection to the database.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
@ExtendWith(MockitoExtension.class)
class V10InputBlobMockTest {

    @Mock
    private FbWireDatabase db;
    @Mock
    private FbWireTransaction transaction;

    @BeforeEach
    void setUp() {
        lenient().when(db.withLock()).thenReturn(LockCloseable.NO_OP);
    }

    /**
     * Test if calling {@link V10InputBlob#putSegment(byte[])} throws a {@link SQLNonTransientException} with
     * error {@link ISCConstants#isc_segstr_no_write}.
     */
    @Test
    void testPutSegment() {
        V10InputBlob blob = new V10InputBlob(db, transaction, null, 1);

        SQLException exception = assertThrows(SQLNonTransientException.class,
                () -> blob.putSegment(new byte[] { 1, 2, 3, 4 }));
        assertThat(exception, allOf(
                errorCodeEquals(ISCConstants.isc_segstr_no_write),
                fbMessageStartsWith(ISCConstants.isc_segstr_no_write)));
    }

    /**
     * Test if {@link V10InputBlob#getSegment(int)} with zero throws an exception.
     */
    @Test
    void testGetSegment_requestedSizeZero() {
        V10InputBlob blob = new V10InputBlob(db, transaction, null, 1);

        SQLException exception = assertThrows(SQLException.class, () -> blob.getSegment(0));
        assertThat(exception, message(startsWith("getSegment called with sizeRequested 0, should be > 0")));
    }

    /**
     * Test if {@link V10InputBlob#getSegment(int)} with less than zero throws an exception.
     */
    @Test
    void testGetSegment_requestedSizeLessThanZero() {
        V10InputBlob blob = new V10InputBlob(db, transaction, null, 1);

        SQLException exception = assertThrows(SQLException.class, () -> blob.getSegment(-1));
        assertThat(exception, message(startsWith("getSegment called with sizeRequested -1, should be > 0")));
    }

    /**
     * Test if {@link V10InputBlob#getSegment(int)} on closed blob throws exception.
     */
    @Test
    void testGetSegment_blobClosed() {
        V10InputBlob blob = new V10InputBlob(db, transaction, null, 1);

        when(db.isAttached()).thenReturn(true);
        when(transaction.getState()).thenReturn(TransactionState.ACTIVE);

        SQLException exception = assertThrows(SQLNonTransientException.class, () -> blob.getSegment(1));
        assertThat(exception, allOf(
                errorCodeEquals(ISCConstants.isc_bad_segstr_handle),
                fbMessageStartsWith(ISCConstants.isc_bad_segstr_handle)));
    }

    @Test
    void testIsEof_newBlob() {
        V10InputBlob blob = new V10InputBlob(db, transaction, null, 1);

        assertTrue(blob.isEof(), "Expected new input blob to be EOF");
    }
}
