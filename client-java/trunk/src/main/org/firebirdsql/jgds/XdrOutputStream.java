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

/*
 * The Original Code is the Firebird Java GDS implementation.
 *
 * The Initial Developer of the Original Code is Alejandro Alberola.
 * Portions created by Alejandro Alberola are Copyright (C) 2001
 * Boix i Oltra, S.L. All Rights Reserved.
 */

package org.firebirdsql.jgds;

import java.io.*;
import java.util.Set;
import java.util.Iterator;

import org.firebirdsql.jdbc.FBConnectionHelper;

import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

/**
 * Describe class <code>XdrOutputStream</code> here.
 *
 * @author <a href="mailto:alberola@users.sourceforge.net">Alejandro Alberola</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
class XdrOutputStream extends DataOutputStream {

    private final static Logger log = LoggerFactory.getLogger(XdrOutputStream.class,false);
    private final static byte[] pad = {0,0,0,0};
    static final byte[] textPad = new byte[32767];

    public XdrOutputStream(OutputStream out) {
        super(out);
        // fill the padding with blanks
        java.util.Arrays.fill(textPad,(byte) 32);
    }
	 
    public final void writeOpaque(byte[] buffer, int len) throws IOException {
        if (buffer != null && len > 0) {
            write(buffer, 0, len);
            write(pad,0,((4 - len) & 3));
        }
    }

    public final void writeBuffer(byte[] buffer, int len) throws IOException {
        writeInt(len);
        if (buffer != null && len > 0) {
            write(buffer, 0, len);
            write(pad,0,((4 - len) & 3));
        }
    }

    public final void writeBlobBuffer(byte[] buffer) throws IOException {
        int len = buffer.length ; // 2 for short for buffer length
        if (log != null) log.debug("writeBlobBuffer len: " + len);
        if (len > Short.MAX_VALUE) {
            throw new IOException(""); //Need a value???
        }
        writeInt(len + 2);
        writeInt(len + 2); //bizarre but true! three copies of the length
        write((len >> 0) & 0xff);
        write((len >> 8) & 0xff);
        write(buffer, 0, len);
        if (log != null) log.debug("writeBlobBuffer wrotebuffer bytes: " + len);
        write(pad,0,((4 - len+2) & 3));
    }

    public final void writeString(String s) throws IOException {
        byte[] buffer = s.getBytes();
		  int len = buffer.length;
        writeInt(len);
        if (len > 0) {
            write(buffer, 0, len);
            write(pad,0,((4 - len) & 3));
        }		
    }
    
    public final void writeString(String s, String encoding) throws IOException {
        String javaEncoding = null;
        
        if (encoding != null && !"NONE".equals(encoding))
            javaEncoding = FBConnectionHelper.getJavaEncoding(encoding);
        
        byte[] buffer;
        
        if (javaEncoding != null)
            buffer = s.getBytes(javaEncoding);
        else
            buffer = s.getBytes();
            
        writeBuffer(buffer, buffer.length);
    }
    
    public final void writeSet(int type, Set s) throws IOException {
//      if (log != null) log.debug("writeSet: type: " + type);
        if (s == null) {
            writeInt(1);
            write(type); //e.g. gds.isc_tpb_version3
        }
        else {
            writeInt(s.size() + 1);
            write(type);
            Iterator i = s.iterator();
            while (i.hasNext()) {
                int n = ((Integer)i.next()).intValue();
                write(n);
//              if (log != null) log.debug("writeSet: value: " + n);
            }
//          if (log != null) log.debug("writeSet: padding 0 : " + ((4 - (s.size() + 1)) & 3));
            write(pad,0,((4 - (s.size() + 1)) & 3));
        }
    }



    final void writeTyped(int type, Xdrable item) throws IOException {
        int size;
        if (item == null) {
            writeInt(1);
            write(type); //e.g. gds.isc_tpb_version3
            size = 1;
        }
        else {
            size = item.getLength() + 1;
            writeInt(size);
            write(type);
            item.write(this);
        }
        write(pad,0,((4 - size) & 3));
    }
    // 
    // This method fill the char up to len with bytes 
    // 
    public final void writeChar(byte[] buffer, int len) throws IOException {
        if (buffer != null) {
            if (buffer.length >=len)
                write(buffer, 0, len);
            else{
                write(buffer, 0, buffer.length);
                write(textPad, 0, len-buffer.length);
            }
            write(pad,0,((4 - len) & 3));
        }
    }
	 
}
