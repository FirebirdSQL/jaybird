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
 */

package org.firebirdsql.encodings;

import org.firebirdsql.jgds.XdrInputStream;
import org.firebirdsql.jgds.XdrOutputStream;

public class EncodingFactory {

    static String defaultEncoding = null;

    static {
        java.io.InputStreamReader reader = new java.io.InputStreamReader(new java.io.ByteArrayInputStream(new byte[2])); 
        defaultEncoding = reader.getEncoding();
        try {
            reader.close();
        }
        catch (java.io.IOException ioe){
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
        else if (encoding.equals("Cp437"))
            return new Encoding_Cp437();
        else if (encoding.equals("Cp850"))
            return new Encoding_Cp850();
        else if (encoding.equals("Cp852"))
            return new Encoding_Cp852();
        else if (encoding.equals("Cp857"))
            return new Encoding_Cp857();
        else if (encoding.equals("Cp860"))
            return new Encoding_Cp860();
        else if (encoding.equals("Cp861"))
            return new Encoding_Cp861();
        else if (encoding.equals("Cp863"))
            return new Encoding_Cp863();
        else if (encoding.equals("Cp865"))
            return new Encoding_Cp865();
        else if (encoding.equals("ISO8859_1"))
            return new Encoding_ISO8859_1();
        else if (encoding.equals("ISO8859_2"))
            return new Encoding_ISO8859_2();
        else 
            return new Encoding_NotOneByte(encoding);
    }
}
