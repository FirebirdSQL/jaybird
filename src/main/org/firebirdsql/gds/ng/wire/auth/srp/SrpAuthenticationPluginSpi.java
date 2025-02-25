// SPDX-FileCopyrightText: Copyright 2015-2018 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.auth.srp;

import java.sql.SQLException;

/**
 * Srp (Srp using SHA-1) authentication plugin service provider.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public class SrpAuthenticationPluginSpi extends AbstractSrpAuthenticationPluginSpi {

    public static final String SRP_AUTH_NAME = "Srp";

    public SrpAuthenticationPluginSpi() throws SQLException {
        super(SRP_AUTH_NAME, "SHA-1");
    }

}
