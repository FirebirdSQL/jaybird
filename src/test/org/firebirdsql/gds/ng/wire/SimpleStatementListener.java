// SPDX-FileCopyrightText: Copyright 2013-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire;

import org.firebirdsql.gds.ng.FbStatement;
import org.firebirdsql.gds.ng.FetchDirection;
import org.firebirdsql.gds.ng.SqlCountHolder;
import org.firebirdsql.gds.ng.StatementState;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.gds.ng.listeners.StatementListener;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.sql.SQLWarning;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Simple implementation of {@link org.firebirdsql.gds.ng.listeners.StatementListener} for testing purposes
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
@NullMarked
public class SimpleStatementListener implements StatementListener {

    private final List<RowValue> rows = new ArrayList<>();
    private final List<SQLWarning> warnings = Collections.synchronizedList(new ArrayList<>());
    private @Nullable Boolean beforeFirst;
    private @Nullable Boolean afterLast;
    private @Nullable Boolean hasResultSet;
    private @Nullable Boolean hasSingletonResult;
    private @Nullable SqlCountHolder sqlCounts;
    private @Nullable Integer lastFetchCount;

    @Override
    public void receivedRow(FbStatement sender, RowValue rowValue) {
        rows.add(rowValue);
        beforeFirst = false;
        afterLast = false;
    }

    @Override
    public void fetchComplete(FbStatement sender, FetchDirection fetchDirection, int rows) {
        lastFetchCount = rows;
    }

    @Override
    public void beforeFirst(FbStatement sender) {
        beforeFirst = true;
        afterLast = false;
    }

    public void clearBeforeFirst() {
        beforeFirst = null;
    }

    @Override
    public void afterLast(FbStatement sender) {
        beforeFirst = false;
        afterLast = true;
    }

    public void clearAfterLast() {
        afterLast = null;
    }

    public void clearPosition() {
        clearBeforeFirst();
        clearAfterLast();
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

    public @Nullable Boolean isBeforeFirst() {
        return beforeFirst;
    }

    public @Nullable Boolean isAfterLast() {
        return afterLast;
    }

    public @Nullable Boolean hasResultSet() {
        return hasResultSet;
    }

    public @Nullable Boolean hasSingletonResult() {
        return hasSingletonResult;
    }

    public List<RowValue> getRows() {
        return rows;
    }

    public List<SQLWarning> getWarnings() {
        return new ArrayList<>(warnings);
    }

    public @Nullable Integer getLastFetchCount() {
        return lastFetchCount;
    }

    public void clear() {
        beforeFirst = null;
        afterLast = null;
        hasResultSet = null;
        hasSingletonResult = null;
        sqlCounts = null;
        lastFetchCount = null;
        rows.clear();
        warnings.clear();
    }

    public @Nullable SqlCountHolder getSqlCounts() {
        return sqlCounts;
    }
}
