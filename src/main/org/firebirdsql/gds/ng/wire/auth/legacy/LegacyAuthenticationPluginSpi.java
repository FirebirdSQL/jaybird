// SPDX-FileCopyrightText: Copyright 2015-2018 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.auth.legacy;

import org.firebirdsql.gds.ng.wire.auth.AuthenticationPlugin;
import org.firebirdsql.gds.ng.wire.auth.AuthenticationPluginSpi;

/**
 * Legacy authentication plugin service provider.
 *
 * @author Mark Rotteveel
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
