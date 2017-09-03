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
package org.firebirdsql.gds.ng.jna;

import org.firebirdsql.encodings.IEncodingFactory;
import org.firebirdsql.gds.ng.DefaultDatatypeCoder;

/**
 * Datatype encoder and decoder for little endian platforms, specifically for use with the Firebird client library.
 * <p>
 * For wire protocol use {@link org.firebirdsql.gds.ng.DefaultDatatypeCoder}.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
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
    public byte[] encodeShort(short value) {
        byte ret[] = new byte[2];
        ret[0] = (byte) (value & 0xff);
        ret[1] = (byte) ((value >>> 8) & 0xff);
        return ret;
    }

    @Override
    public short decodeShort(byte[] byte_int) {
        int b1 = byte_int[0] & 0xFF;
        int b2 = byte_int[1] & 0xFF;

        return (short) (b1 + (b2 << 8));
    }

    @Override
    public byte[] encodeInt(int value) {
        byte ret[] = new byte[4];
        ret[0] = (byte) (value & 0xff);
        ret[1] = (byte) ((value >>> 8) & 0xff);
        ret[2] = (byte) ((value >>> 16) & 0xff);
        ret[3] = (byte) ((value >>> 24) & 0xff);
        return ret;
    }

    @Override
    public int decodeInt(byte[] byte_int) {
        int b1 = byte_int[0] & 0xFF;
        int b2 = byte_int[1] & 0xFF;
        int b3 = byte_int[2] & 0xFF;
        int b4 = byte_int[3] & 0xFF;
        return (b1 + (b2 << 8) + (b3 << 16) + (b4 << 24));
    }

    @Override
    public byte[] encodeLong(long value) {
        byte[] ret = new byte[8];
        ret[0] = (byte) (value & 0xFF);
        ret[1] = (byte) (value >>> 8 & 0xFF);
        ret[2] = (byte) (value >>> 16 & 0xFF);
        ret[3] = (byte) (value >>> 24 & 0xFF);
        ret[4] = (byte) (value >>> 32 & 0xFF);
        ret[5] = (byte) (value >>> 40 & 0xFF);
        ret[6] = (byte) (value >>> 48 & 0xFF);
        ret[7] = (byte) (value >>> 56 & 0xFF);
        return ret;
    }

    @Override
    public long decodeLong(byte[] byte_int) {
        long b1 = byte_int[0] & 0xFF;
        long b2 = byte_int[1] & 0xFF;
        long b3 = byte_int[2] & 0xFF;
        long b4 = byte_int[3] & 0xFF;
        long b5 = byte_int[4] & 0xFF;
        long b6 = byte_int[5] & 0xFF;
        long b7 = byte_int[6] & 0xFF;
        long b8 = byte_int[7] & 0xFF;
        return (b1 + (b2 << 8) + (b3 << 16) + (b4 << 24) + (b5 << 32)
                + (b6 << 40) + (b7 << 48) + (b8 << 56));
    }
}
