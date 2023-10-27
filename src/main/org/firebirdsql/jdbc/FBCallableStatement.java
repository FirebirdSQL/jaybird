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
import org.firebirdsql.gds.ng.LockCloseable;
import org.firebirdsql.jaybird.util.Primitives;
import org.firebirdsql.jdbc.escape.FBEscapedCallParser;
import org.firebirdsql.jdbc.field.FBField;
import org.firebirdsql.jdbc.field.TypeConversionException;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Date;
import java.sql.*;
import java.util.*;

import static org.firebirdsql.jdbc.SQLStateConstants.SQL_STATE_NO_RESULT_SET;

/**
 * Implementation of {@link java.sql.CallableStatement}.
 * 
 * @author David Jencks
 * @author Roman Rokytskyy
 * @author Steven Jardine
 * @author Mark Rotteveel
 */
public class FBCallableStatement extends FBPreparedStatement implements CallableStatement, FirebirdCallableStatement {

    static final String SET_BY_STRING_NOT_SUPPORTED = "Setting parameters by name is not supported";
    static final String NATIVE_CALL_COMMAND = "EXECUTE PROCEDURE ";
    static final String NATIVE_SELECT_COMMAND = "SELECT * FROM ";

    private ResultSet singletonRs;

    protected boolean selectableProcedure;

    protected FBProcedureCall procedureCall;

    protected FBCallableStatement(GDSHelper c, String sql, int rsType,
            int rsConcurrency, int rsHoldability, 
            StoredProcedureMetaData storedProcMetaData,
            FBObjectListener.StatementListener statementListener, 
            FBObjectListener.BlobListener blobListener) 
    throws SQLException {
        super(c, rsType, rsConcurrency, rsHoldability, statementListener, blobListener);

        FBEscapedCallParser parser = new FBEscapedCallParser();

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
    void close(boolean ignoreAlreadyClosed) throws SQLException {
        batchList = null;
        super.close(ignoreAlreadyClosed);
    }

    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        try (LockCloseable ignored = withLock()) {
            // TODO See http://tracker.firebirdsql.org/browse/JDBC-352
            notifyStatementStarted(false);
            prepareFixedStatement(procedureCall.getSQL(isSelectableProcedure()));
            return new FBParameterMetaData(fbStatement.getParameterDescriptor(), connection);
        }
    }

    private List<FBProcedureCall> batchList = new ArrayList<>();

    @Override
    public void addBatch() throws SQLException {
        checkValidity();
        try (LockCloseable ignored = withLock()) {
            procedureCall.checkParameters();
            batchList.add((FBProcedureCall) procedureCall.clone());
        }
    }

    @Override
    public void clearBatch() throws SQLException {
        checkValidity();

        try (LockCloseable ignored = withLock()) {
            // TODO Find open streams and close them?
            batchList.clear();
        }
    }

    @Override
    protected List<Long> executeBatchInternal() throws SQLException {
        checkValidity();
        try (LockCloseable ignored = withLock()) {
            boolean success = false;
            try {
                notifyStatementStarted();

                List<Long> results = new ArrayList<>(batchList.size());
                Iterator<FBProcedureCall> iterator = batchList.iterator();

                try {
                    prepareFixedStatement(procedureCall.getSQL(isSelectableProcedure()));

                    while (iterator.hasNext()) {
                        procedureCall = iterator.next();
                        executeSingleForBatch(results);
                    }

                    success = true;
                    return results;
                } catch (SQLException ex) {
                    throw createBatchUpdateException(ex.getMessage(), ex.getSQLState(),
                            ex.getErrorCode(), Primitives.toLongArray(results), ex);
                } finally {
                    clearBatch();
                }
            } finally {
                notifyStatementCompleted(success);
            }
        }
    }
    
    private void executeSingleForBatch(List<Long> results) throws SQLException {
        if (internalExecute(!isSelectableProcedure())) {
            throw new SQLException("Statements executed as batch should not produce a result set",
                    SQLStateConstants.SQL_STATE_INVALID_STMT_TYPE);
        }

        results.add(getLargeUpdateCountMinZero());
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
        setRequiredTypesInternal((FBResultSet) (singletonRs != null ? singletonRs : getResultSet()));
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
        if (fbStatement != null) return;

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
    public ResultSetMetaData getMetaData() throws SQLException {
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
        procedureCall.checkParameters();
        boolean hasResultSet = false;
        try (LockCloseable ignored = withLock()) {
            notifyStatementStarted();

            try {
                prepareFixedStatement(procedureCall.getSQL(isSelectableProcedure()));
                hasResultSet = internalExecute(!isSelectableProcedure());

                if (hasResultSet) {
                    setRequiredTypes();
                }
            } finally {
            	if (!hasResultSet) notifyStatementCompleted();
            }

            return hasResultSet;
        }

    }

    //This method prepares statement before execution. Rest of the processing is done by superclass.
    @Override
    public ResultSet executeQuery() throws SQLException {
        procedureCall.checkParameters();
        try (LockCloseable ignored = withLock()) {
            checkValidity();
            notifyStatementStarted();
            prepareFixedStatement(procedureCall.getSQL(isSelectableProcedure()));

            if (!internalExecute(!isSelectableProcedure())) {
                throw new SQLNonTransientException("No resultset for sql", SQL_STATE_NO_RESULT_SET);
            }

            ResultSet rs = getResultSet();
            setRequiredTypesInternal((FBResultSet) rs);
            return rs;
        }
    }

    // This method prepares statement before execution. Rest of the processing is done by superclass.
    @Override
    public int executeUpdate() throws SQLException {
        procedureCall.checkParameters();
        try (LockCloseable ignored = withLock()) {
            try {
                notifyStatementStarted();
                prepareFixedStatement(procedureCall.getSQL(isSelectableProcedure()));

                /*
                 * // R.Rokytskyy: JDBC CTS suite uses executeUpdate() //
                 * together with output parameters, therefore we cannot //
                 * throw exception if we want to pass the test suite
                 * 
                 * if (internalExecute(true)) throw new FBSQLException(
                 * "Update statement returned result set");
                 */

                boolean hasResults = internalExecute(!isSelectableProcedure());

                if (hasResults) {
                    setRequiredTypes();
                }

                return getUpdateCountMinZero();
            } finally {
                notifyStatementCompleted();
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
                } else if (value instanceof WrapperWithCalendar) {
                    setField(field, (WrapperWithCalendar)value);
                } else if (value instanceof WrapperWithLong) {
                    setField(field, (WrapperWithLong)value);
                } else {
                    field.setObject(value);
                }
            }
        }

        final boolean hasResultSet = super.internalExecute(sendOutParams);
        if (hasResultSet && isSingletonResult) {
            // Safeguarding first row so it will work even if the result set from getResultSet is manipulated
            singletonRs = new FBResultSet(fbStatement.getRowDescriptor(), connection, new ArrayList<>(specialResult),
                    null, true, false);
        }
        return hasResultSet;
    }

    @Override
    protected FBResultSet createSpecialResultSet(FBObjectListener.ResultSetListener resultSetListener)
            throws SQLException {
        // retrieveBlobs is false, as they were already retrieved when initializing singletonRs in internalExecute
        return new FBResultSet(fbStatement.getRowDescriptor(), connection, new ArrayList<>(specialResult),
                resultSetListener, false, false);
    }

    private void setField(FBField field, WrapperWithLong value) throws SQLException {
        Object obj = value.value();

        if (obj == null) {
            field.setNull();
        } else {
            long longValue = value.longValue();

            if (obj instanceof InputStream) {
                field.setBinaryStream((InputStream) obj, longValue);
            } else if (obj instanceof Reader) {
                field.setCharacterStream((Reader) obj, longValue);
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

            if (obj instanceof Timestamp)
                field.setTimestamp((Timestamp) obj, cal);
            else if (obj instanceof java.sql.Date)
                field.setDate((java.sql.Date) obj, cal);
            else if (obj instanceof Time)
                field.setTime((Time) obj, cal);
            else
                throw new TypeConversionException("Cannot convert type " + obj.getClass().getName());
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
    public String getString(int parameterIndex) throws SQLException {
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

    @Deprecated
    @Override
    public BigDecimal getBigDecimal(int parameterIndex, int scale) throws SQLException {
        return getAndAssertSingletonResultSet().getBigDecimal(mapOutParamIndexToPosition(parameterIndex), scale);
    }

    @Override
    public byte[] getBytes(int parameterIndex) throws SQLException {
        return getAndAssertSingletonResultSet().getBytes(mapOutParamIndexToPosition(parameterIndex));
    }

    @Override
    public java.sql.Date getDate(int parameterIndex) throws SQLException {
        return getAndAssertSingletonResultSet().getDate(mapOutParamIndexToPosition(parameterIndex));
    }

    @Override
    public Time getTime(int parameterIndex) throws SQLException {
        return getAndAssertSingletonResultSet().getTime(mapOutParamIndexToPosition(parameterIndex));
    }

    @Override
    public Timestamp getTimestamp(int parameterIndex) throws SQLException {
        return getAndAssertSingletonResultSet().getTimestamp(mapOutParamIndexToPosition(parameterIndex));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: the registered type is ignored, and the type derived from the actual datatype will be used.
     * </p>
     */
    @Override
    public Object getObject(int parameterIndex) throws SQLException {
        return getAndAssertSingletonResultSet().getObject(mapOutParamIndexToPosition(parameterIndex));
    }

    @Override
    public Object getObject(String colName) throws SQLException {
        return getAndAssertSingletonResultSet().getObject(colName);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: the registered type is ignored, and the type derived from the actual datatype will be used.
     * </p>
     */
    @Override
    public Object getObject(int parameterIndex, Map<String, Class<?>> map) throws SQLException {
        return getAndAssertSingletonResultSet().getObject(mapOutParamIndexToPosition(parameterIndex), map);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: the registered type is ignored, and the type derived from the actual datatype will be used.
     * </p>
     */
    @Override
    public Object getObject(String colName, Map<String, Class<?>> map) throws SQLException {
        return getAndAssertSingletonResultSet().getObject(colName, map);
    }

    @Override
    public <T> T getObject(int parameterIndex, Class<T> type) throws SQLException {
        return getAndAssertSingletonResultSet().getObject(mapOutParamIndexToPosition(parameterIndex), type);
    }

    @Override
    public <T> T getObject(String parameterName, Class<T> type) throws SQLException {
        return getAndAssertSingletonResultSet().getObject(parameterName, type);
    }

    @Override
    public BigDecimal getBigDecimal(int parameterIndex) throws SQLException {
        return getAndAssertSingletonResultSet().getBigDecimal(mapOutParamIndexToPosition(parameterIndex));
    }

    @Override
    public Ref getRef(int parameterIndex) throws SQLException {
        return getAndAssertSingletonResultSet().getRef(mapOutParamIndexToPosition(parameterIndex));
    }

    @Override
    public Blob getBlob(int parameterIndex) throws SQLException {
        return getAndAssertSingletonResultSet().getBlob(mapOutParamIndexToPosition(parameterIndex));
    }

    @Override
    public Clob getClob(int parameterIndex) throws SQLException {
        return getAndAssertSingletonResultSet().getClob(mapOutParamIndexToPosition(parameterIndex));
    }

    @Override
    public Array getArray(int parameterIndex) throws SQLException {
        return getAndAssertSingletonResultSet().getArray(mapOutParamIndexToPosition(parameterIndex));
    }

    @Override
    public java.sql.Date getDate(int parameterIndex, Calendar cal) throws SQLException {
        return getAndAssertSingletonResultSet().getDate(mapOutParamIndexToPosition(parameterIndex), cal);
    }

    @Override
    public Time getTime(int parameterIndex, Calendar cal) throws SQLException {
        return getAndAssertSingletonResultSet().getTime(mapOutParamIndexToPosition(parameterIndex), cal);
    }

    @Override
    public Timestamp getTimestamp(int parameterIndex, Calendar cal) throws SQLException {
        return getAndAssertSingletonResultSet().getTimestamp(mapOutParamIndexToPosition(parameterIndex), cal);
    }

    @Override
    public URL getURL(int parameterIndex) throws SQLException {
        return getAndAssertSingletonResultSet().getURL(mapOutParamIndexToPosition(parameterIndex));
    }

    @Override
    public String getString(String colName) throws SQLException {
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
    public byte[] getBytes(String colName) throws SQLException {
        return getAndAssertSingletonResultSet().getBytes(colName);
    }

    @Override
    public Date getDate(String colName) throws SQLException {
        return getAndAssertSingletonResultSet().getDate(colName);
    }

    @Override
    public Time getTime(String colName) throws SQLException {
        return getAndAssertSingletonResultSet().getTime(colName);
    }

    @Override
    public Timestamp getTimestamp(String colName) throws SQLException {
        return getAndAssertSingletonResultSet().getTimestamp(colName);
    }

    @Override
    public BigDecimal getBigDecimal(String colName) throws SQLException {
        return getAndAssertSingletonResultSet().getBigDecimal(colName);
    }

    @Override
    public Ref getRef(String colName) throws SQLException {
        return getAndAssertSingletonResultSet().getRef(colName);
    }

    @Override
    public Blob getBlob(String colName) throws SQLException {
        return getAndAssertSingletonResultSet().getBlob(colName);
    }

    @Override
    public Clob getClob(String colName) throws SQLException {
        return getAndAssertSingletonResultSet().getClob(colName);
    }

    @Override
    public Array getArray(String colName) throws SQLException {
        return getAndAssertSingletonResultSet().getArray(colName);
    }

    @Override
    public Date getDate(String colName, Calendar cal) throws SQLException {
        return getAndAssertSingletonResultSet().getDate(colName, cal);
    }

    @Override
    public Time getTime(String colName, Calendar cal) throws SQLException {
        return getAndAssertSingletonResultSet().getTime(colName, cal);
    }

    @Override
    public Timestamp getTimestamp(String colName, Calendar cal) throws SQLException {
        return getAndAssertSingletonResultSet().getTimestamp(colName, cal);
    }

    @Override
    public URL getURL(String colName) throws SQLException {
        return getAndAssertSingletonResultSet().getURL(colName);
    }

    @Override
    public Reader getCharacterStream(int parameterIndex) throws SQLException {
        return getAndAssertSingletonResultSet().getCharacterStream(mapOutParamIndexToPosition(parameterIndex));
    }

    @Override
    public Reader getCharacterStream(String parameterName) throws SQLException {
        return getAndAssertSingletonResultSet().getCharacterStream(parameterName);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: This method behaves exactly the same as {@link #getCharacterStream(int)}.
     * </p>
     */
    @Override
    public Reader getNCharacterStream(int parameterIndex) throws SQLException {
        return getAndAssertSingletonResultSet().getNCharacterStream(mapOutParamIndexToPosition(parameterIndex));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: This method behaves exactly the same as {@link #getCharacterStream(String)} .
     * </p>
     */
    @Override
    public Reader getNCharacterStream(String parameterName) throws SQLException {
        return getAndAssertSingletonResultSet().getNCharacterStream(parameterName);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: This method behaves exactly the same as {@link #getString(int)}.
     * </p>
     */
    @Override
    public String getNString(int parameterIndex) throws SQLException {
        return getAndAssertSingletonResultSet().getNString(mapOutParamIndexToPosition(parameterIndex));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: This method behaves exactly the same as {@link #getString(String)}.
     * </p>
     */
    @Override
    public String getNString(String parameterName) throws SQLException {
        return getAndAssertSingletonResultSet().getNString(parameterName);
    }

    @Override
    public void setAsciiStream(String parameterName, InputStream x, long length) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setAsciiStream(String parameterName, InputStream x) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setBinaryStream(String parameterName, InputStream x, long length) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setBinaryStream(String parameterName, InputStream x) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setBlob(String parameterName, Blob x) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setBlob(String parameterName, InputStream inputStream, long length) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setBlob(String parameterName, InputStream inputStream) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setCharacterStream(String parameterName, Reader reader, long length) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setCharacterStream(String parameterName, Reader reader) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setClob(String parameterName, Clob x) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setClob(String parameterName, Reader reader, long length) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setClob(String parameterName, Reader reader) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: This method behaves exactly the same as {@link #setCharacterStream(String, Reader, long)}.
     * </p>
     */
    @Override
    public void setNCharacterStream(String parameterName, Reader value, long length) throws SQLException {
        setCharacterStream(parameterName, value, length);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: This method behaves exactly the same as {@link #setCharacterStream(String, Reader)}.
     * </p>
     */
    @Override
    public void setNCharacterStream(String parameterName, Reader value) throws SQLException {
        setCharacterStream(parameterName, value);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: This method behaves exactly the same as {@link #setClob(String, Reader, long)}.
     * </p>
     */
    @Override
    public void setNClob(String parameterName, Reader reader, long length) throws SQLException {
        setClob(parameterName, reader, length);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: This method behaves exactly the same as {@link #setClob(String, Reader)}.
     * </p>
     */
    @Override
    public void setNClob(String parameterName, Reader reader) throws SQLException {
        setClob(parameterName, reader);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: This method behaves exactly the same as {@link #setString(String, String)}.
     * </p>
     */
    @Override
    public void setNString(String parameterName, String value) throws SQLException {
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
    public void setURL(String param1, URL param2) throws SQLException {
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
    public void setBigDecimal(String param1, BigDecimal param2) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setString(String param1, String param2) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setBytes(String param1, byte[] param2) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setDate(String param1, Date param2) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setTime(String param1, Time param2) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setTimestamp(String param1, Timestamp param2) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setAsciiStream(String param1, InputStream param2, int param3) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setBinaryStream(String param1, InputStream param2, int param3) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setObject(String param1, Object param2, int param3, int param4) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setObject(String param1, Object param2, int param3) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setObject(String param1, Object param2) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setCharacterStream(String param1, Reader param2, int param3) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setDate(String param1, Date param2, Calendar param3) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setTime(String param1, Time param2, Calendar param3) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setTimestamp(String param1, Timestamp param2, Calendar param3) throws SQLException {
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
     * Asserts if the current statement has data to return. It checks if the
     * result set has a row with data.
     *
     * @param rs result set to test
     * @throws java.sql.SQLException when the result set has no data.
     */
    protected void assertHasData(ResultSet rs) throws SQLException {
        if (rs == null) {
            throw new SQLException("Current statement has no data to return", SQL_STATE_NO_RESULT_SET);
        }
        // check if we have a row, and try to move to the first position.
        if (rs.getRow() == 0) {
            rs.next();
        } else {
            return;
        }

        // check if we still have no row and throw an exception in this case.
        if (rs.getRow() == 0) {
            throw new SQLException("Current statement has no data to return", SQL_STATE_NO_RESULT_SET);
        }
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
        final ResultSet rs = !isSelectableProcedure() && singletonRs != null ? singletonRs : getResultSet();
        assertHasData(rs);
        return rs;
    }

    private void setInputParam(int parameterIndex, Object value) throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(value);
    }

    @Override
    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        setInputParam(parameterIndex, x);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream inputStream, int length) throws SQLException {
        setInputParam(parameterIndex, new WrapperWithLong(inputStream, length));
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        setInputParam(parameterIndex, new WrapperWithLong(inputStream, length));
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream inputStream) throws SQLException {
        setInputParam(parameterIndex, inputStream);
    }

    @Override
    public void setBlob(int parameterIndex, Blob blob) throws SQLException {
        setInputParam(parameterIndex, blob);
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        setInputParam(parameterIndex, new WrapperWithLong(inputStream, length));
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
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
    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        setInputParam(parameterIndex, x);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
        setInputParam(parameterIndex, new WrapperWithLong(reader, length));
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        setInputParam(parameterIndex, new WrapperWithLong(reader, length));
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        setInputParam(parameterIndex, reader);
    }

    @Override
    public void setClob(int parameterIndex, Clob x) throws SQLException {
        setInputParam(parameterIndex, x);
    }

    @Override
    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        setInputParam(parameterIndex, new WrapperWithLong(reader, length));
    }

    @Override
    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        setInputParam(parameterIndex, reader);
    }

    @Override
    public void setDate(int parameterIndex, java.sql.Date x, Calendar cal) throws SQLException {
        setInputParam(parameterIndex, new WrapperWithCalendar(x, cal));
    }

    @Override
    public void setDate(int parameterIndex, java.sql.Date x) throws SQLException {
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
    public void setObject(int parameterIndex, Object x, int targetSqlType, int scale) throws SQLException {
        setInputParam(parameterIndex, x);
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        setInputParam(parameterIndex, x);
    }

    @Override
    public void setObject(int parameterIndex, Object x) throws SQLException {
        setInputParam(parameterIndex, x);
    }

    @Override
    public void setShort(int parameterIndex, short x) throws SQLException {
        setInputParam(parameterIndex, x);
    }

    @Override
    public void setString(int parameterIndex, String x) throws SQLException {
        setInputParam(parameterIndex, x);
    }

    @Override
    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        setInputParam(parameterIndex, new WrapperWithCalendar(x, cal));
    }

    @Override
    public void setTime(int parameterIndex, Time x) throws SQLException {
        setInputParam(parameterIndex, x);
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        setInputParam(parameterIndex, new WrapperWithCalendar(x, cal));
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        setInputParam(parameterIndex, x);
    }

    /**
     * Set the selectability of this stored procedure from RDB$PROCEDURE_TYPE.
     *
     * @throws SQLException If no selectability information is available
     */
    private void setSelectabilityAutomatically(StoredProcedureMetaData storedProcMetaData) throws SQLException {
        selectableProcedure = storedProcMetaData.isSelectable(procedureCall.getName());
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: This method behaves exactly the same as {@link #getClob(int)}.
     * </p>
     */
    @Override
    public NClob getNClob(int parameterIndex) throws SQLException {
        return getAndAssertSingletonResultSet().getNClob(mapOutParamIndexToPosition(parameterIndex));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: This method behaves exactly the same as {@link #getClob(String)}.
     * </p>
     */
    @Override
    public NClob getNClob(String parameterName) throws SQLException {
        return getAndAssertSingletonResultSet().getNClob(parameterName);
    }

    @Override
    public RowId getRowId(int parameterIndex) throws SQLException {
        return getAndAssertSingletonResultSet().getRowId(mapOutParamIndexToPosition(parameterIndex));
    }

    @Override
    public RowId getRowId(String parameterName) throws SQLException {
        return getAndAssertSingletonResultSet().getRowId(parameterName);
    }

    @Override
    public SQLXML getSQLXML(int parameterIndex) throws SQLException {
        return getAndAssertSingletonResultSet().getSQLXML(mapOutParamIndexToPosition(parameterIndex));
    }

    @Override
    public SQLXML getSQLXML(String parameterName) throws SQLException {
        return getAndAssertSingletonResultSet().getSQLXML(parameterName);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: This method behaves exactly the same as {@link #setClob(String, Clob)}.
     * </p>
     */
    @Override
    public void setNClob(String parameterName, NClob value) throws SQLException {
        setClob(parameterName, value);
    }

    @Override
    public void setRowId(String parameterName, RowId x) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    @Override
    public void setSQLXML(String parameterName, SQLXML xmlObject) throws SQLException {
        throw new FBDriverNotCapableException("Type SQLXML not supported");
    }

    private record WrapperWithCalendar(Object value, Calendar calendar) {
    }

    private record WrapperWithLong(Object value, long longValue) {
    }

}
