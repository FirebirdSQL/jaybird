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

import org.firebirdsql.gds.DatabaseParameterBuffer;
import org.firebirdsql.gds.ISCConstants;

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
    private boolean resultSetDefaultHoldable;
    private boolean columnLabelForName;

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
    public void setResultSetDefaultHoldable(final boolean holdable) {
        resultSetDefaultHoldable = holdable;
    }

    @Override
    public boolean isResultSetDefaultHoldable() {
        return resultSetDefaultHoldable;
    }

    @Override
    public void setColumnLabelForName(final boolean columnLabelForName) {
        this.columnLabelForName = columnLabelForName;
    }

    @Override
    public boolean isColumnLabelForName() {
        return columnLabelForName;
    }

    @Override
    public IConnectionProperties asImmutable() {
        return new FbImmutableConnectionProperties(this);
    }

    /**
     * Method to populate an FbConnectionProperties from a database parameter buffer.
     * <p>
     * Unsupported or unknown properties are ignored.
     * </p>
     *
     * @param dpb
     *         Database parameter buffer
     * @deprecated TODO: This method is only intended to simplify migration of the protocol implementation and needs to be removed.
     */
    @Deprecated
    public void fromDpb(DatabaseParameterBuffer dpb) {
        if (dpb.hasArgument(ISCConstants.isc_dpb_user_name)) {
            setUser(dpb.getArgumentAsString(ISCConstants.isc_dpb_user_name));
        }
        if (dpb.hasArgument(ISCConstants.isc_dpb_password)) {
            setPassword(dpb.getArgumentAsString(ISCConstants.isc_dpb_password));
        }
        if (dpb.hasArgument(ISCConstants.isc_dpb_sql_role_name)) {
            setRoleName(dpb.getArgumentAsString(ISCConstants.isc_dpb_sql_role_name));
        }

        if (dpb.hasArgument(ISCConstants.isc_dpb_lc_ctype)) {
            setEncoding(dpb.getArgumentAsString(ISCConstants.isc_dpb_lc_ctype));
        }
        if (dpb.hasArgument(ISCConstants.isc_dpb_local_encoding)) {
            setCharSet(dpb.getArgumentAsString(ISCConstants.isc_dpb_local_encoding));
        }

        if (dpb.hasArgument(ISCConstants.isc_dpb_sql_dialect)) {
            setConnectionDialect((short) dpb.getArgumentAsInt(ISCConstants.isc_dpb_sql_dialect));
        }

        if (dpb.hasArgument(ISCConstants.isc_dpb_num_buffers)) {
            setPageCacheSize(dpb.getArgumentAsInt(ISCConstants.isc_dpb_num_buffers));
        }

        if (dpb.hasArgument(ISCConstants.isc_dpb_connect_timeout)) {
            setConnectTimeout(ISCConstants.isc_dpb_connect_timeout);
        }
        if (dpb.hasArgument(ISCConstants.isc_dpb_so_timeout)) {
            setSoTimeout(ISCConstants.isc_dpb_so_timeout);
        }

        if (dpb.hasArgument(ISCConstants.isc_dpb_socket_buffer_size)) {
            setSocketBufferSize(dpb.getArgumentAsInt(ISCConstants.isc_dpb_socket_buffer_size));
        }

        if (dpb.hasArgument(ISCConstants.isc_dpb_result_set_holdable)) {
            setResultSetDefaultHoldable(true);
        }

        if (dpb.hasArgument(ISCConstants.isc_dpb_column_label_for_name)) {
            setColumnLabelForName(true);
        }
    }
}
