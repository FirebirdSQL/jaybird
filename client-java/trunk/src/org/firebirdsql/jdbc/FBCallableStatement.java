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
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;



/**
 *
 *   @see <related>
 *   @author David Jencks (davidjencks@earthlink.net)
 *   @version $ $
 */


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
	 * @see Connection#prepareCall
	 * @see ResultSet 
	 */
public class FBCallableStatement extends FBPreparedStatement implements CallableStatement {

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
			throws SQLException {
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
			throws SQLException {
        throw new SQLException("Not yet implemented");
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
    public 		boolean wasNull() throws SQLException {
        throw new SQLException("Not yet implemented");
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
    public 	String getString(int parameterIndex) throws SQLException {
        throw new SQLException("Not yet implemented");
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
    public 	boolean getBoolean(int parameterIndex) throws SQLException {
        throw new SQLException("Not yet implemented");
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
    public 	byte getByte(int parameterIndex) throws SQLException {
        throw new SQLException("Not yet implemented");
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
    public 	short getShort(int parameterIndex) throws SQLException {
        throw new SQLException("Not yet implemented");
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
    public 	int getInt(int parameterIndex) throws SQLException {
        throw new SQLException("Not yet implemented");
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
    public 	long getLong(int parameterIndex) throws SQLException {
        throw new SQLException("Not yet implemented");
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
    public 	float getFloat(int parameterIndex) throws SQLException {
        throw new SQLException("Not yet implemented");
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
    public 		double getDouble(int parameterIndex) throws SQLException {
        throw new SQLException("Not yet implemented");
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
		  throws SQLException {
        throw new SQLException("Not yet implemented");
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
    public 		byte[] getBytes(int parameterIndex) throws SQLException {
        throw new SQLException("Not yet implemented");
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
    public 		java.sql.Date getDate(int parameterIndex) throws SQLException {
        throw new SQLException("Not yet implemented");
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
    public 		java.sql.Time getTime(int parameterIndex) throws SQLException {
        throw new SQLException("Not yet implemented");
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
    public java.sql.Timestamp getTimestamp(int parameterIndex) 
			throws SQLException {
        throw new SQLException("Not yet implemented");
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
    public 		Object getObject(int parameterIndex) throws SQLException {
        throw new SQLException("Not yet implemented");
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
    public 		BigDecimal getBigDecimal(int parameterIndex) throws SQLException {
        throw new SQLException("Not yet implemented");
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
		 * @param i the first parameter is 1, the second is 2, and so on
		 * @param map the mapping from SQL type names to Java classes
		 * @return a <code>java.lang.Object</code> holding the OUT parameter value
		 * @exception SQLException if a database access error occurs
	     * @since 1.2
		 * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
		 */
    public 		 Object  getObject (int i, java.util.Map map) throws SQLException {
        throw new SQLException("Not yet implemented");
    }


		/**
		 *
		 * Gets the value of a JDBC <code>REF(&lt;structured-type&gt;)</code>
		 * parameter as a {@link Ref} object in the Java programming language.
		 * @param i the first parameter is 1, the second is 2, 
		 * and so on
		 * @return the parameter value as a <code>Ref</code> object in the
		 * Java programming language.  If the value was SQL <code>NULL</code>, the value
		 * <code>null</code> is returned.
		 * @exception SQLException if a database access error occurs
	     * @since 1.2
		 * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
		 */
    public 		 Ref getRef (int i) throws SQLException {
        throw new SQLException("Not yet implemented");
    }


		/**
		 *
		 * Gets the value of a JDBC <code>BLOB</code> parameter as a
		 * {@link Blob} object in the Java programming language.
		 * @param i the first parameter is 1, the second is 2, and so on
		 * @return the parameter value as a <code>Blob</code> object in the
		 * Java programming language.  If the value was SQL <code>NULL</code>, the value
		 * <code>null</code> is returned.
		 * @exception SQLException if a database access error occurs
		 * @since 1.2
		 * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
		 */
    public 		 Blob getBlob (int i) throws SQLException {
        throw new SQLException("Not yet implemented");
    }


		/**
		 *
		 * Gets the value of a JDBC <code>CLOB</code> parameter as a
		 * <code>Clob</code> object in the Java programming language.
		 * @param i the first parameter is 1, the second is 2, and
		 * so on
		 * @return the parameter value as a <code>Clob</code> object in the
		 * Java programming language.  If the value was SQL <code>NULL</code>, the
		 * value <code>null</code> is returned.
		 * @exception SQLException if a database access error occurs
		 * @since 1.2
		 * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
		 */
    public 		 Clob getClob (int i) throws SQLException {
        throw new SQLException("Not yet implemented");
    }


		/**
		 *
		 * Gets the value of a JDBC <code>ARRAY</code> parameter as an
		 * {@link Array} object in the Java programming language.
		 * @param i the first parameter is 1, the second is 2, and 
		 * so on
		 * @return the parameter value as an <code>Array</code> object in
		 * the Java programming language.  If the value was SQL <code>NULL</code>, the
		 * value <code>null</code> is returned.
		 * @exception SQLException if a database access error occurs
		 * @since 1.2
		 * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
		 */
    public 		 Array getArray (int i) throws SQLException {
        throw new SQLException("Not yet implemented");
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
		  throws SQLException {
        throw new SQLException("Not yet implemented");
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
    public java.sql.Time getTime(int parameterIndex, Calendar cal) 
		  throws SQLException {
        throw new SQLException("Not yet implemented");
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
    public java.sql.Timestamp getTimestamp(int parameterIndex, Calendar cal) 
		  throws SQLException {
        throw new SQLException("Not yet implemented");
    }



        /**
         *
         * Registers the designated output parameter.  This version of 
		 * the method <code>registerOutParameter</code>
         * should be used for a user-named or REF output parameter.  Examples
         * of user-named types include: STRUCT, DISTINCT, JAVA_OBJECT, and
         * named array types.
         *
         * Before executing a stored procedure call, you must explicitly
         * call <code>registerOutParameter</code> to register the type from
		 * <code>java.sql.Types</code> for each
         * OUT parameter.  For a user-named parameter the fully-qualified SQL
         * type name of the parameter should also be given, while a REF
         * parameter requires that the fully-qualified type name of the
         * referenced type be given.  A JDBC driver that does not need the
         * type code and type name information may ignore it.   To be portable,
         * however, applications should always provide these values for
         * user-named and REF parameters.
         *
         * Although it is intended for user-named and REF parameters,
         * this method may be used to register a parameter of any JDBC type.
         * If the parameter does not have a user-named or REF type, the
         * typeName parameter is ignored.
         *
         * <P><B>Note:</B> When reading the value of an out parameter, you
         * must use the <code>getXXX</code> method whose Java type XXX corresponds to the
         * parameter's registered SQL type.
         *
         * @param parameterIndex the first parameter is 1, the second is 2,...
         * @param sqlType a value from {@link java.sql.Types}
         * @param typeName the fully-qualified name of an SQL structured type
         * @exception SQLException if a database access error occurs
         * @see Types
		 * @since 1.2
		 * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
         */
    public void registerOutParameter (int paramIndex, int sqlType, String typeName)
     throws SQLException {
    }

}




