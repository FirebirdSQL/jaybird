package org.firebirdsql.nativeoo.gds.ng;

import org.firebirdsql.encodings.IEncodingFactory;
import org.firebirdsql.gds.ng.AbstractConnection;
import org.firebirdsql.gds.ng.DatatypeCoder;
import org.firebirdsql.gds.ng.FbAttachment;
import org.firebirdsql.gds.ng.IAttachProperties;
import org.firebirdsql.gds.ng.jna.BigEndianDatatypeCoder;
import org.firebirdsql.gds.ng.jna.LittleEndianDatatypeCoder;
import org.firebirdsql.jna.fbclient.FbClientLibrary;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.nio.ByteOrder;
import java.sql.SQLException;

import static java.util.Objects.requireNonNull;

/**
 * Class handling the initial setup of the native connection.
 * That's using for native OO API.
 *
 * @param <T>
 *         Type of attach properties
 * @param <C>
 *         Type of connection handle
 * @since 4.0
 */
public abstract class AbstractNativeConnection <T extends IAttachProperties<T>, C extends FbAttachment>
        extends AbstractConnection<T, C> {
    private static final Logger log = LoggerFactory.getLogger(AbstractNativeConnection.class);
    private static final boolean bigEndian = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;

    private final FbClientLibrary clientLibrary;

    /**
     * Creates a AbstractNativeConnection (without establishing a connection to the server).
     *
     * @param clientLibrary
     *         Client library to use
     * @param attachProperties
     *         Attach properties
     * @param encodingFactory
     *         Encoding factory
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
