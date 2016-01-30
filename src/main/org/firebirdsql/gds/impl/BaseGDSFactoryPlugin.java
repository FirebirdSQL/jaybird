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
package org.firebirdsql.gds.impl;

import org.firebirdsql.gds.GDSException;
import org.firebirdsql.jdbc.FBConnection;

/**
 * Base class for {@link GDSFactoryPlugin} implementations.
 * <p>
 * Handles commonalities across existing implementations.
 * </p>
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public abstract class BaseGDSFactoryPlugin implements GDSFactoryPlugin {
    
    public Class<?> getConnectionClass() {
        return FBConnection.class;
    }

    public String getDefaultProtocol() {
        return getSupportedProtocols()[0];
    }

    public String getDatabasePath(String jdbcUrl) throws GDSException {
        String[] protocols = getSupportedProtocols();
        for (String protocol : protocols) {
            if (jdbcUrl.startsWith(protocol))
                return jdbcUrl.substring(protocol.length());
        }

        throw new IllegalArgumentException("Incorrect JDBC protocol handling: " + jdbcUrl);
    }

    public int hashCode() {
        return getTypeName().hashCode();
    }
    
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this) 
            return true;
        
        return getClass().equals(obj.getClass());
    }
}
