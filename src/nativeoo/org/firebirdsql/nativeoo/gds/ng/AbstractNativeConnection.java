package org.firebirdsql.nativeoo.gds.ng;

import com.sun.jna.Pointer;
import org.firebirdsql.encodings.DefaultEncodingDefinition;
import org.firebirdsql.encodings.IEncodingFactory;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.*;
import org.firebirdsql.gds.ng.jna.BigEndianDatatypeCoder;
import org.firebirdsql.gds.ng.jna.LittleEndianDatatypeCoder;
import org.firebirdsql.jna.fbclient.FbClientLibrary;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;
import org.firebirdsql.nativeoo.gds.ng.FbInterface.IStatus;

import java.nio.ByteOrder;
import java.sql.SQLException;

import static java.util.Objects.requireNonNull;
import static org.firebirdsql.gds.ISCConstants.*;
import static org.firebirdsql.gds.ISCConstants.isc_arg_end;

/**
 * Class handling the initial setup of the native connection.
 * That's using for native OO API.
 *
 * @param <T> Type of attach properties
 * @param <C> Type of connection handle
 * @since 4.0
 */
public abstract class AbstractNativeConnection<T extends IAttachProperties<T>, C extends FbAttachment>
        extends AbstractConnection<T, C> {
    private static final Logger log = LoggerFactory.getLogger(AbstractNativeConnection.class);
    private static final boolean bigEndian = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;

    private final FbClientLibrary clientLibrary;

    /**
     * Creates a AbstractNativeConnection (without establishing a connection to the server).
     *
     * @param clientLibrary    Client library to use
     * @param attachProperties Attach properties
     * @param encodingFactory  Encoding factory
     */
    protected AbstractNativeConnection(FbClientLibrary clientLibrary, T attachProperties, IEncodingFactory encodingFactory)
            throws SQLException {
        super(attachProperties, encodingFactory);
        this.clientLibrary = requireNonNull(clientLibrary, "parameter clientLibrary cannot be null");
    }

    /**
     * @return The client library instance associated with the connection.
     */
    public final FbClientLibrary getClientLibrary() {
        return clientLibrary;
    }

    /**
     * Processing {@link IStatus} to get result of native calling
     */
    protected void processStatus(IStatus status, WarningMessageCallback messageCallback) throws SQLException {
        Pointer pointer = status.getErrors();
        long[] statusVector = pointer.getLongArray(0, 20);

        boolean debug = log.isDebugEnabled();
        final FbExceptionBuilder builder = new FbExceptionBuilder();
        int vectorIndex = 0;
        processingLoop:
        while (vectorIndex < statusVector.length) {
            int arg = (int) statusVector[vectorIndex++];
            int errorCode;
            switch (arg) {
                case isc_arg_gds:
                    errorCode = (int) statusVector[vectorIndex++];
                    if (debug) log.debug("readStatusVector arg:isc_arg_gds int: " + errorCode);
                    if (errorCode != 0) {
                        builder.exception(errorCode);
                    }
                    break;
                case isc_arg_warning:
                    errorCode = (int) statusVector[vectorIndex++];
                    if (debug) log.debug("readStatusVector arg:isc_arg_warning int: " + errorCode);
                    if (errorCode != 0) {
                        builder.warning(errorCode);
                    }
                    break;
                case isc_arg_interpreted:
                case isc_arg_string:
                case isc_arg_sql_state:
                    long stringPointerAddress = statusVector[vectorIndex++];
                    if (stringPointerAddress == 0L) {
                        log.warn("Received NULL pointer address for isc_arg_interpreted, isc_arg_string or isc_arg_sql_state");
                        break processingLoop;
                    }
                    Pointer stringPointer = new Pointer(stringPointerAddress);
                    String stringValue = stringPointer.getString(0, getEncodingDefinition().getJavaEncodingName());
                    if (arg != isc_arg_sql_state) {
                        if (debug) log.debug("readStatusVector string: " + stringValue);
                        builder.messageParameter(stringValue);
                    } else {
                        if (debug) log.debug("readStatusVector sqlstate: " + stringValue);
                        builder.sqlState(stringValue);
                    }
                    break;
                case isc_arg_cstring:
                    int stringLength = (int) statusVector[vectorIndex++];
                    long cStringPointerAddress = statusVector[vectorIndex++];
                    Pointer cStringPointer = new Pointer(cStringPointerAddress);
                    byte[] stringData = cStringPointer.getByteArray(0, stringLength);
                    String cStringValue = getEncoding().decodeFromCharset(stringData);
                    builder.messageParameter(cStringValue);
                    break;
                case isc_arg_number:
                    int intValue = (int) statusVector[vectorIndex++];
                    if (debug) log.debug("readStatusVector arg:isc_arg_number int: " + intValue);
                    builder.messageParameter(intValue);
                    break;
                case isc_arg_end:
                    break processingLoop;
                default:
                    int e = (int) statusVector[vectorIndex++];
                    if (debug) log.debug("readStatusVector arg: " + arg + " int: " + e);
                    builder.messageParameter(e);
                    break;
            }
        }

        if (!builder.isEmpty()) {
            SQLException exception = builder.toFlatSQLException();
            throw exception;
        }
    }

    public final DatatypeCoder createDatatypeCoder() {
        if (bigEndian) {
            return BigEndianDatatypeCoder.forEncodingFactory(getEncodingFactory());
        }
        return LittleEndianDatatypeCoder.forEncodingFactory(getEncodingFactory());
    }

    /**
     * Builds the attach URL for the library.
     *
     * @return Attach URL
     */
    public String getAttachUrl() {
        StringBuilder sb = new StringBuilder();
        if (getServerName() != null) {
            boolean ipv6 = getServerName().indexOf(':') != -1;
            if (ipv6) {
                sb.append('[').append(getServerName()).append(']');
            } else {
                sb.append(getServerName());
            }
            sb.append('/')
                    .append(getPortNumber())
                    .append(':');
        }
        sb.append(getAttachObjectName());
        return sb.toString();
    }
}
