/*
 * Firebird Open Source JDBC Driver
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

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.DatatypeCoder;
import org.firebirdsql.gds.ng.DefaultDatatypeCoder;
import org.firebirdsql.gds.ng.jna.BigEndianDatatypeCoder;
import org.firebirdsql.gds.ng.jna.LittleEndianDatatypeCoder;
import org.firebirdsql.jaybird.util.PluginLoader;

import java.lang.reflect.Constructor;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.WARNING;
import static org.firebirdsql.gds.ISCConstants.CS_dynamic;

/**
 * Factory for {@link EncodingDefinition} and {@link Encoding}.
 *
 * @author Mark Rotteveel
 */
public final class EncodingFactory implements IEncodingFactory {

    private static final System.Logger log = System.getLogger(EncodingFactory.class.getName());

    private static final Charset DEFAULT_CHARSET = Charset.defaultCharset();
    private static final int MAX_NORMAL_CHARSET_ID = 255;

    public static final String ENCODING_NAME_NONE = "NONE";
    public static final String ENCODING_NAME_OCTETS = "OCTETS";

    /**
     * Holder class to ensure static fields of EncodingFactory are properly initialized before the default instance can
     * be loaded/created
     */
    private static class DefaultEncodingFactory {
        private static final EncodingFactory ROOT_ENCODING_FACTORY;
        private static final IEncodingFactory PLATFORM_DEFAULT_INSTANCE;
        static {
            ROOT_ENCODING_FACTORY = createInstance();
            PLATFORM_DEFAULT_INSTANCE = ROOT_ENCODING_FACTORY
                    .withDefaultEncodingDefinition();
        }
    }

    // Linked HashMap because we need to preserve insertion order for the lazy initialization;
    // Initial capacity of 128 should prevent resizing during loading (if only DefaultEncodingSet is present)
    private final Map<String, EncodingDefinition> firebirdEncodingToDefinition = new LinkedHashMap<>(128);
    private final EncodingDefinition[] firebirdCharacterSetIdToDefinition =
            new EncodingDefinition[MAX_NORMAL_CHARSET_ID + 1];
    private final Map<Charset, EncodingDefinition> javaCharsetToDefinition = new ConcurrentHashMap<>();
    private final Map<String, EncodingDefinition> javaAliasesToDefinition = new ConcurrentHashMap<>();
    private final Encoding defaultEncoding;
    private final EncodingDefinition defaultEncodingDefinition;
    private final ConcurrentMap<Class<? extends DatatypeCoder>, DatatypeCoder> datatypeCoderCache
            = new ConcurrentHashMap<>(3);
    private final Map<EncodingDefinition, ConnectionEncodingFactory> connectionEncodingFactoryCache
            = new ConcurrentHashMap<>();

    /**
     * Initializes EncodingFactory by processing the encodingSets using the provided iterator.
     *
     * @param encodingSets
     *         Iterator of {@link EncodingSet} instances
     */
    private EncodingFactory(Iterator<EncodingSet> encodingSets) {
        while (encodingSets.hasNext()) {
            processEncodingSet(encodingSets.next());
        }
        // Ensure CS_dynamic to be null
        firebirdCharacterSetIdToDefinition[CS_dynamic] = null;
        final EncodingDefinition candidateDefinition = getEncodingDefinitionByCharset(DEFAULT_CHARSET);

        if (candidateDefinition != null && !candidateDefinition.isInformationOnly()) {
            defaultEncoding = candidateDefinition.getEncoding();
            defaultEncodingDefinition = candidateDefinition;
        } else {
            defaultEncoding = new EncodingGeneric(DEFAULT_CHARSET);
            defaultEncodingDefinition =
                    new DefaultEncodingDefinition(ENCODING_NAME_NONE, DEFAULT_CHARSET, 1, ISCConstants.CS_NONE, false);
        }
    }

    @Override
    public Encoding getDefaultEncoding() {
        return defaultEncoding;
    }

    /**
     * @return The default encoding of the platform.
     */
    public static Encoding getPlatformEncoding() {
        return getRootEncodingFactory().getDefaultEncoding();
    }

    @Override
    public EncodingDefinition getDefaultEncodingDefinition() {
        return defaultEncodingDefinition;
    }

    @Override
    public EncodingDefinition getEncodingDefinitionByFirebirdName(final String firebirdEncodingName) {
        return firebirdEncodingName != null
                ? firebirdEncodingToDefinition.get(firebirdEncodingName.toLowerCase(Locale.ROOT))
                : null;
    }

    /**
     * Gets an {@link org.firebirdsql.encodings.Encoding} for the specified Firebird encoding. If there is no known
     * encoding for this name, or the loaded EncodingDefinition is information-only, then the fallbackEncoding.
     *
     * @param firebirdEncodingName
     *         The Firebird encoding name (case-insensitive)
     * @param fallbackEncoding
     *         The Encoding to use as fallback if no encoding is found (usually the connection encoding). If
     *         <code>null</code>, the defaultEncoding for the JVM is used.
     * @return Encoding instance (never null)
     */
    public Encoding getEncodingForFirebirdName(final String firebirdEncodingName, final Encoding fallbackEncoding) {
        return returnEncodingOrFallback(getEncodingDefinitionByFirebirdName(firebirdEncodingName), fallbackEncoding);
    }

    @Override
    public Encoding getEncodingForFirebirdName(final String firebirdEncodingName) {
        return getEncodingForFirebirdName(firebirdEncodingName, null);
    }

    @Override
    public EncodingDefinition getEncodingDefinitionByCharacterSetId(final int firebirdCharacterSetId) {
        return firebirdCharacterSetIdToDefinition[firebirdCharacterSetId & 0xFF];
    }

    /**
     * Gets an {@link org.firebirdsql.encodings.Encoding} for the specified Firebird character set id. If there is no
     * known encoding for this character set id (or if it is 127, see {@link #getEncodingDefinitionByCharacterSetId(int)}),
     * or the loaded EncodingDefinition is information-only, then the fallbackEncoding will be used.
     *
     * @param firebirdCharacterSetId
     *         The Firebird character set id
     * @param fallbackEncoding
     *         The Encoding to use as fallback if no encoding is found (usually the connection encoding). If
     *         <code>null</code>, the defaultEncoding for the JVM is used.
     * @return Encoding instance (never null)
     */
    public Encoding getEncodingForCharacterSetId(final int firebirdCharacterSetId, final Encoding fallbackEncoding) {
        return returnEncodingOrFallback(getEncodingDefinitionByCharacterSetId(firebirdCharacterSetId), fallbackEncoding);
    }

    @Override
    public Encoding getEncodingForCharacterSetId(final int firebirdCharacterSetId) {
        return getEncodingForCharacterSetId(firebirdCharacterSetId, null);
    }

    @Override
    public EncodingDefinition getEncodingDefinitionByCharset(final Charset charset) {
        EncodingDefinition encodingDefinition = javaCharsetToDefinition.get(charset);
        if (encodingDefinition != null) {
            return encodingDefinition;
        }
        return findAndMapEncodingDefinition(charset);
    }

    private EncodingDefinition findAndMapEncodingDefinition(final Charset charset) {
        final Set<String> potentialNames = toLowerCaseAliasSet(charset);
        for (EncodingDefinition encodingDefinition : firebirdEncodingToDefinition.values()) {
            String javaEncodingName = encodingDefinition.getJavaEncodingName();
            if (javaEncodingName != null && !encodingDefinition.isFirebirdOnly()
                    && potentialNames.contains(javaEncodingName.toLowerCase(Locale.ROOT))) {
                registerJavaMappingForEncodingDefinition(encodingDefinition);
                return encodingDefinition;
            }
        }
        return null;
    }

    @Override
    public Encoding getEncodingForCharset(final Charset charset, final Encoding fallbackEncoding) {
        return returnEncodingOrFallback(getEncodingDefinitionByCharset(charset), fallbackEncoding);
    }

    @Override
    public Encoding getEncodingForCharset(final Charset charset) {
        return getEncodingForCharset(charset, null);
    }

    @Override
    public Encoding getOrCreateEncodingForCharset(final Charset charset) {
        return getEncodingForCharset(charset, new EncodingGeneric(charset));
    }

    @Override
    public EncodingDefinition getEncodingDefinitionByCharsetAlias(final String charsetAlias) {
        if (charsetAlias == null) {
            return null;
        }
        EncodingDefinition encodingDefinition = javaAliasesToDefinition.get(charsetAlias.toLowerCase(Locale.ROOT));
        if (encodingDefinition != null) {
            return encodingDefinition;
        }
        return resolveEncodingDefinitionByCharset(charsetAlias);
    }

    /**
     * Gets an {@link org.firebirdsql.encodings.Encoding} for the specified Java character set name or alias. If there
     * is no known encoding for this name, or the loaded EncodingDefinition is information-only, then the
     * fallbackEncoding will be used.
     *
     * @param charsetAlias
     *         The Java character set name or alias
     * @param fallbackEncoding
     *         The Encoding to use as fallback if no encoding is found (usually the connection encoding). If
     *         <code>null</code>, the defaultEncoding for the JVM is used.
     * @return Encoding instance (never null)
     */
    public Encoding getEncodingForCharsetAlias(final String charsetAlias, final Encoding fallbackEncoding) {
        return returnEncodingOrFallback(getEncodingDefinitionByCharsetAlias(charsetAlias), fallbackEncoding);
    }

    @Override
    public Encoding getEncodingForCharsetAlias(final String charsetAlias) {
        return getEncodingForCharsetAlias(charsetAlias, null);
    }

    @Override
    public EncodingDefinition getEncodingDefinition(final String firebirdEncodingName, final String javaCharsetAlias) {
        try {
            EncodingDefinition encodingDefinition = null;
            Charset charset = null;
            if (firebirdEncodingName != null) {
                encodingDefinition = getEncodingDefinitionByFirebirdName(firebirdEncodingName);
                if (javaCharsetAlias != null) {
                    charset = Charset.forName(javaCharsetAlias);
                } else if (encodingDefinition != null) {
                    charset = encodingDefinition.getJavaCharset();
                }
            } else if (javaCharsetAlias != null) {
                encodingDefinition = getEncodingDefinitionByCharsetAlias(javaCharsetAlias);
                if (encodingDefinition != null) {
                    charset = encodingDefinition.getJavaCharset();
                }
            }

            if (encodingDefinition == null) {
                return null;
            } else if (!encodingDefinition.isInformationOnly()
                    && (charset == null || encodingDefinition.getJavaCharset().equals(charset))) {
                // Normal encoding definition
                return encodingDefinition;
            } else if (charset != null) {
                /* Construct non-standard combination of Firebird encoding + Java character set
                 * This allows for special purpose combinations like Firebird ISO8859_3 with Java ISO-8859-1
                 * But is mostly intended for using Firebird NONE with a specific java character set
                 */
                return new DefaultEncodingDefinition(encodingDefinition.getFirebirdEncodingName(), charset, encodingDefinition.getMaxBytesPerChar(),
                        encodingDefinition.getFirebirdCharacterSetId(), false);
            } else if (ENCODING_NAME_NONE.equalsIgnoreCase(firebirdEncodingName)) {
                encodingDefinition = getDefaultEncodingDefinition();
                return new DefaultEncodingDefinition(ENCODING_NAME_NONE, encodingDefinition.getJavaCharset(), 1, ISCConstants.CS_NONE, false);
            }
            return null;
        } catch (Exception e) {
            log.log(DEBUG,
                    () -> "Exception looking up encoding definition for firebirdEncodingName %s, javaCharsetAlias %s"
                            .formatted(firebirdEncodingName, javaCharsetAlias), e);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation returns an instance of {@link ConnectionEncodingFactory}.
     * </p>
     */
    @Override
    public IEncodingFactory withDefaultEncodingDefinition(EncodingDefinition encodingDefinition) {
        EncodingDefinition resolvedEncodingDefinition =
                encodingDefinition != null && !encodingDefinition.isInformationOnly()
                        ? encodingDefinition
                        : getDefaultEncodingDefinition();
        return connectionEncodingFactoryCache.computeIfAbsent(resolvedEncodingDefinition,
                def -> new ConnectionEncodingFactory(this, def));
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation returns an instance of {@link ConnectionEncodingFactory}.
     * </p>
     */
    @Override
    public IEncodingFactory withDefaultEncodingDefinition(Charset charset) {
        return withDefaultEncodingDefinition(getEncodingDefinitionByCharset(charset));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends DatatypeCoder> T getOrCreateDatatypeCoder(Class<T> datatypeCoderClass) {
        return (T) datatypeCoderCache.computeIfAbsent(datatypeCoderClass, this::createNewDatatypeCoder);
    }

    private <T extends DatatypeCoder> T createNewDatatypeCoder(Class<T> datatypeCoderClass) {
        return createNewDatatypeCoder(datatypeCoderClass, this);
    }

    @SuppressWarnings({ "unchecked" })
    static <T extends DatatypeCoder> T createNewDatatypeCoder(Class<T> datatypeCoderClass,
            IEncodingFactory encodingFactory) {
        // Avoid reflection if we can:
        if (datatypeCoderClass == DefaultDatatypeCoder.class) {
            return (T) new DefaultDatatypeCoder(encodingFactory);
        } else if (datatypeCoderClass == LittleEndianDatatypeCoder.class) {
            return (T) new LittleEndianDatatypeCoder(encodingFactory);
        } else if (datatypeCoderClass == BigEndianDatatypeCoder.class) {
            return (T) new BigEndianDatatypeCoder(encodingFactory);
        } else {
            try {
                Constructor<T> datatypeCoderConstructor = datatypeCoderClass.getConstructor(IEncodingFactory.class);
                return datatypeCoderConstructor.newInstance(encodingFactory);
            } catch (ReflectiveOperationException e) {
                throw new IllegalArgumentException("Type " + datatypeCoderClass +
                        " has no single arg constructor accepting an IEncodingFactory");
            }
        }
    }

    /**
     * Returns an {@link org.firebirdsql.encodings.ConnectionEncodingFactory} that uses
     * {@link #getDefaultEncodingDefinition()} as the default.
     *
     * @return IEncodingFactory instance with the specified default.
     */
    public IEncodingFactory withDefaultEncodingDefinition() {
        return withDefaultEncodingDefinition(getDefaultEncodingDefinition());
    }

    /**
     * Loads the {@link EncodingSet} instances from the classpath (using {@link ServiceLoader}).
     *
     * @see EncodingSet
     */
    private static NavigableSet<EncodingSet> loadEncodingSets() {
        final var encodingSets = new TreeSet<>(encodingSetComparator());
        encodingSets.addAll(
                PluginLoader.findPlugins(EncodingSet.class, List.of(), PluginLoader.ClassSource.PLUGIN_CLASS_LOADER));
        return encodingSets;
    }

    /**
     * Loads the {@link EncodingDefinition} instance from a single {@link EncodingSet} instance.
     *
     * @param encodingSet
     *         The EncodingSet to process
     */
    private void processEncodingSet(final EncodingSet encodingSet) {
        log.log(DEBUG, "Processing EncodingSet {0} with preference weight {1}",
                encodingSet.getClass().getName(), encodingSet.getPreferenceWeight());
        for (EncodingDefinition encodingDefinition : encodingSet.getEncodings()) {
            processEncodingDefinition(encodingDefinition);
        }
    }

    /**
     * Maps an {@link EncodingDefinition} instance from the Firebird encoding and Java charset names. This is skipped
     * if
     * there already is an instance mapped for the Firebird encoding name.
     *
     * @param encodingDefinition
     *         The EncodingDefinition
     */
    private void processEncodingDefinition(final EncodingDefinition encodingDefinition) {
        final String firebirdEncodingName = encodingDefinition.getFirebirdEncodingName();
        final int firebirdCharacterSetId = encodingDefinition.getFirebirdCharacterSetId();
        if (firebirdEncodingToDefinition.containsKey(firebirdEncodingName.toLowerCase(Locale.ROOT))) {
            // We already loaded a definition for this encoding
            log.log(DEBUG, "Skipped loading encoding definition for Firebird encoding {0}, already loaded a definition "
                           + "for that name", firebirdEncodingName);
            return;
        } else if (firebirdCharacterSetId == CS_dynamic) {
            log.log(DEBUG, "Skipped loading encoding definition for Firebird encoding {0}, as it declared itself as "
                           + "the connection character set (FirebirdCharacterSetId 127 or CS_dynamic)",
                    firebirdEncodingName);
            return;
        } else if (firebirdCharacterSetId < 0 || firebirdCharacterSetId > MAX_NORMAL_CHARSET_ID) {
            log.log(WARNING, "Skipped loading encoding definition for Firebird encoding {0}, as it declared itself as "
                             + "FirebirdCharacterSetId {1}, which is outside the range [0, 255]", firebirdEncodingName,
                    firebirdCharacterSetId);
            return;
        }

        // Map firebird encoding and character set id to EncodingDefinition
        firebirdEncodingToDefinition.put(firebirdEncodingName.toLowerCase(Locale.ROOT), encodingDefinition);
        // We don't check whether the characterSetId is already mapped, as a characterSetId should map to a single Firebird encoding
        firebirdCharacterSetIdToDefinition[firebirdCharacterSetId] = encodingDefinition;

        // We are not mapping to Java charset / aliases here; that is delayed until really needed
    }

    private void registerJavaMappingForEncodingDefinition(EncodingDefinition encodingDefinition) {
        final Charset charset = encodingDefinition.getJavaCharset();
        if (encodingDefinition.isInformationOnly() || encodingDefinition.isFirebirdOnly() || charset == null) {
            return;
        }
        final EncodingDefinition currentEncodingDefinition = javaCharsetToDefinition.get(charset);
        if (currentEncodingDefinition == null) {
            // Map Java charset to EncodingDefinition
            javaCharsetToDefinition.put(charset, encodingDefinition);
            javaAliasesToDefinition.put(charset.name().toLowerCase(Locale.ROOT), encodingDefinition);
            for (String charsetAlias : charset.aliases()) {
                javaAliasesToDefinition.put(charsetAlias.toLowerCase(Locale.ROOT), encodingDefinition);
            }
        } else {
            log.log(DEBUG, "Not mapping java charset {0} to Firebird encoding {1}, already mapped to Firebird encoding {2}",
                    charset.name(), encodingDefinition.getEncoding(),
                    currentEncodingDefinition.getFirebirdEncodingName());
        }
    }

    /**
     * Returns the {@link Encoding} from the encodingDefinition. If encodingDefinition is null, information-only or
     * doesn't create an Encoding, it will return the fallbackEncoding. If fallbackEncoding is null, then {@link
     * #getDefaultEncoding()} is used.
     *
     * @param encodingDefinition
     *         EncodingDefinition instance
     * @param fallbackEncoding
     *         Encoding instance for fallback (default encoding for this VM is used if null)
     * @return Encoding instance
     */
    private Encoding returnEncodingOrFallback(EncodingDefinition encodingDefinition, Encoding fallbackEncoding) {
        if (fallbackEncoding == null) {
            fallbackEncoding = getDefaultEncoding();
        }
        if (encodingDefinition == null || encodingDefinition.isInformationOnly()) {
            return fallbackEncoding;
        }
        Encoding encoding = encodingDefinition.getEncoding();
        if (encoding != null) {
            return encoding;
        }
        // We only get here if the EncodingDefinition implementation does not adhere to the contract
        log.log(DEBUG, "EncodingDefinition for Firebird encoding {0} returned null for getEncoding(), using fallback encoding",
                encodingDefinition.getFirebirdEncodingName());
        return fallbackEncoding;
    }

    /**
     * Returns the root instance of EncodingFactory.
     *
     * @return The root instance of EncodingFactory
     * @see #createInstance()
     */
    static EncodingFactory getRootEncodingFactory() {
        return DefaultEncodingFactory.ROOT_ENCODING_FACTORY;
    }

    /**
     * Returns an instance of {@link IEncodingFactory} with the default encoding set to the platform encoding.
     *
     * @return Instance of {@link IEncodingFactory}.
     */
    public static IEncodingFactory getPlatformDefault() {
        return DefaultEncodingFactory.PLATFORM_DEFAULT_INSTANCE;
    }

    /**
     * Returns an {@link org.firebirdsql.encodings.IEncodingFactory} that uses {@code encodingDefinition} as the
     * default.
     * <p>
     * If {@code encodingDefinition} is {@code null} or an information-only encoding, the implementation will
     * return the cached instance returned by {@link #getPlatformDefault()}.
     * </p>
     *
     * @param encodingDefinition
     *         The default encoding to use (or {@code null} to get {@link #getPlatformDefault()}
     * @return IEncodingFactory instance with the specified default.
     */
    public static IEncodingFactory createInstance(EncodingDefinition encodingDefinition) {
        if (encodingDefinition == null || encodingDefinition.isInformationOnly()) {
            return getPlatformDefault();
        }
        return getRootEncodingFactory().withDefaultEncodingDefinition(encodingDefinition);
    }

    /**
     * Returns an {@link IEncodingFactory} that uses an {@link EncodingDefinition} identified
     * by {@code charSet} as the default.
     * <p>
     * If {@code charset} is {@code null}, the implementation will return the cached instance return by
     * {@link #getPlatformDefault()}.
     * </p>
     *
     * @param charset
     *         The default charset to use, or {@code null} for {@link #getPlatformDefault()}.
     * @return IEncodingFactory instance with the specified default.
     */
    public static IEncodingFactory createInstance(Charset charset) {
        if (charset == null) {
            return getPlatformDefault();
        }
        return getRootEncodingFactory().withDefaultEncodingDefinition(charset);
    }

    /**
     * @return A new instance of EncodingFactory
     */
    private static EncodingFactory createInstance() {
        // Process the encoding sets in descending order
        NavigableSet<EncodingSet> encodingSets = loadEncodingSets();
        if (encodingSets.isEmpty()) {
            log.log(WARNING,
                    "No encoding sets were loaded. Make sure at least one valid /META-INF/services/org.firebirdsql.encodings.EncodingSet "
                    + "exists on the classpath (it is normally part of the jaybird jar-file). Falling back to default "
                    + "definition.");
            encodingSets.add(new DefaultEncodingSet());
        }
        return new EncodingFactory(encodingSets.descendingIterator());
    }

    /**
     * Creates a new EncodingFactory based on the supplied encodingSets. The supplied {@link EncodingSet} instances
     * are processed highest preferenceWeight first.
     *
     * @param encodingSets
     *         The EncodingSet definitions to use for the EncodingFactory instance
     * @return EncodingFactory instance based on the supplied encodingSets.
     */
    public static EncodingFactory createInstance(EncodingSet... encodingSets) {
        TreeSet<EncodingSet> sortedEncodingSets = new TreeSet<>(encodingSetComparator());
        // Load the encoding sets and populate the TreeMap
        Collections.addAll(sortedEncodingSets, encodingSets);
        // Process the encoding sets in descending order
        return new EncodingFactory(sortedEncodingSets.descendingIterator());
    }

    /**
     * Resolves the encoding definition by getting the {@code Charset} of the alias, and then retrieving by charset.
     * <p>
     * This is a fallback mechanism if the character set has not been loaded yet.
     * </p>
     *
     * @param charsetAlias Java character set alias
     * @return Encoding definition, or {@code null} if not found
     */
    private EncodingDefinition resolveEncodingDefinitionByCharset(final String charsetAlias) {
        try {
            Charset charset = Charset.forName(charsetAlias);
            return getEncodingDefinitionByCharset(charset);
        } catch (UnsupportedCharsetException | IllegalCharsetNameException e) {
            return null;
        }
    }

    /**
     * Set of the charset name and aliases in lower case.
     *
     * @param charset Character set
     * @return Set of lower case names and aliases
     */
    private static Set<String> toLowerCaseAliasSet(final Charset charset) {
        final Set<String> aliases = charset.aliases();
        final Set<String> potentialNames = new HashSet<>(aliases.size() + 1);
        potentialNames.add(charset.name().toLowerCase(Locale.ROOT));
        for (String alias : aliases) {
            potentialNames.add(alias.toLowerCase(Locale.ROOT));
        }
        return potentialNames;
    }

    private static Comparator<EncodingSet> encodingSetComparator() {
        return Comparator.comparingInt(EncodingSet::getPreferenceWeight);
    }
}
