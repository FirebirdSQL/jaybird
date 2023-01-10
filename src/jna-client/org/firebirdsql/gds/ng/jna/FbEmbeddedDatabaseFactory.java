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

import com.sun.jna.Native;
import com.sun.jna.Platform;
import org.firebirdsql.gds.ng.IAttachProperties;
import org.firebirdsql.jaybird.util.Cleaners;
import org.firebirdsql.jna.embedded.FirebirdEmbeddedLookup;
import org.firebirdsql.jna.embedded.spi.DisposableFirebirdEmbeddedLibrary;
import org.firebirdsql.jna.embedded.spi.FirebirdEmbeddedLibrary;
import org.firebirdsql.jna.fbclient.FbClientLibrary;
import org.firebirdsql.jna.fbclient.WinFbClientLibrary;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.lang.ref.Cleaner;
import java.nio.file.Path;
import java.util.*;

import static java.util.Objects.requireNonNull;

/**
 * Implementation of {@link org.firebirdsql.gds.ng.FbDatabaseFactory} for establishing connection using the
 * Firebird embedded library.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public class FbEmbeddedDatabaseFactory extends AbstractNativeDatabaseFactory {

    private static final Logger log = LoggerFactory.getLogger(FbEmbeddedDatabaseFactory.class);
    // Note Firebird 3 embedded is fbclient + engine12
    private static final List<String> LIBRARIES_TO_TRY = List.of("fbembed", "fbclient");
    private static final FbEmbeddedDatabaseFactory INSTANCE = new FbEmbeddedDatabaseFactory();

    @Override
    protected <T extends IAttachProperties<T>> T filterProperties(T attachProperties) {
        T attachPropertiesCopy = attachProperties.asNewMutable();
        // Clear server name
        attachPropertiesCopy.setServerName(null);
        return attachPropertiesCopy;
    }

    public static FbEmbeddedDatabaseFactory getInstance() {
        return INSTANCE;
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
                log.debugfe("Attempt to load %s failed", libraryName, e);
                // continue with next
            }
        }
        assert throwables.size() == librariesToTry.size();
        if (log.isErrorEnabled()) {
            log.errorf("Could not load any of the libraries in %s:", librariesToTry);
            for (int idx = 0; idx < librariesToTry.size(); idx++) {
                log.errorfe("Loading %s failed", librariesToTry.get(idx), throwables.get(idx));
            }
        }
        throw new NativeLibraryLoadException("Could not load any of " + librariesToTry + "; linking first exception",
                throwables.get(0));
    }

    private List<String> findLibrariesToTry() {
        Optional<FirebirdEmbeddedLibrary> optionalFbEmbeddedInstance = FirebirdEmbeddedLookup.findFirebirdEmbedded();
        if (optionalFbEmbeddedInstance.isPresent()) {
            FirebirdEmbeddedLibrary firebirdEmbeddedLibrary = optionalFbEmbeddedInstance.get();
            log.infof("Found Firebird Embedded %s on classpath", firebirdEmbeddedLibrary.getVersion());
            if (firebirdEmbeddedLibrary instanceof DisposableFirebirdEmbeddedLibrary) {
                NativeResourceTracker.strongRegisterNativeResource(new FirebirdEmbeddedLibraryNativeResource(
                        (DisposableFirebirdEmbeddedLibrary) firebirdEmbeddedLibrary));
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
