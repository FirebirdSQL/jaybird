// SPDX-FileCopyrightText: Copyright 2013-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.listeners;

import org.firebirdsql.gds.ng.FbStatement;
import org.firebirdsql.gds.ng.FetchDirection;
import org.firebirdsql.gds.ng.SqlCountHolder;
import org.firebirdsql.gds.ng.StatementState;
import org.firebirdsql.gds.ng.fields.RowValue;

import java.sql.SQLWarning;

/**
 * Dispatcher to maintain and notify other {@link StatementListener}.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public final class StatementListenerDispatcher extends AbstractListenerDispatcher<StatementListener> implements StatementListener {

    private static final System.Logger log = System.getLogger(StatementListenerDispatcher.class.getName());

    @Override
    public void receivedRow(FbStatement sender, RowValue rowValue) {
        notify(listener -> listener.receivedRow(sender, rowValue), "receivedRow");
    }

    @Override
    public void fetchComplete(FbStatement sender, FetchDirection fetchDirection, int rows) {
        notify(listener -> listener.fetchComplete(sender, fetchDirection, rows), "fetchComplete");
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
        log.log(System.Logger.Level.ERROR, message, throwable);
    }
}
