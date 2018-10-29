package org.firebirdsql.nativeoo.gds.ng;

import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.encodings.IEncodingFactory;
import org.firebirdsql.gds.ng.IServiceProperties;
import org.firebirdsql.jna.fbclient.FbClientLibrary;

import java.sql.SQLException;

/**
 * Class handling the initial setup of the native service connection.
 *
 * @author <a href="mailto:vasiliy.yashkov@red-soft.ru">Vasiliy Yashkov</a>
 * @since 4.0
 */
public class IServiceConnectionImpl extends AbstractNativeConnection<IServiceProperties, IServiceImpl> {
    /**
     * Creates a IServiceConnectionImpl (without establishing a connection to the server).
     *
     * @param clientLibrary
     *         Client library to use
     * @param connectionProperties
     *         Connection properties
     */
    public IServiceConnectionImpl(FbClientLibrary clientLibrary, IServiceProperties connectionProperties)
            throws SQLException {
        this(clientLibrary, connectionProperties, EncodingFactory.getPlatformDefault());
    }

    /**
     * Creates a IServiceConnectionImpl (without establishing a connection to the server).
     *
     * @param clientLibrary
     *         Client library to use
     * @param connectionProperties
     *         Connection properties
     * @param encodingFactory
     *         Factory for encoding definitions
     */
    public IServiceConnectionImpl(FbClientLibrary clientLibrary, IServiceProperties connectionProperties,
                                  IEncodingFactory encodingFactory) throws SQLException {
        super(clientLibrary, connectionProperties, encodingFactory);
    }

    /**
     * Contrary to the description in the super class, this will simply return an unconnected instance.
     *
     * @return FbDatabase instance
     * @throws SQLException
     */
    @Override
    public IServiceImpl identify() throws SQLException {
        return new IServiceImpl(this);
    }
}
