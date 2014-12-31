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
package org.firebirdsql.gds.impl.wire;

import org.firebirdsql.common.rules.GdsTypeRule;
import org.firebirdsql.gds.*;
import org.firebirdsql.gds.impl.GDSFactory;
import org.firebirdsql.jca.FBTpb;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.Arrays;

import static org.firebirdsql.common.FBTestProperties.*;

/**
 * @author <a href="mailto:alberola@users.sourceforge.net">Alejandro Alberola</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 */
public class TestGds {

    // TODO Test doesn't assert anything

    @ClassRule
    public static final GdsTypeRule gdsTypeRule = GdsTypeRule.supports(JavaGDSImpl.PURE_JAVA_TYPE_NAME);

    private static final Logger log = LoggerFactory.getLogger(TestGds.class);

    static final String dbName = "testdb.gdb";
    static final String dbName2 = "testdb2.gdb";

    private GDS gds;
    private IscDbHandle db1;
    private IscDbHandle db2;
    private IscTrHandle t1;
    private DatabaseParameterBuffer c;
    private FBTpb tpb;

    @Before
    public void setUp() {
        gds = GDSFactory.getDefaultGDS();

        c = gds.createDatabaseParameterBuffer();

        c.addArgument(ISCConstants.isc_dpb_num_buffers, new byte[] { 90 });
        c.addArgument(ISCConstants.isc_dpb_dummy_packet_interval, new byte[] { 120, 10, 0, 0 });
        c.addArgument(ISCConstants.isc_dpb_sql_dialect, new byte[] { 3, 0, 0, 0 });
        c.addArgument(ISCConstants.isc_dpb_user_name, DB_USER);
        c.addArgument(ISCConstants.isc_dpb_password, DB_PASSWORD);

        TransactionParameterBufferImpl tpbImpl = new TransactionParameterBufferImpl();
        tpbImpl.addArgument(ISCConstants.isc_tpb_read_committed);
        tpbImpl.addArgument(ISCConstants.isc_tpb_no_rec_version);
        tpbImpl.addArgument(ISCConstants.isc_tpb_write);
        tpbImpl.addArgument(ISCConstants.isc_tpb_wait);

        tpb = new FBTpb(tpbImpl);
    }

    protected IscDbHandle createDatabase(String name) throws Exception {
        IscDbHandle db = gds.createIscDbHandle();
        gds.iscCreateDatabase(getdbpath(name), db, c);
        return db;
    }

    private void dropDatabase(IscDbHandle db) throws Exception {
        gds.iscDropDatabase(db);
    }

    private IscTrHandle startTransaction(IscDbHandle db) throws Exception {
        IscTrHandle tr = gds.createIscTrHandle();
        gds.iscStartTransaction(tr, db, tpb.getTransactionParameterBuffer());
        return tr;
    }

    private void commit(IscTrHandle tr) throws Exception {
        try {
            gds.iscCommitTransaction(tr);
        } catch (Exception e) {
            log.info("exception in commit", e);
            throw e;
        }
    }

    private void doSQLImmed(IscDbHandle db, IscTrHandle tr, String sql) throws Exception {
        gds.iscDsqlExecImmed2(db, tr, sql, ISCConstants.SQL_DIALECT_CURRENT, null, null);
    }

    @Test
    public void testCreateDropDB() throws Exception {
        db1 = createDatabase(dbName);
        gds.iscDetachDatabase(db1);
        db1 = gds.createIscDbHandle();
        gds.iscAttachDatabase(getdbpath(dbName), db1, c);
        dropDatabase(db1);
    }

    @Test
    public void testCreateDropD3DB() throws Exception {
        final DatabaseParameterBuffer databaseParameterBuffer = gds.createDatabaseParameterBuffer();

        databaseParameterBuffer.addArgument(ISCConstants.isc_dpb_num_buffers, new byte[] { 90 });
        databaseParameterBuffer.addArgument(ISCConstants.isc_dpb_dummy_packet_interval, new byte[] { 120, 10, 0, 0 });
        databaseParameterBuffer.addArgument(ISCConstants.isc_dpb_user_name, DB_USER);
        databaseParameterBuffer.addArgument(ISCConstants.isc_dpb_password, DB_PASSWORD);
        databaseParameterBuffer.addArgument(ISCConstants.isc_dpb_overwrite, 0);
        databaseParameterBuffer.addArgument(ISCConstants.isc_dpb_sql_dialect, new byte[] { 3, 0, 0, 0 });

        IscDbHandle db = gds.createIscDbHandle();

        gds.iscCreateDatabase(getdbpath(dbName2), db, databaseParameterBuffer);
        gds.iscDetachDatabase(db);

        db = gds.createIscDbHandle();

        gds.iscAttachDatabase(getdbpath(dbName2), db, databaseParameterBuffer);
        dropDatabase(db);
    }

    /**
     * Test to check if creating (and subsequently attaching to) a database with the org.firebirdsql.jdbc.pid and
     * org.firebirdsql.jdbc.processName does not fail. NOTE: This does not check if this information is correctly
     * communicated to Firebird.
     *
     * @throws Exception
     */
    @Test
    public void testCreateWithProcessIdandName() throws Exception {
        System.setProperty("org.firebirdsql.jdbc.pid", "9513");
        System.setProperty("org.firebirdsql.jdbc.processName", "TestGds");

        db1 = createDatabase(dbName);
        gds.iscDetachDatabase(db1);

        db1 = gds.createIscDbHandle();
        gds.iscAttachDatabase(getdbpath(dbName), db1, c);
        dropDatabase(db1);
    }

    @Test
    public void testDbHandleEquality() throws Exception {
        db1 = createDatabase(dbName);

        db2 = gds.createIscDbHandle();
        gds.iscAttachDatabase(getdbpath(dbName), db2, c);

        log.info("test- rdb_id1: " + db1.getRdbId());
        log.info("test- rdb_id2: " + db2.getRdbId());

        gds.iscDetachDatabase(db1);
        gds.iscDetachDatabase(db2);

        db1 = gds.createIscDbHandle();

        gds.iscAttachDatabase(getdbpath(dbName), db1, c);
        dropDatabase(db1);
    }

    @Test
    public void testDbHandleEquality2() throws Exception {
        db1 = createDatabase(dbName);
        db2 = createDatabase(dbName2);

        log.info("test- rdb_id1: " + db1.getRdbId());
        log.info("test- rdb_id2: " + db2.getRdbId());

        t1 = startTransaction(db1);
        doSQLImmed(db1, t1, "create table r1 (col1 smallint not null primary key)");
        commit(t1);
        t1 = startTransaction(db2);
        doSQLImmed(db2, t1, "create table r1 (col1 smallint not null primary key)");
        commit(t1);

        dropDatabase(db1);
        dropDatabase(db2);
    }

    private IscDbHandle setupTable() throws Exception {
        IscDbHandle db = createDatabase(dbName);
        t1 = startTransaction(db);
        doSQLImmed(db, t1, "create table r1 (col1 smallint not null primary key, col2 smallint)");
        commit(t1);
        return db;
    }

    private void teardownTable(IscDbHandle db) throws Exception {
        t1 = startTransaction(db);
        doSQLImmed(db, t1, "drop table r1");
        commit(t1);
        dropDatabase(db);
    }

    private IscDbHandle setupTable2() throws Exception {
        IscDbHandle db = createDatabase(dbName);
        t1 = startTransaction(db);
        doSQLImmed(db, t1, "create table r2 (col1 smallint not null primary key, col2 blob)");
        commit(t1);
        return db;
    }

    private void teardownTable2(IscDbHandle db) throws Exception {
        t1 = startTransaction(db);
        doSQLImmed(db, t1, "drop table r2");
        commit(t1);
        dropDatabase(db);
    }

    @Test
    public void testCreateDropTable() throws Exception {
        db1 = setupTable();
        teardownTable(db1);
    }

    @Test
    public void testInsert() throws Exception {
        db1 = setupTable();
        t1 = startTransaction(db1);
        doSQLImmed(db1, t1, "INSERT INTO R1 VALUES (1, 2)");

        doSQLImmed(db1, t1, "INSERT INTO R1 VALUES (2, 3)");

        doSQLImmed(db1, t1, "INSERT INTO R1 VALUES (3, 4)");
        commit(t1);

        teardownTable(db1);
    }

    @Test
    public void testParameterizedInsert() throws Exception {
        db1 = setupTable();

        t1 = startTransaction(db1);
        XSQLDA xsqlda = new XSQLDA(2);
        XSQLVAR xsqlvar = new XSQLVAR();
        xsqlvar.sqltype = ISCConstants.SQL_SHORT;
        xsqlvar.sqllen = 2;
        xsqlvar.sqldata = xsqlvar.encodeShort((short) 3);
        xsqlda.sqlvar[0] = xsqlvar;

        xsqlvar = new XSQLVAR();
        xsqlvar.sqltype = ISCConstants.SQL_SHORT;
        xsqlvar.sqllen = 2;
        xsqlvar.sqldata = xsqlvar.encodeShort((short) 4);
        xsqlda.sqlvar[1] = xsqlvar;
        AbstractJavaGDSImpl.calculateBLR(xsqlda);
        AbstractJavaGDSImpl.calculateIOLength(xsqlda);
        gds.iscDsqlExecImmed2(db1, t1, "INSERT INTO R1 VALUES (?, ?)", ISCConstants.SQL_DIALECT_CURRENT, xsqlda, null);

        xsqlda = new XSQLDA(2);
        xsqlvar = new XSQLVAR();
        xsqlvar.sqltype = ISCConstants.SQL_SHORT;
        xsqlvar.sqllen = 2;
        xsqlvar.sqldata = null;
        xsqlda.sqlvar[0] = xsqlvar;

        xsqlvar = new XSQLVAR();
        xsqlvar.sqltype = ISCConstants.SQL_SHORT;
        xsqlvar.sqllen = 2;
        xsqlvar.sqldata = null;
        xsqlda.sqlvar[1] = xsqlvar;
        AbstractJavaGDSImpl.calculateBLR(xsqlda);
        AbstractJavaGDSImpl.calculateIOLength(xsqlda);

        gds.iscDsqlExecImmed2(db1, t1, "SELECT COL1, COL2 FROM R1 WHERE COL1 = 3", ISCConstants.SQL_DIALECT_CURRENT,
                null, xsqlda);

        log.info("test- retrieved inserted row C1 = "
                + Arrays.toString(xsqlda.sqlvar[0].sqldata) + "     " + "C2 = "
                + Arrays.toString(xsqlda.sqlvar[1].sqldata));

        commit(t1);

        teardownTable(db1);
    }

    @Test
    public void testPreparedSelect() throws Exception {
        db1 = setupTable();
        t1 = startTransaction(db1);
        doSQLImmed(db1, t1, "INSERT INTO R1 VALUES (1, 2)");

        doSQLImmed(db1, t1, "INSERT INTO R1 VALUES (2, 3)");

        doSQLImmed(db1, t1, "INSERT INTO R1 VALUES (3, 4)");

        isc_stmt_handle_impl stmt1 = (isc_stmt_handle_impl) gds.createIscStmtHandle();

        gds.iscDsqlAllocateStatement(db1, stmt1);

        XSQLDA out_xsqlda = gds.iscDsqlPrepare(t1, stmt1, "SELECT COL1, COL2 FROM R1 WHERE COL1 = 1",
                ISCConstants.SQL_DIALECT_CURRENT);

        XSQLDA in_xsqlda = null;

        gds.iscDsqlExecute2(t1, stmt1, 1, in_xsqlda, null);

        // int fetch_stat;
        byte[][] row;
        gds.iscDsqlFetch(stmt1, 1, out_xsqlda, 200);
        Object[] rows = stmt1.getRows();
        int size = stmt1.size();
        StringBuilder out = new StringBuilder();
        for (int rowNum = 0; rowNum < size; rowNum++) {
            row = (byte[][]) rows[rowNum];
            for (int i = 0; i < out_xsqlda.sqld; i++) {
                short data = new XSQLVAR().decodeShort(row[i]);
                out.append(data).append("    ");
            }
            out.append(getProperty("line.separator"));
        }
        log.info("fetch returned: " + out);
        gds.iscDsqlFreeStatement(stmt1, ISCConstants.DSQL_drop);

        commit(t1);

        teardownTable(db1);
    }

    @Test
    public void testCreateBlob() throws Exception {
        db1 = createDatabase(dbName);
        t1 = startTransaction(db1);
        IscBlobHandle blob = gds.createIscBlobHandle();
        gds.iscCreateBlob2(db1, t1, blob, null);
        gds.iscCloseBlob(blob);
        commit(t1);
        dropDatabase(db1);
    }

    @Test
    public void testCreateAndWriteBlob() throws Exception {
        byte[] testbuf = "xxThis is a test of a blob".getBytes();
        db1 = setupTable2();
        t1 = startTransaction(db1);
        IscBlobHandle blob1 = gds.createIscBlobHandle();

        final BlobParameterBuffer blobParameterBuffer = gds.createBlobParameterBuffer();
        blobParameterBuffer.addArgument(ISCConstants.isc_bpb_type, ISCConstants.isc_bpb_type_segmented);

        gds.iscCreateBlob2(db1, t1, blob1, blobParameterBuffer);
        log.info("test- test- new blob_id: " + blob1.getBlobId());
        gds.iscPutSegment(blob1, testbuf);
        XSQLDA xsqlda = new XSQLDA(2);
        XSQLVAR xsqlvar = new XSQLVAR();
        xsqlvar.sqltype = ISCConstants.SQL_SHORT;
        xsqlvar.sqllen = 2;
        xsqlvar.sqldata = xsqlvar.encodeShort((short) 3);
        xsqlda.sqlvar[0] = xsqlvar;

        xsqlvar = new XSQLVAR();
        xsqlvar.sqltype = ISCConstants.SQL_BLOB;
        xsqlvar.sqllen = 8;
        xsqlvar.sqldata = xsqlvar.encodeLong(blob1.getBlobId());
        xsqlda.sqlvar[1] = xsqlvar;
        AbstractJavaGDSImpl.calculateBLR(xsqlda);
        AbstractJavaGDSImpl.calculateIOLength(xsqlda);
        gds.iscCloseBlob(blob1);

        gds.iscDsqlExecImmed2(db1, t1, "INSERT INTO R2 VALUES (?, ?)", ISCConstants.SQL_DIALECT_CURRENT, xsqlda, null);

        isc_stmt_handle_impl stmt1 = (isc_stmt_handle_impl) gds.createIscStmtHandle();
        gds.iscDsqlAllocateStatement(db1, stmt1);
        XSQLDA out_xsqlda = gds.iscDsqlPrepare(t1, stmt1, "SELECT COL1, COL2 FROM R2",
                ISCConstants.SQL_DIALECT_CURRENT);

        gds.iscDsqlExecute2(t1, stmt1, 1, null, null);

        IscBlobHandle blob2 = gds.createIscBlobHandle();
        byte[][] row;
        gds.iscDsqlFetch(stmt1, 1, out_xsqlda, 200);
        Object[] rows = stmt1.getRows();
        int size = stmt1.size();
        for (int rowNum = 0; rowNum < size; rowNum++) {
            row = (byte[][]) rows[rowNum];
            StringBuilder out = new StringBuilder();
            for (int i = 0; i < out_xsqlda.sqld; i++) {
                out.append("column: ").append(i).append(", value: ").append(Arrays.toString(row[i]));
            }

            log.info("fetch returned: " + out);

            blob2.setBlobId(xsqlvar.decodeLong(row[1]));
            gds.iscOpenBlob2(db1, t1, blob2, null);
            byte[] answer = gds.iscGetSegment(blob2, 32);// 1026);
            log.info("test- answer length: " + answer.length
                    + ", answer string: " + new String(answer));
            gds.iscCloseBlob(blob2);
        }

        gds.iscDsqlFreeStatement(stmt1, ISCConstants.DSQL_drop);

        commit(t1);
        teardownTable2(db1);
    }

    @Test
    public void testCreateAndWriteBlobStream() throws Exception {
        byte[] a = "a".getBytes();
        byte[] testbuf = new byte[500];
        for (int i = 0; i < 500; i++) {
            testbuf[i] = a[0];
        }
        db1 = setupTable2();
        t1 = startTransaction(db1);
        IscBlobHandle blob1 = gds.createIscBlobHandle();

        final BlobParameterBuffer blobParameterBuffer = gds.createBlobParameterBuffer();
        blobParameterBuffer.addArgument(ISCConstants.isc_bpb_type, ISCConstants.isc_bpb_type_segmented);

        gds.iscCreateBlob2(db1, t1, blob1, blobParameterBuffer);
        log.info("test- new blob_id: " + blob1.getBlobId());
        for (int i = 0; i < 10; i++) {
            gds.iscPutSegment(blob1, testbuf);
            log.info("test- wrote bytes: " + (i * testbuf.length));
        }
        XSQLDA xsqlda = new XSQLDA(2);
        XSQLVAR xsqlvar = new XSQLVAR();
        xsqlvar.sqltype = ISCConstants.SQL_SHORT;
        xsqlvar.sqllen = 2;
        xsqlvar.sqldata = xsqlvar.encodeShort((short) 3);
        xsqlda.sqlvar[0] = xsqlvar;

        xsqlvar = new XSQLVAR();
        xsqlvar.sqltype = ISCConstants.SQL_BLOB;
        xsqlvar.sqllen = 8;
        xsqlvar.sqldata = xsqlvar.encodeLong(blob1.getBlobId());
        xsqlda.sqlvar[1] = xsqlvar;
        AbstractJavaGDSImpl.calculateBLR(xsqlda);
        AbstractJavaGDSImpl.calculateIOLength(xsqlda);
        gds.iscCloseBlob(blob1);

        gds.iscDsqlExecImmed2(db1, t1, "INSERT INTO R2 VALUES (?, ?)", ISCConstants.SQL_DIALECT_CURRENT, xsqlda, null);

        isc_stmt_handle_impl stmt1 = (isc_stmt_handle_impl) gds.createIscStmtHandle();
        gds.iscDsqlAllocateStatement(db1, stmt1);
        XSQLDA out_xsqlda = gds.iscDsqlPrepare(t1, stmt1, "SELECT COL1, COL2 FROM R2",
                ISCConstants.SQL_DIALECT_CURRENT);

        gds.iscDsqlExecute2(t1, stmt1, 1, null, null);

        IscBlobHandle blob2 = gds.createIscBlobHandle();
        byte[][] row;
        gds.iscDsqlFetch(stmt1, 1, out_xsqlda, 200);
        Object[] rows = stmt1.getRows();
        int size = stmt1.size();
        for (int rowNum = 0; rowNum < size; rowNum++) {
            row = (byte[][]) rows[rowNum];
            StringBuilder out = new StringBuilder();
            for (int i = 0; i < out_xsqlda.sqld; i++) {
                out.append("column: ").append(i).append(", value: ").append(Arrays.toString(row[i]));
            }
            log.info(out);
            blob2.setBlobId(xsqlvar.decodeLong(row[1]));
            gds.iscOpenBlob2(db1, t1, blob2, blobParameterBuffer);
            int readcount = 0;
            do {
                byte[] answer = gds.iscGetSegment(blob2, 1050);
                log.info("test- answer length: " + answer.length
                        + ", answer string: " + new String(answer));
                readcount += answer.length;
                log.info("test- read bytes: " + readcount);
            } while (!blob2.isEof());
            gds.iscCloseBlob(blob2);
        }

        gds.iscDsqlFreeStatement(stmt1, ISCConstants.DSQL_drop);

        commit(t1);
        teardownTable2(db1);
    }

    @Test
    public void testCreateAndWriteBlobStreamInSegmentedPieces() throws Exception {
        byte[] a = "a".getBytes();
        byte[] testbuf = new byte[64];
        for (int i = 0; i < 64; i++) {
            testbuf[i] = a[0];
        }
        db1 = setupTable2();
        t1 = startTransaction(db1);
        IscBlobHandle blob1 = gds.createIscBlobHandle();

        final BlobParameterBuffer blobParameterBuffer = gds.createBlobParameterBuffer();
        blobParameterBuffer.addArgument(ISCConstants.isc_bpb_type, ISCConstants.isc_bpb_type_segmented);

        gds.iscCreateBlob2(db1, t1, blob1, blobParameterBuffer);
        log.info("test- new blob_id: " + blob1.getBlobId());
        for (int i = 0; i < 10; i++) {
            gds.iscPutSegment(blob1, testbuf);
            log.info("test- wrote bytes: " + (i * testbuf.length));
        }
        XSQLDA xsqlda = new XSQLDA(2);
        XSQLVAR xsqlvar = new XSQLVAR();
        xsqlvar.sqltype = ISCConstants.SQL_SHORT;
        xsqlvar.sqllen = 2;
        xsqlvar.sqldata = xsqlvar.encodeShort((short) 3);
        xsqlda.sqlvar[0] = xsqlvar;

        xsqlvar = new XSQLVAR();
        xsqlvar.sqltype = ISCConstants.SQL_BLOB;
        xsqlvar.sqllen = 8;
        xsqlvar.sqldata = xsqlvar.encodeLong(blob1.getBlobId());
        xsqlda.sqlvar[1] = xsqlvar;
        AbstractJavaGDSImpl.calculateBLR(xsqlda);
        AbstractJavaGDSImpl.calculateIOLength(xsqlda);
        gds.iscCloseBlob(blob1);

        gds.iscDsqlExecImmed2(db1, t1, "INSERT INTO R2 VALUES (?, ?)", ISCConstants.SQL_DIALECT_CURRENT, xsqlda, null);

        isc_stmt_handle_impl stmt1 = (isc_stmt_handle_impl) gds.createIscStmtHandle();
        gds.iscDsqlAllocateStatement(db1, stmt1);
        XSQLDA out_xsqlda = gds.iscDsqlPrepare(t1, stmt1, "SELECT COL1, COL2 FROM R2",
                ISCConstants.SQL_DIALECT_CURRENT);

        gds.iscDsqlExecute2(t1, stmt1, 1, null, null);

        IscBlobHandle blob2 = gds.createIscBlobHandle();
        byte[][] row;
        gds.iscDsqlFetch(stmt1, 1, out_xsqlda, 200);
        Object[] rows = stmt1.getRows();
        int size = stmt1.size();
        for (int rowNum = 0; rowNum < size; rowNum++) {
            row = (byte[][]) rows[rowNum];
            StringBuilder out = new StringBuilder();
            for (int i = 0; i < out_xsqlda.sqld; i++) {
                out.append("column: ").append(i).append(", value: ").append(Arrays.toString(row[i]));
            }
            log.info(out);
            blob2.setBlobId(xsqlvar.decodeLong(row[1]));
            gds.iscOpenBlob2(db1, t1, blob2, blobParameterBuffer);
            int readcount = 0;
            do {
                byte[] answer = gds.iscGetSegment(blob2, 10);
                log.info("test- answer length: " + answer.length
                        + ", answer string: " + new String(answer));
                readcount += answer.length;
                log.info("test- read bytes: " + readcount);

            } while (!blob2.isEof());
            gds.iscCloseBlob(blob2);
        }

        gds.iscDsqlFreeStatement(stmt1, ISCConstants.DSQL_drop);

        commit(t1);
        teardownTable2(db1);
    }

    @Test
    public void testCreateAndWriteBlobStreamInStreamPieces() throws Exception {
        byte[] a = "a".getBytes();
        byte[] testbuf = new byte[4096];
        int reps = 10;
        Arrays.fill(testbuf, a[0]);
        db1 = setupTable2();
        t1 = startTransaction(db1);
        IscBlobHandle blob1 = gds.createIscBlobHandle();

        final BlobParameterBuffer blobParameterBuffer = gds
                .createBlobParameterBuffer();
        blobParameterBuffer.addArgument(ISCConstants.isc_bpb_type,
                ISCConstants.isc_bpb_type_stream);
        gds.iscCreateBlob2(db1, t1, blob1, blobParameterBuffer);
        log.info("test- new blob_id: " + blob1.getBlobId());
        for (int i = 0; i < reps; i++) {
            gds.iscPutSegment(blob1, testbuf);
        }
        log.info("test- wrote bytes: " + (100 * testbuf.length));
        XSQLDA xsqlda = new XSQLDA(2);
        XSQLVAR xsqlvar = new XSQLVAR();
        xsqlvar.sqltype = ISCConstants.SQL_SHORT;
        xsqlvar.sqllen = 2;
        xsqlvar.sqldata = xsqlvar.encodeShort((short) 3);
        xsqlda.sqlvar[0] = xsqlvar;

        xsqlvar = new XSQLVAR();
        xsqlvar.sqltype = ISCConstants.SQL_BLOB;
        xsqlvar.sqllen = 8;
        xsqlvar.sqldata = xsqlvar.encodeLong(blob1.getBlobId());
        xsqlda.sqlvar[1] = xsqlvar;
        AbstractJavaGDSImpl.calculateBLR(xsqlda);
        AbstractJavaGDSImpl.calculateIOLength(xsqlda);
        gds.iscCloseBlob(blob1);

        gds.iscDsqlExecImmed2(db1, t1, "INSERT INTO R2 VALUES (?, ?)", ISCConstants.SQL_DIALECT_CURRENT, xsqlda, null);

        isc_stmt_handle_impl stmt1 = (isc_stmt_handle_impl) gds.createIscStmtHandle();
        gds.iscDsqlAllocateStatement(db1, stmt1);
        XSQLDA out_xsqlda = gds.iscDsqlPrepare(t1, stmt1, "SELECT COL1, COL2 FROM R2",
                ISCConstants.SQL_DIALECT_CURRENT);

        gds.iscDsqlExecute2(t1, stmt1, 1, null, null);

        IscBlobHandle blob2 = gds.createIscBlobHandle();
        byte[][] row;
        gds.iscDsqlFetch(stmt1, 1, out_xsqlda, 200);
        Object[] rows = stmt1.getRows();
        int size = stmt1.size();
        for (int rowNum = 0; rowNum < size; rowNum++) {
            row = (byte[][]) rows[rowNum];
            StringBuilder out = new StringBuilder();
            for (int i = 0; i < out_xsqlda.sqld; i++) {
                out.append("column: ").append(", value: ").append(Arrays.toString(row[i]));
            }
            log.info(out);
            blob2.setBlobId(xsqlvar.decodeLong(row[1]));
            gds.iscOpenBlob2(db1, t1, blob2, blobParameterBuffer);
            int readcount = 0;
            do {
                byte[] answer = gds.iscGetSegment(blob2, 1052);
                readcount += answer.length;

            } while (!blob2.isEof());
            log.info("test- read bytes: " + readcount);
            if (readcount != (reps * testbuf.length)) {
                throw new Exception("Retrieved wrong size");
            }
            gds.iscCloseBlob(blob2);
        }

        gds.iscDsqlFreeStatement(stmt1, ISCConstants.DSQL_drop);

        commit(t1);
        teardownTable2(db1);
    }
}
