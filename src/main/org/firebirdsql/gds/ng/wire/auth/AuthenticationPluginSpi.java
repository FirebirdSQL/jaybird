// SPDX-FileCopyrightText: Copyright 2015 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds.ng.wire.auth;

/**
 * Service provider interface for authentication plugins.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public interface AuthenticationPluginSpi {

    /**
     * @return Name of the plugin as used by Firebird
     */
    String getPluginName();

    /**
     * @return Plugin instance
     */
    AuthenticationPlugin createPlugin();
}
