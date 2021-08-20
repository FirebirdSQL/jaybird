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
package org.firebirdsql.gds;

import org.firebirdsql.jaybird.fb.constants.DpbItems;

/**
 * Instance of this interface represents a Database Parameter Buffer from the
 * Firebird API documentation and specifies the attributes for the
 * current connection.
 * <p/>
 * Additionally it is possible to change some database properties in a permanent
 * way, however this approach is not recommended. Please use instead management
 * API.
 */
public interface DatabaseParameterBuffer extends ConnectionParameterBuffer {

    //@formatter:off
    @Deprecated
    int CDD_PATHNAME            = DpbItems.isc_dpb_cdd_pathname;
    @Deprecated
    int ALLOCATION              = DpbItems.isc_dpb_allocation;
    @Deprecated
    int JOURNAL                 = DpbItems.isc_dpb_journal;
    @Deprecated
    int PAGE_SIZE               = DpbItems.isc_dpb_page_size;
    @Deprecated
    int NUM_BUFFERS             = DpbItems.isc_dpb_num_buffers;
    @Deprecated
    int BUFFER_LENGTH           = DpbItems.isc_dpb_buffer_length;
    @Deprecated
    int DEBUG                   = DpbItems.isc_dpb_debug;
    @Deprecated
    int GARBAGE_COLLECT         = DpbItems.isc_dpb_garbage_collect;
    @Deprecated
    int VERIFY                  = DpbItems.isc_dpb_verify;
    @Deprecated
    int SWEEP                   = DpbItems.isc_dpb_sweep;
    @Deprecated
    int ENABLE_JOURNAL          = DpbItems.isc_dpb_enable_journal;
    @Deprecated
    int DISABLE_JOURNAL         = DpbItems.isc_dpb_disable_journal;
    @Deprecated
    int DBKEY_SCOPE             = DpbItems.isc_dpb_dbkey_scope;
    @Deprecated
    int NUMBER_OF_USERS         = DpbItems.isc_dpb_number_of_users;
    @Deprecated
    int TRACE                   = DpbItems.isc_dpb_trace;
    @Deprecated
    int NO_GARBAGE_COLLECT      = DpbItems.isc_dpb_no_garbage_collect;
    @Deprecated
    int DAMAGED                 = DpbItems.isc_dpb_damaged;
    @Deprecated
    int LICENSE                 = DpbItems.isc_dpb_license;
    @Deprecated
    int SYS_USER_NAME           = DpbItems.isc_dpb_sys_user_name;
    @Deprecated
    int ENCRYPT_KEY             = DpbItems.isc_dpb_encrypt_key;
    @Deprecated
    int ACTIVATE_SHADOW         = DpbItems.isc_dpb_activate_shadow;
    @Deprecated
    int SWEEP_INTERVAL          = DpbItems.isc_dpb_sweep_interval;
    @Deprecated
    int DELETE_SHADOW           = DpbItems.isc_dpb_delete_shadow;
    @Deprecated
    int FORCE_WRITE             = DpbItems.isc_dpb_force_write;
    @Deprecated
    int BEGIN_LOG               = DpbItems.isc_dpb_begin_log;
    @Deprecated
    int QUIT_LOG                = DpbItems.isc_dpb_quit_log;
    @Deprecated
    int NO_RESERVE              = DpbItems.isc_dpb_no_reserve;
    @Deprecated
    int USER_NAME               = DpbItems.isc_dpb_user_name;
    @Deprecated
    int USER                    = DpbItems.isc_dpb_user; // alias to isc_dpb_user_name
    @Deprecated
    int PASSWORD                = DpbItems.isc_dpb_password;
    @Deprecated
    int PASSWORD_ENC            = DpbItems.isc_dpb_password_enc;
    @Deprecated
    int SYS_USER_NAME_ENC       = DpbItems.isc_dpb_sys_user_name_enc;
    @Deprecated
    int INTERP                  = DpbItems.isc_dpb_interp;
    @Deprecated
    int ONLINE_DUMP             = DpbItems.isc_dpb_online_dump;
    @Deprecated
    int OLD_FILE_SIZE           = DpbItems.isc_dpb_old_file_size;
    @Deprecated
    int OLD_NUM_FILES           = DpbItems.isc_dpb_old_num_files;
    @Deprecated
    int OLD_FILE                = DpbItems.isc_dpb_old_file;
    @Deprecated
    int OLD_START_PAGE          = DpbItems.isc_dpb_old_start_page;
    @Deprecated
    int OLD_START_SEQNO         = DpbItems.isc_dpb_old_start_seqno;
    @Deprecated
    int OLD_START_FILE          = DpbItems.isc_dpb_old_start_file;
    @Deprecated
    int DROP_WALFILE            = DpbItems.isc_dpb_drop_walfile;
    @Deprecated
    int OLD_DUMP_ID             = DpbItems.isc_dpb_old_dump_id;
    @Deprecated
    int WAL_BACKUP_DIR          = DpbItems.isc_dpb_wal_backup_dir;
    @Deprecated
    int WAL_CHKPTLEN            = DpbItems.isc_dpb_wal_chkptlen;
    @Deprecated
    int WAL_NUMBUFS             = DpbItems.isc_dpb_wal_numbufs;
    @Deprecated
    int WAL_BUFSIZE             = DpbItems.isc_dpb_wal_bufsize;
    @Deprecated
    int WAL_GRP_CMT_WAIT        = DpbItems.isc_dpb_wal_grp_cmt_wait;
    @Deprecated
    int LC_MESSAGES             = DpbItems.isc_dpb_lc_messages;
    @Deprecated
    int LC_CTYPE                = DpbItems.isc_dpb_lc_ctype;
    @Deprecated
    int CACHE_MANAGER           = DpbItems.isc_dpb_cache_manager;
    @Deprecated
    int SHUTDOWN                = DpbItems.isc_dpb_shutdown;
    @Deprecated
    int ONLINE                  = DpbItems.isc_dpb_online;
    @Deprecated
    int SHUTDOWN_DELAY          = DpbItems.isc_dpb_shutdown_delay;
    @Deprecated
    int RESERVED                = DpbItems.isc_dpb_reserved;
    @Deprecated
    int OVERWRITE               = DpbItems.isc_dpb_overwrite;
    @Deprecated
    int SEC_ATTACH              = DpbItems.isc_dpb_sec_attach;
    @Deprecated
    int DISABLE_WAL             = DpbItems.isc_dpb_disable_wal;
    @Deprecated
    int CONNECT_TIMEOUT         = DpbItems.isc_dpb_connect_timeout;
    @Deprecated
    int DUMMY_PACKET_INTERVAL   = DpbItems.isc_dpb_dummy_packet_interval;
    @Deprecated
    int GBAK_ATTACH             = DpbItems.isc_dpb_gbak_attach;
    @Deprecated
    int SQL_ROLE_NAME           = DpbItems.isc_dpb_sql_role_name;
    @Deprecated
    int SET_PAGE_BUFFERS        = DpbItems.isc_dpb_set_page_buffers;
    @Deprecated
    int WORKING_DIRECTORY       = DpbItems.isc_dpb_working_directory;
    @Deprecated
    int SQL_DIALECT             = DpbItems.isc_dpb_sql_dialect;
    @Deprecated
    int SET_DB_READONLY         = DpbItems.isc_dpb_set_db_readonly;
    @Deprecated
    int SET_DB_SQL_DIALECT      = DpbItems.isc_dpb_set_db_sql_dialect;
    @Deprecated
    int GFIX_ATTACH             = DpbItems.isc_dpb_gfix_attach;
    @Deprecated
    int GSTAT_ATTACH            = DpbItems.isc_dpb_gstat_attach;
    @Deprecated
    int SET_DB_CHARSET          = DpbItems.isc_dpb_set_db_charset;

    @Deprecated
    int GSEC_ATTACH             = DpbItems.isc_dpb_gsec_attach;
    @Deprecated
    int ADDRESS_PATH            = DpbItems.isc_dpb_address_path;
    @Deprecated
    int PROCESS_ID              = DpbItems.isc_dpb_process_id;
    @Deprecated
    int NO_DB_TRIGGERS          = DpbItems.isc_dpb_no_db_triggers;
    @Deprecated
    int TRUSTED_AUTH            = DpbItems.isc_dpb_trusted_auth;
    @Deprecated
    int PROCESS_NAME            = DpbItems.isc_dpb_process_name;
    //@formatter:on

    /**
     * Make a deep copy of this object.
     *
     * @return deep copy of this object.
     */
    DatabaseParameterBuffer deepCopy();
}
