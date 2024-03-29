/*
 * Public Firebird Java API.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    1. Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *    3. The name of the author may not be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
