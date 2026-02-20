// SPDX-FileCopyrightText: Copyright 2015-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.auth.srp;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.wire.auth.AuthenticationPlugin;
import org.firebirdsql.gds.ng.wire.auth.ClientAuthBlock;
import org.jspecify.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

import static org.firebirdsql.jaybird.util.ByteArrayHelper.toHexString;

/**
 * Authentication plugin for authentication with Srp.
 * <p>
 * Supports multiple hash algorithms for the client proof.
 * </p>
 *
 * @author Mark Rotteveel
 */
class SrpAuthenticationPlugin implements AuthenticationPlugin {

    private final String pluginName;
    private final String clientProofHashAlgorithm;
    private byte @Nullable [] clientData;
    private @Nullable SrpClient srpClient;
    private byte @Nullable [] serverData;

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
        SrpClient srpClient = this.srpClient;
        if (srpClient == null) {
            if (clientAuthBlock.getLogin() == null || clientAuthBlock.getPassword() == null) {
                return AuthStatus.AUTH_CONTINUE;
            }
            srpClient = this.srpClient = new SrpClient(clientProofHashAlgorithm);
            clientData = srpClient.getPublicKeyHex().getBytes(StandardCharsets.ISO_8859_1);
            return AuthStatus.AUTH_MORE_DATA;
        } else if (hasSessionKey()) {
            // Same code and message as Firebird's implementation
            throw FbExceptionBuilder.forException(ISCConstants.isc_random)
                    .messageParameter("Auth sync failure - SRP's authenticate called more times than supported")
                    .toSQLException();
        }

        String login = clientAuthBlock.getNormalizedLogin();
        String password = clientAuthBlock.getPassword();
        if (login == null || password == null) {
            // This shouldn't happen in practice, as it's checked in the previous phase of authentication
            throw new SQLException("Auth sync failure - login or password not available");
        }

        clientData = toHexString(srpClient.clientProof(login, password, serverData))
                .getBytes(StandardCharsets.ISO_8859_1);
        return AuthStatus.AUTH_SUCCESS;
    }

    @Override
    public byte @Nullable [] getClientData() {
        return clientData;
    }

    @Override
    public void setServerData(byte @Nullable [] serverData) {
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

    private boolean hasSessionKey() {
        return srpClient != null && srpClient.getSessionKey() != null;
    }

    @Override
    public byte[] getSessionKey() throws SQLException {
        byte[] sessionKey = srpClient != null ? srpClient.getSessionKey() : null;
        if (sessionKey == null) {
            throw new SQLException("Auth sync failure - SRP plugin has produced no session key at this time");
        }
        return sessionKey;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " : " + getName();
    }

}
