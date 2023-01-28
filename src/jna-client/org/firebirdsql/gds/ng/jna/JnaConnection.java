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
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.nio.ByteOrder;
import java.sql.SQLException;
import java.sql.SQLWarning;

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

    private static final Logger log = LoggerFactory.getLogger(JnaConnection.class);
    private static final boolean bigEndian = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;

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
        if (warningMessageCallback == null) {
            throw new NullPointerException("warningMessageCallback is null");
        }
        boolean debug = log.isDebugEnabled();
        final FbExceptionBuilder builder = new FbExceptionBuilder();
        int vectorIndex = 0;
        processingLoop:
        while (vectorIndex < statusVector.length) {
            int arg = statusVector[vectorIndex++].intValue();
            int errorCode;
            switch (arg) {
            case isc_arg_gds:
                errorCode = statusVector[vectorIndex++].intValue();
                if (debug) log.debugf("readStatusVector arg:isc_arg_gds int: %d", errorCode);
                if (errorCode != 0) {
                    builder.exception(errorCode);
                }
                break;
            case isc_arg_warning:
                errorCode = statusVector[vectorIndex++].intValue();
                if (debug) log.debugf("readStatusVector arg:isc_arg_warning int: %d", errorCode);
                if (errorCode != 0) {
                    builder.warning(errorCode);
                }
                break;
            case isc_arg_interpreted:
            case isc_arg_string:
            case isc_arg_sql_state:
                long stringPointerAddress = statusVector[vectorIndex++].longValue();
                if (stringPointerAddress == 0L) {
                    log.warn("Received NULL pointer address for isc_arg_interpreted, isc_arg_string or isc_arg_sql_state");
                    break processingLoop;
                }
                Pointer stringPointer = new Pointer(stringPointerAddress);
                String stringValue = stringPointer.getString(0, getEncodingDefinition().getJavaEncodingName());
                if (arg != isc_arg_sql_state) {
                    log.debugf("readStatusVector string: %s", stringValue);
                    builder.messageParameter(stringValue);
                } else {
                    log.debugf("readStatusVector sqlstate: %s", stringValue);
                    builder.sqlState(stringValue);
                }
                break;
            case isc_arg_cstring:
                int stringLength = statusVector[vectorIndex++].intValue();
                long cStringPointerAddress = statusVector[vectorIndex++].longValue();
                Pointer cStringPointer = new Pointer(cStringPointerAddress);
                byte[] stringData = cStringPointer.getByteArray(0, stringLength);
                String cStringValue = getEncoding().decodeFromCharset(stringData);
                builder.messageParameter(cStringValue);
                break;
            case isc_arg_number:
                int intValue = statusVector[vectorIndex++].intValue();
                if (debug) log.debugf("readStatusVector arg:isc_arg_number int: %d", intValue);
                builder.messageParameter(intValue);
                break;
            case isc_arg_end:
                break processingLoop;
            default:
                int e = statusVector[vectorIndex++].intValue();
                if (debug) log.debugf("readStatusVector arg: %d int: %d", arg, e);
                builder.messageParameter(e);
                break;
            }
        }

        if (!builder.isEmpty()) {
            SQLException exception = builder.toFlatSQLException();
            if (exception instanceof SQLWarning) {
                warningMessageCallback.processWarning((SQLWarning) exception);
            } else {
                throw exception;
            }
        }
    }

    final DatatypeCoder createDatatypeCoder() {
        if (bigEndian) {
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
