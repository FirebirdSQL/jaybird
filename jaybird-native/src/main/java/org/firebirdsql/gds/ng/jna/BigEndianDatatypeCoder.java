// SPDX-FileCopyrightText: Copyright 2014-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.jna;

import org.firebirdsql.encodings.IEncodingFactory;
import org.firebirdsql.gds.ng.DefaultDatatypeCoder;

/**
 * Datatype encoder and decoder for big endian platforms, specifically for use with the Firebird client library.
 * <p>
 * For wire protocol use {@link org.firebirdsql.gds.ng.DefaultDatatypeCoder}.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 3
 */
public final class BigEndianDatatypeCoder extends DefaultDatatypeCoder {

    /**
     * Returns an instance of {@code BigEndianDatatypeCoder} for an encoding factory.
     *
     * @param encodingFactory
     *         encoding factory
     * @return datatype coder, this might be a cached instance
     */
    public static BigEndianDatatypeCoder forEncodingFactory(IEncodingFactory encodingFactory) {
        return encodingFactory.getOrCreateDatatypeCoder(BigEndianDatatypeCoder.class, BigEndianDatatypeCoder::new);
    }

    /**
     * Creates a big-endian datatype coder for native access on big-endian platforms.
     * <p>
     * In almost all cases, it is better to use {@link #forEncodingFactory(IEncodingFactory)}.
     * </p>
     *
     * @param encodingFactory
     *         encoding factory
     */
    public BigEndianDatatypeCoder(IEncodingFactory encodingFactory) {
        super(encodingFactory);
    }

    @Override
    public int sizeOfShort() {
        return 2;
    }

    @Override
    public void encodeShort(int val, byte[] buf, int off) {
        buf[off] = (byte) (val >>> 8);
        buf[off + 1] = (byte) val;
    }

    @Override
    public short decodeShort(byte[] buf, int off) {
        return (short)
                ((buf[off] << 8) +
                 (buf[off + 1] & 0xFF));
    }
}
