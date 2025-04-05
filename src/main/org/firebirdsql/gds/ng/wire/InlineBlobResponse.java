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
package org.firebirdsql.gds.ng.wire;

/**
 * Response instance for an inline blob ({@code op_inline_blob}) response.
 *
 * @author Mark Rotteveel
 * @since 5.0.8
 */
public final class InlineBlobResponse implements Response {

    private final int transactionHandle;
    private final long blobId;
    private final byte[] info;
    private final byte[] data;

    /**
     * Creates an inline blob response.
     *
     * @param transactionHandle
     *         transaction handle the inline blob is valid for
     * @param blobId
     *         blob id
     * @param info
     *         byte array with blob info
     * @param data
     *         byte array with blob data (without segments)
     */
    public InlineBlobResponse(int transactionHandle, long blobId, byte[] info, byte[] data) {
        this.transactionHandle = transactionHandle;
        this.blobId = blobId;
        this.info = info;
        this.data = data;
    }

    public int transactionHandle() {
        return transactionHandle;
    }

    public long blobId() {
        return blobId;
    }

    public byte[] info() {
        return info;
    }

    public byte[] data() {
        return data;
    }

    public InlineBlob toInlineBlob(FbWireDatabase database) {
        return new InlineBlob(database, blobId, transactionHandle, info, data);
    }

}
