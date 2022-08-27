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
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Tests for {@link EncodingFactory}
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
@SuppressWarnings("deprecation")
class EncodingFactoryTest {

    /**
     * Java alias for FIREBIRD_TEST_CHARSET
     */
    private static final String JAVA_TEST_CHARSET = "ISO-8859-7";
    /**
     * Firebird alias for JAVA_TEST_CHARSET
     */
    private static final String FIREBIRD_TEST_CHARSET = "ISO8859_7";

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
            String iscEncoding = EncodingFactory.getRootEncodingFactory()
                    .getEncodingDefinitionByCharsetAlias(javaAlias).getFirebirdEncodingName();
            if (FIREBIRD_TEST_CHARSET.equals(iscEncoding)) {
                aliasesMatched.add(javaAlias);
            }
        }

        assertEquals(allJavaAliases, aliasesMatched,
                format("One or more aliases of %s did not match expected Firebird character set %s", JAVA_TEST_CHARSET, FIREBIRD_TEST_CHARSET));
    }

    // TODO Add test which checks like above for all character sets specified in isc_encodings.properties
    // TODO Note exception if looking up java alias for fb alias and java alias matches file.encoding property : maps to null

    /**
     * Tests if the alias mapping is case-insensitive.
     */
    @Test
    void aliasMappingJavaToFbCaseInsensitive() {
        final IEncodingFactory factory = EncodingFactory.getRootEncodingFactory();
        assumeTrue(FIREBIRD_TEST_CHARSET.equals(factory
                .getEncodingDefinitionByCharsetAlias(JAVA_TEST_CHARSET).getFirebirdEncodingName()));

        String differentCasedJavaAlias = JAVA_TEST_CHARSET.toUpperCase(Locale.ROOT);
        if (differentCasedJavaAlias.equals(JAVA_TEST_CHARSET)) {
            differentCasedJavaAlias = JAVA_TEST_CHARSET.toLowerCase(Locale.ROOT);
        }

        assertEquals(FIREBIRD_TEST_CHARSET,
                factory.getEncodingDefinitionByCharsetAlias(differentCasedJavaAlias).getFirebirdEncodingName(),
                "Unexpected FB character set for differently cased Java alias");
    }

    @Test
    void aliasMappingFbToJavaCaseInsensitive() {
        final IEncodingFactory factory = EncodingFactory.getRootEncodingFactory();
        assumeTrue(JAVA_TEST_CHARSET.equals(factory.getEncodingDefinitionByFirebirdName(FIREBIRD_TEST_CHARSET)
                .getJavaEncodingName()));

        String differentCasedFbAlias = FIREBIRD_TEST_CHARSET.toUpperCase(Locale.ROOT);
        if (differentCasedFbAlias.equals(FIREBIRD_TEST_CHARSET)) {
            differentCasedFbAlias = FIREBIRD_TEST_CHARSET.toLowerCase(Locale.ROOT);
        }

        assertEquals(JAVA_TEST_CHARSET,
                factory.getEncodingDefinitionByFirebirdName(differentCasedFbAlias).getJavaEncodingName(),
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
        IEncodingFactory factory = EncodingFactory.getRootEncodingFactory();

        assertNull(factory.getEncodingDefinitionByCharacterSetId(ISCConstants.CS_dynamic),
                "Expected null EncodingDefinition for CS_dynamic (127)");
        assertSame(factory.getDefaultEncoding(), factory.getEncodingForCharacterSetId(ISCConstants.CS_dynamic),
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
        final EncodingFactory factory = EncodingFactory.getRootEncodingFactory();
        final Encoding defaultEncoding = factory.getDefaultEncoding();

        assertSame(defaultEncoding, factory.getEncodingForFirebirdName("NoSuchEncoding", null));
    }

    @Test
    void testGetEncodingForFirebirdName_fallbackNotNull() {
        final EncodingFactory factory = EncodingFactory.getRootEncodingFactory();
        final Encoding fallbackEncoding = new EncodingGeneric(Charset.forName("Cp861"));

        assertSame(fallbackEncoding, factory.getEncodingForFirebirdName("NoSuchEncoding", fallbackEncoding));
    }

    @Test
    void testGetEncodingForCharacterSetId_fallbackNull() {
        final EncodingFactory factory = EncodingFactory.getRootEncodingFactory();
        final Encoding defaultEncoding = factory.getDefaultEncoding();

        assertSame(defaultEncoding, factory.getEncodingForCharacterSetId(513));
    }

    @Test
    void testGetEncodingForCharacterSetId_fallbackNotNull() {
        final EncodingFactory factory = EncodingFactory.getRootEncodingFactory();
        final Encoding fallbackEncoding = new EncodingGeneric(Charset.forName("Cp861"));

        assertSame(fallbackEncoding, factory.getEncodingForCharacterSetId(513, fallbackEncoding));
    }

    @Test
    void testGetEncodingForCharset_fallbackNull() {
        final IEncodingFactory factory = EncodingFactory.getRootEncodingFactory();
        // Character set not included in firebird / default-firebird-encodings.xml
        final Charset unsupportedCharset = Charset.forName("ISO-8859-15");
        final Encoding defaultEncoding = factory.getDefaultEncoding();

        assertSame(defaultEncoding, factory.getEncodingForCharset(unsupportedCharset));
    }

    @Test
    void testGetEncodingForCharset_fallbackNotNull() {
        final EncodingFactory factory = EncodingFactory.getRootEncodingFactory();
        // Character set not included in firebird / default-firebird-encodings.xml
        final Charset unsupportedCharset = Charset.forName("ISO-8859-15");
        final Encoding fallbackEncoding = new EncodingGeneric(Charset.forName("Cp861"));

        assertSame(fallbackEncoding, factory.getEncodingForCharset(unsupportedCharset, fallbackEncoding));
    }

    @Test
    void testGetEncodingForCharsetAlias_fallbackNull() {
        final IEncodingFactory factory = EncodingFactory.getRootEncodingFactory();
        // Character set not included in firebird / default-firebird-encodings.xml
        final String unsupportedCharset = "ISO-8859-15";
        final Encoding defaultEncoding = factory.getDefaultEncoding();

        assertSame(defaultEncoding, factory.getEncodingForCharsetAlias(unsupportedCharset));
    }

    @Test
    void testGetEncodingForCharsetAlias_fallbackNotNull() {
        final EncodingFactory factory = EncodingFactory.getRootEncodingFactory();
        // Character set not included in firebird / default-firebird-encodings.xml
        final String unsupportedCharset = "ISO-8859-15";
        final Encoding fallbackEncoding = new EncodingGeneric(Charset.forName("Cp861"));

        assertSame(fallbackEncoding, factory.getEncodingForCharsetAlias(unsupportedCharset, fallbackEncoding));
    }

    @ParameterizedTest
    @CsvSource({
            "21,  1, ISO8859_1",
            "513, 1, (unknown charset)",
            "4,   4, UTF8",
            "3,   3, UNICODE_FSS"
    })
    void testGetCharacterSetSize(int charsetId, int expectedSize, String charsetName) {
        assertEquals(expectedSize, EncodingFactory.getCharacterSetSize(charsetId),
                format("Expected CharacterSetSize %d for encoding (%s, id=%d)", expectedSize, charsetName, charsetId));
    }

    @Test
    void testGetEncoding_alias_Exists() {
        final IEncodingFactory factory = EncodingFactory.getRootEncodingFactory();
        final Encoding expectedEncoding = factory.getEncodingForFirebirdName("UTF8");

        assertSame(expectedEncoding, EncodingFactory.getEncoding("UTF-8"));
    }

    @Test
    void testGetEncoding_alias_DoesNotExist() {
        final IEncodingFactory factory = EncodingFactory.getRootEncodingFactory();
        final Encoding expectedEncoding = factory.getDefaultEncoding();

        assertSame(expectedEncoding, EncodingFactory.getEncoding("DoesNotExist"));
    }

    @Test
    void testGetEncoding_charset_Exists() {
        final IEncodingFactory factory = EncodingFactory.getRootEncodingFactory();
        final Encoding expectedEncoding = factory.getEncodingForCharset(StandardCharsets.UTF_8);

        assertSame(expectedEncoding, EncodingFactory.getEncoding(StandardCharsets.UTF_8));
    }

    @Test
    void testGetEncoding_charset_DoesNotExist() {
        final IEncodingFactory factory = EncodingFactory.getRootEncodingFactory();
        final Encoding defaultEncoding = factory.getDefaultEncoding();

        // getEncoding(charset) creates a new EncodingGeneric if it is not supported
        assertNotSame(defaultEncoding, EncodingFactory.getEncoding(Charset.forName("ISO-8859-15")));
    }

    @Test
    void testGetIscEncoding_alias_Exists() {
        assertEquals("WIN1252", EncodingFactory.getIscEncoding("CP1252"), "Unexpected Firebird encoding name");
    }

    @Test
    void testGetIscEncoding_alias_DoesNotExist() {
        assertNull(EncodingFactory.getIscEncoding("DoesNotExist"), "Expected null for unknown charset alias");
    }

    @Test
    public void testGetIscEncoding_charset_Exists() {
        assertEquals("WIN1252", EncodingFactory.getIscEncoding(Charset.forName("Cp1252")),
                "Unexpected Firebird encoding name");
    }

    @Test
    void testWithDefaultEncodingDefinition_null() {
        final EncodingFactory defaultInstance = EncodingFactory.getRootEncodingFactory();
        final IEncodingFactory alternativeInstance = defaultInstance
                .withDefaultEncodingDefinition((EncodingDefinition) null);

        assertNotNull(alternativeInstance, "Expected non-null instance");
        assertSame(defaultInstance.getDefaultEncoding(), alternativeInstance.getDefaultEncoding(),
                "Unexpected default encoding");
        assertSame(defaultInstance.getDefaultEncodingDefinition(), alternativeInstance.getDefaultEncodingDefinition(),
                "Unexpected default encoding definition");
    }

    @Test
    void testWithDefaultEncodingDefinition_notNull() {
        final EncodingFactory defaultInstance = EncodingFactory.getRootEncodingFactory();
        final EncodingDefinition dos860 = defaultInstance.getEncodingDefinitionByFirebirdName("DOS860");
        final IEncodingFactory alternativeInstance = defaultInstance.withDefaultEncodingDefinition(dos860);

        assertNotNull(alternativeInstance, "Expected non-null instance");
        assertSame(dos860.getEncoding(), alternativeInstance.getDefaultEncoding(), "Unexpected default encoding");
        assertSame(dos860, alternativeInstance.getDefaultEncodingDefinition(),
                "Unexpected default encoding definition");
    }

    @Test
    void testGetIscEncoding_charset_NotLoaded() {
        // ISO-8859-15 is not supported/loaded
        assertNull(EncodingFactory.getIscEncoding(Charset.forName("iso-8859-15")),
                "Expected null for charset alias that isn't loaded");
    }

    @ParameterizedTest
    @CsvSource({
            ",             1",
            "WIN1250,      1",
            "UTF8,         4",
            "DoesNotExist, 1"
    })
    void testGetIscEncodingSize(String charsetName, int expectedSize) {
        assertEquals(expectedSize, EncodingFactory.getIscEncodingSize(charsetName),
                format("Expected size %d for character set (%s)", expectedSize, charsetName));
    }

    @Test
    void testGetJavaEncoding_null() {
        assertNull(EncodingFactory.getJavaEncoding(null), "Expected null for null Firebird encoding name");
    }

    @Test
    void testGetJavaEncoding_defaultEncoding() {
        final EncodingDefinition defaultEncodingDefinition = EncodingFactory.getRootEncodingFactory().getEncodingDefinitionByCharset(Charset.defaultCharset());
        assumeTrue(defaultEncodingDefinition != null);

        assertNull(EncodingFactory.getJavaEncoding(defaultEncodingDefinition.getFirebirdEncodingName()),
                "Expected null for Firebird encoding name of default character set");
    }

    @Test
    void testGetJavaEncoding_notDefaultEncoding() {
        // We assume that we won't run this test on a JVM that has DOS860 as the default encoding.
        final Charset cp860 = Charset.forName("Cp860");
        assumeTrue(!cp860.equals(Charset.defaultCharset()));
        final String expectedName = "Cp860"; // alias used in DefaultEncodingSet

        assertEquals(expectedName, EncodingFactory.getJavaEncoding("DOS860"), "Unexpected java encoding name for Firebird encoding DOS860");
    }

    @Test
    void testGetJavaEncodingForAlias_defaultEncoding() {
        final EncodingDefinition defaultEncodingDefinition = EncodingFactory.getRootEncodingFactory().getEncodingDefinitionByCharset(Charset.defaultCharset());
        assumeTrue(defaultEncodingDefinition != null);

        assertNull(EncodingFactory.getJavaEncodingForAlias(defaultEncodingDefinition.getJavaEncodingName()),
                "Expected null for default encoding");
    }

    @Test
    void testGetJavaEncodingForAlias_notDefaultEncoding() {
        // We assume that we won't run this test on a JVM that has DOS860 as the default encoding.
        final Charset cp860 = Charset.forName("Cp860");
        assumeTrue(!cp860.equals(Charset.defaultCharset()));
        final String expectedName = "Cp860"; // alias used in DefaultEncodingSet

        assertEquals(expectedName, EncodingFactory.getJavaEncodingForAlias(expectedName), "Unexpected java encoding name for encoding alias Cp860");
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
