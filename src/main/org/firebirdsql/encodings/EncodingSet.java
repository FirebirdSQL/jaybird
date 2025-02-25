// SPDX-FileCopyrightText: Copyright 2013-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.encodings;

import java.util.List;

/**
 * {@code EncodingSet} is an interface for the purpose of loading {@link EncodingDefinition} mappings into Jaybird.
 * <p>
 * Jaybird uses the {@link java.util.ServiceLoader} mechanism to load the EncodingSet. An implementation must
 * provide a no-arg constructor, and it must be listed in {@code META-INF/services/org.firebirdsql.encodings.EncodingSet}
 * </p>
 * <p>
 * Providing an EncodingSet in general is not necessary, as Jaybird provides a default. However if Jaybird does not
 * include an encoding, or the current mapping from Firebird to Java (or reverse) doesn't match your requirements, then
 * you can use this mechanism to override it.
 * </p>
 * <p>
 * The encoding information is only loaded once, so the definition will need to be available in the classpath of the
 * class loader that initially loads and initializes Jaybird.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public interface EncodingSet {

    /**
     * Preference weight defines the loading order.
     * <p>
     * An {@code EncodingSet} with a higher number is processed before a lower number. The default preference weight
     * used by {@link DefaultEncodingSet} in Jaybird is {@code 0}. This means that if you want to override any default
     * definition, you need a higher number, if you just want to specify additional mappings, you need to specify a
     * lower number.
     * </p>
     *
     * @return Preference weight
     */
    int getPreferenceWeight();

    /**
     * @return List of {@link EncodingDefinition} implementations.
     */
    List<EncodingDefinition> getEncodings();
}
