/*
 * $Id$
 *
 * Firebird Open Source J2ee connector - jdbc driver
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
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.encodings;

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
public final class EncodingFactory {

    private static final Logger log = LoggerFactory.getLogger(EncodingFactory.class, false);

    private static final Charset DEFAULT_CHARSET = Charset.defaultCharset();
    private static final Comparator<EncodingSet> ENCODING_SET_COMPARATOR = new Comparator<EncodingSet>() {
        @Override
        public int compare(final EncodingSet o1, final EncodingSet o2) {
            return o1.getPreferenceWeight() - o2.getPreferenceWeight();
        }
    };

    /**
     * Holder class to ensure static fields of EncodingFactory are properly initialized before the default instance can
     * be loaded/created
     */
    private static class DefaultEncodingFactory {
        private static final EncodingFactory DEFAULT_INSTANCE = createInstance();
    }

    /**
     * Default mapping table, provides an "identity" mapping.
     */
    public static final char[] DEFAULT_MAPPING;

    static {
        DEFAULT_MAPPING = new char[256 * 256];
        for (int i = 0; i < DEFAULT_MAPPING.length; i++) {
            DEFAULT_MAPPING[i] = (char) i;
        }
    }

    private final Map<String, EncodingDefinition> firebirdEncodingToDefinition = new HashMap<String, EncodingDefinition>();
    private final Map<Integer, EncodingDefinition> firebirdCharacterSetIdToDefinition = new HashMap<Integer, EncodingDefinition>();
    private final Map<Charset, EncodingDefinition> javaCharsetToDefinition = new HashMap<Charset, EncodingDefinition>();
    private final Map<String, EncodingDefinition> javaAliasesToDefinition = new HashMap<String, EncodingDefinition>();
    private final Encoding defaultEncoding;
    private final ConcurrentMap<String, CharacterTranslator> translations = new ConcurrentHashMap<String, CharacterTranslator>();

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
        final EncodingDefinition defaultEncodingDefinition = javaCharsetToDefinition.get(DEFAULT_CHARSET);
        if (defaultEncodingDefinition != null && !defaultEncodingDefinition.isInformationOnly()) {
            defaultEncoding = defaultEncodingDefinition.getEncoding();
        } else {
            defaultEncoding = new EncodingGeneric(DEFAULT_CHARSET);
        }
    }

    /**
     * @param firebirdEncodingName
     *         The Firebird encoding name (case insensitive)
     * @return EncodingDefinition instance or <code>null</code> if the encoding name is unknown
     */
    public EncodingDefinition getEncodingDefinitionByFirebirdName(final String firebirdEncodingName) {
        return firebirdEncodingToDefinition.get(firebirdEncodingName.toLowerCase());
    }

    /**
     * Creates an encoding for the specified Firebird encoding. If there is no known encoding for this name,
     * or the loaded EncodingDefinition is information-only, then the fallbackEncoding.
     *
     * @param firebirdEncodingName
     *         The Firebird encoding name (case insensitive)
     * @param fallbackEncoding
     *         The Encoding to use as fallback if no encoding is found (usually the connection encoding). If
     *         <code>null</code>, the defaultEncoding for the JVM is used.
     * @return Encoding instance (never null)
     */
    public Encoding getEncodingForFirebirdName(final String firebirdEncodingName, final Encoding fallbackEncoding) {
        final EncodingDefinition encodingDefinition = getEncodingDefinitionByFirebirdName(firebirdEncodingName);
        return returnEncodingOrFallback(encodingDefinition, fallbackEncoding);
    }

    /**
     * Looks up the EncodingDefinition for the specified Firebird character set id.
     * <p>
     * This implementation will always return <code>null</code> for the value 127, as that is
     * the indicator to use the connection character set, which is unknown to this EncodingFactory.
     * </p>
     *
     * @param firebirdCharacterSetId
     *         Firebird character set id
     * @return EncodingDefinition instance or <code>null</code> if the character set id is unknown or <code>127</code>
     */
    public EncodingDefinition getEncodingDefinitionByCharacterSetId(final int firebirdCharacterSetId) {
        if (firebirdCharacterSetId == 127) {
            // Value 127 indicates the connection character set is to be used
            // Explicitly returning null to prevent user defined encoding definitions from messing up
            return null;
        }
        return firebirdCharacterSetIdToDefinition.get(firebirdCharacterSetId);
    }

    /**
     * Creates an encoding for the specified Firebird  character set id. If there is no known encoding for this
     * character set id (or if it is 127, see {@link #getEncodingDefinitionByCharacterSetId(int)}),
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
        final EncodingDefinition encodingDefinition = getEncodingDefinitionByCharacterSetId(firebirdCharacterSetId);
        return returnEncodingOrFallback(encodingDefinition, fallbackEncoding);
    }

    /**
     * @param charset
     *         The Java character set
     * @return EncodingDefinition instance or <code>null</code> if the character set is not mapped
     */
    public EncodingDefinition getEncodingDefinitionByCharset(final Charset charset) {
        return javaCharsetToDefinition.get(charset);
    }

    /**
     * Creates an encoding for the specified Java character set. If there is no known encoding for this name,
     * or the loaded EncodingDefinition is information-only, then the fallbackEncoding will be used.
     *
     * @param charset
     *         The Java character set
     * @param fallbackEncoding
     *         The Encoding to use as fallback if no encoding is found (usually the connection encoding). If
     *         <code>null</code>, the defaultEncoding for the JVM is used.
     * @return Encoding instance (never null)
     * @see #getEncodingForCharset(java.nio.charset.Charset)
     */
    public Encoding getEncodingForCharset(final Charset charset, final Encoding fallbackEncoding) {
        final EncodingDefinition encodingDefinition = getEncodingDefinitionByCharset(charset);
        return returnEncodingOrFallback(encodingDefinition, fallbackEncoding);
    }

    public EncodingDefinition getEncodingDefinitionByCharsetAlias(String charsetAlias) {
        if (charsetAlias == null) return null;
        return javaAliasesToDefinition.get(charsetAlias.toLowerCase());
    }

    public Encoding getEncodingForCharsetAlias(final String charsetAlias, final Encoding fallbackEncoding) {
        final EncodingDefinition encodingDefinition = getEncodingDefinitionByCharsetAlias(charsetAlias);
        return returnEncodingOrFallback(encodingDefinition, fallbackEncoding);
    }

    /**
     * Creates an encoding for the specified Java character set. If there is no known encoding for this name,
     * or the loaded EncodingDefinition is information-only, then an Encoding instance based on the charset is
     * returned.
     * <p>
     * In general the method {@link #getEncodingForCharset(java.nio.charset.Charset, Encoding)} should be used.
     * </p>
     *
     * @param charset
     *         The Java character set
     * @return Encoding instance (never null)
     * @see #getEncodingForCharset(java.nio.charset.Charset, Encoding)
     */
    public Encoding getEncodingForCharset(final Charset charset) {
        return getEncodingForCharset(charset, new EncodingGeneric(charset));
    }

    /**
     * Loads the {@link EncodingSet} instances from the classpath (using {@link ServiceLoader}).
     *
     * @see EncodingSet
     */
    private static NavigableSet<EncodingSet> loadEncodingDefinitions() {
        final TreeSet<EncodingSet> encodingSets = new TreeSet<EncodingSet>(ENCODING_SET_COMPARATOR);
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
                log.debug(String.format("Skipped loading encoding for Firebird encoding %s, already loaded a definition for that name", firebirdEncodingName));
            return;
        }

        // Map firebird encoding and character set id to EncodingDefinition
        firebirdEncodingToDefinition.put(firebirdEncodingName.toLowerCase(), encodingDefinition);
        // We don't check whether the characterSetId is already mapped, as a characterSetId should map to a single Firebird encoding
        // TODO Consider if wise, we now allow users to accidentally or intentionally mess up the mapping
        firebirdCharacterSetIdToDefinition.put(encodingDefinition.getFirebirdCharacterSetId(), encodingDefinition);

        final Charset charset = encodingDefinition.getJavaCharset();
        if (!encodingDefinition.isInformationOnly() && !encodingDefinition.isFirebirdOnly() && charset != null) {
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
    }

    /**
     * Gets an instance of {@link CharacterTranslator} for the specified mappingPath.
     *
     * @param mappingPath
     *         Path of the file with mapping definition
     * @return Instance of CharacterTranslator
     * @throws SQLException
     */
    public CharacterTranslator getCharacterTranslator(String mappingPath) throws SQLException {
        CharacterTranslator translator = translations.get(mappingPath);
        if (translator != null) {
            return translator;
        }

        translator = new CharacterTranslator();
        translator.init(mappingPath);
        translations.putIfAbsent(mappingPath, translator);

        return translations.get(mappingPath);
    }

    /**
     * Returns the {@link Encoding} from the encodingDefinition. If encodingDefinition is null, information-only or
     * doesn't create an Encoding, it will return the fallbackEncoding. If fallbackEncoding is null, then {@link
     * #defaultEncoding} is used.
     *
     * @param encodingDefinition
     *         EncodingDefinition instance
     * @param fallbackEncoding
     *         Encoding instance for fallback (default encoding for this VM is used if null)
     * @return Encoding instance
     */
    private Encoding returnEncodingOrFallback(EncodingDefinition encodingDefinition, Encoding fallbackEncoding) {
        if (fallbackEncoding == null) {
            fallbackEncoding = defaultEncoding;
        }
        if (encodingDefinition != null && !encodingDefinition.isInformationOnly()) {
            Encoding encoding = encodingDefinition.getEncoding();
            if (encoding == null) {
                if (log.isDebugEnabled())
                    log.debug(String.format("EncodingDefinition for Firebird encoding %s returned null for getEncoding(), using fallback encoding",
                            encodingDefinition.getFirebirdEncodingName()));
                encoding = fallbackEncoding;
            }
            return encoding;
        }
        return fallbackEncoding;
    }

    /**
     * Returns the default instance of EncodingFactory.
     * <p>
     * The default instance is loaded when the EncodingFactory class is first loaded.
     * </p>
     *
     * @return The default instance of EncodingFactory
     * @see #createInstance()
     */
    public static EncodingFactory getDefaultInstance() {
        return DefaultEncodingFactory.DEFAULT_INSTANCE;
    }

    /**
     * @return A new instance of EncodingFactory
     */
    public static EncodingFactory createInstance() {
        // Process the encoding sets in descending order
        return new EncodingFactory(loadEncodingDefinitions().descendingIterator());
    }

    /**
     * Creates a new EncodingFactory based on the supplied encodingSets. The supplied {@link EncodingSet} instances
     * are processed
     * highest preferenceWeight first.
     *
     * @param encodingSets
     *         The EncodingSet definitions to use for the EncodingFactory instance
     * @return EncodingFactory instance based on the supplied encodingSets.
     */
    public static EncodingFactory createInstance(EncodingSet... encodingSets) {
        TreeSet<EncodingSet> sortedEncodingSets = new TreeSet<EncodingSet>(ENCODING_SET_COMPARATOR);
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
        final EncodingDefinition encodingDefinition = getDefaultInstance().getEncodingDefinitionByCharacterSetId(characterSetId);
        return encodingDefinition != null ? encodingDefinition.getMaxBytesPerChar() : 1;
    }

    /**
     * Gets an {@link Encoding} instance for the supplied java Charset name.
     *
     * @param encoding
     *         Java Charset name
     * @return Instance of {@link Encoding}
     * @deprecated Use {@link #getEncodingForCharsetAlias(String, Encoding)}
     */
    @Deprecated
    public static Encoding getEncoding(String encoding) {
        return getDefaultInstance().getEncodingForCharsetAlias(encoding, null);
    }

    /**
     * Gets an {@link Encoding} instance for the supplied java Charset.
     *
     * @param charset
     *         Java Charset
     * @return Instance of {@link Encoding}
     * @deprecated Use {@link #getEncodingForCharset(java.nio.charset.Charset, Encoding)} or {@link
     *             #getEncodingForCharset(java.nio.charset.Charset)}
     */
    @Deprecated
    public static Encoding getEncoding(Charset charset) {
        return getDefaultInstance().getEncodingForCharset(charset);
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
    public static Encoding getEncoding(String encoding, String mappingPath) throws SQLException {
        EncodingDefinition encodingDefinition = getDefaultInstance().getEncodingDefinitionByCharsetAlias(encoding);
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
    public static Encoding getEncoding(Charset charset, String mappingPath) throws SQLException {
        if (mappingPath == null) {
            return getEncoding(charset);
        }
        CharacterTranslator translator = getTranslator(mappingPath);
        return getEncoding(charset).withTranslation(translator);
    }

    /**
     * Get Firebird encoding for given Java language encoding.
     *
     * @param javaEncoding
     *         Java language encoding.
     * @return corresponding Firebird encoding or <code>null</code> if none
     *         found.
     * @deprecated Use {@link #getEncodingDefinitionByCharsetAlias(String)}
     */
    @Deprecated
    public static String getIscEncoding(String javaEncoding) {
        final EncodingDefinition encodingDefinition = getDefaultInstance().getEncodingDefinitionByCharsetAlias(javaEncoding);
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
        final EncodingDefinition encodingDefinition = getDefaultInstance().getEncodingDefinitionByCharset(javaCharset);
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
        final EncodingDefinition encodingDefinition = getDefaultInstance().getEncodingDefinitionByFirebirdName(iscEncoding);
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
        final EncodingDefinition encodingDefinition = getDefaultInstance().getEncodingDefinitionByFirebirdName(iscEncoding);

        // TODO: Test the claim below
        // very important for performance
        // if javaEncoding is the default one, set to null
        if (encodingDefinition == null || DEFAULT_CHARSET.equals(encodingDefinition.getJavaCharset())) {
            return null;
        }
        return encodingDefinition.getJavaEncodingName();
    }

    /**
     * Get size of a character for the specified Java encoding, as it would be
     * encoded in Firebird.
     *
     * @param javaCharset
     *         Java Charset
     * @return maximum size of the character in bytes or 1 if encoding was not
     *         found.
     * @deprecated Use {@link #getEncodingDefinitionByCharset(java.nio.charset.Charset)} and {@link
     *             EncodingDefinition#getMaxBytesPerChar()}
     */
    @Deprecated
    public static int getJavaEncodingSize(Charset javaCharset) {
        final EncodingDefinition encodingDefinition = getDefaultInstance().getEncodingDefinitionByCharset(javaCharset);
        return encodingDefinition != null ? encodingDefinition.getMaxBytesPerChar() : 1;
    }

    /**
     * Get size of a character for the specified Java encoding, as it would be
     * encoded in Firebird.
     *
     * @param javaEncoding
     *         Java Charset name
     * @return maximum size of the character in bytes or 1 if encoding was not
     *         found.
     * @deprecated Use {@link #getEncodingDefinitionByCharsetAlias(String)} and {@link
     *             EncodingDefinition#getMaxBytesPerChar()}
     */
    @Deprecated
    public static int getJavaEncodingSize(String javaEncoding) {
        final EncodingDefinition encodingDefinition = getDefaultInstance().getEncodingDefinitionByCharsetAlias(javaEncoding);
        return encodingDefinition != null ? encodingDefinition.getMaxBytesPerChar() : 1;
    }

    /**
     * Get Java language encoding for a given Java encoding alias.
     * <p>
     * Ensures that naming is consistent even if a different alias was used.
     * </p>
     *
     * @param javaAlias
     *         Java alias for the encoding
     * @return
     * @deprecated Use {@link #getEncodingDefinitionByCharsetAlias(String)} and {@link
     *             EncodingDefinition#getJavaEncodingName()}
     */
    @Deprecated
    public static String getJavaEncodingForAlias(String javaAlias) {
        final EncodingDefinition encodingDefinition = getDefaultInstance().getEncodingDefinitionByCharsetAlias(javaAlias);
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
     * @deprecated Use {@link #getCharacterTranslator(String)}
     */
    @Deprecated
    public static CharacterTranslator getTranslator(String mappingPath) throws SQLException {
        return getDefaultInstance().getCharacterTranslator(mappingPath);
    }
}
