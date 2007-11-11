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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.sql.Clob;
import java.sql.SQLException;

/**
 * The mapping in the JavaTM programming language for the SQL CLOB type. An SQL
 * CLOB is a built-in type that stores a Character Large Object as a column
 * value in a row of a database table. <b>CLOBS are not currently supported by
 * the Jaybird driver</b>.
 * 
 * The Clob interface provides methods for getting the length of an SQL CLOB
 * (Character Large Object) value, for materializing a CLOB value on the client,
 * and for searching for a substring or CLOB object within a CLOB value. Methods
 * in the interfaces ResultSet, CallableStatement, and PreparedStatement, such
 * as getClob and setClob allow a programmer to access an SQL CLOB value. In
 * addition, this interface has methods for updating a CLOB value.
 * 
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public class FBClob implements Clob {

	private FBBlob wrappedBlob;

	public FBClob(FBBlob blob) {
		this.wrappedBlob = blob;
	}

	/**
	 * Returns the number of characters in the <code>CLOB</code> value
	 * designated by this <code>Clob</code> object.
	 * 
	 * @return length of the <code>CLOB</code> in characters
	 * @exception SQLException
	 *                if there is an error accessing the length of the
	 *                <code>CLOB</code>
	 * @since 1.2
	 * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
	 */
	public long length() throws SQLException {
		throw new FBDriverNotCapableException();
	}

	/**
	 * <b>This operation is not currently supported</b> Truncate this
	 * <code>Clob</code> to a given length.
	 * 
	 * @param param1
	 *            The length to truncate this Clob to
	 * @exception java.sql.SQLException
	 *                this operation is not supported
	 */
	public void truncate(long param1) throws SQLException {
		throw new FBDriverNotCapableException();
	}

	/**
	 * Returns a copy of the specified substring in the <code>CLOB</code>
	 * value designated by this <code>Clob</code> object. The substring begins
	 * at position <code>pos</code> and has up to <code>length</code>
	 * consecutive characters.
	 * 
	 * @param pos
	 *            the first character of the substring to be extracted. The
	 *            first character is at position 1.
	 * @param length
	 *            the number of consecutive characters to be copied
	 * @return a <code>String</code> that is the specified substring in the
	 *         <code>CLOB</code> value designated by this <code>Clob</code>
	 *         object
	 * @exception SQLException
	 *                if there is an error accessing the <code>CLOB</code>
	 * @since 1.2
	 * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
	 */
	public String getSubString(long pos, int length) throws SQLException {
		StringBuffer stringBuffer = new StringBuffer();
		Reader reader = getCharacterStream();
		char[] buffer = new char[1024];
		int n = 0;
		try {
			reader.skip(pos - 1); // 1-based index
			while ((n = reader.read(buffer)) != -1) {
				stringBuffer.append(String.valueOf(buffer, 0, Math.min(n, length)));
				length -= n;
			}
			reader.close();
			return stringBuffer.toString();
		} catch (IOException e) {
			throw new FBSQLException(e);
		}
	}

	/**
	 * Gets the <code>CLOB</code> value designated by this <code>Clob</code>
	 * object as a Unicode stream.
	 * 
	 * @return a Unicode stream containing the <code>CLOB</code> data
	 * @exception SQLException
	 *                if there is an error accessing the <code>CLOB</code>
	 *                value
	 * @since 1.2
	 * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
	 */
	public Reader getCharacterStream() throws SQLException {
		String encoding = getWrappedBlob().gdsHelper.getJavaEncoding();
		InputStream inputStream = wrappedBlob.getBinaryStream();
		if (encoding == null) {
			return new InputStreamReader(inputStream);
		} else {
			try {
				return new InputStreamReader(wrappedBlob.getBinaryStream(),
						encoding);
			} catch (IOException ioe) {
				throw new FBSQLException(ioe);
			}
		}
	}

	/**
	 * Gets the <code>CLOB</code> value designated by this <code>Clob</code>
	 * object as a stream of Ascii bytes.
	 * 
	 * @return an ascii stream containing the <code>CLOB</code> data
	 * @exception SQLException
	 *                if there is an error accessing the <code>CLOB</code>
	 *                value
	 * @since 1.2
	 * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
	 */
	public InputStream getAsciiStream() throws SQLException {
		InputStream inputStream = null;
		if (wrappedBlob != null) {
			inputStream = wrappedBlob.getBinaryStream();
		}
		return inputStream;
	}

	/**
	 * Determines the character position at which the specified substring
	 * <code>searchstr</code> appears in the SQL <code>CLOB</code> value
	 * represented by this <code>Clob</code> object. The search begins at
	 * position <code>start</code>.
	 * 
	 * @param searchstr
	 *            the substring for which to search
	 * @param start
	 *            the position at which to begin searching; the first position
	 *            is 1
	 * @return the position at which the substring appears, else -1; the first
	 *         position is 1
	 * @exception SQLException
	 *                if there is an error accessing the <code>CLOB</code>
	 *                value
	 * @since 1.2
	 * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
	 */
	public long position(String searchstr, long start) throws SQLException {
		throw new FBDriverNotCapableException();
	}

	/**
	 * Determines the character position at which the specified
	 * <code>Clob</code> object <code>searchstr</code> appears in this
	 * <code>Clob</code> object. The search begins at position
	 * <code>start</code>.
	 * 
	 * @param searchstr
	 *            the <code>Clob</code> object for which to search
	 * @param start
	 *            the position at which to begin searching; the first position
	 *            is 1
	 * @return the position at which the <code>Clob</code> object appears,
	 *         else -1; the first position is 1
	 * @exception SQLException
	 *                if there is an error accessing the <code>CLOB</code>
	 *                value
	 * @since 1.2
	 * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
	 */
	public long position(Clob searchstr, long start) throws SQLException {
		throw new FBDriverNotCapableException();
	}

	/**
	 * <b>This operation is not supported</b> Writes the given Java String to
	 * the CLOB value that this <code>Clob</code> object designates at the
	 * position <code>pos</code>.
	 * 
	 * @param start
	 *            position at which to start writing
	 * @param searchString
	 *            The <code>String</code> value to write
	 * @return The number of characters written
	 * @exception java.sql.SQLException
	 *                because this operation is not supported
	 */
	public int setString(long start, String searchString) throws SQLException {
		throw new FBDriverNotCapableException();

	}

	/**
	 * <b>This operation is not supported</b>
	 * 
	 * @param param1
	 *            <description>
	 * @param param2
	 *            <description>
	 * @param param3
	 *            <description>
	 * @param param4
	 *            <description>
	 * @return <description>
	 * @exception java.sql.SQLException
	 *                <description>
	 */
	public int setString(long param1, String param2, int param3, int param4)
			throws SQLException {
		throw new FBDriverNotCapableException();

	}

	/**
	 * Set a byte stream to write the contents of this Clob.
	 * 
	 * @param pos
	 *            The position at which writing is to start.
	 * @return <description>
	 * @exception java.sql.SQLException
	 *                <description>
	 */
	public OutputStream setAsciiStream(long pos) throws SQLException {
		return wrappedBlob.setBinaryStream(pos);
	}

	/**
	 * Create a writer to add character data to this Clob.
	 * 
	 * @param position
	 *            The position at which the Writer should start writing
	 * @return <description>
	 * @exception java.sql.SQLException
	 *                <description>
	 */
	public Writer setCharacterStream(long position) throws SQLException {

		String encoding = wrappedBlob.gdsHelper.getJavaEncoding();
		OutputStream outputStream = wrappedBlob.setBinaryStream(position);
		if (encoding == null) {
			return new OutputStreamWriter(outputStream);
		} else {
			try {
				return new OutputStreamWriter(outputStream, encoding);
			} catch (UnsupportedEncodingException ioe) {
				throw new FBSQLException(ioe);
			}
		}
	}

	public void free() throws SQLException {
		this.wrappedBlob.free();
	}

	public Reader getCharacterStream(long pos, long length) throws SQLException {
		InputStream inputStream = wrappedBlob.getBinaryStream(pos, length);
		String encoding = getWrappedBlob().gdsHelper.getJavaEncoding();
		if (encoding == null) {
			return new InputStreamReader(inputStream);
		} else {
			try {
				return new InputStreamReader(inputStream, encoding);
			} catch (IOException ioe) {
				throw new FBSQLException(ioe);
			}
		}
	}

	public void copyCharacterStream(Reader characterStream) throws SQLException {

		Writer writer = setCharacterStream(0);
		try {
			int chunk = 0;
			char[] buffer = new char[1024];

			while ((chunk = characterStream.read(buffer)) != -1)
				writer.write(buffer, 0, chunk);

			writer.flush();
			writer.close();
		} catch (IOException ioe) {
			throw new FBSQLException(ioe);
		}

	}



	public FBBlob getWrappedBlob() throws SQLException {
		return wrappedBlob;
	}

}
