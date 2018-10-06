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

import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.gds.ng.FbStatement;
import org.firebirdsql.gds.ng.StatementType;
import org.firebirdsql.gds.ng.fields.FieldDescriptor;
import org.firebirdsql.gds.ng.fields.RowDescriptor;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.gds.ng.listeners.DefaultStatementListener;
import org.firebirdsql.jdbc.field.FBField;
import org.firebirdsql.jdbc.field.FBFlushableField;
import org.firebirdsql.jdbc.field.FBFlushableField.CachedObject;
import org.firebirdsql.jdbc.field.FBWorkaroundStringField;
import org.firebirdsql.jdbc.field.FieldDataProvider;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.sql.Date;
import java.util.*;

/**
 * Abstract implementation of {@link java.sql.PreparedStatement}.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
@SuppressWarnings("RedundantThrows")
public abstract class AbstractPreparedStatement extends FBStatement implements FirebirdPreparedStatement {

    public static final String METHOD_NOT_SUPPORTED =
            "This method is only supported on Statement and not supported on PreparedStatement and CallableStatement";
    private static final String UNICODE_STREAM_NOT_SUPPORTED = "Unicode stream not supported.";

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

    /**
     * Create instance of this class for the specified result set type and 
     * concurrency. This constructor is used only in {@link FBCallableStatement}
     * since the statement is prepared right before the execution.
     * 
     * @param c instance of {@link GDSHelper} that will be used to perform all
     * database activities.
     * 
     * @param rsType desired result set type.
     * @param rsConcurrency desired result set concurrency.
     * 
     * @param statementListener statement listener that will be notified about
     * the statement start, close and completion.
     * 
     * @throws SQLException if something went wrong.
     */
    protected AbstractPreparedStatement(GDSHelper c, int rsType,
            int rsConcurrency, int rsHoldability,
            FBObjectListener.StatementListener statementListener,
            FBObjectListener.BlobListener blobListener)
            throws SQLException {
        
        super(c, rsType, rsConcurrency, rsHoldability, statementListener);
        this.blobListener = blobListener;
        this.standaloneStatement = false;
        this.metaDataQuery = false;
        this.generatedKeys = false;
    }

    /**
     * Create instance of this class and prepare SQL statement.
     * 
     * @param c
     *            connection to be used.
     * @param sql
     *            SQL statement to prepare.
     * @param rsType
     *            type of result set to create.
     * @param rsConcurrency
     *            result set concurrency.
     * 
     * @throws SQLException
     *             if something went wrong.
     */
    protected AbstractPreparedStatement(GDSHelper c, String sql, int rsType,
            int rsConcurrency, int rsHoldability,
            FBObjectListener.StatementListener statementListener,
            FBObjectListener.BlobListener blobListener,
            boolean metaDataQuery, boolean standaloneStatement, boolean generatedKeys)
            throws SQLException {
        super(c, rsType, rsConcurrency, rsHoldability, statementListener);

        this.blobListener = blobListener;
        this.metaDataQuery = metaDataQuery;
        this.standaloneStatement = standaloneStatement;
        this.generatedKeys = generatedKeys;

        synchronized (c.getSynchronizationObject()) {
            try {
                // TODO See http://tracker.firebirdsql.org/browse/JDBC-352
                notifyStatementStarted();
                prepareFixedStatement(sql);
            } catch (SQLException | RuntimeException e) {
                notifyStatementCompleted(false);
                throw e;
            }
        }
    }
    
    @Override
    public void completeStatement(CompletionReason reason) throws SQLException {
        if (!metaDataQuery) {
            super.completeStatement(reason);
        } else if (!completed) {
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
    public ResultSet executeQuery() throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkValidity();
            notifyStatementStarted();

            if (!internalExecute(isExecuteProcedureStatement))  
                throw new FBSQLException("No resultset for sql", SQLStateConstants.SQL_STATE_NO_RESULT_SET);

            return getResultSet();
        }
    }

    @Override
    public int executeUpdate() throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkValidity();
            notifyStatementStarted();
            try {
                if (internalExecute(isExecuteProcedureStatement) && !generatedKeys) {
                    throw new FBSQLException("Update statement returned results.");
                }
                return getUpdateCount();
            } finally {
                notifyStatementCompleted();
            }
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

    @Override
    public void setShort(int parameterIndex, short x) throws SQLException {
        getField(parameterIndex).setShort(x);
    }

    @Override
    public void setString(int parameterIndex, String x) throws SQLException {
        getField(parameterIndex).setString(x);
    }

    /**
     * Sets the designated parameter to the given String value. This is a
     * workaround for the ambiguous "operation was cancelled" response from the
     * server for when an oversized string is set for a limited-size field. This
     * method sets the string parameter without checking size constraints.
     * 
     * @param parameterIndex
     *            the first parameter is 1, the second is 2, ...
     * @param x
     *            The String value to be set
     * @throws SQLException
     *             if a database access occurs
     */
    public void setStringForced(int parameterIndex, String x) throws SQLException {
        FBField field = getField(parameterIndex);
        if (field instanceof FBWorkaroundStringField) {
            ((FBWorkaroundStringField) field).setStringForced(x);
        } else {
            field.setString(x);
        }
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
            throw new SQLException("Invalid column index: " + columnIndex, SQLStateConstants.SQL_STATE_INVALID_COLUMN);
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
    @Deprecated
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
        synchronized (getSynchronizationObject()) {
            checkValidity();
            notifyStatementStarted();
            
            boolean hasResultSet = internalExecute(isExecuteProcedureStatement);

            if (!hasResultSet) 
                notifyStatementCompleted();

            return hasResultSet;
        }
    }

    /**
     * Execute meta-data query. This method is similar to
     * {@link #executeQuery()}however, it always returns cached result set and
     * strings in the result set are always trimmed (server defines system
     * tables using CHAR data type, but it should be used as VARCHAR).
     * 
     * @return result set corresponding to the specified query.
     * 
     * @throws SQLException
     *             if something went wrong or no result set was available.
     */
    ResultSet executeMetaDataQuery() throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkValidity();
            notifyStatementStarted();

            boolean hasResultSet = internalExecute(isExecuteProcedureStatement);

            if (!hasResultSet)
                throw new FBSQLException("No result set is available.");

            return getResultSet(true);
        }
    }

    /**
     * Execute this statement. Method checks whether all parameters are set,
     * flushes all "flushable" fields that might contain cached data and
     * executes the statement. 
     * 
     * @param sendOutParams Determines if the XSQLDA structure should be sent to the
     *            database
     * @return <code>true</code> if the statement has more result sets. 
     * @throws SQLException
     */
    protected boolean internalExecute(boolean sendOutParams) throws SQLException {
        checkAllParametersSet();

        synchronized (getSynchronizationObject()) {
            flushFields();

            try {
                fbStatement.execute(fieldValues);
                return currentStatementResult == StatementResult.RESULT_SET;
            } catch (SQLException e) {
                currentStatementResult = StatementResult.NO_MORE_RESULTS;
                throw e;
            }
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
    private void flushFields() throws SQLException {
        // flush any cached data that can be hanging
        for (int i = 0; i < fields.length; i++) {
            FBField field = fields[i];

            if (field instanceof FBFlushableField) {
                ((FBFlushableField) field).flushCachedData();
            }
        }
    }

    // TODO: AbstractCallableStatement adds FBProcedureCall, while AbstractPreparedStatement adds RowValue: separate?
    protected final List<Object> batchList = new ArrayList<>();

    @Override
    public void addBatch() throws SQLException {
        checkValidity();
        checkAllParametersSet();

        final BatchedRowValue batchedValues = new BatchedRowValue(fieldValues.deepCopy());
        for (int i = 0; i < batchedValues.getCount(); i++) {
            FBField field = getField(i + 1);
            if (field instanceof FBFlushableField) {
                batchedValues.setCachedObject(i, ((FBFlushableField) field).getCachedObject());
            }
        }

        batchList.add(batchedValues);
    }

    @Override
    public void clearBatch() throws SQLException {
        // TODO Find open streams and close them?
        batchList.clear();
    }

    @Override
    protected List<Long> executeBatchInternal() throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkValidity();
            final BatchStatementListener batchStatementListener;
            boolean commit = false;
            try {
                notifyStatementStarted();

                final int size = batchList.size();
                if (generatedKeys) {
                    batchStatementListener = new BatchStatementListener(size);
                    fbStatement.addStatementListener(batchStatementListener);
                } else {
                    batchStatementListener = null;
                }
                final List<Long> results = new ArrayList<>(size);
                final Iterator<Object> iter = batchList.iterator();

                try {
                    while (iter.hasNext()) {
                        BatchedRowValue data = (BatchedRowValue) iter.next();

                        executeSingleForBatch(data, results);
                    }

                    commit = true;

                    return results;

                } catch (SQLException ex) {
                    throw jdbcVersionSupport.createBatchUpdateException(ex.getMessage(), ex.getSQLState(),
                            ex.getErrorCode(), toLargeArray(results), ex);
                } finally {
                    if (generatedKeys) {
                        fbStatement.removeStatementListener(batchStatementListener);
                        specialResult.clear();
                        specialResult.addAll(batchStatementListener.getRows());
                    }
                    clearBatch();
                }
            } finally {
                notifyStatementCompleted(commit);
            }
        }
    }

    private void executeSingleForBatch(BatchedRowValue data, List<Long> results) throws SQLException {
        fieldValues.reset();
        for (int i = 0; i < fieldValues.getCount(); i++) {
            FBField field = getField(i + 1);
            if (field instanceof FBFlushableField) {
                ((FBFlushableField) field).setCachedObject((CachedObject) data.getCachedObject(i));
            } else {
                fieldValues.setFieldData(i, data.getFieldData(i));
            }
        }

        if (internalExecute(isExecuteProcedureStatement)) {
            throw jdbcVersionSupport.createBatchUpdateException(
                    "Statements executed as batch should not produce a result set",
                    SQLStateConstants.SQL_STATE_INVALID_STMT_TYPE, 0, toLargeArray(results), null);
        }

        results.add(getLargeUpdateCount());
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
        // if the passed BLOB is not instance of our class, copy its content into the our BLOB
        if (blob != null && !(blob instanceof FBBlob)) {
            FBBlob fbb = new FBBlob(gdsHelper, blobListener);
            fbb.copyStream(blob.getBinaryStream());
            blob = fbb;
        } 
        
        getField(parameterIndex).setBlob((FBBlob) blob);
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        FBBlob blob = new FBBlob(gdsHelper, blobListener);
        blob.copyStream(inputStream, length);
        setBlob(parameterIndex, blob);
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        FBBlob blob = new FBBlob(gdsHelper, blobListener);
        blob.copyStream(inputStream);
        setBlob(parameterIndex, blob);
    }

    @Override
    public void setClob(int parameterIndex, Clob clob) throws SQLException {
        // if the passed BLOB is not instance of our class, copy its content into the our BLOB
        if (!(clob instanceof FBClob)) {
            FBClob fbc = new FBClob(new FBBlob(gdsHelper, blobListener));
            fbc.copyCharacterStream(clob.getCharacterStream());
            clob = fbc;
        } 
        
        getField(parameterIndex).setClob((FBClob) clob);
    }

    @Override
    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        FBClob clob = new FBClob(new FBBlob(gdsHelper, blobListener));
        clob.copyCharacterStream(reader, length);
        setClob(parameterIndex, clob);
    }

    @Override
    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        FBClob clob = new FBClob(new FBBlob(gdsHelper, blobListener));
        clob.copyCharacterStream(reader);
        setClob(parameterIndex, clob);
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
        return new FBResultSetMetaData(fbStatement.getFieldDescriptor(), connection);
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
            fields[i] = FBField.createField(getParameterDescriptor(i + 1), dataProvider, gdsHelper, false);
        }

        this.isExecuteProcedureStatement = fbStatement.getType() == StatementType.STORED_PROCEDURE;
    }

    @Override
    public String getExecutionPlan() throws SQLException {
        return super.getExecutionPlan();
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
        throw new FBSQLException(METHOD_NOT_SUPPORTED);
    }
    
    @Override
    public int executeUpdate(String sql) throws SQLException {
        throw new FBSQLException(METHOD_NOT_SUPPORTED);
    }
    
    @Override
    public boolean execute(String sql) throws SQLException {
        throw new FBSQLException(METHOD_NOT_SUPPORTED);
    }
    
    @Override
    public void addBatch(String sql) throws SQLException {
        throw new FBSQLException(METHOD_NOT_SUPPORTED);
    }
    
    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        throw new FBSQLException(METHOD_NOT_SUPPORTED);
    }
    
    @Override
    public int executeUpdate(String sql, int[] columnIndex) throws SQLException {
        throw new FBSQLException(METHOD_NOT_SUPPORTED);
    }
    
    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        throw new FBSQLException(METHOD_NOT_SUPPORTED);
    }
    
    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        throw new FBSQLException(METHOD_NOT_SUPPORTED);
    }
    
    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        throw new FBSQLException(METHOD_NOT_SUPPORTED);
    }
    
    @Override 
    public boolean execute(String sql, String[] columnNames) throws SQLException {
        throw new FBSQLException(METHOD_NOT_SUPPORTED);
    }

    public long executeLargeUpdate() throws SQLException {
        executeUpdate();
        return getLargeUpdateCount();
    }

    /**
     * This method is for internal implementation use only.
     *
     * @return {@code true} if this prepared statement was initialized (ie: prepared).
     */
    boolean isInitialized() {
        return fields != null;
    }

    private static class BatchStatementListener extends DefaultStatementListener {

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


    private static final class BatchedRowValue {

        private final RowValue rowValue;
        private Object[] cachedObjects;

        private BatchedRowValue(RowValue rowValue) {
            this.rowValue = rowValue;
        }

        private int getCount() {
            return rowValue.getCount();
        }

        private byte[] getFieldData(int index) {
            return rowValue.getFieldData(index);
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

    }
}
