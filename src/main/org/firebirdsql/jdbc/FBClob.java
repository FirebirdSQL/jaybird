/*
 SPDX-FileCopyrightText: Copyright 2001-2002 David Jencks
 SPDX-FileCopyrightText: Copyright 2003-2007 Roman Rokytskyy
 SPDX-FileCopyrightText: Copyright 2007 Gabriel Reid
 SPDX-FileCopyrightText: Copyright 2011-2024 Mark Rotteveel
 SPDX-License-Identifier: LGPL-2.1-or-later
*/
package org.firebirdsql.jdbc;

import org.firebirdsql.util.InternalApi;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.SQLException;
import java.sql.SQLNonTransientException;

/**
 * Implementation of {@link Clob}.
 * <p>
 * This class also implements {@link NClob} so it can be used with the {@code set/get/updateNClob} methods
 * transparently. It technically does not conform to the JDBC requirements for {@code NClob}.
 * </p>
 * <p>
 * This class is internal API of Jaybird. Future versions may radically change, move, or make inaccessible this type.
 * For the public API, refer to the {@link Clob}, {@link NClob}, and {@link FirebirdClob} interfaces.
 * </p>
 * 
 * @author David Jencks
 * @version 1.0
 */
@InternalApi
public final class FBClob implements FirebirdClob, NClob {

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
	public void truncate(long len) throws SQLException {
		throw new FBDriverNotCapableException("Method truncate(long) is not supported");
	}

	@Override
	public String getSubString(long pos, int length) throws SQLException {
		try (Reader reader = getCharacterStream()) {
			long toSkip = pos - 1; // 1-based index
			while (toSkip > 0) {
				long skipped = reader.skip(toSkip);
				if (skipped == 0) {
					throw new EOFException("end of stream was reached at position " + (pos - toSkip));
				}
				toSkip -= skipped;
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
			throw new SQLException(e.toString(), SQLStateConstants.SQL_STATE_GENERAL_ERROR, e);
		}
	}

	@Override
	public Reader getCharacterStream() throws SQLException {
		return wrappedBlob.config().createReader(
				wrappedBlob.getBinaryStream());
	}

	@Override
	public InputStream getAsciiStream() throws SQLException {
		return wrappedBlob.getBinaryStream();
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
     * Jaybird currently only supports this method for {@code position == 1}.
     * </p>
     */
	@Override
	public int setString(long pos, String str) throws SQLException {
        return setString(1, str, 0, str.length());
	}

    /**
     * {@inheritDoc}
     * <p>
     * Jaybird currently only supports this method for {@code position == 1}.
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
	public Writer setCharacterStream(long pos) throws SQLException {
		if (pos < 1) {
			throw new SQLNonTransientException("You can't start before the beginning of the blob",
					SQLStateConstants.SQL_STATE_INVALID_STRING_LENGTH);
		}
		if (pos > 1) {
			throw new FBDriverNotCapableException("Offset start positions are not supported");
		}
		return wrappedBlob.config().createWriter(
				wrappedBlob.setBinaryStream(1));
	}

	@Override
	public void free() throws SQLException {
		wrappedBlob.free();
	}

	@Override
	public Reader getCharacterStream(long pos, long length) throws SQLException {
		throw new FBDriverNotCapableException("Method getCharacterStream(long, long) is not supported");
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
		wrappedBlob.copyCharacterStream(characterStream, length);
	}

	/**
	 * Copy data from a character stream into this Blob.
	 *
	 * @param characterStream the source of data to copy
	 */
	public void copyCharacterStream(Reader characterStream) throws SQLException {
		wrappedBlob.copyCharacterStream(characterStream);
	}

	/**
	 * Retrieves the FBBlob wrapped by this FBClob.
	 * 
	 * @return FBBlob instance
	 */
	@SuppressWarnings({ "RedundantThrows", "java:S1130" })
	public FBBlob getWrappedBlob() throws SQLException {
		return wrappedBlob;
	}
}
