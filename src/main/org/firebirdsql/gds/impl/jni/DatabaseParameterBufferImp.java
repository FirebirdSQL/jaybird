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

import org.firebirdsql.gds.DatabaseParameterBuffer;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.impl.DatabaseParameterBufferExtension;

import java.io.ByteArrayOutputStream;

/**
 * 
 */
public class DatabaseParameterBufferImp extends ParameterBufferBase implements
        DatabaseParameterBufferExtension {

    public DatabaseParameterBufferImp() {
        super();
    }

    public DatabaseParameterBuffer deepCopy() {
        final DatabaseParameterBufferImp copy = new DatabaseParameterBufferImp();

        // All the Argument sub classes are immutable so to make a 'deep' copy
        // this is all we have to do.
        copy.getArgumentsList().addAll(this.getArgumentsList());

        return copy;
    }

    /**
     * Pacakage local method for obtaining buffer suitable for passing to native
     * method.
     * 
     * @return
     */
    byte[] getBytesForNativeCode() {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        byteArrayOutputStream.write(ISCConstants.isc_dpb_version1);

        super.writeArgumentsTo(byteArrayOutputStream);

        return byteArrayOutputStream.toByteArray();
    }

    public DatabaseParameterBuffer removeExtensionParams() {
        DatabaseParameterBuffer copy = deepCopy();
        
        for (int i = 0; i < DatabaseParameterBufferExtension.EXTENSION_PARAMETERS.length; i++) {
            copy.removeArgument(
                DatabaseParameterBufferExtension.EXTENSION_PARAMETERS[i]);
        }
        
        return copy;
    }
    
    
}
