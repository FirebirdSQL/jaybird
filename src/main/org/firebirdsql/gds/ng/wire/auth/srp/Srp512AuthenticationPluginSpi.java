// SPDX-FileCopyrightText: Copyright 2018 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.auth.srp;

import java.sql.SQLException;

/**
 * Srp512 authentication plugin service provider.
 *
 * @author Mark Rotteveel
 * @since 4.0
 */
public class Srp512AuthenticationPluginSpi extends AbstractSrpAuthenticationPluginSpi {

    public static final String SRP_512_AUTH_NAME = "Srp512";

    public Srp512AuthenticationPluginSpi() throws SQLException {
        super(SRP_512_AUTH_NAME, "SHA-512");
    }

}
