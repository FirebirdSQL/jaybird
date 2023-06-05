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
import org.firebirdsql.gds.ng.fields.RowDescriptor;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.jaybird.props.PropertyConstants;
import org.firebirdsql.jdbc.field.FBCloseableField;
import org.firebirdsql.jdbc.field.FBField;
import org.firebirdsql.jdbc.field.FieldDataProvider;
import org.firebirdsql.jdbc.field.TrimmableField;
import org.firebirdsql.util.SQLExceptionChainBuilder;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link ResultSet}.
 *
 * @author David Jencks
 * @author Roman Rokytskyy
 * @author Mark Rotteveel
 */
@SuppressWarnings("RedundantThrows")
public class FBResultSet implements ResultSet, FirebirdResultSet, FBObjectListener.FetcherListener {

    private static final String UNICODE_STREAM_NOT_SUPPORTED = "Unicode stream not supported";

    private final FBStatement fbStatement;
    private FBFetcher fbFetcher;
    private FirebirdRowUpdater rowUpdater;

    protected final FBConnection connection;
    protected final GDSHelper gdsHelper;

    protected final RowDescriptor rowDescriptor;

    protected RowValue row;

    private boolean wasNull = false;
    private boolean wasNullValid = false;
    // closed is false until the close method is invoked;
    private volatile boolean closed = false;

    private SQLWarning firstWarning;

    private final FBField[] fields;
    private final List<FBCloseableField> closeableFields = new ArrayList<>();
    private final Map<String, Integer> colNames;

    private final String cursorName;
    private final FBObjectListener.ResultSetListener listener;

    private final int rsType;
    private final int rsConcurrency;
    private final int rsHoldability;
    private int fetchDirection = ResultSet.FETCH_FORWARD;

    @Override
    public void allRowsFetched(FBFetcher fetcher) throws SQLException {
        listener.allRowsFetched(this);
    }

    @Override
    public void fetcherClosed(FBFetcher fetcher) throws SQLException {
        // ignore, there nothing to do here
    }

    @Override
    public void rowChanged(FBFetcher fetcher, RowValue newRow) throws SQLException {
        this.row = newRow;
    }

    /**
     * Creates a new {@code FBResultSet} instance.
     */
    public FBResultSet(FBConnection connection,
            FBStatement fbStatement,
            FbStatement stmt,
            FBObjectListener.ResultSetListener listener,
            boolean metaDataQuery,
            int rsType,
            int rsConcurrency,
            int rsHoldability,
            boolean cached)
            throws SQLException {
        try {
            this.connection = connection;
            this.gdsHelper = connection != null ? connection.getGDSHelper() : null;
            cursorName = fbStatement.getCursorName();
            this.listener = listener != null ? listener : FBObjectListener.NoActionResultSetListener.instance();
            rowDescriptor = stmt.getRowDescriptor();
            fields = new FBField[rowDescriptor.getCount()];
            colNames = new HashMap<>(rowDescriptor.getCount(), 1);
            this.fbStatement = fbStatement;

            if (rsType == ResultSet.TYPE_SCROLL_SENSITIVE) {
                fbStatement.addWarning(FbExceptionBuilder
                        .forWarning(JaybirdErrorCodes.jb_resultSetTypeDowngradeReasonScrollSensitive)
                        .toSQLException(SQLWarning.class));
                rsType = ResultSet.TYPE_SCROLL_INSENSITIVE;
            }
            boolean serverSideScrollable = rsHoldability != ResultSet.HOLD_CURSORS_OVER_COMMIT && !metaDataQuery
                        && connection != null && connection.isScrollableCursor(PropertyConstants.SCROLLABLE_CURSOR_SERVER)
                        && stmt.supportsFetchScroll();
            cached = cached || metaDataQuery || !(rsType == TYPE_FORWARD_ONLY || serverSideScrollable);

            prepareVars(cached, metaDataQuery);
            if (cached) {
                fbFetcher = new FBCachedFetcher(gdsHelper, fbStatement.fetchSize, fbStatement.maxRows, stmt, this,
                        rsType == ResultSet.TYPE_FORWARD_ONLY);
            } else if (serverSideScrollable && rsType == ResultSet.TYPE_SCROLL_INSENSITIVE) {
                fbFetcher = new FBServerScrollFetcher(fbStatement.fetchSize, fbStatement.maxRows, stmt, this);
            } else if (fbStatement.isUpdatableCursor()) {
                fbFetcher = new FBUpdatableCursorFetcher(gdsHelper, stmt, this, fbStatement.maxRows,
                        fbStatement.fetchSize);
            } else {
                assert rsType == ResultSet.TYPE_FORWARD_ONLY : "Expected TYPE_FORWARD_ONLY";
                fbFetcher = new FBStatementFetcher(gdsHelper, stmt, this, fbStatement.maxRows, fbStatement.fetchSize);
            }

            if (rsConcurrency == ResultSet.CONCUR_UPDATABLE) {
                try {
                    rowUpdater = new FBRowUpdater(connection, rowDescriptor, cached, listener);
                    if (serverSideScrollable && fbFetcher instanceof FBServerScrollFetcher) {
                        fbFetcher = new FBUpdatableFetcher(fbFetcher, this, rowDescriptor.createDeletedRowMarker());
                    }
                } catch (FBResultSetNotUpdatableException ex) {
                    fbStatement.addWarning(FbExceptionBuilder
                            .forWarning(JaybirdErrorCodes.jb_concurrencyResetReadOnlyReasonNotUpdatable)
                            .toSQLException(SQLWarning.class));
                    rsConcurrency = ResultSet.CONCUR_READ_ONLY;
                }
            }
            this.rsType = rsType;
            this.rsConcurrency = rsConcurrency;
            this.rsHoldability = rsHoldability;
            this.fetchDirection = fbStatement.getFetchDirection();
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
     * @param listener
     *         result set listener
     */
    public FBResultSet(RowDescriptor rowDescriptor, List<RowValue> rows, FBObjectListener.ResultSetListener listener)
            throws SQLException {
        this(rowDescriptor, null, rows, listener, false, false);
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
    public FBResultSet(RowDescriptor rowDescriptor, List<RowValue> rows) throws SQLException {
        this(rowDescriptor, rows, null);
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
     * @param connection
     *         connection (cannot be null when {@code retrieveBlobs} is {@code true}
     * @param rows
     *         row data
     * @param retrieveBlobs
     *         {@code true} retrieves the blob data
     */
    public FBResultSet(RowDescriptor rowDescriptor, FBConnection connection, List<RowValue> rows,
            boolean retrieveBlobs) throws SQLException {
        this(rowDescriptor, connection, rows, null, retrieveBlobs, true);
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
     * @param trimStrings
     *         {@code true} when strings need to be trimmed (generally only for metadata queries)
     * @since 5.0.1
     */
    public FBResultSet(RowDescriptor rowDescriptor, FBConnection connection, List<RowValue> rows,
            FBObjectListener.ResultSetListener listener, boolean retrieveBlobs, boolean trimStrings)
            throws SQLException {
        // TODO Evaluate if we need to share more implementation with constructor above
        this.connection = connection;
        gdsHelper = connection != null ? connection.getGDSHelper() : null;;
        fbStatement = null;
        this.listener = listener != null ? listener : FBObjectListener.NoActionResultSetListener.instance();
        cursorName = null;
        fbFetcher = new FBCachedFetcher(rows, this, rowDescriptor, gdsHelper, retrieveBlobs);
        this.rowDescriptor = rowDescriptor;
        fields = new FBField[rowDescriptor.getCount()];
        colNames = new HashMap<>(rowDescriptor.getCount(), 1);
        prepareVars(true, trimStrings);
        // TODO Set specific types (see also previous todo)
        rsType = ResultSet.TYPE_FORWARD_ONLY;
        rsConcurrency = ResultSet.CONCUR_READ_ONLY;
        rsHoldability = ResultSet.CLOSE_CURSORS_AT_COMMIT;
    }

    private void prepareVars(boolean cached, boolean trimStrings) throws SQLException {
        for (int i = 0; i < rowDescriptor.getCount(); i++) {
            final int fieldPosition = i;

            FieldDataProvider dataProvider = new FieldDataProvider() {
                @Override
                public byte[] getFieldData() {
                    return row.getFieldData(fieldPosition);
                }

                @Override
                public void setFieldData(byte[] data) {
                    row.setFieldData(fieldPosition, data);
                }
            };

            final FBField field = FBField.createField(rowDescriptor.getFieldDescriptor(i), dataProvider, gdsHelper, cached);

            if (field instanceof FBCloseableField) {
                closeableFields.add((FBCloseableField) field);
            }

            if (trimStrings && field instanceof TrimmableField) {
                ((TrimmableField) field).setTrimTrailing(true);
            }

            fields[i] = field;
        }
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
            throw new SQLException("The result set is closed", SQLStateConstants.SQL_STATE_NO_RESULT_SET);
        }
    }

    /**
     * Checks if the result set is scrollable
     *
     * @throws SQLException
     *         if ResultSet is not scrollable
     */
    protected void checkScrollable() throws SQLException {
        if (rsType == ResultSet.TYPE_FORWARD_ONLY) {
            throw new FbExceptionBuilder().nonTransientException(JaybirdErrorCodes.jb_operationNotAllowedOnForwardOnly)
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
        // TODO See if we can apply completion reason logic (eg no need to close blob on commit)
        wasNullValid = false;

        // if there are no fields to close, then nothing to do
        if (closeableFields.isEmpty())
            return;

        SQLExceptionChainBuilder<SQLException> chain = new SQLExceptionChainBuilder<>();
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
        return closed;
    }

    void close(boolean notifyListener, CompletionReason completionReason) throws SQLException {
        if (isClosed()) return;
        closed = true;
        SQLExceptionChainBuilder<SQLException> chain = new SQLExceptionChainBuilder<>();

        try {
            closeFields();
        } catch (SQLException ex) {
            chain.append(ex);
        } finally {
            try {
                if (fbFetcher != null) {
                    try {
                        fbFetcher.close(completionReason);
                    } catch (SQLException ex) {
                        chain.append(ex);
                    }
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
                fbFetcher = null;
                rowUpdater = null;
            }
        }

        if (chain.hasException()) {
            throw chain.getException();
        }
    }

    @Override
    public boolean wasNull() throws SQLException {
        if (!wasNullValid) {
            throw new SQLException("Get a column before testing null");
        }
        if (row == null) {
            throw new SQLException("No row available for wasNull");
        }
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
    @Deprecated
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
    public FBField getField(int columnIndex) throws SQLException {
        final FBField field = getField(columnIndex, true);

        wasNullValid = true;
        wasNull = row == null || row.getFieldData(columnIndex - 1) == null;

        return field;
    }

    /**
     * Factory method for the field access objects
     */
    public FBField getField(int columnIndex, boolean checkRowPosition) throws SQLException {
        checkOpen();

        if (checkRowPosition && row == null && rowUpdater == null) {
            throw new SQLException("The result set is not in a row, use next", SQLStateConstants.SQL_STATE_NO_ROW_AVAIL);
        }

        if (columnIndex > rowDescriptor.getCount()) {
            throw new SQLException("Invalid column index: " + columnIndex,
                    SQLStateConstants.SQL_STATE_INVALID_DESC_FIELD_ID);
        }

        if (rowUpdater != null) {
            return rowUpdater.getField(columnIndex - 1);
        } else {
            return fields[columnIndex - 1];
        }
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
        checkOpen();
        if (row == null && rowUpdater == null) {
            throw new SQLException("The result set is not in a row, use next", SQLStateConstants.SQL_STATE_NO_ROW_AVAIL);
        }

        if (columnName == null) {
            throw new SQLException("Column identifier must be not null",
                    SQLStateConstants.SQL_STATE_INVALID_DESC_FIELD_ID);
        }

        Integer fieldNum = colNames.get(columnName);
        // If it is the first time the columnName is used
        if (fieldNum == null) {
            fieldNum = findColumn(columnName);
            colNames.put(columnName, fieldNum);
        }
        final FBField field = rowUpdater != null
                ? rowUpdater.getField(fieldNum - 1)
                : fields[fieldNum - 1];
        wasNullValid = true;
        wasNull = row == null || row.getFieldData(fieldNum - 1) == null;
        return field;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: ignores {@code scale} and behaves identical to {@link #getBigDecimal(int)}.
     * </p>
     */
    @Deprecated
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
    @Deprecated
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
    @Deprecated
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

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return firstWarning;
    }

    @Override
    public void clearWarnings() throws SQLException {
        firstWarning = null;
    }

    @Override
    public String getCursorName() throws SQLException {
        return cursorName;
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        checkOpen();
        return new FBResultSetMetaData(rowDescriptor, connection);
    }

    @Override
    public Object getObject(String columnName) throws SQLException {
        return getField(columnName).getObject();
    }

    // See section 14.2.3 of jdbc-3.0 specification
    // "Column names supplied to getter methods are case insensitive
    // If a select list contains the same column more than once, 
    // the first instance of the column will be returned
    @Override
    public int findColumn(String columnName) throws SQLException {
        if (columnName == null || columnName.equals("")) {
            throw new SQLException("Empty string does not identify a column",
                    SQLStateConstants.SQL_STATE_INVALID_DESC_FIELD_ID);
        }
        if (columnName.startsWith("\"") && columnName.endsWith("\"")) {
            columnName = columnName.substring(1, columnName.length() - 1);
            // case-sensitively check column aliases 
            for (int i = 0; i < rowDescriptor.getCount(); i++) {
                if (columnName.equals(rowDescriptor.getFieldDescriptor(i).getFieldName())) {
                    return ++i;
                }
            }
            // case-sensitively check column names
            for (int i = 0; i < rowDescriptor.getCount(); i++) {
                if (columnName.equals(rowDescriptor.getFieldDescriptor(i).getOriginalName())) {
                    return ++i;
                }
            }
        } else {
            for (int i = 0; i < rowDescriptor.getCount(); i++) {
                if (columnName.equalsIgnoreCase(rowDescriptor.getFieldDescriptor(i).getFieldName())) {
                    return ++i;
                }
            }
            for (int i = 0; i < rowDescriptor.getCount(); i++) {
                if (columnName.equalsIgnoreCase(rowDescriptor.getFieldDescriptor(i).getOriginalName())) {
                    return ++i;
                }
            }
        }

        if ("RDB$DB_KEY".equalsIgnoreCase(columnName)) {
            // Fix up: RDB$DB_KEY is identified as DB_KEY in the result set
            return findColumn("DB_KEY");
        }

        throw new SQLException("Column name " + columnName + " not found in result set",
                SQLStateConstants.SQL_STATE_INVALID_DESC_FIELD_ID);
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
        return fbFetcher.isBeforeFirst();
    }

    @Override
    public boolean isAfterLast() throws SQLException {
        checkOpen();
        return fbFetcher.isAfterLast();
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

    @Override
    public void setFetchDirection(int direction) throws SQLException {
        checkOpen();
        switch (direction) {
        case ResultSet.FETCH_FORWARD:
            fetchDirection = direction;
            break;
        case ResultSet.FETCH_REVERSE:
        case ResultSet.FETCH_UNKNOWN:
            checkScrollable();
            fetchDirection = direction;
            break;
        default:
            throw FbExceptionBuilder.forException(JaybirdErrorCodes.jb_invalidFetchDirection)
                    .messageParameter(direction)
                    .toSQLException();
        }
    }

    @Override
    public int getFetchDirection() throws SQLException {
        checkOpen();
        return fetchDirection;
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {
        checkOpen();
        if (rows < 0) {
            throw new SQLException("Can't set negative fetch size", SQLStateConstants.SQL_STATE_INVALID_ATTR_VALUE);
        }
        fbFetcher.setFetchSize(rows);
    }

    @Override
    public int getFetchSize() throws SQLException {
        checkOpen();
        return fbFetcher.getFetchSize();
    }

    @Override
    public int getType() throws SQLException {
        return rsType;
    }

    @Override
    public int getConcurrency() throws SQLException {
        return rsConcurrency;
    }

    @Override
    public int getHoldability() throws SQLException {
        return rsHoldability;
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
     *         When this result set is not updatable
     */
    private void checkUpdatable() throws SQLException {
        checkOpen();
        if (rowUpdater == null) {
            throw new FBResultSetNotUpdatableException();
        }
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
        checkUpdatable();

        rowUpdater.insertRow();
        fbFetcher.insertRow(rowUpdater.getInsertRow());
        notifyRowUpdater();
    }

    @Override
    public void updateRow() throws SQLException {
        checkUpdatable();

        rowUpdater.updateRow();
        fbFetcher.updateRow(rowUpdater.getNewRow());
        notifyRowUpdater();
    }

    @Override
    public void deleteRow() throws SQLException {
        checkUpdatable();

        rowUpdater.deleteRow();
        fbFetcher.deleteRow();
        notifyRowUpdater();
    }

    @Override
    public void refreshRow() throws SQLException {
        checkUpdatable();

        rowUpdater.refreshRow();
        fbFetcher.updateRow(rowUpdater.getOldRow());

        // this is excessive, but we do this to keep the code uniform
        notifyRowUpdater();
    }

    @Override
    public void cancelRowUpdates() throws SQLException {
        checkUpdatable();
        rowUpdater.cancelRowUpdates();
    }

    @Override
    public void moveToInsertRow() throws SQLException {
        checkUpdatable();
        rowUpdater.moveToInsertRow();
    }

    @Override
    public void moveToCurrentRow() throws SQLException {
        checkUpdatable();
        rowUpdater.moveToCurrentRow();
    }

    @Override
    public Statement getStatement() {
        return fbStatement;
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
        throw new FBDriverNotCapableException("Type URL not supported");
    }

    @Override
    public URL getURL(String param1) throws SQLException {
        throw new FBDriverNotCapableException("Type URL not supported");
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
        throw new FBDriverNotCapableException("Type REF not supported");
    }

    @Override
    public void updateRef(String param1, Ref param2) throws SQLException {
        throw new FBDriverNotCapableException("Type REF not supported");
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
        checkUpdatable();
        getField(columnIndex).setBinaryStream(inputStream, length);
    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
        checkUpdatable();
        getField(columnIndex).setBinaryStream(inputStream);
    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
        checkUpdatable();
        getField(columnLabel).setBinaryStream(inputStream, length);
    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
        checkUpdatable();
        getField(columnLabel).setBinaryStream(inputStream);
    }

    @Override
    public void updateClob(int columnIndex, java.sql.Clob clob) throws SQLException {
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
        checkUpdatable();
        getField(columnIndex).setCharacterStream(reader, length);
    }

    @Override
    public void updateClob(int columnIndex, Reader reader) throws SQLException {
        checkUpdatable();
        getField(columnIndex).setCharacterStream(reader);
    }

    @Override
    public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {
        checkUpdatable();
        getField(columnLabel).setCharacterStream(reader, length);
    }

    @Override
    public void updateClob(String columnLabel, Reader reader) throws SQLException {
        checkUpdatable();
        getField(columnLabel).setCharacterStream(reader);
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
        throw new FBDriverNotCapableException("Type SQLXML not supported");
    }

    @Override
    public SQLXML getSQLXML(String columnLabel) throws SQLException {
        throw new FBDriverNotCapableException("Type SQLXML not supported");
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
        updateClob(columnIndex, reader, length);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: This method behaves exactly the same as {@link #updateClob(int, Reader)}.
     * </p>
     */
    @Override
    public void updateNClob(int columnIndex, Reader reader) throws SQLException {
        updateClob(columnIndex, reader);
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
        updateClob(columnLabel, reader, length);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: This method behaves exactly the same as {@link #updateClob(String, Reader)}.
     * </p>
     */
    @Override
    public void updateNClob(String columnLabel, Reader reader) throws SQLException {
        updateClob(columnLabel, reader);
    }

    @Override
    public void updateRowId(int columnIndex, RowId x) throws SQLException {
        checkUpdatable();
        throw new FBDriverNotCapableException("Firebird rowId (RDB$DB_KEY) is not updatable");
    }

    @Override
    public void updateRowId(String columnLabel, RowId x) throws SQLException {
        checkUpdatable();
        throw new FBDriverNotCapableException("Firebird rowId (RDB$DB_KEY) is not updatable");
    }

    @Override
    public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
        throw new FBDriverNotCapableException("Type SQLXML not supported");
    }

    @Override
    public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {
        throw new FBDriverNotCapableException("Type SQLXML not supported");
    }

    @Override
    public String getExecutionPlan() throws SQLException {
        checkCursorMove();

        if (fbStatement == null)
            return "";

        return fbStatement.getExecutionPlan();
    }

    @Override
    public String getExplainedExecutionPlan() throws SQLException {
        checkCursorMove();

        if (fbStatement == null)
            return "";

        return fbStatement.getExplainedExecutionPlan();
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface != null && iface.isAssignableFrom(this.getClass());
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (!isWrapperFor(iface))
            throw new SQLException("Unable to unwrap to class " + iface.getName());

        return iface.cast(this);
    }

    protected void addWarning(SQLWarning warning) {
        if (firstWarning == null) {
            firstWarning = warning;
        } else {
            firstWarning.setNextWarning(warning);
        }
    }
}
