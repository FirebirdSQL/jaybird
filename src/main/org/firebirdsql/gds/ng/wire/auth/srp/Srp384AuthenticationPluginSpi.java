// SPDX-FileCopyrightText: Copyright 2018 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.auth.srp;

import java.sql.SQLException;

/**
 * Srp384 authentication plugin service provider.
 *
 * @author Mark Rotteveel
 * @since 4.0
 */
public class Srp384AuthenticationPluginSpi extends AbstractSrpAuthenticationPluginSpi {

    public static final String SRP_384_AUTH_NAME = "Srp384";

    public Srp384AuthenticationPluginSpi() throws SQLException {
        super(SRP_384_AUTH_NAME, "SHA-384");
    }

}
