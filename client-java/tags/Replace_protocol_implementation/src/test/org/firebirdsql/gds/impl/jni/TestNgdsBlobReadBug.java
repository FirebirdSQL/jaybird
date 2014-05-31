/*
 * $Id$
 * 
 * Firebird Open Source J2ee connector - jdbc driver
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
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.gds.impl.jni;

import java.util.ArrayList;
import java.util.List;

import org.firebirdsql.common.StringHelper;
import org.firebirdsql.common.rules.TestTypeRule;
import org.firebirdsql.gds.BlobParameterBuffer;
import org.firebirdsql.gds.DatabaseParameterBuffer;
import org.firebirdsql.gds.GDS;
import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.IscBlobHandle;
import org.firebirdsql.gds.IscDbHandle;
import org.firebirdsql.gds.IscTrHandle;
import org.firebirdsql.gds.XSQLDA;
import org.firebirdsql.gds.XSQLVAR;
import org.firebirdsql.gds.impl.AbstractIscStmtHandle;
import org.firebirdsql.gds.impl.GDSFactory;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.gds.impl.wire.AbstractJavaGDSImpl;
import org.firebirdsql.gds.impl.wire.JavaGDSImpl;
import org.firebirdsql.jca.FBTpb;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;
import org.junit.Rule;
import org.junit.Test;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.junit.Assert.assertArrayEquals;

/**
 * Performs test to reproduce bug found in blob reading done by type 2 driver.
 * 
 * Brief Description:
 * 
 * If a blob greater then 65536 bytes in length is written to a database using
 * jaybird in type 4 mode it will not be possible to read it in type 2 mode.
 */
public class TestNgdsBlobReadBug {

    @Rule
    public final TestTypeRule testTypes = TestTypeRule.excludes(
            JavaGDSImpl.PURE_JAVA_TYPE_NAME,
            LocalGDSImpl.LOCAL_TYPE_NAME,
            EmbeddedGDSImpl.EMBEDDED_TYPE_NAME);

    private Logger log = LoggerFactory.getLogger(getClass(), false);

    @Test
    public void testBlobReadBug() throws Exception {
        final byte[] testbuf = createTestData();
        createDatabaseAndWriteBlobAndDetatch(testbuf);
        try {
            byte[] dataReadFromBlob;

            // First lets just ensure that the type 4 GDS can read the blob data it just wrote correctly.
            try {
                dataReadFromBlob = openDatabaseReadBlobAndDetatch(getType4Gds());
            } catch (Throwable th) {
                throw new Exception(
                        "Problem trying to read blob data using type 4 driver - is there a problem with the type 4 or the test itself ?");
            }

            assertArrayEquals(
                    "Bad blob data read using type 4 driver - is there a problem with the type 4 or the test itself ?",
                    testbuf, dataReadFromBlob);

            // Now for the actual test - can we read the blob data correctly with the type 2 GDS.
            dataReadFromBlob = openDatabaseReadBlobAndDetatch(getType2Gds());

            assertArrayEquals("Bad blob data read using type 2 driver.", testbuf, dataReadFromBlob);
        } finally {
            dropDatabase(getType4Gds());
        }
    }

    // The following two methods perform the actual read test on the test database.
    // The supplied GDS implementation is used to perform the read test.

    private byte[] openDatabaseReadBlobAndDetatch(GDS gds) throws Exception {
        DatabaseParameterBuffer databaseParameterBuffer = createDatabaseParameterBuffer(gds);
        IscDbHandle database_handle = gds.createIscDbHandle();
        gds.iscAttachDatabase(getdbpath(DB_NAME), database_handle, databaseParameterBuffer);

        final byte[] data;
        try {
            data = readAndBlobRecord(gds, database_handle);
        } finally {
            gds.iscDetachDatabase(database_handle);
        }

        return data;
    }

    private byte[] readAndBlobRecord(GDS gds, IscDbHandle database_handle) throws Exception {
        int readCount = 0;
        final List<byte[]> results = new ArrayList<byte[]>();
        IscTrHandle transaction_handle = startTransaction(gds, database_handle);

        try {
            final BlobParameterBuffer blobParameterBuffer = gds.createBlobParameterBuffer();
            blobParameterBuffer.addArgument(ISCConstants.isc_bpb_type,
                    ISCConstants.isc_bpb_type_stream);
            AbstractIscStmtHandle statement_handle = (AbstractIscStmtHandle) gds
                    .createIscStmtHandle();

            gds.iscDsqlAllocateStatement(database_handle, statement_handle);
            XSQLDA out_xsqlda = gds.iscDsqlPrepare(transaction_handle, statement_handle,
                    "SELECT COL1, COL2 FROM R2", ISCConstants.SQL_DIALECT_CURRENT);

            gds.iscDsqlExecute2(transaction_handle, statement_handle, 1, null, null);
            IscBlobHandle blob_handle = gds.createIscBlobHandle();
            byte[][] row;
            gds.iscDsqlFetch(statement_handle, 1, out_xsqlda, 200);
            byte[][][] rows = statement_handle.getRows();
            int size = statement_handle.size();

            for (int rowNum = 0; rowNum < size; rowNum++) {
                row = rows[rowNum];

                StringBuilder out = new StringBuilder();

                for (int i = 0; i < out_xsqlda.sqld; i++) {
                    byte[] data = row[i];
                    out.append("column: ").append(i).append(", value: ").append(StringHelper.toHex(data));
                }

                if (log != null) log.info(out);

                blob_handle.setBlobId(out_xsqlda.sqlvar[0].decodeLong(row[1]));
                gds.iscOpenBlob2(database_handle, transaction_handle, blob_handle,
                        blobParameterBuffer);

                do {
                    byte[] answer = gds.iscGetSegment(blob_handle, SEGMENT_SIZE);

                    if (log != null)
                        log.info("test- answer length: " + answer.length + ", answer string: "
                                + new String(answer));

                    readCount += answer.length;
                    results.add(answer);

                    if (log != null)
                    	log.info("test- read bytes: " + readCount);
                } while (!blob_handle.isEof());

                gds.iscCloseBlob(blob_handle);
            }

            gds.iscDsqlFreeStatement(statement_handle, ISCConstants.DSQL_drop);
        } finally {
            commit(gds, transaction_handle);
        }

        int currentWritePosition = 0;
        final byte[] returnValue = new byte[readCount];
        for (final byte[] currentArray : results) {
            System.arraycopy(currentArray, 0, returnValue, currentWritePosition, currentArray.length);
            currentWritePosition += currentArray.length;
        }

        return returnValue;
    }

    // The following three methods create the test database and insert the test
    // data using the type 4 GDS implementation.

    void createDatabaseAndWriteBlobAndDetatch(byte[] testBuffer) throws Exception {
        final GDS gds = getType4Gds();
        IscDbHandle database_handle = createAndSetupDatabase(gds);
        writeBlobRecord(gds, database_handle, testBuffer);
        gds.iscDetachDatabase(database_handle);
    }

    private IscDbHandle createAndSetupDatabase(GDS gds) throws Exception {
        DatabaseParameterBuffer databaseParameterBuffer = createDatabaseParameterBuffer(gds);
        IscDbHandle database_handle = gds.createIscDbHandle();

        gds.iscCreateDatabase(getdbpath(DB_NAME), database_handle, databaseParameterBuffer);
        IscTrHandle transaction_handle = startTransaction(gds, database_handle);
        gds.iscDsqlExecImmed2(database_handle, transaction_handle,
                "create table r2 (col1 smallint not null primary key, col2 blob)",
                ISCConstants.SQL_DIALECT_CURRENT, null, null);
        commit(gds, transaction_handle);

        return database_handle;
    }

    private void writeBlobRecord(GDS gds, IscDbHandle database_handle, byte[] testBuffer)
            throws Exception {
        final BlobParameterBuffer blobParameterBuffer = gds.createBlobParameterBuffer();
        blobParameterBuffer
                .addArgument(ISCConstants.isc_bpb_type, ISCConstants.isc_bpb_type_stream);
        IscBlobHandle blob_handle = gds.createIscBlobHandle();
        IscTrHandle transaction_handle = startTransaction(gds, database_handle);
        gds.iscCreateBlob2(database_handle, transaction_handle, blob_handle, blobParameterBuffer);

        if (log != null)
        	log.info("test- new blob_id: " + blob_handle.getBlobId());

        final byte[][] segments = segmentData(testBuffer);

        for (int i = 0; i < segments.length; i++) {
            gds.iscPutSegment(blob_handle, segments[i]);

            if (log != null)
            	log.info("test- wrote bytes: " + (i * testBuffer.length));
        }

        XSQLDA xsqlda = new org.firebirdsql.gds.XSQLDA(2);

        XSQLVAR xsqlvar = new org.firebirdsql.gds.XSQLVAR();
        xsqlvar.sqldata = xsqlvar.encodeShort((short) 3);
        xsqlvar.sqltype = ISCConstants.SQL_SHORT;
        xsqlvar.sqllen = 2;

        xsqlda.sqlvar[0] = xsqlvar;

        xsqlvar = new org.firebirdsql.gds.XSQLVAR();
        xsqlvar.sqltype = ISCConstants.SQL_BLOB;
        xsqlvar.sqllen = 8;
        xsqlvar.sqldata = xsqlvar.encodeLong(blob_handle.getBlobId());

        xsqlda.sqlvar[1] = xsqlvar;

        AbstractJavaGDSImpl.calculateBLR(xsqlda);
        AbstractJavaGDSImpl.calculateIOLength(xsqlda);
        gds.iscCloseBlob(blob_handle);
        gds.iscDsqlExecImmed2(database_handle, transaction_handle, "INSERT INTO R2 VALUES (?, ?)",
                ISCConstants.SQL_DIALECT_CURRENT, xsqlda, null);
        commit(gds, transaction_handle);
    }

    private void dropDatabase(GDS gds) throws GDSException {
        DatabaseParameterBuffer c = createDatabaseParameterBuffer(gds);
        IscDbHandle db = gds.createIscDbHandle();
        gds.iscAttachDatabase(getdbpath(DB_NAME), db, c);
        gds.iscDropDatabase(db);
    }

    /**
     * basic helper for creating an appropriate DPB for the supplied GDS
     */
    private DatabaseParameterBuffer createDatabaseParameterBuffer(GDS gds) {
        final DatabaseParameterBuffer databaseParameterBuffer = gds.createDatabaseParameterBuffer();
        databaseParameterBuffer.addArgument(ISCConstants.isc_dpb_num_buffers, new byte[] { 90 });
        databaseParameterBuffer.addArgument(ISCConstants.isc_dpb_dummy_packet_interval, new byte[] { 120, 10, 0, 0 });
        databaseParameterBuffer.addArgument(ISCConstants.isc_dpb_sql_dialect, new byte[] { 3, 0, 0, 0 });
        databaseParameterBuffer.addArgument(ISCConstants.isc_dpb_user_name, DB_USER);
        databaseParameterBuffer.addArgument(ISCConstants.isc_dpb_password, DB_PASSWORD);

        return databaseParameterBuffer;
    }

    private GDS getType2Gds() {
        return GDSFactory.getGDSForType(GDSType.getType("NATIVE"));
    }

    private GDS getType4Gds() {
        return GDSFactory.getGDSForType(GDSType.getType("PURE_JAVA"));
    }

    private IscTrHandle startTransaction(GDS gds, IscDbHandle database_handle) throws Exception {
        IscTrHandle tr = gds.createIscTrHandle();

        FBTpb tpb = new FBTpb(gds.newTransactionParameterBuffer());
        tpb.getTransactionParameterBuffer().addArgument(ISCConstants.isc_tpb_write);
        tpb.getTransactionParameterBuffer().addArgument(ISCConstants.isc_tpb_read_committed);
        tpb.getTransactionParameterBuffer().addArgument(ISCConstants.isc_tpb_no_rec_version);
        tpb.getTransactionParameterBuffer().addArgument(ISCConstants.isc_tpb_wait);

        gds.iscStartTransaction(tr, database_handle, tpb.getTransactionParameterBuffer());

        return tr;
    }

    private void commit(GDS gds, IscTrHandle transaction_handle) throws Exception {
        gds.iscCommitTransaction(transaction_handle);
    }

    // constants and methods used for generating and manipulating the test data
    // ----------------------------------------

    private static final int SEGMENT_SIZE = 64;

    private static final int TEST_DATA_SIZE = 640;

    private byte[] createTestData() {
        byte[] dataBuffer = "abcdefghijklmnopqrstuvwxyz".getBytes();
        byte[] returnValue = new byte[TEST_DATA_SIZE];

        int offsetIntoDataBuffer = 0;

        for (int i = 0; i < returnValue.length; i++) {
            returnValue[i] = dataBuffer[offsetIntoDataBuffer];// (byte)i;

            offsetIntoDataBuffer++;
            if (offsetIntoDataBuffer == dataBuffer.length)
                offsetIntoDataBuffer = 0;
        }

        return returnValue;
    }

    private byte[][] segmentData(byte[] data) {
        int segmentCount = data.length / SEGMENT_SIZE;
        if (data.length % SEGMENT_SIZE != 0) segmentCount += 1;
        int offsetInMainBuffer = 0;

        final byte[][] returnValue = new byte[segmentCount][];
        for (int i = 0; i < segmentCount; i++) {
            final byte[] currentSegmentBuffer = new byte[SEGMENT_SIZE];
            System.arraycopy(data, offsetInMainBuffer, currentSegmentBuffer, 0,
                    currentSegmentBuffer.length);
            offsetInMainBuffer += currentSegmentBuffer.length;
            returnValue[i] = currentSegmentBuffer;
        }

        return returnValue;
    }
}
