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

import java.sql.Connection;

/**
 * Default values for the connection pool.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
@Deprecated
public class FBPoolingDefaults {

    public static final int DEFAULT_IDLE_TIMEOUT = Integer.MAX_VALUE / 1000;
    public static final int DEFAULT_BLOCKING_TIMEOUT = 5 * 1000;
    public static final int DEFAULT_LOGIN_TIMEOUT = DEFAULT_BLOCKING_TIMEOUT / 1000;
    public static final int DEFAULT_RETRY_INTERVAL = 1 * 1000;
    public static final int DEFAULT_PING_INTERVAL = 5 * 1000;
    
    public static final int DEFAULT_MAX_SIZE = 10;
    public static final int DEFAULT_MIN_SIZE = 0;
    
    public static final int DEFAULT_MAX_STATEMENTS = 1000;
    
    public static final int DEFAULT_ISOLATION = Connection.TRANSACTION_READ_COMMITTED;
    
}
