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
import org.firebirdsql.gds.ng.FbBatchConfig;
import org.firebirdsql.gds.ng.FbStatement;
import org.firebirdsql.gds.ng.LockCloseable;
import org.firebirdsql.gds.ng.StatementType;
import org.firebirdsql.gds.ng.fields.FieldDescriptor;
import org.firebirdsql.gds.ng.fields.RowDescriptor;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.gds.ng.listeners.StatementListener;
import org.firebirdsql.jdbc.field.BlobListenableField;
import org.firebirdsql.jdbc.field.FBField;
import org.firebirdsql.jdbc.field.FBFlushableField;
import org.firebirdsql.jdbc.field.FBFlushableField.CachedObject;
import org.firebirdsql.jdbc.field.FieldDataProvider;
import org.firebirdsql.util.InternalApi;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.sql.Date;
import java.util.*;

import static java.util.Collections.emptyList;
import static org.firebirdsql.jdbc.SQLStateConstants.SQL_STATE_GENERAL_ERROR;

/**
 * Implementation of {@link java.sql.PreparedStatement}.
 * <p>
 * This class is internal API of Jaybird. Future versions may radically change, move, or make inaccessible this type.
 * For the public API, refer to the {@link PreparedStatement} and {@link FirebirdPreparedStatement} interfaces.
 * </p>
 *
 * @author David Jencks
 * @author Roman Rokytskyy
 * @author Mark Rotteveel
 */
@SuppressWarnings("RedundantThrows")
@InternalApi
public class FBPreparedStatement extends FBStatement implements FirebirdPreparedStatement {

    public static final String METHOD_NOT_SUPPORTED =
            "This method is only supported on Statement and not supported on PreparedStatement and CallableStatement";
    private static final String UNICODE_STREAM_NOT_SUPPORTED = "Unicode stream not supported";

    private final boolean metaDataQuery;
    
    /**
     * This flag is needed to guarantee the correct behavior in case when it 
     * was created without controlling Connection object (in some metadata
     * queries we have only GDSHelper instance)
     */
    private final boolean standaloneStatement;
    
    /**
     * This flag is needed to prevent throwing an exception for the case when
     * result set is returned for INSERT statement and the statement should
     * return the generated keys.
     */
    private final boolean generatedKeys;

    private FBField[] fields = null;

    // we need to handle procedure execution separately,
    // because in this case we must send out_xsqlda to the server.
    private boolean isExecuteProcedureStatement;

    private final FBObjectListener.BlobListener blobListener;
    private RowValue fieldValues;
    private Batch batch;

    /**
     * Create instance of this class for the specified result set type and concurrency. This constructor is used only in
     * {@link FBCallableStatement} since the statement is prepared right before the execution.
     *
     * @param c
     *         instance of {@link GDSHelper} that will be used to perform all database activities
     * @param rsBehavior
     *         result set behavior
     * @param statementListener
     *         statement listener that will be notified about the statement start, close and completion
     * @param blobListener
     *         blob listener that will be notified about the statement start and completion
     * @throws SQLException
     *         if something went wrong.
     */
    protected FBPreparedStatement(GDSHelper c, ResultSetBehavior rsBehavior,
            FBObjectListener.StatementListener statementListener, FBObjectListener.BlobListener blobListener)
            throws SQLException {
        super(c, rsBehavior, statementListener);
        this.blobListener = blobListener;
        this.standaloneStatement = false;
        this.metaDataQuery = false;
        this.generatedKeys = false;
        setPoolable(true);
    }

    /**
     * Create instance of this class and prepare SQL statement.
     *
     * @param c
     *         connection to be used
     * @param sql
     *         SQL statement to prepare
     * @param rsBehavior
     *         result set behavior
     * @param statementListener
     *         statement listener that will be notified about the statement start, close and completion
     * @param blobListener
     *         blob listener that will be notified about the statement start and completion
     * @param metaDataQuery
     *         {@code true} for a metadata query, {@code false} for a normal query
     * @param standaloneStatement
     *         {@code true} for a standalone statement (should only be used when {@code metaDataQuery == true})
     * @param generatedKeys
     *         {@code true} if this statement produces a generated keys result set
     * @throws SQLException
     *         if something went wrong.
     */
    protected FBPreparedStatement(GDSHelper c, String sql, ResultSetBehavior rsBehavior,
            FBObjectListener.StatementListener statementListener, FBObjectListener.BlobListener blobListener,
            boolean metaDataQuery, boolean standaloneStatement, boolean generatedKeys) throws SQLException {
        super(c, rsBehavior, statementListener);
        this.blobListener = blobListener;
        this.metaDataQuery = metaDataQuery;
        this.standaloneStatement = standaloneStatement;
        this.generatedKeys = generatedKeys;
        setPoolable(true);

        try (LockCloseable ignored = c.withLock()) {
            // TODO See http://tracker.firebirdsql.org/browse/JDBC-352
            notifyStatementStarted();
            try {
                prepareFixedStatement(sql);
            } catch (Exception e) {
                notifyStatementCompleted(false, e);
                throw e;
            }
        }
    }
    
    @Override
    public void completeStatement(CompletionReason reason) throws SQLException {
        if (!metaDataQuery || reason == CompletionReason.CONNECTION_ABORT) {
            super.completeStatement(reason);
        } else {
            notifyStatementCompleted();
        }
    }

    @Override
    protected void notifyStatementCompleted(boolean success) throws SQLException {
        try {
            super.notifyStatementCompleted(success);
        } finally {
            if (metaDataQuery && standaloneStatement)
                close();
        }
    }

    @Override
    protected boolean needsScrollableCursorEnabled() {
        return !metaDataQuery && super.needsScrollableCursorEnabled();
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        return executeQuery(false);
    }

    private ResultSet executeQuery(boolean metaDataQuery) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkValidity();
            notifyStatementStarted();
            try {
                if (!internalExecute(isExecuteProcedureStatement)) {
                    throw queryProducedNoResultSet();
                }
                return getResultSet(metaDataQuery);
            } catch (Exception e) {
                notifyStatementCompleted(true, e);
                throw e;
            }
        }
    }

    @Override
    public int executeUpdate() throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkValidity();
            notifyStatementStarted();
            try {
                if (internalExecute(isExecuteProcedureStatement) && !isGeneratedKeyQuery()) {
                    throw updateReturnedResultSet();
                }
                int updateCount = getUpdateCountMinZero();
                notifyStatementCompleted();
                return updateCount;
            } catch (Exception e) {
                notifyStatementCompleted(true, e);
                throw e;
            }
        }
    }

    @Override
    public void close() throws SQLException {
        try {
            Batch batch = this.batch;
            if (batch != null) {
                this.batch = null;
                batch.close();
            }
        } finally {
            super.close();
        }
    }

    public FirebirdParameterMetaData getFirebirdParameterMetaData() throws SQLException {
        return new FBParameterMetaData(fbStatement.getParameterDescriptor(), connection);
    }

    @Override
    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        getField(parameterIndex).setNull();
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream inputStream, int length) throws SQLException {
        getField(parameterIndex).setBinaryStream(inputStream, length);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        getField(parameterIndex).setBinaryStream(inputStream, length);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream inputStream) throws SQLException {
        getField(parameterIndex).setBinaryStream(inputStream);
    }

    @Override
    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        getField(parameterIndex).setBytes(x);
    }

    @Override
    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        getField(parameterIndex).setBoolean(x);
    }

    @Override
    public void setByte(int parameterIndex, byte x) throws SQLException {
        getField(parameterIndex).setByte(x);
    }

    @Override
    public void setDate(int parameterIndex, Date x) throws SQLException {
        getField(parameterIndex).setDate(x);
    }

    @Override
    public void setDouble(int parameterIndex, double x) throws SQLException {
        getField(parameterIndex).setDouble(x);
    }

    @Override
    public void setFloat(int parameterIndex, float x) throws SQLException {
        getField(parameterIndex).setFloat(x);
    }

    @Override
    public void setInt(int parameterIndex, int x) throws SQLException {
        getField(parameterIndex).setInteger(x);
    }

    @Override
    public void setLong(int parameterIndex, long x) throws SQLException {
        getField(parameterIndex).setLong(x);
    }

    @Override
    public void setObject(int parameterIndex, Object x) throws SQLException {
        getField(parameterIndex).setObject(x);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: behaves as {@link #setObject(int, Object, int, int)} called with
     * {@link SQLType#getVendorTypeNumber()}.
     * </p>
     */
    @Override
    public void setObject(int parameterIndex, Object x, SQLType targetSqlType, int scaleOrLength) throws SQLException {
        setObject(parameterIndex, x, targetSqlType.getVendorTypeNumber(), scaleOrLength);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: behaves as {@link #setObject(int, Object, int)} called with
     * {@link SQLType#getVendorTypeNumber()}.
     * </p>
     */
    @Override
    public void setObject(int parameterIndex, Object x, SQLType targetSqlType) throws SQLException {
        setObject(parameterIndex, x, targetSqlType.getVendorTypeNumber());
    }

    @Override
    public void setShort(int parameterIndex, short x) throws SQLException {
        getField(parameterIndex).setShort(x);
    }

    @Override
    public void setString(int parameterIndex, String x) throws SQLException {
        getField(parameterIndex).setString(x);
    }

    @Override
    public void setTime(int parameterIndex, Time x) throws SQLException {
        getField(parameterIndex).setTime(x);
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        getField(parameterIndex).setTimestamp(x);
    }

    @Override
    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        getField(parameterIndex).setBigDecimal(x);
    }

    /**
     * Returns the {@link FieldDescriptor} of the specified parameter.
     *
     * @param columnIndex 1-based index of the parameter
     * @return Field descriptor
     */
    protected FieldDescriptor getParameterDescriptor(int columnIndex) {
        return fbStatement.getParameterDescriptor().getFieldDescriptor(columnIndex - 1);
    }

    /**
     * Factory method for the field access objects
     */
    protected FBField getField(int columnIndex) throws SQLException {
        checkValidity();
        if (columnIndex > fields.length) {
            throw new SQLException("Invalid column index: " + columnIndex,
                    SQLStateConstants.SQL_STATE_INVALID_DESC_FIELD_ID);
        }

        return fields[columnIndex - 1];
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: works identical to {@link #setBinaryStream(int, InputStream, int)}.
     * </p>
     */
    @Override
    public final void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
        setBinaryStream(parameterIndex, x, length);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: works identical to {@link #setBinaryStream(int, InputStream, long)}.
     * </p>
     */
    @Override
    public final void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
        setBinaryStream(parameterIndex, x, length);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: works identical to {@link #setBinaryStream(int, InputStream)}.
     * </p>
     */
    @Override
    public final void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
        setBinaryStream(parameterIndex, x);
    }

    /**
     * Method is no longer supported since Jaybird 3.0.
     * <p>
     * For old behavior use {@link #setBinaryStream(int, InputStream, int)}. For JDBC suggested behavior,
     * use {@link #setCharacterStream(int, Reader, int)}.
     * </p>
     *
     * @throws SQLFeatureNotSupportedException Always
     * @deprecated
     */
    @Deprecated(since = "1")
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
        throw new SQLFeatureNotSupportedException(UNICODE_STREAM_NOT_SUPPORTED);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Jaybird does not support array types.
     * </p>
     */
    @Override
    public void setURL(int parameterIndex, URL url) throws SQLException {
        throw new FBDriverNotCapableException("Type URL not supported");
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: This method behaves exactly the same as {@link #setCharacterStream(int, Reader, long)}.
     * </p>
     */
    @Override
    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
        setCharacterStream(parameterIndex, value, length);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: This method behaves exactly the same as {@link #setCharacterStream(int, Reader)}.
     * </p>
     */
    @Override
    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        setCharacterStream(parameterIndex, value);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: This method behaves exactly the same as {@link #setClob(int, Reader, long)}.
     * </p>
     */
    @Override
    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
        setClob(parameterIndex, reader, length);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: This method behaves exactly the same as {@link #setClob(int, Reader)}.
     * </p>
     */
    @Override
    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        setClob(parameterIndex, reader);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: This method behaves exactly the same as {@link #setString(int, String)}.
     * </p>
     */
    @Override
    public void setNString(int parameterIndex, String value) throws SQLException {
        setString(parameterIndex, value);
    }

    @Override
    public void clearParameters() throws SQLException {
        checkValidity();
        if (fieldValues != null) {
            fieldValues.reset();
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: ignores {@code scale} and {@code targetSqlType} and works as
     * {@link #setObject(int, Object)}.
     * </p>
     */
    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType, int scale) throws SQLException {
        setObject(parameterIndex, x);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: ignores {@code targetSqlType} and works as {@link #setObject(int, Object)}.
     * </p>
     */
    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        setObject(parameterIndex, x);
    }

    @Override
    public boolean execute() throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkValidity();
            notifyStatementStarted();
            try {
                boolean hasResultSet = internalExecute(isExecuteProcedureStatement);
                if (!hasResultSet) {
                    notifyStatementCompleted();
                }
                return hasResultSet;
            } catch (Exception e) {
                notifyStatementCompleted(true, e);
                throw e;
            }
        }
    }

    /**
     * Execute metadata query. This method is similar to {@link #executeQuery()} however, it always returns
     * a cached result set and strings in the result set are always trimmed (server defines system tables using CHAR
     * data type, but it should be used as VARCHAR).
     *
     * @return result set corresponding to the specified query.
     * @throws SQLException
     *         if something went wrong or no result set was available.
     */
    ResultSet executeMetaDataQuery() throws SQLException {
        return executeQuery(true);
    }

    /**
     * Execute this statement. Method checks whether all parameters are set,
     * flushes all "flushable" fields that might contain cached data and
     * executes the statement. 
     * 
     * @param sendOutParams Determines if the XSQLDA structure should be sent to the
     *            database
     * @return {@code true} if the statement has more result sets.
     */
    protected boolean internalExecute(boolean sendOutParams) throws SQLException {
        checkAllParametersSet();

        try (LockCloseable ignored = withLock()) {
            flushFields();

            return internalExecute(fieldValues);
        }
    }

    private void checkAllParametersSet() throws SQLException {
        // This relies on FBFlushableField explicitly initializing the field with null when setting a cached object
        // This way we avoid flushing cached objects unless we are really going to execute
        fbStatement.validateParameters(fieldValues);
    }

    @Override
    protected boolean isGeneratedKeyQuery() {
        return generatedKeys;
    }

    /**
     * Flush fields that might have cached data.
     * 
     * @throws SQLException if something went wrong.
     */
    @SuppressWarnings("ForLoopReplaceableByForEach")
    private void flushFields() throws SQLException {
        // flush any cached data that can be hanging
        for (int i = 0; i < fields.length; i++) {
            if (fields[i] instanceof FBFlushableField flushableField) {
                flushableField.flushCachedData();
            }
        }
    }

    private Batch requireBatch() throws SQLException {
        Batch batch = this.batch;
        if (batch != null) {
            return batch;
        }
        return this.batch = createBatch();
    }

    // NOTE: This is not used for FBCallableStatement!
    private Batch createBatch() throws SQLException {
        if (canExecuteUsingServerBatch()) {
            return new ServerBatch(
                    FbBatchConfig.of(FbBatchConfig.HALT_AT_FIRST_ERROR, FbBatchConfig.UPDATE_COUNTS,
                            FbBatchConfig.SERVER_DEFAULT_DETAILED_ERRORS, connection.getServerBatchBufferSize()),
                    fbStatement);
        } else {
            return new EmulatedPreparedStatementBatch();
        }
    }

    private boolean canExecuteUsingServerBatch() {
        // enabled for connection
        return connection != null && connection.isUseServerBatch()
                // supported by statement implementation
                && fbStatement != null && fbStatement.supportBatchUpdates()
                // server batch execution throws isc_batch_param when executing a statement without parameters
                && fbStatement.getParameterDescriptor().getCount() != 0
                // server batch execution cannot produce rows from RETURNING
                && !isGeneratedKeyQuery();
    }

    @Override
    public void addBatch() throws SQLException {
        checkValidity();
        checkAllParametersSet();

        try (LockCloseable ignored = withLock()) {
            final BatchedRowValue batchedValues = new BatchedRowValue(fieldValues.deepCopy());
            for (int i = 0; i < batchedValues.getCount(); i++) {
                if (getField(i + 1) instanceof FBFlushableField flushableField) {
                    batchedValues.setCachedObject(i, flushableField.getCachedObject());
                }
            }

            requireBatch().addBatch(batchedValues);
        }
    }

    @Override
    public void clearBatch() throws SQLException {
        checkValidity();

        try (LockCloseable ignored = withLock()) {
            Batch batch = this.batch;
            if (batch != null) {
                // TODO Find open streams and close them?
                batch.clearBatch();
            }
        }
    }

    @Override
    protected List<Long> executeBatchInternal() throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkValidity();
            notifyStatementStarted();
            try {
                List<Long> results = requireBatch().execute();
                notifyStatementCompleted();
                return results;
            } catch (Exception e) {
                notifyStatementCompleted(false, e);
                throw e;
            }
        }
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
        getField(parameterIndex).setCharacterStream(reader, length);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        getField(parameterIndex).setCharacterStream(reader, length);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        getField(parameterIndex).setCharacterStream(reader);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Jaybird does not support ref types.
     * </p>
     */
    @Override
    public void setRef(int i, Ref x) throws SQLException {
        throw new FBDriverNotCapableException("Type REF not supported");
    }

    @Override
    public void setBlob(int parameterIndex, Blob blob) throws SQLException {
        getField(parameterIndex).setBlob(blob);
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        setBinaryStream(parameterIndex, inputStream, length);
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        setBinaryStream(parameterIndex, inputStream);
    }

    @Override
    public void setClob(int parameterIndex, Clob clob) throws SQLException {
        getField(parameterIndex).setClob(clob);
    }

    @Override
    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        setCharacterStream(parameterIndex, reader, length);
    }

    @Override
    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        setCharacterStream(parameterIndex, reader);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Jaybird does not support array types.
     * </p>
     */
    @Override
    public void setArray(int i, Array x) throws SQLException {
        throw new FBDriverNotCapableException("Type ARRAY not yet supported");
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        checkValidity();
        // TODO Return null for statements without result set?
        return new FBResultSetMetaData(fbStatement.getRowDescriptor(), connection);
    }

    @Override
    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
        getField(parameterIndex).setDate(x, cal);
    }

    @Override
    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        getField(parameterIndex).setTime(x, cal);
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        getField(parameterIndex).setTimestamp(x, cal);
    }

    @Override
    public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
        // all nulls are represented the same irrespective of type
        setNull(parameterIndex, sqlType); 
    }

    /**
     * Prepare fixed statement and initialize parameters.
     */
    @Override
    protected void prepareFixedStatement(String sql) throws SQLException {
        super.prepareFixedStatement(sql);

        RowDescriptor rowDescriptor = fbStatement.getParameterDescriptor();
        assert rowDescriptor != null : "RowDescriptor should not be null after prepare";

        int fieldCount = rowDescriptor.getCount();
        fieldValues = rowDescriptor.createDefaultFieldValues();
        fields = new FBField[fieldCount];

        for (int i = 0; i < fieldCount; i++) {
            final int fieldPosition = i;

            FieldDataProvider dataProvider = new FieldDataProvider() {
                public byte[] getFieldData() {
                    return fieldValues.getFieldData(fieldPosition);
                }

                public void setFieldData(byte[] data) {
                    fieldValues.setFieldData(fieldPosition, data);
                }
            };

            // FIXME check if we can safely pass cached here
            FBField field = FBField.createField(getParameterDescriptor(i + 1), dataProvider, gdsHelper, false);
            if (field instanceof BlobListenableField blobListenableField) {
                blobListenableField.setBlobListener(blobListener);
            }
            fields[i] = field;
        }

        this.isExecuteProcedureStatement = fbStatement.getType() == StatementType.STORED_PROCEDURE;
    }

    @Override
    public String getExecutionPlan() throws SQLException {
        return super.getExecutionPlan();
    }

    @Override
    public String getExplainedExecutionPlan() throws SQLException {
        return super.getExplainedExecutionPlan();
    }

    @Override
    public int getStatementType() throws SQLException {
        return super.getStatementType();
    }

    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        return getFirebirdParameterMetaData();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: This method behaves exactly the same as {@link #setClob(int, Clob)}.
     * </p>
     */
    @Override
    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        setClob(parameterIndex, value);
    }

    @Override
    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        getField(parameterIndex).setRowId(x);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Jaybird does not support SQLXML.
     * </p>
     */
    @Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
        throw new FBDriverNotCapableException("Type SQLXML not supported");
    }
    
    // Methods not allowed to be used on PreparedStatement and CallableStatement
    
    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        throw new SQLNonTransientException(METHOD_NOT_SUPPORTED, SQL_STATE_GENERAL_ERROR);
    }
    
    @Override
    public int executeUpdate(String sql) throws SQLException {
        throw new SQLNonTransientException(METHOD_NOT_SUPPORTED, SQL_STATE_GENERAL_ERROR);
    }
    
    @Override
    public boolean execute(String sql) throws SQLException {
        throw new SQLNonTransientException(METHOD_NOT_SUPPORTED, SQL_STATE_GENERAL_ERROR);
    }
    
    @Override
    public void addBatch(String sql) throws SQLException {
        throw new SQLNonTransientException(METHOD_NOT_SUPPORTED, SQL_STATE_GENERAL_ERROR);
    }
    
    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        throw new SQLNonTransientException(METHOD_NOT_SUPPORTED, SQL_STATE_GENERAL_ERROR);
    }
    
    @Override
    public int executeUpdate(String sql, int[] columnIndex) throws SQLException {
        throw new SQLNonTransientException(METHOD_NOT_SUPPORTED, SQL_STATE_GENERAL_ERROR);
    }
    
    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        throw new SQLNonTransientException(METHOD_NOT_SUPPORTED, SQL_STATE_GENERAL_ERROR);
    }
    
    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        throw new SQLNonTransientException(METHOD_NOT_SUPPORTED, SQL_STATE_GENERAL_ERROR);
    }
    
    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        throw new SQLNonTransientException(METHOD_NOT_SUPPORTED, SQL_STATE_GENERAL_ERROR);
    }
    
    @Override 
    public boolean execute(String sql, String[] columnNames) throws SQLException {
        throw new SQLNonTransientException(METHOD_NOT_SUPPORTED, SQL_STATE_GENERAL_ERROR);
    }

    @Override
    public long executeLargeUpdate() throws SQLException {
        executeUpdate();
        return getLargeUpdateCountMinZero();
    }

    /**
     * This method is for internal implementation use only.
     *
     * @return {@code true} if this prepared statement was initialized (ie: prepared).
     */
    boolean isInitialized() {
        return fields != null;
    }

    private static final class BatchStatementListener implements StatementListener {

        private final List<RowValue> rows;

        private BatchStatementListener(int expectedSize) {
            rows = new ArrayList<>(expectedSize);
        }

        @Override
        public void receivedRow(FbStatement sender, RowValue rowValue) {
            rows.add(rowValue);
        }

        public List<RowValue> getRows() {
            return new ArrayList<>(rows);
        }
    }


    private final class BatchedRowValue implements Batch.BatchRowValue {

        private final RowValue rowValue;
        private Object[] cachedObjects;

        private BatchedRowValue(RowValue rowValue) {
            this.rowValue = rowValue;
        }

        private int getCount() {
            return rowValue.getCount();
        }

        private void setCachedObject(int index, Object object) {
            checkBounds(index);
            if (cachedObjects == null) {
                cachedObjects = new Object[getCount()];
            }
            cachedObjects[index] = object;
        }

        private Object getCachedObject(int index) {
            checkBounds(index);
            if (cachedObjects == null) {
                return null;
            }
            return cachedObjects[index];
        }

        private void checkBounds(int index) {
            if (index < 0 || index >= getCount()) {
                throw new ArrayIndexOutOfBoundsException(index);
            }
        }

        @Override
        public RowValue toRowValue() throws SQLException {
            // NOTE This is basically the old implementation of flushing fields. The use of the fieldValues field is
            // a bit of a kludge, but we use fields for the operation, which are hardwired to it.
            // We may want to see if we can move the flushing down into CachedObject or something like that
            RowValue preservedFieldValues = fieldValues;
            try {
                fieldValues = rowValue;
                for (int i = 0; i < fieldValues.getCount(); i++) {
                    if (getField(i + 1) instanceof FBFlushableField flushableField) {
                        flushableField.setCachedObject((CachedObject) getCachedObject(i));
                    }
                }
                flushFields();
                return rowValue;
            } finally {
                fieldValues = preservedFieldValues;
            }
        }
    }

    /**
     * Emulated batch, which executes row values individually.
     * <p>
     * This implementation is never really closed, and, once instantiated, its lifetime is tied to the parent statement.
     * It does not check whether the statement or batch is closed, instead relying on the caller to check if the
     * statement is closed or not.
     * </p>
     */
    private final class EmulatedPreparedStatementBatch implements Batch {

        private final Deque<BatchRowValue> batchRowValues = new ArrayDeque<>();

        @Override
        public void addBatch(BatchRowValue rowValue) throws SQLException {
            batchRowValues.addLast(rowValue);
        }

        private boolean isEmpty() {
            return batchRowValues.isEmpty();
        }

        @Override
        public List<Long> execute() throws SQLException {
            if (isEmpty()) {
                return emptyList();
            }
            final int size = batchRowValues.size();
            final BatchStatementListener batchStatementListener;
            if (isGeneratedKeyQuery()) {
                batchStatementListener = new BatchStatementListener(size);
                fbStatement.addStatementListener(batchStatementListener);
            } else {
                batchStatementListener = null;
            }
            final List<Long> results = new ArrayList<>(size);
            try {
                Deque<Batch.BatchRowValue> batchRowValues = this.batchRowValues;
                Batch.BatchRowValue batchRowValue;
                while ((batchRowValue = batchRowValues.pollFirst()) != null) {
                    results.add(executeSingleForBatch(batchRowValue));
                }
                return results;
            } catch (SQLException e) {
                throw createBatchUpdateException(e.getMessage(), e.getSQLState(), e.getErrorCode(), results, e);
            } finally {
                currentStatementResult = StatementResult.NO_MORE_RESULTS;
                if (batchStatementListener != null) {
                    fbStatement.removeStatementListener(batchStatementListener);
                    specialResult.clear();
                    specialResult.addAll(batchStatementListener.getRows());
                }
                clearBatch();
            }
        }

        private long executeSingleForBatch(Batch.BatchRowValue batchRowValue) throws SQLException {
            if (internalExecute(batchRowValue.toRowValue())) {
                throw batchStatementReturnedResultSet();
            }
            return getLargeUpdateCountMinZero();
        }

        @Override
        public void clearBatch() {
            batchRowValues.clear();
        }

        @Override
        public void close() {
            clearBatch();
        }
    }
}
