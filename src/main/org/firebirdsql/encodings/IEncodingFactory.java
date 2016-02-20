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

import java.nio.charset.Charset;
import java.sql.SQLException;

/**
 * Interface for the EncodingFactory.
 * <p>
 * Note on naming convention: normally the prefix <code>I</code> for interfaces is avoided in Java, in this particular
 * case I decided to use it as the concrete class {@link EncodingFactory} is the actual factory and the main access for
 * encoding related information. This interface is used for connection specific factories (eg so the defaultEncoding is
 * the connection character set).
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public interface IEncodingFactory {

    /**
     * @return The {@link Encoding} for {@link java.nio.charset.Charset#defaultCharset()}.
     */
    Encoding getDefaultEncoding();

    /**
     * Returns the {@link EncodingDefinition} for the default platform character set.
     * <p>
     * If the default character set is not supported by Jaybird, an instance of EncodingDefinition should be returned
     * with Firebird encoding name <code>"NONE"</code>.
     * </p>
     *
     * @return The {@link EncodingDefinition} for {@link java.nio.charset.Charset#defaultCharset()}.
     */
    EncodingDefinition getDefaultEncodingDefinition();

    /**
     * Looks up the {@link org.firebirdsql.encodings.EncodingDefinition} for the specified Firebird encoding name.
     *
     * @param firebirdEncodingName
     *         The Firebird encoding name (case insensitive)
     * @return EncodingDefinition instance or <code>null</code> if the encoding name is unknown
     */
    EncodingDefinition getEncodingDefinitionByFirebirdName(String firebirdEncodingName);

    /**
     * Gets an {@link org.firebirdsql.encodings.Encoding} for the specified Firebird encoding. If there is no known
     * encoding for this name,
     * or the loaded EncodingDefinition is information-only, then the defaultEncoding is used.
     *
     * @param firebirdEncodingName
     *         The Firebird encoding name (case insensitive)
     * @return Encoding instance (never null)
     */
    Encoding getEncodingForFirebirdName(final String firebirdEncodingName);

    /**
     * Looks up the {@link org.firebirdsql.encodings.EncodingDefinition} for the specified Firebird character set id.
     * <p>
     * Implementation should return <code>null</code> for the value {@link org.firebirdsql.gds.ISCConstants#CS_dynamic}
     * (=127), as
     * that is the indicator to use the connection character set, which is unknown to this EncodingFactory.
     * </p>
     *
     * @param firebirdCharacterSetId
     *         Firebird character set id
     * @return EncodingDefinition instance or <code>null</code> if the character set id is unknown or <code>127</code>
     */
    EncodingDefinition getEncodingDefinitionByCharacterSetId(int firebirdCharacterSetId);

    /**
     * Gets an {@link org.firebirdsql.encodings.Encoding} for the specified Firebird character set id. If there is no
     * known encoding for this
     * character set id (or if it is 127, see {@link #getEncodingDefinitionByCharacterSetId(int)}),
     * or the loaded EncodingDefinition is information-only, then the defaultEncoding will be used.
     *
     * @param firebirdCharacterSetId
     *         The Firebird character set id
     * @return Encoding instance (never null)
     */
    Encoding getEncodingForCharacterSetId(int firebirdCharacterSetId);

    /**
     * Looks up an {@link org.firebirdsql.encodings.EncodingDefinition} for the Java {@link java.nio.charset.Charset}.
     *
     * @param charset
     *         The Java character set
     * @return EncodingDefinition instance or <code>null</code> if the character set is not mapped
     */
    EncodingDefinition getEncodingDefinitionByCharset(Charset charset);

    /**
     * Gets an {@link org.firebirdsql.encodings.Encoding} for the specified Java character set. If there is no known
     * encoding for this {@link java.nio.charset.Charset}, or the loaded EncodingDefinition is information-only, then
     * the fallbackEncoding will be used.
     *
     * @param charset
     *         The Java character set
     * @param fallbackEncoding
     *         The Encoding to use as fallback if no encoding is found (usually the connection encoding). If
     *         <code>null</code>, the defaultEncoding for the JVM is used.
     * @return Encoding instance (never null)
     * @see #getOrCreateEncodingForCharset(java.nio.charset.Charset)
     */
    Encoding getEncodingForCharset(final Charset charset, final Encoding fallbackEncoding);

    /**
     * Gets an {@link org.firebirdsql.encodings.Encoding} for the specified Java character set. If there is no known
     * encoding for this {@link java.nio.charset.Charset}, or the loaded EncodingDefinition is information-only, then
     * the defaultEncoding will be used.
     *
     * @param charset
     *         The Java character set
     * @return Encoding instance (never null)
     */
    Encoding getEncodingForCharset(Charset charset);

    /**
     * Creates an {@link Encoding} for the specified Java character set. If there is no known encoding for this
     * charset, then an Encoding instance based on the charset is returned.
     * <p>
     * In general the method {@link #getEncodingForCharset(java.nio.charset.Charset, Encoding)} should be used.
     * </p>
     * <p>
     * Don't confuse this method with {@link #getEncodingForCharset(Charset)}, which falls back to the default
     * encoding.
     * </p>
     *
     * @param charset
     *         The Java character set
     * @return Encoding instance (never null)
     * @see #getEncodingForCharset(java.nio.charset.Charset, Encoding)
     */
    Encoding getOrCreateEncodingForCharset(final Charset charset);

    /**
     * Looks up the {@link org.firebirdsql.encodings.EncodingDefinition} by the specified Java character set name or
     * alias.
     *
     * @param charsetAlias
     *         Name (or alias) of the Java character set (case insensitive)
     * @return EncodingDefinition instance or <code>null</code> if the character set name is not mapped
     */
    EncodingDefinition getEncodingDefinitionByCharsetAlias(String charsetAlias);

    /**
     * Gets an {@link org.firebirdsql.encodings.Encoding} for the specified Java character set name or alias. If there
     * is no known encoding for
     * this name, or the loaded EncodingDefinition is information-only, then the defaultEncoding will be used.
     *
     * @param charsetAlias
     *         The Java character set name or alias
     * @return Encoding instance (never null)
     */
    Encoding getEncodingForCharsetAlias(String charsetAlias);

    /**
     * Gets an instance of {@link org.firebirdsql.encodings.CharacterTranslator} for the specified mappingPath.
     *
     * @param mappingPath
     *         Path of the file with mapping definition
     * @return Instance of CharacterTranslator
     * @throws java.sql.SQLException
     */
    CharacterTranslator getCharacterTranslator(String mappingPath) throws SQLException;

    /**
     * Gets or creates an {@link EncodingDefinition} for the supplied Firebird encoding and Java charset.
     * <p>
     * When <code>firebirdEncodingName</code> is not null and <code>javaCharsetAlias</code> is null, then the
     * encoding definition as returned by {@link #getEncodingDefinitionByFirebirdName(String)} is returned. For the
     * reverse (<code>firebirdEncodingName</code> is null and <code>javaCharsetAlias</code> isn't), the encoding
     * definition
     * as returned by {@link #getEncodingDefinitionByCharsetAlias(String)} is returned.
     * </p>
     * <p>
     * When both parameters are set, the result of {@link #getEncodingDefinitionByFirebirdName(String)} is returned if
     * the
     * character set matches, otherwise a new {@link DefaultEncodingDefinition} is created based on its information,
     * but
     * with the specified character set. This can be useful for attempting to fix encoding issues in Firebird.
     * </p>
     * <p>
     * If either of the parameters cannot be resolved, to an EncodingDefinition or {@link Charset}, or the
     * EncodingDefinition
     * is information-only - with the exception of Firebird encoding NONE - and no Java Charset is specified, then null
     * is returned.
     * </p>
     *
     * @param firebirdEncodingName
     *         Name of the Firebird encoding, or null to defer decision to the java Charset alias
     * @param javaCharsetAlias
     *         Alias of the Java character set, or null to defer decision to the Firebird encoding
     * @return An EncodingDefinition or null if both parameters are null, no encoding was found or if an exception
     * occurred.
     */
    EncodingDefinition getEncodingDefinition(String firebirdEncodingName, String javaCharsetAlias);

    /**
     * Returns an {@link org.firebirdsql.encodings.IEncodingFactory} that uses {@code encodingDefinition} as the
     * default.
     *
     * @param encodingDefinition
     *         The default encoding to use (or {@code null} for the value of {@link #getDefaultEncoding()}
     * @return IEncodingFactory instance with the specified default.
     */
    IEncodingFactory withDefaultEncodingDefinition(EncodingDefinition encodingDefinition);

    /**
     * Returns an {@link org.firebirdsql.encodings.IEncodingFactory} that uses an {@link EncodingDefinition} identified
     * by {@code charSet} as the default.
     *
     * @param charset
     *         The default charset to use.
     * @return IEncodingFactory instance with the specified default.
     */
    IEncodingFactory withDefaultEncodingDefinition(Charset charset);
}
