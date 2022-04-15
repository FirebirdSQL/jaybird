package org.firebirdsql.nativeoo.gds.ng;

import com.sun.jna.Native;
import org.firebirdsql.gds.JaybirdSystemProperties;
import org.firebirdsql.jna.fbclient.FbClientLibrary;

/**
 * Implementation of {@link org.firebirdsql.nativeoo.gds.ng.AbstractNativeOODatabaseFactory} to connect with native
 * client library via OO API.
 *
 * @since 5.0
 */
public class FbOOClientDatabaseFactory extends AbstractNativeOODatabaseFactory {
    private static final FbOOClientDatabaseFactory INSTANCE = new FbOOClientDatabaseFactory();

    @Override
    protected FbClientLibrary getClientLibrary() {
        return FbOOClientDatabaseFactory.ClientHolder.clientLibrary;
    }

    public static FbOOClientDatabaseFactory getInstance() {
        return INSTANCE;
    }

    /**
     * Initialization-on-demand depending on classloading behavior specified in JLS 12.4
     */
    private static final class ClientHolder {

        private static final FbClientLibrary clientLibrary = syncWrapIfNecessary(initClientLibrary());

        private static FbClientLibrary initClientLibrary() {
            return Native.load("fbclient", FbInterface.class);
        }

        private static FbClientLibrary syncWrapIfNecessary(FbClientLibrary clientLibrary) {
            if (JaybirdSystemProperties.isSyncWrapNativeLibrary()) {
                return (FbClientLibrary) Native.synchronizedLibrary(clientLibrary);
            }
            return clientLibrary;
        }
    }
}
