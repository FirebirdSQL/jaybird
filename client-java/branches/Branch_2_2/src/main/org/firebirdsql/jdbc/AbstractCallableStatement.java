/*
 * $Id$
 * 
 * Firebird Open Source J2ee connector - jdbc driver
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
 * can be obtained from a CVS history command.
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
import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.impl.DatabaseParameterBufferExtension;
import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.jdbc.field.FBField;
import org.firebirdsql.jdbc.field.TypeConversionException;

/**
 * The interface used to execute SQL
 * stored procedures.  JDBC provides a stored procedure
 * SQL escape syntax that allows stored procedures to be called in a standard
 * way for all RDBMSs. This escape syntax has one form that includes
 * a result parameter and one that does not. If used, the result
 * parameter must be registered as an OUT parameter. The other parameters
 * can be used for input, output or both. Parameters are referred to
 * sequentially, by number, with the first parameter being 1.
 * <P>
 * <blockquote><pre>
 *   {?= call &lt;procedure-name&gt;[&lt;arg1&gt;,&lt;arg2&gt;, ...]}
 *   {call &lt;procedure-name&gt;[&lt;arg1&gt;,&lt;arg2&gt;, ...]}
 * </pre></blockquote>
 * <P>
 * IN parameter values are set using the set methods inherited from
 * {@link PreparedStatement}.  The type of all OUT parameters must be
 * registered prior to executing the stored procedure; their values
 * are retrieved after execution via the <code>get</code> methods provided here.
 * <P>
 * A <code>CallableStatement</code> can return one {@link ResultSet} or
 * multiple <code>ResultSet</code> objects.  Multiple
 * <code>ResultSet</code> objects are handled using operations
 * inherited from {@link Statement}.
 * <P>
 * For maximum portability, a call's <code>ResultSet</code> objects and
 * update counts should be processed prior to getting the values of output
 * parameters.
 * <P>
 * Methods that are new in the JDBC 2.0 API are marked "Since 1.2."
 *
 * Note: Escape syntax currently is not supported. Please use native
 * Firebird procedure call syntax:
 * <pre>
 * EXECUTE PROCEDURE <proc_name>(param1, ...);
 * </pre>
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
    
    static final String NATIVE_CALL_COMMAND = "EXECUTE PROCEDURE";
    static final String NATIVE_SELECT_COMMAND = "SELECT * FROM";

    private ResultSet currentRs;

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

        int mode = FBEscapedParser.USE_BUILT_IN;

        if (dpb.hasArgument(DatabaseParameterBufferExtension.USE_STANDARD_UDF))
            mode = FBEscapedParser.USE_STANDARD_UDF;

        FBEscapedCallParser parser = new FBEscapedCallParser(mode);

        // here statement is parsed twice, once in c.nativeSQL(...)
        // and second time in parser.parseCall(...)... not nice, maybe
        // in the future should be fixed by calling FBEscapedParser for
        // each parameter in FBEscapedCallParser class
        procedureCall = parser.parseCall(nativeSQL(sql));

        if (storedProcMetaData.canGetSelectableInformation()) {
            setSelectabilityAutomatically(storedProcMetaData);
        }
    }

    public ParameterMetaData getParameterMetaData() throws SQLException {
        statementListener.executionStarted(this);
        Object syncObject = getSynchronizationObject();
        synchronized (syncObject) {
            try {
                prepareFixedStatement(procedureCall.getSQL(isSelectableProcedure()), true);
            } catch (GDSException ge) {
                throw new FBSQLException(ge);
            }
        }

        return new FBParameterMetaData(fixedStmt.getInSqlda().sqlvar, gdsHelper);
    }

    public void addBatch() throws SQLException {
        batchList.add(procedureCall.clone());
    }

    public int[] executeBatch() throws SQLException {
        Object syncObject = getSynchronizationObject();
        synchronized (syncObject) {
            boolean success = false;
            try {
                notifyStatementStarted();

                List results = new ArrayList(batchList.size());
                Iterator iterator = batchList.iterator();

                try {
                    while (iterator.hasNext()) {
                        procedureCall = (FBProcedureCall) iterator.next();
                        executeSingleForBatch(results);
                    }

                    success = true;
                    return toArray(results);
                } finally {
                    clearBatch();
                }
            } finally {
                notifyStatementCompleted(success);
            }
        }
    }
    
    private void executeSingleForBatch(List results) throws SQLException {
        /*
         * TODO: array given to BatchUpdateException might not be JDBC-compliant
         * (should set Statement.EXECUTE_FAILED and throwing it right away
         * instead of continuing may fail intention)
         */
        try {
            prepareFixedStatement(procedureCall.getSQL(isSelectableProcedure()), true);

            if (internalExecute(!isSelectableProcedure()))
                throw new BatchUpdateException(toArray(results));

            results.add(new Integer(getUpdateCount()));
        } catch (GDSException ex) {
            throw new BatchUpdateException(ex.getMessage(), "", ex.getFbErrorCode(),
                    toArray(results));
        }
    }

    /* (non-Javadoc)
     * @see org.firebirdsql.jdbc.FirebirdCallableStatement#setSelectableProcedure(boolean)
     */
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

        FBResultSet resultSet = (FBResultSet) getCurrentResultSet();

        Iterator iter = procedureCall.getOutputParams().iterator();
        while (iter.hasNext()) {
            FBProcedureParam param = (FBProcedureParam) iter.next();

            if (param == null)
                continue;

            FBField field = resultSet.getField(
                    procedureCall.mapOutParamIndexToPosition(param.getIndex()),
                    false);

            field.setRequiredType(param.getType());
        }
    }

    /**
     * We allow multiple calls to this method without re-preparing the statement.
     * This is an workaround to the issue that the statement is actually prepared
     * only after all OUT parameters are registered.
     */
    protected void prepareFixedStatement(String sql, boolean describeBind)
            throws GDSException, SQLException {

        if (fixedStmt != null)
            return;

        super.prepareFixedStatement(sql, describeBind);
    }

    /**
     * Since we deferred the statement preparation until all OUT params are 
     * registered, we ensure that the statement is prepared before the meta
     * data for the callable statement is obtained.
     */
    public ResultSetMetaData getMetaData() throws SQLException {

        statementListener.executionStarted(this);

        Object syncObject = getSynchronizationObject();
        synchronized (syncObject) {
            try {
                prepareFixedStatement(procedureCall.getSQL(isSelectableProcedure()), true);
            } catch (GDSException ge) {
                throw new FBSQLException(ge);
            }
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
        boolean hasResultSet = false;
        Object syncObject = getSynchronizationObject();
        synchronized (syncObject) {
            notifyStatementStarted();

            try {
                currentRs = null;

                prepareFixedStatement(procedureCall.getSQL(isSelectableProcedure()), true);
                hasResultSet = internalExecute(!isSelectableProcedure());

                if (hasResultSet)
                    setRequiredTypes();
            }catch (GDSException ge) {
                throw new FBSQLException(ge);
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

        Object syncObject = getSynchronizationObject();
        synchronized (syncObject) {
            notifyStatementStarted();

            try {
                currentRs = null;

                prepareFixedStatement(procedureCall.getSQL(isSelectableProcedure()), true);

                if (!internalExecute(!isSelectableProcedure()))
                	throw new FBSQLException(
                            "No resultset for sql",
                            FBSQLException.SQL_STATE_NO_RESULT_SET);

                getResultSet();

                setRequiredTypes();

                return getCurrentResultSet();

            } catch (GDSException ex) {
                throw new FBSQLException(ex);
            }
        }
    }

    /**
     * Execute query. This method prepares statement before execution. Rest of
     * the processing is done by superclass.
     */
    public int executeUpdate() throws SQLException {
        Object syncObject = getSynchronizationObject();
        synchronized (syncObject) {
            try {
                notifyStatementStarted();

                currentRs = null;

                prepareFixedStatement(procedureCall.getSQL(isSelectableProcedure()), true);

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
            } catch (GDSException ex) {
                throw new FBSQLException(ex);
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

        int counter = 0;

        List inputParams = procedureCall.getInputParams();
        Iterator iter = inputParams.iterator();
        while (iter.hasNext()) {
            FBProcedureParam param = (FBProcedureParam) iter.next();

            if (param != null && param.isParam()) {

                counter++;

                Object value = param.getValue();
                FBField field = getField(counter);

                if (value == null)
                    field.setNull();
                else if (value instanceof WrapperWithCalendar) {
                    setField(field, (WrapperWithCalendar)value);
                } else if (value instanceof WrapperWithInt) {
                    setField(field, (WrapperWithInt)value);
                } else
                    field.setObject(value);

                isParamSet[counter - 1] = true;
            }
        }

        return super.internalExecute(sendOutParams);
    }

    private void setField(FBField field, WrapperWithInt value) throws SQLException {
        Object obj = value.getValue();

        if (obj == null) {
            field.setNull();
        } else {
            int intValue = value.getIntValue();

            if (obj instanceof InputStream)
                field.setBinaryStream((InputStream) obj, intValue);
            else if (obj instanceof Reader)
                field.setCharacterStream((Reader) obj, intValue);
            else
                throw new TypeConversionException("Cannot convert type " + obj.getClass().getName());
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
    public void registerOutParameter(int parameterIndex, int sqlType)
            throws SQLException
    {
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
    public void registerOutParameter(int parameterIndex, int sqlType, int scale)
        throws SQLException
    {
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
        assertHasData(getCurrentResultSet());
        return getCurrentResultSet().wasNull();
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
        assertHasData(getCurrentResultSet());
        parameterIndex = procedureCall.mapOutParamIndexToPosition(parameterIndex);
        return getCurrentResultSet().getString(parameterIndex);
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
        assertHasData(getCurrentResultSet());
        parameterIndex = procedureCall.mapOutParamIndexToPosition(parameterIndex);
        return getCurrentResultSet().getBoolean(parameterIndex);
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
        assertHasData(getCurrentResultSet());
        parameterIndex = procedureCall.mapOutParamIndexToPosition(parameterIndex);
        return getCurrentResultSet().getByte(parameterIndex);
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
        assertHasData(getCurrentResultSet());
        parameterIndex = procedureCall.mapOutParamIndexToPosition(parameterIndex);
        return getCurrentResultSet().getShort(parameterIndex);
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
        assertHasData(getCurrentResultSet());
        parameterIndex = procedureCall.mapOutParamIndexToPosition(parameterIndex);
        return getCurrentResultSet().getInt(parameterIndex);
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
        assertHasData(getCurrentResultSet());
        parameterIndex = procedureCall.mapOutParamIndexToPosition(parameterIndex);
        return getCurrentResultSet().getLong(parameterIndex);
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
        assertHasData(getCurrentResultSet());
        parameterIndex = procedureCall.mapOutParamIndexToPosition(parameterIndex);
        return getCurrentResultSet().getFloat(parameterIndex);
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
        assertHasData(getCurrentResultSet());
        parameterIndex = procedureCall.mapOutParamIndexToPosition(parameterIndex);
        return getCurrentResultSet().getDouble(parameterIndex);
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
    public BigDecimal getBigDecimal(int parameterIndex, int scale)
        throws SQLException
    {
        assertHasData(getCurrentResultSet());
        parameterIndex = procedureCall.mapOutParamIndexToPosition(parameterIndex);
        return getCurrentResultSet().getBigDecimal(parameterIndex, scale);
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
        assertHasData(getCurrentResultSet());
        parameterIndex = procedureCall.mapOutParamIndexToPosition(parameterIndex);
        return getCurrentResultSet().getBytes(parameterIndex);
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
        assertHasData(getCurrentResultSet());
        parameterIndex = procedureCall.mapOutParamIndexToPosition(parameterIndex);
        return getCurrentResultSet().getDate(parameterIndex);
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
        assertHasData(getCurrentResultSet());
        parameterIndex = procedureCall.mapOutParamIndexToPosition(parameterIndex);
        return getCurrentResultSet().getTime(parameterIndex);
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
    public Timestamp getTimestamp(int parameterIndex)
        throws SQLException
    {
        assertHasData(getCurrentResultSet());
        parameterIndex = procedureCall.mapOutParamIndexToPosition(parameterIndex);
        return getCurrentResultSet().getTimestamp(parameterIndex);
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
        assertHasData(getCurrentResultSet());
        parameterIndex = procedureCall.mapOutParamIndexToPosition(parameterIndex);
        return getCurrentResultSet().getObject(parameterIndex);
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
    public Object getObject(int parameterIndex, Map map) throws SQLException {
        assertHasData(getCurrentResultSet());
        parameterIndex = procedureCall.mapOutParamIndexToPosition(parameterIndex);
        return getCurrentResultSet().getObject(parameterIndex, map);
    }
    
    public Object getObject(String colName, Map map) throws SQLException {
        return getObject(findOutParameter(colName), map);
    }
    
    public <T> T getObject(int parameterIndex, Class<T> type) throws SQLException {
        assertHasData(getCurrentResultSet());
        parameterIndex = procedureCall.mapOutParamIndexToPosition(parameterIndex);
        return ((FBResultSet)getCurrentResultSet()).getObject(parameterIndex, type);
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
        assertHasData(getCurrentResultSet());
        parameterIndex = procedureCall.mapOutParamIndexToPosition(parameterIndex);
        return getCurrentResultSet().getBigDecimal(parameterIndex);
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
        assertHasData(getCurrentResultSet());
        parameterIndex = procedureCall.mapOutParamIndexToPosition(parameterIndex);
        return getCurrentResultSet().getRef(parameterIndex);
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
        assertHasData(getCurrentResultSet());
        parameterIndex = procedureCall.mapOutParamIndexToPosition(parameterIndex);
        return getCurrentResultSet().getBlob(parameterIndex);
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
        assertHasData(getCurrentResultSet());
        parameterIndex = procedureCall.mapOutParamIndexToPosition(parameterIndex);
        return getCurrentResultSet().getClob(parameterIndex);
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
        assertHasData(getCurrentResultSet());
        parameterIndex = procedureCall.mapOutParamIndexToPosition(parameterIndex);
        return getCurrentResultSet().getArray(parameterIndex);
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
    public java.sql.Date getDate(int parameterIndex, Calendar cal)
        throws SQLException
    {
        assertHasData(getCurrentResultSet());
        parameterIndex = procedureCall.mapOutParamIndexToPosition(parameterIndex);
        return getCurrentResultSet().getDate(parameterIndex, cal);
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
    public Time getTime(int parameterIndex, Calendar cal)
        throws SQLException
    {
        assertHasData(getCurrentResultSet());
        parameterIndex = procedureCall.mapOutParamIndexToPosition(parameterIndex);
        return getCurrentResultSet().getTime(parameterIndex, cal);
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
    public Timestamp getTimestamp(int parameterIndex, Calendar cal)
        throws SQLException
    {
        assertHasData(getCurrentResultSet());
        parameterIndex = procedureCall.mapOutParamIndexToPosition(parameterIndex);
        return getCurrentResultSet().getTimestamp(parameterIndex, cal);
    }

    public URL getURL(int colIndex) throws SQLException {
        assertHasData(getCurrentResultSet());
        return getCurrentResultSet().getURL(colIndex);
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
        assertHasData(getCurrentResultSet());
        parameterIndex = procedureCall.mapOutParamIndexToPosition(parameterIndex);
        return getCurrentResultSet().getCharacterStream(parameterIndex);
    }

    public Reader getCharacterStream(String parameterName) throws SQLException {
        return getCharacterStream(findOutParameter(parameterName));
    }

    public Reader getNCharacterStream(int parameterIndex) throws SQLException {
        assertHasData(getCurrentResultSet());
        parameterIndex = procedureCall.mapOutParamIndexToPosition(parameterIndex);
        return ((FBResultSet)getCurrentResultSet()).getNCharacterStream(parameterIndex);
    }

    public Reader getNCharacterStream(String parameterName) throws SQLException {
        return getNCharacterStream(findOutParameter(parameterName));
    }

    public String getNString(int parameterIndex) throws SQLException {
        assertHasData(getCurrentResultSet());
        parameterIndex = procedureCall.mapOutParamIndexToPosition(parameterIndex);
        return ((FBResultSet)getCurrentResultSet()).getNString(parameterIndex);
    }

    public String getNString(String parameterName) throws SQLException {
        return getNString(findOutParameter(parameterName));
    }

    public void setAsciiStream(String parameterName, InputStream x, long length)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setAsciiStream(String parameterName, InputStream x) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setBinaryStream(String parameterName, InputStream x, long length)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setBinaryStream(String parameterName, InputStream x) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setBlob(String parameterName, Blob x) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setBlob(String parameterName, InputStream inputStream, long length)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setBlob(String parameterName, InputStream inputStream) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setCharacterStream(String parameterName, Reader reader, long length)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setCharacterStream(String parameterName, Reader reader) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setClob(String parameterName, Clob x) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setClob(String parameterName, Reader reader, long length) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setClob(String parameterName, Reader reader) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setNCharacterStream(String parameterName, Reader value, long length)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setNCharacterStream(String parameterName, Reader value) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setNClob(String parameterName, Reader reader, long length) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setNClob(String parameterName, Reader reader) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setNString(String parameterName, String value) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void registerOutParameter(String param1, int param2) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void registerOutParameter(String param1, int param2, int param3) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void registerOutParameter(String param1, int param2, String param3) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setURL(String param1, URL param2) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setNull(String param1, int param2) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setBoolean(String param1, boolean param2) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setByte(String param1, byte param2) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setShort(String param1, short param2) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setInt(String param1, int param2) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setLong(String param1, long param2) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setFloat(String param1, float param2) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setDouble(String param1, double param2) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setBigDecimal(String param1, BigDecimal param2) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setString(String param1, String param2) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setBytes(String param1, byte[] param2) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setDate(String param1, Date param2) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setTime(String param1, Time param2) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setTimestamp(String param1, Timestamp param2) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setAsciiStream(String param1, InputStream param2, int param3) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setBinaryStream(String param1, InputStream param2, int param3) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setObject(String param1, Object param2, int param3, int param4) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setObject(String param1, Object param2, int param3) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setObject(String param1, Object param2) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setCharacterStream(String param1, Reader param2, int param3) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setDate(String param1, Date param2, Calendar param3) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setTime(String param1, Time param2, Calendar param3) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setTimestamp(String param1, Timestamp param2, Calendar param3) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setNull(String param1, int param2, String param3) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void setURL(int parameterIndex, URL x) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void registerOutParameter(int parameterIndex, int sqlType, String typeName)
            throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public ResultSet getGeneratedKeys() throws SQLException {
        throw new FBDriverNotCapableException();
    }

    /**
     * Asserts if the current statement has data to return. It checks if the
     * result set has a row with data.
     *
     * @param rs result set to test
     * @throws java.sql.SQLException when the result set has no data.
     */
    protected void assertHasData(ResultSet rs) throws SQLException {
        // check if we have a row, and try to move to the first position.
        if (rs.getRow() == 0)
            rs.next();
        else
            return;

        // check if we still have no row and throw an exception in this case.
        if (rs.getRow() == 0)
        	throw new FBSQLException(
                    "Current statement has not data to return.",
                        FBSQLException.SQL_STATE_NO_RESULT_SET);
    }

    // this method doesn't give an exception if it is called twice.
    public ResultSet getCurrentResultSet() throws SQLException {
        if (currentRs == null)
            currentRs = super.getResultSet();
        return currentRs;
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

    public void setBinaryStream(int parameterIndex, InputStream inputStream, int length)
            throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(
                new WrapperWithInt(inputStream, length));
    }

    public void setBlob(int parameterIndex, Blob blob) throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(blob);
    }

    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(Boolean.valueOf(x));
    }

    public void setByte(int parameterIndex, byte x) throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(new Byte(x));
    }

    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(x);
    }

    public void setCharacterStream(int parameterIndex, Reader reader, int length)
            throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(new WrapperWithInt(reader, length));
    }

    public void setClob(int parameterIndex, Clob x) throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(x);
    }

    public void setDate(int parameterIndex, java.sql.Date x, Calendar cal) throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(new WrapperWithCalendar(x, cal));
    }

    public void setDate(int parameterIndex, java.sql.Date x) throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(x);
    }

    public void setDouble(int parameterIndex, double x) throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(new Double(x));
    }

    public void setFloat(int parameterIndex, float x) throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(new Float(x));
    }

    public void setInt(int parameterIndex, int x) throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(new Integer(x));
    }

    public void setLong(int parameterIndex, long x) throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(new Long(x));
    }

    public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(null);
    }

    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(null);
    }

    public void setObject(int parameterIndex, Object x, int targetSqlType, int scale)
            throws SQLException {
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
        procedureCall.getInputParam(parameterIndex).setValue(new Short(x));
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

    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
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
        return getCurrentResultSet().findColumn(paramName);
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

    private static class WrapperWithInt {
        private final Object value;
        private final int intValue;

        private WrapperWithInt(Object value, int intValue) {
            this.value = value;
            this.intValue = intValue;
        }

        private Object getValue() {
            return value;
        }

        private int getIntValue() {
            return intValue;
        }
    }

}
