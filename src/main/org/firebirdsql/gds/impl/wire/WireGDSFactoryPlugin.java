// SPDX-FileCopyrightText: Copyright 2005 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2005 Steven Jardine
// SPDX-FileCopyrightText: Copyright 2012-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.impl.wire;

import org.firebirdsql.gds.impl.BaseGDSFactoryPlugin;
import org.firebirdsql.gds.ng.wire.FbWireDatabaseFactory;
import org.jspecify.annotations.NullMarked;

import java.util.List;

@NullMarked
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
    public FbWireDatabaseFactory getDatabaseFactory() {
        return FbWireDatabaseFactory.getInstance();
    }
}
