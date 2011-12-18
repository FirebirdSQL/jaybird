/*
 * $Id$
 * 
 * Firebird Open Source J2EE Connector - JDBC Driver
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
package org.firebirdsql.gds;

import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

/**
 * Returns classes either by the <code>Class.forName</code> method or by using
 * an already cached instance.
 * 
 * @author <a href="mailto:sjardine@users.sourceforge.net">Steven Jardine </a>
 */
public class ClassFactory {

    // Many commonly used class names.
    public static final String DriverConnectionPoolDataSource = "org.firebirdsql.pool.DriverConnectionPoolDataSource";

    public static final String FBCallableStatement = "org.firebirdsql.jdbc.FBCallableStatement";

    public static final String FBConnection = "org.firebirdsql.jdbc.FBConnection";

    public static final String FBConnectionPoolDataSource = "org.firebirdsql.pool.FBConnectionPoolDataSource";

    public static final String FBPreparedStatement = "org.firebirdsql.jdbc.FBPreparedStatement";

    public static final String FBStatement = "org.firebirdsql.jdbc.FBStatement";

    public static final String JavaGDSImpl = "org.firebirdsql.gds.impl.wire.JavaGDSImpl";

    private static Logger log = LoggerFactory.getLogger(ClassFactory.class, false);

    /**
     * @param className the class name to return.
     * @return the class corresponding to the string class name.
     */
    public static Class get(String className) {

        try {

            return Class.forName(className);

        } catch (ClassNotFoundException e) {

            log.error(e.getMessage(), e);

        }

        return null;

    }
}
