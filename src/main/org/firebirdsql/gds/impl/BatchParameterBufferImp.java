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
package org.firebirdsql.gds.impl;

import org.firebirdsql.gds.BatchParameterBuffer;
import org.firebirdsql.gds.impl.argument.ArgumentType;
import org.firebirdsql.jaybird.fb.constants.BatchItems;

/**
 * Batch parameter buffer implementation.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 5
 */
public final class BatchParameterBufferImp extends ParameterBufferBase implements BatchParameterBuffer {

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
