/*
 * $Id$
 * 
 * Firebird Open Source J2EE Connector - JDBC Driver
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
package org.firebirdsql.gds.ng;

/**
 * Mutable implementation of {@link IConnectionProperties}
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @see FbImmutableConnectionProperties
 * @since 2.3
 */
public final class FbConnectionProperties implements IConnectionProperties {

    private String databaseName;
    private String serverName = IConnectionProperties.DEFAULT_SERVER_NAME;
    private int portNumber = IConnectionProperties.DEFAULT_PORT;
    private String user;
    private String password;
    private String charSet;
    private String encoding;
    private String roleName;
    private short connectionDialect = IConnectionProperties.DEFAULT_DIALECT;
    private int socketBufferSize = IConnectionProperties.DEFAULT_SOCKET_BUFFER_SIZE;
    private int pageCacheSize;
    private int soTimeout = IConnectionProperties.DEFAULT_SO_TIMEOUT;
    private int connectTimeout = IConnectionProperties.DEFAULT_CONNECT_TIMEOUT;

    /**
     * Copy constructor for FbConnectionProperties.
     * <p>
     * All properties defined in {@link IConnectionProperties} are
     * copied from <code>src</code> to the new instance.
     * </p>
     *
     * @param src
     *         Source to copy from
     */
    public FbConnectionProperties(IConnectionProperties src) {
        this();
        if (src != null) {
            databaseName = src.getDatabaseName();
            serverName = src.getServerName();
            portNumber = src.getPortNumber();
            user = src.getUser();
            password = src.getPassword();
            charSet = src.getCharSet();
            encoding = src.getEncoding();
            roleName = src.getRoleName();
            connectionDialect = src.getConnectionDialect();
            socketBufferSize = src.getSocketBufferSize();
            pageCacheSize = src.getPageCacheSize();
            soTimeout = src.getSoTimeout();
            connectTimeout = src.getConnectTimeout();
        }
    }

    /**
     * Default constructor for FbConnectionProperties
     */
    public FbConnectionProperties() {
    }

    @Override
    public String getDatabaseName() {
        return databaseName;
    }

    @Override
    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    @Override
    public String getServerName() {
        return serverName;
    }

    @Override
    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    @Override
    public int getPortNumber() {
        return portNumber;
    }

    @Override
    public void setPortNumber(int portNumber) {
        this.portNumber = portNumber;
    }

    @Override
    public String getUser() {
        return user;
    }

    @Override
    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getCharSet() {
        return charSet;
    }

    @Override
    public void setCharSet(String charSet) {
        this.charSet = charSet;
    }

    @Override
    public String getEncoding() {
        return encoding;
    }

    @Override
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    @Override
    public String getRoleName() {
        return roleName;
    }

    @Override
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    @Override
    public short getConnectionDialect() {
        return connectionDialect;
    }

    @Override
    public void setConnectionDialect(short connectionDialect) {
        this.connectionDialect = connectionDialect;
    }

    @Override
    public int getSocketBufferSize() {
        return socketBufferSize;
    }

    @Override
    public void setSocketBufferSize(int socketBufferSize) {
        this.socketBufferSize = socketBufferSize;
    }

    @Override
    public int getPageCacheSize() {
        return pageCacheSize;
    }

    @Override
    public void setPageCacheSize(int pageCacheSize) {
        this.pageCacheSize = pageCacheSize;
    }

    @Override
    public int getSoTimeout() {
        return soTimeout;
    }

    @Override
    public void setSoTimeout(int soTimeout) {
        this.soTimeout = soTimeout;
    }

    @Override
    public int getConnectTimeout() {
        return connectTimeout;
    }

    @Override
    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    @Override
    public IConnectionProperties asImmutable() {
        return new FbImmutableConnectionProperties(this);
    }
}
