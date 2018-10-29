package org.firebirdsql.nativeoo.gds.ng;

import com.sun.jna.Native;
import org.firebirdsql.gds.JaybirdSystemProperties;
import org.firebirdsql.gds.ng.*;
import org.firebirdsql.jna.fbclient.FbClientLibrary;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FbOOEmbeddedDatabaseFactory extends AbstractNativeOODatabaseFactory {

    private static final Logger log = LoggerFactory.getLogger(FbOOEmbeddedDatabaseFactory.class);
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
            for (String libraryName : LIBRARIES_TO_TRY) {
                try {
                    return Native.loadLibrary(libraryName, FbClientLibrary.class);
                } catch (UnsatisfiedLinkError e) {
                    throwables.add(e);
                    log.debug("Attempt to load " + libraryName + " failed", e);
                    // continue with next
                }
            }
            assert throwables.size() == LIBRARIES_TO_TRY.size();
            log.error("Could not load any of the libraries in " + LIBRARIES_TO_TRY + ":");
            for (int idx = 0; idx < LIBRARIES_TO_TRY.size(); idx++) {
                log.error("Loading " + LIBRARIES_TO_TRY.get(idx) + " failed", throwables.get(idx));
            }
            throw new ExceptionInInitializerError(throwables.get(0));
        }

        private static FbClientLibrary syncWrapIfNecessary(FbClientLibrary clientLibrary) {
            if (JaybirdSystemProperties.isSyncWrapNativeLibrary()) {
                return (FbClientLibrary) Native.synchronizedLibrary(clientLibrary);
            }
            return clientLibrary;
        }
    }
}