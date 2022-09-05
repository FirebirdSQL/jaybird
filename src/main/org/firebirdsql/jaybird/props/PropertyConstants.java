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
package org.firebirdsql.jaybird.props;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.jaybird.props.internal.TransactionNameMapping;
import org.firebirdsql.util.InternalApi;

import java.sql.Connection;

/**
 * Property constants.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
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

    static final boolean DEFAULT_STREAM_BLOBS = true;
    static final boolean DEFAULT_RESULT_SET_HOLDABLE = false;
    static final boolean DEFAULT_COLUMN_LABEL_FOR_NAME = false;
    static final boolean DEFAULT_USE_FIREBIRD_AUTO_COMMIT = false;
    static final boolean DEFAULT_IGNORE_PROCEDURE_TYPE = false;
    static final boolean DEFAULT_WIRE_COMPRESSION = false;
    static final int DEFAULT_BLOB_BUFFER_SIZE = 16384;
    public static final int DEFAULT_PAGE_CACHE_SIZE = 0;
    static final boolean DEFAULT_USE_SERVER_BATCH = true;
    public static final int DEFAULT_SERVER_BATCH_BUFFER_SIZE = 0;
    static final boolean DEFAULT_TIMESTAMP_USES_LOCAL = false;

    public static final int DEFAULT_TRANSACTION_ISOLATION_VALUE = Connection.TRANSACTION_READ_COMMITTED;
    public static final String DEFAULT_TRANSACTION_ISOLATION_NAME = TransactionNameMapping.TRANSACTION_READ_COMMITTED;

    public static final String SCROLLABLE_CURSOR_EMULATED = "EMULATED";
    public static final String SCROLLABLE_CURSOR_SERVER = "SERVER";
    public static final String DEFAULT_SCROLLABLE_CURSOR = SCROLLABLE_CURSOR_EMULATED;

    public static final int TIMEOUT_NOT_SET = -1;
    public static final int BUFFER_SIZE_NOT_SET = -1;

    public static final String SESSION_TIME_ZONE_SERVER = "server";

    public static final String DEFAULT_AUTH_PLUGINS = "Srp256,Srp";

    private PropertyConstants() {
        // no instances
    }
}
