// SPDX-FileCopyrightText: Copyright 2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.impl;

import org.firebirdsql.gds.BlobParameterBuffer;

import java.io.Serial;

/**
 * Empty and immutable blob parameter buffer.
 *
 * @since 7
 */
public final class EmptyBlobParameterBuffer extends EmptyParameterBufferBase implements BlobParameterBuffer {

    @Serial
    private static final long serialVersionUID = 3587241258181937084L;
    private static final EmptyBlobParameterBuffer INSTANCE = new EmptyBlobParameterBuffer();

    private EmptyBlobParameterBuffer() {
        super(BlobParameterBufferImp.BpbMetaData.BPB_VERSION_1);
    }

    public static EmptyBlobParameterBuffer empty() {
        return INSTANCE;
    }

    @Serial
    private Object readResolve() {
        return empty();
    }

}
