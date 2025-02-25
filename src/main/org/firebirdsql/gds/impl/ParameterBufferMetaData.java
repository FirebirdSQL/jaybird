// SPDX-FileCopyrightText: Copyright 2015-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.impl;

import org.firebirdsql.gds.ParameterBuffer;
import org.firebirdsql.gds.impl.argument.ArgumentType;

import java.io.Serializable;

/**
 * Additional metadata for parameter buffer behavior.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public interface ParameterBufferMetaData extends Serializable {

    /**
     * Parameter buffer type (this usually is the version of the parameter buffer).
     *
     * @return Buffer type (for example {@link org.firebirdsql.gds.ISCConstants#isc_spb_version3}).
     */
    int getType();

    /**
     * Allows the metadata to add a preamble to the parameter buffer.
     * <p>
     * This is only intended for the weird "version 2" connection service parameter buffer that requires two tags for
     * the version with {@code isc_spb_version, isc_spb_current_version}.
     * </p>
     *
     * @param parameterBuffer Parameter buffer.
     */
    default void addPreamble(ParameterBuffer parameterBuffer) {
        // Do nothing
    }

    /**
     * Gets the string argument type for the supplied tag.
     * <p>
     * When the tag is not known (or unsupported for string arguments), then the default should be returned.
     * </p>
     *
     * @param tag Tag (item type)
     * @return Argument type (never {@code null})
     */
    ArgumentType getStringArgumentType(int tag);

    /**
     * Gets the byte array argument type for the supplied tag.
     * <p>
     * When the tag is not known (or unsupported for string arguments), then the default should be returned.
     * </p>
     *
     * @param tag Tag (item type)
     * @return Argument type (never {@code null})
     */
    ArgumentType getByteArrayArgumentType(int tag);

    /**
     * Gets the integer argument type for the supplied tag.
     * <p>
     * When the tag is not known (or unsupported for string arguments), then the default should be returned.
     * </p>
     *
     * @param tag Tag (item type)
     * @return Argument type (never {@code null})
     */
    ArgumentType getIntegerArgumentType(int tag);

    /**
     * Gets the single argument type for the supplied tag.
     * <p>
     * When the tag is not known (or unsupported for string arguments), then the default should be returned.
     * </p>
     *
     * @param tag Tag (item type)
     * @return Argument type (never {@code null})
     */
    ArgumentType getSingleArgumentType(int tag);

    /**
     * Gets the byte argument type for the supplied tag.
     * <p>
     * When the tag is not known (or unsupported for string arguments), then the default should be returned.
     * </p>
     *
     * @param tag Tag (item type)
     * @return Argument type (never {@code null})
     */
    default ArgumentType getByteArgumentType(int tag) {
        return getIntegerArgumentType(tag);
    }

}
