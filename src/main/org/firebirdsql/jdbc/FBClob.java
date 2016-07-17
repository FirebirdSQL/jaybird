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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.SQLException;

/**
 * The mapping in the JavaTM programming language for the SQL CLOB type. An SQL
 * CLOB is a built-in type that stores a Character Large Object as a column
 * value in a row of a database table. <b>CLOBS are not currently supported by
 * the Jaybird driver</b>.
 * <p>
 * The Clob interface provides methods for getting the length of an SQL CLOB
 * (Character Large Object) value, for materializing a CLOB value on the client,
 * and for searching for a substring or CLOB object within a CLOB value. Methods
 * in the interfaces ResultSet, CallableStatement, and PreparedStatement, such
 * as getClob and setClob allow a programmer to access an SQL CLOB value. In
 * addition, this interface has methods for updating a CLOB value.
 * </p>
 * <p>
 * This class also implements {@link NClob} so it can be used with the {@code set/get/updateNClob} methods
 * transparently. It technically does not conform to the JDBC requirements for {@code NClob}.
 * </p>
 * 
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public class FBClob implements Clob, NClob {

	private final FBBlob wrappedBlob;

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
		throw new FBDriverNotCapableException("Cannot determine length for CLOB");
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
		try (Reader reader = getCharacterStream()) {
			long toSkip = pos - 1; // 1-based index
			while (toSkip > 0) {
				toSkip -= reader.skip(toSkip);
			}
			int n;
			char[] buffer = new char[Math.min(length, 1024)];
			StringBuilder sb = new StringBuilder(length);
			while (length > 0 && (n = reader.read(buffer, 0, Math.min(length, buffer.length))) != -1) {
				sb.append(buffer, 0, n);
				length -= n;
			}
			return sb.toString();
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
		String encoding = getWrappedBlob().getGdsHelper().getJavaEncoding();
		InputStream inputStream = wrappedBlob.getBinaryStream();
		if (encoding == null) {
			return new InputStreamReader(inputStream);
		} else {
			try {
				return new InputStreamReader(wrappedBlob.getBinaryStream(), encoding);
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
     * {@inheritDoc}
     * <p>
     * Jaybird currently does not support this method.
     * </p>
     */
	public int setString(long pos, String str) throws SQLException {
        return setString(1, str, 0, str.length());
	}

    /**
     * {@inheritDoc}
     * <p>
     * Jaybird currently does not support this method.
     * </p>
     */
	public int setString(long pos, String str, int offset, int len) throws SQLException {
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
		String encoding = wrappedBlob.getGdsHelper().getJavaEncoding();
		// FIXME: This is wrong for multibyte charactersets; doesn't matter right now as setBinaryStream isn't implemented for position > 1
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
		wrappedBlob.free();
	}

	public Reader getCharacterStream(long pos, long length) throws SQLException {
	    // FIXME: This is wrong for multibyte charactersets; doesn't matter right now as getBinaryStream isn't implemented
		InputStream inputStream = wrappedBlob.getBinaryStream(pos, length);
		String encoding = getWrappedBlob().getGdsHelper().getJavaEncoding();
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

	/**
	 * Copy data from a character stream into this Blob.
	 * <p>
	 * Calling with length {@code -1} is equivalent to calling {@link #copyCharacterStream(Reader)}.
	 * </p>
	 *
	 * @param characterStream the source of data to copy
	 * @param length The maximum number of bytes to copy, or {@code -1} to read the whole stream
	 */
	public void copyCharacterStream(Reader characterStream, long length) throws SQLException {
		if (length == -1L) {
			copyCharacterStream(characterStream);
			return;
		}
        try (Writer writer = setCharacterStream(1)) {
            int chunk;
            final char[] buffer = new char[1024];
            while (length > 0 && (chunk = characterStream.read(buffer)) != -1) {
                writer.write(buffer, 0, chunk);
                length -= chunk;
            }
        } catch (IOException ioe) {
            throw new SQLException(ioe);
        }
	}

	public void copyCharacterStream(Reader characterStream) throws SQLException {
		try (Writer writer = setCharacterStream(1)) {
			int chunk;
			final char[] buffer = new char[1024];
			while ((chunk = characterStream.read(buffer)) != -1) {
                writer.write(buffer, 0, chunk);
            }
		} catch (IOException ioe) {
			throw new SQLException(ioe);
		}
	}

	/**
	 * Retrieves the FBBlob wrapped by this FBClob.
	 * 
	 * @return FBBlob instance
	 * @throws SQLException
	 */
	public FBBlob getWrappedBlob() throws SQLException {
		return wrappedBlob;
	}
}
