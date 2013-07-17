/*
 * $Id$
 *
 * Firebird Open Source J2EE Connector - JDBC Driver
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
 * can be obtained from a source repository history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.encodings;

import java.nio.charset.Charset;

/**
 * Definition of a Firebird encoding. This is the default implementation of {@link EncodingDefinition}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.3
 */
public final class DefaultEncodingDefinition implements EncodingDefinition {

    private final Charset charset;
    private final String firebirdEncodingName;
    private final int maxBytesPerChar;
    private final int firebirdCharacterSetId;
    private final boolean firebirdOnly;
    private final Encoding encoding;

    /**
     * Initializes an instance of DefaultEncodingDefinition.
     *
     * @param firebirdEncodingName
     *         Name of the Firebird encoding
     * @param charset
     *         Java {@link Charset} instance (<code>null</code> for information-only instances)
     * @param maxBytesPerChar
     *         Maximum number of bytes per character (eg 1 for ISO8859_1, 4 for UTF8, 3 for UNICODE-FSS)
     * @param firebirdCharacterSetId
     *         Firebird character set id
     * @param firebirdOnly
     *         Mapping only applies from Firebird to Java, but not from Java to Firebird (eg Firebird UNICODE-FSS maps
     *         to Java UTF-8, but Java UTF-8 does not map to Firebird UNICODE-FSS (but to Firebird UTF8)
     */
    public DefaultEncodingDefinition(String firebirdEncodingName, Charset charset, int maxBytesPerChar, int firebirdCharacterSetId, boolean firebirdOnly) {
        this.firebirdEncodingName = firebirdEncodingName;
        this.charset = charset;
        this.maxBytesPerChar = maxBytesPerChar;
        this.firebirdOnly = firebirdOnly;
        this.firebirdCharacterSetId = firebirdCharacterSetId;
        if (charset != null) {
            if (maxBytesPerChar == 1) {
                encoding = new EncodingSingleByte(charset);
            } else {
                encoding = new EncodingGeneric(charset);
            }
        } else {
            encoding = null;
        }
    }

    @Override
    public int getMaxBytesPerChar() {
        return maxBytesPerChar;
    }

    @Override
    public String getJavaEncodingName() {
        return charset != null ? charset.name() : null;
    }

    @Override
    public Charset getJavaCharset() {
        return charset;
    }

    @Override
    public String getFirebirdEncodingName() {
        return firebirdEncodingName;
    }

    @Override
    public int getFirebirdCharacterSetId() {
        return firebirdCharacterSetId;
    }

    @Override
    public boolean isFirebirdOnly() {
        return firebirdOnly;
    }

    @Override
    public boolean isInformationOnly() {
        return charset == null;
    }

    /**
     * Returns the {@link Encoding} instance for this definition. The same object is returned
     * each time this method is called.
     */
    @Override
    public Encoding getEncoding() {
        return encoding;
    }
}