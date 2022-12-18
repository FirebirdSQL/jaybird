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
package org.firebirdsql.jdbc;

import org.firebirdsql.jaybird.props.PropertyConstants;

/**
 * Default values for JDBC connection.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @deprecated will be removed in Jaybird 6, see {@link org.firebirdsql.jaybird.props.PropertyConstants} for
 * replacement. However, that class is considered internal API and may change without notice.
 */
@Deprecated
public class FBConnectionDefaults {

    /**
     * @deprecated use {@link org.firebirdsql.jaybird.props.PropertyConstants#DEFAULT_BLOB_BUFFER_SIZE}
     */
    @Deprecated
    public static final int DEFAULT_BLOB_BUFFER_SIZE = PropertyConstants.DEFAULT_BLOB_BUFFER_SIZE;
    
    /**
     * Default socket buffer size is {@code -1}, meaning that we will use socket buffer size default for runtime platform.
     *
     * @deprecated use {@link org.firebirdsql.jaybird.props.PropertyConstants#BUFFER_SIZE_NOT_SET}
     */
    @Deprecated
    public static final int DEFAULT_SOCKET_BUFFER_SIZE = -1;
}
