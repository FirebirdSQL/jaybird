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

/* added by Blas Rodriguez Somoza:
 *
 * CVS modification log:
 * $Log$
 * Revision 1.4  2003/06/04 12:38:22  brodsom
 * Remove unused vars and imports
 *
 * Revision 1.3  2003/01/26 00:50:07  brodsom
 * New character sets support
 *
 * Revision 1.2  2003/01/23 01:37:05  brodsom
 * Encodings patch
 *
 */

package org.firebirdsql.encodings;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class EncodingFactory {

    static String defaultEncoding = null;

    static {
        InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(new byte[2])); 
        defaultEncoding = reader.getEncoding();
        try {
            reader.close();
        }
        catch (IOException ioe){
        }
    }
    
    public static Encoding getEncoding(String encoding){
        if (encoding == null || encoding.equals("NONE"))
            encoding = defaultEncoding;
        if (encoding.equals("Cp1250"))
            return new Encoding_Cp1250();
        else if (encoding.equals("Cp1251"))
            return new Encoding_Cp1251();
        else if (encoding.equals("Cp1252"))
            return new Encoding_Cp1252();
        else if (encoding.equals("Cp1253"))
            return new Encoding_Cp1253();
        else if (encoding.equals("Cp1254"))
            return new Encoding_Cp1254();
        else if (encoding.equals("Cp1255"))
            return new Encoding_Cp1255();
        else if (encoding.equals("Cp1256"))
            return new Encoding_Cp1256();
        else if (encoding.equals("Cp1257"))
            return new Encoding_Cp1257();
        else if (encoding.equals("Cp437"))
            return new Encoding_Cp437();
        else if (encoding.equals("Cp737"))
            return new Encoding_Cp737();
        else if (encoding.equals("Cp775"))
            return new Encoding_Cp775();
        else if (encoding.equals("Cp850"))
            return new Encoding_Cp850();
        else if (encoding.equals("Cp852"))
            return new Encoding_Cp852();
        else if (encoding.equals("Cp857"))
            return new Encoding_Cp857();
        else if (encoding.equals("Cp858"))
            return new Encoding_Cp858();
        else if (encoding.equals("Cp860"))
            return new Encoding_Cp860();
        else if (encoding.equals("Cp861"))
            return new Encoding_Cp861();
        else if (encoding.equals("Cp862"))
            return new Encoding_Cp862();
        else if (encoding.equals("Cp863"))
            return new Encoding_Cp863();
        else if (encoding.equals("Cp864"))
            return new Encoding_Cp864();
        else if (encoding.equals("Cp865"))
            return new Encoding_Cp865();
        else if (encoding.equals("Cp866"))
            return new Encoding_Cp866();
        else if (encoding.equals("Cp869"))
            return new Encoding_Cp869();
        else if (encoding.equals("ISO8859_1"))
            return new Encoding_ISO8859_1();
        else if (encoding.equals("ISO8859_2"))
            return new Encoding_ISO8859_2();
        else if (encoding.equals("ISO8859_3"))
            return new Encoding_ISO8859_3();
        else if (encoding.equals("ISO8859_4"))
            return new Encoding_ISO8859_4();
        else if (encoding.equals("ISO8859_5"))
            return new Encoding_ISO8859_5();
        else if (encoding.equals("ISO8859_6"))
            return new Encoding_ISO8859_6();
        else if (encoding.equals("ISO8859_7"))
            return new Encoding_ISO8859_7();
        else if (encoding.equals("ISO8859_8"))
            return new Encoding_ISO8859_8();
        else if (encoding.equals("ISO8859_9"))
            return new Encoding_ISO8859_9();
        else if (encoding.equals("ISO8859_13"))
            return new Encoding_ISO8859_13();
        else 
            return new Encoding_NotOneByte(encoding);
    }
}
