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

import org.firebirdsql.gds.ng.LockCloseable;
import org.firebirdsql.gds.ng.StatementType;
import org.firebirdsql.jdbc.escape.FBEscapedCallParser;
import org.firebirdsql.jdbc.field.FBField;
import org.firebirdsql.jdbc.field.TypeConversionException;
import org.firebirdsql.util.InternalApi;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Date;
import java.sql.*;
import java.util.*;

import static java.util.Collections.emptyList;
import static org.firebirdsql.jdbc.SQLStateConstants.SQL_STATE_NO_RESULT_SET;

/**
 * Implementation of {@link java.sql.CallableStatement}.
 * <p>
 * This class is internal API of Jaybird. Future versions may radically change, move, or make inaccessible this type.
 * For the public API, refer to the {@link java.sql.CallableStatement} and {@link FirebirdCallableStatement} interfaces.
 * </p>
 * 
 * @author David Jencks
 * @author Roman Rokytskyy
 * @author Steven Jardine
 * @author Mark Rotteveel
 */
@InternalApi
@NullMarked
public class FBCallableStatement extends FBPreparedStatement implements CallableStatement, FirebirdCallableStatement {

    static final String SET_BY_STRING_NOT_SUPPORTED = "Setting parameters by name is not supported";

    private @Nullable FBResultSet singletonRs;

    protected boolean selectableProcedure;

    protected FBProcedureCall procedureCall;

    protected FBCallableStatement(FBConnection connection, String sql, ResultSetBehavior rsBehavior,
            StoredProcedureMetaData storedProcMetaData, FBObjectListener.StatementListener statementListener,
            FBObjectListener.BlobListener blobListener) throws SQLException {
        super(connection, rsBehavior, statementListener, blobListener);
        var parser = new FBEscapedCallParser();

        // here statement is parsed twice, once in c.nativeSQL(...)
        // and second time in parser.parseCall(...)... not nice, maybe
        // in the future should be fixed by calling FBEscapedParser for
        // each parameter in FBEscapedCallParser class
        // TODO Might be unnecessary now FBEscapedParser processes nested escapes
        procedureCall = parser.parseCall(nativeSQL(sql));

        if (storedProcMetaData.canGetSelectableInformation()) {
            setSelectabilityAutomatically(storedProcMetaData);
        }
    }

    @Override
    public void close() throws SQLException {
        try (var ignored = withLock()) {
            batchList = emptyList();
            super.close();
        }
    }

    @Override
    public FirebirdParameterMetaData getFirebirdParameterMetaData() throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkValidity();
            // TODO See http://tracker.firebirdsql.org/browse/JDBC-352
            notifyStatementStarted(false);
            prepareFixedStatement(procedureCall.getSQL(isSelectableProcedure()));
            return new FBParameterMetaData(fbStatement.getParameterDescriptor(), connection);
        }
    }

    private List<FBProcedureCall> batchList = new ArrayList<>();

    @Override
    public void addBatch() throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkValidity();
            procedureCall.checkParameters();
            batchList.add((FBProcedureCall) procedureCall.clone());
        }
    }

    @Override
    public void clearBatch() throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkValidity();
            // TODO Find open streams and close them?
            batchList.clear();
        }
    }

    @Override
    protected List<Long> executeBatchInternal() throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkValidity();
            List<Long> results = new ArrayList<>(batchList.size());
            notifyStatementStarted();
            try {
                prepareFixedStatement(procedureCall.getSQL(isSelectableProcedure()));
                for (FBProcedureCall fbProcedureCall : batchList) {
                    procedureCall = fbProcedureCall;
                    results.add(executeSingleForBatch());
                }
                notifyStatementCompleted();
                return results;
            } catch (SQLException e) {
                BatchUpdateException batchUpdateException = createBatchUpdateException(e, results);
                notifyStatementCompleted(false, batchUpdateException);
                throw batchUpdateException;
            } catch (RuntimeException e) {
                notifyStatementCompleted(false, e);
                throw e;
            } finally {
                clearBatch();
            }
        }
    }
    
    private long executeSingleForBatch() throws SQLException {
        if (internalExecute(!isSelectableProcedure())) {
            throw batchStatementReturnedResultSet();
        }

        return getLargeUpdateCountMinZero();
    }

    @Override
    public void setSelectableProcedure(boolean selectableProcedure) {
        this.selectableProcedure = selectableProcedure;
    }

    @Override
    public boolean isSelectableProcedure() {
        return selectableProcedure;
    }

    /**
     * Set required types for output parameters.
     * 
     * @throws SQLException if something went wrong.
     */
    protected void setRequiredTypes() throws SQLException {
        FBResultSet rs = singletonRs != null ? singletonRs : getResultSet(false);
        assert rs != null : "a non-null ResultSet is required at this point";
        setRequiredTypesInternal(rs);
    }

    private void setRequiredTypesInternal(FBResultSet resultSet) throws SQLException {
        for (FBProcedureParam param : procedureCall.getOutputParams()) {
            if (param == null) continue;

            resultSet.getField(mapOutParamIndexToPosition(param.getIndex()), false)
                    .setRequiredType(param.getType());
        }
    }

    /**
     * We allow multiple calls to this method without re-preparing the statement.
     * This is a workaround to the issue that the statement is actually prepared
     * only after all OUT parameters are registered.
     */
    @Override
    protected void prepareFixedStatement(String sql) throws SQLException {
        if (fbStatement.getType() != StatementType.NONE) return;

        super.prepareFixedStatement(sql);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Since we deferred the statement preparation until all OUT params are 
     * registered, we ensure that the statement is prepared before the meta
     * data for the callable statement is obtained.
     * </p>
     */
    @Override
    public @Nullable ResultSetMetaData getMetaData() throws SQLException {
        checkValidity();
        try (LockCloseable ignored = withLock()) {
            // TODO See http://tracker.firebirdsql.org/browse/JDBC-352
            notifyStatementStarted(false);
            prepareFixedStatement(procedureCall.getSQL(isSelectableProcedure()));
        }

        return super.getMetaData();
    }

    @Override
    public boolean execute() throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkValidity();
            procedureCall.checkParameters();
            notifyStatementStarted();
            try {
                prepareFixedStatement(procedureCall.getSQL(isSelectableProcedure()));
                boolean hasResultSet = internalExecute(!isSelectableProcedure());
                if (hasResultSet) {
                    setRequiredTypes();
                } else {
                    notifyStatementCompleted();
                }
                return hasResultSet;
            } catch (Exception e) {
                notifyStatementCompleted(true, e);
                throw e;
            }
        }
    }

    //This method prepares statement before execution. Rest of the processing is done by superclass.
    @Override
    public ResultSet executeQuery() throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkValidity();
            procedureCall.checkParameters();
            notifyStatementStarted();
            try {
                prepareFixedStatement(procedureCall.getSQL(isSelectableProcedure()));
                if (!internalExecute(!isSelectableProcedure())) {
                    throw queryProducedNoResultSet();
                }
                FBResultSet rs = getResultSet(false);
                assert rs != null : "a non-null ResultSet is required at this point";
                setRequiredTypesInternal(rs);
                return rs;
            } catch (Exception e) {
                notifyStatementCompleted(true, e);
                throw e;
            }
        }
    }

    // This method prepares statement before execution. Rest of the processing is done by superclass.
    @Override
    public int executeUpdate() throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkValidity();
            procedureCall.checkParameters();
            notifyStatementStarted();
            try {
                prepareFixedStatement(procedureCall.getSQL(isSelectableProcedure()));

                /* R.Rokytskyy: JDBC CTS suite uses executeUpdate() together with output parameters, therefore we
                 * cannot throw exception if we want to pass the test suite.
                 *
                 * if (internalExecute(true)) throw updateReturnedResultSet();
                 */

                boolean hasResults = internalExecute(!isSelectableProcedure());
                if (hasResults) {
                    setRequiredTypes();
                }
                int updateCount = getUpdateCountMinZero();
                if (!isSelectableProcedure()) {
                    notifyStatementCompleted();
                }
                return updateCount;
            } catch (Exception e) {
                notifyStatementCompleted(true, e);
                throw e;
            }
        }
    }

    // Execute statement internally. This method sets cached parameters. Rest of the processing is done by superclass.
    @Override
    protected boolean internalExecute(boolean sendOutParams) throws SQLException {
        singletonRs = null;
        int counter = 0;
        for (FBProcedureParam param : procedureCall.getInputParams()) {
            if (param != null && param.isParam()) {

                counter++;

                Object value = param.getValue();
                FBField field = getField(counter);

                if (value == null) {
                    field.setNull();
                } else if (value instanceof WrapperWithCalendar wrapperWithCalendar) {
                    setField(field, wrapperWithCalendar);
                } else if (value instanceof WrapperWithLong wrapperWithLong) {
                    setField(field, wrapperWithLong);
                } else {
                    field.setObject(value);
                }
            }
        }

        final boolean hasResultSet = super.internalExecute(sendOutParams);
        if (hasResultSet && isSingletonResult) {
            // Safeguarding first row so it will work even if the result set from getResultSet is manipulated
            singletonRs = new FBResultSet(fbStatement.getRowDescriptor(), connection, specialResult, null, true);
        }
        return hasResultSet;
    }

    @Override
    protected FBResultSet createSpecialResultSet(FBObjectListener.@Nullable ResultSetListener resultSetListener)
            throws SQLException {
        // retrieveBlobs is false, as they were already retrieved when initializing singletonRs in internalExecute
        return new FBResultSet(fbStatement.getRowDescriptor(), connection, specialResult, resultSetListener, false);
    }

    private void setField(FBField field, WrapperWithLong value) throws SQLException {
        Object obj = value.value();

        if (obj == null) {
            field.setNull();
        } else {
            long longValue = value.longValue();

            if (obj instanceof InputStream inputStream) {
                field.setBinaryStream(inputStream, longValue);
            } else if (obj instanceof Reader reader) {
                field.setCharacterStream(reader, longValue);
            } else {
                throw new TypeConversionException("Cannot convert type " + obj.getClass().getName());
            }
        }
    }

    private void setField(FBField field, WrapperWithCalendar value) throws SQLException {
        Object obj = value.value();

        if (obj == null) {
            field.setNull();
        } else {
            Calendar cal = value.calendar();

            if (obj instanceof Timestamp timestamp) {
                field.setTimestamp(timestamp, cal);
            } else if (obj instanceof Date date) {
                field.setDate(date, cal);
            } else if (obj instanceof Time time) {
                field.setTime(time, cal);
            } else {
                throw new TypeConversionException("Cannot convert type " + obj.getClass().getName());
            }
        }
    }

    @Override
    public void registerOutParameter(int parameterIndex, int sqlType) throws SQLException {
        procedureCall.registerOutParam(parameterIndex, sqlType);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: This method will behave the same as calling {@link #registerOutParameter(int, int)}.
     * </p>
     */
    @Override
    public void registerOutParameter(int parameterIndex, int sqlType, int scale) throws SQLException {
        registerOutParameter(parameterIndex, sqlType);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: behaves as {@link #registerOutParameter(int, int)} called with
     * {@link SQLType#getVendorTypeNumber()}.
     * </p>
     */
    @Override
    public void registerOutParameter(int parameterIndex, SQLType sqlType) throws SQLException {
        registerOutParameter(parameterIndex, sqlType.getVendorTypeNumber());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: behaves as {@link #registerOutParameter(int, int, int)} called with
     * {@link SQLType#getVendorTypeNumber()}.
     * </p>
     */
    @Override
    public void registerOutParameter(int parameterIndex, SQLType sqlType, int scale) throws SQLException {
        registerOutParameter(parameterIndex, sqlType.getVendorTypeNumber(), scale);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: behaves as {@link #registerOutParameter(int, int, String)} called with
     * {@link SQLType#getVendorTypeNumber()}.
     * </p>
     */
    @Override
    public void registerOutParameter(int parameterIndex, SQLType sqlType, String typeName) throws SQLException {
        registerOutParameter(parameterIndex, sqlType.getVendorTypeNumber(), typeName);
    }

    @Override
    public boolean wasNull() throws SQLException {
        return getAndAssertSingletonResultSet().wasNull();
    }

    private int mapOutParamIndexToPosition(int parameterIndex) throws SQLException {
        return procedureCall.mapOutParamIndexToPosition(parameterIndex);
    }

    @Override
    public @Nullable String getString(int parameterIndex) throws SQLException {
        return getAndAssertSingletonResultSet().getString(mapOutParamIndexToPosition(parameterIndex));
    }

    @Override
    public boolean getBoolean(int parameterIndex) throws SQLException {
        return getAndAssertSingletonResultSet().getBoolean(mapOutParamIndexToPosition(parameterIndex));
    }

    @Override
    public byte getByte(int parameterIndex) throws SQLException {
        return getAndAssertSingletonResultSet().getByte(mapOutParamIndexToPosition(parameterIndex));
    }

    @Override
    public short getShort(int parameterIndex) throws SQLException {
        return getAndAssertSingletonResultSet().getShort(mapOutParamIndexToPosition(parameterIndex));
    }

    @Override
    public int getInt(int parameterIndex) throws SQLException {
        return getAndAssertSingletonResultSet().getInt(mapOutParamIndexToPosition(parameterIndex));
    }

    @Override
    public long getLong(int parameterIndex) throws SQLException {
        return getAndAssertSingletonResultSet().getLong(mapOutParamIndexToPosition(parameterIndex));
    }

    @Override
    public float getFloat(int parameterIndex) throws SQLException {
        return getAndAssertSingletonResultSet().getFloat(mapOutParamIndexToPosition(parameterIndex));
    }

    @Override
    public double getDouble(int parameterIndex) throws SQLException {
        return getAndAssertSingletonResultSet().getDouble(mapOutParamIndexToPosition(parameterIndex));
    }

    @Deprecated(since = "1")
    @Override
    public @Nullable BigDecimal getBigDecimal(int parameterIndex, int scale) throws SQLException {
        return getAndAssertSingletonResultSet().getBigDecimal(mapOutParamIndexToPosition(parameterIndex), scale);
    }

    @Override
    public byte @Nullable [] getBytes(int parameterIndex) throws SQLException {
        return getAndAssertSingletonResultSet().getBytes(mapOutParamIndexToPosition(parameterIndex));
    }

    @Override
    public Date getDate(int parameterIndex) throws SQLException {
        return getAndAssertSingletonResultSet().getDate(mapOutParamIndexToPosition(parameterIndex));
    }

    @Override
    public @Nullable Time getTime(int parameterIndex) throws SQLException {
        return getAndAssertSingletonResultSet().getTime(mapOutParamIndexToPosition(parameterIndex));
    }

    @Override
    public @Nullable Timestamp getTimestamp(int parameterIndex) throws SQLException {
        return getAndAssertSingletonResultSet().getTimestamp(mapOutParamIndexToPosition(parameterIndex));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: the registered type is ignored, and the type derived from the actual datatype will be used.
     * </p>
     */
    @Override
    public @Nullable Object getObject(int parameterIndex) throws SQLException {
        return getAndAssertSingletonResultSet().getObject(mapOutParamIndexToPosition(parameterIndex));
    }

    @Override
    public @Nullable Object getObject(String colName) throws SQLException {
        return getAndAssertSingletonResultSet().getObject(colName);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: the registered type is ignored, and the type derived from the actual datatype will be used.
     * </p>
     */
    @Override
    public @Nullable Object getObject(int parameterIndex, Map<String, Class<?>> map) throws SQLException {
        return getAndAssertSingletonResultSet().getObject(mapOutParamIndexToPosition(parameterIndex), map);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: the registered type is ignored, and the type derived from the actual datatype will be used.
     * </p>
     */
    @Override
    public @Nullable Object getObject(String colName, Map<String, Class<?>> map) throws SQLException {
        return getAndAssertSingletonResultSet().getObject(colName, map);
    }

    @Override
    public <T extends @Nullable Object> @Nullable T getObject(int parameterIndex, Class<T> type) throws SQLException {
        return getAndAssertSingletonResultSet().getObject(mapOutParamIndexToPosition(parameterIndex), type);
    }

    @Override
    public <T extends @Nullable Object> @Nullable T getObject(String parameterName, Class<T> type) throws SQLException {
        return getAndAssertSingletonResultSet().getObject(parameterName, type);
    }

    @Override
    public @Nullable BigDecimal getBigDecimal(int parameterIndex) throws SQLException {
        return getAndAssertSingletonResultSet().getBigDecimal(mapOutParamIndexToPosition(parameterIndex));
    }

    @Override
    public @Nullable Ref getRef(int parameterIndex) throws SQLException {
        return getAndAssertSingletonResultSet().getRef(mapOutParamIndexToPosition(parameterIndex));
    }

    @Override
    public @Nullable Blob getBlob(int parameterIndex) throws SQLException {
        return getAndAssertSingletonResultSet().getBlob(mapOutParamIndexToPosition(parameterIndex));
    }

    @Override
    public @Nullable Clob getClob(int parameterIndex) throws SQLException {
        return getAndAssertSingletonResultSet().getClob(mapOutParamIndexToPosition(parameterIndex));
    }

    @Override
    public @Nullable Array getArray(int parameterIndex) throws SQLException {
        return getAndAssertSingletonResultSet().getArray(mapOutParamIndexToPosition(parameterIndex));
    }

    @Override
    public @Nullable Date getDate(int parameterIndex, @Nullable Calendar cal) throws SQLException {
        return getAndAssertSingletonResultSet().getDate(mapOutParamIndexToPosition(parameterIndex), cal);
    }

    @Override
    public @Nullable Time getTime(int parameterIndex, @Nullable Calendar cal) throws SQLException {
        return getAndAssertSingletonResultSet().getTime(mapOutParamIndexToPosition(parameterIndex), cal);
    }

    @Override
    public @Nullable Timestamp getTimestamp(int parameterIndex, @Nullable Calendar cal) throws SQLException {
        return getAndAssertSingletonResultSet().getTimestamp(mapOutParamIndexToPosition(parameterIndex), cal);
    }

    @Override
    public @Nullable URL getURL(int parameterIndex) throws SQLException {
        return getAndAssertSingletonResultSet().getURL(mapOutParamIndexToPosition(parameterIndex));
    }

    @Override
    public @Nullable String getString(String colName) throws SQLException {
        return getAndAssertSingletonResultSet().getString(colName);
    }

    @Override
    public boolean getBoolean(String colName) throws SQLException {
        return getAndAssertSingletonResultSet().getBoolean(colName);
    }

    @Override
    public byte getByte(String colName) throws SQLException {
        return getAndAssertSingletonResultSet().getByte(colName);
    }

    @Override
    public short getShort(String colName) throws SQLException {
        return getAndAssertSingletonResultSet().getShort(colName);
    }

    @Override
    public int getInt(String colName) throws SQLException {
        return getAndAssertSingletonResultSet().getInt(colName);
    }

    @Override
    public long getLong(String colName) throws SQLException {
        return getAndAssertSingletonResultSet().getLong(colName);
    }

    @Override
    public float getFloat(String colName) throws SQLException {
        return getAndAssertSingletonResultSet().getFloat(colName);
    }

    @Override
    public double getDouble(String colName) throws SQLException {
        return getAndAssertSingletonResultSet().getDouble(colName);
    }

    @Override
    public byte @Nullable [] getBytes(String colName) throws SQLException {
        return getAndAssertSingletonResultSet().getBytes(colName);
    }

    @Override
    public @Nullable Date getDate(String colName) throws SQLException {
        return getAndAssertSingletonResultSet().getDate(colName);
    }

    @Override
    public @Nullable Time getTime(String colName) throws SQLException {
        return getAndAssertSingletonResultSet().getTime(colName);
    }

    @Override
    public @Nullable Timestamp getTimestamp(String colName) throws SQLException {
        return getAndAssertSingletonResultSet().getTimestamp(colName);
    }

    @Override
    public @Nullable BigDecimal getBigDecimal(String colName) throws SQLException {
        return getAndAssertSingletonResultSet().getBigDecimal(colName);
    }

    @Override
    public @Nullable Ref getRef(String colName) throws SQLException {
        return getAndAssertSingletonResultSet().getRef(colName);
    }

    @Override
    public @Nullable Blob getBlob(String colName) throws SQLException {
        return getAndAssertSingletonResultSet().getBlob(colName);
    }

    @Override
    public @Nullable Clob getClob(String colName) throws SQLException {
        return getAndAssertSingletonResultSet().getClob(colName);
    }

    @Override
    public @Nullable Array getArray(String colName) throws SQLException {
        return getAndAssertSingletonResultSet().getArray(colName);
    }

    @Override
    public @Nullable Date getDate(String colName, @Nullable Calendar cal) throws SQLException {
        return getAndAssertSingletonResultSet().getDate(colName, cal);
    }

    @Override
    public @Nullable Time getTime(String colName, @Nullable Calendar cal) throws SQLException {
        return getAndAssertSingletonResultSet().getTime(colName, cal);
    }

    @Override
    public @Nullable Timestamp getTimestamp(String colName, @Nullable Calendar cal) throws SQLException {
        return getAndAssertSingletonResultSet().getTimestamp(colName, cal);
    }

    @Override
    public @Nullable URL getURL(String colName) throws SQLException {
        return getAndAssertSingletonResultSet().getURL(colName);
    }

    @Override
    public @Nullable Reader getCharacterStream(int parameterIndex) throws SQLException {
        return getAndAssertSingletonResultSet().getCharacterStream(mapOutParamIndexToPosition(parameterIndex));
    }

    @Override
    public @Nullable Reader getCharacterStream(String parameterName) throws SQLException {
        return getAndAssertSingletonResultSet().getCharacterStream(parameterName);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: This method behaves exactly the same as {@link #getCharacterStream(int)}.
     * </p>
     */
    @Override
    public @Nullable Reader getNCharacterStream(int parameterIndex) throws SQLException {
        return getAndAssertSingletonResultSet().getNCharacterStream(mapOutParamIndexToPosition(parameterIndex));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: This method behaves exactly the same as {@link #getCharacterStream(String)} .
     * </p>
     */
    @Override
    public @Nullable Reader getNCharacterStream(String parameterName) throws SQLException {
        return getAndAssertSingletonResultSet().getNCharacterStream(parameterName);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: This method behaves exactly the same as {@link #getString(int)}.
     * </p>
     */
    @Override
    public @Nullable String getNString(int parameterIndex) throws SQLException {
        return getAndAssertSingletonResultSet().getNString(mapOutParamIndexToPosition(parameterIndex));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: This method behaves exactly the same as {@link #getString(String)}.
     * </p>
     */
    @Override
    public @Nullable String getNString(String parameterName) throws SQLException {
        return getAndAssertSingletonResultSet().getNString(parameterName);
    }

    @Override
    public void setAsciiStream(String parameterName, @Nullable InputStream x, long length) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setAsciiStream(String parameterName, @Nullable InputStream x) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setBinaryStream(String parameterName, @Nullable InputStream x, long length) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setBinaryStream(String parameterName, @Nullable InputStream x) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setBlob(String parameterName, @Nullable Blob x) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setBlob(String parameterName, @Nullable InputStream inputStream, long length) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setBlob(String parameterName, @Nullable InputStream inputStream) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setCharacterStream(String parameterName, @Nullable Reader reader, long length) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setCharacterStream(String parameterName, @Nullable Reader reader) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setClob(String parameterName, @Nullable Clob x) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setClob(String parameterName, @Nullable Reader reader, long length) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setClob(String parameterName, @Nullable Reader reader) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: This method behaves exactly the same as {@link #setCharacterStream(String, Reader, long)}.
     * </p>
     */
    @Override
    public void setNCharacterStream(String parameterName, @Nullable Reader value, long length) throws SQLException {
        setCharacterStream(parameterName, value, length);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: This method behaves exactly the same as {@link #setCharacterStream(String, Reader)}.
     * </p>
     */
    @Override
    public void setNCharacterStream(String parameterName, @Nullable Reader value) throws SQLException {
        setCharacterStream(parameterName, value);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: This method behaves exactly the same as {@link #setClob(String, Reader, long)}.
     * </p>
     */
    @Override
    public void setNClob(String parameterName, @Nullable Reader reader, long length) throws SQLException {
        setClob(parameterName, reader, length);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: This method behaves exactly the same as {@link #setClob(String, Reader)}.
     * </p>
     */
    @Override
    public void setNClob(String parameterName, @Nullable Reader reader) throws SQLException {
        setClob(parameterName, reader);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: This method behaves exactly the same as {@link #setString(String, String)}.
     * </p>
     */
    @Override
    public void setNString(String parameterName, @Nullable String value) throws SQLException {
        setString(parameterName, value);
    }

    @Override
    public void registerOutParameter(String param1, int param2) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void registerOutParameter(String param1, int param2, int param3) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void registerOutParameter(String param1, int param2, String param3) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: behaves as {@link #registerOutParameter(String, int)} called with
     * {@link SQLType#getVendorTypeNumber()}.
     * </p>
     */
    @Override
    public void registerOutParameter(String parameterName, SQLType sqlType) throws SQLException {
        registerOutParameter(parameterName, sqlType.getVendorTypeNumber());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: behaves as {@link #registerOutParameter(String, int, int)} called with
     * {@link SQLType#getVendorTypeNumber()}.
     * </p>
     */
    @Override
    public void registerOutParameter(String parameterName, SQLType sqlType, int scale) throws SQLException {
        registerOutParameter(parameterName, sqlType.getVendorTypeNumber(), scale);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: behaves as {@link #registerOutParameter(String, int, String)} called with
     * {@link SQLType#getVendorTypeNumber()}.
     * </p>
     */
    @Override
    public void registerOutParameter(String parameterName, SQLType sqlType, String typeName) throws SQLException {
        registerOutParameter(parameterName, sqlType.getVendorTypeNumber(), typeName);
    }

    @Override
    public void setURL(String param1, @Nullable URL param2) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setNull(String param1, int param2) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setBoolean(String param1, boolean param2) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setByte(String param1, byte param2) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setShort(String param1, short param2) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setInt(String param1, int param2) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setLong(String param1, long param2) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setFloat(String param1, float param2) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setDouble(String param1, double param2) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setBigDecimal(String param1, @Nullable BigDecimal param2) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setString(String param1, @Nullable String param2) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setBytes(String param1, byte @Nullable [] param2) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setDate(String param1, @Nullable Date param2) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setTime(String param1, @Nullable Time param2) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setTimestamp(String param1, @Nullable Timestamp param2) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setAsciiStream(String param1, @Nullable InputStream param2, int param3) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setBinaryStream(String param1, @Nullable InputStream param2, int param3) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setObject(String param1, @Nullable Object param2, int param3, int param4) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setObject(String param1, @Nullable Object param2, int param3) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setObject(String param1, @Nullable Object param2) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setCharacterStream(String param1, @Nullable Reader param2, int param3) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setDate(String param1, @Nullable Date param2, @Nullable Calendar param3) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setTime(String param1, @Nullable Time param2, @Nullable Calendar param3) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setTimestamp(String param1, @Nullable Timestamp param2, @Nullable Calendar param3) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setNull(String param1, int param2, String param3) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void registerOutParameter(int parameterIndex, int sqlType, String typeName) throws SQLException {
        // TODO Can we implement this, how?
        throw new FBDriverNotCapableException();
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        throw new FBDriverNotCapableException("getGeneratedKeys is not supported on CallableStatement");
    }

    /**
     * Asserts if the current statement has data to return. It checks if the result set has a row with data.
     *
     * @param rs
     *         result set to test
     * @return non-{@code null} result set (same object as {@code rs})
     * @throws java.sql.SQLException
     *         when the result set has no data.
     */
    protected ResultSet assertHasData(@Nullable ResultSet rs) throws SQLException {
        if (rs == null) {
            throw new SQLException("Current statement has no data to return", SQL_STATE_NO_RESULT_SET);
        }
        // check if we have a row, and try to move to the first position.
        if (rs.getRow() == 0) {
            rs.next();
        } else {
            return rs;
        }

        // check if we still have no row and throw an exception in this case.
        if (rs.getRow() == 0) {
            throw new SQLException("Current statement has no data to return", SQL_STATE_NO_RESULT_SET);
        }
        return rs;
    }

    /**
     * Returns the result set for the singleton row of the callable statement and asserts it has data. If this is a
     * selectable procedure, or there is no singleton row, it will return the normal result set.
     * <p>
     * This should fix the problem described in <a href="http://tracker.firebirdsql.org/browse/JDBC-350">JDBC-350</a>
     * in most circumstances.
     * </p>
     *
     * @return Either the singleton result set, or the current result set as described above
     * @throws SQLException For database access errors
     */
    protected ResultSet getAndAssertSingletonResultSet() throws SQLException {
        return assertHasData(!isSelectableProcedure() && singletonRs != null ? singletonRs : getResultSet());
    }

    private void setInputParam(int parameterIndex, @Nullable Object value) throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(value);
    }

    @Override
    public void setBigDecimal(int parameterIndex, @Nullable BigDecimal x) throws SQLException {
        setInputParam(parameterIndex, x);
    }

    @Override
    public void setBinaryStream(int parameterIndex, @Nullable InputStream inputStream, int length) throws SQLException {
        setInputParam(parameterIndex, new WrapperWithLong(inputStream, length));
    }

    @Override
    public void setBinaryStream(int parameterIndex, @Nullable InputStream inputStream, long length)
            throws SQLException {
        setInputParam(parameterIndex, new WrapperWithLong(inputStream, length));
    }

    @Override
    public void setBinaryStream(int parameterIndex, @Nullable InputStream inputStream) throws SQLException {
        setInputParam(parameterIndex, inputStream);
    }

    @Override
    public void setBlob(int parameterIndex, @Nullable Blob blob) throws SQLException {
        setInputParam(parameterIndex, blob);
    }

    @Override
    public void setBlob(int parameterIndex, @Nullable InputStream inputStream, long length) throws SQLException {
        setInputParam(parameterIndex, new WrapperWithLong(inputStream, length));
    }

    @Override
    public void setBlob(int parameterIndex, @Nullable InputStream inputStream) throws SQLException {
        setInputParam(parameterIndex, inputStream);
    }

    @Override
    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        setInputParam(parameterIndex, x);
    }

    @Override
    public void setByte(int parameterIndex, byte x) throws SQLException {
        setInputParam(parameterIndex, x);
    }

    @Override
    public void setBytes(int parameterIndex, byte @Nullable [] x) throws SQLException {
        setInputParam(parameterIndex, x);
    }

    @Override
    public void setCharacterStream(int parameterIndex, @Nullable Reader reader, int length) throws SQLException {
        setInputParam(parameterIndex, new WrapperWithLong(reader, length));
    }

    @Override
    public void setCharacterStream(int parameterIndex, @Nullable Reader reader, long length) throws SQLException {
        setInputParam(parameterIndex, new WrapperWithLong(reader, length));
    }

    @Override
    public void setCharacterStream(int parameterIndex, @Nullable Reader reader) throws SQLException {
        setInputParam(parameterIndex, reader);
    }

    @Override
    public void setClob(int parameterIndex, @Nullable Clob x) throws SQLException {
        setInputParam(parameterIndex, x);
    }

    @Override
    public void setClob(int parameterIndex, @Nullable Reader reader, long length) throws SQLException {
        setInputParam(parameterIndex, new WrapperWithLong(reader, length));
    }

    @Override
    public void setClob(int parameterIndex, @Nullable Reader reader) throws SQLException {
        setInputParam(parameterIndex, reader);
    }

    @Override
    public void setDate(int parameterIndex, @Nullable Date x, @Nullable Calendar cal) throws SQLException {
        setInputParam(parameterIndex, new WrapperWithCalendar(x, cal));
    }

    @Override
    public void setDate(int parameterIndex, @Nullable Date x) throws SQLException {
        setInputParam(parameterIndex, x);
    }

    @Override
    public void setDouble(int parameterIndex, double x) throws SQLException {
        setInputParam(parameterIndex, x);
    }

    @Override
    public void setFloat(int parameterIndex, float x) throws SQLException {
        setInputParam(parameterIndex, x);
    }

    @Override
    public void setInt(int parameterIndex, int x) throws SQLException {
        setInputParam(parameterIndex, x);
    }

    @Override
    public void setLong(int parameterIndex, long x) throws SQLException {
        setInputParam(parameterIndex, x);
    }

    @Override
    public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
        setInputParam(parameterIndex, null);
    }

    @Override
    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        setInputParam(parameterIndex, null);
    }

    @Override
    public void setObject(int parameterIndex, @Nullable Object x, int targetSqlType, int scale) throws SQLException {
        setInputParam(parameterIndex, x);
    }

    @Override
    public void setObject(int parameterIndex, @Nullable Object x, int targetSqlType) throws SQLException {
        setInputParam(parameterIndex, x);
    }

    @Override
    public void setObject(int parameterIndex, @Nullable Object x) throws SQLException {
        setInputParam(parameterIndex, x);
    }

    @Override
    public void setShort(int parameterIndex, short x) throws SQLException {
        setInputParam(parameterIndex, x);
    }

    @Override
    public void setString(int parameterIndex, @Nullable String x) throws SQLException {
        setInputParam(parameterIndex, x);
    }

    @Override
    public void setTime(int parameterIndex, @Nullable Time x, @Nullable Calendar cal) throws SQLException {
        setInputParam(parameterIndex, new WrapperWithCalendar(x, cal));
    }

    @Override
    public void setTime(int parameterIndex, @Nullable Time x) throws SQLException {
        setInputParam(parameterIndex, x);
    }

    @Override
    public void setTimestamp(int parameterIndex, @Nullable Timestamp x, @Nullable Calendar cal) throws SQLException {
        setInputParam(parameterIndex, new WrapperWithCalendar(x, cal));
    }

    @Override
    public void setTimestamp(int parameterIndex, @Nullable Timestamp x) throws SQLException {
        setInputParam(parameterIndex, x);
    }

    /**
     * Set the selectability of this stored procedure from RDB$PROCEDURE_TYPE.
     *
     * @throws SQLException If no selectability information is available
     */
    private void setSelectabilityAutomatically(StoredProcedureMetaData storedProcMetaData)
            throws SQLException {
        selectableProcedure = storedProcMetaData.isSelectable(procedureCall.getName());
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: This method behaves exactly the same as {@link #getClob(int)}.
     * </p>
     */
    @Override
    public @Nullable NClob getNClob(int parameterIndex) throws SQLException {
        return getAndAssertSingletonResultSet().getNClob(mapOutParamIndexToPosition(parameterIndex));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: This method behaves exactly the same as {@link #getClob(String)}.
     * </p>
     */
    @Override
    public @Nullable NClob getNClob(String parameterName) throws SQLException {
        return getAndAssertSingletonResultSet().getNClob(parameterName);
    }

    @Override
    public @Nullable RowId getRowId(int parameterIndex) throws SQLException {
        return getAndAssertSingletonResultSet().getRowId(mapOutParamIndexToPosition(parameterIndex));
    }

    @Override
    public @Nullable RowId getRowId(String parameterName) throws SQLException {
        return getAndAssertSingletonResultSet().getRowId(parameterName);
    }

    @Override
    public @Nullable SQLXML getSQLXML(int parameterIndex) throws SQLException {
        return getAndAssertSingletonResultSet().getSQLXML(mapOutParamIndexToPosition(parameterIndex));
    }

    @Override
    public @Nullable SQLXML getSQLXML(String parameterName) throws SQLException {
        return getAndAssertSingletonResultSet().getSQLXML(parameterName);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: This method behaves exactly the same as {@link #setClob(String, Clob)}.
     * </p>
     */
    @Override
    public void setNClob(String parameterName, @Nullable NClob value) throws SQLException {
        setClob(parameterName, value);
    }

    @Override
    public void setRowId(String parameterName, @Nullable RowId x) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setSQLXML(String parameterName, @Nullable SQLXML xmlObject) throws SQLException {
        throw new FBDriverNotCapableException("Type SQLXML not supported");
    }

    private record WrapperWithCalendar(@Nullable Object value, @Nullable Calendar calendar) {
    }

    private record WrapperWithLong(@Nullable Object value, long longValue) {
    }

}
