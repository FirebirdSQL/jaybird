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
package org.firebirdsql.gds.ng.wire.auth.legacy;

import org.firebirdsql.gds.ng.wire.auth.AuthenticationPlugin;
import org.firebirdsql.gds.ng.wire.auth.AuthenticationPluginSpi;

/**
 * Legacy authentication plugin service provider.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class LegacyAuthenticationPluginSpi implements AuthenticationPluginSpi {

    public static final String LEGACY_AUTH_NAME = "Legacy_Auth";

    @Override
    public String getPluginName() {
        return LEGACY_AUTH_NAME;
    }

    @Override
    public AuthenticationPlugin createPlugin() {
        return new LegacyAuthenticationPlugin();
    }
}
