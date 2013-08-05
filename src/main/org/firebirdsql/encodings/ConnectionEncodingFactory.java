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
 * Implementation of {@link IEncodingFactory} that wraps an {@link EncodingFactory} to
 * override the default encoding.
 * <p>
 * Main use of this class is as a connection specific encoding factory, where the default (fallback) encoding is the connection
 * character set.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.3
 */
class ConnectionEncodingFactory implements IEncodingFactory {

    private final EncodingFactory factory;
    private final EncodingDefinition defaultEncodingDefinition;
    private final Encoding defaultEncoding;

    ConnectionEncodingFactory(EncodingFactory factory, EncodingDefinition defaultEncodingDefinition) {
        this.factory = factory;
        this.defaultEncodingDefinition = defaultEncodingDefinition;
        this.defaultEncoding = defaultEncodingDefinition.getEncoding();
    }

    @Override
    public Encoding getDefaultEncoding() {
        return defaultEncoding;
    }

    @Override
    public EncodingDefinition getDefaultEncodingDefinition() {
        return defaultEncodingDefinition;
    }

    @Override
    public EncodingDefinition getEncodingDefinitionByFirebirdName(final String firebirdEncodingName) {
        return factory.getEncodingDefinitionByFirebirdName(firebirdEncodingName);
    }

    @Override
    public Encoding getEncodingForFirebirdName(final String firebirdEncodingName) {
        return factory.getEncodingForFirebirdName(firebirdEncodingName, getDefaultEncoding());
    }

    @Override
    public EncodingDefinition getEncodingDefinitionByCharacterSetId(final int firebirdCharacterSetId) {
        // TODO: Be able to return the EncodingDefinition of the default encoding if unknown or 127?
        return factory.getEncodingDefinitionByCharacterSetId(firebirdCharacterSetId);
    }

    @Override
    public Encoding getEncodingForCharacterSetId(final int firebirdCharacterSetId) {
        return factory.getEncodingForCharacterSetId(firebirdCharacterSetId, getDefaultEncoding());
    }

    @Override
    public EncodingDefinition getEncodingDefinitionByCharset(final Charset charset) {
        return factory.getEncodingDefinitionByCharset(charset);
    }

    @Override
    public Encoding getEncodingForCharset(final Charset charset) {
        return factory.getEncodingForCharset(charset, getDefaultEncoding());
    }

    @Override
    public EncodingDefinition getEncodingDefinitionByCharsetAlias(final String charsetAlias) {
        return factory.getEncodingDefinitionByCharsetAlias(charsetAlias);
    }

    @Override
    public Encoding getEncodingForCharsetAlias(final String charsetAlias) {
        return factory.getEncodingForCharsetAlias(charsetAlias, getDefaultEncoding());
    }

    @Override
    public CharacterTranslator getCharacterTranslator(final String mappingPath) throws SQLException {
        return factory.getCharacterTranslator(mappingPath);
    }

    @Override
    public EncodingDefinition getEncodingDefinition(final String firebirdEncodingName, final String javaCharsetAlias) {
        return factory.getEncodingDefinition(firebirdEncodingName, javaCharsetAlias);
    }
}
