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
 * Implementation of {@link Clob}.
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

	@Override
	public long length() throws SQLException {
		throw new FBDriverNotCapableException("Cannot determine length for CLOB");
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * <b>This operation is not currently supported</b>
	 * </p>
	 */
	@Override
	public void truncate(long param1) throws SQLException {
		throw new FBDriverNotCapableException("Method truncate(long) is not supported");
	}

	@Override
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

	@Override
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

	@Override
	public InputStream getAsciiStream() throws SQLException {
		InputStream inputStream = null;
		if (wrappedBlob != null) {
			inputStream = wrappedBlob.getBinaryStream();
		}
		return inputStream;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Jaybird currently does not support this method.
	 * </p>
	 */
	@Override
	public long position(String searchstr, long start) throws SQLException {
		throw new FBDriverNotCapableException("Method position(String, long) is not supported");
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Jaybird currently does not support this method.
	 * </p>
	 */
	@Override
	public long position(Clob searchstr, long start) throws SQLException {
		throw new FBDriverNotCapableException("Method position(Clob, long) is not supported");
	}

    /**
     * {@inheritDoc}
     * <p>
     * Jaybird currently does not support this method.
     * </p>
     */
	@Override
	public int setString(long pos, String str) throws SQLException {
        return setString(1, str, 0, str.length());
	}

    /**
     * {@inheritDoc}
     * <p>
     * Jaybird currently does not support this method.
     * </p>
     */
	@Override
	public int setString(long pos, String str, int offset, int len) throws SQLException {
        try (Writer charStream = setCharacterStream(pos)) {
        	charStream.write(str, offset, len);
        	return len;
		} catch (IOException e) {
			throw new SQLException("IOException writing string to blob", e);
		}
	}

	@Override
	public OutputStream setAsciiStream(long pos) throws SQLException {
		return wrappedBlob.setBinaryStream(pos);
	}

	@Override
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

	@Override
	public void free() throws SQLException {
		wrappedBlob.free();
	}

	@Override
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
