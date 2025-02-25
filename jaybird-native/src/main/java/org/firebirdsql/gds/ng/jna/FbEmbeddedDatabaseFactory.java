// SPDX-FileCopyrightText: Copyright 2016-2024 Mark Rotteveel
// SPDX-FileCopyrightText: Copyright 2016 Adriano dos Santos Fernandes
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.jna;

import com.sun.jna.Native;
import com.sun.jna.Platform;
import org.firebirdsql.gds.ng.IAttachProperties;
import org.firebirdsql.jaybird.util.Cleaners;
import org.firebirdsql.jna.embedded.FirebirdEmbeddedLookup;
import org.firebirdsql.jna.embedded.spi.DisposableFirebirdEmbeddedLibrary;
import org.firebirdsql.jna.embedded.spi.FirebirdEmbeddedLibrary;
import org.firebirdsql.jna.fbclient.FbClientLibrary;
import org.firebirdsql.jna.fbclient.WinFbClientLibrary;

import java.lang.ref.Cleaner;
import java.nio.file.Path;
import java.util.*;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;
import static java.util.Objects.requireNonNull;

/**
 * Implementation of {@link org.firebirdsql.gds.ng.FbDatabaseFactory} for establishing connection using the
 * Firebird embedded library.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public final class FbEmbeddedDatabaseFactory extends AbstractNativeDatabaseFactory {

    private static final System.Logger log = System.getLogger(FbEmbeddedDatabaseFactory.class.getName());
    // Note Firebird 3+ embedded is fbclient + engineNN (e.g. engine12 for Firebird 3.0 / ODS 12)
    private static final List<String> LIBRARIES_TO_TRY =
            List.of("fbembed", FbClientDatabaseFactory.LIBRARY_NAME_FBCLIENT);
    private static final FbEmbeddedDatabaseFactory INSTANCE = new FbEmbeddedDatabaseFactory();

    private FbEmbeddedDatabaseFactory() {
        // only through getInstance()
    }

    public static FbEmbeddedDatabaseFactory getInstance() {
        return INSTANCE;
    }

    @Override
    protected <T extends IAttachProperties<T>> T filterProperties(T attachProperties) {
        T attachPropertiesCopy = attachProperties.asNewMutable();
        // Clear server name
        attachPropertiesCopy.setServerName(null);
        return attachPropertiesCopy;
    }

    @Override
    protected Collection<String> defaultLibraryNames() {
        return LIBRARIES_TO_TRY;
    }

    @Override
    protected FbClientLibrary createClientLibrary() {
        final List<Throwable> throwables = new ArrayList<>();
        final List<String> librariesToTry = findLibrariesToTry();
        for (String libraryName : librariesToTry) {
            try {
                if (Platform.isWindows()) {
                    return Native.load(libraryName, WinFbClientLibrary.class);
                } else {
                    return Native.load(libraryName, FbClientLibrary.class);
                }
            } catch (RuntimeException | UnsatisfiedLinkError e) {
                throwables.add(e);
                log.log(DEBUG, () -> "Attempt to load %s failed".formatted(libraryName), e);
                // continue with next
            }
        }
        assert throwables.size() == librariesToTry.size();
        if (log.isLoggable(ERROR)) {
            log.log(ERROR, "Could not load any of the libraries in {0}:", librariesToTry);
            for (int idx = 0; idx < librariesToTry.size(); idx++) {
                log.log(ERROR, "Loading %s failed".formatted(librariesToTry.get(idx)), throwables.get(idx));
            }
        }
        throw new NativeLibraryLoadException("Could not load any of " + librariesToTry + "; linking first exception",
                throwables.get(0));
    }

    private List<String> findLibrariesToTry() {
        Optional<FirebirdEmbeddedLibrary> optionalFbEmbeddedInstance = FirebirdEmbeddedLookup.findFirebirdEmbedded();
        if (optionalFbEmbeddedInstance.isPresent()) {
            FirebirdEmbeddedLibrary firebirdEmbeddedLibrary = optionalFbEmbeddedInstance.get();
            log.log(INFO, "Found Firebird Embedded {0} on classpath", firebirdEmbeddedLibrary.getVersion());
            if (firebirdEmbeddedLibrary instanceof DisposableFirebirdEmbeddedLibrary disposableLibrary) {
                NativeResourceTracker.strongRegisterNativeResource(
                        new FirebirdEmbeddedLibraryNativeResource(disposableLibrary));
            }

            Path entryPointPath = firebirdEmbeddedLibrary.getEntryPointPath().toAbsolutePath();
            List<String> librariesToTry = new ArrayList<>(LIBRARIES_TO_TRY.size() + 1);
            librariesToTry.add(entryPointPath.toString());
            librariesToTry.addAll(LIBRARIES_TO_TRY);
            return librariesToTry;
        }
        return LIBRARIES_TO_TRY;
    }

    private static final class FirebirdEmbeddedLibraryNativeResource extends NativeResourceTracker.NativeResource {

        private final Cleaner.Cleanable cleanable;

        private FirebirdEmbeddedLibraryNativeResource(DisposableFirebirdEmbeddedLibrary firebirdEmbeddedLibrary) {
            requireNonNull(firebirdEmbeddedLibrary, "firebirdEmbeddedLibrary");
            cleanable = Cleaners.getJbCleaner().register(this, new DisposeAction(firebirdEmbeddedLibrary));
        }

        @Override
        void dispose() {
            cleanable.clean();
        }

        private record DisposeAction(DisposableFirebirdEmbeddedLibrary firebirdEmbeddedLibrary) implements Runnable {
            @Override
            public void run() {
                firebirdEmbeddedLibrary.dispose();
            }
        }
    }

}
