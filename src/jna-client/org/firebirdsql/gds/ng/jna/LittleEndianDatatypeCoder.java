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
 * @since 3.0
 */
public final class LittleEndianDatatypeCoder extends DefaultDatatypeCoder {

    /**
     * Returns an instance of {@code LittleEndianDatatypeCoder} for an encoding factory.
     *
     * @param encodingFactory Encoding factory
     * @return Datatype coder, this might be a cached instance
     */
    public static LittleEndianDatatypeCoder forEncodingFactory(IEncodingFactory encodingFactory) {
        return encodingFactory.getOrCreateDatatypeCoder(LittleEndianDatatypeCoder.class);
    }

    /**
     * Creates a little-endian datatype coder for native access on little-endian platforms.
     * <p>
     * In almost all cases, it is better to use {@link #forEncodingFactory(IEncodingFactory)}.
     * </p>
     *
     * @param encodingFactory Encoding factory
     */
    public LittleEndianDatatypeCoder(IEncodingFactory encodingFactory) {
        super(encodingFactory);
    }

    @Override
    public int sizeOfShort() {
        return 2;
    }

    @Override
    public void encodeShort(int value, byte[] target, int fromIndex) {
        target[fromIndex] = (byte) value;
        target[fromIndex + 1] = (byte) (value >>> 8);
    }

    @Override
    public short decodeShort(byte[] bytes, int fromIndex) {
        return (short)
                ((bytes[fromIndex] & 0xFF) +
                 (bytes[fromIndex + 1] << 8));
    }

    @Override
    public void encodeInt(int value, byte[] target, int fromIndex) {
        target[fromIndex] = (byte) value;
        target[fromIndex + 1] = (byte) (value >>> 8);
        target[fromIndex + 2] = (byte) (value >>> 16);
        target[fromIndex + 3] = (byte) (value >>> 24);
    }

    @Override
    public int decodeInt(byte[] bytes, int fromIndex) {
        return (bytes[fromIndex] & 0xFF) +
               ((bytes[fromIndex + 1] & 0xFF) << 8) +
               ((bytes[fromIndex + 2] & 0xFF) << 16) +
               (bytes[fromIndex + 3] << 24);
    }

    @Override
    public byte[] encodeLong(long value) {
        final byte[] buf = new byte[8];
        buf[0] = (byte) value;
        buf[1] = (byte) (value >>> 8);
        buf[2] = (byte) (value >>> 16);
        buf[3] = (byte) (value >>> 24);
        buf[4] = (byte) (value >>> 32);
        buf[5] = (byte) (value >>> 40);
        buf[6] = (byte) (value >>> 48);
        buf[7] = (byte) (value >>> 56);
        return buf;
    }

    @Override
    public long decodeLong(byte[] byte_int) {
        return (byte_int[0] & 0xFFL) +
               ((byte_int[1] & 0xFFL) << 8) +
               ((byte_int[2] & 0xFFL) << 16) +
               ((byte_int[3] & 0xFFL) << 24) +
               ((byte_int[4] & 0xFFL) << 32) +
               ((byte_int[5] & 0xFFL) << 40) +
               ((byte_int[6] & 0xFFL) << 48) +
               (((long) byte_int[7]) << 56);
    }

    @Override
    protected byte[] networkOrder(final byte[] buf) {
        final byte[] newArray = new byte[buf.length];
        final int maxIndex = newArray.length - 1;
        for (int idx = 0; idx <= maxIndex; idx++) {
            newArray[idx] = buf[maxIndex - idx];
        }
        return newArray;
    }

}
