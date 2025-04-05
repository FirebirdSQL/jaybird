// SPDX-FileCopyrightText: Copyright 2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire;

import org.jspecify.annotations.NullMarked;

/**
 * Response instance for an inline blob ({@code op_inline_blob}) response.
 *
 * @param transactionHandle
 *         transaction handle the inline blob is valid for
 * @param blobId
 *         blob id
 * @param info
 *         byte array with blob info
 * @param data
 *         byte array with blob data (without segments)
 * @author Mark Rotteveel
 * @since 7
 */
@NullMarked
public record InlineBlobResponse(int transactionHandle, long blobId, byte[] info, byte[] data) implements Response {

    public InlineBlob toInlineBlob(FbWireDatabase database) {
        return new InlineBlob(database, blobId, transactionHandle, info, data);
    }

}
