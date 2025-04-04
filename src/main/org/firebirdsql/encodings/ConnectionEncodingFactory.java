// SPDX-FileCopyrightText: Copyright 2013-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.encodings;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.DatatypeCoder;

import java.nio.charset.Charset;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * Implementation of {@link IEncodingFactory} that wraps an {@link EncodingFactory} to
 * override the default encoding.
 * <p>
 * Main use of this class is as a connection specific encoding factory, where the default (fallback) encoding is the
 * connection character set.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
class ConnectionEncodingFactory implements IEncodingFactory {

    private final EncodingFactory factory;
    private final EncodingDefinition defaultEncodingDefinition;
    private final Encoding defaultEncoding;
    private final EncodingDefinition noneEncodingDefinition;
    private final EncodingDefinition octetsEncodingDefinition;
    private final ConcurrentMap<Class<? extends DatatypeCoder>, DatatypeCoder> datatypeCoderCache
            = new ConcurrentHashMap<>(3);

    ConnectionEncodingFactory(EncodingFactory factory, EncodingDefinition defaultEncodingDefinition) {
        assert factory != null && defaultEncodingDefinition != null;
        this.factory = factory;
        this.defaultEncodingDefinition = defaultEncodingDefinition;
        defaultEncoding = defaultEncodingDefinition.getEncoding();
        // Redefine NONE encoding
        if (EncodingFactory.ENCODING_NAME_NONE.equalsIgnoreCase(defaultEncodingDefinition.getFirebirdEncodingName())) {
            noneEncodingDefinition = defaultEncodingDefinition;
        } else if (factory.getEncodingDefinitionByFirebirdName(EncodingFactory.ENCODING_NAME_NONE).isInformationOnly()) {
            noneEncodingDefinition = new DefaultEncodingDefinition(EncodingFactory.ENCODING_NAME_NONE,
                    defaultEncodingDefinition.getJavaCharset(), 1, ISCConstants.CS_NONE, false);
        } else {
            noneEncodingDefinition = getEncodingDefinitionByFirebirdName(EncodingFactory.ENCODING_NAME_NONE);
        }

        // Redefine OCTETS encoding
        if (factory.getEncodingDefinitionByFirebirdName(EncodingFactory.ENCODING_NAME_OCTETS).isInformationOnly()) {
            octetsEncodingDefinition = new DefaultEncodingDefinition(EncodingFactory.ENCODING_NAME_OCTETS,
                    defaultEncodingDefinition.getJavaCharset(), 1, ISCConstants.CS_BINARY, false);
        } else {
            octetsEncodingDefinition = getEncodingDefinitionByFirebirdName(EncodingFactory.ENCODING_NAME_OCTETS);
        }
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
        if (EncodingFactory.ENCODING_NAME_NONE.equalsIgnoreCase(firebirdEncodingName)) {
            return noneEncodingDefinition;
        } else if (EncodingFactory.ENCODING_NAME_OCTETS.equalsIgnoreCase(firebirdEncodingName)) {
            return octetsEncodingDefinition;
        } else {
            return factory.getEncodingDefinitionByFirebirdName(firebirdEncodingName);
        }
    }

    @Override
    public Encoding getEncodingForFirebirdName(final String firebirdEncodingName) {
        if (EncodingFactory.ENCODING_NAME_NONE.equalsIgnoreCase(firebirdEncodingName)) {
            return noneEncodingDefinition.getEncoding();
        } else if (EncodingFactory.ENCODING_NAME_OCTETS.equalsIgnoreCase(firebirdEncodingName)) {
            return octetsEncodingDefinition.getEncoding();
        } else {
            return factory.getEncodingForFirebirdName(firebirdEncodingName, getDefaultEncoding());
        }
    }

    @Override
    public EncodingDefinition getEncodingDefinitionByCharacterSetId(final int firebirdCharacterSetId) {
        switch (firebirdCharacterSetId & 0xFF) {
        case ISCConstants.CS_NONE:
            return noneEncodingDefinition;
        case ISCConstants.CS_BINARY:
            return octetsEncodingDefinition;
        case ISCConstants.CS_dynamic:
            return defaultEncodingDefinition;
        default:
            return factory.getEncodingDefinitionByCharacterSetId(firebirdCharacterSetId);
        }
    }

    @Override
    public Encoding getEncodingForCharacterSetId(final int firebirdCharacterSetId) {
        switch (firebirdCharacterSetId & 0xFF) {
        case ISCConstants.CS_NONE:
            return noneEncodingDefinition.getEncoding();
        case ISCConstants.CS_BINARY:
            return octetsEncodingDefinition.getEncoding();
        case ISCConstants.CS_dynamic:
            return defaultEncoding;
        default:
            return factory.getEncodingForCharacterSetId(firebirdCharacterSetId, getDefaultEncoding());
        }
    }

    @Override
    public EncodingDefinition getEncodingDefinitionByCharset(final Charset charset) {
        return factory.getEncodingDefinitionByCharset(charset);
    }

    @Override
    public Encoding getEncodingForCharset(Charset charset, Encoding fallbackEncoding) {
        return factory.getEncodingForCharset(charset, fallbackEncoding);
    }

    @Override
    public Encoding getEncodingForCharset(final Charset charset) {
        return factory.getEncodingForCharset(charset, getDefaultEncoding());
    }

    @Override
    public Encoding getOrCreateEncodingForCharset(Charset charset) {
        return factory.getOrCreateEncodingForCharset(charset);
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
    public EncodingDefinition getEncodingDefinition(final String firebirdEncodingName, final String javaCharsetAlias) {
        return factory.getEncodingDefinition(firebirdEncodingName, javaCharsetAlias);
    }

    @Override
    public IEncodingFactory withDefaultEncodingDefinition(EncodingDefinition encodingDefinition) {
        return factory.withDefaultEncodingDefinition(
                encodingDefinition != null ? encodingDefinition : getDefaultEncodingDefinition());
    }

    @Override
    public IEncodingFactory withDefaultEncodingDefinition(Charset charset) {
        return withDefaultEncodingDefinition(getEncodingDefinitionByCharset(charset));
    }

    @Override
    public <T extends DatatypeCoder> T getOrCreateDatatypeCoder(Class<T> datatypeCoderClass,
            Function<IEncodingFactory, T> datatypeCoderFactory) {
        return datatypeCoderClass.cast(
                datatypeCoderCache.computeIfAbsent(datatypeCoderClass, clazz -> datatypeCoderFactory.apply(this)));
    }

}
