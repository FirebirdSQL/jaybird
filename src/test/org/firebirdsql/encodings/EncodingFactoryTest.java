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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static java.lang.String.format;
import static org.firebirdsql.gds.ISCConstants.CS_NONE;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Tests for {@link EncodingFactory}
 *
 * @author Mark Rotteveel
 */
class EncodingFactoryTest {

    /**
     * Java alias for FIREBIRD_TEST_CHARSET
     */
    private static final String JAVA_TEST_CHARSET = "ISO-8859-7";
    /**
     * Firebird alias for JAVA_TEST_CHARSET
     */
    private static final String FIREBIRD_TEST_CHARSET = "ISO8859_7";

    private final EncodingFactory root = EncodingFactory.getRootEncodingFactory();

    /**
     * Tests if all aliases of a supported Java character set are mapped
     * to the same Firebird character set.
     */
    @Test
    void javaCharsetAliasMapping() {
        Charset charset = Charset.forName(JAVA_TEST_CHARSET);
        Set<String> allJavaAliases = new HashSet<>();
        allJavaAliases.add(JAVA_TEST_CHARSET);
        // Canonical name is not included in aliases
        allJavaAliases.add(charset.name());
        allJavaAliases.addAll(charset.aliases());

        Set<String> aliasesMatched = new HashSet<>();
        for (String javaAlias : allJavaAliases) {
            String iscEncoding = root.getEncodingDefinitionByCharsetAlias(javaAlias).getFirebirdEncodingName();
            if (FIREBIRD_TEST_CHARSET.equals(iscEncoding)) {
                aliasesMatched.add(javaAlias);
            }
        }

        assertEquals(allJavaAliases, aliasesMatched,
                format("One or more aliases of %s did not match expected Firebird character set %s", JAVA_TEST_CHARSET, FIREBIRD_TEST_CHARSET));
    }

    /**
     * Tests if the alias mapping is case-insensitive.
     */
    @Test
    void aliasMappingJavaToFbCaseInsensitive() {
        assumeTrue(FIREBIRD_TEST_CHARSET.equals(root
                .getEncodingDefinitionByCharsetAlias(JAVA_TEST_CHARSET).getFirebirdEncodingName()));

        String differentCasedJavaAlias = JAVA_TEST_CHARSET.toUpperCase(Locale.ROOT);
        if (differentCasedJavaAlias.equals(JAVA_TEST_CHARSET)) {
            differentCasedJavaAlias = JAVA_TEST_CHARSET.toLowerCase(Locale.ROOT);
        }

        assertEquals(FIREBIRD_TEST_CHARSET,
                root.getEncodingDefinitionByCharsetAlias(differentCasedJavaAlias).getFirebirdEncodingName(),
                "Unexpected FB character set for differently cased Java alias");
    }

    @Test
    void aliasMappingFbToJavaCaseInsensitive() {
        assumeTrue(JAVA_TEST_CHARSET.equals(root.getEncodingDefinitionByFirebirdName(FIREBIRD_TEST_CHARSET)
                .getJavaEncodingName()));

        String differentCasedFbAlias = FIREBIRD_TEST_CHARSET.toUpperCase(Locale.ROOT);
        if (differentCasedFbAlias.equals(FIREBIRD_TEST_CHARSET)) {
            differentCasedFbAlias = FIREBIRD_TEST_CHARSET.toLowerCase(Locale.ROOT);
        }

        assertEquals(JAVA_TEST_CHARSET,
                root.getEncodingDefinitionByFirebirdName(differentCasedFbAlias).getJavaEncodingName(),
                "Unexpected Java character set for differently cased FB alias");
    }

    @Test
    void testCreateInstance_Custom() {
        final String dos866Firebird = "DOS866";
        final int dos866Id = 48;
        final String dos866Java = "Cp866";
        final Charset dos866Charset = Charset.forName(dos866Java);
        final EncodingDefinition testEncodingDefinition = new DefaultEncodingDefinition(dos866Firebird,
                Charset.forName(dos866Java), 1, dos866Id, false);
        final Encoding testEncoding = testEncodingDefinition.getEncoding();
        EncodingSet encodingSet = createEncodingSet(0, testEncodingDefinition);

        EncodingFactory factory = EncodingFactory.createInstance(encodingSet);

        assertSame(testEncodingDefinition, factory.getEncodingDefinitionByFirebirdName(dos866Firebird),
                "Unexpected EncodingDefinition by firebird name");
        assertSame(testEncoding, factory.getEncodingForFirebirdName(dos866Firebird),
                "Unexpected encoding by firebird name");
        assertSame(testEncodingDefinition, factory.getEncodingDefinitionByCharacterSetId(dos866Id),
                "Unexpected EncodingDefinition by character set id");
        assertSame(testEncoding, factory.getEncodingForCharacterSetId(dos866Id),
                "Unexpected encoding by character set id");
        assertSame(testEncodingDefinition, factory.getEncodingDefinitionByCharset(dos866Charset),
                "Unexpected EncodingDefinition by charset");
        assertSame(testEncoding, factory.getOrCreateEncodingForCharset(dos866Charset),
                "Unexpected encoding by charset");
        assertSame(testEncoding, factory.getEncodingForCharset(dos866Charset), "Unexpected encoding by charset");
        assertSame(testEncodingDefinition, factory.getEncodingDefinitionByCharsetAlias(dos866Java),
                "Unexpected EncodingDefinition by java charset alias");
        assertSame(testEncoding, factory.getEncodingForCharsetAlias(dos866Java),
                "Unexpected encoding by java charset alias");
    }

    /**
     * Tests if retrieving info for {@link ISCConstants#CS_dynamic} returns the right result.
     */
    @Test
    void testCharacterSetId_CS_dynamic() {
        assertNull(root.getEncodingDefinitionByCharacterSetId(ISCConstants.CS_dynamic),
                "Expected null EncodingDefinition for CS_dynamic (127)");
        assertSame(root.getDefaultEncoding(), root.getEncodingForCharacterSetId(ISCConstants.CS_dynamic),
                "Expected the default encoding for CS_dynamic (127)");
    }

    /**
     * Tests creating an {@link EncodingFactory} from an {@link EncodingSet} containing a definition for firebird
     * character set id {@link ISCConstants#CS_dynamic} (127), this {@link EncodingDefinition} should not be part of the
     * factory.
     */
    @Test
    void testDefinition_CS_dynamic_skipped() {
        final EncodingSet encodingSet = createEncodingSet(0,
                new DefaultEncodingDefinition("TEST1", (Charset) null, 1, 10, true),
                new DefaultEncodingDefinition("TEST2", (Charset) null, 1, ISCConstants.CS_dynamic, true));
        final IEncodingFactory factory = EncodingFactory.createInstance(encodingSet);

        assertNotNull(factory.getEncodingDefinitionByFirebirdName("TEST1"),
                "Expected EncodingDefinition for TEST1 to be returned");
        assertNull(factory.getEncodingDefinitionByFirebirdName("TEST2"),
                "Expected EncodingDefinition for TEST2 to be null (as it was declared with character set id 127)");
    }

    @Test
    void testCreateInstance_MultipleEncodingSets() {
        final EncodingDefinition test0_1 = new DefaultEncodingDefinition("TEST1", Charset.forName("Cp1255"), 1, 5, false);
        final EncodingDefinition test0_2 = new DefaultEncodingDefinition("TEST2", (Charset) null, 1, 6, true);
        final EncodingDefinition test1_1 = new DefaultEncodingDefinition("TEST1", (Charset) null, 1, 5, true);
        final EncodingDefinition test1_3 = new DefaultEncodingDefinition("TEST3", Charset.forName("Cp1256"), 1, 7, false);
        // Uses same Charset as test1_3, both are firebirdOnly = false
        final EncodingDefinition test0_4 = new DefaultEncodingDefinition("TEST4", Charset.forName("Cp1256"), 1, 8, false);
        final EncodingDefinition test1_5 = new DefaultEncodingDefinition("TEST5", StandardCharsets.UTF_8, 1, 9, true);
        // Uses same Charset as test1_5, but 1_5 is firebirdOnly = true
        final EncodingDefinition test0_6 = new DefaultEncodingDefinition("TEST6", StandardCharsets.UTF_8, 1, 10, false);
        final EncodingDefinition test0_7 = new DefaultEncodingDefinition("TEST7", StandardCharsets.US_ASCII, 1, 11, true);
        final EncodingSet encodingSet0 = createEncodingSet(0, test0_1, test0_2, test0_4, test0_6, test0_7);
        final EncodingSet encodingSet1 = createEncodingSet(1, test1_1, test1_3, test1_5);

        final IEncodingFactory factory = EncodingFactory.createInstance(encodingSet0, encodingSet1);

        assertSame(test1_1, factory.getEncodingDefinitionByFirebirdName("TEST1"),
                "Expected EncodingDefinition 1 from set 1 for name TEST1");
        assertSame(test0_2, factory.getEncodingDefinitionByFirebirdName("TEST2"),
                "Expected EncodingDefinition 2 from set 0 for name TEST2");
        assertSame(test1_3, factory.getEncodingDefinitionByFirebirdName("TEST3"),
                "Expected EncodingDefinition 3 from set 1 for name TEST3");
        assertSame(test0_4, factory.getEncodingDefinitionByFirebirdName("TEST4"),
                "Expected EncodingDefinition 4 from set 0 for name TEST4");
        assertSame(test1_5, factory.getEncodingDefinitionByFirebirdName("TEST5"),
                "Expected EncodingDefinition 5 from set 1 for name TEST5");
        assertSame(test0_6, factory.getEncodingDefinitionByFirebirdName("TEST6"),
                "Expected EncodingDefinition 6 from set 0 for name TEST4");
        assertSame(test0_7, factory.getEncodingDefinitionByFirebirdName("TEST7"),
                "Expected EncodingDefinition 7 from set 0 for name TEST4");
        assertNull(factory.getEncodingDefinitionByCharset(Charset.forName("Cp1255")),
                "Expected no EncodingDefinition for Charset Cp1255, as the definition from set 0 should not be loaded");
        assertSame(test1_3, factory.getEncodingDefinitionByCharset(Charset.forName("Cp1256")),
                "Expected EncodingDefinition 3 from set 1 for Charset Cp1256 as it was loaded before EncodingDefinition 4 from set 0 (and both are firebirdOnly = false)");
        assertSame(test0_6, factory.getEncodingDefinitionByCharset(StandardCharsets.UTF_8),
                "Expected EncodingDefinition 6 from set 0 for Charset UTF8 as it is firebirdOnly=false, while def 5 from set 1 was firebirdOnly=true");
        assertNull(factory.getEncodingDefinitionByCharset(StandardCharsets.US_ASCII),
                "Expected no EncodingDefinition for Charset US-ASCII, as the definition was firebirdOnly=true");
    }

    @Test
    void testGetEncodingForFirebirdName_fallbackNull() {
        final Encoding defaultEncoding = root.getDefaultEncoding();

        assertSame(defaultEncoding, root.getEncodingForFirebirdName("NoSuchEncoding", null));
    }

    @Test
    void testGetEncodingForFirebirdName_fallbackNotNull() {
        final Encoding fallbackEncoding = new EncodingGeneric(Charset.forName("Cp861"));

        assertSame(fallbackEncoding, root.getEncodingForFirebirdName("NoSuchEncoding", fallbackEncoding));
    }

    @Test
    void testGetEncodingForCharacterSetId_fallbackNull() {
        final Encoding defaultEncoding = root.getDefaultEncoding();

        assertSame(defaultEncoding, root.getEncodingForCharacterSetId(513));
    }

    @Test
    void testGetEncodingForCharacterSetId_fallbackNotNull() {
        final Encoding fallbackEncoding = new EncodingGeneric(Charset.forName("Cp861"));

        assertSame(fallbackEncoding, root.getEncodingForCharacterSetId(513, fallbackEncoding));
    }

    @Test
    void testGetEncodingForCharset_fallbackNull() {
        // Character set not included in firebird / default-firebird-encodings.xml
        final Charset unsupportedCharset = Charset.forName("ISO-8859-15");
        final Encoding defaultEncoding = root.getDefaultEncoding();

        assertSame(defaultEncoding, root.getEncodingForCharset(unsupportedCharset));
    }

    @Test
    void testGetEncodingForCharset_fallbackNotNull() {
        // Character set not included in firebird / default-firebird-encodings.xml
        final Charset unsupportedCharset = Charset.forName("ISO-8859-15");
        final Encoding fallbackEncoding = new EncodingGeneric(Charset.forName("Cp861"));

        assertSame(fallbackEncoding, root.getEncodingForCharset(unsupportedCharset, fallbackEncoding));
    }

    @Test
    void testGetEncodingForCharsetAlias_fallbackNull() {
        // Character set not included in firebird / default-firebird-encodings.xml
        final String unsupportedCharset = "ISO-8859-15";
        final Encoding defaultEncoding = root.getDefaultEncoding();

        assertSame(defaultEncoding, root.getEncodingForCharsetAlias(unsupportedCharset));
    }

    @Test
    void testGetEncodingForCharsetAlias_fallbackNotNull() {
        // Character set not included in firebird / default-firebird-encodings.xml
        final String unsupportedCharset = "ISO-8859-15";
        final Encoding fallbackEncoding = new EncodingGeneric(Charset.forName("Cp861"));

        assertSame(fallbackEncoding, root.getEncodingForCharsetAlias(unsupportedCharset, fallbackEncoding));
    }

    @Test
    void testWithDefaultEncodingDefinition_null() {
        final IEncodingFactory alternativeInstance = root
                .withDefaultEncodingDefinition((EncodingDefinition) null);

        assertNotNull(alternativeInstance, "Expected non-null instance");
        assertSame(root.getDefaultEncoding(), alternativeInstance.getDefaultEncoding(),
                "Unexpected default encoding");
        assertSame(root.getDefaultEncodingDefinition(), alternativeInstance.getDefaultEncodingDefinition(),
                "Unexpected default encoding definition");
    }

    @Test
    void testWithDefaultEncodingDefinition_notNull() {
        final EncodingDefinition dos860 = root.getEncodingDefinitionByFirebirdName("DOS860");
        final IEncodingFactory alternativeInstance = root.withDefaultEncodingDefinition(dos860);

        assertNotNull(alternativeInstance, "Expected non-null instance");
        assertSame(dos860.getEncoding(), alternativeInstance.getDefaultEncoding(), "Unexpected default encoding");
        assertSame(dos860, alternativeInstance.getDefaultEncodingDefinition(),
                "Unexpected default encoding definition");
    }

    @ParameterizedTest
    @CsvSource({
            "UTF8       ,      , UTF8",
            "           , UTF-8, UTF8",
            "UTF8       , UTF-8, UTF8",
            "UNICODE_FSS,      , UNICODE_FSS",
            "UNICODE_FSS, UTF-8, UNICODE_FSS"
    })
    void testGetEncodingDefinitionCombinationsFromSet(String firebirdEncodingName, String javaCharsetAlias,
            String expectedByFirebirdName) {
        EncodingDefinition expectedEncodingDefinition = root.getEncodingDefinitionByFirebirdName(expectedByFirebirdName);

        assertSame(expectedEncodingDefinition, root.getEncodingDefinition(firebirdEncodingName, javaCharsetAlias));
    }

    @Test
    void testGetEncodingDefinitionCustomCombination() {
        EncodingDefinition encodingDefinition = root.getEncodingDefinition("NONE", "iso-8859-1");

        assertNotNull(encodingDefinition);
        assertEquals("NONE", encodingDefinition.getFirebirdEncodingName(), "firebirdEncodingName");
        assertEquals("ISO-8859-1", encodingDefinition.getJavaEncodingName(), "javaEncodingName");
        assertEquals(StandardCharsets.ISO_8859_1, encodingDefinition.getJavaCharset(), "javaCharset");
        assertEquals(CS_NONE, encodingDefinition.getFirebirdCharacterSetId(), "firebirdCharacterSetId");
        assertEquals(1, encodingDefinition.getMaxBytesPerChar(), "maxBytesPerChar");
    }

    @Test
    void testGetEncodingDefinition_noneWithNullJavaCharset() {
        EncodingDefinition defaultEncodingDefinition = root.getDefaultEncodingDefinition();
        EncodingDefinition encodingDefinition = root.getEncodingDefinition("NONE", null);

        assertNotNull(encodingDefinition);
        assertEquals("NONE", encodingDefinition.getFirebirdEncodingName(), "firebirdEncodingName");
        assertEquals(defaultEncodingDefinition.getJavaEncodingName(), encodingDefinition.getJavaEncodingName(), "javaEncodingName");
        assertEquals(defaultEncodingDefinition.getJavaCharset(), encodingDefinition.getJavaCharset(), "javaCharset");
        assertEquals(CS_NONE, encodingDefinition.getFirebirdCharacterSetId(), "firebirdCharacterSetId");
        assertEquals(1, encodingDefinition.getMaxBytesPerChar(), "maxBytesPerChar");
    }

    /**
     * Creates an {@link EncodingSet} with the specified {@link EncodingDefinition} instances.
     *
     * @param preferenceWeight
     *         Preference weight to use
     * @param encodingDefinitions
     *         The EncodingDefinition instances
     * @return EncodingSet instance
     */
    private static EncodingSet createEncodingSet(final int preferenceWeight, final EncodingDefinition... encodingDefinitions) {
        return new EncodingSet() {
            @Override
            public int getPreferenceWeight() {
                return preferenceWeight;
            }

            @Override
            public List<EncodingDefinition> getEncodings() {
                return Arrays.asList(encodingDefinitions);
            }
        };
    }
}
