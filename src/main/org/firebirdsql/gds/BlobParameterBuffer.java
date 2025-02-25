// SPDX-FileCopyrightText: Copyright 2003 Ryan Baldwin
// SPDX-FileCopyrightText: Copyright 2004-2005 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2015-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds;

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
 */
public interface BlobParameterBuffer extends ParameterBuffer {

    /**
     * Set a void (valueless) parameter on this {@code BlobParameterBuffer}.
     *
     * @param argumentType The parameter to be set, either an {@code ISCConstants.isc_bpb_*} constant, or one of the
     *        constants of this interface
     */
    @Override
    void addArgument(int argumentType);

    /**
     * Set a {@code String} parameter on this {@code BlobParameterBuffer}.
     *
     * @param argumentType The parameter to be set, either an {@code ISCConstants.isc_bpb_*} constant, or one of the
     *        constants of this interface
     * @param value The value to set for the given parameter
     */
    @Override
    void addArgument(int argumentType, String value);

    /**
     * Set an {@code int} parameter on this {@code BlobParameterBuffer}.
     *
     * @param argumentType The parameter to be set, either an {@code ISCConstants.isc_bpb_*} constant, or one of the
     *        constants of this interface
     * @param value The value to set for the given parameter
     */
    @Override
    void addArgument(int argumentType, int value);

}
