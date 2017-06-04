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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.firebirdsql.gds.ISCConstants.CS_BINARY;
import static org.firebirdsql.gds.ISCConstants.CS_NONE;

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
        final Charset noCharset = null;
        final List<EncodingDefinition> definitionList = new ArrayList<>(52);
        definitionList.add(new DefaultEncodingDefinition("NONE", noCharset, 1, CS_NONE, false));
        definitionList.add(new DefaultEncodingDefinition("OCTETS", noCharset, 1, CS_BINARY, false));
        definitionList.add(new DefaultEncodingDefinition("ASCII", StandardCharsets.US_ASCII, 1, 2, false));
        definitionList.add(new DefaultEncodingDefinition("UNICODE_FSS", StandardCharsets.UTF_8, 3, 3, true));
        definitionList.add(new DefaultEncodingDefinition("UTF8", StandardCharsets.UTF_8, 4, 4, false));
        definitionList.add(new DefaultEncodingDefinition("SJIS_0208", "MS932", 2, 5, false));
        definitionList.add(new DefaultEncodingDefinition("EUCJ_0208", "EUC_JP", 2, 6, false));
        definitionList.add(new DefaultEncodingDefinition("DOS737", "Cp737", 1, 9, false));
        definitionList.add(new DefaultEncodingDefinition("DOS437", "Cp437", 1, 10, false));
        definitionList.add(new DefaultEncodingDefinition("DOS850", "Cp850", 1, 11, false));
        definitionList.add(new DefaultEncodingDefinition("DOS865", "Cp865", 1, 12, false));
        definitionList.add(new DefaultEncodingDefinition("DOS860", "Cp860", 1, 13, false));
        definitionList.add(new DefaultEncodingDefinition("DOS863", "Cp863", 1, 14, false));
        definitionList.add(new DefaultEncodingDefinition("DOS775", "Cp775", 1, 15, false));
        definitionList.add(new DefaultEncodingDefinition("DOS858", "Cp858", 1, 16, false));
        definitionList.add(new DefaultEncodingDefinition("DOS862", "Cp862", 1, 17, false));
        definitionList.add(new DefaultEncodingDefinition("DOS864", "Cp864", 1, 18, false));
        definitionList.add(new DefaultEncodingDefinition("NEXT", noCharset, 1, 19, false));
        definitionList.add(new DefaultEncodingDefinition("ISO8859_1", StandardCharsets.ISO_8859_1, 1, 21, false));
        definitionList.add(new DefaultEncodingDefinition("ISO8859_2", "ISO-8859-2", 1, 22, false));
        definitionList.add(new DefaultEncodingDefinition("ISO8859_3", "ISO-8859-3", 1, 23, false));
        definitionList.add(new DefaultEncodingDefinition("ISO8859_4", "ISO-8859-4", 1, 34, false));
        definitionList.add(new DefaultEncodingDefinition("ISO8859_5", "ISO-8859-5", 1, 35, false));
        definitionList.add(new DefaultEncodingDefinition("ISO8859_6", "ISO-8859-6", 1, 36, false));
        definitionList.add(new DefaultEncodingDefinition("ISO8859_7", "ISO-8859-7", 1, 37, false));
        definitionList.add(new DefaultEncodingDefinition("ISO8859_8", "ISO-8859-8", 1, 38, false));
        definitionList.add(new DefaultEncodingDefinition("ISO8859_9", "ISO-8859-9", 1, 39, false));
        definitionList.add(new DefaultEncodingDefinition("ISO8859_13", "ISO-8859-13", 1, 40, false));
        definitionList.add(new DefaultEncodingDefinition("KSC_5601", "MS949", 2, 44, false));
        definitionList.add(new DefaultEncodingDefinition("DOS852", "Cp852", 1, 45, false));
        definitionList.add(new DefaultEncodingDefinition("DOS857", "Cp857", 1, 46, false));
        definitionList.add(new DefaultEncodingDefinition("DOS861", "Cp861", 1, 47, false));
        definitionList.add(new DefaultEncodingDefinition("DOS866", "Cp866", 1, 48, false));
        definitionList.add(new DefaultEncodingDefinition("DOS869", "Cp869", 1, 49, false));
        definitionList.add(new DefaultEncodingDefinition("CYRL", noCharset, 1, 50, false));
        definitionList.add(new DefaultEncodingDefinition("WIN1250", "Cp1250", 1, 51, false));
        definitionList.add(new DefaultEncodingDefinition("WIN1251", "Cp1251", 1, 52, false));
        definitionList.add(new DefaultEncodingDefinition("WIN1252", "Cp1252", 1, 53, false));
        definitionList.add(new DefaultEncodingDefinition("WIN1253", "Cp1253", 1, 54, false));
        definitionList.add(new DefaultEncodingDefinition("WIN1254", "Cp1254", 1, 55, false));
        definitionList.add(new DefaultEncodingDefinition("BIG_5", "Big5", 2, 56, false));
        definitionList.add(new DefaultEncodingDefinition("GB_2312", "EUC_CN", 2, 57, false));
        definitionList.add(new DefaultEncodingDefinition("WIN1255", "Cp1255", 1, 58, false));
        definitionList.add(new DefaultEncodingDefinition("WIN1256", "Cp1256", 1, 59, false));
        definitionList.add(new DefaultEncodingDefinition("WIN1257", "Cp1257", 1, 60, false));
        definitionList.add(new DefaultEncodingDefinition("KOI8R", "KOI8_R", 1, 63, false));
        definitionList.add(new DefaultEncodingDefinition("KOI8U", "KOI8_U", 1, 64, false));
        definitionList.add(new DefaultEncodingDefinition("WIN1258", "Cp1258", 1, 65, false));
        definitionList.add(new DefaultEncodingDefinition("TIS620", "TIS620", 1, 66, false));
        definitionList.add(new DefaultEncodingDefinition("GBK", "GBK", 2, 67, false));
        definitionList.add(new DefaultEncodingDefinition("CP943C", "Cp943C", 2, 68, false));
        definitionList.add(new DefaultEncodingDefinition("GB18030", "GB18030", 3, 69, false));

        return Collections.unmodifiableList(definitionList);
    }

}
