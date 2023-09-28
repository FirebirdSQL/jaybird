package org.firebirdsql.gds.ng.nativeoo;

import com.sun.jna.Native;
import org.firebirdsql.gds.JaybirdSystemProperties;
import org.firebirdsql.gds.ng.IAttachProperties;
import org.firebirdsql.jna.fbclient.FbClientLibrary;
import org.firebirdsql.jna.fbclient.FbInterface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of {@link org.firebirdsql.gds.ng.nativeoo.AbstractNativeOODatabaseFactory} to connect with embedded
 * client library via OO API.
 *
 * @since 6.0
 */
public class FbOOEmbeddedDatabaseFactory extends AbstractNativeOODatabaseFactory {

    private static final System.Logger log = System.getLogger(FbOOEmbeddedDatabaseFactory.class.getName());
    private static final List<String> LIBRARIES_TO_TRY =
            List.of("fbembed", FbOOClientDatabaseFactory.LIBRARY_NAME_FBCLIENT);
    private static final FbOOEmbeddedDatabaseFactory INSTANCE = new FbOOEmbeddedDatabaseFactory();

    @Override
    protected FbClientLibrary getClientLibrary() {
        return ClientHolder.clientLibrary;
    }

    @Override
    protected <T extends IAttachProperties<T>> T filterProperties(T attachProperties) {
        T attachPropertiesCopy = attachProperties.asNewMutable();
        // Clear server name
        attachPropertiesCopy.setServerName(null);
        return attachPropertiesCopy;
    }

    public static FbOOEmbeddedDatabaseFactory getInstance() {
        return INSTANCE;
    }

    /**
     * Initialization-on-demand depending on classloading behavior specified in JLS 12.4
     */
    private static final class ClientHolder {

        // Note Firebird 3 embedded is fbclient + engine12
        private static final List<String> LIBRARIES_TO_TRY =
                Collections.unmodifiableList(Arrays.asList("fbembed", "fbclient"));

        private static final FbClientLibrary clientLibrary = syncWrapIfNecessary(initClientLibrary());

        private static FbClientLibrary initClientLibrary() {
            final List<Throwable> throwables = new ArrayList<>();
            final List<String> librariesToTry = findLibrariesToTry();
            for (String libraryName : LIBRARIES_TO_TRY) {
                try {
                    return Native.load(libraryName, FbInterface.class);
                } catch (UnsatisfiedLinkError e) {
                    throwables.add(e);
                    log.log(System.Logger.Level.DEBUG, "Attempt to load " + libraryName + " failed", e);
                    // continue with next
                }
            }
            assert throwables.size() == librariesToTry.size();
            log.log(System.Logger.Level.ERROR, "Could not load any of the libraries in " + librariesToTry + ":");
            for (int idx = 0; idx < librariesToTry.size(); idx++) {
                log.log(System.Logger.Level.ERROR, "Loading " + librariesToTry.get(idx) + " failed", throwables.get(idx));
            }
            throw new ExceptionInInitializerError(throwables.get(0));
        }

        private static FbClientLibrary syncWrapIfNecessary(FbClientLibrary clientLibrary) {
            if (JaybirdSystemProperties.isSyncWrapNativeLibrary()) {
                return (FbClientLibrary) Native.synchronizedLibrary(clientLibrary);
            }
            return clientLibrary;
        }

        private static List<String> findLibrariesToTry() {
            return LIBRARIES_TO_TRY;
        }
    }
}
