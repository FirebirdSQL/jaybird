// SPDX-FileCopyrightText: Copyright 2013-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.encodings;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.DatatypeCoder;
import org.jspecify.annotations.Nullable;

import java.nio.charset.Charset;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

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
        this.factory = requireNonNull(factory, "factory");
        this.defaultEncodingDefinition = requireNonNull(defaultEncodingDefinition, "defaultEncodingDefinition");
        defaultEncoding = requireNonNull(defaultEncodingDefinition.getEncoding(), "defaultEncoding");

        // Redefine NONE encoding
        if (EncodingFactory.ENCODING_NAME_NONE.equalsIgnoreCase(defaultEncodingDefinition.getFirebirdEncodingName())) {
            noneEncodingDefinition = defaultEncodingDefinition;
        } else {
            EncodingDefinition defaultNoneDefinition = requireNonNull(
                    factory.getEncodingDefinitionByFirebirdName(EncodingFactory.ENCODING_NAME_NONE),
                    "default NONE encoding");
            if (defaultNoneDefinition.isInformationOnly()) {
                noneEncodingDefinition = new DefaultEncodingDefinition(EncodingFactory.ENCODING_NAME_NONE,
                        defaultEncodingDefinition.getJavaCharset(), 1, ISCConstants.CS_NONE, false);
            } else {
                // This case should normally not be hit, as it would mean a connection encoding factory is derived
                // from another connection encoding factory.
                noneEncodingDefinition = defaultNoneDefinition;
            }
        }

        // Redefine OCTETS encoding
        EncodingDefinition defaultOctetsDefinition = requireNonNull(
                factory.getEncodingDefinitionByFirebirdName(EncodingFactory.ENCODING_NAME_OCTETS),
                "default OCTETS encoding");
        if (defaultOctetsDefinition.isInformationOnly()) {
            octetsEncodingDefinition = new DefaultEncodingDefinition(EncodingFactory.ENCODING_NAME_OCTETS,
                    defaultEncodingDefinition.getJavaCharset(), 1, ISCConstants.CS_BINARY, false);
        } else {
            // This case should normally not be hit, as it would mean a connection encoding factory is derived
            // from another connection encoding factory.
            octetsEncodingDefinition = defaultOctetsDefinition;
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
    public @Nullable EncodingDefinition getEncodingDefinitionByFirebirdName(final String firebirdEncodingName) {
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
            //noinspection DataFlowIssue : never null for NONE
            return noneEncodingDefinition.getEncoding();
        } else if (EncodingFactory.ENCODING_NAME_OCTETS.equalsIgnoreCase(firebirdEncodingName)) {
            //noinspection DataFlowIssue : never null for OCTETS
            return octetsEncodingDefinition.getEncoding();
        } else {
            return factory.getEncodingForFirebirdName(firebirdEncodingName, getDefaultEncoding());
        }
    }

    @Override
    public @Nullable EncodingDefinition getEncodingDefinitionByCharacterSetId(final int firebirdCharacterSetId) {
        return switch (firebirdCharacterSetId & 0xFF) {
            case ISCConstants.CS_NONE -> noneEncodingDefinition;
            case ISCConstants.CS_BINARY -> octetsEncodingDefinition;
            case ISCConstants.CS_dynamic -> defaultEncodingDefinition;
            default -> factory.getEncodingDefinitionByCharacterSetId(firebirdCharacterSetId);
        };
    }

    @Override
    public Encoding getEncodingForCharacterSetId(final int firebirdCharacterSetId) {
        return switch (firebirdCharacterSetId & 0xFF) {
            case ISCConstants.CS_NONE -> //noinspection DataFlowIssue : never null for NONE
                    noneEncodingDefinition.getEncoding();
            case ISCConstants.CS_BINARY -> //noinspection DataFlowIssue : never null for OCTETS
                    octetsEncodingDefinition.getEncoding();
            case ISCConstants.CS_dynamic -> defaultEncoding;
            default -> factory.getEncodingForCharacterSetId(firebirdCharacterSetId, getDefaultEncoding());
        };
    }

    @Override
    public @Nullable EncodingDefinition getEncodingDefinitionByCharset(final Charset charset) {
        return factory.getEncodingDefinitionByCharset(charset);
    }

    @Override
    public Encoding getEncodingForCharset(Charset charset, @Nullable Encoding fallbackEncoding) {
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
    public @Nullable EncodingDefinition getEncodingDefinitionByCharsetAlias(final String charsetAlias) {
        return factory.getEncodingDefinitionByCharsetAlias(charsetAlias);
    }

    @Override
    public Encoding getEncodingForCharsetAlias(final String charsetAlias) {
        return factory.getEncodingForCharsetAlias(charsetAlias, getDefaultEncoding());
    }

    @Override
    public @Nullable EncodingDefinition getEncodingDefinition(final @Nullable String firebirdEncodingName,
            final @Nullable String javaCharsetAlias) {
        return factory.getEncodingDefinition(firebirdEncodingName, javaCharsetAlias);
    }

    @Override
    public IEncodingFactory withDefaultEncodingDefinition(@Nullable EncodingDefinition encodingDefinition) {
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
