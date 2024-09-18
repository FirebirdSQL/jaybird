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

import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.FbStatement;
import org.firebirdsql.gds.ng.fields.FieldDescriptor;
import org.firebirdsql.gds.ng.fields.RowDescriptor;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.jaybird.props.PropertyConstants;
import org.firebirdsql.jaybird.util.SQLExceptionThrowingFunction;
import org.firebirdsql.jaybird.util.UncheckedSQLException;
import org.firebirdsql.jaybird.util.SQLExceptionChainBuilder;
import org.firebirdsql.jdbc.field.FBCloseableField;
import org.firebirdsql.jdbc.field.FBField;
import org.firebirdsql.jdbc.field.FieldDataProvider;
import org.firebirdsql.jdbc.field.TrimmableField;
import org.firebirdsql.util.InternalApi;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

/**
 * Implementation of {@link ResultSet}.
 * <p>
 * This class is internal API of Jaybird. Future versions may radically change, move, or make inaccessible this type.
 * For the public API, refer to the {@link ResultSet} and {@link FirebirdResultSet} interfaces.
 * </p>
 *
 * @author David Jencks
 * @author Roman Rokytskyy
 * @author Mark Rotteveel
 */
@SuppressWarnings("RedundantThrows")
@InternalApi
public class FBResultSet implements ResultSet, FirebirdResultSet, FBObjectListener.FetcherListener {

    private static final String UNICODE_STREAM_NOT_SUPPORTED = "Unicode stream not supported";
    private static final String TYPE_SQLXML = "SQLXML";

    private final @Nullable AbstractStatement statement;
    private final @NonNull FBFetcher fbFetcher;
    private @Nullable FirebirdRowUpdater rowUpdater;

    protected final @Nullable FBConnection connection;
    protected final @Nullable GDSHelper gdsHelper;

    protected final @NonNull RowDescriptor rowDescriptor;

    protected @Nullable RowValue row;

    private boolean wasNull;

    private final @NonNull FBField @NonNull [] fields;
    private final @NonNull List<@NonNull FBCloseableField> closeableFields;
    private final @NonNull Map<String, Integer> colNames;

    private final @Nullable String cursorName;
    private final FBObjectListener.@NonNull ResultSetListener listener;

    @Override
    public void rowChanged(@NonNull FBFetcher fetcher, @Nullable RowValue newRow) throws SQLException {
        this.row = newRow;
    }

    /**
     * Creates a new {@code FBResultSet} instance.
     */
    @SuppressWarnings("java:S1141")
    public FBResultSet(@NonNull AbstractStatement statement, FBObjectListener.@Nullable ResultSetListener listener,
            boolean metaDataQuery) throws SQLException {
        this.statement = requireNonNull(statement, "statement");
        FbStatement stmt = requireNonNull(statement.getStatementHandle(), "statement.statementHandle");
        try {
            connection = requireNonNull(statement.getConnection(), "statement.connection");
            gdsHelper = connection.getGDSHelper();
            cursorName = statement.getCursorName();
            this.listener = listener != null ? listener : FBObjectListener.NoActionResultSetListener.instance();
            rowDescriptor = stmt.getRowDescriptor();

            FetchConfig fetchConfig = statement.fetchConfig();
            ResultSetBehavior behavior = fetchConfig.resultSetBehavior();
            boolean serverSideScrollable =
                    behavior.isScrollable() && behavior.isCloseCursorsAtCommit() && !metaDataQuery
                    && connection.isScrollableCursor(PropertyConstants.SCROLLABLE_CURSOR_SERVER)
                    && stmt.supportsFetchScroll();
            boolean cached = metaDataQuery || behavior.isScrollable() && !serverSideScrollable;

            fields = createFields(cached, metaDataQuery);
            closeableFields = toCloseableFields(fields);
            colNames = new HashMap<>(rowDescriptor.getCount(), 1);
            FBFetcher fbFetcher;
            if (cached) {
                fbFetcher = new FBCachedFetcher(gdsHelper, fetchConfig, stmt, this);
                if (behavior.isForwardOnly()) {
                    fbFetcher = new ForwardOnlyFetcherDecorator(fbFetcher);
                }
            } else if (serverSideScrollable) {
                fbFetcher = new FBServerScrollFetcher(fetchConfig, stmt, this);
            } else if (statement.getCursorName() != null) {
                fbFetcher = new FBUpdatableCursorFetcher(gdsHelper, fetchConfig, stmt, this);
            } else {
                fbFetcher = new FBStatementFetcher(gdsHelper, fetchConfig, stmt, this);
            }

            if (behavior.isUpdatable()) {
                try {
                    rowUpdater = new FBRowUpdater(connection, rowDescriptor, cached, listener);
                    if (fbFetcher instanceof FBServerScrollFetcher) {
                        fbFetcher = new FBUpdatableFetcher(fbFetcher, this, rowDescriptor.createDeletedRowMarker());
                    }
                } catch (FBResultSetNotUpdatableException ex) {
                    statement.addWarning(FbExceptionBuilder
                            .forWarning(JaybirdErrorCodes.jb_concurrencyResetReadOnlyReasonNotUpdatable)
                            .toSQLException(SQLWarning.class));
                    fbFetcher.setReadOnly();
                }
            }
            this.fbFetcher = fbFetcher;
        } catch (SQLException e) {
            try {
                // Ensure cursor is closed to avoid problems with statement reuse
                stmt.closeCursor();
            } catch (SQLException e2) {
                e.addSuppressed(e2);
            }
            throw e;
        }
    }

    /**
     * Creates a FBResultSet with the columns specified by {@code rowDescriptor} and the data in {@code rows}.
     * <p>
     * This constructor is intended for metadata result sets, but can be used for other purposes as well.
     * </p>
     * <p>
     * Current implementation will ensure that strings will be trimmed on retrieval.
     * </p>
     *
     * @param rowDescriptor
     *         column definition
     * @param rows
     *         row data
     */
    public FBResultSet(@NonNull RowDescriptor rowDescriptor, @NonNull List<@NonNull RowValue> rows)
            throws SQLException {
        this(rowDescriptor, null, rows, null, false);
    }

    /**
     * Creates a FBResultSet with the columns specified by {@code rowDescriptor} and the data in {@code rows}.
     * <p>
     * Current implementation will ensure that strings will be trimmed on retrieval.
     * </p>
     *
     * @param rowDescriptor
     *         column definition
     * @param connection
     *         connection (cannot be {@code null} when {@code retrieveBlobs} is {@code true}
     * @param rows
     *         row data
     * @param listener
     *         result set listener
     * @param retrieveBlobs
     *         {@code true} retrieves the blob data
     * @since 5.0.1
     */
    public FBResultSet(@NonNull RowDescriptor rowDescriptor, @Nullable FBConnection connection,
            @NonNull List<@NonNull RowValue> rows, FBObjectListener.@Nullable ResultSetListener listener,
            boolean retrieveBlobs) throws SQLException {
        // TODO Evaluate if we need to share more implementation with constructor above
        this.connection = connection;
        gdsHelper = connection != null ? connection.getGDSHelper() : null;
        statement = null;
        this.listener = listener != null ? listener : FBObjectListener.NoActionResultSetListener.instance();
        cursorName = null;
        // TODO Set specific result set types (see also previous todo)
        var fetchConfig = new FetchConfig(ResultSetBehavior.of());
        fbFetcher = new FBCachedFetcher(rows, fetchConfig, this, rowDescriptor, gdsHelper, retrieveBlobs);
        this.rowDescriptor = rowDescriptor;
        fields = createFields(true, false);
        closeableFields = toCloseableFields(fields);
        colNames = new HashMap<>(rowDescriptor.getCount(), 1);
    }

    private @NonNull FBField @NonNull [] createFields(boolean cached, boolean trimStrings) throws SQLException {
        int fieldCount = rowDescriptor.getCount();
        var fields = new FBField[fieldCount];
        for (int i = 0; i < fieldCount; i++) {
            fields[i] = FBField.createField(rowDescriptor.getFieldDescriptor(i), new DataProvider(i), gdsHelper, cached);
            if (trimStrings && fields[i] instanceof TrimmableField trimmableField) {
                trimmableField.setTrimTrailing(true);
            }
        }
        return fields;
    }

    private static @NonNull List<@NonNull FBCloseableField> toCloseableFields(@NonNull FBField @NonNull [] fields) {
        return Arrays.stream(fields)
                .filter(FBCloseableField.class::isInstance)
                .map(FBCloseableField.class::cast)
                .toList();
    }

    /**
     * Notify the row updater about the new row that was fetched. This method
     * must be called after each change in cursor position.
     */
    private void notifyRowUpdater() throws SQLException {
        if (rowUpdater != null) {
            rowUpdater.setRow(row);
        }
    }

    /**
     * Check if statement is open and prepare statement for cursor move.
     *
     * @throws SQLException
     *         if statement is closed.
     */
    protected void checkCursorMove() throws SQLException {
        checkOpen();
        closeFields();
    }

    /**
     * Check if ResultSet is open.
     *
     * @throws SQLException
     *         if ResultSet is closed.
     */
    protected void checkOpen() throws SQLException {
        if (isClosed()) {
            throw new SQLException("The result set is closed", SQLStateConstants.SQL_STATE_INVALID_CURSOR_STATE);
        }
    }

    /**
     * Checks if the result set is scrollable
     *
     * @throws SQLException
     *         if ResultSet is not scrollable
     */
    protected void checkScrollable() throws SQLException {
        if (behavior().isForwardOnly()) {
            throw FbExceptionBuilder.forNonTransientException(JaybirdErrorCodes.jb_operationNotAllowedOnForwardOnly)
                    .toSQLException();
        }
    }

    /**
     * Close the fields if they were open (applies mainly to the stream fields).
     *
     * @throws SQLException
     *         if something wrong happened.
     */
    protected void closeFields() throws SQLException {
        // TODO See if we can apply completion reason logic (e.g. no need to close blob on commit)
        wasNull = false;
        // if there are no fields to close, then nothing else to do
        if (closeableFields.isEmpty()) return;

        var chain = new SQLExceptionChainBuilder();
        // close current fields, so that resources are freed.
        for (final FBCloseableField field : closeableFields) {
            try {
                field.close();
            } catch (SQLException ex) {
                chain.append(ex);
            }
        }

        if (chain.hasException()) {
            throw chain.getException();
        }
    }

    @Override
    public boolean next() throws SQLException {
        checkCursorMove();
        boolean result = fbFetcher.next();

        if (result)
            notifyRowUpdater();

        return result;
    }

    @Override
    public void close() throws SQLException {
        close(true, CompletionReason.OTHER);
    }

    @Override
    public boolean isClosed() throws SQLException {
        return fbFetcher.isClosed();
    }

    void close(boolean notifyListener, @NonNull CompletionReason completionReason) throws SQLException {
        if (isClosed()) return;
        var chain = new SQLExceptionChainBuilder();

        try {
            closeFields();
        } catch (SQLException ex) {
            chain.append(ex);
        } finally {
            try {
                try {
                    fbFetcher.close(completionReason);
                } catch (SQLException ex) {
                    chain.append(ex);
                }

                if (rowUpdater != null) {
                    try {
                        rowUpdater.close();
                    } catch (SQLException ex) {
                        chain.append(ex);
                    }
                }

                if (notifyListener) {
                    try {
                        listener.resultSetClosed(this);
                    } catch (SQLException ex) {
                        chain.append(ex);
                    }
                }
            } finally {
                rowUpdater = null;
            }
        }

        if (chain.hasException()) {
            throw chain.getException();
        }
    }

    @Override
    public boolean wasNull() throws SQLException {
        checkOpen();
        return wasNull;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: works identical to {@link #getBinaryStream(int)}.
     * </p>
     */
    @Override
    public final InputStream getAsciiStream(int columnIndex) throws SQLException {
        return getBinaryStream(columnIndex);
    }

    @Override
    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        return getField(columnIndex).getBigDecimal();
    }

    @Override
    public InputStream getBinaryStream(int columnIndex) throws SQLException {
        return getField(columnIndex).getBinaryStream();
    }

    @Override
    public Blob getBlob(int columnIndex) throws SQLException {
        return getField(columnIndex).getBlob();
    }

    @Override
    public boolean getBoolean(int columnIndex) throws SQLException {
        return getField(columnIndex).getBoolean();
    }

    @Override
    public byte getByte(int columnIndex) throws SQLException {
        return getField(columnIndex).getByte();
    }

    @Override
    public byte[] getBytes(int columnIndex) throws SQLException {
        return getField(columnIndex).getBytes();
    }

    @Override
    public Date getDate(int columnIndex) throws SQLException {
        return getField(columnIndex).getDate();
    }

    @Override
    public double getDouble(int columnIndex) throws SQLException {
        return getField(columnIndex).getDouble();
    }

    @Override
    public float getFloat(int columnIndex) throws SQLException {
        return getField(columnIndex).getFloat();
    }

    @Override
    public int getInt(int columnIndex) throws SQLException {
        return getField(columnIndex).getInt();
    }

    @Override
    public long getLong(int columnIndex) throws SQLException {
        return getField(columnIndex).getLong();
    }

    @Override
    public Object getObject(int columnIndex) throws SQLException {
        return getField(columnIndex).getObject();
    }

    @Override
    public short getShort(int columnIndex) throws SQLException {
        return getField(columnIndex).getShort();
    }

    @Override
    public String getString(int columnIndex) throws SQLException {
        return getField(columnIndex).getString();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: This method behaves exactly the same as {@link #getString(int)}.
     * </p>
     */
    @Override
    public String getNString(int columnIndex) throws SQLException {
        return getString(columnIndex);
    }

    @Override
    public Time getTime(int columnIndex) throws SQLException {
        return getField(columnIndex).getTime();
    }

    @Override
    public Timestamp getTimestamp(int columnIndex) throws SQLException {
        return getField(columnIndex).getTimestamp();
    }

    /**
     * Method is no longer supported since Jaybird 3.0.
     * <p>
     * For old behavior use {@link #getBinaryStream(int)}. For JDBC suggested behavior,
     * use {@link #getCharacterStream(int)}.
     * </p>
     *
     * @throws SQLFeatureNotSupportedException
     *         Always
     * @deprecated
     */
    @Deprecated(since = "1")
    public InputStream getUnicodeStream(int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException(UNICODE_STREAM_NOT_SUPPORTED);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: This method behaves exactly the same as {@link #getCharacterStream(int)}.
     * </p>
     */
    @Override
    public Reader getNCharacterStream(int columnIndex) throws SQLException {
        return getCharacterStream(columnIndex);
    }

    /**
     * Get the {@code FBField} object at the given column index
     *
     * @param columnIndex
     *         The index of the parameter, 1 is the first index
     * @throws SQLException
     *         If there is an error accessing the field
     */
    public @NonNull FBField getField(int columnIndex) throws SQLException {
        FBField field = getField(columnIndex, true);
        wasNull = field.isNull();
        return field;
    }

    /**
     * Factory method for the field access objects
     */
    public @NonNull FBField getField(int columnIndex, boolean checkRowPosition) throws SQLException {
        checkOpen();

        if (checkRowPosition && row == null && rowUpdater == null) {
            throw new SQLException("The result set is not in a row, use next", SQLStateConstants.SQL_STATE_NO_ROW_AVAIL);
        }

        if (columnIndex > rowDescriptor.getCount()) {
            throw new SQLException("Invalid column index: " + columnIndex,
                    SQLStateConstants.SQL_STATE_INVALID_DESC_FIELD_ID);
        }

        return rowUpdater != null ? rowUpdater.getField(columnIndex - 1) : fields[columnIndex - 1];
    }

    /**
     * Get a {@code FBField} by name.
     *
     * @param columnName
     *         The name of the field to be retrieved
     * @throws SQLException
     *         if the field cannot be retrieved
     */
    public FBField getField(String columnName) throws SQLException {
        try {
            int fieldNum = colNames.computeIfAbsent(columnName,
                    SQLExceptionThrowingFunction.toFunction(this::findColumn));
            return getField(fieldNum);
        } catch (UncheckedSQLException e) {
            throw e.getCause();
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: ignores {@code scale} and behaves identical to {@link #getBigDecimal(int)}.
     * </p>
     */
    @Deprecated(since = "1")
    @Override
    public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
        return getField(columnIndex).getBigDecimal(scale);
    }

    @Override
    public String getString(String columnName) throws SQLException {
        return getField(columnName).getString();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: This method behaves exactly the same as {@link #getString(String)}.
     * </p>
     */
    @Override
    public String getNString(String columnLabel) throws SQLException {
        return getString(columnLabel);
    }

    @Override
    public boolean getBoolean(String columnName) throws SQLException {
        return getField(columnName).getBoolean();
    }

    @Override
    public byte getByte(String columnName) throws SQLException {
        return getField(columnName).getByte();
    }

    @Override
    public short getShort(String columnName) throws SQLException {
        return getField(columnName).getShort();
    }

    @Override
    public int getInt(String columnName) throws SQLException {
        return getField(columnName).getInt();
    }

    @Override
    public long getLong(String columnName) throws SQLException {
        return getField(columnName).getLong();
    }

    @Override
    public float getFloat(String columnName) throws SQLException {
        return getField(columnName).getFloat();
    }

    @Override
    public double getDouble(String columnName) throws SQLException {
        return getField(columnName).getDouble();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: ignores {@code scale} and behaves identical to {@link #getBigDecimal(String)}.
     * </p>
     */
    @Deprecated(since = "1")
    @Override
    public BigDecimal getBigDecimal(String columnName, int scale) throws SQLException {
        return getField(columnName).getBigDecimal(scale);
    }

    @Override
    public byte[] getBytes(String columnName) throws SQLException {
        return getField(columnName).getBytes();
    }

    @Override
    public Date getDate(String columnName) throws SQLException {
        return getField(columnName).getDate();
    }

    @Override
    public Time getTime(String columnName) throws SQLException {
        return getField(columnName).getTime();
    }

    @Override
    public Timestamp getTimestamp(String columnName) throws SQLException {
        return getField(columnName).getTimestamp();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: works identical to {@link #getBinaryStream(String)}.
     * </p>
     */
    @Override
    public final InputStream getAsciiStream(String columnName) throws SQLException {
        return getBinaryStream(columnName);
    }

    /**
     * Method is no longer supported since Jaybird 3.0.
     * <p>
     * For old behavior use {@link #getBinaryStream(String)}. For JDBC suggested behavior,
     * use {@link #getCharacterStream(String)}.
     * </p>
     *
     * @throws SQLFeatureNotSupportedException
     *         Always
     * @deprecated
     */
    @Deprecated(since = "1")
    @Override
    public InputStream getUnicodeStream(String columnName) throws SQLException {
        throw new SQLFeatureNotSupportedException(UNICODE_STREAM_NOT_SUPPORTED);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: This method behaves exactly the same as {@link #getCharacterStream(String)}.
     * </p>
     */
    @Override
    public Reader getNCharacterStream(String columnLabel) throws SQLException {
        return getCharacterStream(columnLabel);
    }

    @Override
    public InputStream getBinaryStream(String columnName) throws SQLException {
        return getField(columnName).getBinaryStream();
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>NOTE:</b> The implementation currently always returns {@code null} as warnings are never recorded for result
     * sets.
     * </p>
     */
    @Override
    public @Nullable SQLWarning getWarnings() throws SQLException {
        // Warnings are never recorded
        return null;
    }

    @Override
    public void clearWarnings() throws SQLException {
        // nothing to do
    }

    @Override
    public @Nullable String getCursorName() throws SQLException {
        return cursorName;
    }

    @Override
    public @NonNull ResultSetMetaData getMetaData() throws SQLException {
        checkOpen();
        return new FBResultSetMetaData(rowDescriptor, connection);
    }

    @Override
    public Object getObject(String columnName) throws SQLException {
        return getField(columnName).getObject();
    }

    // See section 14.2.3 of jdbc-3.0 specification
    // "Column names supplied to getter methods are case-insensitive
    // If a select list contains the same column more than once, 
    // the first instance of the column will be returned"
    @Override
    public int findColumn(String columnName) throws SQLException {
        requireNonEmpty(columnName);
        Predicate<String> columnNamePredicate;
        if (columnName.startsWith("\"") && columnName.endsWith("\"")) {
            String caseSensitiveColumnName = columnName.substring(1, columnName.length() - 1);
            requireNonEmpty(caseSensitiveColumnName);
            // case-sensitively check columns
            columnNamePredicate = caseSensitiveColumnName::equals;
        } else {
            // case-insensitively check columns
            columnNamePredicate = columnName::equalsIgnoreCase;
        }

        OptionalInt position = findColumn(columnNamePredicate);
        if (position.isPresent()) return position.getAsInt();

        if (columnNamePredicate.test("RDB$DB_KEY")) {
            // Fix up: RDB$DB_KEY is identified as DB_KEY in the result set
            OptionalInt dbKeyPosition = findColumn("DB_KEY"::equals);
            if (dbKeyPosition.isPresent()) return dbKeyPosition.getAsInt();
        }

        throw new SQLException("Column name " + columnName + " not found in result set",
                SQLStateConstants.SQL_STATE_INVALID_DESC_FIELD_ID);
    }

    private static void requireNonEmpty(String columnName) throws SQLException {
        if (columnName == null || columnName.isEmpty()) {
            throw new SQLException("Empty string or null does not identify a column",
                    SQLStateConstants.SQL_STATE_INVALID_DESC_FIELD_ID);
        }
    }

    private @NonNull OptionalInt findColumn(@NonNull Predicate<String> columnNamePredicate) {
        // Check labels (aliases) first
        OptionalInt position = findColumn(columnNamePredicate, FieldDescriptor::getFieldName);
        if (position.isPresent()) return position;
        // then check underlying column names
        return findColumn(columnNamePredicate, FieldDescriptor::getOriginalName);
    }

    private @NonNull OptionalInt findColumn(@NonNull Predicate<String> columnNamePredicate,
            Function<FieldDescriptor, String> columnNameAccessor) {
        for (int i = 0; i < rowDescriptor.getCount(); i++) {
            if (columnNamePredicate.test(columnNameAccessor.apply(rowDescriptor.getFieldDescriptor(i)))) {
                return OptionalInt.of(i + 1);
            }
        }
        return OptionalInt.empty();
    }

    @Override
    public Reader getCharacterStream(int columnIndex) throws SQLException {
        return getField(columnIndex).getCharacterStream();
    }

    @Override
    public Reader getCharacterStream(String columnName) throws SQLException {
        return getField(columnName).getCharacterStream();
    }

    @Override
    public BigDecimal getBigDecimal(String columnName) throws SQLException {
        return getField(columnName).getBigDecimal();
    }

    @Override
    public boolean isBeforeFirst() throws SQLException {
        checkOpen();
        return !fbFetcher.isEmpty() && fbFetcher.isBeforeFirst();
    }

    @Override
    public boolean isAfterLast() throws SQLException {
        checkOpen();
        return !fbFetcher.isEmpty() && fbFetcher.isAfterLast();
    }

    @Override
    public boolean isFirst() throws SQLException {
        checkOpen();
        return fbFetcher.isFirst();
    }

    @Override
    public boolean isLast() throws SQLException {
        checkOpen();
        return fbFetcher.isLast();
    }

    @Override
    public void beforeFirst() throws SQLException {
        checkCursorMove();
        fbFetcher.beforeFirst();
        notifyRowUpdater();
    }

    @Override
    public void afterLast() throws SQLException {
        checkCursorMove();
        fbFetcher.afterLast();
        notifyRowUpdater();
    }

    @Override
    public boolean first() throws SQLException {
        checkCursorMove();
        boolean result = fbFetcher.first();
        if (result)
            notifyRowUpdater();
        return result;
    }

    @Override
    public boolean last() throws SQLException {
        checkCursorMove();
        boolean result = fbFetcher.last();
        if (result)
            notifyRowUpdater();
        return result;
    }

    @Override
    public int getRow() throws SQLException {
        checkOpen();
        return fbFetcher.getRowNum();
    }

    @Override
    public boolean absolute(int row) throws SQLException {
        checkCursorMove();
        boolean result = fbFetcher.absolute(row);
        if (result)
            notifyRowUpdater();
        return result;
    }

    @Override
    public boolean relative(int rows) throws SQLException {
        checkCursorMove();
        boolean result = fbFetcher.relative(rows);
        if (result)
            notifyRowUpdater();
        return result;
    }

    @Override
    public boolean previous() throws SQLException {
        checkCursorMove();
        boolean result = fbFetcher.previous();
        if (result)
            notifyRowUpdater();
        return result;
    }

    private ResultSetBehavior behavior() {
        return fbFetcher.getFetchConfig().resultSetBehavior();
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
        if (direction == ResultSet.FETCH_REVERSE || direction == ResultSet.FETCH_UNKNOWN) {
            checkScrollable();
        }
        fbFetcher.setFetchDirection(direction);
    }

    @SuppressWarnings("MagicConstant")
    @Override
    public int getFetchDirection() throws SQLException {
        return fbFetcher.getFetchDirection();
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {
        fbFetcher.setFetchSize(rows);
    }

    @Override
    public int getFetchSize() throws SQLException {
        return fbFetcher.getFetchSize();
    }

    @SuppressWarnings("MagicConstant")
    @Override
    public int getType() throws SQLException {
        checkOpen();
        return behavior().type();
    }

    @SuppressWarnings("MagicConstant")
    @Override
    public int getConcurrency() throws SQLException {
        checkOpen();
        return behavior().concurrency();
    }

    @SuppressWarnings("MagicConstant")
    @Override
    public int getHoldability() throws SQLException {
        checkOpen();
        return behavior().holdability();
    }

    @Override
    public boolean rowUpdated() throws SQLException {
        checkUpdatable();
        return fbFetcher.rowUpdated();
    }

    @Override
    public boolean rowInserted() throws SQLException {
        checkUpdatable();
        return fbFetcher.rowInserted();
    }

    @Override
    public boolean rowDeleted() throws SQLException {
        checkUpdatable();
        return fbFetcher.rowDeleted();
    }

    /**
     * Checks if the result set is updatable, throwing {@link FBResultSetNotUpdatableException} otherwise.
     *
     * @throws FBResultSetNotUpdatableException
     *         when this result set is not updatable
     * @see #requireRowUpdater()
     */
    private void checkUpdatable() throws SQLException {
        checkOpen();
        if (rowUpdater == null) {
            throw new FBResultSetNotUpdatableException();
        }
    }

    /**
     * Checks if the result set is updatable, returning the row updater, throwing
     * {@link FBResultSetNotUpdatableException} otherwise.
     *
     * @return row updater
     * @throws FBResultSetNotUpdatableException
     *         when this result set is not updatable
     * @see #checkUpdatable()
     */
    private @NonNull FirebirdRowUpdater requireRowUpdater() throws SQLException {
        checkOpen();
        FirebirdRowUpdater rowUpdater = this.rowUpdater;
        if (rowUpdater == null) {
            throw new FBResultSetNotUpdatableException();
        }
        return rowUpdater;
    }

    @Override
    public void updateNull(int columnIndex) throws SQLException {
        checkUpdatable();
        getField(columnIndex).setNull();
    }

    @Override
    public void updateBoolean(int columnIndex, boolean x) throws SQLException {
        checkUpdatable();
        getField(columnIndex).setBoolean(x);
    }

    @Override
    public void updateByte(int columnIndex, byte x) throws SQLException {
        checkUpdatable();
        getField(columnIndex).setByte(x);
    }

    @Override
    public void updateShort(int columnIndex, short x) throws SQLException {
        checkUpdatable();
        getField(columnIndex).setShort(x);
    }

    @Override
    public void updateInt(int columnIndex, int x) throws SQLException {
        checkUpdatable();
        getField(columnIndex).setInteger(x);
    }

    @Override
    public void updateLong(int columnIndex, long x) throws SQLException {
        checkUpdatable();
        getField(columnIndex).setLong(x);
    }

    @Override
    public void updateFloat(int columnIndex, float x) throws SQLException {
        checkUpdatable();
        getField(columnIndex).setFloat(x);
    }

    @Override
    public void updateDouble(int columnIndex, double x) throws SQLException {
        checkUpdatable();
        getField(columnIndex).setDouble(x);
    }

    @Override
    public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
        checkUpdatable();
        getField(columnIndex).setBigDecimal(x);
    }

    @Override
    public void updateString(int columnIndex, String x) throws SQLException {
        checkUpdatable();
        getField(columnIndex).setString(x);
    }

    @Override
    public void updateBytes(int columnIndex, byte[] x) throws SQLException {
        checkUpdatable();
        getField(columnIndex).setBytes(x);
    }

    @Override
    public void updateDate(int columnIndex, Date x) throws SQLException {
        checkUpdatable();
        getField(columnIndex).setDate(x);
    }

    @Override
    public void updateTime(int columnIndex, Time x) throws SQLException {
        checkUpdatable();
        getField(columnIndex).setTime(x);
    }

    @Override
    public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
        checkUpdatable();
        getField(columnIndex).setTimestamp(x);
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
        checkUpdatable();
        getField(columnIndex).setBinaryStream(x, length);
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
        checkUpdatable();
        getField(columnIndex).setBinaryStream(x, length);
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
        checkUpdatable();
        getField(columnIndex).setBinaryStream(x);
    }

    @Override
    public void updateBinaryStream(String columnName, InputStream x, int length) throws SQLException {
        checkUpdatable();
        getField(columnName).setBinaryStream(x, length);
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {
        checkUpdatable();
        getField(columnLabel).setBinaryStream(x, length);
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {
        checkUpdatable();
        getField(columnLabel).setBinaryStream(x);
    }

    @Override
    public void updateObject(int columnIndex, Object x, int scale) throws SQLException {
        updateObject(columnIndex, x);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Jaybird delegates to {@link #updateObject(int, Object, int)} and ignores the value of {@code targetSqlType}
     * </p>
     */
    @Override
    public void updateObject(int columnIndex, Object x, SQLType targetSqlType, int scaleOrLength) throws SQLException {
        updateObject(columnIndex, x, scaleOrLength);
    }

    @Override
    public void updateObject(int columnIndex, Object x) throws SQLException {
        checkUpdatable();
        getField(columnIndex).setObject(x);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Jaybird delegates to {@link #updateObject(int, Object)} and ignores the value of {@code targetSqlType}
     * </p>
     */
    @Override
    public void updateObject(int columnIndex, Object x, SQLType targetSqlType) throws SQLException {
        updateObject(columnIndex, x);
    }

    @Override
    public void updateNull(String columnName) throws SQLException {
        checkUpdatable();
        getField(columnName).setNull();
    }

    @Override
    public void updateBoolean(String columnName, boolean x) throws SQLException {
        checkUpdatable();
        getField(columnName).setBoolean(x);
    }

    @Override
    public void updateByte(String columnName, byte x) throws SQLException {
        checkUpdatable();
        getField(columnName).setByte(x);
    }

    @Override
    public void updateShort(String columnName, short x) throws SQLException {
        checkUpdatable();
        getField(columnName).setShort(x);
    }

    @Override
    public void updateInt(String columnName, int x) throws SQLException {
        checkUpdatable();
        getField(columnName).setInteger(x);
    }

    @Override
    public void updateLong(String columnName, long x) throws SQLException {
        checkUpdatable();
        getField(columnName).setLong(x);
    }

    @Override
    public void updateFloat(String columnName, float x) throws SQLException {
        checkUpdatable();
        getField(columnName).setFloat(x);
    }

    @Override
    public void updateDouble(String columnName, double x) throws SQLException {
        checkUpdatable();
        getField(columnName).setDouble(x);
    }

    @Override
    public void updateBigDecimal(String columnName, BigDecimal x) throws SQLException {
        checkUpdatable();
        getField(columnName).setBigDecimal(x);
    }

    @Override
    public void updateString(String columnName, String x) throws SQLException {
        checkUpdatable();
        getField(columnName).setString(x);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: This method behaves exactly the same as {@link #updateString(int, String)}.
     * </p>
     */
    @Override
    public void updateNString(int columnIndex, String string) throws SQLException {
        updateString(columnIndex, string);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: This method behaves exactly the same as {@link #updateString(String, String)}.
     * </p>
     */
    @Override
    public void updateNString(String columnLabel, String string) throws SQLException {
        updateString(columnLabel, string);
    }

    @Override
    public void updateBytes(String columnName, byte[] x) throws SQLException {
        checkUpdatable();
        getField(columnName).setBytes(x);
    }

    @Override
    public void updateDate(String columnName, Date x) throws SQLException {
        checkUpdatable();
        getField(columnName).setDate(x);
    }

    @Override
    public void updateTime(String columnName, Time x) throws SQLException {
        checkUpdatable();
        getField(columnName).setTime(x);
    }

    @Override
    public void updateTimestamp(String columnName, Timestamp x) throws SQLException {
        checkUpdatable();
        getField(columnName).setTimestamp(x);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: works identical to {@link #updateBinaryStream(int, InputStream, int)}.
     * </p>
     */
    @Override
    public final void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
        updateBinaryStream(columnIndex, x, length);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: works identical to {@link #updateBinaryStream(String, InputStream, int)}.
     * </p>
     */
    @Override
    public final void updateAsciiStream(String columnName, InputStream x, int length) throws SQLException {
        updateBinaryStream(columnName, x, length);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: works identical to {@link #updateBinaryStream(int, InputStream, long)}.
     * </p>
     */
    @Override
    public final void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {
        updateBinaryStream(columnIndex, x, length);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: works identical to {@link #updateBinaryStream(int, InputStream)}.
     * </p>
     */
    @Override
    public final void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
        updateBinaryStream(columnIndex, x);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: works identical to {@link #updateBinaryStream(String, InputStream, long)}.
     * </p>
     */
    @Override
    public final void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {
        updateBinaryStream(columnLabel, x, length);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: works identical to {@link #updateBinaryStream(String, InputStream)}.
     * </p>
     */
    @Override
    public final void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {
        updateBinaryStream(columnLabel, x);
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
        checkUpdatable();
        getField(columnIndex).setCharacterStream(x, length);
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
        checkUpdatable();
        getField(columnIndex).setCharacterStream(x, length);
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {
        checkUpdatable();
        getField(columnIndex).setCharacterStream(x);
    }

    @Override
    public void updateCharacterStream(String columnName, Reader reader, int length) throws SQLException {
        checkUpdatable();
        getField(columnName).setCharacterStream(reader, length);
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
        checkUpdatable();
        getField(columnLabel).setCharacterStream(reader, length);
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {
        checkUpdatable();
        getField(columnLabel).setCharacterStream(reader);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: This method behaves exactly the same as {@link #updateCharacterStream(int, Reader, long)}.
     * </p>
     */
    @Override
    public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
        updateCharacterStream(columnIndex, x, length);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: This method behaves exactly the same as {@link #updateCharacterStream(int, Reader)}.
     * </p>
     */
    @Override
    public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {
        updateCharacterStream(columnIndex, x);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: This method behaves exactly the same as {@link #updateClob(String, Reader, long)}.
     * </p>
     */
    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
        updateCharacterStream(columnLabel, reader, length);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: This method behaves exactly the same as {@link #updateCharacterStream(String, Reader)}.
     * </p>
     */
    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {
        updateCharacterStream(columnLabel, reader);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: This method behaves exactly the same as {@link #updateObject(String, Object)}.
     * </p>
     */
    @Override
    public void updateObject(String columnName, Object x, int scale) throws SQLException {
        updateObject(columnName, x);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Jaybird delegates to {@link #updateObject(String, Object, int)} and ignores the value of {@code targetSqlType}
     * </p>
     */
    @Override
    public void updateObject(String columnLabel, Object x, SQLType targetSqlType, int scaleOrLength) throws SQLException {
        updateObject(columnLabel, x, scaleOrLength);
    }

    @Override
    public void updateObject(String columnName, Object x) throws SQLException {
        checkUpdatable();
        getField(columnName).setObject(x);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Jaybird delegates to {@link #updateObject(String, Object)} and ignores the value of {@code targetSqlType}
     * </p>
     */
    @Override
    public void updateObject(String columnLabel, Object x, SQLType targetSqlType) throws SQLException {
        updateObject(columnLabel, x);
    }

    @Override
    public void insertRow() throws SQLException {
        FirebirdRowUpdater rowUpdater = requireRowUpdater();
        rowUpdater.insertRow();
        fbFetcher.insertRow(rowUpdater.getInsertRow());
        notifyRowUpdater();
    }

    @Override
    public void updateRow() throws SQLException {
        FirebirdRowUpdater rowUpdater = requireRowUpdater();
        rowUpdater.updateRow();
        fbFetcher.updateRow(rowUpdater.getNewRow());
        notifyRowUpdater();
    }

    @Override
    public void deleteRow() throws SQLException {
        requireRowUpdater().deleteRow();
        fbFetcher.deleteRow();
        notifyRowUpdater();
    }

    @Override
    public void refreshRow() throws SQLException {
        FirebirdRowUpdater rowUpdater = requireRowUpdater();
        rowUpdater.refreshRow();
        fbFetcher.updateRow(rowUpdater.getOldRow());
        // this is excessive, but we do this to keep the code uniform
        notifyRowUpdater();
    }

    @Override
    public void cancelRowUpdates() throws SQLException {
        requireRowUpdater().cancelRowUpdates();
    }

    @Override
    public void moveToInsertRow() throws SQLException {
        requireRowUpdater().moveToInsertRow();
    }

    @Override
    public void moveToCurrentRow() throws SQLException {
        requireRowUpdater().moveToCurrentRow();
    }

    @Override
    public @Nullable Statement getStatement() {
        return statement;
    }

    @Override
    public Object getObject(int i, Map<String, Class<?>> map) throws SQLException {
        return getField(i).getObject(map);
    }

    @Override
    public Ref getRef(int i) throws SQLException {
        return getField(i).getRef();
    }

    @Override
    public Clob getClob(int i) throws SQLException {
        return getField(i).getClob();
    }

    @Override
    public Array getArray(int i) throws SQLException {
        return getField(i).getArray();
    }

    @Override
    public Object getObject(String columnName, Map<String, Class<?>> map) throws SQLException {
        return getField(columnName).getObject(map);
    }

    @Override
    public Ref getRef(String columnName) throws SQLException {
        return getField(columnName).getRef();
    }

    @Override
    public Blob getBlob(String columnName) throws SQLException {
        return getField(columnName).getBlob();
    }

    @Override
    public Clob getClob(String columnName) throws SQLException {
        return getField(columnName).getClob();
    }

    @Override
    public Array getArray(String columnName) throws SQLException {
        return getField(columnName).getArray();
    }

    @Override
    public Date getDate(int columnIndex, Calendar cal) throws SQLException {
        return getField(columnIndex).getDate(cal);
    }

    @Override
    public Date getDate(String columnName, Calendar cal) throws SQLException {
        return getField(columnName).getDate(cal);
    }

    @Override
    public Time getTime(int columnIndex, Calendar cal) throws SQLException {
        return getField(columnIndex).getTime(cal);
    }

    @Override
    public Time getTime(String columnName, Calendar cal) throws SQLException {
        return getField(columnName).getTime(cal);
    }

    @Override
    public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
        return getField(columnIndex).getTimestamp(cal);
    }

    @Override
    public Timestamp getTimestamp(String columnName, Calendar cal) throws SQLException {
        return getField(columnName).getTimestamp(cal);
    }

    @Override
    public URL getURL(int param1) throws SQLException {
        throw typeNotSupported("URL");
    }

    @Override
    public URL getURL(String param1) throws SQLException {
        throw typeNotSupported("URL");
    }

    @Override
    public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
        return getField(columnIndex).getObject(type);
    }

    @Override
    public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
        return getField(columnLabel).getObject(type);
    }

    @Override
    public void updateRef(int param1, Ref param2) throws SQLException {
        throw typeNotSupported("REF");
    }

    @Override
    public void updateRef(String param1, Ref param2) throws SQLException {
        throw typeNotSupported("REF");
    }

    @Override
    public void updateBlob(int columnIndex, Blob blob) throws SQLException {
        checkUpdatable();
        getField(columnIndex).setBlob(blob);
    }

    @Override
    public void updateBlob(String columnLabel, Blob blob) throws SQLException {
        checkUpdatable();
        getField(columnLabel).setBlob(blob);
    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {
        updateBinaryStream(columnIndex, inputStream, length);
    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
        updateBinaryStream(columnIndex, inputStream);
    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
        updateBinaryStream(columnLabel, inputStream, length);
    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
        updateBinaryStream(columnLabel, inputStream);
    }

    @Override
    public void updateClob(int columnIndex, Clob clob) throws SQLException {
        checkUpdatable();
        getField(columnIndex).setClob(clob);
    }

    @Override
    public void updateClob(String columnLabel, Clob clob) throws SQLException {
        checkUpdatable();
        getField(columnLabel).setClob(clob);
    }

    @Override
    public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
        updateCharacterStream(columnIndex, reader, length);
    }

    @Override
    public void updateClob(int columnIndex, Reader reader) throws SQLException {
        updateCharacterStream(columnIndex, reader);
    }

    @Override
    public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {
        updateCharacterStream(columnLabel, reader, length);
    }

    @Override
    public void updateClob(String columnLabel, Reader reader) throws SQLException {
        updateCharacterStream(columnLabel, reader);
    }

    @Override
    public void updateArray(int param1, Array param2) throws SQLException {
        throw new FBDriverNotCapableException("Type ARRAY not yet supported");
    }

    @Override
    public void updateArray(String param1, Array param2) throws SQLException {
        throw new FBDriverNotCapableException("Type ARRAY not yet supported");
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: This method behaves exactly the same as {@link #getClob(int)}.
     * </p>
     */
    @Override
    public NClob getNClob(int columnIndex) throws SQLException {
        return (NClob) getClob(columnIndex);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: This method behaves exactly the same as {@link #getClob(String)}.
     * </p>
     */
    @Override
    public NClob getNClob(String columnLabel) throws SQLException {
        return (NClob) getClob(columnLabel);
    }

    @Override
    public RowId getRowId(int columnIndex) throws SQLException {
        return getField(columnIndex).getRowId();
    }

    @Override
    public RowId getRowId(String columnLabel) throws SQLException {
        return getField(columnLabel).getRowId();
    }

    @Override
    public SQLXML getSQLXML(int columnIndex) throws SQLException {
        throw typeNotSupported(TYPE_SQLXML);
    }

    @Override
    public SQLXML getSQLXML(String columnLabel) throws SQLException {
        throw typeNotSupported(TYPE_SQLXML);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: This method behaves exactly the same as {@link #updateClob(int, Clob)}.
     * </p>
     */
    @Override
    public void updateNClob(int columnIndex, NClob clob) throws SQLException {
        updateClob(columnIndex, clob);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: This method behaves exactly the same as {@link #updateClob(int, Reader, long)}.
     * </p>
     */
    @Override
    public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {
        updateCharacterStream(columnIndex, reader, length);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: This method behaves exactly the same as {@link #updateClob(int, Reader)}.
     * </p>
     */
    @Override
    public void updateNClob(int columnIndex, Reader reader) throws SQLException {
        updateCharacterStream(columnIndex, reader);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: This method behaves exactly the same as {@link #updateClob(String, Clob)}.
     * </p>
     */
    @Override
    public void updateNClob(String columnLabel, NClob clob) throws SQLException {
        updateClob(columnLabel, clob);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: This method behaves exactly the same as {@link #updateClob(int, Reader, long)}.
     * </p>
     */
    @Override
    public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {
        updateCharacterStream(columnLabel, reader, length);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: This method behaves exactly the same as {@link #updateClob(String, Reader)}.
     * </p>
     */
    @Override
    public void updateNClob(String columnLabel, Reader reader) throws SQLException {
        updateCharacterStream(columnLabel, reader);
    }

    @Override
    public void updateRowId(int columnIndex, RowId x) throws SQLException {
        rowIdNotUpdatable();
    }

    @Override
    public void updateRowId(String columnLabel, RowId x) throws SQLException {
        rowIdNotUpdatable();
    }

    private void rowIdNotUpdatable() throws SQLException {
        checkUpdatable();
        throw new FBDriverNotCapableException("Firebird rowId (RDB$DB_KEY) is not updatable");
    }

    @Override
    public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
        throw typeNotSupported(TYPE_SQLXML);
    }

    @Override
    public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {
        throw typeNotSupported(TYPE_SQLXML);
    }

    @Override
    public String getExecutionPlan() throws SQLException {
        checkCursorMove();
        if (statement == null) return "";
        return statement.getExecutionPlan();
    }

    @Override
    public String getExplainedExecutionPlan() throws SQLException {
        checkCursorMove();
        if (statement == null) return "";
        return statement.getExplainedExecutionPlan();
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface != null && iface.isAssignableFrom(this.getClass());
    }

    @Override
    public <T> @NonNull T unwrap(Class<T> iface) throws SQLException {
        if (!isWrapperFor(iface)) {
            throw new SQLException("Unable to unwrap to class " + (iface != null ? iface.getName() : "(null)"));
        }

        return iface.cast(this);
    }

    private static @NonNull SQLException typeNotSupported(@NonNull String typeName) {
        return new FBDriverNotCapableException("Type " + typeName + " not supported");
    }

    @SuppressWarnings("DataFlowIssue")
    private final class DataProvider implements FieldDataProvider {

        private final int fieldPosition;

        private DataProvider(int fieldPosition) {
            this.fieldPosition = fieldPosition;
        }

        @Override
        public byte[] getFieldData() {
            return row.getFieldData(fieldPosition);
        }

        @Override
        public void setFieldData(byte[] data) {
            row.setFieldData(fieldPosition, data);
        }
    }

}
