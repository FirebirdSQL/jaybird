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

package org.firebirdsql.jdbc;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;
import javax.naming.RefAddr;
import javax.naming.Reference;


/**
 * FBDataSourceObjectFactory.java
 *
 *
 * Created: Fri Jan 11 17:21:38 2002
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version
 */

public class FBDataSourceObjectFactory 
 implements ObjectFactory {

   private static final Map cfs = new HashMap();

   public FBDataSourceObjectFactory ()
   {
      
   }
   // implementation of javax.naming.spi.ObjectFactory interface

   /**
    *
    * @param param1 <description>
    * @param param2 <description>
    * @param param3 <description>
    * @param param4 <description>
    * @return <description>
    * @exception java.lang.Exception <description>
    */
   public synchronized Object getObjectInstance(Object obj, Name name, Context ctx, Hashtable env) throws Exception
   {
      if (cfs.containsKey(name)) 
      {
         return cfs.get(name);
      } // end of if ()
      FBWrappingDataSource ds = new FBWrappingDataSource();
      //This follows the example given for Catalina. 
      //see http://jakarta.apache.org/tomcat/tomcat-4.0-doc/jndi-resources-howto.html
      Reference ref = (Reference)obj;
      String dbName = (String)ref.get("DatabaseName").getContent();
      if (dbName == null) 
      {
         throw new IllegalArgumentException("You must supply a db name");      
      } // end of if ()
      ds.setDatabaseName(dbName);

      ds.setUser(get(ref, "User", null));

      ds.setPassword(get(ref, "Password", null));

      ds.setMinSize(Integer.parseInt(get(ref, "MinSize", "0")));

      ds.setMaxSize(Integer.parseInt(get(ref, "MaxSize", "0")));

      ds.setBlockingTimeout(Integer.parseInt(get(ref, "BlockingTimeout", "5000")));

      ds.setIdleTimeoutMinutes(Integer.parseInt(get(ref, "IdleTimeoutMinutes", "30")));

      ds.setPooling(true);

      cfs.put(name, ds);
      return ds;
   }

   private String get(final Reference ref, final String name, final String defaultValue)
   {
      RefAddr ra = ref.get(name);
      if (ra == null) 
      {
         return defaultValue;
      } // end of if ()
      return (String)ra.getContent();
   }
   
}// FBDataSourceObjectFactory
