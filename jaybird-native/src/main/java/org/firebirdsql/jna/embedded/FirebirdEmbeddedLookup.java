// SPDX-FileCopyrightText: Copyright 2020-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jna.embedded;

import com.sun.jna.Platform;
import org.firebirdsql.jna.embedded.spi.FirebirdEmbeddedLibrary;
import org.firebirdsql.jna.embedded.spi.FirebirdEmbeddedProvider;

import java.util.Iterator;
import java.util.Optional;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import static java.lang.System.Logger.Level.ERROR;

/**
 * Locates a {@link FirebirdEmbeddedLibrary} using the service provider mechanism.
 *
 * @author Mark Rotteveel
 * @since 5
 */
public final class FirebirdEmbeddedLookup {

    private static final System.Logger log = System.getLogger(FirebirdEmbeddedLookup.class.getName());

    private FirebirdEmbeddedLookup() {
        // no instances
    }

    /**
     * Tries to find a Firebird Embedded library service provider for the current platform and install it.
     * <p>
     * The first instance successfully found and installed will be returned.
     * </p>
     *
     * @return the first embedded instance matching the current platform that installed without errors
     */
    @SuppressWarnings("java:S1141")
    public static Optional<FirebirdEmbeddedLibrary> findFirebirdEmbedded() {
        try {
            ServiceLoader<FirebirdEmbeddedProvider> firebirdEmbeddedProviders =
                    ServiceLoader.load(FirebirdEmbeddedProvider.class);
            Iterator<FirebirdEmbeddedProvider> iterator = firebirdEmbeddedProviders.iterator();
            // We can't use foreach here, because the services are lazily loaded, which might trigger a ServiceConfigurationError
            while (iterator.hasNext()) {
                try {
                    FirebirdEmbeddedProvider provider = iterator.next();
                    if (Platform.RESOURCE_PREFIX.equals(provider.getPlatform())) {
                        return Optional.of(provider.getFirebirdEmbeddedLibrary());
                    }
                } catch (Exception | ServiceConfigurationError e) {
                    log.log(ERROR, "Can't load FirebirdEmbeddedProvider (skipping); see debug level for stacktrace");
                    log.log(System.Logger.Level.DEBUG, "Can't load FirebirdEmbeddedProvider (skipping)", e);
                }
            }
        } catch (ServiceConfigurationError | RuntimeException e) {
            log.log(ERROR, "Unable to install Firebird Embedded using ServiceLoader", e);
        }
        return Optional.empty();
    }

}
