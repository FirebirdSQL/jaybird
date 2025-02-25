// SPDX-FileCopyrightText: Copyright 2019-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.version15;

import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.impl.wire.XdrInputStream;
import org.firebirdsql.gds.impl.wire.XdrOutputStream;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.WarningMessageCallback;
import org.firebirdsql.gds.ng.dbcrypt.DbCryptData;
import org.firebirdsql.gds.ng.wire.WireConnection;
import org.firebirdsql.gds.ng.wire.version13.V13WireOperations;

import java.io.IOException;
import java.sql.SQLException;

import static org.firebirdsql.gds.impl.wire.WireProtocolConstants.op_crypt_key_callback;

/**
 * @author Mark Rotteveel
 * @since 4.0
 */
public class V15WireOperations extends V13WireOperations {

    public V15WireOperations(WireConnection<?, ?> connection, WarningMessageCallback defaultWarningMessageCallback) {
        super(connection, defaultWarningMessageCallback);
    }

    @Override
    protected DbCryptData readCryptKeyCallback() throws IOException, SQLException {
        final XdrInputStream xdrIn = getXdrIn();
        final byte[] pluginData = xdrIn.readBuffer(); // p_cc_data
        final int replySize = xdrIn.readInt(); // p_cc_reply
        try {
            return new DbCryptData(pluginData, replySize);
        } catch (RuntimeException e) {
            throw FbExceptionBuilder.forNonTransientConnectionException(JaybirdErrorCodes.jb_dbCryptDataError)
                    .cause(e)
                    .toSQLException();
        }
    }

    @Override
    protected void writeCryptKeyCallback(DbCryptData clientPluginResponse) throws SQLException, IOException {
        final XdrOutputStream xdrOut = getXdrOut();
        xdrOut.writeInt(op_crypt_key_callback);
        xdrOut.writeBuffer(clientPluginResponse.getPluginData()); // p_cc_data
        xdrOut.writeInt(clientPluginResponse.getReplySize()); // p_cc_reply
        xdrOut.flush();
    }
}
