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
package org.firebirdsql.ds;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * InvocationHandler for the logical connection returned by FBXAConnection.
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.2
 */
public class XAConnectionHandler extends PooledConnectionHandler {
    
    private final FBXAConnection xaOwner;

    protected XAConnectionHandler(Connection connection, FBXAConnection owner) {
        super(connection, owner);
        xaOwner = owner;
    }
    
    protected boolean isRollbackAllowed() throws SQLException {
        return !(xaOwner.inDistributedTransaction() || connection.getAutoCommit());
    }
}
