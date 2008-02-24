/*
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

package org.firebirdsql.gds.impl.wire;

import java.util.Arrays;

import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;
import org.firebirdsql.gds.*;
import org.firebirdsql.gds.impl.GDSFactory;
import org.firebirdsql.gds.impl.wire.isc_blob_handle_impl;
import org.firebirdsql.gds.impl.wire.isc_db_handle_impl;
import org.firebirdsql.gds.impl.wire.isc_stmt_handle_impl;
import org.firebirdsql.jca.FBTpb;
import org.firebirdsql.jdbc.FBTpbMapper;
import org.firebirdsql.common.SimpleFBTestBase;


/**
 * @author <a href="mailto:alberola@users.sourceforge.net">Alejandro Alberola</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 */
public class TestGds extends SimpleFBTestBase {

   private Logger log = LoggerFactory.getLogger(getClass(),true);


    static final String dbName = "testdb.gdb";
    static final String dbName2 = "testdb2.gdb";

    private GDS gds;
    private IscDbHandle db1;
    private IscDbHandle db2;
//    private byte[][] status = new Object[20];
    private IscTrHandle t1;
    private byte[] dpb = new byte[256];
        {dpb[0] = (byte) ISCConstants.isc_dpb_version1;
        dpb[1] = (byte) ISCConstants.isc_dpb_num_buffers;
        dpb[2] = (byte) 1;
        dpb[3] = (byte) 90;

        dpb[4] = (byte) ISCConstants.isc_dpb_dummy_packet_interval;
        dpb[5] = (byte) 4;
        dpb[6] = (byte) 120;
        dpb[7] = (byte) 10;
        dpb[8] = (byte) 0;
        dpb[9] = (byte) 0;
        dpb[10] = (byte) 0;
        dpb[11] = (byte) 0;
        dpb[12] = (byte) 0;
        dpb[13] = (byte) 0;
        dpb[14] = (byte) 3;
        dpb[15] = (byte) 0;
        dpb[16] = (byte) 0;
        dpb[17] = (byte) 0;
        }
   
    private short dpb_length = (short)dpb.length;

    private DatabaseParameterBuffer c;

    private FBTpb tpb;

    public TestGds(String name) {
        super(name);
    }


    protected void setUp() {
        if (!System.getProperty("test.gds_type").equals("PURE_JAVA") &&
                !System.getProperty("test.gds_type").equals("TYPE4"))
            fail("This test cannot be run for JNI driver");
        
       //super.setUp(); we will create our own db's directly
        gds = GDSFactory.getDefaultGDS();
        tpb  = new FBTpb(FBTpbMapper.getDefaultMapper(gds).getDefaultMapping());

        c = gds.createDatabaseParameterBuffer();

        c.addArgument(ISCConstants.isc_dpb_num_buffers, new byte[] {90});
        c.addArgument(ISCConstants.isc_dpb_dummy_packet_interval, new byte[] {120, 10, 0, 0});
        c.addArgument(ISCConstants.isc_dpb_sql_dialect, new byte[] {3, 0, 0, 0});
        c.addArgument(ISCConstants.isc_dpb_user_name, DB_USER);
        c.addArgument(ISCConstants.isc_dpb_password, DB_PASSWORD);

        tpb.getTransactionParameterBuffer().addArgument(ISCConstants.isc_tpb_write);
        tpb.getTransactionParameterBuffer().addArgument(ISCConstants.isc_tpb_read_committed);
        tpb.getTransactionParameterBuffer().addArgument(ISCConstants.isc_tpb_no_rec_version);
        tpb.getTransactionParameterBuffer().addArgument(ISCConstants.isc_tpb_wait);
    }

   protected void tearDown() {}//hide superclass teardown.
   

    protected IscDbHandle createDatabase(String name) throws Exception {

        IscDbHandle db = gds.createIscDbHandle();

        if (log!=null) log.info("test- isc_create_database");
        gds.iscCreateDatabase(getdbpath(name), db, c);
        return db;
    }

    private void dropDatabase(IscDbHandle db) throws Exception  {
        if (log!=null) log.info("test- isc_drop_database");
        gds.iscDropDatabase(db);
    }
   
    private IscTrHandle startTransaction(IscDbHandle db) throws Exception {
        IscTrHandle tr = gds.createIscTrHandle();

        if (log!=null) log.info("test- isc_start_transaction");
        gds.iscStartTransaction(tr, db, tpb.getTransactionParameterBuffer());
        return tr;
    }

    private void commit(IscTrHandle tr) throws Exception {
        if (log!=null) log.info("test- isc_commit_transaction");
        try {
           gds.iscCommitTransaction(tr);
        }
        catch (Exception e) {
           if (log!=null) log.info("exception in commit", e);
            throw e;
        }
    }

    private void doSQLImmed(IscDbHandle db, IscTrHandle tr, String sql) throws Exception {
        if (log!=null) log.info("test- isc_dsql_exec_immed2");
        gds.iscDsqlExecImmed2(db, tr, sql,
                                 ISCConstants.SQL_DIALECT_CURRENT, null, null);

    }
      /*
    public void testClumplets() throws Exception {
        if (log!=null) log.info("test- testClumplets");
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        XdrOutputStream x = new XdrOutputStream(b);
        c.write(x);
        byte[] ba = b.toByteArray();
        if (ba.length + 1 != dpb_length) {
            if (log!=null) log.info("test- different length: clumplets " + ba.length  + " dpb_length: " + dpb_length);
        }
//        assert(ba.length + 1 != dpb_length);
        for (int i = 0; i < ba.length;  i++) {
            if (log!=null) log.info("test- clumplet: " + ba[i] + " dpb: " + dpb[i + 1]);
//            assert(ba[i] == dpb[i + 1]);
        }
    }

    public void testClumplets2() throws Exception {

        if (log!=null) log.info("test- testClumplets2");
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        XdrOutputStream x = new XdrOutputStream(b);
        x.writeTyped(ISCConstants.isc_dpb_version1, (Xdrable)c);
        byte[] bac = b.toByteArray();
        b.reset();
        x.writeBuffer(dpb);
        byte[] bap = b.toByteArray();


        if (bac.length != bap.length) {
            if (log!=null) log.info("test- different length: clumplets " + bac.length  + " dpb_length: " + bap.length);
        }
//        assert(bac.length != bap.length);
        for (int i = 0; i < bac.length;  i++) {
            if (log!=null) log.info("test- clumplet: " + bac[i] + " dpb: " + bap[i]);
//            assert(bac[i] == bap[i]);
        }
    }      */



    public void testCreateDropDB() throws Exception {
        if (log!=null) log.info("test- testCreateDropDB");
        db1 = createDatabase(dbName);
        if (log!=null) log.info("test- isc_detach_database");
        gds.iscDetachDatabase(db1);
        
        db1 = gds.createIscDbHandle();

        if (log!=null) log.info("test- isc_attach_database");
        gds.iscAttachDatabase(getdbpath(dbName), db1, c);
        dropDatabase(db1);
    }



   public void testCreateDropD3DB() throws Exception
   {
      GDS gds = GDSFactory.getDefaultGDS();

      final DatabaseParameterBuffer databaseParameterBuffer = gds.createDatabaseParameterBuffer();

      databaseParameterBuffer.addArgument(ISCConstants.isc_dpb_num_buffers, new byte[] {90});
      databaseParameterBuffer.addArgument(ISCConstants.isc_dpb_dummy_packet_interval, new byte[] {120, 10, 0, 0});
      databaseParameterBuffer.addArgument(ISCConstants.isc_dpb_user_name, DB_USER);
      databaseParameterBuffer.addArgument(ISCConstants.isc_dpb_password, DB_PASSWORD);
      databaseParameterBuffer.addArgument(ISCConstants.isc_dpb_overwrite, 0);
      databaseParameterBuffer.addArgument(ISCConstants.isc_dpb_sql_dialect, new byte[] {3, 0, 0, 0});

      IscDbHandle db = gds.createIscDbHandle();

      gds.iscCreateDatabase(getdbpath(dbName2), db, databaseParameterBuffer);
      gds.iscDetachDatabase(db);
      
      db = gds.createIscDbHandle();

      if (log!=null) log.info("test- isc_attach_database");
      gds.iscAttachDatabase(getdbpath(dbName2), db, databaseParameterBuffer);
      dropDatabase(db);

   }


    public void testDbHandleEquality() throws Exception {
        if (log!=null) log.info("test- testDbHandleEquality");
        db1 = createDatabase(dbName);

        db2 = gds.createIscDbHandle();
        gds.iscAttachDatabase(getdbpath(dbName), db2, c);

        if (log!=null) log.info("test- rdb_id1: " + ((isc_db_handle_impl)db1).getRdb_id());
        if (log!=null) log.info("test- rdb_id2: " + ((isc_db_handle_impl)db2).getRdb_id());
        if (log!=null) log.info("test- isc_detach_database");
        gds.iscDetachDatabase(db1);
        gds.iscDetachDatabase(db2);
        
        db1 = gds.createIscDbHandle();

        if (log!=null) log.info("test- isc_attach_database");
        gds.iscAttachDatabase(getdbpath(dbName), db1, c);
        dropDatabase(db1);
    }


    public void testDbHandleEquality2() throws Exception {
        if (log!=null) log.info("test- testDbHandleEquality2");
        db1 = createDatabase(dbName);

        db2 = createDatabase(dbName2);

        if (log!=null) log.info("test- rdb_id1: " + ((isc_db_handle_impl)db1).getRdb_id());
        if (log!=null) log.info("test- rdb_id2: " + ((isc_db_handle_impl)db2).getRdb_id());
        t1 = startTransaction(db1);
        doSQLImmed(db1, t1, "create table r1 (col1 smallint not null primary key)");
        commit(t1);
        t1 = startTransaction(db2);
        doSQLImmed(db2, t1, "create table r1 (col1 smallint not null primary key)");
        commit(t1);

        dropDatabase(db1);
        dropDatabase(db2);
    }

/*Tests whether a transaction started on one db handle can be moved to another.  No, it can't
    public void XXtestTrHandlePortability() throws Exception {
        if (log!=null) log.info();
        if (log!=null) log.info("test- testTrHandlePortability");
        db1 = createDatabase(dbName);

        db2 = gds.get_new_isc_db_handle();

        gds.isc_attach_database(dbName, db2, c);

        if (log!=null) log.info("test- rdb_id1: " + ((isc_db_handle_impl)db1).getRdb_id());
        if (log!=null) log.info("test- rdb_id2: " + ((isc_db_handle_impl)db2).getRdb_id());
        t1 = startTransaction(db1);
        doSQLImmed(db1, t1, "create table r1 (col1 smallint not null primary key)");
        ((isc_tr_handle)t1).rtr_rdb = (isc_db_handle_impl)db2;
        commit(t1); //on db2 connection
        t1 = startTransaction(db2);
        doSQLImmed(db2, t1, "create table r2 (col1 smallint not null primary key)");
        commit(t1);

        dropDatabase(db1);
//        dropDatabase(db2);
    }*/


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

    public void testCreateDropTable() throws Exception {
        if (log!=null) log.info("test- testCreateDropTable");
        db1 = setupTable();
        teardownTable(db1);
    }

    public void testInsert() throws Exception {
        if (log!=null) log.info("test- testInsert");
        db1 = setupTable();
        t1 = startTransaction(db1);
        doSQLImmed(db1, t1, "INSERT INTO R1 VALUES (1, 2)");

        doSQLImmed(db1, t1, "INSERT INTO R1 VALUES (2, 3)");

        doSQLImmed(db1, t1, "INSERT INTO R1 VALUES (3, 4)");
        commit(t1);

        teardownTable(db1);
    }


    public void testParameterizedInsert() throws Exception {
        if (log!=null) log.info("test- testParameterizedInsert");
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
        if (log!=null) log.info("test- isc_dsql_exec_immed2");
        gds.iscDsqlExecImmed2(db1, t1, "INSERT INTO R1 VALUES (?, ?)",
                                 ISCConstants.SQL_DIALECT_CURRENT, xsqlda, null);


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

        if (log!=null) log.info("test- isc_dsql_exec_immed2");
        gds.iscDsqlExecImmed2(db1, t1, "SELECT COL1, COL2 FROM R1 WHERE COL1 = 3",
                                 ISCConstants.SQL_DIALECT_CURRENT, null, xsqlda);

        if (log!=null) log.info("test- retrieved inserted row C1 = " + xsqlda.sqlvar[0].sqldata + "     " +
                           "C2 = " + xsqlda.sqlvar[1].sqldata);



        commit(t1);

        teardownTable(db1);
    }

    public void testPreparedSelect() throws Exception {
        if (log!=null) log.info("test- testPreparedSelect");
        db1 = setupTable();
        t1 = startTransaction(db1);
        doSQLImmed(db1, t1, "INSERT INTO R1 VALUES (1, 2)");

        doSQLImmed(db1, t1, "INSERT INTO R1 VALUES (2, 3)");

        doSQLImmed(db1, t1, "INSERT INTO R1 VALUES (3, 4)");

        XSQLDA in_xsqlda;// = new XSQLDA();
        XSQLDA out_xsqlda;// = new XSQLDA();

        isc_stmt_handle_impl stmt1 = (isc_stmt_handle_impl) gds.createIscStmtHandle();

        if (log!=null) log.info("test- isc_dsql_allocate_statement");
        gds.iscDsqlAllocateStatement(db1, stmt1);


        if (log!=null) log.info("test- isc_dsql_prepare");
        out_xsqlda = gds.iscDsqlPrepare(t1, stmt1, "SELECT COL1, COL2 FROM R1 WHERE COL1 = 1",
                             ISCConstants.SQL_DIALECT_CURRENT);//, out_xsqlda);

//        if (log!=null) log.info("test- isc_dsql_describe_bind");
//        in_xsqlda = gds.isc_dsql_describe_bind(stmt1, 1);//, in_xsqlda);
        in_xsqlda = null;

//        in_xsqlda.sqlvar[0].sqldata = new Short((short) 1);

//        if (log!=null) log.info("test- isc_dsql_describe");
//        out_xsqlda = gds.isc_dsql_describe(stmt1, 1);//, out_xsqlda);

        if (log!=null) log.info("test- isc_dsql_execute2");
        gds.iscDsqlExecute2(t1, stmt1, 1, in_xsqlda, null);

//        if (log!=null) log.info("test- isc_dsql_set_cursor_name");
//        gds.isc_dsql_set_cursor_name(stmt1, "cur1", 0);

//        int fetch_stat;
        String out = "";
        byte[][] row = null;
        gds.iscDsqlFetch(stmt1, 1, out_xsqlda,200);
        Object[] rows = stmt1.getRows();
        int size = stmt1.size();
        for (int rowNum=0; rowNum < size; rowNum++){
           row = (byte[][]) rows[rowNum];
            for (int i = 0; i < out_xsqlda.sqld; i++) {
                Short data =  new Short(new XSQLVAR().decodeShort(row[i]));
                out += data.shortValue() + "    ";
            }
            out += System.getProperty("line.separator");
        }
        if (log!=null) log.info("fetch returned: " + out);
        if (log!=null) log.info("test- isc_dsql_free_statement");
        gds.iscDsqlFreeStatement(stmt1, ISCConstants.DSQL_drop);

        commit(t1);

        teardownTable(db1);
    }

    public void testCreateBlob() throws Exception {
        if (log!=null) log.info("test- testCreateBlob");
        db1 = createDatabase(dbName);
        t1 = startTransaction(db1);
    isc_blob_handle_impl blob = (isc_blob_handle_impl)gds.createIscBlobHandle();
    gds.iscCreateBlob2(db1, t1, blob, null);
    gds.iscCloseBlob(blob);
    commit(t1);
        dropDatabase(db1);
    }

    public void testCreateAndWriteBlob() throws Exception {
      /*        byte[] testbuf = new byte[1024];
    for (int i = 0; i < 1024; i++) {
        testbuf[i] = (byte)i;
        }*/
        byte[] testbuf = new String("xxThis is a test of a blob").getBytes();
    //testbuf[0] = 0;
    //testbuf[1] = 24;
        if (log!=null) log.info("test- test- testCreateAndWriteBlob");
        db1 = setupTable2();
        t1 = startTransaction(db1);
    isc_blob_handle_impl blob1 = (isc_blob_handle_impl)gds.createIscBlobHandle();

    final BlobParameterBuffer blobParameterBuffer = gds.createBlobParameterBuffer();
    blobParameterBuffer.addArgument(ISCConstants.isc_bpb_type, ISCConstants.isc_bpb_type_segmented);

    gds.iscCreateBlob2(db1, t1, blob1, blobParameterBuffer);
    if (log!=null) log.info("test- test- new blob_id: " + blob1.getBlobId());
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

        if (log!=null) log.info("test- isc_dsql_exec_immed2");
        gds.iscDsqlExecImmed2(db1, t1, "INSERT INTO R2 VALUES (?, ?)",
                                 ISCConstants.SQL_DIALECT_CURRENT, xsqlda, null);

        isc_stmt_handle_impl stmt1 = (isc_stmt_handle_impl) gds.createIscStmtHandle();
        if (log!=null) log.info("test- isc_dsql_allocate_statement");
        gds.iscDsqlAllocateStatement(db1, stmt1);
        XSQLDA out_xsqlda = gds.iscDsqlPrepare(t1, stmt1, "SELECT COL1, COL2 FROM R2",
                             ISCConstants.SQL_DIALECT_CURRENT);


        if (log!=null) log.info("test- isc_dsql_execute2");
        gds.iscDsqlExecute2(t1, stmt1, 1, null, null);

        isc_blob_handle_impl blob2 = (isc_blob_handle_impl)gds.createIscBlobHandle();
        byte[][] row = null;
        gds.iscDsqlFetch(stmt1, 1, out_xsqlda, 200);
        Object[] rows = stmt1.getRows();
        int size = stmt1.size();
        for (int rowNum=0; rowNum < size; rowNum++){
           row = (byte[][]) rows[rowNum];
           String out = "";
            for (int i = 0; i < out_xsqlda.sqld; i++) {
                Object data =  row[i];
                out += "column: " + i + ", value: " + data;
            }
        
            if (log!=null) log.info("fetch returned: " + out);

            blob2.setBlobId(xsqlvar.decodeLong(row[1]));
            //blob2.rbl_buffer_length = 30;//1024;
            gds.iscOpenBlob2(db1, t1, blob2, null);
            byte[] answer = gds.iscGetSegment(blob2, 32);//1026);
            if (log!=null) log.info("test- answer length: " + answer.length + ", answer string: " + new String(answer));
            gds.iscCloseBlob(blob2);
        }

        if (log!=null) log.info("test- isc_dsql_free_statement");
        gds.iscDsqlFreeStatement(stmt1, ISCConstants.DSQL_drop);


    commit(t1);
    teardownTable2(db1);
    //        dropDatabase(db1);
    }
    public void testCreateAndWriteBlobStream() throws Exception {
        byte[] a = new String("a").getBytes();
        byte[] testbuf = new byte[500];
    for (int i = 0; i < 500; i++) {
        testbuf[i] = a[0];//(byte)i;
        }
        if (log!=null) log.info("test- testCreateAndWriteBlobStream");
        db1 = setupTable2();
        t1 = startTransaction(db1);
    isc_blob_handle_impl blob1 = (isc_blob_handle_impl)gds.createIscBlobHandle();

    final BlobParameterBuffer blobParameterBuffer = gds.createBlobParameterBuffer();
    blobParameterBuffer.addArgument(ISCConstants.isc_bpb_type, ISCConstants.isc_bpb_type_segmented);

    //Clumplet bpb = GDSFactory.newClumplet(ISCConstants.isc_bpb_type, ISCConstants.isc_bpb_type_stream);
    gds.iscCreateBlob2(db1, t1, blob1, blobParameterBuffer);
    if (log!=null) log.info("test- new blob_id: " + blob1.getBlobId());
    for (int i = 0; i< 10; i++) {
        gds.iscPutSegment(blob1, testbuf);
            if (log!=null) log.info("test- wrote bytes: " + (i * testbuf.length));
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

        if (log!=null) log.info("test- isc_dsql_exec_immed2");
        gds.iscDsqlExecImmed2(db1, t1, "INSERT INTO R2 VALUES (?, ?)",
                                 ISCConstants.SQL_DIALECT_CURRENT, xsqlda, null);

        isc_stmt_handle_impl stmt1 = (isc_stmt_handle_impl) gds.createIscStmtHandle();
        if (log!=null) log.info("test- isc_dsql_allocate_statement");
        gds.iscDsqlAllocateStatement(db1, stmt1);
        XSQLDA out_xsqlda = gds.iscDsqlPrepare(t1, stmt1, "SELECT COL1, COL2 FROM R2",
                             ISCConstants.SQL_DIALECT_CURRENT);


        if (log!=null) log.info("test- isc_dsql_execute2");
        gds.iscDsqlExecute2(t1, stmt1, 1, null, null);

        isc_blob_handle_impl blob2 = (isc_blob_handle_impl)gds.createIscBlobHandle();
        byte[][] row = null;
        gds.iscDsqlFetch(stmt1, 1, out_xsqlda, 200);
        Object[] rows = stmt1.getRows();
        int size = stmt1.size();
        for (int rowNum=0; rowNum < size; rowNum++){
           row = (byte[][]) rows[rowNum];
           String out = "";
            for (int i = 0; i < out_xsqlda.sqld; i++) {
                Object data =  row[i];
                out += "column: " + i + ", value: " + data;
            }
            if (log!=null) log.info(out);
            blob2.setBlobId(xsqlvar.decodeLong(row[1]));
            //blob2.rbl_buffer_length = 1050;//1024;
            gds.iscOpenBlob2(db1, t1, blob2, blobParameterBuffer);
            int readcount = 0;
            do {
                byte[] answer = gds.iscGetSegment(blob2, 1050);;
                if (log!=null) log.info("test- answer length: " + answer.length + ", answer string: " + new String(answer));
                readcount += answer.length;
                if (log!=null) log.info("test- read bytes: " + readcount);
                //blob2.rbl_buffer_length = 1050;//1024;
            } while (!blob2.isEof());
            gds.iscCloseBlob(blob2);
        }

        if (log!=null) log.info("test- isc_dsql_free_statement");
        gds.iscDsqlFreeStatement(stmt1, ISCConstants.DSQL_drop);


    commit(t1);
    teardownTable2(db1);
    //        dropDatabase(db1);
    }

    public void testCreateAndWriteBlobStreamInSegmentedPieces() throws Exception {
        byte[] a = new String("a").getBytes();
        byte[] testbuf = new byte[64];//33];//1024];
    for (int i = 0; i < 64; i++) {
        testbuf[i] = a[0];//(byte)i;
        }
        if (log!=null) log.info("test- testCreateAndWriteBlobStreamInSegmentedPieces");
        db1 = setupTable2();
        t1 = startTransaction(db1);
    isc_blob_handle_impl blob1 = (isc_blob_handle_impl)gds.createIscBlobHandle();

    //Clumplet bpb = GDSFactory.newClumplet(ISCConstants.isc_bpb_type, ISCConstants.isc_bpb_type_stream);
    final BlobParameterBuffer blobParameterBuffer = gds.createBlobParameterBuffer();
    blobParameterBuffer.addArgument(ISCConstants.isc_bpb_type, ISCConstants.isc_bpb_type_segmented);

    gds.iscCreateBlob2(db1, t1, blob1, blobParameterBuffer);
    if (log!=null) log.info("test- new blob_id: " + blob1.getBlobId());
    for (int i = 0; i< 10; i++) {
        gds.iscPutSegment(blob1, testbuf);
            if (log!=null) log.info("test- wrote bytes: " + (i * testbuf.length));
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

        if (log!=null) log.info("test- isc_dsql_exec_immed2");
        gds.iscDsqlExecImmed2(db1, t1, "INSERT INTO R2 VALUES (?, ?)",
                                 ISCConstants.SQL_DIALECT_CURRENT, xsqlda, null);

        isc_stmt_handle_impl stmt1 = (isc_stmt_handle_impl) gds.createIscStmtHandle();
        if (log!=null) log.info("test- isc_dsql_allocate_statement");
        gds.iscDsqlAllocateStatement(db1, stmt1);
        XSQLDA out_xsqlda = gds.iscDsqlPrepare(t1, stmt1, "SELECT COL1, COL2 FROM R2",
                             ISCConstants.SQL_DIALECT_CURRENT);


        if (log!=null) log.info("test- isc_dsql_execute2");
        gds.iscDsqlExecute2(t1, stmt1, 1, null, null);

    isc_blob_handle_impl blob2 = (isc_blob_handle_impl)gds.createIscBlobHandle();
        byte[][] row = null;
        gds.iscDsqlFetch(stmt1, 1, out_xsqlda, 200);
        Object[] rows = stmt1.getRows();
        int size = stmt1.size();
        for (int rowNum=0; rowNum < size; rowNum++){
           row = (byte[][]) rows[rowNum];
           String out = "";
            for (int i = 0; i < out_xsqlda.sqld; i++) {
                Object data =  row[i];
                out += "column: " + i + ", value: " + data;
            }
            if (log!=null) log.info(out);
            blob2.setBlobId(xsqlvar.decodeLong(row[1]));
            // blob2.rbl_buffer_length = 10;//1024;
            gds.iscOpenBlob2(db1, t1, blob2, blobParameterBuffer);
            int readcount = 0;
            do {
               byte[] answer = gds.iscGetSegment(blob2, 10);// 1050)
               if (log!=null) log.info("test- answer length: " + answer.length + ", answer string: " + new String(answer));
               readcount += answer.length;
               if (log!=null) log.info("test- read bytes: " + readcount);

            } while (!blob2.isEof());
            gds.iscCloseBlob(blob2);
        }

        if (log!=null) log.info("test- isc_dsql_free_statement");
        gds.iscDsqlFreeStatement(stmt1, ISCConstants.DSQL_drop);


    commit(t1);
    teardownTable2(db1);
    //        dropDatabase(db1);
    }

    public void testCreateAndWriteBlobStreamInStreamPieces() throws Exception {
        byte[] a = new String("a").getBytes();
        byte[] testbuf = new byte[4096];//2030];
        int reps = 10;//10000;
        Arrays.fill(testbuf, a[0]);
        if (log!=null) log.info("test- testCreateAndWriteBlobInStreamPieces");
        db1 = setupTable2();
        t1 = startTransaction(db1);
    isc_blob_handle_impl blob1 = (isc_blob_handle_impl)gds.createIscBlobHandle();

    final BlobParameterBuffer blobParameterBuffer = gds.createBlobParameterBuffer();
    blobParameterBuffer.addArgument(ISCConstants.isc_bpb_type, ISCConstants.isc_bpb_type_stream);
    //Clumplet bpb = GDSFactory.newClumplet(ISCConstants.isc_bpb_type, ISCConstants.isc_bpb_type_segmented);
    gds.iscCreateBlob2(db1, t1, blob1, blobParameterBuffer);
    if (log!=null) log.info("test- new blob_id: " + blob1.getBlobId());
    for (int i = 0; i< reps; i++) {
        gds.iscPutSegment(blob1, testbuf);
    }
        if (log!=null) log.info("test- wrote bytes: " + (100 * testbuf.length));
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

        if (log!=null) log.info("test- isc_dsql_exec_immed2");
        gds.iscDsqlExecImmed2(db1, t1, "INSERT INTO R2 VALUES (?, ?)",
                                 ISCConstants.SQL_DIALECT_CURRENT, xsqlda, null);

        isc_stmt_handle_impl stmt1 = (isc_stmt_handle_impl) gds.createIscStmtHandle();
        if (log!=null) log.info("test- isc_dsql_allocate_statement");
        gds.iscDsqlAllocateStatement(db1, stmt1);
        XSQLDA out_xsqlda = gds.iscDsqlPrepare(t1, stmt1, "SELECT COL1, COL2 FROM R2",
                             ISCConstants.SQL_DIALECT_CURRENT);


        if (log!=null) log.info("test- isc_dsql_execute2");
        gds.iscDsqlExecute2(t1, stmt1, 1, null, null);

        isc_blob_handle_impl blob2 = (isc_blob_handle_impl)gds.createIscBlobHandle();
        byte[][] row = null;
        gds.iscDsqlFetch(stmt1, 1, out_xsqlda, 200);
        Object[] rows = stmt1.getRows();
        int size = stmt1.size();
        for (int rowNum=0; rowNum < size; rowNum++){
           row = (byte[][]) rows[rowNum];
           String out = "";
            for (int i = 0; i < out_xsqlda.sqld; i++) {
                Object data =  row[i];
                out += "column: " + i + ", value: " + data;
            }
            if (log!=null) log.info(out);
            blob2.setBlobId(xsqlvar.decodeLong(row[1]));
            // blob2.rbl_buffer_length = 10;//1024;
            gds.iscOpenBlob2(db1, t1, blob2, blobParameterBuffer);
            int readcount = 0;
            do {
                byte[] answer = gds.iscGetSegment(blob2, 1052);// 1050)
                readcount += answer.length;

            } while (!blob2.isEof());
            if (log!=null) log.info("test- read bytes: " + readcount);
            if (readcount != (reps * testbuf.length)) {
                throw new Exception("Retrieved wrong size");
            }
        gds.iscCloseBlob(blob2);
        }

        if (log!=null) log.info("test- isc_dsql_free_statement");
        gds.iscDsqlFreeStatement(stmt1, ISCConstants.DSQL_drop);


    commit(t1);
    teardownTable2(db1);
    //        dropDatabase(db1);
    }

   /*this is redundant with other blob tests
    public void testReadBlob() throws Exception {
        if (log!=null) log.info();
        if (log!=null) log.info("test- testReadBlob");
        db1 = gds.get_new_isc_db_handle();

        if (log!=null) log.info("test- isc_attach_database");
        gds.isc_attach_database("localhost:/opt/interbase/examples/v5/employee.gdb", db1, c);
    //        db1 = createDatabase("localhost:/opt/interbase/examples/v5/employee.gdb");
        t1 = startTransaction(db1);
    isc_blob_handle_impl blob1 = (isc_blob_handle_impl)gds.get_new_isc_blob_handle();

        isc_stmt_handle_impl stmt1 = gds.get_new_isc_stmt_handle();

        if (log!=null) log.info("test- isc_dsql_allocate_statement");
        gds.isc_dsql_allocate_statement(db1, stmt1);


        if (log!=null) log.info("test- isc_dsql_prepare");
        XSQLDA out_xsqlda = gds.isc_dsql_prepare(t1, stmt1, "SELECT C1, C2 FROM t2 WHERE C1 = 1",
                             ISCConstants.SQL_DIALECT_CURRENT);

        if (log!=null) log.info("test- isc_dsql_execute2");
        gds.isc_dsql_execute2(t1, stmt1, 1, null, null);

//        if (log!=null) log.info("test- isc_dsql_set_cursor_name");
//        gds.isc_dsql_set_cursor_name(stmt1, "cur1", 0);

//        int fetch_stat;
        while (gds.isc_dsql_fetch(stmt1, 1, out_xsqlda) != null) {
            for (int i = 0; i < out_xsqlda.sqld; i++) {
                Object data =  out_xsqlda.sqlvar[i].sqldata;
                System.out.print(data + "    ");
            }
            if (log!=null) log.info();
        blob1.blob_id = ((Long)out_xsqlda.sqlvar[1].sqldata).longValue();
        }

        if (log!=null) log.info("test- isc_dsql_free_statement");
        gds.isc_dsql_free_statement(stmt1, ISCConstants.DSQL_drop);


    //blob1.rbl_buffer_length = 1024;
    gds.isc_open_blob2(db1, t1, blob1, null);
    byte[] answer = gds.isc_get_segment(blob1, 1026);
    if (log!=null) log.info("test- answer length: " + answer.length + " " + new String(answer));
    gds.isc_close_blob(blob1);
    commit(t1);
    //  teardownTable2(db1);
    //        dropDatabase(db1);
    }
   */
}

