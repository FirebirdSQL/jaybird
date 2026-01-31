// SPDX-FileCopyrightText: Copyright 2021-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.ds;

import org.firebirdsql.jdbc.FirebirdConnectionProperties;
import org.jspecify.annotations.Nullable;

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
    public @Nullable String getServerName() {
        return FirebirdConnectionProperties.super.getServerName();
    }

    @Override
    public void setServerName(@Nullable String serverName) {
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
    public @Nullable String getDatabaseName() {
        return FirebirdConnectionProperties.super.getDatabaseName();
    }

    @Override
    public void setDatabaseName(@Nullable String databaseName) {
        FirebirdConnectionProperties.super.setDatabaseName(databaseName);
    }

    @Override
    public @Nullable String getType() {
        return FirebirdConnectionProperties.super.getType();
    }

    @Override
    public void setType(@Nullable String type) {
        FirebirdConnectionProperties.super.setType(type);
    }

    @Override
    public @Nullable String getUser() {
        return FirebirdConnectionProperties.super.getUser();
    }

    @Override
    public void setUser(@Nullable String user) {
        FirebirdConnectionProperties.super.setUser(user);
    }

    @Override
    public @Nullable String getPassword() {
        return FirebirdConnectionProperties.super.getPassword();
    }

    @Override
    public void setPassword(@Nullable String password) {
        FirebirdConnectionProperties.super.setPassword(password);
    }

    @Override
    public @Nullable String getRoleName() {
        return FirebirdConnectionProperties.super.getRoleName();
    }

    @Override
    public void setRoleName(@Nullable String roleName) {
        FirebirdConnectionProperties.super.setRoleName(roleName);
    }

    @Override
    public @Nullable String getCharSet() {
        return FirebirdConnectionProperties.super.getCharSet();
    }

    @Override
    public void setCharSet(@Nullable String charSet) {
        FirebirdConnectionProperties.super.setCharSet(charSet);
    }

    @Override
    public @Nullable String getEncoding() {
        return FirebirdConnectionProperties.super.getEncoding();
    }

    @Override
    public void setEncoding(@Nullable String encoding) {
        FirebirdConnectionProperties.super.setEncoding(encoding);
    }

    @Override
    public @Nullable Integer getProcessId() {
        return FirebirdConnectionProperties.super.getProcessId();
    }

    @Override
    public void setProcessId(@Nullable Integer processId) {
        FirebirdConnectionProperties.super.setProcessId(processId);
    }

    @Override
    public @Nullable String getProcessName() {
        return FirebirdConnectionProperties.super.getProcessName();
    }

    @Override
    public void setProcessName(@Nullable String processName) {
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
    public int getLoginTimeout() {
        return getConnectTimeout();
    }

    /**
     * {@inheritDoc}
     * <p>
     * This property is an alias for the connectTimeout property.
     * </p>
     */
    @Override
    public void setLoginTimeout(int seconds) {
        setConnectTimeout(seconds);
    }

    @Override
    public String getWireCrypt() {
        return FirebirdConnectionProperties.super.getWireCrypt();
    }

    @Override
    public void setWireCrypt(@Nullable String wireCrypt) {
        FirebirdConnectionProperties.super.setWireCrypt(wireCrypt);
    }

    @Override
    public @Nullable String getDbCryptConfig() {
        return FirebirdConnectionProperties.super.getDbCryptConfig();
    }

    @Override
    public void setDbCryptConfig(@Nullable String dbCryptConfig) {
        FirebirdConnectionProperties.super.setDbCryptConfig(dbCryptConfig);
    }

    @Override
    public String getAuthPlugins() {
        return FirebirdConnectionProperties.super.getAuthPlugins();
    }

    @Override
    public void setAuthPlugins(@Nullable String authPlugins) {
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
    public @Nullable String getEnableProtocol() {
        return FirebirdConnectionProperties.super.getEnableProtocol();
    }

    @Override
    public void setEnableProtocol(@Nullable String enableProtocol) {
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
    public @Nullable String getDataTypeBind() {
        return FirebirdConnectionProperties.super.getDataTypeBind();
    }

    @Override
    public void setDataTypeBind(@Nullable String dataTypeBind) {
        FirebirdConnectionProperties.super.setDataTypeBind(dataTypeBind);
    }

    @Override
    public @Nullable String getSessionTimeZone() {
        return FirebirdConnectionProperties.super.getSessionTimeZone();
    }

    @Override
    public void setSessionTimeZone(@Nullable String sessionTimeZone) {
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
    public @Nullable String getGeneratedKeysEnabled() {
        return FirebirdConnectionProperties.super.getGeneratedKeysEnabled();
    }

    @Override
    public void setGeneratedKeysEnabled(@Nullable String generatedKeysEnabled) {
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
    public @Nullable String getDecfloatRound() {
        return FirebirdConnectionProperties.super.getDecfloatRound();
    }

    @Override
    public void setDecfloatRound(@Nullable String decfloatRound) {
        FirebirdConnectionProperties.super.setDecfloatRound(decfloatRound);
    }

    @Override
    public @Nullable String getDecfloatTraps() {
        return FirebirdConnectionProperties.super.getDecfloatTraps();
    }

    @Override
    public void setDecfloatTraps(@Nullable String decfloatTraps) {
        FirebirdConnectionProperties.super.setDecfloatTraps(decfloatTraps);
    }

    @Override
    public @Nullable String getTpbMapping() {
        return FirebirdConnectionProperties.super.getTpbMapping();
    }

    @Override
    public void setTpbMapping(@Nullable String tpbMapping) {
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
    public void setDefaultIsolation(@Nullable String isolation) {
        FirebirdConnectionProperties.super.setDefaultIsolation(isolation);
    }

    @Override
    public String getScrollableCursor() {
        return FirebirdConnectionProperties.super.getScrollableCursor();
    }

    @Override
    public void setScrollableCursor(@Nullable String scrollableCursor) {
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
    public @Nullable String getSocketFactory() {
        return FirebirdConnectionProperties.super.getSocketFactory();
    }

    @Override
    public void setSocketFactory(@Nullable String socketFactory) {
        FirebirdConnectionProperties.super.setSocketFactory(socketFactory);
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

    @Override
    public String getReportSQLWarnings() {
        return FirebirdConnectionProperties.super.getReportSQLWarnings();
    }

    @Override
    public void setReportSQLWarnings(@Nullable String reportSQLWarnings) {
        FirebirdConnectionProperties.super.setReportSQLWarnings(reportSQLWarnings);
    }

    @Override
    public boolean isAsyncFetch() {
        return FirebirdConnectionProperties.super.isAsyncFetch();
    }

    @Override
    public void setAsyncFetch(boolean asyncFetch) {
        FirebirdConnectionProperties.super.setAsyncFetch(asyncFetch);
    }

    @Override
    public int getMaxInlineBlobSize() {
        return FirebirdConnectionProperties.super.getMaxInlineBlobSize();
    }

    @Override
    public void setMaxInlineBlobSize(int maxInlineBlobSize) {
        FirebirdConnectionProperties.super.setMaxInlineBlobSize(maxInlineBlobSize);
    }

    @Override
    public int getMaxBlobCacheSize() {
        return FirebirdConnectionProperties.super.getMaxBlobCacheSize();
    }

    @Override
    public void setMaxBlobCacheSize(int maxBlobCacheSize) {
        FirebirdConnectionProperties.super.setMaxBlobCacheSize(maxBlobCacheSize);
    }

    @Override
    public @Nullable String getSearchPath() {
        return FirebirdConnectionProperties.super.getSearchPath();
    }

    @Override
    public void setSearchPath(@Nullable String searchPath) {
        FirebirdConnectionProperties.super.setSearchPath(searchPath);
    }

    @SuppressWarnings("deprecation")
    @Deprecated(since = "5")
    @Override
    public @Nullable String getUserName() {
        return FirebirdConnectionProperties.super.getUserName();
    }

    @SuppressWarnings("deprecation")
    @Deprecated(since = "5")
    @Override
    public void setUserName(@Nullable String userName) {
        FirebirdConnectionProperties.super.setUserName(userName);
    }

}
