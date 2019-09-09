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
package org.firebirdsql.gds.impl.wire;

import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.impl.BaseGDSFactoryPlugin;
import org.firebirdsql.gds.ng.wire.FbWireDatabaseFactory;

import java.util.Arrays;

public class WireGDSFactoryPlugin extends BaseGDSFactoryPlugin {

    public static final String PURE_JAVA_TYPE_NAME = "PURE_JAVA";
    private static final String[] TYPE_ALIASES = new String[] { "TYPE4" };
    private static final String DEFAULT_PROTOCOL = "jdbc:firebirdsql:";
    private static final String[] JDBC_PROTOCOLS = new String[] {
            "jdbc:firebirdsql:java:", "jdbc:firebird:java:", "jdbc:firebird:", DEFAULT_PROTOCOL
    };

    @Override
    public String getPluginName() {
        return "Pure Java GDS implementation.";
    }

    @Override
    public String getTypeName() {
        return PURE_JAVA_TYPE_NAME;
    }

    @Override
    public String[] getTypeAliases() {
        return Arrays.copyOf(TYPE_ALIASES, TYPE_ALIASES.length);
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
    public String getDatabasePath(String server, Integer port, String path) throws GDSException {
        if (server == null) {
            throw new GDSException("Server name/address is required for pure Java implementation.");
        }

        if (path == null) {
            throw new GDSException("Database name/path is required.");
        }

        StringBuilder sb = new StringBuilder();

        sb.append("//");
        sb.append(server);
        if (port != null) {
            sb.append(':').append(port.intValue());
        }

        sb.append('/').append(path);

        return sb.toString();
    }

    @Override
    public FbWireDatabaseFactory getDatabaseFactory() {
        return FbWireDatabaseFactory.getInstance();
    }
}
