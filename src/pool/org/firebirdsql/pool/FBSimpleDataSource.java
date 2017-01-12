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

import org.firebirdsql.gds.impl.GDSType;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * This is a simple implementation of {@link DataSource} interface. Connections
 * are physically opened in {@link DataSource#getConnection()} method and
 * physically closed in {@link Connection#close()} method. If you need connection
 * pooling, use a third party connection pool library instead.
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 * @deprecated This class will be removed in Jaybird 3.1; use {@link org.firebirdsql.ds.FBSimpleDataSource}
 */
@Deprecated
public class FBSimpleDataSource extends org.firebirdsql.ds.FBSimpleDataSource {

    private static final long serialVersionUID = 3156578540634970427L;

    /**
     * Create instance of this class.
     */
    public FBSimpleDataSource() {
        super();
    }

    /**
     * Create instance of this class.
     */
    public FBSimpleDataSource(GDSType type) {
        super(type);
    }
}
