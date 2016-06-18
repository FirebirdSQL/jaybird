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

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ParameterBuffer;
import org.firebirdsql.gds.ParameterTagMapping;
import org.firebirdsql.gds.ServiceParameterBuffer;
import org.firebirdsql.gds.impl.argument.ArgumentType;

/**
 * Implementation of ServiceParameterBuffer.
 */
public class ServiceParameterBufferImp extends ParameterBufferBase implements ServiceParameterBuffer {

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
        },
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
                switch (tag) {
                case ISCConstants.isc_spb_rpr_commit_trans_64:
                case ISCConstants.isc_spb_rpr_rollback_trans_64:
                case ISCConstants.isc_spb_rpr_recover_two_phase_64:
                    return ArgumentType.BigIntSpb;
                default:
                    return ArgumentType.IntSpb;
                }
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
        };

        private final int spbVersion;

        SpbMetaData(int spbVersion) {
            this.spbVersion = spbVersion;
        }

        @Override
        public final int getType() {
            return spbVersion;
        }

        @Override
        public void addPreamble(ParameterBuffer parameterBuffer) {
            // Do nothing
        }
    }
}