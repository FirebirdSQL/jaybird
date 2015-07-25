/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
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
    public void receivedRow(final FbStatement sender, final RowValue rowValue) {
        for (StatementListener listener : this) {
            try {
                listener.receivedRow(sender, rowValue);
            } catch (Exception e) {
                log.error("Error on notify receivedRow to listener " + listener, e);
            }
        }
    }

    @Override
    public void allRowsFetched(final FbStatement sender) {
        for (StatementListener listener : this) {
            try {
                listener.allRowsFetched(sender);
            } catch (Exception e) {
                log.error("Error on notify allRowsFetched to listener " + listener, e);
            }
        }
    }

    @Override
    public void statementExecuted(final FbStatement sender, final boolean hasResultSet, final boolean hasSingletonResult) {
        for (StatementListener listener : this) {
            try {
                listener.statementExecuted(sender, hasResultSet, hasSingletonResult);
            } catch (Exception e) {
                log.error("Error on notify statementExecuted to listener " + listener, e);
            }
        }
    }

    @Override
    public void statementStateChanged(FbStatement sender, StatementState newState, StatementState previousState) {
        for (StatementListener listener : this) {
            try {
                listener.statementStateChanged(sender, newState, previousState);
            } catch (Exception e) {
                log.error("Error on notify statementStateChanged to listener " + listener, e);
            }
        }
    }

    @Override
    public void warningReceived(FbStatement sender, SQLWarning warning) {
        for (StatementListener listener : this) {
            try {
                listener.warningReceived(sender, warning);
            } catch (Exception e) {
                log.error("Error on notify warningReceived to listener " + listener, e);
            }
        }
    }

    @Override
    public void sqlCounts(FbStatement sender, SqlCountHolder sqlCounts) {
        for (StatementListener listener : this) {
            try {
                listener.sqlCounts(sender, sqlCounts);
            } catch (Exception e) {
                log.error("Error on notify sqlCounts to listener " + listener, e);
            }
        }
    }
}
