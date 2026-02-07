// SPDX-FileCopyrightText: Copyright 2023-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.crypt;

import org.firebirdsql.jaybird.util.PluginLoader;
import org.firebirdsql.util.InternalApi;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Registry of encryption plugins.
 *
 * @author Mark Rotteveel
 * @since 6
 */
@InternalApi
public final class EncryptionPluginRegistry {

    private EncryptionPluginRegistry() {
        // no instances
    }

    /**
     * Gets the encryption plugin SPI identified by {@code encryptionIdentifier}.
     *
     * @param encryptionIdentifier
     *         encryption identifier
     * @return the encryption plugin SPI, or empty if there is no SPI registered for {@code encryptionIdentifier}
     */
    public static Optional<EncryptionPluginSpi> getEncryptionPluginSpi(EncryptionIdentifier encryptionIdentifier) {
        return Optional.ofNullable(Holder.PLUGIN_SPI_MAP.get(encryptionIdentifier));
    }

    /**
     * Initialize on demand holder
     */
    private static class Holder {

        private static final Map<EncryptionIdentifier, EncryptionPluginSpi> PLUGIN_SPI_MAP =
                Map.copyOf(findEncryptionPluginSpi());

    }

    private static Map<EncryptionIdentifier, EncryptionPluginSpi> findEncryptionPluginSpi() {
        Collection<EncryptionPluginSpi> pluginSpis = PluginLoader.findPlugins(EncryptionPluginSpi.class, List.of(
                "org.firebirdsql.gds.ng.wire.crypt.arc4.Arc4EncryptionPluginSpi",
                "org.firebirdsql.gds.ng.wire.crypt.chacha.ChaChaEncryptionPluginSpi"));
        var pluginSpiMap = new HashMap<EncryptionIdentifier, EncryptionPluginSpi>();
        for (EncryptionPluginSpi pluginSpi : pluginSpis) {
            EncryptionPluginSpi existingPluginSpi =
                    pluginSpiMap.putIfAbsent(pluginSpi.encryptionIdentifier(), pluginSpi);
            if (existingPluginSpi != null) {
                System.getLogger(EncryptionPluginRegistry.class.getName()).log(System.Logger.Level.WARNING,
                        "Encryption plugin SPI with id {0} was already registered for plugin {1}, skipping plugin {2}",
                        pluginSpi.encryptionIdentifier(), existingPluginSpi.getClass().getName(),
                        pluginSpi.getClass().getName());
            }
        }
        return pluginSpiMap;
    }

}
