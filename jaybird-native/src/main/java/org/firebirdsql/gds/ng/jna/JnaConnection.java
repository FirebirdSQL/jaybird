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
package org.firebirdsql.gds.ng.jna;

import com.sun.jna.Pointer;
import org.firebirdsql.encodings.IEncodingFactory;
import org.firebirdsql.gds.impl.DbAttachInfo;
import org.firebirdsql.gds.ng.*;
import org.firebirdsql.jna.fbclient.FbClientLibrary;
import org.firebirdsql.jna.fbclient.ISC_STATUS;

import java.nio.ByteOrder;
import java.sql.SQLException;
import java.sql.SQLWarning;

import static java.lang.System.Logger.Level.WARNING;
import static java.util.Objects.requireNonNull;
import static org.firebirdsql.gds.ISCConstants.*;

/**
 * Class handling the initial setup of the JNA connection.
 *
 * @param <T>
 *         Type of attach properties
 * @param <C>
 *         Type of connection handle
 * @author Mark Rotteveel
 * @since 3.0
 */
public abstract class JnaConnection<T extends IAttachProperties<T>, C extends JnaAttachment>
        extends AbstractConnection<T, C> {

    private static final boolean BIG_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;

    private final FbClientLibrary clientLibrary;
    private final String attachUrl;

    /**
     * Creates a JnaConnection (without establishing a connection to the server).
     *
     * @param clientLibrary
     *         Client library to use
     * @param attachProperties
     *         Attach properties
     * @param encodingFactory
     *         Encoding factory
     */
    protected JnaConnection(FbClientLibrary clientLibrary, T attachProperties, IEncodingFactory encodingFactory)
            throws SQLException {
        super(attachProperties, encodingFactory);
        this.clientLibrary = requireNonNull(clientLibrary, "parameter clientLibrary cannot be null");
        this.attachUrl = createAttachUrl(toDbAttachInfo(attachProperties), attachProperties);
    }

    private DbAttachInfo toDbAttachInfo(T attachProperties) throws SQLException {
        DbAttachInfo initialDbAttachInfo = DbAttachInfo.of(attachProperties);

        if (!initialDbAttachInfo.hasServerName() && initialDbAttachInfo.hasAttachObjectName()
                && initialDbAttachInfo.attachObjectName().startsWith("//")) {
            // This is a connection string using the default URL format which is not directly supported by fbclient
            return DbAttachInfo.parseConnectString(initialDbAttachInfo.attachObjectName());
        }

        return initialDbAttachInfo;
    }

    protected abstract String createAttachUrl(DbAttachInfo dbAttachInfo, T attachProperties) throws SQLException;

    /**
     * @return The client library instance associated with the connection.
     */
    public final FbClientLibrary getClientLibrary() {
        return clientLibrary;
    }

    protected void processStatusVector(ISC_STATUS[] statusVector, WarningMessageCallback warningMessageCallback)
            throws SQLException {
        requireNonNull(warningMessageCallback, "warningMessageCallback");
        final var builder = new FbExceptionBuilder();
        populateExceptionBuilder(statusVector, builder);

        if (builder.isEmpty()) return;
        SQLException exception = builder.toFlatSQLException();
        if (exception instanceof SQLWarning warning) {
            warningMessageCallback.processWarning(warning);
        } else {
            throw exception;
        }
    }

    private void populateExceptionBuilder(ISC_STATUS[] statusVector, FbExceptionBuilder builder) {
        int vectorIndex = 0;
        while (vectorIndex < statusVector.length) {
            switch (statusVector[vectorIndex++].intValue()) {
            case isc_arg_gds -> {
                int errorCode = statusVector[vectorIndex++].intValue();
                if (errorCode != 0) {
                    builder.exception(errorCode);
                }
            }
            case isc_arg_warning -> {
                int errorCode = statusVector[vectorIndex++].intValue();
                if (errorCode != 0) {
                    builder.warning(errorCode);
                }
            }
            case isc_arg_interpreted, isc_arg_string -> {
                String stringValue = getString(statusVector[vectorIndex++]);
                if (stringValue == null) return;
                builder.messageParameter(stringValue);
            }
            case isc_arg_sql_state -> {
                String stringValue = getString(statusVector[vectorIndex++]);
                if (stringValue == null) return;
                builder.sqlState(stringValue);
            }
            case isc_arg_cstring ->
                    builder.messageParameter(getCString(statusVector[vectorIndex++], statusVector[vectorIndex++]));
            case isc_arg_end -> {
                return;
            }
            default -> builder.messageParameter(statusVector[vectorIndex++].intValue());
            }
        }
    }

    private String getCString(ISC_STATUS lengthStatus, ISC_STATUS pointerStatus) {
        var cStringPointer = new Pointer(pointerStatus.longValue());
        byte[] stringData = cStringPointer.getByteArray(0, lengthStatus.intValue());
        return getEncoding().decodeFromCharset(stringData);
    }

    private String getString(ISC_STATUS iscStatus) {
        long stringPointerAddress = iscStatus.longValue();
        if (stringPointerAddress == 0L) {
            System.getLogger(getClass().getName()).log(WARNING,
                    "Received NULL pointer address for isc_arg_interpreted, isc_arg_string or isc_arg_sql_state");
            return null;
        }
        var stringPointer = new Pointer(stringPointerAddress);
        return stringPointer.getString(0, getEncodingDefinition().getJavaEncodingName());
    }

    final DatatypeCoder createDatatypeCoder() {
        if (BIG_ENDIAN) {
            return BigEndianDatatypeCoder.forEncodingFactory(getEncodingFactory());
        }
        return LittleEndianDatatypeCoder.forEncodingFactory(getEncodingFactory());
    }

    /**
     * Gets the attach URL for the library.
     *
     * @return Attach URL
     */
    public String getAttachUrl() {
        return attachUrl;
    }

    /**
     * Builds the attach URL for the library.
     *
     * @return Attach URL
     */
    protected static String toAttachUrl(DbAttachInfo dbAttachInfo) {
        if (!dbAttachInfo.hasServerName()) {
            return dbAttachInfo.attachObjectName();
        }
        String serverName = dbAttachInfo.serverName();
        String attachObjectName = dbAttachInfo.attachObjectName();
        StringBuilder sb = new StringBuilder(serverName.length() + attachObjectName.length() + 4);
        boolean ipv6 = serverName.indexOf(':') != -1;
        if (ipv6) {
            sb.append('[').append(serverName).append(']');
        } else {
            sb.append(serverName);
        }
        sb.append('/').append(dbAttachInfo.portNumber())
                .append(':').append(attachObjectName);
        return sb.toString();
    }
}
