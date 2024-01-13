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
package org.firebirdsql.gds.impl;

import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.IAttachProperties;
import org.firebirdsql.jaybird.props.AttachmentProperties;
import org.firebirdsql.jaybird.props.PropertyConstants;
import org.firebirdsql.jaybird.props.PropertyNames;
import org.firebirdsql.util.InternalApi;

import java.sql.SQLException;

/**
 * Container for attachment information (i.e. server, port and filename/alias/service name/url).
 */
@InternalApi
public record DbAttachInfo(String serverName, int portNumber, String attachObjectName) {

    public DbAttachInfo {
        if (serverName != null && serverName.isEmpty()) {
            serverName = null;
        }
        if (attachObjectName != null && attachObjectName.isEmpty()) {
            attachObjectName = null;
        }
    }

    public DbAttachInfo(String serverName, Integer portNumber, String attachObjectName) {
        this(serverName, portNumber != null ? portNumber : PropertyConstants.DEFAULT_PORT, attachObjectName);
    }

    public static DbAttachInfo of(AttachmentProperties attachmentProperties) {
        return new DbAttachInfo(attachmentProperties.getServerName(), attachmentProperties.getPortNumber(),
                attachmentProperties.getProperty(PropertyNames.attachObjectName));
    }

    public boolean hasServerName() {
        return serverName != null;
    }

    public boolean hasAttachObjectName() {
        return attachObjectName != null;
    }

    public DbAttachInfo withServerName(String serverName) {
        return new DbAttachInfo(serverName, portNumber, attachObjectName);
    }

    public DbAttachInfo withAttachObjectName(String attachObjectName) {
        return new DbAttachInfo(serverName, portNumber, attachObjectName);
    }

    public <T extends IAttachProperties<T>> void copyTo(T attachProperties) {
        attachProperties.setServerName(serverName);
        attachProperties.setPortNumber(portNumber);
        attachProperties.setAttachObjectName(attachObjectName);
    }

    public static DbAttachInfo parseConnectString(String connectString) throws SQLException {
        if (connectString == null) {
            throw FbExceptionBuilder.forNonTransientConnectionException(JaybirdErrorCodes.jb_invalidConnectionString)
                    .messageParameter("(null)")
                    .messageParameter("Connection string is missing")
                    .toSQLException();
        }

        // allows standard syntax //host:port/.... and old fb syntax host/port:....
        connectString = connectString.trim();
        if (connectString.startsWith("//")) {
            return parseUrlConnectString(connectString.substring(2), connectString);
        } else {
            return parseLegacyConnectString(connectString);
        }
    }

    private static DbAttachInfo parseUrlConnectString(String connectString, String originalConnectString)
            throws SQLException {
        // Expect host/filename, host:port/filename, ipv4/filename, ipv4:port/filename, [ipv6]/filename or [ipv6]:port/filename
        if (connectString.isEmpty()) {
            // allow just '//'
            return new DbAttachInfo(null, null, null);
        }
        String server;
        String fileName;
        Integer port = null;
        int pathSep;
        int portSep;
        int connectStringLength = connectString.length();
        if (connectString.charAt(0) == '[') {
            // IPv6 address
            int endIpv6Address = connectString.indexOf(']');
            if (endIpv6Address == -1) {
                throw FbExceptionBuilder
                        .forNonTransientConnectionException(JaybirdErrorCodes.jb_invalidConnectionString)
                        .messageParameter(originalConnectString)
                        .messageParameter("IPv6 address expected, missing closing ']'")
                        .toSQLException();
            }
            server = connectString.substring(1, endIpv6Address);
            int afterEndIpv6Address = endIpv6Address + 1;
            pathSep = connectString.indexOf('/', afterEndIpv6Address);
            if (pathSep == -1) pathSep = connectStringLength;
            portSep = connectString.indexOf(':', afterEndIpv6Address);
            if (portSep > pathSep) portSep = -1;
            if (!(portSep == afterEndIpv6Address || pathSep == afterEndIpv6Address)) {
                throw FbExceptionBuilder
                        .forNonTransientConnectionException(JaybirdErrorCodes.jb_invalidConnectionString)
                        .messageParameter(originalConnectString)
                        .messageParameter("Unexpected tokens '" + connectString.substring(afterEndIpv6Address)
                                + "' after IPv6 address")
                        .toSQLException();
            }
        } else {
            pathSep = connectString.indexOf('/');
            if (pathSep == -1) pathSep = connectStringLength;
            if (pathSep == 0) {
                portSep = -1;
                server = null;
            } else {
                portSep = connectString.indexOf(':');
                if (portSep > pathSep) portSep = -1;
                int endServer = portSep != -1 ? portSep : pathSep;
                server = connectString.substring(0, endServer);
            }
        }
        if (portSep == 0 || portSep == connectStringLength - 1) {
            throw FbExceptionBuilder.forNonTransientConnectionException(JaybirdErrorCodes.jb_invalidConnectionString)
                    .messageParameter(originalConnectString)
                    .messageParameter("Port separator ':' at beginning or end")
                    .toSQLException();
        } else if (portSep > 0) {
            port = parsePortNumber(originalConnectString, connectString.substring(portSep + 1, pathSep));
        }

        fileName = pathSep < connectStringLength - 1 ? connectString.substring(pathSep + 1) : null;

        return new DbAttachInfo(server, port, fileName);
    }

    private static DbAttachInfo parseLegacyConnectString(String connectString) throws SQLException {
        // NOTE: This method does not support IPv6 addresses enclosed in []
        String server = null;
        String fileName;
        Integer port = null;
        int sep = connectString.indexOf(':');
        if (sep == 0) {
            throw FbExceptionBuilder.forNonTransientConnectionException(JaybirdErrorCodes.jb_invalidConnectionString)
                    .messageParameter(connectString)
                    .messageParameter("Path separator ':' at beginning")
                    .toSQLException();
        } else if (sep == 1 && !isLikelyWindowsAbsolutePath(connectString) || sep > 1) {
            server = connectString.substring(0, sep);
            fileName = connectString.substring(sep + 1);
            int portSep = server.indexOf('/');
            if (portSep == 0 || portSep == server.length() - 1) {
                throw FbExceptionBuilder
                        .forNonTransientConnectionException(JaybirdErrorCodes.jb_invalidConnectionString)
                        .messageParameter(connectString)
                        .messageParameter("Port separator '/' at beginning or end")
                        .toSQLException();
            } else if (portSep > 0) {
                String portString = server.substring(portSep + 1);
                port = parsePortNumber(connectString, portString);
                server = server.substring(0, portSep);
            }
        } else {
            fileName = connectString;
        }
        return new DbAttachInfo(server, port, fileName);
    }

    private static boolean isLikelyWindowsAbsolutePath(String connectString) {
        if (connectString.length() < 4 || connectString.charAt(1) != ':') {
            return false;
        } else {
            char possiblyPathSeparator = connectString.charAt(2);
            if (possiblyPathSeparator == '\\' || possiblyPathSeparator == '/') {
                char possiblyDriveLetter = connectString.charAt(0);
                return 'C' <= possiblyDriveLetter && possiblyDriveLetter <= 'Z'
                        || 'c' <= possiblyDriveLetter && possiblyDriveLetter <= 'z';
            } else {
                return false;
            }
        }
    }

    private static Integer parsePortNumber(String connectString, String portString) throws SQLException {
        try {
            return Integer.valueOf(portString);
        } catch (NumberFormatException e) {
            throw FbExceptionBuilder.forNonTransientConnectionException(JaybirdErrorCodes.jb_invalidConnectionString)
                    .messageParameter(connectString)
                    .messageParameter("Bad port: '" + portString + "' is not a number")
                    .cause(e)
                    .toSQLException();
        }
    }
}