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

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.TransactionParameterBuffer;

/**
 * Implementation of the {@link org.firebirdsql.gds.TransactionParameterBuffer} interface.
 */
public final class TransactionParameterBufferImpl extends ParameterBufferBase implements TransactionParameterBuffer {

    public TransactionParameterBufferImpl() {
        super(ISCConstants.isc_tpb_version3);
    }

    @Override
    public TransactionParameterBuffer deepCopy() {
        final TransactionParameterBufferImpl result = new TransactionParameterBufferImpl();

        result.getArgumentsList().addAll(this.getArgumentsList());

        return result;
    }

}
