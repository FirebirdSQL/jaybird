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
package org.firebirdsql.gds.ng.wire.crypt;

import org.firebirdsql.jaybird.util.PluginLoader;
import org.firebirdsql.util.InternalApi;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry of encryption plugins.
 *
 * @author Mark Rotteveel
 * @since 6
 */
@InternalApi
public final class EncryptionPluginRegistry {

    /**
     * Gets the encryption plugin SPI identified by {@code encryptionIdentifier}.
     *
     * @param encryptionIdentifier
     *         encryption identifier
     * @return the encryption plugin SPI, or {@code null} if there is no SPI registered for {@code encryptionIdentifier}
     */
    public static EncryptionPluginSpi getEncryptionPluginSpi(EncryptionIdentifier encryptionIdentifier) {
        return Holder.PLUGIN_SPI_MAP.get(encryptionIdentifier);
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
