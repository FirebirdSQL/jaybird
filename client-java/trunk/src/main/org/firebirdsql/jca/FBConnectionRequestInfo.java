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

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.GDS;
import org.firebirdsql.gds.DatabaseParameterBuffer;



/**
 * The class <code>FBConnectionRequestInfo</code> holds a clumplet that is used
 * to store and transfer connection-specific information such as user, password,
 * and other dpb information..
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */

public class FBConnectionRequestInfo 
    implements ConnectionRequestInfo, ConnectionSpec, Serializable
{
    public static FBConnectionRequestInfo newInstance(GDS gds)
        {
        return new FBConnectionRequestInfo(gds.newDatabaseParameterBuffer());
        }

    public FBConnectionRequestInfo deepCopy()
        {
        return new FBConnectionRequestInfo(c.deepCopy());
        }



   private FBConnectionRequestInfo(DatabaseParameterBuffer src) {
        c = src.deepCopy();
    }

    public DatabaseParameterBuffer getDpb() {
        return c;
    }

    public void setProperty(int type, String content) {
        c.addArgument(type, content);
    }

    public void setProperty(int type) {
        c.addArgument(type);
    }

    public void setProperty(int type, int content) {
        c.addArgument(type, content);
    }

    public void setProperty(int type, byte[] content) {
        c.addArgument(type, content);
    }

    public String getStringProperty(int type)
    {
        return c.getArgumentAsString(type);
    }



    public void setUser(String user) {
        setProperty(ISCConstants.isc_dpb_user_name, user);
    }

    public String getUser()
    {
        return getStringProperty(ISCConstants.isc_dpb_user_name);
    }

    public void setPassword(String password) {
        setProperty(ISCConstants.isc_dpb_password, password);
    }

    public String getPassword()
    {
        return getStringProperty(ISCConstants.isc_dpb_password);
    }
    
    /**
     Checks whether this instance is equal to another. Since connectionRequestInfo is defined
     specific to a resource adapter, the resource adapter is required to implement this method. The
     conditions for equality are specific to the resource adapter.
     Overrides:
         equals in class java.lang.Object
     Returns:
         True if the two instances are equal.
    **/


    public boolean equals(Object other) {
        if ((other == null) || !(other instanceof FBConnectionRequestInfo)) {
            return false;
        }
        DatabaseParameterBuffer otherc = ((FBConnectionRequestInfo)other).c;
        return c.equals(otherc);
    }

    /**
     Returns the hashCode of the ConnectionRequestInfo.
     Overrides:
         hashCode in class java.lang.Object
     Returns:
         hash code os this instance
    **/

    public int hashCode() {
       return c.hashCode();
    }


    private final DatabaseParameterBuffer c;
    }
