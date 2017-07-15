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
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public abstract class AbstractImmutableAttachProperties<T extends IAttachProperties> implements IAttachProperties<T> {

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
    private final EncryptionLevel encryptionLevel;

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
    public AbstractImmutableAttachProperties(IAttachProperties src) {
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
        encryptionLevel = src.getEncryptionLevel();
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
    public void setUser(final String user) {
        immutable();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(final String password) {
        immutable();
    }

    @Override
    public String getRoleName() {
        return roleName;
    }

    @Override
    public void setRoleName(final String roleName) {
        immutable();
    }

    @Override
    public String getCharSet() {
        return charSet;
    }

    @Override
    public void setCharSet(final String charSet) {
        immutable();
    }

    @Override
    public String getEncoding() {
        return encoding;
    }

    @Override
    public void setEncoding(final String encoding) {
        immutable();
    }

    @Override
    public int getSocketBufferSize() {
        return socketBufferSize;
    }

    @Override
    public void setSocketBufferSize(final int socketBufferSize) {
        immutable();
    }

    @Override
    public int getSoTimeout() {
        return soTimeout;
    }

    @Override
    public void setSoTimeout(final int soTimeout) {
        immutable();
    }

    @Override
    public int getConnectTimeout() {
        return connectTimeout;
    }

    @Override
    public void setConnectTimeout(final int connectTimeout) {
        immutable();
    }

    @Override
    public EncryptionLevel getEncryptionLevel() {
        return encryptionLevel;
    }

    @Override
    public void setEncryptionLevel(final EncryptionLevel encryptionLevel) {
        immutable();
    }

    /**
     * Throws an UnsupportedOperationException
     */
    protected final void immutable() {
        throw new UnsupportedOperationException("this object is immutable");
    }
}
