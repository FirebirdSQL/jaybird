/*
 * $Id$
 * 
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
package org.firebirdsql.gds.ng.wire;

import org.firebirdsql.gds.ng.FbStatement;
import org.firebirdsql.gds.ng.SqlCountHolder;
import org.firebirdsql.gds.ng.StatementState;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.gds.ng.listeners.StatementListener;

import java.sql.SQLWarning;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Simple implementation of {@link org.firebirdsql.gds.ng.listeners.StatementListener} for testing purposes
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class SimpleStatementListener implements StatementListener {

    private final List<RowValue> rows = new ArrayList<RowValue>();
    private final List<SQLWarning> warnings = Collections.synchronizedList(new ArrayList<SQLWarning>());
    private Boolean allRowsFetched;
    private Boolean hasResultSet;
    private Boolean hasSingletonResult;
    private SqlCountHolder sqlCounts;

    @Override
    public void receivedRow(FbStatement sender, RowValue rowValue) {
        rows.add(rowValue);
    }

    @Override
    public void allRowsFetched(FbStatement sender) {
        allRowsFetched = true;
    }

    @Override
    public void statementExecuted(FbStatement sender, boolean hasResultSet, boolean hasSingletonResult) {
        this.hasResultSet = hasResultSet;
        this.hasSingletonResult = hasSingletonResult;
    }

    @Override
    public void statementStateChanged(FbStatement sender, StatementState newState, StatementState previousState) {
        // unused for now
    }

    @Override
    public void warningReceived(FbStatement sender, SQLWarning warning) {
        warnings.add(warning);
    }

    @Override
    public void sqlCounts(FbStatement sender, SqlCountHolder sqlCounts) {
        this.sqlCounts = sqlCounts;
    }

    public Boolean isAllRowsFetched() {
        return allRowsFetched;
    }

    public Boolean hasResultSet() {
        return hasResultSet;
    }

    public Boolean hasSingletonResult() {
        return hasSingletonResult;
    }

    public List<RowValue> getRows() {
        return rows;
    }

    public List<SQLWarning> getWarnings() {
        return new ArrayList<>(warnings);
    }

    public void clear() {
        allRowsFetched = null;
        hasResultSet = null;
        hasSingletonResult = null;
        sqlCounts = null;
        rows.clear();
        warnings.clear();
    }

    public SqlCountHolder getSqlCounts() {
        return sqlCounts;
    }
}
