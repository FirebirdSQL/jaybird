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

package org.firebirdsql.gds;


import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

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
public final class XdrOutputStream {

    private static final int BUF_SIZE = 32767;
    private static final int BUF_MAX = (int)(BUF_SIZE * 0.8);

    private static Logger log = LoggerFactory.getLogger(XdrOutputStream.class,false);
    private static byte[] textPad = new byte[BUF_SIZE];
    private static byte[] zero = new XSQLVAR().encodeInt(0);   // todo
    private static byte[] minusOne = new XSQLVAR().encodeInt(-1);

    private byte[] buf = new byte[BUF_SIZE];

    private int count;

    private OutputStream out = null;

    public XdrOutputStream(OutputStream out) {
        this.out = out;
        count=0;
        // fill the padding with blanks
        Arrays.fill(textPad,(byte) 32);
    }

    public void writeBuffer(byte[] buffer) throws IOException {
        if (buffer == null)
            writeInt(0);
        else {
            int len = buffer.length;
            writeInt(len);
            write(buffer, len, (4 - len) & 3);
        }
    }

    public void writeBlobBuffer(byte[] buffer) throws IOException {
        int len = buffer.length ; // 2 for short for buffer length
        if (log != null && log.isDebugEnabled()) log.debug("writeBlobBuffer len: " + len);
        if (len > Short.MAX_VALUE) {
            throw new IOException(""); //Need a value???
        }
        writeInt(len + 2);
        writeInt(len + 2); //bizarre but true! three copies of the length
        buf[count++] = (byte) ((len >> 0) & 0xff);
        buf[count++] = (byte) ((len >> 8) & 0xff);
        write(buffer, len, ((4 - len+2)&3));
    }

    public void writeString(String s) throws IOException {
        byte[] buffer = s.getBytes();
        int len = buffer.length;
        writeInt(len);
        if (len > 0) {
            write(buffer, len, (4 - len) & 3);
        }
    }

    /**
     * @deprecated due to character translation feature use of this method
     * is discouraged
     */
    public void writeString(String s, String encoding) throws IOException {
        String javaEncoding = null;
        
        if (encoding != null && !"NONE".equals(encoding))
            javaEncoding = FBConnectionHelper.getJavaEncoding(encoding);
        
        if (javaEncoding != null)
            writeBuffer(s.getBytes(javaEncoding));
        else
            writeBuffer(s.getBytes());
    }

    public void writeSet(int type, byte[] s) throws IOException {
        if (s == null) {
            writeInt(1);
            buf[count++] = (byte) type; //e.g. gds.isc_tpb_version3
        }
        else {
            int len = s.length;
            writeInt(len + 1);
            buf[count++] = (byte) type;
            write(s, len, (4 - (len+1)) & 3);
        }
    }

    public void writeTyped(int type, Xdrable item) throws IOException {
        int size;
        if (item == null) {
            writeInt(1);
            buf[count++] = (byte) type; //e.g. gds.isc_tpb_version3
            size = 1;
        }
        else {
            size = item.getLength() + 1;
            writeInt(size);
            buf[count++] = (byte) type; //e.g. gds.isc_tpb_version3
            item.write(this);
        }
        count += (4 - size) & 3;
    }

    // 
    // WriteSQLData methods
    // 
    public void writeSQLData(XSQLDA xsqlda) throws IOException {
        for (int i = 0; i < xsqlda.sqld; i++) {
            XSQLVAR xsqlvar = xsqlda.sqlvar[i];
            if (log != null && log.isDebugEnabled()) {
                if (out == null) {
                    log.debug("db.out null in writeSQLDatum");
                }
                if (xsqlvar.sqldata == null) {
                    log.debug("sqldata null in writeSQLDatum: " + xsqlvar);
                }
                if (xsqlvar.sqldata == null) {
                    log.debug("sqldata still null in writeSQLDatum: " + xsqlvar);
                }
            }
            int len = xsqlda.ioLength[i];
            byte[] buffer = xsqlvar.sqldata;
            if (len==0) {
                if (buffer != null) {
                    len = buffer.length;
                    writeInt(len);
                    write(buffer, len, (4 - len) & 3);
                    // sqlind
                    write(zero, 4, 0);
                }
                else{
                    writeInt(0);
                    // sqlind
                    write(minusOne, 4, 0);
                }
            }
            else if (len < 0){
                if (buffer != null) {
                    write(buffer, -len, 0);
                    // sqlind
                    write(zero,4, 0);
                }
                else{
                    write(textPad, -len, 0);
                    // sqlind
                    write(minusOne, 4, 0);
                }
            }
            else {
                // decrement length because it was incremented before
                len--;
                if (buffer != null) {
                    int buflen = buffer.length;
                    if (buflen >=len){
                        write(buffer, len, (4 - len) & 3);
                    }
                    else{
                        write(buffer, buflen, 0);
                        write(textPad, len- buflen, (4 - len) & 3);
                    }
                    // sqlind
                    write(zero, 4, 0);
                }
                else{
                    write(textPad, len, (4 - len) & 3);
                    // sqlind
                    write(minusOne, 4, 0);
                }
            }
        }
    }

    //
    // DataOutputStream methods
    // 
    public final void writeLong(long v) throws IOException {
        buf[count++] = (byte) (v >>> 56 & 0xFF);
        buf[count++] = (byte) (v >>> 48 & 0xFF);
        buf[count++] = (byte) (v >>> 40 & 0xFF);
        buf[count++] = (byte) (v >>> 32 & 0xFF);
        buf[count++] = (byte) (v >>> 24 & 0xFF);
        buf[count++] = (byte) (v >>> 16 & 0xFF);
        buf[count++] = (byte) (v >>>  8 & 0xFF);
        buf[count++] = (byte) (v >>>  0 & 0xFF);
    }

    public final void writeInt(int v) throws IOException {
        buf[count++] = (byte) (v >>> 24);
        buf[count++] = (byte) (v >>> 16);
        buf[count++] = (byte) (v >>>  8);
        buf[count++] = (byte) (v >>>  0);
    }

    //
    // Buffering 
    // If the piece to write is greater than 128 bytes, write it directly
    //

    public void write(byte[] b, int len, int pad) throws IOException {
        if (len > 256 || count + len >= BUF_MAX){
            if (count > 0)
                out.write(buf, 0, count);
            out.write(b, 0, len);
            out.write(textPad, 0, pad);
            count = 0;
        }
        else {
            System.arraycopy(b, 0, buf, count, len);
            count += len + pad;
        }
    }

    public void write(int b) throws IOException {
        buf[count++] = (byte)b;
    }

    public void write(byte b[]) throws IOException{
        write(b,b.length, 0);
    }

    public void flush() throws IOException {
        if (count > 0){
            out.write(buf,0,count);
        }
        count=0;
        out.flush();
    }

    public void close() throws IOException {
        out.close();
    }
}
