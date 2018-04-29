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

import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * Identifier of an encryption type + plugin.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 4.0
 */
public final class EncryptionIdentifier {

    private final String type;
    private final String pluginName;
    private final int hashCode;

    public EncryptionIdentifier(String type, String pluginName) {
        this.type = requireNonNull(type, "type");
        this.pluginName = requireNonNull(pluginName, "pluginName");
        hashCode = Objects.hash(type, pluginName);
    }

    /**
     * Type of encryption.
     * <p>
     * For example: {@code "Symmetric"}.
     * </p>
     *
     * @return Encryption type
     */
    public String getType() {
        return type;
    }

    /**
     * Name of the plugin (or cipher).
     * <p>
     * For example: {@code "Arc4"}.
     * </p>
     *
     * @return Name of the plugin
     */
    public String getPluginName() {
        return pluginName;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof EncryptionIdentifier)) {
            return false;
        }
        EncryptionIdentifier other = (EncryptionIdentifier) o;
        return this == other
                || (this.type.equals(other.type) && this.pluginName.equals(other.pluginName));
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return type + "/" + pluginName;
    }
}
