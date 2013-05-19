package org.firebirdsql.gds.ng;

/**
 * Immutable implementation of {@link org.firebirdsql.gds.ng.IConnectionPropertiesGetters}.
 *
 * @author @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @see FbConnectionProperties
 * @since 2.3
 */
public final class FbImmutableConnectionProperties implements IConnectionPropertiesGetters {

    private final String databaseName;
    private final String serverName;
    private final int portNumber;
    private final String user;
    private final String password;
    private final String charSet;
    private final String encoding;
    private final String roleName;
    private final short connectionDialect;
    private final int socketBufferSize;
    private final int pageCacheSize;
    private final int soTimeout;
    private final int connectTimeout;

    /**
     * Copy constructor for FbConnectionProperties.
     * <p>
     * All properties defined in {@link org.firebirdsql.gds.ng.IConnectionPropertiesGetters} are
     * copied from <code>src</code> to the new instance.
     * </p>
     *
     * @param src
     *         Source to copy from
     */
    public FbImmutableConnectionProperties(IConnectionPropertiesGetters src) {
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

    @Override
    public String getDatabaseName() {
        return databaseName;
    }

    @Override
    public String getServerName() {
        return serverName;
    }

    @Override
    public int getPortNumber() {
        return portNumber;
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
    public String getCharSet() {
        return charSet;
    }

    @Override
    public String getEncoding() {
        return encoding;
    }

    @Override
    public String getRoleName() {
        return roleName;
    }

    @Override
    public short getConnectionDialect() {
        return connectionDialect;
    }

    @Override
    public int getSocketBufferSize() {
        return socketBufferSize;
    }

    @Override
    public int getPageCacheSize() {
        return pageCacheSize;
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
    public IConnectionPropertiesGetters asImmutable() {
        // Immutable already, so just return this
        return this;
    }
}
