// SPDX-FileCopyrightText: Copyright 2003 Ryan Baldwin
// SPDX-FileCopyrightText: Copyright 2005 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2014-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.impl;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ParameterBuffer;
import org.firebirdsql.gds.ParameterTagMapping;
import org.firebirdsql.gds.ServiceParameterBuffer;
import org.firebirdsql.gds.impl.argument.ArgumentType;

import java.io.Serial;

/**
 * Implementation of ServiceParameterBuffer.
 */
public class ServiceParameterBufferImp extends ParameterBufferBase implements ServiceParameterBuffer {

    @Serial
    private static final long serialVersionUID = 3112578847419907591L;

    /**
     * Creates an empty service parameter buffer
     */
    public ServiceParameterBufferImp(SpbMetaData spbMetaData, Encoding defaultEncoding) {
        super(spbMetaData, defaultEncoding);
    }

    @Override
    public ParameterTagMapping getTagMapping() {
        return ParameterTagMapping.SPB;
    }

    public enum SpbMetaData implements ParameterBufferMetaData {

        // TODO Unsure if we need the versions without _ATTACH

        SPB_VERSION_2_ATTACH(ISCConstants.isc_spb_version) {
            @Override
            public void addPreamble(ParameterBuffer parameterBuffer) {
                parameterBuffer.addArgument(ISCConstants.isc_spb_current_version);
            }

            @Override
            public ArgumentType getStringArgumentType(int tag) {
                return ArgumentType.TraditionalDpb;
            }

            @Override
            public ArgumentType getByteArrayArgumentType(int tag) {
                return ArgumentType.TraditionalDpb;
            }

            @Override
            public ArgumentType getIntegerArgumentType(int tag) {
                return ArgumentType.TraditionalDpb;
            }

            @Override
            public ArgumentType getSingleArgumentType(int tag) {
                if (tag == ISCConstants.isc_spb_current_version) {
                    return ArgumentType.SingleTpb;
                }
                return ArgumentType.TraditionalDpb;
            }

            @Override
            public boolean isUpgradable() {
                return true;
            }

            @Override
            public ParameterBufferMetaData upgradeMetaData() {
                return SPB_VERSION_3_ATTACH;
            }
        },
        // Technically this has nothing to do with SPB version 2/3
        SPB_VERSION_2(ISCConstants.isc_spb_current_version) {
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
        },
        SPB_VERSION_3_ATTACH(ISCConstants.isc_spb_version3) {
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
        };

        private final int spbVersion;

        SpbMetaData(int spbVersion) {
            this.spbVersion = spbVersion;
        }

        @Override
        public final int getType() {
            return spbVersion;
        }
    }
}