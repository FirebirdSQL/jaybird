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
package org.firebirdsql.gds.impl.jni;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ServiceParameterBuffer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * ngds implementation for ServiceParameterBuffer.
 */
class ServiceParameterBufferImp extends ParameterBufferBase implements
        ServiceParameterBuffer {

    /**
     * Package local method for obtaining buffer suitable for passing to native
     * method.
     *
     * @return Buffer for native method
     */
    byte[] toByteArray() {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        byteArrayOutputStream.write(ISCConstants.isc_spb_version);
        byteArrayOutputStream.write(ISCConstants.isc_spb_current_version);

        try {
            super.writeArgumentsTo(byteArrayOutputStream);
        } catch (IOException e) {
            // Ignoring IOException, not thrown by ByteArrayOutputStream
        }

        return byteArrayOutputStream.toByteArray();
    }
}
