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
import org.firebirdsql.gds.ServiceRequestBuffer;
import org.firebirdsql.gds.impl.argument.ArgumentType;
import org.firebirdsql.gds.impl.argument.NumericArgument;

/**
 * Implementation of ServiceRequestBufferImp.
 */
public class ServiceRequestBufferImp extends ParameterBufferBase implements ServiceRequestBuffer {

    public ServiceRequestBufferImp(SrbMetaData srbMetaData, Encoding encoding) {
        super(srbMetaData, encoding);
    }

    @Override
    public void addArgument(int argumentType, byte value) {
        // TODO Handle through metadata behavior
        getArgumentsList().add(new NumericArgument(argumentType, ArgumentType.ByteSpb, value));
    }

    public enum SrbMetaData implements ParameterBufferMetaData {
        // TODO Add isc_spb_version3 as well?
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
                switch (tag) {
                case ISCConstants.isc_spb_rpr_commit_trans_64:
                case ISCConstants.isc_spb_rpr_rollback_trans_64:
                case ISCConstants.isc_spb_rpr_recover_two_phase_64:
                    return ArgumentType.BigIntSpb;
                default:
                    return ArgumentType.IntSpb;
                }
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

        @Override
        public void addPreamble(ParameterBuffer parameterBuffer) {
            // Do nothing
        }
    }
}