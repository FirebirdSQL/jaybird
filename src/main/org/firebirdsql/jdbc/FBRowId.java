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
package org.firebirdsql.jdbc;

import org.firebirdsql.util.ByteArrayHelper;

import java.sql.RowId;
import java.util.Arrays;
import java.util.Objects;

/**
 * Firebird row-id.
 * <p>
 * This is a thin wrapper around the bytes of a {@code RDB$DB_KEY}.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.1
 */
public class FBRowId implements RowId {

    private final byte[] rowIdBytes;

    /**
     * Creates an {@code FBRowId} from the provided byte array.
     *
     * @param rowIdBytes
     *         Bytes of the row id
     */
    public FBRowId(byte[] rowIdBytes) {
        this.rowIdBytes = Objects.requireNonNull(rowIdBytes).clone();
    }

    @Override
    public String toString() {
        return ByteArrayHelper.toHexString(rowIdBytes);
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Implementation note</b>: allows for equality with any implementation of {@link java.sql.RowId}.
     * </p>
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof RowId && Arrays.equals(rowIdBytes, ((RowId) obj).getBytes());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(rowIdBytes);
    }

    /**
     * {@inheritDoc}
     * <p>
     * NOTE: implementation will return a clone of the array.
     * </p>
     */
    @Override
    public byte[] getBytes() {
        return rowIdBytes.clone();
    }

}
