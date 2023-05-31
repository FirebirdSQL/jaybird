/*
 * Firebird Open Source JDBC Driver
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
package org.firebirdsql.jdbc.field;

import org.firebirdsql.jdbc.FBBlob;
import org.firebirdsql.jdbc.FBCachedBlob;
import org.firebirdsql.jdbc.FBDriverNotCapableException;
import org.firebirdsql.jdbc.FBSQLException;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.SQLException;

/**
 * Clob implementation that is cached client-side.
 * <p>
 * This implementation is used for disconnected result sets (ie scrollable and hold cursors over commit).
 * </p>
 * <p>
 * This class also implements {@link NClob} so it can be used with the {@code set/get/updateNClob} methods
 * transparently. It technically does not conform to the JDBC requirements for {@code NClob}.
 * </p>
 */
public final class FBCachedClob implements Clob, NClob {
	
	private final FBCachedBlob wrappedBlob;
	private final FBBlob.Config blobConfig;

	public FBCachedClob(FBCachedBlob blob, FBBlob.Config blobConfig) {
		this.wrappedBlob = blob;
		this.blobConfig = blobConfig;
	}

	public void free() throws SQLException {
		wrappedBlob.free();
	}

	public InputStream getAsciiStream() throws SQLException {
		return wrappedBlob.getBinaryStream();
	}

	public Reader getCharacterStream() throws SQLException {
		return blobConfig.createReader(
				wrappedBlob.getBinaryStream());
	}

	public Reader getCharacterStream(long pos, long length) throws SQLException {
		return blobConfig.createReader(
				wrappedBlob.getBinaryStream(pos, length));
	}

	public String getSubString(long pos, int length) throws SQLException {
		throw new FBDriverNotCapableException("Method getSubstring(long, int) is not supported");
	}

	public long length() throws SQLException {
		throw new FBDriverNotCapableException("Cannot determine length for CLOB");
	}

	public long position(String searchstr, long start) throws SQLException {
		throw new FBDriverNotCapableException("Method position(String, long) is not supported");
	}

	public long position(Clob searchstr, long start) throws SQLException {
		throw new FBDriverNotCapableException("Method position(Clob, long) is not supported");
	}

	public OutputStream setAsciiStream(long pos) throws SQLException {
		throw new FBSQLException("Clob in auto-commit mode is read-only.");
	}

	public Writer setCharacterStream(long pos) throws SQLException {
		throw new FBSQLException("Clob in auto-commit mode is read-only.");
	}

	public int setString(long pos, String str) throws SQLException {
		throw new FBSQLException("Clob in auto-commit mode is read-only.");
	}

	public int setString(long pos, String str, int offset, int len)
			throws SQLException {
		throw new FBSQLException("Clob in auto-commit mode is read-only.");
	}

	public void truncate(long len) throws SQLException {
		wrappedBlob.truncate(len);
	}

}
