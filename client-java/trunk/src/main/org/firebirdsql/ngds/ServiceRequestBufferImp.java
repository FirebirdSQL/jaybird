/*
 * Firebird Open Source J2ee connector - jdbc driver
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
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.ngds;

import org.firebirdsql.gds.ServiceRequestBuffer;

import java.io.ByteArrayOutputStream;

/**
 * ngds implementation for ServiceRequestBufferImp.
 */
class ServiceRequestBufferImp extends ServiceBufferBase implements ServiceRequestBuffer
    {
    /**
     * Every ServiceRequestBuffer has an associated taskIdentifier.
     *
     * @param taskIdentifier
     */
    ServiceRequestBufferImp(int taskIdentifier)
        {
        this.taskIdentifier = taskIdentifier;
        }

    /**
     * Pacakage local method for obtaining buffer suitable for passing to native method.
     *
     * @return
     */
    byte[] toByteArray()
        {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        byteArrayOutputStream.write(taskIdentifier);

        super.writeArgumentsTo(byteArrayOutputStream);

        return byteArrayOutputStream.toByteArray();
        }

    // PRIVATE MEMBERS

    private int taskIdentifier;
    }
