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

/* The Original Code is the Firebird Java GDS implementation.
 *
 * The Initial Developer of the Original Code is Alejandro Alberola.
 * Portions created by Alejandro Alberola are Copyright (C) 2001
 * Boix i Oltra, S.L. All Rights Reserved.
 *
 */

package org.firebirdsql.gds;

import java.util.Set;



/**
 * The interface <code>GDS</code> has most of the C client interface functions
 * lightly mapped to java, as well as the constants returned from the server..
 *
 * @author <a href="mailto:alberola@users.sourceforge.net">Alejandro Alberola</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public interface GDS {

    final static int SQLDA_VERSION1             = 1;
    final static int SQL_DIALECT_V5             = 1;
    final static int SQL_DIALECT_V6_TRANSITION  = 2;
    final static int SQL_DIALECT_V6             = 3;
    final static int SQL_DIALECT_CURRENT        = SQL_DIALECT_V6;

    final static int DSQL_close   =  1;
    final static int DSQL_drop    =  2;

    /**********************************/
    /* Database parameter block stuff */
    /**********************************/

    final static int isc_dpb_version1                = 1;
    final static int isc_dpb_cdd_pathname            = 1;
    final static int isc_dpb_allocation              = 2;
    final static int isc_dpb_journal                 = 3;
    final static int isc_dpb_page_size               = 4;
    final static int isc_dpb_num_buffers             = 5;
    final static int isc_dpb_buffer_length           = 6;
    final static int isc_dpb_debug                   = 7;
    final static int isc_dpb_garbage_collect         = 8;
    final static int isc_dpb_verify                  = 9;
    final static int isc_dpb_sweep                   = 10;
    final static int isc_dpb_enable_journal          = 11;
    final static int isc_dpb_disable_journal         = 12;
    final static int isc_dpb_dbkey_scope             = 13;
    final static int isc_dpb_number_of_users         = 14;
    final static int isc_dpb_trace                   = 15;
    final static int isc_dpb_no_garbage_collect      = 16;
    final static int isc_dpb_damaged                 = 17;
    final static int isc_dpb_license                 = 18;
    final static int isc_dpb_sys_user_name           = 19;
    final static int isc_dpb_encrypt_key             = 20;
    final static int isc_dpb_activate_shadow         = 21;
    final static int isc_dpb_sweep_interval          = 22;
    final static int isc_dpb_delete_shadow           = 23;
    final static int isc_dpb_force_write             = 24;
    final static int isc_dpb_begin_log               = 25;
    final static int isc_dpb_quit_log                = 26;
    final static int isc_dpb_no_reserve              = 27;
    final static int isc_dpb_user_name               = 28;
    final static int isc_dpb_password                = 29;
    final static int isc_dpb_password_enc            = 30;
    final static int isc_dpb_sys_user_name_enc       = 31;
    final static int isc_dpb_interp                  = 32;
    final static int isc_dpb_online_dump             = 33;
    final static int isc_dpb_old_file_size           = 34;
    final static int isc_dpb_old_num_files           = 35;
    final static int isc_dpb_old_file                = 36;
    final static int isc_dpb_old_start_page          = 37;
    final static int isc_dpb_old_start_seqno         = 38;
    final static int isc_dpb_old_start_file          = 39;
    final static int isc_dpb_drop_walfile            = 40;
    final static int isc_dpb_old_dump_id             = 41;
    final static int isc_dpb_wal_backup_dir          = 42;
    final static int isc_dpb_wal_chkptlen            = 43;
    final static int isc_dpb_wal_numbufs             = 44;
    final static int isc_dpb_wal_bufsize             = 45;
    final static int isc_dpb_wal_grp_cmt_wait        = 46;
    final static int isc_dpb_lc_messages             = 47;
    final static int isc_dpb_lc_ctype                = 48;
    final static int isc_dpb_cache_manager           = 49;
    final static int isc_dpb_shutdown                = 50;
    final static int isc_dpb_online                  = 51;
    final static int isc_dpb_shutdown_delay          = 52;
    final static int isc_dpb_reserved                = 53;
    final static int isc_dpb_overwrite               = 54;
    final static int isc_dpb_sec_attach              = 55;
    final static int isc_dpb_disable_wal             = 56;
    final static int isc_dpb_connect_timeout         = 57;
    final static int isc_dpb_dummy_packet_interval   = 58;
    final static int isc_dpb_gbak_attach             = 59;
    final static int isc_dpb_sql_role_name           = 60;
    final static int isc_dpb_set_page_buffers        = 61;
    final static int isc_dpb_working_directory       = 62;
    final static int isc_dpb_sql_dialect             = 63;
    final static int isc_dpb_set_db_readonly         = 64;
    final static int isc_dpb_set_db_sql_dialect      = 65;
    final static int isc_dpb_gfix_attach             = 66;
    final static int isc_dpb_gstat_attach            = 67;

    /*************************************/
    /* Transaction parameter block stuff */
    /*************************************/

    final static int isc_tpb_version1                = 1;
    final static int isc_tpb_version3                = 3;
    final static int isc_tpb_consistency             = 1;
    final static int isc_tpb_concurrency             = 2;
    final static int isc_tpb_shared                  = 3;
    final static int isc_tpb_protected               = 4;
    final static int isc_tpb_exclusive               = 5;
    final static int isc_tpb_wait                    = 6;
    final static int isc_tpb_nowait                  = 7;
    final static int isc_tpb_read                    = 8;
    final static int isc_tpb_write                   = 9;
    final static int isc_tpb_lock_read               = 10;
    final static int isc_tpb_lock_write              = 11;
    final static int isc_tpb_verb_time               = 12;
    final static int isc_tpb_commit_time             = 13;
    final static int isc_tpb_ignore_limbo            = 14;
    final static int isc_tpb_read_committed          = 15;
    final static int isc_tpb_autocommit              = 16;
    final static int isc_tpb_rec_version             = 17;
    final static int isc_tpb_no_rec_version          = 18;
    final static int isc_tpb_restart_requests        = 19;
    final static int isc_tpb_no_auto_undo            = 20;

    /*final static Integer isc_tpb_version1                = 1;
    final static Integer isc_tpb_version3                = 3;
    final static Integer isc_tpb_consistency             = 1;
    final static Integer isc_tpb_concurrency             = 2;
    final static Integer isc_tpb_shared                  = 3;
    final static Integer isc_tpb_protected               = 4;
    final static Integer isc_tpb_exclusive               = 5;
    final static Integer isc_tpb_wait                    = 6;
    final static Integer isc_tpb_nowait                  = 7;
    final static Integer isc_tpb_read                    = 8;
    final static Integer isc_tpb_write                   = 9;
    final static Integer isc_tpb_lock_read               = 10;
    final static Integer isc_tpb_lock_write              = 11;
    final static Integer isc_tpb_verb_time               = 12;
    final static Integer isc_tpb_commit_time             = 13;
    final static Integer isc_tpb_ignore_limbo            = 14;
    final static Integer isc_tpb_read_committed          = 15;
    final static Integer isc_tpb_autocommit              = 16;
    final static Integer isc_tpb_rec_version             = 17;
    final static Integer isc_tpb_no_rec_version          = 18;
    final static Integer isc_tpb_restart_requests        = 19;
    final static Integer isc_tpb_no_auto_undo            = 20;
    */
    /****************************/
    /* Common, structural codes */
    /****************************/

    final static int isc_info_end                    = 1;
    final static int isc_info_truncated              = 2;
    final static int isc_info_error                  = 3;
    final static int isc_info_data_not_ready         = 4;
    final static int isc_info_flag_end               = 127;

    /*************************/
    /* SQL information items */
    /*************************/

    final static int isc_info_sql_select              = 4;
    final static int isc_info_sql_bind                = 5;
    final static int isc_info_sql_num_variables       = 6;
    final static int isc_info_sql_describe_vars       = 7;
    final static int isc_info_sql_describe_end        = 8;
    final static int isc_info_sql_sqlda_seq           = 9;
    final static int isc_info_sql_message_seq         = 10;
    final static int isc_info_sql_type                = 11;
    final static int isc_info_sql_sub_type            = 12;
    final static int isc_info_sql_scale               = 13;
    final static int isc_info_sql_length              = 14;
    final static int isc_info_sql_null_ind            = 15;
    final static int isc_info_sql_field               = 16;
    final static int isc_info_sql_relation            = 17;
    final static int isc_info_sql_owner               = 18;
    final static int isc_info_sql_alias               = 19;
    final static int isc_info_sql_sqlda_start         = 20;
    final static int isc_info_sql_stmt_type           = 21;
    final static int isc_info_sql_get_plan            = 22;
    final static int isc_info_sql_records             = 23;
    final static int isc_info_sql_batch_fetch         = 24;

    /*********************************/
    /* SQL information return values */
    /*********************************/

    final static int isc_info_sql_stmt_select         = 1;
    final static int isc_info_sql_stmt_insert         = 2;
    final static int isc_info_sql_stmt_update         = 3;
    final static int isc_info_sql_stmt_delete         = 4;
    final static int isc_info_sql_stmt_ddl            = 5;
    final static int isc_info_sql_stmt_get_segment    = 6;
    final static int isc_info_sql_stmt_put_segment    = 7;
    final static int isc_info_sql_stmt_exec_procedure = 8;
    final static int isc_info_sql_stmt_start_trans    = 9;
    final static int isc_info_sql_stmt_commit         = 10;
    final static int isc_info_sql_stmt_rollback       = 11;
    final static int isc_info_sql_stmt_select_for_upd = 12;
    final static int isc_info_sql_stmt_set_generator  = 13;

    /*****************************/
    /* Request information items */
    /*****************************/

    final static int isc_info_number_messages        =  4;
    final static int isc_info_max_message            =  5;
    final static int isc_info_max_send               =  6;
    final static int isc_info_max_receive            =  7;
    final static int isc_info_state                  =  8;
    final static int isc_info_message_number         =  9;
    final static int isc_info_message_size           =  10;
    final static int isc_info_request_cost           =  11;
    final static int isc_info_access_path            =  12;
    final static int isc_info_req_select_count       =  13;
    final static int isc_info_req_insert_count       =  14;
    final static int isc_info_req_update_count       =  15;
    final static int isc_info_req_delete_count       =  16;

/************************/
/* Blob Parameter Block */
/************************/

    final static int isc_bpb_version1                =  1;
    final static int isc_bpb_source_type             =  1;
    final static int isc_bpb_target_type             =  2;
    final static int isc_bpb_type                    =  3;
    final static int isc_bpb_source_interp           =  4;
    final static int isc_bpb_target_interp           =  5;
    final static int isc_bpb_filter_parameter        =  6;

    final static int isc_bpb_type_segmented          =  0;
    final static int isc_bpb_type_stream             =  1;

    final static int RBL_eof              = 1;
    final static int RBL_segment          = 2;
    final static int RBL_eof_pending      = 4;
    final static int RBL_create           = 8;


    /********************/
    /* ISC Error Codes */
    /*******************/

    final static int SUCCESS = 0;

    final static int isc_facility = 20;
    final static int isc_err_base = 335544320;
    final static int isc_err_factor = 1;
    final static int isc_arg_end = 0;           /* end of argument list */
    final static int isc_arg_gds = 1;           /* generic DSRI status value */
    final static int isc_arg_string = 2;        /* string argument */
    final static int isc_arg_cstring = 3;       /* count & string argument */
    final static int isc_arg_number = 4;        /* numeric argument (long) */
    final static int isc_arg_interpreted = 5;   /* interpreted status code (string) */
    final static int isc_arg_vms = 6;           /* VAX/VMS status code (long) */
    final static int isc_arg_unix = 7;          /* UNIX error code */
    final static int isc_arg_domain = 8;        /* Apollo/Domain error code */
    final static int isc_arg_dos = 9;           /* MSDOS/OS2 error code */
    final static int isc_arg_mpexl = 10;        /* HP MPE/XL error code */
    final static int isc_arg_mpexl_ipc = 11;    /* HP MPE/XL IPC error code */
    final static int isc_arg_next_mach = 15;    /* NeXT/Mach error code */
    final static int isc_arg_netware = 16;      /* NetWare error code */
    final static int isc_arg_win32 = 17;        /* Win32 error code */
    final static int isc_arg_warning = 18;      /* warning argument */

    final static int isc_arith_except                     = 335544321;
    final static int isc_bad_dbkey                        = 335544322;
    final static int isc_bad_db_format                    = 335544323;
    final static int isc_bad_db_handle                    = 335544324;
    final static int isc_bad_dpb_content                  = 335544325;
    final static int isc_bad_dpb_form                     = 335544326;
    final static int isc_bad_req_handle                   = 335544327;
    final static int isc_bad_segstr_handle                = 335544328;
    final static int isc_bad_segstr_id                    = 335544329;
    final static int isc_bad_tpb_content                  = 335544330;
    final static int isc_bad_tpb_form                     = 335544331;
    final static int isc_bad_trans_handle                 = 335544332;
    final static int isc_bug_check                        = 335544333;
    final static int isc_convert_error                    = 335544334;
    final static int isc_db_corrupt                       = 335544335;
    final static int isc_deadlock                         = 335544336;
    final static int isc_excess_trans                     = 335544337;
    final static int isc_from_no_match                    = 335544338;
    final static int isc_infinap                          = 335544339;
    final static int isc_infona                           = 335544340;
    final static int isc_infunk                           = 335544341;
    final static int isc_integ_fail                       = 335544342;
    final static int isc_invalid_blr                      = 335544343;
    final static int isc_io_error                         = 335544344;
    final static int isc_lock_conflict                    = 335544345;
    final static int isc_metadata_corrupt                 = 335544346;
    final static int isc_not_valid                        = 335544347;
    final static int isc_no_cur_rec                       = 335544348;
    final static int isc_no_dup                           = 335544349;
    final static int isc_no_finish                        = 335544350;
    final static int isc_no_meta_update                   = 335544351;
    final static int isc_no_priv                          = 335544352;
    final static int isc_no_recon                         = 335544353;
    final static int isc_no_record                        = 335544354;
    final static int isc_no_segstr_close                  = 335544355;
    final static int isc_obsolete_metadata                = 335544356;
    final static int isc_open_trans                       = 335544357;
    final static int isc_port_len                         = 335544358;
    final static int isc_read_only_field                  = 335544359;
    final static int isc_read_only_rel                    = 335544360;
    final static int isc_read_only_trans                  = 335544361;
    final static int isc_read_only_view                   = 335544362;
    final static int isc_req_no_trans                     = 335544363;
    final static int isc_req_sync                         = 335544364;
    final static int isc_req_wrong_db                     = 335544365;
    final static int isc_segment                          = 335544366;
    final static int isc_segstr_eof                       = 335544367;
    final static int isc_segstr_no_op                     = 335544368;
    final static int isc_segstr_no_read                   = 335544369;
    final static int isc_segstr_no_trans                  = 335544370;
    final static int isc_segstr_no_write                  = 335544371;
    final static int isc_segstr_wrong_db                  = 335544372;
    final static int isc_sys_request                      = 335544373;
    final static int isc_stream_eof                       = 335544374;
    final static int isc_unavailable                      = 335544375;
    final static int isc_unres_rel                        = 335544376;
    final static int isc_uns_ext                          = 335544377;
    final static int isc_wish_list                        = 335544378;
    final static int isc_wrong_ods                        = 335544379;
    final static int isc_wronumarg                        = 335544380;
    final static int isc_imp_exc                          = 335544381;
    final static int isc_random                           = 335544382;
    final static int isc_fatal_conflict                   = 335544383;
    final static int isc_badblk                           = 335544384;
    final static int isc_invpoolcl                        = 335544385;
    final static int isc_nopoolids                        = 335544386;
    final static int isc_relbadblk                        = 335544387;
    final static int isc_blktoobig                        = 335544388;
    final static int isc_bufexh                           = 335544389;
    final static int isc_syntaxerr                        = 335544390;
    final static int isc_bufinuse                         = 335544391;
    final static int isc_bdbincon                         = 335544392;
    final static int isc_reqinuse                         = 335544393;
    final static int isc_badodsver                        = 335544394;
    final static int isc_relnotdef                        = 335544395;
    final static int isc_fldnotdef                        = 335544396;
    final static int isc_dirtypage                        = 335544397;
    final static int isc_waifortra                        = 335544398;
    final static int isc_doubleloc                        = 335544399;
    final static int isc_nodnotfnd                        = 335544400;
    final static int isc_dupnodfnd                        = 335544401;
    final static int isc_locnotmar                        = 335544402;
    final static int isc_badpagtyp                        = 335544403;
    final static int isc_corrupt                          = 335544404;
    final static int isc_badpage                          = 335544405;
    final static int isc_badindex                         = 335544406;
    final static int isc_dbbnotzer                        = 335544407;
    final static int isc_tranotzer                        = 335544408;
    final static int isc_trareqmis                        = 335544409;
    final static int isc_badhndcnt                        = 335544410;
    final static int isc_wrotpbver                        = 335544411;
    final static int isc_wroblrver                        = 335544412;
    final static int isc_wrodpbver                        = 335544413;
    final static int isc_blobnotsup                       = 335544414;
    final static int isc_badrelation                      = 335544415;
    final static int isc_nodetach                         = 335544416;
    final static int isc_notremote                        = 335544417;
    final static int isc_trainlim                         = 335544418;
    final static int isc_notinlim                         = 335544419;
    final static int isc_traoutsta                        = 335544420;
    final static int isc_connect_reject                   = 335544421;
    final static int isc_dbfile                           = 335544422;
    final static int isc_orphan                           = 335544423;
    final static int isc_no_lock_mgr                      = 335544424;
    final static int isc_ctxinuse                         = 335544425;
    final static int isc_ctxnotdef                        = 335544426;
    final static int isc_datnotsup                        = 335544427;
    final static int isc_badmsgnum                        = 335544428;
    final static int isc_badparnum                        = 335544429;
    final static int isc_virmemexh                        = 335544430;
    final static int isc_blocking_signal                  = 335544431;
    final static int isc_lockmanerr                       = 335544432;
    final static int isc_journerr                         = 335544433;
    final static int isc_keytoobig                        = 335544434;
    final static int isc_nullsegkey                       = 335544435;
    final static int isc_sqlerr                           = 335544436;
    final static int isc_wrodynver                        = 335544437;
    final static int isc_funnotdef                        = 335544438;
    final static int isc_funmismat                        = 335544439;
    final static int isc_bad_msg_vec                      = 335544440;
    final static int isc_bad_detach                       = 335544441;
    final static int isc_noargacc_read                    = 335544442;
    final static int isc_noargacc_write                   = 335544443;
    final static int isc_read_only                        = 335544444;
    final static int isc_ext_err                          = 335544445;
    final static int isc_non_updatable                    = 335544446;
    final static int isc_no_rollback                      = 335544447;
    final static int isc_bad_sec_info                     = 335544448;
    final static int isc_invalid_sec_info                 = 335544449;
    final static int isc_misc_interpreted                 = 335544450;
    final static int isc_update_conflict                  = 335544451;
    final static int isc_unlicensed                       = 335544452;
    final static int isc_obj_in_use                       = 335544453;
    final static int isc_nofilter                         = 335544454;
    final static int isc_shadow_accessed                  = 335544455;
    final static int isc_invalid_sdl                      = 335544456;
    final static int isc_out_of_bounds                    = 335544457;
    final static int isc_invalid_dimension                = 335544458;
    final static int isc_rec_in_limbo                     = 335544459;
    final static int isc_shadow_missing                   = 335544460;
    final static int isc_cant_validate                    = 335544461;
    final static int isc_cant_start_journal               = 335544462;
    final static int isc_gennotdef                        = 335544463;
    final static int isc_cant_start_logging               = 335544464;
    final static int isc_bad_segstr_type                  = 335544465;
    final static int isc_foreign_key                      = 335544466;
    final static int isc_high_minor                       = 335544467;
    final static int isc_tra_state                        = 335544468;
    final static int isc_trans_invalid                    = 335544469;
    final static int isc_buf_invalid                      = 335544470;
    final static int isc_indexnotdefined                  = 335544471;
    final static int isc_login                            = 335544472;
    final static int isc_invalid_bookmark                 = 335544473;
    final static int isc_bad_lock_level                   = 335544474;
    final static int isc_relation_lock                    = 335544475;
    final static int isc_record_lock                      = 335544476;
    final static int isc_max_idx                          = 335544477;
    final static int isc_jrn_enable                       = 335544478;
    final static int isc_old_failure                      = 335544479;
    final static int isc_old_in_progress                  = 335544480;
    final static int isc_old_no_space                     = 335544481;
    final static int isc_no_wal_no_jrn                    = 335544482;
    final static int isc_num_old_files                    = 335544483;
    final static int isc_wal_file_open                    = 335544484;
    final static int isc_bad_stmt_handle                  = 335544485;
    final static int isc_wal_failure                      = 335544486;
    final static int isc_walw_err                         = 335544487;
    final static int isc_logh_small                       = 335544488;
    final static int isc_logh_inv_version                 = 335544489;
    final static int isc_logh_open_flag                   = 335544490;
    final static int isc_logh_open_flag2                  = 335544491;
    final static int isc_logh_diff_dbname                 = 335544492;
    final static int isc_logf_unexpected_eof              = 335544493;
    final static int isc_logr_incomplete                  = 335544494;
    final static int isc_logr_header_small                = 335544495;
    final static int isc_logb_small                       = 335544496;
    final static int isc_wal_illegal_attach               = 335544497;
    final static int isc_wal_invalid_wpb                  = 335544498;
    final static int isc_wal_err_rollover                 = 335544499;
    final static int isc_no_wal                           = 335544500;
    final static int isc_drop_wal                         = 335544501;
    final static int isc_stream_not_defined               = 335544502;
    final static int isc_wal_subsys_error                 = 335544503;
    final static int isc_wal_subsys_corrupt               = 335544504;
    final static int isc_no_archive                       = 335544505;
    final static int isc_shutinprog                       = 335544506;
    final static int isc_range_in_use                     = 335544507;
    final static int isc_range_not_found                  = 335544508;
    final static int isc_charset_not_found                = 335544509;
    final static int isc_lock_timeout                     = 335544510;
    final static int isc_prcnotdef                        = 335544511;
    final static int isc_prcmismat                        = 335544512;
    final static int isc_wal_bugcheck                     = 335544513;
    final static int isc_wal_cant_expand                  = 335544514;
    final static int isc_codnotdef                        = 335544515;
    final static int isc_xcpnotdef                        = 335544516;
    final static int isc_except                           = 335544517;
    final static int isc_cache_restart                    = 335544518;
    final static int isc_bad_lock_handle                  = 335544519;
    final static int isc_jrn_present                      = 335544520;
    final static int isc_wal_err_rollover2                = 335544521;
    final static int isc_wal_err_logwrite                 = 335544522;
    final static int isc_wal_err_jrn_comm                 = 335544523;
    final static int isc_wal_err_expansion                = 335544524;
    final static int isc_wal_err_setup                    = 335544525;
    final static int isc_wal_err_ww_sync                  = 335544526;
    final static int isc_wal_err_ww_start                 = 335544527;
    final static int isc_shutdown                         = 335544528;
    final static int isc_existing_priv_mod                = 335544529;
    final static int isc_primary_key_ref                  = 335544530;
    final static int isc_primary_key_notnull              = 335544531;
    final static int isc_ref_cnstrnt_notfound             = 335544532;
    final static int isc_foreign_key_notfound             = 335544533;
    final static int isc_ref_cnstrnt_update               = 335544534;
    final static int isc_check_cnstrnt_update             = 335544535;
    final static int isc_check_cnstrnt_del                = 335544536;
    final static int isc_integ_index_seg_del              = 335544537;
    final static int isc_integ_index_seg_mod              = 335544538;
    final static int isc_integ_index_del                  = 335544539;
    final static int isc_integ_index_mod                  = 335544540;
    final static int isc_check_trig_del                   = 335544541;
    final static int isc_check_trig_update                = 335544542;
    final static int isc_cnstrnt_fld_del                  = 335544543;
    final static int isc_cnstrnt_fld_rename               = 335544544;
    final static int isc_rel_cnstrnt_update               = 335544545;
    final static int isc_constaint_on_view                = 335544546;
    final static int isc_invld_cnstrnt_type               = 335544547;
    final static int isc_primary_key_exists               = 335544548;
    final static int isc_systrig_update                   = 335544549;
    final static int isc_not_rel_owner                    = 335544550;
    final static int isc_grant_obj_notfound               = 335544551;
    final static int isc_grant_fld_notfound               = 335544552;
    final static int isc_grant_nopriv                     = 335544553;
    final static int isc_nonsql_security_rel              = 335544554;
    final static int isc_nonsql_security_fld              = 335544555;
    final static int isc_wal_cache_err                    = 335544556;
    final static int isc_shutfail                         = 335544557;
    final static int isc_check_constraint                 = 335544558;
    final static int isc_bad_svc_handle                   = 335544559;
    final static int isc_shutwarn                         = 335544560;
    final static int isc_wrospbver                        = 335544561;
    final static int isc_bad_spb_form                     = 335544562;
    final static int isc_svcnotdef                        = 335544563;
    final static int isc_no_jrn                           = 335544564;
    final static int isc_transliteration_failed           = 335544565;
    final static int isc_start_cm_for_wal                 = 335544566;
    final static int isc_wal_ovflow_log_required          = 335544567;
    final static int isc_text_subtype                     = 335544568;
    final static int isc_dsql_error                       = 335544569;
    final static int isc_dsql_command_err                 = 335544570;
    final static int isc_dsql_constant_err                = 335544571;
    final static int isc_dsql_cursor_err                  = 335544572;
    final static int isc_dsql_datatype_err                = 335544573;
    final static int isc_dsql_decl_err                    = 335544574;
    final static int isc_dsql_cursor_update_err           = 335544575;
    final static int isc_dsql_cursor_open_err             = 335544576;
    final static int isc_dsql_cursor_close_err            = 335544577;
    final static int isc_dsql_field_err                   = 335544578;
    final static int isc_dsql_internal_err                = 335544579;
    final static int isc_dsql_relation_err                = 335544580;
    final static int isc_dsql_procedure_err               = 335544581;
    final static int isc_dsql_request_err                 = 335544582;
    final static int isc_dsql_sqlda_err                   = 335544583;
    final static int isc_dsql_var_count_err               = 335544584;
    final static int isc_dsql_stmt_handle                 = 335544585;
    final static int isc_dsql_function_err                = 335544586;
    final static int isc_dsql_blob_err                    = 335544587;
    final static int isc_collation_not_found              = 335544588;
    final static int isc_collation_not_for_charset        = 335544589;
    final static int isc_dsql_dup_option                  = 335544590;
    final static int isc_dsql_tran_err                    = 335544591;
    final static int isc_dsql_invalid_array               = 335544592;
    final static int isc_dsql_max_arr_dim_exceeded        = 335544593;
    final static int isc_dsql_arr_range_error             = 335544594;
    final static int isc_dsql_trigger_err                 = 335544595;
    final static int isc_dsql_subselect_err               = 335544596;
    final static int isc_dsql_crdb_prepare_err            = 335544597;
    final static int isc_specify_field_err                = 335544598;
    final static int isc_num_field_err                    = 335544599;
    final static int isc_col_name_err                     = 335544600;
    final static int isc_where_err                        = 335544601;
    final static int isc_table_view_err                   = 335544602;
    final static int isc_distinct_err                     = 335544603;
    final static int isc_key_field_count_err              = 335544604;
    final static int isc_subquery_err                     = 335544605;
    final static int isc_expression_eval_err              = 335544606;
    final static int isc_node_err                         = 335544607;
    final static int isc_command_end_err                  = 335544608;
    final static int isc_index_name                       = 335544609;
    final static int isc_exception_name                   = 335544610;
    final static int isc_field_name                       = 335544611;
    final static int isc_token_err                        = 335544612;
    final static int isc_union_err                        = 335544613;
    final static int isc_dsql_construct_err               = 335544614;
    final static int isc_field_aggregate_err              = 335544615;
    final static int isc_field_ref_err                    = 335544616;
    final static int isc_order_by_err                     = 335544617;
    final static int isc_return_mode_err                  = 335544618;
    final static int isc_extern_func_err                  = 335544619;
    final static int isc_alias_conflict_err               = 335544620;
    final static int isc_procedure_conflict_error         = 335544621;
    final static int isc_relation_conflict_err            = 335544622;
    final static int isc_dsql_domain_err                  = 335544623;
    final static int isc_idx_seg_err                      = 335544624;
    final static int isc_node_name_err                    = 335544625;
    final static int isc_table_name                       = 335544626;
    final static int isc_proc_name                        = 335544627;
    final static int isc_idx_create_err                   = 335544628;
    final static int isc_wal_shadow_err                   = 335544629;
    final static int isc_dependency                       = 335544630;
    final static int isc_idx_key_err                      = 335544631;
    final static int isc_dsql_file_length_err             = 335544632;
    final static int isc_dsql_shadow_number_err           = 335544633;
    final static int isc_dsql_token_unk_err               = 335544634;
    final static int isc_dsql_no_relation_alias           = 335544635;
    final static int isc_indexname                        = 335544636;
    final static int isc_no_stream_plan                   = 335544637;
    final static int isc_stream_twice                     = 335544638;
    final static int isc_stream_not_found                 = 335544639;
    final static int isc_collation_requires_text          = 335544640;
    final static int isc_dsql_domain_not_found            = 335544641;
    final static int isc_index_unused                     = 335544642;
    final static int isc_dsql_self_join                   = 335544643;
    final static int isc_stream_bof                       = 335544644;
    final static int isc_stream_crack                     = 335544645;
    final static int isc_db_or_file_exists                = 335544646;
    final static int isc_invalid_operator                 = 335544647;
    final static int isc_conn_lost                        = 335544648;
    final static int isc_bad_checksum                     = 335544649;
    final static int isc_page_type_err                    = 335544650;
    final static int isc_ext_readonly_err                 = 335544651;
    final static int isc_sing_select_err                  = 335544652;
    final static int isc_psw_attach                       = 335544653;
    final static int isc_psw_start_trans                  = 335544654;
    final static int isc_invalid_direction                = 335544655;
    final static int isc_dsql_var_conflict                = 335544656;
    final static int isc_dsql_no_blob_array               = 335544657;
    final static int isc_dsql_base_table                  = 335544658;
    final static int isc_duplicate_base_table             = 335544659;
    final static int isc_view_alias                       = 335544660;
    final static int isc_index_root_page_full             = 335544661;
    final static int isc_dsql_blob_type_unknown           = 335544662;
    final static int isc_req_max_clones_exceeded          = 335544663;
    final static int isc_dsql_duplicate_spec              = 335544664;
    final static int isc_unique_key_violation             = 335544665;
    final static int isc_srvr_version_too_old             = 335544666;
    final static int isc_drdb_completed_with_errs         = 335544667;
    final static int isc_dsql_procedure_use_err           = 335544668;
    final static int isc_dsql_count_mismatch              = 335544669;
    final static int isc_blob_idx_err                     = 335544670;
    final static int isc_array_idx_err                    = 335544671;
    final static int isc_key_field_err                    = 335544672;
    final static int isc_no_delete                        = 335544673;
    final static int isc_del_last_field                   = 335544674;
    final static int isc_sort_err                         = 335544675;
    final static int isc_sort_mem_err                     = 335544676;
    final static int isc_version_err                      = 335544677;
    final static int isc_inval_key_posn                   = 335544678;
    final static int isc_no_segments_err                  = 335544679;
    final static int isc_crrp_data_err                    = 335544680;
    final static int isc_rec_size_err                     = 335544681;
    final static int isc_dsql_field_ref                   = 335544682;
    final static int isc_req_depth_exceeded               = 335544683;
    final static int isc_no_field_access                  = 335544684;
    final static int isc_no_dbkey                         = 335544685;
    final static int isc_jrn_format_err                   = 335544686;
    final static int isc_jrn_file_full                    = 335544687;
    final static int isc_dsql_open_cursor_request         = 335544688;
    final static int isc_ib_error                         = 335544689;
    final static int isc_cache_redef                      = 335544690;
    final static int isc_cache_too_small                  = 335544691;
    final static int isc_log_redef                        = 335544692;
    final static int isc_log_too_small                    = 335544693;
    final static int isc_partition_too_small              = 335544694;
    final static int isc_partition_not_supp               = 335544695;
    final static int isc_log_length_spec                  = 335544696;
    final static int isc_precision_err                    = 335544697;
    final static int isc_scale_nogt                       = 335544698;
    final static int isc_expec_int                      = 335544699;
    final static int isc_expec_long                       = 335544700;
    final static int isc_expec_uint                     = 335544701;
    final static int isc_like_escape_invalid              = 335544702;
    final static int isc_svcnoexe                         = 335544703;
    final static int isc_net_lookup_err                   = 335544704;
    final static int isc_service_unknown                  = 335544705;
    final static int isc_host_unknown                     = 335544706;
    final static int isc_grant_nopriv_on_base             = 335544707;
    final static int isc_dyn_fld_ambiguous                = 335544708;
    final static int isc_dsql_agg_ref_err                 = 335544709;
    final static int isc_complex_view                     = 335544710;
    final static int isc_unprepared_stmt                  = 335544711;
    final static int isc_expec_positive                   = 335544712;
    final static int isc_dsql_sqlda_value_err             = 335544713;
    final static int isc_invalid_array_id                 = 335544714;
    final static int isc_extfile_uns_op                   = 335544715;
    final static int isc_svc_in_use                       = 335544716;
    final static int isc_err_stack_limit                  = 335544717;
    final static int isc_invalid_key                      = 335544718;
    final static int isc_net_init_error                   = 335544719;
    final static int isc_loadlib_failure                  = 335544720;
    final static int isc_network_error                    = 335544721;
    final static int isc_net_connect_err                  = 335544722;
    final static int isc_net_connect_listen_err           = 335544723;
    final static int isc_net_event_connect_err            = 335544724;
    final static int isc_net_event_listen_err             = 335544725;
    final static int isc_net_read_err                     = 335544726;
    final static int isc_net_write_err                    = 335544727;
    final static int isc_integ_index_deactivate           = 335544728;
    final static int isc_integ_deactivate_primary         = 335544729;
    final static int isc_cse_not_supported                = 335544730;
    final static int isc_tra_must_sweep                   = 335544731;
    final static int isc_unsupported_network_drive        = 335544732;
    final static int isc_io_create_err                    = 335544733;
    final static int isc_io_open_err                      = 335544734;
    final static int isc_io_close_err                     = 335544735;
    final static int isc_io_read_err                      = 335544736;
    final static int isc_io_write_err                     = 335544737;
    final static int isc_io_delete_err                    = 335544738;
    final static int isc_io_access_err                    = 335544739;
    final static int isc_udf_exception                    = 335544740;
    final static int isc_lost_db_connection               = 335544741;
    final static int isc_no_write_user_priv               = 335544742;
    final static int isc_token_too_long                   = 335544743;
    final static int isc_max_att_exceeded                 = 335544744;
    final static int isc_login_same_as_role_name          = 335544745;
    final static int isc_reftable_requires_pk             = 335544746;
    final static int isc_usrname_too_long                 = 335544747;
    final static int isc_password_too_long                = 335544748;
    final static int isc_usrname_required                 = 335544749;
    final static int isc_password_required                = 335544750;
    final static int isc_bad_protocol                     = 335544751;
    final static int isc_dup_usrname_found                = 335544752;
    final static int isc_usrname_not_found                = 335544753;
    final static int isc_error_adding_sec_record          = 335544754;
    final static int isc_error_modifying_sec_record       = 335544755;
    final static int isc_error_deleting_sec_record        = 335544756;
    final static int isc_error_updating_sec_db            = 335544757;
    final static int isc_sort_rec_size_err                = 335544758;
    final static int isc_bad_default_value                = 335544759;
    final static int isc_invalid_clause                   = 335544760;
    final static int isc_too_many_handles                 = 335544761;
    final static int isc_optimizer_blk_exc                = 335544762;
    final static int isc_invalid_string_constant          = 335544763;
    final static int isc_transitional_date                = 335544764;
    final static int isc_read_only_database               = 335544765;
    final static int isc_must_be_dialect_2_and_up         = 335544766;
    final static int isc_blob_filter_exception            = 335544767;
    final static int isc_exception_access_violation       = 335544768;
    final static int isc_exception_datatype_missalignment = 335544769;
    final static int isc_exception_array_bounds_exceeded  = 335544770;
    final static int isc_exception_float_denormal_operand = 335544771;
    final static int isc_exception_float_divide_by_zero   = 335544772;
    final static int isc_exception_float_inexact_result   = 335544773;
    final static int isc_exception_float_invalid_operand  = 335544774;
    final static int isc_exception_float_overflow         = 335544775;
    final static int isc_exception_float_stack_check      = 335544776;
    final static int isc_exception_float_underflow        = 335544777;
    final static int isc_exception_integer_divide_by_zero = 335544778;
    final static int isc_exception_integer_overflow       = 335544779;
    final static int isc_exception_unknown                = 335544780;
    final static int isc_exception_stack_overflow         = 335544781;
    final static int isc_exception_sigsegv                = 335544782;
    final static int isc_exception_sigill                 = 335544783;
    final static int isc_exception_sigbus                 = 335544784;
    final static int isc_exception_sigfpe                 = 335544785;
    final static int isc_ext_file_delete                  = 335544786;
    final static int isc_ext_file_modify                  = 335544787;
    final static int isc_adm_task_denied                  = 335544788;
    final static int isc_extract_input_mismatch           = 335544789;
    final static int isc_insufficient_svc_privileges      = 335544790;
    final static int isc_file_in_use                      = 335544791;
    final static int isc_service_att_err                  = 335544792;
    final static int isc_ddl_not_allowed_by_db_sql_dial   = 335544793;
    final static int isc_cancelled                        = 335544794;
    final static int isc_unexp_spb_form                   = 335544795;
    final static int isc_sql_dialect_datatype_unsupport   = 335544796;
    final static int isc_svcnouser                        = 335544797;
    final static int isc_depend_on_uncommitted_rel        = 335544798;
    final static int isc_svc_name_missing                 = 335544799;
    final static int isc_too_many_contexts                = 335544800;
    final static int isc_datype_notsup                    = 335544801;
    final static int isc_dialect_reset_warning            = 335544802;
    final static int isc_dialect_not_changed              = 335544803;
    final static int isc_database_create_failed           = 335544804;
    final static int isc_inv_dialect_specified            = 335544805;
    final static int isc_valid_db_dialects                = 335544806;
    final static int isc_sqlwarn                          = 335544807;
    final static int isc_dtype_renamed                    = 335544808;
    final static int isc_extern_func_dir_error            = 335544809;
    final static int isc_date_range_exceeded              = 335544810;
    final static int isc_inv_client_dialect_specified     = 335544811;
    final static int isc_valid_client_dialects            = 335544812;
    final static int isc_optimizer_between_err            = 335544813;
    final static int isc_service_not_supported            = 335544814;
    final static int isc_gfix_db_name                     = 335740929;
    final static int isc_gfix_invalid_sw                  = 335740930;
    final static int isc_gfix_incmp_sw                    = 335740932;
    final static int isc_gfix_replay_req                  = 335740933;
    final static int isc_gfix_pgbuf_req                   = 335740934;
    final static int isc_gfix_val_req                     = 335740935;
    final static int isc_gfix_pval_req                    = 335740936;
    final static int isc_gfix_trn_req                     = 335740937;
    final static int isc_gfix_full_req                    = 335740940;
    final static int isc_gfix_usrname_req                 = 335740941;
    final static int isc_gfix_pass_req                    = 335740942;
    final static int isc_gfix_subs_name                   = 335740943;
    final static int isc_gfix_wal_req                     = 335740944;
    final static int isc_gfix_sec_req                     = 335740945;
    final static int isc_gfix_nval_req                    = 335740946;
    final static int isc_gfix_type_shut                   = 335740947;
    final static int isc_gfix_retry                       = 335740948;
    final static int isc_gfix_retry_db                    = 335740951;
    final static int isc_gfix_exceed_max                  = 335740991;
    final static int isc_gfix_corrupt_pool                = 335740992;
    final static int isc_gfix_mem_exhausted               = 335740993;
    final static int isc_gfix_bad_pool                    = 335740994;
    final static int isc_gfix_trn_not_valid               = 335740995;
    final static int isc_gfix_unexp_eoi                   = 335741012;
    final static int isc_gfix_recon_fail                  = 335741018;
    final static int isc_gfix_trn_unknown                 = 335741036;
    final static int isc_gfix_mode_req                    = 335741038;
    final static int isc_gfix_opt_SQL_dialect             = 335741039;
    final static int isc_dsql_dbkey_from_non_table        = 336003074;
    final static int isc_dsql_transitional_numeric        = 336003075;
    final static int isc_dsql_dialect_warning_expr        = 336003076;
    final static int isc_sql_db_dialect_dtype_unsupport   = 336003077;
    final static int isc_isc_sql_dialect_conflict_num     = 336003079;
    final static int isc_dsql_warning_number_ambiguous    = 336003080;
    final static int isc_dsql_warning_number_ambiguous1   = 336003081;
    final static int isc_dsql_warn_precision_ambiguous    = 336003082;
    final static int isc_dsql_warn_precision_ambiguous1   = 336003083;
    final static int isc_dsql_warn_precision_ambiguous2   = 336003084;
    final static int isc_dyn_role_does_not_exist          = 336068796;
    final static int isc_dyn_no_grant_admin_opt           = 336068797;
    final static int isc_dyn_user_not_role_member         = 336068798;
    final static int isc_dyn_delete_role_failed           = 336068799;
    final static int isc_dyn_grant_role_to_user           = 336068800;
    final static int isc_dyn_inv_sql_role_name            = 336068801;
    final static int isc_dyn_dup_sql_role                 = 336068802;
    final static int isc_dyn_kywd_spec_for_role           = 336068803;
    final static int isc_dyn_roles_not_supported          = 336068804;
    final static int isc_dyn_domain_name_exists           = 336068812;
    final static int isc_dyn_field_name_exists            = 336068813;
    final static int isc_dyn_dependency_exists            = 336068814;
    final static int isc_dyn_dtype_invalid                = 336068815;
    final static int isc_dyn_char_fld_too_small           = 336068816;
    final static int isc_dyn_invalid_dtype_conversion     = 336068817;
    final static int isc_dyn_dtype_conv_invalid           = 336068818;
    final static int isc_gbak_unknown_switch              = 336330753;
    final static int isc_gbak_page_size_missing           = 336330754;
    final static int isc_gbak_page_size_toobig            = 336330755;
    final static int isc_gbak_redir_ouput_missing         = 336330756;
    final static int isc_gbak_switches_conflict           = 336330757;
    final static int isc_gbak_unknown_device              = 336330758;
    final static int isc_gbak_no_protection               = 336330759;
    final static int isc_gbak_page_size_not_allowed       = 336330760;
    final static int isc_gbak_multi_source_dest           = 336330761;
    final static int isc_gbak_filename_missing            = 336330762;
    final static int isc_gbak_dup_inout_names             = 336330763;
    final static int isc_gbak_inv_page_size               = 336330764;
    final static int isc_gbak_db_specified                = 336330765;
    final static int isc_gbak_db_exists                   = 336330766;
    final static int isc_gbak_unk_device                  = 336330767;
    final static int isc_gbak_blob_info_failed            = 336330772;
    final static int isc_gbak_unk_blob_item               = 336330773;
    final static int isc_gbak_get_seg_failed              = 336330774;
    final static int isc_gbak_close_blob_failed           = 336330775;
    final static int isc_gbak_open_blob_failed            = 336330776;
    final static int isc_gbak_put_blr_gen_id_failed       = 336330777;
    final static int isc_gbak_unk_type                    = 336330778;
    final static int isc_gbak_comp_req_failed             = 336330779;
    final static int isc_gbak_start_req_failed            = 336330780;
    final static int isc_gbak_rec_failed                  = 336330781;
    final static int isc_gbak_rel_req_failed              = 336330782;
    final static int isc_gbak_db_info_failed              = 336330783;
    final static int isc_gbak_no_db_desc                  = 336330784;
    final static int isc_gbak_db_create_failed            = 336330785;
    final static int isc_gbak_decomp_len_error            = 336330786;
    final static int isc_gbak_tbl_missing                 = 336330787;
    final static int isc_gbak_blob_col_missing            = 336330788;
    final static int isc_gbak_create_blob_failed          = 336330789;
    final static int isc_gbak_put_seg_failed              = 336330790;
    final static int isc_gbak_rec_len_exp                 = 336330791;
    final static int isc_gbak_inv_rec_len                 = 336330792;
    final static int isc_gbak_exp_data_type               = 336330793;
    final static int isc_gbak_gen_id_failed               = 336330794;
    final static int isc_gbak_unk_rec_type                = 336330795;
    final static int isc_gbak_inv_bkup_ver                = 336330796;
    final static int isc_gbak_missing_bkup_desc           = 336330797;
    final static int isc_gbak_string_trunc                = 336330798;
    final static int isc_gbak_cant_rest_record            = 336330799;
    final static int isc_gbak_send_failed                 = 336330800;
    final static int isc_gbak_no_tbl_name                 = 336330801;
    final static int isc_gbak_unexp_eof                   = 336330802;
    final static int isc_gbak_db_format_too_old           = 336330803;
    final static int isc_gbak_inv_array_dim               = 336330804;
    final static int isc_gbak_xdr_len_expected            = 336330807;
    final static int isc_gbak_open_bkup_error             = 336330817;
    final static int isc_gbak_open_error                  = 336330818;
    final static int isc_gbak_missing_block_fac           = 336330934;
    final static int isc_gbak_inv_block_fac               = 336330935;
    final static int isc_gbak_block_fac_specified         = 336330936;
    final static int isc_gbak_missing_username            = 336330940;
    final static int isc_gbak_missing_password            = 336330941;
    final static int isc_gbak_missing_skipped_bytes       = 336330952;
    final static int isc_gbak_inv_skipped_bytes           = 336330953;
    final static int isc_gbak_err_restore_charset         = 336330965;
    final static int isc_gbak_err_restore_collation       = 336330967;
    final static int isc_gbak_read_error                  = 336330972;
    final static int isc_gbak_write_error                 = 336330973;
    final static int isc_gbak_db_in_use                   = 336330985;
    final static int isc_gbak_sysmemex                    = 336330990;
    final static int isc_gbak_restore_role_failed         = 336331002;
    final static int isc_gbak_role_op_missing             = 336331005;
    final static int isc_gbak_page_buffers_missing        = 336331010;
    final static int isc_gbak_page_buffers_wrong_param    = 336331011;
    final static int isc_gbak_page_buffers_restore        = 336331012;
    final static int isc_gbak_inv_size                    = 336331014;
    final static int isc_gbak_file_outof_sequence         = 336331015;
    final static int isc_gbak_join_file_missing           = 336331016;
    final static int isc_gbak_stdin_not_supptd            = 336331017;
    final static int isc_gbak_stdout_not_supptd           = 336331018;
    final static int isc_gbak_bkup_corrupt                = 336331019;
    final static int isc_gbak_unk_db_file_spec            = 336331020;
    final static int isc_gbak_hdr_write_failed            = 336331021;
    final static int isc_gbak_disk_space_ex               = 336331022;
    final static int isc_gbak_size_lt_min                 = 336331023;
    final static int isc_gbak_svc_name_missing            = 336331025;
    final static int isc_gbak_not_ownr                    = 336331026;
    final static int isc_gbak_mode_req                    = 336331031;
    final static int isc_gsec_cant_open_db                = 336723983;
    final static int isc_gsec_switches_error              = 336723984;
    final static int isc_gsec_no_op_spec                  = 336723985;
    final static int isc_gsec_no_usr_name                 = 336723986;
    final static int isc_gsec_err_add                     = 336723987;
    final static int isc_gsec_err_modify                  = 336723988;
    final static int isc_gsec_err_find_mod                = 336723989;
    final static int isc_gsec_err_rec_not_found           = 336723990;
    final static int isc_gsec_err_delete                  = 336723991;
    final static int isc_gsec_err_find_del                = 336723992;
    final static int isc_gsec_err_find_disp               = 336723996;
    final static int isc_gsec_inv_param                   = 336723997;
    final static int isc_gsec_op_specified                = 336723998;
    final static int isc_gsec_pw_specified                = 336723999;
    final static int isc_gsec_uid_specified               = 336724000;
    final static int isc_gsec_gid_specified               = 336724001;
    final static int isc_gsec_proj_specified              = 336724002;
    final static int isc_gsec_org_specified               = 336724003;
    final static int isc_gsec_fname_specified             = 336724004;
    final static int isc_gsec_mname_specified             = 336724005;
    final static int isc_gsec_lname_specified             = 336724006;
    final static int isc_gsec_inv_switch                  = 336724008;
    final static int isc_gsec_amb_switch                  = 336724009;
    final static int isc_gsec_no_op_specified             = 336724010;
    final static int isc_gsec_params_not_allowed          = 336724011;
    final static int isc_gsec_incompat_switch             = 336724012;
    final static int isc_gsec_inv_username                = 336724044;
    final static int isc_gsec_inv_pw_length               = 336724045;
    final static int isc_gsec_db_specified                = 336724046;
    final static int isc_gsec_db_admin_specified          = 336724047;
    final static int isc_gsec_db_admin_pw_specified       = 336724048;
    final static int isc_gsec_sql_role_specified          = 336724049;
    final static int isc_license_no_file                  = 336789504;
    final static int isc_license_op_specified             = 336789523;
    final static int isc_license_op_missing               = 336789524;
    final static int isc_license_inv_switch               = 336789525;
    final static int isc_license_inv_switch_combo         = 336789526;
    final static int isc_license_inv_op_combo             = 336789527;
    final static int isc_license_amb_switch               = 336789528;
    final static int isc_license_inv_parameter            = 336789529;
    final static int isc_license_param_specified          = 336789530;
    final static int isc_license_param_req                = 336789531;
    final static int isc_license_syntx_error              = 336789532;
    final static int isc_license_dup_id                   = 336789534;
    final static int isc_license_inv_id_key               = 336789535;
    final static int isc_license_err_remove               = 336789536;
    final static int isc_license_err_update               = 336789537;
    final static int isc_license_err_convert              = 336789538;
    final static int isc_license_err_unk                  = 336789539;
    final static int isc_license_svc_err_add              = 336789540;
    final static int isc_license_svc_err_remove           = 336789541;
    final static int isc_license_eval_exists              = 336789563;
    final static int isc_gstat_unknown_switch             = 336920577;
    final static int isc_gstat_retry                      = 336920578;
    final static int isc_gstat_wrong_ods                  = 336920579;
    final static int isc_gstat_unexpected_eof             = 336920580;
    final static int isc_gstat_open_err                   = 336920605;
    final static int isc_gstat_read_err                   = 336920606;
    final static int isc_gstat_sysmemex                   = 336920607;
    final static int isc_err_max                          = 689;

    /*******************/
    /* SQL definitions */
    /*******************/

    final static int SQL_TEXT      = 452;
    final static int SQL_VARYING   = 448;
    final static int SQL_SHORT     = 500;
    final static int SQL_LONG      = 496;
    final static int SQL_FLOAT     = 482;
    final static int SQL_DOUBLE    = 480;
    final static int SQL_D_FLOAT   = 530;
    final static int SQL_TIMESTAMP = 510;
    final static int SQL_BLOB      = 520;
    final static int SQL_ARRAY     = 540;
    final static int SQL_QUAD      = 550;
    final static int SQL_TYPE_TIME = 560;
    final static int SQL_TYPE_DATE = 570;
    final static int SQL_INT64     = 580;

    /* Historical alias for pre V6 applications */
    final static int SQL_DATE      = SQL_TIMESTAMP;



    // Database functions

    void isc_create_database(String file_name,
                            isc_db_handle db_handle,
                            Clumplet c ) throws GDSException;

    void isc_attach_database(String file_name,
                            isc_db_handle db_handle,
                            Clumplet c) throws GDSException;

    void isc_database_info(isc_db_handle db_handle,
                            int item_length,
                            byte[] items,
                            int buffer_length,
                            byte[] buffer) throws GDSException;

    void isc_detach_database(isc_db_handle db_handle) throws GDSException;

    void isc_drop_database(isc_db_handle db_handle) throws GDSException;

    byte[] isc_expand_dpb(byte[] dpb, int dpb_length,
                          int param, Object[] params) throws GDSException;


    // Transactions

    void isc_start_transaction(    isc_tr_handle tr_handle,
                                isc_db_handle db_handle,
                                Set tpb) throws GDSException;


    void isc_commit_transaction(    isc_tr_handle tr_handle) throws GDSException;

    void isc_commit_retaining(isc_tr_handle tr_handle) throws GDSException;

    void isc_prepare_transaction(isc_tr_handle tr_handle) throws GDSException;

    void isc_prepare_transaction2(isc_tr_handle tr_handle,
                                   byte[] bytes) throws GDSException;

    void isc_rollback_transaction(isc_tr_handle tr_handle) throws GDSException;

    void isc_rollback_retaining(isc_tr_handle tr_handle) throws GDSException;


    // Dynamic SQL

    void isc_dsql_allocate_statement(isc_db_handle db_handle,
                                       isc_stmt_handle stmt_handle) throws GDSException;

    void isc_dsql_alloc_statement2(isc_db_handle db_handle,
                                     isc_stmt_handle stmt_handle) throws GDSException;

    XSQLDA isc_dsql_describe(isc_stmt_handle stmt_handle,
                            int da_version) throws GDSException;

    XSQLDA isc_dsql_describe_bind(isc_stmt_handle stmt_handle,
                                  int da_version) throws GDSException;

    void isc_dsql_execute(isc_tr_handle tr_handle,
                           isc_stmt_handle stmt_handle,
                           int da_version,
                           XSQLDA xsqlda) throws GDSException;

    void isc_dsql_execute2(isc_tr_handle tr_handle,
                            isc_stmt_handle stmt_handle,
                            int da_version,
                            XSQLDA in_xsqlda,
                            XSQLDA out_xsqlda) throws GDSException;

    void isc_dsql_execute_immediate(isc_db_handle db_handle,
                                      isc_tr_handle tr_handle,
                                      String statement,
                                      int dialect,
                                      XSQLDA xsqlda) throws GDSException;
    void isc_dsql_execute_immediate(isc_db_handle db_handle,
                                      isc_tr_handle tr_handle,
                                      String statement,
                                      String encoding,
                                      int dialect,
                                      XSQLDA xsqlda) throws GDSException;

    void isc_dsql_exec_immed2(isc_db_handle db_handle,
                               isc_tr_handle tr_handle,
                               String statement,
                               int dialect,
                               XSQLDA in_xsqlda,
                               XSQLDA out_xsqlda) throws GDSException;
                               
    void isc_dsql_exec_immed2(isc_db_handle db_handle,
                               isc_tr_handle tr_handle,
                               String statement,
                               String encoding,
                               int dialect,
                               XSQLDA in_xsqlda,
                               XSQLDA out_xsqlda) throws GDSException;

    byte[][] isc_dsql_fetch(isc_stmt_handle stmt_handle,
                         int da_version,
                         XSQLDA xsqlda) throws GDSException;

    void isc_dsql_free_statement(isc_stmt_handle stmt_handle,
                                   int option) throws GDSException;

    XSQLDA isc_dsql_prepare(isc_tr_handle tr_handle,
                           isc_stmt_handle stmt_handle,
                           String statement,
                           int dialect) throws GDSException;
                           
    XSQLDA isc_dsql_prepare(isc_tr_handle tr_handle,
                           isc_stmt_handle stmt_handle,
                           String statement,
                           String encoding,
                           int dialect) throws GDSException;

    void isc_dsql_set_cursor_name(isc_stmt_handle stmt_handle,
                                    String cursor_name,
                                    int type) throws GDSException;


    byte[] isc_dsql_sql_info(isc_stmt_handle stmt_handle,
                            int item_length,
                            byte[] items,
                            int buffer_length) throws GDSException;


    int isc_vax_integer(byte[] buffer, int pos, int length);


    //-----------------------------------------------
    //Blob methods
    //-----------------------------------------------

    void isc_create_blob2(isc_db_handle db,
                        isc_tr_handle tr,
                        isc_blob_handle blob,
                        Clumplet bpb) throws GDSException;

    void isc_open_blob2(isc_db_handle db,
                        isc_tr_handle tr,
                        isc_blob_handle blob,
                        Clumplet bpb) throws GDSException;

    byte[] isc_get_segment(isc_blob_handle blob,
                           int maxread) throws GDSException;

    void isc_put_segment(isc_blob_handle blob_handle,
             byte[] buffer) throws GDSException;

    void isc_close_blob(isc_blob_handle blob) throws GDSException;

    // Handle declaration methods
    isc_db_handle get_new_isc_db_handle();

    isc_tr_handle get_new_isc_tr_handle();

    isc_stmt_handle get_new_isc_stmt_handle();

    isc_blob_handle get_new_isc_blob_handle();
}

