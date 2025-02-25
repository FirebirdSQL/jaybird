// SPDX-FileCopyrightText: Copyright 2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.BlobParameterBuffer;

/**
 * Firebird configuration for a blob.
 *
 * @author Mark Rotteveel
 * @since 5
 */
public interface BlobConfig {

    /**
     * Writes out the configuration for an output blob for a {@code create} operation (writing a blob).
     *
     * @param blobParameterBuffer
     *         a blob parameter buffer to be configured by this instance (implementations can assume it is empty)
     */
    void writeOutputConfig(BlobParameterBuffer blobParameterBuffer);

    /**
     * Writes out the configuration for an input blob for an {@code open} operation (reading a blob).
     *
     * @param blobParameterBuffer
     *         a blob parameter buffer to be configured by this instance (implementations can assume it is empty)
     */
    void writeInputConfig(BlobParameterBuffer blobParameterBuffer);

}
