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
package org.firebirdsql.gds.impl.jni;

import org.firebirdsql.gds.impl.BaseGDSFactoryPlugin;
import org.firebirdsql.gds.ng.jna.FbClientDatabaseFactory;

import java.sql.SQLException;
import java.util.List;

public final class NativeGDSFactoryPlugin extends BaseGDSFactoryPlugin {

    public static final String NATIVE_TYPE_NAME = "NATIVE";
    // NOTE Alias LOCAL is deprecated, as are the *:local: JDBC protocols. They may be removed in Jaybird 6 or later
    private static final List<String> TYPE_ALIASES = List.of("TYPE2", "LOCAL");
    private static final String DEFAULT_PROTOCOL = "jdbc:firebirdsql:native:";
    private static final List<String> JDBC_PROTOCOLS = List.of(
            DEFAULT_PROTOCOL, "jdbc:firebird:native:",
            // For backwards compatibility
            "jdbc:firebirdsql:local:", "jdbc:firebird:local:");

    @Override
    public String getPluginName() {
        return "JNA-based GDS implementation.";
    }

    @Override
    public String getTypeName() {
        return NATIVE_TYPE_NAME;
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
    public String getDatabasePath(String server, Integer port, String path) throws SQLException {
        requirePath(path);
        if (server == null) {
            return path;
        }
        
        var sb = new StringBuilder();
        sb.append(server);
        if (port != null) {
            sb.append('/').append(port.intValue());
        }
        sb.append(':').append(path);
        
        return sb.toString();
    }

    @Override
    public FbClientDatabaseFactory getDatabaseFactory() {
        return FbClientDatabaseFactory.getInstance();
    }
}
