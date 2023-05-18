/*
 * Firebird Open Source JDBC Driver
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
package org.firebirdsql.gds.ng.wire.auth.legacy;

import org.firebirdsql.gds.ng.wire.auth.AuthenticationPlugin;
import org.firebirdsql.gds.ng.wire.auth.ClientAuthBlock;

import java.sql.SQLException;

/**
 * Authentication plugin for the Firebird legacy authentication (as defined in Firebird 3).
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
final class LegacyAuthenticationPlugin implements AuthenticationPlugin {

    private byte[] clientData;
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
    public byte[] getClientData() {
        return clientData;
    }

    @Override
    public void setServerData(byte[] serverData) {
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
