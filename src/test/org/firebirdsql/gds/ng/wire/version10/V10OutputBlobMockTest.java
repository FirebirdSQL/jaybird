// SPDX-FileCopyrightText: Copyright 2014-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.version10;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.impl.GDSServerVersion;
import org.firebirdsql.gds.ng.FbBlob;
import org.firebirdsql.gds.ng.LockCloseable;
import org.firebirdsql.gds.ng.wire.FbWireDatabase;
import org.firebirdsql.gds.ng.wire.FbWireTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.sql.SQLNonTransientException;

import static org.firebirdsql.common.matchers.SQLExceptionMatchers.errorCodeEquals;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.fbMessageStartsWith;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;

/**
 * Tests for {@link V10OutputBlob} that don't require a connection to the database.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
@ExtendWith(MockitoExtension.class)
class V10OutputBlobMockTest {

    @Mock
    private FbWireDatabase db;
    @Mock
    private FbWireTransaction transaction;

    @BeforeEach
    void setUp() {
        lenient().when(db.withLock()).thenReturn(LockCloseable.NO_OP);
        lenient().when(db.getServerVersion()).thenReturn(GDSServerVersion.INVALID_VERSION);
    }

    /**
     * Test if calling {@link V10OutputBlob#getSegment(int)} throws a {@link SQLNonTransientException} with
     * error {@link ISCConstants#isc_segstr_no_read}.
     */
    @Test
    void testGetSegment() {
        V10OutputBlob blob = new V10OutputBlob(db, transaction, null);

        SQLException exception = assertThrows(SQLNonTransientException.class, () -> blob.getSegment(1));
        assertThat(exception, allOf(
                errorCodeEquals(ISCConstants.isc_segstr_no_read),
                fbMessageStartsWith(ISCConstants.isc_segstr_no_read)));
    }

    /**
     * Test if calling {@link V10OutputBlob#seek(int, FbBlob.SeekMode)} throws a {@link SQLNonTransientException} with
     * error {@link ISCConstants#isc_segstr_no_read}.
     */
    @Test
    void testSeek() {
        V10OutputBlob blob = new V10OutputBlob(db, transaction, null);

        SQLException exception = assertThrows(SQLNonTransientException.class,
                () -> blob.seek(0, FbBlob.SeekMode.ABSOLUTE));
        assertThat(exception, allOf(
                errorCodeEquals(ISCConstants.isc_segstr_no_read),
                fbMessageStartsWith(ISCConstants.isc_segstr_no_read)));
    }

    @Test
    void testNewBlob_eof() {
        V10OutputBlob blob = new V10OutputBlob(db, transaction, null);

        assertTrue(blob.isEof(), "Expected new output blob to be EOF");
    }
}
