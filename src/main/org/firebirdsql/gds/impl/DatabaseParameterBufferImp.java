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

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.gds.DatabaseParameterBuffer;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ParameterTagMapping;
import org.firebirdsql.gds.impl.argument.ArgumentType;

import java.io.Serial;

/**
 * Implementation for DatabaseParameterBuffer.
 */
public final class DatabaseParameterBufferImp extends ParameterBufferBase implements DatabaseParameterBuffer {

    @Serial
    private static final long serialVersionUID = -4407431322168409761L;

    public DatabaseParameterBufferImp(DpbMetaData dpbMetaData, Encoding defaultEncoding) {
        super(dpbMetaData, defaultEncoding);
    }

    @Override
    public DatabaseParameterBuffer deepCopy() {
        final var copy =
                new DatabaseParameterBufferImp((DpbMetaData) getParameterBufferMetaData(), getDefaultEncoding());

        // All the Argument subclasses are immutable so to make a 'deep' copy this is all we have to do.
        copy.getArgumentsList().addAll(this.getArgumentsList());

        return copy;
    }

    @Override
    public ParameterTagMapping getTagMapping() {
        return ParameterTagMapping.DPB;
    }

    public enum DpbMetaData implements ParameterBufferMetaData {
        DPB_VERSION_1(ISCConstants.isc_dpb_version1, ArgumentType.TraditionalDpb),
        DPB_VERSION_2(ISCConstants.isc_dpb_version2, ArgumentType.Wide);

        private final int dpbVersion;
        private final ArgumentType argumentType;

        DpbMetaData(int dpbVersion, ArgumentType argumentType) {
            this.dpbVersion = dpbVersion;
            this.argumentType = argumentType;
        }

        @Override
        public final int getType() {
            return dpbVersion;
        }

        @Override
        public final ArgumentType getStringArgumentType(int tag) {
            return argumentType;
        }

        @Override
        public final ArgumentType getByteArrayArgumentType(int tag) {
            return argumentType;
        }

        @Override
        public final ArgumentType getIntegerArgumentType(int tag) {
            return argumentType;
        }

        @Override
        public final ArgumentType getSingleArgumentType(int tag) {
            return argumentType;
        }
    }
}
