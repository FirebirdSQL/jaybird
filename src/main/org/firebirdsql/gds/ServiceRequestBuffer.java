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
 * Instance of this interface represents a Service Request Buffer from the
 * Firebird API documentation and specifies the attributes for the Services API
 * operation.
 */
public interface ServiceRequestBuffer {

    /*************************************************************************
     * Operations that can be called via Services API. There are following 
     * groups of operations:
     * 
     *  - backup/restore
     *  - database repair
     *  - user management
     *  - changing the database properties
     *  - license management
     *  - database and server information
     */
    
    // Backup/restore actions
    int ACTION_BACKUP                   = ISCConstants.isc_action_svc_backup;
    int ACTION_RESTORE                  = ISCConstants.isc_action_svc_restore;
    
    // Database repair actions
    int ACTION_REPAIR                   = ISCConstants.isc_action_svc_repair;
    
    // User management actions
    int ACTION_ADD_USER                 = ISCConstants.isc_action_svc_add_user;
    int ACTION_DELETE_USER              = ISCConstants.isc_action_svc_delete_user;
    int ACTION_MODIFY_USER              = ISCConstants.isc_action_svc_modify_user;
    int ACTION_DISPLAY_USER             = ISCConstants.isc_action_svc_display_user;
    
    // Changing database properties actions
    int ACTION_SET_DB_PROPERTIES        = ISCConstants.isc_action_svc_properties;
    
    // License management actions
    int ACTION_ADD_LICENSE              = ISCConstants.isc_action_svc_add_license;
    int ACTION_REMOVE_LICENSE           = ISCConstants.isc_action_svc_remove_license;
    
    // Database and server information
    int ACTION_DB_STATS                 = ISCConstants.isc_action_svc_db_stats;
    int ACTION_GET_SERVER_LOG           = ISCConstants.isc_action_svc_get_ib_log;
    
    
    
    /**************************************************************************
     * Constants for GDS.iscServiceQuery(...) call
     */
    int INFO_SVR_DB_INFO                = ISCConstants.isc_info_svc_svr_db_info;
    int INFO_GET_LICENSE                = ISCConstants.isc_info_svc_get_license;
    int INFO_GET_LICENSE_MASK           = ISCConstants.isc_info_svc_get_license_mask;
    int INFO_GET_CONFIG                 = ISCConstants.isc_info_svc_get_config;
    int INFO_SVC_MGR_VERSION            = ISCConstants.isc_info_svc_version;
    int INFO_SERVER_VERSION             = ISCConstants.isc_info_svc_server_version;
    int INFO_IMPLEMENTATION             = ISCConstants.isc_info_svc_implementation;
    int INFO_CAPABILITIES               = ISCConstants.isc_info_svc_capabilities;
    int INFO_USER_DB_PATH               = ISCConstants.isc_info_svc_user_dbpath;
    int INFO_GET_ENV                    = ISCConstants.isc_info_svc_get_env;
    int INFO_GET_ENV_LOCK               = ISCConstants.isc_info_svc_get_env_lock;
    int INFO_GET_ENV_MSG                = ISCConstants.isc_info_svc_get_env_msg;
    int INFO_GET_OUTPUT_LINE            = ISCConstants.isc_info_svc_line;
    int INFO_GET_OUTPUT_TO_EOF          = ISCConstants.isc_info_svc_to_eof;
    int INFO_TIMEOUT                    = ISCConstants.isc_info_svc_timeout;
    int INFO_GET_LICENSED_USERS         = ISCConstants.isc_info_svc_get_licensed_users;
    int INFO_RUNNING                    = ISCConstants.isc_info_svc_running;
    int INFO_GET_USERS                  = ISCConstants.isc_info_svc_get_users;

    // Information about the limbo transactions
    int INFO_LIMBO_TRANSACTIONS         = ISCConstants.isc_info_svc_limbo_trans;
    
    int INFO_SINGLE_TRANSACTION_ID      = ISCConstants.isc_spb_single_tra_id;
    int INFO_MULTI_TRANSACTION_ID       = ISCConstants.isc_spb_multi_tra_id;
    int INFO_TRANSACTION_HOST_SITE      = ISCConstants.isc_spb_tra_host_site;
    int INFO_TRANSACTION_REMOTE_SITE    = ISCConstants.isc_spb_tra_remote_site;
    int INFO_TRANSACTION_DB_PATH        = ISCConstants.isc_spb_tra_db_path;
    
    // Server recommendation for transaction outcome resolution
    int INFO_TRANSACTION_ADVICE         = ISCConstants.isc_spb_tra_advise;
    int INFO_TRANSACTION_ADVICE_COMMIT  = ISCConstants.isc_spb_tra_advise_commit;
    int INFO_TRANSACTION_ADVICE_ROLLBACK= ISCConstants.isc_spb_tra_advise_rollback;
    int INFO_TRANSACTION_ADVICE_UNKNOWN = ISCConstants.isc_spb_tra_advise_unknown;
    
    // Information about transaction state
    int INFO_TRANSACTION_STATE          = ISCConstants.isc_spb_tra_state;
    int INFO_TRANSACTION_STATE_COMMIT   = ISCConstants.isc_spb_tra_state_commit;
    int INFO_TRANSACTION_STATE_ROLLBACK = ISCConstants.isc_spb_tra_state_rollback;
    int INFO_TRANSACTION_STATE_LIMBO    = ISCConstants.isc_spb_tra_state_limbo;
    int INFO_TRANSACTION_STATE_UNKNOWN  = ISCConstants.isc_spb_tra_state_unknown;
    
    int INFO_FLAG_END                   = ISCConstants.isc_info_flag_end;
    
    /*************************************************************************
     * Parameters for ACTION_ADD_USER, ACTION_DELETE_USER, ACTION_MODIFY_USER,
     * and ACTION_DISPLAY_USER service calls.
     */
    int SECURITY_USERID                 = ISCConstants.isc_spb_sec_userid;
    int SECURITY_GROUPID                = ISCConstants.isc_spb_sec_groupid;
    int SECURITY_USER_NAME              = ISCConstants.isc_spb_sec_username;
    int SECURITY_PASSWORD               = ISCConstants.isc_spb_sec_password;
    int SECURITY_GROUPNAME              = ISCConstants.isc_spb_sec_groupname;
    int SECURITY_FIRST_NAME             = ISCConstants.isc_spb_sec_firstname;
    int SECURITY_MIDDLE_NAME            = ISCConstants.isc_spb_sec_middlename;
    int SECURITY_LAST_NAME              = ISCConstants.isc_spb_sec_lastname;
    
    
    
    /*************************************************************************
     * Parameters for ACTION_ADD_LICENSE and ACTION_REMOVE_LICENSE calls.
     */
    int LICENSE_KEY                     = ISCConstants.isc_spb_lic_key;
    int LICENSE_ID                      = ISCConstants.isc_spb_lic_id;
    int LICENSE_DESCRIPTION             = ISCConstants.isc_spb_lic_desc;
    
    
    
    /*************************************************************************
     * Parameters for ACTION_BACKUP call.
     */
    int BACKUP_DB_NAME                  = ServiceParameterBuffer.DBNAME;
    int BACKUP_FILE                     = ISCConstants.isc_spb_bkp_file;
    int BACKUP_LENGTH                   = ISCConstants.isc_spb_bkp_length;
    int BACKUP_FACTOR                   = ISCConstants.isc_spb_bkp_factor;

    // Backup operation options, each constant is a bit in a bit mask.
    int BACKUP_OPTIONS                  = ServiceParameterBuffer.OPTIONS;
    
    // Each constant below represents a bit in a bit mask.
    int BACKUP_IGNORE_CHECKSUMS         = ISCConstants.isc_spb_bkp_ignore_checksums;
    int BACKUP_IGNORE_LIMBO             = ISCConstants.isc_spb_bkp_ignore_limbo;
    int BACKUP_METADATA_ONLY            = ISCConstants.isc_spb_bkp_metadata_only;
    int BACKUP_NO_GARBAGE_COLLECT       = ISCConstants.isc_spb_bkp_no_garbage_collect;
    int BACKUP_OLD_DESCRIPTIONS         = ISCConstants.isc_spb_bkp_old_descriptions;
    int BACKUP_NON_TRANSPORTABLE        = ISCConstants.isc_spb_bkp_non_transportable;
    int BACKUP_CONVERT                  = ISCConstants.isc_spb_bkp_convert;
    int BACKUP_EXPAND                   = ISCConstants.isc_spb_bkp_expand;
    
    
    
    /*************************************************************************
     * Parameters for ACTION_RESTORE call.
     */
    int RESTORE_DB_NAME                 = ServiceParameterBuffer.DBNAME;
    int RESTORE_BACKUP_PATH             = ServiceRequestBuffer.BACKUP_FILE;
    int RESTORE_LENGTH                  = ISCConstants.isc_spb_res_length;
    int RESTORE_BUFFERS                 = ISCConstants.isc_spb_res_buffers;
    int RESTORE_PAGE_SIZE               = ISCConstants.isc_spb_res_page_size;
    
    // Access mode for a restored database, read-write or read-only.
    int RESTORE_ACCESS_MODE             = ISCConstants.isc_spb_res_access_mode;
    int RESTORE_ACCESS_MODE_READONLY    = ISCConstants.isc_spb_res_am_readonly;
    int RESTORE_ACCESS_MODE_READWRITE   = ISCConstants.isc_spb_res_am_readwrite;
    
    // Restore operation options
    int RESTORE_OPTIONS                 = ServiceParameterBuffer.OPTIONS;
    
    // Each constant below represents a bit in a bit mask.
    int RESTORE_DEACTIVATE_IDX          = ISCConstants.isc_spb_res_deactivate_idx;
    int RESTORE_NO_SHADOW               = ISCConstants.isc_spb_res_no_shadow;
    int RESTORE_NO_VALIDITY             = ISCConstants.isc_spb_res_no_validity;
    int RESTORE_ONE_AT_A_TIME           = ISCConstants.isc_spb_res_one_at_a_time;
    int RESTORE_OVERWRITE               = ISCConstants.isc_spb_res_replace;
    int RESTORE_CREATE                  = ISCConstants.isc_spb_res_create;
    int RESTORE_USE_ALL_SPACE           = ISCConstants.isc_spb_res_use_all_space;
    
    
    
    /*************************************************************************
     * Parameters for setting database properties.
     */
    int PROPS_DB_NAME                   = ServiceParameterBuffer.DBNAME;
    int PROPS_BUFFERS                   = ISCConstants.isc_spb_prp_page_buffers;
    int PROPS_SWEEP_INTERVAL            = ISCConstants.isc_spb_prp_sweep_interval;
    int PROPS_SHUTDOWN_DB               = ISCConstants.isc_spb_prp_shutdown_db;
    int PROPS_DENY_NEW_TRANSACTIONS     = ISCConstants.isc_spb_prp_deny_new_transactions;
    int PROPS_DENY_NEW_ATTACHMENTS      = ISCConstants.isc_spb_prp_deny_new_attachments;
    int PROPS_SET_SQL_DIALECT           = ISCConstants.isc_spb_prp_set_sql_dialect;
    
    // Reserving 20% space on each page for future record version
    int PROPS_RESERVE_SPACE             = ISCConstants.isc_spb_prp_reserve_space;
    int PROPS_RESERVE_USE_ALL_SPACE     = ISCConstants.isc_spb_prp_res_use_full;
    int PROPS_RESERVE_VERSIONS          = ISCConstants.isc_spb_prp_res;
    
    // Write mode for the database pages sync or async (forced writes)
    int PROPS_WRITE_MODE                = ISCConstants.isc_spb_prp_write_mode;
    int PROPS_WRITE_MODE_ASYNC          = ISCConstants.isc_spb_prp_wm_async;
    int PROPS_WRITE_MODE_SYNC           = ISCConstants.isc_spb_prp_wm_sync;
    
    // Database access mode: read-write or read-only
    int PROPS_ACCESS_MODE               = ISCConstants.isc_spb_prp_access_mode;
    int PROPS_ACCESS_MODE_READONLY      = ISCConstants.isc_spb_prp_am_readonly;
    int PROPS_ACCESS_MODE_READWRITE     = ISCConstants.isc_spb_prp_am_readwrite;
    
    // Database property options
    int PROPS_OPTIONS                   = ServiceParameterBuffer.OPTIONS;
    
    // Each constant below represents a bit in a bit mask
    int PROPS_ACTIVATE_SHADOW           = ISCConstants.isc_spb_prp_activate;
    int PROPS_DB_ONLINE                 = ISCConstants.isc_spb_prp_db_online;
    
    

    /*************************************************************************
     * Parameters for database repair.
     */
    int REPAIR_DB_NAME                  = ServiceParameterBuffer.DBNAME;
    int REPAIR_COMMIT_TRANSACTIONS      = ISCConstants.isc_spb_rpr_commit_trans;
    int REPAIR_ROLLBACK_TRANSACTIONS    = ISCConstants.isc_spb_rpr_rollback_trans;
    int REPAIR_RECOVER_TWO_PHASE        = ISCConstants.isc_spb_rpr_recover_two_phase;
    int REPAIR_TRANSACTION_ID           = ISCConstants.isc_spb_tra_id;
    
    // Database repair options
    int REPAIR_OPTIONS                  = ServiceParameterBuffer.OPTIONS;
    
    // Each constant below represents a bit in a bit mask.
    int REPAIR_VALIDATE_DB              = ISCConstants.isc_spb_rpr_validate_db;
    int REPAIR_SWEEP_DB                 = ISCConstants.isc_spb_rpr_sweep_db;
    int REPAIR_MEND_DB                  = ISCConstants.isc_spb_rpr_mend_db;
    int REPAIR_LIST_LIMBO_TRANSACTIONS  = ISCConstants.isc_spb_rpr_list_limbo_trans;
    int REPAIR_CHECK_DB                 = ISCConstants.isc_spb_rpr_check_db;
    int REPAIR_IGNORE_CHECKSUM          = ISCConstants.isc_spb_rpr_ignore_checksum;
    int REPAIR_KILL_SHADOWS             = ISCConstants.isc_spb_rpr_kill_shadows;
    int REPAIR_FULL                     = ISCConstants.isc_spb_rpr_full;

    
    
    /*************************************************************************
     * Parameters for database statistics.
     */
    int STATS_DB_NAME                   = ServiceParameterBuffer.DBNAME;
    
    // Database statistics options.
    int STATS_OPTIONS                   = ServiceParameterBuffer.OPTIONS;
    
    // Each constant below represents a bit in a bit mask.
    int STATS_DATA_PAGES                = ISCConstants.isc_spb_sts_data_pages;
    int STATS_DB_LOG                    = ISCConstants.isc_spb_sts_db_log;
    int STATS_HEADER_PAGES              = ISCConstants.isc_spb_sts_hdr_pages;
    int STATS_INDEX_PAGES               = ISCConstants.isc_spb_sts_idx_pages;
    int STATS_SYSTEM_RELATIONS          = ISCConstants.isc_spb_sts_sys_relations;
    int STATS_RECORD_VERSIONS           = ISCConstants.isc_spb_sts_record_versions;
    int STATS_TABLE                     = ISCConstants.isc_spb_sts_table;
    int STATS_NOCREATION                = ISCConstants.isc_spb_sts_nocreation;
    
    
    /**
     * Set a void (valueless) parameter on this
     * <code>ServiceRequestBuffer</code>.
     * 
     * @param argumentType
     *            The parameter to be set
     */
    public void addArgument(int argumentType);

    /**
     * Set a <code>String</code> parameter on this
     * <code>ServiceRequestBuffer</code>.
     * 
     * @param argumentType
     *            The parameter to be set
     * @param value
     *            The value to set for the given parameter
     */
    public void addArgument(int argumentType, String value);

    /**
     * Set an <code>int</code> parameter on this
     * <code>ServiceRequestBuffer</code>.
     * 
     * @param argumentType
     *            The parameter to be set
     * @param value
     *            The value to set for the given parameter
     */
    public void addArgument(int argumentType, int value);

    /**
     * Set an <code>long</code> parameter on this <code>ServiceRequestBuffer</code>.
     * <p>
     * Note that most argumentTypes will be handled as if they are called with {@link #addArgument(int, int)}, except
     * for a small number of argument types:
     * <ul>
     *     <li>{@link ISCConstants#isc_spb_rpr_commit_trans_64}</li>
     *     <li>{@link ISCConstants#isc_spb_rpr_rollback_trans_64}</li>
     *     <li>{@link ISCConstants#isc_spb_rpr_recover_two_phase_64}</li>
     * </ul>
     * </p>
     *
     * @param argumentType
     *            The parameter to be set
     * @param value
     *            The value to set for the given parameter
     */
    public void addArgument(int argumentType, long value);

    /**
     * Set a <code>byte</code> parameter on this
     * <code>ServiceRequestBuffer</code>.
     * 
     * @param argumentType
     *            The parameter to be set
     * @param value
     *            The value to the set for the given parameter
     */
    public void addArgument(int argumentType, byte value);
}
