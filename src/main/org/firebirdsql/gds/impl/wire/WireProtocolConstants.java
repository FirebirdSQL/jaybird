/*
 * Public Firebird Java API.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *    1. Redistributions of source code must retain the above copyright notice, 
 *       this list of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimer in the 
 *       documentation and/or other materials provided with the distribution. 
 *    3. The name of the author may not be used to endorse or promote products 
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED 
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO 
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.firebirdsql.gds.impl.wire;

/**
 * Constants for wire protocol operations and messages.
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public interface WireProtocolConstants {

    int INVALID_OBJECT = 0xFFFF;

    /* Operation (packet) types */
    int op_void = 0; /* Packet has been voided */
    int op_connect = 1; /* Connect to remote server */
    int op_exit = 2; /* Remote end has exitted */
    int op_accept = 3; /* Server accepts connection */
    int op_reject = 4; /* Server rejects connection */
    int op_protocol = 5; /* Protocol selection */
    int op_disconnect = 6; /* Connect is going away */
    int op_credit = 7; /* Grant (buffer) credits */
    int op_continuation = 8; /* Continuation packet */
    int op_response = 9; /* Generic response block */

    /* Page server operations */
    int op_open_file = 10; /* Open file for page service */
    int op_create_file = 11; /* Create file for page service */
    int op_close_file = 12; /* Close file for page service */
    int op_read_page = 13; /* optionally lock and read page */
    int op_write_page = 14; /* write page and optionally release lock */
    int op_lock = 15; /* seize lock */
    int op_convert_lock = 16; /* convert existing lock */
    int op_release_lock = 17; /* release existing lock */
    int op_blocking = 18; /* blocking lock message */

    /* Full context server operations */
    int op_attach = 19; /* Attach database */
    int op_create = 20; /* Create database */
    int op_detach = 21; /* Detach database */
    
    /* Request based operations */
    int op_compile = 22; 
    int op_start = 23;
    int op_start_and_send = 24;
    int op_send = 25;
    int op_receive = 26;
    int op_unwind = 27;
    int op_release = 28;
    
    /* Transaction operations */
    int op_transaction = 29; 
    int op_commit = 30;
    int op_rollback = 31;
    int op_prepare = 32;
    int op_reconnect = 33;
    
    /* Blob operations */
    int op_create_blob = 34; 
    int op_open_blob = 35;
    int op_get_segment = 36;
    int op_put_segment = 37;
    int op_cancel_blob = 38;
    int op_close_blob = 39;

    /* Information services */
    int op_info_database = 40; 
    int op_info_request = 41;
    int op_info_transaction = 42;
    int op_info_blob = 43;

    int op_batch_segments = 44; /* Put a bunch of blob segments */
    int op_mgr_set_affinity = 45; /* Establish server affinity */
    int op_mgr_clear_affinity = 46; /* Break server affinity */
    int op_mgr_report = 47; /* Report on server */
    int op_que_events = 48; /* Queue event notification request */
    int op_cancel_events = 49; /* Cancel event notification request */
    int op_commit_retaining = 50; /* Commit retaining (what else) */
    int op_prepare2 = 51; /* Message form of prepare */
    int op_event = 52; /* Completed event request (asynchronous) */
    int op_connect_request = 53; /* Request to establish connection */
    int op_aux_connect = 54; /* Establish auxiliary connection */
    int op_ddl = 55; /* DDL call */
    int op_open_blob2 = 56;
    int op_create_blob2 = 57;
    int op_get_slice = 58;
    int op_put_slice = 59;
    int op_slice = 60; /* Successful response to int op_get_slice */
    int op_seek_blob = 61; /* Blob seek operation */

    /* DSQL operations */
    int op_allocate_statement = 62; /* allocate a statement handle */
    int op_execute = 63; /* execute a prepared statement */
    int op_exec_immediate = 64; /* execute a statement */
    int op_fetch = 65; /* fetch a record */
    int op_fetch_response = 66; /* response for record fetch */
    int op_free_statement = 67; /* free a statement */
    int op_prepare_statement = 68; /* prepare a statement */
    int op_set_cursor = 69; /* set a cursor name */
    int op_info_sql = 70;
    int op_dummy = 71; /* dummy packet to detect loss of client */
    int op_response_piggyback = 72; /* response block for piggybacked messages */
    int op_start_and_receive = 73;
    int op_start_send_and_receive = 74;
    int op_exec_immediate2 = 75; /* execute an immediate statement with msgs */
    int op_execute2 = 76; /* execute a statement with msgs */
    int op_insert = 77;
    int op_sql_response = 78; /* response from execute; exec immed; insert */
    
    int op_transact = 79;
    int op_transact_response = 80;
    
    int op_drop_database = 81;
    
    int op_service_attach = 82;
    int op_service_detach = 83;
    int op_service_info = 84;
    int op_service_start = 85;
    
    int op_rollback_retaining = 86;
    
    /* Two following opcode are used in vulcan.
       No plans to implement them completely for a while, but to
       support protocol 11, where they are used, have them here. */
    int op_update_account_info = 87;
    int op_authenticate_user = 88;

    int op_partial = 89;   /* packet is not complete - delay processing */
    int op_trusted_auth = 90;

    int op_cancel = 91;

    int op_cont_auth = 92;

    int op_ping = 93;

    int op_accept_data = 94;   /* Server accepts connection and returns some data to client */

    int op_abort_aux_connection = 95;   /* Async operation - stop waiting for async connection to arrive */

    int op_crypt = 96;
    int op_crypt_key_callback = 97;
    int op_cond_accept = 98;
    
    /* Protocol version constants */
    
    int CONNECT_VERSION2 = 2;
    int CONNECT_VERSION3 = 3;
    
     /* Protocol 10 includes support for warnings and removes the requirement for
        encoding and decoding status codes */
    int PROTOCOL_VERSION10 = 10;
    
    /* Since protocol 11 we must be separated from Borland Interbase.
       Therefore always set highmost bit in protocol version to 1.
       For unsigned protocol version this does not break version's compare. */
    int FB_PROTOCOL_FLAG = 0x8000;
    int FB_PROTOCOL_MASK = ~FB_PROTOCOL_FLAG & 0xFFFF;
    
    /* Protocol 11 has support for user authentication related
       operations (op_update_account_info, op_authenticate_user and
       op_trusted_auth). When specific operation is not supported,
       we say "sorry". */
    int PROTOCOL_VERSION11 = (FB_PROTOCOL_FLAG | 11);
    
    /* Protocol 12 has support for asynchronous call op_cancel.
       Currently implemented asynchronously only for TCP/IP. */
    int PROTOCOL_VERSION12 = (FB_PROTOCOL_FLAG | 12);

    /* Protocol 13 has support for authentication plugins (op_cont_auth). */
    int PROTOCOL_VERSION13 = (FB_PROTOCOL_FLAG | 13);
    
    /* Architectures */
    int arch_generic = 1; /* Generic -- always use canonical forms */
    
    /* User identification data, if any, is of form:
       <type> <length> <data>
       where
       type   is a byte code
       length is an unsigned byte containing length of data
       data   is 'type' specific
     */
    int CNCT_user = 1; // User name
    int CNCT_passwd = 2;
    //int CNCT_ppo = 3; // Apollo person, project, organization. OBSOLETE.
    int CNCT_host = 4;
    int CNCT_group = 5; // Effective Unix group id
    int CNCT_user_verification = 6; // Attach/create using this connection will use user verification
    int CNCT_specific_data = 7; // Some data, needed for user verification on server
    int CNCT_plugin_name = 8; // Name of plugin, which generated that data
    int CNCT_login = 9; // Same data as isc_dpb_user_name
    int CNCT_plugin_list = 10; // List of plugins, available on client
    int CNCT_client_crypt = 11; // Client encyption level (DISABLED/ENABLED/REQUIRED)
    int WIRE_CRYPT_DISABLED = 0;
    int WIRE_CRYPT_ENABLED = 1;
    int WIRE_CRYPT_REQUIRED = 2;

    int TAG_KEY_TYPE = 0;
    int TAG_KEY_PLUGINS = 1;
    int TAG_KNOWN_PLUGINS = 2;
    
    // Protocol Types
    // p_acpt_type
    // int ptype_page = 1; // Page server protocol
    int ptype_rpc = 2; // Simple remote procedure call
    int ptype_batch_send = 3; // Batch sends, no asynchrony
    int ptype_out_of_band = 4; // Batch sends w/ out of band notification
    int ptype_lazy_send = 5; // Deferred packets delivery

    int P_REQ_async = 1;
}
