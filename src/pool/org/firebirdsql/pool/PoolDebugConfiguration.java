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


/**
 * Debug configuration of the pool. 
 */
@Deprecated
public class PoolDebugConfiguration {

    public final static boolean LOG_DEBUG_INFO;
    public final static boolean SHOW_TRACE;
    public final static boolean DEBUG_STMT_POOL;
    public final static boolean DEBUG_REENTRANT;
    
    static {
        LOG_DEBUG_INFO = Boolean.getBoolean("FBLog4j");
        SHOW_TRACE = Boolean.getBoolean("FBPoolShowTrace");
        DEBUG_STMT_POOL = Boolean.getBoolean("FBPoolDebugStmtCache");
        DEBUG_REENTRANT = Boolean.getBoolean("FBPoolDebugReentrant");
    }

}
