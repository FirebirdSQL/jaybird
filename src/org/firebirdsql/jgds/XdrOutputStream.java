/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is the Firebird Java GDS implementation.
 *
 * The Initial Developer of the Original Code is Alejandro Alberola.
 * Portions created by Alejandro Alberola are Copyright (C) 2001
 * Boix i Oltra, S.L. All Rights Reserved.
 *
 * Contributor(s):
 *
 * Alternatively, the contents of this file may be used under the
 * terms of the GNU Lesser General Public License Version 2.1 or later
 * (the "LGPL"), in which case the provisions of the LGPL are applicable
 * instead of those above.  If you wish to allow use of your
 * version of this file only under the terms of the LGPL and not to
 * allow others to use your version of this file under the MPL,
 * indicate your decision by deleting the provisions above and
 * replace them with the notice and other provisions required by
 * the LGPL.  If you do not delete the provisions above, a recipient
 * may use your version of this file under either the MPL or the
 * LGPL.
 */

package org.firebirdsql.jgds;

import java.io.*;
import java.util.Set;
import java.util.Iterator;

import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

class XdrOutputStream extends DataOutputStream {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final byte[] pad = {0,0,0,0};

    public XdrOutputStream(OutputStream out) {
        super(out);
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
}
