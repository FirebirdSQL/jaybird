// SPDX-FileCopyrightText: Copyright 2013-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.encodings;

import java.nio.charset.Charset;

/**
 * Definition of a Firebird encoding. This is a mapping from the Firebird encoding to a Java Charset and additional
 * information needed by Jaybird to process this encoding.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public interface EncodingDefinition {

    /**
     * @return Maximum number of bytes per character.
     */
    int getMaxBytesPerChar();

    /**
     * @return Java name of the encoding
     */
    String getJavaEncodingName();

    /**
     * @return Java {@link java.nio.charset.Charset} for this encoding
     */
    Charset getJavaCharset();

    /**
     * @return Firebird name of the encoding
     */
    String getFirebirdEncodingName();

    /**
     * @return Firebird id of the encoding
     */
    int getFirebirdCharacterSetId();

    /**
     * Can (or should) this encoding be used for reverse mapping from Java to Firebird.
     * <p>
     * The best example of this is the Firebird character set <code>UNICODE-FSS</code> which maps to the
     * Java character set <code>UTF-8</code>, but when Java character set<code>UTF-8</code> is requested, Jaybird
     * should (in general) map to Firebird character set <code>UTF8</code>.
     * </p>
     *
     * @return <code>true</code> when this encoding maps from Java to Firebird, <code>false</code> otherwise
     */
    boolean isFirebirdOnly();

    /**
     * Can this implementation create an {@link Encoding} instance, or does it provide information only (e.g. about
     * unsupported character sets)
     *
     * @return <code>true</code> if this EncodingDefinition only provides information, and is not capable of building a
     *         concrete implementation.
     */
    boolean isInformationOnly();

    /**
     * Gets the {@link Encoding} based on this definition.
     * <p>
     * Implementations can return the same instance on every call, or create a new one each time this method is called
     * </p>
     *
     * @return Encoding object or <code>null</code> if this is an information only EncodingDefinition
     * @see #isInformationOnly()
     */
    Encoding getEncoding();
}
