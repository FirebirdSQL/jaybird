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
package org.firebirdsql.gds.impl.jni;

import org.firebirdsql.gds.XSQLVAR;

/**
 * Implementation of {@link XSQLVAR} class for big-endian platforms (like
 * Solaris).
 */
public class XSQLVARBigEndianImpl extends XSQLVARImpl {

    /**
     * Create default instance of this class.
     */
    public XSQLVARBigEndianImpl() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.firebirdsql.gds.XSQLVAR#deepCopy()
     */
    public XSQLVAR deepCopy() {
        XSQLVARBigEndianImpl result = new XSQLVARBigEndianImpl();
        result.copyFrom(this);
        return result;
    }

    /**
     * Create instance of this class for the specified XSQLVAR parameters.
     */
    public XSQLVARBigEndianImpl(int sqltype, int sqlscale, int sqlsubtype,
            int sqllen, byte[] sqldata, String sqlname, String relname,
            String ownname, String aliasname, String relaliasname) {
        super(sqltype, sqlscale, sqlsubtype, sqllen, sqldata, sqlname, relname,
                ownname, aliasname, relaliasname);
    }

    public byte[] encodeShort(short value) {
        byte ret[] = new byte[2];
        ret[1] = (byte) ((value >>> 0) & 0xff);
        ret[0] = (byte) ((value >>> 8) & 0xff);
        return ret;
    }

    public short decodeShort(byte[] byte_int) {
        int b1 = byte_int[1] & 0xFF;
        int b2 = byte_int[0] & 0xFF;

        return (short) ((b1 << 0) + (b2 << 8));
    }
}
