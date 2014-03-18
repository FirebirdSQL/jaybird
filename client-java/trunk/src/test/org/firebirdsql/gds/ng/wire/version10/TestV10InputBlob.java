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

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class TestV10InputBlob extends BaseTestV10Blob {

    /**
     * Tests retrieval of a blob (what goes in is what comes out).
     * <p>
     * Test depends on correct working of blob writing (currently using old gds implementation)
     * </p>
     */
    @Test
    public void testBlobRetrieval() throws Exception {
        final int testId = 1;
        final byte[] baseContent = generateBaseContent();
        // Use sufficiently large value so that multiple segments are used
        final int requiredSize = 4 * Short.MAX_VALUE;
        populateBlob(testId, baseContent, requiredSize);

        // Test new implementation
        final FbWireDatabase db = createDatabaseConnection();
        try {
            final SimpleStatementListener listener = new SimpleStatementListener();
            final FbTransaction transaction = getTransaction(db);
            final FbStatement statement = db.createStatement(transaction);
            statement.addStatementListener(listener);
            statement.allocateStatement();
            statement.prepare(SELECT_BLOB_TABLE);

            RowDescriptor descriptor = statement.getParameterDescriptor();
            FieldValue param1 = new FieldValue(descriptor.getFieldDescriptor(0), XSQLVAR.intToBytes(testId));

            statement.execute(Arrays.asList(param1));

            assertEquals("Expected hasResultSet to be set to true", Boolean.TRUE, listener.hasResultSet());

            statement.fetchRows(1);
            assertEquals("Expected a row", 1, listener.getRows().size());

            List<FieldValue> row = listener.getRows().get(0);
            FieldValue blobIdFieldValue = row.get(0);
            long blobId = new XSQLVAR().decodeLong(blobIdFieldValue.getFieldData());

            final FbBlob blob = db.createBlobForInput(transaction, blobId);
            blob.open();
            ByteArrayOutputStream bos = new ByteArrayOutputStream(requiredSize);
            while (!blob.isEof()) {
                bos.write(blob.getSegment(blob.getMaximumSegmentSize()));
            }
            blob.close();
            transaction.commit();
            statement.close();
            byte[] result = bos.toByteArray();
            assertEquals("Unexpected length read from blob", requiredSize, result.length);
            assertTrue("Unexpected blob content", validateBlobContent(result, baseContent, requiredSize));

        } finally {
            db.detach();
        }
    }
}
