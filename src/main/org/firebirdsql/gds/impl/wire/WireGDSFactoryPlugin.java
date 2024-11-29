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

import org.firebirdsql.gds.impl.BaseGDSFactoryPlugin;
import org.firebirdsql.gds.ng.wire.FbWireDatabaseFactory;

import java.util.List;

public final class WireGDSFactoryPlugin extends BaseGDSFactoryPlugin {

    public static final String PURE_JAVA_TYPE_NAME = "PURE_JAVA";
    private static final List<String> TYPE_ALIASES = List.of("TYPE4");
    private static final String DEFAULT_PROTOCOL = "jdbc:firebirdsql:";
    private static final List<String> JDBC_PROTOCOLS = List.of(
            "jdbc:firebirdsql:java:", "jdbc:firebird:java:", "jdbc:firebird:", DEFAULT_PROTOCOL);

    @Override
    public String getPluginName() {
        return "Pure Java GDS implementation";
    }

    @Override
    public String getTypeName() {
        return PURE_JAVA_TYPE_NAME;
    }

    @SuppressWarnings("removal")
    @Deprecated(since = "6", forRemoval = true)
    @Override
    public String[] getTypeAliases() {
        return TYPE_ALIASES.toArray(new String[0]);
    }

    @Override
    public List<String> getTypeAliasList() {
        return TYPE_ALIASES;
    }

    @SuppressWarnings("removal")
    @Deprecated(since = "6", forRemoval = true)
    @Override
    public String[] getSupportedProtocols() {
        return JDBC_PROTOCOLS.toArray(new String[0]);
    }

    @Override
    public List<String> getSupportedProtocolList() {
        return JDBC_PROTOCOLS;
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
