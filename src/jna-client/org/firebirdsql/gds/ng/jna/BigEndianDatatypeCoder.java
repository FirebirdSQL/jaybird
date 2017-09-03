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
 * Datatype encoder and decoder for big endian platforms, specifically for use with the Firebird client library.
 * <p>
 * For wire protocol use {@link org.firebirdsql.gds.ng.DefaultDatatypeCoder}.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public final class BigEndianDatatypeCoder extends DefaultDatatypeCoder {

    /**
     * Returns an instance of {@code BigEndianDatatypeCoder} for an encoding factory.
     *
     * @param encodingFactory Encoding factory
     * @return Datatype coder, this might be a cached instance
     */
    public static BigEndianDatatypeCoder forEncodingFactory(IEncodingFactory encodingFactory) {
        return encodingFactory.getOrCreateDatatypeCoder(BigEndianDatatypeCoder.class);
    }

    /**
     * Creates a big-endian datatype coder for native access on big-endian platforms.
     * <p>
     * In almost all cases, it is better to use {@link #forEncodingFactory(IEncodingFactory)}.
     * </p>
     *
     * @param encodingFactory Encoding factory
     */
    public BigEndianDatatypeCoder(IEncodingFactory encodingFactory) {
        super(encodingFactory);
    }

    @Override
    public byte[] encodeShort(short value) {
        byte ret[] = new byte[2];
        ret[1] = (byte) (value & 0xff);
        ret[0] = (byte) ((value >>> 8) & 0xff);
        return ret;
    }

    @Override
    public short decodeShort(byte[] byte_int) {
        int b1 = byte_int[1] & 0xFF;
        int b2 = byte_int[0] & 0xFF;

        return (short) (b1 + (b2 << 8));
    }
}
