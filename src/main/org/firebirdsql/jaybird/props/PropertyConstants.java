// SPDX-FileCopyrightText: Copyright 2021-2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.props;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.jaybird.props.internal.TransactionNameMapping;
import org.firebirdsql.util.InternalApi;

import java.sql.Connection;

/**
 * Property constants.
 *
 * @author Mark Rotteveel
 * @since 5
 */
@InternalApi
public final class PropertyConstants {

    public static final String DEFAULT_SERVER_NAME = "localhost";
    public static final int DEFAULT_PORT = 3050;
    public static final String DEFAULT_SERVICE_NAME = "service_mgr";

    // Duplicates the names of the WireCrypt enum values
    public static final String WIRE_CRYPT_DEFAULT = "DEFAULT";
    public static final String WIRE_CRYPT_REQUIRED = "REQUIRED";
    public static final String WIRE_CRYPT_ENABLED = "ENABLED";
    public static final String WIRE_CRYPT_DISABLED = "DISABLED";

    public static final int DEFAULT_DIALECT = ISCConstants.SQL_DIALECT_V6;

    public static final boolean DEFAULT_STREAM_BLOBS = true;
    static final boolean DEFAULT_RESULT_SET_HOLDABLE = false;
    static final boolean DEFAULT_COLUMN_LABEL_FOR_NAME = false;
    static final boolean DEFAULT_USE_FIREBIRD_AUTO_COMMIT = false;
    static final boolean DEFAULT_IGNORE_PROCEDURE_TYPE = false;
    static final boolean DEFAULT_WIRE_COMPRESSION = false;
    public static final int DEFAULT_BLOB_BUFFER_SIZE = 16384;
    static final int MIN_BLOB_BUFFER_SIZE = 512;
    public static final int DEFAULT_PAGE_CACHE_SIZE = 0;
    static final boolean DEFAULT_USE_SERVER_BATCH = true;
    public static final int DEFAULT_SERVER_BATCH_BUFFER_SIZE = 0;
    static final boolean DEFAULT_USE_CATALOG_AS_PACKAGE = false;
    static final boolean DEFAULT_ALLOW_TX_STMTS = false;
    static final boolean DEFAULT_EXTENDED_METADATA = true;
    static final boolean DEFAULT_CREATE_DATABASE_IF_NOT_EXIST = false;

    public static final int DEFAULT_TRANSACTION_ISOLATION_VALUE = Connection.TRANSACTION_READ_COMMITTED;
    public static final String DEFAULT_TRANSACTION_ISOLATION_NAME = TransactionNameMapping.TRANSACTION_READ_COMMITTED;

    public static final String SCROLLABLE_CURSOR_EMULATED = "EMULATED";
    public static final String SCROLLABLE_CURSOR_SERVER = "SERVER";
    public static final String DEFAULT_SCROLLABLE_CURSOR = SCROLLABLE_CURSOR_EMULATED;

    public static final String REPORT_SQL_WARNINGS_ALL = "ALL";
    public static final String REPORT_SQL_WARNINGS_NONE = "NONE";
    public static final String DEFAULT_REPORT_SQL_WARNINGS = REPORT_SQL_WARNINGS_ALL;

    public static final boolean DEFAULT_ASYNC_FETCH = true;

    public static final int DEFAULT_MAX_INLINE_BLOB_SIZE = 64 * 1024;
    public static final int DEFAULT_MAX_BLOB_CACHE_SIZE = 10 * 1024 * 1024;

    public static final int TIMEOUT_NOT_SET = -1;
    public static final int BUFFER_SIZE_NOT_SET = -1;
    static final int PARALLEL_WORKERS_NOT_SET = -1;

    public static final String SESSION_TIME_ZONE_SERVER = "server";

    public static final String DEFAULT_AUTH_PLUGINS = "Srp256,Srp";

    private PropertyConstants() {
        // no instances
    }

}
