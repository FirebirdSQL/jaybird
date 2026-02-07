// SPDX-FileCopyrightText: Copyright 2015-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.auth.legacy;

import org.firebirdsql.gds.ng.wire.auth.AuthenticationPlugin;
import org.firebirdsql.gds.ng.wire.auth.ClientAuthBlock;
import org.jspecify.annotations.Nullable;

import java.sql.SQLException;

/**
 * Authentication plugin for the Firebird legacy authentication (as defined in Firebird 3).
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
final class LegacyAuthenticationPlugin implements AuthenticationPlugin {

    private byte @Nullable [] clientData;
    private boolean hasServerData;

    @Override
    public String getName() {
        return LegacyAuthenticationPluginSpi.LEGACY_AUTH_NAME;
    }

    @Override
    public AuthStatus authenticate(ClientAuthBlock clientAuthBlock) {
        if (clientAuthBlock.getLogin() == null || clientAuthBlock.getPassword() == null) {
            return AuthStatus.AUTH_CONTINUE;
        }
        clientData = LegacyHash.fbCrypt(clientAuthBlock.getPassword());
        return AuthStatus.AUTH_SUCCESS;
    }

    @Override
    public byte @Nullable [] getClientData() {
        return clientData;
    }

    @Override
    public void setServerData(byte[] serverData) {
        //noinspection ConstantValue : null-check for robustness
        hasServerData = serverData != null && serverData.length > 0;
    }

    @Override
    public boolean hasServerData() {
        return hasServerData;
    }

    @Override
    public boolean generatesSessionKey() {
        return false;
    }

    @Override
    public byte[] getSessionKey() throws SQLException {
        throw new SQLException("LegacyAuthenticationPlugin cannot generate a session key");
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " : " + getName();
    }
}
