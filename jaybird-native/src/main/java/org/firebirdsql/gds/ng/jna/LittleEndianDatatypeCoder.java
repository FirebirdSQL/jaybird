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
package org.firebirdsql.gds.ng.jna;

import org.firebirdsql.encodings.IEncodingFactory;
import org.firebirdsql.gds.ng.DefaultDatatypeCoder;

/**
 * Datatype encoder and decoder for little endian platforms, specifically for use with the Firebird client library.
 * <p>
 * For wire protocol use {@link org.firebirdsql.gds.ng.DefaultDatatypeCoder}.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 3
 */
public final class LittleEndianDatatypeCoder extends DefaultDatatypeCoder {

    /**
     * Returns an instance of {@code LittleEndianDatatypeCoder} for an encoding factory.
     *
     * @param encodingFactory
     *         encoding factory
     * @return datatype coder, this might be a cached instance
     */
    public static LittleEndianDatatypeCoder forEncodingFactory(IEncodingFactory encodingFactory) {
        return encodingFactory
                .getOrCreateDatatypeCoder(LittleEndianDatatypeCoder.class, LittleEndianDatatypeCoder::new);
    }

    /**
     * Creates a little-endian datatype coder for native access on little-endian platforms.
     * <p>
     * In almost all cases, it is better to use {@link #forEncodingFactory(IEncodingFactory)}.
     * </p>
     *
     * @param encodingFactory
     *         encoding factory
     */
    public LittleEndianDatatypeCoder(IEncodingFactory encodingFactory) {
        super(encodingFactory);
    }

    @Override
    public int sizeOfShort() {
        return 2;
    }

    @Override
    public void encodeShort(int val, byte[] buf, int off) {
        buf[off] = (byte) val;
        buf[off + 1] = (byte) (val >>> 8);
    }

    @Override
    public short decodeShort(byte[] buf, int off) {
        return (short)
                ((buf[off] & 0xFF) +
                 (buf[off + 1] << 8));
    }

    @Override
    public void encodeInt(int val, byte[] buf, int off) {
        buf[off] = (byte) val;
        buf[off + 1] = (byte) (val >>> 8);
        buf[off + 2] = (byte) (val >>> 16);
        buf[off + 3] = (byte) (val >>> 24);
    }

    @Override
    public int decodeInt(byte[] buf, int off) {
        return (buf[off] & 0xFF) +
               ((buf[off + 1] & 0xFF) << 8) +
               ((buf[off + 2] & 0xFF) << 16) +
               (buf[off + 3] << 24);
    }

    @Override
    public byte[] encodeLong(long val) {
        final byte[] buf = new byte[8];
        buf[0] = (byte) val;
        buf[1] = (byte) (val >>> 8);
        buf[2] = (byte) (val >>> 16);
        buf[3] = (byte) (val >>> 24);
        buf[4] = (byte) (val >>> 32);
        buf[5] = (byte) (val >>> 40);
        buf[6] = (byte) (val >>> 48);
        buf[7] = (byte) (val >>> 56);
        return buf;
    }

    @Override
    public long decodeLong(byte[] buf) {
        if (buf == null) return 0;
        return (buf[0] & 0xFFL) +
               ((buf[1] & 0xFFL) << 8) +
               ((buf[2] & 0xFFL) << 16) +
               ((buf[3] & 0xFFL) << 24) +
               ((buf[4] & 0xFFL) << 32) +
               ((buf[5] & 0xFFL) << 40) +
               ((buf[6] & 0xFFL) << 48) +
               (((long) buf[7]) << 56);
    }

    @Override
    @SuppressWarnings("java:S1168")
    protected byte[] networkOrder(final byte[] buf) {
        if (buf == null) return null;
        final byte[] newArray = new byte[buf.length];
        final int maxIndex = newArray.length - 1;
        for (int idx = 0; idx <= maxIndex; idx++) {
            newArray[idx] = buf[maxIndex - idx];
        }
        return newArray;
    }

}
