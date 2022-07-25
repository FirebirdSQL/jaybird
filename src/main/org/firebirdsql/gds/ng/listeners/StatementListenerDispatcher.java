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
package org.firebirdsql.gds.ng.listeners;

import org.firebirdsql.gds.ng.FbStatement;
import org.firebirdsql.gds.ng.SqlCountHolder;
import org.firebirdsql.gds.ng.StatementState;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.sql.SQLWarning;

/**
 * Dispatcher to maintain and notify other {@link StatementListener}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public final class StatementListenerDispatcher extends AbstractListenerDispatcher<StatementListener> implements StatementListener {

    private static final Logger log = LoggerFactory.getLogger(StatementListenerDispatcher.class);

    @Override
    public void receivedRow(FbStatement sender, RowValue rowValue) {
        notify(listener -> listener.receivedRow(sender, rowValue), "receivedRow");
    }

    @Override
    public void beforeFirst(FbStatement sender) {
        notify(listener -> listener.beforeFirst(sender), "beforeFirst");
    }

    @Override
    public void afterLast(FbStatement sender) {
        notify(listener -> listener.afterLast(sender), "afterLast");
    }

    @Override
    public void statementExecuted(FbStatement sender, boolean hasResultSet, boolean hasSingletonResult) {
        notify(listener ->
                listener.statementExecuted(sender, hasResultSet, hasSingletonResult), "statementExecuted");
    }

    @Override
    public void statementStateChanged(FbStatement sender, StatementState newState, StatementState previousState) {
        notify(listener ->
                listener.statementStateChanged(sender, newState, previousState), "statementStateChanged");
    }

    @Override
    public void warningReceived(FbStatement sender, SQLWarning warning) {
        notify(listener -> listener.warningReceived(sender, warning), "warningReceived");
    }

    @Override
    public void sqlCounts(FbStatement sender, SqlCountHolder sqlCounts) {
        notify(listener -> listener.sqlCounts(sender, sqlCounts), "sqlCounts");
    }

    @Override
    protected void logError(String message, Throwable throwable) {
        log.error(message, throwable);
    }
}
