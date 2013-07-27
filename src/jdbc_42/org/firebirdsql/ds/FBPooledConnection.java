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

import javax.sql.StatementEventListener;
import java.sql.Connection;

/**
 * JDBC 4.0 implementation of PooledConnection.
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.2
 */
public class FBPooledConnection extends AbstractPooledConnection {

    protected FBPooledConnection(Connection connection) {
        super(connection);
    }
    
    public void addStatementEventListener(StatementEventListener listener) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void removeStatementEventListener(StatementEventListener listener) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}