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
package org.firebirdsql.jaybird.fb.constants;

/**
 * Constants for SPB (service parameter buffer) items.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 5
 */
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
