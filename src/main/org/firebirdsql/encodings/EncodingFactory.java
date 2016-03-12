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

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Factory for {@link EncodingDefinition} and {@link Encoding}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public final class EncodingFactory implements IEncodingFactory {

    private static final Logger log = LoggerFactory.getLogger(EncodingFactory.class);

    private static final Charset DEFAULT_CHARSET = Charset.defaultCharset();
    private static final Comparator<EncodingSet> ENCODING_SET_COMPARATOR = new Comparator<EncodingSet>() {
        @Override
        public int compare(final EncodingSet o1, final EncodingSet o2) {
            return o1.getPreferenceWeight() - o2.getPreferenceWeight();
        }
    };

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

    private final Map<String, EncodingDefinition> firebirdEncodingToDefinition = new HashMap<>();
    private final Map<Integer, EncodingDefinition> firebirdCharacterSetIdToDefinition = new HashMap<>();
    private final Map<Charset, EncodingDefinition> javaCharsetToDefinition = new HashMap<>();
    private final Map<String, EncodingDefinition> javaAliasesToDefinition = new HashMap<>();
    private final Encoding defaultEncoding;
    private final EncodingDefinition defaultEncodingDefinition;
    private final ConcurrentMap<String, CharacterTranslator> translations = new ConcurrentHashMap<>();

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
        final EncodingDefinition candidateDefinition = javaCharsetToDefinition.get(DEFAULT_CHARSET);

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
        return firebirdEncodingName != null ? firebirdEncodingToDefinition.get(firebirdEncodingName.toLowerCase()) : null;
    }

    /**
     * Gets an {@link org.firebirdsql.encodings.Encoding} for the specified Firebird encoding. If there is no known
     * encoding for this name, or the loaded EncodingDefinition is information-only, then the fallbackEncoding.
     *
     * @param firebirdEncodingName
     *         The Firebird encoding name (case insensitive)
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
        int charsetId = firebirdCharacterSetId & 0xFF;
        if (charsetId == ISCConstants.CS_dynamic) {
            // Value CS_dynamic (127) indicates the connection character set is to be used
            // Explicitly returning null to prevent user defined encoding definitions from messing up
            return null;
        }
        return firebirdCharacterSetIdToDefinition.get(charsetId);
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
        return javaCharsetToDefinition.get(charset);
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
    public EncodingDefinition getEncodingDefinitionByCharsetAlias(String charsetAlias) {
        return charsetAlias != null ? javaAliasesToDefinition.get(charsetAlias.toLowerCase()) : null;
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
    public CharacterTranslator getCharacterTranslator(String mappingPath) throws SQLException {
        if (mappingPath == null) return null;
        CharacterTranslator translator = translations.get(mappingPath);
        if (translator != null) {
            return translator;
        }

        translator = CharacterTranslator.create(mappingPath);
        translations.putIfAbsent(mappingPath, translator);

        return translations.get(mappingPath);
    }

    @Override
    public EncodingDefinition getEncodingDefinition(final String firebirdEncodingName, final String javaCharsetAlias) {
        try {
            EncodingDefinition encodingDefinition = null;
            Charset charset = null;
            // TODO Consider returning getDefaultEncodingDefinition() if both firebirdEncodingName and javaCharsetAlias are NULL
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
                // TODO Consider throwing exception if no EncodingDefinition is found
                return null;
            } else if (!encodingDefinition.isInformationOnly() && (charset == null || encodingDefinition.getJavaCharset().equals(charset))) {
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
            // TODO Consider throwing exception if no EncodingDefinition is found
            return null;
        } catch (Exception e) {
            log.debug(String.format("Exception looking up encoding definition for firebirdEncodingName %s, javaCharsetAlias %s", firebirdEncodingName, javaCharsetAlias), e);
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
        return new ConnectionEncodingFactory(this,
                encodingDefinition != null && !encodingDefinition.isInformationOnly()
                        ? encodingDefinition
                        : getDefaultEncodingDefinition());
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation returns an instance of {@link ConnectionEncodingFactory}.
     * </p>
     */
    @Override
    public IEncodingFactory withDefaultEncodingDefinition(Charset charset) {
        return new ConnectionEncodingFactory(this, getEncodingDefinitionByCharset(charset));
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
        final TreeSet<EncodingSet> encodingSets = new TreeSet<>(ENCODING_SET_COMPARATOR);
        final ServiceLoader<EncodingSet> encodingSetLoader = ServiceLoader.load(EncodingSet.class, EncodingFactory.class.getClassLoader());
        // Load the encoding sets and populate the TreeMap
        for (final EncodingSet encodingSet : encodingSetLoader) {
            encodingSets.add(encodingSet);
        }
        return encodingSets;
    }

    /**
     * Loads the {@link EncodingDefinition} instance from a single {@link EncodingSet} instance.
     *
     * @param encodingSet
     *         The EncodingSet to process
     */
    private void processEncodingSet(final EncodingSet encodingSet) {
        if (log.isDebugEnabled())
            log.debug(String.format("Processing EncodingSet %s with preference weight %d", encodingSet.getClass().getName(), encodingSet.getPreferenceWeight()));
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
        if (firebirdEncodingToDefinition.containsKey(firebirdEncodingName.toLowerCase())) {
            // We already loaded a definition for this encoding
            if (log.isDebugEnabled())
                log.debug(String.format("Skipped loading encoding definition for Firebird encoding %s, already loaded a definition for that name", firebirdEncodingName));
            return;
        } else if (encodingDefinition.getFirebirdCharacterSetId() == ISCConstants.CS_dynamic) {
            if (log.isDebugEnabled())
                log.debug(String.format("Skipped loading encoding definition for Firebird encoding %s, as it declared itself as the connection character set (FirebirdCharacterSetId 127 or CS_dynamic)", firebirdEncodingName));
            return;
        }

        // Map firebird encoding and character set id to EncodingDefinition
        firebirdEncodingToDefinition.put(firebirdEncodingName.toLowerCase(), encodingDefinition);
        // We don't check whether the characterSetId is already mapped, as a characterSetId should map to a single Firebird encoding
        // TODO Consider if wise, we now allow users to accidentally or intentionally mess up the mapping
        firebirdCharacterSetIdToDefinition.put(encodingDefinition.getFirebirdCharacterSetId(), encodingDefinition);

        final Charset charset = encodingDefinition.getJavaCharset();
        if (encodingDefinition.isInformationOnly() || encodingDefinition.isFirebirdOnly() || charset == null) {
            return;
        }
        if (javaCharsetToDefinition.containsKey(charset)) {
            if (log.isDebugEnabled())
                log.debug(String.format("Not mapping java charset %s to Firebird encoding %s, already mapped to Firebird encoding %s",
                        charset.name(), firebirdEncodingName, javaCharsetToDefinition.get(charset).getFirebirdEncodingName()));
        } else {
            // Map Java charset to EncodingDefinition
            javaCharsetToDefinition.put(charset, encodingDefinition);
            javaAliasesToDefinition.put(charset.name().toLowerCase(), encodingDefinition);
            for (String charsetAlias : charset.aliases()) {
                javaAliasesToDefinition.put(charsetAlias.toLowerCase(), encodingDefinition);
            }
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
        if (log.isDebugEnabled()) {
            log.debug(String.format("EncodingDefinition for Firebird encoding %s returned null for getEncoding(), using fallback encoding",
                    encodingDefinition.getFirebirdEncodingName()));
        }
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
        return new EncodingFactory(loadEncodingSets().descendingIterator());
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
        TreeSet<EncodingSet> sortedEncodingSets = new TreeSet<>(ENCODING_SET_COMPARATOR);
        // Load the encoding sets and populate the TreeMap
        Collections.addAll(sortedEncodingSets, encodingSets);
        // Process the encoding sets in descending order
        return new EncodingFactory(sortedEncodingSets.descendingIterator());
    }

    /**
     * Get size of a character for the specified character set.
     *
     * @param characterSetId
     *         of the character set.
     * @return maximum size of the character in bytes or 1 if charset was not
     *         found.
     * @deprecated Use the information provided by the {@link EncodingDefinition} returned by {@link
     *             #getEncodingDefinitionByCharacterSetId(int)}
     */
    @Deprecated
    public static int getCharacterSetSize(int characterSetId) {
        final EncodingDefinition encodingDefinition = getRootEncodingFactory()
                .getEncodingDefinitionByCharacterSetId(characterSetId);
        return encodingDefinition != null ? encodingDefinition.getMaxBytesPerChar() : 1;
    }

    /**
     * Gets an {@link Encoding} instance for the supplied java Charset name.
     *
     * @param javaCharsetAlias
     *         Java Charset name
     * @return Instance of {@link Encoding}
     * @deprecated Use {@link #getEncodingForCharsetAlias(String, Encoding)}
     */
    @Deprecated
    public static Encoding getEncoding(String javaCharsetAlias) {
        return getRootEncodingFactory().getEncodingForCharsetAlias(javaCharsetAlias, null);
    }

    /**
     * Gets an {@link Encoding} instance for the supplied java Charset.
     *
     * @param charset
     *         Java Charset
     * @return Instance of {@link Encoding}
     * @deprecated Use {@link #getEncodingForCharset(java.nio.charset.Charset, Encoding)} or {@link
     *             #getOrCreateEncodingForCharset(java.nio.charset.Charset)}
     */
    @Deprecated
    public static Encoding getEncoding(Charset charset) {
        return getRootEncodingFactory().getOrCreateEncodingForCharset(charset);
    }

    /**
     * Gets an {@link Encoding} instance for the supplied java Charset name and
     * alternative character mapping.
     *
     * @param encoding
     *         Java Charset name
     * @param mappingPath
     *         Resource file with alternative character mapping
     * @return Instance of {@link Encoding}
     * @deprecated Use {@link #getEncodingForCharsetAlias(String, Encoding)} and {@link
     *             Encoding#withTranslation(CharacterTranslator)}
     */
    @Deprecated
    @SuppressWarnings("deprecation")
    public static Encoding getEncoding(String encoding, String mappingPath) throws SQLException {
        EncodingDefinition encodingDefinition = getRootEncodingFactory().getEncodingDefinitionByCharsetAlias(encoding);
        // TODO Express this in terms of other methods of this factory?

        Charset charset = null;
        if (encodingDefinition != null) {
            charset = encodingDefinition.getJavaCharset();
        }
        if (charset == null) {
            charset = DEFAULT_CHARSET;
        }
        return getEncoding(charset, mappingPath);
    }

    /**
     * Gets an {@link Encoding} instance for the supplied java Charset and
     * alternative character mapping.
     *
     * @param charset
     *         Java Charset
     * @param mappingPath
     *         Resource file with alternative character mapping
     * @return Instance of {@link Encoding}
     * @deprecated Use {@link #getEncodingForCharset(java.nio.charset.Charset, Encoding)} and {@link
     *             Encoding#withTranslation(CharacterTranslator)}
     */
    @Deprecated
    @SuppressWarnings("deprecation")
    public static Encoding getEncoding(Charset charset, String mappingPath) throws SQLException {
        final Encoding encoding = getEncoding(charset);
        return mappingPath == null ? encoding : encoding.withTranslation(getTranslator(mappingPath));
    }

    /**
     * Get Firebird encoding for given Java language encoding.
     *
     * @param javaCharsetAlias
     *         Java language encoding.
     * @return corresponding Firebird encoding or <code>null</code> if none
     *         found.
     * @deprecated Use {@link #getEncodingDefinitionByCharsetAlias(String)}
     */
    @Deprecated
    @SuppressWarnings("deprecation")
    public static String getIscEncoding(String javaCharsetAlias) {
        final EncodingDefinition encodingDefinition = getRootEncodingFactory()
                .getEncodingDefinitionByCharsetAlias(javaCharsetAlias);
        return encodingDefinition != null ? encodingDefinition.getFirebirdEncodingName() : null;
    }

    /**
     * Get Firebird encoding for given Java Charset.
     *
     * @param javaCharset
     *         Java Charset
     * @return corresponding Firebird encoding or <code>null</code> if none
     *         found.
     * @deprecated Use {@link #getEncodingDefinitionByCharset(java.nio.charset.Charset)}
     */
    @Deprecated
    public static String getIscEncoding(Charset javaCharset) {
        final EncodingDefinition encodingDefinition = getRootEncodingFactory()
                .getEncodingDefinitionByCharset(javaCharset);
        return encodingDefinition != null ? encodingDefinition.getFirebirdEncodingName() : null;
    }

    /**
     * Get size of a character for the specified Firebird encoding.
     *
     * @param iscEncoding
     *         Firebird encoding.
     * @return maximum size of the character in bytes or 1 if encoding was not
     *         found.
     * @deprecated Use {@link #getEncodingDefinitionByFirebirdName(String)} and {@link EncodingDefinition#getMaxBytesPerChar()}
     */
    @Deprecated
    public static int getIscEncodingSize(String iscEncoding) {
        if (iscEncoding == null) {
            return 1;
        }
        final EncodingDefinition encodingDefinition = getRootEncodingFactory()
                .getEncodingDefinitionByFirebirdName(iscEncoding);
        return encodingDefinition != null ? encodingDefinition.getMaxBytesPerChar() : 1;
    }

    /**
     * Get Java language encoding for given Firebird encoding.
     *
     * @param iscEncoding
     *         Firebird encoding
     * @return corresponding Java encoding or <code>null</code> if none found.
     *         Use {@link #getEncodingDefinitionByFirebirdName(String)} and {@link org.firebirdsql.encodings.EncodingDefinition#getJavaEncodingName()}
     */
    @Deprecated
    public static String getJavaEncoding(String iscEncoding) {
        if (iscEncoding == null) {
            return null;
        }
        final EncodingDefinition encodingDefinition = getRootEncodingFactory()
                .getEncodingDefinitionByFirebirdName(iscEncoding);

        // TODO: Test the claim below
        // very important for performance
        // if javaEncoding is the default one, set to null
        if (encodingDefinition == null || DEFAULT_CHARSET.equals(encodingDefinition.getJavaCharset())) {
            return null;
        }
        return encodingDefinition.getJavaEncodingName();
    }

    /**
     * Get Java language encoding for a given Java encoding alias.
     * <p>
     * Ensures that naming is consistent even if a different alias was used.
     * </p>
     *
     * @param javaAlias
     *         Java alias for the encoding
     * @return Java Charset name
     * @deprecated Use {@link #getEncodingDefinitionByCharsetAlias(String)} and {@link
     *             EncodingDefinition#getJavaEncodingName()}
     */
    @Deprecated
    public static String getJavaEncodingForAlias(String javaAlias) {
        final EncodingDefinition encodingDefinition = getRootEncodingFactory()
                .getEncodingDefinitionByCharsetAlias(javaAlias);
        // TODO: Test the claim below
        // very important for performance
        // if javaEncoding is the default one, set to null
        if (encodingDefinition == null || DEFAULT_CHARSET.equals(encodingDefinition.getJavaCharset())) {
            return null;
        }
        return encodingDefinition.getJavaEncodingName();
    }

    /**
     * Gets the {@link CharacterTranslator} for the specified mappingPath, or <code>null</code> if there is no such
     * mappingPath
     *
     * @param mappingPath
     *         Path of the mapping definition file
     * @return CharacterTranslator or <code>null</code>
     * @throws SQLException
     */
    public static CharacterTranslator getTranslator(String mappingPath) throws SQLException {
        return getRootEncodingFactory().getCharacterTranslator(mappingPath);
    }
}
