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
package org.firebirdsql.gds.impl;

import org.firebirdsql.gds.BlobParameterBuffer;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ParameterBuffer;
import org.firebirdsql.gds.impl.argument.ArgumentType;

/**
 * Implementation of BlobParameterBuffer.
 */
public class BlobParameterBufferImp extends ParameterBufferBase implements BlobParameterBuffer {

    public BlobParameterBufferImp() {
        super(BpbMetaData.BPB_VERSION_1);
    }

    private enum BpbMetaData implements ParameterBufferMetaData {

        BPB_VERSION_1(ISCConstants.isc_bpb_version1);

        private final int bpbVersion;

        BpbMetaData(int bpbVersion) {
            this.bpbVersion = bpbVersion;
        }

        @Override
        public final int getType() {
            return bpbVersion;
        }

        @Override
        public final void addPreamble(ParameterBuffer parameterBuffer) {
            // Do nothing
        }

        @Override
        public final ArgumentType getStringArgumentType(int tag) {
            return ArgumentType.TraditionalDpb;
        }

        @Override
        public final ArgumentType getByteArrayArgumentType(int tag) {
            return ArgumentType.TraditionalDpb;
        }

        @Override
        public final ArgumentType getIntegerArgumentType(int tag) {
            return ArgumentType.TraditionalDpb;
        }
    }
}
