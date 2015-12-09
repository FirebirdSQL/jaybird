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
package org.firebirdsql.gds.impl;

import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.ng.FbExceptionBuilder;

import java.sql.SQLException;

/**
 * Container for attachment information (ie server, port and filename/alias).
 */
public class DbAttachInfo {

    private String server = "localhost";
	private int port = 3050;
	private String fileName;

	public DbAttachInfo(String connectInfo) throws SQLException {
		if (connectInfo == null) {
			throw new FbExceptionBuilder()
                    .nonTransientConnectionException(JaybirdErrorCodes.jb_invalidConnectionString)
                    .messageParameter("(null)")
                    .messageParameter("Connection string is missing")
                    .toFlatSQLException();
		}

		// allows standard syntax //host:port/....
		// and old fb syntax host/port:....
		connectInfo = connectInfo.trim();
		char hostSepChar;
		char portSepChar;
		if (connectInfo.startsWith("//")) {
			connectInfo = connectInfo.substring(2);
			hostSepChar = '/';
			portSepChar = ':';
		} else {
			hostSepChar = ':';
			portSepChar = '/';
		}

		int sep = connectInfo.indexOf(hostSepChar);
		if (sep == 0 || sep == connectInfo.length() - 1) {
            throw new FbExceptionBuilder()
                    .nonTransientConnectionException(JaybirdErrorCodes.jb_invalidConnectionString)
                    .messageParameter(connectInfo)
                    .messageParameter("Host separator: '" + hostSepChar + "' at beginning or end")
                    .toFlatSQLException();
		} else if (sep > 0) {
			server = connectInfo.substring(0, sep);
			fileName = connectInfo.substring(sep + 1);
			int portSep = server.indexOf(portSepChar);
			if (portSep == 0 || portSep == server.length() - 1) {
                throw new FbExceptionBuilder()
                        .nonTransientConnectionException(JaybirdErrorCodes.jb_invalidConnectionString)
                        .messageParameter(connectInfo)
                        .messageParameter("Port separator: '" + portSepChar + "' at beginning or end of: " + server)
                        .toFlatSQLException();
			} else if (portSep > 0) {
				String portString = server.substring(portSep + 1);
				try {
					port = Integer.parseInt(portString);
				} catch (NumberFormatException e) {
                    throw new FbExceptionBuilder()
                            .nonTransientConnectionException(JaybirdErrorCodes.jb_invalidConnectionString)
                            .messageParameter(connectInfo)
                            .messageParameter("Bad port: '" + portString + "' is not a number")
                            .cause(e)
                            .toFlatSQLException();
				}
				server = server.substring(0, portSep);
			}
		} else if (sep == -1) {
			fileName = connectInfo;
		}
	}

	public DbAttachInfo(String server, Integer port, String fileName)
			throws GDSException {
		if (fileName == null || fileName.equals("")) {
			throw new GDSException("null or empty filename in DbAttachInfo");
		}
		if (server != null) {
			this.server = server;
		}
		if (port != null) {
			this.port = port;
		}
		this.fileName = fileName;
	}

	public String getServer() {
		return server;
	}

	public int getPort() {
		return port;
	}

	public String getFileName() {
		return fileName;
	}
}