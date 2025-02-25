// SPDX-FileCopyrightText: Copyright 2022-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.impl;

import org.firebirdsql.gds.BatchParameterBuffer;
import org.firebirdsql.gds.impl.argument.ArgumentType;
import org.firebirdsql.jaybird.fb.constants.BatchItems;

import java.io.Serial;

/**
 * Batch parameter buffer implementation.
 *
 * @author Mark Rotteveel
 * @since 5
 */
public final class BatchParameterBufferImp extends ParameterBufferBase implements BatchParameterBuffer {

    @Serial
    private static final long serialVersionUID = -6537114932080116496L;

    public BatchParameterBufferImp() {
        super(BatchPbMetaData.BATCH_VERSION_1);
    }

    private enum BatchPbMetaData implements ParameterBufferMetaData {
        BATCH_VERSION_1(BatchItems.BATCH_VERSION_1);

        private final int batchPbVersion;

        BatchPbMetaData(int batchPbVersion) {
            this.batchPbVersion = batchPbVersion;
        }

        @Override
        public int getType() {
            return batchPbVersion;
        }

        @Override
        public ArgumentType getStringArgumentType(int tag) {
            return ArgumentType.Wide;
        }

        @Override
        public ArgumentType getByteArrayArgumentType(int tag) {
            return ArgumentType.Wide;
        }

        @Override
        public ArgumentType getIntegerArgumentType(int tag) {
            return ArgumentType.Wide;
        }

        @Override
        public ArgumentType getSingleArgumentType(int tag) {
            return ArgumentType.Wide;
        }
    }
}
