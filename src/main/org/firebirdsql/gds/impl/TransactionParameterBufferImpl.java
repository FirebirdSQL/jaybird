// SPDX-FileCopyrightText: Copyright 2005 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2014-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.impl;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.impl.argument.ArgumentType;

import java.io.Serial;

/**
 * Implementation of the {@link org.firebirdsql.gds.TransactionParameterBuffer} interface.
 */
public final class TransactionParameterBufferImpl extends ParameterBufferBase implements TransactionParameterBuffer {

    @Serial
    private static final long serialVersionUID = 7800513617882155482L;

    public TransactionParameterBufferImpl() {
        super(TpbMetaData.TPB_VERSION_3);
    }

    @Override
    public TransactionParameterBuffer deepCopy() {
        var result = new TransactionParameterBufferImpl();
        copyTo(result);
        return result;
    }

    @Override
    public void copyTo(TransactionParameterBuffer destination) {
        if (destination instanceof TransactionParameterBufferImpl tpbImpl) {
            tpbImpl.getArgumentsList().addAll(this.getArgumentsList());
        } else {
            TransactionParameterBuffer.super.copyTo(destination);
        }
    }

    public enum TpbMetaData implements ParameterBufferMetaData {
        TPB_VERSION_3(ISCConstants.isc_tpb_version3);

        private final int tpbVersion;

        TpbMetaData(int tpbVersion) {
            this.tpbVersion = tpbVersion;
        }

        @Override
        public final int getType() {
            return tpbVersion;
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
            return ArgumentType.SingleTpb;
        }
    }
}
