/*
 * Firebird Open Source J2ee connector - jdbc driver
 * 
 * Distributable under LGPL license. You may obtain a copy of the License at
 * http://www.gnu.org/copyleft/lgpl.html
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the LGPL License for more details.
 * 
 * This file was created by members of the firebird development team. All
 * individual contributions remain the Copyright (C) of those individuals.
 * Contributors to this file are either listed here or can be obtained from a
 * CVS history command.
 * 
 * All rights reserved.
 */
package org.firebirdsql.jdbc;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.sql.Date;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Map;

/**
 * JDBC 3.0 compliant implementation of {@link CallableStatement}.
 */
public class FBCallableStatement extends AbstractCallableStatement {

	/**
	 * Create instance of this class.
	 * 
	 * @param c
	 *            instance of {@link AbstractConnection}
	 * @param sql
	 *            SQL statement containing procedure call.
	 * 
	 * @throws SQLException
	 *             if SQL error occured.
	 */
	public FBCallableStatement(AbstractConnection c, String sql, 
                               int rsType, int rsConcurrency)
		throws SQLException {
		super(c, sql, rsType, rsConcurrency);
	}

	public void registerOutParameter(String param1, int param2)
		throws SQLException {
		throw new SQLException("not yet implemented");
	}

	public void registerOutParameter(String param1, int param2, int param3)
		throws SQLException {
		throw new SQLException("not yet implemented");
	}

	public void registerOutParameter(String param1, int param2, String param3)
		throws SQLException {
		throw new SQLException("not yet implemented");
	}

	public URL getURL(int colIndex) throws SQLException {
		assertHasData(getCurrentResultSet());
		//cast apparently to allow use of jdbc 2 interfaces with jdbc 3
		// methods.
		return ((FBResultSet) getCurrentResultSet()).getURL(colIndex);
	}

	public void setURL(String param1, URL param2) throws SQLException {
		throw new SQLException("not yet implemented");
	}

	public void setNull(String param1, int param2) throws SQLException {
		throw new SQLException("not yet implemented");
	}

	public void setBoolean(String param1, boolean param2) throws SQLException {
		throw new SQLException("not yet implemented");
	}

	public void setByte(String param1, byte param2) throws SQLException {
		throw new SQLException("not yet implemented");
	}

	public void setShort(String param1, short param2) throws SQLException {
		throw new SQLException("not yet implemented");
	}

	public void setInt(String param1, int param2) throws SQLException {
		throw new SQLException("not yet implemented");
	}

	public void setLong(String param1, long param2) throws SQLException {
		throw new SQLException("not yet implemented");
	}

	public void setFloat(String param1, float param2) throws SQLException {
		throw new SQLException("not yet implemented");

	}

	public void setDouble(String param1, double param2) throws SQLException {
		throw new SQLException("not yet implemented");
	}

	public void setBigDecimal(String param1, BigDecimal param2)
		throws SQLException {
		throw new SQLException("not yet implemented");
	}

	public void setString(String param1, String param2) throws SQLException {
		throw new SQLException("not yet implemented");
	}

	public void setBytes(String param1, byte[] param2) throws SQLException {
		throw new SQLException("not yet implemented");
	}

	public void setDate(String param1, Date param2) throws SQLException {
		throw new SQLException("not yet implemented");
	}

	public void setTime(String param1, Time param2) throws SQLException {
		throw new SQLException("not yet implemented");
	}

	public void setTimestamp(String param1, Timestamp param2)
		throws SQLException {
		throw new SQLException("not yet implemented");
	}

	public void setAsciiStream(String param1, InputStream param2, int param3)
		throws SQLException {
		throw new SQLException("not yet implemented");
	}

	public void setBinaryStream(String param1, InputStream param2, int param3)
		throws SQLException {
		throw new SQLException("not yet implemented");
	}

	public void setObject(String param1, Object param2, int param3, int param4)
		throws SQLException {
		throw new SQLException("not yet implemented");
	}

	public void setObject(String param1, Object param2, int param3)
		throws SQLException {
		throw new SQLException("not yet implemented");
	}

	public void setObject(String param1, Object param2) throws SQLException {
		throw new SQLException("not yet implemented");
	}

	public void setCharacterStream(String param1, Reader param2, int param3)
		throws SQLException {
		throw new SQLException("not yet implemented");
	}

	public void setDate(String param1, Date param2, Calendar param3)
		throws SQLException {
		throw new SQLException("not yet implemented");
	}

	public void setTime(String param1, Time param2, Calendar param3)
		throws SQLException {
		throw new SQLException("not yet implemented");
	}

	public void setTimestamp(String param1, Timestamp param2, Calendar param3)
		throws SQLException {
		throw new SQLException("not yet implemented");
	}

	public void setNull(String param1, int param2, String param3)
		throws SQLException {
		throw new SQLException("not yet implemented");
	}

	public String getString(String colName) throws SQLException {
		return getString(getCurrentResultSet().findColumn(colName));
	}

	public boolean getBoolean(String colName) throws SQLException {
		return getBoolean(getCurrentResultSet().findColumn(colName));
	}

	public byte getByte(String colName) throws SQLException {
		return getByte(getCurrentResultSet().findColumn(colName));
	}

	public short getShort(String colName) throws SQLException {
		return getShort(getCurrentResultSet().findColumn(colName));
	}

	public int getInt(String colName) throws SQLException {
		return getInt(getCurrentResultSet().findColumn(colName));
	}

	public long getLong(String colName) throws SQLException {
		return getLong(getCurrentResultSet().findColumn(colName));
	}

	public float getFloat(String colName) throws SQLException {
		return getFloat(getCurrentResultSet().findColumn(colName));
	}

	public double getDouble(String colName) throws SQLException {
		return getDouble(getCurrentResultSet().findColumn(colName));
	}

	public byte[] getBytes(String colName) throws SQLException {
		return getBytes(getCurrentResultSet().findColumn(colName));
	}

	public Date getDate(String colName) throws SQLException {
		return getDate(getCurrentResultSet().findColumn(colName));
	}

	public Time getTime(String colName) throws SQLException {
		return getTime(getCurrentResultSet().findColumn(colName));
	}

	public Timestamp getTimestamp(String colName) throws SQLException {
		return getTimestamp(getCurrentResultSet().findColumn(colName));
	}

	public Object getObject(String colName) throws SQLException {
		return getObject(getCurrentResultSet().findColumn(colName));
	}

	public BigDecimal getBigDecimal(String colName) throws SQLException {
		return getBigDecimal(getCurrentResultSet().findColumn(colName));
	}

	public Object getObject(String colName, Map map) throws SQLException {
		return getObject(getCurrentResultSet().findColumn(colName), map);
	}

	public Ref getRef(String colName) throws SQLException {
		return getRef(getCurrentResultSet().findColumn(colName));
	}

	public Blob getBlob(String colName) throws SQLException {
		return getBlob(getCurrentResultSet().findColumn(colName));
	}

	public Clob getClob(String colName) throws SQLException {
		return getClob(getCurrentResultSet().findColumn(colName));
	}

	public Array getArray(String colName) throws SQLException {
		return getArray(getCurrentResultSet().findColumn(colName));
	}

	public Date getDate(String colName, Calendar cal) throws SQLException {
		return getDate(getCurrentResultSet().findColumn(colName), cal);
	}

	public Time getTime(String colName, Calendar cal) throws SQLException {
		return getTime(getCurrentResultSet().findColumn(colName), cal);
	}

	public Timestamp getTimestamp(String colName, Calendar cal)
		throws SQLException {
		return getTimestamp(getCurrentResultSet().findColumn(colName), cal);
	}

	public URL getURL(String colName) throws SQLException {
		return getURL(getCurrentResultSet().findColumn(colName));
	}

}
