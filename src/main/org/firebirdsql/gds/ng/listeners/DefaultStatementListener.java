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

import java.sql.SQLWarning;

/**
 * Default implementation of {@link org.firebirdsql.gds.ng.listeners.StatementListener} where all implemented methods
 * do nothing.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class DefaultStatementListener implements StatementListener {
    @Override
    public void receivedRow(FbStatement sender, RowValue rowValue) {
    }

    @Override
    public void beforeFirst(FbStatement sender) {
    }

    @Override
    public void afterLast(FbStatement sender) {
    }

    @Override
    public void statementExecuted(FbStatement sender, boolean hasResultSet, boolean hasSingletonResult) {
    }

    @Override
    public void statementStateChanged(FbStatement sender, StatementState newState, StatementState previousState) {
    }

    @Override
    public void warningReceived(FbStatement sender, SQLWarning warning) {
    }

    @Override
    public void sqlCounts(FbStatement sender, SqlCountHolder sqlCounts) {
    }
}
