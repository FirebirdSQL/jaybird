// SPDX-FileCopyrightText: Copyright 2004-2005 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2004 Gabriel Reid
// SPDX-FileCopyrightText: Copyright 2012-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.impl;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ServiceRequestBuffer;
import org.firebirdsql.gds.impl.argument.ArgumentType;

import java.io.Serial;

/**
 * Implementation of ServiceRequestBufferImp.
 */
public class ServiceRequestBufferImp extends ParameterBufferBase implements ServiceRequestBuffer {

    @Serial
    private static final long serialVersionUID = -6651365729319455905L;

    public ServiceRequestBufferImp(SrbMetaData srbMetaData, Encoding encoding) {
        super(srbMetaData, encoding);
    }

    public enum SrbMetaData implements ParameterBufferMetaData {
        // Technically this has nothing to do with SPB version 2/3
        SRB_VERSION_2(ISCConstants.isc_spb_current_version) {
            // TODO Check if correct and add additional types
            @Override
            public ArgumentType getStringArgumentType(int tag) {
                return ArgumentType.StringSpb;
            }

            @Override
            public ArgumentType getByteArrayArgumentType(int tag) {
                return ArgumentType.StringSpb;
            }

            @Override
            public ArgumentType getIntegerArgumentType(int tag) {
                return switch (tag) {
                    case ISCConstants.isc_spb_rpr_commit_trans_64,
                            ISCConstants.isc_spb_rpr_rollback_trans_64,
                            ISCConstants.isc_spb_rpr_recover_two_phase_64 -> ArgumentType.BigIntSpb;
                    default -> ArgumentType.IntSpb;
                };
            }

            @Override
            public ArgumentType getSingleArgumentType(int tag) {
                return ArgumentType.SingleTpb;
            }

            @Override
            public ArgumentType getByteArgumentType(int tag) {
                return ArgumentType.ByteSpb;
            }
        };

        private final int spbVersion;

        SrbMetaData(int spbVersion) {
            this.spbVersion = spbVersion;
        }

        @Override
        public final int getType() {
            return spbVersion;
        }
    }
}