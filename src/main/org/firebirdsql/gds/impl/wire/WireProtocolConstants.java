/*
 SPDX-FileCopyrightText: 2000-2024 Firebird development team and individual contributors
 SPDX-FileCopyrightText: Copyright 2001 Boix i Oltra, S.L.
 SPDX-FileContributor: Alejandro Alberola (Boix i Oltra, S.L.)
 SPDX-FileCopyrightText: Copyright 2005 Steven Jardine
 SPDX-FileCopyrightText: Copyright 2010 Roman Rokytskyy
 SPDX-FileCopyrightText: Copyright 2011-2024 Mark Rotteveel
 SPDX-FileCopyrightText: Copyright 2015 Hajime Nakagami
 SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
 SPDX-FileComment: The constants listed here were obtained from the Firebird sources, which are licensed under the IPL (InterBase Public License) and/or IDPL (Initial Developer Public License), both are variants of the Mozilla Public License version 1.1
*/
package org.firebirdsql.gds.impl.wire;

/**
 * Constants for wire protocol operations and messages.
 *
 * @author Mark Rotteveel
 */
@SuppressWarnings({ "unused", "java:S115", "java:S1214" })
public interface WireProtocolConstants {

    int INVALID_OBJECT = 0xFFFF;

    int op_void = 0;
    int op_connect = 1;
    int op_exit = 2;
    int op_accept = 3;
    int op_reject = 4;
    int op_protocol = 5;
    int op_disconnect = 6;
    int op_credit = 7;
    int op_continuation = 8;
    int op_response = 9;

    int op_open_file = 10;
    int op_create_file = 11;
    int op_close_file = 12;
    int op_read_page = 13;
    int op_write_page = 14;
    int op_lock = 15;
    int op_convert_lock = 16;
    int op_release_lock = 17;
    int op_blocking = 18;

    int op_attach = 19;
    int op_create = 20;
    int op_detach = 21;

    int op_compile = 22;
    int op_start = 23;
    int op_start_and_send = 24;
    int op_send = 25;
    int op_receive = 26;
    int op_unwind = 27;
    int op_release = 28;

    int op_transaction = 29;
    int op_commit = 30;
    int op_rollback = 31;
    int op_prepare = 32;
    int op_reconnect = 33;

    int op_create_blob = 34;
    int op_open_blob = 35;
    int op_get_segment = 36;
    int op_put_segment = 37;
    int op_cancel_blob = 38;
    int op_close_blob = 39;

    int op_info_database = 40;
    int op_info_request = 41;
    int op_info_transaction = 42;
    int op_info_blob = 43;

    int op_batch_segments = 44;
    int op_mgr_set_affinity = 45;
    int op_mgr_clear_affinity = 46;
    int op_mgr_report = 47;
    int op_que_events = 48;
    int op_cancel_events = 49;
    int op_commit_retaining = 50;
    int op_prepare2 = 51;
    int op_event = 52;
    int op_connect_request = 53;
    int op_aux_connect = 54;
    int op_ddl = 55;
    int op_open_blob2 = 56;
    int op_create_blob2 = 57;
    int op_get_slice = 58;
    int op_put_slice = 59;
    int op_slice = 60;
    int op_seek_blob = 61;

    int op_allocate_statement = 62;
    int op_execute = 63;
    int op_exec_immediate = 64;
    int op_fetch = 65;
    int op_fetch_response = 66;
    int op_free_statement = 67;
    int op_prepare_statement = 68;
    int op_set_cursor = 69;
    int op_info_sql = 70;
    int op_dummy = 71;
    int op_response_piggyback = 72;
    int op_start_and_receive = 73;
    int op_start_send_and_receive = 74;
    int op_exec_immediate2 = 75;
    int op_execute2 = 76;
    int op_insert = 77;
    int op_sql_response = 78;

    int op_transact = 79;
    int op_transact_response = 80;

    int op_drop_database = 81;

    int op_service_attach = 82;
    int op_service_detach = 83;
    int op_service_info = 84;
    int op_service_start = 85;

    int op_rollback_retaining = 86;

    int op_update_account_info = 87;
    int op_authenticate_user = 88;

    int op_partial = 89;
    int op_trusted_auth = 90;

    int op_cancel = 91;

    int op_cont_auth = 92;

    int op_ping = 93;

    int op_accept_data = 94;

    int op_abort_aux_connection = 95;

    int op_crypt = 96;
    int op_crypt_key_callback = 97;
    int op_cond_accept = 98;

    int op_batch_create = 99;
    int op_batch_msg = 100;
    int op_batch_exec = 101;
    int op_batch_rls = 102;
    int op_batch_cs = 103;
    int op_batch_regblob = 104;
    int op_batch_blob_stream = 105;
    int op_batch_set_bpb = 106;

    int op_repl_data = 107;
    int op_repl_req = 108;

    int op_batch_cancel = 109;
    int op_batch_sync = 110;
    int op_info_batch = 111;

    int op_fetch_scroll = 112;
    int op_info_cursor = 113;

    int CONNECT_VERSION2 = 2;
    int CONNECT_VERSION3 = 3;

    int PROTOCOL_VERSION10 = 10;

    int FB_PROTOCOL_FLAG = 0x8000;
    int FB_PROTOCOL_MASK = ~FB_PROTOCOL_FLAG & 0xFFFF;

    int PROTOCOL_VERSION11 = (FB_PROTOCOL_FLAG | 11);
    int PROTOCOL_VERSION12 = (FB_PROTOCOL_FLAG | 12);
    int PROTOCOL_VERSION13 = (FB_PROTOCOL_FLAG | 13);
    int PROTOCOL_VERSION14	= (FB_PROTOCOL_FLAG | 14);
    int PROTOCOL_VERSION15 = (FB_PROTOCOL_FLAG | 15);
    int PROTOCOL_VERSION16 = (FB_PROTOCOL_FLAG | 16);
    int PROTOCOL_VERSION17 = (FB_PROTOCOL_FLAG | 17);
    int PROTOCOL_VERSION18 = (FB_PROTOCOL_FLAG | 18);

    // Firebird 3.0.0
    int MINIMUM_SUPPORTED_PROTOCOL_VERSION = PROTOCOL_VERSION13;
    // Firebird 5.0.0
    int MAXIMUM_SUPPORTED_PROTOCOL_VERSION = PROTOCOL_VERSION18;

    int arch_generic = 1;

    int CNCT_user = 1;
    int CNCT_passwd = 2;
    int CNCT_host = 4;
    int CNCT_group = 5;
    int CNCT_user_verification = 6;
    int CNCT_specific_data = 7;
    int CNCT_plugin_name = 8;
    int CNCT_login = 9;
    int CNCT_plugin_list = 10;
    int CNCT_client_crypt = 11;
    int WIRE_CRYPT_DISABLED = 0;
    int WIRE_CRYPT_ENABLED = 1;
    int WIRE_CRYPT_REQUIRED = 2;

    int TAG_KEY_TYPE = 0;
    int TAG_KEY_PLUGINS = 1;
    int TAG_KNOWN_PLUGINS = 2;
    int TAG_PLUGIN_SPECIFIC = 3;

    int ptype_rpc = 2;
    int ptype_batch_send = 3;
    int ptype_out_of_band = 4;
    int ptype_lazy_send = 5;
    int ptype_MASK = 0xFF;

    int pflag_compress = 0x100;

    int P_REQ_async = 1;
}
