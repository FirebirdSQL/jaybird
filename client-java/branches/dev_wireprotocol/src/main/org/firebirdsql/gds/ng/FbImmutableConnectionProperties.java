package org.firebirdsql.gds.ng;

/**
 * Immutable implementation of {@link org.firebirdsql.gds.ng.IConnectionProperties}.
 *
 * @author @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @see FbConnectionProperties
 * @since 2.3
 */
public final class FbImmutableConnectionProperties implements IConnectionProperties {

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
    private final boolean resultSetDefaultHoldable;
    private final boolean columnLabelForName;

    /**
     * Copy constructor for FbConnectionProperties.
     * <p>
     * All properties defined in {@link org.firebirdsql.gds.ng.IConnectionProperties} are
     * copied from <code>src</code> to the new instance.
     * </p>
     *
     * @param src
     *         Source to copy from
     */
    public FbImmutableConnectionProperties(IConnectionProperties src) {
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
        resultSetDefaultHoldable = src.isResultSetDefaultHoldable();
        columnLabelForName = src.isColumnLabelForName();
    }

    @Override
    public String getDatabaseName() {
        return databaseName;
    }

    @Override
    public void setDatabaseName(final String databaseName) {
        immutable();
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
    public String getRoleName() {
        return roleName;
    }

    @Override
    public void setRoleName(final String roleName) {
        immutable();
    }

    @Override
    public short getConnectionDialect() {
        return connectionDialect;
    }

    @Override
    public void setConnectionDialect(final short connectionDialect) {
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
    public int getPageCacheSize() {
        return pageCacheSize;
    }

    @Override
    public void setPageCacheSize(final int pageCacheSize) {
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
    public void setResultSetDefaultHoldable(final boolean holdable) {
        immutable();
    }

    @Override
    public boolean isResultSetDefaultHoldable() {
        return resultSetDefaultHoldable;
    }

    @Override
    public void setColumnLabelForName(final boolean columnLabelForName) {
        immutable();
    }

    @Override
    public boolean isColumnLabelForName() {
        return columnLabelForName;
    }

    @Override
    public IConnectionProperties asImmutable() {
        // Immutable already, so just return this
        return this;
    }

    /**
     * Throws an UnsupportedOperationException
     */
    private static void immutable() {
        throw new UnsupportedOperationException("this object is immutable");
    }
}
