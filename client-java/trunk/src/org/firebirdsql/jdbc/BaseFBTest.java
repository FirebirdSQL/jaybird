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
 * Revision 1.7  2002/06/04 01:17:49  brodsom
 * performance patches
 *
 * Revision 1.6  2002/04/29 21:35:42  rrokytskyy
 * added lc_ctype to initial parameters
 *
 * Revision 1.5  2002/02/04 04:35:51  d_jencks
 * modified test setup to run against remote server using command line properties
 *
 * Revision 1.4  2002/02/03 02:45:39  d_jencks
 * Fixed the rest of the bugs! The testsuite now all passes
 *
 * Revision 1.3  2002/02/02 18:58:24  d_jencks
 * converted to log4j logging and cleaned up some test problems.  If you do not wish to use log4j, you may leave out the log4j-core.jar and get no logging
 *
 * Revision 1.2  2002/01/07 16:32:04  d_jencks
 * Fixed FBManager to require user and password to create a db: added these to setup/teardown for tests.
 *
 * Revision 1.1  2002/01/07 06:59:54  d_jencks
 * Revised FBManager to create dialect 3 databases, and the tests to use a newly created database. Simplified and unified test constants. Test targets are now all-tests for all tests and one-test for one test: specify the test as -Dtest=Gds one-test for the TestGds.class test.  Made a few other small changes to improve error messages
 *
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
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;


/**
 * Class containing test-related constants. It should be changed depending
 * on the particular environment.
 *
 * @author Roman Rokytskyy (rrokytskyy@yahoo.co.uk)
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 */
public class BaseFBTest extends TestCase
{


    /**
     * Default URL for the test
     */
   private static final String DB_PATH = System.getProperty("test.db.dir", "");


   public static final String DB_SERVER_URL = System.getProperty("test.db.host", "localhost");
   public static final int DB_SERVER_PORT = Integer.parseInt(System.getProperty("test.db.port", "3050"));

   public static final String DB_NAME = "fbtest.gdb";

   public static final String getdbpath(String name)
   {
      return DB_SERVER_URL + "/" + DB_SERVER_PORT + ":" + DB_PATH + "/" + name;
   }

   public static final String DB_DATASOURCE_URL = getdbpath(DB_NAME);
   public static final String DB_DRIVER_URL = FBDriver.FIREBIRD_PROTOCOL + DB_DATASOURCE_URL;



    /**
     * Default user name for database connection
     */
    public static final String DB_USER = "sysdba";

    /**
     * Password for the default user for database connection
     */
    public static final String DB_PASSWORD = "masterkey";
    
    public static final String DB_LC_CTYPE = "NONE";

    /**
     * Default properties for database connection
     */
    public static final java.util.Properties DB_INFO =
        new java.util.Properties();

    // set up info properties
    static{
        DB_INFO.setProperty(FBDriver.USER, DB_USER);
        DB_INFO.setProperty(FBDriver.PASSWORD, DB_PASSWORD);
        DB_INFO.setProperty("lc_ctype", DB_LC_CTYPE);
    }

   private final FBManager fbManager = new FBManager();

   protected final Logger log = LoggerFactory.getLogger(getClass());

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
         fbManager.createDatabase(DB_PATH + "/" + DB_NAME, DB_USER, DB_PASSWORD);
      }
      catch (Exception e)
      {
         if (log!=null) log.warn("exception in setup of " + getName() + ": ", e);
      } // end of try-catch
   }

   protected void tearDown() throws Exception
   {
      try 
      {
         fbManager.dropDatabase(DB_NAME, DB_USER, DB_PASSWORD);
         fbManager.stop();
      }
      catch (Exception e)
      {
         if (log!=null) log.warn("exception in teardown of " + getName() + ": ", e);
      } // end of try-catch
      
   }
}
