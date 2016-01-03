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
 * Constants for Firebird features, parameter blocks and errors.
 *
 * @author <a href="mailto:alberola@users.sourceforge.net">Alejandro Alberola</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
@SuppressWarnings("unused")
public interface ISCConstants {

    int SQLDA_VERSION1             = 1;
    int SQL_DIALECT_V5             = 1;
    int SQL_DIALECT_V6_TRANSITION  = 2;
    int SQL_DIALECT_V6             = 3;
    int SQL_DIALECT_CURRENT        = SQL_DIALECT_V6;

    int DSQL_close   =  1;
    int DSQL_drop    =  2;

    /************************/
    /* Blob seek parameters */
    /************************/

    int blb_seek_from_head = 0;
    int blb_seek_relative =  1;
    int blb_seek_from_tail = 2;

    /*******************/
    /* blob_get_result */
    /*******************/
    int blb_got_fragment     = -1;
    int blb_got_eof          = 0;
    int blb_got_full_segment = 1;

    /**********************************/
    /* Database parameter block stuff */
    /**********************************/

    int isc_dpb_version1                = 1;
    int isc_dpb_version2                = 2;

    int isc_dpb_cdd_pathname            = 1;
    int isc_dpb_allocation              = 2;
    int isc_dpb_journal                 = 3;
    int isc_dpb_page_size               = 4;
    int isc_dpb_num_buffers             = 5;
    int isc_dpb_buffer_length           = 6;
    int isc_dpb_debug                   = 7;
    int isc_dpb_garbage_collect         = 8;
    int isc_dpb_verify                  = 9;
    int isc_dpb_sweep                   = 10;
    int isc_dpb_enable_journal          = 11;
    int isc_dpb_disable_journal         = 12;
    int isc_dpb_dbkey_scope             = 13;
    int isc_dpb_number_of_users         = 14;
    int isc_dpb_trace                   = 15;
    int isc_dpb_no_garbage_collect      = 16;
    int isc_dpb_damaged                 = 17;
    int isc_dpb_license                 = 18;
    int isc_dpb_sys_user_name           = 19;
    int isc_dpb_encrypt_key             = 20;
    int isc_dpb_activate_shadow         = 21;
    int isc_dpb_sweep_interval          = 22;
    int isc_dpb_delete_shadow           = 23;
    int isc_dpb_force_write             = 24;
    int isc_dpb_begin_log               = 25;
    int isc_dpb_quit_log                = 26;
    int isc_dpb_no_reserve              = 27;
    int isc_dpb_user_name               = 28;
    int isc_dpb_user                    = 28; // alias to isc_dpb_user_name
    int isc_dpb_password                = 29;
    int isc_dpb_password_enc            = 30;
    int isc_dpb_sys_user_name_enc       = 31;
    int isc_dpb_interp                  = 32;
    int isc_dpb_online_dump             = 33;
    int isc_dpb_old_file_size           = 34;
    int isc_dpb_old_num_files           = 35;
    int isc_dpb_old_file                = 36;
    int isc_dpb_old_start_page          = 37;
    int isc_dpb_old_start_seqno         = 38;
    int isc_dpb_old_start_file          = 39;
    int isc_dpb_drop_walfile            = 40;
    int isc_dpb_old_dump_id             = 41;
    int isc_dpb_wal_backup_dir          = 42;
    int isc_dpb_wal_chkptlen            = 43;
    int isc_dpb_wal_numbufs             = 44;
    int isc_dpb_wal_bufsize             = 45;
    int isc_dpb_wal_grp_cmt_wait        = 46;
    int isc_dpb_lc_messages             = 47;
    int isc_dpb_lc_ctype                = 48;
    int isc_dpb_cache_manager           = 49;
    int isc_dpb_shutdown                = 50;
    int isc_dpb_online                  = 51;
    int isc_dpb_shutdown_delay          = 52;
    int isc_dpb_reserved                = 53;
    int isc_dpb_overwrite               = 54;
    int isc_dpb_sec_attach              = 55;
    int isc_dpb_disable_wal             = 56;
    int isc_dpb_connect_timeout         = 57;
    int isc_dpb_dummy_packet_interval   = 58;
    int isc_dpb_gbak_attach             = 59;
    int isc_dpb_sql_role_name           = 60;
    int isc_dpb_set_page_buffers        = 61;
    int isc_dpb_working_directory       = 62;
    int isc_dpb_sql_dialect             = 63;
    int isc_dpb_set_db_readonly         = 64;
    int isc_dpb_set_db_sql_dialect      = 65;
    int isc_dpb_gfix_attach             = 66;
    int isc_dpb_gstat_attach            = 67;
    int isc_dpb_set_db_charset          = 68;

    // Firebird 2.1 constants
    int isc_dpb_gsec_attach             = 69;
    int isc_dpb_address_path            = 70;
    int isc_dpb_process_id              = 71;
    int isc_dpb_no_db_triggers          = 72;
    int isc_dpb_trusted_auth            = 73;
    int isc_dpb_process_name            = 74;

    // Firebird 2.5 constants
    int isc_dpb_trusted_role            = 75;
    int isc_dpb_org_filename            = 76;
    int isc_dpb_utf8_filename           = 77;
    int isc_dpb_ext_call_depth          = 78;

    // Firebird 3.0 constants
    int isc_dpb_auth_block				= 79;
    int isc_dpb_client_version			= 80;
    int isc_dpb_remote_protocol			= 81;
    int isc_dpb_host_name				= 82;
    int isc_dpb_os_user					= 83;
    int isc_dpb_specific_auth_data      = 84;
    int isc_dpb_auth_plugin_list		= 85;
    int isc_dpb_auth_plugin_name		= 86;
    int isc_dpb_config					= 87;
    int isc_dpb_nolinger				= 88;
    int isc_dpb_reset_icu				= 89;
    int isc_dpb_map_attach              = 90;

    /*
     * Driver-specific DPB params that will be removed before sending them
     * to the server. These params influence only client side.
     */
    int isc_dpb_socket_buffer_size      = 129;
    int isc_dpb_blob_buffer_size        = 130;
    int isc_dpb_use_stream_blobs        = 131;
    int isc_dpb_paranoia_mode           = 132;
    int isc_dpb_timestamp_uses_local_timezone        = 133;
    int isc_dpb_use_standard_udf        = 134;
    int isc_dpb_local_encoding          = 135;
    int isc_dpb_mapping_path            = 136;
    int isc_dpb_no_result_set_tracking  = 137;
    int isc_dpb_result_set_holdable     = 138;
    int isc_dpb_filename_charset        = 139;
    @Deprecated
    int isc_dpb_octets_as_bytes         = 140;
    int isc_dpb_so_timeout              = 141;
    int isc_dpb_column_label_for_name   = 142;
    int isc_dpb_use_firebird_autocommit = 143;

    /*************************************/
    /* Transaction parameter block stuff */
    /*************************************/

    int isc_tpb_version1                = 1;
    int isc_tpb_version3                = 3;
    int isc_tpb_consistency             = 1;
    int isc_tpb_concurrency             = 2;
    int isc_tpb_shared                  = 3;
    int isc_tpb_protected               = 4;
    int isc_tpb_exclusive               = 5;
    int isc_tpb_wait                    = 6;
    int isc_tpb_nowait                  = 7;
    int isc_tpb_read                    = 8;
    int isc_tpb_write                   = 9;
    int isc_tpb_lock_read               = 10;
    int isc_tpb_lock_write              = 11;
    int isc_tpb_verb_time               = 12;
    int isc_tpb_commit_time             = 13;
    int isc_tpb_ignore_limbo            = 14;
    int isc_tpb_read_committed          = 15;
    int isc_tpb_autocommit              = 16;
    int isc_tpb_rec_version             = 17;
    int isc_tpb_no_rec_version          = 18;
    int isc_tpb_restart_requests        = 19;
    int isc_tpb_no_auto_undo            = 20;
    int isc_tpb_lock_timeout            = 21;

    /*************************************/
    /* Service parameter block stuff */
    /*************************************/

    int isc_spb_version1                = 1;
    int isc_spb_current_version         = 2;
    int isc_spb_version			        = isc_spb_current_version;
    int isc_spb_version3                = 3;
    int isc_spb_user_name               = isc_dpb_user_name;
    int isc_spb_sys_user_name           = isc_dpb_sys_user_name;
    int isc_spb_sys_user_name_enc       = isc_dpb_sys_user_name_enc;
    int isc_spb_password                = isc_dpb_password;
    int isc_spb_password_enc            = isc_dpb_password_enc;
    int isc_spb_command_line            = 105;
    int isc_spb_dbname                  = 106;
    int isc_spb_verbose                 = 107;
    int isc_spb_options                 = 108;
    int isc_spb_address_path            = 109;
    int isc_spb_process_id              = 110;
    int isc_spb_trusted_auth			= 111;
    int isc_spb_process_name            = 112;
    int isc_spb_trusted_role            = 113;
    int isc_spb_verbint                 = 114;
    int isc_spb_auth_block              = 115;
    int isc_spb_auth_plugin_name        = 116;
    int isc_spb_auth_plugin_list        = 117;
    int isc_spb_utf8_filename			= 118;
    int isc_spb_client_version          = 119;
    int isc_spb_remote_protocol         = 120;
    int isc_spb_host_name               = 121;
    int isc_spb_os_user                 = 122;
    int isc_spb_config					= 123;
    int isc_spb_expected_db				= 124;

    int isc_spb_connect_timeout         = isc_dpb_connect_timeout;
    int isc_spb_dummy_packet_interval   = isc_dpb_dummy_packet_interval;
    int isc_spb_sql_role_name           = isc_dpb_sql_role_name;

    // This will not be used in protocol 13, therefore may be reused
    int isc_spb_specific_auth_data      = isc_spb_trusted_auth;

    /*****************************************
     * Parameters for isc_action_svc_nbak    *
     * New with Firebird 2.5
     *****************************************/

    int isc_spb_nbk_level  = 5;
    int isc_spb_nbk_file   = 6;
    int isc_spb_nbk_direct = 7;
    int isc_spb_nbk_no_triggers = 0x01;

    /*****************************
     * Service action items      *
     *****************************/

    int isc_action_svc_backup         = 1;	/* Starts database backup process on the server */
    int isc_action_svc_restore        = 2;	/* Starts database restore process on the server */
    int isc_action_svc_repair         = 3;	/* Starts database repair process on the server */
    int isc_action_svc_add_user       = 4;	/* Adds a new user to the security database */
    int isc_action_svc_delete_user    = 5;	/* Deletes a user record from the security database */
    int isc_action_svc_modify_user    = 6;	/* Modifies a user record in the security database */
    int isc_action_svc_display_user   = 7;	/* Displays a user record from the security database */
    int isc_action_svc_properties     = 8;	/* Sets database properties */
    int isc_action_svc_add_license    = 9;	/* Adds a license to the license file */
    int isc_action_svc_remove_license = 10;	/* Removes a license from the license file */
    int isc_action_svc_db_stats	      = 11;	/* Retrieves database statistics */
    int isc_action_svc_get_ib_log     = 12;	/* Retrieves the InterBase log file from the server */
    // NBackup - New with Firebird 2.5
    int isc_action_svc_nbak   		  = 20; // Starts Nbackup
    int isc_action_svc_nrest  		  = 21; // Restores Nbackup
    // Trace - New with Firebird 2.5
    int isc_action_svc_trace_start    = 22; // Starts a trace
    int isc_action_svc_trace_stop     = 23; // Stops a trace
    int isc_action_svc_trace_suspend  = 24; // Suspends a trace
    int isc_action_svc_trace_resume   = 25; // Resumes a trace
    int isc_action_svc_trace_list     = 26; // Lists all trace sessions
    // RDB$ADMIN mapping - New with Firebird 2.5
    int isc_action_svc_set_mapping    = 27; // Sets RDB$ADMIN auto mapping in security database
    int isc_action_svc_drop_mapping   = 28; // Drops RDB$ADMIN auto mapping in security database
    // Firebird 3
    int isc_action_svc_display_user_adm = 29; // Displays user(s) from security database with admin info
    int isc_action_svc_validate         = 30; // Starts database online validation

    /*****************************************
     * Parameters for isc_action_svc_trace   *
     *****************************************/

    int isc_spb_trc_id    = 1; // relevant for stop, suspend and resume
    int isc_spb_trc_name  = 2; // relevant for start
    int isc_spb_trc_cfg   = 3; // relevant for start

    /*****************************
     * Service information items *
     *****************************/

    int isc_info_svc_svr_db_info      =50;	/* Retrieves the number of attachments and databases */
    int isc_info_svc_get_license      =51;	/* Retrieves all license keys and IDs from the license file */
    int isc_info_svc_get_license_mask =52;	/* Retrieves a bitmask representing licensed options on the server */
    int isc_info_svc_get_config       =53;	/* Retrieves the parameters and values for IB_CONFIG */
    int isc_info_svc_version          =54;	/* Retrieves the version of the services manager */
    int isc_info_svc_server_version   =55;	/* Retrieves the version of the InterBase server */
    int isc_info_svc_implementation   =56;	/* Retrieves the implementation of the InterBase server */
    int isc_info_svc_capabilities     =57;	/* Retrieves a bitmask representing the server's capabilities */
    int isc_info_svc_user_dbpath      =58;	/* Retrieves the path to the security database in use by the server */
    int isc_info_svc_get_env	      =59;	/* Retrieves the setting of $INTERBASE */
    int isc_info_svc_get_env_lock     =60;	/* Retrieves the setting of $INTERBASE_LCK */
    int isc_info_svc_get_env_msg      =61;	/* Retrieves the setting of $INTERBASE_MSG */
    int isc_info_svc_line             =62;	/* Retrieves 1 line of service output per call */
    int isc_info_svc_to_eof           =63;	/* Retrieves as much of the server output as will fit in the supplied buffer */
    int isc_info_svc_timeout          =64;	/* Sets / signifies a timeout value for reading service information */
    int isc_info_svc_get_licensed_users =65;	/* Retrieves the number of users licensed for accessing the server */
    int isc_info_svc_limbo_trans      =66;	/* Retrieve the limbo transactions */
    int isc_info_svc_running          =67;	/* Checks to see if a service is running on an attachment */
    int isc_info_svc_get_users        =68;	/* Returns the user information from isc_action_svc_display_users */
    int isc_info_svc_auth_block       =69;	/* Sets authentication block for service query() call */
    int isc_info_svc_stdin            =78;	/* Returns maximum size of data, needed as stdin for service */

    /******************************************************
     * Parameters for isc_action_{add|delete|modify)_user *
     ******************************************************/

    int isc_spb_sec_userid           = 5;
    int isc_spb_sec_groupid          = 6;
    int isc_spb_sec_username         = 7;
    int isc_spb_sec_password         = 8;
    int isc_spb_sec_groupname        = 9;
    int isc_spb_sec_firstname        = 10;
    int isc_spb_sec_middlename       = 11;
    int isc_spb_sec_lastname         = 12;
    int isc_spb_sec_admin            = 13;

    /*******************************************************
     * Parameters for isc_action_svc_(add|remove)_license, *
     * isc_info_svc_get_license                            *
     *******************************************************/

    int isc_spb_lic_key              = 5;
    int isc_spb_lic_id               = 6;
    int isc_spb_lic_desc             = 7;

    /*****************************************
     * Parameters for isc_action_svc_backup  *
     *****************************************/

    int isc_spb_bkp_file               =  5;
    int isc_spb_bkp_factor             =  6;
    int isc_spb_bkp_length             =  7;
    int isc_spb_bkp_skip_data          =  8;
    int isc_spb_bkp_stat               =  15;
    int isc_spb_bkp_ignore_checksums   =  0x01;
    int isc_spb_bkp_ignore_limbo       =  0x02;
    int isc_spb_bkp_metadata_only      =  0x04;
    int isc_spb_bkp_no_garbage_collect =  0x08;
    int isc_spb_bkp_old_descriptions   =  0x10;
    int isc_spb_bkp_non_transportable  =  0x20;
    int isc_spb_bkp_convert            =  0x40;
    int isc_spb_bkp_expand             =  0x80;
    int isc_spb_bkp_no_triggers        =  0x8000;

    /********************************************
     * Parameters for isc_action_svc_properties *
     ********************************************/

    int isc_spb_prp_page_buffers          = 5;
    int isc_spb_prp_sweep_interval        = 6;
    int isc_spb_prp_shutdown_db	          = 7;
    int isc_spb_prp_deny_new_attachments  = 9;
    int isc_spb_prp_deny_new_transactions = 10;
    int isc_spb_prp_reserve_space		  = 11;
    int isc_spb_prp_write_mode			  = 12;
    int isc_spb_prp_access_mode			  = 13;
    int isc_spb_prp_set_sql_dialect		  = 14;
    int isc_spb_prp_activate			  = 0x0100;
    int isc_spb_prp_db_online			  = 0x0200;

    // New shutdown/online modes - New with Firebird 2.5
    int isc_spb_prp_force_shutdown        = 41;
    int isc_spb_prp_attachments_shutdown  = 42;
    int isc_spb_prp_transactions_shutdown = 43;
    int isc_spb_prp_shutdown_mode         = 44;
    int isc_spb_prp_online_mode           = 45;
    int isc_spb_prp_sm_normal             = 0;
    int isc_spb_prp_sm_multi              = 1;
    int isc_spb_prp_sm_single             = 2;
    int isc_spb_prp_sm_full               = 3;

    /********************************************
     * Parameters for isc_spb_prp_reserve_space *
     ********************************************/

    int isc_spb_prp_res_use_full	=35;
    int isc_spb_prp_res			    =36;

    /******************************************
     * Parameters for isc_spb_prp_write_mode  *
     ******************************************/

    int isc_spb_prp_wm_async		=37;
    int isc_spb_prp_wm_sync			=38;

    /******************************************
     * Parameters for isc_spb_prp_access_mode *
     ******************************************/

    int isc_spb_prp_am_readonly		=39;
    int isc_spb_prp_am_readwrite	=40;

    /*****************************************
     * Parameters for isc_action_svc_repair  *
     *****************************************/

    int isc_spb_rpr_commit_trans	=	15;
    int isc_spb_rpr_rollback_trans	=	34;
    int isc_spb_rpr_recover_two_phase=	17;
    int isc_spb_tra_id				 =  18;
    int isc_spb_single_tra_id		 =  19;
    int isc_spb_multi_tra_id		=	20;
    int isc_spb_tra_state			=	21;
    int isc_spb_tra_state_limbo		=	22;
    int isc_spb_tra_state_commit	=   23;
    int isc_spb_tra_state_rollback	=	24;
    int isc_spb_tra_state_unknown	=   25;
    int isc_spb_tra_host_site		=	26;
    int isc_spb_tra_remote_site		=	27;
    int isc_spb_tra_db_path			=	28;
    int isc_spb_tra_advise			=	29;
    int isc_spb_tra_advise_commit	=	30;
    int isc_spb_tra_advise_rollback	=	31;
    int isc_spb_tra_advise_unknown	=	33;

    int isc_spb_rpr_validate_db		=	0x01;
    int isc_spb_rpr_sweep_db		=	0x02;
    int isc_spb_rpr_mend_db			=	0x04;
    int isc_spb_rpr_list_limbo_trans=	0x08;
    int isc_spb_rpr_check_db		=	0x10;
    int isc_spb_rpr_ignore_checksum	=	0x20;
    int isc_spb_rpr_kill_shadows	=	0x40;
    int isc_spb_rpr_full			=	0x80;

    /*****************************************
     * Parameters for isc_action_svc_restore *
     *****************************************/

    int isc_spb_res_skip_data		=   isc_spb_bkp_skip_data;
    int isc_spb_res_buffers			=	9;
    int isc_spb_res_page_size		=	10;
    int isc_spb_res_length			=	11;
    int isc_spb_res_access_mode		=	12;
    int isc_spb_res_fix_fss_data	=	13;
    int isc_spb_res_fix_fss_metadata=	14;
    int isc_spb_res_deactivate_idx	=	0x0100;
    int isc_spb_res_no_shadow		=	0x0200;
    int isc_spb_res_no_validity		=	0x0400;
    int isc_spb_res_one_at_a_time	=	0x0800;
    int isc_spb_res_replace			=	0x1000;
    int isc_spb_res_create			=	0x2000;
    int isc_spb_res_use_all_space	=	0x4000;

    /*****************************************
     * Parameters for isc_action_svc_validate *
     *****************************************/

    int isc_spb_val_tab_incl     = 1;  // include filter based on regular expression
    int isc_spb_val_tab_excl     = 2;  // exclude filter based on regular expression
    int isc_spb_val_idx_incl     = 3;  // regexp of indices to validate
    int isc_spb_val_idx_excl     = 4;  // regexp of indices to NOT validate
    int isc_spb_val_lock_timeout = 5;  // how long to wait for table lock

    /******************************************
     * Parameters for isc_spb_res_access_mode  *
     ******************************************/

    int isc_spb_res_am_readonly		=	isc_spb_prp_am_readonly;
    int isc_spb_res_am_readwrite	=	isc_spb_prp_am_readwrite;

    /*******************************************
     * Parameters for isc_info_svc_svr_db_info *
     *******************************************/

    int isc_spb_num_att		=	5;
    int isc_spb_num_db		=	6;

    /*****************************************
     * Parameters for isc_info_svc_db_stats  *
     *****************************************/

    int isc_spb_sts_data_pages	=	0x01;
    int isc_spb_sts_db_log		=	0x02;
    int isc_spb_sts_hdr_pages	=	0x04;
    int isc_spb_sts_idx_pages	=	0x08;
    int isc_spb_sts_sys_relations=	0x10;
    int isc_spb_sts_record_versions = 0x20;
    int isc_spb_sts_table       =   0x40;
    int isc_spb_sts_nocreation  =   0x80;

    /****************************/
    /* Common, structural codes */
    /****************************/

    int isc_info_end                    = 1;
    int isc_info_truncated              = 2;
    int isc_info_error                  = 3;
    int isc_info_data_not_ready         = 4;
    int isc_info_length                 = 126;
    int isc_info_flag_end               = 127;

    /*************************/
    /* SQL information items */
    /*************************/

    int isc_info_sql_select              = 4;
    int isc_info_sql_bind                = 5;
    int isc_info_sql_num_variables       = 6;
    int isc_info_sql_describe_vars       = 7;
    int isc_info_sql_describe_end        = 8;
    int isc_info_sql_sqlda_seq           = 9;
    int isc_info_sql_message_seq         = 10;
    int isc_info_sql_type                = 11;
    int isc_info_sql_sub_type            = 12;
    int isc_info_sql_scale               = 13;
    int isc_info_sql_length              = 14;
    int isc_info_sql_null_ind            = 15;
    int isc_info_sql_field               = 16;
    int isc_info_sql_relation            = 17;
    int isc_info_sql_owner               = 18;
    int isc_info_sql_alias               = 19;
    int isc_info_sql_sqlda_start         = 20;
    int isc_info_sql_stmt_type           = 21;
    int isc_info_sql_get_plan            = 22;
    int isc_info_sql_records             = 23;
    int isc_info_sql_batch_fetch         = 24;
    int isc_info_sql_relation_alias      = 25;

    /*********************************/
    /* SQL information return values */
    /*********************************/

    int isc_info_sql_stmt_select         = 1;
    int isc_info_sql_stmt_insert         = 2;
    int isc_info_sql_stmt_update         = 3;
    int isc_info_sql_stmt_delete         = 4;
    int isc_info_sql_stmt_ddl            = 5;
    int isc_info_sql_stmt_get_segment    = 6;
    int isc_info_sql_stmt_put_segment    = 7;
    int isc_info_sql_stmt_exec_procedure = 8;
    int isc_info_sql_stmt_start_trans    = 9;
    int isc_info_sql_stmt_commit         = 10;
    int isc_info_sql_stmt_rollback       = 11;
    int isc_info_sql_stmt_select_for_upd = 12;
    int isc_info_sql_stmt_set_generator  = 13;
    int isc_info_sql_stmt_savepoint      = 14;

    /*****************************/
    /* Request information items */
    /*****************************/

    int isc_info_number_messages        =  4;
    int isc_info_max_message            =  5;
    int isc_info_max_send               =  6;
    int isc_info_max_receive            =  7;
    int isc_info_state                  =  8;
    int isc_info_message_number         =  9;
    int isc_info_message_size           =  10;
    int isc_info_request_cost           =  11;
    int isc_info_access_path            =  12;
    int isc_info_req_select_count       =  13;
    int isc_info_req_insert_count       =  14;
    int isc_info_req_update_count       =  15;
    int isc_info_req_delete_count       =  16;

    /*****************************/
    /* Request information items */
    /*****************************/

    int isc_info_db_id = 4;
    int isc_info_reads = 5;
    int isc_info_writes = 6;
    int isc_info_fetches = 7;
    int isc_info_marks = 8;
    int isc_info_implementation = 11;
    int isc_info_isc_version = 12;
    int isc_info_base_level = 13;
    int isc_info_page_size = 14;
    int isc_info_num_buffers = 15;
    int isc_info_limbo = 16;
    int isc_info_current_memory = 17;
    int isc_info_max_memory = 18;
    int isc_info_window_turns = 19;
    int isc_info_license = 20;
    int isc_info_allocation = 21;
    int isc_info_attachment_id = 22;
    int isc_info_read_seq_count = 23;
    int isc_info_read_idx_count = 24;
    int isc_info_insert_count = 25;
    int isc_info_update_count = 26;
    int isc_info_delete_count = 27;
    int isc_info_backout_count = 28;
    int isc_info_purge_count = 29;
    int isc_info_expunge_count = 30;
    int isc_info_sweep_interval = 31;
    int isc_info_ods_version = 32;
    int isc_info_ods_minor_version = 33;
    int isc_info_no_reserve = 34;
    int isc_info_logfile = 35;
    int isc_info_cur_logfile_name = 36;
    int isc_info_cur_log_part_offset = 37;
    int isc_info_num_wal_buffers = 38;
    int isc_info_wal_buffer_size = 39;
    int isc_info_wal_ckpt_length = 40;
    int isc_info_wal_cur_ckpt_interval = 41;
    int isc_info_wal_prv_ckpt_fname = 42;
    int isc_info_wal_prv_ckpt_poffset = 43;
    int isc_info_wal_recv_ckpt_fname = 44;
    int isc_info_wal_recv_ckpt_poffset = 45;
    int isc_info_wal_grpc_wait_usecs = 47;
    int isc_info_wal_num_io = 48;
    int isc_info_wal_avg_io_size = 49;
    int isc_info_wal_num_commits = 50;
    int isc_info_wal_avg_grpc_size = 51;
    int isc_info_forced_writes = 52;
    int isc_info_user_names = 53;
    int isc_info_page_errors = 54;
    int isc_info_record_errors = 55;
    int isc_info_bpage_errors = 56;
    int isc_info_dpage_errors = 57;
    int isc_info_ipage_errors = 58;
    int isc_info_ppage_errors = 59;
    int isc_info_tpage_errors = 60;
    int isc_info_set_page_buffers = 61;
    int isc_info_db_sql_dialect = 62;
    int isc_info_db_read_only = 63;
    int isc_info_db_size_in_pages = 64;

    /* Values 65-100 unused to avoid conflict with InterBase */

    int frb_info_att_charset = 101;
    int isc_info_db_class = 102;
    int isc_info_firebird_version = 103;
    int isc_info_oldest_transaction = 104;
    int isc_info_oldest_active = 105;
    int isc_info_oldest_snapshot = 106;
    int isc_info_next_transaction = 107;
    int isc_info_db_provider = 108;
    int isc_info_active_transactions = 109;
    int isc_info_active_tran_count = 110;
    int isc_info_creation_date = 111;
    int fb_info_page_contents = 113;

    int isc_info_db_impl_rdb_vms = 1;
    int isc_info_db_impl_rdb_eln = 2;
    int isc_info_db_impl_rdb_eln_dev = 3;
    int isc_info_db_impl_rdb_vms_y = 4;
    int isc_info_db_impl_rdb_eln_y = 5;
    int isc_info_db_impl_jri = 6;
    int isc_info_db_impl_jsv = 7;
    int isc_info_db_impl_isc_apl_68K = 25;
    int isc_info_db_impl_isc_vax_ultr = 26;
    int isc_info_db_impl_isc_vms = 27;
    int isc_info_db_impl_isc_sun_68k = 28;
    int isc_info_db_impl_isc_os2 = 29;
    int isc_info_db_impl_isc_sun4 = 30;
    int isc_info_db_impl_isc_hp_ux = 31;
    int isc_info_db_impl_isc_sun_386i = 32;
    int isc_info_db_impl_isc_vms_orcl = 33;
    int isc_info_db_impl_isc_mac_aux = 34;
    int isc_info_db_impl_isc_rt_aix = 35;
    int isc_info_db_impl_isc_mips_ult = 36;
    int isc_info_db_impl_isc_xenix = 37;
    int isc_info_db_impl_isc_dg = 38;
    int isc_info_db_impl_isc_hp_mpexl = 39;
    int isc_info_db_impl_isc_hp_ux68K = 40;
    int isc_info_db_impl_isc_sgi = 41;
    int isc_info_db_impl_isc_sco_unix = 42;
    int isc_info_db_impl_isc_cray = 43;
    int isc_info_db_impl_isc_imp = 44;
    int isc_info_db_impl_isc_delta = 45;
    int isc_info_db_impl_isc_next = 46;
    int isc_info_db_impl_isc_dos = 47;
    int isc_info_db_impl_m88K = 48;
    int isc_info_db_impl_unixware = 49;
    int isc_info_db_impl_isc_winnt_x86 = 50;
    int isc_info_db_impl_isc_epson = 51;
    int isc_info_db_impl_alpha_osf = 52;
    int isc_info_db_impl_alpha_vms = 53;
    int isc_info_db_impl_netware_386 = 54;
    int isc_info_db_impl_win_only = 55;
    int isc_info_db_impl_ncr_3000 = 56;
    int isc_info_db_impl_winnt_ppc = 57;
    int isc_info_db_impl_dg_x86 = 58;
    int isc_info_db_impl_sco_ev = 59;
    int isc_info_db_impl_i386 = 60;
    int isc_info_db_impl_freebsd = 61;
    int isc_info_db_impl_netbsd = 62;
    int isc_info_db_impl_darwin = 63;
    int isc_info_db_impl_sinixz = 64;
    int isc_info_db_impl_linux_sparc = 65;
    int isc_info_db_impl_linux_amd64 = 66;
    int isc_info_db_impl_winnt_amd64 = 68;
    int isc_info_db_impl_linux_ppc = 69;
    int isc_info_db_impl_darwin_x86 = 70;
    int isc_info_db_impl_linux_mipsel = 71;
    int isc_info_db_impl_linux_mips = 72;
    int isc_info_db_impl_darwin_x64 = 73;
    int isc_info_db_impl_sun_amd64 = 74;
    int isc_info_db_impl_linux_arm = 75;
    int isc_info_db_impl_linux_ia64 = 76;
    int isc_info_db_impl_darwin_ppc64 = 77;
    int isc_info_db_impl_linux_s390x = 78;
    int isc_info_db_impl_linux_s390 = 79;
    int isc_info_db_impl_linux_sh = 80;
    int isc_info_db_impl_linux_sheb = 81;
    int isc_info_db_impl_linux_hppa = 82;
    int isc_info_db_impl_linux_alpha = 83;
    int isc_info_db_impl_linux_arm64 = 84;

    int isc_info_db_class_access = 1;
    int isc_info_db_class_y_valve = 2;
    int isc_info_db_class_rem_int = 3;
    int isc_info_db_class_rem_srvr = 4;
    int isc_info_db_class_pipe_int = 7;
    int isc_info_db_class_pipe_srvr = 8;
    int isc_info_db_class_sam_int = 9;
    int isc_info_db_class_sam_srvr = 10;
    int isc_info_db_class_gateway = 11;
    int isc_info_db_class_cache = 12;
    int isc_info_db_class_classic_access = 13;
    int isc_info_db_class_server_access = 14;

    int isc_info_db_code_rdb_eln = 1;
    int isc_info_db_code_rdb_vms = 2;
    int isc_info_db_code_interbase = 3;
    int isc_info_db_code_firebird = 4;

    /************************/
    /* Blob Parameter Block */
    /************************/

    int isc_bpb_version1                =  1;
    int isc_bpb_source_type             =  1;
    int isc_bpb_target_type             =  2;
    int isc_bpb_type                    =  3;
    int isc_bpb_source_interp           =  4;
    int isc_bpb_target_interp           =  5;
    int isc_bpb_filter_parameter        =  6;

    int isc_bpb_type_segmented          =  0;
    int isc_bpb_type_stream             =  1;

    int RBL_eof              = 1;
    int RBL_segment          = 2;
    int RBL_eof_pending      = 4;
    int RBL_create           = 8;


    /**************************/
    /* Blob information items */
    /**************************/
    int isc_info_blob_num_segments      = 4;
    int isc_info_blob_max_segment       = 5;
    int isc_info_blob_total_length      = 6;
    int isc_info_blob_type              = 7;


    /*********************************/
    /* Transaction information items */
    /*********************************/
    int isc_info_tra_id = 4;
    int isc_info_tra_oldest_interesting = 5;
    int isc_info_tra_oldest_snapshot = 6;
    int isc_info_tra_oldest_active = 7;
    int isc_info_tra_isolation = 8;
    int isc_info_tra_access = 9;
    int isc_info_tra_lock_timeout = 10;

    /****************************************/
    /* Cancel types for fb_cancel_operation */
    /****************************************/
    int fb_cancel_disable = 1;
    int fb_cancel_enable = 2;
    int fb_cancel_raise = 3;
    int fb_cancel_abort = 4;


    /********************/
    /* ISC Error Codes */
    /*******************/

    int SUCCESS = 0;

    int isc_facility = 20;
    int isc_err_base = 335544320;
    int isc_err_factor = 1;
    int isc_arg_end = 0;           /* end of argument list */
    int isc_arg_gds = 1;           /* generic DSRI status value */
    int isc_arg_string = 2;        /* string argument */
    int isc_arg_cstring = 3;       /* count & string argument */
    int isc_arg_number = 4;        /* numeric argument (long) */
    int isc_arg_interpreted = 5;   /* interpreted status code (string) */
    int isc_arg_vms = 6;           /* VAX/VMS status code (long) */
    int isc_arg_unix = 7;          /* UNIX error code */
    int isc_arg_domain = 8;        /* Apollo/Domain error code */
    int isc_arg_dos = 9;           /* MSDOS/OS2 error code */
    int isc_arg_mpexl = 10;        /* HP MPE/XL error code */
    int isc_arg_mpexl_ipc = 11;    /* HP MPE/XL IPC error code */
    int isc_arg_next_mach = 15;    /* NeXT/Mach error code */
    int isc_arg_netware = 16;      /* NetWare error code */
    int isc_arg_win32 = 17;        /* Win32 error code */
    int isc_arg_warning = 18;      /* warning argument */
    int isc_arg_sql_state = 19;    /* SQLSTATE */

    int isc_arith_except                     = 335544321;
    int isc_bad_dbkey                        = 335544322;
    int isc_bad_db_format                    = 335544323;
    int isc_bad_db_handle                    = 335544324;
    int isc_bad_dpb_content                  = 335544325;
    int isc_bad_dpb_form                     = 335544326;
    int isc_bad_req_handle                   = 335544327;
    int isc_bad_segstr_handle                = 335544328;
    int isc_bad_segstr_id                    = 335544329;
    int isc_bad_tpb_content                  = 335544330;
    int isc_bad_tpb_form                     = 335544331;
    int isc_bad_trans_handle                 = 335544332;
    int isc_bug_check                        = 335544333;
    int isc_convert_error                    = 335544334;
    int isc_db_corrupt                       = 335544335;
    int isc_deadlock                         = 335544336;
    int isc_excess_trans                     = 335544337;
    int isc_from_no_match                    = 335544338;
    int isc_infinap                          = 335544339;
    int isc_infona                           = 335544340;
    int isc_infunk                           = 335544341;
    int isc_integ_fail                       = 335544342;
    int isc_invalid_blr                      = 335544343;
    int isc_io_error                         = 335544344;
    int isc_lock_conflict                    = 335544345;
    int isc_metadata_corrupt                 = 335544346;
    int isc_not_valid                        = 335544347;
    int isc_no_cur_rec                       = 335544348;
    int isc_no_dup                           = 335544349;
    int isc_no_finish                        = 335544350;
    int isc_no_meta_update                   = 335544351;
    int isc_no_priv                          = 335544352;
    int isc_no_recon                         = 335544353;
    int isc_no_record                        = 335544354;
    int isc_no_segstr_close                  = 335544355;
    int isc_obsolete_metadata                = 335544356;
    int isc_open_trans                       = 335544357;
    int isc_port_len                         = 335544358;
    int isc_read_only_field                  = 335544359;
    int isc_read_only_rel                    = 335544360;
    int isc_read_only_trans                  = 335544361;
    int isc_read_only_view                   = 335544362;
    int isc_req_no_trans                     = 335544363;
    int isc_req_sync                         = 335544364;
    int isc_req_wrong_db                     = 335544365;
    int isc_segment                          = 335544366;
    int isc_segstr_eof                       = 335544367;
    int isc_segstr_no_op                     = 335544368;
    int isc_segstr_no_read                   = 335544369;
    int isc_segstr_no_trans                  = 335544370;
    int isc_segstr_no_write                  = 335544371;
    int isc_segstr_wrong_db                  = 335544372;
    int isc_sys_request                      = 335544373;
    int isc_stream_eof                       = 335544374;
    int isc_unavailable                      = 335544375;
    int isc_unres_rel                        = 335544376;
    int isc_uns_ext                          = 335544377;
    int isc_wish_list                        = 335544378;
    int isc_wrong_ods                        = 335544379;
    int isc_wronumarg                        = 335544380;
    int isc_imp_exc                          = 335544381;
    int isc_random                           = 335544382;
    int isc_fatal_conflict                   = 335544383;
    int isc_badblk                           = 335544384;
    int isc_invpoolcl                        = 335544385;
    int isc_nopoolids                        = 335544386;
    int isc_relbadblk                        = 335544387;
    int isc_blktoobig                        = 335544388;
    int isc_bufexh                           = 335544389;
    int isc_syntaxerr                        = 335544390;
    int isc_bufinuse                         = 335544391;
    int isc_bdbincon                         = 335544392;
    int isc_reqinuse                         = 335544393;
    int isc_badodsver                        = 335544394;
    int isc_relnotdef                        = 335544395;
    int isc_fldnotdef                        = 335544396;
    int isc_dirtypage                        = 335544397;
    int isc_waifortra                        = 335544398;
    int isc_doubleloc                        = 335544399;
    int isc_nodnotfnd                        = 335544400;
    int isc_dupnodfnd                        = 335544401;
    int isc_locnotmar                        = 335544402;
    int isc_badpagtyp                        = 335544403;
    int isc_corrupt                          = 335544404;
    int isc_badpage                          = 335544405;
    int isc_badindex                         = 335544406;
    int isc_dbbnotzer                        = 335544407;
    int isc_tranotzer                        = 335544408;
    int isc_trareqmis                        = 335544409;
    int isc_badhndcnt                        = 335544410;
    int isc_wrotpbver                        = 335544411;
    int isc_wroblrver                        = 335544412;
    int isc_wrodpbver                        = 335544413;
    int isc_blobnotsup                       = 335544414;
    int isc_badrelation                      = 335544415;
    int isc_nodetach                         = 335544416;
    int isc_notremote                        = 335544417;
    int isc_trainlim                         = 335544418;
    int isc_notinlim                         = 335544419;
    int isc_traoutsta                        = 335544420;
    int isc_connect_reject                   = 335544421;
    int isc_dbfile                           = 335544422;
    int isc_orphan                           = 335544423;
    int isc_no_lock_mgr                      = 335544424;
    int isc_ctxinuse                         = 335544425;
    int isc_ctxnotdef                        = 335544426;
    int isc_datnotsup                        = 335544427;
    int isc_badmsgnum                        = 335544428;
    int isc_badparnum                        = 335544429;
    int isc_virmemexh                        = 335544430;
    int isc_blocking_signal                  = 335544431;
    int isc_lockmanerr                       = 335544432;
    int isc_journerr                         = 335544433;
    int isc_keytoobig                        = 335544434;
    int isc_nullsegkey                       = 335544435;
    int isc_sqlerr                           = 335544436;
    int isc_wrodynver                        = 335544437;
    int isc_funnotdef                        = 335544438;
    int isc_funmismat                        = 335544439;
    int isc_bad_msg_vec                      = 335544440;
    int isc_bad_detach                       = 335544441;
    int isc_noargacc_read                    = 335544442;
    int isc_noargacc_write                   = 335544443;
    int isc_read_only                        = 335544444;
    int isc_ext_err                          = 335544445;
    int isc_non_updatable                    = 335544446;
    int isc_no_rollback                      = 335544447;
    int isc_bad_sec_info                     = 335544448;
    int isc_invalid_sec_info                 = 335544449;
    int isc_misc_interpreted                 = 335544450;
    int isc_update_conflict                  = 335544451;
    int isc_unlicensed                       = 335544452;
    int isc_obj_in_use                       = 335544453;
    int isc_nofilter                         = 335544454;
    int isc_shadow_accessed                  = 335544455;
    int isc_invalid_sdl                      = 335544456;
    int isc_out_of_bounds                    = 335544457;
    int isc_invalid_dimension                = 335544458;
    int isc_rec_in_limbo                     = 335544459;
    int isc_shadow_missing                   = 335544460;
    int isc_cant_validate                    = 335544461;
    int isc_cant_start_journal               = 335544462;
    int isc_gennotdef                        = 335544463;
    int isc_cant_start_logging               = 335544464;
    int isc_bad_segstr_type                  = 335544465;
    int isc_foreign_key                      = 335544466;
    int isc_high_minor                       = 335544467;
    int isc_tra_state                        = 335544468;
    int isc_trans_invalid                    = 335544469;
    int isc_buf_invalid                      = 335544470;
    int isc_indexnotdefined                  = 335544471;
    int isc_login                            = 335544472;
    int isc_invalid_bookmark                 = 335544473;
    int isc_bad_lock_level                   = 335544474;
    int isc_relation_lock                    = 335544475;
    int isc_record_lock                      = 335544476;
    int isc_max_idx                          = 335544477;
    int isc_jrn_enable                       = 335544478;
    int isc_old_failure                      = 335544479;
    int isc_old_in_progress                  = 335544480;
    int isc_old_no_space                     = 335544481;
    int isc_no_wal_no_jrn                    = 335544482;
    int isc_num_old_files                    = 335544483;
    int isc_wal_file_open                    = 335544484;
    int isc_bad_stmt_handle                  = 335544485;
    int isc_wal_failure                      = 335544486;
    int isc_walw_err                         = 335544487;
    int isc_logh_small                       = 335544488;
    int isc_logh_inv_version                 = 335544489;
    int isc_logh_open_flag                   = 335544490;
    int isc_logh_open_flag2                  = 335544491;
    int isc_logh_diff_dbname                 = 335544492;
    int isc_logf_unexpected_eof              = 335544493;
    int isc_logr_incomplete                  = 335544494;
    int isc_logr_header_small                = 335544495;
    int isc_logb_small                       = 335544496;
    int isc_wal_illegal_attach               = 335544497;
    int isc_wal_invalid_wpb                  = 335544498;
    int isc_wal_err_rollover                 = 335544499;
    int isc_no_wal                           = 335544500;
    int isc_drop_wal                         = 335544501;
    int isc_stream_not_defined               = 335544502;
    int isc_wal_subsys_error                 = 335544503;
    int isc_wal_subsys_corrupt               = 335544504;
    int isc_no_archive                       = 335544505;
    int isc_shutinprog                       = 335544506;
    int isc_range_in_use                     = 335544507;
    int isc_range_not_found                  = 335544508;
    int isc_charset_not_found                = 335544509;
    int isc_lock_timeout                     = 335544510;
    int isc_prcnotdef                        = 335544511;
    int isc_prcmismat                        = 335544512;
    int isc_wal_bugcheck                     = 335544513;
    int isc_wal_cant_expand                  = 335544514;
    int isc_codnotdef                        = 335544515;
    int isc_xcpnotdef                        = 335544516;
    int isc_except                           = 335544517;
    int isc_cache_restart                    = 335544518;
    int isc_bad_lock_handle                  = 335544519;
    int isc_jrn_present                      = 335544520;
    int isc_wal_err_rollover2                = 335544521;
    int isc_wal_err_logwrite                 = 335544522;
    int isc_wal_err_jrn_comm                 = 335544523;
    int isc_wal_err_expansion                = 335544524;
    int isc_wal_err_setup                    = 335544525;
    int isc_wal_err_ww_sync                  = 335544526;
    int isc_wal_err_ww_start                 = 335544527;
    int isc_shutdown                         = 335544528;
    int isc_existing_priv_mod                = 335544529;
    int isc_primary_key_ref                  = 335544530;
    int isc_primary_key_notnull              = 335544531;
    int isc_ref_cnstrnt_notfound             = 335544532;
    int isc_foreign_key_notfound             = 335544533;
    int isc_ref_cnstrnt_update               = 335544534;
    int isc_check_cnstrnt_update             = 335544535;
    int isc_check_cnstrnt_del                = 335544536;
    int isc_integ_index_seg_del              = 335544537;
    int isc_integ_index_seg_mod              = 335544538;
    int isc_integ_index_del                  = 335544539;
    int isc_integ_index_mod                  = 335544540;
    int isc_check_trig_del                   = 335544541;
    int isc_check_trig_update                = 335544542;
    int isc_cnstrnt_fld_del                  = 335544543;
    int isc_cnstrnt_fld_rename               = 335544544;
    int isc_rel_cnstrnt_update               = 335544545;
    int isc_constaint_on_view                = 335544546;
    int isc_invld_cnstrnt_type               = 335544547;
    int isc_primary_key_exists               = 335544548;
    int isc_systrig_update                   = 335544549;
    int isc_not_rel_owner                    = 335544550;
    int isc_grant_obj_notfound               = 335544551;
    int isc_grant_fld_notfound               = 335544552;
    int isc_grant_nopriv                     = 335544553;
    int isc_nonsql_security_rel              = 335544554;
    int isc_nonsql_security_fld              = 335544555;
    int isc_wal_cache_err                    = 335544556;
    int isc_shutfail                         = 335544557;
    int isc_check_constraint                 = 335544558;
    int isc_bad_svc_handle                   = 335544559;
    int isc_shutwarn                         = 335544560;
    int isc_wrospbver                        = 335544561;
    int isc_bad_spb_form                     = 335544562;
    int isc_svcnotdef                        = 335544563;
    int isc_no_jrn                           = 335544564;
    int isc_transliteration_failed           = 335544565;
    int isc_start_cm_for_wal                 = 335544566;
    int isc_wal_ovflow_log_required          = 335544567;
    int isc_text_subtype                     = 335544568;
    int isc_dsql_error                       = 335544569;
    int isc_dsql_command_err                 = 335544570;
    int isc_dsql_constant_err                = 335544571;
    int isc_dsql_cursor_err                  = 335544572;
    int isc_dsql_datatype_err                = 335544573;
    int isc_dsql_decl_err                    = 335544574;
    int isc_dsql_cursor_update_err           = 335544575;
    int isc_dsql_cursor_open_err             = 335544576;
    int isc_dsql_cursor_close_err            = 335544577;
    int isc_dsql_field_err                   = 335544578;
    int isc_dsql_internal_err                = 335544579;
    int isc_dsql_relation_err                = 335544580;
    int isc_dsql_procedure_err               = 335544581;
    int isc_dsql_request_err                 = 335544582;
    int isc_dsql_sqlda_err                   = 335544583;
    int isc_dsql_var_count_err               = 335544584;
    int isc_dsql_stmt_handle                 = 335544585;
    int isc_dsql_function_err                = 335544586;
    int isc_dsql_blob_err                    = 335544587;
    int isc_collation_not_found              = 335544588;
    int isc_collation_not_for_charset        = 335544589;
    int isc_dsql_dup_option                  = 335544590;
    int isc_dsql_tran_err                    = 335544591;
    int isc_dsql_invalid_array               = 335544592;
    int isc_dsql_max_arr_dim_exceeded        = 335544593;
    int isc_dsql_arr_range_error             = 335544594;
    int isc_dsql_trigger_err                 = 335544595;
    int isc_dsql_subselect_err               = 335544596;
    int isc_dsql_crdb_prepare_err            = 335544597;
    int isc_specify_field_err                = 335544598;
    int isc_num_field_err                    = 335544599;
    int isc_col_name_err                     = 335544600;
    int isc_where_err                        = 335544601;
    int isc_table_view_err                   = 335544602;
    int isc_distinct_err                     = 335544603;
    int isc_key_field_count_err              = 335544604;
    int isc_subquery_err                     = 335544605;
    int isc_expression_eval_err              = 335544606;
    int isc_node_err                         = 335544607;
    int isc_command_end_err                  = 335544608;
    int isc_index_name                       = 335544609;
    int isc_exception_name                   = 335544610;
    int isc_field_name                       = 335544611;
    int isc_token_err                        = 335544612;
    int isc_union_err                        = 335544613;
    int isc_dsql_construct_err               = 335544614;
    int isc_field_aggregate_err              = 335544615;
    int isc_field_ref_err                    = 335544616;
    int isc_order_by_err                     = 335544617;
    int isc_return_mode_err                  = 335544618;
    int isc_extern_func_err                  = 335544619;
    int isc_alias_conflict_err               = 335544620;
    int isc_procedure_conflict_error         = 335544621;
    int isc_relation_conflict_err            = 335544622;
    int isc_dsql_domain_err                  = 335544623;
    int isc_idx_seg_err                      = 335544624;
    int isc_node_name_err                    = 335544625;
    int isc_table_name                       = 335544626;
    int isc_proc_name                        = 335544627;
    int isc_idx_create_err                   = 335544628;
    int isc_wal_shadow_err                   = 335544629;
    int isc_dependency                       = 335544630;
    int isc_idx_key_err                      = 335544631;
    int isc_dsql_file_length_err             = 335544632;
    int isc_dsql_shadow_number_err           = 335544633;
    int isc_dsql_token_unk_err               = 335544634;
    int isc_dsql_no_relation_alias           = 335544635;
    int isc_indexname                        = 335544636;
    int isc_no_stream_plan                   = 335544637;
    int isc_stream_twice                     = 335544638;
    int isc_stream_not_found                 = 335544639;
    int isc_collation_requires_text          = 335544640;
    int isc_dsql_domain_not_found            = 335544641;
    int isc_index_unused                     = 335544642;
    int isc_dsql_self_join                   = 335544643;
    int isc_stream_bof                       = 335544644;
    int isc_stream_crack                     = 335544645;
    int isc_db_or_file_exists                = 335544646;
    int isc_invalid_operator                 = 335544647;
    int isc_conn_lost                        = 335544648;
    int isc_bad_checksum                     = 335544649;
    int isc_page_type_err                    = 335544650;
    int isc_ext_readonly_err                 = 335544651;
    int isc_sing_select_err                  = 335544652;
    int isc_psw_attach                       = 335544653;
    int isc_psw_start_trans                  = 335544654;
    int isc_invalid_direction                = 335544655;
    int isc_dsql_var_conflict                = 335544656;
    int isc_dsql_no_blob_array               = 335544657;
    int isc_dsql_base_table                  = 335544658;
    int isc_duplicate_base_table             = 335544659;
    int isc_view_alias                       = 335544660;
    int isc_index_root_page_full             = 335544661;
    int isc_dsql_blob_type_unknown           = 335544662;
    int isc_req_max_clones_exceeded          = 335544663;
    int isc_dsql_duplicate_spec              = 335544664;
    int isc_unique_key_violation             = 335544665;
    int isc_srvr_version_too_old             = 335544666;
    int isc_drdb_completed_with_errs         = 335544667;
    int isc_dsql_procedure_use_err           = 335544668;
    int isc_dsql_count_mismatch              = 335544669;
    int isc_blob_idx_err                     = 335544670;
    int isc_array_idx_err                    = 335544671;
    int isc_key_field_err                    = 335544672;
    int isc_no_delete                        = 335544673;
    int isc_del_last_field                   = 335544674;
    int isc_sort_err                         = 335544675;
    int isc_sort_mem_err                     = 335544676;
    int isc_version_err                      = 335544677;
    int isc_inval_key_posn                   = 335544678;
    int isc_no_segments_err                  = 335544679;
    int isc_crrp_data_err                    = 335544680;
    int isc_rec_size_err                     = 335544681;
    int isc_dsql_field_ref                   = 335544682;
    int isc_req_depth_exceeded               = 335544683;
    int isc_no_field_access                  = 335544684;
    int isc_no_dbkey                         = 335544685;
    int isc_jrn_format_err                   = 335544686;
    int isc_jrn_file_full                    = 335544687;
    int isc_dsql_open_cursor_request         = 335544688;
    int isc_ib_error                         = 335544689;
    int isc_cache_redef                      = 335544690;
    int isc_cache_too_small                  = 335544691;
    int isc_log_redef                        = 335544692;
    int isc_log_too_small                    = 335544693;
    int isc_partition_too_small              = 335544694;
    int isc_partition_not_supp               = 335544695;
    int isc_log_length_spec                  = 335544696;
    int isc_precision_err                    = 335544697;
    int isc_scale_nogt                       = 335544698;
    int isc_expec_short                      = 335544699;
    int isc_expec_long                       = 335544700;
    int isc_expec_ushort                     = 335544701;
    int isc_escape_invalid                   = 335544702;
    int isc_svcnoexe                         = 335544703;
    int isc_net_lookup_err                   = 335544704;
    int isc_service_unknown                  = 335544705;
    int isc_host_unknown                     = 335544706;
    int isc_grant_nopriv_on_base             = 335544707;
    int isc_dyn_fld_ambiguous                = 335544708;
    int isc_dsql_agg_ref_err                 = 335544709;
    int isc_complex_view                     = 335544710;
    int isc_unprepared_stmt                  = 335544711;
    int isc_expec_positive                   = 335544712;
    int isc_dsql_sqlda_value_err             = 335544713;
    int isc_invalid_array_id                 = 335544714;
    int isc_extfile_uns_op                   = 335544715;
    int isc_svc_in_use                       = 335544716;
    int isc_err_stack_limit                  = 335544717;
    int isc_invalid_key                      = 335544718;
    int isc_net_init_error                   = 335544719;
    int isc_loadlib_failure                  = 335544720;
    int isc_network_error                    = 335544721;
    int isc_net_connect_err                  = 335544722;
    int isc_net_connect_listen_err           = 335544723;
    int isc_net_event_connect_err            = 335544724;
    int isc_net_event_listen_err             = 335544725;
    int isc_net_read_err                     = 335544726;
    int isc_net_write_err                    = 335544727;
    int isc_integ_index_deactivate           = 335544728;
    int isc_integ_deactivate_primary         = 335544729;
    int isc_cse_not_supported                = 335544730;
    int isc_tra_must_sweep                   = 335544731;
    int isc_unsupported_network_drive        = 335544732;
    int isc_io_create_err                    = 335544733;
    int isc_io_open_err                      = 335544734;
    int isc_io_close_err                     = 335544735;
    int isc_io_read_err                      = 335544736;
    int isc_io_write_err                     = 335544737;
    int isc_io_delete_err                    = 335544738;
    int isc_io_access_err                    = 335544739;
    int isc_udf_exception                    = 335544740;
    int isc_lost_db_connection               = 335544741;
    int isc_no_write_user_priv               = 335544742;
    int isc_token_too_long                   = 335544743;
    int isc_max_att_exceeded                 = 335544744;
    int isc_login_same_as_role_name          = 335544745;
    int isc_reftable_requires_pk             = 335544746;
    int isc_usrname_too_long                 = 335544747;
    int isc_password_too_long                = 335544748;
    int isc_usrname_required                 = 335544749;
    int isc_password_required                = 335544750;
    int isc_bad_protocol                     = 335544751;
    int isc_dup_usrname_found                = 335544752;
    int isc_usrname_not_found                = 335544753;
    int isc_error_adding_sec_record          = 335544754;
    int isc_error_modifying_sec_record       = 335544755;
    int isc_error_deleting_sec_record        = 335544756;
    int isc_error_updating_sec_db            = 335544757;
    int isc_sort_rec_size_err                = 335544758;
    int isc_bad_default_value                = 335544759;
    int isc_invalid_clause                   = 335544760;
    int isc_too_many_handles                 = 335544761;
    int isc_optimizer_blk_exc                = 335544762;
    int isc_invalid_string_constant          = 335544763;
    int isc_transitional_date                = 335544764;
    int isc_read_only_database               = 335544765;
    int isc_must_be_dialect_2_and_up         = 335544766;
    int isc_blob_filter_exception            = 335544767;
    int isc_exception_access_violation       = 335544768;
    int isc_exception_datatype_missalignment = 335544769;
    int isc_exception_array_bounds_exceeded  = 335544770;
    int isc_exception_float_denormal_operand = 335544771;
    int isc_exception_float_divide_by_zero   = 335544772;
    int isc_exception_float_inexact_result   = 335544773;
    int isc_exception_float_invalid_operand  = 335544774;
    int isc_exception_float_overflow         = 335544775;
    int isc_exception_float_stack_check      = 335544776;
    int isc_exception_float_underflow        = 335544777;
    int isc_exception_integer_divide_by_zero = 335544778;
    int isc_exception_integer_overflow       = 335544779;
    int isc_exception_unknown                = 335544780;
    int isc_exception_stack_overflow         = 335544781;
    int isc_exception_sigsegv                = 335544782;
    int isc_exception_sigill                 = 335544783;
    int isc_exception_sigbus                 = 335544784;
    int isc_exception_sigfpe                 = 335544785;
    int isc_ext_file_delete                  = 335544786;
    int isc_ext_file_modify                  = 335544787;
    int isc_adm_task_denied                  = 335544788;
    int isc_extract_input_mismatch           = 335544789;
    int isc_insufficient_svc_privileges      = 335544790;
    int isc_file_in_use                      = 335544791;
    int isc_service_att_err                  = 335544792;
    int isc_ddl_not_allowed_by_db_sql_dial   = 335544793;
    int isc_cancelled                        = 335544794;
    int isc_unexp_spb_form                   = 335544795;
    int isc_sql_dialect_datatype_unsupport   = 335544796;
    int isc_svcnouser                        = 335544797;
    int isc_depend_on_uncommitted_rel        = 335544798;
    int isc_svc_name_missing                 = 335544799;
    int isc_too_many_contexts                = 335544800;
    int isc_datype_notsup                    = 335544801;
    int isc_dialect_reset_warning            = 335544802;
    int isc_dialect_not_changed              = 335544803;
    int isc_database_create_failed           = 335544804;
    int isc_inv_dialect_specified            = 335544805;
    int isc_valid_db_dialects                = 335544806;
    int isc_sqlwarn                          = 335544807;
    int isc_dtype_renamed                    = 335544808;
    int isc_extern_func_dir_error            = 335544809;
    int isc_date_range_exceeded              = 335544810;
    int isc_inv_client_dialect_specified     = 335544811;
    int isc_valid_client_dialects            = 335544812;
    int isc_optimizer_between_err            = 335544813;
    int isc_service_not_supported            = 335544814;
    int isc_generator_name                   = 335544815;
    int isc_udf_name                         = 335544816;
    int isc_bad_limit_param                  = 335544817;
    int isc_bad_skip_param                   = 335544818;
    int isc_io_32bit_exceeded_err            = 335544819;
    int isc_invalid_savepoint                = 335544820;
    int isc_dsql_column_pos_err              = 335544821;
    int isc_dsql_agg_where_err               = 335544822;
    int isc_dsql_agg_group_err               = 335544823;
    int isc_dsql_agg_column_err              = 335544824;
    int isc_dsql_agg_having_err              = 335544825;
    int isc_dsql_agg_nested_err              = 335544826;
    int isc_exec_sql_invalid_arg             = 335544827;
    int isc_exec_sql_invalid_req             = 335544828;
    int isc_exec_sql_invalid_var             = 335544829;
    int isc_exec_sql_max_call_exceeded       = 335544830;
    int isc_conf_access_denied               = 335544831;
    int isc_wrong_backup_state               = 335544832;
    int isc_wal_backup_err                   = 335544833;
    int isc_cursor_not_open                  = 335544834;
    int isc_bad_shutdown_mode                = 335544835;
    int isc_concat_overflow                  = 335544836;
    int isc_bad_substring_offset             = 335544837;
    int isc_foreign_key_target_doesnt_exist  = 335544838;
    int isc_foreign_key_references_present   = 335544839;
    int isc_no_update                        = 335544840;
    int isc_cursor_already_open              = 335544841;
    int isc_stack_trace                      = 335544842;
    int isc_ctx_var_not_found                = 335544843;
    int isc_ctx_namespace_invalid            = 335544844;
    int isc_ctx_too_big                      = 335544845;
    int isc_ctx_bad_argument                 = 335544846;
    int isc_identifier_too_long              = 335544847;
    int isc_except2                          = 335544848;
    int isc_malformed_string                 = 335544849;
    int isc_prc_out_param_mismatch           = 335544850;
    int isc_command_end_err2                 = 335544851;
    int isc_partner_idx_incompat_type        = 335544852;
    int isc_bad_substring_length             = 335544853;
    int isc_charset_not_installed            = 335544854;
    int isc_collation_not_installed          = 335544855;
    int isc_att_shutdown                     = 335544856;
    int isc_blobtoobig                       = 335544857;
    int isc_must_have_phys_field             = 335544858;
    int isc_invalid_time_precision           = 335544859;
    int isc_blob_convert_error               = 335544860;
    int isc_array_convert_error              = 335544861;
    int isc_record_lock_not_supp             = 335544862;
    int isc_partner_idx_not_found            = 335544863;
    int isc_tra_num_exc                      = 335544864;
    int isc_field_disappeared                = 335544865;
    int isc_met_wrong_gtt_scope              = 335544866;
    int isc_subtype_for_internal_use         = 335544867;
    int isc_illegal_prc_type                 = 335544868;
    int isc_invalid_sort_datatype            = 335544869;
    int isc_collation_name                   = 335544870;
    int isc_domain_name                      = 335544871;
    int isc_domnotdef                        = 335544872;
    int isc_array_max_dimensions             = 335544873;
    int isc_max_db_per_trans_allowed         = 335544874;
    int isc_bad_debug_format                 = 335544875;
    int isc_bad_proc_BLR                     = 335544876;
    int isc_key_too_big                      = 335544877;
    int isc_concurrent_transaction           = 335544878;
    int isc_not_valid_for_var                = 335544879;
    int isc_not_valid_for                    = 335544880;
    int isc_need_difference                  = 335544881;
    int isc_long_login                       = 335544882;
    int isc_fldnotdef2                       = 335544883;
    int isc_invalid_similar_pattern          = 335544884;
    int isc_bad_teb_form                     = 335544885;
    int isc_tpb_multiple_txn_isolation       = 335544886;
    int isc_tpb_reserv_before_table          = 335544887;
    int isc_tpb_multiple_spec                = 335544888;
    int isc_tpb_option_without_rc            = 335544889;
    int isc_tpb_conflicting_options          = 335544890;
    int isc_tpb_reserv_missing_tlen          = 335544891;
    int isc_tpb_reserv_long_tlen             = 335544892;
    int isc_tpb_reserv_missing_tname         = 335544893;
    int isc_tpb_reserv_corrup_tlen           = 335544894;
    int isc_tpb_reserv_null_tlen             = 335544895;
    int isc_tpb_reserv_relnotfound           = 335544896;
    int isc_tpb_reserv_baserelnotfound       = 335544897;
    int isc_tpb_missing_len                  = 335544898;
    int isc_tpb_missing_value                = 335544899;
    int isc_tpb_corrupt_len                  = 335544900;
    int isc_tpb_null_len                     = 335544901;
    int isc_tpb_overflow_len                 = 335544902;
    int isc_tpb_invalid_value                = 335544903;
    int isc_tpb_reserv_stronger_wng          = 335544904;
    int isc_tpb_reserv_stronger              = 335544905;
    int isc_tpb_reserv_max_recursion         = 335544906;
    int isc_tpb_reserv_virtualtbl            = 335544907;
    int isc_tpb_reserv_systbl                = 335544908;
    int isc_tpb_reserv_temptbl               = 335544909;
    int isc_tpb_readtxn_after_writelock      = 335544910;
    int isc_tpb_writelock_after_readtxn      = 335544911;
    int isc_time_range_exceeded              = 335544912;
    int isc_datetime_range_exceeded          = 335544913;
    int isc_string_truncation                = 335544914;
    int isc_blob_truncation                  = 335544915;
    int isc_numeric_out_of_range             = 335544916;
    int isc_shutdown_timeout                 = 335544917;
    int isc_att_handle_busy                  = 335544918;
    int isc_bad_udf_freeit                   = 335544919;
    int isc_eds_provider_not_found           = 335544920;
    int isc_eds_connection                   = 335544921;
    int isc_eds_preprocess                   = 335544922;
    int isc_eds_stmt_expected                = 335544923;
    int isc_eds_prm_name_expected            = 335544924;
    int isc_eds_unclosed_comment             = 335544925;
    int isc_eds_statement                    = 335544926;
    int isc_eds_input_prm_mismatch           = 335544927;
    int isc_eds_output_prm_mismatch          = 335544928;
    int isc_eds_input_prm_not_set            = 335544929;
    int isc_too_big_blr                      = 335544930;
    int isc_montabexh                        = 335544931;
    int isc_modnotfound                      = 335544932;
    int isc_nothing_to_cancel                = 335544933;
    int isc_ibutil_not_loaded                = 335544934;
    int isc_circular_computed                = 335544935;
    int isc_psw_db_error                     = 335544936;
    int isc_invalid_type_datetime_op         = 335544937;
    int isc_onlycan_add_timetodate           = 335544938;
    int isc_onlycan_add_datetotime           = 335544939;
    int isc_onlycansub_tstampfromtstamp      = 335544940;
    int isc_onlyoneop_mustbe_tstamp          = 335544941;
    int isc_invalid_extractpart_time         = 335544942;
    int isc_invalid_extractpart_date         = 335544943;
    int isc_invalidarg_extract               = 335544944;
    int isc_sysf_argmustbe_exact             = 335544945;
    int isc_sysf_argmustbe_exact_or_fp       = 335544946;
    int isc_sysf_argviolates_uuidtype        = 335544947;
    int isc_sysf_argviolates_uuidlen         = 335544948;
    int isc_sysf_argviolates_uuidfmt         = 335544949;
    int isc_sysf_argviolates_guidigits       = 335544950;
    int isc_sysf_invalid_addpart_time        = 335544951;
    int isc_sysf_invalid_add_datetime        = 335544952;
    int isc_sysf_invalid_addpart_dtime       = 335544953;
    int isc_sysf_invalid_add_dtime_rc        = 335544954;
    int isc_sysf_invalid_diff_dtime          = 335544955;
    int isc_sysf_invalid_timediff            = 335544956;
    int isc_sysf_invalid_tstamptimediff      = 335544957;
    int isc_sysf_invalid_datetimediff        = 335544958;
    int isc_sysf_invalid_diffpart            = 335544959;
    int isc_sysf_argmustbe_positive          = 335544960;
    int isc_sysf_basemustbe_positive         = 335544961;
    int isc_sysf_argnmustbe_nonneg           = 335544962;
    int isc_sysf_argnmustbe_positive         = 335544963;
    int isc_sysf_invalid_zeropowneg          = 335544964;
    int isc_sysf_invalid_negpowfp            = 335544965;
    int isc_sysf_invalid_scale               = 335544966;
    int isc_sysf_argmustbe_nonneg            = 335544967;
    int isc_sysf_binuuid_mustbe_str          = 335544968;
    int isc_sysf_binuuid_wrongsize           = 335544969;
    int isc_missing_required_spb             = 335544970;
    int isc_net_server_shutdown              = 335544971;
    int isc_bad_conn_str                     = 335544972;
    int isc_bad_epb_form                     = 335544973;
    int isc_no_threads                       = 335544974;
    int isc_net_event_connect_timeout        = 335544975;
    int isc_sysf_argmustbe_nonzero           = 335544976;
    int isc_sysf_argmustbe_range_inc1_1      = 335544977;
    int isc_sysf_argmustbe_gteq_one          = 335544978;
    int isc_sysf_argmustbe_range_exc1_1      = 335544979;
    int isc_internal_rejected_params         = 335544980;
    int isc_sysf_fp_overflow                 = 335544981;
    int isc_udf_fp_overflow                  = 335544982;
    int isc_udf_fp_nan                       = 335544983;
    int isc_instance_conflict                = 335544984;
    int isc_out_of_temp_space                = 335544985;
    int isc_eds_expl_tran_ctrl               = 335544986;
    int isc_no_trusted_spb                   = 335544987;
    int isc_package_name                     = 335544988;
    int isc_cannot_make_not_null             = 335544989;
    int isc_feature_removed                  = 335544990;
    int isc_view_name                        = 335544991;
    int isc_lock_dir_access                  = 335544992;
    int isc_invalid_fetch_option             = 335544993;
    int isc_bad_fun_BLR                      = 335544994;
    int isc_func_pack_not_implemented        = 335544995;
    int isc_proc_pack_not_implemented        = 335544996;
    int isc_eem_func_not_returned            = 335544997;
    int isc_eem_proc_not_returned            = 335544998;
    int isc_eem_trig_not_returned            = 335544999;
    int isc_eem_bad_plugin_ver               = 335545000;
    int isc_eem_engine_notfound              = 335545001;
    int isc_attachment_in_use                = 335545002;
    int isc_transaction_in_use               = 335545003;
    int isc_pman_cannot_load_plugin          = 335545004;
    int isc_pman_module_notfound             = 335545005;
    int isc_pman_entrypoint_notfound         = 335545006;
    int isc_pman_module_bad                  = 335545007;
    int isc_pman_plugin_notfound             = 335545008;
    int isc_sysf_invalid_trig_namespace      = 335545009;
    int isc_unexpected_null                  = 335545010;
    int isc_type_notcompat_blob              = 335545011;
    int isc_invalid_date_val                 = 335545012;
    int isc_invalid_time_val                 = 335545013;
    int isc_invalid_timestamp_val            = 335545014;
    int isc_invalid_index_val                = 335545015;
    int isc_formatted_exception              = 335545016;
    int isc_async_active                     = 335545017;
    int isc_private_function                 = 335545018;
    int isc_private_procedure                = 335545019;
    int isc_request_outdated                 = 335545020;
    int isc_bad_events_handle                = 335545021;
    int isc_cannot_copy_stmt                 = 335545022;
    int isc_invalid_boolean_usage            = 335545023;
    int isc_sysf_argscant_both_be_zero       = 335545024;
    int isc_spb_no_id                        = 335545025;
    int isc_ee_blr_mismatch_null             = 335545026;
    int isc_ee_blr_mismatch_length           = 335545027;
    int isc_ss_out_of_bounds                 = 335545028;
    int isc_missing_data_structures          = 335545029;
    int isc_protect_sys_tab                  = 335545030;
    int isc_libtommath_generic               = 335545031;
    int isc_wroblrver2                       = 335545032;
    int isc_trunc_limits                     = 335545033;
    int isc_info_access                      = 335545034;
    int isc_svc_no_stdin                     = 335545035;
    int isc_svc_start_failed                 = 335545036;
    int isc_svc_no_switches                  = 335545037;
    int isc_svc_bad_size                     = 335545038;
    int isc_no_crypt_plugin                  = 335545039;
    int isc_cp_name_too_long                 = 335545040;
    int isc_cp_process_active                = 335545041;
    int isc_cp_already_crypted               = 335545042;
    int isc_decrypt_error                    = 335545043;
    int isc_no_providers                     = 335545044;
    int isc_null_spb                         = 335545045;
    int isc_max_args_exceeded                = 335545046;
    int isc_ee_blr_mismatch_names_count      = 335545047;
    int isc_ee_blr_mismatch_name_not_found   = 335545048;
    int isc_bad_result_set                   = 335545049;
    int isc_wrong_message_length             = 335545050;
    int isc_no_output_format                 = 335545051;
    int isc_item_finish                      = 335545052;
    int isc_miss_config                      = 335545053;
    int isc_conf_line                        = 335545054;
    int isc_conf_include                     = 335545055;
    int isc_include_depth                    = 335545056;
    int isc_include_miss                     = 335545057;
    int isc_protect_ownership                = 335545058;
    int isc_badvarnum                        = 335545059;
    int isc_sec_context                      = 335545060;
    int isc_multi_segment                    = 335545061;
    int isc_login_changed                    = 335545062;
    int isc_auth_handshake_limit             = 335545063;
    int isc_wirecrypt_incompatible           = 335545064;
    int isc_miss_wirecrypt                   = 335545065;
    int isc_wirecrypt_key                    = 335545066;
    int isc_wirecrypt_plugin                 = 335545067;
    int isc_secdb_name                       = 335545068;
    int isc_auth_data                        = 335545069;
    int isc_auth_datalength                  = 335545070;
    int isc_info_unprepared_stmt             = 335545071;
    int isc_idx_key_value                    = 335545072;
    int isc_forupdate_virtualtbl             = 335545073;
    int isc_forupdate_systbl                 = 335545074;
    int isc_forupdate_temptbl                = 335545075;
    int isc_cant_modify_sysobj               = 335545076;
    int isc_server_misconfigured             = 335545077;
    int isc_alter_role                       = 335545078;
    int isc_map_already_exists               = 335545079;
    int isc_map_not_exists                   = 335545080;
    int isc_map_load                         = 335545081;
    int isc_map_aster                        = 335545082;
    int isc_map_multi                        = 335545083;
    int isc_map_undefined                    = 335545084;
    int isc_baddpb_damaged_mode              = 335545085;
    int isc_baddpb_buffers_range             = 335545086;
    int isc_baddpb_temp_buffers              = 335545087;
    int isc_map_nodb                         = 335545088;
    int isc_map_notable                      = 335545089;
    int isc_miss_trusted_role                = 335545090;
    int isc_set_invalid_role                 = 335545091;
    int isc_cursor_not_positioned            = 335545092;
    int isc_dup_attribute                    = 335545093;
    int isc_dyn_no_priv                      = 335545094;
    int isc_dsql_cant_grant_option           = 335545095;
    int isc_read_conflict                    = 335545096;
    int isc_crdb_load                        = 335545097;
    int isc_crdb_nodb                        = 335545098;
    int isc_crdb_notable                     = 335545099;
    int isc_gfix_db_name                     = 335740929;
    int isc_gfix_invalid_sw                  = 335740930;
    int isc_gfix_incmp_sw                    = 335740932;
    int isc_gfix_replay_req                  = 335740933;
    int isc_gfix_pgbuf_req                   = 335740934;
    int isc_gfix_val_req                     = 335740935;
    int isc_gfix_pval_req                    = 335740936;
    int isc_gfix_trn_req                     = 335740937;
    int isc_gfix_full_req                    = 335740940;
    int isc_gfix_usrname_req                 = 335740941;
    int isc_gfix_pass_req                    = 335740942;
    int isc_gfix_subs_name                   = 335740943;
    int isc_gfix_wal_req                     = 335740944;
    int isc_gfix_sec_req                     = 335740945;
    int isc_gfix_nval_req                    = 335740946;
    int isc_gfix_type_shut                   = 335740947;
    int isc_gfix_retry                       = 335740948;
    int isc_gfix_retry_db                    = 335740951;
    int isc_gfix_exceed_max                  = 335740991;
    int isc_gfix_corrupt_pool                = 335740992;
    int isc_gfix_mem_exhausted               = 335740993;
    int isc_gfix_bad_pool                    = 335740994;
    int isc_gfix_trn_not_valid               = 335740995;
    int isc_gfix_unexp_eoi                   = 335741012;
    int isc_gfix_recon_fail                  = 335741018;
    int isc_gfix_trn_unknown                 = 335741036;
    int isc_gfix_mode_req                    = 335741038;
    int isc_gfix_pzval_req                   = 335741042;
    int isc_dsql_dbkey_from_non_table        = 336003074;
    int isc_dsql_transitional_numeric        = 336003075;
    int isc_dsql_dialect_warning_expr        = 336003076;
    int isc_sql_db_dialect_dtype_unsupport   = 336003077;
    int isc_isc_sql_dialect_conflict_num     = 336003079;
    int isc_dsql_warning_number_ambiguous    = 336003080;
    int isc_dsql_warning_number_ambiguous1   = 336003081;
    int isc_dsql_warn_precision_ambiguous    = 336003082;
    int isc_dsql_warn_precision_ambiguous1   = 336003083;
    int isc_dsql_warn_precision_ambiguous2   = 336003084;
    int isc_dsql_ambiguous_field_name        = 336003085;
    int isc_dsql_udf_return_pos_err          = 336003086;
    int isc_dsql_invalid_label               = 336003087;
    int isc_dsql_datatypes_not_comparable    = 336003088;
    int isc_dsql_cursor_invalid              = 336003089;
    int isc_dsql_cursor_redefined            = 336003090;
    int isc_dsql_cursor_not_found            = 336003091;
    int isc_dsql_cursor_exists               = 336003092;
    int isc_dsql_cursor_rel_ambiguous        = 336003093;
    int isc_dsql_cursor_rel_not_found        = 336003094;
    int isc_dsql_cursor_not_open             = 336003095;
    int isc_dsql_type_not_supp_ext_tab       = 336003096;
    int isc_dsql_feature_not_supported_ods   = 336003097;
    int isc_primary_key_required             = 336003098;
    int isc_upd_ins_doesnt_match_pk          = 336003099;
    int isc_upd_ins_doesnt_match_matching    = 336003100;
    int isc_upd_ins_with_complex_view        = 336003101;
    int isc_dsql_incompatible_trigger_type   = 336003102;
    int isc_dsql_db_trigger_type_cant_change = 336003103;
    int isc_dsql_record_version_table        = 336003104;
    int isc_dyn_filter_not_found             = 336068645;
    int isc_dyn_func_not_found               = 336068649;
    int isc_dyn_index_not_found              = 336068656;
    int isc_dyn_view_not_found               = 336068662;
    int isc_dyn_domain_not_found             = 336068697;
    int isc_dyn_cant_modify_auto_trig        = 336068717;
    int isc_dyn_dup_table                    = 336068740;
    int isc_dyn_proc_not_found               = 336068748;
    int isc_dyn_exception_not_found          = 336068752;
    int isc_dyn_proc_param_not_found         = 336068754;
    int isc_dyn_trig_not_found               = 336068755;
    int isc_dyn_charset_not_found            = 336068759;
    int isc_dyn_collation_not_found          = 336068760;
    int isc_dyn_role_not_found               = 336068763;
    int isc_dyn_name_longer                  = 336068767;
    int isc_dyn_column_does_not_exist        = 336068784;
    int isc_dyn_role_does_not_exist          = 336068796;
    int isc_dyn_no_grant_admin_opt           = 336068797;
    int isc_dyn_user_not_role_member         = 336068798;
    int isc_dyn_delete_role_failed           = 336068799;
    int isc_dyn_grant_role_to_user           = 336068800;
    int isc_dyn_inv_sql_role_name            = 336068801;
    int isc_dyn_dup_sql_role                 = 336068802;
    int isc_dyn_kywd_spec_for_role           = 336068803;
    int isc_dyn_roles_not_supported          = 336068804;
    int isc_dyn_domain_name_exists           = 336068812;
    int isc_dyn_field_name_exists            = 336068813;
    int isc_dyn_dependency_exists            = 336068814;
    int isc_dyn_dtype_invalid                = 336068815;
    int isc_dyn_char_fld_too_small           = 336068816;
    int isc_dyn_invalid_dtype_conversion     = 336068817;
    int isc_dyn_dtype_conv_invalid           = 336068818;
    int isc_dyn_zero_len_id                  = 336068820;
    int isc_dyn_gen_not_found                = 336068822;
    int isc_max_coll_per_charset             = 336068829;
    int isc_invalid_coll_attr                = 336068830;
    int isc_dyn_wrong_gtt_scope              = 336068840;
    int isc_dyn_coll_used_table              = 336068843;
    int isc_dyn_coll_used_domain             = 336068844;
    int isc_dyn_cannot_del_syscoll           = 336068845;
    int isc_dyn_cannot_del_def_coll          = 336068846;
    int isc_dyn_table_not_found              = 336068849;
    int isc_dyn_coll_used_procedure          = 336068851;
    int isc_dyn_scale_too_big                = 336068852;
    int isc_dyn_precision_too_small          = 336068853;
    int isc_dyn_miss_priv_warning            = 336068855;
    int isc_dyn_ods_not_supp_feature         = 336068856;
    int isc_dyn_cannot_addrem_computed       = 336068857;
    int isc_dyn_no_empty_pw                  = 336068858;
    int isc_dyn_dup_index                    = 336068859;
    int isc_dyn_package_not_found            = 336068864;
    int isc_dyn_schema_not_found             = 336068865;
    int isc_dyn_cannot_mod_sysproc           = 336068866;
    int isc_dyn_cannot_mod_systrig           = 336068867;
    int isc_dyn_cannot_mod_sysfunc           = 336068868;
    int isc_dyn_invalid_ddl_proc             = 336068869;
    int isc_dyn_invalid_ddl_trig             = 336068870;
    int isc_dyn_funcnotdef_package           = 336068871;
    int isc_dyn_procnotdef_package           = 336068872;
    int isc_dyn_funcsignat_package           = 336068873;
    int isc_dyn_procsignat_package           = 336068874;
    int isc_dyn_defvaldecl_package           = 336068875;
    int isc_dyn_package_body_exists          = 336068877;
    int isc_dyn_invalid_ddl_func             = 336068878;
    int isc_dyn_newfc_oldsyntax              = 336068879;
    int isc_dyn_func_param_not_found         = 336068886;
    int isc_dyn_routine_param_not_found      = 336068887;
    int isc_dyn_routine_param_ambiguous      = 336068888;
    int isc_dyn_coll_used_function           = 336068889;
    int isc_dyn_domain_used_function         = 336068890;
    int isc_dyn_alter_user_no_clause         = 336068891;
    int isc_dyn_duplicate_package_item       = 336068894;
    int isc_dyn_cant_modify_sysobj           = 336068895;
    int isc_dyn_cant_use_zero_increment      = 336068896;
    int isc_dyn_cant_use_in_foreignkey       = 336068897;
    int isc_gbak_unknown_switch              = 336330753;
    int isc_gbak_page_size_missing           = 336330754;
    int isc_gbak_page_size_toobig            = 336330755;
    int isc_gbak_redir_ouput_missing         = 336330756;
    int isc_gbak_switches_conflict           = 336330757;
    int isc_gbak_unknown_device              = 336330758;
    int isc_gbak_no_protection               = 336330759;
    int isc_gbak_page_size_not_allowed       = 336330760;
    int isc_gbak_multi_source_dest           = 336330761;
    int isc_gbak_filename_missing            = 336330762;
    int isc_gbak_dup_inout_names             = 336330763;
    int isc_gbak_inv_page_size               = 336330764;
    int isc_gbak_db_specified                = 336330765;
    int isc_gbak_db_exists                   = 336330766;
    int isc_gbak_unk_device                  = 336330767;
    int isc_gbak_blob_info_failed            = 336330772;
    int isc_gbak_unk_blob_item               = 336330773;
    int isc_gbak_get_seg_failed              = 336330774;
    int isc_gbak_close_blob_failed           = 336330775;
    int isc_gbak_open_blob_failed            = 336330776;
    int isc_gbak_put_blr_gen_id_failed       = 336330777;
    int isc_gbak_unk_type                    = 336330778;
    int isc_gbak_comp_req_failed             = 336330779;
    int isc_gbak_start_req_failed            = 336330780;
    int isc_gbak_rec_failed                  = 336330781;
    int isc_gbak_rel_req_failed              = 336330782;
    int isc_gbak_db_info_failed              = 336330783;
    int isc_gbak_no_db_desc                  = 336330784;
    int isc_gbak_db_create_failed            = 336330785;
    int isc_gbak_decomp_len_error            = 336330786;
    int isc_gbak_tbl_missing                 = 336330787;
    int isc_gbak_blob_col_missing            = 336330788;
    int isc_gbak_create_blob_failed          = 336330789;
    int isc_gbak_put_seg_failed              = 336330790;
    int isc_gbak_rec_len_exp                 = 336330791;
    int isc_gbak_inv_rec_len                 = 336330792;
    int isc_gbak_exp_data_type               = 336330793;
    int isc_gbak_gen_id_failed               = 336330794;
    int isc_gbak_unk_rec_type                = 336330795;
    int isc_gbak_inv_bkup_ver                = 336330796;
    int isc_gbak_missing_bkup_desc           = 336330797;
    int isc_gbak_string_trunc                = 336330798;
    int isc_gbak_cant_rest_record            = 336330799;
    int isc_gbak_send_failed                 = 336330800;
    int isc_gbak_no_tbl_name                 = 336330801;
    int isc_gbak_unexp_eof                   = 336330802;
    int isc_gbak_db_format_too_old           = 336330803;
    int isc_gbak_inv_array_dim               = 336330804;
    int isc_gbak_xdr_len_expected            = 336330807;
    int isc_gbak_open_bkup_error             = 336330817;
    int isc_gbak_open_error                  = 336330818;
    int isc_gbak_missing_block_fac           = 336330934;
    int isc_gbak_inv_block_fac               = 336330935;
    int isc_gbak_block_fac_specified         = 336330936;
    int isc_gbak_missing_username            = 336330940;
    int isc_gbak_missing_password            = 336330941;
    int isc_gbak_missing_skipped_bytes       = 336330952;
    int isc_gbak_inv_skipped_bytes           = 336330953;
    int isc_gbak_err_restore_charset         = 336330965;
    int isc_gbak_err_restore_collation       = 336330967;
    int isc_gbak_read_error                  = 336330972;
    int isc_gbak_write_error                 = 336330973;
    int isc_gbak_db_in_use                   = 336330985;
    int isc_gbak_sysmemex                    = 336330990;
    int isc_gbak_restore_role_failed         = 336331002;
    int isc_gbak_role_op_missing             = 336331005;
    int isc_gbak_page_buffers_missing        = 336331010;
    int isc_gbak_page_buffers_wrong_param    = 336331011;
    int isc_gbak_page_buffers_restore        = 336331012;
    int isc_gbak_inv_size                    = 336331014;
    int isc_gbak_file_outof_sequence         = 336331015;
    int isc_gbak_join_file_missing           = 336331016;
    int isc_gbak_stdin_not_supptd            = 336331017;
    int isc_gbak_stdout_not_supptd           = 336331018;
    int isc_gbak_bkup_corrupt                = 336331019;
    int isc_gbak_unk_db_file_spec            = 336331020;
    int isc_gbak_hdr_write_failed            = 336331021;
    int isc_gbak_disk_space_ex               = 336331022;
    int isc_gbak_size_lt_min                 = 336331023;
    int isc_gbak_svc_name_missing            = 336331025;
    int isc_gbak_not_ownr                    = 336331026;
    int isc_gbak_mode_req                    = 336331031;
    int isc_gbak_just_data                   = 336331033;
    int isc_gbak_data_only                   = 336331034;
    int isc_gbak_missing_interval            = 336331078;
    int isc_gbak_wrong_interval              = 336331079;
    int isc_gbak_verify_verbint              = 336331081;
    int isc_gbak_option_only_restore         = 336331082;
    int isc_gbak_option_only_backup          = 336331083;
    int isc_gbak_option_conflict             = 336331084;
    int isc_gbak_param_conflict              = 336331085;
    int isc_gbak_option_repeated             = 336331086;
    int isc_gbak_max_dbkey_recursion         = 336331091;
    int isc_gbak_max_dbkey_length            = 336331092;
    int isc_gbak_invalid_metadata            = 336331093;
    int isc_gbak_invalid_data                = 336331094;
    int isc_gbak_inv_bkup_ver2               = 336331096;
    int isc_gbak_db_format_too_old2          = 336331100;
    int isc_dsql_too_old_ods                 = 336397205;
    int isc_dsql_table_not_found             = 336397206;
    int isc_dsql_view_not_found              = 336397207;
    int isc_dsql_line_col_error              = 336397208;
    int isc_dsql_unknown_pos                 = 336397209;
    int isc_dsql_no_dup_name                 = 336397210;
    int isc_dsql_too_many_values             = 336397211;
    int isc_dsql_no_array_computed           = 336397212;
    int isc_dsql_implicit_domain_name        = 336397213;
    int isc_dsql_only_can_subscript_array    = 336397214;
    int isc_dsql_max_sort_items              = 336397215;
    int isc_dsql_max_group_items             = 336397216;
    int isc_dsql_conflicting_sort_field      = 336397217;
    int isc_dsql_derived_table_more_columns  = 336397218;
    int isc_dsql_derived_table_less_columns  = 336397219;
    int isc_dsql_derived_field_unnamed       = 336397220;
    int isc_dsql_derived_field_dup_name      = 336397221;
    int isc_dsql_derived_alias_select        = 336397222;
    int isc_dsql_derived_alias_field         = 336397223;
    int isc_dsql_auto_field_bad_pos          = 336397224;
    int isc_dsql_cte_wrong_reference         = 336397225;
    int isc_dsql_cte_cycle                   = 336397226;
    int isc_dsql_cte_outer_join              = 336397227;
    int isc_dsql_cte_mult_references         = 336397228;
    int isc_dsql_cte_not_a_union             = 336397229;
    int isc_dsql_cte_nonrecurs_after_recurs  = 336397230;
    int isc_dsql_cte_wrong_clause            = 336397231;
    int isc_dsql_cte_union_all               = 336397232;
    int isc_dsql_cte_miss_nonrecursive       = 336397233;
    int isc_dsql_cte_nested_with             = 336397234;
    int isc_dsql_col_more_than_once_using    = 336397235;
    int isc_dsql_unsupp_feature_dialect      = 336397236;
    int isc_dsql_cte_not_used                = 336397237;
    int isc_dsql_col_more_than_once_view     = 336397238;
    int isc_dsql_unsupported_in_auto_trans   = 336397239;
    int isc_dsql_eval_unknode                = 336397240;
    int isc_dsql_agg_wrongarg                = 336397241;
    int isc_dsql_agg2_wrongarg               = 336397242;
    int isc_dsql_nodateortime_pm_string      = 336397243;
    int isc_dsql_invalid_datetime_subtract   = 336397244;
    int isc_dsql_invalid_dateortime_add      = 336397245;
    int isc_dsql_invalid_type_minus_date     = 336397246;
    int isc_dsql_nostring_addsub_dial3       = 336397247;
    int isc_dsql_invalid_type_addsub_dial3   = 336397248;
    int isc_dsql_invalid_type_multip_dial1   = 336397249;
    int isc_dsql_nostring_multip_dial3       = 336397250;
    int isc_dsql_invalid_type_multip_dial3   = 336397251;
    int isc_dsql_mustuse_numeric_div_dial1   = 336397252;
    int isc_dsql_nostring_div_dial3          = 336397253;
    int isc_dsql_invalid_type_div_dial3      = 336397254;
    int isc_dsql_nostring_neg_dial3          = 336397255;
    int isc_dsql_invalid_type_neg            = 336397256;
    int isc_dsql_max_distinct_items          = 336397257;
    int isc_dsql_alter_charset_failed        = 336397258;
    int isc_dsql_comment_on_failed           = 336397259;
    int isc_dsql_create_func_failed          = 336397260;
    int isc_dsql_alter_func_failed           = 336397261;
    int isc_dsql_create_alter_func_failed    = 336397262;
    int isc_dsql_drop_func_failed            = 336397263;
    int isc_dsql_recreate_func_failed        = 336397264;
    int isc_dsql_create_proc_failed          = 336397265;
    int isc_dsql_alter_proc_failed           = 336397266;
    int isc_dsql_create_alter_proc_failed    = 336397267;
    int isc_dsql_drop_proc_failed            = 336397268;
    int isc_dsql_recreate_proc_failed        = 336397269;
    int isc_dsql_create_trigger_failed       = 336397270;
    int isc_dsql_alter_trigger_failed        = 336397271;
    int isc_dsql_create_alter_trigger_failed = 336397272;
    int isc_dsql_drop_trigger_failed         = 336397273;
    int isc_dsql_recreate_trigger_failed     = 336397274;
    int isc_dsql_create_collation_failed     = 336397275;
    int isc_dsql_drop_collation_failed       = 336397276;
    int isc_dsql_create_domain_failed        = 336397277;
    int isc_dsql_alter_domain_failed         = 336397278;
    int isc_dsql_drop_domain_failed          = 336397279;
    int isc_dsql_create_except_failed        = 336397280;
    int isc_dsql_alter_except_failed         = 336397281;
    int isc_dsql_create_alter_except_failed  = 336397282;
    int isc_dsql_recreate_except_failed      = 336397283;
    int isc_dsql_drop_except_failed          = 336397284;
    int isc_dsql_create_sequence_failed      = 336397285;
    int isc_dsql_create_table_failed         = 336397286;
    int isc_dsql_alter_table_failed          = 336397287;
    int isc_dsql_drop_table_failed           = 336397288;
    int isc_dsql_recreate_table_failed       = 336397289;
    int isc_dsql_create_pack_failed          = 336397290;
    int isc_dsql_alter_pack_failed           = 336397291;
    int isc_dsql_create_alter_pack_failed    = 336397292;
    int isc_dsql_drop_pack_failed            = 336397293;
    int isc_dsql_recreate_pack_failed        = 336397294;
    int isc_dsql_create_pack_body_failed     = 336397295;
    int isc_dsql_drop_pack_body_failed       = 336397296;
    int isc_dsql_recreate_pack_body_failed   = 336397297;
    int isc_dsql_create_view_failed          = 336397298;
    int isc_dsql_alter_view_failed           = 336397299;
    int isc_dsql_create_alter_view_failed    = 336397300;
    int isc_dsql_recreate_view_failed        = 336397301;
    int isc_dsql_drop_view_failed            = 336397302;
    int isc_dsql_drop_sequence_failed        = 336397303;
    int isc_dsql_recreate_sequence_failed    = 336397304;
    int isc_dsql_drop_index_failed           = 336397305;
    int isc_dsql_drop_filter_failed          = 336397306;
    int isc_dsql_drop_shadow_failed          = 336397307;
    int isc_dsql_drop_role_failed            = 336397308;
    int isc_dsql_drop_user_failed            = 336397309;
    int isc_dsql_create_role_failed          = 336397310;
    int isc_dsql_alter_role_failed           = 336397311;
    int isc_dsql_alter_index_failed          = 336397312;
    int isc_dsql_alter_database_failed       = 336397313;
    int isc_dsql_create_shadow_failed        = 336397314;
    int isc_dsql_create_filter_failed        = 336397315;
    int isc_dsql_create_index_failed         = 336397316;
    int isc_dsql_create_user_failed          = 336397317;
    int isc_dsql_alter_user_failed           = 336397318;
    int isc_dsql_grant_failed                = 336397319;
    int isc_dsql_revoke_failed               = 336397320;
    int isc_dsql_cte_recursive_aggregate     = 336397321;
    int isc_dsql_mapping_failed              = 336397322;
    int isc_dsql_alter_sequence_failed       = 336397323;
    int isc_dsql_create_generator_failed     = 336397324;
    int isc_dsql_set_generator_failed        = 336397325;
    int isc_dsql_wlock_simple                = 336397326;
    int isc_dsql_firstskip_rows              = 336397327;
    int isc_dsql_wlock_aggregates            = 336397328;
    int isc_dsql_wlock_conflict              = 336397329;
    int isc_gsec_cant_open_db                = 336723983;
    int isc_gsec_switches_error              = 336723984;
    int isc_gsec_no_op_spec                  = 336723985;
    int isc_gsec_no_usr_name                 = 336723986;
    int isc_gsec_err_add                     = 336723987;
    int isc_gsec_err_modify                  = 336723988;
    int isc_gsec_err_find_mod                = 336723989;
    int isc_gsec_err_rec_not_found           = 336723990;
    int isc_gsec_err_delete                  = 336723991;
    int isc_gsec_err_find_del                = 336723992;
    int isc_gsec_err_find_disp               = 336723996;
    int isc_gsec_inv_param                   = 336723997;
    int isc_gsec_op_specified                = 336723998;
    int isc_gsec_pw_specified                = 336723999;
    int isc_gsec_uid_specified               = 336724000;
    int isc_gsec_gid_specified               = 336724001;
    int isc_gsec_proj_specified              = 336724002;
    int isc_gsec_org_specified               = 336724003;
    int isc_gsec_fname_specified             = 336724004;
    int isc_gsec_mname_specified             = 336724005;
    int isc_gsec_lname_specified             = 336724006;
    int isc_gsec_inv_switch                  = 336724008;
    int isc_gsec_amb_switch                  = 336724009;
    int isc_gsec_no_op_specified             = 336724010;
    int isc_gsec_params_not_allowed          = 336724011;
    int isc_gsec_incompat_switch             = 336724012;
    int isc_gsec_inv_username                = 336724044;
    int isc_gsec_inv_pw_length               = 336724045;
    int isc_gsec_db_specified                = 336724046;
    int isc_gsec_db_admin_specified          = 336724047;
    int isc_gsec_db_admin_pw_specified       = 336724048;
    int isc_gsec_sql_role_specified          = 336724049;
    int isc_gstat_unknown_switch             = 336920577;
    int isc_gstat_retry                      = 336920578;
    int isc_gstat_wrong_ods                  = 336920579;
    int isc_gstat_unexpected_eof             = 336920580;
    int isc_gstat_open_err                   = 336920605;
    int isc_gstat_read_err                   = 336920606;
    int isc_gstat_sysmemex                   = 336920607;
    int isc_fbsvcmgr_bad_am                  = 336986113;
    int isc_fbsvcmgr_bad_wm                  = 336986114;
    int isc_fbsvcmgr_bad_rs                  = 336986115;
    int isc_fbsvcmgr_info_err                = 336986116;
    int isc_fbsvcmgr_query_err               = 336986117;
    int isc_fbsvcmgr_switch_unknown          = 336986118;
    int isc_fbsvcmgr_bad_sm                  = 336986159;
    int isc_fbsvcmgr_fp_open                 = 336986160;
    int isc_fbsvcmgr_fp_read                 = 336986161;
    int isc_fbsvcmgr_fp_empty                = 336986162;
    int isc_fbsvcmgr_bad_arg                 = 336986164;
    int isc_utl_trusted_switch               = 337051649;
    int isc_nbackup_missing_param            = 337117213;
    int isc_nbackup_allowed_switches         = 337117214;
    int isc_nbackup_unknown_param            = 337117215;
    int isc_nbackup_unknown_switch           = 337117216;
    int isc_nbackup_nofetchpw_svc            = 337117217;
    int isc_nbackup_pwfile_error             = 337117218;
    int isc_nbackup_size_with_lock           = 337117219;
    int isc_nbackup_no_switch                = 337117220;
    int isc_nbackup_err_read                 = 337117223;
    int isc_nbackup_err_write                = 337117224;
    int isc_nbackup_err_seek                 = 337117225;
    int isc_nbackup_err_opendb               = 337117226;
    int isc_nbackup_err_fadvice              = 337117227;
    int isc_nbackup_err_createdb             = 337117228;
    int isc_nbackup_err_openbk               = 337117229;
    int isc_nbackup_err_createbk             = 337117230;
    int isc_nbackup_err_eofdb                = 337117231;
    int isc_nbackup_fixup_wrongstate         = 337117232;
    int isc_nbackup_err_db                   = 337117233;
    int isc_nbackup_userpw_toolong           = 337117234;
    int isc_nbackup_lostrec_db               = 337117235;
    int isc_nbackup_lostguid_db              = 337117236;
    int isc_nbackup_err_eofhdrdb             = 337117237;
    int isc_nbackup_db_notlock               = 337117238;
    int isc_nbackup_lostguid_bk              = 337117239;
    int isc_nbackup_page_changed             = 337117240;
    int isc_nbackup_dbsize_inconsistent      = 337117241;
    int isc_nbackup_failed_lzbk              = 337117242;
    int isc_nbackup_err_eofhdrbk             = 337117243;
    int isc_nbackup_invalid_incbk            = 337117244;
    int isc_nbackup_unsupvers_incbk          = 337117245;
    int isc_nbackup_invlevel_incbk           = 337117246;
    int isc_nbackup_wrong_orderbk            = 337117247;
    int isc_nbackup_err_eofbk                = 337117248;
    int isc_nbackup_err_copy                 = 337117249;
    int isc_nbackup_err_eofhdr_restdb        = 337117250;
    int isc_nbackup_lostguid_l0bk            = 337117251;
    int isc_nbackup_switchd_parameter        = 337117255;
    int isc_nbackup_user_stop                = 337117257;
    int isc_nbackup_deco_parse               = 337117259;
    int isc_trace_conflict_acts              = 337182750;
    int isc_trace_act_notfound               = 337182751;
    int isc_trace_switch_once                = 337182752;
    int isc_trace_param_val_miss             = 337182753;
    int isc_trace_param_invalid              = 337182754;
    int isc_trace_switch_unknown             = 337182755;
    int isc_trace_switch_svc_only            = 337182756;
    int isc_trace_switch_user_only           = 337182757;
    int isc_trace_switch_param_miss          = 337182758;
    int isc_trace_param_act_notcompat        = 337182759;
    int isc_trace_mandatory_switch_miss      = 337182760;
    int isc_err_max                          = 1237;

    /*******************/
    /* SQL definitions */
    /*******************/

    int SQL_TEXT      = 452;
    int SQL_VARYING   = 448;
    int SQL_SHORT     = 500;
    int SQL_LONG      = 496;
    int SQL_FLOAT     = 482;
    int SQL_DOUBLE    = 480;
    int SQL_D_FLOAT   = 530;
    int SQL_TIMESTAMP = 510;
    int SQL_BLOB      = 520;
    int SQL_ARRAY     = 540;
    int SQL_QUAD      = 550;
    int SQL_TYPE_TIME = 560;
    int SQL_TYPE_DATE = 570;
    int SQL_INT64     = 580;
    int SQL_BOOLEAN   = 32764;
    int SQL_NULL      = 32766;

    /* Historical alias for pre V6 applications */
    int SQL_DATE      = SQL_TIMESTAMP;

    /*******************/
    /* Other stuff     */
    /*******************/
    int CS_NONE    = 0; /* No Character Set */
    int CS_BINARY  = 1; /* BINARY BYTES */
    int CS_dynamic = 127; // Pseudo number for runtime charset (see intl\charsets.h and references to it in Firebird)

    // Fetch related constants
    int FETCH_OK = 0;
    int FETCH_NO_MORE_ROWS = 100;
}
