// SPDX-FileCopyrightText: Copyright 2005 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2005 Steven Jardine
// SPDX-FileCopyrightText: Copyright 2012-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.impl.jni;

import org.firebirdsql.gds.impl.BaseGDSFactoryPlugin;
import org.firebirdsql.gds.ng.jna.FbClientDatabaseFactory;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;

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
        return "JNA-based GDS implementation";
    }

    @Override
    public String getTypeName() {
        return NATIVE_TYPE_NAME;
    }

    @Override
    public List<String> getTypeAliasList() {
        return TYPE_ALIASES;
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
    @NullUnmarked
    public String getDatabasePath(@Nullable String server, @Nullable Integer port, String path)
            throws SQLException {
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
