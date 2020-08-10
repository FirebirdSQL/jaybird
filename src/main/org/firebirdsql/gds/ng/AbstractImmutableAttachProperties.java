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

/**
 * Abstract immutable implementation of {@link org.firebirdsql.gds.ng.IAttachProperties}.
 * <p>
 * NOTE: This class relies on the default implementation provided in
 * {@link org.firebirdsql.jaybird.props.AttachmentProperties}, so in itself, immutability is not guaranteed by this
 * class: subclasses need to be {@code final} and guard against mutation (that is, they do not override setters, unless
 * they call {@link #immutable()}(.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public abstract class AbstractImmutableAttachProperties<T extends IAttachProperties<T>> implements IAttachProperties<T> {

    private final String serverName;
    private final int portNumber;
    private final String user;
    private final String password;
    private final String roleName;
    private final String charSet;
    private final String encoding;
    private final int socketBufferSize;
    private final int soTimeout;
    private final int connectTimeout;
    private final WireCrypt wireCrypt;
    private final String dbCryptConfig;
    private final String authPlugins;
    private final boolean wireCompression;

    /**
     * Copy constructor for IAttachProperties.
     * <p>
     * All properties defined in {@link org.firebirdsql.gds.ng.IAttachProperties} are copied
     * from <code>src</code> to the new instance.
     * </p>
     *
     * @param src
     *         Source to copy from
     */
    protected AbstractImmutableAttachProperties(IAttachProperties<T> src) {
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

    @Override
    public String getServerName() {
        return serverName;
    }

    @Override
    public void setServerName(final String serverName) {
        immutable();
    }

    @Override
    public int getPortNumber() {
        return portNumber;
    }

    @Override
    public void setPortNumber(final int portNumber) {
        immutable();
    }

    @Override
    public String getUser() {
        return user;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getRoleName() {
        return roleName;
    }

    @Override
    public String getCharSet() {
        return charSet;
    }

    @Override
    public String getEncoding() {
        return encoding;
    }

    @Override
    public int getSocketBufferSize() {
        return socketBufferSize;
    }

    @Override
    public int getSoTimeout() {
        return soTimeout;
    }

    @Override
    public int getConnectTimeout() {
        return connectTimeout;
    }

    @Override
    public String getWireCrypt() {
        return wireCrypt != null ? wireCrypt.name() : null;
    }

    @Override
    public void setWireCrypt(final WireCrypt wireCrypt) {
        immutable();
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
    public String getAuthPlugins() {
        return authPlugins;
    }

    @Override
    public boolean isWireCompression() {
        return wireCompression;
    }

    @Override
    public final String getProperty(String name) {
        throw new AssertionError("not yet implemented");
    }

    @Override
    public final void setProperty(String name, String value) {
        immutable();
    }

    @Override
    public final Integer getIntProperty(String name) {
        throw new AssertionError("not yet implemented");
    }

    @Override
    public final void setIntProperty(String name, Integer value) {
        immutable();
    }

    @Override
    public final Boolean getBooleanProperty(String name) {
        throw new AssertionError("not yet implemented");
    }

    @Override
    public final void setBooleanProperty(String name, Boolean value) {
        immutable();
    }

    /**
     * Throws an UnsupportedOperationException
     */
    protected final void immutable() {
        throw new UnsupportedOperationException("this object is immutable");
    }
}
