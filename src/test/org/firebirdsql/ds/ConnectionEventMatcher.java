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
package org.firebirdsql.ds;

import org.hamcrest.Matcher;
import org.mockito.ArgumentMatcher;

import javax.sql.ConnectionEvent;
import javax.sql.PooledConnection;
import java.sql.SQLException;

/**
 * Matcher to check for ConnectionEvents
 *  
 * @author Mark Rotteveel
 */
class ConnectionEventMatcher implements ArgumentMatcher<ConnectionEvent> {
    private final PooledConnection pooled;
    private final Matcher<SQLException> exceptionMatcher;

    public ConnectionEventMatcher(PooledConnection pooled, Matcher<SQLException> exceptionMatcher) {
        this.pooled = pooled;
        this.exceptionMatcher = exceptionMatcher;
    }

    @Override
    public boolean matches(ConnectionEvent item) {
        return item.getSource() == pooled && exceptionMatcher.matches(item.getSQLException());
    }
}
