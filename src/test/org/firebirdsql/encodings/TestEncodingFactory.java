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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeNotNull;
import static org.junit.Assume.assumeTrue;

/**
 * Tests for {@link EncodingFactory}
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
@SuppressWarnings("deprecation")
public class TestEncodingFactory {

    /**
     * Java alias for FIREBIRD_TEST_CHARSET
     */
    private static final String JAVA_TEST_CHARSET = "ISO-8859-7";
    /**
     * Firebird alias for JAVA_TEST_CHARSET
     */
    private static final String FIREBIRD_TEST_CHARSET = "ISO8859_7";

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    /**
     * Tests if all aliases of a supported Java characterset are mapped
     * to the same Firebird characterset.
     */
    @Test
    public void javaCharsetAliasMapping() {
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

        assertEquals(String.format("One or more aliases of %s did not match expected Firebird characterset %s", JAVA_TEST_CHARSET, FIREBIRD_TEST_CHARSET),
                allJavaAliases, aliasesMatched);
    }

    // TODO Add test which checks like above for all charactersets specified in isc_encodings.properties
    // TODO Note exception if looking up java alias for fb alias and java alias matches file.encoding property : maps to null

    /**
     * Tests if the alias mapping is case-insensitive.
     */
    @Test
    public void aliasMappingJavaToFbCaseInsensitive() {
        final IEncodingFactory factory = EncodingFactory.getRootEncodingFactory();
        assumeTrue(FIREBIRD_TEST_CHARSET.equals(factory
                .getEncodingDefinitionByCharsetAlias(JAVA_TEST_CHARSET).getFirebirdEncodingName()));

        String differentCasedJavaAlias = JAVA_TEST_CHARSET.toUpperCase();
        if (differentCasedJavaAlias.equals(JAVA_TEST_CHARSET)) {
            differentCasedJavaAlias = JAVA_TEST_CHARSET.toLowerCase();
        }

        assertEquals("Unexpected FB characterset for differently cased Java alias",
                FIREBIRD_TEST_CHARSET,
                factory.getEncodingDefinitionByCharsetAlias(differentCasedJavaAlias).getFirebirdEncodingName());
    }

    @Test
    public void aliasMappingFbToJavaCaseInsensitive() {
        final IEncodingFactory factory = EncodingFactory.getRootEncodingFactory();
        assumeTrue(JAVA_TEST_CHARSET.equals(factory.getEncodingDefinitionByFirebirdName(FIREBIRD_TEST_CHARSET)
                .getJavaEncodingName()));

        String differentCasedFbAlias = FIREBIRD_TEST_CHARSET.toUpperCase();
        if (differentCasedFbAlias.equals(FIREBIRD_TEST_CHARSET)) {
            differentCasedFbAlias = FIREBIRD_TEST_CHARSET.toLowerCase();
        }

        assertEquals("Unexpected Java character set for differently cased FB alias",
                JAVA_TEST_CHARSET, factory.getEncodingDefinitionByFirebirdName(differentCasedFbAlias)
                        .getJavaEncodingName());
    }

    @Test
    public void testCreateInstance_Custom() {
        final String dos866Firebird = "DOS866";
        final int dos866Id = 48;
        final String dos866Java = "Cp866";
        final Charset dos866Charset = Charset.forName(dos866Java);
        final EncodingDefinition testEncodingDefinition = new DefaultEncodingDefinition(dos866Firebird,
                Charset.forName(dos866Java), 1, dos866Id, false);
        final Encoding testEncoding = testEncodingDefinition.getEncoding();
        EncodingSet encodingSet = createEncodingSet(0, testEncodingDefinition);

        EncodingFactory factory = EncodingFactory.createInstance(encodingSet);

        assertSame("Unexpected EncodingDefinition by firebird name",
                testEncodingDefinition, factory.getEncodingDefinitionByFirebirdName(dos866Firebird));
        assertSame("Unexpected encoding by firebird name",
                testEncoding, factory.getEncodingForFirebirdName(dos866Firebird));
        assertSame("Unexpected EncodingDefinition by character set id",
                testEncodingDefinition, factory.getEncodingDefinitionByCharacterSetId(dos866Id));
        assertSame("Unexpected encoding by character set id",
                testEncoding, factory.getEncodingForCharacterSetId(dos866Id));
        assertSame("Unexpected EncodingDefinition by charset",
                testEncodingDefinition, factory.getEncodingDefinitionByCharset(dos866Charset));
        assertSame("Unexpected encoding by charset",
                testEncoding, factory.getOrCreateEncodingForCharset(dos866Charset));
        assertSame("Unexpected encoding by charset",
                testEncoding, factory.getEncodingForCharset(dos866Charset));
        assertSame("Unexpected EncodingDefinition by java charset alias",
                testEncodingDefinition, factory.getEncodingDefinitionByCharsetAlias(dos866Java));
        assertSame("Unexpected encoding by java charset alias",
                testEncoding, factory.getEncodingForCharsetAlias(dos866Java));
    }

    /**
     * Tests if retrieving info for {@link ISCConstants#CS_dynamic} returns the right result.
     */
    @Test
    public void testCharacterSetId_CS_dynamic() {
        IEncodingFactory factory = EncodingFactory.getRootEncodingFactory();

        assertNull("Expected null EncodingDefinition for CS_dynamic (127)", factory.getEncodingDefinitionByCharacterSetId(ISCConstants.CS_dynamic));
        assertSame("Expected the default encoding for CS_dynamic (127)",
                factory.getDefaultEncoding(), factory.getEncodingForCharacterSetId(ISCConstants.CS_dynamic));
    }

    /**
     * Tests creating an {@link EncodingFactory} from an {@link EncodingSet} containing a definition for firebird
     * character set id {@link ISCConstants#CS_dynamic} (127), this {@link EncodingDefinition} should not be part of the
     * factory.
     */
    @Test
    public void testDefinition_CS_dynamic_skipped() {
        final EncodingSet encodingSet = createEncodingSet(0,
                new DefaultEncodingDefinition("TEST1", (Charset) null, 1, 10, true),
                new DefaultEncodingDefinition("TEST2", (Charset) null, 1, ISCConstants.CS_dynamic, true));
        final IEncodingFactory factory = EncodingFactory.createInstance(encodingSet);

        assertNotNull("Expected EncodingDefinition for TEST1 to be returned", factory.getEncodingDefinitionByFirebirdName("TEST1"));
        assertNull("Expected EncodingDefinition for TEST2 to be null (as it was declared with character set id 127)",
                factory.getEncodingDefinitionByFirebirdName("TEST2"));
    }

    @Test
    public void testCreateInstance_MultipleEncodingSets() {
        final EncodingDefinition test0_1 = new DefaultEncodingDefinition("TEST1", Charset.forName("Cp1255"), 1, 5, false);
        final EncodingDefinition test0_2 = new DefaultEncodingDefinition("TEST2", (Charset) null, 1, 6, true);
        final EncodingDefinition test1_1 = new DefaultEncodingDefinition("TEST1", (Charset) null, 1, 5, true);
        final EncodingDefinition test1_3 = new DefaultEncodingDefinition("TEST3", Charset.forName("Cp1256"), 1, 7, false);
        // Uses same Charset as test1_3, both are firebirdOnly = false
        final EncodingDefinition test0_4 = new DefaultEncodingDefinition("TEST4", Charset.forName("Cp1256"), 1, 8, false);
        final EncodingDefinition test1_5 = new DefaultEncodingDefinition("TEST5", Charset.forName("UTF8"), 1, 9, true);
        // Uses same Charset as test1_5, but 1_5 is firebirdOnly = true
        final EncodingDefinition test0_6 = new DefaultEncodingDefinition("TEST6", Charset.forName("UTF8"), 1, 10, false);
        final EncodingDefinition test0_7 = new DefaultEncodingDefinition("TEST7", Charset.forName("US-ASCII"), 1, 11, true);
        final EncodingSet encodingSet0 = createEncodingSet(0, test0_1, test0_2, test0_4, test0_6, test0_7);
        final EncodingSet encodingSet1 = createEncodingSet(1, test1_1, test1_3, test1_5);

        final IEncodingFactory factory = EncodingFactory.createInstance(encodingSet0, encodingSet1);

        assertSame("Expected EncodingDefinition 1 from set 1 for name TEST1", test1_1, factory.getEncodingDefinitionByFirebirdName("TEST1"));
        assertSame("Expected EncodingDefinition 2 from set 0 for name TEST2", test0_2, factory.getEncodingDefinitionByFirebirdName("TEST2"));
        assertSame("Expected EncodingDefinition 3 from set 1 for name TEST3", test1_3, factory.getEncodingDefinitionByFirebirdName("TEST3"));
        assertSame("Expected EncodingDefinition 4 from set 0 for name TEST4", test0_4, factory.getEncodingDefinitionByFirebirdName("TEST4"));
        assertSame("Expected EncodingDefinition 5 from set 1 for name TEST5", test1_5, factory.getEncodingDefinitionByFirebirdName("TEST5"));
        assertSame("Expected EncodingDefinition 6 from set 0 for name TEST4", test0_6, factory.getEncodingDefinitionByFirebirdName("TEST6"));
        assertSame("Expected EncodingDefinition 7 from set 0 for name TEST4", test0_7, factory.getEncodingDefinitionByFirebirdName("TEST7"));
        assertNull("Expected no EncodingDefinition for Charset Cp1255, as the definition from set 0 should not be loaded",
                factory.getEncodingDefinitionByCharset(Charset.forName("Cp1255")));
        assertSame("Expected EncodingDefinition 3 from set 1 for Charset Cp1256 as it was loaded before EncodingDefinition 4 from set 0 (and both are firebirdOnly = false)",
                test1_3, factory.getEncodingDefinitionByCharset(Charset.forName("Cp1256")));
        assertSame("Expected EncodingDefinition 6 from set 0 for Charset UTF8 as it is firebirdOnly=false, while def 5 from set 1 was firebirdOnly=true",
                test0_6, factory.getEncodingDefinitionByCharset(Charset.forName("UTF8")));
        assertNull("Expected no EncodingDefinition for Charset US-ASCII, as the definition was firebirdOnly=true",
                factory.getEncodingDefinitionByCharset(Charset.forName("US-ASCII")));

    }

    @Test
    public void testGetCharacterTranslator_initial() throws Exception {
        final IEncodingFactory factory = EncodingFactory.getRootEncodingFactory();

        final CharacterTranslator characterTranslator = factory.getCharacterTranslator("org.firebirdsql.encodings.testTranslation");
        assertNotNull("Expected a CharacterTranslator instance", characterTranslator);
    }

    @Test
    public void testGetCharacterTranslator_cached() throws Exception {
        final IEncodingFactory factory = EncodingFactory.getRootEncodingFactory();

        final CharacterTranslator characterTranslator = factory.getCharacterTranslator("org.firebirdsql.encodings.testTranslation");
        final CharacterTranslator secondCharacterTranslator = factory.getCharacterTranslator("org.firebirdsql.encodings.testTranslation");
        assertSame("Expected second call to getCharacterTranslator to return same instance", characterTranslator, secondCharacterTranslator);
    }

    @Test
    public void testGetCharacterTranslator_notFound() throws Exception {
        expectedException.expect(SQLException.class);
        expectedException.expectMessage("could not be found.");

        final IEncodingFactory factory = EncodingFactory.getRootEncodingFactory();

        factory.getCharacterTranslator("org.firebirdsql.encodings.testDoesNotExist");
    }

    @Test
    public void testGetEncodingForFirebirdName_fallbackNull() {
        final EncodingFactory factory = EncodingFactory.getRootEncodingFactory();
        final Encoding defaultEncoding = factory.getDefaultEncoding();

        assertSame(defaultEncoding, factory.getEncodingForFirebirdName("NoSuchEncoding", null));
    }

    @Test
    public void testGetEncodingForFirebirdName_fallbackNotNull() {
        final EncodingFactory factory = EncodingFactory.getRootEncodingFactory();
        final Encoding fallbackEncoding = new EncodingGeneric(Charset.forName("Cp861"));

        assertSame(fallbackEncoding, factory.getEncodingForFirebirdName("NoSuchEncoding", fallbackEncoding));
    }

    @Test
    public void testGetEncodingForCharacterSetId_fallbackNull() {
        final EncodingFactory factory = EncodingFactory.getRootEncodingFactory();
        final Encoding defaultEncoding = factory.getDefaultEncoding();

        assertSame(defaultEncoding, factory.getEncodingForCharacterSetId(513));
    }

    @Test
    public void testGetEncodingForCharacterSetId_fallbackNotNull() {
        final EncodingFactory factory = EncodingFactory.getRootEncodingFactory();
        final Encoding fallbackEncoding = new EncodingGeneric(Charset.forName("Cp861"));

        assertSame(fallbackEncoding, factory.getEncodingForCharacterSetId(513, fallbackEncoding));
    }

    @Test
     public void testGetEncodingForCharset_fallbackNull() {
        final IEncodingFactory factory = EncodingFactory.getRootEncodingFactory();
        // Character set not included in firebird / default-firebird-encodings.xml
        final Charset unsupportedCharset = Charset.forName("ISO-8859-15");
        final Encoding defaultEncoding = factory.getDefaultEncoding();

        assertSame(defaultEncoding, factory.getEncodingForCharset(unsupportedCharset));
    }

    @Test
    public void testGetEncodingForCharset_fallbackNotNull() {
        final EncodingFactory factory = EncodingFactory.getRootEncodingFactory();
        // Character set not included in firebird / default-firebird-encodings.xml
        final Charset unsupportedCharset = Charset.forName("ISO-8859-15");
        final Encoding fallbackEncoding = new EncodingGeneric(Charset.forName("Cp861"));

        assertSame(fallbackEncoding, factory.getEncodingForCharset(unsupportedCharset, fallbackEncoding));
    }

    @Test
    public void testGetEncodingForCharsetAlias_fallbackNull() {
        final IEncodingFactory factory = EncodingFactory.getRootEncodingFactory();
        // Character set not included in firebird / default-firebird-encodings.xml
        final String unsupportedCharset = "ISO-8859-15";
        final Encoding defaultEncoding = factory.getDefaultEncoding();

        assertSame(defaultEncoding, factory.getEncodingForCharsetAlias(unsupportedCharset));
    }

    @Test
    public void testGetEncodingForCharsetAlias_fallbackNotNull() {
        final EncodingFactory factory = EncodingFactory.getRootEncodingFactory();
        // Character set not included in firebird / default-firebird-encodings.xml
        final String unsupportedCharset = "ISO-8859-15";
        final Encoding fallbackEncoding = new EncodingGeneric(Charset.forName("Cp861"));

        assertSame(fallbackEncoding, factory.getEncodingForCharsetAlias(unsupportedCharset, fallbackEncoding));
    }

    @Test
    public void testGetCharacterSetSize_SingleByte() {
        final int ISO8859_1_ID = 21;
        assertEquals("Expected CharacterSetSize 1 for single byte encoding (ISO8859_1, id=21)", 1, EncodingFactory.getCharacterSetSize(ISO8859_1_ID));
    }

    @Test
    public void testGetCharacterSetSize_DoesNotExist() {
        final int DOES_NOT_EXIST = 513;
        assertEquals("Expected CharacterSetSize 1 for unknown id (513)", 1, EncodingFactory.getCharacterSetSize(DOES_NOT_EXIST));
    }

    @Test
    public void testGetCharacterSetSize_MultiByte() {
        final int UTF8_ID = 4;
        assertEquals("Expected CharacterSetSize 4 for multi-byte encoding (UTF8, id=4)", 4, EncodingFactory.getCharacterSetSize(UTF8_ID));
    }

    @Test
    public void testGetCharacterSetSize_MultiByte_FirebirdOnly() {
        final int UNICODE_FSS_ID = 3;
        assertEquals("Expected CharacterSetSize 3 for multi-byte, firebirdOnly encoding (UNICODE_FSS, id=3)", 3, EncodingFactory.getCharacterSetSize(UNICODE_FSS_ID));
    }

    @Test
     public void testGetEncoding_alias_Exists() {
        final IEncodingFactory factory = EncodingFactory.getRootEncodingFactory();
        final Encoding expectedEncoding = factory.getEncodingForFirebirdName("UTF8");

        assertSame(expectedEncoding, EncodingFactory.getEncoding("UTF-8"));
    }

    @Test
    public void testGetEncoding_alias_DoesNotExist() {
        final IEncodingFactory factory = EncodingFactory.getRootEncodingFactory();
        final Encoding expectedEncoding = factory.getDefaultEncoding();

        assertSame(expectedEncoding, EncodingFactory.getEncoding("DoesNotExist"));
    }

    @Test
    public void testGetEncoding_charset_Exists() {
        final IEncodingFactory factory = EncodingFactory.getRootEncodingFactory();
        final Encoding expectedEncoding = factory.getEncodingForCharset(Charset.forName("UTF-8"));

        assertSame(expectedEncoding, EncodingFactory.getEncoding(Charset.forName("UTF-8")));
    }

    @Test
    public void testGetEncoding_charset_DoesNotExist() {
        final IEncodingFactory factory = EncodingFactory.getRootEncodingFactory();
        final Encoding defaultEncoding = factory.getDefaultEncoding();

        // getEncoding(charset) creates a new EncodingGeneric if it is not supported
        assertNotSame(defaultEncoding, EncodingFactory.getEncoding(Charset.forName("ISO-8859-15")));
    }

    @Test
      public void testGetIscEncoding_alias_Exists() {
        assertEquals("Unexpected Firebird encoding name", "WIN1252", EncodingFactory.getIscEncoding("CP1252"));
    }

    @Test
    public void testGetIscEncoding_alias_DoesNotExist() {
        assertNull("Expected null for unknown charset alias", EncodingFactory.getIscEncoding("DoesNotExist"));
    }

    @Test
    public void testGetIscEncoding_charset_Exists() {
        assertEquals("Unexpected Firebird encoding name", "WIN1252", EncodingFactory.getIscEncoding(Charset.forName("Cp1252")));
    }

    @Test
     public void testWithDefaultEncodingDefinition_null() {
        final EncodingFactory defaultInstance = EncodingFactory.getRootEncodingFactory();
        final IEncodingFactory alternativeInstance = defaultInstance
                .withDefaultEncodingDefinition((EncodingDefinition) null);

        assertNotNull("Expected non-null instance", alternativeInstance);
        assertSame("Unexpected default encoding", defaultInstance.getDefaultEncoding(), alternativeInstance.getDefaultEncoding());
        assertSame("Unexpected default encoding definition", defaultInstance.getDefaultEncodingDefinition(), alternativeInstance.getDefaultEncodingDefinition());
    }

    @Test
    public void testWithDefaultEncodingDefinition_notNull() {
        final EncodingFactory defaultInstance = EncodingFactory.getRootEncodingFactory();
        final EncodingDefinition dos860 = defaultInstance.getEncodingDefinitionByFirebirdName("DOS860");
        final IEncodingFactory alternativeInstance = defaultInstance.withDefaultEncodingDefinition(dos860);

        assertNotNull("Expected non-null instance", alternativeInstance);
        assertSame("Unexpected default encoding", dos860.getEncoding(), alternativeInstance.getDefaultEncoding());
        assertSame("Unexpected default encoding definition", dos860, alternativeInstance.getDefaultEncodingDefinition());
    }

    @Test
    public void testGetIscEncoding_charset_NotLoaded() {
        // ISO-8859-15 is not supported/loaded
        assertNull("Expected null for charset alias that isn't loaded", EncodingFactory.getIscEncoding(Charset.forName("iso-8859-15")));
    }

    @Test
    public void testGetIscEncodingSize_null() {
        assertEquals("Expected size 1 for null encoding", 1, EncodingFactory.getIscEncodingSize(null));
    }

    @Test
    public void testGetIscEncodingSize_singleByte() {
        assertEquals("Expected size 1 for a known single byte character set (win1250)", 1, EncodingFactory.getIscEncodingSize("WIN1250"));
    }

    @Test
    public void testGetIscEncodingSize_multiByte() {
        assertEquals("Expected size 5 for a known multi byte character set (utf8)", 4, EncodingFactory.getIscEncodingSize("UTF8"));
    }

    @Test
    public void testGetIscEncodingSize_unknown() {
        assertEquals("Expected size 1 for an unknown character set", 1, EncodingFactory.getIscEncodingSize("DoesNotExist"));
    }

    @Test
    public void testGetJavaEncoding_null() {
        assertNull("Expected null for null Firebird encoding name", EncodingFactory.getJavaEncoding(null));
    }

    @Test
    public void testGetJavaEncoding_defaultEncoding() {
        final EncodingDefinition defaultEncodingDefinition = EncodingFactory.getRootEncodingFactory().getEncodingDefinitionByCharset(Charset.defaultCharset());
        assumeNotNull(defaultEncodingDefinition);

        assertNull("Expected null for Firebird encoding name of default character set", EncodingFactory.getJavaEncoding(defaultEncodingDefinition.getFirebirdEncodingName()));
    }

    @Test
    public void testGetJavaEncoding_notDefaultEncoding() {
        // We assume that we won't run this test on a JVM that has DOS860 as the default encoding.
        final Charset cp860 = Charset.forName("Cp860");
        assumeTrue(!cp860.equals(Charset.defaultCharset()));
        final String expectedName = "Cp860"; // alias used in DefaultEncodingSet

        assertEquals("Unexpected java encoding name for Firebird encoding DOS860", expectedName, EncodingFactory.getJavaEncoding("DOS860"));
    }

    @Test
    public void testGetJavaEncodingForAlias_defaultEncoding() {
        final EncodingDefinition defaultEncodingDefinition = EncodingFactory.getRootEncodingFactory().getEncodingDefinitionByCharset(Charset.defaultCharset());
        assumeNotNull(defaultEncodingDefinition);

        assertNull("Expected null for default encoding", EncodingFactory.getJavaEncodingForAlias(defaultEncodingDefinition.getJavaEncodingName()));
    }

    @Test
    public void testGetJavaEncodingForAlias_notDefaultEncoding() {
        // We assume that we won't run this test on a JVM that has DOS860 as the default encoding.
        final Charset cp860 = Charset.forName("Cp860");
        assumeTrue(!cp860.equals(Charset.defaultCharset()));
        final String expectedName = "Cp860"; // alias used in DefaultEncodingSet

        assertEquals("Unexpected java encoding name for encoding alias Cp860", expectedName, EncodingFactory.getJavaEncodingForAlias(expectedName));
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
