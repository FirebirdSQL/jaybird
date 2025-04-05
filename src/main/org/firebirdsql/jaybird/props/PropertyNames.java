// SPDX-FileCopyrightText: Copyright 2020-2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.jaybird.props;

/**
 * Property names and aliases used by Jaybird.
 * <p>
 * In defiance of normal style rules for Java, the constants defined in this class use the same name as their value
 * (if syntactically valid).
 * </p>
 *
 * @author Mark Rotteveel
 * @since 5
 */
@SuppressWarnings("java:S115")
public final class PropertyNames {

    // attachment
    public static final String serverName = "serverName";
    public static final String portNumber = "portNumber";
    public static final String attachObjectName = "attachObjectName";
    // NOTE: alias for attachObjectName
    public static final String databaseName = "databaseName";
    // NOTE: alias for attachObjectName
    public static final String serviceName = "serviceName";
    public static final String type = "type";
    public static final String user = "user";
    public static final String password = "password";
    public static final String roleName = "roleName";
    public static final String processId = "processId";
    public static final String processName = "processName";
    public static final String charSet = "charSet";
    public static final String encoding = "encoding";
    public static final String socketBufferSize = "socketBufferSize";
    public static final String soTimeout = "soTimeout";
    public static final String connectTimeout = "connectTimeout";
    public static final String wireCrypt = "wireCrypt";
    public static final String dbCryptConfig = "dbCryptConfig";
    public static final String authPlugins = "authPlugins";
    public static final String wireCompression = "wireCompression";
    public static final String enableProtocol = "enableProtocol";
    public static final String parallelWorkers = "parallelWorkers";
    public static final String socketFactory = "socketFactory";

    // database connection
    public static final String sqlDialect = "sqlDialect";
    public static final String blobBufferSize = "blobBufferSize";
    public static final String useStreamBlobs = "useStreamBlobs";
    public static final String pageCacheSize = "pageCacheSize";
    public static final String defaultResultSetHoldable = "defaultResultSetHoldable";
    public static final String useFirebirdAutocommit = "useFirebirdAutocommit";
    public static final String generatedKeysEnabled = "generatedKeysEnabled";
    public static final String dataTypeBind = "dataTypeBind";
    public static final String sessionTimeZone = "sessionTimeZone";
    public static final String ignoreProcedureType = "ignoreProcedureType";
    public static final String columnLabelForName = "columnLabelForName";
    public static final String decfloatRound = "decfloatRound";
    public static final String decfloatTraps = "decfloatTraps";
    public static final String tpbMapping = "tpbMapping";
    public static final String defaultIsolation = "defaultIsolation";
    public static final String scrollableCursor = "scrollableCursor";
    public static final String useServerBatch = "useServerBatch";
    public static final String serverBatchBufferSize = "serverBatchBufferSize";
    public static final String useCatalogAsPackage = "useCatalogAsPackage";
    public static final String allowTxStmts = "allowTxStmts";
    public static final String extendedMetadata = "extendedMetadata";
    public static final String createDatabaseIfNotExist = "createDatabaseIfNotExist";
    public static final String reportSQLWarnings = "reportSQLWarnings";
    public static final String asyncFetch = "asyncFetch";
    public static final String maxInlineBlobSize = "maxInlineBlobSize";
    public static final String maxBlobCacheSize = "maxBlobCacheSize";

    // service connection
    public static final String expectedDb = "expectedDb";

    private PropertyNames() {
        // no instances
    }

}
