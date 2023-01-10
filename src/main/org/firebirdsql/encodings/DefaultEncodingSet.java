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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.firebirdsql.gds.ISCConstants.CS_BINARY;
import static org.firebirdsql.gds.ISCConstants.CS_NONE;

/**
 * The default encoding set for Jaybird.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public final class DefaultEncodingSet implements EncodingSet {

    private final List<EncodingDefinition> encodingDefinitions = createEncodingDefinitions();

    @Override
    public int getPreferenceWeight() {
        return 0;
    }

    @Override
    public List<EncodingDefinition> getEncodings() {
        return encodingDefinitions;
    }

    /**
     * Creates all encoding definitions of the default set.
     *
     * @return List of encoding definitions
     */
    private static List<EncodingDefinition> createEncodingDefinitions() {
        return List.of(
                new DefaultEncodingDefinition("NONE", (Charset) null, 1, CS_NONE, false),
                new DefaultEncodingDefinition("OCTETS", (Charset) null, 1, CS_BINARY, false),
                new DefaultEncodingDefinition("ASCII", StandardCharsets.US_ASCII, 1, 2, false),
                new DefaultEncodingDefinition("UNICODE_FSS", StandardCharsets.UTF_8, 3, 3, true),
                new DefaultEncodingDefinition("UTF8", StandardCharsets.UTF_8, 4, 4, false),
                new DefaultEncodingDefinition("SJIS_0208", "MS932", 2, 5, false),
                new DefaultEncodingDefinition("EUCJ_0208", "EUC_JP", 2, 6, false),
                new DefaultEncodingDefinition("DOS737", "Cp737", 1, 9, false),
                new DefaultEncodingDefinition("DOS437", "Cp437", 1, 10, false),
                new DefaultEncodingDefinition("DOS850", "Cp850", 1, 11, false),
                new DefaultEncodingDefinition("DOS865", "Cp865", 1, 12, false),
                new DefaultEncodingDefinition("DOS860", "Cp860", 1, 13, false),
                new DefaultEncodingDefinition("DOS863", "Cp863", 1, 14, false),
                new DefaultEncodingDefinition("DOS775", "Cp775", 1, 15, false),
                new DefaultEncodingDefinition("DOS858", "Cp858", 1, 16, false),
                new DefaultEncodingDefinition("DOS862", "Cp862", 1, 17, false),
                new DefaultEncodingDefinition("DOS864", "Cp864", 1, 18, false),
                new DefaultEncodingDefinition("NEXT", (Charset) null, 1, 19, false),
                new DefaultEncodingDefinition("ISO8859_1", StandardCharsets.ISO_8859_1, 1, 21, false),
                new DefaultEncodingDefinition("ISO8859_2", "ISO-8859-2", 1, 22, false),
                new DefaultEncodingDefinition("ISO8859_3", "ISO-8859-3", 1, 23, false),
                new DefaultEncodingDefinition("ISO8859_4", "ISO-8859-4", 1, 34, false),
                new DefaultEncodingDefinition("ISO8859_5", "ISO-8859-5", 1, 35, false),
                new DefaultEncodingDefinition("ISO8859_6", "ISO-8859-6", 1, 36, false),
                new DefaultEncodingDefinition("ISO8859_7", "ISO-8859-7", 1, 37, false),
                new DefaultEncodingDefinition("ISO8859_8", "ISO-8859-8", 1, 38, false),
                new DefaultEncodingDefinition("ISO8859_9", "ISO-8859-9", 1, 39, false),
                new DefaultEncodingDefinition("ISO8859_13", "ISO-8859-13", 1, 40, false),
                new DefaultEncodingDefinition("KSC_5601", "MS949", 2, 44, false),
                new DefaultEncodingDefinition("DOS852", "Cp852", 1, 45, false),
                new DefaultEncodingDefinition("DOS857", "Cp857", 1, 46, false),
                new DefaultEncodingDefinition("DOS861", "Cp861", 1, 47, false),
                new DefaultEncodingDefinition("DOS866", "Cp866", 1, 48, false),
                new DefaultEncodingDefinition("DOS869", "Cp869", 1, 49, false),
                new DefaultEncodingDefinition("CYRL", (Charset) null, 1, 50, false),
                new DefaultEncodingDefinition("WIN1250", "Cp1250", 1, 51, false),
                new DefaultEncodingDefinition("WIN1251", "Cp1251", 1, 52, false),
                new DefaultEncodingDefinition("WIN1252", "Cp1252", 1, 53, false),
                new DefaultEncodingDefinition("WIN1253", "Cp1253", 1, 54, false),
                new DefaultEncodingDefinition("WIN1254", "Cp1254", 1, 55, false),
                new DefaultEncodingDefinition("BIG_5", "Big5", 2, 56, false),
                new DefaultEncodingDefinition("GB_2312", "EUC_CN", 2, 57, false),
                new DefaultEncodingDefinition("WIN1255", "Cp1255", 1, 58, false),
                new DefaultEncodingDefinition("WIN1256", "Cp1256", 1, 59, false),
                new DefaultEncodingDefinition("WIN1257", "Cp1257", 1, 60, false),
                new DefaultEncodingDefinition("KOI8R", "KOI8_R", 1, 63, false),
                new DefaultEncodingDefinition("KOI8U", "KOI8_U", 1, 64, false),
                new DefaultEncodingDefinition("WIN1258", "Cp1258", 1, 65, false),
                new DefaultEncodingDefinition("TIS620", "TIS620", 1, 66, false),
                new DefaultEncodingDefinition("GBK", "GBK", 2, 67, false),
                new DefaultEncodingDefinition("CP943C", "Cp943C", 2, 68, false),
                new DefaultEncodingDefinition("GB18030", "GB18030", 3, 69, false));
    }

}
