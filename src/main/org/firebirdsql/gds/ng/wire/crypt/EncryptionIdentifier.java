/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
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

import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * Identifier of an encryption type + plugin.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0.4
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
