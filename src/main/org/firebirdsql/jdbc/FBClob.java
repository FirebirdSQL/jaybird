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


import java.sql.Clob;
import java.sql.SQLException;
import java.io.Reader;
import java.io.OutputStream;
import java.io.Writer;
import java.io.InputStream;



/**
 * The mapping in the JavaTM programming language for the SQL CLOB type. 
 * An SQL CLOB is a built-in type that stores a Character Large Object as a 
 * column value in a row of a database table. <b>CLOBS are not currently 
 * supported by the Jaybird driver</b>.
 * 
 * The Clob interface provides methods for getting the length of an SQL CLOB 
 * (Character Large Object) value, for materializing a CLOB value on the 
 * client, and for searching for a substring or CLOB object within a CLOB 
 * value. Methods in the interfaces ResultSet, CallableStatement, and 
 * PreparedStatement, such as getClob and setClob allow a programmer to access 
 * an SQL CLOB value. In addition, this interface has methods for updating a 
 * CLOB value. 
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public class FBClob implements Clob {

  /**
   * Returns the number of characters
   * in the <code>CLOB</code> value
   * designated by this <code>Clob</code> object.
   * @return length of the <code>CLOB</code> in characters
   * @exception SQLException if there is an error accessing the
   * length of the <code>CLOB</code>
   * @since 1.2
   * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
   */
    public long length() throws SQLException {
        throw new FBDriverNotCapableException();
    }

    /**
     * <b>This operation is not currently supported</b>
     * Truncate this <code>Clob</code> to a given length. 
     *
     * @param param1 The length to truncate this Clob to 
     * @exception java.sql.SQLException this operation is not supported     
     */
    public void truncate(long param1) throws SQLException {
        throw new FBDriverNotCapableException();
    }



  /**
   * Returns a copy of the specified substring
   * in the <code>CLOB</code> value
   * designated by this <code>Clob</code> object.
   * The substring begins at position
   * <code>pos</code> and has up to <code>length</code> consecutive
   * characters.
   * @param pos the first character of the substring to be extracted.
   *            The first character is at position 1.
   * @param length the number of consecutive characters to be copied
   * @return a <code>String</code> that is the specified substring in
   *         the <code>CLOB</code> value designated by this <code>Clob</code> object
   * @exception SQLException if there is an error accessing the
   * <code>CLOB</code>
   * @since 1.2
   * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
   */
    public String getSubString(long pos, int length) throws SQLException {
        throw new FBDriverNotCapableException();
    }


  /**
   * Gets the <code>CLOB</code> value designated by this <code>Clob</code>
   * object as a Unicode stream.
   * @return a Unicode stream containing the <code>CLOB</code> data
   * @exception SQLException if there is an error accessing the
   * <code>CLOB</code> value
   * @since 1.2
   * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
   */
    public Reader getCharacterStream() throws SQLException {
        throw new FBDriverNotCapableException();
    }


  /**
   * Gets the <code>CLOB</code> value designated by this <code>Clob</code>
   * object as a stream of Ascii bytes.
   * @return an ascii stream containing the <code>CLOB</code> data
   * @exception SQLException if there is an error accessing the
   * <code>CLOB</code> value
   * @since 1.2
   * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
   */
    public InputStream getAsciiStream() throws SQLException {
        throw new FBDriverNotCapableException();
    }


  /**
   * Determines the character position at which the specified substring
   * <code>searchstr</code> appears in the SQL <code>CLOB</code> value
   * represented by this <code>Clob</code> object.  The search
   * begins at position <code>start</code>.
   * @param searchstr the substring for which to search
   * @param start the position at which to begin searching; the first position
   *              is 1
   * @return the position at which the substring appears, else -1; the first
   *         position is 1
   * @exception SQLException if there is an error accessing the
   * <code>CLOB</code> value
   * @since 1.2
   * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
   */
    public long position(String searchstr, long start) throws SQLException {
        throw new FBDriverNotCapableException();
    }


  /**
   * Determines the character position at which the specified
   * <code>Clob</code> object <code>searchstr</code> appears in this
   * <code>Clob</code> object.  The search begins at position
   * <code>start</code>.
   * @param searchstr the <code>Clob</code> object for which to search
   * @param start the position at which to begin searching; the first
   *              position is 1
   * @return the position at which the <code>Clob</code> object appears,
   * else -1; the first position is 1
   * @exception SQLException if there is an error accessing the
   * <code>CLOB</code> value
   * @since 1.2
   * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
   */
    public long position(Clob searchstr, long start) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    /**
     * <b>This operation is not supported</b>
     * Writes the given Java String to the CLOB value that this 
     * <code>Clob</code> object designates at the position <code>pos</code>. 
     *
     * @param param1 position at which to start writing 
     * @param param2 The <code>String</code> value to write 
     * @return The number of characters written
     * @exception java.sql.SQLException because this operation is not supported 
     */
    public int setString(long param1, String param2) throws SQLException {
        throw new FBDriverNotCapableException();

    }

    /**
     * <b>This operation is not supported</b>
     *
     * @param param1 <description>
     * @param param2 <description>
     * @param param3 <description>
     * @param param4 <description>
     * @return <description>
     * @exception java.sql.SQLException <description>
     */
    public int setString(long param1, String param2, int param3, int param4) throws SQLException {
        throw new FBDriverNotCapableException();

    }

    /**
     * <b>This operation is not supported</b>
     *
     * @param param1 <description>
     * @return <description>
     * @exception java.sql.SQLException <description>
     */
    public OutputStream setAsciiStream(long param1) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    /**
     * <b>This operation is not supported</b>
     *
     * @param param1 <description>
     * @return <description>
     * @exception java.sql.SQLException <description>
     */
    public Writer setCharacterStream(long param1) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public void free() throws SQLException {
        // empty
    }

    public Reader getCharacterStream(long pos, long length) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    

}
