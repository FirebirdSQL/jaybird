// SPDX-FileCopyrightText: Copyright 2017-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.impl.wire.WireProtocolConstants;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Locale;

/**
 * @author Mark Rotteveel
 */
@NullMarked
public enum WireCrypt {
    REQUIRED(WireProtocolConstants.WIRE_CRYPT_REQUIRED),
    ENABLED(WireProtocolConstants.WIRE_CRYPT_ENABLED),
    DISABLED(WireProtocolConstants.WIRE_CRYPT_DISABLED),
    /**
     * Equivalent to {@link #ENABLED} for wire protocol, but for JNA connections
     * it uses the default of the {@code firebird.conf} used by the client library.
     */
    DEFAULT(WireProtocolConstants.WIRE_CRYPT_ENABLED);

    private final int wireProtocolCryptLevel;

    WireCrypt(int wireProtocolCryptLevel) {
        this.wireProtocolCryptLevel = wireProtocolCryptLevel;
    }

    /**
     * @return Encryption level value for the wire protocol.
     */
    public int getWireProtocolCryptLevel() {
        return wireProtocolCryptLevel;
    }

    /**
     * Get the enum value for the provided name, case-insensitive.
     * <p>
     * Works like {@link #valueOf(String)}, except {@code null} will return {@link #DEFAULT} and values
     * are handled case-insensitively.
     * </p>
     *
     * @param name
     *         String name
     * @return Enum name for the name
     * @throws IllegalArgumentException
     *         if this enum type has no constant with the specified name
     */
    public static WireCrypt fromString(@Nullable String name) throws IllegalArgumentException {
        if (name == null) {
            return DEFAULT;
        }
        String uppercaseValue = name.toUpperCase(Locale.ROOT);
        return valueOf(uppercaseValue);
    }
}
