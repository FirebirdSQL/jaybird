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
    
    /*
     * Driver-specific DPB params that will be removed before sending them
     * to the server. These params influence only client side.
     */
    int socket_buffer_size      = ISCConstants.isc_dpb_socket_buffer_size;
    int blob_buffer_size        = ISCConstants.isc_dpb_blob_buffer_size;
    int use_stream_blobs        = ISCConstants.isc_dpb_use_stream_blobs;
    int paranoia_mode           = ISCConstants.isc_dpb_paranoia_mode;
	int timestamp_uses_local_timezone =     ISCConstants.isc_dpb_timestamp_uses_local_timezone;
    int use_standard_udf        = ISCConstants.isc_dpb_use_standard_udf;
    int local_encoding          = ISCConstants.isc_dpb_local_encoding;
    int mapping_path            = ISCConstants.isc_dpb_mapping_path;
	
	
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
