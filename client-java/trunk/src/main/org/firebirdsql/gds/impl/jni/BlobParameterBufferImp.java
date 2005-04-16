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

package org.firebirdsql.gds.impl.jni;

import org.firebirdsql.gds.BlobParameterBuffer;
import org.firebirdsql.gds.ISCConstants;

import java.io.ByteArrayOutputStream;

/**
 *
 */
public class BlobParameterBufferImp extends ParameterBufferBase implements BlobParameterBuffer
    {
    public BlobParameterBufferImp()
        {
        super();
        }

    public void addArgument(int argumentType, int value)
        {
		if(value > 65535)
			throw new RuntimeException("Blob parameter buffer value out of range for type "+argumentType); 
					
        getArgumentsList().add(new NumericArgument(argumentType, value)
            {
            protected void writeValue(ByteArrayOutputStream outputStream, int value)
                {
                outputStream.write(2);
                outputStream.write(value);
                outputStream.write(value>>8);
                }
            });
        }

    /**
     * Pacakage local method for obtaining buffer suitable for passing to native method.
     *
     * @return
     */
    byte[] getBytesForNativeCode()
        {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        byteArrayOutputStream.write(ISCConstants.isc_bpb_version1);

        super.writeArgumentsTo(byteArrayOutputStream);

        return byteArrayOutputStream.toByteArray();
        }
    }
