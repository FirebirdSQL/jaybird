// SPDX-FileCopyrightText: Copyright 2021-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
// SPDX-FileComment: The constants listed here were obtained from the Firebird sources, which are licensed under the IPL (InterBase Public License) and/or IDPL (Initial Developer Public License), both are variants of the Mozilla Public License version 1.1
package org.firebirdsql.jaybird.fb.constants;

/**
 * Constants for SPB (service parameter buffer) items.
 *
 * @author Mark Rotteveel
 * @since 5
 */
@SuppressWarnings({ "unused", "java:S115" })
public final class SpbItems {

    public static final int isc_spb_user_name = DpbItems.isc_dpb_user_name;
    public static final int isc_spb_sys_user_name = DpbItems.isc_dpb_sys_user_name;
    public static final int isc_spb_sys_user_name_enc = DpbItems.isc_dpb_sys_user_name_enc;
    public static final int isc_spb_password = DpbItems.isc_dpb_password;
    public static final int isc_spb_password_enc = DpbItems.isc_dpb_password_enc;
    public static final int isc_spb_connect_timeout = DpbItems.isc_dpb_connect_timeout;
    public static final int isc_spb_dummy_packet_interval = DpbItems.isc_dpb_dummy_packet_interval;
    public static final int isc_spb_sql_role_name = DpbItems.isc_dpb_sql_role_name;
    public static final int isc_spb_command_line = 105;
    public static final int isc_spb_dbname = 106;
    public static final int isc_spb_verbose = 107;
    public static final int isc_spb_options = 108;
    public static final int isc_spb_address_path = 109;
    public static final int isc_spb_process_id = 110;
    public static final int isc_spb_trusted_auth = 111;
    // This will not be used in protocol 13, therefore may be reused
    public static final int isc_spb_specific_auth_data = isc_spb_trusted_auth;
    public static final int isc_spb_process_name = 112;
    public static final int isc_spb_trusted_role = 113;
    public static final int isc_spb_verbint = 114;
    public static final int isc_spb_auth_block = 115;
    public static final int isc_spb_auth_plugin_name = 116;
    public static final int isc_spb_auth_plugin_list = 117;
    public static final int isc_spb_utf8_filename = 118;
    public static final int isc_spb_client_version = 119;
    public static final int isc_spb_remote_protocol = 120;
    public static final int isc_spb_host_name = 121;
    public static final int isc_spb_os_user = 122;
    public static final int isc_spb_config = 123;
    public static final int isc_spb_expected_db = 124;

    private SpbItems() {
        // no instances
    }
}
