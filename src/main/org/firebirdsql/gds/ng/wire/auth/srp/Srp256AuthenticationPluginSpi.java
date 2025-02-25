// SPDX-FileCopyrightText: Copyright 2018 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.auth.srp;

import java.sql.SQLException;

/**
 * Srp256 authentication plugin service provider.
 *
 * @author Mark Rotteveel
 * @since 3.0.5
 */
public class Srp256AuthenticationPluginSpi extends AbstractSrpAuthenticationPluginSpi {

    public static final String SRP_256_AUTH_NAME = "Srp256";

    public Srp256AuthenticationPluginSpi() throws SQLException {
        super(SRP_256_AUTH_NAME, "SHA-256");
    }

}
