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

import java.io.*;
import java.net.*;
import java.util.*;

public class GDS_Impl implements GDS {
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
    static final int op_cancel_events		= 49;   /* Cancel event notification request */
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
    private int resp_object;
    private long resp_blob_id;
    private byte[] resp_data;
    

    public GDS_Impl() {
    }

    
    // Database functions
    
    public int isc_create_database(Object[] status,
                                   String file_name,
                                   isc_db_handle db_handle,
                                   int dpb_length,
                                   byte[] dpb) {

        status = initStatus(status);
        isc_db_handle_impl db = (isc_db_handle_impl) db_handle;
        
        if (db == null) {
            return error(status, isc_bad_db_handle);
        }
        
        if (file_name == null) {
            return error(status, isc_bad_db_format, "");
        }
        
        if (dpb_length > 0 && dpb == null) {
            return error(status, isc_bad_dpb_form);
        }
        
        file_name.trim();
        String node_name;
        int sep = file_name.indexOf(':');
        if (sep == 0 || sep == file_name.length() - 1) {
            return error(status, isc_bad_db_format, "");
        }
        if (sep < 0) {
            node_name = "localhost";
        } else {
            node_name = file_name.substring(0, sep);
            file_name = file_name.substring(sep + 1);
        }
        
        if (!connect(status, db, node_name, file_name)) {
            return ((Integer) status[1]).intValue();
        } else {
            try {
                System.out.print("op_create ");
                db.out.writeInt(op_create);
                db.out.writeInt(0);           // packet->p_atch->p_atch_database
                db.out.writeString(file_name);
                db.out.writeBuffer(dpb, dpb_length);
                System.out.println("sent");
        
                if (!receiveResponse(status, db)) {
                    disconnect(db);
                } else {
                    db.rdb_id = resp_object;
                }
            } catch (IOException ex) {
                return error(status, isc_net_write_err);
            }
        }
        
        return error(status);
    }
    
    
    public int isc_attach_database(Object[] status,
                                   String file_name,
                                   isc_db_handle db_handle,
                                   int dpb_length,
                                   byte[] dpb) {
        
        status = initStatus(status);
        isc_db_handle_impl db = (isc_db_handle_impl) db_handle;
        
        if (db == null) {
            return error(status, isc_bad_db_handle);
        }
        
        if (file_name == null) {
            return error(status, isc_bad_db_format, "");
        }
        
        if (dpb_length > 0 && dpb == null) {
            return error(status, isc_bad_dpb_form);
        }
        
        file_name.trim();
        String node_name;
        int sep = file_name.indexOf(':');
        if (sep == 0 || sep == file_name.length() - 1) {
            return error(status, isc_bad_db_format, "");
        }
        if (sep < 0) {
            node_name = "localhost";
        } else {
            node_name = file_name.substring(0, sep);
            file_name = file_name.substring(sep + 1);
        }
        
        if (!connect(status, db, node_name, file_name)) {
            return ((Integer) status[1]).intValue();
        } else {
            try {
                System.out.print("op_attach ");
                db.out.writeInt(op_attach);
                db.out.writeInt(0);                // packet->p_atch->p_atch_database
                db.out.writeString(file_name);
                db.out.writeBuffer(dpb, dpb_length);
                System.out.println("sent");
        
                if (!receiveResponse(status, db)) {
                    disconnect(db);
                } else {
                    db.rdb_id = resp_object;
                }
            } catch (IOException ex) {
                return error(status, isc_net_write_err);
            }
        }
        
        return error(status);
    }
    
    
    public int isc_database_info(Object[] status,
                                 isc_db_handle handle,
                                 int item_length,
                                 byte[] items,
                                 int buffer_length,
                                 byte[] buffer) {
        return SUCCESS;
    }
    
    public int isc_detach_database(Object[] status, isc_db_handle db_handle) {
        status = initStatus(status);
        isc_db_handle_impl db = (isc_db_handle_impl) db_handle;

        try {
            System.out.print("op_detach ");
            db.out.writeInt(op_detach);
            db.out.writeInt(db.rdb_id);
            System.out.println("sent");
            if (!receiveResponse(status, db)) {
                return error(status);
            }
        } catch (IOException ex) {
            return error(status, isc_network_error);
        }
        
        db.rdb_transactions = new Vector();
        
        return error(status);
    }
    
    public int isc_drop_database(Object[] status, isc_db_handle db_handle) {
        status = initStatus(status);
        isc_db_handle_impl db = (isc_db_handle_impl) db_handle;
        
        try {
            System.out.print("op_drop_database ");
            db.out.writeInt(op_drop_database);
            db.out.writeInt(db.rdb_id);
            System.out.println("sent");
            if (!receiveResponse(status, db)) {
                return error(status);
            }
        } catch (IOException ex) {
            return error(status, isc_network_error);
        }
        
        return error(status);
    }
    
    public byte[] isc_expand_dpb(byte[] dpb, int dpb_length,
                                 int param, Object[] params) {
        return dpb;
    }
    

    // Transaction functions
    
    public int isc_start_transaction(Object[] status,
                                        isc_tr_handle tr_handle,
                                        isc_db_handle db_handle,
                                        int tpb_length,
                                        byte[] tpb) {
        
        status = initStatus(status);
        isc_tr_handle_impl tr = (isc_tr_handle_impl) tr_handle;
        isc_db_handle_impl db = (isc_db_handle_impl) db_handle;
        
        if (tr_handle == null) {
            return error(status, isc_bad_trans_handle);
        }
        
        if (db_handle == null) {
            return error(status, isc_bad_db_handle);
        }
        
        try {
            System.out.print("op_transaction ");
            db.out.writeInt(op_transaction);
            db.out.writeInt(db.rdb_id);
            db.out.writeBuffer(tpb, tpb_length);
            System.out.println("sent");
            //out.flush();
            if (!receiveResponse(status, db)) {
                return error(status);
            }
        } catch (IOException ex) {
            return error(status, isc_network_error);
        }
        
        tr.rtr_id = resp_object;
        tr.rtr_rdb = db;
        db.rdb_transactions.addElement(tr);
        
        return error(status);
    }
    
    public int isc_commit_transaction(Object[] status,
                                      isc_tr_handle tr_handle) {
        status = initStatus(status);
        isc_tr_handle_impl tr = (isc_tr_handle_impl) tr_handle;
        isc_db_handle_impl db = tr.rtr_rdb;
        
        if (tr_handle == null) {
            return error(status, isc_bad_trans_handle);
        }
        
        try {
            System.out.print("op_commit ");
            db.out.writeInt(op_commit);
            db.out.writeInt(tr.rtr_id);
            System.out.println("sent");
            if (!receiveResponse(status, db)) {
                return error(status);
            }
        } catch (IOException ex) {
            return error(status, isc_net_read_err);
        }
        
        tr.rtr_rdb = null;
        db.rdb_transactions.removeElement(tr);
        
        return error(status);
    }
    
    public int isc_commit_retaining(Object[] status,
                                    isc_tr_handle tr_handle) {
        status = initStatus(status);
        isc_tr_handle_impl tr = (isc_tr_handle_impl) tr_handle;
        isc_db_handle_impl db = tr.rtr_rdb;

        if (tr_handle == null) {
            return error(status, isc_bad_trans_handle);
        }
        
        try {
            System.out.print("op_commit_retaining ");
            db.out.writeInt(op_commit_retaining);
            db.out.writeInt(tr.rtr_id);
            System.out.println("sent");
            if (!receiveResponse(status, db)) {
                return error(status);
            }
        } catch (IOException ex) {
            return error(status, isc_net_read_err);
        }
        
        return error(status);
    }
    
    public int isc_prepare_transaction(Object[] status,
                                       isc_tr_handle tr_handle) {
        return error(status, isc_wish_list);
    }
    
    public int isc_prepare_transaction2(Object[] status,
                                        isc_tr_handle tr_handle,
                                        int msg_length,
                                        String message) {
        return error(status, isc_wish_list);
    }
    
    public int isc_rollback_transaction(Object[] status,
                                        isc_tr_handle tr_handle) {
        status = initStatus(status);
        isc_tr_handle_impl tr = (isc_tr_handle_impl) tr_handle;
        isc_db_handle_impl db = tr.rtr_rdb;
        
        if (tr_handle == null) {
            return error(status, isc_bad_trans_handle);
        }
        
        try {
            System.out.print("op_rollback ");
            db.out.writeInt(op_rollback);
            db.out.writeInt(tr.rtr_id);
            System.out.println("sent");
            if (!receiveResponse(status, db)) {
                return error(status);
            }
        } catch (IOException ex) {
            return error(status, isc_net_read_err);
        }
        
        tr.rtr_rdb = null;
        db.rdb_transactions.removeElement(tr);
        
        return error(status);
    }
    
    // Dynamic SQL
    
    public int isc_dsql_allocate_statement(Object[] status,
                                           isc_db_handle db_handle,
                                           isc_stmt_handle stmt_handle) {
        status = initStatus(status);
        isc_db_handle_impl db = (isc_db_handle_impl) db_handle;
        isc_stmt_handle_impl stmt = (isc_stmt_handle_impl) stmt_handle;
        
        if (db_handle == null) {
            return error(status, isc_bad_db_handle);
        }
        
        if (stmt_handle == null) {
            return error(status, isc_bad_req_handle);
        }
        
        try {
            System.out.print("op_allocate_statement ");
            db.out.writeInt(op_allocate_statement);
            db.out.writeInt(db.rdb_id);
            System.out.println("sent");
            if (!receiveResponse(status, db)) {
                return error(status);
            }
        } catch (IOException ex) {
            return error(status, isc_net_read_err);
        }
        
        stmt.rsr_id = resp_object;
        stmt.rsr_rdb = db;
        db.rdb_sql_requests.addElement(stmt);
        
        return error(status);
    }
    
    public int isc_dsql_alloc_statement2(Object[] status,
                                         isc_db_handle db_handle,
                                         isc_stmt_handle stmt_handle) {
        return error(status, isc_wish_list);
    }
    
    public int isc_dsql_describe(Object[] status,
                                 isc_stmt_handle stmt_handle,
                                 int da_version,
                                 XSQLDA xsqlda) {

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

        byte[] buffer = new byte[32000];
        
        if (isc_dsql_sql_info(status, stmt_handle,
                              describe_select_info.length, describe_select_info,
                              buffer.length, buffer) == 0) {
            parseSqlInfo(status, buffer, xsqlda);
        }

        return error(status);
    }

    
    public int isc_dsql_describe_bind(Object[] status,
                                      isc_stmt_handle stmt_handle,
                                      int da_version,
                                      XSQLDA xsqlda) {

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

        byte[] buffer = new byte[32000];
        
        if (isc_dsql_sql_info(status, stmt_handle,
                              describe_bind_info.length, describe_bind_info,
                              buffer.length, buffer) == 0) {
            parseSqlInfo(status, buffer, xsqlda);
        }

        return error(status);
    }
    
    
    public int isc_dsql_execute(Object[] status,
                                isc_tr_handle tr_handle,
                                isc_stmt_handle stmt_handle,
                                int da_version,
                                XSQLDA xsqlda) {
                                    
        return isc_dsql_execute2(status, tr_handle, stmt_handle, da_version,
                                 xsqlda, null);
    }
    
    
    public int isc_dsql_execute2(Object[] status,
                                 isc_tr_handle tr_handle,
                                 isc_stmt_handle stmt_handle,
                                 int da_version,
                                 XSQLDA in_xsqlda,
                                 XSQLDA out_xsqlda) {

        status = initStatus(status);
        isc_tr_handle_impl tr = (isc_tr_handle_impl) tr_handle;
        isc_stmt_handle_impl stmt = (isc_stmt_handle_impl) stmt_handle;
        isc_db_handle_impl db = stmt.rsr_rdb;
        
        // Test Handles

        try {
            System.out.print(
                (out_xsqlda == null) ? "op_execute " : "op_execute2 ");
                
            db.out.writeInt((out_xsqlda == null) ? op_execute : op_execute2);
            db.out.writeInt(stmt.rsr_id);
            db.out.writeInt(tr.rtr_id);

            if (!writeBLR(status, db, in_xsqlda)) {
                return error(status);
            }
            db.out.writeInt(0);
            db.out.writeInt(((in_xsqlda == null) ? 0 : 1));
                
            if (in_xsqlda != null) {
                if (!writeSQLData(status, db, in_xsqlda)) {
                    return error(status);
                }
            }
            
            if (out_xsqlda != null) {
                if (!writeBLR(status, db, out_xsqlda)) {
                    return error(status);
                }
                db.out.writeInt(0);
            }
            System.out.println("sent");
            
            if (nextOperation(db) == op_sql_response) {
                if (!receiveSqlResponse(status, db, out_xsqlda)) {
                    return error(status);
                }
            }
 
            if (!receiveResponse(status, db)) {
                return error(status);
            }
        } catch (IOException ex) {
            return error(status, isc_net_read_err);
        }
                                           
        return error(status);
    }

    
    public int isc_dsql_execute_inmediate(Object[] status,
                                          isc_db_handle db_handle,
                                          isc_tr_handle tr_handle,
                                          String statement,
                                          int dialect,
                                          XSQLDA xsqlda) {
        return error(status, isc_wish_list);
    }
    
    public int isc_dsql_exec_inmed2(Object[] status,
                                    isc_db_handle db_handle,
                                    isc_tr_handle tr_handle,
                                    String statement,
                                    int dialect,
                                    XSQLDA in_xsqlda,
                                    XSQLDA out_xsqlda) {
        status = initStatus(status);
        isc_tr_handle_impl tr = (isc_tr_handle_impl) tr_handle;
        isc_db_handle_impl db = (isc_db_handle_impl) db_handle;
        
        // Test Handles

        try {
            if (in_xsqlda == null && out_xsqlda == null) {
                System.out.print("op_exec_immediate ");
                db.out.writeInt(op_exec_immediate);
            } else {
                System.out.print("op_exec_immediate2 ");
                db.out.writeInt(op_exec_immediate2);

                if (!writeBLR(status, db, in_xsqlda)) {
                    return error(status);
                }
                db.out.writeInt(0);
                db.out.writeInt(((in_xsqlda == null) ? 0 : 1));
                
                if (in_xsqlda != null) {
                    if (!writeSQLData(status, db, in_xsqlda)) {
                        return error(status);
                    }
                }
                
                if (!writeBLR(status, db, out_xsqlda)) {
                    return error(status);
                }
                db.out.writeInt(0);
            }
            
            db.out.writeInt(tr.rtr_id);
            db.out.writeInt(0);
            db.out.writeInt(dialect);
            db.out.writeString(statement);
            db.out.writeString("");
            db.out.writeInt(0);
            
            System.out.println("sent");
            
            if (nextOperation(db) == op_sql_response) {
                if (!receiveSqlResponse(status, db, out_xsqlda)) {
                    return error(status);
                }
            }
 
            if (!receiveResponse(status, db)) {
                return error(status);
            }
        } catch (IOException ex) {
            return error(status, isc_net_read_err);
        }
                                           
        return error(status);
    }
    
    
    public int isc_dsql_fetch(Object[] status,
                              isc_stmt_handle stmt_handle,
                              int da_version,
                              XSQLDA xsqlda) {
                                  
        status = initStatus(status);
        isc_stmt_handle_impl stmt = (isc_stmt_handle_impl) stmt_handle;
        isc_db_handle_impl db = stmt.rsr_rdb;
                                    
        if (stmt_handle == null) {
            return error(status, isc_bad_req_handle);
        }

        if (xsqlda == null) {
            return error(status, isc_dsql_sqlda_err);
        }

        if (stmt.rows.size() == 0) {
            try {
                System.out.print("op_fetch ");
                db.out.writeInt(op_fetch);
                db.out.writeInt(stmt.rsr_id);
                if (!writeBLR(status, db, xsqlda)) {
                    return error(status);
                }
                db.out.writeInt(0);     // p_sqldata_message_number
                db.out.writeInt(1);     // p_sqldata_messages
                System.out.println("sent");
            
                if (nextOperation(db) == op_fetch_response) {
                    int sqldata_status;
                    int sqldata_messages;
                    do {
                        int op = readOperation(db);
                        sqldata_status = db.in.readInt();
                        sqldata_messages = db.in.readInt();
                        
                        if (sqldata_messages > 0 && sqldata_status == 0) {
                            if (!readSQLData(status, db, xsqlda)) {
                                return error(status);
                            }
                            
                            XSQLDA batch_xsqlda = new XSQLDA(xsqlda.sqln);
                            for (int i = 0; i < xsqlda.sqln; i++) {
                                batch_xsqlda.sqlvar[i] =
                                    new XSQLVAR(xsqlda.sqlvar[i].sqldata);
                                xsqlda.sqlvar[i].sqldata = null;
                            }
                
                            stmt.rows.addElement(batch_xsqlda);
                        }

                    } while (sqldata_messages > 0 && sqldata_status == 0);
                    
                    if (sqldata_status == 100) {
                        return sqldata_status;
                    }
                    
                } else if (!receiveResponse(status, db)) {
                    return error(status);
                }
            } catch (IOException ex) {
                return error(status, isc_net_read_err);
            }
        }
        
        if (stmt.rows.size() > 0) {
            XSQLDA out_xsqlda = (XSQLDA) stmt.rows.elementAt(0);
            stmt.rows.removeElementAt(0);
            for (int i = 0; i < xsqlda.sqln; i++) {
                xsqlda.sqlvar[i].sqldata = out_xsqlda.sqlvar[i].sqldata;
            }
        }
                                  
        return error(status);
    }
    
    
    public int isc_dsql_free_statement(Object[] status,
                                       isc_stmt_handle stmt_handle,
                                       int option) {
        status = initStatus(status);
        isc_stmt_handle_impl stmt = (isc_stmt_handle_impl) stmt_handle;
        isc_db_handle_impl db = stmt.rsr_rdb;
                                    
        if (stmt_handle == null) {
            return error(status, isc_bad_req_handle);
        }
        
        try {
            System.out.print("op_free_statement ");
            db.out.writeInt(op_free_statement);
            db.out.writeInt(stmt.rsr_id);
            db.out.writeInt(option);
            System.out.println("sent");
 
            if (!receiveResponse(status, db)) {
                return error(status);
            }
        } catch (IOException ex) {
            return error(status, isc_net_read_err);
        }
        
        return error(status);
    }
    
    
    public int isc_dsql_prepare(Object[] status,
                                isc_tr_handle tr_handle,
                                isc_stmt_handle stmt_handle,
                                String statement,
                                int dialect,
                                XSQLDA xsqlda) {
        status = initStatus(status);
        isc_tr_handle_impl tr = (isc_tr_handle_impl) tr_handle;
        isc_stmt_handle_impl stmt = (isc_stmt_handle_impl) stmt_handle;
        isc_db_handle_impl db = stmt.rsr_rdb;
                                    
        if (tr_handle == null) {
            return error(status, isc_bad_trans_handle);
        }
        
        if (stmt_handle == null) {
            return error(status, isc_bad_req_handle);
        }
        
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
        
        byte[] buffer = new byte[32000];
        
        try {
            System.out.print("op_prepare_statement ");
            db.out.writeInt(op_prepare_statement);
            db.out.writeInt(tr.rtr_id);
            db.out.writeInt(stmt.rsr_id);
            db.out.writeInt(dialect);
            db.out.writeString(statement);
            db.out.writeBuffer(sql_prepare_info, sql_prepare_info.length);
            db.out.writeInt(buffer.length);
            
            System.out.println("sent");
            if (!receiveResponse(status, db)) {
                return error(status);
            } else {
                System.arraycopy(resp_data, 0, buffer, 0, resp_data.length);
                parseSqlInfo(status, buffer, xsqlda);
            }
        } catch (IOException ex) {
            return error(status, isc_net_read_err);
        }
        
        // RSR_blob ??????????
                                    
        return error(status);
    }
    
    
    public int isc_dsql_set_cursor_name(Object[] status,
                                        isc_stmt_handle stmt_handle,
                                        String cursor_name,
                                        int type) {
        status = initStatus(status);
        isc_stmt_handle_impl stmt = (isc_stmt_handle_impl) stmt_handle;
        isc_db_handle_impl db = stmt.rsr_rdb;
                                    
        if (stmt_handle == null) {
            return error(status, isc_bad_req_handle);
        }

        try {
            System.out.print("op_set_cursor ");
            db.out.writeInt(op_set_cursor);
            db.out.writeInt(stmt.rsr_id);
            
            byte[] buffer = new byte[cursor_name.length() + 1];
            System.arraycopy(cursor_name.getBytes(), 0,
                             buffer, 0, cursor_name.length());
            buffer[cursor_name.length()] = (byte) 0;
            
            db.out.writeBuffer(buffer, buffer.length);
            db.out.writeInt(0);
            System.out.println("sent");
            
            if (!receiveResponse(status, db)) {
                return error(status);
            }
        } catch (IOException ex) {
            return error(status, isc_net_read_err);
        }
        
        
        return error(status);
    }
    
    
    public int isc_dsql_sql_info(Object[] status,
                                 isc_stmt_handle stmt_handle,
                                 int item_length,
                                 byte[] items,
                                 int buffer_length,
                                 byte[] buffer) {
        status = initStatus(status);
        isc_stmt_handle_impl stmt = (isc_stmt_handle_impl) stmt_handle;
        isc_db_handle_impl db = stmt.rsr_rdb;

        try {
            System.out.print("op_info_sql ");
            db.out.writeInt(op_info_sql);
            db.out.writeInt(stmt.rsr_id);
            db.out.writeInt(0);
            db.out.writeBuffer(items, item_length);
            db.out.writeInt(buffer_length);
            System.out.println("sent");
            if (!receiveResponse(status, db)) {
                return error(status);
            }
        } catch (IOException ex) {
            return error(status, isc_net_read_err);
        }
        
        System.arraycopy(resp_data, 0, buffer, 0, resp_data.length);
        
        return error(status);
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
 
    private boolean connect(Object[] status,
                            isc_db_handle_impl db,
                            String node_name,
                            String file_name) {
        try {
            try {
                db.socket = new Socket(node_name, 3050);
            } catch (UnknownHostException ex2) {}
                
            db.out = new XdrOutputStream(db.socket.getOutputStream());
            db.in = new XdrInputStream(db.socket.getInputStream());
 
            String user = System.getProperty("user.name");
            String host = InetAddress.getLocalHost().getHostName();
    
            byte[] user_id = new byte[200];
            int n = 0;
            user_id[n++] = 1;   // CNCT_user
            user_id[n++] = (byte) user.length();
            System.arraycopy(user.getBytes(), 0, user_id, n, user.length());
            n += user.length();
            
//            String passwd = "";
//            user_id[n++] = 2;   // CNCT_passwd
//            user_id[n++] = (byte) passwd.length();
//            System.arraycopy(passwd.getBytes(), 0, user_id, n, passwd.length());
//            n += passwd.length();
            
            user_id[n++] = 4;     // CNCT_host
            user_id[n++] = (byte) host.length();    
            System.arraycopy(host.getBytes(), 0, user_id, n, host.length());
            n += host.length();
//            user_id[n++] = 6;     // CNCT_user_verification
//            user_id[n++] = 0;

            System.out.print("op_connect ");
            db.out.writeInt(op_connect);
            db.out.writeInt(op_attach);
            db.out.writeInt(2);                    // CONNECT_VERSION2
            db.out.writeInt(1);                    // arch_generic
            db.out.writeString(file_name);        // p_cnct_file
            db.out.writeInt(1);                   // p_cnct_count
            db.out.writeBuffer(user_id, n);       // p_cnct_user_id

            db.out.writeInt(10);                   // PROTOCOL_VERSION10
            db.out.writeInt(1);                    // arch_generic
            db.out.writeInt(2);                    // ptype_rpc
            db.out.writeInt(3);                    // ptype_batch_send
            db.out.writeInt(2);
            System.out.println("sent");
    
            System.out.print("op_accept ");
            if (readOperation(db) == op_accept) {
                db.in.readInt();                   // Protocol version number
                db.in.readInt();                   // Architecture for protocol
                db.in.readInt();                   // Minimum type
                System.out.println("received");
            } else {
                error(status, isc_connect_reject);
                disconnect(db);
                System.out.println("not received");
                return false;
            }
        } catch (IOException ex) {
            error(status, isc_network_error);
            return false;
        }
    
        return true;
    }
    
    private void disconnect(isc_db_handle_impl db) throws IOException {
        db.socket.close();
    }
    
    private boolean receiveSqlResponse(Object[] status,
                                       isc_db_handle_impl db,
                                       XSQLDA xsqlda) {
        try {
            System.out.print("op_sql_response ");
            if (readOperation(db) == op_sql_response) {
                int messages = db.in.readInt();
                if (messages > 0) {
                    if (!readSQLData(status, db, xsqlda)) {
                        return false;
                    }
                }
                System.out.println("received");
                return true;
            } else {
                System.out.println("not received");
                error(status, isc_net_read_err);
                return false;
            }
        } catch (IOException ex) {
            error(status, isc_net_read_err);
            ex.printStackTrace();
            return false;
        }
    }
    
    private boolean receiveResponse(Object[] status, isc_db_handle_impl db) {
        try {
            System.out.print("op_response ");
            if (readOperation(db) == op_response) {
                resp_object = db.in.readInt();
                resp_blob_id = db.in.readLong();
                resp_data = db.in.readBuffer();
                readStatusVector(status, db);
                System.out.println("received");
                return true;
            } else {
                System.out.println("not received");
                error(status, isc_net_read_err);
                return false;
            }
        } catch (IOException ex) {
            error(status, isc_net_read_err);
            ex.printStackTrace();
            return false;
        }
    }
    
    private int nextOperation(isc_db_handle_impl db) throws IOException {
        do {
            db.op = db.in.readInt();
            if (db.op == op_dummy) {
                System.out.println("op_dummy received");
            }
        } while (db.op == op_dummy);
        return db.op;
    }
    
    private int readOperation(isc_db_handle_impl db) throws IOException {
        int op = (db.op >= 0) ? db.op : nextOperation(db);
        db.op = -1;
        return op;
    }
    
    private void readStatusVector(Object[] status, isc_db_handle_impl db)
            throws IOException {
//        Object[] status = new Object[20];
        int i = 0;
        while (true) {
            int arg = db.in.readInt();
            status[i++] = new Integer(arg);
            switch (arg) {
                case isc_arg_end:
                    return;
                case isc_arg_interpreted:
                case isc_arg_string:
                    status[i++] = db.in.readString();
                    break;
                case isc_arg_number:
                default:
                    status[i++] = new Integer(db.in.readInt());
                    break;
            }
        }
    }
    
    private boolean writeBLR(Object[] status,
                             isc_db_handle_impl db,
                             XSQLDA xsqlda) {
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
//                    return error_dsql_804 (status, gds__dsql_sqlda_value_err);
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
            error(status, isc_net_write_err);
            return false;
        }
        
        return true;
    }
    
    private boolean writeSQLData(Object[] status,
                                 isc_db_handle_impl db,
                                 XSQLDA xsqlda) {
        // This only works if not (port->port_flags & PORT_symmetric)
        
        for (int i = 0; i < xsqlda.sqld; i++) {
            if (!writeSQLDatum(status, db, xsqlda.sqlvar[i])) {
                return false;
            }
        }
        
        return true;
    }
    
    private boolean writeSQLDatum(Object[] status,
                                  isc_db_handle_impl db,
                                  XSQLVAR xsqlvar) {
        byte[] buffer;
        
        try {
            Object sqldata = xsqlvar.sqldata;
            switch (xsqlvar.sqltype & ~1) {
                case SQL_TEXT:
                    buffer =
                        fillString((String) sqldata, xsqlvar.sqllen).getBytes();
                    db.out.writeOpaque(buffer, buffer.length);
                    break;
                case SQL_VARYING:
                    int len = Math.min(((String) sqldata).length(),
                                       xsqlvar.sqllen);
                    db.out.writeInt(len);
                    buffer = ((String) sqldata).substring(0, len).getBytes();
                    db.out.writeOpaque(buffer, buffer.length);
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
            }
            
            db.out.writeInt(xsqlvar.sqlind);
            
        } catch (IOException ex) {
            error(status, isc_net_write_err);
            return false;
        }
        
        return true;
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
    
    private int encodeTime(java.util.Date t) {
        return (int) ((t.getTime() % 86400) / 1000) * 10000;
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
    
    private boolean readSQLData(Object[] status,
                                isc_db_handle_impl db,
                                XSQLDA xsqlda) {
        // This only works if not (port->port_flags & PORT_symmetric)
        
        for (int i = 0; i < xsqlda.sqld; i++) {
            if (!readSQLDatum(status, db, xsqlda.sqlvar[i])) {
                return false;
            }
        }
        
        return true;
    }
    
    private boolean readSQLDatum(Object[] status,
                                 isc_db_handle_impl db,
                                 XSQLVAR xsqlvar) {
        try {
            switch (xsqlvar.sqltype & ~1) {
                case SQL_TEXT:
                    xsqlvar.sqldata =
                        new String(db.in.readOpaque(xsqlvar.sqllen));
                    break;
                case SQL_VARYING:
                    xsqlvar.sqldata =
                        new String(db.in.readOpaque(db.in.readInt()));
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
            
        } catch (IOException ex) {
            status[0] = new Integer(isc_arg_gds);
            status[1] = new Integer(isc_net_read_err);
            status[2] = new Integer(isc_arg_end);

            return false;
        }
        
        return true;
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

    
    private void parseSqlInfo(Object[] status, byte[] info, XSQLDA xsqlda) {
        byte item;
        int index = 0;
        
        int i = 2;

        int len = isc_vax_integer(info, i, 2);
        i += 2;
        xsqlda.sqld = xsqlda.sqln = isc_vax_integer(info, i, len);
        i += len;
        xsqlda.sqlvar = new XSQLVAR[xsqlda.sqln];
        
        while (info[i] != isc_info_end) {
            while ((item = info[i++]) != isc_info_sql_describe_end) {
                len = isc_vax_integer(info, i, 2);
                i += 2;
                switch (item) {
                    case isc_info_sql_sqlda_seq:
                        index = isc_vax_integer(info, i, len) - 1;
                        xsqlda.sqlvar[index] = new XSQLVAR();
                        break;
                    case isc_info_sql_type:
                        xsqlda.sqlvar[index].sqltype = isc_vax_integer (info, i, len);
                        break;
                    case isc_info_sql_sub_type:
                        xsqlda.sqlvar[index].sqlsubtype = isc_vax_integer (info, i, len);
                        break;
                    case isc_info_sql_scale:
                        xsqlda.sqlvar[index].sqlscale = isc_vax_integer (info, i, len);
                        break;
                    case isc_info_sql_length:
                        xsqlda.sqlvar[index].sqllen = isc_vax_integer (info, i, len);
                        break;
                    case isc_info_sql_field:
                        xsqlda.sqlvar[index].sqlname = new String(info, i, len);
                        break;
                    case isc_info_sql_relation:
                        xsqlda.sqlvar[index].relname = new String(info, i, len);
                        break;
                    case isc_info_sql_owner:
                        xsqlda.sqlvar[index].ownname = new String(info, i, len);
                        break;
                    case isc_info_sql_alias:
                        xsqlda.sqlvar[index].aliasname = new String(info, i, len);
                        break;
                    case isc_info_truncated:
                        error(status, isc_dsql_sqlda_err);
                        return;
                    default:
                        error(status, isc_dsql_sqlda_err);
                        return;
                }
                i += len;
            }
        }
    }
    
    // Error management
    
    private Object[] initStatus(Object[] status) {
        if (status == null) {
            status = new Object[20];
            error(status, SUCCESS);
        }
        return status;
    }
    
    private int error(Object[] status, int error) {
        status[0] = new Integer(isc_arg_gds);
        status[1] = new Integer(error);
        status[2] = new Integer(isc_arg_end);
        return error;
    }
    
    private int error(Object[] status, int error, String msg) {
        status[0] = new Integer(isc_arg_gds);
        status[1] = new Integer(error);
        status[2] = new Integer(isc_arg_string);
        status[3] = msg;
        status[4] = new Integer(isc_arg_end);
        return error;
    }
    
    private int error(Object[] status) {
        return ((Integer) status[1]).intValue();
    }
    
}
