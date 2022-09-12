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
package org.firebirdsql.jdbc;

import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.gds.ng.FbStatement;
import org.firebirdsql.gds.ng.LockCloseable;
import org.firebirdsql.gds.ng.fields.FieldDescriptor;
import org.firebirdsql.gds.ng.fields.RowDescriptor;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.gds.ng.listeners.StatementListener;
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
 * <p>
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
 * </p>
 * <p>
 * If the result set definition does not meet these conditions, it is considered
 * read-only.
 * </p>
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
final class FBRowUpdater implements FirebirdRowUpdater {

    private static final int PARAMETER_UNUSED = 0;
    private static final int PARAMETER_USED = 1;
    private static final int PARAMETER_DBKEY = 2;
    private static final byte[][] EMPTY_2D_BYTES = new byte[0][];

    private final FBConnection connection;
    private final GDSHelper gdsHelper;
    private final RowDescriptor rowDescriptor;
    private final FBField[] fields;
    private final QuoteStrategy quoteStrategy;

    private boolean inInsertRow;

    private RowValue newRow;
    private RowValue oldRow;
    private RowValue insertRow;
    private boolean[] updatedFlags;

    private String tableName;

    private final FbStatement[] statements = new FbStatement[4];

    private final FBObjectListener.ResultSetListener rsListener;
    private boolean closed;
    private boolean processing;

    FBRowUpdater(FBConnection connection, RowDescriptor rowDescriptor, boolean cached,
            FBObjectListener.ResultSetListener rsListener) throws SQLException {
        this.rsListener = rsListener;

        this.connection = connection;
        gdsHelper = connection.getGDSHelper();

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
                        return oldRow.getFieldData(fieldPos);
                    } else if (inInsertRow) {
                        return insertRow.getFieldData(fieldPos);
                    } else {
                        return newRow.getFieldData(fieldPos);
                    }
                }

                @Override
                public void setFieldData(byte[] data) {
                    if (inInsertRow) {
                        insertRow.setFieldData(fieldPos, data);
                    } else {
                        newRow.setFieldData(fieldPos, data);
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
        for (FbStatement statement : statements) {
            deallocateStatement(statement, chain);
        }

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
                DatabaseMetaData.bestRowTransaction, true)) {
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
                    if ("RDB$DB_KEY".equals(columnName) && rowDescriptor.getFieldDescriptor(i).isDbKey()) {
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
            if (fieldDescriptor.isDbKey()) {
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

    private static final int UPDATE_STATEMENT_TYPE = 0;
    private static final int DELETE_STATEMENT_TYPE = 1;
    private static final int INSERT_STATEMENT_TYPE = 2;
    private static final int SELECT_STATEMENT_TYPE = 3;

    private void modifyRow(int statementType) throws SQLException {
        try (LockCloseable ignored = gdsHelper.withLock()) {
            boolean success = false;
            try {
                notifyExecutionStarted();

                executeStatement(statementType, getStatementWithTransaction(statementType));
                
                success = true;
            } finally {
                notifyExecutionCompleted(success);
            }
        }
    }

    @SuppressWarnings("resource")
    private FbStatement getStatementWithTransaction(int statementType) throws SQLException {
        FbStatement stmt = statements[statementType];
        if (stmt == null) {
            return statements[statementType] = gdsHelper.allocateStatement();
        } else {
            stmt.setTransaction(gdsHelper.getCurrentTransaction());
            return stmt;
        }
    }

    @Override
    public void updateRow() throws SQLException {
        modifyRow(UPDATE_STATEMENT_TYPE);
    }

    @Override
    public void deleteRow() throws SQLException {
        modifyRow(DELETE_STATEMENT_TYPE);
    }

    @Override
    public void insertRow() throws SQLException {
        modifyRow(INSERT_STATEMENT_TYPE);
    }

    @Override
    public void refreshRow() throws SQLException {
        try (LockCloseable ignored = gdsHelper.withLock()) {
            boolean success = false;
            try {
                notifyExecutionStarted();

                FbStatement selectStatement = getStatementWithTransaction(SELECT_STATEMENT_TYPE);

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

        List<byte[]> params = new ArrayList<>();

        if (statementType == UPDATE_STATEMENT_TYPE) {
            for (int i = 0; i < rowDescriptor.getCount(); i++) {
                if (!updatedFlags[i]) continue;

                params.add(newRow.getFieldData(i));
            }
        }

        RowValue source = statementType == INSERT_STATEMENT_TYPE ? insertRow : oldRow;
        for (int i = 0; i < rowDescriptor.getCount(); i++) {
            if (parameterMask[i] == PARAMETER_UNUSED && statementType != INSERT_STATEMENT_TYPE) {
                continue;
            } else if (!updatedFlags[i] && statementType == INSERT_STATEMENT_TYPE) {
                continue;
            }
            params.add(source.getFieldData(i));
        }

        stmt.execute(RowValue.of(params.toArray(EMPTY_2D_BYTES)));
    }

    @Override
    public RowValue getNewRow() {
        RowValue newRowCopy = rowDescriptor.createDefaultFieldValues();
        for (int i = 0; i < rowDescriptor.getCount(); i++) {
            RowValue source = updatedFlags[i] ? newRow : oldRow;
            byte[] fieldData = source.getFieldData(i);
            newRowCopy.setFieldData(i, fieldData != null ? fieldData.clone() : null);
        }
        return newRowCopy;
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
    public void moveToInsertRow() {
        inInsertRow = true;
        insertRow = rowDescriptor.createDefaultFieldValues();
        this.updatedFlags = new boolean[rowDescriptor.getCount()];
    }

    @Override
    public void moveToCurrentRow() {
        inInsertRow = false;
        insertRow = rowDescriptor.createDefaultFieldValues();
        this.updatedFlags = new boolean[rowDescriptor.getCount()];
    }

    private static final class RowListener implements StatementListener {
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