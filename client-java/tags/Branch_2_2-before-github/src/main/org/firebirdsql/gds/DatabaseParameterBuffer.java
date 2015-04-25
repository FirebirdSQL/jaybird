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

/**
 * Instance of this interface represents a Database Parameter Buffer from the
 * Firebird API documentation and specifies the attributes for the
 * current connection.
 * <p>
 * Additionally it is possible to change some database properties in a permanent
 * way, however this approach is not recommended. Please use instead management
 * API.
 */
public interface DatabaseParameterBuffer {
    
    int CDD_PATHNAME            = ISCConstants.isc_dpb_cdd_pathname;
    int ALLOCATION              = ISCConstants.isc_dpb_allocation;
    int JOURNAL                 = ISCConstants.isc_dpb_journal;
    int PAGE_SIZE               = ISCConstants.isc_dpb_page_size;
    int NUM_BUFFERS             = ISCConstants.isc_dpb_num_buffers;
    int BUFFER_LENGTH           = ISCConstants.isc_dpb_buffer_length;
    int DEBUG                   = ISCConstants.isc_dpb_debug;
    int GARBAGE_COLLECT         = ISCConstants.isc_dpb_garbage_collect;
    int VERIFY                  = ISCConstants.isc_dpb_verify;
    int SWEEP                   = ISCConstants.isc_dpb_sweep;
    int ENABLE_JOURNAL          = ISCConstants.isc_dpb_enable_journal;
    int DISABLE_JOURNAL         = ISCConstants.isc_dpb_disable_journal;
    int DBKEY_SCOPE             = ISCConstants.isc_dpb_dbkey_scope;
    int NUMBER_OF_USERS         = ISCConstants.isc_dpb_number_of_users;
    int TRACE                   = ISCConstants.isc_dpb_trace;
    int NO_GARBAGE_COLLECT      = ISCConstants.isc_dpb_no_garbage_collect;
    int DAMAGED                 = ISCConstants.isc_dpb_damaged;
    int LICENSE                 = ISCConstants.isc_dpb_license;
    int SYS_USER_NAME           = ISCConstants.isc_dpb_sys_user_name;
    int ENCRYPT_KEY             = ISCConstants.isc_dpb_encrypt_key;
    int ACTIVATE_SHADOW         = ISCConstants.isc_dpb_activate_shadow;
    int SWEEP_INTERVAL          = ISCConstants.isc_dpb_sweep_interval;
    int DELETE_SHADOW           = ISCConstants.isc_dpb_delete_shadow;
    int FORCE_WRITE             = ISCConstants.isc_dpb_force_write;
    int BEGIN_LOG               = ISCConstants.isc_dpb_begin_log;
    int QUIT_LOG                = ISCConstants.isc_dpb_quit_log;
    int NO_RESERVE              = ISCConstants.isc_dpb_no_reserve;
    int USER_NAME               = ISCConstants.isc_dpb_user_name;
    int USER                    = ISCConstants.isc_dpb_user; // alias to isc_dpb_user_name
    int PASSWORD                = ISCConstants.isc_dpb_password;
    int PASSWORD_ENC            = ISCConstants.isc_dpb_password_enc;
    int SYS_USER_NAME_ENC       = ISCConstants.isc_dpb_sys_user_name_enc;
    int INTERP                  = ISCConstants.isc_dpb_interp;
    int ONLINE_DUMP             = ISCConstants.isc_dpb_online_dump;
    int OLD_FILE_SIZE           = ISCConstants.isc_dpb_old_file_size;
    int OLD_NUM_FILES           = ISCConstants.isc_dpb_old_num_files;
    int OLD_FILE                = ISCConstants.isc_dpb_old_file;
    int OLD_START_PAGE          = ISCConstants.isc_dpb_old_start_page;
    int OLD_START_SEQNO         = ISCConstants.isc_dpb_old_start_seqno;
    int OLD_START_FILE          = ISCConstants.isc_dpb_old_start_file;
    int DROP_WALFILE            = ISCConstants.isc_dpb_drop_walfile;
    int OLD_DUMP_ID             = ISCConstants.isc_dpb_old_dump_id;
    int WAL_BACKUP_DIR          = ISCConstants.isc_dpb_wal_backup_dir;
    int WAL_CHKPTLEN            = ISCConstants.isc_dpb_wal_chkptlen;
    int WAL_NUMBUFS             = ISCConstants.isc_dpb_wal_numbufs;
    int WAL_BUFSIZE             = ISCConstants.isc_dpb_wal_bufsize;
    int WAL_GRP_CMT_WAIT        = ISCConstants.isc_dpb_wal_grp_cmt_wait;
    int LC_MESSAGES             = ISCConstants.isc_dpb_lc_messages;
    int LC_CTYPE                = ISCConstants.isc_dpb_lc_ctype;
    int CACHE_MANAGER           = ISCConstants.isc_dpb_cache_manager;
    int SHUTDOWN                = ISCConstants.isc_dpb_shutdown;
    int ONLINE                  = ISCConstants.isc_dpb_online;
    int SHUTDOWN_DELAY          = ISCConstants.isc_dpb_shutdown_delay;
    int RESERVED                = ISCConstants.isc_dpb_reserved;
    int OVERWRITE               = ISCConstants.isc_dpb_overwrite;
    int SEC_ATTACH              = ISCConstants.isc_dpb_sec_attach;
    int DISABLE_WAL             = ISCConstants.isc_dpb_disable_wal;
    int CONNECT_TIMEOUT         = ISCConstants.isc_dpb_connect_timeout;
    int DUMMY_PACKET_INTERVAL   = ISCConstants.isc_dpb_dummy_packet_interval;
    int GBAK_ATTACH             = ISCConstants.isc_dpb_gbak_attach;
    int SQL_ROLE_NAME           = ISCConstants.isc_dpb_sql_role_name;
    int SET_PAGE_BUFFERS        = ISCConstants.isc_dpb_set_page_buffers;
    int WORKING_DIRECTORY       = ISCConstants.isc_dpb_working_directory;
    int SQL_DIALECT             = ISCConstants.isc_dpb_sql_dialect;
    int SET_DB_READONLY         = ISCConstants.isc_dpb_set_db_readonly;
    int SET_DB_SQL_DIALECT      = ISCConstants.isc_dpb_set_db_sql_dialect;
    int GFIX_ATTACH             = ISCConstants.isc_dpb_gfix_attach;
    int GSTAT_ATTACH            = ISCConstants.isc_dpb_gstat_attach;
    int SET_DB_CHARSET          = ISCConstants.isc_dpb_set_db_charset;
    
    int GSEC_ATTACH             = ISCConstants.isc_dpb_gsec_attach;
    int ADDRESS_PATH            = ISCConstants.isc_dpb_address_path;
    int PROCESS_ID              = ISCConstants.isc_dpb_process_id;
    int NO_DB_TRIGGERS          = ISCConstants.isc_dpb_no_db_triggers;
    int TRUSTED_AUTH            = ISCConstants.isc_dpb_trusted_auth;
    int PROCESS_NAME            = ISCConstants.isc_dpb_process_name;
    
    /**
     * Add argument with no parameters.
     * 
     * @param argumentType type of argument.
     */
    void addArgument(int argumentType);

    /**
     * Add string argument.
     * @param argumentType type of argument.
     * @param value string value to add.
     */
    void addArgument(int argumentType, String value);

    /**
     * Add integer argument.
     * @param argumentType type of argument.
     * @param value integer value to add.
     */
    void addArgument(int argumentType, int value);
	
	/**
	 * Add array of bytes.
	 * @param argumentType type of argument.
	 * @param content content of argument.
	 */ 
	void addArgument(int argumentType, byte[] content);

	/**
	 * Remove specified argument.
	 * @param argumentType type of argument to remove.
	 */ 
	void removeArgument(int argumentType);

	/**
	 * Get argument as string.
	 * @param argumentType type of argument to find.
	 * @return argument as string or <code>null</code> if nothing found.
	 */ 
	String getArgumentAsString(int argumentType);
    
    /**
     * Get argument as int.
     * @param argumentType type of argument to find.
     * @return argument as string or <code>0</code> if nothing found.
     */ 
    int getArgumentAsInt(int argumentType);
    
	/**
     * Check if this parameter buffer has specified argument.
	 * @param argumentType type of argument to find.
	 * @return <code>true</code> if this buffer contains specified argument.
	 */
    boolean hasArgument(int argumentType);

    /**
	 * Make a deep copy of this object.
	 * @return deep copy of this object.
	 */ 
    DatabaseParameterBuffer deepCopy();
    
}
