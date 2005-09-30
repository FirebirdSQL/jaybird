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
package org.firebirdsql.jca;

import java.io.Serializable;
import javax.resource.cci.ConnectionSpec;
import javax.resource.spi.ConnectionRequestInfo;

import org.firebirdsql.gds.DatabaseParameterBuffer;
import org.firebirdsql.gds.impl.DatabaseParameterBufferExtension;

/**
 * The class <code>FBConnectionRequestInfo</code> holds a clumplet that is
 * used to store and transfer connection-specific information such as user,
 * password, and other dpb information..
 * 
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 2.0
 */

public class FBConnectionRequestInfo implements DatabaseParameterBufferExtension,
        ConnectionRequestInfo, ConnectionSpec, Serializable {

    private DatabaseParameterBuffer dpb;
    
    public FBConnectionRequestInfo(DatabaseParameterBuffer dpb) {
        this.dpb = dpb;
    }
    
    /**
     * Perform a deep copy of this object, returning the copied instance.
     * 
     * @return A deep-copied copy of this FBConnectionRequestInfo object
     */
    public DatabaseParameterBuffer deepCopy() {
        return new FBConnectionRequestInfo(dpb.deepCopy());
    }

    /**
     * Get the underlying Database Parameter Buffer for this object.
     * 
     * @return The underlying dpb for this object
     */
    public DatabaseParameterBuffer getDpb() {
        return dpb;
    }


    public void addArgument(int argumentType, byte[] content) {
        dpb.addArgument(argumentType, content);
    }


    public void addArgument(int argumentType, int value) {
        dpb.addArgument(argumentType, value);
    }


    public void addArgument(int argumentType, String value) {
        dpb.addArgument(argumentType, value);
    }


    public void addArgument(int argumentType) {
        dpb.addArgument(argumentType);
    }


    public int getArgumentAsInt(int argumentType) {
        return dpb.getArgumentAsInt(argumentType);
    }


    public String getArgumentAsString(int argumentType) {
        return dpb.getArgumentAsString(argumentType);
    }


    public boolean hasArgument(int argumentType) {
        return dpb.hasArgument(argumentType);
    }


    public void removeArgument(int argumentType) {
        dpb.removeArgument(argumentType);
    }

    public DatabaseParameterBuffer removeExtensionParams() {
        
        if (dpb instanceof DatabaseParameterBufferExtension)
            return ((DatabaseParameterBufferExtension)dpb).removeExtensionParams();
        else
            return dpb;
    }

    public void setUserName(String userName) {
        removeArgument(DatabaseParameterBufferExtension.USER_NAME);
        addArgument(DatabaseParameterBufferExtension.USER_NAME, userName);
    }
    
    public void setPassword(String password) {
        removeArgument(DatabaseParameterBufferExtension.PASSWORD);
        addArgument(DatabaseParameterBufferExtension.PASSWORD, password);
    }
}
