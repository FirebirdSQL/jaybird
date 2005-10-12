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
package org.firebirdsql.ngds;

import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;
import org.firebirdsql.gds.*;



import org.firebirdsql.jca.FBTpb;
import org.firebirdsql.jca.FBTpbMapper;

import org.firebirdsql.common.SimpleFBTestBase;

import java.util.Arrays;

/**
 * Performs basic low level tests of the ngds package.
 */
public class TestNgds extends SimpleFBTestBase {

   private Logger log = LoggerFactory.getLogger(getClass(),false);

    static final String dbName = "testdb.gdb";

    static final String dbName2 = "testdb2.gdb";

    private GDS gds;

    private isc_db_handle db1;

    private isc_db_handle db2;

    private isc_tr_handle t1;

    private DatabaseParameterBuffer c;

    private FBTpb tpb = new FBTpb(FBTpbMapper.DEFAULT_MAPPER);



    public TestNgds(String name) {
        super(name);
    }





    protected void setUp() {
        if (GDSType.EMBEDDED_STR.equals(System.getProperty("test.gds_type")))
            gds = GDSFactory.getGDSForType(GDSType.NATIVE_EMBEDDED);
        else
            gds = GDSFactory.getGDSForType(GDSType.NATIVE);

        c= gds.newDatabaseParameterBuffer();

        c.addArgument(ISCConstants.isc_dpb_num_buffers, new byte[] {90});
        c.addArgument(ISCConstants.isc_dpb_dummy_packet_interval, new byte[] {120, 10, 0, 0});
        c.addArgument(ISCConstants.isc_dpb_sql_dialect, new byte[] {3, 0, 0, 0});
        c.addArgument(ISCConstants.isc_dpb_user_name, DB_USER);
        c.addArgument(ISCConstants.isc_dpb_password, DB_PASSWORD);

        tpb.add(new Integer(ISCConstants.isc_tpb_write));

        tpb.add(new Integer(ISCConstants.isc_tpb_read_committed));

        tpb.add(new Integer(ISCConstants.isc_tpb_no_rec_version));

        tpb.add(new Integer(ISCConstants.isc_tpb_wait));

    }



   protected void tearDown() {}//hide superclass teardown.





    protected isc_db_handle createDatabase(String name) throws Exception {
         isc_db_handle db = gds.get_new_isc_db_handle();

        if (log!=null) log.info("test- isc_create_database");

        gds.isc_create_database(getdbpath(name), db, c);

        return db;
    }



    private void dropDatabase(isc_db_handle db) throws Exception  {
        if (log!=null) log.info("test- isc_drop_database");

        gds.isc_drop_database(db);
    }



    private isc_tr_handle startTransaction(isc_db_handle db) throws Exception {
        isc_tr_handle tr = gds.get_new_isc_tr_handle();

       if (log!=null) log.info("test- isc_start_transaction");

        gds.isc_start_transaction(tr, db, tpb.getArray());

        return tr;
     }



    private void commit(isc_tr_handle tr) throws Exception {
        if (log!=null) log.info("test- isc_commit_transaction");

        try {

           gds.isc_commit_transaction(tr);

        }

        catch (Exception e) {

           if (log!=null) log.info("exception in commit", e);

            throw e;

        }
    }



    private void doSQLImmed(isc_db_handle db, isc_tr_handle tr, String sql) throws Exception {
        if (log!=null) log.info("test- isc_dsql_exec_immed2");

        gds.isc_dsql_exec_immed2(db, tr, sql,
                                  ISCConstants.SQL_DIALECT_CURRENT, null, null);
     }



    public void testCreateDropDB() throws Exception {
        if (log!=null) log.info("test- testCreateDropDB");

        db1 = createDatabase(dbName);

        if (log!=null) log.info("test- isc_detach_database");

        gds.isc_detach_database(db1);

        db1 = gds.get_new_isc_db_handle();

        if (log!=null) log.info("test- isc_attach_database");

        gds.isc_attach_database(getdbpath(dbName), db1, c);

        dropDatabase(db1);
    }







   public void testCreateDropD3DB() throws Exception
   {
       
       GDS gds;

       if (GDSType.EMBEDDED_STR.equals(System.getProperty("test.gds_type")))
           return;
       else
           gds = GDSFactory.getGDSForType(GDSType.NATIVE);

      DatabaseParameterBuffer databaseParameterBuffer = gds.newDatabaseParameterBuffer();

       databaseParameterBuffer.addArgument(ISCConstants.isc_dpb_num_buffers, new byte[] {90});
       databaseParameterBuffer.addArgument(ISCConstants.isc_dpb_dummy_packet_interval, new byte[] {120, 10, 0, 0});
       databaseParameterBuffer.addArgument(ISCConstants.isc_dpb_user_name, DB_USER);
       databaseParameterBuffer.addArgument(ISCConstants.isc_dpb_password, DB_PASSWORD);
       databaseParameterBuffer.addArgument(ISCConstants.isc_dpb_overwrite, 0);
       databaseParameterBuffer.addArgument(ISCConstants.isc_dpb_sql_dialect, new byte[] {3, 0, 0, 0});

     isc_db_handle db = gds.get_new_isc_db_handle();

      gds.isc_create_database(getdbpath(dbName2), db, databaseParameterBuffer);

      gds.isc_detach_database(db);

    db = gds.get_new_isc_db_handle();

      if (log!=null) log.info("test- isc_attach_database");

      gds.isc_attach_database(getdbpath(dbName2), db, databaseParameterBuffer);

      dropDatabase(db);
   }





    public void testDbHandleEquality() throws Exception {
         if (log!=null) log.info("test- testDbHandleEquality");

        db1 = createDatabase(dbName);

        db2 = gds.get_new_isc_db_handle();

        gds.isc_attach_database(getdbpath(dbName), db2, c);

        if (log!=null) log.info("test- rdb_id1: " + ((isc_db_handle_impl)db1).getRdb_id());

        if (log!=null) log.info("test- rdb_id2: " + ((isc_db_handle_impl)db2).getRdb_id());

        if (log!=null) log.info("test- isc_detach_database");

        gds.isc_detach_database(db1);

        gds.isc_detach_database(db2);

        db1 = gds.get_new_isc_db_handle();



        if (log!=null) log.info("test- isc_attach_database");

        gds.isc_attach_database(getdbpath(dbName), db1, c);

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



    private isc_db_handle setupTable() throws Exception {
         isc_db_handle db = createDatabase(dbName);

        t1 = startTransaction(db);

        doSQLImmed(db, t1, "create table r1 (col1 smallint not null primary key, col2 smallint)");

        commit(t1);

        return db;
     }



    private void teardownTable(isc_db_handle db) throws Exception {
        t1 = startTransaction(db);

        doSQLImmed(db, t1, "drop table r1");

        commit(t1);

        dropDatabase(db);
    }



    private isc_db_handle setupTable2() throws Exception {
        isc_db_handle db = createDatabase(dbName);

        t1 = startTransaction(db);

        doSQLImmed(db, t1, "create table r2 (col1 smallint not null primary key, col2 blob)");

        commit(t1);

        return db;
    }



    private void teardownTable2(isc_db_handle db) throws Exception {
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

        XSQLDA xsqlda = new XSQLDAImpl(2);

        XSQLVAR xsqlvar = new XSQLVARLittleEndianImpl();

        xsqlvar.sqltype = ISCConstants.SQL_SHORT;

        xsqlvar.sqllen = 2;

        xsqlvar.sqldata = xsqlvar.encodeShort((short) 3);

        xsqlda.sqlvar[0] = xsqlvar;

        xsqlvar = new XSQLVARLittleEndianImpl();

        xsqlvar.sqltype = ISCConstants.SQL_SHORT;

        xsqlvar.sqllen = 2;

        xsqlvar.sqldata = xsqlvar.encodeShort((short) 4);

        xsqlda.sqlvar[1] = xsqlvar;

        if (log!=null) log.info("test- isc_dsql_exec_immed2");

        gds.isc_dsql_exec_immed2(db1, t1, "INSERT INTO R1 VALUES (?, ?)",
                                 ISCConstants.SQL_DIALECT_CURRENT, xsqlda, null);

        xsqlda = new XSQLDAImpl(2);

        xsqlvar = new XSQLVARLittleEndianImpl();

        xsqlvar.sqltype = ISCConstants.SQL_SHORT;

        xsqlvar.sqllen = 2;

        xsqlvar.sqldata = null;

        xsqlda.sqlvar[0] = xsqlvar;

        xsqlvar = new XSQLVARLittleEndianImpl();

        xsqlvar.sqltype = ISCConstants.SQL_SHORT;

        xsqlvar.sqllen = 2;

        xsqlvar.sqldata = null;

        xsqlda.sqlvar[1] = xsqlvar;

       if (log!=null) log.info("test- isc_dsql_exec_immed2");

        gds.isc_dsql_exec_immed2(db1, t1, "SELECT COL1, COL2 FROM R1 WHERE COL1 = 3",
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

        isc_stmt_handle_impl stmt1 = (isc_stmt_handle_impl) gds.get_new_isc_stmt_handle();

        if (log!=null) log.info("test- isc_dsql_allocate_statement");

        gds.isc_dsql_allocate_statement(db1, stmt1);

        if (log!=null) log.info("test- isc_dsql_prepare");

        out_xsqlda = gds.isc_dsql_prepare(t1, stmt1, "SELECT COL1, COL2 FROM R1 WHERE COL1 = 1",
                             ISCConstants.SQL_DIALECT_CURRENT);//, out_xsqlda);

        in_xsqlda = null;

        if (log!=null) log.info("test- isc_dsql_execute2");

        gds.isc_dsql_execute2(t1, stmt1, 1, in_xsqlda, null);

        String out = "";

        byte[][] row = null;

        gds.isc_dsql_fetch(stmt1, 1, out_xsqlda,200);

        Object[] rows = stmt1.getRows();

        int size = stmt1.size();

        for (int rowNum=0; rowNum < size; rowNum++){

           row = (byte[][]) rows[rowNum];

            for (int i = 0; i < out_xsqlda.sqld; i++) {

                Short data =  new Short(out_xsqlda.sqlvar[0].decodeShort(row[i]));

                out += data.shortValue() + "    ";

            }

            out += System.getProperty("line.separator");

        }

        if (log!=null) log.info("fetch returned: " + out);

        if (log!=null) log.info("test- isc_dsql_free_statement");

        gds.isc_dsql_free_statement(stmt1, ISCConstants.DSQL_drop);

        commit(t1);

        teardownTable(db1);
    }



    public void testCreateBlob() throws Exception {
         if (log!=null) log.info("test- testCreateBlob");

        db1 = createDatabase(dbName);

        t1 = startTransaction(db1);

    isc_blob_handle_impl blob = (isc_blob_handle_impl)gds.get_new_isc_blob_handle();

    gds.isc_create_blob2(db1, t1, blob, null);

    gds.isc_close_blob(blob);

    commit(t1);

        dropDatabase(db1);
    }



    public void testCreateAndWriteBlob() throws Exception {


        byte[] testbuf = new String("xxThis is a test of a blob").getBytes();


        if (log!=null) log.info("test- test- testCreateAndWriteBlob");

        db1 = setupTable2();

        t1 = startTransaction(db1);

    isc_blob_handle_impl blob1 = (isc_blob_handle_impl)gds.get_new_isc_blob_handle();

    final BlobParameterBuffer blobParameterBuffer = gds.newBlobParameterBuffer();

    blobParameterBuffer.addArgument(ISCConstants.isc_bpb_type, ISCConstants.isc_bpb_type_segmented);

    gds.isc_create_blob2(db1, t1, blob1, blobParameterBuffer);

    if (log!=null) log.info("test- test- new blob_id: " + blob1.getBlob_id());

    gds.isc_put_segment(blob1, testbuf);

        XSQLDA xsqlda = new XSQLDAImpl(2);

        XSQLVAR xsqlvar = new XSQLVARLittleEndianImpl();

        xsqlvar.sqltype = ISCConstants.SQL_SHORT;

        xsqlvar.sqllen = 2;

        xsqlvar.sqldata = xsqlvar.encodeShort((short) 3);

        xsqlda.sqlvar[0] = xsqlvar;



        xsqlvar = new XSQLVARLittleEndianImpl();

        xsqlvar.sqltype = ISCConstants.SQL_BLOB;

        xsqlvar.sqllen = 8;

        xsqlvar.sqldata = xsqlvar.encodeLong(blob1.getBlob_id());

        xsqlda.sqlvar[1] = xsqlvar;


    gds.isc_close_blob(blob1);

        if (log!=null) log.info("test- isc_dsql_exec_immed2");

        gds.isc_dsql_exec_immed2(db1, t1, "INSERT INTO R2 VALUES (?, ?)",

                                 ISCConstants.SQL_DIALECT_CURRENT, xsqlda, null);



        isc_stmt_handle_impl stmt1 = (isc_stmt_handle_impl) gds.get_new_isc_stmt_handle();

        if (log!=null) log.info("test- isc_dsql_allocate_statement");

        gds.isc_dsql_allocate_statement(db1, stmt1);

        XSQLDA out_xsqlda = gds.isc_dsql_prepare(t1, stmt1, "SELECT COL1, COL2 FROM R2",
                             ISCConstants.SQL_DIALECT_CURRENT);


        if (log!=null) log.info("test- isc_dsql_execute2");

        gds.isc_dsql_execute2(t1, stmt1, 1, null, null);

        isc_blob_handle_impl blob2 = (isc_blob_handle_impl)gds.get_new_isc_blob_handle();

        byte[][] row = null;

        gds.isc_dsql_fetch(stmt1, 1, out_xsqlda, 200);

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

         blob2.setBlob_id(xsqlvar.decodeLong(row[1]));

            gds.isc_open_blob2(db1, t1, blob2, null);

            byte[] answer = gds.isc_get_segment(blob2, 32);//1026);

            if (log!=null) log.info("test- answer length: " + answer.length + ", answer string: " + new String(answer));

            gds.isc_close_blob(blob2);
        }

        if (log!=null) log.info("test- isc_dsql_free_statement");

        gds.isc_dsql_free_statement(stmt1, ISCConstants.DSQL_drop);

    commit(t1);

    teardownTable2(db1);
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

    isc_blob_handle_impl blob1 = (isc_blob_handle_impl)gds.get_new_isc_blob_handle();

    final BlobParameterBuffer blobParameterBuffer = gds.newBlobParameterBuffer();

    blobParameterBuffer.addArgument(ISCConstants.isc_bpb_type, ISCConstants.isc_bpb_type_segmented);

    gds.isc_create_blob2(db1, t1, blob1, blobParameterBuffer);

    if (log!=null) log.info("test- new blob_id: " + blob1.getBlob_id());

    for (int i = 0; i< 10; i++) {

        gds.isc_put_segment(blob1, testbuf);

            if (log!=null) log.info("test- wrote bytes: " + (i * testbuf.length));

    }

        XSQLDA xsqlda = new XSQLDAImpl(2);

        XSQLVAR xsqlvar = new XSQLVARLittleEndianImpl();

        xsqlvar.sqltype = ISCConstants.SQL_SHORT;

        xsqlvar.sqllen = 2;

        xsqlvar.sqldata = xsqlvar.encodeShort((short) 3);

        xsqlda.sqlvar[0] = xsqlvar;

        xsqlvar = new XSQLVARLittleEndianImpl();

        xsqlvar.sqltype = ISCConstants.SQL_BLOB;

        xsqlvar.sqllen = 8;

        xsqlvar.sqldata = xsqlvar.encodeLong(blob1.getBlob_id());

        xsqlda.sqlvar[1] = xsqlvar;

        gds.isc_close_blob(blob1);

        if (log!=null) log.info("test- isc_dsql_exec_immed2");

        gds.isc_dsql_exec_immed2(db1, t1, "INSERT INTO R2 VALUES (?, ?)",
                                  ISCConstants.SQL_DIALECT_CURRENT, xsqlda, null);

        isc_stmt_handle_impl stmt1 = (isc_stmt_handle_impl) gds.get_new_isc_stmt_handle();

        if (log!=null) log.info("test- isc_dsql_allocate_statement");

        gds.isc_dsql_allocate_statement(db1, stmt1);

        XSQLDA out_xsqlda = gds.isc_dsql_prepare(t1, stmt1, "SELECT COL1, COL2 FROM R2",
                               ISCConstants.SQL_DIALECT_CURRENT);

        if (log!=null) log.info("test- isc_dsql_execute2");

        gds.isc_dsql_execute2(t1, stmt1, 1, null, null);

        isc_blob_handle_impl blob2 = (isc_blob_handle_impl)gds.get_new_isc_blob_handle();

        byte[][] row = null;

        gds.isc_dsql_fetch(stmt1, 1, out_xsqlda, 200);

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

            blob2.setBlob_id(xsqlvar.decodeLong(row[1]));

            gds.isc_open_blob2(db1, t1, blob2, blobParameterBuffer);

            int readcount = 0;

            do {
                byte[] answer = gds.isc_get_segment(blob2, 1050);;

                if (log!=null) log.info("test- answer length: " + answer.length + ", answer string: " + new String(answer));

                readcount += answer.length;

                if (log!=null) log.info("test- read bytes: " + readcount);

            } while (!blob2.isEof());

           gds.isc_close_blob(blob2);
          }

        if (log!=null) log.info("test- isc_dsql_free_statement");

        gds.isc_dsql_free_statement(stmt1, ISCConstants.DSQL_drop);

    commit(t1);

     teardownTable2(db1);
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

    isc_blob_handle_impl blob1 = (isc_blob_handle_impl)gds.get_new_isc_blob_handle();

    final BlobParameterBuffer blobParameterBuffer = gds.newBlobParameterBuffer();

    blobParameterBuffer.addArgument(ISCConstants.isc_bpb_type, ISCConstants.isc_bpb_type_segmented);

    gds.isc_create_blob2(db1, t1, blob1, blobParameterBuffer);

    if (log!=null) log.info("test- new blob_id: " + blob1.getBlob_id());

    for (int i = 0; i< 10; i++) {
        gds.isc_put_segment(blob1, testbuf);

            if (log!=null) log.info("test- wrote bytes: " + (i * testbuf.length));
    }

        XSQLDA xsqlda = new XSQLDAImpl(2);

        XSQLVAR xsqlvar = new XSQLVARLittleEndianImpl();

        xsqlvar.sqltype = ISCConstants.SQL_SHORT;

        xsqlvar.sqllen = 2;

        xsqlvar.sqldata = xsqlvar.encodeShort((short) 3);

        xsqlda.sqlvar[0] = xsqlvar;



        xsqlvar = new XSQLVARLittleEndianImpl();

        xsqlvar.sqltype = ISCConstants.SQL_BLOB;

        xsqlvar.sqllen = 8;

        xsqlvar.sqldata = xsqlvar.encodeLong(blob1.getBlob_id());

        xsqlda.sqlvar[1] = xsqlvar;

        gds.isc_close_blob(blob1);

        if (log!=null) log.info("test- isc_dsql_exec_immed2");

        gds.isc_dsql_exec_immed2(db1, t1, "INSERT INTO R2 VALUES (?, ?)",
                                  ISCConstants.SQL_DIALECT_CURRENT, xsqlda, null);

       isc_stmt_handle_impl stmt1 = (isc_stmt_handle_impl) gds.get_new_isc_stmt_handle();

        if (log!=null) log.info("test- isc_dsql_allocate_statement");

        gds.isc_dsql_allocate_statement(db1, stmt1);

        XSQLDA out_xsqlda = gds.isc_dsql_prepare(t1, stmt1, "SELECT COL1, COL2 FROM R2",
                             ISCConstants.SQL_DIALECT_CURRENT);

        if (log!=null) log.info("test- isc_dsql_execute2");

        gds.isc_dsql_execute2(t1, stmt1, 1, null, null);

      isc_blob_handle_impl blob2 = (isc_blob_handle_impl)gds.get_new_isc_blob_handle();

        byte[][] row = null;

        gds.isc_dsql_fetch(stmt1, 1, out_xsqlda, 200);

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

            blob2.setBlob_id(xsqlvar.decodeLong(row[1]));

           gds.isc_open_blob2(db1, t1, blob2, blobParameterBuffer );

            int readcount = 0;

            do {
               byte[] answer = gds.isc_get_segment(blob2, 64);// 1050)

               if (log!=null) log.info("test- answer length: " + answer.length + ", answer string: " + new String(answer));

               readcount += answer.length;

               if (log!=null) log.info("test- read bytes: " + readcount);
         } while (!blob2.isEof());

            gds.isc_close_blob(blob2);
        }

        if (log!=null) log.info("test- isc_dsql_free_statement");

        gds.isc_dsql_free_statement(stmt1, ISCConstants.DSQL_drop);

    commit(t1);

    teardownTable2(db1);
  }



    public void testCreateAndWriteBlobStreamInStreamPieces() throws Exception {

        byte[] a = new String("a").getBytes();

        byte[] testbuf = new byte[4096];//2030];

        int reps = 10;//10000;

        Arrays.fill(testbuf, a[0]);

        if (log!=null) log.info("test- testCreateAndWriteBlobInStreamPieces");

        db1 = setupTable2();

        t1 = startTransaction(db1);

    isc_blob_handle_impl blob1 = (isc_blob_handle_impl)gds.get_new_isc_blob_handle();



    final BlobParameterBuffer blobParameterBuffer = gds.newBlobParameterBuffer();

    blobParameterBuffer.addArgument(ISCConstants.isc_bpb_type, ISCConstants.isc_bpb_type_stream);

    gds.isc_create_blob2(db1, t1, blob1, blobParameterBuffer);

    if (log!=null) log.info("test- new blob_id: " + blob1.getBlob_id());

    for (int i = 0; i< reps; i++) {
         gds.isc_put_segment(blob1, testbuf);
     }

        if (log!=null) log.info("test- wrote bytes: " + (100 * testbuf.length));

        XSQLDA xsqlda = new XSQLDAImpl(2);

        XSQLVAR xsqlvar = new XSQLVARLittleEndianImpl();

        xsqlvar.sqltype = ISCConstants.SQL_SHORT;

        xsqlvar.sqllen = 2;

        xsqlvar.sqldata = xsqlvar.encodeShort((short) 3);

        xsqlda.sqlvar[0] = xsqlvar;

        xsqlvar = new XSQLVARLittleEndianImpl();

        xsqlvar.sqltype = ISCConstants.SQL_BLOB;

        xsqlvar.sqllen = 8;

        xsqlvar.sqldata = xsqlvar.encodeLong(blob1.getBlob_id());

        xsqlda.sqlvar[1] = xsqlvar;

        gds.isc_close_blob(blob1);

        if (log!=null) log.info("test- isc_dsql_exec_immed2");

        gds.isc_dsql_exec_immed2(db1, t1, "INSERT INTO R2 VALUES (?, ?)",
                                  ISCConstants.SQL_DIALECT_CURRENT, xsqlda, null);

        isc_stmt_handle_impl stmt1 = (isc_stmt_handle_impl) gds.get_new_isc_stmt_handle();

        if (log!=null) log.info("test- isc_dsql_allocate_statement");

        gds.isc_dsql_allocate_statement(db1, stmt1);

        XSQLDA out_xsqlda = gds.isc_dsql_prepare(t1, stmt1, "SELECT COL1, COL2 FROM R2",
                              ISCConstants.SQL_DIALECT_CURRENT);

        if (log!=null) log.info("test- isc_dsql_execute2");

        gds.isc_dsql_execute2(t1, stmt1, 1, null, null);

        isc_blob_handle_impl blob2 = (isc_blob_handle_impl)gds.get_new_isc_blob_handle();

        byte[][] row = null;

        gds.isc_dsql_fetch(stmt1, 1, out_xsqlda, 200);

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

            blob2.setBlob_id(xsqlvar.decodeLong(row[1]));

           gds.isc_open_blob2(db1, t1, blob2, blobParameterBuffer );

            int readcount = 0;

            do {
                byte[] answer = gds.isc_get_segment(blob2, 4096);// 1050)

                readcount += answer.length;
            } while (!blob2.isEof());

            if (log!=null) log.info("test- read bytes: " + readcount);

            if (readcount != (reps * testbuf.length)) {
                throw new Exception("Retrieved wrong size");
            }
        gds.isc_close_blob(blob2);
        }

       if (log!=null) log.info("test- isc_dsql_free_statement");

        gds.isc_dsql_free_statement(stmt1, ISCConstants.DSQL_drop);

   commit(t1);

    teardownTable2(db1);
    }










    }


