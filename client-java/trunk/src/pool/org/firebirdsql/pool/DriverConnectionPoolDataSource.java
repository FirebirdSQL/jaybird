/*
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
package org.firebirdsql.pool;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;

import org.firebirdsql.pool.AbstractDriverConnectionPoolDataSource;

/**
 * Implementation of the javax.naming.spi.ObjectFactory interface.
 * <p>
 * <b>NOTE</b>: This implementation contains a bug in the handling of PooledConnections.
 * We strongly advise you to use the replacement implementation {@link org.firebirdsql.ds.FBConnectionPoolDataSource}.
 * </p>
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:sjardine@users.sourceforge.net">Steve Jardine</a>
 * @deprecated Use {@link org.firebirdsql.ds.FBConnectionPoolDataSource}
 */
public class DriverConnectionPoolDataSource extends
        AbstractDriverConnectionPoolDataSource implements ObjectFactory {

    /*
     * (non-Javadoc)
     * 
     * @see javax.naming.spi.ObjectFactory#getObjectInstance(java.lang.Object,
     *      javax.naming.Name, javax.naming.Context, java.util.Hashtable <?,?>)
     */
    public Object getObjectInstance(Object obj, Name name, Context nameCtx,
            Hashtable<?,?> environment) throws Exception {
        DriverConnectionPoolDataSource ds = (
            DriverConnectionPoolDataSource)super.getObjectInstance(obj);
        return processObjectInstance(ds,obj);
    }
}
