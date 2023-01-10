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
package org.firebirdsql.jna.embedded;

import com.sun.jna.Platform;
import org.firebirdsql.jna.embedded.spi.FirebirdEmbeddedLibrary;
import org.firebirdsql.jna.embedded.spi.FirebirdEmbeddedProvider;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.util.Iterator;
import java.util.Optional;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

/**
 * Locates a {@link FirebirdEmbeddedLibrary} using the service provider mechanism.
 *
 * @author Mark Rotteveel
 * @since 5
 */
public class FirebirdEmbeddedLookup {

    private static final Logger log = LoggerFactory.getLogger(FirebirdEmbeddedLookup.class);

    /**
     * Tries to find a Firebird Embedded library service provider for the current platform and install it.
     * <p>
     * The first instance successfully found and installed will be returned.
     * </p>
     *
     * @return the first embedded instance matching the current platform that installed without errors
     */
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
                    log.errorDebug("Can't load FirebirdEmbeddedProvider (skipping)", e);
                }
            }
        } catch (ServiceConfigurationError | RuntimeException e) {
            log.error("Unable to install Firebird Embedded using ServiceLoader", e);
        }
        return Optional.empty();
    }

}
