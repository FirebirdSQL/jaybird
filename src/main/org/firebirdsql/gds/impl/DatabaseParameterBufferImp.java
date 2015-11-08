/*
 * $Id$
 *
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
import org.firebirdsql.gds.DatabaseParameterBuffer;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ParameterTagMapping;

/**
 * Implementation for DatabaseParameterBuffer.
 */
public final class DatabaseParameterBufferImp extends ParameterBufferBase implements DatabaseParameterBufferExtension {

    public DatabaseParameterBufferImp(Encoding defaultEncoding) {
        super(ISCConstants.isc_dpb_version1, defaultEncoding);
    }

    @Override
    public DatabaseParameterBuffer deepCopy() {
        final DatabaseParameterBufferImp copy = new DatabaseParameterBufferImp(getDefaultEncoding());

        // All the Argument sub classes are immutable so to make a 'deep' copy this is all we have to do.
        copy.getArgumentsList().addAll(this.getArgumentsList());

        return copy;
    }

    @Override
    public DatabaseParameterBuffer removeExtensionParams() {
        final DatabaseParameterBuffer copy = deepCopy();

        for (int i = 0; i < DatabaseParameterBufferExtension.EXTENSION_PARAMETERS.length; i++) {
            copy.removeArgument(DatabaseParameterBufferExtension.EXTENSION_PARAMETERS[i]);
        }

        return copy;
    }

    @Override
    public ParameterTagMapping getTagMapping() {
        return ParameterTagMapping.DPB;
    }
}
