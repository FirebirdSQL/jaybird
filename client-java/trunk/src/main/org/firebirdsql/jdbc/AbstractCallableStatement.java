/*
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
import java.sql.*;
import java.util.*;
import java.util.Calendar;
import java.util.Map;

import org.firebirdsql.gds.DatabaseParameterBuffer;
import org.firebirdsql.gds.GDSException;
import org.firebirdsql.jdbc.field.FBField;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;


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
 */
public abstract class AbstractCallableStatement 
    extends AbstractPreparedStatement 
    implements CallableStatement, FirebirdCallableStatement 
{
    static final String NATIVE_CALL_COMMAND = "EXECUTE PROCEDURE";
    static final String NATIVE_SELECT_COMMAND = "SELECT * FROM";
    
    private final static Logger log = 
        LoggerFactory.getLogger(AbstractStatement.class,false);
    
    private ResultSet currentRs;
    
    protected boolean selectableProcedure;
    
    protected FBProcedureCall procedureCall;


    protected AbstractCallableStatement(AbstractConnection c, String sql, 
                                        int rsType, int rsConcurrency) 
    throws SQLException {
        super(c, rsType, rsConcurrency);
        
        DatabaseParameterBuffer dpb = c.getDatabaseParameterBuffer();
        
        int mode = FBEscapedParser.USE_BUILT_IN;
        
        if (dpb.hasArgument(DatabaseParameterBuffer.use_standard_udf))
            mode = FBEscapedParser.USE_STANDARD_UDF;
        
        FBEscapedCallParser parser = new FBEscapedCallParser(mode);
        
        // here statement is parsed twicel, once in c.nativeSQL(...)
        // and second time in parser.parseCall(...)... not nice, maybe 
        // in the future should be fixed by calling FBEscapedParser for
        // each parameter in FBEscapedCallParser class
        procedureCall = parser.parseCall(c.nativeSQL(sql));
    }
    
    public void addBatch() throws SQLException {
        throw new FBDriverNotCapableException();
    }
    public void clearBatch() throws SQLException {
        throw new FBDriverNotCapableException();
    }
    public int[] executeBatch() throws SQLException {
        throw new FBDriverNotCapableException();
    }
    
    /* (non-Javadoc)
     * @see org.firebirdsql.jdbc.FirebirdCallableStatement#setSelectableProcedure(boolean)
     */
    public void setSelectableProcedure(boolean selectableProcedure) {
        this.selectableProcedure = selectableProcedure;
    }
    
    /**
     * Set required types for output parameters.
     * 
     * @throws SQLException if something went wrong.
     */
    protected void setRequiredTypes() throws SQLException {
        
        FBResultSet resultSet = (FBResultSet)getCurrentResultSet();
        
        Iterator iter = procedureCall.getOutputParams().iterator();
        while(iter.hasNext()) {
            FBProcedureParam param = (FBProcedureParam)iter.next();
            
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
        
        Object syncObject = getSynchronizationObject();
        synchronized(syncObject) {
            try {
                c.ensureInTransaction();
                prepareFixedStatement(procedureCall.getSQL(selectableProcedure), true);
            } catch (GDSException ge) {
                throw new FBSQLException(ge);
            } finally {
                c.checkEndTransaction();
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
    public boolean execute() throws  SQLException {
        Object syncObject = getSynchronizationObject();
        synchronized(syncObject) {
            try {
                c.ensureInTransaction();
                
                currentRs = null;
                
                prepareFixedStatement(procedureCall.getSQL(selectableProcedure), true);
                boolean hasResultSet = internalExecute(!selectableProcedure);

                if (hasResultSet) {
                    if (c.willEndTransaction())
                        cacheResultSet();
                
                    setRequiredTypes();
                }
                
                return hasResultSet;
                
            } catch (GDSException ge) {
                throw new FBSQLException(ge);
            } finally {
                c.checkEndTransaction();
            } // end of try-catch-finally
        }
    }
    
    /**
     * Execute query. This method prepares statement before execution. Rest of
     * the processing is done by superclass.
     */
	public ResultSet executeQuery() throws SQLException {
        Object syncObject = getSynchronizationObject();
        
        synchronized(syncObject) {
            try {
                c.ensureInTransaction();
                
                currentRs = null;
                
                prepareFixedStatement(procedureCall.getSQL(selectableProcedure), true);
                
                if (!internalExecute(!selectableProcedure)) 
                    throw new FBSQLException(
                            "No resultset for sql",
                            FBSQLException.SQL_STATE_NO_RESULT_SET);
                

                if (c.willEndTransaction()) 
                    cacheResultSet();
                else 
                    getResultSet();

                setRequiredTypes();
                
                return getCurrentResultSet();
                
            } catch(GDSException ex) {
                throw new FBSQLException(ex);
            } finally {
                c.checkEndTransaction();
            }
        }
    }

    /**
     * Execute query. This method prepares statement before execution. Rest of
     * the processing is done by superclass.
     */
    public int executeUpdate() throws SQLException {
        Object syncObject = getSynchronizationObject();
        
        synchronized(syncObject) {
            try {
                c.ensureInTransaction();
                
                currentRs = null;
                
                prepareFixedStatement(procedureCall.getSQL(selectableProcedure), true);
                
                /*
                // R.Rokytskyy: JDBC CTS suite uses executeUpdate()
                // together with output parameters, therefore we cannot
                // throw exception if we want to pass the test suite
                
                if (internalExecute(true)) 
                    throw new FBSQLException(
                    "Update statement returned results.");
                */
                
                boolean hasResults = internalExecute(!selectableProcedure);
                
                if (hasResults) {
                    if (c.willEndTransaction())
                        cacheResultSet();
                
                    setRequiredTypes();
                }
                
                return getUpdateCount();
                
            } catch(GDSException ex) {
                throw new FBSQLException(ex);
            } finally {
                c.checkEndTransaction();
            }
        }
    }

    /**
     * Execute statement internally. This method sets cached parameters. Rest
     * of the processing is done by superclass.
     */
    protected boolean internalExecute(boolean sendOutParams)
    throws SQLException {
        
        int counter = 0;
        
        List inputParams = procedureCall.getInputParams();
        Iterator iter = inputParams.iterator();
        while(iter.hasNext()) {
            FBProcedureParam param = (FBProcedureParam)iter.next();
            
            if (param != null && param.isParam()) {
                
                counter++;
                
                Object value = param.getValue();
                FBField field = getField(counter);
                
                if (value == null)
                    field.setNull();
                else
                if (value instanceof TimestampWithCalendar)
                    field.setTimestamp(
                            (TimestampWithCalendar)value, 
                            ((TimestampWithCalendar)value).getCalendar());
                else
                    field.setObject(value);
            }
        }

        return super.internalExecute(sendOutParams);
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


    //----------------------------------------------------------------------
    // Advanced features:


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



    //--------------------------JDBC 2.0-----------------------------

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
    public Ref getRef (int parameterIndex) throws SQLException {
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
    public Blob getBlob (int parameterIndex) throws SQLException {
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
    public Clob getClob (int parameterIndex) throws SQLException {
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
    public Array getArray (int parameterIndex) throws SQLException {
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

    //--------------------------JDBC 3.0-----------------------------

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

    //this method doesn't give an exception if it is called twice.
    protected ResultSet getCurrentResultSet() throws SQLException {
        if (currentRs == null)
            currentRs = super.getResultSet();
        return currentRs;
    }
    
    protected void cacheResultSet() throws SQLException {
        
        if (currentRs != null)
            throw new FBDriverConsistencyCheckException(
                    "Trying to cache result set before closing exitsing one.");
        
        currentRs = getCachedResultSet(false);
    }
    
    public ResultSet getResultSet() throws SQLException {
        return getCurrentResultSet();
    }
    
    public void setArray(int i, Array x) throws SQLException {
        procedureCall.getInputParam(i).setValue(x);
    }

    public void setAsciiStream(int parameterIndex, InputStream x, int length)
    throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(x);
    }

    public void setBigDecimal(int parameterIndex, BigDecimal x)
        throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(x);
    }

    public void setBinaryStream(int parameterIndex, InputStream inputStream,
        int length) throws SQLException 
    {
        procedureCall.getInputParam(parameterIndex).setValue(inputStream);
    }

    public void setBlob(int parameterIndex, Blob blob) throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(blob);
    }

    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(new Boolean(x));
    }

    public void setByte(int parameterIndex, byte x) throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(new Byte(x));
    }

    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(x);
    }

    public void setCharacterStream(int parameterIndex, Reader reader,
        int length) throws SQLException 
    {
        procedureCall.getInputParam(parameterIndex).setValue(reader);
    }

    public void setClob(int parameterIndex, Clob x) throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(x);
    }

    public void setDate(int parameterIndex, java.sql.Date x, Calendar cal)
        throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(x);
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

    public void setNull(int parameterIndex, int sqlType, String typeName)
        throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(null);
    }

    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(null);
    }

    public void setObject(int parameterIndex, Object x, int targetSqlType,
        int scale) throws SQLException 
    {
        procedureCall.getInputParam(parameterIndex).setValue(x);
    }

    public void setObject(int parameterIndex, Object x, int targetSqlType)
        throws SQLException {
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

    public void setTime(int parameterIndex, Time x, Calendar cal)
        throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(x);
    }

    public void setTime(int parameterIndex, Time x) throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(x);
    }

    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal)
        throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(
                new TimestampWithCalendar(x, cal));
    }

    public void setTimestamp(int parameterIndex, Timestamp x)
        throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(x);
    }

    public void setUnicodeStream(int parameterIndex, InputStream x, int length)
        throws SQLException {
        procedureCall.getInputParam(parameterIndex).setValue(x);
    }
    
    private static class TimestampWithCalendar extends Timestamp {
        private Calendar c;
        
        private TimestampWithCalendar(Timestamp t, Calendar c) {
            super(t.getTime() + t.getNanos()/1000000);
            
            this.c = c;
        }
        
        private Calendar getCalendar() {
            return c;
        }
    }
}




