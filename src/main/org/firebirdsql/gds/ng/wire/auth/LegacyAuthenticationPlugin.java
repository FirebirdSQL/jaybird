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

import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * Authentication plugin for the Firebird legacy authentication (as defined in Firebird 3).
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
class LegacyAuthenticationPlugin implements AuthenticationPlugin {

    private static final Logger log = LoggerFactory.getLogger(LegacyAuthenticationPlugin.class);

    private static final String LEGACY_PASSWORD_SALT = "9z";

    private byte[] clientData;
    private boolean hasServerData;

    @Override
    public String getName() {
        return LegacyAuthenticationPluginSpi.LEGACY_AUTH_NAME;
    }

    @Override
    public AuthStatus authenticate(ClientAuthBlock clientAuthBlock) throws SQLException {
        if (clientAuthBlock.getLogin() == null || clientAuthBlock.getPassword() == null) {
            return AuthStatus.AUTH_CONTINUE;
        }
        clientData = UnixCrypt.crypt(clientAuthBlock.getPassword(), LEGACY_PASSWORD_SALT).substring(2, 13)
                .getBytes(StandardCharsets.US_ASCII);
        return AuthStatus.AUTH_SUCCESS;
    }

    @Override
    public byte[] getClientData() {
        return clientData;
    }

    @Override
    public void setServerData(byte[] serverData) {
        log.debug("Ignoring server data, plugin doesn't use it");
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
