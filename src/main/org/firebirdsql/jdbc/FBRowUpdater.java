// SPDX-FileCopyrightText: Copyright 2005-2011 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2012-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc;

import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.gds.ng.FbStatement;
import org.firebirdsql.gds.ng.LockCloseable;
import org.firebirdsql.gds.ng.fields.FieldDescriptor;
import org.firebirdsql.gds.ng.fields.RowDescriptor;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.gds.ng.listeners.StatementListener;
import org.firebirdsql.jaybird.util.ObjectReference;
import org.firebirdsql.jaybird.util.SQLExceptionChainBuilder;
import org.firebirdsql.jaybird.util.UncheckedSQLException;
import org.firebirdsql.jdbc.field.FBField;
import org.firebirdsql.jdbc.field.FBFlushableField;
import org.firebirdsql.jdbc.field.FieldDataProvider;
import org.jspecify.annotations.Nullable;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

import static org.firebirdsql.jdbc.SQLStateConstants.SQL_STATE_INVALID_CURSOR_STATE;

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
 * @author Roman Rokytskyy
 * @author Mark Rotteveel
 */
final class FBRowUpdater implements FirebirdRowUpdater {

    // Estimated average column length of 10 + 2 quote characters + comma (for pre-sizing string builders).
    // We could be precise by summing over the field descriptors, but we don't want to waste cycles on it.
    private static final int EST_COLUMN_SIZE = 13;
    // Estimated size for update/delete/select statement (for pre-sizing string builders).
    // This is probably still too small for some cases, but will prevent a number of resizes from the default size
    private static final int EST_STATEMENT_SIZE = 64;
    private static final RowValue NO_CURRENT_ROW = RowValue.EMPTY_ROW_VALUE;

    private static final String ROW_INSERT = "insert";
    private static final String ROW_CURRENT = "current";
    private static final String ROW_UPDATE = "update";
    private static final String ROW_OLD = "old";

    private static final byte[][] EMPTY_2D_BYTES = new byte[0][];

    private final ObjectReference table;
    private final FBObjectListener.ResultSetListener rsListener;
    private final GDSHelper gdsHelper;
    private final RowDescriptor rowDescriptor;
    private final List<FBField> fields;
    private final QuoteStrategy quoteStrategy;
    private final Map<StatementType, FbStatement> statements = new EnumMap<>(StatementType.class);

    private final List<FieldDescriptor> keyColumns;
    private final RowValue newRow;
    private RowValue oldRow = NO_CURRENT_ROW;

    private boolean inInsertRow;
    private boolean closed;
    private boolean processing;

    FBRowUpdater(FBConnection connection, RowDescriptor rowDescriptor, boolean cached,
            FBObjectListener.ResultSetListener rsListener) throws SQLException {
        quoteStrategy = connection.getQuoteStrategy();
        table = requireSingleTable(rowDescriptor, quoteStrategy);
        keyColumns = deriveKeyColumns(table, rowDescriptor, connection.getMetaData());

        this.rsListener = rsListener;
        gdsHelper = connection.getGDSHelper();

        fields = createFields(rowDescriptor, cached);
        newRow = rowDescriptor.createDefaultFieldValues();
        this.rowDescriptor = rowDescriptor;
    }

    private List<FBField> createFields(RowDescriptor rowDescriptor, boolean cached) throws SQLException {
        try {
            return StreamSupport.stream(rowDescriptor.spliterator(), false)
                    .map(fieldDescriptor -> createFieldUnchecked(fieldDescriptor, cached))
                    .toList();
        } catch (UncheckedSQLException e) {
            throw e.getCause();
        }
    }

    private FBField createFieldUnchecked(FieldDescriptor fieldDescriptor, boolean cached) {
        try {
            return FBField.createField(
                    fieldDescriptor, new FieldDataProviderImpl(fieldDescriptor.getPosition()), gdsHelper, cached);
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }
    }

    /**
     * Returns the single table name (including schema if supported) referenced by {@code rowDescriptor}, or throws
     * an exception if there are multiple tables, or derived columns (columns without a relation).
     *
     * @param rowDescriptor
     *         row descriptor
     * @return non-null table identifier chain
     * @throws SQLException
     *         if {@code rowDescriptor} references multiple tables or has derived columns
     */
    private static ObjectReference requireSingleTable(RowDescriptor rowDescriptor, QuoteStrategy quoteStrategy)
            throws SQLException {
        // find the table (there can be only one table per updatable result set)
        ObjectReference table = null;
        for (FieldDescriptor fieldDescriptor : rowDescriptor) {
            var currentTable = ObjectReference.ofTable(fieldDescriptor).orElse(null);
            if (currentTable == null) {
                // No table => derived column => not updatable
                throw new FBResultSetNotUpdatableException(
                        "Underlying result set has derived columns (without a relation)");
            }
            if (table == null) {
                table = currentTable;
            } else if (!table.equals(currentTable)) {
                // Different table => not updatable
                throw new FBResultSetNotUpdatableException(
                        "Underlying result set references at least two relations: %s and %s."
                                .formatted(table.toString(quoteStrategy), currentTable.toString(quoteStrategy)));
            }
        }
        if (table == null) {
            // Primarily to silence nullability warning
            throw new FBResultSetNotUpdatableException("Underlying result set has no columns");
        }
        return table;
    }

    private void notifyExecutionStarted() throws SQLException {
        if (closed) throw new SQLException("Corresponding result set is closed.", SQL_STATE_INVALID_CURSOR_STATE);
        if (processing) return;
        rsListener.executionStarted(this);
        processing = true;
    }

    private void notifyExecutionCompleted(boolean success) throws SQLException {
        if (!processing) return;
        rsListener.executionCompleted(this, success);
        processing = false;
    }

    private void deallocateStatement(FbStatement handle, SQLExceptionChainBuilder chain) {
        try {
            handle.close();
        } catch (SQLException ex) {
            chain.append(ex);
        }
    }

    @Override
    public void close() throws SQLException {
        closed = true;
        var chain = new SQLExceptionChainBuilder();
        for (FbStatement statement : statements.values()) {
            deallocateStatement(statement, chain);
        }
        try {
            notifyExecutionCompleted(true);
        } catch (SQLException e) {
            chain.append(e);
        }
        chain.throwIfPresent();
    }

    @Override
    public void setRow(RowValue row) {
        oldRow = row;
        newRow.reset();
        inInsertRow = false;
    }

    @Override
    public void cancelRowUpdates() {
        newRow.reset();
        inInsertRow = false;
    }

    @Override
    public FBField getField(int fieldPosition) {
        return fields.get(fieldPosition);
    }

    /**
     * This method derives the key columns that uniquely identify the row in the result set.
     * <p>
     * The key column(s) are the best row identifier, or {@code RDB$DB_KEY} if available. The columns of that 'best row
     * identifier' (or the DB key) must be a subset of the selected columns. If no suitable columns are found, an
     * exception is thrown.
     * </p>
     *
     * @return immutable list of columns that uniquely identify the row, for use in the WHERE clause of the UPDATE,
     * DELETE, or SELECT statements created in this class.
     * @throws FBResultSetNotUpdatableException
     *         if there are no suitable columns to identify a row uniquely
     * @throws SQLException
     *         for errors looking up the best row identifier
     */
    private static List<FieldDescriptor> deriveKeyColumns(ObjectReference table, RowDescriptor rowDescriptor,
            DatabaseMetaData dbmd) throws SQLException {
        // first try best row identifier
        List<FieldDescriptor> keyColumns = keyColumnsOfBestRowIdentifier(table, rowDescriptor, dbmd);
        if (keyColumns.isEmpty()) {
            // best row identifier not available or not fully matched, fallback to RDB$DB_KEY
            // NOTE: fallback is updatable, but may not be insertable (e.g. if missing PK column(s) are not generated)!
            keyColumns = keyColumnsOfDbKey(rowDescriptor);

            if (keyColumns.isEmpty()) {
                // we did not find the columns of the best row identifier or RDB$DB_KEY in our result set,
                // throw an exception, since we cannot reliably identify the row.
                throw new FBResultSetNotUpdatableException("Underlying result set does not contain all columns that "
                        + "form 'best row identifier' and no RDB$DB_KEY was available as fallback");
            }
        }

        return List.copyOf(keyColumns);
    }

    /**
     * Derives the key columns based on {@code DatabaseMetaData.getBestRowIdentifier}.
     * <p>
     * The 'best row identifier' are the primary key columns <em>or</em> {@code RDB$DB_KEY} if there is no primary key.
     * </p>
     *
     * @return a list of columns in the best row identifier, or empty if there is no best row identifier, or not all
     * columns of the best row identifier exist in {@code rowDescriptor}
     * @throws SQLException
     *         for errors looking up the best row identifier
     */
    private static List<FieldDescriptor> keyColumnsOfBestRowIdentifier(ObjectReference table,
            RowDescriptor rowDescriptor, DatabaseMetaData dbmd) throws SQLException {
        // Method local wrapper to extract schema and table name from an ObjectReference
        // NOTE: We're assuming, but not verifying, a size of 1 or 2.
        record TableRef(ObjectReference ref) {
            String schema() {
                return ref.size() == 1 ? "" : ref.first().name();
            }

            String tableName() {
                return ref.size() == 1 ? ref.first().name() : ref.last().name();
            }
        }
        TableRef tableRef = new TableRef(table);
        try (ResultSet bestRowIdentifier = dbmd.getBestRowIdentifier(
                "", tableRef.schema(), tableRef.tableName(), DatabaseMetaData.bestRowTransaction, true)) {
            int bestRowIdentifierColumnCount = 0;
            List<FieldDescriptor> keyColumns = new ArrayList<>();
            while (bestRowIdentifier.next()) {
                bestRowIdentifierColumnCount++;
                String columnName = bestRowIdentifier.getString(2);
                if (columnName == null) continue;

                for (FieldDescriptor fieldDescriptor : rowDescriptor) {
                    // NOTE: We only use the first occurrence of a column
                    // TODO repeated columns in an updatable result set might be problematic in and of itself, maybe we
                    //  need to explicitly disallow it.
                    if ("RDB$DB_KEY".equals(columnName) && fieldDescriptor.isDbKey()) {
                        // special handling for the RDB$DB_KEY columns that must be referenced as RDB$DB_KEY in select
                        // and where, but in metadata are represented as DB_KEY
                        return List.of(fieldDescriptor);
                    } else if (columnName.equals(fieldDescriptor.getOriginalName())) {
                        keyColumns.add(fieldDescriptor);
                    }
                }

                // column of best row identifier not found, stop matching process
                if (keyColumns.size() != bestRowIdentifierColumnCount) {
                    return List.of();
                }
            }
            return keyColumns;
        }
    }

    /**
     * Derives the key column based on {@code RDB$DB_KEY}, if present in the result set.
     * <p>
     * This is intended as a fallback when {@code keyColumnsOfBestRowIdentifier} returns an empty list.
     * </p>
     *
     * @return list with the (single) {@link FieldDescriptor} of the {@code RDB$DB_KEY} column, or an empty list if
     * {@code rowDescriptor} contains no {@code RDB$DB_KEY} column
     */
    private static List<FieldDescriptor> keyColumnsOfDbKey(RowDescriptor rowDescriptor) {
        for (FieldDescriptor fieldDescriptor : rowDescriptor) {
            if (fieldDescriptor.isDbKey()) {
                return List.of(fieldDescriptor);
            }
        }
        return List.of();
    }

    private void appendWhereClause(StringBuilder sb) {
        sb.append("where ");

        // handle the RDB$DB_KEY case first
        if (keyColumns.get(0).isDbKey()) {
            sb.append("RDB$DB_KEY=?");
            return;
        }

        // no RDB$DB_KEY update was used, so loop through the key columns and build the WHERE clause
        boolean first = true;
        for (FieldDescriptor fieldDescriptor : keyColumns) {
            if (first) {
                first = false;
            } else {
                sb.append("\nand ");
            }

            //noinspection DataFlowIssue : the way keyColumns is constructed guarantees originalName is non-null
            quoteStrategy.appendQuoted(fieldDescriptor.getOriginalName(), sb).append("=?");
        }
    }

    private String buildUpdateStatement() {
        // TODO raise exception if there are no updated columns, or do nothing?
        var sb = new StringBuilder(EST_STATEMENT_SIZE + newRow.initializedCount() * EST_COLUMN_SIZE)
                .append("update ");
        table.append(sb, quoteStrategy).append(" set ");

        boolean first = true;
        for (FieldDescriptor fieldDescriptor : rowDescriptor) {
            if (!newRow.isInitialized(fieldDescriptor.getPosition()) || fieldDescriptor.isDbKey()) continue;

            if (first) {
                first = false;
            } else {
                sb.append(",\n\t");
            }

            //noinspection DataFlowIssue : given the check in requireSingleTable, originalName should be non-null
            quoteStrategy.appendQuoted(fieldDescriptor.getOriginalName(), sb).append("=?");
        }

        sb.append('\n');
        appendWhereClause(sb);

        return sb.toString();
    }

    private String buildDeleteStatement() {
        var sb = new StringBuilder(EST_STATEMENT_SIZE).append("delete from ");
        table.append(sb, quoteStrategy).append('\n');
        appendWhereClause(sb);

        return sb.toString();
    }

    private String buildInsertStatement() {
        // TODO raise exception if there are no initialized columns, or use INSERT .. DEFAULT VALUES?
        final int initializedColumnCount = newRow.initializedCount();
        var columns = new StringBuilder(initializedColumnCount * EST_COLUMN_SIZE);
        var params = new StringBuilder(initializedColumnCount * 2);

        boolean first = true;
        for (FieldDescriptor fieldDescriptor : rowDescriptor) {
            if (!newRow.isInitialized(fieldDescriptor.getPosition()) || fieldDescriptor.isDbKey()) continue;

            if (first) {
                first = false;
            } else {
                columns.append(',');
                params.append(',');
            }

            //noinspection DataFlowIssue : given the check in requireSingleTable, originalName should be non-null
            quoteStrategy.appendQuoted(fieldDescriptor.getOriginalName(), columns);
            params.append('?');
        }

        // 25 = length of appended literals, 32 = guesstimate for (schema +) table
        var sb = new StringBuilder(25 + 32 + columns.length() + params.length())
                .append("insert into ");
        table.append(sb, quoteStrategy)
                .append(" (").append(columns).append(") values (").append(params).append(')');

        return sb.toString();
    }

    private String buildSelectStatement() {
        var columns = new StringBuilder(rowDescriptor.getCount() * EST_COLUMN_SIZE);

        boolean first = true;
        for (FieldDescriptor fieldDescriptor : rowDescriptor) {
            if (first) {
                first = false;
            } else {
                columns.append(',');
            }

            // special handling of RDB$DB_KEY, since Firebird returns DB_KEY column name instead of the correct one
            if (fieldDescriptor.isDbKey()) {
                columns.append("RDB$DB_KEY");
            } else {
                //noinspection DataFlowIssue : given the check in requireSingleTable, originalName should be non-null
                quoteStrategy.appendQuoted(fieldDescriptor.getOriginalName(), columns);
            }
        }

        // 32 = guesstimate for (schema +) table
        var sb = new StringBuilder(EST_STATEMENT_SIZE + columns.length() + 32)
                .append("select ").append(columns).append("\nfrom ");
        table.append(sb, quoteStrategy).append('\n');
        appendWhereClause(sb);
        return sb.toString();
    }

    private enum StatementType {
        UPDATE,
        DELETE,
        INSERT,
        SELECT
    }

    private void modifyRow(StatementType statementType) throws SQLException {
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

    private FbStatement getStatementWithTransaction(StatementType statementType) throws SQLException {
        FbStatement stmt = statements.get(statementType);
        if (stmt == null) {
            stmt = gdsHelper.allocateStatement();
            statements.put(statementType, stmt);
        } else {
            stmt.setTransaction(gdsHelper.getCurrentTransaction());
        }
        return stmt;
    }

    @Override
    public void updateRow() throws SQLException {
        modifyRow(StatementType.UPDATE);
    }

    @Override
    public void deleteRow() throws SQLException {
        modifyRow(StatementType.DELETE);
    }

    @Override
    public void insertRow() throws SQLException {
        modifyRow(StatementType.INSERT);
    }

    @Override
    public void refreshRow() throws SQLException {
        try (LockCloseable ignored = gdsHelper.withLock()) {
            boolean success = false;
            try {
                notifyExecutionStarted();

                FbStatement selectStatement = getStatementWithTransaction(StatementType.SELECT);

                final RowListener rowListener = new RowListener();
                selectStatement.addStatementListener(rowListener);

                try {
                    executeStatement(StatementType.SELECT, selectStatement);

                    // should fetch one row anyway
                    selectStatement.fetchRows(10);

                    List<RowValue> rows = rowListener.getRows();
                    if (rows.isEmpty()) {
                        throw new SQLException("No rows could be fetched.");
                    }

                    if (rows.size() > 1) {
                        throw new SQLException("More then one row fetched.");
                    }

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

    private void executeStatement(StatementType statementType, FbStatement stmt) throws SQLException {
        if (statementType != StatementType.INSERT) {
            if (inInsertRow) {
                throw new SQLException("Only insertRow() is allowed when result set is positioned on insert row.");
            } else if (oldRow == NO_CURRENT_ROW) {
                throw new SQLException("Result set is not positioned on a row.");
            }
        }

        // Flushable field can update the value, which in turn can change the parameter distribution
        flushFields();

        stmt.prepare(generateStatementText(statementType));

        List<byte[]> params = new ArrayList<>(newRow.initializedCount() + keyColumns.size());

        // Set parameters of new values
        if (statementType == StatementType.UPDATE || statementType == StatementType.INSERT) {
            for (FieldDescriptor fieldDescriptor : rowDescriptor) {
                if (newRow.isInitialized(fieldDescriptor.getPosition()) && !fieldDescriptor.isDbKey()) {
                    params.add(newRow.getFieldData(fieldDescriptor.getPosition()));
                }
            }
        }

        // Set parameters of where clause
        if (statementType != StatementType.INSERT) {
            for (FieldDescriptor keyColumn : keyColumns) {
                params.add(oldRow.getFieldData(keyColumn.getPosition()));
            }
        }

        stmt.execute(RowValue.of(params.toArray(EMPTY_2D_BYTES)));
    }

    private void flushFields() throws SQLException {
        for (FBField field : fields) {
            if (field instanceof FBFlushableField flushableField) {
                flushableField.flushCachedData();
            }
        }
    }

    private String generateStatementText(StatementType statementType) {
        return switch (statementType) {
            case UPDATE -> buildUpdateStatement();
            case DELETE -> buildDeleteStatement();
            case INSERT -> buildInsertStatement();
            case SELECT -> buildSelectStatement();
        };
    }

    @Override
    public RowValue getNewRow() throws SQLException {
        if (inInsertRow) {
            throw wrongRow(ROW_UPDATE, ROW_INSERT);
        }

        RowValue newRowCopy = rowDescriptor.createDefaultFieldValues();
        for (int i = 0; i < rowDescriptor.getCount(); i++) {
            byte[] fieldData = getFieldData(i);
            newRowCopy.setFieldData(i, fieldData != null ? fieldData.clone() : null);
        }
        return newRowCopy;
    }

    private byte @Nullable [] getFieldData(int field) {
        RowValue source = newRow.isInitialized(field) || inInsertRow ? newRow : oldRow;
        return source.getFieldData(field);
    }

    @Override
    public RowValue getInsertRow() throws SQLException {
        if (inInsertRow) {
            RowValue newRowCopy = newRow.deepCopy();
            newRowCopy.initializeFields();
            return newRowCopy;
        }
        throw wrongRow(ROW_INSERT, ROW_CURRENT);
    }

    @Override
    public RowValue getOldRow() throws SQLException {
        if (inInsertRow) {
            throw wrongRow(ROW_OLD, ROW_INSERT);
        }
        return oldRow;
    }

    private static SQLException wrongRow(String expectedRow, String actualRow) {
        return new SQLException(
                "Cannot return %s row, currently positioned on %s row".formatted(expectedRow, actualRow));
    }

    @Override
    public void moveToInsertRow() {
        inInsertRow = true;
        newRow.reset();
    }

    @Override
    public void moveToCurrentRow() {
        inInsertRow = false;
        newRow.reset();
    }

    private static final class RowListener implements StatementListener {
        // expect 0 or 1 rows (2 or more would mean the key columns didn't identify a row uniquely)
        private final List<RowValue> rows = new ArrayList<>(1);

        @Override
        public void receivedRow(FbStatement sender, RowValue rowValue) {
            rows.add(rowValue);
        }

        public List<RowValue> getRows() {
            return rows;
        }
    }

    private final class FieldDataProviderImpl implements FieldDataProvider {

        private final int field;

        FieldDataProviderImpl(int field) {
            this.field = field;
        }

        @Override
        public byte @Nullable [] getFieldData() {
            return FBRowUpdater.this.getFieldData(field);
        }

        @Override
        public void setFieldData(byte @Nullable [] data) {
            newRow.setFieldData(field, data);
        }

    }
}