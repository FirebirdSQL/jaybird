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
package org.firebirdsql.jdbc;

/**
 * Default values for JDBC connection.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public class FBConnectionDefaults {
    
    public static final int DEFAULT_BLOB_BUFFER_SIZE = 16 * 1024;
    
    /**
     * Default socket buffer size is <code>-1</code>, meaning that we
     * will use socket buffer size default for runtime platform.
     */
    public static final int DEFAULT_SOCKET_BUFFER_SIZE = -1;
}
