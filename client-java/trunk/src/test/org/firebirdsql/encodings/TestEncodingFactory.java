/*
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

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

/**
 * Tests for {@link EncodingFactory}
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestEncodingFactory {

    /**
     * Java alias for FIREBIRD_TEST_CHARSET
     */
    private static final String JAVA_TEST_CHARSET = "ISO-8859-7";
    /**
     * Firebird alias for JAVA_TEST_CHARSET
     */
    private static final String FIREBIRD_TEST_CHARSET = "ISO8859_7";

    /**
     * Tests if all aliases of a supported Java characterset are mapped
     * to the same Firebird characterset.
     */
    @Test
    public void javaCharsetAliasMapping() {
        Charset charset = Charset.forName(JAVA_TEST_CHARSET);
        Set<String> allJavaAliases = new HashSet<String>();
        allJavaAliases.add(JAVA_TEST_CHARSET);
        // Canonical name is not included in aliases
        allJavaAliases.add(charset.name());
        allJavaAliases.addAll(charset.aliases());
        
        Set<String> aliasesMatched = new HashSet<String>();
        for (String javaAlias : allJavaAliases) {
            String iscEncoding = EncodingFactory.getIscEncoding(javaAlias);
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
        assumeTrue(FIREBIRD_TEST_CHARSET.equals(EncodingFactory.getIscEncoding(JAVA_TEST_CHARSET)));
        
        String differentCasedJavaAlias = JAVA_TEST_CHARSET.toUpperCase();
        if (differentCasedJavaAlias.equals(JAVA_TEST_CHARSET)) {
            differentCasedJavaAlias = JAVA_TEST_CHARSET.toLowerCase();
        }
        
        assertEquals("Unexpected FB characterset for differently cased Java alias", 
                FIREBIRD_TEST_CHARSET, EncodingFactory.getIscEncoding(differentCasedJavaAlias));
    }
    
    @Test
    public void aliasMappingFbToJavaCaseInsensitive() {
        assumeTrue(JAVA_TEST_CHARSET.equals(EncodingFactory.getJavaEncoding(FIREBIRD_TEST_CHARSET)));
        
        String differentCasedFbAlias = FIREBIRD_TEST_CHARSET.toUpperCase();
        if (differentCasedFbAlias.equals(FIREBIRD_TEST_CHARSET)) {
            differentCasedFbAlias = FIREBIRD_TEST_CHARSET.toLowerCase();
        }
        
        assertEquals("Unexpected Java characterset for differently cased FB alias", 
                JAVA_TEST_CHARSET, EncodingFactory.getJavaEncoding(differentCasedFbAlias));
    }
    
}
