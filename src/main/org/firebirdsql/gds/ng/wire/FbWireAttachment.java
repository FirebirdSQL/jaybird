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
package org.firebirdsql.gds.ng.wire;

import org.firebirdsql.gds.ng.FbAttachment;
import org.firebirdsql.gds.ng.WarningMessageCallback;

import java.io.IOException;
import java.sql.SQLException;

/**
 * @author Mark Rotteveel
 * @since 3.0
 */
public interface FbWireAttachment extends FbAttachment {

    /**
     * @return Instance of {@link XdrStreamAccess} for this attachment.
     */
    XdrStreamAccess getXdrStreamAccess();

    /**
     * @return Instance of {@link FbWireOperations} for this attachment.
     */
    FbWireOperations getWireOperations();

    /**
     * Convenience method to read a Response to a GenericResponse
     *
     * @param callback
     *         Callback object for warnings, {@code null} for default callback
     * @return GenericResponse
     * @throws SQLException
     *         For errors returned from the server, or when attempting to
     *         read.
     * @throws IOException
     *         For errors reading the response from the connection.
     */
    GenericResponse readGenericResponse(WarningMessageCallback callback) throws SQLException, IOException;

    /**
     * Receive authentication response from the server.
     * <p>
     * This method is only relevant for protocol V13 or higher.
     * </p>
     *
     * @param acceptPacket
     *         Packet with {@code op_cond_accept} data, or {@code null} when the data should be read from the
     *         connection.
     * @throws IOException
     *         For errors reading the response from the connection.
     * @throws SQLException
     *         For errors returned from the server, or when attempting to
     *         read.
     */
    void authReceiveResponse(AcceptPacket acceptPacket) throws IOException, SQLException;

    /**
     * Struct-like class, reduced equivalent of Firebird p_acpd so we can store data for handling op_cond_accept.
     */
    class AcceptPacket {
        public int operation;
        public byte[] p_acpt_data;
        public String p_acpt_plugin;
        public byte[] p_acpt_keys;
    }
}
