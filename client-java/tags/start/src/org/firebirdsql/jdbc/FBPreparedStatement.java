/*   The contents of this file are subject to the Mozilla Public
 *   License Version 1.1 (the "License"); you may not use this file
 *   except in compliance with the License. You may obtain a copy of
 *   the License at http://www.mozilla.org/MPL/
 *   Alternatively, the contents of this file may be used under the
 *   terms of the GNU Lesser General Public License Version 2 or later (the
 *   "LGPL"), in which case the provisions of the GPL are applicable
 *   instead of those above. You may obtain a copy of the Licence at
 *   http://www.gnu.org/copyleft/lgpl.html
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    relevant License for more details.
 *
 *    This file was created by members of the firebird development team.
 *    All individual contributions remain the Copyright (C) of those
 *    individuals.  Contributors to this file are either listed here or
 *    can be obtained from a CVS history command.
 *
 *    All rights reserved.

 */

package org.firebirdsql.jdbc;


// imports --------------------------------------
import java.math.BigDecimal;
import java.util.Calendar;

import java.sql.PreparedStatement;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;

/**
 *
 *   @see <related>
 *   @author David Jencks (davidjencks@earthlink.net)
 *   @version $ $
 */


/**
 * An object that represents a precompiled SQL statement.
 * <P>A SQL statement is precompiled and stored in a
 * <code>PreparedStatement</code> object. This object can then be used to
 * efficiently execute this statement multiple times. 
 *
 * <P><B>Note:</B> The setXXX methods for setting IN parameter values
 * must specify types that are compatible with the defined SQL type of
 * the input parameter. For instance, if the IN parameter has SQL type
 * <code>Integer</code>, then the method <code>setInt</code> should be used.
 *
 * <p>If arbitrary parameter type conversions are required, the method
 * <code>setObject</code> should be used with a target SQL type.
 * <br>
 * Example of setting a parameter; <code>con</code> is an active connection  
 * <pre><code>
 *   PreparedStatement pstmt = con.prepareStatement("UPDATE EMPLOYEES
 *                                     SET SALARY = ? WHERE ID = ?");
 *   pstmt.setBigDecimal(1, 153833.00)
 *   pstmt.setInt(2, 110592)
 * </code></pre>
 *
 * @see Connection#prepareStatement
 * @see ResultSet 
 * <P>
 * Some of the methods in this interface are new in the JDBC 2.0 API.
 */

public class FBPreparedStatement extends FBStatement implements PreparedStatement {

    /**
	 * Executes the SQL query in this <code>PreparedStatement</code> object
	 * and returns the result set generated by the query.
     *
     * @return a <code>ResultSet</code> object that contains the data produced by the
     * query; never <code>null</code>
     * @exception SQLException if a database access error occurs
     */
    public ResultSet executeQuery() throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


    /**
     * Executes the SQL INSERT, UPDATE or DELETE statement
	 * in this <code>PreparedStatement</code> object.
	 * In addition,
     * SQL statements that return nothing, such as SQL DDL statements,
     * can be executed.
     *
     * @return either the row count for INSERT, UPDATE or DELETE statements;
	 * or 0 for SQL statements that return nothing
     * @exception SQLException if a database access error occurs
     */
    public int executeUpdate() throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


    /**
     * Sets the designated parameter to SQL <code>NULL</code>.
     *
     * <P><B>Note:</B> You must specify the parameter's SQL type.
     *
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param sqlType the SQL type code defined in <code>java.sql.Types</code>
     * @exception SQLException if a database access error occurs
     */
    public void setNull(int parameterIndex, int sqlType) throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


    /**
     * Sets the designated parameter to a Java <code>boolean</code> value.
	 * The driver converts this
     * to an SQL <code>BIT</code> value when it sends it to the database.
     *
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x the parameter value
     * @exception SQLException if a database access error occurs
     */
    public void setBoolean(int parameterIndex, boolean x) throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


    /**
     * Sets the designated parameter to a Java <code>byte</code> value.  
	 * The driver converts this
     * to an SQL <code>TINYINT</code> value when it sends it to the database.
     *
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x the parameter value
     * @exception SQLException if a database access error occurs
     */
    public void setByte(int parameterIndex, byte x) throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


    /**
     * Sets the designated parameter to a Java <code>short</code> value. 
	 * The driver converts this
     * to an SQL <code>SMALLINT</code> value when it sends it to the database.
     *
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x the parameter value
     * @exception SQLException if a database access error occurs
     */
    public void setShort(int parameterIndex, short x) throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


    /**
     * Sets the designated parameter to a Java <code>int</code> value.  
	 * The driver converts this
     * to an SQL <code>INTEGER</code> value when it sends it to the database.
     *
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x the parameter value
     * @exception SQLException if a database access error occurs
     */
    public void setInt(int parameterIndex, int x) throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


    /**
     * Sets the designated parameter to a Java <code>long</code> value. 
	 * The driver converts this
     * to an SQL <code>BIGINT</code> value when it sends it to the database.
     *
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x the parameter value
     * @exception SQLException if a database access error occurs
     */
    public void setLong(int parameterIndex, long x) throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


    /**
     * Sets the designated parameter to a Java <code>float</code> value. 
	 * The driver converts this
     * to an SQL <code>FLOAT</code> value when it sends it to the database.
     *
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x the parameter value
     * @exception SQLException if a database access error occurs
     */
    public void setFloat(int parameterIndex, float x) throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


    /**
     * Sets the designated parameter to a Java <code>double</code> value.  
	 * The driver converts this
     * to an SQL <code>DOUBLE</code> value when it sends it to the database.
     *
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x the parameter value
     * @exception SQLException if a database access error occurs
     */
    public void setDouble(int parameterIndex, double x) throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


    /**
     * Sets the designated parameter to a <code>java.math.BigDecimal</code> value.  
     * The driver converts this to an SQL <code>NUMERIC</code> value when
     * it sends it to the database.
     *
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x the parameter value
     * @exception SQLException if a database access error occurs
     */
    public void setBigDecimal(int parameterIndex, BigDecimal x) throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


    /**
     * Sets the designated parameter to a Java <code>String</code> value. 
	 * The driver converts this
     * to an SQL <code>VARCHAR</code> or <code>LONGVARCHAR</code> value
	 * (depending on the argument's
     * size relative to the driver's limits on <code>VARCHAR</code> values)
	 * when it sends it to the database.
     *
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x the parameter value
     * @exception SQLException if a database access error occurs
     */
    public void setString(int parameterIndex, String x) throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


    /**
     * Sets the designated parameter to a Java array of bytes.  The driver converts
     * this to an SQL <code>VARBINARY</code> or <code>LONGVARBINARY</code>
	 * (depending on the argument's size relative to the driver's limits on
	 * <code>VARBINARY</code> values) when it sends it to the database.
     *
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x the parameter value 
     * @exception SQLException if a database access error occurs
     */
    public void setBytes(int parameterIndex, byte x[]) throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


    /**
     * Sets the designated parameter to a <code<java.sql.Date</code> value.  
	 * The driver converts this
     * to an SQL <code>DATE</code> value when it sends it to the database.
     *
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x the parameter value
     * @exception SQLException if a database access error occurs
     */
    public void setDate(int parameterIndex, java.sql.Date x) throws  SQLException {
    }


    /**
     * Sets the designated parameter to a <code>java.sql.Time</code> value.  
	 * The driver converts this
     * to an SQL <code>TIME</code> value when it sends it to the database.
     *
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x the parameter value
     * @exception SQLException if a database access error occurs
     */
    public void setTime(int parameterIndex, java.sql.Time x) throws  SQLException {
    }


    /**
     * Sets the designated parameter to a <code>java.sql.Timestamp</code> value.  
	 * The driver
     * converts this to an SQL <code>TIMESTAMP</code> value when it sends it to the
     * database.
     *
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x the parameter value 
     * @exception SQLException if a database access error occurs
     */
    public void setTimestamp(int parameterIndex, java.sql.Timestamp x) throws  SQLException {
    }


    /**
	 * Sets the designated parameter to the given input stream, which will have 
	 * the specified number of bytes.
     * When a very large ASCII value is input to a <code>LONGVARCHAR</code>
     * parameter, it may be more practical to send it via a
     * <code>java.io.InputStream</code>. Data will be read from the stream
     * as needed until end-of-file is reached.  The JDBC driver will
     * do any necessary conversion from ASCII to the database char format.
     * 
     * <P><B>Note:</B> This stream object can either be a standard
     * Java stream object or your own subclass that implements the
     * standard interface.
     *
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x the Java input stream that contains the ASCII parameter value
     * @param length the number of bytes in the stream 
     * @exception SQLException if a database access error occurs
     */
    public void setAsciiStream(int parameterIndex, java.io.InputStream x, int length) throws  SQLException {
    }


    /**
	 * Sets the designated parameter to the given input stream, which will have 
	 * the specified number of bytes.
     * When a very large UNICODE value is input to a <code>LONGVARCHAR</code>
     * parameter, it may be more practical to send it via a
     * <code>java.io.InputStream</code> object. The data will be read from the stream
     * as needed until end-of-file is reached.  The JDBC driver will
     * do any necessary conversion from UNICODE to the database char format.
	 * The byte format of the Unicode stream must be Java UTF-8, as
	 * defined in the Java Virtual Machine Specification.
     * 
     * <P><B>Note:</B> This stream object can either be a standard
     * Java stream object or your own subclass that implements the
     * standard interface.
     *
     * @param parameterIndex the first parameter is 1, the second is 2, ...  
     * @param x the java input stream which contains the
     * UNICODE parameter value 
     * @param length the number of bytes in the stream 
     * @exception SQLException if a database access error occurs
     * @deprecated
     */
    public void setUnicodeStream(int parameterIndex, java.io.InputStream x, 
			  int length) throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


    /**
	 * Sets the designated parameter to the given input stream, which will have 
	 * the specified number of bytes.
     * When a very large binary value is input to a <code>LONGVARBINARY</code>
     * parameter, it may be more practical to send it via a
     * <code>java.io.InputStream</code> object. The data will be read from the stream
     * as needed until end-of-file is reached.
     * 
     * <P><B>Note:</B> This stream object can either be a standard
     * Java stream object or your own subclass that implements the
     * standard interface.
     *
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x the java input stream which contains the binary parameter value
     * @param length the number of bytes in the stream 
     * @exception SQLException if a database access error occurs
     */
    public void setBinaryStream(int parameterIndex, java.io.InputStream x, 
			 int length) throws  SQLException {
    }


    /**
	 * Clears the current parameter values immediately.
     * <P>In general, parameter values remain in force for repeated use of a
     * statement. Setting a parameter value automatically clears its
     * previous value.  However, in some cases it is useful to immediately
     * release the resources used by the current parameter values; this can
     * be done by calling the method <code>clearParameters</code>.
     *
     * @exception SQLException if a database access error occurs
     */
    public void clearParameters() throws  SQLException {
    }


    //----------------------------------------------------------------------
    // Advanced features:

    /**
     * <p>Sets the value of the designated parameter with the given object. The second
	 * argument must be an object type; for integral values, the
     * <code>java.lang</code> equivalent objects should be used.
     *
     * <p>The given Java object will be converted to the given targetSqlType
     * before being sent to the database.
     *
     * If the object has a custom mapping (is of a class implementing the 
	 * interface <code>SQLData</code>),
     * the JDBC driver should call the method <code>SQLData.writeSQL</code> to write it 
     * to the SQL data stream.
     * If, on the other hand, the object is of a class implementing
	 * Ref, Blob, Clob, Struct, 
     * or Array, the driver should pass it to the database as a value of the 
     * corresponding SQL type.
     *
     * <p>Note that this method may be used to pass datatabase-
     * specific abstract data types. 
     *
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x the object containing the input parameter value
     * @param targetSqlType the SQL type (as defined in java.sql.Types) to be 
     * sent to the database. The scale argument may further qualify this type.
     * @param scale for java.sql.Types.DECIMAL or java.sql.Types.NUMERIC types,
     *          this is the number of digits after the decimal point.  For all other
     *          types, this value will be ignored.
     * @exception SQLException if a database access error occurs
     * @see Types 
     */
    public void setObject(int parameterIndex, Object x, int targetSqlType, int scale) throws  SQLException {
    }


   /**
	 * Sets the value of the designated parameter with the given object.
     * This method is like the method <code>setObject</code>
	 * above, except that it assumes a scale of zero.
     *
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x the object containing the input parameter value
     * @param targetSqlType the SQL type (as defined in java.sql.Types) to be 
     *                      sent to the database
     * @exception SQLException if a database access error occurs
     */
    public void setObject(int parameterIndex, Object x, int targetSqlType) throws  SQLException {
    }


    /**
     * <p>Sets the value of the designated parameter using the given object. 
	 * The second parameter must be of type <code>Object</code>; therefore, the
     * <code>java.lang</code> equivalent objects should be used for built-in types.
     *
     * <p>The JDBC specification specifies a standard mapping from
     * Java <code>Object</code> types to SQL types.  The given argument 
     * will be converted to the corresponding SQL type before being
     * sent to the database.
     *
     * <p>Note that this method may be used to pass datatabase-
     * specific abstract data types, by using a driver-specific Java
     * type.
     *
     * If the object is of a class implementing the interface <code>SQLData</code>,
     * the JDBC driver should call the method <code>SQLData.writeSQL</code>
	 * to write it to the SQL data stream.
     * If, on the other hand, the object is of a class implementing
	 * Ref, Blob, Clob, Struct, 
     * or Array, then the driver should pass it to the database as a value of the 
     * corresponding SQL type.
     *
     * This method throws an exception if there is an ambiguity, for example, if the
     * object is of a class implementing more than one of the interfaces named above.
     *
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x the object containing the input parameter value 
     * @exception SQLException if a database access error occurs
     */
    public void setObject(int parameterIndex, Object x) throws  SQLException {
    }


    /**
	 * Executes any kind of SQL statement.
     * Some prepared statements return multiple results; the <code>execute</code>
     * method handles these complex statements as well as the simpler
     * form of statements handled by the methods <code>executeQuery</code>
	 * and <code>executeUpdate</code>.
     *
     * @exception SQLException if a database access error occurs
     * @see Statement#execute
     */
    public boolean execute() throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


    //--------------------------JDBC 2.0-----------------------------

    /**
     * Adds a set of parameters to this <code>PreparedStatement</code>
	 * object's batch of commands.
     * 
     * @exception SQLException if a database access error occurs
     * @see Statement#addBatch
	 * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
	 *      2.0 API</a>
     */
    public void addBatch() throws  SQLException {
    }


    /**
	 * Sets the designated parameter to the given <code>Reader</code>
	 * object, which is the given number of characters long.
     * When a very large UNICODE value is input to a <code>LONGVARCHAR</code>
     * parameter, it may be more practical to send it via a
     * <code>java.io.Reader</code> object. The data will be read from the stream
     * as needed until end-of-file is reached.  The JDBC driver will
     * do any necessary conversion from UNICODE to the database char format.
     * 
     * <P><B>Note:</B> This stream object can either be a standard
     * Java stream object or your own subclass that implements the
     * standard interface.
     *
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x the java reader which contains the UNICODE data
     * @param length the number of characters in the stream 
     * @exception SQLException if a database access error occurs
	 * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
	 *      2.0 API</a>
     */
    public void setCharacterStream(int parameterIndex,
       			  java.io.Reader reader,
			  int length) throws  SQLException {
    }


    /**
     * Sets the designated parameter to the given
	 *  <code>REF(&lt;structured-type&gt;)</code> value.
     *
     * @param i the first parameter is 1, the second is 2, ...
     * @param x an SQL <code>REF</code> value
     * @exception SQLException if a database access error occurs
	 * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
	 *      2.0 API</a>
     */
    public void setRef (int i, Ref x) throws  SQLException {
    }


    /**
     * Sets the designated parameter to the given
	 *  <code>Blob</code> object.
     *
     * @param i the first parameter is 1, the second is 2, ...
     * @param x a <code>Blob</code> object that maps an SQL <code>BLOB</code> value
     * @exception SQLException if a database access error occurs
	 * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
	 *      2.0 API</a>
     */
    public void setBlob (int i, Blob x) throws  SQLException {
    }


    /**
     * Sets the designated parameter to the given
	 *  <code>Clob</code> object.
     *
     * @param i the first parameter is 1, the second is 2, ...
     * @param x a <code>Clob</code> object that maps an SQL <code>CLOB</code> value
     * @exception SQLException if a database access error occurs
	 * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
	 *      2.0 API</a>
     */
    public void setClob (int i, Clob x) throws  SQLException {
    }


    /**
     * Sets the designated parameter to the given
	 *  <code>Array</code> object.
     * Sets an Array parameter.
     *
     * @param i the first parameter is 1, the second is 2, ...
     * @param x an <code>Array</code> object that maps an SQL <code>ARRAY</code> value
     * @exception SQLException if a database access error occurs
	 * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
	 *      2.0 API</a>
     */
    public void setArray (int i, Array x) throws  SQLException {
    }


    /**
     * Gets the number, types and properties of a <code>ResultSet</code>
	 * object's columns.
     *
     * @return the description of a <code>ResultSet</code> object's columns
     * @exception SQLException if a database access error occurs
	 * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
	 *      2.0 API</a>
     */
    public ResultSetMetaData getMetaData() throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


    /**
     * Sets the designated parameter to the given <code>java.sql.Date</code> value,
	 * using the given <code>Calendar</code> object.  The driver uses
	 * the <code>Calendar</code> object to construct an SQL <code>DATE</code> value,
	 * which the driver then sends to the database.  With a
	 * a <code>Calendar</code> object, the driver can calculate the date
	 * taking into account a custom timezone.  If no
	 * <code>Calendar</code> object is specified, the driver uses the default
	 * timezone, which is that of the virtual machine running the application.
     *
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x the parameter value
	 * @param cal the <code>Calendar</code> object the driver will use
	 *            to construct the date
     * @exception SQLException if a database access error occurs
	 * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
	 *      2.0 API</a>
     */
    public void setDate(int parameterIndex, java.sql.Date x, Calendar cal) throws  SQLException {
    }


    /**
     * Sets the designated parameter to the given <code>java.sql.Time</code> value,
	 * using the given <code>Calendar</code> object.  The driver uses
	 * the <code>Calendar</code> object to construct an SQL <code>TIME</code> value,
	 * which the driver then sends to the database.  With a
	 * a <code>Calendar</code> object, the driver can calculate the time
	 * taking into account a custom timezone.  If no
	 * <code>Calendar</code> object is specified, the driver uses the default
	 * timezone, which is that of the virtual machine running the application.
     *
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x the parameter value
	 * @param cal the <code>Calendar</code> object the driver will use
	 *            to construct the time
     * @exception SQLException if a database access error occurs
	 * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
	 *      2.0 API</a>
     */
    public void setTime(int parameterIndex, java.sql.Time x, Calendar cal) throws  SQLException {
    }


    /**
     * Sets the designated parameter to the given <code>java.sql.Timestamp</code> value,
	 * using the given <code>Calendar</code> object.  The driver uses
	 * the <code>Calendar</code> object to construct an SQL <code>TIMESTAMP</code> value,
	 * which the driver then sends to the database.  With a
	 * a <code>Calendar</code> object, the driver can calculate the timestamp
	 * taking into account a custom timezone.  If no
	 * <code>Calendar</code> object is specified, the driver uses the default
	 * timezone, which is that of the virtual machine running the application.
     *
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x the parameter value 
	 * @param cal the <code>Calendar</code> object the driver will use
	 *            to construct the timestamp
     * @exception SQLException if a database access error occurs
	 * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
	 *      2.0 API</a>
     */
    public void setTimestamp(int parameterIndex, java.sql.Timestamp x, Calendar cal) throws  SQLException {
    }


    /**
     * Sets the designated parameter to SQL <code>NULL</code>.
	 * This version of the method <code>setNull</code> should
     * be used for user-defined types and REF type parameters.  Examples
     * of user-defined types include: STRUCT, DISTINCT, JAVA_OBJECT, and 
     * named array types.
     *
     * <P><B>Note:</B> To be portable, applications must give the
     * SQL type code and the fully-qualified SQL type name when specifying
     * a NULL user-defined or REF parameter.  In the case of a user-defined type 
     * the name is the type name of the parameter itself.  For a REF 
     * parameter, the name is the type name of the referenced type.  If 
     * a JDBC driver does not need the type code or type name information, 
     * it may ignore it.     
     *
     * Although it is intended for user-defined and Ref parameters,
     * this method may be used to set a null parameter of any JDBC type.
     * If the parameter does not have a user-defined or REF type, the given
     * typeName is ignored.
     *
     *
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param sqlType a value from <code>java.sql.Types</code>
     * @param typeName the fully-qualified name of an SQL user-defined type;
     *  ignored if the parameter is not a user-defined type or REF 
     * @exception SQLException if a database access error occurs
	 * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
	 *      2.0 API</a>
     */
     public void setNull (int paramIndex, int sqlType, String typeName) throws  SQLException {
    }

}









