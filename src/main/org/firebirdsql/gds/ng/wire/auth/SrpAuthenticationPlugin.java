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
import org.firebirdsql.util.ByteArrayHelper;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * Authentication plugin for authentication with Srp.
 * <p>
 * Supports multiple hash algorithms for the client proof.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
class SrpAuthenticationPlugin implements AuthenticationPlugin {

    private static final Logger log = LoggerFactory.getLogger(SrpAuthenticationPlugin.class);

    private final String pluginName;
    private final String clientProofHashAlgorithm;
    private byte[] clientData;
    private SrpClient srpClient;
    private byte[] serverData;

    /**
     * Initializes the SRP authentication plugin.
     *
     * @param pluginName
     *         Firebird name of the plugin
     * @param clientProofHashAlgorithm
     *         Hash algorithm name (as accepted by {@code MessageDigest.getInstance}) for
     *         creating the client proof.
     */
    SrpAuthenticationPlugin(String pluginName, String clientProofHashAlgorithm) {
        this.pluginName = pluginName;
        this.clientProofHashAlgorithm = clientProofHashAlgorithm;
    }

    @Override
    public String getName() {
        return pluginName;
    }

    @Override
    public AuthStatus authenticate(ClientAuthBlock clientAuthBlock) throws SQLException {
        if (srpClient == null) {
            log.debug("SRP phase 1, user: " + clientAuthBlock.getLogin());
            if (clientAuthBlock.getLogin() == null || clientAuthBlock.getPassword() == null) {
                return AuthStatus.AUTH_CONTINUE;
            }
            srpClient = new SrpClient(clientProofHashAlgorithm);
            clientData = srpClient.getPublicKeyHex().getBytes(StandardCharsets.US_ASCII);
            return AuthStatus.AUTH_MORE_DATA;
        } else if (srpClient.getSessionKey() != null) {
            throw new FbExceptionBuilder().exception(ISCConstants.isc_random)
                    .messageParameter("Auth sync failure - SRP's authenticate called more times than supported")
                    .toFlatSQLException();
        }

        log.debug("SRP phase 2");
        clientData = toHex(srpClient.clientProof(clientAuthBlock.getNormalizedLogin(), clientAuthBlock.getPassword(),
                serverData)).getBytes(StandardCharsets.US_ASCII);
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
    public boolean generatesSessionKey() {
        return true;
    }

    @Override
    public byte[] getSessionKey() throws SQLException {
        return srpClient.getSessionKey();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " : " + getName();
    }

    private static String toHex(byte[] bytes) {
        return ByteArrayHelper.toHexString(bytes);
    }
}
