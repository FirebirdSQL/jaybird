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

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.sql.Date;
import java.util.*;

import org.firebirdsql.gds.DatabaseParameterBuffer;
import org.firebirdsql.gds.impl.DatabaseParameterBufferExtension;
import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.jdbc.escape.FBEscapedCallParser;
import org.firebirdsql.jdbc.escape.FBEscapedParser.EscapeParserMode;
import org.firebirdsql.jdbc.field.FBField;
import org.firebirdsql.jdbc.field.TypeConversionException;

/**
 * The interface used to execute SQL stored procedures.  The JDBC API
 * provides a stored procedure SQL escape syntax that allows stored procedures
 * to be called in a standard way for all RDBMSs. This escape syntax has one
 * form that includes a result parameter and one that does not. If used, the result
 * parameter must be registered as an OUT parameter. The other parameters
 * can be used for input, output or both. Parameters are referred to
 * sequentially, by number, with the first parameter being 1.
 * <PRE>
 *   {?= call &lt;procedure-name&gt;[(&lt;arg1&gt;,&lt;arg2&gt;, ...)]}
 *   {call &lt;procedure-name&gt;[(&lt;arg1&gt;,&lt;arg2&gt;, ...)]}
 * </PRE>
 * <P>
 * IN parameter values are set using the <code>set</code> methods inherited from
 * {@link PreparedStatement}.  The type of all OUT parameters must be
 * registered prior to executing the stored procedure; their values
 * are retrieved after execution via the <code>get</code> methods provided here.
 * <P>
 * A <code>CallableStatement</code> can return one {@link ResultSet} object or
 * multiple <code>ResultSet</code> objects.  Multiple
 * <code>ResultSet</code> objects are handled using operations
 * inherited from {@link Statement}.
 * <P>
 * For maximum portability, a call's <code>ResultSet</code> objects and
 * update counts should be processed prior to getting the values of output
 * parameters.
 * <P>
 *
 * @see Connection#prepareCall
 * @see ResultSet
 * 
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:sjardine@users.sourceforge.net">Steven Jardine</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public abstract class AbstractCallableStatement extends FBPreparedStatement implements CallableStatement, FirebirdCallableStatement {

    static final String SET_BY_STRING_NOT_SUPPORTED = "Setting parameters by name is not supported";
    static final String NATIVE_CALL_COMMAND = "EXECUTE PROCEDURE ";
    static final String NATIVE_SELECT_COMMAND = "SELECT * FROM ";

    private ResultSet currentRs;
    private ResultSet singletonRs;

    protected boolean selectableProcedure;

    protected FBProcedureCall procedureCall;

    protected AbstractCallableStatement(GDSHelper c, String sql, int rsType,
            int rsConcurrency, int rsHoldability, 
            StoredProcedureMetaData storedProcMetaData,
            FBObjectListener.StatementListener statementListener, 
            FBObjectListener.BlobListener blobListener) 
    throws SQLException {
        super(c, rsType, rsConcurrency, rsHoldability, statementListener, blobListener);

        DatabaseParameterBuffer dpb = c.getDatabaseParameterBuffer();

        EscapeParserMode mode = EscapeParserMode.USE_BUILT_IN;

        if (dpb.hasArgument(DatabaseParameterBufferExtension.USE_STANDARD_UDF))
            mode = EscapeParserMode.USE_STANDARD_UDF;

        FBEscapedCallParser parser = new FBEscapedCallParser(mode);

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

    public ParameterMetaData getParameterMetaData() throws SQLException {
        synchronized (getSynchronizationObject()) {
            // TODO See http://tracker.firebirdsql.org/browse/JDBC-352
            notifyStatementStarted(false);
            prepareFixedStatement(procedureCall.getSQL(isSelectableProcedure()));
        }

        return new FBParameterMetaData(fbStatement.getParameterDescriptor(), connection);
    }

    public void addBatch() throws SQLException {
        procedureCall.checkParameters();
        batchList.add(procedureCall.clone());
    }

    @Override
    protected List<Long> executeBatchInternal() throws SQLException {
        checkValidity();
        synchronized (getSynchronizationObject()) {
            boolean success = false;
            try {
                notifyStatementStarted();

                List<Long> results = new ArrayList<>(batchList.size());
                Iterator<Object> iterator = batchList.iterator();

                try {
                    prepareFixedStatement(procedureCall.getSQL(isSelectableProcedure()));

                    while (iterator.hasNext()) {
                        procedureCall = (FBProcedureCall) iterator.next();
                        executeSingleForBatch(results);
                    }

                    success = true;
                    return results;
                } catch (SQLException ex) {
                    throw jdbcVersionSupport.createBatchUpdateException(ex.getMessage(), ex.getSQLState(),
                            ex.getErrorCode(), toLargeArray(results), ex);
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
            throw jdbcVersionSupport.createBatchUpdateException(
                    "Statements executed as batch should not produce a result set",
                    SQLStateConstants.SQL_STATE_INVALID_STMT_TYPE, 0, toLargeArray(results), null);
        }

        results.add(getLargeUpdateCount());
    }

    public void setSelectableProcedure(boolean selectableProcedure) {
        this.selectableProcedure = selectableProcedure;
    }

    public boolean isSelectableProcedure() {
        return selectableProcedure;
    }

    /**
     * Set required types for output parameters.
     * 
     * @throws SQLException if something went wrong.
     */
    protected void setRequiredTypes() throws SQLException {
        if (singletonRs != null) {
            setRequiredTypesInternal((FBResultSet) singletonRs);
        }
        setRequiredTypesInternal((FBResultSet) getCurrentResultSet());
    }

    private void setRequiredTypesInternal(FBResultSet resultSet) throws SQLException {
        for (FBProcedureParam param : procedureCall.getOutputParams()) {
            if (param == null) continue;

            FBField field = resultSet.getField(procedureCall.mapOutParamIndexToPosition(param.getIndex()), false);
            field.setRequiredType(param.getType());
        }
    }

    /**
     * We allow multiple calls to this method without re-preparing the statement.
     * This is an workaround to the issue that the statement is actually prepared
     * only after all OUT parameters are registered.
     */
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
    public ResultSetMetaData getMetaData() throws SQLException {
        checkValidity();
        synchronized (getSynchronizationObject()) {
            // TODO See http://tracker.firebirdsql.org/browse/JDBC-352
            notifyStatementStarted(false);
            prepareFixedStatement(procedureCall.getSQL(isSelectableProcedure()));
        }

        return super.getMetaData();
    }

    /**
     * Executes an execute stored procedure.
     * Some prepared statements return multiple results; the <code>execute</code>
     * method handles these complex statements as well as the simpler
     * form of statements handled by the methods <code>executeQuery</code>
     * and <code>executeUpdate</code>.
     *
     * @exception SQLException if a database access error occurs
     * @see Statement#execute
     */
    public boolean execute() throws SQLException {
        procedureCall.checkParameters();
        boolean hasResultSet = false;
        synchronized (getSynchronizationObject()) {
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

    /**
     * Execute query. This method prepares statement before execution. Rest of
     * the processing is done by superclass.
     */
    public ResultSet executeQuery() throws SQLException {
        procedureCall.checkParameters();
        synchronized (getSynchronizationObject()) {
            notifyStatementStarted();
            prepareFixedStatement(procedureCall.getSQL(isSelectableProcedure()));

            if (!internalExecute(!isSelectableProcedure()))
                throw new FBSQLException("No resultset for sql", SQLStateConstants.SQL_STATE_NO_RESULT_SET);

            getResultSet();
            setRequiredTypes();

            return getCurrentResultSet();
        }
    }

    /**
     * Execute query. This method prepares statement before execution. Rest of
     * the processing is done by superclass.
     */
    public int executeUpdate() throws SQLException {
        procedureCall.checkParameters();
        synchronized (getSynchronizationObject()) {
            try {
                notifyStatementStarted();
                prepareFixedStatement(procedureCall.getSQL(isSelectableProcedure()));

                /*
                 * // R.Rokytskyy: JDBC CTS suite uses executeUpdate() //
                 * together with output parameters, therefore we cannot //
                 * throw exception if we want to pass the test suite
                 * 
                 * if (internalExecute(true)) throw new FBSQLException(
                 * "Update statement returned results.");
                 */

                boolean hasResults = internalExecute(!isSelectableProcedure());

                if (hasResults) {
                    setRequiredTypes();
                }

                return getUpdateCount();
            } finally {
                notifyStatementCompleted();
            }
        }
    }

    /**
     * Execute statement internally. This method sets cached parameters. Rest of
     * the processing is done by superclass.
     */
    protected boolean internalExecute(boolean sendOutParams) throws SQLException {
        currentRs = null;
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

                isParamSet[counter - 1] = true;
            }
        }

        final boolean hasResultSet = super.internalExecute(sendOutParams);
        if (hasResultSet && isSingletonResult) {
            // Safeguarding first row so it will work even if the result set from getResultSet is manipulated
            singletonRs = new FBResultSet(fbStatement.getFieldDescriptor(), connection,
                    new ArrayList<>(specialResult), true);
        }
        return hasResultSet;
    }

    private void setField(FBField field, WrapperWithLong value) throws SQLException {
        Object obj = value.getValue();

        if (obj == null) {
            field.setNull();
        } else {
            long longValue = value.getLongValue();

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
        Object obj = value.getValue();

        if (obj == null) {
            field.setNull();
        } else {
            Calendar cal = value.getCalendar();

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

    /**
     * Registers the OUT parameter in ordinal position
     * <code>parameterIndex</code> to the JDBC type
     * <code>sqlType</code>.  All OUT parameters must be registered
     * before a stored procedure is executed.
     * <p>
     * The JDBC type specified by <code>sqlType</code> for an OUT
     * parameter determines the Java type that must be used
     * in the <code>get</code> method to read the value of that parameter.
     * <p>
     * If the JDBC type expected to be returned to this output parameter
     * is specific to this particular database, <code>sqlType</code>
     * should be <code>java.sql.Types.OTHER</code>.  The method
     * {@link #getObject} retrieves the value.
     * @param parameterIndex the first parameter is 1, the second is 2,
     * and so on
     * @param sqlType the JDBC type code defined by <code>java.sql.Types</code>.
     * If the parameter is of JDBC type <code>NUMERIC</code>
     * or <code>DECIMAL</code>, the version of
     * <code>registerOutParameter</code> that accepts a scale value
     * should be used.
     * @exception SQLException if a database access error occurs
     * @see Types
     */
    public void registerOutParameter(int parameterIndex, int sqlType) throws SQLException {
        procedureCall.registerOutParam(parameterIndex, sqlType);
    }

    /**
     * Registers the parameter in ordinal position
     * <code>parameterIndex</code> to be of JDBC type
     * <code>sqlType</code>.  This method must be called
     * before a stored procedure is executed.
     * <p>
     * The JDBC type specified by <code>sqlType</code> for an OUT
     * parameter determines the Java type that must be used
     * in the <code>get</code> method to read the value of that parameter.
     * <p>
     * This version of <code>registerOutParameter</code> should be
     * used when the parameter is of JDBC type <code>NUMERIC</code>
     * or <code>DECIMAL</code>.
     * @param parameterIndex the first parameter is 1, the second is 2,
     * and so on
     * @param sqlType SQL type code defined by <code>java.sql.Types</code>.
     * @param scale the desired number of digits to the right of the
     * decimal point.  It must be greater than or equal to zero.
     * @exception SQLException if a database access error occurs
     * @see Types
     */
    public void registerOutParameter(int parameterIndex, int sqlType, int scale) throws SQLException {
        procedureCall.registerOutParam(parameterIndex, sqlType);
    }

    /**
     * Indicates whether or not the last OUT parameter read had the value of
     * SQL <code>NULL</code>.  Note that this method should be called only after
     * calling a <code>getXXX</code> method; otherwise, there is no value to use in
     * determining whether it is <code>null</code> or not.
     * @return <code>true</code> if the last parameter read was SQL
     * <code>NULL</code>; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean wasNull() throws SQLException {
        return getAndAssertSingletonResultSet().wasNull();
    }

    /**
     * Retrieves the value of a JDBC <code>CHAR</code>, <code>VARCHAR</code>,
     * or <code>LONGVARCHAR</code> parameter as a <code>String</code> in
     * the Java programming language.
     * <p>
     * For the fixed-length type JDBC <code>CHAR</code>,
     * the <code>String</code> object
     * returned has exactly the same value the JDBC
     * <code>CHAR</code> value had in the
     * database, including any padding added by the database.
     * @param parameterIndex the first parameter is 1, the second is 2,
     * and so on
     * @return the parameter value. If the value is SQL <code>NULL</code>, the result
     * is <code>null</code>.
     * @exception SQLException if a database access error occurs
     */
    public String getString(int parameterIndex) throws SQLException {
        parameterIndex = procedureCall.mapOutParamIndexToPosition(parameterIndex);
        return getAndAssertSingletonResultSet().getString(parameterIndex);
    }

    /**
     * Gets the value of a JDBC <code>BIT</code> parameter as a <code>boolean</code>
     * in the Java programming language.
     * @param parameterIndex the first parameter is 1, the second is 2,
     * and so on
     * @return the parameter value.  If the value is SQL <code>NULL</code>, the result
     * is <code>false</code>.
     * @exception SQLException if a database access error occurs
     */
    public boolean getBoolean(int parameterIndex) throws SQLException {
        parameterIndex = procedureCall.mapOutParamIndexToPosition(parameterIndex);
        return getAndAssertSingletonResultSet().getBoolean(parameterIndex);
    }

    /**
     * Gets the value of a JDBC <code>TINYINT</code> parameter as a <code>byte</code>
     * in the Java programming language.
     * @param parameterIndex the first parameter is 1, the second is 2,
     * and so on
     * @return the parameter value.  If the value is SQL <code>NULL</code>, the result
     * is 0.
     * @exception SQLException if a database access error occurs
     */
    public byte getByte(int parameterIndex) throws SQLException {
        parameterIndex = procedureCall.mapOutParamIndexToPosition(parameterIndex);
        return getAndAssertSingletonResultSet().getByte(parameterIndex);
    }

    /**
     * Gets the value of a JDBC <code>SMALLINT</code> parameter as a <code>short</code>
     * in the Java programming language.
     * @param parameterIndex the first parameter is 1, the second is 2,
     * and so on
     * @return the parameter value.  If the value is SQL <code>NULL</code>, the result
     * is 0.
     * @exception SQLException if a database access error occurs
     */
    public short getShort(int parameterIndex) throws SQLException {
        parameterIndex = procedureCall.mapOutParamIndexToPosition(parameterIndex);
        return getAndAssertSingletonResultSet().getShort(parameterIndex);
    }

    /**
     * Gets the value of a JDBC <code>INTEGER</code> parameter as an <code>int</code>
     * in the Java programming language.
     * @param parameterIndex the first parameter is 1, the second is 2,
     * and so on
     * @return the parameter value.  If the value is SQL <code>NULL</code>, the result
     * is 0.
     * @exception SQLException if a database access error occurs
     */
    public int getInt(int parameterIndex) throws SQLException {
        parameterIndex = procedureCall.mapOutParamIndexToPosition(parameterIndex);
        return getAndAssertSingletonResultSet().getInt(parameterIndex);
    }

    /**
     * Gets the value of a JDBC <code>BIGINT</code> parameter as a <code>long</code>
     * in the Java programming language.
     * @param parameterIndex the first parameter is 1, the second is 2,
     * and so on
     * @return the parameter value.  If the value is SQL <code>NULL</code>, the result
     * is 0.
     * @exception SQLException if a database access error occurs
     */
    public long getLong(int parameterIndex) throws SQLException {
        parameterIndex = procedureCall.mapOutParamIndexToPosition(parameterIndex);
        return getAndAssertSingletonResultSet().getLong(parameterIndex);
    }

    /**
     * Gets the value of a JDBC <code>FLOAT</code> parameter as a <code>float</code>
     * in the Java programming language.
     * @param parameterIndex the first parameter is 1, the second is 2,
     * and so on
     * @return the parameter value.  If the value is SQL <code>NULL</code>, the result
     * is 0.
     * @exception SQLException if a database access error occurs
     */
    public float getFloat(int parameterIndex) throws SQLException {
        parameterIndex = procedureCall.mapOutParamIndexToPosition(parameterIndex);
        return getAndAssertSingletonResultSet().getFloat(parameterIndex);
    }

    /**
     * Gets the value of a JDBC <code>DOUBLE</code> parameter as a <code>double</code>
     * in the Java programming language.
     * @param parameterIndex the first parameter is 1, the second is 2,
     * and so on
     * @return the parameter value.  If the value is SQL <code>NULL</code>, the result
     * is 0.
     * @exception SQLException if a database access error occurs
     */
    public double getDouble(int parameterIndex) throws SQLException {
        parameterIndex = procedureCall.mapOutParamIndexToPosition(parameterIndex);
        return getAndAssertSingletonResultSet().getDouble(parameterIndex);
    }

    /**
     * Gets the value of a JDBC <code>NUMERIC</code> parameter as a
     * <code>java.math.BigDecimal</code> object with scale digits to
     * the right of the decimal point.
     * @param parameterIndex the first parameter is 1, the second is 2,
     * and so on
     * @param scale the number of digits to the right of the decimal point
     * @return the parameter value.  If the value is SQL <code>NULL</code>, the result is
     * <code>null</code>.
     * @exception SQLException if a database access error occurs
     * @deprecated
     */
    @SuppressWarnings("deprecation")
    @Deprecated
    public BigDecimal getBigDecimal(int parameterIndex, int scale) throws SQLException {
        parameterIndex = procedureCall.mapOutParamIndexToPosition(parameterIndex);
        return getAndAssertSingletonResultSet().getBigDecimal(parameterIndex, scale);
    }

    /**
     * Gets the value of a JDBC <code>BINARY</code> or <code>VARBINARY</code>
     * parameter as an array of <code>byte</code> values in the Java
     * programming language.
     * @param parameterIndex the first parameter is 1, the second is 2,
     * and so on
     * @return the parameter value.  If the value is SQL <code>NULL</code>, the result is
     *  <code>null</code>.
     * @exception SQLException if a database access error occurs
     */
    public byte[] getBytes(int parameterIndex) throws SQLException {
        parameterIndex = procedureCall.mapOutParamIndexToPosition(parameterIndex);
        return getAndAssertSingletonResultSet().getBytes(parameterIndex);
    }

    /**
     * Gets the value of a JDBC <code>DATE</code> parameter as a
     * <code>java.sql.Date</code> object.
     * @param parameterIndex the first parameter is 1, the second is 2,
     * and so on
     * @return the parameter value.  If the value is SQL <code>NULL</code>, the result
     * is <code>null</code>.
     * @exception SQLException if a database access error occurs
     */
    public java.sql.Date getDate(int parameterIndex) throws SQLException {
        parameterIndex = procedureCall.mapOutParamIndexToPosition(parameterIndex);
        return getAndAssertSingletonResultSet().getDate(parameterIndex);
    }

    /**
     * Get the value of a JDBC <code>TIME</code> parameter as a
     * <code>java.sql.Time</code> object.
     * @param parameterIndex the first parameter is 1, the second is 2,
     * and so on
     * @return the parameter value.  If the value is SQL <code>NULL</code>, the result
     * is <code>null</code>.
     * @exception SQLException if a database access error occurs
     */
    public Time getTime(int parameterIndex) throws SQLException {
        parameterIndex = procedureCall.mapOutParamIndexToPosition(parameterIndex);
        return getAndAssertSingletonResultSet().getTime(parameterIndex);
    }

    /**
     * Gets the value of a JDBC <code>TIMESTAMP</code> parameter as a
     * <code>java.sql.Timestamp</code> object.
     * @param parameterIndex the first parameter is 1, the second is 2,
     * and so on
     * @return the parameter value.  If the value is SQL <code>NULL</code>, the result
     * is <code>null</code>.
     * @exception SQLException if a database access error occurs
     */
    public Timestamp getTimestamp(int parameterIndex) throws SQLException {
        parameterIndex = procedureCall.mapOutParamIndexToPosition(parameterIndex);
        return getAndAssertSingletonResultSet().getTimestamp(parameterIndex);
    }

    /**
     * Gets the value of a parameter as an <code>Object</code> in the Java
     * programming language.
     * <p>
     * This method returns a Java object whose type corresponds to the JDBC
     * type that was registered for this parameter using the method
     * <code>registerOutParameter</code>.  By registering the target JDBC
     * type as <code>java.sql.Types.OTHER</code>, this method can be used
     * to read database-specific abstract data types.
     * @param parameterIndex the first parameter is 1, the second is 2,
     * and so on
     * @return A <code>java.lang.Object</code> holding the OUT parameter value.
     * @exception SQLException if a database access error occurs
     * @see Types
     */
    public Object getObject(int parameterIndex) throws SQLException {
        parameterIndex = procedureCall.mapOutParamIndexToPosition(parameterIndex);
        return getAndAssertSingletonResultSet().getObject(parameterIndex);
    }
    
    public Object getObject(String colName) throws SQLException {
        return getObject(findOutParameter(colName));
    }
    
   /**
    *
    * Returns an object representing the value of OUT parameter
    * <code>i</code> and uses <code>map</code> for the custom
    * mapping of the parameter value.
    * <p>
    * This method returns a Java object whose type corresponds to the
    * JDBC type that was registered for this parameter using the method
    * <code>registerOutParameter</code>.  By registering the target
    * JDBC type as <code>java.sql.Types.OTHER</code>, this method can
    * be used to read database-specific abstract data types.
    * @param parameterIndex the first parameter is 1, the second is 2, and so on
    * @param map the mapping from SQL type names to Java classes
    * @return a <code>java.lang.Object</code> holding the OUT parameter value
    * @exception SQLException if a database access error occurs
    * @since 1.2
    * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
    */
    public Object getObject(int parameterIndex, Map<String, Class<?>> map) throws SQLException {
        parameterIndex = procedureCall.mapOutParamIndexToPosition(parameterIndex);
        return getAndAssertSingletonResultSet().getObject(parameterIndex, map);
    }
    
    public Object getObject(String colName, Map<String, Class<?>> map) throws SQLException {
        return getObject(findOutParameter(colName), map);
    }
    
    public <T> T getObject(int parameterIndex, Class<T> type) throws SQLException {
        parameterIndex = procedureCall.mapOutParamIndexToPosition(parameterIndex);
        // NOTE: Cast required for Java 6 compatibility
        //noinspection RedundantCast
        return ((FBResultSet) getAndAssertSingletonResultSet()).getObject(parameterIndex, type);
    }

    public <T> T getObject(String parameterName, Class<T> type) throws SQLException {
        return getObject(findOutParameter(parameterName), type);
    }

    /**
    *
    * Gets the value of a JDBC <code>NUMERIC</code> parameter as a
    * <code>java.math.BigDecimal</code> object with as many digits to the
    * right of the decimal point as the value contains.
    * @param parameterIndex the first parameter is 1, the second is 2,
    * and so on
    * @return the parameter value in full precision.  If the value is
    * SQL <code>NULL</code>, the result is <code>null</code>.
    * @exception SQLException if a database access error occurs
    * @since 1.2
    * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
    */
    public BigDecimal getBigDecimal(int parameterIndex) throws SQLException {
        parameterIndex = procedureCall.mapOutParamIndexToPosition(parameterIndex);
        return getAndAssertSingletonResultSet().getBigDecimal(parameterIndex);
    }
    
   /**
    *
    * Gets the value of a JDBC <code>REF(&lt;structured-type&gt;)</code>
    * parameter as a {@link Ref} object in the Java programming language.
    * @param parameterIndex the first parameter is 1, the second is 2,
    * and so on
    * @return the parameter value as a <code>Ref</code> object in the
    * Java programming language.  If the value was SQL <code>NULL</code>, the value
    * <code>null</code> is returned.
    * @exception SQLException if a database access error occurs
    * @since 1.2
    * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
    */
    public Ref getRef(int parameterIndex) throws SQLException {
        parameterIndex = procedureCall.mapOutParamIndexToPosition(parameterIndex);
        return getAndAssertSingletonResultSet().getRef(parameterIndex);
    }

    /**
    *
    * Gets the value of a JDBC <code>BLOB</code> parameter as a
    * {@link Blob} object in the Java programming language.
    * @param parameterIndex the first parameter is 1, the second is 2, and so on
    * @return the parameter value as a <code>Blob</code> object in the
    * Java programming language.  If the value was SQL <code>NULL</code>, the value
    * <code>null</code> is returned.
    * @exception SQLException if a database access error occurs
    * @since 1.2
    * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
    */
    public Blob getBlob(int parameterIndex) throws SQLException {
        parameterIndex = procedureCall.mapOutParamIndexToPosition(parameterIndex);
        return getAndAssertSingletonResultSet().getBlob(parameterIndex);
    }

    /**
    *
    * Gets the value of a JDBC <code>CLOB</code> parameter as a
    * <code>Clob</code> object in the Java programming language.
    * @param parameterIndex the first parameter is 1, the second is 2, and
    * so on
    * @return the parameter value as a <code>Clob</code> object in the
    * Java programming language.  If the value was SQL <code>NULL</code>, the
    * value <code>null</code> is returned.
    * @exception SQLException if a database access error occurs
    * @since 1.2
    * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
    */
    public Clob getClob(int parameterIndex) throws SQLException {
        parameterIndex = procedureCall.mapOutParamIndexToPosition(parameterIndex);
        return getAndAssertSingletonResultSet().getClob(parameterIndex);
    }

    /**
    *
    * Gets the value of a JDBC <code>ARRAY</code> parameter as an
    * {@link Array} object in the Java programming language.
    * @param parameterIndex the first parameter is 1, the second is 2, and
    * so on
    * @return the parameter value as an <code>Array</code> object in
    * the Java programming language.  If the value was SQL <code>NULL</code>, the
    * value <code>null</code> is returned.
    * @exception SQLException if a database access error occurs
    * @since 1.2
    * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
    */
    public Array getArray(int parameterIndex) throws SQLException {
        parameterIndex = procedureCall.mapOutParamIndexToPosition(parameterIndex);
        return getAndAssertSingletonResultSet().getArray(parameterIndex);
    }

    /**
     * Gets the value of a JDBC <code>DATE</code> parameter as a
     * <code>java.sql.Date</code> object, using
     * the given <code>Calendar</code> object
     * to construct the date.
     * With a <code>Calendar</code> object, the driver
     * can calculate the date taking into account a custom timezone and locale.
     * If no <code>Calendar</code> object is specified, the driver uses the
     * default timezone and locale.
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     * and so on
     * @param cal the <code>Calendar</code> object the driver will use
     *            to construct the date
     * @return the parameter value.  If the value is SQL <code>NULL</code>, the result is
     * <code>null</code>.
     * @exception SQLException if a database access error occurs
     */
    public java.sql.Date getDate(int parameterIndex, Calendar cal) throws SQLException {
        parameterIndex = procedureCall.mapOutParamIndexToPosition(parameterIndex);
        return getAndAssertSingletonResultSet().getDate(parameterIndex, cal);
    }

    /**
     * Gets the value of a JDBC <code>TIME</code> parameter as a
     * <code>java.sql.Time</code> object, using
     * the given <code>Calendar</code> object
     * to construct the time.
     * With a <code>Calendar</code> object, the driver
     * can calculate the time taking into account a custom timezone and locale.
     * If no <code>Calendar</code> object is specified, the driver uses the
     * default timezone and locale.
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     * and so on
     * @param cal the <code>Calendar</code> object the driver will use
     *            to construct the time
     * @return the parameter value; if the value is SQL <code>NULL</code>, the result is
     * <code>null</code>.
     * @exception SQLException if a database access error occurs
     */
    public Time getTime(int parameterIndex, Calendar cal) throws SQLException {
        parameterIndex = procedureCall.mapOutParamIndexToPosition(parameterIndex);
        return getAndAssertSingletonResultSet().getTime(parameterIndex, cal);
    }

    /**
     * Gets the value of a JDBC <code>TIMESTAMP</code> parameter as a
     * <code>java.sql.Timestamp</code> object, using
     * the given <code>Calendar</code> object to construct
     * the <code>Timestamp</code> object.
     * With a <code>Calendar</code> object, the driver
     * can calculate the timestamp taking into account a custom timezone and locale.
     * If no <code>Calendar</code> object is specified, the driver uses the
     * default timezone and locale.
     *
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     * and so on
     * @param cal the <code>Calendar</code> object the driver will use
     *            to construct the timestamp
     * @return the parameter value.  If the value is SQL <code>NULL</code>, the result is
     * <code>null</code>.
     * @exception SQLException if a database access error occurs
     */
    public Timestamp getTimestamp(int parameterIndex, Calendar cal) throws SQLException {
        parameterIndex = procedureCall.mapOutParamIndexToPosition(parameterIndex);
        return getAndAssertSingletonResultSet().getTimestamp(parameterIndex, cal);
    }

    public URL getURL(int parameterIndex) throws SQLException {
        parameterIndex = procedureCall.mapOutParamIndexToPosition(parameterIndex);
        return getAndAssertSingletonResultSet().getURL(parameterIndex);
    }

    public String getString(String colName) throws SQLException {
        return getString(findOutParameter(colName));
    }

    public boolean getBoolean(String colName) throws SQLException {
        return getBoolean(findOutParameter(colName));
    }

    public byte getByte(String colName) throws SQLException {
        return getByte(findOutParameter(colName));
    }

    public short getShort(String colName) throws SQLException {
        return getShort(findOutParameter(colName));
    }

    public int getInt(String colName) throws SQLException {
        return getInt(findOutParameter(colName));
    }

    public long getLong(String colName) throws SQLException {
        return getLong(findOutParameter(colName));
    }

    public float getFloat(String colName) throws SQLException {
        return getFloat(findOutParameter(colName));
    }

    public double getDouble(String colName) throws SQLException {
        return getDouble(findOutParameter(colName));
    }

    public byte[] getBytes(String colName) throws SQLException {
        return getBytes(findOutParameter(colName));
    }

    public Date getDate(String colName) throws SQLException {
        return getDate(findOutParameter(colName));
    }

    public Time getTime(String colName) throws SQLException {
        return getTime(findOutParameter(colName));
    }

    public Timestamp getTimestamp(String colName) throws SQLException {
        return getTimestamp(findOutParameter(colName));
    }

    public BigDecimal getBigDecimal(String colName) throws SQLException {
        return getBigDecimal(findOutParameter(colName));
    }

    public Ref getRef(String colName) throws SQLException {
        return getRef(findOutParameter(colName));
    }

    public Blob getBlob(String colName) throws SQLException {
        return getBlob(findOutParameter(colName));
    }

    public Clob getClob(String colName) throws SQLException {
        return getClob(findOutParameter(colName));
    }

    public Array getArray(String colName) throws SQLException {
        return getArray(findOutParameter(colName));
    }

    public Date getDate(String colName, Calendar cal) throws SQLException {
        return getDate(findOutParameter(colName), cal);
    }

    public Time getTime(String colName, Calendar cal) throws SQLException {
        return getTime(findOutParameter(colName), cal);
    }

    public Timestamp getTimestamp(String colName, Calendar cal) throws SQLException {
        return getTimestamp(findOutParameter(colName), cal);
    }

    public URL getURL(String colName) throws SQLException {
        return getURL(findOutParameter(colName));
    }

    public Reader getCharacterStream(int parameterIndex) throws SQLException {
        parameterIndex = procedureCall.mapOutParamIndexToPosition(parameterIndex);
        return getAndAssertSingletonResultSet().getCharacterStream(parameterIndex);
    }

    public Reader getCharacterStream(String parameterName) throws SQLException {
        return getCharacterStream(findOutParameter(parameterName));
    }

    public Reader getNCharacterStream(int parameterIndex) throws SQLException {
        parameterIndex = procedureCall.mapOutParamIndexToPosition(parameterIndex);
        return getAndAssertSingletonResultSet().getNCharacterStream(parameterIndex);
    }

    public Reader getNCharacterStream(String parameterName) throws SQLException {
        return getNCharacterStream(findOutParameter(parameterName));
    }

    public String getNString(int parameterIndex) throws SQLException {
        parameterIndex = procedureCall.mapOutParamIndexToPosition(parameterIndex);
        return getAndAssertSingletonResultSet().getNString(parameterIndex);
    }

    public String getNString(String parameterName) throws SQLException {
        return getNString(findOutParameter(parameterName));
    }

    public void setAsciiStream(String parameterName, InputStream x, long length) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    public void setAsciiStream(String parameterName, InputStream x) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    public void setBinaryStream(String parameterName, InputStream x, long length) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    public void setBinaryStream(String parameterName, InputStream x) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    public void setBlob(String parameterName, Blob x) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    public void setBlob(String parameterName, InputStream inputStream, long length) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    public void setBlob(String parameterName, InputStream inputStream) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    public void setCharacterStream(String parameterName, Reader reader, long length) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    public void setCharacterStream(String parameterName, Reader reader) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    public void setClob(String parameterName, Clob x) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    public void setClob(String parameterName, Reader reader, long length) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

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
    public void setNString(String parameterName, String value) throws SQLException {
        setString(parameterName, value);
    }

    public void registerOutParameter(String param1, int param2) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    public void registerOutParameter(String param1, int param2, int param3) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    public void registerOutParameter(String param1, int param2, String param3) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    public void setURL(String param1, URL param2) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    public void setNull(String param1, int param2) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    public void setBoolean(String param1, boolean param2) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    public void setByte(String param1, byte param2) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    public void setShort(String param1, short param2) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    public void setInt(String param1, int param2) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    public void setLong(String param1, long param2) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    public void setFloat(String param1, float param2) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    public void setDouble(String param1, double param2) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    public void setBigDecimal(String param1, BigDecimal param2) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    public void setString(String param1, String param2) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    public void setBytes(String param1, byte[] param2) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    public void setDate(String param1, Date param2) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    public void setTime(String param1, Time param2) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    public void setTimestamp(String param1, Timestamp param2) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    public void setAsciiStream(String param1, InputStream param2, int param3) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    public void setBinaryStream(String param1, InputStream param2, int param3) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    public void setObject(String param1, Object param2, int param3, int param4) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    public void setObject(String param1, Object param2, int param3) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    public void setObject(String param1, Object param2) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    public void setCharacterStream(String param1, Reader param2, int param3) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    public void setDate(String param1, Date param2, Calendar param3) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    public void setTime(String param1, Time param2, Calendar param3) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    public void setTimestamp(String param1, Timestamp param2, Calendar param3) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    public void setNull(String param1, int param2, String param3) throws SQLException {
        throw new FBDriverNotCapableException(SET_BY_STRING_NOT_SUPPORTED);
    }

    public void registerOutParameter(int parameterIndex, int sqlType, String typeName) throws SQLException {
        // TODO Can we implement this, how?
        throw new FBDriverNotCapableException();
    }

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
            throw new SQLException("Current statement has no data to return.",
                    SQLStateConstants.SQL_STATE_NO_RESULT_SET);
        }
        // check if we have a row, and try to move to the first position.
        if (rs.getRow() == 0) {
            rs.next();
        } else {
            return;
        }

        // check if we still have no row and throw an exception in this case.
        if (rs.getRow() == 0) {
            throw new SQLException("Current statement has no data to return.",
                    SQLStateConstants.SQL_STATE_NO_RESULT_SET);
        }
    }

    // this method doesn't give an exception if it is called twice.
    public ResultSet getCurrentResultSet() throws SQLException {
        if (currentRs == null)
            currentRs = super.getResultSet();
        return currentRs;
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
        final ResultSet rs;
        if (!isSelectableProcedure() && singletonRs != null) {
            rs = singletonRs;
        } else {
            rs = getCurrentResultSet();
        }
        assertHasData(rs);
        return rs;
    }

    /**
     * Returns the current result as a <code>ResultSet</code> object.
     * This method should be called only once per result.
     * Calling this method twice with autocommit on and used will probably
     * throw an inappropriate or uninformative exception.
     *
     * @return the current result as a <code>ResultSet</code> object;
     * <code>null</code> if the result is an update count or there are no more results
     * @exception SQLException if a database access error occurs
     * @see #execute
     */
    public ResultSet getResultSet() throws SQLException {
        return getCurrentResultSet();
    }

    public void setArray(int i, Array x) throws SQLException {
        procedureCall.getInputParam(i).setValue(x);
    }

    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(x);
    }

    public void setBinaryStream(int parameterIndex, InputStream inputStream, int length) throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(new WrapperWithLong(inputStream, length));
    }

    public void setBinaryStream(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(new WrapperWithLong(inputStream, length));
    }

    public void setBinaryStream(int parameterIndex, InputStream inputStream) throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(inputStream);
    }

    public void setBlob(int parameterIndex, Blob blob) throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(blob);
    }

    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(new WrapperWithLong(inputStream, length));
    }

    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(inputStream);
    }

    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(x);
    }

    public void setByte(int parameterIndex, byte x) throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(x);
    }

    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(x);
    }

    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(new WrapperWithLong(reader, length));
    }

    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(new WrapperWithLong(reader, length));
    }

    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(reader);
    }

    public void setClob(int parameterIndex, Clob x) throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(x);
    }

    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(new WrapperWithLong(reader, length));
    }

    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(reader);
    }

    public void setDate(int parameterIndex, java.sql.Date x, Calendar cal) throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(new WrapperWithCalendar(x, cal));
    }

    public void setDate(int parameterIndex, java.sql.Date x) throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(x);
    }

    public void setDouble(int parameterIndex, double x) throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(x);
    }

    public void setFloat(int parameterIndex, float x) throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(x);
    }

    public void setInt(int parameterIndex, int x) throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(x);
    }

    public void setLong(int parameterIndex, long x) throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(x);
    }

    public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(null);
    }

    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(null);
    }

    public void setObject(int parameterIndex, Object x, int targetSqlType, int scale) throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(x);
    }

    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(x);
    }

    public void setObject(int parameterIndex, Object x) throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(x);
    }

    public void setRef(int parameterIndex, Ref x) throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(x);
    }

    public void setShort(int parameterIndex, short x) throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(x);
    }

    public void setString(int parameterIndex, String x) throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(x);
    }

    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(new WrapperWithCalendar(x, cal));
    }

    public void setTime(int parameterIndex, Time x) throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(x);
    }

    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(new WrapperWithCalendar(x, cal));
    }

    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(x);
    }

    /**
     * Set the selectability of this stored procedure from RDB$PROCEDURE_TYPE
     * @throws SQLException 
     */
    private void setSelectabilityAutomatically(StoredProcedureMetaData storedProcMetaData) throws SQLException {
        selectableProcedure = storedProcMetaData.isSelectable(procedureCall.getName());
    }
    
    /**
     * Helper method to identify the right resultset column for the give OUT
     * parameter name.
     * 
     * @param paramName
     *            Name of the OUT parameter
     */
    protected int findOutParameter(String paramName) throws SQLException {
        return getAndAssertSingletonResultSet().findColumn(paramName);
    }

    public NClob getNClob(int parameterIndex) throws SQLException {
        parameterIndex = procedureCall.mapOutParamIndexToPosition(parameterIndex);
        return getAndAssertSingletonResultSet().getNClob(parameterIndex);
    }

    public NClob getNClob(String parameterName) throws SQLException {
        return getNClob(findOutParameter(parameterName));
    }

    public RowId getRowId(int parameterIndex) throws SQLException {
        parameterIndex = procedureCall.mapOutParamIndexToPosition(parameterIndex);
        return getAndAssertSingletonResultSet().getRowId(parameterIndex);
    }

    public RowId getRowId(String parameterName) throws SQLException {
        return getRowId(findOutParameter(parameterName));
    }

    public SQLXML getSQLXML(int parameterIndex) throws SQLException {
        parameterIndex = procedureCall.mapOutParamIndexToPosition(parameterIndex);
        return getAndAssertSingletonResultSet().getSQLXML(parameterIndex);
    }

    public SQLXML getSQLXML(String parameterName) throws SQLException {
        return getSQLXML(findOutParameter(parameterName));
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

    public void setRowId(String parameterName, RowId x) throws SQLException {
        throw new FBDriverNotCapableException("Type ROWID not yet supported");
    }

    public void setSQLXML(String parameterName, SQLXML xmlObject) throws SQLException {
        throw new FBDriverNotCapableException("Type SQLXML not supported");
    }

    private static class WrapperWithCalendar {
        private final Object value;
        private final Calendar c;

        private WrapperWithCalendar(Object value, Calendar c) {
            this.value = value;
            this.c = c;
        }

        private Object getValue() {
            return value;
        }

        private Calendar getCalendar() {
            return c;
        }
    }

    private static class WrapperWithLong {
        private final Object value;
        private final long longValue;

        private WrapperWithLong(Object value, long longValue) {
            this.value = value;
            this.longValue = longValue;
        }

        private Object getValue() {
            return value;
        }

        private long getLongValue() {
            return longValue;
        }
    }

}
