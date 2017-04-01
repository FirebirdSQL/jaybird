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

import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * The default encoding set for Jaybird.
 * <p>
 * This {@link EncodingSet} loads the definitions from the file <code>default-firebird-encodings.xml</code> in
 * <code>org.firebirdsql.encodings</code>
 * </p>
 * <p>
 * This class can be subclassed to load other definitions
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class DefaultEncodingSet implements EncodingSet {

    private static final Logger logger = LoggerFactory.getLogger(DefaultEncodingSet.class);
    private List<EncodingDefinition> encodingDefinitions = null;

    @Override
    public int getPreferenceWeight() {
        return 0;
    }

    @Override
    public final synchronized List<EncodingDefinition> getEncodings() {
        if (encodingDefinitions == null) {
            encodingDefinitions = createEncodingDefinitions();
        }
        return encodingDefinitions;
    }

    /**
     * Creates all encoding definitions of the default set.
     *
     * @return List of encoding definitions
     */
    private static List<EncodingDefinition> createEncodingDefinitions() {
        List<EncodingDefinition> definitionList = new ArrayList<>();
        definitionList.add(createEncodingDefinition("NONE", null, 0, 1, false));
        definitionList.add(createEncodingDefinition("OCTETS", null, 1, 1, false));
        definitionList.add(createEncodingDefinition("ASCII", "US-ASCII", 2, 1, false));
        definitionList.add(createEncodingDefinition("UNICODE_FSS", "UTF-8", 3, 3, true));
        definitionList.add(createEncodingDefinition("UTF8", "UTF-8", 4, 4, false));
        definitionList.add(createEncodingDefinition("SJIS_0208", "MS932", 5, 2, false));
        definitionList.add(createEncodingDefinition("EUCJ_0208", "EUC_JP", 6, 2, false));
        definitionList.add(createEncodingDefinition("DOS737", "Cp737", 9, 1, false));
        definitionList.add(createEncodingDefinition("DOS437", "Cp437", 10, 1, false));
        definitionList.add(createEncodingDefinition("DOS850", "Cp850", 11, 1, false));
        definitionList.add(createEncodingDefinition("DOS865", "Cp865", 12, 1, false));
        definitionList.add(createEncodingDefinition("DOS860", "Cp860", 13, 1, false));
        definitionList.add(createEncodingDefinition("DOS863", "Cp863", 14, 1, false));
        definitionList.add(createEncodingDefinition("DOS775", "Cp775", 15, 1, false));
        definitionList.add(createEncodingDefinition("DOS858", "Cp858", 16, 1, false));
        definitionList.add(createEncodingDefinition("DOS862", "Cp862", 17, 1, false));
        definitionList.add(createEncodingDefinition("DOS864", "Cp864", 18, 1, false));
        definitionList.add(createEncodingDefinition("NEXT", null, 19, 1, false));
        definitionList.add(createEncodingDefinition("ISO8859_1", "ISO-8859-1", 21, 1, false));
        definitionList.add(createEncodingDefinition("ISO8859_2", "ISO-8859-2", 22, 1, false));
        definitionList.add(createEncodingDefinition("ISO8859_3", "ISO-8859-3", 23, 1, false));
        definitionList.add(createEncodingDefinition("ISO8859_4", "ISO-8859-4", 34, 1, false));
        definitionList.add(createEncodingDefinition("ISO8859_5", "ISO-8859-5", 35, 1, false));
        definitionList.add(createEncodingDefinition("ISO8859_6", "ISO-8859-6", 36, 1, false));
        definitionList.add(createEncodingDefinition("ISO8859_7", "ISO-8859-7", 37, 1, false));
        definitionList.add(createEncodingDefinition("ISO8859_8", "ISO-8859-8", 38, 1, false));
        definitionList.add(createEncodingDefinition("ISO8859_9", "ISO-8859-9", 39, 1, false));
        definitionList.add(createEncodingDefinition("ISO8859_13", "ISO-8859-13", 40, 1, false));
        definitionList.add(createEncodingDefinition("KSC_5601", "MS949", 44, 2, false));
        definitionList.add(createEncodingDefinition("DOS852", "Cp852", 45, 1, false));
        definitionList.add(createEncodingDefinition("DOS857", "Cp857", 46, 1, false));
        definitionList.add(createEncodingDefinition("DOS861", "Cp861", 47, 1, false));
        definitionList.add(createEncodingDefinition("DOS866", "Cp866", 48, 1, false));
        definitionList.add(createEncodingDefinition("DOS869", "Cp869", 49, 1, false));
        definitionList.add(createEncodingDefinition("CYRL", null, 50, 1, false));
        definitionList.add(createEncodingDefinition("WIN1250", "Cp1250", 51, 1, false));
        definitionList.add(createEncodingDefinition("WIN1251", "Cp1251", 52, 1, false));
        definitionList.add(createEncodingDefinition("WIN1252", "Cp1252", 53, 1, false));
        definitionList.add(createEncodingDefinition("WIN1253", "Cp1253", 54, 1, false));
        definitionList.add(createEncodingDefinition("WIN1254", "Cp1254", 55, 1, false));
        definitionList.add(createEncodingDefinition("BIG_5", "Big5", 56, 2, false));
        definitionList.add(createEncodingDefinition("GB_2312", "EUC_CN", 57, 2, false));
        definitionList.add(createEncodingDefinition("WIN1255", "Cp1255", 58, 1, false));
        definitionList.add(createEncodingDefinition("WIN1256", "Cp1256", 59, 1, false));
        definitionList.add(createEncodingDefinition("WIN1257", "Cp1257", 60, 1, false));
        definitionList.add(createEncodingDefinition("KOI8R", "KOI8_R", 63, 1, false));
        definitionList.add(createEncodingDefinition("KOI8U", "KOI8_U", 64, 1, false));
        definitionList.add(createEncodingDefinition("WIN1258", "Cp1258", 65, 1, false));
        definitionList.add(createEncodingDefinition("TIS620", "TIS620", 66, 1, false));
        definitionList.add(createEncodingDefinition("GBK", "GBK", 67, 2, false));
        definitionList.add(createEncodingDefinition("CP943C", "Cp943C", 68, 2, false));
        definitionList.add(createEncodingDefinition("GB18030", "GB18030", 69, 3, false));

        Iterator<EncodingDefinition> iterator = definitionList.iterator();
        while (iterator.hasNext()) {
            if (iterator.next() == null) {
                iterator.remove();
            }
        }

        return Collections.unmodifiableList(new ArrayList<>(definitionList));
    }

    /**
     * Creates an encoding definition.
     *
     * @param firebirdName
     *         Firebird name of the character set
     * @param javaName
     *         Name of the Java character set (or {@code null} for an information-only encoding). Unsupported character
     *         set names are also handled as information-only. Illegal names (according to the rules established in
     *         {@link java.nio.charset.Charset} will cause this method to return {@code null}
     * @param characterSetId
     *         The Firebird id of the character set (as listed in {@code RDB$CHARACTER_SETS}). Value {@code 127} ({@code
     *         CS_dynamic}) is not allowed and will be ignored.
     * @param maxBytesPerCharacter
     *         Max bytes per character
     * @param firebirdOnly
     *         {@code true} if this encoding should only be used to map from Firebird to Java, not in reverse. This is
     *         for example used to map Firebird encoding {@code UNICODE-FSS} to Java encoding {@code UTF-8}, but in
     *         reverse Java encoding {@code UTF-8} is mapped to Firebird encoding {@code UTF8}.
     * @return Encoding definition, or {@code null} if we failed to create an encoding definition (eg illegal Java
     * character set name, or other failures)
     */
    public static EncodingDefinition createEncodingDefinition(final String firebirdName, final String javaName,
            final int characterSetId, final int maxBytesPerCharacter, final boolean firebirdOnly) {
        try {
            try {
                final Charset charset = javaName != null ? Charset.forName(javaName) : null;
                return new DefaultEncodingDefinition(firebirdName, charset, maxBytesPerCharacter, characterSetId,
                        firebirdOnly);
            } catch (IllegalCharsetNameException e) {
                logger.warn(String.format("javaName=\"%s\" specified for encoding \"%s\" is an illegal character set name, skipping encoding",
                        javaName, firebirdName), e);
            } catch (UnsupportedCharsetException e) {
                logger.warn(String.format("javaName=\"%s\" specified for encoding \"%s\" is not supported by the jvm, creating information-only EncodingDefinition",
                        javaName, firebirdName));
                // Create an 'information-only' definition by using null for charset
                return new DefaultEncodingDefinition(firebirdName, null, maxBytesPerCharacter, characterSetId,
                        firebirdOnly);
            }
        } catch (Exception e) {
            logger.warn(String.format("Loading information for encoding \"%s\" failed with an Exception", firebirdName),
                    e);
        }
        return null;
    }

}
