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


// imports --------------------------------------
import java.sql.SQLOutput;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Ref;
import java.sql.SQLException;
import java.sql.Struct;
import java.sql.SQLData;
import java.net.URL;

/**
 *
 *   @see <related>
 *   @author David Jencks (davidjencks@earthlink.net)
 *   @version $ $
 */


/**
 * The output stream for writing the attributes of a user-defined
 * type back to the database.  This interface, used
 * only for custom mapping, is used by the driver, and its
 * methods are never directly invoked by a programmer.
 * <p>When an object of a class implementing the interface
 * <code>SQLData</code> is passed as an argument to an SQL statement, the
 * JDBC driver calls the method <code>SQLData.getSQLType</code> to
 * determine the  kind of SQL
 * datum being passed to the database.
 * The driver then creates an instance of <code>SQLOutput</code> and
 * passes it to the method <code>SQLData.writeSQL</code>.
 * The method <code>writeSQL</code> in turn calls the
 * appropriate <code>SQLOutput.writeXXX</code> methods
 * to write data from the <code>SQLData</code> object to
 * the <code>SQLOutput</code> output stream as the
 * representation of an SQL user-defined type.
 * @since 1.2
 * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
 *      2.0 API</a>
 */

 public class FBSQLOutput implements SQLOutput {

  //================================================================
  // Methods for writing attributes to the stream of SQL data.
  // These methods correspond to the column-accessor methods of
  // java.sql.ResultSet.
  //================================================================

  /**
   * Writes the next attribute to the stream as a <code>String</code>
   * in the Java programming language.
   *
   * @param x the value to pass to the database
   * @exception SQLException if a database access error occurs
   */
    public void writeString(String x) throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * Writes the next attribute to the stream as a Java boolean.
   * Writes the next attribute to the stream as a <code>String</code>
   * in the Java programming language.
   *
   * @param x the value to pass to the database
   * @exception SQLException if a database access error occurs
   */
    public void writeBoolean(boolean x) throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * Writes the next attribute to the stream as a Java byte.
   * Writes the next attribute to the stream as a <code>String</code>
   * in the Java programming language.
   *
   * @param x the value to pass to the database
   * @exception SQLException if a database access error occurs
   */
    public void writeByte(byte x) throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * Writes the next attribute to the stream as a Java short.
   * Writes the next attribute to the stream as a <code>String</code>
   * in the Java programming language.
   *
   * @param x the value to pass to the database
   * @exception SQLException if a database access error occurs
   */
    public void writeShort(short x) throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * Writes the next attribute to the stream as a Java int.
   * Writes the next attribute to the stream as a <code>String</code>
   * in the Java programming language.
   *
   * @param x the value to pass to the database
   * @exception SQLException if a database access error occurs
   */
    public void writeInt(int x) throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * Writes the next attribute to the stream as a Java long.
   * Writes the next attribute to the stream as a <code>String</code>
   * in the Java programming language.
   *
   * @param x the value to pass to the database
   * @exception SQLException if a database access error occurs
   */
    public void writeLong(long x) throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * Writes the next attribute to the stream as a Java float.
   * Writes the next attribute to the stream as a <code>String</code>
   * in the Java programming language.
   *
   * @param x the value to pass to the database
   * @exception SQLException if a database access error occurs
   */
    public void writeFloat(float x) throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * Writes the next attribute to the stream as a Java double.
   * Writes the next attribute to the stream as a <code>String</code>
   * in the Java programming language.
   *
   * @param x the value to pass to the database
   * @exception SQLException if a database access error occurs
   */
    public void writeDouble(double x) throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * Writes the next attribute to the stream as a java.math.BigDecimal object.
   * Writes the next attribute to the stream as a <code>String</code>
   * in the Java programming language.
   *
   * @param x the value to pass to the database
   * @exception SQLException if a database access error occurs
   */
    public void writeBigDecimal(java.math.BigDecimal x) throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * Writes the next attribute to the stream as an array of bytes.
   * Writes the next attribute to the stream as a <code>String</code>
   * in the Java programming language.
   *
   * @param x the value to pass to the database
   * @exception SQLException if a database access error occurs
   */
    public void writeBytes(byte[] x) throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * Writes the next attribute to the stream as a java.sql.Date object.
   * Writes the next attribute to the stream as a <code>java.sql.Date</code> object
   * in the Java programming language.
   *
   * @param x the value to pass to the database
   * @exception SQLException if a database access error occurs
   */
    public void writeDate(java.sql.Date x) throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * Writes the next attribute to the stream as a java.sql.Time object.
   * Writes the next attribute to the stream as a <code>java.sql.Date</code> object
   * in the Java programming language.
   *
   * @param x the value to pass to the database
   * @exception SQLException if a database access error occurs
   */
    public void writeTime(java.sql.Time x) throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * Writes the next attribute to the stream as a java.sql.Timestamp object.
   * Writes the next attribute to the stream as a <code>java.sql.Date</code> object
   * in the Java programming language.
   *
   * @param x the value to pass to the database
   * @exception SQLException if a database access error occurs
   */
    public void writeTimestamp(java.sql.Timestamp x) throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * Writes the next attribute to the stream as a stream of Unicode characters.
   *
   * @param x the value to pass to the database
   * @exception SQLException if a database access error occurs
   */
    public void writeCharacterStream(java.io.Reader x) throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * Writes the next attribute to the stream as a stream of ASCII characters.
   *
   * @param x the value to pass to the database
   * @exception SQLException if a database access error occurs
   */
    public void writeAsciiStream(java.io.InputStream x) throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * Writes the next attribute to the stream as a stream of uninterpreted
   * bytes.
   *
   * @param x the value to pass to the database
   * @exception SQLException if a database access error occurs
   */
    public void writeBinaryStream(java.io.InputStream x) throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


  //================================================================
  // Methods for writing items of SQL user-defined types to the stream.
  // These methods pass objects to the database as values of SQL
  // Structured Types, Distinct Types, Constructed Types, and Locator
  // Types.  They decompose the Java object(s) and write leaf data
  // items using the methods above.
  //================================================================

  /**
   * Writes to the stream the data contained in the given
   * <code>SQLData</code> object.
   * When the <code>SQLData</code> object is <code>null</code>, this
   * method writes an SQL <code>NULL</code> to the stream.
   * Otherwise, it calls the <code>SQLData.writeSQL</code>
   * method of the given object, which
   * writes the object's attributes to the stream.
   * The implementation of the method <code>SQLData.writeSQ</code>
   * calls the appropriate <code>SQLOutput.writeXXX</code> method(s)
   * for writing each of the object's attributes in order.
   * The attributes must be read from an <code>SQLInput</code>
   * input stream and written to an <code>SQLOutput</code>
   * output stream in the same order in which they were
   * listed in the SQL definition of the user-defined type.
   *
   * @param x the object representing data of an SQL structured or
   * distinct type
   * @exception SQLException if a database access error occurs
   */
    public void writeObject(SQLData x) throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * Writes an SQL <code>REF</code> value to the stream.
   *
   * @param x a <code>Ref</code> object representing data of an SQL
   * <code>REF</code> value
   * @exception SQLException if a database access error occurs
   */
    public void writeRef(Ref x) throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * Writes an SQL <code>BLOB</code> value to the stream.
   *
   * @param x a <code>Blob</code> object representing data of an SQL
   * <code>BLOB</code> value
   *
   * @exception SQLException if a database access error occurs
   */
    public void writeBlob(Blob x) throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * Writes an SQL <code>CLOB</code> value to the stream.
   *
   * @param x a <code>Clob</code> object representing data of an SQL
   * <code>CLOB</code> value
   *
   * @exception SQLException if a database access error occurs
   */
    public void writeClob(Clob x) throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * Writes an SQL structured type value to the stream.
   *
   * @param x a <code>Struct</code> object representing data of an SQL
   * structured type
   *
   * @exception SQLException if a database access error occurs
   */
    public void writeStruct(Struct x) throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * Writes an SQL <code>ARRAY</code> value to the stream.
   *
   * @param x an <code>Array</code> object representing data of an SQL
   * <code>ARRAY</code> type
   *
   * @exception SQLException if a database access error occurs
   */
    public void writeArray(Array x) throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


    /**
     *
     * @param param1 <description>
     * @exception java.sql.SQLException <description>
     */
    public void writeURL(URL param1) throws SQLException {
        // TODO: implement this java.sql.SQLOutput method
    }

}

