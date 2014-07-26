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
package org.firebirdsql.gds.impl.wire;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ServiceParameterBuffer;

/**
 * ngds implementation for ServiceParameterBuffer.
 */
class ServiceParameterBufferImp extends ParameterBufferBase implements ServiceParameterBuffer {

    @Override
    public int getLength() {
        return super.getLength() + 1;
    }

    @Override
    public void write(XdrOutputStream outputStream) throws IOException {
        outputStream.write(ISCConstants.isc_spb_current_version);
        super.write(outputStream);
    }
    
    public byte[] toByteArray() throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final XdrOutputStream outputStream = new XdrOutputStream(out, false);
        write(outputStream);
        return out.toByteArray();
    }

}