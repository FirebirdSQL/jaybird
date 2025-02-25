// SPDX-FileCopyrightText: Copyright 2011-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
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
