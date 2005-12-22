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
package org.firebirdsql.pool;

import org.firebirdsql.gds.ClassFactory;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

/**
 * Creates Pooled DataSource objects.
 * 
 * @author <a href="mailto:sjardine@users.sourceforge.net">Steven Jardine </a>
 */
public class FBPooledDataSourceFactory {

    private static Logger log = LoggerFactory.getLogger(FBPooledDataSourceFactory.class, false);

    /**
     * @return a new DriverConnectionPoolDatasource object.
     */
    public static AbstractDriverConnectionPoolDataSource createDriverConnectionPoolDataSource() {
        try {
            return (AbstractDriverConnectionPoolDataSource) ClassFactory.get(
                    ClassFactory.DriverConnectionPoolDataSource).newInstance();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * @return a new FBConnectionPoolDataSource object.
     */
    public static AbstractFBConnectionPoolDataSource createFBConnectionPoolDataSource() {
        try {
            return (AbstractFBConnectionPoolDataSource) ClassFactory.get(
                    ClassFactory.FBConnectionPoolDataSource).newInstance();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

}
