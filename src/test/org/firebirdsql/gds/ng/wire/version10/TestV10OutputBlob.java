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

import org.firebirdsql.gds.XSQLVAR;
import org.firebirdsql.gds.ng.FbBlob;
import org.firebirdsql.gds.ng.FbStatement;
import org.firebirdsql.gds.ng.FbTransaction;
import org.firebirdsql.gds.ng.fields.FieldValue;
import org.firebirdsql.gds.ng.fields.RowDescriptor;
import org.firebirdsql.gds.ng.wire.FbWireDatabase;
import org.firebirdsql.gds.ng.wire.SimpleStatementListener;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class TestV10OutputBlob extends BaseTestV10Blob {

    /**
     * Tests storage of a blob (what goes in is what comes out).
     * <p>
     * Test depends on correct working of blob reading (currently using old gds implementation)
     * </p>
     */
    @Test
    public void testBlobStorage() throws Exception {
        final int testId = 1;
        final byte[] baseContent = generateBaseContent();
        // Use sufficiently large value so that multiple segments are used
        final int requiredSize = 4 * Short.MAX_VALUE;
        final byte[] testBytes = generateBlobContent(baseContent, requiredSize);

        final FbWireDatabase db = createDatabaseConnection();
        try {
            final SimpleStatementListener listener = new SimpleStatementListener();
            final FbTransaction transaction = getTransaction(db);
            final FbStatement statement = db.createStatement(transaction);
            statement.addStatementListener(listener);
            statement.allocateStatement();
            final FbBlob blob = db.createBlob(transaction);
            blob.open();
            int bytesWritten = 0;
            while (bytesWritten < testBytes.length) {
                // TODO the interface for writing blobs should be simpler
                byte[] buffer = new byte[Math.min(blob.getMaximumSegmentSize(), testBytes.length - bytesWritten)];
                System.arraycopy(testBytes, bytesWritten, buffer, 0, buffer.length);
                blob.putSegment(buffer);
                bytesWritten += buffer.length;
            }
            blob.close();

            statement.prepare(INSERT_BLOB_TABLE);
            RowDescriptor descriptor = statement.getParameterDescriptor();
            FieldValue param1 = new FieldValue(descriptor.getFieldDescriptor(0), XSQLVAR.intToBytes(testId));
            FieldValue param2 = new FieldValue(descriptor.getFieldDescriptor(1), XSQLVAR.longToBytes(blob.getBlobId()));
            statement.execute(Arrays.asList(param1, param2));
            statement.close();
            transaction.commit();
        } finally {
            db.detach();
        }

        assertTrue("Unexpected blob content", validateBlob(testId, baseContent, requiredSize));
    }

}
