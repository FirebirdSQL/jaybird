// SPDX-FileCopyrightText: Copyright 2015-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
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
     * Struct-like class, reduced equivalent of Firebird p_acpd to store data for handling op_cond_accept.
     */
    @SuppressWarnings({ "java:S116", "java:S1104" })
    class AcceptPacket {
        public int operation;
        public byte[] p_acpt_data;
        public String p_acpt_plugin;
        public byte[] p_acpt_keys;
    }
}
