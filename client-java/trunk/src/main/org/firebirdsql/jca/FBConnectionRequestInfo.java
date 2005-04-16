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
   
    /**
     * Factory method to create new FBConnectionRequestInfo objects.
     * The FBConnectionRequestInfo object is bound to a given GDS 
     * implementation.
     * 
     * @param gds The GDS implementation upon which the created object 
     *            will be based
     * @return A new FBConnectionRequestInfo object bound to a given
     *         GDS implementation
     */            
    public static FBConnectionRequestInfo newInstance(GDS gds) {
        return new FBConnectionRequestInfo(gds.createDatabaseParameterBuffer());
        }

    /**
     * Perform a deep copy of this object, returning the copied instance.
     *
     * @return A deep-copied copy of this FBConnectionRequestInfo object
     */
    public FBConnectionRequestInfo deepCopy() {
        return new FBConnectionRequestInfo(c.deepCopy());
        }



   private FBConnectionRequestInfo(DatabaseParameterBuffer src) {
        c = src.deepCopy();
    }

    /**
     * Get the underlying Database Parameter Buffer for this object.     
     *
     * @return The underlying dpb for this object
     */
    public DatabaseParameterBuffer getDpb() {
        return c;
    }

    /**
     * Set a <code>String</code> property of this 
     * <code>FBConnectionRequestInfo</code> object. The  <code>type</code> 
     * parameter is one of the isc_dpb_* static final 
     * fields of {@link ISCConstants}.
     *
     * @param type The type of property, defined by ISCConstants.isc_dpb_*
     * @param content The content to set for the property
     */
    public void setProperty(int type, String content) {
        c.addArgument(type, content);
    }

    /**
     * Set a void property on this <code>FBConnectionRequestInfo</code> object.
     * The type is one of the isc_dpb_* static final fields of
     * {@link ISCConstants}.
     *
     * @param type The type of the property, defined by ISCConstants.isc_dpb_*
     */
    public void setProperty(int type) {
        c.addArgument(type);
    }

    /**
     * Set an <code>int</code> property on this 
     * <code>FBConnectionRequestInfo</code> object. The type is one of the
     * isc_dpb_* static final fields of {@link ISCConstants}.
     *
     * @param type The type of the property, defined by ISCConstants.isc_dpb_*
     * @param content The value to set for the property
     */
    public void setProperty(int type, int content) {
        c.addArgument(type, content);
    }

    /**
     * Set a <code>byte</code> array property on this
     * <code>FBConnectionRequestInfo</code> object. The type is one of the
     * isc_dpb_* static final fields of {@link ISCConstants}.
     *
     * @param type The type of the property, defined by ISCConstants.isc_dpb_*
     * @param content The value to set for the property
     */
    public void setProperty(int type, byte[] content) {
        c.addArgument(type, content);
    }

    /**
     * Get the <code>String</code> value of a property of this 
     * <code>FBConnectionRequestInfo</code> object. The type of the property
     * is given by one of the isc_dpb_* static final fields of 
     * {@link ISCConstants}.
     *
     * @param type The type of the property, defined by ISCConstants.isc_dpb_*
     * @return The <code>String</code> value of the requested property
     */
    public String getStringProperty(int type)
    {
        return c.getArgumentAsString(type);
    }

    /**
     * Get the <code>int</code> value of a property of this 
     * <code>FBConnectionRequestInfo</code> object. The type of the property
     * is given by one of the isc_dpb_* static final fields of 
     * {@link ISCConstants}.
     *
     * @param type The type of the property, defined by ISCConstants.isc_dpb_*
     * @return The <code>int</code> value of the requested property
     */
    public int getIntProperty(int type) {
        return c.getArgumentAsInt(type);
    }

    /**
     * Verify that this <code>FBConnectionRequestInfo</code> has a value 
     * set for the given property. Property is given by one of the 
     * isc_dpb_* static final fields of {@link ISCConstants}.
     *
     * @param type A value defined by ISCConstants.isc_dpb_*
     * @return <code>true</code> if the requested property has a value, 
     *         <code>false</code> otherwise
     */
    public boolean hasArgument(int type) {
        return c.hasArgument(type);
    }

    /**
     * Set the user property to be used in this 
     * <code>FBConnectionRequestInfo</code>
     *
     * @param user The value of the username to be set
     */
    public void setUser(String user) {
        setProperty(ISCConstants.isc_dpb_user_name, user);
    }

    /**
     * Get the user property for this <code>FBConnectionRequestInfo</code>
     *
     * @return The user property
     */
    public String getUser()
    {
        return getStringProperty(ISCConstants.isc_dpb_user_name);
    }


    /**
     * Set the password property to be used in this 
     * <code>FBConnectionRequestInfo</code>
     *
     * @param password The value of the password to be set
     */
    public void setPassword(String password) {
        setProperty(ISCConstants.isc_dpb_password, password);
    }

    /**
     * Get the password property for this <code>FBConnectionRequestInfo</code>
     *
     * @return The password property
     */
    public String getPassword()
    {
        return getStringProperty(ISCConstants.isc_dpb_password);
    }
    
    /**
     * Checks whether this instance is equal to another. Since 
     * connectionRequestInfo is defined specific to a resource adapter, the 
     * resource adapter is required to implement this method. The
     * conditions for equality are specific to the resource adapter.
     *  
     * @return <code>true</code> if the two instances are equal, 
     *         <code>false</code> otherwise
     */
    public boolean equals(Object other) {
        if ((other == null) || !(other instanceof FBConnectionRequestInfo)) {
            return false;
        }
        DatabaseParameterBuffer otherc = ((FBConnectionRequestInfo)other).c;
        return c.equals(otherc);
    }

    /**
     * Returns the hashCode of the ConnectionRequestInfo.
     * @return  hash code of this instance
     */
    public int hashCode() {
       return c.hashCode();
    }

    private final DatabaseParameterBuffer c;
}
