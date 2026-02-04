// SPDX-FileCopyrightText: Copyright 2013-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.encodings;

import org.jspecify.annotations.Nullable;

import java.nio.charset.Charset;
import java.util.Objects;

import static java.lang.System.Logger.Level.DEBUG;
import static java.util.Objects.requireNonNull;

/**
 * Definition of a Firebird encoding. This is the default implementation of {@link EncodingDefinition}.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public final class DefaultEncodingDefinition implements EncodingDefinition {

    private static final System.Logger logger = System.getLogger(DefaultEncodingDefinition.class.getName());

    /**
     * Marker object to indicate the encoding field of an instance hasn't been initialised yet (since {@code null} is
     * a semi-valid initialisation).
     */
    @SuppressWarnings("DataFlowIssue")
    private static final Encoding NOT_INITIALIZED = new EncodingGeneric(null);

    private @Nullable final String charsetName;
    private final String firebirdEncodingName;
    private final int maxBytesPerChar;
    private final int firebirdCharacterSetId;
    private final boolean firebirdOnly;
    private @Nullable Encoding encoding = NOT_INITIALIZED;
    private @Nullable Charset charset;

    /**
     * Initialises an instance of DefaultEncodingDefinition.
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
     *         Mapping only applies from Firebird to Java, but not from Java to Firebird (e.g. Firebird UNICODE-FSS maps
     *         to Java UTF-8, but Java UTF-8 does not map to Firebird UNICODE-FSS (but to Firebird UTF8)
     */
    public DefaultEncodingDefinition(String firebirdEncodingName, @Nullable Charset charset, int maxBytesPerChar,
            int firebirdCharacterSetId, boolean firebirdOnly) {
        this(firebirdEncodingName, charset != null ? charset.name() : null, maxBytesPerChar, firebirdCharacterSetId,
                firebirdOnly);
        this.charset = charset;
    }

    /**
     * Initialises an instance of DefaultEncodingDefinition.
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
     *         Mapping only applies from Firebird to Java, but not from Java to Firebird (e.g. Firebird UNICODE-FSS maps
     *         to Java UTF-8, but Java UTF-8 does not map to Firebird UNICODE-FSS (but to Firebird UTF8)
     */
    public DefaultEncodingDefinition(String firebirdEncodingName, @Nullable String charsetName, int maxBytesPerChar,
            int firebirdCharacterSetId, boolean firebirdOnly) {
        this.firebirdEncodingName = requireNonNull(firebirdEncodingName, "firebirdEncodingName");
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
    public @Nullable String getJavaEncodingName() {
        return charsetName;
    }

    @Override
    public @Nullable Charset getJavaCharset() {
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
    public @Nullable Encoding getEncoding() {
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

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultEncodingDefinition that = (DefaultEncodingDefinition) o;
        return maxBytesPerChar == that.maxBytesPerChar
               && firebirdCharacterSetId == that.firebirdCharacterSetId
               && firebirdOnly == that.firebirdOnly
               && Objects.equals(charsetName, that.charsetName)
               && firebirdEncodingName.equals(that.firebirdEncodingName);
    }

    @Override
    public int hashCode() {
        int result = charsetName != null ? charsetName.hashCode() : 0;
        result = 31 * result + firebirdCharacterSetId;
        return result;
    }

    private void initCharset() {
        assert charsetName != null : "charsetName may not be null when calling initCharset()";
        try {
            charset = Charset.forName(charsetName);
        } catch (IllegalArgumentException e) {
            // Prevent further attempts
            encoding = null;
            logger.log(System.Logger.Level.WARNING,
                    "charsetName=\"{0}\" specified for Firebird encoding \"{1}\" is an illegal or unsupported "
                    + "character set name, handling as information-only; see debug level for stacktrace",
                    charsetName, firebirdEncodingName);
            logger.log(DEBUG, "Exception for illegal or unsupported character set name", e);
        }
    }

    private void initEncoding() {
        if (isInformationOnly()) {
            encoding = null;
        } else {
            //noinspection DataFlowIssue : isInformationOnly() is true if getJavaCharSet() would return null
            encoding = new EncodingGeneric(getJavaCharset());
        }
    }
}
