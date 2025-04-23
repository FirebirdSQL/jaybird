// SPDX-FileCopyrightText: Copyright 2015-2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.version10;

import org.firebirdsql.gds.ng.WireCrypt;
import org.firebirdsql.gds.ng.WarningMessageCallback;
import org.firebirdsql.gds.ng.dbcrypt.DbCryptCallback;
import org.firebirdsql.gds.ng.wire.*;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLWarning;

/**
 * @author Mark Rotteveel
 * @since 3.0
 */
public class V10WireOperations extends AbstractWireOperations {

    private static final System.Logger log = System.getLogger(V10WireOperations.class.getName());

    public V10WireOperations(WireConnection<?, ?> connection, WarningMessageCallback defaultWarningMessageCallback) {
        super(connection, defaultWarningMessageCallback);
    }

    @Override
    @SuppressWarnings("java:S4274")
    public void authReceiveResponse(FbWireAttachment.AcceptPacket acceptPacket, DbCryptCallback dbCryptCallback)
            throws IOException, SQLException {
        assert acceptPacket == null : "Should not be called with non-null acceptPacket in V12 or earlier";
        readGenericResponse(null);
        getClientAuthBlock().setAuthComplete(true);

        // fbclient also ignores REQUIRED when connecting to FB 2.5 or lower, apply same
        if (getAttachProperties().getWireCryptAsEnum() == WireCrypt.REQUIRED) {
            String message = "wireCrypt=REQUIRED, but wire protocol version does not support encryption, "
                    + "encryption requirement dropped";
            log.log(System.Logger.Level.WARNING, message);
            getDefaultWarningMessageCallback().processWarning(new SQLWarning(message));
        }
    }

}
