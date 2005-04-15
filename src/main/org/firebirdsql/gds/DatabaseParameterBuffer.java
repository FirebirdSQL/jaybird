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
 * This interface replaces Clumplet in calls to 
 * <code>isc_create_database</code> and <code>isc_attach_database</code>.
 * <p>
 * Instances are created via <code>GDS.newDatabaseParameterBuffer();</code>
 * <p>
 * Constants from <code>ISCConstants</code> that are relevant to a database 
 * parameter buffer are duplicated on this interface. If the original name 
 * was <code>isc_dpb_cdd_pathname</code> then the new name is 
 * <code>cdd_pathname</code>. 
 */
public interface DatabaseParameterBuffer
    {
    
    /*
    int cdd_pathname            = ISCConstants.isc_dpb_cdd_pathname;
    int allocation              = ISCConstants.isc_dpb_allocation;
    int journal                 = ISCConstants.isc_dpb_journal;
    int page_size               = ISCConstants.isc_dpb_page_size;
    int num_buffers             = ISCConstants.isc_dpb_num_buffers;
    int buffer_length           = ISCConstants.isc_dpb_buffer_length;
    int debug                   = ISCConstants.isc_dpb_debug;
    int garbage_collect         = ISCConstants.isc_dpb_garbage_collect;
    int verify                  = ISCConstants.isc_dpb_verify;
    int sweep                   = ISCConstants.isc_dpb_sweep;
    int enable_journal          = ISCConstants.isc_dpb_enable_journal;
    int disable_journal         = ISCConstants.isc_dpb_disable_journal;
    int dbkey_scope             = ISCConstants.isc_dpb_dbkey_scope;
    int number_of_users         = ISCConstants.isc_dpb_number_of_users;
    int trace                   = ISCConstants.isc_dpb_trace;
    int no_garbage_collect      = ISCConstants.isc_dpb_no_garbage_collect;
    int damaged                 = ISCConstants.isc_dpb_damaged;
    int license                 = ISCConstants.isc_dpb_license;
    int sys_user_name           = ISCConstants.isc_dpb_sys_user_name;
    int encrypt_key             = ISCConstants.isc_dpb_encrypt_key;
    int activate_shadow         = ISCConstants.isc_dpb_activate_shadow;
    int sweep_interval          = ISCConstants.isc_dpb_sweep_interval;
    int delete_shadow           = ISCConstants.isc_dpb_delete_shadow;
    int force_write             = ISCConstants.isc_dpb_force_write;
    int begin_log               = ISCConstants.isc_dpb_begin_log;
    int quit_log                = ISCConstants.isc_dpb_quit_log;
    int no_reserve              = ISCConstants.isc_dpb_no_reserve;
    int user_name               = ISCConstants.isc_dpb_user_name;
    int user                    = ISCConstants.isc_dpb_user; // alias to isc_dpb_user_name
    int password                = ISCConstants.isc_dpb_password;
    int password_enc            = ISCConstants.isc_dpb_password_enc;
    int sys_user_name_enc       = ISCConstants.isc_dpb_sys_user_name_enc;
    int interp                  = ISCConstants.isc_dpb_interp;
    int online_dump             = ISCConstants.isc_dpb_online_dump;
    int old_file_size           = ISCConstants.isc_dpb_old_file_size;
    int old_num_files           = ISCConstants.isc_dpb_old_num_files;
    int old_file                = ISCConstants.isc_dpb_old_file;
    int old_start_page          = ISCConstants.isc_dpb_old_start_page;
    int old_start_seqno         = ISCConstants.isc_dpb_old_start_seqno;
    int old_start_file          = ISCConstants.isc_dpb_old_start_file;
    int drop_walfile            = ISCConstants.isc_dpb_drop_walfile;
    int old_dump_id             = ISCConstants.isc_dpb_old_dump_id;
    int wal_backup_dir          = ISCConstants.isc_dpb_wal_backup_dir;
    int wal_chkptlen            = ISCConstants.isc_dpb_wal_chkptlen;
    int wal_numbufs             = ISCConstants.isc_dpb_wal_numbufs;
    int wal_bufsize             = ISCConstants.isc_dpb_wal_bufsize;
    int wal_grp_cmt_wait        = ISCConstants.isc_dpb_wal_grp_cmt_wait;
    int lc_messages             = ISCConstants.isc_dpb_lc_messages;
    int lc_ctype                = ISCConstants.isc_dpb_lc_ctype;
    int cache_manager           = ISCConstants.isc_dpb_cache_manager;
    int shutdown                = ISCConstants.isc_dpb_shutdown;
    int online                  = ISCConstants.isc_dpb_online;
    int shutdown_delay          = ISCConstants.isc_dpb_shutdown_delay;
    int reserved                = ISCConstants.isc_dpb_reserved;
    int overwrite               = ISCConstants.isc_dpb_overwrite;
    int sec_attach              = ISCConstants.isc_dpb_sec_attach;
    int disable_wal             = ISCConstants.isc_dpb_disable_wal;
    int connect_timeout         = ISCConstants.isc_dpb_connect_timeout;
    int dummy_packet_interval   = ISCConstants.isc_dpb_dummy_packet_interval;
    int gbak_attach             = ISCConstants.isc_dpb_gbak_attach;
    int sql_role_name           = ISCConstants.isc_dpb_sql_role_name;
    int set_page_buffers        = ISCConstants.isc_dpb_set_page_buffers;
    int working_directory       = ISCConstants.isc_dpb_working_directory;
    int sql_dialect             = ISCConstants.isc_dpb_sql_dialect;
    int set_db_readonly         = ISCConstants.isc_dpb_set_db_readonly;
    int set_db_sql_dialect      = ISCConstants.isc_dpb_set_db_sql_dialect;
    int gfix_attach             = ISCConstants.isc_dpb_gfix_attach;
    int gstat_attach            = ISCConstants.isc_dpb_gstat_attach;
    int set_db_charset          = ISCConstants.isc_dpb_set_db_charset;

     */

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
    
    /*
     * Driver-specific DPB params that will be removed before sending them
     * to the server. These params influence only client side.
     */
    int SOCKET_BUFFER_SIZE      = ISCConstants.isc_dpb_socket_buffer_size;
    int BLOB_BUFFER_SIZE        = ISCConstants.isc_dpb_blob_buffer_size;
    int USE_STREAM_BLOBS        = ISCConstants.isc_dpb_use_stream_blobs;
    int PARANOIA_MODE           = ISCConstants.isc_dpb_paranoia_mode;
	int TIMESTAMP_USES_LOCAL_TIMEZONE =     ISCConstants.isc_dpb_timestamp_uses_local_timezone;
    int USE_STANDARD_UDF        = ISCConstants.isc_dpb_use_standard_udf;
    int LOCAL_ENCODING          = ISCConstants.isc_dpb_local_encoding;
    int MAPPING_PATH            = ISCConstants.isc_dpb_mapping_path;
	
	
    /**
     * Add argument.
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
