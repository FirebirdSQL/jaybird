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
/*
 * The Original Code is the Firebird Java GDS implementation.
 *
 * The Initial Developer of the Original Code is Alejandro Alberola.
 * Portions created by Alejandro Alberola are Copyright (C) 2001
 * Boix i Oltra, S.L. All Rights Reserved.
 */

package org.firebirdsql.jgds;

import org.firebirdsql.gds.*;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Describe class <code>GDS_Impl</code> here.
 *
 * @author <a href="mailto:alberola@users.sourceforge.net">Alejandro Alberola</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public final class GDS_Impl implements GDS {

   private static Logger log = LoggerFactory.getLogger(GDS_Impl.class,false);

    /* Operation (packet) types */

    static final int op_void                = 0;    /* Packet has been voided */
    static final int op_connect             = 1;    /* Connect to remote server */
    static final int op_exit                = 2;    /* Remote end has exitted */
    static final int op_accept              = 3;    /* Server accepts connection */
    static final int op_reject              = 4;    /* Server rejects connection */
    static final int op_protocol            = 5;    /* Protocol selection */
    static final int op_disconnect          = 6;    /* Connect is going away */
    static final int op_credit              = 7;    /* Grant (buffer) credits */
    static final int op_continuation        = 8;    /* Continuation packet */
    static final int op_response            = 9;    /* Generic response block */

    /* Page server operations */

    static final int op_open_file           = 10;   /* Open file for page service */
    static final int op_create_file         = 11;   /* Create file for page service */
    static final int op_close_file          = 12;   /* Close file for page service */
    static final int op_read_page           = 13;   /* optionally lock and read page */
    static final int op_write_page          = 14;   /* write page and optionally release lock */
    static final int op_lock                = 15;   /* sieze lock */
    static final int op_convert_lock        = 16;   /* convert existing lock */
    static final int op_release_lock        = 17;   /* release existing lock */
    static final int op_blocking            = 18;   /* blocking lock message */

    /* Full context server operations */

    static final int op_attach              = 19;   /* Attach database */
    static final int op_create              = 20;   /* Create database */
    static final int op_detach              = 21;   /* Detach database */
    static final int op_compile             = 22;   /* Request based operations */
    static final int op_start               = 23;
    static final int op_start_and_send      = 24;
    static final int op_send                = 25;
    static final int op_receive             = 26;
    static final int op_unwind              = 27;
    static final int op_release             = 28;

    static final int op_transaction         = 29;   /* Transaction operations */
    static final int op_commit              = 30;
    static final int op_rollback            = 31;
    static final int op_prepare             = 32;
    static final int op_reconnect           = 33;

    static final int op_create_blob         = 34;   /* Blob operations */
    static final int op_open_blob           = 35;
    static final int op_get_segment         = 36;
    static final int op_put_segment         = 37;
    static final int op_cancel_blob         = 38;
    static final int op_close_blob          = 39;

    static final int op_info_database       = 40;   /* Information services */
    static final int op_info_request        = 41;
    static final int op_info_transaction    = 42;
    static final int op_info_blob           = 43;

    static final int op_batch_segments      = 44;   /* Put a bunch of blob segments */

    static final int op_mgr_set_affinity    = 45;   /* Establish server affinity */
    static final int op_mgr_clear_affinity  = 46;   /* Break server affinity */
    static final int op_mgr_report          = 47;   /* Report on server */

    static final int op_que_events          = 48;   /* Que event notification request */
    static final int op_cancel_events       = 49;   /* Cancel event notification request */
    static final int op_commit_retaining    = 50;   /* Commit retaining (what else) */
    static final int op_prepare2            = 51;   /* Message form of prepare */
    static final int op_event               = 52;   /* Completed event request (asynchronous) */
    static final int op_connect_request     = 53;   /* Request to establish connection */
    static final int op_aux_connect         = 54;   /* Establish auxiliary connection */
    static final int op_ddl                 = 55;   /* DDL call */
    static final int op_open_blob2          = 56;
    static final int op_create_blob2        = 57;
    static final int op_get_slice           = 58;
    static final int op_put_slice           = 59;
    static final int op_slice               = 60;   /* Successful response to static final int op_get_slice */
    static final int op_seek_blob           = 61;   /* Blob seek operation */

    /* DSQL operations */

    static final int op_allocate_statement  = 62;   /* allocate a statment handle */
    static final int op_execute             = 63;   /* execute a prepared statement */
    static final int op_exec_immediate      = 64;   /* execute a statement */
    static final int op_fetch               = 65;   /* fetch a record */
    static final int op_fetch_response      = 66;   /* response for record fetch */
    static final int op_free_statement      = 67;   /* free a statement */
    static final int op_prepare_statement   = 68;   /* prepare a statement */
    static final int op_set_cursor          = 69;   /* set a cursor name */
    static final int op_info_sql            = 70;

    static final int op_dummy               = 71;   /* dummy packet to detect loss of client */

    static final int op_response_piggyback  = 72;   /* response block for piggybacked messages */
    static final int op_start_and_receive   = 73;
    static final int op_start_send_and_receive  = 74;

    static final int op_exec_immediate2     = 75;   /* execute an immediate statement with msgs */
    static final int op_execute2            = 76;   /* execute a statement with msgs */
    static final int op_insert              = 77;
    static final int op_sql_response        = 78;   /* response from execute; exec immed; insert */

    static final int op_transact            = 79;
    static final int op_transact_response   = 80;
    static final int op_drop_database       = 81;

    static final int op_service_attach      = 82;
    static final int op_service_detach      = 83;
    static final int op_service_info        = 84;
    static final int op_service_start       = 85;

    static final int op_rollback_retaining  = 86;


    static final int MAX_BUFFER_SIZE = 1024; //8192;//4096; //max size for response for ??
    
    public GDS_Impl() {
    }


    // Database functions


    /**
     * <code>isc_create_database</code> creates a database
     * based on the file name and Clumplet of database properties
     * supplied.  The supplied db handle is attached to the
     * newly created database.
     *
     * @param file_name a <code>String</code> the file name,
     * including host and port, for the database.
     * The expected format is host:port:path_to_file.
     * The value for host is localhost if not supplied.
     * The value for port is 3050 if not supplied.
     * @param db_handle an <code>isc_db_handle</code> The db handle to
     * attach to the new database.
     * @param c a <code>Clumplet</code> The parameters for the new database
     * and the attachment to it.  See docs for dpb (database
     * parameter block.)
     * @exception GDSException if an error occurs
     */
    public void isc_create_database(String file_name,
                                   isc_db_handle db_handle,
                                   Clumplet c) throws GDSException {

        boolean debug = log != null && log.isDebugEnabled();
        isc_db_handle_impl db = (isc_db_handle_impl) db_handle;

        if (db == null) {
            throw new GDSException(ISCConstants.isc_bad_db_handle);
        }

        synchronized (db) {


            DbAttachInfo dbai = new DbAttachInfo(file_name);
            connect(db, dbai);
            try {
                if (debug) log.debug("op_create ");
                db.out.writeInt(op_create);
                db.out.writeInt(0);           // packet->p_atch->p_atch_database
                db.out.writeString(dbai.getFileName());
                db.out.writeTyped(ISCConstants.isc_dpb_version1, (Xdrable)c);
                //            db.out.writeBuffer(dpb, dpb_length);
                db.out.flush();            
                if (debug) log.debug("sent");

                try {
                    receiveResponse(db,-1);
                    db.setRdb_id(db.getResp_object());
                } catch (GDSException g) {
                    disconnect(db);
                    throw g;
                }
            } catch (IOException ex) {
                throw new GDSException(ISCConstants.isc_net_write_err);
            }
        }

    }

    public void isc_attach_database(String host,
                                    Integer port,
                                    String file_name,
                                    isc_db_handle db_handle,
                                    Clumplet dpb) throws GDSException  {
        DbAttachInfo dbai = new DbAttachInfo(host, port, file_name);
        isc_attach_database(dbai, db_handle, dpb);
    }

    public void isc_attach_database(String connectString,
                                   isc_db_handle db_handle,
                                   Clumplet dpb) throws GDSException  {

        DbAttachInfo dbai = new DbAttachInfo(connectString);
        isc_attach_database(dbai, db_handle, dpb);
    }



      final static byte[] describe_database_info = new byte[] { ISCConstants.isc_info_db_sql_dialect,
                                   ISCConstants.isc_info_isc_version,
                                   ISCConstants.isc_info_ods_version,
                                   ISCConstants.isc_info_ods_minor_version,
                                   ISCConstants.isc_info_end
                                   };

    public void isc_attach_database(DbAttachInfo dbai,
                                   isc_db_handle db_handle,
                                   Clumplet dpb) throws GDSException  {

        boolean debug = log != null && log.isDebugEnabled();
        isc_db_handle_impl db = (isc_db_handle_impl) db_handle;

        if (db == null) {
            throw new GDSException(ISCConstants.isc_bad_db_handle);
        }

        synchronized (db) {
            connect(db, dbai);
            try {
                if (debug) log.debug("op_attach ");
                db.out.writeInt(op_attach);
                db.out.writeInt(0);                // packet->p_atch->p_atch_database
                db.out.writeString(dbai.getFileName());
                db.out.writeTyped(ISCConstants.isc_dpb_version1, (Xdrable)dpb);
                db.out.flush();            
                if (debug) log.debug("sent");

                try {
                    receiveResponse(db,-1);
                    db.setRdb_id(db.getResp_object());
                }
                catch (GDSException ge) {
                    disconnect(db);
                    throw ge;
                }
                // read database information
                parseAttachDatabaseInfo(isc_database_info(db,describe_database_info,1024),db);
            } catch (IOException ex) {
                throw new GDSException(ISCConstants.isc_net_write_err);
            }
        }
    }

    public byte[] isc_database_info(isc_db_handle handle,
                                 byte[] items,
                                 int buffer_length) throws GDSException {
        boolean debug = log != null && log.isDebugEnabled();
        isc_db_handle_impl db = (isc_db_handle_impl) handle;
        synchronized (db){
            try {
                if (debug) log.debug("op_info_database ");
                db.out.writeInt(op_info_database);
                db.out.writeInt(db.getRdb_id());
                db.out.writeInt(0);
                db.out.writeBuffer(items);
                db.out.writeInt(buffer_length);
                db.out.flush();
                if (debug) log.debug("sent");
                receiveResponse(db,-1);
                if (debug) log.debug("parseSqlInfo: first 2 bytes are " + isc_vax_integer(db.getResp_data(), 0, 2) + " or: " + db.getResp_data()[0] + ", " + db.getResp_data()[1]);
                return db.getResp_data();
            } catch (IOException ex) {
                throw new GDSException(ISCConstants.isc_network_error);
            } 
        }
    }

/**
 * Parse database info returned after attach. This method assumes that
 * it is not truncated.
 * @param info information returned by isc_database_info call
 * @param handle isc_db_handle to set connection parameters
 * @throws GDSException if something went wrong :))
 */
    private void parseAttachDatabaseInfo(byte[] info, isc_db_handle handle) throws GDSException {
        boolean debug = log != null && log.isDebugEnabled();
        if (debug) log.debug("parseDatabaseInfo: first 2 bytes are " + isc_vax_integer(info, 0, 2) + " or: " + info[0] + ", " + info[1]);	  
        int value=0;
        int len=0;
        int i = 0;
        isc_db_handle_impl db = (isc_db_handle_impl) handle;
        while (info[i] != ISCConstants.isc_info_end) {
            switch (info[i++]) {
                case ISCConstants.isc_info_db_sql_dialect:
                    len = isc_vax_integer(info, i, 2);
                    i += 2;
                    value = isc_vax_integer (info, i, len);
                    i += len;
                    db.setDialect(value);
                    if (debug) log.debug("isc_info_db_sql_dialect:"+value);
                    break;
                case ISCConstants.isc_info_isc_version:
                    len = isc_vax_integer(info, i, 2);
                    i += 2;
                    if (debug) log.debug("isc_info_version len:"+len);
                    // This +/-2 offset is to skip count and version string length
                    byte[] vers = new byte[len-2];
                    System.arraycopy(info, i+2, vers, 0, len-2);
                    String versS = new String(vers);
                    i += len;
                    db.setVersion(versS);
                    if (debug) log.debug("isc_info_version:"+versS);
                    break;
                case ISCConstants.isc_info_ods_version:
                    len = isc_vax_integer(info, i, 2);
                    i += 2;
                    value = isc_vax_integer (info, i, len);
                    i += len;
                    db.setODSMajorVersion(value);
                    if (debug) log.debug("isc_info_ods_version:"+value);
                    break;
                case ISCConstants.isc_info_ods_minor_version:
                    len = isc_vax_integer(info, i, 2);
                    i += 2;
                    value = isc_vax_integer (info, i, len);
                    i += len;
                    db.setODSMinorVersion(value);
                    if (debug) log.debug("isc_info_ods_minor_version:"+value);
                    break;
                case ISCConstants.isc_info_truncated:
                    if (debug) log.debug("isc_info_truncated ");
                    return;
                default:
                    throw new GDSException(ISCConstants.isc_dsql_sqlda_err);
            }
        }
    }
	 
    public void isc_detach_database(isc_db_handle db_handle) throws GDSException {
        boolean debug = log != null && log.isDebugEnabled();
        isc_db_handle_impl db = (isc_db_handle_impl) db_handle;
        if (db == null) {
            throw new GDSException(ISCConstants.isc_bad_db_handle);
        }

        synchronized (db) {
            if (db_handle.hasTransactions()) 
            {
                throw new GDSException(ISCConstants.isc_open_trans, db.getOpenTransactionCount());
            } // end of if ()
            try {
                if (debug) log.debug("op_detach ");
                db.out.writeInt(op_detach);
                db.out.writeInt(db.getRdb_id());
                db.out.flush();            
                if (debug) log.debug("sent");
                receiveResponse(db,-1);
                
                
            } catch (IOException ex) {
                throw new GDSException(ISCConstants.isc_network_error);
            } 
            finally
            {
                try 
                {
                    disconnect(db);
                }
                catch (IOException ex2) 
                {
                    throw new GDSException(ISCConstants.isc_network_error);
                } 
            } // end of finally
        }
    }

    public void isc_drop_database(isc_db_handle db_handle) throws GDSException {

        boolean debug = log != null && log.isDebugEnabled();
        isc_db_handle_impl db = (isc_db_handle_impl) db_handle;

        if (db == null) {
            throw new GDSException(ISCConstants.isc_bad_db_handle);
        }

        synchronized (db) {

            try {
                if (debug) log.debug("op_drop_database ");
                db.out.writeInt(op_drop_database);
                db.out.writeInt(db.getRdb_id());
                db.out.flush();            
                if (debug) log.debug("sent");
                receiveResponse(db,-1);
            } catch (IOException ex) {
                throw new GDSException(ISCConstants.isc_network_error);
            }
        }

    }

    public byte[] isc_expand_dpb(byte[] dpb, int dpb_length,
                                 int param, Object[] params) throws GDSException {
        return dpb;
    }


    // Transaction functions

    public void isc_start_transaction(     isc_tr_handle tr_handle,
                                        isc_db_handle db_handle,
//                                        Set tpb
//                                int tpb_length,
                                byte[] tpb) throws GDSException {

        boolean debug = log != null && log.isDebugEnabled();
        isc_tr_handle_impl tr = (isc_tr_handle_impl) tr_handle;
        isc_db_handle_impl db = (isc_db_handle_impl) db_handle;

        if (tr_handle == null) {
            throw new GDSException(ISCConstants.isc_bad_trans_handle);
        }

        if (db_handle == null) {
            throw new GDSException(ISCConstants.isc_bad_db_handle);
        }
        synchronized (db) {
            if (tr.getState() != isc_tr_handle.NOTRANSACTION) {
                throw new GDSException(ISCConstants.isc_tra_state);
            }
            tr.setState(isc_tr_handle.TRANSACTIONSTARTING);

            try {
                if (debug) log.debug("op_transaction ");
                db.out.writeInt(op_transaction);
                db.out.writeInt(db.getRdb_id());
                db.out.writeSet(ISCConstants.isc_tpb_version3, tpb);
                //            db.out.writeBuffer(tpb, tpb_length);
                db.out.flush();            
                if (debug) log.debug("sent");
                //out.flush();
                receiveResponse(db,-1);
            } catch (IOException ex) {
                throw new GDSException(ISCConstants.isc_network_error);
            }
            tr.setTransactionId(db.getResp_object());

            //tr.rtr_rdb = db;
            tr.setDbHandle(db);
            tr.setState(isc_tr_handle.TRANSACTIONSTARTED);
            //db.rdb_transactions.addElement(tr);
        }//end synch on db

    }

    public void isc_commit_transaction(isc_tr_handle tr_handle) throws GDSException {
        boolean debug = log != null && log.isDebugEnabled();
        if (tr_handle == null) {
            throw new GDSException(ISCConstants.isc_bad_trans_handle);
        }

        isc_tr_handle_impl tr = (isc_tr_handle_impl) tr_handle;
        isc_db_handle_impl db = (isc_db_handle_impl)tr.getDbHandle();


        synchronized (db) {

            if (tr.getState() != isc_tr_handle.TRANSACTIONSTARTED && tr.getState() != isc_tr_handle.TRANSACTIONPREPARED) {
                throw new GDSException(ISCConstants.isc_tra_state);
            }
            tr.setState(isc_tr_handle.TRANSACTIONCOMMITTING);

            try {
                if (debug) {
                    log.debug("op_commit ");
                    log.debug("tr.rtr_id: " + tr.getTransactionId());
                }
                db.out.writeInt(op_commit);
                db.out.writeInt(tr.getTransactionId());
                db.out.flush();            
                if (debug) log.debug("sent");
                receiveResponse(db,-1);
            } catch (IOException ex) {
                throw new GDSException(ISCConstants.isc_net_read_err);
            }

            tr.setState(isc_tr_handle.NOTRANSACTION);
            //tr.rtr_rdb = null;
            //db.rdb_transactions.removeElement(tr);
            tr.unsetDbHandle();
        }

    }

    public void isc_commit_retaining( isc_tr_handle tr_handle) throws GDSException {
        boolean debug = log != null && log.isDebugEnabled();
        isc_tr_handle_impl tr = (isc_tr_handle_impl) tr_handle;
        if (tr == null) {
            throw new GDSException(ISCConstants.isc_bad_trans_handle);
        }
        isc_db_handle_impl db = (isc_db_handle_impl)tr.getDbHandle();

        synchronized (db) {
            if (tr.getState() != isc_tr_handle.TRANSACTIONSTARTED && tr.getState() != isc_tr_handle.TRANSACTIONPREPARED) {
                throw new GDSException(ISCConstants.isc_tra_state);
            }
            tr.setState(isc_tr_handle.TRANSACTIONCOMMITTING);

            try {
                if (debug) log.debug("op_commit_retaining ");
                db.out.writeInt(op_commit_retaining);
                db.out.writeInt(tr.getTransactionId());
                db.out.flush();            
                if (debug) log.debug("sent");
                receiveResponse(db,-1);
            } catch (IOException ex) {
                throw new GDSException(ISCConstants.isc_net_read_err);
            }
            tr.setState(isc_tr_handle.TRANSACTIONSTARTED);
        }

    }

    public void isc_prepare_transaction(isc_tr_handle tr_handle) throws GDSException {
        boolean debug = log != null && log.isDebugEnabled();
        isc_tr_handle_impl tr = (isc_tr_handle_impl) tr_handle;
        if (tr == null) {
            throw new GDSException(ISCConstants.isc_bad_trans_handle);
        }
        isc_db_handle_impl db = (isc_db_handle_impl)tr.getDbHandle();

        synchronized (db) {
            if (tr.getState() != isc_tr_handle.TRANSACTIONSTARTED) {
                throw new GDSException(ISCConstants.isc_tra_state);
            }
            tr.setState(isc_tr_handle.TRANSACTIONPREPARING);
            try {
                if (debug) log.debug("op_prepare ");
                db.out.writeInt(op_prepare);
                db.out.writeInt(tr.getTransactionId());
                db.out.flush();            
                if (debug) log.debug("sent");
                receiveResponse(db,-1);
            } catch (IOException ex) {
                throw new GDSException(ISCConstants.isc_net_read_err);
            }
            tr.setState(isc_tr_handle.TRANSACTIONPREPARED);
        }
    }

    public void isc_prepare_transaction2(isc_tr_handle tr_handle,
                                        byte[] bytes) throws GDSException {
        boolean debug = log != null && log.isDebugEnabled();
        isc_tr_handle_impl tr = (isc_tr_handle_impl) tr_handle;
        if (tr == null) {
            throw new GDSException(ISCConstants.isc_bad_trans_handle);
        }
        isc_db_handle_impl db = (isc_db_handle_impl)tr.getDbHandle();

        synchronized (db) {
            if (tr.getState() != isc_tr_handle.TRANSACTIONSTARTED) {
                throw new GDSException(ISCConstants.isc_tra_state);
            }
            tr.setState(isc_tr_handle.TRANSACTIONPREPARING);
            try {
                if (debug) log.debug("op_prepare2 ");
                db.out.writeInt(op_prepare2);
                db.out.writeInt(tr.getTransactionId());
                db.out.writeBuffer(bytes);
                db.out.flush();            
                if (debug) log.debug("sent");
                receiveResponse(db,-1);
            } catch (IOException ex) {
                throw new GDSException(ISCConstants.isc_net_read_err);
            }

            tr.setState(isc_tr_handle.TRANSACTIONPREPARED);
        }
    }


    public void isc_rollback_transaction(     isc_tr_handle tr_handle) throws GDSException {

        boolean debug = log != null && log.isDebugEnabled();
        isc_tr_handle_impl tr = (isc_tr_handle_impl) tr_handle;
        if (tr == null) {
            throw new GDSException(ISCConstants.isc_bad_trans_handle);
        }
        isc_db_handle_impl db = (isc_db_handle_impl)tr.getDbHandle();

        synchronized (db) {

            if (tr.getState() == isc_tr_handle.NOTRANSACTION)
            {
                throw new GDSException(ISCConstants.isc_tra_state);
            }
            tr.setState(isc_tr_handle.TRANSACTIONROLLINGBACK);

            try {
                if (debug) log.debug("op_rollback ");
                db.out.writeInt(op_rollback);
                db.out.writeInt(tr.getTransactionId());
                db.out.flush();            
                if (debug) log.debug("sent");
                receiveResponse(db,-1);
            } catch (IOException ex) {
                throw new GDSException(ISCConstants.isc_net_read_err);
            }
            finally
            {
                tr.setState(isc_tr_handle.NOTRANSACTION);
                tr.unsetDbHandle();
            } // end of finally
            
        }

    }

    public void isc_rollback_retaining( isc_tr_handle tr_handle) throws GDSException {
        boolean debug = log != null && log.isDebugEnabled();
        isc_tr_handle_impl tr = (isc_tr_handle_impl) tr_handle;
        if (tr == null) {
            throw new GDSException(ISCConstants.isc_bad_trans_handle);
        }
        isc_db_handle_impl db = (isc_db_handle_impl)tr.getDbHandle();

        synchronized (db) {
            if (tr.getState() != isc_tr_handle.TRANSACTIONSTARTED && tr.getState() != isc_tr_handle.TRANSACTIONPREPARED) {
                throw new GDSException(ISCConstants.isc_tra_state);
            }
            tr.setState(isc_tr_handle.TRANSACTIONROLLINGBACK);

            try {
                if (debug) log.debug("op_rollback_retaining ");
                db.out.writeInt(op_rollback_retaining);
                db.out.writeInt(tr.getTransactionId());
                db.out.flush();            
                if (debug) log.debug("sent");
                receiveResponse(db,-1);
            } catch (IOException ex) {
                throw new GDSException(ISCConstants.isc_net_read_err);
            }
            tr.setState(isc_tr_handle.TRANSACTIONSTARTED);
        }

    }

    // Dynamic SQL

    public void isc_dsql_allocate_statement(        isc_db_handle db_handle,
                                           isc_stmt_handle stmt_handle) throws GDSException {
        boolean debug = log != null && log.isDebugEnabled();
        isc_db_handle_impl db = (isc_db_handle_impl) db_handle;
        isc_stmt_handle_impl stmt = (isc_stmt_handle_impl) stmt_handle;

        if (db_handle == null) {
            throw new GDSException(ISCConstants.isc_bad_db_handle);
        }

        if (stmt_handle == null) {
            throw new GDSException(ISCConstants.isc_bad_req_handle);
        }

        synchronized (db) {
            try {
                if (debug) log.debug("op_allocate_statement ");
                db.out.writeInt(op_allocate_statement);
                db.out.writeInt(db.getRdb_id());
                db.out.flush();            
                if (debug) log.debug("sent");
                receiveResponse(db,-1);
                stmt.setRsr_id(db.getResp_object());
            } catch (IOException ex) {
                throw new GDSException(ISCConstants.isc_net_read_err);
            }

            stmt.setRsr_rdb(db);
            
            /** @todo implement statement handle tracking correctly */
            // db.rdb_sql_requests.addElement(stmt);
            stmt.setAllRowsFetched(false);
        }

    }

    public void isc_dsql_alloc_statement2(      isc_db_handle db_handle,
                                         isc_stmt_handle stmt_handle) throws GDSException {
        throw new GDSException(ISCConstants.isc_wish_list);
    }

    final static byte[] describe_select_info = new byte[] { ISCConstants.isc_info_sql_select,
                                                   ISCConstants.isc_info_sql_describe_vars,
                                                   ISCConstants.isc_info_sql_sqlda_seq,
                                                   ISCConstants.isc_info_sql_type,
                                                   ISCConstants.isc_info_sql_sub_type,
                                                   ISCConstants.isc_info_sql_scale,
                                                   ISCConstants.isc_info_sql_length,
                                                   ISCConstants.isc_info_sql_field,
                                                   ISCConstants.isc_info_sql_relation,
                                                   ISCConstants.isc_info_sql_owner,
                                                   ISCConstants.isc_info_sql_alias,
                                                   ISCConstants.isc_info_sql_describe_end };

    public XSQLDA isc_dsql_describe(isc_stmt_handle stmt_handle,
                                 int da_version) throws GDSException {


        byte[] buffer = isc_dsql_sql_info(stmt_handle,
                              /* describe_select_info.length,*/ describe_select_info,
                              MAX_BUFFER_SIZE);
        return parseSqlInfo(stmt_handle, buffer, describe_select_info);
    }

    final static byte[] describe_bind_info = new byte[] { ISCConstants.isc_info_sql_bind,
                                                 ISCConstants.isc_info_sql_describe_vars,
                                                 ISCConstants.isc_info_sql_sqlda_seq,
                                                 ISCConstants.isc_info_sql_type,
                                                 ISCConstants.isc_info_sql_sub_type,
                                                 ISCConstants.isc_info_sql_scale,
                                                 ISCConstants.isc_info_sql_length,
                                                 ISCConstants.isc_info_sql_field,
                                                 ISCConstants.isc_info_sql_relation,
                                                 ISCConstants.isc_info_sql_owner,
                                                 ISCConstants.isc_info_sql_alias,
                                                 ISCConstants.isc_info_sql_describe_end };

    public XSQLDA isc_dsql_describe_bind(isc_stmt_handle stmt_handle,
                                         int da_version) throws GDSException {

        isc_stmt_handle_impl stmt = (isc_stmt_handle_impl) stmt_handle;
        
        byte[] buffer = isc_dsql_sql_info(stmt_handle,
                              /* describe_bind_info.length,*/ describe_bind_info,
                              MAX_BUFFER_SIZE);
        
        stmt.setInSqlda(parseSqlInfo(stmt_handle, buffer, describe_bind_info));
        return stmt.getInSqlda();
    }


    public void isc_dsql_execute(isc_tr_handle tr_handle,
                                isc_stmt_handle stmt_handle,
                                int da_version,
                                XSQLDA xsqlda) throws GDSException {

        isc_dsql_execute2(tr_handle, stmt_handle, da_version,
                                 xsqlda, null);
    }


    public void isc_dsql_execute2(isc_tr_handle tr_handle,
                                 isc_stmt_handle stmt_handle,
                                 int da_version,
                                 XSQLDA in_xsqlda,
                                 XSQLDA out_xsqlda) throws GDSException {

        boolean debug = log != null && log.isDebugEnabled();
        isc_tr_handle_impl tr = (isc_tr_handle_impl) tr_handle;
        isc_stmt_handle_impl stmt = (isc_stmt_handle_impl) stmt_handle;
        isc_db_handle_impl db = stmt.getRsr_rdb();

        // Test Handles needed here
        synchronized (db) {
            XdrOutputStream out = db.out;
            try {
                if (debug) log.debug((out_xsqlda == null) ? "op_execute " : "op_execute2 ");

                out.writeInt((out_xsqlda == null) ? op_execute : op_execute2);
                out.writeInt(stmt.getRsr_id());
                out.writeInt(tr.getTransactionId());

                if (in_xsqlda != null){
                    out.writeBuffer(in_xsqlda.blr);
                    out.writeInt(0);  //message number = in_message_type
                    out.writeInt(1);  //stmt->rsr_bind_format
                    out.writeSQLData(in_xsqlda);
                }
                else{
                    out.writeBuffer(null);
                    out.writeInt(0);  //message number = in_message_type
                    out.writeInt(0);  //stmt->rsr_bind_format
                }

                if (out_xsqlda != null) {
                    stmt.clearRows();
                    // only need to clear if there is a						 
                    out.writeBuffer(out_xsqlda.blr);
                    out.writeInt(0); //out_message_number = out_message_type
                }
                out.flush();
                if (debug) log.debug("sent");
                int op = nextOperation(db);
                if (op == op_sql_response) {
                    //this would be an Execute procedure
                    stmt.ensureCapacity(1);
                    receiveSqlResponse(db, out_xsqlda, stmt);
                    op = nextOperation(db);
                    stmt.setAllRowsFetched(true);
                    stmt.setIsSingletonResult(true);
                }
                else 
                {
                    stmt.setIsSingletonResult(false);
                } // end of else
                receiveResponse(db,op);
            } catch (IOException ex) {
                throw new GDSException(ISCConstants.isc_net_read_err);
            }
        }
    }


    public void isc_dsql_execute_immediate(isc_db_handle db_handle,
                                          isc_tr_handle tr_handle,
                                          String statement,
                                          int dialect,
                                          XSQLDA xsqlda) throws GDSException {
        isc_dsql_exec_immed2(db_handle, tr_handle, statement, dialect, xsqlda, null);
    }
    
    public void isc_dsql_execute_immediate(isc_db_handle db_handle,
                                          isc_tr_handle tr_handle,
                                          String statement,
                                          String encoding,
                                          int dialect,
                                          XSQLDA xsqlda) throws GDSException {
        isc_dsql_exec_immed2(db_handle, tr_handle, statement, 
            encoding, dialect, xsqlda, null);
    }


    public void isc_dsql_exec_immed2(isc_db_handle db_handle,
                                    isc_tr_handle tr_handle,
                                    String statement,
                                    int dialect,
                                    XSQLDA in_xsqlda,
                                    XSQLDA out_xsqlda) throws GDSException {
        isc_dsql_exec_immed2(db_handle, tr_handle, 
            statement, "NONE", dialect, in_xsqlda, out_xsqlda);
    }

    public void isc_dsql_exec_immed2(isc_db_handle db_handle,
                                    isc_tr_handle tr_handle,
                                    String statement,
                                    String encoding,
                                    int dialect,
                                    XSQLDA in_xsqlda,
                                    XSQLDA out_xsqlda) throws GDSException {
        boolean debug = log != null && log.isDebugEnabled();
        isc_tr_handle_impl tr = (isc_tr_handle_impl) tr_handle;
        isc_db_handle_impl db = (isc_db_handle_impl) db_handle;

        // Test Handles

        synchronized (db) {
            XdrOutputStream out = db.out;
            try {                

                if (in_xsqlda == null && out_xsqlda == null) {
                    if (debug) log.debug("op_exec_immediate ");
                    out.writeInt(op_exec_immediate);
                } else {
                    if (debug) log.debug("op_exec_immediate2 ");
                    out.writeInt(op_exec_immediate2);

                    if (in_xsqlda != null){
                        out.writeBuffer(in_xsqlda.blr);
                        out.writeInt(0);
                        out.writeInt(1);
                        out.writeSQLData(in_xsqlda);
                    }
                    else{
                        out.writeBuffer(null);
                        out.writeInt(0);
                        out.writeInt(0);
                    }
                    if (out_xsqlda != null)
                        out.writeBuffer(out_xsqlda.blr);
                    else
                        out.writeBuffer(null);
                    out.writeInt(0);
                }

                out.writeInt(tr.getTransactionId());
                out.writeInt(0);
                out.writeInt(dialect);
                out.writeString(statement, encoding);
                out.writeString("");
                out.writeInt(0);
                out.flush();            

                if (debug) log.debug("sent");

                int op = nextOperation(db);
                if (op == op_sql_response) {
                    receiveSqlResponse(db, out_xsqlda, null);
                    op = nextOperation(db);
                }
                receiveResponse(db,op);
            } catch (IOException ex) {
                throw new GDSException(ISCConstants.isc_net_read_err);
            }
        }
    }


    public void isc_dsql_fetch(isc_stmt_handle stmt_handle,
                              int da_version,
                              XSQLDA xsqlda, int fetchSize) throws GDSException {

        boolean debug = log != null && log.isDebugEnabled();
        isc_stmt_handle_impl stmt = (isc_stmt_handle_impl) stmt_handle;
        isc_db_handle_impl db = stmt.getRsr_rdb();

        if (stmt_handle == null) {
            throw new GDSException(ISCConstants.isc_bad_req_handle);
        }

        if (xsqlda == null) {
            throw new GDSException(ISCConstants.isc_dsql_sqlda_err);
        }

        if (fetchSize <= 0) {
            throw new GDSException(ISCConstants.isc_dsql_sqlda_err);
        }
        // Apply fetchSize
        synchronized (db) {
            XdrOutputStream out = db.out;
            XdrInputStream in = db.in;
            try {
                    //Fetch next batch of rows
                    stmt.ensureCapacity(fetchSize);
                    if (debug) log.debug("op_fetch ");
                    out.writeInt(op_fetch);
                    out.writeInt(stmt.getRsr_id());
                    out.writeBuffer(xsqlda.blr);
                    out.writeInt(0);              // p_sqldata_message_number
                    out.writeInt(fetchSize); // p_sqldata_messages
                    out.flush();            
                    if (debug) log.debug("sent");

                    int op = nextOperation(db);
                    if (op == op_fetch_response) {
                        int sqldata_status;
                        int sqldata_messages;

                        do {
                            sqldata_status = in.readInt();
                            sqldata_messages = in.readInt();

                            if (sqldata_messages > 0 && sqldata_status == 0) {
                                in.readSQLData(xsqlda.ioLength,stmt);
                                nextOperation(db);
                            }

                        } while (sqldata_messages > 0 && sqldata_status == 0);

                        if (sqldata_status == 100) {
                            if (debug) log.debug("all rows successfully fetched");
                            stmt.setAllRowsFetched(true);
                        }
                    }
                    else {
                        receiveResponse(db,op);
                    }
            } catch (IOException ex) {
                throw new GDSException(ISCConstants.isc_net_read_err);
            }
        }
    }

    public static void calculateIOLength(XSQLDA xsqlda){
        xsqlda.ioLength = new int[xsqlda.sqld];
        for (int i = 0; i < xsqlda.sqld; i++) {
            switch (xsqlda.sqlvar[i].sqltype & ~1) {
                case ISCConstants.SQL_TEXT:
                    xsqlda.ioLength[i] = xsqlda.sqlvar[i].sqllen+1;
                    break;
                case ISCConstants.SQL_VARYING:
                    xsqlda.ioLength[i] = 0;
                    break;
                case ISCConstants.SQL_SHORT:
                case ISCConstants.SQL_LONG:
                case ISCConstants.SQL_FLOAT:
                case ISCConstants.SQL_TYPE_TIME:
                case ISCConstants.SQL_TYPE_DATE:
                    xsqlda.ioLength[i] = -4;
                    break;
//              case SQL_D_FLOAT:
//                  break;
                case ISCConstants.SQL_DOUBLE:
                case ISCConstants.SQL_TIMESTAMP:
                case ISCConstants.SQL_BLOB:
                case ISCConstants.SQL_ARRAY:
                case ISCConstants.SQL_QUAD:
                case ISCConstants.SQL_INT64:
                    xsqlda.ioLength[i] = -8;
                    break;
            }
        }
    }

    public void isc_dsql_free_statement(    isc_stmt_handle stmt_handle,
                                       int option) throws GDSException {
        boolean debug = log != null && log.isDebugEnabled();
        isc_stmt_handle_impl stmt = (isc_stmt_handle_impl) stmt_handle;
        isc_db_handle_impl db = stmt.getRsr_rdb();

        if (stmt_handle == null) {
            throw new GDSException(ISCConstants.isc_bad_req_handle);
        }

        //Does not seem to be possible or necessary to close
        //an execute procedure statement.
        if (stmt.getIsSingletonResult() && option == ISCConstants.DSQL_close) 
        {
            return;        
        } // end of if ()
        

        synchronized (db) {
            try {
                if (!db.isValid()) 
                {
                    //too late, socket has been closed
                    return;
                } // end of if ()
                
                if (debug) log.debug("op_free_statement ");
                db.out.writeInt(op_free_statement);
                db.out.writeInt(stmt.getRsr_id());
                db.out.writeInt(option);
                db.out.flush();            
                if (debug) log.debug("sent");

                receiveResponse(db,-1);
                if (option == ISCConstants.DSQL_drop) {
                    stmt.setInSqlda(null);
                    stmt.setOutSqlda(null);
                }
                // those rows are used by cachedFetcher don't clear
                stmt.clearRows();
                
                /** @todo implement statement handle tracking correctly */
                // db.rdb_sql_requests.remove(stmt);
            } catch (IOException ex) {
                throw new GDSException(ISCConstants.isc_net_read_err);
            }
        }

    }

    final static byte[] sql_prepare_info = new byte[] { ISCConstants.isc_info_sql_select,
                                               ISCConstants.isc_info_sql_describe_vars,
                                               ISCConstants.isc_info_sql_sqlda_seq,
                                               ISCConstants.isc_info_sql_type,
                                               ISCConstants.isc_info_sql_sub_type,
                                               ISCConstants.isc_info_sql_scale,
                                               ISCConstants.isc_info_sql_length,
                                               ISCConstants.isc_info_sql_field,
                                               ISCConstants.isc_info_sql_relation,
                                               ISCConstants.isc_info_sql_owner,
                                               ISCConstants.isc_info_sql_alias,
                                               ISCConstants.isc_info_sql_describe_end };
	 
    public XSQLDA isc_dsql_prepare(isc_tr_handle tr_handle,
                                isc_stmt_handle stmt_handle,
                                String statement,
                                int dialect/*,
                                 xsqlda*/) throws GDSException {
        return isc_dsql_prepare(tr_handle, stmt_handle, statement, "NONE", dialect);
    }

    public XSQLDA isc_dsql_prepare(isc_tr_handle tr_handle,
                                isc_stmt_handle stmt_handle,
                                String statement,
                                String encoding,
                                int dialect) throws GDSException {
        boolean debug = log != null && log.isDebugEnabled();
        isc_tr_handle_impl tr = (isc_tr_handle_impl) tr_handle;
        isc_stmt_handle_impl stmt = (isc_stmt_handle_impl) stmt_handle;
        isc_db_handle_impl db = stmt.getRsr_rdb();

        if (tr_handle == null) {
            throw new GDSException(ISCConstants.isc_bad_trans_handle);
        }

        if (stmt_handle == null) {
            throw new GDSException(ISCConstants.isc_bad_req_handle);
        }

        //reinitialize stmt SQLDA members.

        stmt.setInSqlda(null);
        stmt.setOutSqlda(null);


        synchronized (db) {
            try {
                if (debug) log.debug("op_prepare_statement ");
                db.out.writeInt(op_prepare_statement);
                db.out.writeInt(tr.getTransactionId());
                db.out.writeInt(stmt.getRsr_id());
                db.out.writeInt(dialect);
                db.out.writeString(statement, encoding);
                db.out.writeBuffer(sql_prepare_info);
                db.out.writeInt(MAX_BUFFER_SIZE);
                db.out.flush();            

                if (debug) log.debug("sent");
                receiveResponse(db,-1);
                stmt.setOutSqlda(parseSqlInfo(stmt_handle, db.getResp_data(), sql_prepare_info));
                return stmt.getOutSqlda();
            } catch (IOException ex) {
                throw new GDSException(ISCConstants.isc_net_read_err);
            }

            // RSR_blob ??????????
        }

    }


    public void isc_dsql_set_cursor_name(     isc_stmt_handle stmt_handle,
                                        String cursor_name,
                                        int type) throws GDSException {
        boolean debug = log != null && log.isDebugEnabled();
        isc_stmt_handle_impl stmt = (isc_stmt_handle_impl) stmt_handle;
        isc_db_handle_impl db = stmt.getRsr_rdb();

        if (stmt_handle == null) {
            throw new GDSException(ISCConstants.isc_bad_req_handle);
        }

        synchronized (db) {
            try {
                if (debug) log.debug("op_set_cursor ");
                db.out.writeInt(op_set_cursor);
                db.out.writeInt(stmt.getRsr_id());

                byte[] buffer = new byte[cursor_name.length() + 1];
                System.arraycopy(cursor_name.getBytes(), 0,
                                 buffer, 0, cursor_name.length());
                buffer[cursor_name.length()] = (byte) 0;

                db.out.writeBuffer(buffer);
                db.out.writeInt(0);
                db.out.flush();            
                if (debug) log.debug("sent");

                receiveResponse(db,-1);
            } catch (IOException ex) {
                throw new GDSException(ISCConstants.isc_net_read_err);
            }
        }


    }


    public byte[] isc_dsql_sql_info(isc_stmt_handle stmt_handle,
                                 byte[] items,
                                 int buffer_length) throws GDSException {

        boolean debug = log != null && log.isDebugEnabled();
        isc_stmt_handle_impl stmt = (isc_stmt_handle_impl) stmt_handle;
        isc_db_handle_impl db = stmt.getRsr_rdb();

        synchronized (db) {
            try {
                if (debug) log.debug("op_info_sql ");
                db.out.writeInt(op_info_sql);
                db.out.writeInt(stmt.getRsr_id());
                db.out.writeInt(0);
                db.out.writeBuffer(items);
                db.out.writeInt(buffer_length);
                db.out.flush();            
                if (debug) log.debug("sent");
                receiveResponse(db,-1);
                return db.getResp_data();
            } catch (IOException ex) {
                throw new GDSException(ISCConstants.isc_net_read_err);
            }
        }


    }
	 
    private static byte[] stmtInfo = new byte[]
        {ISCConstants.isc_info_sql_records,
         ISCConstants.isc_info_sql_stmt_type,
         ISCConstants.isc_info_end};
    private static int INFO_SIZE = 128;

    public void getSqlCounts(isc_stmt_handle stmt_handle) throws GDSException {
        isc_stmt_handle_impl stmt = (isc_stmt_handle_impl) stmt_handle;
        byte[] buffer = isc_dsql_sql_info(stmt, /*stmtInfo.length,*/ stmtInfo, INFO_SIZE);
        int pos = 0;
        int length;
        int type;
        while ((type = buffer[pos++]) != ISCConstants.isc_info_end) {
            length = isc_vax_integer(buffer, pos, 2);
            pos += 2;
            switch (type) {
            case ISCConstants.isc_info_sql_records:
                int l;
                int t;
                while ((t = buffer[pos++]) != ISCConstants.isc_info_end) {
                    l = isc_vax_integer(buffer, pos, 2);
                    pos += 2;
                    switch (t) {
                    case ISCConstants.isc_info_req_insert_count:
                        stmt.setInsertCount(isc_vax_integer(buffer, pos, l));
                        break;
                    case ISCConstants.isc_info_req_update_count:
                        stmt.setUpdateCount(isc_vax_integer(buffer, pos, l));
                        break;
                    case ISCConstants.isc_info_req_delete_count:
                        stmt.setDeleteCount(isc_vax_integer(buffer, pos, l));
                        break;
                    case ISCConstants.isc_info_req_select_count:
                        stmt.setSelectCount(isc_vax_integer(buffer, pos, l));
                        break;
                    default:
                        break;
                    }
                    pos += l;
                }
                break;
            case ISCConstants.isc_info_sql_stmt_type:
                stmt.setStatementType(isc_vax_integer(buffer, pos, length));
                pos += length;
                break;
            default:
                pos += length;
                break;
            }
        }
    }

    public int isc_vax_integer(byte[] buffer, int pos, int length) {
        int value;
        int shift;

        value = shift = 0;

        int i = pos;
        while (--length >= 0) {
            value += (buffer[i++] & 0xff) << shift;
            shift += 8;
        }
        return value;
    }


    //-----------------------------------------------
    //Blob methods
    //-----------------------------------------------

    public void isc_create_blob2(isc_db_handle db_handle,
                        isc_tr_handle tr_handle,
                        isc_blob_handle blob_handle, //contains blob_id
                        Clumplet bpb) throws GDSException {
        openOrCreateBlob(db_handle, tr_handle, blob_handle, bpb, (bpb == null)? op_create_blob: op_create_blob2);
        ((isc_blob_handle_impl)blob_handle).rbl_flagsAdd(ISCConstants.RBL_create);
    }

    public void isc_open_blob2(isc_db_handle db_handle,
                        isc_tr_handle tr_handle,
                        isc_blob_handle blob_handle, //contains blob_id
                        Clumplet bpb) throws GDSException {
        openOrCreateBlob(db_handle, tr_handle, blob_handle, bpb, (bpb == null)? op_open_blob: op_open_blob2);
    }

    private final void openOrCreateBlob(isc_db_handle db_handle,
                        isc_tr_handle tr_handle,
                        isc_blob_handle blob_handle, //contains blob_id
                        Clumplet bpb,
                        int op) throws GDSException {
        boolean debug = log != null && log.isDebugEnabled();
        isc_db_handle_impl db = (isc_db_handle_impl) db_handle;
        isc_tr_handle_impl tr = (isc_tr_handle_impl) tr_handle;
        isc_blob_handle_impl blob = (isc_blob_handle_impl) blob_handle;

        if (db == null) {
            throw new GDSException(ISCConstants.isc_bad_db_handle);
        }
        if (tr == null) {
            throw new GDSException(ISCConstants.isc_bad_trans_handle);
        }
        if (blob == null) {
            throw new GDSException(ISCConstants.isc_bad_segstr_handle);
        }
        synchronized (db) {
            try {

                if (debug) {
                    log.debug((bpb == null)? "op_open/create_blob ": "op_open/create_blob2 ");
                    log.debug("op: " + op);
                }
                db.out.writeInt(op);
                if (bpb != null) {
                    db.out.writeTyped(ISCConstants.isc_bpb_version1, (Xdrable)bpb);
                }
                db.out.writeInt(tr.getTransactionId()); //??really a short?
                if (debug) log.debug("sending blob_id: " + blob.getBlob_id());
                db.out.writeLong(blob.getBlob_id());
                db.out.flush();            

                if (debug) log.debug("sent");
                receiveResponse(db,-1);
                blob.setDb(db);
                blob.setTr(tr);
                blob.setRbl_id(db.getResp_object());
                blob.setBlob_id(db.getResp_blob_id());
                tr.addBlob(blob);
            }
            catch (IOException ioe) {
                throw new GDSException(ISCConstants.isc_net_read_err);
            }
        }
    }

    public byte[] isc_get_segment(isc_blob_handle blob_handle,
                                  int requested) throws GDSException {
        boolean debug = log != null && log.isDebugEnabled();
        isc_blob_handle_impl blob = (isc_blob_handle_impl) blob_handle;
        isc_db_handle_impl db = blob.getDb();
        if (db == null) {
            throw new GDSException(ISCConstants.isc_bad_db_handle);
        }
        isc_tr_handle_impl tr = blob.getTr();
        if (tr == null) {
            throw new GDSException(ISCConstants.isc_bad_trans_handle);
        }
        synchronized (db) {
            try {

                if (debug) log.debug("op_get_segment ");
                db.out.writeInt(op_get_segment);
                db.out.writeInt(blob.getRbl_id()); //short???
                if (debug) log.debug("trying to read bytes: " +((requested + 2 < Short.MAX_VALUE) ? requested+2: Short.MAX_VALUE));
                db.out.writeInt((requested + 2 < Short.MAX_VALUE) ? requested+2 : Short.MAX_VALUE);
                db.out.writeInt(0);//writeBuffer for put segment;
                db.out.flush();            
                if (debug) log.debug("sent");
                receiveResponse(db,-1);
                blob.rbl_flagsRemove(ISCConstants.RBL_segment);
                if (db.getResp_object() == 1) {
                    blob.rbl_flagsAdd(ISCConstants.RBL_segment);
                }
                else if (db.getResp_object() == 2) {
                    blob.rbl_flagsAdd(ISCConstants.RBL_eof_pending);
                }
                byte[] buffer = db.getResp_data();
                if (buffer.length == 0) {//previous segment was last, this has no data
                    return buffer;
                }
                int len = 0;
                int srcpos = 0;
                int destpos = 0;
                while (srcpos < buffer.length) {
                    len = isc_vax_integer(buffer, srcpos, 2);
                    srcpos += 2;
                    System.arraycopy(buffer, srcpos, buffer, destpos, len);
                    srcpos += len;
                    destpos += len;
                }
                byte[] result = new byte[destpos];
                System.arraycopy(buffer, 0, result, 0, destpos);
                return result;

            }
            catch (IOException ioe) {
                throw new GDSException(ISCConstants.isc_net_read_err);
            }
        }
    }


    public void isc_put_segment(isc_blob_handle blob_handle, byte[] buffer) throws GDSException {
        boolean debug = log != null && log.isDebugEnabled();
        isc_blob_handle_impl blob = (isc_blob_handle_impl) blob_handle;
        isc_db_handle_impl db = blob.getDb();
        if (db == null) {
            throw new GDSException(ISCConstants.isc_bad_db_handle);
        }
        isc_tr_handle_impl tr = blob.getTr();
        if (tr == null) {
            throw new GDSException(ISCConstants.isc_bad_trans_handle);
        }
        synchronized (db) {
            try {

                if (debug) log.debug("op_batch_segments ");
                db.out.writeInt(op_batch_segments);
                if (debug) log.debug("blob.rbl_id:  " + blob.getRbl_id());
                db.out.writeInt(blob.getRbl_id()); //short???
                if (debug) log.debug("buffer.length " + buffer.length);
                db.out.writeBlobBuffer(buffer);
                db.out.flush();            
                if (debug) log.debug("sent");
                receiveResponse(db,-1);
            }
            catch (IOException ioe) {
                throw new GDSException(ISCConstants.isc_net_read_err);
            }
        }
    }

    public void isc_close_blob(isc_blob_handle blob_handle) throws GDSException {
        isc_blob_handle_impl blob = (isc_blob_handle_impl) blob_handle;
        isc_db_handle_impl db = blob.getDb();
        if (db == null) {
            throw new GDSException(ISCConstants.isc_bad_db_handle);
        }
        isc_tr_handle_impl tr = blob.getTr();
        if (tr == null) {
            throw new GDSException(ISCConstants.isc_bad_trans_handle);
        }
        releaseObject(db, op_close_blob, blob.getRbl_id());
    tr.removeBlob(blob);
    }



    // Handle declaration methods
    public isc_db_handle get_new_isc_db_handle() {
        return new isc_db_handle_impl();
    }

    public isc_tr_handle get_new_isc_tr_handle() {
        return new isc_tr_handle_impl();
    }

    public isc_stmt_handle get_new_isc_stmt_handle() {
        return new isc_stmt_handle_impl();
    }

    public isc_blob_handle get_new_isc_blob_handle() {
        return new isc_blob_handle_impl();
    }

    public void connect(isc_db_handle_impl db,
                            String host, Integer port, String filename) throws GDSException {
        DbAttachInfo dbai = new DbAttachInfo(host, port, filename);
        connect(db, dbai);
    }

    private void connect(isc_db_handle_impl db,
                            DbAttachInfo dbai) throws GDSException {
        boolean debug = log != null && log.isDebugEnabled();
        try {
            try {
                db.socket = new Socket(dbai.getServer(), dbai.getPort());
                db.socket.setTcpNoDelay(true);
                if (debug) log.debug("Got socket");
            } catch (UnknownHostException ex2) {
                String message = "Cannot resolve host " + dbai.getServer();
                if (debug) log.error(message, ex2);
                throw new GDSException(ISCConstants.isc_arg_gds, ISCConstants.isc_network_error
                , dbai.getServer());
            }

            db.out = new XdrOutputStream(db.socket.getOutputStream());
            db.in = new XdrInputStream(db.socket.getInputStream());

            //Here we identify the user to the engine.  This may or may not be used 
            //as login info to a database.
            String user = System.getProperty("user.name");
            if (debug) log.debug("user.name: " + user);
            String host = InetAddress.getLocalHost().getHostName();

//            byte[] user_id = new byte[200];
            byte[] user_id = new byte[6+user.length()+host.length()];
            int n = 0;
            user_id[n++] = 1;   // CNCT_user
            user_id[n++] = (byte) user.length();
            System.arraycopy(user.getBytes(), 0, user_id, n, user.length());
            n += user.length();

            /*            String passwd = "masterkey";
            user_id[n++] = 2;   // CNCT_passwd
            user_id[n++] = (byte) passwd.length();
            System.arraycopy(passwd.getBytes(), 0, user_id, n, passwd.length());
            n += passwd.length();*/

            user_id[n++] = 4;     // CNCT_host
            user_id[n++] = (byte) host.length();
            System.arraycopy(host.getBytes(), 0, user_id, n, host.length());
            n += host.length();
            
            user_id[n++] = 6;     // CNCT_user_verification
            user_id[n++] = 0;

            if (debug) log.debug("op_connect ");
            db.out.writeInt(op_connect);
            db.out.writeInt(op_attach);
            db.out.writeInt(2);                    // CONNECT_VERSION2
            db.out.writeInt(1);                    // arch_generic
//            db.out.writeString(file_name);        // p_cnct_file
            db.out.writeString(dbai.getFileName());        // p_cnct_file
            db.out.writeInt(1);                   // p_cnct_count
            db.out.writeBuffer(user_id);       // p_cnct_user_id

            db.out.writeInt(10);                   // PROTOCOL_VERSION10
            db.out.writeInt(1);                    // arch_generic
            db.out.writeInt(2);                    // ptype_rpc
            db.out.writeInt(3);                    // ptype_batch_send
            db.out.writeInt(2);
            db.out.flush();            
            if (debug) log.debug("sent");

            if (debug) log.debug("op_accept ");
            if (nextOperation(db) == op_accept) {
                db.setProtocol(db.in.readInt());             // Protocol version number
                int arch = db.in.readInt();                  // Architecture for protocol
                int min = db.in.readInt();                   // Minimum type
                if (debug) log.debug("received");
            } else {
                disconnect(db);
                if (debug) log.debug("not received");
                throw new GDSException(ISCConstants.isc_connect_reject);
            }
        } catch (IOException ex) {
            if (debug) log.debug("IOException while trying to connect to db:", ex);
            throw new GDSException(ISCConstants.isc_arg_gds, ISCConstants.isc_network_error
            , dbai.getServer());
        }
    }

    private void disconnect(isc_db_handle_impl db) throws IOException {
        if (log!=null) log.debug("About to invalidate db handle");
        db.invalidate();
        if (log!=null) log.debug("successfully invalidated db handle");
    }

    private void receiveSqlResponse(isc_db_handle_impl db,
                                       XSQLDA xsqlda, isc_stmt_handle_impl stmt) throws GDSException {
        boolean debug = log != null && log.isDebugEnabled();
        try {
            if (debug) log.debug("op_sql_response ");
            int messages = db.in.readInt();
            if (debug) log.debug("received");
            if (messages > 0) {
                db.in.readSQLData(xsqlda.ioLength,stmt);
            }
        } catch (IOException ex) {
            if (debug) log.warn("IOException in receiveSQLResponse", ex);
            // ex.getMessage() makes little sense here, it will not be displayed
            // because error message for isc_net_read_err does not accept params
            throw new GDSException(ISCConstants.isc_arg_gds, ISCConstants.isc_net_read_err
            , ex.getMessage());
        }
    }
	 
    private void receiveResponse(isc_db_handle_impl db, int op) throws GDSException {
        boolean debug = log != null && log.isDebugEnabled();
        // when used directly
        try {
            if (op == -1)			  
                op = nextOperation(db);
            if (debug) log.debug("op_response ");
            if (op == op_response) {
                db.setResp_object(db.in.readInt());
                db.setResp_blob_id(db.in.readLong());
                db.setResp_data(db.in.readBuffer());
                if (debug) {
                    log.debug("op_response resp_object: " + db.getResp_object());
                    log.debug("op_response resp_blob_id: " + db.getResp_blob_id());
                    log.debug("op_response resp_data size: " + db.getResp_data().length);
                }
//              for (int i = 0; i < ((r.resp_data.length< 16) ? r.resp_data.length: 16) ; i++) {
//                  if (debug) log.debug("byte: " + r.resp_data[i]);
//              }
                readStatusVector(db);
                if (debug){
                    log.debug("received");
//                    checkAllRead(db.in);//DEBUG
                }
            } else {
                if (debug){
                    log.debug("not received: op is " + op);
//                    checkAllRead(db.in);
                }
            }
        } catch (IOException ex) {
           if (debug) log.warn("IOException in receiveResponse", ex);
            // ex.getMessage() makes little sense here, it will not be displayed
            // because error message for isc_net_read_err does not accept params
            throw new GDSException(ISCConstants.isc_arg_gds, ISCConstants.isc_net_read_err
            , ex.getMessage());
        }
    }

    private int nextOperation(isc_db_handle_impl db) throws IOException {
        boolean debug = log != null && log.isDebugEnabled();
        int op = 0;
        do {
            op = db.in.readInt();
            if (debug){
                if (op == op_dummy) {
                    log.debug("op_dummy received");
                }
            }
        } while (op == op_dummy);
        return op;
    }

    private void readStatusVector(isc_db_handle_impl db)
            throws GDSException {
        boolean debug = log != null && log.isDebugEnabled();
        try {
            GDSException head = null;
            GDSException tail = null;
            while (true) {
                int arg = db.in.readInt();
                switch (arg) {
                    case ISCConstants.isc_arg_gds: 
                        int er = db.in.readInt();
                        if (debug)log.debug("readStatusVector arg:isc_arg_gds int: " + er);
                        if (er != 0) {
                            GDSException td = new GDSException(arg, er);
                            if (head == null) {
                                head = td;
                                tail = td;
                            }
                            else {
                                tail.setNext(td);
                                tail = td;
                            }
                        }
                        break;
                    case ISCConstants.isc_arg_end:
                        if (head != null && !head.isWarning()) 
                            throw head;
                        else
                        if (head != null && head.isWarning()) 
                            db.addWarning(head);
                        
                        return;
                    case ISCConstants.isc_arg_interpreted:
                    case ISCConstants.isc_arg_string:
                        GDSException ts = new GDSException(arg, db.in.readString());
                        if (debug) log.debug("readStatusVector string: " + ts.getMessage());
                        if (head == null) {
                            head = ts;
                            tail = ts;
                        }
                        else {
                            tail.setNext(ts);
                            tail = ts;
                        }
                        break;
                    case ISCConstants.isc_arg_number:
                        {
                            int arg_value = db.in.readInt();
                            if (debug)log.debug("readStatusVector arg:isc_arg_number int: " + arg_value);
                            GDSException td = new GDSException(arg, arg_value);
                            if (head == null) {
                                head = td;
                                tail = td;
                            }
                            else {
                                tail.setNext(td);
                                tail = td;
                            }
                            break;
                        }
                    default:
                        int e = db.in.readInt();
                        if (debug)log.debug("readStatusVector arg: "+arg+" int: " + e);
                        if (e != 0) {
                            GDSException td = new GDSException(arg, e);
                            if (head == null) {
                                head = td;
                                tail = td;
                            }
                            else {
                                tail.setNext(td);
                                tail = td;
                            }
                        }
                        break;
                }
            }
        }
        catch (IOException ioe) {
            // ioe.getMessage() makes little sense here, it will not be displayed
            // because error message for isc_net_read_err does not accept params
            throw new GDSException(ISCConstants.isc_arg_gds, ISCConstants.isc_net_read_err
            , ioe.getMessage());
        }
    }

    public static void calculateBLR(XSQLDA xsqlda) throws GDSException {
        int blr_len = 0;
//        byte[] blr = null;

        if (xsqlda != null) {
            // Determine the BLR length

            blr_len = 8;
            int par_count = 0;
            for (int i = 0; i < xsqlda.sqld; i++) {
                int dtype = xsqlda.sqlvar[i].sqltype & ~1;
                if (dtype == ISCConstants.SQL_VARYING || dtype == ISCConstants.SQL_TEXT) {
                    blr_len += 3;
                } else if (dtype == ISCConstants.SQL_SHORT || dtype == ISCConstants.SQL_LONG ||
                            dtype == ISCConstants.SQL_INT64 ||
                            dtype == ISCConstants.SQL_QUAD ||
                            dtype == ISCConstants.SQL_BLOB || dtype == ISCConstants.SQL_ARRAY) {
                    blr_len += 2;
                } else {
                    blr_len++;
                }
                blr_len += 2;
                par_count += 2;
            }

            byte[] blr = new byte[blr_len];

            int n = 0;
            blr[n++] = 5;                   // blr_version5
            blr[n++] = 2;                   // blr_begin
            blr[n++] = 4;                   // blr_message
            blr[n++] = 0;

            blr[n++] = (byte) (par_count & 255);
            blr[n++] = (byte) (par_count >> 8);

            for (int i = 0; i < xsqlda.sqld; i++) {
                int dtype = xsqlda.sqlvar[i].sqltype & ~1;
                int len = xsqlda.sqlvar[i].sqllen;
                if (dtype == ISCConstants.SQL_VARYING) {
                    blr[n++] = 37;              // blr_varying
                    blr[n++] = (byte) (len & 255);
                    blr[n++] = (byte) (len >> 8);
                } else if (dtype == ISCConstants.SQL_TEXT) {
                    blr[n++] = 14;              // blr_text
                    blr[n++] = (byte) (len & 255);
                    blr[n++] = (byte) (len >> 8);
                } else if (dtype == ISCConstants.SQL_DOUBLE) {
                    blr[n++] = 27;              // blr_double
                } else if (dtype == ISCConstants.SQL_FLOAT) {
                    blr[n++] = 10;              // blr_float
                } else if (dtype == ISCConstants.SQL_D_FLOAT) {
                    blr[n++] = 11;              // blr_d_float
                } else if (dtype == ISCConstants.SQL_TYPE_DATE) {
                    blr[n++] = 12;              // blr_sql_date
                } else if (dtype == ISCConstants.SQL_TYPE_TIME) {
                    blr[n++] = 13;              // blr_sql_time
                } else if (dtype == ISCConstants.SQL_TIMESTAMP) {
                    blr[n++] = 35;              // blr_timestamp
                } else if (dtype == ISCConstants.SQL_BLOB) {
                    blr[n++] = 9;               // blr_quad
                    blr[n++] = 0;
                } else if (dtype == ISCConstants.SQL_ARRAY) {
                    blr[n++] = 9;               // blr_quad
                    blr[n++] = 0;
                } else if (dtype == ISCConstants.SQL_LONG) {
                    blr[n++] = 8;               // blr_long
                    blr[n++] = (byte) xsqlda.sqlvar[i].sqlscale;
                } else if (dtype == ISCConstants.SQL_SHORT) {
                    blr[n++] = 7;               // blr_short
                    blr[n++] = (byte) xsqlda.sqlvar[i].sqlscale;
                } else if (dtype == ISCConstants.SQL_INT64) {
                    blr[n++] = 16;              // blr_int64
                    blr[n++] = (byte) xsqlda.sqlvar[i].sqlscale;
                } else if (dtype == ISCConstants.SQL_QUAD) {
                    blr[n++] = 9;               // blr_quad
                    blr[n++] = (byte) xsqlda.sqlvar[i].sqlscale;
                } else {
//                    return error_dsql_804 (gds__dsql_sqlda_value_err);
                }

                blr[n++] = 7;               // blr_short
                blr[n++] = 0;

            }

            blr[n++] = (byte) 255;          // blr_end
            blr[n++] = 76;                  // blr_eoc
            // save
            xsqlda.blr = blr;
        }
    }

    private XSQLDA parseSqlInfo(isc_stmt_handle stmt_handle,
                                byte[] info,
                                byte[] items) throws GDSException {
                
        boolean debug = log != null && log.isDebugEnabled();
        if (debug) log.debug("parseSqlInfo started");
        
        XSQLDA xsqlda = new XSQLDA();
        int lastindex = 0;
        while ((lastindex = parseTruncSqlInfo(info, xsqlda, lastindex)) > 0) {
            lastindex--;               // Is this OK ?
            byte[] new_items = new byte[4 + items.length];
            new_items[0] = ISCConstants.isc_info_sql_sqlda_start;
            new_items[1] = 2;
            new_items[2] = (byte) (lastindex & 255);
            new_items[3] = (byte) (lastindex >> 8);
            System.arraycopy(items, 0, new_items, 4, items.length);
            info = isc_dsql_sql_info(stmt_handle, /* new_items.length, */
                                     new_items, info.length);
        }
        if (debug) log.debug("parseSqlInfo ended");
        calculateBLR(xsqlda);
        calculateIOLength(xsqlda);
        return xsqlda;
    }
    
    
    private int parseTruncSqlInfo(byte[] info,
                                  XSQLDA xsqlda,
                                  int lastindex) throws GDSException {
        boolean debug = log != null && log.isDebugEnabled();
        byte item;
        int index = 0;
        if (debug) log.debug("parseSqlInfo: first 2 bytes are " + isc_vax_integer(info, 0, 2) + " or: " + info[0] + ", " + info[1]);

        int i = 2;

        int len = isc_vax_integer(info, i, 2);
        i += 2;
        int n = isc_vax_integer(info, i, len);
        i += len;
        if (xsqlda.sqlvar == null) {
            xsqlda.sqld = xsqlda.sqln = n;
            xsqlda.sqlvar = new XSQLVAR[xsqlda.sqln];
        }
        if (debug) log.debug("xsqlda.sqln read as " + xsqlda.sqln);

        while (info[i] != ISCConstants.isc_info_end) {
            while ((item = info[i++]) != ISCConstants.isc_info_sql_describe_end) {
                switch (item) {
                    case ISCConstants.isc_info_sql_sqlda_seq:
                        len = isc_vax_integer(info, i, 2);
                        i += 2;
                        index = isc_vax_integer(info, i, len);
                        i += len;
                        xsqlda.sqlvar[index - 1] = new XSQLVAR();
                        if (debug) log.debug("new xsqlvar " + (index - 1));
                        break;
                    case ISCConstants.isc_info_sql_type:
                        len = isc_vax_integer(info, i, 2);
                        i += 2;
                        xsqlda.sqlvar[index - 1].sqltype = isc_vax_integer (info, i, len);
                        i += len;
                        if (debug) log.debug("isc_info_sql_type " + xsqlda.sqlvar[index - 1].sqltype);
                        break;
                    case ISCConstants.isc_info_sql_sub_type:
                        len = isc_vax_integer(info, i, 2);
                        i += 2;
                        xsqlda.sqlvar[index - 1].sqlsubtype = isc_vax_integer (info, i, len);
                        i += len;
                        if (debug) log.debug("isc_info_sql_sub_type " + xsqlda.sqlvar[index - 1].sqlsubtype);
                        break;
                    case ISCConstants.isc_info_sql_scale:
                        len = isc_vax_integer(info, i, 2);
                        i += 2;
                        xsqlda.sqlvar[index - 1].sqlscale = isc_vax_integer (info, i, len);
                        i += len;
                        if (debug) log.debug("isc_info_sql_scale " + xsqlda.sqlvar[index - 1].sqlscale);
                        break;
                    case ISCConstants.isc_info_sql_length:
                        len = isc_vax_integer(info, i, 2);
                        i += 2;
                        xsqlda.sqlvar[index - 1].sqllen = isc_vax_integer (info, i, len);
                        i += len;
                        if (debug) log.debug("isc_info_sql_length " + xsqlda.sqlvar[index - 1].sqllen);
                        break;
                    case ISCConstants.isc_info_sql_field:
                        len = isc_vax_integer(info, i, 2);
                        i += 2;
                        xsqlda.sqlvar[index - 1].sqlname = new String(info, i, len);
                        i += len;
                        if (debug) log.debug("isc_info_sql_field " + xsqlda.sqlvar[index - 1].sqlname);
                        break;
                    case ISCConstants.isc_info_sql_relation:
                        len = isc_vax_integer(info, i, 2);
                        i += 2;
                        xsqlda.sqlvar[index - 1].relname = new String(info, i, len);
                        i += len;
                        if (debug) log.debug("isc_info_sql_relation " + xsqlda.sqlvar[index - 1].relname);
                        break;
                    case ISCConstants.isc_info_sql_owner:
                        len = isc_vax_integer(info, i, 2);
                        i += 2;
                        xsqlda.sqlvar[index - 1].ownname = new String(info, i, len);
                        i += len;
                        if (debug) log.debug("isc_info_sql_owner " + xsqlda.sqlvar[index - 1].ownname);
                        break;
                    case ISCConstants.isc_info_sql_alias:
                        len = isc_vax_integer(info, i, 2);
                        i += 2;
                        xsqlda.sqlvar[index - 1].aliasname = new String(info, i, len);
                        i += len;
                        if (debug) log.debug("isc_info_sql_alias " + xsqlda.sqlvar[index - 1].aliasname);
                        break;
                    case ISCConstants.isc_info_truncated:
                        if (debug) log.debug("isc_info_truncated ");
                        return lastindex;
                        //throw new GDSException(isc_dsql_sqlda_err);
                    default:
                        throw new GDSException(ISCConstants.isc_dsql_sqlda_err);
                }
            }
            lastindex = index;
        }
        return 0;
    }

    private void releaseObject(isc_db_handle_impl db, int op, int id) throws GDSException {
        synchronized (db) {
            try {
                db.out.writeInt(op);
                db.out.writeInt(id);
                db.out.flush();            
                receiveResponse(db,-1);
            }
            catch (IOException ioe) {
                throw new GDSException(ISCConstants.isc_net_read_err);
            }
        }
    }



    //inner classes

    protected static class DbAttachInfo {
        private String server = "localhost";
        private int port = 3050;
        private String fileName;

        public DbAttachInfo(String connectInfo) throws GDSException {

            if (connectInfo == null) {
                throw new GDSException("Connection string missing");
            }

            // allows standard syntax //host:port/....
            // and old fb syntax host/port:....
            connectInfo = connectInfo.trim();
            String node_name;
            char hostSepChar;
            char portSepChar;
            if (connectInfo.startsWith("//")){
                connectInfo = connectInfo.substring(2);
                hostSepChar = '/';
                portSepChar = ':';
            }
                else {
                hostSepChar = ':';
                portSepChar = '/';
            }

            int sep = connectInfo.indexOf(hostSepChar);
            if (sep == 0 || sep == connectInfo.length() - 1) {
                throw new GDSException("Bad connection string: '"+hostSepChar+"' at beginning or end of:" + connectInfo +  ISCConstants.isc_bad_db_format);
            }
            else if (sep > 0) {
                server = connectInfo.substring(0, sep);
                fileName = connectInfo.substring(sep + 1);
                int portSep = server.indexOf(portSepChar);
                if (portSep == 0 || portSep == server.length() - 1) {
                    throw new GDSException("Bad server string: '"+portSepChar+"' at beginning or end of: " + server +  ISCConstants.isc_bad_db_format);
                }
                else if (portSep > 0) {
                    port = Integer.parseInt(server.substring(portSep + 1));
                    server = server.substring(0, portSep);
                }
            }
            else if (sep == -1) 
            {
                fileName = connectInfo;
            } // end of if ()            

        }

        public DbAttachInfo(String server, Integer port, String fileName) throws GDSException
        {
            if (fileName == null || fileName.equals("")) 
            {
                throw new GDSException("null filename in DbAttachInfo");
            } // end of if ()
            if (server != null) 
            {
                this.server = server;
            } // end of if ()
            if (port != null) 
            {
                this.port = port.intValue();
            } // end of if ()
            this.fileName = fileName;
            if (fileName == null || fileName.equals("")) 
            {
                throw new GDSException("null filename in DbAttachInfo");
            } // end of if ()
            
        }

        public String getServer() {
            return server;
        }

        public int getPort() {
            return port;
        }

        public String getFileName() {
            return fileName;
        }
    }


    public static Clumplet newClumplet(int type, String content) {
        return new StringClumplet(type, content);
    }

    public static Clumplet newClumplet(int type){
        return new ClumpletImpl(type, new byte[] {});
    }


    public static Clumplet newClumplet(int type, int c){
        return new ClumpletImpl(type, new byte[] {(byte)(c>>24), (byte)(c>>16), (byte)(c>>8), (byte)c});
    }

    public static Clumplet newClumplet(int type, byte[] content) {
        return new ClumpletImpl(type, content);
    }

    public static Clumplet cloneClumplet(Clumplet c) {
        if (c == null) {
            return null;
        }
        return ((ClumpletImpl)c).cloneClumplet();
    }

}
