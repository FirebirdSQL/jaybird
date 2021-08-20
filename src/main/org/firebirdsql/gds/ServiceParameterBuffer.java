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

import org.firebirdsql.jaybird.fb.constants.SpbItems;

/**
 * Instance of this interface represents a Service Parameter Buffer from the
 * Firebird API documentation and specifies the attributes for the Services API
 * connection.
 */
public interface ServiceParameterBuffer extends ConnectionParameterBuffer {

    //@formatter:off
    @Deprecated
    int VERSION                 = ISCConstants.isc_spb_version;
    @Deprecated
    int VERSION1                = ISCConstants.isc_spb_version1;
    @Deprecated
    int CURRENT_VERSION         = ISCConstants.isc_spb_current_version;

    @Deprecated
    int USER_NAME               = SpbItems.isc_spb_user_name;
    @Deprecated
    int SYS_USER_NAME           = SpbItems.isc_spb_sys_user_name;
    @Deprecated
    int SYS_USER_NAME_ENC       = SpbItems.isc_spb_sys_user_name_enc;
    @Deprecated
    int PASSWORD                = SpbItems.isc_spb_password;
    @Deprecated
    int PASSWORD_ENC            = SpbItems.isc_spb_password_enc;

    @Deprecated
    int COMMAND_LINE            = SpbItems.isc_spb_command_line;
    @Deprecated
    int DBNAME                  = SpbItems.isc_spb_dbname;
    @Deprecated
    int VERBOSE                 = SpbItems.isc_spb_verbose;
    @Deprecated
    int OPTIONS                 = SpbItems.isc_spb_options;

    @Deprecated
    int CONNECT_TIMEOUT         = SpbItems.isc_spb_connect_timeout;
    @Deprecated
    int DUMMY_PACKET_INTERVAL   = SpbItems.isc_spb_dummy_packet_interval;
    @Deprecated
    int SQL_ROLE_NAME           = SpbItems.isc_spb_sql_role_name;
    //@formatter:on
}
