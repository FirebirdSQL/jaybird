// SPDX-FileCopyrightText: Copyright 2005 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2005 Steven Jardine
// SPDX-FileCopyrightText: Copyright 2012-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.impl.jni;

import org.firebirdsql.gds.impl.BaseGDSFactoryPlugin;
import org.firebirdsql.gds.ng.jna.FbEmbeddedDatabaseFactory;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;

import java.sql.SQLException;
import java.util.List;

public final class EmbeddedGDSFactoryPlugin extends BaseGDSFactoryPlugin {

    public static final String EMBEDDED_TYPE_NAME = "EMBEDDED";
    private static final String DEFAULT_PROTOCOL = "jdbc:firebirdsql:embedded:";
    private static final List<String> JDBC_PROTOCOLS = List.of(DEFAULT_PROTOCOL, "jdbc:firebird:embedded:");

    @Override
    public String getPluginName() {
        return "GDS implementation for embedded server";
    }

    @Override
    public String getTypeName() {
        return EMBEDDED_TYPE_NAME;
    }

    @Override
    public List<String> getTypeAliasList() {
        return List.of();
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
        return path;
    }

    @Override
    public FbEmbeddedDatabaseFactory getDatabaseFactory() {
        return FbEmbeddedDatabaseFactory.getInstance();
    }
}
