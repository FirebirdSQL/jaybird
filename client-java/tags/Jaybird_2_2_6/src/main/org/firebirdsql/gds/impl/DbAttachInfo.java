/*
 * $Id$
 * 
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
package org.firebirdsql.gds.impl;

import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.ISCConstants;

/**
 * Container for attachment information (ie server, port and filename/alias).
 */
public class DbAttachInfo {

    private String server = "localhost";
	private int port = 3050;
	private String fileName;

	public DbAttachInfo(String connectInfo) throws GDSException {

		if (connectInfo == null) {
			throw new GDSException("Connection string missing");
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
			throw new GDSException("Bad connection string: '" + hostSepChar
					+ "' at beginning or end of:" + connectInfo
					+ ISCConstants.isc_bad_db_format);
		} else if (sep > 0) {
			server = connectInfo.substring(0, sep);
			fileName = connectInfo.substring(sep + 1);
			int portSep = server.indexOf(portSepChar);
			if (portSep == 0 || portSep == server.length() - 1) {
				throw new GDSException("Bad server string: '" + portSepChar
						+ "' at beginning or end of: " + server
						+ ISCConstants.isc_bad_db_format);
			} else if (portSep > 0) {
				port = Integer.parseInt(server.substring(portSep + 1));
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
			this.port = port.intValue();
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