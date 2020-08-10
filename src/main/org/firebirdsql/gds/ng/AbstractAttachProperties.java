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
package org.firebirdsql.gds.ng;

import static java.util.Objects.requireNonNull;

/**
 * Abstract mutable implementation of {@link IAttachProperties}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public abstract class AbstractAttachProperties<T extends IAttachProperties<T>> implements IAttachProperties<T> {

    private String serverName = IAttachProperties.DEFAULT_SERVER_NAME;
    private int portNumber = IAttachProperties.DEFAULT_PORT;
    private String user;
    private String password;
    private String roleName;
    private String charSet;
    private String encoding;
    private int socketBufferSize = IAttachProperties.DEFAULT_SOCKET_BUFFER_SIZE;
    private int soTimeout = IAttachProperties.DEFAULT_SO_TIMEOUT;
    private int connectTimeout = IAttachProperties.DEFAULT_CONNECT_TIMEOUT;
    private WireCrypt wireCrypt = WireCrypt.DEFAULT;
    private String dbCryptConfig;
    private String authPlugins;
    private boolean wireCompression;

    /**
     * Copy constructor for IAttachProperties.
     * <p>
     * All properties defined in {@link IAttachProperties} are copied from <code>src</code> to the new instance.
     * </p>
     *
     * @param src
     *         Source to copy from
     */
    protected AbstractAttachProperties(IAttachProperties<T> src) {
        if (src != null) {
            serverName = src.getServerName();
            portNumber = src.getPortNumber();
            user = src.getUser();
            password = src.getPassword();
            roleName = src.getRoleName();
            charSet = src.getCharSet();
            encoding = src.getEncoding();
            socketBufferSize = src.getSocketBufferSize();
            soTimeout = src.getSoTimeout();
            connectTimeout = src.getConnectTimeout();
            wireCrypt = WireCrypt.fromString(src.getWireCrypt());
            dbCryptConfig = src.getDbCryptConfig();
            authPlugins = src.getAuthPlugins();
            wireCompression = src.isWireCompression();
        }
    }

    /**
     * Default constructor for AbstractAttachProperties
     */
    protected AbstractAttachProperties() {
    }

    @Override
    public String getServerName() {
        return serverName;
    }

    @Override
    public void setServerName(String serverName) {
        this.serverName = serverName;
        dirtied();
    }

    @Override
    public int getPortNumber() {
        return portNumber;
    }

    @Override
    public void setPortNumber(int portNumber) {
        this.portNumber = portNumber;
        dirtied();
    }

    @Override
    public String getUser() {
        return user;
    }

    @Override
    public void setUser(String user) {
        this.user = user;
        dirtied();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
        dirtied();
    }

    @Override
    public String getRoleName() {
        return roleName;
    }

    @Override
    public void setRoleName(String roleName) {
        this.roleName = roleName;
        dirtied();
    }

    @Override
    public String getCharSet() {
        return charSet;
    }

    @Override
    public void setCharSet(String charSet) {
        this.charSet = charSet;
        dirtied();
    }

    @Override
    public String getEncoding() {
        return encoding;
    }

    @Override
    public void setEncoding(String encoding) {
        this.encoding = encoding;
        dirtied();
    }

    @Override
    public int getSocketBufferSize() {
        return socketBufferSize;
    }

    @Override
    public void setSocketBufferSize(int socketBufferSize) {
        this.socketBufferSize = socketBufferSize;
        dirtied();
    }

    @Override
    public int getSoTimeout() {
        return soTimeout;
    }

    @Override
    public void setSoTimeout(int soTimeout) {
        this.soTimeout = soTimeout;
        dirtied();
    }

    @Override
    public int getConnectTimeout() {
        return connectTimeout;
    }

    @Override
    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        dirtied();
    }

    @Override
    public String getWireCrypt() {
        return wireCrypt != null ? wireCrypt.name() : null;
    }

    @Override
    public void setWireCrypt(String wireCrypt) {
        setWireCrypt(WireCrypt.fromString(wireCrypt));
    }

    @Override
    public void setWireCrypt(WireCrypt wireCrypt) {
        this.wireCrypt = requireNonNull(wireCrypt, "wireCrypt");
        dirtied();
    }

    @Override
    public WireCrypt getWireCryptAsEnum() {
        return wireCrypt;
    }

    @Override
    public String getDbCryptConfig() {
        return dbCryptConfig;
    }

    @Override
    public void setDbCryptConfig(String dbCryptConfig) {
        this.dbCryptConfig = dbCryptConfig;
        dirtied();
    }

    @Override
    public String getAuthPlugins() {
        return authPlugins;
    }

    @Override
    public void setAuthPlugins(String authPlugins) {
        this.authPlugins = authPlugins;
        dirtied();
    }

    @Override
    public boolean isWireCompression() {
        return wireCompression;
    }

    @Override
    public void setWireCompression(boolean wireCompression) {
        this.wireCompression = wireCompression;
        dirtied();
    }

    @Override
    public void setProperty(String name, String value) {
        throw new AssertionError("not yet implemented");
    }

    @Override
    public String getProperty(String name) {
        throw new AssertionError("not yet implemented");
    }

    @Override
    public void setIntProperty(String name, Integer value) {
        throw new AssertionError("not yet implemented");
    }

    @Override
    public Integer getIntProperty(String name) {
        throw new AssertionError("not yet implemented");
    }

    @Override
    public void setBooleanProperty(String name, Boolean value) {
        throw new AssertionError("not yet implemented");
    }

    @Override
    public Boolean getBooleanProperty(String name) {
        throw new AssertionError("not yet implemented");
    }

    /**
     * Called by setters if they have been called.
     */
    protected abstract void dirtied();
}
