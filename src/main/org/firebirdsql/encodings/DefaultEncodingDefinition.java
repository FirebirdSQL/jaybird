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
package org.firebirdsql.encodings;

import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;

/**
 * Definition of a Firebird encoding. This is the default implementation of {@link EncodingDefinition}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public final class DefaultEncodingDefinition implements EncodingDefinition {

    private static final Logger logger = LoggerFactory.getLogger(DefaultEncodingDefinition.class);

    /**
     * Marker object to indicate the encoding field of an instance hasn't been initialized yet
     * (since <code>null</code> is a valid initialization).
     */
    private static final Encoding NOT_INITIALIZED = new EncodingGeneric(null);

    private final String charsetName;
    private final String firebirdEncodingName;
    private final int maxBytesPerChar;
    private final int firebirdCharacterSetId;
    private final boolean firebirdOnly;
    private Encoding encoding = NOT_INITIALIZED;
    private Charset charset;

    /**
     * Initializes an instance of DefaultEncodingDefinition.
     *
     * @param firebirdEncodingName
     *         Name of the Firebird encoding
     * @param charset
     *         Java {@link Charset} instance ({@code null} for information-only instances)
     * @param maxBytesPerChar
     *         Maximum number of bytes per character (eg 1 for ISO8859_1, 4 for UTF8, 3 for UNICODE-FSS)
     * @param firebirdCharacterSetId
     *         Firebird character set id
     * @param firebirdOnly
     *         Mapping only applies from Firebird to Java, but not from Java to Firebird (eg Firebird UNICODE-FSS maps
     *         to Java UTF-8, but Java UTF-8 does not map to Firebird UNICODE-FSS (but to Firebird UTF8)
     */
    public DefaultEncodingDefinition(String firebirdEncodingName, Charset charset, int maxBytesPerChar,
            int firebirdCharacterSetId, boolean firebirdOnly) {
        this(firebirdEncodingName, charset != null ? charset.name() : null, maxBytesPerChar, firebirdCharacterSetId,
                firebirdOnly);
        this.charset = charset;
    }

    /**
     * Initializes an instance of DefaultEncodingDefinition.
     * <p>
     * The actual Java character set is loaded on-demand. If the provided name cannot be resolved to a {@link Charset}
     * at on-demand load time, it will be handled as information-only.
     * </p>
     *
     * @param firebirdEncodingName
     *         Name of the Firebird encoding
     * @param charsetName
     *         Java character set name, or {@code null} for information-only instances
     * @param maxBytesPerChar
     *         Maximum number of bytes per character (eg 1 for ISO8859_1, 4 for UTF8, 3 for UNICODE-FSS)
     * @param firebirdCharacterSetId
     *         Firebird character set id
     * @param firebirdOnly
     *         Mapping only applies from Firebird to Java, but not from Java to Firebird (eg Firebird UNICODE-FSS maps
     *         to Java UTF-8, but Java UTF-8 does not map to Firebird UNICODE-FSS (but to Firebird UTF8)
     */
    public DefaultEncodingDefinition(String firebirdEncodingName, String charsetName, int maxBytesPerChar,
            int firebirdCharacterSetId, boolean firebirdOnly) {
        this.firebirdEncodingName = firebirdEncodingName;
        this.charsetName = charsetName;
        if (charsetName == null) {
            encoding = null;
        }
        this.maxBytesPerChar = maxBytesPerChar;
        this.firebirdOnly = firebirdOnly;
        this.firebirdCharacterSetId = firebirdCharacterSetId;
    }

    @Override
    public int getMaxBytesPerChar() {
        return maxBytesPerChar;
    }

    @Override
    public String getJavaEncodingName() {
        return charsetName;
    }

    @Override
    public Charset getJavaCharset() {
        if (charset == null && encoding == NOT_INITIALIZED) {
            // We intentionally don't use synchronization or volatile here
            // Multiple initialization might be a bit wasteful, but it has no other side effects
            initCharset();
        }
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
        return charsetName == null || getJavaCharset() == null;
    }

    /**
     * Returns the {@link Encoding} instance for this definition. The same object is returned
     * each time this method is called.
     */
    @Override
    public Encoding getEncoding() {
        if (encoding == NOT_INITIALIZED) {
            // We intentionally don't use synchronization or volatile here
            // Multiple initialization might be a bit wasteful, but it has no other side effects
            initEncoding();
        }
        return encoding;
    }

    @Override
    public String toString() {
        return "[" +
                "firebirdEncodingName='" + getFirebirdEncodingName() + "'," +
                "javaEncodingName='" + getJavaEncodingName() + "'," +
                "maxBytesPerChar=" + getMaxBytesPerChar() + "," +
                "firebirdOnly=" + isFirebirdOnly() + "," +
                "firebirdCharacterSetId=" + getFirebirdCharacterSetId() +
                "]";
    }

    private void initCharset() {
        try {
            charset = Charset.forName(charsetName);
        } catch (IllegalCharsetNameException | UnsupportedCharsetException e) {
            // Prevent further attempts
            encoding = null;
            String message = String.format("charsetName=\"%s\" specified for Firebird encoding \"%s\" is an illegal or "
                            + "unsupported character set name, handling as information-only",
                    charsetName, firebirdEncodingName);
            logger.warn(message + ": " + e + "; see debug level for stacktrace");
            logger.debug(message, e);
        }
    }

    private void initEncoding() {
        if (isInformationOnly()) {
            encoding = null;
        } else if (getMaxBytesPerChar() == 1
                // Exception for NONE / UTF-8 (prevents it from creating a single byte encoding)
                && !("NONE".equals(firebirdEncodingName) && StandardCharsets.UTF_8.equals(getJavaCharset()))) {
            encoding = new EncodingSingleByte(getJavaCharset());
        } else {
            encoding = new EncodingGeneric(getJavaCharset());
        }
    }
}
