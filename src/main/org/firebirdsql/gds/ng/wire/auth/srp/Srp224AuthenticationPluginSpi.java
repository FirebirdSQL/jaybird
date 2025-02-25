// SPDX-FileCopyrightText: Copyright 2018 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.auth.srp;

import java.sql.SQLException;

/**
 * Srp224 authentication plugin service provider.
 *
 * @author Mark Rotteveel
 * @since 4.0
 */
public class Srp224AuthenticationPluginSpi extends AbstractSrpAuthenticationPluginSpi {

    public static final String SRP_224_AUTH_NAME = "Srp224";

    public Srp224AuthenticationPluginSpi() throws SQLException {
        super(SRP_224_AUTH_NAME, "SHA-224");
    }

}
