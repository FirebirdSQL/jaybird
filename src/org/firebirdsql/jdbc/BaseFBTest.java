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

/*
 * CVS modification log:
 * $Log$
 * Revision 1.3  2002/01/06 23:37:58  d_jencks
 * added a connection test to datasource test, cleaned up constants a bit.
 *
 * Revision 1.2  2001/08/28 17:13:23  d_jencks
 * Improved formatting slightly, removed dos cr's
 *
 * Revision 1.1  2001/07/13 18:16:15  d_jencks
 * Implementation of jdbc 1.0 Driver contributed by Roman Rokytskyy
 *
 * Revision 1.1  2001/07/09 09:09:51  rrokytskyy
 * Initial revision
 *
 */

package org.firebirdsql.jdbc;

import junit.framework.*;
import org.firebirdsql.management.*;


/**
 * Class containing test-related constants. It should be changed depending
 * on the particular environment.
 *
 * @author Roman Rokytskyy (rrokytskyy@yahoo.co.uk)
 */
public class BaseFBTest extends TestCase
{
    /**
     * Default URL for the test
     */
   private static final String dbPath = System.getProperty("test.db.dir");

   //static final String dbName = "localhost:" + dbPath + "/testdb.gdb";
   //static final String dbName2 = "localhost:" + dbPath + "/testdb2.gdb";

   //private static final String PERSONAL_DB_LOCATION = "/usr/local/firebird/dev/client-java/db/fbmctest.gdb";


   public static final String DB_SERVER_URL = "localhost";
   public static final int DB_SERVER_PORT = 3050;

   public static final String DB_NAME = dbPath +  "/fbtest.gdb";

   public static final String DB_DATASOURCE_URL = DB_SERVER_URL + "/" + DB_SERVER_PORT + ":" + DB_NAME;
   public static final String DB_DRIVER_URL = FBDriver.FIREBIRD_PROTOCOL + DB_DATASOURCE_URL;



    /**
     * Default user name for database connection
     */
    public static final String DB_USER = "sysdba";

    /**
     * Password for the default user for database connection
     */
    public static final String DB_PASSWORD = "masterkey";

    /**
     * Default properties for database connection
     */
    public static final java.util.Properties DB_INFO =
        new java.util.Properties();

    // set up info properties
    static{
        DB_INFO.setProperty(FBDriver.USER, DB_USER);
        DB_INFO.setProperty(FBDriver.PASSWORD, DB_PASSWORD);
    }

   private final FBManager fbManager = new FBManager();

    public BaseFBTest(String testName) {
        super(testName);
    }

   protected void setUp() throws Exception
   {
      try 
      {
         fbManager.setURL(DB_SERVER_URL);
         fbManager.setPort(DB_SERVER_PORT);
         fbManager.start();
         fbManager.createDatabase(DB_NAME);
      }
      catch (Exception e)
      {
         System.out.println("exception in setup of " + getName() + ": " + e);
         e.printStackTrace(); 
      } // end of try-catch
   }

   protected void tearDown() throws Exception
   {
      try 
      {
         fbManager.dropDatabase(DB_NAME);
         fbManager.stop();
      }
      catch (Exception e)
      {
         System.out.println("exception in teardown of " + getName() + ": " + e);
         e.printStackTrace(); 
      } // end of try-catch
      
   }
}
