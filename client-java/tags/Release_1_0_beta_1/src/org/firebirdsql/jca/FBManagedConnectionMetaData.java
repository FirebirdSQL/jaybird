/*   The contents of this file are subject to the Mozilla Public
 *   License Version 1.1 (the "License"); you may not use this file
 *   except in compliance with the License. You may obtain a copy of
 *   the License at http://www.mozilla.org/MPL/
 *   Alternatively, the contents of this file may be used under the
 *   terms of the GNU Lesser General Public License Version 2 or later (the
 *   "LGPL"), in which case the provisions of the GPL are applicable
 *   instead of those above. You may obtain a copy of the Licence at
 *   http://www.gnu.org/copyleft/lgpl.html
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    relevant License for more details.
 *
 *    This file was created by members of the firebird development team.
 *    All individual contributions remain the Copyright (C) of those
 *    individuals.  Contributors to this file are either listed here or
 *    can be obtained from a CVS history command.
 *
 *    All rights reserved.

 */

package org.firebirdsql.jca;


// imports --------------------------------------
import javax.resource.spi.ManagedConnectionMetaData;

import javax.resource.ResourceException;


/**
 *
 *   @see <related>
 *   @author David Jencks (davidjencks@earthlink.net)
 *   @version $ $
 */

 public class FBManagedConnectionMetaData implements ManagedConnectionMetaData {

    private final FBManagedConnection mc;

    FBManagedConnectionMetaData(final FBManagedConnection mc)
    {
       this.mc = mc;
    }

    /**
     Returns Product name of the underlying EIS instance connected through the
     ManagedConnection.
     Returns:
         Product name of the EIS instance.
    **/
     public java.lang.String getEISProductName() throws ResourceException {
        throw new ResourceException("Not yet implemented");;
     }

     /**
     Returns product version of the underlying EIS instance connected through the
     ManagedConnection.
     Returns:
         Product version of the EIS instance
    **/
     public java.lang.String getEISProductVersion() throws ResourceException {
         throw new ResourceException("not yet implemented");
     }



    /**
     Returns maximum limit on number of active concurrent connections that an EIS instance can
     support across client processes. If an EIS instance does not know about (or does not have) any
     such limit, it returns a 0.
     Returns:
         Maximum limit for number of active concurrent connections
    **/

    public int getMaxConnections() throws ResourceException {
        return 0;
    }

    /**
     Returns name of the user associated with the ManagedConnection instance. The name
     corresponds to the resource principal under whose whose security context, a connection to the
     EIS instance has been established.
     Returns:
         name of the user
    **/

    public java.lang.String getUserName() throws ResourceException {
        return mc.getUserName();
    }

 }
