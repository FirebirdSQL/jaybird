// SPDX-FileCopyrightText: Copyright 2017-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds.ng.wire.crypt;

/**
 * Service provider interface for wire encryption plugins.
 * <p>
 * NOTE: This plugin is currently only internal to Jaybird, consider the API as unstable.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 4.0
 */
public interface EncryptionPluginSpi {

    /**
     * @return Encryption identifier
     */
    EncryptionIdentifier encryptionIdentifier();

    /**
     * Creates the encryption plugin for the provided crypt session config.
     *
     * @param cryptSessionConfig
     *         Crypt session config
     * @return Encryption plugin
     */
    EncryptionPlugin createEncryptionPlugin(CryptSessionConfig cryptSessionConfig);

    /**
     * Reports if the encryption plugin can work.
     * <p>
     * The {@code connectionInfo} can be used to check compatibility with the connection, but other checks may be done
     * as well. If the plugin expects to always work, it can simply return {@code true}.
     * </p>
     * <p>
     * NOTE: Returning {@code true} does not express a guarantee the plugin will work, instead {@code false} expresses
     * that the plugin cannot (or should not) be tried to use, because it will fail anyway.
     * </p>
     *
     * @param cryptConnectionInfo
     *         information on the connection
     * @return {@code true} if the SPI expects the plugin to work, {@code false} if the plugin will not work
     * @since 6
     */
    boolean isSupported(CryptConnectionInfo cryptConnectionInfo);

}
