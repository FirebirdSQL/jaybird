// SPDX-FileCopyrightText: 2000-2024 Firebird development team and individual contributors
// SPDX-FileCopyrightText: Copyright 2013-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
// SPDX-FileComment: The constants listed here were obtained from the Firebird sources, which are licensed under the IPL (InterBase Public License) and/or IDPL (Initial Developer Public License), both are variants of the Mozilla Public License version 1.1
package org.firebirdsql.gds;

/**
 * Constants for the blr.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
@SuppressWarnings({ "unused", "java:S115" , "java:S1214" })
public interface BlrConstants {
    int blr_text = 14;
    int blr_text2 = 15;
    int blr_short = 7;
    int blr_long = 8;
    int blr_quad = 9;
    int blr_float = 10;
    int blr_double = 27;
    int blr_d_float = 11;
    int blr_timestamp = 35;
    int blr_date = blr_timestamp;
    int blr_varying = 37;
    int blr_varying2 = 38;
    int blr_blob = 261;
    int blr_cstring = 40;
    int blr_cstring2 = 41;
    int blr_blob_id = 45;
    int blr_sql_date = 12;
    int blr_sql_time = 13;
    int blr_int64 = 16;
    int blr_blob2 = 17;
    int blr_domain_name = 18;
    int blr_domain_name2 = 19;
    int blr_not_nullable = 20;
    int blr_column_name = 21;
    int blr_column_name2 = 22;
    int blr_bool = 23;
    int blr_dec64 = 24;
    int blr_dec128 = 25;
    int blr_int128 = 26;
    int blr_sql_time_tz = 28;
    int blr_timestamp_tz = 29;
    int blr_ex_time_tz = 30;
    int blr_ex_timestamp_tz = 31;

    int blr_domain_type_of = 0;
    int blr_domain_full = 1;

    int blr_inner = 0;
    int blr_left = 1;
    int blr_right = 2;
    int blr_full = 3;
    int blr_gds_code = 0;
    int blr_sql_code = 1;
    int blr_exception = 2;
    int blr_trigger_code = 3;
    int blr_default_code = 4;
    int blr_raise = 5;
    int blr_exception_msg = 6;
    int blr_exception_params = 7;
    int blr_version4 = 4;
    int blr_version5 = 5;
    //int blr_version6 = 6;
    int blr_eoc = 76;
    int blr_end = 255;
    int blr_assignment = 1;
    int blr_begin = 2;
    int blr_dcl_variable = 3;
    int blr_message = 4;
    int blr_erase = 5;
    int blr_fetch = 6;
    int blr_for = 7;
    int blr_if = 8;
    int blr_loop = 9;
    int blr_modify = 10;
    int blr_handler = 11;
    int blr_receive = 12;
    int blr_select = 13;
    int blr_send = 14;
    int blr_store = 15;
    int blr_label = 17;
    int blr_leave = 18;
    int blr_store2 = 19;
    int blr_post = 20;
    int blr_literal = 21;
    int blr_dbkey = 22;
    int blr_field = 23;
    int blr_fid = 24;
    int blr_parameter = 25;
    int blr_variable = 26;
    int blr_average = 27;
    int blr_count = 28;
    int blr_maximum = 29;
    int blr_minimum = 30;
    int blr_total = 31;

    int blr_add = 34;
    int blr_subtract = 35;
    int blr_multiply = 36;
    int blr_divide = 37;
    int blr_negate = 38;
    int blr_concatenate = 39;
    int blr_substring = 40;
    int blr_parameter2 = 41;
    int blr_from = 42;
    int blr_via = 43;
    int blr_user_name = 44;
    int blr_null = 45;
    int blr_equiv = 46;
    int blr_eql = 47;
    int blr_neq = 48;
    int blr_gtr = 49;
    int blr_geq = 50;
    int blr_lss = 51;
    int blr_leq = 52;
    int blr_containing = 53;
    int blr_matching = 54;
    int blr_starting = 55;
    int blr_between = 56;
    int blr_or = 57;
    int blr_and = 58;
    int blr_not = 59;
    int blr_any = 60;
    int blr_missing = 61;
    int blr_unique = 62;
    int blr_like = 63;

    int blr_rse = 67;
    int blr_first = 68;
    int blr_project = 69;
    int blr_sort = 70;
    int blr_boolean = 71;
    int blr_ascending = 72;
    int blr_descending = 73;
    int blr_relation = 74;
    int blr_rid = 75;
    int blr_union = 76;
    int blr_map = 77;
    int blr_group_by = 78;
    int blr_aggregate = 79;
    int blr_join_type = 80;

    int blr_agg_count = 83;
    int blr_agg_max = 84;
    int blr_agg_min = 85;
    int blr_agg_total = 86;
    int blr_agg_average = 87;
    int blr_parameter3 = 88;

    /* unsupported
    int blr_run_max = 89;
    int blr_run_min = 90;
    int blr_run_total = 91;
    int blr_run_average = 92;
    */

    int blr_agg_count2 = 93;
    int blr_agg_count_distinct = 94;
    int blr_agg_total_distinct = 95;
    int blr_agg_average_distinct = 96;

    int blr_function = 100;
    int blr_gen_id = 101;
    //int blr_prot_mask = 102;
    int blr_upcase = 103;
    //int blr_lock_state = 104;
    int blr_value_if = 105;
    int blr_matching2 = 106;
    int blr_index = 107;
    int blr_ansi_like = 108;
    int blr_scrollable = 109;

    int blr_run_count = 118;
    int blr_rs_stream = 119;
    int blr_exec_proc = 120;

    int blr_procedure = 124;
    int blr_pid = 125;
    int blr_exec_pid = 126;
    int blr_singular = 127;
    int blr_abort = 128;
    int blr_block = 129;
    int blr_error_handler = 130;
    int blr_cast = 131;
    int blr_pid2 = 132;
    int blr_procedure2 = 133;
    int blr_start_savepoint = 134;
    int blr_end_savepoint = 135;

    int blr_plan = 139;
    int blr_merge = 140;
    int blr_join = 141;
    int blr_sequential = 142;
    int blr_navigational = 143;
    int blr_indices = 144;
    int blr_retrieve = 145;
    int blr_relation2 = 146;
    int blr_rid2 = 147;

    int blr_set_generator = 150;
    int blr_ansi_any = 151;
    int blr_exists = 152;

    int blr_record_version = 154;
    int blr_stall = 155;

    int blr_ansi_all = 158;
    int blr_extract = 159;

    int blr_extract_year = 0;
    int blr_extract_month = 1;
    int blr_extract_day = 2;
    int blr_extract_hour = 3;
    int blr_extract_minute = 4;
    int blr_extract_second = 5;
    int blr_extract_weekday = 6;
    int blr_extract_yearday = 7;
    int blr_extract_millisecond = 8;
    int blr_extract_week = 9;
    int blr_current_date = 160;
    int blr_current_timestamp = 161;
    int blr_current_time = 162;

    int blr_post_arg = 163;
    int blr_exec_into = 164;
    int blr_user_savepoint = 165;
    int blr_dcl_cursor = 166;
    int blr_cursor_stmt = 167;
    int blr_current_timestamp2 = 168;
    int blr_current_time2 = 169;
    int blr_agg_list = 170;
    int blr_agg_list_distinct = 171;
    int blr_modify2 = 172;

    int blr_current_role = 174;
    int blr_skip = 175;

    int blr_exec_sql = 176;
    int blr_internal_info = 177;
    int blr_nullsfirst = 178;
    int blr_writelock = 179;
    int blr_nullslast = 180;

    int blr_lowcase = 181;
    int blr_strlen = 182;

    int blr_strlen_bit = 0;
    int blr_strlen_char = 1;
    int blr_strlen_octet = 2;
    int blr_trim = 183;

    int blr_trim_both = 0;
    int blr_trim_leading = 1;
    int blr_trim_trailing = 2;

    int blr_trim_spaces = 0;
    int blr_trim_characters = 1;

    int blr_savepoint_set = 0;
    int blr_savepoint_release = 1;
    int blr_savepoint_undo = 2;
    int blr_savepoint_release_single = 3;

    int blr_cursor_open = 0;
    int blr_cursor_close = 1;
    int blr_cursor_fetch = 2;
    int blr_cursor_fetch_scroll = 3;

    int blr_scroll_forward = 0;
    int blr_scroll_backward = 1;
    int blr_scroll_bof = 2;
    int blr_scroll_eof = 3;
    int blr_scroll_absolute = 4;
    int blr_scroll_relative = 5;

    int blr_init_variable = 184;
    int blr_recurse = 185;
    int blr_sys_function = 186;

    int blr_auto_trans = 187;
    int blr_similar = 188;
    int blr_exec_stmt = 189;

    int blr_exec_stmt_inputs = 1;
    int blr_exec_stmt_outputs = 2;
    int blr_exec_stmt_sql = 3;
    int blr_exec_stmt_proc_block = 4;
    int blr_exec_stmt_data_src = 5;
    int blr_exec_stmt_user = 6;
    int blr_exec_stmt_pwd = 7;
    int blr_exec_stmt_tran = 8;
    int blr_exec_stmt_tran_clone = 9;
    int blr_exec_stmt_privs = 10;
    int blr_exec_stmt_in_params = 11;
    int blr_exec_stmt_in_params2 = 12;
    int blr_exec_stmt_out_params = 13;
    int blr_exec_stmt_role = 14;
    int blr_stmt_expr = 190;
    int blr_derived_expr = 191;

    int blr_procedure3 = 192;
    int blr_exec_proc2 = 193;
    int blr_function2 = 194;
    int blr_window = 195;
    int blr_partition_by = 196;
    int blr_continue_loop = 197;
    int blr_procedure4 = 198;
    int blr_agg_function = 199;
    int blr_substring_similar = 200;
    int blr_bool_as_value = 201;
    int blr_coalesce = 202;
    int blr_decode = 203;
    int blr_exec_subproc = 204;
    int blr_subproc_decl = 205;
    int blr_subproc = 206;
    int blr_subfunc_decl = 207;
    int blr_subfunc = 208;
    int blr_record_version2 = 209;
}
