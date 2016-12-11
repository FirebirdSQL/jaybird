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
package org.firebirdsql.jdbc;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.gds.ng.FbStatement;
import org.firebirdsql.gds.ng.fields.FieldDescriptor;
import org.firebirdsql.gds.ng.fields.FieldValue;
import org.firebirdsql.gds.ng.fields.RowDescriptor;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.gds.ng.listeners.DefaultStatementListener;
import org.firebirdsql.jdbc.field.FBField;
import org.firebirdsql.jdbc.field.FBFlushableField;
import org.firebirdsql.jdbc.field.FieldDataProvider;
import org.firebirdsql.util.SQLExceptionChainBuilder;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class responsible for modifying updatable result sets.
 *
 * A result set is updatable if and only if:
 * <ul>
 * <li>It is a subset of a single table and includes all columns from the
 * table's primary key (in other words, includes all best row identifiers) or
 * RDB$DB_KEY column (in this case tables  without primary key can be updated
 * too).
 *
 * <li>If base table columns not included in the result set allow NULL values,
 * result set allows inserting rows into it.
 *
 * <li>The result set's SELECT statement does not contain subqueries, a
 * DISTINCT predicate, a HAVING clause, aggregate functions, joined tables,
 * user-defined functions, or stored procedures.
 * </ul>
 *
 * If the result set definition does not meet these conditions, it is considered
 * read-only.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class FBRowUpdater implements FirebirdRowUpdater {

    private static final int PARAMETER_UNUSED = 0;
    private static final int PARAMETER_USED = 1;
    private static final int PARAMETER_DBKEY = 2;

    private final FBConnection connection;
    private final GDSHelper gdsHelper;
    private final Synchronizable syncProvider;
    private final RowDescriptor rowDescriptor;
    private final FBField[] fields;
    private final QuoteStrategy quoteStrategy;

    private boolean inInsertRow;

    private RowValue newRow;
    private RowValue oldRow;
    private RowValue insertRow;
    private boolean[] updatedFlags;

    private String tableName;

    private FbStatement updateStatement;
    private FbStatement deleteStatement;
    private FbStatement insertStatement;
    private FbStatement selectStatement;

    private final FBObjectListener.ResultSetListener rsListener;
    private boolean closed;
    private boolean processing;

    public FBRowUpdater(FBConnection connection, RowDescriptor rowDescriptor,
            Synchronizable syncProvider, boolean cached,
            FBObjectListener.ResultSetListener rsListener) throws SQLException {

        this.rsListener = rsListener;

        this.connection = connection;
        gdsHelper = connection.getGDSHelper();
        this.syncProvider = syncProvider;

        this.rowDescriptor = rowDescriptor;
        fields = new FBField[rowDescriptor.getCount()];

        quoteStrategy = QuoteStrategy.forDialect(gdsHelper.getDialect());

        newRow = rowDescriptor.createDefaultFieldValues();
        updatedFlags = new boolean[rowDescriptor.getCount()];

        for (int i = 0; i < rowDescriptor.getCount(); i++) {
            final int fieldPos = i;

            // implementation of the FieldDataProvider interface
            final FieldDataProvider dataProvider = new FieldDataProvider() {
                @Override
                public byte[] getFieldData() {
                    if (!updatedFlags[fieldPos]) {
                        return oldRow.getFieldValue(fieldPos).getFieldData();
                    } else if (inInsertRow) {
                        return insertRow.getFieldValue(fieldPos).getFieldData();
                    } else {
                        return newRow.getFieldValue(fieldPos).getFieldData();
                    }
                }

                @Override
                public void setFieldData(byte[] data) {
                    if (inInsertRow) {
                        insertRow.getFieldValue(fieldPos).setFieldData(data);
                    } else {
                        newRow.getFieldValue(fieldPos).setFieldData(data);
                    }
                    updatedFlags[fieldPos] = true;
                }
            };

            fields[i] = FBField.createField(rowDescriptor.getFieldDescriptor(i), dataProvider, gdsHelper, cached);
        }

        // find the table name (there can be only one table per result set)
        for (FieldDescriptor fieldDescriptor : rowDescriptor) {
            if (tableName == null) {
                tableName = fieldDescriptor.getOriginalTableName();
            } else if (!tableName.equals(fieldDescriptor.getOriginalTableName())) {
                throw new FBResultSetNotUpdatableException(
                        "Underlying result set references at least two relations: " +
                                tableName + " and " + fieldDescriptor.getOriginalTableName() + ".");
            }
        }
    }

    private void notifyExecutionStarted() throws SQLException {
        if (closed)
            throw new FBSQLException("Corresponding result set is closed.");

        if (processing)
            return;

        rsListener.executionStarted(this);
        this.processing = true;
    }

    private void notifyExecutionCompleted(boolean success) throws SQLException {
        if (!processing)
            return;

        rsListener.executionCompleted(this, success);
        this.processing = false;
    }

    private void deallocateStatement(FbStatement handle, SQLExceptionChainBuilder<SQLException> chain) {
        if (handle == null) return;
        try {
            handle.close();
        } catch (SQLException ex) {
            chain.append(ex);
        }
    }

    public void close() throws SQLException {
        SQLExceptionChainBuilder<SQLException> chain = new SQLExceptionChainBuilder<>();
        deallocateStatement(selectStatement, chain);
        deallocateStatement(insertStatement, chain);
        deallocateStatement(updateStatement, chain);
        deallocateStatement(deleteStatement, chain);

        // TODO: Close not completed by throw at this point?
        if (chain.hasException())
            throw chain.getException();

        this.closed = true;
        if (processing)
            notifyExecutionCompleted(true);
    }

    @Override
    public void setRow(RowValue row) {
        this.oldRow = row;
        this.updatedFlags = new boolean[rowDescriptor.getCount()];
        this.inInsertRow = false;
    }

    @Override
    public void cancelRowUpdates() {
        this.newRow = rowDescriptor.createDefaultFieldValues();
        this.updatedFlags = new boolean[rowDescriptor.getCount()];
        this.inInsertRow = false;
    }

    @Override
    public FBField getField(int fieldPosition) {
        return fields[fieldPosition];
    }

    /**
     * This method gets the parameter mask for the UPDATE or DELETE statement.
     * Parameter mask is an array of booleans, where array item is set to true,
     * if the appropriate field should be included in WHERE clause of the
     * UPDATE or DELETE statement.
     * <p>
     * This method obtains the parameter mask from the best row identifiers, in
     * other words set of columns that form "best row identifiers" must be a
     * subset of the selected columns (no distinction is made whether columns
     * are real or are pseudo-columns). If no
     *
     * @return array of booleans that represent parameter mask.
     */
    private int[] getParameterMask() throws SQLException {
        // loop through the "best row identifiers" and set appropriate flags.
        FBDatabaseMetaData metaData = (FBDatabaseMetaData) connection.getMetaData();

        try (ResultSet bestRowIdentifier = metaData.getBestRowIdentifier("", "", tableName,
                DatabaseMetaData.bestRowSession, true)) {
            int[] result = new int[rowDescriptor.getCount()];
            boolean hasParams = false;
            while (bestRowIdentifier.next()) {
                String columnName = bestRowIdentifier.getString(2);

                if (columnName == null)
                    continue;

                for (int i = 0; i < rowDescriptor.getCount(); i++) {
                    // special handling for the RDB$DB_KEY columns that must be
                    // selected as RDB$DB_KEY, but in XSQLVAR are represented
                    // as DB_KEY
                    if ("RDB$DB_KEY".equals(columnName) && isDbKey(rowDescriptor.getFieldDescriptor(i))) {
                        result[i] = PARAMETER_DBKEY;
                        hasParams = true;
                    } else if (columnName.equals(rowDescriptor.getFieldDescriptor(i).getOriginalName())) {
                        result[i] = PARAMETER_USED;
                        hasParams = true;
                    }
                }

                // if we did not find a column from the best row identifier
                // in our result set, throw an exception, since we cannot
                // reliably identify the row.
                if (!hasParams)
                    throw new FBResultSetNotUpdatableException(
                            "Underlying result set does not contain all columns " +
                                    "that form 'best row identifier'.");
            }

            if (!hasParams)
                throw new FBResultSetNotUpdatableException(
                        "No columns that can be used in WHERE clause could be found.");

            return result;
        }
    }

    private void appendWhereClause(StringBuilder sb, int[] parameterMask) {
        sb.append("WHERE ");

        // handle the RDB$DB_KEY case first
        boolean hasDbKey = false;
        for (int aParameterMask : parameterMask) {
            if (aParameterMask == PARAMETER_DBKEY) {
                hasDbKey = true;
                break;
            }
        }

        if (hasDbKey) {
            sb.append("RDB$DB_KEY = ?");
            return;
        }

        // if we are here, then no RDB$DB_KEY update was used
        // therefore loop through the parameters and build the
        // WHERE clause
        boolean first = true;
        for (int i = 0; i < rowDescriptor.getCount(); i++) {
            if (parameterMask[i] == PARAMETER_UNUSED) continue;

            if (!first) sb.append(" AND");

            sb.append("\n\t");
            quoteStrategy.appendQuoted(rowDescriptor.getFieldDescriptor(i).getOriginalName(), sb).append(" = ?");

            first = false;
        }
    }

    private String buildUpdateStatement(int[] parameterMask) {
        StringBuilder sb = new StringBuilder("UPDATE ");
        quoteStrategy.appendQuoted(tableName, sb)
                .append("\nSET\n");

        boolean first = true;
        for (int i = 0; i < rowDescriptor.getCount(); i++) {
            if (!updatedFlags[i])
                continue;

            if (!first)
                sb.append(',');

            sb.append("\n\t");
            quoteStrategy.appendQuoted(rowDescriptor.getFieldDescriptor(i).getOriginalName(), sb).append(" = ?");

            first = false;
        }

        sb.append('\n');
        appendWhereClause(sb, parameterMask);

        return sb.toString();
    }

    private String buildDeleteStatement(int[] parameterMask) {
        StringBuilder sb = new StringBuilder("DELETE FROM ");
        quoteStrategy.appendQuoted(tableName, sb).append('\n');
        appendWhereClause(sb, parameterMask);

        return sb.toString();
    }

    private String buildInsertStatement() {
        StringBuilder columns = new StringBuilder();
        StringBuilder params = new StringBuilder();

        boolean first = true;
        for (int i = 0; i < rowDescriptor.getCount(); i++) {

            if (!updatedFlags[i])
                continue;

            if (!first) {
                columns.append(',');
                params.append(',');
            }

            quoteStrategy.appendQuoted(rowDescriptor.getFieldDescriptor(i).getOriginalName(), columns);
            params.append('?');

            first = false;
        }

        StringBuilder sb = new StringBuilder("INSERT INTO ");
        quoteStrategy.appendQuoted(tableName, sb)
                .append(" (").append(columns).append(") VALUES (").append(params).append(')');

        return sb.toString();
    }

    private String buildSelectStatement(int[] parameterMask) {
        StringBuilder columns = new StringBuilder();

        boolean first = true;
        for (FieldDescriptor fieldDescriptor : rowDescriptor) {
            if (!first)
                columns.append(',');

            // do special handling of RDB$DB_KEY, since Firebird returns
            // DB_KEY column name instead of the correct one
            if (isDbKey(fieldDescriptor)) {
                columns.append("RDB$DB_KEY");
            } else {
                quoteStrategy.appendQuoted(fieldDescriptor.getOriginalName(), columns);
            }
            first = false;
        }

        StringBuilder sb = new StringBuilder("SELECT ");
        sb.append(columns).append('\n')
                .append("FROM ");
        quoteStrategy.appendQuoted(tableName, sb).append('\n');
        appendWhereClause(sb, parameterMask);
        return sb.toString();
    }

    /**
     * Determines if the supplied {@link org.firebirdsql.gds.ng.fields.FieldDescriptor} is a db-key (RDB$DB_KEY) of a table.
     *
     * @param fieldDescriptor
     *         Field descriptor
     * @return <code>true</code> if <code>xsqlvar</code> is a RDB$DB_KEY
     */
    private boolean isDbKey(FieldDescriptor fieldDescriptor) {
        return "DB_KEY".equals(fieldDescriptor.getOriginalName())
                && ((fieldDescriptor.getType() & ~1) == ISCConstants.SQL_TEXT)
                && fieldDescriptor.getLength() == 8;
    }

    private static final int UPDATE_STATEMENT_TYPE = 1;
    private static final int DELETE_STATEMENT_TYPE = 2;
    private static final int INSERT_STATEMENT_TYPE = 3;
    private static final int SELECT_STATEMENT_TYPE = 4;

    @Override
    public void updateRow() throws SQLException {
        boolean success = false;

        synchronized (syncProvider.getSynchronizationObject()) {
            try {
                notifyExecutionStarted();

                if (updateStatement == null) {
                    updateStatement = gdsHelper.allocateStatement();
                } else {
                    updateStatement.setTransaction(gdsHelper.getCurrentTransaction());
                }

                executeStatement(UPDATE_STATEMENT_TYPE, updateStatement);

                success = true;
            } finally {
                notifyExecutionCompleted(success);
            }
        }
    }

    @Override
    public void deleteRow() throws SQLException {
        boolean success = false;

        synchronized (syncProvider.getSynchronizationObject()) {
            try {
                notifyExecutionStarted();

                if (deleteStatement == null) {
                    deleteStatement = gdsHelper.allocateStatement();
                } else {
                    deleteStatement.setTransaction(gdsHelper.getCurrentTransaction());
                }

                executeStatement(DELETE_STATEMENT_TYPE, deleteStatement);

                success = true;
            } finally {
                notifyExecutionCompleted(success);
            }
        }
    }

    @Override
    public void insertRow() throws SQLException {
        boolean success = false;

        synchronized (syncProvider.getSynchronizationObject()) {
            try {
                notifyExecutionStarted();

                if (insertStatement == null) {
                    insertStatement = gdsHelper.allocateStatement();
                } else {
                    insertStatement.setTransaction(gdsHelper.getCurrentTransaction());
                }

                executeStatement(INSERT_STATEMENT_TYPE, insertStatement);

                success = true;
            } finally {
                notifyExecutionCompleted(success);
            }
        }
    }

    @Override
    public void refreshRow() throws SQLException {
        boolean success = false;

        synchronized (syncProvider.getSynchronizationObject()) {
            try {
                notifyExecutionStarted();

                if (selectStatement == null) {
                    selectStatement = gdsHelper.allocateStatement();
                } else {
                    selectStatement.setTransaction(gdsHelper.getCurrentTransaction());
                }

                final RowListener rowListener = new RowListener();
                selectStatement.addStatementListener(rowListener);

                try {
                    executeStatement(SELECT_STATEMENT_TYPE, selectStatement);

                    // should fetch one row anyway
                    selectStatement.fetchRows(10);

                    List<RowValue> rows = rowListener.getRows();
                    if (rows.size() == 0)
                        throw new SQLException("No rows could be fetched.");

                    if (rows.size() > 1)
                        throw new SQLException("More then one row fetched.");

                    setRow(rows.get(0));
                } finally {
                    selectStatement.removeStatementListener(rowListener);
                    selectStatement.closeCursor();
                }

                success = true;
            } finally {
                notifyExecutionCompleted(success);
            }
        }
    }

    private void executeStatement(int statementType, FbStatement stmt) throws SQLException {
        if (inInsertRow && statementType != INSERT_STATEMENT_TYPE) {
            throw new SQLException("Only insertRow() is allowed when result set is positioned on insert row.");
        }

        if (statementType != INSERT_STATEMENT_TYPE && oldRow == null) {
            throw new SQLException("Result set is not positioned on a row.");
        }

        // we have to flush before constructing the parameters
        // since flushable field can update the value, which
        // in turn can change the parameter distribution
        for (int i = 0; i < rowDescriptor.getCount(); i++) {
            if (fields[i] instanceof FBFlushableField)
                ((FBFlushableField) fields[i]).flushCachedData();
        }

        int[] parameterMask = getParameterMask();

        String sql;
        switch (statementType) {
        case UPDATE_STATEMENT_TYPE:
            sql = buildUpdateStatement(parameterMask);
            break;

        case DELETE_STATEMENT_TYPE:
            sql = buildDeleteStatement(parameterMask);
            break;

        case INSERT_STATEMENT_TYPE:
            sql = buildInsertStatement();
            break;

        case SELECT_STATEMENT_TYPE:
            sql = buildSelectStatement(parameterMask);
            break;

        default:
            throw new IllegalArgumentException("Incorrect statement type specified.");
        }

        stmt.prepare(sql);

        List<FieldValue> params = new ArrayList<>();

        if (statementType == UPDATE_STATEMENT_TYPE) {
            for (int i = 0; i < rowDescriptor.getCount(); i++) {
                if (!updatedFlags[i]) continue;

                params.add(newRow.getFieldValue(i).clone());
            }
        }

        for (int i = 0; i < rowDescriptor.getCount(); i++) {
            if (parameterMask[i] == PARAMETER_UNUSED && statementType != INSERT_STATEMENT_TYPE) {
                continue;
            } else if (!updatedFlags[i] && statementType == INSERT_STATEMENT_TYPE) {
                continue;
            }
            if (statementType == INSERT_STATEMENT_TYPE) {
                params.add(insertRow.getFieldValue(i).clone());
            } else {
                params.add(oldRow.getFieldValue(i).clone());
            }
        }

        stmt.execute(new RowValue(params.toArray(new FieldValue[0])));

        // TODO think about adding COMMIT RETAIN in the auto-commit mode
    }

    @Override
    public boolean rowInserted() throws SQLException {
        return false;
    }

    @Override
    public boolean rowDeleted() throws SQLException {
        return false;
    }

    @Override
    public boolean rowUpdated() throws SQLException {
        return false;
    }

    @Override
    public RowValue getNewRow() {
        FieldValue[] fieldValues = new FieldValue[oldRow.getCount()];
        for (int i = 0; i < fieldValues.length; i++) {
            if (updatedFlags[i]) {
                fieldValues[i] = newRow.getFieldValue(i).clone();
            } else {
                fieldValues[i] = oldRow.getFieldValue(i).clone();
            }
        }
        return new RowValue(fieldValues);
    }

    @Override
    public RowValue getInsertRow() {
        return insertRow;
    }

    @Override
    public RowValue getOldRow() {
        return oldRow;
    }

    @Override
    public void moveToInsertRow() throws SQLException {
        inInsertRow = true;
        insertRow = rowDescriptor.createDefaultFieldValues();
        this.updatedFlags = new boolean[rowDescriptor.getCount()];
    }

    @Override
    public void moveToCurrentRow() throws SQLException {
        inInsertRow = false;
        insertRow = rowDescriptor.createDefaultFieldValues();
        this.updatedFlags = new boolean[rowDescriptor.getCount()];
    }

    private static class RowListener extends DefaultStatementListener {
        private final List<RowValue> rows = new ArrayList<>();

        @Override
        public void receivedRow(FbStatement sender, RowValue rowValue) {
            rows.add(rowValue);
        }

        public List<RowValue> getRows() {
            return rows;
        }
    }
}