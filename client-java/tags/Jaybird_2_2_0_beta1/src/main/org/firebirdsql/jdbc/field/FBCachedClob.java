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
package org.firebirdsql.jdbc.field;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.sql.Clob;
import java.sql.SQLException;

import org.firebirdsql.jdbc.FBCachedBlob;
import org.firebirdsql.jdbc.FBSQLException;

public class FBCachedClob implements Clob {
	
	private FBCachedBlob wrappedBlob;
	private String javaEncoding;

	public FBCachedClob(FBCachedBlob blob, String javaEncoding) {
		this.wrappedBlob = blob;
		this.javaEncoding = javaEncoding;
	}

	public void free() throws SQLException {
		wrappedBlob.free();
	}

	public InputStream getAsciiStream() throws SQLException {
		return wrappedBlob.getBinaryStream();
	}

	public Reader getCharacterStream() throws SQLException {
		InputStream inputStream = wrappedBlob.getBinaryStream();
		if (javaEncoding == null) {
			return new InputStreamReader(inputStream);
		} else {
			try {
				return new InputStreamReader(wrappedBlob.getBinaryStream(),
						javaEncoding);
			} catch (IOException ioe) {
				throw new FBSQLException(ioe);
			}
		}
	}

	public Reader getCharacterStream(long pos, long length) throws SQLException {
		InputStream inputStream = wrappedBlob.getBinaryStream(pos, length);
		if (javaEncoding == null) {
			return new InputStreamReader(inputStream);
		} else {
			try {
				return new InputStreamReader(inputStream, javaEncoding);
			} catch (IOException ioe) {
				throw new FBSQLException(ioe);
			}
		}
	}

	public String getSubString(long pos, int length) throws SQLException {
		throw new FBSQLException("Not implemented");
	}

	public long length() throws SQLException {
		throw new FBSQLException("Not implemented");
	}

	public long position(String searchstr, long start) throws SQLException {
		throw new FBSQLException("Not implemented");
	}

	public long position(Clob searchstr, long start) throws SQLException {
		throw new FBSQLException("Not implemented");
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
