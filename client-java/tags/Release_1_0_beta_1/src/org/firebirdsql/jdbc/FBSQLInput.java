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
import java.sql.SQLInput;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Ref;
import java.sql.SQLException;
import java.sql.Struct;
import java.sql.SQLData;
import java.net.URL;
import java.net.MalformedURLException;

/**
 *
 *   @see <related>
 *   @author David Jencks (davidjencks@earthlink.net)
 *   @version $ $
 */


/**
 * An input stream that contains a stream of values representing an
 * instance of an SQL structured or distinct type.
 * This interface, used only for custom mapping, is used by the driver
 * behind the scenes, and a programmer never directly invokes
 * <code>SQLInput</code> methods. The <code>readXXX</code> methods
 * provide a way to read the values in an <code>SQLInput</code> object.
 * The method <code>wasNull</code> is used to determine whether the
 * the last value read was SQL <code>NULL</code>.
 * <P>When the method <code>getObject</code> is called with an
 * object of a class implementing the interface <code>SQLData</code>,
 * the JDBC driver calls the method <code>SQLData.getSQLType</code>
 * to determine the SQL type of the user-defined type (UDT)
 * being custom mapped. The driver
 * creates an instance of <code>SQLInput</code>, populating it with the
 * attributes of the UDT.  The driver then passes the input
 * stream to the method <code>SQLData.readSQL</code>, which in turn
 * calls the <code>SQLInput.readXXX</code> methods
 * in its implementation for reading the
 * attributes from the input stream.
 * @since 1.2
 * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
 *      2.0 API</a>
 */

public class FBSQLInput implements SQLInput {


  //================================================================
  // Methods for reading attributes from the stream of SQL data.
  // These methods correspond to the column-accessor methods of
  // java.sql.ResultSet.
  //================================================================

  /**
   * Reads the next attribute in the stream as a <code>String</code>
   * in the Java programming language.
   *
   * @return the attribute; if the value is SQL <code>NULL</code>, returns <code>null</code>
   * @exception SQLException if a database access error occurs
   */
    public String readString() throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * Reads the next attribute in the stream as a <code>boolean</code>
   * in the Java programming language.
   *
   * @return the attribute; if the value is SQL <code>NULL</code>, returns <code>false</code>
   * @exception SQLException if a database access error occurs
   */
    public boolean readBoolean() throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * Reads the next attribute in the stream as a <code>byte</code>
   * in the Java programming language.
   *
   * @return the attribute; if the value is SQL <code>NULL</code>, returns <code>0</code>
   * @exception SQLException if a database access error occurs
   */
    public byte readByte() throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * Reads the next attribute in the stream as a <code>short</code>
   * in the Java programming language.
   *
   * @return the attribute; if the value is SQL <code>NULL</code>, returns <code>0</code>
   * @exception SQLException if a database access error occurs
   */
    public short readShort() throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * Reads the next attribute in the stream as an <code>int</code>
   * in the Java programming language.
   *
   * @return the attribute; if the value is SQL <code>NULL</code>, returns <code>0</code>
   * @exception SQLException if a database access error occurs
   */
    public int readInt() throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * Reads the next attribute in the stream as a <code>long</code>
   * in the Java programming language.
   *
   * @return the attribute; if the value is SQL <code>NULL</code>, returns <code>0</code>
   * @exception SQLException if a database access error occurs
   */
    public long readLong() throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * Reads the next attribute in the stream as a <code>float</code>
   * in the Java programming language.
   *
   * @return the attribute; if the value is SQL <code>NULL</code>, returns <code>0</code>
   * @exception SQLException if a database access error occurs
   */
    public float readFloat() throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * Reads the next attribute in the stream as a <code>double</code>
   * in the Java programming language.
   *
   * @return the attribute; if the value is SQL <code>NULL</code>, returns <code>0</code>
   * @exception SQLException if a database access error occurs
   */
    public double readDouble() throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * Reads the next attribute in the stream as a <code>java.math.BigDecimal</code>
   * object in the Java programming language.
   *
   * @return the attribute; if the value is SQL <code>NULL</code>, returns <code>null</code>
   * @exception SQLException if a database access error occurs
   */
    public java.math.BigDecimal readBigDecimal() throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * Reads the next attribute in the stream as an array of bytes
   * in the Java programming language.
   *
   * @return the attribute; if the value is SQL <code>NULL</code>, returns <code>null</code>
   * @exception SQLException if a database access error occurs
   */
    public byte[] readBytes() throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * Reads the next attribute in the stream as a <code>java.sql.Date</code> object.
   *
   * @return the attribute; if the value is SQL <code>NULL</code>, returns <code>null</code>
   * @exception SQLException if a database access error occurs
   */
    public java.sql.Date readDate() throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * Reads the next attribute in the stream as a <code>java.sql.Time</code> object.
   *
   * @return the attribute; if the value is SQL <code>NULL</code>, returns <code>null</code>
   * @exception SQLException if a database access error occurs
   */
    public java.sql.Time readTime() throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * Reads the next attribute in the stream as a <code>java.sql.Timestamp</code> object.
   *
   * @return the attribute; if the value is SQL <code>NULL</code>, returns <code>null</code>
   * @exception SQLException if a database access error occurs
   */
    public java.sql.Timestamp readTimestamp() throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * Returns the next attribute in the stream as a stream of Unicode characters.
   *
   * @return the attribute; if the value is SQL <code>NULL</code>, returns <code>null</code>
   * @exception SQLException if a database access error occurs
   */
    public java.io.Reader readCharacterStream() throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * Returns the next attribute in the stream as a stream of ASCII characters.
   *
   * @return the attribute; if the value is SQL <code>NULL</code>, returns <code>null</code>
   * @exception SQLException if a database access error occurs
   */
    public java.io.InputStream readAsciiStream() throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * Returns the next attribute in the stream as a stream of uninterpreted
   * bytes.
   *
   * @return the attribute; if the value is SQL <code>NULL</code>, returns <code>null</code>
   * @exception SQLException if a database access error occurs
   */
    public java.io.InputStream readBinaryStream() throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


  //================================================================
  // Methods for reading items of SQL user-defined types from the stream.
  //================================================================

  /**
   * Returns the datum at the head of the stream as an
   * <code>Object</code> in the Java programming language.  The
   * actual type of the object returned is determined by the default type
   * mapping, and any customizations present in this stream's type map.
   *
   * <P>A type map is registered with the stream by the JDBC driver before the
   * stream is passed to the application.
   *
   * <P>When the datum at the head of the stream is an SQL <code>NULL</code>,
   * the method returns <code>null</code>.  If the datum is an SQL structured or distinct
   * type, it determines the SQL type of the datum at the head of the stream.
   * If the stream's type map has an entry for that SQL type, the driver
   * constructs an object of the appropriate class and calls the method
   * <code>SQLData.readSQL</code> on that object, which reads additional data from the
   * stream, using the protocol described for that method.
   *
   * @return the datum at the head of the stream as an <code>Object</code> in the
   * Java programming language;<code>null</code> if the datum is SQL <code>NULL</code>
   * @exception SQLException if a database access error occurs
   */
    public Object readObject() throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * Reads an SQL <code>REF</code> value from the stream and returns it as a
   * <code>Ref</code> object in the Java programming language.
   *
   * @return a <code>Ref</code> object representing the SQL <code>REF</code> value
   * at the head of the stream; <code>null</code> if the value read is
   * SQL <code>NULL</code>
   * @exception SQLException if a database access error occurs
   */
    public Ref readRef() throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * Reads an SQL <code>BLOB</code> value from the stream and returns it as a
   * <code>Blob</code> object in the Java programming language.
   *
   * @return a <code>Blob</code> object representing data of the SQL <code>BLOB</code> value
   * at the head of the stream; <code>null</code> if the value read is
   * SQL <code>NULL</code>
   * @exception SQLException if a database access error occurs
   */
    public Blob readBlob() throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * Reads an SQL <code>CLOB</code> value from the stream and returns it as a
   * <code>Clob</code> object in the Java programming language.
   *
   * @return a <code>Clob</code> object representing data of the SQL <code>CLOB</code> value
   * at the head of the stream; <code>null</code> if the value read is
   * SQL <code>NULL</code>
   * @exception SQLException if a database access error occurs
   */
    public Clob readClob() throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * Reads an SQL <code>ARRAY</code> value from the stream and returns it as an
   * <code>Array</code> object in the Java programming language.
   *
   * @return an <code>Array</code> object representing data of the SQL
   * <code>ARRAY</code> value at the head of the stream; <code>null</code>
   * if the value read is SQL <code>NULL</code>
   * @exception SQLException if a database access error occurs
   */
    public Array readArray() throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * Determines whether the last value read was SQL <code>NULL</code>.
   *
   * @return <code>true</code> if the most recently read SQL value was SQL
   * <code>NULL</code>; otherwise, <code>false</code>
   * @exception SQLException if a database access error occurs
   *
   */
    public boolean wasNull() throws  SQLException {
        throw new SQLException("Not yet implemented");
    }

    /**
     *
     * @return <description>
     * @exception java.sql.SQLException <description>
     * @exception java.net.MalformedURLException <description>
     */
    public URL readURL() throws SQLException {
        // TODO: implement this java.sql.SQLInput method
        return null;
    }


}
