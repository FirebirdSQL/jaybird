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
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

public class XdrInputStream extends FilterInputStream {

   private final Logger log = LoggerFactory.getLogger(getClass());

    public XdrInputStream(InputStream in) {
        super(in);
    }

    public short readShort() throws IOException {
        return (short) readInt();
    }

    public int readInt() throws IOException {
        int b1 = in.read();
        int b2 = in.read();
        int b3 = in.read();
        int b4 = in.read();
        if ((b1 | b2 | b3 | b4) < 0) {
            throw new EOFException();
        }
        return ((b1 << 24) + (b2 << 16) + (b3 << 8) + (b4 << 0));
    }

    public long readLong() throws IOException {
        return ((long) (readInt()) << 32) + (readInt() & 0xffffffffL);
    }

    public final float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    public final double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

    public final byte[] readOpaque(int len) throws IOException {
        byte[] buffer = new byte[len];
        int pos = 0;
        int chunk;
        while (pos < len) {
            chunk = in.read(buffer, pos, len - pos);
            if (chunk<0)
                throw new EOFException();
            pos += chunk;
        }
        for (int i = 0; i < ((4 - len) & 3); i++) {
            in.read();
        }
        return buffer;
    }

    public final byte[] readBuffer() throws IOException {
        return readOpaque(readInt());
    }


    public final String readString() throws IOException {
        return new String(readBuffer());
    }
}
