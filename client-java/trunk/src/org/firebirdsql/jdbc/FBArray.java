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
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 *   @see <related>
 *   @author David Jencks (davidjencks@earthlink.net)
 *   @version $ $
 */


 
/**
 *
 * <p>
 * The mapping in the Java programming language for the SQL type
 * <code>ARRAY</code>.
 * By default, an <code>Array</code> object is a transaction-duration 
 * reference to an SQL <code>ARRAY</code> value.  By default, an <code>Array</code>
 * object is implemented using an SQL LOCATOR(array) internally, which
 * means that an <code>Array</code> object contains a logical pointer
 * to the data in the SQL <code>ARRAY</code> value on the server rather
 * than containing the <code>ARRAY</code> value's data.
 * <p>
 * The <code>Array</code> interface provides methods for bringing an SQL
 * <code>ARRAY</code> value's data to the client as either an array or a
 * <code>ResultSet</code> object.
 * <p>
 * If the elements of the SQL <code>ARRAY</code>
 * are a UDT, they may be custom mapped.  To create a custom mapping,
 * a programmer must do two things:
 * <ul>
 * <li>create a class that implements the {@link SQLData}
 * interface for the UDT to be custom mapped. This will typically be
 * done using a tool.
 * <li>make an entry in a type map that contains 
 *   <ul>
 *   <li>the fully-qualified SQL type name of the UDT
 *   <li>the <code>Class</code> object for the class implementing
 *       <code>SQLData</code>
 *   </ul>
 * </ul>
 * <p>
 * When a type map with an entry for
 * the base type is supplied to the methods <code>getArray</code>
 * and <code>getResultSet</code>, the mapping
 * it contains will be used to map the elements of the <code>ARRAY</code> value.
 * If no type map is supplied, which would typically be the case,
 * the connection's type map is used by default.
 * If the connection's type map or a type map supplied to a method has no entry
 * for the base type, the elements are mapped according to the standard mapping.
 * <p>
 * <b>NOTE:</b> This interface is new in the JDBC 2.0 API.
 * 
 */

public class FBArray implements Array {

  /**
   * Returns the SQL type name of the elements in 
   * the array designated by this <code>Array</code> object.
   * If the elements are a built-in type, it returns
   * the database-specific type name of the elements. 
   * If the elements are a user-defined type (UDT),
   * this method returns the fully-qualified SQL type name.
   * @return a <code>String</code> that is the database-specific
   * name for a built-in base type or the fully-qualified SQL type
   * name for a base type that is a UDT
   * @exception SQLException if an error occurs while attempting
   * to access the type name
   * @since 1.2
   * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
   *      2.0 API</a>
   */
    public String getBaseTypeName() throws SQLException {
       return null;
    }


  /**
   * Returns the JDBC type of the elements in the array designated
   * by this <code>Array</code> object.
   * @return a constant from the class {@link java.sql.Types} that is
   * the type code for the elements in the array designated by this
   * <code>Array</code> object.
   * @exception SQLException if an error occurs while attempting
   * to access the base type 
   * @since 1.2
   * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
   *      2.0 API</a>
   */
    public int getBaseType() throws SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * Retrieves the contents of the SQL <code>ARRAY</code> value designated 
   * by this
   * <code>Array</code> object in the form of an array in the Java
   * programming language. This version of the method <code>getArray</code>
   * uses the type map associated with the connection for customizations of 
   * the type mappings.
   * @return an array in the Java programming language that contains 
   * the ordered elements of the SQL <code>ARRAY</code> value
   * designated by this object
   * @exception SQLException if an error occurs while attempting to
   * access the array
   * @since 1.2
   * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
   *      2.0 API</a>
   */
   public Object getArray() throws SQLException {
       return null;
   }

  /**
   * Retrieves the contents of the SQL array designated by this 
   * <code>Array</code> object.
   * This method uses 
   * the specified <code>map</code> for type map customizations
   * unless the base type of the array does not match a user-defined 
   * type in <code>map</code>, in which case it 
   * uses the standard mapping. This version of the method
   * <code>getArray</code> uses either the given type map or the standard mapping;
   * it never uses the type map associated with the connection.
   *
   * @param map a <code>java.util.Map</code> object that contains mappings
   *            of SQL type names to classes in the Java programming language
   * @return an array in the Java programming language that contains the ordered 
   *         elements of the SQL array designated by this object
   * @exception SQLException if an error occurs while attempting to 
   *                         access the array
   * @since 1.2
   * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
   *      2.0 API</a>
   */
    public Object getArray(java.util.Map map) throws SQLException {
        return null;
    }
      

  /**
   * Returns an array containing a slice of the SQL <code>ARRAY</code>
   * value designated by this <code>Array</code> object, beginning with the
   * specified <code>index</code> and containing up to <code>count</code> 
   * successive elements of the SQL array.  This method uses the type map
   * associated with the connection for customizations of the type mappings.
   * @param index the array index of the first element to retrieve;
   *              the first element is at index 1
   * @param count the number of successive SQL array elements to retrieve
   * @return an array containing up to <code>count</code> consecutive elements 
   * of the SQL array, beginning with element <code>index</code>
   * @exception SQLException if an error occurs while attempting to
   * access the array
   * @since 1.2
   * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
   *      2.0 API</a>
   */
    public Object getArray(long index, int count) throws SQLException {
       return null;
   }


  /**
   * Returns an array containing a slice of the SQL array object 
   * designated by this <code>Array</code> object, beginning with the specified
   * <code>index</code> and containing up to <code>count</code>
   * successive elements of the SQL array.  
   * <P>
   * This method uses 
   * the specified <code>map</code> for type map customizations
   * unless the base type of the array does not match a user-defined 
   * type in <code>map</code>, in which case it 
   * uses the standard mapping. This version of the method
   * <code>getArray</code> uses either the given type map or the standard mapping;
   * it never uses the type map associated with the connection.
   *
   * @param index the array index of the first element to retrieve;
   *              the first element is at index 1
   * @param count the number of successive SQL array elements to 
   * retrieve
   * @param map a <code>java.util.Map</code> object
   * that contains SQL type names and the classes in
   * the Java programming language to which they are mapped
   * @return an array containing up to <code>count</code>
   * consecutive elements of the SQL array designated by this
   * <code>Array</code> object, beginning with element 
   * <code>index</code>.
   * @exception SQLException if an error occurs while attempting to
   * access the array
   * @since 1.2
   * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
   *      2.0 API</a>
   */
    public Object getArray(long index, int count, java.util.Map map) 
       throws SQLException {
       return null;
   }


  /**
   * Returns a result set that contains the elements of the SQL 
   * <code>ARRAY</code> value
   * designated by this <code>Array</code> object.  If appropriate,
   * the elements of the array are mapped using the connection's type 
   * map; otherwise, the standard mapping is used.
   * <p>
   * The result set contains one row for each array element, with
   * two columns in each row.  The second column stores the element
   * value; the first column stores the index into the array for 
   * that element (with the first array element being at index 1). 
   * The rows are in ascending order corresponding to
   * the order of the indices.
   * @return a {@link ResultSet} object containing one row for each
   * of the elements in the array designated by this <code>Array</code>
   * object, with the rows in ascending order based on the indices.
   * @exception SQLException if an error occurs while attempting to
   * access the array
   * @since 1.2
   * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
   *      2.0 API</a>
   */
    public ResultSet getResultSet () throws SQLException {
       return null;
    }


  /**
   * Returns a result set that contains the elements of the SQL 
   * <code>ARRAY</code> value 
   * designated by this <code>Array</code> object.
   * This method uses 
   * the specified <code>map</code> for type map customizations
   * unless the base type of the array does not match a user-defined 
   * type in <code>map</code>, in which case it 
   * uses the standard mapping. This version of the method
   * <code>getResultSet</code> uses either the given type map or the standard mapping;
   * it never uses the type map associated with the connection.
   * <p>
   * The result set contains one row for each array element, with
   * two columns in each row.  The second column stores the element
   * value; the first column stores the index into the array for 
   * that element (with the first array element being at index 1). 
   * The rows are in ascending order corresponding to
   * the order of the indices.
   * @param map contains the mapping of SQL user-defined types to 
   * classes in the Java programming language
   * @return a <code>ResultSet</code> object containing one row for each
   * of the elements in the array designated by this <code>Array</code>
   * object, with the rows in ascending order based on the indices.
   * @exception SQLException if an error occurs while attempting to
   * access the array
   * @since 1.2
   * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
   *      2.0 API</a>
   */
    public ResultSet getResultSet (java.util.Map map) throws SQLException {
        return null;
    }


  /**
   * Returns a result set holding the elements of the subarray that
   * starts at index <code>index</code> and contains up to 
   * <code>count</code> successive elements.  This method uses
   * the connection's type map to map the elements of the array if
   * the map contains an entry for the base type. Otherwise, the
   * standard mapping is used.
   * <P>
   * The result set has one row for each element of the SQL array
   * designated by this object, with the first row containing the 
   * element at index <code>index</code>.  The result set has
   * up to <code>count</code> rows in ascending order based on the
   * indices.  Each row has two columns:  The second column stores
   * the element value; the first column stores the index into the
   * array for that element.
   * @param index the array index of the first element to retrieve;
   *              the first element is at index 1
   * @param count the number of successive SQL array elements to retrieve
   * @return a <code>ResultSet</code> object containing up to
   * <code>count</code> consecutive elements of the SQL array
   * designated by this <code>Array</code> object, starting at
   * index <code>index</code>.
   * @exception SQLException if an error occurs while attempting to
   * access the array
   * @since 1.2
   * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
   *      2.0 API</a>
   */
    public ResultSet getResultSet(long index, int count) throws SQLException {
       return null;
    }


  /**
   * Returns a result set holding the elements of the subarray that
   * starts at index <code>index</code> and contains up to
   * <code>count</code> successive elements.
   * This method uses 
   * the specified <code>map</code> for type map customizations
   * unless the base type of the array does not match a user-defined 
   * type in <code>map</code>, in which case it 
   * uses the standard mapping. This version of the method
   * <code>getResultSet</code> uses either the given type map or the standard mapping;
   * it never uses the type map associated with the connection.
   * <P>
   * The result set has one row for each element of the SQL array
   * designated by this object, with the first row containing the
   * element at index <code>index</code>.  The result set has   
   * up to <code>count</code> rows in ascending order based on the
   * indices.  Each row has two columns:  The second column stores  
   * the element value; the first column stroes the index into the
   * array for that element.
   * @param index the array index of the first element to retrieve;
   *              the first element is at index 1
   * @param count the number of successive SQL array elements to retrieve
   * @param map the <code>Map</code> object that contains the mapping
   * of SQL type names to classes in the Java(tm) programming language
   * @return a <code>ResultSet</code> object containing up to               
   * <code>count</code> consecutive elements of the SQL array
   * designated by this <code>Array</code> object, starting at
   * index <code>index</code>.
   * @exception SQLException if an error occurs while attempting to
   * access the array
   * @since 1.2
   * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
   *      2.0 API</a>
   *
   */
    public ResultSet getResultSet (long index, int count, java.util.Map map)
       throws SQLException {
       return null;
    }


}


