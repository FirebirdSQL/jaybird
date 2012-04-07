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

import javax.resource.spi.ManagedConnectionMetaData;
import javax.resource.ResourceException;

import org.firebirdsql.gds.GDSException;


/**
 * The class <code>FBManagedConnectionMetaData</code> implements 
 * <code>javax.resource.sqi.ManagedConnectionMetaData</code>, providing almost 
 * no useful information.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public class FBManagedConnectionMetaData implements ManagedConnectionMetaData {

    private final FBManagedConnection mc;

    FBManagedConnectionMetaData(final FBManagedConnection mc)
    {
       this.mc = mc;
    }

    /**
     * Returns Product name of the underlying EIS instance connected through 
     * the <code>ManagedConnection</code>.  
     *
     * @return Product name of the EIS instance.  
     * @throws ResourceException generic exception
     */
     public String getEISProductName() throws ResourceException {
         try {
             return mc.getGDSHelper().getIscDBHandle().getDatabaseProductName();
         } catch(GDSException ex) {
             throw new FBResourceException(ex);
         }
     }

     /**
      * Returns product version of the underlying EIS instance connected 
      * through the <code>ManagedConnection</code>.  
      *
      * @return Product version of the EIS instance
      * @throws ResourceException generic exception
      */
     public String getEISProductVersion() throws ResourceException {
         try {
             return mc.getGDSHelper().getIscDBHandle().getDatabaseProductVersion();
         } catch(GDSException ex) {
             throw new FBResourceException(ex);
         }
     }



    /** Returns maximum limit on number of active concurrent connections that 
     * an EIS instance can support across client processes. If an EIS instance 
     * does not know about (or does not have) any such limit, it returns a 0.
     *
     * @return Maximum limit for number of active concurrent connections
     * @throws ResourceException generic exception
     */
    public int getMaxConnections() throws ResourceException {
        return 0;
    }

    /**
     * Returns name of the user associated with the 
     * <code>ManagedConnection</code> instance. The name corresponds to the 
     * resource principal under whose whose security context, a connection to 
     * the EIS instance has been established.
     *
     * @return name of the user
     * @throws ResourceException generic exception
     */
    public String getUserName() throws ResourceException {
        try {
            return mc.getGDSHelper().getUserName();
        } catch(GDSException ex) {
            throw new FBResourceException(ex);
        }
    }

 }
