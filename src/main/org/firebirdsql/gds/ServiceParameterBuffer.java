/*
 * $Id$
 *
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
 * Instance of this interface represents a Service Parameter Buffer from the
 * Firebird API documentation and specifies the attributes for the  Services API
 * connection.
 */
public interface ServiceParameterBuffer extends ConnectionParameterBuffer {

    //@formatter:off
    int VERSION                 = ISCConstants.isc_spb_version;
    int VERSION1                = ISCConstants.isc_spb_version1;
    int CURRENT_VERSION         = ISCConstants.isc_spb_current_version;
    
    int USER_NAME               = ISCConstants.isc_spb_user_name;
    int SYS_USER_NAME           = ISCConstants.isc_spb_sys_user_name;
    int SYS_USER_NAME_ENC       = ISCConstants.isc_spb_sys_user_name_enc;
    int PASSWORD                = ISCConstants.isc_spb_password;
    int PASSWORD_ENC            = ISCConstants.isc_spb_password_enc;
    
    int COMMAND_LINE            = ISCConstants.isc_spb_command_line;
    int DBNAME                  = ISCConstants.isc_spb_dbname;
    int VERBOSE                 = ISCConstants.isc_spb_verbose;
    int OPTIONS                 = ISCConstants.isc_spb_options;
    
    int CONNECT_TIMEOUT         = ISCConstants.isc_spb_connect_timeout;
    int DUMMY_PACKET_INTERVAL   = ISCConstants.isc_spb_dummy_packet_interval;
    int SQL_ROLE_NAME           = ISCConstants.isc_spb_sql_role_name;
    //@formatter:on
}
