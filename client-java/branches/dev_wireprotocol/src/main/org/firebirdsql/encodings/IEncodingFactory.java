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
import java.sql.SQLException;

/**
 * Interface for the EncodingFactory.
 * <p>
 * Note on naming convention: normally the prefix <code>I</code> for interfaces is avoided in Java, in this particular case I decided
 * to use it as the concrete class {@link EncodingFactory} is the actual factory and the main access for encoding
 * related information. This interface is used for connection specific factories (eg so the defaultEncoding is the connection
 * character set).
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.3
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
     * Gets an {@link org.firebirdsql.encodings.Encoding} for the specified Firebird encoding. If there is no known encoding for this name,
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
     * Implementation should return <code>null</code> for the value {@link org.firebirdsql.gds.ISCConstants#CS_dynamic} (=127), as
     * that is the indicator to use the connection character set, which is unknown to this EncodingFactory.
     * </p>
     *
     * @param firebirdCharacterSetId
     *         Firebird character set id
     * @return EncodingDefinition instance or <code>null</code> if the character set id is unknown or <code>127</code>
     */
    EncodingDefinition getEncodingDefinitionByCharacterSetId(int firebirdCharacterSetId);

    /**
     * Gets an {@link org.firebirdsql.encodings.Encoding} for the specified Firebird character set id. If there is no known encoding for this
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
     * Gets an {@link org.firebirdsql.encodings.Encoding} for the specified Java character set. If there is no known encoding for this {@link
     * java.nio.charset.Charset}, or the loaded EncodingDefinition is information-only, then the defaultEncoding will be used.
     *
     * @param charset
     *         The Java character set
     * @return Encoding instance (never null)
     */
    Encoding getEncodingForCharset(Charset charset);

    /**
     * Looks up the {@link org.firebirdsql.encodings.EncodingDefinition} by the specified Java character set name or alias.
     *
     * @param charsetAlias
     *         Name (or alias) of the Java character set (case insensitive)
     * @return EncodingDefinition instance or <code>null</code> if the character set name is not mapped
     */
    EncodingDefinition getEncodingDefinitionByCharsetAlias(String charsetAlias);

    /**
     * Gets an {@link org.firebirdsql.encodings.Encoding} for the specified Java character set name or alias. If there is no known encoding for
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
}
