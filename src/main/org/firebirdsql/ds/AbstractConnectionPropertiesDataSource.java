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
package org.firebirdsql.ds;

import org.firebirdsql.jdbc.FirebirdConnectionProperties;

import java.sql.SQLException;

/**
 * Abstract implementation of {@link FirebirdConnectionProperties} to ensure subclasses can be introspected as beans.
 *
 * @author Mark Rotteveel
 * @since 5
 */
public abstract class AbstractConnectionPropertiesDataSource extends RootCommonDataSource
        implements FirebirdConnectionProperties {

    // For a lot of properties, we redirect to the default implementations of the interface to ensure the
    // data source can be introspected as a JavaBean (default methods are not returned by the introspector)

    @Override
    public String getServerName() {
        return FirebirdConnectionProperties.super.getServerName();
    }

    @Override
    public void setServerName(String serverName) {
        FirebirdConnectionProperties.super.setServerName(serverName);
    }

    @Override
    public int getPortNumber() {
        return FirebirdConnectionProperties.super.getPortNumber();
    }

    @Override
    public void setPortNumber(int portNumber) {
        FirebirdConnectionProperties.super.setPortNumber(portNumber);
    }

    @Override
    public String getDatabaseName() {
        return FirebirdConnectionProperties.super.getDatabaseName();
    }

    @Override
    public void setDatabaseName(String databaseName) {
        FirebirdConnectionProperties.super.setDatabaseName(databaseName);
    }

    @Override
    public String getType() {
        return FirebirdConnectionProperties.super.getType();
    }

    @Override
    public void setType(String type) {
        FirebirdConnectionProperties.super.setType(type);
    }

    @Override
    public String getUser() {
        return FirebirdConnectionProperties.super.getUser();
    }

    @Override
    public void setUser(String user) {
        FirebirdConnectionProperties.super.setUser(user);
    }

    @Override
    public String getPassword() {
        return FirebirdConnectionProperties.super.getPassword();
    }

    @Override
    public void setPassword(String password) {
        FirebirdConnectionProperties.super.setPassword(password);
    }

    @Override
    public String getRoleName() {
        return FirebirdConnectionProperties.super.getRoleName();
    }

    @Override
    public void setRoleName(String roleName) {
        FirebirdConnectionProperties.super.setRoleName(roleName);
    }

    @Override
    public String getCharSet() {
        return FirebirdConnectionProperties.super.getCharSet();
    }

    @Override
    public void setCharSet(String charSet) {
        FirebirdConnectionProperties.super.setCharSet(charSet);
    }

    @Override
    public String getEncoding() {
        return FirebirdConnectionProperties.super.getEncoding();
    }

    @Override
    public void setEncoding(String encoding) {
        FirebirdConnectionProperties.super.setEncoding(encoding);
    }

    @Override
    public Integer getProcessId() {
        return FirebirdConnectionProperties.super.getProcessId();
    }

    @Override
    public void setProcessId(Integer processId) {
        FirebirdConnectionProperties.super.setProcessId(processId);
    }

    @Override
    public String getProcessName() {
        return FirebirdConnectionProperties.super.getProcessName();
    }

    @Override
    public void setProcessName(String processName) {
        FirebirdConnectionProperties.super.setProcessName(processName);
    }

    @Override
    public int getSocketBufferSize() {
        return FirebirdConnectionProperties.super.getSocketBufferSize();
    }

    @Override
    public void setSocketBufferSize(int socketBufferSize) {
        FirebirdConnectionProperties.super.setSocketBufferSize(socketBufferSize);
    }

    @Override
    public int getSoTimeout() {
        return FirebirdConnectionProperties.super.getSoTimeout();
    }

    @Override
    public void setSoTimeout(int soTimeout) {
        FirebirdConnectionProperties.super.setSoTimeout(soTimeout);
    }

    @Override
    public int getConnectTimeout() {
        return FirebirdConnectionProperties.super.getConnectTimeout();
    }

    @Override
    public void setConnectTimeout(int connectTimeout) {
        FirebirdConnectionProperties.super.setConnectTimeout(connectTimeout);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This property is an alias for the connectTimeout property.
     * </p>
     */
    @Override
    public int getLoginTimeout() throws SQLException {
        return getConnectTimeout();
    }

    /**
     * {@inheritDoc}
     * <p>
     * This property is an alias for the connectTimeout property.
     * </p>
     */
    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        setConnectTimeout(seconds);
    }

    @Override
    public String getWireCrypt() {
        return FirebirdConnectionProperties.super.getWireCrypt();
    }

    @Override
    public void setWireCrypt(String wireCrypt) {
        FirebirdConnectionProperties.super.setWireCrypt(wireCrypt);
    }

    @Override
    public String getDbCryptConfig() {
        return FirebirdConnectionProperties.super.getDbCryptConfig();
    }

    @Override
    public void setDbCryptConfig(String dbCryptConfig) {
        FirebirdConnectionProperties.super.setDbCryptConfig(dbCryptConfig);
    }

    @Override
    public String getAuthPlugins() {
        return FirebirdConnectionProperties.super.getAuthPlugins();
    }

    @Override
    public void setAuthPlugins(String authPlugins) {
        FirebirdConnectionProperties.super.setAuthPlugins(authPlugins);
    }

    @Override
    public boolean isWireCompression() {
        return FirebirdConnectionProperties.super.isWireCompression();
    }

    @Override
    public void setWireCompression(boolean wireCompression) {
        FirebirdConnectionProperties.super.setWireCompression(wireCompression);
    }

    @Override
    public String getEnableProtocol() {
        return FirebirdConnectionProperties.super.getEnableProtocol();
    }

    @Override
    public void setEnableProtocol(String enableProtocol) {
        FirebirdConnectionProperties.super.setEnableProtocol(enableProtocol);
    }

    @Override
    public int getSqlDialect() {
        return FirebirdConnectionProperties.super.getSqlDialect();
    }

    @Override
    public void setSqlDialect(int sqlDialect) {
        FirebirdConnectionProperties.super.setSqlDialect(sqlDialect);
    }

    @Override
    public int getPageCacheSize() {
        return FirebirdConnectionProperties.super.getPageCacheSize();
    }

    @Override
    public void setPageCacheSize(int pageCacheSize) {
        FirebirdConnectionProperties.super.setPageCacheSize(pageCacheSize);
    }

    @Override
    public String getDataTypeBind() {
        return FirebirdConnectionProperties.super.getDataTypeBind();
    }

    @Override
    public void setDataTypeBind(String dataTypeBind) {
        FirebirdConnectionProperties.super.setDataTypeBind(dataTypeBind);
    }

    @Override
    public String getSessionTimeZone() {
        return FirebirdConnectionProperties.super.getSessionTimeZone();
    }

    @Override
    public void setSessionTimeZone(String sessionTimeZone) {
        FirebirdConnectionProperties.super.setSessionTimeZone(sessionTimeZone);
    }

    @Override
    public int getBlobBufferSize() {
        return FirebirdConnectionProperties.super.getBlobBufferSize();
    }

    @Override
    public void setBlobBufferSize(int blobBufferSize) {
        FirebirdConnectionProperties.super.setBlobBufferSize(blobBufferSize);
    }

    @Override
    public boolean isUseStreamBlobs() {
        return FirebirdConnectionProperties.super.isUseStreamBlobs();
    }

    @Override
    public void setUseStreamBlobs(boolean useStreamBlobs) {
        FirebirdConnectionProperties.super.setUseStreamBlobs(useStreamBlobs);
    }

    @Override
    public boolean isDefaultResultSetHoldable() {
        return FirebirdConnectionProperties.super.isDefaultResultSetHoldable();
    }

    @Override
    public void setDefaultResultSetHoldable(boolean defaultResultSetHoldable) {
        FirebirdConnectionProperties.super.setDefaultResultSetHoldable(defaultResultSetHoldable);
    }

    @Override
    public boolean isUseFirebirdAutocommit() {
        return FirebirdConnectionProperties.super.isUseFirebirdAutocommit();
    }

    @Override
    public void setUseFirebirdAutocommit(boolean useFirebirdAutocommit) {
        FirebirdConnectionProperties.super.setUseFirebirdAutocommit(useFirebirdAutocommit);
    }

    @Override
    public boolean isColumnLabelForName() {
        return FirebirdConnectionProperties.super.isColumnLabelForName();
    }

    @Override
    public void setColumnLabelForName(boolean columnLabelForName) {
        FirebirdConnectionProperties.super.setColumnLabelForName(columnLabelForName);
    }

    @Override
    public String getGeneratedKeysEnabled() {
        return FirebirdConnectionProperties.super.getGeneratedKeysEnabled();
    }

    @Override
    public void setGeneratedKeysEnabled(String generatedKeysEnabled) {
        FirebirdConnectionProperties.super.setGeneratedKeysEnabled(generatedKeysEnabled);
    }

    @Override
    public boolean isIgnoreProcedureType() {
        return FirebirdConnectionProperties.super.isIgnoreProcedureType();
    }

    @Override
    public void setIgnoreProcedureType(boolean ignoreProcedureType) {
        FirebirdConnectionProperties.super.setIgnoreProcedureType(ignoreProcedureType);
    }

    @Override
    public String getDecfloatRound() {
        return FirebirdConnectionProperties.super.getDecfloatRound();
    }

    @Override
    public void setDecfloatRound(String decfloatRound) {
        FirebirdConnectionProperties.super.setDecfloatRound(decfloatRound);
    }

    @Override
    public String getDecfloatTraps() {
        return FirebirdConnectionProperties.super.getDecfloatTraps();
    }

    @Override
    public void setDecfloatTraps(String decfloatTraps) {
        FirebirdConnectionProperties.super.setDecfloatTraps(decfloatTraps);
    }

    @Override
    public String getTpbMapping() {
        return FirebirdConnectionProperties.super.getTpbMapping();
    }

    @Override
    public void setTpbMapping(String tpbMapping) {
        FirebirdConnectionProperties.super.setTpbMapping(tpbMapping);
    }

    @Override
    public int getDefaultTransactionIsolation() {
        return FirebirdConnectionProperties.super.getDefaultTransactionIsolation();
    }

    @Override
    public void setDefaultTransactionIsolation(int defaultIsolationLevel) {
        FirebirdConnectionProperties.super.setDefaultTransactionIsolation(defaultIsolationLevel);
    }

    @Override
    public String getDefaultIsolation() {
        return FirebirdConnectionProperties.super.getDefaultIsolation();
    }

    @Override
    public void setDefaultIsolation(String isolation) {
        FirebirdConnectionProperties.super.setDefaultIsolation(isolation);
    }

    @Override
    public String getScrollableCursor() {
        return FirebirdConnectionProperties.super.getScrollableCursor();
    }

    @Override
    public void setScrollableCursor(String scrollableCursor) {
        FirebirdConnectionProperties.super.setScrollableCursor(scrollableCursor);
    }

    @Override
    public boolean isUseServerBatch() {
        return FirebirdConnectionProperties.super.isUseServerBatch();
    }

    @Override
    public void setUseServerBatch(boolean useServerBatch) {
        FirebirdConnectionProperties.super.setUseServerBatch(useServerBatch);
    }

    @Override
    public int getServerBatchBufferSize() {
        return FirebirdConnectionProperties.super.getServerBatchBufferSize();
    }

    @Override
    public void setServerBatchBufferSize(int serverBatchBufferSize) {
        FirebirdConnectionProperties.super.setServerBatchBufferSize(serverBatchBufferSize);
    }

    @Override
    public int getParallelWorkers() {
        return FirebirdConnectionProperties.super.getParallelWorkers();
    }

    @Override
    public void setParallelWorkers(int parallelWorkers) {
        FirebirdConnectionProperties.super.setParallelWorkers(parallelWorkers);
    }

    @Override
    public boolean isUseCatalogAsPackage() {
        return FirebirdConnectionProperties.super.isUseCatalogAsPackage();
    }

    @Override
    public void setUseCatalogAsPackage(boolean useCatalogAsPackage) {
        FirebirdConnectionProperties.super.setUseCatalogAsPackage(useCatalogAsPackage);
    }

    @Override
    public boolean isAllowTxStmts() {
        return FirebirdConnectionProperties.super.isAllowTxStmts();
    }

    @Override
    public void setAllowTxStmts(boolean allowTxStmts) {
        FirebirdConnectionProperties.super.setAllowTxStmts(allowTxStmts);
    }

    @Override
    public boolean isExtendedMetadata() {
        return FirebirdConnectionProperties.super.isExtendedMetadata();
    }

    @Override
    public void setExtendedMetadata(boolean extendedMetadata) {
        FirebirdConnectionProperties.super.setExtendedMetadata(extendedMetadata);
    }

    @Override
    public boolean isCreateDatabaseIfNotExist() {
        return FirebirdConnectionProperties.super.isCreateDatabaseIfNotExist();
    }

    @Override
    public void setCreateDatabaseIfNotExist(boolean createDatabaseIfNotExist) {
        FirebirdConnectionProperties.super.setCreateDatabaseIfNotExist(createDatabaseIfNotExist);
    }

    @SuppressWarnings("deprecation")
    @Deprecated(since = "5")
    @Override
    public String getUserName() {
        return FirebirdConnectionProperties.super.getUserName();
    }

    @SuppressWarnings("deprecation")
    @Deprecated(since = "5")
    @Override
    public void setUserName(String userName) {
        FirebirdConnectionProperties.super.setUserName(userName);
    }

}
