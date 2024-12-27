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
import org.firebirdsql.gds.ng.jna.FbEmbeddedDatabaseFactory;

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
    public String getDatabasePath(String server, Integer port, String path) throws SQLException {
        requirePath(path);
        return path;
    }

    @Override
    public FbEmbeddedDatabaseFactory getDatabaseFactory() {
        return FbEmbeddedDatabaseFactory.getInstance();
    }
}
