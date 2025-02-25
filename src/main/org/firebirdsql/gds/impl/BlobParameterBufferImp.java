// SPDX-FileCopyrightText: Copyright 2003 Ryan Baldwin
// SPDX-FileCopyrightText: Copyright 2005 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2014-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.impl;

import org.firebirdsql.gds.BlobParameterBuffer;
import org.firebirdsql.gds.impl.argument.ArgumentType;
import org.firebirdsql.jaybird.fb.constants.BpbItems;

import java.io.Serial;

/**
 * Implementation of BlobParameterBuffer.
 */
public class BlobParameterBufferImp extends ParameterBufferBase implements BlobParameterBuffer {

    @Serial
    private static final long serialVersionUID = 6938419219898280131L;

    public BlobParameterBufferImp() {
        super(BpbMetaData.BPB_VERSION_1);
    }

    private enum BpbMetaData implements ParameterBufferMetaData {

        BPB_VERSION_1(BpbItems.isc_bpb_version1);

        private final int bpbVersion;

        BpbMetaData(int bpbVersion) {
            this.bpbVersion = bpbVersion;
        }

        @Override
        public final int getType() {
            return bpbVersion;
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

        @Override
        public final ArgumentType getSingleArgumentType(int tag) {
            return ArgumentType.TraditionalDpb;
        }
    }
}
