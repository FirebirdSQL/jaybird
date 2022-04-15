package org.firebirdsql.nativeoo.gds.ng;

import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.encodings.IEncodingFactory;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.impl.DbAttachInfo;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.IConnectionProperties;
import org.firebirdsql.jna.fbclient.FbClientLibrary;

import java.sql.SQLException;

/**
 * Class handling the initial setup of the native OO API database connection.
 *
 * @since 4.0
 */
public class NativeDatabaseConnection extends AbstractNativeConnection<IConnectionProperties, IDatabaseImpl> {

    /**
     * Creates a IDatabaseConnectionImpl (without establishing a connection to the server).
     *
     * @param clientLibrary
     *         Client library to use
     * @param connectionProperties
     *         Connection properties
     */
    public NativeDatabaseConnection(FbClientLibrary clientLibrary, IConnectionProperties connectionProperties)
            throws SQLException {
        this(clientLibrary, connectionProperties, EncodingFactory.getPlatformDefault());
    }

    /**
     * Creates a IDatabaseConnectionImpl (without establishing a connection to the server).
     *
     * @param clientLibrary    Client library to use
     * @param attachProperties Attach properties
     * @param encodingFactory
     */
    protected NativeDatabaseConnection(FbClientLibrary clientLibrary, IConnectionProperties attachProperties, IEncodingFactory encodingFactory) throws SQLException {
        super(clientLibrary, attachProperties, encodingFactory);
    }

    @Override
    protected String createAttachUrl(DbAttachInfo dbAttachInfo, IConnectionProperties connectionProperties)
            throws SQLException {
        if (!dbAttachInfo.hasAttachObjectName()) {
            throw new FbExceptionBuilder()
                    .nonTransientConnectionException(JaybirdErrorCodes.jb_invalidConnectionString)
                    // Using original attach object name as that may well be non-null even if it is null in dbAttachInfo
                    .messageParameter(connectionProperties.getAttachObjectName())
                    .messageParameter("null or empty database name in connection string")
                    .toFlatSQLException();
        }
        return toAttachUrl(dbAttachInfo);
    }

    @Override
    public IDatabaseImpl identify() throws SQLException {
        return new IDatabaseImpl(this);
    }
}
