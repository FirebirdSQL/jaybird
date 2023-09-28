package org.firebirdsql.gds.ng.nativeoo;

import com.sun.jna.Pointer;
import org.firebirdsql.encodings.IEncodingFactory;
import org.firebirdsql.gds.impl.DbAttachInfo;
import org.firebirdsql.gds.ng.AbstractConnection;
import org.firebirdsql.gds.ng.DatatypeCoder;
import org.firebirdsql.gds.ng.FbAttachment;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.IAttachProperties;
import org.firebirdsql.gds.ng.WarningMessageCallback;
import org.firebirdsql.gds.ng.jna.BigEndianDatatypeCoder;
import org.firebirdsql.gds.ng.jna.LittleEndianDatatypeCoder;
import org.firebirdsql.jna.fbclient.FbClientLibrary;
import org.firebirdsql.jna.fbclient.FbInterface.IStatus;

import java.nio.ByteOrder;
import java.sql.SQLException;
import java.sql.SQLWarning;

import static java.util.Objects.requireNonNull;
import static org.firebirdsql.gds.ISCConstants.isc_arg_cstring;
import static org.firebirdsql.gds.ISCConstants.isc_arg_end;
import static org.firebirdsql.gds.ISCConstants.isc_arg_gds;
import static org.firebirdsql.gds.ISCConstants.isc_arg_interpreted;
import static org.firebirdsql.gds.ISCConstants.isc_arg_number;
import static org.firebirdsql.gds.ISCConstants.isc_arg_sql_state;
import static org.firebirdsql.gds.ISCConstants.isc_arg_string;
import static org.firebirdsql.gds.ISCConstants.isc_arg_warning;

/**
 * Class handling the initial setup of the native connection.
 * That's using for native OO API.
 *
 * @param <T> Type of attach properties
 * @param <C> Type of connection handle
 * @since 6.0
 */
public abstract class AbstractNativeConnection<T extends IAttachProperties<T>, C extends FbAttachment>
        extends AbstractConnection<T, C> {
    private static final System.Logger log = System.getLogger(AbstractNativeConnection.class.getName());
    private static final boolean bigEndian = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;

    private final FbClientLibrary clientLibrary;
    private final String attachUrl;

    /**
     * Creates a AbstractNativeConnection (without establishing a connection to the server).
     *
     * @param clientLibrary    Client library to use
     * @param attachProperties Attach properties
     * @param encodingFactory  Encoding factory
     */
    protected AbstractNativeConnection(FbClientLibrary clientLibrary, T attachProperties,
                                       IEncodingFactory encodingFactory)
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

    /**
     * Processing {@link IStatus} to get result of native calling
     */
    protected void processStatus(IStatus status, WarningMessageCallback warningMessageCallback)
            throws SQLException {
        if (warningMessageCallback == null) {
            throw new NullPointerException("warningMessageCallback is null");
        }

        final FbExceptionBuilder builder = new FbExceptionBuilder();

        if (status.getState() == IStatus.STATE_WARNINGS) {
            final long[] warningVector = status.getWarnings().getLongArray(0, 20);
            processVector(warningVector, builder);
        }

        if (status.getState() == IStatus.STATE_ERRORS) {
            final long[] errorVector = status.getErrors().getLongArray(0, 20);
            processVector(errorVector, builder);
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

    private void processVector(long[] errorVector, FbExceptionBuilder builder) {
        int vectorIndex = 0;
        processingLoop:
        while (vectorIndex < errorVector.length) {
            int arg = (int) errorVector[vectorIndex++];
            int errorCode;
            switch (arg) {
                case isc_arg_gds:
                    errorCode = (int) errorVector[vectorIndex++];
                    log.log(System.Logger.Level.DEBUG, "readStatusVector arg:isc_arg_gds int: " + errorCode);
                    if (errorCode != 0) {
                        builder.exception(errorCode);
                    }
                    break;
                case isc_arg_warning:
                    errorCode = (int) errorVector[vectorIndex++];
                    log.log(System.Logger.Level.DEBUG, "readStatusVector arg:isc_arg_warning int: " + errorCode);
                    if (errorCode != 0) {
                        builder.warning(errorCode);
                    }
                    break;
                case isc_arg_interpreted:
                case isc_arg_string:
                case isc_arg_sql_state:
                    long stringPointerAddress = errorVector[vectorIndex++];
                    if (stringPointerAddress == 0L) {
                        log.log(System.Logger.Level.WARNING, "Received NULL pointer address for isc_arg_interpreted, isc_arg_string or " +
                                "isc_arg_sql_state");
                        break processingLoop;
                    }
                    Pointer stringPointer = new Pointer(stringPointerAddress);
                    String stringValue = stringPointer.getString(0,
                            getEncodingDefinition().getJavaEncodingName());
                    if (arg != isc_arg_sql_state) {
                        log.log(System.Logger.Level.DEBUG, "readStatusVector string: " + stringValue);
                        builder.messageParameter(stringValue);
                    } else {
                        log.log(System.Logger.Level.DEBUG, "readStatusVector sqlstate: " + stringValue);
                        builder.sqlState(stringValue);
                    }
                    break;
                case isc_arg_cstring:
                    int stringLength = (int) errorVector[vectorIndex++];
                    long cStringPointerAddress = errorVector[vectorIndex++];
                    Pointer cStringPointer = new Pointer(cStringPointerAddress);
                    byte[] stringData = cStringPointer.getByteArray(0, stringLength);
                    String cStringValue = getEncoding().decodeFromCharset(stringData);
                    builder.messageParameter(cStringValue);
                    break;
                case isc_arg_number:
                    int intValue = (int) errorVector[vectorIndex++];
                    log.log(System.Logger.Level.DEBUG, "readStatusVector arg:isc_arg_number int: " + intValue);
                    builder.messageParameter(intValue);
                    break;
                case isc_arg_end:
                    break processingLoop;
                default:
                    int e = (int) errorVector[vectorIndex++];
                    log.log(System.Logger.Level.DEBUG, "readStatusVector arg: " + arg + " int: " + e);
                    builder.messageParameter(e);
                    break;
            }
        }

    }

    public final DatatypeCoder createDatatypeCoder() {
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
