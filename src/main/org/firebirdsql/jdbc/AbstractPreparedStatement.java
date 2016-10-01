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
import org.firebirdsql.gds.ng.fields.FieldValue;
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
 * Implementation of {@link java.sql.PreparedStatement}interface. This class
 * contains all methods from the JDBC 2.0 specification.
 * 
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
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

    // this array contains either true or false indicating if parameter
    // was initialized, executeQuery, executeUpdate and execute methods
    // will throw an exception if this array contains at least one false value.
    protected boolean[] isParamSet;

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
        
        try {
            // TODO See http://tracker.firebirdsql.org/browse/JDBC-352
            notifyStatementStarted();
            prepareFixedStatement(sql);
        } catch (SQLException | RuntimeException e) {
            notifyStatementCompleted(false);
            throw e;
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

    /**
     * Executes the SQL query in this <code>PreparedStatement</code> object
     * and returns the result set generated by the query.
     * 
     * @return a <code>ResultSet</code> object that contains the data produced
     *         by the query; never <code>null</code>
     * @exception SQLException
     *                if a database access error occurs
     */
    @Override
    public ResultSet executeQuery() throws SQLException {
        checkValidity();
        synchronized (getSynchronizationObject()) {
            notifyStatementStarted();

            if (!internalExecute(isExecuteProcedureStatement))  
                throw new FBSQLException("No resultset for sql", SQLStateConstants.SQL_STATE_NO_RESULT_SET);

            return getResultSet();
        }
    }

    /**
     * Executes the SQL INSERT, UPDATE or DELETE statement in this
     * <code>PreparedStatement</code> object. In addition, SQL statements that
     * return nothing, such as SQL DDL statements, can be executed.
     * 
     * @return either the row count for INSERT, UPDATE or DELETE statements; or
     *         0 for SQL statements that return nothing
     * @exception SQLException
     *                if a database access error occurs
     */
    public int executeUpdate() throws SQLException {
        checkValidity();
        synchronized (getSynchronizationObject()) {
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
        return new FBParameterMetaData(fbStatement.getParameterDescriptor(), gdsHelper);
    }

    /**
     * Sets the designated parameter to SQL <code>NULL</code>.
     * 
     * <P>
     * <B>Note: </B> You must specify the parameter's SQL type.
     * 
     * @param parameterIndex
     *            the first parameter is 1, the second is 2, ...
     * @param sqlType
     *            the SQL type code defined in <code>java.sql.Types</code>
     * @exception SQLException
     *                if a database access error occurs
     */
    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        getField(parameterIndex).setNull();
        isParamSet[parameterIndex - 1] = true;
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream inputStream, int length) throws SQLException {
        getField(parameterIndex).setBinaryStream(inputStream, length);
        isParamSet[parameterIndex - 1] = true;
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        getField(parameterIndex).setBinaryStream(inputStream, length);
        isParamSet[parameterIndex - 1] = true;
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream inputStream) throws SQLException {
        getField(parameterIndex).setBinaryStream(inputStream);
        isParamSet[parameterIndex - 1] = true;
    }

    /**
     * Set the designated parameter to the given byte array.
     * 
     * @param parameterIndex
     *            the first parameter is 1, the second is 2, ...
     * @param x
     *            The byte array to be set
     * @throws SQLException
     *             if a database access occurs
     */
    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        getField(parameterIndex).setBytes(x);
        isParamSet[parameterIndex - 1] = true;
    }

    /**
     * Sets the designated parameter to the given boolean value.
     * 
     * @param parameterIndex
     *            the first parameter is 1, the second is 2, ...
     * @param x
     *            The boolean value to be set
     * @throws SQLException
     *             if a database access occurs
     */
    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        getField(parameterIndex).setBoolean(x);
        isParamSet[parameterIndex - 1] = true;
    }

    /**
     * Sets the designated parameter to the given byte value.
     * 
     * @param parameterIndex
     *            the first parameter is 1, the second is 2, ...
     * @param x
     *            The byte value to be set
     * @throws SQLException
     *             if a database access occurs
     */
    public void setByte(int parameterIndex, byte x) throws SQLException {
        getField(parameterIndex).setByte(x);
        isParamSet[parameterIndex - 1] = true;
    }

    /**
     * Sets the designated parameter to the given date value.
     * 
     * @param parameterIndex
     *            the first parameter is 1, the second is 2, ...
     * @param x
     *            The date value to be set
     * @throws SQLException
     *             if a database access occurs
     */
    public void setDate(int parameterIndex, Date x) throws SQLException {
        getField(parameterIndex).setDate(x);
        isParamSet[parameterIndex - 1] = true;
    }

    /**
     * Sets the designated parameter to the given double value.
     * 
     * @param parameterIndex
     *            the first parameter is 1, the second is 2, ...
     * @param x
     *            The double value to be set
     * @throws SQLException
     *             if a database access occurs
     */
    public void setDouble(int parameterIndex, double x) throws SQLException {
        getField(parameterIndex).setDouble(x);
        isParamSet[parameterIndex - 1] = true;
    }

    /**
     * Sets the designated parameter to the given floate value.
     * 
     * @param parameterIndex
     *            the first parameter is 1, the second is 2, ...
     * @param x
     *            The float value to be set
     * @throws SQLException
     *             if a database access occurs
     */
    public void setFloat(int parameterIndex, float x) throws SQLException {
        getField(parameterIndex).setFloat(x);
        isParamSet[parameterIndex - 1] = true;
    }

    /**
     * Sets the designated parameter to the given int value.
     * 
     * @param parameterIndex
     *            the first parameter is 1, the second is 2, ...
     * @param x
     *            The int value to be set
     * @throws SQLException
     *             if a database access occurs
     */
    public void setInt(int parameterIndex, int x) throws SQLException {
        getField(parameterIndex).setInteger(x);
        isParamSet[parameterIndex - 1] = true;
    }

    /**
     * Sets the designated parameter to the given long value.
     * 
     * @param parameterIndex
     *            the first parameter is 1, the second is 2, ...
     * @param x
     *            The long value to be set
     * @throws SQLException
     *             if a database access occurs
     */
    public void setLong(int parameterIndex, long x) throws SQLException {
        getField(parameterIndex).setLong(x);
        isParamSet[parameterIndex - 1] = true;
    }

    /**
     * Sets the value of the designated parameter with the given object.
     * 
     * @param parameterIndex
     *            the first parameter is 1, the second is 2, ...
     * @param x
     *            the object containing the parameter value
     * @throws SQLException
     *             if a database access error occurs
     */
    public void setObject(int parameterIndex, Object x) throws SQLException {
        getField(parameterIndex).setObject(x);
        isParamSet[parameterIndex - 1] = true;
    }

    /**
     * Sets the designated parameter to the given short value.
     * 
     * @param parameterIndex
     *            the first parameter is 1, the second is 2, ...
     * @param x
     *            The short value to be set
     * @throws SQLException
     *             if a database access occurs
     */
    public void setShort(int parameterIndex, short x) throws SQLException {
        getField(parameterIndex).setShort(x);
        isParamSet[parameterIndex - 1] = true;
    }

    /**
     * Sets the designated parameter to the given String value.
     * 
     * @param parameterIndex
     *            the first parameter is 1, the second is 2, ...
     * @param x
     *            The String value to be set
     * @throws SQLException
     *             if a database access occurs
     */
    public void setString(int parameterIndex, String x) throws SQLException {
        getField(parameterIndex).setString(x);
        isParamSet[parameterIndex - 1] = true;
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
        if (field instanceof FBWorkaroundStringField)
            ((FBWorkaroundStringField) field).setStringForced(x);
        else
            field.setString(x);
        isParamSet[parameterIndex - 1] = true;
    }

    /**
     * Sets the designated parameter to the given Time value.
     * 
     * @param parameterIndex
     *            the first parameter is 1, the second is 2, ...
     * @param x
     *            The Time value to be set
     * @throws SQLException
     *             if a database access occurs
     */
    public void setTime(int parameterIndex, Time x) throws SQLException {
        getField(parameterIndex).setTime(x);
        isParamSet[parameterIndex - 1] = true;
    }

    /**
     * Sets the designated parameter to the given Timestamp value.
     * 
     * @param parameterIndex
     *            the first parameter is 1, the second is 2, ...
     * @param x
     *            The Timestamp value to be set
     * @throws SQLException
     *             if a database access occurs
     */
    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        getField(parameterIndex).setTimestamp(x);
        isParamSet[parameterIndex - 1] = true;
    }

    /**
     * Sets the designated parameter to the given BigDecimal
     * 
     * @param parameterIndex
     *            The first parameter is 1, second is 2, ...
     * @param x
     *            The BigDecimal to be set as a parameter
     * @throws SQLException
     *             if a database access error occurs
     */
    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        getField(parameterIndex).setBigDecimal(x);
        isParamSet[parameterIndex - 1] = true;
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
     * Sets the designated parameter to the given input stream, which will have
     * the specified number of bytes. When a very large ASCII value is input to
     * a <code>LONGVARCHAR</code> parameter, it may be more practical to send
     * it via a <code>java.io.InputStream</code>. Data will be read from the
     * stream as needed until end-of-file is reached. The JDBC driver will do
     * any necessary conversion from ASCII to the database char format.
     * 
     * <P>
     * <B>Note: </B> This stream object can either be a standard Java stream
     * object or your own subclass that implements the standard interface.
     * 
     * @param parameterIndex
     *            the first parameter is 1, the second is 2, ...
     * @param x
     *            the Java input stream that contains the ASCII parameter value
     * @param length
     *            the number of bytes in the stream
     * @exception SQLException
     *                if a database access error occurs
     */
    @Override
    public final void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
        setBinaryStream(parameterIndex, x, length);
    }

    @Override
    public final void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
        setBinaryStream(parameterIndex, x, length);
    }

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

    /**
     * Clears the current parameter values immediately.
     * <P>
     * In general, parameter values remain in force for repeated use of a
     * statement. Setting a parameter value automatically clears its previous
     * value. However, in some cases it is useful to immediately release the
     * resources used by the current parameter values; this can be done by
     * calling the method <code>clearParameters</code>.
     * 
     * @exception SQLException
     *                if a database access error occurs
     */
    public void clearParameters() throws SQLException {
        checkValidity();
        if (fieldValues == null) return;

        // TODO Remove: should be based on FieldValue#isInitialized
        Arrays.fill(isParamSet, false);

        for (FieldValue fieldValue : fieldValues) {
            fieldValue.reset();
        }
    }

    // ----------------------------------------------------------------------
    // Advanced features:

    /**
     * <p>
     * Sets the value of the designated parameter with the given object. The
     * second argument must be an object type; for integral values, the
     * <code>java.lang</code> equivalent objects should be used.
     * 
     * <p>
     * The given Java object will be converted to the given targetSqlType before
     * being sent to the database.
     * 
     * If the object has a custom mapping (is of a class implementing the
     * interface <code>SQLData</code>), the JDBC driver should call the
     * method <code>SQLData.writeSQL</code> to write it to the SQL data
     * stream. If, on the other hand, the object is of a class implementing Ref,
     * Blob, Clob, Struct, or Array, the driver should pass it to the database
     * as a value of the corresponding SQL type.
     * 
     * <p>
     * Note that this method may be used to pass datatabase- specific abstract
     * data types.
     * 
     * @param parameterIndex
     *            the first parameter is 1, the second is 2, ...
     * @param x
     *            the object containing the input parameter value
     * @param targetSqlType
     *            the SQL type (as defined in java.sql.Types) to be sent to the
     *            database. The scale argument may further qualify this type.
     * @param scale
     *            for java.sql.Types.DECIMAL or java.sql.Types.NUMERIC types,
     *            this is the number of digits after the decimal point. For all
     *            other types, this value will be ignored.
     * @exception SQLException
     *                if a database access error occurs
     * @see Types
     */
    public void setObject(int parameterIndex, Object x, int targetSqlType, int scale) throws SQLException {
        // Workaround for JBuilder DataSets
        setObject(parameterIndex, x);
        isParamSet[parameterIndex - 1] = true;
    }

    /**
     * Sets the value of the designated parameter with the given object. This
     * method is like the method <code>setObject</code> above, except that it
     * assumes a scale of zero.
     * 
     * @param parameterIndex
     *            the first parameter is 1, the second is 2, ...
     * @param x
     *            the object containing the input parameter value
     * @param targetSqlType
     *            the SQL type (as defined in java.sql.Types) to be sent to the
     *            database
     * @exception SQLException
     *                if a database access error occurs
     */
    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        // well, for now
        setObject(parameterIndex, x);
        isParamSet[parameterIndex - 1] = true;
    }

    /**
     * Executes any kind of SQL statement. Some prepared statements return
     * multiple results; the <code>execute</code> method handles these complex
     * statements as well as the simpler form of statements handled by the
     * methods <code>executeQuery</code> and <code>executeUpdate</code>.
     * 
     * @exception SQLException
     *                if a database access error occurs
     * @see Statement#execute
     */
    public boolean execute() throws SQLException {
        checkValidity();
        synchronized (getSynchronizationObject()) {
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
        checkValidity();
        synchronized (getSynchronizationObject()) {
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
        boolean canExecute = true;
        // TODO replace with FieldValue#isInitialized
        for (boolean anIsParamSet : isParamSet) {
            canExecute = canExecute && anIsParamSet;
        }

        if (!canExecute)
            throw new FBMissingParameterException("Not all parameters were set.", isParamSet);

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
        for (int i = 0; i < isParamSet.length; i++) {
            FBField field = getField(i + 1);

            if (!(field instanceof FBFlushableField)) continue;

            ((FBFlushableField) field).flushCachedData();
        }
    }

    // TODO: AbstractCallableStatement adds FBProcedureCall, while AbstractPreparedStatement adds RowValue: separate?
    protected final List<Object> batchList = new ArrayList<>();

    /**
     * Adds a set of parameters to this <code>PreparedStatement</code>
     * object's batch of commands.
     * 
     * @exception SQLException
     *                if a database access error occurs
     * @see Statement#addBatch
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API
     *      </a>
     */
    public void addBatch() throws SQLException {
        checkValidity();
        boolean allParamsSet = true;
        // TODO Replace with check of FieldValue#isInitialized
        for (boolean anIsParamSet : isParamSet) {
            allParamsSet &= anIsParamSet;
        }

        if (!allParamsSet) throw new FBSQLException("Not all parameters set.");

        final RowValue batchedValues = fieldValues.deepCopy();
        for (int i = 0; i < batchedValues.getCount(); i++) {
            FBField field = getField(i + 1);
            if (field instanceof FBFlushableField)
                batchedValues.getFieldValue(i).setCachedObject(((FBFlushableField) field).getCachedObject());
        }

        batchList.add(batchedValues);
    }

    /**
     * Makes the set of commands in the current batch empty. This method is
     * optional.
     * 
     * @exception SQLException
     *                if a database access error occurs or the driver does not
     *                support batch statements
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API
     *      </a>
     */
    public void clearBatch() throws SQLException {
        batchList.clear();
    }

    @Override
    protected List<Long> executeBatchInternal() throws SQLException {
        checkValidity();
        synchronized (getSynchronizationObject()) {
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
                        RowValue data = (RowValue) iter.next();

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

    private void executeSingleForBatch(RowValue data, List<Long> results) throws SQLException {
        for (int i = 0; i < fieldValues.getCount(); i++) {
            FieldValue fieldValue = fieldValues.getFieldValue(i);
            fieldValue.reset();

            FBField field = getField(i + 1);
            if (field instanceof FBFlushableField) {
                // Explicitly set to null to ensure initialized property set to true
                fieldValue.setFieldData(null);
                ((FBFlushableField) field).setCachedObject((CachedObject) data.getFieldValue(i).getCachedObject());
            } else {
                fieldValue.setFieldData(data.getFieldValue(i).getFieldData());
            }
            isParamSet[i] = true;
        }

        if (internalExecute(isExecuteProcedureStatement)) {
            // TODO SQL state
            throw jdbcVersionSupport.createBatchUpdateException(
                    "Statements executed as batch should not produce a result set",
                    SQLStateConstants.SQL_STATE_GENERAL_ERROR, 0, toLargeArray(results), null);
        }

        results.add(getLargeUpdateCount());
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
        getField(parameterIndex).setCharacterStream(reader, length);
        isParamSet[parameterIndex - 1] = true;
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        getField(parameterIndex).setCharacterStream(reader, length);
        isParamSet[parameterIndex - 1] = true;
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        getField(parameterIndex).setCharacterStream(reader);
        isParamSet[parameterIndex - 1] = true;
    }
    
    /**
     * Sets the designated parameter to the given
     * <code>REF(&lt;structured-type&gt;)</code> value.
     * 
     * @param i
     *            the first parameter is 1, the second is 2, ...
     * @param x
     *            an SQL <code>REF</code> value
     * @exception SQLException
     *                if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API
     *      </a>
     */
    public void setRef(int i, Ref x) throws SQLException {
        throw new FBDriverNotCapableException("Type REF not supported");
    }

    /**
     * Sets the designated parameter to the given <code>Blob</code> object.
     * 
     * @param parameterIndex
     *            the first parameter is 1, the second is 2, ...
     * @param blob
     *            a <code>Blob</code> object that maps an SQL
     *            <code>BLOB</code> value
     * @exception SQLException
     *                if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API
     *      </a>
     */
    @Override
    public void setBlob(int parameterIndex, Blob blob) throws SQLException {
        // if the passed BLOB is not instance of our class, copy its content into the our BLOB
        if (blob != null && !(blob instanceof FBBlob)) {
            FBBlob fbb = new FBBlob(gdsHelper, blobListener);
            fbb.copyStream(blob.getBinaryStream());
            blob = fbb;
        } 
        
        getField(parameterIndex).setBlob((FBBlob) blob);
        isParamSet[parameterIndex - 1] = true;
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

    /**
     * Sets the designated parameter to the given <code>Clob</code> object.
     * 
     * @param parameterIndex
     *            the first parameter is 1, the second is 2, ...
     * @param clob
     *            a <code>Clob</code> object that maps an SQL
     *            <code>CLOB</code> value
     * @exception SQLException
     *                if a database access error occurs
     * @since 1.2
     */
    @Override
    public void setClob(int parameterIndex, Clob clob) throws SQLException {
        // if the passed BLOB is not instance of our class, copy its content into the our BLOB
        if (!(clob instanceof FBClob)) {
            FBClob fbc = new FBClob(new FBBlob(gdsHelper, blobListener));
            fbc.copyCharacterStream(clob.getCharacterStream());
            clob = fbc;
        } 
        
        getField(parameterIndex).setClob((FBClob) clob);
        isParamSet[parameterIndex - 1] = true;
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
     * Sets the designated parameter to the given <code>Array</code> object.
     * Sets an Array parameter.
     * 
     * @param i
     *            the first parameter is 1, the second is 2, ...
     * @param x
     *            an <code>Array</code> object that maps an SQL
     *            <code>ARRAY</code> value
     * @exception SQLException
     *                if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API
     *      </a>
     */
    public void setArray(int i, Array x) throws SQLException {
        throw new FBDriverNotCapableException("Type ARRAY not yet supported");
    }

    /**
     * Retrieves a <code>ResultSetMetaData</code> object that contains
     * information about the columns of the <code>ResultSet</code> object
     * that will be returned when this <code>PreparedStatement</code> object
     * is executed.
     * <P>
     * Because a <code>PreparedStatement</code> object is precompiled, it is
     * possible to know about the <code>ResultSet</code> object that it will
     * return without having to execute it.  Consequently, it is possible
     * to invoke the method <code>getMetaData</code> on a
     * <code>PreparedStatement</code> object rather than waiting to execute
     * it and then invoking the <code>ResultSet.getMetaData</code> method
     * on the <code>ResultSet</code> object that is returned.
     * <P>
     *
     * @return the description of a <code>ResultSet</code> object's columns or
     *         <code>null</code> if the driver cannot return a
     *         <code>ResultSetMetaData</code> object
     * @exception SQLException if a database access error occurs or
     * this method is called on a closed <code>PreparedStatement</code>
     * @exception SQLFeatureNotSupportedException if the JDBC driver does not support
     * this method
     * @since 1.2
     */
    public ResultSetMetaData getMetaData() throws SQLException {
        checkValidity();
        return new FBResultSetMetaData(fbStatement.getFieldDescriptor(), gdsHelper);
    }

    /**
     * Sets the designated parameter to the given <code>java.sql.Date</code>
     * value, using the given <code>Calendar</code> object. The driver uses
     * the <code>Calendar</code> object to construct an SQL <code>DATE</code>
     * value, which the driver then sends to the database. With a a
     * <code>Calendar</code> object, the driver can calculate the date taking
     * into account a custom timezone. If no <code>Calendar</code> object is
     * specified, the driver uses the default timezone, which is that of the
     * virtual machine running the application.
     * 
     * @param parameterIndex
     *            the first parameter is 1, the second is 2, ...
     * @param x
     *            the parameter value
     * @param cal
     *            the <code>Calendar</code> object the driver will use to
     *            construct the date
     * @exception SQLException
     *                if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API
     *      </a>
     */
    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
        getField(parameterIndex).setDate(x, cal);
        isParamSet[parameterIndex - 1] = true;
    }

    /**
     * Sets the designated parameter to the given <code>java.sql.Time</code>
     * value, using the given <code>Calendar</code> object. The driver uses
     * the <code>Calendar</code> object to construct an SQL <code>TIME</code>
     * value, which the driver then sends to the database. With a a
     * <code>Calendar</code> object, the driver can calculate the time taking
     * into account a custom timezone. If no <code>Calendar</code> object is
     * specified, the driver uses the default timezone, which is that of the
     * virtual machine running the application.
     * 
     * @param parameterIndex
     *            the first parameter is 1, the second is 2, ...
     * @param x
     *            the parameter value
     * @param cal
     *            the <code>Calendar</code> object the driver will use to
     *            construct the time
     * @exception SQLException
     *                if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API
     *      </a>
     */
    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        getField(parameterIndex).setTime(x, cal);
        isParamSet[parameterIndex - 1] = true;
    }

    /**
     * Sets the designated parameter to the given
     * <code>java.sql.Timestamp</code> value, using the given
     * <code>Calendar</code> object. The driver uses the <code>Calendar</code>
     * object to construct an SQL <code>TIMESTAMP</code> value, which the
     * driver then sends to the database. With a a <code>Calendar</code>
     * object, the driver can calculate the timestamp taking into account a
     * custom timezone. If no <code>Calendar</code> object is specified, the
     * driver uses the default timezone, which is that of the virtual machine
     * running the application.
     * 
     * @param parameterIndex
     *            the first parameter is 1, the second is 2, ...
     * @param x
     *            the parameter value
     * @param cal
     *            the <code>Calendar</code> object the driver will use to
     *            construct the timestamp
     * @exception SQLException
     *                if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API
     *      </a>
     */
    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        getField(parameterIndex).setTimestamp(x, cal);
        isParamSet[parameterIndex - 1] = true;
    }

    /**
     * Sets the designated parameter to SQL <code>NULL</code>. This version
     * of the method <code>setNull</code> should be used for user-defined
     * types and REF type parameters. Examples of user-defined types include:
     * STRUCT, DISTINCT, JAVA_OBJECT, and named array types.
     * 
     * <P>
     * <B>Note: </B> To be portable, applications must give the SQL type code
     * and the fully-qualified SQL type name when specifying a NULL user-defined
     * or REF parameter. In the case of a user-defined type the name is the type
     * name of the parameter itself. For a REF parameter, the name is the type
     * name of the referenced type. If a JDBC driver does not need the type code
     * or type name information, it may ignore it.
     * 
     * Although it is intended for user-defined and Ref parameters, this method
     * may be used to set a null parameter of any JDBC type. If the parameter
     * does not have a user-defined or REF type, the given typeName is ignored.
     * 
     * 
     * @param parameterIndex
     *            the first parameter is 1, the second is 2, ...
     * @param sqlType
     *            a value from <code>java.sql.Types</code>
     * @param typeName
     *            the fully-qualified name of an SQL user-defined type; ignored
     *            if the parameter is not a user-defined type or REF
     * @exception SQLException
     *                if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API
     *      </a>
     */
    public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
        // all nulls are represented the same irrespective of type
        setNull(parameterIndex, sqlType); 
    }

    /**
     * Prepare fixed statement and initialize parameters.
     */
    protected void prepareFixedStatement(String sql) throws SQLException {
        super.prepareFixedStatement(sql);

        RowDescriptor rowDescriptor = fbStatement.getParameterDescriptor();
        assert rowDescriptor != null : "RowDescriptor should not be null after prepare";

        isParamSet = new boolean[rowDescriptor.getCount()];
        fieldValues = rowDescriptor.createDefaultFieldValues();
        fields = new FBField[rowDescriptor.getCount()];

        for (int i = 0; i < isParamSet.length; i++) {
            FieldDataProvider dataProvider = fieldValues.getFieldValue(i);

            // FIXME check if we can safely pass cached here
            fields[i] = FBField.createField(getParameterDescriptor(i + 1), dataProvider, gdsHelper, false);
        }

        this.isExecuteProcedureStatement = fbStatement.getType() == StatementType.STORED_PROCEDURE;
    }

    /**
     * Get the execution plan of this PreparedStatement
     *
     * @return The execution plan of the statement
     */
    public String getExecutionPlan() throws SQLException {
        return super.getExecutionPlan();
    }

    /**
     * Get the statement type of this PreparedStatement.
     * The returned value will be one of the <code>TYPE_*</code> constant
     * values.
     *
     * @return The identifier for the given statement's type
     */
    public int getStatementType() throws SQLException {
        return super.getStatementType();
    }

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

    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        throw new FBDriverNotCapableException("Type ROWID not yet supported");
    }

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
}
