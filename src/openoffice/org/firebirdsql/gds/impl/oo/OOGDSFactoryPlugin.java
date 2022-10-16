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
package org.firebirdsql.gds.impl.oo;

import org.firebirdsql.gds.impl.BaseGDSFactoryPlugin;
import org.firebirdsql.gds.ng.wire.FbWireDatabaseFactory;
import org.firebirdsql.jdbc.oo.OOConnection;

import java.util.Arrays;

public class OOGDSFactoryPlugin extends BaseGDSFactoryPlugin {

    public static final String TYPE_NAME = "OOREMOTE";
    private static final String[] TYPE_ALIASES = new String[] {};
    private static final String DEFAULT_PROTOCOL = "jdbc:firebirdsql:oo:";
    private static final String[] JDBC_PROTOCOLS = new String[] { "jdbc:firebird:oo:", DEFAULT_PROTOCOL };

    @Override
    public String getPluginName() {
        return "GDS implementation for OpenOffice.org/LibreOffice.";
    }

    @Override
    public String getTypeName() {
        return TYPE_NAME;
    }

    @Override
    public String[] getTypeAliases() {
        return TYPE_ALIASES;
    }

    @Override
    public Class<?> getConnectionClass() {
        return OOConnection.class;
    }

    @Override
    public String[] getSupportedProtocols() {
        return Arrays.copyOf(JDBC_PROTOCOLS, JDBC_PROTOCOLS.length);
    }

    @Override
    public String getDefaultProtocol() {
        return DEFAULT_PROTOCOL;
    }

    @Override
    public FbWireDatabaseFactory getDatabaseFactory() {
        return FbWireDatabaseFactory.getInstance();
    }
}
