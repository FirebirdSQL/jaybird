/*
 * $Id$
 * 
 * Firebird Open Source J2ee connector - jdbc driver
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
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.gds.impl.oo;

import org.firebirdsql.gds.*;
import org.firebirdsql.gds.impl.BaseGDSFactoryPlugin;
import org.firebirdsql.gds.impl.wire.JavaGDSImpl;
import org.firebirdsql.jdbc.oo.OOConnection;

public class OOGDSFactoryPlugin extends BaseGDSFactoryPlugin {

    private static final String TYPE_NAME = "OOREMOTE";

    private static final String[] TYPE_ALIASES = new String[] {};

    private static final String[] JDBC_PROTOCOLS = new String[] {
            "jdbc:firebird:oo:", "jdbc:firebirdsql:oo:"};

    public String getPluginName() {
        return "GDS implementation for OpenOffice.";
    }

    public String getTypeName() {
        return TYPE_NAME;
    }

    public String[] getTypeAliases() {
        return TYPE_ALIASES;
    }

    @Override
    public Class<?> getConnectionClass() {
        return OOConnection.class;
    }

    public String[] getSupportedProtocols() {
        return JDBC_PROTOCOLS;
    }

    public String getDatabasePath(String server, Integer port, String path)
            throws GDSException {
        if (server == null)
            throw new GDSException("Server name/address is required "
                    + "for pure Java implementation.");

        if (path == null)
            throw new GDSException("Database name/path is required.");

        StringBuffer sb = new StringBuffer();

        sb.append(server);
        if (port != null) sb.append("/").append(port.intValue());

        sb.append(":").append(path);

        return sb.toString();
    }

    /**
     * Initialization-on-demand depending on classloading behavior specified in JLS 12.4
     */
    private static final class GDSHolder {
        private static final GDS gds = new JavaGDSImpl();
    }

    public GDS getGDS() {
        return GDSHolder.gds;
    }
}
