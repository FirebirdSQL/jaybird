/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is the Firebird Java GDS implementation.
 *
 * The Initial Developer of the Original Code is Alejandro Alberola.
 * Portions created by Alejandro Alberola are Copyright (C) 2001
 * Boix i Oltra, S.L. All Rights Reserved.
 *
 * Contributor(s):
 *
 * Alternatively, the contents of this file may be used under the
 * terms of the GNU Lesser General Public License Version 2.1 or later
 * (the "LGPL"), in which case the provisions of the LGPL are applicable
 * instead of those above.  If you wish to allow use of your
 * version of this file only under the terms of the LGPL and not to
 * allow others to use your version of this file under the MPL,
 * indicate your decision by deleting the provisions above and
 * replace them with the notice and other provisions required by
 * the LGPL.  If you do not delete the provisions above, a recipient
 * may use your version of this file under either the MPL or the
 * LGPL.
 */


package org.firebirdsql.jgds;

import org.firebirdsql.gds.*;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.io.*;
import java.net.*;
import java.util.*;

public class GDS_Impl implements GDS {

   private final Logger log = LoggerFactory.getLogger(getClass(),false);

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


    // Temporal response packet data
/*    private int resp_object;
    private long resp_blob_id;
    private byte[] resp_data;
*/

    static final int MAX_BUFFER_SIZE = 1024; //8192;//4096; //max size for response for ??
    static final int MAX_FETCH_ROWS = 200;     // Max number of rows in a fetch batch
    
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

        isc_db_handle_impl db = (isc_db_handle_impl) db_handle;

        if (db == null) {
            throw new GDSException(isc_bad_db_handle);
        }

        synchronized (db) {


            DbAttachInfo dbai = new DbAttachInfo(file_name);
            connect(db, dbai);
            try {
                if (log != null) log.debug("op_create ");
                db.out.writeInt(op_create);
                db.out.writeInt(0);           // packet->p_atch->p_atch_database
                db.out.writeString(dbai.getFileName());
                db.out.writeTyped(isc_dpb_version1, (Xdrable)c);
                //            db.out.writeBuffer(dpb, dpb_length);
                db.out.flush();            
                if (log != null) log.debug("sent");

                try {
                    Response r = receiveResponse(db);
                    db.setRdb_id(r.resp_object);
                } catch (GDSException g) {
                    disconnect(db);
                    throw g;
                }
            } catch (IOException ex) {
                throw new GDSException(isc_net_write_err);
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



    public void isc_attach_database(DbAttachInfo dbai,
                                   isc_db_handle db_handle,
                                   Clumplet dpb) throws GDSException  {

        isc_db_handle_impl db = (isc_db_handle_impl) db_handle;

        if (db == null) {
            throw new GDSException(isc_bad_db_handle);
        }

        synchronized (db) {
            connect(db, dbai);
            try {
                if (log != null) log.debug("op_attach ");
                db.out.writeInt(op_attach);
                db.out.writeInt(0);                // packet->p_atch->p_atch_database
                db.out.writeString(dbai.getFileName());
                db.out.writeTyped(isc_dpb_version1, (Xdrable)dpb);
                db.out.flush();            
                if (log != null) log.debug("sent");

                try {
                    Response r = receiveResponse(db);
                    db.setRdb_id(r.resp_object);
                }
                catch (GDSException ge) {
                    disconnect(db);
                    throw ge;
                }
            } catch (IOException ex) {
                throw new GDSException(isc_net_write_err);
            }
        }
    }


    public void isc_database_info(isc_db_handle handle,
                                 int item_length,
                                 byte[] items,
                                 int buffer_length,
                                 byte[] buffer) throws GDSException {
        throw new GDSException(isc_wish_list);
    }

    public void isc_detach_database(isc_db_handle db_handle) throws GDSException {
        isc_db_handle_impl db = (isc_db_handle_impl) db_handle;
        if (db == null) {
            throw new GDSException(isc_bad_db_handle);
        }

        synchronized (db) {
            if (db_handle.hasTransactions()) 
            {
                throw new GDSException(isc_open_trans);
            } // end of if ()
        

            try {
                if (log != null) log.debug("op_detach ");
                db.out.writeInt(op_detach);
                db.out.writeInt(db.getRdb_id());
                db.out.flush();            
                if (log != null) log.debug("sent");
                receiveResponse(db);
            } catch (IOException ex) {
                throw new GDSException(isc_network_error);
            }

            //db.rdb_transactions = new Vector();
        }

    }

    public void isc_drop_database(isc_db_handle db_handle) throws GDSException {
        isc_db_handle_impl db = (isc_db_handle_impl) db_handle;

        if (db == null) {
            throw new GDSException(isc_bad_db_handle);
        }

        synchronized (db) {

            try {
                if (log != null) log.debug("op_drop_database ");
                db.out.writeInt(op_drop_database);
                db.out.writeInt(db.getRdb_id());
                db.out.flush();            
                if (log != null) log.debug("sent");
                receiveResponse(db);
            } catch (IOException ex) {
                throw new GDSException(isc_network_error);
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
                                        Set tpb
                                /*int tpb_length,
                                byte[] tpb*/) throws GDSException {

        isc_tr_handle_impl tr = (isc_tr_handle_impl) tr_handle;
        isc_db_handle_impl db = (isc_db_handle_impl) db_handle;

        if (tr_handle == null) {
            throw new GDSException(isc_bad_trans_handle);
        }

        if (db_handle == null) {
            throw new GDSException(isc_bad_db_handle);
        }
        synchronized (db) {
            if (tr.getState() != isc_tr_handle.NOTRANSACTION) {
                throw new GDSException(isc_tra_state);
            }
            tr.setState(isc_tr_handle.TRANSACTIONSTARTING);

            try {
                if (log != null) log.debug("op_transaction ");
                db.out.writeInt(op_transaction);
                db.out.writeInt(db.getRdb_id());
                db.out.writeSet(isc_tpb_version3, tpb);
                //            db.out.writeBuffer(tpb, tpb_length);
                db.out.flush();            
                if (log != null) log.debug("sent");
                //out.flush();
                Response r = receiveResponse(db);
                tr.setTransactionId(r.resp_object);
            } catch (IOException ex) {
                throw new GDSException(isc_network_error);
            }

            //tr.rtr_rdb = db;
            tr.setDbHandle(db);
            tr.setState(isc_tr_handle.TRANSACTIONSTARTED);
            //db.rdb_transactions.addElement(tr);
        }//end synch on db

    }

    public void isc_commit_transaction(isc_tr_handle tr_handle) throws GDSException {
        if (tr_handle == null) {
            throw new GDSException(isc_bad_trans_handle);
        }

        isc_tr_handle_impl tr = (isc_tr_handle_impl) tr_handle;
        isc_db_handle_impl db = (isc_db_handle_impl)tr.getDbHandle();


        synchronized (db) {

            tr.setState(isc_tr_handle.TRANSACTIONCOMMITTING);

            try {
                if (log != null) log.debug("op_commit ");
                if (log != null) log.debug("tr.rtr_id: " + tr.getTransactionId());
                db.out.writeInt(op_commit);
                db.out.writeInt(tr.getTransactionId());
                db.out.flush();            
                if (log != null) log.debug("sent");
                receiveResponse(db);
            } catch (IOException ex) {
                throw new GDSException(isc_net_read_err);
            }

            tr.setState(isc_tr_handle.NOTRANSACTION);
            //tr.rtr_rdb = null;
            //db.rdb_transactions.removeElement(tr);
            tr.unsetDbHandle();
        }

    }

    public void isc_commit_retaining( isc_tr_handle tr_handle) throws GDSException {
        isc_tr_handle_impl tr = (isc_tr_handle_impl) tr_handle;
        if (tr == null) {
            throw new GDSException(isc_bad_trans_handle);
        }
        isc_db_handle_impl db = (isc_db_handle_impl)tr.getDbHandle();

        synchronized (db) {
            tr.setState(isc_tr_handle.TRANSACTIONCOMMITTING);

            try {
                if (log != null) log.debug("op_commit_retaining ");
                db.out.writeInt(op_commit_retaining);
                db.out.writeInt(tr.getTransactionId());
                db.out.flush();            
                if (log != null) log.debug("sent");
                receiveResponse(db);
            } catch (IOException ex) {
                throw new GDSException(isc_net_read_err);
            }
            tr.setState(isc_tr_handle.TRANSACTIONSTARTED);
        }

    }

    public void isc_prepare_transaction(isc_tr_handle tr_handle) throws GDSException {
        isc_tr_handle_impl tr = (isc_tr_handle_impl) tr_handle;
        if (tr == null) {
            throw new GDSException(isc_bad_trans_handle);
        }
        isc_db_handle_impl db = (isc_db_handle_impl)tr.getDbHandle();

        synchronized (db) {
            if (tr.getState() != isc_tr_handle.TRANSACTIONSTARTED) {
                throw new GDSException(isc_tra_state);
            }
            tr.setState(isc_tr_handle.TRANSACTIONPREPARING);
            tr.setState(isc_tr_handle.TRANSACTIONPREPARING);
            try {
                if (log != null) log.debug("op_prepare ");
                db.out.writeInt(op_prepare);
                db.out.writeInt(tr.getTransactionId());
                db.out.flush();            
                if (log != null) log.debug("sent");
                receiveResponse(db);
            } catch (IOException ex) {
                throw new GDSException(isc_net_read_err);
            }
            tr.setState(isc_tr_handle.TRANSACTIONPREPARED);
        }
    }

    public void isc_prepare_transaction2(isc_tr_handle tr_handle,
                                        byte[] bytes) throws GDSException {
        isc_tr_handle_impl tr = (isc_tr_handle_impl) tr_handle;
        if (tr == null) {
            throw new GDSException(isc_bad_trans_handle);
        }
        isc_db_handle_impl db = (isc_db_handle_impl)tr.getDbHandle();

        synchronized (db) {
            if (tr.getState() != isc_tr_handle.TRANSACTIONSTARTED) {
                throw new GDSException(isc_tra_state);
            }
            tr.setState(isc_tr_handle.TRANSACTIONPREPARING);
            try {
                if (log != null) log.debug("op_prepare2 ");
                db.out.writeInt(op_prepare2);
                db.out.writeInt(tr.getTransactionId());
                db.out.writeBuffer(bytes, bytes.length);
                db.out.flush();            
                if (log != null) log.debug("sent");
                receiveResponse(db);
            } catch (IOException ex) {
                throw new GDSException(isc_net_read_err);
            }

            tr.setState(isc_tr_handle.TRANSACTIONPREPARED);
        }
    }


    public void isc_rollback_transaction(     isc_tr_handle tr_handle) throws GDSException {

        isc_tr_handle_impl tr = (isc_tr_handle_impl) tr_handle;
        if (tr == null) {
            throw new GDSException(isc_bad_trans_handle);
        }
        isc_db_handle_impl db = (isc_db_handle_impl)tr.getDbHandle();

        synchronized (db) {

            if (tr.getState() != isc_tr_handle.TRANSACTIONSTARTED && tr.getState() != isc_tr_handle.TRANSACTIONPREPARED) {
                throw new GDSException(isc_tra_state);
            }
            tr.setState(isc_tr_handle.TRANSACTIONROLLINGBACK);

            try {
                if (log != null) log.debug("op_rollback ");
                db.out.writeInt(op_rollback);
                db.out.writeInt(tr.getTransactionId());
                db.out.flush();            
                if (log != null) log.debug("sent");
                receiveResponse(db);
            } catch (IOException ex) {
                throw new GDSException(isc_net_read_err);
            }

            tr.setState(isc_tr_handle.NOTRANSACTION);
            //tr.rtr_rdb = null;
            //db.rdb_transactions.removeElement(tr);
            tr.unsetDbHandle();
        }

    }

    // Dynamic SQL

    public void isc_dsql_allocate_statement(        isc_db_handle db_handle,
                                           isc_stmt_handle stmt_handle) throws GDSException {
        isc_db_handle_impl db = (isc_db_handle_impl) db_handle;
        isc_stmt_handle_impl stmt = (isc_stmt_handle_impl) stmt_handle;

        if (db_handle == null) {
            throw new GDSException(isc_bad_db_handle);
        }

        if (stmt_handle == null) {
            throw new GDSException(isc_bad_req_handle);
        }

        synchronized (db) {
            try {
                if (log != null) log.debug("op_allocate_statement ");
                db.out.writeInt(op_allocate_statement);
                db.out.writeInt(db.getRdb_id());
                db.out.flush();            
                if (log != null) log.debug("sent");
                Response r =receiveResponse(db);
                stmt.rsr_id = r.resp_object;
            } catch (IOException ex) {
                throw new GDSException(isc_net_read_err);
            }

            stmt.rsr_rdb = db;
            db.rdb_sql_requests.addElement(stmt);
            stmt.allRowsFetched = false;
        }

    }

    public void isc_dsql_alloc_statement2(      isc_db_handle db_handle,
                                         isc_stmt_handle stmt_handle) throws GDSException {
        throw new GDSException(isc_wish_list);
    }

    public XSQLDA isc_dsql_describe(isc_stmt_handle stmt_handle,
                                 int da_version) throws GDSException {

        byte[] describe_select_info = new byte[] { isc_info_sql_select,
                                                   isc_info_sql_describe_vars,
                                                   isc_info_sql_sqlda_seq,
                                                   isc_info_sql_type,
                                                   isc_info_sql_sub_type,
                                                   isc_info_sql_scale,
                                                   isc_info_sql_length,
                                                   isc_info_sql_field,
                                                   isc_info_sql_relation,
                                                   isc_info_sql_owner,
                                                   isc_info_sql_alias,
                                                   isc_info_sql_describe_end };

        byte[] buffer = isc_dsql_sql_info(stmt_handle,
                              describe_select_info.length, describe_select_info,
                              MAX_BUFFER_SIZE);
        return parseSqlInfo(stmt_handle, buffer, describe_select_info);
    }


    public XSQLDA isc_dsql_describe_bind(isc_stmt_handle stmt_handle,
                                         int da_version) throws GDSException {

        byte[] describe_bind_info = new byte[] { isc_info_sql_bind,
                                                 isc_info_sql_describe_vars,
                                                 isc_info_sql_sqlda_seq,
                                                 isc_info_sql_type,
                                                 isc_info_sql_sub_type,
                                                 isc_info_sql_scale,
                                                 isc_info_sql_length,
                                                 isc_info_sql_field,
                                                 isc_info_sql_relation,
                                                 isc_info_sql_owner,
                                                 isc_info_sql_alias,
                                                 isc_info_sql_describe_end };

        isc_stmt_handle_impl stmt = (isc_stmt_handle_impl) stmt_handle;
        
        byte[] buffer = isc_dsql_sql_info(stmt_handle,
                              describe_bind_info.length, describe_bind_info,
                              MAX_BUFFER_SIZE);
        
        stmt.in_sqlda = parseSqlInfo(stmt_handle, buffer, describe_bind_info);
        return stmt.in_sqlda;
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

        isc_tr_handle_impl tr = (isc_tr_handle_impl) tr_handle;
        isc_stmt_handle_impl stmt = (isc_stmt_handle_impl) stmt_handle;
        isc_db_handle_impl db = stmt.rsr_rdb;

        stmt.clearRows();
        
        // Test Handles needed here
        synchronized (db) {

            try {
                if (log != null) log.debug((out_xsqlda == null) ? "op_execute " : "op_execute2 ");

                if (!isSQLDataOK(in_xsqlda)) {
                    throw new GDSException(isc_dsql_sqlda_value_err);
                }
                
                db.out.writeInt((out_xsqlda == null) ? op_execute : op_execute2);
                db.out.writeInt(stmt.rsr_id);
                db.out.writeInt(tr.getTransactionId());

                writeBLR(db, in_xsqlda);
                db.out.writeInt(0);  //message number = in_message_type
                db.out.writeInt(((in_xsqlda == null) ? 0 : 1));  //stmt->rsr_bind_format

                if (in_xsqlda != null) {
                    writeSQLData(db, in_xsqlda);
                }

                if (out_xsqlda != null) {
                    writeBLR(db, out_xsqlda);
                    db.out.writeInt(0); //out_message_number = out_message_type
                }
                db.out.flush();            
                if (log != null) log.debug("sent");

                if (nextOperation(db) == op_sql_response) {
                    //this would be an Execute procedure
                    stmt.rows.add(receiveSqlResponse(db, out_xsqlda));
                    stmt.allRowsFetched = true;
                    stmt.isSingletonResult = true;
                }
                else 
                {
                    stmt.isSingletonResult = false;
                } // end of else

                receiveResponse(db);
            } catch (IOException ex) {
                throw new GDSException(isc_net_read_err);
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
            "NONE", dialect, xsqlda, null);
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
        isc_tr_handle_impl tr = (isc_tr_handle_impl) tr_handle;
        isc_db_handle_impl db = (isc_db_handle_impl) db_handle;

        // Test Handles

        synchronized (db) {
            try {
                if (!isSQLDataOK(in_xsqlda)) {
                    throw new GDSException(isc_dsql_sqlda_value_err);
                }
                

                if (in_xsqlda == null && out_xsqlda == null) {
                    if (log != null) log.debug("op_exec_immediate ");
                    db.out.writeInt(op_exec_immediate);
                } else {
                    if (log != null) log.debug("op_exec_immediate2 ");
                    db.out.writeInt(op_exec_immediate2);

                    writeBLR(db, in_xsqlda);
                    db.out.writeInt(0);
                    db.out.writeInt(((in_xsqlda == null) ? 0 : 1));

                    if (in_xsqlda != null) {
                        writeSQLData(db, in_xsqlda);
                    }

                    writeBLR(db, out_xsqlda);
                    db.out.writeInt(0);
                }

                db.out.writeInt(tr.getTransactionId());
                db.out.writeInt(0);
                db.out.writeInt(dialect);
                db.out.writeString(statement, encoding);
                db.out.writeString("");
                db.out.writeInt(0);
                db.out.flush();            

                if (log != null) log.debug("sent");

                if (nextOperation(db) == op_sql_response) {
                    receiveSqlResponse(db, out_xsqlda);
                }

                receiveResponse(db);
            } catch (IOException ex) {
                throw new GDSException(isc_net_read_err);
            }
        }
    }


    public Object[] isc_dsql_fetch(isc_stmt_handle stmt_handle,
                              int da_version,
                              XSQLDA xsqlda) throws GDSException {

        isc_stmt_handle_impl stmt = (isc_stmt_handle_impl) stmt_handle;
        isc_db_handle_impl db = stmt.rsr_rdb;

        if (stmt_handle == null) {
            throw new GDSException(isc_bad_req_handle);
        }

        if (xsqlda == null) {
            throw new GDSException(isc_dsql_sqlda_err);
        }

        if (!stmt.allRowsFetched && stmt.rows.size() == 0) {
            //Fetch next batch of rows
            synchronized (db) {
                try {
                    if (log != null) log.debug("op_fetch ");
                    db.out.writeInt(op_fetch);
                    db.out.writeInt(stmt.rsr_id);
                    writeBLR(db, xsqlda);
                    db.out.writeInt(0);              // p_sqldata_message_number
                    db.out.writeInt(MAX_FETCH_ROWS); // p_sqldata_messages
                    db.out.flush();            
                    if (log != null) log.debug("sent");

                    if (nextOperation(db) == op_fetch_response) {
                        int sqldata_status;
                        int sqldata_messages;
                        do {
                            int op = readOperation(db);
                            sqldata_status = db.in.readInt();
                            sqldata_messages = db.in.readInt();

                            if (sqldata_messages > 0 && sqldata_status == 0) {
                                stmt.rows.add(readSQLData(db, xsqlda));

                                /*                            XSQLDA batch_xsqlda = new XSQLDA(xsqlda.sqln);
                                                              for (int i = 0; i < xsqlda.sqln; i++) {
                                                              batch_xsqlda.sqlvar[i] =
                                                              new XSQLVAR(xsqlda.sqlvar[i].sqldata);
                                                              xsqlda.sqlvar[i].sqldata = null;
                                                              }

                                                              stmt.rows.addElement(batch_xsqlda);*/
                            }

                        } while (sqldata_messages > 0 && sqldata_status == 0);

                        if (sqldata_status == 100) {
                            if (log != null) log.debug("all rows successfully fetched");
                            stmt.allRowsFetched = true;
                        }

                    }
                    else {
                        receiveResponse(db);
                    }
                } catch (IOException ex) {
                    throw new GDSException(isc_net_read_err);
                }
            }
        }

        if (stmt.rows.size() > 0) {
            //Return next row from cache.
            Object[] row = (Object[])stmt.rows.remove(0);
            for (int i = 0; i < xsqlda.sqld; i++) {
                xsqlda.sqlvar[i].sqldata = row[i];
            }
            for (int i = xsqlda.sqld; i< xsqlda.sqln; i++) {
                //is this really necessary?
                xsqlda.sqlvar[i].sqldata = null;
            }
            return row;
/*            XSQLDA out_xsqlda = (XSQLDA) stmt.rows.elementAt(0);
            stmt.rows.removeElementAt(0);
            for (int i = 0; i < xsqlda.sqln; i++) {
                xsqlda.sqlvar[i].sqldata = out_xsqlda.sqlvar[i].sqldata;
            }*/
        }
        else {
            for (int i = 0; i< xsqlda.sqln; i++) {
                xsqlda.sqlvar[i].sqldata = null;
            }
            return null; //no rows fetched
        }

    }


    public void isc_dsql_free_statement(    isc_stmt_handle stmt_handle,
                                       int option) throws GDSException {
        isc_stmt_handle_impl stmt = (isc_stmt_handle_impl) stmt_handle;
        isc_db_handle_impl db = stmt.rsr_rdb;

        if (stmt_handle == null) {
            throw new GDSException(isc_bad_req_handle);
        }

        //Does not seem to be possible or necessary to close
        //an execute proceure statement.
        if (stmt.isSingletonResult && option == DSQL_close) 
        {
            return;        
        } // end of if ()
        

        synchronized (db) {
            try {
                if (log != null) log.debug("op_free_statement ");
                db.out.writeInt(op_free_statement);
                db.out.writeInt(stmt.rsr_id);
                db.out.writeInt(option);
                db.out.flush();            
                if (log != null) log.debug("sent");

                receiveResponse(db);
                if (option == DSQL_drop) {
                    stmt.in_sqlda = null;
                    stmt.out_sqlda = null;
                }
                stmt.clearRows();
                //            stmt.rows.clear();
                //            stmt.allRowsFetched = false;
            } catch (IOException ex) {
                throw new GDSException(isc_net_read_err);
            }
        }

    }

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
                                int dialect/*,
                                 xsqlda*/) throws GDSException {
        isc_tr_handle_impl tr = (isc_tr_handle_impl) tr_handle;
        isc_stmt_handle_impl stmt = (isc_stmt_handle_impl) stmt_handle;
        isc_db_handle_impl db = stmt.rsr_rdb;

        if (tr_handle == null) {
            throw new GDSException(isc_bad_trans_handle);
        }

        if (stmt_handle == null) {
            throw new GDSException(isc_bad_req_handle);
        }

        //reinitialize stmt SQLDA members.

        stmt.in_sqlda = null;
        stmt.out_sqlda = null;

        byte[] sql_prepare_info = new byte[] { isc_info_sql_select,
                                               isc_info_sql_describe_vars,
                                               isc_info_sql_sqlda_seq,
                                               isc_info_sql_type,
                                               isc_info_sql_sub_type,
                                               isc_info_sql_scale,
                                               isc_info_sql_length,
                                               isc_info_sql_field,
                                               isc_info_sql_relation,
                                               isc_info_sql_owner,
                                               isc_info_sql_alias,
                                               isc_info_sql_describe_end };

        synchronized (db) {
            try {
                if (log != null) log.debug("op_prepare_statement ");
                db.out.writeInt(op_prepare_statement);
                db.out.writeInt(tr.getTransactionId());
                db.out.writeInt(stmt.rsr_id);
                db.out.writeInt(dialect);
                db.out.writeString(statement, encoding);
                db.out.writeBuffer(sql_prepare_info, sql_prepare_info.length);
                db.out.writeInt(MAX_BUFFER_SIZE);
                db.out.flush();            

                if (log != null) log.debug("sent");
                Response r = receiveResponse(db);
                stmt.out_sqlda = parseSqlInfo(stmt_handle, r.resp_data, sql_prepare_info);
                return stmt.out_sqlda;
            } catch (IOException ex) {
                throw new GDSException(isc_net_read_err);
            }

            // RSR_blob ??????????
        }

    }


    public void isc_dsql_set_cursor_name(     isc_stmt_handle stmt_handle,
                                        String cursor_name,
                                        int type) throws GDSException {
        isc_stmt_handle_impl stmt = (isc_stmt_handle_impl) stmt_handle;
        isc_db_handle_impl db = stmt.rsr_rdb;

        if (stmt_handle == null) {
            throw new GDSException(isc_bad_req_handle);
        }

        synchronized (db) {
            try {
                if (log != null) log.debug("op_set_cursor ");
                db.out.writeInt(op_set_cursor);
                db.out.writeInt(stmt.rsr_id);

                byte[] buffer = new byte[cursor_name.length() + 1];
                System.arraycopy(cursor_name.getBytes(), 0,
                                 buffer, 0, cursor_name.length());
                buffer[cursor_name.length()] = (byte) 0;

                db.out.writeBuffer(buffer, buffer.length);
                db.out.writeInt(0);
                db.out.flush();            
                if (log != null) log.debug("sent");

                receiveResponse(db);
            } catch (IOException ex) {
                throw new GDSException(isc_net_read_err);
            }
        }


    }


    public byte[] isc_dsql_sql_info(isc_stmt_handle stmt_handle,
                                 int item_length,
                                 byte[] items,
                                 int buffer_length/*,
                                 byte[] buffer*/) throws GDSException {
        isc_stmt_handle_impl stmt = (isc_stmt_handle_impl) stmt_handle;
        isc_db_handle_impl db = stmt.rsr_rdb;

        synchronized (db) {
            try {
                if (log != null) log.debug("op_info_sql ");
                db.out.writeInt(op_info_sql);
                db.out.writeInt(stmt.rsr_id);
                db.out.writeInt(0);
                db.out.writeBuffer(items, item_length);
                db.out.writeInt(buffer_length);
                db.out.flush();            
                if (log != null) log.debug("sent");
                Response r = receiveResponse(db);
                return r.resp_data;
            } catch (IOException ex) {
                throw new GDSException(isc_net_read_err);
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
        ((isc_blob_handle_impl)blob_handle).rbl_flags |= RBL_create;
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
        isc_db_handle_impl db = (isc_db_handle_impl) db_handle;
        isc_tr_handle_impl tr = (isc_tr_handle_impl) tr_handle;
        isc_blob_handle_impl blob = (isc_blob_handle_impl) blob_handle;

        if (db == null) {
            throw new GDSException(isc_bad_db_handle);
        }
        if (tr == null) {
            throw new GDSException(isc_bad_trans_handle);
        }
        if (blob == null) {
            throw new GDSException(isc_bad_segstr_handle);
        }
        synchronized (db) {
            try {

                if (log != null) log.debug((bpb == null)? "op_open/create_blob ": "op_open/create_blob2 ");
                if (log != null) log.debug("op: " + op);
                db.out.writeInt(op);
                if (bpb != null) {
                    db.out.writeTyped(isc_bpb_version1, (Xdrable)bpb);
                }
                db.out.writeInt(tr.getTransactionId()); //??really a short?
                if (log != null) log.debug("sending blob_id: " + blob.blob_id);
                db.out.writeLong(blob.blob_id);
                db.out.flush();            

                if (log != null) log.debug("sent");
                Response r = receiveResponse(db);
                blob.db = db;
                blob.tr = tr;
                blob.rbl_id = r.resp_object;
                blob.blob_id = r.resp_blob_id;
                tr.addBlob(blob);
            }
            catch (IOException ioe) {
                throw new GDSException(isc_net_read_err);
            }
        }
    }

    public byte[] isc_get_segment(isc_blob_handle blob_handle,
                                  int requested) throws GDSException {
        isc_blob_handle_impl blob = (isc_blob_handle_impl) blob_handle;
        isc_db_handle_impl db = blob.db;
        if (db == null) {
            throw new GDSException(isc_bad_db_handle);
        }
        isc_tr_handle_impl tr = blob.tr;
        if (tr == null) {
            throw new GDSException(isc_bad_trans_handle);
        }
        synchronized (db) {
            try {

                if (log != null) log.debug("op_get_segment ");
                db.out.writeInt(op_get_segment);
                db.out.writeInt(blob.rbl_id); //short???
                // if (log != null) log.debug("trying to read bytes: " + ((blob.rbl_buffer_length< maxread)?blob.rbl_buffer_length:maxread) + 2);
                //db.out.writeInt(((blob.rbl_buffer_length< maxread)?blob.rbl_buffer_length:maxread) + 2); //Actually needs to be less than max short value
                if (log != null) log.debug("trying to read bytes: " +((requested + 2 < Short.MAX_VALUE) ? requested+2: Short.MAX_VALUE));
                db.out.writeInt((requested + 2 < Short.MAX_VALUE) ? requested+2 : Short.MAX_VALUE);
                db.out.writeInt(0);//writeBuffer for put segment;
                db.out.flush();            
                if (log != null) log.debug("sent");
                Response resp = receiveResponse(db);
                blob.rbl_flags &= ~RBL_segment;
                if (resp.resp_object == 1) {
                    blob.rbl_flags |= RBL_segment;
                }
                else if (resp.resp_object == 2) {
                    blob.rbl_flags |= RBL_eof_pending;
                }
                byte[] buffer = resp.resp_data;
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
                throw new GDSException(isc_net_read_err);
            }
        }
    }

    //    private final int byteToUchar(byte b) {
    //return (0x7f & b) + (((0x80 & b) == 0) ? 0: 0x80);
    //}

    public void isc_put_segment(isc_blob_handle blob_handle, byte[] buffer) throws GDSException {
        isc_blob_handle_impl blob = (isc_blob_handle_impl) blob_handle;
        isc_db_handle_impl db = blob.db;
        if (db == null) {
            throw new GDSException(isc_bad_db_handle);
        }
        isc_tr_handle_impl tr = blob.tr;
        if (tr == null) {
            throw new GDSException(isc_bad_trans_handle);
        }
        synchronized (db) {
            try {

                if (log != null) log.debug("op_batch_segments ");
                db.out.writeInt(op_batch_segments);
                if (log != null) log.debug("blob.rbl_id:  " + blob.rbl_id);
                db.out.writeInt(blob.rbl_id); //short???
                if (log != null) log.debug("buffer.length " + buffer.length);
                db.out.writeBlobBuffer(buffer);
                db.out.flush();            
                if (log != null) log.debug("sent");
                Response resp = receiveResponse(db);
            }
            catch (IOException ioe) {
                throw new GDSException(isc_net_read_err);
            }
        }
    }

    public void isc_close_blob(isc_blob_handle blob_handle) throws GDSException {
        isc_blob_handle_impl blob = (isc_blob_handle_impl) blob_handle;
        isc_db_handle_impl db = blob.db;
        if (db == null) {
            throw new GDSException(isc_bad_db_handle);
        }
        isc_tr_handle_impl tr = blob.tr;
        if (tr == null) {
            throw new GDSException(isc_bad_trans_handle);
        }
        releaseObject(db, op_close_blob, blob.rbl_id);
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


    private void connect(isc_db_handle_impl db,
                            DbAttachInfo dbai) throws GDSException {
        try {
            try {
                db.socket = new Socket(dbai.getServer(), dbai.getPort());
                if (log != null) log.debug("Got socket");
            } catch (UnknownHostException ex2) {
                String message = "Cannot resolve host " + dbai.getServer();
                if (log != null) log.error(message, ex2);
                throw new GDSException(isc_arg_gds, isc_network_error, dbai.getServer());
            }
            db.out = new XdrOutputStream(new BufferedOutputStream(db.socket.getOutputStream()));
            db.in = new XdrInputStream(new BufferedInputStream(db.socket.getInputStream()));

            //Here we identify the user to the engine.  This may or may not be used 
            //as login info to a database.
            String user = System.getProperty("user.name");
            if (log != null) log.debug("user.name: " + user);
            String host = InetAddress.getLocalHost().getHostName();

            byte[] user_id = new byte[200];
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

            if (log != null) log.debug("op_connect ");
            db.out.writeInt(op_connect);
            db.out.writeInt(op_attach);
            db.out.writeInt(2);                    // CONNECT_VERSION2
            db.out.writeInt(1);                    // arch_generic
//            db.out.writeString(file_name);        // p_cnct_file
            db.out.writeString(dbai.getFileName());        // p_cnct_file
            db.out.writeInt(1);                   // p_cnct_count
            db.out.writeBuffer(user_id, n);       // p_cnct_user_id
//            db.out.writeInt(0);                   // p_cnct_user_id

            db.out.writeInt(10);                   // PROTOCOL_VERSION10
            db.out.writeInt(1);                    // arch_generic
            db.out.writeInt(2);                    // ptype_rpc
            db.out.writeInt(3);                    // ptype_batch_send
            db.out.writeInt(2);
            db.out.flush();            
            if (log != null) log.debug("sent");

            if (log != null) log.debug("op_accept ");
            if (readOperation(db) == op_accept) {
                db.in.readInt();                   // Protocol version number
                db.in.readInt();                   // Architecture for protocol
                db.in.readInt();                   // Minimum type
                if (log != null) log.debug("received");
            } else {
                disconnect(db);
                if (log != null) log.debug("not received");
                throw new GDSException(isc_connect_reject);
            }
        } catch (IOException ex) {
            if (log != null) log.info("IOException while trying to connect to db:", ex);
            throw new GDSException(isc_arg_gds, isc_network_error, dbai.getServer());
        }
    }

    private void disconnect(isc_db_handle_impl db) throws IOException {
        db.socket.close();
    }

    private Object[] receiveSqlResponse(isc_db_handle_impl db,
                                       XSQLDA xsqlda) throws GDSException {
        try {
            if (log != null) log.debug("op_sql_response ");
            if (readOperation(db) == op_sql_response) {
                int messages = db.in.readInt();
                if (log != null) log.debug("received");
                if (messages > 0) {
                    return readSQLData(db, xsqlda);
                }
                else {
                    return null;
                }
            } else {
                if (log != null) log.debug("not received");
                throw new GDSException(isc_net_read_err);
            }
        } catch (IOException ex) {
            if (log != null) log.warn("IOException in receiveSQLResponse", ex);
            // ex.getMessage() makes little sense here, it will not be displayed
            // because error message for isc_net_read_err does not accept params
            throw new GDSException(isc_arg_gds, isc_net_read_err, ex.getMessage());
        }
    }

    private Response receiveResponse(isc_db_handle_impl db) throws GDSException {
        try {
            if (log != null) log.debug("op_response ");
            int op = readOperation(db);
            if (op == op_response) {
                Response r = new Response();
                r.resp_object = db.in.readInt();
                if (log != null) log.debug("op_response resp_object: " + r.resp_object);
                r.resp_blob_id = db.in.readLong();
                if (log != null) log.debug("op_response resp_blob_id: " + r.resp_blob_id);
                r.resp_data = db.in.readBuffer();
                if (log != null) log.debug("op_response resp_data size: " + r.resp_data.length);
//              for (int i = 0; i < ((r.resp_data.length< 16) ? r.resp_data.length: 16) ; i++) {
//                  if (log != null) log.debug("byte: " + r.resp_data[i]);
//              }
                readStatusVector(db);
                if (log != null) log.debug("received");
                if (log != null) checkAllRead(db.in);//DEBUG
                return r;
            } else {
                if (log != null) log.debug("not received: op is " + op);
                if (log != null) checkAllRead(db.in);
                return null;
//                throw new GDSException(isc_net_read_err);
            }
        } catch (IOException ex) {
           if (log != null) log.warn("IOException in receiveResponse", ex);
            // ex.getMessage() makes little sense here, it will not be displayed
            // because error message for isc_net_read_err does not accept params
            throw new GDSException(isc_arg_gds, isc_net_read_err, ex.getMessage());
        }
    }

    private int nextOperation(isc_db_handle_impl db) throws IOException {
        do {
            db.op = db.in.readInt();
            if (log != null){
                if (db.op == op_dummy) {
                    log.debug("op_dummy received");
                }
            }
        } while (db.op == op_dummy);
        return db.op;
    }

    private int readOperation(isc_db_handle_impl db) throws IOException {
        int op = (db.op >= 0) ? db.op : nextOperation(db);
        db.op = -1;
        return op;
    }

    private void readStatusVector(isc_db_handle_impl db)
            throws GDSException {
        try {
            GDSException head = null;
            GDSException tail = null;
            while (true) {
                int arg = db.in.readInt();
                switch (arg) {
                    case isc_arg_end:
                        if (head != null) {
                            throw head;
                        }
                        return;
                    case isc_arg_interpreted:
                    case isc_arg_string:
                        GDSException ts = new GDSException(arg, db.in.readString());
                        if (log != null) log.debug("readStatusVector string: " + ts.getMessage());
                        if (head == null) {
                            head = ts;
                            tail = ts;
                        }
                        else {
                            tail.setNext(ts);
                            tail = ts;
                        }
                        break;
                    case isc_arg_number:
                        {
                            int arg_value = db.in.readInt();
                            if (log != null)log.debug("readStatusVector int: " + arg_value);
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
                        if (log != null)log.debug("readStatusVector int: " + e);
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
            throw new GDSException(isc_arg_gds, isc_net_read_err, ioe.getMessage());
        }
    }

    private void writeBLR(isc_db_handle_impl db,
                             XSQLDA xsqlda) throws GDSException {
        int blr_len = 0;
        byte[] blr = null;

        if (xsqlda != null) {
            // Determine the BLR length

            blr_len = 8;
            int par_count = 0;
            for (int i = 0; i < xsqlda.sqld; i++) {
                int dtype = xsqlda.sqlvar[i].sqltype & ~1;
                if (dtype == SQL_VARYING || dtype == SQL_TEXT) {
                    blr_len += 3;
                } else if (dtype == SQL_SHORT || dtype == SQL_LONG ||
                            dtype == SQL_INT64 ||
                            dtype == SQL_QUAD ||
                            dtype == SQL_BLOB || dtype == SQL_ARRAY) {
                    blr_len += 2;
                } else {
                    blr_len++;
                }
                blr_len += 2;
                par_count += 2;
            }

            blr = new byte[blr_len];

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
                if (dtype == SQL_VARYING) {
                    blr[n++] = 37;              // blr_varying
                    blr[n++] = (byte) (len & 255);
                    blr[n++] = (byte) (len >> 8);
                } else if (dtype == SQL_TEXT) {
                    blr[n++] = 14;              // blr_text
                    blr[n++] = (byte) (len & 255);
                    blr[n++] = (byte) (len >> 8);
                } else if (dtype == SQL_DOUBLE) {
                    blr[n++] = 27;              // blr_double
                } else if (dtype == SQL_FLOAT) {
                    blr[n++] = 10;              // blr_float
                } else if (dtype == SQL_D_FLOAT) {
                    blr[n++] = 11;              // blr_d_float
                } else if (dtype == SQL_TYPE_DATE) {
                    blr[n++] = 12;              // blr_sql_date
                } else if (dtype == SQL_TYPE_TIME) {
                    blr[n++] = 13;              // blr_sql_time
                } else if (dtype == SQL_TIMESTAMP) {
                    blr[n++] = 35;              // blr_timestamp
                } else if (dtype == SQL_BLOB) {
                    blr[n++] = 9;               // blr_quad
                    blr[n++] = 0;
                } else if (dtype == SQL_ARRAY) {
                    blr[n++] = 9;               // blr_quad
                    blr[n++] = 0;
                } else if (dtype == SQL_LONG) {
                    blr[n++] = 8;               // blr_long
                    blr[n++] = (byte) xsqlda.sqlvar[i].sqlscale;
                } else if (dtype == SQL_SHORT) {
                    blr[n++] = 7;               // blr_short
                    blr[n++] = (byte) xsqlda.sqlvar[i].sqlscale;
                } else if (dtype == SQL_INT64) {
                    blr[n++] = 16;              // blr_int64
                    blr[n++] = (byte) xsqlda.sqlvar[i].sqlscale;
                } else if (dtype == SQL_QUAD) {
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
        }

        try {
            db.out.writeBuffer(blr, blr_len);
        } catch (IOException ex) {
            throw new GDSException(isc_net_write_err);
        }

    }

    private boolean isSQLDataOK(XSQLDA xsqlda) {
        if (xsqlda != null) {
            for (int i = 0; i < xsqlda.sqld; i++) {
                if ((xsqlda.sqlvar[i].sqlind != -1) &&
                    (xsqlda.sqlvar[i].sqldata == null)) {
                    return false;
                }
            }
        }
        return true;
    }

    private void writeSQLData(isc_db_handle_impl db,
                                 XSQLDA xsqlda) throws GDSException {
        // This only works if not (port->port_flags & PORT_symmetric)

        for (int i = 0; i < xsqlda.sqld; i++) {
            writeSQLDatum(db, xsqlda.sqlvar[i]);
        }

    }

    private void writeSQLDatum(isc_db_handle_impl db,
                                  XSQLVAR xsqlvar) throws GDSException {
        byte[] buffer;
        if (log != null) {
            if (db.out == null) 
            {
                log.debug("db.out null in writeSQLDatum");
            }
            if (xsqlvar.sqldata == null) 
            {
                log.debug("sqldata null in writeSQLDatum: " + xsqlvar);
            }
        }
        fixNull(xsqlvar);
        if (log != null) {
            if (xsqlvar.sqldata == null) 
            {
                log.debug("sqldata still null in writeSQLDatum: " + xsqlvar);
            }
        }

        try {
            Object sqldata = xsqlvar.sqldata;
            switch (xsqlvar.sqltype & ~1) {
                case SQL_TEXT:
                    //buffer =
                    //fillString((String) sqldata, xsqlvar.sqllen).getBytes();
                    if (((byte[])sqldata).length != xsqlvar.sqllen) {
                        throw new GDSException(isc_rec_size_err);
                    }
                    db.out.writeOpaque((byte[])sqldata, xsqlvar.sqllen);
                    //db.out.writeOpaque(buffer, buffer.length);
                    break;
                case SQL_VARYING:
                    if (((byte[])sqldata).length > xsqlvar.sqllen) {
                        throw new GDSException(isc_rec_size_err);
                    }
                    db.out.writeInt(((byte[])sqldata).length);
                    db.out.writeOpaque((byte[])sqldata, ((byte[])sqldata).length);

//                  int len = ((((String) sqldata).length()<xsqlvar.sqllen) 
//                  ? ((String) sqldata).length() : xsqlvar.sqllen);
//                  db.out.writeInt(len);
//                  buffer = ((String) sqldata).substring(0, len).getBytes();
//                  db.out.writeOpaque(buffer, buffer.length);
                    break;
                case SQL_SHORT:
                    db.out.writeInt(((Short) sqldata).shortValue());
                    break;
                case SQL_LONG:
                    db.out.writeInt(((Integer) sqldata).intValue());
                    break;
                case SQL_FLOAT:
                    db.out.writeFloat(((Float) sqldata).floatValue());
                    break;
                case SQL_DOUBLE:
                    db.out.writeDouble(((Double) sqldata).doubleValue());
                    break;
//            case SQL_D_FLOAT:
//                break;
                case SQL_TIMESTAMP:
                    db.out.writeInt(encodeDate((java.sql.Timestamp) sqldata));
                    db.out.writeInt(encodeTime((java.sql.Timestamp) sqldata));
                    break;
                case SQL_BLOB:
                    db.out.writeLong(((Long) sqldata).longValue());
                    break;
                case SQL_ARRAY:
                    db.out.writeLong(((Long) sqldata).longValue());
                    break;
                case SQL_QUAD:
                    db.out.writeLong(((Long) sqldata).longValue());
                    break;
                case SQL_TYPE_TIME:
                    db.out.writeInt(encodeTime((java.sql.Time) sqldata));
                    break;
                case SQL_TYPE_DATE:
                    db.out.writeInt(encodeDate((java.sql.Date) sqldata));
                    break;
                case SQL_INT64:
                    db.out.writeLong(((Long) sqldata).longValue());
                    break;
                default:
                    throw new GDSException("Unknown sql data type: " + xsqlvar.sqltype);
            }

            db.out.writeInt(xsqlvar.sqlind);

        } catch (IOException ex) {
            throw new GDSException(isc_net_write_err);
        }
    }

    private void fixNull(XSQLVAR xsqlvar) throws GDSException{
        if ((xsqlvar.sqlind == -1) && (xsqlvar.sqldata == null)) {
            switch (xsqlvar.sqltype & ~1) {
                case SQL_TEXT:
                    xsqlvar.sqldata = new byte[xsqlvar.sqllen];
                    break;
                case SQL_VARYING:
                    xsqlvar.sqldata = new byte[0];
                    break;
                case SQL_SHORT:
                    xsqlvar.sqldata = new Short((short)0);
                    break;
                case SQL_LONG:
                    xsqlvar.sqldata = new Integer(0);
                    break;
                case SQL_FLOAT:
                    xsqlvar.sqldata = new Float(0);
                    break;
                case SQL_DOUBLE:
                    xsqlvar.sqldata = new Double(0);
                    break;
//            case SQL_D_FLOAT:
//                break;
                case SQL_TIMESTAMP:
                    xsqlvar.sqldata = new java.sql.Timestamp(0);
                    break;
                case SQL_BLOB:
                case SQL_ARRAY:
                case SQL_QUAD:
                case SQL_INT64:
                    xsqlvar.sqldata = new Long(0);
                    break;
                case SQL_TYPE_TIME:
                    xsqlvar.sqldata = new java.sql.Time(0);
                    break;
                case SQL_TYPE_DATE:
                    xsqlvar.sqldata = new java.sql.Date(0);
                    break;
                default:
                    throw new GDSException("Unknown sql data type: " + xsqlvar.sqltype);
            }
        }
    }



    private String fillString(String s, int len) {
        if (s.length() < len) {
            StringBuffer sb = new StringBuffer();
            sb.ensureCapacity(len);
            sb.append(s);
            for (int i = 0; i < (len - s.length()); i++) {
                sb.append(' ');
            }
            s = sb.toString();
        }
        return s;
    }

    private int encodeTime(java.util.Date d) {
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(d);
        long millisInDay =
          c.get(Calendar.HOUR_OF_DAY)*60*60*1000
          +
          c.get(Calendar.MINUTE)*60*1000
          +
          c.get(Calendar.SECOND)*1000
          +
          c.get(Calendar.MILLISECOND)
          ;

        int iTime = (int) (millisInDay * 10);

        return iTime;
    }

    private int encodeDate(java.util.Date d) {
        int day, month, year;
        int c, ya;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(d);

        day = calendar.get(Calendar.DAY_OF_MONTH);
        month = calendar.get(Calendar.MONTH) + 1;
        year = calendar.get(Calendar.YEAR);

        if (month > 2) {
            month -= 3;
        } else {
            month += 9;
            year -= 1;
        }

        c = year / 100;
        ya = year - 100 * c;

        return ((146097 * c) / 4 +
                 (1461 * ya) / 4 +
                 (153 * month + 2) / 5 +
                 day + 1721119 - 2400001);
    }


    //Now returns results in Object[] and in xsqlda.data
    //Nulls are represented by null values in Object array,
    //but by sqlind = -1 in xsqlda.
    private Object[] readSQLData(isc_db_handle_impl db,
                                XSQLDA xsqlda) throws GDSException {
        // This only works if not (port->port_flags & PORT_symmetric)
        Object[] row = new Object[xsqlda.sqld];
        for (int i = 0; i < xsqlda.sqld; i++) {
            row[i] = readSQLDatum(db, xsqlda.sqlvar[i]);
        }
        return row;
    }

    private Object readSQLDatum(isc_db_handle_impl db,
                                 XSQLVAR xsqlvar) throws GDSException {
        try {
            switch (xsqlvar.sqltype & ~1) {
                case SQL_TEXT:
                    xsqlvar.sqldata = db.in.readOpaque(xsqlvar.sqllen);
                        //new String(db.in.readOpaque(xsqlvar.sqllen));
                    break;
                case SQL_VARYING:
                xsqlvar.sqldata = db.in.readOpaque(db.in.readInt());
                        //new String(db.in.readOpaque(db.in.readInt()));
                    break;
                case SQL_SHORT:
                    xsqlvar.sqldata = new Short((short) db.in.readInt());
                    break;
                case SQL_LONG:
                    xsqlvar.sqldata = new Integer(db.in.readInt());
                    break;
                case SQL_FLOAT:
                    xsqlvar.sqldata = new Float(db.in.readFloat());
                    break;
                case SQL_DOUBLE:
                    xsqlvar.sqldata = new Double(db.in.readDouble());
                    break;
//            case SQL_D_FLOAT:
//                break;
                case SQL_TIMESTAMP:
                    xsqlvar.sqldata = new java.sql.Timestamp(
                        decodeDate(db.in.readInt()).getTime() +
                        decodeTime(db.in.readInt()).getTime());
                    break;
                case SQL_BLOB:
                    xsqlvar.sqldata = new Long(db.in.readLong());
                    break;
                case SQL_ARRAY:
                    xsqlvar.sqldata = new Long(db.in.readLong());
                    break;
                case SQL_QUAD:
                    xsqlvar.sqldata = new Long(db.in.readLong());
                    break;
                case SQL_TYPE_TIME:
                    xsqlvar.sqldata = decodeTime(db.in.readInt());
                    break;
                case SQL_TYPE_DATE:
                    xsqlvar.sqldata = decodeDate(db.in.readInt());
                    break;
                case SQL_INT64:
                    xsqlvar.sqldata = new Long(db.in.readLong());
                    break;
            }

            xsqlvar.sqlind = db.in.readInt();

            if (xsqlvar.sqlind == 0) {
                return xsqlvar.sqldata;
            }
            else if (xsqlvar.sqlind == -1) {
                return null;
            }
            else {
                throw new GDSException("invalid sqlind value: " + xsqlvar.sqlind);
            }

        } catch (IOException ex) {
            throw new GDSException(isc_net_read_err);
        }
    }

    private java.sql.Time decodeTime(int sql_time) {
        return new java.sql.Time((sql_time / 10000) * 1000);
    }

    private java.sql.Date decodeDate(int sql_date) {
        int year, month, day, century;

        sql_date -= 1721119 - 2400001;
        century = (4 * sql_date - 1) / 146097;
        sql_date = 4 * sql_date - 1 - 146097 * century;
        day = sql_date / 4;

        sql_date = (4 * day + 3) / 1461;
        day = 4 * day + 3 - 1461 * sql_date;
        day = (day + 4) / 4;

        month = (5 * day - 3) / 153;
        day = 5 * day - 3 - 153 * month;
        day = (day + 5) / 5;

        year = 100 * century + sql_date;

        if (month < 10) {
            month += 3;
        } else {
            month -= 9;
            year += 1;
        }

        Calendar calendar = new GregorianCalendar(year, month - 1, day);
        return new java.sql.Date(calendar.getTime().getTime());
    }

    
    private XSQLDA parseSqlInfo(isc_stmt_handle stmt_handle,
                                byte[] info,
                                byte[] items) throws GDSException {
                
        if (log != null) log.debug("parseSqlInfo started");
        
        XSQLDA xsqlda = new XSQLDA();
        int lastindex = 0;
        while ((lastindex = parseTruncSqlInfo(info, xsqlda, lastindex)) > 0) {
            lastindex--;               // Is this OK ?
            byte[] new_items = new byte[4 + items.length];
            new_items[0] = isc_info_sql_sqlda_start;
            new_items[1] = 2;
            new_items[2] = (byte) (lastindex & 255);
            new_items[3] = (byte) (lastindex >> 8);
            System.arraycopy(items, 0, new_items, 4, items.length);
            info = isc_dsql_sql_info(stmt_handle, new_items.length,
                                     new_items, info.length);
        }
        if (log != null) log.debug("parseSqlInfo ended");

        return xsqlda;
    }
    
    
    private int parseTruncSqlInfo(byte[] info,
                                  XSQLDA xsqlda,
                                  int lastindex) throws GDSException {
        byte item;
        int index = 0;
        if (log != null) log.debug("parseSqlInfo: first 2 bytes are " + isc_vax_integer(info, 0, 2) + " or: " + info[0] + ", " + info[1]);

        int i = 2;

        int len = isc_vax_integer(info, i, 2);
        i += 2;
        int n = isc_vax_integer(info, i, len);
        i += len;
        if (xsqlda.sqlvar == null) {
            xsqlda.sqld = xsqlda.sqln = n;
            xsqlda.sqlvar = new XSQLVAR[xsqlda.sqln];
        }
        if (log != null) log.debug("xsqlda.sqln read as " + xsqlda.sqln);

        while (info[i] != isc_info_end) {
            while ((item = info[i++]) != isc_info_sql_describe_end) {
                switch (item) {
                    case isc_info_sql_sqlda_seq:
                        len = isc_vax_integer(info, i, 2);
                        i += 2;
                        index = isc_vax_integer(info, i, len);
                        i += len;
                        xsqlda.sqlvar[index - 1] = new XSQLVAR();
                        if (log != null) log.debug("new xsqlvar " + (index - 1));
                        break;
                    case isc_info_sql_type:
                        len = isc_vax_integer(info, i, 2);
                        i += 2;
                        xsqlda.sqlvar[index - 1].sqltype = isc_vax_integer (info, i, len);
                        i += len;
                        if (log != null) log.debug("isc_info_sql_type " + xsqlda.sqlvar[index - 1].sqltype);
                        break;
                    case isc_info_sql_sub_type:
                        len = isc_vax_integer(info, i, 2);
                        i += 2;
                        xsqlda.sqlvar[index - 1].sqlsubtype = isc_vax_integer (info, i, len);
                        i += len;
                        if (log != null) log.debug("isc_info_sql_sub_type " + xsqlda.sqlvar[index - 1].sqlsubtype);
                        break;
                    case isc_info_sql_scale:
                        len = isc_vax_integer(info, i, 2);
                        i += 2;
                        xsqlda.sqlvar[index - 1].sqlscale = isc_vax_integer (info, i, len);
                        i += len;
                        if (log != null) log.debug("isc_info_sql_scale " + xsqlda.sqlvar[index - 1].sqlscale);
                        break;
                    case isc_info_sql_length:
                        len = isc_vax_integer(info, i, 2);
                        i += 2;
                        xsqlda.sqlvar[index - 1].sqllen = isc_vax_integer (info, i, len);
                        i += len;
                        if (log != null) log.debug("isc_info_sql_length " + xsqlda.sqlvar[index - 1].sqllen);
                        break;
                    case isc_info_sql_field:
                        len = isc_vax_integer(info, i, 2);
                        i += 2;
                        xsqlda.sqlvar[index - 1].sqlname = new String(info, i, len);
                        i += len;
                        if (log != null) log.debug("isc_info_sql_field " + xsqlda.sqlvar[index - 1].sqlname);
                        break;
                    case isc_info_sql_relation:
                        len = isc_vax_integer(info, i, 2);
                        i += 2;
                        xsqlda.sqlvar[index - 1].relname = new String(info, i, len);
                        i += len;
                        if (log != null) log.debug("isc_info_sql_relation " + xsqlda.sqlvar[index - 1].relname);
                        break;
                    case isc_info_sql_owner:
                        len = isc_vax_integer(info, i, 2);
                        i += 2;
                        xsqlda.sqlvar[index - 1].ownname = new String(info, i, len);
                        i += len;
                        if (log != null) log.debug("isc_info_sql_owner " + xsqlda.sqlvar[index - 1].ownname);
                        break;
                    case isc_info_sql_alias:
                        len = isc_vax_integer(info, i, 2);
                        i += 2;
                        xsqlda.sqlvar[index - 1].aliasname = new String(info, i, len);
                        i += len;
                        if (log != null) log.debug("isc_info_sql_alias " + xsqlda.sqlvar[index - 1].aliasname);
                        break;
                    case isc_info_truncated:
                        if (log != null) log.debug("isc_info_truncated ");
                        return lastindex;
                        //throw new GDSException(isc_dsql_sqlda_err);
                    default:
                        throw new GDSException(isc_dsql_sqlda_err);
                }
            }
            lastindex = index;
        }
        return 0;
    }

    //DEBUG
    private void checkAllRead(InputStream in) throws GDSException {
        try {
            int i = in.available();
            if (i > 0) {
                if (log != null) log.debug("Extra bytes in packet read: " + i);
                byte[] b = new byte[i];
                in.read(b);
                for (int j = 0; j < ((b.length < 16) ? b.length : 16) ; j++) {					 
                    if (log != null) log.debug("byte: " + b[j]);
                }
            }
        }
        catch (IOException e) {
            throw new GDSException("IOException in checkAllRead: " + e);
        }
    }

    private void releaseObject(isc_db_handle_impl db, int op, int id) throws GDSException {
        synchronized (db) {
            try {
                db.out.writeInt(op);
                db.out.writeInt(id);
                db.out.flush();            
                receiveResponse(db);
            }
            catch (IOException ioe) {
                throw new GDSException(isc_net_read_err);
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
                throw new GDSException("Bad connection string: '"+hostSepChar+"' at beginning or end of:" + connectInfo +  isc_bad_db_format);
            }
            else if (sep > 0) {
                server = connectInfo.substring(0, sep);
                fileName = connectInfo.substring(sep + 1);
                int portSep = server.indexOf(portSepChar);
                if (portSep == 0 || portSep == server.length() - 1) {
                    throw new GDSException("Bad server string: '"+portSepChar+"' at beginning or end of: " + server +  isc_bad_db_format);
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
        return new ClumpletImpl(type, content.getBytes());
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
        return new ClumpletImpl((ClumpletImpl)c);
    }

    private static class Response {
        int resp_object;
        long resp_blob_id;
        byte[] resp_data;
    }

}
