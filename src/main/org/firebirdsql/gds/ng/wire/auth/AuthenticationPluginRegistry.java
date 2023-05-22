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
package org.firebirdsql.gds.ng.wire.auth;

import org.firebirdsql.jaybird.util.PluginLoader;
import org.firebirdsql.util.InternalApi;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry of authentication plugins.
 *
 * @author Mark Rotteveel
 * @since 6
 */
@InternalApi
public final class AuthenticationPluginRegistry {

    private static final Map<String, AuthenticationPluginSpi> PLUGIN_SPI_MAP =
            Map.copyOf(findAuthenticationPluginSpi());

    public static AuthenticationPluginSpi getAuthenticationPluginSpi(String authenticationPluginName) {
        return PLUGIN_SPI_MAP.get(authenticationPluginName);
    }

    private static Map<String, AuthenticationPluginSpi> findAuthenticationPluginSpi() {
        Collection<AuthenticationPluginSpi> pluginSpis = PluginLoader.findPlugins(AuthenticationPluginSpi.class,
                List.of("org.firebirdsql.gds.ng.wire.auth.legacy.LegacyAuthenticationPluginSpi",
                        "org.firebirdsql.gds.ng.wire.auth.srp.SrpAuthenticationPluginSpi",
                        "org.firebirdsql.gds.ng.wire.auth.srp.Srp224AuthenticationPluginSpi",
                        "org.firebirdsql.gds.ng.wire.auth.srp.Srp256AuthenticationPluginSpi",
                        "org.firebirdsql.gds.ng.wire.auth.srp.Srp384AuthenticationPluginSpi",
                        "org.firebirdsql.gds.ng.wire.auth.srp.Srp512AuthenticationPluginSpi"));
        var pluginSpiMap = new HashMap<String, AuthenticationPluginSpi>();
        for (AuthenticationPluginSpi pluginSpi : pluginSpis) {
            if (pluginSpiMap.putIfAbsent(pluginSpi.getPluginName(), pluginSpi) != null) {
                System.getLogger(AuthenticationPluginRegistry.class.getName()).log(System.Logger.Level.WARNING,
                        "Authentication plugin provider for {0} already registered. Skipping {1}",
                        pluginSpi.getPluginName(), pluginSpi.getClass().getName());
            }
        }
        return pluginSpiMap;
    }

}
