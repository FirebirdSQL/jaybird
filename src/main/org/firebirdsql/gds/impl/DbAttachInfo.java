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

    private DbAttachInfo(String server, Integer port, String fileName, String originalConnectString)
            throws SQLException {
        if (fileName == null || fileName.equals("")) {
            throw new FbExceptionBuilder()
                    .nonTransientConnectionException(JaybirdErrorCodes.jb_invalidConnectionString)
                    .messageParameter(originalConnectString)
                    .messageParameter("null or empty database name in connection string")
                    .toFlatSQLException();
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

    public static DbAttachInfo parseConnectString(String connectString) throws SQLException {
        if (connectString == null) {
            throw new FbExceptionBuilder()
                    .nonTransientConnectionException(JaybirdErrorCodes.jb_invalidConnectionString)
                    .messageParameter("(null)")
                    .messageParameter("Connection string is missing")
                    .toFlatSQLException();
        }

        // allows standard syntax //host:port/....
        // and old fb syntax host/port:....
        connectString = connectString.trim();
        if (connectString.startsWith("//")) {
            return parseUrlConnectString(connectString.substring(2));
        } else {
            return parseLegacyConnectString(connectString);
        }
    }

    private static DbAttachInfo parseUrlConnectString(String connectString) throws SQLException {
        // Expect host/filename, host:port/filename, ipv4/filename, ipv4:port/filename, [ipv6]/filename or [ipv6]:port/filename
        String server = null;
        String fileName = null;
        Integer port = null;
        int sep = connectString.indexOf('/');
        if (sep == 0 || sep == connectString.length() - 1) {
            throw new FbExceptionBuilder()
                    .nonTransientConnectionException(JaybirdErrorCodes.jb_invalidConnectionString)
                    .messageParameter(connectString)
                    .messageParameter("Host separator: '/' at beginning or end")
                    .toFlatSQLException();
        } else if (sep > 0) {
            server = connectString.substring(0, sep);
            fileName = connectString.substring(sep + 1);
            if (server.charAt(0) == '[') {
                //ipv6
                int endIpv6Address = server.indexOf(']');
                if (endIpv6Address == -1) {
                    throw new FbExceptionBuilder()
                            .nonTransientConnectionException(JaybirdErrorCodes.jb_invalidConnectionString)
                            .messageParameter(connectString)
                            .messageParameter("IPv6 address expected, missing closing ']'")
                            .toFlatSQLException();
                }
                if (endIpv6Address != server.length() - 1) {
                    if (server.charAt(endIpv6Address + 1) == ':') {
                        port = parsePortNumber(connectString, server.substring(endIpv6Address + 2));
                    } else {
                        throw new FbExceptionBuilder()
                                .nonTransientConnectionException(JaybirdErrorCodes.jb_invalidConnectionString)
                                .messageParameter(connectString)
                                .messageParameter("Unexpected tokens '" + server.substring(endIpv6Address + 1) + "' after IPv6 address")
                                .toFlatSQLException();
                    }
                }
                server = server.substring(1, endIpv6Address);
            } else {
                // ipv4 or hostname
                int portSep = server.indexOf(':');
                if (portSep == 0 || portSep == server.length() - 1) {
                    throw new FbExceptionBuilder()
                            .nonTransientConnectionException(JaybirdErrorCodes.jb_invalidConnectionString)
                            .messageParameter(connectString)
                            .messageParameter("Port separator: ':' at beginning or end of: " + server)
                            .toFlatSQLException();
                } else if (portSep > 0) {
                    String portString = server.substring(portSep + 1);
                    port = parsePortNumber(connectString, portString);
                    server = server.substring(0, portSep);
                }
            }
        } else if (sep == -1) {
            throw new FbExceptionBuilder()
                    .nonTransientConnectionException(JaybirdErrorCodes.jb_invalidConnectionString)
                    .messageParameter(connectString)
                    .messageParameter("null or empty database name in connection string")
                    .toFlatSQLException();
        }
        return new DbAttachInfo(server, port, fileName, connectString);
    }

    private static DbAttachInfo parseLegacyConnectString(String connectString) throws SQLException {
        char hostSepChar = ':';
        char portSepChar = '/';
        String server = null;
        String fileName = null;
        Integer port = null;
        int sep = connectString.indexOf(hostSepChar);
        if (sep == 0 || sep == connectString.length() - 1) {
            throw new FbExceptionBuilder()
                    .nonTransientConnectionException(JaybirdErrorCodes.jb_invalidConnectionString)
                    .messageParameter(connectString)
                    .messageParameter("Host separator: '" + hostSepChar + "' at beginning or end")
                    .toFlatSQLException();
        } else if (sep > 0) {
            server = connectString.substring(0, sep);
            fileName = connectString.substring(sep + 1);
            int portSep = server.indexOf(portSepChar);
            if (portSep == 0 || portSep == server.length() - 1) {
                throw new FbExceptionBuilder()
                        .nonTransientConnectionException(JaybirdErrorCodes.jb_invalidConnectionString)
                        .messageParameter(connectString)
                        .messageParameter("Port separator: '" + portSepChar + "' at beginning or end of: " + server)
                        .toFlatSQLException();
            } else if (portSep > 0) {
                String portString = server.substring(portSep + 1);
                port = parsePortNumber(connectString, portString);
                server = server.substring(0, portSep);
            }
        } else if (sep == -1) {
            fileName = connectString;
        }
        return new DbAttachInfo(server, port, fileName, connectString);
    }

    private static int parsePortNumber(String connectString, String portString) throws SQLException {
        try {
            return Integer.parseInt(portString);
        } catch (NumberFormatException e) {
            throw new FbExceptionBuilder()
                    .nonTransientConnectionException(JaybirdErrorCodes.jb_invalidConnectionString)
                    .messageParameter(connectString)
                    .messageParameter("Bad port: '" + portString + "' is not a number")
                    .cause(e)
                    .toFlatSQLException();
        }
    }
}