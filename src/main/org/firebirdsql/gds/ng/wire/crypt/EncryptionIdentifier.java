// SPDX-FileCopyrightText: Copyright 2017-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds.ng.wire.crypt;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * Identifier of an encryption type + plugin.
 *
 * @author Mark Rotteveel
 * @since 4.0
 */
public final class EncryptionIdentifier {

    public static final String TYPE_SYMMETRIC = "Symmetric";

    private final String type;
    private final String pluginName;
    private final int hashCode;

    public EncryptionIdentifier(String type, String pluginName) {
        this.type = requireNonNull(type, "type");
        this.pluginName = requireNonNull(pluginName, "pluginName");
        // The performance impact of precalculating the hash is significant enough to not convert this to a record
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
    public String type() {
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
    public String pluginName() {
        return pluginName;
    }

    public boolean isTypeSymmetric() {
        return TYPE_SYMMETRIC.equals(type);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (!(o instanceof EncryptionIdentifier other)) return false;
        return this.type.equals(other.type) && this.pluginName.equals(other.pluginName);
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
