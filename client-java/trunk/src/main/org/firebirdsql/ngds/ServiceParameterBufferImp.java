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

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ServiceParameterBuffer;

import java.io.ByteArrayOutputStream;

/**
 * ngds implementation for ServiceParameterBuffer.
 */
class ServiceParameterBufferImp extends ServiceBufferBase implements ServiceParameterBuffer
    {
    // We will overide the addArgument(int argumentType, String value) method.

    public void addArgument(int argumentType, String value)
        {
        getArgumentsList().add(new StringArgument(argumentType, value ));
        }

    private static final class StringArgument extends ServiceBufferBase.Argument
        {
        StringArgument( int type, String value )
            {
            this.type = type;
            this.value = value;
            }

        void writeTo(ByteArrayOutputStream outputStream)
            {
            outputStream.write(type);

            final byte[] valueBytes = this.value.getBytes();
            final int valueLength = valueBytes.length;

            outputStream.write(valueLength);
            for(int i = 0; i<valueLength; i++)
                outputStream.write(valueBytes[i]);
            }

        private int type;
        private String value;
        }

    /**
     * Pacakage local method for obtaining buffer suitable for passing to native method.
     *
     * @return
     */
    byte[] toByteArray()
        {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        byteArrayOutputStream.write(ISCConstants.isc_spb_version);
        byteArrayOutputStream.write(ISCConstants.isc_spb_current_version);

        super.writeArgumentsTo(byteArrayOutputStream);

        return byteArrayOutputStream.toByteArray();
        }
    }
