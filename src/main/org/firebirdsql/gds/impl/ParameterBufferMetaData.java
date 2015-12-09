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
package org.firebirdsql.gds.impl;

import org.firebirdsql.gds.ParameterBuffer;
import org.firebirdsql.gds.impl.argument.ArgumentType;

/**
 * Additional metadata for parameter buffer behavior.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public interface ParameterBufferMetaData {

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
    void addPreamble(ParameterBuffer parameterBuffer);

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
     * Gets the byte array argument type for the supplied tag.
     * <p>
     * When the tag is not known (or unsupported for string arguments), then the default should be returned.
     * </p>
     *
     * @param tag Tag (item type)
     * @return Argument type (never {@code null})
     */
    ArgumentType getIntegerArgumentType(int tag);
}
