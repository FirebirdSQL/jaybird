// SPDX-FileCopyrightText: Copyright 2003 Ryan Baldwin
// SPDX-FileCopyrightText: Copyright 2004-2005 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2015-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds;

import org.firebirdsql.gds.impl.EmptyBlobParameterBuffer;
import org.jspecify.annotations.Nullable;

/**
 * Instance of this interface represents a BLOB Parameter Buffer from the
 * Firebird API documentation and specifies attributes for
 * {@link org.firebirdsql.gds.ng.FbDatabase#createBlobForOutput(org.firebirdsql.gds.ng.FbTransaction, BlobParameterBuffer)}
 * or
 * {@link org.firebirdsql.gds.ng.FbDatabase#createBlobForInput(org.firebirdsql.gds.ng.FbTransaction, BlobParameterBuffer, long)}
 * operations.
 * <p>
 * Two features are available:
 * <ul>
 * <li>Specifying the source and target BLOB types (server uses BLOB filters to
 * perform the conversion)</li>
 * <li>Specifying type of the BLOB - either segmented or stream. The only
 * visible to user difference between segmented and stream BLOBs is the fact
 * that "seek" operation is not defined for segmented BLOBs (see
 * {@link org.firebirdsql.gds.ng.FbBlob#seek(int, org.firebirdsql.gds.ng.FbBlob.SeekMode)}
 * for more details).</li>
 * </ul>
 * </p>
 * <p>
 * Immutable implementations throw {@link UnsupportedOperationException} for mutating operations.
 * </p>
 */
public interface BlobParameterBuffer extends ParameterBuffer {

    /**
     * Set a void (valueless) parameter on this {@code BlobParameterBuffer}.
     *
     * @param argumentType
     *         parameter to be set, an {@link org.firebirdsql.jaybird.fb.constants.BpbItems} constant
     */
    @Override
    void addArgument(int argumentType);

    /**
     * Set a {@code String} parameter on this {@code BlobParameterBuffer}.
     *
     * @param argumentType
     *         parameter to be set, an {@link org.firebirdsql.jaybird.fb.constants.BpbItems} constant
     * @param value
     *         value of parameter
     */
    @Override
    void addArgument(int argumentType, String value);

    /**
     * Set an {@code int} parameter on this {@code BlobParameterBuffer}.
     *
     * @param argumentType
     *         parameter to be set, an {@link org.firebirdsql.jaybird.fb.constants.BpbItems} constant
     * @param value
     *         value of parameter
     */
    @Override
    void addArgument(int argumentType, int value);

    /**
     * Returns an empty and immutable blob parameter buffer.
     *
     * @return empty and immutable blob parameter buffer
     * @see #orEmpty(BlobParameterBuffer)
     * @since 7
     */
    static BlobParameterBuffer empty() {
        return EmptyBlobParameterBuffer.empty();
    }

    /**
     * Returns either the input if not {@code null}, or an empty and immutable blob parameter buffer.
     *
     * @param blobParameterBuffer
     *         possibly {@code null} blob parameter buffer
     * @return either {@code blobParameterBuffer} if not {@code null}, or an empty and immutable blob parameter buffer
     * @see #empty()
     * @since 7
     */
    static BlobParameterBuffer orEmpty(@Nullable BlobParameterBuffer blobParameterBuffer) {
        return blobParameterBuffer != null ? blobParameterBuffer : empty();
    }

}
