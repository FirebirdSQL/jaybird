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
package org.firebirdsql.gds.impl.jni;

import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.impl.BaseGDSFactoryPlugin;

public class EmbeddedGDSFactoryPlugin extends BaseGDSFactoryPlugin {

    public static final String EMBEDDED_TYPE_NAME = "EMBEDDED";

    private static final String[] TYPE_ALIASES = new String[0];

    private static final String[] JDBC_PROTOCOLS = new String[] { 
        "jdbc:firebirdsql:embedded:"
    };

    public String getPluginName() {
        return "GDS implementation for embedded server.";
    }

    public String getTypeName() {
        return EMBEDDED_TYPE_NAME;
    }

    public String[] getTypeAliases() {
        return TYPE_ALIASES;
    }

    public String[] getSupportedProtocols() {
        return JDBC_PROTOCOLS;
    }
    
    public String getDatabasePath(String server, Integer port, String path)
            throws GDSException {
        return path;
    }
}
