/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
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
 * can be obtained from a source control history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.gds.ng.wire.version10;

import org.firebirdsql.gds.ng.WireCrypt;
import org.firebirdsql.gds.ng.WarningMessageCallback;
import org.firebirdsql.gds.ng.wire.*;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLWarning;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class V10WireOperations extends AbstractWireOperations {

    private static final Logger log = LoggerFactory.getLogger(V10WireOperations.class);

    public V10WireOperations(WireConnection<?, ?> connection,
            WarningMessageCallback defaultWarningMessageCallback, Object syncObject) {
        super(connection, defaultWarningMessageCallback, syncObject);
    }

    @Override
    public void enqueueDeferredAction(DeferredAction deferredAction) {
        throw new UnsupportedOperationException("enqueueDeferredAction is not supported in the V10 protocol");
    }

    @Override
    public void processDeferredActions() {
        // does nothing in V10 protocol
    }

    @Override
    public void authReceiveResponse(FbWireAttachment.AcceptPacket acceptPacket,
            ProcessAttachCallback processAttachCallback) throws IOException, SQLException {
        assert acceptPacket == null : "Should not be called with non-null acceptPacket in V12 or earlier";
        GenericResponse response = readGenericResponse(null);
        getClientAuthBlock().setAuthComplete(true);
        processAttachCallback.processAttachResponse(response);

        // fbclient also ignores REQUIRED when connecting to FB 2.5 or lower, apply same
        if (getAttachProperties().getWireCrypt() == WireCrypt.REQUIRED) {
            String message = "wireCrypt=REQUIRED, but wire protocol version does not support encryption, "
                    + "encryption requirement dropped";
            log.warn(message);
            getDefaultWarningMessageCallback().processWarning(new SQLWarning(message));
        }
    }

}
