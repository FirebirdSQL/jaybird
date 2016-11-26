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
package org.firebirdsql.gds.ng.wire.auth;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * Authentication plugin for authentication with SRP.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class SrpAuthenticationPlugin implements AuthenticationPlugin {

    private static final Logger log = LoggerFactory.getLogger(SrpAuthenticationPlugin.class);

    public static final String SRP_AUTH_NAME = "Srp";

    private byte[] clientData;
    private SrpClient srpClient;
    private byte[] serverData;

    @Override
    public String getName() {
        return SRP_AUTH_NAME;
    }

    @Override
    public AuthStatus authenticate(ClientAuthBlock clientAuthBlock) throws SQLException {
        if (srpClient == null) {
            log.debug("SRP phase 1, user: " + clientAuthBlock.getLogin());
            if (clientAuthBlock.getLogin() == null || clientAuthBlock.getPassword() == null) {
                return AuthStatus.AUTH_CONTINUE;
            }
            srpClient = new SrpClient();
            clientData = srpClient.getPublicKeyHex().getBytes(StandardCharsets.US_ASCII);
            return AuthStatus.AUTH_MORE_DATA;
        } else if (srpClient.getSessionKey() != null) {
            throw new FbExceptionBuilder().exception(ISCConstants.isc_random)
                    .messageParameter("Auth sync failure - SRP's authenticate called more times than supported")
                    .toFlatSQLException();
        }

        log.debug("SRP phase 2");
        clientData = toHex(srpClient.clientProof(clientAuthBlock.getLogin(), clientAuthBlock.getPassword(), serverData))
                .getBytes(StandardCharsets.US_ASCII);
        // TODO store key as in the Firebird sources?
        return AuthStatus.AUTH_SUCCESS;
    }

    @Override
    public byte[] getClientData() {
        return clientData;
    }

    @Override
    public void setServerData(byte[] serverData) {
        this.serverData = serverData;
    }

    @Override
    public boolean hasServerData() {
        return serverData != null && serverData.length > 0;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " : " + getName();
    }

    private static String toHex(byte[] bytes) {
        return DatatypeConverter.printHexBinary(bytes);
    }
}
